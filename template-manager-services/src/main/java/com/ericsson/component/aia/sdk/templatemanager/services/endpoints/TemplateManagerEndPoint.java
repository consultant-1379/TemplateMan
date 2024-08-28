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

import static org.springframework.http.ResponseEntity.status;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collection;

import javax.ws.rs.core.Application;

import org.json.JSONException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.ericsson.component.aia.sdk.pba.model.PBAInstance;
import com.ericsson.component.aia.sdk.pba.model.PbaInfo;
import com.ericsson.component.aia.sdk.pba.tools.PBASchemaTool;
import com.ericsson.component.aia.sdk.templatemanager.TemplateManager;
import com.ericsson.component.aia.sdk.templatemanager.cache.ArchivePathCache;
import com.ericsson.component.aia.sdk.templatemanager.cache.DownloadCache;
import com.ericsson.component.aia.sdk.templatemanager.exception.TemplateManagerException;
import com.ericsson.component.aia.sdk.templatemanager.exception.code.TemplateManagerExceptionCodes;
import com.ericsson.component.aia.sdk.templatemanager.services.endpoints.response.ServiceResponse;
import com.ericsson.component.aia.sdk.templatemanager.services.view.Schema;

import io.swagger.annotations.ApiOperation;

/**
 * This class defines the REST endpoints supported by the Template manager application.
 *
 * @author echchik
 *
 */
@CrossOrigin
@RestController
public class TemplateManagerEndPoint {

    @Autowired
    private ArchivePathCache simpleCache;

    @Autowired
    private TemplateManager templateManager;

    @Autowired
    private PBASchemaTool pbaSchemaTool;

    @Autowired
    private DownloadCache downloadCache;

    /**
     * This POST endpoint accepts {@link MultipartFile} and {@link PBAInstance} as json string and return {@link HttpStatus.OK} code
     *
     * @param file
     *            {@link PBAInstance}zip file of the template to publish
     * @param version
     *            the version of the template being published.
     * @throws IOException
     *             an exception occurs when reading.
     */
    @ResponseBody
    @ResponseStatus(value = HttpStatus.OK)
    @RequestMapping(value = "/templates/publish", method = RequestMethod.POST)
    @ApiOperation(value = "publishTemplate accepts applicationzip file, version, technology, serviceId and returns void")
    public void publishTemplate(@RequestParam("applicationzip") final MultipartFile file, @RequestParam("version") final String version)
            throws IOException {
        final InputStream inputStream = file.getInputStream();
        templateManager.publishTemplate(inputStream, file.getOriginalFilename(), version);
        inputStream.close();
    }

    /**
     * This DELETE endpoint accepts name and version of the template to delete and return {@link HttpStatus.OK} if successful.
     *
     * @param pbaId
     *            of the template
     */
    @RequestMapping(value = "/templates/{id:.+}", method = RequestMethod.DELETE)
    @ResponseStatus(value = HttpStatus.OK)
    @ApiOperation(value = "unPublishTemplate accepts pbaId and returns void")
    public void unPublishTemplate(@PathVariable("id") final String pbaId) {
        templateManager.unPublishTemplate(pbaId);
    }

    /**
     * This DELETE endpoint accepts the id of the template to delete and returns {@link HttpStatus.OK} if successful.
     *
     * @param pbaId
     *            of the template
     */
    @RequestMapping(value = "/templates/cleanup/{id:.+}", method = RequestMethod.DELETE)
    @ResponseStatus(value = HttpStatus.OK)
    @ApiOperation(value = "cleanupTemplate accepts pbaId and returns void")
    public void cleanupTemplate(@PathVariable("id") final String pbaId) {
        templateManager.cleanupTemplate(pbaId);
    }

    /**
     * This DELETE endpoint clears the cache which stores templates when the download operation is invoked. This endpoint returns
     * {@link HttpStatus.OK} if successful.
     *
     */
    @RequestMapping(value = "/templates/cache/clear", method = RequestMethod.DELETE)
    @ResponseStatus(value = HttpStatus.OK)
    @ApiOperation(value = "clearCache clears the cache which stores templates")
    public void clearCache() {
        downloadCache.clearCache();
    }

    /**
     * This POST endpoint accepts name and version of the template to be extended and {@link PBAInstance} of new template as json string and return
     * {@link HttpStatus.OK} code
     *
     * @param pbaModelAsString
     *            {@link String} as json of the template to publish
     * @return {@link InputStreamResource} of the extended template to download.
     * @throws FileNotFoundException
     *             if the file to download does not exists.
     */
    @RequestMapping(value = "/templates/extend", method = RequestMethod.POST)
    @ApiOperation(value = "extendTemplate accepts pbaModelAsString, returns HttpStatus.OK and serviceResponse")
    public ResponseEntity<ServiceResponse<String>> extendTemplate(@RequestBody final String pbaModelAsString) throws FileNotFoundException {
        final PBAInstance pbaInstance = pbaSchemaTool.getPBAModelInstance(pbaModelAsString);
        final Path path = templateManager.extendTemplate(pbaInstance);
        final ServiceResponse<String> serviceResponse = new ServiceResponse<>();
        final String uniqueFileDownloadId = simpleCache.add(path);
        serviceResponse.setData(uniqueFileDownloadId);
        return status(HttpStatus.OK).body(serviceResponse);
    }

