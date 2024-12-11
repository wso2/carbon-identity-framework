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
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.action.management.exception.ActionMgtException;
import org.wso2.carbon.identity.action.management.model.Action;
import org.wso2.carbon.identity.action.management.model.Authentication;
import org.wso2.carbon.identity.action.management.model.EndpointConfig;
import org.wso2.carbon.identity.action.management.service.ActionManagementService;
import org.wso2.carbon.identity.application.common.ApplicationAuthenticatorService;
import org.wso2.carbon.identity.application.common.exception.AuthenticatorMgtException;
import org.wso2.carbon.identity.application.common.exception.AuthenticatorMgtServerRuntimeException;
import org.wso2.carbon.identity.application.common.internal.ApplicationCommonServiceDataHolder;
import org.wso2.carbon.identity.application.common.model.LocalAuthenticatorConfig;
import org.wso2.carbon.identity.application.common.model.Property;
import org.wso2.carbon.identity.application.common.model.UserDefinedAuthenticatorEndpointConfig.UserDefinedAuthenticatorEndpointConfigBuilder;
import org.wso2.carbon.identity.application.common.model.UserDefinedLocalAuthenticatorConfig;
import org.wso2.carbon.identity.application.common.model.test.util.ActionMgtTestUtil;
import org.wso2.carbon.identity.base.AuthenticatorPropertyConstants.AuthenticationType;
import org.wso2.carbon.identity.base.AuthenticatorPropertyConstants.DefinedByType;
import org.wso2.carbon.identity.common.testng.WithAxisConfiguration;
import org.wso2.carbon.identity.common.testng.WithCarbonHome;
import org.wso2.carbon.identity.common.testng.WithH2Database;
import org.wso2.carbon.identity.common.testng.WithRealmService;
import org.wso2.carbon.identity.common.testng.WithRegistry;
import org.wso2.carbon.identity.core.internal.IdentityCoreServiceDataHolder;
import org.wso2.carbon.identity.core.util.IdentityDatabaseUtil;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

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

    private UserDefinedLocalAuthenticatorConfig authenticatorConfig1;
    private UserDefinedLocalAuthenticatorConfig authenticatorConfig2;
    private UserDefinedLocalAuthenticatorConfig authenticatorConfigForException;
    private UserDefinedLocalAuthenticatorConfig nonExistAuthenticatorConfig;
    private LocalAuthenticatorConfig systemAuthenticatorConfig;

    private ActionManagementService actionManagementService;
    private static Action action;
    private static EndpointConfig endpointConfig;
    private static EndpointConfig endpointConfigToBeUpdated;

    private static final String AUTHENTICATOR1_NAME = "auth1";
    private static final String AUTHENTICATOR2_NAME = "auth2";
    private static final String AUTHENTICATOR_CONFIG_FOR_EXCEPTION_NAME = "exception_auth";
    private static final String NON_EXIST_AUTHENTICATOR_NAME = "non_exist_auth";
    private static final String SYSTEM_AUTHENTICATOR_NAME = "system_auth";

    @BeforeClass
    public void setUpClass() throws Exception {

        tenantDomain = "carbon.super";
        systemAuthenticatorConfig = createSystemDefinedAuthenticatorConfig(SYSTEM_AUTHENTICATOR_NAME);
        authenticatorConfig1 = createUserDefinedAuthenticatorConfig(AUTHENTICATOR1_NAME,
                AuthenticationType.IDENTIFICATION);
        authenticatorConfig2 = createUserDefinedAuthenticatorConfig(AUTHENTICATOR2_NAME,
                AuthenticationType.VERIFICATION);
        nonExistAuthenticatorConfig = createUserDefinedAuthenticatorConfig(NON_EXIST_AUTHENTICATOR_NAME,
                AuthenticationType.IDENTIFICATION);
        authenticatorConfigForException = createUserDefinedAuthenticatorConfig(AUTHENTICATOR_CONFIG_FOR_EXCEPTION_NAME,
                AuthenticationType.IDENTIFICATION);

        endpointConfig = ActionMgtTestUtil.createEndpointConfig("http://localhost", "admin", "admin");
        endpointConfigToBeUpdated = ActionMgtTestUtil.createEndpointConfig(
                "http://localhost1", "admin1", "admin1");
        action = ActionMgtTestUtil.createAction(endpointConfig);
        actionManagementService = mock(ActionManagementService.class);

        when(actionManagementService.addAction(anyString(), any(), any())).thenReturn(action);
        when(actionManagementService.updateAction(anyString(), any(), any(), any())).thenReturn(action);
        when(actionManagementService.getActionByActionId(anyString(), any(), any())).thenReturn(action);
        doNothing().when(actionManagementService).deleteAction(anyString(), any(), any());

        ApplicationCommonServiceDataHolder.getInstance().setApplicationAuthenticatorService(
                ApplicationAuthenticatorService.getInstance());
        ApplicationCommonServiceDataHolder.getInstance().setActionManagementService(actionManagementService);
    }

    @AfterMethod
    public void tearDown() {

        ApplicationCommonServiceDataHolder.getInstance().setActionManagementService(actionManagementService);
    }

    @DataProvider(name = "authenticatorConfigForCreation")
    public Object[][] authenticatorConfigForCreation() {

        return new Object[][]{
                {authenticatorConfig1},
                {authenticatorConfig2}
        };
    }

    @Test(priority = 1, dataProvider = "authenticatorConfigForCreation")
    public void testCreateUserDefinedLocalAuthenticator(UserDefinedLocalAuthenticatorConfig config)
            throws AuthenticatorMgtException {

        UserDefinedLocalAuthenticatorConfig createdAuthenticator = ApplicationCommonServiceDataHolder.getInstance()
                .getApplicationAuthenticatorService().addUserDefinedLocalAuthenticator(config, tenantDomain);

        Assert.assertEquals(createdAuthenticator.getName(), config.getName());
        Assert.assertEquals(createdAuthenticator.getDisplayName(), config.getDisplayName());
        Assert.assertEquals(createdAuthenticator.isEnabled(), config.isEnabled());
        Assert.assertEquals(createdAuthenticator.getDefinedByType(), DefinedByType.USER);
        if (AuthenticationType.VERIFICATION == config.getAuthenticationType()) {
            Assert.assertTrue(Arrays.asList(createdAuthenticator.getTags()).contains("2FA"),
                    "Tag list does not contain 2FA tag for verification authentication type.");
        }
        Assert.assertEquals(createdAuthenticator.getProperties().length, config.getProperties().length);
    }

    @Test(priority = 2, dataProvider = "authenticatorConfigForCreation", expectedExceptions =
            AuthenticatorMgtException.class, expectedExceptionsMessageRegExp = "The authenticator already exists.")
    public void testCreateUserDefinedLocalAuthenticatorWithExistingAuthenticator(
            UserDefinedLocalAuthenticatorConfig config) throws AuthenticatorMgtException {

        ApplicationCommonServiceDataHolder.getInstance().getApplicationAuthenticatorService()
                .addUserDefinedLocalAuthenticator(config, tenantDomain);
    }

    @Test(priority = 3, expectedExceptions = AuthenticatorMgtException.class,
            expectedExceptionsMessageRegExp = "Invalid empty or blank value.")
    public void testCreateUserDefinedLocalAuthenticatorWithBlankDisplayName() throws AuthenticatorMgtException {

        UserDefinedLocalAuthenticatorConfig config = createUserDefinedAuthenticatorConfig("withBlankDisplayName",
                AuthenticationType.IDENTIFICATION);
        config.setDisplayName("");
        ApplicationCommonServiceDataHolder.getInstance().getApplicationAuthenticatorService()
                .addUserDefinedLocalAuthenticator(config, tenantDomain);
    }

    @Test(priority = 3, expectedExceptions = AuthenticatorMgtException.class,
            expectedExceptionsMessageRegExp = "Authenticator name is invalid.")
    public void testCreateUserDefinedLocalAuthenticatorWithInvalidName() throws AuthenticatorMgtException {

        ApplicationCommonServiceDataHolder.getInstance().getApplicationAuthenticatorService()
                .addUserDefinedLocalAuthenticator(createUserDefinedAuthenticatorConfig("323#2@dwd",
                                AuthenticationType.IDENTIFICATION), tenantDomain);
    }

    @Test(priority = 4)
    public void testAddIdPActionException() throws Exception {

        ActionManagementService actionManagementServiceForException = mock(ActionManagementService.class);
        when(actionManagementServiceForException.addAction(anyString(), any(), any()))
                .thenThrow(ActionMgtException.class);
        ApplicationCommonServiceDataHolder.getInstance().setActionManagementService(
                actionManagementServiceForException);

        assertThrows(AuthenticatorMgtException.class, () ->
                ApplicationCommonServiceDataHolder.getInstance().getApplicationAuthenticatorService()
                        .addUserDefinedLocalAuthenticator(authenticatorConfigForException, tenantDomain));
    }

    @Test(priority = 5)
    public void testAddLocalAuthenticator() {

        ApplicationCommonServiceDataHolder.getInstance().getApplicationAuthenticatorService()
                .addLocalAuthenticator(systemAuthenticatorConfig);
        Assert.assertNotNull(ApplicationCommonServiceDataHolder.getInstance().getApplicationAuthenticatorService()
                .getLocalAuthenticatorByName(SYSTEM_AUTHENTICATOR_NAME));
    }

    @Test(priority = 6)
    public void testAddLocalAuthenticatorWithRuntimeError() {

        assertThrows(AuthenticatorMgtServerRuntimeException.class, () ->
                ApplicationCommonServiceDataHolder.getInstance().getApplicationAuthenticatorService()
                        .addLocalAuthenticator(authenticatorConfig1));
    }

    @Test(priority = 10)
    public void testGetAllUserDefinedLocalAuthenticators() throws Exception {

        List<UserDefinedLocalAuthenticatorConfig> authenticatorsList = ApplicationCommonServiceDataHolder.getInstance()
                .getApplicationAuthenticatorService().getAllUserDefinedLocalAuthenticators(tenantDomain);
        Assert.assertEquals(authenticatorsList.size(), 2);
    }

    @DataProvider(name = "authenticatorConfigToModify")
    public Object[][] authenticatorConfigToModify() {

        authenticatorConfig1.setDisplayName("Updated Display Name");

        authenticatorConfig2.setEnabled(false);
        authenticatorConfig2.setDefinedByType(DefinedByType.SYSTEM);

        return new Object[][]{
                {authenticatorConfig1},
                {authenticatorConfig2}
        };
    }

    @Test(priority = 10)
    public void testGetUserDefinedAuthenticator() throws Exception {

        UserDefinedLocalAuthenticatorConfig authenticator = ApplicationCommonServiceDataHolder.getInstance()
                .getApplicationAuthenticatorService().getUserDefinedLocalAuthenticator(
                        authenticatorConfig1.getName(), tenantDomain);
        Assert.assertNotNull(authenticator);
    }

    @Test(priority = 11)
    public void testGetUserDefinedAuthenticatorWithActionException() throws Exception {

        ApplicationCommonServiceDataHolder.getInstance().getApplicationAuthenticatorService()
                .addUserDefinedLocalAuthenticator(authenticatorConfigForException, tenantDomain);
        ActionManagementService actionManagementServiceForException = mock(ActionManagementService.class);
        when(actionManagementServiceForException.addAction(anyString(), any(), any())).thenReturn(action);
        when(actionManagementServiceForException.getActionByActionId(anyString(), any(), any()))
                .thenThrow(ActionMgtException.class);
        ApplicationCommonServiceDataHolder.getInstance().setActionManagementService(
                actionManagementServiceForException);
    }

    @Test(priority = 20, dataProvider = "authenticatorConfigToModify")
    public void testUpdateUserDefinedLocalAuthenticator(UserDefinedLocalAuthenticatorConfig config)
            throws AuthenticatorMgtException {

        UserDefinedLocalAuthenticatorConfig updatedAuthenticator = ApplicationCommonServiceDataHolder.getInstance()
                .getApplicationAuthenticatorService().updateUserDefinedLocalAuthenticator(config, tenantDomain);

        Assert.assertEquals(updatedAuthenticator.getName(), config.getName());
        Assert.assertEquals(updatedAuthenticator.getDisplayName(), config.getDisplayName());
        Assert.assertEquals(updatedAuthenticator.isEnabled(), config.isEnabled());
        Assert.assertEquals(updatedAuthenticator.getDefinedByType(), DefinedByType.USER);
        Assert.assertEquals(updatedAuthenticator.getProperties().length, config.getProperties().length);
    }

    @Test(priority = 21, expectedExceptions = AuthenticatorMgtException.class,
            expectedExceptionsMessageRegExp = "No Authenticator found.")
    public void testUpdateUserDefinedLocalAuthenticatorWithNonExistingAuthenticator() throws AuthenticatorMgtException {

        ApplicationCommonServiceDataHolder.getInstance().getApplicationAuthenticatorService()
                .updateUserDefinedLocalAuthenticator(nonExistAuthenticatorConfig, tenantDomain);
    }

    @Test(priority = 22)
    public void testUpdateIdPActionException() throws Exception {

        ActionManagementService actionManagementServiceForException = mock(ActionManagementService.class);
        when(actionManagementServiceForException.updateAction(any(), any(), any(), any()))
                .thenThrow(ActionMgtException.class);
        when(actionManagementServiceForException.getActionByActionId(anyString(), any(), any())).thenReturn(action);
        ApplicationCommonServiceDataHolder.getInstance().setActionManagementService(
                actionManagementServiceForException);

        assertThrows(AuthenticatorMgtException.class, () -> ApplicationCommonServiceDataHolder.getInstance()
                        .getApplicationAuthenticatorService().updateUserDefinedLocalAuthenticator(
                        authenticatorConfigForException, tenantDomain));
    }

    @DataProvider(name = "authenticatorConfigToRetrieve")
    public Object[][] authenticatorConfigToRetrieve() {

        return new Object[][]{
                {authenticatorConfig1, authenticatorConfig1, AuthenticationType.IDENTIFICATION.toString()},
                {authenticatorConfig2, authenticatorConfig2, AuthenticationType.VERIFICATION.toString()},
                {nonExistAuthenticatorConfig, null, null}
        };
    }

    @Test(priority = 27, dataProvider = "authenticatorConfigToRetrieve")
    public void testGetUserDefinedLocalAuthenticator(UserDefinedLocalAuthenticatorConfig configToBeRetrieved,
                 UserDefinedLocalAuthenticatorConfig expectedConfig, String type) throws AuthenticatorMgtException {

        UserDefinedLocalAuthenticatorConfig retrievedConfig = ApplicationCommonServiceDataHolder.getInstance()
                .getApplicationAuthenticatorService().getUserDefinedLocalAuthenticator(
                        configToBeRetrieved.getName(), tenantDomain);
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

    @Test(priority = 40)
    public void testDeleteUserDefinedLocalAuthenticatorWithActionException() throws Exception {

        ActionManagementService actionManagementServiceForException = mock(ActionManagementService.class);
        doThrow(ActionMgtException.class).when(actionManagementServiceForException).deleteAction(any(), any(), any());
        when(actionManagementServiceForException.getActionByActionId(anyString(), any(), any())).thenReturn(action);
        ApplicationCommonServiceDataHolder.getInstance()
                .setActionManagementService(actionManagementServiceForException);

        assertThrows(AuthenticatorMgtException.class, () -> ApplicationCommonServiceDataHolder.getInstance().
                getApplicationAuthenticatorService().deleteUserDefinedLocalAuthenticator(
                        authenticatorConfigForException.getName(), tenantDomain));
        Assert.assertNotNull(ApplicationCommonServiceDataHolder.getInstance().
                getApplicationAuthenticatorService().getUserDefinedLocalAuthenticator(
                        authenticatorConfigForException.getName(), tenantDomain));
    }

    @Test(priority = 50, dataProvider = "authenticatorConfigToModify")
    public void testDeleteUserDefinedLocalAuthenticator(UserDefinedLocalAuthenticatorConfig config)
            throws AuthenticatorMgtException {

        ApplicationCommonServiceDataHolder.getInstance().getApplicationAuthenticatorService()
                .deleteUserDefinedLocalAuthenticator(config.getName(), tenantDomain);
        Assert.assertNull(ApplicationCommonServiceDataHolder.getInstance().getApplicationAuthenticatorService()
                .getLocalAuthenticatorByName(config.getName()));
    }

    @Test(priority = 51, expectedExceptions = AuthenticatorMgtException.class,
            expectedExceptionsMessageRegExp = "No Authenticator found.")
    public void testDeleteUserDefinedLocalAuthenticatorWithNonExistingAuthenticator() throws AuthenticatorMgtException {

        ApplicationCommonServiceDataHolder.getInstance().getApplicationAuthenticatorService()
                .deleteUserDefinedLocalAuthenticator(nonExistAuthenticatorConfig.getName(), tenantDomain);
    }

    private UserDefinedLocalAuthenticatorConfig createUserDefinedAuthenticatorConfig(String uniqueIdentifier,
                                                                                     AuthenticationType type) {

        UserDefinedLocalAuthenticatorConfig authenticatorConfig = new
                UserDefinedLocalAuthenticatorConfig(AuthenticationType.IDENTIFICATION);
        authenticatorConfig.setName(uniqueIdentifier);
        authenticatorConfig.setDisplayName("Custom " + uniqueIdentifier);
        authenticatorConfig.setEnabled(true);
        authenticatorConfig.setDefinedByType(DefinedByType.USER);
        authenticatorConfig.setAuthenticationType(type);
        UserDefinedAuthenticatorEndpointConfigBuilder endpointConfigBuilder = buildAuthenticatorEndpointConfig();
        authenticatorConfig.setEndpointConfig(endpointConfigBuilder.build());

        return authenticatorConfig;
    }

    private LocalAuthenticatorConfig createSystemDefinedAuthenticatorConfig(String uniqueIdentifier) {

        LocalAuthenticatorConfig authenticatorConfig = new LocalAuthenticatorConfig();
        authenticatorConfig.setName(uniqueIdentifier);
        authenticatorConfig.setDisplayName("Custom " + uniqueIdentifier);
        authenticatorConfig.setEnabled(true);
        authenticatorConfig.setDefinedByType(DefinedByType.SYSTEM);
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

    private static UserDefinedAuthenticatorEndpointConfigBuilder buildAuthenticatorEndpointConfig() {

        UserDefinedAuthenticatorEndpointConfigBuilder endpointConfigBuilder =
                new UserDefinedAuthenticatorEndpointConfigBuilder();
        endpointConfigBuilder.uri("https://localhost:8080/test");
        endpointConfigBuilder.authenticationType(Authentication.Type.BASIC.getName());
        HashMap<String, String> authProperties = new HashMap<>();
        authProperties.put("username", "admin");
        authProperties.put("password", "admin");
        endpointConfigBuilder.authenticationProperties(authProperties);
        return endpointConfigBuilder;
    }
}
