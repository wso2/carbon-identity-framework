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

package org.wso2.carbon.identity.cors.mgt.core;

import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.testng.PowerMockTestCase;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.wso2.carbon.base.CarbonBaseConstants;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.identity.application.authentication.framework.internal.FrameworkServiceDataHolder;
import org.wso2.carbon.identity.application.authentication.framework.util.FrameworkUtils;
import org.wso2.carbon.identity.configuration.mgt.core.ConfigurationManager;
import org.wso2.carbon.identity.configuration.mgt.core.ConfigurationManagerImpl;
import org.wso2.carbon.identity.configuration.mgt.core.dao.ConfigurationDAO;
import org.wso2.carbon.identity.configuration.mgt.core.dao.impl.ConfigurationDAOImpl;
import org.wso2.carbon.identity.configuration.mgt.core.exception.ConfigurationManagementException;
import org.wso2.carbon.identity.configuration.mgt.core.internal.ConfigurationManagerComponentDataHolder;
import org.wso2.carbon.identity.configuration.mgt.core.model.ConfigurationManagerConfigurationHolder;
import org.wso2.carbon.identity.core.util.IdentityDatabaseUtil;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;
import org.wso2.carbon.identity.core.util.IdentityUtil;
import org.wso2.carbon.identity.cors.mgt.core.exception.CORSManagementServiceException;
import org.wso2.carbon.identity.cors.mgt.core.helper.CORSServiceTestHelper;
import org.wso2.carbon.identity.cors.mgt.core.internal.CORSManagementServiceHolder;
import org.wso2.carbon.identity.cors.mgt.core.internal.impl.CORSManagementServiceImpl;
import org.wso2.carbon.identity.cors.mgt.core.model.CORSOrigin;
import org.wso2.carbon.identity.cors.mgt.core.util.TestUtils;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.user.core.tenant.TenantManager;

import java.nio.file.Paths;
import java.sql.Connection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.sql.DataSource;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.wso2.carbon.base.MultitenantConstants.SUPER_TENANT_DOMAIN_NAME;
import static org.wso2.carbon.base.MultitenantConstants.SUPER_TENANT_ID;
import static org.wso2.carbon.identity.cors.mgt.core.constant.TestConstants.SAMPLE_ORIGIN_LIST_1;
import static org.wso2.carbon.identity.cors.mgt.core.constant.TestConstants.SAMPLE_ORIGIN_LIST_2;
import static org.wso2.carbon.identity.cors.mgt.core.constant.TestConstants.TENANT_DOMAIN_NAME;
import static org.wso2.carbon.identity.cors.mgt.core.constant.TestConstants.TENANT_ID;
import static org.wso2.carbon.identity.cors.mgt.core.helper.CORSServiceTestHelper.getSampleResourceAdd;
import static org.wso2.carbon.identity.cors.mgt.core.helper.CORSServiceTestHelper.mockCarbonContextForTenant;
import static org.wso2.carbon.identity.cors.mgt.core.helper.CORSServiceTestHelper.mockIdentityTenantUtility;
import static org.wso2.carbon.identity.cors.mgt.core.internal.Constants.CORS_ORIGIN_RESOURCE_NAME;
import static org.wso2.carbon.identity.cors.mgt.core.internal.Constants.CORS_ORIGIN_RESOURCE_TYPE_NAME;
import static org.wso2.carbon.identity.cors.mgt.core.util.TestUtils.closeH2Base;
import static org.wso2.carbon.identity.cors.mgt.core.util.TestUtils.initiateH2Base;
import static org.wso2.carbon.identity.cors.mgt.core.util.TestUtils.spyConnection;

/**
 * Unit test cases for CORSService.
 */
@PrepareForTest({PrivilegedCarbonContext.class, IdentityDatabaseUtil.class, IdentityUtil.class,
        IdentityTenantUtil.class, FrameworkUtils.class})
public class CORSManagementServiceTest extends PowerMockTestCase {

    private ConfigurationManager configurationManager;
    private Connection connection;

    private CORSManagementService corsManagementService;

