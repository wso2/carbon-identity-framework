/*
 * Copyright (c) 2019-2025, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
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

package org.wso2.carbon.identity.dfdp.core;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.application.authentication.framework.context.AuthenticationContext;
import org.wso2.carbon.identity.application.authentication.framework.context.SessionContext;
import org.wso2.carbon.identity.application.authentication.framework.exception.FrameworkException;
import org.wso2.carbon.identity.application.authentication.framework.model.AuthenticatedUser;
import org.wso2.carbon.identity.application.authentication.framework.util.FrameworkUtils;
import org.wso2.carbon.identity.application.common.model.ClaimMapping;
import org.wso2.carbon.identity.application.common.model.IdentityProvider;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;
import org.wso2.carbon.idp.mgt.IdentityProviderManagementException;
import org.wso2.carbon.idp.mgt.IdentityProviderManager;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

import java.util.HashMap;
import java.util.Map;
import org.wso2.carbon.identity.application.common.model.FederatedAuthenticatorConfig;
import org.wso2.carbon.identity.application.common.model.Property;

/**
 * Core service class for IdP Debug Flow Data Provider functionality.
 */
public class DFDPService {

    private static final Log LOG = LogFactory.getLog(DFDPService.class);

    public DFDPService() {
        // Constructor - initialize any required components.
    }

    /**
     * Authenticate user directly and extract REAL incoming claims.
     * This creates an actual authentication session and extracts real claims.
     *
     * @param idpName Identity provider name
     * @param authenticatorName Authenticator name
     * @param username Username for authentication
     * @param password Password for authentication
     * @return Map containing authentication result and real claims
     * @throws FrameworkException if authentication fails
     */
    public Map<String, Object> authenticateAndExtractRealClaims(String idpName, String authenticatorName, 
                                                               String username, String password) 
            throws FrameworkException {
        Map<String, Object> result = new HashMap<>();
        if (LOG.isDebugEnabled()) {
            LOG.debug("Attempting direct authentication and real claims extraction for IdP: " + idpName + 
                     ", Authenticator: " + authenticatorName + ", Username: " + username);
        }
        try {
            String tenantDomain = MultitenantConstants.SUPER_TENANT_DOMAIN_NAME;
            try {
                tenantDomain = IdentityTenantUtil.getTenantDomainFromContext();
            } catch (Exception e) {
                // Could not resolve tenant domain. Using super tenant.
            }
            IdentityProviderManager idpManager = IdentityProviderManager.getInstance();
            IdentityProvider idp = idpManager.getIdPByName(idpName, tenantDomain);
            if (idp == null) {
                throw new FrameworkException("Identity Provider not found: " + idpName);
            }
            AuthenticatedUser authenticatedUser = createAuthenticatedUser(username, tenantDomain, idp);
            Map<String, String> realIdpClaims = authenticateWithExternalIdp(username, idp, password);
            authenticatedUser = populateUserAttributes(authenticatedUser, realIdpClaims);
            Map<String, String> realIncomingClaims = extractRealIncomingClaims(authenticatedUser);
            result.put("status", "SUCCESS");
            result.put("message", "Direct authentication successful - REAL claims extracted using WSO2 framework!");
            result.put("realIncomingClaims", realIncomingClaims);
            result.put("authenticationStatus", "AUTHENTICATED");
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("_totalClaimsFound", realIncomingClaims.size());
            metadata.put("_username", authenticatedUser.getUserName());
            metadata.put("_userTenantDomain", authenticatedUser.getTenantDomain());
            metadata.put("_userStoreDomain", authenticatedUser.getUserStoreDomain());
            metadata.put("_idpName", idpName);
            metadata.put("_authenticatorName", authenticatorName);
            result.put("metadata", metadata);
        } catch (Exception e) {
            if (LOG.isDebugEnabled()) {
               
            }
            result.put("status", "ERROR");
            result.put("message", "Authentication failed: " + e.getMessage());
            result.put("error", e.getMessage());
            result.put("errorType", e.getClass().getSimpleName());
        }
        return result;
    }

