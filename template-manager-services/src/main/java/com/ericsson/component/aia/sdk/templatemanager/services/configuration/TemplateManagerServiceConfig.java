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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.ericsson.component.aia.sdk.templatemanager.impl.TemplateManagerConfiguration;

/**
 * Template manager service configuration
 */
@SuppressWarnings("PMD.TooManyFields")
@Component
public class TemplateManagerServiceConfig {

    private static final Logger LOGGER = LoggerFactory.getLogger(TemplateManagerServiceConfig.class);

    @Value("${template.manager.local.repo.path}")
    private String localRepoPath;

    @Value("${template.catalog.name}")
    private String templateCatalogName;

    @Value("${git.service.type}")
    private String gitServiceType;

    @Value("${git.service.url}")
    private String gitServiceUrl;

    @Value("${git.service.ssl.url}")
    private String gitServiceSslUrl;

    @Value("${git.service.access.token}")
    private String gitAccessToken;

    // Blank templates info
    @Value("${git.service.template.repo}")
    private String blankTemplateRepo;

    @Value("${git.service.template.names}")
    private String serviceTemplateNames;

    @Value("${template.download.cache.expiry}")
    private int downloadCacheExpiry; // Unit: Minutes

    @Value("${application.catalog.name}")
    private String applicationCatalogName;

    @Value("${service.catalog.name}")
    private String serviceCatalogName;

    @Value("${docker.client.username}")
    private String dockerClientUsername;

    @Value("${docker.client.password}")
    private String dockerClientPassword;

    @Value("${docker.repo.application.path}")
    private String dockerRepoBasePath;

    @Value("${docker.repo.server.url}")
    private String dockerRepoServerUrl;

    @Value("${artifactory.server.url}")
    private String artifactoryServerUrl;

    @Value("${artifactory.server.path}")
    private String artifactoryServerPath;

    /**
     * Print the parameters being used to start the spring application
     */
    public void printConfiguration() {
        LOGGER.info("templateCatalogName::{}", templateCatalogName);
        LOGGER.info("applicationCatalogName::{}", applicationCatalogName);
        LOGGER.info("serviceCatalogName::{}", serviceCatalogName);
        LOGGER.info("serviceTemplateNames::{}", serviceTemplateNames);

        LOGGER.info("localRepoPath::{}", localRepoPath);

        LOGGER.info("gitServiceType::{}", gitServiceType);
        LOGGER.info("gitServiceUrl::{}", gitServiceUrl);
        LOGGER.info("gitAccessToken::{}", gitAccessToken);

        LOGGER.info("downloadCacheExpiry::{} ", downloadCacheExpiry);

        LOGGER.info("dockerClientUsername::{}", dockerClientUsername);
        LOGGER.info("dockerClientPassword::{}", dockerClientPassword);
        LOGGER.info("dockerRepoBasePath::{}", dockerRepoBasePath);
        LOGGER.info("dockerRepoServerUrl::{} ", dockerRepoServerUrl);

        LOGGER.info("artifactoryServerUrl::{} ", artifactoryServerUrl);
        LOGGER.info("artifactoryServerPath::{}", artifactoryServerPath);
    }

    /**
     * This method will update the TemplateManagerConfiguration to include all template properties.
     */
    public void updateTemplateManagerConfiguration() {
        TemplateManagerConfiguration.localRepoPath = localRepoPath;
        TemplateManagerConfiguration.templateCatalogName = templateCatalogName;

        TemplateManagerConfiguration.gitServiceType = gitServiceType;
        TemplateManagerConfiguration.gitServiceUrl = gitServiceUrl;
        TemplateManagerConfiguration.gitAccessToken = gitAccessToken;

        TemplateManagerConfiguration.blankTemplateRepo = blankTemplateRepo;
        TemplateManagerConfiguration.updateServiceTemplateNames(serviceTemplateNames);
    }

    public String getLocalRepoPath() {
        return localRepoPath;
    }

    public String getTemplateCatalogName() {
        return templateCatalogName;
    }

    public String getGitServiceType() {
        return gitServiceType;
    }

    public String getGitServiceUrl() {
        return gitServiceUrl;
    }

    public String getGitServiceSslUrl() {
        return gitServiceSslUrl;
    }

    public String getGitAccessToken() {
        return gitAccessToken;
    }

    public int getDownloadCacheExpiry() {
        return downloadCacheExpiry;
    }

    public String getApplicationCatalogName() {
        return applicationCatalogName;
    }

    public String getServiceCatalogName() {
        return serviceCatalogName;
    }

    public String getArtifactoryServerUrl() {
        return artifactoryServerUrl;
    }

    public String getArtifactoryServerPath() {
        return artifactoryServerPath;
    }

    public String getDockerClientUsername() {
        return dockerClientUsername;
    }

    public String getDockerClientPassword() {
        return dockerClientPassword;
    }

    public String getDockerRepoBasePath() {
        return dockerRepoBasePath;
    }

    public String getDockerRepoServerUrl() {
        return dockerRepoServerUrl;
    }
}
