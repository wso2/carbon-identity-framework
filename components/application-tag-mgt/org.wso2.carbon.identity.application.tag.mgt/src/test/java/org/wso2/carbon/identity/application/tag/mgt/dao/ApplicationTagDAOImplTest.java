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
import org.wso2.carbon.identity.application.tag.common.model.ApplicationTagPOST;
import org.wso2.carbon.identity.application.tag.common.model.ApplicationTagsListItem;
import org.wso2.carbon.identity.application.tag.mgt.dao.impl.ApplicationTagDAOImpl;
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
public class ApplicationTagDAOImplTest extends PowerMockTestCase {

    private static final String DB_NAME = "application_tag_mgt_dao_db";
    private static final String TEST_TAG_1_NAME = "test_tag_1";
    private static final String TEST_TAG_1_COLOUR = "#677b66";
    private static final String TEST_TAG_1_UPDATED_NAME = "test_tag_1_updated";
    private static final String TEST_TAG_1_UPDATED_COLOUR = "#927b66";
    private static final String TEST_TAG_2_NAME = "test_tag_2";
    private static final String TEST_TAG_2_COLOUR = "#589b66";
    private static final Map<String, BasicDataSource> dataSourceMap = new HashMap<>();
    private static final String SUPER_TENANT_DOMAIN_NAME = "carbon.super";
    private static final String SAMPLE_TENANT_DOMAIN = "wso2.com";
    private ApplicationTagDAOImpl daoImpl;
    private String tenantDomain;
    private String tagId1;
    private String tagId2;

//    @Factory(dataProvider = "applicationTagTestDataProvider")
//    public ApplicationTagDAOImplTest(String tenantDomain) {
//
//        this.tenantDomain = tenantDomain;
//    }

    @BeforeClass
    public void setUp() throws Exception {

        daoImpl = new ApplicationTagDAOImpl();
        initiateH2Database(getFilePath());
    }

    @AfterClass
    public void tearDown() throws Exception {

        closeH2Database();
    }

    @DataProvider(name = "applicationTagTestDataProvider")
    public Object[][] applicationTagDataProvider() {

        return new Object[][]{{SUPER_TENANT_DOMAIN_NAME}, {SAMPLE_TENANT_DOMAIN}};
    }

    @Test(dataProvider = "applicationTagTestDataProvider", priority = 1)
    public void testCreateApplicationTag(String tenantDomain) throws Exception {

//        daoImpl = new ApplicationTagDAOImpl();
//        initiateH2Database(getFilePath());
        mockStatic(IdentityDatabaseUtil.class);
        mockStatic(ApplicationTagManagementUtil.class);
//        throw new IllegalArgumentException("Hi ###");
        Connection connection = getConnection();
        when(IdentityDatabaseUtil.getDBConnection(anyBoolean())).thenReturn(connection);
        PowerMockito.doAnswer((Answer<Void>) invocation -> {
            connection.commit();
            return null;
        }).when(IdentityDatabaseUtil.class, "commitTransaction", any(Connection.class));
        ApplicationTagPOST inputTag = createApplicationTag(TEST_TAG_1_NAME, TEST_TAG_1_COLOUR);
        tagId1 = daoImpl.createApplicationTag(inputTag, tenantDomain);
        Assert.assertNotNull(tagId1);
    }

    @Test(dataProvider = "applicationTagTestDataProvider", priority = 2)
    public void testGetApplicationTagById(String tenantDomain) throws Exception {

        mockStatic(IdentityDatabaseUtil.class);
        mockStatic(ApplicationTagManagementUtil.class);

        Connection connection = getConnection();
        when(IdentityDatabaseUtil.getDBConnection(anyBoolean())).thenReturn(connection);
        PowerMockito.doAnswer((Answer<Void>) invocation -> {
            connection.commit();
            return null;
        }).when(IdentityDatabaseUtil.class, "commitTransaction", any(Connection.class));
        tagId2 = addApplicationTagToDB(TEST_TAG_2_NAME, TEST_TAG_2_COLOUR, getConnection(), tenantDomain);
        when(IdentityDatabaseUtil.getDBConnection(anyBoolean())).thenReturn(getConnection());
        ApplicationTagsListItem fetchedTag = daoImpl.getApplicationTagById(tagId2, tenantDomain);
        Assert.assertNotNull(fetchedTag);
        Assert.assertEquals(fetchedTag.getName(), TEST_TAG_2_NAME);
        Assert.assertEquals(fetchedTag.getColour(), TEST_TAG_2_COLOUR);
    }

