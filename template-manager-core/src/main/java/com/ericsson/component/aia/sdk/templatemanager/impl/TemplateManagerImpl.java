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
package com.ericsson.component.aia.sdk.templatemanager.impl;

import static com.ericsson.component.aia.sdk.templatemanager.TemplateManagerConstants.PBA_TEMPLATE_NAME;
import static com.ericsson.component.aia.sdk.templatemanager.TemplateManagerConstants.UTF_8_ENCODING;
import static com.ericsson.component.aia.sdk.templatemanager.TemplateManagerConstants.ZIP_FILE_EXTENSION;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.aia.metadata.api.MetaDataServiceIfc;
import com.ericsson.aia.metadata.exception.MetaDataServiceException;
import com.ericsson.aia.metadata.model.MetaData;
import com.ericsson.component.aia.sdk.git.exceptions.SdkGitException;
import com.ericsson.component.aia.sdk.git.project.service.GitProjectService;
import com.ericsson.component.aia.sdk.git.repo.service.GitSshService;
import com.ericsson.component.aia.sdk.pba.model.PBAInstance;
import com.ericsson.component.aia.sdk.pba.model.Pba;
import com.ericsson.component.aia.sdk.pba.model.PbaInfo;
import com.ericsson.component.aia.sdk.pba.tools.PBASchemaTool;
import com.ericsson.component.aia.sdk.templatemanager.CompletionStatus;
import com.ericsson.component.aia.sdk.templatemanager.TemplateManager;
import com.ericsson.component.aia.sdk.templatemanager.TemplateManagerConstants;
import com.ericsson.component.aia.sdk.templatemanager.exception.TemplateManagerException;
import com.ericsson.component.aia.sdk.templatemanager.exception.code.TemplateManagerExceptionCodes;
import com.ericsson.component.aia.sdk.templatemanager.generator.TemplateExtender;
import com.ericsson.component.aia.sdk.templatemanager.publisher.TemplatePublisher;
import com.ericsson.component.aia.sdk.templatemanager.util.TemplateManagerUtil;
import com.ericsson.component.aia.sdk.templatemanager.views.TemplateVersionView;
import com.ericsson.component.aia.sdk.util.docker.dependency.DependencyChecker;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

/**
 * This class provides implementations of {@link TemplateManager} operations.
 *
 * @author echchik
 *
 */
