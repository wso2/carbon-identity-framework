/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.identity.application.authentication.framework.handler.request.impl;

import org.junit.Assert;
import org.mockito.MockedStatic;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.application.authentication.framework.config.ConfigurationFacade;
import org.wso2.carbon.identity.application.authentication.framework.config.model.SequenceConfig;
import org.wso2.carbon.identity.application.authentication.framework.context.AuthenticationContext;
import org.wso2.carbon.identity.application.authentication.framework.exception.FrameworkException;
import org.wso2.carbon.identity.application.authentication.framework.model.CommonAuthRequestWrapper;
import org.wso2.carbon.identity.application.authentication.framework.model.CommonAuthResponseWrapper;
import org.wso2.carbon.identity.application.authentication.framework.util.FrameworkConstants;
import org.wso2.carbon.identity.application.authentication.framework.util.FrameworkUtils;
import org.wso2.carbon.identity.application.authentication.framework.util.SessionNonceCookieUtil;
import org.wso2.carbon.identity.application.common.IdentityApplicationManagementException;
import org.wso2.carbon.identity.application.common.model.ServiceProvider;
import org.wso2.carbon.identity.application.mgt.ApplicationManagementService;
import org.wso2.carbon.identity.application.mgt.ApplicationManagementServiceImpl;
import org.wso2.carbon.identity.central.log.mgt.internal.CentralLogMgtServiceComponentHolder;
import org.wso2.carbon.identity.common.testng.WithCarbonHome;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;
import org.wso2.carbon.identity.event.services.IdentityEventService;
import org.wso2.carbon.identity.testutil.IdentityBaseTest;

import java.io.IOException;
import java.io.Serializable;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.wso2.carbon.identity.application.authentication.framework.util.FrameworkConstants.ERROR_DESCRIPTION_APP_DISABLED;
import static org.wso2.carbon.identity.application.authentication.framework.util.FrameworkConstants.ERROR_STATUS_APP_DISABLED;
import static org.wso2.carbon.identity.application.authentication.framework.util.FrameworkConstants.RequestParams.LOGOUT;
import static org.wso2.carbon.identity.application.authentication.framework.util.FrameworkConstants.RequestParams.TENANT_DOMAIN;
import static org.wso2.carbon.identity.application.authentication.framework.util.FrameworkConstants.RequestParams.TYPE;
import static org.wso2.carbon.identity.application.authentication.framework.util.SessionNonceCookieUtil.NONCE_ERROR_CODE;
import static org.wso2.carbon.utils.multitenancy.MultitenantConstants.SUPER_TENANT_DOMAIN_NAME;

/**
 * Unit tests for {@link DefaultRequestCoordinator}.
 */
@WithCarbonHome
public class DefaultRequestCoordinatorTest extends IdentityBaseTest {

    private DefaultRequestCoordinator requestCoordinator;

    @BeforeMethod
    public void setUp() throws Exception {

        requestCoordinator = new DefaultRequestCoordinator();
    }

    @AfterMethod
    public void tearDown() throws Exception {

    }

    @AfterClass
    public void tearDownClass() throws Exception {
        CentralLogMgtServiceComponentHolder.getInstance().setIdentityEventService(null);
    }

    @DataProvider(name = "tenantDomainProvider")
    public Object[][] provideTenantDomain() {

        return new Object[][]{
                {true, null, null, SUPER_TENANT_DOMAIN_NAME},
                {false, null, null, SUPER_TENANT_DOMAIN_NAME},

                {true, "foo.com", "xyz.com", "foo.com"},
                {false, "foo.com", "xyz.com", "xyz.com"},

                {true, null, "xyz.com", "xyz.com"},
                {false, null, "xyz.com", "xyz.com"},
        };
    }

    @Test(dataProvider = "tenantDomainProvider")
    public void testTenantDomainInAuthenticationContext(boolean isTenantQualifiedUrlModeEnabled,
                                                        String tenantDomainInThreadLocal,
                                                        String tenantDomainInRequestParam,
                                                        String expected) throws Exception {

        try (MockedStatic<IdentityTenantUtil> identityTenantUtil = mockStatic(IdentityTenantUtil.class)) {
            identityTenantUtil.when(
                    IdentityTenantUtil::isTenantQualifiedUrlsEnabled).thenReturn(isTenantQualifiedUrlModeEnabled);
            identityTenantUtil.when(IdentityTenantUtil::getTenantDomainFromContext)
                    .thenReturn(tenantDomainInThreadLocal);
            IdentityEventService identityEventService = mock(IdentityEventService.class);
            CentralLogMgtServiceComponentHolder.getInstance().setIdentityEventService(identityEventService);

            HttpServletRequest request = mock(HttpServletRequest.class);
            when(request.getParameter(TYPE)).thenReturn("oauth");
            when(request.getParameter(LOGOUT)).thenReturn("true");
            when(request.getParameter(TENANT_DOMAIN)).thenReturn(tenantDomainInRequestParam);

            HttpServletResponse response = mock(HttpServletResponse.class);

            AuthenticationContext context = requestCoordinator.initializeFlow(request, response);

            assertEquals(context.getTenantDomain(), expected);
        }
    }

