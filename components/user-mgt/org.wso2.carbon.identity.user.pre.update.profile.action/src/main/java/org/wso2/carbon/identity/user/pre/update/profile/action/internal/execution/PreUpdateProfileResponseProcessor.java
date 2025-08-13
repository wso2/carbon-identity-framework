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

package org.wso2.carbon.identity.user.pre.update.profile.action.internal.execution;

import org.wso2.carbon.identity.action.execution.api.exception.ActionExecutionResponseProcessorException;
import org.wso2.carbon.identity.action.execution.api.model.ActionExecutionResponseContext;
import org.wso2.carbon.identity.action.execution.api.model.ActionExecutionStatus;
import org.wso2.carbon.identity.action.execution.api.model.ActionInvocationSuccessResponse;
import org.wso2.carbon.identity.action.execution.api.model.ActionType;
import org.wso2.carbon.identity.action.execution.api.model.FlowContext;
import org.wso2.carbon.identity.action.execution.api.model.Operation;
import org.wso2.carbon.identity.action.execution.api.model.PerformableOperation;
import org.wso2.carbon.identity.action.execution.api.model.Success;
import org.wso2.carbon.identity.action.execution.api.model.SuccessStatus;
import org.wso2.carbon.identity.action.execution.api.model.UserClaim;
import org.wso2.carbon.identity.action.execution.api.service.ActionExecutionResponseProcessor;
import org.wso2.carbon.identity.application.authentication.framework.util.FrameworkUtils;
import org.wso2.carbon.identity.claim.metadata.mgt.ClaimMetadataManagementService;
import org.wso2.carbon.identity.claim.metadata.mgt.exception.ClaimMetadataException;
import org.wso2.carbon.identity.claim.metadata.mgt.model.LocalClaim;
import org.wso2.carbon.identity.claim.metadata.mgt.util.ClaimConstants;
import org.wso2.carbon.identity.core.context.IdentityContext;
import org.wso2.carbon.identity.user.pre.update.profile.action.internal.component.PreUpdateProfileActionServiceComponentHolder;
import org.wso2.carbon.identity.user.pre.update.profile.action.internal.model.PreUpdateProfileEvent;
import org.wso2.carbon.user.api.UserRealm;
import org.wso2.carbon.user.api.UserStoreManager;
import org.wso2.carbon.user.core.UniqueIDUserStoreManager;
import org.wso2.carbon.user.core.UserCoreConstants;
import org.wso2.carbon.user.core.service.RealmService;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Pre Update Profile Action Response Processor.
 */
public class PreUpdateProfileResponseProcessor implements ActionExecutionResponseProcessor {

    private static final String ID_CLAIM_URI = "http://wso2.org/claims/userid";
    private static final String CREATED_CLAIM_URI = "http://wso2.org/claims/created";
    private static final String FAMILY_NAME_CLAIM_URI = "http://wso2.org/claims/lastname";
    private static final String GROUP_CLAIM_URI = "http://wso2.org/claims/groups";
    private static final String ROLE_CLAIM_URI = "http://wso2.org/claims/roles";
    private static final String IDENTITY_CLAIM_URI_PREFIX = "http://wso2.org/claims/identity/";
    private static final String URI = "uri";
    private static final String VALUE = "value";
    private static final String USER_CLAIMS_TO_BE_ADDED = "userClaimsToBeAdded";
    private static final String USER_CLAIMS_TO_BE_MODIFIED = "userClaimsToBeModified";
    private static final String USER_CLAIMS_TO_BE_REMOVED = "userClaimsToBeRemoved";
    private static final String MULTI_VALUED_CLAIMS_TO_BE_ADDED = "multiValuedClaimsToBeAdded";
    private static final String MULTI_VALUED_CLAIMS_TO_BE_REMOVED = "multiValuedClaimsToBeRemoved";

    private UniqueIDUserStoreManager userStoreManager;

