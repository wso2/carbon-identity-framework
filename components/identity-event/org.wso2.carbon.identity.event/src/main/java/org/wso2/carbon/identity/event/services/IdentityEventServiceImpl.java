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

import org.osgi.annotation.bundle.Capability;
import org.wso2.carbon.identity.event.EventDistributionTask;
import org.wso2.carbon.identity.event.IdentityEventException;
import org.wso2.carbon.identity.event.bean.IdentityEventMessageContext;
import org.wso2.carbon.identity.event.event.Event;
import org.wso2.carbon.identity.event.handler.AbstractEventHandler;
import org.wso2.carbon.identity.event.internal.IdentityEventServiceComponent;

import java.util.List;

@Capability(
        namespace = "osgi.service",
        attribute = {
                "objectClass=org.wso2.carbon.identity.event.services.IdentityEventService",
                "service.scope=singleton"
        }
)
public class IdentityEventServiceImpl implements IdentityEventService {

    private static final Log log = LogFactory.getLog(IdentityEventServiceImpl.class);
    private EventDistributionTask eventDistributionTask;

    public IdentityEventServiceImpl(List<AbstractEventHandler> handlerList, int threadPoolSize) {
        this.eventDistributionTask = new EventDistributionTask(handlerList, threadPoolSize);
        if (log.isDebugEnabled()) {
            log.debug("Starting event distribution task from Notification Management component");
        }
        new Thread(eventDistributionTask).start();
    }
    @Override
    public void handleEvent(Event event) throws IdentityEventException {

        List<AbstractEventHandler> eventHandlerList = IdentityEventServiceComponent.eventHandlerList;
        IdentityEventMessageContext eventContext = new IdentityEventMessageContext(event);
        for (final AbstractEventHandler handler : eventHandlerList) {

            if (handler.canHandle(eventContext)) {
                if (handler.isAssociationAsync(event.getEventName())) {
                    eventDistributionTask.addEventToQueue(event);
                } else {
                    handler.handleEvent(event);
                }
            }
        }
    }
}
