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

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.application.common.model.UserDefinedAuthenticatorEndpointConfig;
import org.wso2.carbon.identity.application.common.model.UserDefinedAuthenticatorEndpointConfig.UserDefinedAuthenticatorEndpointConfigBuilder;
import org.wso2.carbon.identity.application.common.model.UserDefinedFederatedAuthenticatorConfig;
import org.wso2.carbon.identity.application.common.model.UserDefinedLocalAuthenticatorConfig;
import org.wso2.carbon.identity.base.AuthenticatorPropertyConstants;

import java.util.HashMap;

import static org.testng.Assert.assertEquals;
import static org.wso2.carbon.identity.base.AuthenticatorPropertyConstants.TAG_2FA;
import static org.wso2.carbon.identity.base.AuthenticatorPropertyConstants.TAG_CUSTOM;

public class UserDefinedAuthenticatorTest {

    private static final String URI = "http://localhost:8080";

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
}
