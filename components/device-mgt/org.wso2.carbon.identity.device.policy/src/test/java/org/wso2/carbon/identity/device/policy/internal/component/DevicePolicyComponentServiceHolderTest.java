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

package org.wso2.carbon.identity.device.policy.internal.component;

import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.application.authentication.framework.JsFunctionRegistry;
import org.wso2.carbon.identity.client.attestation.mgt.services.ClientAttestationService;
import org.wso2.carbon.identity.device.mgt.api.service.DeviceManagementService;
import org.wso2.carbon.identity.device.policy.api.service.DevicePolicyEvaluator;
import org.wso2.carbon.identity.device.policy.internal.service.IntegrityDataEnricher;
import org.wso2.carbon.identity.policy.evaluation.api.service.PolicyEvaluationService;
import org.wso2.carbon.identity.policy.management.api.service.PolicyManagementService;

import static org.mockito.Mockito.mock;

public class DevicePolicyComponentServiceHolderTest {

    private DevicePolicyComponentServiceHolder holder;

    @BeforeMethod
    public void setUp() {
        holder = DevicePolicyComponentServiceHolder.getInstance();
    }

    @AfterMethod
    public void tearDown() {
        holder.setPolicyEvaluationService(null);
        holder.setPolicyManagementService(null);
        holder.setDeviceManagementService(null);
        holder.setJsFunctionRegistry(null);
        holder.setClientAttestationService(null);
        holder.setDevicePolicyEvaluator(null);
        holder.setIntegrityDataEnricher(null);
    }

    @Test
    public void testPolicyEvaluationService() {
        PolicyEvaluationService service = mock(PolicyEvaluationService.class);
        holder.setPolicyEvaluationService(service);
        Assert.assertEquals(holder.getPolicyEvaluationService(), service);
    }

    @Test
    public void testPolicyManagementService() {
        PolicyManagementService service = mock(PolicyManagementService.class);
        holder.setPolicyManagementService(service);
        Assert.assertEquals(holder.getPolicyManagementService(), service);
    }

    @Test
    public void testDeviceManagementService() {
        DeviceManagementService service = mock(DeviceManagementService.class);
        holder.setDeviceManagementService(service);
        Assert.assertEquals(holder.getDeviceManagementService(), service);
    }

    @Test
    public void testJsFunctionRegistry() {
        JsFunctionRegistry registry = mock(JsFunctionRegistry.class);
        holder.setJsFunctionRegistry(registry);
        Assert.assertEquals(holder.getJsFunctionRegistry(), registry);
    }

    @Test
    public void testClientAttestationService() {
        ClientAttestationService service = mock(ClientAttestationService.class);
        holder.setClientAttestationService(service);
        Assert.assertEquals(holder.getClientAttestationService(), service);
    }

    @Test
    public void testDevicePolicyEvaluator() {
        DevicePolicyEvaluator evaluator = mock(DevicePolicyEvaluator.class);
        holder.setDevicePolicyEvaluator(evaluator);
        Assert.assertEquals(holder.getDevicePolicyEvaluator(), evaluator);
    }

    @Test
    public void testIntegrityDataEnricher() {
        IntegrityDataEnricher enricher = mock(IntegrityDataEnricher.class);
        holder.setIntegrityDataEnricher(enricher);
        Assert.assertEquals(holder.getIntegrityDataEnricher(), enricher);
    }
}
