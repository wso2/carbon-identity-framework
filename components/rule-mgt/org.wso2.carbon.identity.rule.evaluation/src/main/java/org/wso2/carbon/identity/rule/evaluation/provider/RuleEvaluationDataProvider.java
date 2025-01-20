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

package org.wso2.carbon.identity.rule.evaluation.provider;

import org.wso2.carbon.identity.rule.evaluation.exception.RuleEvaluationDataProviderException;
import org.wso2.carbon.identity.rule.evaluation.model.FieldValue;
import org.wso2.carbon.identity.rule.evaluation.model.FlowContext;
import org.wso2.carbon.identity.rule.evaluation.model.FlowType;
import org.wso2.carbon.identity.rule.evaluation.model.RuleEvaluationContext;

import java.util.Map;

/**
 * Rule evaluation data provider interface.
 * This interface is used to provide data required for rule evaluation for fields in the rule.
 */
public interface RuleEvaluationDataProvider {

    FlowType getSupportedFlowType();

    Map<String, FieldValue> getEvaluationData(RuleEvaluationContext ruleEvaluationContext, FlowContext flowContext,
                                              String tenantDomain) throws RuleEvaluationDataProviderException;

}
