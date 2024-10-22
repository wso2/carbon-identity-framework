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

package org.wso2.carbon.identity.mgt.endpoint.util;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.mgt.endpoint.util.client.PreferenceRetrievalClient;
import org.wso2.carbon.identity.mgt.endpoint.util.client.PreferenceRetrievalClientException;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertTrue;

public class PreferenceRetrievalClientTest {

    public static final String tenantDomain = "testTenant";
    private static final String SELF_REGISTRATION_PROPERTY = "SelfRegistration.Enable";
    private static final String SELF_SIGN_UP_LOCK_ON_CREATION_PROPERTY = "SelfRegistration.LockOnCreation";
    private static final String AUTO_LOGIN_AFTER_SELF_SIGN_UP = "SelfRegistration.AutoLogin.Enable";
    public static final String SEND_CONFIRMATION_ON_CREATION = "SelfRegistration.SendConfirmationOnCreation";
    private static final String SELF_REG_CALLBACK_REGEX_PROP = "SelfRegistration.CallbackRegex";
    public static final String SHOW_USERNAME_UNAVAILABILITY = "SelfRegistration.ShowUsernameUnavailability";
    private static final String USERNAME_RECOVERY_PROPERTY = "Recovery.Notification.Username.Enable";
    private static final String QUESTION_PASSWORD_RECOVERY_PROPERTY = "Recovery.Question.Password.Enable";
    public static final String NOTIFICATION_PASSWORD_ENABLE_PROPERTY = "Recovery.Notification.Password.Enable";
    public static final String EMAIL_LINK_PASSWORD_RECOVERY_PROPERTY =
            "Recovery.Notification.Password.emailLink.Enable";
    public static final String SMS_OTP_PASSWORD_RECOVERY_PROPERTY = "Recovery.Notification.Password.smsOtp.Enable";
    private static final String AUTO_LOGIN_AFTER_PASSWORD_RECOVERY = "Recovery.AutoLogin.Enable";
    private static final String RECOVERY_CALLBACK_REGEX_PROP = "Recovery.CallbackRegex";
    private static final String MULTI_ATTRIBUTE_LOGIN_PROPERTY = "account.multiattributelogin.handler.enable";
    private static final String MULTI_ATTRIBUTE_LOGIN_HANDLER = "multiattribute.login.handler";
    private static final String MULTI_ATTRIBUTE_LOGIN_ALLOWED_ATTRIBUTES_PROPERTY =
            "account.multiattributelogin.handler.allowedattributes";
    public static final String SELF_SIGN_UP_CONNECTOR = "self-sign-up";
    private static final String RECOVERY_CONNECTOR = "account-recovery";
    private static final String LITE_USER_CONNECTOR = "lite-user-sign-up";
    private static final String TYPING_DNA_CONNECTOR = "typingdna-config";
    private static final String TYPING_DNA_PROPERTY = "adaptive_authentication.tdna.enable";
    private static final String LITE_REG_CALLBACK_REGEX_PROP = "LiteRegistration.CallbackRegex";
    private static final String ACCOUNT_MGT_GOVERNANCE = "Account Management";
    private static final String USER_ONBOARDING_GOVERNANCE = "User Onboarding";

    @Spy
    private PreferenceRetrievalClient preferenceRetrievalClient;

