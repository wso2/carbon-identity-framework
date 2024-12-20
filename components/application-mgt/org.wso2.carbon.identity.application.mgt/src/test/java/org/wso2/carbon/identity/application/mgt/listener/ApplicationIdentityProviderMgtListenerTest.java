/*
 * Copyright (c) 2024, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.carbon.identity.application.mgt.listener;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.base.CarbonBaseConstants;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.context.RegistryType;
import org.wso2.carbon.context.internal.OSGiDataHolder;
import org.wso2.carbon.core.internal.CarbonCoreDataHolder;
import org.wso2.carbon.identity.application.common.ApplicationAuthenticatorService;
import org.wso2.carbon.identity.application.common.IdentityApplicationManagementException;
import org.wso2.carbon.identity.application.common.model.FederatedAuthenticatorConfig;
import org.wso2.carbon.identity.application.common.model.IdentityProvider;
import org.wso2.carbon.identity.application.common.model.Property;
import org.wso2.carbon.identity.application.common.model.ServiceProvider;
import org.wso2.carbon.identity.application.mgt.ApplicationManagementService;
import org.wso2.carbon.identity.application.mgt.ApplicationManagementServiceImpl;
import org.wso2.carbon.identity.application.mgt.internal.ApplicationManagementServiceComponentHolder;
import org.wso2.carbon.identity.application.mgt.provider.ApplicationPermissionProvider;
import org.wso2.carbon.identity.application.mgt.provider.RegistryBasedApplicationPermissionProvider;
import org.wso2.carbon.identity.base.AuthenticatorPropertyConstants;
import org.wso2.carbon.identity.common.testng.WithAxisConfiguration;
import org.wso2.carbon.identity.common.testng.WithCarbonHome;
import org.wso2.carbon.identity.common.testng.WithH2Database;
import org.wso2.carbon.identity.common.testng.WithRealmService;
import org.wso2.carbon.identity.common.testng.WithRegistry;
import org.wso2.carbon.identity.common.testng.realm.InMemoryRealmService;
import org.wso2.carbon.identity.common.testng.realm.MockUserStoreManager;
import org.wso2.carbon.identity.core.internal.IdentityCoreServiceDataHolder;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;
import org.wso2.carbon.idp.mgt.IdentityProviderManagementException;
import org.wso2.carbon.idp.mgt.IdentityProviderManager;
import org.wso2.carbon.idp.mgt.internal.IdpMgtServiceComponentHolder;
import org.wso2.carbon.idp.mgt.util.MetadataConverter;
import org.wso2.carbon.registry.core.Collection;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.internal.RegistryDataHolder;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.registry.core.session.UserRegistry;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.api.UserStoreManager;
import org.wso2.carbon.user.core.service.RealmService;

import java.io.InputStream;
import java.lang.reflect.Field;
import java.nio.file.Paths;

import javax.xml.stream.XMLStreamException;

import static java.lang.Boolean.FALSE;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.wso2.carbon.utils.multitenancy.MultitenantConstants.SUPER_TENANT_DOMAIN_NAME;
import static org.wso2.carbon.utils.multitenancy.MultitenantConstants.SUPER_TENANT_ID;

/**
 * Unit tests for ApplicationIdentityProviderMgtListener.
 */
@Test
@WithAxisConfiguration
@WithCarbonHome
@WithRealmService(injectToSingletons = {IdpMgtServiceComponentHolder.class}, initUserStoreManager = true)
@WithRegistry
@WithH2Database(files = {"dbscripts/identity.sql"})
public class ApplicationIdentityProviderMgtListenerTest {

    private ApplicationManagementService applicationManagementService;
    private IdentityProviderManager identityProviderManager;
    private String appResourceId1;
    private String appResourceId2;
    private IdentityProvider identityProvider1;
    private IdentityProvider identityProvider2;
    private String username;
    private ApplicationIdentityProviderMgtListener applicationIdentityProviderMgtListener;

