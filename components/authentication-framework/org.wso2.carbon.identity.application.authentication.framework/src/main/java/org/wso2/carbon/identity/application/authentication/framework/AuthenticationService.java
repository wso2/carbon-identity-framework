/*
 * Copyright (c) 2023-2026, WSO2 LLC. (https://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.identity.application.authentication.framework;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.base.MultitenantConstants;
import org.wso2.carbon.identity.application.authentication.framework.cache.AuthenticationResultCacheEntry;
import org.wso2.carbon.identity.application.authentication.framework.exception.FrameworkException;
import org.wso2.carbon.identity.application.authentication.framework.exception.auth.service.AuthServiceClientException;
import org.wso2.carbon.identity.application.authentication.framework.exception.auth.service.AuthServiceException;
import org.wso2.carbon.identity.application.authentication.framework.internal.core.ApplicationAuthenticatorManager;
import org.wso2.carbon.identity.application.authentication.framework.model.AuthenticationResult;
import org.wso2.carbon.identity.application.authentication.framework.model.AuthenticatorData;
import org.wso2.carbon.identity.application.authentication.framework.model.auth.service.AuthServiceErrorInfo;
import org.wso2.carbon.identity.application.authentication.framework.model.auth.service.AuthServiceRequest;
import org.wso2.carbon.identity.application.authentication.framework.model.auth.service.AuthServiceRequestWrapper;
import org.wso2.carbon.identity.application.authentication.framework.model.auth.service.AuthServiceResponse;
import org.wso2.carbon.identity.application.authentication.framework.model.auth.service.AuthServiceResponseData;
import org.wso2.carbon.identity.application.authentication.framework.model.auth.service.AuthServiceResponseWrapper;
import org.wso2.carbon.identity.application.authentication.framework.util.FrameworkConstants;
import org.wso2.carbon.identity.application.authentication.framework.util.FrameworkUtils;
import org.wso2.carbon.identity.application.authentication.framework.util.auth.service.AuthServiceConstants;
import org.wso2.carbon.identity.application.authentication.framework.util.auth.service.AuthServiceUtils;
import org.wso2.carbon.identity.application.common.IdentityApplicationManagementClientException;
import org.wso2.carbon.identity.application.common.IdentityApplicationManagementException;
import org.wso2.carbon.identity.application.common.model.ServiceProvider;
import org.wso2.carbon.identity.application.mgt.ApplicationManagementService;
import org.wso2.carbon.identity.central.log.mgt.utils.LoggerUtils;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;
import org.wso2.carbon.identity.core.util.IdentityUtil;
import org.wso2.carbon.utils.DiagnosticLog;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * The authentication service class.
 * The class uses request and response wrappers to communicate with the authentication framework.
 */
public class AuthenticationService {

    private static final Log LOG = LogFactory.getLog(AuthenticationService.class);
    private final CommonAuthenticationHandler commonAuthenticationHandler = new CommonAuthenticationHandler();

    /**
     * Handles the authentication request.
     *
     * @param authRequest The authentication request.
     * @return The authentication response.
     * @throws AuthServiceException If an error occurs while handling the authentication request.
     */
    public AuthServiceResponse handleAuthentication(AuthServiceRequest authRequest) throws AuthServiceException {

        // Request validation is only required for the initial authentication request.
        if (isInitialAuthRequest(authRequest)) {
            validateRequest(authRequest);
        } else {
            logFlowIdUsage(authRequest);
        }
        AuthServiceRequestWrapper wrappedRequest = getWrappedRequest(authRequest.getRequest(),
                authRequest.getParameters());
        AuthServiceResponseWrapper wrappedResponse = getWrappedResponse(authRequest.getResponse());
        try {
            commonAuthenticationHandler.doPost(wrappedRequest, wrappedResponse);
        } catch (ServletException | IOException e) {
            throw new AuthServiceException(AuthServiceConstants.ErrorMessage.ERROR_UNABLE_TO_PROCEED.code(),
                    AuthServiceConstants.ErrorMessage.ERROR_UNABLE_TO_PROCEED.description(), e);
        }

        return processCommonAuthResponse(wrappedRequest, wrappedResponse);
    }

    private AuthServiceRequestWrapper getWrappedRequest(HttpServletRequest request, Map<String, String[]> parameters) {

        return new AuthServiceRequestWrapper(request, parameters);
    }

