/*
 * Copyright (c) 2024, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.carbon.identity.entitlement.persistence;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.entitlement.EntitlementException;
import org.wso2.carbon.identity.entitlement.PDPConstants;
import org.wso2.carbon.identity.entitlement.dto.AttributeDTO;
import org.wso2.carbon.identity.entitlement.dto.PolicyDTO;
import org.wso2.carbon.identity.entitlement.dto.PolicyStoreDTO;
import org.wso2.carbon.identity.entitlement.internal.EntitlementConfigHolder;
import org.wso2.carbon.identity.entitlement.policy.finder.PolicyFinderModule;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertThrows;
import static org.testng.Assert.assertTrue;

/**
 * This is the parent test class for the Policy Persistence Manager test classes.
 */
public abstract class PolicyPersistenceManagerTest {

    PolicyPersistenceManager policyPersistenceManager;

    static final String SAMPLE_POLICY_STRING_1 =
            "<Policy xmlns=\"urn:oasis:names:tc:xacml:3.0:core:schema:wd-17\"  PolicyId=\"sample_policy1\" RuleCombiningAlgId=\"urn:oasis:names:tc:xacml:1.0:rule-combining-algorithm:first-applicable\" Version=\"1.0\"><Target><AnyOf><AllOf><Match MatchId=\"urn:oasis:names:tc:xacml:1.0:function:string-equal\"><AttributeValue DataType=\"http://www.w3.org/2001/XMLSchema#string\">GET</AttributeValue><AttributeDesignator AttributeId=\"urn:oasis:names:tc:xacml:1.0:action:action-id\" Category=\"urn:oasis:names:tc:xacml:3.0:attribute-category:action\" DataType=\"http://www.w3.org/2001/XMLSchema#string\" MustBePresent=\"true\"></AttributeDesignator></Match><Match MatchId=\"urn:oasis:names:tc:xacml:1.0:function:string-equal\"><AttributeValue DataType=\"http://www.w3.org/2001/XMLSchema#string\">resourceA</AttributeValue><AttributeDesignator AttributeId=\"urn:oasis:names:tc:xacml:1.0:resource:resource-id\" Category=\"urn:oasis:names:tc:xacml:3.0:attribute-category:resource\" DataType=\"http://www.w3.org/2001/XMLSchema#string\" MustBePresent=\"true\"></AttributeDesignator></Match></AllOf></AnyOf></Target><Rule Effect=\"Permit\" RuleId=\"rule1\"><Condition><Apply FunctionId=\"urn:oasis:names:tc:xacml:1.0:function:and\"><Apply FunctionId=\"urn:oasis:names:tc:xacml:1.0:function:string-equal\"><Apply FunctionId=\"urn:oasis:names:tc:xacml:1.0:function:string-one-and-only\"><AttributeDesignator AttributeId=\"http://wso2.org/claims/country\" Category=\"http://wso2.org/identity/user\" DataType=\"http://www.w3.org/2001/XMLSchema#string\" MustBePresent=\"true\"></AttributeDesignator></Apply><AttributeValue DataType=\"http://www.w3.org/2001/XMLSchema#string\">Sri Lanka</AttributeValue></Apply><Apply FunctionId=\"urn:oasis:names:tc:xacml:1.0:function:string-is-in\"><AttributeValue DataType=\"http://www.w3.org/2001/XMLSchema#string\">Engineer</AttributeValue><AttributeDesignator AttributeId=\"http://wso2.org/claims/role\" Category=\"urn:oasis:names:tc:xacml:1.0:subject-category:access-subject\" DataType=\"http://www.w3.org/2001/XMLSchema#string\" MustBePresent=\"true\"></AttributeDesignator></Apply></Apply></Condition></Rule><Rule Effect=\"Deny\" RuleId=\"rule2\"></Rule></Policy>";
    static final String SAMPLE_POLICY_STRING_2 =
            "<Policy xmlns=\"urn:oasis:names:tc:xacml:3.0:core:schema:wd-17\"  PolicyId=\"sample_policy2\" RuleCombiningAlgId=\"urn:oasis:names:tc:xacml:1.0:rule-combining-algorithm:first-applicable\" Version=\"1.0\"><Target><AnyOf><AllOf><Match MatchId=\"urn:oasis:names:tc:xacml:1.0:function:string-equal\"><AttributeValue DataType=\"http://www.w3.org/2001/XMLSchema#string\">GET</AttributeValue><AttributeDesignator AttributeId=\"urn:oasis:names:tc:xacml:1.0:action:action-id\" Category=\"urn:oasis:names:tc:xacml:3.0:attribute-category:action\" DataType=\"http://www.w3.org/2001/XMLSchema#string\" MustBePresent=\"true\"></AttributeDesignator></Match><Match MatchId=\"urn:oasis:names:tc:xacml:1.0:function:string-equal\"><AttributeValue DataType=\"http://www.w3.org/2001/XMLSchema#string\">resourceA</AttributeValue><AttributeDesignator AttributeId=\"urn:oasis:names:tc:xacml:1.0:resource:resource-id\" Category=\"urn:oasis:names:tc:xacml:3.0:attribute-category:resource\" DataType=\"http://www.w3.org/2001/XMLSchema#string\" MustBePresent=\"true\"></AttributeDesignator></Match></AllOf></AnyOf></Target><Rule Effect=\"Permit\" RuleId=\"rule1\"><Condition><Apply FunctionId=\"urn:oasis:names:tc:xacml:1.0:function:and\"><Apply FunctionId=\"urn:oasis:names:tc:xacml:1.0:function:string-equal\"><Apply FunctionId=\"urn:oasis:names:tc:xacml:1.0:function:string-one-and-only\"><AttributeDesignator AttributeId=\"http://wso2.org/claims/country\" Category=\"http://wso2.org/identity/user\" DataType=\"http://www.w3.org/2001/XMLSchema#string\" MustBePresent=\"true\"></AttributeDesignator></Apply><AttributeValue DataType=\"http://www.w3.org/2001/XMLSchema#string\">India</AttributeValue></Apply><Apply FunctionId=\"urn:oasis:names:tc:xacml:1.0:function:string-is-in\"><AttributeValue DataType=\"http://www.w3.org/2001/XMLSchema#string\">Engineer</AttributeValue><AttributeDesignator AttributeId=\"http://wso2.org/claims/role\" Category=\"urn:oasis:names:tc:xacml:1.0:subject-category:access-subject\" DataType=\"http://www.w3.org/2001/XMLSchema#string\" MustBePresent=\"true\"></AttributeDesignator></Apply></Apply></Condition></Rule><Rule Effect=\"Deny\" RuleId=\"rule2\"></Rule></Policy>";
    static final String SAMPLE_POLICY_STRING_3 =
            "<Policy xmlns=\"urn:oasis:names:tc:xacml:3.0:core:schema:wd-17\"  PolicyId=\"sample_policy3\" RuleCombiningAlgId=\"urn:oasis:names:tc:xacml:1.0:rule-combining-algorithm:first-applicable\" Version=\"1.0\"><Target><AnyOf><AllOf><Match MatchId=\"urn:oasis:names:tc:xacml:1.0:function:string-equal\"><AttributeValue DataType=\"http://www.w3.org/2001/XMLSchema#string\">GET</AttributeValue><AttributeDesignator AttributeId=\"urn:oasis:names:tc:xacml:1.0:action:action-id\" Category=\"urn:oasis:names:tc:xacml:3.0:attribute-category:action\" DataType=\"http://www.w3.org/2001/XMLSchema#string\" MustBePresent=\"true\"></AttributeDesignator></Match><Match MatchId=\"urn:oasis:names:tc:xacml:1.0:function:string-equal\"><AttributeValue DataType=\"http://www.w3.org/2001/XMLSchema#string\">resourceA</AttributeValue><AttributeDesignator AttributeId=\"urn:oasis:names:tc:xacml:1.0:resource:resource-id\" Category=\"urn:oasis:names:tc:xacml:3.0:attribute-category:resource\" DataType=\"http://www.w3.org/2001/XMLSchema#string\" MustBePresent=\"true\"></AttributeDesignator></Match></AllOf></AnyOf></Target><Rule Effect=\"Permit\" RuleId=\"rule1\"><Condition><Apply FunctionId=\"urn:oasis:names:tc:xacml:1.0:function:and\"><Apply FunctionId=\"urn:oasis:names:tc:xacml:1.0:function:string-equal\"><Apply FunctionId=\"urn:oasis:names:tc:xacml:1.0:function:string-one-and-only\"><AttributeDesignator AttributeId=\"http://wso2.org/claims/country\" Category=\"http://wso2.org/identity/user\" DataType=\"http://www.w3.org/2001/XMLSchema#string\" MustBePresent=\"true\"></AttributeDesignator></Apply><AttributeValue DataType=\"http://www.w3.org/2001/XMLSchema#string\">India</AttributeValue></Apply><Apply FunctionId=\"urn:oasis:names:tc:xacml:1.0:function:string-is-in\"><AttributeValue DataType=\"http://www.w3.org/2001/XMLSchema#string\">Doctor</AttributeValue><AttributeDesignator AttributeId=\"http://wso2.org/claims/role\" Category=\"urn:oasis:names:tc:xacml:1.0:subject-category:access-subject\" DataType=\"http://www.w3.org/2001/XMLSchema#string\" MustBePresent=\"true\"></AttributeDesignator></Apply></Apply></Condition></Rule><Rule Effect=\"Deny\" RuleId=\"rule2\"></Rule></Policy>";

