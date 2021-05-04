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

package org.wso2.carbon.idp.mgt.dao;

import org.apache.commons.dbcp.BasicDataSource;
import org.apache.commons.lang.StringUtils;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.testng.PowerMockTestCase;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

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
import org.wso2.carbon.identity.core.model.ExpressionNode;
import org.wso2.carbon.identity.core.util.IdentityDatabaseUtil;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;
import org.wso2.carbon.identity.core.util.IdentityUtil;
import org.wso2.carbon.idp.mgt.IdentityProviderManagementException;
import org.wso2.carbon.idp.mgt.IdentityProviderManagementServerException;
import org.wso2.carbon.idp.mgt.model.ConnectedAppsResult;

import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.sql.DataSource;

import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyInt;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;
import static org.wso2.carbon.idp.mgt.util.IdPManagementConstants.RESET_PROVISIONING_ENTITIES_ON_CONFIG_UPDATE;


/**
 * Unit test cases for IdPManagementDAO.
 */
@PrepareForTest({IdentityDatabaseUtil.class, DataSource.class, IdentityTenantUtil.class, IdentityUtil.class})
public class IdPManagementDAOTest extends PowerMockTestCase {

    private static Map<String, BasicDataSource> dataSourceMap = new HashMap<>();
    private static final String DB_NAME = "test";
    private static final Integer SAMPLE_TENANT_ID = -1234;
    private static final Integer SAMPLE_TENANT_ID2 = 1;
    private static final String TENANT_DOMAIN = "carbon.super";

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

    public static void closeH2Database() throws Exception {

        BasicDataSource dataSource = dataSourceMap.get(DB_NAME);
        if (dataSource != null) {
            dataSource.close();
        }
    }

    private static String getFilePath(String fileName) {

        if (StringUtils.isNotBlank(fileName)) {
            return Paths.get(System.getProperty("user.dir"), "src", "test", "resources", "dbscripts", fileName)
                    .toString();
        }
        throw new IllegalArgumentException("DB Script file name cannot be empty.");
    }

    private static Connection getConnection(String database) throws SQLException {

        if (dataSourceMap.get(database) != null) {
            return dataSourceMap.get(database).getConnection();
        }
        throw new RuntimeException("No datasource initiated for database: " + database);
    }


    @BeforeMethod
    public void setup () throws Exception {
        initiateH2Database(DB_NAME, getFilePath("h2.sql"));
    }

    @AfterMethod
    public void tearDown() throws Exception {
        closeH2Database();
    }



    @DataProvider
    public Object[][] testGetIdPsData() {

        return new Object[][]{
                {SAMPLE_TENANT_ID, TENANT_DOMAIN, 2} ,
                {SAMPLE_TENANT_ID2, TENANT_DOMAIN, 1},
                {4, "", 0},
        };
    }

    @Test(dataProvider = "testGetIdPsData")
    public void testGetIdPs(int tenantId,  String tenantDomain, int resultCount) throws Exception {

        IdPManagementDAO idPManagementDAO = new IdPManagementDAO();
        mockStatic(IdentityDatabaseUtil.class);
        mockStatic(IdentityTenantUtil.class);

        try (Connection connection = getConnection(DB_NAME)) {
            when(IdentityTenantUtil.getTenantDomain(anyInt())).thenReturn(TENANT_DOMAIN);
            when(IdentityDatabaseUtil.getDBConnection(anyBoolean())).thenReturn(connection);
            addTestIdps(idPManagementDAO);

            List<IdentityProvider> idps1 = idPManagementDAO.getIdPs(connection, tenantId, tenantDomain);
            assertEquals(idps1.size(), resultCount);
            List<IdentityProvider> idps2 = idPManagementDAO.getIdPs(null, tenantId, tenantDomain);
            assertEquals(idps2.size(), resultCount);
        }

        try {
            List<IdentityProvider> idps = idPManagementDAO.getIdPs(null, tenantId, tenantDomain);
        } catch (IdentityProviderManagementException e) {
            assertEquals(e.getMessage(), "Error occurred while retrieving registered Identity " +
                    "Provider Entity IDs for tenant " + tenantDomain);
        }
    }


    @DataProvider
    public Object[][] testGetIdPsSearchData() {

        return new Object[][]{
                {SAMPLE_TENANT_ID, TENANT_DOMAIN, "testIdP1", 1},
                {SAMPLE_TENANT_ID2, TENANT_DOMAIN, "testIdP3", 1},
                {SAMPLE_TENANT_ID, TENANT_DOMAIN, "", 2},
        };
    }

    @Test(dataProvider = "testGetIdPsSearchData")
    public void testGetIdPsSearch(int tenantId,  String tenantDomain, String filter, int resultCount) throws Exception {

        mockStatic(IdentityDatabaseUtil.class);
        mockStatic(IdentityTenantUtil.class);
        IdPManagementDAO idPManagementDAO = new IdPManagementDAO();

        try (Connection connection = getConnection(DB_NAME)) {
            when(IdentityTenantUtil.getTenantDomain(anyInt())).thenReturn(TENANT_DOMAIN);
            when(IdentityDatabaseUtil.getDBConnection(anyBoolean())).thenReturn(connection);
            when(IdentityDatabaseUtil.getDBConnection()).thenReturn(connection);
            addTestIdps(idPManagementDAO);

            List<IdentityProvider> idps1 = idPManagementDAO.getIdPsSearch(connection, tenantId, tenantDomain, filter);
            assertEquals(idps1.size(), resultCount);
            List<IdentityProvider> idps2 = idPManagementDAO.getIdPsSearch(null, tenantId, tenantDomain, filter);
            assertEquals(idps2.size(), resultCount);
        }

        try {
            List<IdentityProvider> idps = idPManagementDAO.getIdPsSearch(null, tenantId, tenantDomain, filter);
        } catch (IdentityProviderManagementException e) {
            assertEquals(e.getMessage(), "Error occurred while retrieving registered Identity " +
                "Provider Entity IDs for tenant " + tenantDomain);
        }
    }


    @DataProvider
    public Object[][] testGetIdPsSearch1Data() {
        ExpressionNode en1 = new ExpressionNode();
        en1.setAttributeValue("name");
        en1.setOperation("co");
        en1.setValue("IdP");
        List<ExpressionNode> expressionNodes1 = new ArrayList<>();
        expressionNodes1.add(en1);

        ExpressionNode en2 = new ExpressionNode();
        List<ExpressionNode> expressionNodes2 = new ArrayList<>();
        en2.setAttributeValue("name");
        en2.setOperation("eq");
        en2.setValue("testIdP1");
        expressionNodes2.add(en2);

        ExpressionNode en3 = new ExpressionNode();
        List<ExpressionNode> expressionNodes3 = new ArrayList<>();
        en3.setAttributeValue("name");
        en3.setOperation("ew");
        en3.setValue("2");
        expressionNodes3.add(en3);

        return new Object[][]{
                {SAMPLE_TENANT_ID, expressionNodes1, 2, 0, "ASC", "NAME", 2, "testIdP1"},
                {SAMPLE_TENANT_ID, expressionNodes1, 2, 0, "DESC", "NAME", 2, "testIdP2"},
                {SAMPLE_TENANT_ID2, expressionNodes1, 1, 1, "ASC", "NAME", 0, "" },
                {SAMPLE_TENANT_ID, expressionNodes2, 1, 0, "ASC", "NAME", 1, "testIdP1"},
                {SAMPLE_TENANT_ID, expressionNodes3, 1, 0, "ASC", "NAME", 1, "testIdP2"},
        };
    }

