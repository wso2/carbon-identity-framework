package org.wso2.carbon.idp.mgt.dao;

import org.apache.commons.dbcp.BasicDataSource;
import org.apache.commons.lang.StringUtils;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.testng.PowerMockTestCase;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.wso2.carbon.base.MultitenantConstants;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.context.internal.CarbonContextDataHolder;
import org.wso2.carbon.identity.application.common.model.*;
import org.wso2.carbon.identity.core.model.ExpressionNode;
import org.wso2.carbon.identity.core.util.IdentityDatabaseUtil;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;
import org.wso2.carbon.identity.core.util.IdentityUtil;
import org.wso2.carbon.idp.mgt.IdentityProviderManagementClientException;
import org.wso2.carbon.idp.mgt.IdentityProviderManagementException;
import org.wso2.carbon.idp.mgt.cache.*;
import org.wso2.carbon.idp.mgt.model.ConnectedAppsResult;
import org.wso2.carbon.idp.mgt.util.IdPManagementConstants;

import javax.sql.DataSource;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

import static org.mockito.Matchers.*;
import static org.powermock.api.mockito.PowerMockito.*;
import static org.testng.Assert.*;
import static org.wso2.carbon.idp.mgt.util.IdPManagementConstants.RESET_PROVISIONING_ENTITIES_ON_CONFIG_UPDATE;

/**
 * Unit tests for CacheBackedIdPManagementDAO.
 */
@PrepareForTest({IdentityDatabaseUtil.class, DataSource.class, IdentityTenantUtil.class, IdentityUtil.class,
        CarbonContext.class})
public class CacheBackedIdPMgtDAOTest extends PowerMockTestCase {

    private static final String DB_NAME = "test";

    private static final Integer SAMPLE_TENANT_ID = -1234;

    private static final Integer SAMPLE_TENANT_ID2 = 1;

    private static final String TENANT_DOMAIN = "carbon.super";

    private static Map<String, BasicDataSource> dataSourceMap = new HashMap<>();

    private CacheBackedIdPMgtDAO cacheBackedIdPMgtDAO;
    private IdPManagementDAO idPManagementDAO;

    private void initiateH2Database(String databaseName, String scriptPath) throws Exception {

        BasicDataSource dataSource = new BasicDataSource();
        dataSource.setDriverClassName("org.h2.Driver");
        dataSource.setUsername("username");
        dataSource.setPassword("password");
        dataSource.setUrl("jdbc:h2:mem:test" + databaseName);
        try (Connection connection = dataSource.getConnection()) {
            connection.createStatement().executeUpdate("RUNSCRIPT FROM '" + scriptPath + "'");
        }
        dataSourceMap.put(databaseName, dataSource);
    }

    private static String getFilePath(String fileName) {

        if (StringUtils.isNotBlank(fileName)) {
            return Paths.get(System.getProperty("user.dir"), "src", "test", "resources", "dbscripts", fileName)
                    .toString();
        }
        throw new IllegalArgumentException("DB Script file name cannot be empty");
    }

    public static void closeH2Database() throws Exception {

        BasicDataSource dataSource = dataSourceMap.get(DB_NAME);
        if (dataSource != null) {
            dataSource.close();
        }
    }

    private static Connection getConnection(String database) throws SQLException {

        if (dataSourceMap.get(database) != null) {
            return dataSourceMap.get(database).getConnection();
        }
        throw new RuntimeException("No datasource initiated for database: " + database);
    }

    @BeforeMethod
    public void setup() throws Exception {

        idPManagementDAO = new IdPManagementDAO();
        cacheBackedIdPMgtDAO = new CacheBackedIdPMgtDAO(idPManagementDAO);
        initiateH2Database(DB_NAME, getFilePath("h2.sql"));
        mockStatic(IdentityTenantUtil.class);
        when(IdentityTenantUtil.getTenantDomain(anyInt())).thenReturn(TENANT_DOMAIN);
    }

    @AfterMethod
    public void tearDown() throws Exception {

        closeH2Database();
    }

    @DataProvider
    public Object[][] getIdPsData() {

        return new Object[][]{
                {SAMPLE_TENANT_ID, TENANT_DOMAIN, 2},
                {SAMPLE_TENANT_ID2, TENANT_DOMAIN, 1},
                {4, TENANT_DOMAIN, 0}
        };
    }

    @Test(dataProvider = "getIdPsData")
    public void testGetIdPs(int tenantId, String tenantDomain, int idpCount) throws Exception {

        mockStatic(IdentityDatabaseUtil.class);

        try (Connection connection = getConnection(DB_NAME)) {

            when(IdentityDatabaseUtil.getDBConnection(anyBoolean())).thenReturn(connection);
            when(IdentityDatabaseUtil.getDataSource()).thenReturn(dataSourceMap.get(DB_NAME));
            addTestIdps();

            List<IdentityProvider> idps1 = cacheBackedIdPMgtDAO.getIdPs(connection, tenantId, tenantDomain);
            assertEquals(idps1.size(), idpCount);
            List<IdentityProvider> idps2 = cacheBackedIdPMgtDAO.getIdPs(null, tenantId, tenantDomain);
            assertEquals(idps2.size(), idpCount);
        }
    }

    @Test(dataProvider = "getIdPsData")
    public void testGetIdPsException(int tenantId, String tenantDomain, int idpCount) throws Exception {

        mockStatic(IdentityDatabaseUtil.class);

        try (Connection connection = getConnection(DB_NAME)) {
            when(IdentityDatabaseUtil.getDBConnection(anyBoolean())).thenReturn(connection);
            when(IdentityDatabaseUtil.getDataSource()).thenReturn(dataSourceMap.get(DB_NAME));
            addTestIdps();
        }

        assertThrows(IdentityProviderManagementException.class, () ->
                cacheBackedIdPMgtDAO.getIdPs(null, tenantId, tenantDomain));
    }

    @DataProvider
    public Object[][] getIdPsSearchData() {

        return new Object[][]{
                {SAMPLE_TENANT_ID, TENANT_DOMAIN, "testIdP1", 1},
                {SAMPLE_TENANT_ID2, TENANT_DOMAIN, "testIdP3", 1},
                {SAMPLE_TENANT_ID, TENANT_DOMAIN, "", 2},
                {SAMPLE_TENANT_ID, TENANT_DOMAIN, "test*", 2},
                {SAMPLE_TENANT_ID, TENANT_DOMAIN, "????IdP*", 2},
                {SAMPLE_TENANT_ID, TENANT_DOMAIN, "tes_I*", 2},
                {SAMPLE_TENANT_ID, TENANT_DOMAIN, "*1", 1},
                {SAMPLE_TENANT_ID, TENANT_DOMAIN, "Notexist", 0},
        };
    }

    @Test(dataProvider = "getIdPsSearchData")
    public void testGetIdPsSearch(int tenantId, String tenantDomain, String filter, int resultCount) throws Exception {

        mockStatic(IdentityDatabaseUtil.class);

        try (Connection connection = getConnection(DB_NAME)) {
            when(IdentityDatabaseUtil.getDBConnection(anyBoolean())).thenReturn(connection);
            when(IdentityDatabaseUtil.getDBConnection()).thenReturn(connection);
            when(IdentityDatabaseUtil.getDataSource()).thenReturn(dataSourceMap.get(DB_NAME));
            addTestIdps();

            List<IdentityProvider> idps1 = cacheBackedIdPMgtDAO.getIdPsSearch(connection, tenantId, tenantDomain, filter);
            assertEquals(idps1.size(), resultCount);
            List<IdentityProvider> idps2 = cacheBackedIdPMgtDAO.getIdPsSearch(null, tenantId, tenantDomain, filter);
            assertEquals(idps2.size(), resultCount);
        }
    }

    @Test(dataProvider = "getIdPsSearchData")
    public void testGetIdPsSearchException(int tenantId, String tenantDomain, String filter, int resultCount)
            throws Exception {

        mockStatic(IdentityDatabaseUtil.class);

        try (Connection connection = getConnection(DB_NAME)) {
            when(IdentityDatabaseUtil.getDBConnection(anyBoolean())).thenReturn(connection);
            when(IdentityDatabaseUtil.getDBConnection()).thenReturn(connection);
            when(IdentityDatabaseUtil.getDataSource()).thenReturn(dataSourceMap.get(DB_NAME));
            addTestIdps();
        }

        assertThrows(IdentityProviderManagementException.class, () ->
                cacheBackedIdPMgtDAO.getIdPsSearch(null, tenantId, tenantDomain, filter));
    }

