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

    private String resourceId;
    private String resourceType;
    private String protocol;
    private boolean successful;
    private String errorMessage;
    private String errorType;
    private Map<String, Object> properties;

    /**
     * Constructs an empty DebugContext.
     */
    public DebugContext() {

        this.properties = new HashMap<>();
        this.successful = true;
    }

    /**
     * Constructs a DebugContext with resource details.
     *
     * @param resourceId   The resource identifier.
     * @param resourceType The resource type.
     */
    public DebugContext(String resourceId, String resourceType) {

        this();
        this.resourceId = resourceId;
        this.resourceType = resourceType;
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
        context.setResourceId((String) contextMap.get("resourceId"));
        context.setResourceType((String) contextMap.get("resourceType"));
        context.setProtocol((String) contextMap.get("protocol"));
        context.setSuccessful((Boolean) contextMap.getOrDefault("successful", true));
        context.setErrorMessage((String) contextMap.get("errorMessage"));
        context.setErrorType((String) contextMap.get("errorType"));

        // Copy additional properties.
        for (Map.Entry<String, Object> entry : contextMap.entrySet()) {
            String key = entry.getKey();
            if (!"resourceId".equals(key) && !"resourceType".equals(key) && 
                !"protocol".equals(key) && !"successful".equals(key) &&
                !"errorMessage".equals(key) && !"errorType".equals(key)) {
                context.setProperty(key, entry.getValue());
            }
        }

        return context;
    }

    /**
     * Creates a success DebugContext.
     *
     * @param resourceId   The resource identifier.
     * @param resourceType The resource type.
     * @return DebugContext instance with success status.
     */
    public static DebugContext success(String resourceId, String resourceType) {

        DebugContext context = new DebugContext(resourceId, resourceType);
        context.setSuccessful(true);
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
     * Gets the resource identifier.
     *
     * @return Resource ID string.
     */
    public String getResourceId() {

        return resourceId;
    }

    /**
     * Sets the resource identifier.
     *
     * @param resourceId Resource ID string.
     */
    public void setResourceId(String resourceId) {

        this.resourceId = resourceId;
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
     * @return Protocol string (e.g., "OIDC", "SAML").
     */
    public String getProtocol() {

        return protocol;
    }

    /**
     * Sets the protocol.
     *
     * @param protocol Protocol string.
     */
    public void setProtocol(String protocol) {

        this.protocol = protocol;
    }

    /**
     * Gets whether the context resolution was successful.
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
     * Gets the context properties map.
     *
     * @return Map containing context properties.
     */
    public Map<String, Object> getProperties() {

        return properties;
    }

    /**
     * Sets the context properties map.
     *
     * @param properties Map containing context properties.
     */
    public void setProperties(Map<String, Object> properties) {

        this.properties = properties != null ? properties : new HashMap<>();
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
     * Converts this context to a Map for backward compatibility.
     *
     * @return Map representation of the context.
     */
    public Map<String, Object> toMap() {

        Map<String, Object> map = new HashMap<>(properties);
        if (resourceId != null) {
            map.put("resourceId", resourceId);
        }
        if (resourceType != null) {
            map.put("resourceType", resourceType);
        }
        if (protocol != null) {
            map.put("protocol", protocol);
        }
        map.put("successful", successful);
        if (errorMessage != null) {
            map.put("errorMessage", errorMessage);
        }
        if (errorType != null) {
            map.put("errorType", errorType);
        }
        return map;
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
