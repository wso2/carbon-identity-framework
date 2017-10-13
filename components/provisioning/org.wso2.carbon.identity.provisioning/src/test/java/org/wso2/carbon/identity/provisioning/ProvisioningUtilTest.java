/*
 *  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.wso2.carbon.identity.provisioning;

import org.apache.commons.collections.MapUtils;
import org.mockito.Mock;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.testng.IObjectFactory;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.ObjectFactory;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.application.common.IdentityApplicationManagementException;
import org.wso2.carbon.identity.application.common.model.Claim;
import org.wso2.carbon.identity.application.common.model.ClaimMapping;
import org.wso2.carbon.identity.application.common.util.IdentityApplicationConstants;
import org.wso2.carbon.identity.claim.metadata.mgt.ClaimMetadataHandler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.powermock.api.mockito.PowerMockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.wso2.carbon.identity.provisioning.ProvisioningEntityType.USER;
import static org.wso2.carbon.identity.provisioning.ProvisioningOperation.POST;

/**
 * Test class for ProvisioningUtil test cases.
 */
@PrepareForTest({ClaimMetadataHandler.class})
public class ProvisioningUtilTest {

    private String localClaimUri = "testLocalClaimUri";
    private String remoteClaimUri = "testRemoteClaimUri";
    private String tenantDomain = "carbon.super";
    private String outboundClaimDialect = "testOutboundClaimDialect";
    private String inboundClaimDialect = "testInboundClaimDialect";
    private String invalidDialect = "invalidDialect";

    private Map<ClaimMapping, List<String>> attributeMap, attributeMap1, attributeMap2;
    private ArrayList<String> valueList, valueList1;
    private ClaimMapping[] mappings;

    private Map<String, String> inboundClaimMap, inboundClaimMap1, inboundClaimMap2;
    private Map<ClaimMapping, List<String>> outboundClaimValueMappings;

    @Mock
    private ClaimMetadataHandler mockClaimMetadataHandler;

    @BeforeMethod
    public void setUp() throws Exception {

        setUpAttributeMaps();
        setUpInboundAndOutboundClaimMaps();

        Map<String, String> outBoundToCarbonClaimMappings = new HashMap<>();
        Map<String, String> outBoundToCarbonClaimMappings1 = new HashMap<>();
        Map<String, String> outBoundToCarbonClaimMappings2 = new HashMap<>();

        outBoundToCarbonClaimMappings.put(localClaimUri, "testOutboundUser");
        outBoundToCarbonClaimMappings.put(localClaimUri + "1", "testOutboundUser1");

        outBoundToCarbonClaimMappings1.put("testOutboundUser", "testClaimUri");

        mockClaimMetadataHandler = PowerMockito.mock(ClaimMetadataHandler.class);
        PowerMockito.mockStatic(ClaimMetadataHandler.class);
        when(ClaimMetadataHandler.getInstance()).thenReturn(mockClaimMetadataHandler);
        when(mockClaimMetadataHandler.getMappingsMapFromOtherDialectToCarbon(invalidDialect, null, tenantDomain,
                true)).thenReturn(null);
        when(mockClaimMetadataHandler
                .getMappingsMapFromOtherDialectToCarbon(IdentityApplicationConstants.WSO2CARBON_CLAIM_DIALECT, null,
                        tenantDomain, true)).thenReturn(outBoundToCarbonClaimMappings1);
        when(mockClaimMetadataHandler.getMappingsMapFromOtherDialectToCarbon(outboundClaimDialect, null, tenantDomain,
                true)).thenReturn(outBoundToCarbonClaimMappings);
        when(mockClaimMetadataHandler.getMappingsMapFromOtherDialectToCarbon(outboundClaimDialect + "1", null,
                tenantDomain, true)).thenReturn(outBoundToCarbonClaimMappings2);
    }

