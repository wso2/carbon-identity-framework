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

package org.wso2.carbon.identity.api.resource.collection.mgt;

import org.mockito.MockedStatic;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.api.resource.collection.mgt.constant.APIResourceCollectionManagementConstants;
import org.wso2.carbon.identity.api.resource.collection.mgt.util.APIResourceCollectionManagementUtil;
import org.wso2.carbon.identity.core.util.IdentityUtil;

import static org.mockito.Mockito.mockStatic;

/**
 * Unit tests for {@link APIResourceCollectionManagementUtil}.
 */
public class APIResourceCollectionManagementUtilTest {

    @DataProvider(name = "granularConsolePermissionsConfigProvider")
    public Object[][] granularConsolePermissionsConfigProvider() {

        // {configured value of ConsoleSettings.UseGranularConsolePermissions, expected boolean}
        return new Object[][]{
                {"true", true},
                {"TRUE", true},
                {"false", false},
                {"", false},
                {null, false},
                {"not-a-boolean", false},
        };
    }

    @Test(dataProvider = "granularConsolePermissionsConfigProvider")
    public void testIsGranularConsolePermissionsEnabled(String configuredValue, boolean expected) {

        try (MockedStatic<IdentityUtil> identityUtil = mockStatic(IdentityUtil.class)) {
            identityUtil.when(() -> IdentityUtil.getProperty(
                            APIResourceCollectionManagementConstants.USE_GRANULAR_CONSOLE_PERMISSIONS_CONFIG))
                    .thenReturn(configuredValue);

            Assert.assertEquals(
                    APIResourceCollectionManagementUtil.isGranularConsolePermissionsEnabled(), expected,
                    "Unexpected granular console permission resolution for configured value: " + configuredValue);
        }
    }
}
