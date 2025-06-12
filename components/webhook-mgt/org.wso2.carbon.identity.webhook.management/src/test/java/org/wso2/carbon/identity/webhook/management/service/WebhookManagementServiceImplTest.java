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

package org.wso2.carbon.identity.webhook.management.service;

import org.mockito.MockedStatic;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.common.testng.WithCarbonHome;
import org.wso2.carbon.identity.common.testng.WithRealmService;
import org.wso2.carbon.identity.core.internal.IdentityCoreServiceDataHolder;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;
import org.wso2.carbon.identity.webhook.management.api.exception.WebhookMgtClientException;
import org.wso2.carbon.identity.webhook.management.api.exception.WebhookMgtException;
import org.wso2.carbon.identity.webhook.management.api.model.Webhook;
import org.wso2.carbon.identity.webhook.management.api.model.WebhookStatus;
import org.wso2.carbon.identity.webhook.management.internal.component.WebhookManagementComponentServiceHolder;
import org.wso2.carbon.identity.webhook.management.internal.dao.WebhookManagementDAO;
import org.wso2.carbon.identity.webhook.management.internal.service.impl.WebhookManagementServiceImpl;
import org.wso2.carbon.identity.webhook.metadata.api.exception.WebhookMetadataException;
import org.wso2.carbon.identity.webhook.metadata.api.model.Channel;
import org.wso2.carbon.identity.webhook.metadata.api.model.EventProfile;
import org.wso2.carbon.identity.webhook.metadata.api.service.WebhookMetadataService;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

@WithCarbonHome
@WithRealmService(injectToSingletons = {IdentityCoreServiceDataHolder.class})
public class WebhookManagementServiceImplTest {

    private WebhookManagementServiceImpl webhookManagementService;
    private WebhookManagementDAO webhookManagementDAO;

    private MockedStatic<IdentityTenantUtil> identityTenantUtilMockedStatic;
    private MockedStatic<WebhookManagementComponentServiceHolder> webhookComponentHolderMockedStatic;
    private WebhookMetadataService webhookMetadataService;

    public static final String WEBHOOK_ID = "webhookId";
    public static final String TENANT_DOMAIN = "test.com";
    public static final int TENANT_ID = 1;
    public static final String WEBHOOK_ENDPOINT = "https://example.com/webhook";

    @BeforeClass
    public void setUpClass() throws WebhookMetadataException {

        webhookManagementService = WebhookManagementServiceImpl.getInstance();
        identityTenantUtilMockedStatic = mockStatic(IdentityTenantUtil.class);
        when(IdentityTenantUtil.getTenantId(TENANT_DOMAIN)).thenReturn(TENANT_ID);

        // Mock WebhookManagementComponentServiceHolder and WebhookMetadataService
        webhookComponentHolderMockedStatic = mockStatic(WebhookManagementComponentServiceHolder.class);
        WebhookManagementComponentServiceHolder holder = mock(WebhookManagementComponentServiceHolder.class);
        webhookMetadataService = mock(WebhookMetadataService.class);

        webhookComponentHolderMockedStatic.when(WebhookManagementComponentServiceHolder::getInstance)
                .thenReturn(holder);
        when(holder.getWebhookMetadataService()).thenReturn(webhookMetadataService);

        // Setup a valid EventProfile and Channel for validation
        Channel channel = new Channel("logins", "Logins Channel", "schemas.identity.wso2.org/events/logins",
                Collections.emptyList());
        EventProfile eventProfile = new EventProfile("schema", "uri", Collections.singletonList(channel));
        when(webhookMetadataService.getSupportedEventProfiles())
                .thenReturn(Collections.singletonList(eventProfile));
    }

    @AfterClass
    public void tearDownClass() {

        identityTenantUtilMockedStatic.close();
        webhookComponentHolderMockedStatic.close();
    }

