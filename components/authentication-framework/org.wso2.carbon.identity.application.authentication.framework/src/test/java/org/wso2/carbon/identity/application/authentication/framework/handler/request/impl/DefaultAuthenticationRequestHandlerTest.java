/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.identity.application.authentication.framework.handler.request.impl;

import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Spy;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.testng.IObjectFactory;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.ObjectFactory;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.application.authentication.framework.cache.AuthenticationResultCacheEntry;
import org.wso2.carbon.identity.application.authentication.framework.config.model.ApplicationConfig;
import org.wso2.carbon.identity.application.authentication.framework.config.model.SequenceConfig;
import org.wso2.carbon.identity.application.authentication.framework.context.AuthenticationContext;
import org.wso2.carbon.identity.application.authentication.framework.exception.FrameworkException;
import org.wso2.carbon.identity.application.authentication.framework.exception.PostAuthenticationFailedException;
import org.wso2.carbon.identity.application.authentication.framework.handler.sequence.impl.DefaultStepBasedSequenceHandler;
import org.wso2.carbon.identity.application.authentication.framework.internal.FrameworkServiceDataHolder;
import org.wso2.carbon.identity.application.authentication.framework.model.AuthenticatedUser;
import org.wso2.carbon.identity.application.authentication.framework.model.AuthenticationResult;
import org.wso2.carbon.identity.application.authentication.framework.model.CommonAuthResponseWrapper;
import org.wso2.carbon.identity.application.authentication.framework.services.PostAuthenticationMgtService;
import org.wso2.carbon.identity.application.authentication.framework.services.PostAuthenticationMgtServiceTest;
import org.wso2.carbon.identity.application.authentication.framework.util.FrameworkConstants;
import org.wso2.carbon.identity.application.authentication.framework.util.FrameworkUtils;
import org.wso2.carbon.identity.application.authentication.framework.util.SessionNonceCookieUtil;
import org.wso2.carbon.identity.application.authentication.framwork.test.utils.CommonTestUtils;
import org.wso2.carbon.identity.application.common.model.LocalAndOutboundAuthenticationConfig;
import org.wso2.carbon.identity.application.common.model.ServiceProvider;
import org.wso2.carbon.identity.application.mgt.ApplicationConstants;
import org.wso2.carbon.identity.common.testng.WithCarbonHome;

import java.io.IOException;
import java.util.UUID;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;
import static org.wso2.carbon.base.MultitenantConstants.SUPER_TENANT_DOMAIN_NAME;

@PrepareForTest({FrameworkUtils.class, SessionNonceCookieUtil.class})
@WithCarbonHome
@PowerMockIgnore("org.mockito.*")
public class DefaultAuthenticationRequestHandlerTest {

    @Mock
    HttpServletRequest request;

    @Mock
    HttpServletResponse response;

    @Spy
    DefaultAuthenticationRequestHandler authenticationRequestHandler;

    @ObjectFactory
    public IObjectFactory getObjectFactory() {

        return new org.powermock.modules.testng.PowerMockObjectFactory();
    }

    @BeforeMethod
    public void setUp() throws Exception {

        initMocks(this);
    }

    @AfterMethod
    public void tearDown() throws Exception {

    }

    @Test
    public void testGetInstance() throws Exception {

        CommonTestUtils.testSingleton(
                DefaultAuthenticationRequestHandler.getInstance(),
                DefaultAuthenticationRequestHandler.getInstance()
        );
    }

    @Test
    public void testHandleDenyFromLoginPage() throws Exception {

        AuthenticationContext context = spy(new AuthenticationContext());
        context.setSequenceConfig(new SequenceConfig());

        // mock the conclude flow
        doNothing().when(authenticationRequestHandler).concludeFlow(request, response, context);
        doNothing().when(authenticationRequestHandler).sendResponse(request, response, context);

        // mock the context to show that flow is returning back from login page
        when(context.isReturning()).thenReturn(true);
        doReturn("DENY").when(request).getParameter(FrameworkConstants.RequestParams.DENY);

        authenticationRequestHandler.handle(request, response, context);

        assertFalse(context.isRequestAuthenticated());
    }

    @DataProvider(name = "rememberMeParamProvider")
    public Object[][] provideRememberMeParam() {

        return new Object[][]{
                {null, false},
                {"on", true},
                // any string other than "on"
                {"off", false}
        };
    }

