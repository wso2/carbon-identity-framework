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

package org.wso2.carbon.identity.provisioning;

import org.apache.commons.dbcp.BasicDataSource;
import org.apache.commons.lang.StringUtils;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.identity.application.common.model.Claim;
import org.wso2.carbon.identity.application.common.model.ClaimConfig;
import org.wso2.carbon.identity.application.common.model.ClaimMapping;
import org.wso2.carbon.identity.application.common.model.FederatedAuthenticatorConfig;
import org.wso2.carbon.identity.application.common.model.IdentityProvider;
import org.wso2.carbon.identity.application.common.model.LocalRole;
import org.wso2.carbon.identity.application.common.model.OutboundProvisioningConfig;
import org.wso2.carbon.identity.application.common.model.PermissionsAndRoleConfig;
import org.wso2.carbon.identity.application.common.model.Property;
import org.wso2.carbon.identity.application.common.model.ProvisioningConnectorConfig;
import org.wso2.carbon.identity.application.common.model.RoleMapping;
import org.wso2.carbon.identity.application.common.model.ServiceProvider;
import org.wso2.carbon.identity.application.mgt.ApplicationConstants;
import org.wso2.carbon.identity.application.mgt.ApplicationManagementService;
import org.wso2.carbon.identity.base.AuthenticatorPropertyConstants;
import org.wso2.carbon.identity.core.util.IdentityDatabaseUtil;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;
import org.wso2.carbon.identity.core.util.IdentityUtil;
import org.wso2.carbon.identity.provisioning.internal.ProvisioningServiceDataHolder;
import org.wso2.carbon.identity.provisioning.listener.DefaultInboundUserProvisioningListener;
import org.wso2.carbon.identity.provisioning.listener.ProvisioningRoleMgtListener;
import org.wso2.carbon.identity.role.v2.mgt.core.RoleManagementService;
import org.wso2.carbon.identity.role.v2.mgt.core.exception.IdentityRoleManagementException;
import org.wso2.carbon.identity.role.v2.mgt.core.model.RoleBasicInfo;
import org.wso2.carbon.identity.secret.mgt.core.IdPSecretsProcessor;
import org.wso2.carbon.identity.secret.mgt.core.SecretsProcessor;
import org.wso2.carbon.idp.mgt.dao.IdPManagementDAO;
import org.wso2.carbon.idp.mgt.internal.IdpMgtServiceComponentHolder;
import org.wso2.carbon.user.api.RealmConfiguration;
import org.wso2.carbon.user.core.UserRealm;
import org.wso2.carbon.user.core.common.AbstractUserStoreManager;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.user.core.tenant.TenantManager;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.wso2.carbon.identity.provisioning.IdentityProvisioningConstants.APPLICATION_BASED_OUTBOUND_PROVISIONING_ENABLED;

@Listeners(MockitoTestNGListener.class)
public class OutboundProvisioningTest {

    @Mock
    RealmService realmService;
    @Mock
    RealmConfiguration realmConfiguration;
    @Mock
    UserRealm userRealm;
    @Mock
    TenantManager tenantManager;
    @Mock
    AbstractUserStoreManager userStoreManager;
    @Mock
    RoleManagementService roleManagementService;
    @Mock
    ApplicationManagementService applicationManagementService;
    @Mock
    AbstractProvisioningConnectorFactory abstractProvisioningConnectorFactory;
    @Mock
    AbstractOutboundProvisioningConnector abstractOutboundProvisioningConnector;

    private static Map<String, BasicDataSource> dataSourceMap = new HashMap<>();
    private static final Integer SUPER_TENANT_ID = -1234;
    private static final String SUPER_TENANT_DOMAIN = "carbon.super";

    private static final String DB_NAME = "test";
    private final IdPManagementDAO idPManagementDAO = new IdPManagementDAO();
    private static final String PROVISIONED_USER_NAME = "PRIMARY/John";
    private static final String PROVISIONED_USER_ID = "123456789";
    private static final String ROLE_UUID = "gt6y1ji1-htda7611";


