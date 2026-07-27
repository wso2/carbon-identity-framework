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

import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.policy.evaluation.api.exception.PolicyEvaluationException;
import org.wso2.carbon.identity.policy.evaluation.api.model.PolicyEvaluationContext;
import org.wso2.carbon.identity.policy.evaluation.api.model.ResourceEvaluationResult;
import org.wso2.carbon.identity.policy.evaluation.internal.component.PolicyEvaluationComponentServiceHolder;
import org.wso2.carbon.identity.policy.evaluation.internal.evaluator.RuleResourceEvaluator;
import org.wso2.carbon.identity.policy.management.api.exception.PolicyManagementClientException;
import org.wso2.carbon.identity.policy.management.api.model.PolicyResource;
import org.wso2.carbon.identity.policy.management.api.model.RulePolicyResource;
import org.wso2.carbon.identity.rule.evaluation.api.exception.RuleEvaluationException;
import org.wso2.carbon.identity.rule.evaluation.api.model.FlowContext;
import org.wso2.carbon.identity.rule.evaluation.api.model.RuleEvaluationResult;
import org.wso2.carbon.identity.rule.evaluation.api.service.RuleEvaluationService;
import org.wso2.carbon.identity.rule.management.api.model.Rule;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Unit test class for RuleResourceEvaluator.
 */
public class RuleResourceEvaluatorTest {

    private static final String TENANT_DOMAIN = "carbon.super";
    private static final String RULE_ID = "rule-1";

    private RuleResourceEvaluator evaluator;

    @Mock
    private RuleEvaluationService ruleEvaluationService;

    @BeforeMethod
    public void setUp() {

        MockitoAnnotations.openMocks(this);
        evaluator = new RuleResourceEvaluator();
        PolicyEvaluationComponentServiceHolder.getInstance().setRuleEvaluationService(ruleEvaluationService);
    }

    @Test
    public void testGetSupportedResourceType() {

        Assert.assertEquals(evaluator.getSupportedResourceType(), PolicyResource.ResourceType.RULE);
    }

    @Test(expectedExceptions = PolicyEvaluationException.class)
    public void testEvaluate_UnsupportedResourceType_ThrowsException() throws PolicyEvaluationException {

        PolicyEvaluationContext context = new PolicyEvaluationContext("PRE_ISSUE_ACCESS_TOKEN");
        evaluator.evaluate(null, context, TENANT_DOMAIN);
    }

    @Test
    public void testEvaluate_NullRuleInResource_ReturnsSatisfied()
            throws PolicyManagementClientException, PolicyEvaluationException {

        PolicyResource resource = new RulePolicyResource.Builder()
                .target("ios")
                .resourceId("res-1")
                .rule(null)
                .build();
        PolicyEvaluationContext context = new PolicyEvaluationContext("PRE_ISSUE_ACCESS_TOKEN");

        ResourceEvaluationResult result = evaluator.evaluate(resource, context, TENANT_DOMAIN);
        Assert.assertTrue(result.isSatisfied());
    }

    @Test
    public void testEvaluate_RuleSatisfied_ReturnsSatisfiedResult()
            throws PolicyManagementClientException, RuleEvaluationException, PolicyEvaluationException {

        Rule rule = mock(Rule.class);
        when(rule.getId()).thenReturn(RULE_ID);
        PolicyResource resource = new RulePolicyResource.Builder()
                .target("ios")
                .resourceId("res-1")
                .rule(rule)
                .build();
        PolicyEvaluationContext context = new PolicyEvaluationContext("PRE_ISSUE_ACCESS_TOKEN");

        when(ruleEvaluationService.evaluate(eq(RULE_ID), any(FlowContext.class), eq(TENANT_DOMAIN)))
                .thenReturn(new RuleEvaluationResult(RULE_ID, true));

        ResourceEvaluationResult result = evaluator.evaluate(resource, context, TENANT_DOMAIN);
        Assert.assertTrue(result.isSatisfied());
    }

    @Test
    public void testEvaluate_RuleUnsatisfied_ReturnsUnsatisfiedResult()
            throws PolicyManagementClientException, RuleEvaluationException, PolicyEvaluationException {

        Rule rule = mock(Rule.class);
        when(rule.getId()).thenReturn(RULE_ID);
        PolicyResource resource = new RulePolicyResource.Builder()
                .target("ios")
                .resourceId("res-1")
                .rule(rule)
                .build();
        PolicyEvaluationContext context = new PolicyEvaluationContext("PRE_ISSUE_ACCESS_TOKEN");

        when(ruleEvaluationService.evaluate(eq(RULE_ID), any(FlowContext.class), eq(TENANT_DOMAIN)))
                .thenReturn(new RuleEvaluationResult(RULE_ID, false, List.of("device.os")));

        ResourceEvaluationResult result = evaluator.evaluate(resource, context, TENANT_DOMAIN);
        Assert.assertFalse(result.isSatisfied());
    }

    @Test(expectedExceptions = PolicyEvaluationException.class)
    public void testEvaluate_RuleEvaluationThrowsException_ThrowsPolicyEvaluationException()
            throws PolicyManagementClientException, RuleEvaluationException, PolicyEvaluationException {

        Rule rule = mock(Rule.class);
        when(rule.getId()).thenReturn(RULE_ID);
        PolicyResource resource = new RulePolicyResource.Builder()
                .target("ios")
                .resourceId("res-1")
                .rule(rule)
                .build();
        PolicyEvaluationContext context = new PolicyEvaluationContext("PRE_ISSUE_ACCESS_TOKEN");

        when(ruleEvaluationService.evaluate(eq(RULE_ID), any(FlowContext.class), eq(TENANT_DOMAIN)))
                .thenThrow(new RuleEvaluationException("Engine error"));

        evaluator.evaluate(resource, context, TENANT_DOMAIN);
    }

    @Test(expectedExceptions = PolicyEvaluationException.class)
    public void testEvaluate_UnsupportedFlowType_ThrowsPolicyEvaluationException()
            throws PolicyManagementClientException, PolicyEvaluationException {

        Rule rule = mock(Rule.class);
        when(rule.getId()).thenReturn(RULE_ID);
        PolicyResource resource = new RulePolicyResource.Builder()
                .target("ios")
                .resourceId("res-1")
                .rule(rule)
                .build();
        PolicyEvaluationContext context = new PolicyEvaluationContext("INVALID_FLOW_TYPE");

        evaluator.evaluate(resource, context, TENANT_DOMAIN);
    }
}
