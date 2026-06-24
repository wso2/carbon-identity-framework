/*
 * Copyright (c) 2026, WSO2 LLC. (http://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.identity.application.authentication.framework.services;

import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.application.authentication.framework.exception.ConsentAppMappingException;
import org.wso2.carbon.identity.application.authentication.framework.internal.FrameworkServiceDataHolder;
import org.wso2.carbon.identity.configuration.mgt.core.ConfigurationManager;
import org.wso2.carbon.identity.configuration.mgt.core.constant.ConfigurationConstants.ErrorMessages;
import org.wso2.carbon.identity.configuration.mgt.core.exception.ConfigurationManagementClientException;
import org.wso2.carbon.identity.configuration.mgt.core.exception.ConfigurationManagementException;
import org.wso2.carbon.identity.configuration.mgt.core.model.Attribute;
import org.wso2.carbon.identity.configuration.mgt.core.model.Resource;
import org.wso2.carbon.identity.configuration.mgt.core.model.ResourceTypeAdd;
import org.wso2.carbon.identity.configuration.mgt.core.model.Resources;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.openMocks;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

/**
 * Unit tests for {@link ConsentAppMappingServiceImpl}.
 */
public class ConsentAppMappingServiceImplTest {

    private static final String PURPOSE_ID = "purpose-uuid-1";
    private static final String APP_ID = "app-resource-id-1";
    private static final String RESOURCE_TYPE = "consent-purpose-mapping";

    @Mock
    private ConfigurationManager configurationManager;

    private ConsentAppMappingServiceImpl service;
    private MockedStatic<FrameworkServiceDataHolder> dataHolderMock;

    @BeforeMethod
    public void setUp() {

        openMocks(this);
        service = new ConsentAppMappingServiceImpl();

        FrameworkServiceDataHolder dataHolder = mock(FrameworkServiceDataHolder.class);
        dataHolderMock = mockStatic(FrameworkServiceDataHolder.class);
        dataHolderMock.when(FrameworkServiceDataHolder::getInstance).thenReturn(dataHolder);
        when(dataHolder.getConfigurationManager()).thenReturn(configurationManager);
    }

    @AfterMethod
    public void tearDown() {

        dataHolderMock.close();
    }

    private static ConfigurationManagementException serverException() {

        return new ConfigurationManagementException(new RuntimeException("Server error"));
    }

    @Test
    public void testGetApplicationsForPurpose_success() throws Exception {

        Resource resource = new Resource();
        Attribute attr1 = new Attribute(APP_ID, null);
        Attribute attr2 = new Attribute("app-2", null);
        resource.setAttributes(Arrays.asList(attr1, attr2));
        when(configurationManager.getResource(RESOURCE_TYPE, PURPOSE_ID, false)).thenReturn(resource);

        List<String> result = service.getApplicationsForPurpose(PURPOSE_ID);

        assertEquals(result.size(), 2);
        assertTrue(result.contains(APP_ID));
        assertTrue(result.contains("app-2"));
    }

    @Test(expectedExceptions = ConsentAppMappingException.class)
    public void testGetApplicationsForPurpose_nullResource() throws Exception {

        when(configurationManager.getResource(RESOURCE_TYPE, PURPOSE_ID, false)).thenReturn(null);

        service.getApplicationsForPurpose(PURPOSE_ID);
    }

    @Test(expectedExceptions = ConsentAppMappingException.class)
    public void testGetApplicationsForPurpose_nullAttributes() throws Exception {

        Resource resource = new Resource();
        resource.setAttributes(null);
        when(configurationManager.getResource(RESOURCE_TYPE, PURPOSE_ID, false)).thenReturn(resource);

        service.getApplicationsForPurpose(PURPOSE_ID);
    }

    @Test(expectedExceptions = ConsentAppMappingException.class)
    public void testGetApplicationsForPurpose_resourceNotFoundClientException() throws Exception {

        ConfigurationManagementClientException ex = new ConfigurationManagementClientException(
                "Resource not found", ErrorMessages.ERROR_CODE_RESOURCE_DOES_NOT_EXISTS.getCode());
        when(configurationManager.getResource(RESOURCE_TYPE, PURPOSE_ID, false)).thenThrow(ex);

        service.getApplicationsForPurpose(PURPOSE_ID);
    }