    @Override
    public ActionType getSupportedActionType() {

        return ActionType.PRE_UPDATE_PROFILE;
    }

    @Override
    public ActionExecutionStatus<Success> processSuccessResponse(FlowContext flowContext,
                                     ActionExecutionResponseContext<ActionInvocationSuccessResponse> responseContext)
            throws ActionExecutionResponseProcessorException {

        List<PerformableOperation> operationsToPerform = responseContext.getActionInvocationResponse().getOperations();
        userStoreManager = getUserStoreManager();

        Map<String, String> userClaimsToBeAdded = new HashMap<>();
        Map<String, String> userClaimsToBeModified = new HashMap<>();
        Map<String, String> userClaimsToBeRemoved = new HashMap<>();
        Map<String, List<String>> simpleMultiValuedClaimsToBeAdded = new HashMap<>();
        Map<String, List<String>> simpleMultiValuedClaimsToBeRemoved = new HashMap<>();

        if (operationsToPerform != null) {
            for (PerformableOperation operation : operationsToPerform) {
                switch (operation.getOp()) {
                    case ADD:
                        populateAddOperationResult(operation, responseContext, userClaimsToBeAdded,
                            userClaimsToBeModified, simpleMultiValuedClaimsToBeAdded);
                        break;
                    case REPLACE:
                        populateModifyOperationResult(operation, responseContext, userClaimsToBeModified,
                                simpleMultiValuedClaimsToBeRemoved, simpleMultiValuedClaimsToBeAdded);
                        break;
                    case REMOVE:
                        populateRemoveOperationResult(operation, responseContext, userClaimsToBeModified,
                                userClaimsToBeRemoved, simpleMultiValuedClaimsToBeRemoved);
                        break;
                    default:
                        break;
                }
            }
        }
        flowContext.add(USER_CLAIMS_TO_BE_ADDED, userClaimsToBeAdded);
        flowContext.add(USER_CLAIMS_TO_BE_MODIFIED, userClaimsToBeModified);
        flowContext.add(USER_CLAIMS_TO_BE_REMOVED, userClaimsToBeRemoved);
        flowContext.add(MULTI_VALUED_CLAIMS_TO_BE_ADDED, simpleMultiValuedClaimsToBeAdded);
        flowContext.add(MULTI_VALUED_CLAIMS_TO_BE_REMOVED, simpleMultiValuedClaimsToBeRemoved);
        return new SuccessStatus.Builder().setResponseContext(flowContext.getContextData()).build();
    }

    private void populateAddOperationResult(PerformableOperation operation,
                                            ActionExecutionResponseContext<ActionInvocationSuccessResponse>
                                                    responseContext, Map<String, String> userClaimsToBeAdded,
                                            Map<String, String> userClaimsToBeModified,
                                            Map<String, List<String>> simpleMultiValuedClaimsToBeAdded)
            throws ActionExecutionResponseProcessorException {

            String userId = responseContext.getActionEvent().getUser().getId();
            PreUpdateProfileEvent.FlowInitiatorType initiatorType =
                    ((PreUpdateProfileEvent) responseContext.getActionEvent()).getInitiatorType();

            LinkedHashMap<?, ?> valueMap = Optional.ofNullable(operation.getValue())
                    .filter(LinkedHashMap.class::isInstance)
                    .map(LinkedHashMap.class::cast)
                    .orElseThrow(() -> new ActionExecutionResponseProcessorException("Operation value is null or " +
                            "not a map"));

            String claimUri = Optional.ofNullable(valueMap.get(URI))
                    .filter(value -> value instanceof String)
                    .map(value -> (String) value)
                    .orElseThrow(() -> new ActionExecutionResponseProcessorException("Missing or wrong format for " +
                            "claim uri in operation"));

            String claimValue = Optional.ofNullable(valueMap.get(VALUE))
                    .filter(value -> value instanceof String)
                    .map(value -> (String) value)
                    .orElseThrow(() ->
                            new ActionExecutionResponseProcessorException("Missing or wrong format for " +
                                    "claim value in operation"));

            Optional<LocalClaim> localClaim = isLocalClaim(claimUri);
            validateGroupAndRoleClaims(claimUri);
            validateImmutableClaims(claimUri);
            validateFlowInitiatorClaims(claimUri, localClaim);
            if (!isMultiValuedClaim(localClaim)) {
                Map<String, String> userClaimValues = getUserClaimValues(userId, claimUri);
                // Validate if the claim value already exists for the user
                if (operation.getOp() == Operation.ADD && userClaimValues.get(claimUri) != null &&
                        !claimValue.trim().isEmpty()) {
                    userClaimsToBeModified.put(claimUri, claimValue.trim());
                } else {
                    userClaimsToBeAdded.put(claimUri, claimValue.trim());
                }
            } else {
                populateMultivaluedClaimsForAddOperation(localClaim, claimValue, userId, userClaimsToBeModified,
                        simpleMultiValuedClaimsToBeAdded, initiatorType);
            }
    }

