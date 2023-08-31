package org.wso2.carbon.identity.application.role.mgt.dao.impl;

import org.apache.commons.dbcp.BasicDataSource;
import org.apache.commons.lang.StringUtils;
import org.mockito.Mockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.testng.PowerMockTestCase;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.wso2.carbon.database.utils.jdbc.NamedJdbcTemplate;
import org.wso2.carbon.identity.application.common.model.IdPGroup;
import org.wso2.carbon.identity.application.common.model.IdentityProvider;
import org.wso2.carbon.identity.application.role.mgt.model.ApplicationRole;
import org.wso2.carbon.identity.application.role.mgt.util.ApplicationRoleMgtUtils;
import org.wso2.carbon.identity.core.persistence.JDBCPersistenceManager;
import org.wso2.carbon.identity.core.util.IdentityDatabaseUtil;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;

import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import static org.mockito.ArgumentMatchers.anyString;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.when;

@PrepareForTest({DataSource.class, JDBCPersistenceManager.class, IdentityTenantUtil.class,
        ApplicationRoleMgtUtils.class, IdentityDatabaseUtil.class})
public class ApplicationRoleMgtDAOImplTest extends PowerMockTestCase {

    private static final String DB_NAME = "application_role_mgt_dao_db";
    private static final String TENANT_DOMAIN = "TEST_TENANT_DOMAIN";
    private static final int TENANT_ID = 2;
    private static final int APP_ID = 1;
    private static final String APP_NAME = "TEST_APP_NAME";
    private static final String USER_STORE = "TEST_USER_STORE";
    private static final String USERNAME = "TEST_USERNAME";
    private static final String AUTH_TYPE = "TEST_AUTH_TYPE";
    private static final String ROLE_ID = "TEST_ROLE_ID";
    private static final String ROLE_NAME = "TEST_ROLE_NAME";
    private static final int IDP_ID = 1;
    private static final String IDP_NAME = "TEST_IDP_NAME";

    private static Map<String, BasicDataSource> dataSourceMap = new HashMap<>();
    private ApplicationRoleMgtDAOImpl daoImpl;
    Connection connection = null;

    @BeforeClass
    public void setUp() throws Exception {

        daoImpl = new ApplicationRoleMgtDAOImpl();
        initiateH2Database(getFilePath());
        populateApplication();
        populateIdp();
    }

    @AfterClass
    public void tearDown() throws Exception {

        closeH2Database();
    }

    @Test
    public void testAddApplicationRole() throws Exception {

        mockStatic(IdentityTenantUtil.class);
        when(IdentityTenantUtil.getTenantId(TENANT_DOMAIN)).thenReturn(TENANT_ID);
        mockStatic(IdentityDatabaseUtil.class);
        Mockito.when(IdentityDatabaseUtil.getDataSource()).thenReturn(dataSourceMap.get(DB_NAME));
        ApplicationRole applicationRole = new ApplicationRole();
        applicationRole.setApplicationId(String.valueOf(APP_ID));
        applicationRole.setRoleId(ROLE_ID);
        applicationRole.setRoleName(ROLE_NAME);
        ApplicationRole addedApplicationRole = daoImpl.addApplicationRole(applicationRole, TENANT_DOMAIN);
        Assert.assertNotNull(addedApplicationRole);
    }

    @DataProvider
    public Object[][] updateApplicationRoleAssignedUsersData() {
        return new Object[][]{
                {new ArrayList<>(Arrays.asList("USER_1", "USER_2", "USER_3")),
                        new ArrayList<>(Collections.emptyList()),
                },

        };
    }

