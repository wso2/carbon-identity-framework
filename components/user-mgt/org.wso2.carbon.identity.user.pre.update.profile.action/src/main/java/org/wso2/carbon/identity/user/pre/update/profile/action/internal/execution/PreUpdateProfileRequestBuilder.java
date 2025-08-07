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

import org.apache.commons.lang.StringUtils;
import org.wso2.carbon.identity.action.execution.api.exception.ActionExecutionRequestBuilderException;
import org.wso2.carbon.identity.action.execution.api.model.ActionExecutionRequest;
import org.wso2.carbon.identity.action.execution.api.model.ActionExecutionRequestContext;
import org.wso2.carbon.identity.action.execution.api.model.ActionType;
import org.wso2.carbon.identity.action.execution.api.model.Event;
import org.wso2.carbon.identity.action.execution.api.model.FlowContext;
import org.wso2.carbon.identity.action.execution.api.model.Organization;
import org.wso2.carbon.identity.action.execution.api.model.Tenant;
import org.wso2.carbon.identity.action.execution.api.model.User;
import org.wso2.carbon.identity.action.execution.api.model.UserStore;
import org.wso2.carbon.identity.action.execution.api.service.ActionExecutionRequestBuilder;
import org.wso2.carbon.identity.application.authentication.framework.util.FrameworkUtils;
import org.wso2.carbon.identity.claim.metadata.mgt.ClaimMetadataManagementService;
import org.wso2.carbon.identity.claim.metadata.mgt.exception.ClaimMetadataException;
import org.wso2.carbon.identity.claim.metadata.mgt.model.LocalClaim;
import org.wso2.carbon.identity.claim.metadata.mgt.util.ClaimConstants;
import org.wso2.carbon.identity.core.context.IdentityContext;
import org.wso2.carbon.identity.core.context.model.Flow;
import org.wso2.carbon.identity.core.context.model.RootOrganization;
import org.wso2.carbon.identity.user.action.api.model.UserActionContext;
import org.wso2.carbon.identity.user.action.api.model.UserActionRequestDTO;
import org.wso2.carbon.identity.user.pre.update.profile.action.api.model.PreUpdateProfileAction;
import org.wso2.carbon.identity.user.pre.update.profile.action.internal.component.PreUpdateProfileActionServiceComponentHolder;
import org.wso2.carbon.identity.user.pre.update.profile.action.internal.model.PreUpdateProfileEvent;
import org.wso2.carbon.identity.user.pre.update.profile.action.internal.model.PreUpdateProfileRequest;
import org.wso2.carbon.identity.user.pre.update.profile.action.internal.model.UpdatingUserClaim;
import org.wso2.carbon.user.api.UserRealm;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.api.UserStoreManager;
import org.wso2.carbon.user.core.UniqueIDUserStoreManager;
import org.wso2.carbon.user.core.UserCoreConstants;
import org.wso2.carbon.user.core.service.RealmService;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Pre Update Profile Action Request Builder.
 */
public class PreUpdateProfileRequestBuilder implements ActionExecutionRequestBuilder {

    private static final String ROLE_CLAIM_URI = "http://wso2.org/claims/roles";
    private static final String GROUP_CLAIM_URI = "http://wso2.org/claims/groups";

    @Override
    public ActionType getSupportedActionType() {

        return ActionType.PRE_UPDATE_PROFILE;
    }

    @Override
    public ActionExecutionRequest buildActionExecutionRequest(FlowContext flowContext,
                                                              ActionExecutionRequestContext actionExecutionContext)
            throws ActionExecutionRequestBuilderException {

        UserActionContext userActionContext =
                flowContext.getValue(UserActionContext.USER_ACTION_CONTEXT_REFERENCE_KEY, UserActionContext.class);
        PreUpdateProfileAction preUpdateProfileAction = (PreUpdateProfileAction) actionExecutionContext.getAction();

        return new ActionExecutionRequest.Builder()
                .actionType(getSupportedActionType())
                .event(getEvent(userActionContext, preUpdateProfileAction))
                .build();
    }

