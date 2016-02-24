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

package org.wso2.carbon.identity.oauth;


import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.core.AbstractAdmin;
import org.wso2.carbon.identity.application.authentication.framework.model.AuthenticatedUser;
import org.wso2.carbon.identity.application.common.model.User;
import org.wso2.carbon.identity.oauth.dao.OAuthAppDO;
import org.wso2.carbon.identity.core.util.IdentityUtil;
import org.wso2.carbon.identity.oauth.cache.AppInfoCache;
import org.wso2.carbon.identity.oauth.cache.OAuthCache;
import org.wso2.carbon.identity.oauth.cache.OAuthCacheKey;
import org.wso2.carbon.identity.oauth.common.OAuth2ErrorCodes;
import org.wso2.carbon.identity.oauth.common.OAuthConstants;
import org.wso2.carbon.identity.oauth.common.exception.InvalidOAuthClientException;
import org.wso2.carbon.identity.oauth.config.OAuthServerConfiguration;
import org.wso2.carbon.identity.oauth.dao.OAuthAppDAO;
import org.wso2.carbon.identity.oauth.dto.OAuthConsumerAppDTO;
import org.wso2.carbon.identity.oauth.dto.OAuthRevocationRequestDTO;
import org.wso2.carbon.identity.oauth.dto.OAuthRevocationResponseDTO;
import org.wso2.carbon.identity.oauth2.IdentityOAuth2Exception;
import org.wso2.carbon.identity.oauth2.dao.TokenMgtDAO;
import org.wso2.carbon.identity.oauth2.model.AccessTokenDO;
import org.wso2.carbon.identity.oauth2.util.OAuth2Util;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.core.util.UserCoreUtil;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class OAuthAdminService extends AbstractAdmin {

    public static final String IMPLICIT = "implicit";
    public static final String AUTHORIZATION_CODE = "authorization_code";
    private static List<String> allowedGrants = null;
    protected Log log = LogFactory.getLog(OAuthAdminService.class);
    private AppInfoCache appInfoCache = AppInfoCache.getInstance();

    /**
     * Registers an consumer secret against the logged in user. A given user can only have a single
     * consumer secret at a time. Calling this method again and again will update the existing
     * consumer secret key.
     *
     * @return An array containing the consumer key and the consumer secret correspondingly.
     * @throws Exception Error when persisting the data in the persistence store.
     */
    public String[] registerOAuthConsumer() throws IdentityOAuthAdminException {

        String loggedInUser = CarbonContext.getThreadLocalCarbonContext().getUsername();

        if (log.isDebugEnabled()) {
            log.debug("Adding a consumer secret for the logged in user " + loggedInUser);
        }

        String tenantUser = MultitenantUtils.getTenantAwareUsername(loggedInUser);
        int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
        String userDomain = IdentityUtil.extractDomainFromName(loggedInUser);
        OAuthAppDAO dao = new OAuthAppDAO();
        return dao.addOAuthConsumer(UserCoreUtil.removeDomainFromName(tenantUser), tenantId, userDomain);
    }

    /**
     * Get all registered OAuth applications for the logged in user.
     *
     * @return An array of <code>OAuthConsumerAppDTO</code> objecting containing the application
     * information of the user
     * @throws Exception Error when reading the data from the persistence store.
     */
    public OAuthConsumerAppDTO[] getAllOAuthApplicationData() throws IdentityOAuthAdminException {

        String userName = CarbonContext.getThreadLocalCarbonContext().getUsername();
        OAuthConsumerAppDTO[] dtos = new OAuthConsumerAppDTO[0];

        if (userName == null) {
            if (log.isErrorEnabled()) {
                log.debug("User not logged in");
            }
            throw new IdentityOAuthAdminException("User not logged in");
        }

        String tenantUser = MultitenantUtils.getTenantAwareUsername(userName);
        int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
        OAuthAppDAO dao = new OAuthAppDAO();
        OAuthAppDO[] apps = dao.getOAuthConsumerAppsOfUser(tenantUser, tenantId);
        if (apps != null && apps.length > 0) {
            dtos = new OAuthConsumerAppDTO[apps.length];
            OAuthConsumerAppDTO dto = null;
            OAuthAppDO app = null;
            for (int i = 0; i < apps.length; i++) {
                app = apps[i];
                dto = new OAuthConsumerAppDTO();
                dto.setApplicationName(app.getApplicationName());
                dto.setCallbackUrl(app.getCallbackUrl());
                dto.setOauthConsumerKey(app.getOauthConsumerKey());
                dto.setOauthConsumerSecret(app.getOauthConsumerSecret());
                dto.setOAuthVersion(app.getOauthVersion());
                dto.setGrantTypes(app.getGrantTypes());
                dto.setUsername(app.getUser().toString());
                dtos[i] = dto;
            }
        }
        return dtos;
    }

    /**
     * Get OAuth application data by the consumer key.
     *
     * @param consumerKey Consumer Key
     * @return <code>OAuthConsumerAppDTO</code> with application information
     * @throws Exception Error when reading application information from persistence store.
     */
    public OAuthConsumerAppDTO getOAuthApplicationData(String consumerKey) throws IdentityOAuthAdminException {
        OAuthConsumerAppDTO dto = new OAuthConsumerAppDTO();
        OAuthAppDAO dao = new OAuthAppDAO();
        try {
            OAuthAppDO app = dao.getAppInformation(consumerKey);
            if (app != null) {
                dto.setApplicationName(app.getApplicationName());
                dto.setCallbackUrl(app.getCallbackUrl());
                dto.setOauthConsumerKey(app.getOauthConsumerKey());
                dto.setOauthConsumerSecret(app.getOauthConsumerSecret());
                dto.setOAuthVersion(app.getOauthVersion());
                dto.setGrantTypes(app.getGrantTypes());
            }
            return dto;
        } catch (InvalidOAuthClientException | IdentityOAuth2Exception e) {
            throw new IdentityOAuthAdminException("Error while retrieving the app information using consumer key", e);
        }

    }

    /**
     * Get OAuth application data by the application name.
     *
     * @param appName OAuth application name
     * @return <code>OAuthConsumerAppDTO</code> with application information
     * @throws Exception Error when reading application information from persistence store.
     */
    public OAuthConsumerAppDTO getOAuthApplicationDataByAppName(String appName) throws IdentityOAuthAdminException {
        OAuthConsumerAppDTO dto = new OAuthConsumerAppDTO();
        OAuthAppDAO dao = new OAuthAppDAO();
        try {
            OAuthAppDO app = dao.getAppInformationByAppName(appName);
            if (app != null) {
                dto.setApplicationName(app.getApplicationName());
                dto.setCallbackUrl(app.getCallbackUrl());
                dto.setOauthConsumerKey(app.getOauthConsumerKey());
                dto.setOauthConsumerSecret(app.getOauthConsumerSecret());
                dto.setOAuthVersion(app.getOauthVersion());
                dto.setGrantTypes(app.getGrantTypes());
            }
            return dto;
        }catch (InvalidOAuthClientException | IdentityOAuth2Exception e){
            throw new IdentityOAuthAdminException("Error while retrieving the app information by app name", e);
        }
    }

    /**
     * Registers an OAuth consumer application.
     *
     * @param application <code>OAuthConsumerAppDTO</code> with application information
     * @throws Exception Error when persisting the application information to the persistence store
     */
    public void registerOAuthApplicationData(OAuthConsumerAppDTO application) throws IdentityOAuthAdminException{
        String userName = CarbonContext.getThreadLocalCarbonContext().getUsername();
        if (userName != null) {
            String tenantUser = MultitenantUtils.getTenantAwareUsername(userName);
            int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
            String tenantDomain = CarbonContext.getThreadLocalCarbonContext().getTenantDomain();

            OAuthAppDAO dao = new OAuthAppDAO();
            OAuthAppDO app = new OAuthAppDO();
            if (application != null) {
                app.setApplicationName(application.getApplicationName());
                if ((application.getGrantTypes().contains(AUTHORIZATION_CODE) || application.getGrantTypes()
                        .contains(IMPLICIT)) && StringUtils.isEmpty(application.getCallbackUrl())) {
                    throw new IdentityOAuthAdminException("Callback Url is required for Code or Implicit grant types");
                }
                app.setCallbackUrl(application.getCallbackUrl());
                if (application.getOauthConsumerKey() == null) {
                    app.setOauthConsumerKey(OAuthUtil.getRandomNumber());
                    app.setOauthConsumerSecret(OAuthUtil.getRandomNumber());
                } else {
                    app.setOauthConsumerKey(application.getOauthConsumerKey());
                    app.setOauthConsumerSecret(application.getOauthConsumerSecret());
                }
                String applicationUser = application.getUsername();
                if (applicationUser != null && applicationUser.trim().length() > 0) {
                    try {
                        if (CarbonContext.getThreadLocalCarbonContext().getUserRealm().
                                getUserStoreManager().isExistingUser(application.getUsername())) {
                            tenantUser = applicationUser;
                        } else {
                            log.warn("OAuth application registrant user name " + applicationUser +
                                    " does not exist in the user store. Using logged-in user name " + tenantUser +
                                    " as registrant name");
                        }
                    } catch (UserStoreException e) {
                        throw new IdentityOAuthAdminException("Error while retrieving the user store manager", e);
                    }

                }
                AuthenticatedUser user = new AuthenticatedUser();
                user.setUserName(UserCoreUtil.removeDomainFromName(tenantUser));
                user.setTenantDomain(tenantDomain);
                user.setUserStoreDomain(IdentityUtil.extractDomainFromName(userName));
                app.setUser(user);
                if (application.getOAuthVersion() != null) {
                    app.setOauthVersion(application.getOAuthVersion());
                } else {   // by default, assume OAuth 2.0, if it is not set.
                    app.setOauthVersion(OAuthConstants.OAuthVersions.VERSION_2);
                }
                if (OAuthConstants.OAuthVersions.VERSION_2.equals(application.getOAuthVersion())) {
                    List<String> allowedGrants = new ArrayList<>(Arrays.asList(getAllowedGrantTypes()));
                    String[] requestGrants = application.getGrantTypes().split("\\s");
                    for (String requestedGrant : requestGrants) {
                        if (StringUtils.isBlank(requestedGrant)){
                            continue;
                        }
                        if (!allowedGrants.contains(requestedGrant)) {
                            throw new IdentityOAuthAdminException(requestedGrant + " not allowed");
                        }
                    }
                    app.setGrantTypes(application.getGrantTypes());
                }
                dao.addOAuthApplication(app);
                if (OAuthServerConfiguration.getInstance().isCacheEnabled()) {
                    appInfoCache.addToCache(app.getOauthConsumerKey(), app);
                }
            }
        }
    }

    /**
     * Update existing consumer application.
     *
     * @param consumerAppDTO <code>OAuthConsumerAppDTO</code> with updated application information
     * @throws IdentityOAuthAdminException Error when updating the underlying identity persistence store.
     */
    public void updateConsumerApplication(OAuthConsumerAppDTO consumerAppDTO) throws IdentityOAuthAdminException {
        String userName = CarbonContext.getThreadLocalCarbonContext().getUsername();
        String tenantAwareUsername = MultitenantUtils.getTenantAwareUsername(userName);
        int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
        String tenantDomain = CarbonContext.getThreadLocalCarbonContext().getTenantDomain();
        OAuthAppDAO dao = new OAuthAppDAO();
        OAuthAppDO oauthappdo = new OAuthAppDO();
        AuthenticatedUser user = new AuthenticatedUser();
        user.setUserName(UserCoreUtil.removeDomainFromName(tenantAwareUsername));
        user.setTenantDomain(tenantDomain);
        user.setUserStoreDomain(IdentityUtil.extractDomainFromName(userName));
        oauthappdo.setUser(user);
        oauthappdo.setOauthConsumerKey(consumerAppDTO.getOauthConsumerKey());
        oauthappdo.setOauthConsumerSecret(consumerAppDTO.getOauthConsumerSecret());
        oauthappdo.setCallbackUrl(consumerAppDTO.getCallbackUrl());
        oauthappdo.setApplicationName(consumerAppDTO.getApplicationName());
        if (OAuthConstants.OAuthVersions.VERSION_2.equals(consumerAppDTO.getOAuthVersion())) {
            List<String> allowedGrants = new ArrayList<>(Arrays.asList(getAllowedGrantTypes()));
            String[] requestGrants = consumerAppDTO.getGrantTypes().split("\\s");
            for (String requestedGrant : requestGrants) {
                if (StringUtils.isBlank(requestedGrant)) {
                    continue;
                }
                if (!allowedGrants.contains(requestedGrant)) {
                    throw new IdentityOAuthAdminException(requestedGrant + " not allowed");
                }
            }
            oauthappdo.setGrantTypes(consumerAppDTO.getGrantTypes());
        }
        dao.updateConsumerApplication(oauthappdo);
        if (OAuthServerConfiguration.getInstance().isCacheEnabled()) {
            appInfoCache.addToCache(oauthappdo.getOauthConsumerKey(), oauthappdo);
        }
    }

    /**
     * Removes an OAuth consumer application.
     *
     * @param consumerKey Consumer Key
     * @throws Exception Error when removing the consumer information from the database.
     */
    public void removeOAuthApplicationData(String consumerKey) throws IdentityOAuthAdminException {
        OAuthAppDAO dao = new OAuthAppDAO();
        dao.removeConsumerApplication(consumerKey);
        // remove client credentials from cache
        if (OAuthServerConfiguration.getInstance().isCacheEnabled()) {
            OAuthCache.getInstance().clearCacheEntry(new OAuthCacheKey(consumerKey));
            appInfoCache.clearCacheEntry(consumerKey);
            if (log.isDebugEnabled()) {
                log.debug("Client credentials are removed from the cache.");
            }
        }
    }

    /**
     * Get apps that are authorized by the given user
     *
     * @return OAuth applications authorized by the user that have tokens in ACTIVE or EXPIRED state
     */
    public OAuthConsumerAppDTO[] getAppsAuthorizedByUser() throws IdentityOAuthAdminException {

        TokenMgtDAO tokenMgtDAO = new TokenMgtDAO();
        OAuthAppDAO appDAO = new OAuthAppDAO();

        String tenantDomain = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantDomain();
        String tenantAwareUserName = PrivilegedCarbonContext.getThreadLocalCarbonContext().getUsername();
        AuthenticatedUser authenticatedUser = new AuthenticatedUser();
        authenticatedUser.setUserName(UserCoreUtil.removeDomainFromName(tenantAwareUserName));
        authenticatedUser.setUserStoreDomain(UserCoreUtil.extractDomainFromName(tenantAwareUserName));
        authenticatedUser.setTenantDomain(tenantDomain);
        String username = UserCoreUtil.addTenantDomainToEntry(tenantAwareUserName, tenantDomain);

        String userStoreDomain = null;
        if (OAuth2Util.checkAccessTokenPartitioningEnabled() && OAuth2Util.checkUserNameAssertionEnabled()) {
            try {
                userStoreDomain = OAuth2Util.getUserStoreDomainFromUserId(username);
            } catch (IdentityOAuth2Exception e) {
                String errorMsg = "Error occurred while getting user store domain for User ID : " + username;
                log.error(errorMsg, e);
                throw new IdentityOAuthAdminException(errorMsg, e);
            }
        }

        Set<String> clientIds = null;
        try {
            clientIds = tokenMgtDAO.getAllTimeAuthorizedClientIds(authenticatedUser);
        } catch (IdentityOAuth2Exception e) {
            String errorMsg = "Error occurred while retrieving apps authorized by User ID : " + username;
            log.error(errorMsg, e);
            throw new IdentityOAuthAdminException(errorMsg, e);
        }
        Set<OAuthConsumerAppDTO> appDTOs = new HashSet<OAuthConsumerAppDTO>();
        for (String clientId : clientIds) {
            Set<AccessTokenDO> accessTokenDOs = null;
            try {
                accessTokenDOs = tokenMgtDAO.retrieveAccessTokens(clientId, authenticatedUser, userStoreDomain, true);
            } catch (IdentityOAuth2Exception e) {
                String errorMsg = "Error occurred while retrieving access tokens issued for " +
                        "Client ID : " + clientId + ", User ID : " + username;
                log.error(errorMsg, e);
                throw new IdentityOAuthAdminException(errorMsg, e);
            }
            if (!accessTokenDOs.isEmpty()) {
                Set<String> distinctClientUserScopeCombo = new HashSet<String>();
                for (AccessTokenDO accessTokenDO : accessTokenDOs) {
                    AccessTokenDO scopedToken = null;
                    String scopeString = OAuth2Util.buildScopeString(accessTokenDO.getScope());
                    try {
                        scopedToken = tokenMgtDAO.retrieveLatestAccessToken(
                                clientId, authenticatedUser, userStoreDomain, scopeString, true);
                        if(scopedToken != null && !distinctClientUserScopeCombo.contains(clientId+":"+username)){
                            OAuthConsumerAppDTO appDTO = new OAuthConsumerAppDTO();
                            OAuthAppDO appDO;
                            try {
                                appDO = appDAO.getAppInformation(scopedToken.getConsumerKey());
                                appDTO.setOauthConsumerKey(scopedToken.getConsumerKey());
                                appDTO.setApplicationName(appDO.getApplicationName());
                                appDTO.setUsername(appDO.getUser().toString());
                                appDTO.setGrantTypes(appDO.getGrantTypes());
                                appDTOs.add(appDTO);
                            } catch (InvalidOAuthClientException e) {
                                String errorMsg = "Invalid Client ID : " + scopedToken.getConsumerKey();
                                log.error(errorMsg, e);
                                throw new IdentityOAuthAdminException(errorMsg);
                            } catch (IdentityOAuth2Exception e) {
                                String errorMsg = "Error occurred while retrieving app information " +
                                        "for Client ID : " + scopedToken.getConsumerKey();
                                log.error(errorMsg, e);
                                throw new IdentityOAuthAdminException(errorMsg);
                            }
                            distinctClientUserScopeCombo.add(clientId+":"+username);

                        }
                    } catch (IdentityOAuth2Exception e) {
                        String errorMsg = "Error occurred while retrieving latest access token issued for Client ID :" +
                                " " + clientId + ", User ID : " + username + " and Scope : " + scopeString;
                        log.error(errorMsg, e);
                        throw new IdentityOAuthAdminException(errorMsg, e);
                    }
                }
            }
        }
        return appDTOs.toArray(new OAuthConsumerAppDTO[appDTOs.size()]);
    }

    /**
     * Revoke authorization for OAuth apps by resource owners
     *
     * @param revokeRequestDTO DTO representing authorized user and apps[]
     * @return revokeRespDTO DTO representing success or failure message
     */
    public OAuthRevocationResponseDTO revokeAuthzForAppsByResoureOwner(
            OAuthRevocationRequestDTO revokeRequestDTO) throws IdentityOAuthAdminException {

        TokenMgtDAO tokenMgtDAO = new TokenMgtDAO();
        if (revokeRequestDTO.getApps() != null && revokeRequestDTO.getApps().length > 0) {
            String tenantDomain = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantDomain();
            String tenantAwareUserName = PrivilegedCarbonContext.getThreadLocalCarbonContext().getUsername();
            AuthenticatedUser user = new AuthenticatedUser();
            user.setUserName(UserCoreUtil.removeDomainFromName(tenantAwareUserName));
            user.setUserStoreDomain(UserCoreUtil.extractDomainFromName(tenantAwareUserName));
            user.setTenantDomain(tenantDomain);
            String userName = UserCoreUtil.addTenantDomainToEntry(tenantAwareUserName, tenantDomain);

            String userStoreDomain = null;
            if (OAuth2Util.checkAccessTokenPartitioningEnabled() &&
                    OAuth2Util.checkUserNameAssertionEnabled()) {
                try {
                    userStoreDomain = OAuth2Util.getUserStoreDomainFromUserId(userName);
                } catch (IdentityOAuth2Exception e) {
                    throw new IdentityOAuthAdminException(
                            "Error occurred while getting user store domain from User ID : " + userName, e);
                }
            }
            OAuthConsumerAppDTO[] appDTOs = getAppsAuthorizedByUser();
            for (String appName : revokeRequestDTO.getApps()) {
                for (OAuthConsumerAppDTO appDTO : appDTOs) {
                    if (appDTO.getApplicationName().equals(appName)) {
                        Set<AccessTokenDO> accessTokenDOs = null;
                        try {
                            // retrieve all ACTIVE or EXPIRED access tokens for particular client authorized by this user
                            accessTokenDOs = tokenMgtDAO.retrieveAccessTokens(
                                    appDTO.getOauthConsumerKey(), user, userStoreDomain, true);
                        } catch (IdentityOAuth2Exception e) {
                            String errorMsg = "Error occurred while retrieving access tokens issued for " +
                                    "Client ID : " + appDTO.getOauthConsumerKey() + ", User ID : " + userName;
                            log.error(errorMsg, e);
                            throw new IdentityOAuthAdminException(errorMsg, e);
                        }
                        User authzUser;
                        for (AccessTokenDO accessTokenDO : accessTokenDOs) {
                            //Clear cache with AccessTokenDO
                            authzUser = accessTokenDO.getAuthzUser();

                            OAuthUtil.clearOAuthCache(accessTokenDO.getConsumerKey(), authzUser,
                                    OAuth2Util.buildScopeString(accessTokenDO.getScope()));
                            OAuthUtil.clearOAuthCache(accessTokenDO.getConsumerKey(), authzUser);
                            OAuthUtil.clearOAuthCache(accessTokenDO.getAccessToken());
                            AccessTokenDO scopedToken = null;
                            try {
                                // retrieve latest access token for particular client, user and scope combination if its ACTIVE or EXPIRED
                                scopedToken = tokenMgtDAO.retrieveLatestAccessToken(
                                        appDTO.getOauthConsumerKey(), user, userStoreDomain,
                                        OAuth2Util.buildScopeString(accessTokenDO.getScope()), true);
                            } catch (IdentityOAuth2Exception e) {
                                String errorMsg = "Error occurred while retrieving latest " +
                                        "access token issued for Client ID : " +
                                        appDTO.getOauthConsumerKey() + ", User ID : " + userName +
                                        " and Scope : " + OAuth2Util.buildScopeString(accessTokenDO.getScope());
                                log.error(errorMsg, e);
                                throw new IdentityOAuthAdminException(errorMsg, e);
                            }
                            if (scopedToken != null) {
                                //Revoking token from database
                                try {
                                    tokenMgtDAO.revokeTokens(new String[]{scopedToken.getAccessToken()});
                                } catch (IdentityOAuth2Exception e) {
                                    String errorMsg = "Error occurred while revoking " + "Access Token : " +
                                            scopedToken.getAccessToken();
                                    log.error(errorMsg, e);
                                    throw new IdentityOAuthAdminException(errorMsg, e);
                                }
                            }
                        }

                        try {
                            tokenMgtDAO.revokeOAuthConsentByApplicationAndUser(userName, appName);
                        } catch (IdentityOAuth2Exception e) {
                            String errorMsg = "Error occurred while removing OAuth Consent of Application " + appName +
                                              " of user " + userName;
                            log.error(errorMsg, e);
                            throw new IdentityOAuthAdminException(errorMsg, e);
                        }
                    }
                }
            }
        } else {
            OAuthRevocationResponseDTO revokeRespDTO = new OAuthRevocationResponseDTO();
            revokeRespDTO.setError(true);
            revokeRespDTO.setErrorCode(OAuth2ErrorCodes.INVALID_REQUEST);
            revokeRespDTO.setErrorMsg("Invalid revocation request");
            return revokeRespDTO;
        }
        return new OAuthRevocationResponseDTO();
    }

    public String[] getAllowedGrantTypes() {
        if (allowedGrants == null) {
            synchronized (OAuthAdminService.class) {
                if (allowedGrants == null) {
                    Set<String> allowedGrantSet =
                            OAuthServerConfiguration.getInstance().getSupportedGrantTypes().keySet();
                    Set<String> modifiableGrantSet = new HashSet(allowedGrantSet);
                    if (OAuthServerConfiguration.getInstance().getSupportedResponseTypes().containsKey("token")) {
                        modifiableGrantSet.add(IMPLICIT);
                    }
                    allowedGrants = new ArrayList<>(modifiableGrantSet);
                }
            }
        }
        return allowedGrants.toArray(new String[allowedGrants.size()]);
    }
}