    @Test(expectedExceptions = ConsentAppMappingException.class)
    public void testGetApplicationsForPurpose_resourceTypeNotFoundClientException() throws Exception {

        ConfigurationManagementClientException ex = new ConfigurationManagementClientException(
                "Resource type not found", ErrorMessages.ERROR_CODE_RESOURCE_TYPE_DOES_NOT_EXISTS.getCode());
        when(configurationManager.getResource(RESOURCE_TYPE, PURPOSE_ID, false)).thenThrow(ex);

        service.getApplicationsForPurpose(PURPOSE_ID);
    }

    @Test(expectedExceptions = ConsentAppMappingException.class)
    public void testGetApplicationsForPurpose_otherClientException() throws Exception {

        ConfigurationManagementClientException ex = new ConfigurationManagementClientException(
                "Other error", "CONFIGM_99999");
        when(configurationManager.getResource(RESOURCE_TYPE, PURPOSE_ID, false)).thenThrow(ex);

        service.getApplicationsForPurpose(PURPOSE_ID);
    }

    @Test(expectedExceptions = ConsentAppMappingException.class)
    public void testGetApplicationsForPurpose_serverException() throws Exception {

        when(configurationManager.getResource(RESOURCE_TYPE, PURPOSE_ID, false)).thenThrow(serverException());

        service.getApplicationsForPurpose(PURPOSE_ID);
    }

    @Test
    public void testAddApplicationToPurpose_newResource() throws Exception {

        ConfigurationManagementClientException ex = new ConfigurationManagementClientException(
                "Resource not found", ErrorMessages.ERROR_CODE_RESOURCE_DOES_NOT_EXISTS.getCode());
        when(configurationManager.getResource(RESOURCE_TYPE, PURPOSE_ID, false)).thenThrow(ex);

        service.addApplicationToPurpose(PURPOSE_ID, APP_ID);

        verify(configurationManager).addResource(eq(RESOURCE_TYPE), any(Resource.class));
    }

    @Test
    public void testAddApplicationToPurpose_existingResource() throws Exception {

        Resource resource = new Resource();
        resource.setAttributes(Collections.singletonList(new Attribute("other-app", null)));
        when(configurationManager.getResource(RESOURCE_TYPE, PURPOSE_ID, false)).thenReturn(resource);

        service.addApplicationToPurpose(PURPOSE_ID, APP_ID);

        verify(configurationManager).addAttribute(eq(RESOURCE_TYPE), eq(PURPOSE_ID), any(Attribute.class));
    }

    @Test
    public void testAddApplicationToPurpose_resourceTypeNotFound_createsTypeAndResource() throws Exception {

        ConfigurationManagementClientException ex = new ConfigurationManagementClientException(
                "Type not found", ErrorMessages.ERROR_CODE_RESOURCE_TYPE_DOES_NOT_EXISTS.getCode());
        when(configurationManager.getResource(RESOURCE_TYPE, PURPOSE_ID, false)).thenThrow(ex);

        service.addApplicationToPurpose(PURPOSE_ID, APP_ID);

        verify(configurationManager).addResourceType(any(ResourceTypeAdd.class));
        verify(configurationManager).addResource(eq(RESOURCE_TYPE), any(Resource.class));
    }

    @Test(expectedExceptions = ConsentAppMappingException.class)
    public void testAddApplicationToPurpose_alreadyMapped() throws Exception {

        Resource resource = new Resource();
        resource.setAttributes(Collections.singletonList(new Attribute(APP_ID, null)));
        when(configurationManager.getResource(RESOURCE_TYPE, PURPOSE_ID, false)).thenReturn(resource);

        service.addApplicationToPurpose(PURPOSE_ID, APP_ID);
    }

    @Test(expectedExceptions = ConsentAppMappingException.class)
    public void testAddApplicationToPurpose_getResourceOtherClientException() throws Exception {

        ConfigurationManagementClientException ex = new ConfigurationManagementClientException(
                "Other error", "CONFIGM_99999");
        when(configurationManager.getResource(RESOURCE_TYPE, PURPOSE_ID, false)).thenThrow(ex);

        service.addApplicationToPurpose(PURPOSE_ID, APP_ID);
    }