public class TemplateManagerImpl implements TemplateManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(TemplateManagerImpl.class);

    private final MetaDataServiceIfc metaDataService;
    private final PBASchemaTool pbaSchemaTool;

    private final GitProjectService gitProjectRepository;
    private final GitSshService gitRepository;

    private final GitOperation gitOperation;

    private final TemplatePublisher templatePublisher;
    private final TemplateExtender templateExtender;
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private final JsonParser jsonParser = new JsonParser();

    /**
     * Instantiates a new template manager impl.
     *
     * @param metaDataService
     *            the meta data service
     * @param pbaSchemaTool
     *            the pba schema tool
     * @param gitProjectRepository
     *            the git project repository
     * @param gitRepository
     *            the git repository
     * @param dependencyChecker
     *            the dependency checker
     */
    public TemplateManagerImpl(final MetaDataServiceIfc metaDataService, final PBASchemaTool pbaSchemaTool,
                               final GitProjectService gitProjectRepository, final GitSshService gitRepository,
                               final DependencyChecker dependencyChecker) {
        this.metaDataService = metaDataService;
        this.pbaSchemaTool = pbaSchemaTool;
        this.gitProjectRepository = gitProjectRepository;
        this.gitRepository = gitRepository;
        this.gitOperation = new GitOperation(metaDataService, pbaSchemaTool, gitProjectRepository);
        this.templatePublisher = new TemplatePublisher(metaDataService, pbaSchemaTool, gitRepository, gitOperation, dependencyChecker);
        this.templateExtender = new TemplateExtender(gitRepository, pbaSchemaTool, metaDataService);

    }

    @Override
    public boolean publishTemplate(final InputStream input, final String fileName, final String version) {
        return templatePublisher.publishTemplate(input, fileName, version);
    }

    @Override
    public Path createTemplate(final String pbaString) {
        final PBAInstance pbaModel = pbaSchemaTool.getPBAModelInstance(pbaString);

        if (pbaModel == null || pbaModel.getPba() == null) {
            throw new TemplateManagerException(TemplateManagerExceptionCodes.PBA_IS_INVALID, "PBAInstance is null or invalid ");
        }

        final PbaInfo templatePbaInfo = pbaModel.getPba().getTemplateInfo();
        if (templatePbaInfo == null) {
            throw new TemplateManagerException(TemplateManagerExceptionCodes.PBA_IS_INVALID, "Pba template info is null");
        }
        final String name = generateTemplateName(templatePbaInfo.getTitle());
        final String version = templatePbaInfo.getVersion();

        pbaModel.getPba().getTemplateInfo().setName(name);

        final JsonElement jsonElement = jsonParser.parse(pbaSchemaTool.convertToJsonString(pbaModel));
        final String prettyJsonString = gson.toJson(jsonElement);

        LOGGER.trace("Create template method invoked with name {} and version {}", name, version);

        try {

            final String templateZipName = TemplateManagerConfiguration.getTemplateZipName(pbaModel.getPba().getProcessorInfo().getTechnology());

            final Path newGitRepo = gitRepository.clone(TemplateManagerConfiguration.blankTemplateRepo, name);
            final Path blankTemplateZip = newGitRepo.resolve(templateZipName);

            try (FileSystem zipfs = FileSystems.newFileSystem(blankTemplateZip, this.getClass().getClassLoader())) {
                final Path pathInZipfile = zipfs.getPath(TemplateManagerConstants.PBA_JSON);
                Files.copy(IOUtils.toInputStream(prettyJsonString, UTF_8_ENCODING), pathInZipfile, StandardCopyOption.REPLACE_EXISTING);
            }

            final Path publishingTemplatePath = newGitRepo.resolve(TemplateManagerUtil.getZipFileName(name, version));
            Files.move(blankTemplateZip, publishingTemplatePath, StandardCopyOption.REPLACE_EXISTING);

            return publishingTemplatePath;

        } catch (final IOException exp) {
            throw new TemplateManagerException(TemplateManagerExceptionCodes.ERROR_CREATE_TEMPLATE,
                    String.format("Create operation failed for template with name %s and version %s ", name, version), exp);
        }
    }

    @Override
    public boolean unPublishTemplate(final String pbaId) {
        LOGGER.trace("Un-Publishing template method invoked  with ID {}", pbaId);
        try {
            metaDataService.delete(TemplateManagerConfiguration.templateCatalogName, pbaId);
            return true;
        } catch (final MetaDataServiceException exp) {
            throw new TemplateManagerException(TemplateManagerExceptionCodes.ERROR_INVOKING_METADATASERVICE_ON_UNPUBLISHED,
                    String.format("Un-Publish operation failed for template with ID %s ", pbaId), exp);
        }
    }

    @Override
    public Path extendTemplate(final PBAInstance pbaInstance) {
        return templateExtender.extendTemplate(pbaInstance);
    }

    @Override
    public PBAInstance getPbaInstance(final String pbaId) {
        LOGGER.trace("GET template Pba method invoked with ID {} ", pbaId);
        try {
            final String pbaAsString = metaDataService.get(TemplateManagerConfiguration.templateCatalogName, pbaId);
            return pbaSchemaTool.getPBAModelInstance(pbaAsString);
        } catch (final MetaDataServiceException exp) {
            throw new TemplateManagerException(TemplateManagerExceptionCodes.ERROR_INVOKING_METADATASERVICE_ON_GET,
                    String.format("Get operation failed for template with ID %s", pbaId), exp);
        }
    }

    @Override
    public Collection<PbaInfo> listTemplates() {
        LOGGER.trace("List templates method invoked ");
        final Set<PbaInfo> templateInfos = new HashSet<>();
        try {
            final ArrayList<MetaData> listOfMetaData = metaDataService.find(TemplateManagerConfiguration.templateCatalogName, "*");
            for (final MetaData metaData : listOfMetaData) {
                if (metaData.getValue() == null || metaData.getValue().trim().isEmpty()) {
                    continue;
                }
                templateInfos.add(getTemplateInfo(metaData));
            }
            return templateInfos;
        } catch (final MetaDataServiceException exp) {
            throw new TemplateManagerException(TemplateManagerExceptionCodes.ERROR_INVOKING_METADATASERVICE_ON_LIST,
                    "List operation failed for templates", exp);
        }
    }

    @Override
    public Path downloadTemplate(final String pbaId) {
        LOGGER.info("Download template method invoked  with ID {} ", pbaId);
        final PBAInstance templatePbaInstance = getPbaInstance(pbaId);
        final Pba pba = templatePbaInstance.getPba();
        final PbaInfo templateInfo = pba.getTemplateInfo();

        try {
            LOGGER.info("Cloning git uri:{}, directory name:{}", pba.getScmInfo().getScm(), templateInfo.getName());
            final Path templateGitRepo = gitRepository.clone(pba.getScmInfo().getScm(), templateInfo.getName());
            final String tag = pba.getScmInfo().getScmTag();
            LOGGER.info("Performing git checkout using tag:{}", tag);
            gitRepository.checkout(templateGitRepo, tag);
            final Path template = TemplateManagerUtil.findFileTypeInPath(templateGitRepo, ".zip").get();
            return TemplateManagerUtil.renameZipFile(template, getZipFileName(templateInfo.getName(), templateInfo.getVersion()));

        } catch (final SdkGitException | IOException exp) {
            throw new TemplateManagerException(TemplateManagerExceptionCodes.ERROR_ACCESSING_GIT_REPOSITORY, "Unable to download template from GIT",
                    exp);
        }
    }

    @Override
    public TemplateVersionView getVersion(final String templateName) {
        try {
            final TemplateVersionView templateVersionView = new TemplateVersionView();
            final List<MetaData> templateMetaDatas = metaDataService.findByPropertyValue(TemplateManagerConfiguration.templateCatalogName,
                    PBA_TEMPLATE_NAME, templateName);

            final List<String> versions = new ArrayList<>();
            for (final MetaData templateMetaData : templateMetaDatas) {
                versions.add(getTemplateVersionFromMetaData(templateMetaData));
            }

            Collections.sort(versions, Collections.reverseOrder());
            if (!versions.isEmpty()) {
                templateVersionView.setMaxVersion(versions.get(0));
                templateVersionView.setVersions(versions);
                templateVersionView.setNewTemplate(false);
            }

            return templateVersionView;

        } catch (final MetaDataServiceException e) {
            throw new TemplateManagerException(TemplateManagerExceptionCodes.ERROR_INVOKING_METADATASERVICE_ON_GET, e);
        }
    }

    @Override
    public CompletionStatus cleanupTemplate(final String pbaId) {
        LOGGER.info("Cleanup template method invoked with ID {}", pbaId);
        final Pba pba = getPbaInstance(pbaId).getPba();
        final String scm = pba.getScmInfo().getScm();
        final String repoName = scm.substring(scm.lastIndexOf("/") + 1).replace(".git", "");

        final String repoOwner = "root"; // TODO Sort this out in next refactor iteration.
        boolean gitResult = false;
        boolean metastoreResult = false;

        // Delete git repository for this template.
        try {
            LOGGER.info("Deleting the Git repository:{} for owner:{}", repoName, repoOwner);
            gitResult = gitProjectRepository.deleteGitRepository(repoName);
            LOGGER.info("Delete of Git repository:{} was successful.", repoName);
        } catch (final SdkGitException exp) {
            LOGGER.warn(String.format("Unable to delete git repo for template with ID %s ", exp));
        }

        // Cleanup entries in the metastore for this  template
        try {
            metaDataService.delete(TemplateManagerConfiguration.templateCatalogName, pbaId);
            LOGGER.info("Delete of metastore entries for pbaId:{} was successful.", pbaId);
            metastoreResult = true;
        } catch (final MetaDataServiceException exp) {
            LOGGER.warn(String.format("Unable to delete metastore entry for template with ID %s ", pbaId), exp);
        }

        if (metastoreResult && gitResult) {
            return CompletionStatus.SUCCESS;
        } else if (!metastoreResult && !gitResult) {
            return CompletionStatus.FAILED;
        } else {
            return CompletionStatus.PARTIAL_SUCCESS;
        }
    }

    private String getTemplateVersionFromMetaData(final MetaData metaData) {
        final PbaInfo pbaInfo = getTemplateInfo(metaData);
        return pbaInfo.getVersion();
    }

    private PbaInfo getTemplateInfo(final MetaData metaData) {
        final PBAInstance pbaInstance = pbaSchemaTool.getPBAModelInstance(metaData.getValue());
        return pbaInstance.getPba().getTemplateInfo();
    }

    private String getZipFileName(final String name, final String version) {
        return new StringBuilder(name).append("-").append(version).append(".").append(ZIP_FILE_EXTENSION).toString();
    }

    private String generateTemplateName(final String templateTitle) {
        return templateTitle.toLowerCase().replaceAll("\\s+", "-"); // Replace whitespace
    }

}
