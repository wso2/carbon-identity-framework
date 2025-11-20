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

    private static final String ACCESS_TOKEN = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.test";
    private static final String REFRESH_TOKEN = "refresh-token-test-value";
    private static final int SUCCESS_STATUS_CODE = 200;
    private static final int ERROR_STATUS_CODE = 400;

    /**
     * Test successful creation of TokenResponse with both access and refresh tokens.
     */
    @Test
    public void testCreateTokenResponseWithBothTokens() {

        String responseBody = String.format("{\"access_token\":\"%s\",\"refresh_token\":\"%s\"," +
                        "\"token_type\":\"Bearer\",\"expires_in\":3600}", ACCESS_TOKEN, REFRESH_TOKEN);
        APIResponse apiResponse = new APIResponse(SUCCESS_STATUS_CODE, responseBody);

        TokenResponse tokenResponse = new TokenResponse(apiResponse, ACCESS_TOKEN, REFRESH_TOKEN);

        assertNotNull(tokenResponse);
        assertEquals(tokenResponse.getStatusCode(), SUCCESS_STATUS_CODE);
        assertEquals(tokenResponse.getResponseBody(), responseBody);
        assertEquals(tokenResponse.getAccessToken(), ACCESS_TOKEN);
        assertEquals(tokenResponse.getRefreshToken(), REFRESH_TOKEN);
    }

    /**
     * Test creation of TokenResponse with only access token.
     */
    @Test
    public void testCreateTokenResponseWithAccessTokenOnly() {

        String responseBody = String.format("{\"access_token\":\"%s\",\"token_type\":\"Bearer\",\"expires_in\":3600}",
                ACCESS_TOKEN);
        APIResponse apiResponse = new APIResponse(SUCCESS_STATUS_CODE, responseBody);

        TokenResponse tokenResponse = new TokenResponse(apiResponse, ACCESS_TOKEN, null);

        assertNotNull(tokenResponse);
        assertEquals(tokenResponse.getStatusCode(), SUCCESS_STATUS_CODE);
        assertEquals(tokenResponse.getAccessToken(), ACCESS_TOKEN);
        assertNull(tokenResponse.getRefreshToken());
    }

    /**
     * Test creation of TokenResponse with only refresh token.
     */
    @Test
    public void testCreateTokenResponseWithRefreshTokenOnly() {

        String responseBody = String.format("{\"refresh_token\":\"%s\",\"token_type\":\"Bearer\",\"expires_in\":3600}",
                REFRESH_TOKEN);
        APIResponse apiResponse = new APIResponse(SUCCESS_STATUS_CODE, responseBody);

        TokenResponse tokenResponse = new TokenResponse(apiResponse, null, REFRESH_TOKEN);

        assertNotNull(tokenResponse);
        assertEquals(tokenResponse.getStatusCode(), SUCCESS_STATUS_CODE);
        assertNull(tokenResponse.getAccessToken());
        assertEquals(tokenResponse.getRefreshToken(), REFRESH_TOKEN);
    }

    /**
     * Test creation of TokenResponse without tokens.
     */
    @Test
    public void testCreateTokenResponseWithoutTokens() {

        String responseBody = "{\"token_type\":\"Bearer\",\"expires_in\":3600}";
        APIResponse apiResponse = new APIResponse(SUCCESS_STATUS_CODE, responseBody);

        TokenResponse tokenResponse = new TokenResponse(apiResponse, null, null);

        assertNotNull(tokenResponse);
        assertEquals(tokenResponse.getStatusCode(), SUCCESS_STATUS_CODE);
        assertNull(tokenResponse.getAccessToken());
        assertNull(tokenResponse.getRefreshToken());
    }

    /**
     * Test creation of TokenResponse with null response body.
     */
    @Test
    public void testCreateTokenResponseWithNullResponseBody() {

        APIResponse apiResponse = new APIResponse(SUCCESS_STATUS_CODE, null);

        TokenResponse tokenResponse = new TokenResponse(apiResponse, null, null);

        assertNotNull(tokenResponse);
        assertEquals(tokenResponse.getStatusCode(), SUCCESS_STATUS_CODE);
        assertNull(tokenResponse.getResponseBody());
        assertNull(tokenResponse.getAccessToken());
        assertNull(tokenResponse.getRefreshToken());
    }

    /**
     * Test creation of TokenResponse with invalid JSON response body.
     */
    @Test
    public void testCreateTokenResponseWithInvalidJson() {

        String responseBody = "This is not a valid JSON";
        APIResponse apiResponse = new APIResponse(ERROR_STATUS_CODE, responseBody);

        TokenResponse tokenResponse = new TokenResponse(apiResponse, null, null);

        assertNotNull(tokenResponse);
        assertEquals(tokenResponse.getStatusCode(), ERROR_STATUS_CODE);
        assertEquals(tokenResponse.getResponseBody(), responseBody);
        assertNull(tokenResponse.getAccessToken());
        assertNull(tokenResponse.getRefreshToken());
    }

    /**
     * Test creation of TokenResponse with empty JSON object.
     */
    @Test
    public void testCreateTokenResponseWithEmptyJsonObject() {

        String responseBody = "{}";
        APIResponse apiResponse = new APIResponse(SUCCESS_STATUS_CODE, responseBody);

        TokenResponse tokenResponse = new TokenResponse(apiResponse, null, null);

        assertNotNull(tokenResponse);
        assertEquals(tokenResponse.getStatusCode(), SUCCESS_STATUS_CODE);
        assertNull(tokenResponse.getAccessToken());
        assertNull(tokenResponse.getRefreshToken());
    }

    /**
     * Test creation of TokenResponse with null token values in JSON.
     */
    @Test
    public void testCreateTokenResponseWithNullTokenValues() {

        String responseBody = "{\"access_token\":null,\"refresh_token\":null}";
        APIResponse apiResponse = new APIResponse(SUCCESS_STATUS_CODE, responseBody);

        TokenResponse tokenResponse = new TokenResponse(apiResponse, null, null);

        assertNotNull(tokenResponse);
        assertEquals(tokenResponse.getStatusCode(), SUCCESS_STATUS_CODE);
        assertNull(tokenResponse.getAccessToken());
        assertNull(tokenResponse.getRefreshToken());
    }

    /**
     * Test creation of TokenResponse with JSON array instead of object.
     */
    @Test
    public void testCreateTokenResponseWithJsonArray() {

        String responseBody = "[\"value1\",\"value2\"]";
        APIResponse apiResponse = new APIResponse(SUCCESS_STATUS_CODE, responseBody);

        TokenResponse tokenResponse = new TokenResponse(apiResponse, null, null);

        assertNotNull(tokenResponse);
        assertEquals(tokenResponse.getStatusCode(), SUCCESS_STATUS_CODE);
        assertNull(tokenResponse.getAccessToken());
        assertNull(tokenResponse.getRefreshToken());
    }

    /**
     * Test creation of TokenResponse with malformed JSON.
     */
    @Test
    public void testCreateTokenResponseWithMalformedJson() {

        String responseBody = "{\"access_token\":\"" + ACCESS_TOKEN + "\",\"refresh_token\":";
        APIResponse apiResponse = new APIResponse(ERROR_STATUS_CODE, responseBody);

        TokenResponse tokenResponse = new TokenResponse(apiResponse, null, null);

        assertNotNull(tokenResponse);
        assertEquals(tokenResponse.getStatusCode(), ERROR_STATUS_CODE);
        assertNull(tokenResponse.getAccessToken());
        assertNull(tokenResponse.getRefreshToken());
    }

    /**
     * Test creation of TokenResponse with empty string response body.
     */
    @Test
    public void testCreateTokenResponseWithEmptyStringResponseBody() {

        String responseBody = "";
        APIResponse apiResponse = new APIResponse(ERROR_STATUS_CODE, responseBody);

        TokenResponse tokenResponse = new TokenResponse(apiResponse, null, null);

        assertNotNull(tokenResponse);
        assertEquals(tokenResponse.getStatusCode(), ERROR_STATUS_CODE);
        assertEquals(tokenResponse.getResponseBody(), responseBody);
        assertNull(tokenResponse.getAccessToken());
        assertNull(tokenResponse.getRefreshToken());
    }

    /**
     * Test creation of TokenResponse with error response.
     */
    @Test
    public void testCreateTokenResponseWithErrorResponse() {

        String responseBody = "{\"error\":\"invalid_client\",\"error_description\":\"Client authentication failed\"}";
        APIResponse apiResponse = new APIResponse(ERROR_STATUS_CODE, responseBody);

        TokenResponse tokenResponse = new TokenResponse(apiResponse, null, null);

        assertNotNull(tokenResponse);
        assertEquals(tokenResponse.getStatusCode(), ERROR_STATUS_CODE);
        assertEquals(tokenResponse.getResponseBody(), responseBody);
        assertNull(tokenResponse.getAccessToken());
        assertNull(tokenResponse.getRefreshToken());
    }

    /**
     * Test creation of TokenResponse with extra fields in JSON.
     */
    @Test
    public void testCreateTokenResponseWithExtraFields() {

        String responseBody = String.format("{\"access_token\":\"%s\",\"refresh_token\":\"%s\"," +
                        "\"token_type\":\"Bearer\",\"expires_in\":3600,\"scope\":\"read write\"," +
                        "\"custom_field\":\"custom_value\"}", ACCESS_TOKEN, REFRESH_TOKEN);
        APIResponse apiResponse = new APIResponse(SUCCESS_STATUS_CODE, responseBody);

        TokenResponse tokenResponse = new TokenResponse(apiResponse, ACCESS_TOKEN, REFRESH_TOKEN);

        assertNotNull(tokenResponse);
        assertEquals(tokenResponse.getStatusCode(), SUCCESS_STATUS_CODE);
        assertEquals(tokenResponse.getAccessToken(), ACCESS_TOKEN);
        assertEquals(tokenResponse.getRefreshToken(), REFRESH_TOKEN);
    }

    /**
     * Test creation of TokenResponse with empty string tokens in JSON.
     */
    @Test
    public void testCreateTokenResponseWithEmptyStringTokens() {

        String responseBody = "{\"access_token\":\"\",\"refresh_token\":\"\"}";
        APIResponse apiResponse = new APIResponse(SUCCESS_STATUS_CODE, responseBody);

        TokenResponse tokenResponse = new TokenResponse(apiResponse, "", "");

        assertNotNull(tokenResponse);
        assertEquals(tokenResponse.getStatusCode(), SUCCESS_STATUS_CODE);
        assertEquals(tokenResponse.getAccessToken(), "");
        assertEquals(tokenResponse.getRefreshToken(), "");
    }

    /**
     * Test creation of TokenResponse with whitespace in token values.
     */
    @Test
    public void testCreateTokenResponseWithWhitespaceTokens() {

        String responseBody = "{\"access_token\":\"   \",\"refresh_token\":\"   \"}";
        APIResponse apiResponse = new APIResponse(SUCCESS_STATUS_CODE, responseBody);

        TokenResponse tokenResponse = new TokenResponse(apiResponse, "   ", "   ");

        assertNotNull(tokenResponse);
        assertEquals(tokenResponse.getStatusCode(), SUCCESS_STATUS_CODE);
        assertEquals(tokenResponse.getAccessToken(), "   ");
        assertEquals(tokenResponse.getRefreshToken(), "   ");
    }

    /**
     * Test inheritance from APIResponse.
     */
    @Test
    public void testInheritanceFromAPIResponse() {

        String responseBody = String.format("{\"access_token\":\"%s\"}", ACCESS_TOKEN);
        APIResponse apiResponse = new APIResponse(SUCCESS_STATUS_CODE, responseBody);

        TokenResponse tokenResponse = new TokenResponse(apiResponse, ACCESS_TOKEN, null);

        assertNotNull(tokenResponse);
        // Verify inherited methods work correctly.
        assertEquals(tokenResponse.getStatusCode(), apiResponse.getStatusCode());
        assertEquals(tokenResponse.getResponseBody(), apiResponse.getResponseBody());
    }
}
