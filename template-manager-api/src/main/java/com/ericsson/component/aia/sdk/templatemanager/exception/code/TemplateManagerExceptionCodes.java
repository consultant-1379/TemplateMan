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
package com.ericsson.component.aia.sdk.templatemanager.exception.code;

/**
 * @author ezsalro
 *
 */
public enum TemplateManagerExceptionCodes implements ExceptionCode {

    NOT_AUTHORIZED("3000001"),
    ERROR_COPYING_UPLOADED_FILE_TO_SERVER("3000002"),
    PBA_IS_INVALID("3000003"),
    PBA_NOT_FOUND("3000004"),
    ERROR_INVOKING_METADATASERVICE_ON_PUBLISHED("3000005"),
    ERROR_INVOKING_METADATASERVICE_ON_UNPUBLISHED("3000006"),
    PBA_IS_CORRUPTED("3000007"),
    ERROR_INVOKING_METADATASERVICE_ON_LIST("3000008"),
    ERROR_INVOKING_METADATASERVICE_ON_GET("3000009"),
    ERROR_READING_SCHEMMA("3000010"),
    TEMPLATE_NOT_FOUND("3000011"),
    ERROR_EXTRACTING_APPLICATION_DATA("3000012"),
    ERROR_UPDATING_PBA("3000013"),
    ERROR_ACCESSING_GIT_REPOSITORY("3000014"),
    ERROR_CREATE_TEMPLATE("3000015"),
    PARENT_TEMPLATE_NOT_FOUND("3000016"),
    DOCKER_IMAGE_NOT_FOUND_FOR_DEPENDENCY("3000017"),
    WRONG_ENVIRONMENT_GIT_URL("3000018");

    private final String code;

    /**
     * @param code
     *            error code
     */
    TemplateManagerExceptionCodes(final String code) {
        this.code = code;
    }

    /**
     * @param text
     *            code in string format
     * @return the equivalent enum
     */
    public TemplateManagerExceptionCodes getExceptionCodes(final String text) {
        for (final TemplateManagerExceptionCodes code : TemplateManagerExceptionCodes.values()) {
            if (code.getCode().equals(text)) {
                return code;
            }
        }
        return null;
    }

    @Override
    public String getCode() {
        return code;
    }

    @Override
    public String toString() {
        return this.code;
    }

}
