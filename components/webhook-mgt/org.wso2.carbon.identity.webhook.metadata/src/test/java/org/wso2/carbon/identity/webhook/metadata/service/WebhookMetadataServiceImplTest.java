/*
 * Copyright (c) 2025, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.carbon.identity.webhook.metadata.service;

import org.mockito.MockedStatic;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.common.testng.WithCarbonHome;
import org.wso2.carbon.identity.common.testng.WithRealmService;
import org.wso2.carbon.identity.core.internal.component.IdentityCoreServiceDataHolder;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;
import org.wso2.carbon.identity.organization.resource.sharing.policy.management.constant.PolicyEnum;
import org.wso2.carbon.identity.webhook.metadata.api.exception.WebhookMetadataException;
import org.wso2.carbon.identity.webhook.metadata.api.model.EventProfile;
import org.wso2.carbon.identity.webhook.metadata.api.model.OrganizationPolicy;
import org.wso2.carbon.identity.webhook.metadata.api.model.WebhookMetadataProperties;
import org.wso2.carbon.identity.webhook.metadata.internal.constant.WebhookMetadataConstants;
import org.wso2.carbon.identity.webhook.metadata.internal.dao.WebhookMetadataDAO;
import org.wso2.carbon.identity.webhook.metadata.internal.dao.impl.FileBasedEventProfileMetadataDAOImpl;
import org.wso2.carbon.identity.webhook.metadata.internal.model.WebhookMetadataProperty;
import org.wso2.carbon.identity.webhook.metadata.internal.service.impl.WebhookMetadataServiceImpl;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

/**
 * Unit tests for WebhookMetadataServiceImpl.
 */
@WithCarbonHome
@WithRealmService(injectToSingletons = {IdentityCoreServiceDataHolder.class})
public class WebhookMetadataServiceImplTest {

    private static final String TENANT_DOMAIN = "test.com";
    private static final int TENANT_ID = 1;

    private WebhookMetadataServiceImpl service;
    private WebhookMetadataDAO webhookMetadataDAO;
    private MockedStatic<IdentityTenantUtil> identityTenantUtilMockedStatic;

    @BeforeClass
    public void setUpClass() throws Exception {

        identityTenantUtilMockedStatic = mockStatic(IdentityTenantUtil.class);
        identityTenantUtilMockedStatic.when(() -> IdentityTenantUtil.getTenantId(TENANT_DOMAIN)).thenReturn(TENANT_ID);

        service = WebhookMetadataServiceImpl.getInstance();
        webhookMetadataDAO = mock(WebhookMetadataDAO.class);

        Field daoField = WebhookMetadataServiceImpl.class.getDeclaredField("webhookMetadataDAO");
        daoField.setAccessible(true);
        daoField.set(service, webhookMetadataDAO);
    }

    @AfterClass
    public void tearDownClass() {

        identityTenantUtilMockedStatic.close();
    }

    @BeforeMethod
    public void setUp() {
        // No-op for now
    }

    @Test
    public void testInit() throws Exception {

        FileBasedEventProfileMetadataDAOImpl eventProfileDAO = mock(FileBasedEventProfileMetadataDAOImpl.class);
        Field eventProfileDaoField = WebhookMetadataServiceImpl.class.getDeclaredField("eventProfileMetadataDAO");
        eventProfileDaoField.setAccessible(true);
        eventProfileDaoField.set(service, eventProfileDAO);

        doNothing().when(eventProfileDAO).init();
        service.init();
        verify(eventProfileDAO, times(1)).init();
    }

    @Test
    public void testGetSupportedEventProfiles_Success() throws Exception {

        FileBasedEventProfileMetadataDAOImpl eventProfileDAO = mock(FileBasedEventProfileMetadataDAOImpl.class);
        Field eventProfileDaoField = WebhookMetadataServiceImpl.class.getDeclaredField("eventProfileMetadataDAO");
        eventProfileDaoField.setAccessible(true);
        eventProfileDaoField.set(service, eventProfileDAO);

        EventProfile profile = mock(EventProfile.class);
        when(eventProfileDAO.getSupportedEventProfiles()).thenReturn(Collections.singletonList(profile));
        assertEquals(service.getSupportedEventProfiles().size(), 1);
    }

    @Test(expectedExceptions = WebhookMetadataException.class)
    public void testGetSupportedEventProfiles_Exception() throws Exception {

        FileBasedEventProfileMetadataDAOImpl eventProfileDAO = mock(FileBasedEventProfileMetadataDAOImpl.class);
        Field eventProfileDaoField = WebhookMetadataServiceImpl.class.getDeclaredField("eventProfileMetadataDAO");
        eventProfileDaoField.setAccessible(true);
        eventProfileDaoField.set(service, eventProfileDAO);

        when(eventProfileDAO.getSupportedEventProfiles()).thenThrow(new RuntimeException("error"));
        service.getSupportedEventProfiles();
    }

    @Test
    public void testGetEventProfile_Success() throws Exception {

        FileBasedEventProfileMetadataDAOImpl eventProfileDAO = mock(FileBasedEventProfileMetadataDAOImpl.class);
        Field eventProfileDaoField = WebhookMetadataServiceImpl.class.getDeclaredField("eventProfileMetadataDAO");
        eventProfileDaoField.setAccessible(true);
        eventProfileDaoField.set(service, eventProfileDAO);

        EventProfile profile = mock(EventProfile.class);
        when(eventProfileDAO.getEventProfile("profile")).thenReturn(profile);
        assertEquals(service.getEventProfile("profile"), profile);
    }

