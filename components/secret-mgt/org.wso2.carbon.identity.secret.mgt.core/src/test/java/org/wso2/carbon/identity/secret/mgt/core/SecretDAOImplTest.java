/*
 * Copyright (c) 2026, WSO2 Inc. (http://www.wso2.com).
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

package org.wso2.carbon.identity.secret.mgt.core;

import org.mockito.MockedStatic;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.wso2.carbon.base.CarbonBaseConstants;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.database.utils.jdbc.NamedJdbcTemplate;
import org.wso2.carbon.identity.core.util.IdentityConfigParser;
import org.wso2.carbon.identity.core.util.IdentityDatabaseUtil;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;
import org.wso2.carbon.identity.core.util.JdbcUtils;
import org.wso2.carbon.identity.secret.mgt.core.dao.SecretDAO;
import org.wso2.carbon.identity.secret.mgt.core.dao.impl.SecretDAOImpl;
import org.wso2.carbon.identity.secret.mgt.core.exception.SecretManagementException;
import org.wso2.carbon.identity.secret.mgt.core.model.Secret;
import org.wso2.carbon.identity.secret.mgt.core.model.SecretType;
import org.wso2.carbon.identity.secret.mgt.core.util.TestUtils;

import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import javax.sql.DataSource;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.wso2.carbon.base.MultitenantConstants.SUPER_TENANT_DOMAIN_NAME;
import static org.wso2.carbon.base.MultitenantConstants.SUPER_TENANT_ID;
import static org.wso2.carbon.identity.secret.mgt.core.constant.SecretConstants.IS_SECRET_VALUE_CLOB_COLUMN_EXISTS;
import static org.wso2.carbon.identity.secret.mgt.core.util.TestUtils.closeOracleBase;
import static org.wso2.carbon.identity.secret.mgt.core.util.TestUtils.getSampleSecretAdd;
import static org.wso2.carbon.identity.secret.mgt.core.util.TestUtils.getSampleSecretTypeAdd;
import static org.wso2.carbon.identity.secret.mgt.core.util.TestUtils.initiateOracleBase;
import static org.wso2.carbon.identity.secret.mgt.core.util.TestUtils.spyConnection;

/**
 * Unit tests for {@link SecretDAOImpl} focusing on Oracle CLOB support.
 *
 * Tests are grouped into three scenarios:
 * 1. Non-Oracle DB (H2) - standard VARCHAR path.
 * 2. Oracle DB + CLOB column enabled - secret value stored/read via SECRET_VALUE_CLOB.
 * 3. Oracle DB + CLOB column disabled - falls back to standard VARCHAR path.
 *
 * The Oracle CLOB path is exercised by mocking {@link JdbcUtils#isOracleDB(NamedJdbcTemplate)}
 * to return {@code true} and configuring {@link IdentityConfigParser} to return the CLOB
 * column existence flag as {@code "true"}.
 */
public class SecretDAOImplTest {

    private SecretDAO secretDAO;
    private Connection connection;

    private MockedStatic<IdentityDatabaseUtil> identityDatabaseUtil;
    private MockedStatic<PrivilegedCarbonContext> privilegedCarbonContext;
    private MockedStatic<IdentityTenantUtil> identityTenantUtil;
    /* Only opened for tests that require Oracle path mocking. */
    private MockedStatic<JdbcUtils> jdbcUtils;
    private MockedStatic<IdentityConfigParser> identityConfigParser;

    private static final String SAMPLE_SECRET_TYPE_NAME = "sample-secret-type";
    private static final String SAMPLE_SECRET_TYPE_DESCRIPTION = "sample-description";
    private static final String SAMPLE_SECRET_NAME = "sample-secret";
    private static final String SAMPLE_SECRET_VALUE = "sample-secret-value";
    private static final String SAMPLE_SECRET_VALUE_UPDATED = "sample-secret-value-updated";
    private static final String SAMPLE_SECRET_DESCRIPTION = "sample-secret-description";

