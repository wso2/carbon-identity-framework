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

package org.wso2.carbon.identity.flow.execution.engine.util;

import org.apache.commons.lang.StringUtils;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.application.common.IdentityApplicationManagementException;
import org.wso2.carbon.identity.application.common.model.ApplicationBasicInfo;
import org.wso2.carbon.identity.application.common.model.InboundAuthenticationConfig;
import org.wso2.carbon.identity.application.common.model.InboundAuthenticationRequestConfig;
import org.wso2.carbon.identity.application.common.model.ServiceProvider;
import org.wso2.carbon.identity.application.authentication.framework.cache.AuthenticationRequestCacheEntry;
import org.wso2.carbon.identity.application.authentication.framework.exception.FrameworkException;
import org.wso2.carbon.identity.application.authentication.framework.model.AuthenticationRequest;
import org.wso2.carbon.identity.application.authentication.framework.util.FrameworkConstants;
import org.wso2.carbon.identity.application.authentication.framework.util.FrameworkUtils;
import org.wso2.carbon.identity.application.mgt.ApplicationManagementService;
import org.wso2.carbon.identity.application.mgt.ApplicationMgtUtil;
import org.wso2.carbon.identity.base.IdentityRuntimeException;
import org.wso2.carbon.identity.common.testng.WithCarbonHome;
import org.wso2.carbon.identity.core.ServiceURL;
import org.wso2.carbon.identity.core.ServiceURLBuilder;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;
import org.wso2.carbon.identity.core.util.IdentityUtil;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.identity.flow.execution.engine.cache.FlowExecCtxCache;
import org.wso2.carbon.identity.flow.execution.engine.cache.FlowExecCtxCacheEntry;
import org.wso2.carbon.identity.flow.execution.engine.exception.FlowEngineException;
import org.wso2.carbon.identity.flow.execution.engine.exception.FlowEngineServerException;
import org.wso2.carbon.identity.flow.execution.engine.internal.FlowExecutionEngineDataHolder;
import org.wso2.carbon.identity.flow.execution.engine.model.FlowExecutionContext;
import org.wso2.carbon.identity.flow.execution.engine.model.FlowOrganization;
import org.wso2.carbon.identity.flow.execution.engine.model.FlowUser;
import org.wso2.carbon.identity.flow.mgt.FlowMgtService;
import org.wso2.carbon.identity.flow.mgt.exception.FlowMgtFrameworkException;
import org.wso2.carbon.identity.flow.mgt.model.ExecutorDTO;
import org.wso2.carbon.identity.flow.mgt.model.FlowRuntimeValueDTO;
import org.wso2.carbon.identity.flow.mgt.model.FlowVariableDataType;
import org.wso2.carbon.identity.flow.mgt.model.FlowVariablesDTO;
import org.wso2.carbon.identity.flow.mgt.model.GraphConfig;
import org.wso2.carbon.identity.flow.mgt.model.NodeConfig;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;
import static org.testng.AssertJUnit.fail;
import static org.wso2.carbon.identity.flow.execution.engine.Constants.ORGANIZATION_AUTO_LOGIN_URL;
import static org.wso2.carbon.identity.flow.execution.engine.Constants.PROVISIONED_TENANT_DOMAIN;
import static org.wso2.carbon.identity.flow.execution.engine.Constants.USERNAME_CLAIM_URI;
import static org.wso2.carbon.identity.flow.execution.engine.Constants.ErrorMessages.ERROR_CODE_FLOW_NOT_FOUND;
import static org.wso2.carbon.identity.flow.execution.engine.Constants.ErrorMessages.ERROR_CODE_GET_DEFAULT_FLOW_FAILURE;
import static org.wso2.carbon.identity.flow.execution.engine.Constants.ErrorMessages.ERROR_CODE_INVALID_FLOW_ID;
import static org.wso2.carbon.identity.flow.execution.engine.Constants.ErrorMessages.ERROR_CODE_TENANT_RESOLVE_FAILURE;
import static org.wso2.carbon.identity.flow.execution.engine.Constants.ErrorMessages.ERROR_CODE_UNDEFINED_FLOW_ID;

/**
 * Unit tests for FlowExecutionEngineUtils.
 */
@WithCarbonHome
@Listeners(MockitoTestNGListener.class)
public class FlowEngineUtilsTest {

    private static final String TENANT_DOMAIN = "carbon.super";
    private static final String TEST_CALLBACK_URL = "https://localhost:3000/myapp/callback";
    private static final String TEST_APP_URL = "https://localhost:3000/myapp";
    private static final String DEFAULT_MY_ACCOUNT_URL = "https://localhost:9443/myaccount";
    private static final String MY_ACCOUNT_APP_NAME = "My Account";
    private static final String ORGANIZATION_TENANT_DOMAIN = "team-a.com";
    private static final String TEST_CLIENT_ID = "test-client-id";
    private static final String ORGANIZATION_CLIENT_ID = "org-test-client-id";
    private static final String TEST_APP_ID = "test-app-id";
    private static final String FLOW_TYPE = "REGISTRATION";
    private static final int TENANT_ID = -1234;
    private FlowExecutionContext testContext;

    @Mock
    private FlowMgtService mgtServiceMock;

    @Mock
    private FlowExecutionEngineDataHolder dataHolderMock;

    @Mock
    private FlowExecCtxCache flowContextCacheMock;

    private MockedStatic<IdentityTenantUtil> identityTenantUtil;
    private AutoCloseable mockAutoCloseable;

