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

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.external.api.token.handler.api.exception.TokenHandlerException;
import org.wso2.carbon.identity.external.api.token.handler.api.exception.TokenRequestException;

import java.util.HashMap;
import java.util.Map;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

/**
 * Unit tests for TokenRequestContext class.
 */
public class TokenRequestContextTest {

    private static final String CLIENT_ID = "test_client_id";
    private static final String CLIENT_SECRET = "test_client_secret";
    private static final String SCOPE = "test_scope";
    private static final String ENDPOINT_URL = "https://example.com/token";
    private static final String PAYLOAD = "test_payload";
    private static final String REFRESH_PAYLOAD = "refresh_test_payload";

    private GrantContext grantContext;
    private Map<String, String> headers;

    @BeforeMethod
    public void setUp() throws TokenHandlerException {

        Map<String, String> properties = new HashMap<>();
        properties.put(GrantContext.Property.CLIENT_ID.getName(), CLIENT_ID);
        properties.put(GrantContext.Property.CLIENT_SECRET.getName(), CLIENT_SECRET);
        properties.put(GrantContext.Property.SCOPE.getName(), SCOPE);

        grantContext = new GrantContext.Builder()
                .grantType(GrantContext.GrantType.CLIENT_CREDENTIAL)
                .properties(properties)
                .build();

        headers = new HashMap<>();
        headers.put("Content-Type", "application/x-www-form-urlencoded");
        headers.put("Accept", "application/json");
    }

    /**
     * Test successful creation of TokenRequestContext with all parameters.
     */
    @Test
    public void testCreateTokenRequestContextWithAllParameters() throws TokenRequestException {

        TokenRequestContext context = new TokenRequestContext.Builder()
                .grantContext(grantContext)
                .endpointUrl(ENDPOINT_URL)
                .headers(headers)
                .payload(PAYLOAD)
                .build();

        assertNotNull(context);
        assertEquals(context.getGrantContext(), grantContext);
        assertEquals(context.getTokenEndpointUrl(), ENDPOINT_URL);
        assertEquals(context.getHeaders(), headers);
        assertEquals(context.getPayLoad(), PAYLOAD);
    }

    /**
     * Test creation of TokenRequestContext with minimum required parameters.
     */
    @Test
    public void testCreateTokenRequestContextWithMinimumParameters() throws TokenRequestException {

        TokenRequestContext context = new TokenRequestContext.Builder()
                .grantContext(grantContext)
                .endpointUrl(ENDPOINT_URL)
                .build();

        assertNotNull(context);
        assertEquals(context.getGrantContext(), grantContext);
        assertEquals(context.getTokenEndpointUrl(), ENDPOINT_URL);
        assertNotNull(context.getHeaders());
        assertTrue(context.getHeaders().isEmpty());
    }

    /**
     * Test creation of TokenRequestContext without grant context - should throw exception.
     */
    @Test
    public void testCreateTokenRequestContextWithoutGrantContext() {

        try {
            new TokenRequestContext.Builder()
                    .endpointUrl(ENDPOINT_URL)
                    .headers(headers)
                    .payload(PAYLOAD)
                    .build();
            fail("Expected TokenRequestException");
        } catch (TokenRequestException e) {
            assertEquals(e.getMessage(), "Grant context cannot be null.");
        }
    }

    /**
     * Test creation of TokenRequestContext without endpoint URL - should throw exception.
     */
    @Test
    public void testCreateTokenRequestContextWithoutEndpointUrl() {

        try {
            new TokenRequestContext.Builder()
                    .grantContext(grantContext)
                    .headers(headers)
                    .payload(PAYLOAD)
                    .build();
            fail("Expected TokenRequestException");
        } catch (TokenRequestException e) {
            assertEquals(e.getMessage(), "Endpoint URL cannot be null or empty.");
        }
    }

