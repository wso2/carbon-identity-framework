/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */
package org.wso2.carbon.identity.application.authentication.framework;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.application.authentication.framework.context.AuthenticationContext;
import org.wso2.carbon.identity.application.authentication.framework.exception.AuthenticationFailedException;
import org.wso2.carbon.identity.application.authentication.framework.exception.LogoutFailedException;
import org.wso2.carbon.identity.application.authentication.framework.internal.FrameworkServiceDataHolder;
import org.wso2.carbon.identity.application.authentication.framework.util.FrameworkConstants;
import org.wso2.carbon.identity.application.authentication.framework.util.FrameworkUtils;
import org.wso2.carbon.identity.core.model.IdentityErrorMsgContext;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;
import org.wso2.carbon.identity.core.util.IdentityUtil;
import org.wso2.carbon.identity.event.IdentityEventConstants;
import org.wso2.carbon.identity.event.IdentityEventException;
import org.wso2.carbon.identity.event.event.Event;
import org.wso2.carbon.identity.event.services.IdentityEventService;
import org.wso2.carbon.user.api.UserRealm;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.core.UserCoreConstants;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static org.wso2.carbon.identity.application.authentication.framework.util.FrameworkErrorConstants.ErrorMessages;

/**
 * This is the super class to fire the account lock event for all authenticators which are not federated
 * authenticator and are not use user-store to authenticate the user.
 */
