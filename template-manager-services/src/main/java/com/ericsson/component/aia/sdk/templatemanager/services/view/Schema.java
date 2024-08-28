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
package com.ericsson.component.aia.sdk.templatemanager.services.view;

import com.fasterxml.jackson.annotation.JsonRawValue;

/**
 * The Class Schema is a view of the current PBA schema for the UI.
 */
public class Schema {

    @JsonRawValue
    private String schema;

    @JsonRawValue
    public String getSchema() {
        return schema;
    }

    @JsonRawValue
    public void setSchema(final String schema) {
        this.schema = schema;
    }
}
