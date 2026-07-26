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

package org.wso2.carbon.identity.policy.evaluation.util;

import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.central.log.mgt.utils.LoggerUtils;
import org.wso2.carbon.identity.policy.evaluation.api.model.PolicyEvaluationResult;
import org.wso2.carbon.identity.policy.evaluation.api.model.RuleResourceEvaluationResult;
import org.wso2.carbon.identity.policy.evaluation.internal.util.PolicyEvaluationDiagnosticLogger;
import org.wso2.carbon.identity.policy.management.api.exception.PolicyManagementClientException;
import org.wso2.carbon.identity.policy.management.api.model.PolicyResource;
import org.wso2.carbon.identity.policy.management.api.model.RulePolicyResource;
import org.wso2.carbon.utils.DiagnosticLog;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;

/**
 * Unit test class for PolicyEvaluationDiagnosticLogger.
 */
public class PolicyEvaluationDiagnosticLoggerTest {

    private PolicyEvaluationDiagnosticLogger diagnosticLogger;
    private MockedStatic<LoggerUtils> loggerUtils;

    @BeforeMethod
    public void setUp() {

        diagnosticLogger = new PolicyEvaluationDiagnosticLogger();
        loggerUtils = mockStatic(LoggerUtils.class);
    }

    @AfterMethod
    public void tearDown() {

        loggerUtils.close();
    }

    @Test
    public void testLogResourceEvaluationResult_UsesCorrectActionId() throws PolicyManagementClientException {

        loggerUtils.when(LoggerUtils::isDiagnosticLogsEnabled).thenReturn(true);

        PolicyResource resource = new RulePolicyResource.Builder()
                .target("ios")
                .resourceId("rule-1")
                .build();
        RuleResourceEvaluationResult result = RuleResourceEvaluationResult.satisfied(resource);

        diagnosticLogger.logResourceEvaluationResult(result);

        ArgumentCaptor<DiagnosticLog.DiagnosticLogBuilder> captor =
                ArgumentCaptor.forClass(DiagnosticLog.DiagnosticLogBuilder.class);
        loggerUtils.verify(() -> LoggerUtils.triggerDiagnosticLogEvent(captor.capture()));

        Assert.assertNotNull(captor.getValue());
    }

    @Test
    public void testLogEvaluationInitiatedAndCompleted_IncludesResourceType() {

        loggerUtils.when(LoggerUtils::isDiagnosticLogsEnabled).thenReturn(true);

        diagnosticLogger.logEvaluationInitiated("policy-1", "ios", "RULE");

        PolicyEvaluationResult result = new PolicyEvaluationResult(true, Collections.emptyList());
        diagnosticLogger.logEvaluationCompleted("policy-1", "ios", "RULE", result);

        loggerUtils.verify(() -> LoggerUtils.triggerDiagnosticLogEvent(
                any(DiagnosticLog.DiagnosticLogBuilder.class)), times(2));
    }

    @Test
    public void testLogPolicyNotFound_TriggersDiagnosticLog() {

        loggerUtils.when(LoggerUtils::isDiagnosticLogsEnabled).thenReturn(true);

        diagnosticLogger.logPolicyNotFound("policy-1");

        ArgumentCaptor<DiagnosticLog.DiagnosticLogBuilder> captor =
                ArgumentCaptor.forClass(DiagnosticLog.DiagnosticLogBuilder.class);
        loggerUtils.verify(() -> LoggerUtils.triggerDiagnosticLogEvent(captor.capture()));
        Assert.assertNotNull(captor.getValue());
    }

    @Test
    public void testLogNoTargetSpecified_TriggersDiagnosticLog() {

        loggerUtils.when(LoggerUtils::isDiagnosticLogsEnabled).thenReturn(true);

        diagnosticLogger.logNoTargetSpecified("policy-1");

        ArgumentCaptor<DiagnosticLog.DiagnosticLogBuilder> captor =
                ArgumentCaptor.forClass(DiagnosticLog.DiagnosticLogBuilder.class);
        loggerUtils.verify(() -> LoggerUtils.triggerDiagnosticLogEvent(captor.capture()));
        Assert.assertNotNull(captor.getValue());
    }

    @Test
    public void testLogNoMatchingResources_TriggersDiagnosticLog() {

        loggerUtils.when(LoggerUtils::isDiagnosticLogsEnabled).thenReturn(true);

        diagnosticLogger.logNoMatchingResources("policy-1", "ios");

        ArgumentCaptor<DiagnosticLog.DiagnosticLogBuilder> captor =
                ArgumentCaptor.forClass(DiagnosticLog.DiagnosticLogBuilder.class);
        loggerUtils.verify(() -> LoggerUtils.triggerDiagnosticLogEvent(captor.capture()));
        Assert.assertNotNull(captor.getValue());
    }

    @Test
    public void testLogNoEvaluatorForResourceType_TriggersDiagnosticLog() throws PolicyManagementClientException {

        loggerUtils.when(LoggerUtils::isDiagnosticLogsEnabled).thenReturn(true);

        PolicyResource resource = new RulePolicyResource.Builder()
                .target("ios")
                .resourceId("rule-1")
                .build();
        diagnosticLogger.logNoEvaluatorForResourceType(resource);

        ArgumentCaptor<DiagnosticLog.DiagnosticLogBuilder> captor =
                ArgumentCaptor.forClass(DiagnosticLog.DiagnosticLogBuilder.class);
        loggerUtils.verify(() -> LoggerUtils.triggerDiagnosticLogEvent(captor.capture()));
        Assert.assertNotNull(captor.getValue());
    }

    @Test
    public void testDefaultOverloads_TriggersDiagnosticLog() {

        loggerUtils.when(LoggerUtils::isDiagnosticLogsEnabled).thenReturn(true);

        diagnosticLogger.logEvaluationInitiated("policy-1", "ios");

        PolicyEvaluationResult result = new PolicyEvaluationResult(true, Collections.emptyList());
        diagnosticLogger.logEvaluationCompleted("policy-1", "ios", result);

        loggerUtils.verify(() -> LoggerUtils.triggerDiagnosticLogEvent(
                any(DiagnosticLog.DiagnosticLogBuilder.class)), times(2));
    }

    @Test
    public void testLogsDisabled_NoLogEventsTriggered() {

        loggerUtils.when(LoggerUtils::isDiagnosticLogsEnabled).thenReturn(false);

        diagnosticLogger.logEvaluationInitiated("policy-1", "ios");
        loggerUtils.verify(() -> LoggerUtils.triggerDiagnosticLogEvent(any()), never());
    }
}
