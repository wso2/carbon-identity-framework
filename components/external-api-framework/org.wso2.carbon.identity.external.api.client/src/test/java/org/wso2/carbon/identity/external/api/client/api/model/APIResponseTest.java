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

import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;

/**
 * Unit tests for APIResponse class.
 */
public class APIResponseTest {

    private static final int STATUS_CODE_200 = 200;
    private static final int STATUS_CODE_404 = 404;
    private static final int STATUS_CODE_500 = 500;
    private static final String RESPONSE_BODY = "{\"message\":\"success\"}";
    private static final String EMPTY_RESPONSE_BODY = "";
    private static final String NULL_RESPONSE_BODY = null;

    /**
     * Test successful creation of APIResponse with valid parameters.
     */
    @Test
    public void testCreateAPIResponseWithValidParameters() {

        APIResponse response = new APIResponse.Builder(STATUS_CODE_200, RESPONSE_BODY).build();

        assertNotNull(response);
        assertEquals(response.getStatusCode(), STATUS_CODE_200);
        assertEquals(response.getResponseBody(), RESPONSE_BODY);
    }

    /**
     * Test creation of APIResponse with different status codes.
     */
    @Test
    public void testCreateAPIResponseWithDifferentStatusCodes() {

        // Test with 200 OK
        APIResponse response200 = new APIResponse.Builder(STATUS_CODE_200, RESPONSE_BODY).build();
        assertEquals(response200.getStatusCode(), STATUS_CODE_200);

        // Test with 404 Not Found
        APIResponse response404 = new APIResponse.Builder(STATUS_CODE_404, RESPONSE_BODY).build();
        assertEquals(response404.getStatusCode(), STATUS_CODE_404);

        // Test with 500 Internal Server Error
        APIResponse response500 = new APIResponse.Builder(STATUS_CODE_500, RESPONSE_BODY).build();
        assertEquals(response500.getStatusCode(), STATUS_CODE_500);
    }

    /**
     * Test creation of APIResponse with empty response body.
     */
    @Test
    public void testCreateAPIResponseWithEmptyResponseBody() {

        APIResponse response = new APIResponse.Builder(STATUS_CODE_200, EMPTY_RESPONSE_BODY).build();

        assertNotNull(response);
        assertEquals(response.getStatusCode(), STATUS_CODE_200);
        assertEquals(response.getResponseBody(), EMPTY_RESPONSE_BODY);
    }

    /**
     * Test creation of APIResponse with null response body.
     */
    @Test
    public void testCreateAPIResponseWithNullResponseBody() {

        APIResponse response = new APIResponse.Builder(STATUS_CODE_200, NULL_RESPONSE_BODY).build();

        assertNotNull(response);
        assertEquals(response.getStatusCode(), STATUS_CODE_200);
        assertNull(response.getResponseBody());
    }

    /**
     * Test creation of APIResponse with zero status code.
     */
    @Test
    public void testCreateAPIResponseWithZeroStatusCode() {

        APIResponse response = new APIResponse.Builder(0, RESPONSE_BODY).build();

        assertNotNull(response);
        assertEquals(response.getStatusCode(), 0);
        assertEquals(response.getResponseBody(), RESPONSE_BODY);
    }

    /**
     * Test creation of APIResponse with negative status code.
     */
    @Test
    public void testCreateAPIResponseWithNegativeStatusCode() {

        APIResponse response = new APIResponse.Builder(-1, RESPONSE_BODY).build();

        assertNotNull(response);
        assertEquals(response.getStatusCode(), -1);
        assertEquals(response.getResponseBody(), RESPONSE_BODY);
    }

    /**
     * Test builder constructor requires both parameters.
     */
    @Test
    public void testBuilderConstructorWithBothParameters() {

        APIResponse.Builder builder = new APIResponse.Builder(STATUS_CODE_200, RESPONSE_BODY);
        APIResponse response = builder.build();

        assertNotNull(response);
        assertEquals(response.getStatusCode(), STATUS_CODE_200);
        assertEquals(response.getResponseBody(), RESPONSE_BODY);
    }

    /**
     * Test builder setter methods.
     */
    @Test
    public void testBuilderSetterMethods() {

        int newStatusCode = 201;
        String newResponseBody = "{\"message\":\"created\"}";

        APIResponse response = new APIResponse.Builder(STATUS_CODE_200, RESPONSE_BODY)
                .statusCode(newStatusCode)
                .responseBody(newResponseBody)
                .build();

        assertNotNull(response);
        assertEquals(response.getStatusCode(), newStatusCode);
        assertEquals(response.getResponseBody(), newResponseBody);
    }