    @BeforeMethod
    public void setUp() throws Exception {

        initiateOracleBase();
        String carbonHome = Paths.get(System.getProperty("user.dir"), "target", "test-classes").toString();
        System.setProperty(CarbonBaseConstants.CARBON_HOME, carbonHome);
        System.setProperty(CarbonBaseConstants.CARBON_CONFIG_DIR_PATH,
                Paths.get(carbonHome, "conf").toString());

        DataSource dataSource = mock(DataSource.class);
        identityDatabaseUtil = mockStatic(IdentityDatabaseUtil.class);
        identityDatabaseUtil.when(IdentityDatabaseUtil::getDataSource).thenReturn(dataSource);

        connection = TestUtils.getOracleConnection();
        Connection spyConnection = spyConnection(connection);
        when(dataSource.getConnection()).thenReturn(spyConnection);

        mockCarbonContextForTenant(SUPER_TENANT_ID, SUPER_TENANT_DOMAIN_NAME);
        mockIdentityTenantUtility();

        secretDAO = new SecretDAOImpl();
    }

    @AfterMethod
    public void tearDown() throws Exception {

        connection.close();
        closeOracleBase();
        identityDatabaseUtil.close();
        privilegedCarbonContext.close();
        identityTenantUtil.close();
        if (jdbcUtils != null) {
            jdbcUtils.close();
            jdbcUtils = null;
        }
        if (identityConfigParser != null) {
            identityConfigParser.close();
            identityConfigParser = null;
        }
    }

    // =====================================================================
    // Non-Oracle (H2) path tests — standard VARCHAR column is used
    // =====================================================================

    @Test
    public void testAddSecretWithNonOracleDb() throws Exception {

        addSampleSecretType();
        Secret secret = buildSampleSecret();
        secretDAO.addSecret(secret);

        String[] columns = querySecretColumns(secret.getSecretName());
        assertEquals(columns[0], SAMPLE_SECRET_VALUE,
                "SECRET_VALUE should contain the secret value for non-Oracle DB");
        assertNull(columns[1],
                "SECRET_VALUE_CLOB should be null for non-Oracle DB");
    }

    @Test
    public void testGetSecretByNameWithNonOracleDb() throws Exception {

        addSampleSecretType();
        secretDAO.addSecret(buildSampleSecret());

        SecretType secretType = secretDAO.getSecretTypeByName(SAMPLE_SECRET_TYPE_NAME);
        Secret retrieved = secretDAO.getSecretByName(SAMPLE_SECRET_NAME, secretType, SUPER_TENANT_ID);

        assertNotNull(retrieved, "Retrieved secret should not be null");
        assertEquals(retrieved.getSecretName(), SAMPLE_SECRET_NAME);
        assertEquals(retrieved.getSecretValue(), SAMPLE_SECRET_VALUE,
                "Secret value should be read from SECRET_VALUE column");
    }

    @Test
    public void testGetSecretByIdWithNonOracleDb() throws Exception {

        addSampleSecretType();
        Secret added = buildSampleSecret();
        secretDAO.addSecret(added);

        Secret retrieved = secretDAO.getSecretById(added.getSecretId(), SUPER_TENANT_ID);

        assertNotNull(retrieved, "Retrieved secret should not be null");
        assertEquals(retrieved.getSecretName(), SAMPLE_SECRET_NAME);
        assertEquals(retrieved.getSecretValue(), SAMPLE_SECRET_VALUE,
                "Secret value should be read from SECRET_VALUE column");
    }

    @Test
    public void testUpdateSecretValueWithNonOracleDb() throws Exception {

        addSampleSecretType();
        Secret added = buildSampleSecret();
        secretDAO.addSecret(added);

        secretDAO.updateSecretValue(added, SAMPLE_SECRET_VALUE_UPDATED);

        String[] columns = querySecretColumns(SAMPLE_SECRET_NAME);
        assertEquals(columns[0], SAMPLE_SECRET_VALUE_UPDATED,
                "SECRET_VALUE should contain the updated value");
        assertNull(columns[1], "SECRET_VALUE_CLOB should remain null for non-Oracle DB");
    }