    @BeforeMethod
    public void setUp() throws Exception {

        initiateH2Base();
        String carbonHome = Paths.get(System.getProperty("user.dir"), "target", "test-classes").toString();
        System.setProperty(CarbonBaseConstants.CARBON_HOME, carbonHome);
        System.setProperty(CarbonBaseConstants.CARBON_CONFIG_DIR_PATH, Paths.get(carbonHome, "conf").toString());

        DataSource dataSource = mock(DataSource.class);
        mockStatic(IdentityDatabaseUtil.class);
        when(IdentityDatabaseUtil.getDataSource()).thenReturn(dataSource);

        connection = TestUtils.getConnection();
        Connection spyConnection = spyConnection(connection);
        when(dataSource.getConnection()).thenReturn(spyConnection);

        ConfigurationManagerComponentDataHolder.setUseCreatedTime(true);
        ConfigurationManagerConfigurationHolder configurationHolder = new ConfigurationManagerConfigurationHolder();
        ConfigurationDAO configurationDAO = new ConfigurationDAOImpl();
        configurationHolder.setConfigurationDAOS(Collections.singletonList(configurationDAO));
        mockCarbonContextForTenant(SUPER_TENANT_ID, SUPER_TENANT_DOMAIN_NAME);
        mockIdentityTenantUtility();
        configurationManager = new ConfigurationManagerImpl(configurationHolder);
        ConfigurationManagerComponentDataHolder.getInstance().setConfigurationManagementEnabled(true);

        RealmService mockRealmService = mock(RealmService.class);
        TenantManager tenantManager = mock(TenantManager.class);
        when(mockRealmService.getTenantManager()).thenReturn(tenantManager);
        FrameworkServiceDataHolder.getInstance().setRealmService(mockRealmService);

        corsManagementService = new CORSManagementServiceImpl();
        CORSManagementServiceHolder.getInstance().setConfigurationManager(configurationManager);
    }

    @AfterMethod
    public void tearDown() throws Exception {

        connection.close();
        closeH2Base();
    }

    @Test(priority = 0)
    public void testGetCORSOriginsWithSuperTenant() throws ConfigurationManagementException,
            CORSManagementServiceException {

        configurationManager.addResource(CORS_ORIGIN_RESOURCE_TYPE_NAME, getSampleResourceAdd(SAMPLE_ORIGIN_LIST_1));
        List<CORSOrigin> corsOrigins = corsManagementService.getCORSOrigins(SUPER_TENANT_DOMAIN_NAME);

        assertEquals(SAMPLE_ORIGIN_LIST_1, corsOrigins);
    }

    @Test(priority = 1)
    public void testGetCORSOrigins() throws ConfigurationManagementException, CORSManagementServiceException {

        mockCarbonContextForTenant(TENANT_ID, TENANT_DOMAIN_NAME);
        configurationManager.addResource(CORS_ORIGIN_RESOURCE_TYPE_NAME, getSampleResourceAdd(SAMPLE_ORIGIN_LIST_1));
        List<CORSOrigin> corsOrigins = corsManagementService.getCORSOrigins(TENANT_DOMAIN_NAME);

        assertEquals(SAMPLE_ORIGIN_LIST_1, corsOrigins);
    }

    @Test(priority = 2)
    public void testSetCORSOrigins() throws CORSManagementServiceException, ConfigurationManagementException {

        corsManagementService.setCORSOrigins(SUPER_TENANT_DOMAIN_NAME, SAMPLE_ORIGIN_LIST_1);
        List<CORSOrigin> corsOrigins = configurationManager.getResource(CORS_ORIGIN_RESOURCE_TYPE_NAME,
                CORS_ORIGIN_RESOURCE_NAME)
                .getAttributes()
                .stream()
                .map(CORSServiceTestHelper::attributeToCORSOrigin)
                .collect(Collectors.toList());

        assertEquals(SAMPLE_ORIGIN_LIST_1, corsOrigins);
    }

    @Test(priority = 3)
    public void testAddCORSOrigins() throws ConfigurationManagementException, CORSManagementServiceException {

        corsManagementService.setCORSOrigins(SUPER_TENANT_DOMAIN_NAME, SAMPLE_ORIGIN_LIST_1);
        corsManagementService.addCORSOrigins(SUPER_TENANT_DOMAIN_NAME, SAMPLE_ORIGIN_LIST_2);
        List<CORSOrigin> corsOrigins = configurationManager.getResource(CORS_ORIGIN_RESOURCE_TYPE_NAME,
                CORS_ORIGIN_RESOURCE_NAME)
                .getAttributes()
                .stream()
                .map(CORSServiceTestHelper::attributeToCORSOrigin)
                .collect(Collectors.toList());

        assertEquals(Stream.concat(SAMPLE_ORIGIN_LIST_1.stream(),
                SAMPLE_ORIGIN_LIST_2.stream()).collect(Collectors.toList()), corsOrigins);
    }

    @Test(priority = 4)
    public void testDeleteCORSOrigins() throws CORSManagementServiceException, ConfigurationManagementException {

        corsManagementService.setCORSOrigins(SUPER_TENANT_DOMAIN_NAME, SAMPLE_ORIGIN_LIST_1);
        corsManagementService.deleteCORSOrigins(SUPER_TENANT_DOMAIN_NAME, SAMPLE_ORIGIN_LIST_1.subList(0, 2));
        List<CORSOrigin> corsOrigins = configurationManager.getResource(CORS_ORIGIN_RESOURCE_TYPE_NAME,
                CORS_ORIGIN_RESOURCE_NAME)
                .getAttributes()
                .stream()
                .map(CORSServiceTestHelper::attributeToCORSOrigin)
                .collect(Collectors.toList());

        assertEquals(SAMPLE_ORIGIN_LIST_1.subList(2, SAMPLE_ORIGIN_LIST_1.size()), corsOrigins);
    }
}
