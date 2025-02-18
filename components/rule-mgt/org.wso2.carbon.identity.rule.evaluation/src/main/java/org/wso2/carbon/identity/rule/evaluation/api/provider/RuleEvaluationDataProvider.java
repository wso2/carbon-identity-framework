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

package org.wso2.carbon.identity.rule.evaluation.api.provider;

import org.wso2.carbon.identity.rule.evaluation.api.exception.RuleEvaluationDataProviderException;
import org.wso2.carbon.identity.rule.evaluation.api.model.FieldValue;
import org.wso2.carbon.identity.rule.evaluation.api.model.FlowContext;
import org.wso2.carbon.identity.rule.evaluation.api.model.FlowType;
import org.wso2.carbon.identity.rule.evaluation.api.model.RuleEvaluationContext;

import java.util.List;

/**
 * Rule evaluation data provider interface.
 * This interface is used to provide data required for rule evaluation for fields in the rule.
 */
public interface RuleEvaluationDataProvider {

    /**
     * Get the supported flow type.
     * This method should return the flow type that this data provider supports.
     *
     * @return Supported flow type.
     */
    FlowType getSupportedFlowType();

    /**
     * Get evaluation data for a given rule evaluation context and flow context.
     *
     * @param ruleEvaluationContext Rule evaluation context.
     * @param flowContext           Flow context.
     * @param tenantDomain          Tenant domain.
     * @return List of field values.
     * @throws RuleEvaluationDataProviderException If an error occurred while getting the evaluation data.
     */
    List<FieldValue> getEvaluationData(RuleEvaluationContext ruleEvaluationContext, FlowContext flowContext,
                                       String tenantDomain) throws RuleEvaluationDataProviderException;

}