    @Test(dataProvider = "testGetIdPsSearch1Data")
    public void testGetIdPsSearch1(int tenantId, List<ExpressionNode> expressionNodes,  int limit, int offset,
                                   String sortOrder, String sortBy, int count, String firstIdp) throws Exception {

        mockStatic(IdentityDatabaseUtil.class);
        mockStatic(IdentityTenantUtil.class);
        IdPManagementDAO idPManagementDAO = new IdPManagementDAO();

        try (Connection connection = getConnection(DB_NAME)) {
            when(IdentityTenantUtil.getTenantDomain(anyInt())).thenReturn(TENANT_DOMAIN);
            when(IdentityDatabaseUtil.getDBConnection(anyBoolean())).thenReturn(connection);
            when(IdentityDatabaseUtil.getDBConnection()).thenReturn(connection);
            addTestIdps(idPManagementDAO);

            List<IdentityProvider> idps1 = idPManagementDAO.getIdPsSearch(tenantId, expressionNodes, limit, offset,
                    sortOrder, sortBy);
            assertEquals(idps1.size(), count);
            if (count > 0) {
                assertEquals(idps1.get(0).getIdentityProviderName(), firstIdp);
            }
        }
    }


    @DataProvider
    public Object[][] testGetIdPsSearch2Data() {
        ExpressionNode en = new ExpressionNode();
        List<ExpressionNode> expressionNodes = new ArrayList<>();
        expressionNodes.add(en);

        List<String> attributes1 = Arrays.asList("id", "name", "description", "isEnabled", "image", "isPrimary");
        List<String> attributes2 = Arrays.asList("homeRealmIdentifier", "isFederationHub", "certificate", "alias",
                "claims", "roles", "federatedAuthenticators", "provisioning");

        return new Object[][]{
                {SAMPLE_TENANT_ID, expressionNodes, 2, 0, "ASC", "NAME", attributes1, 2},
                {SAMPLE_TENANT_ID, expressionNodes, 1, 1, "ASC", "NAME", attributes2, 1},
                {SAMPLE_TENANT_ID, expressionNodes, 2, 0, "DESC", "NAME", attributes1, 2},
        };
    }


    @Test(dataProvider = "testGetIdPsSearch2Data")
    public void testGetIdPsSearch2(int tenantId, List<ExpressionNode> expressionNodes, int limit, int offset,
                                   String order, String sortBy, List<String> attributes, int count) throws Exception {

        mockStatic(IdentityDatabaseUtil.class);
        mockStatic(IdentityTenantUtil.class);
        IdPManagementDAO idPManagementDAO = new IdPManagementDAO();

        try (Connection connection = getConnection(DB_NAME)) {
            when(IdentityTenantUtil.getTenantDomain(anyInt())).thenReturn(TENANT_DOMAIN);
            when(IdentityDatabaseUtil.getDBConnection(anyBoolean())).thenReturn(connection);
            when(IdentityDatabaseUtil.getDBConnection()).thenReturn(connection);
            addTestIdps(idPManagementDAO);

            List<IdentityProvider> idps1 = idPManagementDAO.getIdPsSearch(tenantId, expressionNodes, limit, offset,
                    order, sortBy, attributes);
            assertEquals(idps1.size(), count);
        }

        try {
            List<IdentityProvider> idps1 = idPManagementDAO.getIdPsSearch(tenantId, expressionNodes, limit, offset,
                    order, sortBy, attributes);
        } catch (IdentityProviderManagementServerException e) {
            assertEquals(e.getErrorCode(), "IDP-65006");
        }
    }


    @DataProvider
    public Object[][] testGetCountOfFilteredIdPsData() {
        ExpressionNode en1 = new ExpressionNode();
        List<ExpressionNode> expressionNodes1 = new ArrayList<>();
        expressionNodes1.add(en1);
        ExpressionNode en2 = new ExpressionNode();
        en2.setAttributeValue("name");
        en2.setOperation("sw");
        en2.setValue("test");
        List<ExpressionNode> expressionNodes2 = new ArrayList<>();
        expressionNodes2.add(en2);

        return new Object[][]{
                {SAMPLE_TENANT_ID, expressionNodes1, 2},
                {SAMPLE_TENANT_ID, expressionNodes2, 2},
                {SAMPLE_TENANT_ID2, expressionNodes1, 1},
        };
    }

    @Test(dataProvider = "testGetCountOfFilteredIdPsData")
    public void testGetCountOfFilteredIdPs(int tenantId, List<ExpressionNode> expNodes, int count) throws Exception {

        mockStatic(IdentityDatabaseUtil.class);
        mockStatic(IdentityTenantUtil.class);
        IdPManagementDAO idPManagementDAO = new IdPManagementDAO();

        try (Connection connection = getConnection(DB_NAME)) {
            when(IdentityTenantUtil.getTenantDomain(anyInt())).thenReturn(TENANT_DOMAIN);
            when(IdentityDatabaseUtil.getDBConnection(anyBoolean())).thenReturn(connection);
            when(IdentityDatabaseUtil.getDBConnection()).thenReturn(connection);
            addTestIdps(idPManagementDAO);

            int resultCount = idPManagementDAO.getCountOfFilteredIdPs(tenantId, expNodes);
            assertEquals(resultCount, count);
        }

        try {
            int resultCount = idPManagementDAO.getCountOfFilteredIdPs(tenantId, expNodes);
        } catch (IdentityProviderManagementServerException e) {
            assertEquals(e.getErrorCode(), "IDP-65006");
        }

    }


