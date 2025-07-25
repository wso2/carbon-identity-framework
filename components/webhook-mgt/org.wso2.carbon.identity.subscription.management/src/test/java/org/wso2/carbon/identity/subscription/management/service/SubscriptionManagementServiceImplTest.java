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

package org.wso2.carbon.identity.subscription.management.service;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.subscription.management.api.exception.SubscriptionManagementException;
import org.wso2.carbon.identity.subscription.management.api.model.Subscription;
import org.wso2.carbon.identity.subscription.management.api.model.WebhookSubscriptionRequest;
import org.wso2.carbon.identity.subscription.management.api.model.WebhookUnsubscriptionRequest;
import org.wso2.carbon.identity.subscription.management.api.service.EventSubscriber;
import org.wso2.carbon.identity.subscription.management.internal.component.SubscriptionManagementComponentServiceHolder;
import org.wso2.carbon.identity.subscription.management.internal.service.impl.SubscriptionManagementServiceImpl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

public class SubscriptionManagementServiceImplTest {

    private SubscriptionManagementServiceImpl service;
    private EventSubscriber eventSubscriber;
    private WebhookSubscriptionRequest subscriptionRequest;
    private WebhookUnsubscriptionRequest unsubscriptionRequest;
    private static final String ADAPTER = "TestAdapter";
    private static final String TENANT_DOMAIN = "carbon.super";
    private Subscription subscription;

    @BeforeMethod
    public void setUp() {

        service = SubscriptionManagementServiceImpl.getInstance();

        // Clean up singleton's state
        SubscriptionManagementComponentServiceHolder holder =
                SubscriptionManagementComponentServiceHolder.getInstance();
        List<EventSubscriber> toRemove = new ArrayList<>(holder.getEventSubscribers());
        for (EventSubscriber subscriber : toRemove) {
            holder.removeEventSubscriber(subscriber);
        }

        // Mock EventSubscriber
        eventSubscriber = mock(EventSubscriber.class);
        when(eventSubscriber.getAssociatedAdapter()).thenReturn(ADAPTER);

        // Add mock to holder
        holder.addEventSubscriber(eventSubscriber);

        // Mock requests and response
        subscriptionRequest = mock(WebhookSubscriptionRequest.class);
        unsubscriptionRequest = mock(WebhookUnsubscriptionRequest.class);
        subscription = mock(Subscription.class);
    }

    @Test
    public void testSubscribeSuccess() throws Exception {

        List<Subscription> expected = Collections.singletonList(subscription);
        when(eventSubscriber.subscribe(subscriptionRequest, TENANT_DOMAIN)).thenReturn(expected);

        List<Subscription> result = service.subscribe(subscriptionRequest, ADAPTER, TENANT_DOMAIN);
        assertEquals(result, expected);
        verify(eventSubscriber).subscribe(subscriptionRequest, TENANT_DOMAIN);
    }

    @Test
    public void testUnsubscribeSuccess() throws Exception {

        List<Subscription> expected = Collections.singletonList(subscription);
        when(eventSubscriber.unsubscribe(unsubscriptionRequest, TENANT_DOMAIN)).thenReturn(expected);

        List<Subscription> result = service.unsubscribe(unsubscriptionRequest, ADAPTER, TENANT_DOMAIN);
        assertEquals(result, expected);
        verify(eventSubscriber).unsubscribe(unsubscriptionRequest, TENANT_DOMAIN);
    }

    @Test(expectedExceptions = SubscriptionManagementException.class)
    public void testSubscribeNoSubscribers() throws Exception {
        // Remove all subscribers
        SubscriptionManagementComponentServiceHolder holder =
                SubscriptionManagementComponentServiceHolder.getInstance();
        List<EventSubscriber> toRemove = new ArrayList<>(holder.getEventSubscribers());
        for (EventSubscriber subscriber : toRemove) {
            holder.removeEventSubscriber(subscriber);
        }
        service.subscribe(subscriptionRequest, ADAPTER, TENANT_DOMAIN);
    }

    @Test(expectedExceptions = SubscriptionManagementException.class)
    public void testSubscribeAdapterNotFound() throws Exception {
        // Remove the correct subscriber and add a wrong one
        SubscriptionManagementComponentServiceHolder holder =
                SubscriptionManagementComponentServiceHolder.getInstance();
        List<EventSubscriber> toRemove = new ArrayList<>(holder.getEventSubscribers());
        for (EventSubscriber subscriber : toRemove) {
            holder.removeEventSubscriber(subscriber);
        }
        EventSubscriber otherSubscriber = mock(EventSubscriber.class);
        when(otherSubscriber.getAssociatedAdapter()).thenReturn("OtherAdapter");
        holder.addEventSubscriber(otherSubscriber);

        service.subscribe(subscriptionRequest, ADAPTER, TENANT_DOMAIN);
    }
}
