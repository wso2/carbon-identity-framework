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

import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.modules.testng.PowerMockTestCase;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.wso2.carbon.base.CarbonBaseConstants;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.context.RegistryType;
import org.wso2.carbon.context.internal.OSGiDataHolder;
import org.wso2.carbon.core.internal.CarbonCoreDataHolder;
import org.wso2.carbon.identity.application.common.ApplicationAuthenticatorService;
import org.wso2.carbon.identity.application.common.IdentityApplicationManagementClientException;
import org.wso2.carbon.identity.application.common.IdentityApplicationManagementException;
import org.wso2.carbon.identity.application.common.model.ApplicationBasicInfo;
import org.wso2.carbon.identity.application.common.model.AuthenticationStep;
import org.wso2.carbon.identity.application.common.model.Claim;
import org.wso2.carbon.identity.application.common.model.ClaimConfig;
import org.wso2.carbon.identity.application.common.model.ClaimMapping;
import org.wso2.carbon.identity.application.common.model.FederatedAuthenticatorConfig;
import org.wso2.carbon.identity.application.common.model.IdentityProvider;
import org.wso2.carbon.identity.application.common.model.InboundAuthenticationConfig;
import org.wso2.carbon.identity.application.common.model.InboundAuthenticationRequestConfig;
import org.wso2.carbon.identity.application.common.model.InboundProvisioningConfig;
import org.wso2.carbon.identity.application.common.model.LocalAndOutboundAuthenticationConfig;
import org.wso2.carbon.identity.application.common.model.LocalAuthenticatorConfig;
import org.wso2.carbon.identity.application.common.model.LocalRole;
import org.wso2.carbon.identity.application.common.model.OutboundProvisioningConfig;
import org.wso2.carbon.identity.application.common.model.PermissionsAndRoleConfig;
import org.wso2.carbon.identity.application.common.model.ProvisioningConnectorConfig;
import org.wso2.carbon.identity.application.common.model.RequestPathAuthenticatorConfig;
import org.wso2.carbon.identity.application.common.model.RoleMapping;
import org.wso2.carbon.identity.application.common.model.ServiceProvider;
import org.wso2.carbon.identity.application.mgt.internal.ApplicationManagementServiceComponentHolder;
import org.wso2.carbon.identity.common.testng.WithH2Database;
import org.wso2.carbon.identity.common.testng.realm.InMemoryRealmService;
import org.wso2.carbon.identity.common.testng.realm.MockUserStoreManager;
import org.wso2.carbon.identity.core.internal.IdentityCoreServiceDataHolder;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;
import org.wso2.carbon.idp.mgt.IdentityProviderManagementException;
import org.wso2.carbon.idp.mgt.dao.IdPManagementDAO;
import org.wso2.carbon.registry.core.Collection;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.internal.RegistryDataHolder;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.registry.core.session.UserRegistry;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.api.UserStoreManager;
import org.wso2.carbon.user.core.service.RealmService;

import java.lang.reflect.Field;
import java.nio.file.Paths;

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.when;
import static org.wso2.carbon.CarbonConstants.REGISTRY_SYSTEM_USERNAME;
import static org.wso2.carbon.utils.multitenancy.MultitenantConstants.SUPER_TENANT_DOMAIN_NAME;
import static org.wso2.carbon.utils.multitenancy.MultitenantConstants.SUPER_TENANT_ID;

/*
  Unit tests for ApplicationManagementServiceImpl.
 */
@Test
@WithH2Database(jndiName = "jdbc/WSO2IdentityDB", files = {"dbscripts/identity.sql"})
@PowerMockIgnore({"org.mockito.*"})
public class ApplicationManagementServiceImplTest extends PowerMockTestCase {

    private static final String SAMPLE_TENANT_DOMAIN = "tenant domain";
    private static final String APPLICATION_NAME_1 = "Test application 1";
    private static final String APPLICATION_NAME_2 = "Test application 2";
    private static final String IDP_NAME_1 = "Test IdP 1";
    private static final String IDP_NAME_2 = "Test IdP 2";
    private static final String USERNAME_1 = "user 1";
    private static final String USERNAME_2 = "user 2";

    private IdPManagementDAO idPManagementDAO;
    private ApplicationManagementServiceImpl applicationManagementService;