    @BeforeMethod
    public void setUp() throws Exception {

        webhookManagementDAO = mock(WebhookManagementDAO.class);
        Field daoField = WebhookManagementServiceImpl.class.getDeclaredField("daoFACADE");
        daoField.setAccessible(true);
        daoField.set(webhookManagementService, webhookManagementDAO);
    }

    @Test
    public void testCreateWebhook() throws Exception {

        Webhook inputWebhook = mock(Webhook.class);
        when(inputWebhook.getEndpoint()).thenReturn("https://test.com/webhook");
        when(inputWebhook.getStatus()).thenReturn(null);
        when(inputWebhook.getName()).thenReturn("name");
        when(inputWebhook.getSecret()).thenReturn("aBcD1234_efGh5678~IjKl9012+MnOpQR");
        when(inputWebhook.getEventSchemaName()).thenReturn("schema");
        when(inputWebhook.getEventSchemaUri()).thenReturn("uri");
        when(inputWebhook.getCreatedAt()).thenReturn(null);
        when(inputWebhook.getUpdatedAt()).thenReturn(null);
        when(inputWebhook.getEventsSubscribed()).thenReturn(
                Collections.singletonList("schemas.identity.wso2.org/events/logins"));

        // Endpoint does not exist
        when(webhookManagementDAO.isWebhookEndpointExists("https://test.com/webhook", 1)).thenReturn(false);

        // Simulate returned webhook after creation
        Webhook createdWebhook = mock(Webhook.class);
        when(webhookManagementDAO.getWebhook(anyString(), eq(1))).thenReturn(createdWebhook);

        WebhookManagementServiceImpl service = WebhookManagementServiceImpl.getInstance();

        // Inject mock DAO
        Field daoField = WebhookManagementServiceImpl.class.getDeclaredField("daoFACADE");
        daoField.setAccessible(true);
        daoField.set(service, webhookManagementDAO);

        Webhook result = service.createWebhook(inputWebhook, "test.com");

        verify(webhookManagementDAO).createWebhook(any(Webhook.class), eq(1));
        verify(webhookManagementDAO).getWebhook(anyString(), eq(1));
        assertEquals(result, createdWebhook);
    }

    @Test(expectedExceptions = WebhookMgtClientException.class, expectedExceptionsMessageRegExp =
            "Webhook endpoint already exists")
    public void testAddWebhookWithExistingEndpoint() throws WebhookMgtException {

        Webhook webhook = mock(Webhook.class);
        when(webhook.getEndpoint()).thenReturn(WEBHOOK_ENDPOINT);

        when(webhookManagementDAO.isWebhookEndpointExists(WEBHOOK_ENDPOINT, TENANT_ID)).thenReturn(true);

        webhookManagementService.createWebhook(webhook, TENANT_DOMAIN);
    }

