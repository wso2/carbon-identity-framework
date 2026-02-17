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

package org.wso2.carbon.identity.application.mgt;

import org.mockito.MockedStatic;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.application.common.IdentityApplicationManagementException;
import org.wso2.carbon.identity.application.common.model.ServiceProvider;
import org.wso2.carbon.identity.application.common.model.ServiceProviderProperty;
import org.wso2.carbon.identity.application.mgt.listener.DefaultRoleManagementListener;
import org.wso2.carbon.identity.core.util.IdentityUtil;
import org.wso2.carbon.identity.role.v2.mgt.core.exception.IdentityRoleManagementClientException;
import org.wso2.carbon.identity.role.v2.mgt.core.exception.IdentityRoleManagementServerException;
import org.wso2.carbon.identity.role.v2.mgt.core.util.RoleManagementUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

/**
 * Contains the unit tests for the default role management listener.
 */
public class DefaultRoleManagementListenerTest {

    private DefaultRoleManagementListener defaultRoleManagementListener;
    private static final String ROLE_NAME = "test_role";
    private static final String APPLICATION_NAME = "app_name";
    private static final String APPLICATION_RES_ID = "app_id";
    private static final String IS_FRAGMENT_APP = "isFragmentApp";
    private static final String APPLICATION_AUD = "APPLICATION";
    private static final String ORGANIZATION_AUD = "ORGANIZATION";
    private static final String TENANT_DOMAIN = "wso2.com";

    @BeforeClass
    public void setUp() {

        defaultRoleManagementListener = spy(new DefaultRoleManagementListener());
    }

    @DataProvider(name = "fragmentAppPropertyProvider")
    public Object[][] fragmentAppPropertyProvider() {

        // Creating main application object.
        ServiceProvider mainApplication = createServiceProvider();
        mainApplication.setSpProperties(null);

        // Creating shared application object.
        ServiceProvider fragmentApplication = createServiceProvider();
        ServiceProviderProperty isFragmentAppSpProp = buildServiceProviderProperty(IS_FRAGMENT_APP,
                Boolean.TRUE.toString());
        fragmentApplication.setSpProperties(new ServiceProviderProperty[]{isFragmentAppSpProp});

        return new Object[][] {
                {false, mainApplication},
                {true, fragmentApplication}
        };
    }

    @Test(priority = 1, dataProvider = "fragmentAppPropertyProvider")
    public void testPreAddRoleForFragmentApp(boolean isFragmentApp, ServiceProvider application) throws Exception {

        try (MockedStatic<ApplicationManagementService> applicationManagementServiceMockedStatic =
                     mockStatic(ApplicationManagementService.class)) {

            ApplicationManagementService applicationManagementService = mock(ApplicationManagementService.class);
            applicationManagementServiceMockedStatic.when(ApplicationManagementService::getInstance).
                    thenReturn(applicationManagementService);
            when(applicationManagementService.getApplicationByResourceId(anyString(), anyString())).
                    thenReturn(application);
            when(applicationManagementService.getAllowedAudienceForRoleAssociation(anyString(), anyString())).
                    thenReturn(APPLICATION_AUD);

            // Calling the preAddRole method.
            defaultRoleManagementListener.preAddRole(ROLE_NAME, new ArrayList<>(), new ArrayList<>(),
                    new ArrayList<>(), APPLICATION_AUD, APPLICATION_RES_ID, TENANT_DOMAIN);

            Map<String, Object> threadLocalProps = IdentityUtil.threadLocalProperties.get();
            if (!isFragmentApp) {
                assertFalse(threadLocalProps.containsKey(IS_FRAGMENT_APP));
            } else {
                // If the application is a fragment app, then we add the IS_FRAGMENT_APP property to the thread local.
                assertTrue(threadLocalProps.containsKey(IS_FRAGMENT_APP));
            }
            // Clearing the thread local properties.
            IdentityUtil.threadLocalProperties.set(new HashMap<>());
        }
    }

