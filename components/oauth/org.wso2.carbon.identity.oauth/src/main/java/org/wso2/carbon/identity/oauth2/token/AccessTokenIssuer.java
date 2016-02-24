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

package org.wso2.carbon.identity.oauth2.token;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.oltu.oauth2.common.error.OAuthError;
import org.apache.oltu.oauth2.common.message.types.GrantType;
import org.wso2.carbon.identity.base.IdentityException;
import org.wso2.carbon.identity.oauth.dao.OAuthAppDO;
import org.wso2.carbon.identity.oauth.cache.AppInfoCache;
import org.wso2.carbon.identity.oauth.cache.AuthorizationGrantCache;
import org.wso2.carbon.identity.oauth.cache.AuthorizationGrantCacheEntry;
import org.wso2.carbon.identity.oauth.cache.AuthorizationGrantCacheKey;
import org.wso2.carbon.identity.oauth.common.OAuthConstants;
import org.wso2.carbon.identity.oauth.common.exception.InvalidOAuthClientException;
import org.wso2.carbon.identity.oauth.config.OAuthServerConfiguration;
import org.wso2.carbon.identity.oauth.dao.OAuthAppDAO;
import org.wso2.carbon.identity.oauth2.IdentityOAuth2Exception;
import org.wso2.carbon.identity.oauth2.ResponseHeader;
import org.wso2.carbon.identity.oauth2.dto.OAuth2AccessTokenReqDTO;
import org.wso2.carbon.identity.oauth2.dto.OAuth2AccessTokenRespDTO;
import org.wso2.carbon.identity.oauth2.token.handlers.clientauth.ClientAuthenticationHandler;
import org.wso2.carbon.identity.oauth2.token.handlers.grant.AuthorizationGrantHandler;
import org.wso2.carbon.identity.oauth2.util.OAuth2Util;
import org.wso2.carbon.identity.openidconnect.IDTokenBuilder;
import org.wso2.carbon.utils.CarbonUtils;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

/**
 * This class is used to issue access tokens and refresh tokens.
 */
public class AccessTokenIssuer {

    private static AccessTokenIssuer instance;
    private static Log log = LogFactory.getLog(AccessTokenIssuer.class);
    private Map<String, AuthorizationGrantHandler> authzGrantHandlers =
            new Hashtable<String, AuthorizationGrantHandler>();
    private List<ClientAuthenticationHandler> clientAuthenticationHandlers =
            new ArrayList<ClientAuthenticationHandler>();
    private AppInfoCache appInfoCache;

    /**
     * Private constructor which will not allow to create objects of this class from outside
     */
    private AccessTokenIssuer() throws IdentityOAuth2Exception {

        authzGrantHandlers = OAuthServerConfiguration.getInstance().getSupportedGrantTypes();
        clientAuthenticationHandlers = OAuthServerConfiguration.getInstance().getSupportedClientAuthHandlers();
        appInfoCache = AppInfoCache.getInstance();
        if (appInfoCache != null) {
            if (log.isDebugEnabled()) {
                log.debug("Successfully created AppInfoCache under " + OAuthConstants.OAUTH_CACHE_MANAGER);
            }
        } else {
            log.error("Error while creating AppInfoCache");
        }

    }
    /**
     * Singleton method
     *
     * @return AccessTokenIssuer
     */
    public static AccessTokenIssuer getInstance() throws IdentityOAuth2Exception {

        CarbonUtils.checkSecurity();

        if (instance == null) {
            synchronized (AccessTokenIssuer.class) {
                if (instance == null) {
                    instance = new AccessTokenIssuer();
                }
            }
        }
        return instance;
    }

