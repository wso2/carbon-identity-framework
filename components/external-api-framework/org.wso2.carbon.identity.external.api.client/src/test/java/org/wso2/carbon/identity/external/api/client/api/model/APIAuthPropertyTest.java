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
import org.wso2.carbon.identity.external.api.client.api.exception.APIClientRequestException;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertThrows;

/**
 * Unit tests for APIAuthProperty class.
 */
public class APIAuthPropertyTest {

    private static final String PROPERTY_NAME = "testProperty";
    private static final String PROPERTY_VALUE = "testValue";
    private static final String EMPTY_STRING = "";
    private static final String NULL_VALUE = null;

    /**
     * Test successful creation of APIAuthProperty with valid parameters.
     */
    @Test
    public void testCreateAPIAuthPropertyWithValidParameters() throws APIClientRequestException {

        APIAuthProperty authProperty = new APIAuthProperty.Builder(PROPERTY_NAME, PROPERTY_VALUE).build();

        assertNotNull(authProperty);
        assertEquals(authProperty.getName(), PROPERTY_NAME);
        assertEquals(authProperty.getValue(), PROPERTY_VALUE);
    }

    /**
     * Test creation of APIAuthProperty with empty name.
     */
    @Test
    public void testCreateAPIAuthPropertyWithEmptyName() {

        assertThrows(APIClientRequestException.class, () -> {
            new APIAuthProperty.Builder(EMPTY_STRING, PROPERTY_VALUE).build();
        });
    }

    /**
     * Test creation of APIAuthProperty with empty value.
     */
    @Test
    public void testCreateAPIAuthPropertyWithEmptyValue() throws APIClientRequestException {

        APIAuthProperty authProperty = new APIAuthProperty.Builder(PROPERTY_NAME, EMPTY_STRING).build();

        assertNotNull(authProperty);
        assertEquals(authProperty.getName(), PROPERTY_NAME);
        assertEquals(authProperty.getValue(), EMPTY_STRING);
    }

    /**
     * Test creation of APIAuthProperty with null name.
     */
    @Test
    public void testCreateAPIAuthPropertyWithNullName() {

        assertThrows(APIClientRequestException.class, () -> {
            new APIAuthProperty.Builder(NULL_VALUE, PROPERTY_VALUE).build();
        });
    }

    /**
     * Test creation of APIAuthProperty with null value.
     */
    @Test
    public void testCreateAPIAuthPropertyWithNullValue() {

        assertThrows(APIClientRequestException.class, () -> {
            new APIAuthProperty.Builder(PROPERTY_NAME, NULL_VALUE).build();
        });
    }

    /**
     * Test creation of APIAuthProperty with both name and value as null.
     */
    @Test
    public void testCreateAPIAuthPropertyWithBothNullNameAndValue() {

        assertThrows(APIClientRequestException.class, () -> {
            new APIAuthProperty.Builder(NULL_VALUE, NULL_VALUE).build();
        });
    }
}