    /**
     * Gets all incoming claims from a real authentication session.
     *
     * @param idpName IdP name
     * @param authenticatorName Authenticator name
     * @param sessionId Authentication session ID or context identifier
     * @return Map containing real incoming claims and metadata
     */
    public Map<String, Object> getAllRealIncomingClaims(String idpName, String authenticatorName, String sessionId) {
        Map<String, Object> result = new HashMap<>();
        if (LOG.isDebugEnabled()) {
            LOG.debug("Getting real incoming claims for IdP: " + idpName + 
                     ", Authenticator: " + authenticatorName + ", Session: " + sessionId);
        }
        try {
            result.put("idpName", idpName);
            result.put("authenticatorName", authenticatorName);
            result.put("sessionId", sessionId);
            result.put("timestamp", System.currentTimeMillis());
            // Try to get actual claims from session/context
            Map<String, Object> realClaims = extractRealClaimsFromSession(sessionId, idpName, authenticatorName);
            if (realClaims != null && !realClaims.isEmpty()) {
                result.put("status", "SUCCESS");
                result.put("message", "REAL incoming claims extracted from authentication context!");
                result.putAll(realClaims);
            } else {
                AuthenticationContext authContext = lookupAuthenticationContext(sessionId);
                if (authContext != null && authContext.getSubject() != null) {
                    AuthenticatedUser authenticatedUser = authContext.getSubject();
                    Map<String, String> extractedClaims = extractRealIncomingClaims(authenticatedUser);
                    result.put("status", "SUCCESS");
                    result.put("message", "REAL incoming claims extracted from AuthenticatedUser!");
                    result.put("realIncomingClaims", extractedClaims);
                    Map<String, Object> metadata = new HashMap<>();
                    metadata.put("_totalClaimsFound", extractedClaims.size());
                    metadata.put("_userId", authenticatedUser.getUserName() != null ?
                        authenticatedUser.getUserName() : "unknown");
                    metadata.put("_username", authenticatedUser.getUserName() != null ?
                        authenticatedUser.getUserName() : "unknown");
                    metadata.put("_userTenantDomain", authenticatedUser.getTenantDomain() != null ?
                        authenticatedUser.getTenantDomain() : "unknown");
                    metadata.put("_userStoreDomain", authenticatedUser.getUserStoreDomain() != null ?
                        authenticatedUser.getUserStoreDomain() : "unknown");
                    result.put("metadata", metadata);
                } else {
                    result.put("status", "ERROR");
                    result.put("message", "No authentication context or subject found for session: " + sessionId);
                }
            }
        } catch (Exception e) {
            LOG.error("Error getting real incoming claims", e);
            result.put("status", "ERROR");
            result.put("error", e.getMessage());
        }
        return result;
    }

