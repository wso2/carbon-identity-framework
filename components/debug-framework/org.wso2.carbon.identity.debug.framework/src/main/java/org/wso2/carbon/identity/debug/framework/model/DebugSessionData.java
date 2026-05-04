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

    private String debugId;
    private String status;
    private byte[] sessionData;
    private String resultJson;
    private long createdTime;
    private long expiryTime;
    private String resourceType;
    private String connectionId;

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
     * Returns the current debug session status.
     *
     * @return Current debug session status.
     */
    public String getStatus() {

        return status;
    }

    /**
     * Sets the current debug session status.
     *
     * @param status Current debug session status.
     */
    public void setStatus(String status) {

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

    /**
     * Returns the resource type associated with this debug session.
     *
     * @return Resource type associated with this debug session.
     */
    public String getResourceType() {

        return resourceType;
    }

    /**
     * Sets the resource type associated with this debug session.
     *
     * @param resourceType Resource type associated with this debug session.
     */
    public void setResourceType(String resourceType) {

        this.resourceType = resourceType;
    }

    /**
     * Returns the connection identifier associated with this debug session.
     *
     * @return Connection identifier associated with this debug session.
     */
    public String getConnectionId() {

        return connectionId;
    }

    /**
     * Sets the connection identifier associated with this debug session.
     *
     * @param connectionId Connection identifier associated with this debug session.
     */
    public void setConnectionId(String connectionId) {
        
        this.connectionId = connectionId;
    }
}