    static final String SAMPLE_POLICY_ID_1 = "sample_policy1";
    static final String SAMPLE_POLICY_ID_2 = "sample_policy2";
    static final String SAMPLE_POLICY_ID_3 = "sample_policy3";

    PolicyDTO samplePAPPolicy1;
    PolicyDTO samplePAPPolicy2;
    PolicyDTO samplePAPPolicy3;
    PolicyDTO sampleUpdatedPAPPolicy1;
    PolicyDTO papPolicyWithEmptyPolicyId;

    PolicyStoreDTO samplePDPPolicy1;
    PolicyStoreDTO samplePDPPolicy2;
    PolicyStoreDTO samplePDPPolicy3;
    PolicyStoreDTO orderedSamplePDPPolicy1;
    PolicyStoreDTO inactiveSamplePDPPolicy1;
    PolicyStoreDTO updatedSamplePDPPolicy1;
    PolicyStoreDTO pdpPolicyWithEmptyId;
    PolicyStoreDTO pdpPolicyWithEmptyVersion;

    @BeforeMethod
    public void setUp() {

        Properties engineProperties = new Properties();
        engineProperties.put(PDPConstants.MAX_NO_OF_POLICY_VERSIONS, "4");
        EntitlementConfigHolder.getInstance().setEngineProperties(engineProperties);
        policyPersistenceManager = createPolicyPersistenceManager();

        samplePAPPolicy1 = new PolicyDTO(SAMPLE_POLICY_ID_1);
        samplePAPPolicy1.setPolicy(SAMPLE_POLICY_STRING_1);
        samplePAPPolicy1.setPolicyEditorData(new String[]{"category|Resource", "policyDescription|"});
        samplePAPPolicy1.setPolicyEditor("XML");
        samplePAPPolicy1.setPolicyOrder(3);

        samplePAPPolicy2 = new PolicyDTO(SAMPLE_POLICY_ID_2);
        samplePAPPolicy2.setPolicy(SAMPLE_POLICY_STRING_2);

        samplePAPPolicy3 = new PolicyDTO(SAMPLE_POLICY_ID_3);
        samplePAPPolicy3.setPolicy(SAMPLE_POLICY_STRING_3);

        sampleUpdatedPAPPolicy1 = new PolicyDTO(SAMPLE_POLICY_ID_1);
        sampleUpdatedPAPPolicy1.setPolicy(SAMPLE_POLICY_STRING_2);

        papPolicyWithEmptyPolicyId = new PolicyDTO();
        papPolicyWithEmptyPolicyId.setPolicy(null);
        papPolicyWithEmptyPolicyId.setPolicy(SAMPLE_POLICY_STRING_1);

        samplePDPPolicy1 = getPDPPolicy(SAMPLE_POLICY_ID_1, SAMPLE_POLICY_STRING_1, "1", true, true, 0, false);
        samplePDPPolicy2 = getPDPPolicy(SAMPLE_POLICY_ID_2, SAMPLE_POLICY_STRING_2, "1", false, true, 0, false);
        samplePDPPolicy3 = getPDPPolicy(SAMPLE_POLICY_ID_3, SAMPLE_POLICY_STRING_3, "1", true, true, 2, true);

        orderedSamplePDPPolicy1 = getPDPPolicy(SAMPLE_POLICY_ID_1, SAMPLE_POLICY_STRING_1, null, true, false, 2, true);
        inactiveSamplePDPPolicy1 = getPDPPolicy(SAMPLE_POLICY_ID_1, null, null, false, true, 0, false);
        updatedSamplePDPPolicy1 = getPDPPolicy(SAMPLE_POLICY_ID_1, SAMPLE_POLICY_STRING_2, "2", true, false, 0, false);
        pdpPolicyWithEmptyId = getPDPPolicy(null, null, null, false, false, 0, false);
        pdpPolicyWithEmptyVersion = getPDPPolicy(SAMPLE_POLICY_ID_1, null, "", true, false, 0, false);
    }

