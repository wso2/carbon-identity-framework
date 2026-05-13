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
import java.util.function.BiPredicate;

/**
 * Registry for operators.
 * This class is responsible for loading operators from the metadata service,
 * instantiating its functionality for evaluation.
 */
public class OperatorRegistry {

    private static final Map<String, Operator> operators = new HashMap<>();
    private static final Map<String, BiPredicate<Object, Object>> supportedOperators = new HashMap<>();

    static {
        supportedOperators.put("equals", Object::equals);
        supportedOperators.put("notEquals", (a, b) -> !a.equals(b));
        supportedOperators.put("contains", stringPredicate(String::contains));
        supportedOperators.put("notContains", stringPredicate((a, b) -> !a.contains(b)));
        supportedOperators.put("startsWith", stringPredicate(String::startsWith));
        supportedOperators.put("endsWith", stringPredicate(String::endsWith));
        supportedOperators.put("greaterThan", comparablePredicate(result -> result > 0));
        supportedOperators.put("lessThan", comparablePredicate(result -> result < 0));
    }

    private OperatorRegistry() {

    }

    /**
     * Get operator implementation by name.
     *
     * @param name Operator name.
     * @return Operator instance.
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
                    String name = operator.getName();
                    BiPredicate<Object, Object> predicate = supportedOperators.get(name);
                    if (predicate == null) {
                        throw new IllegalArgumentException("Unsupported operator: " + name);
                    }
                    operators.put(name, new Operator(name, predicate));
                });

        return new OperatorRegistry();
    }

    @SuppressWarnings("unchecked")
    private static BiPredicate<Object, Object> comparablePredicate(
            java.util.function.IntPredicate resultCheck) {

        return (a, b) -> a instanceof Comparable && b instanceof Comparable
                && a.getClass().equals(b.getClass())
                && resultCheck.test(((Comparable<Object>) a).compareTo(b));
    }

    private static BiPredicate<Object, Object> stringPredicate(BiPredicate<String, String> predicate) {

        return (a, b) -> a instanceof String && b instanceof String && predicate.test((String) a, (String) b);
    }
}
