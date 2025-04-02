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

import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.base.IdentityRuntimeException;
import org.wso2.carbon.identity.common.testng.WithCarbonHome;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;
import org.wso2.carbon.identity.user.registration.engine.cache.RegistrationContextCache;
import org.wso2.carbon.identity.user.registration.engine.cache.RegistrationContextCacheEntry;
import org.wso2.carbon.identity.user.registration.engine.exception.RegistrationEngineException;
import org.wso2.carbon.identity.user.registration.engine.internal.RegistrationFlowEngineDataHolder;
import org.wso2.carbon.identity.user.registration.engine.model.RegistrationContext;
import org.wso2.carbon.identity.user.registration.mgt.RegistrationFlowMgtService;
import org.wso2.carbon.identity.user.registration.mgt.exception.RegistrationFrameworkException;
import org.wso2.carbon.identity.user.registration.mgt.model.RegistrationGraphConfig;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.testng.Assert.assertEquals;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertNotNull;
import static org.testng.AssertJUnit.fail;
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
            RegistrationFlowEngineUtils.initiateContext(TENANT_DOMAIN);
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
            RegistrationFlowEngineUtils.initiateContext(TENANT_DOMAIN);
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
            RegistrationFlowEngineUtils.initiateContext(TENANT_DOMAIN);
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

    @Test
    public void testRegContextInitiation() throws Exception {

        String firstNodeId = "testNode123";
        RegistrationGraphConfig graphConfig = new RegistrationGraphConfig();
        graphConfig.setFirstNodeId(firstNodeId);

        identityTenantUtil.when(() -> IdentityTenantUtil.getTenantId(TENANT_DOMAIN)).thenReturn(TENANT_ID);
        try (MockedStatic<RegistrationFlowEngineDataHolder> dataHolderMockedStatic = mockStatic(
                RegistrationFlowEngineDataHolder.class)) {

            dataHolderMockedStatic.when(RegistrationFlowEngineDataHolder::getInstance).thenReturn(dataHolderMock);
            when(dataHolderMock.getRegistrationFlowMgtService()).thenReturn(mgtServiceMock);
            when(mgtServiceMock.getRegistrationGraphConfig(TENANT_ID)).thenReturn(graphConfig);
            RegistrationContext context = RegistrationFlowEngineUtils.initiateContext(TENANT_DOMAIN);
            assertNotNull(context);
            assertEquals(context.getTenantDomain(), TENANT_DOMAIN);
            assertNotNull(context.getRegGraph());
            assertEquals(context.getRegGraph().getFirstNodeId(), firstNodeId);
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
}
