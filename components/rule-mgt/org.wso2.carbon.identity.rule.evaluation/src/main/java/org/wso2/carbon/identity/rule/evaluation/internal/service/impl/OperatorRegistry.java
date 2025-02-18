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

import org.wso2.carbon.identity.rule.evaluation.api.model.Operator;
import org.wso2.carbon.identity.rule.evaluation.internal.component.RuleEvaluationComponentServiceHolder;
import org.wso2.carbon.identity.rule.metadata.api.service.RuleMetadataService;

import java.util.HashMap;
import java.util.Map;

/**
 * Registry for operators.
 * This class is responsible for loading operators from the metadata service,
 * instantiating its functionality for evaluation.
 */
public class OperatorRegistry {

    private static final Map<String, Operator> operators = new HashMap<>();

    // Operators
    private static final String EQUALS = "equals";
    private static final String NOT_EQUALS = "notEquals";
    private static final String CONTAINS = "contains";

    private OperatorRegistry() {

    }

    /**
     * Get operator implementation by name
     *
     * @param name Operator name
     * @return Operator instance
     */
    public Operator getOperator(String name) {

        return operators.get(name);
    }

    /**
     * Instantiate OperatorRegistry loading operators from metadata service.
     *
     * @return OperatorRegistry instance.
     */
    public static OperatorRegistry loadOperators() {

        RuleMetadataService ruleMetadataService = RuleEvaluationComponentServiceHolder.getInstance()
                .getRuleMetadataService();
        if (ruleMetadataService == null) {
            return new OperatorRegistry();
        }
        ruleMetadataService.getApplicableOperatorsInExpressions()
                .forEach(operator -> {
                    switch (operator.getName()) {
                        case EQUALS:
                            operators.put(operator.getName(), new Operator(EQUALS, (a, b) -> a.equals(b)));
                            break;
                        case NOT_EQUALS:
                            operators.put(operator.getName(), new Operator(NOT_EQUALS, (a, b) -> !a.equals(b)));
                            break;
                        case CONTAINS:
                            operators.put(operator.getName(), new Operator(CONTAINS, (a, b) -> {
                                if (a instanceof String && b instanceof String) {
                                    return ((String) a).contains((String) b);
                                }
                                return false;
                            }));
                            break;
                        default:
                            throw new IllegalArgumentException("Unsupported operator: " + operator.getName());
                    }
                });

        return new OperatorRegistry();
    }
}