    @Test(dataProvider = "applicationTagTestDataProvider", priority = 3)
    public void testGetAllApplicationTags(String tenantDomain) throws Exception {

        mockStatic(IdentityDatabaseUtil.class);
        mockStatic(ApplicationTagManagementUtil.class);

        when(IdentityDatabaseUtil.getDBConnection(anyBoolean())).thenReturn(getConnection());
        List<ApplicationTagsListItem> fetchedTags = daoImpl.getAllApplicationTags(tenantDomain);
        Assert.assertNotNull(fetchedTags);
        Assert.assertEquals(fetchedTags.size(), 2);
    }

    @Test(dataProvider = "applicationTagTestDataProvider", priority = 4)
    public void testUpdateApplicationTag(String tenantDomain) throws Exception {

        mockStatic(IdentityDatabaseUtil.class);
        mockStatic(ApplicationTagManagementUtil.class);

        Connection connection = getConnection();
        when(IdentityDatabaseUtil.getDBConnection(anyBoolean())).thenReturn(connection);
        PowerMockito.doAnswer((Answer<Void>) invocation -> {
            connection.commit();
            return null;
        }).when(IdentityDatabaseUtil.class, "commitTransaction", any(Connection.class));
        ApplicationTagPOST inputUpdateTag = createApplicationTag(TEST_TAG_1_UPDATED_NAME, TEST_TAG_1_UPDATED_COLOUR);
        daoImpl.updateApplicationTag(inputUpdateTag, tagId1, tenantDomain);
        when(IdentityDatabaseUtil.getDBConnection(anyBoolean())).thenReturn(getConnection());
        ApplicationTagsListItem fetchedTag = daoImpl.getApplicationTagById(tagId1, tenantDomain);
        Assert.assertNotNull(fetchedTag);
        Assert.assertEquals(fetchedTag.getName(), TEST_TAG_1_UPDATED_NAME);
        Assert.assertEquals(fetchedTag.getColour(), TEST_TAG_1_UPDATED_COLOUR);
    }

    @Test(dataProvider = "applicationTagTestDataProvider", priority = 5)
    public void testDeleteApplicationTag(String tenantDomain) throws Exception {

        mockStatic(IdentityDatabaseUtil.class);
        mockStatic(ApplicationTagManagementUtil.class);

        Connection connection = getConnection();
        when(IdentityDatabaseUtil.getDBConnection(anyBoolean())).thenReturn(connection);
        PowerMockito.doAnswer((Answer<Void>) invocation -> {
            connection.commit();
            return null;
        }).when(IdentityDatabaseUtil.class, "commitTransaction", any(Connection.class));
        daoImpl.deleteApplicationTagById(tagId1, tenantDomain);
        when(IdentityDatabaseUtil.getDBConnection(anyBoolean())).thenReturn(getConnection());
        ApplicationTagsListItem fetchedTag = daoImpl.getApplicationTagById(tagId1, tenantDomain);
        Assert.assertNull(fetchedTag);
    }

    /**
     * Create Application Tag with the given values.
     *
     * @param name      Tag Name.
     * @param colour    Tag Colour.
     * @return ApplicationTagPOST.
     */
    private static ApplicationTagPOST createApplicationTag(String name, String colour) {

        ApplicationTagPOST.ApplicationTagPOSTBuilder appTagBuilder =
                new ApplicationTagPOST.ApplicationTagPOSTBuilder()
                .name(name)
                .colour(colour);
        return appTagBuilder.build();
    }

    /**
     * Add Application Tag to the database.
     *
     * @param name          Tag Name.
     * @param colour        Tag Colour.
     * @param connection    Database connection.
     * @param tenantDomain  Tenant Domain.
     * @return Application Tag Id.
     * @throws Exception Error when adding Application Tag.
     */
    private String addApplicationTagToDB(String name, String colour, Connection connection, String tenantDomain)
            throws Exception {

        ApplicationTagPOST inputApplicationTag = createApplicationTag(name, colour);
        when(IdentityDatabaseUtil.getDBConnection(anyBoolean())).thenReturn(connection);
        PowerMockito.doAnswer((Answer<Void>) invocation -> {
            connection.commit();
            return null;
        }).when(IdentityDatabaseUtil.class, "commitTransaction", any(Connection.class));
        return daoImpl.createApplicationTag(inputApplicationTag, tenantDomain);
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
    private static String getFilePath() {

        if (StringUtils.isNotBlank("h2.sql")) {

            return Paths.get(System.getProperty("user.dir"), "src", "test", "resources", "dbscripts", "h2.sql")
                    .toString();
        }
        throw new IllegalArgumentException("DB Script file name cannot be empty.");
    }
}
