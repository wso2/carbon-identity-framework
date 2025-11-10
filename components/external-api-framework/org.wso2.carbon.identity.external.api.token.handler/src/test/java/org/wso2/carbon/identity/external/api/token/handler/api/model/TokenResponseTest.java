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

import org.testng.annotations.Test;
import org.wso2.carbon.identity.external.api.client.api.model.APIResponse;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;

/**
 * Unit tests for TokenResponse class.
 */
public class TokenResponseTest {

    private static final String ACCESS_TOKEN = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9";
    private static final String REFRESH_TOKEN = "refresh_token_value";
    private static final int STATUS_CODE_200 = 200;
    private static final int STATUS_CODE_400 = 400;
    private static final int STATUS_CODE_401 = 401;
    private static final int STATUS_CODE_500 = 500;

    /**
     * Test successful creation of TokenResponse with valid JSON response containing access and refresh tokens.
     */
    @Test
    public void testCreateTokenResponseWithValidJsonTokens() {

        String responseBody = String.format(
                "{\"access_token\":\"%s\",\"refresh_token\":\"%s\",\"token_type\":\"Bearer\",\"expires_in\":3600}",
                ACCESS_TOKEN, REFRESH_TOKEN);
        APIResponse apiResponse = new APIResponse.Builder(STATUS_CODE_200, responseBody).build();

        TokenResponse tokenResponse = new TokenResponse.Builder(apiResponse).build();

        assertNotNull(tokenResponse);
        assertEquals(tokenResponse.getStatusCode(), STATUS_CODE_200);
        assertEquals(tokenResponse.getResponseBody(), responseBody);
        assertEquals(tokenResponse.getAccessToken(), ACCESS_TOKEN);
        assertEquals(tokenResponse.getRefreshToken(), REFRESH_TOKEN);
    }

    /**
     * Test creation of TokenResponse with valid JSON response containing only access token.
     */
    @Test
    public void testCreateTokenResponseWithOnlyAccessToken() {

        String responseBody = String.format("{\"access_token\":\"%s\",\"token_type\":\"Bearer\",\"expires_in\":3600}", 
                ACCESS_TOKEN);
        APIResponse apiResponse = new APIResponse.Builder(STATUS_CODE_200, responseBody).build();

        TokenResponse tokenResponse = new TokenResponse.Builder(apiResponse).build();

        assertNotNull(tokenResponse);
        assertEquals(tokenResponse.getStatusCode(), STATUS_CODE_200);
        assertEquals(tokenResponse.getResponseBody(), responseBody);
        assertEquals(tokenResponse.getAccessToken(), ACCESS_TOKEN);
        assertNull(tokenResponse.getRefreshToken());
    }

    /**
     * Test creation of TokenResponse with valid JSON response containing only refresh token.
     */
    @Test
    public void testCreateTokenResponseWithOnlyRefreshToken() {

        String responseBody = String.format("{\"refresh_token\":\"%s\",\"token_type\":\"Bearer\"}", 
                REFRESH_TOKEN);
        APIResponse apiResponse = new APIResponse.Builder(STATUS_CODE_200, responseBody).build();

        TokenResponse tokenResponse = new TokenResponse.Builder(apiResponse).build();

        assertNotNull(tokenResponse);
        assertEquals(tokenResponse.getStatusCode(), STATUS_CODE_200);
        assertEquals(tokenResponse.getResponseBody(), responseBody);
        assertNull(tokenResponse.getAccessToken());
        assertEquals(tokenResponse.getRefreshToken(), REFRESH_TOKEN);
    }

    /**
     * Test creation of TokenResponse with empty JSON response.
     */
    @Test
    public void testCreateTokenResponseWithEmptyJson() {

        String responseBody = "{}";
        APIResponse apiResponse = new APIResponse.Builder(STATUS_CODE_200, responseBody).build();

        TokenResponse tokenResponse = new TokenResponse.Builder(apiResponse).build();

        assertNotNull(tokenResponse);
        assertEquals(tokenResponse.getStatusCode(), STATUS_CODE_200);
        assertEquals(tokenResponse.getResponseBody(), responseBody);
        assertNull(tokenResponse.getAccessToken());
        assertNull(tokenResponse.getRefreshToken());
    }

    /**
     * Test creation of TokenResponse with null response body.
     */
    @Test
    public void testCreateTokenResponseWithNullResponseBody() {

        APIResponse apiResponse = new APIResponse.Builder(STATUS_CODE_200, null).build();

        TokenResponse tokenResponse = new TokenResponse.Builder(apiResponse).build();

        assertNotNull(tokenResponse);
        assertEquals(tokenResponse.getStatusCode(), STATUS_CODE_200);
        assertNull(tokenResponse.getResponseBody());
        assertNull(tokenResponse.getAccessToken());
        assertNull(tokenResponse.getRefreshToken());
    }