    @DataProvider
    public Object[][] testAddIdPData() {

        //Initialize Test Identity Provider 1
        IdentityProvider idp1 = new IdentityProvider();
        idp1.setIdentityProviderName("testIdP1");
        idp1.setEnable(true);
        idp1.setPrimary(true);
        idp1.setFederationHub(true);
        idp1.setCertificate("");

        RoleMapping rm1 = new RoleMapping();
        rm1.setRemoteRole("Role1");
        rm1.setLocalRole(new LocalRole("1", "LocalRole1"));
        RoleMapping rm2 = new RoleMapping();
        rm2.setRemoteRole("Role2");
        rm2.setLocalRole(new LocalRole("2", "LocalRole2"));

        PermissionsAndRoleConfig prc = new PermissionsAndRoleConfig();
        prc.setIdpRoles(new String[]{"Role1", "Role2"});
        prc.setRoleMappings(new RoleMapping[]{rm1, rm2});
        idp1.setPermissionAndRoleConfig(prc);

        FederatedAuthenticatorConfig fac = new FederatedAuthenticatorConfig();
        fac.setDisplayName("DisplayName1");
        fac.setName("Name");
        fac.setEnabled(true);
        Property property1 = new Property();
        property1.setName("Property1");
        property1.setValue("value1");
        property1.setConfidential(false);
        Property property2 = new Property();
        property2.setName("Property2");
        property2.setValue("value2");
        property2.setConfidential(true);
        fac.setProperties(new Property[] {property1, property2});
        idp1.setFederatedAuthenticatorConfigs(new FederatedAuthenticatorConfig[] {fac});

        ProvisioningConnectorConfig pcc1 = new ProvisioningConnectorConfig();
        pcc1.setName("ProvisiningConfig1");
        pcc1.setProvisioningProperties(new Property[] {property1});
        ProvisioningConnectorConfig pcc2 = new ProvisioningConnectorConfig();
        pcc2.setName("ProvisiningConfig2");
        pcc2.setProvisioningProperties(new Property[] {property2});
        pcc2.setEnabled(true);
        pcc2.setBlocking(true);
        idp1.setProvisioningConnectorConfigs(new ProvisioningConnectorConfig[] {pcc1, pcc2});

        ClaimConfig claimConfig = new ClaimConfig();
        claimConfig.setLocalClaimDialect(false);
        claimConfig.setRoleClaimURI("Country");
        claimConfig.setUserClaimURI("Country");
        ClaimMapping cm = new ClaimMapping();
        Claim localClaim = new Claim();
        localClaim.setClaimId(0);
        localClaim.setClaimUri("http://wso2.org/claims/country");
        Claim remoteClaim = new Claim();
        remoteClaim.setClaimId(0);
        remoteClaim.setClaimUri("Country");
        cm.setLocalClaim(localClaim);
        cm.setRemoteClaim(remoteClaim);
        claimConfig.setClaimMappings(new ClaimMapping[]{cm});
        claimConfig.setIdpClaims(new Claim[]{remoteClaim});
        idp1.setClaimConfig(claimConfig);


        //Initialize Test Identity Provider 2
        IdentityProvider idp2 = new IdentityProvider();
        idp2.setIdentityProviderName("testIdP2");

        ClaimConfig claimConfig2 = new ClaimConfig();
        claimConfig2.setLocalClaimDialect(true);
        claimConfig2.setRoleClaimURI("http://wso2.org/claims/role");
        claimConfig2.setUserClaimURI("http://wso2.org/claims/fullname");
        ClaimMapping cm2 = new ClaimMapping();
        Claim localClaim2 = new Claim();
        localClaim2.setClaimId(0);
        localClaim2.setClaimUri("http://wso2.org/claims/fullname");
        cm2.setLocalClaim(localClaim2);
        claimConfig2.setClaimMappings(new ClaimMapping[]{cm2});
        idp2.setClaimConfig(claimConfig2);

        //Initialize Test Identity Provider 3
        IdentityProvider idp3 = new IdentityProvider();
        idp3.setIdentityProviderName("testIdP3");

        return new Object[][]{
                {idp1, SAMPLE_TENANT_ID},
                {idp2, SAMPLE_TENANT_ID},
                {idp3, SAMPLE_TENANT_ID2},
        };
    }

    @Test(dataProvider = "testAddIdPData")
    public void testAddIdP(Object identityProvider, int tenantId) throws Exception {


        mockStatic(IdentityDatabaseUtil.class);
        mockStatic(IdentityTenantUtil.class);
        IdPManagementDAO idPManagementDAO = new IdPManagementDAO();


        try (Connection connection = getConnection(DB_NAME)) {
            when(IdentityDatabaseUtil.getDBConnection(anyBoolean())).thenReturn(connection);
            when(IdentityTenantUtil.getTenantDomain(anyInt())).thenReturn(TENANT_DOMAIN);
            idPManagementDAO.addIdP(((IdentityProvider) identityProvider), tenantId);

            String query = "SELECT * FROM IDP WHERE NAME=? AND TENANT_ID=?";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setString(1, ((IdentityProvider) identityProvider).getIdentityProviderName());
            statement.setInt(2, tenantId);
            ResultSet resultSet = statement.executeQuery();
            String resultName = "";
            int resultID = 0;
            if (resultSet.next()) {
                resultID = resultSet.getInt("TENANT_ID");
                resultName = resultSet.getString("NAME");
            }
            assertEquals(resultID, tenantId);
            assertEquals(resultName, ((IdentityProvider) identityProvider).getIdentityProviderName());

            try {
                idPManagementDAO.addIdP(((IdentityProvider) identityProvider), tenantId);
            } catch (IdentityProviderManagementException e) {
                assertEquals(e.getMessage(), "Error occurred while adding Identity Provider for tenant "
                        + tenantId);
            }
        }
    }


    @DataProvider
    public Object[][] testGetPermissionsAndRoleConfigurationData()  {

        return new Object[][]{
                {"testIdP1", 1, SAMPLE_TENANT_ID, 2},
        };
    }


    @Test(dataProvider = "testGetPermissionsAndRoleConfigurationData")
    public void testGetPermissionsAndRoleConfiguration(String idpName, int idpId, int tenantId, int resultCount)
            throws Exception {

        mockStatic(IdentityDatabaseUtil.class);
        mockStatic(IdentityTenantUtil.class);
        IdPManagementDAO idPManagementDAO = new IdPManagementDAO();

        try (Connection connection = getConnection(DB_NAME)) {
            when(IdentityTenantUtil.getTenantDomain(anyInt())).thenReturn(TENANT_DOMAIN);
            when(IdentityDatabaseUtil.getDBConnection(anyBoolean())).thenReturn(connection);
            when(IdentityDatabaseUtil.getDBConnection()).thenReturn(connection);
            addTestIdps(idPManagementDAO);

            PermissionsAndRoleConfig pac = idPManagementDAO.getPermissionsAndRoleConfiguration(connection, idpName,
                    idpId, tenantId);
            assertEquals(pac.getIdpRoles().length, resultCount);
        }
    }


    @DataProvider
    public Object[][] testGetProvisioningConnectorConfigsData()  {

        return new Object[][]{
                {"testIdP1", 1, SAMPLE_TENANT_ID, 2},
        };
    }


    @Test(dataProvider = "testGetProvisioningConnectorConfigsData")
    public void testGetProvisioningConnectorConfigs (String idpName, int idpId, int tenantId, int resultCount)
            throws Exception {

        mockStatic(IdentityDatabaseUtil.class);
        mockStatic(IdentityTenantUtil.class);
        IdPManagementDAO idPManagementDAO = new IdPManagementDAO();

        try (Connection connection = getConnection(DB_NAME)) {
            when(IdentityTenantUtil.getTenantDomain(anyInt())).thenReturn(TENANT_DOMAIN);
            when(IdentityDatabaseUtil.getDBConnection(anyBoolean())).thenReturn(connection);
            when(IdentityDatabaseUtil.getDBConnection()).thenReturn(connection);
            addTestIdps(idPManagementDAO);

            ProvisioningConnectorConfig[] pccResult = idPManagementDAO.getProvisioningConnectorConfigs(connection,
                    idpName, idpId, tenantId);
            assertEquals(pccResult.length, resultCount);
        }
    }


