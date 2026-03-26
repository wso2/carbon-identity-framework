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

package org.wso2.carbon.identity.event.publisher.internal.service.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.annotation.bundle.Capability;
import org.wso2.carbon.identity.event.publisher.api.constant.ErrorMessage;
import org.wso2.carbon.identity.event.publisher.api.exception.EventPublisherException;
import org.wso2.carbon.identity.event.publisher.api.model.EventContext;
import org.wso2.carbon.identity.event.publisher.api.model.SecurityEventTokenPayload;
import org.wso2.carbon.identity.event.publisher.api.service.EventPublisher;
import org.wso2.carbon.identity.event.publisher.api.service.EventPublisherService;
import org.wso2.carbon.identity.event.publisher.internal.component.EventPublisherComponentServiceHolder;
import org.wso2.carbon.identity.event.publisher.internal.util.EventPublisherExceptionHandler;

import java.util.List;

/**
 * Implementation of the EventPublisherService interface.
 * This class provides implementation for event publisher operations.
 */
@Capability(
        namespace = "osgi.service",
        attribute = {
                "objectClass=org.wso2.carbon.identity.event.publisher.api.service.EventPublisherService",
                "service.scope=singleton"
        }
)
public class EventPublisherServiceImpl implements EventPublisherService {

    private static final Log log = LogFactory.getLog(EventPublisherServiceImpl.class);
    private static final EventPublisherServiceImpl eventPublisherServiceImpl = new EventPublisherServiceImpl();
    private final String webhookAdapter;

    private EventPublisherServiceImpl() {

        webhookAdapter = EventPublisherComponentServiceHolder.getInstance()
                .getWebhookAdapter().getName();
    }

    /**
     * Private constructor to prevent instantiation.
     * Use getInstance() method to get the singleton instance.
     */
    public static EventPublisherServiceImpl getInstance() {

        return eventPublisherServiceImpl;
    }

    @Override
    public void publish(SecurityEventTokenPayload eventPayload, EventContext eventContext)
            throws EventPublisherException {

        EventPublisher adapterManager = retrieveAdapterManager(webhookAdapter);

        log.debug("Invoking registered event publisher: " + adapterManager.getClass().getName());
        adapterManager.publish(eventPayload, eventContext);
    }

    @Override
    public boolean canHandleEvent(EventContext eventContext) throws EventPublisherException {

        EventPublisher adapterManager = retrieveAdapterManager(webhookAdapter);

        log.debug("Invoking canHandle method of event publisher: " + adapterManager.getClass().getName());
        try {
            return adapterManager.canHandleEvent(eventContext);
        } catch (EventPublisherException e) {
            log.error("Error while checking if the event can be handled by publisher: " +
                    adapterManager.getClass().getName(), e);
        }
        return false;
    }

    private EventPublisher retrieveAdapterManager(String adapter) throws EventPublisherException {

        List<EventPublisher> managers =
                EventPublisherComponentServiceHolder.getInstance().getEventPublishers();

        for (EventPublisher manager : managers) {
            if (adapter.equals(manager.getAssociatedAdapter())) {
                return manager;
            }
        }

        throw EventPublisherExceptionHandler.handleServerException(ErrorMessage.ERROR_CODE_EVENT_PUBLISHER_NOT_FOUND);
    }
}
