/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
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
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.identity.application.common.model.ClaimMapping;
import org.wso2.carbon.identity.provisioning.cache.ProvisioningEntityCache;
import org.wso2.carbon.identity.provisioning.dao.CacheBackedProvisioningMgtDAO;
import org.wso2.carbon.identity.provisioning.dao.ProvisioningManagementDAO;
import org.wso2.carbon.idp.mgt.util.IdPManagementUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.wso2.carbon.identity.provisioning.ProvisioningEntityType.GROUP;
import static org.wso2.carbon.identity.provisioning.ProvisioningEntityType.USER;
import static org.wso2.carbon.identity.provisioning.ProvisioningOperation.DELETE;
import static org.wso2.carbon.identity.provisioning.ProvisioningOperation.POST;
import static org.wso2.carbon.identity.provisioning.ProvisioningOperation.PUT;

/**
 * Test class for ProvisioningThread test cases.
 */
public class ProvisioningThreadTest {

    private ProvisioningEntity provisioningEntity;
    private String tenantDomainName = "carbon.super";
    private String connectorType = "testConnectorType";
    private String idPName = "testIdpName";

    @Mock
    private AbstractOutboundProvisioningConnector mockConnector;
    private MockedStatic<IdPManagementUtil> idPManagementUtil;

    @BeforeMethod
    public void setUp() throws Exception {

        initMocks(this);

        ProvisionedIdentifier provisionedIdentifier = new ProvisionedIdentifier();
        when(mockConnector.provision(provisioningEntity)).thenReturn(provisionedIdentifier);
        int tenantId = -1234;
        idPManagementUtil = mockStatic(IdPManagementUtil.class);
        idPManagementUtil.when(() -> IdPManagementUtil.getTenantIdOfDomain(tenantDomainName)).thenReturn(tenantId);
    }

    @AfterMethod
    public void tearDown() {

        idPManagementUtil.close();
    }


    @Test(dataProvider = "provisioningData")
    public void testCall(Object entityType, Object entityOperation, String tenantDomainName, Map attributeMap)
            throws Exception {

        System.setProperty("carbon.home", "");
        try (MockedStatic<PrivilegedCarbonContext> privilegedCarbonContext = mockStatic(
                PrivilegedCarbonContext.class)) {
            provisioningEntity = new ProvisioningEntity((ProvisioningEntityType) entityType,
                    (ProvisioningOperation) entityOperation, attributeMap);
            CacheBackedProvisioningMgtDAO mockCacheBackedProvisioningMgDAO = mock(CacheBackedProvisioningMgtDAO.class);
            mockCarbonContext(privilegedCarbonContext);
            doNothing().when(mockCacheBackedProvisioningMgDAO).addProvisioningEntity(idPName, connectorType,
                    provisioningEntity, -1234, tenantDomainName);
            ProvisioningThread provisioningThread =
                    new ProvisioningThread(provisioningEntity, tenantDomainName, mockConnector, connectorType,
                            idPName, mockCacheBackedProvisioningMgDAO);

            Boolean result = provisioningThread.call();
            Assert.assertTrue(result);
        }
    }

    @Test(expectedExceptions = IdentityProvisioningException.class)
    public void testCallForInvalidTenant()
            throws Exception {

        System.setProperty("carbon.home", "");
        try (MockedStatic<PrivilegedCarbonContext> privilegedCarbonContext = mockStatic(PrivilegedCarbonContext.class);
             MockedStatic<ProvisioningEntityCache> provisioningEntityCache =
                     mockStatic(ProvisioningEntityCache.class)) {

            provisioningEntity = new ProvisioningEntity(USER, DELETE, null);

            CacheBackedProvisioningMgtDAO cacheBackedProvisioningMgtDAO =
                    mockCacheBackedProvisioningMgtDAO(privilegedCarbonContext, provisioningEntityCache);
            ProvisioningThread provisioningThread =
                    new ProvisioningThread(provisioningEntity, "", mockConnector, connectorType,
                            idPName, cacheBackedProvisioningMgtDAO);

            provisioningThread.call();
        }
    }

    private CacheBackedProvisioningMgtDAO mockCacheBackedProvisioningMgtDAO(
            MockedStatic<PrivilegedCarbonContext> privilegedCarbonContext,
            MockedStatic<ProvisioningEntityCache> provisioningEntityCache) {

        PrivilegedCarbonContext mockPrivilegedCarbonContext = mock(PrivilegedCarbonContext.class);
        privilegedCarbonContext.when(
                PrivilegedCarbonContext::getThreadLocalCarbonContext).thenReturn(mockPrivilegedCarbonContext);
        ProvisioningEntityCache mockProvisioningEntityCache = spy(ProvisioningEntityCache.class);
        provisioningEntityCache.when(ProvisioningEntityCache::getInstance).thenReturn(mockProvisioningEntityCache);
        ProvisioningManagementDAO provisioningManagementDAO = mock(ProvisioningManagementDAO.class);
        return  new CacheBackedProvisioningMgtDAO(provisioningManagementDAO);
    }

    @DataProvider(name = "provisioningData")
    public Object[][] provisioningData() throws Exception {

        Map<ClaimMapping, List<String>> attributeMap = new HashMap<>();
        attributeMap
                .put(ClaimMapping.build(IdentityProvisioningConstants.NEW_GROUP_NAME_CLAIM_URI, null, null, false), new
                        ArrayList<String>(Arrays.asList("value1", "value2")));

        return new Object[][]{

                {USER, DELETE, tenantDomainName, attributeMap},
                {USER, POST, tenantDomainName, attributeMap},
                {USER, PUT, tenantDomainName, attributeMap},
                {GROUP, PUT, tenantDomainName, attributeMap},
                {GROUP, PUT, tenantDomainName, null},
                {null, null, tenantDomainName, null}
        };
    }

    private void mockCarbonContext(MockedStatic<PrivilegedCarbonContext> privilegedCarbonContext) {

        PrivilegedCarbonContext mockPrivilegedCarbonContext = mock(PrivilegedCarbonContext.class);
        privilegedCarbonContext.when(
                PrivilegedCarbonContext::getThreadLocalCarbonContext).thenReturn(mockPrivilegedCarbonContext);
        when(mockPrivilegedCarbonContext.getTenantDomain()).thenReturn(tenantDomainName);
        when(mockPrivilegedCarbonContext.getTenantId()).thenReturn(-1234);
        when(mockPrivilegedCarbonContext.getUsername()).thenReturn("admin");
    }
}
