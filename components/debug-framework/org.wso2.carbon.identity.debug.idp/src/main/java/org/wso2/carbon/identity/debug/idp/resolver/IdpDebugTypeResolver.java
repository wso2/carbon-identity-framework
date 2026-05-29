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
import org.wso2.carbon.idp.mgt.IdentityProviderManagementException;
import org.wso2.carbon.idp.mgt.IdentityProviderManager;

/**
 * Resolves the authenticator implementation name from an IdP resource,
 * so callers can look up a DebugTypeProvider directly without an intermediate type mapping.
 */
public class IdpDebugTypeResolver {

    private static final Log LOG = LogFactory.getLog(IdpDebugTypeResolver.class);
    private static final IdentityProviderManager IDP_MANAGER = IdentityProviderManager.getInstance();

    private IdpDebugTypeResolver() {

    }

    /**
     * Holds the loaded IdP and the authenticator implementation name resolved from it,
     * so callers can reuse the IdP without a second DB fetch.
     */
    public static class TypeResolutionResult {

        private final String authenticatorName;
        private final IdentityProvider identityProvider;

        public TypeResolutionResult(String authenticatorName, IdentityProvider identityProvider) {
            this.authenticatorName = authenticatorName;
            this.identityProvider = identityProvider;
        }

        public String getAuthenticatorName() {
            return authenticatorName;
        }

        public IdentityProvider getIdentityProvider() {
            return identityProvider;
        }
    }

    /**
     * Loads the IdP for the given resource ID and resolves the authenticator implementation name
     * from its enabled federated authenticator configs.
     *
     * @param resourceId The unique identifier of the IdP resource.
     * @return TypeResolutionResult, or null if the IdP cannot be loaded or has no enabled authenticator.
     */
    public static TypeResolutionResult resolve(String resourceId) {

        if (StringUtils.isEmpty(resourceId)) {
            return null;
        }

        try {
            String tenantDomain = IdentityTenantUtil.resolveTenantDomain();
            IdentityProvider idp = loadIdpConfig(IDP_MANAGER, resourceId, tenantDomain);
            String authenticatorName = resolveAuthenticatorName(idp);
            if (authenticatorName == null) {
                return null;
            }
            if (LOG.isDebugEnabled()) {
                LOG.debug("Resolved authenticator implementation: " + authenticatorName
                        + " for IdP resource: " + resourceId);
            }
            return new TypeResolutionResult(authenticatorName, idp);

        } catch (IdentityProviderManagementException e) {
            LOG.error("Error resolving authenticator type for IdP resource: " + resourceId, e);
        }
        return null;
    }

    protected static IdentityProvider loadIdpConfig(IdentityProviderManager idpManager,
            String resourceId, String tenantDomain) throws IdentityProviderManagementException {

        return idpManager.getIdPByResourceId(resourceId, tenantDomain, true);
    }

    protected static String resolveAuthenticatorName(IdentityProvider idp) {

        FederatedAuthenticatorConfig defaultConfig = idp.getDefaultAuthenticatorConfig();
        if (defaultConfig != null && isValidAuthenticatorConfig(defaultConfig)) {
            return defaultConfig.getName();
        }

        FederatedAuthenticatorConfig[] configs = idp.getFederatedAuthenticatorConfigs();
        if (configs == null || configs.length == 0) {
            return null;
        }

        for (FederatedAuthenticatorConfig config : configs) {
            if (isValidAuthenticatorConfig(config)) {
                return config.getName();
            }
        }
        return null;
    }

    protected static boolean isValidAuthenticatorConfig(FederatedAuthenticatorConfig config) {

        return config != null && config.isEnabled() && !StringUtils.isEmpty(config.getName());
    }
}