    /**
     * Test builder method chaining.
     */
    @Test
    public void testBuilderMethodChaining() {

        APIResponse response = new APIResponse.Builder(STATUS_CODE_404, "Not Found")
                .statusCode(STATUS_CODE_200)
                .responseBody(RESPONSE_BODY)
                .build();

        assertNotNull(response);
        assertEquals(response.getStatusCode(), STATUS_CODE_200);
        assertEquals(response.getResponseBody(), RESPONSE_BODY);
    }

    /**
     * Test getter methods return correct values.
     */
    @Test
    public void testGetterMethods() {

        APIResponse response = new APIResponse.Builder(STATUS_CODE_200, RESPONSE_BODY).build();

        assertEquals(response.getStatusCode(), STATUS_CODE_200);
        assertEquals(response.getResponseBody(), RESPONSE_BODY);
    }

    /**
     * Test common HTTP status codes.
     */
    @Test
    public void testCommonHttpStatusCodes() {

        // 2xx Success codes
        APIResponse response200 = new APIResponse.Builder(200, "OK").build();
        assertEquals(response200.getStatusCode(), 200);

        APIResponse response201 = new APIResponse.Builder(201, "Created").build();
        assertEquals(response201.getStatusCode(), 201);

        APIResponse response204 = new APIResponse.Builder(204, "No Content").build();
        assertEquals(response204.getStatusCode(), 204);

        // 4xx Client Error codes
        APIResponse response400 = new APIResponse.Builder(400, "Bad Request").build();
        assertEquals(response400.getStatusCode(), 400);

        APIResponse response401 = new APIResponse.Builder(401, "Unauthorized").build();
        assertEquals(response401.getStatusCode(), 401);

        APIResponse response403 = new APIResponse.Builder(403, "Forbidden").build();
        assertEquals(response403.getStatusCode(), 403);

        APIResponse response404 = new APIResponse.Builder(404, "Not Found").build();
        assertEquals(response404.getStatusCode(), 404);

        // 5xx Server Error codes
        APIResponse response500 = new APIResponse.Builder(500, "Internal Server Error").build();
        assertEquals(response500.getStatusCode(), 500);

        APIResponse response502 = new APIResponse.Builder(502, "Bad Gateway").build();
        assertEquals(response502.getStatusCode(), 502);

        APIResponse response503 = new APIResponse.Builder(503, "Service Unavailable").build();
        assertEquals(response503.getStatusCode(), 503);
    }

    /**
     * Test builder returns the same builder instance for method chaining.
     */
    @Test
    public void testBuilderReturnsCorrectInstance() {

        APIResponse.Builder builder = new APIResponse.Builder(STATUS_CODE_200, RESPONSE_BODY);

        APIResponse.Builder returnedBuilder = builder.statusCode(STATUS_CODE_404);
        assertEquals(builder, returnedBuilder);

        returnedBuilder = builder.responseBody("New Response");
        assertEquals(builder, returnedBuilder);
    }

    /**
     * Test edge case with maximum integer status code.
     */
    @Test
    public void testMaxIntegerStatusCode() {

        APIResponse response = new APIResponse.Builder(Integer.MAX_VALUE, RESPONSE_BODY).build();

        assertNotNull(response);
        assertEquals(response.getStatusCode(), Integer.MAX_VALUE);
        assertEquals(response.getResponseBody(), RESPONSE_BODY);
    }

    /**
     * Test edge case with minimum integer status code.
     */
    @Test
    public void testMinIntegerStatusCode() {

        APIResponse response = new APIResponse.Builder(Integer.MIN_VALUE, RESPONSE_BODY).build();

        assertNotNull(response);
        assertEquals(response.getStatusCode(), Integer.MIN_VALUE);
        assertEquals(response.getResponseBody(), RESPONSE_BODY);
    }

    /**
     * Test with large response body.
     */
    @Test
    public void testLargeResponseBody() {

        StringBuilder largeBody = new StringBuilder();
        for (int i = 0; i < 1000; i++) {
            largeBody.append("This is a large response body. ");
        }
        String largeResponseBody = largeBody.toString();

        APIResponse response = new APIResponse.Builder(STATUS_CODE_200, largeResponseBody).build();

        assertNotNull(response);
        assertEquals(response.getStatusCode(), STATUS_CODE_200);
        assertEquals(response.getResponseBody(), largeResponseBody);
    }

    /**
     * Test object state consistency.
     */
    @Test
    public void testObjectStateConsistency() {

        APIResponse response = new APIResponse.Builder(STATUS_CODE_200, RESPONSE_BODY).build();

        // Test multiple get calls return same value
        int firstStatusCall = response.getStatusCode();
        int secondStatusCall = response.getStatusCode();
        assertEquals(firstStatusCall, secondStatusCall);

        String firstBodyCall = response.getResponseBody();
        String secondBodyCall = response.getResponseBody();
        assertEquals(firstBodyCall, secondBodyCall);
    }
}
