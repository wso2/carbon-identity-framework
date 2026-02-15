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

package org.wso2.carbon.identity.external.api.token.handler.internal.util;

import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.util.EntityUtils;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.external.api.client.api.model.APIAuthentication;
import org.wso2.carbon.identity.external.api.client.api.model.APIRequestContext;
import org.wso2.carbon.identity.external.api.token.handler.api.model.GrantContext;
import org.wso2.carbon.identity.external.api.token.handler.api.model.TokenRequestContext;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
    private static final String CONTENT_TYPE_HEADER_KEY = "Content-Type";
    private static final String CONTENT_TYPE_HEADER_VALUE = "application/x-www-form-urlencoded";
    private static final String ACCEPT_HEADER_KEY = "Accept";
    private static final String ACCEPT_HEADER_VALUE = "application/json";

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
        headers.put(CONTENT_TYPE_HEADER_KEY, CONTENT_TYPE_HEADER_VALUE);
        headers.put(ACCEPT_HEADER_KEY, ACCEPT_HEADER_VALUE);
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
        Map<String, String> expectedParams = new HashMap<>();
        expectedParams.put("grant_type", "client_credentials");
        expectedParams.put("scope", TEST_SCOPE);
        validatePayload(apiRequestContext.getPayload(), expectedParams);
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

        Map<String, String> expectedParams = new HashMap<>();
        expectedParams.put("grant_type", "refresh_token");
        expectedParams.put("refresh_token", TEST_REFRESH_TOKEN);
        validatePayload(apiRequestContext.getPayload(), expectedParams);
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
     * Test building APIRequestContext with null headers should handle gracefully.
     * Null headers are converted to empty map by the builder.
     */
    @Test
    public void testBuildAPIRequestContextWithNullHeaders() throws Exception {

        TokenRequestContext requestContext = new TokenRequestContext.Builder()
                .grantContext(clientCredentialGrantContext)
                .endpointUrl(TEST_TOKEN_ENDPOINT)
                .headers(null)
                .build();

        APIRequestContext apiRequestContext = TokenRequestBuilderUtils.buildAPIRequestContext(requestContext);
        
        assertNotNull(apiRequestContext);
        assertNotNull(apiRequestContext.getHeaders());
    }

    /**
     * Test building APIRequestContext with multiple headers.
     */
    @Test
    public void testBuildAPIRequestContextWithMultipleHeaders() throws Exception {

        Map<String, String> multipleHeaders = new HashMap<>();
                multipleHeaders.put("Content-Type", "application/x-www-form-urlencoded");
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
                assertEquals(apiRequestContext.getHeaders().get("Content-Type"), "application/x-www-form-urlencoded");
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

        Map<String, String> expectedParams = new HashMap<>();
        expectedParams.put("grant_type", "client_credentials");
        expectedParams.put("scope", "scope:read scope:write");
        validatePayload(apiRequestContext.getPayload(), expectedParams);
    }

    private void validatePayload(HttpEntity payload, Map<String, String> expectedParams) throws Exception {

        assertNotNull(payload, "Payload should not be null.");
        String payloadString = EntityUtils.toString(payload, StandardCharsets.UTF_8);
        List<NameValuePair> params = URLEncodedUtils.parse(payloadString, StandardCharsets.UTF_8);
        Map<String, String> payloadMap = params.stream()
                .collect(Collectors.toMap(NameValuePair::getName, NameValuePair::getValue));

        assertEquals(payloadMap.size(), expectedParams.size());
        for (Map.Entry<String, String> entry : expectedParams.entrySet()) {
            assertEquals(payloadMap.get(entry.getKey()), entry.getValue(),
                    "Payload parameter " + entry.getKey() + " should match.");
        }
    }
}
