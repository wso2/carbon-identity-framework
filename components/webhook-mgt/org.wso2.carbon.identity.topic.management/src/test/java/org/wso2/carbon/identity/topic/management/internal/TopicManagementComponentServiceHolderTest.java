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

package org.wso2.carbon.identity.topic.management.internal;

import org.mockito.Mockito;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.topic.management.api.service.TopicManager;
import org.wso2.carbon.identity.topic.management.internal.component.TopicManagementComponentServiceHolder;
import org.wso2.carbon.identity.webhook.metadata.api.model.Adapter;
import org.wso2.carbon.identity.webhook.metadata.api.model.AdapterType;
import org.wso2.carbon.identity.webhook.metadata.api.service.EventAdapterMetadataService;

import java.util.ArrayList;
import java.util.List;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertSame;

public class TopicManagementComponentServiceHolderTest {

    private TopicManagementComponentServiceHolder holder;
    private TopicManager topicManager;

    @BeforeMethod
    public void setUp() {

        holder = TopicManagementComponentServiceHolder.getInstance();

        // Clear all existing managers first
        List<TopicManager> toRemove = new ArrayList<>(holder.getTopicManagers());
        for (TopicManager manager : toRemove) {
            holder.removeTopicManager(manager);
        }

        topicManager = Mockito.mock(TopicManager.class);
        Mockito.when(topicManager.getAssociatedAdapter()).thenReturn("TestTopicManager");
    }

    @Test
    public void testAddTopicManager() {

        assertEquals(holder.getTopicManagers().size(), 0);
        holder.addTopicManager(topicManager);
        List<TopicManager> managers = holder.getTopicManagers();
        assertEquals(managers.size(), 1);
        assertEquals(topicManager, managers.get(0));
    }

    @Test
    public void testRemoveTopicManager() {

        holder.addTopicManager(topicManager);
        holder.removeTopicManager(topicManager);
        List<TopicManager> managers = holder.getTopicManagers();
        assertFalse(managers.contains(topicManager));
    }

    @Test
    public void testGetTopicManagers() {

        holder.addTopicManager(topicManager);
        List<TopicManager> managers = holder.getTopicManagers();
        assertEquals(managers.size(), 1);
        assertEquals(managers.get(0), topicManager);
    }

    @Test
    public void testSetAndGetWebhookAdapter() {

        Adapter adapter = Mockito.mock(Adapter.class);
        Mockito.when(adapter.getType()).thenReturn(AdapterType.Publisher);
        holder.setWebhookAdapter(adapter);
        assertSame(holder.getWebhookAdapter(), adapter);
    }

    @Test
    public void testSetAndGetEventAdapterMetadataService() {

        EventAdapterMetadataService service = Mockito.mock(EventAdapterMetadataService.class);
        holder.setEventAdapterMetadataService(service);
        assertSame(holder.getEventAdapterMetadataService(), service);
    }

    @Test
    public void testDefaultWebhookAdapterAndEventAdapterMetadataServiceAreNull() {

        assertNull(holder.getWebhookAdapter());
        assertNull(holder.getEventAdapterMetadataService());
    }
}