    @BeforeClass
    public void setup() throws RegistryException, UserStoreException {

        setupConfiguration();
        applicationManagementService = ApplicationManagementServiceImpl.getInstance();
    }

    @DataProvider(name = "addApplicationDataProvider")
    public Object[][] addApplicationDataProvider() {

        ServiceProvider serviceProvider1 = new ServiceProvider();
        serviceProvider1.setApplicationName(APPLICATION_NAME_1);

        ServiceProvider serviceProvider2 = new ServiceProvider();
        serviceProvider2.setApplicationName(APPLICATION_NAME_2);

        return new Object[][]{
                {serviceProvider1, SUPER_TENANT_DOMAIN_NAME, USERNAME_1},
                {serviceProvider2, SAMPLE_TENANT_DOMAIN, USERNAME_2}
        };
    }

    @Test(dataProvider = "addApplicationDataProvider")
    public void testAddApplication(Object serviceProvider, String tenantDomain, String username)
            throws Exception {

        ServiceProvider inputSP = (ServiceProvider) serviceProvider;
        addApplicationConfigurations(inputSP);

        // Adding new application.
        ServiceProvider addedSP = applicationManagementService.addApplication(inputSP, tenantDomain,
                username);

        Assert.assertEquals(addedSP.getApplicationName(), inputSP.getApplicationName());
        Assert.assertEquals(addedSP.getOwner().getUserName(), inputSP.getOwner().getUserName());

        //  Retrieving added application.
        ServiceProvider retrievedSP = applicationManagementService.getApplicationExcludingFileBasedSPs
                (inputSP.getApplicationName(), tenantDomain);

        Assert.assertEquals(retrievedSP.getApplicationID(), inputSP.getApplicationID());
        Assert.assertEquals(retrievedSP.getOwner().getUserName(), inputSP.getOwner().getUserName());
        Assert.assertFalse(retrievedSP.isManagementApp());

        // Deleting added application.
        applicationManagementService.deleteApplication(inputSP.getApplicationName(), tenantDomain, username);
    }

    @DataProvider(name = "addApplicationNullAppNameDataProvider")
    public Object[][] addApplicationNullAppNameDataProvider() {

        ServiceProvider serviceProvider1 = new ServiceProvider();

        ServiceProvider serviceProvider2 = new ServiceProvider();
        serviceProvider2.setApplicationName("");

        ServiceProvider serviceProvider3 = new ServiceProvider();
        serviceProvider3.setApplicationName(null);

        return new Object[][]{
                {serviceProvider1, SUPER_TENANT_DOMAIN_NAME, USERNAME_1},
                {serviceProvider2, SUPER_TENANT_DOMAIN_NAME, USERNAME_1},
                {serviceProvider3, SUPER_TENANT_DOMAIN_NAME, USERNAME_1}
        };
    }

    @Test(dataProvider = "addApplicationNullAppNameDataProvider")
    public void testAddApplicationWithNullAppName(Object serviceProvider, String tenantDomain, String username) {

        Assert.assertThrows(IdentityApplicationManagementClientException.class, () -> applicationManagementService.
                addApplication((ServiceProvider) serviceProvider, tenantDomain, username));
    }

    @DataProvider(name = "addApplicationInvalidAppNameDataProvider")
    public Object[][] addApplicationInvalidAppNameDataProvider() {

        ServiceProvider serviceProvider1 = new ServiceProvider();
        serviceProvider1.setApplicationName("@#!app");

        ServiceProvider serviceProvider2 = new ServiceProvider();
        serviceProvider2.setApplicationName("1234@");

        return new Object[][]{
                {serviceProvider1, SUPER_TENANT_DOMAIN_NAME, USERNAME_1},
                {serviceProvider2, SUPER_TENANT_DOMAIN_NAME, USERNAME_1}
        };
    }

    @Test(dataProvider = "addApplicationInvalidAppNameDataProvider")
    public void testAddApplicationWithInvalidAppName(Object serviceProvider, String tenantDomain, String username) {

        Assert.assertThrows(IdentityApplicationManagementClientException.class, () -> applicationManagementService.
                addApplication((ServiceProvider) serviceProvider, tenantDomain, username));
    }

