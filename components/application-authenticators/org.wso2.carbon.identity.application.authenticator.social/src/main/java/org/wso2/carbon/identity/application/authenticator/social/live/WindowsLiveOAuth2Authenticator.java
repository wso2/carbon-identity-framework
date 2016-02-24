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
package org.wso2.carbon.identity.application.authenticator.social.live;

import org.apache.commons.lang.StringUtils;
import org.apache.oltu.oauth2.client.response.OAuthClientResponse;
import org.wso2.carbon.identity.application.authentication.framework.context.AuthenticationContext;
import org.wso2.carbon.identity.application.authenticator.oidc.OIDCAuthenticatorConstants;
import org.wso2.carbon.identity.application.authenticator.oidc.OpenIDConnectAuthenticator;
import org.wso2.carbon.identity.application.common.model.Property;
import org.wso2.carbon.identity.application.common.util.IdentityApplicationConstants;
import org.wso2.carbon.identity.core.util.IdentityIOStreamUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class WindowsLiveOAuth2Authenticator extends OpenIDConnectAuthenticator {

    private static final long serialVersionUID = -4154255583070524018L;
    private String tokenEndpoint;
    private String oAuthEndpoint;
    private String userInfoEndpoint;

    /**
     * initiate tokenEndpoint reading from application-authentication.xml
     */
    private void initTokenEndpoint() {
        this.tokenEndpoint = getAuthenticatorConfig().getParameterMap().get(WindowsLiveOAuth2AuthenticatorConstants
                .WINDOWS_LIVE_TOKEN_URL);
        if (StringUtils.isBlank(this.tokenEndpoint)) {
            this.tokenEndpoint = IdentityApplicationConstants.WINDOWS_LIVE_TOKEN_URL;
        }
    }

    /**
     * initiate oAuthEndpoint reading from application-authentication.xml
     */
    private void initOAuthEndpoint() {
        this.oAuthEndpoint = getAuthenticatorConfig().getParameterMap().get(WindowsLiveOAuth2AuthenticatorConstants
                .WINDOWS_LIVE_AUTHZ_URL);
        if (StringUtils.isBlank(this.oAuthEndpoint)) {
            this.oAuthEndpoint = IdentityApplicationConstants.WINDOWS_LIVE_OAUTH_URL;
        }
    }

    /**
     * initiate userInfoEndpoint reading from application-authentication.xml
     */
    private void initUserInfoEndPoint() {
        this.userInfoEndpoint = getAuthenticatorConfig().getParameterMap().get
                (WindowsLiveOAuth2AuthenticatorConstants.WINDOWS_LIVE_USER_INFO_URL);
        if (StringUtils.isBlank(this.userInfoEndpoint)) {
            this.userInfoEndpoint = IdentityApplicationConstants.WINDOWS_LIVE_USERINFO_URL;
        }
    }

    /**
     *
     * @return userInfoEndpoint
     */
    @Override
    protected String getUserInfoEndpoint(OAuthClientResponse token, Map<String, String> authenticatorProperties) {

        if (StringUtils.isBlank(this.userInfoEndpoint)) {
            initUserInfoEndPoint();
        }

        return this.userInfoEndpoint;
    }

    /**
     *
     * @return oAuthEndpoint
     */
    @Override
    protected String getAuthorizationServerEndpoint(Map<String, String> authenticatorProperties) {
        if (StringUtils.isBlank(this.oAuthEndpoint)) {
            initOAuthEndpoint();
        }
        return this.oAuthEndpoint;
    }

    /**
     * @return
     */
    @Override
    protected String getTokenEndpoint(Map<String, String> authenticatorProperties) {
        if (StringUtils.isBlank(this.tokenEndpoint)) {
            initTokenEndpoint();
        }
        return this.tokenEndpoint;
    }

    /**
     * @param state
     * @return
     */
    @Override
    protected String getState(String state, Map<String, String> authenticatorProperties) {
        return state;
    }

    /**
     * @return
     */
    @Override
    protected String getScope(String scope, Map<String, String> authenticatorProperties) {
        return "wl.contacts_emails"; // bingads.manage
    }

    /**
     * Get the default claim dialect URI.
     * @return Claim dialect URI.
     */
    @Override
    public String getClaimDialectURI() {

        // We do not have a default claim dialect.
        return null;
    }

    /**
     * Always return false since there is no ID token in MS Live.
     * @return True if ID token is required.
     */
    @Override
    protected boolean requiredIDToken(Map<String, String> authenticatorProperties) {
        return false;
    }

    /**
     * @param token
     * @return
     */
    @Override
    protected String getAuthenticateUser(AuthenticationContext context, Map<String, Object> jsonObject, OAuthClientResponse token) {
        return token.getParam(WindowsLiveOAuth2AuthenticatorConstants.USER_ID);
    }

    @Override
    public List<Property> getConfigurationProperties() {

        List<Property> configProperties = new ArrayList<Property>();

        Property callbackUrl = new Property();
        callbackUrl.setDisplayName("Callback Url");
        callbackUrl.setName(IdentityApplicationConstants.OAuth2.CALLBACK_URL);
        callbackUrl.setDescription("Enter value corresponding to callback url.");
        callbackUrl.setDisplayOrder(3);
        configProperties.add(callbackUrl);

        Property clientId = new Property();
        clientId.setName(OIDCAuthenticatorConstants.CLIENT_ID);
        clientId.setDisplayName("Client Id");
        clientId.setRequired(true);
        clientId.setDescription("Enter Microsoft Live client identifier value");
        clientId.setDisplayOrder(1);
        configProperties.add(clientId);

        Property clientSecret = new Property();
        clientSecret.setName(OIDCAuthenticatorConstants.CLIENT_SECRET);
        clientSecret.setDisplayName("Client Secret");
        clientSecret.setRequired(true);
        clientSecret.setConfidential(true);
        clientSecret.setDescription("Enter Microsoft Live client secret value");
        clientSecret.setDisplayOrder(2);
        configProperties.add(clientSecret);

        return configProperties;
    }

    @Override
    public String getFriendlyName() {
        return WindowsLiveOAuth2AuthenticatorConstants.AUTHENTICATOR_FRIENDLY_NAME;
    }

    @Override
    public String getName() {
        return WindowsLiveOAuth2AuthenticatorConstants.AUTHENTICATOR_NAME;
    }

    @Override
    protected String sendRequest(String url, String accessToken) throws IOException {

        if (!StringUtils.isBlank(url) && !StringUtils.isBlank(accessToken)) {

            String finalUrl = url + accessToken;
            URLConnection urlConnection = new URL(finalUrl).openConnection();
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(urlConnection.getInputStream(), Charset.forName("utf-8")));

            StringBuilder builder = new StringBuilder();
            try {
                String inputLine = reader.readLine();

                while (inputLine != null) {
                    builder.append(inputLine).append("\n");
                    inputLine = reader.readLine();
                }
            }finally {
                IdentityIOStreamUtils.closeReader(reader);
            }
            return builder.toString();
        } else {
            return StringUtils.EMPTY;
        }
    }
}
