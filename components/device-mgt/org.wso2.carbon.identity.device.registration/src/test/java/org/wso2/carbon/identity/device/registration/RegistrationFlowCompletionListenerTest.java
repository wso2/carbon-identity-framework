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

package org.wso2.carbon.identity.device.registration;

import org.mockito.ArgumentMatcher;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.identity.common.testng.WithCarbonHome;
import org.wso2.carbon.identity.device.mgt.api.exception.DeviceMgtServerException;
import org.wso2.carbon.identity.device.mgt.api.model.Device;
import org.wso2.carbon.identity.device.mgt.api.service.DeviceManagementService;
import org.wso2.carbon.identity.device.registration.internal.DeviceRegistrationExecutorDataHolder;
import org.wso2.carbon.identity.flow.execution.engine.model.FlowExecutionContext;
import org.wso2.carbon.identity.flow.execution.engine.model.FlowExecutionStep;
import org.wso2.carbon.identity.flow.execution.engine.model.FlowUser;
import org.wso2.carbon.identity.flow.mgt.Constants.FlowTypes;
import org.wso2.carbon.identity.flow.mgt.model.GraphConfig;

import java.sql.Timestamp;
import java.time.Instant;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import static org.wso2.carbon.identity.flow.execution.engine.Constants.STATUS_COMPLETE;
import static org.wso2.carbon.identity.flow.execution.engine.Constants.STATUS_INCOMPLETE;

/**
 * Unit tests for {@link RegistrationFlowCompletionListener}.
 */
@WithCarbonHome
public class RegistrationFlowCompletionListenerTest {

    private static final String TENANT_DOMAIN = "test.com";
    private static final String PROVISIONED_USER_ID = "provisioned-user-id";
    private static final String DEVICE_ID = "device-id-1";

    private AutoCloseable closeable;
    private RegistrationFlowCompletionListener listener;

    @Mock
    private DeviceManagementService deviceManagementService;

    private DeviceManagementService originalDeviceManagementService;

    @BeforeClass
    public void setUpClass() {

        closeable = MockitoAnnotations.openMocks(this);
        listener = new RegistrationFlowCompletionListener();

        DeviceRegistrationExecutorDataHolder holder = DeviceRegistrationExecutorDataHolder.getInstance();
        originalDeviceManagementService = holder.getDeviceManagementService();
        holder.setDeviceManagementService(deviceManagementService);
    }

    @AfterClass
    public void tearDownClass() throws Exception {

        DeviceRegistrationExecutorDataHolder.getInstance().setDeviceManagementService(originalDeviceManagementService);

        if (closeable != null) {
            closeable.close();
        }
    }

    @BeforeMethod
    public void setUp() {

        reset(deviceManagementService);
        PrivilegedCarbonContext.startTenantFlow();
        PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(TENANT_DOMAIN);
    }

    @AfterMethod
    public void tearDownMethod() {

        PrivilegedCarbonContext.endTenantFlow();
    }

    @Test
    public void testGetExecutionOrderId() {

        assertEquals(listener.getExecutionOrderId(), 10);
    }

    @Test
    public void testGetDefaultOrderId() {

        assertEquals(listener.getDefaultOrderId(), 10);
    }

    @Test
    public void testIsEnabled() {

        assertTrue(listener.isEnabled());
    }

    @Test
    public void testDoPostExecuteCompletesAndPersistsWithProvisionedUserId() throws Exception {

        FlowExecutionContext context = registrationContext();
        context.getFlowUser().setUserId(PROVISIONED_USER_ID);
        context.setProperty(DeviceRegistrationExecutorConstants.CTX_DEVICE_REGISTRATION, pendingDevice());

        boolean result = listener.doPostExecute(completeStep(), context);

        assertTrue(result);
        verify(deviceManagementService, times(1))
                .persistDevice(argThatUserIdMatches(PROVISIONED_USER_ID), eq(TENANT_DOMAIN));
    }

    @Test
    public void testDoPostExecuteNoVerifiedDeviceOnContextIsNoOp() throws Exception {

        FlowExecutionContext context = registrationContext();
        context.getFlowUser().setUserId(PROVISIONED_USER_ID);
        // CTX_DEVICE_REGISTRATION intentionally not set.

        boolean result = listener.doPostExecute(completeStep(), context);

        assertTrue(result);
        verify(deviceManagementService, never()).persistDevice(any(), any());
    }

