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
import org.wso2.carbon.identity.event.publisher.api.model.EventContext;
import org.wso2.carbon.identity.event.publisher.api.model.SecurityEventTokenPayload;
import org.wso2.carbon.identity.event.publisher.api.service.EventPublisher;
import org.wso2.carbon.identity.event.publisher.api.service.EventPublisherService;
import org.wso2.carbon.identity.event.publisher.internal.component.EventPublisherComponentServiceHolder;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Implementation of the TopicManagementService interface.
 * This class provides implementation for topic management operations.
 */
public class EventPublisherServiceImpl implements EventPublisherService {

    private static final Log log = LogFactory.getLog(EventPublisherServiceImpl.class);
    private static final EventPublisherServiceImpl eventPublisherServiceImpl = new EventPublisherServiceImpl();
    private static final int THREAD_POOL_SIZE = 10;
    private static final ExecutorService executorService = Executors.newFixedThreadPool(THREAD_POOL_SIZE);

    /**
     * Private constructor to prevent instantiation.
     * Use getInstance() method to get the singleton instance.
     */
    public static EventPublisherServiceImpl getInstance() {

        return eventPublisherServiceImpl;
    }

    @Override
    public void publish(SecurityEventTokenPayload eventPayload, EventContext eventContext) {

        List<EventPublisher> eventPublishers =
                EventPublisherComponentServiceHolder.getInstance().getEventPublishers();

        for (EventPublisher eventPublisher : eventPublishers) {
            log.debug("Invoking registered event publisher: " + eventPublisher.getClass().getName());
            CompletableFuture.runAsync(() -> {
                try {
                    eventPublisher.publish(eventPayload, eventContext);
                } catch (Exception e) {
                    log.error("Error while publishing event with publisher: " +
                            eventPublisher.getClass().getName(), e);
                }
            }, executorService).exceptionally(ex -> {
                log.error("Error occurred in async event publishing: " + ex.getMessage(), ex);
                return null;
            });
        }
    }
}
