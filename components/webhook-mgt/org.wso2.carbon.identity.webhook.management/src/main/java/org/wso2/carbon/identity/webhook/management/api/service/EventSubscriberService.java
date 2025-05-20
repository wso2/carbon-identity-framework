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

package org.wso2.carbon.identity.webhook.management.api.service;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.webhook.management.api.constant.ErrorMessage;
import org.wso2.carbon.identity.webhook.management.api.exception.WebhookMgtException;
import org.wso2.carbon.identity.webhook.management.api.model.Webhook;
import org.wso2.carbon.identity.webhook.management.internal.component.WebhookManagementComponentServiceHolder;
import org.wso2.carbon.identity.webhook.management.internal.util.WebhookManagementExceptionHandler;

import java.util.List;

/**
 * Service implementation for event subscription operations.
 * This class manages event subscribers and delegates subscription operations to appropriate subscribers.
 */
public class EventSubscriberService {

    private static final Log LOG = LogFactory.getLog(EventSubscriberService.class);

    /**
     * Subscribe an event with external systems.
     * This method delegates to appropriate event subscribers.
     *
     * @param webhook      The webhook to subscribe.
     * @param tenantDomain Tenant domain.
     * @throws WebhookMgtException If an error occurs during subscription.
     */
    public void subscribe(Webhook webhook, String tenantDomain) throws WebhookMgtException {

        List<EventSubscriber> subscribers =
                WebhookManagementComponentServiceHolder.getInstance().getEventSubscribers();
        if (subscribers.isEmpty()) {
            throw WebhookManagementExceptionHandler.handleServerException(
                    ErrorMessage.ERROR_CODE_WEBHOOK_SUBSCRIBERS_NOT_FOUND, tenantDomain);
        }

        for (EventSubscriber subscriber : subscribers) {
            try {
                boolean success =
                        subscriber.subscribe(webhook.getEventsSubscribed(), webhook.getEndpoint(), tenantDomain);
                if (!success) {
                    LOG.error(
                            "Subscriber " + subscriber.getName() + " failed to subscribe webhook: " + webhook.getId());
                }
            } catch (WebhookMgtException e) {
                throw WebhookManagementExceptionHandler.handleServerException(
                        ErrorMessage.ERROR_CODE_WEBHOOK_SUBSCRIPTION_ERROR, webhook.getId());
            }
        }
    }

    /**
     * Unsubscribe an event from external systems.
     * This method delegates to appropriate event subscribers.
     *
     * @param webhook      The webhook to unsubscribe.
     * @param tenantDomain Tenant domain.
     * @throws WebhookMgtException If an error occurs during unsubscription.
     */
    public void unsubscribe(Webhook webhook, String tenantDomain) throws WebhookMgtException {

        List<EventSubscriber> subscribers =
                WebhookManagementComponentServiceHolder.getInstance().getEventSubscribers();
        if (subscribers.isEmpty()) {
            throw WebhookManagementExceptionHandler.handleServerException(
                    ErrorMessage.ERROR_CODE_WEBHOOK_SUBSCRIBERS_NOT_FOUND, tenantDomain);
        }
        for (EventSubscriber subscriber : subscribers) {
            try {
                boolean success =
                        subscriber.unsubscribe(webhook.getEventsSubscribed(), webhook.getEndpoint(), tenantDomain);
                if (!success) {
                    LOG.warn("Subscriber " + subscriber.getName() + " failed to unsubscribe webhook: " +
                            webhook.getId());
                }
            } catch (WebhookMgtException e) {
                throw WebhookManagementExceptionHandler.handleServerException(
                        ErrorMessage.ERROR_CODE_WEBHOOK_UNSUBSCRIPTION_ERROR, webhook.getId());
            }
        }
    }
}
