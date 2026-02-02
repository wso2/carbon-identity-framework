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
import java.util.Collections;
import java.util.List;

/**
 * Approval workflow rule metadata provider.
 * This class is responsible for providing dynamic field definitions for user claims
 * that can be used in workflow approval rule expressions.
 * Claims are fetched dynamically at runtime for workflow flow types.
 */
public class ApprovalWorkflowMetadataProvider implements RuleMetadataProvider {

    private static final Log LOG = LogFactory.getLog(ApprovalWorkflowMetadataProvider.class);

    /**
     * Get the expression metadata for the given flow type.
     * Returns field definitions for all available user claim URIs.
     * Claims are fetched dynamically at runtime.
     *
     * @param flowType     Flow type.
     * @param tenantDomain Tenant domain.
     * @return List of field definitions for user claims.
     * @throws RuleMetadataException If an error occurred while getting the metadata.
     */
    @Override
    public List<FieldDefinition> getExpressionMeta(FlowType flowType, String tenantDomain)
            throws RuleMetadataException {

        List<LocalClaim> localClaims = getAllowedUserClaims(tenantDomain);
        if (localClaims.isEmpty()) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("No user claims found for tenant: " + tenantDomain);
            }
            return Collections.emptyList();
        }

        List<FieldDefinition> fieldDefinitions = new ArrayList<>();
        for (LocalClaim localClaim : localClaims) {
            FieldDefinition fieldDefinition = createFieldDefinitionForClaim(localClaim);
            fieldDefinitions.add(fieldDefinition);
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("Generated " + fieldDefinitions.size() + " field definitions for user claims for tenant: " +
                    tenantDomain);
        }

        return fieldDefinitions;
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

        if (claimService == null) {
            LOG.warn("ClaimMetadataManagementService is not available. Returning empty claim list.");
            return Collections.emptyList();
        }

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
     * Create a field definition for a user claim.
     *
     * @param localClaim Local claim.
     * @return FieldDefinition for the claim.
     */
    private FieldDefinition createFieldDefinitionForClaim(LocalClaim localClaim) {

        String claimUri = localClaim.getClaimURI();
        String displayName = getClaimDisplayName(localClaim);

        Field field = new Field(claimUri, displayName);
        List<Operator> operators = getOperatorsForClaim();
        Value valueMeta = new InputValue(Value.ValueType.STRING);

        return new FieldDefinition(field, operators, valueMeta);
    }

    /**
     * Get the display name of a claim.
     * Returns the DisplayName property if available, otherwise falls back to the claim URI.
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
     * Returns basic comparison operators: equals and notEquals.
     *
     * @return List of operators.
     */
    private List<Operator> getOperatorsForClaim() {

        List<Operator> operators = new ArrayList<>();

        // Add equals operator.
        operators.add(new Operator("equals", "Equals"));
        
        // Add notEquals operator.
        operators.add(new Operator("notEquals", "Not Equals"));

        return operators;
    }
}