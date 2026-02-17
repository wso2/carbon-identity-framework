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

package org.wso2.carbon.identity.external.api.client.api.model;

import org.apache.http.HttpEntity;
import org.apache.http.entity.StringEntity;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.external.api.client.api.constant.ErrorMessageConstant.ErrorMessage;
import org.wso2.carbon.identity.external.api.client.api.exception.APIClientRequestException;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.fail;

/**
 * Unit tests for APIRequestContext class.
 */
public class APIRequestContextTest {

    private static final String ENDPOINT_URL = "https://api.example.com/endpoint";
    private static final HttpEntity PAYLOAD = new StringEntity("{\"key\":\"value\"}", StandardCharsets.UTF_8);
    private static final String HEADER_KEY = "Content-Type";
    private static final String HEADER_VALUE = "application/json";

    private APIAuthentication apiAuthentication;
    private Map<String, String> headers;

    @BeforeMethod
    public void setUp() throws APIClientRequestException {

        // Create a valid authentication object
        apiAuthentication = new APIAuthentication.Builder()
                .authType(APIAuthentication.AuthType.NONE)
                .build();

        // Create headers map
        headers = new HashMap<>();
        headers.put(HEADER_KEY, HEADER_VALUE);
    }

    /**
     * Test successful creation of APIRequestContext with all valid parameters.
     */
    @Test
    public void testCreateAPIRequestContextWithValidParameters() throws APIClientRequestException {

        APIRequestContext context = new APIRequestContext.Builder()
                .httpMethod(APIRequestContext.HttpMethod.POST)
                .apiAuthentication(apiAuthentication)
                .endpointUrl(ENDPOINT_URL)
                .headers(headers)
                .payload(PAYLOAD)
                .build();

        assertNotNull(context);
        assertEquals(context.getHttpMethod(), APIRequestContext.HttpMethod.POST);
        assertEquals(context.getApiAuthentication(), apiAuthentication);
        assertEquals(context.getEndpointUrl(), ENDPOINT_URL);
        assertEquals(context.getHeaders(), headers);
        assertEquals(context.getPayload(), PAYLOAD);
    }

    /**
     * Test creation of APIRequestContext with minimal required parameters.
     */
    @Test
    public void testCreateAPIRequestContextWithMinimalParameters() throws APIClientRequestException {

        APIRequestContext context = new APIRequestContext.Builder()
                .httpMethod(APIRequestContext.HttpMethod.POST)
                .apiAuthentication(apiAuthentication)
                .endpointUrl(ENDPOINT_URL)
                .payload(PAYLOAD)
                .build();

        assertNotNull(context);
        assertEquals(context.getHttpMethod(), APIRequestContext.HttpMethod.POST);
        assertEquals(context.getApiAuthentication(), apiAuthentication);
        assertEquals(context.getEndpointUrl(), ENDPOINT_URL);
        assertEquals(context.getPayload(), PAYLOAD);
        assertNotNull(context.getHeaders());
    }

    /**
     * Test creation with null HTTP method throws exception.
     */
    @Test
    public void testCreateAPIRequestContextWithNullHttpMethod() {

        try {
            new APIRequestContext.Builder()
                    .apiAuthentication(apiAuthentication)
                    .endpointUrl(ENDPOINT_URL)
                    .payload(PAYLOAD)
                    .build();
            fail("Expected APIClientRequestException was not thrown");
        } catch (APIClientRequestException e) {
            assertEquals(e.getErrorCode(), ErrorMessage.ERROR_CODE_MISSING_REQUEST_FIELD.getCode());
        }
    }

    /**
     * Test creation with null API authentication throws exception.
     */
    @Test
    public void testCreateAPIRequestContextWithNullAuthentication() {

        try {
            new APIRequestContext.Builder()
                    .httpMethod(APIRequestContext.HttpMethod.POST)
                    .endpointUrl(ENDPOINT_URL)
                    .payload(PAYLOAD)
                    .build();
            fail("Expected APIClientRequestException was not thrown");
        } catch (APIClientRequestException e) {
            assertEquals(e.getErrorCode(), ErrorMessage.ERROR_CODE_MISSING_REQUEST_FIELD.getCode());
        }
    }

