/*
 * Copyright (c) 2021-2025, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.idp.mgt;

import org.mockito.MockedStatic;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.wso2.carbon.core.util.CryptoUtil;
import org.wso2.carbon.identity.action.management.api.exception.ActionMgtClientException;
import org.wso2.carbon.identity.action.management.api.exception.ActionMgtServerException;
import org.wso2.carbon.identity.action.management.api.model.Action;
import org.wso2.carbon.identity.action.management.api.model.EndpointConfig;
import org.wso2.carbon.identity.action.management.api.service.ActionManagementService;
import org.wso2.carbon.identity.application.common.ApplicationAuthenticatorService;
import org.wso2.carbon.identity.application.common.ProvisioningConnectorService;
import org.wso2.carbon.identity.application.common.model.Claim;
import org.wso2.carbon.identity.application.common.model.ClaimConfig;
import org.wso2.carbon.identity.application.common.model.ClaimMapping;
import org.wso2.carbon.identity.application.common.model.FederatedAuthenticatorConfig;
import org.wso2.carbon.identity.application.common.model.IdentityProvider;
import org.wso2.carbon.identity.application.common.model.IdentityProviderProperty;
import org.wso2.carbon.identity.application.common.model.LocalRole;
import org.wso2.carbon.identity.application.common.model.PermissionsAndRoleConfig;
import org.wso2.carbon.identity.application.common.model.Property;
import org.wso2.carbon.identity.application.common.model.ProvisioningConnectorConfig;
import org.wso2.carbon.identity.application.common.model.RoleMapping;
import org.wso2.carbon.identity.application.common.model.UserDefinedFederatedAuthenticatorConfig;
import org.wso2.carbon.identity.application.common.util.IdentityApplicationConstants;
import org.wso2.carbon.identity.base.AuthenticatorPropertyConstants.DefinedByType;
import org.wso2.carbon.identity.claim.metadata.mgt.ClaimMetadataManagementServiceImpl;
import org.wso2.carbon.identity.claim.metadata.mgt.exception.ClaimMetadataException;
import org.wso2.carbon.identity.claim.metadata.mgt.model.LocalClaim;
import org.wso2.carbon.identity.common.testng.WithAxisConfiguration;
import org.wso2.carbon.identity.common.testng.WithCarbonHome;
import org.wso2.carbon.identity.common.testng.WithH2Database;
import org.wso2.carbon.identity.common.testng.WithKeyStore;
import org.wso2.carbon.identity.common.testng.WithRealmService;
import org.wso2.carbon.identity.common.testng.WithRegistry;
import org.wso2.carbon.identity.core.util.IdentityDatabaseUtil;
import org.wso2.carbon.identity.organization.management.service.OrganizationManager;
import org.wso2.carbon.identity.organization.management.service.util.OrganizationManagementUtil;
import org.wso2.carbon.identity.organization.management.service.util.Utils;
import org.wso2.carbon.identity.organization.resource.hierarchy.traverse.service.OrgResourceResolverService;
import org.wso2.carbon.identity.secret.mgt.core.SecretManagerImpl;
import org.wso2.carbon.identity.secret.mgt.core.model.SecretType;
import org.wso2.carbon.idp.mgt.dao.CacheBackedIdPMgtDAO;
import org.wso2.carbon.idp.mgt.dao.IdPManagementDAO;
import org.wso2.carbon.idp.mgt.internal.IdpMgtServiceComponentHolder;
import org.wso2.carbon.idp.mgt.util.ActionMgtTestUtil;
import org.wso2.carbon.idp.mgt.util.IdPManagementConstants;
import org.wso2.carbon.idp.mgt.util.IdPManagementConstants.ErrorMessage;
import org.wso2.carbon.idp.mgt.util.MetadataConverter;

import java.io.ByteArrayInputStream;
import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

import javax.xml.stream.XMLStreamException;

import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;
import static org.wso2.carbon.base.MultitenantConstants.SUPER_TENANT_ID;

import static java.lang.Boolean.TRUE;

/**
 * Unit tests for IdentityProviderManagementService.
 */
@WithAxisConfiguration
@WithCarbonHome
@WithRealmService(injectToSingletons = {IdpMgtServiceComponentHolder.class}, initUserStoreManager = true)
@WithRegistry
@WithH2Database(jndiName = "jdbc/WSO2IdentityDB", files = {"dbscripts/h2.sql"})
@WithKeyStore
public class IdentityProviderManagementServiceTest {

    MetadataConverter mockMetadataConverter;
    private IdentityProviderManagementService identityProviderManagementService;
    private CacheBackedIdPMgtDAO dao;
    private CacheBackedIdPMgtDAO daoForException;
    private Field field;
    private IdentityProviderManager identityProviderManager;
    private MockedStatic<CryptoUtil> cryptoUtil;
    private ActionManagementService actionManagementService;
    private OrgResourceResolverService orgResourceResolverService;
    private MockedStatic<OrganizationManagementUtil> organizationManagementUtilMockedStatic;
    private MockedStatic<Utils> utilsMockedStatic;

    private static final String ASSOCIATED_ACTION_ID = "Dummy_Action_ID";
    private static final String CUSTOM_IDP_NAME = "customIdP";
    private static final String ROOT_TENANT_DOMAIN = "carbon.super";
    private static final String ROOT_ORG_ID = "10084a8d-113f-4211-a0d5-efe36b082211";
    private static Action action;
    private static EndpointConfig endpointConfig;
    private static EndpointConfig endpointConfigToBeUpdated;
    private IdentityProvider idpForErrorScenarios;
    private IdentityProvider userDefinedIdP;

    @BeforeClass
    public void setUpClass() throws Exception {

        SecretManagerImpl secretManager = mock(SecretManagerImpl.class);
        SecretType secretType = mock(SecretType.class);
        IdpMgtServiceComponentHolder.getInstance().setSecretManager(secretManager);
        when(secretType.getId()).thenReturn("secretId");
        doReturn(secretType).when(secretManager).getSecretType(any());
        when(secretManager.isSecretExist(anyString(), anyString())).thenReturn(false);

        cryptoUtil = mockStatic(CryptoUtil.class);
        CryptoUtil mockCryptoUtil = mock(CryptoUtil.class);
        cryptoUtil.when(CryptoUtil::getDefaultCryptoUtil).thenReturn(mockCryptoUtil);

        dao = new CacheBackedIdPMgtDAO(new IdPManagementDAO());
        identityProviderManager = mock(IdentityProviderManager.class);
        identityProviderManagementService = new IdentityProviderManagementService();
        field = IdentityProviderManager.class.getDeclaredField("dao");
        field.setAccessible(true);
        field.set(identityProviderManager, dao);

        registerSystemAuthenticators();

        endpointConfig = ActionMgtTestUtil.createEndpointConfig("http://localhost", "admin", "admin");
        endpointConfigToBeUpdated = ActionMgtTestUtil.createEndpointConfig(
                "http://localhost1", "admin1", "admin1");
        action = ActionMgtTestUtil.createAction(endpointConfig);
        userDefinedIdP = ActionMgtTestUtil.createIdPWithUserDefinedFederatedAuthenticatorConfig(
                CUSTOM_IDP_NAME, action.getEndpoint());
        idpForErrorScenarios = ActionMgtTestUtil.createIdPWithUserDefinedFederatedAuthenticatorConfig(
                CUSTOM_IDP_NAME + "Error", action.getEndpoint());
    }

    @AfterClass
    public void tearDownClass() {
        cryptoUtil.close();
    }

    @BeforeMethod
    public void setUp() throws Exception {

        field.set(identityProviderManager, dao);
        mockMetadataConverter = mock(MetadataConverter.class);
        List<MetadataConverter> metadataConverterList = Arrays.asList(mockMetadataConverter);
        IdpMgtServiceComponentHolder.getInstance().setMetadataConverters(metadataConverterList);

        actionManagementService = mock(ActionManagementService.class);
        IdpMgtServiceComponentHolder.getInstance().setActionManagementService(actionManagementService);
        when(actionManagementService.addAction(anyString(), any(), any())).thenReturn(action);
        when(actionManagementService.updateAction(anyString(), any(), any(), any())).thenReturn(action);
        when(actionManagementService.getActionByActionId(anyString(), any(), any())).thenReturn(action);
        doNothing().when(actionManagementService).deleteAction(anyString(), any(), any());

        organizationManagementUtilMockedStatic = mockStatic(OrganizationManagementUtil.class);
        organizationManagementUtilMockedStatic.when(() -> OrganizationManagementUtil.isOrganization(any()))
                .thenReturn(false);
        utilsMockedStatic = mockStatic(Utils.class);

        OrganizationManager organizationManager = mock(OrganizationManager.class);
        IdpMgtServiceComponentHolder.getInstance().setOrganizationManager(organizationManager);
        when(organizationManager.resolveOrganizationId(ROOT_TENANT_DOMAIN)).thenReturn(ROOT_ORG_ID);

        orgResourceResolverService = mock(OrgResourceResolverService.class);
        IdpMgtServiceComponentHolder.getInstance().setOrgResourceResolverService(orgResourceResolverService);
    }

