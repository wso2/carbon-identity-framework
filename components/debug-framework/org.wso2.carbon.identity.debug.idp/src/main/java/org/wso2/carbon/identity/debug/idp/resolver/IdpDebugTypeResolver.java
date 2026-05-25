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
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;
import org.wso2.carbon.identity.debug.idp.core.IdpDebugConstants;
import org.wso2.carbon.idp.mgt.IdentityProviderManagementException;
import org.wso2.carbon.idp.mgt.IdentityProviderManager;

/**
 * Debug protocol resolver for Identity Providers.
 * Resolves the protocol based on the configured authenticators in the IdP.
 */
public class IdpDebugTypeResolver {

    private static final Log LOG = LogFactory.getLog(IdpDebugTypeResolver.class);
    private static final IdentityProviderManager IDP_MANAGER = IdentityProviderManager.getInstance();

    private IdpDebugTypeResolver() {

    }

    /**
     * Holds the result of protocol resolution: the protocol key and the already-loaded IdP object,
     * so callers can reuse the IdP without a second DB fetch.
     */
    public static class ProtocolResolutionResult {

        private final String protocolKey;
        private final IdentityProvider identityProvider;

        public ProtocolResolutionResult(String protocolKey, IdentityProvider identityProvider) {
            this.protocolKey = protocolKey;
            this.identityProvider = identityProvider;
        }

        public String getProtocolKey() {
            return protocolKey;
        }

        public IdentityProvider getIdentityProvider() {
            return identityProvider;
        }
    }

    /**
     * Resolves the protocol type for a given IdP resource ID and returns the loaded IdP alongside it.
     *
     * @param resourceId The unique identifier of the IdP resource.
     * @return ProtocolResolutionResult containing the protocol key and loaded IdP, or null if unresolvable.
     */
    public static ProtocolResolutionResult resolveProtocol(String resourceId) {

        if (StringUtils.isEmpty(resourceId)) {
            return null;
        }

        try {
            String tenantDomain = IdentityTenantUtil.resolveTenantDomain();
            IdentityProvider resource = loadIdpConfig(IDP_MANAGER, resourceId, tenantDomain);
            String protocolKey = detectProtocolFromIdP(resource);
            if (protocolKey == null) {
                return null;
            }
            return new ProtocolResolutionResult(protocolKey, resource);

        } catch (IdentityProviderManagementException e) {
            LOG.error("Error resolving protocol for IdP resource: " + resourceId, e);
        }
        return null;
    }

    protected static IdentityProvider loadIdpConfig(IdentityProviderManager idpManager,
            String resourceId, String tenantDomain) throws IdentityProviderManagementException {

        return idpManager.getIdPByResourceId(resourceId, tenantDomain, true);
    }

    protected static String detectProtocolFromIdP(IdentityProvider resource) {

        FederatedAuthenticatorConfig defaultConfig = resource.getDefaultAuthenticatorConfig();
        if (defaultConfig != null && isValidAuthenticatorConfig(defaultConfig)) {
            return resolveProtocolForConfig(defaultConfig);
        }

        FederatedAuthenticatorConfig[] configs = resource.getFederatedAuthenticatorConfigs();
        if (configs == null || configs.length == 0) {
            return null;
        }

        return detectProtocolFromConfigs(configs);
    }

    protected static String detectProtocolFromConfigs(FederatedAuthenticatorConfig[] configs) {

        for (FederatedAuthenticatorConfig config : configs) {
            if (!isValidAuthenticatorConfig(config)) {
                continue;
            }

            String protocol = resolveProtocolForConfig(config);
            if (protocol != null) {
                return protocol;
            }
        }
        return null;
    }

    protected static String resolveProtocolForConfig(FederatedAuthenticatorConfig config) {

        String implementationName = config.getName();
        String protocolType = resolveIdpType(implementationName);

        if (protocolType != null && LOG.isDebugEnabled()) {
            LOG.debug("Detected protocol type: " + protocolType + " for implementation: " + implementationName);
        }

        return protocolType;
    }

    protected static boolean isValidAuthenticatorConfig(FederatedAuthenticatorConfig config) {

        return config != null && config.isEnabled() && !StringUtils.isEmpty(config.getName());
    }

    /**
     * Maps a federated authenticator implementation name to a canonical protocol key.
     * Returns {@code null} for unrecognized implementations. Note that SAML resolves to a key
     * for which no debug provider is registered yet; such requests fail later with a clear
     * "executor not found" error rather than being silently dropped here.
     */
    protected static String resolveIdpType(String implementationName) {

        if (StringUtils.isBlank(implementationName)) {
            return null;
        }

        if (IdpDebugConstants.IMPLEMENTATION_OPENID_CONNECT.equalsIgnoreCase(implementationName)) {
            return IdpDebugConstants.IDP_TYPE_OIDC;
        }
        if (IdpDebugConstants.IMPLEMENTATION_GOOGLE_OIDC.equalsIgnoreCase(implementationName)) {
            return IdpDebugConstants.IDP_TYPE_GOOGLE;
        }
        if (IdpDebugConstants.IMPLEMENTATION_GITHUB.equalsIgnoreCase(implementationName)) {
            return IdpDebugConstants.IDP_TYPE_GITHUB;
        }
        if (IdpDebugConstants.IMPLEMENTATION_FACEBOOK.equalsIgnoreCase(implementationName)) {
            return IdpDebugConstants.IDP_TYPE_FACEBOOK;
        }
        if (IdpDebugConstants.IMPLEMENTATION_SAML_SSO.equalsIgnoreCase(implementationName)) {
            return IdpDebugConstants.IDP_TYPE_SAML;
        }
        return null;
    }
}