    /**
     * Test creation of TokenResponse with invalid JSON response.
     */
    @Test
    public void testCreateTokenResponseWithInvalidJson() {

        String responseBody = "invalid json {";
        APIResponse apiResponse = new APIResponse.Builder(STATUS_CODE_400, responseBody).build();

        TokenResponse tokenResponse = new TokenResponse.Builder(apiResponse).build();

        assertNotNull(tokenResponse);
        assertEquals(tokenResponse.getStatusCode(), STATUS_CODE_400);
        assertEquals(tokenResponse.getResponseBody(), responseBody);
        assertNull(tokenResponse.getAccessToken());
        assertNull(tokenResponse.getRefreshToken());
    }

    /**
     * Test creation of TokenResponse with non-JSON response body.
     */
    @Test
    public void testCreateTokenResponseWithNonJsonResponse() {

        String responseBody = "Not a JSON response";
        APIResponse apiResponse = new APIResponse.Builder(STATUS_CODE_500, responseBody).build();

        TokenResponse tokenResponse = new TokenResponse.Builder(apiResponse).build();

        assertNotNull(tokenResponse);
        assertEquals(tokenResponse.getStatusCode(), STATUS_CODE_500);
        assertEquals(tokenResponse.getResponseBody(), responseBody);
        assertNull(tokenResponse.getAccessToken());
        assertNull(tokenResponse.getRefreshToken());
    }

    /**
     * Test creation of TokenResponse with JSON array instead of object.
     */
    @Test
    public void testCreateTokenResponseWithJsonArray() {

        String responseBody = "[{\"access_token\":\"token\"}]";
        APIResponse apiResponse = new APIResponse.Builder(STATUS_CODE_200, responseBody).build();

        TokenResponse tokenResponse = new TokenResponse.Builder(apiResponse).build();

        assertNotNull(tokenResponse);
        assertEquals(tokenResponse.getStatusCode(), STATUS_CODE_200);
        assertEquals(tokenResponse.getResponseBody(), responseBody);
        assertNull(tokenResponse.getAccessToken());
        assertNull(tokenResponse.getRefreshToken());
    }

    /**
     * Test creation of TokenResponse with null token values in JSON.
     */
    @Test
    public void testCreateTokenResponseWithNullTokenValues() {

        String responseBody = "{\"access_token\":null,\"refresh_token\":null}";
        APIResponse apiResponse = new APIResponse.Builder(STATUS_CODE_200, responseBody).build();

        TokenResponse tokenResponse = new TokenResponse.Builder(apiResponse).build();

        assertNotNull(tokenResponse);
        assertEquals(tokenResponse.getStatusCode(), STATUS_CODE_200);
        assertEquals(tokenResponse.getResponseBody(), responseBody);
        assertNull(tokenResponse.getAccessToken());
        assertNull(tokenResponse.getRefreshToken());
    }

    /**
     * Test creation of TokenResponse with empty string token values.
     */
    @Test
    public void testCreateTokenResponseWithEmptyStringTokens() {

        String responseBody = "{\"access_token\":\"\",\"refresh_token\":\"\"}";
        APIResponse apiResponse = new APIResponse.Builder(STATUS_CODE_200, responseBody).build();

        TokenResponse tokenResponse = new TokenResponse.Builder(apiResponse).build();

        assertNotNull(tokenResponse);
        assertEquals(tokenResponse.getStatusCode(), STATUS_CODE_200);
        assertEquals(tokenResponse.getResponseBody(), responseBody);
        assertEquals(tokenResponse.getAccessToken(), "");
        assertEquals(tokenResponse.getRefreshToken(), "");
    }

    /**
     * Test creation of TokenResponse with malformed access token field.
     */
    @Test
    public void testCreateTokenResponseWithMalformedAccessToken() {

        String responseBody = "{\"access_token\":123,\"refresh_token\":\"valid_refresh_token\"}";
        APIResponse apiResponse = new APIResponse.Builder(STATUS_CODE_200, responseBody).build();

        TokenResponse tokenResponse = new TokenResponse.Builder(apiResponse).build();

        assertNotNull(tokenResponse);
        assertEquals(tokenResponse.getStatusCode(), STATUS_CODE_200);
        assertEquals(tokenResponse.getResponseBody(), responseBody);
        assertEquals(tokenResponse.getAccessToken(), "123");
        assertEquals(tokenResponse.getRefreshToken(), "valid_refresh_token");
    }

