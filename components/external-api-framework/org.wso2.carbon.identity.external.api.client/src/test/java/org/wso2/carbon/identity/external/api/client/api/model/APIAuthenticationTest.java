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
import org.wso2.carbon.identity.external.api.client.api.constant.ErrorMessageConstant.ErrorMessage;
import org.wso2.carbon.identity.external.api.client.api.exception.APIClientRequestException;

import java.util.HashMap;
import java.util.Map;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

/**
 * Unit tests for APIAuthentication class.
 */
public class APIAuthenticationTest {

    private static final String USERNAME = "testuser";
    private static final String PASSWORD = "testpass";
    private static final String ACCESS_TOKEN = "test-token";
    private static final String HEADER_NAME = "X-API-Key";
    private static final String HEADER_VALUE = "api-key-value";

    /**
     * Test successful creation of BASIC authentication.
     */
    @Test
    public void testCreateBasicAuthentication() throws APIClientRequestException {

        Map<String, String> authProperties = new HashMap<>();
        authProperties.put(APIAuthentication.Property.USERNAME.getName(), USERNAME);
        authProperties.put(APIAuthentication.Property.PASSWORD.getName(), PASSWORD);

        APIAuthentication authentication = new APIAuthentication.Builder()
                .authType(APIAuthentication.AuthType.BASIC)
                .properties(authProperties)
                .build();

        assertNotNull(authentication);
        assertEquals(authentication.getType(), APIAuthentication.AuthType.BASIC);
        assertEquals(authentication.getProperties().size(), 2);

        APIAuthProperty usernameProperty = authentication.getProperty(APIAuthentication.Property.USERNAME);
        assertNotNull(usernameProperty);
        assertEquals(usernameProperty.getName(), APIAuthentication.Property.USERNAME.getName());
        assertEquals(usernameProperty.getValue(), USERNAME);

        APIAuthProperty passwordProperty = authentication.getProperty(APIAuthentication.Property.PASSWORD);
        assertNotNull(passwordProperty);
        assertEquals(passwordProperty.getName(), APIAuthentication.Property.PASSWORD.getName());
        assertEquals(passwordProperty.getValue(), PASSWORD);
    }

    /**
     * Test successful creation of BEARER authentication.
     */
    @Test
    public void testCreateBearerAuthentication() throws APIClientRequestException {

        Map<String, String> authProperties = new HashMap<>();
        authProperties.put(APIAuthentication.Property.ACCESS_TOKEN.getName(), ACCESS_TOKEN);

        APIAuthentication authentication = new APIAuthentication.Builder()
                .authType(APIAuthentication.AuthType.BEARER)
                .properties(authProperties)
                .build();

        assertNotNull(authentication);
        assertEquals(authentication.getType(), APIAuthentication.AuthType.BEARER);
        assertEquals(authentication.getProperties().size(), 1);

        APIAuthProperty tokenProperty = authentication.getProperty(APIAuthentication.Property.ACCESS_TOKEN);
        assertNotNull(tokenProperty);
        assertEquals(tokenProperty.getName(), APIAuthentication.Property.ACCESS_TOKEN.getName());
        assertEquals(tokenProperty.getValue(), ACCESS_TOKEN);
    }

    /**
     * Test successful creation of API_KEY authentication.
     */
    @Test
    public void testCreateApiKeyAuthentication() throws APIClientRequestException {

        Map<String, String> authProperties = new HashMap<>();
        authProperties.put(APIAuthentication.Property.HEADER.getName(), HEADER_NAME);
        authProperties.put(APIAuthentication.Property.VALUE.getName(), HEADER_VALUE);

        APIAuthentication authentication = new APIAuthentication.Builder()
                .authType(APIAuthentication.AuthType.API_KEY)
                .properties(authProperties)
                .build();

        assertNotNull(authentication);
        assertEquals(authentication.getType(), APIAuthentication.AuthType.API_KEY);
        assertEquals(authentication.getProperties().size(), 2);

        APIAuthProperty headerProperty = authentication.getProperty(APIAuthentication.Property.HEADER);
        assertNotNull(headerProperty);
        assertEquals(headerProperty.getName(), APIAuthentication.Property.HEADER.getName());
        assertEquals(headerProperty.getValue(), HEADER_NAME);

        APIAuthProperty valueProperty = authentication.getProperty(APIAuthentication.Property.VALUE);
        assertNotNull(valueProperty);
        assertEquals(valueProperty.getName(), APIAuthentication.Property.VALUE.getName());
        assertEquals(valueProperty.getValue(), HEADER_VALUE);
    }

