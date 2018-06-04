/*
 * Copyright (c) 2013, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
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

package org.wso2.carbon.identity.application.authentication.framework;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.identity.application.authentication.framework.config.ConfigurationFacade;
import org.wso2.carbon.identity.application.authentication.framework.config.builder.FileBasedConfigurationBuilder;
import org.wso2.carbon.identity.application.authentication.framework.config.model.AuthenticatorConfig;
import org.wso2.carbon.identity.application.authentication.framework.config.model.SequenceConfig;
import org.wso2.carbon.identity.application.authentication.framework.config.model.StepConfig;
import org.wso2.carbon.identity.application.authentication.framework.config.model.graph.AuthenticationGraph;
import org.wso2.carbon.identity.application.authentication.framework.context.AuthenticationContext;
import org.wso2.carbon.identity.application.authentication.framework.exception.AuthenticationFailedException;
import org.wso2.carbon.identity.application.authentication.framework.exception.LogoutFailedException;
import org.wso2.carbon.identity.application.authentication.framework.internal.FrameworkServiceDataHolder;
import org.wso2.carbon.identity.application.authentication.framework.util.FrameworkConstants;
import org.wso2.carbon.identity.application.common.model.Property;
import org.wso2.carbon.identity.application.common.model.User;
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
import org.wso2.carbon.user.core.util.UserCoreUtil;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static org.wso2.carbon.identity.application.common.util.IdentityApplicationConstants.REDIRECT_TO_MULTI_OPTION_PAGE_ON_FAILURE;

/**
 * This is the super class of all the Application Authenticators. Authenticator writers must extend
 * this.
 */
public abstract class AbstractApplicationAuthenticator implements ApplicationAuthenticator {

    private static final long serialVersionUID = -4406878411547612129L;
    private static final Log log = LogFactory.getLog(AbstractApplicationAuthenticator.class);