    @DataProvider(name = "addApplicationWithExistingAppNameDataProvider")
    public Object[][] addApplicationWithExistingAppNameDataProvider() {

        ServiceProvider serviceProvider = new ServiceProvider();
        serviceProvider.setApplicationName(APPLICATION_NAME_1);

        ServiceProvider newServiceProvider = new ServiceProvider();
        newServiceProvider.setApplicationName(APPLICATION_NAME_1);

        return new Object[][]{
                {serviceProvider, newServiceProvider, SUPER_TENANT_DOMAIN_NAME, USERNAME_1}
        };
    }

    @Test(dataProvider = "addApplicationWithExistingAppNameDataProvider")
    public void testAddApplicationWithExistingAppName(Object serviceProvider, Object newServiceProvider,
                                                      String tenantDomain, String username) throws
            IdentityApplicationManagementException {

        applicationManagementService.addApplication((ServiceProvider) serviceProvider, tenantDomain, username);

        Assert.assertThrows(IdentityApplicationManagementClientException.class, () -> applicationManagementService.
                addApplication((ServiceProvider) newServiceProvider, tenantDomain, username));
    }

    @DataProvider(name = "getApplicationDataProvider")
    public Object[][] getApplicationDataProvider() {

        ServiceProvider serviceProvider1 = new ServiceProvider();
        serviceProvider1.setApplicationName(APPLICATION_NAME_1);

        ServiceProvider serviceProvider2 = new ServiceProvider();
        serviceProvider2.setApplicationName(APPLICATION_NAME_2);

        return new Object[][]{
                {serviceProvider1, SUPER_TENANT_DOMAIN_NAME, USERNAME_1},
                {serviceProvider2, SAMPLE_TENANT_DOMAIN, USERNAME_2}
        };
    }

    @Test(dataProvider = "getApplicationDataProvider")
    public void testGetApplicationBasicInfoWithFilter(Object serviceProvider, String tenantDomain, String username)
            throws IdentityApplicationManagementException {

        ServiceProvider inputSP = (ServiceProvider) serviceProvider;

        // Adding new application.
        ServiceProvider addedSP = applicationManagementService.addApplication(inputSP, tenantDomain,
                username);

        // Retrieving added application info.
        ApplicationBasicInfo[] applicationBasicInfo = applicationManagementService.getApplicationBasicInfo
                (tenantDomain, username, inputSP.getApplicationName());
        Assert.assertEquals(applicationBasicInfo[0].getApplicationName(), inputSP.getApplicationName());
        Assert.assertEquals(applicationBasicInfo[0].getApplicationName(), addedSP.getApplicationName());

        // Deleting added application.
        applicationManagementService.deleteApplication(inputSP.getApplicationName(), tenantDomain, username);
    }

    @Test(dataProvider = "getApplicationDataProvider")
    public void testGetPaginatedApplicationBasicInfo(Object serviceProvider, String tenantDomain, String username)
            throws IdentityApplicationManagementException {

        ServiceProvider inputSP = (ServiceProvider) serviceProvider;

        // Adding new application.
        ServiceProvider addedSP = applicationManagementService.addApplication(inputSP, tenantDomain,
                username);

        // Retrieving added application info.
        ApplicationBasicInfo[] applicationBasicInfo = applicationManagementService.getPaginatedApplicationBasicInfo
                (tenantDomain, username, 1, inputSP.getApplicationName());
        Assert.assertEquals(applicationBasicInfo[0].getApplicationName(), inputSP.getApplicationName());
        Assert.assertEquals(applicationBasicInfo[0].getApplicationName(), addedSP.getApplicationName());

        // Deleting added application.
        applicationManagementService.deleteApplication(inputSP.getApplicationName(), tenantDomain, username);
    }

    private void addApplications() throws IdentityApplicationManagementException {

        ServiceProvider serviceProvider1 = new ServiceProvider();
        serviceProvider1.setApplicationName(APPLICATION_NAME_1);

        ServiceProvider serviceProvider2 = new ServiceProvider();
        serviceProvider2.setApplicationName(APPLICATION_NAME_2);

        applicationManagementService.addApplication(serviceProvider1, SUPER_TENANT_DOMAIN_NAME, USERNAME_1);
        applicationManagementService.addApplication(serviceProvider2, SUPER_TENANT_DOMAIN_NAME, USERNAME_1);
    }

