/*
 * Copyright (c) 2025, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.carbon.identity.external.api.token.handler.internal.util;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.external.api.client.api.model.APIAuthentication;
import org.wso2.carbon.identity.external.api.client.api.model.APIRequestContext;
import org.wso2.carbon.identity.external.api.token.handler.api.model.GrantContext;
import org.wso2.carbon.identity.external.api.token.handler.api.model.TokenRequestContext;

import java.util.HashMap;
import java.util.Map;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.fail;

/**
 * Unit tests for TokenRequestBuilderUtils class.
 */
public class TokenRequestBuilderUtilsTest {

    private static final String TEST_CLIENT_ID = "test-client-id";
    private static final String TEST_CLIENT_SECRET = "test-client-secret";
    private static final String TEST_SCOPE = "test-scope";
    private static final String TEST_TOKEN_ENDPOINT = "https://localhost:9443/oauth2/token";
    private static final String TEST_REFRESH_TOKEN = "test-refresh-token";
    private static final String HEADER_KEY = "Content-Type";
    private static final String HEADER_VALUE = "application/json";

    private GrantContext clientCredentialGrantContext;
    private Map<String, String> headers;

    /**
     * Set up test data before each test method.
     */
    @BeforeMethod
    public void setUp() throws Exception {

        Map<String, String> properties = new HashMap<>();
        properties.put(GrantContext.Property.CLIENT_ID.getName(), TEST_CLIENT_ID);
        properties.put(GrantContext.Property.CLIENT_SECRET.getName(), TEST_CLIENT_SECRET);
        properties.put(GrantContext.Property.SCOPE.getName(), TEST_SCOPE);

        clientCredentialGrantContext = new GrantContext.Builder()
                .grantType(GrantContext.GrantType.CLIENT_CREDENTIAL)
                .properties(properties)
                .build();

        headers = new HashMap<>();
        headers.put(HEADER_KEY, HEADER_VALUE);
    }

    /**
     * Test building APIRequestContext for client credentials grant type.
     */
    @Test
    public void testBuildAPIRequestContextForClientCredentials() throws Exception {

        TokenRequestContext requestContext = new TokenRequestContext.Builder()
                .grantContext(clientCredentialGrantContext)
                .endpointUrl(TEST_TOKEN_ENDPOINT)
                .headers(headers)
                .build();

        APIRequestContext apiRequestContext = TokenRequestBuilderUtils.buildAPIRequestContext(requestContext);

        assertNotNull(apiRequestContext, "APIRequestContext should not be null.");
        assertEquals(apiRequestContext.getHttpMethod(), APIRequestContext.HttpMethod.POST,
                "HTTP method should be POST.");
        assertEquals(apiRequestContext.getEndpointUrl(), TEST_TOKEN_ENDPOINT,
                "Token endpoint URL should match.");
        assertEquals(apiRequestContext.getHeaders(), headers, "Headers should match.");

        // Verify authentication type.
        APIAuthentication authentication = apiRequestContext.getApiAuthentication();
        assertNotNull(authentication, "APIAuthentication should not be null.");
        assertEquals(authentication.getType(), APIAuthentication.AuthType.BASIC,
                "Authentication type should be BASIC for client credentials.");
        assertEquals(authentication.getProperty(APIAuthentication.Property.USERNAME).getValue(), TEST_CLIENT_ID,
                "Client ID should match.");
        assertEquals(authentication.getProperty(APIAuthentication.Property.PASSWORD).getValue(), TEST_CLIENT_SECRET,
                "Client secret should match.");

        // Verify payload.
        String payload = apiRequestContext.getPayload();
        assertNotNull(payload, "Payload should not be null.");

        JsonObject payloadJson = JsonParser.parseString(payload).getAsJsonObject();
        assertEquals(payloadJson.get("grant_type").getAsString(), "client_credentials",
                "Grant type in payload should be client_credentials.");
        assertEquals(payloadJson.get("client_id").getAsString(), TEST_CLIENT_ID,
                "Client ID in payload should match.");
        assertEquals(payloadJson.get("client_secret").getAsString(), TEST_CLIENT_SECRET,
                "Client secret in payload should match.");
        assertEquals(payloadJson.get("scope").getAsString(), TEST_SCOPE,
                "Scope in payload should match.");
    }

    /**
     * Test building APIRequestContext for refresh token grant type.
     */
    @Test
    public void testBuildAPIRequestContextForRefreshGrant() throws Exception {

        TokenRequestContext requestContext = new TokenRequestContext.Builder()
                .grantContext(clientCredentialGrantContext)
                .endpointUrl(TEST_TOKEN_ENDPOINT)
                .headers(headers)
                .build();

        APIRequestContext apiRequestContext = TokenRequestBuilderUtils
                .buildAPIRequestContextForRefreshGrant(requestContext, TEST_REFRESH_TOKEN);

        assertNotNull(apiRequestContext, "APIRequestContext should not be null.");
        assertEquals(apiRequestContext.getHttpMethod(), APIRequestContext.HttpMethod.POST,
                "HTTP method should be POST.");
        assertEquals(apiRequestContext.getEndpointUrl(), TEST_TOKEN_ENDPOINT,
                "Token endpoint URL should match.");
        assertEquals(apiRequestContext.getHeaders(), headers, "Headers should match.");

        // Verify authentication type.
        APIAuthentication authentication = apiRequestContext.getApiAuthentication();
        assertNotNull(authentication, "APIAuthentication should not be null.");
        assertEquals(authentication.getType(), APIAuthentication.AuthType.BASIC,
                "Authentication type should be BASIC.");
        assertEquals(authentication.getProperty(APIAuthentication.Property.USERNAME).getValue(), TEST_CLIENT_ID,
                "Client ID should match.");
        assertEquals(authentication.getProperty(APIAuthentication.Property.PASSWORD).getValue(), TEST_CLIENT_SECRET,
                "Client secret should match.");

        // Verify payload.
        String payload = apiRequestContext.getPayload();
        assertNotNull(payload, "Payload should not be null.");

        JsonObject payloadJson = JsonParser.parseString(payload).getAsJsonObject();
        assertEquals(payloadJson.get("grant_type").getAsString(), "refresh_token",
                "Grant type in payload should be refresh_token.");
        assertEquals(payloadJson.get("refresh_token").getAsString(), TEST_REFRESH_TOKEN,
                "Refresh token in payload should match.");
    }