    @DataProvider
    public Object[][] getPaginatedIdPsSearchData() {

        ExpressionNode expressionNode = new ExpressionNode();
        List<ExpressionNode> expressionNodeList = new ArrayList<>();
        expressionNodeList.add(expressionNode);

        List<String> attributes1 = Arrays.asList("id", "name", "description", "isEnabled", "image", "isPrimary");
        List<String> attributes2 = Arrays.asList("homeRealmIdentifier", "isFederationHub", "certificate", "alias",
                "claims", "roles", "federatedAuthenticators", "provisioning");

        return new Object[][]{
                {SAMPLE_TENANT_ID, expressionNodeList, 2, 0, "ASC", "NAME", attributes1, 2},
                {SAMPLE_TENANT_ID, expressionNodeList, 1, 1, "ASC", "NAME", attributes2, 1},
                {SAMPLE_TENANT_ID, expressionNodeList, 2, 0, "DESC", "NAME", attributes1, 2},
        };
    }

    @Test(dataProvider = "getPaginatedIdPsSearchData")
    public void testGetPaginatedIdPsSearch(int tenantId, List<ExpressionNode> expressionNodes, int limit,
                                           int offset, String order, String sortBy, List<String> attributes,
                                           int count) throws Exception {

        mockStatic(IdentityDatabaseUtil.class);

        try (Connection connection = getConnection(DB_NAME)) {
            when(IdentityDatabaseUtil.getDBConnection(anyBoolean())).thenReturn(connection);
            when(IdentityDatabaseUtil.getDataSource()).thenReturn(dataSourceMap.get(DB_NAME));
            addTestIdps();

            List<IdentityProvider> idps = cacheBackedIdPMgtDAO.getPaginatedIdPsSearch(tenantId, expressionNodes, limit,
                    offset, order, sortBy, attributes);
            assertEquals(idps.size(), count);
        }
    }

    @DataProvider
    public Object[][] getPaginatedIdPsSearchExceptionData() {

        ExpressionNode expressionNode = new ExpressionNode();
        List<ExpressionNode> expressionNodes = new ArrayList<>();
        expressionNodes.add(expressionNode);
        List<String> attributes = Arrays.asList("WrongAttribute");

        return new Object[][]{
                {SAMPLE_TENANT_ID, expressionNodes, 2, 0, "ASC", "NAME", attributes}
        };
    }

    @Test(dataProvider = "getPaginatedIdPsSearchExceptionData")
    public void testGetPaginatedIdPsSearchException(int tenantId, List<ExpressionNode> expressionNodes, int limit,
                                                    int offset, String order, String sortBy,
                                                    List<String> attributes) throws Exception {

        mockStatic(IdentityDatabaseUtil.class);

        try (Connection connection = getConnection(DB_NAME)) {
            when(IdentityDatabaseUtil.getDBConnection(anyBoolean())).thenReturn(connection);
            when(IdentityDatabaseUtil.getDBConnection()).thenReturn(connection);
            when(IdentityDatabaseUtil.getDataSource()).thenReturn(dataSourceMap.get(DB_NAME));
            addTestIdps();

            assertThrows(IdentityProviderManagementClientException.class, () -> cacheBackedIdPMgtDAO.getPaginatedIdPsSearch
                    (tenantId, expressionNodes, limit, offset, order, sortBy, attributes));
        }
    }

    @DataProvider
    public Object[][] getTotalIdPCountData() {

        ExpressionNode expressionNode1 = new ExpressionNode();
        List<ExpressionNode> expressionNodesList1 = new ArrayList<>();
        expressionNodesList1.add(expressionNode1);
        ExpressionNode expressionNode2 = new ExpressionNode();
        expressionNode2.setAttributeValue("name");
        expressionNode2.setOperation("sw");
        expressionNode2.setValue("test");
        List<ExpressionNode> expressionNodesList2 = new ArrayList<>();
        expressionNodesList2.add(expressionNode2);

        return new Object[][]{
                {SAMPLE_TENANT_ID, expressionNodesList1, 2},
                {SAMPLE_TENANT_ID, expressionNodesList2, 2},
                {SAMPLE_TENANT_ID2, expressionNodesList1, 1},
        };
    }

    @Test(dataProvider = "getTotalIdPCountData")
    public void testGetTotalIdPCount(int tenantId, List<ExpressionNode> expressionNodes, int count) throws Exception {

        mockStatic(IdentityDatabaseUtil.class);

        try (Connection connection = getConnection(DB_NAME)) {
            when(IdentityDatabaseUtil.getDBConnection(anyBoolean())).thenReturn(connection);
            when(IdentityDatabaseUtil.getDataSource()).thenReturn(dataSourceMap.get(DB_NAME));
            addTestIdps();

            int resultCount = cacheBackedIdPMgtDAO.getTotalIdPCount(tenantId, expressionNodes);
            assertEquals(resultCount, count);
        }
    }

    @Test(dataProvider = "getTotalIdPCountData")
    public void testGetTotalIdPCountException(int tenantId, List<ExpressionNode> expressionNodes, int count)
            throws Exception {

        mockStatic(IdentityDatabaseUtil.class);

        try (Connection connection = getConnection(DB_NAME)) {
            when(IdentityDatabaseUtil.getDBConnection(anyBoolean())).thenReturn(connection);
            when(IdentityDatabaseUtil.getDataSource()).thenReturn(dataSourceMap.get(DB_NAME));
            addTestIdps();
        }

        assertThrows(IdentityProviderManagementException.class, () ->
                cacheBackedIdPMgtDAO.getTotalIdPCount(tenantId, expressionNodes));
    }

    @DataProvider
    public Object[][] getIdPByNameData() {

        return new Object[][]{
                {"testIdP1", SAMPLE_TENANT_ID, true},
                {"testIdP3", SAMPLE_TENANT_ID2, true},
                {"notExist", SAMPLE_TENANT_ID, false},
        };
    }

    @Test(dataProvider = "getIdPByNameData")
    public void testGetIdPByName(String idpName, int tenantId, boolean isExist) throws Exception {

        mockStatic(IdentityDatabaseUtil.class);

        try (Connection connection = getConnection(DB_NAME)) {
            when(IdentityDatabaseUtil.getDBConnection(anyBoolean())).thenReturn(connection);
            when(IdentityDatabaseUtil.getDataSource()).thenReturn(dataSourceMap.get(DB_NAME));
            addTestIdps();

            IdentityProvider idpResult = cacheBackedIdPMgtDAO.getIdPByName(connection, idpName, tenantId, TENANT_DOMAIN);

            IdentityProvider idpResult2 = new IdentityProvider();
            if (isExist){
                IdPCacheByName idPCacheByName = IdPCacheByName.getInstance();
                IdPNameCacheKey cacheKey = new IdPNameCacheKey(idpName);
                IdPCacheEntry entry = idPCacheByName.getValueFromCache(cacheKey, TENANT_DOMAIN);
                idpResult2 = entry.getIdentityProvider();
            }

            IdentityProvider idpFromCache = cacheBackedIdPMgtDAO.getIdPByName(connection,idpName, tenantId, TENANT_DOMAIN);

            if (isExist) {
                assertEquals(idpFromCache.getIdentityProviderName(), idpName, "'getIdPByName' method fails");
                assertEquals(idpFromCache, idpResult2, "'getIdPByName' method fails");
            } else {
                assertNull(idpResult, "'getIdPByName' method fails");
                assertNull(idpFromCache, "'getIdPByName' method fails");
            }
        }
    }

    @DataProvider
    public Object[][] getIdPByIdData() {

        return new Object[][]{
                {"testIdP1", 1, SAMPLE_TENANT_ID, true},
                {"testIdP3", 3, SAMPLE_TENANT_ID2, true},
                {"notExist", 4, SAMPLE_TENANT_ID, false},
        };
    }

    @Test(dataProvider = "getIdPByIdData")
    public void testGetIdPById(String idpName, int idpId, int tenantId, boolean isExist) throws Exception {

        mockStatic(IdentityDatabaseUtil.class);

        try (Connection connection = getConnection(DB_NAME)) {
            when(IdentityDatabaseUtil.getDBConnection(anyBoolean())).thenReturn(connection);
            when(IdentityDatabaseUtil.getDataSource()).thenReturn(dataSourceMap.get(DB_NAME));
            addTestIdps();

            IdentityProvider idpResult = cacheBackedIdPMgtDAO.getIdPById(connection, idpId, tenantId, TENANT_DOMAIN);

            IdentityProvider idpResult2 = new IdentityProvider();
            if (isExist){
                IdPCacheByName idPCacheByName = IdPCacheByName.getInstance();
                IdPNameCacheKey cacheKey = new IdPNameCacheKey(idpName);
                IdPCacheEntry entry = idPCacheByName.getValueFromCache(cacheKey, TENANT_DOMAIN);
                idpResult2 = entry.getIdentityProvider();
            }

            if (isExist) {
                assertEquals(idpResult.getIdentityProviderName(), idpName, "'getIDPbyId' method fails");
                assertEquals(idpResult2.getIdentityProviderName(), idpName, "'getIDPbyId' method fails");
                assertEquals(idpResult2, idpResult);
            } else {
                assertNull(idpResult, "'getIDPbyId' method fails");
            }
        }
    }

