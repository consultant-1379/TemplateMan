/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2016
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/
package com.ericsson.component.aia.sdk.templatemanager.impl;

import static com.ericsson.component.aia.sdk.templatemanager.impl.TemplateManagerConfiguration.templateCatalogName;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.aia.metadata.api.MetaDataServiceIfc;
import com.ericsson.aia.metadata.exception.MetaDataServiceException;
import com.ericsson.component.aia.sdk.git.project.service.GitProjectService;
import com.ericsson.component.aia.sdk.git.project.service.GitRepoInfo;
import com.ericsson.component.aia.sdk.pba.model.Pba;
import com.ericsson.component.aia.sdk.pba.tools.PBASchemaTool;
import com.ericsson.component.aia.sdk.templatemanager.exception.TemplateManagerException;
import com.ericsson.component.aia.sdk.templatemanager.exception.code.TemplateManagerExceptionCodes;

/**
 * This class is used to create Git repository in the appropriate state to push a new Template
 *
 */
public class GitOperation {

    private static final Logger LOGGER = LoggerFactory.getLogger(GitOperation.class);

    private final MetaDataServiceIfc metaDataServiceManager;
    private final PBASchemaTool pbaSchemaTool;
    private final GitProjectService gitProjectService;

    /**
     * Instantiates a new git operation.
     *
     * @param metaDataServiceManager
     *            the meta data service manager
     * @param pbaSchemaTool
     *            the pba schema tool
     * @param gitProjectService
     *            the git project service
     */
    public GitOperation(final MetaDataServiceIfc metaDataServiceManager, final PBASchemaTool pbaSchemaTool,
                        final GitProjectService gitProjectService) {
        this.metaDataServiceManager = metaDataServiceManager;
        this.pbaSchemaTool = pbaSchemaTool;
        this.gitProjectService = gitProjectService;
    }

    /**
     * Get the SSH URI of the templates Git repository.
     *
     * @param pba
     *            the template PBA
     * @return the Git repository url
     */
    public String getGitRepoUrl(final Pba pba) {
        final Optional<String> parentScmUrl = getParentIfAvailable(pba);
        if (parentScmUrl.isPresent()) {
            return parentScmUrl.get();
        }
        return createNewGitRepoIfRequired(pba);
    }

    private Optional<String> getParentIfAvailable(final Pba pba) {
        final String scmUrl;

        final Optional<String> parentId = Optional.ofNullable(pba.getTemplateInfo().getParentId());
        if (parentId.isPresent()) {
            final GitRepoInfo parentGitRepo = getParentRepo(parentId.get());
            scmUrl = parentGitRepo.getSshRepoUrl();
            pba.getScmInfo().setScm(scmUrl);
            return Optional.of(scmUrl);
        }

        return Optional.empty();
    }

    private String createNewGitRepoIfRequired(final Pba pba) {
        final String scmUrl;

        final String templateName = pba.getTemplateInfo().getName();
        final Optional<GitRepoInfo> existingScmUrl = gitProjectService.getExistingGitRepository(templateName);

        if (existingScmUrl.isPresent()) {
            LOGGER.info("Template git repository already exists for template named:: {}", templateName);
            scmUrl = existingScmUrl.get().getSshRepoUrl();

        } else {
            LOGGER.info("Template git repository doesn't exist for template named:: {}, creating new git repository", templateName);
            scmUrl = gitProjectService.createGitRepository(templateName, pba.getTemplateInfo().getDescription()).getSshRepoUrl();
        }

        pba.getScmInfo().setScm(scmUrl);
        return scmUrl;
    }

    /**
     * This method will recursively search through a templates parents until it finds the root template (template without a parentId) it will then
     * return that templates Git repository.
     *
     * @param templatesId
     *            The Id of the templates to finds parent.
     * @return {@link GitRepoInfo} the Git repository info for the root application.
     */
    private GitRepoInfo getParentRepo(final String templateId) {
        final Pba pba;

        try {
            pba = pbaSchemaTool.getPBAModelInstance(metaDataServiceManager.get(templateCatalogName, templateId)).getPba();
            final Optional<String> parentId = Optional.ofNullable(pba.getTemplateInfo().getParentId());
            if (parentId.isPresent()) {
                return getParentRepo(parentId.get());
            }
        } catch (final MetaDataServiceException e) {
            throw new TemplateManagerException(TemplateManagerExceptionCodes.PARENT_TEMPLATE_NOT_FOUND, "Cannot find parent template", e);
        }

        final Optional<GitRepoInfo> existingScmUrl = gitProjectService.getExistingGitRepository(pba.getTemplateInfo().getName());
        if (!existingScmUrl.isPresent()) {
            throw new TemplateManagerException(TemplateManagerExceptionCodes.ERROR_ACCESSING_GIT_REPOSITORY, "Unable to find parent Git repository");
        }

        return existingScmUrl.get();
    }

}
