/*
 * Copyright (c) 2026, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.carbon.identity.policy.management.dao;

import org.mockito.MockedStatic;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.common.testng.WithCarbonHome;
import org.wso2.carbon.identity.common.testng.WithH2Database;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;
import org.wso2.carbon.identity.policy.management.api.exception.PolicyManagementException;
import org.wso2.carbon.identity.policy.management.api.model.Policy;
import org.wso2.carbon.identity.policy.management.api.model.PolicyBasicInfo;
import org.wso2.carbon.identity.policy.management.api.model.PolicyResource;
import org.wso2.carbon.identity.policy.management.api.model.ResourceType;
import org.wso2.carbon.identity.policy.management.api.model.RulePolicyResource;
import org.wso2.carbon.identity.policy.management.internal.dao.impl.PolicyManagementDAOImpl;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.mockStatic;

/**
 * Unit tests for PolicyManagementDAOImpl.
 */
@WithCarbonHome
@WithH2Database(files = {"dbscripts/h2.sql"})
public class PolicyManagementDAOImplTest {

    private static final int TENANT_ID = -1234;
    private static final String TENANT_DOMAIN = "carbon.super";
    private static final String TEST_POLICY_NAME = "TestPolicy";
    private static final String TEST_RULE_ID = UUID.randomUUID().toString();

    private PolicyManagementDAOImpl policyManagementDAO;
    private MockedStatic<IdentityTenantUtil> identityTenantUtil;
    private String createdPolicyId;

    @BeforeClass
    public void setUp() {

        policyManagementDAO = new PolicyManagementDAOImpl();
        identityTenantUtil = mockStatic(IdentityTenantUtil.class);
        identityTenantUtil.when(() -> IdentityTenantUtil.getTenantDomain(TENANT_ID))
                .thenReturn(TENANT_DOMAIN);
        identityTenantUtil.when(() -> IdentityTenantUtil.getTenantId(TENANT_DOMAIN))
                .thenReturn(TENANT_ID);
    }

    @AfterClass
    public void tearDown() {

        identityTenantUtil.close();
    }

    @Test(priority = 1)
    public void testAddPolicy() throws PolicyManagementException {

        List<PolicyResource> resources = Collections.singletonList(
                new RulePolicyResource(null, "android", TEST_RULE_ID, null));

        Policy policy = new Policy(
                UUID.randomUUID().toString(),
                TEST_POLICY_NAME,
                TENANT_DOMAIN,
                resources);

        Policy result = policyManagementDAO.addPolicy(policy, TENANT_ID);

        Assert.assertNotNull(result);
        Assert.assertEquals(result.getName(), TEST_POLICY_NAME);
        Assert.assertEquals(result.getResources().size(), 1);
        Assert.assertEquals(result.getResources().get(0).getTarget(), "android");

        createdPolicyId = result.getId();
    }

    @Test(priority = 2, dependsOnMethods = {"testAddPolicy"})
    public void testGetPolicyById() throws PolicyManagementException {

        Policy result = policyManagementDAO.getPolicyById(createdPolicyId, TENANT_ID);

        Assert.assertNotNull(result);
        Assert.assertEquals(result.getId(), createdPolicyId);
        Assert.assertEquals(result.getName(), TEST_POLICY_NAME);
        Assert.assertEquals(result.getResources().size(), 1);
        Assert.assertEquals(result.getResources().get(0).getResourceId(), TEST_RULE_ID);
    }

    @Test(priority = 3, dependsOnMethods = {"testAddPolicy"})
    public void testUpdatePolicy() throws PolicyManagementException {

        String updatedName = "UpdatedPolicy";
        String updatedRuleId = UUID.randomUUID().toString();

        List<PolicyResource> updatedResources = Collections.singletonList(
                new RulePolicyResource(null, "ios", updatedRuleId, null));

        Policy updatedPolicy = new Policy(createdPolicyId, updatedName, TENANT_DOMAIN, updatedResources);

        Policy result = policyManagementDAO.updatePolicy(updatedPolicy, TENANT_ID);

        Assert.assertNotNull(result);
        Assert.assertEquals(result.getName(), updatedName);
        Assert.assertEquals(result.getResources().size(), 1);
        Assert.assertEquals(result.getResources().get(0).getTarget(), "ios");
    }

    @Test(priority = 4, dependsOnMethods = {"testAddPolicy"})
    public void testGetPolicyById_NotFound() throws PolicyManagementException {

        Policy result = policyManagementDAO.getPolicyById(
                UUID.randomUUID().toString(), TENANT_ID);

        Assert.assertNull(result);
    }