    @AfterMethod
    public void tearDown() throws Exception {

        // Unpublish PDP policies used in test cases.
        policyPersistenceManager.deletePolicy(SAMPLE_POLICY_ID_1);
        policyPersistenceManager.deletePolicy(SAMPLE_POLICY_ID_2);
        policyPersistenceManager.deletePolicy(SAMPLE_POLICY_ID_3);

        // Remove PAP policies used in test cases.
        policyPersistenceManager.removePolicy(SAMPLE_POLICY_ID_1);
        policyPersistenceManager.removePolicy(SAMPLE_POLICY_ID_2);
        policyPersistenceManager.removePolicy(SAMPLE_POLICY_ID_3);
    }

    @Test(priority = 1)
    public void testGetModuleName() {

        assertEquals(policyPersistenceManager.getModuleName(), PDPConstants.MODULE_NAME);
    }

    @Test(priority = 2)
    public void testGetSupportedSearchAttributesScheme() {

        assertEquals(policyPersistenceManager.getSupportedSearchAttributesScheme(),
                PolicyFinderModule.COMBINATIONS_BY_CATEGORY_AND_PARAMETER);
    }

    @Test(priority = 3)
    public void testAddPAPPolicy() throws Exception {

        policyPersistenceManager.addOrUpdatePolicy(samplePAPPolicy1, true);

        PolicyDTO policyFromStorage = policyPersistenceManager.getPAPPolicy(samplePAPPolicy1.getPolicyId());
        assertEquals(policyFromStorage.getPolicy(), samplePAPPolicy1.getPolicy());
        assertEquals(policyFromStorage.getPolicyId(), samplePAPPolicy1.getPolicyId());
        assertEquals(policyFromStorage.getVersion(), "1");
        assertEquals(policyFromStorage.getPolicyEditorData(), samplePAPPolicy1.getPolicyEditorData());
        assertEquals(policyFromStorage.getPolicyOrder(), samplePAPPolicy1.getPolicyOrder());
        assertEquals(policyFromStorage.getAttributeDTOs().length, 4);
    }

