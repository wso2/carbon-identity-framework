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

        if (value != null && value.getType() == null) {
            /*
             * Stored rules are read with unknown value types as null, so a type added by a newer node arrives here
             * without one. What its value means is unknown, so the condition fails closed rather than being read
             * as a plain value.
             */
            return false;
        }

        if (value != null && value.getType() == Value.Type.FIELD) {
            return evaluateAgainstField(operator, fieldValue, value.getFieldReference(), evaluationData);
        }

        if (value != null && value.getType() == Value.Type.LIST && !fieldValue.getValueType().equals(LIST)) {
            if (value.getFieldValues() == null) {
                // A missing set holds for no operator, negated ones included.
                return false;
            }
            // A set of the field's own type: its entries are read the way a single value of that type would be.
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
     * Compares a field with another field read from the same context, rather than with a value given in the rule.
     * <p>
     * A comparison with a side that has no value does not hold, for any operator, negated ones included: an
     * absent value is not known to differ from anything.
     *
     * @param operator       Operator of the expression.
     * @param left           Value of the expression's own field.
     * @param reference      The field the expression is compared against.
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
        if (left.getValueType().equals(LIST)) {
            return applyOperatorForList(operator, left.getValue(), right.getValue());
        }
        return operator.apply(left.getValue(), right.getValue());
    }

    /**
     * The right-hand side of an expression. A LIST carries its values separately from the single
     * value every other type uses, so which one to read is decided by the declared type.
     *
     * @param expression Expression being evaluated.
     * @return The values for a LIST, otherwise the single value.
     */
    private static Object rightOperand(Expression expression) {

        Value value = expression.getValue();
        return value.getType() == Value.Type.LIST ? value.getFieldValues() : value.getFieldValue();
    }

    /**
     * The entries of a LIST value, converted to the type of the field they are compared with, so that a number
     * or a boolean is matched against numbers or booleans rather than against their text.
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
            // A missing set holds for no operator, negated ones included.
            return false;
        }

        if (operator.getName().equals(IN) || operator.getName().equals(NOT_IN)) {
            /*
             * Both sides are sets here, so membership is intersection: in holds when they share a
             * value. A right-hand side that is not a set cannot be intersected, and neither operator
             * holds in that case -- a malformed expression fails closed rather than passing by
             * negation.
             */
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
