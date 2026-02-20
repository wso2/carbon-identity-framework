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

package org.wso2.carbon.identity.application.authentication.framework.model;

import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.application.authentication.framework.config.model.ApplicationConfig;
import org.wso2.carbon.identity.application.authentication.framework.config.model.AuthenticatorConfig;
import org.wso2.carbon.identity.application.authentication.framework.config.model.SequenceConfig;
import org.wso2.carbon.identity.application.authentication.framework.config.model.StepConfig;
import org.wso2.carbon.identity.application.authentication.framework.context.SessionAuthHistory;
import org.wso2.carbon.identity.application.authentication.framework.exception.session.storage.SessionDataStorageOptimizationClientException;
import org.wso2.carbon.identity.application.authentication.framework.exception.session.storage.SessionDataStorageOptimizationServerException;
import org.wso2.carbon.identity.application.authentication.framework.internal.FrameworkServiceDataHolder;
import org.wso2.carbon.identity.application.authentication.framework.internal.core.ApplicationAuthenticatorManager;
import org.wso2.carbon.identity.application.common.model.IdentityProvider;
import org.wso2.carbon.identity.application.common.model.ServiceProvider;
import org.wso2.carbon.identity.application.mgt.ApplicationManagementService;
import org.wso2.carbon.identity.application.mgt.ApplicationManagementServiceImpl;
import org.wso2.carbon.identity.common.testng.WithCarbonHome;
import org.wso2.carbon.idp.mgt.IdentityProviderManagementClientException;
import org.wso2.carbon.idp.mgt.IdentityProviderManagementException;
import org.wso2.carbon.idp.mgt.IdentityProviderManagementServerException;
import org.wso2.carbon.idp.mgt.IdentityProviderManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

/**
 * Unit tests for {@link OptimizedAuthenticatedOrgData}.
 */
@WithCarbonHome
public class OptimizedAuthenticatedOrgDataTest {

    private static final String TENANT_DOMAIN = "carbon.super";
    private static final String APP_NAME = "testApp";
    private static final String IDP_NAME = "testIdP";
    private static final String IDP_RESOURCE_ID = "idp-resource-id-123";
    private static final String APP_RESOURCE_ID = "app-resource-id-456";

    private AutoCloseable closeable;
    private MockedStatic<FrameworkServiceDataHolder> frameworkServiceDataHolderStatic;
    private MockedStatic<ApplicationManagementService> appMgtServiceStatic;
    private MockedStatic<ApplicationAuthenticatorManager> appAuthenticatorManagerStatic;
    @Mock
    private FrameworkServiceDataHolder frameworkServiceDataHolder;
    @Mock
    private IdentityProviderManager identityProviderManager;
    @Mock
    private ApplicationManagementServiceImpl appMgtService;
    @Mock
    private ApplicationAuthenticatorManager applicationAuthenticatorManager;

    @BeforeMethod
    public void setUp() throws Exception {

        closeable = MockitoAnnotations.openMocks(this);

        frameworkServiceDataHolderStatic = mockStatic(FrameworkServiceDataHolder.class);
        appMgtServiceStatic = mockStatic(ApplicationManagementService.class);
        appAuthenticatorManagerStatic = mockStatic(ApplicationAuthenticatorManager.class);

        frameworkServiceDataHolderStatic.when(FrameworkServiceDataHolder::getInstance).thenReturn(
                frameworkServiceDataHolder);
        when(frameworkServiceDataHolder.getIdentityProviderManager()).thenReturn(identityProviderManager);
        when(frameworkServiceDataHolder.getApplicationManagementService()).thenReturn(appMgtService);
        appMgtServiceStatic.when(ApplicationManagementService::getInstance).thenReturn(appMgtService);
        appAuthenticatorManagerStatic.when(ApplicationAuthenticatorManager::getInstance).thenReturn(
                applicationAuthenticatorManager);

        // Default mock for ApplicationAuthenticatorManager to return an IDP during deserialization.
        IdentityProvider defaultIdp = new IdentityProvider();
        defaultIdp.setIdentityProviderName(IDP_NAME);
        defaultIdp.setResourceId(IDP_RESOURCE_ID);
        when(applicationAuthenticatorManager.getSerializableIdPByResourceId(anyString(), anyString()))
                .thenReturn(defaultIdp);
        when(applicationAuthenticatorManager.getApplicationAuthenticatorByName(anyString(), anyString()))
                .thenReturn(null);
    }

    @AfterMethod
    public void tearDown() throws Exception {

        appAuthenticatorManagerStatic.close();
        appMgtServiceStatic.close();
        frameworkServiceDataHolderStatic.close();
        closeable.close();
    }

