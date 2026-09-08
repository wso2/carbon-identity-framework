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

package org.wso2.carbon.identity.device.policy.internal.service;

import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.central.log.mgt.utils.LoggerUtils;
import org.wso2.carbon.identity.client.attestation.mgt.exceptions.ClientAttestationMgtException;
import org.wso2.carbon.identity.client.attestation.mgt.model.ClientAttestationContext;
import org.wso2.carbon.identity.client.attestation.mgt.services.ClientAttestationService;
import org.wso2.carbon.identity.client.attestation.mgt.utils.Constants;
import org.wso2.carbon.identity.common.testng.CarbonBasedTestListener;
import org.wso2.carbon.identity.common.testng.WithCarbonHome;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;
import org.wso2.carbon.identity.device.policy.api.constant.DevicePolicyErrorMessage;
import org.wso2.carbon.identity.device.policy.api.exception.DevicePolicyClientException;
import org.wso2.carbon.identity.device.policy.api.exception.DevicePolicyException;
import org.wso2.carbon.identity.device.policy.internal.component.DevicePolicyComponentServiceHolder;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

@WithCarbonHome
@Listeners(CarbonBasedTestListener.class)
public class IntegrityDataEnricherTest {

    private IntegrityDataEnricher integrityDataEnricher;
    private MockedStatic<DevicePolicyComponentServiceHolder> mockedServiceHolder;
    private MockedStatic<IdentityTenantUtil> mockedIdentityTenantUtil;
    private MockedStatic<LoggerUtils> mockedLoggerUtils;
    private DevicePolicyComponentServiceHolder serviceHolder;
    private ClientAttestationService clientAttestationService;

    @BeforeMethod
    public void setUp() {
        integrityDataEnricher = new IntegrityDataEnricher();
        serviceHolder = mock(DevicePolicyComponentServiceHolder.class);
        clientAttestationService = mock(ClientAttestationService.class);

        mockedServiceHolder = Mockito.mockStatic(DevicePolicyComponentServiceHolder.class);
        mockedServiceHolder.when(DevicePolicyComponentServiceHolder::getInstance).thenReturn(serviceHolder);
        when(serviceHolder.getClientAttestationService()).thenReturn(clientAttestationService);

        mockedIdentityTenantUtil = mockStatic(IdentityTenantUtil.class);
        mockedIdentityTenantUtil.when(() -> IdentityTenantUtil.getTenantId(anyString())).thenReturn(-1234);

        mockedLoggerUtils = mockStatic(LoggerUtils.class);
        mockedLoggerUtils.when(LoggerUtils::isDiagnosticLogsEnabled).thenReturn(false);
    }

    @AfterMethod
    public void tearDown() {
        mockedServiceHolder.close();
        mockedIdentityTenantUtil.close();
        mockedLoggerUtils.close();
    }

    @Test
    public void testEnrichWithoutToken() throws Exception {
        Map<String, Object> deviceData = new HashMap<>();
        integrityDataEnricher.enrich(deviceData, "appId", "carbon.super");

        Assert.assertTrue(deviceData.isEmpty(), "Device data should remain empty if no attestation token is provided.");
    }

    @Test
    public void testEnrichWithoutAttestationService() throws Exception {
        when(serviceHolder.getClientAttestationService()).thenReturn(null);

        Map<String, Object> deviceData = new HashMap<>();
        deviceData.put("attestationToken", "test-token");
        integrityDataEnricher.enrich(deviceData, "appId", "carbon.super");

        Assert.assertFalse(deviceData.containsKey("attestationToken"));
        Assert.assertEquals(deviceData.get("androidIntegrity"), "INTEGRITY_FAILED");
        Assert.assertEquals(deviceData.get("iosIntegrity"), "false");
    }

    @Test
    public void testEnrichWithAndroidAttestation() throws Exception {
        ClientAttestationContext context = mock(ClientAttestationContext.class);
        when(context.getClientType()).thenReturn(Constants.ClientTypes.ANDROID);
        when(context.getDeviceIntegrityVerdicts()).thenReturn(Collections.singletonList("MEETS_STRONG_INTEGRITY"));

        when(clientAttestationService.validateAttestation(anyString(), anyString(), anyString())).thenReturn(context);

        Map<String, Object> deviceData = new HashMap<>();
        deviceData.put("attestationToken", "test-token");
        integrityDataEnricher.enrich(deviceData, "appId", "carbon.super");

        Assert.assertEquals(deviceData.get("androidIntegrity"), "MEETS_STRONG_INTEGRITY");
    }

