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

    private static final Log LOG = LogFactory.getLog(IdpDebugProtocolResolver.class);
    private static final String GOOGLE_HOST = "google";
    private static final String FACEBOOK_HOST = "facebook";

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

    protected IdentityProvider loadResourceConfiguration(IdentityProviderManager idpManager,
            String resourceId, String tenantDomain) throws IdentityProviderManagementException {

        IdentityProvider resource = idpManager.getIdPByResourceId(resourceId, tenantDomain, true);

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
        if (configs == null || configs.length == 0) {
            return null;
        }

        return findProtocolFromEnabledConfigs(resource, configs);
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

        // Only apply Google heuristic when the implementation is already OIDC-typed — prevents false
        // positives like "Google Analytics Integration" being classified as Google OIDC.
        if (IdpDebugConstants.PROTOCOL_TYPE_OIDC.equalsIgnoreCase(protocolType)
                && isBackedByProvider(resource, config, IdpDebugConstants.IMPLEMENTATION_GOOGLE_OIDC, GOOGLE_HOST)) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Detected Google-backed OIDC configuration from implementation: " + implementationName);
            }
            return IdpDebugConstants.PROTOCOL_TYPE_GOOGLE;
        }

        // Mirror the Google gate: only apply Facebook heuristic when the implementation is OIDC or Facebook-typed.
        if (IdpDebugConstants.PROTOCOL_TYPE_FACEBOOK.equalsIgnoreCase(protocolType)
                || (IdpDebugConstants.PROTOCOL_TYPE_OIDC.equalsIgnoreCase(protocolType)
                        && isBackedByProvider(resource, config, IdpDebugConstants.IMPLEMENTATION_FACEBOOK,
                                FACEBOOK_HOST))) {
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

    protected boolean isValidAuthenticatorConfig(FederatedAuthenticatorConfig config) {

        return config != null && config.isEnabled() && !StringUtils.isEmpty(config.getName());
    }

    protected String resolveProtocolTypeFromImplementation(String implementationName) {

        if (StringUtils.isBlank(implementationName)) {
            return null;
        }

        if (IdpDebugConstants.IMPLEMENTATION_OPENID_CONNECT.equalsIgnoreCase(implementationName)
                || IdpDebugConstants.IMPLEMENTATION_GOOGLE_OIDC.equalsIgnoreCase(implementationName)) {
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

    /**
     * Checks if an authenticator config is backed by a specific provider by inspecting the implementation
     * name, IdP name, and property values for the given host indicator.
     */
    protected boolean isBackedByProvider(IdentityProvider resource, FederatedAuthenticatorConfig config,
            String knownImplementation, String hostIndicator) {

        if (config != null && knownImplementation.equalsIgnoreCase(config.getName())) {
            return true;
        }

        if (resource != null && StringUtils.containsIgnoreCase(resource.getIdentityProviderName(), hostIndicator)) {
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
            if (StringUtils.containsIgnoreCase(property.getValue(), hostIndicator)) {
                return true;
            }
        }
        return false;
    }
}
