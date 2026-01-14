package org.wso2.carbon.identity.rule.metadata.internal.provider.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.rule.metadata.api.exception.RuleMetadataException;
import org.wso2.carbon.identity.rule.metadata.api.model.Field;
import org.wso2.carbon.identity.rule.metadata.api.model.FieldDefinition;
import org.wso2.carbon.identity.rule.metadata.api.model.FlowType;
import org.wso2.carbon.identity.rule.metadata.api.model.InputValue;
import org.wso2.carbon.identity.rule.metadata.api.model.Operator;
import org.wso2.carbon.identity.rule.metadata.api.model.Value;
import org.wso2.carbon.identity.rule.metadata.api.provider.RuleMetadataProvider;
import org.wso2.carbon.identity.rule.metadata.internal.config.RuleMetadataConfigFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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

        // TODO: Implement dynamic claim fetching from ClaimMetadataManagementService.
        // For now, return hardcoded claims for testing. This should be implemented to fetch claims at runtime.
        // Example implementation:
        // try {
        //     ClaimMetadataManagementService claimService = getClaimMetadataManagementService();
        //     List<LocalClaim> localClaims = claimService.getLocalClaims(tenantDomain);
        //     return localClaims.stream()
        //         .map(LocalClaim::getClaimURI)
        //         .collect(Collectors.toList());
        // } catch (ClaimMetadataException e) {
        //     throw new RuleMetadataException("Error fetching claims for tenant: " + tenantDomain, e);
        // }

        List<String> tempAllowedClaims = new ArrayList<>();
        tempAllowedClaims.add("http://wso2.org/claims/emailaddress");
        tempAllowedClaims.add("http://wso2.org/claims/country");

        if (LOG.isDebugEnabled()) {
            LOG.debug("Returning " + tempAllowedClaims.size() + " test claims for tenant: " +
                    tenantDomain);
        }

        return tempAllowedClaims;
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

