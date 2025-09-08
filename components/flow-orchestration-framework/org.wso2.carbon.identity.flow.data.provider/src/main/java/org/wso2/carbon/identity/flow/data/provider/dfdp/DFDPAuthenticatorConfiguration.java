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

package org.wso2.carbon.identity.flow.data.provider.dfdp;

import java.util.List;
import java.util.Map;

/**
 * DFDP Authenticator Configuration.
 * Part 4: Authenticator Configuration - Detailed configuration information for an authenticator.
 */
public class DFDPAuthenticatorConfiguration {

    private String authenticatorName;
    private String identityProviderName;
    private String authenticatorType;
    private String displayName;
    private boolean enabled;
    private boolean configured;
    private Map<String, String> properties;
    private List<String> supportedFeatures;
    private List<String> requiredProperties;
    private List<String> optionalProperties;
    private Map<String, String> propertyDescriptions;

    /**
     * Gets authenticator name.
     * 
     * @return Authenticator name
     */
    public String getAuthenticatorName() {
        return authenticatorName;
    }

    /**
     * Sets authenticator name.
     * 
     * @param authenticatorName Authenticator name
     */
    public void setAuthenticatorName(String authenticatorName) {
        this.authenticatorName = authenticatorName;
    }

    /**
     * Gets Identity Provider name.
     * 
     * @return Identity Provider name
     */
    public String getIdentityProviderName() {
        return identityProviderName;
    }

    /**
     * Sets Identity Provider name.
     * 
     * @param identityProviderName Identity Provider name
     */
    public void setIdentityProviderName(String identityProviderName) {
        this.identityProviderName = identityProviderName;
    }

    /**
     * Gets authenticator type.
     * 
     * @return Authenticator type
     */
    public String getAuthenticatorType() {
        return authenticatorType;
    }

    /**
     * Sets authenticator type.
     * 
     * @param authenticatorType Authenticator type
     */
    public void setAuthenticatorType(String authenticatorType) {
        this.authenticatorType = authenticatorType;
    }

    /**
     * Gets display name.
     * 
     * @return Display name
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Sets display name.
     * 
     * @param displayName Display name
     */
    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    /**
     * Gets enabled status.
     * 
     * @return true if enabled
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * Sets enabled status.
     * 
     * @param enabled Enabled status
     */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    /**
     * Gets configured status.
     * 
     * @return true if configured
     */
    public boolean isConfigured() {
        return configured;
    }

    /**
     * Sets configured status.
     * 
     * @param configured Configured status
     */
    public void setConfigured(boolean configured) {
        this.configured = configured;
    }

    /**
     * Gets authenticator properties.
     * 
     * @return Properties map
     */
    public Map<String, String> getProperties() {
        return properties;
    }

    /**
     * Sets authenticator properties.
     * 
     * @param properties Properties map
     */
    public void setProperties(Map<String, String> properties) {
        this.properties = properties;
    }

    /**
     * Gets supported features.
     * 
     * @return List of supported features
     */
    public List<String> getSupportedFeatures() {
        return supportedFeatures;
    }

    /**
     * Sets supported features.
     * 
     * @param supportedFeatures List of supported features
     */
    public void setSupportedFeatures(List<String> supportedFeatures) {
        this.supportedFeatures = supportedFeatures;
    }

    /**
     * Gets required properties.
     * 
     * @return List of required properties
     */
    public List<String> getRequiredProperties() {
        return requiredProperties;
    }

    /**
     * Sets required properties.
     * 
     * @param requiredProperties List of required properties
     */
    public void setRequiredProperties(List<String> requiredProperties) {
        this.requiredProperties = requiredProperties;
    }

    /**
     * Gets optional properties.
     * 
     * @return List of optional properties
     */
    public List<String> getOptionalProperties() {
        return optionalProperties;
    }

    /**
     * Sets optional properties.
     * 
     * @param optionalProperties List of optional properties
     */
    public void setOptionalProperties(List<String> optionalProperties) {
        this.optionalProperties = optionalProperties;
    }

    /**
     * Gets property descriptions.
     * 
     * @return Property descriptions map
     */
    public Map<String, String> getPropertyDescriptions() {
        return propertyDescriptions;
    }

    /**
     * Sets property descriptions.
     * 
     * @param propertyDescriptions Property descriptions map
     */
    public void setPropertyDescriptions(Map<String, String> propertyDescriptions) {
        this.propertyDescriptions = propertyDescriptions;
    }

    /**
     * Gets property count.
     * 
     * @return Total property count
     */
    public int getPropertyCount() {
        return properties != null ? properties.size() : 0;
    }

    /**
     * Gets configuration completeness percentage.
     * 
     * @return Completeness percentage
     */
    public double getConfigurationCompleteness() {
        if (requiredProperties == null || requiredProperties.isEmpty()) {
            return 100.0;
        }
        
        int configuredCount = 0;
        for (String requiredProp : requiredProperties) {
            if (properties != null && properties.containsKey(requiredProp) && 
                properties.get(requiredProp) != null && !properties.get(requiredProp).trim().isEmpty()) {
                configuredCount++;
            }
        }
        
        return (double) configuredCount / requiredProperties.size() * 100.0;
    }

    /**
     * Checks if authenticator is fully configured.
     * 
     * @return true if fully configured
     */
    public boolean isFullyConfigured() {
        return getConfigurationCompleteness() >= 100.0;
    }

    /**
     * Gets configuration summary.
     * 
     * @return Configuration summary
     */
    public String getConfigurationSummary() {
        return String.format("%s (%s) - %s, %.1f%% configured, %d properties",
                            authenticatorName, authenticatorType,
                            enabled ? "Enabled" : "Disabled",
                            getConfigurationCompleteness(),
                            getPropertyCount());
    }
}