    private AuthServiceResponseWrapper getWrappedResponse(HttpServletResponse response) {

        return new AuthServiceResponseWrapper(response);
    }

    private AuthServiceResponse processCommonAuthResponse(AuthServiceRequestWrapper request,
                                                          AuthServiceResponseWrapper response)
            throws AuthServiceException {

        AuthServiceResponse authServiceResponse = new AuthServiceResponse();

        /* This order of flow checking should be maintained as some of the
         error flows could come with flow status INCOMPLETE.*/
        if (isAuthFlowSuccessful(request)) {
            handleSuccessAuthResponse(request, response, authServiceResponse);
        } else if (isAuthFlowFailed(request, response)) {
            handleFailedAuthResponse(request, response, authServiceResponse);
        } else if (isAuthFlowIncomplete(request)) {
            handleIntermediateAuthResponse(request, response, authServiceResponse);
        } else {
            throw new AuthServiceException(AuthServiceConstants.ErrorMessage.ERROR_UNKNOWN_AUTH_FLOW_STATUS.code(),
                    String.format(AuthServiceConstants.ErrorMessage.ERROR_UNKNOWN_AUTH_FLOW_STATUS.description(),
                            request.getAuthFlowStatus()));
        }

        return authServiceResponse;
    }

    private void handleIntermediateAuthResponse(AuthServiceRequestWrapper request, AuthServiceResponseWrapper response,
                                                AuthServiceResponse authServiceResponse) throws AuthServiceException {

        authServiceResponse.setSessionDataKey(request.getSessionDataKey());
        authServiceResponse.setFlowStatus(AuthServiceConstants.FlowStatus.INCOMPLETE);
        logFlowIdIssuance(request.getSessionDataKey(), AuthServiceConstants.FlowStatus.INCOMPLETE);
        AuthServiceResponseData responseData = new AuthServiceResponseData();
        boolean isMultiOptionsResponse = request.isMultiOptionsResponse();

        List<AuthenticatorData> authenticatorDataList;
        if (isMultiOptionsResponse) {
            responseData.setAuthenticatorSelectionRequired(true);
            authenticatorDataList = getAuthenticatorBasicData(response.getAuthenticators(),
                    request.getAuthInitiationData(),  getTenantDomain((HttpServletRequest) request.getRequest()));
        } else {
            authenticatorDataList = request.getAuthInitiationData();
        }
        responseData.setAuthenticatorOptions(authenticatorDataList);
        authServiceResponse.setData(responseData);
    }

    private void handleSuccessAuthResponse(AuthServiceRequestWrapper request, AuthServiceResponseWrapper response,
                                           AuthServiceResponse authServiceResponse) throws AuthServiceException {

        authServiceResponse.setSessionDataKey(getFlowCompletionSessionDataKey(request, response));
        authServiceResponse.setFlowStatus(AuthServiceConstants.FlowStatus.SUCCESS_COMPLETED);
    }

    private void handleFailedAuthResponse(AuthServiceRequestWrapper request, AuthServiceResponseWrapper response,
                                          AuthServiceResponse authServiceResponse) throws AuthServiceException {

        if (request.isAuthFlowConcluded()) {
            handleFailedConcludedAuthResponse(request, authServiceResponse);
        } else {
            handleFailedIncompleteAuthResponse(request, response, authServiceResponse);
        }
    }