    @Test(priority = 3, dependsOnMethods = {"testAddPAPPolicy"})
    public void testAddPAPPolicyWhenPolicyMetaDataStoringDisabled() throws Exception {

        Properties properties = EntitlementConfigHolder.getInstance().getEngineProperties();
        properties.setProperty(PDPConstants.STORE_POLICY_META_DATA, "false");
        EntitlementConfigHolder.getInstance().setEngineProperties(properties);

        policyPersistenceManager.addOrUpdatePolicy(samplePAPPolicy1, true);
        // Verify weather the policy meta-data was not stored for PAP policy.
        PolicyDTO papPolicyFromStorage = policyPersistenceManager.getPAPPolicy(samplePDPPolicy1.getPolicyId());
        assertEquals(papPolicyFromStorage.getAttributeDTOs().length, 0);

        properties.setProperty(PDPConstants.STORE_POLICY_META_DATA, "true");
        EntitlementConfigHolder.getInstance().setEngineProperties(properties);
    }

    @Test(priority = 3)
    public void testAddInvalidPolicy() {

        assertThrows(EntitlementException.class, () -> policyPersistenceManager.
                addOrUpdatePolicy(papPolicyWithEmptyPolicyId, true));
    }

    @Test(priority = 3)
    public void testAddPolicyMoreThanMaxVersions() throws Exception {

        policyPersistenceManager.addOrUpdatePolicy(samplePAPPolicy1, true);
        policyPersistenceManager.addOrUpdatePolicy(samplePAPPolicy1, true);
        policyPersistenceManager.addOrUpdatePolicy(samplePAPPolicy1, true);
        policyPersistenceManager.addOrUpdatePolicy(samplePAPPolicy1, true);
        policyPersistenceManager.addOrUpdatePolicy(samplePAPPolicy1, true);

        String[] policyVersions = policyPersistenceManager.getVersions(samplePAPPolicy1.getPolicyId());
        assertEquals(policyVersions.length, 5);

        policyPersistenceManager.addOrUpdatePolicy(samplePAPPolicy1, true);
        String[] policyVersionsAfterMax = policyPersistenceManager.getVersions(samplePAPPolicy1.getPolicyId());
        assertEquals(policyVersionsAfterMax.length, 5);
        assertFalse(Arrays.asList(policyVersionsAfterMax).contains("1"));
    }

