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

package org.wso2.carbon.identity.user.action.internal.listener;

import org.wso2.carbon.identity.action.execution.api.exception.ActionExecutionException;
import org.wso2.carbon.identity.action.execution.api.model.ActionExecutionStatus;
import org.wso2.carbon.identity.action.execution.api.model.ActionType;
import org.wso2.carbon.identity.action.execution.api.model.Error;
import org.wso2.carbon.identity.action.execution.api.model.Failure;
import org.wso2.carbon.identity.action.execution.api.model.Organization;
import org.wso2.carbon.identity.core.AbstractIdentityUserOperationEventListener;
import org.wso2.carbon.identity.core.context.IdentityContext;
import org.wso2.carbon.identity.core.context.model.Flow;
import org.wso2.carbon.identity.core.util.IdentityCoreConstants;
import org.wso2.carbon.identity.organization.management.service.constant.OrganizationManagementConstants;
import org.wso2.carbon.identity.organization.management.service.exception.OrganizationManagementException;
import org.wso2.carbon.identity.organization.management.service.model.BasicOrganization;
import org.wso2.carbon.identity.user.action.api.constant.UserActionError;
import org.wso2.carbon.identity.user.action.api.exception.UserActionExecutionClientException;
import org.wso2.carbon.identity.user.action.api.exception.UserActionExecutionServerException;
import org.wso2.carbon.identity.user.action.api.model.UserActionContext;
import org.wso2.carbon.identity.user.action.api.model.UserActionRequestDTO;
import org.wso2.carbon.identity.user.action.internal.component.UserActionServiceComponentHolder;
import org.wso2.carbon.identity.user.action.internal.factory.UserActionExecutorFactory;
import org.wso2.carbon.user.core.UniqueIDUserStoreManager;
import org.wso2.carbon.user.core.UserCoreConstants;
import org.wso2.carbon.user.core.UserStoreException;
import org.wso2.carbon.user.core.UserStoreManager;
import org.wso2.carbon.user.core.listener.SecretHandleableListener;
import org.wso2.carbon.user.core.util.UserCoreUtil;
import org.wso2.carbon.utils.Secret;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

import java.util.Collections;
import java.util.Map;

/**
 * This class is responsible for handling the action invocation related to user flows.
 */