    @DataProvider(name = "contextDataProvider")
    public Object[][] contextData() {

        return new Object[][]{
                {"dummy_key", null, "xyz.com", SUPER_TENANT_DOMAIN_NAME},
                {"dummy_key", "samlsso", null, SUPER_TENANT_DOMAIN_NAME},
                {null, "oAuth", "xyz.com", "foo.com"},
                {"dummy_key", "openid", "xyz.com", null},
        };
    }

    @Test(dataProvider = "contextDataProvider")
    public void testClonedContext(String sessionDataKey,
                                  String type,
                                  String relyingParty,
                                  String tenantDomain) throws Exception {

        AuthenticationContext context = new AuthenticationContext();
        context.setCallerSessionKey(sessionDataKey);
        context.setRequestType(type);
        context.setRelyingParty(relyingParty);
        context.setTenantDomain(tenantDomain);
        int initialStep = context.getCurrentStep();
        String initialAuthenticator = context.getCurrentAuthenticator();
        context.setProperty(FrameworkConstants.INITIAL_CONTEXT, context.clone());
        AuthenticationContext clonedContext = (AuthenticationContext)
                context.getProperty(FrameworkConstants.INITIAL_CONTEXT);
        assertEquals(clonedContext.getCallerSessionKey(), sessionDataKey);
        assertEquals(clonedContext.getRequestType(), type);
        assertEquals(clonedContext.getRelyingParty(), relyingParty);
        assertEquals(clonedContext.getTenantDomain(), tenantDomain);

        //Check the cloned context after updating
        context.setCurrentStep(initialStep + 1);
        context.setCurrentAuthenticator("dummyAuthenticator");
        assertEquals(clonedContext.getCurrentStep(), initialStep);
        assertEquals(clonedContext.getCurrentAuthenticator(), initialAuthenticator);
    }

    @Test
    public void testCookieValidationFailedException() {

        try (MockedStatic<FrameworkUtils> frameworkUtils = mockStatic(FrameworkUtils.class);
             MockedStatic<SessionNonceCookieUtil> sessionNonceCookieUtil =
                     mockStatic(SessionNonceCookieUtil.class)) {
            HttpServletRequest request = mock(HttpServletRequest.class);
            HttpServletResponse response = mock(HttpServletResponse.class);
            SequenceConfig sequenceConfig = mock(SequenceConfig.class);
            DefaultAuthenticationRequestHandler authenticationRequestHandler =
                    new DefaultAuthenticationRequestHandler();
            AuthenticationContext context = mock(AuthenticationContext.class);
            when(context.getSequenceConfig()).thenReturn(sequenceConfig);
            when(sequenceConfig.getReqPathAuthenticators()).thenReturn(new ArrayList<>());
            sessionNonceCookieUtil.when(() -> SessionNonceCookieUtil.isNonceCookieEnabled()).thenReturn(true);
            sessionNonceCookieUtil.when(() -> SessionNonceCookieUtil.getNonceCookieName(context)).thenReturn("nonce");
            when(context.isReturning()).thenReturn(true);
            sessionNonceCookieUtil.when(() -> SessionNonceCookieUtil.validateNonceCookie(request, context))
                    .thenReturn(false);
            frameworkUtils.when(() -> FrameworkUtils.getContextData(request)).thenReturn(context);
            frameworkUtils.when(() -> FrameworkUtils.getAuthenticationRequestHandler())
                    .thenReturn(authenticationRequestHandler);
            try {
                authenticationRequestHandler.handle(request, response, context);
            } catch (FrameworkException e) {
                assertEquals(e.getErrorCode(), NONCE_ERROR_CODE);
            }
        }
    }

