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

import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.device.policy.api.constant.DevicePolicyErrorMessage;
import org.wso2.carbon.identity.device.policy.api.exception.DevicePolicyClientException;
import org.wso2.carbon.identity.device.policy.api.exception.DevicePolicyServerException;
import org.wso2.carbon.identity.device.policy.api.model.DevicePolicyEvaluationResult;
import org.wso2.carbon.identity.device.policy.internal.component.DevicePolicyComponentServiceHolder;
import org.wso2.carbon.identity.device.policy.internal.service.IntegrityDataEnricher;
import org.wso2.carbon.identity.device.policy.internal.util.DeviceTokenExtractor;
import org.wso2.carbon.identity.organization.management.service.OrganizationManager;
import org.wso2.carbon.identity.organization.management.service.exception.OrganizationManagementServerException;
import org.wso2.carbon.identity.organization.management.service.util.OrganizationManagementUtil;
import org.wso2.carbon.identity.policy.evaluation.api.model.PolicyEvaluationContext;
import org.wso2.carbon.identity.policy.evaluation.api.model.PolicyEvaluationResult;
import org.wso2.carbon.identity.policy.evaluation.api.model.RuleResourceEvaluationResult;
import org.wso2.carbon.identity.policy.evaluation.api.service.PolicyEvaluationService;
import org.wso2.carbon.identity.policy.management.api.model.Policy;
import org.wso2.carbon.identity.policy.management.api.service.PolicyManagementService;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class DevicePolicyEvaluatorImplTest {

    private DevicePolicyEvaluatorImpl devicePolicyEvaluator;
    private MockedStatic<DevicePolicyComponentServiceHolder> mockedServiceHolder;
    private DevicePolicyComponentServiceHolder serviceHolder;
    private IntegrityDataEnricher integrityDataEnricher;
    private PolicyManagementService policyManagementService;
    private PolicyEvaluationService policyEvaluationService;
    private OrganizationManager organizationManager;
    private MockedStatic<OrganizationManagementUtil> mockedOrganizationManagementUtil;

    private static final String ROOT_TENANT = "carbon.super";
    private static final String ROOT_ORG_ID = "10084a8d-113f-4211-a0d5-efe36b082211";
    private static final String PARENT_ORG_TENANT = "parent-org-tenant";
    private static final String PARENT_ORG_ID = "6bd5a0c6-parent";
    private static final String SUB_ORG_TENANT = "sub-org-tenant";
    private static final String SUB_ORG_ID = "f1c2d3e4-sub";

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

        organizationManager = mock(OrganizationManager.class);
        when(serviceHolder.getOrganizationManager()).thenReturn(organizationManager);
        mockedOrganizationManagementUtil = Mockito.mockStatic(OrganizationManagementUtil.class);
        mockedOrganizationManagementUtil.when(() -> OrganizationManagementUtil.isOrganization(SUB_ORG_TENANT))
                .thenReturn(true);
        mockedOrganizationManagementUtil.when(() -> OrganizationManagementUtil.isOrganization(PARENT_ORG_TENANT))
                .thenReturn(true);
        when(organizationManager.resolveOrganizationId(SUB_ORG_TENANT)).thenReturn(SUB_ORG_ID);
        when(organizationManager.resolveOrganizationId(PARENT_ORG_TENANT)).thenReturn(PARENT_ORG_ID);
        when(organizationManager.getPrimaryOrganizationId(SUB_ORG_ID)).thenReturn(ROOT_ORG_ID);
        when(organizationManager.getPrimaryOrganizationId(PARENT_ORG_ID)).thenReturn(ROOT_ORG_ID);
        when(organizationManager.resolveTenantDomain(SUB_ORG_ID)).thenReturn(SUB_ORG_TENANT);
        when(organizationManager.resolveTenantDomain(PARENT_ORG_ID)).thenReturn(PARENT_ORG_TENANT);
        when(organizationManager.resolveTenantDomain(ROOT_ORG_ID)).thenReturn(ROOT_TENANT);
    }

    @AfterMethod
    public void tearDown() {
        mockedServiceHolder.close();
        mockedOrganizationManagementUtil.close();
    }

    @Test
    public void testEvaluateWithMissingPlatform() throws Exception {
        Map<String, Object> deviceData = new HashMap<>();

        Policy policy = mock(Policy.class);
        when(policyManagementService.getPolicyById(anyString(), anyString())).thenReturn(policy);

        DevicePolicyEvaluationResult result =
                devicePolicyEvaluator.evaluate("testPolicyId", deviceData, "appId", "carbon.super");

        Assert.assertEquals(result.getStatus(), DevicePolicyEvaluationResult.Status.INCOMPLETE_DEVICE_DATA);
        Assert.assertEquals(result.getMissingFields(), Collections.singletonList("platform"));
        Assert.assertFalse(result.isCompliant());
    }

    @Test
    public void testEvaluateReportsFailedFieldsTheDeviceNeverSentAsMissing() throws Exception {
        Map<String, Object> deviceData = new HashMap<>();
        deviceData.put("platform", "android");

        Policy policy = mock(Policy.class);
        when(policyManagementService.getPolicyById("testPolicyId", "carbon.super")).thenReturn(policy);

        PolicyEvaluationResult evalResult = mock(PolicyEvaluationResult.class);
        when(evalResult.isSatisfied()).thenReturn(false);
        RuleResourceEvaluationResult ruleResult = mock(RuleResourceEvaluationResult.class);
        when(ruleResult.isSatisfied()).thenReturn(false);
        when(ruleResult.getFailedFields()).thenReturn(Collections.singletonList("androidIntegrity"));
        when(evalResult.getResults()).thenReturn(Collections.singletonList(ruleResult));
        when(policyEvaluationService.evaluate(anyString(), anyString(),
                any(PolicyEvaluationContext.class), anyString())).thenReturn(evalResult);

        DevicePolicyEvaluationResult result =
                devicePolicyEvaluator.evaluate("testPolicyId", deviceData, "appId", "carbon.super");

        Assert.assertEquals(result.getStatus(), DevicePolicyEvaluationResult.Status.INCOMPLETE_DEVICE_DATA);
        Assert.assertEquals(result.getMissingFields(), Collections.singletonList("androidIntegrity"));
        Assert.assertTrue(result.getFailedFields().isEmpty());
    }

    @Test
    public void testEvaluateIsCompliantWhenPartialDataStillSatisfiesTheRule() throws Exception {
        // A rule combined with OR can be satisfied by one branch alone, so absent data for the other
        // branch must not short circuit the evaluation.
        Map<String, Object> deviceData = new HashMap<>();
        deviceData.put("platform", "android");
        deviceData.put("biometric", "true");

        Policy policy = mock(Policy.class);
        when(policyManagementService.getPolicyById("testPolicyId", "carbon.super")).thenReturn(policy);

        PolicyEvaluationResult evalResult = mock(PolicyEvaluationResult.class);
        when(evalResult.isSatisfied()).thenReturn(true);
        when(policyEvaluationService.evaluate(anyString(), anyString(),
                any(PolicyEvaluationContext.class), anyString())).thenReturn(evalResult);

        DevicePolicyEvaluationResult result =
                devicePolicyEvaluator.evaluate("testPolicyId", deviceData, "appId", "carbon.super");

        Assert.assertTrue(result.isCompliant());
        Assert.assertTrue(result.getMissingFields().isEmpty());
    }

    @Test(expectedExceptions = DevicePolicyClientException.class)
    public void testEvaluatePolicyNotFoundThrows() throws Exception {
        Map<String, Object> deviceData = new HashMap<>();
        deviceData.put("platform", "android");

        when(policyManagementService.getPolicyById(anyString(), anyString())).thenReturn(null);

        devicePolicyEvaluator.evaluate("testPolicyId", deviceData, "appId", "carbon.super");
    }

    @Test
    public void testEvaluateResultSatisfied() throws Exception {
        Map<String, Object> deviceData = new HashMap<>();
        deviceData.put("platform", "android");

        Policy policy = mock(Policy.class);
        when(policy.getId()).thenReturn("policyId123");
        when(policy.getResources()).thenReturn(Collections.emptyList());
        when(policyManagementService.getPolicyById(anyString(), anyString())).thenReturn(policy);

        PolicyEvaluationResult evalResult = mock(PolicyEvaluationResult.class);
        when(evalResult.isSatisfied()).thenReturn(true);
        when(policyEvaluationService.evaluate(anyString(), anyString(),
                any(PolicyEvaluationContext.class), anyString())).thenReturn(evalResult);

        DevicePolicyEvaluationResult result =
                devicePolicyEvaluator.evaluate("testPolicyId", deviceData, "appId", "carbon.super");

        Assert.assertEquals(result.getStatus(), DevicePolicyEvaluationResult.Status.COMPLIANT);
        Assert.assertTrue(result.isCompliant());
    }

    @Test
    public void testEvaluateResultNotSatisfied() throws Exception {
        Map<String, Object> deviceData = new HashMap<>();
        deviceData.put("platform", "android");
        deviceData.put("field1", "value1");
        deviceData.put("field2", "value2");

        Policy policy = mock(Policy.class);
        when(policy.getId()).thenReturn("policyId123");
        when(policy.getResources()).thenReturn(Collections.emptyList());
        when(policyManagementService.getPolicyById(anyString(), anyString())).thenReturn(policy);

        PolicyEvaluationResult evalResult = mock(PolicyEvaluationResult.class);
        when(evalResult.isSatisfied()).thenReturn(false);

        RuleResourceEvaluationResult ruleResult = mock(RuleResourceEvaluationResult.class);
        when(ruleResult.isSatisfied()).thenReturn(false);
        when(ruleResult.getFailedFields()).thenReturn(Arrays.asList("field1", "field2"));

        when(evalResult.getResults()).thenReturn(Collections.singletonList(ruleResult));

        when(policyEvaluationService.evaluate(anyString(), anyString(),
                any(PolicyEvaluationContext.class), anyString())).thenReturn(evalResult);

        DevicePolicyEvaluationResult result =
                devicePolicyEvaluator.evaluate("testPolicyId", deviceData, "appId", "carbon.super");

        Assert.assertEquals(result.getStatus(), DevicePolicyEvaluationResult.Status.NON_COMPLIANT);
        Assert.assertEquals(result.getFailedFields(), Arrays.asList("field1", "field2"));
        Assert.assertTrue(result.getMissingFields().isEmpty());
    }

    @Test
    public void testDistinguishIncompleteDeviceDataFromNonCompliant() throws Exception {
        Policy policy = mock(Policy.class);
        when(policyManagementService.getPolicyById("testPolicyId", "carbon.super")).thenReturn(policy);

        PolicyEvaluationResult evalResult = mock(PolicyEvaluationResult.class);
        when(evalResult.isSatisfied()).thenReturn(false);
        RuleResourceEvaluationResult ruleResult = mock(RuleResourceEvaluationResult.class);
        when(ruleResult.isSatisfied()).thenReturn(false);
        when(ruleResult.getFailedFields()).thenReturn(Collections.singletonList("isRooted"));
        when(evalResult.getResults()).thenReturn(Collections.singletonList(ruleResult));
        when(policyEvaluationService.evaluate(anyString(), anyString(),
                any(PolicyEvaluationContext.class), anyString())).thenReturn(evalResult);

        // Scenario 1: the device never sent 'isRooted' -> INCOMPLETE_DEVICE_DATA.
        Map<String, Object> deviceDataMissing = new HashMap<>();
        deviceDataMissing.put("platform", "android");

        DevicePolicyEvaluationResult resultIncomplete =
                devicePolicyEvaluator.evaluate("testPolicyId", deviceDataMissing, "appId", "carbon.super");

        Assert.assertEquals(resultIncomplete.getStatus(), DevicePolicyEvaluationResult.Status.INCOMPLETE_DEVICE_DATA);
        Assert.assertEquals(resultIncomplete.getMissingFields(), Collections.singletonList("isRooted"));

        // Scenario 2: 'isRooted' was sent but failed the rule -> NON_COMPLIANT.
        Map<String, Object> deviceDataPresent = new HashMap<>();
        deviceDataPresent.put("platform", "android");
        deviceDataPresent.put("isRooted", "true");

        DevicePolicyEvaluationResult resultNonCompliant =
                devicePolicyEvaluator.evaluate("testPolicyId", deviceDataPresent, "appId", "carbon.super");

        Assert.assertEquals(resultNonCompliant.getStatus(), DevicePolicyEvaluationResult.Status.NON_COMPLIANT);
        Assert.assertEquals(resultNonCompliant.getFailedFields(), Collections.singletonList("isRooted"));
    }

    @Test
    public void testEvaluateByPolicyNameResolvesTheIdAndEvaluates() throws Exception {
        Map<String, Object> deviceData = new HashMap<>();
        deviceData.put("platform", "android");

        Policy policy = mock(Policy.class);
        when(policyManagementService.getPolicyIdByName("DEVICE", "testPolicy", "carbon.super"))
                .thenReturn("testPolicyId");
        when(policyManagementService.getPolicyById("testPolicyId", "carbon.super")).thenReturn(policy);

        PolicyEvaluationResult evaluationResult = mock(PolicyEvaluationResult.class);
        when(evaluationResult.isSatisfied()).thenReturn(true);
        when(policyEvaluationService.evaluate(any(), anyString(), any(), anyString())).thenReturn(evaluationResult);

        DevicePolicyEvaluationResult result =
                devicePolicyEvaluator.evaluateByPolicyName("testPolicy", deviceData, "appId", "carbon.super");

        Assert.assertTrue(result.isCompliant());
        // The result identifies the policy by ID, whichever entry point was used.
        Assert.assertEquals(result.getPolicyId(), "testPolicyId");
        verify(policyEvaluationService).evaluate(eq("testPolicyId"), eq("android"),
                any(PolicyEvaluationContext.class), eq("carbon.super"));
        verify(organizationManager, never()).getPrimaryOrganizationId(any());
    }

    @Test(expectedExceptions = DevicePolicyClientException.class)
    public void testEvaluateByPolicyNameThrowsWhenTheNameDoesNotResolve() throws Exception {
        Map<String, Object> deviceData = new HashMap<>();
        deviceData.put("platform", "android");

        when(policyManagementService.getPolicyIdByName(anyString(), anyString(), anyString())).thenReturn(null);

        devicePolicyEvaluator.evaluateByPolicyName("testPolicy", deviceData, "appId", "carbon.super");
    }

    @Test
    public void testEvaluateFromTokenVerifiesTheTokenAndEvaluates() throws Exception {
        Map<String, Object> deviceData = new HashMap<>();
        deviceData.put("platform", "android");

        Policy policy = mock(Policy.class);
        when(policyManagementService.getPolicyById("testPolicyId", "carbon.super")).thenReturn(policy);

        PolicyEvaluationResult evaluationResult = mock(PolicyEvaluationResult.class);
        when(evaluationResult.isSatisfied()).thenReturn(true);
        when(policyEvaluationService.evaluate(any(), anyString(), any(), anyString())).thenReturn(evaluationResult);

        try (MockedConstruction<DeviceTokenExtractor> mockedExtractor = Mockito.mockConstruction(
                DeviceTokenExtractor.class,
                (mock, ctx) -> when(mock.extractWithPublicKey(anyString(), anyString(), anyString(), anyString()))
                        .thenReturn(deviceData))) {

            DevicePolicyEvaluationResult result = devicePolicyEvaluator.evaluateFromToken("testPolicyId",
                    "token", "publicKey", "correlationId", "appId", "carbon.super");

            Assert.assertTrue(result.isCompliant());
            Assert.assertEquals(result.getPolicyId(), "testPolicyId");
            verify(mockedExtractor.constructed().get(0))
                    .extractWithPublicKey("token", "publicKey", "correlationId", "carbon.super");
        }
    }

    @Test(expectedExceptions = DevicePolicyClientException.class)
    public void testEvaluateFromTokenByPolicyNameThrowsWhenTheNameDoesNotResolve() throws Exception {

        when(policyManagementService.getPolicyIdByName(anyString(), anyString(), anyString())).thenReturn(null);

        devicePolicyEvaluator.evaluateFromTokenByPolicyName("testPolicy", "token", "publicKey",
                "correlationId", "appId", "carbon.super");
    }

    @DataProvider(name = "organizationTenants")
    public Object[][] organizationTenants() {

        return new Object[][]{{SUB_ORG_TENANT}, {PARENT_ORG_TENANT}};
    }

    @Test(dataProvider = "organizationTenants")
    public void testEvaluateByPolicyNameInOrganizationUsesTheRootPolicy(String orgTenant) throws Exception {
        Map<String, Object> deviceData = new HashMap<>();
        deviceData.put("platform", "android");

        when(policyManagementService.getPolicyIdByName("DEVICE", "testPolicy", ROOT_TENANT))
                .thenReturn("rootPolicyId");
        mockSatisfiedEvaluation();

        DevicePolicyEvaluationResult result =
                devicePolicyEvaluator.evaluateByPolicyName("testPolicy", deviceData, "appId", orgTenant);

        Assert.assertTrue(result.isCompliant());
        Assert.assertEquals(result.getPolicyId(), "rootPolicyId");
        verify(policyEvaluationService).evaluate(eq("rootPolicyId"), eq("android"),
                any(PolicyEvaluationContext.class), eq(ROOT_TENANT));
        verify(policyManagementService, never()).getPolicyIdByName("DEVICE", "testPolicy", orgTenant);
        verify(policyManagementService, never()).getPolicyIdByName("DEVICE", "testPolicy", PARENT_ORG_TENANT);
        verify(integrityDataEnricher).enrich(deviceData, "appId", orgTenant);
    }

    @Test(dataProvider = "organizationTenants")
    public void testEvaluateByIdInOrganizationUsesTheRootPolicy(String orgTenant) throws Exception {
        Map<String, Object> deviceData = new HashMap<>();
        deviceData.put("platform", "ios");

        when(policyManagementService.getPolicyById("rootPolicyId", ROOT_TENANT)).thenReturn(mock(Policy.class));
        mockSatisfiedEvaluation();

        DevicePolicyEvaluationResult result =
                devicePolicyEvaluator.evaluate("rootPolicyId", deviceData, "appId", orgTenant);

        Assert.assertTrue(result.isCompliant());
        verify(policyEvaluationService).evaluate(eq("rootPolicyId"), eq("ios"),
                any(PolicyEvaluationContext.class), eq(ROOT_TENANT));
        verify(policyManagementService, never()).getPolicyById("rootPolicyId", orgTenant);
        verify(integrityDataEnricher).enrich(deviceData, "appId", orgTenant);
    }

    @Test
    public void testEvaluateByPolicyNameInOrganizationThrowsWhenTheRootHasNoPolicy() throws Exception {
        Map<String, Object> deviceData = new HashMap<>();
        deviceData.put("platform", "android");

        when(policyManagementService.getPolicyIdByName("DEVICE", "testPolicy", SUB_ORG_TENANT))
                .thenReturn("subOrgPolicyId");

        try {
            devicePolicyEvaluator.evaluateByPolicyName("testPolicy", deviceData, "appId", SUB_ORG_TENANT);
            Assert.fail("Expected a policy not found error.");
        } catch (DevicePolicyClientException e) {
            Assert.assertEquals(e.getErrorCode(), DevicePolicyErrorMessage.ERROR_DEVICE_POLICY_NOT_FOUND.getCode());
        }
        verify(policyManagementService).getPolicyIdByName("DEVICE", "testPolicy", ROOT_TENANT);
        verify(policyManagementService, never()).getPolicyIdByName("DEVICE", "testPolicy", SUB_ORG_TENANT);
        verify(policyEvaluationService, never()).evaluate(any(), any(), any(), any());
    }

    @Test
    public void testEvaluateByIdInOrganizationThrowsWhenTheRootHasNoPolicy() throws Exception {
        Map<String, Object> deviceData = new HashMap<>();
        deviceData.put("platform", "android");

        try {
            devicePolicyEvaluator.evaluate("unknownPolicyId", deviceData, "appId", SUB_ORG_TENANT);
            Assert.fail("Expected a policy not found error.");
        } catch (DevicePolicyClientException e) {
            Assert.assertEquals(e.getErrorCode(), DevicePolicyErrorMessage.ERROR_DEVICE_POLICY_NOT_FOUND.getCode());
        }
        verify(policyManagementService).getPolicyById("unknownPolicyId", ROOT_TENANT);
        verify(policyManagementService, never()).getPolicyById("unknownPolicyId", SUB_ORG_TENANT);
    }

    @Test
    public void testEvaluateFromTokenByPolicyNameInOrganizationUsesTheRootPolicy() throws Exception {
        Map<String, Object> deviceData = new HashMap<>();
        deviceData.put("platform", "android");

        when(policyManagementService.getPolicyIdByName("DEVICE", "testPolicy", ROOT_TENANT))
                .thenReturn("rootPolicyId");
        mockSatisfiedEvaluation();

        try (MockedConstruction<DeviceTokenExtractor> mockedExtractor = Mockito.mockConstruction(
                DeviceTokenExtractor.class,
                (mock, ctx) -> when(mock.extractWithPublicKey(anyString(), anyString(), anyString(), anyString()))
                        .thenReturn(deviceData))) {

            DevicePolicyEvaluationResult result = devicePolicyEvaluator.evaluateFromTokenByPolicyName(
                    "testPolicy", "token", "publicKey", "correlationId", "appId", SUB_ORG_TENANT);

            Assert.assertTrue(result.isCompliant());
            Assert.assertEquals(result.getPolicyId(), "rootPolicyId");
            verify(mockedExtractor.constructed().get(0))
                    .extractWithPublicKey("token", "publicKey", "correlationId", SUB_ORG_TENANT);
            verify(integrityDataEnricher).enrich(deviceData, "appId", SUB_ORG_TENANT);
            verify(policyEvaluationService).evaluate(eq("rootPolicyId"), eq("android"),
                    any(PolicyEvaluationContext.class), eq(ROOT_TENANT));
        }
    }

    @Test
    public void testEvaluateByPolicyNameThrowsServerErrorWhenTheRootOrganizationCannotBeResolved()
            throws Exception {
        Map<String, Object> deviceData = new HashMap<>();
        deviceData.put("platform", "android");

        when(organizationManager.getPrimaryOrganizationId(SUB_ORG_ID))
                .thenThrow(new OrganizationManagementServerException("error"));

        try {
            devicePolicyEvaluator.evaluateByPolicyName("testPolicy", deviceData, "appId", SUB_ORG_TENANT);
            Assert.fail("Expected a server error.");
        } catch (DevicePolicyServerException e) {
            Assert.assertEquals(e.getErrorCode(),
                    DevicePolicyErrorMessage.ERROR_DEVICE_POLICY_ORGANIZATION_RESOLUTION_FAILED.getCode());
        }
        verify(policyManagementService, never()).getPolicyIdByName(anyString(), anyString(), anyString());
    }

    @Test
    public void testEvaluateByPolicyNameInOrganizationFailsAfterTheRootDeletesThePolicy() throws Exception {
        Map<String, Object> deviceData = new HashMap<>();
        deviceData.put("platform", "android");

        when(policyManagementService.getPolicyIdByName("DEVICE", "testPolicy", ROOT_TENANT))
                .thenReturn("rootPolicyId");
        mockSatisfiedEvaluation();
        devicePolicyEvaluator.evaluateByPolicyName("testPolicy", deviceData, "appId", SUB_ORG_TENANT);

        when(policyManagementService.getPolicyIdByName("DEVICE", "testPolicy", ROOT_TENANT)).thenReturn(null);

        try {
            devicePolicyEvaluator.evaluateByPolicyName("testPolicy", deviceData, "appId", SUB_ORG_TENANT);
            Assert.fail("Expected a policy not found error.");
        } catch (DevicePolicyClientException e) {
            Assert.assertEquals(e.getErrorCode(), DevicePolicyErrorMessage.ERROR_DEVICE_POLICY_NOT_FOUND.getCode());
        }
        verify(policyEvaluationService, times(1)).evaluate(any(), any(), any(), any());
    }

    @Test
    public void testEvaluateByPolicyNameInOrganizationUsesTheNewPolicyAfterTheRootRecreatesIt() throws Exception {
        Map<String, Object> deviceData = new HashMap<>();
        deviceData.put("platform", "android");

        when(policyManagementService.getPolicyIdByName("DEVICE", "testPolicy", ROOT_TENANT))
                .thenReturn("rootPolicyId");
        mockSatisfiedEvaluation();
        devicePolicyEvaluator.evaluateByPolicyName("testPolicy", deviceData, "appId", SUB_ORG_TENANT);

        when(policyManagementService.getPolicyIdByName("DEVICE", "testPolicy", ROOT_TENANT))
                .thenReturn("newRootPolicyId");

        DevicePolicyEvaluationResult result =
                devicePolicyEvaluator.evaluateByPolicyName("testPolicy", deviceData, "appId", SUB_ORG_TENANT);

        Assert.assertEquals(result.getPolicyId(), "newRootPolicyId");
        verify(policyEvaluationService).evaluate(eq("newRootPolicyId"), eq("android"),
                any(PolicyEvaluationContext.class), eq(ROOT_TENANT));
    }

    private void mockSatisfiedEvaluation() throws Exception {

        PolicyEvaluationResult evaluationResult = mock(PolicyEvaluationResult.class);
        when(evaluationResult.isSatisfied()).thenReturn(true);
        when(policyEvaluationService.evaluate(any(), anyString(), any(), anyString())).thenReturn(evaluationResult);
    }
}
