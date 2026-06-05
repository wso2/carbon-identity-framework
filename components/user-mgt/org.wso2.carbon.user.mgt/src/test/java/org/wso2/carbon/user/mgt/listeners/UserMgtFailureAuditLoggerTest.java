/*
 * Copyright (c) 2018 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.wso2.carbon.user.mgt.listeners;

import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.Logger;

import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.appender.WriterAppender;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.layout.PatternLayout;
import org.mockito.MockedStatic;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.wso2.carbon.base.CarbonBaseConstants;
import org.wso2.carbon.identity.central.log.mgt.utils.LoggerUtils;
import org.wso2.carbon.user.core.constants.UserCoreErrorConstants;
import org.wso2.carbon.user.mgt.listeners.utils.ListenerUtils;


import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;

import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

/**
 * This class tests the functionality of {@link UserMgtFailureAuditLogger}.
 */
public class UserMgtFailureAuditLoggerTest {

    private Logger logger;
    private ByteArrayOutputStream out;
    private Appender appender;
    private UserMgtFailureAuditLogger userMgtFailureAuditLogger;
    private Writer writer;

    @BeforeClass
    public void init() {

        logger = (Logger)LogManager.getLogger("AUDIT_LOG");
        System.setProperty(CarbonBaseConstants.CARBON_HOME, ".");
    }

    @BeforeMethod
    public void initMethod() {

        out = new ByteArrayOutputStream();
        writer = new PrintWriter(out);
        LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
        Configuration config = ctx.getConfiguration();
        PatternLayout layout = PatternLayout.newBuilder().withConfiguration(config).build();
        appender = WriterAppender.newBuilder().setName("writeLogger").setTarget(writer).setLayout(layout)
                .setConfiguration(config).build();
        appender.start();
        logger.addAppender(appender);
        userMgtFailureAuditLogger = spy(UserMgtFailureAuditLogger.class);
        when(userMgtFailureAuditLogger.isEnable()).thenReturn(true);
    }

    @Test(description = "This method tests the behaviour of the audit logger, when it is disabled")
    public void listenerDisabledTestCase() {

        when(userMgtFailureAuditLogger.isEnable()).thenReturn(false);
        userMgtFailureAuditLogger.onAddRoleFailure(null, null, null, null, null, null);
    }

    @Test(description = "This method tests whether relevant audit log message is getting printed correctly")
    public void testOnUpdateRoleNameFailure() throws UnsupportedEncodingException {

        userMgtFailureAuditLogger.onUpdateRoleNameFailure(
                UserCoreErrorConstants.ErrorMessages.ERROR_CODE_ERROR_WHILE_UPDATING_ROLE_NAME.getCode(), null, null,
                null, null);

        String logMsg = out.toString(StandardCharsets.UTF_8.name());
        Assert.assertTrue(StringUtils.isNotEmpty(logMsg),
                "Audit message is not getting logged when there is a role name update failure");
        Assert.assertTrue(logMsg.contains(
                "\"Error Code\":\"" + UserCoreErrorConstants.ErrorMessages.ERROR_CODE_ERROR_WHILE_UPDATING_ROLE_NAME
                        .getCode()),
                "Error code was missing in the relevant audit log of role name update failure. Actual log message "
                        + "received " + logMsg);
    }

    @Test(description = "This method tests whether relevant audit log message is getting printed correctly when there"
            + " is an add role failure")
    public void testOnAddRoleFailure() throws UnsupportedEncodingException {

        userMgtFailureAuditLogger
                .onAddRoleFailure(UserCoreErrorConstants.ErrorMessages.ERROR_CODE_INVALID_ROLE_NAME.getCode(),
                        String.format(UserCoreErrorConstants.ErrorMessages.ERROR_CODE_INVALID_ROLE_NAME.getMessage(),
                                "test", "test123"), "test123", null, null, null);

        String logMsg = out.toString(StandardCharsets.UTF_8.name());
        Assert.assertTrue(StringUtils.isNotEmpty(logMsg),
                "Audit message is not getting logged when there is add role failure");
        Assert.assertTrue(logMsg.contains("\"Error Message\":\"" + String
                        .format(UserCoreErrorConstants.ErrorMessages.ERROR_CODE_INVALID_ROLE_NAME.getMessage(), "test",
                                "test123")),
                "Error message was missing in the relevant audit log of add role failure. Actual log message "
                        + "received " + logMsg);
    }