    @Test
    public void testGetAllApplicationBasicInfo() throws IdentityApplicationManagementException {

        addApplications();
        ApplicationBasicInfo[] applicationBasicInfo = applicationManagementService.getAllApplicationBasicInfo
                (SUPER_TENANT_DOMAIN_NAME, USERNAME_1);

        Assert.assertEquals(applicationBasicInfo.length, 2);
        Assert.assertEquals(applicationBasicInfo[0].getApplicationName(), APPLICATION_NAME_2);
        Assert.assertEquals(applicationBasicInfo[1].getApplicationName(), APPLICATION_NAME_1);

        // Deleting all added applications.
        applicationManagementService.deleteApplications(SUPER_TENANT_ID);
    }

    @Test
    public void testGetAllPaginatedApplicationBasicInfo() throws IdentityApplicationManagementException {

        addApplications();
        ApplicationBasicInfo[] applicationBasicInfo = applicationManagementService.getAllPaginatedApplicationBasicInfo
                (SUPER_TENANT_DOMAIN_NAME, USERNAME_1, 1);

        Assert.assertEquals(applicationBasicInfo.length, 2);
        Assert.assertEquals(applicationBasicInfo[0].getApplicationName(), APPLICATION_NAME_2);
        Assert.assertEquals(applicationBasicInfo[1].getApplicationName(), APPLICATION_NAME_1);

        // Deleting all added applications.
        applicationManagementService.deleteApplications(SUPER_TENANT_ID);
    }

    @Test
    public void testGetApplicationBasicInfoOffsetLimit() throws IdentityApplicationManagementException {

        addApplications();

        ApplicationBasicInfo[] applicationBasicInfo1 = applicationManagementService.getApplicationBasicInfo
                (SUPER_TENANT_DOMAIN_NAME, USERNAME_1, 0, 1);
        Assert.assertEquals(applicationBasicInfo1[0].getApplicationName(), APPLICATION_NAME_2);

        ApplicationBasicInfo[] applicationBasicInfo2 = applicationManagementService.getApplicationBasicInfo
                (SUPER_TENANT_DOMAIN_NAME, USERNAME_1, 1, 1);
        Assert.assertEquals(applicationBasicInfo2[0].getApplicationName(), APPLICATION_NAME_1);

        // Deleting all added applications.
        applicationManagementService.deleteApplications(SUPER_TENANT_ID);
    }

    @Test
    public void testGetApplicationBasicInfoFilterOffsetLimit() throws IdentityApplicationManagementException {

        addApplications();

        ApplicationBasicInfo[] applicationBasicInfo1 = applicationManagementService.getApplicationBasicInfo
                (SUPER_TENANT_DOMAIN_NAME, USERNAME_1, APPLICATION_NAME_2, 0, 1);
        Assert.assertEquals(applicationBasicInfo1[0].getApplicationName(), APPLICATION_NAME_2);

        ApplicationBasicInfo[] applicationBasicInfo2 = applicationManagementService.getApplicationBasicInfo
                (SUPER_TENANT_DOMAIN_NAME, USERNAME_1, APPLICATION_NAME_1, 0, 1);
        Assert.assertEquals(applicationBasicInfo2[0].getApplicationName(), APPLICATION_NAME_1);

        // Deleting all added applications.
        applicationManagementService.deleteApplications(SUPER_TENANT_ID);
    }

    @Test
    public void testGetCountOfAllApplications() throws IdentityApplicationManagementException {

        addApplications();
        Assert.assertEquals(applicationManagementService.getCountOfAllApplications(SUPER_TENANT_DOMAIN_NAME,
                USERNAME_1), 2);

        // Deleting all added applications.
        applicationManagementService.deleteApplications(SUPER_TENANT_ID);
    }

    @Test
    public void testGetCountOfApplicationsFilter() throws IdentityApplicationManagementException {

        addApplications();
        Assert.assertEquals(applicationManagementService.getCountOfApplications(SUPER_TENANT_DOMAIN_NAME,
                USERNAME_1, APPLICATION_NAME_1), 1);
        Assert.assertEquals(applicationManagementService.getCountOfApplications(SUPER_TENANT_DOMAIN_NAME,
                USERNAME_1, APPLICATION_NAME_2), 1);

        // Deleting all added applications.
        applicationManagementService.deleteApplications(SUPER_TENANT_ID);
    }

