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

package org.wso2.carbon.identity.debug.framework.core.enums;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.debug.framework.core.DebugProtocolProvider;
import org.wso2.carbon.identity.debug.framework.core.provider.OIDCDebugProtocolProvider;
import org.wso2.carbon.identity.debug.framework.core.registry.DebugProtocolRegistry;

/**
 * Enum for IdP debug protocols.
 * Defines the protocols that can be used by Identity Provider resources.
 */
public enum IdpDebugProtocol {

    OAUTH2_OIDC("oauth2_oidc", "OAuth2/OIDC"),
    GOOGLE("google", "Google"),
    GITHUB("github", "GitHub"),
    CUSTOM("custom", "Custom Protocol");

    private static final Log LOG = LogFactory.getLog(IdpDebugProtocol.class);

    private final String protocolId;
    private final String displayName;

    /**
     * Constructor for IdpDebugProtocol enum.
     *
     * @param protocolId The unique identifier for the protocol.
     * @param displayName The display name for the protocol.
     */
    IdpDebugProtocol(String protocolId, String displayName) {
        this.protocolId = protocolId;
        this.displayName = displayName;
    }

    /**
     * Gets the protocol ID.
     *
     * @return The protocol ID.
     */
    public String getProtocolId() {
        return protocolId;
    }

    /**
     * Gets the display name.
     *
     * @return The display name.
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Converts a string (typically authenticator name) to the corresponding IdpDebugProtocol.
     * Handles case-insensitive matching and common variations.
     *
     * @param protocolName The protocol or authenticator name.
     * @return The corresponding IdpDebugProtocol, or CUSTOM if not recognized.
     */
    public static IdpDebugProtocol fromString(String protocolName) {
        if (protocolName == null) {
            return CUSTOM;
        }

        String lowerName = protocolName.toLowerCase().trim();

        switch (lowerName) {
            case "oauth2":
            case "oidc":
            case "oauth2_oidc":
            case "openidconnect":
            case "openidconnectauthenticator":
            case "oidcauthenticator":
                return OAUTH2_OIDC;

            case "google":
            case "googleauthenticator":
            case "google_oauth2":
                return GOOGLE;

            case "github":
            case "githubauthenticator":
            case "github_oauth2":
                return GITHUB;

            default:
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Unknown protocol name: " + protocolName + ". Defaulting to CUSTOM.");
                }
                return CUSTOM;
        }
    }

    /**
     * Gets the appropriate DebugProtocolProvider for this protocol.
     * Uses switch case for built-in protocols and registry for custom protocols.
     *
     * @param resourceId The ID of the IdP resource.
     * @return The appropriate DebugProtocolProvider, or null if not found.
     */
    public DebugProtocolProvider getProvider(String resourceId) {
        switch (this) {
            case OAUTH2_OIDC:
                return new OIDCDebugProtocolProvider();

            case GOOGLE:
                return DebugProtocolRegistry.getInstance()
                    .getProvider("GOOGLE");

            case GITHUB:
                return DebugProtocolRegistry.getInstance()
                    .getProvider("GITHUB");

            case CUSTOM:
                // Try to look up custom provider by resource ID
                DebugProtocolProvider customProvider = DebugProtocolRegistry.getInstance()
                    .getProvider(resourceId);
                if (customProvider != null) {
                    return customProvider;
                }
                // Fall back to checking if there's a custom provider registered by name
                return DebugProtocolRegistry.getInstance()
                    .getProvider("CUSTOM");

            default:
                return null;
        }
    }
}
