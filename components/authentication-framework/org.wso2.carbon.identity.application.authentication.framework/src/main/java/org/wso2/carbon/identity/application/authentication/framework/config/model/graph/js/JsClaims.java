/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.wso2.carbon.identity.application.authentication.framework.config.model.graph.js;

import org.apache.commons.collections.map.HashedMap;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.identity.application.authentication.framework.ApplicationAuthenticator;
import org.wso2.carbon.identity.application.authentication.framework.config.ConfigurationFacade;
import org.wso2.carbon.identity.application.authentication.framework.config.model.ExternalIdPConfig;
import org.wso2.carbon.identity.application.authentication.framework.context.AuthenticationContext;
import org.wso2.carbon.identity.application.authentication.framework.internal.FrameworkServiceDataHolder;
import org.wso2.carbon.identity.application.authentication.framework.model.AuthenticatedUser;
import org.wso2.carbon.identity.application.authentication.framework.util.FrameworkConstants;
import org.wso2.carbon.identity.application.authentication.framework.util.FrameworkUtils;
import org.wso2.carbon.identity.application.common.model.ClaimMapping;
import org.wso2.carbon.identity.application.mgt.ApplicationConstants;
import org.wso2.carbon.identity.base.IdentityException;
import org.wso2.carbon.identity.claim.metadata.mgt.ClaimMetadataHandler;
import org.wso2.carbon.identity.claim.metadata.mgt.exception.ClaimMetadataException;
import org.wso2.carbon.identity.core.IdentityClaimManager;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;
import org.wso2.carbon.idp.mgt.IdentityProviderManagementException;
import org.wso2.carbon.idp.mgt.IdentityProviderManager;
import org.wso2.carbon.user.api.UserRealm;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.core.claim.Claim;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.user.core.util.UserCoreUtil;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Represent the user's claim. Can be either remote or local.
 */
public class JsClaims extends AbstractJSContextMemberObject {

    private static final Log LOG = LogFactory.getLog(JsClaims.class);
    private String idp;
    private boolean isRemoteClaimRequest;
    private int step;
    private transient AuthenticatedUser authenticatedUser;

    /**
     * Constructor to get the user authenticated in step 'n'
     *
     * @param step                 The authentication step
     * @param idp                  The authenticated IdP
     * @param isRemoteClaimRequest Whether the request is for remote claim (false for local claim request)
     */
    public JsClaims(AuthenticationContext context, int step, String idp, boolean isRemoteClaimRequest) {

        this(step, idp, isRemoteClaimRequest);
        initializeContext(context);
    }

    public JsClaims(int step, String idp, boolean isRemoteClaimRequest) {

        this.isRemoteClaimRequest = isRemoteClaimRequest;
        this.idp = idp;
        this.step = step;
    }

    @Override
    public void initializeContext(AuthenticationContext context) {

        super.initializeContext(context);
        if (StringUtils.isNotBlank(idp) && getContext().getCurrentAuthenticatedIdPs().containsKey(idp)) {
            this.authenticatedUser = getContext().getCurrentAuthenticatedIdPs().get(idp).getUser();
        }
    }

    /**
     * Constructor to get user who is not directly from a authentication step. Eg. Associated user of authenticated
     * federated user in a authentication step.
     *
     * @param authenticatedUser    Authenticated user
     * @param isRemoteClaimRequest Whether the request is for remote claim (false for local claim request)
     */
    public JsClaims(AuthenticatedUser authenticatedUser, boolean isRemoteClaimRequest) {

        this.isRemoteClaimRequest = isRemoteClaimRequest;
        this.authenticatedUser = authenticatedUser;
    }

    public JsClaims(AuthenticationContext context,AuthenticatedUser authenticatedUser, boolean isRemoteClaimRequest) {

        this(authenticatedUser, isRemoteClaimRequest);
        initializeContext(context);
    }

    @Override
    public Object getMember(String claimUri) {

        if (authenticatedUser != null) {
            if (isRemoteClaimRequest) {
                return getFederatedClaim(claimUri);
            } else {
                return getLocalClaim(claimUri);
            }
        }
        return null;
    }

    @Override
    public boolean hasMember(String claimUri) {

        if (authenticatedUser != null) {
            if (isRemoteClaimRequest) {
                return hasFederatedClaim(claimUri);
            } else {
                return hasLocalClaim(claimUri);
            }
        }
        return false;
    }

    @Override
    public void setMember(String claimUri, Object claimValue) {

        if (authenticatedUser != null) {
            if (isRemoteClaimRequest) {
                setFederatedClaim(claimUri, claimValue);
                return;
            } else {
                setLocalClaim(claimUri, claimValue);
                return;
            }
        }
        super.setMember(claimUri, claimValue);
    }

