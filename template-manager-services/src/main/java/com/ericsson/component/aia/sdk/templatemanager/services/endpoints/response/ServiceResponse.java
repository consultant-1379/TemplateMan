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
package com.ericsson.component.aia.sdk.templatemanager.services.endpoints.response;

import com.ericsson.component.aia.sdk.templatemanager.views.ResponseError;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Represents A service Response. Contains the data that is returned to the UI
 *
 * @author echchik
 *
 * @param <T>
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class ServiceResponse<T> {

    @JsonProperty("data")
    @JsonInclude(Include.NON_NULL)
    private T data;

    @JsonInclude(Include.NON_NULL)
    private ResponseError error;

    /**
     * Default constructor.
     */
    public ServiceResponse() {
    }

    /**
     * Constructor with <T> as parameter.
     *
     * @param data
     *            of the service response.
     */
    public ServiceResponse(final T data) {
        this();
        this.data = data;
    }

    public T getData() {
        return data;
    }

    public void setData(final T data) {
        this.data = data;
    }

    public ResponseError getError() {
        return error;
    }

    public void setError(final ResponseError error) {
        this.error = error;
    }

}
