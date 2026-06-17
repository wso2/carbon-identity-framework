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

package org.wso2.carbon.identity.policy.management.service;

import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.common.testng.WithCarbonHome;
import org.wso2.carbon.identity.common.testng.WithRealmService;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;
import org.wso2.carbon.identity.policy.management.api.exception.PolicyManagementClientException;
import org.wso2.carbon.identity.policy.management.api.exception.PolicyManagementException;
import org.wso2.carbon.identity.policy.management.api.exception.PolicyManagementServerException;
import org.wso2.carbon.identity.policy.management.api.model.Policy;
import org.wso2.carbon.identity.policy.management.api.model.PolicyResource;
import org.wso2.carbon.identity.policy.management.api.model.ResourceType;
import org.wso2.carbon.identity.policy.management.internal.component.PolicyMgtComponentServiceHolder;
import org.wso2.carbon.identity.policy.management.internal.dao.PolicyManagementDAO;
import org.wso2.carbon.identity.policy.management.internal.service.impl.PolicyManagementServiceImpl;
import org.wso2.carbon.identity.rule.management.api.exception.RuleManagementException;
import org.wso2.carbon.identity.rule.management.api.model.Rule;
import org.wso2.carbon.identity.rule.management.api.service.RuleManagementService;

import java.util.Arrays;
import java.util.Collections;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for PolicyManagementServiceImpl.
 * Covers validation, rule-mgt orchestration, and best-effort saga compensation.
 */
@WithCarbonHome
@WithRealmService
public class PolicyManagementServiceImplTest {

    private static final String TENANT_DOMAIN = "carbon.super";
    private static final int TENANT_ID = -1234;
    private static final String TEST_POLICY_NAME = "TestPolicy";
    private static final String TEST_POLICY_ID = UUID.randomUUID().toString();

    @Mock
    private PolicyManagementDAO policyManagementDAO;

    @Mock
    private RuleManagementService ruleManagementService;

    private PolicyManagementServiceImpl policyManagementService;
    private MockedStatic<IdentityTenantUtil> identityTenantUtil;
    private AutoCloseable mocks;
    // Reflection fields removed; tests now construct the service with a mock DAO.

    @BeforeClass
    public void setUp() throws Exception {

        mocks = MockitoAnnotations.openMocks(this);
        policyManagementService = new PolicyManagementServiceImpl(policyManagementDAO);

        PolicyMgtComponentServiceHolder.getInstance().setRuleManagementService(ruleManagementService);

        identityTenantUtil = mockStatic(IdentityTenantUtil.class);
        identityTenantUtil.when(() -> IdentityTenantUtil.getTenantId(TENANT_DOMAIN)).thenReturn(TENANT_ID);
        identityTenantUtil.when(() -> IdentityTenantUtil.getTenantDomain(TENANT_ID)).thenReturn(TENANT_DOMAIN);
    }

    @AfterClass
    public void tearDown() throws Exception {
        identityTenantUtil.close();
        mocks.close();
    }

    @BeforeMethod
    public void reset() {

        org.mockito.Mockito.reset(policyManagementDAO);
        org.mockito.Mockito.reset(ruleManagementService);
    }

    // --- Basic validation tests ---

    @Test
    public void testAddPolicy() throws PolicyManagementException {

        Policy inputPolicy = new Policy(null, TEST_POLICY_NAME, TENANT_DOMAIN,
                Collections.emptyList());
        Policy savedPolicy = new Policy(TEST_POLICY_ID, TEST_POLICY_NAME, TENANT_DOMAIN,
                Collections.emptyList());

        when(policyManagementDAO.addPolicy(any(Policy.class), eq(TENANT_ID))).thenReturn(savedPolicy);

        Policy result = policyManagementService.addPolicy(inputPolicy, TENANT_DOMAIN);

        Assert.assertNotNull(result);
        Assert.assertEquals(result.getName(), TEST_POLICY_NAME);
        verify(policyManagementDAO).addPolicy(any(Policy.class), eq(TENANT_ID));
    }

    @Test(expectedExceptions = PolicyManagementClientException.class)
    public void testAddPolicy_EmptyName() throws PolicyManagementException {

        Policy inputPolicy = new Policy(null, "", TENANT_DOMAIN,
                Collections.emptyList());
        policyManagementService.addPolicy(inputPolicy, TENANT_DOMAIN);
    }

