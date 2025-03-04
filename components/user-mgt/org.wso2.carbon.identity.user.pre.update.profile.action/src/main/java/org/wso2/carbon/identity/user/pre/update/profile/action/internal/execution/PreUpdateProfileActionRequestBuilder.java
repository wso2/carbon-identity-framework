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

import org.wso2.carbon.identity.action.execution.api.exception.ActionExecutionRequestBuilderException;
import org.wso2.carbon.identity.action.execution.api.model.*;
import org.wso2.carbon.identity.action.execution.api.service.ActionExecutionRequestBuilder;
import org.wso2.carbon.identity.action.management.api.model.Action;
import org.wso2.carbon.identity.core.context.IdentityContext;
import org.wso2.carbon.identity.core.context.model.Flow;
import org.wso2.carbon.identity.user.action.api.model.UserActionContext;
import org.wso2.carbon.identity.user.pre.update.profile.action.api.model.PreUpdateProfileAction;
import org.wso2.carbon.identity.user.pre.update.profile.action.api.model.PreUpdateProfileEvent;
import org.wso2.carbon.identity.user.pre.update.profile.action.api.model.PreUpdateProfileRequest;
import org.wso2.carbon.identity.user.pre.update.profile.action.api.model.ProfileUpdatingUser;
import org.wso2.carbon.identity.user.pre.update.profile.action.internal.constant.PreUpdateProfileActionConstants;

import java.util.ArrayList;
import java.util.List;

/**
 * This class is responsible for building the action execution request for the pre update profile action.
 */
public class PreUpdateProfileActionRequestBuilder implements ActionExecutionRequestBuilder {

    private UserActionContext userActionContext;
    private PreUpdateProfileAction preUpdateProfileAction;

    @Override
    public ActionType getSupportedActionType() {

        return ActionType.PRE_UPDATE_PROFILE;
    }

    @Override
    public ActionExecutionRequest buildActionExecutionRequest(FlowContext flowContext,
                                                              ActionExecutionRequestContext actionExecutionContext)
            throws ActionExecutionRequestBuilderException {



        resolveAction(actionExecutionContext);
        UserActionContext userActionContext = resolveUserActionContext(flowContext);
        ActionExecutionRequest.Builder actionRequestBuilder = new ActionExecutionRequest.Builder();
        actionRequestBuilder.actionType(getSupportedActionType());
        actionRequestBuilder.event(getEvent(userActionContext));

        return actionRequestBuilder.build();
    }

    private UserActionContext resolveUserActionContext(FlowContext flowContext) throws ActionExecutionRequestBuilderException {

        Object userContext = flowContext.getContextData().get(PreUpdateProfileActionConstants.USER_ACTION_CONTEXT);
        if (!(userContext instanceof UserActionContext)) {
            throw new ActionExecutionRequestBuilderException("Provided User Action Context is not valid.");
        }
        return (UserActionContext) userContext;
    }

    private void resolveAction(ActionExecutionRequestContext actionExecutionContext)
            throws ActionExecutionRequestBuilderException {

        Action action = actionExecutionContext.getAction();
        if (!(action instanceof PreUpdateProfileAction)) {
            throw new ActionExecutionRequestBuilderException("Provided action is not a Pre Update Profile Action.");

        }
        preUpdateProfileAction = (PreUpdateProfileAction) action;
    }

    private Event getEvent(UserActionContext userActionContext) throws ActionExecutionRequestBuilderException {

        return new PreUpdateProfileEvent.Builder()
                .initiatorType(getInitiatorType())
                .action(getAction())
                .request(getRequest(userActionContext))
                .tenant(getTenant())
                .user(getUser())
                .userStore(new UserStore(userActionContext.getUserStoreDomain()))
                .build();
    }

    private PreUpdateProfileEvent.FlowInitiatorType getInitiatorType() throws ActionExecutionRequestBuilderException {

        Flow flow = IdentityContext.getThreadLocalIdentityContext().getFlow();
        if (flow == null) {
            throw new ActionExecutionRequestBuilderException("Flow is not identified.");
        }

        switch(flow.getInitiatingPersona()) {
            case ADMIN:
                return PreUpdateProfileEvent.FlowInitiatorType.ADMIN;
            case APPLICATION:
                return PreUpdateProfileEvent.FlowInitiatorType.APPLICATION;
            case USER:
                return PreUpdateProfileEvent.FlowInitiatorType.USER;
            default:
                break;
        }
        throw new ActionExecutionRequestBuilderException("Invalid initiator flow.");
    }

    private PreUpdateProfileEvent.Action getAction() throws ActionExecutionRequestBuilderException {

        Flow flow = IdentityContext.getThreadLocalIdentityContext().getFlow();
        if (flow == null) {
            throw new ActionExecutionRequestBuilderException("Flow is not identified.");
        }

        if (flow.getName() == Flow.Name.PROFILE_UPDATE) {
            return PreUpdateProfileEvent.Action.UPDATE;
        }

        throw new ActionExecutionRequestBuilderException("Invalid action flow.");
    }

    private PreUpdateProfileRequest getRequest(UserActionContext userActionContext) {

        return new PreUpdateProfileRequest.Builder()
                .build();
    }

    private static Tenant getTenant() {

        return new Tenant(String.valueOf(IdentityContext.getThreadLocalCarbonContext().getTenantId()),
                IdentityContext.getThreadLocalCarbonContext().getTenantDomain());
    }

    private User getUser() throws ActionExecutionRequestBuilderException {

        List<String> groups = new ArrayList<>();
        groups.add("admin");

        List<String> roles = new ArrayList<>();
        roles.add("manager");

        ProfileUpdatingUser.Builder userBuilder = new ProfileUpdatingUser.Builder()
                .id(userActionContext.getUserId())
                .groups(groups)
                .roles(roles);
        return userBuilder.build();
    }
}
