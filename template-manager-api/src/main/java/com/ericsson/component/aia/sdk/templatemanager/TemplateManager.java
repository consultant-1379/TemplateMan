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
package com.ericsson.component.aia.sdk.templatemanager;

import java.io.InputStream;
import java.nio.file.Path;
import java.util.Collection;

import com.ericsson.component.aia.sdk.pba.model.PBAInstance;
import com.ericsson.component.aia.sdk.pba.model.PbaInfo;
import com.ericsson.component.aia.sdk.templatemanager.views.TemplateVersionView;

/**
 * <p>
 * Template manager interface with supported operations.
 * </p>
 *
 * @author echchik
 */
public interface TemplateManager {

    /**
     * <p>
     * This method will publish new template into the template catalog.
     * </p>
     *
     * @param inputStream
     *            <p>
     *            Input stream of the new template ZIP file.
     *            </p>
     * @param fileName
     *            <p>
     *            The name of the template archive.
     *            </p>
     * @param version
     *            <p>
     *            the version of the template being published.
     *            </p>
     * @return
     *         <p>
     *         true if operation is successful else false
     *         </p>
     */
    boolean publishTemplate(InputStream inputStream, String fileName, String version);

    /**
     * <p>
     * This method will unpublish template from the template catalog.
     * </p>
     *
     * @param pbaId
     *            <p>
     *            of the template ID to be unpublished
     *            </p>
     * @return
     *         <p>
     *         true if operation is successful else false
     *         </p>
     */
    boolean unPublishTemplate(String pbaId);

    /**
     * <p>
     * This method will update an existing published template in the template catalog.
     * </p>
     *
     * @param pbaInstance
     *            <p>
     *            of the extended template containing metadata.
     *            </p>
     * @return
     *         <p>
     *         Path of extended template.
     *         </p>
     */
    Path extendTemplate(PBAInstance pbaInstance);

    /**
     * <p>
     * This method will return {@link PBAInstance}for requested template name and version.
     * </p>
     *
     * @param pbaId
     *            <p>
     *            of the template ID to fetch from template catalog
     *            </p>
     * @return
     *         <p>
     *         {@link PBAInstance} of the template published in the template catalog
     *         </p>
     */
    PBAInstance getPbaInstance(String pbaId);

    /**
     * <p>
     * This method will return collection of {@link TemplateInfo} of published templates.
     * </p>
     *
     * @return
     *         <p>
     *         This method will return collection of {@link TemplateInfo} published in template catalog
     *         </p>
     */
    Collection<PbaInfo> listTemplates();

    /**
     * <p>
     * This method will download template from the template repository.
     * </p>
     *
     * @param pbaId
     *            <p>
     *            of the template ID to be downloaded.
     *            </p>
     * @return
     *         <p>
     *         The path of the template archive to download.
     *         </p>
     */
    Path downloadTemplate(String pbaId);

    /**
     * <p>
     * This method will download the template from the Git repository and update with the contents of the PBA string.
     * </p>
     *
     * @param pbaString
     *            <p>
     *            The PBA of the new template
     *            </p>
     * @return
     *         <p>
     *         the path to the new template archive
     *         </p>
     */
    Path createTemplate(String pbaString);

    /**
     * <p>
     * This method will remove a template fully. It is only for uses in development and testing. It is not intended for use by clients of the App SDK.
     * It performs the following cleanup duties:: - Removes the git repo - Removes entries in the Metastore relating to this template - Deletes the
     * Docker image associated with this template
     * </p>
     *
     * @param pbaId
     *            <p>
     *            of the template ID to be delete
     *            </p>
     * @return
     *         <p>
     *         status of operation
     *         </p>
     */
    CompletionStatus cleanupTemplate(String pbaId);

    /**
     * Gets the version.
     *
     * @param templateName
     *            the template name
     * @return the version info {@link TemplateVersionView}
     */
    TemplateVersionView getVersion(String templateName);
}