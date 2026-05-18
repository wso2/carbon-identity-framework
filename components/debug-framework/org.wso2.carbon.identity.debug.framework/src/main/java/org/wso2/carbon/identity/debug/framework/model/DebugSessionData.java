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

import java.io.Serializable;
import java.util.Arrays;

/**
 * Model class for Debug Session Data.
 */
public class DebugSessionData implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * Valid lifecycle states for a debug session.
     * Transitions are strictly PENDING → COMPLETED; no other state is permitted.
     */
    public enum SessionStatus {

        PENDING, COMPLETED;

        /**
         * Parses a DB-sourced string to a {@code SessionStatus}.
         *
         * @param value raw status string from the database.
         * @return matching {@code SessionStatus}.
         * @throws IllegalArgumentException if the value does not match a known status.
         */
        public static SessionStatus fromString(String value) {

            for (SessionStatus s : values()) {
                if (s.name().equalsIgnoreCase(value)) {
                    return s;
                }
            }
            throw new IllegalArgumentException("Unknown session status: " + value);
        }
    }

    private String debugId;
    private SessionStatus status;
    private byte[] sessionData;
    private String resultJson;
    private long createdTime;
    private long expiryTime;

    /**
     * Returns the debug session identifier.
     *
     * @return Debug session identifier.
     */
    public String getDebugId() {

        return debugId;
    }

    /**
     * Sets the debug session identifier.
     *
     * @param debugId Debug session identifier.
     */
    public void setDebugId(String debugId) {

        this.debugId = debugId;
    }

    /**
     * Returns the current debug session status as a string suitable for DB persistence.
     *
     * @return Current debug session status string.
     */
    public String getStatus() {

        return status != null ? status.name() : null;
    }

    /**
     * Sets the current debug session status.
     *
     * @param status Current debug session status.
     */
    public void setStatus(SessionStatus status) {

        this.status = status;
    }

    public byte[] getSessionData() {

        return sessionData == null ? null : Arrays.copyOf(sessionData, sessionData.length);
    }

    public void setSessionData(byte[] sessionData) {

        this.sessionData = sessionData == null ? null : Arrays.copyOf(sessionData, sessionData.length);
    }

    /**
     * Returns the debug result JSON payload.
     *
     * @return Debug result JSON payload.
     */
    public String getResultJson() {

        return resultJson;
    }

    /**
     * Sets the debug result JSON payload.
     *
     * @param resultJson Debug result JSON payload.
     */
    public void setResultJson(String resultJson) {

        this.resultJson = resultJson;
    }

    /**
     * Returns the creation timestamp of the debug session.
     *
     * @return Creation timestamp of the debug session.
     */
    public long getCreatedTime() {

        return createdTime;
    }

    /**
     * Sets the creation timestamp of the debug session.
     *
     * @param createdTime Creation timestamp of the debug session.
     */
    public void setCreatedTime(long createdTime) {

        this.createdTime = createdTime;
    }

    /**
     * Returns the expiry timestamp of the debug session.
     *
     * @return Expiry timestamp of the debug session.
     */
    public long getExpiryTime() {

        return expiryTime;
    }

    /**
     * Sets the expiry timestamp of the debug session.
     *
     * @param expiryTime Expiry timestamp of the debug session.
     */
    public void setExpiryTime(long expiryTime) {

        this.expiryTime = expiryTime;
    }

    @Override
    public String toString() {

        StringBuilder sb = new StringBuilder("DebugSessionData{");
        sb.append("debugId='").append(debugId).append('\'');
        sb.append(", status='").append(status).append('\'');
        sb.append(", sessionDataSize=").append(sessionData != null ? sessionData.length : 0);
        sb.append(", resultJsonLength=").append(resultJson != null ? resultJson.length() : 0);
        sb.append(", createdTime=").append(createdTime);
        sb.append(", expiryTime=").append(expiryTime);
        sb.append('}');
        return sb.toString();
    }

}
