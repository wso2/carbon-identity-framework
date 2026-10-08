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
import org.wso2.carbon.identity.flow.mgt.model.StepDTO;
import org.wso2.carbon.identity.flow.mgt.internal.FlowMgtServiceDataHolder;
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

import static org.wso2.carbon.identity.flow.mgt.utils.FlowMgtUtils.handleClientException;
import static org.wso2.carbon.identity.flow.mgt.utils.FlowMgtUtils.handleServerException;

/**
 * Checks the conditions on a flow's decisions against what the flow actually publishes.
 * <p>
 * The check is the one rule management applies to any rule: the field has to exist, the operator has
 * to be one that field accepts, and the value has to match the field's type. Replaying the condition
 * through the same builder is what keeps a flow condition and an action condition rejecting for the
 * same reasons, rather than two validators drifting apart.
 */
public class FlowRuleValidator {

    private static final String IN = "in";
    private static final String NOT_IN = "notIn";

    /**
     * The only operators a multi-valued claim accepts. Anything else compares against the joined
     * form of the values, which matches substrings of neighbouring values and is wrong in a way
     * nothing reports at runtime.
     */
    private static final Set<String> MEMBERSHIP_OPERATORS =
            Collections.unmodifiableSet(new HashSet<>(Arrays.asList(IN, NOT_IN)));

    private static final Set<String> NUMERIC_OPERATORS = Collections.unmodifiableSet(new HashSet<>(
            Arrays.asList("equals", "notEquals", "greaterThan", "lessThan")));

    /**
     * Everything that is not a number is compared as text, dates and booleans included: nothing
     * validates their format on write, so ordering them would compare strings.
     */
    private static final Set<String> TEXT_OPERATORS = Collections.unmodifiableSet(new HashSet<>(
            Arrays.asList("equals", "notEquals", "contains", "notContains", "startsWith", "endsWith")));

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

