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
import org.wso2.carbon.identity.external.api.token.handler.api.exception.TokenHandlerException;

import java.util.HashMap;
import java.util.Map;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.fail;

/**
 * Unit tests for GrantContext class.
 */
public class GrantContextTest {

    private static final String CLIENT_ID = "test-client-id";
    private static final String CLIENT_SECRET = "test-client-secret";
    private static final String SCOPE = "test-scope";

    /**
     * Test successful creation of GrantContext with CLIENT_CREDENTIAL grant type.
     */
    @Test
    public void testCreateClientCredentialGrantContext() throws TokenHandlerException {

        Map<String, String> properties = new HashMap<>();
        properties.put(GrantContext.Property.CLIENT_ID.getName(), CLIENT_ID);
        properties.put(GrantContext.Property.CLIENT_SECRET.getName(), CLIENT_SECRET);
        properties.put(GrantContext.Property.SCOPE.getName(), SCOPE);

        GrantContext grantContext = new GrantContext.Builder()
                .grantType(GrantContext.GrantType.CLIENT_CREDENTIAL)
                .properties(properties)
                .build();

        assertNotNull(grantContext);
        assertEquals(grantContext.getGrantType(), GrantContext.GrantType.CLIENT_CREDENTIAL);
        assertEquals(grantContext.getProperty(GrantContext.Property.CLIENT_ID.getName()), CLIENT_ID);
        assertEquals(grantContext.getProperty(GrantContext.Property.CLIENT_SECRET.getName()), CLIENT_SECRET);
        assertEquals(grantContext.getProperty(GrantContext.Property.SCOPE.getName()), SCOPE);
    }

    /**
     * Test creation of GrantContext without grant type should throw exception.
     */
    @Test
    public void testCreateGrantContextWithoutGrantType() {

        Map<String, String> properties = new HashMap<>();
        properties.put(GrantContext.Property.CLIENT_ID.getName(), CLIENT_ID);
        properties.put(GrantContext.Property.CLIENT_SECRET.getName(), CLIENT_SECRET);
        properties.put(GrantContext.Property.SCOPE.getName(), SCOPE);

        try {
            new GrantContext.Builder()
                    .properties(properties)
                    .build();
            fail("Expected TokenHandlerException was not thrown.");
        } catch (TokenHandlerException e) {
            assertEquals(e.getErrorCode(), "TOKENMGT-65001");
            assertEquals(e.getDescription(), "The grant type must be provided for the GrantContext builder.");
        }
    }

    /**
     * Test creation of GrantContext without client_id should throw exception.
     */
    @Test
    public void testCreateGrantContextWithoutClientId() {

        Map<String, String> properties = new HashMap<>();
        properties.put(GrantContext.Property.CLIENT_SECRET.getName(), CLIENT_SECRET);
        properties.put(GrantContext.Property.SCOPE.getName(), SCOPE);

        try {
            new GrantContext.Builder()
                    .grantType(GrantContext.GrantType.CLIENT_CREDENTIAL)
                    .properties(properties)
                    .build();
            fail("Expected TokenHandlerException was not thrown.");
        } catch (TokenHandlerException e) {
            assertEquals(e.getErrorCode(), "TOKENMGT-65003");
            assertEquals(
                    e.getDescription(), "The property client_id must be included as an authentication property.");
        }
    }

    /**
     * Test creation of GrantContext without client_secret should throw exception.
     */
    @Test
    public void testCreateGrantContextWithoutClientSecret() {

        Map<String, String> properties = new HashMap<>();
        properties.put(GrantContext.Property.CLIENT_ID.getName(), CLIENT_ID);
        properties.put(GrantContext.Property.SCOPE.getName(), SCOPE);

        try {
            new GrantContext.Builder()
                    .grantType(GrantContext.GrantType.CLIENT_CREDENTIAL)
                    .properties(properties)
                    .build();
            fail("Expected TokenHandlerException was not thrown.");
        } catch (TokenHandlerException e) {
            assertEquals(e.getErrorCode(), "TOKENMGT-65003");
            assertEquals(
                    e.getDescription(), "The property client_secret must be included as an authentication property.");
        }
    }

    /**
     * Test creation of GrantContext without scope should throw exception.
     */
    @Test
    public void testCreateGrantContextWithoutScope() {

        Map<String, String> properties = new HashMap<>();
        properties.put(GrantContext.Property.CLIENT_ID.getName(), CLIENT_ID);
        properties.put(GrantContext.Property.CLIENT_SECRET.getName(), CLIENT_SECRET);

        try {
            new GrantContext.Builder()
                    .grantType(GrantContext.GrantType.CLIENT_CREDENTIAL)
                    .properties(properties)
                    .build();
            fail("Expected TokenHandlerException was not thrown.");
        } catch (TokenHandlerException e) {
            assertEquals(e.getErrorCode(), "TOKENMGT-65003");
            assertEquals(e.getDescription(), "The property scope must be included as an authentication property.");
        }
    }