    @Test(expectedExceptions = ConsentAppMappingException.class)
    public void testAddApplicationToPurpose_getResourceServerException() throws Exception {

        when(configurationManager.getResource(RESOURCE_TYPE, PURPOSE_ID, false)).thenThrow(serverException());

        service.addApplicationToPurpose(PURPOSE_ID, APP_ID);
    }

    @Test(expectedExceptions = ConsentAppMappingException.class)
    public void testAddApplicationToPurpose_addResourceFails() throws Exception {

        ConfigurationManagementClientException notFound = new ConfigurationManagementClientException(
                "Not found", ErrorMessages.ERROR_CODE_RESOURCE_DOES_NOT_EXISTS.getCode());
        when(configurationManager.getResource(RESOURCE_TYPE, PURPOSE_ID, false)).thenThrow(notFound);
        when(configurationManager.addResource(anyString(), any(Resource.class))).thenThrow(serverException());

        service.addApplicationToPurpose(PURPOSE_ID, APP_ID);
    }

    @Test(expectedExceptions = ConsentAppMappingException.class)
    public void testAddApplicationToPurpose_createResourceTypeFails() throws Exception {

        ConfigurationManagementClientException typeNotFound = new ConfigurationManagementClientException(
                "Type not found", ErrorMessages.ERROR_CODE_RESOURCE_TYPE_DOES_NOT_EXISTS.getCode());
        when(configurationManager.getResource(RESOURCE_TYPE, PURPOSE_ID, false)).thenThrow(typeNotFound);
        when(configurationManager.addResourceType(any(ResourceTypeAdd.class))).thenThrow(serverException());

        service.addApplicationToPurpose(PURPOSE_ID, APP_ID);
    }

    @Test
    public void testRemoveApplicationFromPurpose_success() throws Exception {

        Resource resource = new Resource();
        resource.setAttributes(Collections.singletonList(new Attribute(APP_ID, null)));
        when(configurationManager.getResource(RESOURCE_TYPE, PURPOSE_ID, false)).thenReturn(resource);

        service.removeApplicationFromPurpose(PURPOSE_ID, APP_ID);

        verify(configurationManager).deleteAttribute(RESOURCE_TYPE, PURPOSE_ID, APP_ID);
    }

    @Test(expectedExceptions = ConsentAppMappingException.class)
    public void testRemoveApplicationFromPurpose_resourceNotFound() throws Exception {

        ConfigurationManagementClientException ex = new ConfigurationManagementClientException(
                "Not found", ErrorMessages.ERROR_CODE_RESOURCE_DOES_NOT_EXISTS.getCode());
        when(configurationManager.getResource(RESOURCE_TYPE, PURPOSE_ID, false)).thenThrow(ex);

        service.removeApplicationFromPurpose(PURPOSE_ID, APP_ID);
    }

    @Test(expectedExceptions = ConsentAppMappingException.class)
    public void testRemoveApplicationFromPurpose_otherClientException() throws Exception {

        ConfigurationManagementClientException ex = new ConfigurationManagementClientException(
                "Other error", "CONFIGM_99999");
        when(configurationManager.getResource(RESOURCE_TYPE, PURPOSE_ID, false)).thenThrow(ex);

        service.removeApplicationFromPurpose(PURPOSE_ID, APP_ID);
    }

    @Test(expectedExceptions = ConsentAppMappingException.class)
    public void testRemoveApplicationFromPurpose_serverException() throws Exception {

        when(configurationManager.getResource(RESOURCE_TYPE, PURPOSE_ID, false)).thenThrow(serverException());

        service.removeApplicationFromPurpose(PURPOSE_ID, APP_ID);
    }

    @Test(expectedExceptions = ConsentAppMappingException.class)
    public void testRemoveApplicationFromPurpose_nullResource() throws Exception {

        when(configurationManager.getResource(RESOURCE_TYPE, PURPOSE_ID, false)).thenReturn(null);

        service.removeApplicationFromPurpose(PURPOSE_ID, APP_ID);
    }

    @Test(expectedExceptions = ConsentAppMappingException.class)
    public void testRemoveApplicationFromPurpose_appNotMapped() throws Exception {

        Resource resource = new Resource();
        resource.setAttributes(Collections.singletonList(new Attribute("other-app", null)));
        when(configurationManager.getResource(RESOURCE_TYPE, PURPOSE_ID, false)).thenReturn(resource);

        service.removeApplicationFromPurpose(PURPOSE_ID, APP_ID);
    }

