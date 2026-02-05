/*
 * Copyright (c) 2022-2026, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.carbon.identity.application.authentication.framework.handler.step.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.client.utils.URIBuilder;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Spy;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.application.authentication.framework.ApplicationAuthenticator;
import org.wso2.carbon.identity.application.authentication.framework.LocalApplicationAuthenticator;
import org.wso2.carbon.identity.application.authentication.framework.config.ConfigurationFacade;
import org.wso2.carbon.identity.application.authentication.framework.config.model.AuthenticatorConfig;
import org.wso2.carbon.identity.application.authentication.framework.config.model.SequenceConfig;
import org.wso2.carbon.identity.application.authentication.framework.config.model.StepConfig;
import org.wso2.carbon.identity.application.authentication.framework.context.AuthenticationContext;
import org.wso2.carbon.identity.application.authentication.framework.exception.AuthenticationFailedException;
import org.wso2.carbon.identity.application.authentication.framework.exception.FrameworkException;
import org.wso2.carbon.identity.application.authentication.framework.exception.LogoutFailedException;
import org.wso2.carbon.identity.application.authentication.framework.model.AuthenticatedIdPData;
import org.wso2.carbon.identity.application.authentication.framework.model.AuthenticatedUser;
import org.wso2.carbon.identity.application.authentication.framework.model.AuthenticationResult;
import org.wso2.carbon.identity.application.authentication.framework.model.CommonAuthResponseWrapper;
import org.wso2.carbon.identity.application.authentication.framework.util.FrameworkConstants;
import org.wso2.carbon.identity.application.authentication.framework.util.FrameworkUtils;
import org.wso2.carbon.identity.application.authentication.framwork.test.utils.CommonTestUtils;
import org.wso2.carbon.identity.central.log.mgt.utils.LoggerUtils;
import org.wso2.carbon.identity.common.testng.WithCarbonHome;
import org.wso2.carbon.identity.core.ServiceURL;
import org.wso2.carbon.identity.core.ServiceURLBuilder;
import org.wso2.carbon.identity.core.URLBuilderException;
import org.wso2.carbon.identity.core.model.IdentityErrorMsgContext;
import org.wso2.carbon.identity.core.util.IdentityCoreConstants;
import org.wso2.carbon.identity.core.util.IdentityUtil;
import org.wso2.carbon.identity.flow.mgt.Constants;
import org.wso2.carbon.identity.flow.mgt.model.FlowConfigDTO;
import org.wso2.carbon.identity.flow.mgt.utils.FlowMgtConfigUtils;
import org.wso2.carbon.user.core.UserCoreConstants;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

/**
 * This test ensures that the retry page is returned when relevant properties are set with the values. The default
 * behavior is to return to the login page after login failure.
 */
@WithCarbonHome
public class DefaultStepHandlerTest {

    @Mock
    HttpServletRequest request;

    @Mock
    HttpServletResponse response;

    @Spy
    DefaultStepHandler defaultStepHandler;


    @BeforeMethod
    public void setUp() {

        initMocks(this);
    }

    @Test
    public void testGetInstance() {

        CommonTestUtils.testSingleton(
                DefaultStepHandler.getInstance(),
                DefaultStepHandler.getInstance()
        );
    }

    @DataProvider
    public Object[][] isRedirectionToRetryPageOnAccountLockData() {

        AuthenticationContext context1 = new AuthenticationContext();
        context1.setSendToMultiOptionPage(true);
        AuthenticationContext context2 = new AuthenticationContext();
        context2.setSendToMultiOptionPage(false);
        Map<String, String> parametersMap1 = new HashMap<>();
        parametersMap1.put(FrameworkConstants.SHOW_AUTH_FAILURE_REASON_ON_LOGIN_PAGE_CONF, "true");
        Map<String, String> parametersMap2 = new HashMap<>();
        parametersMap2.put(FrameworkConstants.REDIRECT_TO_RETRY_PAGE_ON_ACCOUNT_LOCK_CONF, "true");

        return new Object[][] {
                {context1, null, false},
                {context2, null, false},
                {context2, parametersMap1, false},
                {context2, parametersMap2, true}
        };
    }