    /**
     * Test creation of TokenRequestContext with blank endpoint URL - should throw exception.
     */
    @Test
    public void testCreateTokenRequestContextWithBlankEndpointUrl() {

        try {
            new TokenRequestContext.Builder()
                    .grantContext(grantContext)
                    .endpointUrl("   ")
                    .headers(headers)
                    .payload(PAYLOAD)
                    .build();
            fail("Expected TokenRequestException");
        } catch (TokenRequestException e) {
            assertEquals(e.getMessage(), "Endpoint URL cannot be null or empty.");
        }
    }

    /**
     * Test creation of TokenRequestContext with empty endpoint URL - should throw exception.
     */
    @Test
    public void testCreateTokenRequestContextWithEmptyEndpointUrl() {

        try {
            new TokenRequestContext.Builder()
                    .grantContext(grantContext)
                    .endpointUrl("")
                    .headers(headers)
                    .payload(PAYLOAD)
                    .build();
            fail("Expected TokenRequestException");
        } catch (TokenRequestException e) {
            assertEquals(e.getMessage(), "Endpoint URL cannot be null or empty.");
        }
    }

    /**
     * Test setting payload after creating TokenRequestContext.
     */
    @Test
    public void testSetPayload() throws TokenRequestException {

        TokenRequestContext context = new TokenRequestContext.Builder()
                .grantContext(grantContext)
                .endpointUrl(ENDPOINT_URL)
                .build();

        context.setPayLoad(PAYLOAD);
        assertEquals(context.getPayLoad(), PAYLOAD);
    }

    /**
     * Test setting null payload - should throw exception.
     */
    @Test
    public void testSetNullPayload() throws TokenRequestException {

        TokenRequestContext context = new TokenRequestContext.Builder()
                .grantContext(grantContext)
                .endpointUrl(ENDPOINT_URL)
                .build();

        try {
            context.setPayLoad(null);
            fail("Expected TokenRequestException");
        } catch (TokenRequestException e) {
            assertEquals(e.getMessage(), "Payload cannot be null or empty.");
        }
    }

    /**
     * Test setting empty payload - should throw exception.
     */
    @Test
    public void testSetEmptyPayload() throws TokenRequestException {

        TokenRequestContext context = new TokenRequestContext.Builder()
                .grantContext(grantContext)
                .endpointUrl(ENDPOINT_URL)
                .build();

        try {
            context.setPayLoad("");
            fail("Expected TokenRequestException");
        } catch (TokenRequestException e) {
            assertEquals(e.getMessage(), "Payload cannot be null or empty.");
        }
    }

    /**
     * Test setting blank payload - should throw exception.
     */
    @Test
    public void testSetBlankPayload() throws TokenRequestException {

        TokenRequestContext context = new TokenRequestContext.Builder()
                .grantContext(grantContext)
                .endpointUrl(ENDPOINT_URL)
                .build();

        try {
            context.setPayLoad("   ");
            fail("Expected TokenRequestException");
        } catch (TokenRequestException e) {
            assertEquals(e.getMessage(), "Payload cannot be null or empty.");
        }
    }

    /**
     * Test setting refresh grant payload.
     */
    @Test
    public void testSetRefreshGrantPayload() throws TokenRequestException {

        TokenRequestContext context = new TokenRequestContext.Builder()
                .grantContext(grantContext)
                .endpointUrl(ENDPOINT_URL)
                .build();

        context.setRefreshGrantPayload(REFRESH_PAYLOAD);
        assertEquals(context.getRefreshGrantPayload(), REFRESH_PAYLOAD);
    }

    /**
     * Test setting null refresh grant payload - should throw exception.
     */
    @Test
    public void testSetNullRefreshGrantPayload() throws TokenRequestException {

        TokenRequestContext context = new TokenRequestContext.Builder()
                .grantContext(grantContext)
                .endpointUrl(ENDPOINT_URL)
                .build();

        try {
            context.setRefreshGrantPayload(null);
            fail("Expected TokenRequestException");
        } catch (TokenRequestException e) {
            assertEquals(e.getMessage(), "Payload cannot be null or empty.");
        }
    }

