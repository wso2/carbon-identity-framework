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

/**
 * DFDP Authenticator Information.
 * Part 4: Authenticator Configuration - Basic information about a discovered authenticator.
 */
public class DFDPAuthenticatorInfo {

    private String name;
    private String displayName;
    private String type;
    private boolean enabled;
    private boolean defaultAuthenticator;
    private boolean dfdpCompatible;
    private List<String> supportedClaims;
    private int propertyCount;

    /**
     * Gets authenticator name.
     * 
     * @return Authenticator name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets authenticator name.
     * 
     * @param name Authenticator name
     */
    public void setName(String name) {
        this.name = name;
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
     * Gets authenticator type.
     * 
     * @return Authenticator type
     */
    public String getType() {
        return type;
    }

    /**
     * Sets authenticator type.
     * 
     * @param type Authenticator type
     */
    public void setType(String type) {
        this.type = type;
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
     * Gets default authenticator status.
     * 
     * @return true if default authenticator
     */
    public boolean isDefaultAuthenticator() {
        return defaultAuthenticator;
    }

    /**
     * Sets default authenticator status.
     * 
     * @param defaultAuthenticator Default authenticator status
     */
    public void setDefaultAuthenticator(boolean defaultAuthenticator) {
        this.defaultAuthenticator = defaultAuthenticator;
    }

    /**
     * Gets DFDP compatibility status.
     * 
     * @return true if DFDP compatible
     */
    public boolean isDfdpCompatible() {
        return dfdpCompatible;
    }

    /**
     * Sets DFDP compatibility status.
     * 
     * @param dfdpCompatible DFDP compatibility status
     */
    public void setDfdpCompatible(boolean dfdpCompatible) {
        this.dfdpCompatible = dfdpCompatible;
    }

    /**
     * Gets supported claims.
     * 
     * @return List of supported claims
     */
    public List<String> getSupportedClaims() {
        return supportedClaims;
    }

    /**
     * Sets supported claims.
     * 
     * @param supportedClaims List of supported claims
     */
    public void setSupportedClaims(List<String> supportedClaims) {
        this.supportedClaims = supportedClaims;
    }

    /**
     * Gets property count.
     * 
     * @return Property count
     */
    public int getPropertyCount() {
        return propertyCount;
    }

    /**
     * Sets property count.
     * 
     * @param propertyCount Property count
     */
    public void setPropertyCount(int propertyCount) {
        this.propertyCount = propertyCount;
    }

    /**
     * Gets authenticator summary.
     * 
     * @return Summary string
     */
    public String getSummary() {
        return String.format("%s (%s) - %s, %s, %s", 
                            name, type, 
                            enabled ? "Enabled" : "Disabled",
                            defaultAuthenticator ? "Default" : "Non-Default",
                            dfdpCompatible ? "DFDP Compatible" : "DFDP Incompatible");
    }
}
