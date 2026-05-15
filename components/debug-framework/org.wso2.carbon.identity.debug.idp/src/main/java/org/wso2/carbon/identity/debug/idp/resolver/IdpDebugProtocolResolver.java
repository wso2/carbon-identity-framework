/*
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

package org.wso2.carbon.identity.debug.idp.resolver;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.application.common.model.FederatedAuthenticatorConfig;
import org.wso2.carbon.identity.application.common.model.IdentityProvider;
import org.wso2.carbon.identity.application.common.model.Property;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;
import org.wso2.carbon.identity.debug.framework.extension.DebugProtocolResolver;
import org.wso2.carbon.identity.debug.idp.core.IdpDebugConstants;
import org.wso2.carbon.idp.mgt.IdentityProviderManagementException;
import org.wso2.carbon.idp.mgt.IdentityProviderManager;

/**
 * Debug protocol resolver for Identity Providers.
 * Resolves the protocol based on the configured authenticators in the IdP.
 */
public class IdpDebugProtocolResolver implements DebugProtocolResolver {

    protected static final Log LOG = LogFactory.getLog(IdpDebugProtocolResolver.class);
    private static final String GOOGLE_HOST = "google";
    private static final String FACEBOOK_HOST = "facebook";
    private static final int RESOLVER_ORDER = 10;

    @Override
    public String resolveProtocol(String resourceId) {

        if (StringUtils.isEmpty(resourceId)) {
            return null;
        }

        try {
            String tenantDomain = IdentityTenantUtil.resolveTenantDomain();
            IdentityProviderManager idpManager = IdentityProviderManager.getInstance();
            IdentityProvider resource = loadResourceConfiguration(idpManager, resourceId, tenantDomain);

            if (resource == null) {
                return null;
            }

            return detectProtocolFromIdP(resource);

        } catch (IdentityProviderManagementException e) {
            LOG.error("Error resolving protocol for IdP resource: " + resourceId, e);
        }
        return null;
    }

    @Override
    public int getOrder() {

        return RESOLVER_ORDER;
    }

