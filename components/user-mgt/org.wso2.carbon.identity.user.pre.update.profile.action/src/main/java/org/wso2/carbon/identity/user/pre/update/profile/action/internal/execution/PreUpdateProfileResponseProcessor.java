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
import org.wso2.carbon.identity.action.execution.api.service.ActionExecutionResponseProcessor;
import org.wso2.carbon.identity.action.execution.internal.component.ActionExecutionServiceComponentHolder;
import org.wso2.carbon.identity.application.authentication.framework.util.FrameworkUtils;
import org.wso2.carbon.identity.claim.metadata.mgt.ClaimMetadataManagementService;
import org.wso2.carbon.identity.claim.metadata.mgt.exception.ClaimMetadataException;
import org.wso2.carbon.identity.claim.metadata.mgt.model.Claim;
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
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Pre Update Profile Action Response Processor.
 */
public class PreUpdateProfileResponseProcessor implements ActionExecutionResponseProcessor {

    private static final String GROUP_CLAIM_URI = "http://wso2.org/claims/groups";
    private static final String ROLE_CLAIM_URI = "http://wso2.org/claims/roles";
    private static final String SCIM_SCHEMA_URI_PREFIX = "urn:ietf:params:scim:schemas";
    private static final String URI = "uri";
    private static final String VALUE = "value";
    private static final String USER_CLAIMS_TO_BE_ADDED = "userClaimsToBeAdded";
    private static final String USER_CLAIMS_TO_BE_MODIFIED = "userClaimsToBeModified";
    private static final String USER_CLAIMS_TO_BE_REMOVED = "userClaimsToBeRemoved";
    private static final String MULTI_VALUED_CLAIMS_TO_BE_ADDED = "multiValuedClaimsToBeAdded";
    private static final String MULTI_VALUED_CLAIMS_TO_BE_REMOVED = "multiValuedClaimsToBeRemoved";
    private static final String USER_CLAIMS_FILTER_PATH_PREFIX = "/user/claims[";

    @Override
    public ActionType getSupportedActionType() {

        return ActionType.PRE_UPDATE_PROFILE;
    }

