/*
 * Copyright (c) 2023, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.carbon.identity.application.authentication.framework.config.model.graph.js.graaljs;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.graalvm.polyglot.Value;
import org.graalvm.polyglot.proxy.ProxyArray;
import org.graalvm.polyglot.proxy.ProxyObject;
import org.wso2.carbon.identity.application.authentication.framework.config.model.graph.js.JsClaims;
import org.wso2.carbon.identity.application.authentication.framework.config.model.graph.js.nashorn.JsNashornClaims;
import org.wso2.carbon.identity.application.authentication.framework.context.AuthenticationContext;
import org.wso2.carbon.identity.application.authentication.framework.internal.FrameworkServiceDataHolder;
import org.wso2.carbon.identity.application.authentication.framework.model.AuthenticatedUser;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;
import org.wso2.carbon.user.api.UserRealm;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.user.core.util.UserCoreUtil;

import java.util.HashMap;
import java.util.Map;

/**
 * Represent the user's claim for GraalJs Execution. Can be either remote or local.
 */
public class JsGraalClaims extends JsClaims implements ProxyObject {

    private final transient Map<String, String> localClaimUriToValueReadCache = new HashMap<>();
    private static final Log LOG = LogFactory.getLog(JsNashornClaims.class);

    public JsGraalClaims(AuthenticationContext context, int step, String idp, boolean isRemoteClaimRequest) {

        super(context, step, idp, isRemoteClaimRequest);
    }

    public JsGraalClaims(AuthenticationContext context, AuthenticatedUser authenticatedUser,
                         boolean isRemoteClaimRequest) {

        super(context, authenticatedUser, isRemoteClaimRequest);
    }

    @Override
    public Object getMemberKeys() {

        return ProxyArray.fromArray();
    }

    @Override
    public void putMember(String claimUri, Value claimValue) {

        if (authenticatedUser != null) {
            if (isRemoteClaimRequest) {
                setFederatedClaim(claimUri, String.valueOf(claimValue));
            } else {
                setLocalClaim(claimUri, String.valueOf(claimValue));
            }
        }
    }

    /**
     * Sets a local claim directly at the userstore for the given user by given claim uri.
     *
     * @param claimUri   Local claim URI
     * @param claimValue Claim value
     */
    protected void setLocalUserClaim(String claimUri, Object claimValue) {

        localClaimUriToValueReadCache.clear();

        int usersTenantId = IdentityTenantUtil.getTenantId(authenticatedUser.getTenantDomain());
        RealmService realmService = FrameworkServiceDataHolder.getInstance().getRealmService();
        String usernameWithDomain =
                UserCoreUtil.addDomainToName(authenticatedUser.getUserName(), authenticatedUser.getUserStoreDomain());
        try {
            UserRealm userRealm = realmService.getTenantUserRealm(usersTenantId);
            Map<String, String> claimUriMap = new HashMap<>();
            claimUriMap.put(claimUri, String.valueOf(claimValue));
            userRealm.getUserStoreManager().setUserClaimValues(usernameWithDomain, claimUriMap, null);
        } catch (UserStoreException e) {
            LOG.error(
                    String.format("Error when setting claim : %s of user: %s to value: %s", claimUri, authenticatedUser,
                            claimValue), e);
        }
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
     * Get the local user claim value specified by the Claim URI.
     *
     * @param claimUri Local claim URI
     * @return Claim value of the given claim URI for the local user if available. Null Otherwise.
     */
    protected String getLocalUserClaim(String claimUri) {

        String value = localClaimUriToValueReadCache.get(claimUri);
        if (value != null) {
            return value;
        }
        int usersTenantId = IdentityTenantUtil.getTenantId(authenticatedUser.getTenantDomain());
        String usernameWithDomain =
                UserCoreUtil.addDomainToName(authenticatedUser.getUserName(), authenticatedUser.getUserStoreDomain());
        RealmService realmService = FrameworkServiceDataHolder.getInstance().getRealmService();
        try {
            UserRealm userRealm = realmService.getTenantUserRealm(usersTenantId);
            Map<String, String> claimValues = userRealm.getUserStoreManager()
                    .getUserClaimValues(usernameWithDomain, new String[]{claimUri}, null);
            return claimValues.get(claimUri);
        } catch (UserStoreException e) {
            LOG.error(String.format("Error when getting claim : %s of user: %s", claimUri, authenticatedUser), e);
        }
        return null;
    }
}