    private Event getEvent(UserActionContext userActionContext, PreUpdateProfileAction preUpdateProfileAction)
            throws ActionExecutionRequestBuilderException {

        PreUpdateProfileEvent.Builder eventBuilder = new PreUpdateProfileEvent.Builder();
        eventBuilder.initiatorType(getInitiatorType());
        eventBuilder.action(PreUpdateProfileEvent.Action.UPDATE);
        eventBuilder.request(getPreUpdateProfileRequest(userActionContext));
        eventBuilder.tenant(getTenant());

        UniqueIDUserStoreManager userStoreManager = getUserStoreManager();
        eventBuilder.user(getUser(userActionContext, preUpdateProfileAction, userStoreManager));
        eventBuilder.userStore(getUserStore(userActionContext.getUserActionRequestDTO(), userStoreManager));
        eventBuilder.organization(getOrganization());

        return eventBuilder.build();
    }

    private PreUpdateProfileEvent.FlowInitiatorType getInitiatorType() throws ActionExecutionRequestBuilderException {

        Flow flow = Optional.ofNullable(IdentityContext.getThreadLocalIdentityContext().getCurrentFlow())
                .orElseThrow(() -> new ActionExecutionRequestBuilderException("Unknown flow."));

        switch (flow.getInitiatingPersona()) {
            case ADMIN:
                return PreUpdateProfileEvent.FlowInitiatorType.ADMIN;
            case APPLICATION:
                return PreUpdateProfileEvent.FlowInitiatorType.APPLICATION;
            case USER:
                return PreUpdateProfileEvent.FlowInitiatorType.USER;
            default:
                throw new ActionExecutionRequestBuilderException(
                        "Unknown initiator type for the flow: " + flow.getName());
        }
    }

    private PreUpdateProfileRequest getPreUpdateProfileRequest(UserActionContext userActionContext)
            throws ActionExecutionRequestBuilderException {

        UserActionRequestDTO userActionRequestDTO = userActionContext.getUserActionRequestDTO();

        if (userActionRequestDTO.getClaims() == null || userActionRequestDTO.getClaims().isEmpty()) {
            return null;
        }

        PreUpdateProfileRequest.Builder preUpdateProfileRequestBuilder = new PreUpdateProfileRequest.Builder();

        for (Map.Entry<String, Object> claimEntry : userActionRequestDTO.getClaims().entrySet()) {
            if (isRoleOrGroupClaim(claimEntry.getKey())) {
                 /* Skip role and group claims.
                 Profile update extension should not share role and group claims as claims updating.
                 Updating roles or groups should be a separate workflow.*/
                continue;
            }
            if (claimEntry.getValue() instanceof String) {
                preUpdateProfileRequestBuilder.addClaim(claimEntry.getKey(), claimEntry.getValue().toString());
            } else if (claimEntry.getValue() instanceof String[]) {
                preUpdateProfileRequestBuilder.addClaim(claimEntry.getKey(), (String[]) claimEntry.getValue());
            } else {
                throw new ActionExecutionRequestBuilderException(
                        "Unknown user claim value format. Only String and String[] types expected.");
            }
        }

        return preUpdateProfileRequestBuilder.build();
    }

    private Tenant getTenant() throws ActionExecutionRequestBuilderException {

        RootOrganization rootOrganization = IdentityContext.getThreadLocalIdentityContext().getRootOrganization();
        if (rootOrganization == null) {
            throw new ActionExecutionRequestBuilderException("Root organization information is not available in " +
                    "Identity Context.");
        }

        return new Tenant(String.valueOf(rootOrganization.getAssociatedTenantId()),
                rootOrganization.getAssociatedTenantDomain());
    }

    private Organization getOrganization() {

        org.wso2.carbon.identity.core.context.model.Organization organization =
                IdentityContext.getThreadLocalIdentityContext().getOrganization();
        if (organization == null) {
            return null;
        }

        return new Organization.Builder()
                .id(organization.getId())
                .name(organization.getName())
                .orgHandle(organization.getOrganizationHandle())
                .depth(organization.getDepth())
                .build();
    }

