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

package services;

import org.mockito.MockedStatic;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.base.IdentityException;
import org.wso2.carbon.identity.mgt.IdentityMgtConfig;
import org.wso2.carbon.identity.mgt.RecoveryProcessor;
import org.wso2.carbon.identity.mgt.beans.VerificationBean;
import org.wso2.carbon.identity.mgt.constants.IdentityMgtConstants;
import org.wso2.carbon.identity.mgt.dto.UserChallengesDTO;
import org.wso2.carbon.identity.mgt.dto.UserDTO;
import org.wso2.carbon.identity.mgt.internal.IdentityMgtServiceComponent;
import org.wso2.carbon.identity.mgt.services.UserInformationRecoveryService;
import org.wso2.carbon.identity.mgt.util.UserIdentityManagementUtil;
import org.wso2.carbon.identity.mgt.util.Utils;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.user.core.tenant.TenantManager;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

public class UserInformationRecoveryServiceTest {

    private static final String EXISTING_USER = "Username already exists in the system";
    private static final String INVALID_CLAIM_URL = "InvalidClaimUrl";
    private static final String EXISTING_ROLE = "RoleExisting";
    private static final String READ_ONLY_STORE = "User store is read only";
    private static final String READ_ONLY_PRIMARY_STORE = "ReadOnlyPrimaryUserStoreManager";
    private static final String INVALID_ROLE = "InvalidRole";
    private static final String NO_READ_WRITE_PERMISSIONS = "NoReadWritePermission";
    private static final String PASSWORD_INVALID = "Credential must be a non null string";
    private static final String INVALID_USER_NAME = "InvalidUserName";
    private static final String PASSWORD_POLICY_VIOLATION = "Password at least should have";;
    
    private UserIdentityManagementUtil userIdentityManagementUtil;
    private IdentityException mockIdentityException;
    private Exception mockException;

    @BeforeMethod
    public void init() {

        userIdentityManagementUtil = new UserIdentityManagementUtil();
        mockIdentityException = mock(IdentityException.class);
        mockException = mock(Exception.class);

    }

    @Test
    public void testVerifyConfirmationCodeFailure() throws IdentityException, UserStoreException {

        try (MockedStatic<IdentityMgtConfig> mockIdentityMgtConfig = mockStatic(IdentityMgtConfig.class);
             MockedStatic<Utils> utils = mockStatic(Utils.class);
             MockedStatic<IdentityMgtServiceComponent> identityMgtServiceComponent = mockStatic(IdentityMgtServiceComponent.class)) {

            IdentityMgtConfig identityMgtConfig = mock(IdentityMgtConfig.class);
            RealmService realmService = mock(RealmService.class);
            RecoveryProcessor recoveryProcessor = mock(RecoveryProcessor.class);
            TenantManager tenantManager = mock(TenantManager.class);

            mockIdentityMgtConfig.when(IdentityMgtConfig::getInstance).thenReturn(identityMgtConfig);
            utils.when(() -> Utils.processUserId(anyString())).thenReturn(new UserDTO(""));
            identityMgtServiceComponent.when(IdentityMgtServiceComponent::getRealmService).thenReturn(realmService);
            identityMgtServiceComponent.when(IdentityMgtServiceComponent::getRecoveryProcessor).thenReturn(recoveryProcessor);


            when(realmService.getTenantManager()).thenReturn(tenantManager);
            when(tenantManager.getTenantId(anyString())).thenReturn(-1234);
            when(recoveryProcessor.verifyConfirmationCode(anyInt(), anyString(), anyString())).thenThrow(new IdentityException(""));

            UserInformationRecoveryService userInformationRecoveryService = new UserInformationRecoveryService();
            userInformationRecoveryService.verifyConfirmationCode("", "", null);
            userInformationRecoveryService.updatePassword("", "", "");
            userInformationRecoveryService.getUserChallengeQuestionIds("", "");
            userInformationRecoveryService.getUserChallengeQuestion("", "", "");
            userInformationRecoveryService.getUserChallengeQuestions("", "");
            userInformationRecoveryService.verifyUserChallengeAnswer("", "", "", "");

            UserChallengesDTO[] userChallengesDTOs = { new UserChallengesDTO() };
            userInformationRecoveryService.verifyUserChallengeAnswers("", "", userChallengesDTOs);
        }
    }

