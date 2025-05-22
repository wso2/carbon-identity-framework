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

package org.wso2.carbon.identity.webhook.management.internal.component;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.webhook.management.api.service.EventSubscriber;
import org.wso2.carbon.identity.webhook.management.api.service.EventSubscriberService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Webhook Management Component Service Holder.
 * This class holds references to services required by the webhook management component.
 */
public class WebhookManagementComponentServiceHolder {

    private static final Log LOG = LogFactory.getLog(WebhookManagementComponentServiceHolder.class);
    private static final WebhookManagementComponentServiceHolder INSTANCE =
            new WebhookManagementComponentServiceHolder();
    private List<EventSubscriber> eventSubscribers = new ArrayList<>();
    private EventSubscriberService eventSubscriberService;

    private WebhookManagementComponentServiceHolder() {

    }

    public static WebhookManagementComponentServiceHolder getInstance() {

        return INSTANCE;
    }

    /**
     * Get all registered webhook subscribers.
     *
     * @return List of WebhookSubscriber instances.
     */
    public List<EventSubscriber> getEventSubscribers() {

        return Collections.unmodifiableList(eventSubscribers);
    }

    /**
     * Add an event subscriber.
     *
     * @param eventSubscriber EventSubscriber instance to add.
     */
    public void addEventSubscriber(EventSubscriber eventSubscriber) {

        LOG.info("Adding webhook subscriber: " + eventSubscriber.getName());
        eventSubscribers.add(eventSubscriber);
    }

    /**
     * Remove an event subscriber.
     *
     * @param eventSubscriber EventSubscriber instance to remove.
     */
    public void removeEventSubscriber(EventSubscriber eventSubscriber) {

        LOG.info("Removing event subscriber: " + eventSubscriber.getName());
        eventSubscribers.remove(eventSubscriber);
    }

    /**
     * Get the EventSubscriberService.
     *
     * @return EventSubscriberService instance.
     */
    public EventSubscriberService getEventSubscriberService() {

        return eventSubscriberService;
    }

    /**
     * Set the EventSubscriberService.
     *
     * @param eventSubscriberService EventSubscriberService instance.
     */
    public void setEventSubscriberService(EventSubscriberService eventSubscriberService) {

        this.eventSubscriberService = eventSubscriberService;
    }
}
