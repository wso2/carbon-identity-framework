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

package org.wso2.carbon.identity.subscription.management.internal.service.impl;

import org.osgi.annotation.bundle.Capability;
import org.wso2.carbon.identity.subscription.management.api.exception.SubscriptionManagementException;
import org.wso2.carbon.identity.subscription.management.api.model.Subscription;
import org.wso2.carbon.identity.subscription.management.api.model.WebhookSubscriptionRequest;
import org.wso2.carbon.identity.subscription.management.api.model.WebhookUnsubscriptionRequest;
import org.wso2.carbon.identity.subscription.management.api.service.EventSubscriber;
import org.wso2.carbon.identity.subscription.management.api.service.SubscriptionManagementService;
import org.wso2.carbon.identity.subscription.management.internal.component.SubscriptionManagementComponentServiceHolder;
import org.wso2.carbon.identity.subscription.management.internal.constant.ErrorMessage;
import org.wso2.carbon.identity.subscription.management.internal.util.SubscriptionManagementExceptionHandler;

import java.util.List;

/**
 * Implementation of the SubscriptionManagementService interface.
 * This class provides implementation for subscription management operations.
 */
@Capability(
        namespace = "osgi.service",
        attribute = {
                "objectClass=org.wso2.carbon.identity.subscription.management.api.service." +
                        "SubscriptionManagementService",
                "service.scope=singleton"
        }
)
public class SubscriptionManagementServiceImpl implements SubscriptionManagementService {

    private static final SubscriptionManagementServiceImpl subscriptionManagementServiceImpl =
            new SubscriptionManagementServiceImpl();

    /**
     * Singleton instance of SubscriptionManagementServiceImpl.
     *
     * @return Singleton instance.
     */
    public static SubscriptionManagementServiceImpl getInstance() {

        return subscriptionManagementServiceImpl;
    }

    @Override
    public List<Subscription> subscribe(WebhookSubscriptionRequest subscriptionRequest, String adapter,
                                        String tenantDomain)
            throws SubscriptionManagementException {

        EventSubscriber subscriber = retrieveAdapterManager(adapter);
        return subscriber.subscribe(subscriptionRequest, tenantDomain);
    }

    @Override
    public List<Subscription> unsubscribe(WebhookUnsubscriptionRequest unsubscriptionRequest, String adapter,
                                          String tenantDomain)
            throws SubscriptionManagementException {

        EventSubscriber subscriber = retrieveAdapterManager(adapter);
        return subscriber.unsubscribe(unsubscriptionRequest, tenantDomain);
    }

    private EventSubscriber retrieveAdapterManager(String adapter) throws SubscriptionManagementException {

        List<EventSubscriber> subscribers =
                SubscriptionManagementComponentServiceHolder.getInstance().getEventSubscribers();

        if (subscribers.isEmpty()) {
            throw SubscriptionManagementExceptionHandler.handleServerException(
                    ErrorMessage.ERROR_CODE_WEBHOOK_SUBSCRIBERS_NOT_FOUND);
        }

        for (EventSubscriber subscriber : subscribers) {
            if (adapter.equals(subscriber.getAssociatedAdapter())) {
                return subscriber;
            }
        }

        throw SubscriptionManagementExceptionHandler.handleServerException(
                ErrorMessage.ERROR_CODE_WEBHOOK_SUBSCRIBER_NOT_FOUND, adapter);
    }
}
