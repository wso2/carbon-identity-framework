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

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.webhook.management.api.exception.WebhookMgtClientException;
import org.wso2.carbon.identity.webhook.management.api.exception.WebhookMgtException;
import org.wso2.carbon.identity.webhook.management.api.model.Webhook;
import org.wso2.carbon.identity.webhook.management.api.model.WebhookDTO;
import org.wso2.carbon.identity.webhook.management.api.model.WebhookStatus;
import org.wso2.carbon.identity.webhook.management.internal.dao.impl.WebhookManagementDAOFacade;
import org.wso2.carbon.identity.webhook.management.internal.service.impl.WebhookManagementServiceImpl;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class WebhookManagementServiceImplTest {

    @Mock
    private WebhookManagementDAOFacade daoFacade;

    @InjectMocks
    private WebhookManagementServiceImpl webhookManagementService;

    private static final String TENANT_DOMAIN = "carbon.super";
    private static final String WEBHOOK_ID = "webhook-123";
    private static final String ENDPOINT = "https://example.com/webhook";

    private Webhook mockedWebhook;

    @BeforeClass
    public void setUp() throws Exception {

        MockitoAnnotations.initMocks(this);

        // Create a mocked webhook
        mockedWebhook = new Webhook();
        mockedWebhook.setId(WEBHOOK_ID);
        mockedWebhook.setEndpoint(ENDPOINT);
        mockedWebhook.setDescription("Test Webhook");
        mockedWebhook.setStatus(WebhookStatus.ACTIVE);
        mockedWebhook.setSecret("secret123");
        mockedWebhook.setEventsSubscribed(Arrays.asList("user.created", "user.updated"));
        mockedWebhook.setCreatedAt(Timestamp.from(Instant.now()));
        mockedWebhook.setUpdatedAt(Timestamp.from(Instant.now()));

        // Set the private static DAO_FACADE field in WebhookManagementServiceImpl
        setFinalStatic(WebhookManagementServiceImpl.class.getDeclaredField("DAO_FACADE"), daoFacade);
    }

    @BeforeMethod
    public void resetMocks() {

        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testCreateWebhook() throws WebhookMgtException {
        // Prepare
        when(daoFacade.isWebhookEndpointExists(anyString(), anyInt())).thenReturn(false);
        when(daoFacade.getWebhook(anyString(), anyInt())).thenReturn(mockedWebhook);
        doNothing().when(daoFacade).createWebhook(any(Webhook.class), anyInt());

        // Execute
        WebhookDTO result = webhookManagementService.createWebhook(mockedWebhook, TENANT_DOMAIN);

        // Verify
        Assert.assertNotNull(result);
        Assert.assertEquals(result.getId(), mockedWebhook.getId());
        Assert.assertEquals(result.getEndpoint(), mockedWebhook.getEndpoint());
        Assert.assertEquals(result.getDescription(), mockedWebhook.getDescription());
        Assert.assertEquals(result.getStatus(), mockedWebhook.getStatus());
        verify(daoFacade, times(1)).createWebhook(any(Webhook.class), anyInt());
        verify(daoFacade, times(1)).getWebhook(anyString(), anyInt());
    }

    @Test(expectedExceptions = WebhookMgtClientException.class)
    public void testCreateWebhookEndpointAlreadyExists() throws WebhookMgtException {
        // Prepare
        when(daoFacade.isWebhookEndpointExists(anyString(), anyInt())).thenReturn(true);

        // Execute - should throw exception
        webhookManagementService.createWebhook(mockedWebhook, TENANT_DOMAIN);
    }

    @Test
    public void testGetWebhook() throws WebhookMgtException {
        // Prepare
        when(daoFacade.getWebhook(eq(WEBHOOK_ID), anyInt())).thenReturn(mockedWebhook);

        // Execute
        WebhookDTO result = webhookManagementService.getWebhook(WEBHOOK_ID, TENANT_DOMAIN);

        // Verify
        Assert.assertNotNull(result);
        Assert.assertEquals(result.getId(), mockedWebhook.getId());
        Assert.assertEquals(result.getEndpoint(), mockedWebhook.getEndpoint());
        verify(daoFacade, times(1)).getWebhook(eq(WEBHOOK_ID), anyInt());
    }

    @Test
    public void testUpdateWebhook() throws WebhookMgtException {
        // Prepare
        when(daoFacade.getWebhook(eq(WEBHOOK_ID), anyInt())).thenReturn(mockedWebhook);
        doNothing().when(daoFacade).updateWebhook(anyString(), any(Webhook.class), anyInt());

        Webhook updatedWebhook = new Webhook();
        updatedWebhook.setId(WEBHOOK_ID);
        updatedWebhook.setEndpoint(ENDPOINT);
        updatedWebhook.setDescription("Updated Description");

        // Execute
        WebhookDTO result = webhookManagementService.updateWebhook(WEBHOOK_ID, updatedWebhook, TENANT_DOMAIN);

        // Verify
        Assert.assertNotNull(result);
        verify(daoFacade, times(1)).updateWebhook(eq(WEBHOOK_ID), any(Webhook.class), anyInt());
        verify(daoFacade, times(2)).getWebhook(eq(WEBHOOK_ID), anyInt());
    }

    @Test(expectedExceptions = WebhookMgtClientException.class)
    public void testUpdateNonExistentWebhook() throws WebhookMgtException {
        // Prepare
        when(daoFacade.getWebhook(eq(WEBHOOK_ID), anyInt())).thenReturn(null);

        // Execute - should throw exception
        webhookManagementService.updateWebhook(WEBHOOK_ID, mockedWebhook, TENANT_DOMAIN);
    }

    @Test
    public void testDeleteWebhook() throws WebhookMgtException {
        // Prepare
        when(daoFacade.getWebhook(eq(WEBHOOK_ID), anyInt())).thenReturn(mockedWebhook);
        doNothing().when(daoFacade).deleteWebhook(anyString(), anyInt());

        // Execute
        webhookManagementService.deleteWebhook(WEBHOOK_ID, TENANT_DOMAIN);

        // Verify
        verify(daoFacade, times(1)).deleteWebhook(eq(WEBHOOK_ID), anyInt());
    }

    @Test(expectedExceptions = WebhookMgtClientException.class)
    public void testDeleteNonExistentWebhook() throws WebhookMgtException {
        // Prepare
        when(daoFacade.getWebhook(eq(WEBHOOK_ID), anyInt())).thenReturn(null);

        // Execute - should throw exception
        webhookManagementService.deleteWebhook(WEBHOOK_ID, TENANT_DOMAIN);
    }

    @Test
    public void testGetWebhooks() throws WebhookMgtException {
        // Prepare
        List<Webhook> webhooks = new ArrayList<>();
        webhooks.add(mockedWebhook);

        Webhook webhook2 = new Webhook();
        webhook2.setId("webhook-456");
        webhook2.setEndpoint("https://example.org/webhook");
        webhook2.setStatus(WebhookStatus.INACTIVE);
        webhooks.add(webhook2);

        when(daoFacade.getWebhooks(anyInt())).thenReturn(webhooks);

        // Execute
        List<WebhookDTO> results = webhookManagementService.getWebhooks(TENANT_DOMAIN);

        // Verify
        Assert.assertNotNull(results);
        Assert.assertEquals(results.size(), 2);
        Assert.assertEquals(results.get(0).getId(), mockedWebhook.getId());
        Assert.assertEquals(results.get(1).getId(), webhook2.getId());
        verify(daoFacade, times(1)).getWebhooks(anyInt());
    }

    @Test
    public void testActivateWebhook() throws WebhookMgtException {
        // Prepare
        when(daoFacade.getWebhook(eq(WEBHOOK_ID), anyInt())).thenReturn(mockedWebhook);
        doNothing().when(daoFacade).activateWebhook(anyString(), anyInt());

        // Execute
        webhookManagementService.activateWebhook(WEBHOOK_ID, TENANT_DOMAIN);

        // Verify
        verify(daoFacade, times(1)).activateWebhook(eq(WEBHOOK_ID), anyInt());
    }

    @Test(expectedExceptions = WebhookMgtClientException.class)
    public void testActivateNonExistentWebhook() throws WebhookMgtException {
        // Prepare
        when(daoFacade.getWebhook(eq(WEBHOOK_ID), anyInt())).thenReturn(null);

        // Execute - should throw exception
        webhookManagementService.activateWebhook(WEBHOOK_ID, TENANT_DOMAIN);
    }

    @Test
    public void testDeactivateWebhook() throws WebhookMgtException {
        // Prepare
        when(daoFacade.getWebhook(eq(WEBHOOK_ID), anyInt())).thenReturn(mockedWebhook);
        doNothing().when(daoFacade).deactivateWebhook(anyString(), anyInt());

        // Execute
        webhookManagementService.deactivateWebhook(WEBHOOK_ID, TENANT_DOMAIN);

        // Verify
        verify(daoFacade, times(1)).deactivateWebhook(eq(WEBHOOK_ID), anyInt());
    }

    @Test(expectedExceptions = WebhookMgtClientException.class)
    public void testDeactivateNonExistentWebhook() throws WebhookMgtException {
        // Prepare
        when(daoFacade.getWebhook(eq(WEBHOOK_ID), anyInt())).thenReturn(null);

        // Execute - should throw exception
        webhookManagementService.deactivateWebhook(WEBHOOK_ID, TENANT_DOMAIN);
    }

    private static void setFinalStatic(Field field, Object newValue) throws Exception {

        field.setAccessible(true);

        Field modifiersField = Field.class.getDeclaredField("modifiers");
        modifiersField.setAccessible(true);
        modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);

        field.set(null, newValue);
    }
}