    @DataProvider(name = "getIdentityProviderDataProvider")
    public Object[][] getIdentityProviderDataProvider() {

        IdentityProvider idp1 = new IdentityProvider();
        idp1.setIdentityProviderName(IDP_NAME_1);

        IdentityProvider idp2 = new IdentityProvider();
        idp2.setIdentityProviderName(IDP_NAME_2);

        return new Object[][]{
                {idp1, SUPER_TENANT_DOMAIN_NAME, SUPER_TENANT_ID},
                {idp2, SUPER_TENANT_DOMAIN_NAME, SUPER_TENANT_ID}
        };
    }

    @Test(dataProvider = "getIdentityProviderDataProvider")
    public void testGetIdentityProvider(Object idp, String tenantDomain, int tenantId) throws
            IdentityApplicationManagementException,
            IdentityProviderManagementException {

        idPManagementDAO = new IdPManagementDAO();
        idPManagementDAO.addIdP((IdentityProvider) idp, tenantId);

        IdentityProvider identityProvider = applicationManagementService.getIdentityProvider
                (((IdentityProvider) idp).getIdentityProviderName(), tenantDomain);
        Assert.assertEquals(identityProvider.getIdentityProviderName(), ((IdentityProvider) idp).
                getIdentityProviderName());

        // Deleting added identity provider.
        idPManagementDAO.deleteIdP(identityProvider.getIdentityProviderName(), tenantId, tenantDomain);
    }

    private void addIdentityProviders() throws IdentityProviderManagementException {

        idPManagementDAO = new IdPManagementDAO();

        IdentityProvider idp1 = new IdentityProvider();
        idp1.setIdentityProviderName(IDP_NAME_1);

        IdentityProvider idp2 = new IdentityProvider();
        idp2.setIdentityProviderName(IDP_NAME_2);

        idPManagementDAO.addIdP(idp1, SUPER_TENANT_ID);
        idPManagementDAO.addIdP(idp2, SUPER_TENANT_ID);
    }

    @Test
    public void testGetAllIdentityProviders() throws IdentityApplicationManagementException,
            IdentityProviderManagementException {

        addIdentityProviders();

        IdentityProvider[] identityProviders = applicationManagementService.
                getAllIdentityProviders(SUPER_TENANT_DOMAIN_NAME);
        Assert.assertEquals(identityProviders[0].getIdentityProviderName(), IDP_NAME_1);
        Assert.assertEquals(identityProviders[1].getIdentityProviderName(), IDP_NAME_2);

        // Deleting added all identity providers.
        idPManagementDAO.deleteIdPs(SUPER_TENANT_ID);
    }

    @Test
    public void testGetAllLocalAuthenticators() throws IdentityApplicationManagementException {

        ApplicationAuthenticatorService appAuthenticatorService = ApplicationAuthenticatorService.getInstance();
        LocalAuthenticatorConfig localAuthenticatorConfig = new LocalAuthenticatorConfig();
        appAuthenticatorService.addLocalAuthenticator(localAuthenticatorConfig);

        LocalAuthenticatorConfig[] localAuthenticatorConfigs = applicationManagementService.getAllLocalAuthenticators
                (SUPER_TENANT_DOMAIN_NAME);

        Assert.assertEquals(localAuthenticatorConfigs[0], localAuthenticatorConfig);
    }

    @Test
    public void testGetAllRequestPathAuthenticators() throws IdentityApplicationManagementException {

        ApplicationAuthenticatorService appAuthenticatorService = ApplicationAuthenticatorService.getInstance();
        RequestPathAuthenticatorConfig requestPathAuthenticatorConfig = new RequestPathAuthenticatorConfig();
        appAuthenticatorService.addRequestPathAuthenticator(requestPathAuthenticatorConfig);

        RequestPathAuthenticatorConfig[] requestPathAuthenticatorConfigs = applicationManagementService.
                getAllRequestPathAuthenticators(SUPER_TENANT_DOMAIN_NAME);

        Assert.assertEquals(requestPathAuthenticatorConfigs[0], requestPathAuthenticatorConfig);
    }

