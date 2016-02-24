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

import org.apache.axiom.util.base64.Base64Utils;
import org.apache.commons.io.Charsets;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.oltu.oauth2.common.error.OAuthError;
import org.apache.oltu.oauth2.common.exception.OAuthSystemException;
import org.wso2.carbon.identity.core.util.IdentityUtil;
import org.wso2.carbon.identity.oauth.cache.CacheKey;
import org.wso2.carbon.identity.oauth.cache.OAuthCacheKey;
import org.wso2.carbon.identity.oauth.common.OAuthConstants;
import org.wso2.carbon.identity.oauth.config.OAuthServerConfiguration;
import org.wso2.carbon.identity.oauth2.IdentityOAuth2Exception;
import org.wso2.carbon.identity.oauth2.ResponseHeader;
import org.wso2.carbon.identity.oauth2.dto.OAuth2AccessTokenReqDTO;
import org.wso2.carbon.identity.oauth2.dto.OAuth2AccessTokenRespDTO;
import org.wso2.carbon.identity.oauth2.model.AccessTokenDO;
import org.wso2.carbon.identity.oauth2.model.RefreshTokenValidationDataDO;
import org.wso2.carbon.identity.oauth2.token.OAuthTokenReqMessageContext;
import org.wso2.carbon.identity.oauth2.util.OAuth2Util;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.UUID;

/**
 * Grant Type handler for Grant Type refresh_token which is used to get a new access token.
 */
public class RefreshGrantHandler extends AbstractAuthorizationGrantHandler {

    private static final String PREV_ACCESS_TOKEN = "previousAccessToken";
    private static Log log = LogFactory.getLog(RefreshGrantHandler.class);

    @Override
    public boolean validateGrant(OAuthTokenReqMessageContext tokReqMsgCtx)
            throws IdentityOAuth2Exception {

        if(!super.validateGrant(tokReqMsgCtx)){
            return false;
        }

        OAuth2AccessTokenReqDTO tokenReqDTO = tokReqMsgCtx.getOauth2AccessTokenReqDTO();

        String refreshToken = tokenReqDTO.getRefreshToken();

        RefreshTokenValidationDataDO validationDataDO = tokenMgtDAO.validateRefreshToken(
                tokenReqDTO.getClientId(), refreshToken);

        if (validationDataDO.getAccessToken() == null) {
            log.debug("Invalid Refresh Token provided for Client with " +
                    "Client Id : " + tokenReqDTO.getClientId());
            return false;
        }

        if (validationDataDO.getRefreshTokenState() != null &&
                !OAuthConstants.TokenStates.TOKEN_STATE_ACTIVE.equals(
                        validationDataDO.getRefreshTokenState()) &&
                !OAuthConstants.TokenStates.TOKEN_STATE_EXPIRED.equals(
                        validationDataDO.getRefreshTokenState())) {
            if(log.isDebugEnabled()) {
                log.debug("Access Token is not in 'ACTIVE' or 'EXPIRED' state for Client with " +
                        "Client Id : " + tokenReqDTO.getClientId());
            }
            return false;
        }

        String userStoreDomain = null;
        if (OAuth2Util.checkAccessTokenPartitioningEnabled() && OAuth2Util.checkUserNameAssertionEnabled()) {
            try {
                userStoreDomain = OAuth2Util.getUserStoreDomainFromUserId(validationDataDO.getAuthorizedUser().toString());
            } catch (IdentityOAuth2Exception e) {
                String errorMsg = "Error occurred while getting user store domain for User ID : " + validationDataDO.getAuthorizedUser();
                log.error(errorMsg, e);
                throw new IdentityOAuth2Exception(errorMsg, e);
            }
        }

        AccessTokenDO accessTokenDO = tokenMgtDAO.retrieveLatestAccessToken(tokenReqDTO.getClientId(),
                validationDataDO.getAuthorizedUser(),
                userStoreDomain, OAuth2Util.buildScopeString(validationDataDO.getScope()), true);

        if (accessTokenDO == null){
            if(log.isDebugEnabled()){
                log.debug("Error while retrieving the latest refresh token");
            }
            return false;
        }else if(!refreshToken.equals(accessTokenDO.getRefreshToken())){
            if(log.isDebugEnabled()){
                log.debug("Refresh token is not the latest.");
            }
            return false;
        }

        if (log.isDebugEnabled()) {
            log.debug("Refresh token validation successful for " +
                    "Client id : " + tokenReqDTO.getClientId() +
                    ", Authorized User : " + validationDataDO.getAuthorizedUser() +
                    ", Token Scope : " + OAuth2Util.buildScopeString(validationDataDO.getScope()));
        }

        tokReqMsgCtx.setAuthorizedUser(validationDataDO.getAuthorizedUser());
        tokReqMsgCtx.setScope(validationDataDO.getScope());
        // Store the old access token as a OAuthTokenReqMessageContext property, this is already
        // a preprocessed token.
        tokReqMsgCtx.addProperty(PREV_ACCESS_TOKEN, validationDataDO);
        return true;
    }