    @Test(dataProvider = "isRedirectionToRetryPageOnAccountLockData")
    public void testIsRedirectionToRetryPageOnAccountLock(Object contextObj, Map<String, String> parameters,
                                                          boolean result) {

        AuthenticationContext context = (AuthenticationContext) contextObj;
        AuthenticatorConfig authenticatorConfig = spy(new AuthenticatorConfig());
        when(defaultStepHandler.getAuthenticatorConfig()).thenReturn(authenticatorConfig);
        when(defaultStepHandler.getAuthenticatorConfig().getParameterMap()).thenReturn(parameters);
        if (result) {
            Assert.assertTrue(defaultStepHandler.isRedirectionToRetryPageOnAccountLock(context));
        } else {
            Assert.assertFalse(defaultStepHandler.isRedirectionToRetryPageOnAccountLock(context));
        }
    }

    @DataProvider
    public Object[][] getRedirectUrlAsRetryPageData() {

        return new Object[][] {
                {true},
                {false}
        };
    }

    @Test(dataProvider = "getRedirectUrlAsRetryPageData")
    public void testGetRedirectUrlAsRetryPage(boolean redirectToRetryPageOnAccountLock)
            throws URISyntaxException, IOException {

        try (MockedStatic<IdentityUtil> identityUtil = mockStatic(IdentityUtil.class)) {
            // The authConfig "redirectToRetryPageOnAccountLock" has to be true to return the retry page as redirectUrl.
            Map<String, String> authParameters = new HashMap<>();
            authParameters.put(
                    FrameworkConstants.REDIRECT_TO_RETRY_PAGE_ON_ACCOUNT_LOCK_CONF,
                    String.valueOf(redirectToRetryPageOnAccountLock));
            AuthenticatorConfig authenticatorConfig = spy(new AuthenticatorConfig());
            when(defaultStepHandler.getAuthenticatorConfig()).thenReturn(authenticatorConfig);
            when(defaultStepHandler.getAuthenticatorConfig().getParameterMap()).thenReturn(authParameters);

            // Context needs to be passed as a parameter for the getRedirectUrl method.
            // "SendToMultipleOptionPage" property value should be false.
            AuthenticationContext context = spy(new AuthenticationContext());
            context.setSendToMultiOptionPage(false);

            // ErrorContext has to be null.
            // So errorContextParams will be filled with query parameters.
            // Then errorContextParams will be used to build errorParamString.
            identityUtil.when(IdentityUtil::getIdentityErrorMsg).thenReturn(null);

            // RetryParam needs to be passed as a parameter for the getRedirectUrl method.
            // Not relevant to the test flow furthermore.
            String retryParam = "";
            doReturn(retryParam).when(defaultStepHandler).handleIdentifierFirstLogin(context, retryParam);

            // The authConfig "showAuthFailureReason" can't be null and should be true.
            String showAuthFailureReason = "true";
            String maskUserNotExistsErrorCode = "true";

            // AuthenticatorNames and loginPage needs to be passed as parameters for the getRedirectUrl method.
            // Not relevant to the test flow furthermore.
            String authenticatorNames = "";
            String loginPage = "";

            // The basicAuthRedirectUrl should contain the error code for the user locked state as query parameters
            URIBuilder basicAuthRedirectUrlBuilder = new URIBuilder("http://example.com/");
            basicAuthRedirectUrlBuilder.addParameter(
                    FrameworkConstants.ERROR_CODE,
                    UserCoreConstants.ErrorCode.USER_IS_LOCKED);
            String basicAuthRedirectUrl = basicAuthRedirectUrlBuilder.build().toString();
            response = spy(new CommonAuthResponseWrapper(response));
            when(((CommonAuthResponseWrapper) response).getRedirectURL()).thenReturn(basicAuthRedirectUrl);

            defaultStepHandler.getRedirectUrl(request, response, context, authenticatorNames,
                    showAuthFailureReason, retryParam, loginPage);

            ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
            verify(response).encodeRedirectURL(captor.capture());
            if (redirectToRetryPageOnAccountLock) {
                Assert.assertTrue(captor.getValue().contains(
                        FrameworkConstants.DefaultUrlContexts.AUTHENTICATION_ENDPOINT_RETRY));
            } else {
                Assert.assertFalse(captor.getValue().contains(
                        FrameworkConstants.DefaultUrlContexts.AUTHENTICATION_ENDPOINT_RETRY));
            }
        }
    }

