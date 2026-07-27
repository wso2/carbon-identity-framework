/*
 * Copyright (c) 2026, WSO2 LLC. (http://www.wso2.com).
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

import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.application.authentication.framework.config.loader.SequenceLoader;
import org.wso2.carbon.identity.application.authentication.framework.config.model.SequenceConfig;
import org.wso2.carbon.identity.application.authentication.framework.context.AuthenticationContext;
import org.wso2.carbon.identity.application.authentication.framework.exception.FrameworkException;
import org.wso2.carbon.identity.application.authentication.framework.internal.FrameworkServiceDataHolder;
import org.wso2.carbon.identity.application.common.IdentityApplicationManagementException;
import org.wso2.carbon.identity.application.common.model.InboundAuthenticationConfig;
import org.wso2.carbon.identity.application.common.model.InboundAuthenticationRequestConfig;
import org.wso2.carbon.identity.application.common.model.ServiceProvider;
import org.wso2.carbon.identity.application.mgt.ApplicationManagementService;
import org.wso2.carbon.identity.application.mgt.ApplicationManagementServiceImpl;
import org.wso2.carbon.identity.common.testng.WithCarbonHome;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

/**
 * Unit tests for {@link AbstractRequestCoordinator}.
 */
@WithCarbonHome
public class AbstractRequestCoordinatorTest {

    private static final String TENANT_DOMAIN = "carbon.super";
    private static final String SHARED_APP_ID = "shared-app-id-123";
    private static final String CLIENT_ID = "oauth-client-id";

    private AutoCloseable closeable;
    private TestableRequestCoordinator testableRequestCoordinator;
    private MockedStatic<FrameworkServiceDataHolder> frameworkServiceDataHolderStatic;
    private MockedStatic<ApplicationManagementService> appMgtServiceStatic;
    @Mock
    private FrameworkServiceDataHolder frameworkServiceDataHolder;
    @Mock
    private SequenceLoader sequenceLoader;
    @Mock
    private ApplicationManagementServiceImpl appMgtService;

    /**
     * Concrete subclass to test the abstract class.
     */
    private static class TestableRequestCoordinator extends AbstractRequestCoordinator {

        @Override
        public void handle(HttpServletRequest request, HttpServletResponse response) {

            // Test method.
        }
    }

    @BeforeMethod
    public void setUp() {

        closeable = MockitoAnnotations.openMocks(this);
        testableRequestCoordinator = new TestableRequestCoordinator();
        frameworkServiceDataHolderStatic = mockStatic(FrameworkServiceDataHolder.class);
        appMgtServiceStatic = mockStatic(ApplicationManagementService.class);
        frameworkServiceDataHolderStatic.when(FrameworkServiceDataHolder::getInstance).thenReturn(
                frameworkServiceDataHolder);
        when(frameworkServiceDataHolder.getSequenceLoader()).thenReturn(sequenceLoader);
        appMgtServiceStatic.when(ApplicationManagementService::getInstance).thenReturn(appMgtService);
    }

    @AfterMethod
    public void tearDown() throws Exception {

        appMgtServiceStatic.close();
        frameworkServiceDataHolderStatic.close();
        closeable.close();
    }

    @Test
    public void testGetSharedAppSequenceConfigSuccess() throws Exception {

        // Build a shared app ServiceProvider with OAuth2 inbound config.
        ServiceProvider sharedApp = buildServiceProviderWithOAuth2Inbound(CLIENT_ID);
        when(appMgtService.getApplicationByResourceId(SHARED_APP_ID, TENANT_DOMAIN)).thenReturn(sharedApp);

        // Mock the final service provider returned by getServiceProvider (called with client ID).
        ServiceProvider resolvedSp = mock(ServiceProvider.class);
        when(appMgtService.getServiceProviderByClientId(CLIENT_ID, "oauth2", TENANT_DOMAIN))
                .thenReturn(resolvedSp);

        // Mock the sequence config returned by the loader.
        SequenceConfig expectedSequenceConfig = mock(SequenceConfig.class);
        when(sequenceLoader.getSequenceConfig(any(AuthenticationContext.class), any(Map.class),
                eq(resolvedSp))).thenReturn(expectedSequenceConfig);

        // Setup context and parameter map.
        AuthenticationContext context = new AuthenticationContext();
        context.setTenantDomain(TENANT_DOMAIN);
        Map<String, String[]> parameterMap = new HashMap<>();

        SequenceConfig result =
                testableRequestCoordinator.getSharedAppSequenceConfig(context, parameterMap, SHARED_APP_ID);

        assertNotNull(result);
        assertEquals(result, expectedSequenceConfig);
        verify(sequenceLoader).getSequenceConfig(eq(context), eq(parameterMap), eq(resolvedSp));
    }

