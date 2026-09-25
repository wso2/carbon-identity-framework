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

package org.wso2.carbon.identity.provisioning;

import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.application.common.model.ServiceProvider;
import org.wso2.carbon.identity.application.mgt.ApplicationConstants;
import org.wso2.carbon.identity.application.mgt.ApplicationManagementService;
import org.wso2.carbon.identity.central.log.mgt.utils.LoggerUtils;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;
import org.wso2.carbon.identity.core.util.IdentityUtil;

import java.util.HashMap;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests outbound provisioning triggered by a request that is authenticated with a token of an
 * application registered in another tenant, such as a system application. The application does not
 * resolve in the tenant the user store operation runs in.
 */
@Listeners(MockitoTestNGListener.class)
public class CrossTenantApplicationProvisioningTest {

    private static final String TENANT_DOMAIN = "wso2.com";
    private static final int TENANT_ID = 1;
    private static final String CROSS_TENANT_APP_NAME = "System App Grant Client";
    private static final String PROVISIONED_USER_NAME = "PRIMARY/John";

    @Mock
    private ApplicationManagementService applicationManagementService;

    @BeforeClass
    public void setUpClass() {

        if (System.getProperty("carbon.home") == null) {
            System.setProperty("carbon.home", ".");
        }
    }

    @BeforeMethod
    public void setUp() {

        MockitoAnnotations.openMocks(this);
    }

    /**
     * Application based outbound provisioning is disabled (the default), so the resident application
     * configurations of the operating tenant are used and the operation does not fail.
     */
    @Test
    public void testProvisionFallsBackToResidentApplicationWhenApplicationIsNotInTheTenant() throws Exception {

        ServiceProvider residentServiceProvider = new ServiceProvider();
        residentServiceProvider.setApplicationName(ApplicationConstants.LOCAL_SP);

        try (MockedStatic<IdentityTenantUtil> identityTenantUtil = Mockito.mockStatic(IdentityTenantUtil.class);
             MockedStatic<ApplicationManagementService> appMgtService =
                     Mockito.mockStatic(ApplicationManagementService.class);
             MockedStatic<IdentityUtil> identityUtil = Mockito.mockStatic(IdentityUtil.class);
             MockedStatic<LoggerUtils> loggerUtils = Mockito.mockStatic(LoggerUtils.class)) {

            identityTenantUtil.when(() -> IdentityTenantUtil.getTenantId(TENANT_DOMAIN)).thenReturn(TENANT_ID);
            loggerUtils.when(LoggerUtils::isDiagnosticLogsEnabled).thenReturn(false);
            appMgtService.when(ApplicationManagementService::getInstance).thenReturn(applicationManagementService);
            when(applicationManagementService.getServiceProvider(eq(CROSS_TENANT_APP_NAME), eq(TENANT_DOMAIN)))
                    .thenReturn(null);
            when(applicationManagementService.getServiceProvider(eq(ApplicationConstants.LOCAL_SP),
                    eq(TENANT_DOMAIN))).thenReturn(residentServiceProvider);

            // Must not throw: the operation is not an outbound provisioning operation to begin with.
            OutboundProvisioningManager.getInstance().provision(userProvisioningEntity(), CROSS_TENANT_APP_NAME,
                    null, TENANT_DOMAIN, false);

            verify(applicationManagementService).getServiceProvider(eq(ApplicationConstants.LOCAL_SP),
                    eq(TENANT_DOMAIN));
        }
    }

    /**
     * Application based outbound provisioning is enabled, so there is nothing to fall back to and
     * provisioning is skipped without failing the operation.
     */
    @Test
    public void testProvisionIsSkippedWhenApplicationBasedOutboundProvisioningIsEnabled() throws Exception {

        try (MockedStatic<ApplicationManagementService> appMgtService =
                     Mockito.mockStatic(ApplicationManagementService.class);
             MockedStatic<IdentityUtil> identityUtil = Mockito.mockStatic(IdentityUtil.class)) {

            appMgtService.when(ApplicationManagementService::getInstance).thenReturn(applicationManagementService);
            identityUtil.when(() -> IdentityUtil.getProperty(
                            eq(IdentityProvisioningConstants.APPLICATION_BASED_OUTBOUND_PROVISIONING_ENABLED)))
                    .thenReturn("true");
            when(applicationManagementService.getServiceProvider(eq(CROSS_TENANT_APP_NAME), eq(TENANT_DOMAIN)))
                    .thenReturn(null);

            OutboundProvisioningManager.getInstance().provision(userProvisioningEntity(), CROSS_TENANT_APP_NAME,
                    null, TENANT_DOMAIN, false);

            verify(applicationManagementService, Mockito.never())
                    .getServiceProvider(eq(ApplicationConstants.LOCAL_SP), anyString());
        }
    }

    private ProvisioningEntity userProvisioningEntity() {

        return new ProvisioningEntity(ProvisioningEntityType.USER, PROVISIONED_USER_NAME,
                ProvisioningOperation.POST, new HashMap<>());
    }
}
