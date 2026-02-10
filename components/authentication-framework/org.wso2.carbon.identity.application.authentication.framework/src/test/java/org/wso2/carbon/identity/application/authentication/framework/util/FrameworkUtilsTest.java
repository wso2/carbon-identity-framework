/*
 * Copyright (c) 2017-2025, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.carbon.identity.application.authentication.framework.util;

import org.apache.commons.codec.digest.DigestUtils;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;
import org.wso2.carbon.context.CarbonContext;
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
import org.wso2.carbon.identity.application.authentication.framework.config.model.ApplicationConfig;
import org.wso2.carbon.identity.application.authentication.framework.config.model.ExternalIdPConfig;
import org.wso2.carbon.identity.application.authentication.framework.config.model.SequenceConfig;
import org.wso2.carbon.identity.application.authentication.framework.context.AuthenticationContext;
import org.wso2.carbon.identity.application.authentication.framework.context.SessionContext;
import org.wso2.carbon.identity.application.authentication.framework.exception.FrameworkException;
import org.wso2.carbon.identity.application.authentication.framework.handler.approles.ApplicationRolesResolver;
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
import org.wso2.carbon.identity.application.authentication.framework.internal.FrameworkServiceDataHolder;
import org.wso2.carbon.identity.application.authentication.framework.internal.core.ApplicationAuthenticatorManager;
import org.wso2.carbon.identity.application.authentication.framework.model.AuthenticationResult;
import org.wso2.carbon.identity.application.authentication.framework.model.ImpersonatedUser;
import org.wso2.carbon.identity.application.authentication.framework.model.OrganizationDiscoveryInput;
import org.wso2.carbon.identity.application.authentication.framework.store.SessionDataStore;
import org.wso2.carbon.identity.application.common.model.ClaimConfig;
import org.wso2.carbon.identity.application.common.model.ClaimMapping;
import org.wso2.carbon.identity.application.common.model.FederatedAuthenticatorConfig;
import org.wso2.carbon.identity.application.common.model.IdentityProvider;
import org.wso2.carbon.identity.application.common.model.ServiceProvider;
import org.wso2.carbon.identity.claim.metadata.mgt.ClaimMetadataHandler;
import org.wso2.carbon.identity.common.testng.WithCarbonHome;
import org.wso2.carbon.identity.core.model.IdentityCookieConfig;
import org.wso2.carbon.identity.core.util.IdentityConfigParser;
import org.wso2.carbon.identity.core.util.IdentityCoreConstants;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;
import org.wso2.carbon.identity.core.util.IdentityUtil;
import org.wso2.carbon.identity.event.services.IdentityEventService;
import org.wso2.carbon.identity.event.services.IdentityEventServiceImpl;
import org.wso2.carbon.identity.organization.management.service.OrganizationManager;
import org.wso2.carbon.identity.organization.management.service.exception.OrganizationManagementException;
import org.wso2.carbon.identity.testutil.IdentityBaseTest;
import org.wso2.carbon.idp.mgt.IdentityProviderManager;
import org.wso2.carbon.user.api.RealmConfiguration;
import org.wso2.carbon.user.api.UserRealm;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.api.UserStoreManager;
import org.wso2.carbon.user.core.UserCoreConstants;
import org.wso2.carbon.user.core.common.AbstractUserStoreManager;
import org.wso2.carbon.user.core.common.User;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.user.core.tenant.TenantManager;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;
import static org.wso2.carbon.identity.application.authentication.framework.util.FrameworkConstants.GROUPS_CLAIM;
import static org.wso2.carbon.identity.application.authentication.framework.util.FrameworkConstants.OrgDiscoveryInputParameters.LOGIN_HINT;
import static org.wso2.carbon.identity.application.authentication.framework.util.FrameworkConstants.OrgDiscoveryInputParameters.ORG_DISCOVERY_TYPE;
import static org.wso2.carbon.identity.application.authentication.framework.util.FrameworkConstants.OrgDiscoveryInputParameters.ORG_HANDLE;
import static org.wso2.carbon.identity.application.authentication.framework.util.FrameworkConstants.OrgDiscoveryInputParameters.ORG_ID;
import static org.wso2.carbon.identity.application.authentication.framework.util.FrameworkConstants.OrgDiscoveryInputParameters.ORG_NAME;
import static org.wso2.carbon.identity.application.authentication.framework.util.FrameworkConstants.REQUEST_PARAM_SP;
import static org.wso2.carbon.identity.application.authentication.framework.util.FrameworkConstants.ROLES_CLAIM;
import static org.wso2.carbon.identity.application.authentication.framework.util.FrameworkConstants.USERNAME_CLAIM;
import static org.wso2.carbon.identity.application.authentication.framework.util.FrameworkConstants.USE_IDP_ROLE_CLAIM_AS_IDP_GROUP_CLAIM;
import static org.wso2.carbon.identity.application.authentication.framework.util.FrameworkUtils.TENANT_DOMAIN;
import static org.wso2.carbon.identity.core.util.IdentityUtil.getLocalGroupsClaimURI;
import static org.wso2.carbon.utils.multitenancy.MultitenantConstants.SUPER_TENANT_DOMAIN_NAME;

@WithCarbonHome
@Listeners(MockitoTestNGListener.class)
public class FrameworkUtilsTest extends IdentityBaseTest {

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

    @Mock
    private IdentityProviderManager mockedIdentityProviderManager;

    @Mock
    private IdentityProvider mockedIdentityProvider;

    @Mock
    private ClaimConfig mockedClaimConfig;

    @Mock
    private ClaimMapping mockedClaimMapping;
    @Mock
    private FederatedAuthenticatorConfig mockedFederatedAuthenticatorConfig;
    @Mock
    private ClaimMetadataHandler mockedClaimMetadataHandler;

    @Mock
    private SessionDataStore mockedSessionDataStore;

    @Mock
    private AuthenticationResultCacheEntry mockedAuthenticationResultCacheEntry;

    @Mock
    private AuthenticationResult mockedAuthenticationResult;

    @Captor
    ArgumentCaptor<Cookie> cookieCaptor;

    @BeforeClass
    public void setFrameworkServiceComponent() {

        removeAllSystemDefinedAuthenticators();
        ApplicationAuthenticatorManager.getInstance().addSystemDefinedAuthenticator(
                new MockAuthenticator("BasicAuthenticator"));
        ApplicationAuthenticatorManager.getInstance().addSystemDefinedAuthenticator(
                new MockAuthenticator("HwkMockAuthenticator"));
        ApplicationAuthenticatorManager.getInstance().addSystemDefinedAuthenticator(
                new MockAuthenticator("FederatedAuthenticator", null, "sampleClaimDialectURI"));
           
        authenticationContext.setTenantDomain("abc");
    }

    @Test
    public void testGetAppAuthenticatorByNameExistingAuthenticator() {

        ApplicationAuthenticator applicationAuthenticator;
        applicationAuthenticator = ApplicationAuthenticatorManager.getInstance()
                .getSystemDefinedAuthenticatorByName("BasicAuthenticator");
        assert applicationAuthenticator != null;
        assertEquals(applicationAuthenticator.getName(), "BasicAuthenticator");
    }

    @Test
    public void testGetAppAuthenticatorByNameNonExistingAuthenticator() {

        ApplicationAuthenticator applicationAuthenticator;
        applicationAuthenticator = ApplicationAuthenticatorManager.getInstance()
                .getSystemDefinedAuthenticatorByName("NonExistingAuthenticator");
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

        try (MockedStatic<AuthenticationContextCache> authenticationContextCache =
                mockStatic(AuthenticationContextCache.class)) {
            authenticationContextCache.when(
                    AuthenticationContextCache::getInstance).thenReturn(mockedAuthenticationContextCache);
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

    @Test
    public void getAddAuthenticationResultToCache() {

        try (MockedStatic<AuthenticationResultCache> authenticationResultCache =
                     mockStatic(AuthenticationResultCache.class)) {
            authenticationResultCache.when(
                    AuthenticationResultCache::getInstance).thenReturn(mockedAuthenticationResultCache);
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
    }

    @Test
    public void testGetAuthenticationResultFromCache() {

        try (MockedStatic<AuthenticationResultCache> authenticationResultCache =
                     mockStatic(AuthenticationResultCache.class)) {
            authenticationResultCache.when(
                    AuthenticationResultCache::getInstance).thenReturn(mockedAuthenticationResultCache);
            when(mockedAuthenticationResultCache.getValueFromCache(authenticationCacheKey))
                    .thenReturn(authenticationCacheEntry);
            AuthenticationResultCacheEntry cacheEntry =
                    FrameworkUtils.getAuthenticationResultFromCache(DUMMY_CACHE_KEY);
            assertEquals(cacheEntry, authenticationCacheEntry);
        }
    }

    @Test
    public void testGetAuthenticationResultFromSessionDataStore() {

        try (MockedStatic<SessionDataStore> sessionDataStore = mockStatic(SessionDataStore.class);
             MockedStatic<IdentityUtil> identityUtil = mockStatic(IdentityUtil.class)) {

            sessionDataStore.when(SessionDataStore::getInstance).thenReturn(mockedSessionDataStore);
            identityUtil.when(() -> IdentityUtil.getProperty("JDBCPersistenceManager.SessionDataPersist.Temporary"))
                    .thenReturn("true");

            AuthenticationResultCache authenticationCacheSpy = spy(AuthenticationResultCache.getInstance());

            AuthenticationResultCacheKey key = new AuthenticationResultCacheKey(DUMMY_CACHE_KEY);

            when(authenticationCacheSpy.getValueFromCache(eq(key), anyString())).thenReturn(null);

            when(mockedSessionDataStore.getSessionData(anyString(), anyString()))
                    .thenReturn(mockedAuthenticationResultCacheEntry);
            when(mockedAuthenticationResultCacheEntry.getResult()).thenReturn(mockedAuthenticationResult);
            when(mockedAuthenticationResultCacheEntry.getValidityPeriod()).thenReturn(60000000L);
            when(mockedAuthenticationResult.getProperty(anyString())).thenReturn(System.currentTimeMillis());

            AuthenticationResultCacheEntry result = authenticationCacheSpy.getValueFromCache(key);
            assertNotNull(result);
        }
    }

    @Test
    public void testGetAuthenticationResultFromSessionDataStoreExpired() {

        try (MockedStatic<SessionDataStore> sessionDataStore = mockStatic(SessionDataStore.class);
            MockedStatic<IdentityUtil> identityUtil = mockStatic(IdentityUtil.class)) {

                sessionDataStore.when(SessionDataStore::getInstance).thenReturn(mockedSessionDataStore);
                identityUtil.when(() -> IdentityUtil.getProperty("JDBCPersistenceManager.SessionDataPersist.Temporary"))
                        .thenReturn("true");

            AuthenticationResultCache authenticationCacheSpy = spy(AuthenticationResultCache.getInstance());

            AuthenticationResultCacheKey key = new AuthenticationResultCacheKey(DUMMY_CACHE_KEY);

            when(authenticationCacheSpy.getValueFromCache(eq(key), anyString())).thenReturn(null);

            when(mockedSessionDataStore.getSessionData(anyString(), anyString()))
                    .thenReturn(mockedAuthenticationResultCacheEntry);
            when(mockedAuthenticationResultCacheEntry.getResult()).thenReturn(mockedAuthenticationResult);
            when(mockedAuthenticationResultCacheEntry.getValidityPeriod()).thenReturn(-60000000L);
            when(mockedAuthenticationResult.getProperty(anyString())).thenReturn(System.currentTimeMillis());

            AuthenticationResultCacheEntry result = authenticationCacheSpy.getValueFromCache(key);
            assertNull(result);
        }
    }

    @Test
    public void testGetAuthenticationResultFromSessionDataStoreNoTimestamp() {

        try (MockedStatic<SessionDataStore> sessionDataStore = mockStatic(SessionDataStore.class);
             MockedStatic<IdentityUtil> identityUtil = mockStatic(IdentityUtil.class)) {

            sessionDataStore.when(SessionDataStore::getInstance).thenReturn(mockedSessionDataStore);
            identityUtil.when(() -> IdentityUtil.getProperty("JDBCPersistenceManager.SessionDataPersist.Temporary"))
                    .thenReturn("true");

            AuthenticationResultCache authenticationCacheSpy = spy(AuthenticationResultCache.getInstance());

            AuthenticationResultCacheKey key = new AuthenticationResultCacheKey(DUMMY_CACHE_KEY);

            when(authenticationCacheSpy.getValueFromCache(eq(key), anyString())).thenReturn(null);

            when(mockedSessionDataStore.getSessionData(anyString(), anyString()))
                    .thenReturn(mockedAuthenticationResultCacheEntry);
            when(mockedAuthenticationResultCacheEntry.getResult()).thenReturn(mockedAuthenticationResult);
            when(mockedAuthenticationResult.getProperty(anyString())).thenReturn(null);

            AuthenticationResultCacheEntry result = authenticationCacheSpy.getValueFromCache(key);
            assertNotNull(result);
        }
    }

    @Test
    public void testRemoveAuthenticationResultFromCache() {

        try (MockedStatic<AuthenticationResultCache> authenticationResultCache =
                     mockStatic(AuthenticationResultCache.class)) {
            authenticationResultCache.when(
                    AuthenticationResultCache::getInstance).thenReturn(mockedAuthenticationResultCache);

            FrameworkUtils.removeAuthenticationResultFromCache(DUMMY_CACHE_KEY);

            ArgumentCaptor<AuthenticationResultCacheKey> captor;
            captor = ArgumentCaptor.forClass(AuthenticationResultCacheKey.class);
            verify(mockedAuthenticationResultCache).clearCacheEntry(captor.capture());
            assertEquals(captor.getValue().getResultId(), DUMMY_CACHE_KEY);
        }
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

        verify(response, never()).addCookie(any());
    }

    @Test
    public void testSetCookieNonExistCookieConfig() {

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
    public void testSetCookieExistCookieConfigWithMaxAgeAndPath() {

        IdentityCookieConfig cookieConfig = new IdentityCookieConfig(FrameworkConstants.COMMONAUTH_COOKIE);
        cookieConfig.setPath("Dummy-Path");
        cookieConfig.setMaxAge(3600);
        IdentityUtil.getIdentityCookiesConfigurationHolder().put(FrameworkConstants.COMMONAUTH_COOKIE, cookieConfig);
        int age = 7200;

        FrameworkUtils.setCookie(request, response, FrameworkConstants.COMMONAUTH_COOKIE, "commonAuthIdValue", age);

        verify(response, times(1)).addCookie(cookieCaptor.capture());
        List<Cookie> capturedCookies = cookieCaptor.getAllValues();
        Cookie storedCookie = capturedCookies.get(0);
        assertEquals(storedCookie.getName(), FrameworkConstants.COMMONAUTH_COOKIE);
        assertEquals(storedCookie.getPath(), "Dummy-Path");
        assertEquals(storedCookie.getMaxAge(), age);

        IdentityUtil.getIdentityCookiesConfigurationHolder().put(FrameworkConstants.COMMONAUTH_COOKIE, null);
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

    @Test
    public void testGetSessionContextFromCacheNullCacheEntry() {

        try (MockedStatic<SessionContextCache> sessionContextCache = mockStatic(SessionContextCache.class)) {
            sessionContextCache.when(SessionContextCache::getInstance).thenReturn(mockedSessionContextCache);
            SessionContext sessionContext = FrameworkUtils.getSessionContextFromCache(DUMMY_CACHE_KEY,
                    SUPER_TENANT_DOMAIN_NAME);
            assertNull(sessionContext);
        }
    }

    @Test
    public void testGetSessionContextFromCacheValidCacheEntry() {

        cacheEntry.setContext(context);
        try (MockedStatic<SessionContextCache> sessionContextCache = mockStatic(SessionContextCache.class)) {
            sessionContextCache.when(SessionContextCache::getInstance).thenReturn(mockedSessionContextCache);
            when(mockedSessionContextCache.getValueFromCache(cacheKey, "abc")).thenReturn(cacheEntry);

            SessionContext sessionContext = FrameworkUtils.getSessionContextFromCache(DUMMY_CACHE_KEY, "abc");
            assertEquals(sessionContext, context);
        }
    }

    @Test
    public void testGetSessionContextFromCacheExpiredSession() throws FrameworkException {

        cacheEntry.setContext(context);
        try (MockedStatic<SessionContextCache> sessionContextCache = mockStatic(SessionContextCache.class)) {
            sessionContextCache.when(SessionContextCache::getInstance).thenReturn(mockedSessionContextCache);
            IdentityEventService identityEventService = new IdentityEventServiceImpl(Collections.EMPTY_LIST, 1);

            FrameworkServiceDataHolder.getInstance().setIdentityEventService(identityEventService);
            AuthenticationContext authenticationContext = new AuthenticationContext();
            SessionContext sessionContext = FrameworkUtils.getSessionContextFromCache(request,
                    authenticationContext, DUMMY_CACHE_KEY);
            assertNull(sessionContext);
        }
    }

    @Test
    public void testGetSessionContextFromCacheNotExpiredSession() throws FrameworkException {

        cacheEntry.setContext(context);
        try (MockedStatic<SessionContextCache> sessionContextCache = mockStatic(SessionContextCache.class)) {
            sessionContextCache.when(SessionContextCache::getInstance).thenReturn(mockedSessionContextCache);
            when(mockedSessionContextCache.getSessionContextCacheEntry(cacheKey, "abc")).thenReturn(cacheEntry);
            when(mockedSessionContextCache.isSessionExpired(any(SessionContextCacheKey.class),
                    any(SessionContextCacheEntry.class))).thenReturn(false);
            SessionContext sessionContext = FrameworkUtils.getSessionContextFromCache(request,
                    authenticationContext, DUMMY_CACHE_KEY);
            assertEquals(sessionContext, context);
        }
    }

    @DataProvider(name = "idpRoleClaimUriProvider")
    public Object[][] getIdpRoleClaimUriData() {

        return new Object[][]{
                {"IDP_ROLE_CLAIM", "IDP_ROLE_CLAIM"},
                {"", getLocalGroupsClaimURI()},
                {null, getLocalGroupsClaimURI()}
        };
    }

    /*
     Get User Role Claim URI from IDP Mapped Role Claim URI
     */
    @Test(dataProvider = "idpRoleClaimUriProvider")
    public void testGetIdpRoleClaimUri(String idpRoleClaimUri,
                                       String expectedRoleClaimUri) throws Exception {

        ExternalIdPConfig externalIdPConfig = mock(ExternalIdPConfig.class);
        when(externalIdPConfig.getRoleClaimUri()).thenReturn(idpRoleClaimUri);
        assertEquals(FrameworkUtils.getIdpRoleClaimUri(externalIdPConfig), expectedRoleClaimUri);
    }

    @DataProvider(name = "idpClaimMappingProvider")
    public Object[][] getIdpClaimMappingsProvider() {

        try (MockedStatic<IdentityUtil> identityUtil = mockStatic(IdentityUtil.class)) {
            identityUtil.when(IdentityUtil::getLocalGroupsClaimURI).thenReturn(UserCoreConstants.ROLE_CLAIM);
            return new Object[][]{
                    {       // SP mapped role claim
                            new ClaimMapping[]{
                                    ClaimMapping.build(getLocalGroupsClaimURI(), "IDP_ROLE_CLAIM", "", true)
                            },
                            "IDP_ROLE_CLAIM"
                    },
                    {       // Role claim not among SP mapped claims
                            new ClaimMapping[]{
                                    ClaimMapping.build("LOCAL_CLAIM", "IDP_CLAIM", "", true)
                            },
                            null
                    },
                    {       // Role claim among claim mappings but remote claim is null
                            new ClaimMapping[]{
                                    ClaimMapping.build(getLocalGroupsClaimURI(), null, null, true)
                            },
                            null
                    },
                    {      // No IDP mapped claims
                            new ClaimMapping[0], UserCoreConstants.ROLE_CLAIM
                    },
                    {
                            null, UserCoreConstants.ROLE_CLAIM
                    }
            };
        }
    }

    @Test(dataProvider = "idpClaimMappingProvider")
    public void testGetIdpRoleClaimUriFromClaimMappings(Object claimMappings,
                                                        String expectedRoleClaimUri) throws Exception {

        try (MockedStatic<IdentityUtil> identityUtil = mockStatic(IdentityUtil.class)) {
            identityUtil.when(IdentityUtil::getLocalGroupsClaimURI).thenReturn(UserCoreConstants.ROLE_CLAIM);
            ExternalIdPConfig externalIdPConfig = mock(ExternalIdPConfig.class);
            when(externalIdPConfig.getClaimMappings()).thenReturn((ClaimMapping[]) claimMappings);

            String roleClaim = FrameworkUtils.getIdpRoleClaimUri(externalIdPConfig);
            assertEquals(roleClaim, expectedRoleClaimUri);
        }
    }

    @Test
    public void testGetUserIdClaimURI() throws Exception {

        when(mockedIdentityProviderManager.getIdPByName("testIdp", "testTenant"))
                .thenReturn(mockedIdentityProvider);
        when(mockedIdentityProvider.getClaimConfig()).thenReturn(mockedClaimConfig);

        when(mockedClaimConfig.getUserClaimURI()).thenReturn("http://wso2.org/claims/username");
        when(mockedClaimConfig.getClaimMappings()).thenReturn(new ClaimMapping[]{mockedClaimMapping});

        try (MockedStatic<FrameworkServiceDataHolder> mockedFrameworkService =
                     mockStatic(FrameworkServiceDataHolder.class)) {
            FrameworkServiceDataHolder frameworkServiceDataHolder = mock(FrameworkServiceDataHolder.class);
            when(frameworkServiceDataHolder.getIdentityProviderManager()).thenReturn(mockedIdentityProviderManager);
            mockedFrameworkService.when(FrameworkServiceDataHolder::getInstance).thenReturn(frameworkServiceDataHolder);

            String result = FrameworkUtils.getUserIdClaimURI("testIdp", "testTenant");
            assertEquals(result, "http://wso2.org/claims/username");
        }
    }

    @Test(description = "Verify that the username auto-fill configuration is retrieved correctly")
    public void testGetUsernameFieldAutofillWithSubjectAttrConfig() {

        try (MockedStatic<IdentityUtil> identityUtilMockedStatic = mockStatic(IdentityUtil.class)) {
            identityUtilMockedStatic.when(
                            () -> IdentityUtil.getProperty(
                                    eq("JITProvisioning.AutofillUsernameFieldWithSubjectAttribute")))
                    .thenReturn("true");
            assertTrue(FrameworkUtils.isUsernameFieldAutofillWithSubjectAttr());
            identityUtilMockedStatic.when(
                            () -> IdentityUtil.getProperty(
                                    eq("JITProvisioning.AutofillUsernameFieldWithSubjectAttribute")))
                    .thenReturn("false");
            assertFalse(FrameworkUtils.isUsernameFieldAutofillWithSubjectAttr());
        }
    }

    @Test
    public void testGetAppAssociatedRolesFromFederatedUserAttributesValidAttributes() throws Exception {

        Map<String, String> fedUserAttributes = new HashMap<>();
        fedUserAttributes.put("idpGroupAttribute", "idpGroup1,idpGroup2");
        fedUserAttributes.put("testClaim", "abc");

        String applicationId = "testAppId";
        String idpGroupClaimURI = "testIdPGroupAttribute";
        String[] associatedRoles = new String[]{"role1", "role2"};

        ApplicationRolesResolver appRolesResolver = mock(ApplicationRolesResolver.class);
        when(appRolesResolver.getAppAssociatedRolesOfFederatedUser(any(), any(), eq(applicationId),
                eq(idpGroupClaimURI), eq(DUMMY_TENANT_DOMAIN)))
                .thenReturn(associatedRoles);
        FrameworkServiceDataHolder.getInstance().addApplicationRolesResolver(appRolesResolver);

        List<String> roles = FrameworkUtils.getAppAssociatedRolesFromFederatedUserAttributes(fedUserAttributes,
                mockedIdentityProvider, applicationId, idpGroupClaimURI, DUMMY_TENANT_DOMAIN);

        assertEquals(roles, Arrays.asList(associatedRoles));
    }

    @Test
    public void testGetEffectiveIdpGroupClaimUriDefaultBehaviourForCustomGroupClaimMapping() {

        when(mockedIdentityProvider.getClaimConfig()).thenReturn(mockedClaimConfig);
        ClaimMapping[] claimMappings = new ClaimMapping[3];
        claimMappings[0] = ClaimMapping.build(USERNAME_CLAIM, "idpUsernameAttribute", null, false);
        claimMappings[1] = ClaimMapping.build(GROUPS_CLAIM, "idpGroupAttribute", null, false);
        claimMappings[2] = ClaimMapping.build(ROLES_CLAIM, "idpRoleAttribute", null, false);
        when(mockedClaimConfig.getClaimMappings()).thenReturn(claimMappings);

        try (MockedStatic<IdentityUtil> identityUtilMockedStatic = mockStatic(IdentityUtil.class)) {
            identityUtilMockedStatic.when(() -> IdentityUtil.getProperty(USE_IDP_ROLE_CLAIM_AS_IDP_GROUP_CLAIM))
                    .thenReturn(String.valueOf(false));

            String result = FrameworkUtils.getEffectiveIdpGroupClaimUri(mockedIdentityProvider, DUMMY_TENANT_DOMAIN);

            assertEquals(result, "idpGroupAttribute");
        }
    }

    @Test
    public void testGetEffectiveIdpGroupClaimUriLegacyBehaviourForCustomGroupClaimMapping() {

        when(mockedIdentityProvider.getClaimConfig()).thenReturn(mockedClaimConfig);
        ClaimMapping[] claimMappings = new ClaimMapping[3];
        claimMappings[0] = ClaimMapping.build(USERNAME_CLAIM, "idpUsernameAttribute", null, false);
        claimMappings[1] = ClaimMapping.build(GROUPS_CLAIM, "idpGroupAttribute", null, false);
        claimMappings[2] = ClaimMapping.build(ROLES_CLAIM, "idpRoleAttribute", null, false);
        when(mockedClaimConfig.getClaimMappings()).thenReturn(claimMappings);

        try (MockedStatic<IdentityUtil> identityUtilMockedStatic = mockStatic(IdentityUtil.class)) {
            identityUtilMockedStatic.when(() -> IdentityUtil.getProperty(USE_IDP_ROLE_CLAIM_AS_IDP_GROUP_CLAIM))
                    .thenReturn(String.valueOf(true));
            identityUtilMockedStatic.when(IdentityUtil::getLocalGroupsClaimURI)
                    .thenReturn(UserCoreConstants.INTERNAL_ROLES_CLAIM);

            String result = FrameworkUtils.getEffectiveIdpGroupClaimUri(mockedIdentityProvider, DUMMY_TENANT_DOMAIN);

            assertEquals(result, "idpRoleAttribute");
        }
    }

    @Test
    public void testGetEffectiveIdpGroupClaimUriDefaultBehaviourWithNoCustomClaimMappings() throws Exception {

        when(mockedIdentityProvider.getClaimConfig()).thenReturn(mockedClaimConfig);
        when(mockedClaimConfig.getClaimMappings()).thenReturn(null);
        when(mockedClaimConfig.isLocalClaimDialect()).thenReturn(true);
        when(mockedIdentityProvider.getDefaultAuthenticatorConfig()).thenReturn(mockedFederatedAuthenticatorConfig);
        when(mockedFederatedAuthenticatorConfig.getName()).thenReturn("FederatedAuthenticator");

        try (MockedStatic<IdentityUtil> identityUtilMockedStatic = mockStatic(IdentityUtil.class);
             MockedStatic<ClaimMetadataHandler> claimMetadataHandlerMockedStatic = mockStatic(
                     ClaimMetadataHandler.class)) {
            identityUtilMockedStatic.when(() -> IdentityUtil.getProperty(USE_IDP_ROLE_CLAIM_AS_IDP_GROUP_CLAIM))
                    .thenReturn(String.valueOf(false));

            claimMetadataHandlerMockedStatic.when(() -> ClaimMetadataHandler.getInstance())
                    .thenReturn(mockedClaimMetadataHandler);
            Map<String, String> otherClaimDialectToCarbonMapping = new HashMap<>();
            otherClaimDialectToCarbonMapping.put("groupsClaimInDialect", "http://wso2.org/claims/groups");
            when(mockedClaimMetadataHandler.getMappingsMapFromOtherDialectToCarbon(anyString(),
                    isNull(), anyString(), anyBoolean())).thenReturn(otherClaimDialectToCarbonMapping);

            String result = FrameworkUtils.getEffectiveIdpGroupClaimUri(mockedIdentityProvider, DUMMY_TENANT_DOMAIN);

            assertEquals(result, "groupsClaimInDialect");
        }
    }

    @Test
    public void testGetEffectiveIdpGroupClaimUriLegacyBehaviourWithNoCustomClaimMappings() throws Exception {

        when(mockedIdentityProvider.getClaimConfig()).thenReturn(mockedClaimConfig);
        when(mockedClaimConfig.getClaimMappings()).thenReturn(new ClaimMapping[0]);
        when(mockedClaimConfig.isLocalClaimDialect()).thenReturn(true);
        when(mockedIdentityProvider.getDefaultAuthenticatorConfig()).thenReturn(mockedFederatedAuthenticatorConfig);
        when(mockedFederatedAuthenticatorConfig.getName()).thenReturn("FederatedAuthenticator");

        try (MockedStatic<IdentityUtil> identityUtilMockedStatic = mockStatic(IdentityUtil.class);
             MockedStatic<ClaimMetadataHandler> claimMetadataHandlerMockedStatic = mockStatic(
                     ClaimMetadataHandler.class)) {
            identityUtilMockedStatic.when(() -> IdentityUtil.getProperty(USE_IDP_ROLE_CLAIM_AS_IDP_GROUP_CLAIM))
                    .thenReturn(String.valueOf(true));
            identityUtilMockedStatic.when(IdentityUtil::getLocalGroupsClaimURI)
                    .thenReturn(UserCoreConstants.INTERNAL_ROLES_CLAIM);

            claimMetadataHandlerMockedStatic.when(() -> ClaimMetadataHandler.getInstance())
                    .thenReturn(mockedClaimMetadataHandler);
            Map<String, String> otherClaimDialectToCarbonMapping = new HashMap<>();
            otherClaimDialectToCarbonMapping.put("rolesClaimInDialect", "http://wso2.org/claims/roles");
            when(mockedClaimMetadataHandler.getMappingsMapFromOtherDialectToCarbon(anyString(),
                    isNull(), anyString(), anyBoolean())).thenReturn(otherClaimDialectToCarbonMapping);

            String result = FrameworkUtils.getEffectiveIdpGroupClaimUri(mockedIdentityProvider, DUMMY_TENANT_DOMAIN);

            assertEquals(result, "rolesClaimInDialect");
        }
    }

    @Test
    public void testGetClaimValue() throws Exception {

        String username = "DOMAIN/user";
        String claimURI = "http://wso2.org/claims/emailaddress";
        String expectedValue = "user@example.com";

        UserStoreManager mockUserStoreManager = Mockito.mock(UserStoreManager.class);
        Map<String, String> claims = new HashMap<>();
        claims.put(claimURI, expectedValue);
        Mockito.when(mockUserStoreManager.getUserClaimValues(
                Mockito.eq(username),
                Mockito.eq(new String[]{claimURI}),
                Mockito.eq(UserCoreConstants.DEFAULT_PROFILE))).thenReturn(claims);

        Method method = FrameworkUtils.class.getDeclaredMethod("getClaimValue", String.class,
                UserStoreManager.class, String.class);
        method.setAccessible(true);
        String result = (String) method.invoke(null, username, mockUserStoreManager, claimURI);
        assertEquals(result, expectedValue);
    }

    @DataProvider(name = "dataProviderGetImpersonatedUser")
    public Object[][] dataProviderGetImpersonatedUser() {

        // tenantDomain, userAccessingOrg, userResidentOrg, isSubOrgUser
        return new Object[][]{
                {"tenant1.com", "org1", "org1", true},
                {"tenant1.com", null, null, false},
        };
    }

    @Test(dataProvider = "dataProviderGetImpersonatedUser")
    public void testGetImpersonatedUser(String tenantDomain, String userAccessingOrg, String userResidentOrg,
                                        boolean isSubOrgUser)
            throws UserStoreException, FrameworkException, OrganizationManagementException {

        String userId = "user-123";
        String subOrgHandle = "org1.com";

        // User object.
        User user = new User(
        "user-123",
        "dummyUser",
        "dummyUser",
        "dummyUser",
        "dummyTenantDomain",
        "DUMMYDOMAIN",
        null
        );

        RealmService realmService = mock(RealmService.class);
        FrameworkServiceDataHolder.getInstance().setRealmService(realmService);
        OrganizationManager organizationManager = mock(OrganizationManager.class);
        FrameworkServiceDataHolder.getInstance().setOrganizationManager(organizationManager);
        lenient().when(organizationManager.resolveTenantDomain(anyString())).thenReturn(subOrgHandle);
        TenantManager tenantManager = mock(TenantManager.class);
        when(realmService.getTenantManager()).thenReturn(tenantManager);
        when(tenantManager.getTenantId(anyString())).thenReturn(1);
        UserRealm userRealm = mock(UserRealm.class);
        when(realmService.getTenantUserRealm(anyInt())).thenReturn(userRealm);
        AbstractUserStoreManager userStoreManager = mock(AbstractUserStoreManager.class);
        when(userRealm.getUserStoreManager()).thenReturn(userStoreManager);
        when(userStoreManager.getUser(userId, null)).thenReturn(user);
        when(userStoreManager.isExistingUserWithID(anyString())).thenReturn(true);

        // Create Application config.
        ApplicationConfig applicationConfig = mock(ApplicationConfig.class);
        when(applicationConfig.getSubjectClaimUri()).thenReturn(null);
        when(applicationConfig.isUseUserstoreDomainInLocalSubjectIdentifier()).thenReturn(false);
        when(applicationConfig.isUseTenantDomainInLocalSubjectIdentifier()).thenReturn(false);

        ImpersonatedUser impersonatedUser = FrameworkUtils.getImpersonatedUser(userId, tenantDomain,
                userAccessingOrg, userResidentOrg, applicationConfig);

        assertNotNull(impersonatedUser);
        assertEquals(impersonatedUser.isFederatedUser(), isSubOrgUser);
        assertEquals(impersonatedUser.getAuthenticatedSubjectIdentifier(), userId);
    }

    @DataProvider
    public Object[][] dataProviderGetSubjectIdentifier() {

        // userDomain, tenantDomain, isFederatedUser, federatedIdp, useUserStoreDomainInLocalSubjectIdentifier,
        // useTenantDomainInLocalSubjectIdentifier, subjectClaimUri, subjectClaimUriValue, expectedSubjectClaimUri
        return new Object[][]{
                {"DUMMYDOMAIN", "dummyTenantDomain", false, "LOCAL", true, true, "http://wso2.org/claims/emailaddress",
                        "dummyUser@email.com", "DUMMYDOMAIN/dummyUser@email.com@dummyTenantDomain"},
                {"DUMMYDOMAIN", "dummyTenantDomain", true, "LOCAL", true, true, "http://wso2.org/claims/emailaddress",
                        "dummyUser@email.com", "DUMMYDOMAIN/dummyUser@email.com@dummyTenantDomain"},
                {"DUMMYDOMAIN", "dummyTenantDomain", true, "SSO", true, true, "http://wso2.org/claims/emailaddress",
                        "dummyUser@email.com", "dummyUser@email.com@dummyTenantDomain"},
        };
    }

    @Test(dataProvider = "dataProviderGetSubjectIdentifier")
    public void testGetSubjectIdentifier(String userDomain, String tenantDomain, boolean isFederatedUser,
                                         String federatedIdp, boolean useUserStoreDomainInLocalSubjectIdentifier,
                                         boolean useTenantDomainInLocalSubjectIdentifier, String subjectClaimUri,
                                         String subjectClaimUriValue, String expectedSubjectClaimUri)
            throws UserStoreException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {

        // Create impersonated user.
        String userName = "dummyUser";
        ImpersonatedUser impersonatedUser = new ImpersonatedUser();
        impersonatedUser.setUserId("user-123");
        impersonatedUser.setUserName(userName);
        impersonatedUser.setUserStoreDomain(userDomain);
        impersonatedUser.setTenantDomain(tenantDomain);
        impersonatedUser.setFederatedUser(isFederatedUser);
        impersonatedUser.setFederatedIdPName(federatedIdp);

        // Create Application config.
        ApplicationConfig applicationConfig = mock(ApplicationConfig.class);
        when(applicationConfig.getSubjectClaimUri()).thenReturn(subjectClaimUri);
        when(applicationConfig.isUseUserstoreDomainInLocalSubjectIdentifier()).thenReturn(
                useUserStoreDomainInLocalSubjectIdentifier);
        when(applicationConfig.isUseTenantDomainInLocalSubjectIdentifier()).thenReturn(
                useTenantDomainInLocalSubjectIdentifier);

        try (MockedStatic<IdentityUtil> identityUtilMockedStatic = mockStatic(IdentityUtil.class);
            MockedStatic<IdentityTenantUtil> identityTenantUtilMockedStatic = mockStatic(IdentityTenantUtil.class)) {

            // Mock IdentityUtil methods.
            identityUtilMockedStatic.when(() -> IdentityUtil.addDomainToName(subjectClaimUriValue, userDomain))
                    .thenReturn(userDomain + "/" + subjectClaimUriValue);
            identityUtilMockedStatic.when(() -> IdentityUtil.addDomainToName(userName, userDomain))
                    .thenReturn(userDomain + "/" + userName);
            identityTenantUtilMockedStatic.when(() -> IdentityTenantUtil.getTenantId(tenantDomain)).thenReturn(1);

            // Mock RealmService and UserStoreManager.
            RealmService realmService = mock(RealmService.class);
            UserRealm userRealm = mock(UserRealm.class);
            UserStoreManager userStoreManager = mock(UserStoreManager.class);
            FrameworkServiceDataHolder.getInstance().setRealmService(realmService);
            when(realmService.getTenantUserRealm(anyInt())).thenReturn(userRealm);
            when(userRealm.getUserStoreManager()).thenReturn(userStoreManager);
            Map<String, String> claims = new HashMap<>();
            claims.put(subjectClaimUri, subjectClaimUriValue);
            when(userStoreManager.getUserClaimValues(
                    userDomain + "/" + userName,
                    new String[]{subjectClaimUri},
                    UserCoreConstants.DEFAULT_PROFILE)).thenReturn(claims);

            // Invoke method.
            Method method = FrameworkUtils.class.getDeclaredMethod("getSubjectIdentifier",
                    ImpersonatedUser.class, ApplicationConfig.class);
            method.setAccessible(true);
            String result = (String) method.invoke(null, impersonatedUser, applicationConfig);
            assertEquals(result, expectedSubjectClaimUri);
        }
    }

    @Test
    public void getAppAuthenticatorByNameExistingAuthenticator() {

        Assert.assertNotNull(FrameworkUtils.getAppAuthenticatorByName("BasicAuthenticator"));
    }

    @Test
    public void getAppAuthenticatorByNameNonExistingAuthenticator() {

        Assert.assertNull(FrameworkUtils.getAppAuthenticatorByName("NonExistAuthenticator"));
    }

    @Test
    public void testGetOrganizationDiscoveryInput() {

        Mockito.when(request.getParameter(ORG_ID)).thenReturn("org123");
        Mockito.when(request.getParameter(ORG_HANDLE)).thenReturn("orgHandle123");
        Mockito.when(request.getParameter(ORG_NAME)).thenReturn("Organization Name");
        Mockito.when(request.getParameter(LOGIN_HINT)).thenReturn("loginHint123");
        Mockito.when(request.getParameter(ORG_DISCOVERY_TYPE)).thenReturn("discoveryType123");

        // Call the method.
        OrganizationDiscoveryInput input = FrameworkUtils.getOrganizationDiscoveryInput(request);

        assertEquals(input.getOrgId(), "org123");
        assertEquals(input.getOrgHandle(), "orgHandle123");
        assertEquals(input.getOrgName(), "Organization Name");
        assertEquals(input.getLoginHint(), "loginHint123");
        assertEquals(input.getOrgDiscoveryType(), "discoveryType123");
    }

    private void removeAllSystemDefinedAuthenticators() {

        List<ApplicationAuthenticator> authenticatorList = new ArrayList<>(
                ApplicationAuthenticatorManager.getInstance().getSystemDefinedAuthenticators());
        for (ApplicationAuthenticator authenticator : authenticatorList) {
            ApplicationAuthenticatorManager.getInstance().removeSystemDefinedAuthenticator(authenticator);
        }
    }

    @DataProvider(name = "serviceProviderVersionProvider")
    public Object[][] serviceProviderVersionProvider() {

        return new Object[][]{
                // appVersion, expectedResult
                {"v3.0.0", true},           // v3 version should return true
                {"v4.0.0", true},           // v4+ version should return true
                {"v2.0.0", false},          // v2.x version should return false
        };
    }

    @Test(dataProvider = "serviceProviderVersionProvider")
    public void testIsLoginFailureWithNoLocalAssociationEnabledForApp(String appVersion, boolean expectedResult) {

        // Create a mock ServiceProvider
        ServiceProvider serviceProvider = mock(ServiceProvider.class);
        when(serviceProvider.getApplicationVersion()).thenReturn(appVersion);

        // Call the method under test
        boolean result = FrameworkUtils.isLoginFailureWithNoLocalAssociationEnabledForApp(serviceProvider);

        // Assert the result
        assertEquals(result, expectedResult);
    }

    @DataProvider(name = "userAssertionCases")
    public Object[][] userAssertionCases() {
        return new Object[][]{

                {"ctx-jwt", "req-jwt", true, "Context non-null -> true; request ignored"},
                {null, "req-jwt", true, "Context null -> use request non-empty"},
                {null, null, false, "Both null -> false"},
                {"", "req-jwt", false, "Context empty string -> dominates -> false"},
                {" ", null, true, "Whitespace is NOT empty -> true with isNotEmpty"},
                {null, "", false, "Request empty string -> false"},
        };
    }

    @Test(dataProvider = "userAssertionCases",
            description = "Verifies precedence (context over request), null/empty handling, and toString() conversion.")
    public void testContextHasUserAssertion(Object contextProp, String requestParam, boolean expected, String note) {

        HttpServletRequest request = mock(HttpServletRequest.class);
        AuthenticationContext context = mock(AuthenticationContext.class);

        when(context.getProperty(FrameworkConstants.USER_ASSERTION)).thenReturn(contextProp);
        if (contextProp == null) {
            when(request.getParameter(FrameworkConstants.USER_ASSERTION)).thenReturn(requestParam);
        }

        boolean actual = FrameworkUtils.contextHasUserAssertion(request, context);
        assertEquals(actual, expected, note);
    }

    /**
     * Test multi attribute separator retrieval from user realm configuration.
     */
    @Test
    public void testGetMultiAttributeSeparatorFromUserRealmConfig() {

        final String multiAttributeSeparator = ",";
        try (MockedStatic<CarbonContext> carbonContextMockedStatic = mockStatic(CarbonContext.class)) {
            CarbonContext carbonContext = mock(CarbonContext.class);
            UserRealm userRealm = mock(UserRealm.class);
            AbstractUserStoreManager primaryUserStoreManager = mock(AbstractUserStoreManager.class);
            RealmConfiguration realmConfiguration = mock(RealmConfiguration.class);

            carbonContextMockedStatic.when(CarbonContext::getThreadLocalCarbonContext).thenReturn(carbonContext);
            when(carbonContext.getUserRealm()).thenReturn(userRealm);
            try {
                when(userRealm.getUserStoreManager()).thenReturn(primaryUserStoreManager);
            } catch (UserStoreException e) {
                throw new RuntimeException("Unexpected UserStoreException in test setup.", e);
            }
            when(primaryUserStoreManager.getRealmConfiguration()).thenReturn(realmConfiguration);
            when(realmConfiguration.getUserStoreProperty(IdentityCoreConstants.MULTI_ATTRIBUTE_SEPARATOR))
                    .thenReturn(multiAttributeSeparator);

            String separator = FrameworkUtils.getMultiAttributeSeparator();
            assertEquals(separator, multiAttributeSeparator);
        }
    }

    /**
     * Test multi attribute separator retrieval for a specific user store domain.
     */
    @Test
    public void testGetMultiAttributeSeparatorWithUserStoreDomain() {

        String userStoreDomain = "SECONDARY";
        final String multiAttributeSeparator = ";";

        try (MockedStatic<CarbonContext> carbonContextMockedStatic = mockStatic(CarbonContext.class)) {
            CarbonContext carbonContext = mock(CarbonContext.class);
            UserRealm userRealm = mock(UserRealm.class);
            AbstractUserStoreManager primaryUserStoreManager = mock(AbstractUserStoreManager.class);
            AbstractUserStoreManager secondaryUserStoreManager = mock(AbstractUserStoreManager.class);
            RealmConfiguration realmConfiguration = mock(RealmConfiguration.class);

            carbonContextMockedStatic.when(CarbonContext::getThreadLocalCarbonContext).thenReturn(carbonContext);
            when(carbonContext.getUserRealm()).thenReturn(userRealm);
            try {
                when(userRealm.getUserStoreManager()).thenReturn(primaryUserStoreManager);
            } catch (UserStoreException e) {
                throw new RuntimeException("Unexpected UserStoreException in test setup.", e);
            }
            when(primaryUserStoreManager.getSecondaryUserStoreManager(userStoreDomain))
                    .thenReturn(secondaryUserStoreManager);
            when(secondaryUserStoreManager.getRealmConfiguration()).thenReturn(realmConfiguration);
            when(realmConfiguration.getUserStoreProperty(IdentityCoreConstants.MULTI_ATTRIBUTE_SEPARATOR))
                    .thenReturn(multiAttributeSeparator);

            String separator = FrameworkUtils.getMultiAttributeSeparator(userStoreDomain);
            assertEquals(separator, multiAttributeSeparator);
        }
    }

    @Test
    public void testPreprocessUsernameWithContextTenantDomainReturnsOriginalForLegacySaaSApp() {

        AuthenticationContext context = buildAuthenticationContext(true, "app.com", "user.com");

        try (MockedStatic<IdentityUtil> identityUtilMock = mockStatic(IdentityUtil.class);
             MockedStatic<IdentityTenantUtil> identityTenantUtilMock = mockStatic(IdentityTenantUtil.class)) {

            identityUtilMock.when(IdentityUtil::isEmailUsernameEnabled).thenReturn(false);
            identityTenantUtilMock.when(IdentityTenantUtil::isLegacySaaSAuthenticationEnabled).thenReturn(true);
            identityTenantUtilMock.when(IdentityTenantUtil::isTenantedSessionsEnabled).thenReturn(true);

            String processedUsername = FrameworkUtils.preprocessUsernameWithContextTenantDomain("alice", context);

            assertEquals(processedUsername, "alice");
        }
    }

    @Test
    public void testPreprocessUsernameWithContextTenantDomainWhenEmailUsernameEnabled() {

        AuthenticationContext context = buildAuthenticationContext(false, "app.com", "user.com");

        try (MockedStatic<IdentityUtil> identityUtilMock = mockStatic(IdentityUtil.class);
             MockedStatic<IdentityTenantUtil> identityTenantUtilMock = mockStatic(IdentityTenantUtil.class)) {

            identityUtilMock.when(IdentityUtil::isEmailUsernameEnabled).thenReturn(true);
            identityTenantUtilMock.when(IdentityTenantUtil::isLegacySaaSAuthenticationEnabled).thenReturn(false);
            identityTenantUtilMock.when(IdentityTenantUtil::isTenantedSessionsEnabled).thenReturn(true);

            String processedUsername = FrameworkUtils
                    .preprocessUsernameWithContextTenantDomain("alice@example.com", context);

            assertEquals(processedUsername, "alice@example.com@user.com");
        }
    }

    @Test
    public void testPreprocessUsernameWithContextTenantDomainAppendsContextTenantForNonSaaS() {

        AuthenticationContext context = buildAuthenticationContext(false, "app.com", "user.com");

        try (MockedStatic<IdentityUtil> identityUtilMock = mockStatic(IdentityUtil.class);
             MockedStatic<IdentityTenantUtil> identityTenantUtilMock = mockStatic(IdentityTenantUtil.class)) {

            identityUtilMock.when(IdentityUtil::isEmailUsernameEnabled).thenReturn(false);
            identityTenantUtilMock.when(IdentityTenantUtil::isLegacySaaSAuthenticationEnabled).thenReturn(false);
            identityTenantUtilMock.when(IdentityTenantUtil::isTenantedSessionsEnabled).thenReturn(true);

            String processedUsername = FrameworkUtils
                    .preprocessUsernameWithContextTenantDomain("alice", context);

            assertEquals(processedUsername, "alice@app.com");
        }
    }

    @Test
    public void testPreprocessUsernameWithContextTenantDomainReturnsOriginalForSaaSEmailUser() {

        AuthenticationContext context = buildAuthenticationContext(true, "app.com", "user.com");

        try (MockedStatic<IdentityUtil> identityUtilMock = mockStatic(IdentityUtil.class);
             MockedStatic<IdentityTenantUtil> identityTenantUtilMock = mockStatic(IdentityTenantUtil.class)) {

            identityUtilMock.when(IdentityUtil::isEmailUsernameEnabled).thenReturn(false);
            identityTenantUtilMock.when(IdentityTenantUtil::isLegacySaaSAuthenticationEnabled).thenReturn(false);
            identityTenantUtilMock.when(IdentityTenantUtil::isTenantedSessionsEnabled).thenReturn(true);

            String processedUsername = FrameworkUtils
                    .preprocessUsernameWithContextTenantDomain("alice@example.com", context);

            assertEquals(processedUsername, "alice@example.com");
        }
    }

    private AuthenticationContext buildAuthenticationContext(boolean isSaaSApp, String tenantDomain,
                                                             String userTenantDomain) {

        AuthenticationContext context = new AuthenticationContext();
        context.setTenantDomain(tenantDomain);
        context.setLoginTenantDomain(userTenantDomain);
        context.setUserTenantDomainHint(userTenantDomain);

        SequenceConfig sequenceConfig = new SequenceConfig();
        ApplicationConfig applicationConfig = mock(ApplicationConfig.class);
        when(applicationConfig.isSaaSApp()).thenReturn(isSaaSApp);
        sequenceConfig.setApplicationConfig(applicationConfig);
        context.setSequenceConfig(sequenceConfig);

        return context;
    }

    @DataProvider(name = "preprocessUsernameDataProvider")
    public Object[][] providePreprocessUsernameData() {

        return new Object[][]{
                // {username, userTenantDomain, isSaaSApp, isLegacySaaSEnabled, isEmailUsernameEnabled, expectedOutput}
                // Legacy SaaS enabled - should return username as-is.
                {"alice", "user.com", true, true, false, "alice"},
                {"alice@example.com", "user.com", true, true, false, "alice@example.com"},
                {"alice@example.com", "user.com", true, true, true, "alice@example.com"},
                {"alice", "user.com", true, true, true, "alice"},

                // Email username enabled - single @ in username - should append tenant domain.
                {"alice@example.com", "user.com", false, false, true, "alice@example.com@user.com"},
                {"alice@example.com", "user.com", true, false, true, "alice@example.com@user.com"},

                // Email username enabled - multiple @ in username - should return as-is.
                {"alice@example.com@domain.com", "user.com", false, false, true, "alice@example.com@domain.com"},
                {"alice@example.com@domain.com", "user.com", true, false, true, "alice@example.com@domain.com"},

                // Email username disabled - username already ends with tenant domain.
                {"alice@user.com", "user.com", false, false, false, "alice@user.com"},
                {"alice@user.com", "user.com", true, false, false, "alice@user.com"},

                // Email username disabled - non-SaaS app - should append tenant domain.
                {"alice", "user.com", false, false, false, "alice@user.com"},
                {"bob", "tenant.org", false, false, false, "bob@tenant.org"},

                // Email username disabled - SaaS app with username containing multiple @.
                {"alice@example.com@domain.com", "user.com", true, false, false, "alice@example.com@domain.com"},

                // Email username disabled - SaaS app with non-email username - should append tenant domain.
                {"alice", "user.com", true, false, false, "alice@user.com"},
                {"bob", "tenant.org", true, false, false, "bob@tenant.org"},

                // email username disabled - SaaS app with username containing single @ that doesn't end
                // with tenant domain
                {"alice@example.com", "user.com", true, false, false, "alice@example.com"}
        };
    }

    @Test(dataProvider = "preprocessUsernameDataProvider")
    public void testPreprocessUsername(String username, String userTenantDomain, boolean isSaaSApp,
                                        boolean isLegacySaaSEnabled, boolean isEmailUsernameEnabled,
                                        String expectedOutput) {

        try (MockedStatic<IdentityUtil> identityUtilMock = mockStatic(IdentityUtil.class);
             MockedStatic<IdentityTenantUtil> identityTenantUtilMock = mockStatic(IdentityTenantUtil.class)) {

            identityUtilMock.when(IdentityUtil::isEmailUsernameEnabled).thenReturn(isEmailUsernameEnabled);
            identityTenantUtilMock.when(IdentityTenantUtil::isLegacySaaSAuthenticationEnabled)
                    .thenReturn(isLegacySaaSEnabled);

            String processedUsername = FrameworkUtils.preprocessUsername(username, userTenantDomain, isSaaSApp);

            assertEquals(processedUsername, expectedOutput);
        }
    }
}