    private void handleFailedConcludedAuthResponse(AuthServiceRequestWrapper request,
                                                   AuthServiceResponse authServiceResponse) {

        String errorCode = AuthServiceConstants.ErrorMessage.ERROR_AUTHENTICATION_FAILURE.code();
        String errorMessage = AuthServiceConstants.ErrorMessage.ERROR_AUTHENTICATION_FAILURE.message();
        String errorDescription = AuthServiceConstants.ErrorMessage.ERROR_AUTHENTICATION_FAILURE.description();
        String internalErrorCode = null;
        String internalErrorMessage = null;

        authServiceResponse.setSessionDataKey(request.getSessionDataKey());
        authServiceResponse.setFlowStatus(AuthServiceConstants.FlowStatus.FAIL_COMPLETED);
        AuthenticationResult authenticationResult = getAuthenticationResult(request);
        if (authenticationResult != null) {
            internalErrorCode = (String) authenticationResult.getProperty(FrameworkConstants.AUTH_ERROR_CODE);
            internalErrorMessage = (String) authenticationResult.getProperty(FrameworkConstants.AUTH_ERROR_MSG);
        } else if (request.isSentToRetry()) {
            // Check for retry status to resolve mapped error.
            String retryStatus = (String) request.getAttribute(FrameworkConstants.REQ_ATTR_RETRY_STATUS);
            if (StringUtils.isNotBlank(retryStatus)) {
                internalErrorCode = retryStatus;
            }
        }

        AuthServiceConstants.ErrorMessage mappedError = getMappedError(internalErrorCode);
        if (mappedError != null) {
            errorCode = mappedError.code();
            errorMessage = mappedError.message();
            errorDescription = mappedError.description();
            if (AuthServiceConstants.ErrorMessage.ERROR_AUTHENTICATION_CONTEXT_NULL == mappedError
                    || AuthServiceConstants.ErrorMessage.ERROR_AUTHENTICATION_FLOW_TIMEOUT == mappedError) {
                logInvalidFlowIdUsage(request.getSessionDataKey(), mappedError);
            }
        } else {
            String builtErrorMessage = buildFailedConcludedErrorMessage(internalErrorCode, internalErrorMessage);
            if (StringUtils.isNotBlank(builtErrorMessage)) {
                errorMessage = builtErrorMessage;
            }
        }

        AuthServiceErrorInfo errorInfo = new AuthServiceErrorInfo(errorCode, errorMessage, errorDescription);
        authServiceResponse.setErrorInfo(errorInfo);
    }

    private static String buildFailedConcludedErrorMessage(String errorCode, String errorMessage) {

        String errorMsgBuilder = StringUtils.EMPTY;
        if (StringUtils.isNotBlank(errorCode)) {
            errorMsgBuilder = errorCode;
        }

        if (StringUtils.isNotBlank(errorMessage)) {
            if (StringUtils.isNotBlank(errorMsgBuilder)) {
                errorMsgBuilder = new StringJoiner(" ")
                        .add(errorMsgBuilder)
                        .add(AuthServiceConstants.INTERNAL_ERROR_MSG_SEPARATOR)
                        .add(errorMessage).toString();
            } else if (StringUtils.isBlank(errorMsgBuilder)) {
                errorMsgBuilder = errorMessage;
            }
        }

        /* If there is an error message and an error code provided from the authentication framework then the
         final error message will be set as "<internal errorCode> - <internal errorMessage>".
         This is done to preserve the error details while sending out a standard error response.*/
        return errorMsgBuilder;
    }

    private void handleFailedIncompleteAuthResponse(AuthServiceRequestWrapper request, AuthServiceResponseWrapper
            response, AuthServiceResponse authServiceResponse) throws AuthServiceException {

        String errorCode;
        String errorMessage;
        String errorDescription = AuthServiceConstants.ErrorMessage.
                ERROR_AUTHENTICATION_FAILURE_RETRY_AVAILABLE.description();

        authServiceResponse.setSessionDataKey(request.getSessionDataKey());
        authServiceResponse.setFlowStatus(AuthServiceConstants.FlowStatus.FAIL_INCOMPLETE);
        logFlowIdIssuance(request.getSessionDataKey(), AuthServiceConstants.FlowStatus.FAIL_INCOMPLETE);
        AuthServiceResponseData responseData = new AuthServiceResponseData();
        List<AuthenticatorData> authenticatorDataList;
        boolean isMultiOptionsResponse = request.isMultiOptionsResponse();
        if ((includeMultiOptionsInResponse() || includeAuthInitDataOnRetryInApiBasedAuthResponse())
                && isMultiOptionsResponse) {
            responseData.setAuthenticatorSelectionRequired(true);
            authenticatorDataList = getAuthenticatorBasicData(response.getAuthenticators(),
                    request.getAuthInitiationData(), getTenantDomain((HttpServletRequest) request.getRequest()));
        } else {
            authenticatorDataList = request.getAuthInitiationData();
        }
        responseData.setAuthenticatorOptions(authenticatorDataList);
        authServiceResponse.setData(responseData);
        errorCode = getErrorCode(response);
        errorMessage = getErrorMessage(response);

        if (StringUtils.isBlank(errorCode)) {
            errorCode = AuthServiceConstants.ErrorMessage.ERROR_AUTHENTICATION_FAILURE_RETRY_AVAILABLE.code();
        }

        if (StringUtils.isBlank(errorMessage)) {
            errorMessage = AuthServiceConstants.ErrorMessage.ERROR_AUTHENTICATION_FAILURE_RETRY_AVAILABLE.message();
        }

        AuthServiceErrorInfo errorInfo = new AuthServiceErrorInfo(errorCode, errorMessage, errorDescription);
        authServiceResponse.setErrorInfo(errorInfo);
    }

