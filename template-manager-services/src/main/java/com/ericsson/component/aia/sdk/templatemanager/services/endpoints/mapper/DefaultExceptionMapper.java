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
package com.ericsson.component.aia.sdk.templatemanager.services.endpoints.mapper;

import static org.springframework.http.ResponseEntity.status;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import com.ericsson.component.aia.sdk.templatemanager.services.endpoints.response.ServiceResponse;
import com.ericsson.component.aia.sdk.templatemanager.views.ResponseError;

/**
 * This class provides all the {@link ExceptionHandler} of this application.
 *
 * @author echchik
 *
 */
@ControllerAdvice
public class DefaultExceptionMapper {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    /**
     * Exception handler for {@link Exception}.
     *
     * @param req
     *            {@link HttpServletRequest}
     * @param exception
     *            {@link Exception} to handle.
     * @return {@link ServiceResponse} of the application for {@link Exception}.
     * @throws Exception
     *             if {@link Exception} handling throws exception.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ServiceResponse<Object>> defaultErrorHandler(final HttpServletRequest req, final Exception exception) {
        logger.error(exception.getMessage(), exception);
        return status(500).body(getResponseError(exception));
    }

    private ServiceResponse<Object> getResponseError(final Exception exception) {
        final ServiceResponse<Object> serviceResponse = new ServiceResponse<Object>();
        serviceResponse.setError(new ResponseError(exception));
        return serviceResponse;
    }
}
