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

        DiagnosticLog diagnosticLog = captor.getValue().build();
        Assert.assertEquals(diagnosticLog.getActionId(), "evaluate-policy-resource");
        Assert.assertEquals(diagnosticLog.getResultMessage(), "Policy resource evaluated.");
        Assert.assertEquals(diagnosticLog.getParams().get("resourceType"), "RULE");
        Assert.assertEquals(diagnosticLog.getParams().get("target"), "ios");
    }

    @Test
    public void testLogEvaluationInitiatedAndCompleted_IncludesResourceType() {

        loggerUtils.when(LoggerUtils::isDiagnosticLogsEnabled).thenReturn(true);

        diagnosticLogger.logEvaluationInitiated("policy-1", "ios", "RULE");

        ArgumentCaptor<DiagnosticLog.DiagnosticLogBuilder> captor =
                ArgumentCaptor.forClass(DiagnosticLog.DiagnosticLogBuilder.class);
        loggerUtils.verify(() -> LoggerUtils.triggerDiagnosticLogEvent(captor.capture()));

        DiagnosticLog log1 = captor.getValue().build();
        Assert.assertEquals(log1.getActionId(), "evaluate-policy");
        Assert.assertEquals(log1.getParams().get("resourceType"), "RULE");

        PolicyEvaluationResult result = new PolicyEvaluationResult(true, Collections.emptyList());
        diagnosticLogger.logEvaluationCompleted("policy-1", "ios", "RULE", result);

        ArgumentCaptor<DiagnosticLog.DiagnosticLogBuilder> captor2 =
                ArgumentCaptor.forClass(DiagnosticLog.DiagnosticLogBuilder.class);
        loggerUtils.verify(() -> LoggerUtils.triggerDiagnosticLogEvent(captor2.capture()));

        DiagnosticLog log2 = captor2.getValue().build();
        Assert.assertEquals(log2.getActionId(), "evaluate-policy");
        Assert.assertEquals(log2.getParams().get("resourceType"), "RULE");
    }

    @Test
    public void testLogsDisabled_NoLogEventsTriggered() {

        loggerUtils.when(LoggerUtils::isDiagnosticLogsEnabled).thenReturn(false);

        diagnosticLogger.logEvaluationInitiated("policy-1", "ios");
        loggerUtils.verify(() -> LoggerUtils.triggerDiagnosticLogEvent(any()), never());
    }
}