    @Test(expectedExceptions = PolicyManagementClientException.class)
    public void testAddPolicy_NullName() throws PolicyManagementException {

        Policy inputPolicy = new Policy(null, null, TENANT_DOMAIN,
                Collections.emptyList());
        policyManagementService.addPolicy(inputPolicy, TENANT_DOMAIN);
    }

    @Test
    public void testGetPolicyById() throws PolicyManagementException {

        Policy expectedPolicy = new Policy(TEST_POLICY_ID, TEST_POLICY_NAME, TENANT_DOMAIN,
                Collections.emptyList());

        when(policyManagementDAO.getPolicyById(TEST_POLICY_ID, TENANT_ID)).thenReturn(expectedPolicy);

        Policy result = policyManagementService.getPolicyById(TEST_POLICY_ID, TENANT_DOMAIN);

        Assert.assertNotNull(result);
        Assert.assertEquals(result.getId(), TEST_POLICY_ID);
        Assert.assertEquals(result.getName(), TEST_POLICY_NAME);
        verify(policyManagementDAO).getPolicyById(TEST_POLICY_ID, TENANT_ID);
    }

    @Test
    public void testUpdatePolicy() throws PolicyManagementException {

        Policy existingPolicy = new Policy(TEST_POLICY_ID, TEST_POLICY_NAME, TENANT_DOMAIN,
                Collections.emptyList());
        Policy updatedPolicy = new Policy(TEST_POLICY_ID, "UpdatedPolicy", TENANT_DOMAIN,
                Collections.emptyList());

        when(policyManagementDAO.getPolicyById(TEST_POLICY_ID, TENANT_ID)).thenReturn(existingPolicy);
        when(policyManagementDAO.updatePolicy(any(Policy.class), eq(TENANT_ID))).thenReturn(updatedPolicy);

        Policy result = policyManagementService.updatePolicy(updatedPolicy, TENANT_DOMAIN);

        Assert.assertNotNull(result);
        Assert.assertEquals(result.getName(), "UpdatedPolicy");
        verify(policyManagementDAO).updatePolicy(any(Policy.class), eq(TENANT_ID));
    }

    @Test(expectedExceptions = PolicyManagementClientException.class)
    public void testUpdatePolicy_PolicyNotFound() throws PolicyManagementException {

        Policy policy = new Policy(TEST_POLICY_ID, TEST_POLICY_NAME, TENANT_DOMAIN,
                Collections.emptyList());

        when(policyManagementDAO.getPolicyById(TEST_POLICY_ID, TENANT_ID)).thenReturn(null);

        policyManagementService.updatePolicy(policy, TENANT_DOMAIN);
    }

    @Test
    public void testDeletePolicy() throws PolicyManagementException {

        Policy existingPolicy = new Policy(TEST_POLICY_ID, TEST_POLICY_NAME, TENANT_DOMAIN,
                Collections.emptyList());

        when(policyManagementDAO.getPolicyById(TEST_POLICY_ID, TENANT_ID)).thenReturn(existingPolicy);

        policyManagementService.deletePolicy(TEST_POLICY_ID, TENANT_DOMAIN);

        verify(policyManagementDAO).deletePolicy(TEST_POLICY_ID, TENANT_ID);
    }

    @Test
    public void testDeletePolicy_PolicyNotExists() throws PolicyManagementException {

        when(policyManagementDAO.getPolicyById(TEST_POLICY_ID, TENANT_ID)).thenReturn(null);

        policyManagementService.deletePolicy(TEST_POLICY_ID, TENANT_DOMAIN);

        verify(policyManagementDAO, org.mockito.Mockito.never()).deletePolicy(any(), eq(TENANT_ID));
    }

    // --- Rule orchestration and saga compensation tests ---