    @BeforeClass
    public void setup() throws Exception {

        mockAutoCloseable = MockitoAnnotations.openMocks(this);
        identityTenantUtil = mockStatic(IdentityTenantUtil.class);
        identityTenantUtil.when(() -> IdentityTenantUtil.getTenantId(TENANT_DOMAIN)).thenReturn(TENANT_ID);

        ServiceURL serviceURL = mock(ServiceURL.class);
        when(serviceURL.getAbsolutePublicURL()).thenReturn(DEFAULT_MY_ACCOUNT_URL);
    }

    @Test
    public void testInitContextTenantResolveFailure() {

        identityTenantUtil.when(() -> IdentityTenantUtil.getTenantId(TENANT_DOMAIN))
                .thenThrow(IdentityRuntimeException.class);
        try {
            FlowExecutionEngineUtils.initiateContext(TENANT_DOMAIN, null, FLOW_TYPE);
        } catch (FlowEngineException e) {
            assertEquals(e.getErrorCode(), ERROR_CODE_TENANT_RESOLVE_FAILURE.getCode());
        }
    }

    @Test
    public void testInitContextGraphNotDefined() throws Exception {

        identityTenantUtil.when(() -> IdentityTenantUtil.getTenantId(TENANT_DOMAIN)).thenReturn(TENANT_ID);
        try (MockedStatic<FlowExecutionEngineDataHolder> dataHolderMockedStatic = mockStatic(
                FlowExecutionEngineDataHolder.class)) {
            dataHolderMockedStatic.when(FlowExecutionEngineDataHolder::getInstance).thenReturn(dataHolderMock);
            when(dataHolderMock.getFlowMgtService()).thenReturn(mgtServiceMock);
            when(mgtServiceMock.getGraphConfig(FLOW_TYPE, TENANT_ID)).thenReturn(null);
            FlowExecutionEngineUtils.initiateContext(TENANT_DOMAIN, null, FLOW_TYPE);
        } catch (FlowEngineException e) {
            assertEquals(e.getErrorCode(), ERROR_CODE_FLOW_NOT_FOUND.getCode());
        }
    }

    @Test
    public void testGraphRetrievalFailure() throws Exception {

        identityTenantUtil.when(() -> IdentityTenantUtil.getTenantId(TENANT_DOMAIN)).thenReturn(TENANT_ID);
        try (MockedStatic<FlowExecutionEngineDataHolder> dataHolderMockedStatic = mockStatic(
                FlowExecutionEngineDataHolder.class)) {

            dataHolderMockedStatic.when(FlowExecutionEngineDataHolder::getInstance).thenReturn(dataHolderMock);
            when(dataHolderMock.getFlowMgtService()).thenReturn(mgtServiceMock);
            when(mgtServiceMock.getGraphConfig(FLOW_TYPE, TENANT_ID)).thenThrow(FlowMgtFrameworkException.class);
            FlowExecutionEngineUtils.initiateContext(TENANT_DOMAIN, null, FLOW_TYPE);
        } catch (FlowEngineException e) {
            assertEquals(e.getErrorCode(), ERROR_CODE_GET_DEFAULT_FLOW_FAILURE.getCode());
        }
    }

    /**
     * A new run starts each defined runtime value with its initial value.
     */
    @Test
    public void testFlowContextInitiationStartsDefinedVariables() throws Exception {

        FlowVariablesDTO inputs = new FlowVariablesDTO();
        FlowRuntimeValueDTO plan = new FlowRuntimeValueDTO("plan", FlowVariableDataType.STRING);
        plan.setValue("basic");
        inputs.getRuntimeValues().add(plan);
        inputs.getRuntimeValues().add(new FlowRuntimeValueDTO("attempts", FlowVariableDataType.INTEGER));
        GraphConfig graphConfig = new GraphConfig();
        graphConfig.setFlowVariables(inputs);

        identityTenantUtil.when(() -> IdentityTenantUtil.getTenantId(TENANT_DOMAIN)).thenReturn(TENANT_ID);
        try (MockedStatic<FlowExecutionEngineDataHolder> dataHolderMockedStatic = mockStatic(
                FlowExecutionEngineDataHolder.class);
             MockedStatic<IdentityUtil> identityUtil = mockStatic(IdentityUtil.class, CALLS_REAL_METHODS)) {
            identityUtil.when(() -> IdentityUtil.getProperty(
                    org.wso2.carbon.identity.flow.mgt.Constants.FlowVariableConfigs.ENABLE)).thenReturn("true");
            dataHolderMockedStatic.when(FlowExecutionEngineDataHolder::getInstance).thenReturn(dataHolderMock);
            when(dataHolderMock.getFlowMgtService()).thenReturn(mgtServiceMock);
            when(mgtServiceMock.getGraphConfig(FLOW_TYPE, TENANT_ID)).thenReturn(graphConfig);

            FlowExecutionContext context = FlowExecutionEngineUtils.initiateContext(TENANT_DOMAIN, null, FLOW_TYPE);

            assertEquals(context.getRuntimeValues().get("plan"), "basic");
            assertNull(context.getRuntimeValues().get("attempts"));
        }
    }

    @Test
    public void testContextRetrievalWithEmptyId() {

        try {
            FlowExecutionEngineUtils.retrieveFlowContextFromCache(null);
        } catch (FlowEngineException e) {
            assertEquals(e.getErrorCode(), ERROR_CODE_UNDEFINED_FLOW_ID.getCode());
        }
    }

