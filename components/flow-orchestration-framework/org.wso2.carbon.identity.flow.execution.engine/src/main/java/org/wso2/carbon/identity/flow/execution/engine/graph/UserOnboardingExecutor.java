/*
 * Copyright (c) 2025, WSO2 LLC. (https://www.wso2.com) All Rights Reserved.
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.identity.flow.execution.engine.graph;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.application.common.model.User;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;
import org.wso2.carbon.identity.core.util.IdentityUtil;
import org.wso2.carbon.identity.core.util.LambdaExceptionUtils;
import org.wso2.carbon.identity.flow.execution.engine.Constants;
import org.wso2.carbon.identity.flow.execution.engine.Constants.ErrorMessages;
import org.wso2.carbon.identity.flow.execution.engine.exception.FlowEngineClientException;
import org.wso2.carbon.identity.flow.execution.engine.exception.FlowEngineException;
import org.wso2.carbon.identity.flow.execution.engine.internal.FlowExecutionEngineDataHolder;
import org.wso2.carbon.identity.flow.execution.engine.model.ExecutorResponse;
import org.wso2.carbon.identity.flow.execution.engine.model.FlowExecutionContext;
import org.wso2.carbon.identity.flow.execution.engine.model.FlowUser;
import org.wso2.carbon.identity.flow.execution.engine.util.FlowExecutionEngineUtils;
import org.wso2.carbon.identity.user.action.api.constant.UserActionError;
import org.wso2.carbon.identity.user.action.api.exception.UserActionExecutionClientException;
import org.wso2.carbon.identity.user.profile.mgt.association.federation.FederatedAssociationManager;
import org.wso2.carbon.identity.user.profile.mgt.association.federation.exception.FederatedAssociationManagerException;
import org.wso2.carbon.user.api.UserRealm;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.core.UserCoreConstants;
import org.wso2.carbon.user.core.UserStoreClientException;
import org.wso2.carbon.user.core.UserStoreManager;
import org.wso2.carbon.user.core.common.AbstractUserStoreManager;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.user.core.util.UserCoreUtil;
import org.wso2.carbon.user.mgt.common.DefaultPasswordGenerator;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static java.util.Locale.ENGLISH;
import static org.wso2.carbon.identity.application.authentication.framework.util.FrameworkConstants.EMAIL_ADDRESS_CLAIM;
import static org.wso2.carbon.identity.flow.execution.engine.Constants.ErrorMessages.ERROR_CODE_INVALID_USERNAME;
import static org.wso2.carbon.identity.flow.execution.engine.Constants.ErrorMessages.ERROR_CODE_USERNAME_ALREADY_EXISTS;
import static org.wso2.carbon.identity.flow.execution.engine.Constants.ErrorMessages.ERROR_CODE_USERSTORE_MANAGER_FAILURE;
import static org.wso2.carbon.identity.flow.execution.engine.Constants.ErrorMessages.ERROR_CODE_PRE_UPDATE_PASSWORD_ACTION_VALIDATION_FAILURE;
import static org.wso2.carbon.identity.flow.execution.engine.Constants.ErrorMessages.ERROR_CODE_USER_ONBOARD_FAILURE;
import static org.wso2.carbon.identity.flow.execution.engine.Constants.ExecutorStatus.USER_ALREADY_EXISTING_USERNAME;
import static org.wso2.carbon.identity.flow.execution.engine.Constants.PASSWORD_KEY;
import static org.wso2.carbon.identity.flow.execution.engine.Constants.SELF_REGISTRATION_DEFAULT_USERSTORE_CONFIG;
import static org.wso2.carbon.identity.flow.execution.engine.Constants.STATUS_COMPLETE;
import static org.wso2.carbon.identity.flow.execution.engine.Constants.USERNAME_CLAIM_URI;
import static org.wso2.carbon.identity.flow.execution.engine.util.FlowExecutionEngineUtils.handleClientException;
import static org.wso2.carbon.identity.flow.execution.engine.util.FlowExecutionEngineUtils.handleServerException;
import static org.wso2.carbon.identity.flow.mgt.Constants.ExecutorTypes.USER_ONBOARDING;
import static org.wso2.carbon.identity.flow.mgt.Constants.FlowTypes.REGISTRATION;
import static org.wso2.carbon.user.core.UserCoreConstants.APPLICATION_DOMAIN;
import static org.wso2.carbon.user.core.UserCoreConstants.INTERNAL_DOMAIN;
import static org.wso2.carbon.user.core.UserCoreConstants.WORKFLOW_DOMAIN;

/**
 * Implementation of the executor which handles onboarding the user to the system.
 */
