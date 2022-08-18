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
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.CarbonConstants;
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
import org.wso2.carbon.identity.base.IdentityConstants;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;
import org.wso2.carbon.identity.event.IdentityEventConstants;
import org.wso2.carbon.identity.event.IdentityEventException;
import org.wso2.carbon.identity.event.event.Event;
import org.wso2.carbon.identity.event.services.IdentityEventService;
import org.wso2.carbon.user.api.UserRealm;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.user.core.util.UserCoreUtil;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static org.wso2.carbon.identity.application.authentication.framework.util.FrameworkErrorConstants.ErrorMessages;
import static org.wso2.carbon.identity.application.common.util.IdentityApplicationConstants.REDIRECT_TO_MULTI_OPTION_PAGE_ON_FAILURE;

/**
 * This is the super class of all the Application Authenticators. Authenticator writers must extend
 * this.
 */
public abstract class AbstractApplicationAuthenticator implements ApplicationAuthenticator {

    private static final long serialVersionUID = -4406878411547612129L;
    private static final Log log = LogFactory.getLog(AbstractApplicationAuthenticator.class);
    public static final String ENABLE_RETRY_FROM_AUTHENTICATOR = "enableRetryFromAuthenticator";

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
                    processAuthenticationResponse(request, response, context);
                    if (this instanceof LocalApplicationAuthenticator) {
                        if (!context.getSequenceConfig().getApplicationConfig().isSaaSApp()) {
                            String userDomain = context.getSubject().getTenantDomain();
                            String tenantDomain = context.getTenantDomain();
                            if (!StringUtils.equals(userDomain, tenantDomain)) {
                                context.setProperty("UserTenantDomainMismatch", true);
                                throw new AuthenticationFailedException(
                                        ErrorMessages.MISMATCHING_TENANT_DOMAIN.getCode(),
                                        ErrorMessages.MISMATCHING_TENANT_DOMAIN.getMessage(),
                                        context.getSubject());
                            }
                        }
                    }
                    request.setAttribute(FrameworkConstants.REQ_ATTR_HANDLED, true);
                    context.setProperty(FrameworkConstants.LAST_FAILED_AUTHENTICATOR, null);
                    publishAuthenticationStepAttempt(request, context, context.getSubject(), true);
                    return AuthenticatorFlowStatus.SUCCESS_COMPLETED;
                } catch (AuthenticationFailedException e) {
                    publishAuthenticationStepAttemptFailure(request, context, e.getUser(), e.getErrorCode());
                    request.setAttribute(FrameworkConstants.REQ_ATTR_HANDLED, true);
                    // Decide whether we need to redirect to the login page to retry authentication.
                    boolean sendToMultiOptionPage =
                            isStepHasMultiOption(context) && isRedirectToMultiOptionPageOnFailure();
                    context.setSendToMultiOptionPage(sendToMultiOptionPage);
                    context.setRetrying(retryAuthenticationEnabled());
                    if (retryAuthenticationEnabled(context) && !sendToMultiOptionPage) {
                        if (log.isDebugEnabled()) {
                            log.debug("Error occurred during the authentication process, hence retrying.", e);
                        }
                        // The Authenticator will re-initiate the authentication and retry.
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

    private void handlePostAuthentication(AuthenticationContext context) throws AuthenticationFailedException {

        Map<String, Object> eventProperties = new HashMap<>();
        String username = MultitenantUtils.getTenantAwareUsername(context.getSubject().toFullQualifiedUsername());
        if (context.getSubject().isFederatedUser()) {
            username = UserCoreUtil.removeDomainFromName(username);
        }
        String tenantDomain = context.getTenantDomain();
        IdentityEventService identityEventService = FrameworkServiceDataHolder.getInstance().getIdentityEventService();
        RealmService realmService = FrameworkServiceDataHolder.getInstance().getRealmService();
        try {
            UserRealm userRealm = realmService.getTenantUserRealm(IdentityTenantUtil.getTenantId(tenantDomain));
            eventProperties.put(IdentityEventConstants.EventProperty.USER_NAME, username);
            eventProperties.put(IdentityEventConstants.EventProperty.USER_STORE_MANAGER, userRealm
                    .getUserStoreManager());
            eventProperties.put(IdentityEventConstants.EventProperty.TENANT_DOMAIN, tenantDomain);
            if (context.isRequestAuthenticated()) {
                eventProperties.put(IdentityEventConstants.EventProperty.OPERATION_STATUS, true);
            } else {
                eventProperties.put(IdentityEventConstants.EventProperty.OPERATION_STATUS, false);
            }
            Event event = new Event(IdentityEventConstants.Event.POST_AUTHENTICATION, eventProperties);
            identityEventService.handleEvent(event);
        } catch (UserStoreException e) {
            throw new AuthenticationFailedException(ErrorMessages.SYSTEM_ERROR_WHILE_AUTHENTICATING.getCode(),
                    " Error in accessing user store in tenant: " + tenantDomain, e);
        } catch (IdentityEventException e) {
            throw new AuthenticationFailedException(ErrorMessages.SYSTEM_ERROR_WHILE_AUTHENTICATING.getCode(),
                    " Error while handling post authentication event for user: " + username + " in tenant: " +
                            tenantDomain, e);
        }
    }

    protected boolean retryAuthenticationEnabled(AuthenticationContext context) {
        SequenceConfig sequenceConfig = context.getSequenceConfig();
        AuthenticationGraph graph = sequenceConfig.getAuthenticationGraph();
        boolean isRetryAuthenticatorEnabled = false;
        Map<String, String> authParams = context.getAuthenticatorParams(context.getCurrentAuthenticator());
        if (MapUtils.isNotEmpty(authParams)) {
            isRetryAuthenticatorEnabled = Boolean.parseBoolean(authParams.get(ENABLE_RETRY_FROM_AUTHENTICATOR));
        }
        if (graph == null || !graph.isEnabled() || isRetryAuthenticatorEnabled) {
            return retryAuthenticationEnabled();
        }
        return false;
    }

    protected boolean isStepHasMultiOption(AuthenticationContext context) {
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

    protected void publishAuthenticationStepAttempt(HttpServletRequest request, AuthenticationContext context,
                                                    User user, boolean success) {

        AuthenticationDataPublisher authnDataPublisherProxy = FrameworkServiceDataHolder.getInstance()
                .getAuthnDataPublisherProxy();
        if (authnDataPublisherProxy != null && authnDataPublisherProxy.isEnabled(context)) {
            Serializable currentAuthenticatorStartTime =
                    context.getAnalyticsData(FrameworkConstants.AnalyticsData.CURRENT_AUTHENTICATOR_START_TIME);
            if (currentAuthenticatorStartTime instanceof Long) {
                context.setAnalyticsData(FrameworkConstants.AnalyticsData.CURRENT_AUTHENTICATOR_DURATION,
                        System.currentTimeMillis() - (long) currentAuthenticatorStartTime);
            }
            boolean isFederated = this instanceof FederatedApplicationAuthenticator;
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put(FrameworkConstants.AnalyticsAttributes.USER, user);
            if (isFederated) {
                // Setting this value to authentication context in order to use in AuthenticationSuccess Event
                context.setProperty(FrameworkConstants.AnalyticsAttributes.HAS_FEDERATED_STEP, true);
                paramMap.put(FrameworkConstants.AnalyticsAttributes.IS_FEDERATED, true);
                paramMap.put(FrameworkConstants.AUTHENTICATOR, getName());
                if (user != null) {
                    user.setTenantDomain(context.getTenantDomain());
                }
            } else {
                // Setting this value to authentication context in order to use in AuthenticationSuccess Event
                context.setProperty(FrameworkConstants.AnalyticsAttributes.HAS_LOCAL_STEP, true);
                paramMap.put(FrameworkConstants.AnalyticsAttributes.IS_FEDERATED, false);
            }
            Map<String, Object> unmodifiableParamMap = Collections.unmodifiableMap(paramMap);
            if (success) {
                authnDataPublisherProxy.publishAuthenticationStepSuccess(request, context,
                        unmodifiableParamMap);
                /*
                Resetting the authenticator start time to null since the step event is Success and for the next
                step event start time will be added in DefaultStepHandler handle method.
                 */
                context.setAnalyticsData(FrameworkConstants.AnalyticsData.CURRENT_AUTHENTICATOR_START_TIME, null);

            } else {
                authnDataPublisherProxy.publishAuthenticationStepFailure(request, context,
                        unmodifiableParamMap);
                /*
                Resetting the authenticator start time to current time since the step event is failure and retrying
                the event duration will be counted as a new step.
                 */
                context.setAnalyticsData(
                        FrameworkConstants.AnalyticsData.CURRENT_AUTHENTICATOR_START_TIME, System.currentTimeMillis());
            }
        }
    }

    /**
     * Helper delegator to publish the events for Authentication Step Attempt Failure.
     *
     * @param request   Incoming Http request to framework for authentication
     * @param context   Authentication Context
     * @param user      initiated user
     * @param errorCode of the exception
     */
    private void publishAuthenticationStepAttemptFailure(HttpServletRequest request, AuthenticationContext context,
                                                         User user, String errorCode) {

        context.setAnalyticsData(FrameworkConstants.AnalyticsData.CURRENT_AUTHENTICATOR_ERROR_CODE, errorCode);
        publishAuthenticationStepAttempt(request, context, user, false);
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

    /**
     * Get map of runtime params set through the script.
     *
     * @param context context
     * @return Map of params
     */
    public Map<String, String> getRuntimeParams(AuthenticationContext context) {

        Map<String, String> runtimeParams = context.getAuthenticatorParams(getName());
        Map<String, String> commonParams = context
                .getAuthenticatorParams(FrameworkConstants.JSAttributes.JS_COMMON_OPTIONS);
        if (MapUtils.isNotEmpty(commonParams)) {
            if (runtimeParams != null) {
                commonParams.putAll(runtimeParams);
            }
            return commonParams;
        } else if (runtimeParams != null) {
            return runtimeParams;
        }
        return Collections.emptyMap();
    }

    @Override
    public String getAuthMechanism() {

        String authMechanism = getAuthenticatorConfig().getParameterMap().get(FrameworkConstants.AUTH_MECHANISM);
        if (StringUtils.isEmpty(authMechanism)) {
            authMechanism = getName();
        }
        return authMechanism;
    }

    @Override
    public String[] getTags() {

        String tags = getAuthenticatorConfig().getParameterMap().get(IdentityConstants.TAGS);
        String[] tagsArray = StringUtils.split(tags, ",");
        if (ArrayUtils.isNotEmpty(tagsArray)) {
            for (int i = 0; i < tagsArray.length; i++) {
                tagsArray[i] = tagsArray[i].trim();
            }
        }
        return tagsArray;
    }
}
