/*
 * Copyright (c) 2025, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.carbon.identity.user.registration.mgt.dao;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.dbcp.BasicDataSource;
import org.apache.commons.lang.StringUtils;
import org.mockito.MockedStatic;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.core.util.IdentityDatabaseUtil;
import org.wso2.carbon.identity.user.registration.mgt.model.RegistrationFlowDTO;
import org.wso2.carbon.identity.user.registration.mgt.model.RegistrationGraphConfig;
import org.wso2.carbon.identity.user.registration.mgt.utils.GraphBuilder;

import java.io.File;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.mockStatic;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

/**
 * Test class for RegistrationFlowDAOImpl.
 */
public class RegistrationFlowDAOImplTest {

    private static final String DB_NAME = "registration_flow_mgt_dao_db";
    private RegistrationFlowDAOImpl daoImpl;
    private static final Map<String, BasicDataSource> dataSourceMap = new HashMap<>();

    @BeforeClass
    public void setUp() throws Exception {

        daoImpl = new RegistrationFlowDAOImpl();
        initiateH2Database(getFilePath("identity.sql"));
    }

    @AfterClass
    public void tearDown() throws Exception {

        closeH2Database();
    }

    @DataProvider
    public Object[][] registrationFlowData() {
        return new Object[][]{
                {createSampleRegistrationGraphConfig(), 1, "SampleFlow"}
        };
    }

    @Test(dataProvider = "registrationFlowData")
    public void testUpdateDefaultRegistrationFlowByTenant(RegistrationGraphConfig regFlowConfig, int tenantId,
                                                          String flowName) throws Exception {

        try (MockedStatic<IdentityDatabaseUtil> identityDatabaseUtil = mockStatic(IdentityDatabaseUtil.class)) {
            identityDatabaseUtil.when(IdentityDatabaseUtil::getDataSource)
                    .thenReturn(dataSourceMap.get(DB_NAME));
            daoImpl.updateDefaultRegistrationFlowByTenant(regFlowConfig, tenantId, flowName);
            RegistrationFlowDTO flowDTO = daoImpl.getDefaultRegistrationFlowByTenant(tenantId);
            assertNotNull(flowDTO);
        }
    }

    @Test(dataProvider = "registrationFlowData")
    public void testGetDefaultRegistrationFlowByTenant(RegistrationGraphConfig regFlowConfig, int tenantId,
                                                       String flowName) throws Exception {

        try (MockedStatic<IdentityDatabaseUtil> identityDatabaseUtil = mockStatic(IdentityDatabaseUtil.class)) {
            identityDatabaseUtil.when(IdentityDatabaseUtil::getDataSource)
                    .thenReturn(dataSourceMap.get(DB_NAME));
            daoImpl.updateDefaultRegistrationFlowByTenant(regFlowConfig, tenantId, flowName);
            RegistrationFlowDTO flowDTO = daoImpl.getDefaultRegistrationFlowByTenant(tenantId);
            assertNotNull(flowDTO);
            assertEquals(flowDTO.getSteps().size(), 4);
            assertEquals(flowDTO.getSteps().get(0).getId(), "step_1");
        }
    }

    private static RegistrationGraphConfig createSampleRegistrationGraphConfig() {

        try {
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            RegistrationFlowDTO flowDTO = objectMapper.readValue(new File(getFilePath("reg_flow.json")),
                    RegistrationFlowDTO.class);
            return new GraphBuilder().withSteps(flowDTO.getSteps()).build();
        } catch (Exception e) {
            throw new RuntimeException("Error while reading the JSON file.", e);
        }
    }

    private static Connection getConnection() throws SQLException {

        if (dataSourceMap.get(DB_NAME) != null) {
            return dataSourceMap.get(DB_NAME).getConnection();
        }
        throw new RuntimeException("No datasource initiated for database: " + DB_NAME);
    }

    /**
     * Initiate H2 database.
     *
     * @param scriptPath Path to the database script.
     * @throws Exception Error when initiating H2 database.
     */
    private void initiateH2Database(String scriptPath) throws Exception {

        BasicDataSource dataSource = new BasicDataSource();
        dataSource.setDriverClassName("org.h2.Driver");
        dataSource.setUsername("username");
        dataSource.setPassword("password");
        dataSource.setUrl("jdbc:h2:mem:test" + DB_NAME);
        dataSource.setTestOnBorrow(true);
        dataSource.setValidationQuery("select 1");
        try (Connection connection = dataSource.getConnection()) {
            connection.createStatement().executeUpdate("RUNSCRIPT FROM '" + scriptPath + "'");
        }
        dataSourceMap.put(DB_NAME, dataSource);
    }

    /**
     * Close H2 database.
     *
     * @throws Exception Error when closing H2 database.
     */
    public static void closeH2Database() throws Exception {

        BasicDataSource dataSource = dataSourceMap.get(DB_NAME);
        if (dataSource != null) {
            dataSource.close();
        }
    }

    /**
     * Get the path to the database script.
     *
     * @return Path to the database script.
     */
    private static String getFilePath(String filename) {

        if (StringUtils.isNotBlank(filename)) {
            return Paths.get(System.getProperty("user.dir"), "src", "test", "resources", filename).toString();
        }
        throw new IllegalArgumentException("DB Script file name cannot be empty.");
    }
}