    @Test(dataProvider = "rememberMeParamProvider")
    public void testHandleRememberMeOptionFromLoginPage(String rememberMeParam,
                                                        boolean expectedResult) throws Exception {

        doReturn(rememberMeParam).when(request).getParameter(FrameworkConstants.RequestParams.REMEMBER_ME);

        AuthenticationContext context = spy(new AuthenticationContext());
        SequenceConfig sequenceConfig = spy(new SequenceConfig());
        when(sequenceConfig.isCompleted()).thenReturn(true);
        ServiceProvider serviceProvider = spy(new ServiceProvider());
        LocalAndOutboundAuthenticationConfig localAndOutboundAuthenticationConfig = spy(new
            LocalAndOutboundAuthenticationConfig());
        when(localAndOutboundAuthenticationConfig.getAuthenticationType()).thenReturn(ApplicationConstants
            .AUTH_TYPE_LOCAL);
        serviceProvider.setLocalAndOutBoundAuthenticationConfig(localAndOutboundAuthenticationConfig);
        ApplicationConfig applicationConfig = spy(new ApplicationConfig(serviceProvider, SUPER_TENANT_DOMAIN_NAME));
        sequenceConfig.setApplicationConfig(applicationConfig);

        context.setSequenceConfig(sequenceConfig);

        // mock the context to show that flow is returning back from login page
        when(context.isReturning()).thenReturn(true);
        when(context.getCurrentStep()).thenReturn(0);


        //mock session nonce cookie validation
        mockStatic(SessionNonceCookieUtil.class);
        when(SessionNonceCookieUtil.validateNonceCookie(any(), any())).thenReturn(true);

        // Mock conclude flow and post authentication flows to isolate remember me option
        doNothing().when(authenticationRequestHandler).concludeFlow(request, response, context);

        authenticationRequestHandler.handle(request, response, context);

        assertEquals(context.isRememberMe(), expectedResult);
    }

    @DataProvider(name = "RequestParamDataProvider")
    public Object[][] provideSequenceStartRequestParams() {

        return new Object[][]{
                {"true", true},
                {"false", false},
                {null, false}
        };
    }

    @Test(dataProvider = "RequestParamDataProvider")
    public void testHandleSequenceStart(String paramValue,
                                        boolean expectedResult) throws Exception {

        AuthenticationContext context = new AuthenticationContext();

        // ForceAuth
        doReturn(paramValue).when(request).getParameter(FrameworkConstants.RequestParams.FORCE_AUTHENTICATE);
        assertFalse(authenticationRequestHandler.handleSequenceStart(request, response, context));
        assertEquals(context.isForceAuthenticate(), expectedResult);

        // Reauthenticate
        doReturn(paramValue).when(request).getParameter(FrameworkConstants.RequestParams.RE_AUTHENTICATE);
        assertFalse(authenticationRequestHandler.handleSequenceStart(request, response, context));
        assertEquals(context.isReAuthenticate(), expectedResult);

        // PassiveAuth
        doReturn(paramValue).when(request).getParameter(FrameworkConstants.RequestParams.PASSIVE_AUTHENTICATION);
        assertFalse(authenticationRequestHandler.handleSequenceStart(request, response, context));
        assertEquals(context.isPassiveAuthenticate(), expectedResult);
    }

    @Test
    public void testConcludeFlow() throws Exception {

    }

    @DataProvider(name = "sendResponseDataProvider")
    public Object[][] provideSendResponseData() {

        return new Object[][]{
                {true, true, "/samlsso", "dummy_data_key", "/samlsso?sessionDataKey=dummy_data_key&chkRemember=on"},
                {true, false, "/samlsso", "dummy_data_key", "/samlsso?sessionDataKey=dummy_data_key"},
                {false, true, "/samlsso", "dummy_data_key", "/samlsso?sessionDataKey=dummy_data_key"},
                {true, true, "/samlsso", null, "/samlsso?chkRemember=on"}
        };
    }