    /**
     * Test creation of TokenResponse with error response.
     */
    @Test
    public void testCreateTokenResponseWithErrorResponse() {

        String responseBody = String.format(
                "{\"error\":\"invalid_grant\",\"error_description\":\"The provided authorization grant is invalid\"}");
        APIResponse apiResponse = new APIResponse.Builder(STATUS_CODE_400, responseBody).build();

        TokenResponse tokenResponse = new TokenResponse.Builder(apiResponse).build();

        assertNotNull(tokenResponse);
        assertEquals(tokenResponse.getStatusCode(), STATUS_CODE_400);
        assertEquals(tokenResponse.getResponseBody(), responseBody);
        assertNull(tokenResponse.getAccessToken());
        assertNull(tokenResponse.getRefreshToken());
    }

    /**
     * Test creation of TokenResponse with unauthorized error.
     */
    @Test
    public void testCreateTokenResponseWithUnauthorizedError() {

        String responseBody = String.format(
                "{\"error\":\"invalid_client\",\"error_description\":\"Client authentication failed\"}");
        APIResponse apiResponse = new APIResponse.Builder(STATUS_CODE_401, responseBody).build();

        TokenResponse tokenResponse = new TokenResponse.Builder(apiResponse).build();

        assertNotNull(tokenResponse);
        assertEquals(tokenResponse.getStatusCode(), STATUS_CODE_401);
        assertEquals(tokenResponse.getResponseBody(), responseBody);
        assertNull(tokenResponse.getAccessToken());
        assertNull(tokenResponse.getRefreshToken());
    }

    /**
     * Test creation of TokenResponse with complex JSON structure.
     */
    @Test
    public void testCreateTokenResponseWithComplexJson() {

        String responseBody = String.format(
                "{\"access_token\":\"%s\",\"refresh_token\":\"%s\",\"token_type\":\"Bearer\",\"expires_in\":3600," +
                "\"scope\":\"read write\",\"additional_info\":{\"user_id\":\"12345\",\"role\":\"admin\"}}",
                ACCESS_TOKEN, REFRESH_TOKEN);
        APIResponse apiResponse = new APIResponse.Builder(STATUS_CODE_200, responseBody).build();

        TokenResponse tokenResponse = new TokenResponse.Builder(apiResponse).build();

        assertNotNull(tokenResponse);
        assertEquals(tokenResponse.getStatusCode(), STATUS_CODE_200);
        assertEquals(tokenResponse.getResponseBody(), responseBody);
        assertEquals(tokenResponse.getAccessToken(), ACCESS_TOKEN);
        assertEquals(tokenResponse.getRefreshToken(), REFRESH_TOKEN);
    }

    /**
     * Test TokenResponse inherits from APIResponse properly.
     */
    @Test
    public void testTokenResponseInheritance() {

        String responseBody = String.format("{\"access_token\":\"%s\"}", ACCESS_TOKEN);
        APIResponse apiResponse = new APIResponse.Builder(STATUS_CODE_200, responseBody).build();

        TokenResponse tokenResponse = new TokenResponse.Builder(apiResponse).build();

        // Verify it's an instance of APIResponse
        assertNotNull(tokenResponse);
        assertEquals(tokenResponse.getStatusCode(), STATUS_CODE_200);
        assertEquals(tokenResponse.getResponseBody(), responseBody);
    }

    /**
     * Test creation of TokenResponse with very long token values.
     */
    @Test
    public void testCreateTokenResponseWithLongTokens() {

        StringBuilder accessTokenBuilder = new StringBuilder();
        StringBuilder refreshTokenBuilder = new StringBuilder();
        for (int i = 0; i < 1000; i++) {
            accessTokenBuilder.append("a");
            refreshTokenBuilder.append("r");
        }
        String longAccessToken = accessTokenBuilder.toString();
        String longRefreshToken = refreshTokenBuilder.toString();
        
        String responseBody = String.format("{\"access_token\":\"%s\",\"refresh_token\":\"%s\"}", 
                longAccessToken, longRefreshToken);
        APIResponse apiResponse = new APIResponse.Builder(STATUS_CODE_200, responseBody).build();

        TokenResponse tokenResponse = new TokenResponse.Builder(apiResponse).build();

        assertNotNull(tokenResponse);
        assertEquals(tokenResponse.getStatusCode(), STATUS_CODE_200);
        assertEquals(tokenResponse.getAccessToken(), longAccessToken);
        assertEquals(tokenResponse.getRefreshToken(), longRefreshToken);
    }
}
