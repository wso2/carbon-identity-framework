/*
 *
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

package org.wso2.carbon.identity.notification.mgt;


import org.wso2.carbon.identity.notification.mgt.bean.ModuleConfiguration;
import org.wso2.carbon.identity.notification.mgt.bean.PublisherEvent;

/**
 * Abstract implementation of NotificationSendingModule.
 */
@SuppressWarnings("unused")
public abstract class AbstractNotificationSendingModule implements NotificationSendingModule {
    /**
     * Configurations for notification sending module
     */
    protected ModuleConfiguration moduleConfiguration;

    /**
     * Returns the simple name of the class
     *
     * @return Simple name of the class
     */
    @Override
    public String getModuleName() {
        return this.getClass().getName();
    }

    @Override
    public void init(ModuleConfiguration moduleConfiguration) throws NotificationManagementException {
        this.moduleConfiguration = moduleConfiguration;
    }

    /**
     * @param event Event which is published from the publisher
     * @return true
     * @throws NotificationManagementException
     */
    @Override
    public boolean isSubscribed(PublisherEvent event) throws NotificationManagementException {
        return true;
    }
}
