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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.rule.evaluation.api.exception.RuleEvaluationException;
import org.wso2.carbon.identity.rule.evaluation.api.model.FieldValue;
import org.wso2.carbon.identity.rule.evaluation.api.model.Operator;
import org.wso2.carbon.identity.rule.evaluation.api.model.RuleEvaluationResult;
import org.wso2.carbon.identity.rule.evaluation.api.model.ValueType;
import org.wso2.carbon.identity.rule.management.api.model.ANDCombinedRule;
import org.wso2.carbon.identity.rule.management.api.model.Expression;
import org.wso2.carbon.identity.rule.management.api.model.FieldReference;
import org.wso2.carbon.identity.rule.management.api.model.ORCombinedRule;
import org.wso2.carbon.identity.rule.management.api.model.Rule;
import org.wso2.carbon.identity.rule.management.api.model.Value;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.wso2.carbon.identity.rule.evaluation.api.model.ValueType.BOOLEAN;
import static org.wso2.carbon.identity.rule.evaluation.api.model.ValueType.LIST;
import static org.wso2.carbon.identity.rule.evaluation.api.model.ValueType.NUMBER;
import static org.wso2.carbon.identity.rule.evaluation.api.model.ValueType.REFERENCE;
import static org.wso2.carbon.identity.rule.evaluation.api.model.ValueType.STRING;

/**
 * Rule evaluator.
 * This class is responsible for evaluating rules.
 */
public class RuleEvaluator {

    private static final Log LOG = LogFactory.getLog(RuleEvaluator.class);

    private final OperatorRegistry operatorRegistry;

    // Operators
    private static final String EQUALS = "equals";
    private static final String NOT_EQUALS = "notEquals";
    private static final String CONTAINS = "contains";
    private static final String IN = "in";
    private static final String NOT_IN = "notIn";

    public RuleEvaluator(OperatorRegistry operatorRegistry) {

        this.operatorRegistry = operatorRegistry;
    }

    /**
     * Evaluate a given rule and return the result, including the fields that failed evaluation.
     *
     * @param rule           Rule to evaluate.
     * @param evaluationData Evaluation data.
     * @return Rule evaluation result with the satisfied status and the failed fields.
     * @throws RuleEvaluationException If an error occurs while evaluating the rule.
     */
    public RuleEvaluationResult evaluate(Rule rule, Map<String, FieldValue> evaluationData)
            throws RuleEvaluationException {

        List<String> failedFields = new ArrayList<>();
        ORCombinedRule orRule = (ORCombinedRule) rule;
        boolean ruleSatisfied = evaluateORCombinedRule(orRule, evaluationData, failedFields);
        return new RuleEvaluationResult(rule.getId(), ruleSatisfied, failedFields);
    }

    private boolean evaluateORCombinedRule(ORCombinedRule orRule, Map<String, FieldValue> evaluationData,
                                           List<String> failedFields)
            throws RuleEvaluationException {

        for (ANDCombinedRule andRule : orRule.getRules()) {
            List<String> branchFailedFields = new ArrayList<>();
            if (evaluateANDCombinedRule(andRule, evaluationData, branchFailedFields)) {
                failedFields.clear();
                return true; // If any ANDCombinedRule evaluates to true, the ORCombinedRule passes
            }
            failedFields.addAll(branchFailedFields);
        }
        return false; // If none of the ANDCombinedRules pass, the ORCombinedRule fails
    }

    private boolean evaluateANDCombinedRule(ANDCombinedRule andRule, Map<String, FieldValue> evaluationData,
                                            List<String> branchFailedFields)
            throws RuleEvaluationException {

        for (Expression expression : andRule.getExpressions()) {
            if (!evaluateExpression(expression, evaluationData)) {
                branchFailedFields.add(expression.getField());
                return false; // If any expression fails, the ANDCombinedRule fails
            }
        }
        return true; // All expressions passed, the ANDCombinedRule passes
    }