    @BeforeClass
    public void setup()
            throws UserStoreException, RegistryException, XMLStreamException, IdentityProviderManagementException,
            IdentityApplicationManagementException {

        username = "test-user";
        CarbonConstants.ENABLE_LEGACY_AUTHZ_RUNTIME = false;

        setupConfiguration();

        applicationManagementService = ApplicationManagementServiceImpl.getInstance();
        identityProviderManager = IdentityProviderManager.getInstance();
        registerSystemAuthenticators();
        IdpMgtServiceComponentHolder.getInstance().addMetadataConverter(mock(MetadataConverter.class));
        applicationIdentityProviderMgtListener = new ApplicationIdentityProviderMgtListener();

        IdentityProvider idp1 = getTestIdentityProvider("test-idp-1.xml");
        identityProvider1 = identityProviderManager.addIdPWithResourceId(idp1, SUPER_TENANT_DOMAIN_NAME);
        ServiceProvider serviceProvider1 = getTestServiceProvider("test-sp-1.xml");
        appResourceId1 =
                applicationManagementService.createApplication(serviceProvider1, SUPER_TENANT_DOMAIN_NAME, username);

        IdentityProvider idp2 = getTestIdentityProvider("test-idp-2.xml");
        identityProvider2 = identityProviderManager.addIdPWithResourceId(idp2, SUPER_TENANT_DOMAIN_NAME);
        ServiceProvider serviceProvider2 = getTestServiceProvider("test-sp-2.xml");
        appResourceId2 =
                applicationManagementService.createApplication(serviceProvider2, SUPER_TENANT_DOMAIN_NAME, username);
    }

    @Test(description = "Test the general workflow of the pre-update IDP listener during a normal IDP update.")
    public void testDoPreUpdateIDP() throws IdentityProviderManagementException {

        identityProvider1.setIdentityProviderDescription("This is a test description for IDP");
        boolean result =
                applicationIdentityProviderMgtListener.doPreUpdateIdP(identityProvider1.getIdentityProviderName(),
                        identityProvider1, SUPER_TENANT_DOMAIN_NAME);
        Assert.assertTrue(result);
    }

    private void registerSystemAuthenticators() {

        FederatedAuthenticatorConfig federatedAuthenticatorConfig = new FederatedAuthenticatorConfig();
        federatedAuthenticatorConfig.setDisplayName("DisplayName");
        federatedAuthenticatorConfig.setName("SAMLSSOAuthenticator");
        federatedAuthenticatorConfig.setEnabled(true);
        federatedAuthenticatorConfig.setDefinedByType(AuthenticatorPropertyConstants.DefinedByType.SYSTEM);
        Property property1 = new Property();
        property1.setName("SPEntityId");
        property1.setConfidential(false);
        Property property2 = new Property();
        property2.setName("meta_data_saml");
        property2.setConfidential(false);
        federatedAuthenticatorConfig.setProperties(new Property[] {property1, property2});
        ApplicationAuthenticatorService.getInstance().addFederatedAuthenticator(federatedAuthenticatorConfig);

        FederatedAuthenticatorConfig config = new FederatedAuthenticatorConfig();
        config.setName("CustomAuthenticator");
        config.setDisplayName("DisplayName");
        config.setEnabled(true);
        config.setDefinedByType(AuthenticatorPropertyConstants.DefinedByType.USER);
        ApplicationAuthenticatorService.getInstance().addFederatedAuthenticator(config);
    }

    private ServiceProvider getTestServiceProvider(String spFileName) throws XMLStreamException {

        InputStream inputStream = this.getClass().getResourceAsStream(spFileName);
        OMElement documentElement = new StAXOMBuilder(inputStream).getDocumentElement();
        return ServiceProvider.build(documentElement);
    }

    private IdentityProvider getTestIdentityProvider(String idpFileName) throws XMLStreamException {

        InputStream inputStream = this.getClass().getResourceAsStream(idpFileName);
        OMElement documentElement = new StAXOMBuilder(inputStream).getDocumentElement();
        return IdentityProvider.build(documentElement);
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

    private void setupConfiguration() throws UserStoreException, RegistryException {

        String carbonHome = Paths.get(System.getProperty("user.dir"), "target", "test-classes", "repository").
                toString();
        System.setProperty(CarbonBaseConstants.CARBON_HOME, carbonHome);
        System.setProperty(CarbonBaseConstants.CARBON_CONFIG_DIR_PATH, Paths.get(carbonHome, "conf").toString());

        // Configure RealmService.
        PrivilegedCarbonContext.startTenantFlow();
        PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantId(SUPER_TENANT_ID);
        PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(SUPER_TENANT_DOMAIN_NAME);
        PrivilegedCarbonContext.getThreadLocalCarbonContext().setUsername(username);
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
        setInstanceValue(new RegistryBasedApplicationPermissionProvider(), ApplicationPermissionProvider.class,
                ApplicationManagementServiceComponentHolder.class, holder);

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
}