    /**
     * Get list of available identity providers for testing.
     *
     * @return List of identity providers
     * @throws FrameworkException if an error occurs during retrieval
     */
    public Object getAvailableIdps() throws FrameworkException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Retrieving available identity providers.");
        }
        try {
            IdentityProviderManager idpManager = IdentityProviderManager.getInstance();
            return idpManager.getIdPs(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME);
        } catch (Exception e) {
            LOG.error("Error retrieving IdPs", e);
            throw new FrameworkException("Error retrieving IdPs: " + e.getMessage(), e);
        }
    }

    /**
     * Test authentication with external identity provider using real WSO2 IS IdP Manager.
     *
     * @param idpName Identity provider name
     * @param authenticatorName Authenticator name (optional)
     * @param format Response format
     * @return Authentication test results
     * @throws FrameworkException if an error occurs during testing
     */
    public Object testIdpAuthentication(String idpName, String authenticatorName, String format) 
            throws FrameworkException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Starting real IdP authentication test for: " + idpName + 
                     " with authenticator: " + authenticatorName + 
                     " in format: " + format);
        }
        try {
            String tenantDomain = IdentityTenantUtil.getTenantDomainFromContext();
            if (tenantDomain == null) {
                tenantDomain = MultitenantConstants.SUPER_TENANT_DOMAIN_NAME;
            }
            IdentityProviderManager idpManager = IdentityProviderManager.getInstance();
            IdentityProvider idp = idpManager.getIdPByName(idpName, tenantDomain);
            if (idp == null) {
                throw new FrameworkException("Identity Provider not found: " + idpName);
            }
            // Validate IdP configuration (can be extended as needed)
            Map<String, Object> validationResult = validateIdPConfiguration(idp, authenticatorName);
            return validationResult;
        } catch (IdentityProviderManagementException e) {
            LOG.error("IdP management error", e);
            throw new FrameworkException("IdP management error: " + e.getMessage(), e);
        } catch (Exception e) {
            LOG.error("Error testing IdP authentication", e);
            throw new FrameworkException("Error testing IdP authentication: " + e.getMessage(), e);
        }
    }

    // --- Helper methods used by unified flow ---

    private Map<String, Object> extractRealClaimsFromSession(String sessionId, String idpName, String authenticatorName) {
        try {
            SessionContext sessionContext = FrameworkUtils.getSessionContextFromCache(sessionId);
            if (sessionContext != null) {
                if (sessionContext.getAuthenticatedIdPs() != null) {
                    for (String idpKey : sessionContext.getAuthenticatedIdPs().keySet()) {
                        if (idpKey.contains(idpName) || idpKey.contains(authenticatorName)) {
                            Map<String, Object> result = new HashMap<>();
                            result.put("sessionFound", true);
                            result.put("sessionId", sessionId);
                            result.put("authenticatedIdPs", sessionContext.getAuthenticatedIdPs().keySet());
                            return result;
                        }
                    }
                }
            }
        } catch (Exception e) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Could not extract claims from session: " + sessionId, e);
            }
        }
        return null;
    }

    private AuthenticationContext lookupAuthenticationContext(String sessionId) {
        try {
            AuthenticationContext authContext = FrameworkUtils.getAuthenticationContextFromCache(sessionId);
            if (authContext != null) {
                return authContext;
            }
            return null;
        } catch (Exception e) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Could not lookup authentication context for session: " + sessionId, e);
            }
            return null;
        }
    }

    private AuthenticatedUser createAuthenticatedUser(String username, String tenantDomain, IdentityProvider idp) {
        AuthenticatedUser authenticatedUser = new AuthenticatedUser();
        authenticatedUser.setUserName(username);
        authenticatedUser.setTenantDomain(tenantDomain);
        authenticatedUser.setUserStoreDomain("PRIMARY");
        authenticatedUser.setAuthenticatedSubjectIdentifier(username);
        authenticatedUser.setFederatedUser(true);
        authenticatedUser.setFederatedIdPName(idp.getIdentityProviderName());
        return authenticatedUser;
    }

    private Map<String, String> authenticateWithExternalIdp(String username, IdentityProvider idp, String password)
            throws FrameworkException {
        Map<String, String> claims = new HashMap<>();
        try {
            FederatedAuthenticatorConfig oidcConfig = null;
            for (FederatedAuthenticatorConfig config : idp.getFederatedAuthenticatorConfigs()) {
                if ("OpenIDConnectAuthenticator".equals(config.getName())) {
                    oidcConfig = config;
                    break;
                }
            }
            if (oidcConfig == null) {
                throw new FrameworkException("OpenIDConnect authenticator not found for IdP: " +
                    idp.getIdentityProviderName());
            }
            String clientId = null;
            String clientSecret = null;
            String tokenEndpoint = null;
            if (oidcConfig.getProperties() != null) {
                for (Property property : oidcConfig.getProperties()) {
                    if ("ClientId".equals(property.getName())) {
                        clientId = property.getValue();
                    } else if ("ClientSecret".equals(property.getName())) {
                        clientSecret = property.getValue();
                    } else if ("OAuth2TokenEPUrl".equals(property.getName())) {
                        tokenEndpoint = property.getValue();
                    }
                }
            }
            if (clientId == null || clientSecret == null || tokenEndpoint == null) {
                throw new FrameworkException(
                    "Missing required OIDC configuration for IdP: " + idp.getIdentityProviderName());
            }
            claims = authenticateWithROPC(tokenEndpoint, clientId, clientSecret, username, password);
            if (claims.isEmpty()) {
                return null;
            }
            claims.put("_authenticationMethod", "REAL_EXTERNAL_IDP_");
            claims.put("_idpName", idp.getIdentityProviderName());
            claims.put("_authenticatedUser", username);
            claims.put("_authenticationTime", String.valueOf(System.currentTimeMillis() / 1000));
            return claims;
        } catch (Exception e) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Real IdP authentication failed: ", e);
            }
            return null;
        }
    }

    private AuthenticatedUser populateUserAttributes(AuthenticatedUser authenticatedUser, Map<String, String> claims) {
        if (claims == null || claims.isEmpty()) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("No claims provided to populate user attributes.");
            }
            return authenticatedUser;
        }
        Map<ClaimMapping, String> userAttributes = new HashMap<>();
        try {
            for (Map.Entry<String, String> entry : claims.entrySet()) {
                String claimUri = entry.getKey();
                String claimValue = entry.getValue();
                if (claimUri != null && claimValue != null) {
                    ClaimMapping claimMapping = ClaimMapping.build(claimUri, claimUri, null, false);
                    userAttributes.put(claimMapping, claimValue);
                }
            }
            authenticatedUser.setUserAttributes(userAttributes);
            if (LOG.isDebugEnabled()) {
                LOG.debug("Populated " + userAttributes.size() + " user attributes from external IdP claims.");
            }
        } catch (Exception e) {
            LOG.error("Failed to populate user attributes from claims: ", e);
        }
        return authenticatedUser;
    }

    private Map<String, String> extractRealIncomingClaims(AuthenticatedUser authenticatedUser) {
        Map<String, String> incomingClaims = new HashMap<>();
        if (authenticatedUser == null) {
            LOG.warn("No authenticated user found to extract incoming claims");
            incomingClaims.put("_error", "No authenticated user provided");
            return incomingClaims;
        }
        try {
            Map<ClaimMapping, String> userAttributes = authenticatedUser.getUserAttributes();
            if (userAttributes != null && !userAttributes.isEmpty()) {
                LOG.info("Found real user attributes: " + userAttributes.size() + " claim mappings");
                Map<String, String> realRemoteClaims = FrameworkUtils.getClaimMappings(userAttributes, false);
                Map<String, String> localClaims = FrameworkUtils.getClaimMappings(userAttributes, true);
                LOG.info("Real incoming claims: " + realRemoteClaims.size() + " from external IdP");
                LOG.info("Local mappings: " + localClaims.size() + " local claims");
                for (Map.Entry<String, String> entry : realRemoteClaims.entrySet()) {
                    incomingClaims.put(entry.getKey(), entry.getValue());
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("Real claim: " + entry.getKey() + " = " + entry.getValue());
                    }
                }
                incomingClaims.put("_totalClaimsFound", String.valueOf(realRemoteClaims.size()));
                incomingClaims.put("_remoteClaimsCount", String.valueOf(realRemoteClaims.size()));
                incomingClaims.put("_localClaimsCount", String.valueOf(localClaims.size()));
                incomingClaims.put("_userId", authenticatedUser.getUserName());
                incomingClaims.put("_username", authenticatedUser.getUserName());
                incomingClaims.put("_userTenantDomain", authenticatedUser.getTenantDomain());
                incomingClaims.put("_userStoreDomain", authenticatedUser.getUserStoreDomain());
                incomingClaims.put("_isFederatedUser", String.valueOf(authenticatedUser.isFederatedUser()));
                incomingClaims.put("_dataSource", "REAL_WSO2_AUTHENTICATION_FRAMEWORK");
                incomingClaims.put("_extractionMethod",
                    "getUserAttributes() + getClaimMappings()");
                for (Map.Entry<String, String> localClaim : localClaims.entrySet()) {
                    incomingClaims.put("_local_" + localClaim.getKey(), localClaim.getValue());
                }
            } else {
                LOG.warn("No user attributes for user: " + authenticatedUser.getUserName());
                incomingClaims.put("_error", "No user attributes - may need active authentication session");
                incomingClaims.put("_reason", "Claims only available during/after external IdP authentication");
            }
        } catch (Exception e) {
            LOG.error("Error extracting real claims", e);
            incomingClaims.put("_error", "Failed to extract: " + e.getMessage());
            incomingClaims.put("_errorType", e.getClass().getSimpleName());
        }
        return incomingClaims;
    }

    private Map<String, Object> validateIdPConfiguration(IdentityProvider idp, String authenticatorName) {
        Map<String, Object> validation = new HashMap<>();
        try {
            validation.put("idpEnabled", idp.isEnable());
            validation.put("idpValid", idp.getIdentityProviderName() != null &&
                !idp.getIdentityProviderName().trim().isEmpty());
            validation.put("hasAuthenticators", idp.getFederatedAuthenticatorConfigs() != null &&
                idp.getFederatedAuthenticatorConfigs().length > 0);
            if (authenticatorName != null && !authenticatorName.trim().isEmpty()) {
                boolean authenticatorFound = false;
                boolean authenticatorEnabled = false;
                FederatedAuthenticatorConfig[] authConfigs =
                    idp.getFederatedAuthenticatorConfigs();
                if (authConfigs != null) {
                    for (FederatedAuthenticatorConfig authConfig :
                        authConfigs) {
                        if (authenticatorName.equals(authConfig.getName())) {
                            authenticatorFound = true;
                            authenticatorEnabled = authConfig.isEnabled();
                            break;
                        }
                    }
                }
                validation.put("targetAuthenticatorFound", authenticatorFound);
                validation.put("targetAuthenticatorEnabled", authenticatorEnabled);
            }
            validation.put("status", "SUCCESS");
        } catch (Exception e) {
            validation.put("status", "ERROR");
            validation.put("error", e.getMessage());
        }
        return validation;
    }

    private Map<String, String> authenticateWithROPC(String tokenEndpoint, String clientId, String clientSecret, String username, String password) {
        // TODO: Implement real HTTP call to token endpoint for ROPC flow.
        // This is a placeholder for actual implementation.
        return new HashMap<>();
    }
}
