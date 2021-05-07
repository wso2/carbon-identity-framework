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

import org.apache.commons.codec.digest.DigestUtils;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.wso2.carbon.core.SameSiteCookie;
import org.wso2.carbon.identity.application.authentication.framework.ApplicationAuthenticator;
import org.wso2.carbon.identity.application.authentication.framework.MockAuthenticator;
import org.wso2.carbon.identity.application.authentication.framework.cache.AuthenticationContextCache;
import org.wso2.carbon.identity.application.authentication.framework.cache.AuthenticationContextCacheEntry;
import org.wso2.carbon.identity.application.authentication.framework.cache.AuthenticationContextCacheKey;
import org.wso2.carbon.identity.application.authentication.framework.cache.AuthenticationResultCache;
import org.wso2.carbon.identity.application.authentication.framework.cache.AuthenticationResultCacheEntry;
import org.wso2.carbon.identity.application.authentication.framework.cache.AuthenticationResultCacheKey;
import org.wso2.carbon.identity.application.authentication.framework.cache.SessionContextCache;
import org.wso2.carbon.identity.application.authentication.framework.cache.SessionContextCacheEntry;
import org.wso2.carbon.identity.application.authentication.framework.cache.SessionContextCacheKey;
import org.wso2.carbon.identity.application.authentication.framework.config.ConfigurationFacade;
import org.wso2.carbon.identity.application.authentication.framework.context.AuthenticationContext;
import org.wso2.carbon.identity.application.authentication.framework.context.SessionContext;
import org.wso2.carbon.identity.application.authentication.framework.exception.FrameworkException;
import org.wso2.carbon.identity.application.authentication.framework.handler.claims.ClaimHandler;
import org.wso2.carbon.identity.application.authentication.framework.handler.claims.impl.DefaultClaimHandler;
import org.wso2.carbon.identity.application.authentication.framework.handler.hrd.HomeRealmDiscoverer;
import org.wso2.carbon.identity.application.authentication.framework.handler.hrd.impl.DefaultHomeRealmDiscoverer;
import org.wso2.carbon.identity.application.authentication.framework.handler.provisioning.ProvisioningHandler;
import org.wso2.carbon.identity.application.authentication.framework.handler.provisioning.impl.DefaultProvisioningHandler;
import org.wso2.carbon.identity.application.authentication.framework.handler.request.impl.DefaultAuthenticationRequestHandler;
import org.wso2.carbon.identity.application.authentication.framework.handler.request.impl.DefaultLogoutRequestHandler;
import org.wso2.carbon.identity.application.authentication.framework.handler.request.impl.DefaultRequestCoordinator;
import org.wso2.carbon.identity.application.authentication.framework.handler.sequence.RequestPathBasedSequenceHandler;
import org.wso2.carbon.identity.application.authentication.framework.handler.sequence.impl.DefaultRequestPathBasedSequenceHandler;
import org.wso2.carbon.identity.application.authentication.framework.handler.sequence.impl.DefaultStepBasedSequenceHandler;
import org.wso2.carbon.identity.application.authentication.framework.handler.sequence.impl.GraphBasedSequenceHandler;
import org.wso2.carbon.identity.application.authentication.framework.handler.step.StepHandler;
import org.wso2.carbon.identity.application.authentication.framework.handler.step.impl.DefaultStepHandler;
import org.wso2.carbon.identity.application.authentication.framework.handler.step.impl.GraphBasedStepHandler;
import org.wso2.carbon.identity.application.authentication.framework.internal.FrameworkServiceComponent;
import org.wso2.carbon.identity.application.authentication.framework.internal.FrameworkServiceDataHolder;
import org.wso2.carbon.identity.application.authentication.framework.model.AuthenticationResult;
import org.wso2.carbon.identity.common.testng.WithCarbonHome;
import org.wso2.carbon.identity.core.model.IdentityCookieConfig;
import org.wso2.carbon.identity.core.util.IdentityConfigParser;
import org.wso2.carbon.identity.core.util.IdentityCoreConstants;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;
import org.wso2.carbon.identity.core.util.IdentityUtil;
import org.wso2.carbon.identity.event.services.IdentityEventService;
import org.wso2.carbon.identity.event.services.IdentityEventServiceImpl;
import org.wso2.carbon.identity.testutil.powermock.PowerMockIdentityBaseTest;

