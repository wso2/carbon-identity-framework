/*
 * Copyright (c) 2025, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.carbon.identity.user.registration.engine.util;

import org.apache.commons.lang.StringUtils;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.application.common.IdentityApplicationManagementException;
import org.wso2.carbon.identity.application.common.model.ApplicationBasicInfo;
import org.wso2.carbon.identity.application.mgt.ApplicationManagementService;
import org.wso2.carbon.identity.application.mgt.ApplicationMgtUtil;
import org.wso2.carbon.identity.base.IdentityRuntimeException;
import org.wso2.carbon.identity.common.testng.WithCarbonHome;
import org.wso2.carbon.identity.core.ServiceURL;
import org.wso2.carbon.identity.core.ServiceURLBuilder;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;
import org.wso2.carbon.identity.user.registration.engine.cache.RegistrationContextCache;
import org.wso2.carbon.identity.user.registration.engine.cache.RegistrationContextCacheEntry;
import org.wso2.carbon.identity.user.registration.engine.exception.RegistrationEngineException;
import org.wso2.carbon.identity.user.registration.engine.exception.RegistrationEngineServerException;
import org.wso2.carbon.identity.user.registration.engine.internal.RegistrationFlowEngineDataHolder;
import org.wso2.carbon.identity.user.registration.engine.model.RegistrationContext;
import org.wso2.carbon.identity.user.registration.mgt.RegistrationFlowMgtService;
import org.wso2.carbon.identity.user.registration.mgt.exception.RegistrationFrameworkException;
import org.wso2.carbon.identity.user.registration.mgt.model.RegistrationGraphConfig;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.testng.Assert.assertEquals;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertNotNull;
import static org.testng.AssertJUnit.fail;
import static org.wso2.carbon.identity.user.registration.engine.Constants.DEFAULT_REGISTRATION_CALLBACK;
import static org.wso2.carbon.identity.user.registration.engine.Constants.ErrorMessages.ERROR_CODE_GET_DEFAULT_REG_FLOW_FAILURE;
import static org.wso2.carbon.identity.user.registration.engine.Constants.ErrorMessages.ERROR_CODE_INVALID_FLOW_ID;
import static org.wso2.carbon.identity.user.registration.engine.Constants.ErrorMessages.ERROR_CODE_REG_FLOW_NOT_FOUND;
import static org.wso2.carbon.identity.user.registration.engine.Constants.ErrorMessages.ERROR_CODE_TENANT_RESOLVE_FAILURE;
import static org.wso2.carbon.identity.user.registration.engine.Constants.ErrorMessages.ERROR_CODE_UNDEFINED_FLOW_ID;

/**
 * Unit tests for RegistrationFlowEngineUtils.
 */
@WithCarbonHome
@Listeners(MockitoTestNGListener.class)
public class RegistrationFlowEngineUtilsTest {

    private static final String TENANT_DOMAIN = "carbon.super";
    private static final String TEST_CALLBACK_URL = "https://localhost:3000/myapp/callback";
    private static final String TEST_APP_URL = "https://localhost:3000/myapp";
    private static final String DEFAULT_MY_ACCOUNT_URL = "https://localhost:9443/myaccount";
    private static final int TENANT_ID = -1234;
    private RegistrationContext testContext;

    @Mock
    private RegistrationFlowMgtService mgtServiceMock;

    @Mock
    private RegistrationFlowEngineDataHolder dataHolderMock;

    @Mock
    private RegistrationContextCache regContextCacheMock;

    private MockedStatic<IdentityTenantUtil> identityTenantUtil;

    @BeforeClass
    public void setup() {

        MockitoAnnotations.openMocks(this);
        identityTenantUtil = mockStatic(IdentityTenantUtil.class);
        identityTenantUtil.when(() -> IdentityTenantUtil.getTenantId(TENANT_DOMAIN)).thenReturn(TENANT_ID);
    }

