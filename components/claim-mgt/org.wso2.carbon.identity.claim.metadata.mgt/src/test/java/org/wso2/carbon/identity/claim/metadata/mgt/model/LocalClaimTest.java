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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;
import static org.testng.Assert.assertNull;

/**
 * Unit test class for LocalClaim.
 */
public class LocalClaimTest {

    /**
     * Test to verify that two equal LocalClaim objects are considered equal.
     */
    @Test
    public void equalsReturnsTrueForEqualLocalClaims() {

        List<AttributeMapping> mappedAttributes = new ArrayList<>();
        mappedAttributes.add(new AttributeMapping("PRIMARY", "email"));

        Map<String, String> claimProperties = new HashMap<>();
        claimProperties.put("Description", "TestDescription");

        LocalClaim localClaim1 = new LocalClaim("http://wso2.org/claims/email", mappedAttributes, claimProperties);
        LocalClaim localClaim2 = new LocalClaim("http://wso2.org/claims/email", mappedAttributes, claimProperties);

        assertEquals(localClaim1, localClaim2, "Equal LocalClaim objects should be considered equal");
    }

    /**
     * Test to verify that LocalClaim objects with different claim URIs are not equal.
     */
    @Test
    public void equalsReturnsFalseForDifferentClaimURI() {

        LocalClaim localClaim1 = new LocalClaim("http://wso2.org/claims/email");
        LocalClaim localClaim2 = new LocalClaim("http://wso2.org/claims/username");

        assertNotEquals(localClaim1, localClaim2, "LocalClaims with different claim URIs should not be equal");
    }

    /**
     * Test to verify that LocalClaim objects with different mapped attributes are not equal.
     */
    @Test
    public void equalsReturnsFalseForDifferentMappedAttributes() {

        List<AttributeMapping> mappedAttributes1 = new ArrayList<>();
        mappedAttributes1.add(new AttributeMapping("PRIMARY", "email"));

        List<AttributeMapping> mappedAttributes2 = new ArrayList<>();
        mappedAttributes2.add(new AttributeMapping("SECONDARY", "username"));

        LocalClaim localClaim1 = new LocalClaim("http://wso2.org/claims/email", mappedAttributes1, null);
        LocalClaim localClaim2 = new LocalClaim("http://wso2.org/claims/email", mappedAttributes2, null);

        assertNotEquals(localClaim1, localClaim2, "LocalClaims with different mapped attributes should not be equal");
    }

    /**
     * Test to verify that LocalClaim objects with different claim properties are not equal.
     */
    @Test
    public void equalsReturnsFalseForDifferentClaimProperties() {

        Map<String, String> claimProperties1 = new HashMap<>();
        claimProperties1.put("Description", "TestDescription1");

        Map<String, String> claimProperties2 = new HashMap<>();
        claimProperties2.put("Description", "TestDescription2");

        LocalClaim localClaim1 = new LocalClaim("http://wso2.org/claims/email", null, claimProperties1);
        LocalClaim localClaim2 = new LocalClaim("http://wso2.org/claims/email", null, claimProperties2);

        assertNotEquals(localClaim1, localClaim2, "LocalClaims with different claim properties should not be equal");
    }

    /**
     * Test to verify that getMappedAttribute returns the correct value for an existing domain.
     */
    @Test
    public void getMappedAttributeReturnsCorrectValue() {

        List<AttributeMapping> mappedAttributes = new ArrayList<>();
        mappedAttributes.add(new AttributeMapping("PRIMARY", "email"));

        LocalClaim localClaim = new LocalClaim("http://wso2.org/claims/email", mappedAttributes, null);

        assertEquals(localClaim.getMappedAttribute("PRIMARY"), "email", "Mapped attribute should return the correct value");
    }

    /**
     * Test to verify that getMappedAttribute returns null for a non-existent domain.
     */
    @Test
    public void getMappedAttributeReturnsNullForNonExistentDomain() {

        List<AttributeMapping> mappedAttributes = new ArrayList<>();
        mappedAttributes.add(new AttributeMapping("PRIMARY", "email"));

        LocalClaim localClaim = new LocalClaim("http://wso2.org/claims/email", mappedAttributes, null);

        assertNull(localClaim.getMappedAttribute("SECONDARY"), "Mapped attribute should return null for a non-existent domain");
    }

    /**
     * Test to verify that setMappedAttributes updates the mapped attributes correctly.
     */
    @Test
    public void setMappedAttributesUpdatesAttributes() {

        List<AttributeMapping> mappedAttributes = new ArrayList<>();
        mappedAttributes.add(new AttributeMapping("PRIMARY", "email"));

        LocalClaim localClaim = new LocalClaim("http://wso2.org/claims/email");
        localClaim.setMappedAttributes(mappedAttributes);

        assertEquals(localClaim.getMappedAttributes(), mappedAttributes, "Mapped attributes should be updated correctly");
    }

    /**
     * Test to verify that setMappedAttribute adds a single mapped attribute correctly.
     */
    @Test
    public void setMappedAttributeAddsSingleAttribute() {

        LocalClaim localClaim = new LocalClaim("http://wso2.org/claims/email");
        localClaim.setMappedAttribute(new AttributeMapping("PRIMARY", "email"));

        assertEquals(localClaim.getMappedAttributes().size(), 1, "Mapped attribute should be added correctly");
        assertEquals(localClaim.getMappedAttributes().get(0).getAttributeName(), "email", "Mapped attribute name should match");
    }
}
