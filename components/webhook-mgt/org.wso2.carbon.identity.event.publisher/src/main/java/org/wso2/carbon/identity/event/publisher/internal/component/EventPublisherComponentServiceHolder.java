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

package org.wso2.carbon.identity.event.publisher.internal.component;

import org.wso2.carbon.identity.event.publisher.api.service.EventPublisher;
import org.wso2.carbon.identity.webhook.metadata.api.model.Adapter;
import org.wso2.carbon.identity.webhook.metadata.api.service.EventAdapterMetadataService;

import java.util.ArrayList;
import java.util.List;

/**
 * Event publisher Component Service Holder.
 * This class holds references to services required by the event publisher component.
 */
public class EventPublisherComponentServiceHolder {

    private static final EventPublisherComponentServiceHolder INSTANCE =
            new EventPublisherComponentServiceHolder();
    private List<EventPublisher> eventPublishers = new ArrayList<>();
    private EventAdapterMetadataService eventAdapterMetadataService;
    private Adapter webhookAdapter;

    private EventPublisherComponentServiceHolder() {

    }

    public static EventPublisherComponentServiceHolder getInstance() {

        return INSTANCE;
    }

    /**
     * Get the list of event publishers.
     *
     * @return List of event publishers.
     */
    public List<EventPublisher> getEventPublishers() {

        return eventPublishers;
    }

    /**
     * Add event publisher implementation.
     *
     * @param eventPublisher Event publisher implementation.
     */
    public void addEventPublisher(EventPublisher eventPublisher) {

        eventPublishers.add(eventPublisher);
    }

    /**
     * Remove event publisher implementation.
     *
     * @param eventPublisher Event publisher implementation.
     */
    public void removeEventPublisher(EventPublisher eventPublisher) {

        eventPublishers.remove(eventPublisher);
    }

    /**
     * Set a list of event publishers.
     *
     * @param eventPublishers List of event publishers.
     */
    public void setEventPublishers(List<EventPublisher> eventPublishers) {

        this.eventPublishers = eventPublishers;
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
    }
}
