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

import jdk.nashorn.api.scripting.AbstractJSObject;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.identity.application.authentication.framework.ApplicationAuthenticator;
import org.wso2.carbon.identity.application.authentication.framework.config.ConfigurationFacade;
import org.wso2.carbon.identity.application.authentication.framework.config.model.ExternalIdPConfig;
import org.wso2.carbon.identity.application.authentication.framework.context.AuthenticationContext;
import org.wso2.carbon.identity.application.authentication.framework.internal.FrameworkServiceDataHolder;
import org.wso2.carbon.identity.application.authentication.framework.model.AuthenticatedIdPData;
import org.wso2.carbon.identity.application.authentication.framework.util.FrameworkConstants;
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

import java.util.Collections;
import java.util.Map;

public class JsClaims extends AbstractJSObject {

    private AuthenticationContext wrappedContext;
    private String idp;
    private boolean isFederated;
    private int step;

    private static final Log LOG = LogFactory.getLog(JsClaims.class);

    public JsClaims(AuthenticationContext wrappedContext, int step, String idp, boolean isFederated) {

        this.wrappedContext = wrappedContext;
        this.isFederated = isFederated;
        this.idp = idp;
        this.step = step;
    }

    @Override
    public Object getMember(String name) {

        if (isFederated) {
            return getFederatedClaim(name);
        } else {
            return getLocalClaim(name);
        }
    }

    @Override
    public boolean hasMember(String claimUri) {

        if (isFederated) {
            return hasFederatedClaim(claimUri);
        } else {
            return hasLocalClaim(claimUri);
        }
    }

    /**
     * Check if the user has a federated claim with given name.
     * @param claimUri Federated claim URI
     * @return <code>true</code> if the IdP is federated and it has a claim for user with given URI.
     * <code>false</code> otherwise
     */
    private boolean hasFederatedClaim(String claimUri) {

        if (isFederatedIdP()) {
            AuthenticatedIdPData idPData = wrappedContext.getCurrentAuthenticatedIdPs().get(idp);
            Map<ClaimMapping, String> attributesMap = idPData.getUser().getUserAttributes();
            Map<String, String> remoteMapping = FrameworkUtils.getClaimMappings(attributesMap, false);
            return remoteMapping.containsKey(claimUri);
        }
        return false;
    }

    /**
     * Get the claim by federated claim URI.
     * @param claimUri Federated claim URI
     * @return Claim value if the Idp is a federated Idp, and has a claim by given url for the user.
     * <code>null</code> otherwise.
     */
    private String getFederatedClaim(String claimUri) {

        // If the idp is local, return null
        if (StringUtils.isNotBlank(idp) && !FrameworkConstants.LOCAL_IDP_NAME.equals(idp)) {
            AuthenticatedIdPData idPData = wrappedContext.getCurrentAuthenticatedIdPs().get(idp);
            Map<ClaimMapping, String> attributesMap = idPData.getUser().getUserAttributes();
            Map<String, String> remoteMapping = FrameworkUtils.getClaimMappings(attributesMap, false);
            return remoteMapping.get(claimUri);
        }
        return null;
    }

    /**
     * Check if there is a local claim by given name
     * @param name
     * @return
     */
    private boolean hasLocalClaim(String name) {

        return true;
    }

    /**
     * Get the claim by local claim URI.
     * @param claimUri Local claim URI
     * @return Local user's claim value if the Idp is local, Mapped remote claim if the Idp is federated.
     */
    private String getLocalClaim(String claimUri) {

        if (isFederatedIdP()) {
            return getLocalMappedClaim(claimUri);
        } else if (isLocalIdP()) {
            return getLocalUserClaim(claimUri);
        }
        return null;
    }

    private String getLocalUserClaim(String claimUri) {

        AuthenticatedIdPData idPData = wrappedContext.getCurrentAuthenticatedIdPs().get(idp);
        int usersTenantId = IdentityTenantUtil.getTenantId(idPData.getUser().getTenantDomain());
        RealmService realmService = FrameworkServiceDataHolder.getInstance().getRealmService();
        try {
            UserRealm userRealm = realmService.getTenantUserRealm(usersTenantId);
            Map<String, String> claimValues = userRealm.getUserStoreManager().getUserClaimValues(idPData.getUser()
                    .getUserName(), new String[]{claimUri}, null);
            return claimValues.get(claimUri);
        } catch (UserStoreException e) {
            LOG.error(String.format("Error when getting claim : %s of user: %s", claimUri, idPData.getUser()), e);
        }
        return null;
    }

    private String getLocalMappedClaim(String claimUri) {

        AuthenticatedIdPData idPData = wrappedContext.getCurrentAuthenticatedIdPs().get(idp);
        Map<ClaimMapping, String> idpAttributesMap = idPData.getUser().getUserAttributes();
        String authenticatorDialect = null;
        Map<String, String> localToIdpClaimMapping = null;
        String tenantDomain = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantDomain();
        try {
            // Check if the IDP use an standard dialect (like oidc), If it does, dialect claim mapping are
            // prioritized over IdP claim mapping
            ApplicationAuthenticator authenticator = wrappedContext.getSequenceConfig().getStepMap().get(step)
                    .getAuthenticatedAutenticator().getApplicationAuthenticator();
            authenticatorDialect = authenticator.getClaimDialectURI();
            ExternalIdPConfig idPConfig = ConfigurationFacade.getInstance().getIdPConfigByName(idp, tenantDomain);
            boolean useDefaultIdpDialect = idPConfig.useDefaultLocalIdpDialect();
            Map<String, String> remoteMapping = FrameworkUtils.getClaimMappings(idpAttributesMap, false);

            if (authenticatorDialect != null || useDefaultIdpDialect) {
                if (authenticatorDialect == null) {
                    authenticatorDialect = ApplicationConstants.LOCAL_IDP_DEFAULT_CLAIM_DIALECT;
                }
                localToIdpClaimMapping = ClaimMetadataHandler.getInstance().getMappingsMapFromOtherDialectToCarbon
                        (authenticatorDialect, remoteMapping.keySet(), tenantDomain, true);
            } else {
                localToIdpClaimMapping = IdentityProviderManager.getInstance().getMappedIdPClaimsMap
                        (idp, PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantDomain(), Collections
                                .singletonList(claimUri));

            }
            if (localToIdpClaimMapping != null) {
                return remoteMapping.get(localToIdpClaimMapping.get(claimUri));
            }
        } catch (IdentityProviderManagementException e) {
            LOG.error(String.format("Error when getting claim : %s of user: %s", claimUri, idPData.getUser()), e);
        } catch (ClaimMetadataException e) {
            LOG.error("Error when getting claim mappings from " + authenticatorDialect + " for tenant domain: " +
                    tenantDomain);
        }
        return null;
    }

    private boolean isFederatedIdP() {

        return StringUtils.isNotBlank(idp) && !FrameworkConstants.LOCAL.equals(idp);
    }

    private boolean isLocalIdP() {

        return StringUtils.isNotBlank(idp) && FrameworkConstants.LOCAL.equals(idp);
    }
}
