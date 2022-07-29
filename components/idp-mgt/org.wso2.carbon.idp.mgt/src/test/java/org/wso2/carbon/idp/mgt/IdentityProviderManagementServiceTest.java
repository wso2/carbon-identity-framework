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

package org.wso2.carbon.idp.mgt;

import org.mockito.Mock;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.modules.testng.PowerMockTestCase;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
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
import org.wso2.carbon.identity.application.common.util.IdentityApplicationConstants;
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
import org.wso2.carbon.idp.mgt.internal.IdpMgtServiceComponentHolder;
import org.wso2.carbon.idp.mgt.util.IdPManagementConstants;
import org.wso2.carbon.idp.mgt.util.MetadataConverter;

import java.io.ByteArrayInputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.xml.stream.XMLStreamException;

import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.when;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertThrows;
import static org.wso2.carbon.base.MultitenantConstants.SUPER_TENANT_ID;
import static java.lang.Boolean.TRUE;

/**
 * Unit tests for IdentityProviderManagementService.
 */
@WithAxisConfiguration
@WithCarbonHome
@WithRegistry
@WithRealmService(injectToSingletons = {IdpMgtServiceComponentHolder.class}, initUserStoreManager = true)
@WithH2Database(jndiName = "jdbc/WSO2IdentityDB", files = {"dbscripts/h2.sql"})
@WithKeyStore
@PowerMockIgnore("org.mockito.*")
public class IdentityProviderManagementServiceTest extends PowerMockTestCase {

    @Mock
    MetadataConverter mockMetadataConverter;
    private IdentityProviderManagementService identityProviderManagementService;

    @BeforeMethod
    public void setUp() throws Exception {

        identityProviderManagementService = new IdentityProviderManagementService();
        List<MetadataConverter> metadataConverterList = Arrays.asList(mockMetadataConverter);
        IdpMgtServiceComponentHolder.getInstance().setMetadataConverters(metadataConverterList);
    }