import java.io.UnsupportedEncodingException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.anyObject;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;
import static org.wso2.carbon.identity.application.authentication.framework.util.FrameworkConstants.REQUEST_PARAM_SP;
import static org.wso2.carbon.identity.application.authentication.framework.util.FrameworkUtils.TENANT_DOMAIN;

@WithCarbonHome
@PrepareForTest({SameSiteCookie.class, SessionContextCache.class, AuthenticationResultCache.class, AuthenticationContextCache.class, IdentityTenantUtil.class})
@PowerMockIgnore({"javax.net.*", "javax.security.*", "javax.crypto.*", "javax.xml.*"})
public class FrameworkUtilsTest extends PowerMockIdentityBaseTest {

    final public static String ROOT_DOMAIN = "/";
    final public static String DUMMY_TENANT_DOMAIN = "ABC";
    final public static String DUMMY_SP_NAME = "wso2carbon-local-sp";
    final public static String REDIRECT_URL = "custom-page?";
    final public static String DUMMY_CACHE_KEY = "cache-key";

    SessionContextCacheKey cacheKey = new SessionContextCacheKey(DUMMY_CACHE_KEY);
    SessionContextCacheEntry cacheEntry = new SessionContextCacheEntry();
    SessionContext context = new SessionContext();
    AuthenticationContext authenticationContext = new AuthenticationContext();
    AuthenticationResultCacheKey authenticationCacheKey = new AuthenticationResultCacheKey(DUMMY_CACHE_KEY);
    AuthenticationResultCacheEntry authenticationCacheEntry = new AuthenticationResultCacheEntry();
    AuthenticationResult authenticationResult = new AuthenticationResult();

    @Mock
    SessionContextCache mockedSessionContextCache;

    @Mock
    AuthenticationResultCache mockedAuthenticationResultCache;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    AuthenticationContextCache mockedAuthenticationContextCache;

    @Captor
    ArgumentCaptor<Cookie> cookieCaptor;

    @BeforeTest
    public void setFrameworkServiceComponent() {

        FrameworkServiceComponent.getAuthenticators().clear();
        FrameworkServiceComponent.getAuthenticators().add(new MockAuthenticator("BasicAuthenticator"));
        FrameworkServiceComponent.getAuthenticators().add(new MockAuthenticator("HwkMockAuthenticator"));
    }

    @Test
    public void testGetAppAuthenticatorByNameExistingAuthenticator() {

        ApplicationAuthenticator out = FrameworkUtils.getAppAuthenticatorByName("BasicAuthenticator");
        assert out != null;
        assertEquals(out.getName(), "BasicAuthenticator");
    }

    @Test
    public void testGetAppAuthenticatorByNameNonExistingAuthenticator() {

        ApplicationAuthenticator out = FrameworkUtils.getAppAuthenticatorByName("NonExistingAuthenticator");
        assertNull(out);
    }

    @Test
    public void testGetRequestCoordinatorExistingHandler() {

        DefaultRequestCoordinator testRequestCoordinator= new DefaultRequestCoordinator();
        ConfigurationFacade.getInstance().getExtensions().put(FrameworkConstants.Config.QNAME_EXT_REQ_COORDINATOR, testRequestCoordinator);
        Object out = FrameworkUtils.getRequestCoordinator();
        assertEquals(out, testRequestCoordinator);
    }

    @Test
    public void testGetRequestCoordinatorNonExistingReqCoordinator() {

        ConfigurationFacade.getInstance().getExtensions().remove(FrameworkConstants.Config.QNAME_EXT_REQ_COORDINATOR);
        Object out = FrameworkUtils.getRequestCoordinator();
        assertEquals(out.getClass(), DefaultRequestCoordinator.class);
    }

