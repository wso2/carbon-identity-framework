/*
 * Copyright (c) 2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
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

package org.wso2.carbon.identity.mgt;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.mgt.event.Event;
import org.wso2.carbon.identity.mgt.handler.EventHandler;
import org.wso2.carbon.identity.mgt.internal.EventMgtServiceDataHolder;

import java.util.List;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingDeque;

/**
 * This has a queue inside. All publishers add events to this queue and this event distribution task is responsible
 * for distributing these events to Notification sending modules
 */
public class EventDistributionTask implements Runnable {

    private static final Log log = LogFactory.getLog(EventDistributionTask.class);

    /**
     * Queue used to add events by publishers.
     */
    private BlockingDeque<Event> eventQueue;
    /**
     * Registered message sending modules.
     */
    private List<EventHandler> notificationSendingModules;
    /**
     * Condition to break event distribution task
     */
    private volatile boolean running;

    /**
     * Overridden constructor to initiate notification sending modules and thread pool size
     *
     * @param notificationSendingModules List of notification sending modules registered
     * @param threadPoolSize             Size of thread pool for notification sending components
     */
    public EventDistributionTask(List<EventHandler> notificationSendingModules, int threadPoolSize) {
        this.notificationSendingModules = notificationSendingModules;
        this.eventQueue = new LinkedBlockingDeque<Event>();
        EventMgtServiceDataHolder.getInstance().setThreadPool(Executors.newFixedThreadPool(threadPoolSize));
    }

    public void addEventToQueue(Event publisherEvent) {
        this.eventQueue.add(publisherEvent);
    }

    @Override
    public void run() {
        running = true;
        // Run forever until stop the bundle. Will stop in eventQueue.take()
        while (running) {
            try {
                final Event event = eventQueue.take();
                for (final EventHandler module : notificationSendingModules) {
                    // If the module is subscribed to the event, module will be executed.
                    try {
                        if (module.isRegistered(event)) {
                            // Create a runnable and submit to the thread pool for sending message.
                            Runnable msgSender = new Runnable() {
                                @Override
                                public void run() {
                                    if (log.isDebugEnabled()) {
                                        log.debug("Executing " + module.getModuleName() + " on event" + event.
                                                getEventName());
                                    }
                                    try {
                                        module.handleEvent(event);
                                    } catch (EventMgtException e) {
                                        log.error("Error while invoking notification sending module " + module.
                                                getModuleName(), e);
                                    }
                                }
                            };
                            EventMgtServiceDataHolder.getInstance().getThreadPool().submit(msgSender);
                        }
                    } catch (EventMgtException e) {
                        log.error("Error while getting subscription status from notification module " + module.
                                getModuleName(), e);
                    }
                }
            } catch (InterruptedException e) {
                log.error("Error while picking up event from event queue", e);
            }
        }
    }

    public void shutdown() {
        this.running = false;
    }
}
