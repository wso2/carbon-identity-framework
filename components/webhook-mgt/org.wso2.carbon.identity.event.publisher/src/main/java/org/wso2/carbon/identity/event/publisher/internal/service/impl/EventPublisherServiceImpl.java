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
import org.wso2.carbon.identity.event.publisher.api.exception.EventPublisherException;
import org.wso2.carbon.identity.event.publisher.api.model.EventContext;
import org.wso2.carbon.identity.event.publisher.api.model.SecurityEventTokenPayload;
import org.wso2.carbon.identity.event.publisher.api.service.EventPublisher;
import org.wso2.carbon.identity.event.publisher.api.service.EventPublisherService;
import org.wso2.carbon.identity.event.publisher.internal.component.EventPublisherComponentServiceHolder;

import java.util.ArrayList;
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

        for (EventPublisher publisher : retrieveActivePublishers()) {
            log.debug("Invoking registered event publisher: " + publisher.getClass().getName());
            publisher.publish(eventPayload, eventContext);
        }
    }

    @Override
    public boolean canHandleEvent(EventContext eventContext) throws EventPublisherException {

        for (EventPublisher publisher : retrieveActivePublishers()) {
            log.debug("Invoking canHandle method of event publisher: " + publisher.getClass().getName());
            try {
                if (publisher.canHandleEvent(eventContext)) {
                    return true;
                }
            } catch (EventPublisherException e) {
                log.error("Error while checking if the event can be handled by publisher: " +
                        publisher.getClass().getName(), e);
            }
        }
        return false;
    }

    /**
     * Retrieve all registered event publishers associated with the currently active webhook adapter.
     *
     * @return List of event publishers associated with the active adapter.
     */
    private List<EventPublisher> retrieveActivePublishers() {

        List<EventPublisher> activePublishers = new ArrayList<>();
        for (EventPublisher publisher : EventPublisherComponentServiceHolder.getInstance().getEventPublishers()) {
            if (webhookAdapter.equals(publisher.getAssociatedAdapter())) {
                activePublishers.add(publisher);
            }
        }
        return activePublishers;
    }
}
