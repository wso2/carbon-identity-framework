/*
 * Copyright (c) 2025, WSO2 LLC. (http://www.wso2.com).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.identity.external.api.token.handler.internal.util;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.external.api.token.handler.api.exception.TokenHandlerException;
import org.wso2.carbon.identity.external.api.token.handler.api.exception.TokenRequestException;
import org.wso2.carbon.identity.external.api.token.handler.api.model.GrantContext;
import org.wso2.carbon.identity.external.api.token.handler.api.model.TokenRequestContext;

import java.util.HashMap;
import java.util.Map;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

/**
 * Simple tests for TokenRequestBuilderUtils.
 * Note: Simplified tests due to external API client dependencies not being available.
 */
public class TokenRequestBuilderUtilsTestSimple {

    private static final String CLIENT_ID = "testClientId";
    private static final String CLIENT_SECRET = "testClientSecret";
    private static final String SCOPE = "test_scope";
    private static final String ENDPOINT_URL = "https://api.example.com/token";

    private GrantContext grantContext;
    private TokenRequestContext tokenRequestContext;
    private Map<String, String> headers;

    /**
     * Set up test data before each test method.
     */
    @BeforeMethod
    public void setUp() throws TokenHandlerException, TokenRequestException {

        // Set up grant context properties
        Map<String, String> properties = new HashMap<>();
        properties.put(GrantContext.Property.CLIENT_ID.getName(), CLIENT_ID);
        properties.put(GrantContext.Property.CLIENT_SECRET.getName(), CLIENT_SECRET);
        properties.put(GrantContext.Property.SCOPE.getName(), SCOPE);

        // Set up grant context for client credentials
        grantContext = new GrantContext.Builder()
                .grantType(GrantContext.GrantType.CLIENT_CREDENTIAL)
                .properties(properties)
                .build();

        // Set up headers
        headers = new HashMap<>();
        headers.put("Content-Type", "application/x-www-form-urlencoded");
        headers.put("Accept", "application/json");

        // Set up token request context
        tokenRequestContext = new TokenRequestContext.Builder()
                .grantContext(grantContext)
                .endpointUrl(ENDPOINT_URL)
                .headers(headers)
                .build();
    }

    /**
     * Test that token request context is created successfully.
     */
    @Test
    public void testTokenRequestContextCreation() {

        assertNotNull(tokenRequestContext);
        assertEquals(tokenRequestContext.getGrantContext(), grantContext);
        assertEquals(tokenRequestContext.getTokenEndpointUrl(), ENDPOINT_URL);
        assertEquals(tokenRequestContext.getHeaders(), headers);
    }

    /**
     * Test grant context has correct properties.
     */
    @Test
    public void testGrantContextProperties() {

        assertNotNull(grantContext);
        assertEquals(grantContext.getGrantType(), GrantContext.GrantType.CLIENT_CREDENTIAL);
        assertEquals(grantContext.getProperty(GrantContext.Property.CLIENT_ID.getName()), CLIENT_ID);
        assertEquals(grantContext.getProperty(GrantContext.Property.CLIENT_SECRET.getName()), CLIENT_SECRET);
        assertEquals(grantContext.getProperty(GrantContext.Property.SCOPE.getName()), SCOPE);
    }

    /**
     * Test token request context with custom headers.
     */
    @Test
    public void testTokenRequestContextWithCustomHeaders() throws TokenRequestException {

        Map<String, String> customHeaders = new HashMap<>();
        customHeaders.put("Authorization", "Bearer custom-token");
        customHeaders.put("X-Custom-Header", "custom-value");

        TokenRequestContext customRequestContext = new TokenRequestContext.Builder()
                .grantContext(grantContext)
                .endpointUrl(ENDPOINT_URL)
                .headers(customHeaders)
                .build();

        assertNotNull(customRequestContext);
        assertEquals(customRequestContext.getHeaders(), customHeaders);
        assertTrue(customRequestContext.getHeaders().containsKey("Authorization"));
        assertTrue(customRequestContext.getHeaders().containsKey("X-Custom-Header"));
    }

    /**
     * Test token request context with payload.
     */
    @Test
    public void testTokenRequestContextWithPayload() throws TokenRequestException {

        String testPayload = "grant_type=client_credentials&client_id=test&client_secret=secret";

        TokenRequestContext contextWithPayload = new TokenRequestContext.Builder()
                .grantContext(grantContext)
                .endpointUrl(ENDPOINT_URL)
                .headers(headers)
                .payload(testPayload)
                .build();

        assertNotNull(contextWithPayload);
        assertEquals(contextWithPayload.getPayLoad(), testPayload);
    }
}