    /**
     * Issue access token using the respective grant handler and client authentication handler.
     *
     * @param tokenReqDTO
     * @return access token response
     * @throws IdentityException
     * @throws InvalidOAuthClientException
     */
    public OAuth2AccessTokenRespDTO issue(OAuth2AccessTokenReqDTO tokenReqDTO)
            throws IdentityException, InvalidOAuthClientException {

        String grantType = tokenReqDTO.getGrantType();
        OAuth2AccessTokenRespDTO tokenRespDTO;

        AuthorizationGrantHandler authzGrantHandler = authzGrantHandlers.get(grantType);

        OAuthTokenReqMessageContext tokReqMsgCtx = new OAuthTokenReqMessageContext(tokenReqDTO);

        // If multiple client authentication methods have been used the authorization server must reject the request
        int authenticatorHandlerIndex = -1;
        for (int i = 0; i < clientAuthenticationHandlers.size(); i++) {
            if (clientAuthenticationHandlers.get(i).canAuthenticate(tokReqMsgCtx)) {
                if (authenticatorHandlerIndex > -1) {
                    log.debug("Multiple Client Authentication Methods used for client id : " +
                            tokenReqDTO.getClientId());
                    tokenRespDTO = handleError(
                            OAuthConstants.OAuthError.TokenResponse.UNSUPPORTED_CLIENT_AUTHENTICATION_METHOD,
                            "Unsupported Client Authentication Method!", tokenReqDTO);
                    setResponseHeaders(tokReqMsgCtx, tokenRespDTO);
                    return tokenRespDTO;
                }
                authenticatorHandlerIndex = i;
            }
        }
        if (authenticatorHandlerIndex < 0 && authzGrantHandler.isConfidentialClient()) {
            log.debug("Confidential client cannot be authenticated for client id : " +
                    tokenReqDTO.getClientId());
            tokenRespDTO = handleError(
                    OAuthConstants.OAuthError.TokenResponse.UNSUPPORTED_CLIENT_AUTHENTICATION_METHOD,
                    "Unsupported Client Authentication Method!", tokenReqDTO);
            setResponseHeaders(tokReqMsgCtx, tokenRespDTO);
            return tokenRespDTO;
        }

        ClientAuthenticationHandler clientAuthHandler = null;
        if (authenticatorHandlerIndex > -1) {
            clientAuthHandler = clientAuthenticationHandlers.get(authenticatorHandlerIndex);
        }
        boolean isAuthenticated;
        if (clientAuthHandler != null) {
            isAuthenticated = clientAuthHandler.authenticateClient(tokReqMsgCtx);
        } else {
            isAuthenticated = true;
        }
        if (!isAuthenticated) {
            if(log.isDebugEnabled()) {
                log.debug("Client Authentication failed for client Id: " + tokenReqDTO.getClientId());
            }
            tokenRespDTO = handleError(OAuthError.TokenResponse.INVALID_CLIENT,
                    "Client credentials are invalid.", tokenReqDTO);
            setResponseHeaders(tokReqMsgCtx, tokenRespDTO);
            return tokenRespDTO;
        }

        // loading the stored application data
        OAuthAppDO oAuthAppDO = getAppInformation(tokenReqDTO);
        if (!authzGrantHandler.isOfTypeApplicationUser()) {
            tokReqMsgCtx.setAuthorizedUser(oAuthAppDO.getUser());
        }

        boolean isValidGrant = false;
        String error = "Provided Authorization Grant is invalid";
        try {
            isValidGrant = authzGrantHandler.validateGrant(tokReqMsgCtx);
        } catch (IdentityOAuth2Exception e) {
            if(log.isDebugEnabled()){
                log.debug("Error occurred while validating grant", e);
            }
            error = e.getMessage();
        }

        if (!isValidGrant) {
            if (log.isDebugEnabled()) {
                log.debug("Invalid Grant provided by the client Id: " + tokenReqDTO.getClientId());
            }
            tokenRespDTO = handleError(OAuthError.TokenResponse.INVALID_GRANT, error, tokenReqDTO);
            setResponseHeaders(tokReqMsgCtx, tokenRespDTO);
            return tokenRespDTO;
        }

        boolean isAuthorized = authzGrantHandler.authorizeAccessDelegation(tokReqMsgCtx);
        if (!isAuthorized) {
            if(log.isDebugEnabled()) {
                log.debug("Invalid authorization for client Id = " + tokenReqDTO.getClientId());
            }
            tokenRespDTO = handleError(OAuthError.TokenResponse.UNAUTHORIZED_CLIENT,
                    "Unauthorized Client!", tokenReqDTO);
            setResponseHeaders(tokReqMsgCtx, tokenRespDTO);
            return tokenRespDTO;
        }

        boolean isValidScope = authzGrantHandler.validateScope(tokReqMsgCtx);
        if (!isValidScope) {
            if(log.isDebugEnabled()) {
                log.debug("Invalid scope provided by client Id: " + tokenReqDTO.getClientId());
            }
            tokenRespDTO = handleError(OAuthError.TokenResponse.INVALID_SCOPE, "Invalid Scope!", tokenReqDTO);
            setResponseHeaders(tokReqMsgCtx, tokenRespDTO);
            return tokenRespDTO;
        }

	try {
	    // set the token request context to be used by downstream handlers. This is introduced as a fix for
	    // IDENTITY-4111.
	    OAuth2Util.setTokenRequestContext(tokReqMsgCtx);
	    tokenRespDTO = authzGrantHandler.issue(tokReqMsgCtx);
	} finally {
	    // clears the token request context.
	    OAuth2Util.clearTokenRequestContext();
	}
	
        tokenRespDTO.setCallbackURI(oAuthAppDO.getCallbackUrl());

        String[] scopes = tokReqMsgCtx.getScope();
        if (scopes != null && scopes.length > 0) {
            StringBuilder scopeString = new StringBuilder("");
            for (String scope : scopes) {
                scopeString.append(scope);
                scopeString.append(" ");
            }
            tokenRespDTO.setAuthorizedScopes(scopeString.toString().trim());
        }

        setResponseHeaders(tokReqMsgCtx, tokenRespDTO);

        //Do not change this log format as these logs use by external applications
        if (log.isDebugEnabled()) {
            log.debug("Access token issued to client Id: " + tokenReqDTO.getClientId() + " username: " +
                    tokReqMsgCtx.getAuthorizedUser() + " and scopes: " + tokenRespDTO.getAuthorizedScopes());
        }

        if (tokReqMsgCtx.getScope() != null && OAuth2Util.isOIDCAuthzRequest(tokReqMsgCtx.getScope())) {
            IDTokenBuilder builder = OAuthServerConfiguration.getInstance().getOpenIDConnectIDTokenBuilder();
            tokenRespDTO.setIDToken(builder.buildIDToken(tokReqMsgCtx, tokenRespDTO));
        }

        if (tokenReqDTO.getGrantType().equals(GrantType.AUTHORIZATION_CODE.toString())) {
            addUserAttributesToCache(tokenReqDTO, tokenRespDTO);
        }

        return tokenRespDTO;
    }

