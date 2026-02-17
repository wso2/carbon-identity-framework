/*
 * Copyright (c) 2022, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.carbon.idp.mgt.dao;

import org.apache.commons.dbcp.BasicDataSource;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.junit.Assert;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.wso2.carbon.base.MultitenantConstants;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.identity.action.management.api.exception.ActionMgtServerException;
import org.wso2.carbon.identity.action.management.api.model.Action;
import org.wso2.carbon.identity.action.management.api.model.EndpointConfig;
import org.wso2.carbon.identity.action.management.api.service.ActionManagementService;
import org.wso2.carbon.identity.application.common.model.*;
import org.wso2.carbon.identity.base.AuthenticatorPropertyConstants.DefinedByType;
import org.wso2.carbon.identity.common.testng.WithCarbonHome;
import org.wso2.carbon.identity.core.model.ExpressionNode;
import org.wso2.carbon.identity.core.util.IdentityDatabaseUtil;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;
import org.wso2.carbon.identity.core.util.IdentityUtil;
import org.wso2.carbon.identity.organization.management.service.util.OrganizationManagementUtil;
import org.wso2.carbon.identity.secret.mgt.core.SecretManagerImpl;
import org.wso2.carbon.identity.secret.mgt.core.model.SecretType;
import org.wso2.carbon.idp.mgt.IdentityProviderManagementClientException;
import org.wso2.carbon.idp.mgt.IdentityProviderManagementException;
import org.wso2.carbon.idp.mgt.cache.IdPCacheByHRI;
import org.wso2.carbon.idp.mgt.cache.IdPCacheByMetadataProperty;
import org.wso2.carbon.idp.mgt.cache.IdPCacheByName;
import org.wso2.carbon.idp.mgt.cache.IdPCacheByResourceId;
import org.wso2.carbon.idp.mgt.cache.IdPCacheEntry;
import org.wso2.carbon.idp.mgt.cache.IdPHomeRealmIdCacheKey;
import org.wso2.carbon.idp.mgt.cache.IdPMetadataPropertyCacheKey;
import org.wso2.carbon.idp.mgt.cache.IdPNameCacheKey;
import org.wso2.carbon.idp.mgt.cache.IdPResourceIdCacheKey;
import org.wso2.carbon.idp.mgt.internal.IdpMgtServiceComponentHolder;
import org.wso2.carbon.idp.mgt.model.ConnectedAppsResult;
import org.wso2.carbon.idp.mgt.util.ActionMgtTestUtil;
import org.wso2.carbon.idp.mgt.util.IdPManagementConstants;
import org.wso2.carbon.idp.mgt.util.IdPSecretsProcessor;

import java.lang.reflect.Field;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertThrows;
import static org.testng.Assert.assertTrue;
import static org.wso2.carbon.idp.mgt.util.IdPManagementConstants.RESET_PROVISIONING_ENTITIES_ON_CONFIG_UPDATE;

/**
 * Unit tests for CacheBackedIdPManagementDAO.
 */
@WithCarbonHome
public class CacheBackedIdPMgtDAOTest {

    private static final String DB_NAME = "test";
    private static final Integer SUPER_TENANT_ID = -1234;
    private static final Integer SAMPLE_TENANT_ID1 = 1;
    private static final Integer SAMPLE_TENANT_ID2 = 2;
    private static final Integer NOT_EXISTING_TENANT_ID = 4;
    private static final String SUPER_TENANT_DOMAIN = "carbon.super";
    private static final String SAMPLE_TENANT_DOMAIN1 = "custom.tenant.1";
    private static final String SAMPLE_TENANT_DOMAIN2 = "custom.tenant.2";
    private static final String NOT_EXISTING_TENANT_DOMAIN = "not.exist";
    private static final String IDP_GROUP1 = "idpGroup1";
    private static final String IDP_GROUP2 = "idpGroup2";
    private static final String IDP_GROUP2_ID = "idpGroup2Id";

    private static final String CUSTOM_IDP_NAME = "customIdP";

