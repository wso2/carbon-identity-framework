/*
 * Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
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

package org.wso2.carbon.identity.application.mgt;

import org.apache.axiom.om.OMElement;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.testng.PowerMockTestCase;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;
import org.wso2.carbon.base.CarbonBaseConstants;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.identity.application.common.IdentityApplicationManagementException;
import org.wso2.carbon.identity.application.common.model.ApplicationBasicInfo;
import org.wso2.carbon.identity.application.common.model.IdentityProvider;
import org.wso2.carbon.identity.application.common.model.LocalAuthenticatorConfig;
import org.wso2.carbon.identity.application.common.model.RequestPathAuthenticatorConfig;
import org.wso2.carbon.identity.application.common.model.ServiceProvider;
import org.wso2.carbon.identity.application.common.model.SpTemplate;
import org.wso2.carbon.identity.application.mgt.cache.ServiceProviderTemplateCache;
import org.wso2.carbon.identity.application.mgt.dao.ApplicationDAO;
import org.wso2.carbon.identity.application.mgt.dao.IdentityProviderDAO;
import org.wso2.carbon.identity.application.mgt.dao.PaginatableFilterableApplicationDAO;
import org.wso2.carbon.identity.application.mgt.dao.impl.AbstractApplicationDAOImpl;
import org.wso2.carbon.identity.application.mgt.internal.ApplicationManagementServiceComponent;
import org.wso2.carbon.identity.application.mgt.internal.ApplicationManagementServiceComponentHolder;
import org.wso2.carbon.identity.application.mgt.internal.ApplicationMgtListenerServiceComponent;
import org.wso2.carbon.identity.application.mgt.listener.AbstractApplicationMgtListener;
import org.wso2.carbon.identity.application.mgt.listener.ApplicationMgtListener;
import org.wso2.carbon.identity.application.mgt.validator.ApplicationValidatorManager;
import org.wso2.carbon.identity.core.util.IdentityConfigParser;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.core.service.RealmService;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.powermock.api.mockito.PowerMockito.doCallRealMethod;
import static org.powermock.api.mockito.PowerMockito.doNothing;
import static org.powermock.api.mockito.PowerMockito.doThrow;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.spy;
import static org.powermock.api.mockito.PowerMockito.when;
import static org.wso2.carbon.base.MultitenantConstants.SUPER_TENANT_DOMAIN_NAME;
import static org.wso2.carbon.base.MultitenantConstants.SUPER_TENANT_ID;

/*
  Unit tests for ApplicationManagementServiceImpl.
 */
@PrepareForTest({ApplicationManagementServiceComponent.class, ApplicationMgtSystemConfig.class,
        ServiceProviderTemplateCache.class, ApplicationManagementServiceComponentHolder.class,
        ApplicationMgtListenerServiceComponent.class, IdentityConfigParser.class, PrivilegedCarbonContext.class,
        ApplicationMgtUtil.class})
public class ApplicationManagementServiceImplTest extends PowerMockTestCase {

    @Mock
    private ApplicationDAO mockApplicationDAO;
    @Mock
    private ApplicationMgtSystemConfig mockAppMgtSystemConfig;
    @Mock
    private Map<String, ServiceProvider> mockFileBasedSPs;
    @Mock
    private ApplicationValidatorManager mockApplicationValidatorManager;
    @Mock
    private IdentityProviderDAO mockIdentityProviderDAO;
    @Mock
    private List<LocalAuthenticatorConfig> mockLocalAuthenticatorConfigs;
    @Mock
    private SpTemplate mockSpTemplate;
    @Mock
    private ServiceProviderTemplateCache mockSPTemplateCache;
    @Mock
    private ServiceProvider mockServiceProvider;
    @Mock
    private AbstractApplicationDAOImpl mockAbstractAppDAOImpl;
    @Mock
    private PaginatableFilterableApplicationDAO mockPaginatedAppDAO;
    @Mock
    private AbstractApplicationMgtListener mockAppMgtListener;
    @Mock
    private IdentityProvider mockIdentityProvider;
    @Mock
    private Map<String, String> mockClaimMap;
    @Mock
    private LocalAuthenticatorConfig mockLocalAuthenticatorConfig;
    @Mock
    private RequestPathAuthenticatorConfig mockReqPathAuthenticatorConfig;
    @Mock
    private IdentityConfigParser mockConfigParser;
    @Mock
    private OMElement mockSystemApplicationsConfig;
    @Mock
    private OMElement mockChildApplicationConfig;
    @Mock
    private RealmService mockRealmService;
    @Mock
    private org.wso2.carbon.user.core.tenant.TenantManager mockTenantManager;

