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
package com.ericsson.component.aia.sdk.templatemanager.services.configuration;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.mongo.MongoDataAutoConfiguration;
import org.springframework.boot.autoconfigure.data.mongo.MongoRepositoriesAutoConfiguration;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;
import org.springframework.boot.autoconfigure.mongo.embedded.EmbeddedMongoAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;

import com.ericsson.aia.metadata.api.MetaDataServiceIfc;
import com.ericsson.aia.metadata.exception.MetaDataServiceException;
import com.ericsson.aia.metadata.lifecycle.MetaDataServiceLifecycleManagerIfc;
import com.ericsson.aia.metadata.lifecycle.impl.MetaDataServiceLifecycleManagerImpl;
import com.ericsson.component.aia.sdk.git.project.service.GitProjectService;
import com.ericsson.component.aia.sdk.git.repo.service.GitSshService;
import com.ericsson.component.aia.sdk.pba.tools.PBASchemaTool;
import com.ericsson.component.aia.sdk.templatemanager.TemplateManager;
import com.ericsson.component.aia.sdk.templatemanager.cache.ArchivePathCache;
import com.ericsson.component.aia.sdk.templatemanager.cache.DownloadCache;
import com.ericsson.component.aia.sdk.templatemanager.impl.TemplateManagerBuilder;
import com.ericsson.component.aia.sdk.util.docker.SdkDockerService;
import com.ericsson.component.aia.sdk.util.docker.SdkDockerServiceBuilder;
import com.ericsson.component.aia.sdk.util.docker.dependency.DependencyChecker;
import com.ericsson.component.aia.sdk.util.docker.dependency.DependencyCheckerBuilder;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;

import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

/**
 * This will define beans needed for this application to run.
 *
 * @author echchik
 *
 */
@SpringBootApplication
@EnableAutoConfiguration(exclude = { MongoAutoConfiguration.class, MongoDataAutoConfiguration.class, EmbeddedMongoAutoConfiguration.class,
        MongoRepositoriesAutoConfiguration.class })
@EnableSwagger2
@ComponentScan("com.ericsson.component.aia.sdk.templatemanager.services")
public class TemplateManagerApplication {

    @Autowired
    private MetaStoreConfig metaStoreConfig;

    @Autowired
    private TemplateManagerServiceConfig templateManagerConfig;

    private TemplateManager templateManager;

    /**
     * This method creates {@link TemplateManager} bean.
     *
     * @return {@link TemplateManager} bean.
     * @throws MetaDataServiceException
     *             if {@link MetaDataServiceIfc} bean creation fails.
     * @throws IOException
     *             if {@link MetaDataServiceIfc} bean creation fails.
     */
    @Bean
    public TemplateManager templateManager() throws MetaDataServiceException, IOException {
        // Check if the TemplateManager has been created already, if not go and create it.
        templateManagerConfig.printConfiguration();
        templateManagerConfig.updateTemplateManagerConfiguration();

        if (this.templateManager == null) {
            this.templateManager = new TemplateManagerBuilder().metaDataService(metaDataServiceIfc()).pbaSchemaTool(pbaSchemaTool())
                    .gitRepository(new GitSshService()).gitRepository(gitProjectService()).dependencyChecker(dependencyChecker()).build();
            setupFileSystem();
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

    /**
     * This method creates {@link GitProjectService} bean.
     *
     * @return {@link GitProjectService} bean.
     */
    @Bean
    public GitProjectService gitProjectService() {
        return GitProjectService.newGitProjectRepository(templateManagerConfig.getGitServiceType(), templateManagerConfig.getGitAccessToken(),
                templateManagerConfig.getGitServiceUrl(), templateManagerConfig.getGitServiceSslUrl());
    }

    /**
     * This method creates {@link ArchivePathCache} bean.
     *
     * @return {@link ArchivePathCache} bean.
     */
    @Bean
    public ArchivePathCache ArchivePathCache() {
        return new ArchivePathCache();
    }

    /**
     * This method creates {@link DownloadCache} bean.
     *
     * @return {@link DownloadCache} bean.
     * @throws MetaDataServiceException
     *             if {@link TemplateManager} creation fails.
     * @throws IOException
     *             if {@link TemplateManager} creation fails.
     */
    @Bean
    public DownloadCache downloadCache() throws MetaDataServiceException, IOException {
        final TemplateManager templateManager = templateManager();
        return new DownloadCache(templateManager, this.templateManagerConfig.getDownloadCacheExpiry());
    }

    /**
     * This method creates {@link MetaDataServiceIfc} bean.
     *
     * @return {@link MetaDataServiceIfc} bean.
     * @throws MetaDataServiceException
     *             if {@link MetaDataServiceIfc} bean creation fails.
     * @throws IOException
     *             if {@link MetaDataServiceIfc} bean creation fails.
     */
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

    /**
     * Get a new SDK docker service used to work with docker images and interact with the Artifactory repository.
     *
     * @return the SDK docker service
     */

    public SdkDockerService sdkDockerService() {
        return new SdkDockerServiceBuilder().setArtifactoryServerUrl(templateManagerConfig.getArtifactoryServerUrl())
                .setArtifactoryServerPath(templateManagerConfig.getArtifactoryServerPath())
                .setDockerClientUsername(templateManagerConfig.getDockerClientUsername())
                .setDockerClientPassword(templateManagerConfig.getDockerClientPassword())
                .setDockerRepoBasePath(templateManagerConfig.getDockerRepoBasePath())
                .setDockerRepoServerUrl(templateManagerConfig.getDockerRepoServerUrl()).build();
    }

    /**
     * Get a new Dependency checker which is used to check if all of a PBAs dependencies exist if not an exception is thrown.
     *
     * @return the sdk docker service
     * @throws MetaDataServiceException
     *             if {@link MetaDataServiceIfc} bean creation fails.
     * @throws IOException
     *             if {@link MetaDataServiceIfc} bean creation fails.
     */

    public DependencyChecker dependencyChecker() throws MetaDataServiceException, IOException {
        return new DependencyCheckerBuilder().setServiceCatalogName(templateManagerConfig.getServiceCatalogName())
                .setApplicationCatalogName(templateManagerConfig.getApplicationCatalogName()).setMetaDataServiceManager(metaDataServiceIfc())
                .setPbaSchemaTool(pbaSchemaTool()).setSdkDockerService(sdkDockerService()).build();
    }

    /**
     * This method creates {@link PBASchemaTool} bean.
     *
     * @return {@link PBASchemaTool} bean.
     */
    @Bean
    public PBASchemaTool pbaSchemaTool() {
        return new PBASchemaTool();
    }

    /**
     * Swagger Configuration
     *
     * @return Docket
     */
    @Bean
    public Docket api() {
        return new Docket(DocumentationType.SWAGGER_2).apiInfo(apiInfo()).select().apis(RequestHandlerSelectors.any()).paths(paths()).build();
    }

    // Describe Template Mgr apis
    private ApiInfo apiInfo() {
        return new ApiInfoBuilder().title("Template Manager APIs").description("Lists APIs for Template Manager.").build();
    }

    // Only select apis that matches the given Predicates.
    private Predicate<String> paths() {
        // Match all paths except /error
        return Predicates.and(PathSelectors.regex("/.*"), Predicates.not(PathSelectors.regex("/error.*")));
    }

    /**
     * Starting point of the application
     *
     * @param args
     *            arguments for the application.
     */
    public static void main(final String[] args) {
        SpringApplication.run(TemplateManagerApplication.class, args);
    }
}
