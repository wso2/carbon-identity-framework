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
import org.wso2.carbon.identity.debug.framework.DebugFrameworkConstants;
import org.wso2.carbon.identity.debug.framework.extension.DebugProtocolResolver;
import org.wso2.carbon.idp.mgt.IdentityProviderManagementException;
import org.wso2.carbon.idp.mgt.IdentityProviderManager;

/**
 * Debug protocol resolver for Identity Providers.
 * Resolves the protocol based on the configured authenticators in the IdP.
 */
public class IdpDebugProtocolResolver implements DebugProtocolResolver {

    private static final Log LOG = LogFactory.getLog(IdpDebugProtocolResolver.class);
    private static final String GOOGLE_HOST = "google";
    // Order should be relatively high to allow specific resolvers (e.g. for App) to run first if needed.
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

    private IdentityProvider loadResourceConfiguration(IdentityProviderManager idpManager,
            String resourceId, String tenantDomain) throws IdentityProviderManagementException {

        IdentityProvider resource = null;

        // Try to get by resource ID first.
        try {
            resource = idpManager.getIdPByResourceId(resourceId, tenantDomain, true);
        } catch (IdentityProviderManagementException e) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Resource not found by ID, trying by name: " + resourceId);
            }
            // Try by name.
            resource = idpManager.getIdPByName(resourceId, tenantDomain);
        }

        return resource;
    }

    private String detectProtocolFromIdP(IdentityProvider resource) {

        if (resource == null) {
            return null;
        }

        FederatedAuthenticatorConfig[] configs = resource.getFederatedAuthenticatorConfigs();
        if (!hasValidAuthenticators(configs)) {
            return null;
        }

        return findProtocolFromEnabledConfigs(resource, configs);
    }

    private boolean hasValidAuthenticators(FederatedAuthenticatorConfig[] configs) {

        return configs != null && configs.length > 0;
    }

    private String findProtocolFromEnabledConfigs(IdentityProvider resource,
            FederatedAuthenticatorConfig[] configs) {

        for (FederatedAuthenticatorConfig config : configs) {
            if (isValidAuthenticatorConfig(config)) {
                String implementationName = config.getName();
                String protocolType = resolveProtocolTypeFromImplementation(implementationName);
                if (DebugFrameworkConstants.PROTOCOL_TYPE_OIDC.equalsIgnoreCase(protocolType)
                        && isGoogleBackedOidcAuthenticator(resource, config)) {
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("Detected Google-backed OIDC configuration from implementation: "
                                + implementationName);
                    }
                    return DebugFrameworkConstants.PROTOCOL_TYPE_GOOGLE;
                }
                if (protocolType != null) {
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("Detected protocol type: " + protocolType
                                + " for implementation: " + implementationName);
                    }
                    return protocolType;
                }
                if (isGoogleBackedOidcAuthenticator(resource, config)) {
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("Detected Google-backed OIDC configuration from properties: "
                                + implementationName);
                    }
                    return DebugFrameworkConstants.PROTOCOL_TYPE_GOOGLE;
                }
            }
        }
        return null;
    }

    private boolean isValidAuthenticatorConfig(FederatedAuthenticatorConfig config) {

        return config != null && config.isEnabled() && !StringUtils.isEmpty(config.getName());
    }

    private String resolveProtocolTypeFromImplementation(String implementationName) {

        if (StringUtils.isBlank(implementationName)) {
            return null;
        }

        if (matchesOidcImplementation(implementationName)) {
            return DebugFrameworkConstants.PROTOCOL_TYPE_OIDC;
        }
        if (DebugFrameworkConstants.IMPLEMENTATION_GITHUB.equalsIgnoreCase(implementationName)) {
            return DebugFrameworkConstants.PROTOCOL_TYPE_GITHUB;
        }
        if (DebugFrameworkConstants.IMPLEMENTATION_SAML_SSO.equalsIgnoreCase(implementationName)) {
            return DebugFrameworkConstants.PROTOCOL_TYPE_SAML;
        }
        return null;
    }

    private boolean matchesOidcImplementation(String implementationName) {

        return DebugFrameworkConstants.IMPLEMENTATION_OPENID_CONNECT.equalsIgnoreCase(implementationName)
                || DebugFrameworkConstants.IMPLEMENTATION_GOOGLE_OIDC.equalsIgnoreCase(implementationName);
    }

    private boolean isGoogleBackedOidcAuthenticator(IdentityProvider resource,
            FederatedAuthenticatorConfig config) {

        if (resource != null && containsGoogleIndicator(resource.getIdentityProviderName())) {
            return true;
        }

        Property[] properties = config.getProperties();
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

    private boolean containsGoogleIndicator(String value) {

        String normalizedValue = StringUtils.lowerCase(value);
        return normalizedValue.contains(GOOGLE_HOST);
    }
}
