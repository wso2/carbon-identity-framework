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

package org.wso2.carbon.identity.user.pre.update.password.action.internal.versioning.v1;

import org.wso2.carbon.identity.action.execution.api.exception.ActionExecutionRequestBuilderException;
import org.wso2.carbon.identity.action.execution.api.model.ActionExecutionRequest;
import org.wso2.carbon.identity.action.execution.api.model.ActionExecutionRequestContext;
import org.wso2.carbon.identity.action.execution.api.model.ActionType;
import org.wso2.carbon.identity.action.execution.api.model.Event;
import org.wso2.carbon.identity.action.execution.api.model.FlowContext;
import org.wso2.carbon.identity.action.execution.api.model.User;
import org.wso2.carbon.identity.action.execution.api.model.UserStore;
import org.wso2.carbon.identity.action.execution.api.service.ActionExecutionRequestBuilder;
import org.wso2.carbon.identity.application.authentication.framework.util.FrameworkUtils;
import org.wso2.carbon.identity.certificate.management.model.Certificate;
import org.wso2.carbon.identity.core.context.IdentityContext;
import org.wso2.carbon.identity.core.context.model.Flow;
import org.wso2.carbon.identity.core.util.IdentityUtil;
import org.wso2.carbon.identity.user.action.api.model.UserActionContext;
import org.wso2.carbon.identity.user.pre.update.password.action.api.model.PreUpdatePasswordAction;
import org.wso2.carbon.identity.user.pre.update.password.action.internal.versioning.common.model.PasswordUpdatingUser;
import org.wso2.carbon.identity.user.pre.update.password.action.internal.versioning.common.util.PreUpdatePasswordRequestBuilderUtil;
import org.wso2.carbon.identity.user.pre.update.password.action.internal.versioning.v1.model.PreUpdatePasswordEvent;

import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import static org.wso2.carbon.identity.user.pre.update.password.action.internal.constant.PreUpdatePasswordActionConstants.GROUP_CLAIM_URI;

/**
 * This class is responsible for building the action execution request for the pre update password action.
 */
public class PreUpdatePasswordRequestBuilderV1 implements ActionExecutionRequestBuilder {

    @Override
    public ActionType getSupportedActionType() {

        return ActionType.PRE_UPDATE_PASSWORD;
    }

    @Override
    public ActionExecutionRequest buildActionExecutionRequest(FlowContext flowContext,
                                                              ActionExecutionRequestContext actionExecutionContext)
            throws ActionExecutionRequestBuilderException {

        PreUpdatePasswordAction preUpdatePasswordAction = PreUpdatePasswordRequestBuilderUtil.resolveAction(
                actionExecutionContext);
        UserActionContext userActionContext = PreUpdatePasswordRequestBuilderUtil.resolveUserActionContext(flowContext);

        ActionExecutionRequest.Builder actionRequestBuilder = new ActionExecutionRequest.Builder();
        actionRequestBuilder.actionType(getSupportedActionType());
        actionRequestBuilder.event(getEvent(userActionContext, preUpdatePasswordAction));

        return actionRequestBuilder.build();
    }

    private Event getEvent(UserActionContext userActionContext, PreUpdatePasswordAction preUpdatePasswordAction)
            throws ActionExecutionRequestBuilderException {

        return new PreUpdatePasswordEvent.Builder()
                .initiatorType(getInitiatorType())
                .action(getAction())
                .tenant(PreUpdatePasswordRequestBuilderUtil.getTenant())
                .user(getUser(userActionContext, preUpdatePasswordAction))
                .userStore(new UserStore(userActionContext.getUserActionRequestDTO().getUserStoreDomain()))
                .organization(PreUpdatePasswordRequestBuilderUtil.getOrganization())
                .build();
    }

