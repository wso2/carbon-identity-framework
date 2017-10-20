/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.identity.provisioning;

import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.application.common.model.Claim;
import org.wso2.carbon.identity.application.common.model.ClaimMapping;
import org.wso2.carbon.identity.application.common.model.Property;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.MockitoAnnotations.initMocks;

/**
 * Test class for AbstractOutboundProvisioningConnector test cases.
 */
public class AbstractOutboundProvisioningConnectorTest {

    private AbstractOutboundProvisioningConnector connector;
    private Map<ClaimMapping, List<String>> attributeMap;

    @BeforeMethod
    public void setUp() {

        initMocks(this);
        connector = new AbstractOutboundProvisioningConnector() {

            @Override
            public void init(Property[] provisioningProperties) throws IdentityProvisioningException {

            }

            @Override
            public ProvisionedIdentifier provision(ProvisioningEntity provisioningEntity)
                    throws IdentityProvisioningException {

                return null;
            }
        };

        attributeMap = new HashMap<>();
        attributeMap.put(ClaimMapping.build("testLocalClaim", "testRemoteClaim",
                null, false), Arrays.asList("remoteClaimValue"));
        attributeMap.put(ClaimMapping.build("testLocalClaim1", null,
                null, false), new ArrayList<String>());
        ClaimMapping mapping = new ClaimMapping();
        mapping.setRemoteClaim(null);
        mapping.setLocalClaim(new Claim());
        attributeMap.put(mapping, new ArrayList<String>());
        attributeMap.put(ClaimMapping.build(IdentityProvisioningConstants.GROUP_CLAIM_URI, IdentityProvisioningConstants
                .GROUP_CLAIM_URI, null, false), Arrays.asList("testGroupName"));
        attributeMap.put(ClaimMapping.build(IdentityProvisioningConstants.USERNAME_CLAIM_URI,
                IdentityProvisioningConstants.USERNAME_CLAIM_URI,
                null, false), Arrays.asList("testUserName"));
        attributeMap.put(ClaimMapping.build(IdentityProvisioningConstants.PASSWORD_CLAIM_URI,
                IdentityProvisioningConstants.PASSWORD_CLAIM_URI,
                null, false), Arrays.asList("testPassword1", "testPassword2"));
        attributeMap.put(ClaimMapping.build("testLocalClaim", "testRemoteClaim1",
                null, false), Arrays.asList("testValue1", "testValue2"));
        attributeMap.put(ClaimMapping.build("testLocalClaim2", "testRemoteClaim1",
                null, false), Arrays.asList(null, "testValue2"));
    }

    @Test
    public void testGetSingleValuedClaims() {

        Map<String, String> claimValues = connector.getSingleValuedClaims(attributeMap);
        Assert.assertTrue(claimValues.containsValue("remoteClaimValue"), "Single claim value list should contain " +
                "'remoteClaimValue'.");
    }

    @Test
    public void testGetClaimDialectUri() throws Exception {

        Assert.assertNull(connector.getClaimDialectUri(), "Claim Dialect URI should be null by default.");
    }

    @Test
    public void testGetUserNames() {

        List result = connector.getUserNames(attributeMap);
        Assert.assertTrue(result.contains("testUserName"), "User names should contain 'testUserName'.");
    }

    @Test
    public void testGetGroupNames() {

        List result = connector.getGroupNames(attributeMap);
        Assert.assertTrue(result.contains("testGroupName"), "Group names should contain 'testGroupName'.");
    }

    @Test
    public void testGetPassword() {

        Assert.assertEquals(connector.getPassword(attributeMap), "testPassword1", "It should return the " +
                "corresponding claim value for the password claim.");
    }

    @Test
    public void testGetPasswordWithEmptyAttributeMap() {

        Map<ClaimMapping, List<String>> attributeMap1 = new HashMap<>();
        attributeMap1.put(ClaimMapping.build("testLocalClaim1", IdentityProvisioningConstants.PASSWORD_CLAIM_URI,
                null, false), Arrays.asList("testPassword1", "testPassword2"));
        Assert.assertNotNull(connector.getPassword(attributeMap1), "It should return a random generated string for " +
                "the password claim if the attribute map is empty.");
    }

    @Test
    public void testIsJitProvisioningEnabled() throws Exception {

        Assert.assertFalse(connector.isJitProvisioningEnabled(), "JIT provisioning should be disabled by default.");
    }
}
