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

package org.wso2.carbon.identity.claim.metadata.mgt.model;

import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.Map;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;
import static org.testng.Assert.assertNull;

/**
 * Unit test class for Claim.
 */
public class ClaimTest {

    /**
     * Test to verify that two equal Claim objects are considered equal.
     */
    @Test
    public void equalsReturnsTrueForEqualClaims() {

        Claim claim1 = new Claim("http://wso2.org/claims", "http://wso2.org/claims/email");
        Claim claim2 = new Claim("http://wso2.org/claims", "http://wso2.org/claims/email");

        assertEquals(claim1, claim2, "Equal Claim objects should be considered equal");
    }

    /**
     * Test to verify that Claim objects with different dialect URIs are not equal.
     */
    @Test
    public void equalsReturnsFalseForDifferentClaimDialectURI() {

        Claim claim1 = new Claim("http://wso2.org/claims", "http://wso2.org/claims/email");
        Claim claim2 = new Claim("http://example.org/claims", "http://wso2.org/claims/email");

        assertNotEquals(claim1, claim2, "Claims with different dialect URIs should not be equal");
    }

    /**
     * Test to verify that Claim objects with different claim URIs are not equal.
     */
    @Test
    public void equalsReturnsFalseForDifferentClaimURI() {

        Claim claim1 = new Claim("http://wso2.org/claims", "http://wso2.org/claims/email");
        Claim claim2 = new Claim("http://wso2.org/claims", "http://wso2.org/claims/username");

        assertNotEquals(claim1, claim2, "Claims with different claim URIs should not be equal");
    }

    /**
     * Test to verify that Claim objects with different properties are not equal.
     */
    @Test
    public void equalsReturnsFalseForDifferentClaimProperties() {

        Map<String, String> properties1 = new HashMap<>();
        properties1.put("key1", "value1");

        Map<String, String> properties2 = new HashMap<>();
        properties2.put("key2", "value2");

        Claim claim1 = new Claim("http://wso2.org/claims", "http://wso2.org/claims/email", properties1);
        Claim claim2 = new Claim("http://wso2.org/claims", "http://wso2.org/claims/email", properties2);

        assertNotEquals(claim1, claim2, "Claims with different properties should not be equal");
    }

    /**
     * Test to verify that Claim is not equal to null.
     */
    @Test
    public void equalsReturnsFalseForNullObject() {

        Claim claim = new Claim("http://wso2.org/claims", "http://wso2.org/claims/email");

        assertNotEquals(claim, null, "Claim should not be equal to null");
    }

    /**
     * Test to verify that Claim is not equal to an object of a different type.
     */
    @Test
    public void equalsReturnsFalseForDifferentObjectType() {

        Claim claim = new Claim("http://wso2.org/claims", "http://wso2.org/claims/email");

        assertNotEquals(claim, "SomeString", "Claim should not be equal to an object of a different type");
    }

    /**
     * Test to verify that getClaimDialectURI returns the correct value.
     */
    @Test
    public void getClaimDialectURIReturnsCorrectValue() {

        Claim claim = new Claim("http://wso2.org/claims", "http://wso2.org/claims/email");

        assertEquals(claim.getClaimDialectURI(), "http://wso2.org/claims", "Claim dialect URI should match the provided value");
    }

    /**
     * Test to verify that getClaimURI returns the correct value.
     */
    @Test
    public void getClaimURIReturnsCorrectValue() {

        Claim claim = new Claim("http://wso2.org/claims", "http://wso2.org/claims/email");

        assertEquals(claim.getClaimURI(), "http://wso2.org/claims/email", "Claim URI should match the provided value");
    }

    /**
     * Test to verify that getClaimProperties returns the correct value.
     */
    @Test
    public void getClaimPropertiesReturnsCorrectValue() {

        Map<String, String> properties = new HashMap<>();
        properties.put("key1", "value1");

        Claim claim = new Claim("http://wso2.org/claims", "http://wso2.org/claims/email", properties);

        assertEquals(claim.getClaimProperties(), properties, "Claim properties should match the provided value");
    }

    /**
     * Test to verify that getClaimProperty returns the correct value for an existing key.
     */
    @Test
    public void getClaimPropertyReturnsCorrectValue() {

        Map<String, String> properties = new HashMap<>();
        properties.put("key1", "value1");

        Claim claim = new Claim("http://wso2.org/claims", "http://wso2.org/claims/email", properties);

        assertEquals(claim.getClaimProperty("key1"), "value1", "Claim property value should match the provided value");
    }

    /**
     * Test to verify that getClaimProperty returns null for a non-existent key.
     */
    @Test
    public void getClaimPropertyReturnsNullForNonExistentKey() {

        Claim claim = new Claim("http://wso2.org/claims", "http://wso2.org/claims/email");

        assertNull(claim.getClaimProperty("nonExistentKey"), "Claim property value should be null for a non-existent key");
    }

    /**
     * Test to verify that setClaimProperties updates the properties correctly.
     */
    @Test
    public void setClaimPropertiesUpdatesProperties() {

        Map<String, String> properties = new HashMap<>();
        properties.put("key1", "value1");

        Claim claim = new Claim("http://wso2.org/claims", "http://wso2.org/claims/email");
        claim.setClaimProperties(properties);

        assertEquals(claim.getClaimProperties(), properties, "Claim properties should be updated correctly");
    }

    /**
     * Test to verify that setClaimProperty updates a single property correctly.
     */
    @Test
    public void setClaimPropertyUpdatesSingleProperty() {

        Claim claim = new Claim("http://wso2.org/claims", "http://wso2.org/claims/email");
        claim.setClaimProperty("key1", "value1");

        assertEquals(claim.getClaimProperty("key1"), "value1", "Claim property should be updated correctly");
    }
}
