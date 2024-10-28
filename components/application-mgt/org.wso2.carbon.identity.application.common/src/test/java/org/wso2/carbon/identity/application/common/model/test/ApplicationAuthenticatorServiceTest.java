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

package org.wso2.carbon.identity.application.common.model.test;

import org.mockito.MockedStatic;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.application.common.ApplicationAuthenticatorService;
import org.wso2.carbon.identity.application.common.dao.impl.AuthenticatorManagementDAOImpl;
import org.wso2.carbon.identity.application.common.exception.AuthenticatorMgtException;
import org.wso2.carbon.identity.application.common.model.LocalAuthenticatorConfig;
import org.wso2.carbon.identity.application.common.model.Property;
import org.wso2.carbon.identity.application.common.model.UserDefinedLocalAuthenticatorConfig;
import org.wso2.carbon.identity.base.AuthenticatorPropertyConstants.AuthenticationType;
import org.wso2.carbon.identity.base.AuthenticatorPropertyConstants.DefinedByType;
import org.wso2.carbon.identity.common.testng.WithAxisConfiguration;
import org.wso2.carbon.identity.common.testng.WithCarbonHome;
import org.wso2.carbon.identity.common.testng.WithH2Database;
import org.wso2.carbon.identity.common.testng.WithRealmService;
import org.wso2.carbon.identity.common.testng.WithRegistry;
import org.wso2.carbon.identity.core.internal.IdentityCoreServiceDataHolder;
import org.wso2.carbon.identity.core.util.IdentityDatabaseUtil;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

import java.util.Arrays;
import java.util.List;

/**
 * This class is a test suite for the ApplicationAuthenticatorServiceTest class.
 * It contains unit tests to verify the functionality of the methods
 * in the ApplicationAuthenticatorServiceTest class.
 */
@WithAxisConfiguration
@WithCarbonHome
@WithH2Database(files = {"dbscripts/h2.sql"})
@WithRegistry
@WithRealmService(injectToSingletons = {IdentityCoreServiceDataHolder.class})
public class ApplicationAuthenticatorServiceTest {

    private MockedStatic<IdentityDatabaseUtil> identityDatabaseUtil;
    private String tenantDomain;

    private ApplicationAuthenticatorService authenticatorService;;
    private UserDefinedLocalAuthenticatorConfig authenticatorConfig1;
    private UserDefinedLocalAuthenticatorConfig authenticatorConfig2;
    private UserDefinedLocalAuthenticatorConfig nonExistAuthenticatorConfig;
    private UserDefinedLocalAuthenticatorConfig systemAuthenticatorConfig;

    private static final String authenticator1Name = "auth1";
    private static final String authenticator2Name = "auth2";
    private static final String nonExistAuthenticator = "non_exist_auth";
    private static final String systemAuthenticator = "system_auth";

    @BeforeClass
    public void setUpClass() throws Exception {

        tenantDomain = "carbon.super";
        authenticatorService = ApplicationAuthenticatorService.getInstance();
        authenticatorConfig1 = createAuthenticatorConfig(authenticator1Name, DefinedByType.USER);
        authenticatorConfig2 = createAuthenticatorConfig(authenticator2Name, DefinedByType.USER);
        nonExistAuthenticatorConfig = createAuthenticatorConfig(nonExistAuthenticator, DefinedByType.USER);
        systemAuthenticatorConfig = createAuthenticatorConfig(systemAuthenticator, DefinedByType.SYSTEM);

        authenticatorService.addLocalAuthenticator(systemAuthenticatorConfig);
        addSystemAuthenticator();
    }

    private void addSystemAuthenticator() throws AuthenticatorMgtException {

        AuthenticatorManagementDAOImpl dao = new AuthenticatorManagementDAOImpl();
        dao.addUserDefinedLocalAuthenticator(systemAuthenticatorConfig, MultitenantConstants.SUPER_TENANT_ID);

    }

    @DataProvider(name = "authenticatorConfigForCreation")
    public Object[][] authenticatorConfigForCreation() {

        return new Object[][]{
                {authenticatorConfig1, AuthenticationType.IDENTIFICATION},
                {authenticatorConfig2, AuthenticationType.VERIFICATION}
        };
    }

    @Test(priority = 1, dataProvider = "authenticatorConfigForCreation")
    public void testCreateUserDefinedLocalAuthenticator(UserDefinedLocalAuthenticatorConfig config,
                                                        AuthenticationType type) throws AuthenticatorMgtException {

        UserDefinedLocalAuthenticatorConfig createdAuthenticator = authenticatorService
                .addUserDefinedLocalAuthenticator(config, tenantDomain);

        Assert.assertEquals(createdAuthenticator.getName(), config.getName());
        Assert.assertEquals(createdAuthenticator.getDisplayName(), config.getDisplayName());
        Assert.assertEquals(createdAuthenticator.isEnabled(), config.isEnabled());
        Assert.assertEquals(createdAuthenticator.getDefinedByType(), DefinedByType.USER);
        if (AuthenticationType.VERIFICATION == type) {
            Assert.assertTrue(Arrays.asList(createdAuthenticator.getTags()).contains("2FA"),
                    "Tag list does not contain 2FA tag for verification authentication type.");
        }
        Assert.assertEquals(createdAuthenticator.getProperties().length, config.getProperties().length);
    }

