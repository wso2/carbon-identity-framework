/*
 * Copyright (c) 2023, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.carbon.identity.application.tag.mgt.dao;

import org.apache.commons.dbcp.BasicDataSource;
import org.apache.commons.lang.StringUtils;
import org.mockito.stubbing.Answer;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.testng.PowerMockTestCase;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.wso2.carbon.base.CarbonBaseConstants;
import org.wso2.carbon.identity.application.common.model.ApplicationTag;
import org.wso2.carbon.identity.application.common.model.ApplicationTag.ApplicationTagBuilder;
import org.wso2.carbon.identity.application.common.model.ApplicationTagsItem;
import org.wso2.carbon.identity.application.tag.mgt.dao.impl.ApplicationTagDAOImpl;
import org.wso2.carbon.identity.application.tag.mgt.dao.impl.CacheBackedApplicationTagDAO;
import org.wso2.carbon.identity.application.tag.mgt.util.ApplicationTagManagementUtil;
import org.wso2.carbon.identity.core.util.IdentityDatabaseUtil;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;
import org.wso2.carbon.identity.core.util.IdentityUtil;


import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.when;

@PrepareForTest({IdentityDatabaseUtil.class, IdentityTenantUtil.class, IdentityUtil.class, DataSource.class,
        ApplicationTagManagementUtil.class})
public class CacheBackedApplicationTagDAOTest extends PowerMockTestCase {

    private static final String DB_NAME = "cache_backed_application_tag_mgt_dao_db";
    private static final String TEST_TAG_1_NAME = "test_tag_1";
    private static final String TEST_TAG_1_COLOUR = "#677b66";
    private static final String TEST_TAG_1_UPDATED_NAME = "test_tag_1_updated";
    private static final String TEST_TAG_UPDATED_COLOUR = "#927b66";
    private static final String TEST_TAG_2_NAME = "test_tag_2";
    private static final String TEST_TAG_2_COLOUR = "#589b66";
    private static final String TEST_TAG_DEFAULT_COLOUR = "#345f66";
    private static final Integer SUPER_TENANT_DOMAIN_ID = -1234;
    private static final String SUPER_TENANT_DOMAIN_NAME = "carbon.super";
    private static final Integer SAMPLE_TENANT_DOMAIN_ID = 2;
    private static final String SAMPLE_TENANT_DOMAIN_NAME = "wso2.com";
    private static final Map<String, BasicDataSource> dataSourceMap = new HashMap<>();
    private CacheBackedApplicationTagDAO cacheBackedDaoImpl;
    private String tagId1;
    private String tagId2;

    @BeforeClass
    public void setUp() throws Exception {

        setUpCarbonHome();
        ApplicationTagDAOImpl applicationTagDAO = new ApplicationTagDAOImpl();
        cacheBackedDaoImpl = new CacheBackedApplicationTagDAO(applicationTagDAO);
        initiateH2Database(getFilePath());
    }

    private static void setUpCarbonHome() {

        String carbonHome = Paths.get(System.getProperty("user.dir"), "target", "test-classes").toString();
        System.setProperty(CarbonBaseConstants.CARBON_HOME, carbonHome);
        System.setProperty(CarbonBaseConstants.CARBON_CONFIG_DIR_PATH, Paths.get(carbonHome,
                "repository/conf").toString());
    }

    @AfterClass
    public void tearDown() throws Exception {

        closeH2Database();
    }

    @DataProvider(name = "applicationTagTestDataProvider")
    public Object[][] applicationTagDataProvider() {

        return new Object[][]{{SUPER_TENANT_DOMAIN_ID}, {SAMPLE_TENANT_DOMAIN_ID}};
    }

    @Test(dataProvider = "applicationTagTestDataProvider", priority = 1)
    public void testGetAllApplicationTags(Integer tenantID) throws Exception {

        mockStatic(IdentityDatabaseUtil.class);
        mockStatic(IdentityTenantUtil.class);
        mockStatic(ApplicationTagManagementUtil.class);

        when(IdentityTenantUtil.getTenantDomain(tenantID)).thenReturn(getTenantDomain(tenantID));
        String tagId1 = addApplicationTagToDB(TEST_TAG_1_NAME, TEST_TAG_1_COLOUR, getConnection(), tenantID);
        String tagId2 = addApplicationTagToDB(TEST_TAG_2_NAME, TEST_TAG_2_COLOUR, getConnection(), tenantID);
        when(IdentityDatabaseUtil.getDBConnection(anyBoolean())).thenReturn(getConnection());
        List<ApplicationTagsItem> fetchedTags = cacheBackedDaoImpl.getAllApplicationTags(tenantID);

        Assert.assertNotNull(fetchedTags);
        Assert.assertEquals(fetchedTags.size(), 2);
        boolean isTag1Exist = false;
        boolean isTag2Exist = false;
        for (ApplicationTagsItem appTag: fetchedTags) {
            if (tagId1.equals(appTag.getId()) && TEST_TAG_1_NAME.equals(appTag.getName()) &&
                    TEST_TAG_1_COLOUR.equals(appTag.getColour())) {
                isTag1Exist = true;
            } else if (tagId2.equals(appTag.getId()) && TEST_TAG_2_NAME.equals(appTag.getName()) &&
                    TEST_TAG_2_COLOUR.equals(appTag.getColour())) {
                isTag2Exist = true;
            }
        }
        Assert.assertTrue(isTag1Exist);
        Assert.assertTrue(isTag2Exist);
    }

    @Test(dataProvider = "applicationTagTestDataProvider", priority = 3)
    public void testCreateApplicationTag(Integer tenantID) throws Exception {

        mockStatic(IdentityDatabaseUtil.class);
        mockStatic(ApplicationTagManagementUtil.class);

        when(IdentityDatabaseUtil.getDBConnection(anyBoolean())).thenReturn(getConnection());
        ApplicationTag inputTag = createApplicationTag("testCreateAppTag" + tenantID.toString(),
                TEST_TAG_DEFAULT_COLOUR);
        String tagId1 = cacheBackedDaoImpl.createApplicationTag(inputTag, tenantID);
        Assert.assertNotNull(tagId1);
    }

    @Test(dataProvider = "applicationTagTestDataProvider", priority = 3)
    public void testGetApplicationTagById(Integer tenantID) throws Exception {

        mockStatic(IdentityDatabaseUtil.class);
        mockStatic(IdentityTenantUtil.class);
        mockStatic(ApplicationTagManagementUtil.class);

        when(IdentityTenantUtil.getTenantDomain(tenantID)).thenReturn(getTenantDomain(tenantID));
        String tagName = "testGetAppTagById_" + getTenantDomain(tenantID);
        String tagId = addApplicationTagToDB(tagName, TEST_TAG_DEFAULT_COLOUR, getConnection(), tenantID);
        when(IdentityDatabaseUtil.getDBConnection(anyBoolean())).thenReturn(getConnection());
        ApplicationTagsItem fetchedTag = cacheBackedDaoImpl.getApplicationTagById(tagId, tenantID);
        Assert.assertNotNull(fetchedTag);
        Assert.assertEquals(fetchedTag.getId(), tagId);
        Assert.assertEquals(fetchedTag.getName(), tagName);
        Assert.assertEquals(fetchedTag.getColour(), TEST_TAG_DEFAULT_COLOUR);
    }

    @Test(dataProvider = "applicationTagTestDataProvider", priority = 4)
    public void testUpdateApplicationTag(Integer tenantID) throws Exception {

        mockStatic(IdentityDatabaseUtil.class);
        mockStatic(IdentityTenantUtil.class);
        mockStatic(ApplicationTagManagementUtil.class);

        when(IdentityTenantUtil.getTenantDomain(tenantID)).thenReturn(getTenantDomain(tenantID));
        String tagId = addApplicationTagToDB("testAppTag_" + getTenantDomain(tenantID), TEST_TAG_DEFAULT_COLOUR,
                getConnection(), tenantID);
        Connection connection = getConnection();
        when(IdentityDatabaseUtil.getDBConnection(true)).thenReturn(connection);
        when(IdentityDatabaseUtil.getDBConnection(false)).thenReturn(getConnection());
        PowerMockito.doAnswer((Answer<Void>) invocation -> {
            connection.commit();
            return null;
        }).when(IdentityDatabaseUtil.class, "commitTransaction", any(Connection.class));
        String updatedTagName = "testUpdatedAppTag_" + getTenantDomain(tenantID);
        ApplicationTag inputUpdateTag = createApplicationTag(updatedTagName, TEST_TAG_UPDATED_COLOUR);
        cacheBackedDaoImpl.updateApplicationTag(inputUpdateTag, tagId, tenantID);

        when(IdentityDatabaseUtil.getDBConnection(anyBoolean())).thenReturn(getConnection());
        ApplicationTagsItem fetchedTag = cacheBackedDaoImpl.getApplicationTagById(tagId, tenantID);

        Assert.assertNotNull(fetchedTag);
        Assert.assertEquals(fetchedTag.getId(), tagId);
        Assert.assertEquals(fetchedTag.getName(), updatedTagName);
        Assert.assertEquals(fetchedTag.getColour(), TEST_TAG_UPDATED_COLOUR);
    }

    @Test(dataProvider = "applicationTagTestDataProvider", priority = 5)
    public void testDeleteApplicationTag(Integer tenantID) throws Exception {

        mockStatic(IdentityDatabaseUtil.class);
        mockStatic(IdentityTenantUtil.class);
        mockStatic(ApplicationTagManagementUtil.class);

        when(IdentityTenantUtil.getTenantDomain(tenantID)).thenReturn(getTenantDomain(tenantID));
        String tagId = addApplicationTagToDB("testDeleteAppTag_" + getTenantDomain(tenantID),
                TEST_TAG_DEFAULT_COLOUR, getConnection(), tenantID);
        Connection connection = getConnection();
        when(IdentityDatabaseUtil.getDBConnection(true)).thenReturn(connection);
        when(IdentityDatabaseUtil.getDBConnection(false)).thenReturn(getConnection());
        PowerMockito.doAnswer((Answer<Void>) invocation -> {
            connection.commit();
            return null;
        }).when(IdentityDatabaseUtil.class, "commitTransaction", any(Connection.class));
        cacheBackedDaoImpl.deleteApplicationTagById(tagId, tenantID);
        when(IdentityDatabaseUtil.getDBConnection(anyBoolean())).thenReturn(getConnection());
        ApplicationTagsItem fetchedTag = cacheBackedDaoImpl.getApplicationTagById(tagId, tenantID);
        Assert.assertNull(fetchedTag);
    }

    private static Connection getConnection() throws SQLException {

        if (dataSourceMap.get(DB_NAME) != null) {
            return dataSourceMap.get(DB_NAME).getConnection();
        }
        throw new RuntimeException("No datasource initiated for database: " + DB_NAME);
    }

    /**
     * Create Application Tag with the given values.
     *
     * @param name      Tag Name.
     * @param colour    Tag Colour.
     * @return ApplicationTagPOST.
     */
    private static ApplicationTag createApplicationTag(String name, String colour) {

        ApplicationTagBuilder appTagBuilder =
                new ApplicationTagBuilder()
                        .name(name)
                        .colour(colour);
        return appTagBuilder.build();
    }

    /**
     * Add Application Tag to the database.
     *
     * @param name       Tag Name.
     * @param colour     Tag Colour.
     * @param connection Database connection.
     * @param tenantID   Tenant Domain.
     * @return Application Tag Id.
     * @throws Exception Error when adding Application Tag.
     */
    private String addApplicationTagToDB(String name, String colour, Connection connection, Integer tenantID)
            throws Exception {

        ApplicationTag inputApplicationTag = createApplicationTag(name, colour);
        when(IdentityDatabaseUtil.getDBConnection(anyBoolean())).thenReturn(connection);
        PowerMockito.doAnswer((Answer<Void>) invocation -> {
            connection.commit();
            return null;
        }).when(IdentityDatabaseUtil.class, "commitTransaction", any(Connection.class));
        return cacheBackedDaoImpl.createApplicationTag(inputApplicationTag, tenantID);
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
    private static String getFilePath() {

        if (StringUtils.isNotBlank("h2.sql")) {
            return Paths.get(System.getProperty("user.dir"), "src", "test", "resources", "dbscripts", "h2.sql")
                    .toString();
        }
        throw new IllegalArgumentException("DB Script file name cannot be empty.");
    }

    private static String getTenantDomain(int tenantId) {

        if (tenantId == SUPER_TENANT_DOMAIN_ID) {
            return SUPER_TENANT_DOMAIN_NAME;
        } else if (tenantId == SAMPLE_TENANT_DOMAIN_ID) {
            return SAMPLE_TENANT_DOMAIN_NAME;
        }
        return null;
    }
}