    @Test
    public void testContextRetrievalWithInvalidId() {

        try (MockedStatic<FlowExecCtxCache> flowContextCacheMockedStatic = mockStatic(
                FlowExecCtxCache.class)) {
            flowContextCacheMockedStatic.when(FlowExecCtxCache::getInstance).thenReturn(flowContextCacheMock);
            lenient().when(flowContextCacheMock.getValueFromCache(any())).thenReturn(null);
            FlowExecutionEngineUtils.retrieveFlowContextFromCache("invalidFlowId");
        } catch (FlowEngineException e) {
            assertEquals(e.getErrorCode(), ERROR_CODE_INVALID_FLOW_ID.getCode());
        }
    }

    @DataProvider(name = "initiateContextScenarios")
    public Object[][] initiateContextScenarios() {

        return new Object[][]{

                {"test-app-id-1"},
                {null},
        };
    }

    @Test(dataProvider = "initiateContextScenarios")
    public void testFlowContextInitiation(String appId) throws Exception {

        String firstNodeId = "testNode123";
        GraphConfig graphConfig = new GraphConfig();
        graphConfig.setFirstNodeId(firstNodeId);

        identityTenantUtil.when(() -> IdentityTenantUtil.getTenantId(TENANT_DOMAIN)).thenReturn(TENANT_ID);
        try (MockedStatic<FlowExecutionEngineDataHolder> dataHolderMockedStatic = mockStatic(
                FlowExecutionEngineDataHolder.class);) {
            dataHolderMockedStatic.when(FlowExecutionEngineDataHolder::getInstance).thenReturn(dataHolderMock);
            when(dataHolderMock.getFlowMgtService()).thenReturn(mgtServiceMock);
            when(mgtServiceMock.getGraphConfig(FLOW_TYPE, TENANT_ID)).thenReturn(graphConfig);
            FlowExecutionContext context = FlowExecutionEngineUtils.initiateContext(TENANT_DOMAIN, appId, FLOW_TYPE);
            assertNotNull(context);
            assertEquals(context.getTenantDomain(), TENANT_DOMAIN);
            assertNotNull(context.getGraphConfig());
            assertEquals(context.getGraphConfig().getFirstNodeId(), firstNodeId);
            assertEquals(context.getApplicationId(), appId);
            testContext = context;
        }
    }

    @Test(dependsOnMethods = {"testFlowContextInitiation"})
    public void testFlowContextCacheAddition() {

        try (MockedStatic<FlowExecCtxCache> flowContextCacheMockedStatic = mockStatic(
                FlowExecCtxCache.class)) {
            flowContextCacheMockedStatic.when(FlowExecCtxCache::getInstance).thenReturn(flowContextCacheMock);
            lenient().doNothing().when(flowContextCacheMock).addToCache(any(), any());
            FlowExecutionEngineUtils.addFlowContextToCache(testContext);
        } catch (Exception e) {
            fail("Method threw an exception: " + e.getMessage());
        }
    }

    @Test(dependsOnMethods = {"testFlowContextCacheAddition"})
    public void testFlowContextCacheRetrieval() {

        FlowExecCtxCacheEntry entry = new FlowExecCtxCacheEntry(testContext);

        identityTenantUtil.when(() -> IdentityTenantUtil.getTenantId(TENANT_DOMAIN)).thenReturn(TENANT_ID);
        try (MockedStatic<FlowExecCtxCache> flowContextCacheMockedStatic = mockStatic(FlowExecCtxCache.class);
             MockedStatic<FlowExecutionEngineDataHolder> dataHolderMockedStatic = mockStatic(
                     FlowExecutionEngineDataHolder.class)) {
            flowContextCacheMockedStatic.when(FlowExecCtxCache::getInstance).thenReturn(flowContextCacheMock);
            dataHolderMockedStatic.when(FlowExecutionEngineDataHolder::getInstance).thenReturn(dataHolderMock);
            when(dataHolderMock.getFlowMgtService()).thenReturn(mgtServiceMock);
            lenient().when(flowContextCacheMock.getValueFromCache(any())).thenReturn(entry);
            FlowExecutionContext context =
                    FlowExecutionEngineUtils.retrieveFlowContextFromCache(testContext.getContextIdentifier());
            assertNotNull(context);
            assertEquals(context, testContext);
        } catch (Exception e) {
            fail("Method threw an exception: " + e.getMessage());
        }
    }

    @DataProvider(name = "redirectionUrlScenarios")
    public Object[][] redirectionUrlScenarios() {

        return new Object[][]{
                {TEST_APP_URL, true, TEST_APP_URL}, // App URL found, use app URL.
                {null, true, DEFAULT_MY_ACCOUNT_URL}, // No app URL, use myaccount URL.
                {null, false, DEFAULT_MY_ACCOUNT_URL}, // App not found, use myaccount URL.
        };
    }

