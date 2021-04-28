package org.wso2.carbon.idp.mgt.dao;

import org.apache.commons.dbcp.BasicDataSource;
import org.apache.commons.lang.StringUtils;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.testng.annotations.*;
import org.powermock.modules.testng.PowerMockTestCase;

import static org.mockito.Matchers.anyInt;
import static org.powermock.api.mockito.PowerMockito.spy;

import org.wso2.carbon.identity.application.common.model.*;
import org.wso2.carbon.identity.core.util.IdentityDatabaseUtil;
import org.wso2.carbon.idp.mgt.IdentityProviderManagementException;
import org.wso2.carbon.idp.mgt.util.IdPManagementConstants;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;

import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Mockito.mock;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.when;
import org.testng.Assert;
import static org.testng.Assert.*;
import static org.testng.Assert.assertEquals;


@PrepareForTest({IdentityDatabaseUtil.class, DataSource.class, IdentityTenantUtil.class})

public class IdPManagementDAOTest extends PowerMockTestCase {

    private static Map<String, BasicDataSource> dataSourceMap = new HashMap<>();
    private static final String DB_NAME = "test";
    private static final Integer SAMPLE_TENANT_ID = -1234;
    private static final Integer SAMPLE_TENANT_ID2 = 1;
    private static final String TENANT_DOMAIN = "carbon.super";

