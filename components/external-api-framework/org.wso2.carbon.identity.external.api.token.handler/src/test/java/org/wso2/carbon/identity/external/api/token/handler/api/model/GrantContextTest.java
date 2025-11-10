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
import java.util.NoSuchElementException;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.fail;

/**
 * Unit tests for GrantContext class.
 */
public class GrantContextTest {

    private static final String CLIENT_ID = "test_client_id";
    private static final String CLIENT_SECRET = "test_client_secret";
    private static final String SCOPE = "test_scope";

    /**
     * Test successful creation of GrantContext with CLIENT_CREDENTIAL grant type.
     */
    @Test
    public void testCreateGrantContextWithClientCredential() throws TokenHandlerException {

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
     * Test creation of GrantContext with minimum required properties.
     */
    @Test
    public void testCreateGrantContextWithMinimumProperties() throws TokenHandlerException {

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
    }

    /**
     * Test creation of GrantContext without grant type - should throw exception.
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
            fail("Expected TokenHandlerException");
        } catch (TokenHandlerException e) {
            assertEquals(e.getMessage(), "Grant type must be provided for the grant context configuration.");
        }
    }

    /**
     * Test creation of GrantContext with missing client ID - should throw exception.
     */
    @Test
    public void testCreateGrantContextWithMissingClientId() {

        Map<String, String> properties = new HashMap<>();
        properties.put(GrantContext.Property.CLIENT_SECRET.getName(), CLIENT_SECRET);
        properties.put(GrantContext.Property.SCOPE.getName(), SCOPE);

        try {
            new GrantContext.Builder()
                    .grantType(GrantContext.GrantType.CLIENT_CREDENTIAL)
                    .properties(properties)
                    .build();
            fail("Expected NoSuchElementException");
        } catch (NoSuchElementException e) {
            assertEquals(e.getMessage(), "The property client_id must be provided as a property for the " +
                    "CLIENT_CREDENTIAL grant type.");
        } catch (TokenHandlerException e) {
            fail("Unexpected TokenHandlerException: " + e.getMessage());
        }
    }

    /**
     * Test creation of GrantContext with missing client secret - should throw exception.
     */
    @Test
    public void testCreateGrantContextWithMissingClientSecret() {

        Map<String, String> properties = new HashMap<>();
        properties.put(GrantContext.Property.CLIENT_ID.getName(), CLIENT_ID);
        properties.put(GrantContext.Property.SCOPE.getName(), SCOPE);

        try {
            new GrantContext.Builder()
                    .grantType(GrantContext.GrantType.CLIENT_CREDENTIAL)
                    .properties(properties)
                    .build();
            fail("Expected NoSuchElementException");
        } catch (NoSuchElementException e) {
            assertEquals(e.getMessage(), "The property client_secret must be provided as a property for the " +
                    "CLIENT_CREDENTIAL grant type.");
        } catch (TokenHandlerException e) {
            fail("Unexpected TokenHandlerException: " + e.getMessage());
        }
    }

    /**
     * Test creation of GrantContext with missing scope - should throw exception.
     */
    @Test
    public void testCreateGrantContextWithMissingScope() {

        Map<String, String> properties = new HashMap<>();
        properties.put(GrantContext.Property.CLIENT_ID.getName(), CLIENT_ID);
        properties.put(GrantContext.Property.CLIENT_SECRET.getName(), CLIENT_SECRET);

        try {
            new GrantContext.Builder()
                    .grantType(GrantContext.GrantType.CLIENT_CREDENTIAL)
                    .properties(properties)
                    .build();
            fail("Expected NoSuchElementException");
        } catch (NoSuchElementException e) {
            assertEquals(e.getMessage(), "The property scope must be provided as a property for the " +
                    "CLIENT_CREDENTIAL grant type.");
        } catch (TokenHandlerException e) {
            fail("Unexpected TokenHandlerException: " + e.getMessage());
        }
    }

    /**
     * Test creation of GrantContext with blank client ID - should throw exception.
     */
    @Test
    public void testCreateGrantContextWithBlankClientId() {

        Map<String, String> properties = new HashMap<>();
        properties.put(GrantContext.Property.CLIENT_ID.getName(), "");
        properties.put(GrantContext.Property.CLIENT_SECRET.getName(), CLIENT_SECRET);
        properties.put(GrantContext.Property.SCOPE.getName(), SCOPE);

        try {
            new GrantContext.Builder()
                    .grantType(GrantContext.GrantType.CLIENT_CREDENTIAL)
                    .properties(properties)
                    .build();
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertEquals(e.getMessage(), "The Property client_id cannot be blank.");
        } catch (TokenHandlerException e) {
            fail("Unexpected TokenHandlerException: " + e.getMessage());
        }
    }

    /**
     * Test creation of GrantContext with blank client secret - should throw exception.
     */
    @Test
    public void testCreateGrantContextWithBlankClientSecret() {

        Map<String, String> properties = new HashMap<>();
        properties.put(GrantContext.Property.CLIENT_ID.getName(), CLIENT_ID);
        properties.put(GrantContext.Property.CLIENT_SECRET.getName(), "   ");
        properties.put(GrantContext.Property.SCOPE.getName(), SCOPE);

        try {
            new GrantContext.Builder()
                    .grantType(GrantContext.GrantType.CLIENT_CREDENTIAL)
                    .properties(properties)
                    .build();
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertEquals(e.getMessage(), "The Property client_secret cannot be blank.");
        } catch (TokenHandlerException e) {
            fail("Unexpected TokenHandlerException: " + e.getMessage());
        }
    }

    /**
     * Test creation of GrantContext with null properties map.
     */
    @Test
    public void testCreateGrantContextWithNullProperties() {

        try {
            new GrantContext.Builder()
                    .grantType(GrantContext.GrantType.CLIENT_CREDENTIAL)
                    .properties(null)
                    .build();
            fail("Expected NoSuchElementException");
        } catch (NoSuchElementException e) {
            assertEquals(e.getMessage(), "The property client_id must be provided as a property for the " +
                    "CLIENT_CREDENTIAL grant type.");
        } catch (TokenHandlerException e) {
            fail("Unexpected TokenHandlerException: " + e.getMessage());
        }
    }

    /**
     * Test creation of GrantContext without setting properties.
     */
    @Test
    public void testCreateGrantContextWithoutProperties() {

        try {
            new GrantContext.Builder()
                    .grantType(GrantContext.GrantType.CLIENT_CREDENTIAL)
                    .build();
            fail("Expected NoSuchElementException");
        } catch (NoSuchElementException e) {
            assertEquals(e.getMessage(), "The property client_id must be provided as a property for the " +
                    "CLIENT_CREDENTIAL grant type.");
        } catch (TokenHandlerException e) {
            fail("Unexpected TokenHandlerException: " + e.getMessage());
        }
    }

    /**
     * Test GrantContext property names.
     */
    @Test
    public void testGrantContextPropertyNames() {

        assertEquals(GrantContext.Property.CLIENT_ID.getName(), "client_id");
        assertEquals(GrantContext.Property.CLIENT_SECRET.getName(), "client_secret");
        assertEquals(GrantContext.Property.SCOPE.getName(), "scope");
    }

    /**
     * Test accessing non-existent property returns null.
     */
    @Test
    public void testGetNonExistentProperty() throws TokenHandlerException {

        Map<String, String> properties = new HashMap<>();
        properties.put(GrantContext.Property.CLIENT_ID.getName(), CLIENT_ID);
        properties.put(GrantContext.Property.CLIENT_SECRET.getName(), CLIENT_SECRET);
        properties.put(GrantContext.Property.SCOPE.getName(), SCOPE);

        GrantContext grantContext = new GrantContext.Builder()
                .grantType(GrantContext.GrantType.CLIENT_CREDENTIAL)
                .properties(properties)
                .build();

        assertEquals(grantContext.getProperty("non_existent_property"), null);
    }

    /**
     * Test GrantType enum values.
     */
    @Test
    public void testGrantTypeEnumValues() {

        GrantContext.GrantType[] grantTypes = GrantContext.GrantType.values();
        assertEquals(grantTypes.length, 1);
        assertEquals(grantTypes[0], GrantContext.GrantType.CLIENT_CREDENTIAL);
    }
}