    private boolean evaluateExpression(Expression expression, Map<String, FieldValue> evaluationData)
            throws RuleEvaluationException {

        FieldValue fieldValue = evaluationData.get(
                FieldLookup.token(expression.getField(), expression.getFieldQualifier()));
        if (fieldValue == null) {
            throw new RuleEvaluationException("Field value not found for the field: " + expression.getField());
        }

        Operator operator = operatorRegistry.getOperator(expression.getOperator());
        Value value = expression.getValue();

        // A condition with a missing value on either side, or a value type unknown to this node, never holds.
        if (value.getType() == null || fieldValue.getValue() == null) {
            return false;
        }

        if (value.getType() == Value.Type.FIELD) {
            return evaluateAgainstField(operator, fieldValue, value.getFieldReference(), evaluationData);
        }

        if (rightOperand(expression) == null) {
            return false;
        }

        if (value.getType() == Value.Type.LIST && !fieldValue.getValueType().equals(LIST)) {
            return operator.apply(fieldValue.getValue(), typedEntries(fieldValue.getValueType(),
                    value.getFieldValues()));
        }

        // Evaluate based on the value type of the field
        if (fieldValue.getValueType().equals(STRING)) {
            return operator.apply(fieldValue.getValue(), rightOperand(expression));
        } else if (fieldValue.getValueType().equals(BOOLEAN)) {
            return operator.apply(fieldValue.getValue(),
                    Boolean.parseBoolean(expression.getValue().getFieldValue()));
        } else if (fieldValue.getValueType().equals(NUMBER)) {
            return operator.apply(fieldValue.getValue(), Double.parseDouble(expression.getValue().getFieldValue()));
        } else if (fieldValue.getValueType().equals(REFERENCE)) {
            return operator.apply(fieldValue.getValue(), rightOperand(expression));
        } else if (fieldValue.getValueType().equals(LIST)) {
            return applyOperatorForList(operator, fieldValue.getValue(), rightOperand(expression));
        }

        throw new IllegalStateException("Unsupported value type: " + fieldValue.getValueType());
    }

    /**
     * Compares a field with another field. A side without a value never holds.
     * <p>
     * {@code in} and {@code notIn} compare the two sides as sets, a single value counting as a set of one. Any
     * other operator holds only between two single values.
     *
     * @param operator       Operator of the expression.
     * @param left           Value of the expression's field.
     * @param reference      The field compared against.
     * @param evaluationData Values resolved for the rule.
     * @return Whether the comparison holds.
     */
    private boolean evaluateAgainstField(Operator operator, FieldValue left, FieldReference reference,
                                         Map<String, FieldValue> evaluationData) {

        if (reference == null || left.getValue() == null) {
            return false;
        }
        FieldValue right = evaluationData.get(FieldLookup.token(reference.getName(), reference.getQualifier()));
        if (right == null || right.getValue() == null) {
            return false;
        }
        if (isMembership(operator)) {
            return operator.getName().equals(IN) != Collections.disjoint(asCollection(left.getValue()),
                    asCollection(right.getValue()));
        }
        if (left.getValue() instanceof Collection || right.getValue() instanceof Collection) {
            return false;
        }
        return operator.apply(left.getValue(), right.getValue());
    }

    private static boolean isMembership(Operator operator) {

        return IN.equals(operator.getName()) || NOT_IN.equals(operator.getName());
    }

    private static Collection<?> asCollection(Object value) {

        return value instanceof Collection ? (Collection<?>) value : Collections.singletonList(value);
    }

    /**
     * The right-hand side of an expression: the values of a LIST, otherwise the single value.
     *
     * @param expression Expression being evaluated.
     * @return The right-hand side.
     */
    private static Object rightOperand(Expression expression) {

        Value value = expression.getValue();
        return value.getType() == Value.Type.LIST ? value.getFieldValues() : value.getFieldValue();
    }

    /**
     * The entries of a LIST value, converted to the value type of the field they are compared with.
     *
     * @param valueType Value type of the field.
     * @param entries   Entries of the LIST value.
     * @return The converted entries.
     */
    private static List<Object> typedEntries(ValueType valueType, List<String> entries) {

        List<Object> typed = new ArrayList<>(entries.size());
        for (String entry : entries) {
            if (valueType.equals(NUMBER)) {
                typed.add(Double.parseDouble(entry));
            } else if (valueType.equals(BOOLEAN)) {
                typed.add(Boolean.parseBoolean(entry));
            } else {
                typed.add(entry);
            }
        }
        return typed;
    }

    private boolean applyOperatorForList(Operator operator, Object fieldValue, Object expressionValue) {

        List<?> list = (List<?>) fieldValue;
        if (list == null) {
            return false;
        }

        if (isMembership(operator)) {
            // Membership between two sets holds when they share a value; a right side that is not a set never holds.
            if (!(expressionValue instanceof Collection)) {
                return false;
            }
            return operator.getName().equals(IN) != Collections.disjoint(list, (Collection<?>) expressionValue);
        }

        if (operator.getName().equals(EQUALS)) {
            return list.contains(expressionValue);
        } else if (operator.getName().equals(NOT_EQUALS)) {
            return !list.contains(expressionValue);
        } else if (operator.getName().equals(CONTAINS)) {
            return list.contains(expressionValue);
        }

        throw new IllegalStateException("Unsupported operator: " + operator.getName() + " for LIST value type");
    }
}