    @DataProvider
    public Object[][] getIDPbyResourceIdData() {

        return new Object[][]{
                {"testIdP1", SAMPLE_TENANT_ID, true},
                {"testIdP3", SAMPLE_TENANT_ID2, true},
                {"notExist", SAMPLE_TENANT_ID, false},
        };
    }

    @Test(dataProvider = "getIDPbyResourceIdData")
    public void testGetIDPbyResourceId(String idpName, int tenantId, boolean isExist) throws Exception {

        mockStatic(IdentityDatabaseUtil.class);

        try (Connection connection = getConnection(DB_NAME)) {
            when(IdentityDatabaseUtil.getDBConnection(anyBoolean())).thenReturn(connection);
            when(IdentityDatabaseUtil.getDataSource()).thenReturn(dataSourceMap.get(DB_NAME));
            addTestIdps();

            String uuid = "";
            IdentityProvider idpResult2 = new IdentityProvider();

            if (isExist) {
                IdentityProvider idPResult1 = idPManagementDAO.getIdPByName(connection, idpName, tenantId, TENANT_DOMAIN);
                uuid = idPResult1.getResourceId();
                cacheBackedIdPMgtDAO.addIdPCache(idPResult1, TENANT_DOMAIN);

                IdPCacheByResourceId idPCacheByResourceId = IdPCacheByResourceId.getInstance();
                IdPResourceIdCacheKey cacheKey = new IdPResourceIdCacheKey(uuid);
                IdPCacheEntry entry = idPCacheByResourceId.getValueFromCache(cacheKey, TENANT_DOMAIN);
                idpResult2 = entry.getIdentityProvider();

            }

            IdentityProvider idpFromCache = cacheBackedIdPMgtDAO.getIdPByResourceId(uuid, tenantId, TENANT_DOMAIN);
            if (isExist) {
                assertEquals(idpFromCache.getIdentityProviderName(), idpName, "'getIDPbyResourceId' method fails");
                assertEquals(idpResult2.getIdentityProviderName(), idpName, "'getIDPbyResourceId' method fails");
                assertEquals(idpFromCache, idpResult2, "'getIDPbyResourceId' method fails");
            } else {
                assertNull(idpFromCache, "'getIDPbyResourceId' method fails");
            }
        }
    }

    @DataProvider
    public Object[][] getIDPNameByResourceIdData() {

        return new Object[][]{
                {"testIdP1", SAMPLE_TENANT_ID},
                {"testIdP3", SAMPLE_TENANT_ID2}
        };
    }

    @Test(dataProvider = "getIDPNameByResourceIdData")
    public void testGetIdPNameByResourceId(String idpName, int tenantId) throws Exception {

        mockStatic(IdentityDatabaseUtil.class);

        try (Connection connection = getConnection(DB_NAME)) {
            when(IdentityDatabaseUtil.getDBConnection(anyBoolean())).thenReturn(connection);
            when(IdentityDatabaseUtil.getDBConnection()).thenReturn(connection);
            when(IdentityDatabaseUtil.getDataSource()).thenReturn(dataSourceMap.get(DB_NAME));

            addTestIdps();

            String uuid = "";
            IdentityProvider idPResult1 = idPManagementDAO.getIdPByName(connection, idpName, tenantId, TENANT_DOMAIN);
            uuid = idPResult1.getResourceId();
            cacheBackedIdPMgtDAO.addIdPCache(idPResult1, TENANT_DOMAIN);

            IdPCacheByResourceId idPCacheByResourceId = IdPCacheByResourceId.getInstance();
            IdPResourceIdCacheKey cacheKey = new IdPResourceIdCacheKey(uuid);
            IdPCacheEntry entry = idPCacheByResourceId.getValueFromCache(cacheKey, TENANT_DOMAIN);
            IdentityProvider idpResult2 = entry.getIdentityProvider();

            IdentityProvider idpFromCache = cacheBackedIdPMgtDAO.getIdPByResourceId(uuid, tenantId, TENANT_DOMAIN);

            mockStatic(CarbonContext.class);
            CarbonContext carbonContext = mock(CarbonContext.class);
            when(CarbonContext.getThreadLocalCarbonContext()).thenReturn(carbonContext);
            when(CarbonContext.getThreadLocalCarbonContext().getTenantDomain()).thenReturn(TENANT_DOMAIN);
            String name = cacheBackedIdPMgtDAO.getIdPNameByResourceId(uuid);

            assertEquals(idpResult2.getIdentityProviderName(), idpName, "'getIDPNameByResourceId' method fails");
            assertEquals(idpFromCache.getIdentityProviderName(), idpName, "'getIDPNameByResourceId' method fails");
            assertEquals(name, idpName);

        }
    }

    @DataProvider
    public Object[][] getIdPByAuthenticatorPropertyValueWithoutAuthenticatorData() {

        return new Object[][]{
                {SAMPLE_TENANT_ID, "testIdP1", "Property1", "value1", true},
                {SAMPLE_TENANT_ID, "testIdP1", "Property2", "value2", true},
                {SAMPLE_TENANT_ID, "NotExist", "Null", "Null", false},
        };
    }

    @Test(dataProvider = "getIdPByAuthenticatorPropertyValueWithoutAuthenticatorData")
    public void testGetIdPByAuthenticatorPropertyValueWithoutAuthenticatorData(int tenantId, String idpName, String property,
                                                                               String value, boolean isExist) throws Exception {

        mockStatic(IdentityDatabaseUtil.class);

        try (Connection connection = getConnection(DB_NAME)) {
            when(IdentityDatabaseUtil.getDBConnection(anyBoolean())).thenReturn(connection);
            when(IdentityDatabaseUtil.getDataSource()).thenReturn(dataSourceMap.get(DB_NAME));
            addTestIdps();

            IdentityProvider idpResult = cacheBackedIdPMgtDAO.getIdPByAuthenticatorPropertyValue(connection, property,
                    value, tenantId, TENANT_DOMAIN);

            IdentityProvider idpResult2 = new IdentityProvider();
            if (isExist){
                IdPCacheByName idPCacheByName = IdPCacheByName.getInstance();
                IdPNameCacheKey cacheKey = new IdPNameCacheKey(idpName);
                IdPCacheEntry entry = idPCacheByName.getValueFromCache(cacheKey, TENANT_DOMAIN);
                idpResult2 = entry.getIdentityProvider();
            }

            if (isExist) {
                assertEquals(idpResult.getIdentityProviderName(), idpName, "''getIdPByAuthenticatorPropertyValue' method fails");
                assertEquals(idpResult2.getIdentityProviderName(), idpName, "''getIdPByAuthenticatorPropertyValue' method fails");
                assertEquals(idpResult2, idpResult);
            } else {
                assertNull(idpResult, "'getIdPByAuthenticatorPropertyValue' method fails");
            }
        }
    }

    @DataProvider
    public Object[][] getIdPByAuthenticatorPropertyValueData() {

        return new Object[][]{
                {SAMPLE_TENANT_ID, "testIdP1", "Property1", "value1", "Name", true},
                {SAMPLE_TENANT_ID, "testIdP1", "Property2", "value2", "Name", true},
                {SAMPLE_TENANT_ID, "NotExist", "Null", "Null", "Null", false},
        };
    }

    @Test(dataProvider = "getIdPByAuthenticatorPropertyValueData")
    public void testGetIdPByAuthenticatorPropertyValue(int tenantId, String idpName, String property, String value,
                                                       String authenticator, boolean isExist) throws Exception {

        mockStatic(IdentityDatabaseUtil.class);

        try (Connection connection = getConnection(DB_NAME)) {
            when(IdentityDatabaseUtil.getDBConnection(anyBoolean())).thenReturn(connection);
            when(IdentityDatabaseUtil.getDataSource()).thenReturn(dataSourceMap.get(DB_NAME));
            addTestIdps();

            IdentityProvider idpResult = cacheBackedIdPMgtDAO.getIdPByAuthenticatorPropertyValue(connection, property,
                    value, authenticator, tenantId, TENANT_DOMAIN);

            IdentityProvider idpResult2 = new IdentityProvider();
            if (isExist){
                IdPCacheByName idPCacheByName = IdPCacheByName.getInstance();
                IdPNameCacheKey cacheKey = new IdPNameCacheKey(idpName);
                IdPCacheEntry entry = idPCacheByName.getValueFromCache(cacheKey, TENANT_DOMAIN);
                idpResult2 = entry.getIdentityProvider();
            }

            if (isExist) {
                assertEquals(idpResult.getIdentityProviderName(), idpName, "''getIdPByAuthenticatorPropertyValue' method fails");
                assertEquals(idpResult2.getIdentityProviderName(), idpName, "''getIdPByAuthenticatorPropertyValue' method fails");
                assertEquals(idpResult2, idpResult);
            } else {
                assertNull(idpResult, "'getIdPByAuthenticatorPropertyValue' method fails");
            }
        }
    }

