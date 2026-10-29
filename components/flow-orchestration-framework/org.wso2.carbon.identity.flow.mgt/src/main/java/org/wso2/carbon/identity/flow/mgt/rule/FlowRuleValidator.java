/*
 * Copyright (c) 2026, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.carbon.identity.flow.mgt.rule;

import org.apache.commons.lang.StringUtils;
import org.wso2.carbon.identity.claim.metadata.mgt.ClaimMetadataManagementService;
import org.wso2.carbon.identity.claim.metadata.mgt.exception.ClaimMetadataException;
import org.wso2.carbon.identity.claim.metadata.mgt.model.LocalClaim;
import org.wso2.carbon.identity.claim.metadata.mgt.util.ClaimConstants;
import org.wso2.carbon.identity.flow.mgt.Constants;
import org.wso2.carbon.identity.flow.mgt.exception.FlowMgtClientException;
import org.wso2.carbon.identity.flow.mgt.exception.FlowMgtFrameworkException;
import org.wso2.carbon.identity.flow.mgt.exception.FlowMgtServerException;
import org.wso2.carbon.identity.flow.mgt.model.ActionDTO;
import org.wso2.carbon.identity.flow.mgt.model.BranchDTO;
import org.wso2.carbon.identity.flow.mgt.model.FlowDTO;
import org.wso2.carbon.identity.flow.mgt.model.FlowVariableDTO;
import org.wso2.carbon.identity.flow.mgt.model.FlowVariableDataType;
import org.wso2.carbon.identity.flow.mgt.model.FlowVariablesDTO;
import org.wso2.carbon.identity.flow.mgt.model.StepDTO;
import org.wso2.carbon.identity.flow.mgt.internal.FlowMgtServiceDataHolder;
import org.wso2.carbon.identity.flow.mgt.utils.FlowMgtUtils;
import org.wso2.carbon.identity.flow.mgt.utils.FlowVariableUtils;
import org.wso2.carbon.identity.rule.management.api.exception.RuleManagementException;
import org.wso2.carbon.identity.rule.management.api.model.ANDCombinedRule;
import org.wso2.carbon.identity.rule.management.api.model.Expression;
import org.wso2.carbon.identity.rule.management.api.model.FieldReference;
import org.wso2.carbon.identity.rule.management.api.model.FlowType;
import org.wso2.carbon.identity.rule.management.api.util.RuleBuilder;
import org.wso2.carbon.identity.rule.management.api.model.Value;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.wso2.carbon.identity.flow.mgt.utils.FlowMgtUtils.handleDecisionStepException;
import static org.wso2.carbon.identity.flow.mgt.utils.FlowMgtUtils.handleServerException;

/**
 * Checks the conditions on a flow's decisions against what the flow actually publishes.
 */
public class FlowRuleValidator {

    private static final String IN = "in";
    private static final String NOT_IN = "notIn";
    private static final String EQUALS = "equals";
    private static final String NOT_EQUALS = "notEquals";
    private static final String IS_BLANK = "isBlank";
    private static final String IS_NOT_BLANK = "isNotBlank";

    /**
     * The operators that take a list of values.
     */
    private static final Set<String> LIST_OPERATORS = Set.of(IN, NOT_IN);

    /**
     * The only operators a multi-valued attribute accepts: they check its entries.
     */
    private static final Set<String> MULTI_VALUED_OPERATORS = Set.of(IN, NOT_IN, "isEmpty", "isNotEmpty");

    private static final Set<String> NUMERIC_OPERATORS = Set.of(EQUALS, NOT_EQUALS, "greaterThan", "lessThan",
            IS_BLANK, IS_NOT_BLANK);

    /**
     * Everything that is not a number is compared as text, dates and booleans included: nothing validates their format
     * on write, so ordering them would compare strings.
     */
    private static final Set<String> TEXT_OPERATORS = Set.of(EQUALS, NOT_EQUALS, "contains", "notContains",
            "startsWith", "endsWith", IS_BLANK, IS_NOT_BLANK);

