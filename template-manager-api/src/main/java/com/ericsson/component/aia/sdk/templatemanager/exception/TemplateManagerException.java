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
package com.ericsson.component.aia.sdk.templatemanager.exception;

import com.ericsson.component.aia.sdk.templatemanager.TemplateManager;
import com.ericsson.component.aia.sdk.templatemanager.exception.code.ExceptionCode;

/**
 * This exception will be thrown if any exception occurs during {@link TemplateManager} operations.
 *
 * @author echchik
 *
 */
public class TemplateManagerException extends AppSdkException {

    private static final long serialVersionUID = -2234126889509568637L;

    /**
     * Default constructor.
     */
    public TemplateManagerException() {
        super();
    }

    /**
     * @param code
     *            error code
     * @param msg
     *            message
     */
    public TemplateManagerException(final ExceptionCode code, final String msg) {
        super(code, msg);
    }

    /**
     * @param code
     *            error code
     * @param msg
     *            message
     * @param exception
     *            exception to be throw
     */
    public TemplateManagerException(final String code, final String msg, final Throwable exception) {
        super(code, msg, exception);
    }

    /**
     * @param code
     *            error code
     * @param exception
     *            exception to be throw
     */
    public TemplateManagerException(final ExceptionCode code, final Throwable exception) {
        super(code, exception);
    }

    /**
     * @param code
     *            error code
     * @param msg
     *            message
     * @param runtimeException
     *            exception to be throw
     */
    public TemplateManagerException(final String code, final String msg, final String runtimeException) {
        super(code, msg, runtimeException);
    }

    /**
     * @param code
     *            error code
     * @param msg
     *            message
     * @param exception
     *            exception to be throw
     */
    public TemplateManagerException(final ExceptionCode code, final String msg, final Throwable exception) {
        super(code, msg, exception);
    }

    /**
     * @param code
     *            error code
     * @param msg
     *            message
     * @param exception
     *            exception to be throw
     * @param runtimeException
     *            runtime error code
     */
    public TemplateManagerException(final String code, final String msg, final Throwable exception, final String runtimeException) {
        super(code, msg, exception, runtimeException);
    }

}
