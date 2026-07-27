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

import org.wso2.carbon.identity.fraud.detection.core.constant.FraudDetectionConstants;

import java.io.Serializable;
import java.util.Map;

/**
 * Data Transfer Object for Fraud Detector Request.
 */
public class FraudDetectorRequestDTO implements Serializable {

    private static final long serialVersionUID = -2927529155183668608L;

    private FraudDetectionConstants.FraudDetectionEvents eventName;
    private boolean interruptFlow;
    private boolean logRequestPayload;
    private Map<String, Object> properties;

    /**
     * Private constructor to prevent direct instantiation.
     */
    private FraudDetectorRequestDTO() {

    }

    /**
     * Constructor to create a FraudDetectorRequestDTO with the given event name and properties.
     *
     * @param eventName  Name of the fraud detection event.
     * @param properties Map of properties related to the event.
     */
    public FraudDetectorRequestDTO(FraudDetectionConstants.FraudDetectionEvents eventName,
                                   Map<String, Object> properties) {

        this.eventName = eventName;
        this.properties = properties;
        this.interruptFlow = false;
        this.logRequestPayload = false;
    }

    /**
     * Returns the event name.
     *
     * @return Event name.
     */
    public FraudDetectionConstants.FraudDetectionEvents getEventName() {

        return eventName;
    }

    /**
     * Returns the properties map.
     *
     * @return Map of properties.
     */
    public Map<String, Object> getProperties() {

        return properties;
    }

    /**
     * Checks whether to interrupt the flow based on fraud detection.
     *
     * @return true if the flow should be interrupted, false otherwise.
     */
    public boolean isInterruptFlow() {

        return interruptFlow;
    }

    /**
     * Sets whether to interrupt the flow based on fraud detection.
     *
     * @param interruptFlow true to interrupt the flow, false otherwise.
     */
    public void setInterruptFlow(boolean interruptFlow) {

        this.interruptFlow = interruptFlow;
    }

    /**
     * Checks whether to log the request payload.
     *
     * @return true if the request payload should be logged, false otherwise.
     */
    public boolean isLogRequestPayload() {

        return logRequestPayload;
    }

    /**
     * Sets whether to log the request payload.
     *
     * @param logRequestPayload true to log the request payload, false otherwise.
     */
    public void setLogRequestPayload(boolean logRequestPayload) {

        this.logRequestPayload = logRequestPayload;
    }
}
