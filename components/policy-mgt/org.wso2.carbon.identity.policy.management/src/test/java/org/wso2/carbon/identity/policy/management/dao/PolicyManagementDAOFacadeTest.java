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

import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;
import org.wso2.carbon.identity.policy.management.api.exception.PolicyManagementException;
import org.wso2.carbon.identity.policy.management.api.exception.PolicyManagementServerException;
import org.wso2.carbon.identity.policy.management.api.model.Policy;
import org.wso2.carbon.identity.policy.management.api.model.PolicyResource;
import org.wso2.carbon.identity.policy.management.api.model.ResourceType;
import org.wso2.carbon.identity.policy.management.internal.component.PolicyMgtComponentServiceHolder;
import org.wso2.carbon.identity.policy.management.internal.dao.PolicyManagementDAO;
import org.wso2.carbon.identity.policy.management.internal.dao.impl.PolicyManagementDAOFacade;
import org.wso2.carbon.identity.rule.management.api.exception.RuleManagementException;
import org.wso2.carbon.identity.rule.management.api.model.Rule;
import org.wso2.carbon.identity.rule.management.api.service.RuleManagementService;

import java.util.Arrays;
import java.util.Collections;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for PolicyManagementDAOFacade.
 * Verifies the rule-mgt orchestration and best-effort saga compensation around the policy DB layer.
 */
public class PolicyManagementDAOFacadeTest {

    private static final String TENANT_DOMAIN = "carbon.super";
    private static final int TENANT_ID = -1234;
    private static final String POLICY_ID = UUID.randomUUID().toString();

    private PolicyManagementDAO policyManagementDAO;
    private RuleManagementService ruleManagementService;
    private PolicyManagementDAOFacade facade;
    private MockedStatic<IdentityTenantUtil> identityTenantUtil;

    @BeforeMethod
    public void setUp() {

        policyManagementDAO = mock(PolicyManagementDAO.class);
        ruleManagementService = mock(RuleManagementService.class);
        PolicyMgtComponentServiceHolder.getInstance().setRuleManagementService(ruleManagementService);
        facade = new PolicyManagementDAOFacade(policyManagementDAO);

        identityTenantUtil = org.mockito.Mockito.mockStatic(IdentityTenantUtil.class);
        identityTenantUtil.when(() -> IdentityTenantUtil.getTenantDomain(TENANT_ID)).thenReturn(TENANT_DOMAIN);
    }

    @AfterMethod
    public void tearDown() {

        identityTenantUtil.close();
    }

    private PolicyResource ruleResource(String target) {

        return new PolicyResource(null, target, ResourceType.RULE, null, mock(Rule.class));
    }

    private Rule createdRule(String ruleId) {

        Rule rule = mock(Rule.class);
        when(rule.getId()).thenReturn(ruleId);
        return rule;
    }

    @Test
    public void testAddPolicyCreatesRuleAndPersistsWithResourceId() throws PolicyManagementException,
            RuleManagementException {

        Policy policy = new Policy(POLICY_ID, "P", TENANT_DOMAIN,
                Collections.singletonList(ruleResource("ios")));
        Rule created = createdRule("rule-1");
        when(ruleManagementService.addRule(any(Rule.class), eq(TENANT_DOMAIN))).thenReturn(created);

        facade.addPolicy(policy, TENANT_ID);

        verify(ruleManagementService).addRule(any(Rule.class), eq(TENANT_DOMAIN));
        ArgumentCaptor<Policy> captor = ArgumentCaptor.forClass(Policy.class);
        verify(policyManagementDAO).addPolicy(captor.capture(), eq(TENANT_ID));
        PolicyResource persisted = captor.getValue().getResources().get(0);
        Assert.assertEquals(persisted.getResourceType(), ResourceType.RULE);
        Assert.assertEquals(persisted.getResourceId(), "rule-1");
        Assert.assertEquals(persisted.getTarget(), "ios");
    }

