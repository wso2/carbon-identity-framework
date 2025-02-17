/*
 * Copyright (c) 2024, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.carbon.identity.rule.management.api.util;

import org.wso2.carbon.identity.rule.management.api.exception.RuleManagementClientException;
import org.wso2.carbon.identity.rule.management.api.exception.RuleManagementException;
import org.wso2.carbon.identity.rule.management.api.exception.RuleManagementServerException;
import org.wso2.carbon.identity.rule.management.api.model.ANDCombinedRule;
import org.wso2.carbon.identity.rule.management.api.model.Expression;
import org.wso2.carbon.identity.rule.management.api.model.FlowType;
import org.wso2.carbon.identity.rule.management.api.model.ORCombinedRule;
import org.wso2.carbon.identity.rule.management.api.model.Rule;
import org.wso2.carbon.identity.rule.management.api.model.Value;
import org.wso2.carbon.identity.rule.management.internal.component.RuleManagementComponentServiceHolder;
import org.wso2.carbon.identity.rule.metadata.api.exception.RuleMetadataException;
import org.wso2.carbon.identity.rule.metadata.api.model.FieldDefinition;
import org.wso2.carbon.identity.rule.metadata.api.model.OptionsInputValue;

import java.util.List;
import java.util.Map;

/**
 * RuleBuilder class is used to build a rule.
 * The RuleBuilder instance can be created using the create method for a given flow type and tenant domain.
 * This class provides methods to add expressions and conditions to the rule and to build the rule.
 * Validations are done while adding expressions and conditions to the rule.
 */
public class RuleBuilder {

    private static final int MAX_EXPRESSIONS_COMBINED_WITH_AND = 5;
    private static final int MAX_RULES_COMBINED_WITH_OR = 10;

    private final ORCombinedRule.Builder orCombinedRuleBuilder = new ORCombinedRule.Builder();
    private ANDCombinedRule.Builder andCombinedRuleBuilder = new ANDCombinedRule.Builder();
    private final Map<String, FieldDefinition> expressionMetadataFieldsMap;

    private boolean isError = false;
    private String errorMessage;

    private int andRuleCount = 0;
    private int orRuleCount = 0;

    private RuleBuilder(List<FieldDefinition> expressionMetadataFields) {

        this.expressionMetadataFieldsMap = expressionMetadataFields.stream()
                .collect(java.util.stream.Collectors.toMap(fieldDefinition -> fieldDefinition.getField().getName(),
                        fieldDefinition -> fieldDefinition));
    }

    /**
     * Add an expression to the rule.
     *
     * @param expression Expression to be added.
     * @return RuleBuilder
     */
    public RuleBuilder addAndExpression(Expression expression) {

        Expression validatedExpression = validateExpressionAndResolveValue(expression);
        addExpressionForANDCombinedRule(validatedExpression);
        validateMaxAllowedANDCombinedExpressions();
        return this;
    }

    /**
     * Add an OR condition to the rule.
     *
     * @return RuleBuilder
     */
    public RuleBuilder addOrCondition() {

        addORCombinedRule();
        validateMaxAllowedORCombinedRules();
        initANDCombinedRule();
        return this;
    }

    /**
     * Build the rule.
     *
     * @return Rule
     * @throws RuleManagementClientException If an error occurs while building the rule.
     */
    public Rule build() throws RuleManagementClientException {

        if (isError) {
            // The very first validation error will be thrown as an exception.
            throw new RuleManagementClientException(
                    "Rule validation failed: " + errorMessage);
        }

        orCombinedRuleBuilder.addRule(andCombinedRuleBuilder.build());
        return orCombinedRuleBuilder.build();
    }

    /**
     * Create a RuleBuilder instance.
     *
     * @param flowType     Flow type.
     * @param tenantDomain Tenant domain.
     * @return RuleBuilder
     * @throws RuleManagementException If an error occurs while creating the RuleBuilder instance.
     */
    public static RuleBuilder create(FlowType flowType, String tenantDomain) throws RuleManagementException {

        try {
            List<FieldDefinition> fieldDefinitionList =
                    RuleManagementComponentServiceHolder.getInstance().getRuleMetadataService()
                            .getExpressionMeta(
                                    org.wso2.carbon.identity.rule.metadata.api.model.FlowType.valueOf(flowType.name()),
                                    tenantDomain);

            if (fieldDefinitionList == null || fieldDefinitionList.isEmpty()) {
                throw new RuleManagementClientException(
                        "Expression metadata from RuleMetadataService is null or empty.");
            }

            return new RuleBuilder(fieldDefinitionList);
        } catch (RuleMetadataException e) {
            throw new RuleManagementServerException(
                    "Error while retrieving expression metadata from RuleMetadataService.", e);
        }
    }

    private void addExpressionForANDCombinedRule(Expression expression) {

        andCombinedRuleBuilder.addExpression(expression);
        andRuleCount++;
    }

    private void initANDCombinedRule() {

        andCombinedRuleBuilder = new ANDCombinedRule.Builder();
        andRuleCount = 0;
    }

    private void addORCombinedRule() {

        orCombinedRuleBuilder.addRule(andCombinedRuleBuilder.build());
        orRuleCount++;
    }