    @Test(description = "This method tests whether relevant audit log message is getting printed correctly when there"
            + " is failure while adding user")
    public void testOnAddUserFailure() throws UnsupportedEncodingException {

        userMgtFailureAuditLogger
                .onAddUserFailure(UserCoreErrorConstants.ErrorMessages.ERROR_CODE_USER_ALREADY_EXISTS.getCode(),
                        String.format(UserCoreErrorConstants.ErrorMessages.ERROR_CODE_USER_ALREADY_EXISTS.getMessage(),
                                "existing-user"), null, null, null, null, null, null);
        String logMsg = out.toString(StandardCharsets.UTF_8.name());
        Assert.assertTrue(StringUtils.isNotEmpty(logMsg),
                "Audit message is not getting logged when there is add user failure");
        Assert.assertTrue(logMsg.contains("\"Error Message\":\"" + String
                        .format(UserCoreErrorConstants.ErrorMessages.ERROR_CODE_USER_ALREADY_EXISTS.getMessage(),
                                "existing-user")),
                "Error message was missing in the relevant audit log of add user failure. Actual log message "
                        + "received " + logMsg);
    }

    @Test(description = "This method tests whether relevant audit log message is getting printed correctly when there"
            + " is a failure while updating credential")
    public void testOnUpdateCredentialFailure() throws UnsupportedEncodingException {

        userMgtFailureAuditLogger.onUpdateCredentialFailure(
                UserCoreErrorConstants.ErrorMessages.ERROR_CODE_OLD_CREDENTIAL_DOES_NOT_MATCH.getCode(),
                UserCoreErrorConstants.ErrorMessages.ERROR_CODE_OLD_CREDENTIAL_DOES_NOT_MATCH.getMessage(), null, null,
                null, null);
        String logMsg = out.toString(StandardCharsets.UTF_8.name());
        Assert.assertTrue(StringUtils.isNotEmpty(logMsg),
                "Audit message is not getting logged when there is add user failure");
        Assert.assertTrue(logMsg.contains("Outcome=Failure"),
                "Result state was missing in the relevant audit log of update credential failure. Actual log message "
                        + "received " + logMsg);
    }

    @Test(description = "This method tests whether relevant audit log message is getting printed correctly when there"
            + " is a failure while updating credential by admin")
    public void testOnUpdateCredentialByAdminFailure() throws UnsupportedEncodingException {

        userMgtFailureAuditLogger.onUpdateCredentialByAdminFailure(
                UserCoreErrorConstants.ErrorMessages.ERROR_CODE_UNSUPPORTED_CREDENTIAL_TYPE.getCode(),
                UserCoreErrorConstants.ErrorMessages.ERROR_CODE_UNSUPPORTED_CREDENTIAL_TYPE.getMessage(), "test", null,
                null);
        String logMsg = out.toString(StandardCharsets.UTF_8.name());
        Assert.assertTrue(StringUtils.isNotEmpty(logMsg),
                "Audit message is not getting logged when there is update credential by admin failure");
        Assert.assertTrue(logMsg.contains("Target=test"),
                "Target is missing in the relevant audit log of update credential by admin failure. Actual log "
                        + "message received " + logMsg);
    }

    @Test(description = "This method tests whether relevant audit log message is getting printed correctly when there"
            + " is a failure while deleting a user")
    public void testOnDeleteUserFailure() throws UnsupportedEncodingException {

        userMgtFailureAuditLogger
                .onDeleteUserFailure(UserCoreErrorConstants.ErrorMessages.ERROR_CODE_NON_EXISTING_USER.getCode(),
                        String.format(UserCoreErrorConstants.ErrorMessages.ERROR_CODE_NON_EXISTING_USER.getMessage(),
                                "test", "primary"), "test", null);
        String logMsg = out.toString(StandardCharsets.UTF_8.name());
        Assert.assertTrue(StringUtils.isNotEmpty(logMsg),
                "Audit message is not getting logged when there is a failure while deleting a user ");
        Assert.assertTrue(logMsg.contains("Initiator=" + ListenerUtils.getUser()),
                "Initiator is missing in the relevant audit log of delete user failure. Actual log "
                        + "message received " + logMsg);
    }