    private static final Set<String> BOOLEAN_OPERATORS = Set.of(EQUALS, NOT_EQUALS);

    private FlowRuleValidator() {

    }

    /**
     * Validate every condition in the flow.
     *
     * @param flowDTO      Flow being saved.
     * @param tenantDomain Tenant domain, which decides what the metadata publishes.
     * @throws FlowMgtClientException If a condition names something the flow does not publish.
     * @throws FlowMgtServerException If the claims of the tenant cannot be read.
     */
    public static void validate(FlowDTO flowDTO, String tenantDomain) throws FlowMgtFrameworkException {

        // Without conditional branching, the graph builder rejects decision steps, so there is nothing to validate.
        if (flowDTO == null || flowDTO.getSteps() == null || !FlowMgtUtils.isConditionalBranchingEnabled()) {
            return;
        }
        ClaimLookup claims = new ClaimLookup(tenantDomain);
        FlowVariableLookup variables = new FlowVariableLookup(flowDTO.getFlowVariables());
        for (StepDTO step : flowDTO.getSteps()) {
            for (BranchDTO branch : branchesOf(step)) {
                if (branch.getRule() != null) {
                    validateBranchRule(step.getId(), branch, flowDTO.getFlowType(), tenantDomain, claims,
                            variables);
                }
            }
        }
    }

    /**
     * The branches of a decision step, or none for any other step.
     */
    private static List<BranchDTO> branchesOf(StepDTO step) {

        if (!Constants.StepTypes.RULE_EVALUATION.equals(step.getType()) || step.getData() == null) {
            return Collections.emptyList();
        }
        ActionDTO action = step.getData().getAction();
        return action == null || action.getBranches() == null ? Collections.emptyList() : action.getBranches();
    }

    private static void validateBranchRule(String stepId, BranchDTO branch, String flowType, String tenantDomain,
                                           ClaimLookup claims, FlowVariableLookup variables)
            throws FlowMgtFrameworkException {

        if (StringUtils.isBlank(flowType)) {
            throw handleDecisionStepException("Flow type not provided.",
                    "The flow type is required to validate the rules of its decision steps.");
        }
        FlowType ruleFlowType;
        try {
            ruleFlowType = FlowType.valueOf(flowType);
        } catch (IllegalArgumentException e) {
            throw handleDecisionStepException("Rules are not supported for this flow.",
                    "The %s flow does not publish rule fields, so the decision step %s cannot be evaluated.",
                    flowType, stepId);
        }
        validateRuleShape(stepId, branch);

        // Checked before the rule builder, so the error names the specific problem.
        for (ANDCombinedRule andCombinedRule : branch.getRule().getRules()) {
            for (Expression expression : andCombinedRule.getExpressions()) {
                validateQualifier(branch, expression);
                validateValueShape(branch, expression);
            }
        }

        try {
            buildRule(branch, ruleFlowType, tenantDomain);
        } catch (RuleManagementException e) {
            throw invalidRule(stepId, branch, e.getMessage());
        }

        for (ANDCombinedRule andCombinedRule : branch.getRule().getRules()) {
            for (Expression expression : andCombinedRule.getExpressions()) {
                if (isAttributeField(expression.getField())) {
                    validateAttributeOperator(branch, expression, claims);
                } else if (variables.isVariableField(expression.getField())) {
                    validateVariableCondition(branch, expression, variables);
                }
                validateReferencedAttribute(branch, expression, claims);
            }
        }
    }

    /**
     * Builds the branch's rule with the rule builder, which checks it against the fields the flow publishes.
     */
    private static void buildRule(BranchDTO branch, FlowType ruleFlowType, String tenantDomain)
            throws RuleManagementException {

        RuleBuilder ruleBuilder = RuleBuilder.create(ruleFlowType, tenantDomain);
        boolean firstGroup = true;
        for (ANDCombinedRule andCombinedRule : branch.getRule().getRules()) {
            if (!firstGroup) {
                ruleBuilder.addOrCondition();
            }
            for (Expression expression : andCombinedRule.getExpressions()) {
                ruleBuilder.addAndExpression(expression);
            }
            firstGroup = false;
        }
        ruleBuilder.build();
    }

