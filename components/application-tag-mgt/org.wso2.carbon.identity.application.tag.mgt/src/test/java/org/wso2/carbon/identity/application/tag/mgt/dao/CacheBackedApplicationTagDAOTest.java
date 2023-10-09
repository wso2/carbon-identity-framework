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
import org.wso2.carbon.identity.application.common.model.ApplicationTagsListItem;
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
    private static final String TEST_TAG_NAME = "test_tag_";
    private static final String TEST_TAG_COLOUR = "#677b6";
    private static final String TEST_TAG_UPDATED_COLOUR = "#927b66";
    private static final String TEST_TAG_DEFAULT_COLOUR = "#345f66";
    private static final Integer SUPER_TENANT_DOMAIN_ID = -1234;
    private static final String SUPER_TENANT_DOMAIN_NAME = "carbon.super";
    private static final Integer SAMPLE_TENANT_DOMAIN_ID = 2;
    private static final String SAMPLE_TENANT_DOMAIN_NAME = "wso2.com";
    private static final Map<String, BasicDataSource> dataSourceMap = new HashMap<>();
    private CacheBackedApplicationTagDAO cacheBackedDaoImpl;
    private String[] tagIdList;
    private String[] tagNameList;
    private String[] tagColourList;

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

        int tagCount  = 12;
        tagIdList = new String[tagCount];
        tagNameList = new String[tagCount];
        tagColourList = new String[tagCount];

        when(IdentityTenantUtil.getTenantDomain(tenantID)).thenReturn(getTenantDomain(tenantID));
        for (int count = 0; count < tagCount; count++) {
            String number = count < 9 ? "0" + String.valueOf(count + 1) : String.valueOf(count + 1);
            tagNameList[count] = TEST_TAG_NAME + number + getTenantDomain(tenantID);
            tagColourList[count] = TEST_TAG_COLOUR + number;
            tagIdList[count] = addApplicationTagToDB(tagNameList[count], tagColourList[count], getConnection(),
                    tenantID);
        }

        // Get all Tags without setting offset or filter
        int limit = 12;
        int offset = 0;
        when(IdentityDatabaseUtil.getDBConnection(anyBoolean())).thenReturn(getConnection());
        List<ApplicationTagsListItem> fetchedTags = cacheBackedDaoImpl.getAllApplicationTags(tenantID, offset, limit,
                null);
        when(IdentityDatabaseUtil.getDBConnection(anyBoolean())).thenReturn(getConnection());
        Assert.assertEquals(cacheBackedDaoImpl.getCountOfApplicationTags(null, tenantID), tagCount);
        Assert.assertEquals(fetchedTags.size(), tagCount);
        Assert.assertTrue(isAllApplicationTagsFetched(offset, fetchedTags),
                "Failed to retrieve Application Tags without offset and filter");

        // Get all Tags with limit, offset and without filter
        limit = 10;
        offset = 0;
        when(IdentityDatabaseUtil.getDBConnection(anyBoolean())).thenReturn(getConnection());
        fetchedTags = cacheBackedDaoImpl.getAllApplicationTags(tenantID, offset, limit, null);
        Assert.assertEquals(fetchedTags.size(), limit);
        Assert.assertTrue(isAllApplicationTagsFetched(offset, fetchedTags),
                "Failed to retrieve Application Tags with limit and without offset and filter");

        limit = 10;
        offset = 10;
        when(IdentityDatabaseUtil.getDBConnection(anyBoolean())).thenReturn(getConnection());
        fetchedTags = cacheBackedDaoImpl.getAllApplicationTags(tenantID, offset, limit, null);
        Assert.assertEquals(fetchedTags.size(), tagCount - limit);
        Assert.assertTrue(isAllApplicationTagsFetched(offset, fetchedTags),
                "Failed to retrieve Application Tags with limit, offset and without filter");

        // Get all Tags with filter 'eq' operation and without offset
        limit = 12;
        offset = 0;
        String filter = "name eq " + TEST_TAG_NAME + "02" + getTenantDomain(tenantID);
        when(IdentityDatabaseUtil.getDBConnection(anyBoolean())).thenReturn(getConnection());
        fetchedTags = cacheBackedDaoImpl.getAllApplicationTags(tenantID, offset, limit, filter);
        when(IdentityDatabaseUtil.getDBConnection(anyBoolean())).thenReturn(getConnection());
        Assert.assertEquals(cacheBackedDaoImpl.getCountOfApplicationTags(filter, tenantID), 1);
        Assert.assertEquals(fetchedTags.size(), 1);
        Assert.assertTrue(tagIdList[1].equals(fetchedTags.get(0).getId()) && tagNameList[1].equals(
                        fetchedTags.get(0).getName()) && tagColourList[1].equals(fetchedTags.get(0).getColour()),
                "Failed to retrieve Application Tags with filter 'eq' operation and without offset");

        // Get all Tags with filter 'co' operation and without offset
        limit = 12;
        offset = 0;
        filter = "name co 1";
        when(IdentityDatabaseUtil.getDBConnection(anyBoolean())).thenReturn(getConnection());
        fetchedTags = cacheBackedDaoImpl.getAllApplicationTags(tenantID, offset, limit, filter);
        when(IdentityDatabaseUtil.getDBConnection(anyBoolean())).thenReturn(getConnection());
        Assert.assertEquals(cacheBackedDaoImpl.getCountOfApplicationTags(filter, tenantID), 4);
        Assert.assertEquals(fetchedTags.size(), 4);
        boolean isAllExpectedTagsFetched = false;
        for (ApplicationTagsListItem fetchedTag : fetchedTags) {
            if (fetchedTag.getName().contains("1")) {
                isAllExpectedTagsFetched = true;
            } else {
                isAllExpectedTagsFetched = false;
                break;
            }
        }
        Assert.assertTrue(isAllExpectedTagsFetched,
                "Failed to retrieve Application Tags with filter 'co' operation and without offset");

        // Get all Tags with filter 'sw' operation and without offset
        limit = 12;
        offset = 0;
        filter = "name sw test";
        when(IdentityDatabaseUtil.getDBConnection(anyBoolean())).thenReturn(getConnection());
        fetchedTags = cacheBackedDaoImpl.getAllApplicationTags(tenantID, offset, limit, filter);
        when(IdentityDatabaseUtil.getDBConnection(anyBoolean())).thenReturn(getConnection());
        Assert.assertEquals(cacheBackedDaoImpl.getCountOfApplicationTags(filter, tenantID), tagCount);
        Assert.assertEquals(fetchedTags.size(), limit);
        Assert.assertTrue(isAllApplicationTagsFetched(offset, fetchedTags),
                "Failed to retrieve Application Tags with filter 'sw' operation and without offset");

        // Get all Tags with filter 'ew' operation and offset
        limit = 10;
        offset = 0;
        filter = "name ew " + getTenantDomain(tenantID);
        when(IdentityDatabaseUtil.getDBConnection(anyBoolean())).thenReturn(getConnection());
        fetchedTags = cacheBackedDaoImpl.getAllApplicationTags(tenantID, offset, limit, filter);
        when(IdentityDatabaseUtil.getDBConnection(anyBoolean())).thenReturn(getConnection());
        Assert.assertEquals(cacheBackedDaoImpl.getCountOfApplicationTags(filter, tenantID), tagCount);
        Assert.assertEquals(fetchedTags.size(), limit);
        Assert.assertTrue(isAllApplicationTagsFetched(offset, fetchedTags),
                "Failed to retrieve Application Tags with filter 'ew' operation, limit and without offset");

        limit = 10;
        offset = 10;
        when(IdentityDatabaseUtil.getDBConnection(anyBoolean())).thenReturn(getConnection());
        fetchedTags = cacheBackedDaoImpl.getAllApplicationTags(tenantID, offset, limit, filter);
        Assert.assertEquals(fetchedTags.size(), tagCount - limit);
        Assert.assertTrue(isAllApplicationTagsFetched(offset, fetchedTags),
                "Failed to retrieve Application Tags with filter 'ew' operation, limit and offset");

        // Get all Tags with multiple filter operations and without offset
        limit = 12;
        offset = 0;
        filter = "name sw test and name co 1 and name ew 2" + getTenantDomain(tenantID);
        when(IdentityDatabaseUtil.getDBConnection(anyBoolean())).thenReturn(getConnection());
        fetchedTags = cacheBackedDaoImpl.getAllApplicationTags(tenantID, offset, limit, filter);
        when(IdentityDatabaseUtil.getDBConnection(anyBoolean())).thenReturn(getConnection());
        Assert.assertEquals(cacheBackedDaoImpl.getCountOfApplicationTags(filter, tenantID), 1);
        Assert.assertEquals(fetchedTags.size(), 1);
        Assert.assertTrue(tagIdList[11].equals(fetchedTags.get(0).getId()) && tagNameList[11].equals(
                        fetchedTags.get(0).getName()) && tagColourList[11].equals(fetchedTags.get(0).getColour()),
                "Failed to retrieve Application Tags with multiple filter operations and without offset");

        // Delete created application Tags
        deleteApplicationTags(tagIdList, tenantID);
    }

    @Test(dataProvider = "applicationTagTestDataProvider", priority = 3)
    public void testCreateApplicationTag(Integer tenantID) throws Exception {

        mockStatic(IdentityDatabaseUtil.class);
        mockStatic(IdentityTenantUtil.class);
        mockStatic(ApplicationTagManagementUtil.class);

        when(IdentityDatabaseUtil.getDBConnection(anyBoolean())).thenReturn(getConnection());
        ApplicationTag inputTag = createApplicationTag("testCreateAppTag" + tenantID.toString(),
                TEST_TAG_DEFAULT_COLOUR);
        ApplicationTagsItem createdTag = cacheBackedDaoImpl.createApplicationTag(inputTag, tenantID);
        Assert.assertNotNull(createdTag.getId());
        Assert.assertEquals(createdTag.getName(), "testCreateAppTag" + tenantID.toString());
        Assert.assertEquals(createdTag.getColour(), TEST_TAG_DEFAULT_COLOUR);

        //Delete created Application Tags
        deleteApplicationTags(new String[]{createdTag.getId()}, tenantID);
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
        ApplicationTagsListItem fetchedTag = cacheBackedDaoImpl.getApplicationTagById(tagId, tenantID);
        Assert.assertNotNull(fetchedTag);
        Assert.assertEquals(fetchedTag.getId(), tagId);
        Assert.assertEquals(fetchedTag.getName(), tagName);
        Assert.assertEquals(fetchedTag.getColour(), TEST_TAG_DEFAULT_COLOUR);

        //Delete created Application Tags
        deleteApplicationTags(new String[]{tagId}, tenantID);
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
        ApplicationTagsListItem fetchedTag = cacheBackedDaoImpl.getApplicationTagById(tagId, tenantID);

        Assert.assertNotNull(fetchedTag);
        Assert.assertEquals(fetchedTag.getId(), tagId);
        Assert.assertEquals(fetchedTag.getName(), updatedTagName);
        Assert.assertEquals(fetchedTag.getColour(), TEST_TAG_UPDATED_COLOUR);

        //Delete created Application Tags
        deleteApplicationTags(new String[]{tagId}, tenantID);
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
        ApplicationTagsListItem fetchedTag = cacheBackedDaoImpl.getApplicationTagById(tagId, tenantID);
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
        return cacheBackedDaoImpl.createApplicationTag(inputApplicationTag, tenantID).getId();
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

    private boolean isAllApplicationTagsFetched(Integer offset, List<ApplicationTagsListItem> fetchedTags) {

        boolean isAllTagsFetched = false;
        for (int count = offset; count < offset + fetchedTags.size(); count++) {
            // Applications should be sorted by Name by default
            if (tagIdList[count].equals(fetchedTags.get(count - offset).getId()) &&
                    tagNameList[count].equals(fetchedTags.get(count - offset).getName()) &&
                    tagColourList[count].equals(fetchedTags.get(count - offset).getColour())) {
                isAllTagsFetched = true;
            } else {
                isAllTagsFetched = false;
                break;
            }
        }
        return isAllTagsFetched;
    }

    private void deleteApplicationTags(String[] applicationTagIds, Integer tenantID) throws Exception {

        when(IdentityTenantUtil.getTenantDomain(tenantID)).thenReturn(getTenantDomain(tenantID));
        for (String tagId : applicationTagIds) {
            Connection connection = getConnection();
            when(IdentityDatabaseUtil.getDBConnection(true)).thenReturn(connection);
            when(IdentityDatabaseUtil.getDBConnection(false)).thenReturn(getConnection());
            PowerMockito.doAnswer((Answer<Void>) invocation -> {
                connection.commit();
                return null;
            }).when(IdentityDatabaseUtil.class, "commitTransaction", any(Connection.class));
            cacheBackedDaoImpl.deleteApplicationTagById(tagId, tenantID);
        }
    }
}