    @DataProvider
    public Object[][] getIdPByRealmIdData() {

        return new Object[][]{
                {"testIdP1", "1", SAMPLE_TENANT_ID, true},
                {"testIdP2", "2", SAMPLE_TENANT_ID, true},
                {"notExist", "4", SAMPLE_TENANT_ID2, false},
        };
    }

    @Test(dataProvider = "getIdPByRealmIdData")
    public void testGetIdPRealmId(String idpName, String realmId, int tenantId, boolean isExist) throws Exception {

        mockStatic(IdentityDatabaseUtil.class);

        try (Connection connection = getConnection(DB_NAME)) {
            when(IdentityDatabaseUtil.getDBConnection(anyBoolean())).thenReturn(connection);
            when(IdentityDatabaseUtil.getDataSource()).thenReturn(dataSourceMap.get(DB_NAME));
            addTestIdps();

            IdentityProvider idpResult = cacheBackedIdPMgtDAO.getIdPByRealmId(realmId, tenantId, TENANT_DOMAIN);

            IdentityProvider idpResult2 = new IdentityProvider();
            if (isExist) {
                IdPCacheByHRI idPCacheByHRI = IdPCacheByHRI.getInstance();
                IdPHomeRealmIdCacheKey cacheKey = new IdPHomeRealmIdCacheKey(realmId);
                IdPCacheEntry entry = idPCacheByHRI.getValueFromCache(cacheKey, TENANT_DOMAIN);
                idpResult2 = entry.getIdentityProvider();
            }

            IdentityProvider idpFromCache = cacheBackedIdPMgtDAO.getIdPByName(connection, idpName, tenantId, TENANT_DOMAIN);

            if (isExist) {
                assertEquals(idpFromCache.getIdentityProviderName(), idpName, "'getIdPByRealmId' method fails");
                assertEquals(idpFromCache, idpResult2, "'getIdPByRealmId' method fails");
            } else {
                assertNull(idpResult, "'getIdPByRealmId' method fails");
                assertNull(idpFromCache, "'getIdPByRealmId' method fails");
            }
        }
    }

    @DataProvider
    public Object[][] addIdPData() {

        // Initialize Test Identity Provider 1.
        IdentityProvider idp1 = new IdentityProvider();
        idp1.setIdentityProviderName("testIdP1");
        idp1.setEnable(true);
        idp1.setPrimary(true);
        idp1.setFederationHub(true);
        idp1.setCertificate("");

        RoleMapping roleMapping1 = new RoleMapping(new LocalRole("1", "LocalRole1"), "Role1");
        RoleMapping roleMapping2 = new RoleMapping(new LocalRole("2", "LocalRole2"), "Role2");
        PermissionsAndRoleConfig permissionsAndRoleConfig = new PermissionsAndRoleConfig();
        permissionsAndRoleConfig.setIdpRoles(new String[]{"Role1", "Role2"});
        permissionsAndRoleConfig.setRoleMappings(new RoleMapping[]{roleMapping1, roleMapping2});
        idp1.setPermissionAndRoleConfig(permissionsAndRoleConfig);

        FederatedAuthenticatorConfig federatedAuthenticatorConfig = new FederatedAuthenticatorConfig();
        federatedAuthenticatorConfig.setDisplayName("DisplayName1");
        federatedAuthenticatorConfig.setName("Name");
        federatedAuthenticatorConfig.setEnabled(true);
        Property property1 = new Property();
        property1.setName("Property1");
        property1.setValue("value1");
        property1.setConfidential(false);
        Property property2 = new Property();
        property2.setName("Property2");
        property2.setValue("value2");
        property2.setConfidential(true);
        federatedAuthenticatorConfig.setProperties(new Property[]{property1, property2});
        idp1.setFederatedAuthenticatorConfigs(new FederatedAuthenticatorConfig[]{federatedAuthenticatorConfig});

        ProvisioningConnectorConfig provisioningConnectorConfig1 = new ProvisioningConnectorConfig();
        provisioningConnectorConfig1.setName("ProvisiningConfig1");
        provisioningConnectorConfig1.setProvisioningProperties(new Property[]{property1});
        ProvisioningConnectorConfig provisioningConnectorConfig2 = new ProvisioningConnectorConfig();
        provisioningConnectorConfig2.setName("ProvisiningConfig2");
        provisioningConnectorConfig2.setProvisioningProperties(new Property[]{property2});
        provisioningConnectorConfig2.setEnabled(true);
        provisioningConnectorConfig2.setBlocking(true);
        idp1.setProvisioningConnectorConfigs(new ProvisioningConnectorConfig[]{provisioningConnectorConfig1,
                provisioningConnectorConfig2});

        ClaimConfig claimConfig = new ClaimConfig();
        claimConfig.setLocalClaimDialect(false);
        claimConfig.setRoleClaimURI("Country");
        claimConfig.setUserClaimURI("Country");
        ClaimMapping claimMapping = ClaimMapping.build("http://wso2.org/claims/country", "Country", "", true);
        claimConfig.setClaimMappings(new ClaimMapping[]{claimMapping});
        Claim remoteClaim = new Claim();
        remoteClaim.setClaimId(0);
        remoteClaim.setClaimUri("Country");
        claimConfig.setIdpClaims(new Claim[]{remoteClaim});
        idp1.setClaimConfig(claimConfig);

        // Initialize Test Identity Provider 2.
        IdentityProvider idp2 = new IdentityProvider();
        idp2.setIdentityProviderName("testIdP2");

        ClaimConfig claimConfig2 = new ClaimConfig();
        claimConfig2.setLocalClaimDialect(true);
        claimConfig2.setRoleClaimURI("http://wso2.org/claims/role");
        claimConfig2.setUserClaimURI("http://wso2.org/claims/fullname");
        ClaimMapping claimMapping2 = new ClaimMapping();
        Claim localClaim2 = new Claim();
        localClaim2.setClaimId(0);
        localClaim2.setClaimUri("http://wso2.org/claims/fullname");
        claimMapping2.setLocalClaim(localClaim2);
        claimConfig2.setClaimMappings(new ClaimMapping[]{claimMapping2});
        idp2.setClaimConfig(claimConfig2);

        // Initialize Test Identity Provider 3.
        IdentityProvider idp3 = new IdentityProvider();
        idp3.setIdentityProviderName("testIdP3");

        return new Object[][]{
                // IDP with PermissionsAndRoleConfig,FederatedAuthenticatorConfigs,ProvisioningConnectorConfigs,Claims.
                {idp1, SAMPLE_TENANT_ID},
                // IDP with Local Cliam Dialect ClaimConfigs.
                {idp2, SAMPLE_TENANT_ID},
                // IDP with Only name.
                {idp3, SAMPLE_TENANT_ID2},
        };
    }

    @Test(dataProvider = "addIdPData")
    public void testAddIdP(Object identityProvider, int tenantId) throws Exception {

        mockStatic(IdentityDatabaseUtil.class);

        try (Connection connection = getConnection(DB_NAME)) {
            when(IdentityDatabaseUtil.getDBConnection(anyBoolean())).thenReturn(connection);
            when(IdentityDatabaseUtil.getDataSource()).thenReturn(dataSourceMap.get(DB_NAME));
            cacheBackedIdPMgtDAO.addIdP(((IdentityProvider) identityProvider), tenantId, TENANT_DOMAIN);

            String query = IdPManagementConstants.SQLQueries.GET_IDP_BY_NAME_SQL;
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setInt(1, tenantId);
            statement.setInt(2, MultitenantConstants.SUPER_TENANT_ID);
            statement.setString(3, ((IdentityProvider) identityProvider).getIdentityProviderName());
            ResultSet resultSet = statement.executeQuery();
            String resultName = "";
            if (resultSet.next()) {
                resultName = resultSet.getString("NAME");
            }
            statement.close();
            assertEquals(resultName, ((IdentityProvider) identityProvider).getIdentityProviderName());
        }
    }

