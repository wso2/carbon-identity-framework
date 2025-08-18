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

package org.wso2.carbon.identity.event.publisher.api.service;

import org.wso2.carbon.identity.event.publisher.api.exception.EventPublisherException;
import org.wso2.carbon.identity.event.publisher.api.model.EventContext;
import org.wso2.carbon.identity.event.publisher.api.model.SecurityEventTokenPayload;

/**
 * The EventPublisher interface.
 */
public interface EventPublisher {

    /**
     * Retrieves the name of the event publisher.
     *
     * @return Name of the event publisher.
     */
    String getAssociatedAdapter();

    /**
     * Publish a given event to the intermediate hub.
     *
     * @param payload      Event payload.
     * @param eventContext Event Context.
     * @throws EventPublisherException If an error occurs while publishing the event.
     */
    void publish(SecurityEventTokenPayload payload, EventContext eventContext) throws EventPublisherException;

    /**
     * Check whether the event publisher can handle the given event context.
     *
     * @param eventContext Event Context.
     * @throws EventPublisherException If the event publisher cannot handle the event context.
     */
    boolean canHandleEvent(EventContext eventContext) throws EventPublisherException;
}
