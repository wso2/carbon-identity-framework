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

package org.wso2.carbon.identity.rule.management.util;

import org.wso2.carbon.identity.rule.management.exception.RuleManagementClientException;
import org.wso2.carbon.identity.rule.management.exception.RuleManagementException;
import org.wso2.carbon.identity.rule.management.exception.RuleManagementServerException;
import org.wso2.carbon.identity.rule.management.internal.RuleManagementComponentServiceHolder;
import org.wso2.carbon.identity.rule.management.model.Expression;
import org.wso2.carbon.identity.rule.management.model.FlowType;
import org.wso2.carbon.identity.rule.management.model.Rule;
import org.wso2.carbon.identity.rule.management.model.internal.ANDCombinedRule;
import org.wso2.carbon.identity.rule.management.model.internal.ORCombinedRule;
import org.wso2.carbon.identity.rule.metadata.exception.RuleMetadataException;
import org.wso2.carbon.identity.rule.metadata.model.FieldDefinition;
import org.wso2.carbon.identity.rule.metadata.model.OptionsInputValue;

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

        validateExpression(expression);
        addExpressionForANDCombinedRule(expression);
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
                    "Building rule failed due to validation errors. Error: " + errorMessage);
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
                                    org.wso2.carbon.identity.rule.metadata.model.FlowType.valueOf(flowType.name()),
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

    private void validateExpression(Expression expression) {

        FieldDefinition fieldDefinition = expressionMetadataFieldsMap.get(expression.getField());

        if (isError) {
            return;
        }

        if (fieldDefinition == null) {
            setValidationError("Field " + expression.getField() + " is not supported");
            return;
        }

        if (fieldDefinition.getOperators().stream()
                .noneMatch(operator -> operator.getName().equals(expression.getOperator()))) {
            setValidationError("Operator " + expression.getOperator() + " is not supported for field " +
                    expression.getField());
            return;
        }

        if (!fieldDefinition.getValue().getValueType().name().equals(expression.getValue().getType().name())) {
            setValidationError("Value type " + expression.getValue().getType().name() + " is not supported for field "
                    + expression.getField());
        }

        if (fieldDefinition.getValue() instanceof OptionsInputValue &&
                ((OptionsInputValue) fieldDefinition.getValue()).getValues().stream().noneMatch(
                        optionsValue -> optionsValue.getName().equals(expression.getValue().getFieldValue()))) {
            setValidationError("Value " + expression.getValue().getFieldValue() + " is not supported for field " +
                    expression.getField());
        }
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
}
