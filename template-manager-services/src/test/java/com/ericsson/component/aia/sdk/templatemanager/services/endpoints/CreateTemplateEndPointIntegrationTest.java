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
package com.ericsson.component.aia.sdk.templatemanager.services.endpoints;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import java.io.File;
import java.io.IOException;

import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import com.ericsson.aia.metadata.api.MetaDataServiceIfc;
import com.ericsson.aia.metadata.exception.MetaDataServiceException;
import com.ericsson.aia.metadata.model.MetaData;
import com.ericsson.component.aia.sdk.pba.tools.PBASchemaTool;
import com.ericsson.component.aia.sdk.templatemanager.services.configuration.TemplateManagerTestApplication;

/**
 * Integration test for {@link TemplateManagerEndPoint}
 *
 * @author echchik
 *
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = TemplateManagerTestApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(locations = "classpath:application-test.properties")
public class CreateTemplateEndPointIntegrationTest {

    private static final String TEST_PBA_JSON = "src/test/resources/test-files/pba.json";

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private MetaDataServiceIfc metaDataService;

    @Value("${template.catalog.name}")
    private String templateCatalogName;

    @Test
    public void shouldReturn500ResponseCodeWhenCreateTemplateFails() throws Exception {
        final String pbaAsString = "THIS IS AN INVALID PBA STRING";
        final HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        final HttpEntity<String> entity = new HttpEntity<>(pbaAsString, headers);
        final ResponseEntity<String> response = this.restTemplate.postForEntity("/templates", entity, String.class);
        assertThat(response.getStatusCodeValue(), equalTo(500));
    }

    protected String getPbaAsString() throws IOException {
        final File pbaJson = new File(TEST_PBA_JSON);
        final String pbaAsString = PBASchemaTool.readStreamAsStringFronFile(pbaJson.toPath());
        return pbaAsString;
    }

    @After
    public void cleanUp() throws MetaDataServiceException {
        for (final MetaData metaData : metaDataService.findAll(templateCatalogName)) {
            if (!metaData.getKey().isEmpty()) {
                metaDataService.delete(templateCatalogName, metaData.getKey());
            }
        }
    }
}
