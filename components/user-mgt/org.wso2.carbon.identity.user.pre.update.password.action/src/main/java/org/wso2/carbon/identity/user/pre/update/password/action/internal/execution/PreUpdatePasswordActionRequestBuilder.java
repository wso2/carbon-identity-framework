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
import org.wso2.carbon.identity.action.execution.api.model.Tenant;
import org.wso2.carbon.identity.action.execution.api.model.User;
import org.wso2.carbon.identity.action.execution.api.model.UserStore;
import org.wso2.carbon.identity.action.execution.api.service.ActionExecutionRequestBuilder;
import org.wso2.carbon.identity.action.management.api.model.Action;
import org.wso2.carbon.identity.certificate.management.model.Certificate;
import org.wso2.carbon.identity.core.context.IdentityContext;
import org.wso2.carbon.identity.core.context.model.Flow;
import org.wso2.carbon.identity.core.util.IdentityUtil;
import org.wso2.carbon.identity.user.action.api.model.UserActionContext;
import org.wso2.carbon.identity.user.pre.update.password.action.api.model.PasswordSharing;
import org.wso2.carbon.identity.user.pre.update.password.action.api.model.PreUpdatePasswordAction;
import org.wso2.carbon.identity.user.pre.update.password.action.internal.constant.PreUpdatePasswordActionConstants;
import org.wso2.carbon.identity.user.pre.update.password.action.internal.model.Credential;
import org.wso2.carbon.identity.user.pre.update.password.action.internal.model.PasswordUpdatingUser;
import org.wso2.carbon.identity.user.pre.update.password.action.internal.model.PreUpdatePasswordEvent;
import org.wso2.carbon.utils.Secret;
import org.wso2.carbon.utils.UnsupportedSecretTypeException;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

/**
 * This class is responsible for building the action execution request for the pre update password action.
 */
public class PreUpdatePasswordActionRequestBuilder implements ActionExecutionRequestBuilder {

    private UserActionContext userActionContext;
    private PreUpdatePasswordAction preUpdatePasswordAction;

    @Override
    public ActionType getSupportedActionType() {

        return ActionType.PRE_UPDATE_PASSWORD;
    }

    @Override
    public ActionExecutionRequest buildActionExecutionRequest(FlowContext flowContext,
                                                              ActionExecutionRequestContext actionExecutionContext)
            throws ActionExecutionRequestBuilderException {

        resolveAction(actionExecutionContext);
        resolveUserActionContext(flowContext);
        ActionExecutionRequest.Builder actionRequestBuilder = new ActionExecutionRequest.Builder();
        actionRequestBuilder.actionType(getSupportedActionType());
        actionRequestBuilder.event(getEvent());

        return actionRequestBuilder.build();
    }

    private void resolveUserActionContext(FlowContext flowContext) throws ActionExecutionRequestBuilderException {

        Object userContext = flowContext.getContextData().get(PreUpdatePasswordActionConstants.USER_ACTION_CONTEXT);
        if (!(userContext instanceof UserActionContext)) {
            throw new ActionExecutionRequestBuilderException("Provided User Action Context is not valid.");
        }
        userActionContext = (UserActionContext) userContext;
    }

    private void resolveAction(ActionExecutionRequestContext actionExecutionContext)
            throws ActionExecutionRequestBuilderException {

        Action action = actionExecutionContext.getAction();
        if (!(action instanceof PreUpdatePasswordAction)) {
            throw new ActionExecutionRequestBuilderException("Provided action is not a Pre Update Password Action.");

        }
        preUpdatePasswordAction = (PreUpdatePasswordAction) action;
    }

    private Event getEvent() throws ActionExecutionRequestBuilderException {

        return new PreUpdatePasswordEvent.Builder()
                .initiatorType(getInitiatorType())
                .action(getAction())
                .tenant(getTenant())
                .user(getUser())
                .userStore(new UserStore(userActionContext.getUserStoreDomain()))
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
                return PreUpdatePasswordEvent.Action.INVITE;
            default:
                break;
        }
        throw new ActionExecutionRequestBuilderException("Invalid action flow.");
    }

    private static Tenant getTenant() {

        return new Tenant(String.valueOf(IdentityContext.getThreadLocalCarbonContext().getTenantId()),
                IdentityContext.getThreadLocalCarbonContext().getTenantDomain());
    }

    private User getUser() throws ActionExecutionRequestBuilderException {

        PasswordUpdatingUser.Builder userBuilder = new PasswordUpdatingUser.Builder()
                .id(userActionContext.getUserId());
        populateCredential(userBuilder);
        return userBuilder.build();
    }

    private void populateCredential(PasswordUpdatingUser.Builder userBuilder)
            throws ActionExecutionRequestBuilderException {

        Certificate certificate = preUpdatePasswordAction.getPasswordSharing().getCertificate();
        if (isEncryptionRequired(certificate)) {
            try {
                X509Certificate publicCert = (X509Certificate)
                        IdentityUtil.convertPEMEncodedContentToCertificate(certificate.getCertificateContent());
                userBuilder.updatingCredential(getUnEncryptedCredential(), true, publicCert);
            } catch (CertificateException e) {
                throw new ActionExecutionRequestBuilderException("Error while building X509 certificate.", e);
            }
        } else {
            userBuilder.updatingCredential(getUnEncryptedCredential(), false, null);
        }
    }

    private Credential getUnEncryptedCredential() throws ActionExecutionRequestBuilderException {

        PasswordSharing.Format passwordSharingFormat = preUpdatePasswordAction.getPasswordSharing().getFormat();
        if (PasswordSharing.Format.SHA256_HASHED.equals(passwordSharingFormat)) {
            Secret credentialObj;
            try {
                credentialObj = Secret.getSecret(userActionContext.getPassword());
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
                    .value(userActionContext.getPassword())
                    .build();
        }

        return null;
    }

    private boolean isEncryptionRequired(Certificate certificate) {

        return certificate != null && StringUtils.isNotEmpty(certificate.getCertificateContent());
    }
}