    /**
     * A branch's rule must hold at least one group of conditions, and every group at least one condition, so that the
     * checks below and the rule builder have something to read.
     */
    private static void validateRuleShape(String stepId, BranchDTO branch) throws FlowMgtClientException {

        List<ANDCombinedRule> groups = branch.getRule().getRules();
        if (groups == null || groups.isEmpty()) {
            throw invalidRule(stepId, branch, "The rule has no groups of conditions combined with OR.");
        }
        for (ANDCombinedRule group : groups) {
            if (group == null || group.getExpressions() == null || group.getExpressions().isEmpty()
                    || group.getExpressions().contains(null)) {
                throw invalidRule(stepId, branch, "A group of conditions combined with AND is empty.");
            }
        }
    }

    private static FlowMgtClientException invalidRule(String stepId, BranchDTO branch, String reason) {

        return handleDecisionStepException("Invalid rule.",
                "The rule on branch %s of the decision step %s is not valid: %s", branch.getName(), stepId, reason);
    }

    private static boolean isAttributeField(String field) {

        return FlowRuleMetadataProvider.USER_ATTRIBUTES.equals(field)
                || FlowRuleMetadataProvider.USER_COLLECTED_ATTRIBUTES.equals(field);
    }

    /**
     * A qualified field is meaningless without its qualifier, and a qualifier on a field that names one value would be
     * stored and never read.
     */
    private static void validateQualifier(BranchDTO branch, Expression expression) throws FlowMgtClientException {

        boolean hasQualifier = StringUtils.isNotBlank(expression.getFieldQualifier());
        if (FlowRuleMetadataProvider.isQualified(expression.getField())) {
            if (!hasQualifier) {
                if (isAttributeField(expression.getField())) {
                    throw handleDecisionStepException("Attribute not selected.",
                            "A condition on %s in branch %s does not say which attribute it compares.",
                            expression.getField(), branch.getName());
                }
                throw handleDecisionStepException("Qualifier not provided.",
                        "A condition on %s in branch %s does not say which value of that field it compares.",
                        expression.getField(), branch.getName());
            }
        } else if (hasQualifier) {
            throw handleDecisionStepException("Qualifier not allowed.",
                    "The field %s in branch %s names a single value and takes no qualifier.",
                    expression.getField(), branch.getName());
        }
    }

    private static void validateAttributeOperator(BranchDTO branch, Expression expression, ClaimLookup claims)
            throws FlowMgtFrameworkException {

        LocalClaim claim = claims.find(expression.getFieldQualifier());
        if (claim == null) {
            throw handleDecisionStepException("Unknown attribute.",
                    "The rule in branch %s compares the attribute %s, which is not an attribute of this organization.",
                    branch.getName(), expression.getFieldQualifier());
        }

        boolean multiValued = Boolean.parseBoolean(
                claim.getClaimProperty(ClaimConstants.MULTI_VALUED_PROPERTY));
        Set<String> allowed = multiValued
                ? MULTI_VALUED_OPERATORS
                : operatorsFor(claim.getClaimProperty(ClaimConstants.DATA_TYPE_PROPERTY));

        if (!allowed.contains(expression.getOperator())) {
            String reason = multiValued
                    ? "It holds more than one value, so only in, notIn, isEmpty and isNotEmpty apply."
                    : "It holds a single value. Allowed operators are: " + String.join(", ", allowed) + ".";
            throw handleDecisionStepException("Operator not allowed for this attribute.",
                    "The operator %s cannot be used with the attribute %s in branch %s. %s",
                    expression.getOperator(), expression.getFieldQualifier(), branch.getName(), reason);
        }
    }

