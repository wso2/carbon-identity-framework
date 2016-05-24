/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.identity.event.services;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.wso2.carbon.identity.event.EventDistributionTask;
import org.wso2.carbon.identity.event.EventMgtException;
import org.wso2.carbon.identity.event.bean.IdentityEventMessageContext;
import org.wso2.carbon.identity.event.event.Event;
import org.wso2.carbon.identity.event.handler.AbstractEventHandler;
import org.wso2.carbon.identity.event.internal.EventMgtServiceComponent;

import java.util.HashMap;
import java.util.Map;
import java.util.List;

public class EventMgtServiceImpl implements EventMgtService {

    private static final Log log = LogFactory.getLog(EventMgtServiceImpl.class);
    private EventDistributionTask eventDistributionTask;

    public EventMgtServiceImpl (List<AbstractEventHandler> handlerList, int threadPoolSize) {
        this.eventDistributionTask = new EventDistributionTask(handlerList, threadPoolSize);
        if (log.isDebugEnabled()) {
            log.debug("Starting event distribution task from Notification Management component");
        }
        new Thread(eventDistributionTask).start();
    }
    @Override
    public boolean handleEvent(Event event) throws EventMgtException {

        List<AbstractEventHandler> eventHandlerList = EventMgtServiceComponent.eventHandlerList;
        Map<String, Event> eventMap = new HashMap<>();
        eventMap.put("Event", event);
        IdentityEventMessageContext eventContext = new IdentityEventMessageContext(eventMap);
        boolean returnValue = true;
        for (final AbstractEventHandler handler : eventHandlerList) {

            if (handler.canHandle(eventContext)) {
                if (handler.isAssociationAsync(event.getEventName())) {
                    eventDistributionTask.addEventToQueue(event);
                } else {
                    returnValue = handler.handleEvent(event);
                }
            }
        }
        return returnValue;
    }
}