    @AfterMethod
    public void tearDown() throws Exception {

        IdpMgtServiceComponentHolder.getInstance().setActionManagementService(actionManagementService);
        field.set(identityProviderManager, dao);
        // Clear Database after every test.
        removeTestIdps();
        organizationManagementUtilMockedStatic.close();
        utilsMockedStatic.close();
    }

    private void registerSystemAuthenticators() {

        FederatedAuthenticatorConfig federatedAuthenticatorConfig = new FederatedAuthenticatorConfig();
        federatedAuthenticatorConfig.setDisplayName("DisplayName");
        federatedAuthenticatorConfig.setName("SAMLSSOAuthenticator");
        federatedAuthenticatorConfig.setEnabled(true);
        federatedAuthenticatorConfig.setDefinedByType(DefinedByType.SYSTEM);
        Property property1 = new Property();
        property1.setName("SPEntityId");
        property1.setConfidential(false);
        Property property2 = new Property();
        property2.setName("meta_data_saml");
        property2.setConfidential(false);
        federatedAuthenticatorConfig.setProperties(new Property[]{property1, property2});
        ApplicationAuthenticatorService.getInstance().addFederatedAuthenticator(federatedAuthenticatorConfig);

        FederatedAuthenticatorConfig config = new FederatedAuthenticatorConfig();
        config.setName("Name");
        config.setDisplayName("DisplayName");
        config.setEnabled(true);
        config.setDefinedByType(DefinedByType.USER);
        ApplicationAuthenticatorService.getInstance().addFederatedAuthenticator(config);

        FederatedAuthenticatorConfig samlSsoConfig = new FederatedAuthenticatorConfig();
        samlSsoConfig.setName("samlsso");
        samlSsoConfig.setDisplayName("DisplayName");
        samlSsoConfig.setEnabled(true);
        samlSsoConfig.setDefinedByType(DefinedByType.USER);
        ApplicationAuthenticatorService.getInstance().addFederatedAuthenticator(samlSsoConfig);
    }

    @DataProvider
    public Object[][] addFederatedAuthenticatorData() {

        FederatedAuthenticatorConfig systemDefinedAuthWithInvalidName = new FederatedAuthenticatorConfig();
        systemDefinedAuthWithInvalidName.setDisplayName("DisplayName1");
        systemDefinedAuthWithInvalidName.setName("NonRegisteredAuthenticator");
        systemDefinedAuthWithInvalidName.setEnabled(true);
        systemDefinedAuthWithInvalidName.setDefinedByType(DefinedByType.SYSTEM);

        FederatedAuthenticatorConfig userDefinedAuthWithExistingName = new UserDefinedFederatedAuthenticatorConfig();
        userDefinedAuthWithExistingName.setDisplayName("DisplayName1");
        userDefinedAuthWithExistingName.setName("SAMLSSOAuthenticator");
        userDefinedAuthWithExistingName.setEnabled(true);
        userDefinedAuthWithExistingName.setDefinedByType(DefinedByType.USER);

        FederatedAuthenticatorConfig userDefinedAuthWithInvalidName = new UserDefinedFederatedAuthenticatorConfig();
        userDefinedAuthWithInvalidName.setDisplayName("DisplayName1");
        userDefinedAuthWithInvalidName.setName("Invalid regex name");
        userDefinedAuthWithInvalidName.setEnabled(true);
        userDefinedAuthWithInvalidName.setDefinedByType(DefinedByType.USER);

        return new Object[][]{
                {systemDefinedAuthWithInvalidName, ErrorMessage.ERROR_CODE_NO_SYSTEM_AUTHENTICATOR_FOUND},
                {userDefinedAuthWithExistingName, ErrorMessage.ERROR_CODE_AUTHENTICATOR_NAME_ALREADY_TAKEN},
                {userDefinedAuthWithInvalidName, ErrorMessage.ERROR_INVALID_AUTHENTICATOR_NAME}
        };
    }

    @Test(dataProvider = "addFederatedAuthenticatorData")
    public void testFederatedAuthenticatorNameValidation(FederatedAuthenticatorConfig config, ErrorMessage error) {

        IdentityProvider identityProvider = new IdentityProvider();
        identityProvider.setIdentityProviderName("testInvalidIdP");
        identityProvider.setDisplayName("test Invalid IdP");
        identityProvider.setFederatedAuthenticatorConfigs(new FederatedAuthenticatorConfig[]{config});

        IdentityProviderManagementException thrownException = assertThrows(
                IdentityProviderManagementException.class,
                () -> identityProviderManagementService.addIdP(identityProvider)
        );
        assertEquals(thrownException.getErrorCode(), error.getCode());
    }

    @Test
    public void testAddIdPActionException() throws Exception {

        ActionManagementService actionManagementServiceForException = mock(ActionManagementService.class);
        when(actionManagementServiceForException.addAction(anyString(), any(), any()))
                .thenThrow(ActionMgtServerException.class);
        IdpMgtServiceComponentHolder.getInstance().setActionManagementService(actionManagementServiceForException);

        assertThrows(IdentityProviderManagementServerException.class, () ->
                identityProviderManagementService.addIdP(idpForErrorScenarios));
        identityProviderManagementService.getIdPByName(idpForErrorScenarios.getIdentityProviderName());
    }

    @Test
    public void testAddIdPActionClientException() throws Exception {

        ActionManagementService actionManagementServiceForException = mock(ActionManagementService.class);
        when(actionManagementServiceForException.addAction(anyString(), any(), any()))
                .thenThrow(ActionMgtClientException.class);
        IdpMgtServiceComponentHolder.getInstance().setActionManagementService(actionManagementServiceForException);

        assertThrows(IdentityProviderManagementClientException.class, () ->
                identityProviderManagementService.addIdP(idpForErrorScenarios));
        identityProviderManagementService.getIdPByName(idpForErrorScenarios.getIdentityProviderName());
    }

    @DataProvider
    public Object[][] addIdPData() {

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
        federatedAuthenticatorConfig.setEnabled(true);
        federatedAuthenticatorConfig.setDefinedByType(DefinedByType.SYSTEM);
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
        provisioningConnectorConfig1.setName("ProvisiningConfig1");
        provisioningConnectorConfig1.setProvisioningProperties(new Property[]{property1});
        ProvisioningConnectorConfig provisioningConnectorConfig2 = new ProvisioningConnectorConfig();
        provisioningConnectorConfig2.setName("ProvisiningConfig2");
        provisioningConnectorConfig2.setProvisioningProperties(new Property[]{property2});
        provisioningConnectorConfig2.setEnabled(true);
        provisioningConnectorConfig2.setBlocking(true);
        idp1.setProvisioningConnectorConfigs(new ProvisioningConnectorConfig[]{provisioningConnectorConfig1,
                provisioningConnectorConfig2});

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

        // Initialize Test Identity Provider 2.
        IdentityProvider idp2 = new IdentityProvider();
        idp2.setIdentityProviderName("testIdP2");

        ClaimConfig claimConfig2 = new ClaimConfig();
        claimConfig2.setLocalClaimDialect(true);
        claimConfig2.setRoleClaimURI("http://wso2.org/claims/role");
        claimConfig2.setUserClaimURI("http://wso2.org/claims/fullname");
        ClaimMapping claimMapping2 = new ClaimMapping();
        Claim localClaim2 = new Claim();
        localClaim2.setClaimId(0);
        localClaim2.setClaimUri("http://wso2.org/claims/fullname");
        claimMapping2.setLocalClaim(localClaim2);
        claimConfig2.setClaimMappings(new ClaimMapping[]{claimMapping2});
        idp2.setClaimConfig(claimConfig2);

        // Initialize Test Identity Provider 3.
        IdentityProvider idp3 = new IdentityProvider();
        idp3.setIdentityProviderName("testIdP3");

        return new Object[][]{
                // IDP with PermissionsAndRoleConfig,FederatedAuthenticatorConfigs,ProvisioningConnectorConfigs,Claims.
                {idp1},
                // IDP with Local Cliam Dialect ClaimConfigs.
                {idp2},
                // IDP with only the name.
                {idp3},
                {userDefinedIdP}
        };
    }

    @Test(dataProvider = "addIdPData")
    public void testAddIdP(Object identityProvider) throws Exception {

        String idpName = ((IdentityProvider) identityProvider).getIdentityProviderName();
        identityProviderManagementService.addIdP(((IdentityProvider) identityProvider));

        IdentityProvider idpFromDb = identityProviderManagementService.getIdPByName(idpName);
        assertIdPResult(idpFromDb);
        Assert.assertEquals(idpFromDb.getIdentityProviderName(), idpName);
    }

