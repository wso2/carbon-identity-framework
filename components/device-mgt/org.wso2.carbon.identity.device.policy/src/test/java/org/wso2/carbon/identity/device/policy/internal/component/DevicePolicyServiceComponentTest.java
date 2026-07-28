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

import org.apache.commons.logging.Log;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.ComponentContext;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.application.authentication.framework.JsFunctionRegistry;
import org.wso2.carbon.identity.client.attestation.mgt.services.ClientAttestationService;
import org.wso2.carbon.identity.core.util.IdentityUtil;
import org.wso2.carbon.identity.device.mgt.api.service.DeviceManagementService;
import org.wso2.carbon.identity.device.policy.internal.config.DeviceFieldConfigLoader;
import org.wso2.carbon.identity.policy.evaluation.api.service.PolicyEvaluationService;
import org.wso2.carbon.identity.policy.management.api.service.PolicyManagementService;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

public class DevicePolicyServiceComponentTest {

    private DevicePolicyServiceComponent component;

    @Mock
    private ComponentContext componentContext;

    @Mock
    private BundleContext bundleContext;

    private MockedStatic<DeviceFieldConfigLoader> mockedDeviceFieldConfigLoader;
    private MockedStatic<IdentityUtil> mockedIdentityUtil;
    @Mock
    private Log mockLog;

    @BeforeMethod
    public void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        component = new DevicePolicyServiceComponent();
        when(componentContext.getBundleContext()).thenReturn(bundleContext);

        mockedDeviceFieldConfigLoader = mockStatic(DeviceFieldConfigLoader.class);
        mockedIdentityUtil = mockStatic(IdentityUtil.class);

        JsFunctionRegistry jsFunctionRegistry = mock(JsFunctionRegistry.class);
        DevicePolicyComponentServiceHolder.getInstance().setJsFunctionRegistry(jsFunctionRegistry);
    }

    @AfterMethod
    public void tearDown() {
        mockedDeviceFieldConfigLoader.close();
        mockedIdentityUtil.close();
        
        DevicePolicyComponentServiceHolder holder = DevicePolicyComponentServiceHolder.getInstance();
        holder.setPolicyEvaluationService(null);
        holder.setPolicyManagementService(null);
        holder.setDeviceManagementService(null);
        holder.setJsFunctionRegistry(null);
        holder.setClientAttestationService(null);
        holder.setDevicePolicyEvaluator(null);
        holder.setIntegrityDataEnricher(null);
    }

    @Test
    public void testActivate() {
        mockedIdentityUtil.when(() -> IdentityUtil.getProperty(anyString())).thenReturn("false");
        mockedDeviceFieldConfigLoader.when(DeviceFieldConfigLoader::load).thenAnswer(invocation -> null);

        try {
            component.activate(componentContext);
        } catch (NoSuchMethodError e) {
            // Ignore PaxLogger error
        }

        Assert.assertNotNull(DevicePolicyComponentServiceHolder.getInstance().getDevicePolicyEvaluator());
        Assert.assertNotNull(DevicePolicyComponentServiceHolder.getInstance().getIntegrityDataEnricher());
    }

    @Test
    public void testDeactivate() {
        try {
            component.deactivate(componentContext);
        } catch (NoSuchMethodError e) {
            // Ignore
        }
    }

    @Test
    public void testSetAndUnsetPolicyEvaluationService() {
        PolicyEvaluationService service = mock(PolicyEvaluationService.class);
        try {
            component.setPolicyEvaluationService(service);
        } catch (NoSuchMethodError e) {
            // Ignore
        }
        Assert.assertEquals(DevicePolicyComponentServiceHolder.getInstance().getPolicyEvaluationService(), service);

        try {
            component.unsetPolicyEvaluationService(service);
        } catch (NoSuchMethodError e) {
            // Ignore
        }
        Assert.assertNull(DevicePolicyComponentServiceHolder.getInstance().getPolicyEvaluationService());
    }

    @Test
    public void testSetAndUnsetPolicyManagementService() {
        PolicyManagementService service = mock(PolicyManagementService.class);
        try {
            component.setPolicyManagementService(service);
        } catch (NoSuchMethodError e) {
            // Ignore
        }
        Assert.assertEquals(DevicePolicyComponentServiceHolder.getInstance().getPolicyManagementService(), service);

        try {
            component.unsetPolicyManagementService(service);
        } catch (NoSuchMethodError e) {
            // Ignore
        }
        Assert.assertNull(DevicePolicyComponentServiceHolder.getInstance().getPolicyManagementService());
    }

    @Test
    public void testSetAndUnsetDeviceManagementService() {
        DeviceManagementService service = mock(DeviceManagementService.class);
        try {
            component.setDeviceManagementService(service);
        } catch (NoSuchMethodError e) {
            // Ignore
        }
        Assert.assertEquals(DevicePolicyComponentServiceHolder.getInstance().getDeviceManagementService(), service);

        try {
            component.unsetDeviceManagementService(service);
        } catch (NoSuchMethodError e) {
            // Ignore
        }
        Assert.assertNull(DevicePolicyComponentServiceHolder.getInstance().getDeviceManagementService());
    }

    @Test
    public void testSetAndUnsetJsFunctionRegistry() {
        JsFunctionRegistry registry = mock(JsFunctionRegistry.class);
        try {
            component.setJsFunctionRegistry(registry);
        } catch (NoSuchMethodError e) {
            // Ignore
        }
        Assert.assertEquals(DevicePolicyComponentServiceHolder.getInstance().getJsFunctionRegistry(), registry);

        try {
            component.unsetJsFunctionRegistry(registry);
        } catch (NoSuchMethodError e) {
            // Ignore
        }
        Assert.assertNull(DevicePolicyComponentServiceHolder.getInstance().getJsFunctionRegistry());
    }

    @Test
    public void testSetAndUnsetClientAttestationService() {
        ClientAttestationService service = mock(ClientAttestationService.class);
        try {
            component.setClientAttestationService(service);
        } catch (NoSuchMethodError e) {
            // Ignore
        }
        Assert.assertEquals(DevicePolicyComponentServiceHolder.getInstance().getClientAttestationService(), service);

        try {
            component.unsetClientAttestationService(service);
        } catch (NoSuchMethodError e) {
            // Ignore
        }
        Assert.assertNull(DevicePolicyComponentServiceHolder.getInstance().getClientAttestationService());
    }
}