    @Test
    public void testReplaceSecretWithNonOracleDb() throws Exception {

        addSampleSecretType();
        Secret added = buildSampleSecret();
        secretDAO.addSecret(added);

        Secret replacement = buildSampleSecret();
        replacement.setSecretId(added.getSecretId());
        replacement.setSecretValue(SAMPLE_SECRET_VALUE_UPDATED);
        secretDAO.replaceSecret(replacement);

        String[] columns = querySecretColumns(SAMPLE_SECRET_NAME);
        assertEquals(columns[0], SAMPLE_SECRET_VALUE_UPDATED,
                "SECRET_VALUE should contain the replaced value");
        assertNull(columns[1], "SECRET_VALUE_CLOB should remain null for non-Oracle DB");
    }

    // =====================================================================
    // Oracle DB + CLOB enabled — secret value stored/read via CLOB column
    // =====================================================================

    @Test
    public void testAddSecretWithOracleClobEnabled() throws Exception {

        mockOracleClobEnabled();
        addSampleSecretType();
        Secret secret = buildSampleSecret();
        secretDAO.addSecret(secret);

        String[] columns = querySecretColumns(secret.getSecretName());
        assertNull(columns[0],
                "SECRET_VALUE should be null when Oracle INSERT_SECRET_ORACLE is used");
        assertEquals(columns[1], SAMPLE_SECRET_VALUE,
                "SECRET_VALUE_CLOB should contain the secret value for Oracle DB");
    }

    @Test
    public void testGetSecretByNameWithOracleClobEnabled() throws Exception {

        mockOracleClobEnabled();
        addSampleSecretType();
        secretDAO.addSecret(buildSampleSecret());

        SecretType secretType = secretDAO.getSecretTypeByName(SAMPLE_SECRET_TYPE_NAME);
        Secret retrieved = secretDAO.getSecretByName(SAMPLE_SECRET_NAME, secretType, SUPER_TENANT_ID);

        assertNotNull(retrieved, "Retrieved secret should not be null");
        assertEquals(retrieved.getSecretName(), SAMPLE_SECRET_NAME);
        assertEquals(retrieved.getSecretValue(), SAMPLE_SECRET_VALUE,
                "Secret value should be resolved from SECRET_VALUE_CLOB column");
    }

    @Test
    public void testGetSecretByIdWithOracleClobEnabled() throws Exception {

        mockOracleClobEnabled();
        addSampleSecretType();
        Secret added = buildSampleSecret();
        secretDAO.addSecret(added);

        Secret retrieved = secretDAO.getSecretById(added.getSecretId(), SUPER_TENANT_ID);

        assertNotNull(retrieved, "Retrieved secret should not be null");
        assertEquals(retrieved.getSecretName(), SAMPLE_SECRET_NAME);
        assertEquals(retrieved.getSecretValue(), SAMPLE_SECRET_VALUE,
                "Secret value should be resolved from SECRET_VALUE_CLOB column");
    }

    @Test
    public void testUpdateSecretValueWithOracleClobEnabled() throws Exception {

        mockOracleClobEnabled();
        addSampleSecretType();
        Secret added = buildSampleSecret();
        secretDAO.addSecret(added);

        secretDAO.updateSecretValue(added, SAMPLE_SECRET_VALUE_UPDATED);

        String[] columns = querySecretColumns(SAMPLE_SECRET_NAME);
        assertEquals(columns[0], "",
                "SECRET_VALUE should be cleared to empty string after Oracle update");
        assertEquals(columns[1], SAMPLE_SECRET_VALUE_UPDATED,
                "SECRET_VALUE_CLOB should contain the updated value");
    }

    @Test
    public void testReplaceSecretWithOracleClobEnabled() throws Exception {

        mockOracleClobEnabled();
        addSampleSecretType();
        Secret added = buildSampleSecret();
        secretDAO.addSecret(added);

        Secret replacement = buildSampleSecret();
        replacement.setSecretId(added.getSecretId());
        replacement.setSecretValue(SAMPLE_SECRET_VALUE_UPDATED);
        secretDAO.replaceSecret(replacement);

        String[] columns = querySecretColumns(SAMPLE_SECRET_NAME);
        assertEquals(columns[0], "",
                "SECRET_VALUE should be cleared to empty string after Oracle replace");
        assertEquals(columns[1], SAMPLE_SECRET_VALUE_UPDATED,
                "SECRET_VALUE_CLOB should contain the replaced value");
    }