    @DataProvider
    public Object[][] addIdPExceptionData() {

        IdentityProvider idp1 = new IdentityProvider();
        idp1.setIdentityProviderName("testIdP1");

        IdentityProvider idp2 = new IdentityProvider();
        idp2.setIdentityProviderName("SHARED_testIdP");

        IdentityProvider idp3 = new IdentityProvider();

        return new Object[][]{
                // Already existing IDP.
                {idp1},
                // IDP name with "SHARED_" prefix.
                {idp2},
                // IDP without a name.
                {idp3},
        };
    }

    @Test(dataProvider = "addIdPExceptionData")
    public void testAddIdPException(Object identityProvider) throws Exception {

        addTestIdps();

        assertThrows(IdentityProviderManagementException.class, () ->
                identityProviderManagementService.addIdP((IdentityProvider) identityProvider));
    }

    @DataProvider
    public Object[][] getIdPByNameData() {

        return new Object[][]{
                {"testIdP1"},
                {"testIdP2"},
                {"testIdP3"},
                {userDefinedIdP.getIdentityProviderName()}
        };
    }

    @Test(dataProvider = "getIdPByNameData")
    public void testGetIdPByName(String idpName) throws Exception {

        addTestIdps();

        IdentityProvider idpFromDb = identityProviderManagementService.getIdPByName(idpName);
        assertIdPResult(idpFromDb);
        Assert.assertEquals(idpFromDb.getIdentityProviderName(), idpName);
    }

    @DataProvider
    public Object[][] getIdPByNameNullReturnData() {

        return new Object[][]{
                {"NonExistingIdP"},
        };
    }

    @Test(dataProvider = "getIdPByNameNullReturnData")
    public void testGetIdPByNameNullReturn(String idpName) throws Exception {

        addTestIdps();
        assertNull(identityProviderManagementService.getIdPByName(idpName));
    }

    @DataProvider
    public Object[][] getIdPByNameIllegalArgumentExceptionData() {

        return new Object[][]{
                {""},
                {null},
        };
    }

    @Test(dataProvider = "getIdPByNameIllegalArgumentExceptionData")
    public void testGetIdPByNameIllegalArgumentException(String idpName) throws Exception {

        addTestIdps();

        assertThrows(IllegalArgumentException.class, () ->
                identityProviderManagementService.getIdPByName(idpName));
    }

    @Test
    public void testGetAllIdpCount() throws Exception {

        // Without idp data in database.
        int idpCount = identityProviderManagementService.getAllIdpCount();
        Assert.assertEquals(idpCount, 0);

        // With 3 idps  in database.
        addTestIdps();
        idpCount = identityProviderManagementService.getAllIdpCount();
        Assert.assertEquals(idpCount, 4);
    }

    @Test
    public void testGetAllIdps() throws Exception {

        // Without idp data in database.
        IdentityProvider[] idpsList = identityProviderManagementService.getAllIdPs();
        Assert.assertEquals(idpsList.length, 0);

        // With 3 idps  in database.
        addTestIdps();
        idpsList = identityProviderManagementService.getAllIdPs();
        Assert.assertEquals(idpsList.length, 4);

        // With 3 idps and Shared idp  in database.
        addSharedIdp();
        idpsList = identityProviderManagementService.getAllIdPs();
        Assert.assertEquals(idpsList.length, 4);
    }

    @DataProvider
    public Object[][] getAllPaginatedIdpInfoData() {

        return new Object[][]{
                {1, 4},
                {2, 0},
        };
    }

    @Test(dataProvider = "getAllPaginatedIdpInfoData")
    public void testGetAllPaginatedIdpInfo(int pageNumber, int idpCount) throws Exception {

        addTestIdps();

        IdentityProvider[] idpList = identityProviderManagementService.getAllPaginatedIdpInfo(pageNumber);
        Assert.assertEquals(idpList.length, idpCount);
    }

    @DataProvider
    public Object[][] getAllPaginatedIdpInfoExceptionData() {

        return new Object[][]{
                {0},
                {-1},
        };
    }

    @Test(dataProvider = "getAllPaginatedIdpInfoExceptionData")
    public void testGetAllPaginatedIdpInfoException(int pageNumber) throws Exception {

        addTestIdps();

        assertThrows(IdentityProviderManagementException.class, () ->
                identityProviderManagementService.getAllPaginatedIdpInfo(pageNumber));
    }

    @DataProvider
    public Object[][] getPaginatedIdpInfoData() {

        return new Object[][]{
                {1, "", 4},
                {1, "name sw test", 3},
                {1, "homeRealmIdentifier eq 1", 1},
                {1, "isEnabled co true", 4},
                {1, "isEnabled eq false", 0},
                {1, "id ew NotExist", 0},
                {2, "name eq testIdP2", 0},
        };
    }

    @Test(dataProvider = "getPaginatedIdpInfoData")
    public void testGetPaginatedIdpInfo(int pageNumber, String filter, int idpCount) throws Exception {

        addTestIdps();

        IdentityProvider[] idpList = identityProviderManagementService.getPaginatedIdpInfo(filter, pageNumber);
        Assert.assertEquals(idpList.length, idpCount);
    }

    @DataProvider
    public Object[][] getPaginatedIdpInfoExceptionData() {

        return new Object[][]{
                {1, "Wrong_Filter"},
                {1, "WrongValue eq 1"},
                {1, "name WrongOperator test"},
                {1, "isEnabled eq Wrong"},
        };
    }

    @Test(dataProvider = "getPaginatedIdpInfoExceptionData")
    public void testGetPaginatedIdpInfoException(int pageNumber, String filter) throws Exception {

        addTestIdps();

        assertThrows(IdentityProviderManagementException.class, () ->
                identityProviderManagementService.getPaginatedIdpInfo(filter, pageNumber));
    }

    @DataProvider
    public Object[][] getFilteredIdpCountData() {

        return new Object[][]{
                {"", 4},
                {"name ew 1", 1},
                {"name co IdP", 4},
                {"description eq Test Idp 1", 1}
        };
    }

    @Test(dataProvider = "getFilteredIdpCountData")
    public void testGetFilteredIdpCount(String filter, int idpCount) throws Exception {

        addTestIdps();

        int idpCountFromDb = identityProviderManagementService.getFilteredIdpCount(filter);
        Assert.assertEquals(idpCountFromDb, idpCount);
    }

    @DataProvider
    public Object[][] getAllIdPsSearchData() {

        return new Object[][]{
                {"", 4},
                {"test*", 3},
                {"????IdP*", 3},
                {"tes_I*", 3},
                {"*1", 1},
                {"testIdP1", 1},
                {"Notexist", 0},
        };
    }

    @Test(dataProvider = "getAllIdPsSearchData")
    public void testGetAllIdPsSearch(String filter, int idpCount) throws Exception {

        addTestIdps();
        IdentityProvider[] idpsList = identityProviderManagementService.getAllIdPsSearch(filter);
        Assert.assertEquals(idpsList.length, idpCount);

        // With a shared_idp.
        addSharedIdp();
        idpsList = identityProviderManagementService.getAllIdPsSearch(filter);
        Assert.assertEquals(idpsList.length, idpCount);
    }

    @Test
    public void testGetEnabledAllIdPs() throws Exception {

        addTestIdps();

        IdentityProvider[] idpsList = identityProviderManagementService.getEnabledAllIdPs();
        Assert.assertEquals(idpsList.length, 4);
    }

    @Test
    public void testDeleteIdPActionException() throws Exception {

        identityProviderManagementService.addIdP(userDefinedIdP);

        ActionManagementService actionManagementServiceForException = mock(ActionManagementService.class);
        doThrow(ActionMgtServerException.class).when(actionManagementServiceForException)
                .deleteAction(any(), any(), any());
        when(actionManagementServiceForException.getActionByActionId(anyString(), any(), any())).thenReturn(action);
        IdpMgtServiceComponentHolder.getInstance().setActionManagementService(actionManagementServiceForException);
        identityProviderManagementService.deleteIdP(userDefinedIdP.getIdentityProviderName());
        Assert.assertNull(identityProviderManagementService.getIdPByName(userDefinedIdP
                .getIdentityProviderName()));
    }

    @DataProvider
    public Object[][] deleteIdPData() {

        return new Object[][]{
                {"testIdP1"},
                {"testIdP2"},
                {"testIdP3"},
                {userDefinedIdP.getIdentityProviderName()}
        };
    }

    @Test(dataProvider = "deleteIdPData")
    public void testDeleteIdP(String idpName) throws Exception {

        addTestIdps();

        Assert.assertNotNull(identityProviderManagementService.getIdPByName(idpName));
        identityProviderManagementService.deleteIdP(idpName);
        Assert.assertNull(identityProviderManagementService.getIdPByName(idpName));
    }

