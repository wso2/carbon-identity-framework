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

package org.wso2.carbon.identity.user.pre.update.password.action.internal.versioning.common.util;

import org.apache.axiom.om.util.Base64;
import org.apache.commons.lang.StringUtils;
import org.wso2.carbon.identity.action.execution.api.exception.ActionExecutionRequestBuilderException;
import org.wso2.carbon.identity.action.execution.api.model.ActionExecutionRequestContext;
import org.wso2.carbon.identity.action.execution.api.model.FlowContext;
import org.wso2.carbon.identity.action.execution.api.model.Organization;
import org.wso2.carbon.identity.action.execution.api.model.Tenant;
import org.wso2.carbon.identity.action.execution.api.model.UserClaim;
import org.wso2.carbon.identity.action.management.api.model.Action;
import org.wso2.carbon.identity.certificate.management.model.Certificate;
import org.wso2.carbon.identity.claim.metadata.mgt.ClaimMetadataManagementService;
import org.wso2.carbon.identity.claim.metadata.mgt.exception.ClaimMetadataException;
import org.wso2.carbon.identity.claim.metadata.mgt.model.LocalClaim;
import org.wso2.carbon.identity.claim.metadata.mgt.util.ClaimConstants;
import org.wso2.carbon.identity.core.context.IdentityContext;
import org.wso2.carbon.identity.core.context.model.Flow;
import org.wso2.carbon.identity.core.context.model.RootOrganization;
import org.wso2.carbon.identity.user.action.api.model.UserActionContext;
import org.wso2.carbon.identity.user.pre.update.password.action.api.model.PasswordSharing;
import org.wso2.carbon.identity.user.pre.update.password.action.api.model.PreUpdatePasswordAction;
import org.wso2.carbon.identity.user.pre.update.password.action.internal.component.PreUpdatePasswordActionServiceComponentHolder;
import org.wso2.carbon.identity.user.pre.update.password.action.internal.constant.PreUpdatePasswordActionConstants;
import org.wso2.carbon.identity.user.pre.update.password.action.internal.versioning.common.model.Credential;
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
import java.util.ArrayList;
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
public class PreUpdatePasswordRequestBuilderUtil {

    public static UserActionContext resolveUserActionContext(FlowContext flowContext)
            throws ActionExecutionRequestBuilderException {

        Object userContext = flowContext.getContextData().get(PreUpdatePasswordActionConstants.USER_ACTION_CONTEXT);
        if (!(userContext instanceof UserActionContext)) {
            throw new ActionExecutionRequestBuilderException("Provided User Action Context is not valid.");
        }
        return (UserActionContext) userContext;
    }

    public static PreUpdatePasswordAction resolveAction(ActionExecutionRequestContext actionExecutionContext)
            throws ActionExecutionRequestBuilderException {

        Action action = actionExecutionContext.getAction();
        if (!(action instanceof PreUpdatePasswordAction)) {
            throw new ActionExecutionRequestBuilderException("Provided action is not a Pre Update Password Action.");

        }
        return (PreUpdatePasswordAction) action;
    }

    public static Tenant getTenant() throws ActionExecutionRequestBuilderException {

        RootOrganization rootOrganization = IdentityContext.getThreadLocalIdentityContext().getRootOrganization();
        if (rootOrganization == null) {
            throw new ActionExecutionRequestBuilderException("Root organization information is not available in " +
                    "Identity Context.");
        }

        return new Tenant(String.valueOf(rootOrganization.getAssociatedTenantId()),
                rootOrganization.getAssociatedTenantDomain());
    }

    public static Organization getOrganization() {

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

    public static Credential getUnEncryptedCredential(UserActionContext userActionContext,
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

    public static boolean isEncryptionRequired(Certificate certificate) {

        return certificate != null && StringUtils.isNotEmpty(certificate.getCertificateContent());
    }


    public static Map<String, String> getClaimValues(String userId, List<String> requestedClaims)
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

    public static List<UserClaim> setClaimsInUserBuilder(Map<String, String> claimValues,
                                                         String multiAttributeSeparator)
            throws ActionExecutionRequestBuilderException {

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

        return userClaimValuesToSetInEvent;
    }

    private static boolean isRoleOrGroupClaim(String claimKey) {

        return ROLE_CLAIM_URI.equals(claimKey) || GROUP_CLAIM_URI.equals(claimKey);
    }

    private static boolean isMultiValuedClaim(String claimUri) throws ActionExecutionRequestBuilderException {

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

    private static UniqueIDUserStoreManager getUserStoreManager() throws ActionExecutionRequestBuilderException {

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

    public static Flow.Name getCurrentFlowName() {

        Flow flow = IdentityContext.getThreadLocalIdentityContext().getCurrentFlow();
        return (flow != null) ? flow.getName() : null;
    }
}
