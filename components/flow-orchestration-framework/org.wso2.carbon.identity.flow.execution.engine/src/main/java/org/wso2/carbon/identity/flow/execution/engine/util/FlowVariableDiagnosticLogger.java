/*
 * Copyright (c) 2026, WSO2 LLC. (https://www.wso2.com) All Rights Reserved.
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.identity.flow.execution.engine.util;

import org.wso2.carbon.identity.central.log.mgt.utils.LoggerUtils;
import org.wso2.carbon.utils.DiagnosticLog;

/**
 * Diagnostic logs for the headers and runtime values of a flow: a value the flow received and did not
 * keep, and why. Only names are logged, never the values.
 */
public class FlowVariableDiagnosticLogger {

    private static final String COMPONENT_ID = "flow-execution";
    private static final String FLOW_VARIABLES = "flow-variables";

    private static final String FLOW_TYPE = "flowType";
    private static final String VARIABLE_TYPE = "variableType";
    private static final String NAME = "name";

    /**
     * Logs that a value was ignored, so the flow continued with the value it had.
     *
     * @param flowType     Flow type.
     * @param variableType Type of the variable: a header or a runtime value.
     * @param name         Name of the value.
     * @param reason       Why the value was ignored.
     */
    public void logValueIgnored(String flowType, String variableType, String name, String reason) {

        if (!LoggerUtils.isDiagnosticLogsEnabled()) {
            return;
        }
        LoggerUtils.triggerDiagnosticLogEvent(new DiagnosticLog.DiagnosticLogBuilder(COMPONENT_ID, FLOW_VARIABLES)
                .resultMessage("Ignored a value received for the flow. Reason: " + reason)
                .resultStatus(DiagnosticLog.ResultStatus.FAILED)
                .logDetailLevel(DiagnosticLog.LogDetailLevel.APPLICATION)
                .inputParam(FLOW_TYPE, flowType)
                .inputParam(VARIABLE_TYPE, variableType)
                .inputParam(NAME, name));
    }
}
