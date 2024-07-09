/*
 * Copyright (c) 2024, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.carbon.identity.trusted.app.mgt;

import org.mockito.MockedStatic;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.application.common.model.TrustedApp;
import org.wso2.carbon.identity.application.common.util.IdentityApplicationConstants.PlatformType;
import org.wso2.carbon.identity.application.mgt.ApplicationManagementService;
import org.wso2.carbon.identity.trusted.app.mgt.internal.TrustedAppMgtDataHolder;
import org.wso2.carbon.identity.trusted.app.mgt.model.TrustedAndroidApp;
import org.wso2.carbon.identity.trusted.app.mgt.model.TrustedIosApp;
import org.wso2.carbon.identity.trusted.app.mgt.services.TrustedAppMgtService;
import org.wso2.carbon.identity.trusted.app.mgt.services.TrustedAppMgtServiceImpl;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;
import static org.wso2.carbon.identity.trusted.app.mgt.utils.Constants.ANDROID_CREDENTIAL_PERMISSION;
import static org.wso2.carbon.identity.trusted.app.mgt.utils.Constants.ANDROID_HANDLE_URLS_PERMISSION;
import static org.wso2.carbon.identity.trusted.app.mgt.utils.Constants.IOS_CREDENTIAL_PERMISSION;

/**
 * Testing the TrustedAppMgtServiceImpl class.
 */
public class TrustedAppMgtServiceImplTest {

    private TrustedAppMgtService trustedAppMgtService;
    private ApplicationManagementService applicationManagementService;
    private MockedStatic<TrustedAppMgtDataHolder> trustedAppMgtDataHolder;

    private static final String ANDROID_PACKAGE_NAME = "com.wso2.sample";
    private static final String APPLE_APP_ID = "APPLETEAMID.com.wso2.sample";

    @BeforeMethod
    public void setup() {

        trustedAppMgtService = new TrustedAppMgtServiceImpl();
        TrustedAppMgtDataHolder mockTrustedAppMgtDataHolder = mock(TrustedAppMgtDataHolder.class);
        trustedAppMgtDataHolder = mockStatic(TrustedAppMgtDataHolder.class);
        applicationManagementService = mock(ApplicationManagementService.class);
        trustedAppMgtDataHolder.when(TrustedAppMgtDataHolder::getInstance)
                .thenReturn(mockTrustedAppMgtDataHolder);
        when(mockTrustedAppMgtDataHolder.getApplicationManagementService())
                .thenReturn(applicationManagementService);
    }

    @AfterMethod
    public void tearDown() {

        trustedAppMgtDataHolder.close();
    }

    @DataProvider(name = "getTrustedAndroidAppsDataProvider")
    public Object[][] getTrustedAndroidAppsDataProvider() {

        String[] thumbprints1 = {"thumbprint1"};
        String[] thumbprints2 = {"thumbprint1", "thumbprint2"};

        Set<String> permissions1 = new HashSet<>();
        permissions1.add(ANDROID_CREDENTIAL_PERMISSION);
        permissions1.add(ANDROID_HANDLE_URLS_PERMISSION);

        return new Object[][]{
                {thumbprints1, true, 1, thumbprints1, permissions1},
                // Trusted app with multiple thumbprints.
                {thumbprints2, true, 1, thumbprints2, permissions1},
                // Trusted app without any thumbprints.
                {new String[0], true, 0, null, null},
                // When fido trusted app feature is disabled for the app.
                {thumbprints1, false, 0, null, null}
        };
    }

    @Test(dataProvider = "getTrustedAndroidAppsDataProvider")
    public void testGetTrustedAndroidApps(String[] providedThumbprints, boolean isFidoTrusted,
                                          int trustedAppsCount, String[] expectedThumbprints,
                                          Set<String> expectedPermissions) throws Exception {

        List<TrustedApp> trustedApps = new ArrayList<>();
        TrustedApp trustedApp = new TrustedApp();
        trustedApp.setPlatformType(PlatformType.ANDROID);
        trustedApp.setAppIdentifier(ANDROID_PACKAGE_NAME);
        trustedApp.setThumbprints(providedThumbprints);
        trustedApp.setIsFIDOTrusted(isFidoTrusted);
        trustedApps.add(trustedApp);

        when(applicationManagementService.getTrustedApps(PlatformType.ANDROID)).thenReturn(trustedApps);

        List<TrustedAndroidApp> trustedAndroidApps = trustedAppMgtService.getTrustedAndroidApps();

        Assert.assertEquals(trustedAndroidApps.size(), trustedAppsCount, "Trusted apps count mismatch.");
        if (trustedAppsCount > 0) {
            Assert.assertEquals(trustedAndroidApps.get(0).getThumbprints(), expectedThumbprints,
                    "Incorrect thumbprints resolved for the trusted app.");
            Assert.assertEquals(trustedAndroidApps.get(0).getPermissions(), expectedPermissions,
                    "Incorrect permissions resolved for the trusted app.");
        }
    }

    @DataProvider(name = "getTrustedIosAppsDataProvider")
    public Object[][] getTrustedIosAppsDataProvider() {

        Set<String> permissions = new HashSet<>();
        permissions.add(IOS_CREDENTIAL_PERMISSION);

        return new Object[][]{
                {true, 1, permissions},
                {false, 0, null}
        };
    }

    @Test(dataProvider = "getTrustedIosAppsDataProvider")
    public void testGetTrustedIosApps(boolean isFIDOTrusted, int trustedAppCount, Set<String> expectedPermissions)
            throws Exception {

        List<TrustedApp> trustedApps = new ArrayList<>();
        TrustedApp trustedApp = new TrustedApp();
        trustedApp.setPlatformType(PlatformType.IOS);
        trustedApp.setAppIdentifier(APPLE_APP_ID);
        trustedApp.setThumbprints(new String[0]);
        trustedApp.setIsFIDOTrusted(isFIDOTrusted);
        trustedApps.add(trustedApp);

        when(applicationManagementService.getTrustedApps(PlatformType.IOS)).thenReturn(trustedApps);

        List<TrustedIosApp> trustedIosApps = trustedAppMgtService.getTrustedIosApps();

        Assert.assertEquals(trustedIosApps.size(), trustedAppCount, "Trusted apps count mismatch.");
        if (trustedAppCount > 0) {
            Assert.assertEquals(trustedIosApps.get(0).getPermissions(), expectedPermissions,
                    "Incorrect permissions resolved for the trusted app.");
        }
    }
}