    @Test(description = "This method tests whether relevant audit message is getting printed correctly when there is "
            + "failure while setting user claim value.")
    public void testOnSetUserClaimValueFailure() throws UnsupportedEncodingException {

        userMgtFailureAuditLogger.onSetUserClaimValueFailure(
                UserCoreErrorConstants.ErrorMessages.ERROR_CODE_UNABLE_TO_FETCH_CLAIM_MAPPING.getCode(), String.format(
                        UserCoreErrorConstants.ErrorMessages.ERROR_CODE_UNABLE_TO_FETCH_CLAIM_MAPPING.getMessage(),
                        "test"), "test", null, null, null, null);
        String logMsg = out.toString(StandardCharsets.UTF_8.name());
        Assert.assertTrue(StringUtils.isNotEmpty(logMsg),
                "Audit message is not getting logged when there is a failure while setting user claim value.");
        Assert.assertTrue(logMsg.contains("Target=test"),
                "Target is missing in the relevant audit log while setting user claim value. Actual log "
                        + "message received " + logMsg);
    }

    @Test(description = "This method tests whether relevant audit message is getting printed correctly when there is "
            + "failure while updating user list of role.")
    public void testOnUpdateUserListOfRoleFailure() throws UnsupportedEncodingException {

        userMgtFailureAuditLogger.onUpdateUserListOfRoleFailure(
                UserCoreErrorConstants.ErrorMessages.ERROR_CODE_CANNOT_UPDATE_EVERYONE_ROLE.getCode(),
                UserCoreErrorConstants.ErrorMessages.ERROR_CODE_CANNOT_UPDATE_EVERYONE_ROLE.getMessage(), "test",
                new String[] {}, null, null);
        String logMsg = out.toString(StandardCharsets.UTF_8.name());
        Assert.assertTrue(StringUtils.isNotEmpty(logMsg),
                "Audit message is not getting logged when there is a failure while updating user list of role.");
        Assert.assertTrue(logMsg.contains(
                "\"Error Code\":\"" + UserCoreErrorConstants.ErrorMessages.ERROR_CODE_CANNOT_UPDATE_EVERYONE_ROLE
                        .getCode()),
                "Error code is missing in the relevant audit log while updating user list of role. Actual log "
                        + "message received " + logMsg);
    }

    @Test(description = "This method tests whether relevant audit message is getting printed correctly when there is "
            + "failure while updating role list of user.")
    public void testOnUpdateRoleListOfUserFailure() throws UnsupportedEncodingException {

        userMgtFailureAuditLogger.onUpdateRoleListOfUserFailure(
                UserCoreErrorConstants.ErrorMessages.ERROR_CODE_CANNOT_UPDATE_ADMIN_ROLE.getCode(),
                UserCoreErrorConstants.ErrorMessages.ERROR_CODE_CANNOT_UPDATE_ADMIN_ROLE.getMessage(), "test",
                new String[] {}, new String[] {}, null);
        String logMsg = out.toString(StandardCharsets.UTF_8.name());
        Assert.assertTrue(StringUtils.isNotEmpty(logMsg),
                "Audit message is not getting logged when there is a failure while updating role list of user.");
    }

    @Test(description = "This method tests whether relevant audit message is getting printed correctly when there is "
            + "failure while getting user list.")
    public void testOnGetUserListFailure() throws UnsupportedEncodingException {

        userMgtFailureAuditLogger.onGetUserListFailure(
                UserCoreErrorConstants.ErrorMessages.ERROR_CODE_ERROR_DURING_PRE_GET_USER_LIST.getCode(),
                UserCoreErrorConstants.ErrorMessages.ERROR_CODE_ERROR_DURING_PRE_GET_USER_LIST.getMessage(), null, null,
                null, null);
        String logMsg = out.toString(StandardCharsets.UTF_8.name());
        Assert.assertTrue(StringUtils.isNotEmpty(logMsg),
                "Audit message is not getting logged when there is a failure while getting user list.");
    }

    @Test(description = "This method tests whether relevant audit message is getting printed correctly when there is "
            + "failure while getting claim values of a user.")
    public void testOnGetUserClaimValuesFailure() throws UnsupportedEncodingException {

        userMgtFailureAuditLogger.onGetUserClaimValuesFailure(
                UserCoreErrorConstants.ErrorMessages.ERROR_CODE_ERROR_DURING_POST_GET_USER_CLAIM_VALUE.getCode(),
                UserCoreErrorConstants.ErrorMessages.ERROR_CODE_ERROR_DURING_POST_GET_USER_CLAIM_VALUE.getMessage(),
                null, new String[] {}, null, null);
        String logMsg = out.toString(StandardCharsets.UTF_8.name());
        Assert.assertTrue(StringUtils.isNotEmpty(logMsg),
                "Audit message is not getting logged when there is a failure while getting user claim values.");
    }