    @DataProvider
    public Object[][] updateIdPData() {

        // Initialize Test Identity Provider 1.
        IdentityProvider idp1 = new IdentityProvider();
        idp1.setIdentityProviderName("testIdP1");
        idp1.setHomeRealmId("1");
        idp1.setEnable(true);
        idp1.setPrimary(true);
        idp1.setFederationHub(true);
        idp1.setCertificate("");

        FederatedAuthenticatorConfig federatedAuthenticatorConfig = new FederatedAuthenticatorConfig();
        federatedAuthenticatorConfig.setDisplayName("DisplayName1");
        federatedAuthenticatorConfig.setName("Name");
        federatedAuthenticatorConfig.setEnabled(true);
        Property property1 = new Property();
        property1.setName("Property1");
        property1.setValue("value1");
        property1.setConfidential(true);
        federatedAuthenticatorConfig.setProperties(new Property[]{property1});
        idp1.setFederatedAuthenticatorConfigs(new FederatedAuthenticatorConfig[]{federatedAuthenticatorConfig});

        ProvisioningConnectorConfig provisioningConnectorConfig = new ProvisioningConnectorConfig();
        provisioningConnectorConfig.setName("ProvisiningConfig1");
        provisioningConnectorConfig.setProvisioningProperties(new Property[]{property1});
        idp1.setProvisioningConnectorConfigs(new ProvisioningConnectorConfig[]{provisioningConnectorConfig});

        // Initialize Test Identity Provider 2.
        IdentityProvider idp2 = new IdentityProvider();
        idp2.setIdentityProviderName("testIdP2");
        idp2.setHomeRealmId("2");

        ClaimConfig claimConfig2 = new ClaimConfig();
        claimConfig2.setLocalClaimDialect(true);
        claimConfig2.setRoleClaimURI("http://wso2.org/claims/role");
        claimConfig2.setUserClaimURI("http://wso2.org/claims/fullname");
        ClaimMapping claimMapping2 = new ClaimMapping();
        Claim localClaim2 = new Claim();
        localClaim2.setClaimId(0);
        localClaim2.setClaimUri("http://wso2.org/claims/fullname");
        claimMapping2.setLocalClaim(localClaim2);
        claimConfig2.setClaimMappings(new ClaimMapping[]{claimMapping2});
        idp2.setClaimConfig(claimConfig2);

        // Initialize Test Identity Provider 3.
        IdentityProvider idp3 = new IdentityProvider();
        idp3.setIdentityProviderName("testIdP3");

        // Initialize New Test Identity Provider 1.
        IdentityProvider idp1New = new IdentityProvider();
        idp1New.setIdentityProviderName("testIdP1New");
        idp1New.setEnable(true);
        idp1New.setPrimary(true);
        idp1New.setFederationHub(true);
        idp1New.setCertificate("");

        RoleMapping newRoleMapping1 = new RoleMapping();
        newRoleMapping1.setRemoteRole("Role1New");
        newRoleMapping1.setLocalRole(new LocalRole("1", "LocalRole1"));
        RoleMapping newRoleMapping2 = new RoleMapping();
        newRoleMapping2.setRemoteRole("Role2New");
        newRoleMapping2.setLocalRole(new LocalRole("2", "LocalRole2"));

        PermissionsAndRoleConfig newPermissionsAndRoleConfig = new PermissionsAndRoleConfig();
        newPermissionsAndRoleConfig.setIdpRoles(new String[]{"Role1New", "Role2New"});
        newPermissionsAndRoleConfig.setRoleMappings(new RoleMapping[]{newRoleMapping1, newRoleMapping2});
        idp1New.setPermissionAndRoleConfig(newPermissionsAndRoleConfig);

        FederatedAuthenticatorConfig newFederatedAuthenticatorConfig = new FederatedAuthenticatorConfig();
        newFederatedAuthenticatorConfig.setDisplayName("DisplayName1New");
        newFederatedAuthenticatorConfig.setName("Name");
        newFederatedAuthenticatorConfig.setEnabled(true);
        Property property1New = new Property();
        property1New.setName("Property1New");
        property1New.setValue("value1New");
        property1New.setConfidential(false);
        Property property2New = new Property();
        property2New.setName("Property2New");
        property2New.setValue("value2New");
        property2New.setConfidential(false);
        newFederatedAuthenticatorConfig.setProperties(new Property[]{property1New, property2New});
        idp1New.setFederatedAuthenticatorConfigs(new FederatedAuthenticatorConfig[]{newFederatedAuthenticatorConfig});

        ProvisioningConnectorConfig newProvisioningConnectorConfig1 = new ProvisioningConnectorConfig();
        newProvisioningConnectorConfig1.setName("ProvisiningConfig1");
        newProvisioningConnectorConfig1.setProvisioningProperties(new Property[]{property1New});
        ProvisioningConnectorConfig newProvisioningConnectorConfig2 = new ProvisioningConnectorConfig();
        newProvisioningConnectorConfig2.setName("ProvisiningConfig2");
        newProvisioningConnectorConfig2.setProvisioningProperties(new Property[]{property2New});
        newProvisioningConnectorConfig2.setEnabled(true);
        newProvisioningConnectorConfig2.setBlocking(true);
        idp1New.setProvisioningConnectorConfigs(new ProvisioningConnectorConfig[]{newProvisioningConnectorConfig1,
                newProvisioningConnectorConfig2});

        JustInTimeProvisioningConfig justInTimeProvisioningConfig = new JustInTimeProvisioningConfig();
        justInTimeProvisioningConfig.setProvisioningUserStore("PRIMARY");
        idp1New.setJustInTimeProvisioningConfig(justInTimeProvisioningConfig);

        ClaimConfig newClaimConfig = new ClaimConfig();
        newClaimConfig.setLocalClaimDialect(false);
        newClaimConfig.setRoleClaimURI("Country");
        newClaimConfig.setUserClaimURI("Country");
        ClaimMapping claimMapping = ClaimMapping.build("http://wso2.org/claims/country", "Country", "", true);
        Claim remoteClaim = new Claim();
        remoteClaim.setClaimId(0);
        remoteClaim.setClaimUri("Country");
        newClaimConfig.setClaimMappings(new ClaimMapping[]{claimMapping});
        newClaimConfig.setIdpClaims(new Claim[]{remoteClaim});
        idp1New.setClaimConfig(newClaimConfig);

        // Initialize New Test Identity Provider 2.
        IdentityProvider idp2New = new IdentityProvider();
        idp2New.setIdentityProviderName("testIdP2New");

        // Initialize New Test Identity Provider 3.
        IdentityProvider idp3New = new IdentityProvider();
        idp3New.setIdentityProviderName("testIdP3New");

        return new Object[][]{
                // Update PermissionsAndRoleConfig,FederatedAuthenticatorConfig,ProvisioningConnectorConfig,ClaimConfig.
                {idp1, idp1New, SAMPLE_TENANT_ID},
                // Update name, LocalClaimDialect, ClaimConfig.
                {idp2, idp2New, SAMPLE_TENANT_ID},
                // Update name.
                {idp3, idp3New, SAMPLE_TENANT_ID2},
        };
    }

    @Test(dataProvider = "updateIdPData")
    public void testUpdateIdP(Object oldIdP, Object newIdP, int tenantId) throws Exception {

        mockStatic(IdentityDatabaseUtil.class);
        mockStatic(IdentityUtil.class);

        try (Connection connection = getConnection(DB_NAME)) {
            when(IdentityDatabaseUtil.getDBConnection(anyBoolean())).thenReturn(connection);
            when(IdentityDatabaseUtil.getDBConnection()).thenReturn(connection);
            when(IdentityUtil.getProperty(RESET_PROVISIONING_ENTITIES_ON_CONFIG_UPDATE)).thenReturn("false");
            when(IdentityDatabaseUtil.getDataSource()).thenReturn(dataSourceMap.get(DB_NAME));

            addTestIdps();
            cacheBackedIdPMgtDAO.updateIdP((IdentityProvider) newIdP, (IdentityProvider) oldIdP, tenantId, TENANT_DOMAIN);

            String newIdpName = ((IdentityProvider)  newIdP).getIdentityProviderName();
            IdentityProvider idpResult = cacheBackedIdPMgtDAO.getIdPByName(connection, newIdpName, tenantId, TENANT_DOMAIN);
            assertEquals(idpResult.getIdentityProviderName(), newIdpName);
        }
    }

