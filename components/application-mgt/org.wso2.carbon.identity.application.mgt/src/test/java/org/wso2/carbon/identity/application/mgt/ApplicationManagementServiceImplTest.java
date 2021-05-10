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

import org.powermock.modules.testng.PowerMockTestCase;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.wso2.carbon.base.CarbonBaseConstants;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.context.RegistryType;
import org.wso2.carbon.context.internal.OSGiDataHolder;
import org.wso2.carbon.core.internal.CarbonCoreDataHolder;
import org.wso2.carbon.identity.application.common.ApplicationAuthenticatorService;
import org.wso2.carbon.identity.application.common.IdentityApplicationManagementClientException;
import org.wso2.carbon.identity.application.common.IdentityApplicationManagementException;
import org.wso2.carbon.identity.application.common.model.ApplicationBasicInfo;
import org.wso2.carbon.identity.application.common.model.IdentityProvider;
import org.wso2.carbon.identity.application.common.model.LocalAuthenticatorConfig;
import org.wso2.carbon.identity.application.common.model.RequestPathAuthenticatorConfig;
import org.wso2.carbon.identity.application.common.model.ServiceProvider;
import org.wso2.carbon.identity.application.mgt.internal.ApplicationManagementServiceComponentHolder;
import org.wso2.carbon.identity.common.testng.WithH2Database;
import org.wso2.carbon.identity.common.testng.realm.InMemoryRealmService;
import org.wso2.carbon.identity.common.testng.realm.MockUserStoreManager;
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
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.when;
import static org.wso2.carbon.utils.multitenancy.MultitenantConstants.SUPER_TENANT_DOMAIN_NAME;
import static org.wso2.carbon.utils.multitenancy.MultitenantConstants.SUPER_TENANT_ID;

/*
  Unit tests for ApplicationManagementServiceImpl.
 */
@Test
@WithH2Database(jndiName = "jdbc/WSO2IdentityDB", files = {"dbscripts/identity.sql"})
public class ApplicationManagementServiceImplTest extends PowerMockTestCase {

    private final String sampleTenantDomain = "tenant domain";
    private final String applicationName1 = "Test application 1";
    private final String applicationName2 = "Test application 2";
    private final String idpName1 = "Test IdP 1";
    private final String idpName2 = "Test IdP 2";
    private final String username1 = "user 1";
    private final String username2 = "user 2";

    private ApplicationManagementServiceImpl applicationManagementService;
    private IdPManagementDAO idPManagementDAO;

    @BeforeClass
    public void setup() throws RegistryException, UserStoreException {
        setupConfiguration();
        applicationManagementService = ApplicationManagementServiceImpl.getInstance();
    }

    @DataProvider(name = "addApplicationDataProvider")
    public Object[][] addApplicationDataProvider() {

        ServiceProvider serviceProvider1 = new ServiceProvider();
        serviceProvider1.setApplicationName(applicationName1);

        ServiceProvider serviceProvider2 = new ServiceProvider();
        serviceProvider2.setApplicationName(applicationName2);

        return new Object[][]{
                {serviceProvider1, SUPER_TENANT_DOMAIN_NAME, username1},
                {serviceProvider2, sampleTenantDomain, username2}
        };
    }