    private static final String USERNAME = "user";
    private static final String TENANT_DOMAIN = "carbon.super";
    private static final String APPLICATION_NAME = "applicationName";
    private static final int APPLICATION_ID = 123;
    private static final String FEDERATED_IDP_NAME = "fedIdpName";
    private ApplicationManagementServiceImpl applicationManagementService;
    private ApplicationBasicInfo[] applicationBasicInfo;

    @BeforeTest
    public void setup() {

        applicationBasicInfo = new ApplicationBasicInfo[]{};
        applicationManagementService = ApplicationManagementServiceImpl.getInstance();
    }

    @BeforeMethod
    public void init() throws Exception {

        startTenantFlow();
        mockStatic(ApplicationMgtSystemConfig.class);
        when(ApplicationMgtSystemConfig.getInstance()).thenReturn(mockAppMgtSystemConfig);
    }

    private void startTenantFlow() throws UserStoreException {

        String carbonHome = Paths.get(System.getProperty("user.dir"), "target", "test-classes").toString();
        System.setProperty(CarbonBaseConstants.CARBON_HOME, carbonHome);
        System.setProperty(CarbonBaseConstants.CARBON_CONFIG_DIR_PATH, Paths.get(carbonHome, "conf").toString());

        mockStatic(ApplicationManagementServiceComponentHolder.class);
        ApplicationManagementServiceComponentHolder appMgtServiceComHolder = Mockito.
                mock(ApplicationManagementServiceComponentHolder.class);
        when(ApplicationManagementServiceComponentHolder.getInstance()).thenReturn(appMgtServiceComHolder);
        when(appMgtServiceComHolder.getRealmService()).thenReturn(mockRealmService);
        when(mockRealmService.getTenantManager()).thenReturn(mockTenantManager);
        when(mockTenantManager.getTenantId(anyString())).thenReturn(SUPER_TENANT_ID);

        mockStatic(PrivilegedCarbonContext.class);
        PrivilegedCarbonContext privilegedCarbonContext = Mockito.mock(PrivilegedCarbonContext.class);
        when(PrivilegedCarbonContext.getThreadLocalCarbonContext()).thenReturn(privilegedCarbonContext);
        when(privilegedCarbonContext.getTenantDomain()).thenReturn(SUPER_TENANT_DOMAIN_NAME);
        when(privilegedCarbonContext.getTenantId()).thenReturn(SUPER_TENANT_ID);
        when(privilegedCarbonContext.getUsername()).thenReturn("admin");
    }

    private void mockFileBasedSPs() {

        mockStatic(ApplicationManagementServiceComponent.class);
        when(ApplicationManagementServiceComponent.getFileBasedSPs()).thenReturn(mockFileBasedSPs);
    }

    private void mockApplicationMgtListeners() {

        Collection<ApplicationMgtListener> mockMgtListeners = new ArrayList<>();
        mockMgtListeners.add(mockAppMgtListener);
        mockStatic(ApplicationMgtListenerServiceComponent.class);
        when(ApplicationMgtListenerServiceComponent.getApplicationMgtListeners()).thenReturn(mockMgtListeners);
        when(mockAppMgtListener.isEnable()).thenReturn(FALSE);
    }

    @Test
    public void testGetApplicationExcludingFileBasedSPs() throws IdentityApplicationManagementException {

        mockApplicationMgtListeners();
        when(mockAppMgtListener.doPreGetApplicationExcludingFileBasedSPs(anyString(), anyString())).
                thenReturn(TRUE);
        when(mockAppMgtListener.doPostGetApplicationExcludingFileBasedSPs(anyObject(), anyString(),
                anyString())).thenReturn(TRUE);
        when(mockAppMgtSystemConfig.getApplicationDAO()).thenReturn(mockApplicationDAO);
        when(mockApplicationDAO.getApplication(anyString(), anyString())).thenReturn(mockServiceProvider);

        Assert.assertEquals(mockServiceProvider, applicationManagementService.getApplicationExcludingFileBasedSPs
                (APPLICATION_NAME, TENANT_DOMAIN));
    }

