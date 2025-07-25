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

package org.wso2.carbon.identity.subscription.management.internal.component;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.subscription.management.api.service.EventSubscriber;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Subscription Management Component Service Holder.
 * This class holds references to services required by the subscription management component.
 */
public class SubscriptionManagementComponentServiceHolder {

    private static final Log LOG = LogFactory.getLog(SubscriptionManagementComponentServiceHolder.class);
    private static final SubscriptionManagementComponentServiceHolder INSTANCE =
            new SubscriptionManagementComponentServiceHolder();
    private List<EventSubscriber> eventSubscribers = new ArrayList<>();

    private SubscriptionManagementComponentServiceHolder() {

    }

    public static SubscriptionManagementComponentServiceHolder getInstance() {

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

        LOG.debug("Adding webhook subscriber: " + eventSubscriber.getAssociatedAdapter());
        eventSubscribers.add(eventSubscriber);
    }

    /**
     * Remove an event subscriber.
     *
     * @param eventSubscriber EventSubscriber instance to remove.
     */
    public void removeEventSubscriber(EventSubscriber eventSubscriber) {

        LOG.debug("Removing event subscriber: " + eventSubscriber.getAssociatedAdapter());
        eventSubscribers.remove(eventSubscriber);
    }
}