    @Test
    public void testGetAuthenticationRequestHandlerExistingHandler() {

        DefaultAuthenticationRequestHandler testAuthenticationRequestHandler = new DefaultAuthenticationRequestHandler();
        ConfigurationFacade.getInstance().getExtensions().put(FrameworkConstants.Config.QNAME_EXT_AUTH_REQ_HANDLER, testAuthenticationRequestHandler);
        Object out = FrameworkUtils.getAuthenticationRequestHandler();
        assertEquals(out, testAuthenticationRequestHandler);
    }

    @Test
    public void testGetAuthenticationRequestHandlerNonExistingReqCoordinator() {

        ConfigurationFacade.getInstance().getExtensions().remove(FrameworkConstants.Config.QNAME_EXT_AUTH_REQ_HANDLER);
        Object out = FrameworkUtils.getAuthenticationRequestHandler();
        assertEquals(out.getClass(), DefaultAuthenticationRequestHandler.class);
    }

    @Test
    public void testGetLogoutRequestHandlerExistingHandler() {

        DefaultLogoutRequestHandler testLogoutRequestHandler = new DefaultLogoutRequestHandler();
        ConfigurationFacade.getInstance().getExtensions().put(FrameworkConstants.Config.QNAME_EXT_LOGOUT_REQ_HANDLER, testLogoutRequestHandler);
        Object out = FrameworkUtils.getLogoutRequestHandler();
        assertEquals(out, testLogoutRequestHandler);
    }

    @Test
    public void testGetLogoutRequestHandlerNonExistingReqCoordinator() {

        ConfigurationFacade.getInstance().getExtensions().remove(FrameworkConstants.Config.QNAME_EXT_LOGOUT_REQ_HANDLER);
        Object out = FrameworkUtils.getLogoutRequestHandler();
        assertEquals(out.getClass(), DefaultLogoutRequestHandler.class);
    }

    @Test
    public void testGetStepBasedSequenceHandlerExistingHandler() {

        DefaultStepBasedSequenceHandler testStepBasedSequenceHandler = new DefaultStepBasedSequenceHandler();
        ConfigurationFacade.getInstance().getExtensions().put(FrameworkConstants.Config.QNAME_EXT_STEP_BASED_SEQ_HANDLER, testStepBasedSequenceHandler);
        Object out = FrameworkUtils.getStepBasedSequenceHandler();
        assertEquals(out, testStepBasedSequenceHandler);
    }

    @Test
    public void testGetStepBasedSequenceHandlerNonExistingReqCoordinator() {

        ConfigurationFacade.getInstance().getExtensions().remove(FrameworkConstants.Config.QNAME_EXT_STEP_BASED_SEQ_HANDLER);
        Object out = FrameworkUtils.getStepBasedSequenceHandler();
        assertEquals(out.getClass(), GraphBasedSequenceHandler.class);
    }

    @Test
    public void testGetRequestPathBasedSequenceHandlerExistingHandler() {

        DefaultRequestPathBasedSequenceHandler testRequestPathBasedSequenceHandler = new DefaultRequestPathBasedSequenceHandler();
        ConfigurationFacade.getInstance().getExtensions().put(FrameworkConstants.Config.QNAME_EXT_REQ_PATH_BASED_SEQ_HANDLER, testRequestPathBasedSequenceHandler);
        RequestPathBasedSequenceHandler out = FrameworkUtils.getRequestPathBasedSequenceHandler();
        assertEquals(out, testRequestPathBasedSequenceHandler);
    }

    @Test
    public void testGetRequestPathBasedSequenceHandlerNonExistingHandler() {

        ConfigurationFacade.getInstance().getExtensions().remove(FrameworkConstants.Config.QNAME_EXT_REQ_PATH_BASED_SEQ_HANDLER);
        Object out = FrameworkUtils.getRequestPathBasedSequenceHandler();
        assertEquals(out.getClass(), DefaultRequestPathBasedSequenceHandler.class);
    }

