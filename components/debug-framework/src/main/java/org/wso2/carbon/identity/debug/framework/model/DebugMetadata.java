/*
 * Copyright (c) 2025, WSO2 LLC. (http://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.identity.debug.framework.model;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents context metadata for a debug operation.
 * Stores information about the Identity Provider, authenticator, and other contextual data.
 * This model is extensible for use by specific debug framework implementations.
 */
public class DebugMetadata {

    private String contextId;
    private String idpId;
    private String idpName;
    private String authenticatorName;
    private String tenantDomain;
    private long createdAt;
    private String sessionId;
    private Map<String, String> properties;

    /**
     * Constructs an empty DebugMetadata.
     */
    public DebugMetadata() {
        this.properties = new HashMap<>();
        this.createdAt = System.currentTimeMillis();
    }

    /**
     * Constructs a DebugMetadata with context ID.
     *
     * @param contextId Unique context identifier.
     */
    public DebugMetadata(String contextId) {
        this();
        this.contextId = contextId;
    }

    /**
     * Gets the context ID.
     *
     * @return Context ID string.
     */
    public String getContextId() {
        return contextId;
    }

    /**
     * Sets the context ID.
     *
     * @param contextId Context ID string.
     */
    public void setContextId(String contextId) {
        this.contextId = contextId;
    }

    /**
     * Gets the Identity Provider ID.
     *
     * @return IdP ID string.
     */
    public String getIdpId() {
        return idpId;
    }

    /**
     * Sets the Identity Provider ID.
     *
     * @param idpId IdP ID string.
     */
    public void setIdpId(String idpId) {
        this.idpId = idpId;
    }

    /**
     * Gets the Identity Provider name.
     *
     * @return IdP name string.
     */
    public String getIdpName() {
        return idpName;
    }

    /**
     * Sets the Identity Provider name.
     *
     * @param idpName IdP name string.
     */
    public void setIdpName(String idpName) {
        this.idpName = idpName;
    }

    /**
     * Gets the authenticator name.
     *
     * @return Authenticator name string.
     */
    public String getAuthenticatorName() {
        return authenticatorName;
    }

    /**
     * Sets the authenticator name.
     *
     * @param authenticatorName Authenticator name string.
     */
    public void setAuthenticatorName(String authenticatorName) {
        this.authenticatorName = authenticatorName;
    }

    /**
     * Gets the tenant domain.
     *
     * @return Tenant domain string.
     */
    public String getTenantDomain() {
        return tenantDomain;
    }

    /**
     * Sets the tenant domain.
     *
     * @param tenantDomain Tenant domain string.
     */
    public void setTenantDomain(String tenantDomain) {
        this.tenantDomain = tenantDomain;
    }

    /**
     * Gets the creation timestamp.
     *
     * @return Timestamp in milliseconds.
     */
    public long getCreatedAt() {
        return createdAt;
    }

    /**
     * Sets the creation timestamp.
     *
     * @param createdAt Timestamp in milliseconds.
     */
    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    /**
     * Gets the session ID.
     *
     * @return Session ID string.
     */
    public String getSessionId() {
        return sessionId;
    }

    /**
     * Sets the session ID.
     *
     * @param sessionId Session ID string.
     */
    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    /**
     * Gets the properties map.
     *
     * @return Map of metadata properties.
     */
    public Map<String, String> getProperties() {
        return properties;
    }

    /**
     * Sets the properties map.
     *
     * @param properties Map of metadata properties.
     */
    public void setProperties(Map<String, String> properties) {
        this.properties = properties != null ? properties : new HashMap<>();
    }

    /**
     * Adds a property.
     *
     * @param key   Property key.
     * @param value Property value.
     */
    public void addProperty(String key, String value) {
        if (this.properties == null) {
            this.properties = new HashMap<>();
        }
        this.properties.put(key, value);
    }

    /**
     * Gets a property value.
     *
     * @param key Property key.
     * @return Property value or null if not found.
     */
    public String getProperty(String key) {
        return this.properties != null ? this.properties.get(key) : null;
    }

    @Override
    public String toString() {
        return "DebugMetadata{" +
                "contextId='" + contextId + '\'' +
                ", idpId='" + idpId + '\'' +
                ", idpName='" + idpName + '\'' +
                ", authenticatorName='" + authenticatorName + '\'' +
                ", tenantDomain='" + tenantDomain + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }
}
