/*
 * Copyright (c) 2013, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.identity.oauth2.token.handlers.grant;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.base.MultitenantConstants;
import org.wso2.carbon.identity.application.common.IdentityApplicationManagementException;
import org.wso2.carbon.identity.application.common.model.ServiceProvider;
import org.wso2.carbon.identity.base.IdentityRuntimeException;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;
import org.wso2.carbon.identity.oauth.internal.OAuthComponentServiceHolder;
import org.wso2.carbon.identity.oauth2.IdentityOAuth2Exception;
import org.wso2.carbon.identity.oauth2.dto.OAuth2AccessTokenReqDTO;
import org.wso2.carbon.identity.oauth2.internal.OAuth2ServiceComponentHolder;
import org.wso2.carbon.identity.oauth2.token.OAuthTokenReqMessageContext;
import org.wso2.carbon.identity.oauth2.util.OAuth2Util;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.api.UserStoreManager;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.user.core.util.UserCoreUtil;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

/**
 * Handles the Password Grant Type of the OAuth 2.0 specification. Resource owner sends his
 * credentials in the token request which is validated against the corresponding user store.
 * Grant Type : password
 */
public class PasswordGrantHandler extends AbstractAuthorizationGrantHandler {

    private static Log log = LogFactory.getLog(PasswordGrantHandler.class);

    @Override
    public boolean validateGrant(OAuthTokenReqMessageContext tokReqMsgCtx)
            throws IdentityOAuth2Exception {

        if(!super.validateGrant(tokReqMsgCtx)){
            return false;
        }

        OAuth2AccessTokenReqDTO oAuth2AccessTokenReqDTO = tokReqMsgCtx.getOauth2AccessTokenReqDTO();
        String username = oAuth2AccessTokenReqDTO.getResourceOwnerUsername();
        String userTenantDomain = MultitenantUtils.getTenantDomain(username);
        String clientId = oAuth2AccessTokenReqDTO.getClientId();
        String tenantDomain = oAuth2AccessTokenReqDTO.getTenantDomain();
        ServiceProvider serviceProvider = null;
        try {
            serviceProvider = OAuth2ServiceComponentHolder.getApplicationMgtService().getServiceProviderByClientId(
                    clientId, "oauth2", tenantDomain);
        } catch (IdentityApplicationManagementException e) {
            throw new IdentityOAuth2Exception("Error occurred while retrieving OAuth2 application data for client id " +
                    clientId, e);
        }
        if(!serviceProvider.isSaasApp() && !userTenantDomain.equals(tenantDomain)){
            if(log.isDebugEnabled()) {
                log.debug("Non-SaaS service provider tenant domain is not same as user tenant domain; " +
                        tenantDomain + " != " + userTenantDomain);
            }
            return false;

        }
        String tenantAwareUserName = MultitenantUtils.getTenantAwareUsername(username);
        username = tenantAwareUserName + "@" + userTenantDomain;
        int tenantId = MultitenantConstants.INVALID_TENANT_ID;
        try {
            tenantId = IdentityTenantUtil.getTenantIdOfUser(username);
        } catch (IdentityRuntimeException e) {
            log.error("Token request with Password Grant Type for an invalid tenant : " +
                    MultitenantUtils.getTenantDomain(username));
            return false;
        }

        RealmService realmService = OAuthComponentServiceHolder.getRealmService();
        UserStoreManager userStoreManager = null;
        boolean authStatus;
        try {
            userStoreManager = realmService.getTenantUserRealm(tenantId).getUserStoreManager();
            authStatus = userStoreManager.authenticate(tenantAwareUserName, oAuth2AccessTokenReqDTO.getResourceOwnerPassword());

            if (log.isDebugEnabled()) {
                log.debug("Token request with Password Grant Type received. " +
                        "Username : " + username +
                        "Scope : " + OAuth2Util.buildScopeString(oAuth2AccessTokenReqDTO.getScope()) +
                        ", Authentication State : " + authStatus);
            }

        } catch (UserStoreException e) {
            throw new IdentityOAuth2Exception(e.getMessage(), e);
        }
        if (authStatus) {
            if (!username.contains(CarbonConstants.DOMAIN_SEPARATOR) && StringUtils.isNotBlank(UserCoreUtil
                    .getDomainFromThreadLocal())) {
                username = UserCoreUtil.getDomainFromThreadLocal() + CarbonConstants.DOMAIN_SEPARATOR + username;
            }
            tokReqMsgCtx.setAuthorizedUser(OAuth2Util.getUserFromUserName(username));
            tokReqMsgCtx.setScope(oAuth2AccessTokenReqDTO.getScope());
        } else {
            throw new IdentityOAuth2Exception("Authentication failed for " + username);
        }
        return authStatus;
    }
}
