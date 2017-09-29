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

import org.mockito.Mock;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.application.authentication.framework.config.model.SequenceConfig;
import org.wso2.carbon.identity.application.authentication.framework.context.AuthenticationContext;
import org.wso2.carbon.identity.application.authentication.framework.util.FrameworkConstants;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static org.mockito.AdditionalMatchers.not;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.argThat;
import static org.mockito.Matchers.eq;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.powermock.api.mockito.PowerMockito.doNothing;
import static org.powermock.api.mockito.PowerMockito.doReturn;
import static org.powermock.api.mockito.PowerMockito.spy;
import static org.powermock.api.mockito.PowerMockito.when;

public class DefaultAuthenticationRequestHandlerTest {

    @Mock
    HttpServletRequest request;

    @Mock
    HttpServletResponse response;

    private DefaultAuthenticationRequestHandler authenticationRequestHandler;


    @BeforeMethod
    public void setUp() throws Exception {

        initMocks(this);
        authenticationRequestHandler = new DefaultAuthenticationRequestHandler();
    }

    @AfterMethod
    public void tearDown() throws Exception {
    }

    @Test
    public void testGetInstance() throws Exception {

        DefaultAuthenticationRequestHandler instance = DefaultAuthenticationRequestHandler.getInstance();
        Assert.assertNotNull(instance);

        DefaultAuthenticationRequestHandler anotherInstance = DefaultAuthenticationRequestHandler.getInstance();
        Assert.assertNotNull(anotherInstance);

        Assert.assertEquals(instance, anotherInstance);
    }

    @Test
    public void testHandleDenyFromLoginPage() throws Exception {

        AuthenticationContext context = spy(new AuthenticationContext());
        context.setSequenceConfig(new SequenceConfig());

        DefaultAuthenticationRequestHandler authenticationRequestHandler =
                spy(new DefaultAuthenticationRequestHandler());

        // mock the conclude flow
        doNothing().when(authenticationRequestHandler).concludeFlow(request, response, context);
        doNothing().when(authenticationRequestHandler).sendResponse(request, response, context);

        // mock the context to show that flow is returning back from login page
        when(context.isReturning()).thenReturn(true);
        doReturn("DENY").when(request).getParameter(FrameworkConstants.RequestParams.DENY);

        authenticationRequestHandler.handle(request, response, context);

        Assert.assertFalse(context.isRequestAuthenticated());
    }


    @DataProvider(name = "rememberMeParamProvider")
    public Object[][] provideRememberMeParam() {

        return new Object[][] {
                {null, false},
                {"on", true},
                // any string other than "on"
                {"off", false}
        };
    }

    @Test(dataProvider = "rememberMeParamProvider")
    public void testHandleRememberMeOptionFromLoginPage(String rememberMeParam,
                                                        boolean expectedResult) throws Exception{

        doReturn(rememberMeParam).when(request).getParameter(FrameworkConstants.RequestParams.REMEMBER_ME);

        AuthenticationContext context = spy(new AuthenticationContext());
        SequenceConfig sequenceConfig = spy(new SequenceConfig());
        when(sequenceConfig.isCompleted()).thenReturn(true);

        context.setSequenceConfig(sequenceConfig);

        // mock the context to show that flow is returning back from login page
        when(context.isReturning()).thenReturn(true);
        when(context.getCurrentStep()).thenReturn(0);

        DefaultAuthenticationRequestHandler authenticationRequestHandler =
                spy(new DefaultAuthenticationRequestHandler());

        // Mock conclude flow and post authentication flows to isolate remember me option
        doNothing().when(authenticationRequestHandler).concludeFlow(request, response, context);
        doReturn(true).when(authenticationRequestHandler).isPostAuthenticationExtensionCompleted(context);

        authenticationRequestHandler.handle(request, response, context);

        Assert.assertEquals(context.isRememberMe(), expectedResult);
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
        Assert.assertFalse(authenticationRequestHandler.handleSequenceStart(request, response, context));
        Assert.assertEquals(context.isForceAuthenticate(), expectedResult);

        // Reauthenticate
        doReturn(paramValue).when(request).getParameter(FrameworkConstants.RequestParams.RE_AUTHENTICATE);
        Assert.assertFalse(authenticationRequestHandler.handleSequenceStart(request, response, context));
        Assert.assertEquals(context.isReAuthenticate(), expectedResult);

        // PassiveAuth
        doReturn(paramValue).when(request).getParameter(FrameworkConstants.RequestParams.PASSIVE_AUTHENTICATION);
        Assert.assertFalse(authenticationRequestHandler.handleSequenceStart(request, response, context));
        Assert.assertEquals(context.isPassiveAuthenticate(), expectedResult);
    }

    @Test
    public void testConcludeFlow() throws Exception {
    }

    @Test
    public void testSendResponse() throws Exception {
    }

    @Test
    public void testHandleAuthorization() throws Exception {
    }

    @DataProvider(name = "postAuthExtensionParam")
    public Object[][] getPostAuthExtensionParam(){

        return new Object[][] {
                {Boolean.TRUE, true},
                {Boolean.FALSE, false},
                {null, false},
                {new Object(), false}
        };
    }

    @Test(dataProvider="postAuthExtensionParam")
    public void testIsPostAuthenticationExtensionCompleted(Object postAuthExtensionCompleted,
                                                           boolean expectedResult) throws Exception {

        AuthenticationContext authenticationContext = new AuthenticationContext();
        authenticationContext
                .setProperty(FrameworkConstants.POST_AUTHENTICATION_EXTENSION_COMPLETED, postAuthExtensionCompleted);

        Assert.assertEquals(
                authenticationRequestHandler.isPostAuthenticationExtensionCompleted(authenticationContext),
                expectedResult
        );
    }

    @Test
    public void testPopulateErrorInformation() throws Exception {
    }

}