public class UserOnboardingExecutor implements Executor {

    private static final Log LOG = LogFactory.getLog(UserOnboardingExecutor.class);
    private static final String WSO2_CLAIM_DIALECT = "http://wso2.org/claims/";
    private static final String USERNAME_PATTERN_VALIDATION_SKIPPED = "isUsernamePatternValidationSkipped";

    @Override
    public String getName() {

        return USER_ONBOARDING;
    }

    @Override
    public List<String> getInitiationData() {

        LOG.debug("Initiation data is not required for the executor: " + getName());
        return null;
    }

    @Override
    public ExecutorResponse rollback(FlowExecutionContext context) throws FlowEngineException {

        return null;
    }

    @Override
    public ExecutorResponse execute(FlowExecutionContext context) {

        char[] password = null;
        ExecutorResponse response = new ExecutorResponse();
        try {
            FlowUser user = updateUserProfile(context);
            Map<String, String> userClaims = user.getClaims();
            Map<String, char[]> credentials = user.getUserCredentials();
            password =
                    credentials.getOrDefault(PASSWORD_KEY, new DefaultPasswordGenerator().generatePassword());

            String userStoreDomainName = resolveUserStoreDomain(user.getUsername());
            UserStoreManager userStoreManager = getUserStoreManager(context.getTenantDomain(), userStoreDomainName,
                    context.getContextIdentifier(), context.getFlowType());
            userStoreManager.addUser(IdentityUtil.addDomainToName(user.getUsername(), userStoreDomainName),
                    String.valueOf(password), null, userClaims, null);
            String userid = ((AbstractUserStoreManager) userStoreManager).getUserIDFromUserName(user.getUsername());
            user.setUserStoreDomain(userStoreDomainName);
            user.setUserId(userid);
            createFederatedAssociations(user, context.getTenantDomain());
            if (LOG.isDebugEnabled()) {
                LOG.debug("User: " + user.getUsername() + " successfully onboarded in user store: " +
                        userStoreDomainName + " of tenant: " + context.getTenantDomain());
            }
            response.setResult(STATUS_COMPLETE);
            return response;
        } catch (UserStoreException e) {
            response = handleAndThrowClientExceptionForActionFailure(response, e);
            if (response.getResult() != null) {
                return response;
            }
            if (e.getMessage().contains(USER_ALREADY_EXISTING_USERNAME)) {
                return userErrorResponse(response, ERROR_CODE_USERNAME_ALREADY_EXISTS, context.getTenantDomain());
            }
            return errorResponse(response, ERROR_CODE_USER_ONBOARD_FAILURE, e, context.getFlowUser().getUsername(),
                    context.getContextIdentifier());
        } catch (FlowEngineClientException e) {
            return userErrorResponse(response, e);
        } catch (FlowEngineException e) {
            return errorResponse(response, e);
        } finally {
            if (password != null) {
                Arrays.fill(password, '\0');
            }
        }
    }

    private ExecutorResponse handleAndThrowClientExceptionForActionFailure(ExecutorResponse response, UserStoreException e) {

        if (e instanceof UserStoreClientException &&
                UserActionError.PRE_UPDATE_PASSWORD_ACTION_EXECUTION_FAILED
                        .equals(((UserStoreClientException) e).getErrorCode())) {
            Throwable cause = e.getCause();
            while (cause != null) {
                if (cause instanceof UserActionExecutionClientException) {
                    return userErrorResponse(response,
                            ERROR_CODE_PRE_UPDATE_PASSWORD_ACTION_VALIDATION_FAILURE.getCode(),
                            ((UserActionExecutionClientException) cause).getError(),
                            ((UserActionExecutionClientException) cause).getDescription(), cause);
                }
                cause = cause.getCause();
            }
        }
        return response;
    }

    private FlowUser updateUserProfile(FlowExecutionContext context) throws FlowEngineException {

        FlowUser user = context.getFlowUser();
        context.getUserInputData().forEach((key, value) -> {
            if (key.startsWith(WSO2_CLAIM_DIALECT)) {
                if (!user.getClaims().containsKey(key)) {
                    user.addClaim(key, value);
                }
            }
        });
        user.setUsername(resolveUsername(user, context.getTenantDomain()));
        setUsernamePatternValidation(context);
        return user;
    }