    /**
     * Get the claim by local claim URI.
     *
     * @param claimUri   Local claim URI
     * @param claimValue Claim Value
     */
    private void setLocalClaim(String claimUri, Object claimValue) {

        if (isFederatedIdP()) {
            setLocalMappedClaim(claimUri, claimValue);
        } else {
            // This covers step with a local authenticator, and the scenarios where step/idp is not set
            // if the step/idp is not set, user is assumed to be a local user
            setLocalUserClaim(claimUri, claimValue);
        }
    }

    /**
     * Sets the remote claim value that is mapped to the give local claim
     *
     * @param localClaimURI Local claim URI
     * @param claimValue    Value to be set
     */
    private void setLocalMappedClaim(String localClaimURI, Object claimValue) {

        Map<ClaimMapping, String> idpAttributesMap = authenticatedUser.getUserAttributes();
        Map<String, String> remoteMapping = FrameworkUtils.getClaimMappings(idpAttributesMap, false);
        String mappedRemoteClaim = getRemoteClaimMappedToLocalClaim(localClaimURI, remoteMapping);
        if (mappedRemoteClaim != null) {
            setFederatedClaim(mappedRemoteClaim, String.valueOf(claimValue));
        }
    }

    /**
     * Sets a local claim directly at the userstore for the given user by given claim uri
     *
     * @param claimUri   Local claim URI
     * @param claimValue Claim value
     */
    private void setLocalUserClaim(String claimUri, Object claimValue) {

        int usersTenantId = IdentityTenantUtil.getTenantId(authenticatedUser.getTenantDomain());
        RealmService realmService = FrameworkServiceDataHolder.getInstance().getRealmService();
        String usernameWithDomain = UserCoreUtil.addDomainToName(authenticatedUser.getUserName(), authenticatedUser
            .getUserStoreDomain());
        try {
            UserRealm userRealm = realmService.getTenantUserRealm(usersTenantId);
            Map<String, String> claimUriMap = new HashMap<>();
            claimUriMap.put(claimUri, String.valueOf(claimValue));
            userRealm.getUserStoreManager().setUserClaimValues(usernameWithDomain, claimUriMap, null);
        } catch (UserStoreException e) {
            LOG.error(String.format("Error when setting claim : %s of user: %s to value: %s", claimUri,
                    authenticatedUser, String.valueOf(claimValue)), e);
        }
    }

    /**
     * Gets the remote claim that is mapped to the given local claim
     *
     * @param localClaim      local claim URI
     * @param remoteClaimsMap Remote claim URI - value map
     * @return Mapped remote claim URI if present. null otherwise
     */
    private String getRemoteClaimMappedToLocalClaim(String localClaim, Map<String, String> remoteClaimsMap) {

        String authenticatorDialect = null;
        Map<String, String> localToIdpClaimMapping = null;
        String tenantDomain = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantDomain();
        try {
            // Check if the IDP use an standard dialect (like oidc), If it does, dialect claim mapping are
            // prioritized over IdP claim mapping
            ApplicationAuthenticator authenticator = getContext().getSequenceConfig().getStepMap().get(step)
                    .getAuthenticatedAutenticator().getApplicationAuthenticator();
            authenticatorDialect = authenticator.getClaimDialectURI();
            ExternalIdPConfig idPConfig = ConfigurationFacade.getInstance().getIdPConfigByName(idp, tenantDomain);
            boolean useDefaultIdpDialect = idPConfig.useDefaultLocalIdpDialect();

            if (authenticatorDialect != null || useDefaultIdpDialect) {
                if (authenticatorDialect == null) {
                    authenticatorDialect = ApplicationConstants.LOCAL_IDP_DEFAULT_CLAIM_DIALECT;
                }
                localToIdpClaimMapping = ClaimMetadataHandler.getInstance().getMappingsMapFromOtherDialectToCarbon
                        (authenticatorDialect, remoteClaimsMap.keySet(), tenantDomain, true);
            } else {
                localToIdpClaimMapping = IdentityProviderManager.getInstance().getMappedIdPClaimsMap
                        (idp, PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantDomain(), Collections
                                .singletonList(localClaim));

            }
            if (localToIdpClaimMapping != null) {
                return localToIdpClaimMapping.get(localClaim);
            }
        } catch (IdentityProviderManagementException e) {
            LOG.error(String.format("Error when getting claim : %s of user: %s", localClaim, authenticatedUser), e);
        } catch (ClaimMetadataException e) {
            LOG.error("Error when getting claim mappings from " + authenticatorDialect + " for tenant domain: " +
                    tenantDomain);
        }
        return null;
    }

    /**
     * Check if the user has a federated claim with given name.
     *
     * @param claimUri Federated claim URI
     * @return <code>true</code> if the IdP is federated and it has a claim for user with given URI.
     * <code>false</code> otherwise
     */
    private boolean hasFederatedClaim(String claimUri) {

        if (isFederatedIdP()) {
            Map<ClaimMapping, String> attributesMap = authenticatedUser.getUserAttributes();
            Map<String, String> remoteMapping = FrameworkUtils.getClaimMappings(attributesMap, false);
            return remoteMapping.containsKey(claimUri);
        }
        // Can be a case where step is not set (e.g. associated local user)
        return false;
    }