    @Test
    public void testUpdateWebhook() throws Exception {

        // Setup a valid EventProfile and Channel for validation
        Channel channel = new Channel("logins", "Logins Channel", "schemas.identity.wso2.org/events/logins",
                Collections.emptyList());
        EventProfile eventProfile =
                new EventProfile("updated-schema", "updated-uri", Collections.singletonList(channel));
        when(webhookMetadataService.getSupportedEventProfiles())
                .thenReturn(Collections.singletonList(eventProfile));

        String webhookId = "webhook-123";
        String tenantDomain = "test.com";
        int tenantId = 1;

        // Mock input and existing webhooks
        Webhook updateWebhook = mock(Webhook.class);
        Webhook existingWebhook = mock(Webhook.class);
        Webhook resultWebhook = mock(Webhook.class);

        // Configure complete data for updateWebhook
        when(updateWebhook.getEndpoint()).thenReturn("https://test.com/webhook");
        when(updateWebhook.getName()).thenReturn("updated-name");
        when(updateWebhook.getUuid()).thenReturn(webhookId);
        when(updateWebhook.getEventSchemaName()).thenReturn("updated-schema");
        when(updateWebhook.getEventSchemaUri()).thenReturn("updated-uri");
        when(updateWebhook.getStatus()).thenReturn(WebhookStatus.ACTIVE);
        when(updateWebhook.getSecret()).thenReturn("updated-test12-secret-1234567890");

        // Make sure eventsSubscribed isn't null
        List<String> events = new ArrayList<>();
        events.add("schemas.identity.wso2.org/events/logins");
        when(updateWebhook.getEventsSubscribed()).thenReturn(events);

        // Configure existing webhook
        when(existingWebhook.getEndpoint()).thenReturn("https://test.com/webhook");
        when(existingWebhook.getStatus()).thenReturn(WebhookStatus.ACTIVE);
        when(existingWebhook.getName()).thenReturn("name");
        when(existingWebhook.getSecret()).thenReturn("aBcD1234_efGh5678~IjKl9012+MnOpQR");
        when(existingWebhook.getEventSchemaName()).thenReturn("schema");
        when(existingWebhook.getEventSchemaUri()).thenReturn("uri");
        when(existingWebhook.getUuid()).thenReturn(webhookId);
        when(existingWebhook.getEventsSubscribed()).thenReturn(events);

        // Set up result webhook
        when(resultWebhook.getUuid()).thenReturn(webhookId);

        // Simulate DAO behavior
        when(webhookManagementDAO.getWebhook(webhookId, tenantId))
                .thenReturn(existingWebhook) // First call during validation
                .thenReturn(existingWebhook) // Second call
                .thenReturn(resultWebhook);  // Third call when returning the updated webhook

        // Mock endpoint check
        when(webhookManagementDAO.isWebhookEndpointExists("https://test.com/webhook", tenantId)).thenReturn(false);

        Webhook result = webhookManagementService.updateWebhook(webhookId, updateWebhook, tenantDomain);

        verify(webhookManagementDAO).updateWebhook(any(Webhook.class), eq(tenantId));
        verify(webhookManagementDAO, times(3)).getWebhook(webhookId, tenantId);

        assertEquals(result, resultWebhook);
    }

    @Test(expectedExceptions = WebhookMgtClientException.class, expectedExceptionsMessageRegExp =
            "Webhook endpoint already exists")
    public void testUpdateWebhookWithExistingEndpoint() throws WebhookMgtException {

        // Mock the existing webhook with original endpoint
        Webhook existingWebhook = mock(Webhook.class);
        String originalEndpoint = "https://original-endpoint.com";
        when(existingWebhook.getEndpoint()).thenReturn(originalEndpoint);

        // Mock the webhook to update with new endpoint
        Webhook webhook = mock(Webhook.class);
        when(webhook.getUuid()).thenReturn(WEBHOOK_ID);
        when(webhook.getEndpoint()).thenReturn(WEBHOOK_ENDPOINT);

        // Return existingWebhook first time getWebhook is called
        when(webhookManagementDAO.getWebhook(WEBHOOK_ID, TENANT_ID)).thenReturn(existingWebhook);

        // Mock that the new endpoint already exists in another webhook
        when(webhookManagementDAO.isWebhookEndpointExists(WEBHOOK_ENDPOINT, TENANT_ID)).thenReturn(true);

        webhookManagementService.updateWebhook(webhook.getUuid(), webhook, TENANT_DOMAIN);
    }

    @Test
    public void testDeleteWebhook() throws WebhookMgtException {

        Webhook webhook = mock(Webhook.class);
        when(webhookManagementDAO.getWebhook(WEBHOOK_ID, TENANT_ID)).thenReturn(webhook);

        webhookManagementService.deleteWebhook(WEBHOOK_ID, TENANT_DOMAIN);

        verify(webhookManagementDAO).deleteWebhook(WEBHOOK_ID, TENANT_ID);
    }

    @Test
    public void testGetWebhookByWebhookId() throws WebhookMgtException {

        Webhook webhook = mock(Webhook.class);
        when(webhookManagementDAO.getWebhook(WEBHOOK_ID, TENANT_ID)).thenReturn(webhook);

        Webhook result = webhookManagementService.getWebhook(WEBHOOK_ID, TENANT_DOMAIN);

        verify(webhookManagementDAO).getWebhook(WEBHOOK_ID, TENANT_ID);
        assertEquals(result, webhook);
    }