    @DataProvider
    public Object[][] testgetIdPByData()  {

        return new Object[][]{
                {"testIdP1", 1, SAMPLE_TENANT_ID, true},
                {"testIdP2", 2, SAMPLE_TENANT_ID, true},
                {"testIdP3", 3, SAMPLE_TENANT_ID2, true},
                {"notExist", 4, SAMPLE_TENANT_ID2, false},
        };
    }

    @Test(dataProvider = "testgetIdPByData")
    public void testgetIdPBy(String idpName, int idpId,  int tenantId, boolean isExist) throws Exception {

        mockStatic(IdentityDatabaseUtil.class);
        mockStatic(IdentityTenantUtil.class);
        IdPManagementDAO idPManagementDAO = new IdPManagementDAO();

        try (Connection connection = getConnection(DB_NAME)) {
            when(IdentityTenantUtil.getTenantDomain(anyInt())).thenReturn(TENANT_DOMAIN);
            when(IdentityDatabaseUtil.getDBConnection(anyBoolean())).thenReturn(connection);
            when(IdentityDatabaseUtil.getDBConnection()).thenReturn(connection);
            addTestIdps(idPManagementDAO);

            //getIdPByName
            IdentityProvider idpResult1 = idPManagementDAO.getIdPByName(connection, idpName, tenantId, TENANT_DOMAIN);
            if (isExist) {
                assertEquals(idpResult1.getIdentityProviderName(), idpName, "'getIdPByName' method fails");
            } else {
                assertNull(idpResult1, "'getIdPByName' method fails");
            }

            //getIDPbyId
            IdentityProvider idpResult2 = idPManagementDAO.getIDPbyId(connection, idpId, tenantId, TENANT_DOMAIN);
            if (isExist) {
                assertEquals(idpResult2.getIdentityProviderName(), idpName, "'getIDPbyId' method fails");
            } else {
                assertNull(idpResult2, "'getIDPbyId' method fails");
            }

            //getIDPbyResourceId
            String uuid = "";
            if (isExist) {
                uuid = idpResult1.getResourceId();
            }
            IdentityProvider idpResult3 = idPManagementDAO.getIDPbyResourceId(connection, uuid, tenantId,
                    TENANT_DOMAIN);
            if (isExist) {
                assertEquals(idpResult3.getIdentityProviderName(), idpName, "'getIDPbyResourceId' method fails");
            } else {
                assertNull(idpResult3, "'getIDPbyResourceId' method fails");
            }

            //getIDPbyRealmId
            String realmId;
            if (isExist) {
                realmId = idpResult1.getHomeRealmId();
            } else {
                realmId = "";
            }
            IdentityProvider idpResult4 = idPManagementDAO.getIdPByRealmId(realmId,  tenantId, TENANT_DOMAIN);
            if (isExist) {
                assertEquals(idpResult4.getIdentityProviderName(), idpName, "'getIDPbyRealmId' method fails");
            } else {
                assertNull(idpResult4, "'getIDPbyRealmId' method fails");
            }
        }
    }



    @Test(dataProvider = "testgetIdPByData")
    public void testIDPNameByResourceId(String idpName, int idpId, int tenantId, boolean isExist) throws Exception {

        mockStatic(IdentityDatabaseUtil.class);
        mockStatic(IdentityTenantUtil.class);
        IdPManagementDAO idPManagementDAO = new IdPManagementDAO();

        try (Connection connection = getConnection(DB_NAME)) {
            when(IdentityTenantUtil.getTenantDomain(anyInt())).thenReturn(TENANT_DOMAIN);
            when(IdentityDatabaseUtil.getDBConnection(anyBoolean())).thenReturn(connection);
            when(IdentityDatabaseUtil.getDBConnection()).thenReturn(connection);
            addTestIdps(idPManagementDAO);

            //getIdPByName
            IdentityProvider idpResult = idPManagementDAO.getIdPByName(connection, idpName, tenantId, TENANT_DOMAIN);
            String uuid = "";
            if (isExist) {
                uuid = idpResult.getResourceId();
            }

            String outputName = idPManagementDAO.getIDPNameByResourceId(uuid);
            if (isExist) {
                assertEquals(outputName, idpName);
            } else {
                assertNull(outputName);
            }
        }
    }


    @DataProvider
    public Object[][] testGetIdPNameByMetadataPropertyData()  {

        return new Object[][]{
                {"testIdP1", "idpPropertyName", "idpPropertyValue", SAMPLE_TENANT_ID},
                {null, "notExist", "4", SAMPLE_TENANT_ID2},
        };
    }

    @Test(dataProvider = "testGetIdPNameByMetadataPropertyData")
    public void testGetIdPNameByMetadataProperty(String idpName, String property, String value, int tenantId)
            throws Exception {

        mockStatic(IdentityDatabaseUtil.class);
        mockStatic(IdentityTenantUtil.class);
        IdPManagementDAO idPManagementDAO = new IdPManagementDAO();

        try (Connection connection = getConnection(DB_NAME)) {
            when(IdentityTenantUtil.getTenantDomain(anyInt())).thenReturn(TENANT_DOMAIN);
            when(IdentityDatabaseUtil.getDBConnection(anyBoolean())).thenReturn(connection);
            when(IdentityDatabaseUtil.getDBConnection()).thenReturn(connection);
            addTestIdps(idPManagementDAO);

            String outputName = idPManagementDAO.getIdPNameByMetadataProperty(connection, property, value, tenantId);
            assertEquals(outputName, idpName);
            String outputName1 = idPManagementDAO.getIdPNameByMetadataProperty(null, property, value, tenantId);
            assertEquals(outputName1, idpName);
        }
    }


    @DataProvider
    public Object[][] testgetIdPByAuthConfData()  {

        return new Object[][]{
                {SAMPLE_TENANT_ID, "testIdP1", "Property1", "value1", "Name" },
                {SAMPLE_TENANT_ID, "testIdP1", "Property2", "value2", "Name" },
        };
    }

    @Test(dataProvider = "testgetIdPByAuthConfData")
    public void testgetIdPByAuthConf(int tenantId, String idpName,  String property, String value, String authenticator)
            throws Exception {

        mockStatic(IdentityDatabaseUtil.class);
        mockStatic(IdentityTenantUtil.class);
        IdPManagementDAO idPManagementDAO = new IdPManagementDAO();

        try (Connection connection = getConnection(DB_NAME)) {
            when(IdentityTenantUtil.getTenantDomain(anyInt())).thenReturn(TENANT_DOMAIN);
            when(IdentityDatabaseUtil.getDBConnection(anyBoolean())).thenReturn(connection);
            when(IdentityDatabaseUtil.getDBConnection()).thenReturn(connection);
            addTestIdps(idPManagementDAO);

            IdentityProvider idpResult1 = idPManagementDAO.getIdPByAuthenticatorPropertyValue (connection, property,
                    value, authenticator, tenantId, TENANT_DOMAIN);
            assertEquals(idpResult1.getIdentityProviderName(), idpName);

            IdentityProvider idpResult2 = idPManagementDAO.getIdPByAuthenticatorPropertyValue (connection, property,
                    value, tenantId, TENANT_DOMAIN);
            assertEquals(idpResult2.getIdentityProviderName(), idpName);

        }
    }