    @Override
    public OAuth2AccessTokenRespDTO issue(OAuthTokenReqMessageContext tokReqMsgCtx)
            throws IdentityOAuth2Exception {

        OAuth2AccessTokenRespDTO tokenRespDTO = new OAuth2AccessTokenRespDTO();
        OAuth2AccessTokenReqDTO oauth2AccessTokenReqDTO = tokReqMsgCtx.getOauth2AccessTokenReqDTO();
        String scope = OAuth2Util.buildScopeString(tokReqMsgCtx.getScope());

        String tokenId;
        String accessToken;
        String refreshToken;
        String userStoreDomain = null;
        String grantType;

        Timestamp refreshTokenIssuedTime = null;
        long refreshTokenValidityPeriodInMillis = 0;

        tokenId = UUID.randomUUID().toString();
        grantType = oauth2AccessTokenReqDTO.getGrantType();
        try {
            accessToken = oauthIssuerImpl.accessToken();
            refreshToken = oauthIssuerImpl.refreshToken();
        } catch (OAuthSystemException e) {
            throw new IdentityOAuth2Exception("Error when generating the tokens.", e);
        }

        boolean renew = OAuthServerConfiguration.getInstance().isRefreshTokenRenewalEnabled();

        // an active or expired token will be returned. since we do the validation for active or expired token in
        // validateGrant() no need to do it here again
        RefreshTokenValidationDataDO refreshTokenValidationDataDO = tokenMgtDAO
                .validateRefreshToken(oauth2AccessTokenReqDTO.getClientId(), oauth2AccessTokenReqDTO.getRefreshToken());

        long issuedTime = refreshTokenValidationDataDO.getIssuedTime().getTime();
        long refreshValidity = refreshTokenValidationDataDO.getValidityPeriodInMillis();
        long skew = OAuthServerConfiguration.getInstance().getTimeStampSkewInSeconds() * 1000;

        if (issuedTime + refreshValidity - (System.currentTimeMillis() + skew) > 1000) {
            if (!renew) {
                // if refresh token renewal not enabled, we use existing one else we issue a new refresh token
                refreshToken = oauth2AccessTokenReqDTO.getRefreshToken();
                refreshTokenIssuedTime = refreshTokenValidationDataDO.getIssuedTime();
                refreshTokenValidityPeriodInMillis = refreshTokenValidationDataDO.getValidityPeriodInMillis();
            }
        } else {
            // todo add proper error message/error code
            return handleError(OAuthError.TokenResponse.INVALID_REQUEST, "Refresh token is expired.");
        }

        Timestamp timestamp = new Timestamp(new Date().getTime());

        // if reusing existing refresh token, use its original issued time
        if (refreshTokenIssuedTime == null) {
            refreshTokenIssuedTime = timestamp;
        }

        // Default Validity Period (in seconds)
        long validityPeriodInMillis = OAuthServerConfiguration.getInstance()
                .getUserAccessTokenValidityPeriodInSeconds() * 1000;

        // if a VALID validity period is set through the callback, then use it
        long callbackValidityPeriod = tokReqMsgCtx.getValidityPeriod();
        if (callbackValidityPeriod != OAuthConstants.UNASSIGNED_VALIDITY_PERIOD) {
            validityPeriodInMillis = callbackValidityPeriod * 1000;
        }

        // If issuing new refresh token, use default refresh token validity Period
        // otherwise use existing refresh token's validity period
        if (refreshTokenValidityPeriodInMillis == 0) {
            refreshTokenValidityPeriodInMillis = OAuthServerConfiguration.getInstance()
                                                         .getRefreshTokenValidityPeriodInSeconds() * 1000;
        }

        String tokenType;
        if (isOfTypeApplicationUser()) {
            tokenType = OAuthConstants.UserType.APPLICATION_USER;
        } else {
            tokenType = OAuthConstants.UserType.APPLICATION;
        }

        String clientId = oauth2AccessTokenReqDTO.getClientId();

        // set the validity period. this is needed by downstream handlers.
        // if this is set before - then this will override it by the calculated new value.
        tokReqMsgCtx.setValidityPeriod(validityPeriodInMillis);

        // set the refresh token validity period. this is needed by downstream handlers.
        // if this is set before - then this will override it by the calculated new value.
        tokReqMsgCtx.setRefreshTokenvalidityPeriod(refreshTokenValidityPeriodInMillis);

        // set access token issued time.this is needed by downstream handlers.
        tokReqMsgCtx.setAccessTokenIssuedTime(timestamp.getTime());

        // set refresh token issued time.this is needed by downstream handlers.
        tokReqMsgCtx.setRefreshTokenIssuedTime(refreshTokenIssuedTime.getTime());

        if (OAuth2Util.checkUserNameAssertionEnabled()) {
            String userName = tokReqMsgCtx.getAuthorizedUser().toString();
            // use ':' for token & userStoreDomain separation
            String accessTokenStrToEncode = accessToken + ":" + userName;
            accessToken = Base64Utils.encode(accessTokenStrToEncode.getBytes(Charsets.UTF_8));

            String refreshTokenStrToEncode = refreshToken + ":" + userName;
            refreshToken = Base64Utils.encode(refreshTokenStrToEncode.getBytes(Charsets.UTF_8));

            // logic to store access token into different tables when multiple user stores are configured.
            if (OAuth2Util.checkAccessTokenPartitioningEnabled()) {
                userStoreDomain = OAuth2Util.getUserStoreDomainFromUserId(userName);
            }
        }

        AccessTokenDO accessTokenDO = new AccessTokenDO(clientId, tokReqMsgCtx.getAuthorizedUser(),
                tokReqMsgCtx.getScope(), timestamp, refreshTokenIssuedTime, validityPeriodInMillis,
                refreshTokenValidityPeriodInMillis, tokenType);

        accessTokenDO.setTokenState(OAuthConstants.TokenStates.TOKEN_STATE_ACTIVE);
        accessTokenDO.setRefreshToken(refreshToken);
        accessTokenDO.setTokenId(tokenId);
        accessTokenDO.setAccessToken(accessToken);
        accessTokenDO.setGrantType(grantType);

        RefreshTokenValidationDataDO oldAccessToken =
                (RefreshTokenValidationDataDO) tokReqMsgCtx.getProperty(PREV_ACCESS_TOKEN);

        String authorizedUser = tokReqMsgCtx.getAuthorizedUser().toString();
	    // set the previous access token state to "INACTIVE" and store new access token in single db connection
	    tokenMgtDAO.invalidateAndCreateNewToken(oldAccessToken.getTokenId(), "INACTIVE", clientId,
	                                            UUID.randomUUID().toString(), accessTokenDO,
	                                            userStoreDomain);

        //remove the previous access token from cache and add the new access token info to the cache,
        // if it's enabled.
        if (cacheEnabled) {
            // Remove the old access token from the OAuthCache
            boolean isUsernameCaseSensitive = IdentityUtil.isUserStoreInUsernameCaseSensitive(authorizedUser);
            String cacheKeyString;
            if (isUsernameCaseSensitive) {
                cacheKeyString = clientId + ":" + authorizedUser + ":" + scope;
            } else {
                cacheKeyString = clientId + ":" + authorizedUser.toLowerCase() + ":" + scope;
            }

            OAuthCacheKey oauthCacheKey = new OAuthCacheKey(cacheKeyString);
            oauthCache.clearCacheEntry(oauthCacheKey);

            // Remove the old access token from the AccessTokenCache
            OAuthCacheKey accessTokenCacheKey = new OAuthCacheKey(oldAccessToken.getAccessToken());
            oauthCache.clearCacheEntry(accessTokenCacheKey);

            // Add new access token to the OAuthCache
            oauthCache.addToCache(oauthCacheKey, accessTokenDO);

            // Add new access token to the AccessTokenCache
            accessTokenCacheKey = new OAuthCacheKey(accessToken);
            oauthCache.addToCache(accessTokenCacheKey, accessTokenDO);

            if (log.isDebugEnabled()) {
                log.debug("Access Token info for the refresh token was added to the cache for " +
                        "the client id : " + clientId + ". Old access token entry was " +
                        "also removed from the cache.");
            }
        }

        if (log.isDebugEnabled()) {
            log.debug("Persisted an access token for the refresh token, " +
                    "Client ID : " + clientId +
                    "authorized user : " + tokReqMsgCtx.getAuthorizedUser() +
                    "timestamp : " + timestamp +
                    "validity period (s) : " + accessTokenDO.getValidityPeriod() +
                    "scope : " + OAuth2Util.buildScopeString(tokReqMsgCtx.getScope()) +
                    "Token State : " + OAuthConstants.TokenStates.TOKEN_STATE_ACTIVE +
                    "User Type : " + tokenType);
        }

        tokenRespDTO.setAccessToken(accessToken);
        tokenRespDTO.setRefreshToken(refreshToken);
        if (validityPeriodInMillis > 0) {
            tokenRespDTO.setExpiresIn(accessTokenDO.getValidityPeriod());
            tokenRespDTO.setExpiresInMillis(accessTokenDO.getValidityPeriodInMillis());
        } else {
            tokenRespDTO.setExpiresIn(Long.MAX_VALUE);
            tokenRespDTO.setExpiresInMillis(Long.MAX_VALUE);
        }
        tokenRespDTO.setAuthorizedScopes(scope);

        ArrayList<ResponseHeader> respHeaders = new ArrayList<ResponseHeader>();
        ResponseHeader header = new ResponseHeader();
        header.setKey("DeactivatedAccessToken");
        header.setValue(oldAccessToken.getAccessToken());
        respHeaders.add(header);

        tokReqMsgCtx.addProperty("RESPONSE_HEADERS", respHeaders.toArray(
                new ResponseHeader[respHeaders.size()]));

        return tokenRespDTO;
    }

    private OAuth2AccessTokenRespDTO handleError(String errorCode, String errorMsg) {
        OAuth2AccessTokenRespDTO tokenRespDTO;
        tokenRespDTO = new OAuth2AccessTokenRespDTO();
        tokenRespDTO.setError(true);
        tokenRespDTO.setErrorCode(errorCode);
        tokenRespDTO.setErrorMsg(errorMsg);
        return tokenRespDTO;
    }
}