    @Test
    public void testConstructorAndGetAuthenticatedOrgDataSuccess() throws Exception {

        AuthenticatedOrgData orgData = buildAuthenticatedOrgData(true, IDP_RESOURCE_ID);

        ServiceProvider resolvedSp = buildServiceProvider(APP_RESOURCE_ID, TENANT_DOMAIN);
        when(appMgtService.getApplicationByResourceId(eq(APP_RESOURCE_ID), eq(TENANT_DOMAIN)))
                .thenReturn(resolvedSp);

        OptimizedAuthenticatedOrgData optimizedData = new OptimizedAuthenticatedOrgData(orgData);
        AuthenticatedOrgData result = optimizedData.getAuthenticatedOrgData();

        assertNotNull(result);
        assertTrue(result.isRememberMe());
        assertNotNull(result.getSessionAuthHistory());
        assertNotNull(result.getAuthenticatedSequences());
        assertTrue(result.getAuthenticatedSequences().containsKey(APP_NAME));
        assertNotNull(result.getAuthenticatedIdPs());
        assertTrue(result.getAuthenticatedIdPs().containsKey(IDP_NAME));
        assertNotNull(result.getAuthenticatedIdPsOfApp());
        assertNotNull(result.getAuthenticatedIdPsOfApp().get(APP_NAME));
    }

    @Test
    public void testConstructorWithEmptyIdPResourceIdResolvesFromManager() throws Exception {

        // Build with empty resource ID so it resolves via IdentityProviderManager.
        AuthenticatedOrgData orgData = buildAuthenticatedOrgData(false, "");

        IdentityProvider resolvedIdp = new IdentityProvider();
        resolvedIdp.setResourceId(IDP_RESOURCE_ID);
        when(identityProviderManager.getIdPByName(eq(IDP_NAME), eq(TENANT_DOMAIN))).thenReturn(resolvedIdp);

        ServiceProvider resolvedSp = buildServiceProvider(APP_RESOURCE_ID, TENANT_DOMAIN);
        when(appMgtService.getApplicationByResourceId(eq(APP_RESOURCE_ID), eq(TENANT_DOMAIN)))
                .thenReturn(resolvedSp);

        OptimizedAuthenticatedOrgData optimizedData = new OptimizedAuthenticatedOrgData(orgData);
        AuthenticatedOrgData result = optimizedData.getAuthenticatedOrgData();

        assertNotNull(result);
        assertFalse(result.isRememberMe());
    }

    @Test
    public void testConstructorPreservesRememberMeFlag() throws Exception {

        AuthenticatedOrgData orgData = buildAuthenticatedOrgData(true, IDP_RESOURCE_ID);

        ServiceProvider resolvedSp = buildServiceProvider(APP_RESOURCE_ID, TENANT_DOMAIN);
        when(appMgtService.getApplicationByResourceId(eq(APP_RESOURCE_ID), eq(TENANT_DOMAIN)))
                .thenReturn(resolvedSp);

        OptimizedAuthenticatedOrgData optimizedData = new OptimizedAuthenticatedOrgData(orgData);
        AuthenticatedOrgData result = optimizedData.getAuthenticatedOrgData();

        assertTrue(result.isRememberMe());
    }

    @Test
    public void testConstructorPreservesSessionAuthHistory() throws Exception {

        AuthenticatedOrgData orgData = buildAuthenticatedOrgData(false, IDP_RESOURCE_ID);
        SessionAuthHistory sessionAuthHistory = orgData.getSessionAuthHistory();

        ServiceProvider resolvedSp = buildServiceProvider(APP_RESOURCE_ID, TENANT_DOMAIN);
        when(appMgtService.getApplicationByResourceId(eq(APP_RESOURCE_ID), eq(TENANT_DOMAIN)))
                .thenReturn(resolvedSp);

        OptimizedAuthenticatedOrgData optimizedData = new OptimizedAuthenticatedOrgData(orgData);
        AuthenticatedOrgData result = optimizedData.getAuthenticatedOrgData();

        assertEquals(result.getSessionAuthHistory(), sessionAuthHistory);
    }

    @Test(expectedExceptions = SessionDataStorageOptimizationClientException.class,
            expectedExceptionsMessageRegExp = ".*Tenant domain is null.*")
    public void testConstructorThrowsClientExceptionWhenTenantDomainEmpty() throws Exception {

        AuthenticatedOrgData orgData = buildAuthenticatedOrgDataWithTenantDomain("", false, "");

        new OptimizedAuthenticatedOrgData(orgData);
    }

