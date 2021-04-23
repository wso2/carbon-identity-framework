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
package org.wso2.carbon.identity.application.authentication.framework.util;

import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.application.authentication.framework.handler.request.LogoutRequestHandler;
import org.wso2.carbon.identity.application.authentication.framework.handler.request.impl.DefaultLogoutRequestHandler;
import org.wso2.carbon.identity.application.authentication.framework.handler.sequence.RequestPathBasedSequenceHandler;
import org.wso2.carbon.identity.application.authentication.framework.handler.sequence.StepBasedSequenceHandler;
import org.wso2.carbon.identity.application.authentication.framework.handler.sequence.impl.DefaultRequestPathBasedSequenceHandler;
import org.wso2.carbon.identity.application.authentication.framework.handler.sequence.impl.GraphBasedSequenceHandler;
import org.wso2.carbon.identity.application.authentication.framework.handler.step.StepHandler;
import org.wso2.carbon.identity.application.authentication.framework.handler.step.impl.DefaultStepHandler;
import org.wso2.carbon.identity.application.authentication.framework.handler.step.impl.GraphBasedStepHandler;
import org.wso2.carbon.identity.common.testng.WithCarbonHome;
import org.wso2.carbon.core.SameSiteCookie;
import org.wso2.carbon.identity.application.authentication.framework.ApplicationAuthenticator;
import org.wso2.carbon.identity.application.authentication.framework.config.ConfigurationFacade;
import org.wso2.carbon.identity.application.authentication.framework.handler.request.AuthenticationRequestHandler;
import org.wso2.carbon.identity.application.authentication.framework.handler.request.RequestCoordinator;
import org.wso2.carbon.identity.application.authentication.framework.handler.request.impl.DefaultAuthenticationRequestHandler;
import org.wso2.carbon.identity.application.authentication.framework.handler.request.impl.DefaultRequestCoordinator;
import org.wso2.carbon.identity.application.authentication.framework.handler.request.impl.PostAuthnMissingClaimHandler;
import org.wso2.carbon.identity.application.authentication.framework.internal.FrameworkServiceComponent;
import org.wso2.carbon.identity.core.model.IdentityCookieConfig;
import org.wso2.carbon.identity.core.util.IdentityUtil;
import org.wso2.carbon.identity.testutil.powermock.PowerMockIdentityBaseTest;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import java.io.UnsupportedEncodingException;

import static org.mockito.Mockito.*;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.when;
import static org.testng.Assert.*;

@WithCarbonHome
@PrepareForTest({ConfigurationFacade.class, FrameworkServiceComponent.class, SameSiteCookie.class, IdentityUtil.class,
        GraphBasedStepHandler.class})
public class FrameworkUtilsTest extends PowerMockIdentityBaseTest {

    @Mock
    ConfigurationFacade mockedConfigurationFacade;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Captor
    ArgumentCaptor<Cookie> cookieCaptor;

    public static String ROOT_DOMAIN = "/";
    public static String TENANT_DOMAIN = "ABC";
    private PostAuthnMissingClaimHandler testPostAuthenticationHandler;
    private DefaultRequestCoordinator testRequestCoordinator;
    private DefaultAuthenticationRequestHandler testAuthenticationRequestHandler;
    private DefaultLogoutRequestHandler testLogoutRequestHandler;
    private StepBasedSequenceHandler testStepBasedSequenceHandler;
    private DefaultRequestPathBasedSequenceHandler testRequestPathBasedSequenceHandler;
    private DefaultStepHandler testStepHandler;
    private List<ApplicationAuthenticator> authenticators;

    @BeforeTest
    public void setUp() {
        testPostAuthenticationHandler = new PostAuthnMissingClaimHandler();
        testRequestCoordinator = new DefaultRequestCoordinator();
        testAuthenticationRequestHandler = new DefaultAuthenticationRequestHandler();
        testLogoutRequestHandler = new DefaultLogoutRequestHandler();
        testStepBasedSequenceHandler = new GraphBasedSequenceHandler();
        testStepHandler = new DefaultStepHandler();
        testRequestPathBasedSequenceHandler = new DefaultRequestPathBasedSequenceHandler();
    }

    private void setMockedConfigurationFacade() {
        mockStatic(ConfigurationFacade.class);
        when(ConfigurationFacade.getInstance()).thenReturn(mockedConfigurationFacade);
    }

