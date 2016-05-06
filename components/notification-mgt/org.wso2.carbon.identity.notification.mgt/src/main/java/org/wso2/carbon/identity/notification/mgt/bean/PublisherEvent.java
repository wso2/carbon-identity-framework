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

package org.wso2.carbon.identity.notification.mgt.bean;

import org.apache.commons.lang.StringUtils;
import org.wso2.carbon.identity.notification.mgt.NotificationManagementException;

import java.util.Properties;

/**
 * A bean class for Publisher event. This contains the information which are passed to the Notification Management
 * component by the notification publisher.
 */
@SuppressWarnings("unused")
public class PublisherEvent {

    /**
     * Name of the event
     */
    private String eventName;
    /**
     * Set of dynamic properties including event data
     */
    private Properties eventProperties;

    public PublisherEvent(String eventName) throws NotificationManagementException {
        if (StringUtils.isEmpty(eventName)) {
            throw new NotificationManagementException("Cannot build Publisher Event without a valid event name");
        }
        this.eventName = eventName;
        eventProperties = new Properties();

    }

    public Properties getEventProperties() {
        return eventProperties;
    }

    public String getEventName() {
        return eventName;
    }

    /**
     * Add dynamic property to eventPublisher bean
     *
     * @param key   key of the property
     * @param value value of the property
     */
    public void addEventProperty(String key, String value) {
        eventProperties.put(key, value);
    }
}