    @Test
    public void testCreateApplicationWithTemplate() throws Exception {

        mockApplicationMgtListeners();
        when(mockAppMgtListener.doPreCreateApplication(anyObject(), anyString(), anyString())).thenReturn(TRUE);
        when(mockAppMgtListener.doPostCreateApplication(anyObject(), anyString(), anyString())).thenReturn(TRUE);

        ServiceProvider mockedSP = spy(new ServiceProvider());
        when(mockedSP.getApplicationName()).thenReturn(APPLICATION_NAME);
        when(mockAppMgtSystemConfig.getApplicationDAO()).thenReturn(mockApplicationDAO);
        when(mockApplicationDAO.createApplication(mockedSP, TENANT_DOMAIN)).thenReturn(APPLICATION_ID);

        mockFileBasedSPs();
        doNothing().when(mockApplicationValidatorManager).validateSPConfigurations(anyObject(),
                anyString(), anyString());

        mockStatic(ApplicationMgtUtil.class);
        doCallRealMethod().when(ApplicationMgtUtil.class, "isRegexValidated", APPLICATION_NAME);
        doCallRealMethod().when(ApplicationMgtUtil.class, "getSPValidatorRegex");

        when(mockAppMgtSystemConfig.getIdentityProviderDAO()).thenReturn(mockIdentityProviderDAO);
        when(mockIdentityProviderDAO.getAllLocalAuthenticators()).thenReturn(mockLocalAuthenticatorConfigs);

        mockStatic(ServiceProviderTemplateCache.class);
        when(ServiceProviderTemplateCache.getInstance()).thenReturn(mockSPTemplateCache);
        when(mockSPTemplateCache.getValueFromCache(anyObject())).thenReturn(mockSpTemplate);

        ServiceProvider serviceProvider = applicationManagementService.createApplicationWithTemplate(mockedSP,
                TENANT_DOMAIN, USERNAME, "");
        Assert.assertNotNull(serviceProvider);
        Assert.assertEquals(serviceProvider.getApplicationID(), APPLICATION_ID);
        Assert.assertNotNull(applicationManagementService.addApplication(mockedSP, TENANT_DOMAIN, USERNAME));
    }

    @Test
    public void testGetApplicationBasicInfo() throws IdentityApplicationManagementException {

        mockApplicationMgtListeners();
        when(mockAppMgtListener.doPreGetApplicationBasicInfo(anyString(), anyString(), anyString())).thenReturn(TRUE);
        when(mockAppMgtListener.doPostGetApplicationBasicInfo(anyObject(), anyString(), anyString(), anyString())).
                thenReturn(TRUE);
        when(mockAppMgtSystemConfig.getApplicationDAO()).thenReturn(mockAbstractAppDAOImpl);
        when(mockAbstractAppDAOImpl.getApplicationBasicInfo(anyString())).thenReturn(applicationBasicInfo);

        Assert.assertNotNull(applicationManagementService.getApplicationBasicInfo(TENANT_DOMAIN, USERNAME, "*"));
        Assert.assertNotNull(applicationManagementService.getAllApplicationBasicInfo(TENANT_DOMAIN, USERNAME));
    }

    @Test
    public void testGetAllPaginatedApplicationBasicInfo() throws IdentityApplicationManagementException {

        mockApplicationMgtListeners();
        when(mockAppMgtListener.doPreGetPaginatedApplicationBasicInfo(anyString(), anyString(), anyInt())).
                thenReturn(TRUE);
        when(mockAppMgtListener.doPostGetPaginatedApplicationBasicInfo(anyString(), anyString(), anyInt(),
                anyObject())).thenReturn(TRUE);
        when(mockAppMgtSystemConfig.getApplicationDAO()).thenReturn(mockPaginatedAppDAO);
        when(mockPaginatedAppDAO.getAllPaginatedApplicationBasicInfo(anyInt())).thenReturn(applicationBasicInfo);

        Assert.assertNotNull(applicationManagementService.getAllPaginatedApplicationBasicInfo(TENANT_DOMAIN, USERNAME,
                123));
    }