    private User getUser(UserActionContext userActionContext, PreUpdateProfileAction preUpdateProfileAction,
                         UniqueIDUserStoreManager userStoreManager) throws ActionExecutionRequestBuilderException {

        UserActionRequestDTO userActionRequestDTO = userActionContext.getUserActionRequestDTO();
        List<String> userClaimsToSetInEvent = preUpdateProfileAction.getAttributes();

        User.Builder userBuilder = new User.Builder(userActionRequestDTO.getUserId())
                .organization(userActionRequestDTO.getResidentOrganization())
                .sharedUserId(userActionRequestDTO.getSharedUserId());
        if (userClaimsToSetInEvent == null || userClaimsToSetInEvent.isEmpty()) {
            return userBuilder.build();
        }

        Map<String, String> claimValues = getClaimValues(resolveOrgBoundUserId(userActionRequestDTO),
                userClaimsToSetInEvent, userStoreManager);
        String multiAttributeSeparator = FrameworkUtils.getMultiAttributeSeparator();

        setClaimsInUserBuilder(userBuilder, claimValues, userActionRequestDTO.getClaims(), multiAttributeSeparator);
        setGroupsInUserBuilder(userBuilder, claimValues, multiAttributeSeparator);

        return userBuilder.build();
    }

    private Map<String, String> getClaimValues(String userId, List<String> requestedClaims,
                                               UniqueIDUserStoreManager userStoreManager)
            throws ActionExecutionRequestBuilderException {

        try {
            Map<String, String> claimValues = userStoreManager.getUserClaimValuesWithID(userId,
                    requestedClaims.toArray(new String[0]), UserCoreConstants.DEFAULT_PROFILE);

            // Filter out the extra claims that are not requested.
            return requestedClaims.stream()
                    .filter(claimValues::containsKey)
                    .collect(Collectors.toMap(Function.identity(), claimValues::get));
        } catch (org.wso2.carbon.user.core.UserStoreException e) {
            throw new ActionExecutionRequestBuilderException("Failed to retrieve user claims from user store.", e);
        }
    }

    private void setClaimsInUserBuilder(User.Builder userBuilder, Map<String, String> claimValues,
                                        Map<String, Object> updatingUserClaimsInRequest,
                                        String multiAttributeSeparator) throws ActionExecutionRequestBuilderException {

        List<UpdatingUserClaim> userClaimValuesToSetInEvent = new ArrayList<>();
        for (Map.Entry<String, String> claimEntry : claimValues.entrySet()) {
            String claimKey = claimEntry.getKey();
            String claimValue = claimEntry.getValue();

            if (isRoleOrGroupClaim(claimKey) || StringUtils.isBlank(claimValue)) {
                continue;
            }

            if (isMultiValuedClaim(claimKey)) {
                userClaimValuesToSetInEvent.add(
                        constructMultiValuedClaim(updatingUserClaimsInRequest, claimKey, claimValue,
                                multiAttributeSeparator));
            } else {
                userClaimValuesToSetInEvent.add(constructSingleValuedClaim(updatingUserClaimsInRequest, claimKey,
                        claimValue));
            }
        }

        userBuilder.claims(userClaimValuesToSetInEvent);
    }

    private void setGroupsInUserBuilder(User.Builder userBuilder, Map<String, String> claimValues,
                                        String multiAttributeSeparator) {

        if (claimValues.get(GROUP_CLAIM_URI) != null) {
            userBuilder.groups(Arrays.asList(
                    claimValues.get(GROUP_CLAIM_URI).split(Pattern.quote(multiAttributeSeparator))));
        }
    }

    private boolean isRoleOrGroupClaim(String claimKey) {

        return ROLE_CLAIM_URI.equals(claimKey) || GROUP_CLAIM_URI.equals(claimKey);
    }

    private UpdatingUserClaim constructMultiValuedClaim(Map<String, Object> updatingUserClaimsInRequest,
                                                        String claimKey, String claimValue,
                                                        String multiAttributeSeparator)
            throws ActionExecutionRequestBuilderException {

        if (updatingUserClaimsInRequest.containsKey(claimKey)) {
            Object updatingClaimValue = updatingUserClaimsInRequest.get(claimKey);
            if (!(updatingClaimValue instanceof String[])) {
                throw new ActionExecutionRequestBuilderException(
                        "Invalid claim value format for multi-valued claim: " + claimKey +
                                " Only String[] types are expected.");
            }
            return new UpdatingUserClaim(claimKey, claimValue.split(Pattern.quote(multiAttributeSeparator)),
                    (String[]) updatingClaimValue);
        }

        return new UpdatingUserClaim(claimKey,
                claimValue.split(Pattern.quote(multiAttributeSeparator)));
    }