    @Test(dataProvider = "addApplicationDataProvider")
    public void testCreateApplication(Object serviceProvider, String tenantDomain, String username) throws
            IdentityApplicationManagementException {

        // Adding application.
        addApplicationConfigurations((ServiceProvider) serviceProvider);
        String resourceId = applicationManagementService.createApplication((ServiceProvider) serviceProvider,
                tenantDomain, username);

        // Retrieving application by ResourceId
        ServiceProvider expectedSP = applicationManagementService.getApplicationByResourceId(resourceId,
                tenantDomain);
        Assert.assertEquals(resourceId, expectedSP.getApplicationResourceId());

        ApplicationBasicInfo applicationBasicInfo = applicationManagementService.getApplicationBasicInfoByResourceId
                (resourceId, tenantDomain);
        Assert.assertEquals(applicationBasicInfo.getApplicationName(), ((ServiceProvider) serviceProvider).
                getApplicationName());

        // Deleting added application
        applicationManagementService.deleteApplicationByResourceId(resourceId, tenantDomain, username);
    }

    @Test
    public void testGetServiceProviderByClientId() throws IdentityApplicationManagementException {

        ServiceProvider inputSP = new ServiceProvider();
        inputSP.setApplicationName(APPLICATION_NAME_1);
        addApplicationConfigurations(inputSP);

        // Adding application.
        applicationManagementService.createApplication(inputSP, SUPER_TENANT_DOMAIN_NAME, USERNAME_1);

        // Retrieving application by ResourceId.
        ServiceProvider actual = applicationManagementService.getServiceProviderByClientId("auth key",
                "oauth2", SUPER_TENANT_DOMAIN_NAME);

        Assert.assertEquals(actual.getApplicationName(), inputSP.getApplicationName());
        Assert.assertEquals(actual.getOwner().getUserName(), USERNAME_1);
        Assert.assertEquals(actual.getDescription(), inputSP.getDescription());

        // Deleting added application.
        applicationManagementService.deleteApplications(SUPER_TENANT_ID);
    }

    @DataProvider(name = "testAddApplicationWithIsManagementApplicationData")
    public Object[][] testAddApplicationWithIsManagementApplicationData() {


        return new Object[][]{
                {true},
                {false}
        };
    }

    @Test(dataProvider = "testAddApplicationWithIsManagementApplicationData")
    public void testAddApplicationWithIsManagementApplication(boolean isManagementApp) throws Exception {

        ServiceProvider inputSP = new ServiceProvider();
        inputSP.setApplicationName(APPLICATION_NAME_1);

        addApplicationConfigurations(inputSP);
        inputSP.setManagementApp(isManagementApp);

        // Adding new application.
        ServiceProvider addedSP = applicationManagementService.addApplication(inputSP, SUPER_TENANT_DOMAIN_NAME,
                REGISTRY_SYSTEM_USERNAME);
        Assert.assertEquals(addedSP.isManagementApp(), isManagementApp);


        //  Retrieving added application.
        ServiceProvider retrievedSP = applicationManagementService.getApplicationExcludingFileBasedSPs
                (inputSP.getApplicationName(), SUPER_TENANT_DOMAIN_NAME);
        Assert.assertEquals(retrievedSP.isManagementApp(), isManagementApp);

        // Updating the application by changing the isManagementApplication flag. It should not be changed.
        inputSP.setManagementApp(!isManagementApp);

        applicationManagementService.updateApplication(inputSP, SUPER_TENANT_DOMAIN_NAME, REGISTRY_SYSTEM_USERNAME);

        retrievedSP = applicationManagementService.getApplicationExcludingFileBasedSPs
                (inputSP.getApplicationName(), SUPER_TENANT_DOMAIN_NAME);

        Assert.assertEquals(retrievedSP.isManagementApp(), isManagementApp);

        // Deleting added application.
        applicationManagementService.deleteApplication(inputSP.getApplicationName(), SUPER_TENANT_DOMAIN_NAME,
                REGISTRY_SYSTEM_USERNAME);
    }

