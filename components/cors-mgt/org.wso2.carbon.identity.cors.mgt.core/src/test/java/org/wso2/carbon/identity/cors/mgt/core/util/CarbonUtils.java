/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.identity.cors.mgt.core.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.base.CarbonBaseConstants;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.identity.application.authentication.framework.internal.FrameworkServiceDataHolder;
import org.wso2.carbon.identity.application.common.IdentityApplicationManagementException;
import org.wso2.carbon.identity.application.common.model.ApplicationBasicInfo;
import org.wso2.carbon.identity.application.mgt.ApplicationManagementService;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;
import org.wso2.carbon.identity.cors.mgt.core.constant.TestConstants.SampleApp1;
import org.wso2.carbon.identity.cors.mgt.core.constant.TestConstants.SampleApp2;
import org.wso2.carbon.identity.cors.mgt.core.constant.TestConstants.SampleTenant;
import org.wso2.carbon.identity.cors.mgt.core.internal.impl.CORSManagementServiceImpl;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.user.core.tenant.TenantManager;

import java.nio.file.Paths;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.wso2.carbon.base.MultitenantConstants.SUPER_TENANT_DOMAIN_NAME;
import static org.wso2.carbon.base.MultitenantConstants.SUPER_TENANT_ID;

/**
 * Utility class for Carbon functions.
 */
public class CarbonUtils {

    private static final Log log = LogFactory.getLog(CORSManagementServiceImpl.class);

    /**
     * Private constructor of CarbonUtils.
     */
    private CarbonUtils() {

    }

    public static void setCarbonHome() {

        String carbonHome = Paths.get(System.getProperty("user.dir"), "target", "test-classes").toString();
        System.setProperty(CarbonBaseConstants.CARBON_HOME, carbonHome);
        System.setProperty(CarbonBaseConstants.CARBON_CONFIG_DIR_PATH, Paths.get(carbonHome, "conf").toString());
    }

    public static void mockCarbonContextForTenant(int tenantId, String tenantDomain) {

        mockStatic(PrivilegedCarbonContext.class);
        PrivilegedCarbonContext privilegedCarbonContext = mock(PrivilegedCarbonContext.class);

        when(PrivilegedCarbonContext.getThreadLocalCarbonContext()).thenReturn(privilegedCarbonContext);
        when(privilegedCarbonContext.getTenantDomain()).thenReturn(tenantDomain);
        when(privilegedCarbonContext.getTenantId()).thenReturn(tenantId);
        when(privilegedCarbonContext.getUsername()).thenReturn("admin");
    }

    public static void mockIdentityTenantUtility() {

        mockStatic(IdentityTenantUtil.class);
        IdentityTenantUtil identityTenantUtil = mock(IdentityTenantUtil.class);
        when(IdentityTenantUtil.getTenantDomain(SUPER_TENANT_ID)).thenReturn(SUPER_TENANT_DOMAIN_NAME);
        when(IdentityTenantUtil.getTenantDomain(SampleTenant.ID)).thenReturn(SampleTenant.DOMAIN_NAME);
        when(IdentityTenantUtil.getTenantId(SUPER_TENANT_DOMAIN_NAME)).thenReturn(SUPER_TENANT_ID);
        when(IdentityTenantUtil.getTenantId(SampleTenant.DOMAIN_NAME)).thenReturn(SampleTenant.ID);
    }

    public static void mockApplicationManagementService() {

        ApplicationManagementService applicationManagementService = mock(ApplicationManagementService.class);

        mockStatic(ApplicationManagementService.class);
        when(ApplicationManagementService.getInstance()).thenReturn(applicationManagementService);
        try {
            ApplicationBasicInfo applicationBasicInfo1 = new ApplicationBasicInfo();
            applicationBasicInfo1.setApplicationId(SampleApp1.ID);
            when(applicationManagementService.getApplicationBasicInfoByResourceId(eq(SampleApp1.UUID),
                    any(String.class))).thenReturn(applicationBasicInfo1);
            ApplicationBasicInfo applicationBasicInfo2 = new ApplicationBasicInfo();
            applicationBasicInfo2.setApplicationId(SampleApp2.ID);
            when(applicationManagementService.getApplicationBasicInfoByResourceId(eq(SampleApp2.UUID),
                    any(String.class))).thenReturn(applicationBasicInfo2);
        } catch (IdentityApplicationManagementException e) {
            if (log.isDebugEnabled()) {
                log.debug(e);
            }
        }
    }

    public static void mockRealmService() {

        RealmService mockRealmService = mock(RealmService.class);
        FrameworkServiceDataHolder.getInstance().setRealmService(mockRealmService);

        TenantManager tenantManager = mock(TenantManager.class);
        when(mockRealmService.getTenantManager()).thenReturn(tenantManager);
    }
}