    @DataProvider
    public Object[] getOTPBasedFailedLoginScenarios() {

        return new Object[]{
                IdentityCoreConstants.ASK_PASSWORD_SET_PASSWORD_VIA_OTP_ERROR_CODE,
                IdentityCoreConstants.ADMIN_FORCED_USER_PASSWORD_RESET_VIA_OTP_ERROR_CODE
        };
    }

    @Test(dataProvider = "getOTPBasedFailedLoginScenarios")
    public void testGetRedirectURLWhenAuthenticationFail(String errorCode)
            throws URISyntaxException, IOException, URLBuilderException {

        try (MockedStatic<IdentityUtil> identityUtil = mockStatic(IdentityUtil.class);
             MockedStatic<ServiceURLBuilder> serviceURLBuilder = mockStatic(ServiceURLBuilder.class);
             MockedStatic<FlowMgtConfigUtils> flowMgtConfigUtil = mockStatic(FlowMgtConfigUtils.class)) {

            String callbackUrl = "http://localhost:8080/callback";

            AuthenticationContext context = new AuthenticationContext();
            context.setTenantDomain("carbon.super");
            IdentityErrorMsgContext errorMsgContext = mock(IdentityErrorMsgContext.class);
            when(errorMsgContext.getErrorCode()).thenReturn(errorCode);
            identityUtil.when(IdentityUtil::getIdentityErrorMsg).thenReturn(errorMsgContext);

            // Mock ServiceURLBuilder chain
            ServiceURLBuilder mockServiceURLBuilder = mock(ServiceURLBuilder.class);
            ServiceURL mockServiceURL = mock(ServiceURL.class);
            serviceURLBuilder.when(ServiceURLBuilder::create).thenReturn(mockServiceURLBuilder);
            when(mockServiceURLBuilder.addPath(any(String.class))).thenReturn(mockServiceURLBuilder);
            when(mockServiceURLBuilder.build()).thenReturn(mockServiceURL);  // Deprecated method used in the code
            when(mockServiceURLBuilder.build(any(String.class))).thenReturn(mockServiceURL);  // Non-deprecated method
            when(mockServiceURL.getAbsolutePublicURL()).thenReturn(callbackUrl);

            FlowConfigDTO flowConfigDTO = new FlowConfigDTO();
            flowConfigDTO.setIsEnabled(true);
            flowMgtConfigUtil.when(() -> FlowMgtConfigUtils.getFlowConfig(
                    Constants.FlowTypes.INVITED_USER_REGISTRATION.getType(), "carbon.super"))
                    .thenReturn(flowConfigDTO);
            // RetryParam needs to be passed as a parameter for the getRedirectUrl method.
            // Not relevant to the test flow furthermore.
            String retryParam = "";
            doReturn(retryParam).when(defaultStepHandler).handleIdentifierFirstLogin(context, retryParam);

            // The basicAuthRedirectUrl should contain the error code for the user locked state as query parameters
            URIBuilder basicAuthRedirectUrlBuilder = new URIBuilder("http://example.com/");
            basicAuthRedirectUrlBuilder.addParameter(
                    FrameworkConstants.ERROR_CODE,
                    UserCoreConstants.ErrorCode.USER_IS_LOCKED);
            String basicAuthRedirectUrl = basicAuthRedirectUrlBuilder.build().toString();
            response = spy(new CommonAuthResponseWrapper(response));
            when(((CommonAuthResponseWrapper) response).getRedirectURL()).thenReturn(basicAuthRedirectUrl);

            String redirectUrl = defaultStepHandler.getRedirectUrl(request, response, context, "",
                    "true", retryParam, "");
            Assert.assertTrue(redirectUrl.contains(URLEncoder.encode(callbackUrl, "UTF-8")));

            when(request.getParameter("username")).thenReturn("testUser");
            redirectUrl = defaultStepHandler.getRedirectUrl(request, response, context, "",
                    "false", retryParam, "");
            Assert.assertTrue(redirectUrl.contains(URLEncoder.encode(callbackUrl, "UTF-8")));
        }
    }