    @DataProvider
    public Object[][] testUpdateIdPData()  {

        //Initialize Test Identity Provider 1
        IdentityProvider idp1 = new IdentityProvider();
        idp1.setIdentityProviderName("testIdP1");
        idp1.setHomeRealmId("1");
        idp1.setEnable(true);
        idp1.setPrimary(true);
        idp1.setFederationHub(true);
        idp1.setCertificate("");

        FederatedAuthenticatorConfig fac = new FederatedAuthenticatorConfig();
        fac.setDisplayName("DisplayName1");
        fac.setName("Name");
        fac.setEnabled(true);
        Property property1 = new Property();
        property1.setName("Property1");
        property1.setValue("value1");
        property1.setConfidential(true);
        fac.setProperties(new Property[] {property1});
        idp1.setFederatedAuthenticatorConfigs(new FederatedAuthenticatorConfig[] {fac});

        ProvisioningConnectorConfig pcc1 = new ProvisioningConnectorConfig();
        pcc1.setName("ProvisiningConfig1");
        pcc1.setProvisioningProperties(new Property[] {property1});
        idp1.setProvisioningConnectorConfigs(new ProvisioningConnectorConfig[] { pcc1 });


        //Initialize Test Identity Provider 2
        IdentityProvider idp2 = new IdentityProvider();
        idp2.setIdentityProviderName("testIdP2");

        //Initialize Test Identity Provider 3
        IdentityProvider idp3 = new IdentityProvider();
        idp3.setIdentityProviderName("testIdP3");

        //INITIALIZE NEW IDPS

        //Initialize New Test Identity Provider 1
        IdentityProvider idp1New = new IdentityProvider();
        idp1New.setIdentityProviderName("testIdP1New");
        idp1New.setEnable(true);
        idp1New.setPrimary(true);
        idp1New.setFederationHub(true);
        idp1New.setCertificate("");

        RoleMapping rm1New = new RoleMapping();
        rm1New.setRemoteRole("Role1New");
        rm1New.setLocalRole(new LocalRole("1", "LocalRole1"));
        RoleMapping rm2New = new RoleMapping();
        rm2New.setRemoteRole("Role2New");
        rm2New.setLocalRole(new LocalRole("2", "LocalRole2"));

        PermissionsAndRoleConfig prcNew = new PermissionsAndRoleConfig();
        prcNew.setIdpRoles(new String[] {"Role1New", "Role2New"});
        prcNew.setRoleMappings(new RoleMapping[] {rm1New, rm2New});
        idp1New.setPermissionAndRoleConfig(prcNew);

        FederatedAuthenticatorConfig facNew = new FederatedAuthenticatorConfig();
        facNew.setDisplayName("DisplayName1New");
        facNew.setName("Name");
        facNew.setEnabled(true);
        Property property1New = new Property();
        property1New.setName("Property1New");
        property1New.setValue("value1New");
        property1New.setConfidential(false);
        Property property2New = new Property();
        property2New.setName("Property2New");
        property2New.setValue("value2New");
        property2New.setConfidential(false);
        facNew.setProperties(new Property[] {property1New, property2New});
        idp1New.setFederatedAuthenticatorConfigs(new FederatedAuthenticatorConfig[] {facNew});

        ProvisioningConnectorConfig pcc1New = new ProvisioningConnectorConfig();
        pcc1New.setName("ProvisiningConfig1");
        pcc1New.setProvisioningProperties(new Property[] {property1New});
        ProvisioningConnectorConfig pcc2New = new ProvisioningConnectorConfig();
        pcc2New.setName("ProvisiningConfig2");
        pcc2New.setProvisioningProperties(new Property[] {property2New});
        pcc2New.setEnabled(true);
        pcc2New.setBlocking(true);
        idp1New.setProvisioningConnectorConfigs(new ProvisioningConnectorConfig[] {pcc1New, pcc2New});

        ClaimConfig claimConfigNew = new ClaimConfig();
        claimConfigNew.setLocalClaimDialect(false);
        claimConfigNew.setRoleClaimURI("Country");
        claimConfigNew.setUserClaimURI("Country");
        ClaimMapping cm = new ClaimMapping();
        Claim localClaim = new Claim();
        localClaim.setClaimId(0);
        localClaim.setClaimUri("http://wso2.org/claims/country");
        Claim remoteClaim = new Claim();
        remoteClaim.setClaimId(0);
        remoteClaim.setClaimUri("Country");
        cm.setLocalClaim(localClaim);
        cm.setRemoteClaim(remoteClaim);
        claimConfigNew.setClaimMappings(new ClaimMapping[]{cm});
        claimConfigNew.setIdpClaims(new Claim[]{remoteClaim});
        idp1New.setClaimConfig(claimConfigNew);

        //Initialize New Test Identity Provider 2
        IdentityProvider idp2New = new IdentityProvider();
        idp2New.setIdentityProviderName("testIdP2New");

        //Initialize New Test Identity Provider 3
        IdentityProvider idp3New = new IdentityProvider();
        idp3New.setIdentityProviderName("testIdP3New");

        return new Object[][]{
                {idp1, idp1New, SAMPLE_TENANT_ID},
                {idp2, idp2New, SAMPLE_TENANT_ID},
                {idp3, idp3New, SAMPLE_TENANT_ID2},
        };
    }

    @Test(dataProvider = "testUpdateIdPData")
    public void testUpdateIdP(Object oldIdp, Object newIdp, int tenantId) throws Exception {


        mockStatic(IdentityDatabaseUtil.class);
        mockStatic(IdentityTenantUtil.class);
        mockStatic(IdentityUtil.class);
        IdPManagementDAO idPManagementDAO = new IdPManagementDAO();

        try (Connection connection = getConnection(DB_NAME)) {
            when(IdentityDatabaseUtil.getDBConnection(anyBoolean())).thenReturn(connection);
            when(IdentityDatabaseUtil.getDBConnection()).thenReturn(connection);
            when(IdentityTenantUtil.getTenantDomain(anyInt())).thenReturn(TENANT_DOMAIN);
            when(IdentityUtil.getProperty(RESET_PROVISIONING_ENTITIES_ON_CONFIG_UPDATE)).thenReturn("false");
            addTestIdps(idPManagementDAO);
            idPManagementDAO.updateIdP((IdentityProvider) newIdp, ((IdentityProvider) oldIdp), tenantId);

            IdentityProvider idpResult1 = idPManagementDAO.getIdPByName(connection,
                    ((IdentityProvider) newIdp).getIdentityProviderName(), tenantId, TENANT_DOMAIN);
            assertEquals(idpResult1.getIdentityProviderName(), ((IdentityProvider) newIdp).getIdentityProviderName());
        }
    }


