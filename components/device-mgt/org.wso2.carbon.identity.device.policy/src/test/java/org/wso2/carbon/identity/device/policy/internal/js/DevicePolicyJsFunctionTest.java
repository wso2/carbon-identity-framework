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

package org.wso2.carbon.identity.device.policy.internal.js;

import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.application.authentication.framework.config.model.graph.js.base.JsBaseAuthenticationContext;
import org.wso2.carbon.identity.application.authentication.framework.context.AuthenticationContext;
import org.wso2.carbon.identity.application.authentication.framework.util.FrameworkConstants;
import org.wso2.carbon.identity.central.log.mgt.utils.LoggerUtils;
import org.wso2.carbon.identity.common.testng.CarbonBasedTestListener;
import org.wso2.carbon.identity.common.testng.WithCarbonHome;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;
import org.wso2.carbon.identity.device.policy.api.exception.DevicePolicyClientException;
import org.wso2.carbon.identity.device.policy.api.exception.DevicePolicyServerException;
import org.wso2.carbon.identity.device.policy.api.model.DevicePolicyEvaluationResult;
import org.wso2.carbon.identity.device.policy.api.service.DevicePolicyEvaluator;
import org.wso2.carbon.identity.device.policy.internal.component.DevicePolicyComponentServiceHolder;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mockStatic;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;

/**
 * Unit tests for DevicePolicyJsFunction covering all 7 paths.
 */
@WithCarbonHome
@Listeners(CarbonBasedTestListener.class)
public class DevicePolicyJsFunctionTest {

    private DevicePolicyJsFunction jsFunction;
    private JsBaseAuthenticationContext jsContext;
    private AuthenticationContext authContext;
    private DevicePolicyEvaluator devicePolicyEvaluator;
    private MockedStatic<DevicePolicyComponentServiceHolder> mockedHolder;
    private MockedStatic<IdentityTenantUtil> mockedIdentityTenantUtil;
    private MockedStatic<LoggerUtils> mockedLoggerUtils;

    @BeforeMethod
    public void setUp() {

        jsFunction = new DevicePolicyJsFunction();
        jsContext = Mockito.mock(JsBaseAuthenticationContext.class);
        authContext = Mockito.mock(AuthenticationContext.class);
        Mockito.when(jsContext.getWrapped()).thenReturn(authContext);

        devicePolicyEvaluator = Mockito.mock(DevicePolicyEvaluator.class);

        mockedHolder = Mockito.mockStatic(DevicePolicyComponentServiceHolder.class);
        DevicePolicyComponentServiceHolder holderInstance = Mockito.mock(DevicePolicyComponentServiceHolder.class);
        mockedHolder.when(DevicePolicyComponentServiceHolder::getInstance).thenReturn(holderInstance);
        Mockito.when(holderInstance.getDevicePolicyEvaluator()).thenReturn(devicePolicyEvaluator);

        mockedIdentityTenantUtil = mockStatic(IdentityTenantUtil.class);
        mockedIdentityTenantUtil.when(() -> IdentityTenantUtil.getTenantId(anyString())).thenReturn(-1234);

        mockedLoggerUtils = mockStatic(LoggerUtils.class);
        mockedLoggerUtils.when(LoggerUtils::isDiagnosticLogsEnabled).thenReturn(false);
    }

    @AfterMethod
    public void tearDown() {

        mockedHolder.close();
        mockedIdentityTenantUtil.close();
        mockedLoggerUtils.close();
    }

    @Test
    public void testApplyCompliantResult() throws Exception {

        Map<String, Object> deviceData = new HashMap<>();
        deviceData.put("platform", "android");
        Mockito.when(authContext.getProperty(FrameworkConstants.DEVICE_DATA)).thenReturn(deviceData);
        Mockito.when(authContext.getTenantDomain()).thenReturn("carbon.super");
        Mockito.when(authContext.getServiceProviderResourceId()).thenReturn("app-123");

        Mockito.when(devicePolicyEvaluator.evaluate("testPolicy", deviceData, "app-123", "carbon.super"))
                .thenReturn(DevicePolicyEvaluationResult.compliant("testPolicy"));

        String result = jsFunction.apply(jsContext, "testPolicy");
        assertNull(result);
    }

