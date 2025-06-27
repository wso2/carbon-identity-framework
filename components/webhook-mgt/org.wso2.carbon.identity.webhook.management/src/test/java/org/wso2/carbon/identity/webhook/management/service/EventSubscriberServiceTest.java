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
import org.wso2.carbon.identity.webhook.management.api.model.Subscription;
import org.wso2.carbon.identity.webhook.management.api.model.Webhook;
import org.wso2.carbon.identity.webhook.management.api.service.EventSubscriber;
import org.wso2.carbon.identity.webhook.management.internal.component.WebhookManagementComponentServiceHolder;
import org.wso2.carbon.identity.webhook.management.internal.service.impl.EventSubscriberService;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
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

    private static final String ADAPTOR = "Subscriber1";
    private static final int TENANT_ID = 1;

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

        List<Subscription> expectedSubscriptions = Collections.singletonList(mock(Subscription.class));
        when(subscriber1.subscribe(webhook, TENANT_ID)).thenReturn(expectedSubscriptions);

        List<Subscription> result = eventSubscriberService.subscribe(webhook, ADAPTOR, TENANT_ID);

        verify(subscriber1).subscribe(webhook, TENANT_ID);
        assert result == expectedSubscriptions;
    }

    @Test(expectedExceptions = WebhookMgtException.class)
    public void testSubscribeWithNoSubscribers() throws WebhookMgtException {

        when(mockedHolder.getEventSubscribers()).thenReturn(new ArrayList<>());

        eventSubscriberService.subscribe(webhook, ADAPTOR, TENANT_ID);
    }

    @Test(expectedExceptions = RuntimeException.class)
    public void testSubscribeWithException() throws WebhookMgtException {

        List<EventSubscriber> subscribers = Arrays.asList(subscriber1, subscriber2);
        when(mockedHolder.getEventSubscribers()).thenReturn(subscribers);

        when(subscriber1.subscribe(webhook, TENANT_ID)).thenThrow(new RuntimeException("Subscription error"));

        eventSubscriberService.subscribe(webhook, ADAPTOR, TENANT_ID);
    }

    @Test
    public void testUnsubscribeWithMultipleSubscribers() throws WebhookMgtException {

        List<EventSubscriber> subscribers = Arrays.asList(subscriber1, subscriber2);
        when(mockedHolder.getEventSubscribers()).thenReturn(subscribers);

        List<Subscription> expectedSubscriptions = Collections.singletonList(mock(Subscription.class));
        when(subscriber1.unsubscribe(webhook, TENANT_ID)).thenReturn(expectedSubscriptions);

        List<Subscription> result = eventSubscriberService.unsubscribe(webhook, ADAPTOR, TENANT_ID);

        verify(subscriber1).unsubscribe(webhook, TENANT_ID);
        assert result == expectedSubscriptions;
    }

    @Test(expectedExceptions = WebhookMgtException.class)
    public void testUnsubscribeWithNoSubscribers() throws WebhookMgtException {

        when(mockedHolder.getEventSubscribers()).thenReturn(new ArrayList<>());

        eventSubscriberService.unsubscribe(webhook, ADAPTOR, TENANT_ID);
    }

    @Test(expectedExceptions = RuntimeException.class)
    public void testUnsubscribeWithException() throws WebhookMgtException {

        List<EventSubscriber> subscribers = Arrays.asList(subscriber1, subscriber2);
        when(mockedHolder.getEventSubscribers()).thenReturn(subscribers);

        when(subscriber1.unsubscribe(webhook, TENANT_ID)).thenThrow(new RuntimeException("Unsubscription error"));

        eventSubscriberService.unsubscribe(webhook, ADAPTOR, TENANT_ID);
    }
}
