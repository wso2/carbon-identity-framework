package org.wso2.carbon.identity.application.authentication.framework.config.model.graph;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.graalvm.polyglot.Value;
import org.wso2.carbon.identity.application.authentication.framework.ApplicationAuthenticator;
import org.wso2.carbon.identity.application.authentication.framework.config.ConfigurationFacade;
import org.wso2.carbon.identity.application.authentication.framework.config.model.ExternalIdPConfig;
import org.wso2.carbon.identity.application.authentication.framework.config.model.StepConfig;
import org.wso2.carbon.identity.application.authentication.framework.context.AuthenticationContext;
import org.wso2.carbon.identity.application.authentication.framework.internal.FrameworkServiceDataHolder;
import org.wso2.carbon.identity.application.authentication.framework.model.AuthenticatedUser;
import org.wso2.carbon.identity.application.authentication.framework.util.FrameworkUtils;
import org.wso2.carbon.identity.application.common.model.ClaimMapping;
import org.wso2.carbon.identity.application.mgt.ApplicationConstants;
import org.wso2.carbon.identity.claim.metadata.mgt.ClaimMetadataHandler;
import org.wso2.carbon.identity.claim.metadata.mgt.exception.ClaimMetadataException;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;
import org.wso2.carbon.idp.mgt.IdentityProviderManagementException;
import org.wso2.carbon.idp.mgt.IdentityProviderManager;
import org.wso2.carbon.user.api.UserRealm;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.user.core.util.UserCoreUtil;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

class GraphUtils {

    /**
     * Get authenticated user from step config of current subject identifier.
     *
     * @return AuthenticatedUser.
     */
    static AuthenticatedUser getAuthenticatedUserFromSubjectIdentifierStep(AuthenticationContext context) {

        AuthenticatedUser authenticatedUser = null;
        StepConfig stepConfig = getCurrentSubjectIdentifierStep(context);
        if (stepConfig != null) {
            authenticatedUser = getCurrentSubjectIdentifierStep(context).getAuthenticatedUser();
        }
        return authenticatedUser;
    }

    /**
     * Retrieve step config of current subject identifier.
     *
     * @return StepConfig.
     */
    static StepConfig getCurrentSubjectIdentifierStep(AuthenticationContext context) {

        if (context.getSequenceConfig() == null) {
            // Sequence config is not yet initialized.
            return null;
        }
        Map<Integer, StepConfig> stepConfigs = context.getSequenceConfig().getStepMap();
        Optional<StepConfig> subjectIdentifierStep = stepConfigs.values().stream()
                .filter(stepConfig -> (stepConfig.isCompleted() && stepConfig.isSubjectIdentifierStep())).findFirst();
        if (subjectIdentifierStep.isPresent()) {
            return subjectIdentifierStep.get();
        } else if (context.getCurrentStep() > 0) {
            return stepConfigs.get(context.getCurrentStep());
        } else {
            return null;
        }
    }

    /**
     * Get the claim by local claim URI.
     *
     * @param claimUri   Local claim URI
     * @param claimValue Claim Value
     */
    private void setLocalClaim(String claimUri, Value claimValue, boolean isFederatedIdP, AuthenticatedUser authenticatedUser, Log log) {

        if (isFederatedIdP) {
            setLocalMappedClaim(claimUri, claimValue, authenticatedUser);
        } else {
            // This covers step with a local authenticator, and the scenarios where step/idp is not set
            // if the step/idp is not set, user is assumed to be a local user
            setLocalUserClaim(claimUri, claimValue, authenticatedUser, log);
        }
    }

    /**
     * Sets the remote claim value that is mapped to the give local claim.
     *
     * @param localClaimURI Local claim URI
     * @param claimValue    Value to be set
     */
    private void setLocalMappedClaim(String localClaimURI, Value claimValue, AuthenticatedUser authenticatedUser) {

        Map<ClaimMapping, String> idpAttributesMap = authenticatedUser.getUserAttributes();
        Map<String, String> remoteMapping = FrameworkUtils.getClaimMappings(idpAttributesMap, false);
        String mappedRemoteClaim = getRemoteClaimMappedToLocalClaim(localClaimURI, remoteMapping);
        if (mappedRemoteClaim != null) {
            setFederatedClaim(mappedRemoteClaim, String.valueOf(claimValue));
        }
    }

    /**
     * Sets a local claim directly at the userstore for the given user by given claim uri.
     *
     * @param claimUri   Local claim URI
     * @param claimValue Claim value
     */
    private void setLocalUserClaim(String claimUri, Object claimValue, AuthenticatedUser authenticatedUser, Log log) {

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
            log.error(String.format("Error when setting claim : %s of user: %s to value: %s", claimUri,
                    authenticatedUser, String.valueOf(claimValue)), e);
        }
    }

    /**
     * Gets the remote claim that is mapped to the given local claim.
     *
     * @param localClaim      local claim URI
     * @param remoteClaimsMap Remote claim URI - value map
     * @return Mapped remote claim URI if present. null otherwise
     */
    private String getRemoteClaimMappedToLocalClaim(String localClaim, Map<String, String> remoteClaimsMap, AuthenticationContext context) {

        String authenticatorDialect = null;
        Map<String, String> localToIdpClaimMapping;
        String tenantDomain = context.getTenantDomain();
        try {
            // Check if the IDP use an standard dialect (like oidc), If it does, dialect claim mapping are
            // prioritized over IdP claim mapping
            ApplicationAuthenticator authenticator = context.getSequenceConfig().getStepMap().get(step)
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
                        (idp, tenantDomain, Collections
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
    protected boolean hasFederatedClaim(String claimUri) {

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
    protected boolean hasLocalClaim(String claimUri) {
        String value = localClaimUriToValueReadCache.get(claimUri);
        if (value != null) {
            return true;
        }
        value = getLocalClaim(claimUri);
        if (value != null) {
            localClaimUriToValueReadCache.put(claimUri, value);
            return true;
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
    protected String getFederatedClaim(String claimUri) {

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
    protected String getLocalClaim(String claimUri) {

        if (isFederatedIdP()) {
            return getLocalMappedClaim(claimUri);
        } else {
            // This covers step with a local authenticator, and the scenarios where step/idp is not set
            // if the step/idp is not set, user is assumed to be a local user
            return getLocalUserClaim(claimUri);
        }
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
     * Gets the mapped remote claim value for the given local claim URI.
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
    private String getLocalUserClaim(String claimUri, Log ) {

        String value = localClaimUriToValueReadCache.get(claimUri);
        if (value != null) {
            return value;
        }
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