    private void addApplicationConfigurations(ServiceProvider serviceProvider) {

        serviceProvider.setDescription("Created for testing");
        serviceProvider.setSaasApp(TRUE);

        // Inbound Authentication Configurations.
        InboundAuthenticationConfig inboundAuthenticationConfig = new InboundAuthenticationConfig();
        InboundAuthenticationRequestConfig authRequestConfig = new InboundAuthenticationRequestConfig();
        authRequestConfig.setInboundAuthKey("auth key");
        authRequestConfig.setInboundAuthType("oauth2");
        InboundAuthenticationRequestConfig[] authRequests = new InboundAuthenticationRequestConfig[]
                {authRequestConfig};
        inboundAuthenticationConfig.setInboundAuthenticationRequestConfigs(authRequests);
        serviceProvider.setInboundAuthenticationConfig(inboundAuthenticationConfig);

        // Inbound Provisioning Configurations.
        InboundProvisioningConfig provisioningConfig = new InboundProvisioningConfig();
        provisioningConfig.setProvisioningUserStore("UserStore");
        serviceProvider.setInboundProvisioningConfig(provisioningConfig);

        // OutBound Provisioning Configurations.
        IdentityProvider provisioningIdP = new IdentityProvider();
        provisioningIdP.setIdentityProviderName("Provisioning IdP");
        OutboundProvisioningConfig outboundProvisioningConfig = new OutboundProvisioningConfig();
        outboundProvisioningConfig.setProvisioningIdentityProviders(new IdentityProvider[]{provisioningIdP});
        ProvisioningConnectorConfig provisioningConnectorConfig = new ProvisioningConnectorConfig();
        provisioningConnectorConfig.setName("Provisioning connector");
        provisioningIdP.setDefaultProvisioningConnectorConfig(provisioningConnectorConfig);
        serviceProvider.setOutboundProvisioningConfig(outboundProvisioningConfig);

        // Local And OutBound Authentication Configuration.
        LocalAndOutboundAuthenticationConfig authenticationConfig = new LocalAndOutboundAuthenticationConfig();
        AuthenticationStep authenticationStep = new AuthenticationStep();
        IdentityProvider identityProvider = new IdentityProvider();
        identityProvider.setIdentityProviderName(IDP_NAME_1);
        FederatedAuthenticatorConfig federatedAuthenticatorConfig = new FederatedAuthenticatorConfig();
        federatedAuthenticatorConfig.setName("Federated authenticator");
        identityProvider.setFederatedAuthenticatorConfigs(new FederatedAuthenticatorConfig[]
                {federatedAuthenticatorConfig});
        authenticationStep.setFederatedIdentityProviders(new IdentityProvider[]{identityProvider});
        LocalAuthenticatorConfig localAuthenticatorConfig = new LocalAuthenticatorConfig();
        localAuthenticatorConfig.setName("Local authenticator");
        authenticationStep.setLocalAuthenticatorConfigs(new LocalAuthenticatorConfig[]{localAuthenticatorConfig});
        authenticationConfig.setAuthenticationSteps(new AuthenticationStep[]{authenticationStep});
        serviceProvider.setLocalAndOutBoundAuthenticationConfig(authenticationConfig);

        // Request Path Authenticator Configuration.
        RequestPathAuthenticatorConfig requestPathAuthenticatorConfig = new RequestPathAuthenticatorConfig();
        requestPathAuthenticatorConfig.setName("Request path authenticator");
        serviceProvider.setRequestPathAuthenticatorConfigs(new RequestPathAuthenticatorConfig[]{
                requestPathAuthenticatorConfig});

        // Claim Configurations.
        ClaimConfig claimConfig = new ClaimConfig();
        claimConfig.setRoleClaimURI("Role claim uri");
        claimConfig.setSpClaimDialects(new String[]{"SP claim dialect"});
        ClaimMapping claimMapping = new ClaimMapping();
        Claim localClaim = new Claim();
        localClaim.setClaimUri("Local claim uri");
        Claim remoteClaim = new Claim();
        remoteClaim.setClaimUri("Remote claim uri");
        claimMapping.setLocalClaim(localClaim);
        claimMapping.setRemoteClaim(remoteClaim);
        claimConfig.setClaimMappings(new ClaimMapping[]{claimMapping});
        serviceProvider.setClaimConfig(claimConfig);

        // Permission Role Configurations.
        PermissionsAndRoleConfig permissionsAndRoleConfig = new PermissionsAndRoleConfig();
        RoleMapping roleMapping = new RoleMapping();
        LocalRole localRole = new LocalRole("Local role");
        roleMapping.setLocalRole(localRole);
        roleMapping.setRemoteRole("Remote role");
        RoleMapping[] roleMappings = new RoleMapping[]{roleMapping};
        permissionsAndRoleConfig.setRoleMappings(roleMappings);
    }