    @Override
    public AuthenticatorFlowStatus process(HttpServletRequest request,
                                           HttpServletResponse response, AuthenticationContext context)
            throws AuthenticationFailedException, LogoutFailedException {

        // if an authentication flow
        if (!context.isLogoutRequest()) {
            if (!canHandle(request)
                    || Boolean.TRUE.equals(request.getAttribute(FrameworkConstants.REQ_ATTR_HANDLED))) {
                if (getName().equals(context.getProperty(FrameworkConstants.LAST_FAILED_AUTHENTICATOR))) {
                    context.setRetrying(true);
                }
                initiateAuthenticationRequest(request, response, context);
                context.setCurrentAuthenticator(getName());
                context.setRetrying(false);
                return AuthenticatorFlowStatus.INCOMPLETE;
            } else {
                try {
                    if (eventFiringEnabled() && context.getProperty(FrameworkConstants.USERNAME) != null) {
                        String eventName = IdentityEventConstants.Event.PRE_AUTHENTICATION;
                        fireEvent(context, eventName, false);
                    }
                    processAuthenticationResponse(request, response, context);
                    if (this instanceof LocalApplicationAuthenticator) {
                        if (!context.getSequenceConfig().getApplicationConfig().isSaaSApp()) {
                            String userDomain = context.getSubject().getTenantDomain();
                            String tenantDomain = context.getTenantDomain();
                            if (!StringUtils.equals(userDomain, tenantDomain)) {
                                context.setProperty("UserTenantDomainMismatch", true);
                                throw new AuthenticationFailedException("Service Provider tenant domain must be " +
                                        "equal to user tenant domain for non-SaaS applications", context.getSubject());
                            }
                        }
                    }
                    request.setAttribute(FrameworkConstants.REQ_ATTR_HANDLED, true);
                    context.setProperty(FrameworkConstants.LAST_FAILED_AUTHENTICATOR, null);
                    publishAuthenticationStepAttempt(request, context, context.getSubject(), true);
                    return AuthenticatorFlowStatus.SUCCESS_COMPLETED;
                } catch (AuthenticationFailedException e) {
                    String retryPage = ConfigurationFacade.getInstance().getAuthenticationEndpointRetryURL();
                    String queryParams = context.getContextIdIncludedQueryParams();
                    String errorCode = getErrorCode();
                    if (StringUtils.isNotEmpty(errorCode) && errorCode.equals(UserCoreConstants.ErrorCode
                            .USER_IS_LOCKED)) {
                        context.setRetrying(true);
                        context.setCurrentAuthenticator(getName());
                        try {
                            String redirectUrl = response.encodeRedirectURL(retryPage + ("?" + queryParams)) +
                                    FrameworkConstants.STATUS_MSG + FrameworkConstants.ERROR_MSG +
                                    FrameworkConstants.STATUS + FrameworkConstants.ACCOUNT_LOCKED_MSG;
                            response.sendRedirect(redirectUrl);
                        } catch (IOException e1) {
                            throw new AuthenticationFailedException(" Error while redirecting to the retry page ", e1);
                        }
                        return AuthenticatorFlowStatus.INCOMPLETE;
                    }
                    publishAuthenticationStepAttempt(request, context, e.getUser(), false);
                    request.setAttribute(FrameworkConstants.REQ_ATTR_HANDLED, true);
                    // Decide whether we need to redirect to the login page to retry authentication.
                    boolean sendToMultiOptionPage =
                            isStepHasMultiOption(context) && isRedirectToMultiOptionPageOnFailure();
                    if (retryAuthenticationEnabled(context) && !sendToMultiOptionPage) {
                        // The Authenticator will re-initiate the authentication and retry.
                        context.setRetrying(true);
                        context.setCurrentAuthenticator(getName());
                        initiateAuthenticationRequest(request, response, context);
                        return AuthenticatorFlowStatus.INCOMPLETE;
                    } else {
                        context.setProperty(FrameworkConstants.LAST_FAILED_AUTHENTICATOR, getName());
                        // By throwing this exception step handler will redirect to multi options page if
                        // multi-option are available in the step.
                        throw e;
                    }
                }
            }
            // if a logout flow
        } else {
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
    }

    protected boolean retryAuthenticationEnabled(AuthenticationContext context) {
        SequenceConfig sequenceConfig = context.getSequenceConfig();
        AuthenticationGraph graph = sequenceConfig.getAuthenticationGraph();
        if (graph == null || !graph.isEnabled()) {
            return retryAuthenticationEnabled();
        }
        return false;
    }

    private boolean isStepHasMultiOption(AuthenticationContext context) {
        Map<Integer, StepConfig> stepMap = context.getSequenceConfig().getStepMap();
        boolean stepHasMultiOption = false;

        if (stepMap != null && !stepMap.isEmpty()) {
            StepConfig stepConfig = stepMap.get(context.getCurrentStep());

            if (stepConfig != null) {
                stepHasMultiOption = stepConfig.isMultiOption();
            }
        }
        return stepHasMultiOption;
    }

    private void publishAuthenticationStepAttempt(HttpServletRequest request, AuthenticationContext context,
                                                  User user, boolean success) throws AuthenticationFailedException {

        AuthenticationDataPublisher authnDataPublisherProxy = FrameworkServiceDataHolder.getInstance()
                .getAuthnDataPublisherProxy();
        if (authnDataPublisherProxy != null && authnDataPublisherProxy.isEnabled(context)) {
            boolean isFederated = this instanceof FederatedApplicationAuthenticator;
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put(FrameworkConstants.AnalyticsAttributes.USER, user);
            if (isFederated) {
                // Setting this value to authentication context in order to use in AuthenticationSuccess Event
                context.setProperty(FrameworkConstants.AnalyticsAttributes.HAS_FEDERATED_STEP, true);
                paramMap.put(FrameworkConstants.AnalyticsAttributes.IS_FEDERATED, true);
                paramMap.put(FrameworkConstants.AUTHENTICATOR, getName());
            } else {
                // Setting this value to authentication context in order to use in AuthenticationSuccess Event
                context.setProperty(FrameworkConstants.AnalyticsAttributes.HAS_LOCAL_STEP, true);
                paramMap.put(FrameworkConstants.AnalyticsAttributes.IS_FEDERATED, false);
            }
            Map<String, Object> unmodifiableParamMap = Collections.unmodifiableMap(paramMap);
            String eventName;
            if (success) {
                authnDataPublisherProxy.publishAuthenticationStepSuccess(request, context,
                        unmodifiableParamMap);
                if (eventFiringEnabled() && context.getProperty(FrameworkConstants.USERNAME) != null) {
                    eventName = IdentityEventConstants.Event.POST_AUTHENTICATION;
                    fireEvent(context, eventName, true);
                }

            } else {
                authnDataPublisherProxy.publishAuthenticationStepFailure(request, context,
                        unmodifiableParamMap);
                if (eventFiringEnabled() && context.getProperty(FrameworkConstants.USERNAME) != null) {
                    eventName = IdentityEventConstants.Event.POST_AUTHENTICATION;
                    fireEvent(context, eventName, false);
                }
            }
        }
    }

    private void fireEvent(AuthenticationContext context, String eventName, boolean operationStatus)
            throws AuthenticationFailedException {

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
            throw new AuthenticationFailedException(" Error in accessing user store ", e);
        } catch (IdentityEventException e) {
            throw new AuthenticationFailedException(" Error while firing the events ", e);
        }
    }

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