    /**
     * A condition on a header or runtime value must name one the flow defines, use an operator that
     * suits its type, and give values of that type.
     */
    private static void validateVariableCondition(BranchDTO branch, Expression expression,
                                                  FlowVariableLookup variables)
            throws FlowMgtClientException {

        FlowVariableDataType dataType = variables.find(expression.getField(), expression.getFieldQualifier());
        if (dataType == null) {
            throw handleDecisionStepException("Value not defined.",
                    "The rule in branch %s compares %s of %s, which the flow does not define.",
                    branch.getName(), expression.getFieldQualifier(), expression.getField());
        }
        Set<String> allowed = operatorsFor(dataType);
        if (!allowed.contains(expression.getOperator())) {
            throw handleDecisionStepException("Operator not allowed for this value.",
                    "The operator %s cannot be used with %s of %s in branch %s, which is of type %s. Allowed " +
                            "operators are: %s.", expression.getOperator(), expression.getFieldQualifier(),
                    expression.getField(), branch.getName(), dataType, String.join(", ", allowed));
        }
        Value value = expression.getValue();
        if (value == null) {
            return;
        }
        List<String> entries = value.getType() == Value.Type.LIST
                ? value.getFieldValues() : Collections.singletonList(value.getFieldValue());
        for (String entry : entries) {
            if (entry != null && !isValidLiteral(dataType, entry)) {
                throw handleDecisionStepException("Value does not match the type.",
                        "The value %s compared with %s of %s in branch %s is not a valid %s.%s", entry,
                        expression.getFieldQualifier(), expression.getField(), branch.getName(), dataType,
                        dataType == FlowVariableDataType.NUMBER_ARRAY
                                ? " Numbers in a list are compared as written, so write them without trailing "
                                        + "zeros, such as 18 rather than 18.0."
                                : "");
            }
        }
    }

    /**
     * Arrays hold numbers as text, so a number in a list must be written as the flow writes it to match.
     */
    private static boolean isValidLiteral(FlowVariableDataType dataType, String literal) {

        switch (dataType) {
            case INTEGER:
                return FlowVariableUtils.parse(FlowVariableDataType.INTEGER, literal) != null;
            case DECIMAL:
                return FlowVariableUtils.parse(FlowVariableDataType.DECIMAL, literal) != null;
            case BOOLEAN:
                return FlowVariableUtils.parse(FlowVariableDataType.BOOLEAN, literal) != null;
            case NUMBER_ARRAY:
                Object number = FlowVariableUtils.parse(FlowVariableDataType.DECIMAL, literal);
                return number != null && FlowVariableUtils.toText((Number) number).equals(literal);
            default:
                return true;
        }
    }

    private static Set<String> operatorsFor(FlowVariableDataType dataType) {

        if (dataType.isArray()) {
            return MULTI_VALUED_OPERATORS;
        }
        switch (dataType) {
            case INTEGER, DECIMAL:
                return NUMERIC_OPERATORS;
            case BOOLEAN:
                return BOOLEAN_OPERATORS;
            default:
                return TEXT_OPERATORS;
        }
    }

    /**
     * A value read from an attribute field must name an attribute that exists, as the expression's own attribute must.
     */
    private static void validateReferencedAttribute(BranchDTO branch, Expression expression, ClaimLookup claims)
            throws FlowMgtFrameworkException {

        FieldReference reference = expression.getValue() == null ? null : expression.getValue().getFieldReference();
        if (reference == null || !isAttributeField(reference.getName())) {
            return;
        }
        if (claims.find(reference.getQualifier()) == null) {
            throw handleDecisionStepException("Unknown attribute.",
                    "The rule in branch %s compares the attribute %s, which is not an attribute of this organization.",
                    branch.getName(), reference.getQualifier());
        }
    }

