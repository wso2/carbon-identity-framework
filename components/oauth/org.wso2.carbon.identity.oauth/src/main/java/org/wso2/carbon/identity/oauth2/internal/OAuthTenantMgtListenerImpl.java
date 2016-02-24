/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.identity.oauth2.internal;

import org.wso2.carbon.identity.oauth.OAuthUtil;
import org.wso2.carbon.identity.oauth2.IdentityOAuth2Exception;
import org.wso2.carbon.identity.oauth2.dao.TokenMgtDAO;
import org.wso2.carbon.identity.oauth2.model.AccessTokenDO;
import org.wso2.carbon.identity.oauth2.model.AuthzCodeDO;
import org.wso2.carbon.identity.oauth2.util.OAuth2Util;
import org.wso2.carbon.stratos.common.beans.TenantInfoBean;
import org.wso2.carbon.stratos.common.exception.StratosException;
import org.wso2.carbon.stratos.common.listeners.TenantMgtListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class OAuthTenantMgtListenerImpl implements TenantMgtListener {
    @Override
    public void onTenantCreate(TenantInfoBean tenantInfoBean) throws StratosException {
        return;
    }

    @Override
    public void onTenantUpdate(TenantInfoBean tenantInfoBean) throws StratosException {
        return;
    }

    @Override
    public void onTenantDelete(int i) {
        return;
    }

    @Override
    public void onTenantRename(int i, String s, String s1) throws StratosException {
        return;
    }

    @Override
    public void onTenantInitialActivation(int i) throws StratosException {
        return;
    }

    @Override
    public void onTenantActivation(int i) throws StratosException {
        return;
    }

    @Override
    public void onTenantDeactivation(int i) throws StratosException {
        return;
    }

    @Override
    public void onSubscriptionPlanChange(int i, String s, String s1) throws StratosException {
        return;
    }

    @Override
    public int getListenerOrder() {
        return 0;
    }

    @Override
    public void onPreDelete(int tenantId) throws StratosException {
        TokenMgtDAO tokenMgtDAO = new TokenMgtDAO();
        try {
            Set<AccessTokenDO> accessTokenDOs = tokenMgtDAO.getAccessTokensOfTenant(tenantId);
            Map<String, AccessTokenDO> latestAccessTokens = new HashMap<>();
            for (AccessTokenDO accessTokenDO : accessTokenDOs) {
                String keyString = accessTokenDO.getConsumerKey() + ":" + accessTokenDO.getAuthzUser() + ":" +
                        OAuth2Util.buildScopeString(accessTokenDO.getScope());
                AccessTokenDO accessTokenDOFromMap = latestAccessTokens.get(keyString);
                if (accessTokenDOFromMap != null) {
                    if (accessTokenDOFromMap.getIssuedTime().before(accessTokenDO.getIssuedTime())) {
                        latestAccessTokens.put(keyString, accessTokenDO);
                    }
                } else {
                    latestAccessTokens.put(keyString, accessTokenDO);
                }

                //Clear cache
                OAuthUtil.clearOAuthCache(accessTokenDO.getConsumerKey(), accessTokenDO.getAuthzUser(),
                        OAuth2Util.buildScopeString(accessTokenDO.getScope()));
                OAuthUtil.clearOAuthCache(accessTokenDO.getConsumerKey(), accessTokenDO.getAuthzUser());
                OAuthUtil.clearOAuthCache(accessTokenDO.getAccessToken());
            }
            ArrayList<String> tokensToRevoke = new ArrayList<>();
            for (Map.Entry entry : latestAccessTokens.entrySet()) {
                tokensToRevoke.add(((AccessTokenDO) entry.getValue()).getAccessToken());
            }
            tokenMgtDAO.revokeTokens(tokensToRevoke.toArray(new String[tokensToRevoke.size()]));
            List<AuthzCodeDO> latestAuthzCodes = tokenMgtDAO.getLatestAuthorizationCodesOfTenant(tenantId);
            for (AuthzCodeDO authzCodeDO : latestAuthzCodes) {
                // remove the authorization code from the cache
                OAuthUtil.clearOAuthCache(authzCodeDO.getConsumerKey() + ":" +
                        authzCodeDO.getAuthorizationCode());

            }
            tokenMgtDAO.deactivateAuthorizationCode(latestAuthzCodes);
        } catch (IdentityOAuth2Exception e) {
            throw new StratosException("Error occurred while revoking the access tokens in tenant " + tenantId, e);
        }
    }
}