    @DataProvider
    public Object[][] deleteIdPExceptionData() {

        return new Object[][]{
                {""},
                {null},
        };
    }

    @Test(dataProvider = "deleteIdPExceptionData")
    public void testDeleteIdPException(String idpName) throws Exception {

        addTestIdps();

        assertThrows(IdentityProviderManagementException.class, () ->
                identityProviderManagementService.deleteIdP(idpName));
    }

    @DataProvider
    public Object[][] forceDeleteIdPData() {

        return new Object[][]{
                {"testIdP1"},
                {"testIdP2"},
                {"testIdP3"},
                {userDefinedIdP.getIdentityProviderName()}
        };
    }

    @Test(dataProvider = "forceDeleteIdPData")
    public void testForceDeleteIdP(String idpName) throws Exception {

        addTestIdps();

        Assert.assertNotNull(identityProviderManagementService.getIdPByName(idpName));
        identityProviderManagementService.forceDeleteIdP(idpName);
        Assert.assertNull(identityProviderManagementService.getIdPByName(idpName));
    }

    @Test(dataProvider = "forceDeleteIdPData")
    public void testForceDeleteIdPDAOException(String idpName) throws Exception {

        addTestIdps();
        Assert.assertNotNull(identityProviderManagementService.getIdPByName(idpName));

        IdPManagementDAO daoForError = mock(IdPManagementDAO.class);
        doThrow(IdentityProviderManagementServerException.class).when(daoForError)
                .forceDeleteIdPByResourceId(anyString(), anyInt(), anyString());
        daoForException = new CacheBackedIdPMgtDAO(daoForError);
        field.set(identityProviderManager, daoForException);

        assertThrows(IdentityProviderManagementException.class, () ->
                identityProviderManagementService.forceDeleteIdP(idpName));

        field.set(identityProviderManager, dao);
        Assert.assertNotNull(identityProviderManagementService.getIdPByName(idpName));
    }

    @DataProvider
    public Object[][] forceDeleteIdPExceptionData() {

        return new Object[][]{
                {""},
                {null},
        };
    }

    @Test(dataProvider = "forceDeleteIdPExceptionData")
    public void testForceDeleteIdPException(String idpName) throws Exception {

        addTestIdps();

        assertThrows(IdentityProviderManagementException.class, () ->
                identityProviderManagementService.forceDeleteIdP(idpName));
    }

    @Test
    public void testUpdateIdPActionException() throws Exception {

        IdentityProvider idpForErrorScenariosTobeUpdate = ActionMgtTestUtil.
                createIdPWithUserDefinedFederatedAuthenticatorConfig(
                idpForErrorScenarios.getIdentityProviderName(), endpointConfig);
        identityProviderManagementService.addIdP(idpForErrorScenarios);

        ActionManagementService actionManagementServiceForException = mock(ActionManagementService.class);
        when(actionManagementServiceForException.updateAction(any(), any(), any(), any()))
                .thenThrow(ActionMgtServerException.class);
        when(actionManagementServiceForException.getActionByActionId(anyString(), any(), any())).thenReturn(action);
        IdpMgtServiceComponentHolder.getInstance().setActionManagementService(actionManagementServiceForException);

        assertThrows(IdentityProviderManagementServerException.class, () ->
                identityProviderManagementService.updateIdP(idpForErrorScenariosTobeUpdate.getIdentityProviderName(),
                        idpForErrorScenarios));
        identityProviderManagementService.getIdPByName(idpForErrorScenarios.getIdentityProviderName());
    }

    @DataProvider
    public Object[][] updateIdPData() {

        // Initialize New Test Identity Provider 1.
        IdentityProvider idp1New = new IdentityProvider();
        idp1New.setIdentityProviderName("testIdP1New");
        idp1New.setEnable(true);
        idp1New.setPrimary(true);
        idp1New.setFederationHub(true);
        idp1New.setCertificate("");

        RoleMapping newRoleMapping1 = new RoleMapping();
        newRoleMapping1.setRemoteRole("Role1New");
        newRoleMapping1.setLocalRole(new LocalRole("1", "LocalRole1"));
        RoleMapping newRoleMapping2 = new RoleMapping();
        newRoleMapping2.setRemoteRole("Role2New");
        newRoleMapping2.setLocalRole(new LocalRole("2", "LocalRole2"));

        PermissionsAndRoleConfig newPermissionsAndRoleConfig = new PermissionsAndRoleConfig();
        newPermissionsAndRoleConfig.setIdpRoles(new String[]{"Role1New", "Role2New"});
        newPermissionsAndRoleConfig.setRoleMappings(new RoleMapping[]{newRoleMapping1, newRoleMapping2});
        idp1New.setPermissionAndRoleConfig(newPermissionsAndRoleConfig);

        FederatedAuthenticatorConfig newFederatedAuthenticatorConfig = new FederatedAuthenticatorConfig();
        newFederatedAuthenticatorConfig.setDisplayName("DisplayName1New");
        newFederatedAuthenticatorConfig.setName("Name");
        newFederatedAuthenticatorConfig.setEnabled(true);
        newFederatedAuthenticatorConfig.setDefinedByType(DefinedByType.SYSTEM);
        Property newProperty1 = new Property();
        newProperty1.setName("Property1New");
        newProperty1.setValue("value1New");
        newProperty1.setConfidential(false);
        Property newProperty2 = new Property();
        newProperty2.setName("Property2New");
        newProperty2.setValue("value2New");
        newProperty2.setConfidential(false);
        newFederatedAuthenticatorConfig.setProperties(new Property[]{newProperty1, newProperty2});
        idp1New.setFederatedAuthenticatorConfigs(new FederatedAuthenticatorConfig[]{newFederatedAuthenticatorConfig});

        ProvisioningConnectorConfig newProvisioningConnectorConfig1 = new ProvisioningConnectorConfig();
        newProvisioningConnectorConfig1.setName("ProvisiningConfig1");
        newProvisioningConnectorConfig1.setProvisioningProperties(new Property[]{newProperty1});
        ProvisioningConnectorConfig newProvisioningConnectorConfig2 = new ProvisioningConnectorConfig();
        newProvisioningConnectorConfig2.setName("ProvisiningConfig2");
        newProvisioningConnectorConfig2.setProvisioningProperties(new Property[]{newProperty2});
        newProvisioningConnectorConfig2.setEnabled(true);
        newProvisioningConnectorConfig2.setBlocking(true);
        idp1New.setProvisioningConnectorConfigs(new ProvisioningConnectorConfig[]{newProvisioningConnectorConfig1,
                newProvisioningConnectorConfig2});

        ClaimConfig newClaimConfig = new ClaimConfig();
        newClaimConfig.setLocalClaimDialect(false);
        newClaimConfig.setRoleClaimURI("Country");
        newClaimConfig.setUserClaimURI("Country");
        ClaimMapping claimMapping = ClaimMapping.build("http://wso2.org/claims/country", "Country", "", true);
        Claim remoteClaim = new Claim();
        remoteClaim.setClaimId(0);
        remoteClaim.setClaimUri("Country");
        newClaimConfig.setClaimMappings(new ClaimMapping[]{claimMapping});
        newClaimConfig.setIdpClaims(new Claim[]{remoteClaim});
        idp1New.setClaimConfig(newClaimConfig);

        // Initialize New Test Identity Provider 2.
        IdentityProvider idp2New = new IdentityProvider();
        idp2New.setIdentityProviderName("testIdP2New");

        // Initialize New Test Identity Provider 3.
        IdentityProvider idp3New = new IdentityProvider();
        idp3New.setIdentityProviderName("testIdP3New");

        IdentityProvider updateIdPWithExistingUserDefinedFedAuth = ActionMgtTestUtil.
                createIdPWithUserDefinedFederatedAuthenticatorConfig(CUSTOM_IDP_NAME + "new", endpointConfigToBeUpdated);
        IdentityProvider updateIdPWithNewUserDefinedFedAuth = ActionMgtTestUtil.
                createIdPWithUserDefinedFederatedAuthenticatorConfig(CUSTOM_IDP_NAME + "new", endpointConfigToBeUpdated);
        updateIdPWithNewUserDefinedFedAuth.getFederatedAuthenticatorConfigs()[0].setName("New Fed Auth");


        return new Object[][]{
                // IDP with PermissionsAndRoleConfig,FederatedAuthenticatorConfig,ProvisioningConnectorConfig,ClaimConf.
                {"testIdP1", idp1New},
                // New IDP with Only name.
                {"testIdP2", idp2New},
                // New IDP with Only name.
                {"testIdP3", idp3New},
                // IDP with User Defined Federated Authenticator.
                {userDefinedIdP.getIdentityProviderName(), updateIdPWithExistingUserDefinedFedAuth}
        };
    }

