package org.wso2.carbon.identity.role.mgt.core.v2.dao;

import org.apache.commons.dbcp.BasicDataSource;
import org.apache.commons.lang.StringUtils;
import org.mockito.Mock;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.testng.PowerMockTestCase;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.identity.common.testng.WithCarbonHome;
import org.wso2.carbon.identity.core.util.IdentityDatabaseUtil;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;
import org.wso2.carbon.identity.core.util.IdentityUtil;
import org.wso2.carbon.identity.role.mgt.core.v2.Permission;
import org.wso2.carbon.identity.role.mgt.core.v2.RoleBasicInfo;
import org.wso2.carbon.user.api.UserRealm;
import org.wso2.carbon.user.core.util.UserCoreUtil;

import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.powermock.api.mockito.PowerMockito.doCallRealMethod;
import static org.powermock.api.mockito.PowerMockito.doNothing;
import static org.powermock.api.mockito.PowerMockito.doReturn;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.spy;
import static org.powermock.api.mockito.PowerMockito.when;
import static org.testng.Assert.assertTrue;

@WithCarbonHome
@PrepareForTest({IdentityDatabaseUtil.class, IdentityTenantUtil.class, IdentityUtil.class, UserCoreUtil.class,
        CarbonContext.class, RoleDAOImpl.class})
@PowerMockIgnore("org.mockito.*")
public class RoleDAOTest extends PowerMockTestCase {


    private static final int SAMPLE_TENANT_ID = 1;
    private static final String SAMPLE_TENANT_DOMAIN = "wso2.com";
    private static final String DB_NAME = "ROLE_DB";
    private static final String ORGANIZATION_AUD  = "Organization";
    private static final String APPLICATION_AUD  = "Application";
    private static Map<String, BasicDataSource> dataSourceMap = new HashMap<>();
    private RoleDAO roleDAO;
    private List<String> userNamesList = new ArrayList<>();
    private List<String> emptyList = new ArrayList<>();
    private List<String> groupNamesList = new ArrayList<>();
    private Map<String, String> groupNamesMap = new HashMap<>();
    private Map<String, String> emptyMap = new HashMap<>();
    private Map<String, String> groupIdsMap = new HashMap<>();
    private List<String> userIDsList = new ArrayList<>();
    private List<String> groupIDsList = new ArrayList<>();
    private List<Permission> permissions = new ArrayList<>();

    @Mock
    UserRealm mockUserRealm;

    @BeforeMethod
    public void setUp() throws Exception {

        userNamesList.add("user1");
        userNamesList.add("user2");
        groupNamesList.add("group2");
        groupNamesList.add("group1");

        groupNamesMap.put("groupID2", "group2");
        groupNamesMap.put("groupID1", "group1");
        groupIdsMap.put("group2", "groupID2");
        groupIdsMap.put("group1", "groupID1");
        userIDsList.add("userID1");
        userIDsList.add("userID2");
        groupIDsList.add("groupID1");
        groupIDsList.add("groupID2");
        permissions.add(new Permission("read", "read"));
        permissions.add(new Permission("write", "write"));
        mockStatic(IdentityDatabaseUtil.class);
        mockStatic(IdentityTenantUtil.class);
        initializeDataSource(getFilePath("h2.sql"));
        populateData();
    }

    @Test
    public void testAddOrgRole() throws Exception {

        try (Connection connection1 = getConnection();
             Connection connection2 = getConnection();
             Connection connection3 = getConnection()) {

            roleDAO = spy(RoleMgtDAOFactory.getInstance().getRoleDAO());
            mockCacheClearing();
            when(IdentityDatabaseUtil.getDBConnection(anyBoolean())).thenReturn(connection1);
            when(IdentityDatabaseUtil.getUserDBConnection(anyBoolean())).thenReturn(connection2);
            RoleBasicInfo role = addRole("role1", ORGANIZATION_AUD, "test-org-id");
            when(IdentityDatabaseUtil.getUserDBConnection(anyBoolean())).thenReturn(connection3);
            doCallRealMethod().when(roleDAO, "isExistingRoleName", anyString(), anyString());
            assertTrue(roleDAO.isExistingRoleName("role1", SAMPLE_TENANT_DOMAIN));
        }
    }

    @Test
    public void testAddAppRole() throws Exception {

        try (Connection connection1 = getConnection();
             Connection connection2 = getConnection();
             Connection connection3 = getConnection()) {

            roleDAO = spy(RoleMgtDAOFactory.getInstance().getRoleDAO());
            mockCacheClearing();
            when(IdentityDatabaseUtil.getDBConnection(anyBoolean())).thenReturn(connection1);
            when(IdentityDatabaseUtil.getUserDBConnection(anyBoolean())).thenReturn(connection2);
            RoleBasicInfo role = addRole("role2", APPLICATION_AUD, "test-app-id");
            when(IdentityDatabaseUtil.getUserDBConnection(anyBoolean())).thenReturn(connection3);
            doCallRealMethod().when(roleDAO, "isExistingRoleName", anyString(), anyString());
            assertTrue(roleDAO.isExistingRoleName("role2", SAMPLE_TENANT_DOMAIN));
        }
    }

