/*
 * Copyright (c) 2023, WSO2 LLC. (http://www.wso2.com).
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
import org.mockito.stubbing.Answer;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.testng.PowerMockTestCase;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.api.resource.mgt.dao.impl.APIResourceManagementDAOImpl;
import org.wso2.carbon.identity.api.resource.mgt.util.APIResourceManagementUtil;
import org.wso2.carbon.identity.application.common.model.APIResource;
import org.wso2.carbon.identity.application.common.model.Scope;
import org.wso2.carbon.identity.core.model.ExpressionNode;
import org.wso2.carbon.identity.core.util.IdentityDatabaseUtil;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;
import org.wso2.carbon.identity.core.util.IdentityUtil;

import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.when;

@PrepareForTest({IdentityDatabaseUtil.class, IdentityTenantUtil.class, IdentityUtil.class, DataSource.class,
        APIResourceManagementUtil.class})
public class APIResourceManagementDAOImplTest extends PowerMockTestCase {

    private static final int TENANT_ID = 2;
    private static final int INVALID_TENANT_ID = 3;
    private static final String DB_NAME = "api_resource_mgt_dao_db";
    public static final String APIRESOURCE_IDENTIFIER = "testAPIResource identifier ";
    public static final String TEST_SCOPE_1 = "testScope1 ";
    private static final Map<String, BasicDataSource> dataSourceMap = new HashMap<>();
    private APIResourceManagementDAOImpl daoImpl;

    @BeforeClass
    public void setUp() throws Exception {

        daoImpl = new APIResourceManagementDAOImpl();
        initiateH2Database(getFilePath());
        mockStatic(IdentityTenantUtil.class);
        mockStatic(IdentityDatabaseUtil.class);
        mockStatic(APIResourceManagementUtil.class);


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
                {TENANT_ID, new ArrayList<>(), 2},
                {INVALID_TENANT_ID, new ArrayList<>(), 0},
        };
    }

    @Test(dataProvider = "getAPIResourcesCountData")
    public void testGetAPIResourcesCount(Integer tenantId, List<ExpressionNode> expressionNodes, int expected)
            throws Exception {

        mockStatic(IdentityDatabaseUtil.class);
        mockStatic(APIResourceManagementUtil.class);
        when(IdentityDatabaseUtil.getDBConnection(anyBoolean())).thenReturn(getConnection());
        Assert.assertEquals(daoImpl.getAPIResourcesCount(tenantId, expressionNodes).intValue(), expected);
    }

    @DataProvider
    public Object[][] getAPIResourcesData() {
        return new Object[][]{
                {2, TENANT_ID, "ASC", new ArrayList<>(), 2},
                {1, TENANT_ID, "ASC", new ArrayList<>(), 1},
                {1, INVALID_TENANT_ID, "ASC", new ArrayList<>(), 0},
        };
    }

    @Test(dataProvider = "getAPIResourcesData", priority = 1)
    public void testGetAPIResources(Integer limit, Integer tenantId, String sortOrder,
                                    List<ExpressionNode> expressionNodes, int count) throws Exception {

        mockStatic(IdentityDatabaseUtil.class);
        mockStatic(APIResourceManagementUtil.class);
        when(IdentityDatabaseUtil.getDBConnection(anyBoolean())).thenReturn(getConnection());
        Assert.assertEquals(daoImpl.getAPIResources(limit, tenantId, sortOrder, expressionNodes).size(), count);
    }

    @DataProvider
    public Object[][] addAPIResourceData() {

        return new Object[][]{
                {"AddAPITest-1", TENANT_ID}
        };
    }

    @Test(dataProvider = "addAPIResourceData", priority = 2)
    public void testAddAPIResource(String postfix, int tenantId) throws Exception {

        mockStatic(IdentityDatabaseUtil.class);
        mockStatic(APIResourceManagementUtil.class);
        when(IdentityDatabaseUtil.getDBConnection(anyBoolean())).thenReturn(getConnection());
        APIResource apiResource = createAPIResource(postfix);
        APIResource createdAPIResource = daoImpl.addAPIResource(apiResource, tenantId);
        Assert.assertNotNull(createdAPIResource);
        Assert.assertTrue(createdAPIResource.getName().contains(postfix));
        Assert.assertNotNull(createdAPIResource.getId());
    }

    @DataProvider
    public Object[][] getScopesByAPIData() {
        // Define your test cases here
        return new Object[][]{
                {"GetScopesTest", TENANT_ID, 2},
                {"GetScopesTest2", INVALID_TENANT_ID, 0}
        };
    }

    @Test(dataProvider = "getScopesByAPIData", priority = 3)
    public void testGetScopesByAPI(String name, Integer tenantId, int expected) throws Exception {
        mockStatic(IdentityDatabaseUtil.class);
        mockStatic(APIResourceManagementUtil.class);


        String apiId = addAPIResourceToDB(name, getConnection(), tenantId).getId();
        when(IdentityDatabaseUtil.getDBConnection(anyBoolean())).thenReturn(getConnection());
        Assert.assertEquals(daoImpl.getScopesByAPI(apiId, TENANT_ID).size(), expected);
    }

    @DataProvider
    public Object[][] isAPIResourceExistData() {
        return new Object[][]{
                {"identifier1", TENANT_ID, true},
                {"identifier4", INVALID_TENANT_ID, false}
        };
    }

    @Test(dataProvider = "isAPIResourceExistData", priority = 4)
    public void testIsAPIResourceExist(String identifierPostFix, Integer tenantId, boolean expected) throws Exception {

        mockStatic(IdentityDatabaseUtil.class);
        mockStatic(APIResourceManagementUtil.class);

        addAPIResourceToDB(identifierPostFix, getConnection(), tenantId);
        when(IdentityDatabaseUtil.getDBConnection(anyBoolean())).thenReturn(getConnection());
        Assert.assertEquals(daoImpl.isAPIResourceExist(APIRESOURCE_IDENTIFIER + identifierPostFix, TENANT_ID),
                expected);
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
        mockStatic(IdentityDatabaseUtil.class);
        mockStatic(APIResourceManagementUtil.class);

        APIResource createdAPIResource = addAPIResourceToDB("testIsAPIResourceExistById", getConnection(),
                tenantId);
        String apiId = createdAPIResource.getId();
        when(IdentityDatabaseUtil.getDBConnection(anyBoolean())).thenReturn(getConnection());
        Assert.assertEquals(daoImpl.isAPIResourceExistById(apiId, TENANT_ID), expected);
    }

    @DataProvider
    public Object[][] getAPIResourceByIdData() {
        return new Object[][]{
                {TENANT_ID, true},
                {INVALID_TENANT_ID, false}
        };
    }

    @Test(dataProvider = "getAPIResourceByIdData", priority = 6)
    public void testGetAPIResourceById(Integer tenantId, boolean expected) throws Exception {

        mockStatic(IdentityDatabaseUtil.class);
        mockStatic(APIResourceManagementUtil.class);

        APIResource createdAPIResource = addAPIResourceToDB("testGetAPIResourceById", getConnection(),
                tenantId);
        String apiId = createdAPIResource.getId();
        when(IdentityDatabaseUtil.getDBConnection(anyBoolean())).thenReturn(getConnection());
        Assert.assertEquals(daoImpl.getAPIResourceById(apiId, TENANT_ID) != null, expected);
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

        mockStatic(IdentityDatabaseUtil.class);
        mockStatic(APIResourceManagementUtil.class);

        APIResource createdAPIResource = addAPIResourceToDB("testIsScopeExistById", getConnection(),
                tenantId);
        String scopeId = createdAPIResource.getScopes().get(0).getId();
        when(IdentityDatabaseUtil.getDBConnection(anyBoolean())).thenReturn(getConnection());
        Assert.assertEquals(daoImpl.isScopeExistById(scopeId, TENANT_ID), expected);
    }

    @DataProvider
    public Object[][] deleteAPIResourceByIdData() {

        return new Object[][]{
                {TENANT_ID, false}
        };
    }

    @Test(dataProvider = "deleteAPIResourceByIdData", priority = 8)
    public void testDeleteAPIResourceById(Integer tenantId, boolean expected) throws Exception {

        mockStatic(IdentityDatabaseUtil.class);
        mockStatic(APIResourceManagementUtil.class);

        String apiId = addAPIResourceToDB("testDeleteAPIResourceById", getConnection(), tenantId).getId();
        Connection connection = getConnection();
        when(IdentityDatabaseUtil.getDBConnection(anyBoolean())).thenReturn(connection);
        PowerMockito.doAnswer((Answer<Void>) invocation -> {
            connection.commit();
            return null;
        }).when(IdentityDatabaseUtil.class, "commitTransaction", any(Connection.class));
        daoImpl.deleteAPIResourceById(apiId, tenantId);
        when(IdentityDatabaseUtil.getDBConnection(anyBoolean())).thenReturn(getConnection());
        Assert.assertEquals(daoImpl.isAPIResourceExistById(apiId, tenantId), expected);
    }

    @DataProvider
    public Object[][] isScopeExistByNameData() {
        return new Object[][]{
                {TENANT_ID, "testIsScopeExistByName", true},
                {INVALID_TENANT_ID, "nonExistentScopeName", false}
        };
    }

    @Test(dataProvider = "isScopeExistByNameData", priority = 9)
    public void testIsScopeExistByName(Integer tenantId, String scopeName, boolean expected) throws Exception {
        mockStatic(IdentityDatabaseUtil.class);
        mockStatic(APIResourceManagementUtil.class);

        addAPIResourceToDB(scopeName, getConnection(), tenantId);
        when(IdentityDatabaseUtil.getDBConnection(anyBoolean())).thenReturn(getConnection());

        Assert.assertEquals(daoImpl.isScopeExistByName(TEST_SCOPE_1 + scopeName, TENANT_ID), expected);
    }

    @DataProvider
    public Object[][] getScopeByNameAndTenantIdData() {
        String scopeName = "testGetScopeByNameAndTenantId";
        return new Object[][]{
                {TENANT_ID, scopeName, scopeName}
        };
    }

    @Test(dataProvider = "getScopeByNameAndTenantIdData", priority = 10)
    public void testGetScopeByNameAndTenantId(Integer tenantId, String scopeName, String expectedName)
            throws Exception {
        mockStatic(IdentityDatabaseUtil.class);
        mockStatic(APIResourceManagementUtil.class);

        addAPIResourceToDB(scopeName, getConnection(), tenantId);
        when(IdentityDatabaseUtil.getDBConnection(anyBoolean())).thenReturn(getConnection());
        Scope scope = daoImpl.getScopeByNameAndTenantId(TEST_SCOPE_1 + scopeName, tenantId);
        Assert.assertEquals(scope.getName(), TEST_SCOPE_1 + expectedName);
    }

    @Test(priority = 11)
    public void testAddScopes() throws Exception {

        mockStatic(IdentityDatabaseUtil.class);
        mockStatic(APIResourceManagementUtil.class);

        APIResource apiResource = addAPIResourceToDB("testAddScopes", getConnection(), TENANT_ID);
        String apiId = apiResource.getId();
        List<Scope> scopes = Arrays.asList(createScope("scope1"), createScope("scope2"));

        Connection connection = getConnection();
        when(IdentityDatabaseUtil.getDBConnection(anyBoolean())).thenReturn(connection);
        PowerMockito.doAnswer((Answer<Void>) invocation -> {
            connection.commit();
            return null;
        }).when(IdentityDatabaseUtil.class, "commitTransaction", any(Connection.class));
        daoImpl.addScopes(scopes, apiId, TENANT_ID);

        for (Scope scope : scopes) {
            when(IdentityDatabaseUtil.getDBConnection(anyBoolean())).thenReturn(getConnection());
            Assert.assertTrue(daoImpl.isScopeExistByName(scope.getName(), TENANT_ID));
        }
    }

    @Test(priority = 12)
    public void testDeleteAllScopes() throws Exception {

        mockStatic(IdentityDatabaseUtil.class);
        mockStatic(APIResourceManagementUtil.class);

        APIResource apiResource = addAPIResourceToDB("testDeleteAllScopes", getConnection(), TENANT_ID);
        String apiId = apiResource.getId();
        Connection connection = getConnection();
        when(IdentityDatabaseUtil.getDBConnection(anyBoolean())).thenReturn(connection);
        PowerMockito.doAnswer((Answer<Void>) invocation -> {
            connection.commit();
            return null;
        }).when(IdentityDatabaseUtil.class, "commitTransaction", any(Connection.class));

        daoImpl.deleteAllScopes(apiId, TENANT_ID);
        for (Scope scope : apiResource.getScopes()) {
            when(IdentityDatabaseUtil.getDBConnection(anyBoolean())).thenReturn(getConnection());
            Assert.assertFalse(daoImpl.isScopeExistById(scope.getId(), TENANT_ID));
        }
    }

    @Test(priority = 13)
    public void testDeleteScope() throws Exception {

        mockStatic(IdentityDatabaseUtil.class);
        mockStatic(APIResourceManagementUtil.class);

        APIResource apiResource = addAPIResourceToDB("testDeleteScope", getConnection(), TENANT_ID);
        String apiId = apiResource.getId();
        String scopeName = apiResource.getScopes().get(0).getName(); // Assuming there's at least one scope

        Connection connection = getConnection();
        when(IdentityDatabaseUtil.getDBConnection(anyBoolean())).thenReturn(connection);
        PowerMockito.doAnswer((Answer<Void>) invocation -> {
            connection.commit();
            return null;
        }).when(IdentityDatabaseUtil.class, "commitTransaction", any(Connection.class));

        // Testing the deleteScope method with the created API resource's ID and scope ID
        daoImpl.deleteScope(apiId, scopeName, TENANT_ID);

        // Checking whether the scope is deleted
        when(IdentityDatabaseUtil.getDBConnection(anyBoolean())).thenReturn(getConnection());
        Assert.assertFalse(daoImpl.isScopeExistByName(scopeName, TENANT_ID));
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
     * Add API resource to the database.
     *
     * @param namePostFix Postfix to be appended to each API resource and scope information.
     * @param connection  Database connection.
     * @return API resource.
     * @throws Exception Error when adding API resource.
     */
    private APIResource addAPIResourceToDB(String namePostFix, Connection connection, int tenantId) throws Exception {

        APIResource apiResource = createAPIResource(namePostFix);
        when(IdentityDatabaseUtil.getDBConnection(anyBoolean())).thenReturn(connection);
        PowerMockito.doAnswer((Answer<Void>) invocation -> {
            connection.commit();
            return null;
        }).when(IdentityDatabaseUtil.class, "commitTransaction", any(Connection.class));
        return daoImpl.addAPIResource(apiResource, tenantId);
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
}
