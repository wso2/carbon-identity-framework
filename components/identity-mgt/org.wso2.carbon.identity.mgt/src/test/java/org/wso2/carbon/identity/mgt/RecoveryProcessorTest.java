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
import org.wso2.carbon.identity.mgt.beans.VerificationBean;
import org.wso2.carbon.identity.mgt.constants.IdentityMgtConstants;
import org.wso2.carbon.identity.mgt.dto.UserRecoveryDataDO;
import org.wso2.carbon.identity.mgt.store.UserRecoveryDataStore;

import java.util.Collections;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
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
    private UserRecoveryDataStore dataStore;
    private RecoveryProcessor recoveryProcessor;

    /**
     * Builds a RecoveryProcessor backed by a mocked configuration and recovery data store.
     */
    @BeforeMethod
    public void setUp() {

        dataStore = mock(UserRecoveryDataStore.class);

        IdentityMgtConfig config = mock(IdentityMgtConfig.class);
        when(config.getNotificationSendingModules())
                .thenReturn(Collections.singletonList(mock(NotificationSendingModule.class)));
        when(config.getRecoveryDataStore()).thenReturn(dataStore);
        when(config.getProperty(anyString())).thenReturn(null);

        identityMgtConfig = mockStatic(IdentityMgtConfig.class);
        identityMgtConfig.when(IdentityMgtConfig::getInstance).thenReturn(config);

        recoveryProcessor = new RecoveryProcessor();
    }

    /**
     * Releases the static mock of the identity management configuration.
     */
    @AfterMethod
    public void tearDown() {

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

    private String internalCodeOf(String username, String code) {

        return SEQUENCE + IdentityMgtConstants.REG_DELIMITER + username + IdentityMgtConstants.REG_DELIMITER + code;
    }
}
