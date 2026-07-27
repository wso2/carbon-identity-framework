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
import org.wso2.carbon.identity.secret.mgt.core.SecretResolveManager;
import org.wso2.carbon.identity.subscription.management.api.service.SubscriptionManagementService;
import org.wso2.carbon.identity.topic.management.api.service.TopicManagementService;
import org.wso2.carbon.identity.webhook.management.internal.component.WebhookManagementComponentServiceHolder;
import org.wso2.carbon.identity.webhook.metadata.api.model.Adapter;
import org.wso2.carbon.identity.webhook.metadata.api.model.AdapterType;
import org.wso2.carbon.identity.webhook.metadata.api.service.EventAdapterMetadataService;
import org.wso2.carbon.identity.webhook.metadata.api.service.WebhookMetadataService;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;

public class WebhookManagementComponentServiceHolderTest {

    private WebhookManagementComponentServiceHolder holder;

    @BeforeMethod
    public void setUp() {

        holder = WebhookManagementComponentServiceHolder.getInstance();

        // Reset all services to null before each test
        holder.setSecretManager(null);
        holder.setSecretResolveManager(null);
        holder.setTopicManagementService(null);
        holder.setSubscriptionManagementService(null);
        holder.setWebhookMetadataService(null);
        holder.setEventAdapterMetadataService(null);
        Adapter adapter = Mockito.mock(Adapter.class);
        Mockito.when(adapter.getType()).thenReturn(AdapterType.Publisher);
        holder.setWebhookAdapter(adapter);
    }

    @Test
    public void testSecretManager() {

        assertNull(holder.getSecretManager());
        SecretManager secretManager = Mockito.mock(SecretManager.class);
        holder.setSecretManager(secretManager);
        assertEquals(holder.getSecretManager(), secretManager);
    }

    @Test
    public void testSecretResolveManager() {

        assertNull(holder.getSecretResolveManager());
        SecretResolveManager secretResolveManager = Mockito.mock(SecretResolveManager.class);
        holder.setSecretResolveManager(secretResolveManager);
        assertEquals(holder.getSecretResolveManager(), secretResolveManager);
    }

    @Test
    public void testTopicManagementService() {

        assertNull(holder.getTopicManagementService());
        TopicManagementService topicManagementService = Mockito.mock(TopicManagementService.class);
        holder.setTopicManagementService(topicManagementService);
        assertEquals(holder.getTopicManagementService(), topicManagementService);
    }

    @Test
    public void testSubscriptionManagementService() {

        assertNull(holder.getSubscriptionManagementService());
        SubscriptionManagementService subscriptionManagementService = Mockito.mock(SubscriptionManagementService.class);
        holder.setSubscriptionManagementService(subscriptionManagementService);
        assertEquals(holder.getSubscriptionManagementService(), subscriptionManagementService);
    }

    @Test
    public void testWebhookMetadataService() {

        assertNull(holder.getWebhookMetadataService());
        WebhookMetadataService webhookMetadataService = Mockito.mock(WebhookMetadataService.class);
        holder.setWebhookMetadataService(webhookMetadataService);
        assertEquals(holder.getWebhookMetadataService(), webhookMetadataService);
    }

    @Test
    public void testEventAdapterMetadataService() {

        assertNull(holder.getEventAdapterMetadataService());
        EventAdapterMetadataService eventAdapterMetadataService = Mockito.mock(EventAdapterMetadataService.class);
        holder.setEventAdapterMetadataService(eventAdapterMetadataService);
        assertEquals(holder.getEventAdapterMetadataService(), eventAdapterMetadataService);
    }

    @Test
    public void testWebhookAdapter() {

        Adapter webhookAdapter = Mockito.mock(Adapter.class);
        holder.setWebhookAdapter(webhookAdapter);
        assertEquals(holder.getWebhookAdapter(), webhookAdapter);
    }
}