    private void populateModifyOperationResult(PerformableOperation operation,
                                               ActionExecutionResponseContext<ActionInvocationSuccessResponse>
                                                       responseContext, Map<String, String> userClaimsToBeModified,
                                               Map<String, List<String>> simpleMultiValuedClaimsToBeRemoved,
                                               Map<String, List<String>> simpleMultiValuedClaimsToBeAdded)
            throws ActionExecutionResponseProcessorException {

        String path = operation.getPath();
        List<UserClaim> claims = responseContext.getActionEvent().getUser().getClaims();
        String userId = responseContext.getActionEvent().getUser().getId();
        PreUpdateProfileEvent.FlowInitiatorType initiatorType =
                ((PreUpdateProfileEvent) responseContext.getActionEvent()).getInitiatorType();
        String separator = FrameworkUtils.getMultiAttributeSeparator();
        validateReplaceAndRemovePaths(claims, path);
        int claimIndex = getClaimIndex(path);
        int multiValueIndex = getMultiValueindex(path);
        UserClaim claim = claims.get(claimIndex);
        if (multiValueIndex == -1) {
            String claimValue = Optional.ofNullable(operation.getValue())
                    .filter(value -> value instanceof String)
                    .map(value -> (String) value)
                    .orElseThrow(() ->
                            new ActionExecutionResponseProcessorException("Missing or wrong format for " +
                                    "claim value in operation"));
            userClaimsToBeModified.put(claim.getUri(), claimValue.trim());
        } else {
            populateMultiValuedClaimsForReplaceOperation(operation, initiatorType, userClaimsToBeModified,
                    simpleMultiValuedClaimsToBeAdded, simpleMultiValuedClaimsToBeRemoved, userId, claim, separator);
        }
    }

    private void populateRemoveOperationResult(PerformableOperation operation,
                                                ActionExecutionResponseContext<ActionInvocationSuccessResponse>
                                                        responseContext, Map<String, String> userClaimsToBeModified,
                                                Map<String, String> userClaimsToBeRemoved,
                                                Map<String, List<String>> simpleMultiValuedClaimsToBeRemoved) throws
            ActionExecutionResponseProcessorException {

        String path = operation.getPath();
        List<UserClaim> claims = responseContext.getActionEvent().getUser().getClaims();
        PreUpdateProfileEvent.FlowInitiatorType initiatorType =
                ((PreUpdateProfileEvent) responseContext.getActionEvent()).getInitiatorType();
        validateReplaceAndRemovePaths(claims, path);
        int claimIndex = getClaimIndex(path);
        int multiValueIndex = getMultiValueindex(path);
        UserClaim claim = claims.get(claimIndex);
        if (multiValueIndex == -1) {
            userClaimsToBeModified.put(claim.getUri(), "");
        } else {
            populateMultiValuedClaimsForRemoveOperation(initiatorType, claim, operation, userClaimsToBeRemoved,
                    multiValueIndex, simpleMultiValuedClaimsToBeRemoved);
        }
    }