    @Test(dataProvider = "addApplicationDataProvider")
    public void testAddApplication(Object serviceProvider, String tenantDomain, String username)
            throws Exception {

        ServiceProvider inputSP = (ServiceProvider) serviceProvider;

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
                {serviceProvider1, SUPER_TENANT_DOMAIN_NAME, username1},
                {serviceProvider2, SUPER_TENANT_DOMAIN_NAME, username1},
                {serviceProvider3, SUPER_TENANT_DOMAIN_NAME, username1}
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
                {serviceProvider1, SUPER_TENANT_DOMAIN_NAME, username1},
                {serviceProvider2, SUPER_TENANT_DOMAIN_NAME, username1}
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
        serviceProvider.setApplicationName(applicationName1);

        ServiceProvider newServiceProvider = new ServiceProvider();
        newServiceProvider.setApplicationName(applicationName1);

        return new Object[][]{
                {serviceProvider, newServiceProvider, SUPER_TENANT_DOMAIN_NAME, username1}
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
        serviceProvider1.setApplicationName(applicationName1);

        ServiceProvider serviceProvider2 = new ServiceProvider();
        serviceProvider2.setApplicationName(applicationName2);

        return new Object[][]{
                {serviceProvider1, SUPER_TENANT_DOMAIN_NAME, username1},
                {serviceProvider2, sampleTenantDomain, username2}
        };
    }

    @Test(dataProvider = "getApplicationDataProvider")
    public void testGetApplicationBasicInfo(Object serviceProvider, String tenantDomain, String username)
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
        serviceProvider1.setApplicationName(applicationName1);

        ServiceProvider serviceProvider2 = new ServiceProvider();
        serviceProvider2.setApplicationName(applicationName2);

        applicationManagementService.addApplication(serviceProvider1, SUPER_TENANT_DOMAIN_NAME, username1);
        applicationManagementService.addApplication(serviceProvider2, SUPER_TENANT_DOMAIN_NAME, username1);
    }

    @Test
    public void testGetAllApplicationBasicInfo() throws IdentityApplicationManagementException {

        addApplications();
        ApplicationBasicInfo[] applicationBasicInfo = applicationManagementService.getAllApplicationBasicInfo
                (SUPER_TENANT_DOMAIN_NAME, username1);

        Assert.assertEquals(applicationBasicInfo.length, 2);
        Assert.assertEquals(applicationBasicInfo[0].getApplicationName(), applicationName2);
        Assert.assertEquals(applicationBasicInfo[1].getApplicationName(), applicationName1);

        // Deleting all added applications.
        applicationManagementService.deleteApplications(SUPER_TENANT_ID);
    }

    @Test
    public void testGetAllPaginatedApplicationBasicInfo() throws IdentityApplicationManagementException {

        addApplications();
        ApplicationBasicInfo[] applicationBasicInfo = applicationManagementService.getAllPaginatedApplicationBasicInfo
                (SUPER_TENANT_DOMAIN_NAME, username1, 1);

        Assert.assertEquals(applicationBasicInfo.length, 2);
        Assert.assertEquals(applicationBasicInfo[0].getApplicationName(), applicationName2);
        Assert.assertEquals(applicationBasicInfo[1].getApplicationName(), applicationName1);

        // Deleting all added applications.
        applicationManagementService.deleteApplications(SUPER_TENANT_ID);
    }

    @Test
    public void testGetApplicationBasicInfoOffsetLimit() throws IdentityApplicationManagementException {

        addApplications();

        ApplicationBasicInfo[] applicationBasicInfo1 = applicationManagementService.getApplicationBasicInfo
                (SUPER_TENANT_DOMAIN_NAME, username1, 0, 1);
        Assert.assertEquals(applicationBasicInfo1[0].getApplicationName(), applicationName2);

        ApplicationBasicInfo[] applicationBasicInfo2 = applicationManagementService.getApplicationBasicInfo
                (SUPER_TENANT_DOMAIN_NAME, username1, 1, 1);
        Assert.assertEquals(applicationBasicInfo2[0].getApplicationName(), applicationName1);

        // Deleting all added applications.
        applicationManagementService.deleteApplications(SUPER_TENANT_ID);
    }

    @Test
    public void testGetApplicationBasicInfoFilterOffsetLimit() throws IdentityApplicationManagementException {

        addApplications();

        ApplicationBasicInfo[] applicationBasicInfo1 = applicationManagementService.getApplicationBasicInfo
                (SUPER_TENANT_DOMAIN_NAME, username1, applicationName2, 0, 1);
        Assert.assertEquals(applicationBasicInfo1[0].getApplicationName(), applicationName2);

        ApplicationBasicInfo[] applicationBasicInfo2 = applicationManagementService.getApplicationBasicInfo
                (SUPER_TENANT_DOMAIN_NAME, username1, applicationName1, 0, 1);
        Assert.assertEquals(applicationBasicInfo2[0].getApplicationName(), applicationName1);

        // Deleting all added applications.
        applicationManagementService.deleteApplications(SUPER_TENANT_ID);
    }

