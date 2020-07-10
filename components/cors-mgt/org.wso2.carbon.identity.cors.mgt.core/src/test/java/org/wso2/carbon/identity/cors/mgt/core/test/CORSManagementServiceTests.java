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

package org.wso2.carbon.identity.cors.mgt.core.test;

import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.testng.PowerMockTestCase;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.identity.application.mgt.ApplicationManagementService;
import org.wso2.carbon.identity.configuration.mgt.core.ConfigurationManager;
import org.wso2.carbon.identity.configuration.mgt.core.exception.ConfigurationManagementException;
import org.wso2.carbon.identity.configuration.mgt.core.model.Attribute;
import org.wso2.carbon.identity.configuration.mgt.core.model.Resource;
import org.wso2.carbon.identity.core.util.IdentityDatabaseUtil;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;
import org.wso2.carbon.identity.core.util.IdentityUtil;
import org.wso2.carbon.identity.cors.mgt.core.CORSManagementService;
import org.wso2.carbon.identity.cors.mgt.core.exception.CORSManagementServiceClientException;
import org.wso2.carbon.identity.cors.mgt.core.exception.CORSManagementServiceException;
import org.wso2.carbon.identity.cors.mgt.core.internal.CORSManagementServiceHolder;
import org.wso2.carbon.identity.cors.mgt.core.internal.impl.CORSManagementServiceImpl;
import org.wso2.carbon.identity.cors.mgt.core.model.CORSOrigin;
import org.wso2.carbon.identity.cors.mgt.core.util.CarbonUtils;
import org.wso2.carbon.identity.cors.mgt.core.util.ConfigurationManagementUtils;
import org.wso2.carbon.identity.cors.mgt.core.util.DatabaseUtils;

import java.sql.Connection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertThrows;
import static org.testng.Assert.assertTrue;
import static org.wso2.carbon.base.MultitenantConstants.SUPER_TENANT_DOMAIN_NAME;
import static org.wso2.carbon.base.MultitenantConstants.SUPER_TENANT_ID;
import static org.wso2.carbon.identity.cors.mgt.core.constant.TestConstants.APP_ID_1;
import static org.wso2.carbon.identity.cors.mgt.core.constant.TestConstants.APP_ID_2;
import static org.wso2.carbon.identity.cors.mgt.core.constant.TestConstants.SAMPLE_ORIGIN_LIST_1;
import static org.wso2.carbon.identity.cors.mgt.core.constant.TestConstants.SAMPLE_ORIGIN_LIST_2;
import static org.wso2.carbon.identity.cors.mgt.core.constant.TestConstants.TENANT_DOMAIN_NAME;
import static org.wso2.carbon.identity.cors.mgt.core.constant.TestConstants.TENANT_ID;
import static org.wso2.carbon.identity.cors.mgt.core.helper.CORSManagementServiceTestHelper.getSampleResourceAdd;
import static org.wso2.carbon.identity.cors.mgt.core.internal.Constants.CORS_ORIGIN_RESOURCE_TYPE_NAME;

/**
 * Unit test cases for CORSService.
 */
@PrepareForTest({PrivilegedCarbonContext.class,
        IdentityDatabaseUtil.class,
        IdentityUtil.class,
        IdentityTenantUtil.class,
        ApplicationManagementService.class})
public class CORSManagementServiceTests extends PowerMockTestCase {

    private ConfigurationManager configurationManager;
    private Connection connection;
    private CORSManagementService corsManagementService;

    @BeforeMethod
    public void setUp() throws Exception {

        DatabaseUtils.initiateH2Base();

        CarbonUtils.setCarbonHome();
        CarbonUtils.mockCarbonContextForTenant(SUPER_TENANT_ID, SUPER_TENANT_DOMAIN_NAME);
        CarbonUtils.mockIdentityTenantUtility();
        CarbonUtils.mockRealmService();
        CarbonUtils.mockApplicationManagementService();

        connection = DatabaseUtils.createDataSource();
        configurationManager = ConfigurationManagementUtils.getConfigurationManager();

        corsManagementService = new CORSManagementServiceImpl();
        CORSManagementServiceHolder.getInstance().setConfigurationManager(configurationManager);
    }

    @AfterMethod
    public void tearDown() throws Exception {

        connection.close();
        DatabaseUtils.closeH2Base();
    }

    @Test
    public void testGetCORSOriginsWithNonExisting() throws CORSManagementServiceException {

        List<CORSOrigin> corsOrigins = corsManagementService.getCORSOrigins(SUPER_TENANT_DOMAIN_NAME);

        assertTrue(corsOrigins.isEmpty());
    }

    @Test
    public void testGetCORSOriginsWithSuperTenant() throws ConfigurationManagementException,
            CORSManagementServiceException {

        for (String sampleOrigin : SAMPLE_ORIGIN_LIST_1) {
            configurationManager.addResource(CORS_ORIGIN_RESOURCE_TYPE_NAME, getSampleResourceAdd(sampleOrigin));
        }

        List<String> retrievedOrigins = corsManagementService.getCORSOrigins(SUPER_TENANT_DOMAIN_NAME).stream()
                .map(CORSOrigin::getOrigin).collect(Collectors.toList());

        assertEquals(retrievedOrigins, SAMPLE_ORIGIN_LIST_1);
    }