    private UpdatingUserClaim constructSingleValuedClaim(Map<String, Object> updatingUserClaimsInRequest,
                                                         String claimKey, String claimValue) {

        if (updatingUserClaimsInRequest.containsKey(claimKey)) {
            Object updatingClaimValue = updatingUserClaimsInRequest.get(claimKey);
            return new UpdatingUserClaim(claimKey, claimValue, String.valueOf(updatingClaimValue));
        }

        return new UpdatingUserClaim(claimKey, claimValue);
    }

    private UserStore getUserStore(UserActionRequestDTO userActionRequestDTO, UniqueIDUserStoreManager userStoreManager)
            throws ActionExecutionRequestBuilderException {

        String userStoreDomain = userActionRequestDTO.getUserStoreDomain();
        if (StringUtils.isBlank(userStoreDomain)) {

            try {
                org.wso2.carbon.user.core.common.User userFromUserStore = userStoreManager.getUserWithID(
                        resolveOrgBoundUserId(userActionRequestDTO), null, UserCoreConstants.DEFAULT_PROFILE);
                userStoreDomain = userFromUserStore.getUserStoreDomain();
            } catch (org.wso2.carbon.user.core.UserStoreException e) {
                throw new ActionExecutionRequestBuilderException("Error while retrieving user store domain.", e);
            }
        }

        return new UserStore(userStoreDomain);
    }

    private UniqueIDUserStoreManager getUserStoreManager() throws ActionExecutionRequestBuilderException {

        String tenantDomain = IdentityContext.getThreadLocalIdentityContext().getTenantDomain();
        RealmService realmService = PreUpdateProfileActionServiceComponentHolder.getInstance().getRealmService();

        if (realmService == null) {
            throw new ActionExecutionRequestBuilderException("Realm service is unavailable.");
        }

        try {
            int tenantId = realmService.getTenantManager().getTenantId(tenantDomain);
            UserRealm userRealm = realmService.getTenantUserRealm(tenantId);

            if (userRealm == null) {
                throw new ActionExecutionRequestBuilderException(
                        "User realm is not available for tenant: " + tenantDomain);
            }

            UserStoreManager userStoreManager = userRealm.getUserStoreManager();
            if (!(userStoreManager instanceof UniqueIDUserStoreManager)) {
                throw new ActionExecutionRequestBuilderException(
                        "User store manager is not an instance of UniqueIDUserStoreManager for tenant: " +
                                tenantDomain);
            }

            return (UniqueIDUserStoreManager) userStoreManager;
        } catch (UserStoreException e) {
            throw new ActionExecutionRequestBuilderException(
                    "Error while loading user store manager for tenant: " + tenantDomain, e);
        }
    }

    private boolean isMultiValuedClaim(String claimUri) throws ActionExecutionRequestBuilderException {

        ClaimMetadataManagementService claimMetadataManagementService =
                PreUpdateProfileActionServiceComponentHolder.getInstance().getClaimManagementService();

        try {
            Optional<LocalClaim>
                    localClaim = claimMetadataManagementService.getLocalClaim(claimUri,
                    IdentityContext.getThreadLocalIdentityContext().getTenantDomain());

            if (!localClaim.isPresent()) {
                throw new ActionExecutionRequestBuilderException("Claim not found for claim URI: " + claimUri);
            }

            return Boolean.parseBoolean(localClaim.get().getClaimProperty(ClaimConstants.MULTI_VALUED_PROPERTY));
        } catch (ClaimMetadataException e) {
            throw new ActionExecutionRequestBuilderException("Error while retrieving claim metadata for claim URI: " +
                    claimUri, e);
        }
    }

    private static String resolveOrgBoundUserId(UserActionRequestDTO userActionRequestDTO) {

        // If the user is shared, shared user id should be returned.
        if (userActionRequestDTO.getSharedUserId() != null) {
            return userActionRequestDTO.getSharedUserId();
        }
        return userActionRequestDTO.getUserId();
    }
}