    @Test(priority = 3)
    public void testAddPolicyMoreThanMaxVersionsWhenPolicyWasPublished() throws Exception {

        policyPersistenceManager.addOrUpdatePolicy(samplePAPPolicy1, true);
        policyPersistenceManager.addPolicy(samplePDPPolicy1);

        policyPersistenceManager.addOrUpdatePolicy(samplePAPPolicy1, true);
        policyPersistenceManager.addOrUpdatePolicy(samplePAPPolicy1, true);
        policyPersistenceManager.addOrUpdatePolicy(samplePAPPolicy1, true);
        policyPersistenceManager.addOrUpdatePolicy(samplePAPPolicy1, true);

        String[] policyVersions = policyPersistenceManager.getVersions(samplePAPPolicy1.getPolicyId());
        assertEquals(policyVersions.length, 5);

        policyPersistenceManager.addOrUpdatePolicy(samplePAPPolicy1, true);
        String[] policyVersionsAfterMax = policyPersistenceManager.getVersions(samplePAPPolicy1.getPolicyId());
        assertEquals(policyVersionsAfterMax.length, 5);
        assertFalse(Arrays.asList(policyVersionsAfterMax).contains("1"));

        // Verify weather the policy was not deleted from PDP.
        assertTrue(policyPersistenceManager.isPolicyExist(samplePAPPolicy1.getPolicyId()));
    }

    @Test(priority = 3)
    public void testGetPolicyForInvalidScenarios() throws EntitlementException {

        assertThrows(EntitlementException.class, () -> policyPersistenceManager.
                getPolicy(SAMPLE_POLICY_ID_1, ""));
        policyPersistenceManager.addOrUpdatePolicy(samplePAPPolicy1, true);
        assertThrows(EntitlementException.class, () -> policyPersistenceManager.
                getPolicy(SAMPLE_POLICY_ID_1, "2"));
    }

    @Test(priority = 4)
    public void testDeletePAPPolicy() throws Exception {

        policyPersistenceManager.addOrUpdatePolicy(samplePAPPolicy1, true);
        policyPersistenceManager.removePolicy(samplePAPPolicy1.getPolicyId());
        assertNull(policyPersistenceManager.getPAPPolicy(samplePAPPolicy1.getPolicyId()));
    }

    @Test(priority = 4)
    public void testDeletePublishedPAPPolicyFromPAP() throws Exception {

        policyPersistenceManager.addOrUpdatePolicy(samplePAPPolicy1, true);
        policyPersistenceManager.addPolicy(samplePDPPolicy1);
        policyPersistenceManager.removePolicy(samplePAPPolicy1.getPolicyId());

        assertNull(policyPersistenceManager.getPAPPolicy(samplePAPPolicy1.getPolicyId()));
        assertTrue(policyPersistenceManager.isPolicyExist(samplePAPPolicy1.getPolicyId()));

        policyPersistenceManager.deletePolicy(samplePAPPolicy1.getPolicyId());
        policyPersistenceManager.removePolicy(samplePAPPolicy1.getPolicyId());
        assertFalse(policyPersistenceManager.isPolicyExist(samplePAPPolicy1.getPolicyId()));
    }