public class ActionUserOperationEventListener extends AbstractIdentityUserOperationEventListener implements
        SecretHandleableListener {

    private static final String MANAGED_ORG_CLAIM_URI = "http://wso2.org/claims/identity/managedOrg";

    @Override
    public int getExecutionOrderId() {

        // Order id should be set to a high value so that this listener executes last,
        // supporting the extension of product behavior at user operations.
        int orderId = getOrderId();
        if (orderId != IdentityCoreConstants.EVENT_LISTENER_ORDER_ID) {
            return orderId;
        }
        return 10000;
    }

    /**
     * This method is responsible for handling the pre update password action execution.
     * Since the listener is as a secret handleable listener, the receiving credential will be in Secret object type.
     *
     * @param userID           User id of the user.
     * @param credential       Updating credential.
     * @param userStoreManager User store manager.
     * @return True if the operation is successful.
     * @throws UserStoreException If an error occurs while executing the action.
     */
    @Override
    public boolean doPreUpdateCredentialByAdminWithID(String userID, Object credential,
                                                      UserStoreManager userStoreManager) throws UserStoreException {

        if (!isEnable()) {
            return true;
        }

        if (!isExecutableFlow()) {
            return true;
        }

        try {
            UserActionContext userActionContext = new UserActionContext(
                    new UserActionRequestDTO.Builder()
                            .userId(userID)
                            .password(getSecret(credential))
                            .userStoreDomain(UserCoreUtil.getDomainName(userStoreManager.getRealmConfiguration()))
                            .residentOrganization(getUserResidentOrganization(userID, userStoreManager))
                            .build());

            ActionExecutionStatus<?> executionStatus =
                    UserActionExecutorFactory.getUserActionExecutor(ActionType.PRE_UPDATE_PASSWORD)
                            .execute(userActionContext,
                                    IdentityContext.getThreadLocalCarbonContext().getTenantDomain());

            if (executionStatus.getStatus() == ActionExecutionStatus.Status.SUCCESS) {
                return true;
            } else if (executionStatus.getStatus() == ActionExecutionStatus.Status.FAILED) {
                Failure failure = (Failure) executionStatus.getResponse();
                throw new UserActionExecutionClientException(
                        UserActionError.PRE_UPDATE_PASSWORD_ACTION_EXECUTION_FAILED,
                        failure.getFailureReason(), failure.getFailureDescription());
            } else if (executionStatus.getStatus() == ActionExecutionStatus.Status.ERROR) {
                Error error = (Error) executionStatus.getResponse();
                throw new UserActionExecutionServerException(
                        UserActionError.PRE_UPDATE_PASSWORD_ACTION_EXECUTION_ERROR,
                        error.getErrorMessage(), error.getErrorDescription());
            } else {
                return false;
            }
        } catch (ActionExecutionException e) {
            throw new UserActionExecutionServerException(UserActionError.PRE_UPDATE_PASSWORD_ACTION_SERVER_ERROR,
                    "Error while executing pre update password action.");
        }
    }

    /**
     * This method is responsible for handling the pre add user action execution.
     *
     * @param userID           User id of the user.
     * @param credential       Credential of the user.
     * @param userStoreManager User store manager.
     * @return True if the operation is successful.
     * @throws UserStoreException If an error occurs while executing the action.
     */
    @Override
    public boolean doPreAddUserWithID(String userID, Object credential, String[] roleList, Map<String, String> claims,
                                      String profile, UserStoreManager userStoreManager) throws UserStoreException {

        if (!isEnable()) {
            return true;
        }

        if (!isPreAddUserWithIDExecutableFlow()) {
            return true;
        }

        try {
            UserActionContext userActionContext = new UserActionContext(
                    new UserActionRequestDTO.Builder()
                            .userId(userID)
                            .password(getSecret(credential))
                            .userStoreDomain(UserCoreUtil.getDomainName(userStoreManager.getRealmConfiguration()))
                            .build());

            ActionExecutionStatus<?> executionStatus =
                    UserActionExecutorFactory.getUserActionExecutor(ActionType.PRE_UPDATE_PASSWORD)
                            .execute(userActionContext,
                                    IdentityContext.getThreadLocalCarbonContext().getTenantDomain());

            if (executionStatus.getStatus() == ActionExecutionStatus.Status.SUCCESS) {
                return true;
            } else if (executionStatus.getStatus() == ActionExecutionStatus.Status.FAILED) {
                Failure failure = (Failure) executionStatus.getResponse();
                throw new UserActionExecutionClientException(
                        UserActionError.PRE_UPDATE_PASSWORD_ACTION_EXECUTION_FAILED,
                        failure.getFailureReason(), failure.getFailureDescription());
            } else if (executionStatus.getStatus() == ActionExecutionStatus.Status.ERROR) {
                Error error = (Error) executionStatus.getResponse();
                throw new UserActionExecutionServerException(
                        UserActionError.PRE_UPDATE_PASSWORD_ACTION_EXECUTION_ERROR,
                        error.getErrorMessage(), error.getErrorDescription());
            } else {
                return false;
            }
        } catch (ActionExecutionException e) {
            throw new UserActionExecutionServerException(UserActionError.PRE_UPDATE_PASSWORD_ACTION_SERVER_ERROR,
                    "Error while executing pre update password action.");
        }
    }

    private Organization getUserResidentOrganization(String userID, UserStoreManager userStoreManager)
            throws ActionExecutionException {


        org.wso2.carbon.identity.core.context.model.Organization accessingOrganization =
                IdentityContext.getThreadLocalIdentityContext().getOrganization();
        if (accessingOrganization == null) {
            // If the accessing organization is null, it means the user is not accessing from an organization context.
            // Hence, return null.
            return null;
        }

        try {
            String managedOrgId = ((UniqueIDUserStoreManager) userStoreManager)
                    .getUserClaimValueWithID(userID, MANAGED_ORG_CLAIM_URI, UserCoreConstants.DEFAULT_PROFILE);
            if (managedOrgId == null) {
                // User resident organization is the accessing organization if the managed organization claim is
                // not set.
               return new Organization.Builder()
                       .id(accessingOrganization.getId())
                       .name(accessingOrganization.getName())
                       .orgHandle(accessingOrganization.getOrganizationHandle())
                       .depth(accessingOrganization.getDepth())
                       .build();
            }
            // If the managed organization claim is set, retrieve the organization details.
            return getOrganization(managedOrgId);
        } catch (UserStoreException e) {
            throw new ActionExecutionException("Error while retrieving the user's managed by organization claim.", e);
        }
    }

    private Organization getOrganization(String managedOrgId) throws ActionExecutionException {

        if (OrganizationManagementConstants.SUPER_ORG_ID.equals(managedOrgId)) {
            return new Organization.Builder()
                    .id(OrganizationManagementConstants.SUPER_ORG_ID)
                    .name(OrganizationManagementConstants.SUPER)
                    .orgHandle(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME)
                    .depth(0)
                    .build();
        }

        try {
            Map<String, BasicOrganization> orgsMap = UserActionServiceComponentHolder.getInstance().
                    getOrganizationManager()
                    .getBasicOrganizationDetailsByOrgIDs(Collections.singletonList(managedOrgId));
            BasicOrganization basicOrganization = orgsMap.get(managedOrgId);
            if (basicOrganization == null) {
                throw new ActionExecutionException("No organization found for the user's managed organization id: "
                        + managedOrgId);
            }

            int depth = UserActionServiceComponentHolder.getInstance().getOrganizationManager()
                    .getOrganizationDepthInHierarchy(managedOrgId);
            return new Organization.Builder()
                    .id(basicOrganization.getId())
                    .name(basicOrganization.getName())
                    .orgHandle(basicOrganization.getOrganizationHandle())
                    .depth(depth)
                    .build();
        } catch (OrganizationManagementException e) {
            throw new ActionExecutionException("Error while retrieving organization details for the user's " +
                    "managed organization id: " + managedOrgId, e);
        }
    }

    private boolean isExecutableFlow() {

        Flow flow = IdentityContext.getThreadLocalIdentityContext().getFlow();
        if (flow == null) {
            return false;
        }

        return flow.getName() == Flow.Name.PASSWORD_RESET ||
                flow.getName() == Flow.Name.INVITE ||
                flow.getName() == Flow.Name.INVITED_USER_REGISTRATION ||
                flow.getName() == Flow.Name.PROFILE_UPDATE;
    }

    private boolean isPreAddUserWithIDExecutableFlow() {
        Flow flow = IdentityContext.getThreadLocalIdentityContext().getFlow();
        if (flow == null) {
            return false;
        }

        return flow.getName() == Flow.Name.USER_REGISTRATION;
    }

    private char[] getSecret(Object credential) throws UserActionExecutionServerException {

        if (credential instanceof Secret) {
            return ((Secret) credential).getChars();
        } else if (credential instanceof StringBuffer) {
            return ((StringBuffer) credential).toString().toCharArray();
        } else {
            throw new UserActionExecutionServerException(UserActionError.PRE_UPDATE_PASSWORD_ACTION_UNSUPPORTED_SECRET,
                    "Credential is not in the expected format.");
        }
    }
}
