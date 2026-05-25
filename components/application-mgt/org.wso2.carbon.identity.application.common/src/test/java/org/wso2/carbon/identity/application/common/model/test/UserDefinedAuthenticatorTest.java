/*
 * Copyright (c) 2024, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.carbon.identity.application.common.model.test;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.action.management.internal.component.ActionMgtServiceComponentHolder;
import org.wso2.carbon.identity.application.common.model.UserDefinedAuthenticatorEndpointConfig;
import org.wso2.carbon.identity.application.common.model.UserDefinedAuthenticatorEndpointConfig.UserDefinedAuthenticatorEndpointConfigBuilder;
import org.wso2.carbon.identity.application.common.model.UserDefinedFederatedAuthenticatorConfig;
import org.wso2.carbon.identity.application.common.model.UserDefinedLocalAuthenticatorConfig;
import org.wso2.carbon.identity.base.AuthenticatorPropertyConstants;
import org.wso2.carbon.identity.secret.mgt.core.SecretManager;
import org.wso2.carbon.identity.secret.mgt.core.SecretManagerImpl;
import org.wso2.carbon.identity.secret.mgt.core.SecretResolveManager;
import org.wso2.carbon.identity.secret.mgt.core.exception.SecretManagementException;
import org.wso2.carbon.identity.secret.mgt.core.model.ResolvedSecret;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.wso2.carbon.identity.base.AuthenticatorPropertyConstants.TAG_2FA;
import static org.wso2.carbon.identity.base.AuthenticatorPropertyConstants.TAG_CUSTOM;

public class UserDefinedAuthenticatorTest {

    private static final String URI = "http://localhost:8080";
    private static final String ACTION_ID = "action-id";
    private static final String SECRET_TYPE = "ACTION_API_ENDPOINT_AUTH_SECRETS";

    private SecretResolveManager secretResolveManager;
    private SecretManager secretManager;

    @BeforeMethod
    public void setUp() throws SecretManagementException {

        secretResolveManager = mock(SecretResolveManager.class);
        secretManager = mock(SecretManagerImpl.class);
        // decryptPropertyBySecretReference checks isSecretExist before resolving; report present so
        // the resolve path is reached.
        doReturn(true).when(secretManager).isSecretExist(any(), any());
        ActionMgtServiceComponentHolder.getInstance().setSecretResolveManager(secretResolveManager);
        ActionMgtServiceComponentHolder.getInstance().setSecretManager(secretManager);
    }

    @Test
    public void createUserDefinedLocalVerificationAuthenticator() {

        UserDefinedLocalAuthenticatorConfig config =
                new UserDefinedLocalAuthenticatorConfig(AuthenticatorPropertyConstants.AuthenticationType.VERIFICATION);
        assertEquals(config.getTags(), new String[]{TAG_CUSTOM, TAG_2FA});
        assertEquals(config.getDefinedByType(), AuthenticatorPropertyConstants.DefinedByType.USER);
    }

    @Test
    public void createUserDefinedLocalIdentificationAuthenticator() {

        UserDefinedLocalAuthenticatorConfig config =
                new UserDefinedLocalAuthenticatorConfig(
                        AuthenticatorPropertyConstants.AuthenticationType.IDENTIFICATION);
        assertEquals(config.getTags(), new String[]{TAG_CUSTOM});
        assertEquals(config.getDefinedByType(), AuthenticatorPropertyConstants.DefinedByType.USER);
    }

    @Test
    public void createUserDefinedFederatedAuthenticator() {

        UserDefinedFederatedAuthenticatorConfig config = new UserDefinedFederatedAuthenticatorConfig();
        assertEquals(config.getTags(), new String[]{TAG_CUSTOM});
        assertEquals(config.getDefinedByType(), AuthenticatorPropertyConstants.DefinedByType.USER);
    }

    @DataProvider(name = "endpointConfig")
    public Object[][] endpointConfig() {
        return new Object[][] {
            {URI, "BASIC", new HashMap<String, String>() {{
                put("username", "value1");
                put("password", "value2");
            }}},
            {URI, "BEARER", new HashMap<String, String>() {{
                put("accessToken", "value1");
            }}}
        };
    }

    @Test(dataProvider = "endpointConfig")
    public void createEndpointConfigurationTest(String uri, String authenticationType, HashMap<String,
            String> endpointConfig) {

        UserDefinedAuthenticatorEndpointConfigBuilder endpointConfigBuilder =
                new UserDefinedAuthenticatorEndpointConfigBuilder();
        endpointConfigBuilder.uri(uri);
        endpointConfigBuilder.authenticationType(authenticationType);
        endpointConfigBuilder.authenticationProperties(endpointConfig);
        UserDefinedAuthenticatorEndpointConfig authEndpointConfig = endpointConfigBuilder.build();

        assertEquals(authEndpointConfig.getAuthenticatorEndpointUri(), uri);
        assertEquals(authEndpointConfig.getAuthenticatorEndpointAuthenticationType(), authenticationType);
        assertEquals(authEndpointConfig.getAuthenticatorEndpointAuthenticationProperties(), endpointConfig);
    }

    @DataProvider(name = "invalidEndpointConfig")
    public Object[][] invalidEndpointConfig() {
        return new Object[][] {
                {URI, "INVALID", new HashMap<String, String>() {{
                    put("username", "value1");
                    put("password", "value2");
                }}},
                {URI, "BEARER", new HashMap<String, String>() {{
                    put("invalidProp", "value1");
                }}},
                {URI, "BASIC", new HashMap<String, String>() {{
                    put("username", "value1");
                }}}
        };
    }

    @Test(dataProvider = "invalidEndpointConfig", expectedExceptions = RuntimeException.class)
    public void invalidEndpointConfigurationTest(String uri, String authenticationType, HashMap<String,
            String> endpointConfig) {

        UserDefinedAuthenticatorEndpointConfigBuilder endpointConfigBuilder =
                new UserDefinedAuthenticatorEndpointConfigBuilder();
        endpointConfigBuilder.uri(uri);
        endpointConfigBuilder.authenticationType(authenticationType);
        endpointConfigBuilder.authenticationProperties(endpointConfig);
        endpointConfigBuilder.build();
    }

    @Test
    public void resolvedEndpointAuthenticationPropertiesForBasic() throws SecretManagementException {

        stubResolvedSecret(secretName("BASIC", "username"), "alice");

        Map<String, String> input = new HashMap<>();
        input.put("username", secretReference("BASIC", "username"));
        input.put("password", secretReference("BASIC", "password"));

        Map<String, Object> resolved = buildEndpointConfig("BASIC", input)
                .getResolvedEndpointAuthenticationProperties();

        assertEquals(resolved.size(), 1);
        assertEquals(resolved.get("username"), "alice");
        assertFalse(resolved.containsKey("password"),
                "BASIC resolved view must not expose the password secret reference.");
    }

    @Test
    public void resolvedEndpointAuthenticationPropertiesForApiKey() {

        Map<String, String> input = new HashMap<>();
        input.put("header", "Authorization");
        input.put("value", secretReference("API_KEY", "value"));

        Map<String, Object> resolved = buildEndpointConfig("API_KEY", input)
                .getResolvedEndpointAuthenticationProperties();

        assertEquals(resolved.size(), 1);
        assertEquals(resolved.get("header"), "Authorization");
        assertFalse(resolved.containsKey("value"),
                "API_KEY resolved view must not expose the api key value secret reference.");
    }

    @Test
    public void resolvedEndpointAuthenticationPropertiesForClientCredential() throws SecretManagementException {

        stubResolvedSecret(secretName("CLIENT_CREDENTIAL", "clientId"), "client-123");

        Map<String, String> input = new HashMap<>();
        input.put("clientId", secretReference("CLIENT_CREDENTIAL", "clientId"));
        input.put("clientSecret", secretReference("CLIENT_CREDENTIAL", "clientSecret"));
        input.put("tokenEndpoint", "https://token.example.com");
        input.put("scopes", "openid profile");

        Map<String, Object> resolved = buildEndpointConfig("CLIENT_CREDENTIAL", input)
                .getResolvedEndpointAuthenticationProperties();

        assertEquals(resolved.size(), 3);
        assertEquals(resolved.get("clientId"), "client-123");
        assertEquals(resolved.get("tokenEndpoint"), "https://token.example.com");
        assertEquals(resolved.get("scopes"), "openid profile");
        assertFalse(resolved.containsKey("clientSecret"),
                "CLIENT_CREDENTIAL resolved view must not expose the client secret reference.");
    }

    @Test
    public void resolvedEndpointAuthenticationPropertiesForPasswordCredential() throws SecretManagementException {

        stubResolvedSecret(secretName("PASSWORD_CREDENTIAL", "clientId"), "pwd-client");
        stubResolvedSecret(secretName("PASSWORD_CREDENTIAL", "username"), "bob");

        Map<String, String> input = new HashMap<>();
        input.put("clientId", secretReference("PASSWORD_CREDENTIAL", "clientId"));
        input.put("clientSecret", secretReference("PASSWORD_CREDENTIAL", "clientSecret"));
        input.put("tokenEndpoint", "https://token.example.com");
        input.put("username", secretReference("PASSWORD_CREDENTIAL", "username"));
        input.put("password", secretReference("PASSWORD_CREDENTIAL", "password"));

        Map<String, Object> resolved = buildEndpointConfig("PASSWORD_CREDENTIAL", input)
                .getResolvedEndpointAuthenticationProperties();

        assertEquals(resolved.size(), 3);
        assertEquals(resolved.get("clientId"), "pwd-client");
        assertEquals(resolved.get("username"), "bob");
        assertEquals(resolved.get("tokenEndpoint"), "https://token.example.com");
        assertFalse(resolved.containsKey("clientSecret"),
                "PASSWORD_CREDENTIAL resolved view must not expose the client secret reference.");
        assertFalse(resolved.containsKey("password"),
                "PASSWORD_CREDENTIAL resolved view must not expose the password secret reference.");
        assertFalse(resolved.containsKey("scopes"), "Optional scopes must be absent when not configured.");
    }

    private UserDefinedAuthenticatorEndpointConfig buildEndpointConfig(String authenticationType,
                                                                       Map<String, String> properties) {

        return new UserDefinedAuthenticatorEndpointConfigBuilder()
                .uri(URI)
                .authenticationType(authenticationType)
                .authenticationProperties(properties)
                .build();
    }

    private void stubResolvedSecret(String secretName, String resolvedValue) throws SecretManagementException {

        ResolvedSecret resolvedSecret = new ResolvedSecret();
        resolvedSecret.setResolvedSecretValue(resolvedValue);
        doReturn(resolvedSecret).when(secretResolveManager).getResolvedSecret(any(), eq(secretName));
    }

    private static String secretReference(String authType, String propertyName) {

        return SECRET_TYPE + ":" + secretName(authType, propertyName);
    }

    private static String secretName(String authType, String propertyName) {

        return ACTION_ID + ":" + authType + ":" + propertyName;
    }
}
