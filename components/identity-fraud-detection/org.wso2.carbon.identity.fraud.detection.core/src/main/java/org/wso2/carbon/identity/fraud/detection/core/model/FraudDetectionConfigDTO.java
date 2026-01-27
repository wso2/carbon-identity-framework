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
package org.wso2.carbon.identity.fraud.detection.core.model;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Data Transfer Object for Fraud Detection Configuration.
 */
public class FraudDetectionConfigDTO implements Serializable {

    private static final long serialVersionUID = -9127801979180720339L;
    private Map<String, EventConfigDTO> events = new HashMap<>();
    private boolean publishUserInfo = false;
    private boolean publishDeviceMetadata = false;
    private boolean logRequestPayload = false;

    /**
     * Returns the EventConfigDTO for the given event name.
     *
     * @param eventName Name of the event.
     * @return EventConfigDTO corresponding to the event name.
     */
    public EventConfigDTO getEventConfig(String eventName) {

        return events.get(eventName);
    }

    /**
     * Returns the map of event names to their corresponding EventConfigDTOs.
     *
     * @return Map of event names to EventConfigDTOs.
     */
    public Map<String, EventConfigDTO> getEvents() {

        return events;
    }

    /**
     * Adds an EventConfigDTO for the given event name.
     *
     * @param eventName      Name of the event.
     * @param eventConfigDTO EventConfigDTO to be added.
     */
    public void addEventConfig(String eventName, EventConfigDTO eventConfigDTO) {

        this.events.put(eventName, eventConfigDTO);
    }

    /**
     * Sets the map of event names to their corresponding EventConfigDTOs.
     *
     * @param events Map of event names to EventConfigDTOs.
     */
    public void setEvents(Map<String, EventConfigDTO> events) {

        this.events = events;
    }

    /**
     * Indicates whether user information should be published.
     *
     * @return true if user information should be published, false otherwise.
     */
    public boolean isPublishUserInfo() {

        return publishUserInfo;
    }

    /**
     * Sets whether user information should be published.
     *
     * @param publishUserInfo true to publish user information, false otherwise.
     */
    public void setPublishUserInfo(boolean publishUserInfo) {

        this.publishUserInfo = publishUserInfo;
    }

    /**
     * Indicates whether device metadata should be published.
     *
     * @return true if device metadata should be published, false otherwise.
     */
    public boolean isPublishDeviceMetadata() {

        return publishDeviceMetadata;
    }

    /**
     * Sets whether device metadata should be published.
     *
     * @param publishDeviceMetadata true to publish device metadata, false otherwise.
     */
    public void setPublishDeviceMetadata(boolean publishDeviceMetadata) {

        this.publishDeviceMetadata = publishDeviceMetadata;
    }

    /**
     * Indicates whether request payload logging is enabled.
     *
     * @return true if request payload logging is enabled, false otherwise.
     */
    public boolean isLogRequestPayload() {

        return logRequestPayload;
    }

    /**
     * Sets whether request payload logging is enabled.
     *
     * @param logRequestPayload true to enable request payload logging, false otherwise.
     */
    public void setLogRequestPayload(boolean logRequestPayload) {

        this.logRequestPayload = logRequestPayload;
    }
}
