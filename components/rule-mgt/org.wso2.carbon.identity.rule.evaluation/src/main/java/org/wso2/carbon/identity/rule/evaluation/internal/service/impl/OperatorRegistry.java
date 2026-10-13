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
import org.wso2.carbon.identity.rule.metadata.api.model.Operator.ValueInput;
import org.wso2.carbon.identity.rule.metadata.api.service.RuleMetadataService;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.BiPredicate;

/**
 * Registry for operators.
 * This class is responsible for loading operators from the metadata service,
 * instantiating its functionality for evaluation.
 */
public class OperatorRegistry {

    private static final String IS_BLANK = "isBlank";
    private static final String IS_NOT_BLANK = "isNotBlank";
    private static final String IS_EMPTY = "isEmpty";
    private static final String IS_NOT_EMPTY = "isNotEmpty";
    private static final Set<String> VALUE_LESS_OPERATORS = Collections.unmodifiableSet(new HashSet<>(
            Arrays.asList(IS_BLANK, IS_NOT_BLANK, IS_EMPTY, IS_NOT_EMPTY)));
    private static final Set<String> LIST_OPERATORS = Collections.unmodifiableSet(new HashSet<>(
            Arrays.asList("in", "notIn")));
    private static final Set<String> MULTI_VALUED_OPERATORS = Collections.unmodifiableSet(new HashSet<>(
            Arrays.asList(IS_EMPTY, IS_NOT_EMPTY)));

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
        supportedOperators.put("in", membershipPredicate(true));
        supportedOperators.put("notIn", membershipPredicate(false));
        supportedOperators.put(IS_BLANK, (a, b) -> isBlank(a));
        supportedOperators.put(IS_NOT_BLANK, (a, b) -> !isBlank(a));
        supportedOperators.put(IS_EMPTY, (a, b) -> isEmpty(a));
        supportedOperators.put(IS_NOT_EMPTY, (a, b) -> !isEmpty(a));
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
                    ValueInput valueInput = valueInputOf(name);
                    if (operator.getValueInput() != valueInput) {
                        throw new IllegalArgumentException("Operator " + name + " is configured with value input "
                                + operator.getValueInput().getValue() + ", but its implementation takes "
                                + valueInput.getValue() + ".");
                    }
                    operators.put(name, new Operator(name, predicate, valueInput,
                            MULTI_VALUED_OPERATORS.contains(name)));
                });

        return new OperatorRegistry();
    }

    private static ValueInput valueInputOf(String name) {

        if (VALUE_LESS_OPERATORS.contains(name)) {
            return ValueInput.NONE;
        }
        return LIST_OPERATORS.contains(name) ? ValueInput.LIST : ValueInput.SINGLE;
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

    private static BiPredicate<Object, Object> membershipPredicate(boolean expected) {

        return (a, b) -> a != null && b instanceof Collection && ((Collection<?>) b).contains(a) == expected;
    }

    /**
     * Null, or a collection without entries.
     */
    private static boolean isEmpty(Object value) {

        return value == null || (value instanceof Collection && ((Collection<?>) value).isEmpty());
    }

    /**
     * Null, a string of whitespace alone, or a collection whose entries are all blank.
     */
    private static boolean isBlank(Object value) {

        if (value instanceof Collection) {
            return ((Collection<?>) value).stream().allMatch(OperatorRegistry::isBlank);
        }
        return value == null || (value instanceof String && ((String) value).trim().isEmpty());
    }
}
