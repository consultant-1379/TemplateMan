/* *******************************************************************************
 * COPYRIGHT Ericsson 2017
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/
package com.ericsson.component.aia.sdk.templatemanager.services.configuration;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

import org.apache.commons.io.FileUtils;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.mongo.MongoDataAutoConfiguration;
import org.springframework.boot.autoconfigure.data.mongo.MongoRepositoriesAutoConfiguration;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;
import org.springframework.boot.autoconfigure.mongo.embedded.EmbeddedMongoAutoConfiguration;
import org.springframework.boot.autoconfigure.mongo.embedded.EmbeddedMongoProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;

import com.ericsson.aia.metadata.api.MetaDataServiceIfc;
import com.ericsson.aia.metadata.exception.MetaDataServiceException;
import com.ericsson.aia.metadata.lifecycle.MetaDataServiceLifecycleManagerIfc;
import com.ericsson.aia.metadata.lifecycle.impl.MetaDataServiceLifecycleManagerImpl;
import com.ericsson.component.aia.sdk.git.exceptions.SdkGitException;
import com.ericsson.component.aia.sdk.git.project.service.GitProjectService;
import com.ericsson.component.aia.sdk.git.project.service.GitRepoInfo;
import com.ericsson.component.aia.sdk.git.repo.service.GitSshService;
import com.ericsson.component.aia.sdk.pba.tools.PBASchemaTool;
import com.ericsson.component.aia.sdk.templatemanager.TemplateManager;
import com.ericsson.component.aia.sdk.templatemanager.cache.ArchivePathCache;
import com.ericsson.component.aia.sdk.templatemanager.cache.DownloadCache;
import com.ericsson.component.aia.sdk.templatemanager.impl.TemplateManagerBuilder;
import com.ericsson.component.aia.sdk.util.docker.dependency.DependencyChecker;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

/**
 * Application class to define test environment.
 *
 * @author echchik
 *
 */
@SpringBootApplication
@EnableAutoConfiguration(exclude = { MongoAutoConfiguration.class, MongoDataAutoConfiguration.class, EmbeddedMongoAutoConfiguration.class,
        MongoRepositoriesAutoConfiguration.class, EmbeddedMongoProperties.class })
@ComponentScan("com.ericsson.component.aia.sdk.templatemanager.services")
public class TemplateManagerTestApplication {

    private static final String TEST_TEMPLATE_ZIP = "src/test/resources/test-files/MockProjectGit";
    private static final String TEMPLATE_ZIP = "target/MockProjectGit";

    @Autowired
    private TemplateManagerServiceConfig templateManagerConfig;

    @Autowired
    private MetaStoreConfig metaStoreConfig;

    private TemplateManager templateManager;

    @Bean
    public TemplateManager templateManager() throws MetaDataServiceException, IOException {
        // Check if the TemplateManager has been created already, if not go and create it.
        if (this.templateManager == null) {
            templateManagerConfig.updateTemplateManagerConfiguration();

            final GitSshService gitSshService = new GitSshService() {
                @Override
                public void pushToGitRepo(final Path gitRepo, final String name, final String tag, final String email, final String message)
                        throws SdkGitException {
                }

                @Override
                public void checkoutBranch(final Path gitDir, final String name) throws SdkGitException {
                }

                @Override
                public boolean checkGitRepoExists(final String gitRepoUri) throws SdkGitException {
                    return true;
                }

                @Override
                public Path clone(final String gitRepoUri, final String dirName) throws SdkGitException {
                    try {
                        final File mockGitRepo = Paths.get(TEMPLATE_ZIP).toFile();
                        if (mockGitRepo.exists()) {
                            FileUtils.deleteDirectory(mockGitRepo);
                        }
                        FileUtils.copyDirectory(Paths.get(TEST_TEMPLATE_ZIP).toFile(), mockGitRepo);
                        return Paths.get(TEMPLATE_ZIP);

                    } catch (final IOException e) {
                        e.printStackTrace();
                        throw new SdkGitException(e);
                    }
                }

                @Override
                public void checkout(final Path gitDir, final String name) throws SdkGitException {
                };
            };

            final GitProjectService gitProjectService = new GitProjectService() {
                @Override
                public GitRepoInfo createGitRepository(final String repoName, final String description) {
                    return new GitRepoInfo("", Paths.get(TEST_TEMPLATE_ZIP).toAbsolutePath().toString(), "");
                }

                @Override
                public boolean deleteGitRepository(final String repoName) {
                    return true;
                }

                @Override
                public Optional<GitRepoInfo> getExistingGitRepository(final String repoName) {
                    return Optional.empty();
                }

                @Override
                public boolean deleteGitRepositoryTag(String repoName, String tag) {
                    // TODO Auto-generated method stub
                    return false;
                }
            };

            final TemplateManager templateManager = new TemplateManagerBuilder().metaDataService(metaDataServiceIfc()).pbaSchemaTool(pbaSchemaTool())
                    .gitRepository(gitSshService).gitRepository(gitProjectService).dependencyChecker(Mockito.mock(DependencyChecker.class)).build();

            setupFileSystem();
            this.templateManager = templateManager;
        }

        return this.templateManager;
    }

    private void setupFileSystem() {
        final File templateManagerRootDirectory = Paths.get(templateManagerConfig.getLocalRepoPath()).toFile();
        if (templateManagerRootDirectory.exists()) {
            templateManagerRootDirectory.delete();
        }
        templateManagerRootDirectory.mkdirs();
    }

    @Bean
    public ObjectMapper mapper() {
        final ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        return objectMapper;
    }

    @Bean
    public MetaDataServiceIfc metaDataServiceIfc() throws MetaDataServiceException, IOException {
        final MetaDataServiceLifecycleManagerIfc serviceLifecycleManager = new MetaDataServiceLifecycleManagerImpl();
        serviceLifecycleManager.provisionService(metaStoreConfig.getMetaStoreProperties());
        final MetaDataServiceIfc metaDataService = serviceLifecycleManager.getServiceReference();
        if (!metaDataService.schemaExists(templateManagerConfig.getTemplateCatalogName())) {
            metaDataService.createSchema(templateManagerConfig.getTemplateCatalogName());
        }
        return metaDataService;
    }

    @Bean
    public PBASchemaTool pbaSchemaTool() {
        return new PBASchemaTool();
    }

    @Bean
    public ArchivePathCache ArchivePathCache() {
        return new ArchivePathCache();
    }

    @Bean
    public DownloadCache downloadCache() throws MetaDataServiceException, IOException {
        final TemplateManager templateManager = templateManager();
        return new DownloadCache(templateManager, this.templateManagerConfig.getDownloadCacheExpiry());
    }
}
