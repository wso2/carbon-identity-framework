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

import org.wso2.carbon.identity.action.execution.api.exception.ActionExecutionException;
import org.wso2.carbon.identity.action.execution.api.model.ActionExecutionStatus;
import org.wso2.carbon.identity.action.execution.api.model.ActionType;
import org.wso2.carbon.identity.action.execution.api.model.FlowContext;
import org.wso2.carbon.identity.user.action.api.model.UserActionContext;
import org.wso2.carbon.identity.user.action.api.service.UserActionExecutor;
import org.wso2.carbon.identity.user.pre.update.profile.action.internal.component.PreUpdateProfileActionServiceComponentHolder;
import org.wso2.carbon.identity.user.pre.update.profile.action.internal.constant.PreUpdateProfileActionConstants;

/**
 * User Pre Update Profile Action Executor.
 */
public class UserPreUpdateProfileActionExecutor implements UserActionExecutor {

    @Override
    public ActionType getSupportedActionType() {

        return ActionType.PRE_UPDATE_PROFILE;
    }

    @Override
    public ActionExecutionStatus<?> execute(UserActionContext userActionContext, String tenantDomain)
            throws ActionExecutionException {

        FlowContext flowContext = FlowContext.create();
        flowContext.add(PreUpdateProfileActionConstants.USER_ACTION_CONTEXT, userActionContext);

        return PreUpdateProfileActionServiceComponentHolder.getInstance().getActionExecutorService()
                .execute(getSupportedActionType(), flowContext, tenantDomain);
    }
}