    /**
     * Test building APIRequestContext with null grant context should throw exception.
     */
    @Test
    public void testBuildAPIRequestContextWithNullGrantContext() {

        try {
            TokenRequestContext requestContext = new TokenRequestContext.Builder()
                    .grantContext(null)
                    .endpointUrl(TEST_TOKEN_ENDPOINT)
                    .headers(headers)
                    .build();

            TokenRequestBuilderUtils.buildAPIRequestContext(requestContext);
            fail("Expected TokenRequestException was not thrown.");
        } catch (Exception e) {
            // Expected exception.
        }
    }

    /**
     * Test building APIRequestContext with null endpoint URL should throw exception.
     */
    @Test
    public void testBuildAPIRequestContextWithNullEndpointUrl() {

        try {
            TokenRequestContext requestContext = new TokenRequestContext.Builder()
                    .grantContext(clientCredentialGrantContext)
                    .endpointUrl(null)
                    .headers(headers)
                    .build();

            TokenRequestBuilderUtils.buildAPIRequestContext(requestContext);
            fail("Expected exception was not thrown.");
        } catch (Exception e) {
            // Expected exception.
        }
    }

    /**
     * Test building APIRequestContext with null headers should throw exception.
     */
    @Test
    public void testBuildAPIRequestContextWithNullHeaders() {

        try {
            TokenRequestContext requestContext = new TokenRequestContext.Builder()
                    .grantContext(clientCredentialGrantContext)
                    .endpointUrl(TEST_TOKEN_ENDPOINT)
                    .headers(null)
                    .build();

            TokenRequestBuilderUtils.buildAPIRequestContext(requestContext);
            fail("Expected exception for null headers was not thrown.");
        } catch (Exception e) {
            // Expected exception.
        }
    }

    /**
     * Test building APIRequestContext with multiple headers.
     */
    @Test
    public void testBuildAPIRequestContextWithMultipleHeaders() throws Exception {

        Map<String, String> multipleHeaders = new HashMap<>();
        multipleHeaders.put("Content-Type", "application/json");
        multipleHeaders.put("Accept", "application/json");
        multipleHeaders.put("X-Custom-Header", "custom-value");

        TokenRequestContext requestContext = new TokenRequestContext.Builder()
                .grantContext(clientCredentialGrantContext)
                .endpointUrl(TEST_TOKEN_ENDPOINT)
                .headers(multipleHeaders)
                .build();

        APIRequestContext apiRequestContext = TokenRequestBuilderUtils.buildAPIRequestContext(requestContext);

        assertNotNull(apiRequestContext, "APIRequestContext should not be null.");
        assertEquals(apiRequestContext.getHeaders().size(), 3, "Should have 3 headers.");
        assertEquals(apiRequestContext.getHeaders().get("Content-Type"), "application/json");
        assertEquals(apiRequestContext.getHeaders().get("Accept"), "application/json");
        assertEquals(apiRequestContext.getHeaders().get("X-Custom-Header"), "custom-value");
    }

    /**
     * Test building APIRequestContext with special characters in credentials.
     */
    @Test
    public void testBuildAPIRequestContextWithSpecialCharactersInCredentials() throws Exception {

        Map<String, String> properties = new HashMap<>();
        properties.put(GrantContext.Property.CLIENT_ID.getName(), "client@domain.com");
        properties.put(GrantContext.Property.CLIENT_SECRET.getName(), "p@ssw0rd!#$%^&*()");
        properties.put(GrantContext.Property.SCOPE.getName(), "scope:read scope:write");

        GrantContext grantContext = new GrantContext.Builder()
                .grantType(GrantContext.GrantType.CLIENT_CREDENTIAL)
                .properties(properties)
                .build();

        TokenRequestContext requestContext = new TokenRequestContext.Builder()
                .grantContext(grantContext)
                .endpointUrl(TEST_TOKEN_ENDPOINT)
                .headers(headers)
                .build();

        APIRequestContext apiRequestContext = TokenRequestBuilderUtils.buildAPIRequestContext(requestContext);

        assertNotNull(apiRequestContext, "APIRequestContext should not be null.");

        APIAuthentication authentication = apiRequestContext.getApiAuthentication();
        assertEquals(authentication.getProperty(APIAuthentication.Property.USERNAME).getValue(), "client@domain.com");
        assertEquals(authentication.getProperty(APIAuthentication.Property.PASSWORD).getValue(), "p@ssw0rd!#$%^&*()");

        String payload = apiRequestContext.getPayload();
        JsonObject payloadJson = JsonParser.parseString(payload).getAsJsonObject();
        assertEquals(payloadJson.get("client_id").getAsString(), "client@domain.com");
        assertEquals(payloadJson.get("client_secret").getAsString(), "p@ssw0rd!#$%^&*()");
        assertEquals(payloadJson.get("scope").getAsString(), "scope:read scope:write");
    }
}