    @Test
    public void testGetStepHandlerExistHandler() {

        DefaultStepHandler testStepHandler = new DefaultStepHandler();
        ConfigurationFacade.getInstance().getExtensions().put(FrameworkConstants.Config.QNAME_EXT_STEP_HANDLER, testStepHandler);
        StepHandler out = FrameworkUtils.getStepHandler();
        assertEquals(out, testStepHandler);
    }

    @Test
    public void testGetStepHandlerNonExistHandler() {

        ConfigurationFacade.getInstance().getExtensions().remove(FrameworkConstants.Config.QNAME_EXT_STEP_HANDLER);
        FrameworkUtils.getStepHandler();
        verify(GraphBasedStepHandler.class, times(1));
    }

    @Test
    public void testGetHomeRealmDiscovererExistingDiscoverer() {

        DefaultHomeRealmDiscoverer textHomeRealmDiscoverer = new DefaultHomeRealmDiscoverer();
        ConfigurationFacade.getInstance().getExtensions().put(FrameworkConstants.Config.QNAME_EXT_HRD, textHomeRealmDiscoverer);
        HomeRealmDiscoverer out = FrameworkUtils.getHomeRealmDiscoverer();
        assertEquals(out, textHomeRealmDiscoverer);
    }

    @Test
    public void testGetHomeRealmDiscovererNonExistDiscoverer() {

        ConfigurationFacade.getInstance().getExtensions().remove(FrameworkConstants.Config.QNAME_EXT_HRD);
        Object out = FrameworkUtils.getHomeRealmDiscoverer();
        assertEquals(out.getClass(), DefaultHomeRealmDiscoverer.class);
    }

    @Test
    public void testGetClaimHandlerExistHandler() {

        DefaultClaimHandler testClaimHandler = new DefaultClaimHandler();
        ConfigurationFacade.getInstance().getExtensions().put(FrameworkConstants.Config.QNAME_EXT_CLAIM_HANDLER, testClaimHandler);
        ClaimHandler out = FrameworkUtils.getClaimHandler();
        assertEquals(out, testClaimHandler);
    }

    @Test
    public void testGetClaimHandlerNonExistHandler() {

        ConfigurationFacade.getInstance().getExtensions().remove(FrameworkConstants.Config.QNAME_EXT_CLAIM_HANDLER);
        Object out = FrameworkUtils.getClaimHandler();
        assertEquals(out.getClass(), DefaultClaimHandler.class);
    }

    @Test
    public void testGetProvisioningHandlerExistHandler() {

        DefaultProvisioningHandler testProvisioningHandler = new DefaultProvisioningHandler();
        ConfigurationFacade.getInstance().getExtensions().put(FrameworkConstants.Config.QNAME_EXT_PROVISIONING_HANDLER, testProvisioningHandler);
        ProvisioningHandler out = FrameworkUtils.getProvisioningHandler();
        assertEquals(out, testProvisioningHandler);
    }

    @Test
    public void testGetProvisioningHandlerNonExistHandler() {

        ConfigurationFacade.getInstance().getExtensions().remove(FrameworkConstants.Config.QNAME_EXT_PROVISIONING_HANDLER);
        Object out = FrameworkUtils.getProvisioningHandler();
        assertEquals(out.getClass(), DefaultProvisioningHandler.class);
    }

