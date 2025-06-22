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

package org.wso2.carbon.identity.webhook.management.internal;

import org.mockito.Mockito;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.secret.mgt.core.SecretManager;
import org.wso2.carbon.identity.topic.management.api.service.TopicManagementService;
import org.wso2.carbon.identity.webhook.management.api.service.EventSubscriber;
import org.wso2.carbon.identity.webhook.management.internal.component.WebhookManagementComponentServiceHolder;
import org.wso2.carbon.identity.webhook.management.internal.service.impl.EventSubscriberService;
import org.wso2.carbon.identity.webhook.metadata.api.service.WebhookMetadataService;

import java.util.ArrayList;
import java.util.List;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertSame;
import static org.testng.Assert.assertTrue;

public class WebhookManagementComponentServiceHolderTest {

    private WebhookManagementComponentServiceHolder holder;
    private EventSubscriber subscriber;

    @BeforeMethod
    public void setUp() {

        holder = WebhookManagementComponentServiceHolder.getInstance();
        // Copy to avoid ConcurrentModificationException
        List<EventSubscriber> toRemove = new ArrayList<>(holder.getEventSubscribers());
        toRemove.forEach(holder::removeEventSubscriber);
        subscriber = Mockito.mock(EventSubscriber.class);
        Mockito.when(subscriber.getName()).thenReturn("TestSubscriber");
    }

    @Test
    public void testAddEventSubscriber() {

        holder.addEventSubscriber(subscriber);
        List<EventSubscriber> subscribers = holder.getEventSubscribers();
        assertTrue(subscribers.contains(subscriber));
    }

    @Test
    public void testRemoveEventSubscriber() {

        holder.addEventSubscriber(subscriber);
        holder.removeEventSubscriber(subscriber);
        List<EventSubscriber> subscribers = holder.getEventSubscribers();
        assertFalse(subscribers.contains(subscriber));
    }

    @Test
    public void testGetEventSubscribers() {

        holder.addEventSubscriber(subscriber);
        List<EventSubscriber> subscribers = holder.getEventSubscribers();
        assertEquals(subscribers.size(), 1);
        assertEquals(subscribers.get(0), subscriber);
    }

    @Test
    public void testSetAndGetEventSubscriberService() {

        EventSubscriberService eventSubscriberService = Mockito.mock(EventSubscriberService.class);
        holder.setEventSubscriberService(eventSubscriberService);
        assertSame(holder.getEventSubscriberService(), eventSubscriberService);
    }

    @Test
    public void testSetAndGetSecretManager() {

        SecretManager secretManager = Mockito.mock(SecretManager.class);
        holder.setSecretManager(secretManager);
        assertSame(holder.getSecretManager(), secretManager);
    }

    @Test
    public void testSetAndGetTopicManagementService() {

        TopicManagementService topicManagementService = Mockito.mock(TopicManagementService.class);
        holder.setTopicManagementService(topicManagementService);
        assertSame(holder.getTopicManagementService(), topicManagementService);
    }

    @Test
    public void testSetAndGetWebhookMetadataService() {

        WebhookMetadataService webhookMetadataService = Mockito.mock(WebhookMetadataService.class);
        holder.setWebhookMetadataService(webhookMetadataService);
        assertSame(holder.getWebhookMetadataService(), webhookMetadataService);
    }
}