    @Test(priority = 2, dataProvider = "authenticatorConfigForCreation", expectedExceptions =
            AuthenticatorMgtException.class, expectedExceptionsMessageRegExp = "An authenticator already exists.")
    public void testCreateUserDefinedLocalAuthenticatorWithExistingAuthenticator(
            UserDefinedLocalAuthenticatorConfig config, AuthenticationType type) throws AuthenticatorMgtException {

        authenticatorService.addUserDefinedLocalAuthenticator(config, tenantDomain);
    }

    @Test(priority = 3, expectedExceptions = AuthenticatorMgtException.class,
            expectedExceptionsMessageRegExp = "No operations allowed on system authenticators.")
    public void testCreateUserDefinedLocalAuthenticatorWithSystemAuthenticator() throws AuthenticatorMgtException {

        authenticatorService.addUserDefinedLocalAuthenticator(createAuthenticatorConfig(systemAuthenticator + "new",
                DefinedByType.SYSTEM), tenantDomain);
    }

    @Test(priority = 3, expectedExceptions = AuthenticatorMgtException.class,
            expectedExceptionsMessageRegExp = "Invalid empty or blank value.")
    public void testCreateUserDefinedLocalAuthenticatorWithBlankDisplayName() throws AuthenticatorMgtException {

        UserDefinedLocalAuthenticatorConfig config = createAuthenticatorConfig("withBlankDisplayName",
                DefinedByType.USER);
        config.setDisplayName("");
        authenticatorService.addUserDefinedLocalAuthenticator(config, tenantDomain);
    }

    @Test(priority = 3, expectedExceptions = AuthenticatorMgtException.class,
            expectedExceptionsMessageRegExp = "Authenticator name is invalid.")
    public void testCreateUserDefinedLocalAuthenticatorWithInvalidName() throws AuthenticatorMgtException {

        authenticatorService.addUserDefinedLocalAuthenticator(createAuthenticatorConfig("323#2@dwd",
                DefinedByType.USER), tenantDomain);
    }

    @DataProvider(name = "authenticatorConfigToModify")
    public Object[][] authenticatorConfigToModify() {

        Property prop1 = new Property();
        prop1.setName("PropertyNameNew1");
        prop1.setValue("PropertyValueNew2");
        prop1.setConfidential(false);
        authenticatorConfig1.setProperties(new Property[0]);
        authenticatorConfig1.setDisplayName("Updated Display Name");

        authenticatorConfig2.setEnabled(false);
        authenticatorConfig2.setDefinedByType(DefinedByType.SYSTEM);

        return new Object[][]{
                {authenticatorConfig1},
                {authenticatorConfig2}
        };
    }

    @Test(priority = 4, dataProvider = "authenticatorConfigToModify")
    public void testUpdateUserDefinedLocalAuthenticator(UserDefinedLocalAuthenticatorConfig config)
            throws AuthenticatorMgtException {

        UserDefinedLocalAuthenticatorConfig updatedAuthenticator = authenticatorService
                .updateUserDefinedLocalAuthenticator(config, tenantDomain);

        Assert.assertEquals(updatedAuthenticator.getName(), config.getName());
        Assert.assertEquals(updatedAuthenticator.getDisplayName(), config.getDisplayName());
        Assert.assertEquals(updatedAuthenticator.isEnabled(), config.isEnabled());
        Assert.assertEquals(updatedAuthenticator.getDefinedByType(), DefinedByType.USER);
        Assert.assertEquals(updatedAuthenticator.getProperties().length, config.getProperties().length);
    }

    @Test(priority = 5, expectedExceptions = AuthenticatorMgtException.class,
            expectedExceptionsMessageRegExp = "No Authenticator found.")
    public void testUpdateUserDefinedLocalAuthenticatorWithNonExistingAuthenticator() throws AuthenticatorMgtException {

        authenticatorService.updateUserDefinedLocalAuthenticator(nonExistAuthenticatorConfig, tenantDomain);
    }

    @Test(priority = 6, expectedExceptions = AuthenticatorMgtException.class,
            expectedExceptionsMessageRegExp = "No operations allowed on system authenticators.")
    public void testUpdateUserDefinedLocalAuthenticatorWithSystemAuthenticator() throws AuthenticatorMgtException {

        authenticatorService.updateUserDefinedLocalAuthenticator(systemAuthenticatorConfig, tenantDomain);
    }

    @DataProvider(name = "authenticatorConfigToRetrieve")
    public Object[][] authenticatorConfigToRetrieve() {

        return new Object[][]{
                {authenticatorConfig1, authenticatorConfig1, AuthenticationType.IDENTIFICATION.toString()},
                {authenticatorConfig2, authenticatorConfig2, AuthenticationType.VERIFICATION.toString()},
                {nonExistAuthenticatorConfig, null, null},
                {systemAuthenticatorConfig, null, null}
        };
    }