    /**
     * Data provider for testLoginFailureNotLoggedForDefinedRecoveryScenarios.
     *
     * @return Object[][] with error codes.
     */
    @DataProvider
    public Object[][] emailVerificationErrorCodes() {

        return new Object[][] {
                { IdentityCoreConstants.USER_EMAIL_NOT_VERIFIED_ERROR_CODE, "email.verification.pending" },
                { IdentityCoreConstants.USER_EMAIL_OTP_NOT_VERIFIED_ERROR_CODE, "email.otp.verification.pending" }
        };
    }

    @Test(dataProvider = "emailVerificationErrorCodes")
    public void testGetRedirectURLWhenEmailOTPVerificationAuthenticationFail(String errorCode, String failureMessage)
            throws URISyntaxException, IOException {

        try (MockedStatic<IdentityUtil> identityUtil = mockStatic(IdentityUtil.class)) {

            AuthenticationContext context = new AuthenticationContext();
            IdentityErrorMsgContext errorMsgContext = mock(IdentityErrorMsgContext.class);
            when(errorMsgContext.getErrorCode()).thenReturn(errorCode);
            identityUtil.when(IdentityUtil::getIdentityErrorMsg).thenReturn(errorMsgContext);

            // RetryParam needs to be passed as a parameter for the getRedirectUrl method.
            // Not relevant to the test flow furthermore.
            String retryParam = "";
            doReturn(retryParam).when(defaultStepHandler).handleIdentifierFirstLogin(context, retryParam);

            // The basicAuthRedirectUrl should contain the error code for the user locked state as query parameters
            URIBuilder basicAuthRedirectUrlBuilder = new URIBuilder("http://example.com/");
            basicAuthRedirectUrlBuilder.addParameter(
                    FrameworkConstants.ERROR_CODE,
                    UserCoreConstants.ErrorCode.USER_IS_LOCKED);
            String basicAuthRedirectUrl = basicAuthRedirectUrlBuilder.build().toString();
            response = spy(new CommonAuthResponseWrapper(response));
            when(((CommonAuthResponseWrapper) response).getRedirectURL()).thenReturn(basicAuthRedirectUrl);

            String redirectUrl = defaultStepHandler.getRedirectUrl(request, response, context, "",
                    "true", retryParam, "");
            Assert.assertTrue(redirectUrl.contains(errorCode));
            Assert.assertTrue(redirectUrl.contains("authFailureMsg=" + failureMessage));
        }
    }

    /**
     * Data provider for testLoginFailureNotLoggedForDefinedRecoveryScenarios.
     *
     * @return Object[][] with error codes.
     */
    @DataProvider
    public Object[][] recoveryErrorCodeProvider() {

        return new Object[][] {
                { IdentityCoreConstants.ASK_PASSWORD_SET_PASSWORD_VIA_OTP_ERROR_CODE },
                { IdentityCoreConstants.USER_EMAIL_OTP_NOT_VERIFIED_ERROR_CODE }
        };
    }

