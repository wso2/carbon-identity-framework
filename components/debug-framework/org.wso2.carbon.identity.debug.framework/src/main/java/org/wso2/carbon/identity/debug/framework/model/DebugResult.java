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

package org.wso2.carbon.identity.debug.framework.model;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents the result of a debug operation.
 * This is a generic model that can be extended by specific implementations.
 * Stores the outcome of debug flows including tokens, claims, and status information.
 */
public class DebugResult {

    private boolean successful;
    private String resultId;
    private long timestamp;
    private String status;
    private String errorCode;
    private String errorMessage;
    private Map<String, Object> resultData;
    private Map<String, Object> metadata;

    /**
     * Constructs an empty DebugResult.
     */
    public DebugResult() {

        this.resultData = new HashMap<>();
        this.metadata = new HashMap<>();
        this.timestamp = System.currentTimeMillis();
    }

    /**
     * Constructs a DebugResult with success status and ID.
     *
     * @param successful Success status.
     * @param resultId   Unique identifier for this result.
     */
    public DebugResult(boolean successful, String resultId) {

        this();
        this.successful = successful;
        this.resultId = resultId;
    }

    /**
     * Gets whether the debug operation was successful.
     *
     * @return true if successful, false otherwise.
     */
    public boolean isSuccessful() {

        return successful;
    }

    /**
     * Sets the success status.
     *
     * @param successful Success status.
     */
    public void setSuccessful(boolean successful) {

        this.successful = successful;
    }

    /**
     * Gets the result ID.
     *
     * @return Result ID string.
     */
    public String getResultId() {

        return resultId;
    }

    /**
     * Sets the result ID.
     *
     * @param resultId Result ID string.
     */
    public void setResultId(String resultId) {

        this.resultId = resultId;
    }

    /**
     * Gets the timestamp of result creation.
     *
     * @return Timestamp in milliseconds.
     */
    public long getTimestamp() {

        return timestamp;
    }

    /**
     * Sets the timestamp.
     *
     * @param timestamp Timestamp in milliseconds.
     */
    public void setTimestamp(long timestamp) {

        this.timestamp = timestamp;
    }

    /**
     * Gets the status message.
     *
     * @return Status string.
     */
    public String getStatus() {

        return status;
    }

    /**
     * Sets the status message.
     *
     * @param status Status string.
     */
    public void setStatus(String status) {

        this.status = status;
    }

    /**
     * Gets the error code if operation failed.
     *
     * @return Error code string.
     */
    public String getErrorCode() {

        return errorCode;
    }

    /**
     * Sets the error code.
     *
     * @param errorCode Error code string.
     */
    public void setErrorCode(String errorCode) {

        this.errorCode = errorCode;
    }

    /**
     * Gets the error message if operation failed.
     *
     * @return Error message string.
     */
    public String getErrorMessage() {

        return errorMessage;
    }

    /**
     * Sets the error message.
     *
     * @param errorMessage Error message string.
     */
    public void setErrorMessage(String errorMessage) {

        this.errorMessage = errorMessage;
    }

    /**
     * Gets the result data map.
     *
     * @return Map containing result data.
     */
    public Map<String, Object> getResultData() {

        return resultData;
    }

    /**
     * Sets the result data map.
     *
     * @param resultData Map containing result data.
     */
    public void setResultData(Map<String, Object> resultData) {

        this.resultData = resultData != null ? resultData : new HashMap<>();
    }

    /**
     * Adds a key-value pair to the result data.
     *
     * @param key   Data key.
     * @param value Data value.
     */
    public void addResultData(String key, Object value) {

        if (this.resultData == null) {
            this.resultData = new HashMap<>();
        }
        this.resultData.put(key, value);
    }

    /**
     * Gets the metadata map.
     *
     * @return Map containing metadata.
     */
    public Map<String, Object> getMetadata() {

        return metadata;
    }

    /**
     * Sets the metadata map.
     *
     * @param metadata Map containing metadata.
     */
    public void setMetadata(Map<String, Object> metadata) {

        this.metadata = metadata != null ? metadata : new HashMap<>();
    }

    /**
     * Adds a key-value pair to the metadata.
     *
     * @param key   Metadata key.
     * @param value Metadata value.
     */
    public void addMetadata(String key, Object value) {

        if (this.metadata == null) {
            this.metadata = new HashMap<>();
        }
        this.metadata.put(key, value);
    }

    @Override
    public String toString() {
        
        return "DebugResult{" +
                "successful=" + successful +
                ", resultId='" + resultId + '\'' +
                ", timestamp=" + timestamp +
                ", status='" + status + '\'' +
                ", errorCode='" + errorCode + '\'' +
                '}';
    }
}