    @Test(expectedExceptions = SessionDataStorageOptimizationClientException.class,
            expectedExceptionsMessageRegExp = ".*Cannot find the Identity Provider.*")
    public void testConstructorThrowsClientExceptionWhenIdPNotFound() throws Exception {

        AuthenticatedOrgData orgData = buildAuthenticatedOrgData(false, "");

        when(identityProviderManager.getIdPByName(eq(IDP_NAME), eq(TENANT_DOMAIN))).thenReturn(null);

        new OptimizedAuthenticatedOrgData(orgData);
    }

    @Test(expectedExceptions = SessionDataStorageOptimizationClientException.class,
            expectedExceptionsMessageRegExp = ".*IDP management client error.*")
    public void testConstructorWrapsIdPManagementClientException() throws Exception {

        AuthenticatedOrgData orgData = buildAuthenticatedOrgData(false, "");

        when(identityProviderManager.getIdPByName(eq(IDP_NAME), eq(TENANT_DOMAIN)))
                .thenThrow(new IdentityProviderManagementClientException("Client error"));

        new OptimizedAuthenticatedOrgData(orgData);
    }

    @Test(expectedExceptions = SessionDataStorageOptimizationServerException.class,
            expectedExceptionsMessageRegExp = ".*IDP management server error.*")
    public void testConstructorWrapsIdPManagementServerException() throws Exception {

        AuthenticatedOrgData orgData = buildAuthenticatedOrgData(false, "");

        when(identityProviderManager.getIdPByName(eq(IDP_NAME), eq(TENANT_DOMAIN)))
                .thenThrow(new IdentityProviderManagementServerException("Server error"));

        new OptimizedAuthenticatedOrgData(orgData);
    }

    @Test(expectedExceptions = SessionDataStorageOptimizationServerException.class,
            expectedExceptionsMessageRegExp = ".*Error while retrieving the Identity Provider.*")
    public void testConstructorWrapsGenericIdPManagementException() throws Exception {

        AuthenticatedOrgData orgData = buildAuthenticatedOrgData(false, "");

        when(identityProviderManager.getIdPByName(eq(IDP_NAME), eq(TENANT_DOMAIN)))
                .thenThrow(new IdentityProviderManagementException("Generic error"));

        new OptimizedAuthenticatedOrgData(orgData);
    }

    @Test
    public void testGetAuthenticatedOrgDataWithEmptyAuthenticatedIdPs() throws Exception {

        AuthenticatedOrgData orgData = buildAuthenticatedOrgData(false, IDP_RESOURCE_ID);
        orgData.setAuthenticatedIdPs(new HashMap<>());
        orgData.setAuthenticatedIdPsOfApp(new HashMap<>());

        ServiceProvider resolvedSp = buildServiceProvider(APP_RESOURCE_ID, TENANT_DOMAIN);
        when(appMgtService.getApplicationByResourceId(eq(APP_RESOURCE_ID), eq(TENANT_DOMAIN)))
                .thenReturn(resolvedSp);

        OptimizedAuthenticatedOrgData optimizedData = new OptimizedAuthenticatedOrgData(orgData);
        AuthenticatedOrgData result = optimizedData.getAuthenticatedOrgData();

        assertNotNull(result);
        assertTrue(result.getAuthenticatedIdPs().isEmpty());
        assertTrue(result.getAuthenticatedIdPsOfApp().isEmpty());
    }

    @Test
    public void testGetAuthenticatedOrgDataPreservesMultipleAppsAndIdPs() throws Exception {

        String secondAppName = "secondApp";
        String secondIdpName = "secondIdP";

        AuthenticatedOrgData orgData = buildAuthenticatedOrgData(false, IDP_RESOURCE_ID);

        // Add a second IDP to the map.
        AuthenticatedIdPData secondIdpData = buildAuthenticatedIdPData(secondIdpName);
        orgData.getAuthenticatedIdPs().put(secondIdpName, secondIdpData);

        // Add a second app to the authenticatedIdPsOfApp.
        Map<String, AuthenticatedIdPData> secondAppIdPs = new HashMap<>();
        secondAppIdPs.put(secondIdpName, secondIdpData);
        orgData.getAuthenticatedIdPsOfApp().put(secondAppName, secondAppIdPs);

        ServiceProvider resolvedSp = buildServiceProvider(APP_RESOURCE_ID, TENANT_DOMAIN);
        when(appMgtService.getApplicationByResourceId(eq(APP_RESOURCE_ID), eq(TENANT_DOMAIN)))
                .thenReturn(resolvedSp);

        OptimizedAuthenticatedOrgData optimizedData = new OptimizedAuthenticatedOrgData(orgData);
        AuthenticatedOrgData result = optimizedData.getAuthenticatedOrgData();

        assertNotNull(result);
        assertEquals(result.getAuthenticatedIdPs().size(), 2);
        assertTrue(result.getAuthenticatedIdPs().containsKey(IDP_NAME));
        assertTrue(result.getAuthenticatedIdPs().containsKey(secondIdpName));
        assertEquals(result.getAuthenticatedIdPsOfApp().size(), 2);
        assertTrue(result.getAuthenticatedIdPsOfApp().containsKey(APP_NAME));
        assertTrue(result.getAuthenticatedIdPsOfApp().containsKey(secondAppName));
    }