    private PreUpdatePasswordEvent.FlowInitiatorType getInitiatorType() throws ActionExecutionRequestBuilderException {

        Flow flow = IdentityContext.getThreadLocalIdentityContext().getCurrentFlow();
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

        Flow flow = IdentityContext.getThreadLocalIdentityContext().getCurrentFlow();
        if (flow == null) {
            throw new ActionExecutionRequestBuilderException("Flow is not identified.");
        }

        switch (flow.getName()) {
            case PROFILE_UPDATE:
            case CREDENTIAL_UPDATE:
                // Password update is a sub-flow of profile update.
                return PreUpdatePasswordEvent.Action.UPDATE;
            case CREDENTIAL_RESET:
                return PreUpdatePasswordEvent.Action.RESET;
            case INVITE:
            case INVITED_USER_REGISTRATION:
                return PreUpdatePasswordEvent.Action.INVITE;
            default:
                break;
        }
        throw new ActionExecutionRequestBuilderException("Invalid action flow.");
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
        if (PreUpdatePasswordRequestBuilderUtil.isEncryptionRequired(certificate)) {
            try {
                X509Certificate publicCert = (X509Certificate)
                        IdentityUtil.convertPEMEncodedContentToCertificate(certificate.getCertificateContent());
                userBuilder.updatingCredential(PreUpdatePasswordRequestBuilderUtil.getUnEncryptedCredential(
                        userActionContext, preUpdatePasswordAction), true, publicCert);
            } catch (CertificateException e) {
                throw new ActionExecutionRequestBuilderException("Error while building X509 certificate.", e);
            }
        } else {
            userBuilder.updatingCredential(PreUpdatePasswordRequestBuilderUtil.getUnEncryptedCredential(
                    userActionContext, preUpdatePasswordAction), false, null);
        }
    }

    private void populateClaims(PasswordUpdatingUser.Builder userBuilder, UserActionContext userActionContext,
                                PreUpdatePasswordAction action) throws ActionExecutionRequestBuilderException {

        List<String> userClaimsToSetInEvent = action.getAttributes();
        if (userClaimsToSetInEvent == null || userClaimsToSetInEvent.isEmpty()) {
            return;
        }

        String multiAttributeSeparator = FrameworkUtils.getMultiAttributeSeparator();
        Map<String, String> claimValues = new HashMap<>();
        if (userActionContext.getUserActionRequestDTO().getUserId() == null
                || PreUpdatePasswordRequestBuilderUtil.getCurrentFlowName() == Flow.Name.REGISTER) {
            // User id is not available during the user registration where the user is not yet created.
            // In such cases, UserActionContext contains the creating user claims.
            Map<String, Object> claimsFromUserContext = userActionContext.getUserActionRequestDTO().getClaims();
            for (String claim : userClaimsToSetInEvent) {
                if (claimsFromUserContext.containsKey(claim) && claimsFromUserContext.get(claim) instanceof String) {
                    claimValues.put(claim, (String) claimsFromUserContext.get(claim));
                }
            }
        } else {
            claimValues = PreUpdatePasswordRequestBuilderUtil.getClaimValues(
                    userActionContext.getUserActionRequestDTO().getUserId(),
                    userClaimsToSetInEvent);
        }

        setClaimsInUserBuilder(userBuilder, claimValues, multiAttributeSeparator);
        setGroupsInUserBuilder(userBuilder, claimValues, multiAttributeSeparator);
    }

    private void setClaimsInUserBuilder(PasswordUpdatingUser.Builder userBuilder, Map<String, String> claimValues,
                                        String multiAttributeSeparator) throws ActionExecutionRequestBuilderException {

        userBuilder.claims(
                PreUpdatePasswordRequestBuilderUtil.setClaimsInUserBuilder(claimValues, multiAttributeSeparator));
    }

    private void setGroupsInUserBuilder(PasswordUpdatingUser.Builder userBuilder, Map<String, String> claimValues,
                                        String multiAttributeSeparator) {

        if (claimValues.get(GROUP_CLAIM_URI) != null) {
            userBuilder.groups(Arrays.asList(
                    claimValues.get(GROUP_CLAIM_URI).split(Pattern.quote(multiAttributeSeparator))));
        }
    }

}
