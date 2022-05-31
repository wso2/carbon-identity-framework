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
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.testng.PowerMockTestCase;
import org.testng.Assert;
import org.testng.IObjectFactory;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.ObjectFactory;
import org.testng.annotations.Test;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.identity.application.common.model.ClaimMapping;
import org.wso2.carbon.identity.provisioning.dao.CacheBackedProvisioningMgtDAO;
import org.wso2.carbon.idp.mgt.util.IdPManagementUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.mock;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.powermock.api.mockito.PowerMockito.doNothing;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.when;
import static org.wso2.carbon.identity.provisioning.ProvisioningEntityType.GROUP;
import static org.wso2.carbon.identity.provisioning.ProvisioningEntityType.USER;
import static org.wso2.carbon.identity.provisioning.ProvisioningOperation.DELETE;
import static org.wso2.carbon.identity.provisioning.ProvisioningOperation.POST;
import static org.wso2.carbon.identity.provisioning.ProvisioningOperation.PUT;

/**
 * Test class for ProvisioningThread test cases.
 */
@PrepareForTest({IdPManagementUtil.class, CacheBackedProvisioningMgtDAO.class, PrivilegedCarbonContext.class})
public class ProvisioningThreadTest extends PowerMockTestCase {

    private ProvisioningEntity provisioningEntity;
    private String tenantDomainName = "carbon.super";
    private String connectorType = "testConnectorType";
    private String idPName = "testIdpName";

    @Mock
    private CacheBackedProvisioningMgtDAO mockCacheBackedProvisioningMgDAO;
    @Mock
    private AbstractOutboundProvisioningConnector mockConnector;

    @BeforeMethod
    public void setUp() throws Exception {

        initMocks(this);

        ProvisionedIdentifier provisionedIdentifier = new ProvisionedIdentifier();
        when(mockConnector.provision(provisioningEntity)).thenReturn(provisionedIdentifier);
        int tenantId = -1234;
        doNothing().when(mockCacheBackedProvisioningMgDAO).addProvisioningEntity(idPName, connectorType,
                provisioningEntity, tenantId, tenantDomainName);
        mockStatic(IdPManagementUtil.class);
        when(IdPManagementUtil.getTenantIdOfDomain(tenantDomainName)).thenReturn(tenantId);
    }


    @Test(dataProvider = "provisioningData")
    public void testCall(Object entityType, Object entityOperation, String tenantDomainName, Map attributeMap)
            throws Exception {

        provisioningEntity = new ProvisioningEntity((ProvisioningEntityType) entityType,
                (ProvisioningOperation) entityOperation, attributeMap);
        System.setProperty("carbon.home", "");
        mockCarbonContextForTenant();
        ProvisioningThread provisioningThread =
                new ProvisioningThread(provisioningEntity, tenantDomainName, mockConnector, connectorType,
                        idPName, mockCacheBackedProvisioningMgDAO);

        Boolean result = provisioningThread.call();
        Assert.assertTrue(result);
    }

    @Test
    public void testCallForInvalidTenant()
            throws Exception {

        provisioningEntity = new ProvisioningEntity(USER, DELETE, null);
        System.setProperty("carbon.home", "");
        mockCarbonContextForTenant();
        ProvisioningThread provisioningThread =
                new ProvisioningThread(provisioningEntity, "", mockConnector, connectorType,
                        idPName, mockCacheBackedProvisioningMgDAO);

        provisioningThread.call();
        Assert.assertEquals(PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId(), -1);
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

    @ObjectFactory
    public IObjectFactory getObjectFactory() {

        return new org.powermock.modules.testng.PowerMockObjectFactory();
    }

    private void mockCarbonContextForTenant() {

        mockStatic(PrivilegedCarbonContext.class);
        PrivilegedCarbonContext privilegedCarbonContext = mock(PrivilegedCarbonContext.class);
        when(PrivilegedCarbonContext.getThreadLocalCarbonContext()).thenReturn(privilegedCarbonContext);
        when(privilegedCarbonContext.getTenantDomain()).thenReturn(tenantDomainName);
        when(privilegedCarbonContext.getTenantId()).thenReturn(-1234);
        when(privilegedCarbonContext.getUsername()).thenReturn("admin");
    }
}
