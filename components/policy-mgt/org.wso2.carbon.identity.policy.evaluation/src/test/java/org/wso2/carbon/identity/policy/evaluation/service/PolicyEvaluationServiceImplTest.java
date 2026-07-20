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

import org.mockito.MockedStatic;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.central.log.mgt.utils.LoggerUtils;
import org.wso2.carbon.identity.policy.evaluation.api.exception.PolicyEvaluationException;
import org.wso2.carbon.identity.policy.evaluation.api.model.PolicyEvaluationContext;
import org.wso2.carbon.identity.policy.evaluation.api.model.PolicyEvaluationResult;
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
import org.wso2.carbon.utils.DiagnosticLog;

import java.util.Arrays;
import java.util.Collections;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
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
    private PolicyEvaluationContext context;
    private MockedStatic<LoggerUtils> loggerUtils;

    @BeforeMethod
    public void setUp() {

        policyManagementService = mock(PolicyManagementService.class);
        ruleEvaluationService = mock(RuleEvaluationService.class);
        PolicyEvaluationComponentServiceHolder.getInstance().setPolicyManagementService(policyManagementService);
        PolicyEvaluationComponentServiceHolder.getInstance().setRuleEvaluationService(ruleEvaluationService);
        PolicyEvaluationComponentServiceHolder.getInstance().addPolicyResourceEvaluator(new RuleResourceEvaluator());
        policyEvaluationService = new PolicyEvaluationServiceImpl();
        context = PolicyEvaluationContext.create("PRE_ISSUE_ACCESS_TOKEN");

        loggerUtils = mockStatic(LoggerUtils.class);
        loggerUtils.when(LoggerUtils::isDiagnosticLogsEnabled).thenReturn(false);
    }

    @AfterMethod
    public void tearDown() {

        loggerUtils.close();
    }

    private Policy policyWithRule(String target) {

        Rule rule = mock(Rule.class);
        when(rule.getId()).thenReturn(RULE_ID);
        PolicyResource resource = new RulePolicyResource(
                UUID.randomUUID().toString(), target, RULE_ID, rule);
        return new Policy(UUID.randomUUID().toString(), POLICY_NAME, TENANT_DOMAIN,
                Collections.singletonList(resource));
    }

    @Test(expectedExceptions = PolicyEvaluationException.class)
    public void testPolicyNotFoundThrows() throws PolicyManagementException, PolicyEvaluationException {

        when(policyManagementService.getPolicyById(POLICY_ID, TENANT_DOMAIN)).thenReturn(null);

        policyEvaluationService.evaluate(POLICY_ID, "ios", context, TENANT_DOMAIN);
    }

    @Test
    public void testNoMatchingResourceReturnsCompliant() throws PolicyManagementException, RuleEvaluationException,
            PolicyEvaluationException {

        Policy policy = policyWithRule("android");
        when(policyManagementService.getPolicyById(POLICY_ID, TENANT_DOMAIN)).thenReturn(policy);

        PolicyEvaluationResult result = policyEvaluationService.evaluate(POLICY_ID, "ios", context, TENANT_DOMAIN);

        Assert.assertNotNull(result);
        Assert.assertTrue(result.isSatisfied());
        Assert.assertTrue(result.getResults().isEmpty());
        verify(ruleEvaluationService, org.mockito.Mockito.never())
                .evaluate(eq(RULE_ID), any(FlowContext.class), eq(TENANT_DOMAIN));
    }

    @Test
    public void testSingleRuleSatisfied() throws PolicyManagementException, RuleEvaluationException,
            PolicyEvaluationException {

        Policy policy = policyWithRule("ios");
        when(policyManagementService.getPolicyById(POLICY_ID, TENANT_DOMAIN)).thenReturn(policy);
        when(ruleEvaluationService.evaluate(eq(RULE_ID), any(FlowContext.class), eq(TENANT_DOMAIN)))
                .thenReturn(new RuleEvaluationResult(RULE_ID, true));

        PolicyEvaluationResult result = policyEvaluationService.evaluate(POLICY_ID, "ios", context, TENANT_DOMAIN);

        Assert.assertTrue(result.isSatisfied());
        Assert.assertEquals(result.getResults().size(), 1);
        Assert.assertTrue(result.getResults().get(0).isSatisfied());
        Assert.assertEquals(result.getResults().get(0).getResourceType(), ResourceType.RULE);
        Assert.assertEquals(result.getResults().get(0).getResourceId(), RULE_ID);
        verify(ruleEvaluationService).evaluate(eq(RULE_ID), any(FlowContext.class), eq(TENANT_DOMAIN));
        loggerUtils.verify(() -> LoggerUtils.triggerDiagnosticLogEvent(
                any(DiagnosticLog.DiagnosticLogBuilder.class)), never());
    }

    @Test
    public void testSingleRuleNotSatisfied() throws PolicyManagementException, RuleEvaluationException,
            PolicyEvaluationException {

        Policy policy = policyWithRule("ios");
        when(policyManagementService.getPolicyById(POLICY_ID, TENANT_DOMAIN)).thenReturn(policy);
        when(ruleEvaluationService.evaluate(eq(RULE_ID), any(FlowContext.class), eq(TENANT_DOMAIN)))
                .thenReturn(new RuleEvaluationResult(RULE_ID, false));

        PolicyEvaluationResult result = policyEvaluationService.evaluate(POLICY_ID, "ios", context, TENANT_DOMAIN);

        Assert.assertFalse(result.isSatisfied());
        Assert.assertEquals(result.getResults().size(), 1);
        Assert.assertFalse(result.getResults().get(0).isSatisfied());
    }

    @Test
    public void testNullSelectorReturnsCompliant() throws PolicyManagementException, PolicyEvaluationException {

        Policy policy = policyWithRule("ios");
        when(policyManagementService.getPolicyById(POLICY_ID, TENANT_DOMAIN)).thenReturn(policy);

        PolicyEvaluationResult result = policyEvaluationService.evaluate(POLICY_ID, null, context, TENANT_DOMAIN);

        Assert.assertTrue(result.isSatisfied());
        Assert.assertTrue(result.getResults().isEmpty());
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

        PolicyEvaluationResult result = policyEvaluationService.evaluate(POLICY_ID, "ios", context, TENANT_DOMAIN);

        Assert.assertTrue(result.isSatisfied());
        Assert.assertTrue(result.getResults().isEmpty());
    }

    @Test
    public void testSelectorMatchIsCaseInsensitive() throws PolicyManagementException, RuleEvaluationException,
            PolicyEvaluationException {

        Policy policy = policyWithRule("iOS");
        when(policyManagementService.getPolicyById(POLICY_ID, TENANT_DOMAIN)).thenReturn(policy);
        when(ruleEvaluationService.evaluate(eq(RULE_ID), any(FlowContext.class), eq(TENANT_DOMAIN)))
                .thenReturn(new RuleEvaluationResult(RULE_ID, true));

        PolicyEvaluationResult result = policyEvaluationService.evaluate(POLICY_ID, "ios", context, TENANT_DOMAIN);

        Assert.assertTrue(result.isSatisfied());
        verify(ruleEvaluationService).evaluate(eq(RULE_ID), any(FlowContext.class), eq(TENANT_DOMAIN));
    }

    @Test
    public void testMultipleResourcesAndSemantics_AllSatisfied() throws PolicyManagementException,
            RuleEvaluationException, PolicyEvaluationException {

        String secondRuleId = UUID.randomUUID().toString();
        Rule rule1 = mock(Rule.class);
        when(rule1.getId()).thenReturn(RULE_ID);
        Rule rule2 = mock(Rule.class);
        when(rule2.getId()).thenReturn(secondRuleId);
        PolicyResource ruleResource1 = new RulePolicyResource(UUID.randomUUID().toString(), "ios", RULE_ID, rule1);
        PolicyResource ruleResource2 = new RulePolicyResource(
                UUID.randomUUID().toString(), "ios", secondRuleId, rule2);
        Policy policy = new Policy(UUID.randomUUID().toString(), POLICY_NAME, TENANT_DOMAIN,
                Arrays.asList(ruleResource1, ruleResource2));
        when(policyManagementService.getPolicyById(POLICY_ID, TENANT_DOMAIN)).thenReturn(policy);
        when(ruleEvaluationService.evaluate(eq(RULE_ID), any(FlowContext.class), eq(TENANT_DOMAIN)))
                .thenReturn(new RuleEvaluationResult(RULE_ID, true));
        when(ruleEvaluationService.evaluate(eq(secondRuleId), any(FlowContext.class), eq(TENANT_DOMAIN)))
                .thenReturn(new RuleEvaluationResult(secondRuleId, true));

        PolicyEvaluationResult result = policyEvaluationService.evaluate(POLICY_ID, "ios", context, TENANT_DOMAIN);

        Assert.assertTrue(result.isSatisfied());
        Assert.assertEquals(result.getResults().size(), 2);
    }

    @Test
    public void testMultipleResourcesAndSemantics_OneUnsatisfiedFailsAll() throws PolicyManagementException,
            RuleEvaluationException, PolicyEvaluationException {

        String secondRuleId = UUID.randomUUID().toString();
        Rule rule1 = mock(Rule.class);
        when(rule1.getId()).thenReturn(RULE_ID);
        Rule rule2 = mock(Rule.class);
        when(rule2.getId()).thenReturn(secondRuleId);
        PolicyResource ruleResource1 = new RulePolicyResource(UUID.randomUUID().toString(), "ios", RULE_ID, rule1);
        PolicyResource ruleResource2 = new RulePolicyResource(
                UUID.randomUUID().toString(), "ios", secondRuleId, rule2);
        Policy policy = new Policy(UUID.randomUUID().toString(), POLICY_NAME, TENANT_DOMAIN,
                Arrays.asList(ruleResource1, ruleResource2));
        when(policyManagementService.getPolicyById(POLICY_ID, TENANT_DOMAIN)).thenReturn(policy);
        when(ruleEvaluationService.evaluate(eq(RULE_ID), any(FlowContext.class), eq(TENANT_DOMAIN)))
                .thenReturn(new RuleEvaluationResult(RULE_ID, true));
        when(ruleEvaluationService.evaluate(eq(secondRuleId), any(FlowContext.class), eq(TENANT_DOMAIN)))
                .thenReturn(new RuleEvaluationResult(secondRuleId, false));

        PolicyEvaluationResult result = policyEvaluationService.evaluate(POLICY_ID, "ios", context, TENANT_DOMAIN);

        Assert.assertFalse(result.isSatisfied());
        Assert.assertEquals(result.getResults().size(), 2);
    }

    @Test(expectedExceptions = PolicyEvaluationException.class)
    public void testMissingEvaluatorThrows() throws PolicyManagementException, PolicyEvaluationException {

        RuleResourceEvaluator ruleResourceEvaluator = new RuleResourceEvaluator();
        PolicyEvaluationComponentServiceHolder.getInstance().removePolicyResourceEvaluator(ruleResourceEvaluator);
        try {
            Policy policy = policyWithRule("ios");
            when(policyManagementService.getPolicyById(POLICY_ID, TENANT_DOMAIN)).thenReturn(policy);

            policyEvaluationService.evaluate(POLICY_ID, "ios", context, TENANT_DOMAIN);
        } finally {
            PolicyEvaluationComponentServiceHolder.getInstance().addPolicyResourceEvaluator(ruleResourceEvaluator);
        }
    }

    @Test
    public void testPolicyManagementExceptionIsWrapped() throws PolicyManagementException {

        when(policyManagementService.getPolicyById(POLICY_ID, TENANT_DOMAIN))
                .thenThrow(new PolicyManagementException("boom", "boom description", "PM-000"));

        try {
            policyEvaluationService.evaluate(POLICY_ID, "ios", context, TENANT_DOMAIN);
            Assert.fail("Expected PolicyEvaluationException");
        } catch (PolicyEvaluationException e) {
            Assert.assertTrue(e.getCause() instanceof PolicyManagementException);
        }
    }

    @Test
    public void testRuleEvaluationExceptionIsWrapped() throws PolicyManagementException, RuleEvaluationException {

        Policy policy = policyWithRule("ios");
        when(policyManagementService.getPolicyById(POLICY_ID, TENANT_DOMAIN)).thenReturn(policy);
        when(ruleEvaluationService.evaluate(eq(RULE_ID), any(FlowContext.class), eq(TENANT_DOMAIN)))
                .thenThrow(new RuleEvaluationException("boom"));

        try {
            policyEvaluationService.evaluate(POLICY_ID, "ios", context, TENANT_DOMAIN);
            Assert.fail("Expected PolicyEvaluationException");
        } catch (PolicyEvaluationException e) {
            Assert.assertTrue(e.getCause() instanceof RuleEvaluationException);
        }
    }

    @Test(expectedExceptions = PolicyEvaluationException.class)
    public void testUnsupportedFlowTypeThrows() throws PolicyManagementException, PolicyEvaluationException {

        Policy policy = policyWithRule("ios");
        when(policyManagementService.getPolicyById(POLICY_ID, TENANT_DOMAIN)).thenReturn(policy);

        PolicyEvaluationContext unknownFlow = PolicyEvaluationContext.create("NOT_A_REAL_FLOW");

        policyEvaluationService.evaluate(POLICY_ID, "ios", unknownFlow, TENANT_DOMAIN);
    }

    // --- Diagnostic logging tests ---

    @Test
    public void testDiagnosticLogsEnabled_SingleRuleSatisfiedLogsInitiationPerResourceAndCompletion()
            throws PolicyManagementException, RuleEvaluationException, PolicyEvaluationException {

        loggerUtils.when(LoggerUtils::isDiagnosticLogsEnabled).thenReturn(true);

        Policy policy = policyWithRule("ios");
        when(policyManagementService.getPolicyById(POLICY_ID, TENANT_DOMAIN)).thenReturn(policy);
        when(ruleEvaluationService.evaluate(eq(RULE_ID), any(FlowContext.class), eq(TENANT_DOMAIN)))
                .thenReturn(new RuleEvaluationResult(RULE_ID, true));

        policyEvaluationService.evaluate(POLICY_ID, "ios", context, TENANT_DOMAIN);

        // One log each for: evaluation initiated, the single resource evaluated, evaluation completed.
        loggerUtils.verify(() -> LoggerUtils.triggerDiagnosticLogEvent(
                any(DiagnosticLog.DiagnosticLogBuilder.class)), times(3));
    }

    @Test
    public void testDiagnosticLogsEnabled_MultipleResourcesLogsPerResourceForEach() throws PolicyManagementException,
            RuleEvaluationException, PolicyEvaluationException {

        loggerUtils.when(LoggerUtils::isDiagnosticLogsEnabled).thenReturn(true);

        String secondRuleId = UUID.randomUUID().toString();
        Rule rule1 = mock(Rule.class);
        when(rule1.getId()).thenReturn(RULE_ID);
        Rule rule2 = mock(Rule.class);
        when(rule2.getId()).thenReturn(secondRuleId);
        PolicyResource ruleResource1 = new RulePolicyResource(UUID.randomUUID().toString(), "ios", RULE_ID, rule1);
        PolicyResource ruleResource2 = new RulePolicyResource(
                UUID.randomUUID().toString(), "ios", secondRuleId, rule2);
        Policy policy = new Policy(UUID.randomUUID().toString(), POLICY_NAME, TENANT_DOMAIN,
                Arrays.asList(ruleResource1, ruleResource2));
        when(policyManagementService.getPolicyById(POLICY_ID, TENANT_DOMAIN)).thenReturn(policy);
        when(ruleEvaluationService.evaluate(eq(RULE_ID), any(FlowContext.class), eq(TENANT_DOMAIN)))
                .thenReturn(new RuleEvaluationResult(RULE_ID, true));
        when(ruleEvaluationService.evaluate(eq(secondRuleId), any(FlowContext.class), eq(TENANT_DOMAIN)))
                .thenReturn(new RuleEvaluationResult(secondRuleId, true));

        policyEvaluationService.evaluate(POLICY_ID, "ios", context, TENANT_DOMAIN);

        // One log each for: evaluation initiated, the two resources evaluated, evaluation completed.
        loggerUtils.verify(() -> LoggerUtils.triggerDiagnosticLogEvent(
                any(DiagnosticLog.DiagnosticLogBuilder.class)), times(4));
    }

    @Test
    public void testDiagnosticLogsEnabled_PolicyNotFoundLogsInitiationAndNotFound()
            throws PolicyManagementException, PolicyEvaluationException {

        loggerUtils.when(LoggerUtils::isDiagnosticLogsEnabled).thenReturn(true);

        when(policyManagementService.getPolicyById(POLICY_ID, TENANT_DOMAIN)).thenReturn(null);

        try {
            policyEvaluationService.evaluate(POLICY_ID, "ios", context, TENANT_DOMAIN);
            Assert.fail("Expected PolicyEvaluationException");
        } catch (PolicyEvaluationException expected) {
            // Expected.
        }

        // One log each for: evaluation initiated, policy not found.
        loggerUtils.verify(() -> LoggerUtils.triggerDiagnosticLogEvent(
                any(DiagnosticLog.DiagnosticLogBuilder.class)), times(2));
    }

    @Test
    public void testDiagnosticLogsDisabled_NoLogEventsFired() throws PolicyManagementException,
            RuleEvaluationException, PolicyEvaluationException {

        Policy policy = policyWithRule("ios");
        when(policyManagementService.getPolicyById(POLICY_ID, TENANT_DOMAIN)).thenReturn(policy);
        when(ruleEvaluationService.evaluate(eq(RULE_ID), any(FlowContext.class), eq(TENANT_DOMAIN)))
                .thenReturn(new RuleEvaluationResult(RULE_ID, true));

        policyEvaluationService.evaluate(POLICY_ID, "ios", context, TENANT_DOMAIN);

        loggerUtils.verify(() -> LoggerUtils.triggerDiagnosticLogEvent(
                any(DiagnosticLog.DiagnosticLogBuilder.class)), never());
    }
}