    @Test(dataProvider = "updateIdPData")
    public void testGetUpdatedIdPByResourceId(Object oldIdP, Object newIdP, int tenantId) throws Exception {

        mockStatic(IdentityDatabaseUtil.class);
        mockStatic(IdentityUtil.class);

        try (Connection connection = getConnection(DB_NAME)) {
            when(IdentityDatabaseUtil.getDBConnection(anyBoolean())).thenReturn(connection);
            when(IdentityDatabaseUtil.getDBConnection()).thenReturn(connection);
            when(IdentityUtil.getProperty(RESET_PROVISIONING_ENTITIES_ON_CONFIG_UPDATE)).thenReturn("false");
            when(IdentityDatabaseUtil.getDataSource()).thenReturn(dataSourceMap.get(DB_NAME));

            addTestIdps();
            String oldIdPName = ((IdentityProvider) oldIdP).getIdentityProviderName();
            IdentityProvider idp1 = cacheBackedIdPMgtDAO.getIdPByName(connection, oldIdPName, tenantId, TENANT_DOMAIN);
            assertEquals(idp1.getIdentityProviderName(), oldIdPName);

            String resourceId = idp1.getResourceId();
            cacheBackedIdPMgtDAO.addIdPCache(idp1, TENANT_DOMAIN);

            IdPCacheByResourceId idPCacheByResourceId = IdPCacheByResourceId.getInstance();
            IdPResourceIdCacheKey cacheKey = new IdPResourceIdCacheKey(resourceId);
            IdPCacheEntry entry = idPCacheByResourceId.getValueFromCache(cacheKey, TENANT_DOMAIN);
            IdentityProvider idp2 = entry.getIdentityProvider();
            assertEquals(idp2.getIdentityProviderName(), oldIdPName);

            cacheBackedIdPMgtDAO.updateIdP((IdentityProvider) newIdP, (IdentityProvider) oldIdP, tenantId, TENANT_DOMAIN);

            String newIdPName = ((IdentityProvider) newIdP).getIdentityProviderName();
            IdentityProvider idp3 = cacheBackedIdPMgtDAO.getIdPByName(connection, newIdPName, tenantId, TENANT_DOMAIN);
            assertEquals(idp3.getIdentityProviderName(), newIdPName);

            String resourceId2 = idp3.getResourceId();
            IdentityProvider idp4 = cacheBackedIdPMgtDAO.getUpdatedIdPByResourceId(resourceId2, tenantId, TENANT_DOMAIN);

            IdPResourceIdCacheKey cacheKey2 = new IdPResourceIdCacheKey(resourceId2);
            IdPCacheEntry entry2 = idPCacheByResourceId.getValueFromCache(cacheKey2, TENANT_DOMAIN);
            IdentityProvider idp5 = entry2.getIdentityProvider();
            assertEquals(idp5.getIdentityProviderName(), idp4.getIdentityProviderName());
        }
    }

    @Test(dataProvider = "updateIdPData")
    public void testUpdateIdPException(Object oldIdp, Object newIdp, int tenantId) throws Exception {

        mockStatic(IdentityDatabaseUtil.class);
        mockStatic(IdentityUtil.class);

        try (Connection connection = getConnection(DB_NAME)) {
            when(IdentityDatabaseUtil.getDBConnection(anyBoolean())).thenReturn(connection);
            when(IdentityDatabaseUtil.getDBConnection()).thenReturn(connection);
            when(IdentityUtil.getProperty(RESET_PROVISIONING_ENTITIES_ON_CONFIG_UPDATE)).thenReturn("false");

            assertThrows(IdentityProviderManagementException.class, () ->
                    cacheBackedIdPMgtDAO.updateIdP((IdentityProvider) newIdp, ((IdentityProvider) oldIdp), tenantId,
                            TENANT_DOMAIN));
        }
    }

    @DataProvider
    public Object[][] deleteIdPData() {

        return new Object[][]{
                {"testIdP1", SAMPLE_TENANT_ID},
                {"testIdP3", SAMPLE_TENANT_ID2},
        };
    }

    @Test(dataProvider = "deleteIdPData")
    public void testDeleteIdP(String idpName, int tenantId) throws Exception {

        mockStatic(IdentityDatabaseUtil.class);

        try (Connection connection = getConnection(DB_NAME)) {
            when(IdentityDatabaseUtil.getDBConnection(anyBoolean())).thenReturn(connection);
            when(IdentityDatabaseUtil.getDBConnection()).thenReturn(connection);
            when(IdentityDatabaseUtil.getDataSource()).thenReturn(dataSourceMap.get(DB_NAME));
            addTestIdps();

            cacheBackedIdPMgtDAO.deleteIdP(idpName, tenantId, TENANT_DOMAIN);
            int resultSize = getIdPCount(connection, idpName, tenantId);
            assertEquals(resultSize, 0, "'deleteIdP' method fails");
        }
    }

    @DataProvider
    public Object[][] deleteIdPsData() {

        return new Object[][]{
                {SAMPLE_TENANT_ID},
                {SAMPLE_TENANT_ID2},
        };
    }

    @Test(dataProvider = "deleteIdPsData")
    public void testDeleteIdPs(int tenantId) throws Exception {

        mockStatic(IdentityDatabaseUtil.class);

        try (Connection connection = getConnection(DB_NAME)) {
            when(IdentityDatabaseUtil.getDBConnection(anyBoolean())).thenReturn(connection);
            when(IdentityDatabaseUtil.getDBConnection()).thenReturn(connection);
            when(IdentityDatabaseUtil.getDataSource()).thenReturn(dataSourceMap.get(DB_NAME));
            addTestIdps();

            idPManagementDAO.deleteIdPs(tenantId);
        }

        try (Connection connection = getConnection(DB_NAME)) {
            String query = IdPManagementConstants.SQLQueries.GET_IDPS_SQL;
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setInt(1, tenantId);
            statement.setInt(2, MultitenantConstants.SUPER_TENANT_ID);
            ResultSet resultSet = statement.executeQuery();
            int resultSize = 0;
            if (resultSet.next()) {
                resultSize = resultSet.getRow();
            }
            statement.close();
            assertEquals(resultSize, 0);
        }
    }

    @Test(dataProvider = "deleteIdPData")
    public void testDeleteIdPByResourceId(String idpName, int tenantId) throws Exception {

        mockStatic(IdentityDatabaseUtil.class);

        try (Connection connection = getConnection(DB_NAME)) {
            when(IdentityDatabaseUtil.getDBConnection(anyBoolean())).thenReturn(connection);
            when(IdentityDatabaseUtil.getDBConnection()).thenReturn(connection);
            when(IdentityDatabaseUtil.getDataSource()).thenReturn(dataSourceMap.get(DB_NAME));
            addTestIdps();

            String uuid = cacheBackedIdPMgtDAO.getIdPByName(connection, idpName, tenantId, TENANT_DOMAIN).getResourceId();
            cacheBackedIdPMgtDAO.deleteIdPByResourceId(uuid, tenantId, TENANT_DOMAIN);
            int resultSize = getIdPCount(connection, idpName, tenantId);
            assertEquals(resultSize, 0, "'deleteIdPByResourceId' method fails");

            IdPCacheByResourceId idPCacheByResourceId = IdPCacheByResourceId.getInstance();
            IdPResourceIdCacheKey cacheKey = new IdPResourceIdCacheKey(uuid);
            IdPCacheEntry entry = idPCacheByResourceId.getValueFromCache(cacheKey, TENANT_DOMAIN);
            assertNull(entry, "'deleteIdPByResourceId' method fails");
        }
    }

    @Test(dataProvider = "deleteIdPData")
    public void testForceDeleteIdPByResourceId(String idpName, int tenantId) throws Exception {

        mockStatic(IdentityDatabaseUtil.class);

        try (Connection connection = getConnection(DB_NAME)) {
            when(IdentityDatabaseUtil.getDBConnection(anyBoolean())).thenReturn(connection);
            when(IdentityDatabaseUtil.getDBConnection()).thenReturn(connection);
            when(IdentityDatabaseUtil.getDataSource()).thenReturn(dataSourceMap.get(DB_NAME));
            addTestIdps();

            String uuid = cacheBackedIdPMgtDAO.getIdPByName(connection, idpName, tenantId, TENANT_DOMAIN).getResourceId();
            cacheBackedIdPMgtDAO.forceDeleteIdPByResourceId(uuid, tenantId, TENANT_DOMAIN);
            int resultSize = getIdPCount(connection, idpName, tenantId);
            assertEquals(resultSize, 0, "'forceDeleteIdPByResourceId' method fails");

            IdPCacheByResourceId idPCacheByResourceId = IdPCacheByResourceId.getInstance();
            IdPResourceIdCacheKey cacheKey = new IdPResourceIdCacheKey(uuid);
            IdPCacheEntry entry = idPCacheByResourceId.getValueFromCache(cacheKey, TENANT_DOMAIN);
            assertNull(entry, "'forceDeleteIdPByResourceId' method fails");
        }
    }

    @DataProvider
    public Object[][] addIdPCacheData() {

        return new Object[][]{
                {"testIdP1", SAMPLE_TENANT_ID, true},
                {"testIdP3", SAMPLE_TENANT_ID2, true},
                {"notExist", SAMPLE_TENANT_ID, false},
        };
    }