    private void mockFrameworkServiceComponent() {
        mockStatic(FrameworkServiceComponent.class);
        when(FrameworkServiceComponent.getAuthenticators()).thenReturn(authenticators);
    }

    private ApplicationAuthenticator initAuthenticators(String name) {
        ApplicationAuthenticator applicationAuthenticator = mock(ApplicationAuthenticator.class);
        when(applicationAuthenticator.getName()).thenReturn(name);

        return applicationAuthenticator;
    }

    @Test
    public void testGetAppAuthenticatorByNameValidAuthenticator() {
        authenticators = new ArrayList<>();
        String name = "Authenticator1";
        ApplicationAuthenticator authenticator1 = initAuthenticators(name);
        authenticators.add(authenticator1);

        mockFrameworkServiceComponent();

        ApplicationAuthenticator out = FrameworkUtils.getAppAuthenticatorByName(name);
        assertEquals(out, authenticator1);
    }

    @Test
    public void testGetAppAuthenticatorByNameNonExistAuthenticator() {
        ApplicationAuthenticator out = FrameworkUtils.getAppAuthenticatorByName("nonExistingAuthenticator");
        assertNull(out);
    }

    @DataProvider(name = "provideRequestCoordinators")
    public Object[][] provideRequestCoordinators() {

        Map<String, Object> map1 = new HashMap<>();
        map1.put(FrameworkConstants.Config.QNAME_EXT_REQ_COORDINATOR, testRequestCoordinator);
        return new Object[][]{
                {map1, testRequestCoordinator}
        };
    }

    @Test(dataProvider = "provideRequestCoordinators")
    public void testGetStepBasedSequenceHandlerExistReqCoordinator(Map<String, Object> instance, Object expectedOutput) {
        setMockedConfigurationFacade();
        when(ConfigurationFacade.getInstance().getExtensions()).thenReturn(instance);
        RequestCoordinator out = FrameworkUtils.getRequestCoordinator();
        assertEquals(out, expectedOutput);
    }

    @Test
    public void testGetStepBasedSequenceHandlerNonExistReqCoordinator() {
        RequestCoordinator out = FrameworkUtils.getRequestCoordinator();
        assertEquals(out, DefaultRequestCoordinator.getInstance());
    }

    @DataProvider(name = "provideAuthReqHandler")
    public Object[][] provideAuthReqHandler() {

        Map<String, Object> map1 = new HashMap<>();
        map1.put(FrameworkConstants.Config.QNAME_EXT_AUTH_REQ_HANDLER, testAuthenticationRequestHandler);

        return new Object[][]{
                {map1, testAuthenticationRequestHandler}
        };
    }

    @Test(dataProvider = "provideAuthReqHandler")
    public void testGetAuthenticationRequestHandlerExistHandler(Map<String, Object> instance, Object expectedOutput) {
        setMockedConfigurationFacade();
        when(ConfigurationFacade.getInstance().getExtensions()).thenReturn(instance);
        AuthenticationRequestHandler out = FrameworkUtils.getAuthenticationRequestHandler();
        assertEquals(out, expectedOutput);
    }

    @Test
    public void testGetAuthenticationRequestHandlerNonExistHandler() {
        AuthenticationRequestHandler out = FrameworkUtils.getAuthenticationRequestHandler();
        assertEquals(out, DefaultAuthenticationRequestHandler.getInstance());
    }

    @DataProvider(name = "provideLogoutRequestHandler")
    public Object[][] provideLogoutRequestHandler() {

        Map<String, Object> map1 = new HashMap<>();
        map1.put(FrameworkConstants.Config.QNAME_EXT_LOGOUT_REQ_HANDLER, testLogoutRequestHandler);

        return new Object[][]{
                {map1, testLogoutRequestHandler}
        };
    }

    @Test(dataProvider = "provideLogoutRequestHandler")
    public void testGetLogoutRequestHandlerExistHandler(Map<String, Object> instance, Object expectedOutput) {
        setMockedConfigurationFacade();
        when(ConfigurationFacade.getInstance().getExtensions()).thenReturn(instance);
        LogoutRequestHandler out = FrameworkUtils.getLogoutRequestHandler();
        assertEquals(out, expectedOutput);
    }