    @BeforeMethod
    public void setUp() {

        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testCheckSelfRegistration() throws PreferenceRetrievalClientException {

        doReturn(true).when(preferenceRetrievalClient)
                .checkPreference(tenantDomain, SELF_SIGN_UP_CONNECTOR, SELF_REGISTRATION_PROPERTY);
        boolean result = preferenceRetrievalClient.checkSelfRegistration(tenantDomain);
        assertTrue(result);
        verify(preferenceRetrievalClient, times(1)).checkPreference(tenantDomain, SELF_SIGN_UP_CONNECTOR,
                SELF_REGISTRATION_PROPERTY);
    }

    @Test
    public void testCheckSelfRegistrationLockOnCreation() throws PreferenceRetrievalClientException {

        doReturn(false).when(preferenceRetrievalClient)
                .checkPreference(tenantDomain, SELF_SIGN_UP_CONNECTOR, SELF_SIGN_UP_LOCK_ON_CREATION_PROPERTY);
        boolean result = preferenceRetrievalClient.checkSelfRegistrationLockOnCreation(tenantDomain);
        assertFalse(result);
        verify(preferenceRetrievalClient, times(1)).checkPreference(tenantDomain, SELF_SIGN_UP_CONNECTOR,
                SELF_SIGN_UP_LOCK_ON_CREATION_PROPERTY);
    }

    @Test
    public void testCheckSelfRegistrationSendConfirmationOnCreation() throws PreferenceRetrievalClientException {

        doReturn(true).when(preferenceRetrievalClient)
                .checkPreference(tenantDomain, SELF_SIGN_UP_CONNECTOR, SEND_CONFIRMATION_ON_CREATION);
        boolean result = preferenceRetrievalClient.checkSelfRegistrationSendConfirmationOnCreation(tenantDomain);
        assertTrue(result);
        verify(preferenceRetrievalClient, times(1)).checkPreference(tenantDomain, SELF_SIGN_UP_CONNECTOR,
                SEND_CONFIRMATION_ON_CREATION);
    }

    @Test
    public void testCheckSelfRegistrationShowUsernameUnavailability() throws PreferenceRetrievalClientException {

        doReturn(false).when(preferenceRetrievalClient)
                .checkPreference(tenantDomain, SELF_SIGN_UP_CONNECTOR, SHOW_USERNAME_UNAVAILABILITY);
        boolean result = preferenceRetrievalClient.checkSelfRegistrationShowUsernameUnavailability(tenantDomain);
        assertFalse(result);
        verify(preferenceRetrievalClient, times(1)).checkPreference(tenantDomain, SELF_SIGN_UP_CONNECTOR,
                SHOW_USERNAME_UNAVAILABILITY);
    }

    @Test
    public void testCheckUsernameRecovery() throws PreferenceRetrievalClientException {

        doReturn(true).when(preferenceRetrievalClient)
                .checkPreference(tenantDomain, RECOVERY_CONNECTOR, USERNAME_RECOVERY_PROPERTY);
        boolean result = preferenceRetrievalClient.checkUsernameRecovery(tenantDomain);
        assertTrue(result);
        verify(preferenceRetrievalClient, times(1)).checkPreference(tenantDomain, RECOVERY_CONNECTOR,
                USERNAME_RECOVERY_PROPERTY);
    }

    @Test
    public void testCheckNotificationBasedPasswordRecovery() throws PreferenceRetrievalClientException {

        doReturn(false).when(preferenceRetrievalClient)
                .checkPreference(tenantDomain, RECOVERY_CONNECTOR, NOTIFICATION_PASSWORD_ENABLE_PROPERTY);
        boolean result = preferenceRetrievalClient.checkNotificationBasedPasswordRecovery(tenantDomain);
        assertFalse(result);
        verify(preferenceRetrievalClient, times(1)).checkPreference(tenantDomain, RECOVERY_CONNECTOR,
                NOTIFICATION_PASSWORD_ENABLE_PROPERTY);
    }

    @Test
    public void testCheckEmailLinkBasedPasswordRecovery() throws PreferenceRetrievalClientException {

        doReturn(true).when(preferenceRetrievalClient)
                .checkPreference(tenantDomain, RECOVERY_CONNECTOR, EMAIL_LINK_PASSWORD_RECOVERY_PROPERTY);
        boolean result = preferenceRetrievalClient.checkEmailLinkBasedPasswordRecovery(tenantDomain);
        assertTrue(result);
        verify(preferenceRetrievalClient, times(1)).checkPreference(tenantDomain, RECOVERY_CONNECTOR,
                EMAIL_LINK_PASSWORD_RECOVERY_PROPERTY);
    }

    @Test
    public void testCheckSMSOTPBasedPasswordRecovery() throws PreferenceRetrievalClientException {

        doReturn(false).when(preferenceRetrievalClient)
                .checkPreference(tenantDomain, RECOVERY_CONNECTOR, SMS_OTP_PASSWORD_RECOVERY_PROPERTY);
        boolean result = preferenceRetrievalClient.checkSMSOTPBasedPasswordRecovery(tenantDomain);
        assertFalse(result);
        verify(preferenceRetrievalClient, times(1)).checkPreference(tenantDomain, RECOVERY_CONNECTOR,
                SMS_OTP_PASSWORD_RECOVERY_PROPERTY);
    }

    @Test
    public void testCheckQuestionBasedPasswordRecovery() throws PreferenceRetrievalClientException {

        doReturn(true).when(preferenceRetrievalClient)
                .checkPreference(tenantDomain, RECOVERY_CONNECTOR, QUESTION_PASSWORD_RECOVERY_PROPERTY);
        boolean result = preferenceRetrievalClient.checkQuestionBasedPasswordRecovery(tenantDomain);
        assertTrue(result);
        verify(preferenceRetrievalClient, times(1)).checkPreference(tenantDomain, RECOVERY_CONNECTOR,
                QUESTION_PASSWORD_RECOVERY_PROPERTY);
    }

    @Test
    public void testCheckPasswordRecovery() throws PreferenceRetrievalClientException {

        List<String> propertyNameList = new ArrayList<>();
        propertyNameList.add(NOTIFICATION_PASSWORD_ENABLE_PROPERTY);
        propertyNameList.add(QUESTION_PASSWORD_RECOVERY_PROPERTY);
        doReturn(true).when(preferenceRetrievalClient)
                .checkMultiplePreference(tenantDomain, RECOVERY_CONNECTOR, propertyNameList);
        boolean result = preferenceRetrievalClient.checkPasswordRecovery(tenantDomain);
        assertTrue(result);
        verify(preferenceRetrievalClient, times(1)).checkMultiplePreference(tenantDomain, RECOVERY_CONNECTOR,
                propertyNameList);
    }

    @Test
    public void testCheckMultiAttributeLogin() throws PreferenceRetrievalClientException {

        doReturn(true).when(preferenceRetrievalClient)
                .checkPreference(tenantDomain, MULTI_ATTRIBUTE_LOGIN_HANDLER, MULTI_ATTRIBUTE_LOGIN_PROPERTY);
        boolean result = preferenceRetrievalClient.checkMultiAttributeLogin(tenantDomain);
        assertTrue(result);
        verify(preferenceRetrievalClient, times(1)).checkPreference(tenantDomain, MULTI_ATTRIBUTE_LOGIN_HANDLER,
                MULTI_ATTRIBUTE_LOGIN_PROPERTY);
    }

    @Test
    public void testCheckMultiAttributeLoginProperty() throws PreferenceRetrievalClientException {

        String expectedAttributes = "http://wso2.org/claims/username";
        doReturn(Optional.of(expectedAttributes)).when(preferenceRetrievalClient)
                .getPropertyValue(tenantDomain, ACCOUNT_MGT_GOVERNANCE, MULTI_ATTRIBUTE_LOGIN_HANDLER,
                        MULTI_ATTRIBUTE_LOGIN_ALLOWED_ATTRIBUTES_PROPERTY);

        String result = preferenceRetrievalClient.checkMultiAttributeLoginProperty(tenantDomain);
        assertEquals(expectedAttributes, result);
        verify(preferenceRetrievalClient, times(1)).getPropertyValue(tenantDomain, ACCOUNT_MGT_GOVERNANCE,
                MULTI_ATTRIBUTE_LOGIN_HANDLER, MULTI_ATTRIBUTE_LOGIN_ALLOWED_ATTRIBUTES_PROPERTY);
    }

    @Test
    public void testCheckTypingDNA() throws PreferenceRetrievalClientException {

        doReturn(true).when(preferenceRetrievalClient)
                .checkPreference(tenantDomain, TYPING_DNA_CONNECTOR, TYPING_DNA_PROPERTY, false);
        boolean result = preferenceRetrievalClient.checkTypingDNA(tenantDomain);
        assertTrue(result);
        verify(preferenceRetrievalClient, times(1)).checkPreference(tenantDomain, TYPING_DNA_CONNECTOR,
                TYPING_DNA_PROPERTY, false);
    }

    @Test
    public void testCheckAutoLoginAfterSelfRegistrationEnabled() throws PreferenceRetrievalClientException {

        doReturn(true).when(preferenceRetrievalClient)
                .checkPreference(tenantDomain, SELF_SIGN_UP_CONNECTOR, AUTO_LOGIN_AFTER_SELF_SIGN_UP);
        boolean result = preferenceRetrievalClient.checkAutoLoginAfterSelfRegistrationEnabled(tenantDomain);
        assertTrue(result);
        verify(preferenceRetrievalClient, times(1)).checkPreference(tenantDomain, SELF_SIGN_UP_CONNECTOR,
                AUTO_LOGIN_AFTER_SELF_SIGN_UP);
    }

    @Test
    public void testCheckAutoLoginAfterPasswordRecoveryEnabled() throws PreferenceRetrievalClientException {

        doReturn(true).when(preferenceRetrievalClient)
                .checkPreference(tenantDomain, RECOVERY_CONNECTOR, AUTO_LOGIN_AFTER_PASSWORD_RECOVERY);
        boolean result = preferenceRetrievalClient.checkAutoLoginAfterPasswordRecoveryEnabled(tenantDomain);
        assertTrue(result);
        verify(preferenceRetrievalClient, times(1)).checkPreference(tenantDomain, RECOVERY_CONNECTOR,
                AUTO_LOGIN_AFTER_PASSWORD_RECOVERY);
    }

    @Test
    public void testCheckIfRecoveryCallbackURLValid() throws PreferenceRetrievalClientException {

        String callbackURL = "http://example.com/callback";
        doReturn(Optional.of("http://.*")).when(preferenceRetrievalClient)
                .getPropertyValue(tenantDomain, ACCOUNT_MGT_GOVERNANCE, RECOVERY_CONNECTOR,
                        RECOVERY_CALLBACK_REGEX_PROP);
        boolean result = preferenceRetrievalClient.checkIfRecoveryCallbackURLValid(tenantDomain, callbackURL);
        assertTrue(result);
        verify(preferenceRetrievalClient, times(1)).getPropertyValue(tenantDomain, ACCOUNT_MGT_GOVERNANCE,
                RECOVERY_CONNECTOR, RECOVERY_CALLBACK_REGEX_PROP);
    }

    @Test
    public void testCheckIfSelfRegCallbackURLValid() throws PreferenceRetrievalClientException {

        String callbackURL = "http://example.com/selfreg";
        doReturn(Optional.of("http://.*")).when(preferenceRetrievalClient)
                .getPropertyValue(tenantDomain, USER_ONBOARDING_GOVERNANCE, SELF_SIGN_UP_CONNECTOR,
                        SELF_REG_CALLBACK_REGEX_PROP);
        boolean result = preferenceRetrievalClient.checkIfSelfRegCallbackURLValid(tenantDomain, callbackURL);
        assertTrue(result);
        verify(preferenceRetrievalClient, times(1)).getPropertyValue(tenantDomain, USER_ONBOARDING_GOVERNANCE,
                SELF_SIGN_UP_CONNECTOR, SELF_REG_CALLBACK_REGEX_PROP);
    }

    @Test
    public void testCheckIfLiteRegCallbackURLValid() throws PreferenceRetrievalClientException {

        String callbackURL = "http://example.com/lite";
        doReturn(Optional.of("http://.*")).when(preferenceRetrievalClient)
                .getPropertyValue(tenantDomain, USER_ONBOARDING_GOVERNANCE, LITE_USER_CONNECTOR,
                        LITE_REG_CALLBACK_REGEX_PROP);
        boolean result = preferenceRetrievalClient.checkIfLiteRegCallbackURLValid(tenantDomain, callbackURL);
        assertTrue(result);
        verify(preferenceRetrievalClient, times(1)).getPropertyValue(tenantDomain, USER_ONBOARDING_GOVERNANCE,
                LITE_USER_CONNECTOR, LITE_REG_CALLBACK_REGEX_PROP);
    }
}
