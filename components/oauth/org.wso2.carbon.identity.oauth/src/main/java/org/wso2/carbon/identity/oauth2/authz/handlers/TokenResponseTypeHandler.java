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

package org.wso2.carbon.identity.oauth2.authz.handlers;

import org.apache.axiom.util.base64.Base64Utils;
import org.apache.commons.io.Charsets;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.oltu.oauth2.common.exception.OAuthSystemException;
import org.apache.oltu.oauth2.common.message.types.ResponseType;
import org.wso2.carbon.identity.base.IdentityException;
import org.wso2.carbon.identity.core.util.IdentityUtil;
import org.wso2.carbon.identity.oauth.cache.OAuthCacheKey;
import org.wso2.carbon.identity.oauth.common.OAuthConstants;
import org.wso2.carbon.identity.oauth.config.OAuthServerConfiguration;
import org.wso2.carbon.identity.oauth2.IdentityOAuth2Exception;
import org.wso2.carbon.identity.oauth2.authz.OAuthAuthzReqMessageContext;
import org.wso2.carbon.identity.oauth2.dto.OAuth2AuthorizeReqDTO;
import org.wso2.carbon.identity.oauth2.dto.OAuth2AuthorizeRespDTO;
import org.wso2.carbon.identity.oauth2.model.AccessTokenDO;
import org.wso2.carbon.identity.oauth2.util.OAuth2Util;
import org.wso2.carbon.identity.openidconnect.IDTokenBuilder;

import java.sql.Timestamp;
import java.util.Date;
import java.util.UUID;

public class TokenResponseTypeHandler extends AbstractResponseTypeHandler {

    private static Log log = LogFactory.getLog(TokenResponseTypeHandler.class);