    /**
     * Add user attributes to cache.
     *
     * @param tokenReqDTO
     * @param tokenRespDTO
     */
    private void addUserAttributesToCache(OAuth2AccessTokenReqDTO tokenReqDTO, OAuth2AccessTokenRespDTO tokenRespDTO) {
        AuthorizationGrantCacheKey oldCacheKey = new AuthorizationGrantCacheKey(tokenReqDTO.getAuthorizationCode());
        //checking getUserAttributesId value of cacheKey before retrieve entry from cache as it causes to NPE
        if (oldCacheKey.getUserAttributesId() != null) {
            AuthorizationGrantCacheEntry authorizationGrantCacheEntry = AuthorizationGrantCache.getInstance().getValueFromCacheByCode(oldCacheKey);
            AuthorizationGrantCacheKey newCacheKey = new AuthorizationGrantCacheKey(tokenRespDTO.getAccessToken());
            authorizationGrantCacheEntry.setTokenId(tokenRespDTO.getTokenId());
            if (AuthorizationGrantCache.getInstance().getValueFromCacheByToken(newCacheKey) == null) {
                if(log.isDebugEnabled()){
                   log.debug("No AuthorizationGrantCache entry found for the access token:"+ newCacheKey.getUserAttributesId()+
                   ", hence adding to cache");
                }
                AuthorizationGrantCache.getInstance().addToCacheByToken(newCacheKey, authorizationGrantCacheEntry);
                AuthorizationGrantCache.getInstance().clearCacheEntryByCode(oldCacheKey);
            } else{
                //if the user attributes are already saved for access token, no need to add again.
            }
        }
    }

    /**
     * Get Oauth application information
     *
     * @param tokenReqDTO
     * @return Oauth app information
     * @throws IdentityOAuth2Exception
     * @throws InvalidOAuthClientException
     */
    private OAuthAppDO getAppInformation(OAuth2AccessTokenReqDTO tokenReqDTO) throws IdentityOAuth2Exception, InvalidOAuthClientException {
        OAuthAppDO oAuthAppDO = appInfoCache.getValueFromCache(tokenReqDTO.getClientId());
        if (oAuthAppDO != null) {
            return oAuthAppDO;
        } else {
            oAuthAppDO = new OAuthAppDAO().getAppInformation(tokenReqDTO.getClientId());
            appInfoCache.addToCache(tokenReqDTO.getClientId(), oAuthAppDO);
            return oAuthAppDO;
        }
    }

    /**
     * Handle error scenarios in issueing the access token.
     *
     * @param errorCode
     * @param errorMsg
     * @param tokenReqDTO
     * @return Access token response DTO
     */
    private OAuth2AccessTokenRespDTO handleError(String errorCode,
                                                 String errorMsg,
                                                 OAuth2AccessTokenReqDTO tokenReqDTO) {
        if (log.isDebugEnabled()) {
            log.debug("OAuth-Error-Code=" + errorCode + " client-id=" + tokenReqDTO.getClientId()
                    + " grant-type=" + tokenReqDTO.getGrantType()
                    + " scope=" + OAuth2Util.buildScopeString(tokenReqDTO.getScope()));
        }
        OAuth2AccessTokenRespDTO tokenRespDTO;
        tokenRespDTO = new OAuth2AccessTokenRespDTO();
        tokenRespDTO.setError(true);
        tokenRespDTO.setErrorCode(errorCode);
        tokenRespDTO.setErrorMsg(errorMsg);
        return tokenRespDTO;
    }

    /**
     * Set headers in OAuth2AccessTokenRespDTO
     * @param tokReqMsgCtx
     * @param tokenRespDTO
     */
    private void setResponseHeaders(OAuthTokenReqMessageContext tokReqMsgCtx,
                                    OAuth2AccessTokenRespDTO tokenRespDTO) {
        if (tokReqMsgCtx.getProperty("RESPONSE_HEADERS") != null) {
            tokenRespDTO.setResponseHeaders((ResponseHeader[]) tokReqMsgCtx.getProperty("RESPONSE_HEADERS"));
        }
    }
}