    private void setupConfiguration() throws UserStoreException, RegistryException {

        String carbonHome = Paths.get(System.getProperty("user.dir"), "target", "test-classes", "repository").
                toString();
        System.setProperty(CarbonBaseConstants.CARBON_HOME, carbonHome);
        System.setProperty(CarbonBaseConstants.CARBON_CONFIG_DIR_PATH, Paths.get(carbonHome, "conf").toString());

        PrivilegedCarbonContext.startTenantFlow();
        PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(SUPER_TENANT_DOMAIN_NAME);
        PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantId(SUPER_TENANT_ID);
        PrivilegedCarbonContext.getThreadLocalCarbonContext();

        // Configure RealmService.
        PrivilegedCarbonContext.startTenantFlow();
        PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantId(SUPER_TENANT_ID);
        PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(SUPER_TENANT_DOMAIN_NAME);
        InMemoryRealmService testSessionRealmService = new InMemoryRealmService(SUPER_TENANT_ID);
        UserStoreManager userStoreManager = testSessionRealmService.getTenantUserRealm(SUPER_TENANT_ID)
                .getUserStoreManager();
        ((MockUserStoreManager) userStoreManager)
                .addSecondaryUserStoreManager("PRIMARY", (MockUserStoreManager) userStoreManager);
        IdentityTenantUtil.setRealmService(testSessionRealmService);
        RegistryDataHolder.getInstance().setRealmService(testSessionRealmService);
        OSGiDataHolder.getInstance().setUserRealmService(testSessionRealmService);
        IdentityCoreServiceDataHolder.getInstance().setRealmService(testSessionRealmService);
        ApplicationManagementServiceComponentHolder holder = ApplicationManagementServiceComponentHolder.getInstance();
        setInstanceValue(testSessionRealmService, RealmService.class, ApplicationManagementServiceComponentHolder.class,
                holder);

       // Configure Registry Service.
        RegistryService mockRegistryService = mock(RegistryService.class);
        UserRegistry mockRegistry = mock(UserRegistry.class);
        when(mockRegistryService.getGovernanceUserRegistry(anyString(), anyInt())).thenReturn(mockRegistry);
        OSGiDataHolder.getInstance().setRegistryService(mockRegistryService);
        CarbonCoreDataHolder.getInstance().setRegistryService(mockRegistryService);
        PrivilegedCarbonContext.getThreadLocalCarbonContext()
                .setRegistry(RegistryType.USER_GOVERNANCE, mockRegistryService.getRegistry());
        when(mockRegistry.resourceExists(anyString())).thenReturn(FALSE);
        Collection mockPermissionNode = mock(Collection.class);
        when(mockRegistry.newCollection()).thenReturn(mockPermissionNode);
        when(mockRegistry.get(anyString())).thenReturn(mockPermissionNode);
        when(CarbonContext.getThreadLocalCarbonContext().getRegistry(
                RegistryType.USER_GOVERNANCE)).thenReturn(mockRegistry);
        when(mockRegistry.resourceExists(anyString())).thenReturn(FALSE);
    }

    private void setInstanceValue(Object value, Class valueType, Class clazz, Object instance) {

        for (Field field1 : clazz.getDeclaredFields()) {
            if (field1.getType().isAssignableFrom(valueType)) {
                field1.setAccessible(true);

                if (java.lang.reflect.Modifier.isStatic(field1.getModifiers())) {
                    setInternalState(clazz, field1.getName(), value);
                } else if (instance != null) {
                    setInternalState(instance, field1.getName(), value);
                }
            }
        }
    }

    private static void setInternalState(Object target, String field, Object value) {

        Class targetClass = target.getClass();

        try {
            Field declaredField = targetClass.getDeclaredField(field);
            declaredField.setAccessible(true);
            declaredField.set(target, value);
        } catch (Exception e) {
            throw new RuntimeException("Unable to set internal state on a private field.", e);
        }
    }

    private static void setInternalState(Class c, String field, Object value) {

        try {
            Field declaredField = c.getDeclaredField(field);
            declaredField.setAccessible(true);
            declaredField.set(null, value);
        } catch (Exception e) {
            throw new RuntimeException("Unable to set internal state on a private field.", e);
        }
    }
}
