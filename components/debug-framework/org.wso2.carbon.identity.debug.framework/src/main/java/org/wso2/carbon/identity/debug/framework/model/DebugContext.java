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

    private String connectionId;
    private String resourceType;
    private boolean successful = true;
    private String errorMessage;
    private String errorType;
    private Map<String, Object> properties = new HashMap<>();

    /**
     * Constructs an empty DebugContext.
     * Properties map is initialized, and successful flag defaults to true.
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
    public static DebugContext fromMap(Map<String, Object> contextMap) {

        if (contextMap == null) {
            return null;
        }

        DebugContext context = new DebugContext();
        context.setConnectionId((String) contextMap.get("connectionId"));
        context.setResourceType((String) contextMap.get("resourceType"));
        context.setSuccessful((Boolean) contextMap.getOrDefault("successful", true));
        context.setErrorMessage((String) contextMap.get("errorMessage"));
        context.setErrorType((String) contextMap.get("errorType"));

        // Copy additional properties (including protocol if present).
        for (Map.Entry<String, Object> entry : contextMap.entrySet()) {
            String key = entry.getKey();
            if (!"connectionId".equals(key) && !"resourceType".equals(key) && 
                !"successful".equals(key) &&
                !"errorMessage".equals(key) && !"errorType".equals(key)) {
                context.setProperty(key, entry.getValue());
            }
        }

        return context;
    }

    /**
     * Creates an error DebugContext.
     *
     * @param errorMessage The error message.
     * @return DebugContext instance with failure status.
     */
    public static DebugContext error(String errorMessage) {

        DebugContext context = new DebugContext();
        context.setSuccessful(false);
        context.setErrorMessage(errorMessage);
        return context;
    }

    /**
     * Creates an error DebugContext with error type.
     *
     * @param errorMessage The error message.
     * @param errorType    The error type.
     * @return DebugContext instance with failure status.
     */
    public static DebugContext error(String errorMessage, String errorType) {

        DebugContext context = error(errorMessage);
        context.setErrorType(errorType);
        return context;
    }

    /**
     * Gets the connection identifier.
     *
     * @return Connection ID string.
     */
    public String getConnectionId() {

        return connectionId;
    }

    /**
     * Sets the connection identifier.
     *
     * @param connectionId Connection ID string.
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
     * Sets the success status.
     *
     * @param successful Success status.
     */
    public void setSuccessful(boolean successful) {

        this.successful = successful;
    }

    /**
     * Gets the error message if context resolution failed.
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
     * Gets the error type.
     *
     * @return Error type string.
     */
    public String getErrorType() {

        return errorType;
    }

    /**
     * Sets the error type.
     *
     * @param errorType Error type string.
     */
    public void setErrorType(String errorType) {

        this.errorType = errorType;
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

    /**
     * Checks if this is an error context.
     *
     * @return true if this context represents an error, false otherwise.
     */
    public boolean isError() {

        return !successful;
    }
}