    private static void setUsernamePatternValidation(FlowExecutionContext context) {

        Boolean isUsernamePatternValidationSkipped = (Boolean) context.getProperty(USERNAME_PATTERN_VALIDATION_SKIPPED);
        if (isUsernamePatternValidationSkipped == null || !isUsernamePatternValidationSkipped ) {
            return;
        }
        UserCoreUtil.setSkipUsernamePatternValidationThreadLocal(true);
    }

    private UserStoreManager getUserStoreManager(String tenantDomain, String userdomain, String flowId, String flowType)
            throws FlowEngineException {

        RealmService realmService = FlowExecutionEngineDataHolder.getInstance().getRealmService();
        UserStoreManager userStoreManager;
        try {
            UserRealm tenantUserRealm = realmService.getTenantUserRealm(IdentityTenantUtil.getTenantId(tenantDomain));
            if (IdentityUtil.getPrimaryDomainName().equals(userdomain)) {
                userStoreManager = (UserStoreManager) tenantUserRealm.getUserStoreManager();
            } else {
                userStoreManager =
                        ((UserStoreManager) tenantUserRealm.getUserStoreManager()).getSecondaryUserStoreManager(userdomain);
            }
            if (userStoreManager == null) {
                throw handleServerException(flowType, ERROR_CODE_USERSTORE_MANAGER_FAILURE, tenantDomain, flowType,
                        flowId);
            }
            return userStoreManager;
        } catch (UserStoreException e) {
            throw handleServerException(flowType, ERROR_CODE_USERSTORE_MANAGER_FAILURE, e, tenantDomain, flowType,
                    flowId);
        }
    }

    private String resolveUserStoreDomain(String username) {

        int separatorIndex = username.indexOf(UserCoreConstants.DOMAIN_SEPARATOR);
        if (separatorIndex >= 0) {
            String domain = username.substring(0, separatorIndex);
            if (INTERNAL_DOMAIN.equalsIgnoreCase(domain) || WORKFLOW_DOMAIN.equalsIgnoreCase(domain)
                    || APPLICATION_DOMAIN.equalsIgnoreCase(domain)) {
                return domain.substring(0, 1).toUpperCase(ENGLISH) + domain.substring(1).toLowerCase(ENGLISH);
            }
            return domain.toUpperCase(ENGLISH);
        }

        String domainName = IdentityUtil.getProperty(SELF_REGISTRATION_DEFAULT_USERSTORE_CONFIG);
        return domainName != null ? domainName.toUpperCase(ENGLISH) :
                IdentityUtil.getPrimaryDomainName().toUpperCase(ENGLISH);
    }

    private String resolveUsername(FlowUser user, String tenantDomain) throws FlowEngineException {

        String username = Optional.ofNullable(user.getClaims().get(USERNAME_CLAIM_URI)).orElse("");
        if (StringUtils.isBlank(username)) {
            if ((FlowExecutionEngineUtils.isEmailUsernameValidator(tenantDomain) ||
                    IdentityUtil.isEmailUsernameEnabled())
                    && StringUtils.isNotBlank((String) user.getClaim(EMAIL_ADDRESS_CLAIM))) {
                // If email format validation is enabled and username is not provided, use email as username.
                return (String) user.getClaim(EMAIL_ADDRESS_CLAIM);
            }
            // Else generate a random UUID as the username and set the skip validation flag.
            username = UUID.randomUUID().toString();
            UserCoreUtil.setSkipUsernamePatternValidationThreadLocal(true);
            return username;
        } else if (IdentityUtil.isEmailUsernameEnabled() && !username.contains("@")) {
            // Assuming the flow is REGISTRATION, as this is user onboarding executor.
            throw handleClientException(REGISTRATION.getType(), ERROR_CODE_INVALID_USERNAME, username);
        }
        return username;
    }

