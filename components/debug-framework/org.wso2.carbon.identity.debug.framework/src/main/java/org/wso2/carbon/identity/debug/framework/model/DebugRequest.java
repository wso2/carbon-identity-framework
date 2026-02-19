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

import org.apache.commons.lang.StringUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents a debug request with resource information.
 * This model provides type-safe access to debug request parameters.
 */
public class DebugRequest {

    private String connectionId;
    private String resourceType;
    private final Map<String, Object> additionalContext;

    /**
     * Constructs an empty DebugRequest.
     */
    public DebugRequest() {

        this.additionalContext = new HashMap<>();
    }

    /**
     * Gets the resource identifier.
     *
     * @return Resource ID string.
     */
    public String getConnectionId() {

        return connectionId;
    }

    /**
     * Sets the resource identifier.
     *
     * @param connectionId Resource ID string.
     */
    public void setConnectionId(String connectionId) {

        this.connectionId = connectionId;
    }

    /**
     * Gets the resource type.
     *
     * @return Resource type string.
     */
    public String getResourceType() {

        return resourceType;
    }

    /**
     * Sets the resource type.
     *
     * @param resourceType Resource type string.
     */
    public void setResourceType(String resourceType) {

        this.resourceType = resourceType;
    }

    /**
     * Adds a context property.
     *
     * @param key   Property key.
     * @param value Property value.
     */
    public void addContextProperty(String key, Object value) {

        this.additionalContext.put(key, value);
    }

    /**
     * Converts this request to a Map context for backward compatibility.
     *
     * @return Map representation of the request.
     */
    public Map<String, Object> toMap() {

        Map<String, Object> map = new HashMap<>(additionalContext);
        if (connectionId != null) {
            map.put("connectionId", connectionId);
        }
        if (resourceType != null) {
            map.put("resourceType", resourceType);
        }
        return map;
    }

    /**
     * Gets the effective resource ID.
     * Checks in order: top-level connectionId, additionalContext connectionId, idpId.
     *
     * @return The effective resource identifier, or null if not set anywhere.
     */
    public String getEffectiveConnectionId() {

        if (StringUtils.isNotEmpty(connectionId)) {
            return connectionId;
        }

        // Check for common resource ID keys in additional context (properties).
        String[] possibleKeys = { "connectionId" };
        for (String key : possibleKeys) {
            Object value = additionalContext.get(key);
            if (value instanceof String && StringUtils.isNotEmpty((String) value)) {
                return (String) value;
            }
        }

        return connectionId;
    }
}