    @Test(dataProvider = "testAddIdPData")
    public void testDeleteIdP(Object identityProvider, int tenantId) throws Exception {

        mockStatic(IdentityDatabaseUtil.class);
        mockStatic(IdentityTenantUtil.class);
        IdPManagementDAO idPManagementDAO = new IdPManagementDAO();

        try (Connection connection = getConnection(DB_NAME)) {
            when(IdentityDatabaseUtil.getDBConnection(anyBoolean())).thenReturn(connection);
            when(IdentityDatabaseUtil.getDBConnection()).thenReturn(connection);
            when(IdentityTenantUtil.getTenantDomain(anyInt())).thenReturn(TENANT_DOMAIN);

            //delete IdP
            idPManagementDAO.addIdP(((IdentityProvider) identityProvider), tenantId);
            String idpName = ((IdentityProvider) identityProvider).getIdentityProviderName();
            idPManagementDAO.deleteIdP(idpName, tenantId, TENANT_DOMAIN);

            int resultSize = getIdPCount(connection, idpName, tenantId);
            assertEquals(resultSize, 0, "'deleteIdP' method fails");

            try {
                idPManagementDAO.deleteIdP(idpName, tenantId, TENANT_DOMAIN);
            } catch (IdentityProviderManagementException e) {
                assertEquals(e.getMessage(), String.format("Trying to delete non-existent Identity Provider: %s in " +
                        "tenantDomain: %s", idpName, TENANT_DOMAIN), "'deleteIdP' method fails");
            }


            //delete IdPByResourceID
            idPManagementDAO.addIdP(((IdentityProvider) identityProvider), tenantId);
            idpName = ((IdentityProvider) identityProvider).getIdentityProviderName();
            String uuid = idPManagementDAO.getIdPByName(connection, idpName, tenantId, TENANT_DOMAIN).getResourceId();
            idPManagementDAO.deleteIdPByResourceId(uuid, tenantId, TENANT_DOMAIN);

            resultSize = getIdPCount(connection, idpName, tenantId);
            assertEquals(resultSize, 0, "'deleteIdPByResourceId' method fails");

            try {
                idPManagementDAO.deleteIdPByResourceId(uuid, tenantId, TENANT_DOMAIN);
            } catch (IdentityProviderManagementException e) {
                assertEquals(e.getMessage(), String.format("Trying to delete non-existent Identity Provider with " +
                        "resource ID: %s in tenantDomain: %s", uuid, TENANT_DOMAIN),
                        "'deleteIdPByResourceId' method fails");
            }

        }
    }


    @Test(dataProvider = "testAddIdPData")
    public void testForceDeleteIdP(Object identityProvider, int tenantId) throws Exception {

        mockStatic(IdentityDatabaseUtil.class);
        mockStatic(IdentityTenantUtil.class);
        IdPManagementDAO idPManagementDAO = new IdPManagementDAO();

        try (Connection connection = getConnection(DB_NAME)) {
            when(IdentityDatabaseUtil.getDBConnection(anyBoolean())).thenReturn(connection);
            when(IdentityDatabaseUtil.getDBConnection()).thenReturn(connection);
            when(IdentityTenantUtil.getTenantDomain(anyInt())).thenReturn(TENANT_DOMAIN);


            //forceDeleteIdP
            idPManagementDAO.addIdP(((IdentityProvider) identityProvider), tenantId);
            String idpName = ((IdentityProvider) identityProvider).getIdentityProviderName();
            idPManagementDAO.forceDeleteIdP(idpName , tenantId, TENANT_DOMAIN);

            int resultSize = getIdPCount(connection, idpName, tenantId);
            assertEquals(resultSize, 0, "'forceDeleteIdP' method fails");

            try {
                idPManagementDAO.forceDeleteIdP(idpName , tenantId, TENANT_DOMAIN);
            } catch (IdentityProviderManagementException e) {
                assertEquals(e.getMessage(), String.format("Trying to force delete non-existent Identity Provider: " +
                        "%s in tenantDomain: %s", idpName, TENANT_DOMAIN), "'forceDeleteIdP' method fails");
            }


            //forceDeleteIdPByResourceId
            idPManagementDAO.addIdP(((IdentityProvider) identityProvider), tenantId);
            idpName = ((IdentityProvider) identityProvider).getIdentityProviderName();
            String uuid = idPManagementDAO.getIdPByName(connection, idpName, tenantId, TENANT_DOMAIN).getResourceId();
            idPManagementDAO.forceDeleteIdPByResourceId(uuid, tenantId, TENANT_DOMAIN);

            resultSize = getIdPCount(connection, idpName, tenantId);
            assertEquals(resultSize, 0, "'forceDeleteIdPByResourceId' method fails");

            try {
                idPManagementDAO.forceDeleteIdPByResourceId(uuid, tenantId, TENANT_DOMAIN);
            } catch (IdentityProviderManagementException e) {
                assertEquals(e.getMessage(), String.format("Trying to force delete non-existent Identity Provider " +
                        "with resource ID: %s in tenantDomain: %s", uuid, TENANT_DOMAIN),
                        "'forceDeleteIdPByResourceId' method fails");
            }

        }
    }


    @DataProvider
    public Object[][] testDeleteIdPsData()  {

        return new Object[][]{
                {SAMPLE_TENANT_ID},
                {SAMPLE_TENANT_ID2},
        };
    }

    @Test(dataProvider = "testDeleteIdPsData")
    public void testDeleteIdPs(int tenantId) throws Exception {

        mockStatic(IdentityDatabaseUtil.class);
        mockStatic(IdentityTenantUtil.class);
        IdPManagementDAO idPManagementDAO = new IdPManagementDAO();

        try (Connection connection = getConnection(DB_NAME)) {
            when(IdentityDatabaseUtil.getDBConnection(anyBoolean())).thenReturn(connection);
            when(IdentityDatabaseUtil.getDBConnection()).thenReturn(connection);
            when(IdentityTenantUtil.getTenantDomain(anyInt())).thenReturn(TENANT_DOMAIN);

            addTestIdps(idPManagementDAO);
            idPManagementDAO.deleteIdPs(tenantId);
        }

        try (Connection connection = getConnection(DB_NAME)) {
            String query = "SELECT * FROM IDP WHERE TENANT_ID=?";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setInt(1, tenantId);
            ResultSet resultSet = statement.executeQuery();
            int resultSize = 0;
            if (resultSet.next()) {
                resultSize = resultSet.getRow();
            }
            assertEquals(resultSize, 0);
        }
    }


    @DataProvider
    public Object[][] testDeleteTenantRoleData()  {

        return new Object[][]{
                {SAMPLE_TENANT_ID, "Role1"},
                {SAMPLE_TENANT_ID, "Role2"},
                {SAMPLE_TENANT_ID, "NotExistRole"},

        };
    }

    @Test(dataProvider = "testDeleteTenantRoleData")
    public void testDeleteTenantRole(int tenantId, String role) throws Exception {

        mockStatic(IdentityDatabaseUtil.class);
        mockStatic(IdentityTenantUtil.class);
        IdPManagementDAO idPManagementDAO = new IdPManagementDAO();

        try (Connection connection = getConnection(DB_NAME)) {
            when(IdentityDatabaseUtil.getDBConnection(anyBoolean())).thenReturn(connection);
            when(IdentityDatabaseUtil.getDBConnection()).thenReturn(connection);
            when(IdentityTenantUtil.getTenantDomain(anyInt())).thenReturn(TENANT_DOMAIN);

            addTestIdps(idPManagementDAO);
            idPManagementDAO.deleteTenantRole(tenantId, role, TENANT_DOMAIN);
        }

        try (Connection connection = getConnection(DB_NAME)) {
            String query = "SELECT * FROM IDP_ROLE WHERE TENANT_ID=? AND ROLE=?";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setInt(1, tenantId);
            statement.setString(2, role);
            ResultSet resultSet = statement.executeQuery();
            int resultSize = 0;
            if (resultSet.next()) {
                resultSize = resultSet.getRow();
            }
            assertEquals(resultSize, 0);
        }
    }


