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

package org.wso2.carbon.identity.workflow.mgt.rule;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.claim.metadata.mgt.ClaimMetadataManagementService;
import org.wso2.carbon.identity.claim.metadata.mgt.exception.ClaimMetadataException;
import org.wso2.carbon.identity.claim.metadata.mgt.model.LocalClaim;
import org.wso2.carbon.identity.claim.metadata.mgt.util.ClaimConstants;
import org.wso2.carbon.identity.rule.metadata.api.exception.RuleMetadataException;
import org.wso2.carbon.identity.rule.metadata.api.model.Field;
import org.wso2.carbon.identity.rule.metadata.api.model.FieldDefinition;
import org.wso2.carbon.identity.rule.metadata.api.model.FlowType;
import org.wso2.carbon.identity.rule.metadata.api.model.InputValue;
import org.wso2.carbon.identity.rule.metadata.api.model.Operator;
import org.wso2.carbon.identity.rule.metadata.api.model.Value;
import org.wso2.carbon.identity.rule.metadata.api.provider.RuleMetadataProvider;
import org.wso2.carbon.identity.workflow.mgt.internal.WorkflowServiceDataHolder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Approval workflow rule metadata provider.
 * This class is responsible for providing field definitions
 * that can be used in approval workflow rule expressions.
 */
public class ApprovalWorkflowMetadataProvider implements RuleMetadataProvider {

    private static final Log LOG = LogFactory.getLog(ApprovalWorkflowMetadataProvider.class);

    private static final String USER_CLAIM_PREFIX = "user.";
    private static final String INITIATOR_CLAIM_PREFIX = "initiator.";

    private static final List<Operator> CLAIM_OPERATORS = Collections.unmodifiableList(Arrays.asList(
            new Operator("equals", "equals"),
            new Operator("notEquals", "not equals"),
            new Operator("contains", "contains")
    ));

    /**
     * Get the expression metadata for the given flow type.
     * Returns field definitions for approval workflow specific fields and dynamic user claim fields.
     *
     * @param flowType     Flow type.
     * @param tenantDomain Tenant domain.
     * @return List of field definitions for approval workflow, or empty list for other flow types.
     * @throws RuleMetadataException If an error occurred while getting the metadata.
     */
    @Override
    public List<FieldDefinition> getExpressionMeta(FlowType flowType, String tenantDomain)
            throws RuleMetadataException {

        if (flowType != FlowType.APPROVAL_WORKFLOW) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Skipping metadata generation for flow type: " + flowType +
                        ". This provider only supports APPROVAL_WORKFLOW.");
            }
            return Collections.emptyList();
        }
        List<FieldDefinition> fieldDefinitions = new ArrayList<>(getStaticApprovalWorkflowFields());
        List<LocalClaim> localClaims = getAllowedUserClaims(tenantDomain);
        for (LocalClaim localClaim : localClaims) {
            fieldDefinitions.addAll(createClaimFieldDefinitions(localClaim));
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug("Generated " + fieldDefinitions.size() + " field definitions for approval workflow for tenant: "
                    + tenantDomain);
        }
        return fieldDefinitions;
    }

    /**
     * Get the list of static, approval workflow specific field definitions from the registry.
     *
     * @return List of static field definitions.
     */
    private List<FieldDefinition> getStaticApprovalWorkflowFields() {

        List<FieldDefinition> staticFields = new ArrayList<>(WorkflowRuleFieldRegistry.FIELDS.values());
        if (LOG.isDebugEnabled()) {
            LOG.debug("Loaded " + staticFields.size() + " static approval workflow fields from registry.");
        }
        return staticFields;
    }

    /**
     * Get the list of allowed user claims dynamically at runtime.
     * Fetches all local claims from the claim management service.
     *
     * @param tenantDomain Tenant domain.
     * @return List of allowed user claims.
     * @throws RuleMetadataException If an error occurs while fetching claims.
     */
    private List<LocalClaim> getAllowedUserClaims(String tenantDomain) throws RuleMetadataException {

        ClaimMetadataManagementService claimService = WorkflowServiceDataHolder.getInstance()
                .getClaimMetadataManagementService();
        try {
            List<LocalClaim> localClaims = claimService.getLocalClaims(tenantDomain);
            if (localClaims == null || localClaims.isEmpty()) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("No local claims found for tenant: " + tenantDomain);
                }
                return Collections.emptyList();
            }
            if (LOG.isDebugEnabled()) {
                LOG.debug("Retrieved " + localClaims.size() + " local claims for tenant: " + tenantDomain);
            }
            return localClaims;
        } catch (ClaimMetadataException e) {
            throw new RuleMetadataException("WORKFLOW_METADATA_ERROR_50001",
                    "Error fetching local claims for tenant: " + tenantDomain,
                    "An error occurred while retrieving local claims from the claim metadata service.", e);
        }
    }

    /**
     * Create two field definitions for a local claim — one prefixed with {user.}
     * and one prefixed with {initiator.}.
     *
     * @param localClaim Local claim.
     * @return List of two field definitions for the claim.
     */
    private List<FieldDefinition> createClaimFieldDefinitions(LocalClaim localClaim) {

        String claimUri = localClaim.getClaimURI();
        String displayName = getClaimDisplayName(localClaim);
        List<Operator> operators = CLAIM_OPERATORS;
        Value valueMeta = new InputValue(Value.ValueType.STRING);

        List<FieldDefinition> fieldDefinitions = new ArrayList<>();
        fieldDefinitions.add(new FieldDefinition(
                new Field(USER_CLAIM_PREFIX + claimUri, USER_CLAIM_PREFIX + displayName),
                operators, valueMeta));
        fieldDefinitions.add(new FieldDefinition(
                new Field(INITIATOR_CLAIM_PREFIX + claimUri, INITIATOR_CLAIM_PREFIX + displayName),
                operators, valueMeta));
        return fieldDefinitions;
    }

    /**
     * Get the display name of a claim.
     *
     * @param localClaim Local claim.
     * @return Display name of the claim.
     */
    private String getClaimDisplayName(LocalClaim localClaim) {

        String displayName = localClaim.getClaimProperty(ClaimConstants.DISPLAY_NAME_PROPERTY);
        if (StringUtils.isNotBlank(displayName)) {
            return displayName;
        }
        // Fallback to claim URI if display name is not available.
        return localClaim.getClaimURI();
    }

    /**
     * Get the list of operators applicable for claim fields.
     *
     * @return List of operators.
     */
    private List<Operator> getOperatorsForClaim() {

        return CLAIM_OPERATORS;
    }
}