    @Test
    public void testExpiredCode() {
        
        when(mockIdentityException.getMessage()).thenReturn(VerificationBean.ERROR_CODE_EXPIRED_CODE);
        VerificationBean result = userIdentityManagementUtil.getCustomErrorMessagesForCodeVerification(mockIdentityException, "testUser");
        assertNotNull(result);
        assertEquals("18002 The code is expired", result.getError());
    }

    @Test
    public void testInvalidConfirmationCode() {
        
        when(mockIdentityException.getMessage()).thenReturn(IdentityMgtConstants.ErrorHandling.INVALID_CONFIRMATION_CODE);
        VerificationBean result = userIdentityManagementUtil.getCustomErrorMessagesForCodeVerification(mockIdentityException, "testUser");
        assertNotNull(result);
        assertEquals("18001  Invalid confirmation code ", result.getError());
    }

    @Test
    public void testLoadingDataFailureCodeVerification() {
        
        when(mockIdentityException.getMessage()).thenReturn(VerificationBean.ERROR_CODE_LOADING_DATA_FAILURE);
        VerificationBean result = userIdentityManagementUtil.getCustomErrorMessagesForCodeVerification(mockIdentityException, "testUser");
        assertNotNull(result);
        assertEquals("18014 Error loading data for user : testUser", result.getError());
    }

    @Test
    public void testExternalCodeErrorCodeVerification() {

        when(mockIdentityException.getMessage()).thenReturn(IdentityMgtConstants.ErrorHandling.EXTERNAL_CODE);
        VerificationBean result = userIdentityManagementUtil.getCustomErrorMessagesForCodeVerification(mockIdentityException, "testUser");
        assertNotNull(result);
        assertEquals("18001 Error occurred while getting external code for user : : testUser", result.getError());
    }

    @Test
    public void testNotificationFailureCodeVerification() {

        when(mockIdentityException.getMessage()).thenReturn(IdentityMgtConstants.ErrorHandling.NOTIFICATION_FAILURE);
        VerificationBean result = userIdentityManagementUtil.getCustomErrorMessagesForCodeVerification(mockIdentityException, "testUser");
        assertNotNull(result);
        assertEquals("18015 Notification sending failure. Notification address is not defined for user:: testUser", result.getError());
    }

    @Test
    public void testErrorLoadingEmailTemplateCodeVerification() {

        when(mockIdentityException.getMessage()).thenReturn(IdentityMgtConstants.ErrorHandling.ERROR_LOADING_EMAIL_TEMP);
        VerificationBean result = userIdentityManagementUtil.getCustomErrorMessagesForCodeVerification(mockIdentityException, "testUser");
        assertNotNull(result);
        assertEquals("18015: Error occurred while loading email templates for user :  testUser", result.getError());
    }

    @Test
    public void testCreatingNotificationErrorCodeVerification() {

        when(mockIdentityException.getMessage()).thenReturn(IdentityMgtConstants.ErrorHandling.CREATING_NOTIFICATION_ERROR);
        VerificationBean result = userIdentityManagementUtil.getCustomErrorMessagesForCodeVerification(mockIdentityException, "testUser");
        assertNotNull(result);
        assertEquals("18015: Error occurred while creating notification for user :  testUser", result.getError());
    }

    @Test
    public void testNoUserAccountFoundCodeVerification() {
        
        when(mockIdentityException.getMessage()).thenReturn(IdentityMgtConstants.ErrorHandling.USER_ACCOUNT);
        VerificationBean result = userIdentityManagementUtil.getCustomErrorMessagesForCodeVerification(mockIdentityException, "testUser");
        assertNotNull(result);
        assertEquals("18001 No user account found for user", result.getError());
    }

    @Test
    public void testNullExceptionMessageCodeVerification() {
        
        when(mockIdentityException.getMessage()).thenReturn(null);
        VerificationBean result = userIdentityManagementUtil.getCustomErrorMessagesForCodeVerification(mockIdentityException, "testUser");
        assertNotNull(result);
        assertEquals("18001 No user account found for user", result.getError());
    }

    @Test
    public void testInvalidPasswordRegistration() {

        when(mockException.getMessage()).thenReturn(PASSWORD_INVALID);
        VerificationBean result = userIdentityManagementUtil.retrieveCustomErrorMessagesForRegistration(mockException, "testUser");
        assertNotNull(result);
        assertEquals("17002 Credential not valid. Credential must be a non null for the user : testUser", result.getError());
    }