    @Test
    public void testDoPostExecuteNonRegistrationFlowIsNoOp() throws Exception {

        FlowExecutionContext context = new FlowExecutionContext();
        context.setTenantDomain(TENANT_DOMAIN);
        context.setGraphConfig(new GraphConfig());
        context.setFlowUser(new FlowUser());
        context.getFlowUser().setUserId(PROVISIONED_USER_ID);
        context.setFlowType(FlowTypes.PASSWORD_RECOVERY.getType());
        context.setProperty(DeviceRegistrationExecutorConstants.CTX_DEVICE_REGISTRATION, pendingDevice());

        boolean result = listener.doPostExecute(completeStep(), context);

        assertTrue(result);
        verify(deviceManagementService, never()).persistDevice(any(), any());
    }

    @Test
    public void testDoPostExecuteStepNotCompleteIsNoOp() throws Exception {

        FlowExecutionContext context = registrationContext();
        context.getFlowUser().setUserId(PROVISIONED_USER_ID);
        context.setProperty(DeviceRegistrationExecutorConstants.CTX_DEVICE_REGISTRATION, pendingDevice());

        FlowExecutionStep incompleteStep = new FlowExecutionStep();
        incompleteStep.setFlowStatus(STATUS_INCOMPLETE);

        boolean result = listener.doPostExecute(incompleteStep, context);

        assertTrue(result);
        verify(deviceManagementService, never()).persistDevice(any(), any());
    }

    @Test
    public void testDoPostExecuteBlankProvisionedUserIdIsNoOp() throws Exception {

        FlowExecutionContext context = registrationContext();
        // Only userId is under test here; a real FlowUser's getUsername() (used in the listener's
        // error log line) never actually returns blank — leaving it unset would fall back to a
        // resolution path that needs an optional module not on this test classpath, so set one.
        context.getFlowUser().setUsername("alice");
        context.getFlowUser().setUserId("  ");
        context.setProperty(DeviceRegistrationExecutorConstants.CTX_DEVICE_REGISTRATION, pendingDevice());

        boolean result = listener.doPostExecute(completeStep(), context);

        assertTrue(result);
        verify(deviceManagementService, never()).persistDevice(any(), any());
    }

    @Test
    public void testDoPostExecuteWhenPersistDeviceThrowsIsHandledGracefully() throws Exception {

        FlowExecutionContext context = registrationContext();
        context.getFlowUser().setUserId(PROVISIONED_USER_ID);
        context.setProperty(DeviceRegistrationExecutorConstants.CTX_DEVICE_REGISTRATION, pendingDevice());

        doThrow(new DeviceMgtServerException("Persist failed.", "Simulated failure.", "TEST-001"))
                .when(deviceManagementService).persistDevice(any(), any());

        boolean result = listener.doPostExecute(completeStep(), context);

        assertTrue(result);
    }

    private FlowExecutionContext registrationContext() {

        FlowExecutionContext context = new FlowExecutionContext();
        context.setTenantDomain(TENANT_DOMAIN);
        context.setGraphConfig(new GraphConfig());
        context.setFlowUser(new FlowUser());
        context.setFlowType(FlowTypes.REGISTRATION.getType());
        return context;
    }

    private FlowExecutionStep completeStep() {

        FlowExecutionStep step = new FlowExecutionStep();
        step.setFlowStatus(STATUS_COMPLETE);
        return step;
    }

    private Device pendingDevice() {

        return new Device.Builder()
                .id(DEVICE_ID)
                .deviceName("Alice's Device")
                .publicKey("base64PublicKey")
                .status(Device.Status.ACTIVE)
                .registeredAt(Timestamp.from(Instant.now()))
                .build();
    }

    private Device argThatUserIdMatches(String expectedUserId) {

        ArgumentMatcher<Device> matcher = device -> device != null
                && expectedUserId.equals(device.getUserId())
                && DEVICE_ID.equals(device.getId());
        return argThat(matcher);
    }
}