    @Test
    public void testGetLogoutRequestHandlerNonExistHandler() {
        LogoutRequestHandler out = FrameworkUtils.getLogoutRequestHandler();
        assertEquals(out, DefaultLogoutRequestHandler.getInstance());
    }

    @DataProvider(name = "provideStepBasedSequenceHandler")
    public Object[][] provideStepBasedSequenceHandler() {

        Map<String, Object> map1 = new HashMap<>();
        map1.put(FrameworkConstants.Config.QNAME_EXT_STEP_BASED_SEQ_HANDLER, testStepBasedSequenceHandler);

        return new Object[][]{
                {map1, testStepBasedSequenceHandler}
        };
    }

    @Test(dataProvider = "provideStepBasedSequenceHandler")
    public void testGetStepBasedSequenceHandlerExistHandler(Map<String, Object> instance, Object expectedOutput) {
        setMockedConfigurationFacade();
        when(ConfigurationFacade.getInstance().getExtensions()).thenReturn(instance);
        StepBasedSequenceHandler out = FrameworkUtils.getStepBasedSequenceHandler();
        assertEquals(out, expectedOutput);
    }

    @Test
    public void testGetStepBasedSequenceHandlerNonExistHandler() {
        FrameworkUtils.getStepBasedSequenceHandler();
        verify(GraphBasedSequenceHandler.class, times(1));
    }

    @DataProvider(name = "provideRequestPathBasedSequenceHandler")
    public Object[][] provideRequestPathBasedSequenceHandler() {

        Map<String, Object> map1 = new HashMap<>();
        map1.put(FrameworkConstants.Config.QNAME_EXT_REQ_PATH_BASED_SEQ_HANDLER, testRequestPathBasedSequenceHandler);

        return new Object[][]{
                {map1, testRequestPathBasedSequenceHandler}
        };
    }

    @Test(dataProvider = "provideRequestPathBasedSequenceHandler")
    public void testGetRequestPathBasedSequenceHandlerExistHandler(Map<String, Object> instance, Object expectedOutput) {
        setMockedConfigurationFacade();
        when(ConfigurationFacade.getInstance().getExtensions()).thenReturn(instance);
        RequestPathBasedSequenceHandler out = FrameworkUtils.getRequestPathBasedSequenceHandler();
        assertEquals(out, expectedOutput);
    }

    @Test
    public void testGetRequestPathBasedSequenceHandlerNonExistHandler() {
        RequestPathBasedSequenceHandler out = FrameworkUtils.getRequestPathBasedSequenceHandler();
        assertEquals(out, DefaultRequestPathBasedSequenceHandler.getInstance());
    }

    @DataProvider(name = "provideStepHandler")
    public Object[][] provideStepHandler() {

        Map<String, Object> map1 = new HashMap<>();
        map1.put(FrameworkConstants.Config.QNAME_EXT_STEP_HANDLER, testStepHandler);

        return new Object[][]{
                {map1, testStepHandler}
        };
    }

    @Test(dataProvider = "provideStepHandler")
    public void testGetStepHandlerExistHandler(Map<String, Object> instance, Object expectedOutput) {
        setMockedConfigurationFacade();
        when(ConfigurationFacade.getInstance().getExtensions()).thenReturn(instance);
        StepHandler out = FrameworkUtils.getStepHandler();
        assertEquals(out, expectedOutput);
    }

    @Test
    public void testGetStepHandlerNonExistHandler() {
        FrameworkUtils.getStepHandler();
        verify(GraphBasedStepHandler.class, times(1));
    }

    @DataProvider(name = "providePostAuthenticationData")
    public Object[][] provideInvalidData() {

        Map<String, Object> map1 = new HashMap<>();
        map1.put(FrameworkConstants.Config.QNAME_EXT_POST_AUTHENTICATION_HANDLER, testPostAuthenticationHandler);

        Map<String, Object> map2 = new HashMap<>();
        map2.put(FrameworkConstants.Config.QNAME_EXT_POST_AUTHENTICATION_HANDLER, new Object());

        return new Object[][]{
                {map1, true},
                {map2, false}
        };
    }

    @Test(dataProvider = "provideURLParamData")
    public void testAppendQueryParamsToUrl(String url, Map<String, String> queryParamMap, String expectedOutput)
            throws Exception {

        String out = FrameworkUtils.appendQueryParamsToUrl(url, queryParamMap);
        assertEquals(out, expectedOutput);
    }