    private String getErrorCode(AuthServiceResponseWrapper response) throws AuthServiceException {

        Map<String, String> queryParams = AuthServiceUtils.extractQueryParams(response.getRedirectURL());
        return queryParams.get(AuthServiceConstants.ERROR_CODE_PARAM);
    }

    private String getErrorMessage(AuthServiceResponseWrapper response) throws AuthServiceException {

        Map<String, String> queryParams = AuthServiceUtils.extractQueryParams(response.getRedirectURL());

        if (queryParams.containsKey(AuthServiceConstants.PASSWORD_EXPIRED_PARAM)
                && queryParams.containsKey(AuthServiceConstants.PASSWORD_EXPIRED_MSG_PARAM)) {
            return queryParams.get(AuthServiceConstants.PASSWORD_EXPIRED_MSG_PARAM);
        }
        return queryParams.get(AuthServiceConstants.AUTH_FAILURE_MSG_PARAM);
    }

    private List<AuthenticatorData> getAuthenticatorBasicData(String authenticatorList,
               List<AuthenticatorData> authInitiationData, String tenantDomain) throws AuthServiceException {

        List<AuthenticatorData> authenticatorDataList = new ArrayList<>();
        String[] authenticatorAndIdpsArr = StringUtils.split(authenticatorList,
                AuthServiceConstants.AUTHENTICATOR_SEPARATOR);
        for (String authenticatorAndIdps : authenticatorAndIdpsArr) {
            String[] authenticatorIdpSeperatedArr = StringUtils.split(authenticatorAndIdps,
                    AuthServiceConstants.AUTHENTICATOR_IDP_SEPARATOR);
            String name = authenticatorIdpSeperatedArr[0];

            // Some authentication options would directly send the complete data. ex: basic authenticator.
            AuthenticatorData authenticatorData = getAuthenticatorData(name, authInitiationData);
            if (authenticatorData != null) {
                authenticatorDataList.add(authenticatorData);
                continue;
            }

            ApplicationAuthenticator authenticator = null;
            try {
                authenticator = ApplicationAuthenticatorManager.getInstance()
                        .getApplicationAuthenticatorByName(name, tenantDomain);
            } catch (FrameworkException e) {
                throw new AuthServiceException(String.format("An error occurred while resolving authenticator:%s",
                        name), e);
            }

            if (authenticator == null) {
                throw new AuthServiceException(AuthServiceConstants.ErrorMessage.ERROR_AUTHENTICATOR_NOT_FOUND.code(),
                        String.format(AuthServiceConstants.ErrorMessage.ERROR_AUTHENTICATOR_NOT_FOUND.description(),
                                name));
            }

            boolean isApiBasedAuthSupported = authenticator.isAPIBasedAuthenticationSupported();
            if (!isApiBasedAuthSupported && LOG.isDebugEnabled()) {
                LOG.debug("Authenticator: " + name + " does not support API based authentication.");
            }

            // The first element is the authenticator name hence its skipped to get the idp.
            for (int i = 1; i < authenticatorIdpSeperatedArr.length; i++) {
                String idp = authenticatorIdpSeperatedArr[i];
                authenticatorData = new AuthenticatorData();
                authenticatorData.setName(name);
                authenticatorData.setIdp(idp);
                authenticatorData.setDisplayName(authenticator.getFriendlyName());
                authenticatorData.setI18nKey(authenticator.getI18nKey());
                authenticatorDataList.add(authenticatorData);
            }
        }
        return authenticatorDataList;
    }

    private AuthenticatorData getAuthenticatorData(String authenticator,
                                                   List<AuthenticatorData> authenticatorDataList) {

        for (AuthenticatorData authenticatorData : authenticatorDataList) {
            if (StringUtils.equals(authenticatorData.getName(), authenticator)) {
                return authenticatorData;
            }
        }
        return null;
    }

