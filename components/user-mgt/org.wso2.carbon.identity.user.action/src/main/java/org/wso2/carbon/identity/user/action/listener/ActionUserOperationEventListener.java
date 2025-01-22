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

package org.wso2.carbon.identity.user.action.listener;

import org.wso2.carbon.identity.action.execution.exception.ActionExecutionException;
import org.wso2.carbon.identity.action.execution.model.ActionExecutionStatus;
import org.wso2.carbon.identity.action.execution.model.ActionType;
import org.wso2.carbon.identity.action.execution.model.Error;
import org.wso2.carbon.identity.action.execution.model.Failure;
import org.wso2.carbon.identity.core.AbstractIdentityUserOperationEventListener;
import org.wso2.carbon.identity.core.context.IdentityContext;
import org.wso2.carbon.identity.core.util.IdentityCoreConstants;
import org.wso2.carbon.identity.user.action.constant.UserActionConstants;
import org.wso2.carbon.identity.user.action.factory.UserActionExecutorFactory;
import org.wso2.carbon.identity.user.action.model.UserActionContext;
import org.wso2.carbon.user.core.UserStoreClientException;
import org.wso2.carbon.user.core.UserStoreException;
import org.wso2.carbon.user.core.UserStoreManager;
import org.wso2.carbon.user.core.util.UserCoreUtil;

import java.util.HashMap;
import java.util.Map;

/**
 * This class is responsible for handling the action invocation related to user flows.
 */
public class ActionUserOperationEventListener extends AbstractIdentityUserOperationEventListener {

    @Override
    public int getExecutionOrderId() {

        int orderId = getOrderId();
        if (orderId != IdentityCoreConstants.EVENT_LISTENER_ORDER_ID) {
            return orderId;
        }
        return 10000;
    }

    @Override
    public boolean doPreUpdateCredentialByAdminWithID(String userName, Object credential,
                                                      UserStoreManager userStoreManager) throws UserStoreException {

        if (!isEnable()) {
            return true;
        }

        try {
            UserActionContext userActionContext = new UserActionContext.Builder()
                    .userId(userName)
                    .password(credential.toString())
                    .userStoreDomain(UserCoreUtil.getDomainName(userStoreManager.getRealmConfiguration()))
                    .build();

            ActionExecutionStatus<?> executionStatus =
                    UserActionExecutorFactory.getUserActionExecutor(ActionType.PRE_UPDATE_PASSWORD)
                            .execute(userActionContext,
                                    IdentityContext.getThreadLocalCarbonContext().getTenantDomain());

            if (executionStatus.getStatus() == ActionExecutionStatus.Status.SUCCESS) {
                return true;
            } else if (executionStatus.getStatus() == ActionExecutionStatus.Status.FAILED) {
                Failure failedResponse = (Failure) executionStatus.getResponse();
                String error = buildErrorMessage(failedResponse.getFailureReason(),
                        failedResponse.getFailureDescription());
                throw new UserStoreClientException(error, UserActionConstants.PRE_UPDATE_PASSWORD_ACTION_ERROR_CODE);
            } else if (executionStatus.getStatus() == ActionExecutionStatus.Status.ERROR) {
                Error errorResponse = (Error) executionStatus.getResponse();
                String error = buildErrorMessage(errorResponse.getErrorMessage(), errorResponse.getErrorDescription());
                throw new UserStoreException(error, UserActionConstants.PRE_UPDATE_PASSWORD_ACTION_ERROR_CODE);
            } else {
                throw new UserStoreException("Unknown status received from the action executor.");
            }
        } catch (ActionExecutionException e) {
            throw new UserStoreException("Error while executing pre update password action.", e);
        }
    }

    private String buildErrorMessage(String message, String description) {

        return message + ". " + description;
    }
}