    @Test(priority = 5)
    public void testListPAPPolicy() throws Exception {

        List<String> policyIds = new ArrayList<>();
        List<PolicyDTO> papPolicies = policyPersistenceManager.getPAPPolicies(policyIds);
        assertEquals(papPolicies.size(), 0);
        papPolicies = policyPersistenceManager.getPAPPolicies(null);
        assertEquals(papPolicies.size(), 0);

        policyPersistenceManager.addOrUpdatePolicy(samplePAPPolicy1, true);
        policyPersistenceManager.addOrUpdatePolicy(samplePAPPolicy2, true);

        policyIds.add(samplePAPPolicy1.getPolicyId());
        policyIds.add(samplePAPPolicy2.getPolicyId());
        List<PolicyDTO> papPoliciesFromStorage = policyPersistenceManager.getPAPPolicies(policyIds);
        assertEquals(papPoliciesFromStorage.size(), 2);

        List<String> policyIdListFromStorage = policyPersistenceManager.listPolicyIds();
        assertEquals(policyIdListFromStorage.size(), 2);
        assertTrue(policyIdListFromStorage.containsAll(policyIds));
    }

    @Test(priority = 6)
    public void testUpdatePAPPolicy() throws Exception {

        policyPersistenceManager.addOrUpdatePolicy(samplePAPPolicy1, true);
        policyPersistenceManager.addOrUpdatePolicy(sampleUpdatedPAPPolicy1, true);

        PolicyDTO updatedPolicyFromStorage = policyPersistenceManager.getPAPPolicy(samplePAPPolicy1.getPolicyId());
        assertEquals(updatedPolicyFromStorage.getPolicy(), sampleUpdatedPAPPolicy1.getPolicy());
        assertEquals(updatedPolicyFromStorage.getPolicyId(), sampleUpdatedPAPPolicy1.getPolicyId());
        assertEquals(updatedPolicyFromStorage.getVersion(), "2");

        // Verify the policy version 1.
        PolicyDTO policyVersion1 = policyPersistenceManager.getPolicy(samplePAPPolicy1.getPolicyId(), "1");
        assertEquals(policyVersion1.getPolicy(), samplePAPPolicy1.getPolicy());
        // Verify the policy version 2.
        PolicyDTO policyVersion2 = policyPersistenceManager.getPolicy(samplePAPPolicy1.getPolicyId(), "2");
        assertEquals(policyVersion2.getPolicy(), sampleUpdatedPAPPolicy1.getPolicy());

        // Verify the total number of versions.
        String[] policyVersions = policyPersistenceManager.getVersions(samplePAPPolicy1.getPolicyId());
        assertEquals(policyVersions.length, 2);
    }

    @Test(priority = 6)
    public void testGetPolicyWithoutVersion() throws Exception {

        policyPersistenceManager.addOrUpdatePolicy(samplePAPPolicy1, true);
        policyPersistenceManager.addOrUpdatePolicy(sampleUpdatedPAPPolicy1, true);

        // Verify the policy version without defining the version.
        PolicyDTO latestPolicy = policyPersistenceManager.getPolicy(samplePAPPolicy1.getPolicyId(), " ");
        assertEquals(latestPolicy.getPolicy(), sampleUpdatedPAPPolicy1.getPolicy());

        latestPolicy = policyPersistenceManager.getPolicy(samplePAPPolicy1.getPolicyId(), null);
        assertEquals(latestPolicy.getPolicy(), sampleUpdatedPAPPolicy1.getPolicy());
    }

    @Test(priority = 7)
    public void testAddPDPPolicy() throws Exception {

        policyPersistenceManager.addOrUpdatePolicy(samplePAPPolicy1, true);
        policyPersistenceManager.addPolicy(samplePDPPolicy1);

        assertTrue(policyPersistenceManager.isPolicyExist(samplePDPPolicy1.getPolicyId()));
        PolicyStoreDTO publishedPolicyFromStorage =
                policyPersistenceManager.getPublishedPolicy(samplePDPPolicy1.getPolicyId());
        assertEquals(publishedPolicyFromStorage.getPolicy(), samplePDPPolicy1.getPolicy());
        assertEquals(publishedPolicyFromStorage.getPolicyId(), samplePDPPolicy1.getPolicyId());
    }

    @Test(priority = 7)
    public void testIsPolicyExists() throws Exception {

        assertFalse(policyPersistenceManager.isPolicyExist(null));
        assertFalse(policyPersistenceManager.isPolicyExist(""));
        assertFalse(policyPersistenceManager.isPolicyExist("sample_policy1"));

        policyPersistenceManager.addOrUpdatePolicy(samplePAPPolicy1, true);
        policyPersistenceManager.addPolicy(samplePDPPolicy1);
        assertTrue(policyPersistenceManager.isPolicyExist(samplePDPPolicy1.getPolicyId()));
    }