    @Test(description = "This method tests whether relevant audit message is getting printed correctly when there is "
            + "failure while deleting user claim value")
    public void testOnDeleteUserClaimValueFailure() throws UnsupportedEncodingException {

        userMgtFailureAuditLogger.onDeleteUserClaimValueFailure(
                UserCoreErrorConstants.ErrorMessages.ERROR_CODE_ERROR_WHILE_DELETING_USER_CLAIM_VALUE.getCode(),
                UserCoreErrorConstants.ErrorMessages.ERROR_CODE_ERROR_WHILE_DELETING_USER_CLAIM_VALUE.getMessage(),
                null, null, null, null);
        String logMsg = out.toString(StandardCharsets.UTF_8.name());
        Assert.assertTrue(StringUtils.isNotEmpty(logMsg),
                "Audit message is not getting logged when there is a failure while deleting user claim value.");
    }

    @Test(description = "This method tests whether relevant audit message is getting printed correctly when there is "
            + "failure while getting claim value of a user.")
    public void testOnGetUserClaimValueFailure() throws UnsupportedEncodingException {

        userMgtFailureAuditLogger.onGetUserClaimValueFailure(
                UserCoreErrorConstants.ErrorMessages.ERROR_CODE_ERROR_WHILE_GETTING_USER_CLAIM_VALUE.getCode(),
                UserCoreErrorConstants.ErrorMessages.ERROR_CODE_ERROR_WHILE_GETTING_USER_CLAIM_VALUE.getMessage(), null,
                null, null, null);
        String logMsg = out.toString(StandardCharsets.UTF_8.name());
        Assert.assertTrue(StringUtils.isNotEmpty(logMsg),
                "Audit message is not getting logged when there is a failure while getting user claim value.");
    }

    @Test(description = "This method tests whether relevant audit message is getting printed correctly when there is "
            + "failure while setting claim values for a user.")
    public void testOnSetUserClaimValuesFailure() throws UnsupportedEncodingException {

        userMgtFailureAuditLogger.onSetUserClaimValuesFailure(
                UserCoreErrorConstants.ErrorMessages.ERROR_CODE_ERROR_WHILE_SETTING_USER_CLAIM_VALUES.getCode(),
                UserCoreErrorConstants.ErrorMessages.ERROR_CODE_ERROR_WHILE_SETTING_USER_CLAIM_VALUES.getMessage(),
                null, null, null, null);
        String logMsg = out.toString(StandardCharsets.UTF_8.name());
        Assert.assertTrue(StringUtils.isNotEmpty(logMsg),
                "Audit message is not getting logged when there is a failure while setting user claim values.");
    }

    @Test(description = "This method tests whether relevant audit message is getting printed correctly when there is "
            + "failure while deleting user claim values")
    public void testOnDeleteUserClaimValuesFailure() throws UnsupportedEncodingException {

        userMgtFailureAuditLogger.onDeleteUserClaimValuesFailure(
                UserCoreErrorConstants.ErrorMessages.ERROR_CODE_ERROR_WHILE_DELETING_USER_CLAIM_VALUES.getCode(),
                UserCoreErrorConstants.ErrorMessages.ERROR_CODE_ERROR_WHILE_DELETING_USER_CLAIM_VALUES.getMessage(),
                null, null, null, null);
        String logMsg = out.toString(StandardCharsets.UTF_8.name());
        Assert.assertTrue(StringUtils.isNotEmpty(logMsg),
                "Audit message is not getting logged when there is a failure while deleting user claim values.");
    }

    @Test(description = "This method tests whether relevant audit message is getting printed correctly when there is "
            + "failure while deleting role")
    public void testOnDeleteRoleFailure() throws UnsupportedEncodingException {

        userMgtFailureAuditLogger
                .onDeleteRoleFailure(UserCoreErrorConstants.ErrorMessages.ERROR_CODE_ERROR_WHILE_DELETE_ROLE.getCode(),
                        UserCoreErrorConstants.ErrorMessages.ERROR_CODE_ERROR_WHILE_DELETE_ROLE.getMessage(), null,
                        null);
        String logMsg = out.toString(StandardCharsets.UTF_8.name());
        Assert.assertTrue(StringUtils.isNotEmpty(logMsg),
                "Audit message is not getting logged when there is a failure while deleting role.");
    }