    @Test
    public void testInitContextTenantResolveFailure() {

        identityTenantUtil.when(() -> IdentityTenantUtil.getTenantId(TENANT_DOMAIN))
                .thenThrow(IdentityRuntimeException.class);
        try {
            RegistrationFlowEngineUtils.initiateContext(TENANT_DOMAIN, TEST_CALLBACK_URL, null);
        } catch (RegistrationEngineException e) {
            assertEquals(e.getErrorCode(), ERROR_CODE_TENANT_RESOLVE_FAILURE.getCode());
        }
    }

    @Test
    public void testInitContextGraphNotDefined() throws Exception {

        identityTenantUtil.when(() -> IdentityTenantUtil.getTenantId(TENANT_DOMAIN)).thenReturn(TENANT_ID);
        try (MockedStatic<RegistrationFlowEngineDataHolder> dataHolderMockedStatic = mockStatic(
                RegistrationFlowEngineDataHolder.class)) {
            dataHolderMockedStatic.when(RegistrationFlowEngineDataHolder::getInstance).thenReturn(dataHolderMock);
            when(dataHolderMock.getRegistrationFlowMgtService()).thenReturn(mgtServiceMock);
            when(mgtServiceMock.getRegistrationGraphConfig(TENANT_ID)).thenReturn(null);
            RegistrationFlowEngineUtils.initiateContext(TENANT_DOMAIN, TEST_CALLBACK_URL, null);
        } catch (RegistrationEngineException e) {
            assertEquals(e.getErrorCode(), ERROR_CODE_REG_FLOW_NOT_FOUND.getCode());
        }
    }

    @Test
    public void testRegistrationGraphRetrievalFailure() throws Exception {

        identityTenantUtil.when(() -> IdentityTenantUtil.getTenantId(TENANT_DOMAIN)).thenReturn(TENANT_ID);
        try (MockedStatic<RegistrationFlowEngineDataHolder> dataHolderMockedStatic = mockStatic(
                RegistrationFlowEngineDataHolder.class)) {

            dataHolderMockedStatic.when(RegistrationFlowEngineDataHolder::getInstance).thenReturn(dataHolderMock);
            when(dataHolderMock.getRegistrationFlowMgtService()).thenReturn(mgtServiceMock);
            when(mgtServiceMock.getRegistrationGraphConfig(TENANT_ID)).thenThrow(RegistrationFrameworkException.class);
            RegistrationFlowEngineUtils.initiateContext(TENANT_DOMAIN, TEST_CALLBACK_URL, null);
        } catch (RegistrationEngineException e) {
            assertEquals(e.getErrorCode(), ERROR_CODE_GET_DEFAULT_REG_FLOW_FAILURE.getCode());
        }
    }

    @Test
    public void testContextRetrievalWithEmptyId() {

        try {
            RegistrationFlowEngineUtils.retrieveRegContextFromCache(null);
        } catch (RegistrationEngineException e) {
            assertEquals(e.getErrorCode(), ERROR_CODE_UNDEFINED_FLOW_ID.getCode());
        }
    }

    @Test
    public void testContextRetrievalWithInvalidId() {

        try (MockedStatic<RegistrationContextCache> regContextCacheMockedStatic = mockStatic(
                RegistrationContextCache.class)) {
            regContextCacheMockedStatic.when(RegistrationContextCache::getInstance).thenReturn(regContextCacheMock);
            lenient().when(regContextCacheMock.getValueFromCache(any())).thenReturn(null);
            RegistrationFlowEngineUtils.retrieveRegContextFromCache("invalidFlowId");
        } catch (RegistrationEngineException e) {
            assertEquals(e.getErrorCode(), ERROR_CODE_INVALID_FLOW_ID.getCode());
        }
    }

