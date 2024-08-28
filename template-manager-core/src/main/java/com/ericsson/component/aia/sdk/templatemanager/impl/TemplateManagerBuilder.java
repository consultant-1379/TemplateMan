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

import com.ericsson.aia.metadata.api.MetaDataServiceIfc;
import com.ericsson.component.aia.sdk.git.project.service.GitProjectService;
import com.ericsson.component.aia.sdk.git.repo.service.GitSshService;
import com.ericsson.component.aia.sdk.pba.tools.PBASchemaTool;
import com.ericsson.component.aia.sdk.templatemanager.TemplateManager;
import com.ericsson.component.aia.sdk.util.docker.dependency.DependencyChecker;

/**
 * This class is a builder for {@link TemplateManager}.
 *
 */
public class TemplateManagerBuilder {

    private MetaDataServiceIfc metaDataService;
    private PBASchemaTool pbaSchemaTool;
    private DependencyChecker dependencyChecker;

    private GitProjectService gitProjectRepository;
    private GitSshService gitRepository = new GitSshService();

    /**
     * Sets the dependency checker.
     *
     * @param dependencyChecker
     *            the dependency checker
     * @return the template manager builder
     */
    public TemplateManagerBuilder dependencyChecker(final DependencyChecker dependencyChecker) {
        this.dependencyChecker = dependencyChecker;
        return this;
    }

    /**
     * Meta data service.
     *
     * @param metaDataService
     *            the meta data service
     * @return the template manager builder
     */
    public TemplateManagerBuilder metaDataService(final MetaDataServiceIfc metaDataService) {
        this.metaDataService = metaDataService;
        return this;
    }

    /**
     * Pba schema tool.
     *
     * @param pbaSchemaTool
     *            the pba schema tool
     * @return the template manager builder
     */
    public TemplateManagerBuilder pbaSchemaTool(final PBASchemaTool pbaSchemaTool) {
        this.pbaSchemaTool = pbaSchemaTool;
        return this;
    }

    /**
     * Git repository.
     *
     * @param gitRepository
     *            the git repository
     * @return the template manager builder
     */
    public TemplateManagerBuilder gitRepository(final GitSshService gitRepository) {
        this.gitRepository = gitRepository;
        return this;
    }

    /**
     * Git repository.
     *
     * @param gitProjectRepository
     *            the git project repository
     * @return the template manager builder
     */
    public TemplateManagerBuilder gitRepository(final GitProjectService gitProjectRepository) {
        this.gitProjectRepository = gitProjectRepository;
        return this;
    }

    /**
     * Builds the template manager implementation.
     *
     * @return the template manager
     */
    public TemplateManager build() {
        return new TemplateManagerImpl(metaDataService, pbaSchemaTool, gitProjectRepository, gitRepository, dependencyChecker);
    }
}
