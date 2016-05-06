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


import org.wso2.carbon.identity.notification.mgt.bean.ModuleConfiguration;
import org.wso2.carbon.identity.notification.mgt.bean.PublisherEvent;

/**
 * This interface is used to implement a MessageSender. All services which expose through this
 * interface are registered as Message Sending modules and fires on relevant events whenever
 * Notification sender is invoked.
 */
@SuppressWarnings("unused")
public interface NotificationSendingModule {

    /**
     * The logic for sending message. When the Notification Sender is invoked,
     * It will invoke this method of all registered MessageSendingModules.
     *
     * @param publisherEvent Published event name
     * @throws NotificationManagementException
     */
    public void sendMessage(PublisherEvent publisherEvent) throws NotificationManagementException;

    /**
     * A unique name for Module. Basically used to identify the registered module
     *
     * @return Module name
     */
    public String getModuleName();

    /**
     * Initiate the Notification sending module with configured properties to the particular module
     *
     * @param moduleConfiguration Configuration object for a notification sending module.
     * @throws NotificationManagementException
     */

    public void init(ModuleConfiguration moduleConfiguration) throws NotificationManagementException;

    /**
     * This method returns whether this MessageSendingModule can handle the given type of event or not. ie Whether
     * this module is subscribed to the passed event or not. Depending on the content of the event or depending on the
     * configurations module can decide whether it is subscribed or not.
     *
     * @param event Event which is published from the publisher
     * @return True if the module can handle or subscribed to the event.
     * @throws NotificationManagementException
     */
    public boolean isSubscribed(PublisherEvent event) throws NotificationManagementException;

}