    protected IdentityProvider loadResourceConfiguration(IdentityProviderManager idpManager,
            String resourceId, String tenantDomain) throws IdentityProviderManagementException {

        // Try to get by resource ID first (null return means not found).
        IdentityProvider resource = idpManager.getIdPByResourceId(resourceId, tenantDomain, true);

        // Fall back to lookup by name if resource ID lookup returned null.
        if (resource == null) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Resource not found by ID, trying by name: " + resourceId);
            }
            resource = idpManager.getIdPByName(resourceId, tenantDomain);
        }

        return resource;
    }

    protected String detectProtocolFromIdP(IdentityProvider resource) {

        if (resource == null) {
            return null;
        }

        FederatedAuthenticatorConfig[] configs = resource.getFederatedAuthenticatorConfigs();
        if (!hasValidAuthenticators(configs)) {
            return null;
        }

        return findProtocolFromEnabledConfigs(resource, configs);
    }

    protected boolean hasValidAuthenticators(FederatedAuthenticatorConfig[] configs) {

        return configs != null && configs.length > 0;
    }

    protected String findProtocolFromEnabledConfigs(IdentityProvider resource,
            FederatedAuthenticatorConfig[] configs) {

        for (FederatedAuthenticatorConfig config : configs) {
            if (!isValidAuthenticatorConfig(config)) {
                continue;
            }

            String protocol = resolveProtocolForConfig(resource, config);
            if (protocol != null) {
                return protocol;
            }
        }
        return null;
    }

    protected String resolveProtocolForConfig(IdentityProvider resource, FederatedAuthenticatorConfig config) {

        String implementationName = config.getName();
        String protocolType = resolveProtocolTypeFromImplementation(implementationName);

        if (isGoogleBackedOidcProtocol(resource, config, protocolType)) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Detected Google-backed OIDC configuration from implementation: " + implementationName);
            }
            return IdpDebugConstants.PROTOCOL_TYPE_GOOGLE;
        }

        if (isFacebookProtocol(resource, config, protocolType)) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Detected Facebook-backed configuration from implementation: " + implementationName);
            }
            return IdpDebugConstants.PROTOCOL_TYPE_FACEBOOK;
        }

        if (protocolType != null) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Detected protocol type: " + protocolType + " for implementation: " + implementationName);
            }
            return protocolType;
        }

        return null;
    }

    protected boolean isGoogleBackedOidcProtocol(IdentityProvider resource, FederatedAuthenticatorConfig config,
            String protocolType) {

        // Only apply heuristic if implementation is already identified as OIDC.
        // This prevents false positives like "Google Analytics Integration" being treated as Google OIDC.
        return IdpDebugConstants.PROTOCOL_TYPE_OIDC.equalsIgnoreCase(protocolType)
                && isGoogleBackedOidcAuthenticator(resource, config);
    }

    protected boolean isFacebookProtocol(IdentityProvider resource, FederatedAuthenticatorConfig config,
            String protocolType) {

        return IdpDebugConstants.PROTOCOL_TYPE_FACEBOOK.equalsIgnoreCase(protocolType)
                || isFacebookBackedAuthenticator(resource, config);
    }

    protected boolean isValidAuthenticatorConfig(FederatedAuthenticatorConfig config) {

        return config != null && config.isEnabled() && !StringUtils.isEmpty(config.getName());
    }

    protected String resolveProtocolTypeFromImplementation(String implementationName) {

        if (StringUtils.isBlank(implementationName)) {
            return null;
        }

        if (matchesOidcImplementation(implementationName)) {
            return IdpDebugConstants.PROTOCOL_TYPE_OIDC;
        }
        if (IdpDebugConstants.IMPLEMENTATION_GITHUB.equalsIgnoreCase(implementationName)) {
            return IdpDebugConstants.PROTOCOL_TYPE_GITHUB;
        }
        if (IdpDebugConstants.IMPLEMENTATION_FACEBOOK.equalsIgnoreCase(implementationName)) {
            return IdpDebugConstants.PROTOCOL_TYPE_FACEBOOK;
        }
        if (IdpDebugConstants.IMPLEMENTATION_SAML_SSO.equalsIgnoreCase(implementationName)) {
            return IdpDebugConstants.PROTOCOL_TYPE_SAML;
        }
        return null;
    }

    protected boolean matchesOidcImplementation(String implementationName) {

        return IdpDebugConstants.IMPLEMENTATION_OPENID_CONNECT.equalsIgnoreCase(implementationName)
                || IdpDebugConstants.IMPLEMENTATION_GOOGLE_OIDC.equalsIgnoreCase(implementationName);
    }

    /**
     * Checks if an OIDC authenticator is backed by Google.
     *
     * @param resource Identity provider resource.
     * @param config Federated authenticator configuration.
     * @return true if IdP name or properties suggest Google backing, false otherwise.
     */
    protected boolean isGoogleBackedOidcAuthenticator(IdentityProvider resource,
            FederatedAuthenticatorConfig config) {

        if (config != null && IdpDebugConstants.IMPLEMENTATION_GOOGLE_OIDC.equalsIgnoreCase(config.getName())) {
            return true;
        }

        if (resource != null && containsGoogleIndicator(resource.getIdentityProviderName())) {
            return true;
        }

        Property[] properties = config != null ? config.getProperties() : null;
        if (properties == null || properties.length == 0) {
            return false;
        }

        for (Property property : properties) {
            if (property == null || StringUtils.isBlank(property.getValue())) {
                continue;
            }
            if (containsGoogleIndicator(property.getValue())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks if an authenticator is backed by Facebook.
     *
     * @param resource Identity provider resource.
     * @param config Federated authenticator configuration.
     * @return true if IdP name or properties suggest Facebook backing, false otherwise.
     */
    protected boolean isFacebookBackedAuthenticator(IdentityProvider resource,
            FederatedAuthenticatorConfig config) {

        if (config != null && IdpDebugConstants.IMPLEMENTATION_FACEBOOK.equalsIgnoreCase(config.getName())) {
            return true;
        }

        if (resource != null && containsFacebookIndicator(resource.getIdentityProviderName())) {
            return true;
        }

        Property[] properties = config != null ? config.getProperties() : null;
        if (properties == null || properties.length == 0) {
            return false;
        }

        for (Property property : properties) {
            if (property == null || StringUtils.isBlank(property.getValue())) {
                continue;
            }
            if (containsFacebookIndicator(property.getValue())) {
                return true;
            }
        }
        return false;
    }

    protected boolean containsGoogleIndicator(String value) {

        return StringUtils.contains(StringUtils.lowerCase(value), GOOGLE_HOST);
    }

    protected boolean containsFacebookIndicator(String value) {

        return StringUtils.contains(StringUtils.lowerCase(value), FACEBOOK_HOST);
    }
}
