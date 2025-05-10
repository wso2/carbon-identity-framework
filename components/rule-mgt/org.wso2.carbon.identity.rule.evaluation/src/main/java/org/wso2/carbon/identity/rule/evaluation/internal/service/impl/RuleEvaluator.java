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
import org.wso2.carbon.identity.rule.management.api.model.ANDCombinedRule;
import org.wso2.carbon.identity.rule.management.api.model.Expression;
import org.wso2.carbon.identity.rule.management.api.model.ORCombinedRule;
import org.wso2.carbon.identity.rule.management.api.model.Rule;

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

    public RuleEvaluator(OperatorRegistry operatorRegistry) {

        this.operatorRegistry = operatorRegistry;
    }

    /**
     * Evaluate a given rule.
     *
     * @param rule           Rule to evaluate.
     * @param evaluationData Evaluation data.
     * @return Evaluation result.
     * @throws RuleEvaluationException If an error occurs while evaluating the rule.
     */
    public boolean evaluate(Rule rule, Map<String, FieldValue> evaluationData) throws RuleEvaluationException {

        ORCombinedRule orRule = (ORCombinedRule) rule;
        return evaluateORCombinedRule(orRule, evaluationData);
    }

    private boolean evaluateORCombinedRule(ORCombinedRule orRule, Map<String, FieldValue> evaluationData)
            throws RuleEvaluationException {

        for (ANDCombinedRule andRule : orRule.getRules()) {
            if (evaluateANDCombinedRule(andRule, evaluationData)) {
                return true; // If any ANDCombinedRule evaluates to true, the ORCombinedRule passes
            }
        }
        return false; // If none of the ANDCombinedRules pass, the ORCombinedRule fails
    }

    private boolean evaluateANDCombinedRule(ANDCombinedRule andRule, Map<String, FieldValue> evaluationData)
            throws RuleEvaluationException {

        for (Expression expression : andRule.getExpressions()) {
            if (!evaluateExpression(expression, evaluationData)) {
                return false; // If any expression fails, the ANDCombinedRule fails
            }
        }
        return true; // All expressions passed, the ANDCombinedRule passes
    }

    private boolean evaluateExpression(Expression expression, Map<String, FieldValue> evaluationData)
            throws RuleEvaluationException {

        FieldValue fieldValue = evaluationData.get(expression.getField());
        if (fieldValue == null) {
            throw new RuleEvaluationException("Field value not found for the field: " + expression.getField());
        }

        Operator operator = operatorRegistry.getOperator(expression.getOperator());

        // Evaluate based on the value type of the field
        if (fieldValue.getValueType().equals(STRING)) {
            return operator.apply(fieldValue.getValue(), expression.getValue().getFieldValue());
        } else if (fieldValue.getValueType().equals(BOOLEAN)) {
            return operator.apply(fieldValue.getValue(),
                    Boolean.parseBoolean(expression.getValue().getFieldValue()));
        } else if (fieldValue.getValueType().equals(NUMBER)) {
            return operator.apply(fieldValue.getValue(), Double.parseDouble(expression.getValue().getFieldValue()));
        } else if (fieldValue.getValueType().equals(REFERENCE)) {
            return operator.apply(fieldValue.getValue(), expression.getValue().getFieldValue());
        } else if (fieldValue.getValueType().equals(LIST)) {
            return applyOperatorForList(operator, fieldValue.getValue(), expression.getValue().getFieldValue());
        }

        throw new IllegalStateException("Unsupported value type: " + fieldValue.getValueType());
    }

    private boolean applyOperatorForList(Operator operator, Object fieldValue, Object expressionValue) {

        List<?> list = (List<?>) fieldValue;

        if (operator.getName().equals(EQUALS)) {
            return list.contains(expressionValue);
        } else if (operator.getName().equals(NOT_EQUALS)) {
            return !list.contains(expressionValue);
        }

        throw new IllegalStateException("Unsupported operator: " + operator.getName() + " for LIST value type");
    }
}
