/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2017
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/
package com.ericsson.component.aia.sdk.templatemanager.publisher;

import static com.ericsson.component.aia.sdk.templatemanager.TemplateManagerConstants.HYPHEN;
import static com.ericsson.component.aia.sdk.templatemanager.TemplateManagerConstants.PBA_TEMPLATE_INFO_ID;
import static com.ericsson.component.aia.sdk.templatemanager.TemplateManagerConstants.UTF_8_ENCODING;
import static java.nio.file.StandardCopyOption.COPY_ATTRIBUTES;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Optional;
import java.util.stream.Stream;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.aia.metadata.api.MetaDataServiceIfc;
import com.ericsson.aia.metadata.exception.MetaDataServiceException;
import com.ericsson.component.aia.sdk.git.repo.service.GitSshService;
import com.ericsson.component.aia.sdk.pba.model.AuthorInfo;
import com.ericsson.component.aia.sdk.pba.model.PBAInstance;
import com.ericsson.component.aia.sdk.pba.model.Pba;
import com.ericsson.component.aia.sdk.pba.model.PbaInfo;
import com.ericsson.component.aia.sdk.pba.tools.PBASchemaTool;
import com.ericsson.component.aia.sdk.templatemanager.exception.AppSdkException;
import com.ericsson.component.aia.sdk.templatemanager.exception.TemplateManagerException;
import com.ericsson.component.aia.sdk.templatemanager.exception.WrongEnvironmentGitUrlException;
import com.ericsson.component.aia.sdk.templatemanager.exception.code.TemplateManagerExceptionCodes;
import com.ericsson.component.aia.sdk.templatemanager.impl.GitOperation;
import com.ericsson.component.aia.sdk.templatemanager.impl.TemplateManagerConfiguration;
import com.ericsson.component.aia.sdk.templatemanager.impl.TemplateManagerImpl;
import com.ericsson.component.aia.sdk.templatemanager.util.PbaValidatorUtils;
import com.ericsson.component.aia.sdk.templatemanager.util.TemplateManagerUtil;
import com.ericsson.component.aia.sdk.util.docker.dependency.DependencyChecker;
import com.ericsson.component.aia.sdk.util.docker.exception.SdkDockerImageNotFoundException;
import com.ericsson.component.aia.sdk.util.docker.exception.SdkPbaNotFoundException;

/**
 * This class is used to publish templates.
 */
public class TemplatePublisher {

    private static final String PBA_JSON = "pba.json";
    private static final String ZIP = ".zip";

    private static final Logger LOGGER = LoggerFactory.getLogger(TemplateManagerImpl.class);

    private final MetaDataServiceIfc metaDataService;
    private final PBASchemaTool pbaSchemaTool;

    private final GitSshService gitRepository;
    private final GitOperation gitOperation;
    private final DependencyChecker dependencyChecker;

    /**
     * Instantiates a new template publisher.
     *
     * @param metaDataService
     *            the meta data service
     * @param pbaSchemaTool
     *            the pba schema tool
     * @param gitRepository
     *            the git repository
     * @param gitOperation
     *            the git operation
     * @param dependencyChecker
     *            the dependency checker
     */
    public TemplatePublisher(final MetaDataServiceIfc metaDataService, final PBASchemaTool pbaSchemaTool, final GitSshService gitRepository,
                             final GitOperation gitOperation, final DependencyChecker dependencyChecker) {
        this.metaDataService = metaDataService;
        this.pbaSchemaTool = pbaSchemaTool;
        this.gitRepository = gitRepository;
        this.gitOperation = gitOperation;
        this.dependencyChecker = dependencyChecker;

    }

    /**
     * Publish template represented by an inputStream.
     *
     * @param input
     *            the input
     * @param fileName
     *            the file name
     * @param version
     *            the version
     * @return true, if successful
     */
    public boolean publishTemplate(final InputStream input, final String fileName, final String version) {

        final Path templatePath = copyUploadedFileToServer(input, fileName);
        final PBAInstance pbaModel = pbaSchemaTool.getPBAModelInstance(extractPbaFromZip(templatePath));
        final Pba pba = pbaModel.getPba();

        final PbaInfo templatePbaInfo = pba.getTemplateInfo();
        final String name = templatePbaInfo.getName();
        templatePbaInfo.setVersion(version);
        try {
            dependencyChecker.verifyDependenciesExist(pba);
        } catch (final SdkDockerImageNotFoundException | SdkPbaNotFoundException ex) {
            throw new TemplateManagerException(TemplateManagerExceptionCodes.DOCKER_IMAGE_NOT_FOUND_FOR_DEPENDENCY, ex);
        }

        try {
            LOGGER.info("Publishing template method invoked with name {}, version {}", name, version);
            final String scmUrl = cloneFromGitRepository(version, templatePath, pbaModel, pba, name);

            if (!gitRepository.checkGitRepoExists(scmUrl)) {
                return false;
            }

            final String templateId = TemplateManagerUtil.createTemplateId(pbaModel.getPba());

            metaDataService.put(TemplateManagerConfiguration.templateCatalogName, PBA_TEMPLATE_INFO_ID, templateId,
                    pbaSchemaTool.convertToJsonString(pbaModel));

            return true;
        } catch (final MetaDataServiceException | IOException exp) {
            throw new TemplateManagerException(TemplateManagerExceptionCodes.ERROR_INVOKING_METADATASERVICE_ON_PUBLISHED,
                    String.format("Publish operation failed for template with name %s and version %s", name, version), exp);
        }
    }