    @Test(priority = 2, dataProvider = "fragmentAppPropertyProvider")
    public void testPreAddRoleWhenPreviousFragmentPropertyNotCleared(boolean isFragmentApp,
                                                                     ServiceProvider application) throws Exception {

        try (MockedStatic<ApplicationManagementService> applicationManagementServiceMockedStatic =
                     mockStatic(ApplicationManagementService.class)) {

            ApplicationManagementService applicationManagementService = mock(ApplicationManagementService.class);
            applicationManagementServiceMockedStatic.when(ApplicationManagementService::getInstance).
                    thenReturn(applicationManagementService);
            when(applicationManagementService.getApplicationByResourceId(anyString(), anyString())).
                    thenReturn(application);
            when(applicationManagementService.getAllowedAudienceForRoleAssociation(anyString(), anyString())).
                    thenReturn(APPLICATION_AUD);

            // Mimicking the scenario where the previous fragment property is not cleared in the thread local.
            Map<String, Object> threadLocalProps = new HashMap<>();
            threadLocalProps.put(IS_FRAGMENT_APP, Boolean.TRUE);
            IdentityUtil.threadLocalProperties.set(threadLocalProps);

            // Calling the preAddRole method.
            defaultRoleManagementListener.preAddRole(ROLE_NAME, new ArrayList<>(), new ArrayList<>(),
                    new ArrayList<>(), APPLICATION_AUD, APPLICATION_RES_ID, TENANT_DOMAIN);

            if (!isFragmentApp) {
                assertFalse(threadLocalProps.containsKey(IS_FRAGMENT_APP));
            } else {
                // If the application is a fragment app, then we add the IS_FRAGMENT_APP property to the thread local.
                assertTrue(threadLocalProps.containsKey(IS_FRAGMENT_APP));
            }
            // Clearing the thread local properties.
            IdentityUtil.threadLocalProperties.set(new HashMap<>());
        }
    }

    @Test(priority = 3)
    public void testPreAddRoleWithOtherSPProperties() throws Exception {

        try (MockedStatic<ApplicationManagementService> applicationManagementServiceMockedStatic =
                     mockStatic(ApplicationManagementService.class)) {

            // Creating service provider object.
            ServiceProvider serviceProvider = createServiceProvider();

            // Creating service provider property object.
            ServiceProviderProperty isFragmentAppSpProp = buildServiceProviderProperty("test-prop-name",
                    "test-prop-value");
            serviceProvider.setSpProperties(new ServiceProviderProperty[]{isFragmentAppSpProp});

            ApplicationManagementService applicationManagementService = mock(ApplicationManagementService.class);
            applicationManagementServiceMockedStatic.when(ApplicationManagementService::getInstance).
                    thenReturn(applicationManagementService);
            when(applicationManagementService.getApplicationByResourceId(anyString(), anyString())).
                    thenReturn(serviceProvider);
            when(applicationManagementService.getAllowedAudienceForRoleAssociation(anyString(), anyString())).
                    thenReturn(APPLICATION_AUD);

            // Calling the preAddRole method.
            defaultRoleManagementListener.preAddRole(ROLE_NAME, new ArrayList<>(), new ArrayList<>(),
                    new ArrayList<>(), APPLICATION_AUD, APPLICATION_RES_ID, TENANT_DOMAIN);

            assertFalse(IdentityUtil.threadLocalProperties.get().containsKey(IS_FRAGMENT_APP));
        }
    }

    @Test(priority = 4)
    public void testPreAddRoleForOrgAudience() throws Exception {

        defaultRoleManagementListener.preAddRole(ROLE_NAME, new ArrayList<>(), new ArrayList<>(),
                new ArrayList<>(), ORGANIZATION_AUD, "org-id", TENANT_DOMAIN);
        // If the audience is not application, then in the preAddRole method, it will return without going to the
        // other methods. So the thread local properties will not be set.
        Map<String, Object> threadLocalProps = IdentityUtil.threadLocalProperties.get();
        assertFalse(threadLocalProps.containsKey(IS_FRAGMENT_APP));
    }

