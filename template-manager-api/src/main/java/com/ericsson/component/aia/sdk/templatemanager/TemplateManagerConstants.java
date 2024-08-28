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

/**
 * This interface contains common constants being used by template manager
 */
public interface TemplateManagerConstants {

    String UTF_8_ENCODING = "UTF-8";
    String PBA_TEMPLATE_INFO_ID = "pba.templateInfo.id";
    String PBA_TEMPLATE_NAME = "pba.templateInfo.name";

    String HYPHEN = "-";
    String DEFAULT_STARTING_VERSION = "0.0.0";

    String PBA_JSON = "pba.json";
    String ZIP_FILE_EXTENSION = ".zip";
    String JAVA_FILE_EXTENSION = ".java";

    /** The pba application info id. */
    String PBA_APPLICATION_INFO_ID = "pba.applicationInfo.id";

    /** The sdk published application cache folder. */
    String SDK_PUBLISHED_APPLICATION_CACHE_FOLDER = "sdk-published-application-cache-folder";

    /** The sdk application cache folder. */
    String SDK_APPLICATION_CACHE_FOLDER = "sdk-application-cache-folder";

    /** The sdk template cache folder. */
    String SDK_TEMPLATE_CACHE_FOLDER = "sdk-template-cache-folder";

    /** The flow xml. */
    String FLOW_XML = "flow.xml";

    String FORWARD_SLASH = "/";
    String COLON = ":";

    String ACTIVE_STATUS = "ACTIVE";
    String INACTIVE_STATUS = "INACTIVE";
    String DOCKER_IMAGE_TAR = "DockerImage.tar";

    String APPLICATION_PBA_NAME = "pba.applicationInfo.name";

    /** Default SDK delimiter. eg AppName-version */
    String SDK_DELIMITER = "-";

    /** Constant string for root path. */
    String ROOT_PATH = "/";

    /** Constant string for placeholder in template. */
    String PBA_NAME_CAMELCASE = "pbaNameCamelCase";

    /** Constant string for placeholder in template. */
    String PBA_NAME_CAMELCASE_WITH_JAVA_EXT = PBA_NAME_CAMELCASE + ZIP_FILE_EXTENSION;

    /** Constant string for placeholder in template. */
    String PBA_NAME = "pbaName";

    /** Constant string for placeholder in template. */
    String PBA_CLASS_NAME = "PbaName";

    /** Constant string for placeholder in template. */
    String PBA_VERSION = "pbaVersion";

    /** Constant string for placeholder in template. */
    String PBA_DESCRIPTION = "pbaDescription";

    /** Constant string for Hypen */
    char CHAR_HYPHEN = '-';

    /** Constant string for regex to replace special characters. */
    String REGEX_TO_REPLACE_SPECIAL_CHARACTERS = "[^\\w\\s]";

}