    /**
     * in and notIn need a list on the right, and no other operator takes one. Operators that take no value, such as
     * isBlank, are checked by the rule builder.
     */
    private static void validateValueShape(BranchDTO branch, Expression expression) throws FlowMgtClientException {

        if (expression.getValue() != null && expression.getValue().getType() == Value.Type.FIELD) {
            return;
        }
        boolean membership = LIST_OPERATORS.contains(expression.getOperator());
        boolean listValued = expression.getValue() != null
                && expression.getValue().getType() == Value.Type.LIST;
        if (membership != listValued) {
            throw handleDecisionStepException("Value does not match the operator.",
                    "The operator %s in branch %s %s a list of values.",
                    expression.getOperator(), branch.getName(), membership ? "requires" : "does not take");
        }
    }

    private static Set<String> operatorsFor(String dataType) {

        if (ClaimConstants.ClaimDataType.INTEGER.name().equalsIgnoreCase(dataType)
                || ClaimConstants.ClaimDataType.DECIMAL.name().equalsIgnoreCase(dataType)) {
            return NUMERIC_OPERATORS;
        }
        return TEXT_OPERATORS;
    }

    /**
     * The types of the headers and runtime values the flow being saved defines, by the field a
     * condition reads them through.
     */
    private static final class FlowVariableLookup {

        private final Map<String, Map<String, FlowVariableDataType>> typesByField = new HashMap<>();

        private FlowVariableLookup(FlowVariablesDTO variables) {

            Map<String, FlowVariableDataType> runtimeValues = new HashMap<>();
            Map<String, FlowVariableDataType> headers = new HashMap<>();
            if (variables != null) {
                addAll(runtimeValues, variables.getRuntimeValues());
                addAll(headers, variables.getHeaders());
            }
            typesByField.put(FlowRuleMetadataProvider.RUNTIME_VALUES, runtimeValues);
            typesByField.put(FlowRuleMetadataProvider.REQUEST_HEADERS, headers);
        }

        private boolean isVariableField(String field) {

            return typesByField.containsKey(field);
        }

        private FlowVariableDataType find(String field, String name) {

            if (name == null) {
                return null;
            }
            String key = FlowRuleMetadataProvider.REQUEST_HEADERS.equals(field)
                    ? FlowVariableUtils.normalizeHeaderName(name) : name;
            return typesByField.get(field).get(key);
        }

        private static void addAll(Map<String, FlowVariableDataType> types,
                                   List<? extends FlowVariableDTO> variables) {

            for (FlowVariableDTO variable : variables) {
                types.put(variable.getName(), variable.getDataType());
            }
        }
    }

    /**
     * The local claims of a tenant, read once per save however many conditions refer to them, and only when some
     * condition does.
     */
    private static final class ClaimLookup {

        private final String tenantDomain;
        private Map<String, LocalClaim> claimsByUri;

        private ClaimLookup(String tenantDomain) {

            this.tenantDomain = tenantDomain;
        }

        private LocalClaim find(String claimUri) throws FlowMgtServerException {

            if (claimsByUri == null) {
                claimsByUri = load();
            }
            return claimsByUri.get(claimUri);
        }

        private Map<String, LocalClaim> load() throws FlowMgtServerException {

            ClaimMetadataManagementService claimService =
                    FlowMgtServiceDataHolder.getInstance().getClaimMetadataManagementService();
            if (claimService == null) {
                throw handleServerException(Constants.ErrorMessages.ERROR_CODE_GET_CLAIM_METADATA, tenantDomain);
            }
            try {
                Map<String, LocalClaim> claims = new HashMap<>();
                List<LocalClaim> localClaims = claimService.getLocalClaims(tenantDomain);
                if (localClaims != null) {
                    for (LocalClaim localClaim : localClaims) {
                        claims.put(localClaim.getClaimURI(), localClaim);
                    }
                }
                return claims;
            } catch (ClaimMetadataException e) {
                throw handleServerException(Constants.ErrorMessages.ERROR_CODE_GET_CLAIM_METADATA, e,
                        tenantDomain);
            }
        }
    }
}
