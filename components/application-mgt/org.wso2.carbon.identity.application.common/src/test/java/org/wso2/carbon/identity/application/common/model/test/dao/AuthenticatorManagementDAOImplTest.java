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

package org.wso2.carbon.identity.application.common.model.test.dao;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.application.common.dao.impl.AuthenticatorManagementDAOImpl;
import org.wso2.carbon.identity.application.common.exception.AuthenticatorMgtException;
import org.wso2.carbon.identity.application.common.model.Property;
import org.wso2.carbon.identity.application.common.model.UserDefinedLocalAuthenticatorConfig;
import org.wso2.carbon.identity.application.common.model.test.util.UserDefinedLocalAuthenticatorDataUtil;
import org.wso2.carbon.identity.base.AuthenticatorPropertyConstants;
import org.wso2.carbon.identity.common.testng.WithCarbonHome;
import org.wso2.carbon.identity.common.testng.WithH2Database;
import org.wso2.carbon.identity.common.testng.WithRealmService;
import org.wso2.carbon.identity.core.internal.component.IdentityCoreServiceDataHolder;

import static org.junit.Assert.assertThrows;
import static org.wso2.carbon.identity.application.common.util.AuthenticatorMgtExceptionBuilder.AuthenticatorMgtError.ERROR_WHILE_ADDING_AUTHENTICATOR;
import static org.wso2.carbon.identity.application.common.util.AuthenticatorMgtExceptionBuilder.AuthenticatorMgtError.ERROR_WHILE_UPDATING_AUTHENTICATOR;

@WithH2Database(files = {"dbscripts/h2.sql"})
@WithCarbonHome
@WithRealmService(injectToSingletons = {IdentityCoreServiceDataHolder.class})
public class AuthenticatorManagementDAOImplTest {

    private final int tenantId = -1234;

    private UserDefinedLocalAuthenticatorConfig authenticatorConfig1;
    private UserDefinedLocalAuthenticatorConfig authenticatorConfig2;
    private UserDefinedLocalAuthenticatorConfig authenticatorConfigForException;
    private UserDefinedLocalAuthenticatorConfig authenticatorForUpdate;
    private UserDefinedLocalAuthenticatorConfig authenticatorForUpdateForException;

    private static final String AUTHENTICATOR1_NAME = "custom-auth1";
    private static final String AUTHENTICATOR2_NAME = "custom-auth2";
    private static final String AUTHENTICATOR_CONFIG_FOR_EXCEPTION_NAME = "exception_auth";
    private static final String NON_EXIST_AUTHENTICATOR_NAME = "non_exist_auth";

    private final AuthenticatorManagementDAOImpl authenticatorManagementDAO = new AuthenticatorManagementDAOImpl();

    @BeforeClass
    public void setUpClass() {

        authenticatorConfig1 = UserDefinedLocalAuthenticatorDataUtil.createUserDefinedAuthenticatorConfig
                (AUTHENTICATOR1_NAME, AuthenticatorPropertyConstants.AuthenticationType.IDENTIFICATION);
        authenticatorConfig2 = UserDefinedLocalAuthenticatorDataUtil.createUserDefinedAuthenticatorConfig(
                AUTHENTICATOR2_NAME, AuthenticatorPropertyConstants.AuthenticationType.VERIFICATION);
        authenticatorForUpdate = UserDefinedLocalAuthenticatorDataUtil.updateUserDefinedAuthenticatorConfig(
                authenticatorConfig1);
        authenticatorForUpdateForException = UserDefinedLocalAuthenticatorDataUtil
                .updateUserDefinedAuthenticatorConfigForSQLException(authenticatorConfig1);
        authenticatorConfigForException = UserDefinedLocalAuthenticatorDataUtil
                .createUserDefinedAuthenticatorConfigForSQLException(
                AUTHENTICATOR_CONFIG_FOR_EXCEPTION_NAME,
                AuthenticatorPropertyConstants.AuthenticationType.IDENTIFICATION);

    }

    @DataProvider(name = "authenticatorConfig")
    public Object[][] authenticatorConfig() {

        return new Object[][]{
                {authenticatorConfig1},
                {authenticatorConfig2}
        };
    }

    @Test(dataProvider = "authenticatorConfig", priority = 1)
    public void testAddUserDefinedLocalAuthenticator(UserDefinedLocalAuthenticatorConfig config)
            throws AuthenticatorMgtException {

        UserDefinedLocalAuthenticatorConfig createdAuthenticator = authenticatorManagementDAO
                .addUserDefinedLocalAuthenticator(config, tenantId);
        Assert.assertNotNull(createdAuthenticator);
        Assert.assertEquals(createdAuthenticator.getName(), config.getName());
        Assert.assertEquals(createdAuthenticator.getImageUrl(), config.getImageUrl());
        Assert.assertEquals(createdAuthenticator.getDescription(), config.getDescription());
        Assert.assertEquals(createdAuthenticator.getDisplayName(), config.getDisplayName());
        Assert.assertEquals(createdAuthenticator.isEnabled(), config.isEnabled());
        Assert.assertEquals(createdAuthenticator.getDefinedByType(), config.getDefinedByType());
    }