    @Test(dataProvider = "addIdPCacheData")
    public void testAddIdPCache(String idpName, int tenantId, boolean isExist) throws Exception{

        mockStatic(IdentityDatabaseUtil.class);

        try (Connection connection = getConnection(DB_NAME)) {

            when(IdentityDatabaseUtil.getDBConnection(anyBoolean())).thenReturn(connection);
            when(IdentityDatabaseUtil.getDBConnection()).thenReturn(connection);
            when(IdentityDatabaseUtil.getDataSource()).thenReturn(dataSourceMap.get(DB_NAME));
            addTestIdps();

            IdentityProvider idpResult = idPManagementDAO.getIdPByName(connection, idpName, tenantId, TENANT_DOMAIN);

            if (isExist) {
                cacheBackedIdPMgtDAO.addIdPCache(idpResult, TENANT_DOMAIN);

                IdPCacheByName idPCacheByName = IdPCacheByName.getInstance();
                IdPNameCacheKey cacheKey = new IdPNameCacheKey(idpName);
                IdPCacheEntry entry = idPCacheByName.getValueFromCache(cacheKey, TENANT_DOMAIN);
                IdentityProvider idpCacheResult = entry.getIdentityProvider();

                assertEquals(idpCacheResult.getIdentityProviderName(), idpName, "'addIdpCache' method failed!");
            }
        }
    }

    @DataProvider
    public Object[][] clearIdPCacheData() {

        return new Object[][]{
                {"testIdP1", SAMPLE_TENANT_ID, true},
                {"testIdP3", SAMPLE_TENANT_ID2, true},
                {"notExist", SAMPLE_TENANT_ID, false},
        };
    }

    @Test(dataProvider = "clearIdPCacheData")
    public void testClearIdpCache(String idpName, int tenantId, boolean isExist) throws Exception {

        mockStatic(IdentityDatabaseUtil.class);

        try (Connection connection = getConnection(DB_NAME)) {

            when(IdentityDatabaseUtil.getDBConnection(anyBoolean())).thenReturn(connection);
            when(IdentityDatabaseUtil.getDBConnection()).thenReturn(connection);
            when(IdentityDatabaseUtil.getDataSource()).thenReturn(dataSourceMap.get(DB_NAME));
            addTestIdps();

            IdentityProvider idpResult = cacheBackedIdPMgtDAO.getIdPByName(connection, idpName, tenantId, TENANT_DOMAIN);

            if (idpResult != null) {

                cacheBackedIdPMgtDAO.clearIdpCache(idpName, tenantId, TENANT_DOMAIN);
                IdPCacheByName idPCacheByName = IdPCacheByName.getInstance();
                IdPNameCacheKey cacheKey = new IdPNameCacheKey(idpName);
                IdPCacheEntry entry = idPCacheByName.getValueFromCache(cacheKey, TENANT_DOMAIN);

                assertNull(entry, "'clearIdpCache' method failed!");
            }
        }
    }

    @DataProvider
    public Object[][] deleteTenantRoleData() {

        return new Object[][]{
                {SAMPLE_TENANT_ID, "Role1"},
                {SAMPLE_TENANT_ID, "Role2"},
        };
    }

    @Test(dataProvider = "deleteTenantRoleData")
    public void testDeleteTenantRole(int tenantId, String role) throws Exception {

        mockStatic(IdentityDatabaseUtil.class);

        try (Connection connection = getConnection(DB_NAME)) {

            when(IdentityDatabaseUtil.getDBConnection(anyBoolean())).thenReturn(connection);
            when(IdentityDatabaseUtil.getDBConnection()).thenReturn(connection);
            when(IdentityDatabaseUtil.getDataSource()).thenReturn(dataSourceMap.get(DB_NAME));

            addTestIdps();
            cacheBackedIdPMgtDAO.deleteTenantRole(tenantId, role, TENANT_DOMAIN);

            List<IdentityProvider> idps1 = cacheBackedIdPMgtDAO.getIdPs(connection, tenantId, TENANT_DOMAIN);
            for (IdentityProvider idp : idps1) {
                IdPCacheByName idPCacheByName = IdPCacheByName.getInstance();
                IdPNameCacheKey cacheKey = new IdPNameCacheKey(idp.getIdentityProviderName());
                IdPCacheEntry entry = idPCacheByName.getValueFromCache(cacheKey, TENANT_DOMAIN);
                assertNull(entry, "Idp not removed from cache!");
            }
        }

        try (Connection connection = getConnection(DB_NAME)) {
            String query = "SELECT * FROM IDP_ROLE WHERE TENANT_ID=? AND ROLE=?";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setInt(1, tenantId);
            statement.setString(2, role);
            ResultSet resultSet = statement.executeQuery();
            int resultSize = 0;
            if (resultSet.next()) {
                resultSize = resultSet.getRow();
            }
            statement.close();
            assertEquals(resultSize, 0);
        }
    }

    @DataProvider
    public Object[][] renameTenantRoleData() {

        return new Object[][]{
                {SAMPLE_TENANT_ID, "NewLocalRole1", "LocalRole1", 1},
                {SAMPLE_TENANT_ID, "NewLocalRole2", "LocalRole2", 1},
                {SAMPLE_TENANT_ID, "NewLocalRole2", "2", 0},
        };
    }

    @Test(dataProvider = "renameTenantRoleData")
    public void testRenameTenantRoleData(int tenantId, String newRole, String oldRole, int count) throws Exception {

        mockStatic(IdentityDatabaseUtil.class);

        try (Connection connection = getConnection(DB_NAME)) {

            when(IdentityDatabaseUtil.getDBConnection(anyBoolean())).thenReturn(connection);
            when(IdentityDatabaseUtil.getDBConnection()).thenReturn(connection);
            when(IdentityDatabaseUtil.getDataSource()).thenReturn(dataSourceMap.get(DB_NAME));

            addTestIdps();
            cacheBackedIdPMgtDAO.renameTenantRole(newRole, oldRole, tenantId, TENANT_DOMAIN);

            List<IdentityProvider> idps1 = cacheBackedIdPMgtDAO.getIdPs(connection, tenantId, TENANT_DOMAIN);
            for (IdentityProvider idp : idps1) {
                IdPCacheByName idPCacheByName = IdPCacheByName.getInstance();
                IdPNameCacheKey cacheKey = new IdPNameCacheKey(idp.getIdentityProviderName());
                IdPCacheEntry entry = idPCacheByName.getValueFromCache(cacheKey, TENANT_DOMAIN);
                assertNull(entry, "Idp not removed from cache!");
            }

            String query = "SELECT * FROM IDP_ROLE_MAPPING WHERE TENANT_ID=? AND LOCAL_ROLE=?";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setInt(1, tenantId);
            statement.setString(2, newRole);
            ResultSet resultSet = statement.executeQuery();
            int resultSize = 0;
            if (resultSet.next()) {
                resultSize = resultSet.getRow();
            }
            statement.close();
            assertEquals(resultSize, count);
        }
    }

    @DataProvider
    public Object[][] isIdPAvailableForAuthenticatorPropertyData() {

        return new Object[][]{
                {SAMPLE_TENANT_ID, "Name", "Property1", "value1", true},
                {SAMPLE_TENANT_ID, "Address", "Property2", "value2", false},
        };
    }

    @Test(dataProvider = "isIdPAvailableForAuthenticatorPropertyData")
    public void testIsIdPAvailableForAuthenticatorProperty(int tenantId, String authenticator, String property,
                                                           String value, boolean isAvailable) throws Exception {

        mockStatic(IdentityDatabaseUtil.class);

        try (Connection connection = getConnection(DB_NAME)) {

            when(IdentityDatabaseUtil.getDBConnection(anyBoolean())).thenReturn(connection);
            when(IdentityDatabaseUtil.getDBConnection()).thenReturn(connection);
            when(IdentityDatabaseUtil.getDataSource()).thenReturn(dataSourceMap.get(DB_NAME));
            addTestIdps();

            boolean availabilityResult = cacheBackedIdPMgtDAO.isIdPAvailableForAuthenticatorProperty(authenticator,
                    property, value, tenantId);
            assertEquals(availabilityResult, isAvailable);
        }
    }

    @Test(dataProvider = "isIdPAvailableForAuthenticatorPropertyData")
    public void testIsIdPAvailableForAuthenticatorPropertyException(int tenantId, String authenticator, String property,
                                                                    String value, boolean isAvailable)
            throws Exception {

        mockStatic(IdentityDatabaseUtil.class);

        try (Connection connection = getConnection(DB_NAME)) {
            when(IdentityDatabaseUtil.getDBConnection(anyBoolean())).thenReturn(connection);
            when(IdentityDatabaseUtil.getDBConnection()).thenReturn(connection);
            when(IdentityDatabaseUtil.getDataSource()).thenReturn(dataSourceMap.get(DB_NAME));
            addTestIdps();
        }
        assertThrows(IdentityProviderManagementException.class, () ->
                cacheBackedIdPMgtDAO.isIdPAvailableForAuthenticatorProperty(authenticator, property, value, tenantId));
    }

    @DataProvider
    public Object[][] getConnectedApplicationsData() {

        return new Object[][]{
                {"testIdP1", SAMPLE_TENANT_ID, 2, 2, 0},
                {"testIdP2", SAMPLE_TENANT_ID, 1, 1, 0},
        };
    }

