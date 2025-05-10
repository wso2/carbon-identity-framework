/*
 * Copyright (c) 2025, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.carbon.identity.core.util;

import org.mockito.MockedStatic;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
import java.util.Map;

import static org.mockito.Mockito.mockStatic;

public class IdentityTenantUtilTest {

    MockedStatic<IdentityTenantUtil> identityTenantUtil;

    private static final String SUPER_TENANT = "carbon.super";

    @BeforeMethod
    public void setUp() throws Exception {

        IdentityUtil.threadLocalProperties.remove();

        identityTenantUtil = mockStatic(IdentityTenantUtil.class);
    }

    @AfterMethod
    public void tearDown() throws Exception {

        IdentityUtil.threadLocalProperties.remove();

        identityTenantUtil.close();
    }

    @DataProvider
    public Object[][] getShouldUseTenantQualifiedURLsConfigData() {

        return new Object[][]{
                { true, true, true }, // tenant qualified URLs are enabled server wide (default behaviour)
                { false, true,  true  }, // tenant qualified URLs are disabled but is a system application
                { false, false, false },  // tenant qualified URLs are disabled and is a client application
        };
    }


    @Test(dataProvider = "getShouldUseTenantQualifiedURLsConfigData")
    public void testShouldUseTenantQualifiedURLs(boolean isTenantQualifiedURLsEnabled, boolean isSystemApplication,
                                                 boolean expectedResult) {

        identityTenantUtil.when(IdentityTenantUtil::isTenantQualifiedUrlsEnabled).
                thenReturn(isTenantQualifiedURLsEnabled);
        identityTenantUtil.when(IdentityTenantUtil::shouldUseTenantQualifiedURLs).thenCallRealMethod();

        Map<String, Object> threadLocalProperties = IdentityUtil.threadLocalProperties.get();
        threadLocalProperties.clear();
        threadLocalProperties.put(IdentityCoreConstants.IS_SYSTEM_APPLICATION, isSystemApplication);

        assertEquals(IdentityTenantUtil.shouldUseTenantQualifiedURLs(), expectedResult);
    }

    @DataProvider
    public Object[][] getIsSystemApplicationConfigData() {

        return new Object[][]{
                { SUPER_TENANT, "MY_ACCOUNT", true },
                { SUPER_TENANT, "app-1-client-id", false },
                { "abc.com", "MY_ACCOUNT_tenant.com", false },
                { "abc.com", "CONSOLE_abc.com", true },
        };
    }

    @Test(dataProvider = "getIsSystemApplicationConfigData")
    public void testIsSystemApplication(String tenantDomain, String clientID, boolean expectedResult) {

        identityTenantUtil.when(() -> IdentityTenantUtil.isSystemApplication(tenantDomain, clientID)).
                thenCallRealMethod();

        assertEquals(IdentityTenantUtil.isSystemApplication(tenantDomain, clientID), expectedResult);
    }
}