    @AfterMethod
    public void tearDown() throws Exception {

        // Clear Database after every test.
        removeTestIdps();
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
        };
    }

    @Test(dataProvider = "addIdPData")
    public void testAddIdP(Object identityProvider) throws Exception {

        String idpName = ((IdentityProvider) identityProvider).getIdentityProviderName();
        identityProviderManagementService.addIdP(((IdentityProvider) identityProvider));

        IdentityProvider idpFromDb = identityProviderManagementService.getIdPByName(idpName);
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
        };
    }

    @Test(dataProvider = "getIdPByNameData")
    public void testGetIdPByName(String idpName) throws Exception {

        addTestIdps();

        IdentityProvider idpFromDb = identityProviderManagementService.getIdPByName(idpName);
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
        Assert.assertEquals(idpCount, 3);
    }

    @Test
    public void testGetAllIdps() throws Exception {

        // Without idp data in database.
        IdentityProvider[] idpsList = identityProviderManagementService.getAllIdPs();
        Assert.assertEquals(idpsList.length, 0);

        // With 3 idps  in database.
        addTestIdps();
        idpsList = identityProviderManagementService.getAllIdPs();
        Assert.assertEquals(idpsList.length, 3);

        // With 3 idps and Shared idp  in database.
        addSharedIdp();
        idpsList = identityProviderManagementService.getAllIdPs();
        Assert.assertEquals(idpsList.length, 3);
    }

    @DataProvider
    public Object[][] getAllPaginatedIdpInfoData() {

        return new Object[][]{
                {1, 3},
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
                {1, "", 3},
                {1, "name sw test", 3},
                {1, "homeRealmIdentifier eq 1", 1},
                {1, "isEnabled co true", 3},
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
                {"", 3},
                {"name ew 1", 1},
                {"name co IdP", 3},
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
                {"", 3},
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
        Assert.assertEquals(idpsList.length, 3);
    }

    @DataProvider
    public Object[][] deleteIdPData() {

        return new Object[][]{
                {"testIdP1"},
                {"testIdP2"},
                {"testIdP3"},
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
        };
    }

    @Test(dataProvider = "forceDeleteIdPData")
    public void testForceDeleteIdP(String idpName) throws Exception {

        addTestIdps();

        Assert.assertNotNull(identityProviderManagementService.getIdPByName(idpName));
        identityProviderManagementService.forceDeleteIdP(idpName);
        Assert.assertNull(identityProviderManagementService.getIdPByName(idpName));
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

        return new Object[][]{
                // IDP with PermissionsAndRoleConfig,FederatedAuthenticatorConfig,ProvisioningConnectorConfig,ClaimConf.
                {"testIdP1", idp1New},
                // New IDP with Only name.
                {"testIdP2", idp2New},
                // New IDP with Only name.
                {"testIdP3", idp3New},
        };
    }

    @Test(dataProvider = "updateIdPData")
    public void testUpdateIdP(String oldIdpName, Object newIdp) throws Exception {

        addTestIdps();
        identityProviderManagementService.updateIdP(oldIdpName, (IdentityProvider) newIdp);
        String newIdpName = ((IdentityProvider) newIdp).getIdentityProviderName();

        Assert.assertNull(identityProviderManagementService.getIdPByName(oldIdpName));
        Assert.assertNotNull(identityProviderManagementService.getIdPByName(newIdpName));
    }

    @Test(dataProvider = "updateIdPData")
    public void testUpdateIdPByResourceId(String oldIdpName, Object newIdp) throws Exception {

        addTestIdps();
        IdentityProvider oldIdp = identityProviderManagementService.getIdPByName(oldIdpName);
        IdentityProviderManager.getInstance()
                .updateIdPByResourceId(oldIdp.getResourceId(), (IdentityProvider) newIdp, "carbon.super");
        String newIdpName = ((IdentityProvider) newIdp).getIdentityProviderName();

        Assert.assertNull(identityProviderManagementService.getIdPByName(oldIdpName));
        Assert.assertNotNull(identityProviderManagementService.getIdPByName(newIdpName));
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
        Assert.assertEquals(allFederatedAuthenticators.length, 0);

        FederatedAuthenticatorConfig federatedAuthenticatorConfig1 = mock(FederatedAuthenticatorConfig.class);
        federatedAuthenticatorConfig1.setDisplayName("DisplayName1");
        federatedAuthenticatorConfig1.setName("Name1");
        federatedAuthenticatorConfig1.setEnabled(true);
        FederatedAuthenticatorConfig federatedAuthenticatorConfig2 = mock(FederatedAuthenticatorConfig.class);
        federatedAuthenticatorConfig2.setDisplayName("DisplayName2");
        federatedAuthenticatorConfig2.setName("Name2");
        federatedAuthenticatorConfig2.setEnabled(true);

        ApplicationAuthenticatorService.getInstance().addFederatedAuthenticator(federatedAuthenticatorConfig1);
        allFederatedAuthenticators = identityProviderManagementService.getAllFederatedAuthenticators();
        Assert.assertEquals(allFederatedAuthenticators.length, 1);

        ApplicationAuthenticatorService.getInstance().addFederatedAuthenticator(federatedAuthenticatorConfig2);
        allFederatedAuthenticators = identityProviderManagementService.getAllFederatedAuthenticators();
        Assert.assertEquals(allFederatedAuthenticators.length, 2);

        // Clear after the test.
        ApplicationAuthenticatorService.getInstance().removeFederatedAuthenticator(federatedAuthenticatorConfig1);
        ApplicationAuthenticatorService.getInstance().removeFederatedAuthenticator(federatedAuthenticatorConfig2);

        allFederatedAuthenticators = identityProviderManagementService.getAllFederatedAuthenticators();
        Assert.assertEquals(allFederatedAuthenticators.length, 0);
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

        when(mockMetadataConverter.canHandle((FederatedAuthenticatorConfig) anyObject())).thenReturn(TRUE);
        when(mockMetadataConverter.getMetadataString((FederatedAuthenticatorConfig) anyObject())).
                thenReturn("saml2sso");

        IdentityProvider newIdp = new IdentityProvider();
        newIdp.setIdentityProviderName("LOCAL");
        FederatedAuthenticatorConfig facNew = new FederatedAuthenticatorConfig();
        facNew.setDisplayName("SAML2SSO");
        facNew.setName("saml2sso");
        facNew.setEnabled(true);
        newIdp.setFederatedAuthenticatorConfigs(new FederatedAuthenticatorConfig[]{facNew});
        identityProviderManagementService.updateResidentIdP((IdentityProvider) newIdp);

        Assert.assertNotNull(identityProviderManagementService.getResidentIDPMetadata());
        Assert.assertEquals(identityProviderManagementService.getResidentIDPMetadata(), "saml2sso");
    }

    @Test
    public void testGetResidentIDPMetadataException() throws Exception {

        addResidentIdp();

        when(mockMetadataConverter.canHandle((FederatedAuthenticatorConfig) anyObject())).thenReturn(TRUE);
        when(mockMetadataConverter.getMetadataString((FederatedAuthenticatorConfig) anyObject())).thenThrow
                (IdentityProviderSAMLException.class);

        IdentityProvider newIdp = new IdentityProvider();
        newIdp.setIdentityProviderName("LOCAL");
        FederatedAuthenticatorConfig facNew = new FederatedAuthenticatorConfig();
        facNew.setDisplayName("SAML2SSO");
        facNew.setName("saml2sso");
        facNew.setEnabled(true);
        newIdp.setFederatedAuthenticatorConfigs(new FederatedAuthenticatorConfig[]{facNew});
        identityProviderManagementService.updateResidentIdP((IdentityProvider) newIdp);

        assertThrows(IdentityProviderManagementException.class, () ->
                identityProviderManagementService.getResidentIDPMetadata());
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
    }

    private void addResidentIdp() throws IdentityProviderManagementException {

        IdentityProvider residentIdp = new IdentityProvider();
        residentIdp.setIdentityProviderName("LOCAL");
        IdentityProviderProperty idpProperty1 = new IdentityProviderProperty();
        idpProperty1.setName(IdentityApplicationConstants.SESSION_IDLE_TIME_OUT);
        idpProperty1.setValue("20");
        residentIdp.setIdpProperties(new IdentityProviderProperty[]{idpProperty1});

        identityProviderManagementService.addIdP(residentIdp);
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

        when(mockMetadataConverter.canHandle((Property) anyObject())).thenReturn(TRUE);
        when(mockMetadataConverter.getFederatedAuthenticatorConfig(anyObject(), anyObject())).thenReturn(
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

}
