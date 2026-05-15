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

import static org.wso2.carbon.identity.debug.framework.DebugFrameworkConstants.DEBUG_STATUS_FAILURE;
import static org.wso2.carbon.identity.debug.framework.DebugFrameworkConstants.DEBUG_STATUS_SUCCESS;
import static org.wso2.carbon.identity.debug.framework.DebugFrameworkConstants.DEBUG_STATUS_SUCCESS_COMPLETE;
import static org.wso2.carbon.identity.debug.framework.DebugFrameworkConstants.DEBUG_STATUS_SUCCESS_INCOMPLETE;

/**
 * Represents a debug response with status and result information.
 * This model provides type-safe access to debug response data.
 */
public class DebugFrameworkResponse {

    private String debugId;
    private String status;
    private String message;
    private String errorCode;
    private Map<String, Object> data;

    /**
     * Constructs an empty DebugFrameworkResponse.
     */
    public DebugFrameworkResponse() {

        this.data = new HashMap<>();
    }

    /**
     * Constructs a successful DebugFrameworkResponse with data.
     *
     * @param data The response data.
     */
    public DebugFrameworkResponse(Map<String, Object> data) {

        this.status = DEBUG_STATUS_SUCCESS;
        this.data = data != null ? data : new HashMap<>();
    }

    /**
     * Constructs a DebugFrameworkResponse with custom status and message.
     *
     * @param status  The response status.
     * @param message The response message.
     */
    public DebugFrameworkResponse(String status, String message) {

        this.status = status;
        this.message = message;
        this.data = new HashMap<>();
    }

    /**
     * Creates a success response.
     *
     * @param data The response data.
     * @return DebugFrameworkResponse instance with SUCCESS status.
     */
    public static DebugFrameworkResponse success(Map<String, Object> data) {

        return new DebugFrameworkResponse(data);
    }

    /**
     * Creates an error response.
     *
     * @param message The error message.
     * @return DebugFrameworkResponse instance with FAILURE status.
     */
    public static DebugFrameworkResponse error(String message) {

        String fallbackMessage = message != null ? message : "Unknown error occurred during debug execution";
        return new DebugFrameworkResponse(DEBUG_STATUS_FAILURE, fallbackMessage);
    }

    /**
     * Creates an error response with error code.
     *
     * @param errorCode The error code.
     * @param message   The error message.
     * @return DebugFrameworkResponse instance with FAILURE status and error code.
     */
    public static DebugFrameworkResponse error(String errorCode, String message) {

        DebugFrameworkResponse response = new DebugFrameworkResponse(DEBUG_STATUS_FAILURE, message);
        response.setErrorCode(errorCode);
        return response;
    }

    /**
     * Gets the error code.
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
     * Creates a DebugFrameworkResponse from DebugResult.
     * Sets debugId, status, and message as top-level fields.
     * The data map contains only protocol-specific metadata.
     *
     * @param result The DebugResult to convert.
     * @return DebugFrameworkResponse instance.
     */
    public static DebugFrameworkResponse fromDebugResult(DebugResult result) {

        if (result == null) {
            return error("Result is null");
        }

        DebugFrameworkResponse response = new DebugFrameworkResponse();

        // Set top-level fields directly.
        response.setDebugId(result.getDebugId());

        String resolvedStatus = resolveLifecycleStatus(result.getStatus());
        response.setStatus(resolvedStatus != null ? resolvedStatus
                : (result.isSuccessful() ? DEBUG_STATUS_SUCCESS_COMPLETE : DEBUG_STATUS_FAILURE));

        response.setMessage(result.getErrorMessage() != null ? result.getErrorMessage() : result.getStatus());

        // The data map contains only protocol-specific metadata — no top-level fields.
        Map<String, Object> data = new HashMap<>();

        if (result.getResultData() != null && !result.getResultData().isEmpty()) {
            data.putAll(result.getResultData());
        }

        if (result.getErrorCode() != null) {
            data.put("errorCode", result.getErrorCode());
        }

        // Merge metadata without overwriting already present resultData keys.
        if (result.getMetadata() != null && !result.getMetadata().isEmpty()) {
            for (Map.Entry<String, Object> entry : result.getMetadata().entrySet()) {
                data.putIfAbsent(entry.getKey(), entry.getValue());
            }
        }

        // Remove reserved top-level keys that may have been included in resultData or metadata
        // by protocol-specific executors. These fields are already promoted to top-level response fields.
        data.remove("debugId");
        data.remove("status");
        data.remove("message");

        response.setData(data);
        return response;
    }

    /**
     * Gets the debug session ID.
     *
     * @return Debug session ID string.
     */
    public String getDebugId() {

        return debugId;
    }

    /**
     * Sets the debug session ID.
     *
     * @param debugId Debug session ID string.
     */
    public void setDebugId(String debugId) {

        this.debugId = debugId;
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
     * Gets the response status.
     *
     * @return Status string.
     */
    public String getStatus() {

        return status;
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
     * Gets the response message.
     *
     * @return Response message.
     */
    public String getMessage() {

        return message;
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

        return DEBUG_STATUS_SUCCESS.equals(status);
    }

    private static String resolveLifecycleStatus(String status) {

        if (DEBUG_STATUS_FAILURE.equals(status)
                || DEBUG_STATUS_SUCCESS.equals(status)
                || DEBUG_STATUS_SUCCESS_INCOMPLETE.equals(status)
                || DEBUG_STATUS_SUCCESS_COMPLETE.equals(status)) {
            return status;
        }
        return null;
    }
}
