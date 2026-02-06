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

package org.wso2.carbon.identity.external.api.token.handler.api.model;

import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.message.BasicNameValuePair;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.external.api.token.handler.api.exception.TokenHandlerException;
import org.wso2.carbon.identity.external.api.token.handler.api.exception.TokenRequestException;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.fail;

/**
 * Unit tests for TokenRequestContext class.
 */
public class TokenRequestContextTest {

    private static HttpEntity ccPayload;
    private static HttpEntity refreshPayload;

    private static final String CLIENT_ID = "test-client-id";
    private static final String CLIENT_SECRET = "test-client-secret";
    private static final String SCOPE = "test-scope";
    private static final String TOKEN_ENDPOINT_URL = "https://example.com/token";

    private GrantContext grantContext;

    @BeforeMethod
    public void setUp() throws TokenHandlerException {

        List<NameValuePair> formParamsForPayload = new ArrayList<>();
        formParamsForPayload.add(new BasicNameValuePair("grant_type", "client_credentials"));
        ccPayload = new UrlEncodedFormEntity(formParamsForPayload, StandardCharsets.UTF_8);

        List<NameValuePair> formParamsForRefreshPayload = new ArrayList<>();
        formParamsForRefreshPayload.add(new BasicNameValuePair("grant_type", "refresh_token"));
        formParamsForRefreshPayload.add(new BasicNameValuePair("refresh_token", "test-refresh-token"));
        refreshPayload = new UrlEncodedFormEntity(formParamsForRefreshPayload, StandardCharsets.UTF_8);

        Map<String, String> properties = new HashMap<>();
        properties.put(GrantContext.Property.CLIENT_ID.getName(), CLIENT_ID);
        properties.put(GrantContext.Property.CLIENT_SECRET.getName(), CLIENT_SECRET);
        properties.put(GrantContext.Property.SCOPE.getName(), SCOPE);

        grantContext = new GrantContext.Builder()
                .grantType(GrantContext.GrantType.CLIENT_CREDENTIAL)
                .properties(properties)
                .build();
    }

    /**
     * Test successful creation of TokenRequestContext with all parameters.
     */
    @Test
    public void testCreateTokenRequestContextWithAllParameters() throws TokenRequestException {

        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/x-www-form-urlencoded");
        headers.put("Authorization", "Basic dGVzdDp0ZXN0");

        TokenRequestContext context = new TokenRequestContext.Builder()
                .grantContext(grantContext)
                .endpointUrl(TOKEN_ENDPOINT_URL)
                .headers(headers)
                .payload(ccPayload)
                .build();

        assertNotNull(context);
        assertEquals(context.getGrantContext(), grantContext);
        assertEquals(context.getTokenEndpointUrl(), TOKEN_ENDPOINT_URL);
        assertEquals(context.getHeaders().size(), 2);
        assertEquals(context.getHeaders().get("Content-Type"), "application/x-www-form-urlencoded");
        assertEquals(context.getHeaders().get("Authorization"), "Basic dGVzdDp0ZXN0");
        assertEquals(context.getPayLoad(), ccPayload);
        assertNull(context.getRefreshGrantPayload());
    }

    /**
     * Test successful creation of TokenRequestContext without headers.
     */
    @Test
    public void testCreateTokenRequestContextWithoutHeaders() throws TokenRequestException {

        TokenRequestContext context = new TokenRequestContext.Builder()
                .grantContext(grantContext)
                .endpointUrl(TOKEN_ENDPOINT_URL)
                .payload(ccPayload)
                .build();

        assertNotNull(context);
        assertEquals(context.getGrantContext(), grantContext);
        assertEquals(context.getTokenEndpointUrl(), TOKEN_ENDPOINT_URL);
        assertNotNull(context.getHeaders());
        assertEquals(context.getHeaders().size(), 0);
        assertEquals(context.getPayLoad(), ccPayload);
    }

    /**
     * Test successful creation of TokenRequestContext without payload.
     */
    @Test
    public void testCreateTokenRequestContextWithoutPayload() throws TokenRequestException {

        TokenRequestContext context = new TokenRequestContext.Builder()
                .grantContext(grantContext)
                .endpointUrl(TOKEN_ENDPOINT_URL)
                .build();

        assertNotNull(context);
        assertEquals(context.getGrantContext(), grantContext);
        assertEquals(context.getTokenEndpointUrl(), TOKEN_ENDPOINT_URL);
        assertNull(context.getPayLoad());
    }

    /**
     * Test creation of TokenRequestContext without grant context should throw exception.
     */
    @Test
    public void testCreateTokenRequestContextWithoutGrantContext() {

        try {
            new TokenRequestContext.Builder()
                    .endpointUrl(TOKEN_ENDPOINT_URL)
                    .payload(ccPayload)
                    .build();
            fail("Expected TokenRequestException was not thrown.");
        } catch (TokenRequestException e) {
            assertEquals(e.getErrorCode(), "TOKENMGT-65005");
            assertEquals(e.getDescription(),
                    "The field grant context must be included in the token request context.");
        }
    }