    @Test
    public void testGetCountOfAllApplications() throws IdentityApplicationManagementException {

        addApplications();
        Assert.assertEquals(applicationManagementService.getCountOfAllApplications(SUPER_TENANT_DOMAIN_NAME,
                username1), 2);

        // Deleting all added applications.
        applicationManagementService.deleteApplications(SUPER_TENANT_ID);
    }

    @Test
    public void testGetCountOfApplicationsFilter() throws IdentityApplicationManagementException {

        addApplications();
        Assert.assertEquals(applicationManagementService.getCountOfApplications(SUPER_TENANT_DOMAIN_NAME,
                username1, applicationName1), 1);
        Assert.assertEquals(applicationManagementService.getCountOfApplications(SUPER_TENANT_DOMAIN_NAME,
                username1, applicationName2), 1);

        // Deleting all added applications.
        applicationManagementService.deleteApplications(SUPER_TENANT_ID);
    }

    @DataProvider(name = "getIdentityProviderDataProvider")
    public Object[][] getIdentityProviderDataProvider() {

        IdentityProvider idp1 = new IdentityProvider();
        idp1.setIdentityProviderName(idpName1);

        IdentityProvider idp2 = new IdentityProvider();
        idp2.setIdentityProviderName(idpName2);

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
        idp1.setIdentityProviderName(idpName1);

        IdentityProvider idp2 = new IdentityProvider();
        idp2.setIdentityProviderName(idpName2);

        idPManagementDAO.addIdP(idp1, SUPER_TENANT_ID);
        idPManagementDAO.addIdP(idp2, SUPER_TENANT_ID);
    }

    public void testGetAllIdentityProviders() throws IdentityApplicationManagementException,
            IdentityProviderManagementException {

        addIdentityProviders();

        IdentityProvider[] identityProviders = applicationManagementService.
                getAllIdentityProviders(SUPER_TENANT_DOMAIN_NAME);
        Assert.assertEquals(identityProviders[0].getIdentityProviderName(), idpName1);
        Assert.assertEquals(identityProviders[1].getIdentityProviderName(), idpName2);

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
        ApplicationManagementServiceComponentHolder holder = ApplicationManagementServiceComponentHolder.getInstance();
        setInstanceValue(testSessionRealmService, RealmService.class, ApplicationManagementServiceComponentHolder.class,
                holder);

       // Configure Registry Service.
        RegistryService registryService = mock(RegistryService.class);
        UserRegistry registry = mock(UserRegistry.class);
        when(registryService.getGovernanceUserRegistry(anyString(), anyInt())).thenReturn(registry);
        OSGiDataHolder.getInstance().setRegistryService(registryService);
        CarbonCoreDataHolder.getInstance().setRegistryService(registryService);
        PrivilegedCarbonContext.getThreadLocalCarbonContext()
                .setRegistry(RegistryType.USER_GOVERNANCE, registryService.getRegistry());
        when(registry.resourceExists(anyString())).thenReturn(FALSE);
        Collection permissionNode = mock(Collection.class);
        when(registry.newCollection()).thenReturn(permissionNode);
        when(registry.get(anyString())).thenReturn(permissionNode);
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

        Class c = target.getClass();

        try {
            Field f = c.getDeclaredField(field);
            f.setAccessible(true);
            f.set(target, value);
        } catch (Exception e) {
            throw new RuntimeException("Unable to set internal state on a private field.", e);
        }
    }

    private static void setInternalState(Class c, String field, Object value) {

        try {
            Field f = c.getDeclaredField(field);
            f.setAccessible(true);
            f.set(null, value);
        } catch (Exception e) {
            throw new RuntimeException("Unable to set internal state on a private field.", e);
        }
    }
}
