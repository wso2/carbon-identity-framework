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
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.webhook.management.api.exception.WebhookMgtException;
import org.wso2.carbon.identity.webhook.management.api.model.Webhook;
import org.wso2.carbon.identity.webhook.management.api.model.WebhookSearchResult;
import org.wso2.carbon.identity.webhook.management.internal.dao.WebhookManagementDAO;
import org.wso2.carbon.identity.webhook.management.internal.dao.impl.WebhookManagementFacade;
import org.wso2.carbon.identity.webhook.management.util.TestUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.wso2.carbon.identity.webhook.management.internal.constant.WebhookSQLConstants.STATUS_ACTIVE;

public class WebhookManagementDAOFacadeTest {

    private static final int TENANT_ID = 1;
    private static final String WEBHOOK_ID = UUID.randomUUID().toString();
    private static final String ENDPOINT = "https://test-webhook.com/endpoint";
    private static final String DESCRIPTION = "Test webhook description";
    private static final String SECRET = "test-secret-key";
    private static final String VERSION = "1.0.0";
    private static final String EVENT_SCHEMA_NAME = "standard";
    private static final String EVENT_SCHEMA_VERSION = "1.0";
    private static final String EVENT_SCHEMA_URI = "https://example.com/schema";
    private static final List<String> EVENTS_SUBSCRIBED = Arrays.asList("user.created", "user.updated");

    @Mock
    private WebhookManagementDAO webhookManagementDAO;

    private WebhookManagementFacade webhookManagementFacade;
    private Webhook webhook;

    @BeforeClass
    public void setUpClass() {

        MockitoAnnotations.openMocks(this);
    }

    @BeforeMethod
    public void setUp() {

        webhookManagementFacade = new WebhookManagementFacade(webhookManagementDAO);
        webhook = TestUtil.buildMockWebhook(
                WEBHOOK_ID,
                ENDPOINT,
                DESCRIPTION,
                SECRET,
                VERSION,
                EVENT_SCHEMA_NAME,
                EVENT_SCHEMA_URI,
                EVENT_SCHEMA_VERSION,
                STATUS_ACTIVE,
                EVENTS_SUBSCRIBED
        );
    }

    @Test
    public void testCreateWebhook() throws WebhookMgtException {

        doNothing().when(webhookManagementDAO).createWebhook(any(Webhook.class), anyInt());

        webhookManagementFacade.createWebhook(webhook, TENANT_ID);

        verify(webhookManagementDAO, times(1)).createWebhook(webhook, TENANT_ID);
    }

    @Test
    public void testGetWebhook() throws WebhookMgtException {

        when(webhookManagementDAO.getWebhook(anyString(), anyInt())).thenReturn(webhook);

        Webhook retrievedWebhook = webhookManagementFacade.getWebhook(WEBHOOK_ID, TENANT_ID);

        Assert.assertNotNull(retrievedWebhook);
        Assert.assertEquals(retrievedWebhook.getId(), WEBHOOK_ID);
        Assert.assertEquals(retrievedWebhook.getEndpoint(), ENDPOINT);
        Assert.assertEquals(retrievedWebhook.getDescription(), DESCRIPTION);

        verify(webhookManagementDAO, times(1)).getWebhook(WEBHOOK_ID, TENANT_ID);
    }

    @Test
    public void testUpdateWebhook() throws WebhookMgtException {

        doNothing().when(webhookManagementDAO).updateWebhook(anyString(), any(Webhook.class), anyInt());

        webhookManagementFacade.updateWebhook(WEBHOOK_ID, webhook, TENANT_ID);

        verify(webhookManagementDAO, times(1)).updateWebhook(WEBHOOK_ID, webhook, TENANT_ID);
    }

    @Test
    public void testDeleteWebhook() throws WebhookMgtException {

        doNothing().when(webhookManagementDAO).deleteWebhook(anyString(), anyInt());

        webhookManagementFacade.deleteWebhook(WEBHOOK_ID, TENANT_ID);

        verify(webhookManagementDAO, times(1)).deleteWebhook(WEBHOOK_ID, TENANT_ID);
    }

    @Test
    public void testListWebhooks() throws WebhookMgtException {

        List<Webhook> webhooks = new ArrayList<>();
        webhooks.add(webhook);
        WebhookSearchResult expectedResult = new WebhookSearchResult(webhooks, 1);

        when(webhookManagementDAO.listWebhooks(anyString(), anyInt(), anyInt(), anyInt())).thenReturn(expectedResult);

        WebhookSearchResult result = webhookManagementFacade.listWebhooks(STATUS_ACTIVE, 10, 0, TENANT_ID);

        Assert.assertNotNull(result);
        Assert.assertEquals(result.getTotalCount(), 1);
        Assert.assertEquals(result.getWebhooks().size(), 1);
        Assert.assertEquals(result.getWebhooks().get(0).getId(), WEBHOOK_ID);

        verify(webhookManagementDAO, times(1)).listWebhooks(STATUS_ACTIVE, 10, 0, TENANT_ID);
    }

    @Test
    public void testIsWebhookEndpointExists() throws WebhookMgtException {

        when(webhookManagementDAO.isWebhookEndpointExists(anyString(), anyInt())).thenReturn(true);

        boolean exists = webhookManagementFacade.isWebhookEndpointExists(ENDPOINT, TENANT_ID);

        Assert.assertTrue(exists);
        verify(webhookManagementDAO, times(1)).isWebhookEndpointExists(ENDPOINT, TENANT_ID);
    }

    @Test
    public void testListWebhookEndpoints() throws WebhookMgtException {

        List<String> endpoints = new ArrayList<>();
        endpoints.add(ENDPOINT);

        when(webhookManagementDAO.listWebhookEndpoints(anyInt())).thenReturn(endpoints);

        List<String> result = webhookManagementFacade.listWebhookEndpoints(TENANT_ID);

        Assert.assertNotNull(result);
        Assert.assertEquals(result.size(), 1);
        Assert.assertEquals(result.get(0), ENDPOINT);

        verify(webhookManagementDAO, times(1)).listWebhookEndpoints(TENANT_ID);
    }

    @Test
    public void testActivateWebhook() throws WebhookMgtException {

        doNothing().when(webhookManagementDAO).activateWebhook(anyString(), anyInt());

        webhookManagementFacade.activateWebhook(WEBHOOK_ID, TENANT_ID);

        verify(webhookManagementDAO, times(1)).activateWebhook(WEBHOOK_ID, TENANT_ID);
    }

    @Test
    public void testDeactivateWebhook() throws WebhookMgtException {

        doNothing().when(webhookManagementDAO).deactivateWebhook(anyString(), anyInt());

        webhookManagementFacade.deactivateWebhook(WEBHOOK_ID, TENANT_ID);

        verify(webhookManagementDAO, times(1)).deactivateWebhook(WEBHOOK_ID, TENANT_ID);
    }
}