    @Test(dataProvider = "updateIdPData")
    public void testUpdateIdP(String oldIdpName, Object newIdp) throws Exception {

        addTestIdps();
        identityProviderManagementService.updateIdP(oldIdpName, (IdentityProvider) newIdp);
        String newIdpName = ((IdentityProvider) newIdp).getIdentityProviderName();

        Assert.assertNull(identityProviderManagementService.getIdPByName(oldIdpName));
        IdentityProvider newIdpFromDb = identityProviderManagementService.getIdPByName(newIdpName);
        Assert.assertNotNull(newIdpFromDb);
        assertIdPResult(newIdpFromDb);
    }

    @Test(dataProvider = "updateIdPData")
    public void testUpdateIdPByResourceId(String oldIdpName, Object newIdp) throws Exception {

        addTestIdps();
        IdentityProvider oldIdp = identityProviderManagementService.getIdPByName(oldIdpName);
        IdentityProviderManager.getInstance()
                .updateIdPByResourceId(oldIdp.getResourceId(), (IdentityProvider) newIdp, "carbon.super");
        String newIdpName = ((IdentityProvider) newIdp).getIdentityProviderName();

        Assert.assertNull(identityProviderManagementService.getIdPByName(oldIdpName));
        IdentityProvider newIdpFromDb = identityProviderManagementService.getIdPByName(newIdpName);
        Assert.assertNotNull(newIdpFromDb);
        assertIdPResult(newIdpFromDb);
    }

    @DataProvider
    public Object[][] updateIdPExceptionData() {

        // Initialize New Test Identity Provider 1.
        IdentityProvider idp1New = new IdentityProvider();
        idp1New.setIdentityProviderName("testIdP1New");

        // Initialize New Test Identity Provider 2.
        IdentityProvider idp2New = new IdentityProvider();

        return new Object[][]{
                {"NonExist", idp1New},
                {"testIdP2", idp2New},
        };
    }

    @Test(dataProvider = "updateIdPExceptionData")
    public void testUpdateIdPException(String oldIdpName, Object newIdp) throws Exception {

        addTestIdps();

        assertThrows(IdentityProviderManagementException.class, () ->
                identityProviderManagementService.updateIdP(oldIdpName, (IdentityProvider) newIdp));
    }

    @Test
    public void testGetIdPActionException() throws Exception {

        ActionManagementService actionManagementServiceForException = mock(ActionManagementService.class);
        when(actionManagementServiceForException.addAction(anyString(), any(), any())).thenReturn(action);
        when(actionManagementServiceForException.getActionByActionId(anyString(), any(), any()))
                .thenThrow(ActionMgtServerException.class);
        IdpMgtServiceComponentHolder.getInstance().setActionManagementService(actionManagementServiceForException);

        IdentityProviderManagementServerException error = assertThrows(IdentityProviderManagementServerException.class,
                () -> identityProviderManagementService.addIdP(idpForErrorScenarios));
        assertEquals(error.getErrorCode(), ErrorMessage.ERROR_CODE_RETRIEVING_ENDPOINT_CONFIG.getCode());
    }

    @Test
    public void testGetAllLocalClaimUris() throws Exception {

        ClaimMetadataManagementServiceImpl claimMetadataManagementService =
                mock(ClaimMetadataManagementServiceImpl.class);
        IdpMgtServiceComponentHolder.getInstance().setClaimMetadataManagementService(claimMetadataManagementService);

        LocalClaim localClaim1 = new LocalClaim("http://wso2.org/claims/test1");
        List<LocalClaim> claimList = new ArrayList<>();
        claimList.add(localClaim1);
        when(claimMetadataManagementService.getLocalClaims(anyString())).thenReturn(claimList);

        String[] allLocalClaimUris = identityProviderManagementService.getAllLocalClaimUris();
        Assert.assertEquals(allLocalClaimUris.length, 1);
        Assert.assertEquals(allLocalClaimUris[0], "http://wso2.org/claims/test1");
    }

    @Test
    public void testGetAllLocalClaimUrisException() throws Exception {

        ClaimMetadataManagementServiceImpl claimMetadataManagementService =
                mock(ClaimMetadataManagementServiceImpl.class);
        IdpMgtServiceComponentHolder.getInstance().setClaimMetadataManagementService(claimMetadataManagementService);
        when(claimMetadataManagementService.getLocalClaims(anyString())).thenThrow(ClaimMetadataException.class);

        assertThrows(IdentityProviderManagementException.class, () ->
                identityProviderManagementService.getAllLocalClaimUris());
    }

    @Test
    public void testGetAllFederatedAuthenticators() throws Exception {

        FederatedAuthenticatorConfig[] allFederatedAuthenticators =
                identityProviderManagementService.getAllFederatedAuthenticators();
        Assert.assertEquals(allFederatedAuthenticators.length, 3);

        FederatedAuthenticatorConfig federatedAuthenticatorConfig1 = new FederatedAuthenticatorConfig();
        federatedAuthenticatorConfig1.setDisplayName("DisplayName1");
        federatedAuthenticatorConfig1.setName("Name1");
        federatedAuthenticatorConfig1.setEnabled(true);
        FederatedAuthenticatorConfig federatedAuthenticatorConfig2 = new FederatedAuthenticatorConfig();
        federatedAuthenticatorConfig2.setDisplayName("DisplayName2");
        federatedAuthenticatorConfig2.setName("Name2");
        federatedAuthenticatorConfig2.setEnabled(true);

        ApplicationAuthenticatorService.getInstance().addFederatedAuthenticator(federatedAuthenticatorConfig1);
        allFederatedAuthenticators = identityProviderManagementService.getAllFederatedAuthenticators();
        Assert.assertEquals(allFederatedAuthenticators.length, 4);

        ApplicationAuthenticatorService.getInstance().addFederatedAuthenticator(federatedAuthenticatorConfig2);
        allFederatedAuthenticators = identityProviderManagementService.getAllFederatedAuthenticators();
        Assert.assertEquals(allFederatedAuthenticators.length, 5);

        // Clear after the test.
        ApplicationAuthenticatorService.getInstance().removeFederatedAuthenticator(federatedAuthenticatorConfig1);
        ApplicationAuthenticatorService.getInstance().removeFederatedAuthenticator(federatedAuthenticatorConfig2);

        allFederatedAuthenticators = identityProviderManagementService.getAllFederatedAuthenticators();
        Assert.assertEquals(allFederatedAuthenticators.length, 3);
    }

    @Test
    public void testGetAllProvisioningConnectors() throws Exception {

        ProvisioningConnectorConfig[] allProvisioningConnectors =
                identityProviderManagementService.getAllProvisioningConnectors();
        Assert.assertNull(allProvisioningConnectors);

        ProvisioningConnectorConfig provisioningConnectorConfig1 = mock(ProvisioningConnectorConfig.class);
        provisioningConnectorConfig1.setName("ProvisiningConfig1");
        ProvisioningConnectorConfig provisioningConnectorConfig2 = mock(ProvisioningConnectorConfig.class);
        provisioningConnectorConfig2.setName("ProvisiningConfig2");
        provisioningConnectorConfig2.setEnabled(true);
        provisioningConnectorConfig2.setBlocking(true);

        ProvisioningConnectorService.getInstance().addProvisioningConnectorConfigs(provisioningConnectorConfig1);
        allProvisioningConnectors = identityProviderManagementService.getAllProvisioningConnectors();
        Assert.assertEquals(allProvisioningConnectors.length, 1);

        ProvisioningConnectorService.getInstance().addProvisioningConnectorConfigs(provisioningConnectorConfig2);
        allProvisioningConnectors = identityProviderManagementService.getAllProvisioningConnectors();
        Assert.assertEquals(allProvisioningConnectors.length, 2);

        // Clear after the test.
        ProvisioningConnectorService.getInstance().removeProvisioningConnectorConfigs(provisioningConnectorConfig1);
        ProvisioningConnectorService.getInstance().removeProvisioningConnectorConfigs(provisioningConnectorConfig2);

        allProvisioningConnectors = identityProviderManagementService.getAllProvisioningConnectors();
        Assert.assertNull(allProvisioningConnectors);
    }

    @Test
    public void testGetResidentIdP() throws Exception {

        addResidentIdp();
        IdentityProvider idpFromDb = identityProviderManagementService.getResidentIdP();

        // Verify that the orgResourceResolverService is not called for the root org.
        verify(orgResourceResolverService, times(0)).getResourcesFromOrgHierarchy(
                eq(ROOT_ORG_ID), any(), any());
        Assert.assertNotNull(idpFromDb);
        Assert.assertEquals(idpFromDb.getIdentityProviderName(), "LOCAL");
    }

