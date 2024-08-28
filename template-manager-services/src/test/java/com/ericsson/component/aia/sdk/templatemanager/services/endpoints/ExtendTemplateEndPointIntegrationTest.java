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
import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.spi.json.JacksonJsonNodeJsonProvider;
import com.jayway.jsonpath.spi.mapper.JacksonMappingProvider;

/**
 * Integration test for {@link TemplateManagerEndPoint}
 *
 * @author echchik
 *
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = TemplateManagerTestApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(locations = "classpath:application-test.properties")
public class ExtendTemplateEndPointIntegrationTest {

    private static final String TEST_METADATA_KEY = "pba.templateInfo.id";

    private static final String TEST_PBA_JSON = "src/test/resources/test-files/pba.json";

    private static final String TEST_EXTENDED_TEMPLATE_PBA_JSON = "src/test/resources/test-files/extended_template_pba.json";

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private MetaDataServiceIfc metaDataService;

    @Value("${template.catalog.name}")
    private String templateCatalogName;

    private static final Configuration configuration = Configuration.builder().jsonProvider(new JacksonJsonNodeJsonProvider())
            .mappingProvider(new JacksonMappingProvider()).build();

    @Test
    public void shouldReturnResponseEntityWithStreamWhenExtendTemplateIsSuccessful() throws Exception {
        final String existingTemplatePbaId = addPbaIntoMetaDataStore();
        final File pbaJson = new File(TEST_EXTENDED_TEMPLATE_PBA_JSON);

        final String pbaAsString = JsonPath.using(configuration).parse(PBASchemaTool.readStreamAsStringFronFile(pbaJson.toPath()))
                .set("$.pba.templateInfo.id", existingTemplatePbaId).json().toString();

        final HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        final HttpEntity<String> entity = new HttpEntity<>(pbaAsString, headers);

        final ResponseEntity<String> response = this.restTemplate.postForEntity("/templates/extend", entity, String.class);

        assertThat(response.getStatusCodeValue(), equalTo(200));
    }

    @Test
    public void shouldReturn500ResponseCodeForExtendOfInvalidTemplate() throws Exception {
        final File pbaJson = new File(TEST_EXTENDED_TEMPLATE_PBA_JSON);
        final String pbaAsString = JsonPath.using(configuration).parse(PBASchemaTool.readStreamAsStringFronFile(pbaJson.toPath()))
                .set("$.pba.templateInfo.id", "INVALID_TEMPLATE_PBA_ID").json().toString();

        final HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        final HttpEntity<String> entity = new HttpEntity<>(pbaAsString, headers);

        final ResponseEntity<String> response = this.restTemplate.postForEntity("/templates/extend", entity, String.class);
        assertThat(response.getStatusCodeValue(), equalTo(500));
        // TODO: At present the error returned from the MetaService is not meaningful. Update this test once this has been sorted out.
        // String expectedErrorMessage = "Extend operation failed for template with ID " + pbaId;
        // Assert.assertEquals(expectedErrorMessage, errorMessage);
    }

    private String addPbaIntoMetaDataStore() throws IOException, MetaDataServiceException {
        final String pbaAsString = getPbaAsString();
        final String pbaId = metaDataService.put(templateCatalogName, TEST_METADATA_KEY, pbaAsString);
        return pbaId;
    }

    private String getPbaAsString() throws IOException {
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