    @Test(dataProvider = "redirectionUrlScenarios")
    public void testResolveCompletionRedirectionUrl(String appAccessUrl, boolean appFound, String expectedUrl)
            throws Exception {

        FlowExecutionContext context = new FlowExecutionContext();
        context.setTenantDomain(TENANT_DOMAIN);
        context.setApplicationId("test-app-id");

        ApplicationManagementService appMgmtService = mock(ApplicationManagementService.class);

        try (MockedStatic<FlowExecutionEngineDataHolder> dataHolderMockedStatic = mockStatic(
                FlowExecutionEngineDataHolder.class);
             MockedStatic<ApplicationMgtUtil> appMgtUtilMockedStatic = mockStatic(ApplicationMgtUtil.class)) {

            dataHolderMockedStatic.when(FlowExecutionEngineDataHolder::getInstance).thenReturn(dataHolderMock);
            when(dataHolderMock.getApplicationManagementService()).thenReturn(appMgmtService);

            if (appFound) {
                ApplicationBasicInfo appInfo = new ApplicationBasicInfo();
                appInfo.setApplicationResourceId("test-app-id");
                appInfo.setAccessUrl(appAccessUrl);
                when(appMgmtService.getApplicationBasicInfoByResourceId("test-app-id", TENANT_DOMAIN)).thenReturn(appInfo);
            }

            appMgtUtilMockedStatic.when(() -> ApplicationMgtUtil.getMyAccountAccessUrlFromServerConfig(TENANT_DOMAIN))
                    .thenReturn(DEFAULT_MY_ACCOUNT_URL);

            String redirectUrl = FlowExecutionEngineUtils.resolveCompletionRedirectionUrl(context);
            assertEquals(redirectUrl, expectedUrl);
        }
    }

    @Test
    public void testResolveCompletionRedirectionUrlWithException() throws Exception {

        FlowExecutionContext context = new FlowExecutionContext();
        context.setTenantDomain(TENANT_DOMAIN);
        context.setApplicationId("test-app-id");
        context.setCallbackUrl(TEST_CALLBACK_URL);

        ApplicationManagementService appMgmtService = mock(ApplicationManagementService.class);

        try (MockedStatic<FlowExecutionEngineDataHolder> dataHolderMockedStatic = mockStatic(
                FlowExecutionEngineDataHolder.class)) {

            dataHolderMockedStatic.when(FlowExecutionEngineDataHolder::getInstance).thenReturn(dataHolderMock);
            when(dataHolderMock.getApplicationManagementService()).thenReturn(appMgmtService);

            when(appMgmtService.getApplicationBasicInfoByResourceId("test-app-id", TENANT_DOMAIN))
                    .thenThrow(IdentityApplicationManagementException.class);

            try {
                FlowExecutionEngineUtils.resolveCompletionRedirectionUrl(context);
                fail("Expected exception was not thrown.");
            } catch (FlowEngineServerException e) {
                assertEquals(e.getErrorCode(), "FE-65016");
            }
        }
    }

    @Test
    public void testResolveCompletionRedirectionUrlForProvisionedOrganization() throws Exception {

        FlowExecutionContext context = createProvisionedOrganizationContext();
        String organizationId = context.getFlowOrganization().getOrganizationId();
        String organizationMyAccountUrl =
                "https://localhost:9443/t/" + TENANT_DOMAIN + "/o/" + organizationId + "/myaccount";
        ApplicationManagementService appMgmtService = mock(ApplicationManagementService.class);

        try (MockedStatic<FlowExecutionEngineDataHolder> dataHolderMockedStatic = mockStatic(
                FlowExecutionEngineDataHolder.class);
             MockedStatic<IdentityUtil> identityUtilMockedStatic = mockStatic(IdentityUtil.class)) {

            dataHolderMockedStatic.when(FlowExecutionEngineDataHolder::getInstance).thenReturn(dataHolderMock);
            when(dataHolderMock.getApplicationManagementService()).thenReturn(appMgmtService);
            when(appMgmtService.getServiceProvider(MY_ACCOUNT_APP_NAME, ORGANIZATION_TENANT_DOMAIN))
                    .thenReturn(createOrganizationMyAccountApplication());
            identityUtilMockedStatic.when(() -> IdentityUtil.getServerURL("/myaccount", true, true))
                    .thenReturn(DEFAULT_MY_ACCOUNT_URL);

            String redirectUrl = FlowExecutionEngineUtils.resolveCompletionRedirectionUrl(context);

            assertEquals(redirectUrl, organizationMyAccountUrl);
        }
    }

