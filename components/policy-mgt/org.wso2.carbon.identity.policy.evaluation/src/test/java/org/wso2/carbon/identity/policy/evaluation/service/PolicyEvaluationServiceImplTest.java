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

package org.wso2.carbon.identity.policy.evaluation.service;

import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.policy.evaluation.api.evaluator.PolicyResourceEvaluator;
import org.wso2.carbon.identity.policy.evaluation.api.exception.PolicyEvaluationException;
import org.wso2.carbon.identity.policy.evaluation.api.model.PolicyEvaluationResult;
import org.wso2.carbon.identity.policy.evaluation.api.model.ResourceEvaluationOutcome;
import org.wso2.carbon.identity.policy.evaluation.internal.component.PolicyEvaluationComponentServiceHolder;
import org.wso2.carbon.identity.policy.evaluation.internal.evaluator.RuleResourceEvaluator;
import org.wso2.carbon.identity.policy.evaluation.internal.service.impl.PolicyEvaluationServiceImpl;
import org.wso2.carbon.identity.policy.management.api.exception.PolicyManagementException;
import org.wso2.carbon.identity.policy.management.api.model.Policy;
import org.wso2.carbon.identity.policy.management.api.model.PolicyResource;
import org.wso2.carbon.identity.policy.management.api.model.ResourceType;
import org.wso2.carbon.identity.policy.management.api.model.RulePolicyResource;
import org.wso2.carbon.identity.policy.management.api.service.PolicyManagementService;
import org.wso2.carbon.identity.rule.evaluation.api.exception.RuleEvaluationException;
import org.wso2.carbon.identity.rule.evaluation.api.model.FlowContext;
import org.wso2.carbon.identity.rule.evaluation.api.model.RuleEvaluationResult;
import org.wso2.carbon.identity.rule.evaluation.api.service.RuleEvaluationService;
import org.wso2.carbon.identity.rule.management.api.model.Rule;

