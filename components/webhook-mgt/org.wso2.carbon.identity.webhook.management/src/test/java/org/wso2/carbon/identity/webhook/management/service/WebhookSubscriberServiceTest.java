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

import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.webhook.management.api.exception.WebhookMgtException;
import org.wso2.carbon.identity.webhook.management.api.model.Webhook;
import org.wso2.carbon.identity.webhook.management.api.service.WebhookSubscriber;
import org.wso2.carbon.identity.webhook.management.api.service.WebhookSubscriberService;
import org.wso2.carbon.identity.webhook.management.internal.component.WebhookManagementComponentServiceHolder;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

/**
 * Test class for WebhookSubscriberService.
 */
public class WebhookSubscriberServiceTest {

    private WebhookSubscriberService webhookSubscriberService;

    @Mock
    private WebhookSubscriber subscriber1;

    @Mock
    private WebhookSubscriber subscriber2;

    @Mock
    private Webhook webhook;

    @BeforeClass
    public void setUpClass() {

        MockitoAnnotations.openMocks(this);
        webhookSubscriberService = new WebhookSubscriberService();

        when(subscriber1.getName()).thenReturn("Subscriber1");
        when(subscriber2.getName()).thenReturn("Subscriber2");
    }

    @BeforeMethod
    public void setUp() {

        List<WebhookSubscriber> subscribers = new ArrayList<>();
        subscribers.add(subscriber1);
        subscribers.add(subscriber2);

        when(WebhookManagementComponentServiceHolder.getInstance().getWebhookSubscribers()).thenReturn(subscribers);
    }

    @Test
    public void testSubscribeWithNoSubscribers() throws WebhookMgtException {

        when(WebhookManagementComponentServiceHolder.getInstance().getWebhookSubscribers()).thenReturn(
                new ArrayList<>());

        boolean result = webhookSubscriberService.subscribe(webhook, "testTenant");
        Assert.assertTrue(result, "Subscription should succeed even with no subscribers.");
    }

    @Test
    public void testSubscribeWithSubscribers() throws WebhookMgtException {

        when(webhook.getEventsSubscribed()).thenReturn(new ArrayList<>());
        when(webhook.getEndpoint()).thenReturn("http://example.com");
        when(webhook.getId()).thenReturn("webhook1");

        when(subscriber1.subscribe(anyList(), anyString(), anyString())).thenReturn(true);
        when(subscriber2.subscribe(anyList(), anyString(), anyString())).thenReturn(true);

        boolean result = webhookSubscriberService.subscribe(webhook, "testTenant");
        Assert.assertTrue(result, "Subscription should succeed when all subscribers succeed.");
    }

    @Test
    public void testSubscribeWithSubscriberFailure() throws WebhookMgtException {

        when(webhook.getEventsSubscribed()).thenReturn(new ArrayList<>());
        when(webhook.getEndpoint()).thenReturn("http://example.com");
        when(webhook.getId()).thenReturn("webhook1");

        when(subscriber1.subscribe(anyList(), anyString(), anyString())).thenReturn(false);
        when(subscriber2.subscribe(anyList(), anyString(), anyString())).thenReturn(true);

        boolean result = webhookSubscriberService.subscribe(webhook, "testTenant");
        Assert.assertTrue(result, "Subscription should succeed even if one subscriber fails.");
    }

    @Test
    public void testUnsubscribeWithNoSubscribers() throws WebhookMgtException {

        when(WebhookManagementComponentServiceHolder.getInstance().getWebhookSubscribers()).thenReturn(
                new ArrayList<>());

        boolean result = webhookSubscriberService.unsubscribe(webhook, "testTenant");
        Assert.assertTrue(result, "Unsubscription should succeed even with no subscribers.");
    }

    @Test
    public void testUnsubscribeWithSubscribers() throws WebhookMgtException {

        when(webhook.getEventsSubscribed()).thenReturn(new ArrayList<>());
        when(webhook.getEndpoint()).thenReturn("http://example.com");
        when(webhook.getId()).thenReturn("webhook1");

        when(subscriber1.unsubscribe(anyList(), anyString(), anyString())).thenReturn(true);
        when(subscriber2.unsubscribe(anyList(), anyString(), anyString())).thenReturn(true);

        boolean result = webhookSubscriberService.unsubscribe(webhook, "testTenant");
        Assert.assertTrue(result, "Unsubscription should succeed when all subscribers succeed.");
    }

    @Test
    public void testUnsubscribeWithSubscriberFailure() throws WebhookMgtException {

        when(webhook.getEventsSubscribed()).thenReturn(new ArrayList<>());
        when(webhook.getEndpoint()).thenReturn("http://example.com");
        when(webhook.getId()).thenReturn("webhook1");

        when(subscriber1.unsubscribe(anyList(), anyString(), anyString())).thenReturn(false);
        when(subscriber2.unsubscribe(anyList(), anyString(), anyString())).thenReturn(true);

        boolean result = webhookSubscriberService.unsubscribe(webhook, "testTenant");
        Assert.assertTrue(result, "Unsubscription should succeed even if one subscriber fails.");
    }
}
