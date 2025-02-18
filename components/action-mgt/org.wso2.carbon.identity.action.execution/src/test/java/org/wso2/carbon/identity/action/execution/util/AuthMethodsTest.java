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

package org.wso2.carbon.identity.action.execution.util;

import org.apache.http.client.methods.HttpPost;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.action.execution.internal.util.AuthMethods;
import org.wso2.carbon.identity.action.management.api.model.AuthProperty;
import org.wso2.carbon.identity.action.management.api.model.Authentication;

import java.util.Arrays;
import java.util.Base64;
import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.testng.Assert.assertEquals;

public class AuthMethodsTest {

    @Mock
    private HttpPost httpPost;

    @BeforeMethod
    public void setUp() {

        MockitoAnnotations.openMocks(this);
        httpPost = mock(HttpPost.class);
    }

    @Test
    public void testBearerAuth() {

        AuthProperty accessTokenProperty = new AuthProperty.AuthPropertyBuilder()
                .name(Authentication.Property.ACCESS_TOKEN.getName())
                .isConfidential(true)
                .value("testToken")
                .build();

        List<AuthProperty> authProperties = Collections.singletonList(accessTokenProperty);
        AuthMethods.BearerAuth bearerAuth = new AuthMethods.BearerAuth(authProperties);

        assertEquals(Authentication.Type.BEARER.getName(), bearerAuth.getAuthType());

        bearerAuth.applyAuth(httpPost);
        verify(httpPost).setHeader("Authorization", "Bearer testToken");
    }

    @Test
    public void testBasicAuth() {

        AuthProperty usernameProperty = new AuthProperty.AuthPropertyBuilder()
                .name(Authentication.Property.USERNAME.getName())
                .isConfidential(true)
                .value("testUser")
                .build();
        AuthProperty passwordProperty = new AuthProperty.AuthPropertyBuilder()
                .name(Authentication.Property.PASSWORD.getName())
                .isConfidential(true)
                .value("testPass")
                .build();

        List<AuthProperty> authProperties = Arrays.asList(usernameProperty, passwordProperty);
        AuthMethods.BasicAuth basicAuth = new AuthMethods.BasicAuth(authProperties);

        assertEquals(Authentication.Type.BASIC.getName(), basicAuth.getAuthType());

        basicAuth.applyAuth(httpPost);
        String expectedAuthHeader = "Basic " + new String(Base64.getEncoder().encode("testUser:testPass".getBytes()));
        verify(httpPost).setHeader("Authorization", expectedAuthHeader);
    }

    @Test
    public void testAPIKeyAuth() {

        AuthProperty headerProperty = new AuthProperty.AuthPropertyBuilder()
                .name(Authentication.Property.HEADER.getName())
                .isConfidential(false)
                .value("x-api-key")
                .build();
        AuthProperty valueProperty = new AuthProperty.AuthPropertyBuilder()
                .name(Authentication.Property.VALUE.getName())
                .isConfidential(true)
                .value("testApiKey")
                .build();

        List<AuthProperty> authProperties = Arrays.asList(headerProperty, valueProperty);
        AuthMethods.APIKeyAuth apiKeyAuth = new AuthMethods.APIKeyAuth(authProperties);

        assertEquals(Authentication.Type.API_KEY.getName(), apiKeyAuth.getAuthType());

        apiKeyAuth.applyAuth(httpPost);
        verify(httpPost).setHeader("x-api-key", "testApiKey");
    }
}
