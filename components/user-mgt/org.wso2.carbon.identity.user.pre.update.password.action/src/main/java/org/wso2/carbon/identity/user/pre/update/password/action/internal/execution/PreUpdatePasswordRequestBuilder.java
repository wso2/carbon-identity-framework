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

package org.wso2.carbon.identity.user.pre.update.password.action.internal.execution;

import org.apache.axiom.om.util.Base64;
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
import org.wso2.carbon.identity.action.execution.api.model.UserClaim;
import org.wso2.carbon.identity.action.execution.api.model.UserStore;
import org.wso2.carbon.identity.action.execution.api.service.ActionExecutionRequestBuilder;
import org.wso2.carbon.identity.action.management.api.model.Action;
import org.wso2.carbon.identity.application.authentication.framework.util.FrameworkUtils;
import org.wso2.carbon.identity.certificate.management.model.Certificate;
import org.wso2.carbon.identity.claim.metadata.mgt.ClaimMetadataManagementService;
import org.wso2.carbon.identity.claim.metadata.mgt.exception.ClaimMetadataException;
import org.wso2.carbon.identity.claim.metadata.mgt.model.LocalClaim;
import org.wso2.carbon.identity.claim.metadata.mgt.util.ClaimConstants;
import org.wso2.carbon.identity.core.context.IdentityContext;
import org.wso2.carbon.identity.core.context.model.Flow;
import org.wso2.carbon.identity.core.context.model.RootOrganization;
import org.wso2.carbon.identity.core.util.IdentityUtil;
import org.wso2.carbon.identity.user.action.api.model.UserActionContext;
import org.wso2.carbon.identity.user.pre.update.password.action.api.model.PasswordSharing;
import org.wso2.carbon.identity.user.pre.update.password.action.api.model.PreUpdatePasswordAction;
import org.wso2.carbon.identity.user.pre.update.password.action.internal.component.PreUpdatePasswordActionServiceComponentHolder;
import org.wso2.carbon.identity.user.pre.update.password.action.internal.constant.PreUpdatePasswordActionConstants;
import org.wso2.carbon.identity.user.pre.update.password.action.internal.model.Credential;
import org.wso2.carbon.identity.user.pre.update.password.action.internal.model.PasswordUpdatingUser;
import org.wso2.carbon.identity.user.pre.update.password.action.internal.model.PreUpdatePasswordEvent;
import org.wso2.carbon.user.api.UserRealm;
import org.wso2.carbon.user.api.UserStoreManager;
import org.wso2.carbon.user.core.UniqueIDUserStoreManager;
import org.wso2.carbon.user.core.UserCoreConstants;
import org.wso2.carbon.user.core.UserStoreException;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.utils.Secret;
import org.wso2.carbon.utils.UnsupportedSecretTypeException;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static org.wso2.carbon.identity.user.pre.update.password.action.internal.constant.PreUpdatePasswordActionConstants.GROUP_CLAIM_URI;
import static org.wso2.carbon.identity.user.pre.update.password.action.internal.constant.PreUpdatePasswordActionConstants.ROLE_CLAIM_URI;

/**
 * This class is responsible for building the action execution request for the pre update password action.
 */
public class PreUpdatePasswordRequestBuilder implements ActionExecutionRequestBuilder {

    @Override
    public ActionType getSupportedActionType() {

        return ActionType.PRE_UPDATE_PASSWORD;
    }

    @Override
    public ActionExecutionRequest buildActionExecutionRequest(FlowContext flowContext,
                                                              ActionExecutionRequestContext actionExecutionContext)
            throws ActionExecutionRequestBuilderException {

        PreUpdatePasswordAction preUpdatePasswordAction = resolveAction(actionExecutionContext);
        UserActionContext userActionContext = resolveUserActionContext(flowContext);

        ActionExecutionRequest.Builder actionRequestBuilder = new ActionExecutionRequest.Builder();
        actionRequestBuilder.actionType(getSupportedActionType());
        actionRequestBuilder.event(getEvent(userActionContext, preUpdatePasswordAction));

        return actionRequestBuilder.build();
    }

    private UserActionContext resolveUserActionContext(FlowContext flowContext)
            throws ActionExecutionRequestBuilderException {

        Object userContext = flowContext.getContextData().get(PreUpdatePasswordActionConstants.USER_ACTION_CONTEXT);
        if (!(userContext instanceof UserActionContext)) {
            throw new ActionExecutionRequestBuilderException("Provided User Action Context is not valid.");
        }
        return (UserActionContext) userContext;
    }

    private PreUpdatePasswordAction resolveAction(ActionExecutionRequestContext actionExecutionContext)
            throws ActionExecutionRequestBuilderException {

        Action action = actionExecutionContext.getAction();
        if (!(action instanceof PreUpdatePasswordAction)) {
            throw new ActionExecutionRequestBuilderException("Provided action is not a Pre Update Password Action.");

        }
        return (PreUpdatePasswordAction) action;
    }