    @Test
    public void testCreateOrganizationAuthenticationRequest() throws Exception {

        FlowExecutionContext context = createProvisionedOrganizationContext();
        String organizationId = context.getFlowOrganization().getOrganizationId();
        String organizationMyAccountUrl =
                "https://localhost:9443/t/" + TENANT_DOMAIN + "/o/" + organizationId + "/myaccount";
        String organizationCommonAuthUrl = "https://localhost:9443/o/" + organizationId + "/commonauth";
        ApplicationManagementService appMgmtService = mock(ApplicationManagementService.class);
        ServiceURLBuilder urlBuilder = mock(ServiceURLBuilder.class);
        ServiceURL commonAuthURL = mock(ServiceURL.class);

        try (MockedStatic<FlowExecutionEngineDataHolder> dataHolderMockedStatic = mockStatic(
                FlowExecutionEngineDataHolder.class);
             MockedStatic<IdentityUtil> identityUtilMockedStatic = mockStatic(IdentityUtil.class);
             MockedStatic<ServiceURLBuilder> serviceURLBuilderMockedStatic = mockStatic(ServiceURLBuilder.class);
             MockedStatic<FrameworkUtils> frameworkUtilsMockedStatic = mockStatic(FrameworkUtils.class)) {

            dataHolderMockedStatic.when(FlowExecutionEngineDataHolder::getInstance).thenReturn(dataHolderMock);
            when(dataHolderMock.getApplicationManagementService()).thenReturn(appMgmtService);
            when(appMgmtService.getServiceProvider(MY_ACCOUNT_APP_NAME, ORGANIZATION_TENANT_DOMAIN))
                    .thenReturn(createOrganizationMyAccountApplication());
            identityUtilMockedStatic.when(() -> IdentityUtil.getServerURL("/myaccount", true, true))
                    .thenReturn(DEFAULT_MY_ACCOUNT_URL);
            identityUtilMockedStatic.when(IdentityUtil::getOperationCleanUpTimeout).thenReturn(60L);

            serviceURLBuilderMockedStatic.when(ServiceURLBuilder::create).thenReturn(urlBuilder);
            when(urlBuilder.addPath(any(String[].class))).thenReturn(urlBuilder);
            when(urlBuilder.setOrganization(organizationId)).thenReturn(urlBuilder);
            when(urlBuilder.build()).thenReturn(commonAuthURL);
            when(commonAuthURL.getAbsolutePublicURL()).thenReturn(organizationCommonAuthUrl);

            Map<String, String> autoLoginData = FlowExecutionEngineUtils.createOrganizationAuthenticationRequest(context);

            assertNotNull(autoLoginData.get(FrameworkConstants.SESSION_DATA_KEY));
            assertEquals(autoLoginData.get(FrameworkConstants.RequestParams.TYPE),
                    FrameworkConstants.RequestType.CLAIM_TYPE_OIDC);
            assertEquals(autoLoginData.get(ORGANIZATION_AUTO_LOGIN_URL), organizationCommonAuthUrl);

            ArgumentCaptor<AuthenticationRequestCacheEntry> cacheEntryCaptor =
                    ArgumentCaptor.forClass(AuthenticationRequestCacheEntry.class);
            frameworkUtilsMockedStatic.verify(() -> FrameworkUtils.addAuthenticationRequestToCache(
                    anyString(), cacheEntryCaptor.capture()));
            AuthenticationRequest cachedRequest = cacheEntryCaptor.getValue().getAuthenticationRequest();
            assertEquals(cachedRequest.getTenantDomain(), ORGANIZATION_TENANT_DOMAIN);
            assertEquals(cachedRequest.getRelyingParty(), ORGANIZATION_CLIENT_ID);
            assertEquals(cachedRequest.getType(), FrameworkConstants.RequestType.CLAIM_TYPE_OIDC);
            assertEquals(cachedRequest.getCommonAuthCallerPath(),
                    URLEncoder.encode(organizationMyAccountUrl, StandardCharsets.UTF_8.name()));
        }
    }

    @Test
    public void testCreateOrganizationAuthenticationRequestWithOidcInbound() throws Exception {

        FlowExecutionContext context = createProvisionedOrganizationContext();
        String organizationId = context.getFlowOrganization().getOrganizationId();
        String organizationMyAccountUrl =
                "https://localhost:9443/t/" + TENANT_DOMAIN + "/o/" + organizationId + "/myaccount";
        ApplicationManagementService appMgmtService = mock(ApplicationManagementService.class);
        ServiceURLBuilder urlBuilder = mock(ServiceURLBuilder.class);
        ServiceURL commonAuthURL = mock(ServiceURL.class);

        try (MockedStatic<FlowExecutionEngineDataHolder> dataHolderMockedStatic = mockStatic(
                FlowExecutionEngineDataHolder.class);
             MockedStatic<IdentityUtil> identityUtilMockedStatic = mockStatic(IdentityUtil.class);
             MockedStatic<ServiceURLBuilder> serviceURLBuilderMockedStatic = mockStatic(ServiceURLBuilder.class);
             MockedStatic<FrameworkUtils> frameworkUtilsMockedStatic = mockStatic(FrameworkUtils.class)) {

            dataHolderMockedStatic.when(FlowExecutionEngineDataHolder::getInstance).thenReturn(dataHolderMock);
            when(dataHolderMock.getApplicationManagementService()).thenReturn(appMgmtService);
            when(appMgmtService.getServiceProvider(MY_ACCOUNT_APP_NAME, ORGANIZATION_TENANT_DOMAIN))
                    .thenReturn(createOidcOrganizationMyAccountApplication());
            identityUtilMockedStatic.when(() -> IdentityUtil.getServerURL("/myaccount", true, true))
                    .thenReturn(DEFAULT_MY_ACCOUNT_URL);
            identityUtilMockedStatic.when(IdentityUtil::getOperationCleanUpTimeout).thenReturn(60L);

            serviceURLBuilderMockedStatic.when(ServiceURLBuilder::create).thenReturn(urlBuilder);
            when(urlBuilder.addPath(any(String[].class))).thenReturn(urlBuilder);
            when(urlBuilder.setOrganization(organizationId)).thenReturn(urlBuilder);
            when(urlBuilder.build()).thenReturn(commonAuthURL);
            when(commonAuthURL.getAbsolutePublicURL()).thenReturn(
                    "https://localhost:9443/o/" + organizationId + "/commonauth");

            Map<String, String> autoLoginData = FlowExecutionEngineUtils.createOrganizationAuthenticationRequest(context);

            assertNotNull(autoLoginData.get(FrameworkConstants.SESSION_DATA_KEY));

            ArgumentCaptor<AuthenticationRequestCacheEntry> cacheEntryCaptor =
                    ArgumentCaptor.forClass(AuthenticationRequestCacheEntry.class);
            frameworkUtilsMockedStatic.verify(() -> FrameworkUtils.addAuthenticationRequestToCache(
                    anyString(), cacheEntryCaptor.capture()));
            AuthenticationRequest cachedRequest = cacheEntryCaptor.getValue().getAuthenticationRequest();
            assertEquals(cachedRequest.getRelyingParty(), ORGANIZATION_CLIENT_ID);
        }
    }