    @Test
    public void getAddAuthenticationContextToCache() {

        mockStatic(AuthenticationContextCache.class);
        when(AuthenticationContextCache.getInstance()).thenReturn(mockedAuthenticationContextCache);
        String contextId = "CONTEXT-ID";

        FrameworkUtils.addAuthenticationContextToCache(contextId, authenticationContext);

        ArgumentCaptor<AuthenticationContextCacheKey> captorKey = ArgumentCaptor.forClass(AuthenticationContextCacheKey.class);
        ArgumentCaptor<AuthenticationContextCacheEntry> captorEntry = ArgumentCaptor.forClass(AuthenticationContextCacheEntry.class);
        verify(mockedAuthenticationContextCache).addToCache(captorKey.capture(), captorEntry.capture());
        assertEquals(captorKey.getValue().getContextId(), contextId);
        assertEquals(captorEntry.getValue().getContext(), authenticationContext);
        assertTrue(captorEntry.getValue().getValidityPeriod() > 0);
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

    @Test(dataProvider = "provideURLParamData")
    public void testAppendQueryParamsToUrl(String url, Map<String, String> queryParamMap, String expectedOutput)
            throws Exception {

        String out = FrameworkUtils.appendQueryParamsToUrl(url, queryParamMap);
        assertEquals(out, expectedOutput);
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

    @Test(dataProvider = "provideURLParamData")
    public void testBuildURLWithQueryParams(String url, Map<String, String> queryParamMap, String expectedOutput)
            throws UnsupportedEncodingException {

        String out = FrameworkUtils.buildURLWithQueryParams(url, queryParamMap);
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

    private void setMockedAuthenticationResultCache() {

        mockStatic(AuthenticationResultCache.class);
        when(AuthenticationResultCache.getInstance()).thenReturn(mockedAuthenticationResultCache);
        when(mockedAuthenticationResultCache.getValueFromCache(authenticationCacheKey)).thenReturn(authenticationCacheEntry);
    }

    @Test
    public void getAddAuthenticationResultToCache() {

        setMockedAuthenticationResultCache();

        FrameworkUtils.addAuthenticationResultToCache(DUMMY_CACHE_KEY, authenticationResult);

        ArgumentCaptor<AuthenticationResultCacheKey> captorKey = ArgumentCaptor.forClass(AuthenticationResultCacheKey.class);
        ArgumentCaptor<AuthenticationResultCacheEntry> captorEntry = ArgumentCaptor.forClass(AuthenticationResultCacheEntry.class);
        verify(mockedAuthenticationResultCache).addToCache(captorKey.capture(), captorEntry.capture());
        assertEquals(captorKey.getValue().getResultId(), DUMMY_CACHE_KEY);
        assertEquals(captorEntry.getValue().getResult(), authenticationResult);
        assertTrue(captorEntry.getValue().getValidityPeriod() > 0);
    }

    @Test
    public void testGetAuthenticationResultFromCache() {

        setMockedAuthenticationResultCache();
        AuthenticationResultCacheEntry out = FrameworkUtils.getAuthenticationResultFromCache(DUMMY_CACHE_KEY);
        assertEquals(out, authenticationCacheEntry);
    }

    @Test
    public void testRemoveAuthenticationResultFromCache() {

        setMockedAuthenticationResultCache();
        FrameworkUtils.removeAuthenticationResultFromCache(DUMMY_CACHE_KEY);

        ArgumentCaptor<AuthenticationResultCacheKey> captor = ArgumentCaptor.forClass(AuthenticationResultCacheKey.class);
        verify(mockedAuthenticationResultCache).clearCacheEntry(captor.capture());
        assertEquals(captor.getValue().getResultId(), DUMMY_CACHE_KEY);
    }

    @DataProvider(name = "provideRequestAttributes")
    public Object[][] provideRequestAttributes() {

        String expectedOut1 = REDIRECT_URL + "&sp=" + DUMMY_SP_NAME + "&tenantDomain=" + DUMMY_TENANT_DOMAIN;
        String expectedOut2 = REDIRECT_URL + "&sp=" + DUMMY_SP_NAME;
        String expectedOut3 = REDIRECT_URL + "&tenantDomain=" + DUMMY_TENANT_DOMAIN;
        String expectedOut4 = REDIRECT_URL;

        return new Object[][]{
                {DUMMY_SP_NAME, DUMMY_TENANT_DOMAIN, expectedOut1},
                {DUMMY_SP_NAME, null, expectedOut2},
                {null, DUMMY_TENANT_DOMAIN, expectedOut3},
                {null, null, expectedOut4}
        };
    }

    @Test(dataProvider = "provideRequestAttributes")
    public void testGetRedirectURL(String spName, String tenantDomain, String expectedOut) {

        IdentityConfigParser.getInstance().getConfiguration().put(IdentityCoreConstants.ENABLE_TENANT_QUALIFIED_URLS, true);
        when(request.getAttribute(REQUEST_PARAM_SP)).thenReturn(spName);
        when(request.getAttribute(TENANT_DOMAIN)).thenReturn(tenantDomain);

        String out = FrameworkUtils.getRedirectURL(REDIRECT_URL, request);

        assertEquals(out, expectedOut);
    }

    private Cookie[] getAuthenticationCookies() {

        Cookie[] cookies = new Cookie[2];
        cookies[0] = new Cookie(FrameworkConstants.COMMONAUTH_COOKIE, "commonAuthIdValue");
        cookies[1] = new Cookie(FrameworkConstants.COMMONAUTH_COOKIE, "commonAuthIdValue");
        cookies[1].setPath(FrameworkConstants.TENANT_CONTEXT_PREFIX + DUMMY_TENANT_DOMAIN + "/");
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
        FrameworkUtils.removeAuthCookie(request, response, DUMMY_TENANT_DOMAIN);
        verify(response, times(1)).addCookie(cookieCaptor.capture());
        List<Cookie> capturedCookies = cookieCaptor.getAllValues();

        Cookie removedCookie = capturedCookies.get(0);
        assertEquals(removedCookie.getName(), FrameworkConstants.COMMONAUTH_COOKIE);
        assertEquals(removedCookie.getPath(), FrameworkConstants.TENANT_CONTEXT_PREFIX + DUMMY_TENANT_DOMAIN + "/");
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
        IdentityUtil.getIdentityCookiesConfigurationHolder().put(FrameworkConstants.COMMONAUTH_COOKIE, cookieConfig);

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
        IdentityUtil.getIdentityCookiesConfigurationHolder().put(FrameworkConstants.COMMONAUTH_COOKIE, cookieConfig);
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
    public void testSetCookieWithSameSiteCookieNonExistCookieConfig() {

        mockCookieTest();
        int age = 3600;

        FrameworkUtils.setCookie(request, response, FrameworkConstants.COMMONAUTH_COOKIE, "commonAuthIdValue", age, SameSiteCookie.STRICT, "Dummy-Path");

        verify(response, times(1)).addCookie(cookieCaptor.capture());
        List<Cookie> capturedCookies = cookieCaptor.getAllValues();
        Cookie storedCookie = capturedCookies.get(0);

        assertEquals(storedCookie.getName(), FrameworkConstants.COMMONAUTH_COOKIE);
        assertEquals(storedCookie.getPath(), "Dummy-Path");
        assertEquals(storedCookie.getMaxAge(), age);

    }

    @Test
    public void testSetCookieWithSameSiteExistCookieConfig() {

        mockCookieTest();
        IdentityCookieConfig cookieConfig = new IdentityCookieConfig(FrameworkConstants.COMMONAUTH_COOKIE);
        IdentityUtil.getIdentityCookiesConfigurationHolder().put(FrameworkConstants.COMMONAUTH_COOKIE, cookieConfig);
        int age = 3600;

        FrameworkUtils.setCookie(request, response, FrameworkConstants.COMMONAUTH_COOKIE, "commonAuthIdValue", age, SameSiteCookie.STRICT, "Dummy-Path");

        verify(response, times(1)).addCookie(cookieCaptor.capture());
        List<Cookie> capturedCookies = cookieCaptor.getAllValues();
        Cookie storedCookie = capturedCookies.get(0);

        assertEquals(storedCookie.getName(), FrameworkConstants.COMMONAUTH_COOKIE);
        assertEquals(storedCookie.getPath(), "Dummy-Path");
        assertEquals(storedCookie.getMaxAge(), age);
    }

    @Test
    public void testGetCookieExistingCookie() {

        Cookie[] cookies = getAuthenticationCookies();
        when(request.getCookies()).thenReturn(cookies);

        Cookie out = FrameworkUtils.getCookie(request, cookies[0].getName());

        assertEquals(out, cookies[0]);
    }

    @Test
    public void testGetCookieNonExistingCookie() {

        Cookie out = FrameworkUtils.getCookie(request, "nonExistingCookie");
        assertNull(out);
    }

    @Test
    public void testGetHashOfCookie() {

        Cookie cookie = new Cookie(FrameworkConstants.COMMONAUTH_COOKIE, "commonAuthIdValue");

        String out = FrameworkUtils.getHashOfCookie(cookie);

        assertEquals(out, DigestUtils.sha256Hex("commonAuthIdValue"));
    }

    @Test
    public void testGetPasswordProvisioningUIUrlEmptyURL() {

        String out = FrameworkUtils.getPasswordProvisioningUIUrl();
        assertEquals(out, FrameworkConstants.SIGN_UP_ENDPOINT);
    }

    @Test
    public void testGetPasswordProvisioningUIUrlNonEmptyURL() {

        String dummyURL = "Dummy_Provisioning_URL";
        IdentityConfigParser.getInstance().getConfiguration().put("JITProvisioning.PasswordProvisioningUI", dummyURL);
        String out = FrameworkUtils.getPasswordProvisioningUIUrl();
        assertEquals(out, "/accountrecoveryendpoint/signup.do");
    }

    @Test
    public void testGetUserNameProvisioningUIUrlEmptyURL() {

        String out = FrameworkUtils.getUserNameProvisioningUIUrl();
        assertEquals(out, FrameworkConstants.REGISTRATION_ENDPOINT);
    }

    @Test
    public void testGetUserNameProvisioningUIUrlNonEmptyURL() {

        String dummyURL = "Dummy_Provisioning_URL";
        IdentityConfigParser.getInstance().getConfiguration().put("JITProvisioning.UserNameProvisioningUI", dummyURL);

        String out = FrameworkUtils.getUserNameProvisioningUIUrl();
        assertEquals(out, "/accountrecoveryendpoint/register.do");
    }

    private void setMockedSessionContextCache() {

        mockStatic(SessionContextCache.class);
        when(SessionContextCache.getInstance()).thenReturn(mockedSessionContextCache);
    }

    @Test
    public void testGetSessionContextFromCacheNullCacheEntry() {

        setMockedSessionContextCache();

        SessionContext out = FrameworkUtils.getSessionContextFromCache(DUMMY_CACHE_KEY);
        System.out.println(out);
        assertNull(out);
    }

    @Test
    public void testGetSessionContextFromCacheValidCacheEntry() {

        cacheEntry.setContext(context);
        setMockedSessionContextCache();
        when(mockedSessionContextCache.getValueFromCache(cacheKey)).thenReturn(cacheEntry);

        SessionContext out = FrameworkUtils.getSessionContextFromCache(DUMMY_CACHE_KEY);
        assertEquals(out, context);
    }

    @Test
    public void testGetSessionContextFromCacheExpiredSession() throws FrameworkException {

        cacheEntry.setContext(context);
        setMockedSessionContextCache();
        when(mockedSessionContextCache.getValueFromCache(cacheKey)).thenReturn(cacheEntry);
        when(mockedSessionContextCache.isSessionExpired(any(SessionContextCacheKey.class), any(SessionContextCacheEntry.class))).thenReturn(true);
        IdentityEventService identityEventService = new IdentityEventServiceImpl(Collections.EMPTY_LIST, 1);
        FrameworkServiceDataHolder.getInstance().setIdentityEventService(identityEventService);
        AuthenticationContext authenticationContext = new AuthenticationContext();

        SessionContext out = FrameworkUtils.getSessionContextFromCache(request, authenticationContext, DUMMY_CACHE_KEY);
        assertNull(out);
    }

    @Test
    public void testGetSessionContextFromCacheNotExpiredSession() throws FrameworkException {

        cacheEntry.setContext(context);
        setMockedSessionContextCache();
        when(mockedSessionContextCache.getSessionContextCacheEntry(cacheKey)).thenReturn(cacheEntry);
        when(mockedSessionContextCache.isSessionExpired(any(SessionContextCacheKey.class), any(SessionContextCacheEntry.class))).thenReturn(false);

        SessionContext out = FrameworkUtils.getSessionContextFromCache(request, authenticationContext, DUMMY_CACHE_KEY);
        assertEquals(out, context);

    }
}