    private void initiateH2Base(String databaseName, String scriptPath) throws Exception {

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

    public static void closeH2Base() throws Exception {

        BasicDataSource dataSource = dataSourceMap.get(DB_NAME);
        if (dataSource != null) {
            dataSource.close();
        }
    }

    private static String getFilePath(String fileName) {

        if (StringUtils.isNotBlank(fileName)) {
            return Paths.get(System.getProperty("user.dir"), "src", "test", "resources", "dbscripts", fileName)
                    .toString();
        }
        throw new IllegalArgumentException("DB Script file name cannot be empty.");
    }

    private static Connection getConnection(String database) throws SQLException {

        if (dataSourceMap.get(database) != null) {
            return dataSourceMap.get(database).getConnection();
        }
        throw new RuntimeException("No datasource initiated for database: " + database);
    }

    private void prepareConnection(Connection connection1, boolean b) {
        mockStatic(IdentityDatabaseUtil.class);
        PowerMockito.when(IdentityDatabaseUtil.getDBConnection(b)).thenReturn(connection1);
    }


    @BeforeMethod
    public void setup () throws Exception {
        initiateH2Base(DB_NAME, getFilePath("h2.sql"));
    }

    @AfterMethod
    public void tearDown() throws Exception {
        closeH2Base();
    }



    @DataProvider
    public Object[][] testGetIdPsData() throws Exception {

        return new Object[][]{
                {SAMPLE_TENANT_ID, "testIdP1", "uuid1","1","1","1","1", TENANT_DOMAIN,2},
                {SAMPLE_TENANT_ID2, "testIdP2", "uuid2","0","0","0","0", TENANT_DOMAIN,1},
        };
    }

    @Test(dataProvider = "testGetIdPsData")
    public void testGetIdPs(int TENANT_ID, String IDP_NAME, String UUID, String IS_PRIMARY, String IS_FEDERATION_HUB,
                            String IS_LOCAL_CLAIM_DIALECT, String IS_ENABLED,  String tenantDomain, int resultCount) throws Exception {


        IdPManagementDAO idPManagementDAO = new IdPManagementDAO();
        mockStatic(IdentityDatabaseUtil.class);
        mockStatic(IdentityTenantUtil.class);


        try (Connection connection = getConnection(DB_NAME)) {
            when(IdentityTenantUtil.getTenantDomain(anyInt())).thenReturn(TENANT_DOMAIN);
            when(IdentityDatabaseUtil.getDBConnection(anyBoolean())).thenReturn(connection);
            addTestIdps(idPManagementDAO);

            List<IdentityProvider> idps1 = idPManagementDAO.getIdPs(connection, TENANT_ID, tenantDomain);
            assertEquals(idps1.size(), resultCount);
            List<IdentityProvider> idps2 = idPManagementDAO.getIdPs(null, TENANT_ID, tenantDomain);
            assertEquals(idps2.size(), resultCount);

        }
    }


    @DataProvider
    public Object[][] testGetIdPsSearchData() throws Exception {

        return new Object[][]{
                {SAMPLE_TENANT_ID,  TENANT_DOMAIN, "testIdP1",1},
                {SAMPLE_TENANT_ID2,  TENANT_DOMAIN, "testIdP3", 1},
                {SAMPLE_TENANT_ID,  TENANT_DOMAIN, "",2},
        };
    }

    @Test(dataProvider = "testGetIdPsSearchData")
    public void testGetIdPsSearch(int TENANT_ID,  String tenantDomain, String filter, int resultCount) throws Exception {

        mockStatic(IdentityDatabaseUtil.class);
        mockStatic(IdentityTenantUtil.class);
        IdPManagementDAO idPManagementDAO = new IdPManagementDAO();

        try (Connection connection = getConnection(DB_NAME)) {
            when(IdentityTenantUtil.getTenantDomain(anyInt())).thenReturn(TENANT_DOMAIN);
            when(IdentityDatabaseUtil.getDBConnection(anyBoolean())).thenReturn(connection);
            when(IdentityDatabaseUtil.getDBConnection()).thenReturn(connection);
            addTestIdps(idPManagementDAO);

            List<IdentityProvider> idps1 = idPManagementDAO.getIdPsSearch(connection, TENANT_ID, tenantDomain, filter);
            assertEquals(idps1.size(), resultCount);
            List<IdentityProvider> idps2 = idPManagementDAO.getIdPsSearch(null, TENANT_ID, tenantDomain, filter);
            assertEquals(idps2.size(), resultCount);
        }
    }


    @DataProvider
    public Object[][] testAddIdPData() throws Exception {

        //Initialize Test Identity Provider 1
        String[] idpRoles = {"Role1", "Role2" };
        RoleMapping rm1 = new RoleMapping();
        rm1.setRemoteRole("Role1");
        rm1.setLocalRole(new LocalRole("1","LocalRole1"));
        RoleMapping rm2 = new RoleMapping();
        rm2.setRemoteRole("Role2");
        rm2.setLocalRole(new LocalRole("2","LocalRole2"));

        RoleMapping[] roleMap = {rm1, rm2};
        PermissionsAndRoleConfig prc = new PermissionsAndRoleConfig();
        prc.setIdpRoles(idpRoles);
        prc.setRoleMappings(roleMap);

        IdentityProvider idp1 = new IdentityProvider();
        idp1.setIdentityProviderName("testIdP1");
        idp1.setEnable(true);
        idp1.setPrimary(true);
        idp1.setFederationHub(true);
        idp1.setCertificate("");
        idp1.setPermissionAndRoleConfig(prc);

        FederatedAuthenticatorConfig fac = new FederatedAuthenticatorConfig();
        fac.setDisplayName("DisplayName1");
        fac.setName("Name");
        fac.setEnabled(true);
        Property property1 = new Property();
        property1.setName("Property1");
        property1.setValue("value1");
        property1.setConfidential(false);
        Property[] propertyList = {property1};
        fac.setProperties(propertyList);
        FederatedAuthenticatorConfig[] facList = {fac};
        idp1.setFederatedAuthenticatorConfigs(facList);


        //Initialize Test Identity Provider 2
        IdentityProvider idp2 = new IdentityProvider();
        idp2.setIdentityProviderName("testIdP2");

        //Initialize Test Identity Provider 3
        IdentityProvider idp3 = new IdentityProvider();
        idp3.setIdentityProviderName("testIdP3");

        return new Object[][]{
                {idp1, SAMPLE_TENANT_ID},
                {idp2, SAMPLE_TENANT_ID},
                {idp3, SAMPLE_TENANT_ID2},
        };
    }


    @Test(dataProvider = "testAddIdPData")
    public void testAddIdP(Object identityProvider, int tenantId) throws Exception {


        mockStatic(IdentityDatabaseUtil.class);
        mockStatic(IdentityTenantUtil.class);
        IdPManagementDAO idPManagementDAO = new IdPManagementDAO();


        try (Connection connection = getConnection(DB_NAME)) {
            when(IdentityDatabaseUtil.getDBConnection(anyBoolean())).thenReturn(connection);
            when(IdentityTenantUtil.getTenantDomain(anyInt())).thenReturn(TENANT_DOMAIN);
            idPManagementDAO.addIdP(((IdentityProvider) identityProvider), tenantId);

            String query = "SELECT * FROM IDP WHERE NAME=? AND TENANT_ID=?";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setString(1, ((IdentityProvider) identityProvider).getIdentityProviderName());
            statement.setInt(2, tenantId);
            ResultSet resultSet = statement.executeQuery();
            String resultName = "";
            int resultID = 0;
            if (resultSet.next()) {
                resultID = resultSet.getInt("TENANT_ID");
                resultName = resultSet.getString("NAME");
            }
            assertEquals(resultID ,tenantId);
            assertEquals(resultName ,((IdentityProvider) identityProvider).getIdentityProviderName());

        }
    }



    @DataProvider
    public Object[][] testgetPermissionsAndRoleConfigurationData() throws Exception {

        return new Object[][]{
                {"testIdP1", 1, SAMPLE_TENANT_ID,  2},
        };
    }


    @Test(dataProvider = "testgetPermissionsAndRoleConfigurationData")
    public void testgetPermissionsAndRoleConfiguration(String idpName, int idpID, int TENANT_ID, int resultCount) throws Exception {

        mockStatic(IdentityDatabaseUtil.class);
        mockStatic(IdentityTenantUtil.class);
        IdPManagementDAO idPManagementDAO = new IdPManagementDAO();

        try (Connection connection = getConnection(DB_NAME)) {
            when(IdentityTenantUtil.getTenantDomain(anyInt())).thenReturn(TENANT_DOMAIN);
            when(IdentityDatabaseUtil.getDBConnection(anyBoolean())).thenReturn(connection);
            when(IdentityDatabaseUtil.getDBConnection()).thenReturn(connection);
            addTestIdps(idPManagementDAO);

            PermissionsAndRoleConfig pac = idPManagementDAO.getPermissionsAndRoleConfiguration(connection, idpName, idpID, TENANT_ID);
            assertEquals(pac.getIdpRoles().length, resultCount);
        }
    }


    @DataProvider
    public Object[][] testgetIdPByData() throws Exception {

        return new Object[][]{
                {"testIdP1",1,  SAMPLE_TENANT_ID, TENANT_DOMAIN, 1},
                {"testIdP2",2, SAMPLE_TENANT_ID, TENANT_DOMAIN, 1},
                {"testIdP3",3, SAMPLE_TENANT_ID2, TENANT_DOMAIN, 1},

        };
    }

    @Test(dataProvider = "testgetIdPByData")
    public void testgetIdPBy(String idpName, int idpID,  int tenantId, String tenantDomain, int resultCount) throws Exception {

        mockStatic(IdentityDatabaseUtil.class);
        mockStatic(IdentityTenantUtil.class);
        IdPManagementDAO idPManagementDAO = new IdPManagementDAO();

        try (Connection connection = getConnection(DB_NAME)) {
            when(IdentityTenantUtil.getTenantDomain(anyInt())).thenReturn(TENANT_DOMAIN);
            when(IdentityDatabaseUtil.getDBConnection(anyBoolean())).thenReturn(connection);
            when(IdentityDatabaseUtil.getDBConnection()).thenReturn(connection);
            addTestIdps(idPManagementDAO);

            IdentityProvider idpResult1 = idPManagementDAO.getIdPByName(connection, idpName, tenantId ,tenantDomain );
            assertEquals(idpResult1.getIdentityProviderName(), idpName);
            IdentityProvider idpResult2 = idPManagementDAO.getIDPbyId(connection, idpID, tenantId ,tenantDomain );
            assertEquals(idpResult2.getIdentityProviderName(), idpName);
            String uuid = idpResult1.getResourceId();
            IdentityProvider idpResult3 = idPManagementDAO.getIDPbyResourceId(connection, uuid, tenantId ,tenantDomain );
            assertEquals(idpResult3.getIdentityProviderName(), idpName);
        }
    }


    @DataProvider
    public Object[][] testUpdateIdPData() throws Exception {

        //Initialize Test Identity Provider 1
        String[] idpRoles = {"Role1", "Role2" };
        RoleMapping rm1 = new RoleMapping();
        rm1.setRemoteRole("Role1");
        rm1.setLocalRole(new LocalRole("1","LocalRole1"));
        RoleMapping rm2 = new RoleMapping();
        rm2.setRemoteRole("Role2");
        rm2.setLocalRole(new LocalRole("2","LocalRole2"));

        RoleMapping[] roleMap = {rm1, rm2};
        PermissionsAndRoleConfig prc = new PermissionsAndRoleConfig();
        prc.setIdpRoles(idpRoles);
        prc.setRoleMappings(roleMap);

        IdentityProvider idp1 = new IdentityProvider();
        idp1.setIdentityProviderName("testIdP1");
        idp1.setEnable(true);
        idp1.setPrimary(true);
        idp1.setFederationHub(true);
        idp1.setCertificate("");
        idp1.setPermissionAndRoleConfig(prc);

        FederatedAuthenticatorConfig fac = new FederatedAuthenticatorConfig();
        fac.setDisplayName("DisplayName1");
        fac.setName("Name");
        fac.setEnabled(true);
        Property property1 = new Property();
        property1.setName("Property1");
        property1.setValue("value1");
        property1.setConfidential(false);
        Property[] propertyList = {property1};
        fac.setProperties(propertyList);
        FederatedAuthenticatorConfig[] facList = {fac};
        idp1.setFederatedAuthenticatorConfigs(facList);


        //Initialize Test Identity Provider 2
        IdentityProvider idp2 = new IdentityProvider();
        idp2.setIdentityProviderName("testIdP2");

        //Initialize Test Identity Provider 3
        IdentityProvider idp3 = new IdentityProvider();
        idp3.setIdentityProviderName("testIdP3");

        //INITIALIZE NEW IDPS

        //Initialize New Test Identity Provider 1
        String[] idpRolesNew = {"Role1New", "Role2New" };
        RoleMapping rm1New = new RoleMapping();
        rm1New.setRemoteRole("Role1New");
        rm1New.setLocalRole(new LocalRole("1","LocalRole1"));
        RoleMapping rm2New = new RoleMapping();
        rm2New.setRemoteRole("Role2");
        rm2New.setLocalRole(new LocalRole("2","LocalRole2"));

        RoleMapping[] roleMapNew = {rm1, rm2};
        PermissionsAndRoleConfig prcNew = new PermissionsAndRoleConfig();
        prcNew.setIdpRoles(idpRolesNew);
        prcNew.setRoleMappings(roleMapNew);

        IdentityProvider idp1New = new IdentityProvider();
        idp1New.setIdentityProviderName("testIdP1New");
        idp1New.setEnable(true);
        idp1New.setPrimary(true);
        idp1New.setFederationHub(true);
        idp1New.setCertificate("");
        idp1New.setPermissionAndRoleConfig(prc);

        FederatedAuthenticatorConfig facNew = new FederatedAuthenticatorConfig();
        facNew.setDisplayName("DisplayName1New");
        facNew.setName("NameNew");
        facNew.setEnabled(true);
        Property property1New = new Property();
        property1New.setName("Property1New");
        property1New.setValue("value1New");
        property1New.setConfidential(false);
        Property[] propertyListNew = {property1};
        facNew.setProperties(propertyListNew);
        FederatedAuthenticatorConfig[] facListNew = {facNew};
        idp1New.setFederatedAuthenticatorConfigs(facListNew);


        //Initialize New Test Identity Provider 2
        IdentityProvider idp2New = new IdentityProvider();
        idp2New.setIdentityProviderName("testIdP2New");

        //Initialize New Test Identity Provider 3
        IdentityProvider idp3New = new IdentityProvider();
        idp3New.setIdentityProviderName("testIdP3New");

        return new Object[][]{
                {idp1, idp1New, SAMPLE_TENANT_ID},
                {idp2, idp2New, SAMPLE_TENANT_ID},
                {idp3, idp3New, SAMPLE_TENANT_ID2},
        };
    }

    @Test(dataProvider = "testUpdateIdPData")
    public void testUpdateIdP(Object oldIdP, Object newIdP, int tenantId) throws Exception {


        mockStatic(IdentityDatabaseUtil.class);
        mockStatic(IdentityTenantUtil.class);
        IdPManagementDAO idPManagementDAO = new IdPManagementDAO();

        try (Connection connection = getConnection(DB_NAME)) {
            when(IdentityDatabaseUtil.getDBConnection(anyBoolean())).thenReturn(connection);
            when(IdentityDatabaseUtil.getDBConnection()).thenReturn(connection);
            when(IdentityTenantUtil.getTenantDomain(anyInt())).thenReturn(TENANT_DOMAIN);
            addTestIdps(idPManagementDAO);
            idPManagementDAO.updateIdP( (IdentityProvider) newIdP, ((IdentityProvider) oldIdP), tenantId);

            IdentityProvider idpResult1 = idPManagementDAO.getIdPByName(connection, ((IdentityProvider) newIdP).getIdentityProviderName(), tenantId ,TENANT_DOMAIN );
            assertEquals(idpResult1.getIdentityProviderName(), ((IdentityProvider) newIdP).getIdentityProviderName());
        }
    }


    @Test(dataProvider = "testAddIdPData")
    public void testDeleteIdP(Object identityProvider, int tenantId) throws Exception {

        mockStatic(IdentityDatabaseUtil.class);
        mockStatic(IdentityTenantUtil.class);
        IdPManagementDAO idPManagementDAO = new IdPManagementDAO();

        try (Connection connection = getConnection(DB_NAME)) {
            when(IdentityDatabaseUtil.getDBConnection(anyBoolean())).thenReturn(connection);
            when(IdentityDatabaseUtil.getDBConnection()).thenReturn(connection);
            when(IdentityTenantUtil.getTenantDomain(anyInt())).thenReturn(TENANT_DOMAIN);

            //delete IdP
            idPManagementDAO.addIdP(((IdentityProvider) identityProvider), tenantId);
            idPManagementDAO.deleteIdP(((IdentityProvider) identityProvider).getIdentityProviderName(), tenantId, TENANT_DOMAIN);

            int resultSize = getIdPCount(connection, ((IdentityProvider) identityProvider).getIdentityProviderName(), tenantId);
            assertEquals(resultSize ,0);


            //delete IdPByResourceID
            idPManagementDAO.addIdP(((IdentityProvider) identityProvider), tenantId);
            String uuid = idPManagementDAO.getIdPByName(connection, ((IdentityProvider) identityProvider).getIdentityProviderName(), tenantId, TENANT_DOMAIN ).getResourceId();
            idPManagementDAO.deleteIdPByResourceId(uuid , tenantId, TENANT_DOMAIN);

            resultSize = getIdPCount(connection, ((IdentityProvider) identityProvider).getIdentityProviderName(), tenantId);
            assertEquals(resultSize ,0);


            //forceDeleteIdP
            idPManagementDAO.addIdP(((IdentityProvider) identityProvider), tenantId);
            idPManagementDAO.forceDeleteIdP(((IdentityProvider) identityProvider).getIdentityProviderName() , tenantId, TENANT_DOMAIN);

            resultSize = getIdPCount(connection, ((IdentityProvider) identityProvider).getIdentityProviderName(), tenantId);
            assertEquals(resultSize ,0);


            //forceDeleteIdPByResourceId
            idPManagementDAO.addIdP(((IdentityProvider) identityProvider), tenantId);
            uuid = idPManagementDAO.getIdPByName(connection, ((IdentityProvider) identityProvider).getIdentityProviderName(), tenantId, TENANT_DOMAIN ).getResourceId();
            idPManagementDAO.forceDeleteIdPByResourceId(uuid , tenantId, TENANT_DOMAIN);

            resultSize = getIdPCount(connection, ((IdentityProvider) identityProvider).getIdentityProviderName(), tenantId);
            assertEquals(resultSize ,0);

        }
    }


    @DataProvider
    public Object[][] testDeleteIdPsData() throws Exception {

        return new Object[][]{
                {SAMPLE_TENANT_ID},
                {SAMPLE_TENANT_ID2},
        };
    }

    @Test(dataProvider = "testDeleteIdPsData")
    public void testDeleteIdPs(int tenantId) throws Exception {

        mockStatic(IdentityDatabaseUtil.class);
        mockStatic(IdentityTenantUtil.class);
        IdPManagementDAO idPManagementDAO = new IdPManagementDAO();

        try (Connection connection = getConnection(DB_NAME)) {
            when(IdentityDatabaseUtil.getDBConnection(anyBoolean())).thenReturn(connection);
            when(IdentityDatabaseUtil.getDBConnection()).thenReturn(connection);
            when(IdentityTenantUtil.getTenantDomain(anyInt())).thenReturn(TENANT_DOMAIN);

            addTestIdps(idPManagementDAO);
            idPManagementDAO.deleteIdPs(tenantId);
        }

        try (Connection connection = getConnection(DB_NAME)){
            String query = "SELECT * FROM IDP WHERE TENANT_ID=?";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setInt(1, tenantId);
            ResultSet resultSet = statement.executeQuery();
            int resultSize = 0;
            if (resultSet.next()) {
                resultSize = resultSet.getRow();
            }
            assertEquals(resultSize ,0);
        }
    }


    @DataProvider
    public Object[][] testDeleteTenantRoleData() throws Exception {

        return new Object[][]{
                {SAMPLE_TENANT_ID, "Role1"},
                {SAMPLE_TENANT_ID, "Role2"},
        };
    }

    @Test(dataProvider = "testDeleteTenantRoleData")
    public void testDeleteTenantRole(int tenantId, String role) throws Exception {

        mockStatic(IdentityDatabaseUtil.class);
        mockStatic(IdentityTenantUtil.class);
        IdPManagementDAO idPManagementDAO = new IdPManagementDAO();

        try (Connection connection = getConnection(DB_NAME)) {
            when(IdentityDatabaseUtil.getDBConnection(anyBoolean())).thenReturn(connection);
            when(IdentityDatabaseUtil.getDBConnection()).thenReturn(connection);
            when(IdentityTenantUtil.getTenantDomain(anyInt())).thenReturn(TENANT_DOMAIN);

            addTestIdps(idPManagementDAO);
            idPManagementDAO.deleteTenantRole(tenantId, role, TENANT_DOMAIN);
        }

        try (Connection connection = getConnection(DB_NAME)){
            String query = "SELECT * FROM IDP_ROLE WHERE TENANT_ID=? AND ROLE=?";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setInt(1, tenantId);
            statement.setString(2, role);
            ResultSet resultSet = statement.executeQuery();
            int resultSize = 0;
            if (resultSet.next()) {
                resultSize = resultSet.getRow();
            }
            assertEquals(resultSize ,0);
        }
    }


    @DataProvider
    public Object[][] testRenameTenantRoleData() throws Exception {

        return new Object[][]{
                {SAMPLE_TENANT_ID, "NewLocalRole1", "LocalRole1"},
                {SAMPLE_TENANT_ID, "NewLocalRole2", "LocalRole2"},
        };
    }

    @Test(dataProvider = "testRenameTenantRoleData")
    public void testRenameTenantRole(int tenantId, String newRole, String oldRole) throws Exception {


        mockStatic(IdentityDatabaseUtil.class);
        mockStatic(IdentityTenantUtil.class);
        IdPManagementDAO idPManagementDAO = new IdPManagementDAO();


        try (Connection connection = getConnection(DB_NAME)) {
            when(IdentityDatabaseUtil.getDBConnection(anyBoolean())).thenReturn(connection);
            when(IdentityDatabaseUtil.getDBConnection()).thenReturn(connection);
            when(IdentityTenantUtil.getTenantDomain(anyInt())).thenReturn(TENANT_DOMAIN);

            addTestIdps(idPManagementDAO);
            idPManagementDAO.renameTenantRole(newRole, oldRole, tenantId, TENANT_DOMAIN);

            String query = "SELECT * FROM IDP_ROLE_MAPPING WHERE TENANT_ID=? AND LOCAL_ROLE=?";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setInt(1, tenantId);
            statement.setString(2, newRole);
            ResultSet resultSet = statement.executeQuery();
            int resultSize = 0;
            if (resultSet.next()) {
                resultSize = resultSet.getRow();
            }
            assertEquals(resultSize ,1);
        }
    }



    private void addTestIdps(IdPManagementDAO idPManagementDAO) throws SQLException, IdentityProviderManagementException {

        //Initialize Test Identity Provider 1
        String[] idpRoles = {"Role1", "Role2" };
        RoleMapping rm1 = new RoleMapping();
        rm1.setRemoteRole("Role1");
        rm1.setLocalRole(new LocalRole("1","LocalRole1"));
        RoleMapping rm2 = new RoleMapping();
        rm2.setRemoteRole("Role2");
        rm2.setLocalRole(new LocalRole("2","LocalRole2"));

        RoleMapping[] roleMap = {rm1, rm2};
        PermissionsAndRoleConfig prc = new PermissionsAndRoleConfig();
        prc.setIdpRoles(idpRoles);
        prc.setRoleMappings(roleMap);

        IdentityProvider idp1 = new IdentityProvider();
        idp1.setIdentityProviderName("testIdP1");
        idp1.setEnable(true);
        idp1.setPrimary(true);
        idp1.setFederationHub(true);
        idp1.setCertificate("");
        idp1.setPermissionAndRoleConfig(prc);
        idp1.setResourceId("uuid1");

        //Initialize Test Identity Provider 2
        IdentityProvider idp2 = new IdentityProvider();
        idp2.setIdentityProviderName("testIdP2");

        //Initialize Test Identity Provider 3
        IdentityProvider idp3 = new IdentityProvider();
        idp3.setIdentityProviderName("testIdP3");

        idPManagementDAO.addIdP(idp1, SAMPLE_TENANT_ID);
        idPManagementDAO.addIdP(idp2, SAMPLE_TENANT_ID);
        idPManagementDAO.addIdP(idp3, SAMPLE_TENANT_ID2);
    }

    private int getIdPCount(Connection connection, String idpName, int tenantId) throws SQLException {
        String query = "SELECT * FROM IDP WHERE NAME=? AND TENANT_ID=?";
        PreparedStatement statement = connection.prepareStatement(query);
        statement.setString(1, (idpName));
        statement.setInt(2, tenantId);
        ResultSet resultSet = statement.executeQuery();
        int resultSize = 0;
        if (resultSet.next()) {
            resultSize = resultSet.getRow();
        }
        statement.clearParameters();
        return resultSize;
    }
}