    @Test(dataProvider = "sendResponseDataProvider")
    public void testSendResponse(boolean isRequestAuthenticated,
                                 boolean isRememberMe,
                                 String callerPath,
                                 String sessionDataKey,
                                 String expectedRedirectUrl) throws Exception {

        AuthenticationContext context = new AuthenticationContext();
        context.setRequestAuthenticated(isRequestAuthenticated);
        context.setRememberMe(isRememberMe);
        context.setCallerPath(callerPath);
        context.setCallerSessionKey(sessionDataKey);

        SequenceConfig sequenceConfig = spy(new SequenceConfig());
        context.setSequenceConfig(sequenceConfig);

        DefaultAuthenticationRequestHandler requestHandler = spy(new DefaultAuthenticationRequestHandler());
        doNothing().when(requestHandler).populateErrorInformation(request, response, context);

        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        requestHandler.sendResponse(request, response, context);
        verify(response).sendRedirect(captor.capture());
        assertEquals(captor.getValue(), expectedRedirectUrl);
    }

    @Test(expectedExceptions = FrameworkException.class)
    public void testSendResponseException() throws Exception {

        AuthenticationContext context = new AuthenticationContext();
        context.setRequestAuthenticated(true);
        context.setRememberMe(true);
        context.setCallerPath("/samlsso");
        String sessionDataKey = UUID.randomUUID().toString();
        context.setCallerSessionKey(sessionDataKey);

        SequenceConfig sequenceConfig = spy(new SequenceConfig());
        context.setSequenceConfig(sequenceConfig);

        doThrow(new IOException()).when(response).sendRedirect(anyString());
        authenticationRequestHandler.sendResponse(request, response, context);
    }

    @Test
    public void testHandleAuthorization() throws Exception {

    }

    @DataProvider(name = "errorInfoDataProvider")
    public Object[][] getErrorInfoFormationData() {

        return new Object[][]{
                {"error_code", "error_message", "error_uri", "samlsso"},
                {null, "error_message", "error_uri", "other"},
                {"error_code", null, "error_uri", "other"},
                {"error_code", "error_message", null, "other"},
                {"error_code", "error_message", "error_uri", "other"}
        };

    }

    @Test(dataProvider = "errorInfoDataProvider")
    public void testPopulateErrorInformation(String errorCode,
                                             String errorMessage,
                                             String errorUri,
                                             String requestType) throws Exception {

        AuthenticationResult authenticationResult = new AuthenticationResult();
        doReturn(authenticationResult).when(request).getAttribute(FrameworkConstants.RequestAttribute.AUTH_RESULT);

        // Populate the context with error details
        AuthenticationContext context = new AuthenticationContext();
        context.setProperty(FrameworkConstants.AUTH_ERROR_CODE, errorCode);
        context.setProperty(FrameworkConstants.AUTH_ERROR_MSG, errorMessage);
        context.setProperty(FrameworkConstants.AUTH_ERROR_URI, errorUri);

        // request type is does not cache authentication result
        context.setRequestType(requestType);
        response = spy(new CommonAuthResponseWrapper(response));

        // if request type caches authentication result we need to mock required dependent objects
        AuthenticationResultCacheEntry cacheEntry = spy(new AuthenticationResultCacheEntry());
        when(cacheEntry.getResult()).thenReturn(authenticationResult);
        mockStatic(FrameworkUtils.class);
        when(FrameworkUtils.getAuthenticationResultFromCache(isNull())).thenReturn(cacheEntry);

        authenticationRequestHandler.populateErrorInformation(request, response, context);

        // Assert stuff
        AuthenticationResult modifiedAuthenticationResult =
                (AuthenticationResult) request.getAttribute(FrameworkConstants.RequestAttribute.AUTH_RESULT);

        assertNotNull(modifiedAuthenticationResult);
        assertEquals(modifiedAuthenticationResult.getProperty(FrameworkConstants.AUTH_ERROR_CODE), errorCode);
        assertEquals(modifiedAuthenticationResult.getProperty(FrameworkConstants.AUTH_ERROR_MSG), errorMessage);
        assertEquals(modifiedAuthenticationResult.getProperty(FrameworkConstants.AUTH_ERROR_URI), errorUri);
    }

    @Test
    public void testPostAuthenticationHandlers() throws Exception {

        Cookie[] cookies = new Cookie[1];
        AuthenticationContext context = prepareContextForPostAuthnTests();
        authenticationRequestHandler.handle(request, response, context);
        assertNull(context.getParameter(FrameworkConstants.POST_AUTHENTICATION_EXTENSION_COMPLETED));
        String pastrCookie = context.getParameter(FrameworkConstants.PASTR_COOKIE).toString();
        cookies[0] = new Cookie(FrameworkConstants.PASTR_COOKIE + "-" + context.getContextIdentifier(),
                pastrCookie);
        when(request.getCookies()).thenReturn(cookies);
        when(FrameworkUtils.getCookie(any(HttpServletRequest.class), isNull())).thenReturn
                (cookies[0]);
        authenticationRequestHandler.handle(request, response, context);
        assertTrue(Boolean.parseBoolean(context.getProperty(
                FrameworkConstants.POST_AUTHENTICATION_EXTENSION_COMPLETED).toString()));
    }

