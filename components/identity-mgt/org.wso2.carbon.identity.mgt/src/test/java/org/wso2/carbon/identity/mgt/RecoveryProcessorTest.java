/*
 * Copyright (c) 2026, WSO2 LLC. (https://www.wso2.com) All Rights Reserved.
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
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.identity.mgt;

import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.core.util.IdentityUtil;
import org.wso2.carbon.identity.mgt.beans.VerificationBean;
import org.wso2.carbon.identity.mgt.config.Config;
import org.wso2.carbon.identity.mgt.config.ConfigBuilder;
import org.wso2.carbon.identity.mgt.constants.IdentityMgtConstants;
import org.wso2.carbon.identity.mgt.dto.NotificationDataDTO;
import org.wso2.carbon.identity.mgt.dto.UserDTO;
import org.wso2.carbon.identity.mgt.dto.UserRecoveryDTO;
import org.wso2.carbon.identity.mgt.dto.UserRecoveryDataDO;
import org.wso2.carbon.identity.mgt.internal.IdentityMgtServiceComponent;
import org.wso2.carbon.identity.mgt.mail.Notification;
import org.wso2.carbon.identity.mgt.mail.NotificationBuilder;
import org.wso2.carbon.identity.mgt.store.UserRecoveryDataStore;
import org.wso2.carbon.identity.mgt.util.Utils;
import org.wso2.carbon.user.core.UserRealm;
import org.wso2.carbon.user.core.UserStoreManager;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.user.core.tenant.TenantManager;

import java.util.Collections;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

/**
 * Unit tests for the confirmation code generation in {@link RecoveryProcessor}.
 *
 * <p>The internal (stored) code is built as {@code sequence + "___" + username + "___" + code}. The code handed to the
 * user must therefore be generated independently and never recovered by splitting the internal code, because a username
 * that contains the delimiter is indistinguishable from the delimiter itself.</p>
 */
public class RecoveryProcessorTest {

    private static final int TENANT_ID = -1234;
    private static final int SEQUENCE = 2;

    private MockedStatic<IdentityMgtConfig> identityMgtConfig;
    private MockedStatic<IdentityMgtServiceComponent> identityMgtServiceComponent;
    private IdentityMgtConfig config;
    private UserStoreManager userStoreManager;
    private UserRecoveryDataStore dataStore;
    private RecoveryProcessor recoveryProcessor;

    /**
     * Builds a RecoveryProcessor backed by a mocked configuration and recovery data store.
     */
    @BeforeMethod
    public void setUp() throws Exception {

        dataStore = mock(UserRecoveryDataStore.class);

        config = mock(IdentityMgtConfig.class);
        when(config.getNotificationSendingModules())
                .thenReturn(Collections.singletonList(mock(NotificationSendingModule.class)));
        when(config.getRecoveryDataStore()).thenReturn(dataStore);
        when(config.getProperty(anyString())).thenReturn(null);

        identityMgtConfig = mockStatic(IdentityMgtConfig.class);
        identityMgtConfig.when(IdentityMgtConfig::getInstance).thenReturn(config);

        recoveryProcessor = new RecoveryProcessor();

        userStoreManager = mock(UserStoreManager.class);
        UserRealm userRealm = mock(UserRealm.class);
        when(userRealm.getUserStoreManager()).thenReturn(userStoreManager);
        RealmService realmService = mock(RealmService.class);
        when(realmService.getTenantUserRealm(TENANT_ID)).thenReturn(userRealm);
        when(realmService.getTenantManager()).thenReturn(mock(TenantManager.class));

        identityMgtServiceComponent = mockStatic(IdentityMgtServiceComponent.class);
        identityMgtServiceComponent.when(IdentityMgtServiceComponent::getRealmService).thenReturn(realmService);
    }

    /**
     * Releases the static mock of the identity management configuration.
     */
    @AfterMethod
    public void tearDown() {

        identityMgtServiceComponent.close();
        identityMgtConfig.close();
    }

    @DataProvider(name = "usernames")
    public Object[][] usernames() {

        return new Object[][]{
                {"normaluser"},
                {"test___user"},
                {"edge_user_"},
                {"_leadinguser"},
                {"a___b___c"},
        };
    }

    @Test(dataProvider = "usernames")
    public void testUpdateConfirmationCodeReturnsStandaloneCode(String username) throws Exception {

        VerificationBean bean = recoveryProcessor.updateConfirmationCode(SEQUENCE, username, TENANT_ID);

        assertNotNull(bean.getKey(), "No confirmation code was returned for user: " + username);
        // A bare UUID: no part of the username may leak into the code handed to the user.
        assertEquals(UUID.fromString(bean.getKey()).toString(), bean.getKey(),
                "Confirmation code is not a standalone UUID for user: " + username);

        ArgumentCaptor<UserRecoveryDataDO> captor = ArgumentCaptor.forClass(UserRecoveryDataDO.class);
        verify(dataStore).store(captor.capture());
        assertEquals(captor.getValue().getCode(), internalCodeOf(username, bean.getKey()),
                "Stored code does not follow the sequence/username/code format for user: " + username);
    }

    @Test(dataProvider = "usernames")
    public void testIssuedCodeVerifiesAgainstStoredCode(String username) throws Exception {

        VerificationBean bean = recoveryProcessor.updateConfirmationCode(SEQUENCE, username, TENANT_ID);

        ArgumentCaptor<UserRecoveryDataDO> captor = ArgumentCaptor.forClass(UserRecoveryDataDO.class);
        verify(dataStore).store(captor.capture());
        String storedCode = captor.getValue().getCode();

        UserRecoveryDataDO storedData = new UserRecoveryDataDO(username, TENANT_ID, storedCode, "secret");
        storedData.setValid(true);
        when(dataStore.load(storedCode)).thenReturn(storedData);

        // The code the user received must resolve back to the stored entry, otherwise the reset link fails with
        // "Invalid confirmation code".
        VerificationBean verification = recoveryProcessor.verifyConfirmationCode(SEQUENCE, username, bean.getKey());
        assertEquals(verification.isVerified(), true,
                "Issued confirmation code did not verify for user: " + username);
    }

