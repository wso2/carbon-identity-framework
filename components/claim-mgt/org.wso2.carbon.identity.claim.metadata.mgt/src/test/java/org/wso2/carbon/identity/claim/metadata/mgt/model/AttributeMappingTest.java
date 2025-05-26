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

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotEquals;

/**
 * Unit test class for AttributeMapping.
 */
public class AttributeMappingTest {

    /**
     * Test to verify that two equal AttributeMapping objects are considered equal.
     */
    @Test
    public void testEqualsReturnsTrueForEqualObjects() {

        AttributeMapping mapping1 = new AttributeMapping("PRIMARY", "email");
        AttributeMapping mapping2 = new AttributeMapping("PRIMARY", "email");

        assertEquals(mapping1, mapping2, "Equal AttributeMapping objects should be considered equal");
    }

    /**
     * Test to verify that AttributeMapping objects with different user store domains are not equal.
     */
    @Test
    public void testEqualsReturnsFalseForDifferentUserStoreDomains() {

        AttributeMapping mapping1 = new AttributeMapping("PRIMARY", "email");
        AttributeMapping mapping2 = new AttributeMapping("SECONDARY", "email");

        assertNotEquals(mapping1, mapping2,
                "AttributeMapping objects with different user store domains should not be equal");
    }

    /**
     * Test to verify that AttributeMapping objects with different attribute names are not equal.
     */
    @Test
    public void testEqualsReturnsFalseForDifferentAttributeNames() {

        AttributeMapping mapping1 = new AttributeMapping("PRIMARY", "email");
        AttributeMapping mapping2 = new AttributeMapping("PRIMARY", "username");

        assertNotEquals(mapping1, mapping2,
                "AttributeMapping objects with different attribute names should not be equal");
    }

    /**
     * Test to verify that AttributeMapping is not equal to null.
     */
    @Test
    public void testEqualsReturnsFalseForNullObject() {

        AttributeMapping mapping = new AttributeMapping("PRIMARY", "email");

        assertNotEquals(mapping, null, "AttributeMapping should not be equal to null");
    }

    /**
     * Test to verify that AttributeMapping is not equal to an object of a different type.
     */
    @Test
    public void testEqualsReturnsFalseForDifferentObjectType() {

        AttributeMapping mapping = new AttributeMapping("PRIMARY", "email");

        assertFalse(mapping.equals("SomeString"),
                "AttributeMapping should not be equal to an object of a different type");
    }

    /**
     * Test to verify that the user store domain is returned in uppercase.
     */
    @Test
    public void testGetUserStoreDomainReturnsUpperCaseValue() {

        AttributeMapping mapping = new AttributeMapping("primary", "email");

        assertEquals(mapping.getUserStoreDomain(), "PRIMARY",
                "User store domain should be converted to uppercase");
    }

    /**
     * Test to verify that the attribute name is returned correctly.
     */
    @Test
    public void testGetAttributeNameReturnsCorrectValue() {

        AttributeMapping mapping = new AttributeMapping("PRIMARY", "email");

        assertEquals(mapping.getAttributeName(), "email",
                "Attribute name should match the provided value");
    }

    /**
     * Test to verify that AttributeMapping is not equal to a completely different object.
     */
    @Test
    public void testEqualsReturnsFalseForDifferentObject() {

        AttributeMapping mapping1 = new AttributeMapping("PRIMARY", "email");
        Object obj = new Object();

        assertNotEquals(obj, mapping1,
                "AttributeMapping should not be equal to an object of a different type");
    }
}