    @Test
    public void testCreateOrganizationAuthenticationRequestForFlowTenant() throws Exception {

        FlowExecutionContext context = new FlowExecutionContext();
        context.setTenantDomain(TENANT_DOMAIN);
        context.setApplicationId(TEST_APP_ID);

        Map<String, String> autoLoginData = FlowExecutionEngineUtils.createOrganizationAuthenticationRequest(context);

        assertTrue(autoLoginData.isEmpty());
    }

    @Test
    public void testCreateOrganizationAuthenticationRequestWithoutOAuthInbound() throws Exception {

        FlowExecutionContext context = createProvisionedOrganizationContext();
        ApplicationManagementService appMgmtService = mock(ApplicationManagementService.class);

        try (MockedStatic<FlowExecutionEngineDataHolder> dataHolderMockedStatic = mockStatic(
                FlowExecutionEngineDataHolder.class)) {

            dataHolderMockedStatic.when(FlowExecutionEngineDataHolder::getInstance).thenReturn(dataHolderMock);
            when(dataHolderMock.getApplicationManagementService()).thenReturn(appMgmtService);
            when(appMgmtService.getServiceProvider(MY_ACCOUNT_APP_NAME, ORGANIZATION_TENANT_DOMAIN))
                    .thenReturn(new ServiceProvider());

            Map<String, String> autoLoginData = FlowExecutionEngineUtils.createOrganizationAuthenticationRequest(context);

            assertTrue(autoLoginData.isEmpty());
        }
    }

    @Test
    public void testCreateOrganizationAuthenticationRequestWhenPortalNotShared() throws Exception {

        FlowExecutionContext context = createProvisionedOrganizationContext();
        ApplicationManagementService appMgmtService = mock(ApplicationManagementService.class);

        try (MockedStatic<FlowExecutionEngineDataHolder> dataHolderMockedStatic = mockStatic(
                FlowExecutionEngineDataHolder.class)) {

            dataHolderMockedStatic.when(FlowExecutionEngineDataHolder::getInstance).thenReturn(dataHolderMock);
            when(dataHolderMock.getApplicationManagementService()).thenReturn(appMgmtService);
            when(appMgmtService.getServiceProvider(MY_ACCOUNT_APP_NAME, ORGANIZATION_TENANT_DOMAIN))
                    .thenThrow(IdentityApplicationManagementException.class);

            Map<String, String> autoLoginData = FlowExecutionEngineUtils.createOrganizationAuthenticationRequest(context);

            assertTrue(autoLoginData.isEmpty());
        }
    }

    private FlowExecutionContext createProvisionedOrganizationContext() {

        FlowOrganization organization = new FlowOrganization();
        organization.setOrganizationHandle(ORGANIZATION_TENANT_DOMAIN);

        FlowExecutionContext context = new FlowExecutionContext();
        context.setTenantDomain(TENANT_DOMAIN);
        context.setApplicationId(TEST_APP_ID);
        context.setFlowOrganization(organization);
        context.setProperty(PROVISIONED_TENANT_DOMAIN, ORGANIZATION_TENANT_DOMAIN);
        return context;
    }

    private ServiceProvider createMyAccountApplication() {

        InboundAuthenticationRequestConfig oauthInboundConfig = new InboundAuthenticationRequestConfig();
        oauthInboundConfig.setInboundAuthType("oauth2");
        oauthInboundConfig.setInboundAuthKey(TEST_CLIENT_ID);

        InboundAuthenticationConfig inboundAuthenticationConfig = new InboundAuthenticationConfig();
        inboundAuthenticationConfig.setInboundAuthenticationRequestConfigs(
                new InboundAuthenticationRequestConfig[]{oauthInboundConfig});

        ServiceProvider application = new ServiceProvider();
        application.setApplicationName(MY_ACCOUNT_APP_NAME);
        application.setInboundAuthenticationConfig(inboundAuthenticationConfig);
        return application;
    }

    private ServiceProvider createOrganizationMyAccountApplication() {

        InboundAuthenticationRequestConfig oauthInboundConfig = new InboundAuthenticationRequestConfig();
        oauthInboundConfig.setInboundAuthType("oauth2");
        oauthInboundConfig.setInboundAuthKey(ORGANIZATION_CLIENT_ID);

        InboundAuthenticationConfig inboundAuthenticationConfig = new InboundAuthenticationConfig();
        inboundAuthenticationConfig.setInboundAuthenticationRequestConfigs(
                new InboundAuthenticationRequestConfig[]{oauthInboundConfig});

        ServiceProvider application = new ServiceProvider();
        application.setApplicationName(MY_ACCOUNT_APP_NAME);
        application.setInboundAuthenticationConfig(inboundAuthenticationConfig);
        return application;
    }

