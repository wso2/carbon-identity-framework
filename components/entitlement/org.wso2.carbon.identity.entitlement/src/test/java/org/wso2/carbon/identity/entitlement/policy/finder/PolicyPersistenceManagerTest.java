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

package org.wso2.carbon.identity.entitlement.policy.finder;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.entitlement.PDPConstants;
import org.wso2.carbon.identity.entitlement.dto.PolicyDTO;
import org.wso2.carbon.identity.entitlement.dto.PolicyStoreDTO;
import org.wso2.carbon.identity.entitlement.internal.EntitlementConfigHolder;
import org.wso2.carbon.identity.entitlement.persistence.PolicyPersistenceManager;

import java.util.Properties;

import static org.testng.Assert.assertEquals;

/**
 * This is the parent test class for the Policy Persistence Manager test classes.
 */
public class PolicyPersistenceManagerTest {

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

    PolicyStoreDTO samplePDPPolicy1;
    PolicyStoreDTO samplePDPPolicy2;
    PolicyStoreDTO samplePDPPolicy3;

    @BeforeClass
    public void setUpClass() {

        Properties engineProperties = new Properties();
        engineProperties.put(PDPConstants.MAX_NO_OF_POLICY_VERSIONS, "0");
        EntitlementConfigHolder.getInstance().setEngineProperties(engineProperties);

        samplePAPPolicy1 = new PolicyDTO(SAMPLE_POLICY_ID_1);
        samplePAPPolicy1.setPolicy(SAMPLE_POLICY_STRING_1);

        samplePAPPolicy2 = new PolicyDTO(SAMPLE_POLICY_ID_2);
        samplePAPPolicy2.setPolicy(SAMPLE_POLICY_STRING_2);

        samplePAPPolicy3 = new PolicyDTO(SAMPLE_POLICY_ID_3);
        samplePAPPolicy3.setPolicy(SAMPLE_POLICY_STRING_3);

        samplePDPPolicy1 = getPDPPolicy(SAMPLE_POLICY_ID_1, SAMPLE_POLICY_STRING_1, "1", true, true, 0, false);
        samplePDPPolicy2 = getPDPPolicy(SAMPLE_POLICY_ID_2, SAMPLE_POLICY_STRING_2, "1", false, true, 0, false);
        samplePDPPolicy3 = getPDPPolicy(SAMPLE_POLICY_ID_3, SAMPLE_POLICY_STRING_3, "1", true, true, 2, true);
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

    @Test
    public void testGetPolicyIdentifiers() throws Exception {

        policyPersistenceManager.addOrUpdatePolicy(samplePAPPolicy1, true);
        policyPersistenceManager.addOrUpdatePolicy(samplePAPPolicy2, true);
        policyPersistenceManager.addOrUpdatePolicy(samplePAPPolicy3, true);

        String[] policyIdentifiersBeforePublishing = ((AbstractPolicyFinderModule) policyPersistenceManager)
                .getPolicyIdentifiers();
        assertEquals(policyIdentifiersBeforePublishing.length, 0);

        policyPersistenceManager.addPolicy(samplePDPPolicy1);
        policyPersistenceManager.addPolicy(samplePDPPolicy2);
        policyPersistenceManager.addPolicy(samplePDPPolicy3);

        String[] policyIdentifiersAfterPublishing = ((AbstractPolicyFinderModule) policyPersistenceManager).
                getPolicyIdentifiers();
        assertEquals(policyIdentifiersAfterPublishing.length, 3);
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
}