    private boolean isAuthFlowSuccessful(AuthServiceRequestWrapper request) {

        return AuthenticatorFlowStatus.SUCCESS_COMPLETED == request.getAuthFlowStatus();
    }

    private boolean isAuthFlowFailed(AuthServiceRequestWrapper request, AuthServiceResponseWrapper response)
            throws AuthServiceException {

        return AuthenticatorFlowStatus.FAIL_COMPLETED == request.getAuthFlowStatus() || response.isErrorResponse() ||
                isSentToRetryPage(request) || response.isPasswordExpiredResponse();
    }

    private boolean isAuthFlowIncomplete(AuthServiceRequestWrapper request) {

        return AuthenticatorFlowStatus.INCOMPLETE == request.getAuthFlowStatus();
    }

    private AuthenticationResult getAuthenticationResult(AuthServiceRequestWrapper request) {

        AuthenticationResult authenticationResult =
                (AuthenticationResult) request.getAttribute(FrameworkConstants.RequestAttribute.AUTH_RESULT);
        if (authenticationResult == null && request.getSessionDataKey() != null) {
            AuthenticationResultCacheEntry authenticationResultCacheEntry =
                    FrameworkUtils.getAuthenticationResultFromCache(request.getSessionDataKey());
            if (authenticationResultCacheEntry != null) {
                authenticationResult = authenticationResultCacheEntry.getResult();
            }
        }
        return authenticationResult;
    }

    private boolean isSentToRetryPage(AuthServiceRequestWrapper request) {

        if (request.isSentToRetry()) {
            // If it's a retry the flow should be restarted.
            request.setAuthFlowConcluded(true);
            return true;
        }
        return false;
    }

    private String getFlowCompletionSessionDataKey(AuthServiceRequestWrapper request,
                                                   AuthServiceResponseWrapper response) throws AuthServiceException {

        String completionSessionDataKey = (String) request.getAttribute(FrameworkConstants.SESSION_DATA_KEY);
        if (StringUtils.isBlank(completionSessionDataKey)) {
            completionSessionDataKey = response.getSessionDataKey();
        }

        return completionSessionDataKey;
    }

    private void validateRequest(AuthServiceRequest authServiceRequest) throws AuthServiceException {

        String clientId = getClientId(authServiceRequest.getRequest());
        String tenantDomain = getTenantDomain(authServiceRequest.getRequest());
        ServiceProvider serviceProvider = getServiceProvider(clientId, tenantDomain);

        if (serviceProvider == null) {
            throw new AuthServiceClientException(
                    AuthServiceConstants.ErrorMessage.ERROR_UNABLE_TO_FIND_APPLICATION.code(),
                    String.format(AuthServiceConstants.ErrorMessage.ERROR_UNABLE_TO_FIND_APPLICATION.description(),
                            clientId, tenantDomain));
        }

        // Check if the application is enabled to proceed with authentication.
        if (!serviceProvider.isApplicationEnabled()) {
            throw new AuthServiceClientException(
                    AuthServiceConstants.ErrorMessage.ERROR_DISABLED_APPLICATION.code(),
                    AuthServiceConstants.ErrorMessage.ERROR_DISABLED_APPLICATION.description());
        }

        // Check whether api based authentication is enabled for the SP.
        if (!serviceProvider.isAPIBasedAuthenticationEnabled()) {
            throw new AuthServiceClientException(
                    AuthServiceConstants.ErrorMessage.ERROR_API_BASED_AUTH_NOT_ENABLED.code(),
                    String.format(AuthServiceConstants.ErrorMessage.ERROR_API_BASED_AUTH_NOT_ENABLED.description(),
                            serviceProvider.getApplicationResourceId()));
        }
    }

    private ServiceProvider getServiceProvider(String clientId, String tenantDomain)
            throws AuthServiceException {

        ApplicationManagementService appMgtService = ApplicationManagementService.getInstance();

        try {
            return appMgtService.getServiceProviderByClientId(clientId, FrameworkConstants.OAUTH2,
                    tenantDomain);
        } catch (IdentityApplicationManagementClientException e) {
            throw new AuthServiceClientException(
                    AuthServiceConstants.ErrorMessage.ERROR_UNABLE_TO_FIND_APPLICATION.code(),
                    String.format(AuthServiceConstants.ErrorMessage.ERROR_UNABLE_TO_FIND_APPLICATION.description(),
                            clientId, tenantDomain, e));
        } catch (IdentityApplicationManagementException e) {
            throw new AuthServiceException(AuthServiceConstants.ErrorMessage.ERROR_RETRIEVING_APPLICATION.code(),
                    String.format(AuthServiceConstants.ErrorMessage.ERROR_RETRIEVING_APPLICATION.description(),
                            clientId, tenantDomain, e));
        }
    }