    @Override
    public ActionExecutionStatus<Success> processSuccessResponse(
            FlowContext flowContext, ActionExecutionResponseContext<ActionInvocationSuccessResponse> responseContext)
            throws ActionExecutionResponseProcessorException {

        List<PerformableOperation> operationsToPerform = responseContext.getActionInvocationResponse().getOperations();
        Map<String, String> userClaimsToBeAdded = new HashMap<>();
        Map<String, String> userClaimsToBeModified = new HashMap<>();
        Map<String, String> userClaimsToBeRemoved = new HashMap<>();
        Map<String, List<String>> simpleMultiValuedClaimsToBeAdded = new HashMap<>();
        Map<String, List<String>> simpleMultiValuedClaimsToBeRemoved = new HashMap<>();

        if (operationsToPerform != null && !operationsToPerform.isEmpty()) {
            UniqueIDUserStoreManager userStoreManager = getUserStoreManager();
            for (PerformableOperation operation : operationsToPerform) {
                switch (operation.getOp()) {
                    case ADD:
                        populateAddOperationResult(operation, responseContext, userClaimsToBeAdded,
                                userClaimsToBeModified, simpleMultiValuedClaimsToBeAdded, userStoreManager);
                        break;
                    case REPLACE:
                        populateModifyOperationResult(operation, responseContext, userClaimsToBeModified,
                                simpleMultiValuedClaimsToBeRemoved, simpleMultiValuedClaimsToBeAdded, userStoreManager);
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
                                                    responseContext,
                                            Map<String, String> userClaimsToBeAdded,
                                            Map<String, String> userClaimsToBeModified,
                                            Map<String, List<String>> simpleMultiValuedClaimsToBeAdded,
                                            UniqueIDUserStoreManager userStoreManager)
            throws ActionExecutionResponseProcessorException {

        String userId = responseContext.getActionEvent().getUser().getId();
        PreUpdateProfileEvent.FlowInitiatorType initiatorType = ((PreUpdateProfileEvent) responseContext
                .getActionEvent()).getInitiatorType();

        String claimUri;
        String path = operation.getPath();

        // Determine claim URI: from path (path-based format) or from value map.
        if (path != null && isClaimPathFormat(path)) {
            claimUri = getClaimUriFromPath(path);
        } else if (operation.getValue() instanceof LinkedHashMap) {
            LinkedHashMap<?, ?> valueMap = (LinkedHashMap<?, ?>) operation.getValue();
            claimUri = Optional.ofNullable(valueMap.get(URI))
                    .filter(value -> value instanceof String)
                    .map(value -> (String) value)
                    .orElseThrow(() -> new ActionExecutionResponseProcessorException("Missing or wrong format for " +
                            "claim uri in operation"));
        } else {
            throw new ActionExecutionResponseProcessorException("Operation path does not contain claim URI and " +
                    "operation value is not a map");
        }

        Optional<LocalClaim> localClaim = isLocalClaim(claimUri);
        validateGroupAndRoleClaims(claimUri);
        validateFlowInitiatorClaims(claimUri, localClaim);
        validateSCIMLevelAttributes(claimUri, operation.getOp(), operation.getValue());

        if (!isMultiValuedClaim(localClaim)) {
            // Extract single claim value from String directly or from value map.
            String claimValue;
            if (operation.getValue() instanceof String) {
                claimValue = (String) operation.getValue();
            } else if (operation.getValue() instanceof LinkedHashMap) {
                LinkedHashMap<?, ?> valueMap = (LinkedHashMap<?, ?>) operation.getValue();
                claimValue = Optional.ofNullable(valueMap.get(VALUE))
                        .filter(value -> value instanceof String)
                        .map(value -> (String) value)
                        .orElseThrow(
                                () -> new ActionExecutionResponseProcessorException("Missing or wrong format for " +
                                        "single valued claim in operation"));
            } else {
                throw new ActionExecutionResponseProcessorException("Missing or wrong format for " +
                        "claim value in add operation");
            }

            Map<String, String> userClaimValues = getUserClaimValues(userId, claimUri, userStoreManager);
            // Validate if the claim value already exists for the user
            if (operation.getOp() == Operation.ADD && userClaimValues.get(claimUri) != null &&
                    !claimValue.trim().isEmpty()) {
                userClaimsToBeModified.put(claimUri, claimValue.trim());
            } else {
                userClaimsToBeAdded.put(claimUri, claimValue.trim());
            }
        } else {
            // Extract multi-valued claim value from List directly or from value map.
            List<String> claimValue;
            if (operation.getValue() instanceof LinkedHashMap) {
                LinkedHashMap<?, ?> valueMap = (LinkedHashMap<?, ?>) operation.getValue();
                claimValue = Optional.ofNullable(valueMap.get(VALUE))
                        .filter(value -> value instanceof List)
                        .map(value -> (List<?>) value)
                        .filter(list -> list.stream().allMatch(e -> e instanceof String))
                        .map(list -> (List<String>) list)
                        .orElseThrow(() -> new ActionExecutionResponseProcessorException(
                                "Missing or wrong format for multi valued claim in operation"));
            } else if (operation.getValue() instanceof List) {
                List<?> rawList = (List<?>) operation.getValue();
                if (!rawList.stream().allMatch(e -> e instanceof String)) {
                    throw new ActionExecutionResponseProcessorException(
                            "Missing or wrong format for multi valued claim in operation");
                }
                List<String> typedList = (List<String>) operation.getValue();
                claimValue = typedList;
            } else {
                throw new ActionExecutionResponseProcessorException(
                        "Missing or wrong format for multi valued claim in add operation");
            }
            populateMultivaluedClaimsForAddOperation(localClaim, claimValue, userId, userClaimsToBeModified,
                    simpleMultiValuedClaimsToBeAdded, initiatorType, userStoreManager);
        }
    }

    private void populateModifyOperationResult(PerformableOperation operation,
                                               ActionExecutionResponseContext<ActionInvocationSuccessResponse>
                                                       responseContext,
                                               Map<String, String> userClaimsToBeModified,
                                               Map<String, List<String>> simpleMultiValuedClaimsToBeRemoved,
                                               Map<String, List<String>> simpleMultiValuedClaimsToBeAdded,
                                               UniqueIDUserStoreManager userStoreManager)
            throws ActionExecutionResponseProcessorException {

        String userId = responseContext.getActionEvent().getUser().getId();
        PreUpdateProfileEvent.FlowInitiatorType initiatorType = ((PreUpdateProfileEvent) responseContext
                .getActionEvent()).getInitiatorType();

        String path = operation.getPath();
        String claimUri = getClaimUriFromPath(path);

        Optional<LocalClaim> localClaim = isLocalClaim(claimUri);
        validateGroupAndRoleClaims(claimUri);
        validateFlowInitiatorClaims(claimUri, localClaim);
        validateSCIMLevelAttributes(claimUri, operation.getOp(), operation.getValue());
        String separator = FrameworkUtils.getMultiAttributeSeparator();

        if (!isMultiValuedClaim(localClaim)) {
            // For single valued claims, value is directly the new value string.
            String claimValue;
            if (operation.getValue() instanceof String) {
                claimValue = (String) operation.getValue();
            } else if (operation.getValue() instanceof LinkedHashMap) {
                LinkedHashMap<?, ?> valueMap = (LinkedHashMap<?, ?>) operation.getValue();
                claimValue = Optional.ofNullable(valueMap.get(VALUE))
                        .filter(value -> value instanceof String)
                        .map(value -> (String) value)
                        .orElseThrow(
                                () -> new ActionExecutionResponseProcessorException("Missing or wrong format for " +
                                        "claim value in operation"));
            } else {
                throw new ActionExecutionResponseProcessorException("Missing or wrong format for " +
                        "claim value in replace operation");
            }
            userClaimsToBeModified.put(claimUri, claimValue.trim());
        } else {
            populateMultiValuedClaimsForReplaceOperation(operation, initiatorType, userClaimsToBeModified,
                    simpleMultiValuedClaimsToBeAdded, simpleMultiValuedClaimsToBeRemoved, userId, claimUri, separator,
                    userStoreManager);
        }
    }

    private void populateRemoveOperationResult(PerformableOperation operation,
                                               ActionExecutionResponseContext<ActionInvocationSuccessResponse>
                                                       responseContext,
                                               Map<String, String> userClaimsToBeModified,
                                               Map<String, String> userClaimsToBeRemoved,
                                               Map<String, List<String>> simpleMultiValuedClaimsToBeRemoved)
            throws ActionExecutionResponseProcessorException {

        String path = operation.getPath();
        PreUpdateProfileEvent.FlowInitiatorType initiatorType = ((PreUpdateProfileEvent) responseContext
                .getActionEvent()).getInitiatorType();

        // Extract the claim URI and optionally the specific value to remove for
        // multivalued claims.
        String claimUri = getClaimUriFromPath(path);

        Optional<LocalClaim> localClaim = isLocalClaim(claimUri);
        validateGroupAndRoleClaims(claimUri);
        validateSCIMLevelAttributes(claimUri, operation.getOp(), operation.getValue());

        if (!isMultiValuedClaim(localClaim)) {
            userClaimsToBeRemoved.put(claimUri, "");
            userClaimsToBeModified.remove(claimUri);
        } else {
            if (operation.getValue() != null) {
                throw new ActionExecutionResponseProcessorException(
                        "Remove specific value from a multivalued claim is not supported.");
            }
            populateMultiValuedClaimsForRemoveOperation(initiatorType, claimUri, userClaimsToBeRemoved,
                    simpleMultiValuedClaimsToBeRemoved);
        }
    }

    private void populateMultivaluedClaimsForAddOperation(Optional<LocalClaim> localClaim, List<String> claimValue,
                                                          String userId,
                                                          Map<String, String> userClaimsToBeModified,
                                                          Map<String, List<String>> simpleMultiValuedClaimsToBeAdded,
                                                          PreUpdateProfileEvent.FlowInitiatorType initiatorType,
                                                          UniqueIDUserStoreManager userStoreManager)
            throws ActionExecutionResponseProcessorException {

        String claimUri = localClaim.get().getClaimURI();
        String separator = FrameworkUtils.getMultiAttributeSeparator();
        Map<String, String> userClaimValues = getUserClaimValues(userId, claimUri, userStoreManager);
        List<String> filteredValues = getFilteredModifyingClaimValues(userClaimValues, claimUri, claimValue,
                separator);
        if (initiatorType == PreUpdateProfileEvent.FlowInitiatorType.ADMIN ||
                initiatorType == PreUpdateProfileEvent.FlowInitiatorType.APPLICATION) {
            simpleMultiValuedClaimsToBeAdded.put(claimUri, filteredValues);
        } else if (initiatorType == PreUpdateProfileEvent.FlowInitiatorType.USER) {
            String addingClaimValue = (userClaimValues.get(claimUri) == null) ? String.join(separator, filteredValues)
                    : userClaimValues.get(claimUri) + separator + String.join(separator, filteredValues);
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
                                                              String userId, String claimUri, String separator,
                                                              UniqueIDUserStoreManager userStoreManager)
            throws ActionExecutionResponseProcessorException {

        // For multivalued replace, value can be a map with "value" (list) and optional
        // "oldValue" (string), or directly a list of strings for replacing the entire array.
        List<String> claimValue;
        String oldValueName = null;

        if (operation.getValue() instanceof LinkedHashMap) {
            LinkedHashMap<?, ?> valueMap = (LinkedHashMap<?, ?>) operation.getValue();
            claimValue = Optional.ofNullable(valueMap.get(VALUE))
                    .filter(value -> value instanceof List)
                    .map(value -> (List<?>) value)
                    .filter(list -> list.stream().allMatch(e -> e instanceof String))
                    .map(list -> (List<String>) list)
                    .orElseThrow(() -> new ActionExecutionResponseProcessorException(
                            "Missing or wrong format for multi valued claim in operation"));

            oldValueName = Optional.ofNullable(valueMap.get("oldValue"))
                    .filter(value -> value instanceof String)
                    .map(value -> (String) value)
                    .orElse(null);
        } else if (operation.getValue() instanceof List) {
            List<?> rawList = (List<?>) operation.getValue();
            if (!rawList.stream().allMatch(e -> e instanceof String)) {
                throw new ActionExecutionResponseProcessorException(
                        "Missing or wrong format for multi valued claim in replace operation");
            }
            claimValue = (List<String>) operation.getValue();
        } else {
            throw new ActionExecutionResponseProcessorException(
                    "Missing or wrong format for multi valued claim in replace operation");
        }

        if (oldValueName == null) {
            // Replacing the entire array: calculate deltas to avoid changing unaffected values.
            Map<String, String> userClaimValues = getUserClaimValues(userId, claimUri, userStoreManager);
            List<String> existingValues = new ArrayList<>();
            if (userClaimValues.get(claimUri) != null && !userClaimValues.get(claimUri).isEmpty()) {
                existingValues.addAll(Arrays.asList(userClaimValues.get(claimUri).split(Pattern.quote(separator))));
            }

            if (claimValue.size() < existingValues.size()) {
                throw new ActionExecutionResponseProcessorException(
                        "To replace a multivalued attribute, the full array must be passed to " +
                                "prevent accidental deletion of existing values.");
            }

            List<String> addedValues = claimValue.stream()
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .filter(s -> !existingValues.contains(s))
                    .collect(Collectors.toList());

            List<String> removedValues = existingValues.stream()
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .filter(s -> !claimValue.contains(s))
                    .collect(Collectors.toList());

            if (initiatorType == PreUpdateProfileEvent.FlowInitiatorType.ADMIN ||
                    initiatorType == PreUpdateProfileEvent.FlowInitiatorType.APPLICATION) {
                if (!removedValues.isEmpty()) {
                    simpleMultiValuedClaimsToBeRemoved.put(claimUri, removedValues);
                }
                if (!addedValues.isEmpty()) {
                    simpleMultiValuedClaimsToBeAdded.put(claimUri, addedValues);
                }
            } else if (initiatorType == PreUpdateProfileEvent.FlowInitiatorType.USER) {
                String modifyingClaimValue = claimValue.stream()
                        .map(String::trim)
                        .filter(s -> !s.isEmpty())
                        .collect(Collectors.joining(separator));
                userClaimsToBeModified.put(claimUri, modifyingClaimValue);
            }
        } else {
            // Replacing a specific value in the array
            if (initiatorType == PreUpdateProfileEvent.FlowInitiatorType.ADMIN ||
                    initiatorType == PreUpdateProfileEvent.FlowInitiatorType.APPLICATION) {
                Map<String, String> userClaimValues = getUserClaimValues(userId, claimUri, userStoreManager);
                List<String> filteredClaims = getFilteredModifyingClaimValues(userClaimValues, claimUri,
                        claimValue, separator);

                if (!filteredClaims.isEmpty()) {
                    simpleMultiValuedClaimsToBeRemoved.put(claimUri, Arrays.asList(oldValueName));
                    List<String> trimmedFilteredClaims = filteredClaims.stream()
                            .map(String::trim)
                            .collect(Collectors.toList());
                    simpleMultiValuedClaimsToBeAdded.put(claimUri, trimmedFilteredClaims);
                }
            } else if (initiatorType == PreUpdateProfileEvent.FlowInitiatorType.USER) {
                Map<String, String> userClaimValues = getUserClaimValues(userId, claimUri, userStoreManager);
                List<String> filteredClaims = getFilteredModifyingClaimValues(userClaimValues, claimUri,
                        claimValue, separator);

                List<String> existingValues = new ArrayList<>();
                if (userClaimValues.get(claimUri) != null && !userClaimValues.get(claimUri).isEmpty()) {
                    existingValues.addAll(Arrays.asList(userClaimValues.get(claimUri).split(Pattern.quote(separator))));
                }

                existingValues.remove(oldValueName);
                existingValues.addAll(filteredClaims);
                String modifyingClaimValue = existingValues.stream()
                        .map(String::trim)
                        .filter(s -> !s.isEmpty())
                        .collect(Collectors.joining(separator));
                userClaimsToBeModified.put(claimUri, modifyingClaimValue);
            }
        }
    }

    private void populateMultiValuedClaimsForRemoveOperation(
            PreUpdateProfileEvent.FlowInitiatorType initiatorType,
            String claimUri,
            Map<String, String> userClaimsToBeRemoved,
            Map<String, List<String>> simpleMultiValuedClaimsToBeRemoved)
            throws ActionExecutionResponseProcessorException {

        userClaimsToBeRemoved.put(claimUri, "");
    }

    private Optional<LocalClaim> isLocalClaim(String claimUri) throws ActionExecutionResponseProcessorException {

        ClaimMetadataManagementService claimMetadataManagementService = PreUpdateProfileActionServiceComponentHolder
                .getInstance().getClaimManagementService();
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

    private String getClaimUriFromPath(String path)
            throws ActionExecutionResponseProcessorException {

        if (path.startsWith(USER_CLAIMS_FILTER_PATH_PREFIX)) {
            int uriKeyStart = path.indexOf("[uri=");
            if (uriKeyStart == -1) {
                throw new ActionExecutionResponseProcessorException("Invalid filter path format: " + path);
            }

            int valueStart = uriKeyStart + "[uri=".length();
            if (valueStart >= path.length()) {
                throw new ActionExecutionResponseProcessorException("Invalid filter path format: " + path);
            }

            char quoteChar = path.charAt(valueStart);
            if (quoteChar == '\'' || quoteChar == '"') {
                int valueEnd = path.indexOf(quoteChar, valueStart + 1);
                if (valueEnd == -1 || valueEnd + 1 >= path.length() || path.charAt(valueEnd + 1) != ']') {
                    throw new ActionExecutionResponseProcessorException("Invalid filter path format: " + path);
                }
                if (valueEnd + 2 != path.length()) {
                    throw new ActionExecutionResponseProcessorException("Invalid filter path format: " + path);
                }
                return path.substring(valueStart + 1, valueEnd);
            }

            int valueEnd = path.indexOf(']', valueStart);
            if (valueEnd == -1) {
                throw new ActionExecutionResponseProcessorException("Invalid filter path format: " + path);
            }
            if (valueEnd + 1 != path.length()) {
                throw new ActionExecutionResponseProcessorException("Invalid filter path format: " + path);
            }
            return path.substring(valueStart, valueEnd);
        }

        throw new ActionExecutionResponseProcessorException("Invalid path format: " + path);
    }

    private boolean isClaimPathFormat(String path) {

        return path != null && path.startsWith(USER_CLAIMS_FILTER_PATH_PREFIX);
    }

    private UniqueIDUserStoreManager getUserStoreManager() throws ActionExecutionResponseProcessorException {

        String tenantDomain = IdentityContext.getThreadLocalIdentityContext().getTenantDomain();
        RealmService realmService = ActionExecutionServiceComponentHolder.getInstance().getRealmService();

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

    private Map<String, String> getUserClaimValues(String userId, String claimUri,
                                                   UniqueIDUserStoreManager userStoreManager) throws
            ActionExecutionResponseProcessorException {

        try {
            return userStoreManager.getUserClaimValuesWithID(userId, new String[] { claimUri },
                    UserCoreConstants.DEFAULT_PROFILE);
        } catch (org.wso2.carbon.user.core.UserStoreException e) {
            throw new ActionExecutionResponseProcessorException("Failed to retrieve user claims from user store.", e);
        }
    }

    private List<String> getFilteredModifyingClaimValues(Map<String, String> userClaimValues, String claimUri,
                                                         List<String> claimValue, String separator) {

        List<String> userValues = Optional.ofNullable(userClaimValues.get(claimUri))
                .map(value -> Arrays.asList(value.split(separator)))
                .orElse(Collections.emptyList());
        String[] claimValuesList = filterDuplicatedValues(claimValue);

        return Arrays.stream(claimValuesList)
                .filter(value -> !userValues.contains(value))
                .collect(Collectors.toList());
    }

    private String[] filterDuplicatedValues(List<String> claimValue) {

        return claimValue.stream()
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .distinct()
                .toArray(String[]::new);
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

    private void validateSCIMLevelAttributes(String claimUri, Operation op, Object value)
            throws ActionExecutionResponseProcessorException {

        List<Claim> scimClaims = convertLocalToSCIMDialect(claimUri);
        if (scimClaims.isEmpty()) {
            return;
        }

        boolean isReadOnly = false;
        boolean isRequired = false;
        boolean isSingleValued = false;

        // multiple scim mapping
        for (Claim scimClaim : scimClaims) {
            if (Boolean.parseBoolean(scimClaim.getClaimProperty(ClaimConstants.READ_ONLY_PROPERTY))) {
                isReadOnly = true;
            }
            if (Boolean.parseBoolean(scimClaim.getClaimProperty(ClaimConstants.REQUIRED_PROPERTY))) {
                isRequired = true;
            }
            if (!Boolean.parseBoolean(scimClaim.getClaimProperty(ClaimConstants.MULTI_VALUED_PROPERTY))) {
                isSingleValued = true;
            }
        }

        if (isReadOnly) {
            throw new ActionExecutionResponseProcessorException(
                    "Cannot modify read-only SCIM attribute mapped to local claim: " + claimUri);
        }

        if (isRequired) {
            if (op == Operation.REMOVE) {
                throw new ActionExecutionResponseProcessorException(
                        "Cannot remove required SCIM attribute mapped to local claim: " + claimUri);
            }
            if (op == Operation.REPLACE || op == Operation.ADD) {
                boolean isEmpty = false;
                if (value == null) {
                    isEmpty = true;
                } else if (value instanceof String && ((String) value).trim().isEmpty()) {
                    isEmpty = true;
                } else if (value instanceof List && ((List<?>) value).isEmpty()) {
                    isEmpty = true;
                } else if (value instanceof LinkedHashMap) {
                    Object val = ((LinkedHashMap<?, ?>) value).get(VALUE);
                    if (val == null || (val instanceof String && ((String) val).trim().isEmpty()) ||
                            (val instanceof List && ((List<?>) val).isEmpty())) {
                        isEmpty = true;
                    }
                }
                if (isEmpty) {
                    throw new ActionExecutionResponseProcessorException(
                            "Cannot set empty value to required SCIM attribute mapped to local claim: " + claimUri);
                }
            }
        }

        if (isSingleValued) {
            boolean hasMultipleValues = false;
            if (value instanceof List && ((List<?>) value).size() > 1) {
                hasMultipleValues = true;
            } else if (value instanceof LinkedHashMap) {
                Object val = ((LinkedHashMap<?, ?>) value).get(VALUE);
                if (val instanceof List && ((List<?>) val).size() > 1) {
                    hasMultipleValues = true;
                }
            }
            if (hasMultipleValues) {
                throw new ActionExecutionResponseProcessorException(
                        "Cannot set multiple values to single-valued SCIM attribute mapped to local claim: "
                                + claimUri);
            }
        }
    }

    /**
     * Converts claims in local WSO2 dialect to SCIM dialect.
     *
     * @param claimUri The local claim URI.
     * @return A list of SCIM dialect claims.
     * @throws ActionExecutionResponseProcessorException If an error occurs during the conversion.
     */
    private static List<Claim> convertLocalToSCIMDialect(String claimUri) throws
            ActionExecutionResponseProcessorException {

        if (claimUri == null || claimUri.trim().isEmpty()) {
            return Collections.emptyList();
        }

        ClaimMetadataManagementService claimMetadataManagementService = PreUpdateProfileActionServiceComponentHolder
                .getInstance().getClaimManagementService();
        String tenantDomain = IdentityContext.getThreadLocalIdentityContext().getTenantDomain();
        try {
            List<Claim> mappedExternalClaims = claimMetadataManagementService
                    .getMappedExternalClaimsForLocalClaim(claimUri, tenantDomain);
            if (mappedExternalClaims == null || mappedExternalClaims.isEmpty()) {
                return Collections.emptyList();
            }

            return mappedExternalClaims.stream()
                    .filter(claim -> claim.getClaimDialectURI() != null
                            && claim.getClaimDialectURI().contains(SCIM_SCHEMA_URI_PREFIX))
                    .collect(Collectors.toList());

        } catch (ClaimMetadataException e) {
            throw new ActionExecutionResponseProcessorException(
                    "Error while retrieving mapped external claims for claim uri: " + claimUri, e);
        }
    }
}