    @Test
    public void testGetSubOrgResidentIdP() throws Exception {

        organizationManagementUtilMockedStatic.when(() -> OrganizationManagementUtil.isOrganization(anyInt()))
                .thenReturn(true);
        organizationManagementUtilMockedStatic.when(() -> OrganizationManagementUtil.isOrganization(anyString()))
                .thenReturn(true);
        utilsMockedStatic.when(() -> Utils.isLoginAndRegistrationConfigInheritanceEnabled(anyString()))
                .thenReturn(true);
        utilsMockedStatic.when(() -> Utils.isLoginAndRegistrationConfigInheritanceEnabled(any()))
                .thenReturn(true);
        addResidentIdp();
        IdentityProvider idpFromDb = identityProviderManagementService.getResidentIdP();

        verify(orgResourceResolverService, times(3)).getResourcesFromOrgHierarchy(
                eq(ROOT_ORG_ID), any(), any());
        Assert.assertNotNull(idpFromDb);
        Assert.assertEquals(idpFromDb.getIdentityProviderName(), "LOCAL");
    }

    @Test
    public void testGetSubOrgResidentIdPInheritanceDisabled() throws Exception {

        organizationManagementUtilMockedStatic.when(() -> OrganizationManagementUtil.isOrganization(anyInt()))
                .thenReturn(true);
        organizationManagementUtilMockedStatic.when(() -> OrganizationManagementUtil.isOrganization(anyString()))
                .thenReturn(true);
        utilsMockedStatic.when(() -> Utils.isLoginAndRegistrationConfigInheritanceEnabled(anyString()))
                .thenReturn(false);
        utilsMockedStatic.when(() -> Utils.isLoginAndRegistrationConfigInheritanceEnabled(any()))
                .thenReturn(false);
        addResidentIdp();
        IdentityProvider idpFromDb = identityProviderManagementService.getResidentIdP();

        verify(orgResourceResolverService, times(0)).getResourcesFromOrgHierarchy(
                eq(ROOT_ORG_ID), any(), any());
        Assert.assertNotNull(idpFromDb);
        Assert.assertEquals(idpFromDb.getIdentityProviderName(), "LOCAL");
    }

    @Test
    public void testGetResidentIdPException() throws Exception {

        assertThrows(IdentityProviderManagementException.class, () ->
                identityProviderManagementService.getResidentIdP());
    }

    @DataProvider
    public Object[][] updateResidentIdPData() {

        // Initialize New Resident Identity Provider 1.
        IdentityProvider idp1New = new IdentityProvider();
        idp1New.setIdentityProviderName("LOCAL");

        IdentityProviderProperty idpProperty1 = new IdentityProviderProperty();
        idpProperty1.setName(IdentityApplicationConstants.SESSION_IDLE_TIME_OUT);
        idpProperty1.setValue("100");
        IdentityProviderProperty idpProperty2 = new IdentityProviderProperty();
        idpProperty2.setName(IdentityApplicationConstants.REMEMBER_ME_TIME_OUT);
        idpProperty2.setValue("10000");
        IdentityProviderProperty idpProperty3 = new IdentityProviderProperty();
        idpProperty3.setName(IdentityApplicationConstants.Authenticator.SAML2SSO.SAML_METADATA_VALIDITY_PERIOD);
        idpProperty3.setValue("10000");
        IdentityProviderProperty idpProperty4 = new IdentityProviderProperty();
        idpProperty4.setName(IdentityApplicationConstants.Authenticator.SAML2SSO.SAML_METADATA_SIGNING_ENABLED);
        idpProperty4.setValue("true");
        idp1New.setIdpProperties(new IdentityProviderProperty[]{idpProperty1, idpProperty2, idpProperty3,
                idpProperty4});

        // Initialize New Resident Identity Provider 2.
        IdentityProvider idp2New = new IdentityProvider();
        idp2New.setIdentityProviderName("LOCAL");

        FederatedAuthenticatorConfig facNew = new FederatedAuthenticatorConfig();
        facNew.setDisplayName("DisplayName1New");
        facNew.setName("Name");
        facNew.setEnabled(true);
        facNew.setDefinedByType(DefinedByType.SYSTEM);
        idp2New.setFederatedAuthenticatorConfigs(new FederatedAuthenticatorConfig[]{facNew});

        // Initialize New Resident Identity Provider 3.
        IdentityProvider idp3New = new IdentityProvider();
        idp3New.setIdentityProviderName("LOCAL");

        return new Object[][]{
                // New Resident IDP with new IDP Properties.
                {idp1New},
                // New Resident IDP with new FederatedAuthenticatorConfigs.
                {idp2New},
                // New Resident IDP with no object properties (only contains the name).
                {idp3New},
        };
    }

    @Test(dataProvider = "updateResidentIdPData")
    public void testUpdateResidentIdP(Object newIdp) throws Exception {

        addResidentIdp();

        identityProviderManagementService.updateResidentIdP((IdentityProvider) newIdp);
        Assert.assertNotNull(identityProviderManagementService.getResidentIdP());
    }

    @DataProvider
    public Object[][] updateResidentIdPExceptionData() {

        // Initialize New Test Resident Identity Provider 1.
        IdentityProvider idp1New = new IdentityProvider();
        idp1New.setIdentityProviderName("LOCAL");
        IdentityProviderProperty idpProperty1 = new IdentityProviderProperty();
        idpProperty1.setName(IdentityApplicationConstants.SESSION_IDLE_TIME_OUT);
        idpProperty1.setValue("-1");
        idp1New.setIdpProperties(new IdentityProviderProperty[]{idpProperty1});

        // Initialize New Test Resident Identity Provider 2.
        IdentityProvider idp2New = new IdentityProvider();
        idp2New.setIdentityProviderName("LOCAL");
        IdentityProviderProperty idpProperty2 = new IdentityProviderProperty();
        idpProperty2.setName(IdentityApplicationConstants.REMEMBER_ME_TIME_OUT);
        idpProperty2.setValue("Invalid");
        idp2New.setIdpProperties(new IdentityProviderProperty[]{idpProperty2});

        // Initialize New Test Resident Identity Provider 3.
        IdentityProvider idp3New = new IdentityProvider();
        idp3New.setIdentityProviderName("LOCAL");
        IdentityProviderProperty idpProperty3 = new IdentityProviderProperty();
        idpProperty3.setName(IdentityApplicationConstants.Authenticator.SAML2SSO.SAML_METADATA_VALIDITY_PERIOD);
        idpProperty3.setValue("");
        idp3New.setIdpProperties(new IdentityProviderProperty[]{idpProperty3});

        // Initialize New Test Resident Identity Provider 4.
        IdentityProvider idp4New = new IdentityProvider();
        idp4New.setIdentityProviderName("LOCAL");
        IdentityProviderProperty idpProperty4 = new IdentityProviderProperty();
        idpProperty4.setName(IdentityApplicationConstants.Authenticator.SAML2SSO.SAML_METADATA_SIGNING_ENABLED);
        idpProperty4.setValue("");
        idp4New.setIdpProperties(new IdentityProviderProperty[]{idpProperty4});

        return new Object[][]{
                // New Resident IDP with Invalid value to the 'SESSION_IDLE_TIME_OUT' idp property.
                {idp1New},
                // New Resident IDP with Invalid value to the 'REMEMBER_ME_TIME_OUT' idp property.
                {idp2New},
                // New Resident IDP with Invalid 'SAML_METADATA_VALIDITY_PERIOD' idp property value.
                {idp3New},
                // New Resident IDP with Invalid 'SAML_METADATA_SIGNING_ENABLED' idp property value.
                {idp4New},
        };
    }

    @Test(dataProvider = "updateResidentIdPExceptionData")
    public void testUpdateResidentIdPException(Object newIdp) throws Exception {

        addResidentIdp();

        assertThrows(IdentityProviderManagementException.class, () ->
                identityProviderManagementService.updateResidentIdP((IdentityProvider) newIdp));
    }

    @Test
    public void testGetResidentIDPMetadata() throws Exception {

        addResidentIdp();
        Assert.assertNull(identityProviderManagementService.getResidentIDPMetadata());

        when(mockMetadataConverter.canHandle(any(FederatedAuthenticatorConfig.class))).thenReturn(TRUE);
        when(mockMetadataConverter.getMetadataString(any(FederatedAuthenticatorConfig.class))).
                thenReturn("saml2sso");

        IdentityProvider newIdp = new IdentityProvider();
        newIdp.setIdentityProviderName("LOCAL");
        FederatedAuthenticatorConfig facNew = new FederatedAuthenticatorConfig();
        facNew.setDisplayName("SAML2SSO");
        facNew.setName("saml2sso");
        facNew.setEnabled(true);
        facNew.setDefinedByType(DefinedByType.SYSTEM);
        newIdp.setFederatedAuthenticatorConfigs(new FederatedAuthenticatorConfig[]{facNew});
        identityProviderManagementService.updateResidentIdP((IdentityProvider) newIdp);

        Assert.assertNotNull(identityProviderManagementService.getResidentIDPMetadata());
        Assert.assertEquals(identityProviderManagementService.getResidentIDPMetadata(), "saml2sso");
    }