    @Test(priority = 7, dataProvider = "authenticatorConfigToRetrieve")
    public void testGetUserDefinedLocalAuthenticator(UserDefinedLocalAuthenticatorConfig configToBeRetrieved,
                                                     UserDefinedLocalAuthenticatorConfig expectedConfig, String type) throws AuthenticatorMgtException {

        UserDefinedLocalAuthenticatorConfig retrievedConfig =
                authenticatorService.getUserDefinedLocalAuthenticator(configToBeRetrieved.getName(), tenantDomain);
        Assert.assertEquals(retrievedConfig, expectedConfig);
        if (expectedConfig != null) {
            Assert.assertEquals(retrievedConfig.getDisplayName(), expectedConfig.getDisplayName());
            Assert.assertEquals(retrievedConfig.isEnabled(), expectedConfig.isEnabled());
            Assert.assertEquals(retrievedConfig.getDefinedByType(), DefinedByType.USER);
            if (AuthenticationType.VERIFICATION.toString().equals(type)) {
                Assert.assertTrue(Arrays.asList(retrievedConfig.getTags()).contains("2FA"),
                        "Tag list does not contain 2FA tag for verification authentication type.");
            }
            Assert.assertEquals(retrievedConfig.getProperties().length, expectedConfig.getProperties().length);
        }
    }

    @Test(priority = 8)
    public void testGetAllUserDefinedLocalAuthenticator() throws AuthenticatorMgtException {

        List<LocalAuthenticatorConfig> retrievedConfig = authenticatorService.getLocalAuthenticators();
        Assert.assertEquals(retrievedConfig.size(), 3);
    }

    @Test(priority = 9)
    public void testGetUserDefinedLocalAuthenticatorWithSystemAuthenticator() throws AuthenticatorMgtException {

        LocalAuthenticatorConfig retrievedConfig = authenticatorService.getLocalAuthenticatorByName(
                systemAuthenticatorConfig.getName(), tenantDomain);
        Assert.assertNotNull(retrievedConfig);
        Assert.assertEquals(retrievedConfig.getName(), systemAuthenticatorConfig.getName());
        Assert.assertEquals(retrievedConfig.getDisplayName(), systemAuthenticatorConfig.getDisplayName());
        Assert.assertEquals(retrievedConfig.getDefinedByType(), DefinedByType.SYSTEM);
        Assert.assertEquals(retrievedConfig.getProperties().length, systemAuthenticatorConfig.getProperties().length);
    }

    @Test(priority = 10, dataProvider = "authenticatorConfigToModify")
    public void testDeleteUserDefinedLocalAuthenticator(UserDefinedLocalAuthenticatorConfig config)
            throws AuthenticatorMgtException {

        authenticatorService.deleteUserDefinedLocalAuthenticator(config.getName(), tenantDomain);
        Assert.assertNull(authenticatorService.getLocalAuthenticatorByName(config.getName()));
    }

    @Test(priority = 11, expectedExceptions = AuthenticatorMgtException.class,
            expectedExceptionsMessageRegExp = "No Authenticator found.")
    public void testDeleteUserDefinedLocalAuthenticatorWithNonExistingAuthenticator() throws AuthenticatorMgtException {

        authenticatorService.deleteUserDefinedLocalAuthenticator(nonExistAuthenticatorConfig.getName(), tenantDomain);
    }

    @Test(priority = 12, expectedExceptions = AuthenticatorMgtException.class,
            expectedExceptionsMessageRegExp = "No operations allowed on system authenticators.")
    public void testDeleteUserDefinedLocalAuthenticatorWithSystemAuthenticator() throws AuthenticatorMgtException {

        authenticatorService.deleteUserDefinedLocalAuthenticator(systemAuthenticatorConfig.getName(), tenantDomain);
    }

    private UserDefinedLocalAuthenticatorConfig createAuthenticatorConfig(String uniqueIdentifier,
                                                                          DefinedByType definedByType) {

        UserDefinedLocalAuthenticatorConfig authenticatorConfig = new
                UserDefinedLocalAuthenticatorConfig(AuthenticationType.IDENTIFICATION);
        authenticatorConfig.setName(uniqueIdentifier);
        authenticatorConfig.setDisplayName("Custom " + uniqueIdentifier);
        authenticatorConfig.setEnabled(true);
        authenticatorConfig.setDefinedByType(definedByType);

        Property prop1 = new Property();
        prop1.setName("PropertyName1_" + uniqueIdentifier);
        prop1.setValue("PropertyValue1_" + uniqueIdentifier);
        prop1.setConfidential(false);
        Property prop2 = new Property();
        prop2.setName("PropertyName2_" + uniqueIdentifier);
        prop2.setValue("PropertyValue2_" + uniqueIdentifier);
        prop2.setConfidential(true);
        authenticatorConfig.setProperties(new Property[]{prop1, prop2});

        return authenticatorConfig;
    }
}