    @Test(dataProvider = "recoveryErrorCodeProvider")
    public void testLoginFailureNotLoggedForDefinedRecoveryScenarios(String errorCode) throws FrameworkException,
            AuthenticationFailedException, LogoutFailedException {

        try (MockedStatic<IdentityUtil> identityUtil = mockStatic(IdentityUtil.class);
             MockedStatic<LoggerUtils> loggerUtils = mockStatic(LoggerUtils.class);
             MockedStatic<LogFactory> logFactory = mockStatic(LogFactory.class)) {

            // Mock the LOG instance.
            Log mockLog = mock(Log.class);
            when(mockLog.isDebugEnabled()).thenReturn(true);
            when(mockLog.isErrorEnabled()).thenReturn(true);
            logFactory.when(() -> LogFactory.getLog(DefaultStepHandler.class)).thenReturn(mockLog);

            AuthenticationContext context = mock(AuthenticationContext.class);
            SequenceConfig sequenceConfig = mock(SequenceConfig.class);
            when(context.getCurrentStep()).thenReturn(1);
            when(context.getSequenceConfig()).thenReturn(sequenceConfig);
            StepConfig stepConfig = mock(StepConfig.class);
            Map<Integer, StepConfig> stepMap = new HashMap<>();
            stepMap.put(1, stepConfig);
            when(sequenceConfig.getStepMap()).thenReturn(stepMap);

            IdentityErrorMsgContext errorMsgContext = mock(IdentityErrorMsgContext.class);
            when(errorMsgContext.getErrorCode()).thenReturn(errorCode);
            identityUtil.when(IdentityUtil::getIdentityErrorMsg).thenReturn(errorMsgContext);

            // RetryParam needs to be passed as a parameter for the getRedirectUrl method.
            // Not relevant to the test flow furthermore.
            String retryParam = "";
            doReturn(retryParam).when(defaultStepHandler).handleIdentifierFirstLogin(context, retryParam);

            AuthenticatorConfig authenticatorConfig = mock(AuthenticatorConfig.class);
            ApplicationAuthenticator applicationAuthenticator = mock(ApplicationAuthenticator.class);
            when(authenticatorConfig.getApplicationAuthenticator()).thenReturn(applicationAuthenticator);
            when(applicationAuthenticator.isAuthenticationRequired(request, response, context)).thenReturn(true);
            when(applicationAuthenticator.process(request, response, context))
                    .thenThrow(new AuthenticationFailedException(errorCode));
            loggerUtils.when(LoggerUtils::isDiagnosticLogsEnabled).thenReturn(false);

            defaultStepHandler.doAuthentication(request, response, context, authenticatorConfig);

            verify(mockLog, never()).error("Authentication failed exception! " + errorCode);
        }
    }

    @Test(expectedExceptions = FrameworkException.class,
            expectedExceptionsMessageRegExp = "Invalid user assertion.")
    public void testHandleResponseNoneCanHandle() throws Exception {

        // Arrange
        DefaultStepHandler handler = spy(DefaultStepHandler.class);
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        AuthenticationContext context = mock(AuthenticationContext.class);
        SequenceConfig sequenceConfig = mock(SequenceConfig.class);
        StepConfig stepConfig = mock(StepConfig.class);
        AuthenticatorConfig authenticatorConfig = mock(AuthenticatorConfig.class);
        ApplicationAuthenticator authenticator = mock(ApplicationAuthenticator.class);

        Map<Integer, StepConfig> stepMap = new HashMap<>();
        stepMap.put(1, stepConfig);
        List<AuthenticatorConfig> authenticatorList = Collections.singletonList(authenticatorConfig);

        when(context.getSequenceConfig()).thenReturn(sequenceConfig);
        when(context.getCurrentStep()).thenReturn(1);
        when(sequenceConfig.getStepMap()).thenReturn(stepMap);
        when(stepConfig.getAuthenticatorList()).thenReturn(authenticatorList);
        when(authenticatorConfig.getApplicationAuthenticator()).thenReturn(authenticator);
        when(authenticator.canHandleRequestFromMultiOptionStep(request, context)).thenReturn(false);
        when(authenticator.canHandleWithUserAssertion(request, response, context)).thenReturn(false);
        when(authenticator.getName()).thenReturn("TestAuthenticator");

        try (MockedStatic<FrameworkUtils> frameworkUtils = mockStatic(FrameworkUtils.class)) {
            frameworkUtils.when(() ->
                    FrameworkUtils.contextHasUserAssertion(any(), any())
            ).thenReturn(true);
            // Should throw FrameworkException
            handler.handleResponse(request, response, context);
        }
    }

