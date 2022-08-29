/*
 * Copyright (c) 2022, WSO2 LLC. (http://www.wso2.com).
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

import org.apache.http.client.utils.URIBuilder;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Spy;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.testng.Assert;
import org.testng.IObjectFactory;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.ObjectFactory;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.application.authentication.framework.config.model.AuthenticatorConfig;
import org.wso2.carbon.identity.application.authentication.framework.context.AuthenticationContext;
import org.wso2.carbon.identity.application.authentication.framework.model.CommonAuthResponseWrapper;
import org.wso2.carbon.identity.application.authentication.framework.util.FrameworkConstants;
import org.wso2.carbon.identity.application.authentication.framwork.test.utils.CommonTestUtils;
import org.wso2.carbon.identity.common.testng.WithCarbonHome;
import org.wso2.carbon.identity.core.util.IdentityUtil;
import org.wso2.carbon.user.core.UserCoreConstants;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.powermock.api.mockito.PowerMockito.mockStatic;

/**
 * This test ensures that the retry page is returned when relevant properties are set with the values. The default
 * behavior is to return to the login page after login failure.
 */
@PrepareForTest({IdentityUtil.class})
@WithCarbonHome
@PowerMockIgnore("org.mockito.*")
public class DefaultStepHandlerTest {

    @Mock
    HttpServletRequest request;

    @Mock
    HttpServletResponse response;

    @Spy
    DefaultStepHandler defaultStepHandler;

    @ObjectFactory
    public IObjectFactory getObjectFactory() {

        return new org.powermock.modules.testng.PowerMockObjectFactory();
    }

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
        mockStatic(IdentityUtil.class);
        when(IdentityUtil.getIdentityErrorMsg()).thenReturn(null);

        // RetryParam needs to be passed as a parameter for the getRedirectUrl method.
        // Not relevant to the test flow furthermore.
        String retryParam = "";
        doReturn(retryParam).when(defaultStepHandler).handleIdentifierFirstLogin(context, retryParam);

        // The authConfig "showAuthFailureReason" can't be null and should be true.
        String showAuthFailureReason = "true";

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