    private String cloneFromGitRepository(final String version, final Path templatePath, final PBAInstance pbaModel, final Pba pba, final String name)
            throws IOException {
        final AuthorInfo authorInfo = pba.getAuthorInfo();

        final String scmUrl = gitOperation.getGitRepoUrl(pba);

        try {
            PbaValidatorUtils.validateScmUrl(scmUrl, TemplateManagerConfiguration.gitServiceUrl);
        } catch (final WrongEnvironmentGitUrlException e) {
            throw new AppSdkException(TemplateManagerExceptionCodes.WRONG_ENVIRONMENT_GIT_URL, e.getMessage());
        }

        final String tag = name + HYPHEN + version;
        LOGGER.info("Cloning git repository for template with scmUrl: {} and name: {}", scmUrl, name);
        final Path newGitRepo = gitRepository.clone(scmUrl, name);
        pbaModel.getPba().getScmInfo().setScm(scmUrl);
        pbaModel.getPba().getScmInfo().setScmTag(tag);
        removeOldZipFile(newGitRepo);

        gitRepository.checkoutBranch(newGitRepo, name);
        final String jsonPba = pbaSchemaTool.convertToJsonString(pbaModel);
        LOGGER.trace("New json pba generated: {}", jsonPba);
        updateScmInfoInProject(templatePath, jsonPba);

        TemplateManagerUtil.updateTemplateReadMe(templatePath, newGitRepo);
        Files.copy(templatePath, newGitRepo.resolve(name + ".zip"), REPLACE_EXISTING, COPY_ATTRIBUTES);

        final String commitMessage = String.format("Publishing Template:%s Version:%s", name, version);
        LOGGER.info("Pushing to git repo with name {}, version {}, tag {}", name, version, tag);
        gitRepository.pushToGitRepo(newGitRepo, authorInfo.getAuthor(), tag, authorInfo.getEmail(), commitMessage);
        return scmUrl;
    }

    private String extractPbaFromZip(final Path publishingTemplatePath) {
        try (final FileSystem zipfs = FileSystems.newFileSystem(publishingTemplatePath, this.getClass().getClassLoader())) {
            for (final Path root : zipfs.getRootDirectories()) {
                final Optional<Path> possiblePbaPath = Files.walk(root)
                        .filter(path -> (path.getFileName() != null && PBA_JSON.equalsIgnoreCase(path.getFileName().toString()))).findFirst();

                if (possiblePbaPath.isPresent()) {
                    try (InputStream pbaInputStream = Files.newInputStream(possiblePbaPath.get())) {
                        return IOUtils.toString(pbaInputStream, "UTF-8");
                    }
                }
            }
        } catch (final IOException ex) {
            throw new TemplateManagerException(TemplateManagerExceptionCodes.PBA_IS_CORRUPTED,
                    "Exception occurred, when trying to extract data from application being published", ex);
        }
        throw new TemplateManagerException(TemplateManagerExceptionCodes.PBA_NOT_FOUND, "Unable to locate PBA within zip file");
    }

    private Path copyUploadedFileToServer(final InputStream input, final String fileName) {
        final Path publishingTemplatePath = Paths.get(TemplateManagerConfiguration.localRepoPath).resolve(fileName);
        try {
            if (publishingTemplatePath.toFile().exists()) {
                publishingTemplatePath.toFile().delete();
            }
            Files.copy(input, publishingTemplatePath);
            input.close();
        } catch (final IOException exp) {
            throw new TemplateManagerException(TemplateManagerExceptionCodes.ERROR_COPYING_UPLOADED_FILE_TO_SERVER,
                    String.format("Publish operation failed for when copying uploaded file to server"), exp);
        }
        return publishingTemplatePath;
    }

    private void updateScmInfoInProject(final Path publishingApplicationPath, final String pbaAsString) {
        try (final FileSystem zipfs = FileSystems.newFileSystem(publishingApplicationPath, this.getClass().getClassLoader())) {
            for (final Path root : zipfs.getRootDirectories()) {
                final Optional<Path> possiblePbaPath = findPba(root);

                if (possiblePbaPath.isPresent()) {
                    Files.copy(IOUtils.toInputStream(pbaAsString, UTF_8_ENCODING), possiblePbaPath.get(), StandardCopyOption.REPLACE_EXISTING);
                    return;
                }
            }
        } catch (final IOException exp) {
            throw new TemplateManagerException(TemplateManagerExceptionCodes.ERROR_COPYING_UPLOADED_FILE_TO_SERVER,
                    "Exception occurred, when trying to extract data from application being published", exp);
        }
        throw new TemplateManagerException(TemplateManagerExceptionCodes.PBA_NOT_FOUND, "Unable to locate PBA within zip file");
    }

    private Optional<Path> findPba(final Path root) throws IOException {
        try (final Stream<Path> possiblePbaPath = Files.walk(root)) {
            return possiblePbaPath.filter(path -> (path.getFileName() != null && PBA_JSON.equalsIgnoreCase(path.getFileName().toString())))
                    .findFirst();
        }
    }

    private void removeOldZipFile(final Path newGitRepo) throws IOException {
        try (Stream<Path> paths = Files.find(newGitRepo, 1, (path, attrs) -> attrs.isRegularFile() && path.toString().endsWith(ZIP))) {
            paths.forEach((path) -> {
                try {
                    FileUtils.forceDelete(path.toFile());
                } catch (final IOException exp) {
                    LOGGER.error("Failed to remove old application zip file from repository", exp);
                }
            });
        }
    }
}
