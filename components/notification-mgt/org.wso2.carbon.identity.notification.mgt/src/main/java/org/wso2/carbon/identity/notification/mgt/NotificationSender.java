/*
 * Copyright (c) 2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.identity.notification.mgt;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.annotation.bundle.Capability;
import org.wso2.carbon.identity.notification.mgt.bean.PublisherEvent;

import java.util.List;

/**
 * The service class exposed from this bundle. This will take care of triggering all registered message sending
 * modules on a publisher invocation of invoke method.
 */

@Capability(
        namespace = "osgi.service",
        attribute = {
                "objectClass=org.wso2.carbon.identity.notification.mgt.NotificationSender",
                "service.scope=singleton"
        }
)
@SuppressWarnings("unused")
public class NotificationSender {

    private static final Log log = LogFactory.getLog(NotificationSender.class);
    /**
     * Task for event distribution towards notification sending modules.
     */
    private EventDistributionTask eventDistributionTask;

    /**
     * Overridden to initiate event distribution task towards notification sending modules.
     *
     * @param notificationSendingModules Set of notification sending modules registered
     */
    public NotificationSender(List<NotificationSendingModule> notificationSendingModules, int threadPoolSize) {
        this.eventDistributionTask = new EventDistributionTask(notificationSendingModules, threadPoolSize);
        if (log.isDebugEnabled()) {
            log.debug("Starting event distribution task from Notification Management component");
        }
        new Thread(eventDistributionTask).start();
    }

    /**
     * This method is called from all service consumers of this bundle, whenever messageSendingModules need to be
     * fired. This method will check whether the registered message sending modules can handle the event type and if
     * can, it will invoke sendMessage
     *
     * @param event Publisher event
     * @throws NotificationManagementException
     */
    public void invoke(PublisherEvent event) throws NotificationManagementException {

        if (event == null) {
            throw new NotificationManagementException("No publisher event found to send notification");
        }
        if (log.isDebugEnabled()) {
            log.debug("Adding event to the event queue " + event.getEventName());
        }
        eventDistributionTask.addEventToQueue(event);
    }

    public void stopService() {
        eventDistributionTask.shutdown();
    }

}