    // =====================================================================
    // Backward-compatibility: CLOB null → fall back to VARCHAR
    // Simulates reading pre-migration data (stored in VARCHAR) with Oracle+CLOB mode enabled.
    // =====================================================================

    @Test
    public void testGetSecretByNameFallsBackToVarcharWhenClobIsEmpty() throws Exception {

        // Step 1: insert using the non-Oracle path — value lands in SECRET_VALUE only.
        addSampleSecretType();
        Secret added = buildSampleSecret();
        secretDAO.addSecret(added);

        // Step 2: switch to Oracle + CLOB mode and retrieve — CLOB column is null,
        // so resolveSecretValue() should fall back to SECRET_VALUE.
        mockOracleClobEnabled();
        SecretType secretType = secretDAO.getSecretTypeByName(SAMPLE_SECRET_TYPE_NAME);
        Secret retrieved = secretDAO.getSecretByName(SAMPLE_SECRET_NAME, secretType, SUPER_TENANT_ID);

        assertNotNull(retrieved, "Retrieved secret should not be null");
        assertEquals(retrieved.getSecretValue(), SAMPLE_SECRET_VALUE,
                "Secret value should fall back to SECRET_VALUE when SECRET_VALUE_CLOB is null");
    }

    @Test
    public void testGetSecretByIdFallsBackToVarcharWhenClobIsEmpty() throws Exception {

        // Step 1: insert using the non-Oracle path — value lands in SECRET_VALUE only.
        addSampleSecretType();
        Secret added = buildSampleSecret();
        secretDAO.addSecret(added);

        // Step 2: switch to Oracle + CLOB mode and retrieve by ID.
        mockOracleClobEnabled();
        Secret retrieved = secretDAO.getSecretById(added.getSecretId(), SUPER_TENANT_ID);

        assertNotNull(retrieved, "Retrieved secret should not be null");
        assertEquals(retrieved.getSecretValue(), SAMPLE_SECRET_VALUE,
                "Secret value should fall back to SECRET_VALUE when SECRET_VALUE_CLOB is null");
    }

    // =====================================================================
    // Oracle DB + CLOB disabled — should fall back to standard VARCHAR path
    // =====================================================================

    @Test
    public void testAddSecretWithOracleClobDisabled() throws Exception {

        mockOracleClobDisabled();
        addSampleSecretType();
        Secret secret = buildSampleSecret();
        secretDAO.addSecret(secret);

        String[] columns = querySecretColumns(secret.getSecretName());
        assertEquals(columns[0], SAMPLE_SECRET_VALUE,
                "SECRET_VALUE should be populated when CLOB column is disabled");
        assertNull(columns[1],
                "SECRET_VALUE_CLOB should be null when CLOB column is disabled");
    }

    @Test
    public void testGetSecretByNameWithOracleClobDisabled() throws Exception {

        mockOracleClobDisabled();
        addSampleSecretType();
        secretDAO.addSecret(buildSampleSecret());

        SecretType secretType = secretDAO.getSecretTypeByName(SAMPLE_SECRET_TYPE_NAME);
        Secret retrieved = secretDAO.getSecretByName(SAMPLE_SECRET_NAME, secretType, SUPER_TENANT_ID);

        assertNotNull(retrieved, "Retrieved secret should not be null");
        assertEquals(retrieved.getSecretValue(), SAMPLE_SECRET_VALUE,
                "Secret value should be read from SECRET_VALUE when CLOB is disabled");
    }

    // =====================================================================
    // Helpers
    // =====================================================================

