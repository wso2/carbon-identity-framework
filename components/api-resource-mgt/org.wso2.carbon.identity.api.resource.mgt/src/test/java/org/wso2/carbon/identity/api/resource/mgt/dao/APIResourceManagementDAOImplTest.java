/*
 * Copyright (c) 2023-2025, WSO2 LLC. (https://www.wso2.com).
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

import org.apache.commons.dbcp.BasicDataSource;
import org.apache.commons.lang.StringUtils;
import org.mockito.MockedStatic;
import org.mockito.stubbing.Answer;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.api.resource.mgt.APIResourceMgtClientException;
import org.wso2.carbon.identity.api.resource.mgt.APIResourceMgtServerException;
import org.wso2.carbon.identity.api.resource.mgt.TestDAOUtils;
import org.wso2.carbon.identity.api.resource.mgt.constant.APIResourceManagementConstants;
import org.wso2.carbon.identity.api.resource.mgt.constant.SQLConstants;
import org.wso2.carbon.identity.api.resource.mgt.dao.impl.APIResourceManagementDAOImpl;
import org.wso2.carbon.identity.api.resource.mgt.internal.APIResourceManagementServiceComponentHolder;
import org.wso2.carbon.identity.application.common.model.APIResource;
import org.wso2.carbon.identity.application.common.model.Scope;
import org.wso2.carbon.identity.core.model.ExpressionNode;
import org.wso2.carbon.identity.core.util.IdentityDatabaseUtil;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;
import org.wso2.carbon.identity.organization.management.service.OrganizationManager;
import org.wso2.carbon.identity.organization.management.service.exception.OrganizationManagementException;
import org.wso2.carbon.identity.organization.management.service.util.OrganizationManagementUtil;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class APIResourceManagementDAOImplTest {

    private static final int TENANT_ID = 2;
    private static final int INVALID_TENANT_ID = 3;
    private static final int ORGANIZATION_TENANT_ID = 1001;
    private static final String DB_NAME = "api_resource_mgt_dao_db";
    public static final String APIRESOURCE_IDENTIFIER = "testAPIResource identifier ";
    public static final String TEST_SCOPE_1 = "testScope1 ";
    private static final String TENANT_TYPE = "TENANT";
    private static final String ORGANIZATION_TYPE = "ORGANIZATION";
    private static final String CONSOLE_ORG_LEVEL_TYPE = "CONSOLE_ORG_LEVEL";
    private static final Map<String, BasicDataSource> dataSourceMap = new HashMap<>();
    private APIResourceManagementDAOImpl daoImpl;

    @BeforeClass
    public void setUp() throws Exception {

        daoImpl = new APIResourceManagementDAOImpl();
        initiateH2Database(getFilePath());

        APIResourceManagementServiceComponentHolder.getInstance().setRichAuthorizationRequestsEnabled(true);

        // Add initial API resources.
        addAPIResourceToDB("Setup-1", getConnection(), TENANT_ID);
        addAPIResourceToDB("Setup-2", getConnection(), TENANT_ID);
    }

    @AfterClass
    public void tearDown() throws Exception {

        closeH2Database();
    }

    @DataProvider
    public Object[][] getAPIResourcesCountData() {
        return new Object[][]{
                {TENANT_ID, TENANT_ID, new ArrayList<>(), false, 2},
                {TENANT_ID, ORGANIZATION_TENANT_ID, new ArrayList<>(), true, 2},
                {INVALID_TENANT_ID, INVALID_TENANT_ID, new ArrayList<>(), false, 0},
        };
    }

    @Test(dataProvider = "getAPIResourcesCountData")
    public void testGetAPIResourcesCount(Integer rootTenantId, Integer retrievingTenantId,
                                         List<ExpressionNode> expressionNodes, boolean isOrganization, int expected)
            throws Exception {

        try (MockedStatic<IdentityDatabaseUtil> identityDatabaseUtil = mockStatic(IdentityDatabaseUtil.class);
             MockedStatic<OrganizationManagementUtil> organizationManagementUtil =
                     mockStatic(OrganizationManagementUtil.class);
             MockedStatic<IdentityTenantUtil> identityTenantUtil = mockStatic(IdentityTenantUtil.class)) {
            if (isOrganization) {
                mockRootOrganizationExtraction(rootTenantId, identityTenantUtil);
            }
            identityDatabaseUtil.when(() -> IdentityDatabaseUtil.getDBConnection(anyBoolean()))
                    .thenReturn(getConnection());
            organizationManagementUtil.when(() -> OrganizationManagementUtil.isOrganization(anyInt()))
                    .thenReturn(isOrganization);
            Assert.assertEquals(daoImpl.getAPIResourcesCount(retrievingTenantId, expressionNodes).intValue(),
                    expected);
        }
    }

    @DataProvider
    public Object[][] getAPIResourcesData() {
        return new Object[][]{
                {2, TENANT_ID, TENANT_ID, false, "ASC", new ArrayList<>(), 2},
                {2, TENANT_ID, ORGANIZATION_TENANT_ID, true, "ASC", new ArrayList<>(), 2},
                {1, TENANT_ID, TENANT_ID, false, "ASC", new ArrayList<>(), 1},
                {1, TENANT_ID, ORGANIZATION_TENANT_ID, true, "ASC", new ArrayList<>(), 1},
                {1, INVALID_TENANT_ID, INVALID_TENANT_ID, false, "ASC", new ArrayList<>(), 0},
        };
    }

    @Test(dataProvider = "getAPIResourcesData", priority = 1)
    public void testGetAPIResources(Integer limit, Integer rootTenantId, Integer retrievingTenantId,
                                    boolean isOrganization, String sortOrder, List<ExpressionNode> expressionNodes,
                                    int count) throws Exception {

        try (MockedStatic<IdentityDatabaseUtil> identityDatabaseUtil = mockStatic(IdentityDatabaseUtil.class);
             MockedStatic<OrganizationManagementUtil> organizationManagementUtil =
                     mockStatic(OrganizationManagementUtil.class);
             MockedStatic<IdentityTenantUtil> identityTenantUtil = mockStatic(IdentityTenantUtil.class)) {
            if (isOrganization) {
                mockRootOrganizationExtraction(rootTenantId, identityTenantUtil);
            }
            identityDatabaseUtil.when(() -> IdentityDatabaseUtil.getDBConnection(anyBoolean()))
                    .thenReturn(getConnection());
            organizationManagementUtil.when(() -> OrganizationManagementUtil.isOrganization(anyInt()))
                    .thenReturn(isOrganization);
            Assert.assertEquals(daoImpl.getAPIResources(limit, retrievingTenantId, sortOrder, expressionNodes).size(),
                    count);
        }
    }

    @DataProvider
    public Object[][] addAPIResourceData() {

        return new Object[][]{
                {"AddAPITest-1", TENANT_ID, false},
                {"AddAPITest-1", TENANT_ID, true}
        };
    }

    @Test(dataProvider = "addAPIResourceData", priority = 2)
    public void testAddAPIResource(String postfix, int tenantId, boolean isOrganization) throws Exception {

        try (MockedStatic<IdentityDatabaseUtil> identityDatabaseUtil = mockStatic(IdentityDatabaseUtil.class);
             MockedStatic<OrganizationManagementUtil> organizationManagementUtil =
                     mockStatic(OrganizationManagementUtil.class)) {
            identityDatabaseUtil.when(() -> IdentityDatabaseUtil.getDBConnection(anyBoolean()))
                    .thenReturn(getConnection());
            organizationManagementUtil.when(() -> OrganizationManagementUtil.isOrganization(anyInt()))
                    .thenReturn(isOrganization);
            APIResource apiResource = createAPIResource(postfix);
            if (!isOrganization) {
                APIResource createdAPIResource = daoImpl.addAPIResource(apiResource, tenantId);
                Assert.assertNotNull(createdAPIResource);
                Assert.assertTrue(createdAPIResource.getName().contains(postfix));
                Assert.assertNotNull(createdAPIResource.getId());
            } else {
                Assert.expectThrows(APIResourceMgtClientException.class, () ->
                        daoImpl.addAPIResource(apiResource, tenantId));
            }
        }
    }

    @DataProvider
    public Object[][] getScopesByAPIData() {
        // Define your test cases here
        return new Object[][]{
                {"GetScopesTest", TENANT_ID, TENANT_ID, false, 2},
                {"GetScopesTest2", INVALID_TENANT_ID, INVALID_TENANT_ID, false, 0},
                {"GetScopesTest3", TENANT_ID, ORGANIZATION_TENANT_ID, true, 2}
        };
    }

    @Test(dataProvider = "getScopesByAPIData", priority = 3)
    public void testGetScopesByAPI(String name, Integer rootTenantId, Integer retrievingTenantId,
                                   boolean isOrganization, int expected) throws Exception {

        try (MockedStatic<IdentityDatabaseUtil> identityDatabaseUtil = mockStatic(IdentityDatabaseUtil.class);
             MockedStatic<OrganizationManagementUtil> organizationManagementUtil =
                     mockStatic(OrganizationManagementUtil.class);
             MockedStatic<IdentityTenantUtil> identityTenantUtil = mockStatic(IdentityTenantUtil.class)) {
            if (isOrganization) {
                mockRootOrganizationExtraction(rootTenantId, identityTenantUtil);
            }
            String apiId = addAPIResourceToDB(name, getConnection(), rootTenantId, identityDatabaseUtil,
                    organizationManagementUtil).getId();
            identityDatabaseUtil.when(() -> IdentityDatabaseUtil.getDBConnection(anyBoolean()))
                    .thenReturn(getConnection());
            organizationManagementUtil.when(() -> OrganizationManagementUtil.isOrganization(anyInt()))
                    .thenReturn(isOrganization);
            Assert.assertEquals(daoImpl.getScopesByAPI(apiId, TENANT_ID).size(), expected);
        }
    }

    @DataProvider
    public Object[][] isAPIResourceExistData() {
        return new Object[][]{
                {"identifier1", TENANT_ID, true},
                {"identifier5", TENANT_ID, true},
                {"identifier4", INVALID_TENANT_ID, false}
        };
    }

    @Test(dataProvider = "isAPIResourceExistData", priority = 4)
    public void testIsAPIResourceExist(String identifierPostFix, Integer tenantId, boolean expected) throws Exception {

        try (MockedStatic<IdentityDatabaseUtil> identityDatabaseUtil = mockStatic(IdentityDatabaseUtil.class);
             MockedStatic<OrganizationManagementUtil> organizationManagementUtil =
                     mockStatic(OrganizationManagementUtil.class)) {
            addAPIResourceToDB(identifierPostFix, getConnection(), tenantId, identityDatabaseUtil,
                    organizationManagementUtil);
            identityDatabaseUtil.when(() -> IdentityDatabaseUtil.getDBConnection(anyBoolean()))
                    .thenReturn(getConnection());
            Assert.assertEquals(daoImpl.isAPIResourceExist(APIRESOURCE_IDENTIFIER + identifierPostFix, TENANT_ID),
                    expected);
        }
    }

    @DataProvider
    public Object[][] isAPIResourceExistByIdData() {
        return new Object[][]{
                {TENANT_ID, true},
                {INVALID_TENANT_ID, false}
        };
    }

    @Test(dataProvider = "isAPIResourceExistByIdData", priority = 5)
    public void testIsAPIResourceExistById(Integer tenantId, boolean expected) throws Exception {

        String namePostFix = "testIsAPIResourceExistById";
        try (MockedStatic<IdentityDatabaseUtil> identityDatabaseUtil = mockStatic(IdentityDatabaseUtil.class);
             MockedStatic<OrganizationManagementUtil> organizationManagementUtil =
                     mockStatic(OrganizationManagementUtil.class)) {
            APIResource createdAPIResource =
                    addAPIResourceToDB(namePostFix, getConnection(), tenantId, identityDatabaseUtil,
                            organizationManagementUtil);
            String apiId = createdAPIResource.getId();
            identityDatabaseUtil.when(() -> IdentityDatabaseUtil.getDBConnection(anyBoolean()))
                    .thenReturn(getConnection());
            Assert.assertEquals(daoImpl.isAPIResourceExistById(apiId, TENANT_ID), expected);
        }
    }

    @DataProvider
    public Object[][] getAPIResourceByIdData() {
        return new Object[][]{
                {"testGetAPIResourceById", TENANT_ID, TENANT_ID, false, true},
                {"testGetOrgAPIResourceById", TENANT_ID, ORGANIZATION_TENANT_ID, true, false},
                {"testGetAPIResourceByIdInvalidTenant", TENANT_ID, INVALID_TENANT_ID, false, false}
        };
    }

    @Test(dataProvider = "getAPIResourceByIdData", priority = 6)
    public void testGetAPIResourceById(String apiNamePostFix, Integer rootTenantId, Integer retrievingTenantId,
                                       boolean isOrganization, boolean expected) throws Exception {

        try (MockedStatic<IdentityDatabaseUtil> identityDatabaseUtil = mockStatic(IdentityDatabaseUtil.class);
             MockedStatic<OrganizationManagementUtil> organizationManagementUtil =
                     mockStatic(OrganizationManagementUtil.class);
             MockedStatic<IdentityTenantUtil> identityTenantUtil = mockStatic(IdentityTenantUtil.class)) {
            if (isOrganization) {
                mockRootOrganizationExtraction(rootTenantId, identityTenantUtil);
            }
            APIResource createdAPIResource =
                    addAPIResourceToDB(apiNamePostFix, getConnection(), rootTenantId, identityDatabaseUtil,
                            organizationManagementUtil);
            String apiId = createdAPIResource.getId();
            identityDatabaseUtil.when(() -> IdentityDatabaseUtil.getDBConnection(anyBoolean()))
                    .thenReturn(getConnection());
            organizationManagementUtil.when(() -> OrganizationManagementUtil.isOrganization(anyInt()))
                    .thenReturn(isOrganization);
            Assert.assertEquals(daoImpl.getAPIResourceById(apiId, retrievingTenantId) != null, expected);
        }
    }

    @DataProvider
    public Object[][] isScopeExistByIdData() {
        return new Object[][]{
                {TENANT_ID, true},
                {INVALID_TENANT_ID, false}
        };
    }

    @Test(dataProvider = "isScopeExistByIdData", priority = 7)
    public void testIsScopeExistById(Integer tenantId, boolean expected) throws Exception {

        try (MockedStatic<IdentityDatabaseUtil> identityDatabaseUtil = mockStatic(IdentityDatabaseUtil.class);
             MockedStatic<OrganizationManagementUtil> organizationManagementUtil =
                     mockStatic(OrganizationManagementUtil.class)) {
            APIResource createdAPIResource = addAPIResourceToDB("testIsScopeExistById", getConnection(), tenantId,
                    identityDatabaseUtil, organizationManagementUtil);
            String scopeId = createdAPIResource.getScopes().get(0).getId();
            identityDatabaseUtil.when(() -> IdentityDatabaseUtil.getDBConnection(anyBoolean()))
                    .thenReturn(getConnection());
            Assert.assertEquals(daoImpl.isScopeExistById(scopeId, TENANT_ID), expected);
        }
    }

    @DataProvider
    public Object[][] deleteAPIResourceByIdData() {

        return new Object[][]{
                {TENANT_ID, false, false},
                {TENANT_ID, true, false}
        };
    }

    @Test(dataProvider = "deleteAPIResourceByIdData", priority = 8)
    public void testDeleteAPIResourceById(Integer tenantId, boolean isOrganization, boolean expected)
            throws Exception {

        try (MockedStatic<IdentityDatabaseUtil> identityDatabaseUtil = mockStatic(IdentityDatabaseUtil.class);
             MockedStatic<OrganizationManagementUtil> organizationManagementUtil =
                     mockStatic(OrganizationManagementUtil.class);
             MockedStatic<IdentityTenantUtil> identityTenantUtil = mockStatic(IdentityTenantUtil.class)) {
            if (isOrganization) {
                mockRootOrganizationExtraction(tenantId, identityTenantUtil);
            }
            String apiId = addAPIResourceToDB("testDeleteAPIResourceById", getConnection(), tenantId,
                    identityDatabaseUtil, organizationManagementUtil).getId();
            Connection connection = getConnection();
            identityDatabaseUtil.when(() -> IdentityDatabaseUtil.getDBConnection(anyBoolean())).thenReturn(connection);

            identityDatabaseUtil.when(() -> IdentityDatabaseUtil.commitTransaction(any(Connection.class)))
                    .thenAnswer((Answer<Void>) invocation -> {
                        connection.commit();
                        return null;
                    });
            organizationManagementUtil.when(() -> OrganizationManagementUtil.isOrganization(anyInt()))
                    .thenReturn(isOrganization);
            if (!isOrganization) {
                daoImpl.deleteAPIResourceById(apiId, tenantId);
                identityDatabaseUtil.when(() -> IdentityDatabaseUtil.getDBConnection(anyBoolean()))
                        .thenReturn(getConnection());
                Assert.assertEquals(daoImpl.isAPIResourceExistById(apiId, tenantId), expected);
            } else {
                Assert.expectThrows(APIResourceMgtClientException.class, () ->
                        daoImpl.deleteAPIResourceById(apiId, tenantId));
            }
        }
    }

    @DataProvider
    public Object[][] isScopeExistByNameData() {
        return new Object[][]{
                {TENANT_ID, "testIsScopeExistByName", true},
                {INVALID_TENANT_ID, "nonExistentScopeName", false}
        };
    }

    @Test(dataProvider = "isScopeExistByNameData", priority = 9)
    public void testIsScopeExistByName(Integer tenantId, String scopeName, boolean expected)
            throws Exception {

        try (MockedStatic<IdentityDatabaseUtil> identityDatabaseUtil = mockStatic(IdentityDatabaseUtil.class);
             MockedStatic<OrganizationManagementUtil> organizationManagementUtil =
                     mockStatic(OrganizationManagementUtil.class);
             MockedStatic<IdentityTenantUtil> identityTenantUtil = mockStatic(IdentityTenantUtil.class)) {
            addAPIResourceToDB(scopeName, getConnection(), tenantId, identityDatabaseUtil, organizationManagementUtil);
            identityDatabaseUtil.when(() -> IdentityDatabaseUtil.getDBConnection(anyBoolean()))
                    .thenReturn(getConnection());

            Assert.assertEquals(daoImpl.isScopeExistByName(TEST_SCOPE_1 + scopeName, TENANT_ID), expected);
        }
    }

    @DataProvider
    public Object[][] getScopeByNameAndTenantIdData() {
        return new Object[][]{
                {TENANT_ID, "testGetScopeByNameAndTenantId", "testGetScopeByNameAndTenantId"}
        };
    }

    @Test(dataProvider = "getScopeByNameAndTenantIdData", priority = 10)
    public void testGetScopeByNameAndTenantId(Integer tenantId, String scopeName, String expectedName)
            throws Exception {

        try (MockedStatic<IdentityDatabaseUtil> identityDatabaseUtil = mockStatic(IdentityDatabaseUtil.class);
             MockedStatic<OrganizationManagementUtil> organizationManagementUtil =
                     mockStatic(OrganizationManagementUtil.class)) {
            addAPIResourceToDB(scopeName, getConnection(), tenantId, identityDatabaseUtil, organizationManagementUtil);
            identityDatabaseUtil.when(() -> IdentityDatabaseUtil.getDBConnection(anyBoolean()))
                    .thenReturn(getConnection());
            Scope scope = daoImpl.getScopeByNameAndTenantId(TEST_SCOPE_1 + scopeName, tenantId);
            Assert.assertEquals(scope.getName(), TEST_SCOPE_1 + expectedName);
        }
    }

    @DataProvider
    public Object[][] addScopes() {
        return new Object[][]{
                {TENANT_ID, TENANT_ID, false, "testAddScopes", "scope1", "scope2"},
                {TENANT_ID, ORGANIZATION_TENANT_ID, true, "testAddScopesOrg", "scope1", "scope2"}
        };
    }

    @Test(dataProvider = "addScopes", priority = 11)
    public void testAddScopes(Integer rootTenantId, Integer addingTenantId, boolean isOrganization,
                              String apiNamePostFix, String scope1, String scope2) throws Exception {

        try (MockedStatic<IdentityDatabaseUtil> identityDatabaseUtil = mockStatic(IdentityDatabaseUtil.class);
             MockedStatic<OrganizationManagementUtil> organizationManagementUtil =
                     mockStatic(OrganizationManagementUtil.class)) {

            APIResource apiResource =
                    addAPIResourceToDB(apiNamePostFix, getConnection(), rootTenantId, identityDatabaseUtil,
                            organizationManagementUtil);
            String apiId = apiResource.getId();
            List<Scope> scopes = Arrays.asList(createScope(scope1), createScope(scope2));

            Connection connection = getConnection();
            identityDatabaseUtil.when(() -> IdentityDatabaseUtil.getDBConnection(anyBoolean())).thenReturn(connection);
            identityDatabaseUtil.when(() -> IdentityDatabaseUtil.commitTransaction(any(Connection.class)))
                    .thenAnswer((Answer<Void>) invocation -> {
                        connection.commit();
                        return null;
                    });
            organizationManagementUtil.when(() -> OrganizationManagementUtil.isOrganization(anyInt()))
                    .thenReturn(isOrganization);
            if (!isOrganization) {
                daoImpl.addScopes(scopes, apiId, addingTenantId);
                for (Scope scope : scopes) {
                    identityDatabaseUtil.when(() -> IdentityDatabaseUtil.getDBConnection(anyBoolean()))
                            .thenReturn(getConnection());
                    Assert.assertTrue(daoImpl.isScopeExistByName(scope.getName(), rootTenantId));
                }
            } else {
                Assert.expectThrows(APIResourceMgtClientException.class, () ->
                        daoImpl.addScopes(scopes, apiId, addingTenantId));
            }
        }
    }

    @DataProvider
    public Object[][] deleteAllScopes() {
        return new Object[][]{
                {TENANT_ID, TENANT_ID, false, "testDeleteAllScopes"},
                {TENANT_ID, ORGANIZATION_TENANT_ID, true, "testDeleteAllOrgScopes"}
        };
    }

    @Test(dataProvider = "deleteAllScopes", priority = 12)
    public void testDeleteAllScopes(Integer rootTenantId, Integer deletingTenantId, boolean isOrganization,
                                    String apiNamePostFix) throws Exception {

        try (MockedStatic<IdentityDatabaseUtil> identityDatabaseUtil = mockStatic(IdentityDatabaseUtil.class);
             MockedStatic<OrganizationManagementUtil> organizationManagementUtil =
                     mockStatic(OrganizationManagementUtil.class)) {

            APIResource apiResource =
                    addAPIResourceToDB(apiNamePostFix, getConnection(), rootTenantId, identityDatabaseUtil,
                            organizationManagementUtil);
            String apiId = apiResource.getId();
            Connection connection = getConnection();
            identityDatabaseUtil.when(() -> IdentityDatabaseUtil.getDBConnection(anyBoolean())).thenReturn(connection);
            identityDatabaseUtil.when(() -> IdentityDatabaseUtil.commitTransaction(any(Connection.class)))
                    .thenAnswer((Answer<Void>) invocation -> {
                        connection.commit();
                        return null;
                    });
            organizationManagementUtil.when(() -> OrganizationManagementUtil.isOrganization(anyInt()))
                    .thenReturn(isOrganization);
            if (!isOrganization) {
                daoImpl.deleteAllScopes(apiId, deletingTenantId);
                for (Scope scope : apiResource.getScopes()) {
                    identityDatabaseUtil.when(() -> IdentityDatabaseUtil.getDBConnection(anyBoolean()))
                            .thenReturn(getConnection());
                    Assert.assertFalse(daoImpl.isScopeExistById(scope.getId(), TENANT_ID));
                }
            } else {
                Assert.expectThrows(APIResourceMgtClientException.class, () ->
                        daoImpl.deleteAllScopes(apiId, deletingTenantId));
            }
        }
    }

    @DataProvider
    public Object[][] deleteScope() {
        return new Object[][]{
                {TENANT_ID, TENANT_ID, false, "testDeleteScope"},
                {TENANT_ID, ORGANIZATION_TENANT_ID, true, "testDeleteOrgScope"}
        };
    }

    @Test(dataProvider = "deleteScope", priority = 13)
    public void testDeleteScope(Integer rootTenantId, Integer deletingTenantId, boolean isOrganization,
                                String apiNamePostFix) throws Exception {

        try (MockedStatic<IdentityDatabaseUtil> identityDatabaseUtil = mockStatic(IdentityDatabaseUtil.class);
             MockedStatic<OrganizationManagementUtil> organizationManagementUtil =
                     mockStatic(OrganizationManagementUtil.class)) {

            APIResource apiResource =
                    addAPIResourceToDB(apiNamePostFix, getConnection(), rootTenantId, identityDatabaseUtil,
                            organizationManagementUtil);
            String apiId = apiResource.getId();
            String scopeName = apiResource.getScopes().get(0).getName(); // Assuming there's at least one scope

            Connection connection = getConnection();
            identityDatabaseUtil.when(() -> IdentityDatabaseUtil.getDBConnection(anyBoolean())).thenReturn(connection);
            identityDatabaseUtil.when(() -> IdentityDatabaseUtil.commitTransaction(any(Connection.class)))
                    .thenAnswer((Answer<Void>) invocation -> {
                        connection.commit();
                        return null;
                    });

            organizationManagementUtil.when(() -> OrganizationManagementUtil.isOrganization(anyInt()))
                    .thenReturn(isOrganization);
            if (!isOrganization) {
                // Testing the deleteScope method with the created API resource's ID and scope ID
                daoImpl.deleteScope(apiId, scopeName, deletingTenantId);

                // Checking whether the scope is deleted
                identityDatabaseUtil.when(() -> IdentityDatabaseUtil.getDBConnection(anyBoolean()))
                        .thenReturn(getConnection());
                Assert.assertFalse(daoImpl.isScopeExistByName(scopeName, deletingTenantId));
            } else {
                Assert.expectThrows(APIResourceMgtClientException.class, () ->
                        daoImpl.deleteScope(apiId, scopeName, deletingTenantId));
            }
        }

    }

    @DataProvider
    public Object[][] updateAPIResourceScopeAddition() {

        return new Object[][]{
                {APIResourceManagementConstants.APIResourceTypes.BUSINESS, 2},
                {APIResourceManagementConstants.APIResourceTypes.SYSTEM, 2},
                {ORGANIZATION_TYPE, 2},
                {TENANT_TYPE, 2},
                {CONSOLE_ORG_LEVEL_TYPE, 2}
        };
    }

    @Test(dataProvider = "updateAPIResourceScopeAddition", priority = 14)
    public void testUpdateAPIResourceScopeAddition(String type, int expectedValue)
            throws Exception {

        // Add API resource to database.
        String apiNamePostFix = "update-scope-addition-test";
        List<Scope> scopes = new ArrayList<>();
        scopes.add(createScope("test_scope_1_" + apiNamePostFix));
        scopes.add(createScope("test_scope_2_" + apiNamePostFix));
        try (MockedStatic<OrganizationManagementUtil> organizationManagementUtil =
                     mockStatic(OrganizationManagementUtil.class)) {
            // Following mocks are in action when the registered API resources are extracted from the database. This
            // will not affect to the update operation since the update operation does not support in the organization
            // level.
            APIResource apiResource = addAPIResourceToDB(apiNamePostFix, scopes, type, getConnection(), TENANT_ID,
                    organizationManagementUtil);

            // Validate scopes count before update.
            validateScopesCount(apiResource.getId(), expectedValue);

            try (MockedStatic<IdentityDatabaseUtil> identityDatabaseUtil = mockStatic(IdentityDatabaseUtil.class)) {
                identityDatabaseUtil.when(() -> IdentityDatabaseUtil.getDBConnection(anyBoolean()))
                        .thenReturn(getConnection());

                Scope newScope = createScope("test_scope_3_" + apiNamePostFix);
                apiResource.getScopes().add(newScope);

                List<Scope> addedScopes = new ArrayList<>();
                addedScopes.add(newScope);

                // Update API resource with a new scope.
                daoImpl.updateAPIResource(apiResource, addedScopes, Collections.emptyList(), TENANT_ID);
            }

            // Validate updated scopes count.
            validateScopesCount(apiResource.getId(), 3);

            // Delete API resource from database.
            deleteAPIResourceFromDB(apiResource.getId(), TENANT_ID);
        }

    }

    @Test(priority = 15)
    public void testIsManagementOrOrganizationAPIResourceType_withTenant() throws Exception {

        boolean result = invokeIsManagementOrOrganizationAPIResourceType(
                APIResourceManagementConstants.APIResourceTypes.TENANT);
        Assert.assertTrue(result, "TENANT type should be identified as management or organization type");
    }

    @Test(priority = 16)
    public void testIsManagementOrOrganizationAPIResourceType_withOrganization() throws Exception {

        boolean result = invokeIsManagementOrOrganizationAPIResourceType(
                APIResourceManagementConstants.APIResourceTypes.ORGANIZATION);
        Assert.assertTrue(result, "ORGANIZATION type should be identified " +
                "as management or organization type");
    }

    @Test(priority = 17)
    public void testIsManagementOrOrganizationAPIResourceType_withBusiness() throws Exception {

        boolean result = invokeIsManagementOrOrganizationAPIResourceType(
                APIResourceManagementConstants.APIResourceTypes.BUSINESS);
        Assert.assertFalse(result, "BUSINESS type should not be identified " +
                "as management or organization type");
    }

    @Test(priority = 18)
    public void testIsScopeExistsByApiID_whenScopeExists() throws Exception {

        try (
                MockedStatic<OrganizationManagementUtil> organizationManagementUtil =
                        mockStatic(OrganizationManagementUtil.class)
        ) {
            Connection connection = mock(Connection.class);
            PreparedStatement preparedStatement = mock(PreparedStatement.class);
            ResultSet resultSet = mock(ResultSet.class);

            when(connection.prepareStatement(SQLConstants.GET_SCOPE_BY_NAME_AND_API_ID)).thenReturn(preparedStatement);
            when(preparedStatement.executeQuery()).thenReturn(resultSet);
            when(resultSet.next()).thenReturn(true);

            organizationManagementUtil.when(() -> OrganizationManagementUtil.isOrganization(anyInt()))
                    .thenReturn(false);

            boolean result = invokeIsScopeExistsByApiID(connection, "read", 1, "api-123");
            Assert.assertTrue(result, "Scope should exist when resultSet.next() returns true");

            verify(preparedStatement).setString(1, "read");
            verify(preparedStatement).setInt(2, 1);
            verify(preparedStatement).setString(3, "api-123");
        }
    }

    @Test(priority = 19)
    public void testIsScopeExistsByApiID_whenScopeDoesNotExist() throws Exception {

        try (
                MockedStatic<OrganizationManagementUtil> organizationManagementUtil =
                        mockStatic(OrganizationManagementUtil.class)
        ) {
            Connection connection = mock(Connection.class);
            PreparedStatement preparedStatement = mock(PreparedStatement.class);
            ResultSet resultSet = mock(ResultSet.class);

            when(connection.prepareStatement(SQLConstants.GET_SCOPE_BY_NAME_AND_API_ID)).thenReturn(preparedStatement);
            when(preparedStatement.executeQuery()).thenReturn(resultSet);
            when(resultSet.next()).thenReturn(false);

            organizationManagementUtil.when(() -> OrganizationManagementUtil.isOrganization(anyInt()))
                    .thenReturn(false);

            boolean result = invokeIsScopeExistsByApiID(connection, "write", 1, "api-123");
            Assert.assertFalse(result, "Scope should not exist when resultSet.next() returns false");
        }
    }

    @Test(priority = 20)
    public void testAddScopesWithTenantResourceType() throws Exception {

        try (MockedStatic<OrganizationManagementUtil> organizationManagementUtil =
                     mockStatic(OrganizationManagementUtil.class)) {

            Connection mockConnection = mock(Connection.class);
            PreparedStatement mockInsertStmt = mock(PreparedStatement.class);
            PreparedStatement mockExistStmt = mock(PreparedStatement.class);
            ResultSet mockResultSet = mock(ResultSet.class);

            Scope testScope1 = createScope("testScope1");
            Scope testScope2 = createScope("testScope2");
            List<Scope> testScopes = Arrays.asList(testScope1, testScope2);
            addAPIResourceToDB("testAPI1", testScopes, "TENANT", getConnection(),
                    TENANT_ID, organizationManagementUtil);

            Scope testScope3 = createScope("testScope1");
            Scope testScope4 = createScope("testScope3");
            List<Scope> scopes = Arrays.asList(testScope3, testScope4);

            APIResource apiResource = addAPIResourceToDB("testAPI", scopes, "TENANT", getConnection(),
                    TENANT_ID, organizationManagementUtil);
            String apiId = apiResource.getId();
            String apiType = apiResource.getType();

            when(mockConnection.prepareStatement(SQLConstants.GET_SCOPE_BY_NAME_AND_API_ID)).thenReturn(mockExistStmt);
            when(mockExistStmt.executeQuery()).thenReturn(mockResultSet);
            when(mockResultSet.next()).thenReturn(false);
            when(mockConnection.prepareStatement(SQLConstants.ADD_SCOPE)).thenReturn(mockInsertStmt);

            Method addScopesMethod = APIResourceManagementDAOImpl.class.getDeclaredMethod(
                    "addScopes", Connection.class, String.class, List.class, Integer.class, String.class);
            addScopesMethod.setAccessible(true);
            addScopesMethod.invoke(daoImpl, mockConnection, apiId, scopes, TENANT_ID, apiType);

            // Verifications
            verify(mockConnection, times(scopes.size()))
                    .prepareStatement(SQLConstants.GET_SCOPE_BY_NAME_AND_API_ID);
            verify(mockConnection, times(1))
                    .prepareStatement(SQLConstants.ADD_SCOPE);
            verify(mockInsertStmt, times(scopes.size())).addBatch();
            verify(mockInsertStmt, times(1)).executeBatch();
        }
    }

    @Test(priority = 21)
    public void testAddBusinessAPIResourceWithDuplicateScope() throws Exception {

        try (MockedStatic<IdentityDatabaseUtil> identityDatabaseUtil = mockStatic(IdentityDatabaseUtil.class);
             MockedStatic<OrganizationManagementUtil> organizationManagementUtil =
                     mockStatic(OrganizationManagementUtil.class)) {

            // Mock database utilities to return fresh connections
            identityDatabaseUtil.when(() -> IdentityDatabaseUtil.getDBConnection(anyBoolean()))
                    .thenAnswer(invocation -> getConnection());
            identityDatabaseUtil.when(() -> IdentityDatabaseUtil.commitTransaction(any(Connection.class)))
                    .thenAnswer((Answer<Void>) invocation -> {
                        Connection conn = invocation.getArgument(0);
                        if (!conn.isClosed()) {
                            conn.commit();
                        }
                        return null;
                    });
            identityDatabaseUtil.when(() -> IdentityDatabaseUtil.rollbackTransaction(any(Connection.class)))
                    .thenAnswer((Answer<Void>) invocation -> {
                        Connection conn = invocation.getArgument(0);
                        if (!conn.isClosed()) {
                            conn.rollback();
                        }
                        return null;
                    });
            organizationManagementUtil.when(() -> OrganizationManagementUtil.isOrganization(anyInt()))
                    .thenReturn(false);

            // Create the first business API resource with a scope
            String sharedScopeName = "duplicate_scope_test";

            List<Scope> firstApiScopes = new ArrayList<>();
            firstApiScopes.add(TestDAOUtils.createScope(sharedScopeName));

            APIResource.APIResourceBuilder firstApiBuilder = new APIResource.APIResourceBuilder()
                    .name("BusinessAPI-1")
                    .identifier("business-api-1-identifier-" + System.currentTimeMillis())
                    .description("First business API resource")
                    .type(APIResourceManagementConstants.APIResourceTypes.BUSINESS)
                    .requiresAuthorization(true)
                    .scopes(firstApiScopes);

            APIResource firstApiResource = firstApiBuilder.build();

            // Add the first business API resource - this should succeed
            APIResource createdFirstApi = daoImpl.addAPIResource(firstApiResource, TENANT_ID);
            Assert.assertNotNull(createdFirstApi);
            Assert.assertEquals(createdFirstApi.getType(), APIResourceManagementConstants.APIResourceTypes.BUSINESS);
            Assert.assertNotNull(createdFirstApi.getId());
            Assert.assertEquals(createdFirstApi.getScopes().size(), 1);
            Assert.assertEquals(createdFirstApi.getScopes().get(0).getName(), sharedScopeName);

            // Verify the scope exists in the database
            boolean scopeExists = daoImpl.isScopeExistByName(sharedScopeName, TENANT_ID);
            Assert.assertTrue(scopeExists, "Scope should exist after first API creation");

            // Create the second business API resource with the same scope name
            List<Scope> secondApiScopes = new ArrayList<>();
            secondApiScopes.add(TestDAOUtils.createScope(sharedScopeName)); // Same scope name as first API

            APIResource.APIResourceBuilder secondApiBuilder = new APIResource.APIResourceBuilder()
                    .name("BusinessAPI-2")
                    .identifier("business-api-2-identifier-" + System.currentTimeMillis())
                    .description("Second business API resource")
                    .type(APIResourceManagementConstants.APIResourceTypes.BUSINESS)
                    .requiresAuthorization(true)
                    .scopes(secondApiScopes);

            APIResource secondApiResource = secondApiBuilder.build();

            // Try to add the second business API resource with duplicate scope - this should fail
            APIResourceMgtClientException exception = Assert.expectThrows(APIResourceMgtClientException.class, () ->
                    daoImpl.addAPIResource(secondApiResource, TENANT_ID));

            // Verify the exception details
            Assert.assertEquals(exception.getErrorCode(),
                    APIResourceManagementConstants.ErrorMessages.ERROR_CODE_SCOPE_ALREADY_EXISTS.getCode());
            Assert.assertTrue(exception.getMessage().contains("Unable to add scope"));
            Assert.assertTrue(exception.getDescription().contains("Scope already exists for the tenant"));
        }
    }

    @Test(priority = 22)
    public void testIsScopeExistsByApiId_SQLException() throws Exception {
        
        try (MockedStatic<OrganizationManagementUtil> organizationManagementUtil = 
                     mockStatic(OrganizationManagementUtil.class)) {
            
            Connection connection = mock(Connection.class);
            PreparedStatement preparedStatement = mock(PreparedStatement.class);
            organizationManagementUtil.when(() -> OrganizationManagementUtil.isOrganization(anyInt()))
                    .thenReturn(false);
            when(connection.prepareStatement(SQLConstants.GET_SCOPE_BY_NAME_AND_API_ID))
                    .thenReturn(preparedStatement);
            when(preparedStatement.executeQuery()).thenThrow(new SQLException("Database connection error"));
            
            try {
                invokeIsScopeExistsByApiID(connection, "testScope", TENANT_ID, "api-123");
                Assert.fail("Should have thrown exception due to SQLException");
            } catch (Exception e) {
                // Check if it's InvocationTargetException (from reflection) or direct exception
                Throwable actualCause = e;
                if (e instanceof InvocationTargetException) {
                    actualCause = ((InvocationTargetException) e).getCause();
                }
                
                // Verify it's the expected server exception
                Assert.assertTrue(actualCause instanceof APIResourceMgtServerException, 
                    "Expected APIResourceMgtServerException but got: " + actualCause.getClass().getName());
                Assert.assertTrue(actualCause.getMessage().contains("Error while checking existence of scope"), 
                    "Exception message should contain scope existence error");
            }
        }
    }

    @Test(priority = 23)
    public void testIsScopeExistsByApiId_OrganizationManagementException() throws Exception {
        
        try (MockedStatic<OrganizationManagementUtil> organizationManagementUtil = 
                     mockStatic(OrganizationManagementUtil.class);
             MockedStatic<IdentityTenantUtil> identityTenantUtil = 
                     mockStatic(IdentityTenantUtil.class)) {
            
            Connection connection = mock(Connection.class);
            organizationManagementUtil.when(() -> OrganizationManagementUtil.isOrganization(anyInt()))
                    .thenThrow(new OrganizationManagementException("Error while checking organization"));
            identityTenantUtil.when(() -> IdentityTenantUtil.getTenantDomain(anyInt()))
                    .thenReturn("carbon.super");
            
            try {
                invokeIsScopeExistsByApiID(connection, "testScope", TENANT_ID, "api-123");
                Assert.fail("Should have thrown exception due to OrganizationManagementException");
            } catch (Exception e) {
                Throwable actualCause = e;
                if (e instanceof InvocationTargetException) {
                    actualCause = ((InvocationTargetException) e).getCause();
                }
                
                Assert.assertTrue(actualCause instanceof APIResourceMgtServerException, 
                    "Expected APIResourceMgtServerException but got: " + actualCause.getClass().getName());
                Assert.assertNotNull(actualCause.getCause(),
                         "Should have original OrganizationManagementException as cause");
                Assert.assertTrue(actualCause.getCause() instanceof OrganizationManagementException, 
                    "Root cause should be OrganizationManagementException");
            }
        }
    }

    private boolean invokeIsScopeExistsByApiID(Connection connection, String scope,
                                               int tenantId, String apiId) throws Exception {

        APIResourceManagementDAOImpl daoImpl = new APIResourceManagementDAOImpl();
        Method method = APIResourceManagementDAOImpl.class.getDeclaredMethod(
                "isScopeExistsByApiId", Connection.class, String.class, Integer.class, String.class);
        method.setAccessible(true);
        try {
            return (boolean) method.invoke(daoImpl, connection, scope, tenantId, apiId);
        } finally {
            method.setAccessible(false);
        }
    }

    private boolean invokeIsManagementOrOrganizationAPIResourceType(String type) throws Exception {

        APIResourceManagementDAOImpl daoImpl = new APIResourceManagementDAOImpl();
        Method method = APIResourceManagementDAOImpl.class.getDeclaredMethod(
                "isManagementOrOrganizationAPIResourceType", String.class);
        method.setAccessible(true);
        return (boolean) method.invoke(daoImpl, type);
    }

    /**
     * Create scope with the given name.
     *
     * @param name Name of the scope.
     * @return Scope.
     */
    private static Scope createScope(String name) {

        Scope.ScopeBuilder scopeBuilder = new Scope.ScopeBuilder()
                .name(name)
                .displayName("displayName " + name)
                .description("description " + name);
        return scopeBuilder.build();
    }


    /**
     * Create API resource with the given postfix.
     *
     * @param postFix Postfix to be appended to each API resource and scope information.
     * @return API resource.
     */
    private static APIResource createAPIResource(String postFix) {

        List<Scope> scopes = new ArrayList<>();
        scopes.add(createScope(TEST_SCOPE_1 + postFix));
        scopes.add(createScope("testScope2 " + postFix));

        APIResource.APIResourceBuilder apiResourceBuilder = new APIResource.APIResourceBuilder()
                .name("testAPIResource name " + postFix)
                .identifier(APIRESOURCE_IDENTIFIER + postFix)
                .description("testAPIResource description " + postFix)
                .type("BUSINESS")
                .requiresAuthorization(true)
                .scopes(scopes);
        return apiResourceBuilder.build();
    }

    /**
     * Create API resource with the given postfix, scopes and type.
     *
     * @param postFix Postfix to be appended to each API resource and scope information.
     * @param scopes  List of scopes.
     * @param type    API resource type.
     * @return API resource.
     */
    private static APIResource createAPIResource(String postFix, List<Scope> scopes, String type) {

        APIResource.APIResourceBuilder apiResourceBuilder = new APIResource.APIResourceBuilder()
                .name("Test API Resource Name " + postFix)
                .identifier("/test/api/path/" + postFix)
                .description("Test API Resource Description " + postFix)
                .type(type)
                .requiresAuthorization(true)
                .scopes(scopes);
        return apiResourceBuilder.build();
    }

    /**
     * Add API resource to the database.
     *
     * @param namePostFix Postfix to be appended to each API resource and scope information.
     * @param connection  Database connection.
     * @param tenantId    Tenant ID.
     * @return API resource.
     * @throws Exception Error when adding API resource.
     */
    private APIResource addAPIResourceToDB(String namePostFix, Connection connection, int tenantId) throws Exception {

        try (MockedStatic<IdentityDatabaseUtil> identityDatabaseUtil = mockStatic(IdentityDatabaseUtil.class);
             MockedStatic<OrganizationManagementUtil> organizationManagementUtil =
                     mockStatic(OrganizationManagementUtil.class)) {
            return addAPIResourceToDB(namePostFix, connection, tenantId, identityDatabaseUtil,
                    organizationManagementUtil);
        }
    }

    /**
     * Add API resource to the database.
     *
     * @param namePostFix Postfix to be appended to each API resource and scope information.
     * @param connection  Database connection.
     * @param tenantId    Tenant ID.
     * @param identityDatabaseUtil Mocked IdentityDatabaseUtil.
     * @return API resource.
     * @throws Exception Error when adding API resource.
     */
    private APIResource addAPIResourceToDB(String namePostFix, Connection connection, int tenantId,
                                           MockedStatic<IdentityDatabaseUtil> identityDatabaseUtil,
                                           MockedStatic<OrganizationManagementUtil> organizationManagementUtil)
            throws Exception {

        APIResource apiResource = createAPIResource(namePostFix);
        identityDatabaseUtil.when(() -> IdentityDatabaseUtil.getDBConnection(anyBoolean())).thenReturn(connection);
        identityDatabaseUtil.when(() -> IdentityDatabaseUtil.commitTransaction(any(Connection.class)))
                .thenAnswer((Answer<Void>) invocation -> {
                    connection.commit();
                    return null;
                });
        organizationManagementUtil.when(() -> OrganizationManagementUtil.isOrganization(anyInt()))
                .thenReturn(false);
        return daoImpl.addAPIResource(apiResource, tenantId);
    }

    /**
     * Add API resource to the database.
     *
     * @param namePostFix Postfix to be appended to each API resource and scope information.
     * @param scopes      List of scopes.
     * @param type        API resource type.
     * @param connection  Database connection.
     * @param tenantId    Tenant ID.
     * @return API resource.
     * @throws Exception Error when adding API resource.
     */
    private APIResource addAPIResourceToDB(String namePostFix, List<Scope> scopes, String type, Connection connection,
                                           int tenantId,
                                           MockedStatic<OrganizationManagementUtil> organizationManagementUtil)
            throws Exception {

        try (MockedStatic<IdentityDatabaseUtil> identityDatabaseUtil = mockStatic(IdentityDatabaseUtil.class)) {
            APIResource apiResource = createAPIResource(namePostFix, scopes, type);
            identityDatabaseUtil.when(() -> IdentityDatabaseUtil.getDBConnection(anyBoolean())).thenReturn(connection);
            identityDatabaseUtil.when(() -> IdentityDatabaseUtil.commitTransaction(any(Connection.class)))
                    .thenAnswer((Answer<Void>) invocation -> {
                        connection.commit();
                        return null;
                    });
            organizationManagementUtil.when(() -> OrganizationManagementUtil.isOrganization(anyInt()))
                    .thenReturn(false);
            return daoImpl.addAPIResource(apiResource, tenantId);
        }

    }

    /**
     * Delete API resource from the database.
     *
     * @param apiId    API resource ID.
     * @param tenantId Tenant ID.
     * @throws Exception Error when deleting API resource.
     */
    private void deleteAPIResourceFromDB(String apiId, int tenantId) throws Exception {

        try (MockedStatic<IdentityDatabaseUtil> identityDatabaseUtil = mockStatic(IdentityDatabaseUtil.class)) {
            Connection connection = getConnection();
            identityDatabaseUtil.when(() -> IdentityDatabaseUtil.getDBConnection(anyBoolean())).thenReturn(connection);

            identityDatabaseUtil.when(() -> IdentityDatabaseUtil.commitTransaction(any(Connection.class)))
                    .thenAnswer((Answer<Void>) invocation -> {
                        connection.commit();
                        return null;
                    });
            daoImpl.deleteAPIResourceById(apiId, tenantId);
        }
    }

    /**
     * Method to validate the number of scopes for a given API.
     *
     * @param apiId                  API resource ID.
     * @param expectedNumberOfScopes Expected number of scopes.
     * @throws Exception Error when validating scopes count.
     */
    private void validateScopesCount(String apiId, int expectedNumberOfScopes) throws Exception {

        try (MockedStatic<IdentityDatabaseUtil> identityDatabaseUtil = mockStatic(IdentityDatabaseUtil.class)) {
            identityDatabaseUtil.when(() -> IdentityDatabaseUtil.getDBConnection(anyBoolean()))
                    .thenReturn(getConnection());

            List<Scope> scopesList = daoImpl.getScopesByAPI(apiId, TENANT_ID);
            Assert.assertNotNull(scopesList);
            Assert.assertEquals(scopesList.size(), expectedNumberOfScopes);
        }
    }

    private static Connection getConnection() throws SQLException {

        if (dataSourceMap.get(DB_NAME) != null) {
            return dataSourceMap.get(DB_NAME).getConnection();
        }
        throw new RuntimeException("No datasource initiated for database: " + APIResourceManagementDAOImplTest.DB_NAME);
    }

    /**
     * Initiate H2 database.
     *
     * @param scriptPath Path to the database script.
     * @throws Exception Error when initiating H2 database.
     */
    private void initiateH2Database(String scriptPath) throws Exception {

        BasicDataSource dataSource = new BasicDataSource();
        dataSource.setDriverClassName("org.h2.Driver");
        dataSource.setUsername("username");
        dataSource.setPassword("password");
        dataSource.setUrl("jdbc:h2:mem:test" + DB_NAME);
        dataSource.setTestOnBorrow(true);
        dataSource.setValidationQuery("select 1");
        try (Connection connection = dataSource.getConnection()) {
            connection.createStatement().executeUpdate("RUNSCRIPT FROM '" + scriptPath + "'");
        }
        dataSourceMap.put(DB_NAME, dataSource);
    }

    /**
     * Close H2 database.
     *
     * @throws Exception Error when closing H2 database.
     */
    public static void closeH2Database() throws Exception {

        BasicDataSource dataSource = dataSourceMap.get(DB_NAME);
        if (dataSource != null) {
            dataSource.close();
        }
    }

    /**
     * Get the path to the database script.
     *
     * @return Path to the database script.
     */
    private static String getFilePath() {

        if (StringUtils.isNotBlank("h2.sql")) {
            return Paths.get(System.getProperty("user.dir"), "src", "test", "resources", "dbscripts", "h2.sql")
                    .toString();
        }
        throw new IllegalArgumentException("DB Script file name cannot be empty.");
    }

    /**
     * Mock the root organization extraction when the APIs need to be extracted from the organization level.
     *
     * @param rootTenantId Root tenant ID of the sub organization.
     * @param identityTenantUtil Mocked IdentityTenantUtil to get the tenant domains.
     * @throws OrganizationManagementException Error when extracting the root organization.
     */
    private static void mockRootOrganizationExtraction(Integer rootTenantId,
                                                       MockedStatic<IdentityTenantUtil> identityTenantUtil)
            throws OrganizationManagementException {

        identityTenantUtil.when(() -> IdentityTenantUtil.getTenantDomain(anyInt())).thenReturn("tenant1");
        OrganizationManager organizationManager = mock(OrganizationManager.class);
        APIResourceManagementServiceComponentHolder.getInstance().setOrganizationManager(organizationManager);
        lenient().when(organizationManager.resolveOrganizationId(anyString())).thenReturn("org-id-1234");
        lenient().when(organizationManager.getPrimaryOrganizationId("org-id-1234"))
                .thenReturn("prim-org-id-1234");
        lenient().when(organizationManager.resolveTenantDomain("prim-org-id-1234"))
                .thenReturn("prim-tenant");
        identityTenantUtil.when(() -> IdentityTenantUtil.getTenantId(anyString())).thenReturn(rootTenantId);
    }
}
