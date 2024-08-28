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
import static org.skyscreamer.jsonassert.JSONAssert.assertEquals;

import java.io.File;
import java.io.IOException;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import com.ericsson.aia.metadata.api.MetaDataServiceIfc;
import com.ericsson.aia.metadata.exception.MetaDataServiceException;
import com.ericsson.component.aia.sdk.pba.model.PBAInstance;
import com.ericsson.component.aia.sdk.pba.tools.PBASchemaTool;
import com.ericsson.component.aia.sdk.templatemanager.exception.code.TemplateManagerExceptionCodes;
import com.ericsson.component.aia.sdk.templatemanager.services.configuration.TemplateManagerTestApplication;
import com.ericsson.component.aia.sdk.templatemanager.services.endpoints.response.ServiceResponse;
import com.ericsson.component.aia.sdk.templatemanager.util.TemplateManagerUtil;
import com.ericsson.component.aia.sdk.templatemanager.views.ResponseError;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Integration test for {@link TemplateManagerEndPoint}
 *
 * @author echchik
 *
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = TemplateManagerTestApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(locations = "classpath:application-test.properties")
public class TemplateManagerEndPointIntegrationTest {

    private static final String TEST_METADATA_KEY = "pba.templateInfo.id";
    private static final String TEST_PBA_JSON = "src/test/resources/test-files/pba.json";

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private MetaDataServiceIfc metaDataService;

    @Autowired
    private ObjectMapper objectMapper;

    @Value("${template.catalog.name}")
    private String templateCatalogName;

    private final PBASchemaTool pbaSchemaTool = new PBASchemaTool();

    @Test
    public void shouldReturnOkStatusWhenPublishTemplateIsSuccessful() throws Exception {
        final MultiValueMap<String, Object> map = new LinkedMultiValueMap<>();
        map.add("applicationzip", new ClassPathResource("aia-spark-streaming-1.0.12.zip", getClass()));
        map.add("version", "1.0.12");
        final HttpEntity<MultiValueMap<String, Object>> entity = new HttpEntity<>(map);

        final ResponseEntity<String> response = this.restTemplate.postForEntity("/templates/publish", entity, String.class);
        assertThat(response.getStatusCodeValue(), equalTo(200));
    }

    @SuppressWarnings("rawtypes")
    @Test
    public void shouldReturnResponseEntityWhenListTemplatesIsSuccessful() throws Exception {
        final String pbaId = addPbaIntoMetaDataStore();
        final ResponseEntity<ServiceResponse> response = this.restTemplate.getForEntity("/templates/" + pbaId, ServiceResponse.class);
        assertThat(response.getStatusCodeValue(), equalTo(200));
        final String responseAsString = objectMapper.writeValueAsString(response.getBody());
        final File expectedResonse = new File(
                "src/test/resources/response/integration-test/shouldReturnResponseEntityWhenListTemplatesIsSuccessful-expected.json");
        final String expectedResonseAsString = PBASchemaTool.readStreamAsStringFronFile(expectedResonse.toPath());
        assertEquals(expectedResonseAsString, responseAsString, true);
        this.restTemplate.delete("/templates/" + pbaId);
    }

    @SuppressWarnings("rawtypes")
    @Test
    public void shouldReturnResponseEntityWhenListTemplatesAndNoneExist() throws Exception {
        final ResponseEntity<ServiceResponse> response = this.restTemplate.getForEntity("/templates", ServiceResponse.class);
        assertThat(response.getStatusCodeValue(), equalTo(200));
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Test
    public void shouldReturn500ResponseCodeForGetOnTemplateWhichDoesNotExist() throws Exception {
        final String pbaId = "INVALID_PBA_ID_STRING";
        final ResponseEntity<ServiceResponse> response = this.restTemplate.getForEntity("/templates/" + pbaId, ServiceResponse.class);
        assertThat(response.getStatusCodeValue(), equalTo(500));
        final ServiceResponse<PBAInstance> body = response.getBody();
        final PBAInstance pbaInstance = body.getData();
        Assert.assertNull(pbaInstance);
        final ResponseError error = body.getError();
        assertEquals(TemplateManagerExceptionCodes.ERROR_INVOKING_METADATASERVICE_ON_GET.getCode(), error.getCode(), true);
    }

    @Test
    public void shouldReturnOkStatusWhenUnPublishTemplatesIsSuccessful() throws Exception {
        final String pbaId = addPbaIntoMetaDataStore();
        this.restTemplate.delete("/templates/" + pbaId);
    }

    @Test
    public void shouldReturnResponseEntityWithStreamWhenFileDownloadIsSuccessful() throws Exception {
        final String pbaId = addPbaIntoMetaDataStore();
        final ResponseEntity<InputStreamResource> response = this.restTemplate.getForEntity("/templates/" + pbaId + "/download",
                InputStreamResource.class);
        assertThat(response.getStatusCodeValue(), equalTo(200));
    }

    @Test
    public void shouldReturn500ResponseCodeIfFileDownloadThrowsException() throws Exception {
        final ResponseEntity<InputStreamResource> response = this.restTemplate.getForEntity("/templates/dert45-758hf-jd56-hrts/download",
                InputStreamResource.class);
        assertThat(response.getStatusCodeValue(), equalTo(500));
    }

    private String addPbaIntoMetaDataStore() throws IOException, MetaDataServiceException {
        final String pbaAsString = getPbaAsString();
        final PBAInstance pbaInstance = pbaSchemaTool.getPBAModelInstance(pbaAsString);
        final String pbaId = TemplateManagerUtil.createTemplateId(pbaInstance.getPba());
        metaDataService.put(templateCatalogName, TEST_METADATA_KEY, pbaId, pbaAsString);
        return pbaId;
    }

    private String getPbaAsString() throws IOException {
        final File pbaJson = new File(TEST_PBA_JSON);
        final String pbaAsString = PBASchemaTool.readStreamAsStringFronFile(pbaJson.toPath());
        return pbaAsString;
    }

}