    @Test
    public void testAddPolicyPassesThroughNonRuleResource() throws PolicyManagementException,
            RuleManagementException {

        PolicyResource action = new PolicyResource(null, "ios", ResourceType.ACTION, "action-1", null);
        Policy policy = new Policy(POLICY_ID, "P", TENANT_DOMAIN, Collections.singletonList(action));

        facade.addPolicy(policy, TENANT_ID);

        // ACTION resources are not created through rule-mgt; they pass through unchanged.
        verify(ruleManagementService, never()).addRule(any(Rule.class), eq(TENANT_DOMAIN));
        ArgumentCaptor<Policy> captor = ArgumentCaptor.forClass(Policy.class);
        verify(policyManagementDAO).addPolicy(captor.capture(), eq(TENANT_ID));
        PolicyResource persisted = captor.getValue().getResources().get(0);
        Assert.assertEquals(persisted.getResourceType(), ResourceType.ACTION);
        Assert.assertEquals(persisted.getResourceId(), "action-1");
    }

    @Test
    public void testAddPolicyCompensatesWhenRuleCreationFails() throws RuleManagementException,
            PolicyManagementException {

        Policy policy = new Policy(POLICY_ID, "P", TENANT_DOMAIN,
                Arrays.asList(ruleResource("ios"), ruleResource("android")));
        // First rule succeeds, second fails — the first must be rolled back.
        Rule created = createdRule("rule-1");
        when(ruleManagementService.addRule(any(Rule.class), eq(TENANT_DOMAIN)))
                .thenReturn(created)
                .thenThrow(RuleManagementException.class);

        try {
            facade.addPolicy(policy, TENANT_ID);
            Assert.fail("Expected PolicyManagementException");
        } catch (PolicyManagementException expected) {
            // Expected.
        }

        verify(ruleManagementService).deleteRule("rule-1", TENANT_DOMAIN);
        verify(policyManagementDAO, never()).addPolicy(any(Policy.class), eq(TENANT_ID));
    }

    @Test
    public void testAddPolicyCompensatesWhenPersistenceFails() throws RuleManagementException,
            PolicyManagementException {

        Policy policy = new Policy(POLICY_ID, "P", TENANT_DOMAIN,
                Collections.singletonList(ruleResource("ios")));
        Rule created = createdRule("rule-1");
        when(ruleManagementService.addRule(any(Rule.class), eq(TENANT_DOMAIN))).thenReturn(created);
        when(policyManagementDAO.addPolicy(any(Policy.class), eq(TENANT_ID)))
                .thenThrow(PolicyManagementServerException.class);

        try {
            facade.addPolicy(policy, TENANT_ID);
            Assert.fail("Expected PolicyManagementException");
        } catch (PolicyManagementException expected) {
            // Expected.
        }

        // The rule created before the DB write failed must be compensated.
        verify(ruleManagementService).deleteRule("rule-1", TENANT_DOMAIN);
    }

    @Test
    public void testUpdatePolicyDeletesOldRulesAndAddsNew() throws PolicyManagementException,
            RuleManagementException {

        Policy existing = new Policy(POLICY_ID, "P", TENANT_DOMAIN,
                Collections.singletonList(new PolicyResource(null, "ios", ResourceType.RULE, "old-rule", null)));
        when(policyManagementDAO.getPolicyById(POLICY_ID, TENANT_ID)).thenReturn(existing);
        Rule created = createdRule("new-rule");
        when(ruleManagementService.addRule(any(Rule.class), eq(TENANT_DOMAIN))).thenReturn(created);

        Policy update = new Policy(POLICY_ID, "P", TENANT_DOMAIN,
                Collections.singletonList(ruleResource("ios")));
        facade.updatePolicy(update, TENANT_ID);

        verify(ruleManagementService).deleteRule("old-rule", TENANT_DOMAIN);
        verify(ruleManagementService).addRule(any(Rule.class), eq(TENANT_DOMAIN));
        verify(policyManagementDAO).updatePolicy(any(Policy.class), eq(TENANT_ID));
    }

    @Test
    public void testDeletePolicyRemovesRulesFromRuleMgt() throws PolicyManagementException,
            RuleManagementException {

        Policy existing = new Policy(POLICY_ID, "P", TENANT_DOMAIN,
                Collections.singletonList(new PolicyResource(null, "ios", ResourceType.RULE, "rule-1", null)));
        when(policyManagementDAO.getPolicyById(POLICY_ID, TENANT_ID)).thenReturn(existing);

        facade.deletePolicy(POLICY_ID, TENANT_ID);

        verify(policyManagementDAO).deletePolicy(POLICY_ID, TENANT_ID);
        verify(ruleManagementService).deleteRule("rule-1", TENANT_DOMAIN);
    }
}