    @Test(priority = 7)
    public void testAddInvalidPDPPolicy() throws Exception {

        assertThrows(EntitlementException.class, () -> policyPersistenceManager.addPolicy(pdpPolicyWithEmptyId));
        assertThrows(EntitlementException.class, () -> policyPersistenceManager.addPolicy(pdpPolicyWithEmptyVersion));
        assertThrows(EntitlementException.class, () -> policyPersistenceManager.addPolicy(null));
    }

    @Test(priority = 8)
    public void testDeletePDPPolicy() throws Exception {

        policyPersistenceManager.addOrUpdatePolicy(samplePAPPolicy1, true);
        policyPersistenceManager.addPolicy(samplePDPPolicy1);

        policyPersistenceManager.deletePolicy(samplePDPPolicy1.getPolicyId());
        assertFalse(policyPersistenceManager.isPolicyExist(samplePDPPolicy1.getPolicyId()));
    }

    @Test(priority = 8)
    public void testDeletePDPPolicyUsingBlankID() throws Exception {

        assertFalse(policyPersistenceManager.deletePolicy(null));
        assertFalse(policyPersistenceManager.deletePolicy(""));
    }

    @Test(priority = 9)
    public void testGetReferencedPolicy() throws Exception {

        policyPersistenceManager.addOrUpdatePolicy(samplePAPPolicy1, true);
        policyPersistenceManager.addOrUpdatePolicy(samplePAPPolicy2, true);

        policyPersistenceManager.addPolicy(samplePDPPolicy1);
        policyPersistenceManager.addPolicy(samplePDPPolicy2);

        // Verify the policies that are not active.
        assertNull(policyPersistenceManager.getReferencedPolicy(samplePDPPolicy1.getPolicyId()));
        assertEquals(policyPersistenceManager.getReferencedPolicy(samplePDPPolicy2.getPolicyId()),
                samplePDPPolicy2.getPolicy());
    }

    @Test(priority = 10)
    public void testGetPolicyOrder() throws Exception {

        policyPersistenceManager.addOrUpdatePolicy(samplePAPPolicy1, true);
        policyPersistenceManager.addOrUpdatePolicy(samplePAPPolicy3, true);

        policyPersistenceManager.addPolicy(samplePDPPolicy1);
        policyPersistenceManager.addPolicy(samplePDPPolicy3);

        // Verify the policy order.
        assertEquals(policyPersistenceManager.getPolicyOrder(samplePDPPolicy1.getPolicyId()), 0);
        assertEquals(policyPersistenceManager.getPolicyOrder(samplePDPPolicy3.getPolicyId()),
                samplePDPPolicy3.getPolicyOrder());
    }

    @Test(priority = 11)
    public void testListPDPPolicy() throws Exception {

        policyPersistenceManager.addOrUpdatePolicy(samplePAPPolicy1, true);
        policyPersistenceManager.addOrUpdatePolicy(samplePAPPolicy2, true);
        policyPersistenceManager.addOrUpdatePolicy(samplePAPPolicy3, true);

        policyPersistenceManager.addPolicy(samplePDPPolicy1);
        policyPersistenceManager.addPolicy(samplePDPPolicy2);
        policyPersistenceManager.addPolicy(samplePDPPolicy3);

        // Verify the number of published policies.
        List<String> policyIds = policyPersistenceManager.listPublishedPolicyIds();
        assertEquals(policyIds.size(), 3);

        // Verify the number of ordered policy identifiers.
        String[] orderedPolicyIdentifiers = policyPersistenceManager.getOrderedPolicyIdentifiers();
        assertEquals(orderedPolicyIdentifiers.length, 3);

        // Verify the number of active policies.
        String[] activePolicies = policyPersistenceManager.getActivePolicies();
        assertEquals(activePolicies.length, 2);
    }