    @Test(dataProvider = "updateApplicationRoleAssignedUsersData", priority = 2)
    public void testUpdateApplicationRoleAssignedUsers(List<String> addedUsers, List<String> removedUsers)
            throws Exception {

        mockStatic(ApplicationRoleMgtUtils.class);
        mockStatic(IdentityTenantUtil.class);
        when(IdentityTenantUtil.getTenantId(TENANT_DOMAIN)).thenReturn(TENANT_ID);
        when(ApplicationRoleMgtUtils.getNewTemplate()).thenReturn(new NamedJdbcTemplate(dataSourceMap.get(DB_NAME)));
        when(ApplicationRoleMgtUtils.isUserExists(anyString())).thenReturn(true);
        ApplicationRole role =
                daoImpl.updateApplicationRoleAssignedUsers(ROLE_ID, addedUsers, removedUsers, TENANT_DOMAIN);
        Assert.assertEquals(role.getAssignedUsers().size(), addedUsers.size());
    }

    @DataProvider
    public Object[][] updateApplicationRoleAssignedGroupsData() {
        return new Object[][]{
                {new ArrayList<>(Arrays.asList("GROUP_1", "GROUP_2", "GROUP_3")),
                        new ArrayList<>(Collections.emptyList()),
                },

        };
    }

    @Test(dataProvider = "updateApplicationRoleAssignedGroupsData", priority = 2)
    public void testUpdateApplicationRoleAssignedGroups(List<String> addedGroups, List<String> removedGroups)
            throws Exception {

        mockStatic(ApplicationRoleMgtUtils.class);
        mockStatic(IdentityTenantUtil.class);
        when(IdentityTenantUtil.getTenantId(TENANT_DOMAIN)).thenReturn(TENANT_ID);
        when(ApplicationRoleMgtUtils.getNewTemplate()).thenReturn(new NamedJdbcTemplate(dataSourceMap.get(DB_NAME)));
        when(ApplicationRoleMgtUtils.isGroupExists(anyString())).thenReturn(true);
        IdentityProvider identityProvider = new IdentityProvider();
        identityProvider.setResourceId(String.valueOf(IDP_ID));
        List<IdPGroup> idPGroups = new ArrayList<>();
        for (String group: addedGroups) {
            IdPGroup idPGroup = new IdPGroup();
            idPGroup.setIdpGroupId(group);
            idPGroup.setIdpGroupName(group);
            idPGroups.add(idPGroup);
        }
        identityProvider.setIdPGroupConfig(idPGroups.toArray(new IdPGroup[0]));
        ApplicationRole role =
                daoImpl.updateApplicationRoleAssignedGroups(ROLE_ID, identityProvider, addedGroups, removedGroups,
                        TENANT_DOMAIN);
        Assert.assertEquals(role.getAssignedGroups().size(), addedGroups.size());
    }

    private void populateApplication() throws Exception {

        String domainDataSQL = "INSERT INTO SP_APP (ID, TENANT_ID, APP_NAME, USER_STORE, USERNAME, AUTH_TYPE, UUID) " +
                "VALUES " + "(" + APP_ID + "," + TENANT_ID + ",'" + APP_NAME + "','" + USER_STORE + "','" + USERNAME
                + "','" + AUTH_TYPE + "','" + APP_ID + "')";

        try {
            connection.createStatement().executeUpdate(domainDataSQL);
        } catch (SQLException e) {
            String errorMessage = "Error while Adding test data for SP_APP table";
            throw new Exception(errorMessage, e);
        }
    }

    private void populateIdp() throws Exception {

        String domainDataSQL = "INSERT INTO IDP (ID, TENANT_ID, NAME, UUID) " +
                "VALUES " + "(" + IDP_ID + "," + TENANT_ID + ",'" + IDP_NAME + "','" + IDP_ID + "')";

        try {
            connection.createStatement().executeUpdate(domainDataSQL);
        } catch (SQLException e) {
            String errorMessage = "Error while Adding test data for IDP table";
            throw new Exception(errorMessage, e);
        }
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
        connection = dataSource.getConnection();
        connection.createStatement().executeUpdate("RUNSCRIPT FROM '" + scriptPath + "'");
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

    private static String getFilePath() {

        if (StringUtils.isNotBlank("h2.sql")) {
            return Paths.get(System.getProperty("user.dir"), "src", "test", "resources", "dbscripts", "h2.sql")
                    .toString();
        }
        throw new IllegalArgumentException("DB Script file name cannot be empty.");
    }
}
