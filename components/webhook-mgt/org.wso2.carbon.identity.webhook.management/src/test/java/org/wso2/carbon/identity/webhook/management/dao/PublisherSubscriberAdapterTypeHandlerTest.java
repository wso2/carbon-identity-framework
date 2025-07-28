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

import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.secret.mgt.core.SecretManager;
import org.wso2.carbon.identity.secret.mgt.core.SecretResolveManager;
import org.wso2.carbon.identity.secret.mgt.core.exception.SecretManagementException;
import org.wso2.carbon.identity.secret.mgt.core.model.ResolvedSecret;
import org.wso2.carbon.identity.secret.mgt.core.model.SecretType;
import org.wso2.carbon.identity.subscription.management.api.exception.SubscriptionManagementException;
import org.wso2.carbon.identity.subscription.management.api.model.Subscription;
import org.wso2.carbon.identity.subscription.management.api.model.SubscriptionStatus;
import org.wso2.carbon.identity.subscription.management.api.service.SubscriptionManagementService;
import org.wso2.carbon.identity.topic.management.api.service.TopicManagementService;
import org.wso2.carbon.identity.webhook.management.api.exception.WebhookMgtException;
import org.wso2.carbon.identity.webhook.management.api.model.Webhook;
import org.wso2.carbon.identity.webhook.management.api.model.WebhookStatus;
import org.wso2.carbon.identity.webhook.management.internal.component.WebhookManagementComponentServiceHolder;
import org.wso2.carbon.identity.webhook.management.internal.dao.WebhookManagementDAO;
import org.wso2.carbon.identity.webhook.management.internal.dao.impl.handler.PublisherSubscriberAdapterTypeHandler;
import org.wso2.carbon.identity.webhook.metadata.api.model.Adapter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class PublisherSubscriberAdapterTypeHandlerTest {

    private PublisherSubscriberAdapterTypeHandler handler;
    private WebhookManagementDAO dao;
    private MockedStatic<WebhookManagementComponentServiceHolder> serviceHolderMock;
    private SecretManager secretManager;
    private SecretResolveManager secretResolveManager;
    private SubscriptionManagementService subscriptionManagementService;
    private TopicManagementService topicManagementService;

    @BeforeClass
    public void setUpClass() throws SecretManagementException {

        dao = mock(WebhookManagementDAO.class);
        serviceHolderMock = Mockito.mockStatic(WebhookManagementComponentServiceHolder.class);
        WebhookManagementComponentServiceHolder holder = mock(WebhookManagementComponentServiceHolder.class);
        when(holder.getWebhookAdapter()).thenReturn(mock(Adapter.class));

        // SecretManager and SecretResolveManager stubs
        secretManager = mock(SecretManager.class);
        secretResolveManager = mock(SecretResolveManager.class);
        when(holder.getSecretManager()).thenReturn(secretManager);
        when(holder.getSecretResolveManager()).thenReturn(secretResolveManager);
        SecretType secretType = new SecretType("WebhookSecret", "webhook-secret-type", "desc");
        when(secretManager.getSecretType(anyString())).thenReturn(secretType);

        // In your setUpClass() or setUp() method:
        String secretName = "id:ENDPOINT:SECRET";
        when(secretManager.isSecretExist("WEBHOOK_SECRETS", secretName)).thenReturn(true);

        ResolvedSecret resolvedSecret = new ResolvedSecret();
        resolvedSecret.setResolvedSecretValue("decryptedSecret");
        when(secretResolveManager.getResolvedSecret("WEBHOOK_SECRETS", secretName)).thenReturn(resolvedSecret);

        // SubscriptionManagementService and TopicManagementService stubs
        subscriptionManagementService = mock(SubscriptionManagementService.class);
        topicManagementService = mock(TopicManagementService.class);
        when(holder.getSubscriptionManagementService()).thenReturn(subscriptionManagementService);
        when(holder.getTopicManagementService()).thenReturn(topicManagementService);

        // Secret existence and resolution stubs
        when(secretManager.isSecretExist(anyString(), anyString())).thenReturn(true);
        resolvedSecret.setResolvedSecretValue("decryptedSecret");
        when(secretResolveManager.getResolvedSecret(anyString(), anyString())).thenReturn(resolvedSecret);

        serviceHolderMock.when(WebhookManagementComponentServiceHolder::getInstance).thenReturn(holder);

        handler = new PublisherSubscriberAdapterTypeHandler(dao);
    }

    @AfterClass
    public void tearDownClass() {

        serviceHolderMock.close();
    }

    @BeforeMethod
    public void resetMocks() {

        Mockito.reset(dao);
    }

    @Test
    public void testCreateWebhook() throws WebhookMgtException {

        Webhook webhook = mock(Webhook.class);
        when(webhook.getId()).thenReturn("id");
        when(webhook.getSecret()).thenReturn("secret");
        when(webhook.getEventsSubscribed()).thenReturn(Collections.emptyList());
        when(webhook.getEventProfileName()).thenReturn("profile");
        when(webhook.getEventProfileVersion()).thenReturn("v1");
        when(webhook.getStatus()).thenReturn(WebhookStatus.INACTIVE);
        handler.createWebhook(webhook, 1);
        verify(dao, times(1)).createWebhook(any(Webhook.class), eq(1));
    }

    @Test
    public void testGetWebhooks() throws WebhookMgtException {

        List<Webhook> webhooks = Collections.singletonList(mock(Webhook.class));
        when(dao.getWebhooks(1)).thenReturn(webhooks);
        List<Webhook> result = handler.getWebhooks(1);
        Assert.assertEquals(result, webhooks);
        verify(dao).getWebhooks(1);
    }

    @Test
    public void testGetWebhook() throws WebhookMgtException {

        Webhook webhook = mock(Webhook.class);
        when(dao.getWebhook("id", 1)).thenReturn(webhook);
        Webhook result = handler.getWebhook("id", 1);
        Assert.assertEquals(result, webhook);
        verify(dao, times(1)).getWebhook("id", 1);
    }

    @Test
    public void testGetWebhookEvents() throws WebhookMgtException {

        List<Subscription> events = Collections.singletonList(mock(Subscription.class));
        when(dao.getWebhookEvents("id", 1)).thenReturn(events);
        List<Subscription> result = handler.getWebhookEvents("id", 1);
        Assert.assertEquals(result, events);
        verify(dao).getWebhookEvents("id", 1);
    }

    @Test
    public void testDeleteWebhook() throws WebhookMgtException {

        Webhook webhook = mock(Webhook.class);
        when(webhook.getStatus()).thenReturn(WebhookStatus.INACTIVE);
        when(dao.getWebhook("id", 1)).thenReturn(webhook);
        handler.deleteWebhook("id", 1);
        verify(dao).deleteWebhook("id", 1);
    }

    @Test
    public void testIsWebhookEndpointExists() throws WebhookMgtException {

        when(dao.isWebhookEndpointExists("endpoint", 1)).thenReturn(true);
        boolean exists = handler.isWebhookEndpointExists("endpoint", 1);
        Assert.assertTrue(exists);
        verify(dao).isWebhookEndpointExists("endpoint", 1);
    }

    @Test
    public void testActivateWebhookByObject()
            throws WebhookMgtException, SubscriptionManagementException, SecretManagementException {

        Webhook webhook = mock(Webhook.class);

        when(secretManager.isSecretExist(anyString(), eq("id"))).thenReturn(true);
        ResolvedSecret resolvedSecret = new ResolvedSecret();
        resolvedSecret.setResolvedSecretValue("decryptedSecret");
        when(secretResolveManager.getResolvedSecret(anyString(), eq("id"))).thenReturn(resolvedSecret);

        when(webhook.getId()).thenReturn("id");
        when(webhook.getStatus()).thenReturn(WebhookStatus.INACTIVE);
        when(webhook.getEventsSubscribed()).thenReturn(Collections.emptyList());
        when(webhook.getEventProfileName()).thenReturn("profile");
        when(webhook.getEventProfileVersion()).thenReturn("v1");
        when(webhook.getSecret()).thenReturn("secret");
        when(subscriptionManagementService.subscribe(any(), anyString(), anyString()))
                .thenReturn(Collections.emptyList());
        handler.activateWebhook(webhook, 1);
        verify(dao).activateWebhook(any(Webhook.class), eq(1));
    }

    @Test
    public void testDeactivateWebhookByObject() throws WebhookMgtException, SubscriptionManagementException {

        Webhook webhook = mock(Webhook.class);
        when(webhook.getId()).thenReturn("id");
        when(webhook.getStatus()).thenReturn(WebhookStatus.ACTIVE);
        when(webhook.getEventsSubscribed()).thenReturn(Collections.emptyList());
        when(webhook.getEventProfileName()).thenReturn("profile");
        when(webhook.getEventProfileVersion()).thenReturn("v1");
        when(subscriptionManagementService.unsubscribe(any(), anyString(), anyString()))
                .thenReturn(Collections.emptyList());
        handler.deactivateWebhook(webhook, 1);
        verify(dao).deactivateWebhook(any(Webhook.class), eq(1));
    }

    @Test
    public void testGetWebhooksCount() throws WebhookMgtException {

        when(dao.getWebhooksCount(1)).thenReturn(5);
        int count = handler.getWebhooksCount(1);
        Assert.assertEquals(count, 5);
        verify(dao).getWebhooksCount(1);
    }

    @Test(expectedExceptions = WebhookMgtException.class)
    public void testUpdateWebhook() throws WebhookMgtException {

        Webhook webhook = mock(Webhook.class);
        handler.updateWebhook(webhook, 1);
    }

    @Test(expectedExceptions = WebhookMgtException.class)
    public void testRetryWebhookThrowsException() throws WebhookMgtException {

        Webhook webhook = mock(Webhook.class);
        // Set status to PARTIALLY_ACTIVE to trigger the exception
        when(webhook.getStatus()).thenReturn(WebhookStatus.PARTIALLY_ACTIVE);
        doThrow(new WebhookMgtException("Retry not allowed")).when(dao).retryWebhook(any(Webhook.class), anyInt());
        handler.retryWebhook(webhook, 1);
    }

    @Test
    public void testRetryWebhookPartiallyInactive() throws WebhookMgtException, SubscriptionManagementException {

        Webhook webhook = mock(Webhook.class);
        when(webhook.getStatus()).thenReturn(WebhookStatus.PARTIALLY_INACTIVE);
        when(webhook.getId()).thenReturn("id");
        when(webhook.getEventProfileName()).thenReturn("profile");
        when(webhook.getEventProfileVersion()).thenReturn("v1");
        when(webhook.getEndpoint()).thenReturn("endpoint");
        when(webhook.getSecret()).thenReturn("secret");

        Subscription sub1 = mock(Subscription.class);
        when(sub1.getStatus()).thenReturn(SubscriptionStatus.UNSUBSCRIPTION_ACCEPTED);
        when(sub1.getChannelUri()).thenReturn("chan1");
        Subscription sub2 = mock(Subscription.class);
        when(sub2.getStatus()).thenReturn(SubscriptionStatus.UNSUBSCRIPTION_ERROR);
        when(sub2.getChannelUri()).thenReturn("chan2");
        List<Subscription> eventsSubscribed = new ArrayList<>();
        eventsSubscribed.add(sub1);
        eventsSubscribed.add(sub2);
        when(webhook.getEventsSubscribed()).thenReturn(eventsSubscribed);

        // Unsubscription results: one error, one accepted
        Subscription result1 = mock(Subscription.class);
        when(result1.getStatus()).thenReturn(SubscriptionStatus.UNSUBSCRIPTION_ACCEPTED);
        when(result1.getChannelUri()).thenReturn("chan1");
        Subscription result2 = mock(Subscription.class);
        when(result2.getStatus()).thenReturn(SubscriptionStatus.UNSUBSCRIPTION_ERROR);
        when(result2.getChannelUri()).thenReturn("chan2");
        List<Subscription> allResults = new ArrayList<>();
        allResults.add(result1);
        allResults.add(result2);

        when(subscriptionManagementService.unsubscribe(any(), anyString(), anyString())).thenReturn(allResults);

        handler.retryWebhook(webhook, 1);

        verify(dao).retryWebhook(any(Webhook.class), eq(1));
    }
}