    /**
     * Direct unit test of the private helper {@code getErrorMessageWithMaskedUsername} via reflection. Asserts the
     * four corners of the null-guard:
     * <ul>
     *   <li>both args null &rarr; return null (no NPE);</li>
     *   <li>null errorMessage with non-null userName &rarr; return null;</li>
     *   <li>non-null errorMessage with null userName &rarr; return the original errorMessage unchanged;</li>
     *   <li>non-null errorMessage with empty userName &rarr; return the original errorMessage unchanged.</li>
     * </ul>
     */
    @Test(description = "getErrorMessageWithMaskedUsername null-guard returns the original message instead of NPE")
    public void testGetErrorMessageWithMaskedUsernameNullGuard() throws Exception {

        boolean originalMasking = LoggerUtils.isLogMaskingEnable;
        try {
            LoggerUtils.isLogMaskingEnable = true;

            Method method = UserMgtFailureAuditLogger.class.getDeclaredMethod(
                    "getErrorMessageWithMaskedUsername", String.class, String.class);
            method.setAccessible(true);

            // Case 1: both null.
            Assert.assertNull(method.invoke(null, null, null),
                    "Both-args-null should return null without NPE.");

            // Case 2: null errorMessage, non-null username.
            Assert.assertNull(method.invoke(null, null, "alice"),
                    "Null errorMessage should pass through as null.");

            // Case 3: non-null errorMessage, null username — the exact scenario the bug exposed.
            String original = "User addition failed";
            Assert.assertEquals(method.invoke(null, original, null), original,
                    "Null username must short-circuit and return the original error message.");

            // Case 4: non-null errorMessage, empty username.
            Assert.assertEquals(method.invoke(null, original, ""), original,
                    "Empty username must short-circuit and return the original error message.");
        } finally {
            LoggerUtils.isLogMaskingEnable = originalMasking;
        }
    }

    /**
     * Verify the happy path is unchanged — when both args are valid AND log masking is enabled
     * AND the username appears in the error message, the username is masked. This guards against
     * over-eager null-guards that would skip masking in the supported case.
     */
    @Test(description = "Happy path: when masking is on and the username is contained in the message, it is masked")
    public void testGetErrorMessageWithMaskedUsernameMasksWhenEnabled() throws Exception {

        boolean originalMasking = LoggerUtils.isLogMaskingEnable;
        try (MockedStatic<LoggerUtils> loggerUtilsMockedStatic = mockStatic(LoggerUtils.class)) {
            LoggerUtils.isLogMaskingEnable = true;
            loggerUtilsMockedStatic.when(() -> LoggerUtils.getMaskedContent("alice")).thenReturn("a***e");

            Method method = UserMgtFailureAuditLogger.class.getDeclaredMethod(
                    "getErrorMessageWithMaskedUsername", String.class, String.class);
            method.setAccessible(true);

            String result = (String) method.invoke(null, "User alice could not be added", "alice");
            Assert.assertEquals(result, "User a***e could not be added",
                    "Username should be masked in the error message when masking is enabled.");
        } finally {
            LoggerUtils.isLogMaskingEnable = originalMasking;
        }
    }

    /**
     * Verify that when log masking is disabled, the helper returns the message untouched
     * regardless of whether the username appears in it.
     */
    @Test(description = "When masking is disabled, the error message is returned unchanged")
    public void testGetErrorMessageWithMaskedUsernameNoMaskingWhenDisabled() throws Exception {

        boolean originalMasking = LoggerUtils.isLogMaskingEnable;
        try {
            LoggerUtils.isLogMaskingEnable = false;

            Method method = UserMgtFailureAuditLogger.class.getDeclaredMethod(
                    "getErrorMessageWithMaskedUsername", String.class, String.class);
            method.setAccessible(true);

            String original = "User alice could not be added";
            Assert.assertEquals(method.invoke(null, original, "alice"), original,
                    "When log masking is disabled the error message must be returned unchanged.");
        } finally {
            LoggerUtils.isLogMaskingEnable = originalMasking;
        }
    }

    @AfterMethod
    public void tearDown() {

        logger.removeAppender(appender);
    }
}