    @Test(expectedExceptions = WebhookMetadataException.class)
    public void testGetEventProfile_Exception() throws Exception {

        FileBasedEventProfileMetadataDAOImpl eventProfileDAO = mock(FileBasedEventProfileMetadataDAOImpl.class);
        Field eventProfileDaoField = WebhookMetadataServiceImpl.class.getDeclaredField("eventProfileMetadataDAO");
        eventProfileDaoField.setAccessible(true);
        eventProfileDaoField.set(service, eventProfileDAO);

        when(eventProfileDAO.getEventProfile("profile")).thenThrow(new RuntimeException("error"));
        service.getEventProfile("profile");
    }

    @Test
    public void testGetWebhookMetadataProperties_NoPolicySet() throws Exception {

        when(webhookMetadataDAO.getWebhookMetadataProperties(TENANT_ID)).thenReturn(Collections.emptyMap());

        WebhookMetadataProperties props = service.getWebhookMetadataProperties(TENANT_DOMAIN);

        assertNotNull(props);
        assertEquals(props.getOrganizationPolicy().getPolicyCode(), PolicyEnum.NO_SHARING.getPolicyCode());
    }

    @Test
    public void testGetWebhookMetadataProperties_WithPolicySet() throws Exception {

        Map<String, WebhookMetadataProperty> propertyMap = new HashMap<>();
        propertyMap.put(WebhookMetadataConstants.MetadataPropertyFields.ORGANIZATION_POLICY_PROPERTY_NAME,
                new WebhookMetadataProperty.Builder(PolicyEnum.NO_SHARING.getPolicyCode()).build());
        when(webhookMetadataDAO.getWebhookMetadataProperties(TENANT_ID)).thenReturn(propertyMap);

        WebhookMetadataProperties props = service.getWebhookMetadataProperties(TENANT_DOMAIN);

        assertNotNull(props);
        assertEquals(props.getOrganizationPolicy().getPolicyCode(), PolicyEnum.NO_SHARING.getPolicyCode());
    }

    @Test
    public void testUpdateWebhookMetadataProperties_AddNew() throws Exception {

        OrganizationPolicy policy = new OrganizationPolicy(PolicyEnum.NO_SHARING);
        WebhookMetadataProperties inputProps = new WebhookMetadataProperties.Builder()
                .organizationPolicy(policy)
                .build();

        when(webhookMetadataDAO.getWebhookMetadataProperties(TENANT_ID)).thenReturn(Collections.emptyMap());
        doNothing().when(webhookMetadataDAO).addWebhookMetadataProperties(any(), eq(TENANT_ID));
        // After add, return the new property
        Map<String, WebhookMetadataProperty> propertyMap = new HashMap<>();
        propertyMap.put(WebhookMetadataConstants.MetadataPropertyFields.ORGANIZATION_POLICY_PROPERTY_NAME,
                new WebhookMetadataProperty.Builder(PolicyEnum.NO_SHARING.getPolicyCode()).build());
        when(webhookMetadataDAO.getWebhookMetadataProperties(TENANT_ID)).thenReturn(propertyMap);

        WebhookMetadataProperties result = service.updateWebhookMetadataProperties(inputProps, TENANT_DOMAIN);

        assertNotNull(result);
        assertEquals(result.getOrganizationPolicy().getPolicyCode(), PolicyEnum.NO_SHARING.getPolicyCode());
    }

    @Test
    public void testUpdateWebhookMetadataProperties_UpdateExisting() throws Exception {

        Map<String, WebhookMetadataProperty> existingMap = new HashMap<>();
        existingMap.put(WebhookMetadataConstants.MetadataPropertyFields.ORGANIZATION_POLICY_PROPERTY_NAME,
                new WebhookMetadataProperty.Builder(PolicyEnum.NO_SHARING.getPolicyCode()).build());
        when(webhookMetadataDAO.getWebhookMetadataProperties(TENANT_ID)).thenReturn(existingMap);

        OrganizationPolicy policy = new OrganizationPolicy(PolicyEnum.NO_SHARING);
        WebhookMetadataProperties inputProps = new WebhookMetadataProperties.Builder()
                .organizationPolicy(policy)
                .build();

        doNothing().when(webhookMetadataDAO).updateWebhookMetadataProperties(any(), eq(TENANT_ID));
        // After update, return the new property
        Map<String, WebhookMetadataProperty> propertyMap = new HashMap<>();
        propertyMap.put(WebhookMetadataConstants.MetadataPropertyFields.ORGANIZATION_POLICY_PROPERTY_NAME,
                new WebhookMetadataProperty.Builder(PolicyEnum.NO_SHARING.getPolicyCode()).build());
        when(webhookMetadataDAO.getWebhookMetadataProperties(TENANT_ID)).thenReturn(propertyMap);

        WebhookMetadataProperties result = service.updateWebhookMetadataProperties(inputProps, TENANT_DOMAIN);

        assertNotNull(result);
        assertEquals(result.getOrganizationPolicy().getPolicyCode(), PolicyEnum.NO_SHARING.getPolicyCode());
    }
}
