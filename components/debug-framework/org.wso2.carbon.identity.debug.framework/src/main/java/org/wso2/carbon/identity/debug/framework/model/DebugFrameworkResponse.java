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
    private Map<String, Object> data = new HashMap<>();

    public DebugFrameworkResponse() {

    }

    public String getDebugId() {

        return debugId;
    }

    public void setDebugId(String debugId) {

        this.debugId = debugId;
    }

    public String getStatus() {

        return status;
    }

    public void setStatus(String status) {

        this.status = status;
    }

    public String getMessage() {

        return message;
    }

    public void setMessage(String message) {

        this.message = message;
    }

    public String getErrorCode() {

        return errorCode;
    }

    public void setErrorCode(String errorCode) {

        this.errorCode = errorCode;
    }

    public Map<String, Object> getData() {

        return data;
    }

    public void setData(Map<String, Object> data) {

        this.data = data != null ? data : new HashMap<>();
    }

    /**
     * Checks if the response indicates success (complete or incomplete).
     *
     * @return true if status is SUCCESS_COMPLETE or SUCCESS_INCOMPLETE, false otherwise.
     */
    public boolean isSuccess() {

        return DEBUG_STATUS_SUCCESS_COMPLETE.equals(status)
                || DEBUG_STATUS_SUCCESS_INCOMPLETE.equals(status);
    }
}