    @Test
    public void testGetApplicationBasicInfoBasedOffsetLimit() throws IdentityApplicationManagementException {

        mockApplicationMgtListeners();
        when(mockAppMgtListener.doPreGetApplicationBasicInfo(anyString(), anyString(), anyInt(), anyInt())).
                thenReturn(TRUE);
        when(mockAppMgtListener.doPostGetApplicationBasicInfo(anyString(), anyString(), anyInt(), anyInt(),
                anyObject())).thenReturn(TRUE);
        when(mockAppMgtSystemConfig.getApplicationDAO()).thenReturn(mockPaginatedAppDAO);
        when(mockPaginatedAppDAO.getApplicationBasicInfo(anyInt(), anyInt())).thenReturn(applicationBasicInfo);

        Assert.assertNotNull(applicationManagementService.getApplicationBasicInfo(TENANT_DOMAIN, USERNAME,
                0, 0));
    }

    @Test
    public void testGetPaginatedApplicationBasicInfoBasedFilter() throws IdentityApplicationManagementException {

        mockApplicationMgtListeners();
        when(mockAppMgtListener.doPreGetPaginatedApplicationBasicInfo(anyString(), anyString(), anyInt(), anyString())).
                thenReturn(TRUE);
        when(mockAppMgtListener.doPostGetPaginatedApplicationBasicInfo(anyString(), anyString(), anyInt(), anyString(),
                anyObject())).thenReturn(TRUE);
        when(mockAppMgtSystemConfig.getApplicationDAO()).thenReturn(mockPaginatedAppDAO);
        when(mockPaginatedAppDAO.getPaginatedApplicationBasicInfo(anyInt(), anyString())).
                thenReturn(applicationBasicInfo);

        Assert.assertNotNull(applicationManagementService.getPaginatedApplicationBasicInfo(TENANT_DOMAIN, USERNAME,
                123, "*"));
    }

    @Test
    public void testGetApplicationBasicInfoBasedFilterOffsetLimit() throws IdentityApplicationManagementException {

        mockApplicationMgtListeners();
        when(mockAppMgtSystemConfig.getApplicationDAO()).thenReturn(mockPaginatedAppDAO);
        when(mockPaginatedAppDAO.getApplicationBasicInfo(anyString(), anyInt(), anyInt())).
                thenReturn(applicationBasicInfo);
        when(mockAppMgtListener.doPreGetApplicationBasicInfo(anyString(), anyString(), anyString(), anyInt(),
                anyInt())).thenReturn(TRUE);
        when(mockAppMgtListener.doPostGetApplicationBasicInfo(anyString(), anyString(), anyString(), anyInt(), anyInt(),
                anyObject())).thenReturn(TRUE);

        Assert.assertNotNull(applicationManagementService.getApplicationBasicInfo(TENANT_DOMAIN, USERNAME, "*",
                123, 1));
    }

    @Test
    public void testGetCountOfAllApplications() throws IdentityApplicationManagementException {

        when(mockAppMgtSystemConfig.getApplicationDAO()).thenReturn(mockPaginatedAppDAO);
        when(mockPaginatedAppDAO.getCountOfAllApplications()).thenReturn(10);

        Assert.assertEquals(10, applicationManagementService.getCountOfAllApplications(TENANT_DOMAIN, USERNAME));
    }

    @Test
    public void testGetCountOfApplications() throws IdentityApplicationManagementException {

        when(mockAppMgtSystemConfig.getApplicationDAO()).thenReturn(mockPaginatedAppDAO);
        when(mockPaginatedAppDAO.getCountOfApplications(anyString())).thenReturn(10);

        Assert.assertEquals(10, applicationManagementService.getCountOfApplications(TENANT_DOMAIN, USERNAME,
                "*"));
    }

