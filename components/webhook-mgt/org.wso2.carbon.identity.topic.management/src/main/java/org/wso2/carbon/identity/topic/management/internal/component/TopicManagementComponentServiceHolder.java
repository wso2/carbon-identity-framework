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

package org.wso2.carbon.identity.topic.management.internal.component;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.topic.management.api.service.TopicManager;
import org.wso2.carbon.identity.webhook.metadata.api.model.Adapter;
import org.wso2.carbon.identity.webhook.metadata.api.service.EventAdapterMetadataService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Topic Management Component Service Holder.
 * This class holds references to services required by the topic management component.
 */
public class TopicManagementComponentServiceHolder {

    private static final Log LOG = LogFactory.getLog(TopicManagementComponentServiceHolder.class);
    private static final TopicManagementComponentServiceHolder INSTANCE =
            new TopicManagementComponentServiceHolder();
    private final List<TopicManager> topicManagers = new ArrayList<>();
    private EventAdapterMetadataService eventAdapterMetadataService;
    private Adapter webhookAdapter;

    private TopicManagementComponentServiceHolder() {

    }

    public static TopicManagementComponentServiceHolder getInstance() {

        return INSTANCE;
    }

    /**
     * Get all registered topic managers.
     *
     * @return List of TopicManager instances.
     */
    public List<TopicManager> getTopicManagers() {

        return Collections.unmodifiableList(topicManagers);
    }

    /**
     * Add an event subscriber.
     *
     * @param topicManager TopicManager instance to add.
     */
    public void addTopicManager(TopicManager topicManager) {

        LOG.debug("Adding topic manager: " + topicManager.getAssociatedAdapter());
        topicManagers.add(topicManager);
    }

    /**
     * Remove a topic manager.
     *
     * @param topicManager TopicManager instance to remove.
     */
    public void removeTopicManager(TopicManager topicManager) {

        LOG.debug("Removing topic manager: " + topicManager.getAssociatedAdapter());
        topicManagers.remove(topicManager);
    }

    /**
     * Get the event adapter metadata service.
     *
     * @return EventAdapterMetadataService instance.
     */
    public EventAdapterMetadataService getEventAdapterMetadataService() {

        return eventAdapterMetadataService;
    }

    /**
     * Set the event adapter metadata service.
     *
     * @param eventAdapterMetadataService EventAdapterMetadataService instance.
     */
    public void setEventAdapterMetadataService(EventAdapterMetadataService eventAdapterMetadataService) {

        this.eventAdapterMetadataService = eventAdapterMetadataService;
    }

    /**
     * Get the webhook adapter.
     *
     * @return WebhookAdapter instance.
     */
    public Adapter getWebhookAdapter() {

        return webhookAdapter;
    }

    /**
     * Set the webhook adapter.
     *
     * @param webhookAdapter WebhookAdapter instance.
     */
    public void setWebhookAdapter(Adapter webhookAdapter) {

        this.webhookAdapter = webhookAdapter;
        LOG.debug("Webhook adapter set to type " + webhookAdapter.getType());
    }
}
