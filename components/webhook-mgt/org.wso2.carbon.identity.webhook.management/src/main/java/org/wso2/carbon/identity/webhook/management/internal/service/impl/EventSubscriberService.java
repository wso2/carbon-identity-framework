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

package org.wso2.carbon.identity.webhook.management.internal.service.impl;

import org.wso2.carbon.identity.webhook.management.api.constant.ErrorMessage;
import org.wso2.carbon.identity.webhook.management.api.exception.WebhookMgtException;
import org.wso2.carbon.identity.webhook.management.api.model.Subscription;
import org.wso2.carbon.identity.webhook.management.api.model.Webhook;
import org.wso2.carbon.identity.webhook.management.api.service.EventSubscriber;
import org.wso2.carbon.identity.webhook.management.internal.component.WebhookManagementComponentServiceHolder;
import org.wso2.carbon.identity.webhook.management.internal.util.WebhookManagementExceptionHandler;

import java.util.List;

/**
 * Service implementation for event subscription operations.
 * This class manages event subscribers and delegates subscription operations to appropriate subscribers.
 */
public class EventSubscriberService {

    /**
     * Subscribe events from external systems.
     * This method delegates to appropriate event subscribers.
     *
     * @param webhook      Webhook to be subscribed.
     * @param adaptor      The name of the adaptor to use for subscription.
     * @return List of subscriptions that were successfully subscribed.
     */
    public List<Subscription> subscribe(Webhook webhook, String adaptor)
            throws WebhookMgtException {

        EventSubscriber subscriber = retrieveAdaptorManager(adaptor);
        return subscriber.subscribe(webhook);
    }

    /**
     * Unsubscribe events from external systems.
     * This method delegates to appropriate event subscribers.
     *
     * @param webhook      Webhook to be unsubscribed.
     * @param adaptor      The name of the adaptor to use for unsubscription.
     * @return List of subscriptions that were successfully unsubscribed.
     */
    public List<Subscription> unsubscribe(Webhook webhook, String adaptor)
            throws WebhookMgtException {

        EventSubscriber subscriber = retrieveAdaptorManager(adaptor);
        return subscriber.unsubscribe(webhook);
    }

    private EventSubscriber retrieveAdaptorManager(String adaptor) throws WebhookMgtException {

        List<EventSubscriber> subscribers =
                WebhookManagementComponentServiceHolder.getInstance().getEventSubscribers();

        if (subscribers.isEmpty()) {
            throw WebhookManagementExceptionHandler.handleServerException(
                    ErrorMessage.ERROR_CODE_WEBHOOK_SUBSCRIBERS_NOT_FOUND);
        }

        for (EventSubscriber subscriber : subscribers) {
            if (adaptor.equals(subscriber.getName())) {
                return subscriber;
            }
        }

        throw WebhookManagementExceptionHandler.handleServerException(
                ErrorMessage.ERROR_CODE_WEBHOOK_SUBSCRIBER_NOT_FOUND, adaptor);
    }
}
