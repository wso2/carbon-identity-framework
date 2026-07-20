/*
 * Copyright (c) 2026, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.carbon.identity.central.log.mgt.utils;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;

/**
 * Unit tests for {@link LoggerUtils#getSanitizedErrorMessage(String, String)}.
 *
 */
public class LoggerUtilsTest {

    @AfterMethod
    public void resetMaskingFlag() {

        LoggerUtils.isLogMaskingEnable = false;
    }

    /**
     * Core regression: masking enabled + null/blank/empty userName must NOT throw and must return
     * the message unchanged. Before the fix this threw NPE ("s is null") inside String.contains.
     */
    @Test(dataProvider = "blankUserNames")
    public void testBlankUserNameReturnsMessageUnchanged(String userName) {

        LoggerUtils.isLogMaskingEnable = true;
        String errorMessage = "Some error for admin.";
        assertEquals(LoggerUtils.getSanitizedErrorMessage(errorMessage, userName), errorMessage,
                "Blank userName must return the original error message unchanged.");
    }

    @DataProvider(name = "blankUserNames")
    public Object[][] blankUserNames() {

        return new Object[][]{{""}, {"   "}, {null}};
    }

    /**
     * Masking disabled: null userName must still not throw and the message is returned as-is.
     */
    @Test
    public void testNullUserNameWithMaskingDisabledDoesNotThrow() {

        LoggerUtils.isLogMaskingEnable = false;
        String errorMessage = "Persisted access token data not found.";
        assertEquals(LoggerUtils.getSanitizedErrorMessage(errorMessage, null), errorMessage,
                "Masking disabled must return the original message unchanged.");
    }

    /**
     * Existing behavior preserved: masking enabled and the userName present in the message ->
     * the username is masked. Pattern (?<=.).(?=.) masks all but the first and last char.
     */
    @Test
    public void testUserNamePresentIsMaskedWhenMaskingEnabled() {

        LoggerUtils.isLogMaskingEnable = true;
        String userName = "admin";
        String errorMessage = "Login failed for user admin in tenant carbon.super";
        String sanitized = LoggerUtils.getSanitizedErrorMessage(errorMessage, userName);
        assertFalse(sanitized.contains(userName),
                "Raw username must not remain in the sanitized message.");
        assertEquals(sanitized, "Login failed for user a***n in tenant carbon.super",
                "Username must be masked with the first/last char retained.");
    }

    /**
     * Masking enabled but the userName is not contained in the message -> message returned as-is.
     */
    @Test
    public void testUserNameNotPresentReturnsMessageUnchanged() {

        LoggerUtils.isLogMaskingEnable = true;
        String errorMessage = "Persisted access token data not found.";
        assertEquals(LoggerUtils.getSanitizedErrorMessage(errorMessage, "admin"), errorMessage,
                "When the username is absent the message must be unchanged.");
    }
}