    /**
     * Check if there is a local claim by given name.
     *
     * @param claimUri The local claim URI
     * @return Claim value of the user authenticated by the indicated IdP
     */
    private boolean hasLocalClaim(String claimUri) {

        int usersTenantId = IdentityTenantUtil.getTenantId(authenticatedUser.getTenantDomain());
        RealmService realmService = FrameworkServiceDataHolder.getInstance().getRealmService();
        try {
            UserRealm userRealm = realmService.getTenantUserRealm(usersTenantId);
            Claim[] supportedClaims = IdentityClaimManager.getInstance().getAllSupportedClaims((org.wso2.carbon.user
                .core.UserRealm) userRealm);
            for (Claim claim : supportedClaims) {
                if (claim.getClaimUri().equals(claimUri)) {
                    return true;
                }
            }
        } catch (UserStoreException e) {
            LOG.error("Error when retrieving user realm for tenant : " + usersTenantId, e);
        } catch (IdentityException e) {
            LOG.error("Error when initializing identity claim manager.", e);
        }
        return false;
    }

    /**
     * Get the claim by federated claim URI.
     *
     * @param claimUri Federated claim URI
     * @return Claim value if the Idp is a federated Idp, and has a claim by given url for the user.
     * <code>null</code> otherwise.
     */
    private String getFederatedClaim(String claimUri) {

        // If the idp is local, return null
        if (isFederatedIdP()) {
            Map<ClaimMapping, String> attributesMap = authenticatedUser.getUserAttributes();
            Map<String, String> remoteMapping = FrameworkUtils.getClaimMappings(attributesMap, false);
            return remoteMapping.get(claimUri);
        }
        // Can be a case where step is not set (e.g. associated local user)
        return null;
    }

    /**
     * Get the claim by local claim URI.
     *
     * @param claimUri Local claim URI
     * @return Local user's claim value if the Idp is local, Mapped remote claim if the Idp is federated.
     */
    private String getLocalClaim(String claimUri) {

        if (isFederatedIdP()) {
            return getLocalMappedClaim(claimUri);
        } else {
            // This covers step with a local authenticator, and the scenarios where step/idp is not set
            // if the step/idp is not set, user is assumed to be a local user
            return getLocalUserClaim(claimUri);
        }
    }

    /**
     * Check if step's IdP is a federated IDP
     *
     * @return true if the idp is federated
     */
    private boolean isFederatedIdP() {

        return StringUtils.isNotBlank(idp) && !FrameworkConstants.LOCAL.equals(idp);
    }

    /**
     * Sets a custom remote claim to the user.
     *
     * @param claimUri   Remote claim uri
     * @param claimValue Claim value
     */
    private void setFederatedClaim(String claimUri, Object claimValue) {

        if (claimValue == null) {
            claimValue = StringUtils.EMPTY;
        }
        ClaimMapping newClaimMapping = ClaimMapping.build(claimUri, claimUri, null, false);
        authenticatedUser.getUserAttributes().put(newClaimMapping, String.valueOf(claimValue));
    }

    /**
     * Gets the mapped remote claim value for the given local claim URI
     *
     * @param claimUri Local claim URI
     * @return Mapped remote claim value from IdP
     */
    private String getLocalMappedClaim(String claimUri) {

        Map<ClaimMapping, String> idpAttributesMap = authenticatedUser.getUserAttributes();
        Map<String, String> remoteMapping = FrameworkUtils.getClaimMappings(idpAttributesMap, false);

        String remoteMappedClaim = getRemoteClaimMappedToLocalClaim(claimUri, remoteMapping);
        if (remoteMappedClaim != null) {
            return remoteMapping.get(remoteMappedClaim);
        }
        return null;
    }

    /**
     * Get the local user claim value specified by the Claim URI.
     *
     * @param claimUri Local claim URI
     * @return Claim value of the given claim URI for the local user if available. Null Otherwise.
     */
    private String getLocalUserClaim(String claimUri) {

        int usersTenantId = IdentityTenantUtil.getTenantId(authenticatedUser.getTenantDomain());
        String usernameWithDomain = UserCoreUtil.addDomainToName(authenticatedUser.getUserName(), authenticatedUser
            .getUserStoreDomain());
        RealmService realmService = FrameworkServiceDataHolder.getInstance().getRealmService();
        try {
            UserRealm userRealm = realmService.getTenantUserRealm(usersTenantId);
            Map<String, String> claimValues = userRealm.getUserStoreManager().getUserClaimValues(usernameWithDomain, new
                String[]{claimUri}, null);
            return claimValues.get(claimUri);
        } catch (UserStoreException e) {
            LOG.error(String.format("Error when getting claim : %s of user: %s", claimUri, authenticatedUser), e);
        }
        return null;
    }
}
