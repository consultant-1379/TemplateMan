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

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class models configuration required for {@link TemplateManagerImpl}
 *
 */
public class TemplateManagerConfiguration {

    public static String localRepoPath;

    public static String templateCatalogName;

    public static String gitServiceType;
    public static String gitServiceUrl;
    public static String gitAccessToken;

    public static String blankTemplateRepo;

    public static Map<String, String> serviceTemplateZipName = new HashMap<>();

    private static final Logger LOGGER = LoggerFactory.getLogger(TemplateManagerConfiguration.class);

    private TemplateManagerConfiguration() {

    }

    /**
     * @param serviceTemplateNames
     *            - the list of templates separated by comma.
     */
    public static void updateServiceTemplateNames(final String serviceTemplateNames) {
        if (serviceTemplateNames == null) {
            return;
        }
        LOGGER.trace("Replacing service template names for {}", serviceTemplateNames);
        serviceTemplateZipName.clear();
        for (final String serviceTemplateName : serviceTemplateNames.split(",")) {
            final String[] templateServiceInfo = serviceTemplateName.trim().split("\\s+");
            if (templateServiceInfo.length == 2) {
                serviceTemplateZipName.put(templateServiceInfo[0].trim().toUpperCase(), templateServiceInfo[1].trim());
            }
        }
    }

    /**
     * Gets the template zip name.
     *
     * @param technology
     *            the technology
     * @return the template zip name
     */
    public static String getTemplateZipName(final String technology) {
        if (StringUtils.isEmpty(technology)) {
            return null;
        }
        return serviceTemplateZipName.get(technology.toUpperCase());
    }

}