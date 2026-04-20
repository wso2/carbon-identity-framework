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

    /**
     * Returns serialized session data bytes.
     *
     * @return Serialized session data.
     */
    public byte[] getSessionData() {

        return sessionData;
    }

    /**
     * Sets serialized session data bytes.
     *
     * @param sessionData Serialized session data.
     */
    public void setSessionData(byte[] sessionData) {

        this.sessionData = sessionData;
    }

    public String getResultJson() {

        return resultJson;
    }

    public void setResultJson(String resultJson) {

        this.resultJson = resultJson;
    }

    public long getCreatedTime() {

        return createdTime;
    }

    public void setCreatedTime(long createdTime) {

        this.createdTime = createdTime;
    }

    public long getExpiryTime() {

        return expiryTime;
    }

    public void setExpiryTime(long expiryTime) {

        this.expiryTime = expiryTime;
    }

    public String getResourceType() {

        return resourceType;
    }

    public void setResourceType(String resourceType) {

        this.resourceType = resourceType;
    }

    public String getConnectionId() {

        return connectionId;
    }

    public void setConnectionId(String connectionId) {
        
        this.connectionId = connectionId;
    }
}
