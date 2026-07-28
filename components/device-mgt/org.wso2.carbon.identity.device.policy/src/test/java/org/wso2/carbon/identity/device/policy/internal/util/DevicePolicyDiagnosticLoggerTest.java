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

package org.wso2.carbon.identity.device.policy.internal.util;

import org.mockito.MockedStatic;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.central.log.mgt.utils.LoggerUtils;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mockStatic;

public class DevicePolicyDiagnosticLoggerTest {

    private DevicePolicyDiagnosticLogger logger;

    @BeforeMethod
    public void setUp() {
        logger = new DevicePolicyDiagnosticLogger();
    }

    @Test
    public void testLogTokenValidationFailure() {
        try (MockedStatic<LoggerUtils> loggerUtils = mockStatic(LoggerUtils.class)) {
            loggerUtils.when(LoggerUtils::isDiagnosticLogsEnabled).thenReturn(true);
            loggerUtils.when(() -> LoggerUtils.triggerDiagnosticLogEvent(any())).thenAnswer(invocation -> null);

            logger.logTokenValidationFailure("testDeviceId", "expired");
            logger.logTokenValidationFailure(null, "expired");

            loggerUtils.verify(() -> LoggerUtils.triggerDiagnosticLogEvent(any()), org.mockito.Mockito.times(2));
        }
    }

    @Test
    public void testLogTokenValidationFailureDisabled() {
        try (MockedStatic<LoggerUtils> loggerUtils = mockStatic(LoggerUtils.class)) {
            loggerUtils.when(LoggerUtils::isDiagnosticLogsEnabled).thenReturn(false);

            logger.logTokenValidationFailure("testDeviceId", "expired");

            loggerUtils.verify(() -> LoggerUtils.triggerDiagnosticLogEvent(any()), org.mockito.Mockito.never());
        }
    }

    @Test
    public void testLogTokenValidationSuccess() {
        try (MockedStatic<LoggerUtils> loggerUtils = mockStatic(LoggerUtils.class)) {
            loggerUtils.when(LoggerUtils::isDiagnosticLogsEnabled).thenReturn(true);
            loggerUtils.when(() -> LoggerUtils.triggerDiagnosticLogEvent(any())).thenAnswer(invocation -> null);

            logger.logTokenValidationSuccess("testDeviceId");
            logger.logTokenValidationSuccess(null);

            loggerUtils.verify(() -> LoggerUtils.triggerDiagnosticLogEvent(any()), org.mockito.Mockito.times(2));
        }
    }

    @Test
    public void testLogIntegrityEnrichmentFailure() {
        try (MockedStatic<LoggerUtils> loggerUtils = mockStatic(LoggerUtils.class)) {
            loggerUtils.when(LoggerUtils::isDiagnosticLogsEnabled).thenReturn(true);
            loggerUtils.when(() -> LoggerUtils.triggerDiagnosticLogEvent(any())).thenAnswer(invocation -> null);

            logger.logIntegrityEnrichmentFailure("testAppId", "reason");
            logger.logIntegrityEnrichmentFailure(null, "reason");

            loggerUtils.verify(() -> LoggerUtils.triggerDiagnosticLogEvent(any()), org.mockito.Mockito.times(2));
        }
    }

    @Test
    public void testLogMissingDeviceData() {
        try (MockedStatic<LoggerUtils> loggerUtils = mockStatic(LoggerUtils.class)) {
            loggerUtils.when(LoggerUtils::isDiagnosticLogsEnabled).thenReturn(true);
            loggerUtils.when(() -> LoggerUtils.triggerDiagnosticLogEvent(any())).thenAnswer(invocation -> null);

            logger.logMissingDeviceData("testPolicy");
            logger.logMissingDeviceData(null);

            loggerUtils.verify(() -> LoggerUtils.triggerDiagnosticLogEvent(any()), org.mockito.Mockito.times(2));
        }
    }
}