    private AuthenticatedOrgData buildAuthenticatedOrgData(boolean rememberMe, String idpResourceId) {

        return buildAuthenticatedOrgDataWithTenantDomain(TENANT_DOMAIN, rememberMe, idpResourceId);
    }

    private AuthenticatedOrgData buildAuthenticatedOrgDataWithTenantDomain(String tenantDomain,
                                                                           boolean rememberMe,
                                                                           String idpResourceId) {

        AuthenticatedOrgData orgData = new AuthenticatedOrgData();
        orgData.setRememberMe(rememberMe);
        orgData.setSessionAuthHistory(new SessionAuthHistory());

        // Build authenticated sequences.
        SequenceConfig sequenceConfig = buildSequenceConfig(tenantDomain, idpResourceId);
        Map<String, SequenceConfig> authenticatedSequences = new HashMap<>();
        authenticatedSequences.put(APP_NAME, sequenceConfig);
        orgData.setAuthenticatedSequences(authenticatedSequences);

        // Build authenticated IdPs.
        Map<String, AuthenticatedIdPData> authenticatedIdPs = new HashMap<>();
        authenticatedIdPs.put(IDP_NAME, buildAuthenticatedIdPData(IDP_NAME));
        orgData.setAuthenticatedIdPs(authenticatedIdPs);

        // Build authenticated IdPs of app.
        Map<String, Map<String, AuthenticatedIdPData>> authenticatedIdPsOfApp = new HashMap<>();
        Map<String, AuthenticatedIdPData> appIdPs = new HashMap<>();
        appIdPs.put(IDP_NAME, buildAuthenticatedIdPData(IDP_NAME));
        authenticatedIdPsOfApp.put(APP_NAME, appIdPs);
        orgData.setAuthenticatedIdPsOfApp(authenticatedIdPsOfApp);

        return orgData;
    }

    private SequenceConfig buildSequenceConfig(String tenantDomain, String idpResourceId) {

        // Build identity provider.
        IdentityProvider idp = new IdentityProvider();
        idp.setIdentityProviderName(IDP_NAME);
        idp.setResourceId(idpResourceId);

        // Build authenticator config with the IDP.
        AuthenticatorConfig authenticatorConfig = new AuthenticatorConfig();
        authenticatorConfig.setName("BasicAuthenticator");
        Map<String, IdentityProvider> idps = new HashMap<>();
        idps.put(IDP_NAME, idp);
        authenticatorConfig.setIdPs(idps);

        // Build step config.
        StepConfig stepConfig = new StepConfig();
        List<AuthenticatorConfig> authenticatorList = new ArrayList<>();
        authenticatorList.add(authenticatorConfig);
        stepConfig.setAuthenticatorList(authenticatorList);

        // Build service provider.
        ServiceProvider serviceProvider = buildServiceProvider(APP_RESOURCE_ID, tenantDomain);

        // Build application config.
        ApplicationConfig applicationConfig = new ApplicationConfig(serviceProvider, tenantDomain);

        // Build sequence config.
        SequenceConfig sequenceConfig = new SequenceConfig();
        sequenceConfig.setName(APP_NAME);
        sequenceConfig.setApplicationConfig(applicationConfig);
        Map<Integer, StepConfig> stepMap = new HashMap<>();
        stepMap.put(1, stepConfig);
        sequenceConfig.setStepMap(stepMap);

        return sequenceConfig;
    }

    private AuthenticatedIdPData buildAuthenticatedIdPData(String idpName) {

        AuthenticatedIdPData idpData = new AuthenticatedIdPData();
        idpData.setIdpName(idpName);
        idpData.setAuthenticators(new ArrayList<>());
        return idpData;
    }

    private ServiceProvider buildServiceProvider(String resourceId, String tenantDomain) {

        ServiceProvider sp = new ServiceProvider();
        sp.setApplicationName(APP_NAME);
        sp.setApplicationResourceId(resourceId);
        sp.setTenantDomain(tenantDomain);
        return sp;
    }
}