    @Test
    public void testGetResidentIDPMetadataException() throws Exception {

        addResidentIdp();

        when(mockMetadataConverter.canHandle(any(FederatedAuthenticatorConfig.class))).thenReturn(TRUE);
        when(mockMetadataConverter.getMetadataString(any(FederatedAuthenticatorConfig.class))).thenThrow
                (IdentityProviderSAMLException.class);

        IdentityProvider newIdp = new IdentityProvider();
        newIdp.setIdentityProviderName("LOCAL");
        FederatedAuthenticatorConfig facNew = new FederatedAuthenticatorConfig();
        facNew.setDisplayName("SAML2SSO");
        facNew.setName("saml2sso");
        facNew.setEnabled(true);
        facNew.setDefinedByType(DefinedByType.SYSTEM);
        newIdp.setFederatedAuthenticatorConfigs(new FederatedAuthenticatorConfig[]{facNew});
        identityProviderManagementService.updateResidentIdP((IdentityProvider) newIdp);

        assertThrows(IdentityProviderManagementException.class, () ->
                identityProviderManagementService.getResidentIDPMetadata());
    }

    @Test
    public void testAddIdPDAOException() throws Exception {

        IdPManagementDAO daoForError = mock(IdPManagementDAO.class);
        doThrow(IdentityProviderManagementServerException.class).when(daoForError)
                .addIdPWithResourceId(any(), anyInt());
        daoForException = new CacheBackedIdPMgtDAO(daoForError);
        field.set(identityProviderManager, daoForException);

        assertThrows(IdentityProviderManagementServerException.class, () ->
                identityProviderManagementService.addIdP(userDefinedIdP));

        // check ActionManagementService actionManagementService.deleteAction() is called.
        verify(actionManagementService, times(1)).deleteAction(anyString(), any(), any());
    }

    @Test
    public void testUpdateIdPDAOException() throws Exception {

        identityProviderManagementService.addIdP(userDefinedIdP);
        IdPManagementDAO daoForError = mock(IdPManagementDAO.class);
        doThrow(IdentityProviderManagementServerException.class).when(daoForError).updateIdPWithResourceId(anyString(),
                any(), any(), anyInt());
        when(daoForError.getIdPByName(any(), anyString(), anyInt(), anyString())).thenReturn(userDefinedIdP);
        daoForException = new CacheBackedIdPMgtDAO(daoForError);
        field.set(identityProviderManager, daoForException);

        assertThrows(IdentityProviderManagementServerException.class, () ->
                identityProviderManagementService.updateIdP(userDefinedIdP.getIdentityProviderName(), userDefinedIdP));

        // check ActionManagementService actionManagementService.deleteAction() is called.
        verify(actionManagementService, times(2)).updateAction(anyString(), anyString(),
                any(), anyString());
    }

    @Test
    public void testDeleteIdPDAOException() throws Exception {

        identityProviderManagementService.addIdP(userDefinedIdP);
        IdPManagementDAO daoForError = mock(IdPManagementDAO.class);
        doThrow(IdentityProviderManagementException.class).when(daoForError)
                .deleteIdPByResourceId(anyString(), anyInt(), anyString());
        when(daoForError.getIdPByName(any(), anyString(), anyInt(), anyString())).thenReturn(userDefinedIdP);
        when(daoForError.getIDPbyResourceId(any(), anyString(), anyInt(), anyString())).thenReturn(userDefinedIdP);
        daoForException = new CacheBackedIdPMgtDAO(daoForError);
        field.set(identityProviderManager, daoForException);

        assertThrows(IdentityProviderManagementException.class, () ->
                identityProviderManagementService.deleteIdP(userDefinedIdP.getIdentityProviderName()));

        /* check ActionManagementService actionManagementService.deleteAction() is called only once when creating the
        user defined federated authenticator. */
        verify(actionManagementService, times(1)).addAction(anyString(), any(), anyString());
    }

    @Test
    public void testDeleteResidentIdpProperties() throws Exception {

        addResidentIdp();
        List<String> propertiesToDelete = new ArrayList<>();
        propertiesToDelete.add(IdentityApplicationConstants.SESSION_IDLE_TIME_OUT);
        IdentityProviderManager.getInstance().deleteResidentIdpProperties(propertiesToDelete, ROOT_TENANT_DOMAIN);
        Assert.assertNotNull(identityProviderManagementService.getResidentIdP());
    }

    private void addTestIdps() throws IdentityProviderManagementException {

        // Initialize Test Identity Provider 1.
        IdentityProvider idp1 = new IdentityProvider();
        idp1.setIdentityProviderName("testIdP1");
        idp1.setIdentityProviderDescription("Test Idp 1");
        idp1.setHomeRealmId("1");
        idp1.setEnable(true);
        idp1.setPrimary(true);
        idp1.setFederationHub(true);
        idp1.setCertificate("");

        RoleMapping roleMapping1 = new RoleMapping();
        roleMapping1.setRemoteRole("Role1");
        roleMapping1.setLocalRole(new LocalRole("1", "LocalRole1"));
        RoleMapping roleMapping2 = new RoleMapping();
        roleMapping2.setRemoteRole("Role2");
        roleMapping2.setLocalRole(new LocalRole("2", "LocalRole2"));

        PermissionsAndRoleConfig permissionsAndRoleConfig = new PermissionsAndRoleConfig();
        permissionsAndRoleConfig.setIdpRoles(new String[]{"Role1", "Role2"});
        permissionsAndRoleConfig.setRoleMappings(new RoleMapping[]{roleMapping1, roleMapping2});
        idp1.setPermissionAndRoleConfig(permissionsAndRoleConfig);

        FederatedAuthenticatorConfig federatedAuthenticatorConfig = new FederatedAuthenticatorConfig();
        federatedAuthenticatorConfig.setDisplayName("DisplayName1");
        federatedAuthenticatorConfig.setName("Name");
        federatedAuthenticatorConfig.setEnabled(true);
        federatedAuthenticatorConfig.setDefinedByType(DefinedByType.SYSTEM);
        Property property1 = new Property();
        property1.setName("Property1");
        property1.setValue("value1");
        property1.setConfidential(true);
        Property property2 = new Property();
        property2.setName("Property2");
        property2.setValue("value2");
        property2.setConfidential(false);
        federatedAuthenticatorConfig.setProperties(new Property[]{property1, property2});
        idp1.setFederatedAuthenticatorConfigs(new FederatedAuthenticatorConfig[]{federatedAuthenticatorConfig});

        ProvisioningConnectorConfig provisioningConnectorConfig1 = new ProvisioningConnectorConfig();
        provisioningConnectorConfig1.setName("ProvisiningConfig1");
        provisioningConnectorConfig1.setProvisioningProperties(new Property[]{property1});
        ProvisioningConnectorConfig provisioningConnectorConfig2 = new ProvisioningConnectorConfig();
        provisioningConnectorConfig2.setName("ProvisiningConfig2");
        provisioningConnectorConfig2.setProvisioningProperties(new Property[]{property2});
        provisioningConnectorConfig2.setEnabled(true);
        provisioningConnectorConfig2.setBlocking(true);
        idp1.setProvisioningConnectorConfigs(new ProvisioningConnectorConfig[]{provisioningConnectorConfig1,
                provisioningConnectorConfig2});

        IdentityProviderProperty identityProviderProperty = new IdentityProviderProperty();
        identityProviderProperty.setDisplayName("idpDisplayName");
        identityProviderProperty.setName("idpPropertyName");
        identityProviderProperty.setValue("idpPropertyValue");
        idp1.setIdpProperties(new IdentityProviderProperty[]{identityProviderProperty});

        ClaimConfig claimConfig = new ClaimConfig();
        claimConfig.setLocalClaimDialect(false);
        claimConfig.setRoleClaimURI("Country");
        claimConfig.setUserClaimURI("Country");
        ClaimMapping claimMapping = ClaimMapping.build("http://wso2.org/claims/country", "Country", "", true);
        Claim remoteClaim = new Claim();
        remoteClaim.setClaimId(0);
        remoteClaim.setClaimUri("Country");
        claimConfig.setClaimMappings(new ClaimMapping[]{claimMapping});
        claimConfig.setIdpClaims(new Claim[]{remoteClaim});
        idp1.setClaimConfig(claimConfig);

        // Initialize Test Identity Provider 2.
        IdentityProvider idp2 = new IdentityProvider();
        idp2.setIdentityProviderName("testIdP2");
        idp2.setHomeRealmId("2");

        ClaimConfig claimConfig2 = new ClaimConfig();
        claimConfig2.setLocalClaimDialect(true);
        claimConfig2.setRoleClaimURI("http://wso2.org/claims/role");
        claimConfig2.setUserClaimURI("http://wso2.org/claims/fullname");
        ClaimMapping claimMapping2 = new ClaimMapping();
        Claim localClaim2 = new Claim();
        localClaim2.setClaimId(0);
        localClaim2.setClaimUri("http://wso2.org/claims/fullname");
        claimMapping2.setLocalClaim(localClaim2);
        claimConfig2.setClaimMappings(new ClaimMapping[]{claimMapping2});
        idp2.setClaimConfig(claimConfig2);

        // Initialize Test Identity Provider 3.
        IdentityProvider idp3 = new IdentityProvider();
        idp3.setIdentityProviderName("testIdP3");
        idp3.setHomeRealmId("3");

        // IDP with PermissionsAndRoleConfig, FederatedAuthenticatorConfigs, ProvisioningConnectorConfigs, ClaimConfigs.
        identityProviderManagementService.addIdP(idp1);

        // IDP with Local Cliam Dialect ClaimConfigs.
        identityProviderManagementService.addIdP(idp2);

        // IDP with Only name.
        identityProviderManagementService.addIdP(idp3);

        // User defined IDP.
        identityProviderManagementService.addIdP(userDefinedIdP);
        userDefinedIdP = identityProviderManagementService.getIdPByName(userDefinedIdP.getIdentityProviderName());
    }

