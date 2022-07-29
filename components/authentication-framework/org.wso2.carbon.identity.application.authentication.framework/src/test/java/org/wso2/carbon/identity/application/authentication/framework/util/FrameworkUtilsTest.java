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

import static org.mockito.ArgumentMatchers.any;
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
import static org.wso2.carbon.utils.multitenancy.MultitenantConstants.SUPER_TENANT_DOMAIN_NAME;

@WithCarbonHome
@PrepareForTest({SameSiteCookie.class, SessionContextCache.class, AuthenticationResultCache.class,
        AuthenticationContextCache.class, IdentityTenantUtil.class})
@PowerMockIgnore({"javax.net.*", "javax.security.*", "javax.crypto.*", "javax.xml.*"})
public class FrameworkUtilsTest extends PowerMockIdentityBaseTest {

    private static final String ROOT_DOMAIN = "/";
    private static final String DUMMY_TENANT_DOMAIN = "ABC";
    private static final String DUMMY_SP_NAME = "wso2carbon-local-sp";
    private static final String REDIRECT_URL = "custom-page?";
    private static final String DUMMY_CACHE_KEY = "cache-key";

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
        authenticationContext.setTenantDomain("abc");
    }

    @Test
    public void testGetAppAuthenticatorByNameExistingAuthenticator() {

        ApplicationAuthenticator applicationAuthenticator;
        applicationAuthenticator = FrameworkUtils.getAppAuthenticatorByName("BasicAuthenticator");
        assert applicationAuthenticator != null;
        assertEquals(applicationAuthenticator.getName(), "BasicAuthenticator");
    }

    @Test
    public void testGetAppAuthenticatorByNameNonExistingAuthenticator() {

        ApplicationAuthenticator applicationAuthenticator;
        applicationAuthenticator = FrameworkUtils.getAppAuthenticatorByName("NonExistingAuthenticator");
        assertNull(applicationAuthenticator);
    }

    @Test
    public void testGetRequestCoordinatorExistingHandler() {

        DefaultRequestCoordinator testRequestCoordinator = new DefaultRequestCoordinator();
        ConfigurationFacade.getInstance().getExtensions()
                .put(FrameworkConstants.Config.QNAME_EXT_REQ_COORDINATOR, testRequestCoordinator);
        Object requestCoordinator = FrameworkUtils.getRequestCoordinator();
        assertEquals(requestCoordinator, testRequestCoordinator);
    }

    @Test
    public void testGetRequestCoordinatorNonExistingReqCoordinator() {

        ConfigurationFacade.getInstance().getExtensions().remove(FrameworkConstants.Config.QNAME_EXT_REQ_COORDINATOR);
        Object requestCoordinator = FrameworkUtils.getRequestCoordinator();
        assertEquals(requestCoordinator.getClass(), DefaultRequestCoordinator.class);
    }

    @Test
    public void testGetAuthenticationRequestHandlerExistingHandler() {

        DefaultAuthenticationRequestHandler testAuthenticationRequestHandler;
        testAuthenticationRequestHandler = new DefaultAuthenticationRequestHandler();
        ConfigurationFacade.getInstance().getExtensions()
                .put(FrameworkConstants.Config.QNAME_EXT_AUTH_REQ_HANDLER, testAuthenticationRequestHandler);
        Object authenticationRequestHandler = FrameworkUtils.getAuthenticationRequestHandler();
        assertEquals(authenticationRequestHandler, testAuthenticationRequestHandler);
    }

    @Test
    public void testGetAuthenticationRequestHandlerNonExistingReqCoordinator() {

        ConfigurationFacade.getInstance().getExtensions().remove(FrameworkConstants.Config.QNAME_EXT_AUTH_REQ_HANDLER);
        Object authenticationRequestHandler = FrameworkUtils.getAuthenticationRequestHandler();
        assertEquals(authenticationRequestHandler.getClass(), DefaultAuthenticationRequestHandler.class);
    }

    @Test
    public void testGetLogoutRequestHandlerExistingHandler() {

        DefaultLogoutRequestHandler testLogoutRequestHandler = new DefaultLogoutRequestHandler();
        ConfigurationFacade.getInstance().getExtensions()
                .put(FrameworkConstants.Config.QNAME_EXT_LOGOUT_REQ_HANDLER, testLogoutRequestHandler);
        Object logoutRequestHandler = FrameworkUtils.getLogoutRequestHandler();
        assertEquals(logoutRequestHandler, testLogoutRequestHandler);
    }

    @Test
    public void testGetLogoutRequestHandlerNonExistingReqCoordinator() {

        ConfigurationFacade.getInstance().getExtensions()
                .remove(FrameworkConstants.Config.QNAME_EXT_LOGOUT_REQ_HANDLER);
        Object logoutRequestHandler = FrameworkUtils.getLogoutRequestHandler();
        assertEquals(logoutRequestHandler.getClass(), DefaultLogoutRequestHandler.class);
    }

    @Test
    public void testGetStepBasedSequenceHandlerExistingHandler() {

        DefaultStepBasedSequenceHandler testStepBasedSequenceHandler = new DefaultStepBasedSequenceHandler();
        ConfigurationFacade.getInstance().getExtensions()
                .put(FrameworkConstants.Config.QNAME_EXT_STEP_BASED_SEQ_HANDLER, testStepBasedSequenceHandler);
        Object stepBasedSequenceHandler = FrameworkUtils.getStepBasedSequenceHandler();
        assertEquals(stepBasedSequenceHandler, testStepBasedSequenceHandler);
    }

    @Test
    public void testGetStepBasedSequenceHandlerNonExistingReqCoordinator() {

        ConfigurationFacade.getInstance().getExtensions()
                .remove(FrameworkConstants.Config.QNAME_EXT_STEP_BASED_SEQ_HANDLER);
        Object stepBasedSequenceHandler = FrameworkUtils.getStepBasedSequenceHandler();
        assertEquals(stepBasedSequenceHandler.getClass(), GraphBasedSequenceHandler.class);
    }

    @Test
    public void testGetRequestPathBasedSequenceHandlerExistingHandler() {

        DefaultRequestPathBasedSequenceHandler testRequestPathBasedSequenceHandler;
        testRequestPathBasedSequenceHandler = new DefaultRequestPathBasedSequenceHandler();
        ConfigurationFacade.getInstance().getExtensions()
                .put(FrameworkConstants.Config.QNAME_EXT_REQ_PATH_BASED_SEQ_HANDLER,
                        testRequestPathBasedSequenceHandler);
        Object requestPathBasedSequenceHandler = FrameworkUtils.getRequestPathBasedSequenceHandler();
        assertEquals(requestPathBasedSequenceHandler, testRequestPathBasedSequenceHandler);
    }

    @Test
    public void testGetRequestPathBasedSequenceHandlerNonExistingHandler() {

        ConfigurationFacade.getInstance().getExtensions()
                .remove(FrameworkConstants.Config.QNAME_EXT_REQ_PATH_BASED_SEQ_HANDLER);
        Object requestPathBasedSequenceHandler = FrameworkUtils.getRequestPathBasedSequenceHandler();
        assertEquals(requestPathBasedSequenceHandler.getClass(), DefaultRequestPathBasedSequenceHandler.class);
    }

    @Test
    public void testGetStepHandlerExistHandler() {

        DefaultStepHandler testStepHandler = new DefaultStepHandler();
        ConfigurationFacade.getInstance().getExtensions()
                .put(FrameworkConstants.Config.QNAME_EXT_STEP_HANDLER, testStepHandler);
        StepHandler stepHandler = FrameworkUtils.getStepHandler();
        assertEquals(stepHandler, testStepHandler);
    }

    @Test
    public void testGetStepHandlerNonExistHandler() {

        ConfigurationFacade.getInstance().getExtensions().remove(FrameworkConstants.Config.QNAME_EXT_STEP_HANDLER);
        StepHandler stepHandler = FrameworkUtils.getStepHandler();
        assertEquals(GraphBasedStepHandler.class, stepHandler.getClass());
    }

    @Test
    public void testGetHomeRealmDiscovererExistingDiscoverer() {

        DefaultHomeRealmDiscoverer testHomeRealmDiscoverer = new DefaultHomeRealmDiscoverer();
        ConfigurationFacade.getInstance().getExtensions()
                .put(FrameworkConstants.Config.QNAME_EXT_HRD, testHomeRealmDiscoverer);
        HomeRealmDiscoverer homeRealmDiscoverer = FrameworkUtils.getHomeRealmDiscoverer();
        assertEquals(homeRealmDiscoverer, testHomeRealmDiscoverer);
    }

    @Test
    public void testGetHomeRealmDiscovererNonExistDiscoverer() {

        ConfigurationFacade.getInstance().getExtensions().remove(FrameworkConstants.Config.QNAME_EXT_HRD);
        Object realmDiscoverer = FrameworkUtils.getHomeRealmDiscoverer();
        assertEquals(realmDiscoverer.getClass(), DefaultHomeRealmDiscoverer.class);
    }

    @Test
    public void testGetClaimHandlerExistHandler() {

        DefaultClaimHandler testClaimHandler = new DefaultClaimHandler();
        ConfigurationFacade.getInstance().getExtensions()
                .put(FrameworkConstants.Config.QNAME_EXT_CLAIM_HANDLER, testClaimHandler);
        ClaimHandler claimHandler = FrameworkUtils.getClaimHandler();
        assertEquals(claimHandler, testClaimHandler);
    }

    @Test
    public void testGetClaimHandlerNonExistHandler() {

        ConfigurationFacade.getInstance().getExtensions().remove(FrameworkConstants.Config.QNAME_EXT_CLAIM_HANDLER);
        Object claimHandler = FrameworkUtils.getClaimHandler();
        assertEquals(claimHandler.getClass(), DefaultClaimHandler.class);
    }

    @Test
    public void testGetProvisioningHandlerExistHandler() {

        DefaultProvisioningHandler testProvisioningHandler = new DefaultProvisioningHandler();
        ConfigurationFacade.getInstance().getExtensions()
                .put(FrameworkConstants.Config.QNAME_EXT_PROVISIONING_HANDLER, testProvisioningHandler);
        ProvisioningHandler provisioningHandler = FrameworkUtils.getProvisioningHandler();
        assertEquals(provisioningHandler, testProvisioningHandler);
    }

    @Test
    public void testGetProvisioningHandlerNonExistHandler() {

        ConfigurationFacade.getInstance().getExtensions()
                .remove(FrameworkConstants.Config.QNAME_EXT_PROVISIONING_HANDLER);
        Object provisioningHandler = FrameworkUtils.getProvisioningHandler();
        assertEquals(provisioningHandler.getClass(), DefaultProvisioningHandler.class);
    }

    @Test
    public void getAddAuthenticationContextToCache() {

        mockStatic(AuthenticationContextCache.class);
        when(AuthenticationContextCache.getInstance()).thenReturn(mockedAuthenticationContextCache);
        String contextId = "CONTEXT-ID";

        FrameworkUtils.addAuthenticationContextToCache(contextId, authenticationContext);

        ArgumentCaptor<AuthenticationContextCacheKey> captorKey;
        captorKey = ArgumentCaptor.forClass(AuthenticationContextCacheKey.class);
        ArgumentCaptor<AuthenticationContextCacheEntry> captorEntry;
        captorEntry = ArgumentCaptor.forClass(AuthenticationContextCacheEntry.class);
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

        String modifiedUrl = FrameworkUtils.appendQueryParamsToUrl(url, queryParamMap);
        assertEquals(modifiedUrl, expectedOutput);
    }

    @DataProvider(name = "provideQueryParamData")
    public Object[][] provideQueryParamData() {

        Map<String, String> queryParamMap = new HashMap<>();
        queryParamMap.put("a", "wer");
        queryParamMap.put("b", "dfg");

        return new Object[][]{
                {queryParamMap}
        };
    }

    @Test(dataProvider = "provideURLParamData")
    public void testBuildURLWithQueryParams(String url, Map<String, String> queryParamMap, String expectedOutput)
            throws UnsupportedEncodingException {

        String modifiedUrl = FrameworkUtils.buildURLWithQueryParams(url, queryParamMap);
        assertEquals(modifiedUrl, expectedOutput);
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
        when(mockedAuthenticationResultCache.getValueFromCache(authenticationCacheKey))
                .thenReturn(authenticationCacheEntry);
    }

    @Test
    public void getAddAuthenticationResultToCache() {

        setMockedAuthenticationResultCache();
        FrameworkUtils.addAuthenticationResultToCache(DUMMY_CACHE_KEY, authenticationResult);

        ArgumentCaptor<AuthenticationResultCacheKey> captorKey;
        captorKey = ArgumentCaptor.forClass(AuthenticationResultCacheKey.class);
        ArgumentCaptor<AuthenticationResultCacheEntry> captorEntry;
        captorEntry = ArgumentCaptor.forClass(AuthenticationResultCacheEntry.class);
        verify(mockedAuthenticationResultCache).addToCache(captorKey.capture(), captorEntry.capture());
        assertEquals(captorKey.getValue().getResultId(), DUMMY_CACHE_KEY);
        assertEquals(captorEntry.getValue().getResult(), authenticationResult);
        assertTrue(captorEntry.getValue().getValidityPeriod() > 0);
    }

    @Test
    public void testGetAuthenticationResultFromCache() {

        setMockedAuthenticationResultCache();
        AuthenticationResultCacheEntry cacheEntry = FrameworkUtils.getAuthenticationResultFromCache(DUMMY_CACHE_KEY);
        assertEquals(cacheEntry, authenticationCacheEntry);
    }

    @Test
    public void testRemoveAuthenticationResultFromCache() {

        setMockedAuthenticationResultCache();
        FrameworkUtils.removeAuthenticationResultFromCache(DUMMY_CACHE_KEY);

        ArgumentCaptor<AuthenticationResultCacheKey> captor;
        captor = ArgumentCaptor.forClass(AuthenticationResultCacheKey.class);
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

        IdentityConfigParser.getInstance().getConfiguration()
                .put(IdentityCoreConstants.ENABLE_TENANT_QUALIFIED_URLS, true);
        when(request.getAttribute(REQUEST_PARAM_SP)).thenReturn(spName);
        when(request.getAttribute(TENANT_DOMAIN)).thenReturn(tenantDomain);

        String redirectURL = FrameworkUtils.getRedirectURL(REDIRECT_URL, request);
        assertEquals(redirectURL, expectedOut);
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
        assertEquals(removedCookie.getPath(),
                FrameworkConstants.TENANT_CONTEXT_PREFIX + DUMMY_TENANT_DOMAIN + "/");
    }

    @Test
    public void testRemoveCookieNonExistCookieConfig() {

        mockCookieTest();
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

        FrameworkUtils.setCookie(request, response, FrameworkConstants.COMMONAUTH_COOKIE, "commonAuthIdValue", age,
                SameSiteCookie.STRICT, "Dummy-Path");

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

        FrameworkUtils.setCookie(request, response, FrameworkConstants.COMMONAUTH_COOKIE,
                "commonAuthIdValue", age, SameSiteCookie.STRICT, "Dummy-Path");

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

        Cookie cookie = FrameworkUtils.getCookie(request, cookies[0].getName());
        assertEquals(cookie, cookies[0]);
    }

    @Test
    public void testGetCookieNonExistingCookie() {

        Cookie cookie = FrameworkUtils.getCookie(request, "nonExistingCookie");
        assertNull(cookie);
    }

    @Test
    public void testGetHashOfCookie() {

        Cookie cookie = new Cookie(FrameworkConstants.COMMONAUTH_COOKIE, "commonAuthIdValue");
        String getHashOfCookie = FrameworkUtils.getHashOfCookie(cookie);
        assertEquals(getHashOfCookie, DigestUtils.sha256Hex("commonAuthIdValue"));
    }

    @Test
    public void testGetPasswordProvisioningUIUrlEmptyURL() {

        String passwordProvisioningUIUrl = FrameworkUtils.getPasswordProvisioningUIUrl();
        assertEquals(passwordProvisioningUIUrl, FrameworkConstants.SIGN_UP_ENDPOINT);
    }

    @Test
    public void testGetPasswordProvisioningUIUrlNonEmptyURL() {

        String dummyURL = "Dummy_Provisioning_URL";
        IdentityConfigParser.getInstance().getConfiguration()
                .put("JITProvisioning.PasswordProvisioningUI", dummyURL);
        String passwordProvisioningUIUrl = FrameworkUtils.getPasswordProvisioningUIUrl();
        assertEquals(passwordProvisioningUIUrl, "/accountrecoveryendpoint/signup.do");
    }

    @Test
    public void testGetUserNameProvisioningUIUrlEmptyURL() {

        String userNameProvisioningUIUrl = FrameworkUtils.getUserNameProvisioningUIUrl();
        assertEquals(userNameProvisioningUIUrl, FrameworkConstants.REGISTRATION_ENDPOINT);
    }

    @Test
    public void testGetUserNameProvisioningUIUrlNonEmptyURL() {

        String dummyURL = "Dummy_Provisioning_URL";
        IdentityConfigParser.getInstance().getConfiguration()
                .put("JITProvisioning.UserNameProvisioningUI", dummyURL);

        String userNameProvisioningUIUrl = FrameworkUtils.getUserNameProvisioningUIUrl();
        assertEquals(userNameProvisioningUIUrl, "/accountrecoveryendpoint/register.do");
    }

    private void setMockedSessionContextCache() {

        mockStatic(SessionContextCache.class);
        when(SessionContextCache.getInstance()).thenReturn(mockedSessionContextCache);
    }

    @Test
    public void testGetSessionContextFromCacheNullCacheEntry() {

        setMockedSessionContextCache();
        SessionContext sessionContext = FrameworkUtils.getSessionContextFromCache(DUMMY_CACHE_KEY,
                SUPER_TENANT_DOMAIN_NAME);
        assertNull(sessionContext);
    }

    @Test
    public void testGetSessionContextFromCacheValidCacheEntry() {

        cacheEntry.setContext(context);
        setMockedSessionContextCache();
        when(mockedSessionContextCache.getValueFromCache(cacheKey, "abc")).thenReturn(cacheEntry);

        SessionContext sessionContext = FrameworkUtils.getSessionContextFromCache(DUMMY_CACHE_KEY, "abc");
        assertEquals(sessionContext, context);
    }

    @Test
    public void testGetSessionContextFromCacheExpiredSession() throws FrameworkException {

        cacheEntry.setContext(context);
        setMockedSessionContextCache();
        when(mockedSessionContextCache.getValueFromCache(cacheKey)).thenReturn(cacheEntry);
        when(mockedSessionContextCache.isSessionExpired(any(SessionContextCacheKey.class),
                any(SessionContextCacheEntry.class))).thenReturn(true);
        IdentityEventService identityEventService = new IdentityEventServiceImpl(Collections.EMPTY_LIST, 1);

        FrameworkServiceDataHolder.getInstance().setIdentityEventService(identityEventService);
        AuthenticationContext authenticationContext = new AuthenticationContext();
        SessionContext sessionContext = FrameworkUtils.getSessionContextFromCache(request,
                authenticationContext, DUMMY_CACHE_KEY);
        assertNull(sessionContext);
    }

    @Test
    public void testGetSessionContextFromCacheNotExpiredSession() throws FrameworkException {

        cacheEntry.setContext(context);
        setMockedSessionContextCache();
        when(mockedSessionContextCache.getSessionContextCacheEntry(cacheKey, "abc")).thenReturn(cacheEntry);
        when(mockedSessionContextCache.isSessionExpired(any(SessionContextCacheKey.class),
                any(SessionContextCacheEntry.class))).thenReturn(false);
        SessionContext sessionContext = FrameworkUtils.getSessionContextFromCache(request,
                authenticationContext, DUMMY_CACHE_KEY);
        assertEquals(sessionContext, context);
    }
}
