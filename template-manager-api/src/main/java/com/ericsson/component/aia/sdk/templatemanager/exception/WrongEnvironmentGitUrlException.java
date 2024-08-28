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

/**
 * @author ezsalro
 *
 */
public class WrongEnvironmentGitUrlException extends Exception {

    private static final long serialVersionUID = 1L;

    /**
     * Default constructor.
     */
    public WrongEnvironmentGitUrlException() {
    }

    /**
     * @param message
     *            - message
     */
    public WrongEnvironmentGitUrlException(final String message) {
        super(message);
    }

    /**
     * @param cause
     *            - cause
     */
    public WrongEnvironmentGitUrlException(final Throwable cause) {
        super(cause);
    }

    /**
     * @param message
     *            - message
     * @param cause
     *            - cause
     */
    public WrongEnvironmentGitUrlException(final String message, final Throwable cause) {
        super(message, cause);
    }

    /**
     * @param message
     *            - message
     * @param cause
     *            - cause
     * @param enableSuppression
     *            - enable suppression
     * @param writableStackTrace
     *            - stack trace
     */
    public WrongEnvironmentGitUrlException(final String message, final Throwable cause, final boolean enableSuppression,
                                           final boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