    private void addResidentIdp() throws Exception {

        IdentityProvider residentIdp = new IdentityProvider();
        residentIdp.setIdentityProviderName("LOCAL");
        IdentityProviderProperty idpProperty1 = new IdentityProviderProperty();
        idpProperty1.setName(IdentityApplicationConstants.SESSION_IDLE_TIME_OUT);
        idpProperty1.setValue("20");
        residentIdp.setIdpProperties(new IdentityProviderProperty[]{idpProperty1});

        FederatedAuthenticatorConfig federatedAuthenticatorConfig =
                getFederatedAuthenticatorConfig();
        residentIdp.setFederatedAuthenticatorConfigs(new FederatedAuthenticatorConfig[]{federatedAuthenticatorConfig});

        when(orgResourceResolverService.getResourcesFromOrgHierarchy(
                eq(ROOT_ORG_ID), any(), any())).thenAnswer(invocation -> {
            // Mock the return type based on the passed lambda.
            Function<String, ?> function = invocation.getArgument(1);
            Object result = function.apply(ROOT_ORG_ID);
            if (result instanceof Optional) {
                Optional<?> optionalResult = (Optional<?>) result;
                if (optionalResult.isPresent() && optionalResult.get() instanceof List) {
                    return new ArrayList<>();
                } else if (optionalResult.isPresent() && optionalResult.get() instanceof Set) {
                    return new HashSet<>();
                }
            }
            return Optional.empty();
        });
        identityProviderManagementService.addIdP(residentIdp);
    }

    private static FederatedAuthenticatorConfig getFederatedAuthenticatorConfig() {

        FederatedAuthenticatorConfig federatedAuthenticatorConfig = new FederatedAuthenticatorConfig();
        federatedAuthenticatorConfig.setDisplayName("DisplayName1");
        federatedAuthenticatorConfig.setName("samlsso");
        federatedAuthenticatorConfig.setEnabled(true);
        federatedAuthenticatorConfig.setDefinedByType(DefinedByType.SYSTEM);
        Property property1 = new Property();
        property1.setName("Property1");
        property1.setValue("value1");
        property1.setConfidential(true);
        Property property2 = new Property();
        property2.setName("Property2");
        property2.setValue("value2");
        property2.setConfidential(false);
        federatedAuthenticatorConfig.setProperties(new Property[]{property1, property2});
        return federatedAuthenticatorConfig;
    }

    private void addSharedIdp() throws SQLException, IdentityProviderManagementException {

        try (Connection connection = IdentityDatabaseUtil.getDBConnection(false)) {
            String sqlStmt = IdPManagementConstants.SQLQueries.ADD_IDP_SQL;
            PreparedStatement prepStmt = connection.prepareStatement(sqlStmt);

            prepStmt.setInt(1, SUPER_TENANT_ID);
            prepStmt.setString(2, "SHARED_IDP");
            prepStmt.setString(3, IdPManagementConstants.IS_TRUE_VALUE);
            prepStmt.setString(4, "");
            prepStmt.setBinaryStream(5, new ByteArrayInputStream(new byte[0]), 0);
            prepStmt.setString(6, "");
            prepStmt.setString(7, IdPManagementConstants.IS_FALSE_VALUE);
            prepStmt.setString(8, null);
            prepStmt.setString(9, null);
            prepStmt.setString(10, null);
            prepStmt.setString(11, null);
            prepStmt.setString(12, null);
            prepStmt.setString(13, "SHARED_IDP");
            prepStmt.setString(14, IdPManagementConstants.IS_FALSE_VALUE);
            prepStmt.setString(15, IdPManagementConstants.IS_FALSE_VALUE);
            prepStmt.setString(16, "Role");
            prepStmt.setString(17, IdPManagementConstants.IS_TRUE_VALUE);
            prepStmt.setString(18, "");
            prepStmt.setString(19, "");
            prepStmt.setString(20, "0000");

            prepStmt.executeUpdate();
            prepStmt.clearParameters();
        }
        IdentityProvider sharedIdp = identityProviderManagementService.getIdPByName("SHARED_IDP");
        Assert.assertNotNull(sharedIdp);
    }

    private void removeTestIdps() throws IdentityProviderManagementException {

        IdentityProvider[] idpsFromDb = identityProviderManagementService.getAllIdPs();

        for (IdentityProvider idp : idpsFromDb) {
            // Remove current idps.
            identityProviderManagementService.deleteIdP(idp.getIdentityProviderName());
        }
        // Remove resident idp.
        identityProviderManagementService.deleteIdP("LOCAL");
        // Remove shared idp.
        identityProviderManagementService.deleteIdP("SHARED_IDP");
    }

    @Test(expectedExceptions = {IdentityProviderManagementException.class}, expectedExceptionsMessageRegExp =
            "An Identity Provider Entity ID has already been registered with the name 'localhost' for tenant .*")
    public void testAddIdPWithResourceId() throws IdentityProviderManagementException, XMLStreamException {

        when(mockMetadataConverter.canHandle(any(Property.class))).thenReturn(TRUE);
        when(mockMetadataConverter.getFederatedAuthenticatorConfig(any(), any())).thenReturn(
                federatedAuthenticatorConfigWithIdpEntityIdPropertySet());
        identityProviderManagementService.addIdP(addIdPDataWithSameIdpEntityId("idp1"));
        identityProviderManagementService.addIdP(addIdPDataWithSameIdpEntityId("idp2"));
    }

    private IdentityProvider addIdPDataWithSameIdpEntityId(String idpName) {

        // Initialize Test Identity Provider.
        IdentityProvider idp = new IdentityProvider();
        idp.setIdentityProviderName(idpName);

        FederatedAuthenticatorConfig federatedAuthenticatorConfig = new FederatedAuthenticatorConfig();
        federatedAuthenticatorConfig.setDisplayName("DisplayName");
        federatedAuthenticatorConfig.setName("SAMLSSOAuthenticator");
        federatedAuthenticatorConfig.setEnabled(true);
        federatedAuthenticatorConfig.setDefinedByType(DefinedByType.SYSTEM);
        Property property1 = new Property();
        property1.setName("SPEntityId");
        property1.setValue("wso2-is");
        Property property2 = new Property();
        property2.setName("meta_data_saml");
        property2.setValue("dummyMetadataValue");

        federatedAuthenticatorConfig.setProperties(new Property[]{property1, property2});

        idp.setFederatedAuthenticatorConfigs(new FederatedAuthenticatorConfig[]{federatedAuthenticatorConfig});

        return idp;
    }

    private FederatedAuthenticatorConfig federatedAuthenticatorConfigWithIdpEntityIdPropertySet() {

        // Initialize IdPEntityId Property.
        Property property = new Property();
        property.setName("IdPEntityId");
        property.setValue("localhost");

        // Add to and return federatedAuthenticatorConfig.
        FederatedAuthenticatorConfig federatedAuthenticatorConfig = new FederatedAuthenticatorConfig();
        federatedAuthenticatorConfig.setProperties(new Property[]{property});
        return federatedAuthenticatorConfig;
    }

    private void assertIdPResult(IdentityProvider idpResult) {

        for (FederatedAuthenticatorConfig config : idpResult.getFederatedAuthenticatorConfigs()) {
            if (config instanceof UserDefinedFederatedAuthenticatorConfig) {
                Assert.assertEquals(config.getDefinedByType(), DefinedByType.USER);
                Property[] prop = idpResult.getFederatedAuthenticatorConfigs()[0].getProperties();
                assertEquals(prop.length, 1);
                assertEquals(prop[0].getName(), "actionId");
                assertEquals(prop[0].getValue(), ASSOCIATED_ACTION_ID);
            } else {
                Assert.assertEquals(config.getDefinedByType(), DefinedByType.SYSTEM);
            }
        }
    }
}