    private Expression validateExpressionAndResolveValue(Expression expression) {

        FieldDefinition fieldDefinition = expressionMetadataFieldsMap.get(expression.getField());

        if (isError) {
            return expression;
        }

        if (!isValidFieldDefinition(fieldDefinition, expression.getField())) {
            return expression;
        }

        if (!isValidOperator(fieldDefinition, expression.getOperator())) {
            return expression;
        }

        Value resolvedValue = validateAndResolveValue(fieldDefinition, expression.getValue());
        if (isError) {
            return expression;
        }

        return new Expression.Builder()
                .field(expression.getField())
                .operator(expression.getOperator())
                .value(resolvedValue)
                .build();
    }

    private boolean isValidFieldDefinition(FieldDefinition fieldDefinition, String field) {

        if (fieldDefinition == null) {
            setValidationError("Field " + field + " is not supported");
            return false;
        }
        return true;
    }

    private boolean isValidOperator(FieldDefinition fieldDefinition, String operator) {

        if (fieldDefinition.getOperators().stream()
                .noneMatch(op -> op.getName().equals(operator))) {
            setValidationError(
                    "Operator " + operator + " is not supported for field " + fieldDefinition.getField().getName());
            return false;
        }
        return true;
    }

    private void validateMaxAllowedANDCombinedExpressions() {

        if (isError) {
            return;
        }

        if (andRuleCount > MAX_EXPRESSIONS_COMBINED_WITH_AND) {
            setValidationError("Maximum number of expressions combined with AND exceeded. Maximum allowed: " +
                    MAX_EXPRESSIONS_COMBINED_WITH_AND + " Provided: " + andRuleCount);
        }
    }

    private void validateMaxAllowedORCombinedRules() {

        if (isError) {
            return;
        }

        if (orRuleCount > MAX_RULES_COMBINED_WITH_OR) {
            setValidationError("Maximum number of rules combined with OR exceeded. Maximum allowed: " +
                    MAX_RULES_COMBINED_WITH_OR + " Provided: " + orRuleCount);
        }
    }

    private void setValidationError(String message) {

        isError = true;
        errorMessage = message;
    }

    private Value validateAndResolveValue(FieldDefinition fieldDefinition, Value value) {

        String rawValue = value.getFieldValue();

        if (!isValidValueType(fieldDefinition, value)) {
            return value;
        }

        if (!isValidOptionsInputValue(fieldDefinition, rawValue)) {
            return value;
        }

        try {
            return resolveValue(fieldDefinition, rawValue);
        } catch (RuleManagementClientException e) {
            setValidationError(e.getMessage());
            return value;
        }
    }

    private Value resolveValue(FieldDefinition fieldDefinition,
                               String rawValue) throws RuleManagementClientException {

        org.wso2.carbon.identity.rule.metadata.api.model.Value.ValueType
                fieldDefinitionValueType = fieldDefinition.getValue().getValueType();

        switch (fieldDefinitionValueType) {
            case STRING:
                return new Value(Value.Type.STRING, rawValue);
            case NUMBER:
                return validateNumberValue(rawValue);
            case BOOLEAN:
                return validateBooleanValue(rawValue);
            case REFERENCE:
                return new Value(Value.Type.REFERENCE, rawValue);
            default:
                throw new RuleManagementClientException(
                        "Unsupported value type: " + fieldDefinitionValueType + " for field: " +
                                fieldDefinition.getField().getName());
        }
    }

    private boolean isValidValueType(FieldDefinition fieldDefinition, Value value) {

        org.wso2.carbon.identity.rule.metadata.api.model.Value.ValueType fieldDefinitionValueType =
                fieldDefinition.getValue().getValueType();
        Value.Type valueType = value.getType();

        if (valueType != Value.Type.RAW && !valueType.name().equals(fieldDefinitionValueType.name())) {
            setValidationError(
                    "Value type " + valueType + " is not supported for field " + fieldDefinition.getField().getName());
            return false;
        }
        return true;
    }

    private boolean isValidOptionsInputValue(FieldDefinition fieldDefinition, String fieldValue) {

        if (fieldDefinition.getValue() instanceof OptionsInputValue &&
                ((OptionsInputValue) fieldDefinition.getValue()).getValues().stream()
                        .noneMatch(optionsValue -> optionsValue.getName().equals(fieldValue))) {
            setValidationError(
                    "Value " + fieldValue + " is not supported for field " + fieldDefinition.getField().getName());
            return false;
        }

        return true;
    }

    private Value validateNumberValue(String rawValue) throws RuleManagementClientException {

        try {
            Double.parseDouble(rawValue);
            return new Value(Value.Type.NUMBER, rawValue);
        } catch (NumberFormatException e) {
            throw new RuleManagementClientException("Value " + rawValue + " is not a valid NUMBER.");
        }
    }

    private Value validateBooleanValue(String rawValue) throws RuleManagementClientException {

        if (!rawValue.equalsIgnoreCase("true") && !rawValue.equalsIgnoreCase("false")) {
            throw new RuleManagementClientException("Value " + rawValue + " is not a valid BOOLEAN.");
        }
        return new Value(Value.Type.BOOLEAN, rawValue);
    }
}