import java.util.Arrays;
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
    private static final String POLICY_ID = UUID.randomUUID().toString();
    private static final String TENANT_DOMAIN = "carbon.super";
    private static final String RULE_ID = UUID.randomUUID().toString();

    private PolicyManagementService policyManagementService;
    private RuleEvaluationService ruleEvaluationService;
    private PolicyEvaluationServiceImpl policyEvaluationService;
    private FlowContext flowContext;

    /**
     * Minimal non-rule stand-in used to exercise dispatch for a resource type, without introducing
     * a production ActionPolicyResource class.
     */
    private static class StubActionPolicyResource extends PolicyResource {

        StubActionPolicyResource(String id, String target, String resourceId) {

            super(id, target, resourceId);
        }

        @Override
        public ResourceType getResourceType() {

            return ResourceType.ACTION;
        }
    }

    @BeforeMethod
    public void setUp() {

        policyManagementService = mock(PolicyManagementService.class);
        ruleEvaluationService = mock(RuleEvaluationService.class);
        PolicyEvaluationComponentServiceHolder.getInstance().setPolicyManagementService(policyManagementService);
        PolicyEvaluationComponentServiceHolder.getInstance().setRuleEvaluationService(ruleEvaluationService);
        PolicyEvaluationComponentServiceHolder.getInstance().addPolicyResourceEvaluator(new RuleResourceEvaluator());
        policyEvaluationService = new PolicyEvaluationServiceImpl();
        flowContext = mock(FlowContext.class);
    }

    private Policy policyWithRule(String target) {

        Rule rule = mock(Rule.class);
        when(rule.getId()).thenReturn(RULE_ID);
        PolicyResource resource = new RulePolicyResource(
                UUID.randomUUID().toString(), target, RULE_ID, rule);
        return new Policy(UUID.randomUUID().toString(), POLICY_NAME, TENANT_DOMAIN,
                Collections.singletonList(resource));
    }

    private PolicyResourceEvaluator stubEvaluator(ResourceType resourceType, boolean satisfied) {

        return new PolicyResourceEvaluator() {

            @Override
            public ResourceType getSupportedResourceType() {

                return resourceType;
            }

            @Override
            public ResourceEvaluationOutcome evaluate(PolicyResource resource, FlowContext flowContext,
                                                      String tenantDomain) {

                return new ResourceEvaluationOutcome(resource.getResourceId(), resourceType, satisfied);
            }
        };
    }

    @Test
    public void testPolicyNotFoundReturnsNull() throws PolicyManagementException, PolicyEvaluationException {

        when(policyManagementService.getPolicyById(POLICY_ID, TENANT_DOMAIN)).thenReturn(null);

        PolicyEvaluationResult result = policyEvaluationService.evaluate(POLICY_ID, "ios", flowContext, TENANT_DOMAIN);

        Assert.assertNull(result);
    }

    @Test
    public void testNoMatchingResourceReturnsCompliant() throws PolicyManagementException, RuleEvaluationException,
            PolicyEvaluationException {

        Policy policy = policyWithRule("android");
        when(policyManagementService.getPolicyById(POLICY_ID, TENANT_DOMAIN)).thenReturn(policy);

        PolicyEvaluationResult result = policyEvaluationService.evaluate(POLICY_ID, "ios", flowContext, TENANT_DOMAIN);

        Assert.assertNotNull(result);
        Assert.assertTrue(result.isSatisfied());
        Assert.assertTrue(result.getOutcomes().isEmpty());
        verify(ruleEvaluationService, org.mockito.Mockito.never())
                .evaluate(eq(RULE_ID), eq(flowContext), eq(TENANT_DOMAIN));
    }

    @Test
    public void testSingleRuleSatisfied() throws PolicyManagementException, RuleEvaluationException,
            PolicyEvaluationException {

        Policy policy = policyWithRule("ios");
        when(policyManagementService.getPolicyById(POLICY_ID, TENANT_DOMAIN)).thenReturn(policy);
        when(ruleEvaluationService.evaluate(RULE_ID, flowContext, TENANT_DOMAIN))
                .thenReturn(new RuleEvaluationResult(RULE_ID, true));

        PolicyEvaluationResult result = policyEvaluationService.evaluate(POLICY_ID, "ios", flowContext, TENANT_DOMAIN);

        Assert.assertTrue(result.isSatisfied());
        Assert.assertEquals(result.getOutcomes().size(), 1);
        Assert.assertTrue(result.getOutcomes().get(0).isSatisfied());
        Assert.assertEquals(result.getOutcomes().get(0).getResourceType(), ResourceType.RULE);
        Assert.assertEquals(result.getOutcomes().get(0).getResourceId(), RULE_ID);
        verify(ruleEvaluationService).evaluate(RULE_ID, flowContext, TENANT_DOMAIN);
    }

    @Test
    public void testSingleRuleNotSatisfied() throws PolicyManagementException, RuleEvaluationException,
            PolicyEvaluationException {

        Policy policy = policyWithRule("ios");
        when(policyManagementService.getPolicyById(POLICY_ID, TENANT_DOMAIN)).thenReturn(policy);
        when(ruleEvaluationService.evaluate(RULE_ID, flowContext, TENANT_DOMAIN))
                .thenReturn(new RuleEvaluationResult(RULE_ID, false));

        PolicyEvaluationResult result = policyEvaluationService.evaluate(POLICY_ID, "ios", flowContext, TENANT_DOMAIN);

        Assert.assertFalse(result.isSatisfied());
        Assert.assertEquals(result.getOutcomes().size(), 1);
        Assert.assertFalse(result.getOutcomes().get(0).isSatisfied());
    }

    @Test
    public void testNullSelectorReturnsCompliant() throws PolicyManagementException, PolicyEvaluationException {

        Policy policy = policyWithRule("ios");
        when(policyManagementService.getPolicyById(POLICY_ID, TENANT_DOMAIN)).thenReturn(policy);

        PolicyEvaluationResult result = policyEvaluationService.evaluate(POLICY_ID, null, flowContext, TENANT_DOMAIN);

        Assert.assertTrue(result.isSatisfied());
        Assert.assertTrue(result.getOutcomes().isEmpty());
    }

    @Test
    public void testResourceWithNullTargetIsSkipped() throws PolicyManagementException, PolicyEvaluationException {

        Rule rule = mock(Rule.class);
        when(rule.getId()).thenReturn(RULE_ID);
        // Resource with a null target — the filter must not NPE on equalsIgnoreCase.
        PolicyResource nullTargetResource = new RulePolicyResource(
                UUID.randomUUID().toString(), null, RULE_ID, rule);
        Policy policy = new Policy(UUID.randomUUID().toString(), POLICY_NAME, TENANT_DOMAIN,
                Collections.singletonList(nullTargetResource));
        when(policyManagementService.getPolicyById(POLICY_ID, TENANT_DOMAIN)).thenReturn(policy);

        PolicyEvaluationResult result = policyEvaluationService.evaluate(POLICY_ID, "ios", flowContext, TENANT_DOMAIN);

        Assert.assertTrue(result.isSatisfied());
        Assert.assertTrue(result.getOutcomes().isEmpty());
    }

    @Test
    public void testSelectorMatchIsCaseInsensitive() throws PolicyManagementException, RuleEvaluationException,
            PolicyEvaluationException {

        Policy policy = policyWithRule("iOS");
        when(policyManagementService.getPolicyById(POLICY_ID, TENANT_DOMAIN)).thenReturn(policy);
        when(ruleEvaluationService.evaluate(RULE_ID, flowContext, TENANT_DOMAIN))
                .thenReturn(new RuleEvaluationResult(RULE_ID, true));

        PolicyEvaluationResult result = policyEvaluationService.evaluate(POLICY_ID, "ios", flowContext, TENANT_DOMAIN);

        Assert.assertTrue(result.isSatisfied());
        verify(ruleEvaluationService).evaluate(RULE_ID, flowContext, TENANT_DOMAIN);
    }

    @Test
    public void testMultipleResourcesAndSemantics_AllSatisfied() throws PolicyManagementException,
            RuleEvaluationException, PolicyEvaluationException {

        PolicyResourceEvaluator stubActionEvaluator = stubEvaluator(ResourceType.ACTION, true);
        PolicyEvaluationComponentServiceHolder.getInstance().addPolicyResourceEvaluator(stubActionEvaluator);
        try {
            Rule rule = mock(Rule.class);
            when(rule.getId()).thenReturn(RULE_ID);
            PolicyResource ruleResource = new RulePolicyResource(UUID.randomUUID().toString(), "ios", RULE_ID, rule);
            PolicyResource actionResource = new StubActionPolicyResource(
                    UUID.randomUUID().toString(), "ios", "action-1");
            Policy policy = new Policy(UUID.randomUUID().toString(), POLICY_NAME, TENANT_DOMAIN,
                    Arrays.asList(ruleResource, actionResource));
            when(policyManagementService.getPolicyById(POLICY_ID, TENANT_DOMAIN)).thenReturn(policy);
            when(ruleEvaluationService.evaluate(RULE_ID, flowContext, TENANT_DOMAIN))
                    .thenReturn(new RuleEvaluationResult(RULE_ID, true));

            PolicyEvaluationResult result = policyEvaluationService.evaluate(
                    POLICY_ID, "ios", flowContext, TENANT_DOMAIN);

            Assert.assertTrue(result.isSatisfied());
            Assert.assertEquals(result.getOutcomes().size(), 2);
        } finally {
            PolicyEvaluationComponentServiceHolder.getInstance().removePolicyResourceEvaluator(stubActionEvaluator);
        }
    }

    @Test
    public void testMultipleResourcesAndSemantics_OneUnsatisfiedFailsAll() throws PolicyManagementException,
            RuleEvaluationException, PolicyEvaluationException {

        PolicyResourceEvaluator stubActionEvaluator = stubEvaluator(ResourceType.ACTION, false);
        PolicyEvaluationComponentServiceHolder.getInstance().addPolicyResourceEvaluator(stubActionEvaluator);
        try {
            Rule rule = mock(Rule.class);
            when(rule.getId()).thenReturn(RULE_ID);
            PolicyResource ruleResource = new RulePolicyResource(UUID.randomUUID().toString(), "ios", RULE_ID, rule);
            PolicyResource actionResource = new StubActionPolicyResource(
                    UUID.randomUUID().toString(), "ios", "action-1");
            Policy policy = new Policy(UUID.randomUUID().toString(), POLICY_NAME, TENANT_DOMAIN,
                    Arrays.asList(ruleResource, actionResource));
            when(policyManagementService.getPolicyById(POLICY_ID, TENANT_DOMAIN)).thenReturn(policy);
            when(ruleEvaluationService.evaluate(RULE_ID, flowContext, TENANT_DOMAIN))
                    .thenReturn(new RuleEvaluationResult(RULE_ID, true));

            PolicyEvaluationResult result = policyEvaluationService.evaluate(
                    POLICY_ID, "ios", flowContext, TENANT_DOMAIN);

            Assert.assertFalse(result.isSatisfied());
            Assert.assertEquals(result.getOutcomes().size(), 2);
        } finally {
            PolicyEvaluationComponentServiceHolder.getInstance().removePolicyResourceEvaluator(stubActionEvaluator);
        }
    }

    @Test(expectedExceptions = PolicyEvaluationException.class)
    public void testMissingEvaluatorThrows() throws PolicyManagementException, PolicyEvaluationException {

        PolicyResource actionResource = new StubActionPolicyResource(
                UUID.randomUUID().toString(), "ios", "action-1");
        Policy policy = new Policy(UUID.randomUUID().toString(), POLICY_NAME, TENANT_DOMAIN,
                Collections.singletonList(actionResource));
        when(policyManagementService.getPolicyById(POLICY_ID, TENANT_DOMAIN)).thenReturn(policy);

        policyEvaluationService.evaluate(POLICY_ID, "ios", flowContext, TENANT_DOMAIN);
    }

    @Test
    public void testPolicyManagementExceptionIsWrapped() throws PolicyManagementException {

        when(policyManagementService.getPolicyById(POLICY_ID, TENANT_DOMAIN))
                .thenThrow(new PolicyManagementException("boom", "boom description", "PM-000"));

        try {
            policyEvaluationService.evaluate(POLICY_ID, "ios", flowContext, TENANT_DOMAIN);
            Assert.fail("Expected PolicyEvaluationException");
        } catch (PolicyEvaluationException e) {
            Assert.assertTrue(e.getCause() instanceof PolicyManagementException);
        }
    }

    @Test
    public void testRuleEvaluationExceptionIsWrapped() throws PolicyManagementException, RuleEvaluationException {

        Policy policy = policyWithRule("ios");
        when(policyManagementService.getPolicyById(POLICY_ID, TENANT_DOMAIN)).thenReturn(policy);
        when(ruleEvaluationService.evaluate(RULE_ID, flowContext, TENANT_DOMAIN))
                .thenThrow(new RuleEvaluationException("boom"));

        try {
            policyEvaluationService.evaluate(POLICY_ID, "ios", flowContext, TENANT_DOMAIN);
            Assert.fail("Expected PolicyEvaluationException");
        } catch (PolicyEvaluationException e) {
            Assert.assertTrue(e.getCause() instanceof RuleEvaluationException);
        }
    }
}