    @Test
    public void testApplicationDisabled() {

        try (MockedStatic<FrameworkUtils> frameworkUtils = mockStatic(FrameworkUtils.class);
             MockedStatic<ApplicationManagementService> applicationManagementService =
                     mockStatic(ApplicationManagementService.class);
             MockedStatic<ConfigurationFacade> configurationFacade = mockStatic(ConfigurationFacade.class)) {

            String requestType = "oauth2";
            String relyingParty = "console";
            String tenantDomain = "carbon.super";

            HttpServletRequest requestMock = spy(HttpServletRequest.class);
            HttpServletResponse responseMock = spy(HttpServletResponse.class);
            CommonAuthRequestWrapper request = new CommonAuthRequestWrapper(requestMock);
            CommonAuthResponseWrapper response = new CommonAuthResponseWrapper(responseMock);
            AuthenticationContext context = mock(AuthenticationContext.class);
            DefaultRequestCoordinator defaultRequestCoordinator = new DefaultRequestCoordinator();

            // Mocking request and context parameters
            when(request.getParameter(FrameworkConstants.RequestParams.ISSUER)).thenReturn(relyingParty);
            when(request.getParameter(TENANT_DOMAIN)).thenReturn(tenantDomain);
            when(context.getRequestType()).thenReturn(requestType);
            when(context.getTenantDomain()).thenReturn(tenantDomain);
            when(context.getServiceProviderName()).thenReturn("consoleApplication");
            when(context.getEndpointParams()).thenReturn(new HashMap<>());
            when(context.getSessionIdentifier()).thenReturn("randomKey");

            // Mocking FrameworkUtils
            frameworkUtils.when(() -> FrameworkUtils.getContextData(request)).thenReturn(context);

            // Mocking ApplicationManagementService behavior
            ApplicationManagementServiceImpl mockApplicationManagementService =
                    mock(ApplicationManagementServiceImpl.class);
            applicationManagementService.when(ApplicationManagementService::getInstance)
                    .thenReturn(mockApplicationManagementService);

            // Mocking ServiceProvider and its properties
            ServiceProvider serviceProvider = mock(ServiceProvider.class);
            when(serviceProvider.isApplicationEnabled()).thenReturn(false);  // ServiceProvider is disabled
            when(mockApplicationManagementService.getServiceProviderByClientId(anyString(), anyString(), anyString()))
                    .thenReturn(serviceProvider);

            ConfigurationFacade configurationFacadeInstance = mock(ConfigurationFacade.class);
            configurationFacade.when(ConfigurationFacade::getInstance).thenReturn(configurationFacadeInstance);
            when(configurationFacadeInstance.getAuthenticationEndpointRetryURL())
                    .thenReturn("https://localhost:9443/retry");

            frameworkUtils.when(() -> FrameworkUtils.sendToRetryPage(any(), any(), any(), any(), any())).
                    thenCallRealMethod();

            frameworkUtils.when(() -> FrameworkUtils.getRedirectURL(any(), any())).thenCallRealMethod();

            frameworkUtils.when(() -> FrameworkUtils.addAuthenticationErrorToCache(any(), any(), any())).
                    thenAnswer(invocation -> null);

            frameworkUtils.when(() -> FrameworkUtils.getRedirectURLWithFilteredParams(any(),
                            (AuthenticationContext) any())).thenCallRealMethod();

            frameworkUtils.when(() -> FrameworkUtils.getRedirectURLWithFilteredParams(any(),
                            (Map<String, Serializable>) any())).thenCallRealMethod();

            // Invoke handle method
            defaultRequestCoordinator.handle(request, response);

            Map<String, String> queryParams = getQueryParams(response.getRedirectURL());
            String status = queryParams.get(FrameworkConstants.STATUS_PARAM);
            String statusMsg = queryParams.get(FrameworkConstants.STATUS_MSG_PARAM);

            // Assert the response
            Assert.assertEquals(status, ERROR_STATUS_APP_DISABLED);
            Assert.assertEquals(statusMsg, ERROR_DESCRIPTION_APP_DISABLED);

        } catch (IdentityApplicationManagementException | IOException | URISyntaxException e) {
            Assert.fail("Exception occurred: " + e.getMessage());
        }
    }

    private Map<String, String> getQueryParams(String url) throws URISyntaxException {

        Map<String, String> queryPairs = new HashMap<>();
        String query = url.substring(url.indexOf('?') + 1);
        String[] pairs = query.split("&");
        for (String pair : pairs) {
            int idx = pair.indexOf("=");
            String key = pair.substring(0, idx);
            String value = pair.substring(idx + 1);
            queryPairs.put(key, value);
        }
        return queryPairs;
    }
}