    Connection connection;

    DefaultInboundUserProvisioningListener defaultInboundUserProvisioningListener;
    IdentityProvider identityProvider = createIDPWithData();
    ServiceProvider serviceProvider = new ServiceProvider();


    @BeforeTest
    public void setup() throws Exception {

        // Initialize mocks
        MockitoAnnotations.initMocks(this);
        defaultInboundUserProvisioningListener = new DefaultInboundUserProvisioningListener();

        ProvisioningServiceDataHolder.getInstance().setRealmService(realmService);
        ProvisioningServiceDataHolder.getInstance()
                .setDefaultInboundUserProvisioningListener(defaultInboundUserProvisioningListener);
        IdpMgtServiceComponentHolder.getInstance().setRealmService(realmService);
        Map<String, AbstractProvisioningConnectorFactory> connectorFactories = new HashMap<>();
        connectorFactories.put("SCIM2", abstractProvisioningConnectorFactory);
        ProvisioningServiceDataHolder.getInstance().setConnectorFactories(connectorFactories);
        when(abstractProvisioningConnectorFactory
                .getConnector(anyString(), any(), anyString()))
                .thenReturn(abstractOutboundProvisioningConnector);

        when(realmService.getTenantUserRealm(SUPER_TENANT_ID)).thenReturn(userRealm);
        when(realmService.getTenantManager()).thenReturn(tenantManager);
        when(tenantManager.getTenantId(SUPER_TENANT_DOMAIN)).thenReturn(-1234);
        when(userRealm.getUserStoreManager()).thenReturn(userStoreManager);

        when(userStoreManager.getUserNameFromUserID(PROVISIONED_USER_ID)).thenReturn(PROVISIONED_USER_NAME);
        when(userStoreManager.getUserIDFromUserName(PROVISIONED_USER_NAME)).thenReturn(PROVISIONED_USER_ID);

        when(userStoreManager.getRealmConfiguration()).thenReturn(realmConfiguration);
        when(realmConfiguration.getUserStoreProperty("DomainName")).thenReturn("PRIMARY");

        when(userStoreManager.getRoleListOfUser(anyString())).thenReturn(new String[]{"group1"});

        serviceProvider.setApplicationName(ApplicationConstants.CONSOLE_APPLICATION_NAME);

        initiateH2Database(DB_NAME, getFilePath("h2.sql"));

        SecretsProcessor<IdentityProvider> idpSecretsProcessor = mock(
                IdPSecretsProcessor.class);
        IdpMgtServiceComponentHolder.getInstance().setIdPSecretsProcessorService(idpSecretsProcessor);

        when(idpSecretsProcessor.decryptAssociatedSecrets(any())).thenAnswer(invocation -> invocation.getArguments()[0]);
        when(idpSecretsProcessor.encryptAssociatedSecrets(any())).thenAnswer(invocation -> invocation.getArguments()[0]);

        OutboundProvisioningConfig outboundProvisioningConfig = new OutboundProvisioningConfig();
        outboundProvisioningConfig.setProvisioningIdentityProviders(new IdentityProvider[]{identityProvider});
        serviceProvider.setOutboundProvisioningConfig(outboundProvisioningConfig);
    }

    @AfterMethod
    public void tearDown() throws Exception {

        closeH2Database();
    }

