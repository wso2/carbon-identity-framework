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
import org.wso2.carbon.identity.event.publisher.api.exception.EventPublisherException;
import org.wso2.carbon.identity.event.publisher.api.model.EventContext;
import org.wso2.carbon.identity.event.publisher.api.model.SecurityEventTokenPayload;
import org.wso2.carbon.identity.event.publisher.api.service.EventPublisher;
import org.wso2.carbon.identity.event.publisher.api.service.EventPublisherService;
import org.wso2.carbon.identity.event.publisher.internal.component.EventPublisherComponentServiceHolder;
import org.wso2.carbon.identity.event.publisher.internal.constant.ErrorMessage;
import org.wso2.carbon.identity.event.publisher.internal.util.EventPublisherExceptionHandler;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Implementation of the EventPublisherService interface.
 * This class provides implementation for event publisher operations.
 */
public class EventPublisherServiceImpl implements EventPublisherService {

    private static final Log log = LogFactory.getLog(EventPublisherServiceImpl.class);
    private static final EventPublisherServiceImpl eventPublisherServiceImpl = new EventPublisherServiceImpl();
    private static final int THREAD_POOL_SIZE = 10;
    private static final ExecutorService executorService = Executors.newFixedThreadPool(THREAD_POOL_SIZE);
    //TODO: Get the topic manager name from a configuration
    private static final String ADAPTOR = "webSubHubAdapter";

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

        EventPublisher adaptorManager = retrieveAdaptorManager(ADAPTOR);

        log.debug("Invoking registered event publisher: " + adaptorManager.getClass().getName());
        CompletableFuture.runAsync(() -> {
            try {
                adaptorManager.publish(eventPayload, eventContext);
            } catch (EventPublisherException e) {
                log.error("Error while publishing event with publisher: " +
                        adaptorManager.getClass().getName(), e);
            }
        }, executorService).exceptionally(ex -> {
            log.error("Error occurred in async event publishing: " + ex.getMessage(), ex);
            return null;
        });
    }

    @Override
    public void canHandleEvent(EventContext eventContext) throws EventPublisherException {

        EventPublisher adaptorManager = retrieveAdaptorManager(ADAPTOR);

        log.debug("Invoking canHandle method of event publisher: " + adaptorManager.getClass().getName());
        try {
            adaptorManager.canHandleEvent(eventContext);
        } catch (EventPublisherException e) {
            log.error("Error while checking if the event can be handled by publisher: " +
                    adaptorManager.getClass().getName(), e);
        }
    }

    private EventPublisher retrieveAdaptorManager(String adaptor) throws EventPublisherException {

        List<EventPublisher> managers =
                EventPublisherComponentServiceHolder.getInstance().getEventPublishers();

        for (EventPublisher manager : managers) {
            if (adaptor.equals(manager.getAssociatedAdaptor())) {
                return manager;
            }
        }

        throw EventPublisherExceptionHandler.handleServerException(ErrorMessage.ERROR_CODE_EVENT_PUBLISHER_NOT_FOUND);
    }
}
