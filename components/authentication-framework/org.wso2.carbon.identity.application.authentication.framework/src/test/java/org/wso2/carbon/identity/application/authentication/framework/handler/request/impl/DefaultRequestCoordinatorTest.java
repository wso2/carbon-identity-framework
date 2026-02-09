/*
 * Copyright (c) 2020-2025, WSO2 LLC. (http://www.wso2.com).
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
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.identity.application.authentication.framework.config.ConfigurationFacade;
import org.wso2.carbon.identity.application.authentication.framework.config.model.ApplicationConfig;
import org.wso2.carbon.identity.application.authentication.framework.config.model.SequenceConfig;
import org.wso2.carbon.identity.application.authentication.framework.config.model.graph.AuthGraphNode;
import org.wso2.carbon.identity.application.authentication.framework.config.model.graph.ShowPromptNode;
import org.wso2.carbon.identity.application.authentication.framework.context.AuthenticationContext;
import org.wso2.carbon.identity.application.authentication.framework.context.SessionContext;
import org.wso2.carbon.identity.application.authentication.framework.exception.CookieValidationFailedException;
import org.wso2.carbon.identity.application.authentication.framework.exception.FrameworkException;
import org.wso2.carbon.identity.application.authentication.framework.model.AuthenticatedUser;
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
import org.wso2.carbon.identity.central.log.mgt.utils.LoggerUtils;
import org.wso2.carbon.identity.common.testng.WithCarbonHome;
import org.wso2.carbon.identity.core.context.IdentityContext;
import org.wso2.carbon.identity.core.context.model.UserActor;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;
import org.wso2.carbon.identity.core.util.IdentityUtil;
import org.wso2.carbon.identity.event.services.IdentityEventService;
import org.wso2.carbon.identity.testutil.IdentityBaseTest;

import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.Method;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;
import static org.wso2.carbon.identity.application.authentication.framework.util.FrameworkConstants.AUTHENTICATOR;
import static org.wso2.carbon.identity.application.authentication.framework.util.FrameworkConstants.ERROR_DESCRIPTION_APP_DISABLED;
import static org.wso2.carbon.identity.application.authentication.framework.util.FrameworkConstants.ERROR_STATUS_APP_DISABLED;
import static org.wso2.carbon.identity.application.authentication.framework.util.FrameworkConstants.ORGANIZATION_AUTHENTICATOR;
import static org.wso2.carbon.identity.application.authentication.framework.util.FrameworkConstants.RequestParams.CALLER_PATH;
import static org.wso2.carbon.identity.application.authentication.framework.util.FrameworkConstants.RequestParams.LOGOUT;
import static org.wso2.carbon.identity.application.authentication.framework.util.FrameworkConstants.RequestParams.TENANT_DOMAIN;
import static org.wso2.carbon.identity.application.authentication.framework.util.FrameworkConstants.RequestParams.TYPE;
import static org.wso2.carbon.identity.application.authentication.framework.util.FrameworkConstants.USER_TENANT_DOMAIN;
import static org.wso2.carbon.identity.application.authentication.framework.util.FrameworkErrorConstants.ErrorMessages.ERROR_MISMATCHING_TENANT_DOMAIN;
import static org.wso2.carbon.identity.application.authentication.framework.util.SessionNonceCookieUtil.NONCE_ERROR_CODE;
import static org.wso2.carbon.utils.multitenancy.MultitenantConstants.SUPER_TENANT_DOMAIN_NAME;

/**
 * Unit tests for {@link DefaultRequestCoordinator}.
 */
@WithCarbonHome
public class DefaultRequestCoordinatorTest extends IdentityBaseTest {

    private DefaultRequestCoordinator requestCoordinator;
    private static final String OAUTH = "oauth";
    private static final String TRUE = "true";
    private static final String CUSTOM_TENANT_DOMAIN = "xyz.com";
    private static final String SUPER_TENANT_PATH = "/t/carbon.super/";
    private static final String ORGANIZATION_ID = "179fa754-dcff-48f3-bd1c-a8ea84c8bbf4";
    private static final String ORG_CALLER_PATH = "/o/179fa754-dcff-48f3-bd1c-a8ea84c8bbf4/";
    private static final String ABSOLUTE_CALLER_PATH = "https://wso2.is.com";

