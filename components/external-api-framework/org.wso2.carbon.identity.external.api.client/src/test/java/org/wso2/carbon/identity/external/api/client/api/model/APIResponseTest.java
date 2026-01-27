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
    private static final String RESPONSE_BODY = "{\"message\":\"success\"}";
    private static final String EMPTY_RESPONSE_BODY = "";
    private static final String NULL_RESPONSE_BODY = null;

    /**
     * Test successful creation of APIResponse with valid parameters.
     */
    @Test
    public void testCreateAPIResponseWithValidParameters() {

        APIResponse response = new APIResponse(STATUS_CODE_200, RESPONSE_BODY);

        assertNotNull(response);
        assertEquals(response.getStatusCode(), STATUS_CODE_200);
        assertEquals(response.getResponseBody(), RESPONSE_BODY);
    }

    /**
     * Test creation of APIResponse with empty response body.
     */
    @Test
    public void testCreateAPIResponseWithEmptyResponseBody() {

        APIResponse response = new APIResponse(STATUS_CODE_200, EMPTY_RESPONSE_BODY);

        assertNotNull(response);
        assertEquals(response.getStatusCode(), STATUS_CODE_200);
        assertEquals(response.getResponseBody(), EMPTY_RESPONSE_BODY);
    }

    /**
     * Test creation of APIResponse with null response body.
     */
    @Test
    public void testCreateAPIResponseWithNullResponseBody() {

        APIResponse response = new APIResponse(STATUS_CODE_200, NULL_RESPONSE_BODY);

        assertNotNull(response);
        assertEquals(response.getStatusCode(), STATUS_CODE_200);
        assertNull(response.getResponseBody());
    }

    /**
     * Test creation of APIResponse with zero status code.
     */
    @Test
    public void testCreateAPIResponseWithZeroStatusCode() {

        APIResponse response = new APIResponse(0, RESPONSE_BODY);

        assertNotNull(response);
        assertEquals(response.getStatusCode(), 0);
        assertEquals(response.getResponseBody(), RESPONSE_BODY);
    }

    /**
     * Test creation of APIResponse with negative status code.
     */
    @Test
    public void testCreateAPIResponseWithNegativeStatusCode() {

        APIResponse response = new APIResponse(-1, RESPONSE_BODY);

        assertNotNull(response);
        assertEquals(response.getStatusCode(), -1);
        assertEquals(response.getResponseBody(), RESPONSE_BODY);
    }
}
