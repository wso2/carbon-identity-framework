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

package org.wso2.carbon.identity.api.resource.mgt.dao;

import org.mockito.MockedStatic;
import org.mockito.stubbing.Answer;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.api.resource.mgt.APIResourceMgtException;
import org.wso2.carbon.identity.api.resource.mgt.TestDAOUtils;
import org.wso2.carbon.identity.api.resource.mgt.dao.impl.AuthorizationDetailsTypeMgtDAOImpl;
import org.wso2.carbon.identity.api.resource.mgt.dao.impl.CacheBackedAuthorizationDetailsTypeMgtDAOImpl;
import org.wso2.carbon.identity.api.resource.mgt.internal.APIResourceManagementServiceComponentHolder;
import org.wso2.carbon.identity.application.common.model.APIResource;
import org.wso2.carbon.identity.application.common.model.AuthorizationDetailsType;
import org.wso2.carbon.identity.common.testng.WithCarbonHome;
import org.wso2.carbon.identity.core.util.IdentityDatabaseUtil;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.mockStatic;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;
import static org.testng.AssertJUnit.assertNull;
import static org.wso2.carbon.identity.api.resource.mgt.TestDAOUtils.TEST_TENANT_DOMAIN;
import static org.wso2.carbon.identity.api.resource.mgt.TestDAOUtils.TEST_TENANT_ID;
import static org.wso2.carbon.identity.api.resource.mgt.TestDAOUtils.TEST_TYPE_1;
import static org.wso2.carbon.identity.api.resource.mgt.TestDAOUtils.TEST_TYPE_2;

/**
 * Test class for {@link AuthorizationDetailsTypeMgtDAO}.
 */
@WithCarbonHome
public class CacheBackedAuthorizationDetailsTypeMgtDAOImplTest {

    private static final String TEST_TYPE_NAME = "test name";
    private APIResource apiResource;
    private AuthorizationDetailsTypeMgtDAO uut;
    private MockedStatic<IdentityTenantUtil> identityTenantUtil;
    private MockedStatic<IdentityDatabaseUtil> identityDatabaseUtil;

    @BeforeClass
    public void setUp() throws Exception {

        TestDAOUtils.initializeDataSource(TestDAOUtils.getFilePath("h2.sql"));

        identityTenantUtil = mockStatic(IdentityTenantUtil.class);
        identityDatabaseUtil = mockStatic(IdentityDatabaseUtil.class);

        apiResource = TestDAOUtils.addAPIResourceToDB("testApiResource2", TestDAOUtils.getConnection(),
                TEST_TENANT_ID, identityDatabaseUtil);
        uut = new CacheBackedAuthorizationDetailsTypeMgtDAOImpl(new AuthorizationDetailsTypeMgtDAOImpl());

        APIResourceManagementServiceComponentHolder.getInstance().setRichAuthorizationRequestsEnabled(true);
    }

    @BeforeMethod
    public void setUpBeforeMethod() throws SQLException {

        identityDatabaseUtil.when(() -> IdentityDatabaseUtil.getDBConnection(anyBoolean()))
                .thenReturn(TestDAOUtils.getConnection());
        identityDatabaseUtil.when(() -> IdentityDatabaseUtil.commitTransaction(any(Connection.class)))
                .thenAnswer((Answer<Void>) invocation -> {
                    TestDAOUtils.getConnection().commit();
                    return null;
                });
        identityTenantUtil
                .when(() -> IdentityTenantUtil.getTenantDomain(TEST_TENANT_ID))
                .thenReturn(TEST_TENANT_DOMAIN);
    }

    @AfterClass
    public void tearDown() throws Exception {

        TestDAOUtils.closeDataSource();
        TestDAOUtils.closeMockedStatic(identityTenantUtil);
        TestDAOUtils.closeMockedStatic(identityDatabaseUtil);
    }

    @Test
    public void shouldAddAuthorizationDetailsTypesSuccessfully() throws APIResourceMgtException, SQLException {

        uut.addAuthorizationDetailsTypes(TestDAOUtils.getConnection(), apiResource.getId(),
                TestDAOUtils.getAuthorizationDetailsTypes(), TEST_TENANT_ID);

        AuthorizationDetailsType type1 =
                uut.getAuthorizationDetailsTypeByApiIdAndType(apiResource.getId(), TEST_TYPE_1, TEST_TENANT_ID);

        assertNotNull(type1);
        assertNotNull(type1.getId());
        assertNull(type1.getDescription());
        assertEquals(type1.getType(), TEST_TYPE_1);
    }

    @Test(dependsOnMethods = {"shouldAddAuthorizationDetailsTypesSuccessfully"})
    public void shouldUpdateAuthorizationDetailsTypeSuccessfully() throws APIResourceMgtException, SQLException {

        List<AuthorizationDetailsType> fetchedTypes = uut.getAuthorizationDetailsTypesByApiId(TestDAOUtils
                .getConnection(), apiResource.getId(), TEST_TENANT_ID);

        fetchedTypes.stream()
                .filter(type -> TEST_TYPE_1.equals(type.getType()))
                .forEach(type -> type.setName(TEST_TYPE_NAME));

        identityDatabaseUtil.when(() -> IdentityDatabaseUtil.getDBConnection(anyBoolean()))
                .thenReturn(TestDAOUtils.getConnection());
        uut.updateAuthorizationDetailsTypes(TestDAOUtils.getConnection(), apiResource.getId(), fetchedTypes,
                TEST_TENANT_ID);

        AuthorizationDetailsType type1 =
                uut.getAuthorizationDetailsTypeByApiIdAndType(apiResource.getId(), TEST_TYPE_1, TEST_TENANT_ID);

        assertNotNull(type1);
        assertNull(type1.getDescription());
        assertEquals(type1.getName(), TEST_TYPE_NAME);

        identityDatabaseUtil.when(() -> IdentityDatabaseUtil.getDBConnection(anyBoolean()))
                .thenReturn(TestDAOUtils.getConnection());
        AuthorizationDetailsType type2 =
                uut.getAuthorizationDetailsTypeByApiIdAndType(apiResource.getId(), TEST_TYPE_2, TEST_TENANT_ID);
        assertNotNull(type2);
        assertNull(type2.getName());
        assertNull(type2.getDescription());
    }

    @Test(priority = 1)
    public void shouldDeleteAllAuthorizationDetailsTypesByApiIdAndType() throws APIResourceMgtException, SQLException {

        uut.deleteAuthorizationDetailsTypeByApiIdAndType(apiResource.getId(), TEST_TYPE_1, TEST_TENANT_ID);

        identityDatabaseUtil.when(() -> IdentityDatabaseUtil.getDBConnection(anyBoolean()))
                .thenReturn(TestDAOUtils.getConnection());
        assertFalse(uut.isAuthorizationDetailsTypeExists(apiResource.getId(), TEST_TYPE_1, TEST_TENANT_ID));

        identityDatabaseUtil.when(() -> IdentityDatabaseUtil.getDBConnection(anyBoolean()))
                .thenReturn(TestDAOUtils.getConnection());
        assertTrue(uut.isAuthorizationDetailsTypeExists(apiResource.getId(), TEST_TYPE_2, TEST_TENANT_ID));
    }
}