    @Test
    public void testAddPolicyWithRuleResource_CreatesRuleAndPersistsWithResourceId()
            throws PolicyManagementException, RuleManagementException {

        Rule created = mock(Rule.class);
        when(created.getId()).thenReturn("rule-1");
        when(ruleManagementService.addRule(any(Rule.class), eq(TENANT_DOMAIN))).thenReturn(created);

        PolicyResource ruleRes = new PolicyResource(null, "ios", ResourceType.RULE, null, mock(Rule.class));
        Policy inputPolicy = new Policy(null, TEST_POLICY_NAME, TENANT_DOMAIN,
                Collections.singletonList(ruleRes));
        Policy savedPolicy = new Policy(TEST_POLICY_ID, TEST_POLICY_NAME, TENANT_DOMAIN,
                Collections.singletonList(ruleRes));
        when(policyManagementDAO.addPolicy(any(Policy.class), eq(TENANT_ID))).thenReturn(savedPolicy);

        policyManagementService.addPolicy(inputPolicy, TENANT_DOMAIN);

        verify(ruleManagementService).addRule(any(Rule.class), eq(TENANT_DOMAIN));
        ArgumentCaptor<Policy> captor = ArgumentCaptor.forClass(Policy.class);
        verify(policyManagementDAO).addPolicy(captor.capture(), eq(TENANT_ID));
        PolicyResource persisted = captor.getValue().getResources().get(0);
        Assert.assertEquals(persisted.getResourceType(), ResourceType.RULE);
        Assert.assertEquals(persisted.getResourceId(), "rule-1");
        Assert.assertEquals(persisted.getTarget(), "ios");
    }

    @Test
    public void testAddPolicyWithActionResource_PassesThrough()
            throws PolicyManagementException, RuleManagementException {

        PolicyResource action = new PolicyResource(null, "ios", ResourceType.ACTION, "action-1", null);
        Policy inputPolicy = new Policy(null, TEST_POLICY_NAME, TENANT_DOMAIN,
                Collections.singletonList(action));
        Policy savedPolicy = new Policy(TEST_POLICY_ID, TEST_POLICY_NAME, TENANT_DOMAIN,
                Collections.singletonList(action));
        when(policyManagementDAO.addPolicy(any(Policy.class), eq(TENANT_ID))).thenReturn(savedPolicy);

        policyManagementService.addPolicy(inputPolicy, TENANT_DOMAIN);

        // ACTION resources are not created through rule-mgt; they pass through unchanged.
        verify(ruleManagementService, never()).addRule(any(Rule.class), eq(TENANT_DOMAIN));
        ArgumentCaptor<Policy> captor = ArgumentCaptor.forClass(Policy.class);
        verify(policyManagementDAO).addPolicy(captor.capture(), eq(TENANT_ID));
        PolicyResource persisted = captor.getValue().getResources().get(0);
        Assert.assertEquals(persisted.getResourceType(), ResourceType.ACTION);
        Assert.assertEquals(persisted.getResourceId(), "action-1");
    }

    @Test
    public void testAddPolicyWithRule_CompensatesWhenRuleCreationFails()
            throws RuleManagementException, PolicyManagementException {

        Rule created = mock(Rule.class);
        when(created.getId()).thenReturn("rule-1");
        when(ruleManagementService.addRule(any(Rule.class), eq(TENANT_DOMAIN)))
                .thenReturn(created)
                .thenThrow(RuleManagementException.class);

        PolicyResource r1 = new PolicyResource(null, "ios", ResourceType.RULE, null, mock(Rule.class));
        PolicyResource r2 = new PolicyResource(null, "android", ResourceType.RULE, null, mock(Rule.class));
        Policy inputPolicy = new Policy(null, TEST_POLICY_NAME, TENANT_DOMAIN, Arrays.asList(r1, r2));

        try {
            policyManagementService.addPolicy(inputPolicy, TENANT_DOMAIN);
            Assert.fail("Expected PolicyManagementException");
        } catch (PolicyManagementException expected) {
            // Expected.
        }

        // The first rule succeeded; it must be compensated after the second rule creation fails.
        verify(ruleManagementService).deleteRule("rule-1", TENANT_DOMAIN);
        verify(policyManagementDAO, never()).addPolicy(any(Policy.class), eq(TENANT_ID));
    }

    @Test
    public void testAddPolicyWithRule_CompensatesWhenPersistenceFails()
            throws RuleManagementException, PolicyManagementException {

        Rule created = mock(Rule.class);
        when(created.getId()).thenReturn("rule-1");
        when(ruleManagementService.addRule(any(Rule.class), eq(TENANT_DOMAIN))).thenReturn(created);
        when(policyManagementDAO.addPolicy(any(Policy.class), eq(TENANT_ID)))
                .thenThrow(PolicyManagementServerException.class);

        PolicyResource ruleRes = new PolicyResource(null, "ios", ResourceType.RULE, null, mock(Rule.class));
        Policy inputPolicy = new Policy(null, TEST_POLICY_NAME, TENANT_DOMAIN,
                Collections.singletonList(ruleRes));

        try {
            policyManagementService.addPolicy(inputPolicy, TENANT_DOMAIN);
            Assert.fail("Expected PolicyManagementException");
        } catch (PolicyManagementException expected) {
            // Expected.
        }

        // The rule created before the DB write failed must be compensated.
        verify(ruleManagementService).deleteRule("rule-1", TENANT_DOMAIN);
    }