    @Test(priority = 2)
    public void testAddUserDefinedLocalAuthenticatorWithSQLException() {

        AuthenticatorMgtException exception = assertThrows(AuthenticatorMgtException.class, () ->
                authenticatorManagementDAO.addUserDefinedLocalAuthenticator(authenticatorConfigForException, tenantId));
        Assert.assertEquals(exception.getErrorCode(), ERROR_WHILE_ADDING_AUTHENTICATOR.getCode());
    }

    @Test(priority = 3)
    public void testAddUserDefinedLocalAuthenticatorWithOutActionProperty() {

        authenticatorConfigForException.setProperties(new Property[0]);
        AuthenticatorMgtException exception = assertThrows(AuthenticatorMgtException.class, () ->
                authenticatorManagementDAO.addUserDefinedLocalAuthenticator(authenticatorConfigForException, tenantId));
        Assert.assertEquals(exception.getErrorCode(), ERROR_WHILE_ADDING_AUTHENTICATOR.getCode());
    }

    @Test(priority = 4)
    public void testUpdateUserDefinedLocalAuthenticator() throws AuthenticatorMgtException {

        UserDefinedLocalAuthenticatorConfig updatedConfig = authenticatorManagementDAO
                .updateUserDefinedLocalAuthenticator(authenticatorConfig1, authenticatorForUpdate, tenantId);
        Assert.assertNotNull(updatedConfig);
        Assert.assertEquals(updatedConfig.getName(), authenticatorForUpdate.getName());
        Assert.assertEquals(updatedConfig.getImageUrl(), authenticatorForUpdate.getImageUrl());
        Assert.assertEquals(updatedConfig.getDescription(), authenticatorForUpdate.getDescription());
        Assert.assertEquals(updatedConfig.getDisplayName(), authenticatorForUpdate.getDisplayName());
        Assert.assertEquals(updatedConfig.isEnabled(), authenticatorForUpdate.isEnabled());
        Assert.assertEquals(updatedConfig.getDefinedByType(), authenticatorForUpdate.getDefinedByType());

        authenticatorConfig1 = authenticatorForUpdate;
    }

    @Test(priority = 5)
    public void testUpdateUserDefinedLocalAuthenticatorForException() {

        AuthenticatorMgtException exception = assertThrows(AuthenticatorMgtException.class, () ->
                authenticatorManagementDAO.updateUserDefinedLocalAuthenticator(authenticatorConfig1,
                    authenticatorForUpdateForException, tenantId));
        Assert.assertEquals(exception.getErrorCode(), ERROR_WHILE_UPDATING_AUTHENTICATOR.getCode());
    }

    @Test(dataProvider = "authenticatorConfig", priority = 6)
    public void testGetUserDefinedLocalAuthenticator(UserDefinedLocalAuthenticatorConfig config)
            throws AuthenticatorMgtException {

        UserDefinedLocalAuthenticatorConfig retrievedConfig =  authenticatorManagementDAO
                .getUserDefinedLocalAuthenticator(config.getName(), tenantId);
        Assert.assertNotNull(retrievedConfig);
        Assert.assertEquals(retrievedConfig.getName(), config.getName());
        Assert.assertEquals(retrievedConfig.getImageUrl(), config.getImageUrl());
        Assert.assertEquals(retrievedConfig.getDescription(), config.getDescription());
        Assert.assertEquals(retrievedConfig.getDisplayName(), config.getDisplayName());
        Assert.assertEquals(retrievedConfig.isEnabled(), config.isEnabled());
        Assert.assertEquals(retrievedConfig.getDefinedByType(), config.getDefinedByType());
    }

    @Test(priority = 7)
    public void testGetNonExistingUserDefinedLocalAuthenticator() throws AuthenticatorMgtException {

        UserDefinedLocalAuthenticatorConfig config = authenticatorManagementDAO.getUserDefinedLocalAuthenticator(
                AUTHENTICATOR_CONFIG_FOR_EXCEPTION_NAME, tenantId);

        Assert.assertNull(config);
    }

    @Test(priority = 8)
    public void testGetUserDefinedLocalAuthenticatorForNonExist() throws AuthenticatorMgtException {

        Assert.assertNull(authenticatorManagementDAO.getUserDefinedLocalAuthenticator(
                NON_EXIST_AUTHENTICATOR_NAME, tenantId));
    }

    @Test(priority = 9)
    public void testIsExistingAuthenticatorName() throws AuthenticatorMgtException {

        Assert.assertTrue(authenticatorManagementDAO.isExistingAuthenticatorName(
                authenticatorConfig1.getName(), tenantId));
    }

    @Test(priority = 10)
    public void testIsExistingAuthenticatorNameForNonExistName() throws AuthenticatorMgtException {

        Assert.assertFalse(authenticatorManagementDAO.isExistingAuthenticatorName(
                NON_EXIST_AUTHENTICATOR_NAME, tenantId));
    }

    @Test(dataProvider = "authenticatorConfig", priority = 11)
    public void testDeleteUserDefinedLocalAuthenticator(UserDefinedLocalAuthenticatorConfig config)
            throws AuthenticatorMgtException {

        authenticatorManagementDAO.deleteUserDefinedLocalAuthenticator(config.getName(), config, tenantId);
        Assert.assertNull(authenticatorManagementDAO.getUserDefinedLocalAuthenticator(config.getName(), tenantId));
    }
}