    /**
     * Test creation of TokenRequestContext without endpoint URL should throw exception.
     */
    @Test
    public void testCreateTokenRequestContextWithoutEndpointUrl() {

        try {
            new TokenRequestContext.Builder()
                    .grantContext(grantContext)
                    .payload(ccPayload)
                    .build();
            fail("Expected TokenRequestException was not thrown.");
        } catch (TokenRequestException e) {
            assertEquals(e.getErrorCode(), "TOKENMGT-65005");
            assertEquals(e.getDescription(),
                    "The field token endpoint url must be included in the token request context.");
        }
    }

    /**
     * Test creation of TokenRequestContext with blank endpoint URL should throw exception.
     */
    @Test
    public void testCreateTokenRequestContextWithBlankEndpointUrl() {

        try {
            new TokenRequestContext.Builder()
                    .grantContext(grantContext)
                    .endpointUrl("   ")
                    .payload(ccPayload)
                    .build();
            fail("Expected TokenRequestException was not thrown.");
        } catch (TokenRequestException e) {
            assertEquals(e.getErrorCode(), "TOKENMGT-65005");
            assertEquals(e.getDescription(),
                    "The field token endpoint url must be included in the token request context.");
        }
    }

    /**
     * Test creation of TokenRequestContext with empty endpoint URL should throw exception.
     */
    @Test
    public void testCreateTokenRequestContextWithEmptyEndpointUrl() {

        try {
            new TokenRequestContext.Builder()
                    .grantContext(grantContext)
                    .endpointUrl("")
                    .payload(ccPayload)
                    .build();
            fail("Expected TokenRequestException was not thrown.");
        } catch (TokenRequestException e) {
            assertEquals(e.getErrorCode(), "TOKENMGT-65005");
            assertEquals(e.getDescription(),
                    "The field token endpoint url must be included in the token request context.");
        }
    }

    /**
     * Test setPayLoad with valid payload.
     */
    @Test
    public void testSetPayLoad() throws TokenRequestException {

        TokenRequestContext context = new TokenRequestContext.Builder()
                .grantContext(grantContext)
                .endpointUrl(TOKEN_ENDPOINT_URL)
                .build();

        assertNull(context.getPayLoad());

        context.setPayLoad(ccPayload);
        assertEquals(context.getPayLoad(), ccPayload);
    }

    /**
     * Test setPayLoad with null payload should throw exception.
     */
    @Test
    public void testSetPayLoadWithNull() throws TokenRequestException {

        TokenRequestContext context = new TokenRequestContext.Builder()
                .grantContext(grantContext)
                .endpointUrl(TOKEN_ENDPOINT_URL)
                .build();

        try {
            context.setPayLoad(null);
            fail("Expected TokenRequestException was not thrown.");
        } catch (TokenRequestException e) {
            assertEquals(e.getErrorCode(), "TOKENMGT-65004");
            assertEquals(e.getDescription(), "The token payload must be non-blank payload.");
        }
    }

    /**
     * Test setRefreshGrantPayload with valid payload.
     */
    @Test
    public void testSetRefreshGrantPayload() throws TokenRequestException {

        TokenRequestContext context = new TokenRequestContext.Builder()
                .grantContext(grantContext)
                .endpointUrl(TOKEN_ENDPOINT_URL)
                .payload(ccPayload)
                .build();

        assertNull(context.getRefreshGrantPayload());

        context.setRefreshGrantPayload(refreshPayload);
        assertEquals(context.getRefreshGrantPayload(), refreshPayload);
    }

    /**
     * Test setRefreshGrantPayload with null payload should throw exception.
     */
    @Test
    public void testSetRefreshGrantPayloadWithNull() throws TokenRequestException {

        TokenRequestContext context = new TokenRequestContext.Builder()
                .grantContext(grantContext)
                .endpointUrl(TOKEN_ENDPOINT_URL)
                .build();

        try {
            context.setRefreshGrantPayload(null);
            fail("Expected TokenRequestException was not thrown.");
        } catch (TokenRequestException e) {
            assertEquals(e.getErrorCode(), "TOKENMGT-65004");
            assertEquals(e.getDescription(), "The refresh grant payload must be non-blank payload.");
        }
    }

    /**
     * Test builder chaining.
     */
    @Test
    public void testBuilderChaining() throws TokenRequestException {

        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/x-www-form-urlencoded");

        TokenRequestContext context = new TokenRequestContext.Builder()
                .grantContext(grantContext)
                .endpointUrl(TOKEN_ENDPOINT_URL)
                .headers(headers)
                .payload(ccPayload)
                .build();

        assertNotNull(context);
        assertEquals(context.getGrantContext(), grantContext);
        assertEquals(context.getTokenEndpointUrl(), TOKEN_ENDPOINT_URL);
        assertEquals(context.getHeaders().size(), 1);
        assertEquals(context.getPayLoad(), ccPayload);
    }

    /**
     * Test headers map is empty by default when not set.
     */
    @Test
    public void testDefaultHeaders() throws TokenRequestException {

        TokenRequestContext context = new TokenRequestContext.Builder()
                .grantContext(grantContext)
                .endpointUrl(TOKEN_ENDPOINT_URL)
                .build();

        assertNotNull(context.getHeaders());
        assertEquals(context.getHeaders().size(), 0);
    }
}