    @Test(dataProvider = "usernames")
    public void testVerifyUserForRecoveryReturnsStandaloneCode(String username) throws Exception {

        when(userStoreManager.isExistingUser(username)).thenReturn(true);
        when(config.isAuthPolicyAccountLockCheck()).thenReturn(false);
        when(config.isAuthPolicyAccountDisableCheck()).thenReturn(false);

        UserDTO userDTO = new UserDTO(username);
        userDTO.setTenantId(TENANT_ID);

        VerificationBean bean = recoveryProcessor.verifyUserForRecovery(SEQUENCE, userDTO);

        assertNotNull(bean.getKey(), "No confirmation code was returned for user: " + username);
        assertEquals(UUID.fromString(bean.getKey()).toString(), bean.getKey(),
                "Confirmation code is not a standalone UUID for user: " + username);

        ArgumentCaptor<UserRecoveryDataDO> captor = ArgumentCaptor.forClass(UserRecoveryDataDO.class);
        verify(dataStore).store(captor.capture());
        assertEquals(captor.getValue().getCode(), internalCodeOf(username, bean.getKey()),
                "Stored code does not follow the sequence/username/code format for user: " + username);
    }

    @Test
    public void testVerifyUserForRecoveryWithNonExistingUser() throws Exception {

        String username = "missinguser";
        when(userStoreManager.isExistingUser(username)).thenReturn(false);

        UserDTO userDTO = new UserDTO(username);
        userDTO.setTenantId(TENANT_ID);

        VerificationBean bean = recoveryProcessor.verifyUserForRecovery(SEQUENCE, userDTO);

        assertEquals(bean.isVerified(), false, "A missing user should not be verified.");
        verify(dataStore, never()).store(any(UserRecoveryDataDO.class));
    }

    @DataProvider(name = "codeCarryingNotifications")
    public Object[][] codeCarryingNotifications() {

        Object[][] usernames = usernames();
        String[] notifications = {
                IdentityMgtConstants.Notification.PASSWORD_RESET_RECOVERY,
                IdentityMgtConstants.Notification.ASK_PASSWORD,
        };
        Object[][] cases = new Object[usernames.length * notifications.length][2];
        int i = 0;
        for (Object[] username : usernames) {
            for (String notification : notifications) {
                cases[i++] = new Object[]{username[0], notification};
            }
        }
        return cases;
    }

    @Test(dataProvider = "codeCarryingNotifications")
    public void testRecoverWithNotificationIssuesStandaloneCode(String username, String notification)
            throws Exception {

        when(config.isNotificationInternallyManaged()).thenReturn(false);

        UserRecoveryDTO recoveryDTO = new UserRecoveryDTO(username);
        recoveryDTO.setTenantId(TENANT_ID);
        recoveryDTO.setNotification(notification);
        recoveryDTO.setNotificationType("EMAIL");

        NotificationDataDTO notificationData;
        try (MockedStatic<Utils> utils = mockStatic(Utils.class);
             MockedStatic<IdentityUtil> identityUtil = mockStatic(IdentityUtil.class);
             MockedStatic<ConfigBuilder> configBuilder = mockStatic(ConfigBuilder.class);
             MockedStatic<NotificationBuilder> notificationBuilder = mockStatic(NotificationBuilder.class)) {

            identityUtil.when(() -> IdentityUtil.extractDomainFromName(anyString())).thenReturn("PRIMARY");

            utils.when(() -> Utils.getEmailAddressForUser(anyString(), anyInt()))
                    .thenReturn(username + "@example.com");
            utils.when(() -> Utils.getClaimFromUserStoreManager(anyString(), anyInt(), anyString()))
                    .thenReturn("Test");

            Config emailConfig = mock(Config.class);
            when(emailConfig.getProperty(anyString())).thenReturn("template");
            ConfigBuilder builder = mock(ConfigBuilder.class);
            when(builder.loadConfiguration(any(), any(), anyInt())).thenReturn(emailConfig);
            configBuilder.when(ConfigBuilder::getInstance).thenReturn(builder);

            notificationBuilder.when(() -> NotificationBuilder.createNotification(anyString(), anyString(), any()))
                    .thenReturn(mock(Notification.class));

            notificationData = recoveryProcessor.recoverWithNotification(recoveryDTO);
        }

        String confirmationCode = notificationData.getNotificationCode();
        assertNotNull(confirmationCode, "No confirmation code was returned for user: " + username);
        assertEquals(UUID.fromString(confirmationCode).toString(), confirmationCode,
                "Confirmation code is not a standalone UUID for user: " + username);

        ArgumentCaptor<UserRecoveryDataDO> captor = ArgumentCaptor.forClass(UserRecoveryDataDO.class);
        verify(dataStore).store(captor.capture());
        assertEquals(captor.getValue().getCode(), internalCodeOf(username, confirmationCode),
                "Stored code does not follow the sequence/username/code format for user: " + username);
    }

    private String internalCodeOf(String username, String code) {

        return SEQUENCE + IdentityMgtConstants.REG_DELIMITER + username + IdentityMgtConstants.REG_DELIMITER + code;
    }
}