    /**
     * Configure mocks so the DAO treats the current DB as Oracle with the CLOB column present.
     */
    private void mockOracleClobEnabled() {

        jdbcUtils = mockStatic(JdbcUtils.class);
        jdbcUtils.when(() -> JdbcUtils.isOracleDB(any(NamedJdbcTemplate.class))).thenReturn(true);

        IdentityConfigParser mockParser = mock(IdentityConfigParser.class);
        Map<String, Object> config = new HashMap<>();
        config.put(IS_SECRET_VALUE_CLOB_COLUMN_EXISTS, "true");
        when(mockParser.getConfiguration()).thenReturn(config);

        identityConfigParser = mockStatic(IdentityConfigParser.class);
        identityConfigParser.when(IdentityConfigParser::getInstance).thenReturn(mockParser);
    }

    /**
     * Configure mocks so the DAO treats the current DB as Oracle but with the CLOB column absent
     * (config flag disabled). The DAO should fall back to the standard VARCHAR path.
     */
    private void mockOracleClobDisabled() {

        jdbcUtils = mockStatic(JdbcUtils.class);
        jdbcUtils.when(() -> JdbcUtils.isOracleDB(any(NamedJdbcTemplate.class))).thenReturn(true);

        IdentityConfigParser mockParser = mock(IdentityConfigParser.class);
        Map<String, Object> config = new HashMap<>();
        config.put(IS_SECRET_VALUE_CLOB_COLUMN_EXISTS, "false");
        when(mockParser.getConfiguration()).thenReturn(config);

        identityConfigParser = mockStatic(IdentityConfigParser.class);
        identityConfigParser.when(IdentityConfigParser::getInstance).thenReturn(mockParser);
    }

    private void addSampleSecretType() throws SecretManagementException {

        SecretType secretType = getSampleSecretTypeAdd(SAMPLE_SECRET_TYPE_NAME, SAMPLE_SECRET_TYPE_DESCRIPTION);
        secretType.setId(UUID.randomUUID().toString());
        secretDAO.addSecretType(secretType);
    }

    private Secret buildSampleSecret() {

        Secret secret = getSampleSecretAdd(SAMPLE_SECRET_NAME, SAMPLE_SECRET_VALUE);
        secret.setSecretId(UUID.randomUUID().toString());
        secret.setSecretType(SAMPLE_SECRET_TYPE_NAME);
        secret.setDescription(SAMPLE_SECRET_DESCRIPTION);
        return secret;
    }

    /**
     * Directly queries the underlying H2 connection to read both secret value columns.
     *
     * @return a two-element array: [SECRET_VALUE, SECRET_VALUE_CLOB]
     */
    private String[] querySecretColumns(String secretName) throws Exception {

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(
                     "SELECT SECRET_VALUE, SECRET_VALUE_CLOB FROM IDN_SECRET " +
                             "WHERE SECRET_NAME = '" + secretName + "'")) {
            if (rs.next()) {
                return new String[]{rs.getString("SECRET_VALUE"), rs.getString("SECRET_VALUE_CLOB")};
            }
        }
        throw new RuntimeException("Secret not found in DB: " + secretName);
    }

    private void mockCarbonContextForTenant(int tenantId, String tenantDomain) {

        privilegedCarbonContext = mockStatic(PrivilegedCarbonContext.class);
        PrivilegedCarbonContext mockPrivilegedCarbonContext = mock(PrivilegedCarbonContext.class);
        privilegedCarbonContext.when(PrivilegedCarbonContext::getThreadLocalCarbonContext)
                .thenReturn(mockPrivilegedCarbonContext);
        when(mockPrivilegedCarbonContext.getTenantDomain()).thenReturn(tenantDomain);
        when(mockPrivilegedCarbonContext.getTenantId()).thenReturn(tenantId);
        when(mockPrivilegedCarbonContext.getUsername()).thenReturn("admin");
    }

    private void mockIdentityTenantUtility() {

        identityTenantUtil = mockStatic(IdentityTenantUtil.class);
        identityTenantUtil.when(() -> IdentityTenantUtil.getTenantDomain(any(Integer.class)))
                .thenReturn(SUPER_TENANT_DOMAIN_NAME);
    }
}