    /**
     * Test creation with null endpoint URL throws exception.
     */
    @Test
    public void testCreateAPIRequestContextWithNullEndpointUrl() {

        try {
            new APIRequestContext.Builder()
                    .httpMethod(APIRequestContext.HttpMethod.POST)
                    .apiAuthentication(apiAuthentication)
                    .payload(PAYLOAD)
                    .build();
            fail("Expected APIClientRequestException was not thrown");
        } catch (APIClientRequestException e) {
            assertEquals(e.getErrorCode(), ErrorMessage.ERROR_CODE_MISSING_REQUEST_FIELD.getCode());
        }
    }

    /**
     * Test creation with blank endpoint URL throws exception.
     */
    @Test
    public void testCreateAPIRequestContextWithBlankEndpointUrl() {

        try {
            new APIRequestContext.Builder()
                    .httpMethod(APIRequestContext.HttpMethod.POST)
                    .apiAuthentication(apiAuthentication)
                    .endpointUrl("")
                    .payload(PAYLOAD)
                    .build();
            fail("Expected APIClientRequestException was not thrown");
        } catch (APIClientRequestException e) {
            assertEquals(e.getErrorCode(), ErrorMessage.ERROR_CODE_MISSING_REQUEST_FIELD.getCode());
        }
    }

    /**
     * Test creation with whitespace-only endpoint URL throws exception.
     */
    @Test
    public void testCreateAPIRequestContextWithWhitespaceEndpointUrl() {

        try {
            new APIRequestContext.Builder()
                    .httpMethod(APIRequestContext.HttpMethod.POST)
                    .apiAuthentication(apiAuthentication)
                    .endpointUrl("   ")
                    .payload(PAYLOAD)
                    .build();
            fail("Expected APIClientRequestException was not thrown");
        } catch (APIClientRequestException e) {
            assertEquals(e.getErrorCode(), ErrorMessage.ERROR_CODE_MISSING_REQUEST_FIELD.getCode());
        }
    }

    /**
     * Test creation with null payload throws exception.
     */
    @Test
    public void testCreateAPIRequestContextWithNullPayload() {

        try {
            new APIRequestContext.Builder()
                    .httpMethod(APIRequestContext.HttpMethod.POST)
                    .apiAuthentication(apiAuthentication)
                    .endpointUrl(ENDPOINT_URL)
                    .build();
            fail("Expected APIClientRequestException was not thrown");
        } catch (APIClientRequestException e) {
            assertEquals(e.getErrorCode(), ErrorMessage.ERROR_CODE_MISSING_REQUEST_FIELD.getCode());
        }
    }

    /**
     * Test builder method chaining.
     */
    @Test
    public void testBuilderMethodChaining() throws APIClientRequestException {

        APIRequestContext context = new APIRequestContext.Builder()
                .httpMethod(APIRequestContext.HttpMethod.POST)
                .apiAuthentication(apiAuthentication)
                .endpointUrl(ENDPOINT_URL)
                .headers(headers)
                .payload(PAYLOAD)
                .build();

        assertNotNull(context);
        assertEquals(context.getHttpMethod(), APIRequestContext.HttpMethod.POST);
        assertEquals(context.getApiAuthentication(), apiAuthentication);
        assertEquals(context.getEndpointUrl(), ENDPOINT_URL);
        assertEquals(context.getHeaders(), headers);
        assertEquals(context.getPayload(), PAYLOAD);
    }

    /**
     * Test with multiple headers.
     */
    @Test
    public void testCreateAPIRequestContextWithMultipleHeaders() throws APIClientRequestException {

        Map<String, String> multipleHeaders = new HashMap<>();
        multipleHeaders.put("Content-Type", "application/json");
        multipleHeaders.put("Authorization", "Bearer token");
        multipleHeaders.put("Accept", "application/json");

        APIRequestContext context = new APIRequestContext.Builder()
                .httpMethod(APIRequestContext.HttpMethod.POST)
                .apiAuthentication(apiAuthentication)
                .endpointUrl(ENDPOINT_URL)
                .headers(multipleHeaders)
                .payload(PAYLOAD)
                .build();

        assertNotNull(context);
        assertEquals(context.getHeaders().size(), 3);
        assertEquals(context.getHeaders().get("Content-Type"), "application/json");
        assertEquals(context.getHeaders().get("Authorization"), "Bearer token");
        assertEquals(context.getHeaders().get("Accept"), "application/json");
    }
}