    @Test
    public void testEnrichWithAndroidAttestationVariousVerdicts() throws Exception {
        // Device integrity.
        ClientAttestationContext context = mock(ClientAttestationContext.class);
        when(context.getClientType()).thenReturn(Constants.ClientTypes.ANDROID);
        when(context.getDeviceIntegrityVerdicts()).thenReturn(Arrays.asList("MEETS_DEVICE_INTEGRITY"));
        when(clientAttestationService.validateAttestation(anyString(), anyString(), anyString())).thenReturn(context);
        Map<String, Object> deviceData = new HashMap<>();
        deviceData.put("attestationToken", "test-token");
        integrityDataEnricher.enrich(deviceData, "appId", "carbon.super");
        Assert.assertEquals(deviceData.get("androidIntegrity"), "MEETS_DEVICE_INTEGRITY");

        // Basic integrity.
        when(context.getDeviceIntegrityVerdicts()).thenReturn(Arrays.asList("MEETS_BASIC_INTEGRITY"));
        deviceData.put("attestationToken", "test-token");
        integrityDataEnricher.enrich(deviceData, "appId", "carbon.super");
        Assert.assertEquals(deviceData.get("androidIntegrity"), "MEETS_BASIC_INTEGRITY");

        // Virtual integrity.
        when(context.getDeviceIntegrityVerdicts()).thenReturn(Arrays.asList("MEETS_VIRTUAL_INTEGRITY"));
        deviceData.put("attestationToken", "test-token");
        integrityDataEnricher.enrich(deviceData, "appId", "carbon.super");
        Assert.assertEquals(deviceData.get("androidIntegrity"), "MEETS_VIRTUAL_INTEGRITY");

        // Integrity failed (empty verdicts).
        when(context.getDeviceIntegrityVerdicts()).thenReturn(Collections.emptyList());
        deviceData.put("attestationToken", "test-token");
        integrityDataEnricher.enrich(deviceData, "appId", "carbon.super");
        Assert.assertEquals(deviceData.get("androidIntegrity"), "INTEGRITY_FAILED");
    }

    @Test
    public void testEnrichWithIOSAttestation() throws Exception {
        ClientAttestationContext context = mock(ClientAttestationContext.class);
        when(context.getClientType()).thenReturn(Constants.ClientTypes.IOS);
        when(context.isAttested()).thenReturn(true);

        when(clientAttestationService.validateAttestation(anyString(), anyString(), anyString())).thenReturn(context);

        Map<String, Object> deviceData = new HashMap<>();
        deviceData.put("attestationToken", "test-token");
        integrityDataEnricher.enrich(deviceData, "appId", "carbon.super");

        Assert.assertEquals(deviceData.get("iosIntegrity"), "true");
    }

    @Test
    public void testEnrichWithUnrecognizedClientTypeFailsClosed() throws Exception {
        ClientAttestationContext context = mock(ClientAttestationContext.class);
        when(context.getClientType()).thenReturn(null);

        when(clientAttestationService.validateAttestation(anyString(), anyString(), anyString())).thenReturn(context);

        Map<String, Object> deviceData = new HashMap<>();
        deviceData.put("attestationToken", "test-token");
        integrityDataEnricher.enrich(deviceData, "appId", "carbon.super");

        Assert.assertEquals(deviceData.get("androidIntegrity"), "INTEGRITY_FAILED");
        Assert.assertEquals(deviceData.get("iosIntegrity"), "false");
    }

    @Test
    public void testEnrichWithAttestationException() throws ClientAttestationMgtException, DevicePolicyException {
        when(clientAttestationService.validateAttestation(anyString(), anyString(), anyString()))
                .thenThrow(new ClientAttestationMgtException("Invalid token"));

        Map<String, Object> deviceData = new HashMap<>();
        deviceData.put("attestationToken", "test-token");

        try {
            integrityDataEnricher.enrich(deviceData, "appId", "carbon.super");
            Assert.fail("Expected a DevicePolicyClientException to be thrown.");
        } catch (DevicePolicyClientException e) {
            Assert.assertEquals(e.getErrorCode(),
                    DevicePolicyErrorMessage.ERROR_DEVICE_ATTESTATION_VERIFICATION_FAILED.getCode());
        }
    }
}