    /**
     * Test creation of GrantContext with blank client_id should throw exception.
     */
    @Test
    public void testCreateGrantContextWithBlankClientId() {

        Map<String, String> properties = new HashMap<>();
        properties.put(GrantContext.Property.CLIENT_ID.getName(), "   ");
        properties.put(GrantContext.Property.CLIENT_SECRET.getName(), CLIENT_SECRET);
        properties.put(GrantContext.Property.SCOPE.getName(), SCOPE);

        try {
            new GrantContext.Builder()
                    .grantType(GrantContext.GrantType.CLIENT_CREDENTIAL)
                    .properties(properties)
                    .build();
            fail("Expected TokenHandlerException was not thrown.");
        } catch (TokenHandlerException e) {
            assertEquals(e.getErrorCode(), "TOKENMGT-65002");
            assertEquals(e.getDescription(), "The property client_id cannot be blank or empty.");
        }
    }

    /**
     * Test creation of GrantContext with blank client_secret should throw exception.
     */
    @Test
    public void testCreateGrantContextWithBlankClientSecret() {

        Map<String, String> properties = new HashMap<>();
        properties.put(GrantContext.Property.CLIENT_ID.getName(), CLIENT_ID);
        properties.put(GrantContext.Property.CLIENT_SECRET.getName(), "");
        properties.put(GrantContext.Property.SCOPE.getName(), SCOPE);

        try {
            new GrantContext.Builder()
                    .grantType(GrantContext.GrantType.CLIENT_CREDENTIAL)
                    .properties(properties)
                    .build();
            fail("Expected TokenHandlerException was not thrown.");
        } catch (TokenHandlerException e) {
            assertEquals(e.getErrorCode(), "TOKENMGT-65002");
            assertEquals(e.getDescription(), "The property client_secret cannot be blank or empty.");
        }
    }

    /**
     * Test creation of GrantContext with blank scope should throw exception.
     */
    @Test
    public void testCreateGrantContextWithBlankScope() {

        Map<String, String> properties = new HashMap<>();
        properties.put(GrantContext.Property.CLIENT_ID.getName(), CLIENT_ID);
        properties.put(GrantContext.Property.CLIENT_SECRET.getName(), CLIENT_SECRET);
        properties.put(GrantContext.Property.SCOPE.getName(), "");

        try {
            new GrantContext.Builder()
                    .grantType(GrantContext.GrantType.CLIENT_CREDENTIAL)
                    .properties(properties)
                    .build();
            fail("Expected TokenHandlerException was not thrown.");
        } catch (TokenHandlerException e) {
            assertEquals(e.getErrorCode(), "TOKENMGT-65002");
            assertEquals(e.getDescription(), "The property scope cannot be blank or empty.");
        }
    }

    /**
     * Test creation of GrantContext with null properties should throw exception.
     */
    @Test
    public void testCreateGrantContextWithNullProperties() {

        try {
            new GrantContext.Builder()
                    .grantType(GrantContext.GrantType.CLIENT_CREDENTIAL)
                    .properties(null)
                    .build();
            fail("Expected TokenHandlerException was not thrown.");
        } catch (TokenHandlerException e) {
            assertEquals(e.getErrorCode(), "TOKENMGT-65003");
            assertEquals(
                    e.getDescription(), "The property client_id must be included as an authentication property.");
        }
    }

    /**
     * Test builder can be reused for multiple builds.
     */
    @Test
    public void testBuilderReusability() throws TokenHandlerException {

        Map<String, String> properties = new HashMap<>();
        properties.put(GrantContext.Property.CLIENT_ID.getName(), CLIENT_ID);
        properties.put(GrantContext.Property.CLIENT_SECRET.getName(), CLIENT_SECRET);
        properties.put(GrantContext.Property.SCOPE.getName(), SCOPE);

        GrantContext.Builder builder = new GrantContext.Builder()
                .grantType(GrantContext.GrantType.CLIENT_CREDENTIAL)
                .properties(properties);

        GrantContext grantContext1 = builder.build();
        GrantContext grantContext2 = builder.build();

        assertNotNull(grantContext1);
        assertNotNull(grantContext2);
        // Verify both contexts have correct values.
        assertEquals(grantContext1.getProperty(GrantContext.Property.CLIENT_ID.getName()), CLIENT_ID);
        assertEquals(grantContext2.getProperty(GrantContext.Property.CLIENT_ID.getName()), CLIENT_ID);
    }
}
