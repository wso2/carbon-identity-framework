/*
 * Copyright (c) 2025, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.carbon.identity.input.validation.mgt.test.listener;

import org.mockito.MockedStatic;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.application.authentication.framework.exception.UserIdNotFoundException;
import org.wso2.carbon.identity.application.authentication.framework.model.AuthenticatedUser;
import org.wso2.carbon.identity.configuration.mgt.core.ConfigurationManager;
import org.wso2.carbon.identity.configuration.mgt.core.exception.ConfigurationManagementException;
import org.wso2.carbon.identity.configuration.mgt.core.model.Attribute;
import org.wso2.carbon.identity.configuration.mgt.core.model.Resource;
import org.wso2.carbon.identity.configuration.mgt.core.model.Resources;
import org.wso2.carbon.identity.core.AbstractIdentityUserOperationEventListener;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;
import org.wso2.carbon.identity.input.validation.mgt.internal.InputValidationDataHolder;
import org.wso2.carbon.identity.input.validation.mgt.listener.InputValidationListener;
import org.wso2.carbon.user.core.UserStoreException;
import org.wso2.carbon.user.core.UserStoreManager;
import org.wso2.carbon.user.core.util.UserCoreUtil;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;
import static org.wso2.carbon.identity.input.validation.mgt.utils.Constants.INPUT_VAL_CONFIG_RESOURCE_NAME_PREFIX;
import static org.wso2.carbon.identity.input.validation.mgt.utils.Constants.INPUT_VAL_CONFIG_RESOURCE_TYPE_NAME;

public class InputValidationListenerTest {

    private InputValidationListener inputValidationListener;
    private UserStoreManager userStoreManager;
    private ConfigurationManager configurationManager;

    private MockedStatic<UserCoreUtil> userCoreUtilStaticMock;
    private MockedStatic<IdentityTenantUtil> identityTenantUtilStaticMock;
    private MockedStatic<InputValidationDataHolder> inputValidationDataHolderStaticMock;

    @BeforeMethod
    public void setup() {

        inputValidationListener = new InputValidationListener();

        userStoreManager = mock(UserStoreManager.class);
        configurationManager = mock(ConfigurationManager.class);

        userCoreUtilStaticMock = mockStatic(UserCoreUtil.class);
        identityTenantUtilStaticMock = mockStatic(IdentityTenantUtil.class);
        inputValidationDataHolderStaticMock = mockStatic(InputValidationDataHolder.class);

        AbstractIdentityUserOperationEventListener userOperationEventListener =
                mock(AbstractIdentityUserOperationEventListener.class);
        doReturn(true).when(userOperationEventListener).isEnable();

        identityTenantUtilStaticMock.when(() -> IdentityTenantUtil.getTenantId("carbon.super"))
                .thenReturn(1234);
    }

    @AfterMethod
    public void tearDown() {
        userCoreUtilStaticMock.close();
        identityTenantUtilStaticMock.close();
        inputValidationDataHolderStaticMock.close();
    }

    @DataProvider(name = "preUpdateCredentialByAdminWithIDDataProvider")
    public Object[][] preUpdateCredentialByAdminWithIDDataProvider() {

        AuthenticatedUser user = new AuthenticatedUser();
        user.setUserName("test_user");
        user.setTenantDomain("carbon.super");
        user.setUserStoreDomain("PRIMARY");
        user.setUserId("123456");

        // skipPasswordValidation, user, newCredential, expectedResult
        return new Object[][]{
                { true, user, "pw", false },
                { false, user, "pw", false },
                { false, user, "newPassword", false }
        };
    }


    @Test(dataProvider = "preUpdateCredentialByAdminWithIDDataProvider")
    public void doPreUpdateCredentialByAdminWithIDTest(boolean skipPasswordValidation, AuthenticatedUser user,
                                                       String newCredential, boolean expectedResult)
            throws UserStoreException, UserIdNotFoundException, ConfigurationManagementException {

        when(UserCoreUtil.getSkipPasswordPatternValidationThreadLocal()).thenReturn(skipPasswordValidation);

        ConfigurationManager configurationManager = mock(ConfigurationManager.class);
        when(InputValidationDataHolder.getConfigurationManager()).thenReturn(configurationManager);
        when(configurationManager.getResourcesByType(INPUT_VAL_CONFIG_RESOURCE_TYPE_NAME)).thenReturn(getResources());

        boolean isValid = inputValidationListener.doPreUpdateCredentialByAdminWithID(user.getUserId(), newCredential,
                userStoreManager);
        Assert.assertEquals(isValid, expectedResult);

    }

    @DataProvider(name = "doPreUpdateCredentialByAdminDataProvider")
    public Object[][] doPreUpdateCredentialByAdminDataProvider() {

        AuthenticatedUser user = new AuthenticatedUser();
        user.setUserName("test_user");
        user.setTenantDomain("carbon.super");
        user.setUserStoreDomain("PRIMARY");
        user.setUserId("123456");

        // skipPasswordValidation, user, newCredential, expectedResult
        return new Object[][]{
                { true, user, "pw", false },
                { false, user, "pw", false },
                { false, user, "newPassword", false }
        };
    }

    @Test(dataProvider = "doPreUpdateCredentialByAdminDataProvider")
    public void doPreUpdateCredentialByAdminTest(boolean skipPasswordValidation, AuthenticatedUser user,
                                                       String newCredential, boolean expectedResult)
            throws UserStoreException, ConfigurationManagementException {

        when(UserCoreUtil.getSkipPasswordPatternValidationThreadLocal()).thenReturn(skipPasswordValidation);

        ConfigurationManager configurationManager = mock(ConfigurationManager.class);
        when(InputValidationDataHolder.getConfigurationManager()).thenReturn(configurationManager);
        when(configurationManager.getResourcesByType(INPUT_VAL_CONFIG_RESOURCE_TYPE_NAME)).thenReturn(getResources());

        boolean isValid = inputValidationListener.doPreUpdateCredentialByAdmin(user.getUserName(), newCredential,
                userStoreManager);
        Assert.assertEquals(isValid, expectedResult);
    }

    private Resources getResources() {

        Resources resources = new Resources();
        List<Resource> resourceList = new ArrayList<>();
        List<Attribute> attributesForPassword = new ArrayList<>();

        Attribute lengthAttribute = new Attribute();
        lengthAttribute.setKey("LengthValidator.min.length");
        lengthAttribute.setValue("5");
        attributesForPassword.add(lengthAttribute);

        Attribute attributeType = new Attribute();
        attributeType.setKey("validation.type");
        attributeType.setValue("RULE");
        attributesForPassword.add(attributeType);

        Resource resourceForPassword = new Resource();
        resourceForPassword.setAttributes(attributesForPassword);
        resourceForPassword.setResourceName(INPUT_VAL_CONFIG_RESOURCE_NAME_PREFIX + "password");
        resourceList.add(resourceForPassword);

        resources.setResources(resourceList);
        return resources;
    }
}
