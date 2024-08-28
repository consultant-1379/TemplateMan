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

import java.util.UUID;

import com.ericsson.component.aia.sdk.templatemanager.exception.code.ExceptionCode;

/**
 * @author ezsalro
 *
 */
public class AppSdkException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private final String code;

    private final String runtimeCode;

    /**
     * Default constructor.
     */
    public AppSdkException() {
        this.code = null;
        this.runtimeCode = null;
    }

    /**
     * @param exception
     *            error to be throw
     */
    public AppSdkException(final Throwable exception) {
        super(exception);
        this.code = null;
        this.runtimeCode = null;
    }

    /**
     * @param msg
     *            message
     * @param exception
     *            error
     */
    public AppSdkException(final String msg, final Throwable exception) {
        super(msg, exception);
        this.code = null;
        this.runtimeCode = null;

    }

    /**
     * @param code
     *            error code
     * @param msg
     *            message
     */
    public AppSdkException(final String code, final String msg) {
        super(msg);
        this.code = code;
        this.runtimeCode = UUID.randomUUID().toString().replaceAll("-", "");
    }

    /**
     * @param code
     *            error code
     * @param msg
     *            message
     * @param runtimeException
     *            exception
     */
    public AppSdkException(final String code, final String msg, final String runtimeException) {
        super(msg);
        this.code = code;
        this.runtimeCode = runtimeException;
    }

    /**
     * @param code
     *            error code
     * @param msg
     *            message
     * @param exception
     *            exception to be throw
     */
    public AppSdkException(final String code, final String msg, final Throwable exception) {
        super(msg, exception);
        this.code = code;
        this.runtimeCode = UUID.randomUUID().toString().replaceAll("-", "");
    }

    /**
     * @param code
     *            error code
     * @param msg
     *            message
     * @param exception
     *            exception to be throw
     */
    public AppSdkException(final ExceptionCode code, final String msg, final Throwable exception) {
        this(code.toString(), msg, exception);
    }

    /**
     * @param code
     *            error code
     * @param exception
     *            exception to be throw
     */
    public AppSdkException(final ExceptionCode code, final Throwable exception) {
        this(code.toString(), exception.getMessage(), exception);
    }

    /**
     * @param code
     *            error code
     * @param msg
     *            message
     */
    public AppSdkException(final ExceptionCode code, final String msg) {
        this(code.toString(), msg);
    }

    /**
     * @param code
     *            error code
     * @param msg
     *            message
     * @param exception
     *            exception to be throw
     * @param runtimeException
     *            runtime code
     */
    public AppSdkException(final String code, final String msg, final Throwable exception, final String runtimeException) {
        super(msg, exception);
        this.code = code;
        this.runtimeCode = runtimeException;
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
    public AppSdkException(final ExceptionCode code, final String msg, final Throwable exception, final String runtimeException) {
        super(msg, exception);
        this.code = code.toString();
        this.runtimeCode = runtimeException;
    }

    /**
     * @return code
     */
    public String getCode() {
        return code;
    }

    /**
     * @return runtime code
     */
    public String getRuntimeCode() {
        return runtimeCode;
    }

}