    @DataProvider
    public Object[][] accountLockBaseUrlProvider() {
        return new Object[][]{
                {true, "/authenticationendpoint/login.do",
                        "/authenticationendpoint/retry.do", "/authenticationendpoint/retry.do"},
                {false, "/authenticationendpoint/login.do",
                        "/authenticationendpoint/retry.do", "/authenticationendpoint/login.do"}
        };
    }
    @Test(dataProvider = "accountLockBaseUrlProvider")
    public void testAccountLockBaseUrlSelection(boolean isRetryEnabled, String loginPage,
                                                String configRetryUrl, String expectedBaseUrl)
            throws URISyntaxException, IOException {

        try (MockedStatic<IdentityUtil> identityUtil = mockStatic(IdentityUtil.class);
             MockedStatic<ConfigurationFacade> configurationFacade = mockStatic(ConfigurationFacade.class)) {

            ConfigurationFacade mockConfigInstance = mock(ConfigurationFacade.class);
            configurationFacade.when(ConfigurationFacade::getInstance).thenReturn(mockConfigInstance);
            when(mockConfigInstance.getAuthenticationEndpointRetryURL()).thenReturn(configRetryUrl);

            AuthenticationContext context = spy(new AuthenticationContext());
            context.setSendToMultiOptionPage(false);

            Map<String, String> authParameters = new HashMap<>();
            authParameters.put(
                    FrameworkConstants.REDIRECT_TO_RETRY_PAGE_ON_ACCOUNT_LOCK_CONF,
                    String.valueOf(isRetryEnabled));

            AuthenticatorConfig authenticatorConfig = spy(new AuthenticatorConfig());
            when(defaultStepHandler.getAuthenticatorConfig()).thenReturn(authenticatorConfig);
            when(defaultStepHandler.getAuthenticatorConfig().getParameterMap()).thenReturn(authParameters);

            IdentityErrorMsgContext errorMsgContext = mock(IdentityErrorMsgContext.class);
            when(errorMsgContext.getErrorCode()).thenReturn(UserCoreConstants.ErrorCode.USER_IS_LOCKED);
            when(errorMsgContext.getMaximumLoginAttempts()).thenReturn(5);
            when(errorMsgContext.getFailedLoginAttempts()).thenReturn(5);
            identityUtil.when(IdentityUtil::getIdentityErrorMsg).thenReturn(errorMsgContext);

            URIBuilder basicAuthRedirectUrlBuilder =
                    new URIBuilder("https://localhost:9443/authenticationendpoint/login.do");
            basicAuthRedirectUrlBuilder
                    .addParameter(FrameworkConstants.ERROR_CODE, UserCoreConstants.ErrorCode.USER_IS_LOCKED);
            String basicAuthRedirectUrl = basicAuthRedirectUrlBuilder.build().toString();
            response = spy(new CommonAuthResponseWrapper(response));
            when(((CommonAuthResponseWrapper) response).getRedirectURL()).thenReturn(basicAuthRedirectUrl);

            String retryParam = "";
            doReturn(retryParam).when(defaultStepHandler).handleIdentifierFirstLogin(context, retryParam);
            String authenticatorNames = "BasicAuthenticator";
            String showAuthFailureReason = "true"; // Must be true to enter the logic block

            defaultStepHandler.getRedirectUrl(request, response, context, authenticatorNames,
                    showAuthFailureReason, retryParam, loginPage);

            ArgumentCaptor<String> urlCaptor = ArgumentCaptor.forClass(String.class);
            verify(response).encodeRedirectURL(urlCaptor.capture());

            String actualUrl = urlCaptor.getValue();

            Assert.assertTrue(actualUrl.startsWith(expectedBaseUrl),
                    "Expected URL to start with " + expectedBaseUrl + " but found " + actualUrl);
        }
    }