    @DataProvider(name = "initiateContextScenarios")
    public Object[][] initiateContextScenarios() {

        String DEFAULT_MY_ACCOUNT_URL = "https://localhost:9443/myaccount";
        return new Object[][] {
                // applicationId, callbackUrl, expectedCallBackUrl
                {"test-app-id-1", TEST_CALLBACK_URL, TEST_CALLBACK_URL},
                {null, null, DEFAULT_MY_ACCOUNT_URL},
        };
    }

    @Test (dataProvider = "initiateContextScenarios")
    public void testRegContextInitiation(String appId, String callback, String expectedCallback) throws Exception {

        String firstNodeId = "testNode123";
        RegistrationGraphConfig graphConfig = new RegistrationGraphConfig();
        graphConfig.setFirstNodeId(firstNodeId);

        identityTenantUtil.when(() -> IdentityTenantUtil.getTenantId(TENANT_DOMAIN)).thenReturn(TENANT_ID);
        try (MockedStatic<RegistrationFlowEngineDataHolder> dataHolderMockedStatic = mockStatic(
                RegistrationFlowEngineDataHolder.class);
             MockedStatic<ServiceURLBuilder> serviceURLBuilderMockedStatic = mockStatic(
                     ServiceURLBuilder.class)) {

            if (StringUtils.isEmpty(callback)) {
                ServiceURLBuilder serviceURLBuilder = mock(ServiceURLBuilder.class);
                ServiceURL serviceURL = mock(ServiceURL.class);
                serviceURLBuilderMockedStatic.when(ServiceURLBuilder::create).thenReturn(serviceURLBuilder);
                when(serviceURLBuilder.addPath(DEFAULT_REGISTRATION_CALLBACK)).thenReturn(serviceURLBuilder);
                when(serviceURLBuilder.build()).thenReturn(serviceURL);
                when(serviceURL.getAbsolutePublicURL()).thenReturn(DEFAULT_MY_ACCOUNT_URL);
            }

            dataHolderMockedStatic.when(RegistrationFlowEngineDataHolder::getInstance).thenReturn(dataHolderMock);
            when(dataHolderMock.getRegistrationFlowMgtService()).thenReturn(mgtServiceMock);
            when(mgtServiceMock.getRegistrationGraphConfig(TENANT_ID)).thenReturn(graphConfig);
            RegistrationContext context = RegistrationFlowEngineUtils.initiateContext(TENANT_DOMAIN, callback, appId);
            assertNotNull(context);
            assertEquals(context.getTenantDomain(), TENANT_DOMAIN);
            assertNotNull(context.getRegGraph());
            assertEquals(context.getRegGraph().getFirstNodeId(), firstNodeId);
            assertEquals(context.getApplicationId(), appId);
            assertEquals(context.getCallbackUrl(), expectedCallback);
            testContext = context;
        }
    }

    @Test(dependsOnMethods = {"testRegContextInitiation"})
    public void testRegContextCacheAddition() {

        lenient().doNothing().when(regContextCacheMock).addToCache(any(), any());
        try {
            RegistrationFlowEngineUtils.addRegContextToCache(testContext);
        } catch (Exception e) {
            fail("Method threw an exception: " + e.getMessage());
        }
    }

    @Test(dependsOnMethods = {"testRegContextCacheAddition"})
    public void testRegContextCacheRetrieval() {

        RegistrationContextCacheEntry entry = new RegistrationContextCacheEntry(testContext);

        try (MockedStatic<RegistrationContextCache> regContextCacheMockedStatic = mockStatic(
                RegistrationContextCache.class)) {
            regContextCacheMockedStatic.when(RegistrationContextCache::getInstance).thenReturn(regContextCacheMock);
            lenient().when(regContextCacheMock.getValueFromCache(any())).thenReturn(entry);
            RegistrationContext context =
                    RegistrationFlowEngineUtils.retrieveRegContextFromCache(testContext.getContextIdentifier());
            assertNotNull(context);
            assertEquals(context, testContext);
        } catch (Exception e) {
            fail("Method threw an exception: " + e.getMessage());
        }
    }