    @Test(dataProvider = "getConnectedApplicationsData")
    public void testGetConnectedApplications(String idpName, int tenantId, int limit, int offset, int count)
            throws  Exception{

        mockStatic(IdentityDatabaseUtil.class);

        try (Connection connection = getConnection(DB_NAME)) {

            when(IdentityDatabaseUtil.getDBConnection(anyBoolean())).thenReturn(connection);
            when(IdentityDatabaseUtil.getDBConnection()).thenReturn(connection);
            when(IdentityDatabaseUtil.getDataSource()).thenReturn(dataSourceMap.get(DB_NAME));
            addTestIdps();

            IdentityProvider idp = cacheBackedIdPMgtDAO.getIdPByName(connection, idpName, tenantId, TENANT_DOMAIN);
            String uuid = idp.getResourceId();

            ConnectedAppsResult result = cacheBackedIdPMgtDAO.getConnectedApplications(uuid, limit, offset);
            assertEquals(result.getTotalAppCount(), count);
        }
    }

    @DataProvider
    public Object[][] getIdPNameByMetadataPropertyData() {

        return new Object[][]{
                {"testIdP1", "idpPropertyName", "idpPropertyValue", SAMPLE_TENANT_ID},
                {null, "notExist", "4", SAMPLE_TENANT_ID2},
        };
    }

    @Test(dataProvider = "getIdPNameByMetadataPropertyData")
    public void testGetIdPNameByMetadataProperty(String idpName, String property, String value, int tenantId)
            throws Exception {

        mockStatic(IdentityDatabaseUtil.class);

        try (Connection connection = getConnection(DB_NAME)) {

            when(IdentityDatabaseUtil.getDBConnection(anyBoolean())).thenReturn(connection);
            when(IdentityDatabaseUtil.getDBConnection()).thenReturn(connection);
            when(IdentityDatabaseUtil.getDataSource()).thenReturn(dataSourceMap.get(DB_NAME));
            addTestIdps();

            String outputName = cacheBackedIdPMgtDAO.getIdPNameByMetadataProperty(connection, property, value, tenantId,
                    TENANT_DOMAIN);
            assertEquals(outputName, idpName, "'getIdPNameByMetadataProperty' method failed!");

            IdPCacheByMetadataProperty idPCacheByMetadataProperty = IdPCacheByMetadataProperty.getInstance();
            IdPMetadataPropertyCacheKey cacheKey = new IdPMetadataPropertyCacheKey(property, value);
            String idPNameCache = idPCacheByMetadataProperty.getValueFromCache(cacheKey, TENANT_DOMAIN);
            assertEquals(idPNameCache, idpName, "Cannot find idP in cache!");
        }
    }

    private void addTestIdps() throws IdentityProviderManagementException {

        // Initialize Test Identity Provider 1.
        IdentityProvider idp1 = new IdentityProvider();
        idp1.setIdentityProviderName("testIdP1");
        idp1.setHomeRealmId("1");
        idp1.setEnable(true);
        idp1.setPrimary(true);
        idp1.setFederationHub(true);
        idp1.setCertificate("");

        RoleMapping roleMapping1 = new RoleMapping();
        roleMapping1.setRemoteRole("Role1");
        roleMapping1.setLocalRole(new LocalRole("1", "LocalRole1"));
        RoleMapping roleMapping2 = new RoleMapping();
        roleMapping2.setRemoteRole("Role2");
        roleMapping2.setLocalRole(new LocalRole("2", "LocalRole2"));

        PermissionsAndRoleConfig permissionsAndRoleConfig = new PermissionsAndRoleConfig();
        permissionsAndRoleConfig.setIdpRoles(new String[]{"Role1", "Role2"});
        permissionsAndRoleConfig.setRoleMappings(new RoleMapping[]{roleMapping1, roleMapping2});
        idp1.setPermissionAndRoleConfig(permissionsAndRoleConfig);

        FederatedAuthenticatorConfig federatedAuthenticatorConfig = new FederatedAuthenticatorConfig();
        federatedAuthenticatorConfig.setDisplayName("DisplayName1");
        federatedAuthenticatorConfig.setName("Name");
        federatedAuthenticatorConfig.setEnabled(true);
        Property property1 = new Property();
        property1.setName("Property1");
        property1.setValue("value1");
        property1.setConfidential(true);
        Property property2 = new Property();
        property2.setName("Property2");
        property2.setValue("value2");
        property2.setConfidential(false);
        federatedAuthenticatorConfig.setProperties(new Property[]{property1, property2});
        idp1.setFederatedAuthenticatorConfigs(new FederatedAuthenticatorConfig[]{federatedAuthenticatorConfig});

        ProvisioningConnectorConfig provisioningConnectorConfig1 = new ProvisioningConnectorConfig();
        provisioningConnectorConfig1.setName("ProvisiningConfig1");
        provisioningConnectorConfig1.setProvisioningProperties(new Property[]{property1});
        ProvisioningConnectorConfig provisioningConnectorConfig2 = new ProvisioningConnectorConfig();
        provisioningConnectorConfig2.setName("ProvisiningConfig2");
        provisioningConnectorConfig2.setProvisioningProperties(new Property[]{property2});
        provisioningConnectorConfig2.setEnabled(true);
        provisioningConnectorConfig2.setBlocking(true);
        idp1.setProvisioningConnectorConfigs(new ProvisioningConnectorConfig[]{provisioningConnectorConfig1,
                provisioningConnectorConfig2});

        IdentityProviderProperty identityProviderProperty = new IdentityProviderProperty();
        identityProviderProperty.setDisplayName("idpDisplayName");
        identityProviderProperty.setName("idpPropertyName");
        identityProviderProperty.setValue("idpPropertyValue");
        idp1.setIdpProperties(new IdentityProviderProperty[]{identityProviderProperty});

        ClaimConfig claimConfig = new ClaimConfig();
        claimConfig.setLocalClaimDialect(false);
        claimConfig.setRoleClaimURI("Country");
        claimConfig.setUserClaimURI("Country");
        ClaimMapping claimMapping = ClaimMapping.build("http://wso2.org/claims/country", "Country", "", true);
        Claim remoteClaim = new Claim();
        remoteClaim.setClaimId(0);
        remoteClaim.setClaimUri("Country");
        claimConfig.setClaimMappings(new ClaimMapping[]{claimMapping});
        claimConfig.setIdpClaims(new Claim[]{remoteClaim});
        idp1.setClaimConfig(claimConfig);

        // Initialize Test Identity Provider 2.
        IdentityProvider idp2 = new IdentityProvider();
        idp2.setIdentityProviderName("testIdP2");
        idp2.setHomeRealmId("2");

        ClaimConfig claimConfig2 = new ClaimConfig();
        claimConfig2.setLocalClaimDialect(true);
        claimConfig2.setRoleClaimURI("http://wso2.org/claims/role");
        claimConfig2.setUserClaimURI("http://wso2.org/claims/fullname");
        ClaimMapping claimMapping2 = new ClaimMapping();
        Claim localClaim2 = new Claim();
        localClaim2.setClaimId(0);
        localClaim2.setClaimUri("http://wso2.org/claims/fullname");
        claimMapping2.setLocalClaim(localClaim2);
        claimConfig2.setClaimMappings(new ClaimMapping[]{claimMapping2});
        idp2.setClaimConfig(claimConfig2);

        // Initialize Test Identity Provider 3.
        IdentityProvider idp3 = new IdentityProvider();
        idp3.setIdentityProviderName("testIdP3");
        idp3.setHomeRealmId("3");

        // IDP with PermissionsAndRoleConfig, FederatedAuthenticatorConfigs, ProvisioningConnectorConfigs, ClaimConfigs.
        idPManagementDAO.addIdP(idp1, SAMPLE_TENANT_ID);
        // IDP with Local Cliam Dialect ClaimConfigs.
        idPManagementDAO.addIdP(idp2, SAMPLE_TENANT_ID);
        // IDP with Only name.
        idPManagementDAO.addIdP(idp3, SAMPLE_TENANT_ID2);

    }

    private int getIdPCount(Connection connection, String idpName, int tenantId) throws SQLException {

        String query = IdPManagementConstants.SQLQueries.GET_IDP_BY_NAME_SQL;
        PreparedStatement statement = connection.prepareStatement(query);
        statement.setInt(1, tenantId);
        statement.setInt(2, MultitenantConstants.SUPER_TENANT_ID);
        statement.setString(3, (idpName));

        ResultSet resultSet = statement.executeQuery();
        int resultSize = 0;
        if (resultSet.next()) {
            resultSize = resultSet.getRow();
        }
        statement.clearParameters();
        statement.close();
        return resultSize;
    }

}