    @Test(priority = 5, expectedExceptions = {IdentityRoleManagementClientException.class},
            expectedExceptionsMessageRegExp = "Invalid audience. No application found with application id: " +
                    APPLICATION_RES_ID + " and tenant domain : " + TENANT_DOMAIN)
    public void testPreAddRoleWithNullApplication() throws Exception {

        try (MockedStatic<ApplicationManagementService> applicationManagementServiceMockedStatic =
                     mockStatic(ApplicationManagementService.class)) {

            ApplicationManagementService applicationManagementService = mock(ApplicationManagementService.class);
            applicationManagementServiceMockedStatic.when(ApplicationManagementService::getInstance).
                    thenReturn(applicationManagementService);
            when(applicationManagementService.getApplicationByResourceId(anyString(), anyString())).
                    thenReturn(null);
            // Calling the preAddRole method.
            defaultRoleManagementListener.preAddRole(ROLE_NAME, new ArrayList<>(), new ArrayList<>(),
                    new ArrayList<>(), APPLICATION_AUD, APPLICATION_RES_ID, TENANT_DOMAIN);
        }
    }

    @Test(priority = 6, expectedExceptions = {IdentityRoleManagementClientException.class},
            expectedExceptionsMessageRegExp = "Application: " + APPLICATION_RES_ID + " does not have Application " +
                    "role audience type")
    public void testPreAddRoleWithWrongAudience() throws Exception {

        try (MockedStatic<ApplicationManagementService> applicationManagementServiceMockedStatic =
                     mockStatic(ApplicationManagementService.class)) {

            // Creating service provider object.
            ServiceProvider serviceProvider = createServiceProvider();

            ApplicationManagementService applicationManagementService = mock(ApplicationManagementService.class);
            applicationManagementServiceMockedStatic.when(ApplicationManagementService::getInstance).
                    thenReturn(applicationManagementService);
            when(applicationManagementService.getApplicationByResourceId(anyString(), anyString())).
                    thenReturn(serviceProvider);
            when(applicationManagementService.getAllowedAudienceForRoleAssociation(anyString(), anyString())).
                    thenReturn(ORGANIZATION_AUD);
            // Calling the preAddRole method.
            defaultRoleManagementListener.preAddRole(ROLE_NAME, new ArrayList<>(), new ArrayList<>(),
                    new ArrayList<>(), APPLICATION_AUD, APPLICATION_RES_ID, TENANT_DOMAIN);
        }
    }

    @Test(priority = 7, expectedExceptions = {IdentityRoleManagementServerException.class},
            expectedExceptionsMessageRegExp = "Error while retrieving the application for the given id: " +
                    APPLICATION_RES_ID)
    public void testPreAddRoleWithApplicationRetrievingException() throws Exception {

        try (MockedStatic<ApplicationManagementService> applicationManagementServiceMockedStatic =
                     mockStatic(ApplicationManagementService.class)) {

            ApplicationManagementService applicationManagementService = mock(ApplicationManagementService.class);
            applicationManagementServiceMockedStatic.when(ApplicationManagementService::getInstance).
                    thenReturn(applicationManagementService);
            when(applicationManagementService.getApplicationByResourceId(anyString(), anyString())).
                    thenThrow(IdentityApplicationManagementException.class);

            // Calling the preAddRole method.
            defaultRoleManagementListener.preAddRole(ROLE_NAME, new ArrayList<>(), new ArrayList<>(),
                    new ArrayList<>(), APPLICATION_AUD, APPLICATION_RES_ID, TENANT_DOMAIN);
        }
    }

    private static ServiceProviderProperty buildServiceProviderProperty(String name, String value) {

        ServiceProviderProperty isFragmentAppSpProp = new ServiceProviderProperty();
        isFragmentAppSpProp.setName(name);
        isFragmentAppSpProp.setValue(value);
        return isFragmentAppSpProp;
    }

    private static ServiceProvider createServiceProvider() {

        ServiceProvider serviceProvider = new ServiceProvider();
        serviceProvider.setApplicationName(APPLICATION_NAME);
        serviceProvider.setApplicationResourceId(APPLICATION_RES_ID);
        return serviceProvider;
    }

    @Test(priority = 8)
    public void testDoPostUpdateApplication() throws Exception {

        try (MockedStatic<RoleManagementUtils> roleManagementUtilsMockedStatic =
                     mockStatic(RoleManagementUtils.class)) {
            ServiceProvider serviceProvider = createServiceProvider();
            defaultRoleManagementListener.doPostUpdateApplication(serviceProvider, TENANT_DOMAIN, "admin");
            roleManagementUtilsMockedStatic.verify(() ->
                    RoleManagementUtils.clearRoleBasicInfoCacheByTenant(TENANT_DOMAIN),
                    times(1));
        }
    }
}
