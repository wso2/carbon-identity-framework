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

package org.wso2.carbon.identity.device.registration.internal.component;

import org.mockito.MockedStatic;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.ComponentContext;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.core.util.IdentityUtil;
import org.wso2.carbon.identity.device.registration.executor.DeviceRegistrationExecutor;
import org.wso2.carbon.identity.device.registration.internal.constant.DeviceRegistrationConstants;
import org.wso2.carbon.identity.device.registration.listener.RegistrationFlowCompletionListener;
import org.wso2.carbon.identity.flow.execution.engine.graph.Executor;
import org.wso2.carbon.identity.flow.execution.engine.listener.FlowExecutionListener;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests for {@link DeviceRegistrationServiceComponent}.
 */
public class DeviceRegistrationServiceComponentTest {

    @DataProvider
    public Object[][] disabledConfigValues() {

        return new Object[][]{{null}, {""}, {"true"}, {"TRUE"}, {"invalid"}};
    }

    @Test(dataProvider = "disabledConfigValues")
    public void testExecutorNotRegisteredWhenDisabled(String configValue) {

        BundleContext bundleContext = mock(BundleContext.class);
        try (MockedStatic<IdentityUtil> identityUtil = mockStatic(IdentityUtil.class)) {
            identityUtil.when(() -> IdentityUtil.getProperty(
                    DeviceRegistrationConstants.DISABLE_REGISTRATION_EXECUTOR_CONFIG)).thenReturn(configValue);

            new DeviceRegistrationServiceComponent().activate(componentContext(bundleContext));
        }

        verify(bundleContext, never()).registerService(anyString(), any(), any());
    }

    @Test
    public void testExecutorRegisteredWhenEnabled() {

        BundleContext bundleContext = mock(BundleContext.class);
        try (MockedStatic<IdentityUtil> identityUtil = mockStatic(IdentityUtil.class)) {
            identityUtil.when(() -> IdentityUtil.getProperty(
                    DeviceRegistrationConstants.DISABLE_REGISTRATION_EXECUTOR_CONFIG)).thenReturn(" false ");

            new DeviceRegistrationServiceComponent().activate(componentContext(bundleContext));
        }

        verify(bundleContext).registerService(eq(Executor.class.getName()),
                any(DeviceRegistrationExecutor.class), isNull());
        verify(bundleContext).registerService(eq(FlowExecutionListener.class.getName()),
                any(RegistrationFlowCompletionListener.class), isNull());
    }

    private ComponentContext componentContext(BundleContext bundleContext) {

        ComponentContext componentContext = mock(ComponentContext.class);
        when(componentContext.getBundleContext()).thenReturn(bundleContext);
        return componentContext;
    }
}