    @Test(priority = 5, dependsOnMethods = {"testAddPolicy"})
    public void testDeletePolicy() throws PolicyManagementException {

        policyManagementDAO.deletePolicy(createdPolicyId, TENANT_ID);

        Policy result = policyManagementDAO.getPolicyById(createdPolicyId, TENANT_ID);
        Assert.assertNull(result);
    }

    private String addPolicy(String name) throws PolicyManagementException {

        Policy policy = new Policy(UUID.randomUUID().toString(), name, TENANT_DOMAIN, Collections.emptyList());
        return policyManagementDAO.addPolicy(policy, TENANT_ID).getId();
    }

    @Test(priority = 6)
    public void testGetPoliciesPaginationAndCount() throws PolicyManagementException {

        addPolicy("PageTestAlpha");
        addPolicy("PageTestBeta");
        addPolicy("PageTestGamma");

        Assert.assertEquals(policyManagementDAO.getPolicyCount(TENANT_ID, "PageTest"), 3);

        List<PolicyBasicInfo> firstPage = policyManagementDAO.getPolicies(TENANT_ID, "PageTest", 0, 2);
        Assert.assertEquals(firstPage.size(), 2);
        // Results are ordered by policy name ascending.
        Assert.assertEquals(firstPage.get(0).getName(), "PageTestAlpha");
        Assert.assertEquals(firstPage.get(1).getName(), "PageTestBeta");

        List<PolicyBasicInfo> secondPage = policyManagementDAO.getPolicies(TENANT_ID, "PageTest", 2, 2);
        Assert.assertEquals(secondPage.size(), 1);
        Assert.assertEquals(secondPage.get(0).getName(), "PageTestGamma");

        // A non-positive limit yields an empty page.
        Assert.assertTrue(policyManagementDAO.getPolicies(TENANT_ID, "PageTest", 0, 0).isEmpty());
    }

    @Test(priority = 7)
    public void testGetPoliciesWithFilter() throws PolicyManagementException {

        addPolicy("FilterTestUnique");

        Assert.assertEquals(policyManagementDAO.getPolicyCount(TENANT_ID, "FilterTestUnique"), 1);
        List<PolicyBasicInfo> result = policyManagementDAO.getPolicies(TENANT_ID, "FilterTestUnique", 0, 10);
        Assert.assertEquals(result.size(), 1);
        Assert.assertEquals(result.get(0).getName(), "FilterTestUnique");
    }

    @Test(priority = 8)
    public void testGetPolicyByName() throws PolicyManagementException {

        addPolicy("NameLookupPolicy");

        Policy result = policyManagementDAO.getPolicyByName("NameLookupPolicy", TENANT_ID);
        Assert.assertNotNull(result);
        Assert.assertEquals(result.getName(), "NameLookupPolicy");
    }

    @Test(priority = 9)
    public void testResourceRoundTripAndTypeStored() throws PolicyManagementException {

        String ruleId = UUID.randomUUID().toString();
        Policy policy = new Policy(UUID.randomUUID().toString(), "ResourceRoundTrip", TENANT_DOMAIN,
                Collections.singletonList(new RulePolicyResource(null, "ios", ruleId, null)));
        String id = policyManagementDAO.addPolicy(policy, TENANT_ID).getId();

        Policy fetched = policyManagementDAO.getPolicyById(id, TENANT_ID);
        Assert.assertEquals(fetched.getResources().size(), 1);
        PolicyResource resource = fetched.getResources().get(0);
        Assert.assertEquals(resource.getResourceType(), ResourceType.RULE);
        Assert.assertEquals(resource.getTarget(), "ios");
        Assert.assertEquals(resource.getResourceId(), ruleId);
    }

    @Test(priority = 10, expectedExceptions = PolicyManagementException.class)
    public void testDuplicateTargetPerTypeRejected() throws PolicyManagementException {

        // Two RULE resources for the same target violate UNIQUE (POLICY_ID, TARGET, RESOURCE_TYPE).
        Policy policy = new Policy(UUID.randomUUID().toString(), "DuplicateTargetPolicy", TENANT_DOMAIN,
                Arrays.asList(
                        new RulePolicyResource(null, "ios", UUID.randomUUID().toString(), null),
                        new RulePolicyResource(null, "ios", UUID.randomUUID().toString(), null)));

        policyManagementDAO.addPolicy(policy, TENANT_ID);
    }
}
