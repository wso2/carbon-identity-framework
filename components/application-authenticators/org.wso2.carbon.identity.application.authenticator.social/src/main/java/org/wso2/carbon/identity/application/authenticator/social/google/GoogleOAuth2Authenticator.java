/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.identity.application.authenticator.social.google;

import org.apache.commons.lang.StringUtils;
import org.apache.oltu.oauth2.client.response.OAuthClientResponse;
import org.wso2.carbon.identity.application.authentication.framework.context.AuthenticationContext;
import org.wso2.carbon.identity.application.authenticator.oidc.OIDCAuthenticatorConstants;
import org.wso2.carbon.identity.application.authenticator.oidc.OpenIDConnectAuthenticator;
import org.wso2.carbon.identity.application.common.model.Property;
import org.wso2.carbon.identity.application.common.util.IdentityApplicationConstants;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class GoogleOAuth2Authenticator extends OpenIDConnectAuthenticator {

    private static final long serialVersionUID = -4154255583070524018L;
    private String tokenEndpoint;
    private String oAuthEndpoint;
    private String userInfoURL;

    /**
     * Initiate tokenEndpoint
     */
    private void initTokenEndpoint() {
        this.tokenEndpoint = getAuthenticatorConfig().getParameterMap().get(GoogleOAuth2AuthenticationConstant
                .GOOGLE_TOKEN_ENDPOINT);
        if (StringUtils.isBlank(this.tokenEndpoint)) {
            this.tokenEndpoint = IdentityApplicationConstants.GOOGLE_TOKEN_URL;
        }
    }

    /**
     * Initiate authorization server endpoint
     */
    private void initOAuthEndpoint() {
        this.oAuthEndpoint = getAuthenticatorConfig().getParameterMap().get(GoogleOAuth2AuthenticationConstant
                .GOOGLE_AUTHZ_ENDPOINT);
        if (StringUtils.isBlank(this.oAuthEndpoint)) {
            this.oAuthEndpoint = IdentityApplicationConstants.GOOGLE_OAUTH_URL;
        }
    }

    /**
     * Initialize the Yahoo user info url.
     */
    private void initUserInfoURL() {

        userInfoURL = getAuthenticatorConfig()
                .getParameterMap()
                .get(GoogleOAuth2AuthenticationConstant.GOOGLE_USERINFO_ENDPOINT);

        if (userInfoURL == null) {
            userInfoURL = IdentityApplicationConstants.GOOGLE_USERINFO_URL;
        }
    }

    /**
     * Get the user info endpoint url.
     * @return User info endpoint url.
     */
    private String getUserInfoURL() {

        if(userInfoURL == null) {
            initUserInfoURL();
        }

        return userInfoURL;
    }

    /**
     * Get Authorization Server Endpoint
     *
     * @param authenticatorProperties this is not used currently in the method
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
     * Get Token Endpoint
     *
     * @param authenticatorProperties this is not used currently in the method
     * @return tokenEndpoint
     */
    @Override
    protected String getTokenEndpoint(Map<String, String> authenticatorProperties) {
        if (StringUtils.isBlank(this.tokenEndpoint)) {
            initTokenEndpoint();
        }
        return this.tokenEndpoint;
    }

    /**
     * Get Scope
     *
     * @param scope
     * @param authenticatorProperties
     * @return
     */
    @Override
    protected String getScope(String scope,
                              Map<String, String> authenticatorProperties) {
        return GoogleOAuth2AuthenticationConstant.GOOGLE_SCOPE;
    }


    @Override
    protected String getAuthenticateUser(AuthenticationContext context, Map<String, Object> jsonObject, OAuthClientResponse token) {
        if (jsonObject.get(OIDCAuthenticatorConstants.Claim.EMAIL) == null) {
            return (String) jsonObject.get("sub");
        } else {
            return (String) jsonObject.get(OIDCAuthenticatorConstants.Claim.EMAIL);
        }
    }

    /**
     * Get google user info endpoint.
     * @param token OAuth client response.
     * @return User info endpoint.
     */
    @Override
    protected String getUserInfoEndpoint(OAuthClientResponse token, Map<String, String> authenticatorProperties) {
        return getUserInfoURL();
    }

    @Override
    protected String getQueryString(Map<String, String> authenticatorProperties) {
        return authenticatorProperties.get(GoogleOAuth2AuthenticationConstant.ADDITIONAL_QUERY_PARAMS);
    }

    /**
     * Get Configuration Properties
     *
     * @return
     */
    @Override
    public List<Property> getConfigurationProperties() {

        List<Property> configProperties = new ArrayList<Property>();

        Property clientId = new Property();
        clientId.setName(OIDCAuthenticatorConstants.CLIENT_ID);
        clientId.setDisplayName("Client Id");
        clientId.setRequired(true);
        clientId.setDescription("Enter Google IDP client identifier value");
        clientId.setDisplayOrder(1);
        configProperties.add(clientId);

        Property clientSecret = new Property();
        clientSecret.setName(OIDCAuthenticatorConstants.CLIENT_SECRET);
        clientSecret.setDisplayName("Client Secret");
        clientSecret.setRequired(true);
        clientSecret.setConfidential(true);
        clientSecret.setDescription("Enter Google IDP client secret value");
        clientSecret.setDisplayOrder(2);
        configProperties.add(clientSecret);

        Property callbackUrl = new Property();
        callbackUrl.setDisplayName("Callback Url");
        callbackUrl.setName(IdentityApplicationConstants.OAuth2.CALLBACK_URL);
        callbackUrl.setDescription("Enter value corresponding to callback url.");
        callbackUrl.setDisplayOrder(3);
        configProperties.add(callbackUrl);

        Property scope = new Property();
        scope.setDisplayName("Additional Query Parameters");
        scope.setName("AdditionalQueryParameters");
        scope.setValue("scope=openid email profile");
        scope.setDescription("Additional query parameters. e.g: paramName1=value1");
        scope.setDisplayOrder(4);
        configProperties.add(scope);

        return configProperties;
    }

    /**
     * Get Friendly Name
     *
     * @return
     */
    @Override
    public String getFriendlyName() {
        return GoogleOAuth2AuthenticationConstant.GOOGLE_CONNECTOR_FRIENDLY_NAME;
    }

    /**
     * GetName
     *
     * @return
     */
    @Override
    public String getName() {
        return GoogleOAuth2AuthenticationConstant.GOOGLE_CONNECTOR_NAME;
    }
}
