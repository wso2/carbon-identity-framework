/*
 * Copyright (c) 2025, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.carbon.identity.rule.evaluation.api.model;

import java.util.Map;

/**
 * Flow context model.
 * This class represents the context of a flow used at rule evaluation.
 */
public class FlowContext {

    private final FlowType flowType;
    private final Map<String, Object> contextData;

    public FlowContext(FlowType flowType, Map<String, Object> contextData) {

        this.flowType = flowType;
        this.contextData = contextData;
    }

    public FlowType getFlowType() {

        return flowType;
    }

    public Map<String, Object> getContextData() {

        return contextData;
    }
}