    private Event getEvent(UserActionContext userActionContext, PreUpdatePasswordAction preUpdatePasswordAction)
            throws ActionExecutionRequestBuilderException {

        return new PreUpdatePasswordEvent.Builder()
                .initiatorType(getInitiatorType())
                .action(getAction())
                .tenant(getTenant())
                .user(getUser(userActionContext, preUpdatePasswordAction))
                .userStore(new UserStore(userActionContext.getUserActionRequestDTO().getUserStoreDomain()))
                .organization(getOrganization())
                .build();
    }

    private PreUpdatePasswordEvent.FlowInitiatorType getInitiatorType() throws ActionExecutionRequestBuilderException {

        Flow flow = IdentityContext.getThreadLocalIdentityContext().getFlow();
        if (flow == null) {
            throw new ActionExecutionRequestBuilderException("Flow is not identified.");
        }

        switch(flow.getInitiatingPersona()) {
            case ADMIN:
                return PreUpdatePasswordEvent.FlowInitiatorType.ADMIN;
            case APPLICATION:
                return PreUpdatePasswordEvent.FlowInitiatorType.APPLICATION;
            case USER:
                return PreUpdatePasswordEvent.FlowInitiatorType.USER;
            default:
                break;
        }
        throw new ActionExecutionRequestBuilderException("Invalid initiator flow.");
    }