    @Test(expectedExceptions = FrameworkException.class,
            expectedExceptionsMessageRegExp = ".*SequenceLoader is not available.*")
    public void testGetSharedAppSequenceConfigWithoutSequenceLoader() throws Exception {

        // Override default setup: set SequenceLoader to null.
        when(frameworkServiceDataHolder.getSequenceLoader()).thenReturn(null);

        AuthenticationContext context = new AuthenticationContext();
        context.setTenantDomain(TENANT_DOMAIN);
        Map<String, String[]> parameterMap = new HashMap<>();

        testableRequestCoordinator.getSharedAppSequenceConfig(context, parameterMap, SHARED_APP_ID);
    }

    @Test(expectedExceptions = FrameworkException.class,
            expectedExceptionsMessageRegExp = ".*Shared application not found.*")
    public void testGetSharedAppSequenceConfigAppNotFound() throws Exception {

        // Return null for the shared app lookup.
        when(appMgtService.getApplicationByResourceId(SHARED_APP_ID, TENANT_DOMAIN)).thenReturn(null);

        AuthenticationContext context = new AuthenticationContext();
        context.setTenantDomain(TENANT_DOMAIN);
        Map<String, String[]> parameterMap = new HashMap<>();

        testableRequestCoordinator.getSharedAppSequenceConfig(context, parameterMap, SHARED_APP_ID);
    }

    @Test(expectedExceptions = FrameworkException.class,
            expectedExceptionsMessageRegExp = ".*Inbound authentication is not configured.*")
    public void testGetSharedAppSequenceConfigNoInboundAuth() throws Exception {

        // Return a ServiceProvider with no inbound auth config.
        ServiceProvider sharedApp = new ServiceProvider();
        sharedApp.setInboundAuthenticationConfig(null);
        when(appMgtService.getApplicationByResourceId(SHARED_APP_ID, TENANT_DOMAIN)).thenReturn(sharedApp);

        AuthenticationContext context = new AuthenticationContext();
        context.setTenantDomain(TENANT_DOMAIN);
        Map<String, String[]> parameterMap = new HashMap<>();

        testableRequestCoordinator.getSharedAppSequenceConfig(context, parameterMap, SHARED_APP_ID);
    }

    @Test(expectedExceptions = FrameworkException.class,
            expectedExceptionsMessageRegExp = ".*Inbound authentication is not configured.*")
    public void testGetSharedAppSequenceConfigNoOAuth2Inbound() throws Exception {

        // Return a ServiceProvider with inbound auth but only SAML, no OAuth2.
        ServiceProvider sharedApp = new ServiceProvider();
        InboundAuthenticationConfig inboundConfig = new InboundAuthenticationConfig();
        InboundAuthenticationRequestConfig samlConfig = new InboundAuthenticationRequestConfig();
        samlConfig.setInboundAuthType("samlsso");
        samlConfig.setInboundAuthKey("saml-client");
        inboundConfig.setInboundAuthenticationRequestConfigs(
                new InboundAuthenticationRequestConfig[]{samlConfig});
        sharedApp.setInboundAuthenticationConfig(inboundConfig);
        when(appMgtService.getApplicationByResourceId(SHARED_APP_ID, TENANT_DOMAIN)).thenReturn(sharedApp);

        AuthenticationContext context = new AuthenticationContext();
        context.setTenantDomain(TENANT_DOMAIN);
        Map<String, String[]> parameterMap = new HashMap<>();

        testableRequestCoordinator.getSharedAppSequenceConfig(context, parameterMap, SHARED_APP_ID);
    }

    @Test(expectedExceptions = FrameworkException.class,
            expectedExceptionsMessageRegExp = ".*Error while retrieving service provider.*")
    public void testGetSharedAppSequenceConfigApplicationManagementException() throws Exception {

        // Throw IdentityApplicationManagementException from getApplicationByResourceId.
        when(appMgtService.getApplicationByResourceId(SHARED_APP_ID, TENANT_DOMAIN))
                .thenThrow(new IdentityApplicationManagementException("DB error"));

        AuthenticationContext context = new AuthenticationContext();
        context.setTenantDomain(TENANT_DOMAIN);
        Map<String, String[]> parameterMap = new HashMap<>();

        testableRequestCoordinator.getSharedAppSequenceConfig(context, parameterMap, SHARED_APP_ID);
    }

    private ServiceProvider buildServiceProviderWithOAuth2Inbound(String clientId) {

        ServiceProvider sp = new ServiceProvider();
        InboundAuthenticationConfig inboundConfig = new InboundAuthenticationConfig();
        InboundAuthenticationRequestConfig oauth2Config = new InboundAuthenticationRequestConfig();
        oauth2Config.setInboundAuthType("oauth2");
        oauth2Config.setInboundAuthKey(clientId);
        inboundConfig.setInboundAuthenticationRequestConfigs(
                new InboundAuthenticationRequestConfig[]{oauth2Config});
        sp.setInboundAuthenticationConfig(inboundConfig);
        return sp;
    }
}