    /**
     * Test that the authenticated user is properly set in the context subject when step is skipped in SSO.
     */
    @Test
    public void testSSOStepSkippingSetsContextSubject() throws FrameworkException {

        String testUsername = "ssoTestUser";

        try (MockedStatic<LoggerUtils> loggerUtils = mockStatic(LoggerUtils.class);
             MockedStatic<FrameworkUtils> frameworkUtils = mockStatic(FrameworkUtils.class)) {

            loggerUtils.when(LoggerUtils::isDiagnosticLogsEnabled).thenReturn(false);

            // Authentication Context Setup.
            AuthenticationContext context = new AuthenticationContext();
            context.initializeAnalyticsData();
            context.setTenantDomain("carbon.super");
            context.setCurrentStep(1);

            // Authenticator Config Setup.
            AuthenticatorConfig authConfig = new AuthenticatorConfig();
            authConfig.setName("BasicAuthenticator");
            authConfig.setIdPNames(Collections.singletonList(FrameworkConstants.LOCAL_IDP_NAME));
            LocalApplicationAuthenticator authenticator = mock(LocalApplicationAuthenticator.class);
            when(authenticator.getName()).thenReturn("BasicAuthenticator");
            when(authenticator.getAuthMechanism()).thenReturn("basic");
            authConfig.setApplicationAuthenticator(authenticator);

            StepConfig stepConfig = new StepConfig();
            stepConfig.setOrder(1);
            stepConfig.setAuthenticatorList(Collections.singletonList(authConfig));
            SequenceConfig sequenceConfig = new SequenceConfig();
            sequenceConfig.setStepMap(Collections.singletonMap(1, stepConfig));
            context.setSequenceConfig(sequenceConfig);

            AuthenticatedUser user = new AuthenticatedUser();
            user.setUserName(testUsername);
            user.setTenantDomain("carbon.super");
            user.setUserStoreDomain("PRIMARY");

            AuthenticatedIdPData idPData = new AuthenticatedIdPData();
            idPData.setIdpName(FrameworkConstants.LOCAL_IDP_NAME);
            idPData.setUser(user);
            idPData.addAuthenticator(authConfig);

            Map<String, AuthenticatedIdPData> authenticatedIdPs = new HashMap<>();
            authenticatedIdPs.put(FrameworkConstants.LOCAL_IDP_NAME, idPData);
            context.setPreviousAuthenticatedIdPs(authenticatedIdPs);
            context.setCurrentAuthenticatedIdPs(new HashMap<>());

            // Mock FrameworkUtils.
            frameworkUtils.when(() -> FrameworkUtils.getAuthenticatedStepIdPs(any(StepConfig.class), any(Map.class)))
                    .thenReturn(Collections.singletonMap(FrameworkConstants.LOCAL_IDP_NAME, authConfig));
            frameworkUtils.when(() -> FrameworkUtils.getAuthenticatorIdPMappingString(anyList()))
                    .thenReturn("BasicAuthenticator:LOCAL");

            defaultStepHandler.handle(request, response, context);

            Assert.assertNotNull(context.getSubject(),
                    "Context subject should be set when step is skipped in SSO.");
            Assert.assertEquals(context.getSubject().getUserName(), testUsername,
                    "Subject username should match the authenticated user.");
            Assert.assertEquals(context.getSubject().getTenantDomain(), "carbon.super",
                    "Subject tenant domain should match the authenticated user.");
        }
    }

    @Test
    public void testAPIBasedFlowWithUnsupportedAuthenticatorSetsErrorProperties() throws Exception {

        try (MockedStatic<LoggerUtils> loggerUtils = mockStatic(LoggerUtils.class);
             MockedStatic<FrameworkUtils> frameworkUtils = mockStatic(FrameworkUtils.class)) {

            HttpServletRequest request = mock(HttpServletRequest.class);
            HttpServletResponse response = mock(HttpServletResponse.class);
            AuthenticationContext context = mock(AuthenticationContext.class);
            SequenceConfig sequenceConfig = mock(SequenceConfig.class);
            StepConfig stepConfig = mock(StepConfig.class);
            AuthenticatorConfig authenticatorConfig = mock(AuthenticatorConfig.class);
            ApplicationAuthenticator authenticator = mock(ApplicationAuthenticator.class);

            when(context.getSequenceConfig()).thenReturn(sequenceConfig);
            when(context.getCurrentStep()).thenReturn(1);
            Map<Integer, StepConfig> stepMap = new HashMap<>();
            stepMap.put(1, stepConfig);
            when(sequenceConfig.getStepMap()).thenReturn(stepMap);
            when(stepConfig.getOrder()).thenReturn(1);
            when(authenticatorConfig.getApplicationAuthenticator()).thenReturn(authenticator);
            when(authenticator.getName()).thenReturn("UnsupportedAuthenticator");
            when(authenticator.getFriendlyName()).thenReturn("Unsupported Authenticator");
            when(authenticator.isAPIBasedAuthenticationSupported()).thenReturn(false);
            when(context.getProperty(FrameworkConstants.AUTH_ERROR_CODE))
                    .thenReturn(FrameworkConstants.ERROR_STATUS_AUTHENTICATOR_NOT_SUPPORTED);
            frameworkUtils.when(() -> FrameworkUtils.isAPIBasedAuthenticationFlow(request)).thenReturn(true);
            loggerUtils.when(LoggerUtils::isDiagnosticLogsEnabled).thenReturn(false);
            defaultStepHandler.doAuthentication(request, response, context, authenticatorConfig);
            verify(context).setProperty(FrameworkConstants.AUTH_ERROR_CODE,
                    FrameworkConstants.ERROR_STATUS_AUTHENTICATOR_NOT_SUPPORTED);
        }
    }