public abstract class AbstractLocalApplicationAuthenticator extends AbstractApplicationAuthenticator
        implements ApplicationAuthenticator {

    private static final long serialVersionUID = -4406878411547612129L;
    private static final Log log = LogFactory.getLog(AbstractLocalApplicationAuthenticator.class);

    @Override
    public AuthenticatorFlowStatus process(HttpServletRequest request, HttpServletResponse response,
                                           AuthenticationContext context) throws AuthenticationFailedException,
            LogoutFailedException {

        // if an authentication flow
        if (!context.isLogoutRequest()) {
            if (!canHandle(request)
                    || Boolean.TRUE.equals(request.getAttribute(FrameworkConstants.REQ_ATTR_HANDLED))) {
                context.setRetrying(false);
                return initiateAuthenticationFlow(request, response, context);
            } else {
                try {
                    fireEvent(context, IdentityEventConstants.Event.PRE_AUTHENTICATION, false);
                    processAuthenticationResponse(request, response, context);
                    if (this instanceof LocalApplicationAuthenticator && !context.getSequenceConfig()
                            .getApplicationConfig().isSaaSApp()) {
                        validateNonSaasAppLogin(context);
                    }
                    request.setAttribute(FrameworkConstants.REQ_ATTR_HANDLED, true);
                    context.setProperty(FrameworkConstants.LAST_FAILED_AUTHENTICATOR, null);
                    fireEvent(context, IdentityEventConstants.Event.POST_AUTHENTICATION, true);
                    return AuthenticatorFlowStatus.SUCCESS_COMPLETED;
                } catch (AuthenticationFailedException e) {
                    if (isAccountLocked(context)) {
                        try {
                            FrameworkUtils.sendToRetryPage(request, response, context, FrameworkConstants.
                                    ACCOUNT_LOCKED_MSG, FrameworkConstants.ERROR_MSG);
                        } catch (IOException e1) {
                            throw new AuthenticationFailedException(
                                    ErrorMessages.SYSTEM_ERROR_WHILE_AUTHENTICATING.getCode(),
                                    " Error while redirecting to the retry page ", e1);
                        }
                        return AuthenticatorFlowStatus.INCOMPLETE;
                    }
                    fireEvent(context, IdentityEventConstants.Event.POST_AUTHENTICATION, false);
                    request.setAttribute(FrameworkConstants.REQ_ATTR_HANDLED, true);
                    // Decide whether we need to redirect to the login page to retry authentication.
                    return handleRetryOnFailure(request, response, context, e);
                }
            }
            // else a logout flow
        } else {
            return processLogoutFlow(request, response, context);
        }
    }

    /**
     * To decide whether need to redirect the user to login page to retry authentication.
     *
     * @param request  the httpServletRequest
     * @param response the httpServletResponse
     * @param context  the authentication context
     * @param e        the authentication failed exception
     * @return authentication flow status
     * @throws AuthenticationFailedException the exception in the authentication flow
     */
    protected AuthenticatorFlowStatus handleRetryOnFailure(HttpServletRequest request,
                                                                    HttpServletResponse response,
                                                                    AuthenticationContext context,
                                                                    AuthenticationFailedException e)
            throws AuthenticationFailedException {

        boolean sendToMultiOptionPage =
                isStepHasMultiOption(context) && isRedirectToMultiOptionPageOnFailure();
        if (retryAuthenticationEnabled(context) && !sendToMultiOptionPage) {
            // The Authenticator will re-initiate the authentication and retry.
            context.setRetrying(true);
            return initiateAuthenticationFlow(request, response, context);
        } else {
            context.setProperty(FrameworkConstants.LAST_FAILED_AUTHENTICATOR, getName());
            /*
                By throwing this exception step handler will redirect to multi options page if
                multi-option are available in the step.
             */
            throw e;
        }
    }

    /**
     * To check whether user domain and tenant domain equal for non SaaS application.
     *
     * @param context the authentication context
     * @throws AuthenticationFailedException the exception in the authentication flow
     */
    protected void validateNonSaasAppLogin(AuthenticationContext context) throws AuthenticationFailedException {

        String userTenantDomain = context.getSubject().getTenantDomain();
        String spTenantDomain = context.getTenantDomain();
        if (!StringUtils.equals(userTenantDomain, spTenantDomain)) {
            context.setProperty(FrameworkConstants.USER_TENANT_DOMAIN_MISMATCH, true);
            throw new AuthenticationFailedException("Service Provider tenant domain must be " +
                    "equal to user tenant domain for non-SaaS applications", context.getSubject());
        }
    }

    /**
     * To process the authentication failed flow
     *
     * @param request  the httpServletRequest
     * @param response the httpServletResponse
     * @param context  the authentication context
     * @return authentication flow status
     * @throws AuthenticationFailedException the exception in the authentication flow
     */
    protected AuthenticatorFlowStatus initiateAuthenticationFlow(HttpServletRequest request,
                                                                 HttpServletResponse response,
                                                                 AuthenticationContext context)
            throws AuthenticationFailedException {

        if (getName().equals(context.getProperty(FrameworkConstants.LAST_FAILED_AUTHENTICATOR))) {
            context.setRetrying(true);
        }
        initiateAuthenticationRequest(request, response, context);
        context.setCurrentAuthenticator(getName());
        return AuthenticatorFlowStatus.INCOMPLETE;
    }

    /**
     * To check whether the user's account is being already locked or not.
     *
     * @param context  the authentication context
     * @return true or false
     * @throws AuthenticationFailedException the exception in the authentication flow
     */
    protected boolean isAccountLocked(AuthenticationContext context) throws AuthenticationFailedException {

        String errorCode = getErrorCode();
        if (StringUtils.isNotEmpty(errorCode) && errorCode.equals(UserCoreConstants.ErrorCode
                .USER_IS_LOCKED)) {
            context.setRetrying(true);
            context.setCurrentAuthenticator(getName());
            return true;
        }
        return false;
    }

    /**
     * To process the logout flow.
     *
     * @param request  the httpServletRequest
     * @param response the httpServletResponse
     * @param context  the authentication context
     * @return the authentication flow status
     * @throws LogoutFailedException the exception in logout flow
     */
    protected AuthenticatorFlowStatus processLogoutFlow(HttpServletRequest request, HttpServletResponse response,
                                                        AuthenticationContext context) throws LogoutFailedException {

        try {
            if (!canHandle(request)) {
                context.setCurrentAuthenticator(getName());
                initiateLogoutRequest(request, response, context);
                return AuthenticatorFlowStatus.INCOMPLETE;
            } else {
                processLogoutResponse(request, response, context);
                return AuthenticatorFlowStatus.SUCCESS_COMPLETED;
            }
        } catch (UnsupportedOperationException e) {
            if (log.isDebugEnabled()) {
                log.debug("Ignoring UnsupportedOperationException.", e);
            }
            return AuthenticatorFlowStatus.SUCCESS_COMPLETED;
        }
    }

    /**
     * To fire the events for account locking.
     *
     * @param context         the authentication context
     * @param eventName       the event name
     * @param operationStatus the success or failure status
     * @throws AuthenticationFailedException the exception in the authentication flow
     */
    private void fireEvent(AuthenticationContext context, String eventName, boolean operationStatus)
            throws AuthenticationFailedException {

        if (eventFiringEnabledForAccountLocking()) {
            IdentityEventService eventService = FrameworkServiceDataHolder.getInstance().getIdentityEventService();
            try {
                Map<String, Object> eventProperties = new HashMap<>();
                String userName = (String) context.getProperty(FrameworkConstants.USERNAME);
                String tenantAwareUsername = MultitenantUtils.getTenantAwareUsername(userName);
                String tenantDomain = context.getTenantDomain();
                int tenantID = IdentityTenantUtil.getTenantId(tenantDomain);
                RealmService realmService = FrameworkServiceDataHolder.getInstance().getRealmService();
                UserRealm userRealm = realmService.getTenantUserRealm(tenantID);
                eventProperties.put(IdentityEventConstants.EventProperty.USER_NAME, tenantAwareUsername);
                eventProperties.put(IdentityEventConstants.EventProperty.USER_STORE_MANAGER, userRealm
                        .getUserStoreManager());
                eventProperties.put(IdentityEventConstants.EventProperty.TENANT_DOMAIN, tenantDomain);
                eventProperties.put(IdentityEventConstants.EventProperty.OPERATION_STATUS, operationStatus);
                Event event = new Event(eventName, eventProperties);
                eventService.handleEvent(event);

            } catch (UserStoreException e) {
                throw new AuthenticationFailedException(ErrorMessages.SYSTEM_ERROR_WHILE_AUTHENTICATING.getCode(),
                        " Error in accessing user store ", e);
            } catch (IdentityEventException e) {
                throw new AuthenticationFailedException(ErrorMessages.SYSTEM_ERROR_WHILE_AUTHENTICATING.getCode(),
                        " Error while firing the events ", e);
            }
        }
    }

    /**
     * To get account lock error code from identity error message context.
     *
     * @return the error code
     */
    private String getErrorCode() {

        String errorCode = null;
        IdentityErrorMsgContext errorContext = IdentityUtil.getIdentityErrorMsg();
        IdentityUtil.clearIdentityErrorMsg();
        if (errorContext != null && errorContext.getErrorCode() != null) {
            if (log.isDebugEnabled()) {
                log.debug("Retrieving error code " + errorContext.getErrorCode() + " from identity error message " +
                        "context ");
            }
            errorCode = errorContext.getErrorCode();
        }
        return errorCode;
    }

    /**
     * To check whether the event need to fire for account locking or not.
     *
     * @return true or false
     */
    protected boolean eventFiringEnabledForAccountLocking() {

        return false;
    }
}
