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
import org.wso2.carbon.identity.rule.management.api.model.ANDCombinedRule;
import org.wso2.carbon.identity.rule.management.api.model.Expression;
import org.wso2.carbon.identity.rule.management.api.model.FieldReference;
import org.wso2.carbon.identity.rule.management.api.model.ORCombinedRule;
import org.wso2.carbon.identity.rule.management.api.model.Rule;
import org.wso2.carbon.identity.rule.management.api.model.Value;
import org.wso2.carbon.identity.rule.metadata.api.model.Operator.ValueInput;

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
    private static final String TRUE = "true";
    private static final String FALSE = "false";

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

        Operator operator = validateAndResolveOperator(expression, fieldValue);
        Value value = expression.getValue();

        if (operator.getValueInput() == ValueInput.NONE) {
            if (value != null) {
                throw new RuleEvaluationException("Operator " + operator.getName() + " takes no value.");
            }
            return operator.apply(fieldValue.getValue(), null);
        }
        validateValue(operator, value, expression.getField());

        // A condition with a missing value on either side never holds.
        if (fieldValue.getValue() == null) {
            return false;
        }

        if (value.getType() == Value.Type.FIELD) {
            return evaluateAgainstField(operator, fieldValue, value.getFieldReference(), evaluationData);
        }

        if (rightOperand(expression) == null) {
            return false;
        }
        return evaluateAgainstValue(operator, fieldValue, expression);
    }

    /**
     * The operator of an expression, which must be known and suit the field.
     *
     * @param expression Expression being evaluated.
     * @param fieldValue Value of the expression's field.
     * @return The operator.
     * @throws RuleEvaluationException If the operator is unknown, or needs a multi-valued field the field is not.
     */
    private Operator validateAndResolveOperator(Expression expression, FieldValue fieldValue)
            throws RuleEvaluationException {

        Operator operator = operatorRegistry.getOperator(expression.getOperator());
        if (operator == null) {
            throw new RuleEvaluationException("Unsupported operator: " + expression.getOperator());
        }
        if (operatorRegistry.isMultiValuedOnly(operator) && !LIST.equals(fieldValue.getValueType())) {
            throw new RuleEvaluationException("Operator " + operator.getName()
                    + " applies to multi-valued fields only, which field " + expression.getField() + " is not.");
        }
        return operator;
    }

    /**
     * Checks that the value of an expression is what its operator takes.
     *
     * @param operator Operator of the expression, which takes a value.
     * @param value    Value of the expression.
     * @param field    Field of the expression.
     * @throws RuleEvaluationException If the value is missing, of an unknown type, or of a shape the operator
     *                                 does not take.
     */
    private static void validateValue(Operator operator, Value value, String field) throws RuleEvaluationException {

        if (value == null) {
            throw new RuleEvaluationException("Operator " + operator.getName() + " needs a value.");
        }
        if (value.getType() == null) {
            throw new RuleEvaluationException("Unsupported value type in the condition on field: " + field);
        }
        boolean listOperator = operator.getValueInput() == ValueInput.LIST;
        if (value.getType() == Value.Type.LIST && !listOperator) {
            throw new RuleEvaluationException("Operator " + operator.getName() + " takes no list of values.");
        }
        if (listOperator && value.getType() != Value.Type.LIST && value.getType() != Value.Type.FIELD) {
            throw new RuleEvaluationException("Operator " + operator.getName()
                    + " needs a list of values or another field.");
        }
        if (value.getType() == Value.Type.LIST && value.getFieldValues() != null
                && value.getFieldValues().contains(null)) {
            throw new RuleEvaluationException("A list value in the condition has a missing entry.");
        }
    }

    /**
     * Compares a field with the value given in the expression, read as the field's value type.
     *
     * @param operator   Operator of the expression.
     * @param fieldValue Value of the expression's field.
     * @param expression Expression being evaluated.
     * @return Whether the comparison holds.
     * @throws RuleEvaluationException If the value given is not of the field's value type.
     */
    private boolean evaluateAgainstValue(Operator operator, FieldValue fieldValue, Expression expression)
            throws RuleEvaluationException {

        Value value = expression.getValue();
        if (fieldValue.getValueType().equals(STRING)) {
            return operator.apply(fieldValue.getValue(), rightOperand(expression));
        } else if (fieldValue.getValueType().equals(BOOLEAN)) {
            return operator.apply(fieldValue.getValue(), parseBoolean(value.getFieldValue()));
        } else if (fieldValue.getValueType().equals(NUMBER)) {
            return operator.apply(fieldValue.getValue(), parseNumber(value.getFieldValue()));
        } else if (fieldValue.getValueType().equals(REFERENCE)) {
            return operator.apply(fieldValue.getValue(), rightOperand(expression));
        } else if (fieldValue.getValueType().equals(LIST)) {
            return applyOperatorForList(operator, fieldValue.getValue(), rightOperand(expression));
        }

        throw new RuleEvaluationException("Unsupported value type: " + fieldValue.getValueType());
    }

    /**
     * Compares a field with another field. A side without a value never holds; a referenced field that was not
     * resolved at all is an error.
     * <p>
     * {@code in} and {@code notIn} compare the two sides as sets, a single value counting as a set of one. Any
     * other operator holds only between two single values.
     *
     * @param operator       Operator of the expression.
     * @param left           Value of the expression's field.
     * @param reference      The field compared against.
     * @param evaluationData Values resolved for the rule.
     * @return Whether the comparison holds.
     * @throws RuleEvaluationException If the reference names no field, or the referenced field was not resolved.
     */
    private boolean evaluateAgainstField(Operator operator, FieldValue left, FieldReference reference,
                                         Map<String, FieldValue> evaluationData) throws RuleEvaluationException {

        if (reference == null || reference.getName() == null) {
            throw new RuleEvaluationException("A condition reads its value from a field it does not name.");
        }
        FieldValue right = evaluationData.get(FieldLookup.token(reference.getName(), reference.getQualifier()));
        if (right == null) {
            throw new RuleEvaluationException("Field value not found for the field: " + reference.getName());
        }
        if (right.getValue() == null) {
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

        return operator.getValueInput() == ValueInput.LIST;
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

    private static Double parseNumber(String value) throws RuleEvaluationException {

        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException e) {
            throw new RuleEvaluationException("Value " + value + " in the condition is not a valid NUMBER.");
        }
    }

    private static Boolean parseBoolean(String value) throws RuleEvaluationException {

        if (TRUE.equalsIgnoreCase(value)) {
            return Boolean.TRUE;
        }
        if (FALSE.equalsIgnoreCase(value)) {
            return Boolean.FALSE;
        }
        throw new RuleEvaluationException("Value " + value + " in the condition is not a valid BOOLEAN.");
    }

    private boolean applyOperatorForList(Operator operator, Object fieldValue, Object expressionValue)
            throws RuleEvaluationException {

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

        throw new RuleEvaluationException("Operator " + operator.getName() + " is not supported on a multi-valued "
                + "field.");
    }
}