    @Test
    public void testGetIdentityProvider() throws IdentityApplicationManagementException {

        when(mockAppMgtSystemConfig.getIdentityProviderDAO()).thenReturn(mockIdentityProviderDAO);
        when(mockIdentityProviderDAO.getIdentityProvider(anyString())).thenReturn(mockIdentityProvider);
        Assert.assertEquals(mockIdentityProvider, applicationManagementService.getIdentityProvider(FEDERATED_IDP_NAME,
                TENANT_DOMAIN));

        doThrow(new IdentityApplicationManagementException("")).when(mockIdentityProviderDAO).
                getIdentityProvider(anyString());
        try {
            applicationManagementService.getIdentityProvider(FEDERATED_IDP_NAME, TENANT_DOMAIN);
        } catch (IdentityApplicationManagementException e) {
            Assert.assertEquals("Error occurred while retrieving Identity Provider: " + FEDERATED_IDP_NAME + ". ",
                    e.getMessage());
        }
    }

    @Test
    public void testGetAllIdentityProviders() throws IdentityApplicationManagementException {

        when(mockAppMgtSystemConfig.getIdentityProviderDAO()).thenReturn(mockIdentityProviderDAO);
        List<IdentityProvider> fedIdpList = Collections.singletonList(mockIdentityProvider);
        when(mockIdentityProviderDAO.getAllIdentityProviders()).thenReturn(fedIdpList);
        Assert.assertEquals(mockIdentityProvider, applicationManagementService.getAllIdentityProviders
                (TENANT_DOMAIN)[0]);

        doThrow(new IdentityApplicationManagementException("")).when(mockIdentityProviderDAO).getAllIdentityProviders();
        try {
            applicationManagementService.getAllIdentityProviders(TENANT_DOMAIN);
        } catch (IdentityApplicationManagementException e) {
            Assert.assertEquals("Error occurred while retrieving all Identity Providers" + ". ",
                    e.getMessage());
        }
    }

    @Test
    public void testGetAllLocalAuthenticators() throws IdentityApplicationManagementException {

        when(mockAppMgtSystemConfig.getIdentityProviderDAO()).thenReturn(mockIdentityProviderDAO);
        List<LocalAuthenticatorConfig> localAuthenticators = Collections.singletonList(mockLocalAuthenticatorConfig);
        when(mockIdentityProviderDAO.getAllLocalAuthenticators()).thenReturn(localAuthenticators);
        Assert.assertEquals(mockLocalAuthenticatorConfig, applicationManagementService.getAllLocalAuthenticators
                (TENANT_DOMAIN)[0]);

        doThrow(new IdentityApplicationManagementException("")).when(mockIdentityProviderDAO).
                getAllLocalAuthenticators();
        try {
            applicationManagementService.getAllLocalAuthenticators(TENANT_DOMAIN);
        } catch (IdentityApplicationManagementException e) {
            Assert.assertEquals("Error occurred while retrieving all Local Authenticators" + ". ",
                    e.getMessage());
        }
    }

    @Test
    public void testGetAllRequestPathAuthenticators() throws IdentityApplicationManagementException {

        when(mockAppMgtSystemConfig.getIdentityProviderDAO()).thenReturn(mockIdentityProviderDAO);
        List<RequestPathAuthenticatorConfig> reqPathAuthenticators = Collections.singletonList
                (mockReqPathAuthenticatorConfig);
        when(mockIdentityProviderDAO.getAllRequestPathAuthenticators()).thenReturn(reqPathAuthenticators);
        Assert.assertEquals(mockReqPathAuthenticatorConfig, applicationManagementService.getAllRequestPathAuthenticators
                (TENANT_DOMAIN)[0]);

        doThrow(new IdentityApplicationManagementException("")).when(mockIdentityProviderDAO).
                getAllRequestPathAuthenticators();
        try {
            applicationManagementService.getAllRequestPathAuthenticators(TENANT_DOMAIN);
        } catch (IdentityApplicationManagementException e) {
            Assert.assertEquals("Error occurred while retrieving all Request Path Authenticators" + ". ",
                    e.getMessage());
        }
    }

