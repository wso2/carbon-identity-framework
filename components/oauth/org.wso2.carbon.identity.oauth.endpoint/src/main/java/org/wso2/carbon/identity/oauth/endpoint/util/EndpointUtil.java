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
package org.wso2.carbon.identity.oauth.endpoint.util;

import org.apache.axiom.util.base64.Base64Utils;
import org.apache.commons.io.Charsets;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.oltu.oauth2.common.exception.OAuthSystemException;
import org.wso2.carbon.base.ServerConfiguration;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.identity.application.authentication.framework.cache.AuthenticationRequestCacheEntry;
import org.wso2.carbon.identity.application.authentication.framework.model.AuthenticationRequest;
import org.wso2.carbon.identity.application.authentication.framework.util.FrameworkConstants;
import org.wso2.carbon.identity.application.authentication.framework.util.FrameworkUtils;
import org.wso2.carbon.identity.core.util.IdentityUtil;
import org.wso2.carbon.identity.oauth.cache.SessionDataCache;
import org.wso2.carbon.identity.oauth.cache.SessionDataCacheEntry;
import org.wso2.carbon.identity.oauth.cache.SessionDataCacheKey;
import org.wso2.carbon.identity.oauth.common.OAuthConstants;
import org.wso2.carbon.identity.oauth.common.exception.OAuthClientException;
import org.wso2.carbon.identity.oauth.config.OAuthServerConfiguration;
import org.wso2.carbon.identity.oauth2.IdentityOAuth2Exception;
import org.wso2.carbon.identity.oauth2.OAuth2Service;
import org.wso2.carbon.identity.oauth2.OAuth2TokenValidationService;
import org.wso2.carbon.identity.oauth2.model.OAuth2Parameters;
import org.wso2.carbon.identity.oauth2.util.OAuth2Util;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class EndpointUtil {

    private static final Log log = LogFactory.getLog(EndpointUtil.class);

    private EndpointUtil() {

    }

    /**
     * Returns the {@code OAuth2Service} instance
     *
     * @return
     */
    public static OAuth2Service getOAuth2Service() {
        return (OAuth2Service) PrivilegedCarbonContext.getThreadLocalCarbonContext()
                .getOSGiService(OAuth2Service.class);
    }

    /**
     * Returns the {@code OAuthServerConfiguration} instance
     *
     * @return
     */
    public static OAuthServerConfiguration getOAuthServerConfiguration() {
        return (OAuthServerConfiguration) PrivilegedCarbonContext.getThreadLocalCarbonContext()
                .getOSGiService(OAuthServerConfiguration.class);
    }

    /**
     * Returns the {@code OAuthServerConfiguration} instance
     *
     * @return
     */
    public static OAuth2TokenValidationService getOAuth2TokenValidationService() {
        return (OAuth2TokenValidationService) PrivilegedCarbonContext.getThreadLocalCarbonContext()
                .getOSGiService(OAuth2TokenValidationService.class);
    }

    /**
     * Returns the request validator class name
     *
     * @return
     * @throws OAuthSystemException
     */
    public static String getUserInfoRequestValidator() throws OAuthSystemException {
        return getOAuthServerConfiguration().getOpenIDConnectUserInfoEndpointRequestValidator();
    }

    /**
     * Returns the access token validator class name
     *
     * @return
     */
    public static String getAccessTokenValidator() {
        return getOAuthServerConfiguration().getOpenIDConnectUserInfoEndpointAccessTokenValidator();
    }

    /**
     * Returns the response builder class name
     *
     * @return
     */
    public static String getUserInfoResponseBuilder() {
        return getOAuthServerConfiguration().getOpenIDConnectUserInfoEndpointResponseBuilder();
    }

    /**
     * Returns the claim retriever class name
     *
     * @return
     */
    public static String getUserInfoClaimRetriever() {
        return getOAuthServerConfiguration().getOpenIDConnectUserInfoEndpointClaimRetriever();
    }

    /**
     * Return the claim dialect for the claim retriever
     *
     * @return
     */
    public static String getUserInfoClaimDialect() {
        return getOAuthServerConfiguration().getOpenIDConnectUserInfoEndpointClaimDialect();
    }

    /**
     * Extracts the username and password info from the HTTP Authorization Header
     *
     * @param authorizationHeader "Basic " + base64encode(username + ":" + password)
     * @return String array with client id and client secret.
     * @throws org.wso2.carbon.identity.base.IdentityException If the decoded data is null.
     */
    public static String[] extractCredentialsFromAuthzHeader(String authorizationHeader)
            throws OAuthClientException {
        String[] splitValues = authorizationHeader.trim().split(" ");
        if(splitValues.length == 2) {
            byte[] decodedBytes = Base64Utils.decode(splitValues[1].trim());
            if (decodedBytes != null) {
                String userNamePassword = new String(decodedBytes, Charsets.UTF_8);
                return userNamePassword.split(":");
            }
        }
        String errMsg = "Error decoding authorization header. Space delimited \"<authMethod> <base64Hash>\" format violated.";
        throw new OAuthClientException(errMsg);
    }

    /**
     * Returns the error page URL. If appName is not <code>null</code> it will be added as query parameter
     * to be displayed to the user. If redirect_uri is <code>null</code> the common error page URL will be returned.
     *
     * @param errorCode
     * @param errorMessage
     * @param appName
     * @return
     */
    public static String getErrorPageURL(String errorCode, String errorMessage, String appName, String redirectUri) {

        String errorPageUrl;
        if (StringUtils.isNotBlank(redirectUri)) {
            errorPageUrl = redirectUri;
        } else {
            errorPageUrl = OAuth2Util.OAuthURL.getOAuth2ErrorPageUrl();
        }
        try {
            errorPageUrl += "?" + OAuthConstants.OAUTH_ERROR_CODE + "=" + URLEncoder.encode(errorCode, "UTF-8") + "&"
                    + OAuthConstants.OAUTH_ERROR_MESSAGE + "=" + URLEncoder.encode(errorMessage, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            //ignore
            if (log.isDebugEnabled()){
                log.debug("Error while encoding the error page url", e);
            }
        }

        if (appName != null) {
            try {
                errorPageUrl += "application" + "=" + URLEncoder.encode(appName, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                //ignore
                if (log.isDebugEnabled()){
                    log.debug("Error while encoding the error page url", e);
                }
            }
        }

        return errorPageUrl;
    }

    /**
     * Returns the login page URL.
     *
     * @param checkAuthentication
     * @param forceAuthenticate
     * @param scopes
     * @return
     */
    public static String getLoginPageURL(String clientId, String sessionDataKey,
                                         boolean forceAuthenticate, boolean checkAuthentication, Set<String> scopes)
            throws IdentityOAuth2Exception {

        try {
            SessionDataCacheEntry entry = SessionDataCache.getInstance()
                    .getValueFromCache(new SessionDataCacheKey(sessionDataKey));

            return getLoginPageURL(clientId, sessionDataKey, forceAuthenticate,
                    checkAuthentication, scopes, entry.getParamMap());
        } finally {
            OAuth2Util.clearClientTenantId();
        }
    }

    /**
     * Returns the login page URL.
     *
     * @param clientId
     * @param sessionDataKey
     * @param reqParams
     * @param forceAuthenticate
     * @param checkAuthentication
     * @param scopes
     * @return
     * @throws UnsupportedEncodingException
     */
    public static String getLoginPageURL(String clientId, String sessionDataKey,
                                         boolean forceAuthenticate, boolean checkAuthentication, Set<String> scopes,
                                         Map<String, String[]> reqParams) throws IdentityOAuth2Exception {

        try {

            String type = "oauth2";

            if (scopes != null && scopes.contains("openid")) {
                type = "oidc";
            }
            String commonAuthURL = IdentityUtil.getServerURL(FrameworkConstants.COMMONAUTH, false, true);
            String selfPath = "/oauth2/authorize";
            AuthenticationRequest authenticationRequest = new AuthenticationRequest();

            int tenantId = OAuth2Util.getClientTenatId();

            //Build the authentication request context.
            authenticationRequest.setCommonAuthCallerPath(selfPath);
            authenticationRequest.setForceAuth(forceAuthenticate);
            authenticationRequest.setPassiveAuth(checkAuthentication);
            authenticationRequest.setRelyingParty(clientId);
            authenticationRequest.setTenantDomain(OAuth2Util.getTenantDomain(tenantId));
            authenticationRequest.setRequestQueryParams(reqParams);

            //Build an AuthenticationRequestCacheEntry which wraps AuthenticationRequestContext
            AuthenticationRequestCacheEntry authRequest = new AuthenticationRequestCacheEntry
                    (authenticationRequest);
            FrameworkUtils.addAuthenticationRequestToCache(sessionDataKey, authRequest);
            // Build new query param with only type and session data key
            StringBuilder queryStringBuilder = new StringBuilder();
            queryStringBuilder.append(commonAuthURL).
                    append("?").
                    append(FrameworkConstants.SESSION_DATA_KEY).
                    append("=").
                    append(sessionDataKey).
                    append("&").
                    append(FrameworkConstants.RequestParams.TYPE).
                    append("=").
                    append(type);

            return queryStringBuilder.toString();
        } finally {
            OAuth2Util.clearClientTenantId();
        }
    }

    /**
     * Returns the consent page URL.
     *
     * @param params
     * @param loggedInUser
     * @return
     */
    public static String getUserConsentURL(OAuth2Parameters params, String loggedInUser, String sessionDataKey,
                                           boolean isOIDC) throws OAuthSystemException {
        String queryString = "";
        if (log.isDebugEnabled()) {
            log.debug("Received Session Data Key is :  " + sessionDataKey);
            if (params == null) {
                log.debug("Received OAuth2 params are Null for UserConsentURL");
            }
        }
        SessionDataCache sessionDataCache = SessionDataCache.getInstance();
        SessionDataCacheEntry entry = sessionDataCache.getValueFromCache(new SessionDataCacheKey(sessionDataKey));
        String consentPage = null;
        String sessionDataKeyConsent = UUID.randomUUID().toString();
        try {
            if (entry == null) {
                if (log.isDebugEnabled()) {
                    log.debug("Cache Entry is Null from SessionDataCache ");
                }
            } else {
                sessionDataCache.addToCache(new SessionDataCacheKey(sessionDataKeyConsent),entry);
                queryString = URLEncoder.encode(entry.getQueryString(), "UTF-8");
            }


            if (isOIDC) {
                consentPage = OAuth2Util.OAuthURL.getOIDCConsentPageUrl();
            } else {
                consentPage = OAuth2Util.OAuthURL.getOAuth2ConsentPageUrl();
            }
            if (params != null) {
                consentPage += "?" + OAuthConstants.OIDC_LOGGED_IN_USER + "=" + URLEncoder.encode(loggedInUser,
                        "UTF-8") + "&application=" + URLEncoder.encode(params.getApplicationName(), "ISO-8859-1") +
                        "&" + OAuthConstants.OAuth20Params.SCOPE + "=" + URLEncoder.encode(EndpointUtil.getScope
                        (params), "ISO-8859-1") + "&" + OAuthConstants.SESSION_DATA_KEY_CONSENT + "=" + URLEncoder
                        .encode(sessionDataKeyConsent, "UTF-8") + "&spQueryParams=" + queryString;
            } else {
                throw new OAuthSystemException("Error while retrieving the application name");
            }
        } catch (UnsupportedEncodingException e) {
            throw new OAuthSystemException("Error while encoding the url", e);
        }

        return consentPage;
    }

    public static String getScope(OAuth2Parameters params) {
        StringBuilder scopes = new StringBuilder();
        for (String scope : params.getScopes()) {
            scopes.append(scope + " ");
        }
        return scopes.toString().trim();
    }

    public static String getRealmInfo() {
        return "Basic realm=" + getHostName();
    }

    public static String getHostName() {
        return ServerConfiguration.getInstance().getFirstProperty("HostName");
    }

}
