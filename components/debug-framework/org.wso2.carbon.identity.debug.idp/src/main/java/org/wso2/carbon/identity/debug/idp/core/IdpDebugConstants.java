/**
 * Copyright (c) 2026, WSO2 LLC. (https://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.identity.debug.idp.core;

/**
 * Constants for the IdP Debug Adapter.
 */
public final class IdpDebugConstants {

    public static final String CONNECTION_ID = "connectionId";
    public static final String RESOURCE_TYPE_KEY = "resourceType";
    public static final String RESOURCE_TYPE_IDP = "IDP";

    // Known implementation names used for protocol detection.
    public static final String IMPLEMENTATION_OPENID_CONNECT = "OpenIDConnectAuthenticator";
    public static final String IMPLEMENTATION_GOOGLE_OIDC = "GoogleOIDCAuthenticator";
    public static final String IMPLEMENTATION_FACEBOOK = "FacebookAuthenticator";
    public static final String IMPLEMENTATION_GITHUB = "GitHubAuthenticator";
    public static final String IMPLEMENTATION_SAML_SSO = "SAMLSSOAuthenticator";

    // Canonical protocol type keys.
    public static final String PROTOCOL_TYPE_OIDC = "OIDC";
    public static final String PROTOCOL_TYPE_GOOGLE = "Google";
    public static final String PROTOCOL_TYPE_FACEBOOK = "Facebook";
    public static final String PROTOCOL_TYPE_GITHUB = "GitHub";
    public static final String PROTOCOL_TYPE_SAML = "SAML";

    private IdpDebugConstants() {
        
    }
}
