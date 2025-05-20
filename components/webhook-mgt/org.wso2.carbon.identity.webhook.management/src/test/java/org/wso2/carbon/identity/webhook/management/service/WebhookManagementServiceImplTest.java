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
import org.wso2.carbon.identity.webhook.management.api.model.WebhookDTO;
import org.wso2.carbon.identity.webhook.management.api.model.WebhookSummaryDTO;
import org.wso2.carbon.identity.webhook.management.internal.dao.WebhookManagementDAO;
import org.wso2.carbon.identity.webhook.management.internal.service.impl.WebhookManagementServiceImpl;

import java.lang.reflect.Field;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

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

    public static final String WEBHOOK_ID = "webhookId";
    public static final String TENANT_DOMAIN = "test.com";
    public static final int TENANT_ID = 1;
    public static final String WEBHOOK_ENDPOINT = "https://example.com/webhook";

    @BeforeClass
    public void setUpClass() {

        webhookManagementService = WebhookManagementServiceImpl.getInstance();

        identityTenantUtilMockedStatic = mockStatic(IdentityTenantUtil.class);
        when(IdentityTenantUtil.getTenantId(TENANT_DOMAIN)).thenReturn(TENANT_ID);
    }

    @AfterClass
    public void tearDownClass() {

        identityTenantUtilMockedStatic.close();
    }

    @BeforeMethod
    public void setUp() throws Exception {

        webhookManagementDAO = mock(WebhookManagementDAO.class);
        Field daoField = WebhookManagementServiceImpl.class.getDeclaredField("daoFACADE");
        daoField.setAccessible(true);
        daoField.set(webhookManagementService, webhookManagementDAO);
    }

    @Test
    public void testAddWebhook() throws WebhookMgtException {

        Webhook webhook = mock(Webhook.class);
        when(webhook.getId()).thenReturn(WEBHOOK_ID);

        when(webhookManagementDAO.getWebhook(WEBHOOK_ID, TENANT_ID)).thenReturn(webhook);

        WebhookDTO result = webhookManagementService.createWebhook(webhook, TENANT_DOMAIN);

        verify(webhookManagementDAO).createWebhook(webhook, TENANT_ID);
        verify(webhookManagementDAO).getWebhook(WEBHOOK_ID, TENANT_ID);

        WebhookDTO expectedWebhookDTO = WebhookDTO.Builder.fromWebhook(webhook).build();

        assertEquals(result, expectedWebhookDTO);
    }

    @Test(expectedExceptions = WebhookMgtClientException.class, expectedExceptionsMessageRegExp =
            "Webhook already exists")
    public void testAddWebhookWithExistingEndpoint() throws WebhookMgtException {

        Webhook webhook = mock(Webhook.class);
        when(webhook.getEndpoint()).thenReturn(WEBHOOK_ENDPOINT);

        when(webhookManagementDAO.isWebhookEndpointExists(WEBHOOK_ENDPOINT, TENANT_ID)).thenReturn(true);

        webhookManagementService.createWebhook(webhook, TENANT_DOMAIN);
    }

    @Test
    public void testUpdateWebhook() throws WebhookMgtException {

        Webhook webhook = mock(Webhook.class);
        when(webhook.getUuid()).thenReturn(WEBHOOK_ID);
        when(webhook.getEndpoint()).thenReturn(WEBHOOK_ENDPOINT);
        when(webhookManagementDAO.getWebhook(WEBHOOK_ID, TENANT_ID)).thenReturn(webhook);

        WebhookDTO result = webhookManagementService.updateWebhook(webhook, TENANT_DOMAIN);

        WebhookDTO expectedWebhookDTO = new WebhookDTO.Builder()
                .setId(webhook.getId())
                .setCreatedAt(webhook.getCreatedAt() != null ?
                        webhook.getCreatedAt().toInstant().atZone(ZoneId.of("UTC"))
                                .format(DateTimeFormatter.ISO_INSTANT) : null)
                .setUpdatedAt(webhook.getUpdatedAt() != null ?
                        webhook.getUpdatedAt().toInstant().atZone(ZoneId.of("UTC"))
                                .format(DateTimeFormatter.ISO_INSTANT) : null)
                .setEndpoint(webhook.getEndpoint())
                .setEventSchemaName(webhook.getEventSchemaName())
                .setEventSchemaUri(webhook.getEventSchemaUri())
                .setDescription(webhook.getDescription())
                .setEventsSubscribed(webhook.getEventsSubscribed())
                .setStatus(webhook.getStatus())
                .build();

        verify(webhookManagementDAO).updateWebhook(webhook, TENANT_ID);
        verify(webhookManagementDAO, times(3)).getWebhook(WEBHOOK_ID, TENANT_ID);
        assertEquals(result, expectedWebhookDTO);
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

        webhookManagementService.updateWebhook(webhook, TENANT_DOMAIN);
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

        WebhookDTO result = webhookManagementService.getWebhook(WEBHOOK_ID, TENANT_DOMAIN);

        WebhookDTO expectedWebhookDTO = WebhookDTO.Builder.fromWebhook(webhook).build();

        verify(webhookManagementDAO).getWebhook(WEBHOOK_ID, TENANT_ID);
        assertEquals(result, expectedWebhookDTO);
    }

    @Test
    public void testDeactivateWebhook() throws WebhookMgtException {

        Webhook webhook = mock(Webhook.class);
        when(webhookManagementDAO.getWebhook(WEBHOOK_ID, TENANT_ID)).thenReturn(webhook);

        webhookManagementService.deactivateWebhook(WEBHOOK_ID, TENANT_DOMAIN);

        verify(webhookManagementDAO).deactivateWebhook(WEBHOOK_ID, TENANT_ID);
        verify(webhookManagementDAO, times(1)).getWebhook(WEBHOOK_ID, TENANT_ID);
    }

    @Test(expectedExceptions = WebhookMgtClientException.class, expectedExceptionsMessageRegExp =
            "Webhook not found")
    public void testUpdateIfWebhookNotExist() throws WebhookMgtException {

        when(webhookManagementDAO.getWebhook(WEBHOOK_ID, TENANT_ID)).thenReturn(null);

        Webhook webhook = mock(Webhook.class);
        when(webhook.getId()).thenReturn(WEBHOOK_ID);
        webhookManagementService.updateWebhook(webhook, TENANT_DOMAIN);
    }

    @Test
    public void testGetWebhooks() throws WebhookMgtException {

        List<Webhook> webhooks = new ArrayList<>();
        Webhook webhook1 = mock(Webhook.class);
        Webhook webhook2 = mock(Webhook.class);
        webhooks.add(webhook1);
        webhooks.add(webhook2);

        when(webhookManagementDAO.getWebhooks(TENANT_ID)).thenReturn(webhooks);

        List<WebhookSummaryDTO> result = webhookManagementService.getWebhooks(TENANT_DOMAIN);

        verify(webhookManagementDAO).getWebhooks(TENANT_ID);
        assertEquals(result.size(), 2);
        assertEquals(result.get(0), WebhookSummaryDTO.Builder.fromWebhook(webhook1).build());
        assertEquals(result.get(1), WebhookSummaryDTO.Builder.fromWebhook(webhook2).build());
    }

    @Test
    public void testActivateWebhook() throws WebhookMgtException {

        Webhook webhook = mock(Webhook.class);
        when(webhookManagementDAO.getWebhook(WEBHOOK_ID, TENANT_ID)).thenReturn(webhook);

        webhookManagementService.activateWebhook(WEBHOOK_ID, TENANT_DOMAIN);

        verify(webhookManagementDAO).activateWebhook(WEBHOOK_ID, TENANT_ID);
        verify(webhookManagementDAO, times(1)).getWebhook(WEBHOOK_ID, TENANT_ID);
    }

    @Test(expectedExceptions = WebhookMgtClientException.class, expectedExceptionsMessageRegExp =
            "Webhook not found")
    public void testActivateWebhookNotExist() throws WebhookMgtException {

        when(webhookManagementDAO.getWebhook(WEBHOOK_ID, TENANT_ID)).thenReturn(null);

        webhookManagementService.activateWebhook(WEBHOOK_ID, TENANT_DOMAIN);
    }
}
