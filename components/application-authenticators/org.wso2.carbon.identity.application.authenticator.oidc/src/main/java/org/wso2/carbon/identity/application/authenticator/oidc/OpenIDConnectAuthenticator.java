/*
 * Copyright (c) 2013, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.identity.application.authenticator.oidc;

import net.minidev.json.JSONArray;
import net.minidev.json.JSONValue;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.oltu.oauth2.client.OAuthClient;
import org.apache.oltu.oauth2.client.URLConnectionClient;
import org.apache.oltu.oauth2.client.request.OAuthClientRequest;
import org.apache.oltu.oauth2.client.response.OAuthAuthzResponse;
import org.apache.oltu.oauth2.client.response.OAuthClientResponse;
import org.apache.oltu.oauth2.common.exception.OAuthProblemException;
import org.apache.oltu.oauth2.common.exception.OAuthSystemException;
import org.apache.oltu.oauth2.common.message.types.GrantType;
import org.apache.oltu.oauth2.common.utils.JSONUtils;
import org.wso2.carbon.base.MultitenantConstants;
import org.wso2.carbon.identity.application.authentication.framework.AbstractApplicationAuthenticator;
import org.wso2.carbon.identity.application.authentication.framework.FederatedApplicationAuthenticator;
import org.wso2.carbon.identity.application.authentication.framework.context.AuthenticationContext;
import org.wso2.carbon.identity.application.authentication.framework.exception.AuthenticationFailedException;
import org.wso2.carbon.identity.application.authentication.framework.model.AuthenticatedUser;
import org.wso2.carbon.identity.application.authentication.framework.util.FrameworkConstants;
import org.wso2.carbon.identity.application.authentication.framework.util.FrameworkUtils;
import org.wso2.carbon.identity.application.authenticator.oidc.internal.OpenIDConnectAuthenticatorServiceComponent;
import org.wso2.carbon.identity.application.common.model.ClaimMapping;
import org.wso2.carbon.identity.application.common.util.IdentityApplicationConstants;
import org.wso2.carbon.identity.base.IdentityConstants;
import org.wso2.carbon.identity.core.util.IdentityCoreConstants;
import org.wso2.carbon.identity.core.util.IdentityUtil;
import org.wso2.carbon.user.api.UserRealm;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.core.UserStoreManager;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class OpenIDConnectAuthenticator extends AbstractApplicationAuthenticator implements
        FederatedApplicationAuthenticator {

    private static final long serialVersionUID = -4154255583070524018L;

    private static Log log = LogFactory.getLog(OpenIDConnectAuthenticator.class);

    @Override
    public boolean canHandle(HttpServletRequest request) {

        if (log.isTraceEnabled()) {
            log.trace("Inside OpenIDConnectAuthenticator.canHandle()");
        }

        // Check commonauth got an OIDC response
        if (request.getParameter(OIDCAuthenticatorConstants.OAUTH2_GRANT_TYPE_CODE) != null
                && request.getParameter(OIDCAuthenticatorConstants.OAUTH2_PARAM_STATE) != null
                && OIDCAuthenticatorConstants.LOGIN_TYPE.equals(getLoginType(request))) {
            return true;
        } else if (request.getParameter(OIDCAuthenticatorConstants.OAUTH2_PARAM_STATE) != null &&
                request.getParameter(OIDCAuthenticatorConstants.OAUTH2_ERROR) != null) {
            //if sends error like access_denied
            return true;
        }
        // TODO : What if IdP failed?

        return false;
    }

    /**
     * @return
     */
    protected String getAuthorizationServerEndpoint(Map<String, String> authenticatorProperties) {
        return null;
    }

    /**
     * @return
     */
    protected String getCallbackUrl(Map<String, String> authenticatorProperties) {
        return authenticatorProperties.get(IdentityApplicationConstants.OAuth2.CALLBACK_URL);
    }

    /**
     * @return
     */
    protected String getTokenEndpoint(Map<String, String> authenticatorProperties) {
        return null;
    }

    /**
     * @param state
     * @return
     */
    protected String getState(String state, Map<String, String> authenticatorProperties) {
        return state;
    }

    /**
     * @return
     */
    protected String getScope(String scope, Map<String, String> authenticatorProperties) {
        return scope;
    }

    /**
     * @return
     */
    protected boolean requiredIDToken(Map<String, String> authenticatorProperties) {
        return true;
    }

    /**
     *
     * @param context
     * @param jsonObject
     * @param token
     * @return
     */

    protected String getAuthenticateUser(AuthenticationContext context, Map<String, Object> jsonObject,OAuthClientResponse token) {
        return (String) jsonObject.get("sub");
    }

    protected String getCallBackURL(Map<String, String> authenticatorProperties) {
        return getCallbackUrl(authenticatorProperties);
    }


    protected String getQueryString(Map<String, String> authenticatorProperties) {
        return authenticatorProperties.get(FrameworkConstants.QUERY_PARAMS);
    }

    /**
     * Get user info endpoint.
     * @param token OAuthClientResponse
     * @param authenticatorProperties Map<String, String> (Authenticator property, Property value)
     * @return User info endpoint.
     */
    protected String getUserInfoEndpoint(OAuthClientResponse token, Map<String, String> authenticatorProperties) {
        return authenticatorProperties.get(IdentityApplicationConstants.Authenticator.OIDC.USER_INFO_URL);
    }

    /**
     * Get subject attributes.
     * @param token OAuthClientResponse
     * @param authenticatorProperties Map<String, String> (Authenticator property, Property value)
     * @return Map<ClaimMapping, String> Claim mappings.
     */
    protected Map<ClaimMapping, String> getSubjectAttributes(OAuthClientResponse token,
                                                             Map<String, String> authenticatorProperties) {

        Map<ClaimMapping, String> claims = new HashMap<>();

        try {
            String accessToken = token.getParam(OIDCAuthenticatorConstants.ACCESS_TOKEN);
            String url = getUserInfoEndpoint(token, authenticatorProperties);
            String json = sendRequest(url, accessToken);

            if (StringUtils.isBlank(json)) {
                if(log.isDebugEnabled()) {
                    log.debug("Empty JSON response from user info endpoint. Unable to fetch user claims." +
                            " Proceeding without user claims");
                }
                return claims;
            }

            Map<String, Object> jsonObject = JSONUtils.parseJSON(json);

            for (Map.Entry<String, Object> data : jsonObject.entrySet()) {
                String key = data.getKey();
                Object value = data.getValue();

                if (value != null) {
                    claims.put(ClaimMapping.build(key, key, null, false), value.toString());
                }

                if (log.isDebugEnabled() &&
                        IdentityUtil.isTokenLoggable(IdentityConstants.IdentityTokens.USER_CLAIMS)) {
                    log.debug("Adding claims from end-point data mapping : " + key + " - " +
                            jsonObject.get(key).toString());
                }
            }
        } catch (IOException e) {
            log.error("Communication error occurred while accessing user info endpoint", e);
        }

        return claims;
    }

    @Override
    protected void initiateAuthenticationRequest(HttpServletRequest request,
                                                 HttpServletResponse response, AuthenticationContext context)
            throws AuthenticationFailedException {

        try {
            Map<String, String> authenticatorProperties = context.getAuthenticatorProperties();
            if (authenticatorProperties != null) {
                String clientId = authenticatorProperties.get(OIDCAuthenticatorConstants.CLIENT_ID);
                String authorizationEP = getAuthorizationServerEndpoint(authenticatorProperties);

                if (authorizationEP == null) {
                    authorizationEP = authenticatorProperties
                            .get(OIDCAuthenticatorConstants.OAUTH2_AUTHZ_URL);
                }

                String callbackurl = getCallbackUrl(authenticatorProperties);

                if (StringUtils.isBlank(callbackurl)) {
                    callbackurl = IdentityUtil.getServerURL(FrameworkConstants.COMMONAUTH, true, true);
                }

                String state = context.getContextIdentifier() + ","
                        + OIDCAuthenticatorConstants.LOGIN_TYPE;

                state = getState(state, authenticatorProperties);

                OAuthClientRequest authzRequest;

                String queryString = getQueryString(authenticatorProperties);
                Map<String, String> paramValueMap = new HashMap<>();

                if (StringUtils.isNotBlank(queryString)) {
                    String[] params = queryString.split("&");
                    if (params != null && params.length > 0) {
                        for (String param : params) {
                            String[] intParam = param.split("=");
                            paramValueMap.put(intParam[0], intParam[1]);
                        }
                        context.setProperty("oidc:param.map", paramValueMap);
                    }
                }

                String scope = paramValueMap.get("scope");

                if (scope == null) {
                    scope = OIDCAuthenticatorConstants.OAUTH_OIDC_SCOPE;
                }

                scope = getScope(scope, authenticatorProperties);

                if (queryString != null && queryString.toLowerCase().contains("scope=")
                        && queryString.toLowerCase().contains("redirect_uri=")) {
                    authzRequest = OAuthClientRequest.authorizationLocation(authorizationEP)
                            .setClientId(clientId)
                            .setResponseType(OIDCAuthenticatorConstants.OAUTH2_GRANT_TYPE_CODE)
                            .setState(state).buildQueryMessage();
                } else if (queryString != null && queryString.toLowerCase().contains("scope=")) {
                    authzRequest = OAuthClientRequest.authorizationLocation(authorizationEP)
                            .setClientId(clientId).setRedirectURI(callbackurl)
                            .setResponseType(OIDCAuthenticatorConstants.OAUTH2_GRANT_TYPE_CODE)
                            .setState(state).buildQueryMessage();
                } else if (queryString != null
                        && queryString.toLowerCase().contains("redirect_uri=")) {
                    authzRequest = OAuthClientRequest.authorizationLocation(authorizationEP)
                            .setClientId(clientId)
                            .setResponseType(OIDCAuthenticatorConstants.OAUTH2_GRANT_TYPE_CODE)
                            .setScope(OIDCAuthenticatorConstants.OAUTH_OIDC_SCOPE).setState(state)
                            .buildQueryMessage();

                } else {
                    authzRequest = OAuthClientRequest.authorizationLocation(authorizationEP)
                            .setClientId(clientId).setRedirectURI(callbackurl)
                            .setResponseType(OIDCAuthenticatorConstants.OAUTH2_GRANT_TYPE_CODE)
                            .setScope(scope).setState(state).buildQueryMessage();
                }

                String loginPage = authzRequest.getLocationUri();
                String domain = request.getParameter("domain");

                if (domain != null) {
                    loginPage = loginPage + "&fidp=" + domain;
                }

                if (queryString != null) {
                    if (!queryString.startsWith("&")) {
                        loginPage = loginPage + "&" + queryString;
                    } else {
                        loginPage = loginPage + queryString;
                    }
                }
                response.sendRedirect(loginPage);
            } else {
                if (log.isDebugEnabled()) {
                    log.debug("Error while retrieving properties. Authenticator Properties cannot be null");
                }
                throw new AuthenticationFailedException(
                        "Error while retrieving properties. Authenticator Properties cannot be null");
            }
        } catch (IOException e) {
            log.error("Exception while sending to the login page", e);
            throw new AuthenticationFailedException(e.getMessage(), e);
        } catch (OAuthSystemException e) {
            log.error("Exception while building authorization code request", e);
            throw new AuthenticationFailedException(e.getMessage(), e);
        }
        return;
    }

    @Override
    protected void processAuthenticationResponse(HttpServletRequest request, HttpServletResponse response,
                                                 AuthenticationContext context)
            throws AuthenticationFailedException {

        try {

            Map<String, String> authenticatorProperties = context.getAuthenticatorProperties();

            String clientId = authenticatorProperties.get(OIDCAuthenticatorConstants.CLIENT_ID);
            String clientSecret = authenticatorProperties.get(OIDCAuthenticatorConstants.CLIENT_SECRET);
            String tokenEndPoint = getTokenEndpoint(authenticatorProperties);

            if (tokenEndPoint == null) {
                tokenEndPoint = authenticatorProperties.get(OIDCAuthenticatorConstants.OAUTH2_TOKEN_URL);
            }

            String callbackUrl = getCallbackUrl(authenticatorProperties);

            if (StringUtils.isBlank(callbackUrl)) {
                callbackUrl = IdentityUtil.getServerURL(FrameworkConstants.COMMONAUTH, true, true);
            }

            @SuppressWarnings({"unchecked"})
            Map<String, String> paramValueMap = (Map<String, String>) context.getProperty("oidc:param.map");

            if (paramValueMap != null && paramValueMap.containsKey("redirect_uri")) {
                callbackUrl = paramValueMap.get("redirect_uri");
            }

            OAuthAuthzResponse authzResponse = OAuthAuthzResponse.oauthCodeAuthzResponse(request);
            String code = authzResponse.getCode();

            OAuthClientRequest accessRequest =
                    getAccessRequest(tokenEndPoint, clientId, code, clientSecret, callbackUrl);

            // Create OAuth client that uses custom http client under the hood
            OAuthClient oAuthClient = new OAuthClient(new URLConnectionClient());
            OAuthClientResponse oAuthResponse = getOauthResponse(oAuthClient, accessRequest);

            // TODO : return access token and id token to framework
            String accessToken = oAuthResponse.getParam(OIDCAuthenticatorConstants.ACCESS_TOKEN);

            if (StringUtils.isBlank(accessToken)) {
                throw new AuthenticationFailedException("Access token is empty or null");
            }

            String idToken = oAuthResponse.getParam(OIDCAuthenticatorConstants.ID_TOKEN);

            if (StringUtils.isBlank(idToken) && requiredIDToken(authenticatorProperties)) {
                throw new AuthenticationFailedException("Id token is required and is missing");
            }

            context.setProperty(OIDCAuthenticatorConstants.ACCESS_TOKEN, accessToken);

            AuthenticatedUser authenticatedUserObj;
            Map<ClaimMapping, String> claims = new HashMap<>();
            Map<String, Object> jsonObject = new HashMap<>();

            if (StringUtils.isNotBlank(idToken)) {

                context.setProperty(OIDCAuthenticatorConstants.ID_TOKEN, idToken);

                String base64Body = idToken.split("\\.")[1];
                byte[] decoded = Base64.decodeBase64(base64Body.getBytes());
                String json = new String(decoded);

                jsonObject = JSONUtils.parseJSON(json);

                if (jsonObject == null) {

                    if (log.isDebugEnabled()) {
                        log.debug("Decoded json object is null");
                    }
                    throw new AuthenticationFailedException("Decoded json object is null");
                }

                if (log.isDebugEnabled() &&
                        IdentityUtil.isTokenLoggable(IdentityConstants.IdentityTokens.USER_ID_TOKEN)) {
                    log.debug("Retrieved the User Information:" + jsonObject);
                }
                String authenticatedUser = null;
                String isSubjectInClaimsProp = context.getAuthenticatorProperties().get(
                        IdentityApplicationConstants.Authenticator.SAML2SSO.IS_USER_ID_IN_CLAIMS);

                if (Boolean.parseBoolean(isSubjectInClaimsProp)) {
                    authenticatedUser = getSubjectFromUserIDClaimURI(context);

                    if (authenticatedUser == null && log.isDebugEnabled()) {
                        log.debug("Subject claim could not be found amongst subject attributes. " +
                                "Defaulting to the sub attribute in IDToken.");
                    }
                }

                if (authenticatedUser == null) {
                    authenticatedUser = getAuthenticateUser(context, jsonObject, oAuthResponse);

                    if (authenticatedUser == null) {
                        throw new AuthenticationFailedException("Cannot find federated User Identifier");
                    }
                }

                String attributeSeparator = null;

                try {

                    String tenantDomain = context.getTenantDomain();

                    if (StringUtils.isBlank(tenantDomain)) {
                        tenantDomain = MultitenantConstants.SUPER_TENANT_DOMAIN_NAME;
                    }
                    int tenantId = OpenIDConnectAuthenticatorServiceComponent.getRealmService()
                            .getTenantManager().getTenantId(tenantDomain);
                    UserRealm userRealm = OpenIDConnectAuthenticatorServiceComponent.getRealmService()
                            .getTenantUserRealm(tenantId);

                    if (userRealm != null) {
                        UserStoreManager userStore = (UserStoreManager) userRealm.getUserStoreManager();
                        attributeSeparator = userStore.getRealmConfiguration()
                                .getUserStoreProperty(IdentityCoreConstants.MULTI_ATTRIBUTE_SEPARATOR);
                        if (log.isDebugEnabled()) {
                            log.debug("For the claim mapping: " + attributeSeparator +
                                    " is used as the attributeSeparator in tenant: " + tenantDomain);
                        }
                    }
                } catch (UserStoreException e) {
                    throw new AuthenticationFailedException("Error while retrieving multi attribute separator", e);
                }

                for (Map.Entry<String, Object> entry : jsonObject.entrySet()) {
                    buildClaimMappings(claims, entry, attributeSeparator);
                }
                authenticatedUserObj = AuthenticatedUser
                        .createFederateAuthenticatedUserFromSubjectIdentifier(authenticatedUser);
            } else {

                if (log.isDebugEnabled()) {
                    log.debug("The IdToken is null");
                }
                authenticatedUserObj = AuthenticatedUser
                        .createFederateAuthenticatedUserFromSubjectIdentifier(
                                getAuthenticateUser(context, jsonObject, oAuthResponse));
            }

            claims.putAll(getSubjectAttributes(oAuthResponse, authenticatorProperties));
            authenticatedUserObj.setUserAttributes(claims);

            context.setSubject(authenticatedUserObj);

        } catch (OAuthProblemException e) {
            throw new AuthenticationFailedException("Authentication process failed", e);
        }
    }

    protected void buildClaimMappings(Map<ClaimMapping, String> claims, Map.Entry<String, Object> entry, String
            separator) {
        String claimValue = null;
        if (StringUtils.isBlank(separator)) {
            separator = IdentityCoreConstants.MULTI_ATTRIBUTE_SEPARATOR_DEFAULT;
        }
        try {
            JSONArray jsonArray = (JSONArray) JSONValue.parseWithException(entry.getValue().toString());
            if (jsonArray != null && jsonArray.size() > 0) {
                Iterator attributeIterator = jsonArray.iterator();
                while (attributeIterator.hasNext()) {
                    if (claimValue == null) {
                        claimValue = attributeIterator.next().toString();
                    } else {
                        claimValue = claimValue + separator + attributeIterator.next().toString();
                    }
                }

            }
        } catch (Exception e) {
            claimValue = entry.getValue().toString();
        }

        claims.put(ClaimMapping.build(entry.getKey(), entry.getKey(), null, false), claimValue);
        if (log.isDebugEnabled() && IdentityUtil.isTokenLoggable(IdentityConstants.IdentityTokens.USER_CLAIMS)) {
            log.debug("Adding claim mapping : " + entry.getKey() + " <> " + entry.getKey() + " : " + claimValue);
        }

    }

    private OAuthClientRequest getAccessRequest(String tokenEndPoint, String clientId, String code, String clientSecret,
                                                String callbackurl)
            throws AuthenticationFailedException {

        OAuthClientRequest accessRequest = null;
        try {
            accessRequest = OAuthClientRequest.tokenLocation(tokenEndPoint)
                    .setGrantType(GrantType.AUTHORIZATION_CODE).setClientId(clientId)
                    .setClientSecret(clientSecret).setRedirectURI(callbackurl).setCode(code)
                    .buildBodyMessage();

        } catch (OAuthSystemException e) {
            if (log.isDebugEnabled()) {
                log.debug("Exception while building request for request access token", e);
            }
            throw new AuthenticationFailedException(e.getMessage(), e);
        }
        return accessRequest;
    }

    private OAuthClientResponse getOauthResponse(OAuthClient oAuthClient, OAuthClientRequest accessRequest)
            throws AuthenticationFailedException {

        OAuthClientResponse oAuthResponse = null;
        try {
            oAuthResponse = oAuthClient.accessToken(accessRequest);
        } catch (OAuthSystemException e) {
            if (log.isDebugEnabled()) {
                log.debug("Exception while requesting access token", e);
            }
            throw new AuthenticationFailedException(e.getMessage(), e);
        } catch (OAuthProblemException e) {
            if (log.isDebugEnabled()) {
                log.debug("Exception while requesting access token", e);
            }
        }
        return oAuthResponse;
    }

    @Override
    public String getContextIdentifier(HttpServletRequest request) {

        if (log.isTraceEnabled()) {
            log.trace("Inside OpenIDConnectAuthenticator.getContextIdentifier()");
        }
        String state = request.getParameter(OIDCAuthenticatorConstants.OAUTH2_PARAM_STATE);
        if (state != null) {
            return state.split(",")[0];
        } else {
            return null;
        }
    }

    private String getLoginType(HttpServletRequest request) {
        String state = request.getParameter(OIDCAuthenticatorConstants.OAUTH2_PARAM_STATE);
        if (state != null) {
            return state.split(",")[1];
        } else {
            return null;
        }
    }

    @Override
    public String getFriendlyName() {
        return "openidconnect";
    }

    @Override
    public String getName() {
        return OIDCAuthenticatorConstants.AUTHENTICATOR_NAME;
    }

    @Override
    public String getClaimDialectURI() {
        return "http://wso2.org/oidc/claim";
    }


    /**
     * @subject
     */
    protected String getSubjectFromUserIDClaimURI(AuthenticationContext context) {
        String subject = null;
        try {
            subject = FrameworkUtils.getFederatedSubjectFromClaims(context, getClaimDialectURI());
        } catch (Exception e) {
            if(log.isDebugEnabled()) {
                log.debug("Couldn't find the subject claim from claim mappings ", e);
            }
        }
        return subject;
    }

    /**
     * Request user claims from user info endpoint.
     * @param url User info endpoint.
     * @param accessToken Access token.
     * @return Response string.
     * @throws IOException
     */
    protected String sendRequest(String url, String accessToken) throws IOException {

        if (log.isDebugEnabled()) {
            log.debug("Claim URL: " + url);
        }

        if (url == null) {
            return StringUtils.EMPTY;
        }

        StringBuilder builder = new StringBuilder();
        BufferedReader reader = null;

        try {
            URL obj = new URL(url);
            HttpURLConnection urlConnection = (HttpURLConnection) obj.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.setRequestProperty("Authorization", "Bearer " + accessToken);
            reader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
            String inputLine = reader.readLine();

            while (inputLine != null) {
                builder.append(inputLine).append("\n");
                inputLine = reader.readLine();
            }
        } finally {
            if (reader != null) {
                reader.close();
            }
        }

        if (log.isDebugEnabled() && IdentityUtil.isTokenLoggable(IdentityConstants.IdentityTokens.USER_ID_TOKEN)) {
            log.debug("response: " + builder.toString());
        }
        return builder.toString();
    }
}
