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
package com.ericsson.component.aia.sdk.templatemanager.services.configuration;

import java.util.Properties;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Meta store configuration
 */
@Component
public class MetaStoreConfig {

    @Value("${datastore.type}")
    private String type;

    @Value("${datastore.host}")
    private String host;

    @Value("${datastore.port}")
    private String port;

    @Value("${datastore.username}")
    private String username;

    @Value("${datastore.password}")
    private String password;

    @Value("${datastore.db}")
    private String datastoreDb;

    /**
     * Gets the Meta store properties based on the application configuration.
     *
     * @return {@link Properties} The meta data store properties
     */
    public Properties getMetaStoreProperties() {
        final Properties properties = new Properties();
        properties.put("datastore.type", type);
        properties.put("datastore.host", host);
        properties.put("datastore.port", port);
        properties.put("datastore.username", username);
        properties.put("datastore.password", password);
        properties.put("datastore.db", datastoreDb);
        return properties;
    }

}