    @Test
    public void testGetServiceProviderNameByClientIdExcludingFileBasedSPs() throws
            IdentityApplicationManagementException {

        mockApplicationMgtListeners();
        when(mockAppMgtListener.doPreGetServiceProviderNameByClientIdExcludingFileBasedSPs(anyString(), anyString(),
                anyString(), anyString())).thenReturn(TRUE);
        when(mockAppMgtListener.doPostGetServiceProviderNameByClientIdExcludingFileBasedSPs(anyString(), anyString(),
                anyString(), anyString())).thenReturn(TRUE);
        when(mockAppMgtSystemConfig.getApplicationDAO()).thenReturn(mockApplicationDAO);
        when(mockApplicationDAO.getServiceProviderNameByClientId(anyString(), anyString(), anyString())).
                thenReturn(APPLICATION_NAME);
        Assert.assertEquals(APPLICATION_NAME, applicationManagementService.
                getServiceProviderNameByClientIdExcludingFileBasedSPs("clientId", "type", TENANT_DOMAIN));

        doThrow(new IdentityApplicationManagementException("")).when(mockApplicationDAO).
                getServiceProviderNameByClientId(anyString(), anyString(), anyString());
        try {
            applicationManagementService.getServiceProviderNameByClientIdExcludingFileBasedSPs("clientId",
                    "type", TENANT_DOMAIN);
        } catch (IdentityApplicationManagementException e) {
            Assert.assertEquals("Error occurred while retrieving the service provider for client id :  " +
                            "clientId. ", e.getMessage());
        }
    }

    @Test
    public void testGetServiceProviderToLocalIdPClaimMapping() throws IdentityApplicationManagementException {

        mockFileBasedSPs();
        when(mockAppMgtSystemConfig.getApplicationDAO()).thenReturn(mockApplicationDAO);
        when(mockApplicationDAO.getServiceProviderToLocalIdPClaimMapping(APPLICATION_NAME, TENANT_DOMAIN)).
                thenReturn(mockClaimMap);
        when(mockClaimMap.isEmpty()).thenReturn(TRUE);
        when(mockFileBasedSPs.containsKey(APPLICATION_NAME)).thenReturn(TRUE);
        when(mockFileBasedSPs.get(APPLICATION_NAME)).thenReturn(mockServiceProvider);

        Assert.assertNotNull(applicationManagementService.getServiceProviderToLocalIdPClaimMapping(APPLICATION_NAME,
                TENANT_DOMAIN));
    }

    @Test
    public void testGetLocalIdPToServiceProviderClaimMapping() throws IdentityApplicationManagementException {

        mockFileBasedSPs();
        when(mockAppMgtSystemConfig.getApplicationDAO()).thenReturn(mockApplicationDAO);
        when(mockApplicationDAO.getLocalIdPToServiceProviderClaimMapping(APPLICATION_NAME, TENANT_DOMAIN)).
                thenReturn(mockClaimMap);
        when(mockClaimMap.isEmpty()).thenReturn(TRUE);
        when(mockFileBasedSPs.containsKey(APPLICATION_NAME)).thenReturn(TRUE);
        when(mockFileBasedSPs.get(APPLICATION_NAME)).thenReturn(mockServiceProvider);

        Assert.assertNotNull(applicationManagementService.getLocalIdPToServiceProviderClaimMapping(APPLICATION_NAME,
                TENANT_DOMAIN));
    }

    @Test
    public void testGetSystemApplications() {

        mockStatic(IdentityConfigParser.class);
        when(IdentityConfigParser.getInstance()).thenReturn(mockConfigParser);

        when(mockConfigParser.getConfigElement(anyString())).thenReturn(null);
        Assert.assertEquals(Collections.emptySet(), applicationManagementService.getSystemApplications());

        when(mockConfigParser.getConfigElement(anyString())).thenReturn(mockSystemApplicationsConfig);
        Assert.assertEquals(Collections.emptySet(), applicationManagementService.getSystemApplications());

        List<OMElement> applicationIdentifierConfigs = Collections.singletonList(mockChildApplicationConfig);
        Iterator<OMElement> applicationIdentifierIterator = applicationIdentifierConfigs.iterator();
        when(mockSystemApplicationsConfig.getChildrenWithLocalName(anyString())).
                thenReturn(applicationIdentifierIterator);
        when(mockChildApplicationConfig.getText()).thenReturn(APPLICATION_NAME);
        Assert.assertEquals(APPLICATION_NAME, applicationManagementService.getSystemApplications().toArray()[0]);
    }
}
