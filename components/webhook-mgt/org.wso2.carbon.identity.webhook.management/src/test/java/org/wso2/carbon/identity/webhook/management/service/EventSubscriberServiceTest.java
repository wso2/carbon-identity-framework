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
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.webhook.management.api.exception.WebhookMgtException;
import org.wso2.carbon.identity.webhook.management.api.model.Webhook;
import org.wso2.carbon.identity.webhook.management.api.service.EventSubscriber;
import org.wso2.carbon.identity.webhook.management.api.service.EventSubscriberService;
import org.wso2.carbon.identity.webhook.management.internal.component.WebhookManagementComponentServiceHolder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class EventSubscriberServiceTest {

    private EventSubscriberService eventSubscriberService;

    @Mock
    private EventSubscriber subscriber1;

    @Mock
    private EventSubscriber subscriber2;

    @Mock
    private Webhook webhook;

    private MockedStatic<WebhookManagementComponentServiceHolder> mockedStaticHolder;
    private WebhookManagementComponentServiceHolder mockedHolder;

    private static final String TENANT_DOMAIN = "carbon.super";

    @BeforeMethod
    public void setUp() {

        MockitoAnnotations.openMocks(this);

        mockedHolder = mock(WebhookManagementComponentServiceHolder.class);
        mockedStaticHolder = mockStatic(WebhookManagementComponentServiceHolder.class);
        mockedStaticHolder.when(WebhookManagementComponentServiceHolder::getInstance).thenReturn(mockedHolder);

        eventSubscriberService = new EventSubscriberService();

        when(subscriber1.getName()).thenReturn("Subscriber1");
        when(subscriber2.getName()).thenReturn("Subscriber2");
    }

    @AfterMethod
    public void tearDown() {

        mockedStaticHolder.close();
    }

    @Test
    public void testSubscribeWithMultipleSubscribers() throws WebhookMgtException {

        List<EventSubscriber> subscribers = Arrays.asList(subscriber1, subscriber2);
        when(mockedHolder.getEventSubscribers()).thenReturn(subscribers);
        when(subscriber1.subscribe(webhook.getEventsSubscribed(), webhook.getEndpoint(), TENANT_DOMAIN)).thenReturn(
                true);
        when(subscriber2.subscribe(webhook.getEventsSubscribed(), webhook.getEndpoint(), TENANT_DOMAIN)).thenReturn(
                true);

        eventSubscriberService.subscribe(webhook, TENANT_DOMAIN);

        verify(subscriber1).subscribe(webhook.getEventsSubscribed(), webhook.getEndpoint(), TENANT_DOMAIN);
        verify(subscriber2).subscribe(webhook.getEventsSubscribed(), webhook.getEndpoint(), TENANT_DOMAIN);
    }

    @Test(expectedExceptions = WebhookMgtException.class)
    public void testSubscribeWithNoSubscribers() throws WebhookMgtException {

        when(mockedHolder.getEventSubscribers()).thenReturn(new ArrayList<>());

        eventSubscriberService.subscribe(webhook, TENANT_DOMAIN);
    }

    @Test
    public void testSubscribeWithPartialFailure() throws WebhookMgtException {

        List<EventSubscriber> subscribers = Arrays.asList(subscriber1, subscriber2);
        when(mockedHolder.getEventSubscribers()).thenReturn(subscribers);
        when(subscriber1.subscribe(webhook.getEventsSubscribed(), webhook.getEndpoint(), TENANT_DOMAIN)).thenReturn(
                true);
        when(subscriber2.subscribe(webhook.getEventsSubscribed(), webhook.getEndpoint(), TENANT_DOMAIN)).thenReturn(
                false);

        eventSubscriberService.subscribe(webhook, TENANT_DOMAIN);

        verify(subscriber1).subscribe(webhook.getEventsSubscribed(), webhook.getEndpoint(), TENANT_DOMAIN);
        verify(subscriber2).subscribe(webhook.getEventsSubscribed(), webhook.getEndpoint(), TENANT_DOMAIN);
    }

    @Test(expectedExceptions = WebhookMgtException.class)
    public void testSubscribeWithException() throws WebhookMgtException {

        List<EventSubscriber> subscribers = Arrays.asList(subscriber1, subscriber2);
        when(mockedHolder.getEventSubscribers()).thenReturn(subscribers);
        when(subscriber1.subscribe(webhook.getEventsSubscribed(), webhook.getEndpoint(), TENANT_DOMAIN)).thenThrow(
                new WebhookMgtException("Subscription error"));

        eventSubscriberService.subscribe(webhook, TENANT_DOMAIN);
    }

    @Test
    public void testUnsubscribeWithMultipleSubscribers() throws WebhookMgtException {

        List<EventSubscriber> subscribers = Arrays.asList(subscriber1, subscriber2);
        when(mockedHolder.getEventSubscribers()).thenReturn(subscribers);
        when(subscriber1.unsubscribe(webhook.getEventsSubscribed(), webhook.getEndpoint(), TENANT_DOMAIN)).thenReturn(
                true);
        when(subscriber2.unsubscribe(webhook.getEventsSubscribed(), webhook.getEndpoint(), TENANT_DOMAIN)).thenReturn(
                true);

        eventSubscriberService.unsubscribe(webhook, TENANT_DOMAIN);

        verify(subscriber1).unsubscribe(webhook.getEventsSubscribed(), webhook.getEndpoint(), TENANT_DOMAIN);
        verify(subscriber2).unsubscribe(webhook.getEventsSubscribed(), webhook.getEndpoint(), TENANT_DOMAIN);
    }

    @Test(expectedExceptions = WebhookMgtException.class)
    public void testUnsubscribeWithNoSubscribers() throws WebhookMgtException {

        when(mockedHolder.getEventSubscribers()).thenReturn(new ArrayList<>());

        eventSubscriberService.unsubscribe(webhook, TENANT_DOMAIN);
    }

    @Test
    public void testUnsubscribeWithPartialFailure() throws WebhookMgtException {

        List<EventSubscriber> subscribers = Arrays.asList(subscriber1, subscriber2);
        when(mockedHolder.getEventSubscribers()).thenReturn(subscribers);
        when(subscriber1.unsubscribe(webhook.getEventsSubscribed(), webhook.getEndpoint(), TENANT_DOMAIN)).thenReturn(
                true);
        when(subscriber2.unsubscribe(webhook.getEventsSubscribed(), webhook.getEndpoint(), TENANT_DOMAIN)).thenReturn(
                false);

        eventSubscriberService.unsubscribe(webhook, TENANT_DOMAIN);

        verify(subscriber1).unsubscribe(webhook.getEventsSubscribed(), webhook.getEndpoint(), TENANT_DOMAIN);
        verify(subscriber2).unsubscribe(webhook.getEventsSubscribed(), webhook.getEndpoint(), TENANT_DOMAIN);
    }

    @Test(expectedExceptions = WebhookMgtException.class)
    public void testUnsubscribeWithException() throws WebhookMgtException {

        List<EventSubscriber> subscribers = Arrays.asList(subscriber1, subscriber2);
        when(mockedHolder.getEventSubscribers()).thenReturn(subscribers);
        when(subscriber1.unsubscribe(webhook.getEventsSubscribed(), webhook.getEndpoint(), TENANT_DOMAIN)).thenThrow(
                new WebhookMgtException("Unsubscription error"));

        eventSubscriberService.unsubscribe(webhook, TENANT_DOMAIN);
    }
}