    @Test(expectedExceptions = IllegalArgumentException.class,
            dataProvider = "provideQueryParamData")
    public void testAppendQueryParamsToUrlEmptyUrl(Map<String, String> queryParamMap)
            throws Exception {
        FrameworkUtils.appendQueryParamsToUrl(null, queryParamMap);
    }

    @Test(expectedExceptions = IllegalArgumentException.class,
            dataProvider = "provideQueryParamData")
    public void testBuildURLWithQueryParamsEmptyUrl(Map<String, String> queryParamMap)
            throws Exception {
        FrameworkUtils.appendQueryParamsToUrl(null, queryParamMap);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testAppendQueryParamsToUrlEmptyQueryParams() throws Exception {
        FrameworkUtils.appendQueryParamsToUrl("https://www.example.com", null);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testBuildURLWithQueryParamsEmptyQueryParams() throws Exception {
        FrameworkUtils.appendQueryParamsToUrl("https://www.example.com", null);
    }

    @Test(dataProvider = "provideURLParamData")
    public void testBuildURLWithQueryParams(String url, Map<String, String> queryParamMap, String expectedOutput)
            throws UnsupportedEncodingException {

        String out = FrameworkUtils.buildURLWithQueryParams(url, queryParamMap);
        assertEquals(out, expectedOutput);
    }

    @DataProvider(name = "provideURLParamData")
    public Object[][] provideURLParamData() {

        String url1 = "https://www.example.com";
        String url2 = "https://www.example.com?x=asd";

        Map<String, String> queryParamMap1 = new HashMap<>();
        queryParamMap1.put("a", "wer");
        queryParamMap1.put("b", "dfg");
        String queryParamString = "a=wer&b=dfg";

        Map<String, String> queryParamMap2 = new HashMap<>();
        queryParamMap2.put("a", "http://wso2.com");

        Map<String, String> queryParamMap3 = new HashMap<>();

        String expectedOutput1 = url1 + "?" + queryParamString;
        String expectedOutput2 = url2 + "&" + queryParamString;
        String expectedOutput3 = url1 + "?a=http%3A%2F%2Fwso2.com";

        return new Object[][]{
                {url1, queryParamMap1, expectedOutput1},
                {url2, queryParamMap1, expectedOutput2},
                {url1, queryParamMap2, expectedOutput3},
                {url1, queryParamMap3, url1},
                {url2, queryParamMap3, url2}
        };
    }

    @DataProvider(name = "provideQueryParamData")
    public Object[][] provideQueryParamData() {

        Map<String, String> queryParamMap1 = new HashMap<>();
        queryParamMap1.put("a", "wer");
        queryParamMap1.put("b", "dfg");

        return new Object[][]{
                {queryParamMap1}
        };
    }

    private Cookie[] getAuthenticationCookies() {
        Cookie[] cookies = new Cookie[2];
        cookies[0] = new Cookie(FrameworkConstants.COMMONAUTH_COOKIE, "commonAuthIdValue");
        cookies[1] = new Cookie(FrameworkConstants.COMMONAUTH_COOKIE, "commonAuthIdValue");
        cookies[1].setPath(FrameworkConstants.TENANT_CONTEXT_PREFIX + TENANT_DOMAIN + "/");
        return cookies;
    }

    private void mockCookieTest() {
        mockStatic(SameSiteCookie.class);
        Cookie[] cookies = getAuthenticationCookies();
        when(request.getCookies()).thenReturn(cookies);
    }

    @Test
    public void testRemoveAuthCookie() {
        mockCookieTest();

        FrameworkUtils.removeAuthCookie(request, response);
        verify(response, times(1)).addCookie(cookieCaptor.capture());
        List<Cookie> capturedCookies = cookieCaptor.getAllValues();
        Cookie removedOldSessionNonce = capturedCookies.get(0);

        assertEquals(removedOldSessionNonce.getName(), FrameworkConstants.COMMONAUTH_COOKIE);
    }

    @Test
    public void testRemoveAuthCookieInTenant() {
        mockCookieTest();
        FrameworkUtils.removeAuthCookie(request, response, TENANT_DOMAIN);
        verify(response, times(1)).addCookie(cookieCaptor.capture());
        List<Cookie> capturedCookies = cookieCaptor.getAllValues();

        Cookie removedCookie = capturedCookies.get(0);
        assertEquals(removedCookie.getName(), FrameworkConstants.COMMONAUTH_COOKIE);
        assertEquals(removedCookie.getPath(), FrameworkConstants.TENANT_CONTEXT_PREFIX + TENANT_DOMAIN + "/");
    }

    @Test
    public void testRemoveCookieNonExistCookieConfig() {
        mockCookieTest();
        FrameworkUtils.removeCookie(request, response, FrameworkConstants.COMMONAUTH_COOKIE, SameSiteCookie.STRICT, ROOT_DOMAIN);

        verify(response, times(1)).addCookie(cookieCaptor.capture());
        List<Cookie> capturedCookies = cookieCaptor.getAllValues();
        Cookie removedCookie = capturedCookies.get(0);

        assertEquals(removedCookie.getName(), FrameworkConstants.COMMONAUTH_COOKIE);
        assertEquals(removedCookie.getPath(), ROOT_DOMAIN);
        assertEquals(removedCookie.getMaxAge(), 0);
    }

    @Test
    public void testRemoveCookieExistCookieConfig() {
        mockCookieTest();

        IdentityCookieConfig cookieConfig = new IdentityCookieConfig(FrameworkConstants.COMMONAUTH_COOKIE);
        mockStatic(IdentityUtil.class);
        when(IdentityUtil.getIdentityCookieConfig(FrameworkConstants.COMMONAUTH_COOKIE)).thenReturn(cookieConfig);

        FrameworkUtils.removeCookie(request, response, FrameworkConstants.COMMONAUTH_COOKIE,
                SameSiteCookie.STRICT, ROOT_DOMAIN);

        verify(response, times(1)).addCookie(cookieCaptor.capture());
        List<Cookie> capturedCookies = cookieCaptor.getAllValues();
        Cookie removedCookie = capturedCookies.get(0);

        assertEquals(removedCookie.getName(), FrameworkConstants.COMMONAUTH_COOKIE);
        assertEquals(removedCookie.getPath(), ROOT_DOMAIN);
        assertEquals(removedCookie.getMaxAge(), 0);

    }

    @Test
    public void testRemoveCookieNonExistCookie() {
        mockCookieTest();

        FrameworkUtils.removeCookie(request, response, "NonExistingCookie",
                SameSiteCookie.STRICT, ROOT_DOMAIN);

        verify(response, never()).addCookie(anyObject());
    }

    @Test
    public void testSetCookieNonExistCookieConfig() {
        mockCookieTest();
        int age = 3600;

        FrameworkUtils.setCookie(request, response, FrameworkConstants.COMMONAUTH_COOKIE, "commonAuthIdValue", age);

        verify(response, times(1)).addCookie(cookieCaptor.capture());
        List<Cookie> capturedCookies = cookieCaptor.getAllValues();
        Cookie storedCookie = capturedCookies.get(0);

        assertEquals(storedCookie.getName(), FrameworkConstants.COMMONAUTH_COOKIE);
        assertEquals(storedCookie.getPath(), ROOT_DOMAIN);
        assertEquals(storedCookie.getMaxAge(), age);

    }

    @Test
    public void testSetCookieExistCookieConfig() {
        mockCookieTest();

        IdentityCookieConfig cookieConfig = new IdentityCookieConfig(FrameworkConstants.COMMONAUTH_COOKIE);
        mockStatic(IdentityUtil.class);
        when(IdentityUtil.getIdentityCookieConfig(FrameworkConstants.COMMONAUTH_COOKIE)).thenReturn(cookieConfig);
        int age = 3600;

        FrameworkUtils.setCookie(request, response, FrameworkConstants.COMMONAUTH_COOKIE, "commonAuthIdValue", age);

        verify(response, times(1)).addCookie(cookieCaptor.capture());
        List<Cookie> capturedCookies = cookieCaptor.getAllValues();
        Cookie storedCookie = capturedCookies.get(0);

        assertEquals(storedCookie.getName(), FrameworkConstants.COMMONAUTH_COOKIE);
        assertEquals(storedCookie.getPath(), ROOT_DOMAIN);
        assertEquals(storedCookie.getMaxAge(), age);
    }

}
