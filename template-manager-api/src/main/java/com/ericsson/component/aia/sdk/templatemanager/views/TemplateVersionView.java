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
package com.ericsson.component.aia.sdk.templatemanager.views;

import java.util.ArrayList;
import java.util.List;

import com.ericsson.component.aia.sdk.templatemanager.TemplateManagerConstants;

/**
 * This class is used as a DTO for returning a list of the template versions
 */
public class TemplateVersionView {
    private boolean newTemplate = true;
    private String maxVersion = TemplateManagerConstants.DEFAULT_STARTING_VERSION;
    private List<String> versions = new ArrayList<>();

    /**
     * Gets the versions.
     *
     * @return the versions
     */
    public List<String> getVersions() {
        return versions;
    }

    /**
     * Adds the versions.
     *
     * @param versions
     *            the versions
     */
    public void setVersions(final List<String> versions) {
        this.versions = versions;
    }

    /**
     * Gets the max version.
     *
     * @return the max version
     */
    public String getMaxVersion() {
        return maxVersion;
    }

    /**
     * Sets the max version.
     *
     * @param maxVersion
     *            the new max version
     */
    public void setMaxVersion(final String maxVersion) {
        this.maxVersion = maxVersion;
    }

    /**
     * Checks if is new application.
     *
     * @return true, if is new application
     */
    public boolean isNewTemplate() {
        return newTemplate;
    }

    /**
     * Sets the new template.
     *
     * @param newTemplate
     *            the new new template
     */
    public void setNewTemplate(final boolean newTemplate) {
        this.newTemplate = newTemplate;
    }
}