    @Test(priority = 12)
    public void testUpdatePDPPolicy() throws Exception {

        policyPersistenceManager.addOrUpdatePolicy(samplePAPPolicy1, true);
        policyPersistenceManager.addPolicy(samplePDPPolicy1);

        // Update Policy order.
        policyPersistenceManager.updatePolicy(orderedSamplePDPPolicy1);
        assertEquals(policyPersistenceManager.getPolicyOrder(samplePDPPolicy1.getPolicyId()),
                orderedSamplePDPPolicy1.getPolicyOrder());
        PolicyStoreDTO orderUpdatedPDPPolicy =
                policyPersistenceManager.getPublishedPolicy(samplePDPPolicy1.getPolicyId());
        assertTrue(orderUpdatedPDPPolicy.isActive());

        // Update Policy active status.
        policyPersistenceManager.updatePolicy(inactiveSamplePDPPolicy1);
        PolicyStoreDTO statusUpdatedPDPPolicy =
                policyPersistenceManager.getPublishedPolicy(samplePDPPolicy1.getPolicyId());
        assertEquals(policyPersistenceManager.getPolicyOrder(samplePDPPolicy1.getPolicyId()),
                orderedSamplePDPPolicy1.getPolicyOrder());
        assertFalse(statusUpdatedPDPPolicy.isActive());

        // Update only the policy String.
        policyPersistenceManager.addOrUpdatePolicy(sampleUpdatedPAPPolicy1, true);
        policyPersistenceManager.updatePolicy(updatedSamplePDPPolicy1);
        PolicyStoreDTO policyUpdatedPDPPolicy =
                policyPersistenceManager.getPublishedPolicy(samplePDPPolicy1.getPolicyId());
        assertEquals(policyUpdatedPDPPolicy.getPolicy(), updatedSamplePDPPolicy1.getPolicy());
    }

    @Test(priority = 12)
    public void testUpdateInvalidPDPPolicy() throws Exception {

        assertThrows(EntitlementException.class, () -> policyPersistenceManager.updatePolicy(null));
        assertThrows(EntitlementException.class, () -> policyPersistenceManager.updatePolicy(pdpPolicyWithEmptyId));
        assertThrows(EntitlementException.class, () -> policyPersistenceManager.
                updatePolicy(pdpPolicyWithEmptyVersion));
        assertThrows(EntitlementException.class, () -> policyPersistenceManager.
                updatePolicy(inactiveSamplePDPPolicy1));
    }

    @Test(priority = 13)
    public void testGetSearchAttributes() throws Exception {

        Map<String, Set<AttributeDTO>> attributes = policyPersistenceManager.getSearchAttributes("identifier", null);
        assertEquals(attributes.size(), 0);

        policyPersistenceManager.addOrUpdatePolicy(samplePAPPolicy1, true);
        policyPersistenceManager.addPolicy(samplePDPPolicy1);
        attributes = policyPersistenceManager.getSearchAttributes(null, null);
        assertEquals(attributes.size(), 1);
        assertEquals(attributes.get(samplePDPPolicy1.getPolicyId()).size(), 4);

        policyPersistenceManager.addOrUpdatePolicy(samplePAPPolicy3, true);
        policyPersistenceManager.addPolicy(samplePDPPolicy3);
        attributes = policyPersistenceManager.getSearchAttributes(null, null);
        assertEquals(attributes.size(), 2);
        assertEquals(attributes.get(samplePDPPolicy1.getPolicyId()).size(), 4);
        assertEquals(attributes.get(samplePDPPolicy3.getPolicyId()).size(), 4);
    }

    private PolicyStoreDTO getPDPPolicy(String id, String policy, String version, boolean active, boolean setActive,
                                        int order, boolean setOrder) {

        PolicyStoreDTO policyStoreDTO = new PolicyStoreDTO();
        if (id != null) {
            policyStoreDTO.setPolicyId(id);
        }
        if (policy != null) {
            policyStoreDTO.setPolicy(policy);
        }
        if (version != null) {
            policyStoreDTO.setVersion(version);
        }
        policyStoreDTO.setActive(active);
        policyStoreDTO.setSetActive(setActive);
        if (order != 0) {
            policyStoreDTO.setPolicyOrder(order);
        }
        policyStoreDTO.setSetOrder(setOrder);
        return policyStoreDTO;
    }

    /**
     * Abstract method to create the policy persistence manager.
     *
     * @return The policy persistence manager.
     */
    public abstract PolicyPersistenceManager createPolicyPersistenceManager();
}
