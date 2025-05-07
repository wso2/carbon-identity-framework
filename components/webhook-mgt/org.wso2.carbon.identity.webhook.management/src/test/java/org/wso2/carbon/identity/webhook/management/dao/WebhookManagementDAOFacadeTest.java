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

package org.wso2.carbon.identity.webhook.management.dao;

import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.webhook.management.api.exception.WebhookMgtException;
import org.wso2.carbon.identity.webhook.management.api.model.Webhook;
import org.wso2.carbon.identity.webhook.management.api.model.WebhookStatus;
import org.wso2.carbon.identity.webhook.management.internal.dao.impl.CacheBackedWebhookManagementDAO;
import org.wso2.carbon.identity.webhook.management.internal.dao.impl.WebhookManagementDAOFacade;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class WebhookManagementDAOFacadeTest {

    private static final String WEBHOOK_ID = "webhook-123";
    private static final int TENANT_ID = 1;
    private static final String ENDPOINT = "https://example.com/webhook";

    @Mock
    private CacheBackedWebhookManagementDAO cacheBackedWebhookManagementDAO;

    private WebhookManagementDAOFacade webhookManagementDAOFacade;
    private Webhook mockedWebhook;

    @BeforeMethod
    public void setUp() {

        MockitoAnnotations.openMocks(this);
        webhookManagementDAOFacade = new WebhookManagementDAOFacade(cacheBackedWebhookManagementDAO);

        // Create a mocked webhook for testing
        mockedWebhook = new Webhook();
        mockedWebhook.setId(WEBHOOK_ID);
        mockedWebhook.setEndpoint(ENDPOINT);
        mockedWebhook.setDescription("Test Webhook");
        mockedWebhook.setStatus(WebhookStatus.ACTIVE);

        List<String> events = new ArrayList<>();
        events.add("user.created");
        events.add("user.updated");
        mockedWebhook.setEventsSubscribed(events);
    }

    @Test
    public void testCreateWebhook() throws WebhookMgtException {

        doNothing().when(cacheBackedWebhookManagementDAO).createWebhook(any(Webhook.class), anyInt());

        webhookManagementDAOFacade.createWebhook(mockedWebhook, TENANT_ID);

        verify(cacheBackedWebhookManagementDAO, times(1)).createWebhook(mockedWebhook,
                TENANT_ID);
    }

    @Test
    public void testGetWebhook() throws WebhookMgtException {

        when(cacheBackedWebhookManagementDAO.getWebhook(anyString(), anyInt())).thenReturn(mockedWebhook);

        Webhook webhook = webhookManagementDAOFacade.getWebhook(WEBHOOK_ID, TENANT_ID);

        Assert.assertNotNull(webhook);
        Assert.assertEquals(webhook.getId(), mockedWebhook.getId());
        Assert.assertEquals(webhook.getEndpoint(), mockedWebhook.getEndpoint());
        Assert.assertEquals(webhook.getDescription(), mockedWebhook.getDescription());
        Assert.assertEquals(webhook.getStatus(), mockedWebhook.getStatus());
        Assert.assertEquals(webhook.getEventsSubscribed(), mockedWebhook.getEventsSubscribed());

        verify(cacheBackedWebhookManagementDAO, times(1)).getWebhook(WEBHOOK_ID, TENANT_ID);
    }

    @Test
    public void testUpdateWebhook() throws WebhookMgtException {

        doNothing().when(cacheBackedWebhookManagementDAO).updateWebhook(anyString(), any(Webhook.class), anyInt());

        webhookManagementDAOFacade.updateWebhook(WEBHOOK_ID, mockedWebhook, TENANT_ID);

        verify(cacheBackedWebhookManagementDAO, times(1)).updateWebhook(WEBHOOK_ID,
                mockedWebhook, TENANT_ID);
    }

    @Test
    public void testDeleteWebhook() throws WebhookMgtException {

        doNothing().when(cacheBackedWebhookManagementDAO).deleteWebhook(anyString(), anyInt());

        webhookManagementDAOFacade.deleteWebhook(WEBHOOK_ID, TENANT_ID);

        verify(cacheBackedWebhookManagementDAO, times(1)).deleteWebhook(WEBHOOK_ID, TENANT_ID);
    }

    @Test
    public void testGetWebhooks() throws WebhookMgtException {

        List<Webhook> webhooks = new ArrayList<>();
        webhooks.add(mockedWebhook);

        when(cacheBackedWebhookManagementDAO.getWebhooks(anyInt())).thenReturn(webhooks);

        List<Webhook> result = webhookManagementDAOFacade.getWebhooks(TENANT_ID);

        Assert.assertNotNull(result);
        Assert.assertEquals(result.size(), 1);
        Assert.assertEquals(result.get(0).getId(), mockedWebhook.getId());

        verify(cacheBackedWebhookManagementDAO, times(1)).getWebhooks(TENANT_ID);
    }

    @Test
    public void testIsWebhookEndpointExists() throws WebhookMgtException {

        when(cacheBackedWebhookManagementDAO.isWebhookEndpointExists(anyString(), anyInt())).thenReturn(true);

        boolean exists = webhookManagementDAOFacade.isWebhookEndpointExists(ENDPOINT, TENANT_ID);

        Assert.assertTrue(exists);
        verify(cacheBackedWebhookManagementDAO, times(1)).isWebhookEndpointExists(ENDPOINT,
                TENANT_ID);
    }

    @Test
    public void testActivateWebhook() throws WebhookMgtException {

        doNothing().when(cacheBackedWebhookManagementDAO).activateWebhook(anyString(), anyInt());

        webhookManagementDAOFacade.activateWebhook(WEBHOOK_ID, TENANT_ID);

        verify(cacheBackedWebhookManagementDAO, times(1)).activateWebhook(WEBHOOK_ID,
                TENANT_ID);
    }

    @Test
    public void testDeactivateWebhook() throws WebhookMgtException {

        doNothing().when(cacheBackedWebhookManagementDAO).deactivateWebhook(anyString(), anyInt());

        webhookManagementDAOFacade.deactivateWebhook(WEBHOOK_ID, TENANT_ID);

        verify(cacheBackedWebhookManagementDAO, times(1)).deactivateWebhook(WEBHOOK_ID,
                TENANT_ID);
    }
}