    private void setUpAttributeMaps() {

        attributeMap = new HashMap<>();

        mappings = new ClaimMapping[6];
        mappings[0] = ClaimMapping.build(localClaimUri, remoteClaimUri, "defaultValue", false);
        mappings[1] = ClaimMapping.build(localClaimUri + "1", remoteClaimUri + "1", null, false);
        mappings[2] = ClaimMapping.build(localClaimUri + "1", null, null, false);
        mappings[3] = ClaimMapping.build(null, null, null, false);
        mappings[4] = ClaimMapping.build(localClaimUri + "2", remoteClaimUri, null, false);

        Claim claim = new Claim();
        ClaimMapping mappingWithNullLocalClaim = new ClaimMapping();
        mappingWithNullLocalClaim.setLocalClaim(null);
        mappingWithNullLocalClaim.setRemoteClaim(claim);
        mappings[5] = mappingWithNullLocalClaim;

        valueList = new ArrayList();
        valueList.add("testuser");
        valueList.add("SECONDARY/testuser1");

        valueList1 = new ArrayList();
        valueList1.add("testuser2");

        attributeMap.put(mappings[0], valueList);
        attributeMap.put(mappings[1], new ArrayList());
        attributeMap.put(mappings[2], valueList1);
        attributeMap.put(mappings[3], valueList);
        attributeMap.put(mappings[4], valueList);

        attributeMap1 = new HashMap<>();
        attributeMap2 = new HashMap<>();
        attributeMap2.put(mappingWithNullLocalClaim, new ArrayList<String>());
    }

    private void setUpInboundAndOutboundClaimMaps() throws Exception {

        inboundClaimMap = new HashMap();
        inboundClaimMap1 = new HashMap();
        inboundClaimMap2 = new HashMap();

        inboundClaimMap.put(localClaimUri, "testuser");
        inboundClaimMap.put(localClaimUri + "1", "testuser1");
        inboundClaimMap.put(localClaimUri + "2", "testuser2");
        inboundClaimMap.put(remoteClaimUri, "testuser3");
        inboundClaimMap.put(remoteClaimUri + "1", "testuser4");

        inboundClaimMap1.put(localClaimUri, "testuser");
        inboundClaimMap1.put(localClaimUri + "1", "testuser1");

        outboundClaimValueMappings = new HashMap();
        outboundClaimValueMappings.put(mappings[0], valueList);
        outboundClaimValueMappings.put(mappings[1], valueList1);
    }

    @Test(dataProvider = "getClaimValueData")
    public void testGetClaimValues(Map<ClaimMapping, List<String>> attributeMap,
                                   String claimUri, String userStoreDomainName) throws Exception {

        assertNotNull(ProvisioningUtil.getClaimValues(attributeMap, claimUri, userStoreDomainName), "claim values " +
                "cannot be null.");
    }

    @Test
    public void testSetClaimValue() throws Exception {

        ProvisioningUtil.setClaimValue(localClaimUri + "2", attributeMap, valueList1);
        List<String> resultValues = attributeMap.get(ClaimMapping.build(localClaimUri + "2", remoteClaimUri, null,
                false));
        assertEquals(resultValues, valueList1, "attributeMap should contain the new claim value list.");
    }

    @Test
    public void testSetClaimValueWithEmptyAttributeMap() throws Exception {

        ProvisioningUtil.setClaimValue(localClaimUri + "2", attributeMap1, valueList1);
        List<String> resultValues1 = attributeMap1.get(ClaimMapping.build(localClaimUri + "2", remoteClaimUri, null,
                false));
        assertNull(resultValues1, "Result claim value should be null when attribute map is empty.");
    }

    @Test(dataProvider = "getAttributeData")
    public void testGetAttributeValue(Map attributeMap, String expected, String message) {

        ProvisioningEntity provisioningEntity = new ProvisioningEntity(USER, "testEntityName", POST,
                attributeMap);
        String value = ProvisioningUtil.getAttributeValue(provisioningEntity, localClaimUri + "1");
        assertEquals(value, expected, message);
    }

    @Test(dataProvider = "getInboundClaimData")
    public void testGetMappedClaims(String outboundClaimDialect, Map inboundClaimValueMap, Object inboundClaimMappings,
                                    Map outboundClaimValueMappings, String tenantDomain, Map expected, String message)
            throws Exception {

        Map<ClaimMapping, List<String>> result;
        result = ProvisioningUtil
                .getMappedClaims(outboundClaimDialect, inboundClaimValueMap, (ClaimMapping[]) inboundClaimMappings,
                        outboundClaimValueMappings, tenantDomain);
        assertEquals(result, expected, message);
    }

