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

package org.wso2.carbon.identity.device.policy.internal.service.impl;

import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.device.policy.api.exception.DevicePolicyClientException;
import org.wso2.carbon.identity.device.policy.api.model.DevicePolicyEvaluationResult;
import org.wso2.carbon.identity.device.policy.internal.component.DevicePolicyComponentServiceHolder;
import org.wso2.carbon.identity.device.policy.internal.service.IntegrityDataEnricher;
import org.wso2.carbon.identity.policy.evaluation.api.model.PolicyEvaluationContext;
import org.wso2.carbon.identity.policy.evaluation.api.model.PolicyEvaluationResult;
import org.wso2.carbon.identity.policy.evaluation.api.model.RuleResourceEvaluationResult;
import org.wso2.carbon.identity.policy.evaluation.api.service.PolicyEvaluationService;
import org.wso2.carbon.identity.policy.management.api.model.Policy;
import org.wso2.carbon.identity.policy.management.api.model.PolicyResource;
import org.wso2.carbon.identity.policy.management.api.model.PolicyResource.ResourceType;
import org.wso2.carbon.identity.policy.management.api.model.RulePolicyResource;
import org.wso2.carbon.identity.policy.management.api.service.PolicyManagementService;
import org.wso2.carbon.identity.rule.management.api.model.Expression;
import org.wso2.carbon.identity.rule.management.api.model.Rule;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class DevicePolicyEvaluatorImplTest {

    private DevicePolicyEvaluatorImpl devicePolicyEvaluator;
    private MockedStatic<DevicePolicyComponentServiceHolder> mockedServiceHolder;
    private DevicePolicyComponentServiceHolder serviceHolder;
    private IntegrityDataEnricher integrityDataEnricher;
    private PolicyManagementService policyManagementService;
    private PolicyEvaluationService policyEvaluationService;

    @BeforeMethod
    public void setUp() throws Exception {
        devicePolicyEvaluator = new DevicePolicyEvaluatorImpl();

        serviceHolder = mock(DevicePolicyComponentServiceHolder.class);
        integrityDataEnricher = mock(IntegrityDataEnricher.class);
        policyManagementService = mock(PolicyManagementService.class);
        policyEvaluationService = mock(PolicyEvaluationService.class);

        mockedServiceHolder = Mockito.mockStatic(DevicePolicyComponentServiceHolder.class);
        mockedServiceHolder.when(DevicePolicyComponentServiceHolder::getInstance).thenReturn(serviceHolder);

        when(serviceHolder.getIntegrityDataEnricher()).thenReturn(integrityDataEnricher);
        when(serviceHolder.getPolicyManagementService()).thenReturn(policyManagementService);
        when(serviceHolder.getPolicyEvaluationService()).thenReturn(policyEvaluationService);

        doNothing().when(integrityDataEnricher).enrich(any(), anyString(), anyString());
    }

    @AfterMethod
    public void tearDown() {
        mockedServiceHolder.close();
    }

    @Test
    public void testEvaluateWithMissingPlatform() throws Exception {
        Map<String, Object> deviceData = new HashMap<>();

        Policy policy = mock(Policy.class);
        when(policyManagementService.getPolicyByName(anyString(), anyString())).thenReturn(policy);

        DevicePolicyEvaluationResult result =
                devicePolicyEvaluator.evaluate("testPolicy", deviceData, "appId", "carbon.super");

        Assert.assertEquals(result.getStatus(), DevicePolicyEvaluationResult.Status.INCOMPLETE_DEVICE_DATA);
        Assert.assertEquals(result.getMissingFields(), Collections.singletonList("platform"));
        Assert.assertFalse(result.isCompliant());
    }

    @Test
    public void testEvaluateWithMissingFields() throws Exception {
        Map<String, Object> deviceData = new HashMap<>();
        deviceData.put("platform", "android");

        Policy policy = mock(Policy.class);
        RulePolicyResource ruleResource = mock(RulePolicyResource.class);
        when(ruleResource.getResourceType()).thenReturn(ResourceType.RULE);
        when(ruleResource.getTarget()).thenReturn("android");

        Rule rule = mock(Rule.class);
        Expression expression = mock(Expression.class);
        when(expression.getField()).thenReturn("androidIntegrity");
        when(rule.getExpressions()).thenReturn(Collections.singletonList(expression));
        when(ruleResource.getRule()).thenReturn(rule);

        List<PolicyResource> resources = new ArrayList<>();
        resources.add(ruleResource);
        when(policy.getResources()).thenReturn(resources);

        when(policyManagementService.getPolicyByName("testPolicy", "carbon.super")).thenReturn(policy);

        DevicePolicyEvaluationResult result =
                devicePolicyEvaluator.evaluate("testPolicy", deviceData, "appId", "carbon.super");

        Assert.assertEquals(result.getStatus(), DevicePolicyEvaluationResult.Status.INCOMPLETE_DEVICE_DATA);
        Assert.assertEquals(result.getMissingFields(), Collections.singletonList("androidIntegrity"));
        Assert.assertTrue(result.getFailedFields().isEmpty());
    }

    @Test(expectedExceptions = DevicePolicyClientException.class)
    public void testEvaluatePolicyNotFoundThrows() throws Exception {
        Map<String, Object> deviceData = new HashMap<>();
        deviceData.put("platform", "android");

        when(policyManagementService.getPolicyByName(anyString(), anyString())).thenReturn(null);

        devicePolicyEvaluator.evaluate("testPolicy", deviceData, "appId", "carbon.super");
    }

    @Test
    public void testEvaluateResultSatisfied() throws Exception {
        Map<String, Object> deviceData = new HashMap<>();
        deviceData.put("platform", "android");

        Policy policy = mock(Policy.class);
        when(policy.getId()).thenReturn("policyId123");
        when(policy.getResources()).thenReturn(Collections.emptyList());
        when(policyManagementService.getPolicyByName(anyString(), anyString())).thenReturn(policy);

        PolicyEvaluationResult evalResult = mock(PolicyEvaluationResult.class);
        when(evalResult.isSatisfied()).thenReturn(true);
        when(policyEvaluationService.evaluate(anyString(), anyString(),
                any(PolicyEvaluationContext.class), anyString())).thenReturn(evalResult);

        DevicePolicyEvaluationResult result =
                devicePolicyEvaluator.evaluate("testPolicy", deviceData, "appId", "carbon.super");

        Assert.assertEquals(result.getStatus(), DevicePolicyEvaluationResult.Status.COMPLIANT);
        Assert.assertTrue(result.isCompliant());
    }

    @Test
    public void testEvaluateResultNotSatisfied() throws Exception {
        Map<String, Object> deviceData = new HashMap<>();
        deviceData.put("platform", "android");

        Policy policy = mock(Policy.class);
        when(policy.getId()).thenReturn("policyId123");
        when(policy.getResources()).thenReturn(Collections.emptyList());
        when(policyManagementService.getPolicyByName(anyString(), anyString())).thenReturn(policy);

        PolicyEvaluationResult evalResult = mock(PolicyEvaluationResult.class);
        when(evalResult.isSatisfied()).thenReturn(false);

        RuleResourceEvaluationResult ruleResult = mock(RuleResourceEvaluationResult.class);
        when(ruleResult.isSatisfied()).thenReturn(false);
        when(ruleResult.getFailedFields()).thenReturn(Arrays.asList("field1", "field2"));

        when(evalResult.getResults()).thenReturn(Collections.singletonList(ruleResult));

        when(policyEvaluationService.evaluate(anyString(), anyString(),
                any(PolicyEvaluationContext.class), anyString())).thenReturn(evalResult);

        DevicePolicyEvaluationResult result =
                devicePolicyEvaluator.evaluate("testPolicy", deviceData, "appId", "carbon.super");

        Assert.assertEquals(result.getStatus(), DevicePolicyEvaluationResult.Status.NON_COMPLIANT);
        Assert.assertEquals(result.getFailedFields(), Arrays.asList("field1", "field2"));
        Assert.assertTrue(result.getMissingFields().isEmpty());
    }

    @Test
    public void testDistinguishIncompleteDeviceDataFromNonCompliant() throws Exception {
        // Scenario 1: missing field 'isRooted' -> INCOMPLETE_DEVICE_DATA
        Map<String, Object> deviceDataMissing = new HashMap<>();
        deviceDataMissing.put("platform", "android");

        Policy policy = mock(Policy.class);
        RulePolicyResource ruleResource = mock(RulePolicyResource.class);
        when(ruleResource.getResourceType()).thenReturn(ResourceType.RULE);
        when(ruleResource.getTarget()).thenReturn("android");

        Rule rule = mock(Rule.class);
        Expression expression = mock(Expression.class);
        when(expression.getField()).thenReturn("isRooted");
        when(rule.getExpressions()).thenReturn(Collections.singletonList(expression));
        when(ruleResource.getRule()).thenReturn(rule);

        List<PolicyResource> resources = new ArrayList<>();
        resources.add(ruleResource);
        when(policy.getResources()).thenReturn(resources);
        when(policyManagementService.getPolicyByName("testPolicy", "carbon.super")).thenReturn(policy);

        DevicePolicyEvaluationResult resultIncomplete =
                devicePolicyEvaluator.evaluate("testPolicy", deviceDataMissing, "appId", "carbon.super");
        Assert.assertEquals(resultIncomplete.getStatus(), DevicePolicyEvaluationResult.Status.INCOMPLETE_DEVICE_DATA);
        Assert.assertEquals(resultIncomplete.getMissingFields(), Collections.singletonList("isRooted"));

        // Scenario 2: field present but failed evaluation -> NON_COMPLIANT
        Map<String, Object> deviceDataPresent = new HashMap<>();
        deviceDataPresent.put("platform", "android");
        deviceDataPresent.put("isRooted", "true");

        when(policy.getId()).thenReturn("policyId123");
        PolicyEvaluationResult evalResult = mock(PolicyEvaluationResult.class);
        when(evalResult.isSatisfied()).thenReturn(false);

        RuleResourceEvaluationResult ruleResult = mock(RuleResourceEvaluationResult.class);
        when(ruleResult.isSatisfied()).thenReturn(false);
        when(ruleResult.getFailedFields()).thenReturn(Collections.singletonList("isRooted"));
        when(evalResult.getResults()).thenReturn(Collections.singletonList(ruleResult));
        when(policyEvaluationService.evaluate(anyString(), anyString(),
                any(PolicyEvaluationContext.class), anyString())).thenReturn(evalResult);

        DevicePolicyEvaluationResult resultNonCompliant =
                devicePolicyEvaluator.evaluate("testPolicy", deviceDataPresent, "appId", "carbon.super");
        Assert.assertEquals(resultNonCompliant.getStatus(), DevicePolicyEvaluationResult.Status.NON_COMPLIANT);
        Assert.assertEquals(resultNonCompliant.getFailedFields(), Collections.singletonList("isRooted"));
    }
}