    @DataProvider(name = "redirectionUrlScenarios")
    public Object[][] redirectionUrlScenarios() {

        String tenantMyAccountUrl = "https://myaccount.is.io/t/${UserTenantHint}";

        return new Object[][] {
                // tenantDomain, appAccessUrl, appFound, myAccountUrl, expectedUrl
                {TENANT_DOMAIN, TEST_APP_URL, true, DEFAULT_MY_ACCOUNT_URL, TEST_APP_URL},
                {TENANT_DOMAIN, null, true, DEFAULT_MY_ACCOUNT_URL, DEFAULT_MY_ACCOUNT_URL},
                {TENANT_DOMAIN, null, false, DEFAULT_MY_ACCOUNT_URL, DEFAULT_MY_ACCOUNT_URL},
                {"test.com", null, false, tenantMyAccountUrl, "https://myaccount.is.io/t/test.com"}
        };
    }

    @Test(dataProvider = "redirectionUrlScenarios")
    public void testResolveCompletionRedirectionUrl(String tenantDomain, String appAccessUrl, boolean appFound,
                                                    String myAccountUrl, String expectedUrl)
            throws Exception {

        RegistrationContext context = new RegistrationContext();
        context.setTenantDomain(tenantDomain);
        context.setApplicationId("test-app-id");

        ApplicationManagementService appMgmtService = mock(ApplicationManagementService.class);

        try (MockedStatic<RegistrationFlowEngineDataHolder> dataHolderMockedStatic = mockStatic(
                RegistrationFlowEngineDataHolder.class);
             MockedStatic<ApplicationMgtUtil> appMgtUtilMockedStatic = mockStatic(ApplicationMgtUtil.class)) {

            dataHolderMockedStatic.when(RegistrationFlowEngineDataHolder::getInstance).thenReturn(dataHolderMock);
            when(dataHolderMock.getApplicationManagementService()).thenReturn(appMgmtService);

            // Create applicationBasicInfo based on whether app should be found.
            if (appFound) {
                ApplicationBasicInfo appInfo = new ApplicationBasicInfo();
                appInfo.setApplicationResourceId("test-app-id");
                appInfo.setAccessUrl(appAccessUrl);
                when(appMgmtService.getApplicationBasicInfoByResourceId("test-app-id", tenantDomain))
                        .thenReturn(appInfo);
            }

            appMgtUtilMockedStatic.when(() -> ApplicationMgtUtil.getMyAccountAccessUrlFromServerConfig(tenantDomain))
                    .thenReturn(myAccountUrl);
            String redirectUrl = RegistrationFlowEngineUtils.resolveCompletionRedirectionUrl(context);
            assertEquals(redirectUrl, expectedUrl);
        }
    }

    @Test
    public void testResolveCompletionRedirectionUrlWithException() throws Exception {

        RegistrationContext context = new RegistrationContext();
        context.setTenantDomain(TENANT_DOMAIN);
        context.setApplicationId("test-app-id");
        context.setCallbackUrl(TEST_CALLBACK_URL);

        ApplicationManagementService appMgmtService = mock(ApplicationManagementService.class);

        try (MockedStatic<RegistrationFlowEngineDataHolder> dataHolderMockedStatic = mockStatic(
                RegistrationFlowEngineDataHolder.class)) {

            dataHolderMockedStatic.when(RegistrationFlowEngineDataHolder::getInstance).thenReturn(dataHolderMock);
            when(dataHolderMock.getApplicationManagementService()).thenReturn(appMgmtService);

            when(appMgmtService.getApplicationBasicInfoByResourceId("test-app-id", TENANT_DOMAIN))
                    .thenThrow(IdentityApplicationManagementException.class);

            try {
                RegistrationFlowEngineUtils.resolveCompletionRedirectionUrl(context);
                fail("Expected exception was not thrown.");
            } catch (RegistrationEngineServerException e) {
                assertEquals(e.getErrorCode(), "RFE-65016");
            }
        }
    }
}