    @Test(dataProvider = "getInboundClaimData1")
    public void testGetMappedClaims1(String outboundClaimDialect,
                                     Map inboundClaimValueMap, String inboundClaimMappingDialect,
                                     Map outboundClaimValueMappings, String tenantDomain, Map expected, String message)
            throws Exception {

        Set keySet = null;
        if (MapUtils.isNotEmpty(inboundClaimValueMap)) {
            keySet = inboundClaimValueMap.keySet();
        }

        Map<String, String> claimMappings = new HashMap<>();
        claimMappings.put(localClaimUri, "testOutboundUser");
        claimMappings.put(localClaimUri + "1", "testOutboundUser1");
        claimMappings.put("test", localClaimUri);

        when(mockClaimMetadataHandler.getMappingsMapFromOtherDialectToCarbon(inboundClaimDialect,
                keySet, tenantDomain, false)).thenReturn(claimMappings);
        Map<ClaimMapping, List<String>> result;
        result = ProvisioningUtil.getMappedClaims(outboundClaimDialect, inboundClaimValueMap,
                inboundClaimMappingDialect, outboundClaimValueMappings, tenantDomain);
        assertEquals(result, expected, message);
    }

    @Test(dataProvider = "getInboundClaimData2")
    public void testGetMappedClaims2(Object outboundClaimMappings, Map inboundClaimValueMap,
                                     Object inboundClaimMappings,
                                     Map outboundClaimValueMappings, Map expected, String message) throws Exception {

        Map<ClaimMapping, List<String>> result;
        result = ProvisioningUtil.getMappedClaims((ClaimMapping[]) outboundClaimMappings,
                inboundClaimValueMap, (ClaimMapping[]) inboundClaimMappings, outboundClaimValueMappings);
        assertEquals(result, expected, message);
    }

    @Test(expectedExceptions = IdentityApplicationManagementException.class)
    public void testGetMappedClaimsForException() throws Exception {

        ClaimMapping[] mappings1 = new ClaimMapping[1];
        ClaimMapping claimMappingWithNullLocalClaim = new ClaimMapping();
        claimMappingWithNullLocalClaim.setLocalClaim(null);
        mappings1[0] = claimMappingWithNullLocalClaim;

        Set keySet = null;
        if (MapUtils.isNotEmpty(inboundClaimMap)) {
            keySet = inboundClaimMap.keySet();
        }

        Map<String, String> claimMappings = new HashMap<>();
        claimMappings.put(localClaimUri, "testOutboundUser");
        claimMappings.put(localClaimUri + "1", "testOutboundUser1");
        claimMappings.put("test", localClaimUri);
        claimMappings.put(localClaimUri, localClaimUri);

        when(mockClaimMetadataHandler.getMappingsMapFromOtherDialectToCarbon(inboundClaimDialect,
                keySet, tenantDomain, true)).thenReturn(claimMappings);
        ProvisioningUtil.getMappedClaims(mappings1, inboundClaimMap,
                inboundClaimDialect, outboundClaimValueMappings, tenantDomain);
    }

    @ObjectFactory
    public IObjectFactory getObjectFactory() {
        return new org.powermock.modules.testng.PowerMockObjectFactory();
    }

    @DataProvider(name = "getClaimValueData")
    public Object[][] claimValueData() {

        return new Object[][]{
                {attributeMap, localClaimUri, "PRIMARY"},
                {attributeMap, localClaimUri + "1", "PRIMARY"},
                {attributeMap, localClaimUri, null},
                {attributeMap2, localClaimUri, "PRIMARY"},
                {new HashMap<>(), localClaimUri, "PRIMARY"},
                {null, localClaimUri, "PRIMARY"}
        };
    }

    @DataProvider(name = "getAttributeData")
    public Object[][] AttributeData() {

        setUpAttributeMaps();

        return new Object[][]{
                {attributeMap, valueList1.get(0), "first element in the list of claim values corresponding to the " +
                        "claim URI should be returned."},
                {attributeMap1, null, "since provisioning entity's attribute map is null it should return null " +
                        "attribute value for the claim URI."}
        };
    }

    @DataProvider(name = "getInboundClaimData")
    public Object[][] InboundClaimData() {

        Map expected = new HashMap();
        expected.putAll(outboundClaimValueMappings);
        expected.put(ClaimMapping.build(remoteClaimUri, "testOutboundUser", null,
                false),
                Arrays.asList(new String[]{"testuser3"}));
        expected.put(ClaimMapping.build(remoteClaimUri + "1", "testOutboundUser1", null, false), Arrays.asList(new
                String[]{"testuser4"}));

        return new Object[][]{
                {outboundClaimDialect, inboundClaimMap, mappings, outboundClaimValueMappings, tenantDomain, expected,
                        "mapped claims should be added to outbound claim map."},
                {outboundClaimDialect, inboundClaimMap1, mappings, outboundClaimValueMappings, tenantDomain,
                        expected, "mapped claims should be added to outbound claim map."},
                {outboundClaimDialect + "1", inboundClaimMap1, mappings, outboundClaimValueMappings, tenantDomain,
                        expected, "mapped claims should be added to outbound claim map."},
                {invalidDialect, inboundClaimMap1, mappings, outboundClaimValueMappings, tenantDomain,
                        outboundClaimValueMappings, "it should return outbound claim map if an invalid dialect is " +
                        "given as outbound claim dialect, to retrieve carbon claim mappings."},
                {null, null, mappings, outboundClaimValueMappings, null, outboundClaimValueMappings, "it should " +
                        "return the outbound claim mappings if the inbound claim map is empty."}
        };
    }