    @Test
    public void testRoleBaseOutBoundProvisioning() throws IdentityRoleManagementException {

        ProvisioningRoleMgtListener postUpdateUserListOfRole = new ProvisioningRoleMgtListener();
        RoleBasicInfo roleBasicInfo = new RoleBasicInfo(ROLE_UUID, "developer");
        roleBasicInfo.setAudience("organization");
        ProvisioningServiceDataHolder.getInstance().setRoleManagementService(roleManagementService);
        when(roleManagementService.getRoleBasicInfoById(eq(ROLE_UUID),
                eq(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME))).thenReturn(roleBasicInfo);
        when(roleManagementService.getRoleIdListOfUser(PROVISIONED_USER_ID, SUPER_TENANT_DOMAIN))
                .thenReturn(Collections.singletonList(ROLE_UUID));

        try (MockedStatic<CarbonContext> carbonContext = Mockito.mockStatic(CarbonContext.class);
             MockedStatic<IdentityTenantUtil> identityTenantUtil = Mockito.mockStatic(IdentityTenantUtil.class);
             MockedStatic<IdentityDatabaseUtil> identityDatabaseUtil = Mockito.mockStatic(IdentityDatabaseUtil.class);
             MockedStatic<ApplicationManagementService> appMgtService = Mockito.mockStatic(ApplicationManagementService.class);
             MockedStatic<IdentityUtil> identityUtil = Mockito.mockStatic(IdentityUtil.class)) {

            mockCarbonContext(carbonContext);
            connection = getConnection(DB_NAME);
            identityDatabaseUtil.when(() -> IdentityDatabaseUtil.getDBConnection(anyBoolean())).thenReturn(connection);
            identityDatabaseUtil.when(IdentityDatabaseUtil::getDataSource).thenReturn(dataSourceMap.get(DB_NAME));

            identityTenantUtil.when(() -> IdentityTenantUtil.getTenantId(SUPER_TENANT_DOMAIN)).thenReturn(SUPER_TENANT_ID);
            identityTenantUtil.when(() -> IdentityTenantUtil.getTenantDomain(SUPER_TENANT_ID)).thenReturn(SUPER_TENANT_DOMAIN);
            identityUtil.when(() -> IdentityUtil.extractDomainFromName(anyString())).thenReturn("PRIMARY");
            appMgtService.when(ApplicationManagementService::getInstance).thenReturn(applicationManagementService);
            when(applicationManagementService.getServiceProvider(anyString(), anyString())).thenReturn(serviceProvider);
            when(userStoreManager.getRealmConfiguration()).thenReturn(realmConfiguration);
            when(realmConfiguration.getUserStoreProperty("DomainName")).thenReturn("PRIMARY");

            idPManagementDAO.addIdP(identityProvider, SUPER_TENANT_ID);
            postUpdateUserListOfRole.postUpdateUserListOfRole(ROLE_UUID,
                    Collections.singletonList(PROVISIONED_USER_ID), new ArrayList<>(),
                    MultitenantConstants.SUPER_TENANT_DOMAIN_NAME);

            DefaultInboundUserProvisioningListener defaultInboundUserProvisioningListener1 =
                    new DefaultInboundUserProvisioningListener();
            defaultInboundUserProvisioningListener1.doPreDeleteUser(PROVISIONED_USER_NAME, userStoreManager);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    private void initiateH2Database(String databaseName, String scriptPath) throws Exception {

        BasicDataSource dataSource = new BasicDataSource();
        dataSource.setDriverClassName("org.h2.Driver");
        dataSource.setUsername("username");
        dataSource.setPassword("password");
        dataSource.setUrl("jdbc:h2:mem:test" + databaseName);
        try (Connection connection = dataSource.getConnection()) {
            connection.createStatement().executeUpdate("RUNSCRIPT FROM '" + scriptPath + "'");
        }
        dataSourceMap.put(databaseName, dataSource);
    }

    private static String getFilePath(String fileName) {

        if (StringUtils.isNotBlank(fileName)) {
            return Paths.get(System.getProperty("user.dir"), "src", "test", "resources", "dbscripts", fileName)
                    .toString();
        }
        throw new IllegalArgumentException("DB Script file name cannot be empty.");
    }

    public static void closeH2Database() throws Exception {

        BasicDataSource dataSource = dataSourceMap.get(DB_NAME);
        if (dataSource != null) {
            dataSource.close();
        }
    }

    private static Connection getConnection(String database) throws SQLException {

        if (dataSourceMap.get(database) != null) {
            return dataSourceMap.get(database).getConnection();
        }
        throw new RuntimeException("No datasource initiated for database: " + database);
    }

    public IdentityProvider createIDPWithData() {

        // Initialize Test Identity Provider 1.
        IdentityProvider idp1 = new IdentityProvider();
        idp1.setIdentityProviderName("testIdP1");
        idp1.setEnable(true);
        idp1.setPrimary(true);
        idp1.setFederationHub(true);
        idp1.setCertificate("");

        RoleMapping roleMapping1 = new RoleMapping(new LocalRole("1", "LocalRole1"), "Role1");
        RoleMapping roleMapping2 = new RoleMapping(new LocalRole("2", "LocalRole2"), "Role2");
        PermissionsAndRoleConfig permissionsAndRoleConfig = new PermissionsAndRoleConfig();
        permissionsAndRoleConfig.setIdpRoles(new String[]{"Role1", "Role2"});
        permissionsAndRoleConfig.setRoleMappings(new RoleMapping[]{roleMapping1, roleMapping2});
        idp1.setPermissionAndRoleConfig(permissionsAndRoleConfig);

        FederatedAuthenticatorConfig federatedAuthenticatorConfig = new FederatedAuthenticatorConfig();
        federatedAuthenticatorConfig.setDisplayName("DisplayName1");
        federatedAuthenticatorConfig.setName("Name");
        federatedAuthenticatorConfig.setDefinedByType(AuthenticatorPropertyConstants.DefinedByType.SYSTEM);
        federatedAuthenticatorConfig.setEnabled(true);
        Property property1 = new Property();
        property1.setName("Property1");
        property1.setValue("value1");
        property1.setConfidential(false);
        Property property2 = new Property();
        property2.setName("Property2");
        property2.setValue("value2");
        property2.setConfidential(true);
        federatedAuthenticatorConfig.setProperties(new Property[]{property1, property2});
        idp1.setFederatedAuthenticatorConfigs(new FederatedAuthenticatorConfig[]{federatedAuthenticatorConfig});

        ProvisioningConnectorConfig provisioningConnectorConfig1 = new ProvisioningConnectorConfig();
        provisioningConnectorConfig1.setName("SCIM2");
        provisioningConnectorConfig1.setEnabled(true);
        provisioningConnectorConfig1.setBlocking(true);
        provisioningConnectorConfig1.setProvisioningProperties(new Property[]{property1});
        idp1.setProvisioningConnectorConfigs(new ProvisioningConnectorConfig[]{provisioningConnectorConfig1});
        idp1.setDefaultProvisioningConnectorConfig(provisioningConnectorConfig1);

        ClaimConfig claimConfig = new ClaimConfig();
        claimConfig.setLocalClaimDialect(false);
        claimConfig.setRoleClaimURI("Country");
        claimConfig.setUserClaimURI("Country");
        ClaimMapping claimMapping = ClaimMapping.build("http://wso2.org/claims/country", "Country", "", true);
        claimConfig.setClaimMappings(new ClaimMapping[]{claimMapping});
        Claim remoteClaim = new Claim();
        remoteClaim.setClaimId(0);
        remoteClaim.setClaimUri("Country");
        claimConfig.setIdpClaims(new Claim[]{remoteClaim});
        idp1.setClaimConfig(claimConfig);
        idp1.setProvisioningRole("Internal/developer");
        return idp1;
    }

    private void mockCarbonContext(MockedStatic<CarbonContext> carbonContext) {

        CarbonContext mockCarbonContext = mock(CarbonContext.class);
        carbonContext.when(CarbonContext::getThreadLocalCarbonContext).thenReturn(mockCarbonContext);
        when(mockCarbonContext.getTenantDomain()).thenReturn("carbon.super");
        when(mockCarbonContext.getTenantId()).thenReturn(-1234);
    }
}