    @Override
    public OAuth2AuthorizeRespDTO issue(OAuthAuthzReqMessageContext oauthAuthzMsgCtx)
            throws IdentityOAuth2Exception {

        OAuth2AuthorizeRespDTO respDTO = new OAuth2AuthorizeRespDTO();
        OAuth2AuthorizeReqDTO authorizationReqDTO = oauthAuthzMsgCtx.getAuthorizationReqDTO();

        String scope = OAuth2Util.buildScopeString(oauthAuthzMsgCtx.getApprovedScope());

        respDTO.setCallbackURI(authorizationReqDTO.getCallbackUrl());

        String consumerKey = authorizationReqDTO.getConsumerKey();
        String authorizedUser = authorizationReqDTO.getUser().toString();
        String oAuthCacheKeyString;

        String responseType = oauthAuthzMsgCtx.getAuthorizationReqDTO().getResponseType();
        String grantType;

        if (StringUtils.contains(responseType, OAuthConstants.GrantTypes.TOKEN)) {
            grantType = OAuthConstants.GrantTypes.IMPLICIT;
        } else {
            grantType = responseType;
        }

        boolean isUsernameCaseSensitive = IdentityUtil.isUserStoreInUsernameCaseSensitive(authorizedUser);

        if (isUsernameCaseSensitive) {
            oAuthCacheKeyString = consumerKey + ":" + authorizedUser + ":" + scope;
        } else {
            oAuthCacheKeyString = consumerKey + ":" + authorizedUser.toLowerCase() + ":" + scope;
        }

        OAuthCacheKey cacheKey = new OAuthCacheKey(oAuthCacheKeyString);
        String userStoreDomain = null;

        // Select the user store domain when multiple user stores are configured.
        if (OAuth2Util.checkAccessTokenPartitioningEnabled() &&
                OAuth2Util.checkUserNameAssertionEnabled()) {
            userStoreDomain = OAuth2Util.getUserStoreDomainFromUserId(authorizedUser);
        }

        String refreshToken = null;
        Timestamp refreshTokenIssuedTime = null;
        long refreshTokenValidityPeriodInMillis = 0;

        synchronized ((consumerKey + ":" + authorizedUser + ":" + scope).intern()) {

            // check if valid access token exists in cache
            if (cacheEnabled) {
                AccessTokenDO accessTokenDO = (AccessTokenDO) oauthCache.getValueFromCache(cacheKey);
                if (accessTokenDO != null) {
                    if (log.isDebugEnabled()) {
                        log.debug("Retrieved active Access Token" +
                                " for Client Id : " + consumerKey + ", User ID :" + authorizedUser +
                                " and Scope : " + scope + " from cache");
                    }

                    long expireTime = OAuth2Util.getTokenExpireTimeMillis(accessTokenDO);

                    if (expireTime > 0 || expireTime < 0) {
                        if (log.isDebugEnabled()) {
                            if(expireTime > 0) {
                                log.debug("Access Token"+
                                                " is valid for another " + expireTime + "ms");
                            } else {
                                log.debug("Infinite lifetime Access Token found in cache");
                            }
                        }
                        respDTO.setAccessToken(accessTokenDO.getAccessToken());

                        if (expireTime > 0) {
                            respDTO.setValidityPeriod(expireTime/1000);
                        } else {
                            respDTO.setValidityPeriod(Long.MAX_VALUE/1000);
                        }
                        respDTO.setScope(oauthAuthzMsgCtx.getApprovedScope());
                        respDTO.setTokenType(accessTokenDO.getTokenType());
                        buildIdToken(oauthAuthzMsgCtx, respDTO);
                        return respDTO;
                    } else {

                        long refreshTokenExpiryTime = OAuth2Util.getRefreshTokenExpireTimeMillis(accessTokenDO);

                        if (refreshTokenExpiryTime < 0 || refreshTokenExpiryTime > 0) {

                            if (log.isDebugEnabled()) {
                                log.debug("Access token has expired, But refresh token is still valid. User existing " +
                                        "refresh token.");
                            }
                            refreshToken = accessTokenDO.getRefreshToken();
                            refreshTokenIssuedTime = accessTokenDO.getRefreshTokenIssuedTime();
                            refreshTokenValidityPeriodInMillis = accessTokenDO.getRefreshTokenValidityPeriodInMillis();
                        }

                        // Token is expired. Clear it from cache
                        oauthCache.clearCacheEntry(cacheKey);

                        if (log.isDebugEnabled()) {
                            log.debug("Access Token is expired. Therefore cleared it from cache and marked it" +
                                    " as expired in database");
                        }
                    }
                } else {
                    if (log.isDebugEnabled()) {
                        log.debug("No active access token found in cache for Client ID : " + consumerKey +
                                ", User ID : " + authorizedUser + " and Scope : " + scope);
                    }
                }
            }

            // check if the last issued access token is still active and valid in the database
            AccessTokenDO existingAccessTokenDO = tokenMgtDAO.retrieveLatestAccessToken(
                    consumerKey, authorizationReqDTO.getUser(), userStoreDomain, scope, false);

            if (existingAccessTokenDO != null) {

                if (log.isDebugEnabled()) {
                    log.debug("Retrieved latest Access Token" +
                            " for Client ID : " + consumerKey + ", User ID :" + authorizedUser +
                            " and Scope : " + scope + " from database");
                }

                long expiryTime = OAuth2Util.getTokenExpireTimeMillis(existingAccessTokenDO);

                long refreshTokenExpiryTime = OAuth2Util.getRefreshTokenExpireTimeMillis(existingAccessTokenDO);

                if (OAuthConstants.TokenStates.TOKEN_STATE_ACTIVE.equals(
                        existingAccessTokenDO.getTokenState()) && (expiryTime > 0 || expiryTime < 0)) {

                    // token is active and valid
                    if (log.isDebugEnabled()) {
                        if(expiryTime > 0) {
                            log.debug("Access token is valid for another " + expiryTime + "ms");
                        } else {
                            log.debug("Infinite lifetime Access Token found in cache");
                        }
                    }

                    if (cacheEnabled) {
                        oauthCache.addToCache(cacheKey, existingAccessTokenDO);
                        if (log.isDebugEnabled()) {
                            log.debug("Access Token was added to cache for cache key : "
                                    + cacheKey.getCacheKeyString());
                        }
                    }

                    respDTO.setAccessToken(existingAccessTokenDO.getAccessToken());

                    if(expiryTime > 0) {
                        respDTO.setValidityPeriod(expiryTime / 1000);
                    } else {
                        respDTO.setValidityPeriod(Long.MAX_VALUE / 1000);
                    }

                    respDTO.setScope(oauthAuthzMsgCtx.getApprovedScope());
                    respDTO.setTokenType(existingAccessTokenDO.getTokenType());

                    buildIdToken(oauthAuthzMsgCtx, respDTO);

                    return respDTO;

                } else {

                    if (log.isDebugEnabled()) {
                        log.debug("Access Token is " + existingAccessTokenDO.getTokenState());
                    }
                    String tokenState = existingAccessTokenDO.getTokenState();

                    if (OAuthConstants.TokenStates.TOKEN_STATE_ACTIVE.equals(tokenState)) {

                        // Token is expired. If refresh token is still valid, use it.
                        if (refreshTokenExpiryTime > 0 || refreshTokenExpiryTime < 0) {
                            if (log.isDebugEnabled()) {
                                log.debug("Access token has expired, But refresh token is still valid. User existing " +
                                        "refresh token.");
                            }
                            refreshToken = existingAccessTokenDO.getRefreshToken();
                            refreshTokenIssuedTime = existingAccessTokenDO.getRefreshTokenIssuedTime();
                            refreshTokenValidityPeriodInMillis = existingAccessTokenDO.getRefreshTokenValidityPeriodInMillis();
                        }

                        if (log.isDebugEnabled()) {
                            log.debug("Marked Access Token as expired");
                        }
                    } else {

                        //Token is revoked or inactive
                        if (log.isDebugEnabled()) {
                            log.debug("Access Token is " + existingAccessTokenDO.getTokenState());
                        }
                    }
                }
            } else {
                if (log.isDebugEnabled()) {
                    log.debug("No access token found in database for Client ID : " + consumerKey +
                            ", User ID : " + authorizedUser + " and Scope : " + scope +
                            ". Therefore issuing new access token");
                }
            }

            Timestamp timestamp = new Timestamp(new Date().getTime());

            // if reusing existing refresh token, use its original issued time
            if(refreshTokenIssuedTime == null) {
                refreshTokenIssuedTime = timestamp;
            }
            // Default token validity Period
            long validityPeriodInMillis = OAuthServerConfiguration.getInstance().
                    getUserAccessTokenValidityPeriodInSeconds() * 1000;

            // if a VALID validity period is set through the callback, then use it
            long callbackValidityPeriod = oauthAuthzMsgCtx.getValidityPeriod();
            if ((callbackValidityPeriod != OAuthConstants.UNASSIGNED_VALIDITY_PERIOD)
                    && callbackValidityPeriod > 0) {
                validityPeriodInMillis = callbackValidityPeriod * 1000;
            }

            // If issuing new refresh token, use default refresh token validity Period
            // otherwise use existing refresh token's validity period
            if(refreshTokenValidityPeriodInMillis == 0) {
                refreshTokenValidityPeriodInMillis = OAuthServerConfiguration.getInstance()
                        .getRefreshTokenValidityPeriodInSeconds() * 1000;
            }
            
            // issue a new access token
            String accessToken;
            
            // set the validity period. this is needed by downstream handlers.
            // if this is set before - then this will override it by the calculated new value.
            oauthAuthzMsgCtx.setValidityPeriod(validityPeriodInMillis);
            
            // set the refresh token validity period. this is needed by downstream handlers.
            // if this is set before - then this will override it by the calculated new value.
            oauthAuthzMsgCtx.setRefreshTokenvalidityPeriod(refreshTokenValidityPeriodInMillis);
            
            // set access token issued time.this is needed by downstream handlers.
            oauthAuthzMsgCtx.setAccessTokenIssuedTime(timestamp.getTime());
            
            // set refresh token issued time.this is needed by downstream handlers.
            oauthAuthzMsgCtx.setRefreshTokenIssuedTime(refreshTokenIssuedTime.getTime());

	    try {
		accessToken = oauthIssuerImpl.accessToken();

		// regenerate only if refresh token is null
		if (refreshToken == null) {
		    refreshToken = oauthIssuerImpl.refreshToken();
		}

	    } catch (OAuthSystemException e) {
		throw new IdentityOAuth2Exception("Error occurred while generating access token and refresh token", e);
	    } 

            if (OAuth2Util.checkUserNameAssertionEnabled()) {
                String userName = oauthAuthzMsgCtx.getAuthorizationReqDTO().getUser().toString();
                //use ':' for token & userStoreDomain separation
                String accessTokenStrToEncode = accessToken + ":" + userName;
                accessToken = Base64Utils.encode(accessTokenStrToEncode.getBytes(Charsets.UTF_8));

                String refreshTokenStrToEncode = refreshToken + ":" + userName;
                refreshToken = Base64Utils.encode(refreshTokenStrToEncode.getBytes(Charsets.UTF_8));
            }

            AccessTokenDO newAccessTokenDO = new AccessTokenDO(consumerKey, authorizationReqDTO.getUser(), oauthAuthzMsgCtx.getApprovedScope(), timestamp,
                    refreshTokenIssuedTime, validityPeriodInMillis, refreshTokenValidityPeriodInMillis,
                    OAuthConstants.UserType.APPLICATION_USER);

            newAccessTokenDO.setAccessToken(accessToken);
            newAccessTokenDO.setRefreshToken(refreshToken);
            newAccessTokenDO.setTokenState(OAuthConstants.TokenStates.TOKEN_STATE_ACTIVE);
            newAccessTokenDO.setTokenId(UUID.randomUUID().toString());
            newAccessTokenDO.setGrantType(grantType);

            // Persist the access token in database
            try {
                tokenMgtDAO.storeAccessToken(accessToken, authorizationReqDTO.getConsumerKey(),
                                             newAccessTokenDO, existingAccessTokenDO, userStoreDomain);
            } catch (IdentityException e) {
                throw new IdentityOAuth2Exception(
                                "Error occurred while storing new access token : " + accessToken, e);
            }

            if (log.isDebugEnabled()) {
                log.debug("Persisted Access Token for " +
                          "Client ID : " + authorizationReqDTO.getConsumerKey() +
                          ", Authorized User : " + authorizationReqDTO.getUser() +
                          ", Timestamp : " + timestamp +
                          ", Validity period (s) : " + newAccessTokenDO.getValidityPeriod() +
                          ", Scope : " + OAuth2Util.buildScopeString(oauthAuthzMsgCtx.getApprovedScope()) +
                          ", Callback URL : " + authorizationReqDTO.getCallbackUrl() +
                          ", Token State : " + OAuthConstants.TokenStates.TOKEN_STATE_ACTIVE +
                          " and User Type : " + OAuthConstants.UserType.APPLICATION_USER);
            }

            // Add the access token to the cache.
            if (cacheEnabled) {
                oauthCache.addToCache(cacheKey, newAccessTokenDO);
                if (log.isDebugEnabled()) {
                    log.debug("Access Token was added to OAuthCache for " +
                            "cache key : " + cacheKey.getCacheKeyString());
                }
            }

            if(StringUtils.contains(responseType, ResponseType.TOKEN.toString())) {
                respDTO.setAccessToken(accessToken);

                if (validityPeriodInMillis > 0) {
                    respDTO.setValidityPeriod(newAccessTokenDO.getValidityPeriod());
                } else {
                    respDTO.setValidityPeriod(Long.MAX_VALUE / 1000);
                }

                respDTO.setScope(newAccessTokenDO.getScope());
                respDTO.setTokenType(newAccessTokenDO.getTokenType());
            }
        }

        buildIdToken(oauthAuthzMsgCtx, respDTO);
        return respDTO;
    }

    private void buildIdToken(OAuthAuthzReqMessageContext msgCtx, OAuth2AuthorizeRespDTO authzRespDTO)
            throws IdentityOAuth2Exception{

        if (StringUtils.contains(msgCtx.getAuthorizationReqDTO().getResponseType(), "id_token") &&
                msgCtx.getApprovedScope() != null && OAuth2Util.isOIDCAuthzRequest(msgCtx.getApprovedScope())) {
            IDTokenBuilder builder = OAuthServerConfiguration.getInstance().getOpenIDConnectIDTokenBuilder();
            authzRespDTO.setIdToken(builder.buildIDToken(msgCtx, authzRespDTO));
        }
    }
}