    @DataProvider(name = "getInboundClaimData1")
    public Object[][] InboundClaimData1() throws Exception {

        setUpAttributeMaps();
        setUpInboundAndOutboundClaimMaps();

        Map expected = new HashMap();
        expected.putAll(outboundClaimValueMappings);
        expected.put(ClaimMapping.build(localClaimUri + "1", "testOutboundUser1", null, false),
                Arrays.asList(new String[]{"testuser1"}));
        expected.put(ClaimMapping.build(localClaimUri, "testOutboundUser", null, false),
                Arrays.asList(new String[]{"testuser"}));

        return new Object[][]{
                {outboundClaimDialect, inboundClaimMap1, IdentityApplicationConstants.WSO2CARBON_CLAIM_DIALECT,
                        outboundClaimValueMappings, tenantDomain, expected, "resulting outbound claim value map " +
                        "should contain the inbound mapped claim value."},
                {inboundClaimDialect, inboundClaimMap, outboundClaimDialect, outboundClaimValueMappings,
                        tenantDomain, expected, "resulting outbound claim value map " +
                        "should contain the inbound mapped claim value."},
                {outboundClaimDialect, inboundClaimMap, inboundClaimDialect, outboundClaimValueMappings,
                        tenantDomain, outboundClaimValueMappings, "resulting outbound claim value map " +
                        "should contain the inbound mapped claim value."},
                {outboundClaimDialect, inboundClaimMap1, inboundClaimDialect, outboundClaimValueMappings,
                        tenantDomain, outboundClaimValueMappings, "resulting outbound claim value map " +
                        "should contain the inbound mapped claim value."},
                {null, null, null, outboundClaimValueMappings, null, outboundClaimValueMappings, "no new claim " +
                        "mapping values should be added to the outbound claim value map if the inbound claim map is " +
                        "null."}
        };
    }

    @DataProvider(name = "getInboundClaimData2")
    public Object[][] InboundClaimData2() throws Exception {

        setUpAttributeMaps();
        setUpInboundAndOutboundClaimMaps();

        Map<ClaimMapping, List<String>> outboundClaimValueMappingsExpected = new HashMap();
        outboundClaimValueMappingsExpected.put(ClaimMapping.build("testRemoteClaimUri", "testRemoteClaimUri",
                "defaultValue", false), Arrays.asList(new String[]{"testuser3"}));

        ClaimMapping claimMapping = ClaimMapping.build("test", "test1", "defaultValue", false);

        ClaimMapping[] mappings1 = new ClaimMapping[1];
        mappings1[0] = claimMapping;

        Map actual = new HashMap();
        actual.putAll(outboundClaimValueMappings);

        Map expected = new HashMap();
        expected.putAll(outboundClaimValueMappings);
        expected.put(ClaimMapping.build("testRemoteClaimUri", "testRemoteClaimUri", "defaultValue", false),
                Arrays.asList(new String[]{"testuser3"}));

        Map expected2 = new HashMap();
        expected2.putAll(outboundClaimValueMappings);
        expected2.put(ClaimMapping.build("testRemoteClaimUri", "testRemoteClaimUri", "defaultValue", false),
                new ArrayList<String>());

        return new Object[][]{
                {mappings, inboundClaimMap, mappings, actual, expected, "outbound claim value map" +
                        " should contain the mapped claims."},
                {mappings, inboundClaimMap, mappings, null, outboundClaimValueMappingsExpected,
                        "if the outbound claim value " +
                                "map is null, outbound claim value map should not change."},
                {null, inboundClaimMap, mappings, actual, expected, "outbound claim value map" +
                        " should contain the mapped claims."},
                {mappings, inboundClaimMap1, mappings, actual, expected2, "outbound claim value " +
                        "map should contain the mapped claims."},
                {null, null, mappings, actual, expected2, "outbound claim value map should contain the mapped claims."}
        };
    }
}