    @Test
    public void testApplyNonCompliantResult() throws Exception {

        Map<String, Object> deviceData = new HashMap<>();
        deviceData.put("platform", "android");
        Mockito.when(authContext.getProperty(FrameworkConstants.DEVICE_DATA)).thenReturn(deviceData);
        Mockito.when(authContext.getTenantDomain()).thenReturn("carbon.super");
        Mockito.when(authContext.getServiceProviderResourceId()).thenReturn("app-123");

        Mockito.when(devicePolicyEvaluator.evaluate("testPolicy", deviceData, "app-123", "carbon.super"))
                .thenReturn(DevicePolicyEvaluationResult.nonCompliant(
                        "testPolicy", Arrays.asList("isRooted", "diskEncryption")));

        String result = jsFunction.apply(jsContext, "testPolicy");
        assertEquals(result, "isRooted, diskEncryption");
    }

    @Test
    public void testApplyIncompleteDeviceDataResult() throws Exception {

        Map<String, Object> deviceData = new HashMap<>();
        Mockito.when(authContext.getProperty(FrameworkConstants.DEVICE_DATA)).thenReturn(deviceData);
        Mockito.when(authContext.getTenantDomain()).thenReturn("carbon.super");
        Mockito.when(authContext.getServiceProviderResourceId()).thenReturn("app-123");

        Mockito.when(devicePolicyEvaluator.evaluate("testPolicy", deviceData, "app-123", "carbon.super"))
                .thenReturn(DevicePolicyEvaluationResult.incompleteDeviceData(
                        "testPolicy", Arrays.asList("platform")));

        String result = jsFunction.apply(jsContext, "testPolicy");
        assertEquals(result, "platform");
    }

    @Test
    public void testApplyPolicyNotFoundResult() throws Exception {

        Map<String, Object> deviceData = new HashMap<>();
        deviceData.put("platform", "android");
        Mockito.when(authContext.getProperty(FrameworkConstants.DEVICE_DATA)).thenReturn(deviceData);
        Mockito.when(authContext.getTenantDomain()).thenReturn("carbon.super");
        Mockito.when(authContext.getServiceProviderResourceId()).thenReturn("app-123");

        Mockito.when(devicePolicyEvaluator.evaluate("testPolicy", deviceData, "app-123", "carbon.super"))
                .thenReturn(DevicePolicyEvaluationResult.policyNotFound("testPolicy"));

        String result = jsFunction.apply(jsContext, "testPolicy");
        assertEquals(result, "testPolicy:policy_not_found");
    }

    @Test
    public void testApplyWithMissingDeviceData() {

        Mockito.when(authContext.getProperty(FrameworkConstants.DEVICE_DATA)).thenReturn(null);
        Mockito.when(authContext.getTenantDomain()).thenReturn("carbon.super");
        String result = jsFunction.apply(jsContext, "testPolicy");
        assertEquals(result, "testPolicy:device token is missing or validation failed");
    }

    @Test
    public void testApplyWithDevicePolicyClientException() throws Exception {

        Map<String, Object> deviceData = new HashMap<>();
        Mockito.when(authContext.getProperty(FrameworkConstants.DEVICE_DATA)).thenReturn(deviceData);
        Mockito.when(authContext.getTenantDomain()).thenReturn("carbon.super");
        Mockito.when(authContext.getServiceProviderResourceId()).thenReturn("app-123");

        Mockito.when(devicePolicyEvaluator.evaluate("testPolicy", deviceData, "app-123", "carbon.super"))
                .thenThrow(Mockito.mock(DevicePolicyClientException.class));

        String result = jsFunction.apply(jsContext, "testPolicy");
        assertEquals(result, "testPolicy:policy_error");
    }

    @Test
    public void testApplyWithDevicePolicyServerException() throws Exception {

        Map<String, Object> deviceData = new HashMap<>();
        Mockito.when(authContext.getProperty(FrameworkConstants.DEVICE_DATA)).thenReturn(deviceData);
        Mockito.when(authContext.getTenantDomain()).thenReturn("carbon.super");
        Mockito.when(authContext.getServiceProviderResourceId()).thenReturn("app-123");

        Mockito.when(devicePolicyEvaluator.evaluate("testPolicy", deviceData, "app-123", "carbon.super"))
                .thenThrow(Mockito.mock(DevicePolicyServerException.class));

        String result = jsFunction.apply(jsContext, "testPolicy");
        assertEquals(result, "testPolicy:evaluation_error");
    }
}