    @Test(expectedExceptions = ConsentAppMappingException.class)
    public void testRemoveApplicationFromPurpose_deleteAttributeFails() throws Exception {

        Resource resource = new Resource();
        resource.setAttributes(Collections.singletonList(new Attribute(APP_ID, null)));
        when(configurationManager.getResource(RESOURCE_TYPE, PURPOSE_ID, false)).thenReturn(resource);
        doThrow(serverException()).when(configurationManager).deleteAttribute(RESOURCE_TYPE, PURPOSE_ID, APP_ID);

        service.removeApplicationFromPurpose(PURPOSE_ID, APP_ID);
    }

    @Test
    public void testGetPurposesForApplication_success() throws Exception {

        Resource r1 = new Resource(PURPOSE_ID, RESOURCE_TYPE);
        r1.setAttributes(Collections.singletonList(new Attribute(APP_ID, null)));
        Resource r2 = new Resource("purpose-2", RESOURCE_TYPE);
        r2.setAttributes(Collections.singletonList(new Attribute("other-app", null)));
        Resources resources = new Resources();
        resources.setResources(Arrays.asList(r1, r2));
        when(configurationManager.getResourcesByType(RESOURCE_TYPE)).thenReturn(resources);

        List<String> result = service.getPurposesForApplication(APP_ID);

        assertEquals(result.size(), 1);
        assertEquals(result.get(0), PURPOSE_ID);
    }

    @Test
    public void testGetPurposesForApplication_nullResources() throws Exception {

        when(configurationManager.getResourcesByType(RESOURCE_TYPE)).thenReturn(null);

        List<String> result = service.getPurposesForApplication(APP_ID);

        assertTrue(result.isEmpty());
    }

    @Test
    public void testGetPurposesForApplication_emptyResources() throws Exception {

        Resources resources = new Resources();
        resources.setResources(null);
        when(configurationManager.getResourcesByType(RESOURCE_TYPE)).thenReturn(resources);

        List<String> result = service.getPurposesForApplication(APP_ID);

        assertTrue(result.isEmpty());
    }

    @Test
    public void testGetPurposesForApplication_resourceTypeNotFound() throws Exception {

        ConfigurationManagementClientException ex = new ConfigurationManagementClientException(
                "Type not found", ErrorMessages.ERROR_CODE_RESOURCE_TYPE_DOES_NOT_EXISTS.getCode());
        when(configurationManager.getResourcesByType(RESOURCE_TYPE)).thenThrow(ex);

        List<String> result = service.getPurposesForApplication(APP_ID);

        assertTrue(result.isEmpty());
    }

    @Test(expectedExceptions = ConsentAppMappingException.class)
    public void testGetPurposesForApplication_otherClientException() throws Exception {

        ConfigurationManagementClientException ex = new ConfigurationManagementClientException(
                "Other error", "CONFIGM_99999");
        when(configurationManager.getResourcesByType(RESOURCE_TYPE)).thenThrow(ex);

        service.getPurposesForApplication(APP_ID);
    }

    @Test(expectedExceptions = ConsentAppMappingException.class)
    public void testGetPurposesForApplication_serverException() throws Exception {

        when(configurationManager.getResourcesByType(RESOURCE_TYPE)).thenThrow(serverException());

        service.getPurposesForApplication(APP_ID);
    }

    @Test
    public void testRemoveAllPurposeMappingsForApplication_success() throws Exception {

        Resource r1 = new Resource(PURPOSE_ID, RESOURCE_TYPE);
        r1.setAttributes(Collections.singletonList(new Attribute(APP_ID, null)));
        Resources resources = new Resources();
        resources.setResources(Collections.singletonList(r1));
        when(configurationManager.getResourcesByType(RESOURCE_TYPE)).thenReturn(resources);

        service.removeAllPurposeMappingsForApplication(APP_ID);

        verify(configurationManager).deleteAttribute(RESOURCE_TYPE, PURPOSE_ID, APP_ID);
    }

    @Test
    public void testRemoveAllPurposeMappingsForApplication_noPurposes() throws Exception {

        when(configurationManager.getResourcesByType(RESOURCE_TYPE)).thenReturn(null);

        service.removeAllPurposeMappingsForApplication(APP_ID);

        verify(configurationManager, never()).deleteAttribute(anyString(), anyString(), anyString());
    }

