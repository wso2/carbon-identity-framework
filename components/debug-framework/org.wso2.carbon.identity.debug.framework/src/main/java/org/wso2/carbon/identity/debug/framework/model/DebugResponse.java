/**
 * Copyright (c) 2026, WSO2 LLC. (https://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.identity.debug.framework.model;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents a debug response with status and result information.
 * This model provides type-safe access to debug response data.
 */
public class DebugResponse {

    private String status;
    private String message;
    private Map<String, Object> data;

    /**
     * Constructs an empty DebugResponse.
     */
    public DebugResponse() {

        this.data = new HashMap<>();
    }

    /**
     * Constructs a successful DebugResponse with data.
     *
     * @param data The response data.
     */
    public DebugResponse(Map<String, Object> data) {

        this.status = "SUCCESS";
        this.data = data != null ? data : new HashMap<>();
    }

    /**
     * Constructs a DebugResponse with custom status and message.
     *
     * @param status  The response status.
     * @param message The response message.
     */
    public DebugResponse(String status, String message) {

        this.status = status;
        this.message = message;
        this.data = new HashMap<>();
    }

    /**
     * Creates a success response.
     *
     * @param data The response data.
     * @return DebugResponse instance with SUCCESS status.
     */
    public static DebugResponse success(Map<String, Object> data) {

        return new DebugResponse(data);
    }

    /**
     * Creates an error response.
     *
     * @param message The error message.
     * @return DebugResponse instance with FAILURE status.
     */
    public static DebugResponse error(String message) {

        String fallbackMessage = message != null ? message : "Unknown error occurred during debug execution";
        return new DebugResponse("FAILURE", fallbackMessage);
    }

    /**
     * Creates an error response with custom status.
     *
     * @param status  The error status code.
     * @param message The error message.
     * @return DebugResponse instance.
     */
    public static DebugResponse error(String status, String message) {

        return new DebugResponse(status, message);
    }

    /**
     * Creates a DebugResponse from DebugResult.
     * Flattens resultData and metadata into the top-level data map.
     *
     * @param result The DebugResult to convert.
     * @return DebugResponse instance.
     */
    public static DebugResponse fromDebugResult(DebugResult result) {

        if (result == null) {
            return error("Result is null");
        }

        // Set top-level status and message.
        DebugResponse response = new DebugResponse();
        response.setStatus(result.isSuccessful() ? "SUCCESS" : "FAILURE");

        String message = result.getErrorMessage();
        if (message == null) {
            message = result.getStatus();
        }
        response.setMessage(message);

        Map<String, Object> data = new HashMap<>();

        // Add basic fields for backward compatibility.
        data.put("successful", result.isSuccessful());
        data.put("resultId", result.getResultId());
        data.put("timestamp", result.getTimestamp());

        if (result.getErrorCode() != null) {
            data.put("errorCode", result.getErrorCode());
        }

        // Flatten resultData into the top-level map.
        if (result.getResultData() != null && !result.getResultData().isEmpty()) {
            data.putAll(result.getResultData());
        }

        // Flatten metadata into the top-level map (avoid overwriting existing keys).
        if (result.getMetadata() != null && !result.getMetadata().isEmpty()) {
            for (Map.Entry<String, Object> entry : result.getMetadata().entrySet()) {
                data.putIfAbsent(entry.getKey(), entry.getValue());
            }
        }

        response.setData(data);
        return response;
    }

    /**
     * Sets the response status.
     *
     * @param status Status string.
     */
    public void setStatus(String status) {

        this.status = status;
    }

    /**
     * Sets the response message.
     *
     * @param message Message string.
     */
    public void setMessage(String message) {

        this.message = message;
    }

    /**
     * Gets the response data.
     *
     * @return Map of response data.
     */
    public Map<String, Object> getData() {

        return data;
    }

    /**
     * Sets the response data.
     *
     * @param data Map of response data.
     */
    public void setData(Map<String, Object> data) {

        this.data = data != null ? data : new HashMap<>();
    }

    /**
     * Checks if the response indicates success.
     *
     * @return true if status is SUCCESS, false otherwise.
     */
    public boolean isSuccess() {

        return "SUCCESS".equals(status);
    }
}
