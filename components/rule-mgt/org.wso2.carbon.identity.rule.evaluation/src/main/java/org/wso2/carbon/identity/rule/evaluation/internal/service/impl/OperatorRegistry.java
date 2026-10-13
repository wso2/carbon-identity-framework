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

import java.util.Collection;
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
    private static final Map<String, OperatorDefinition> definitions = new HashMap<>();

    static {
        register("equals", Object::equals, ValueInput.SINGLE, false);
        register("notEquals", (a, b) -> !a.equals(b), ValueInput.SINGLE, false);
        register("contains", stringPredicate(String::contains), ValueInput.SINGLE, false);
        register("notContains", stringPredicate((a, b) -> !a.contains(b)), ValueInput.SINGLE, false);
        register("startsWith", stringPredicate(String::startsWith), ValueInput.SINGLE, false);
        register("endsWith", stringPredicate(String::endsWith), ValueInput.SINGLE, false);
        register("greaterThan", comparablePredicate(result -> result > 0), ValueInput.SINGLE, false);
        register("lessThan", comparablePredicate(result -> result < 0), ValueInput.SINGLE, false);
        register("in", membershipPredicate(true), ValueInput.LIST, true);
        register("notIn", membershipPredicate(false), ValueInput.LIST, true);
        register("isBlank", (a, b) -> isBlank(a), ValueInput.NONE, false);
        register("isNotBlank", (a, b) -> !isBlank(a), ValueInput.NONE, false);
        register("isEmpty", (a, b) -> isEmpty(a), ValueInput.NONE, true);
        register("isNotEmpty", (a, b) -> !isEmpty(a), ValueInput.NONE, true);
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
     * Whether an operator applies to multi-valued fields alone, as isEmpty and in do.
     *
     * @param operator Operator.
     * @return True if the operator checks a field's entries, which only a multi-valued field has.
     */
    public boolean isMultiValuedOnly(Operator operator) {

        OperatorDefinition definition = definitions.get(operator.getName());
        return definition != null && definition.multiValuedOnly;
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
                    OperatorDefinition definition = definitions.get(name);
                    if (definition == null) {
                        throw new IllegalArgumentException("Unsupported operator: " + name);
                    }
                    if (operator.getValueInput() != definition.valueInput) {
                        throw new IllegalArgumentException("Operator " + name + " is configured with value input "
                                + operator.getValueInput().getValue() + ", but its implementation takes "
                                + definition.valueInput.getValue() + ".");
                    }
                    operators.put(name, new Operator(name, definition.predicate, definition.valueInput));
                });

        return new OperatorRegistry();
    }

    private static void register(String name, BiPredicate<Object, Object> predicate, ValueInput valueInput,
                                 boolean multiValuedOnly) {

        definitions.put(name, new OperatorDefinition(predicate, valueInput, multiValuedOnly));
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

    /**
     * What an operator does, the value it takes, and whether it applies to multi-valued fields alone.
     */
    private static final class OperatorDefinition {

        private final BiPredicate<Object, Object> predicate;
        private final ValueInput valueInput;
        private final boolean multiValuedOnly;

        private OperatorDefinition(BiPredicate<Object, Object> predicate, ValueInput valueInput,
                                   boolean multiValuedOnly) {

            this.predicate = predicate;
            this.valueInput = valueInput;
            this.multiValuedOnly = multiValuedOnly;
        }
    }
}
