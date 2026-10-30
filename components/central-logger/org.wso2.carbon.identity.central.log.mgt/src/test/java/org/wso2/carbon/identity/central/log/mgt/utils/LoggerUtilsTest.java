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

import org.apache.commons.logging.Log;
import org.mockito.MockedStatic;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.core.util.IdentityUtil;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.wso2.carbon.identity.central.log.mgt.utils.LogConstants.TENANT_RESOURCE_FAILURE_LOGS_DIAGNOSTIC_ONLY;

/**
 * Unit tests for tenant resource failure logging in {@link LoggerUtils}.
 */
public class LoggerUtilsTest {

    private MockedStatic<IdentityUtil> identityUtil;

    @BeforeMethod
    public void setUp() {

        identityUtil = mockStatic(IdentityUtil.class);
    }

    @AfterMethod
    public void tearDown() {

        identityUtil.close();
    }

    @DataProvider
    public Object[][] diagnosticOnlyConfigs() {

        return new Object[][]{
                {"true", true},
                {"false", false},
                {null, false}
        };
    }

    @Test(dataProvider = "diagnosticOnlyConfigs")
    public void testIsTenantResourceFailureDiagnosticOnly(String configValue, boolean expected) {

        identityUtil.when(() -> IdentityUtil.getProperty(TENANT_RESOURCE_FAILURE_LOGS_DIAGNOSTIC_ONLY))
                .thenReturn(configValue);

        assertEquals(LoggerUtils.isTenantResourceFailureDiagnosticOnly(), expected);
    }

    @Test
    public void testLogTenantResourceFailureAsErrorByDefault() {

        identityUtil.when(() -> IdentityUtil.getProperty(TENANT_RESOURCE_FAILURE_LOGS_DIAGNOSTIC_ONLY))
                .thenReturn(null);
        Log log = mock(Log.class);
        Exception e = new Exception("endpoint failed");

        LoggerUtils.logTenantResourceFailure(log, "message", e);

        verify(log).error("message", e);
        verify(log, never()).debug(any(), any());
    }

    @Test
    public void testLogTenantResourceFailureAsDebugWhenDiagnosticOnly() {

        identityUtil.when(() -> IdentityUtil.getProperty(TENANT_RESOURCE_FAILURE_LOGS_DIAGNOSTIC_ONLY))
                .thenReturn("true");
        Log log = mock(Log.class);
        when(log.isDebugEnabled()).thenReturn(true);
        Exception e = new Exception("endpoint failed");

        LoggerUtils.logTenantResourceFailure(log, "message", e);

        verify(log).debug("message", e);
        verify(log, never()).error(anyString(), any());
    }
}
