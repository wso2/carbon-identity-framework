package org.wso2.carbon.identity.rule.metadata.internal.provider.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.claim.metadata.mgt.ClaimMetadataManagementService;
import org.wso2.carbon.identity.claim.metadata.mgt.exception.ClaimMetadataException;
import org.wso2.carbon.identity.claim.metadata.mgt.model.LocalClaim;
import org.wso2.carbon.identity.rule.metadata.api.exception.RuleMetadataException;
import org.wso2.carbon.identity.rule.metadata.api.model.Field;
import org.wso2.carbon.identity.rule.metadata.api.model.FieldDefinition;
import org.wso2.carbon.identity.rule.metadata.api.model.FlowType;
import org.wso2.carbon.identity.rule.metadata.api.model.InputValue;
import org.wso2.carbon.identity.rule.metadata.api.model.Operator;
import org.wso2.carbon.identity.rule.metadata.api.model.Value;
import org.wso2.carbon.identity.rule.metadata.api.provider.RuleMetadataProvider;
import org.wso2.carbon.identity.rule.metadata.internal.component.RuleMetadataServiceDataHolder;
import org.wso2.carbon.identity.rule.metadata.internal.config.RuleMetadataConfigFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * User claim metadata provider.
 * This class is responsible for providing dynamic field definitions for user claims
 * that can be used in rule expressions without defining them in fields.json.
 * Claims are fetched dynamically at runtime for all flow types.
 */
public class DynamicRuleMetadataProvider implements RuleMetadataProvider {

    private static final Log LOG = LogFactory.getLog(DynamicRuleMetadataProvider.class);


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

        List<String> allowedClaimUris = getAllowedUserClaimUris(tenantDomain);
        if (allowedClaimUris.isEmpty()) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("No user claims found for tenant: " + tenantDomain);
            }
            return Collections.emptyList();
        }

        List<FieldDefinition> fieldDefinitions = new ArrayList<>();
        for (String claimUri : allowedClaimUris) {
            FieldDefinition fieldDefinition = createFieldDefinitionForClaim(claimUri);
            fieldDefinitions.add(fieldDefinition);
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("Generated " + fieldDefinitions.size() + " field definitions for user claims for tenant: " +
                    tenantDomain);
        }

        return fieldDefinitions;
    }

    /**
     * Get the list of allowed user claim URIs dynamically at runtime.
     * Fetches all local claims from the claim management service.
     *
     * @param tenantDomain Tenant domain.
     * @return List of allowed user claim URIs.
     * @throws RuleMetadataException If an error occurs while fetching claims.
     */
    private List<String> getAllowedUserClaimUris(String tenantDomain) throws RuleMetadataException {

        ClaimMetadataManagementService claimService = RuleMetadataServiceDataHolder.getInstance()
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

            List<String> claimUris = localClaims.stream()
                    .map(LocalClaim::getClaimURI)
                    .collect(Collectors.toList());

            if (LOG.isDebugEnabled()) {
                LOG.debug("Retrieved " + claimUris.size() + " local claims for tenant: " + tenantDomain);
            }

            return claimUris;
        } catch (ClaimMetadataException e) {
            throw new RuleMetadataException("RULE_METADATA_ERROR_50001",
                    "Error fetching local claims for tenant: " + tenantDomain,
                    "An error occurred while retrieving local claims from the claim metadata service.", e);
        }
    }

    /**
     * Create a field definition for a user claim URI.
     *
     * @param claimUri User claim URI
     * @return FieldDefinition for the claim
     */
    private FieldDefinition createFieldDefinitionForClaim(String claimUri) {

        Field field = new Field(claimUri, deriveDisplayNameFromClaimUri(claimUri));
        List<Operator> operators = getOperatorsForClaim();
        Value valueMeta = new InputValue(Value.ValueType.STRING);

        return new FieldDefinition(field, operators, valueMeta);
    }

    /**
     * Derive a display name from the claim URI.
     * Extracts the last segment of the URI and formats it.
     * Example: "http://wso2.org/claims/emailaddress" -> "Email Address"
     *
     * @param claimUri User claim URI
     * @return Display name
     */
    private String deriveDisplayNameFromClaimUri(String claimUri) {

        if (claimUri == null || claimUri.isEmpty()) {
            return claimUri;
        }

        // Extract the last segment after the last '/'
        String lastSegment = claimUri;
        int lastSlashIndex = claimUri.lastIndexOf('/');
        if (lastSlashIndex >= 0 && lastSlashIndex < claimUri.length() - 1) {
            lastSegment = claimUri.substring(lastSlashIndex + 1);
        }

        // Convert camelCase or lowercase to Title Case with spaces.
        // Example: "emailaddress" -> "Email Address"
        StringBuilder displayName = new StringBuilder();
        boolean capitalizeNext = true;

        for (int i = 0; i < lastSegment.length(); i++) {
            char c = lastSegment.charAt(i);

            if (Character.isUpperCase(c) && i > 0) {
                // Add space before capital letters in camelCase.
                displayName.append(' ');
                displayName.append(c);
                capitalizeNext = false;
            } else if (capitalizeNext) {
                displayName.append(Character.toUpperCase(c));
                capitalizeNext = false;
            } else {
                displayName.append(Character.toLowerCase(c));
            }

            if (c == '_' || c == '-') {
                // Replace underscore or hyphen with space and capitalize next.
                displayName.setCharAt(displayName.length() - 1, ' ');
                capitalizeNext = true;
            }
        }

        return displayName.toString().trim();
    }

    /**
     * Get the list of operators applicable for claim fields.
     * Currently returns equals and notEquals operators.
     *
     * @return List of operators
     */
    private List<Operator> getOperatorsForClaim() {

        List<Operator> operators = new ArrayList<>();

        // Get operators from the operator config.
        Operator equalsOperator = RuleMetadataConfigFactory.getOperatorConfig()
                .getOperatorsMap().get("equals");
        Operator notEqualsOperator = RuleMetadataConfigFactory.getOperatorConfig()
                .getOperatorsMap().get("notEquals");

        if (equalsOperator != null) {
            operators.add(equalsOperator);
        }

        if (notEqualsOperator != null) {
            operators.add(notEqualsOperator);
        }

        return operators;
    }
}

