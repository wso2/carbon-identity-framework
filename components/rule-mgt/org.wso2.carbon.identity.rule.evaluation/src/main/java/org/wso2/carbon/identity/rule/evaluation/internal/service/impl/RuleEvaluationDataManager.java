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

package org.wso2.carbon.identity.rule.evaluation.internal.service.impl;

import org.wso2.carbon.identity.rule.evaluation.api.exception.RuleEvaluationException;
import org.wso2.carbon.identity.rule.evaluation.api.model.FieldValue;
import org.wso2.carbon.identity.rule.evaluation.api.model.FlowContext;
import org.wso2.carbon.identity.rule.evaluation.api.model.FlowType;
import org.wso2.carbon.identity.rule.evaluation.api.model.RuleEvaluationContext;
import org.wso2.carbon.identity.rule.evaluation.api.provider.RuleEvaluationDataProvider;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Rule evaluation data manager.
 * This class is responsible for managing rule evaluation data providers.
 */
public class RuleEvaluationDataManager {

    private static final RuleEvaluationDataManager INSTANCE = new RuleEvaluationDataManager();
    private final Map<FlowType, RuleEvaluationDataProvider> ruleEvaluationDataProviderMap =
            new EnumMap<>(FlowType.class);

    private RuleEvaluationDataManager() {

    }

    /**
     * Get instance of RuleEvaluationDataManager.
     *
     * @return RuleEvaluationDataManager instance.
     */
    public static RuleEvaluationDataManager getInstance() {

        return INSTANCE;
    }

    /**
     * Get evaluation data for a given rule evaluation context and flow context.
     *
     * @param ruleEvaluationContext Rule evaluation context.
     * @param flowContext           Flow context.
     * @param tenantDomain          Tenant domain.
     * @return List of field values.
     * @throws RuleEvaluationException If an error occurs while getting evaluation data.
     */
    public List<FieldValue> getEvaluationData(RuleEvaluationContext ruleEvaluationContext,
                                              FlowContext flowContext,
                                              String tenantDomain) throws RuleEvaluationException {

        RuleEvaluationDataProvider ruleEvaluationDataProvider =
                ruleEvaluationDataProviderMap.get(flowContext.getFlowType());

        return ruleEvaluationDataProvider.getEvaluationData(ruleEvaluationContext, flowContext, tenantDomain);
    }

    /**
     * Register a rule evaluation data provider.
     *
     * @param ruleEvaluationDataProvider Rule evaluation data provider.
     */
    public void registerRuleEvaluationDataProvider(RuleEvaluationDataProvider ruleEvaluationDataProvider) {

        ruleEvaluationDataProviderMap.put(ruleEvaluationDataProvider.getSupportedFlowType(),
                ruleEvaluationDataProvider);
    }

    /**
     * Unregister a rule evaluation data provider.
     *
     * @param ruleEvaluationDataProvider Rule evaluation data provider.
     */
    public void unregisterRuleEvaluationDataProvider(RuleEvaluationDataProvider ruleEvaluationDataProvider) {

        ruleEvaluationDataProviderMap.remove(ruleEvaluationDataProvider.getSupportedFlowType());
    }
}