        if (flowDTO == null || flowDTO.getSteps() == null) {
            return;
        }
        ClaimLookup claims = new ClaimLookup(tenantDomain);
        for (StepDTO step : flowDTO.getSteps()) {
            if (!Constants.StepTypes.RULE_EVALUATION.equals(step.getType()) || step.getData() == null) {
                continue;
            }
            ActionDTO action = step.getData().getAction();
            if (action == null || action.getBranches() == null) {
                continue;
            }
            for (BranchDTO branch : action.getBranches()) {
                if (branch.getRule() != null) {
                    validateBranchRule(step.getId(), branch, flowDTO.getFlowType(), tenantDomain, claims);
                }
            }
        }
    }

    private static void validateBranchRule(String stepId, BranchDTO branch, String flowType, String tenantDomain,
                                           ClaimLookup claims) throws FlowMgtFrameworkException {

        FlowType ruleFlowType;
        try {
            ruleFlowType = FlowType.valueOf(flowType);
        } catch (IllegalArgumentException e) {
            throw handleClientException(Constants.ErrorMessages.ERROR_CODE_RULES_NOT_SUPPORTED_FOR_FLOW,
                    flowType, stepId);
        }

        /*
         * The rule builder checks these too, but reports every failure as one generic invalid condition. Checking
         * them first gives the author the specific reason: a missing or stray qualifier, or a list under the wrong
         * operator.
         */
        for (ANDCombinedRule andCombinedRule : branch.getRule().getRules()) {
            for (Expression expression : andCombinedRule.getExpressions()) {
                validateKey(branch, expression);
                validateValueShape(branch, expression);
            }
        }

        try {
            RuleBuilder ruleBuilder = RuleBuilder.create(ruleFlowType, tenantDomain);
            boolean firstGroup = true;
            for (ANDCombinedRule andCombinedRule : branch.getRule().getRules()) {
                if (!firstGroup) {
                    // Closes the group before it and opens the next.
                    ruleBuilder.addOrCondition();
                }
                for (Expression expression : andCombinedRule.getExpressions()) {
                    ruleBuilder.addAndExpression(expression);
                }
                firstGroup = false;
            }
            ruleBuilder.build();
        } catch (RuleManagementException e) {
            throw handleClientException(Constants.ErrorMessages.ERROR_CODE_INVALID_BRANCH_RULE,
                    branch.getName(), stepId, e.getMessage());
        }

        /*
         * The builder checks the operator against what the *field* declares, and a claim field
         * declares everything any claim could accept. Which operators are legal depends on the claim
         * the qualifier names, so that check can only happen once the qualifier is known.
         */
        for (ANDCombinedRule andCombinedRule : branch.getRule().getRules()) {
            for (Expression expression : andCombinedRule.getExpressions()) {
                if (isClaimField(expression.getField())) {
                    validateClaimOperator(branch, expression, claims);
                }
                validateReferencedClaim(branch, expression, claims);
            }
        }
    }

    private static boolean isClaimField(String field) {

        return FlowRuleMetadataProvider.USER_CLAIMS.equals(field)
                || FlowRuleMetadataProvider.USER_COLLECTED_CLAIMS.equals(field);
    }

    /**
     * A qualified field is meaningless without its qualifier, and a qualifier on a field that names one value would be
     * stored and never read.
     */
    private static void validateKey(BranchDTO branch, Expression expression) throws FlowMgtClientException {

        boolean hasQualifier = StringUtils.isNotBlank(expression.getFieldQualifier());
        if (FlowRuleMetadataProvider.isQualified(expression.getField())) {
            if (!hasQualifier) {
                throw handleClientException(isClaimField(expression.getField())
                                ? Constants.ErrorMessages.ERROR_CODE_CLAIM_QUALIFIER_REQUIRED
                                : Constants.ErrorMessages.ERROR_CODE_QUALIFIER_REQUIRED,
                        expression.getField(), branch.getName());
            }
        } else if (hasQualifier) {
            throw handleClientException(Constants.ErrorMessages.ERROR_CODE_QUALIFIER_NOT_ALLOWED,
                    expression.getField(), branch.getName());
        }
    }

    private static void validateClaimOperator(BranchDTO branch, Expression expression, ClaimLookup claims)
            throws FlowMgtFrameworkException {

        LocalClaim claim = claims.find(expression.getFieldQualifier());
        if (claim == null) {
            throw handleClientException(Constants.ErrorMessages.ERROR_CODE_UNKNOWN_CLAIM,
                    branch.getName(), expression.getFieldQualifier());
        }

        boolean multiValued = Boolean.parseBoolean(
                claim.getClaimProperty(ClaimConstants.MULTI_VALUED_PROPERTY));
        Set<String> allowed = multiValued
                ? MEMBERSHIP_OPERATORS
                : operatorsFor(claim.getClaimProperty(ClaimConstants.DATA_TYPE_PROPERTY));

        if (!allowed.contains(expression.getOperator())) {
            String reason = multiValued
                    ? "It holds more than one value, so only in and notIn compare it by value."
                    : "Allowed operators are: " + String.join(", ", allowed) + ".";
            throw handleClientException(Constants.ErrorMessages.ERROR_CODE_OPERATOR_NOT_ALLOWED_FOR_CLAIM,
                    expression.getOperator(), expression.getFieldQualifier(), branch.getName(), reason);
        }
    }

    /**
     * Membership needs a set on the right; everything else needs a single value. Checked for every
     * field, since a mismatch on any of them evaluates to something the author did not write.
     */
    /**
     * A value read from a claim field must name a claim that exists, as the expression's own claim must.
     */
    private static void validateReferencedClaim(BranchDTO branch, Expression expression, ClaimLookup claims)
            throws FlowMgtFrameworkException {

        FieldReference reference = expression.getValue() == null ? null : expression.getValue().getFieldReference();
        if (reference == null || !isClaimField(reference.getName())) {
            return;
        }
        if (claims.find(reference.getQualifier()) == null) {
            throw handleClientException(Constants.ErrorMessages.ERROR_CODE_UNKNOWN_CLAIM,
                    branch.getName(), reference.getQualifier());
        }
    }

    private static void validateValueShape(BranchDTO branch, Expression expression) throws FlowMgtClientException {

        if (expression.getValue() != null && expression.getValue().getType() == Value.Type.FIELD) {
            // Read from another field: whether that holds one value or several is only known at evaluation.
            return;
        }
        boolean membership = MEMBERSHIP_OPERATORS.contains(expression.getOperator());
        boolean listValued = expression.getValue() != null
                && expression.getValue().getType() == Value.Type.LIST;
        if (membership != listValued) {
            throw handleClientException(Constants.ErrorMessages.ERROR_CODE_LIST_VALUE_MISMATCH,
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
     * The local claims of a tenant, read once per save however many conditions refer to them, and
     * only when some condition does.
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
                // Not the author's mistake, so not reported as an unknown claim.
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
