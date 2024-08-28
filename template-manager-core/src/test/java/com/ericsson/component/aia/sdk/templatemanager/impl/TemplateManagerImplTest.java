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
package com.ericsson.component.aia.sdk.templatemanager.impl;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collection;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.ericsson.aia.metadata.api.MetaDataServiceIfc;
import com.ericsson.aia.metadata.exception.MetaDataServiceException;
import com.ericsson.aia.metadata.model.MetaData;
import com.ericsson.component.aia.sdk.pba.model.PBAInstance;
import com.ericsson.component.aia.sdk.pba.model.Pba;
import com.ericsson.component.aia.sdk.pba.model.PbaInfo;
import com.ericsson.component.aia.sdk.pba.tools.PBASchemaTool;
import com.ericsson.component.aia.sdk.templatemanager.exception.TemplateManagerException;
import com.ericsson.component.aia.sdk.templatemanager.util.TemplateManagerUtil;

@RunWith(MockitoJUnitRunner.class)
public class TemplateManagerImplTest {

    @Mock
    private MetaDataServiceIfc metaDataService;

    @Mock
    private PBASchemaTool pbaSchemaTool;

    @Mock
    private PBAInstance pbaInstance;

    @Mock
    private Pba pba;

    @Mock
    private PbaInfo pbaInfo;

    @Mock
    private TemplateManagerConfiguration templateManagerConfig;

    @Mock
    private MetaData metaData;

    @Mock
    private TemplateManagerUtil zipFileUtil;

    @InjectMocks
    private TemplateManagerImpl templateManagerImpl;

    @Before
    public void setup() {
        when(pbaInstance.getPba()).thenReturn(pba);
        when(pba.getTemplateInfo()).thenReturn(pbaInfo);
        when(pbaInfo.getName()).thenReturn("name");
        when(pbaInfo.getVersion()).thenReturn("1.0.0");
        when(pbaInfo.getId()).thenReturn("df5t-78hd-89fg-gh84");
    }

    @Test
    public void unPublishShouldReturnTrueIfSuccessful() {
        final boolean result = templateManagerImpl.unPublishTemplate("id");
        assertThat(result, equalTo(true));
    }

    @Test(expected = TemplateManagerException.class)
    public void unPublishShouldThrowExceptionIfOperationFails() throws MetaDataServiceException {
        doThrow(new MetaDataServiceException("")).when(metaDataService).delete(any(String.class), any(String.class));
        templateManagerImpl.unPublishTemplate("id");
    }

    @Test
    public void getShouldReturnPBAInstanceIfSuccessful() throws MetaDataServiceException {
        when(metaDataService.get(any(String.class), any(String.class))).thenReturn("validPba");
        when(pbaSchemaTool.getPBAModelInstance(any(String.class))).thenReturn(pbaInstance);
        final PBAInstance pbaInstance = templateManagerImpl.getPbaInstance("id");
        assertThat(pbaInstance, equalTo(this.pbaInstance));
    }

    @Test(expected = TemplateManagerException.class)
    public void getShouldThrowExceptionIfOperationFails() throws MetaDataServiceException {
        doThrow(new MetaDataServiceException("")).when(metaDataService).get(any(String.class), any(String.class));
        templateManagerImpl.getPbaInstance("id");
    }

    @Test
    public void listShouldReturnCollectionOfTemplateInfoIfSuccessful() throws MetaDataServiceException {
        final ArrayList<MetaData> metaDatas = new ArrayList<>();
        metaDatas.add(metaData);
        when(metaDataService.find(any(String.class), any(String.class))).thenReturn(metaDatas);
        when(metaData.getValue()).thenReturn("validPba");
        when(pbaSchemaTool.getPBAModelInstance(any(String.class))).thenReturn(pbaInstance);

        final Collection<PbaInfo> templateInfos = templateManagerImpl.listTemplates();
        assertThat(true, equalTo(templateInfos != null));
        assertThat(templateInfos.size(), equalTo(1));
    }

    @Test(expected = TemplateManagerException.class)
    public void listShouldThrowExceptionIfOperationFails() throws MetaDataServiceException {
        doThrow(new MetaDataServiceException("")).when(metaDataService).find(any(String.class), any(String.class));
        templateManagerImpl.listTemplates();
    }
}