    private PreUpdatePasswordEvent.Action getAction() throws ActionExecutionRequestBuilderException {

        Flow flow = IdentityContext.getThreadLocalIdentityContext().getFlow();
        if (flow == null) {
            throw new ActionExecutionRequestBuilderException("Flow is not identified.");
        }

        switch (flow.getName()) {
            case PROFILE_UPDATE:
                // Password update is a sub-flow of profile update.
                return PreUpdatePasswordEvent.Action.UPDATE;
            case PASSWORD_RESET:
                return PreUpdatePasswordEvent.Action.RESET;
            case USER_REGISTRATION_INVITE_WITH_PASSWORD:
            case INVITED_USER_REGISTRATION:
                return PreUpdatePasswordEvent.Action.INVITE;
            default:
                break;
        }
        throw new ActionExecutionRequestBuilderException("Invalid action flow.");
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

    private User getUser(UserActionContext userActionContext, PreUpdatePasswordAction preUpdatePasswordAction)
            throws ActionExecutionRequestBuilderException {

        PasswordUpdatingUser.Builder userBuilder = new PasswordUpdatingUser.Builder()
                .id(userActionContext.getUserActionRequestDTO().getUserId())
                .organization(userActionContext.getUserActionRequestDTO().getResidentOrganization());
        populateCredential(userBuilder, userActionContext, preUpdatePasswordAction);

        if (preUpdatePasswordAction.getAttributes() != null && !preUpdatePasswordAction.getAttributes().isEmpty()) {
            populateClaims(userBuilder, userActionContext, preUpdatePasswordAction);
        }
        return userBuilder.build();
    }

    private void populateCredential(PasswordUpdatingUser.Builder userBuilder, UserActionContext userActionContext,
                                    PreUpdatePasswordAction preUpdatePasswordAction)
            throws ActionExecutionRequestBuilderException {

        Certificate certificate = preUpdatePasswordAction.getPasswordSharing().getCertificate();
        if (isEncryptionRequired(certificate)) {
            try {
                X509Certificate publicCert = (X509Certificate)
                        IdentityUtil.convertPEMEncodedContentToCertificate(certificate.getCertificateContent());
                userBuilder.updatingCredential(getUnEncryptedCredential(userActionContext, preUpdatePasswordAction),
                        true, publicCert);
            } catch (CertificateException e) {
                throw new ActionExecutionRequestBuilderException("Error while building X509 certificate.", e);
            }
        } else {
            userBuilder.updatingCredential(getUnEncryptedCredential(userActionContext, preUpdatePasswordAction), false,
                    null);
        }
    }

    private Credential getUnEncryptedCredential(UserActionContext userActionContext,
                                                PreUpdatePasswordAction preUpdatePasswordAction)
            throws ActionExecutionRequestBuilderException {

        PasswordSharing.Format passwordSharingFormat = preUpdatePasswordAction.getPasswordSharing().getFormat();
        if (PasswordSharing.Format.SHA256_HASHED.equals(passwordSharingFormat)) {
            Secret credentialObj;
            try {
                credentialObj = Secret.getSecret(userActionContext.getUserActionRequestDTO().getPassword());
                MessageDigest digest = MessageDigest.getInstance("SHA-256");
                byte[] byteValue = digest.digest(credentialObj.getBytes());
                String passwordHash = Base64.encode(byteValue);

                return new Credential.Builder()
                        .type(Credential.Type.PASSWORD)
                        .format(Credential.Format.HASH)
                        .value(passwordHash.toCharArray())
                        .algorithm(Credential.Algorithm.SHA256)
                        .build();
            } catch (NoSuchAlgorithmException | UnsupportedSecretTypeException e) {
                throw new ActionExecutionRequestBuilderException("Error while hashing the credential.", e);
            }

        } else if (PasswordSharing.Format.PLAIN_TEXT.equals(passwordSharingFormat)) {
            return new Credential.Builder()
                    .type(Credential.Type.PASSWORD)
                    .format(Credential.Format.PLAIN_TEXT)
                    .value(userActionContext.getUserActionRequestDTO().getPassword())
                    .build();
        }

        return null;
    }

    private boolean isEncryptionRequired(Certificate certificate) {

        return certificate != null && StringUtils.isNotEmpty(certificate.getCertificateContent());
    }

    private void populateClaims(PasswordUpdatingUser.Builder userBuilder, UserActionContext userActionContext,
                                PreUpdatePasswordAction action) throws ActionExecutionRequestBuilderException {

        List<String> userClaimsToSetInEvent = action.getAttributes();
        if (userClaimsToSetInEvent == null || userClaimsToSetInEvent.isEmpty()) {
            return;
        }

        Map<String, String> claimValues = getClaimValues(userActionContext.getUserActionRequestDTO().getUserId(),
                userClaimsToSetInEvent);
        String multiAttributeSeparator = FrameworkUtils.getMultiAttributeSeparator();

        setClaimsInUserBuilder(userBuilder, claimValues, multiAttributeSeparator);
        setGroupsInUserBuilder(userBuilder, claimValues, multiAttributeSeparator);
    }

    private Map<String, String> getClaimValues(String userId, List<String> requestedClaims)
            throws ActionExecutionRequestBuilderException {

        try {
            Map<String, String> claimValues = getUserStoreManager().getUserClaimValuesWithID(userId,
                    requestedClaims.toArray(new String[0]), UserCoreConstants.DEFAULT_PROFILE);

            // Filter out the extra claims that are not requested.
            return requestedClaims.stream()
                    .filter(claimValues::containsKey)
                    .collect(Collectors.toMap(Function.identity(), claimValues::get));
        } catch (UserStoreException e) {
            throw new ActionExecutionRequestBuilderException("Failed to retrieve user claims from user store.", e);
        }
    }

    private void setClaimsInUserBuilder(PasswordUpdatingUser.Builder userBuilder, Map<String, String> claimValues,
                                        String multiAttributeSeparator) throws ActionExecutionRequestBuilderException {

        List<UserClaim> userClaimValuesToSetInEvent = new ArrayList<>();
        for (Map.Entry<String, String> claimEntry : claimValues.entrySet()) {
            String claimKey = claimEntry.getKey();
            String claimValue = claimEntry.getValue();

            if (isRoleOrGroupClaim(claimKey) || StringUtils.isBlank(claimValue)) {
                continue;
            }

            if (isMultiValuedClaim(claimKey)) {
                userClaimValuesToSetInEvent.add(
                        new UserClaim(claimKey, claimValue.split(Pattern.quote(multiAttributeSeparator))));
            } else {
                userClaimValuesToSetInEvent.add(new UserClaim(claimKey, claimValue));
            }
        }

        userBuilder.claims(userClaimValuesToSetInEvent);
    }

    private void setGroupsInUserBuilder(PasswordUpdatingUser.Builder userBuilder, Map<String, String> claimValues,
                                        String multiAttributeSeparator) {

        if (claimValues.get(GROUP_CLAIM_URI) != null) {
            userBuilder.groups(Arrays.asList(
                    claimValues.get(GROUP_CLAIM_URI).split(Pattern.quote(multiAttributeSeparator))));
        }
    }

    private boolean isRoleOrGroupClaim(String claimKey) {

        return ROLE_CLAIM_URI.equals(claimKey) || GROUP_CLAIM_URI.equals(claimKey);
    }

    private boolean isMultiValuedClaim(String claimUri) throws ActionExecutionRequestBuilderException {

        ClaimMetadataManagementService claimMetadataManagementService =
                PreUpdatePasswordActionServiceComponentHolder.getInstance().getClaimManagementService();

        try {
            Optional<LocalClaim> localClaim = claimMetadataManagementService.getLocalClaim(claimUri,
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

    private UniqueIDUserStoreManager getUserStoreManager() throws ActionExecutionRequestBuilderException {

        String tenantDomain = IdentityContext.getThreadLocalIdentityContext().getTenantDomain();
        RealmService realmService = PreUpdatePasswordActionServiceComponentHolder.getInstance().getRealmService();

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
        } catch (org.wso2.carbon.user.api.UserStoreException e) {
            throw new ActionExecutionRequestBuilderException(
                    "Error while loading user store manager for tenant: " + tenantDomain, e);
        }
    }
}