    private void populateMultivaluedClaimsForAddOperation(Optional<LocalClaim> localClaim, String claimValue,
                                                          String userId,
                                                          Map<String, String> userClaimsToBeModified,
                                                          Map<String, List<String>> simpleMultiValuedClaimsToBeAdded,
                                                          PreUpdateProfileEvent.FlowInitiatorType initiatorType)
            throws ActionExecutionResponseProcessorException {

        String claimUri = localClaim.get().getClaimURI();
        String separator = FrameworkUtils.getMultiAttributeSeparator();
        Map<String, String> userClaimValues = getUserClaimValues(userId, claimUri);
        List<String> filteredValues = getFilteredModifyingClaimValues(userClaimValues, claimUri, claimValue,
                separator);
        if (initiatorType == PreUpdateProfileEvent.FlowInitiatorType.ADMIN ||
                initiatorType == PreUpdateProfileEvent.FlowInitiatorType.APPLICATION) {
            simpleMultiValuedClaimsToBeAdded.put(claimUri, filteredValues);
        } else if (initiatorType == PreUpdateProfileEvent.FlowInitiatorType.USER) {
            String addingClaimValue = (userClaimValues.get(claimUri) == null) ?
                    String.join(separator, filteredValues) :
                    userClaimValues.get(claimUri) + separator + String.join(separator, filteredValues);
            userClaimsToBeModified.put(claimUri, addingClaimValue);
        }
    }

    private void populateMultiValuedClaimsForReplaceOperation(PerformableOperation operation,
                                                              PreUpdateProfileEvent.FlowInitiatorType initiatorType,
                                                              Map<String, String> userClaimsToBeModified,
                                                              Map<String, List<String>>
                                                                      simpleMultiValuedClaimsToBeAdded,
                                                              Map<String, List<String>>
                                                                      simpleMultiValuedClaimsToBeRemoved,
                                                              String userId, UserClaim claim, String separator)
            throws ActionExecutionResponseProcessorException {

        String claimValue = Optional.ofNullable(operation.getValue())
                .filter(value -> value instanceof String)
                .map(value -> (String) value)
                .orElseThrow(() ->
                        new ActionExecutionResponseProcessorException("Missing or wrong format for " +
                                "claim value in operation"));
        if (initiatorType == PreUpdateProfileEvent.FlowInitiatorType.ADMIN ||
                initiatorType == PreUpdateProfileEvent.FlowInitiatorType.APPLICATION) {
            Map<String, String> userClaimValues = getUserClaimValues(userId, claim.getUri());
            List<String> filteredClaims = getFilteredModifyingClaimValues(userClaimValues, claim.getUri(),
                    claimValue, FrameworkUtils.getMultiAttributeSeparator());
            int multiValueIndex = getMultiValueindex(operation.getPath());
            if (!filteredClaims.isEmpty()) {
                simpleMultiValuedClaimsToBeRemoved.put(claim.getUri(),
                        Arrays.asList(((String[]) claim.getValue())[multiValueIndex]));
                simpleMultiValuedClaimsToBeAdded.put(claim.getUri(), Arrays.asList(filteredClaims.get(0).trim()));
            }
        } else if (initiatorType == PreUpdateProfileEvent.FlowInitiatorType.USER) {
            Map<String, String> userClaimValues = getUserClaimValues(userId, claim.getUri());
            List<String> filteredClaims = getFilteredModifyingClaimValues(userClaimValues, claim.getUri(),
                    claimValue, FrameworkUtils.getMultiAttributeSeparator());
            String[] claimValues = (String[]) claim.getValue();
            int multiValueIndex = getMultiValueindex(operation.getPath());
            List<String> combined = new ArrayList<>();
            IntStream.range(0, claimValues.length)
                    .filter(i -> i != multiValueIndex)
                    .forEach(i -> combined.add(claimValues[i]));
            combined.addAll(filteredClaims);
            String modifyingClaimValue = String.join(separator, combined);
            userClaimsToBeModified.put(claim.getUri(), modifyingClaimValue);
        }
    }

