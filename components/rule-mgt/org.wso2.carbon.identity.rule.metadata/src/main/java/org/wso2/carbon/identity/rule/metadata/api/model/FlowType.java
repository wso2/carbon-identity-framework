/*
 * Copyright (c) 2024, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.carbon.identity.rule.metadata.api.model;

import org.wso2.carbon.identity.rule.metadata.api.exception.RuleMetadataException;
import org.wso2.carbon.identity.rule.metadata.internal.util.RuleMetadataExceptionBuilder;

/**
 * Enum class to represent the flow types.
 */
public enum FlowType {
    PRE_ISSUE_ACCESS_TOKEN("preIssueAccessToken"),
    PRE_UPDATE_PASSWORD("preUpdatePassword"),
    PRE_UPDATE_PROFILE("preUpdateProfile"),
    PRE_ISSUE_ID_TOKEN("preIssueIdToken");

    private final String flowAlias;

    FlowType(String flowAlias) {

        this.flowAlias = flowAlias;
    }

    public String getFlowAlias() {

        return flowAlias;
    }

    public static FlowType valueOfFlowAlias(String flowAlias) throws RuleMetadataException {

        for (FlowType flowType : FlowType.values()) {
            if (flowType.flowAlias.equals(flowAlias)) {
                return flowType;
            }
        }

        throw RuleMetadataExceptionBuilder.buildClientException(
                RuleMetadataExceptionBuilder.RuleMetadataError.ERROR_INVALID_FLOW_TYPE, flowAlias);
    }
}
