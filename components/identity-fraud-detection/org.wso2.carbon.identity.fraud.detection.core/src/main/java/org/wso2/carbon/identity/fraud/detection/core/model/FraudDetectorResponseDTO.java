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

/**
 * Data Transfer Object for Fraud Detector Response.
 */
public class FraudDetectorResponseDTO implements Serializable {

    private static final long serialVersionUID = 3446356171180022031L;
    private FraudDetectionConstants.ExecutionStatus status;
    private FraudDetectionConstants.ErrorType errorType;
    private String errorReason;
    private FraudDetectionConstants.FraudDetectionEvents eventName;
    private boolean interruptFlow;

    /**
     * Private constructor to prevent direct instantiation.
     */
    private FraudDetectorResponseDTO() {

    }

    /**
     * Constructor to create a FraudDetectorResponseDTO with the given status and event name.
     *
     * @param status    Execution status of the fraud detection.
     * @param eventName Name of the fraud detection event.
     */
    public FraudDetectorResponseDTO(FraudDetectionConstants.ExecutionStatus status,
                                    FraudDetectionConstants.FraudDetectionEvents eventName) {

        this.interruptFlow = false;
        this.status = status;
        this.eventName = eventName;
        this.errorReason = null;
    }

    /**
     * Indicates whether the fraud detection should interrupt the flow.
     *
     * @return true if the flow should be interrupted, false otherwise.
     */
    public boolean isInterruptFlow() {

        return interruptFlow;
    }

    /**
     * Sets whether the fraud detection should interrupt the flow.
     *
     * @param interruptFlow true to interrupt the flow, false otherwise.
     */
    public void setInterruptFlow(boolean interruptFlow) {

        this.interruptFlow = interruptFlow;
    }

    /**
     * Gets the execution status of the fraud detection.
     */
    public FraudDetectionConstants.ExecutionStatus getStatus() {

        return status;
    }

    /**
     * Sets the error type for the fraud detection response.
     *
     * @param errorType Type of error encountered during fraud detection.
     */
    public void setErrorType(FraudDetectionConstants.ErrorType errorType) {

        this.errorType = errorType;
    }

    /**
     * Gets the error type for the fraud detection response.
     *
     * @return Type of error encountered during fraud detection.
     */
    public FraudDetectionConstants.ErrorType getErrorType() {

        return errorType;
    }

    /**
     * Sets the error reason for the fraud detection response.
     *
     * @param errorReason Reason for the error encountered during fraud detection.
     */
    public void setErrorReason(String errorReason) {

        this.errorReason = errorReason;
    }

    /**
     * Gets the error reason for the fraud detection response.
     *
     * @return Reason for the error encountered during fraud detection.
     */
    public String getErrorReason() {

        return errorReason;
    }

    /**
     * Gets the event name associated with the fraud detection.
     *
     * @return Name of the fraud detection event.
     */
    public FraudDetectionConstants.FraudDetectionEvents getEventName() {

        return eventName;
    }
}
