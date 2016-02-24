/*
 *  Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package org.wso2.carbon.identity.application.authenticator.social.yahoo;

/**
 * Common string constants related to the Yahoo authenticator.
 */
public class YahooOAuth2AuthenticatorConstants {

    private YahooOAuth2AuthenticatorConstants () {
    }

    public static final String YAHOO_OAUTHZ_ENDPOINT = "YahooOAuthzEndpoint";
    public static final String YAHOO_TOKEN_ENDPOINT = "YahooTokenEndpoint";
    public static final String YAHOO_USERINFO_ENDPOINT = "YahooUserInfoEndpoint";
    public static final String YAHOO_CONNECTOR_FRIENDLY_NAME = "Yahoo";
    public static final String YAHOO_CONNECTOR_NAME = "YahooOAuth2Authenticator";
    public static final String YAHOO_SCOPE = "";
    public static final String USER_GUID = "xoauth_yahoo_guid";
    public static final String CALLBACK_URL = "Yahoo-callback-url";
    public static final String YAHOO_USER_DETAILS_JSON = "/profile?format=json";
}