    protected void initiateAuthenticationRequest(HttpServletRequest request,
                                                 HttpServletResponse response, AuthenticationContext context)
            throws AuthenticationFailedException {
    }

    protected abstract void processAuthenticationResponse(HttpServletRequest request,
                                                          HttpServletResponse response, AuthenticationContext context)
            throws AuthenticationFailedException;

    protected void initiateLogoutRequest(HttpServletRequest request, HttpServletResponse response,
                                         AuthenticationContext context) throws LogoutFailedException {
        throw new UnsupportedOperationException();
    }

    protected void processLogoutResponse(HttpServletRequest request, HttpServletResponse response,
                                         AuthenticationContext context) throws LogoutFailedException {
        throw new UnsupportedOperationException();
    }

    protected AuthenticatorConfig getAuthenticatorConfig() {
        AuthenticatorConfig authConfig = FileBasedConfigurationBuilder.getInstance().getAuthenticatorBean(getName());
        if (authConfig == null) {
            authConfig = new AuthenticatorConfig();
            authConfig.setParameterMap(new HashMap<String, String>());
        }
        return authConfig;
    }

    protected boolean retryAuthenticationEnabled() {
        return false;
    }

    protected boolean eventFiringEnabled() {
        return false;
    }
    /**
     * In case of the authenticator being an option in a multi-option step, decide whether to redirect to
     * multi-options page to retry. By default all authenticators will redirect to the multi-option page on failure.
     * <p>
     * Some authenticators may want to avoid this by having their own retry mechanism/retry page rather than
     * re-initiating the step from the multi-option page.
     *
     * @return
     */
    protected boolean isRedirectToMultiOptionPageOnFailure() {
        Map<String, String> parameterMap = getAuthenticatorConfig().getParameterMap();
        boolean isRedirectToMultiOptionPageOnFailure = true;
        if (MapUtils.isNotEmpty(parameterMap)) {
            String redirectToMultiOptionOnFailure = parameterMap.get(REDIRECT_TO_MULTI_OPTION_PAGE_ON_FAILURE);
            isRedirectToMultiOptionPageOnFailure
                    = redirectToMultiOptionOnFailure == null || Boolean.parseBoolean(redirectToMultiOptionOnFailure);
            if (log.isDebugEnabled()) {
                log.debug("redirectToMultiOptionOnFailure has been set as : " + isRedirectToMultiOptionPageOnFailure);
            }
        }
        return isRedirectToMultiOptionPageOnFailure;
    }

    @Override
    public String getClaimDialectURI() {
        return null;
    }

    @Override
    public List<Property> getConfigurationProperties() {
        return new ArrayList<Property>();
    }

    protected String getUserStoreAppendedName(String userName) {
        if (!userName.contains(CarbonConstants.DOMAIN_SEPARATOR) && UserCoreUtil.getDomainFromThreadLocal() != null
            && !"".equals(UserCoreUtil.getDomainFromThreadLocal())) {
            userName = UserCoreUtil.getDomainFromThreadLocal() + CarbonConstants.DOMAIN_SEPARATOR + userName;
        }
        return userName;
    }
}