    private String getTenantDomain(HttpServletRequest request) {

        String tenantDomain;
        if (IdentityTenantUtil.isTenantQualifiedUrlsEnabled()) {
            if (request.getAttribute(AuthServiceConstants.APP_TENANT_DOMAIN) != null) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Retrieving the tenant domain from request attribute in the flows initiated with " +
                            "tenant qualified organization access paths.");
                }
                tenantDomain = request.getAttribute(AuthServiceConstants.APP_TENANT_DOMAIN).toString();
            } else {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Tenant Qualified URL mode enabled. Retrieving tenantDomain from thread " +
                            "local context.");
                }
                tenantDomain = IdentityTenantUtil.getTenantDomainFromContext();
            }
        } else {
            tenantDomain = request.getParameter(FrameworkConstants.RequestParams.TENANT_DOMAIN);
        }

        if (StringUtils.isEmpty(tenantDomain)) {
            tenantDomain = MultitenantConstants.SUPER_TENANT_DOMAIN_NAME;
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("Resolved tenant domain: " + tenantDomain);
        }
        return tenantDomain;
    }

    private String getClientId(HttpServletRequest request) {

        return (String) request.getAttribute(AuthServiceConstants.REQ_ATTR_RELYING_PARTY);
    }

    private boolean isInitialAuthRequest(AuthServiceRequest authServiceRequest) {

        return Boolean.TRUE.equals(authServiceRequest.getRequest().getAttribute(
                AuthServiceConstants.REQ_ATTR_IS_INITIAL_API_BASED_AUTH_REQUEST));
    }

    private AuthServiceConstants.ErrorMessage getMappedError(String errorCode) {

        if (errorCode == null) {
            return null;
        }

        switch (errorCode) {
            case FrameworkConstants.ERROR_STATUS_AUTH_FLOW_TIMEOUT:
                return AuthServiceConstants.ErrorMessage.ERROR_AUTHENTICATION_FLOW_TIMEOUT;
            case FrameworkConstants.ERROR_STATUS_AUTH_CONTEXT_NULL:
                return AuthServiceConstants.ErrorMessage.ERROR_AUTHENTICATION_CONTEXT_NULL;
            case FrameworkConstants.ERROR_STATUS_APP_DISABLED:
                return AuthServiceConstants.ErrorMessage.ERROR_DISABLED_APPLICATION;
            case FrameworkConstants.ERROR_STATUS_INVALID_AUTHENTICATOR:
                return AuthServiceConstants.ErrorMessage.ERROR_INVALID_AUTHENTICATOR;
            case FrameworkConstants.ERROR_STATUS_AUTHENTICATOR_NOT_SUPPORTED:
                return AuthServiceConstants.ErrorMessage.ERROR_AUTHENTICATOR_NOT_SUPPORTED;
            case FrameworkConstants.ERROR_STATUS_ALLOWED_RESEND_LIMIT_EXCEEDED:
                return AuthServiceConstants.ErrorMessage.ERROR_RESEND_COUNT_EXCEEDED;
            case FrameworkConstants.ERROR_STATUS_ALLOWED_RETRY_LIMIT_EXCEEDED:
                return AuthServiceConstants.ErrorMessage.ERROR_RETRY_COUNT_EXCEEDED;
            default:
                return null;
        }
    }

    /**
     * Records the flowId received with a subsequent app native authentication request. This makes it possible to
     * trace every usage of a given flowId, including usages of a flowId which is no longer active.
     *
     * @param authServiceRequest Authentication request received by the service.
     */
    private void logFlowIdUsage(AuthServiceRequest authServiceRequest) {

        String flowId = getFlowId(authServiceRequest);
        if (LOG.isDebugEnabled()) {
            LOG.debug("Received app native authentication request with the flowId: " + flowId);
        }
        if (!LoggerUtils.isDiagnosticLogsEnabled()) {
            return;
        }
        LoggerUtils.triggerDiagnosticLogEvent(new DiagnosticLog.DiagnosticLogBuilder(
                FrameworkConstants.LogConstants.AUTHENTICATION_FRAMEWORK,
                FrameworkConstants.LogConstants.ActionIDs.VALIDATE_FLOW_ID)
                .inputParam(FrameworkConstants.LogConstants.FLOW_ID, flowId)
                .resultMessage("Received an app native authentication request with a flowId.")
                .logDetailLevel(DiagnosticLog.LogDetailLevel.APPLICATION)
                .resultStatus(DiagnosticLog.ResultStatus.SUCCESS));
    }

    /**
     * Records the flowId returned to the client so that the point at which a flowId was issued can be traced.
     *
     * @param flowId     flowId returned with the authentication response.
     * @param flowStatus Flow status of the authentication response.
     */
    private void logFlowIdIssuance(String flowId, AuthServiceConstants.FlowStatus flowStatus) {

        if (StringUtils.isBlank(flowId) || !LoggerUtils.isDiagnosticLogsEnabled()) {
            return;
        }
        LoggerUtils.triggerDiagnosticLogEvent(new DiagnosticLog.DiagnosticLogBuilder(
                FrameworkConstants.LogConstants.AUTHENTICATION_FRAMEWORK,
                FrameworkConstants.LogConstants.ActionIDs.ISSUE_FLOW_ID)
                .inputParam(FrameworkConstants.LogConstants.FLOW_ID, flowId)
                .inputParam("flow status", flowStatus.name())
                .resultMessage("flowId is issued for the incomplete authentication flow. The same flowId is " +
                        "expected in the next authentication request of this flow.")
                .logDetailLevel(DiagnosticLog.LogDetailLevel.APPLICATION)
                .resultStatus(DiagnosticLog.ResultStatus.SUCCESS));
    }

    /**
     * Records that the authentication flow was concluded with a failure because the provided flowId is no longer
     * usable. The framework invalidates the flowId once the authentication flow is concluded or once the flow has
     * timed out, hence a flowId can be reported as invalid even if it was valid at the time it was issued.
     *
     * @param flowId      flowId received with the authentication request.
     * @param mappedError Error which the authentication flow concluded with.
     */
    private void logInvalidFlowIdUsage(String flowId, AuthServiceConstants.ErrorMessage mappedError) {

        LOG.warn("App native authentication request received with a flowId which is no longer active. flowId: "
                + flowId + ". Error: " + mappedError.code() + " - " + mappedError.description());
        if (!LoggerUtils.isDiagnosticLogsEnabled()) {
            return;
        }
        LoggerUtils.triggerDiagnosticLogEvent(new DiagnosticLog.DiagnosticLogBuilder(
                FrameworkConstants.LogConstants.AUTHENTICATION_FRAMEWORK,
                FrameworkConstants.LogConstants.ActionIDs.VALIDATE_FLOW_ID)
                .inputParam(FrameworkConstants.LogConstants.FLOW_ID, flowId)
                .inputParam(AuthServiceConstants.ERROR_CODE_PARAM, mappedError.code())
                .resultMessage("The provided flowId is no longer active. " + mappedError.description()
                        + " A new authentication flow needs to be initiated.")
                .logDetailLevel(DiagnosticLog.LogDetailLevel.APPLICATION)
                .resultStatus(DiagnosticLog.ResultStatus.FAILED));
    }

    private String getFlowId(AuthServiceRequest authServiceRequest) {

        Map<String, String[]> parameters = authServiceRequest.getParameters();
        if (parameters == null) {
            return null;
        }
        String[] flowIdParam = parameters.get(AuthServiceConstants.FLOW_ID);
        return flowIdParam != null && flowIdParam.length > 0 ? flowIdParam[0] : null;
    }

    private boolean includeMultiOptionsInResponse() {

        return Boolean.parseBoolean(IdentityUtil.getProperty(
                FrameworkConstants.INCLUDE_MULTI_OPTIONS_IN_API_BASED_RESPONSE));
    }

    private boolean includeAuthInitDataOnRetryInApiBasedAuthResponse() {

        return Boolean.parseBoolean(IdentityUtil.getProperty(
                FrameworkConstants.INCLUDE_AUTH_INIT_DATA_ON_RETRY_IN_API_BASED_AUTH_RESPONSE));
    }
}
