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

package org.wso2.carbon.identity.application.authentication.framework.internal;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.application.authentication.framework.handler.device.DeviceDataResolver;

import static org.mockito.Mockito.mock;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;

/**
 * Unit tests for the optional service references bound by {@link FrameworkServiceComponent}.
 */
public class FrameworkServiceComponentTest {

    @AfterMethod
    public void tearDown() {

        FrameworkServiceDataHolder.getInstance().setDeviceDataResolver(null);
    }

    @Test
    public void testDeviceDataResolverIsAbsentByDefault() {

        assertNull(FrameworkServiceDataHolder.getInstance().getDeviceDataResolver());
    }

    @Test
    public void testSetDeviceDataResolver() {

        FrameworkServiceComponent serviceComponent = new FrameworkServiceComponent();
        DeviceDataResolver deviceDataResolver = mock(DeviceDataResolver.class);

        serviceComponent.setDeviceDataResolver(deviceDataResolver);

        assertEquals(FrameworkServiceDataHolder.getInstance().getDeviceDataResolver(), deviceDataResolver);
    }

    @Test
    public void testUnsetDeviceDataResolver() {

        FrameworkServiceComponent serviceComponent = new FrameworkServiceComponent();
        DeviceDataResolver deviceDataResolver = mock(DeviceDataResolver.class);

        serviceComponent.setDeviceDataResolver(deviceDataResolver);
        serviceComponent.unsetDeviceDataResolver(deviceDataResolver);

        assertNull(FrameworkServiceDataHolder.getInstance().getDeviceDataResolver());
    }
}
