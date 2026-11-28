/*
 * Copyright (c) 2026, WSO2 LLC. (http://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.identity.application.mgt.listener;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.application.common.model.InboundAuthenticationRequestConfig;
import org.wso2.carbon.identity.central.log.mgt.utils.LoggerUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

/**
 * Unit tests for {@link ApplicationMgtAuditLogger}.
 */
public class ApplicationMgtAuditLoggerTest {

    private final ApplicationMgtAuditLogger auditLogger = new ApplicationMgtAuditLogger();
    private boolean originalLogMaskingEnable;

    @BeforeMethod
    public void setUp() {

        originalLogMaskingEnable = LoggerUtils.isLogMaskingEnable;
        LoggerUtils.isLogMaskingEnable = false;
    }

    @AfterMethod
    public void tearDown() {

        LoggerUtils.isLogMaskingEnable = originalLogMaskingEnable;
    }

    @Test
    public void testOAuthInboundConfigurationIsLogged() {

        String inboundConfiguration = auditLogger.buildInboundConfiguration(
                buildRequestConfig("oauth2", null, buildOAuthData()));

        assertEquals(inboundConfiguration, "{Grant Types:[client_credentials, authorization_code, implicit], " +
                "Callback URL:https://localhost:3000/callback, Allowed Origins:[https://localhost:3000], " +
                "Token Type:JWT, User Access Token Expiry:3600, Application Access Token Expiry:3600, " +
                "Refresh Token Expiry:86400, ID Token Expiry:3600, Renew Refresh Token:false, PKCE Mandatory:true, " +
                "PKCE Support Plain:false, Public Client:false, State:ACTIVE, Audiences:[aud1, aud2], " +
                "Subject Type:public, Hybrid Flow Enabled:false, Request Object Signature Validation:false, " +
                "Pushed Authorization Requests Required:false}");
        assertFalse(inboundConfiguration.contains("secret-value"), "Client secret must not be logged.");
    }

    @Test
    public void testOAuthInboundConfigurationWithoutData() {

        assertNull(auditLogger.buildInboundConfiguration(buildRequestConfig("oauth2", null, null)));
    }

    @Test
    public void testOAuthInboundConfigurationWithOnlyAllowedOrigins() {

        // OAuthAdminServiceImpl#buildSPData returns an empty map if serialising the OAuthAppDO fails, and only
        // "allowedOrigins" is added to it afterwards.
        Map<String, Object> data = new HashMap<>();
        data.put("allowedOrigins", Collections.singletonList("https://localhost:3000"));

        assertEquals(auditLogger.buildInboundConfiguration(buildRequestConfig("oauth2", null, data)),
                "{Grant Types:null, Callback URL:null, Allowed Origins:[https://localhost:3000], Token Type:null, " +
                        "User Access Token Expiry:null, Application Access Token Expiry:null, " +
                        "Refresh Token Expiry:null, ID Token Expiry:null, Renew Refresh Token:null, " +
                        "PKCE Mandatory:null, PKCE Support Plain:null, Public Client:null, State:null, " +
                        "Audiences:null, Subject Type:null, Hybrid Flow Enabled:null, " +
                        "Request Object Signature Validation:null, Pushed Authorization Requests Required:null}");
    }

    @Test
    public void testOAuthInboundConfigurationWithNullGrantTypes() {

        Map<String, Object> data = buildOAuthData();
        data.put("grantTypes", null);

        assertTrue(auditLogger.buildInboundConfiguration(buildRequestConfig("oauth2", null, data))
                .startsWith("{Grant Types:null, "));
    }

    @Test
    public void testOAuthInboundConfigurationWithEmptyGrantTypes() {

        Map<String, Object> data = buildOAuthData();
        data.put("grantTypes", "");

        assertTrue(auditLogger.buildInboundConfiguration(buildRequestConfig("oauth2", null, data))
                .startsWith("{Grant Types:[], "));
    }

    @Test
    public void testOAuthInboundConfigurationDoesNotModifyRequestConfigData() {

        Map<String, Object> data = buildOAuthData();
        auditLogger.buildInboundConfiguration(buildRequestConfig("oauth2", null, data));

        assertEquals(data.get("grantTypes"), "client_credentials authorization_code implicit");
    }

    @Test
    public void testLegacyInboundConfigurationTakesPrecedenceForOAuth() {

        assertEquals(auditLogger.buildInboundConfiguration(
                buildRequestConfig("oauth2", "<config/>", buildOAuthData())), "<config/>");
    }

    @Test
    public void testNonOAuthInboundConfigurationIsUnchanged() {

        assertEquals(auditLogger.buildInboundConfiguration(
                buildRequestConfig("samlsso", "<config/>", buildOAuthData())), "<config/>");
    }

    /**
     * Mimic the data map built by OAuthAdminServiceImpl#buildSPData (OAuthAppDO serialised by Jackson and parsed
     * back by Gson) with the allowed origins added.
     */
    private static Map<String, Object> buildOAuthData() {

        Map<String, Object> data = new HashMap<>();
        data.put("grantTypes", "client_credentials authorization_code implicit");
        data.put("callbackUrl", "https://localhost:3000/callback");
        data.put("allowedOrigins", Collections.singletonList("https://localhost:3000"));
        data.put("tokenType", "JWT");
        data.put("userAccessTokenExpiryTime", 3600.0);
        data.put("applicationAccessTokenExpiryTime", 3600.0);
        data.put("refreshTokenExpiryTime", 86400.0);
        data.put("idTokenExpiryTime", 3600.0);
        data.put("renewRefreshTokenEnabled", "false");
        data.put("pkceMandatory", true);
        data.put("pkceSupportPlain", false);
        data.put("bypassClientCredentials", false);
        data.put("state", "ACTIVE");
        data.put("audiences", new ArrayList<>(Arrays.asList("aud1", "aud2")));
        data.put("subjectType", "public");
        data.put("hybridFlowEnabled", false);
        data.put("requestObjectSignatureValidationEnabled", false);
        data.put("requirePushedAuthorizationRequests", false);
        data.put("oauthConsumerSecret", "secret-value");
        return data;
    }

    private static InboundAuthenticationRequestConfig buildRequestConfig(String authType, String inboundConfiguration,
                                                                         Map<String, Object> data) {

        InboundAuthenticationRequestConfig requestConfig = new InboundAuthenticationRequestConfig();
        requestConfig.setInboundAuthKey("clientId");
        requestConfig.setInboundAuthType(authType);
        requestConfig.setInboundConfiguration(inboundConfiguration);
        requestConfig.setData(data);
        return requestConfig;
    }
}