    @BeforeMethod
    public void setUp() throws Exception {

        requestCoordinator = new DefaultRequestCoordinator();
        addActorToIdentityContext();
    }

    @AfterMethod
    public void tearDown() throws Exception {

        IdentityContext.destroyCurrentContext();
        IdentityUtil.threadLocalProperties.remove();
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

                {true, "foo.com", CUSTOM_TENANT_DOMAIN, "foo.com"},
                {false, "foo.com", CUSTOM_TENANT_DOMAIN, CUSTOM_TENANT_DOMAIN},

                {true, null, CUSTOM_TENANT_DOMAIN, CUSTOM_TENANT_DOMAIN},
                {false, null, CUSTOM_TENANT_DOMAIN, CUSTOM_TENANT_DOMAIN},
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
            identityTenantUtil.when(
                    IdentityTenantUtil::shouldUseTenantQualifiedURLs).thenReturn(isTenantQualifiedUrlModeEnabled);
            identityTenantUtil.when(IdentityTenantUtil::getTenantDomainFromContext)
                    .thenReturn(tenantDomainInThreadLocal);
            IdentityEventService identityEventService = mock(IdentityEventService.class);
            CentralLogMgtServiceComponentHolder.getInstance().setIdentityEventService(identityEventService);

            HttpServletRequest request = mock(HttpServletRequest.class);
            when(request.getParameter(TYPE)).thenReturn(OAUTH);
            when(request.getParameter(LOGOUT)).thenReturn(TRUE);
            when(request.getParameter(TENANT_DOMAIN)).thenReturn(tenantDomainInRequestParam);

            HttpServletResponse response = mock(HttpServletResponse.class);

            AuthenticationContext context = requestCoordinator.initializeFlow(request, response);

            assertEquals(context.getTenantDomain(), expected);
        }
    }

    @DataProvider(name = "callerPathProvider")
    public Object[][] provideCommonAuthCallerPath() {

        return new Object[][]{
                {SUPER_TENANT_DOMAIN_NAME, SUPER_TENANT_PATH, SUPER_TENANT_PATH},
                {ORGANIZATION_ID, ORG_CALLER_PATH, ORG_CALLER_PATH},
                {CUSTOM_TENANT_DOMAIN, ORG_CALLER_PATH, ORG_CALLER_PATH},
                {SUPER_TENANT_DOMAIN_NAME, "/" + OAUTH, SUPER_TENANT_PATH + OAUTH},
                {SUPER_TENANT_DOMAIN_NAME, ABSOLUTE_CALLER_PATH, ABSOLUTE_CALLER_PATH},
        };
    }

    @Test(dataProvider = "callerPathProvider")
    public void testCallerPathInAuthenticationContext(String tenantDomain, String callerPath, String expectedCallerPath)
            throws Exception {

        try (MockedStatic<IdentityTenantUtil> identityTenantUtil = mockStatic(IdentityTenantUtil.class)) {

            identityTenantUtil.when(IdentityTenantUtil::getTenantDomainFromContext).thenReturn(tenantDomain);
            identityTenantUtil.when(IdentityTenantUtil::isTenantedSessionsEnabled).thenReturn(true);
            CentralLogMgtServiceComponentHolder.getInstance().setIdentityEventService(mock(IdentityEventService.class));
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setOrganizationId(ORGANIZATION_ID);

            HttpServletResponse response = mock(HttpServletResponse.class);
            HttpServletRequest request = mock(HttpServletRequest.class);
            when(request.getParameter(TYPE)).thenReturn(OAUTH);
            when(request.getParameter(LOGOUT)).thenReturn(TRUE);
            when(request.getParameter(CALLER_PATH)).thenReturn(callerPath);

            AuthenticationContext context = requestCoordinator.initializeFlow(request, response);
            assertEquals(context.getCallerPath(), expectedCallerPath);
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setOrganizationId(null);
        }
    }

    @DataProvider(name = "preserveNestedRedirectParamsProvider")
    public Object[][] preserveNestedRedirectParamsProvider() {
        return new Object[][]{
                { false, "/commonauth", "true",  false }, // 1) Config disabled -> false.
                { true,  "/oauth2/authorize", "true",  false }, // 2) Config enabled, NOT /commonauth endpoint -> false.
                { true,  "/commonauth", "false", false }, // 3) Config enabled, /commonauth, logout != true -> false.
                { true,  "/commonauth", "true",  true }, // 4) Config enabled, /commonauth, logout=true -> true.
                { true,  null, "true",  false } // 5) Config enabled, null URI (can happen in some mocks) -> false.
        };
    }

    @Test(dataProvider = "preserveNestedRedirectParamsProvider")
    public void testShouldPreserveNestedRedirectParamsInCommonAuthLogout(boolean configEnabled,
                                                                         String uri,
                                                                         String logoutParam,
                                                                         boolean expected) throws Exception {

        try (MockedStatic<FrameworkUtils> frameworkUtils = mockStatic(FrameworkUtils.class)) {
            // Stub the new config accessor.
            frameworkUtils.when(FrameworkUtils::isNestedRedirectParamsInLogoutReturnUrlEnabled)
                    .thenReturn(configEnabled);

            // Mock request.
            HttpServletRequest request = mock(HttpServletRequest.class);
            when(request.getRequestURI()).thenReturn(uri);
            when(request.getParameter(LOGOUT)).thenReturn(logoutParam);

            // Invoke the private method via reflection.
            Method m = DefaultRequestCoordinator.class.getDeclaredMethod(
                    "shouldPreserveNestedRedirectParamsInCommonAuthLogout", HttpServletRequest.class);
            m.setAccessible(true);
            boolean result = (boolean) m.invoke(requestCoordinator, request);

            assertEquals(result, expected);
        }
    }


    @DataProvider(name = "contextDataProvider")
    public Object[][] contextData() {

        return new Object[][]{
                {"dummy_key", null, CUSTOM_TENANT_DOMAIN, SUPER_TENANT_DOMAIN_NAME},
                {"dummy_key", "samlsso", null, SUPER_TENANT_DOMAIN_NAME},
                {null, "oAuth", CUSTOM_TENANT_DOMAIN, "foo.com"},
                {"dummy_key", "openid", CUSTOM_TENANT_DOMAIN, null},
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

    @Test(description = "Test for generic exception in handle method")
    public void testHandleWithGenericException() throws FrameworkException, IOException {

        try (MockedStatic<FrameworkUtils> frameworkUtils = mockStatic(FrameworkUtils.class)) {
            HttpServletRequest request = mock(HttpServletRequest.class);
            when(request.getParameter("sessionDataKey")).thenReturn("sdKey");
            when(request.getParameter("type")).thenReturn("sso");
            HttpServletResponse response = mock(HttpServletResponse.class);
            frameworkUtils.when(() -> FrameworkUtils.sendToRetryPage(any(), any(), any(), any(), any()))
                    .thenAnswer(invocation -> {
                        ((HttpServletResponse) invocation.getArgument(1)).sendRedirect("dummyUrl");
                        return null;
                    });
            frameworkUtils.when(() -> FrameworkUtils.getAuthenticationRequestFromCache(anyString()))
                    .thenReturn(null);
            DefaultRequestCoordinator coordinator = new DefaultRequestCoordinator();
            coordinator.handle(request, response);
            verify(response, atLeastOnce()).sendRedirect(anyString());
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

    @Test
    public void testTenantMismatchInCommonAuthCookie () throws Exception {

        try (MockedStatic<FrameworkUtils> frameworkUtils = mockStatic(FrameworkUtils.class);
             MockedStatic<ApplicationManagementService> applicationManagementService =
                     mockStatic(ApplicationManagementService.class)) {

            String relyingParty = "console";
            String tenantDomain = "carbon.super";
            String restartLoginFlow = TRUE;

            HttpServletRequest requestMock = spy(HttpServletRequest.class);
            HttpServletResponse responseMock = spy(HttpServletResponse.class);
            CommonAuthRequestWrapper request = new CommonAuthRequestWrapper(requestMock);
            CommonAuthResponseWrapper response = new CommonAuthResponseWrapper(responseMock);
            DefaultAuthenticationRequestHandler authenticationRequestHandler =
                    mock(DefaultAuthenticationRequestHandler.class);

            DefaultRequestCoordinator defaultRequestCoordinator = new DefaultRequestCoordinator();

            Cookie cookie = new Cookie(FrameworkConstants.COMMONAUTH_COOKIE, "/carbon.super");

            // Mocking request parameters
            when(request.getParameter(FrameworkConstants.RequestParams.ISSUER)).thenReturn(relyingParty);
            when(request.getParameter(TENANT_DOMAIN)).thenReturn(tenantDomain);
            when(request.getAttribute(FrameworkConstants.RESTART_LOGIN_FLOW)).thenReturn(restartLoginFlow);
            when(request.getParameter(AUTHENTICATOR)).thenReturn(ORGANIZATION_AUTHENTICATOR);
            when(request.getAttribute(FrameworkConstants.REMOVE_COMMONAUTH_COOKIE)).thenReturn("true");
            when(request.getCookies()).thenReturn(new Cookie[]{cookie});

            // Creating a new AuthenticationContext
            AuthenticationContext context = new AuthenticationContext();
            context.setTenantDomain(tenantDomain);
            context.setServiceProviderName("consoleApplication");
            context.setRequestType("oauth2");
            context.setProperty(FrameworkConstants.INITIAL_CONTEXT, context.clone());

            frameworkUtils.when(() -> FrameworkUtils.sendToRetryPage(any(), any(), any()))
                    .thenThrow(new NullPointerException("Error occurred"));

            when(FrameworkUtils.getContextData(request)).thenAnswer(invocation -> context);
            when(FrameworkUtils.getAuthenticationRequestHandler()).thenReturn(authenticationRequestHandler);
            doThrow(new FrameworkException(
                ERROR_MISMATCHING_TENANT_DOMAIN.getCode(),
                ERROR_MISMATCHING_TENANT_DOMAIN.getMessage()))
                .when(authenticationRequestHandler).handle(any(), any(), any());

            // Mocking ApplicationManagementService behavior
            ApplicationManagementServiceImpl mockApplicationManagementService =
                    mock(ApplicationManagementServiceImpl.class);
            applicationManagementService.when(ApplicationManagementService::getInstance)
                    .thenReturn(mockApplicationManagementService);

            // Mocking ServiceProvider and its properties
            ServiceProvider serviceProvider = mock(ServiceProvider.class);
            when(serviceProvider.isApplicationEnabled()).thenReturn(true);
            when(mockApplicationManagementService.getServiceProviderByClientId(anyString(), anyString(), anyString()))
                    .thenReturn(serviceProvider);

            // Invoke handle method
            assertThrows(CookieValidationFailedException.class, () -> {
                defaultRequestCoordinator.handle(request, response);
            });

            verify(requestMock, times(1)).setAttribute(
                    FrameworkConstants.REMOVE_COMMONAUTH_COOKIE, Boolean.TRUE);
            verify(requestMock, times(1)).setAttribute(
                    FrameworkConstants.RESTART_LOGIN_FLOW, "true");
        } catch (NullPointerException e) {
            Assert.fail("NullPointerException occurred: " + e.getMessage());
        } catch (Exception e) {
            Assert.fail("Exception occurred: " + e.getMessage());
        }
    }

    //@Test
    public void testNonNullContext() {

        try (MockedStatic<FrameworkUtils> frameworkUtils = mockStatic(FrameworkUtils.class);
             MockedStatic<ApplicationManagementService> applicationManagementService =
                     mockStatic(ApplicationManagementService.class)) {

            String relyingParty = "console";
            String tenantDomain = "carbon.super";
            String restartLoginFlow = TRUE;

            HttpServletRequest requestMock = spy(HttpServletRequest.class);
            HttpServletResponse responseMock = spy(HttpServletResponse.class);
            CommonAuthRequestWrapper request = new CommonAuthRequestWrapper(requestMock);
            CommonAuthResponseWrapper response = new CommonAuthResponseWrapper(responseMock);
            DefaultAuthenticationRequestHandler authenticationRequestHandler =
                    mock(DefaultAuthenticationRequestHandler.class);

            DefaultRequestCoordinator defaultRequestCoordinator = new DefaultRequestCoordinator();

            // Mocking request parameters
            when(request.getParameter(FrameworkConstants.RequestParams.ISSUER)).thenReturn(relyingParty);
            when(request.getParameter(TENANT_DOMAIN)).thenReturn(tenantDomain);
            when(request.getAttribute(FrameworkConstants.RESTART_LOGIN_FLOW)).thenReturn(restartLoginFlow);
            when(request.getParameter(AUTHENTICATOR)).thenReturn(ORGANIZATION_AUTHENTICATOR);

            // Creating a new AuthenticationContext
            AuthenticationContext context = new AuthenticationContext();
            context.setTenantDomain(tenantDomain);
            context.setServiceProviderName("consoleApplication");
            context.setRequestType("oauth2");
            context.setProperty(FrameworkConstants.INITIAL_CONTEXT, context.clone());

            frameworkUtils.when(() -> FrameworkUtils.sendToRetryPage(any(), any(), any()))
                    .thenThrow(new NullPointerException("Error occurred"));

            when(FrameworkUtils.getContextData(request)).thenAnswer(invocation -> context);
            when(FrameworkUtils.getAuthenticationRequestHandler()).thenReturn(authenticationRequestHandler);
            doNothing().when(authenticationRequestHandler).handle(request, response, context);

            // Mocking ApplicationManagementService behavior
            ApplicationManagementServiceImpl mockApplicationManagementService =
                    mock(ApplicationManagementServiceImpl.class);
            applicationManagementService.when(ApplicationManagementService::getInstance)
                    .thenReturn(mockApplicationManagementService);

            // Mocking ServiceProvider and its properties
            ServiceProvider serviceProvider = mock(ServiceProvider.class);
            when(serviceProvider.isApplicationEnabled()).thenReturn(true);
            when(mockApplicationManagementService.getServiceProviderByClientId(anyString(), anyString(), anyString()))
                    .thenReturn(serviceProvider);

            // Invoke handle method
            defaultRequestCoordinator.handle(request, response);
            // Return the context to the initial state
            when(FrameworkUtils.getContextData(request)).thenAnswer(
                    invocation -> context.getProperty(FrameworkConstants.INITIAL_CONTEXT));
            // Invoke handle method again
            defaultRequestCoordinator.handle(request, response);

        } catch (NullPointerException e) {
            Assert.fail("NullPointerException occurred: " + e.getMessage());
        } catch (Exception e) {
            Assert.fail("Exception occurred: " + e.getMessage());
        }
    }

    @Test
    public void testHandlePromptRequest() {

        try (MockedStatic<FrameworkUtils> frameworkUtils = mockStatic(FrameworkUtils.class);
             MockedStatic<ApplicationManagementService> applicationManagementService =
                     mockStatic(ApplicationManagementService.class)) {

            String relyingParty = "console";
            String tenantDomain = "carbon.super";
            String restartLoginFlow = TRUE;

            AuthGraphNode showPromptNode = mock(ShowPromptNode.class);
            AuthGraphNode authGraphNode = mock(AuthGraphNode.class);
            when(authGraphNode.getParent()).thenReturn(showPromptNode);

            HttpServletRequest requestMock = spy(HttpServletRequest.class);
            HttpServletResponse responseMock = spy(HttpServletResponse.class);
            CommonAuthRequestWrapper request = new CommonAuthRequestWrapper(requestMock);
            CommonAuthResponseWrapper response = new CommonAuthResponseWrapper(responseMock);
            DefaultAuthenticationRequestHandler authenticationRequestHandler =
                    mock(DefaultAuthenticationRequestHandler.class);

            DefaultRequestCoordinator defaultRequestCoordinator = new DefaultRequestCoordinator();

            // Mocking request parameters
            when(request.getParameter(FrameworkConstants.RequestParams.ISSUER)).thenReturn(relyingParty);
            when(request.getParameter(TENANT_DOMAIN)).thenReturn(tenantDomain);
            when(request.getAttribute(FrameworkConstants.RESTART_LOGIN_FLOW)).thenReturn(restartLoginFlow);
            when(request.getParameter(AUTHENTICATOR)).thenReturn("BasicAuthenticator");
            when(request.getParameter("promptResp")).thenReturn(TRUE);
            when(request.getParameter("promptId")).thenReturn("97d506f3-fd3c-4...");

            // Creating a new AuthenticationContext
            AuthenticationContext context = new AuthenticationContext();
            context.setTenantDomain(tenantDomain);
            context.setServiceProviderName("consoleApplication");
            context.setRequestType("oidc");
            context.setProperty(FrameworkConstants.INITIAL_CONTEXT, context.clone());
            context.setCurrentStep(1);
            context.setProperty(FrameworkConstants.JSAttributes.PROP_CURRENT_NODE, authGraphNode);

            frameworkUtils.when(() -> FrameworkUtils.sendToRetryPage(any(), any(), any()))
                    .thenThrow(new NullPointerException("Error occurred"));

            when(FrameworkUtils.getContextData(request)).thenAnswer(invocation -> context);
            when(FrameworkUtils.getAuthenticationRequestHandler()).thenReturn(authenticationRequestHandler);
            doNothing().when(authenticationRequestHandler).handle(request, response, context);

            // Mocking ApplicationManagementService behavior
            ApplicationManagementServiceImpl mockApplicationManagementService =
                    mock(ApplicationManagementServiceImpl.class);
            applicationManagementService.when(ApplicationManagementService::getInstance)
                    .thenReturn(mockApplicationManagementService);

            // Mocking ServiceProvider and its properties
            ServiceProvider serviceProvider = mock(ServiceProvider.class);
            when(serviceProvider.isApplicationEnabled()).thenReturn(true);
            when(mockApplicationManagementService.getServiceProviderByClientId(anyString(), anyString(), anyString()))
                    .thenReturn(serviceProvider);

            // Invoke handle method
            defaultRequestCoordinator.handle(request, response);
        } catch (NullPointerException e) {
            Assert.fail("NullPointerException occurred: " + e.getMessage());
        } catch (Exception e) {
            Assert.fail("Exception occurred: " + e.getMessage());
        }
    }

    @Test
    public void testFindPreviousAuthenticatedSession() throws FrameworkException {

        // Define constants for test values.
        final String testSessionId = "testId";
        final String testIssuer = "testIssuer";
        final String testTenantDomain = "carbon.super";
        final String testRequestType = "testRequestType";
        final String testAppName = "testApp";

        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getParameter(FrameworkConstants.RequestParams.SESSION_ID)).thenReturn(testSessionId);
        when(request.getParameter(FrameworkConstants.RequestParams.ISSUER)).thenReturn(testIssuer);

        AuthenticationContext authenticationContext = spy(AuthenticationContext.class);
        when(authenticationContext.getTenantDomain()).thenReturn(testTenantDomain);
        when(authenticationContext.getLoginTenantDomain()).thenReturn(testTenantDomain);
        when(authenticationContext.getRequestType()).thenReturn(testRequestType);

        try (MockedStatic<LoggerUtils> loggerUtilsMockedStatic = mockStatic(LoggerUtils.class);
             MockedStatic<ConfigurationFacade> configurationFacadeMockedStatic = mockStatic(ConfigurationFacade.class);
             MockedStatic<FrameworkUtils> frameworkUtilsMockedStatic = mockStatic(FrameworkUtils.class);
             MockedStatic<ApplicationManagementService> applicationManagementServiceMockedStatic = mockStatic(
                     ApplicationManagementService.class)) {

            // Mock LoggerUtils.
            loggerUtilsMockedStatic.when(LoggerUtils::isDiagnosticLogsEnabled).thenReturn(false);

            // Mock ConfigurationFacade.
            ConfigurationFacade configurationFacade = mock(ConfigurationFacade.class);
            SequenceConfig sequenceConfig = mock(SequenceConfig.class);
            when(configurationFacade.getSequenceConfig(null, testRequestType, testTenantDomain)).thenReturn(
                    sequenceConfig);
            configurationFacadeMockedStatic.when(ConfigurationFacade::getInstance).thenReturn(configurationFacade);

            // Mock FrameworkUtils.
            frameworkUtilsMockedStatic.when(() -> FrameworkUtils.isAPIBasedAuthenticationFlow(request))
                    .thenReturn(true);
            SessionContext sessionContext = mock(SessionContext.class);
            when(sessionContext.getProperty(FrameworkUtils.TENANT_DOMAIN)).thenReturn(testTenantDomain);
            frameworkUtilsMockedStatic.
                    when(() -> FrameworkUtils.getSessionContextFromCache(request, authenticationContext, testSessionId))
                    .thenReturn(sessionContext);

            // Mock ApplicationManagementService.
            ApplicationManagementService applicationManagementService = mock(ApplicationManagementService.class);
            applicationManagementServiceMockedStatic.when(ApplicationManagementService::getInstance)
                    .thenReturn(applicationManagementService);

            ServiceProvider serviceProvider = mock(ServiceProvider.class);
            when(applicationManagementService.getServiceProviderByClientId(anyString(), anyString(),
                    anyString())).thenReturn(serviceProvider);

            // Mock ApplicationConfig.
            ApplicationConfig applicationConfig = mock(ApplicationConfig.class);
            when(sequenceConfig.getApplicationConfig()).thenReturn(applicationConfig);
            when(applicationConfig.getApplicationName()).thenReturn(testAppName);

            // Mock authenticated sequences.
            Map<String, SequenceConfig> mockedMap = mock(Map.class);
            when(mockedMap.get(any())).thenReturn(sequenceConfig);
            when(sessionContext.getAuthenticatedSequences()).thenReturn(mockedMap);

            // Mock AuthenticatedUser.
            AuthenticatedUser authenticatedUser = mock(AuthenticatedUser.class);
            when(sequenceConfig.getAuthenticatedUser()).thenReturn(authenticatedUser);
            when(authenticatedUser.getTenantDomain()).thenReturn(testTenantDomain);

            // Case 1: Authenticated user has a tenant domain.
            requestCoordinator.findPreviousAuthenticatedSession(request, authenticationContext);

            assertEquals(authenticationContext.getSubject(), authenticatedUser);
            assertEquals(authenticationContext.getProperty(USER_TENANT_DOMAIN), testTenantDomain);

            // clear the previous tenant domain.
            authenticationContext.setProperty(USER_TENANT_DOMAIN, null);

            // Case2: Authenticated user return null tenant domain.
            when(authenticatedUser.getTenantDomain()).thenReturn(null);
            requestCoordinator.findPreviousAuthenticatedSession(request, authenticationContext);
            assertNull(authenticationContext.getProperty(USER_TENANT_DOMAIN));

            // Case 3: Authenticated user is null.
            // clear the previous subject.
            authenticationContext.setSubject(null);

            when(sequenceConfig.getAuthenticatedUser()).thenReturn(null);
            requestCoordinator.findPreviousAuthenticatedSession(request, authenticationContext);
            assertNull(authenticationContext.getSubject());

            // Case 4: Mismatch between the sp tenant domain and the user login tenant domain.
            when(authenticationContext.getLoginTenantDomain()).thenReturn("tenant123");
            requestCoordinator.findPreviousAuthenticatedSession(request, authenticationContext);
            verify(request).setAttribute(FrameworkConstants.REMOVE_COMMONAUTH_COOKIE, true);

        } catch (IdentityApplicationManagementException e) {
            throw new RuntimeException(e);
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


    private void addActorToIdentityContext() {

        UserActor userActor = new UserActor.Builder()
                .username("username")
                .build();
        IdentityContext.getThreadLocalIdentityContext().setActor(userActor);
    }

    /**
     * Test the addServiceProviderIdToRedirectUrl method with various scenarios.
     */
    @Test
    public void testAddServiceProviderIdToRedirectUrl() throws Exception {

        // Use reflection to access the private method (current signature: two params).
        Method method = DefaultRequestCoordinator.class.getDeclaredMethod(
                "addServiceProviderIdToRedirectUrl",
                CommonAuthResponseWrapper.class,
                AuthenticationContext.class
        );
        method.setAccessible(true);

        DefaultRequestCoordinator coordinator = new DefaultRequestCoordinator();

        // Case 1: Null responseWrapper. Should return early without exception.
        method.invoke(coordinator, null, mock(AuthenticationContext.class));

        // Case 2: Null context. Should return early and not call sendRedirect on responseWrapper.
        CommonAuthResponseWrapper responseWrapper = mock(CommonAuthResponseWrapper.class);
        method.invoke(coordinator, responseWrapper, null);
        verify(responseWrapper, times(0)).sendRedirect(anyString());

        // Case 3: Blank redirect URL -> method should detect blank and not call sendRedirect.
        responseWrapper = mock(CommonAuthResponseWrapper.class);
        AuthenticationContext context = mock(AuthenticationContext.class);
        when(responseWrapper.getRedirectURL()).thenReturn("");
        when(context.getServiceProviderResourceId()).thenReturn("sp-blank");
        method.invoke(coordinator, responseWrapper, context);
        verify(responseWrapper, times(0)).sendRedirect(anyString());

        // Case 4: Service provider ID is blank -> method should not call sendRedirect.
        responseWrapper = mock(CommonAuthResponseWrapper.class);
        context = mock(AuthenticationContext.class);
        when(responseWrapper.getRedirectURL()).thenReturn("https://example.com/redirect");
        when(context.getServiceProviderResourceId()).thenReturn(null);
        method.invoke(coordinator, responseWrapper, context);
        verify(responseWrapper, times(0)).sendRedirect(anyString());

        // Case 5: Service provider ID is present and appended to URL with existing query param.
        responseWrapper = mock(CommonAuthResponseWrapper.class);
        context = mock(AuthenticationContext.class);
        when(responseWrapper.getRedirectURL()).thenReturn("https://example.com/redirect?param=value");
        when(context.getServiceProviderResourceId()).thenReturn("sp-resource-id-123");
        when(context.getServiceProviderName()).thenReturn("TestSP");

        String expectedUrl = "https://example.com/redirect?param=value&" +
                FrameworkConstants.REQUEST_PARAM_SP_UUID + "=" + java.net.URLEncoder.encode(
                "sp-resource-id-123", java.nio.charset.StandardCharsets.UTF_8.name());

        method.invoke(coordinator, responseWrapper, context);

        verify(responseWrapper, times(1)).sendRedirect(expectedUrl);

        // Case 6: IOException thrown during sendRedirect. The method should catch and not propagate the exception.
        responseWrapper = mock(CommonAuthResponseWrapper.class);
        context = mock(AuthenticationContext.class);
        // Use a URL that contains a query string.
        when(responseWrapper.getRedirectURL()).thenReturn("https://example.com/redirect?existing=1");
        when(context.getServiceProviderResourceId()).thenReturn("sp-resource-id-456");
        when(context.getServiceProviderName()).thenReturn("TestSP2");

        String expectedUrlForIOException = "https://example.com/redirect?existing=1&" +
                FrameworkConstants.REQUEST_PARAM_SP_UUID + "=" + java.net.URLEncoder.encode(
                "sp-resource-id-456", java.nio.charset.StandardCharsets.UTF_8.name());

        doThrow(new IOException("Redirect failed")).when(responseWrapper).sendRedirect(anyString());

        // Should not throw exception; only logs debug message.
        method.invoke(coordinator, responseWrapper, context);

        verify(responseWrapper, times(1)).sendRedirect(expectedUrlForIOException);
    }
}
