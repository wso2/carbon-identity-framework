/*
 * Copyright (c) 2026, WSO2 LLC. (https://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.identity.central.log.mgt.utils;

import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.Test;

/**
 * Unit tests for {@link LoggerUtils}.
 */
public class LoggerUtilsTest {

    @AfterMethod
    public void tearDown() {

        LoggerUtils.isLogMaskingEnable = false;
    }

    @Test
    public void testGetSanitizedErrorMessageWithNullErrorMessage() {

        LoggerUtils.isLogMaskingEnable = true;
        Assert.assertNull(LoggerUtils.getSanitizedErrorMessage(null, "testUser"));
    }

    @Test
    public void testGetSanitizedErrorMessageMasksUserName() {

        LoggerUtils.isLogMaskingEnable = true;
        String sanitized = LoggerUtils.getSanitizedErrorMessage("Error for user testUser", "testUser");
        Assert.assertFalse(sanitized.contains("testUser"));
    }

    @Test
    public void testGetSanitizedErrorMessageWithMaskingDisabled() {

        LoggerUtils.isLogMaskingEnable = false;
        String errorMessage = "Error for user testUser";
        Assert.assertEquals(LoggerUtils.getSanitizedErrorMessage(errorMessage, "testUser"), errorMessage);
    }
}