    @Test
    public void testRemoveAllPurposeMappingsForApplication_resourceNotFoundIgnored() throws Exception {

        Resource r1 = new Resource(PURPOSE_ID, RESOURCE_TYPE);
        r1.setAttributes(Collections.singletonList(new Attribute(APP_ID, null)));
        Resources resources = new Resources();
        resources.setResources(Collections.singletonList(r1));
        when(configurationManager.getResourcesByType(RESOURCE_TYPE)).thenReturn(resources);
        ConfigurationManagementClientException notFound = new ConfigurationManagementClientException(
                "Not found", ErrorMessages.ERROR_CODE_RESOURCE_DOES_NOT_EXISTS.getCode());
        doThrow(notFound).when(configurationManager).deleteAttribute(RESOURCE_TYPE, PURPOSE_ID, APP_ID);

        service.removeAllPurposeMappingsForApplication(APP_ID);
    }

    @Test(expectedExceptions = ConsentAppMappingException.class)
    public void testRemoveAllPurposeMappingsForApplication_otherClientException() throws Exception {

        Resource r1 = new Resource(PURPOSE_ID, RESOURCE_TYPE);
        r1.setAttributes(Collections.singletonList(new Attribute(APP_ID, null)));
        Resources resources = new Resources();
        resources.setResources(Collections.singletonList(r1));
        when(configurationManager.getResourcesByType(RESOURCE_TYPE)).thenReturn(resources);
        ConfigurationManagementClientException ex = new ConfigurationManagementClientException(
                "Other error", "CONFIGM_99999");
        doThrow(ex).when(configurationManager).deleteAttribute(RESOURCE_TYPE, PURPOSE_ID, APP_ID);

        service.removeAllPurposeMappingsForApplication(APP_ID);
    }

    @Test(expectedExceptions = ConsentAppMappingException.class)
    public void testRemoveAllPurposeMappingsForApplication_serverException() throws Exception {

        Resource r1 = new Resource(PURPOSE_ID, RESOURCE_TYPE);
        r1.setAttributes(Collections.singletonList(new Attribute(APP_ID, null)));
        Resources resources = new Resources();
        resources.setResources(Collections.singletonList(r1));
        when(configurationManager.getResourcesByType(RESOURCE_TYPE)).thenReturn(resources);
        doThrow(serverException()).when(configurationManager).deleteAttribute(RESOURCE_TYPE, PURPOSE_ID, APP_ID);

        service.removeAllPurposeMappingsForApplication(APP_ID);
    }

    @Test
    public void testRemoveAllApplicationMappingsForPurpose_success() throws Exception {

        service.removeAllApplicationMappingsForPurpose(PURPOSE_ID);

        verify(configurationManager).deleteResource(RESOURCE_TYPE, PURPOSE_ID);
    }

    @Test
    public void testRemoveAllApplicationMappingsForPurpose_resourceNotFoundIgnored() throws Exception {

        ConfigurationManagementClientException notFound = new ConfigurationManagementClientException(
                "Not found", ErrorMessages.ERROR_CODE_RESOURCE_DOES_NOT_EXISTS.getCode());
        doThrow(notFound).when(configurationManager).deleteResource(RESOURCE_TYPE, PURPOSE_ID);

        service.removeAllApplicationMappingsForPurpose(PURPOSE_ID);
    }

    @Test(expectedExceptions = ConsentAppMappingException.class)
    public void testRemoveAllApplicationMappingsForPurpose_otherClientException() throws Exception {

        ConfigurationManagementClientException ex = new ConfigurationManagementClientException(
                "Other error", "CONFIGM_99999");
        doThrow(ex).when(configurationManager).deleteResource(RESOURCE_TYPE, PURPOSE_ID);

        service.removeAllApplicationMappingsForPurpose(PURPOSE_ID);
    }

    @Test(expectedExceptions = ConsentAppMappingException.class)
    public void testRemoveAllApplicationMappingsForPurpose_serverException() throws Exception {

        doThrow(serverException()).when(configurationManager).deleteResource(RESOURCE_TYPE, PURPOSE_ID);

        service.removeAllApplicationMappingsForPurpose(PURPOSE_ID);
    }
}