    private static Action action;
    private static EndpointConfig endpointConfig;
    private static EndpointConfig endpointConfigToBeUpdated;
    private IdentityProvider idpForErrorScenarios;
    private IdentityProvider userDefinedIdP;
    private static Map<String, BasicDataSource> dataSourceMap = new HashMap<>();
    private CacheBackedIdPMgtDAO cacheBackedIdPMgtDAO;
    private CacheBackedIdPMgtDAO cacheBackedIdPMgtDAOForException;
    private IdPManagementDAO idPManagementDAO;
    private IdPManagementDAO idPManagementDAOForException;
    private ActionManagementService actionManagementService;
    private final ActionManagementService actionManagementServiceForException = mock(ActionManagementService.class);
    MockedStatic<IdentityTenantUtil> identityTenantUtil;
    MockedStatic<OrganizationManagementUtil> organizationManagementUtilMockedStatic;

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
        throw new IllegalArgumentException("DB Script file name cannot be empty");
    }

    private static void closeH2Database() throws Exception {

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

    @BeforeClass
    public void setUpClass() throws Exception {

        SecretManagerImpl secretManager = mock(SecretManagerImpl.class);
        SecretType secretType = mock(SecretType.class);
        IdpMgtServiceComponentHolder.getInstance().setSecretManager(secretManager);
        when(secretType.getId()).thenReturn("secretId");
        doReturn(secretType).when(secretManager).getSecretType(any());
        when(secretManager.isSecretExist(anyString(), anyString())).thenReturn(false);

        endpointConfig = ActionMgtTestUtil.createEndpointConfig("http://localhost", "admin", "admin");
        endpointConfigToBeUpdated = ActionMgtTestUtil.createEndpointConfig(
                "http://localhost1", "admin1", "admin1");
        action = ActionMgtTestUtil.createAction(endpointConfig);
        userDefinedIdP = ActionMgtTestUtil.createIdPWithUserDefinedFederatedAuthenticatorConfig(
                CUSTOM_IDP_NAME, action.getEndpoint());
        idpForErrorScenarios = ActionMgtTestUtil.createIdPWithUserDefinedFederatedAuthenticatorConfig(
                CUSTOM_IDP_NAME + "Error", action.getEndpoint());

        actionManagementService = mock(ActionManagementService.class);
        when(actionManagementService.addAction(anyString(), any(), any())).thenReturn(action);
        when(actionManagementService.updateAction(anyString(), any(), any(), any())).thenReturn(action);
        when(actionManagementService.getActionByActionId(anyString(), any(), any())).thenReturn(action);
        doNothing().when(actionManagementService).deleteAction(anyString(), any(), any());

        doThrow(ActionMgtServerException.class).when(actionManagementServiceForException)
                .deleteAction(any(), any(), any());
        when(actionManagementServiceForException.getActionByActionId(anyString(), any(), any())).thenReturn(action);
    }

    @BeforeMethod
    public void setup() throws Exception {

        IdPSecretsProcessor idpSecretsProcessor = mock(IdPSecretsProcessor.class);
        when(idpSecretsProcessor.decryptAssociatedSecrets(any())).thenAnswer(
                invocation -> invocation.getArguments()[0]);
        when(idpSecretsProcessor.encryptAssociatedSecrets(any())).thenAnswer(
                invocation -> invocation.getArguments()[0]);
        idPManagementDAO = new IdPManagementDAO();
        idPManagementDAOForException = mock(IdPManagementDAO.class);
        cacheBackedIdPMgtDAOForException = new CacheBackedIdPMgtDAO(idPManagementDAOForException);

        Field idpSecretsProcessorField = IdPManagementDAO.class.getDeclaredField("idpSecretsProcessorService");
        idpSecretsProcessorField.setAccessible(true);
        idpSecretsProcessorField.set(idPManagementDAO, idpSecretsProcessor);

        cacheBackedIdPMgtDAO = new CacheBackedIdPMgtDAO(idPManagementDAO);
        initiateH2Database(DB_NAME, getFilePath("h2.sql"));

        identityTenantUtil = mockStatic(IdentityTenantUtil.class);
        identityTenantUtil.when(() -> IdentityTenantUtil.getTenantId(SUPER_TENANT_DOMAIN)).
                thenReturn(SUPER_TENANT_ID);
        identityTenantUtil.when(() -> IdentityTenantUtil.getTenantId(SAMPLE_TENANT_DOMAIN1))
                .thenReturn(SAMPLE_TENANT_ID1);
        identityTenantUtil.when(() -> IdentityTenantUtil.getTenantId(SAMPLE_TENANT_DOMAIN2))
                .thenReturn(SAMPLE_TENANT_ID2);
        identityTenantUtil.when(() -> IdentityTenantUtil.getTenantDomain(SUPER_TENANT_ID))
                .thenReturn(SUPER_TENANT_DOMAIN);
        identityTenantUtil.when(() -> IdentityTenantUtil.getTenantDomain(SAMPLE_TENANT_ID1))
                .thenReturn(SAMPLE_TENANT_DOMAIN1);
        identityTenantUtil.when(() -> IdentityTenantUtil.getTenantDomain(SAMPLE_TENANT_ID2))
                .thenReturn(SAMPLE_TENANT_DOMAIN2);

        organizationManagementUtilMockedStatic = mockStatic(OrganizationManagementUtil.class);
        organizationManagementUtilMockedStatic.when(() -> OrganizationManagementUtil.isOrganization(anyString()))
                .thenReturn(false);

        IdpMgtServiceComponentHolder.getInstance().setActionManagementService(actionManagementService);
    }

    @AfterMethod
    public void tearDown() throws Exception {

        try (MockedStatic<IdentityDatabaseUtil> identityDatabaseUtil = mockStatic(IdentityDatabaseUtil.class);
             Connection connection = getConnection(DB_NAME)) {
            identityDatabaseUtil.when(() -> IdentityDatabaseUtil.getDBConnection(anyBoolean())).thenReturn(connection);
            identityDatabaseUtil.when(IdentityDatabaseUtil::getDBConnection).thenReturn(connection);
            identityDatabaseUtil.when(IdentityDatabaseUtil::getDataSource).thenReturn(dataSourceMap.get(DB_NAME));
            // Deleting IDP.
            cacheBackedIdPMgtDAO.deleteIdP("testIdP1New", SUPER_TENANT_ID, SUPER_TENANT_DOMAIN);
            cacheBackedIdPMgtDAO.deleteIdP("testIdP2New", SUPER_TENANT_ID, SUPER_TENANT_DOMAIN);
            cacheBackedIdPMgtDAO.deleteIdP("testIdP3New", SAMPLE_TENANT_ID1, SAMPLE_TENANT_DOMAIN1);
        }

        closeH2Database();
        identityTenantUtil.close();
        organizationManagementUtilMockedStatic.close();
    }

    @DataProvider
    public Object[][] getIdPsData() {

        return new Object[][]{
                {SUPER_TENANT_ID, SUPER_TENANT_DOMAIN, 3},
                {SAMPLE_TENANT_ID1, SAMPLE_TENANT_DOMAIN1, 1},
                {NOT_EXISTING_TENANT_ID, NOT_EXISTING_TENANT_DOMAIN, 0}
        };
    }

    @Test(dataProvider = "getIdPsData")
    public void testGetIdPs(int tenantId, String tenantDomain, int idpCount) throws Exception {

        try (MockedStatic<IdentityDatabaseUtil> identityDatabaseUtil = mockStatic(IdentityDatabaseUtil.class);
             Connection connection = getConnection(DB_NAME)) {
            identityDatabaseUtil.when(() -> IdentityDatabaseUtil.getDBConnection(anyBoolean())).thenReturn(connection);
            identityDatabaseUtil.when(IdentityDatabaseUtil::getDataSource).thenReturn(dataSourceMap.get(DB_NAME));
            addTestIdps();

            // Directly providing initiated db connection as connection parameter.
            List<IdentityProvider> idpList1 = cacheBackedIdPMgtDAO.getIdPs(connection, tenantId, tenantDomain);
            assertEquals(idpList1.size(), idpCount);
            // Providing null as connection parameter so value of connection parameter will be retrieved
            // from the mock above.
            List<IdentityProvider> idpList2 = cacheBackedIdPMgtDAO.getIdPs(null, tenantId, tenantDomain);
            assertEquals(idpList2.size(), idpCount);
        }
    }

    @Test(dataProvider = "getIdPsData")
    public void testGetIdPsException(int tenantId, String tenantDomain, int idpCount) throws Exception {

        try (MockedStatic<IdentityDatabaseUtil> identityDatabaseUtil = mockStatic(IdentityDatabaseUtil.class)) {
            try (Connection connection = getConnection(DB_NAME)) {
                identityDatabaseUtil.when(() -> IdentityDatabaseUtil.getDBConnection(anyBoolean()))
                        .thenReturn(connection);
                identityDatabaseUtil.when(IdentityDatabaseUtil::getDataSource).thenReturn(dataSourceMap.get(DB_NAME));
                addTestIdps();
            }
            assertThrows(IdentityProviderManagementException.class, () ->
                    cacheBackedIdPMgtDAO.getIdPs(null, tenantId, tenantDomain));
        }
    }

    @DataProvider
    public Object[][] getIdPsSearchData() {

        return new Object[][]{
                {SUPER_TENANT_ID, SUPER_TENANT_DOMAIN, "testIdP1", 1},
                {SAMPLE_TENANT_ID1, SAMPLE_TENANT_DOMAIN1, "testIdP3", 1},
                {SUPER_TENANT_ID, SUPER_TENANT_DOMAIN, "", 3},
                {SUPER_TENANT_ID, SUPER_TENANT_DOMAIN, "test*", 2},
                {SUPER_TENANT_ID, SUPER_TENANT_DOMAIN, "????IdP*", 2},
                {SUPER_TENANT_ID, SUPER_TENANT_DOMAIN, "tes_I*", 2},
                {SUPER_TENANT_ID, SUPER_TENANT_DOMAIN, "*1", 1},
                {SUPER_TENANT_ID, SUPER_TENANT_DOMAIN, "Notexist", 0},
        };
    }

    @Test(dataProvider = "getIdPsSearchData")
    public void testGetIdPsSearch(int tenantId, String tenantDomain,  String filter, int resultCount) throws Exception {

        try (MockedStatic<IdentityDatabaseUtil> identityDatabaseUtil = mockStatic(IdentityDatabaseUtil.class);
             Connection connection = getConnection(DB_NAME)) {
            identityDatabaseUtil.when(() -> IdentityDatabaseUtil.getDBConnection(anyBoolean())).thenReturn(connection);
            identityDatabaseUtil.when(IdentityDatabaseUtil::getDBConnection).thenReturn(connection);
            identityDatabaseUtil.when(IdentityDatabaseUtil::getDataSource).thenReturn(dataSourceMap.get(DB_NAME));
            addTestIdps();

            // Directly providing initiated db connection as connection parameter.
            List<IdentityProvider> idpList1 = cacheBackedIdPMgtDAO.getIdPsSearch(connection, tenantId,
                    tenantDomain, filter);
            assertEquals(idpList1.size(), resultCount);
            // Providing null as connection parameter so value of connection parameter will be retrieved
            // from the mock above.
            List<IdentityProvider> idpList2 = cacheBackedIdPMgtDAO.getIdPsSearch(null, tenantId,
                    tenantDomain, filter);
            assertEquals(idpList2.size(), resultCount);
        }
    }

    @Test(dataProvider = "getIdPsSearchData")
    public void testGetIdPsSearchException(int tenantId, String tenantDomain, String filter, int resultCount)
            throws Exception {

        try (MockedStatic<IdentityDatabaseUtil> identityDatabaseUtil = mockStatic(IdentityDatabaseUtil.class)) {
            try (Connection connection = getConnection(DB_NAME)) {
                identityDatabaseUtil.when(() -> IdentityDatabaseUtil.getDBConnection(anyBoolean())).thenReturn(connection);
                identityDatabaseUtil.when(IdentityDatabaseUtil::getDBConnection).thenReturn(connection);
                identityDatabaseUtil.when(IdentityDatabaseUtil::getDataSource).thenReturn(dataSourceMap.get(DB_NAME));
                addTestIdps();
            }
            assertThrows(IdentityProviderManagementException.class, () ->
                    cacheBackedIdPMgtDAO.getIdPsSearch(null, tenantId, tenantDomain, filter));
        }
    }

    @DataProvider
    public Object[][] getPaginatedIdPsSearchData() {

        ExpressionNode expressionNode = new ExpressionNode();
        List<ExpressionNode> expressionNodeList = new ArrayList<>();
        expressionNodeList.add(expressionNode);

        List<String> attributesList1 = Arrays.asList("id", "name", "description", "isEnabled", "image", "isPrimary");
        List<String> attributesList2 = Arrays.asList("homeRealmIdentifier", "isFederationHub", "certificate", "alias",
                "claims", "roles", "federatedAuthenticators", "provisioning");

        return new Object[][]{
                {expressionNodeList, 2, 0, "ASC", "NAME", attributesList1, 2},
                {expressionNodeList, 1, 1, "ASC", "NAME", attributesList2, 1},
                {expressionNodeList, 2, 0, "DESC", "NAME", attributesList1, 2},
        };
    }

    @Test(dataProvider = "getPaginatedIdPsSearchData")
    public void testGetPaginatedIdPsSearch(List<ExpressionNode> expressionNodes, int limit, int offset, String order,
                                           String sortBy, List<String> attributes, int count) throws Exception {

        try (MockedStatic<IdentityDatabaseUtil> identityDatabaseUtil = mockStatic(IdentityDatabaseUtil.class);
             Connection connection = getConnection(DB_NAME)) {
            identityDatabaseUtil.when(() -> IdentityDatabaseUtil.getDBConnection(anyBoolean())).thenReturn(connection);
            identityDatabaseUtil.when(IdentityDatabaseUtil::getDataSource).thenReturn(dataSourceMap.get(DB_NAME));
            addTestIdps();
            List<IdentityProvider> idpList = cacheBackedIdPMgtDAO.getPaginatedIdPsSearch(SUPER_TENANT_ID,
                    expressionNodes, limit, offset, order, sortBy, attributes);
            assertEquals(idpList.size(), count);
        }
    }

    @Test
    public void testGetPaginatedIdPsSearchException() throws Exception {

        ExpressionNode expressionNode = new ExpressionNode();
        List<ExpressionNode> expressionNodes = new ArrayList<>();
        expressionNodes.add(expressionNode);
        List<String> attributes = Arrays.asList("WrongAttribute");

        try (MockedStatic<IdentityDatabaseUtil> identityDatabaseUtil = mockStatic(IdentityDatabaseUtil.class);
             Connection connection = getConnection(DB_NAME)) {
            identityDatabaseUtil.when(() -> IdentityDatabaseUtil.getDBConnection(anyBoolean())).thenReturn(connection);
            identityDatabaseUtil.when(IdentityDatabaseUtil::getDBConnection).thenReturn(connection);
            identityDatabaseUtil.when(IdentityDatabaseUtil::getDataSource).thenReturn(dataSourceMap.get(DB_NAME));
            addTestIdps();
            assertThrows(IdentityProviderManagementClientException.class, () ->
                    cacheBackedIdPMgtDAO.getPaginatedIdPsSearch(SUPER_TENANT_ID, expressionNodes, 2, 0,
                            "ASC", "NAME", attributes));
        }
    }

    @DataProvider
    public Object[][] getTotalIdPCountData() {

        ExpressionNode expressionNode1 = new ExpressionNode();
        List<ExpressionNode> expressionNodesList1 = new ArrayList<>();
        expressionNodesList1.add(expressionNode1);
        ExpressionNode expressionNode2 = new ExpressionNode();
        expressionNode2.setAttributeValue("name");
        expressionNode2.setOperation("sw");
        expressionNode2.setValue("test");
        List<ExpressionNode> expressionNodesList2 = new ArrayList<>();
        expressionNodesList2.add(expressionNode2);

        return new Object[][]{
                {SUPER_TENANT_ID, expressionNodesList1, 3},
                {SUPER_TENANT_ID, expressionNodesList2, 2},
                {SAMPLE_TENANT_ID1, expressionNodesList1, 1},
        };
    }

    @Test(dataProvider = "getTotalIdPCountData")
    public void testGetTotalIdPCount(int tenantId, List<ExpressionNode> expressionNodes, int count) throws Exception {

        try (MockedStatic<IdentityDatabaseUtil> identityDatabaseUtil = mockStatic(IdentityDatabaseUtil.class);
             Connection connection = getConnection(DB_NAME)) {
            identityDatabaseUtil.when(() -> IdentityDatabaseUtil.getDBConnection(anyBoolean())).thenReturn(connection);
            identityDatabaseUtil.when(IdentityDatabaseUtil::getDataSource).thenReturn(dataSourceMap.get(DB_NAME));
            addTestIdps();
            int resultCount = cacheBackedIdPMgtDAO.getTotalIdPCount(tenantId, expressionNodes);
            assertEquals(resultCount, count);
        }
    }

    @Test(dataProvider = "getTotalIdPCountData")
    public void testGetTotalIdPCountException(int tenantId, List<ExpressionNode> expressionNodes, int count)
            throws Exception {

        try (MockedStatic<IdentityDatabaseUtil> identityDatabaseUtil = mockStatic(IdentityDatabaseUtil.class)) {
            try (Connection connection = getConnection(DB_NAME)) {
                identityDatabaseUtil.when(() -> IdentityDatabaseUtil.getDBConnection(anyBoolean())).thenReturn(connection);
                identityDatabaseUtil.when(IdentityDatabaseUtil::getDataSource).thenReturn(dataSourceMap.get(DB_NAME));
                addTestIdps();
            }
            assertThrows(IdentityProviderManagementException.class, () ->
                    cacheBackedIdPMgtDAO.getTotalIdPCount(tenantId, expressionNodes));
        }
    }

    @DataProvider
    public Object[][] getIdPByNameData() {

        return new Object[][]{
                {"testIdP1", SUPER_TENANT_ID, SUPER_TENANT_DOMAIN,  true},
                {"testIdP3", SAMPLE_TENANT_ID1, SAMPLE_TENANT_DOMAIN1, true},
                {"notExist", SUPER_TENANT_ID, SUPER_TENANT_DOMAIN, false},
        };
    }

    // TODO failing test start
    @Test(dataProvider = "getIdPByNameData")
    public void testGetIdPByName(String idpName, int tenantId, String tenantDomain, boolean isExist) throws Exception {

        try (MockedStatic<IdentityDatabaseUtil> identityDatabaseUtil = mockStatic(IdentityDatabaseUtil.class);
             Connection connection = getConnection(DB_NAME)) {
            identityDatabaseUtil.when(() -> IdentityDatabaseUtil.getDBConnection(anyBoolean())).thenReturn(connection);
            identityDatabaseUtil.when(IdentityDatabaseUtil::getDataSource).thenReturn(dataSourceMap.get(DB_NAME));
            addTestIdps();

            // Retrieving IDP from DB and adding to cache.
            IdentityProvider idpResult = cacheBackedIdPMgtDAO.getIdPByName(connection, idpName,
                    tenantId, tenantDomain);

            IdentityProvider idpFromCache = null;
            if (isExist) {
                // Retrieving IDP using cache entry.
                idpFromCache = idpFromCacheByName(idpName);
            }
            if (isExist) {
                assertEquals(idpResult.getIdentityProviderName(), idpName, "'getIdPByName' method fails");
                if (idpFromCache != null) {
                    assertEquals(idpFromCache.getIdentityProviderName(), idpName,
                            "'getIdPByName' method fails");
                }
            } else {
                assertNull(idpResult, "'getIdPByName' method fails");
                assertNull(idpFromCache, "'getIdPByName' method fails");
            }
        }
    }

    @DataProvider
    public Object[][] getIdPByIdData() {

        return new Object[][]{
                {"testIdP1", 1, SUPER_TENANT_ID, SUPER_TENANT_DOMAIN, true},
                {"testIdP3", 3, SAMPLE_TENANT_ID1, SAMPLE_TENANT_DOMAIN1, true},
                {"notExist", 99, SUPER_TENANT_ID, SUPER_TENANT_DOMAIN, false},
        };
    }

    @Test(dataProvider = "getIdPByIdData")
    public void testGetIdPById(String idpName, int idpId, int tenantId, String tenantDomain, boolean isExist)
            throws Exception {

        try (MockedStatic<IdentityDatabaseUtil> identityDatabaseUtil = mockStatic(IdentityDatabaseUtil.class);
             Connection connection = getConnection(DB_NAME)) {
            identityDatabaseUtil.when(() -> IdentityDatabaseUtil.getDBConnection(anyBoolean())).thenReturn(connection);
            identityDatabaseUtil.when(IdentityDatabaseUtil::getDataSource).thenReturn(dataSourceMap.get(DB_NAME));
            addTestIdps();
            // Retrieving IDP form DB and adding to cache.
            IdentityProvider idpResult = cacheBackedIdPMgtDAO.getIdPById(connection, idpId, tenantId, tenantDomain);

            IdentityProvider idpFromCache = null;
            if (isExist) {
                // Retrieving IDP using cache entry.
                idpFromCache = idpFromCacheByName(idpName);
            }
            if (isExist) {
                assertEquals(idpResult.getIdentityProviderName(), idpName, "'getIDPbyId' method fails");
                if (idpFromCache != null) {
                    assertEquals(idpFromCache.getIdentityProviderName(), idpName, "'getIDPbyId' method fails");
                }
            } else {
                assertNull(idpResult, "'getIDPbyId' method fails");
            }
        }
    }

    @Test
    public void testGetIdPNamesById() throws Exception {

        try (MockedStatic<IdentityDatabaseUtil> identityDatabaseUtil = mockStatic(IdentityDatabaseUtil.class);
             Connection connection = getConnection(DB_NAME)) {
            identityDatabaseUtil.when(() -> IdentityDatabaseUtil.getDBConnection(anyBoolean())).thenReturn(connection);
            identityDatabaseUtil.when(IdentityDatabaseUtil::getDataSource).thenReturn(dataSourceMap.get(DB_NAME));
            addTestIdps();

            Set<String> idpIds = new HashSet<>(Arrays.asList(
                    cacheBackedIdPMgtDAO.getIdPByName(connection, "testIdP1", SUPER_TENANT_ID, SUPER_TENANT_DOMAIN)
                            .getId(),
                    cacheBackedIdPMgtDAO.getIdPByName(connection, "testIdP2", SUPER_TENANT_ID, SUPER_TENANT_DOMAIN)
                            .getId()
            ));
            // Retrieving IDP form DB and adding to cache.
            Map<String, String> idpNameMap = cacheBackedIdPMgtDAO.getIdPNamesById(SUPER_TENANT_ID, idpIds);

            assertTrue(idpNameMap.containsValue("testIdP1"));
            assertTrue(idpNameMap.containsValue("testIdP2"));
        }
    }

    @DataProvider
    public Object[][] getIDPbyResourceIdData() {

        return new Object[][]{
                {"testIdP1", SUPER_TENANT_ID, SUPER_TENANT_DOMAIN, true},
                {"testIdP3", SAMPLE_TENANT_ID1, SAMPLE_TENANT_DOMAIN1, true},
                {"notExist", SUPER_TENANT_ID, SUPER_TENANT_DOMAIN, false},
        };
    }

    @Test(dataProvider = "getIDPbyResourceIdData")
    public void testGetIDPbyResourceId(String idpName, int tenantId, String tenantDomain, boolean isExist)
            throws Exception {

        try (MockedStatic<IdentityDatabaseUtil> identityDatabaseUtil = mockStatic(IdentityDatabaseUtil.class);
             Connection connection = getConnection(DB_NAME)) {
            identityDatabaseUtil.when(() -> IdentityDatabaseUtil.getDBConnection(anyBoolean())).thenReturn(connection);
            identityDatabaseUtil.when(IdentityDatabaseUtil::getDataSource).thenReturn(dataSourceMap.get(DB_NAME));
            addTestIdps();
            String uuid = "";
            // Retrieving IDP from DB.
            IdentityProvider idpResults = idPManagementDAO.getIdPByName(connection, idpName, tenantId, tenantDomain);
            IdentityProvider idpFromDB = new IdentityProvider();
            IdentityProvider idpFromCache = new IdentityProvider();

            if (idpResults != null) {
                uuid = idpResults.getResourceId();
                // Retrieving IDP from DB and adds it to the cache.
                idpFromDB = cacheBackedIdPMgtDAO.getIdPByResourceId(uuid, tenantId, tenantDomain);
                // Retrieving IDP using cache entry.
                idpFromCache = idpFromCacheByResourceId(uuid);
            }
            if (isExist) {
                assertEquals(idpFromDB.getIdentityProviderName(), idpName,
                        "'getIDPbyResourceId' method fails");
                if (idpFromCache != null) {
                    assertEquals(idpFromCache.getIdentityProviderName(), idpName,
                            "'getIDPbyResourceId' method fails");
                }
            } else {
                assertNull(idpResults);
            }
        }
    }

    @DataProvider
    public Object[][] getIDPNameByResourceIdData() {

        return new Object[][]{
                {"testIdP1", SUPER_TENANT_ID, SUPER_TENANT_DOMAIN},
                {"testIdP3", SAMPLE_TENANT_ID1, SAMPLE_TENANT_DOMAIN1}
        };
    }

    @Test
    public void getAllUserDefinedFederatedAuthenticators() throws Exception {

        try (MockedStatic<IdentityDatabaseUtil> identityDatabaseUtil = mockStatic(IdentityDatabaseUtil.class);
             Connection connection = getConnection(DB_NAME)) {
            identityDatabaseUtil.when(() -> IdentityDatabaseUtil.getDBConnection(anyBoolean())).thenReturn(connection);
            identityDatabaseUtil.when(IdentityDatabaseUtil::getDBConnection).thenReturn(connection);
            identityDatabaseUtil.when(IdentityDatabaseUtil::getDataSource).thenReturn(dataSourceMap.get(DB_NAME));
            addTestIdps();

            List<FederatedAuthenticatorConfig> result = cacheBackedIdPMgtDAO
                    .getAllUserDefinedFederatedAuthenticators(SUPER_TENANT_ID);
            assertEquals(result.size(), 1);
            assertEquals(result.get(0).getName(), userDefinedIdP.getFederatedAuthenticatorConfigs()[0].getName());

            // Test user defined federated authenticators caching.
            IdPManagementDAO mockedIdPManagementDAO = mock(IdPManagementDAO.class);
            CacheBackedIdPMgtDAO mockedCacheBackedIdPMgtDAO = new CacheBackedIdPMgtDAO(mockedIdPManagementDAO);
            List<FederatedAuthenticatorConfig> cachedResult = mockedCacheBackedIdPMgtDAO
                    .getAllUserDefinedFederatedAuthenticators(SUPER_TENANT_ID);
            verify(mockedIdPManagementDAO, never()).getAllUserDefinedFederatedAuthenticators(anyInt());
            assertEquals(cachedResult, result);
        }
    }

    @DataProvider
    public Object[][] getIdPsSearchWithExpressionNodesData() {

        ExpressionNode expressionNode1 = new ExpressionNode();
        expressionNode1.setAttributeValue("name");
        expressionNode1.setOperation("co");
        expressionNode1.setValue("IdP");
        List<ExpressionNode> expressionNodesList1 = new ArrayList<>();
        expressionNodesList1.add(expressionNode1);

        ExpressionNode expressionNode2 = new ExpressionNode();
        List<ExpressionNode> expressionNodesList2 = new ArrayList<>();
        expressionNode2.setAttributeValue("name");
        expressionNode2.setOperation("eq");
        expressionNode2.setValue("testIdP1");
        expressionNodesList2.add(expressionNode2);

        ExpressionNode expressionNode3 = new ExpressionNode();
        List<ExpressionNode> expressionNodesList3 = new ArrayList<>();
        expressionNode3.setAttributeValue("name");
        expressionNode3.setOperation("ew");
        expressionNode3.setValue("2");
        expressionNodesList3.add(expressionNode3);

        return new Object[][]{
                {SUPER_TENANT_ID, expressionNodesList1, 2, 0, "ASC", "NAME", 2, "customIdP"},
                {SUPER_TENANT_ID, expressionNodesList1, 2, 0, "DESC", "NAME", 2, "testIdP2"},
                {SAMPLE_TENANT_ID1, expressionNodesList1, 1, 0, "ASC", "NAME", 1, "testIdP3"},
                {SUPER_TENANT_ID, expressionNodesList2, 1, 0, "ASC", "NAME", 1, "testIdP1"},
                {SUPER_TENANT_ID, expressionNodesList3, 1, 0, "ASC", "NAME", 1, "testIdP2"},
        };
    }

    @Test
    public void testGetIdPGroupsByIds() throws Exception {

        try (MockedStatic<IdentityDatabaseUtil> identityDatabaseUtil = mockStatic(IdentityDatabaseUtil.class);
             Connection connection = getConnection(DB_NAME)) {
            identityDatabaseUtil.when(() -> IdentityDatabaseUtil.getDBConnection(anyBoolean())).thenReturn(connection);
            identityDatabaseUtil.when(IdentityDatabaseUtil::getDataSource).thenReturn(dataSourceMap.get(DB_NAME));

            IdPManagementDAO mockedIdPManagementDAO = mock(IdPManagementDAO.class);
            CacheBackedIdPMgtDAO mockedCacheBackedIdPMgtDAO = new CacheBackedIdPMgtDAO(mockedIdPManagementDAO);

            mockedCacheBackedIdPMgtDAO.getIdPGroupsByIds(Arrays.asList("testIdP1", "testIdP2"), SUPER_TENANT_ID);
            verify(mockedIdPManagementDAO, times(1))
                    .getIdPGroupsByIds(any(), anyInt());
        }
    }

    @Test
    public void testGetConnectedAppsOfLocalAuthenticator() throws Exception {

        try (MockedStatic<IdentityDatabaseUtil> identityDatabaseUtil = mockStatic(IdentityDatabaseUtil.class);
             Connection connection = getConnection(DB_NAME)) {
            identityDatabaseUtil.when(() -> IdentityDatabaseUtil.getDBConnection(anyBoolean())).thenReturn(connection);
            identityDatabaseUtil.when(IdentityDatabaseUtil::getDataSource).thenReturn(dataSourceMap.get(DB_NAME));

            IdPManagementDAO mockedIdPManagementDAO = mock(IdPManagementDAO.class);
            CacheBackedIdPMgtDAO mockedCacheBackedIdPMgtDAO = new CacheBackedIdPMgtDAO(mockedIdPManagementDAO);

            mockedCacheBackedIdPMgtDAO.getConnectedAppsOfLocalAuthenticator("testIdP1", SUPER_TENANT_ID, 5, 0);
            verify(mockedIdPManagementDAO, times(1))
                    .getConnectedAppsOfLocalAuthenticator(anyString(), anyInt(), any(), any());
        }
    }

    @Test(dataProvider = "getIdPsSearchWithExpressionNodesData")
    public void testGetIdPsSearchWithExpressionNodes(int tenantId, List<ExpressionNode> expressionNodes, int limit,
                                                     int offset, String sortOrder, String sortBy, int count,
                                                     String firstIdp) throws Exception {

        try (MockedStatic<IdentityDatabaseUtil> identityDatabaseUtil = mockStatic(IdentityDatabaseUtil.class);
             Connection connection = getConnection(DB_NAME)) {
            identityDatabaseUtil.when(() -> IdentityDatabaseUtil.getDBConnection(anyBoolean())).thenReturn(connection);
            identityDatabaseUtil.when(IdentityDatabaseUtil::getDBConnection).thenReturn(connection);
            identityDatabaseUtil.when(IdentityDatabaseUtil::getDataSource).thenReturn(dataSourceMap.get(DB_NAME));
            addTestIdps();

            List<IdentityProvider> idps = cacheBackedIdPMgtDAO.getPaginatedIdPsSearch(
                    tenantId, expressionNodes, limit, offset, sortOrder, sortBy);
            assertEquals(idps.size(), count);
            if (count > 0) {
                assertEquals(idps.get(0).getIdentityProviderName(), firstIdp);
            }
        }
    }

    @DataProvider
    public Object[][] getIdPDatForSearch() {

        return new Object[][]{
                {SUPER_TENANT_ID, SUPER_TENANT_DOMAIN, "testIdP1", 1},
                {SAMPLE_TENANT_ID1, SUPER_TENANT_DOMAIN, "testIdP3", 1},
                {SUPER_TENANT_ID, SUPER_TENANT_DOMAIN, "", 2},
                {SUPER_TENANT_ID, SUPER_TENANT_DOMAIN, "test*", 2},
                {SUPER_TENANT_ID, SUPER_TENANT_DOMAIN, "????IdP*", 2},
                {SUPER_TENANT_ID, SUPER_TENANT_DOMAIN, "tes_I*", 2},
                {SUPER_TENANT_ID, SUPER_TENANT_DOMAIN, "*1", 1},
                {SUPER_TENANT_ID, SUPER_TENANT_DOMAIN, "Notexist", 0},
        };
    }

    @Test(dataProvider = "getIdPDatForSearch")
    public void testGetTrustedTokenIssuersException(int tenantId, String tenantDomain, String filter, int resultCount)
            throws Exception {

        try (MockedStatic<IdentityDatabaseUtil> identityDatabaseUtil = mockStatic(IdentityDatabaseUtil.class)) {
            try (Connection connection = getConnection(DB_NAME)) {
                identityDatabaseUtil.when(() -> IdentityDatabaseUtil.getDBConnection(anyBoolean())).thenReturn(connection);
                identityDatabaseUtil.when(IdentityDatabaseUtil::getDBConnection).thenReturn(connection);
                identityDatabaseUtil.when(IdentityDatabaseUtil::getDataSource).thenReturn(dataSourceMap.get(DB_NAME));
                addTestIdps();
            }

            assertThrows(IdentityProviderManagementException.class, () ->
                    cacheBackedIdPMgtDAO.getIdPsSearch(null, tenantId, tenantDomain, filter));
        }
    }

    @DataProvider
    public Object[][] getTrustedTokenIssuersData() {

        ExpressionNode expressionNode = new ExpressionNode();
        List<ExpressionNode> expressionNodesList = new ArrayList<>();
        expressionNodesList.add(expressionNode);

        List<String> attributes1 = Arrays.asList("id", "name", "description", "isEnabled", "image", "isPrimary");
        List<String> attributes2 = Arrays.asList("homeRealmIdentifier", "isFederationHub", "certificate", "alias",
                "claims", "roles", "federatedAuthenticators", "provisioning");

        return new Object[][]{
                {SUPER_TENANT_ID, expressionNodesList, 2, 0, "ASC", "NAME", attributes1, 1},
                {SUPER_TENANT_ID, expressionNodesList, 1, 1, "ASC", "NAME", attributes2, 0},
                {SUPER_TENANT_ID, expressionNodesList, 2, 0, "DESC", "NAME", attributes1, 1},
        };
    }

    @Test(dataProvider = "getTrustedTokenIssuersData")
    public void testGetTrustedTokenIssuers(int tenantId, List<ExpressionNode> expressionNodes, int limit, int offset,
                                           String sortOrder, String sortBy, List<String> attributes, int resultCount)
            throws Exception {

        try (MockedStatic<IdentityDatabaseUtil> identityDatabaseUtil = mockStatic(IdentityDatabaseUtil.class);
             Connection connection = getConnection(DB_NAME)) {
            identityDatabaseUtil.when(() -> IdentityDatabaseUtil.getDBConnection(anyBoolean())).thenReturn(connection);
            identityDatabaseUtil.when(IdentityDatabaseUtil::getDBConnection).thenReturn(connection);
            identityDatabaseUtil.when(IdentityDatabaseUtil::getDataSource).thenReturn(dataSourceMap.get(DB_NAME));
            addTestTrustedTokenIssuers();

            List<IdentityProvider> idps1 = cacheBackedIdPMgtDAO.getPaginatedTrustedTokenIssuersSearch(tenantId,
                    expressionNodes, limit, offset, sortOrder, sortBy, attributes);
            assertEquals(idps1.size(), resultCount);
        }
    }

    @DataProvider
    public Object[][] getCountOfFilteredTrustedTokenIssuersData() {

        ExpressionNode expressionNode1 = new ExpressionNode();
        List<ExpressionNode> expressionNodesList1 = new ArrayList<>();
        expressionNodesList1.add(expressionNode1);
        ExpressionNode expressionNode2 = new ExpressionNode();
        expressionNode2.setAttributeValue("name");
        expressionNode2.setOperation("sw");
        expressionNode2.setValue("test");
        List<ExpressionNode> expressionNodesList2 = new ArrayList<>();
        expressionNodesList2.add(expressionNode2);

        return new Object[][]{
                {SUPER_TENANT_ID, expressionNodesList1, 1},
                {SUPER_TENANT_ID, expressionNodesList2, 1},
                {SAMPLE_TENANT_ID1, expressionNodesList1, 0},
        };
    }

    @Test(dataProvider = "getCountOfFilteredTrustedTokenIssuersData")
    public void testGetCountOfFilteredTrustedTokenIssuers(int tenantId, List<ExpressionNode> expNodes, int count)
            throws Exception {

        try (MockedStatic<IdentityDatabaseUtil> identityDatabaseUtil = mockStatic(IdentityDatabaseUtil.class);
             Connection connection = getConnection(DB_NAME)) {
            identityDatabaseUtil.when(() -> IdentityDatabaseUtil.getDBConnection(anyBoolean())).thenReturn(connection);
            identityDatabaseUtil.when(IdentityDatabaseUtil::getDBConnection).thenReturn(connection);
            identityDatabaseUtil.when(IdentityDatabaseUtil::getDataSource).thenReturn(dataSourceMap.get(DB_NAME));
            addTestTrustedTokenIssuers();

            int resultCount = cacheBackedIdPMgtDAO.getTotalTrustedTokenIssuerCount(tenantId, expNodes);
            assertEquals(resultCount, count);
        }
    }

    private void addTestTrustedTokenIssuers() throws IdentityProviderManagementException {

        // Initialize Test Identity Provider 1.
        IdentityProvider idp1 = new IdentityProvider();
        idp1.setIdentityProviderName("testIdP1");
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

        IdPGroup idPGroup1 = new IdPGroup();
        idPGroup1.setIdpGroupName(IDP_GROUP1);
        IdPGroup idPGroup2 = new IdPGroup();
        idPGroup2.setIdpGroupName(IDP_GROUP2);
        idp1.setIdPGroupConfig(new IdPGroup[]{idPGroup1, idPGroup2});

        idp1.setTrustedTokenIssuer(true);

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

        IdPGroup idPGroup3 = new IdPGroup();
        idPGroup3.setIdpGroupName(IDP_GROUP1);
        IdPGroup idPGroup4 = new IdPGroup();
        idPGroup4.setIdpGroupName(IDP_GROUP2);
        idPGroup4.setIdpGroupId(IDP_GROUP2_ID);
        idp2.setIdPGroupConfig(new IdPGroup[]{idPGroup3, idPGroup4});

        // Initialize Test Identity Provider 3.
        IdentityProvider idp3 = new IdentityProvider();
        idp3.setIdentityProviderName("testIdP3");
        idp3.setHomeRealmId("3");

        // IDP with PermissionsAndRoleConfig, FederatedAuthenticatorConfigs, ProvisioningConnectorConfigs, ClaimConfigs.
        idPManagementDAO.addIdP(idp1, SUPER_TENANT_ID);
        // IDP with Local Cliam Dialect ClaimConfigs.
        idPManagementDAO.addIdP(idp2, SUPER_TENANT_ID);
        // IDP with Only name.
        idPManagementDAO.addIdP(idp3, SAMPLE_TENANT_ID1);
    }

    @Test(dataProvider = "getIDPNameByResourceIdData")
    public void testGetIdPNameByResourceId(String idpName, int tenantId, String tenantDomain) throws Exception {

        try (MockedStatic<IdentityDatabaseUtil> identityDatabaseUtil = mockStatic(IdentityDatabaseUtil.class);
             Connection connection = getConnection(DB_NAME)) {
            identityDatabaseUtil.when(() -> IdentityDatabaseUtil.getDBConnection(anyBoolean())).thenReturn(connection);
            identityDatabaseUtil.when(IdentityDatabaseUtil::getDBConnection).thenReturn(connection);
            identityDatabaseUtil.when(IdentityDatabaseUtil::getDataSource).thenReturn(dataSourceMap.get(DB_NAME));
            addTestIdps();

            String uuid = "";
            // Retrieving IDP from DB.
            IdentityProvider idPResult = idPManagementDAO.getIdPByName(connection, idpName, tenantId, tenantDomain);
            uuid = idPResult.getResourceId();
            // Retrieving IDP using resourceId from DB and adding to cache.
            cacheBackedIdPMgtDAO.getIdPByResourceId(uuid, tenantId, tenantDomain);
            IdentityProvider idpFromCache = idpFromCacheByResourceId(uuid);

            try (MockedStatic<CarbonContext> carbonContext = mockStatic(CarbonContext.class)) {
                CarbonContext mockCarbonContext = mock(CarbonContext.class);
                carbonContext.when(CarbonContext::getThreadLocalCarbonContext).thenReturn(mockCarbonContext);
                when(CarbonContext.getThreadLocalCarbonContext().getTenantDomain()).thenReturn(tenantDomain);
                when(CarbonContext.getThreadLocalCarbonContext().getTenantId()).thenReturn(tenantId);
                // Retrieving IDP name from cache using resourceId as cache key.
                String name = cacheBackedIdPMgtDAO.getIdPNameByResourceId(uuid);

                if (idpFromCache != null) {
                    assertEquals(idpFromCache.getIdentityProviderName(), idpName,
                            "'getIDPNameByResourceId' method fails");
                    assertEquals(idpFromCache.getIdentityProviderName(), idpName,
                            "'getIDPNameByResourceId' method fails");
                }
                assertEquals(name, idpName);
            }
        }
    }

    @Test(dataProvider = "getIDPNameByResourceIdData")
    public void testGetIdPNameByResourceIdFromDB(String idpName, int tenantId, String tenantDomain) throws Exception {

        try (MockedStatic<IdentityDatabaseUtil> identityDatabaseUtil = mockStatic(IdentityDatabaseUtil.class);
             Connection connection = getConnection(DB_NAME)) {
            identityDatabaseUtil.when(() -> IdentityDatabaseUtil.getDBConnection(anyBoolean())).thenReturn(connection);
            identityDatabaseUtil.when(IdentityDatabaseUtil::getDBConnection).thenReturn(connection);
            identityDatabaseUtil.when(IdentityDatabaseUtil::getDataSource).thenReturn(dataSourceMap.get(DB_NAME));
            addTestIdps();

            try (MockedStatic<CarbonContext> carbonContext = mockStatic(CarbonContext.class)) {
                CarbonContext mockCarbonContext = mock(CarbonContext.class);
                carbonContext.when(CarbonContext::getThreadLocalCarbonContext).thenReturn(mockCarbonContext);
                when(CarbonContext.getThreadLocalCarbonContext().getTenantDomain()).thenReturn(tenantDomain);
                when(CarbonContext.getThreadLocalCarbonContext().getTenantId()).thenReturn(tenantId);

                // Retrieving IDP from DB.
                IdentityProvider idPResult = idPManagementDAO.getIdPByName(connection, idpName, tenantId,
                        tenantDomain);
                String uuid = idPResult.getResourceId();

                String nameFromDB = cacheBackedIdPMgtDAO.getIdPNameByResourceId(uuid);
                assertEquals(nameFromDB, idpName);
            }
        }
    }

    @DataProvider
    public Object[][] getIdPByAuthenticatorPropertyValueWithoutAuthenticatorData() {

        return new Object[][]{
                {"testIdP1", "Property1", "value1", true},
                {"testIdP1", "Property2", "value2", true},
                {"NotExist", "Null", "Null", false},
        };
    }

    @Test(dataProvider = "getIdPByAuthenticatorPropertyValueWithoutAuthenticatorData")
    public void testGetIdPByAuthenticatorPropertyValueWithoutAuthenticator(String idpName, String property,
                                                                           String value, boolean isExist)
            throws Exception {

        try (MockedStatic<IdentityDatabaseUtil> identityDatabaseUtil = mockStatic(IdentityDatabaseUtil.class);
             Connection connection = getConnection(DB_NAME)) {
            identityDatabaseUtil.when(() -> IdentityDatabaseUtil.getDBConnection(anyBoolean())).thenReturn(connection);
            identityDatabaseUtil.when(IdentityDatabaseUtil::getDataSource).thenReturn(dataSourceMap.get(DB_NAME));
            addTestIdps();
            // Retrieving IDP from DB.
            IdentityProvider idpResult = cacheBackedIdPMgtDAO.getIdPByAuthenticatorPropertyValue(connection, property,
                    value, SUPER_TENANT_ID, SUPER_TENANT_DOMAIN);

            IdentityProvider idpFromCache = new IdentityProvider();
            if (isExist) {
                // Retrieving IDP from cache using name as cache key.
                idpFromCache = idpFromCacheByName(idpName);
            }
            if (isExist) {
                assertEquals(idpResult.getIdentityProviderName(), idpName,
                        "''getIdPByAuthenticatorPropertyValue' method fails");
                if (idpFromCache != null) {
                    assertEquals(idpFromCache.getIdentityProviderName(), idpName,
                            "''getIdPByAuthenticatorPropertyValue' method fails");
                }
            } else {
                assertNull(idpResult, "'getIdPByAuthenticatorPropertyValue' method fails");
            }
        }
    }

    @DataProvider
    public Object[][] getIdPByAuthenticatorPropertyValueData() {

        return new Object[][]{
                {"testIdP1", "Property1", "value1", "Name", true},
                {"testIdP1", "Property2", "value2", "Name", true},
                {"NotExist", "Null", "Null", "Null", false},
        };
    }

    @Test(dataProvider = "getIdPByAuthenticatorPropertyValueData")
    public void testGetIdPByAuthenticatorPropertyValue(String idpName, String property, String value,
                                                       String authenticator, boolean isExist) throws Exception {

        try (MockedStatic<IdentityDatabaseUtil> identityDatabaseUtil = mockStatic(IdentityDatabaseUtil.class);
             Connection connection = getConnection(DB_NAME)) {
            identityDatabaseUtil.when(() -> IdentityDatabaseUtil.getDBConnection(anyBoolean())).thenReturn(connection);
            identityDatabaseUtil.when(IdentityDatabaseUtil::getDataSource).thenReturn(dataSourceMap.get(DB_NAME));
            addTestIdps();
            // Retrieving IDP from DB.
            IdentityProvider idpResult = cacheBackedIdPMgtDAO.getIdPByAuthenticatorPropertyValue(connection, property,
                    value, authenticator, SUPER_TENANT_ID, SUPER_TENANT_DOMAIN);
            IdentityProvider idpFromCache = new IdentityProvider();
            if (isExist) {
                // Retrieving IDP from cache using name as cache key.
                idpFromCache = idpFromCacheByName(idpName);
            }
            if (isExist) {
                assertEquals(idpResult.getIdentityProviderName(), idpName,
                        "''getIdPByAuthenticatorPropertyValue' method fails");
                if (idpFromCache != null) {
                    assertEquals(idpFromCache.getIdentityProviderName(), idpName,
                            "''getIdPByAuthenticatorPropertyValue' method fails");
                }
                assertEquals(idpFromCache, idpResult);
            } else {
                assertNull(idpResult, "'getIdPByAuthenticatorPropertyValue' method fails");
            }
        }
    }

    @DataProvider
    public Object[][] getIdPByRealmIdData() {

        return new Object[][]{
                {"testIdP1", "1", SUPER_TENANT_ID, SUPER_TENANT_DOMAIN, true},
                {"testIdP2", "2", SUPER_TENANT_ID, SUPER_TENANT_DOMAIN, true},
                {"notExist", "99", SAMPLE_TENANT_ID1, SAMPLE_TENANT_DOMAIN1, false},
        };
    }

    @Test(dataProvider = "getIdPByRealmIdData")
    public void testGetIdPRealmId(String idpName, String realmId, int tenantId, String tenantDomain, boolean isExist)
            throws Exception {

        try (MockedStatic<IdentityDatabaseUtil> identityDatabaseUtil = mockStatic(IdentityDatabaseUtil.class);
             Connection connection = getConnection(DB_NAME)) {
            identityDatabaseUtil.when(() -> IdentityDatabaseUtil.getDBConnection(anyBoolean())).thenReturn(connection);
            identityDatabaseUtil.when(IdentityDatabaseUtil::getDataSource).thenReturn(dataSourceMap.get(DB_NAME));
            addTestIdps();
            // Retrieving IDP from DB.
            IdentityProvider idpResult = cacheBackedIdPMgtDAO.getIdPByRealmId(realmId, tenantId, tenantDomain);

            IdentityProvider idpFromCache = null;
            if (isExist) {
                // Retrieving IDP from cache using realmID as cache key.
                IdPCacheByHRI idPCacheByHRI = IdPCacheByHRI.getInstance();
                IdPHomeRealmIdCacheKey cacheKey = new IdPHomeRealmIdCacheKey(realmId);
                IdPCacheEntry entry = idPCacheByHRI.getValueFromCache(cacheKey, tenantDomain);
                idpFromCache = entry.getIdentityProvider();
            }
            if (isExist) {
                assertEquals(idpFromCache.getIdentityProviderName(), idpName,
                        "'getIdPByRealmId' method fails");
                assertEquals(idpResult, idpFromCache, "'getIdPByRealmId' method fails");
            } else {
                assertNull(idpResult, "'getIdPByRealmId' method fails");
                assertNull(idpFromCache, "'getIdPByRealmId' method fails");
            }
        }
    }

    @DataProvider
    public Object[][] getEnabledIdPByRealmIdData() {

        return new Object[][]{
                {"testIdP4", "4", SAMPLE_TENANT_ID2, SAMPLE_TENANT_DOMAIN2, true, true},
                {"testIdP5", "5", SAMPLE_TENANT_ID2, SAMPLE_TENANT_DOMAIN2, true, false},
                {"notExist", "99", SAMPLE_TENANT_ID2, SAMPLE_TENANT_DOMAIN2, false, false},
        };
    }

    @Test(dataProvider = "getEnabledIdPByRealmIdData")
    public void testGetEnabledIdPRealmId(String idpName, String realmId, int tenantId, String tenantDomain, boolean isExist, boolean isEnabled) throws Exception {

        try (MockedStatic<IdentityDatabaseUtil> identityDatabaseUtil = mockStatic(IdentityDatabaseUtil.class);
              Connection connection = getConnection(DB_NAME)) {
            identityDatabaseUtil.when(() -> IdentityDatabaseUtil.getDBConnection(anyBoolean())).thenReturn(connection);
            identityDatabaseUtil.when(IdentityDatabaseUtil::getDBConnection).thenReturn(connection);
            identityDatabaseUtil.when(IdentityDatabaseUtil::getDataSource).thenReturn(dataSourceMap.get(DB_NAME));
            addTestIdps(connection);
            // Retrieving IDP from DB.
            IdentityProvider idpResult = cacheBackedIdPMgtDAO.getEnabledIdPByRealmId(realmId, tenantId, tenantDomain);

            IdentityProvider idpFromCache = null;
            if (isExist && isEnabled) {
                // Retrieving IDP from cache using realmID as cache key.
                IdPCacheByHRI idPCacheByHRI = IdPCacheByHRI.getInstance();
                IdPHomeRealmIdCacheKey cacheKey = new IdPHomeRealmIdCacheKey(realmId);
                IdPCacheEntry entry = idPCacheByHRI.getValueFromCache(cacheKey, tenantDomain);
                idpFromCache = entry.getIdentityProvider();
            }
            if (isExist && isEnabled) {
                assertEquals(idpFromCache.getIdentityProviderName(), idpName,
                        "'getEnabledIdPByRealmId' method fails");
                assertEquals(idpResult, idpFromCache, "'getEnabledIdPByRealmId' method fails");
            } else {
                assertNull(idpResult, "'getEnabledIdPByRealmId' method fails");
                assertNull(idpFromCache, "'getEnabledIdPByRealmId' method fails");
            }
        }
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

        RoleMapping roleMapping1 = new RoleMapping(
                new LocalRole("1", "LocalRole1"), "Role1");
        RoleMapping roleMapping2 = new RoleMapping(
                new LocalRole("2", "LocalRole2"), "Role2");
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
        ClaimMapping claimMapping = ClaimMapping.build("http://wso2.org/claims/country",
                "Country", "", true);
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
                {idp1, SUPER_TENANT_ID, SUPER_TENANT_DOMAIN},
                // IDP with Local Cliam Dialect ClaimConfigs.
                {idp2, SUPER_TENANT_ID, SUPER_TENANT_DOMAIN},
                // IDP with Only name.
                {idp3, SAMPLE_TENANT_ID1, SAMPLE_TENANT_DOMAIN1},
        };
    }

    @Test(dataProvider = "addIdPData")
    public void testAddIdP(Object identityProvider, int tenantId, String tenantDomain) throws Exception {

        try (MockedStatic<IdentityDatabaseUtil> identityDatabaseUtil = mockStatic(IdentityDatabaseUtil.class);
             Connection connection = getConnection(DB_NAME)) {
            identityDatabaseUtil.when(() -> IdentityDatabaseUtil.getDBConnection(anyBoolean())).thenReturn(connection);
            identityDatabaseUtil.when(IdentityDatabaseUtil::getDataSource).thenReturn(dataSourceMap.get(DB_NAME));

            String resourceId = cacheBackedIdPMgtDAO.addIdP(((IdentityProvider) identityProvider),
                    tenantId, tenantDomain);
            // Retrieving IDP from DB and adding to cache.
            IdentityProvider idpFromDB = cacheBackedIdPMgtDAO.getIdPByResourceId(resourceId, tenantId, tenantDomain);
            assertEquals(idpFromDB.getIdentityProviderName(),
                    ((IdentityProvider) identityProvider).getIdentityProviderName());
            // Retrieving IDP from cache using resourceId as cache key.
            IdentityProvider idpFromCache = idpFromCacheByResourceId(resourceId);
            if (idpFromCache != null) {
                assertEquals(idpFromCache.getIdentityProviderName(),
                        ((IdentityProvider) identityProvider).getIdentityProviderName());;
            }
        }
    }

    @DataProvider
    public Object[][] updateIdPData() {

        // Initialize Test Identity Provider 1.
        IdentityProvider idp1 = new IdentityProvider();
        idp1.setIdentityProviderName("testIdP1");
        idp1.setHomeRealmId("1");
        idp1.setEnable(true);
        idp1.setPrimary(true);
        idp1.setFederationHub(true);
        idp1.setCertificate("");

        FederatedAuthenticatorConfig federatedAuthenticatorConfig = new FederatedAuthenticatorConfig();
        federatedAuthenticatorConfig.setDisplayName("DisplayName1");
        federatedAuthenticatorConfig.setName("Name");
        federatedAuthenticatorConfig.setEnabled(true);
        federatedAuthenticatorConfig.setDefinedByType(DefinedByType.SYSTEM);
        Property property1 = new Property();
        property1.setName("Property1");
        property1.setValue("value1");
        property1.setConfidential(true);
        federatedAuthenticatorConfig.setProperties(new Property[]{property1});
        idp1.setFederatedAuthenticatorConfigs(new FederatedAuthenticatorConfig[]{federatedAuthenticatorConfig});

        ProvisioningConnectorConfig provisioningConnectorConfig = new ProvisioningConnectorConfig();
        provisioningConnectorConfig.setName("ProvisiningConfig1");
        provisioningConnectorConfig.setProvisioningProperties(new Property[]{property1});
        idp1.setProvisioningConnectorConfigs(new ProvisioningConnectorConfig[]{provisioningConnectorConfig});

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
        Property property1New = new Property();
        property1New.setName("Property1New");
        property1New.setValue("value1New");
        property1New.setConfidential(false);
        Property property2New = new Property();
        property2New.setName("Property2New");
        property2New.setValue("value2New");
        property2New.setConfidential(false);
        newFederatedAuthenticatorConfig.setProperties(new Property[]{property1New, property2New});
        idp1New.setFederatedAuthenticatorConfigs(new FederatedAuthenticatorConfig[]{newFederatedAuthenticatorConfig});

        ProvisioningConnectorConfig newProvisioningConnectorConfig1 = new ProvisioningConnectorConfig();
        newProvisioningConnectorConfig1.setName("ProvisiningConfig1");
        newProvisioningConnectorConfig1.setProvisioningProperties(new Property[]{property1New});
        ProvisioningConnectorConfig newProvisioningConnectorConfig2 = new ProvisioningConnectorConfig();
        newProvisioningConnectorConfig2.setName("ProvisiningConfig2");
        newProvisioningConnectorConfig2.setProvisioningProperties(new Property[]{property2New});
        newProvisioningConnectorConfig2.setEnabled(true);
        newProvisioningConnectorConfig2.setBlocking(true);
        idp1New.setProvisioningConnectorConfigs(new ProvisioningConnectorConfig[]{newProvisioningConnectorConfig1,
                newProvisioningConnectorConfig2});

        JustInTimeProvisioningConfig justInTimeProvisioningConfig = new JustInTimeProvisioningConfig();
        justInTimeProvisioningConfig.setProvisioningUserStore("PRIMARY");
        idp1New.setJustInTimeProvisioningConfig(justInTimeProvisioningConfig);

        ClaimConfig newClaimConfig = new ClaimConfig();
        newClaimConfig.setLocalClaimDialect(false);
        newClaimConfig.setRoleClaimURI("Country");
        newClaimConfig.setUserClaimURI("Country");
        ClaimMapping claimMapping = ClaimMapping.build("http://wso2.org/claims/country",
                "Country", "", true);
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
                // Update PermissionsAndRoleConfig,FederatedAuthenticatorConfig,ProvisioningConnectorConfig,ClaimConfig.
                {idp1, idp1New, SUPER_TENANT_ID, SUPER_TENANT_DOMAIN},
                // Update name, LocalClaimDialect, ClaimConfig.
                {idp2, idp2New, SUPER_TENANT_ID, SUPER_TENANT_DOMAIN},
                // Update name.
                {idp3, idp3New, SAMPLE_TENANT_ID1, SAMPLE_TENANT_DOMAIN1},
        };
    }

    @Test(dataProvider = "updateIdPData")
    public void testUpdateIdP(Object oldIdP, Object newIdP, int tenantId, String tenantDomain) throws Exception {

        try (MockedStatic<IdentityUtil> identityUtil = mockStatic(IdentityUtil.class);
             MockedStatic<IdentityDatabaseUtil> identityDatabaseUtil = mockStatic(IdentityDatabaseUtil.class);
             Connection connection = getConnection(DB_NAME)) {
            identityDatabaseUtil.when(() -> IdentityDatabaseUtil.getDBConnection(anyBoolean())).thenReturn(connection);
            identityDatabaseUtil.when(() -> IdentityDatabaseUtil.getDBConnection()).thenReturn(connection);
            identityUtil.when(() -> IdentityUtil.getProperty(RESET_PROVISIONING_ENTITIES_ON_CONFIG_UPDATE))
                    .thenReturn("false");
            identityDatabaseUtil.when(() -> IdentityDatabaseUtil.getDataSource())
                    .thenReturn(dataSourceMap.get(DB_NAME));
            addTestIdps();
            cacheBackedIdPMgtDAO.updateIdP((IdentityProvider) newIdP, (IdentityProvider) oldIdP,
                    tenantId, tenantDomain);

            String newIdpName = ((IdentityProvider)  newIdP).getIdentityProviderName();
            // Retrieving updated IDP from DB and adding to cache.
            IdentityProvider idpResult = cacheBackedIdPMgtDAO.getIdPByName(connection, newIdpName,
                    tenantId, tenantDomain);
            assertEquals(idpResult.getIdentityProviderName(), newIdpName);
            // Retrieving IDP from cache using name as cache key.
            IdentityProvider idpFromCache = idpFromCacheByName(newIdpName);
            if (idpFromCache != null) {
                assertEquals(idpFromCache.getIdentityProviderName(), newIdpName);
            }
        }
    }

    @Test(dataProvider = "updateIdPData")
    public void testGetUpdatedIdPByResourceId(Object oldIdP, Object newIdP, int tenantId, String tenantDomain)
            throws Exception {

        try (MockedStatic<IdentityUtil> identityUtil = mockStatic(IdentityUtil.class);
             MockedStatic<IdentityDatabaseUtil> identityDatabaseUtil = mockStatic(IdentityDatabaseUtil.class);
             Connection connection = getConnection(DB_NAME)) {
            identityDatabaseUtil.when(() -> IdentityDatabaseUtil.getDBConnection(anyBoolean())).thenReturn(connection);
            identityDatabaseUtil.when(IdentityDatabaseUtil::getDBConnection).thenReturn(connection);
            identityUtil.when(() -> IdentityUtil.getProperty(RESET_PROVISIONING_ENTITIES_ON_CONFIG_UPDATE)).thenReturn("false");
            identityDatabaseUtil.when(IdentityDatabaseUtil::getDataSource).thenReturn(dataSourceMap.get(DB_NAME));
            addTestIdps();
            String oldIdPName = ((IdentityProvider) oldIdP).getIdentityProviderName();
            // Retrieving IDP from DB.
            IdentityProvider idpFromDB = idPManagementDAO.getIdPByName(connection, oldIdPName, tenantId,
                    tenantDomain);
            assertEquals(idpFromDB.getIdentityProviderName(), oldIdPName);

            String resourceId = idpFromDB.getResourceId();
            // Retrieving IDP from DB and adding cache using resourceId.
            IdentityProvider idpFromDBByResourceId = cacheBackedIdPMgtDAO.getIdPByResourceId(resourceId,
                    tenantId, tenantDomain);
            // Retrieving IDP from cache.
            IdentityProvider idpFromCache = idpFromCacheByResourceId(resourceId);
            if (idpFromCache != null) {
                assertEquals(idpFromCache.getIdentityProviderName(), oldIdPName);
            }
            // updating IDP.
            cacheBackedIdPMgtDAO.updateIdP((IdentityProvider) newIdP, (IdentityProvider) oldIdP,
                    tenantId, tenantDomain);
            String newIdPName = ((IdentityProvider) newIdP).getIdentityProviderName();
            // Retrieving updated IDP from DB.
            IdentityProvider updatedIDPFromDB = idPManagementDAO.getIdPByName(connection, newIdPName,
                    tenantId, tenantDomain);
            assertEquals(updatedIDPFromDB.getIdentityProviderName(), newIdPName);

            String updatedIDPResourceId = updatedIDPFromDB.getResourceId();
            // Retrieving updated IDP from DB and adding cache using resourceId.
            IdentityProvider updatedIdpByResourceId = cacheBackedIdPMgtDAO.getUpdatedIdPByResourceId(
                    updatedIDPResourceId, tenantId, tenantDomain);
            // Retrieving updated IDP from cache.
            IdentityProvider updatedIDPFromCache = idpFromCacheByResourceId(resourceId);
            if (updatedIDPFromCache != null) {
                assertEquals(updatedIdpByResourceId.getIdentityProviderName(),
                        updatedIdpByResourceId.getIdentityProviderName());
            }
        }
    }

    @Test(dataProvider = "updateIdPData")
    public void testUpdateIdPException(Object oldIdp, Object newIdp, int tenantId, String tenantDomain)
            throws Exception {


        try (MockedStatic<IdentityUtil> identityUtil = mockStatic(IdentityUtil.class);
             MockedStatic<IdentityDatabaseUtil> identityDatabaseUtil = mockStatic(IdentityDatabaseUtil.class);
             Connection connection = getConnection(DB_NAME)) {
            identityDatabaseUtil.when(() -> IdentityDatabaseUtil.getDBConnection(anyBoolean())).thenReturn(connection);
            identityDatabaseUtil.when(IdentityDatabaseUtil::getDBConnection).thenReturn(connection);
            identityUtil.when(() -> IdentityUtil.getProperty(RESET_PROVISIONING_ENTITIES_ON_CONFIG_UPDATE)).thenReturn("false");

            assertThrows(IdentityProviderManagementException.class, () ->
                    cacheBackedIdPMgtDAO.updateIdP((IdentityProvider) newIdp, ((IdentityProvider) oldIdp), tenantId,
                            tenantDomain));
        }
    }

    @DataProvider
    public Object[][] deleteIdPData() {

        return new Object[][]{
                {"testIdP1", SUPER_TENANT_ID, SUPER_TENANT_DOMAIN},
                {"testIdP3", SAMPLE_TENANT_ID1, SAMPLE_TENANT_DOMAIN1},
        };
    }

    @Test(dataProvider = "deleteIdPData")
    public void testDeleteIdP(String idpName, int tenantId, String tenantDomain) throws Exception {

        try (MockedStatic<IdentityDatabaseUtil> identityDatabaseUtil = mockStatic(IdentityDatabaseUtil.class);
             Connection connection = getConnection(DB_NAME)) {
            identityDatabaseUtil.when(() -> IdentityDatabaseUtil.getDBConnection(anyBoolean())).thenReturn(connection);
            identityDatabaseUtil.when(IdentityDatabaseUtil::getDBConnection).thenReturn(connection);
            identityDatabaseUtil.when(IdentityDatabaseUtil::getDataSource).thenReturn(dataSourceMap.get(DB_NAME));
            addTestIdps();
            // Retrieving IDP from DB and adding to cache.
            IdentityProvider idpResult = cacheBackedIdPMgtDAO.getIdPByName(connection, idpName,
                    tenantId, tenantDomain);
            // Retrieving IDP from cache.
            IdentityProvider idpFromCache = idpFromCacheByName(idpName);
            if (idpFromCache != null) {
                assertEquals(idpFromCache.getIdentityProviderName(), idpName);
            }
            // Deleting IDP.
            cacheBackedIdPMgtDAO.deleteIdP(idpName, tenantId, tenantDomain);
            int resultSize = getIdPCount(connection, idpName, tenantId);
            assertEquals(resultSize, 0, "'deleteIdP' method fails");
            IdentityProvider idpFromCacheAfterDeletion = idpFromCacheByName(idpName);
            assertNull(idpFromCacheAfterDeletion);
        }
    }

    @DataProvider
    public Object[][] deleteIdPsData() {

        return new Object[][]{
                {SUPER_TENANT_ID, SUPER_TENANT_DOMAIN},
                {SAMPLE_TENANT_ID1, SAMPLE_TENANT_DOMAIN1},
        };
    }

    @Test(dataProvider = "deleteIdPsData")
    public void testDeleteIdPs(int tenantId, String tenantDomain) throws Exception {

        try (MockedStatic<IdentityDatabaseUtil> identityDatabaseUtil = mockStatic(IdentityDatabaseUtil.class);
             Connection connection = getConnection(DB_NAME)) {
            identityDatabaseUtil.when(() -> IdentityDatabaseUtil.getDBConnection(anyBoolean())).thenReturn(connection);
            identityDatabaseUtil.when(IdentityDatabaseUtil::getDBConnection).thenReturn(connection);
            identityDatabaseUtil.when(IdentityDatabaseUtil::getDataSource).thenReturn(dataSourceMap.get(DB_NAME));
            addTestIdps();
            // Deleting multiple IDPs on a tenant.
            cacheBackedIdPMgtDAO.deleteIdPs(tenantId);
        }

        try (Connection connection = getConnection(DB_NAME)) {
            String query = IdPManagementConstants.SQLQueries.GET_IDPS_SQL;
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setInt(1, tenantId);
            statement.setInt(2, MultitenantConstants.SUPER_TENANT_ID);
            ResultSet resultSet = statement.executeQuery();
            int resultSize = 0;
            if (resultSet.next()) {
                resultSize = resultSet.getRow();
            }
            statement.close();
            assertEquals(resultSize, 0);
        }
    }

    @Test
    public void testDeleteIdPsDAOException() throws Exception {

        try (MockedStatic<IdentityDatabaseUtil> identityDatabaseUtil = mockStatic(IdentityDatabaseUtil.class);
             Connection connection = getConnection(DB_NAME)) {
            identityDatabaseUtil.when(() -> IdentityDatabaseUtil.getDBConnection(anyBoolean())).thenReturn(connection);
            identityDatabaseUtil.when(IdentityDatabaseUtil::getDBConnection).thenReturn(connection);
            identityDatabaseUtil.when(IdentityDatabaseUtil::getDataSource).thenReturn(dataSourceMap.get(DB_NAME));
            addTestIdps();

            List<IdentityProvider> idpList = idPManagementDAO.getIdPs(connection, SUPER_TENANT_ID, SUPER_TENANT_DOMAIN);
            when(idPManagementDAOForException.getIdPs(any(), anyInt(), anyString()))
                    .thenReturn(idpList);
            doThrow(IdentityProviderManagementException.class).when(idPManagementDAOForException).deleteIdPs(anyInt());

            // Deleting multiple IDPs on a tenant.
            assertThrows(IdentityProviderManagementException.class, () ->
                    cacheBackedIdPMgtDAOForException.deleteIdPs(SUPER_TENANT_ID));
            verify(actionManagementService, never()).addAction(anyString(), any(), anyString());
        }
    }

    @Test(dataProvider = "deleteIdPData")
    public void testDeleteIdPByResourceId(String idpName, int tenantId, String tenantDomain) throws Exception {

        try (MockedStatic<IdentityDatabaseUtil> identityDatabaseUtil = mockStatic(IdentityDatabaseUtil.class);
             Connection connection = getConnection(DB_NAME)) {
            identityDatabaseUtil.when(()->IdentityDatabaseUtil.getDBConnection(anyBoolean())).thenReturn(connection);
            identityDatabaseUtil.when(IdentityDatabaseUtil::getDBConnection).thenReturn(connection);
            identityDatabaseUtil.when(IdentityDatabaseUtil::getDataSource).thenReturn(dataSourceMap.get(DB_NAME));
            addTestIdps();
            // ResourceId of IDP retrieved from DB.
            String uuid = cacheBackedIdPMgtDAO.getIdPByName(connection, idpName, tenantId, tenantDomain)
                    .getResourceId();
            // Delete IDP using resourceId.
            cacheBackedIdPMgtDAO.deleteIdPByResourceId(uuid, tenantId, tenantDomain);
            int resultSize = getIdPCount(connection, idpName, tenantId);
            assertEquals(resultSize, 0, "'deleteIdPByResourceId' method fails");
            IdentityProvider idpFromCache = idpFromCacheByResourceId(uuid);
            assertNull(idpFromCache, "'deleteIdPByResourceId' method fails");
        }
    }

    @Test(dataProvider = "deleteIdPData")
    public void testForceDeleteIdPByResourceId(String idpName, int tenantId, String tenantDomain) throws Exception {

        try (MockedStatic<IdentityDatabaseUtil> identityDatabaseUtil = mockStatic(IdentityDatabaseUtil.class);
             Connection connection = getConnection(DB_NAME)) {
            identityDatabaseUtil.when(() -> IdentityDatabaseUtil.getDBConnection(anyBoolean())).thenReturn(connection);
            identityDatabaseUtil.when(IdentityDatabaseUtil::getDBConnection).thenReturn(connection);
            identityDatabaseUtil.when(IdentityDatabaseUtil::getDataSource).thenReturn(dataSourceMap.get(DB_NAME));
            addTestIdps();
            // ResourceId of IDP retrieved from DB.
            String uuid = cacheBackedIdPMgtDAO.getIdPByName(connection, idpName, tenantId, tenantDomain)
                    .getResourceId();
            // Force delete IDP using resourceId.
            cacheBackedIdPMgtDAO.forceDeleteIdPByResourceId(uuid, tenantId, tenantDomain);
            int resultSize = getIdPCount(connection, idpName, tenantId);
            assertEquals(resultSize, 0, "'forceDeleteIdPByResourceId' method fails");
            IdentityProvider idpFromCache = idpFromCacheByResourceId(uuid);
            assertNull(idpFromCache, "'deleteIdPByResourceId' method fails");
        }
    }

    @Test(dataProvider = "deleteIdPData")
    public void testForceDeleteIdP(String idpName, int tenantId, String tenantDomain) throws Exception {

        try (MockedStatic<IdentityDatabaseUtil> identityDatabaseUtil = mockStatic(IdentityDatabaseUtil.class);
             Connection connection = getConnection(DB_NAME)) {
            identityDatabaseUtil.when(() -> IdentityDatabaseUtil.getDBConnection(anyBoolean())).thenReturn(connection);
            identityDatabaseUtil.when(IdentityDatabaseUtil::getDBConnection).thenReturn(connection);
            identityDatabaseUtil.when(IdentityDatabaseUtil::getDataSource).thenReturn(dataSourceMap.get(DB_NAME));
            addTestIdps();

            // Force delete IDP using resourceId.
            cacheBackedIdPMgtDAO.forceDeleteIdP(idpName, tenantId, tenantDomain);
            int resultSize = getIdPCount(connection, idpName, tenantId);
            assertEquals(resultSize, 0, "'forceDeleteIdP' method fails");
            IdentityProvider idpFromCache = idpFromCacheByName(idpName);
            assertNull(idpFromCache, "'deleteIdPByResourceId' method fails");
        }
    }

    @Test
    public void testForceDeleteIdPDAOException() throws Exception {

        try (MockedStatic<IdentityDatabaseUtil> identityDatabaseUtil = mockStatic(IdentityDatabaseUtil.class);
             Connection connection = getConnection(DB_NAME)) {
            identityDatabaseUtil.when(() -> IdentityDatabaseUtil.getDBConnection(anyBoolean())).thenReturn(connection);
            identityDatabaseUtil.when(IdentityDatabaseUtil::getDBConnection).thenReturn(connection);
            identityDatabaseUtil.when(IdentityDatabaseUtil::getDataSource).thenReturn(dataSourceMap.get(DB_NAME));
            addTestIdps();

            doThrow(IdentityProviderManagementException.class).when(idPManagementDAOForException).forceDeleteIdP(
                    anyString(), anyInt(), anyString());
            when(idPManagementDAOForException.getIdPByName(any(), anyString(), anyInt(), anyString()))
                    .thenReturn(userDefinedIdP);

            // Deleting multiple IDPs on a tenant.
            assertThrows(IdentityProviderManagementException.class, () ->
                    cacheBackedIdPMgtDAOForException.forceDeleteIdP(
                            userDefinedIdP.getIdentityProviderName(), SUPER_TENANT_ID, SUPER_TENANT_DOMAIN));

            verify(actionManagementService, never()).addAction(anyString(), any(), anyString());
        }
    }

    @Test
    public void testDeleteIdPDAOException() throws Exception {

        try (MockedStatic<IdentityDatabaseUtil> identityDatabaseUtil = mockStatic(IdentityDatabaseUtil.class);
             Connection connection = getConnection(DB_NAME)) {
            identityDatabaseUtil.when(() -> IdentityDatabaseUtil.getDBConnection(anyBoolean())).thenReturn(connection);
            identityDatabaseUtil.when(IdentityDatabaseUtil::getDBConnection).thenReturn(connection);
            identityDatabaseUtil.when(IdentityDatabaseUtil::getDataSource).thenReturn(dataSourceMap.get(DB_NAME));
            addTestIdps();

            doThrow(IdentityProviderManagementException.class).when(idPManagementDAOForException).deleteIdP(
                    anyString(), anyInt(), anyString());
            when(idPManagementDAOForException.getIdPByName(any(), anyString(), anyInt(), anyString()))
                    .thenReturn(userDefinedIdP);

            // Deleting multiple IDPs on a tenant.
            assertThrows(IdentityProviderManagementException.class, () ->
                    cacheBackedIdPMgtDAOForException.deleteIdP(
                            userDefinedIdP.getIdentityProviderName(), SUPER_TENANT_ID, SUPER_TENANT_DOMAIN));

            verify(actionManagementService, never()).addAction(anyString(), any(), anyString());
        }
    }

    @DataProvider
    public Object[][] addIdPCacheData() {

        return new Object[][]{
                {"testIdP1", SUPER_TENANT_ID, SUPER_TENANT_DOMAIN, true},
                {"testIdP3", SAMPLE_TENANT_ID1, SAMPLE_TENANT_DOMAIN1, true},
                {"notExist", SUPER_TENANT_ID, SUPER_TENANT_DOMAIN, false},
        };
    }

    @Test(dataProvider = "addIdPCacheData")
    public void testAddIdPCache(String idpName, int tenantId, String tenantDomain, boolean isExist) throws Exception{

        try (MockedStatic<IdentityDatabaseUtil> identityDatabaseUtil = mockStatic(IdentityDatabaseUtil.class);
             Connection connection = getConnection(DB_NAME)) {
            identityDatabaseUtil.when(() -> IdentityDatabaseUtil.getDBConnection(anyBoolean())).thenReturn(connection);
            identityDatabaseUtil.when(IdentityDatabaseUtil::getDBConnection).thenReturn(connection);
            identityDatabaseUtil.when(IdentityDatabaseUtil::getDataSource).thenReturn(dataSourceMap.get(DB_NAME));
            addTestIdps();
            // Retrieving IDP from DB.
            IdentityProvider idpResult = idPManagementDAO.getIdPByName(connection, idpName, tenantId,
                    tenantDomain);
            if (isExist) {
                // Adding IDP to cache.
                cacheBackedIdPMgtDAO.addIdPCache(idpResult, tenantDomain);
                // Retrieving IDP from cache using name as cache key.
                IdentityProvider idpCacheResult = idpFromCacheByName(idpName);
                if (idpCacheResult != null) {
                    assertEquals(idpCacheResult.getIdentityProviderName(), idpName,
                            "'addIdpCache' method failed!");
                }
            }
        }
    }

    @DataProvider
    public Object[][] clearIdPCacheData() {

        return new Object[][]{
                {"testIdP1", SUPER_TENANT_ID, SUPER_TENANT_DOMAIN, true},
                {"testIdP3", SAMPLE_TENANT_ID1, SAMPLE_TENANT_DOMAIN1, true},
                {"notExist", SUPER_TENANT_ID, SUPER_TENANT_DOMAIN, false},
        };
    }

    @Test(dataProvider = "clearIdPCacheData")
    public void testClearIdpCache(String idpName, int tenantId, String tenantDomain, boolean isExist) throws Exception {

        try (MockedStatic<IdentityDatabaseUtil> identityDatabaseUtil = mockStatic(IdentityDatabaseUtil.class);
             Connection connection = getConnection(DB_NAME)) {
            identityDatabaseUtil.when(() -> IdentityDatabaseUtil.getDBConnection(anyBoolean())).thenReturn(connection);
            identityDatabaseUtil.when(IdentityDatabaseUtil::getDBConnection).thenReturn(connection);
            identityDatabaseUtil.when(IdentityDatabaseUtil::getDataSource).thenReturn(dataSourceMap.get(DB_NAME));
            addTestIdps();
            // Retrieving IDP from DB and adding to cache.
            IdentityProvider idpResult = cacheBackedIdPMgtDAO.getIdPByName(connection, idpName,
                    tenantId, tenantDomain);
            if (idpResult != null) {
                // Clear cache entry of IDP.
                cacheBackedIdPMgtDAO.clearIdpCache(idpName, tenantId, tenantDomain);
                IdentityProvider idpCacheResult = idpFromCacheByName(idpName);
                assertNull(idpCacheResult, "'clearIdpCache' method failed!");
            }
        }
    }

    @DataProvider
    public Object[][] deleteTenantRoleData() {

        return new Object[][]{
                {"Role1"},
                {"Role2"},
        };
    }

    @Test(dataProvider = "deleteTenantRoleData")
    public void testDeleteTenantRole(String role) throws Exception {

        try (MockedStatic<IdentityDatabaseUtil> identityDatabaseUtil = mockStatic(IdentityDatabaseUtil.class);
             Connection connection = getConnection(DB_NAME)) {
            identityDatabaseUtil.when(() -> IdentityDatabaseUtil.getDBConnection(anyBoolean())).thenReturn(connection);
            identityDatabaseUtil.when(IdentityDatabaseUtil::getDBConnection).thenReturn(connection);
            identityDatabaseUtil.when(IdentityDatabaseUtil::getDataSource).thenReturn(dataSourceMap.get(DB_NAME));
            addTestIdps();
            // Deleting tenant role.
            cacheBackedIdPMgtDAO.deleteTenantRole(SUPER_TENANT_ID, role, SUPER_TENANT_DOMAIN);
            // Retrieving list of IDPs on a tenant.
            List<IdentityProvider> idpList = cacheBackedIdPMgtDAO.getIdPs(connection, SUPER_TENANT_ID,
                    SUPER_TENANT_DOMAIN);
            // Validating IDPs are removed from cache.
            for (IdentityProvider idp : idpList) {
                IdentityProvider idpFromCache = idpFromCacheByName(idp.getIdentityProviderName());
                assertNull(idpFromCache, "Idp not removed from cache!");
            }
        }

        try (Connection connection = getConnection(DB_NAME)) {
            String query = "SELECT * FROM IDP_ROLE WHERE TENANT_ID=? AND ROLE=?";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setInt(1, SUPER_TENANT_ID);
            statement.setString(2, role);
            ResultSet resultSet = statement.executeQuery();
            int resultSize = 0;
            if (resultSet.next()) {
                resultSize = resultSet.getRow();
            }
            statement.close();
            assertEquals(resultSize, 0);
        }
    }

    @DataProvider
    public Object[][] renameTenantRoleData() {

        return new Object[][]{
                {"NewLocalRole1", "LocalRole1", 1},
                {"NewLocalRole2", "LocalRole2", 1},
                {"NewLocalRole2", "2", 0},
        };
    }

    @Test(dataProvider = "renameTenantRoleData")
    public void testRenameTenantRoleData(String newRole, String oldRole, int count) throws Exception {

        try (MockedStatic<IdentityDatabaseUtil> identityDatabaseUtil = mockStatic(IdentityDatabaseUtil.class);
             Connection connection = getConnection(DB_NAME)) {
            identityDatabaseUtil.when(() -> IdentityDatabaseUtil.getDBConnection(anyBoolean())).thenReturn(connection);
            identityDatabaseUtil.when(IdentityDatabaseUtil::getDBConnection).thenReturn(connection);
            identityDatabaseUtil.when(IdentityDatabaseUtil::getDataSource).thenReturn(dataSourceMap.get(DB_NAME));
            addTestIdps();
            // Renaming tenant role.
            cacheBackedIdPMgtDAO.renameTenantRole(newRole, oldRole, SUPER_TENANT_ID, SUPER_TENANT_DOMAIN);
            // Retrieving list of IDPs on a tenant.
            List<IdentityProvider> idpList = cacheBackedIdPMgtDAO.getIdPs(connection, SUPER_TENANT_ID,
                    SUPER_TENANT_DOMAIN);
            // validating IDPs are removed from cache.
            for (IdentityProvider idp : idpList) {
                IdentityProvider idpFromCache = idpFromCacheByName(idp.getIdentityProviderName());
                assertNull(idpFromCache, "Idp not removed from cache!");
            }

            String query = "SELECT * FROM IDP_ROLE_MAPPING WHERE TENANT_ID=? AND LOCAL_ROLE=?";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setInt(1, SUPER_TENANT_ID);
            statement.setString(2, newRole);
            ResultSet resultSet = statement.executeQuery();
            int resultSize = 0;
            if (resultSet.next()) {
                resultSize = resultSet.getRow();
            }
            statement.close();
            assertEquals(resultSize, count);
        }
    }

    @DataProvider
    public Object[][] isIdPAvailableForAuthenticatorPropertyData() {

        return new Object[][]{
                {"Name", "Property1", "value1", true},
                {"Address", "Property2", "value2", false},
        };
    }

    @Test(dataProvider = "isIdPAvailableForAuthenticatorPropertyData")
    public void testIsIdPAvailableForAuthenticatorProperty(String authenticator, String property,
                                                           String value, boolean isAvailable) throws Exception {

        try (MockedStatic<IdentityDatabaseUtil> identityDatabaseUtil = mockStatic(IdentityDatabaseUtil.class);
             Connection connection = getConnection(DB_NAME)) {
            identityDatabaseUtil.when(() -> IdentityDatabaseUtil.getDBConnection(anyBoolean())).thenReturn(connection);
            identityDatabaseUtil.when(IdentityDatabaseUtil::getDBConnection).thenReturn(connection);
            identityDatabaseUtil.when(IdentityDatabaseUtil::getDataSource).thenReturn(dataSourceMap.get(DB_NAME));
            addTestIdps();
            boolean availabilityResult = cacheBackedIdPMgtDAO.isIdPAvailableForAuthenticatorProperty(authenticator,
                    property, value, SUPER_TENANT_ID);
            assertEquals(availabilityResult, isAvailable);
        }
    }

    @Test(dataProvider = "isIdPAvailableForAuthenticatorPropertyData")
    public void testIsIdPAvailableForAuthenticatorPropertyException(String authenticator, String property,
                                                                    String value, boolean isAvailable)
                                                                    throws Exception {

        try (MockedStatic<IdentityDatabaseUtil> identityDatabaseUtil = mockStatic(IdentityDatabaseUtil.class)) {

            try (Connection connection = getConnection(DB_NAME)) {
                identityDatabaseUtil.when(() -> IdentityDatabaseUtil.getDBConnection(anyBoolean()))
                        .thenReturn(connection);
                identityDatabaseUtil.when(IdentityDatabaseUtil::getDBConnection).thenReturn(connection);
                identityDatabaseUtil.when(IdentityDatabaseUtil::getDataSource).thenReturn(dataSourceMap.get(DB_NAME));
                addTestIdps();
            }
            assertThrows(IdentityProviderManagementException.class, () ->
                    cacheBackedIdPMgtDAO.isIdPAvailableForAuthenticatorProperty(authenticator, property, value,
                            SUPER_TENANT_ID));
        }
    }

    @DataProvider
    public Object[][] getConnectedApplicationsData() {

        return new Object[][]{
                {"testIdP1", 2, 2, 0},
                {"testIdP2", 1, 1, 0},
        };
    }

    @Test(dataProvider = "getConnectedApplicationsData")
    public void testGetConnectedApplications(String idpName, int limit, int offset, int count)
            throws  Exception{

        try (MockedStatic<IdentityDatabaseUtil> identityDatabaseUtil = mockStatic(IdentityDatabaseUtil.class);
             Connection connection = getConnection(DB_NAME)) {
            identityDatabaseUtil.when(() -> IdentityDatabaseUtil.getDBConnection(anyBoolean())).thenReturn(connection);
            identityDatabaseUtil.when(IdentityDatabaseUtil::getDBConnection).thenReturn(connection);
            identityDatabaseUtil.when(IdentityDatabaseUtil::getDataSource).thenReturn(dataSourceMap.get(DB_NAME));
            addTestIdps();
            // Retrieving IDP from DB.
            IdentityProvider idp = cacheBackedIdPMgtDAO.getIdPByName(connection, idpName, SUPER_TENANT_ID,
                    SUPER_TENANT_DOMAIN);
            String uuid = idp.getResourceId();
            // Retrieving connected IDPs.
            ConnectedAppsResult result = cacheBackedIdPMgtDAO.getConnectedApplications(uuid, limit, offset);
            assertEquals(result.getTotalAppCount(), count);
        }
    }

    @DataProvider
    public Object[][] getIdPNameByMetadataPropertyData() {

        return new Object[][]{
                {"testIdP1", "idpPropertyName", "idpPropertyValue", SUPER_TENANT_ID, SUPER_TENANT_DOMAIN},
                {null, "notExist", "4", SAMPLE_TENANT_ID1, SAMPLE_TENANT_DOMAIN1},
        };
    }

    @Test(dataProvider = "getIdPNameByMetadataPropertyData")
    public void testGetIdPNameByMetadataProperty(String idpName, String property, String value, int tenantId,
                                                 String tenantDomain) throws Exception {

        try (MockedStatic<IdentityDatabaseUtil> identityDatabaseUtil = mockStatic(IdentityDatabaseUtil.class);
             Connection connection = getConnection(DB_NAME)) {
            identityDatabaseUtil.when(() -> IdentityDatabaseUtil.getDBConnection(anyBoolean())).thenReturn(connection);
            identityDatabaseUtil.when(IdentityDatabaseUtil::getDBConnection).thenReturn(connection);
            identityDatabaseUtil.when(IdentityDatabaseUtil::getDataSource).thenReturn(dataSourceMap.get(DB_NAME));
            addTestIdps();
            // Retrieving IDP from DB and adding to cache using Metadata property.
            String outputName = cacheBackedIdPMgtDAO.getIdPNameByMetadataProperty(connection, property, value, tenantId,
                    tenantDomain);
            assertEquals(outputName, idpName, "'getIdPNameByMetadataProperty' method failed!");
            // Retrieving IDP from cache using metadata property.
            IdPCacheByMetadataProperty idPCacheByMetadataProperty = IdPCacheByMetadataProperty.getInstance();
            IdPMetadataPropertyCacheKey cacheKey = new IdPMetadataPropertyCacheKey(property, value);
            String idPNameCache = idPCacheByMetadataProperty.getValueFromCache(cacheKey, tenantDomain);
            assertEquals(idPNameCache, idpName, "Cannot find idP in cache!");
        }
    }

    @Test
    public void testDeleteIdPActionException() throws Exception {

        try (MockedStatic<IdentityDatabaseUtil> identityDatabaseUtil = mockStatic(IdentityDatabaseUtil.class);
             Connection connection = getConnection(DB_NAME)) {
            identityDatabaseUtil.when(() -> IdentityDatabaseUtil.getDBConnection(anyBoolean())).thenReturn(connection);
            identityDatabaseUtil.when(IdentityDatabaseUtil::getDBConnection).thenReturn(connection);
            identityDatabaseUtil.when(IdentityDatabaseUtil::getDataSource).thenReturn(dataSourceMap.get(DB_NAME));
            addTestIdps();

            Log mockedLog = mockLogger();
            setActionServiceForException();

            cacheBackedIdPMgtDAO.deleteIdP(userDefinedIdP.getIdentityProviderName(), SUPER_TENANT_ID,
                    SUPER_TENANT_DOMAIN);
            Assert.assertNull(cacheBackedIdPMgtDAO.getIdPByName(null,
                    userDefinedIdP.getIdentityProviderName(), SUPER_TENANT_ID, SUPER_TENANT_DOMAIN));
            Mockito.verify(mockedLog).warn(Mockito.any());
        }
    }

    @Test
    public void testForceDeleteIdPActionException() throws Exception {

        try (MockedStatic<IdentityDatabaseUtil> identityDatabaseUtil = mockStatic(IdentityDatabaseUtil.class);
             Connection connection = getConnection(DB_NAME)) {
            identityDatabaseUtil.when(() -> IdentityDatabaseUtil.getDBConnection(anyBoolean())).thenReturn(connection);
            identityDatabaseUtil.when(IdentityDatabaseUtil::getDBConnection).thenReturn(connection);
            identityDatabaseUtil.when(IdentityDatabaseUtil::getDataSource).thenReturn(dataSourceMap.get(DB_NAME));
            addTestIdps();

            Log mockedLog = mockLogger();
            setActionServiceForException();

            cacheBackedIdPMgtDAO.forceDeleteIdP(
                    userDefinedIdP.getIdentityProviderName(), SUPER_TENANT_ID, SUPER_TENANT_DOMAIN);
            Assert.assertNull(cacheBackedIdPMgtDAO.getIdPByName(null,
                    userDefinedIdP.getIdentityProviderName(), SUPER_TENANT_ID, SUPER_TENANT_DOMAIN));
            Mockito.verify(mockedLog).warn(Mockito.any());
        }
    }

    @Test
    public void testDeleteIdPByResourceIdActionException() throws Exception {

        try (MockedStatic<IdentityDatabaseUtil> identityDatabaseUtil = mockStatic(IdentityDatabaseUtil.class);
             Connection connection = getConnection(DB_NAME)) {
            identityDatabaseUtil.when(() -> IdentityDatabaseUtil.getDBConnection(anyBoolean())).thenReturn(connection);
            identityDatabaseUtil.when(IdentityDatabaseUtil::getDBConnection).thenReturn(connection);
            identityDatabaseUtil.when(IdentityDatabaseUtil::getDataSource).thenReturn(dataSourceMap.get(DB_NAME));
            addTestIdps();

            Log mockedLog = mockLogger();
            setActionServiceForException();

            cacheBackedIdPMgtDAO.deleteIdPByResourceId(
                    userDefinedIdP.getResourceId(), SUPER_TENANT_ID, SUPER_TENANT_DOMAIN);
            Assert.assertNull(cacheBackedIdPMgtDAO.getIdPByName(null,
                    userDefinedIdP.getIdentityProviderName(), SUPER_TENANT_ID, SUPER_TENANT_DOMAIN));
            Mockito.verify(mockedLog).warn(Mockito.any());
        }
    }

    /**
     * Mock logger using reflection (compatible with Java 12+).
     * Uses Unsafe API to modify static final fields.
     *
     * @return The mocked Log instance.
     * @throws Exception If an error occurs.
     */
    private Log mockLogger() throws Exception {

        Log mockedLog = Mockito.mock(Log.class);
        Field logField = IdPManagementFacade.class.getDeclaredField("LOG");
        logField.setAccessible(true);

        // Use Unsafe to modify static final fields in Java 12+
        Field unsafeField = sun.misc.Unsafe.class.getDeclaredField("theUnsafe");
        unsafeField.setAccessible(true);
        sun.misc.Unsafe unsafe = (sun.misc.Unsafe) unsafeField.get(null);

        Object fieldBase = unsafe.staticFieldBase(logField);
        long fieldOffset = unsafe.staticFieldOffset(logField);
        unsafe.putObject(fieldBase, fieldOffset, mockedLog);

        return mockedLog;
    }

    private void setActionServiceForException() {

        IdpMgtServiceComponentHolder.getInstance().setActionManagementService(actionManagementServiceForException);
    }

    private void addTestIdps() throws IdentityProviderManagementException {

        // Initialize Test Identity Provider 1.
        IdentityProvider idp1 = new IdentityProvider();
        idp1.setIdentityProviderName("testIdP1");
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
        ClaimMapping claimMapping = ClaimMapping.build("http://wso2.org/claims/country",
                "Country", "", true);
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
        idPManagementDAO.addIdP(idp1, SUPER_TENANT_ID);
        // IDP with Local Cliam Dialect ClaimConfigs.
        idPManagementDAO.addIdP(idp2, SUPER_TENANT_ID);
        // IDP with Only name.
        idPManagementDAO.addIdP(idp3, SAMPLE_TENANT_ID1);
        // IDP with user defined federated authenticators.
        idPManagementDAO.addIdP(userDefinedIdP, SUPER_TENANT_ID);
        userDefinedIdP =  idPManagementDAO.getIdPByName(null, userDefinedIdP.getIdentityProviderName(),
                SUPER_TENANT_ID, SUPER_TENANT_DOMAIN);
    }

    private void addTestIdps(Connection connection) throws IdentityProviderManagementException {
        // Initialize Test Identity Provider 4.
        IdentityProvider idp4 = new IdentityProvider();
        idp4.setIdentityProviderName("testIdP4");
        idp4.setHomeRealmId("4");

        // Initialize Test Identity Provider 5.
        IdentityProvider idp5 = new IdentityProvider();
        idp5.setIdentityProviderName("testIdP5");
        idp5.setHomeRealmId("5");

        // IDP with Only name.
        idPManagementDAO.addIdP(idp4, SAMPLE_TENANT_ID2);
        // Disabled IDP.
        idPManagementDAO.addIdP(idp5, SAMPLE_TENANT_ID2);
        IdentityProvider tempIdP = idPManagementDAO.getIdPByName(connection, "testIdP5", SAMPLE_TENANT_ID2,
                SUPER_TENANT_DOMAIN);
        tempIdP.setEnable(false);
        idPManagementDAO.updateIdP(tempIdP, idp5, SAMPLE_TENANT_ID2);
    }

    private int getIdPCount(Connection connection, String idpName, int tenantId) throws SQLException {

        String query = IdPManagementConstants.SQLQueries.GET_IDP_BY_NAME_SQL;
        PreparedStatement statement = connection.prepareStatement(query);
        statement.setInt(1, tenantId);
        statement.setInt(2, MultitenantConstants.SUPER_TENANT_ID);
        statement.setString(3, (idpName));
        ResultSet resultSet = statement.executeQuery();
        int resultSize = 0;
        if (resultSet.next()) {
            resultSize = resultSet.getRow();
        }
        statement.clearParameters();
        statement.close();
        return resultSize;
    }

    private IdentityProvider idpFromCacheByName(String idpName) {

        IdPCacheByName idPCacheByName = IdPCacheByName.getInstance();
        IdPNameCacheKey cacheKey = new IdPNameCacheKey(idpName);
        IdPCacheEntry entry = idPCacheByName.getValueFromCache(cacheKey, SUPER_TENANT_DOMAIN);
        if (entry != null) {
            return entry.getIdentityProvider();
        } else {
            return null;
        }
    }

    private IdentityProvider idpFromCacheByResourceId(String resourceId) {

        IdPCacheByResourceId idPCacheByResourceId = IdPCacheByResourceId.getInstance();
        IdPResourceIdCacheKey cacheKey = new IdPResourceIdCacheKey(resourceId);
        IdPCacheEntry entry = idPCacheByResourceId.getValueFromCache(cacheKey, SUPER_TENANT_DOMAIN);
        if (entry != null) {
            return entry.getIdentityProvider();
        } else {
            return null;
        }
    }
}