    private void populateMultiValuedClaimsForRemoveOperation(PreUpdateProfileEvent.FlowInitiatorType initiatorType,
                                                             UserClaim claim, PerformableOperation operation,
                                                             Map<String, String> userClaimsToBeRemoved,
                                                             int multiValueIndex, Map<String, List<String>>
                                                                     simpleMultiValuedClaimsToBeRemoved) {

        if (initiatorType == PreUpdateProfileEvent.FlowInitiatorType.ADMIN ||
                initiatorType == PreUpdateProfileEvent.FlowInitiatorType.APPLICATION) {
            simpleMultiValuedClaimsToBeRemoved.put(claim.getUri(),
                    Arrays.asList(((String[]) claim.getValue())[multiValueIndex]));
        } else if (initiatorType == PreUpdateProfileEvent.FlowInitiatorType.USER) {
            userClaimsToBeRemoved.put(claim.getUri(), ((String[]) claim.getValue())[multiValueIndex]);
        }
    }

    private Optional<LocalClaim> isLocalClaim(String claimUri) throws ActionExecutionResponseProcessorException {

        ClaimMetadataManagementService claimMetadataManagementService =
                PreUpdateProfileActionServiceComponentHolder.getInstance().getClaimManagementService();
        String tenantDomain = IdentityContext.getThreadLocalIdentityContext().getTenantDomain();
        try {
            Optional<LocalClaim> localClaim = claimMetadataManagementService.getLocalClaim(claimUri, tenantDomain);
            if (!localClaim.isPresent()) {
                throw new ActionExecutionResponseProcessorException("Local claim not found for claim uri: " + claimUri);
            }
            return localClaim;
        } catch (ClaimMetadataException e) {
            throw new ActionExecutionResponseProcessorException("Error while retrieving localClaim for claim uri: "
                    + claimUri, e);
        }
    }

    private boolean isMultiValuedClaim(Optional<LocalClaim> localClaim) {

        return localClaim.isPresent() &&
                Boolean.parseBoolean(localClaim.get().getClaimProperty(ClaimConstants.MULTI_VALUED_PROPERTY));
    }

    public int getClaimIndex(String path) {

        String[] parts = path.split("/");
        return Integer.parseInt(parts[3]);
    }

    public int getMultiValueindex(String path) {

        String[] parts = path.split("/");
        return parts.length == 6 ? Integer.parseInt(parts[5]) : -1;
    }


    private UniqueIDUserStoreManager getUserStoreManager() throws ActionExecutionResponseProcessorException {

        String tenantDomain = IdentityContext.getThreadLocalIdentityContext().getTenantDomain();
        RealmService realmService = PreUpdateProfileActionServiceComponentHolder.getInstance().getRealmService();

        if (realmService == null) {
            throw new ActionExecutionResponseProcessorException("Realm service is unavailable.");
        }

        try {
            int tenantId = realmService.getTenantManager().getTenantId(tenantDomain);
            UserRealm userRealm = realmService.getTenantUserRealm(tenantId);

            if (userRealm == null) {
                throw new ActionExecutionResponseProcessorException(
                        "User realm is not available for tenant: " + tenantDomain);
            }

            UserStoreManager userStoreManager = userRealm.getUserStoreManager();
            if (!(userStoreManager instanceof UniqueIDUserStoreManager)) {
                throw new ActionExecutionResponseProcessorException(
                        "User store manager is not an instance of UniqueIDUserStoreManager for tenant: " +
                                tenantDomain);
            }

            return (UniqueIDUserStoreManager) userStoreManager;
        } catch (org.wso2.carbon.user.api.UserStoreException e) {
            throw new ActionExecutionResponseProcessorException(
                    "Error while loading user store manager for tenant: " + tenantDomain, e);
        }
    }
    
