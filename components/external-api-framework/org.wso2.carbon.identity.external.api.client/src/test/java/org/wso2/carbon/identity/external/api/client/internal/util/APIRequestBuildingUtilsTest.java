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

package org.wso2.carbon.identity.external.api.client.internal.util;

import org.apache.http.Header;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.external.api.client.api.exception.APIClientRequestException;
import org.wso2.carbon.identity.external.api.client.api.model.APIAuthentication;

import java.util.HashMap;
import java.util.Map;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;

/**
 * Unit tests for APIRequestBuildingUtils class.
 */
public class APIRequestBuildingUtilsTest {

    private static final String TEST_USERNAME = "user@domain.com";
    private static final String TEST_PASSWORD = "p@ssw0rd!#$";
    private static final String TEST_TOKEN = "test-access-token";
    private static final String TEST_API_KEY_HEADER = "X-API-Key";
    private static final String TEST_API_KEY_VALUE = "api-key-123";

    /**
     * Test building authentication header for BASIC authentication.
     */
    @Test
    public void testBuildAuthenticationHeaderForBasicAuth() throws APIClientRequestException {

        Map<String, String> authProperties = new HashMap<>();
        authProperties.put(APIAuthentication.Property.USERNAME.getName(), TEST_USERNAME);
        authProperties.put(APIAuthentication.Property.PASSWORD.getName(), TEST_PASSWORD);

        APIAuthentication authentication = new APIAuthentication.Builder()
                .authType(APIAuthentication.AuthType.BASIC)
                .properties(authProperties)
                .build();

        Header header = APIRequestBuildingUtils.buildAuthenticationHeader(authentication);

        assertNotNull(header);
        assertEquals(header.getName(), "Authorization");
        
        // Verify the Basic auth format
        assertEquals(header.getValue(), "Basic dXNlckBkb21haW4uY29tOnBAc3N3MHJkISMk");
    }

    /**
     * Test BASIC authentication UTF-8 encoding.
     */
    @Test
    public void testBuildAuthenticationHeaderForBasicAuthWithUTF8Characters() throws APIClientRequestException {

        String unicodeUsername = "użytkownik";
        String unicodePassword = "密码";

        Map<String, String> authProperties = new HashMap<>();
        authProperties.put(APIAuthentication.Property.USERNAME.getName(), unicodeUsername);
        authProperties.put(APIAuthentication.Property.PASSWORD.getName(), unicodePassword);

        APIAuthentication authentication = new APIAuthentication.Builder()
                .authType(APIAuthentication.AuthType.BASIC)
                .properties(authProperties)
                .build();

        Header header = APIRequestBuildingUtils.buildAuthenticationHeader(authentication);

        assertNotNull(header);
        assertEquals(header.getName(), "Authorization");

        // Verify UTF-8 encoding
        assertEquals(header.getValue(), "Basic dcW8eXRrb3duaWs65a+G56CB");
    }

    /**
     * Test building authentication header for BEARER authentication.
     */
    @Test
    public void testBuildAuthenticationHeaderForBearerAuth() throws APIClientRequestException {

        Map<String, String> authProperties = new HashMap<>();
        authProperties.put(APIAuthentication.Property.ACCESS_TOKEN.getName(), TEST_TOKEN);

        APIAuthentication authentication = new APIAuthentication.Builder()
                .authType(APIAuthentication.AuthType.BEARER)
                .properties(authProperties)
                .build();

        Header header = APIRequestBuildingUtils.buildAuthenticationHeader(authentication);

        assertNotNull(header);
        assertEquals(header.getName(), "Authorization");
        assertEquals(header.getValue(), "Bearer " + TEST_TOKEN);
    }

    /**
     * Test building authentication header for API_KEY authentication.
     */
    @Test
    public void testBuildAuthenticationHeaderForApiKeyAuth() throws APIClientRequestException {

        Map<String, String> authProperties = new HashMap<>();
        authProperties.put(APIAuthentication.Property.HEADER.getName(), TEST_API_KEY_HEADER);
        authProperties.put(APIAuthentication.Property.VALUE.getName(), TEST_API_KEY_VALUE);

        APIAuthentication authentication = new APIAuthentication.Builder()
                .authType(APIAuthentication.AuthType.API_KEY)
                .properties(authProperties)
                .build();

        Header header = APIRequestBuildingUtils.buildAuthenticationHeader(authentication);

        assertNotNull(header);
        assertEquals(header.getName(), TEST_API_KEY_HEADER);
        assertEquals(header.getValue(), TEST_API_KEY_VALUE);
    }

    /**
     * Test building authentication header for NONE authentication.
     */
    @Test
    public void testBuildAuthenticationHeaderForNoneAuth() throws APIClientRequestException {

        APIAuthentication authentication = new APIAuthentication.Builder()
                .authType(APIAuthentication.AuthType.NONE)
                .build();

        Header header = APIRequestBuildingUtils.buildAuthenticationHeader(authentication);

        assertNull(header);
    }
}
