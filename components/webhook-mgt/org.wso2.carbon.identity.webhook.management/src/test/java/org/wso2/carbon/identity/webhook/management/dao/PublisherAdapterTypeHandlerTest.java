/*
 * Copyright (c) 2024, WSO2 LLC. (http://www.wso2.com).
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

import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.common.testng.WithCarbonHome;
import org.wso2.carbon.identity.common.testng.WithH2Database;
import org.wso2.carbon.identity.secret.mgt.core.SecretManager;
import org.wso2.carbon.identity.secret.mgt.core.SecretResolveManager;
import org.wso2.carbon.identity.secret.mgt.core.exception.SecretManagementException;
import org.wso2.carbon.identity.subscription.management.api.model.Subscription;
import org.wso2.carbon.identity.subscription.management.api.model.SubscriptionStatus;
import org.wso2.carbon.identity.webhook.management.api.exception.WebhookMgtException;
import org.wso2.carbon.identity.webhook.management.api.model.Webhook;
import org.wso2.carbon.identity.webhook.management.api.model.WebhookStatus;
import org.wso2.carbon.identity.webhook.management.internal.component.WebhookManagementComponentServiceHolder;
import org.wso2.carbon.identity.webhook.management.internal.dao.WebhookManagementDAO;
import org.wso2.carbon.identity.webhook.management.internal.dao.impl.handler.PublisherAdapterTypeHandler;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@WithH2Database(files = {"dbscripts/h2.sql"})
@WithCarbonHome
public class PublisherAdapterTypeHandlerTest {

    private PublisherAdapterTypeHandler handler;
    private WebhookManagementDAO mockDao;
    private Webhook testWebhook;
    private MockedStatic<WebhookManagementComponentServiceHolder> mockedServiceHolder;

    @BeforeClass
    public void setUpClass() throws SecretManagementException {

        mockDao = mock(WebhookManagementDAO.class);
        handler = new PublisherAdapterTypeHandler(mockDao);

        // Mock static WebhookManagementComponentServiceHolder.getInstance()
        mockedServiceHolder = org.mockito.Mockito.mockStatic(WebhookManagementComponentServiceHolder.class);
        WebhookManagementComponentServiceHolder mockHolder = mock(WebhookManagementComponentServiceHolder.class);
        mockedServiceHolder.when(WebhookManagementComponentServiceHolder::getInstance).thenReturn(mockHolder);

        // Mock SecretManager and SecretResolveManager
        SecretManager mockSecretManager = mock(SecretManager.class);
        SecretResolveManager mockSecretResolveManager = mock(SecretResolveManager.class);
        when(mockHolder.getSecretManager()).thenReturn(mockSecretManager);
        when(mockHolder.getSecretResolveManager()).thenReturn(mockSecretResolveManager);

        // Mock methods used in WebhookSecretProcessor
        when(mockSecretManager.isSecretExist(anyString(), anyString())).thenReturn(false);
        when(mockSecretManager.getSecretType(anyString())).thenReturn(
                new org.wso2.carbon.identity.secret.mgt.core.model.SecretType("WEBHOOK_SECRETS", "WEBHOOK_SECRETS",
                        "Webhook Secrets"));
        org.wso2.carbon.identity.secret.mgt.core.model.ResolvedSecret resolvedSecret =
                new org.wso2.carbon.identity.secret.mgt.core.model.ResolvedSecret();
        resolvedSecret.setSecretName("name");
        resolvedSecret.setResolvedSecretValue("value");
        when(mockSecretResolveManager.getResolvedSecret(anyString(), anyString()))
                .thenReturn(resolvedSecret);
    }

    @AfterClass
    public void tearDownClass() {

        if (mockedServiceHolder != null) {
            mockedServiceHolder.close();
        }
    }

    @BeforeMethod
    public void setUp() throws WebhookMgtException {

        testWebhook = createTestWebhook();
        when(mockDao.getWebhook(anyString(), anyInt())).thenReturn(testWebhook);
        List<Webhook> value = new ArrayList<>();
        value.add(testWebhook);
        when(mockDao.getWebhooks(anyInt())).thenReturn(
                value);
        when(mockDao.isWebhookEndpointExists(anyString(), anyInt())).thenReturn(true);
        when(mockDao.getWebhookEvents(anyString(), anyInt())).thenReturn(testWebhook.getEventsSubscribed());
    }

    @Test
    public void testCreateWebhook() throws WebhookMgtException {

        doNothing().when(mockDao).createWebhook(any(Webhook.class), anyInt());
        handler.createWebhook(testWebhook, 1);
        verify(mockDao, times(1)).createWebhook(any(Webhook.class), eq(1));
    }

    @Test
    public void testGetWebhooks() throws WebhookMgtException {

        List<Webhook> webhooks = handler.getWebhooks(1);
        Assert.assertNotNull(webhooks);
        Assert.assertEquals(webhooks.size(), 1);
        Assert.assertEquals(webhooks.get(0).getId(), testWebhook.getId());
    }

    @Test
    public void testGetWebhook() throws WebhookMgtException {

        Webhook webhook = handler.getWebhook(testWebhook.getId(), 1);
        Assert.assertNotNull(webhook);
        Assert.assertEquals(webhook.getId(), testWebhook.getId());
    }

    @Test
    public void testUpdateWebhook() throws WebhookMgtException {

        doNothing().when(mockDao).updateWebhook(any(Webhook.class), anyInt());
        handler.updateWebhook(testWebhook, 1);
        verify(mockDao, times(1)).updateWebhook(any(Webhook.class), eq(1));
    }

    @Test
    public void testDeleteWebhook() throws WebhookMgtException {

        doNothing().when(mockDao).deleteWebhook(anyString(), anyInt());
        handler.deleteWebhook(testWebhook.getId(), 1);
        verify(mockDao, times(1)).deleteWebhook(testWebhook.getId(), 1);
    }

    @Test
    public void testIsWebhookEndpointExists() throws WebhookMgtException {

        boolean exists = handler.isWebhookEndpointExists(testWebhook.getEndpoint(), 1);
        Assert.assertTrue(exists);
    }

    @Test
    public void testActivateWebhook() throws WebhookMgtException {

        doNothing().when(mockDao).activateWebhook(any(), anyInt());
        handler.activateWebhook(testWebhook, 1);

        ArgumentCaptor<Webhook> captor = ArgumentCaptor.forClass(Webhook.class);
        verify(mockDao, times(1)).activateWebhook(captor.capture(), eq(1));
        Webhook activatedWebhook = captor.getValue();
        Assert.assertEquals(activatedWebhook.getId(), testWebhook.getId());
        Assert.assertEquals(activatedWebhook.getStatus(), WebhookStatus.ACTIVE);
    }

    @Test
    public void testDeactivateWebhook() throws WebhookMgtException {

        doNothing().when(mockDao).deactivateWebhook(any(), anyInt());
        handler.deactivateWebhook(testWebhook, 1);

        ArgumentCaptor<Webhook> captor = ArgumentCaptor.forClass(Webhook.class);
        verify(mockDao, times(1)).deactivateWebhook(captor.capture(), eq(1));
        Webhook deactivatedWebhook = captor.getValue();
        Assert.assertEquals(deactivatedWebhook.getId(), testWebhook.getId());
        Assert.assertEquals(deactivatedWebhook.getStatus(), WebhookStatus.INACTIVE);
    }

    @Test
    public void testGetWebhookEvents() throws WebhookMgtException {

        List<Subscription> events = handler.getWebhookEvents(testWebhook.getId(), 1);
        Assert.assertNotNull(events);
        Assert.assertEquals(events.size(), testWebhook.getEventsSubscribed().size());
    }

    @Test
    public void testGetWebhooksCount() throws WebhookMgtException {

        when(mockDao.getWebhooksCount(anyInt())).thenReturn(1);
        int count = handler.getWebhooksCount(1);
        Assert.assertEquals(count, 1);
    }

    private Webhook createTestWebhook() {

        List<Subscription> eventsSubscribed = new ArrayList<>();
        eventsSubscribed.add(Subscription.builder()
                .channelUri("user.create")
                .status(SubscriptionStatus.SUBSCRIPTION_ACCEPTED)
                .build());
        eventsSubscribed.add(Subscription.builder()
                .channelUri("user.update")
                .status(SubscriptionStatus.SUBSCRIPTION_ACCEPTED)
                .build());
        return new Webhook.Builder()
                .uuid(UUID.randomUUID().toString())
                .endpoint("https://example.com/webhook")
                .name("Test webhook")
                .secret("test-secret")
                .eventProfileName("user-events")
                .eventProfileUri("https://schemas.org/user-events")
                .status(WebhookStatus.ACTIVE)
                .createdAt(new Timestamp(System.currentTimeMillis()))
                .updatedAt(new Timestamp(System.currentTimeMillis()))
                .eventsSubscribed(eventsSubscribed)
                .build();
    }
}