    @Test
    public void testExistingUserRegistration() {

        when(mockException.getMessage()).thenReturn(EXISTING_USER);
        VerificationBean result = userIdentityManagementUtil.retrieveCustomErrorMessagesForRegistration(mockException, "testUser");
        assertNotNull(result);
        assertEquals("18003 Username 'testUser' already exists in the system. Please enter another username.", result.getError());
    }

    @Test
    public void testInvalidClaimUrlRegistration() {

        when(mockException.getMessage()).thenReturn(INVALID_CLAIM_URL);
        VerificationBean result = userIdentityManagementUtil.retrieveCustomErrorMessagesForRegistration(mockException, "testUser");
        assertNotNull(result);
        assertEquals("18013 Invalid claim uri has been provided.", result.getError());
    }

    @Test
    public void testInvalidUserNameRegistration() {

        when(mockException.getMessage()).thenReturn(INVALID_USER_NAME);
        VerificationBean result = userIdentityManagementUtil.retrieveCustomErrorMessagesForRegistration(mockException, "testUser");
        assertNotNull(result);
        assertEquals("18003 Username testUser is not valid. User name must be a non null", result.getError());
    }

    @Test
    public void testReadOnlyStoreRegistration() {

        when(mockException.getMessage()).thenReturn(READ_ONLY_STORE);
        VerificationBean result = userIdentityManagementUtil.retrieveCustomErrorMessagesForRegistration(mockException, "testUser");
        assertNotNull(result);
        assertEquals("18013 Read-only UserStoreManager. Roles cannot be added or modified.", result.getError());
    }

    @Test
    public void testReadOnlyPrimaryStoreRegistration() {

        when(mockException.getMessage()).thenReturn(READ_ONLY_PRIMARY_STORE);
        VerificationBean result = userIdentityManagementUtil.retrieveCustomErrorMessagesForRegistration(mockException, "testUser");
        assertNotNull(result);
        assertEquals("18013 Cannot add role to Read Only user store unless it is primary.", result.getError());
    }

    @Test
    public void testInvalidRoleRegistration() {

        when(mockException.getMessage()).thenReturn(INVALID_ROLE);
        VerificationBean result = userIdentityManagementUtil.retrieveCustomErrorMessagesForRegistration(mockException, "testUser");
        assertNotNull(result);
        assertEquals("18013 Invalid role name. Role name must be a non null string.", result.getError());
    }

    @Test
    public void testNoReadWritePermissionsRegistration() {

        when(mockException.getMessage()).thenReturn(NO_READ_WRITE_PERMISSIONS);
        VerificationBean result = userIdentityManagementUtil.retrieveCustomErrorMessagesForRegistration(mockException, "testUser");
        assertNotNull(result);
        assertEquals("18013 Role cannot be added. User store is read only or cannot write groups.", result.getError());
    }

    @Test
    public void testExistingRoleRegistration() {

        when(mockException.getMessage()).thenReturn(EXISTING_ROLE);
        VerificationBean result = userIdentityManagementUtil.retrieveCustomErrorMessagesForRegistration(mockException, "testUser");
        assertNotNull(result);
        assertEquals("18013 Role already exists in the system. Please enter another role name.", result.getError());
    }

    @Test
    public void testPasswordPolicyViolationRegistration() {

        when(mockException.getMessage()).thenReturn(PASSWORD_POLICY_VIOLATION);
        VerificationBean result = userIdentityManagementUtil.retrieveCustomErrorMessagesForRegistration(mockException, "testUser");
        assertNotNull(result);
        assertEquals("18013 " + PASSWORD_POLICY_VIOLATION, result.getError());
    }

    @Test
    public void testUnexpectedErrorRegistration() {

        when(mockException.getMessage()).thenReturn("Some unexpected error");
        VerificationBean result = userIdentityManagementUtil.retrieveCustomErrorMessagesForRegistration(mockException, "testUser");
        assertNotNull(result);
        assertEquals("18013 Error occurred while adding user : testUser", result.getError());
    }

    @Test
    public void testNullExceptionMessageRegistration() {

        when(mockException.getMessage()).thenReturn(null);
        VerificationBean result = userIdentityManagementUtil.retrieveCustomErrorMessagesForRegistration(mockException, "testUser");
        assertNotNull(result);
        assertEquals("18013 Error occurred while adding user : testUser", result.getError());
    }
}
