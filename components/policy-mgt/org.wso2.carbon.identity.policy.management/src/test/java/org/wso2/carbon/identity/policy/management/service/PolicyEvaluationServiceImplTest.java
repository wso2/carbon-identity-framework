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

import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.policy.management.api.exception.PolicyManagementException;
import org.wso2.carbon.identity.policy.management.api.model.Policy;
import org.wso2.carbon.identity.policy.management.api.model.PolicyResource;
import org.wso2.carbon.identity.policy.management.api.model.ResourceType;
import org.wso2.carbon.identity.policy.management.api.service.PolicyManagementService;
import org.wso2.carbon.identity.policy.management.internal.component.PolicyMgtComponentServiceHolder;
import org.wso2.carbon.identity.policy.management.internal.service.impl.PolicyEvaluationServiceImpl;
import org.wso2.carbon.identity.rule.evaluation.api.exception.RuleEvaluationException;
import org.wso2.carbon.identity.rule.evaluation.api.model.FlowContext;
import org.wso2.carbon.identity.rule.evaluation.api.model.RuleEvaluationResult;
import org.wso2.carbon.identity.rule.evaluation.api.service.RuleEvaluationService;
import org.wso2.carbon.identity.rule.management.api.model.Rule;

import java.util.Collections;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for PolicyEvaluationServiceImpl.
 */
public class PolicyEvaluationServiceImplTest {

    private static final String POLICY_NAME = "TestPolicy";
    private static final String TENANT_DOMAIN = "carbon.super";
    private static final String RULE_ID = UUID.randomUUID().toString();

    private PolicyManagementService policyManagementService;
    private RuleEvaluationService ruleEvaluationService;
    private PolicyEvaluationServiceImpl policyEvaluationService;
    private FlowContext flowContext;

    @BeforeMethod
    public void setUp() {

        policyManagementService = mock(PolicyManagementService.class);
        ruleEvaluationService = mock(RuleEvaluationService.class);
        PolicyMgtComponentServiceHolder.getInstance().setPolicyManagementService(policyManagementService);
        PolicyMgtComponentServiceHolder.getInstance().setRuleEvaluationService(ruleEvaluationService);
        policyEvaluationService = PolicyEvaluationServiceImpl.getInstance();
        flowContext = mock(FlowContext.class);
    }

    private Policy policyWithRule(String target) {

        Rule rule = mock(Rule.class);
        when(rule.getId()).thenReturn(RULE_ID);
        PolicyResource resource = new PolicyResource(
                UUID.randomUUID().toString(), target, ResourceType.RULE, RULE_ID, rule);
        return new Policy(UUID.randomUUID().toString(), POLICY_NAME, TENANT_DOMAIN,
                Collections.singletonList(resource));
    }

    @Test
    public void testPolicyNotFoundReturnsNull() throws PolicyManagementException, RuleEvaluationException {

        when(policyManagementService.getPolicyByName(POLICY_NAME, TENANT_DOMAIN)).thenReturn(null);

        RuleEvaluationResult result = policyEvaluationService.evaluate(
                POLICY_NAME, "ios", flowContext, TENANT_DOMAIN);

        Assert.assertNull(result);
    }

    @Test
    public void testNoMatchingResourceReturnsCompliant() throws PolicyManagementException, RuleEvaluationException {

        Policy policy = policyWithRule("android");
        when(policyManagementService.getPolicyByName(POLICY_NAME, TENANT_DOMAIN)).thenReturn(policy);

        RuleEvaluationResult result = policyEvaluationService.evaluate(
                POLICY_NAME, "ios", flowContext, TENANT_DOMAIN);

        Assert.assertNotNull(result);
        Assert.assertTrue(result.isRuleSatisfied());
        verify(ruleEvaluationService, org.mockito.Mockito.never())
                .evaluate(eq(RULE_ID), eq(flowContext), eq(TENANT_DOMAIN));
    }

    @Test
    public void testMatchingRuleDelegatesToRuleEvaluation() throws PolicyManagementException, RuleEvaluationException {

        Policy policy = policyWithRule("ios");
        when(policyManagementService.getPolicyByName(POLICY_NAME, TENANT_DOMAIN)).thenReturn(policy);
        RuleEvaluationResult expected = mock(RuleEvaluationResult.class);
        when(ruleEvaluationService.evaluate(RULE_ID, flowContext, TENANT_DOMAIN)).thenReturn(expected);

        RuleEvaluationResult result = policyEvaluationService.evaluate(
                POLICY_NAME, "ios", flowContext, TENANT_DOMAIN);

        Assert.assertEquals(result, expected);
        verify(ruleEvaluationService).evaluate(RULE_ID, flowContext, TENANT_DOMAIN);
    }

    @Test
    public void testSelectorMatchIsCaseInsensitive() throws PolicyManagementException, RuleEvaluationException {

        Policy policy = policyWithRule("iOS");
        when(policyManagementService.getPolicyByName(POLICY_NAME, TENANT_DOMAIN)).thenReturn(policy);
        RuleEvaluationResult expected = mock(RuleEvaluationResult.class);
        when(ruleEvaluationService.evaluate(RULE_ID, flowContext, TENANT_DOMAIN)).thenReturn(expected);

        RuleEvaluationResult result = policyEvaluationService.evaluate(
                POLICY_NAME, "ios", flowContext, TENANT_DOMAIN);

        Assert.assertEquals(result, expected);
        verify(ruleEvaluationService).evaluate(RULE_ID, flowContext, TENANT_DOMAIN);
    }
}
