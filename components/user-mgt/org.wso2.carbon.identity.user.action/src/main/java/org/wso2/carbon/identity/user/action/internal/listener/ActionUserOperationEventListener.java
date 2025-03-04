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
import org.wso2.carbon.identity.core.AbstractIdentityUserOperationEventListener;
import org.wso2.carbon.identity.core.context.IdentityContext;
import org.wso2.carbon.identity.core.context.model.Flow;
import org.wso2.carbon.identity.core.util.IdentityCoreConstants;
import org.wso2.carbon.identity.user.action.api.constant.UserActionError;
import org.wso2.carbon.identity.user.action.api.model.UserActionContext;
import org.wso2.carbon.identity.user.action.internal.factory.UserActionExecutorFactory;
import org.wso2.carbon.user.core.UserStoreClientException;
import org.wso2.carbon.user.core.UserStoreException;
import org.wso2.carbon.user.core.UserStoreManager;
import org.wso2.carbon.user.core.listener.SecretHandleableListener;
import org.wso2.carbon.user.core.util.UserCoreUtil;
import org.wso2.carbon.utils.Secret;

import java.util.Map;

/**
 * This class is responsible for handling the action invocation related to user flows.
 */
public class ActionUserOperationEventListener extends AbstractIdentityUserOperationEventListener implements
        SecretHandleableListener {

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
     * @param userName          Username of the user.
     * @param credential        Updating credential.
     * @param userStoreManager  User store manager.
     * @return True if the operation is successful.
     * @throws UserStoreException If an error occurs while executing the action.
     */
    @Override
    public boolean doPreUpdateCredentialByAdminWithID(String userName, Object credential,
                                                      UserStoreManager userStoreManager) throws UserStoreException {

        //TODO: this configuration only checks for listener configuration. check for the action configuration as well.
        if (!isEnable()) {
            return true;
        }

        if (!isExecutableFlow()) {
            return true;
        }

        try {
            UserActionContext userActionContext = new UserActionContext.Builder()
                    .userId(userName)
                    .password(getSecret(credential))
                    .userStoreDomain(UserCoreUtil.getDomainName(userStoreManager.getRealmConfiguration()))
                    .build();

            ActionExecutionStatus<?> executionStatus =
                    UserActionExecutorFactory.getUserActionExecutor(ActionType.PRE_UPDATE_PASSWORD)
                            .execute(userActionContext,
                                    IdentityContext.getThreadLocalCarbonContext().getTenantDomain());

            if (executionStatus.getStatus() == ActionExecutionStatus.Status.SUCCESS) {
                return true;
            } else if (executionStatus.getStatus() == ActionExecutionStatus.Status.FAILED) {
                Failure failure = (Failure) executionStatus.getResponse();
                String errorMsg = buildErrorMessage(failure.getFailureReason(), failure.getFailureDescription());
                throw new UserStoreClientException(errorMsg,
                        UserActionError.PRE_UPDATE_PASSWORD_ACTION_EXECUTION_FAILED);
            } else if (executionStatus.getStatus() == ActionExecutionStatus.Status.ERROR) {
                Error error = (Error) executionStatus.getResponse();
                String errorMsg = buildErrorMessage(error.getErrorMessage(), error.getErrorDescription());
                throw new UserStoreException(errorMsg, UserActionError.PRE_UPDATE_PASSWORD_ACTION_EXECUTION_ERROR);
            } else {
                return false;
            }
        } catch (ActionExecutionException e) {
            throw new UserStoreException("Error while executing pre update password action.",
                    UserActionError.PRE_UPDATE_PASSWORD_ACTION_SERVER_ERROR, e);
        }
    }

    @Override
    public boolean doPreSetUserClaimValues(String userName, Map<String, String> claims, String profileName,
                                           UserStoreManager userStoreManager) throws UserStoreException {

        if (!isEnable()) {
            return true;
        }

        if (!isExecutableFlow()) {
            return true;
        }

        try {
            UserActionContext userActionContext = new UserActionContext.Builder()
                    .userId(userName)
                    .userStoreDomain(UserCoreUtil.getDomainName(userStoreManager.getRealmConfiguration()))
                    .build();

            ActionExecutionStatus<?> executionStatus =
                    UserActionExecutorFactory.getUserActionExecutor(ActionType.PRE_UPDATE_PROFILE)
                            .execute(userActionContext,
                                    IdentityContext.getThreadLocalCarbonContext().getTenantDomain());

            if (executionStatus.getStatus() == ActionExecutionStatus.Status.SUCCESS) {
                return true;
            } else if (executionStatus.getStatus() == ActionExecutionStatus.Status.FAILED) {
                Failure failure = (Failure) executionStatus.getResponse();
                String errorMsg = buildErrorMessage(failure.getFailureReason(), failure.getFailureDescription());
                throw new UserStoreClientException(errorMsg,
                        UserActionError.PRE_UPDATE_PROFILE_ACTION_EXECUTION_FAILED);
            } else if (executionStatus.getStatus() == ActionExecutionStatus.Status.ERROR) {
                Error error = (Error) executionStatus.getResponse();
                String errorMsg = buildErrorMessage(error.getErrorMessage(), error.getErrorDescription());
                throw new UserStoreException(errorMsg, UserActionError.PRE_UPDATE_PROFILE_ACTION_EXECUTION_ERROR);
            } else {
                return false;
            }
        } catch (ActionExecutionException e) {
            throw new UserStoreException("Error while executing pre update profile action.",
                    UserActionError.PRE_UPDATE_PROFILE_ACTION_SERVER_ERROR, e);
        }
    }

    private boolean isExecutableFlow() {

        Flow flow = IdentityContext.getThreadLocalIdentityContext().getFlow();
        if (flow == null) {
            return false;
        }

        return flow.getName() == Flow.Name.PASSWORD_RESET ||
                flow.getName() == Flow.Name.USER_REGISTRATION_INVITE_WITH_PASSWORD ||
                flow.getName() == Flow.Name.PROFILE_UPDATE;
    }

    private char[] getSecret(Object credential) throws UserStoreException {

        if (credential instanceof Secret) {
            return ((Secret) credential).getChars();
        } else if (credential instanceof StringBuffer) {
            return ((StringBuffer) credential).toString().toCharArray();
        } else {
            throw new UserStoreException("Credential is not in the expected format.",
                    UserActionError.PRE_UPDATE_PASSWORD_ACTION_UNSUPPORTED_SECRET);
        }
    }

    private String buildErrorMessage(String message, String description) {

        if (description == null) {
            return message;
        }

        return message + ". " + description;
    }
}