    @Test
    public void testGetCORSOrigins() throws ConfigurationManagementException, CORSManagementServiceException {

        CarbonUtils.mockCarbonContextForTenant(TENANT_ID, TENANT_DOMAIN_NAME);
        for (String sampleOrigin : SAMPLE_ORIGIN_LIST_1) {
            configurationManager.addResource(CORS_ORIGIN_RESOURCE_TYPE_NAME, getSampleResourceAdd(sampleOrigin));
        }

        List<String> retrievedOrigins = corsManagementService.getCORSOrigins(TENANT_DOMAIN_NAME).stream()
                .map(CORSOrigin::getOrigin).collect(Collectors.toList());

        assertEquals(retrievedOrigins, SAMPLE_ORIGIN_LIST_1);
    }

    @Test
    public void testSetCORSOrigins() throws CORSManagementServiceException, ConfigurationManagementException {

        corsManagementService.setCORSOrigins(SUPER_TENANT_DOMAIN_NAME, APP_ID_1, SAMPLE_ORIGIN_LIST_1);

        List<String> retrievedOrigins = configurationManager.getResourcesByType(CORS_ORIGIN_RESOURCE_TYPE_NAME)
                .getResources().stream()
                .map(Resource::getResourceName).collect(Collectors.toList());

        assertEquals(retrievedOrigins, SAMPLE_ORIGIN_LIST_1);
    }

    @Test
    public void testAddCORSOrigins() throws ConfigurationManagementException, CORSManagementServiceException {

        corsManagementService.addCORSOrigins(SUPER_TENANT_DOMAIN_NAME, APP_ID_1, SAMPLE_ORIGIN_LIST_1);

        List<String> retrievedOrigins = configurationManager.getResourcesByType(CORS_ORIGIN_RESOURCE_TYPE_NAME)
                .getResources().stream()
                .map(Resource::getResourceName).collect(Collectors.toList());

        assertEquals(retrievedOrigins, SAMPLE_ORIGIN_LIST_1);
    }

    @Test
    public void testAddCORSOriginsForTenant() throws ConfigurationManagementException, CORSManagementServiceException {

        corsManagementService.setCORSOrigins(SUPER_TENANT_DOMAIN_NAME, null, SAMPLE_ORIGIN_LIST_1);
        corsManagementService.addCORSOrigins(SUPER_TENANT_DOMAIN_NAME, APP_ID_1, SAMPLE_ORIGIN_LIST_2);

        List<CORSOrigin> retrievedOrigins = configurationManager.getResourcesByType(CORS_ORIGIN_RESOURCE_TYPE_NAME)
                .getResources().stream()
                .map(resource -> {
                    CORSOrigin corsOrigin = new CORSOrigin();
                    corsOrigin.setId(resource.getResourceId());
                    corsOrigin.setOrigin(resource.getResourceName());
                    if (resource.isHasAttribute()) {
                        List<Attribute> attributes = resource.getAttributes();
                        for (Attribute attribute : attributes) {
                            corsOrigin.getAppIds().add(attribute.getKey());
                        }
                    }
                    return corsOrigin;
                })
                .collect(Collectors.toList());

        assertEquals(retrievedOrigins.stream().map(CORSOrigin::getOrigin).collect(Collectors.toList()),
                Stream.concat(SAMPLE_ORIGIN_LIST_1.stream(), SAMPLE_ORIGIN_LIST_2.stream())
                        .collect(Collectors.toList()));
        assertEquals(retrievedOrigins.stream().filter(corsOrigin -> corsOrigin.getAppIds().isEmpty())
                .map(CORSOrigin::getOrigin).collect(Collectors.toList()), SAMPLE_ORIGIN_LIST_1);
        assertEquals(retrievedOrigins.stream().filter(corsOrigin ->
                corsOrigin.getAppIds().size() == 1 && corsOrigin.getAppIds().contains(APP_ID_1))
                .map(CORSOrigin::getOrigin).collect(Collectors.toList()), SAMPLE_ORIGIN_LIST_2);
    }

    @Test
    public void testAddCORSOriginsWithInvalidApp() {

        assertThrows(CORSManagementServiceClientException.class,
                () -> corsManagementService.addCORSOrigins(SUPER_TENANT_DOMAIN_NAME, APP_ID_2, SAMPLE_ORIGIN_LIST_2));
    }

    @Test
    public void testDeleteCORSOrigins() throws CORSManagementServiceException, ConfigurationManagementException {

        for (String sampleOrigin : SAMPLE_ORIGIN_LIST_1) {
            configurationManager.addResource(CORS_ORIGIN_RESOURCE_TYPE_NAME, getSampleResourceAdd(sampleOrigin));
        }
        List<CORSOrigin> preRetrievedOrigins = configurationManager.getResourcesByType(CORS_ORIGIN_RESOURCE_TYPE_NAME)
                .getResources().stream()
                .map(resource -> {
                    CORSOrigin corsOrigin = new CORSOrigin();
                    corsOrigin.setId(resource.getResourceId());
                    corsOrigin.setOrigin(resource.getResourceName());
                    return corsOrigin;
                })
                .collect(Collectors.toList());

        corsManagementService.deleteCORSOrigins(SUPER_TENANT_DOMAIN_NAME, APP_ID_1,
                preRetrievedOrigins.subList(0, 2).stream()
                        .map(CORSOrigin::getId).collect(Collectors.toList()));
        List<String> retrievedOrigins = configurationManager.getResourcesByType(CORS_ORIGIN_RESOURCE_TYPE_NAME)
                .getResources().stream()
                .map(Resource::getResourceName)
                .collect(Collectors.toList());

        assertEquals(retrievedOrigins, SAMPLE_ORIGIN_LIST_1.subList(2, SAMPLE_ORIGIN_LIST_1.size()));
    }
}
