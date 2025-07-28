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

package org.wso2.carbon.identity.subscription.management.internal;

import org.mockito.Mockito;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.subscription.management.api.service.EventSubscriber;
import org.wso2.carbon.identity.subscription.management.internal.component.SubscriptionManagementComponentServiceHolder;

import java.util.ArrayList;
import java.util.List;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;

public class SubscriptionManagementComponentServiceHolderTest {

    private SubscriptionManagementComponentServiceHolder holder;
    private EventSubscriber eventSubscriber;

    @BeforeMethod
    public void setUp() {

        holder = SubscriptionManagementComponentServiceHolder.getInstance();

        // Clear all existing subscribers first
        List<EventSubscriber> toRemove = new ArrayList<>(holder.getEventSubscribers());
        for (EventSubscriber subscriber : toRemove) {
            holder.removeEventSubscriber(subscriber);
        }

        // Create a fresh mock for each test
        eventSubscriber = Mockito.mock(EventSubscriber.class);
        Mockito.when(eventSubscriber.getAssociatedAdapter()).thenReturn("TestAdapter");
    }

    @Test
    public void testAddEventSubscriber() {
        // Make sure list is empty first
        assertEquals(holder.getEventSubscribers().size(), 0);

        // Add the subscriber
        holder.addEventSubscriber(eventSubscriber);

        // Verify it was added
        List<EventSubscriber> subscribers = holder.getEventSubscribers();
        assertEquals(subscribers.size(), 1);
        assertEquals(eventSubscriber, subscribers.get(0));
    }

    @Test
    public void testRemoveEventSubscriber() {

        holder.addEventSubscriber(eventSubscriber);
        holder.removeEventSubscriber(eventSubscriber);
        List<EventSubscriber> subscribers = holder.getEventSubscribers();
        assertFalse(subscribers.contains(eventSubscriber));
    }

    @Test
    public void testGetEventSubscribers() {

        holder.addEventSubscriber(eventSubscriber);
        List<EventSubscriber> subscribers = holder.getEventSubscribers();
        assertEquals(subscribers.size(), 1);
        assertEquals(subscribers.get(0), eventSubscriber);
    }
}