    private RoleBasicInfo addRole(String roleName, String audience, String audienceId) throws Exception {

        mockCacheClearing();
        mockStatic(IdentityUtil.class);
        when(IdentityUtil.getPrimaryDomainName()).thenReturn("PRIMARY");
        doCallRealMethod().when(IdentityUtil.class, "extractDomainFromName", anyString());
        doReturn(new ArrayList<>()).when(roleDAO, "getUserNamesByIDs", anyCollection(), anyString());
        doReturn(new HashMap<>()).when(roleDAO, "getGroupNamesByIDs", anyCollection(), anyString());
        doReturn(false).when(roleDAO, "isExistingRoleName", anyString(), anyString());
        doReturn(groupIdsMap).when(roleDAO, "getGroupIDsByNames", anyCollection(), anyString());
        doReturn(roleName).when(roleDAO, "getRoleNameByID", anyString(), anyString());
        doReturn("test-org").when(roleDAO, "getOrganizationName", anyString());
        when(IdentityTenantUtil.getTenantId(anyString())).thenReturn(SAMPLE_TENANT_ID);
        return roleDAO.addRole(roleName, userIDsList, groupIDsList, permissions, audience, audienceId ,
                SAMPLE_TENANT_DOMAIN);
    }

    private void mockCacheClearing() throws Exception {

        doNothing().when(roleDAO, "clearUserRolesCache", anyString(), anyInt());
    }

    private void initializeDataSource(String scriptPath) throws Exception {

        BasicDataSource dataSource = new BasicDataSource();
        dataSource.setDriverClassName("org.h2.Driver");
        dataSource.setUsername("username");
        dataSource.setPassword("password");
        dataSource.setUrl("jdbc:h2:mem:" + RoleDAOTest.DB_NAME);

        try (Connection connection = dataSource.getConnection()) {
            connection.createStatement().executeUpdate("RUNSCRIPT FROM '" + scriptPath + "'");
        }
        dataSourceMap.put(RoleDAOTest.DB_NAME, dataSource);
    }

    private String getFilePath(String fileName) {
        if (StringUtils.isNotBlank(fileName)) {
            return Paths.get(System.getProperty("user.dir"), "src", "test", "resources", "dbscripts", fileName)
                    .toString();
        }
        return null;
    }

    private void populateData() throws Exception {

        String domainDataSQL = "INSERT INTO UM_DOMAIN (UM_DOMAIN_ID, UM_DOMAIN_NAME, UM_TENANT_ID) VALUES "
                + "(1,'PRIMARY',-1234), (2,'SYSTEM',-1234), (3,'INTERNAL',-1234), (4,'APPLICATION',-1234), "
                + "(5,'WORKFLOW',-1234), (6,'PRIMARY',1), (7,'SYSTEM',1), (8,'INTERNAL',1), (9,'APPLICATION',1), "
                + "(10,'WORKFLOW',1)";
        String aPIResourceSQL = "INSERT INTO API_RESOURCE (ID, NAME, IDENTIFIER, TENANT_ID, DESCRIPTION, TYPE," +
                " REQUIRES_AUTHORIZATION) VALUES (1,'DOC','DOC',1,'DOC','RBAC',true);";
        String scopeSQL = "INSERT INTO SCOPE (ID,API_ID,NAME,DISPLAY_NAME,TENANT_ID,DESCRIPTION) VALUES " +
                "(1,1,'read','read',1,'read'), (2,1,'write','write',1,'write')";
        String sPAppSQL = "INSERT INTO SP_APP (ID, TENANT_ID, APP_NAME, USER_STORE, USERNAME, AUTH_TYPE, UUID) " +
                "VALUES (1, 1, 'TEST_APP_NAME','TEST_USER_STORE', 'TEST_USERNAME', 'TEST_AUTH_TYPE', 'test-app-id')";

        try (Connection connection = getConnection()) {
            connection.createStatement().executeUpdate(domainDataSQL);
            connection.createStatement().executeUpdate(aPIResourceSQL);
            connection.createStatement().executeUpdate(scopeSQL);
            connection.createStatement().executeUpdate(sPAppSQL);
        } catch (SQLException e) {
            String errorMessage = "Error while Adding test data for UM_DOMAIN table";
            throw new Exception(errorMessage, e);
        }
    }

    private Connection getConnection() throws Exception {
        if (dataSourceMap.get(RoleDAOTest.DB_NAME) != null) {
            if (dataSourceMap.get(RoleDAOTest.DB_NAME).getConnection().isClosed()) {
                initializeDataSource(getFilePath("h2.sql"));
            }
            return dataSourceMap.get(RoleDAOTest.DB_NAME).getConnection();
        }
        throw new RuntimeException("Invalid datasource.");
    }
}