    private Map<String, String> getUserClaimValues(String userId, String claimUri) throws
            ActionExecutionResponseProcessorException {

        try {
            return userStoreManager.getUserClaimValuesWithID(userId, new String[]{claimUri},
                    UserCoreConstants.DEFAULT_PROFILE);
        } catch (org.wso2.carbon.user.core.UserStoreException e) {
            throw new ActionExecutionResponseProcessorException("Failed to retrieve user claims from user store.", e);
        }
    }

    private List<String> getFilteredModifyingClaimValues(Map<String, String> userClaimValues, String claimUri,
                                                         String claimValue, String separator) {

        List<String> userValues = Optional.ofNullable(userClaimValues.get(claimUri))
                .map(value -> Arrays.asList(value.split(separator)))
                .orElse(Collections.emptyList());
        String[] claimValuesList = filterDuplicatedValues(claimValue, separator);

        return Arrays.stream(claimValuesList)
                .filter(value -> !userValues.contains(value))
                .collect(Collectors.toList());
    }

    private String[] filterDuplicatedValues(String claimValue, String separator) {

        return Arrays.stream(claimValue.split(separator))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .distinct()
                .toArray(String[]::new);
    }

    private void validateReplaceAndRemovePaths(List<UserClaim> claims, String path)
            throws ActionExecutionResponseProcessorException {

        Pattern pattern = Pattern.compile("^/user/claims/(\\d+)(/value/(\\d+))?");
        Matcher matcher = pattern.matcher(path);

        if (matcher.find()) {
            int index = Integer.parseInt(matcher.group(1));
            if (index < 0 || index >= claims.size()) {
                throw new ActionExecutionResponseProcessorException("Invalid index for claims: " + index);
            }
            UserClaim claim = claims.get(index);
            if (matcher.group(2) != null) {
                int valueIndex = Integer.parseInt(matcher.group(3));
                String[] values;
                if (claim.getValue() instanceof String []) {
                    values = (String[]) claim.getValue();
                } else {
                    throw new ActionExecutionResponseProcessorException("Invalid path for multivalued claim: " + path);
                }
                if (valueIndex < 0 || valueIndex >= values.length) {
                    throw new ActionExecutionResponseProcessorException("Invalid index for multivalued claim: "
                            + valueIndex);
                }
            }
        } else {
            throw new ActionExecutionResponseProcessorException("Invalid path format: " + path);
        }
    }

    private void validateImmutableClaims(String claimUri) throws ActionExecutionResponseProcessorException {

        if (claimUri.equals(ID_CLAIM_URI) || claimUri.equals(CREATED_CLAIM_URI) ||
                claimUri.equals(FAMILY_NAME_CLAIM_URI) || claimUri.contains(IDENTITY_CLAIM_URI_PREFIX)) {
            throw new ActionExecutionResponseProcessorException("Immutable claims cannot be added or modified: " +
                    claimUri);
        }
    }

    private void validateFlowInitiatorClaims(String claimUri, Optional<LocalClaim> localClaim)
            throws ActionExecutionResponseProcessorException {

        if (localClaim.get().getFlowInitiator()) {
            throw new ActionExecutionResponseProcessorException(claimUri + " is not allowed to be added.");
        }
    }

    private void validateGroupAndRoleClaims(String claimUri) 
            throws ActionExecutionResponseProcessorException {

        if (claimUri.equals(GROUP_CLAIM_URI) || claimUri.equals(ROLE_CLAIM_URI)) {
            throw new ActionExecutionResponseProcessorException("Groups/Roles are not allowed to be added: "
                    + claimUri);
        }
    }
}
