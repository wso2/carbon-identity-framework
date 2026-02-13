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
 * Represents a debug request with resource information.
 * This model provides type-safe access to debug request parameters.
 */
public class DebugRequest {

    private String resourceId;
    private String resourceType;
    private String idpId; // Alternative identifier for backward compatibility.
    private Map<String, Object> additionalContext;

    /**
     * Constructs an empty DebugRequest.
     */
    public DebugRequest() {

        this.additionalContext = new HashMap<>();
    }

    /**
     * Constructs a DebugRequest with resource details.
     *
     * @param resourceId   The resource identifier.
     * @param resourceType The resource type.
     */
    public DebugRequest(String resourceId, String resourceType) {

        this();
        this.resourceId = resourceId;
        this.resourceType = resourceType;
    }

    /**
     * Creates a DebugRequest from a Map context.
     *
     * @param context Map containing request parameters.
     * @return DebugRequest instance.
     */
    public static DebugRequest fromMap(Map<String, Object> context) {

        if (context == null) {
            return null;
        }

        DebugRequest request = new DebugRequest();
        request.setResourceId((String) context.get("resourceId"));
        request.setResourceType((String) context.get("resourceType"));
        request.setIdpId((String) context.get("idpId"));

        // Copy additional context properties.
        for (Map.Entry<String, Object> entry : context.entrySet()) {
            String key = entry.getKey();
            if (!"resourceId".equals(key) && !"resourceType".equals(key) && !"idpId".equals(key)) {
                request.addContextProperty(key, entry.getValue());
            }
        }

        return request;
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
     * Gets the IDP identifier (alternative to resourceId).
     *
     * @return IDP ID string.
     */
    public String getIdpId() {

        return idpId;
    }

    /**
     * Sets the IDP identifier.
     *
     * @param idpId IDP ID string.
     */
    public void setIdpId(String idpId) {

        this.idpId = idpId;
    }

    /**
     * Gets the additional context properties.
     *
     * @return Map of additional properties.
     */
    public Map<String, Object> getAdditionalContext() {

        return additionalContext;
    }

    /**
     * Sets the additional context properties.
     *
     * @param additionalContext Map of additional properties.
     */
    public void setAdditionalContext(Map<String, Object> additionalContext) {

        this.additionalContext = additionalContext != null ? additionalContext : new HashMap<>();
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
     * Gets a context property.
     *
     * @param key Property key.
     * @return Property value or null if not found.
     */
    public Object getContextProperty(String key) {

        return this.additionalContext.get(key);
    }

    /**
     * Converts this request to a Map context for backward compatibility.
     *
     * @return Map representation of the request.
     */
    public Map<String, Object> toMap() {

        Map<String, Object> map = new HashMap<>(additionalContext);
        if (resourceId != null) {
            map.put("resourceId", resourceId);
        }
        if (resourceType != null) {
            map.put("resourceType", resourceType);
        }
        if (idpId != null) {
            map.put("idpId", idpId);
        }
        return map;
    }

    /**
     * Gets the effective resource ID (prefers resourceId, falls back to idpId).
     *
     * @return The effective resource identifier.
     */
    public String getEffectiveResourceId() {

        return resourceId != null ? resourceId : idpId;
    }
}