    /**
     * Test setting empty refresh grant payload - should throw exception.
     */
    @Test
    public void testSetEmptyRefreshGrantPayload() throws TokenRequestException {

        TokenRequestContext context = new TokenRequestContext.Builder()
                .grantContext(grantContext)
                .endpointUrl(ENDPOINT_URL)
                .build();

        try {
            context.setRefreshGrantPayload("");
            fail("Expected TokenRequestException");
        } catch (TokenRequestException e) {
            assertEquals(e.getMessage(), "Payload cannot be null or empty.");
        }
    }

    /**
     * Test setting blank refresh grant payload - should throw exception.
     */
    @Test
    public void testSetBlankRefreshGrantPayload() throws TokenRequestException {

        TokenRequestContext context = new TokenRequestContext.Builder()
                .grantContext(grantContext)
                .endpointUrl(ENDPOINT_URL)
                .build();

        try {
            context.setRefreshGrantPayload("   ");
            fail("Expected TokenRequestException");
        } catch (TokenRequestException e) {
            assertEquals(e.getMessage(), "Payload cannot be null or empty.");
        }
    }

    /**
     * Test headers are properly set and retrieved.
     */
    @Test
    public void testHeadersHandling() throws TokenRequestException {

        Map<String, String> customHeaders = new HashMap<>();
        customHeaders.put("Authorization", "Bearer token");
        customHeaders.put("Custom-Header", "custom-value");

        TokenRequestContext context = new TokenRequestContext.Builder()
                .grantContext(grantContext)
                .endpointUrl(ENDPOINT_URL)
                .headers(customHeaders)
                .build();

        Map<String, String> retrievedHeaders = context.getHeaders();
        assertEquals(retrievedHeaders.size(), 2);
        assertEquals(retrievedHeaders.get("Authorization"), "Bearer token");
        assertEquals(retrievedHeaders.get("Custom-Header"), "custom-value");
    }

    /**
     * Test with null headers - should not fail.
     */
    @Test
    public void testNullHeaders() throws TokenRequestException {

        TokenRequestContext context = new TokenRequestContext.Builder()
                .grantContext(grantContext)
                .endpointUrl(ENDPOINT_URL)
                .headers(null)
                .build();

        assertNotNull(context);
        assertNull(context.getHeaders());
    }

    /**
     * Test with empty headers map.
     */
    @Test
    public void testEmptyHeaders() throws TokenRequestException {

        Map<String, String> emptyHeaders = new HashMap<>();

        TokenRequestContext context = new TokenRequestContext.Builder()
                .grantContext(grantContext)
                .endpointUrl(ENDPOINT_URL)
                .headers(emptyHeaders)
                .build();

        assertNotNull(context);
        assertNotNull(context.getHeaders());
        assertTrue(context.getHeaders().isEmpty());
    }

    /**
     * Test builder pattern with method chaining.
     */
    @Test
    public void testBuilderMethodChaining() throws TokenRequestException {

        TokenRequestContext context = new TokenRequestContext.Builder()
                .grantContext(grantContext)
                .endpointUrl(ENDPOINT_URL)
                .headers(headers)
                .payload(PAYLOAD)
                .build();

        assertNotNull(context);
        assertEquals(context.getGrantContext(), grantContext);
        assertEquals(context.getTokenEndpointUrl(), ENDPOINT_URL);
        assertEquals(context.getHeaders(), headers);
        assertEquals(context.getPayLoad(), PAYLOAD);
    }

    /**
     * Test initial payload is null when not set in builder.
     */
    @Test
    public void testInitialPayloadIsNull() throws TokenRequestException {

        TokenRequestContext context = new TokenRequestContext.Builder()
                .grantContext(grantContext)
                .endpointUrl(ENDPOINT_URL)
                .build();

        assertNull(context.getPayLoad());
        assertNull(context.getRefreshGrantPayload());
    }
}