    @Test
    public void testUnsupportedAuthenticatorSetsAuthResultAttributes() throws Exception {

        try (MockedStatic<LoggerUtils> loggerUtils = mockStatic(LoggerUtils.class);
             MockedStatic<FrameworkUtils> frameworkUtils = mockStatic(FrameworkUtils.class)) {

            HttpServletRequest request = mock(HttpServletRequest.class);
            HttpServletResponse response = mock(HttpServletResponse.class);
            AuthenticationContext context = mock(AuthenticationContext.class);
            SequenceConfig sequenceConfig = mock(SequenceConfig.class);
            StepConfig stepConfig = mock(StepConfig.class);
            AuthenticatorConfig authenticatorConfig = mock(AuthenticatorConfig.class);
            ApplicationAuthenticator authenticator = mock(ApplicationAuthenticator.class);

            when(context.getSequenceConfig()).thenReturn(sequenceConfig);
            when(context.getCurrentStep()).thenReturn(1);
            when(context.getProperty(FrameworkConstants.AUTH_ERROR_CODE))
                    .thenReturn(FrameworkConstants.ERROR_STATUS_AUTHENTICATOR_NOT_SUPPORTED);
            Map<Integer, StepConfig> stepMap = new HashMap<>();
            stepMap.put(1, stepConfig);
            when(sequenceConfig.getStepMap()).thenReturn(stepMap);
            when(stepConfig.getOrder()).thenReturn(1);
            when(authenticatorConfig.getApplicationAuthenticator()).thenReturn(authenticator);
            when(authenticator.getName()).thenReturn("TestAuth");
            when(authenticator.getFriendlyName()).thenReturn("Test Authenticator");
            when(authenticator.isAPIBasedAuthenticationSupported()).thenReturn(false);
            frameworkUtils.when(() -> FrameworkUtils.isAPIBasedAuthenticationFlow(request)).thenReturn(true);
            loggerUtils.when(LoggerUtils::isDiagnosticLogsEnabled).thenReturn(false);
            defaultStepHandler.doAuthentication(request, response, context, authenticatorConfig);

            ArgumentCaptor<Boolean> flowConcludedCaptor = ArgumentCaptor.forClass(Boolean.class);
            verify(request).setAttribute(
                    org.mockito.ArgumentMatchers.eq(FrameworkConstants.IS_AUTH_FLOW_CONCLUDED),
                    flowConcludedCaptor.capture());
            Assert.assertTrue(flowConcludedCaptor.getValue(),
                    "Expected IS_AUTH_FLOW_CONCLUDED to be true");
            ArgumentCaptor<AuthenticationResult> authResultCaptor =
                    ArgumentCaptor.forClass(AuthenticationResult.class);
            verify(request).setAttribute(
                    org.mockito.ArgumentMatchers.eq(FrameworkConstants.RequestAttribute.AUTH_RESULT),
                    authResultCaptor.capture());

            AuthenticationResult authResult = authResultCaptor.getValue();
            Assert.assertFalse(authResult.isAuthenticated(),
                    "Expected authentication result to be not authenticated");
            Assert.assertEquals(authResult.getProperty(FrameworkConstants.AUTH_ERROR_CODE),
                    FrameworkConstants.ERROR_STATUS_AUTHENTICATOR_NOT_SUPPORTED);
        }
    }
}