    @Test(expectedExceptions = PostAuthenticationFailedException.class)
    public void testPostAuthenticationHandlerFailures() throws Exception {

        Cookie[] cookies = new Cookie[1];
        AuthenticationContext context = prepareContextForPostAuthnTests();
        when(FrameworkUtils.getStepBasedSequenceHandler()).thenReturn(new DefaultStepBasedSequenceHandler());
        authenticationRequestHandler.handle(request, response, context);
        assertNull(context.getParameter(FrameworkConstants.POST_AUTHENTICATION_EXTENSION_COMPLETED));
        String pastrCookie = context.getParameter(FrameworkConstants.PASTR_COOKIE).toString();
        cookies[0] = new Cookie(FrameworkConstants.PASTR_COOKIE + "-" + context.getContextIdentifier(), pastrCookie);
        when(request.getCookies()).thenReturn(cookies);
        when(FrameworkUtils.getCookie(any(HttpServletRequest.class), anyString())).thenReturn
                (new Cookie(FrameworkConstants.PASTR_COOKIE + "-" + context.getContextIdentifier(),
                        "someGibberishValue"));
        authenticationRequestHandler.handle(request, response, context);
        assertTrue(Boolean.parseBoolean(context.getProperty(
                FrameworkConstants.POST_AUTHENTICATION_EXTENSION_COMPLETED).toString()));
    }

    private AuthenticationContext prepareContextForPostAuthnTests() {

        AuthenticationContext context = new AuthenticationContext();
        context.setContextIdentifier(String.valueOf(UUID.randomUUID()));
        addSequence(context, true);
        addApplicationConfig(context);
        setUser(context, "admin");
        setPostAuthnMgtService();
        addPostAuthnHandler();
        mockStatic(FrameworkUtils.class);
        when(FrameworkUtils.getStepBasedSequenceHandler()).thenReturn(new DefaultStepBasedSequenceHandler());
        context.initializeAnalyticsData();
        return context;
    }

    private void addSequence(AuthenticationContext context, boolean isCompleted) {

        SequenceConfig sequenceConfig = new SequenceConfig();
        sequenceConfig.setCompleted(isCompleted);
        context.setSequenceConfig(sequenceConfig);
    }

    private void addApplicationConfig(AuthenticationContext context) {

        ApplicationConfig applicationConfig = new ApplicationConfig(new ServiceProvider(), SUPER_TENANT_DOMAIN_NAME);
        LocalAndOutboundAuthenticationConfig localAndOutboundAuthenticationConfig = new
                LocalAndOutboundAuthenticationConfig();
        applicationConfig.getServiceProvider().setLocalAndOutBoundAuthenticationConfig
                (localAndOutboundAuthenticationConfig);
        context.getSequenceConfig().setApplicationConfig(applicationConfig);
    }

    private void setUser(AuthenticationContext context, String userName) {

        AuthenticatedUser authenticatedUser = new AuthenticatedUser();
        authenticatedUser.setAuthenticatedSubjectIdentifier(userName);
        authenticatedUser.setUserId("4b4414e1-916b-4475-aaee-6b0751c29ff6");
        context.setProperty("user-tenant-domain", SUPER_TENANT_DOMAIN_NAME);
        context.getSequenceConfig().setAuthenticatedUser(authenticatedUser);
    }

    private void addPostAuthnHandler() {

        PostAuthenticationMgtServiceTest.TestPostHandlerWithRedirect postAuthenticationHandler =
                new PostAuthenticationMgtServiceTest.TestPostHandlerWithRedirect();
        postAuthenticationHandler.setEnabled(true);
        FrameworkServiceDataHolder.getInstance().addPostAuthenticationHandler(postAuthenticationHandler);
    }

    private void setPostAuthnMgtService() {

        PostAuthenticationMgtService postAuthenticationMgtService = new PostAuthenticationMgtService();
        FrameworkServiceDataHolder.getInstance().setPostAuthenticationMgtService(postAuthenticationMgtService);
    }
}