    @DataProvider
    public Object[][] testRenameTenantRoleData()  {

        return new Object[][]{
                {SAMPLE_TENANT_ID, "NewLocalRole1", "LocalRole1", 1},
                {SAMPLE_TENANT_ID, "NewLocalRole2", "LocalRole2", 1},
                {SAMPLE_TENANT_ID, "NewLocalRole2", "2", 0},
        };
    }

    @Test(dataProvider = "testRenameTenantRoleData")
    public void testRenameTenantRole(int tenantId, String newRole, String oldRole, int count) throws Exception {

        mockStatic(IdentityDatabaseUtil.class);
        mockStatic(IdentityTenantUtil.class);
        IdPManagementDAO idPManagementDAO = new IdPManagementDAO();

        try (Connection connection = getConnection(DB_NAME)) {
            when(IdentityDatabaseUtil.getDBConnection(anyBoolean())).thenReturn(connection);
            when(IdentityDatabaseUtil.getDBConnection()).thenReturn(connection);
            when(IdentityTenantUtil.getTenantDomain(anyInt())).thenReturn(TENANT_DOMAIN);

            addTestIdps(idPManagementDAO);
            idPManagementDAO.renameTenantRole(newRole, oldRole, tenantId, TENANT_DOMAIN);

            String query = "SELECT * FROM IDP_ROLE_MAPPING WHERE TENANT_ID=? AND LOCAL_ROLE=?";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setInt(1, tenantId);
            statement.setString(2, newRole);
            ResultSet resultSet = statement.executeQuery();
            int resultSize = 0;
            if (resultSet.next()) {
                resultSize = resultSet.getRow();
            }
            assertEquals(resultSize, count);
        }

        try {
            idPManagementDAO.renameTenantRole(newRole, oldRole, tenantId, TENANT_DOMAIN);
        } catch (IdentityProviderManagementException e) {
            assertEquals(e.getMessage(), "Error occurred while renaming tenant role " + oldRole + " to "
                    + newRole + " of tenant " + TENANT_DOMAIN);
        }
    }


    @DataProvider
    public Object[][] testRenameClaimURIData()  {

        return new Object[][]{
                {SAMPLE_TENANT_ID, "newClaimURI", "http://wso2.org/claims/country", 1},
                {4, "newClaimURI", "oldClaimURI", 0},
        };
    }

    @Test(dataProvider = "testRenameClaimURIData")
    public void testRenameClaimURI(int tenantId, String newClaimURI, String oldClaimURI, int count) throws Exception {


        mockStatic(IdentityDatabaseUtil.class);
        mockStatic(IdentityTenantUtil.class);
        IdPManagementDAO idPManagementDAO = new IdPManagementDAO();

        try (Connection connection = getConnection(DB_NAME)) {
            when(IdentityDatabaseUtil.getDBConnection(anyBoolean())).thenReturn(connection);
            when(IdentityDatabaseUtil.getDBConnection()).thenReturn(connection);
            when(IdentityTenantUtil.getTenantDomain(anyInt())).thenReturn(TENANT_DOMAIN);

            addTestIdps(idPManagementDAO);
            idPManagementDAO.renameClaimURI(newClaimURI, oldClaimURI, tenantId, TENANT_DOMAIN);

            String query = "SELECT * FROM IDP_CLAIM_MAPPING WHERE TENANT_ID=? AND LOCAL_CLAIM=?";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setInt(1, tenantId);
            statement.setString(2, newClaimURI);
            ResultSet resultSet = statement.executeQuery();
            int resultSize = 0;
            if (resultSet.next()) {
                resultSize = resultSet.getRow();
            }
            assertEquals(resultSize, count);
        }

        try {
            idPManagementDAO.renameClaimURI(newClaimURI, oldClaimURI, tenantId, TENANT_DOMAIN);
        } catch (IdentityProviderManagementException e) {
            assertEquals(e.getMessage(), "Error occurred while renaming tenant role " + oldClaimURI + " to "
                    + newClaimURI + " of tenant " + TENANT_DOMAIN);
        }
    }


    @DataProvider
    public Object[][] testIsIdPAvailableForAuthenticatorPropertyData()  {

        return new Object[][]{
                {SAMPLE_TENANT_ID, "Name", "Property1", "value1", true},
                {SAMPLE_TENANT_ID, "Address", "Property2", "value2", false},
        };
    }

    @Test(dataProvider = "testIsIdPAvailableForAuthenticatorPropertyData")
    public void testIsIdPAvailableForAuthenticatorProperty (int tenantId, String authenticator, String property,
                                                            String value, boolean isAvailable) throws Exception {

        mockStatic(IdentityDatabaseUtil.class);
        mockStatic(IdentityTenantUtil.class);
        IdPManagementDAO idPManagementDAO = new IdPManagementDAO();

        try (Connection connection = getConnection(DB_NAME)) {
            when(IdentityTenantUtil.getTenantDomain(anyInt())).thenReturn(TENANT_DOMAIN);
            when(IdentityDatabaseUtil.getDBConnection(anyBoolean())).thenReturn(connection);
            when(IdentityDatabaseUtil.getDBConnection()).thenReturn(connection);
            addTestIdps(idPManagementDAO);

            boolean availabilityResult = idPManagementDAO.isIdPAvailableForAuthenticatorProperty(authenticator,
                    property, value, tenantId);
            assertEquals(availabilityResult, isAvailable);
        }

        try {
            boolean availabilityResult = idPManagementDAO.isIdPAvailableForAuthenticatorProperty(authenticator,
                    property, value, tenantId);
        } catch (IdentityProviderManagementException e) {
            assertEquals(e.getMessage(), "Error occurred while searching for similar IdP EntityIds");
        }
    }


    @DataProvider
    public Object[][] testIsIdpReferredBySPData()  {

        return new Object[][]{
                {"testIdP1", SAMPLE_TENANT_ID, false},
                {"testIdP2", SAMPLE_TENANT_ID, false},
        };
    }

    @Test(dataProvider = "testIsIdpReferredBySPData")
    public void testIsIdpReferredBySP (String idPName, int tenantId, boolean isAvailable) throws Exception {

        mockStatic(IdentityDatabaseUtil.class);
        mockStatic(IdentityTenantUtil.class);
        IdPManagementDAO idPManagementDAO = new IdPManagementDAO();

        try (Connection connection = getConnection(DB_NAME)) {
            when(IdentityTenantUtil.getTenantDomain(anyInt())).thenReturn(TENANT_DOMAIN);
            when(IdentityDatabaseUtil.getDBConnection(anyBoolean())).thenReturn(connection);
            when(IdentityDatabaseUtil.getDBConnection()).thenReturn(connection);
            addTestIdps(idPManagementDAO);

            boolean availabilityResult = idPManagementDAO.isIdpReferredBySP(idPName, tenantId);
            assertEquals(availabilityResult, isAvailable);
        }

        try {
            boolean availabilityResult = idPManagementDAO.isIdpReferredBySP(idPName, tenantId);
        } catch (IdentityProviderManagementException e) {
            assertEquals(e.getMessage(), "Error occurred while searching for IDP references in SP ");
        }
    }