    /**
     * This POST endpoint accepts name and version of the template to be extended and {@link PBAInstance} of new template as json string and return
     * {@link HttpStatus.OK} code
     *
     * @param pbaModelAsString
     *            {@link String} as json of the template to publish
     * @return {@link InputStreamResource} of the extended template to download.
     * @throws FileNotFoundException
     *             if the file to download does not exists.
     */
    @RequestMapping(value = "/templates", method = RequestMethod.POST)
    @ApiOperation(value = "createTemplate accepts pbaModelAsString, returns HttpStatus.OK and serviceResponse")
    public ResponseEntity<ServiceResponse<String>> createTemplate(@RequestBody final String pbaModelAsString) throws FileNotFoundException {
        final Path path = templateManager.createTemplate(pbaModelAsString);
        final ServiceResponse<String> serviceResponse = new ServiceResponse<>();
        final String uniqueFileDownloadId = simpleCache.add(path);
        serviceResponse.setData(uniqueFileDownloadId);
        return status(HttpStatus.OK).body(serviceResponse);
    }

    /**
     * This GET return {@link HttpStatus.OK} code and collection of {@link PbaInfo} as json.
     *
     * @return {@link ServiceResponse} containing collection of {@link PbaInfo} as data.
     */
    @RequestMapping(value = "/templates", method = RequestMethod.GET)
    @ApiOperation(value = "listTemplates returns HttpStatus.OK and serviceResponse")
    public ResponseEntity<ServiceResponse<Collection<PbaInfo>>> listTemplates() {
        final Collection<PbaInfo> templateInfos = templateManager.listTemplates();
        final ServiceResponse<Collection<PbaInfo>> serviceResponse = new ServiceResponse<>();
        serviceResponse.setData(templateInfos);
        return status(HttpStatus.OK).body(serviceResponse);
    }

    /**
     * This GET endpoint accepts name and version of the template and return {@link HttpStatus.OK} code and {@link PBAInstance} as json.
     *
     * @param pbaId
     *            of the template
     * @return {@link ServiceResponse} containing {@link PBAInstance} as data
     */
    @RequestMapping(value = "/templates/{id:.+}", method = RequestMethod.GET)
    @ApiOperation(value = "getTemplatePba accepts pbaId returns HttpStatus.OK and serviceResponse")
    public ResponseEntity<ServiceResponse<PBAInstance>> getTemplatePba(@PathVariable("id") final String pbaId) {
        final PBAInstance pbaModel = templateManager.getPbaInstance(pbaId);
        final ServiceResponse<PBAInstance> serviceResponse = new ServiceResponse<>();
        serviceResponse.setData(pbaModel);
        return status(HttpStatus.OK).body(serviceResponse);
    }

    /**
     * This GET endpoint accepts name and version of the template and return {@link HttpStatus.OK} code and {@link InputStreamResource} of the
     * template to download.
     *
     * @param pbaId
     *            of the template
     * @return {@link InputStreamResource} of the template to download.
     * @throws FileNotFoundException
     *             if the file to download does not exists.
     */
    @RequestMapping(value = "/templates/{id}/download", method = RequestMethod.GET)
    @ApiOperation(value = "downloadTemplate accepts pbaId returns projectPath")
    public ResponseEntity<InputStreamResource> downloadTemplate(@PathVariable("id") final String pbaId) throws FileNotFoundException {
        final Path projectPath = downloadCache.get(pbaId);
        if (projectPath == null) {
            throw new TemplateManagerException(TemplateManagerExceptionCodes.PBA_NOT_FOUND, "Pba not found for id: " + pbaId);
        }
        return getInputStreamResourceResponseEntity(projectPath);
    }

    /**
     * This GET return {@link HttpStatus.OK} code and collection of {@link Application} published as json.
     *
     * @param projectId
     *            the unique id of the local zip file
     * @return {@link ServiceResponse} containing collection of {@link Application} as data.
     * @throws FileNotFoundException
     *             if the file to download does not exists.
     */
    @RequestMapping(value = "/templates/{id}/zip", method = RequestMethod.GET)
    @ApiOperation(value = "downloadProject accepts projectId returns projectPath")
    public ResponseEntity<InputStreamResource> downloadProject(@PathVariable("id") final String projectId) throws FileNotFoundException {
        final Path projectPath = simpleCache.get(projectId);
        return getInputStreamResourceResponseEntity(projectPath);
    }

    /**
     * This GET endpoint that returns a {@link Schema} to the user which contains the JSON schema which will be used to validate the all PBAs
     *
     * @return {@link InputStreamResource} of the template to download.
     * @throws IOException
     *             if the schema is not accessible.
     * @throws JSONException
     *             If the schema is not parsable as JSON.
     */
    @RequestMapping(value = "/schema", method = RequestMethod.GET)
    @ApiOperation(value = "templateSchema returns HttpStatus.OK and schema")
    public ResponseEntity<Schema> templateSchema() throws IOException, JSONException {
        final Schema schema = new Schema();
        schema.setSchema(pbaSchemaTool.getSchema());
        return status(HttpStatus.OK).body(schema);
    }

    private ResponseEntity<InputStreamResource> getInputStreamResourceResponseEntity(final Path path) throws FileNotFoundException {
        final File file = path.toFile();
        final HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        headers.setContentDispositionFormData("attachement;filename=", file.getName());
        headers.setAccessControlAllowHeaders(Arrays.asList("Content-Type"));
        headers.setAccessControlAllowOrigin("*");
        headers.setCacheControl("no-cache, no-store, must-revalidate");
        headers.setPragma("no-cache");
        headers.setExpires(0);
        headers.setContentLength(file.length());
        final InputStreamResource inputStreamResource = new InputStreamResource(new FileInputStream(file));
        final ResponseEntity<InputStreamResource> response = new ResponseEntity<>(inputStreamResource, headers, HttpStatus.OK);
        return response;
    }

}
