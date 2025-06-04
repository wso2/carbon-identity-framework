package org.wso2.carbon.identity.input.validation.mgt.test.listener;

import org.mockito.MockedStatic;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.application.authentication.framework.exception.UserIdNotFoundException;
import org.wso2.carbon.identity.application.authentication.framework.model.AuthenticatedUser;
import org.wso2.carbon.identity.core.AbstractIdentityUserOperationEventListener;
import org.wso2.carbon.identity.input.validation.mgt.listener.InputValidationListener;
import org.wso2.carbon.user.core.UserStoreException;
import org.wso2.carbon.user.core.UserStoreManager;
import org.wso2.carbon.user.core.util.UserCoreUtil;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

public class InputValidationListenerTest {

    private UserStoreManager userStoreManager;
    private MockedStatic<UserCoreUtil> userCoreUtilMock;

    @BeforeMethod
    public void setup() {

        userStoreManager = mock(UserStoreManager.class);
        userCoreUtilMock = mockStatic(UserCoreUtil.class);

        AbstractIdentityUserOperationEventListener userOperationEventListener =
                mock(AbstractIdentityUserOperationEventListener.class);
        doReturn(true).when(userOperationEventListener).isEnable();
    }

    @AfterMethod
    public void tearDown() {
        userCoreUtilMock.close();
    }

    @DataProvider(name = "preUpdateCredentialByAdminWithIDDataProvider")
    public Object[][] preUpdateCredentialByAdminWithIDDataProvider() {

        AuthenticatedUser user = new AuthenticatedUser();
        user.setUserName("test_user");
        user.setTenantDomain("carbon.super");
        user.setUserStoreDomain("PRIMARY");
        user.setUserId("123456");

        // skipPasswordValidation, user, newCredential
        return new Object[][]{
                { true, user, "pw" },
                { true, user, "newPassword" }
        };
    }


    @Test(dataProvider = "preUpdateCredentialByAdminWithIDDataProvider")
    public void doPreUpdateCredentialByAdminWithIDTest(boolean skipPasswordValidation, AuthenticatedUser user,
                                                       String newCredential)
            throws UserStoreException, UserIdNotFoundException {

        when(UserCoreUtil.getSkipPasswordPatternValidationThreadLocal()).thenReturn(skipPasswordValidation);


        InputValidationListener inputValidationListener = mock(InputValidationListener.class);
        when(inputValidationListener.doPreUpdateCredentialByAdmin(user.getUserId(), newCredential,
                userStoreManager)).thenReturn(true);
    }

    @DataProvider(name = "doPreUpdateCredentialByAdminDataProvider")
    public Object[][] doPreUpdateCredentialByAdminDataProvider() {

        AuthenticatedUser user = new AuthenticatedUser();
        user.setUserName("test_user");
        user.setTenantDomain("carbon.super");
        user.setUserStoreDomain("PRIMARY");
        user.setUserId("123456");

        // skipPasswordValidation, user, newCredential
        return new Object[][]{
                { true, user, "pw" },
                { false, user, "newPassword" }
        };
    }


    @Test(dataProvider = "doPreUpdateCredentialByAdminDataProvider")
    public void doPreUpdateCredentialByAdminTest(boolean skipPasswordValidation, AuthenticatedUser user,
                                                       String newCredential) throws UserStoreException {

        when(UserCoreUtil.getSkipPasswordPatternValidationThreadLocal()).thenReturn(skipPasswordValidation);

        InputValidationListener inputValidationListener = mock(InputValidationListener.class);
        when(inputValidationListener.doPreUpdateCredentialByAdmin(user.getUserName(), newCredential,
                userStoreManager)).thenReturn(true);
    }
}
