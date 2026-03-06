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
 * Represents a resolved debug context containing all necessary information
 * for executing a debug flow.
 * This model provides type-safe access to context properties.
 */
public class DebugContext {

    private String resourceType;
    private Map<String, Object> properties = new HashMap<>();

    /**
     * Constructs an empty DebugContext.
     */
    public DebugContext() {
        // Fields initialized inline.
    }

    /**
     * Creates a DebugContext from a Map.
     *
     * @param contextMap Map containing context properties.
     * @return DebugContext instance.
     */
    public static DebugContext buildFromMap(Map<String, Object> contextMap) {

        if (contextMap == null) {
            return null;
        }

        DebugContext context = new DebugContext();
        context.setResourceType((String) contextMap.get("resourceType"));

        // Copy all properties.
        for (Map.Entry<String, Object> entry : contextMap.entrySet()) {
            String key = entry.getKey();
            if (!"resourceType".equals(key)) {
                context.setProperty(key, entry.getValue());
            }
        }

        return context;
    }

    /**
     * Gets the connection identifier.
     *
     * @return Connection ID string from properties.
     */
    public String getConnectionId() {

        return (String) properties.get("connectionId");
    }

    /**
     * Sets the connection identifier.
     *
     * @param connectionId Connection ID string.
     */
    public void setConnectionId(String connectionId) {

        this.properties.put("connectionId", connectionId);
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
     * Gets the protocol.
     *
     * @return Protocol string (e.g., "OIDC", "SAML") from properties.
     */
    public String getProtocol() {

        return (String) properties.get("protocol");
    }

    /**
     * Sets the protocol.
     *
     * @param protocol Protocol string.
     */
    public void setProtocol(String protocol) {

        this.properties.put("protocol", protocol);
    }

    /**
     * Gets a context property.
     *
     * @param key Property key.
     * @return Property value or null if not found.
     */
    public Object getProperty(String key) {

        return this.properties.get(key);
    }

    /**
     * Sets a context property.
     *
     * @param key   Property key.
     * @param value Property value.
     */
    public void setProperty(String key, Object value) {

        this.properties.put(key, value);
    }

    /**
     * Gets all properties as a map.
     * Returns a defensive copy to prevent external modification.
     *
     * @return Map of all properties.
     */
    public Map<String, Object> getProperties() {

        return new HashMap<>(this.properties);
    }
}