    @Test
    public void testDeactivateWebhook() throws WebhookMgtException {

        Webhook webhook = mock(Webhook.class);
        when(webhookManagementDAO.getWebhook(WEBHOOK_ID, TENANT_ID)).thenReturn(webhook);

        webhookManagementService.deactivateWebhook(WEBHOOK_ID, TENANT_DOMAIN);

        verify(webhookManagementDAO).deactivateWebhook(WEBHOOK_ID, TENANT_ID);
        verify(webhookManagementDAO, times(2)).getWebhook(WEBHOOK_ID, TENANT_ID);
    }

    @Test(expectedExceptions = WebhookMgtClientException.class, expectedExceptionsMessageRegExp =
            "Webhook not found")
    public void testUpdateIfWebhookNotExist() throws WebhookMgtException {

        when(webhookManagementDAO.getWebhook(WEBHOOK_ID, TENANT_ID)).thenReturn(null);

        Webhook webhook = mock(Webhook.class);
        when(webhook.getUuid()).thenReturn(WEBHOOK_ID);
        webhookManagementService.updateWebhook(webhook.getUuid(), webhook, TENANT_DOMAIN);
    }

    @Test
    public void testGetWebhooks() throws WebhookMgtException {

        List<Webhook> webhooks = new ArrayList<>();
        Webhook webhook1 = mock(Webhook.class);
        Webhook webhook2 = mock(Webhook.class);
        webhooks.add(webhook1);
        webhooks.add(webhook2);

        when(webhookManagementDAO.getWebhooks(TENANT_ID)).thenReturn(webhooks);

        List<Webhook> result = webhookManagementService.getWebhooks(TENANT_DOMAIN);

        verify(webhookManagementDAO).getWebhooks(TENANT_ID);
        assertEquals(result.size(), 2);
        assertEquals(result.get(0), webhook1);
        assertEquals(result.get(1), webhook2);
    }

    @Test
    public void testActivateWebhook() throws WebhookMgtException {

        Webhook webhook = mock(Webhook.class);
        when(webhookManagementDAO.getWebhook(WEBHOOK_ID, TENANT_ID)).thenReturn(webhook);

        webhookManagementService.activateWebhook(WEBHOOK_ID, TENANT_DOMAIN);

        verify(webhookManagementDAO).activateWebhook(WEBHOOK_ID, TENANT_ID);
        verify(webhookManagementDAO, times(2)).getWebhook(WEBHOOK_ID, TENANT_ID);
    }

    @Test(expectedExceptions = WebhookMgtClientException.class, expectedExceptionsMessageRegExp =
            "Webhook not found")
    public void testActivateWebhookNotExist() throws WebhookMgtException {

        when(webhookManagementDAO.getWebhook(WEBHOOK_ID, TENANT_ID)).thenReturn(null);

        webhookManagementService.activateWebhook(WEBHOOK_ID, TENANT_DOMAIN);
    }

    @Test
    public void testGetWebhookEvents() throws WebhookMgtException {

        List<String> events = new ArrayList<>();
        events.add("event1");
        events.add("event2");

        when(webhookManagementDAO.getWebhook(WEBHOOK_ID, TENANT_ID)).thenReturn(mock(Webhook.class));
        when(webhookManagementDAO.getWebhookEvents(WEBHOOK_ID, TENANT_ID)).thenReturn(events);

        List<String> result = webhookManagementService.getWebhookEvents(WEBHOOK_ID, TENANT_DOMAIN);

        verify(webhookManagementDAO).getWebhook(WEBHOOK_ID, TENANT_ID);
        verify(webhookManagementDAO).getWebhookEvents(WEBHOOK_ID, TENANT_ID);
        assertEquals(result, events);
    }
}