    @DataProvider
    public Object[][] testGetConnectedApplicationsData()  {

        return new Object[][]{
                {"testIdP1", SAMPLE_TENANT_ID, 2, 2, 0 },
                {"testIdP2", SAMPLE_TENANT_ID, 1, 1, 0 },
        };
    }

    @Test(dataProvider = "testGetConnectedApplicationsData")
    public void testGetConnectedApplications (String idPName, int tenantId, int limit, int offset, int count)
            throws Exception {

        mockStatic(IdentityDatabaseUtil.class);
        mockStatic(IdentityTenantUtil.class);
        IdPManagementDAO idPManagementDAO = new IdPManagementDAO();

        try (Connection connection = getConnection(DB_NAME)) {
            when(IdentityTenantUtil.getTenantDomain(anyInt())).thenReturn(TENANT_DOMAIN);
            when(IdentityDatabaseUtil.getDBConnection(anyBoolean())).thenReturn(connection);
            when(IdentityDatabaseUtil.getDBConnection()).thenReturn(connection);
            addTestIdps(idPManagementDAO);

            IdentityProvider idp = idPManagementDAO.getIdPByName(connection, idPName, tenantId, TENANT_DOMAIN);
            String uuid = idp.getResourceId();

            ConnectedAppsResult result = idPManagementDAO.getConnectedApplications(uuid, limit, offset);
            assertEquals(result.getTotalAppCount(), count);
        }
    }



    private void addTestIdps(IdPManagementDAO idPManagementDAO) throws IdentityProviderManagementException {

        //Initialize Test Identity Provider 1
        IdentityProvider idp1 = new IdentityProvider();
        idp1.setIdentityProviderName("testIdP1");
        idp1.setHomeRealmId("1");
        idp1.setEnable(true);
        idp1.setPrimary(true);
        idp1.setFederationHub(true);
        idp1.setCertificate("");

        RoleMapping rm1 = new RoleMapping();
        rm1.setRemoteRole("Role1");
        rm1.setLocalRole(new LocalRole("1", "LocalRole1"));
        RoleMapping rm2 = new RoleMapping();
        rm2.setRemoteRole("Role2");
        rm2.setLocalRole(new LocalRole("2", "LocalRole2"));

        PermissionsAndRoleConfig prc = new PermissionsAndRoleConfig();
        prc.setIdpRoles(new String[]{"Role1", "Role2"});
        prc.setRoleMappings(new RoleMapping[]{rm1, rm2});
        idp1.setPermissionAndRoleConfig(prc);

        FederatedAuthenticatorConfig fac = new FederatedAuthenticatorConfig();
        fac.setDisplayName("DisplayName1");
        fac.setName("Name");
        fac.setEnabled(true);
        Property property1 = new Property();
        property1.setName("Property1");
        property1.setValue("value1");
        property1.setConfidential(true);
        Property property2 = new Property();
        property2.setName("Property2");
        property2.setValue("value2");
        property2.setConfidential(false);
        fac.setProperties(new Property[] {property1, property2});
        idp1.setFederatedAuthenticatorConfigs(new FederatedAuthenticatorConfig[] {fac});

        ProvisioningConnectorConfig pcc1 = new ProvisioningConnectorConfig();
        pcc1.setName("ProvisiningConfig1");
        pcc1.setProvisioningProperties(new Property[] {property1});
        ProvisioningConnectorConfig pcc2 = new ProvisioningConnectorConfig();
        pcc2.setName("ProvisiningConfig2");
        pcc2.setProvisioningProperties(new Property[] {property2});
        pcc2.setEnabled(true);
        pcc2.setBlocking(true);
        idp1.setProvisioningConnectorConfigs(new ProvisioningConnectorConfig[] {pcc1, pcc2});

        IdentityProviderProperty identityProviderProperty = new IdentityProviderProperty();
        identityProviderProperty.setDisplayName("idpDisplayName");
        identityProviderProperty.setName("idpPropertyName");
        identityProviderProperty.setValue("idpPropertyValue");
        idp1.setIdpProperties(new IdentityProviderProperty[]{identityProviderProperty});

        ClaimConfig claimConfig = new ClaimConfig();
        claimConfig.setLocalClaimDialect(false);
        claimConfig.setRoleClaimURI("Country");
        claimConfig.setUserClaimURI("Country");
        ClaimMapping cm = new ClaimMapping();
        Claim localClaim = new Claim();
        localClaim.setClaimId(0);
        localClaim.setClaimUri("http://wso2.org/claims/country");
        Claim remoteClaim = new Claim();
        remoteClaim.setClaimId(0);
        remoteClaim.setClaimUri("Country");
        cm.setLocalClaim(localClaim);
        cm.setRemoteClaim(remoteClaim);
        claimConfig.setClaimMappings(new ClaimMapping[]{cm});
        claimConfig.setIdpClaims(new Claim[]{remoteClaim});
        idp1.setClaimConfig(claimConfig);


        //Initialize Test Identity Provider 2
        IdentityProvider idp2 = new IdentityProvider();
        idp2.setIdentityProviderName("testIdP2");
        idp2.setHomeRealmId("2");

        ClaimConfig claimConfig2 = new ClaimConfig();
        claimConfig2.setLocalClaimDialect(true);
        claimConfig2.setRoleClaimURI("http://wso2.org/claims/role");
        claimConfig2.setUserClaimURI("http://wso2.org/claims/fullname");
        ClaimMapping cm2 = new ClaimMapping();
        Claim localClaim2 = new Claim();
        localClaim2.setClaimId(0);
        localClaim2.setClaimUri("http://wso2.org/claims/fullname");
        cm2.setLocalClaim(localClaim2);
        claimConfig2.setClaimMappings(new ClaimMapping[]{cm2});
        idp2.setClaimConfig(claimConfig2);


        //Initialize Test Identity Provider 3
        IdentityProvider idp3 = new IdentityProvider();
        idp3.setIdentityProviderName("testIdP3");
        idp3.setHomeRealmId("3");
        idp3.setEnable(false);

        idPManagementDAO.addIdP(idp1, SAMPLE_TENANT_ID);
        idPManagementDAO.addIdP(idp2, SAMPLE_TENANT_ID);
        idPManagementDAO.addIdP(idp3, SAMPLE_TENANT_ID2);
    }


    private int getIdPCount(Connection connection, String idpName, int tenantId) throws SQLException {
        String query = "SELECT * FROM IDP WHERE NAME=? AND TENANT_ID=?";
        PreparedStatement statement = connection.prepareStatement(query);
        statement.setString(1, (idpName));
        statement.setInt(2, tenantId);
        ResultSet resultSet = statement.executeQuery();
        int resultSize = 0;
        if (resultSet.next()) {
            resultSize = resultSet.getRow();
        }
        statement.clearParameters();
        return resultSize;
    }
}