    private void createFederatedAssociations(FlowUser user, String tenantDomain) {

        if (user.getFederatedAssociations().isEmpty()) {
            return;
        }
        FederatedAssociationManager fedAssociationManager =
                FlowExecutionEngineDataHolder.getInstance().getFederatedAssociationManager();
        user.getFederatedAssociations().forEach(LambdaExceptionUtils.rethrowBiConsumer((idpName, idpSubjectId) -> {
            if (StringUtils.isNotBlank(idpName) && StringUtils.isNotBlank(idpSubjectId)) {
                try {
                    User localUser = new User();
                    localUser.setUserName(user.getUsername());
                    localUser.setTenantDomain(tenantDomain);
                    localUser.setUserStoreDomain(user.getUserStoreDomain());
                    fedAssociationManager.createFederatedAssociation(localUser, idpName, idpSubjectId);
                } catch (FederatedAssociationManagerException e) {
                    LOG.error("Error while creating federated association for user: " + user.getUsername()
                            + " with IdP: " + idpName + " and subject ID: " + idpSubjectId, e);
                    // Assuming the flow is REGISTRATION, as this is user onboarding executor.
                    throw handleServerException(REGISTRATION.getType(), ERROR_CODE_USER_ONBOARD_FAILURE, e,
                            user.getUsername(), user.getUserStoreDomain(), tenantDomain);
                }
            }
        }));
    }

    /**
     * Creates an error response with the provided details.
     *
     * @param response ExecutorResponse to be modified with error details.
     * @param error    Error message enum to be set in the response.
     * @param data     Optional data to format the error description.
     * @return Modified ExecutorResponse with error details.
     */
    private ExecutorResponse errorResponse(ExecutorResponse response, ErrorMessages error, Throwable e, Object... data) {

        String description = error.getDescription();
        if (ArrayUtils.isNotEmpty(data)) {
            description = String.format(description, data);
        }
        response.setErrorCode(error.getCode());
        response.setErrorMessage(error.getMessage());
        response.setErrorDescription(description);
        response.setThrowable(e);
        response.setResult(Constants.ExecutorStatus.STATUS_ERROR);
        return response;
    }

    /**
     * Creates an error response with the provided FlowEngineException details.
     *
     * @param response            ExecutorResponse to be modified with error details.
     * @param flowEngineException FlowEngineException containing error details.
     * @return Modified ExecutorResponse with error details.
     */
    private ExecutorResponse errorResponse(ExecutorResponse response, FlowEngineException flowEngineException) {

        response.setErrorMessage(flowEngineException.getMessage());
        response.setErrorCode(flowEngineException.getErrorCode());
        response.setErrorDescription(flowEngineException.getDescription());
        response.setThrowable(flowEngineException);
        response.setResult(Constants.ExecutorStatus.STATUS_ERROR);
        return response;
    }

    /**
     * Creates an error response with the provided details.
     *
     * @param response ExecutorResponse to be modified with error details.
     * @param error    Error message enum to be set in the response.
     * @param data     Optional data to format the error description.
     * @return Modified ExecutorResponse with error details.
     */
    private ExecutorResponse userErrorResponse(ExecutorResponse response, ErrorMessages error, Object... data) {

        String description = error.getDescription();
        if (ArrayUtils.isNotEmpty(data)) {
            description = String.format(description, data);
        }
        response.setErrorCode(error.getCode());
        response.setErrorMessage(error.getMessage());
        response.setErrorDescription(description);
        response.setResult(Constants.ExecutorStatus.STATUS_USER_ERROR);
        return response;
    }

    /**
     * Creates a user error response with the provided details.
     *
     * @param response    ExecutorResponse to be modified with user error details.
     * @param errorCode   Error code to be set in the response.
     * @param message     User error message to be set in the response.
     * @param description Description of the error to be set in the response.
     * @param throwable   Throwable associated with the error.
     * @return Modified ExecutorResponse with user error details.
     */
    private ExecutorResponse userErrorResponse(ExecutorResponse response, String errorCode, String message,
                                               String description, Throwable throwable) {

        response.setErrorCode(errorCode);
        response.setErrorMessage(message);
        response.setErrorDescription(description);
        response.setThrowable(throwable);
        response.setResult(Constants.ExecutorStatus.STATUS_USER_ERROR);
        return response;
    }

    /**
     * Creates a user error response with the provided FlowEngineException details.
     *
     * @param response            ExecutorResponse to be modified with user error details.
     * @param flowEngineException FlowEngineException containing error details.
     * @return Modified ExecutorResponse with user error details.
     */
    private ExecutorResponse userErrorResponse(ExecutorResponse response, FlowEngineException flowEngineException) {

        response.setErrorCode(flowEngineException.getErrorCode());
        response.setErrorMessage(flowEngineException.getMessage());
        response.setErrorDescription(flowEngineException.getDescription());
        response.setThrowable(flowEngineException);
        response.setResult(Constants.ExecutorStatus.STATUS_USER_ERROR);
        return response;
    }
}
