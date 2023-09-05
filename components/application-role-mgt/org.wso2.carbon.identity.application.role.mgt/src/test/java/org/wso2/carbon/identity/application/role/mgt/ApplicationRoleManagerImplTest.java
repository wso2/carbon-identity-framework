package org.wso2.carbon.identity.application.role.mgt;

import org.powermock.modules.testng.PowerMockTestCase;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.application.role.mgt.exceptions.ApplicationRoleManagementException;
import org.wso2.carbon.identity.application.role.mgt.model.ApplicationRole;
import org.wso2.carbon.identity.common.testng.WithAxisConfiguration;
import org.wso2.carbon.identity.common.testng.WithCarbonHome;
import org.wso2.carbon.identity.common.testng.WithH2Database;
import org.wso2.carbon.identity.common.testng.WithRealmService;
import org.wso2.carbon.identity.common.testng.WithRegistry;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@WithAxisConfiguration
@WithCarbonHome
@WithRegistry
@WithRealmService
@WithH2Database(jndiName = "jdbc/WSO2IdentityDB", files = {"dbScripts/h2.sql"})
public class ApplicationRoleManagerImplTest extends PowerMockTestCase {

    private ApplicationRoleManager applicationRoleManager;

    @BeforeClass
    public void setUp() {

        applicationRoleManager = ApplicationRoleManagerImpl.getInstance();
    }

    @AfterClass
    public void tearDown() {

    }

    @DataProvider(name = "addApplicationRoleDataProvider")
    public Object[][] addApplicationRoleData() {

        ApplicationRole applicationRole1 = createApplicationRole("1");
        ApplicationRole applicationRole2 = createApplicationRole("2");
        return new Object[][]{
                {applicationRole1},
                {applicationRole2}
        };
    }

    @Test(dataProvider = "addApplicationRoleDataProvider", priority = 1)
    public void testAddApplicationRole(ApplicationRole applicationRole) throws ApplicationRoleManagementException {

        ApplicationRole role =  applicationRoleManager.addApplicationRole(applicationRole);
        Assert.assertNotNull(role);
    }

    @Test(priority = 1)
    public void testAddApplicationRoleException() throws Exception {

        ApplicationRole applicationRole = createApplicationRole("1");
        applicationRoleManager.addApplicationRole(applicationRole);
        ApplicationRoleManagementException exception = null;
        try {
            applicationRoleManager.addApplicationRole(applicationRole);
        } catch (ApplicationRoleManagementException e) {
            exception = e;
        }
        Assert.assertNotNull(exception);
    }

    @DataProvider(name = "getApplicationRoleByIdData")
    public Object[][] getApplicationRoleByIdData() throws ApplicationRoleManagementException {

        ApplicationRole applicationRole1 = createApplicationRole("1");
        ApplicationRole applicationRole2 = createApplicationRole("2");
        applicationRoleManager.addApplicationRole(applicationRole1);
        applicationRoleManager.addApplicationRole(applicationRole2);
        return new Object[][]{
                {applicationRole1},
                {applicationRole2}
        };
    }

    @Test(dataProvider = "getApplicationRoleByIdData", priority = 2)
    public void testGetApplicationRoleById(ApplicationRole applicationRole) throws Exception {

        ApplicationRole role =  applicationRoleManager.getApplicationRoleById(applicationRole.getRoleId());
        Assert.assertNotNull(role);
    }

    @Test(priority = 2)
    public void testGetApplicationRoleByIdException() {


        Exception exception = null;
        try {
            applicationRoleManager.getApplicationRoleById("fake-id");
        } catch (Exception e) {
            exception = e;
        }
        Assert.assertNotNull(exception);
    }

    @DataProvider(name = "updateApplicationRoleData")
    public Object[][] updateApplicationRoleData() throws ApplicationRoleManagementException {

        ApplicationRole applicationRole1 = createApplicationRole("1");
        ApplicationRole applicationRole2 = createApplicationRole("2");
        applicationRoleManager.addApplicationRole(applicationRole1);
        applicationRoleManager.addApplicationRole(applicationRole2);
        return new Object[][]{
                {applicationRole1, "NEW_NAME-1",
                        new ArrayList<>(Arrays.asList("TEST_SCOPE_4", "TEST_SCOPE_5", "TEST_SCOPE_6")),
                        new ArrayList<>(Arrays.asList("TEST_SCOPE_1", "TEST_SCOPE_2", "TEST_SCOPE_3"))},
                {applicationRole2, "NEW_NAME-2",
                        new ArrayList<>(Arrays.asList("TEST_SCOPE_4", "TEST_SCOPE_5", "TEST_SCOPE_6")),
                        new ArrayList<>(Arrays.asList("TEST_SCOPE_1", "TEST_SCOPE_2", "TEST_SCOPE_3"))}
        };
    }

    @Test(dataProvider = "updateApplicationRoleData", priority = 2)
    public void testUpdateApplicationRole(ApplicationRole applicationRole, String newName, List<String> addedScopes,
                                          List<String> removedScopes) throws Exception {

        ApplicationRole role =  applicationRoleManager.updateApplicationRole(applicationRole.getApplicationId(),
                applicationRole.getRoleId(), newName, addedScopes, removedScopes);
        Assert.assertNotNull(role);
    }

    @Test(priority = 2)
    public void testUpdateApplicationRoleException() {


        Exception exception = null;
        try {
            applicationRoleManager.updateApplicationRole("fake-app-id",
                    "fake-roke-id", "newName", new ArrayList<>(), new ArrayList<>());
        } catch (Exception e) {
            exception = e;
        }
        Assert.assertNotNull(exception);
    }

    @DataProvider(name = "deleteApplicationRoleData")
    public Object[][] deleteApplicationRoleData() throws ApplicationRoleManagementException {

        ApplicationRole applicationRole1 = createApplicationRole("1");
        ApplicationRole applicationRole2 = createApplicationRole("2");
        applicationRoleManager.addApplicationRole(applicationRole1);
        applicationRoleManager.addApplicationRole(applicationRole2);
        return new Object[][]{
                {applicationRole1},
                {applicationRole2}
        };
    }

    @Test(dataProvider = "deleteApplicationRoleData", priority = 2)
    public void testDeleteApplicationRole(ApplicationRole applicationRole) throws Exception {

        Exception exception = null;
        try {
            applicationRoleManager.deleteApplicationRole(applicationRole.getRoleId());
        } catch (Exception e) {
            exception = e;
        }
        Assert.assertNull(exception);
    }

    @Test(priority = 2)
    public void testDeleteApplicationRoleException() {


        Exception exception = null;
        try {
            applicationRoleManager.deleteApplicationRole("fake-id");
        } catch (Exception e) {
            exception = e;
        }
        Assert.assertNotNull(exception);
    }

    /**
     * Create application role with the given postfix.
     *
     * @param postFix Postfix to be appended to each API resource and scope information.
     * @return Application Role.
     */
    private static ApplicationRole createApplicationRole(String postFix) {

        ApplicationRole applicationRole = new ApplicationRole();
        applicationRole.setRoleId("testAppRoleId-" + postFix);
        applicationRole.setRoleName("testAppRoleName-" + postFix);
        applicationRole.setApplicationId("1");
        applicationRole.setPermissions(new String[]{"TEST_SCOPE_1", "TEST_SCOPE_2", "TEST_SCOPE_3"});
        return applicationRole;
    }
}
