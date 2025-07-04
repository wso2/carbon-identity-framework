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
    public List<Subscription> subscribe(WebhookSubscriptionRequest subscriptionRequest, String adaptor,
                                        String tenantDomain)
            throws SubscriptionManagementException {

        EventSubscriber subscriber = retrieveAdaptorManager(adaptor);
        return subscriber.subscribe(subscriptionRequest, tenantDomain);
    }

    @Override
    public List<Subscription> unsubscribe(WebhookUnsubscriptionRequest unsubscriptionRequest, String adaptor,
                                          String tenantDomain)
            throws SubscriptionManagementException {

        EventSubscriber subscriber = retrieveAdaptorManager(adaptor);
        return subscriber.unsubscribe(unsubscriptionRequest, tenantDomain);
    }

    private EventSubscriber retrieveAdaptorManager(String adaptor) throws SubscriptionManagementException {

        List<EventSubscriber> subscribers =
                SubscriptionManagementComponentServiceHolder.getInstance().getEventSubscribers();

        if (subscribers.isEmpty()) {
            throw SubscriptionManagementExceptionHandler.handleServerException(
                    ErrorMessage.ERROR_CODE_WEBHOOK_SUBSCRIBERS_NOT_FOUND);
        }

        for (EventSubscriber subscriber : subscribers) {
            if (adaptor.equals(subscriber.getAssociatedAdaptor())) {
                return subscriber;
            }
        }

        throw SubscriptionManagementExceptionHandler.handleServerException(
                ErrorMessage.ERROR_CODE_WEBHOOK_SUBSCRIBER_NOT_FOUND, adaptor);
    }
}
