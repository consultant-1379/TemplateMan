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
package com.ericsson.component.aia.sdk.templatemanager.generator;

import java.io.IOException;
import java.nio.file.Path;

import com.ericsson.aia.metadata.api.MetaDataServiceIfc;
import com.ericsson.aia.metadata.exception.MetaDataServiceException;
import com.ericsson.component.aia.sdk.git.repo.service.GitSshService;
import com.ericsson.component.aia.sdk.pba.model.PBAInstance;
import com.ericsson.component.aia.sdk.pba.model.Pba;
import com.ericsson.component.aia.sdk.pba.model.PbaInfo;
import com.ericsson.component.aia.sdk.pba.tools.PBASchemaTool;
import com.ericsson.component.aia.sdk.templatemanager.exception.TemplateManagerException;
import com.ericsson.component.aia.sdk.templatemanager.exception.code.TemplateManagerExceptionCodes;
import com.ericsson.component.aia.sdk.templatemanager.impl.TemplateManagerConfiguration;
import com.ericsson.component.aia.sdk.templatemanager.util.TemplateManagerUtil;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

/**
 * The Class TemplateExtender is responsible for extending a template.
 */
public class TemplateExtender {

    private final GitSshService gitRepository;
    private final PBASchemaTool pbaSchemaTool;
    private final MetaDataServiceIfc metaDataService;
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private final JsonParser jsonParser = new JsonParser();

    /**
     * Instantiates a new template extender.
     *
     * @param gitRepository
     *            the Git repository
     * @param pbaSchemaTool
     *            the PBA schema tool
     * @param metaDataService
     *            the meta data service
     */
    public TemplateExtender(final GitSshService gitRepository, final PBASchemaTool pbaSchemaTool, final MetaDataServiceIfc metaDataService) {
        this.gitRepository = gitRepository;
        this.pbaSchemaTool = pbaSchemaTool;
        this.metaDataService = metaDataService;
    }

    /**
     * This method will update an existing published template in the template catalog.
     *
     * @param pbaInstance
     *            of the extended template containing metadata.
     * @return Path of the extended template
     */
    public Path extendTemplate(final PBAInstance pbaInstance) {
        final PbaInfo newTemplateInfo = pbaInstance.getPba().getTemplateInfo();
        final String pbaIdToExtend = newTemplateInfo.getId();
        final String newTemplateName = newTemplateInfo.getName();
        final String newTemplateVersion = newTemplateInfo.getVersion();

        pbaInstance.getPba().getTemplateInfo().setParentId(pbaIdToExtend);

        final JsonElement jsonElement = jsonParser.parse(pbaSchemaTool.convertToJsonString(pbaInstance));
        final String extendedTemplatePbaAsString = gson.toJson(jsonElement);

        final Pba existingTemplatePba = getPbaOfTemplateToBeExtended(pbaIdToExtend);
        final PbaInfo existingTemplateInfo = existingTemplatePba.getTemplateInfo();

        final Path templateRepo = gitRepository.clone(existingTemplatePba.getScmInfo().getScm(), existingTemplateInfo.getName());
        final Path zipFilePath = getZipFileOfTemplateToExtend(templateRepo);

        TemplateManagerUtil.updatePbaInZipFile(extendedTemplatePbaAsString, zipFilePath);

        return TemplateManagerUtil.renameZipFile(zipFilePath, TemplateManagerUtil.getZipFileName(newTemplateName, newTemplateVersion));
    }

    private Path getZipFileOfTemplateToExtend(final Path templateRepo) {
        try {
            return TemplateManagerUtil.findFileTypeInPath(templateRepo, ".zip").get();
        } catch (final IOException e) {
            throw new TemplateManagerException(TemplateManagerExceptionCodes.TEMPLATE_NOT_FOUND,
                    "Unable to locate template zip file within Git repository::" + templateRepo, e);
        }
    }

    private Pba getPbaOfTemplateToBeExtended(final String pbaId) {
        try {
            final String pbaAsString = metaDataService.get(TemplateManagerConfiguration.templateCatalogName, pbaId);
            return pbaSchemaTool.getPBAModelInstance(pbaAsString).getPba();
        } catch (final MetaDataServiceException exp) {
            throw new TemplateManagerException(TemplateManagerExceptionCodes.TEMPLATE_NOT_FOUND,
                    "Unable to find specified template to extend within meta store", exp);
        }
    }

}