    private ServiceProvider createOidcOrganizationMyAccountApplication() {

        InboundAuthenticationRequestConfig oidcInboundConfig = new InboundAuthenticationRequestConfig();
        oidcInboundConfig.setInboundAuthType("oidc");
        oidcInboundConfig.setInboundAuthKey(ORGANIZATION_CLIENT_ID);

        InboundAuthenticationConfig inboundAuthenticationConfig = new InboundAuthenticationConfig();
        inboundAuthenticationConfig.setInboundAuthenticationRequestConfigs(
                new InboundAuthenticationRequestConfig[]{oidcInboundConfig});

        ServiceProvider application = new ServiceProvider();
        application.setApplicationName(MY_ACCOUNT_APP_NAME);
        application.setInboundAuthenticationConfig(inboundAuthenticationConfig);
        return application;
    }

    @Test
    public void testAssertionGenerationIntegration() throws Exception {

        String testUsername = "testuser";
        String testUserId = "user123";
        String testContextId = "context123";
        String expectedAssertion = "signed.jwt.token";

        FlowExecutionContext mockContext = new FlowExecutionContext();
        mockContext.setTenantDomain(TENANT_DOMAIN);
        mockContext.setContextIdentifier(testContextId);

        FlowUser flowUser = new FlowUser();
        flowUser.setUsername(testUsername);
        flowUser.setUserId(testUserId);
        mockContext.setFlowUser(flowUser);


        NodeConfig authNode = new NodeConfig.Builder()
                .id("auth-node-1")
                .type("AUTHENTICATION")
                .executorConfig(new ExecutorDTO("password-authenticator"))
                .build();
        mockContext.getCompletedNodes().add(authNode);

        try (MockedStatic<AuthenticationAssertionUtils> assertionUtilsMock =
                     mockStatic(AuthenticationAssertionUtils.class)) {

            assertionUtilsMock.when(() -> AuthenticationAssertionUtils.getSignedUserAssertion(any()))
                    .thenReturn(expectedAssertion);
            String generatedAssertion = AuthenticationAssertionUtils.getSignedUserAssertion(mockContext);
            assertNotNull(generatedAssertion, "Generated assertion should not be null");
            assertEquals(generatedAssertion, expectedAssertion, "Generated assertion should match expected value");
            assertionUtilsMock.verify(() -> AuthenticationAssertionUtils.getSignedUserAssertion(mockContext));
        }
    }

    @Test
    public void testAssertionGenerationWithEmptyCompletedNodes() throws Exception {

        FlowExecutionContext mockContext = new FlowExecutionContext();
        mockContext.setTenantDomain(TENANT_DOMAIN);
        mockContext.setContextIdentifier("empty-context");

        FlowUser flowUser = new FlowUser();
        flowUser.setUsername("testuser");
        flowUser.setUserId("user123");
        mockContext.setFlowUser(flowUser);
        String expectedAssertion = "minimal.jwt.token";

        try (MockedStatic<AuthenticationAssertionUtils> assertionUtilsMock =
                     mockStatic(AuthenticationAssertionUtils.class)) {
            assertionUtilsMock.when(() -> AuthenticationAssertionUtils.getSignedUserAssertion(any()))
                    .thenReturn(expectedAssertion);
            String generatedAssertion = AuthenticationAssertionUtils.getSignedUserAssertion(mockContext);
            assertNotNull(generatedAssertion, "Assertion should be generated even with empty nodes");
            assertEquals(generatedAssertion, expectedAssertion);
        }
    }

    @DataProvider(name = "resolveTenantDomainScenarios")
    public Object[][] resolveTenantDomainScenarios() {

        return new Object[][]{
                // appResidentOrgId, loginTenantDomain, expectedTenantDomain
                {null, "carbon.super", "carbon.super"},
                {"", "testorg.com", "testorg.com"},
                {"10084a8d-113f-4211-a0d5-efe36b082211", "carbon.super", "org1.com"}
        };
    }

    @Test(dataProvider = "resolveTenantDomainScenarios")
    public void testResolveTenantDomain(String appResidentOrgId, String loginTenantDomain,
                                        String expectedTenantDomain) throws Exception {

        try (MockedStatic<FrameworkUtils> frameworkUtilsMock = mockStatic(FrameworkUtils.class);
             MockedStatic<PrivilegedCarbonContext> carbonContextMock = mockStatic(PrivilegedCarbonContext.class)) {

            PrivilegedCarbonContext privilegedCarbonContext = mock(PrivilegedCarbonContext.class);
            carbonContextMock.when(PrivilegedCarbonContext::getThreadLocalCarbonContext)
                    .thenReturn(privilegedCarbonContext);

            when(privilegedCarbonContext.getTenantDomain()).thenReturn(loginTenantDomain);
            when(privilegedCarbonContext.getApplicationResidentOrganizationId()).thenReturn(appResidentOrgId);

            if (StringUtils.isNotBlank(appResidentOrgId)) {
                frameworkUtilsMock.when(() -> FrameworkUtils.resolveTenantDomainFromOrganizationId(appResidentOrgId))
                        .thenReturn(expectedTenantDomain);
            }

            String resolvedTenantDomain = FlowExecutionEngineUtils.resolveTenantDomain();

            assertNotNull(resolvedTenantDomain);
            assertEquals(resolvedTenantDomain, expectedTenantDomain);
        }
    }