    /**
     * Test successful creation of NONE authentication.
     */
    @Test
    public void testCreateNoneAuthentication() throws APIClientRequestException {

        APIAuthentication authentication = new APIAuthentication.Builder()
                .authType(APIAuthentication.AuthType.NONE)
                .build();

        assertNotNull(authentication);
        assertEquals(authentication.getType(), APIAuthentication.AuthType.NONE);
        assertTrue(authentication.getProperties().isEmpty());
    }

    /**
     * Test creation with null auth type throws exception.
     */
    @Test
    public void testCreateAuthenticationWithNullAuthType() {

        try {
            new APIAuthentication.Builder().build();
            fail("Expected APIClientRequestException was not thrown");
        } catch (APIClientRequestException e) {
            assertEquals(e.getErrorCode(), ErrorMessage.ERROR_CODE_MISSING_AUTH_TYPE.getCode());
        }
    }

    /**
     * Test BASIC authentication with missing username throws exception.
     */
    @Test
    public void testCreateBasicAuthenticationWithMissingUsername() {

        Map<String, String> authProperties = new HashMap<>();
        authProperties.put(APIAuthentication.Property.PASSWORD.getName(), PASSWORD);

        try {
            new APIAuthentication.Builder()
                    .authType(APIAuthentication.AuthType.BASIC)
                    .properties(authProperties)
                    .build();
            fail("Expected APIClientRequestException was not thrown");
        } catch (APIClientRequestException e) {
            assertEquals(e.getErrorCode(), ErrorMessage.ERROR_CODE_MISSING_AUTH_PROPERTY.getCode());
        }
    }

    /**
     * Test BASIC authentication with missing password throws exception.
     */
    @Test
    public void testCreateBasicAuthenticationWithMissingPassword() {

        Map<String, String> authProperties = new HashMap<>();
        authProperties.put(APIAuthentication.Property.USERNAME.getName(), USERNAME);

        try {
            new APIAuthentication.Builder()
                    .authType(APIAuthentication.AuthType.BASIC)
                    .properties(authProperties)
                    .build();
            fail("Expected APIClientRequestException was not thrown");
        } catch (APIClientRequestException e) {
            assertEquals(e.getErrorCode(), ErrorMessage.ERROR_CODE_MISSING_AUTH_PROPERTY.getCode());
        }
    }

    /**
     * Test BASIC authentication with blank username throws exception.
     */
    @Test
    public void testCreateBasicAuthenticationWithBlankUsername() {

        Map<String, String> authProperties = new HashMap<>();
        authProperties.put(APIAuthentication.Property.USERNAME.getName(), "");
        authProperties.put(APIAuthentication.Property.PASSWORD.getName(), PASSWORD);

        try {
            new APIAuthentication.Builder()
                    .authType(APIAuthentication.AuthType.BASIC)
                    .properties(authProperties)
                    .build();
            fail("Expected APIClientRequestException was not thrown");
        } catch (APIClientRequestException e) {
            assertEquals(e.getErrorCode(), ErrorMessage.ERROR_CODE_BLANK_AUTH_PROPERTY.getCode());
        }
    }

    /**
     * Test BEARER authentication with missing access token throws exception.
     */
    @Test
    public void testCreateBearerAuthenticationWithMissingToken() {

        try {
            new APIAuthentication.Builder()
                    .authType(APIAuthentication.AuthType.BEARER)
                    .build();
            fail("Expected APIClientRequestException was not thrown");
        } catch (APIClientRequestException e) {
            assertEquals(e.getErrorCode(), ErrorMessage.ERROR_CODE_MISSING_AUTH_PROPERTY.getCode());
        }
    }

    /**
     * Test API_KEY authentication with missing header throws exception.
     */
    @Test
    public void testCreateApiKeyAuthenticationWithMissingHeader() {

        Map<String, String> authProperties = new HashMap<>();
        authProperties.put(APIAuthentication.Property.VALUE.getName(), HEADER_VALUE);

        try {
            new APIAuthentication.Builder()
                    .authType(APIAuthentication.AuthType.API_KEY)
                    .properties(authProperties)
                    .build();
            fail("Expected APIClientRequestException was not thrown");
        } catch (APIClientRequestException e) {
            assertEquals(e.getErrorCode(), ErrorMessage.ERROR_CODE_MISSING_AUTH_PROPERTY.getCode());
        }
    }

    /**
     * Test getProperty returns null for non-existent property.
     */
    @Test
    public void testGetPropertyReturnsNullForNonExistentProperty() throws APIClientRequestException {

        APIAuthentication authentication = new APIAuthentication.Builder()
                .authType(APIAuthentication.AuthType.NONE)
                .build();

        APIAuthProperty property = authentication.getProperty(APIAuthentication.Property.USERNAME);
        assertNull(property);
    }
}