    @Test
    public void testUpdatePolicyWithRuleResource_DeletesOldRulesAndAddsNew()
            throws PolicyManagementException, RuleManagementException {

        Policy existing = new Policy(TEST_POLICY_ID, TEST_POLICY_NAME, TENANT_DOMAIN,
                Collections.singletonList(
                        new PolicyResource(null, "ios", ResourceType.RULE, "old-rule", null)));
        when(policyManagementDAO.getPolicyById(TEST_POLICY_ID, TENANT_ID)).thenReturn(existing);

        Rule created = mock(Rule.class);
        when(created.getId()).thenReturn("new-rule");
        when(ruleManagementService.addRule(any(Rule.class), eq(TENANT_DOMAIN))).thenReturn(created);

        PolicyResource ruleRes = new PolicyResource(null, "ios", ResourceType.RULE, null, mock(Rule.class));
        Policy update = new Policy(TEST_POLICY_ID, TEST_POLICY_NAME, TENANT_DOMAIN,
                Collections.singletonList(ruleRes));
        when(policyManagementDAO.updatePolicy(any(Policy.class), eq(TENANT_ID))).thenReturn(update);

        policyManagementService.updatePolicy(update, TENANT_DOMAIN);

        verify(ruleManagementService).addRule(any(Rule.class), eq(TENANT_DOMAIN));
        verify(policyManagementDAO).updatePolicy(any(Policy.class), eq(TENANT_ID));
        verify(ruleManagementService).deleteRule("old-rule", TENANT_DOMAIN);
    }

    @Test
    public void testUpdatePolicyWithRule_KeepsOldRulesWhenPersistenceFails()
            throws PolicyManagementException, RuleManagementException {

        Policy existing = new Policy(TEST_POLICY_ID, TEST_POLICY_NAME, TENANT_DOMAIN,
                Collections.singletonList(
                        new PolicyResource(null, "ios", ResourceType.RULE, "old-rule", null)));
        when(policyManagementDAO.getPolicyById(TEST_POLICY_ID, TENANT_ID)).thenReturn(existing);

        Rule created = mock(Rule.class);
        when(created.getId()).thenReturn("new-rule");
        when(ruleManagementService.addRule(any(Rule.class), eq(TENANT_DOMAIN))).thenReturn(created);
        when(policyManagementDAO.updatePolicy(any(Policy.class), eq(TENANT_ID)))
                .thenThrow(PolicyManagementServerException.class);

        PolicyResource ruleRes = new PolicyResource(null, "ios", ResourceType.RULE, null, mock(Rule.class));
        Policy update = new Policy(TEST_POLICY_ID, TEST_POLICY_NAME, TENANT_DOMAIN,
                Collections.singletonList(ruleRes));

        try {
            policyManagementService.updatePolicy(update, TENANT_DOMAIN);
            Assert.fail("Expected PolicyManagementException");
        } catch (PolicyManagementException expected) {
            // Expected.
        }

        // The newly created rule must be compensated, but the old rule must survive the failed DB commit.
        verify(ruleManagementService).deleteRule("new-rule", TENANT_DOMAIN);
        verify(ruleManagementService, never()).deleteRule("old-rule", TENANT_DOMAIN);
    }

    @Test
    public void testDeletePolicyWithRuleResource_RemovesRulesFromRuleMgt()
            throws PolicyManagementException, RuleManagementException {

        Policy existing = new Policy(TEST_POLICY_ID, TEST_POLICY_NAME, TENANT_DOMAIN,
                Collections.singletonList(
                        new PolicyResource(null, "ios", ResourceType.RULE, "rule-1", null)));
        when(policyManagementDAO.getPolicyById(TEST_POLICY_ID, TENANT_ID)).thenReturn(existing);

        policyManagementService.deletePolicy(TEST_POLICY_ID, TENANT_DOMAIN);

        verify(policyManagementDAO).deletePolicy(TEST_POLICY_ID, TENANT_ID);
        verify(ruleManagementService).deleteRule("rule-1", TENANT_DOMAIN);
    }
}