    @Test(expectedExceptions = FlowEngineServerException.class)
    public void testResolveTenantDomainWithFrameworkException() throws Exception {

        String loginTenantDomain = "carbon.super";
        String appResidentOrgId = "invalid-org-id";

        try (MockedStatic<FrameworkUtils> frameworkUtilsMock = mockStatic(FrameworkUtils.class);
             MockedStatic<PrivilegedCarbonContext> carbonContextMock = mockStatic(PrivilegedCarbonContext.class)) {

            PrivilegedCarbonContext privilegedCarbonContext = mock(PrivilegedCarbonContext.class);
            carbonContextMock.when(PrivilegedCarbonContext::getThreadLocalCarbonContext)
                    .thenReturn(privilegedCarbonContext);

            when(privilegedCarbonContext.getTenantDomain()).thenReturn(loginTenantDomain);
            when(privilegedCarbonContext.getApplicationResidentOrganizationId()).thenReturn(appResidentOrgId);

            FrameworkException frameworkException = new FrameworkException(
                    "Failed to resolve tenant domain from organization");
            frameworkUtilsMock.when(() -> FrameworkUtils.resolveTenantDomainFromOrganizationId(appResidentOrgId))
                    .thenThrow(frameworkException);

            FlowExecutionEngineUtils.resolveTenantDomain();
        }
    }

    @AfterClass
    public void teardown() throws Exception {

        if (identityTenantUtil != null) {
            identityTenantUtil.close();
        }

        if (mockAutoCloseable != null) {
            mockAutoCloseable.close();
        }
    }

    @Test
    public void testRegistrationUserStoreDomainIsResolvedFromTheUsername() {

        try (MockedStatic<IdentityUtil> identityUtil = mockStatic(IdentityUtil.class, CALLS_REAL_METHODS)) {
            identityUtil.when(IdentityUtil::getPrimaryDomainName).thenReturn("DEFAULT");
            FlowUser user = new FlowUser();
            FlowExecutionContext context = userContext("REGISTRATION", user);

            user.addClaim(USERNAME_CLAIM_URI, "alice");
            FlowExecutionEngineUtils.resolveUserStoreDomain(context);
            assertEquals(user.getUserStoreDomain(), "DEFAULT");

            user.addClaim(USERNAME_CLAIM_URI, "partners/alice");
            FlowExecutionEngineUtils.resolveUserStoreDomain(context);
            assertEquals(user.getUserStoreDomain(), "PARTNERS");
        }
    }

    @Test
    public void testRegistrationUserStoreDomainFollowsTheDefaultRegistrationUserStore() {

        try (MockedStatic<IdentityUtil> identityUtil = mockStatic(IdentityUtil.class, CALLS_REAL_METHODS)) {
            identityUtil.when(IdentityUtil::getPrimaryDomainName).thenReturn("PRIMARY");
            identityUtil.when(() -> IdentityUtil.getProperty("FlowExecution.Registration.DefaultUserStore"))
                    .thenReturn("customers");
            FlowUser user = new FlowUser();
            FlowExecutionContext context = userContext("REGISTRATION", user);

            user.addClaim(USERNAME_CLAIM_URI, "alice");
            FlowExecutionEngineUtils.resolveUserStoreDomain(context);
            assertEquals(user.getUserStoreDomain(), "CUSTOMERS");

            // A username that names its user store is created there, whatever the default.
            user.addClaim(USERNAME_CLAIM_URI, "partners/alice");
            FlowExecutionEngineUtils.resolveUserStoreDomain(context);
            assertEquals(user.getUserStoreDomain(), "PARTNERS");

            // A separator with nothing before it names no user store.
            user.addClaim(USERNAME_CLAIM_URI, "/alice");
            FlowExecutionEngineUtils.resolveUserStoreDomain(context);
            assertEquals(user.getUserStoreDomain(), "CUSTOMERS");
        }
    }

    @Test
    public void testUserStoreDomainOfAnIdentifiedUserIsKept() {

        FlowUser user = new FlowUser();
        user.setUserId("user-1");
        user.setUserStoreDomain("LDAP");
        user.addClaim(USERNAME_CLAIM_URI, "partners/alice");

        FlowExecutionEngineUtils.resolveUserStoreDomain(userContext("REGISTRATION", user));

        assertEquals(user.getUserStoreDomain(), "LDAP");
    }

    @Test
    public void testUserStoreDomainOutsideRegistrationIsOnlyTakenFromADomainQualifiedUsername() {

        FlowUser plainUser = new FlowUser();
        plainUser.addClaim(USERNAME_CLAIM_URI, "alice");
        FlowExecutionEngineUtils.resolveUserStoreDomain(userContext("PASSWORD_RECOVERY", plainUser));
        assertNull(plainUser.getUserStoreDomain());

        FlowUser qualifiedUser = new FlowUser();
        qualifiedUser.addClaim(USERNAME_CLAIM_URI, "partners/alice");
        FlowExecutionEngineUtils.resolveUserStoreDomain(userContext("PASSWORD_RECOVERY", qualifiedUser));
        assertEquals(qualifiedUser.getUserStoreDomain(), "PARTNERS");

        FlowUser resolvedUser = new FlowUser();
        resolvedUser.setUserStoreDomain("LDAP");
        resolvedUser.addClaim(USERNAME_CLAIM_URI, "partners/alice");
        FlowExecutionEngineUtils.resolveUserStoreDomain(userContext("PASSWORD_RECOVERY", resolvedUser));
        assertEquals(resolvedUser.getUserStoreDomain(), "LDAP");
    }

    private static FlowExecutionContext userContext(String flowType, FlowUser user) {

        FlowExecutionContext context = new FlowExecutionContext();
        context.setFlowType(flowType);
        context.setFlowUser(user);
        return context;
    }
}
