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

package org.wso2.carbon.identity.application.tag.mgt;

import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.application.common.model.ApplicationTag;
import org.wso2.carbon.identity.application.common.model.ApplicationTag.ApplicationTagBuilder;
import org.wso2.carbon.identity.application.common.model.ApplicationTagsItem;
import org.wso2.carbon.identity.application.common.model.ApplicationTagsListItem;
import org.wso2.carbon.identity.common.testng.WithAxisConfiguration;
import org.wso2.carbon.identity.common.testng.WithCarbonHome;
import org.wso2.carbon.identity.common.testng.WithH2Database;
import org.wso2.carbon.identity.common.testng.WithRealmService;
import org.wso2.carbon.identity.common.testng.WithRegistry;

import java.util.List;

@WithAxisConfiguration
@WithCarbonHome
@WithRegistry
@WithRealmService
@WithH2Database(files = {"dbscripts/h2.sql"})
public class ApplicationTagManagerTest {

    private static final String TEST_TAG_NAME = "test_tag_";
    private static final String TEST_TAG_COLOUR = "#677b6";
    private static final String TEST_TAG_UPDATED_COLOUR = "#927b66";
    private static final String TEST_TAG_DEFAULT_COLOUR = "#345f66";
    private static final String SUPER_TENANT_DOMAIN_NAME = "carbon.super";
    private ApplicationTagManager applicationTagManager;
    private String[] tagIdList;
    private String[] tagNameList;
    private String[] tagColourList;

    @BeforeMethod
    public void setUp() {

        applicationTagManager = ApplicationTagManagerImpl.getInstance();
    }

    @AfterMethod
    public void tearDown() {

    }

    @DataProvider(name = "applicationTagTestDataProvider")
    public Object[][] applicationTagDataProvider() {

        return new Object[][]{{SUPER_TENANT_DOMAIN_NAME}};
    }

    @Test(dataProvider = "applicationTagTestDataProvider", priority = 1)
    public void testGetAllApplicationTags(String tenantDomain) throws Exception {

        int tagCount  = 12;
        tagIdList = new String[tagCount];
        tagNameList = new String[tagCount];
        tagColourList = new String[tagCount];

        for (int count = 0; count < tagCount; count++) {
            String number = count < 9 ? "0" + String.valueOf(count + 1) : String.valueOf(count + 1);
            tagNameList[count] = TEST_TAG_NAME + number + tenantDomain;
            tagColourList[count] = TEST_TAG_COLOUR + number;
            tagIdList[count] = applicationTagManager.createApplicationTag(createAppTagObj(tagNameList[count], 
                    tagColourList[count]), tenantDomain).getId();
        }

        // Get all Tags without setting offset or filter
        int limit = 12;
        int offset = 0;
        List<ApplicationTagsListItem> fetchedTags = applicationTagManager.getAllApplicationTags(tenantDomain, offset,
                limit, null);
        Assert.assertEquals(applicationTagManager.getCountOfApplicationTags(null, tenantDomain), tagCount);
        Assert.assertEquals(fetchedTags.size(), tagCount);
        Assert.assertTrue(isAllApplicationTagsFetched(offset, fetchedTags),
                "Failed to retrieve Application Tags without offset and filter");

        // Get all Tags with limit, offset and without filter
        limit = 10;
        offset = 0;
        fetchedTags = applicationTagManager.getAllApplicationTags(tenantDomain, offset, limit, null);
        Assert.assertEquals(fetchedTags.size(), limit);
        Assert.assertTrue(isAllApplicationTagsFetched(offset, fetchedTags),
                "Failed to retrieve Application Tags with limit and without offset and filter");

        limit = 10;
        offset = 10;
        fetchedTags = applicationTagManager.getAllApplicationTags(tenantDomain, offset, limit, null);
        Assert.assertEquals(fetchedTags.size(), tagCount - limit);
        Assert.assertTrue(isAllApplicationTagsFetched(offset, fetchedTags),
                "Failed to retrieve Application Tags with limit, offset and without filter");

        // Get all Tags with filter 'eq' operation and without offset
        limit = 12;
        offset = 0;
        String filter = "name eq " + TEST_TAG_NAME + "02" + tenantDomain;
        fetchedTags = applicationTagManager.getAllApplicationTags(tenantDomain, offset, limit, filter);
        Assert.assertEquals(applicationTagManager.getCountOfApplicationTags(filter, tenantDomain), 1);
        Assert.assertEquals(fetchedTags.size(), 1);
        Assert.assertTrue(tagIdList[1].equals(fetchedTags.get(0).getId()) && tagNameList[1].equals(
                        fetchedTags.get(0).getName()) && tagColourList[1].equals(fetchedTags.get(0).getColour()),
                "Failed to retrieve Application Tags with filter 'eq' operation and without offset");

        // Get all Tags with filter 'co' operation and without offset
        limit = 12;
        offset = 0;
        filter = "name co 1";
        fetchedTags = applicationTagManager.getAllApplicationTags(tenantDomain, offset, limit, filter);
        Assert.assertEquals(applicationTagManager.getCountOfApplicationTags(filter, tenantDomain), 4);
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
        fetchedTags = applicationTagManager.getAllApplicationTags(tenantDomain, offset, limit, filter);
        Assert.assertEquals(applicationTagManager.getCountOfApplicationTags(filter, tenantDomain), tagCount);
        Assert.assertEquals(fetchedTags.size(), limit);
        Assert.assertTrue(isAllApplicationTagsFetched(offset, fetchedTags),
                "Failed to retrieve Application Tags with filter 'sw' operation and without offset");

        // Get all Tags with filter 'ew' operation and offset
        limit = 10;
        offset = 0;
        filter = "name ew " + tenantDomain;
        fetchedTags = applicationTagManager.getAllApplicationTags(tenantDomain, offset, limit, filter);
        Assert.assertEquals(applicationTagManager.getCountOfApplicationTags(filter, tenantDomain), tagCount);
        Assert.assertEquals(fetchedTags.size(), limit);
        Assert.assertTrue(isAllApplicationTagsFetched(offset, fetchedTags),
                "Failed to retrieve Application Tags with filter 'ew' operation, limit and without offset");

        limit = 10;
        offset = 10;
        fetchedTags = applicationTagManager.getAllApplicationTags(tenantDomain, offset, limit, filter);
        Assert.assertEquals(fetchedTags.size(), tagCount - limit);
        Assert.assertTrue(isAllApplicationTagsFetched(offset, fetchedTags),
                "Failed to retrieve Application Tags with filter 'ew' operation, limit and offset");

        // Get all Tags with multiple filter operations and without offset
        limit = 12;
        offset = 0;
        filter = "name sw test and name co 1 and name ew 2" + tenantDomain;
        fetchedTags = applicationTagManager.getAllApplicationTags(tenantDomain, offset, limit, filter);
        Assert.assertEquals(applicationTagManager.getCountOfApplicationTags(filter, tenantDomain), 1);
        Assert.assertEquals(fetchedTags.size(), 1);
        Assert.assertTrue(tagIdList[11].equals(fetchedTags.get(0).getId()) && tagNameList[11].equals(
                        fetchedTags.get(0).getName()) && tagColourList[11].equals(fetchedTags.get(0).getColour()),
                "Failed to retrieve Application Tags with multiple filter operations and without offset");

        //Delete created Application Tags
        deleteApplicationTags(tagIdList, tenantDomain);
    }

    @Test(dataProvider = "applicationTagTestDataProvider", priority = 2)
    public void testCreateApplicationTag(String tenantDomain) throws Exception {

        ApplicationTagMgtClientException exception = null;
        ApplicationTag inpTag = createAppTagObj("testCreateAppTag_" + tenantDomain, TEST_TAG_DEFAULT_COLOUR);
        ApplicationTagsItem createdTag = null;
        try {
            createdTag = applicationTagManager.createApplicationTag(inpTag, tenantDomain);
        } catch (ApplicationTagMgtClientException e) {
            exception = e;
        }
        Assert.assertNull(exception);
        Assert.assertNotNull(createdTag.getId());
        Assert.assertEquals(createdTag.getName(), "testCreateAppTag_" + tenantDomain);
        Assert.assertEquals(createdTag.getColour(), TEST_TAG_DEFAULT_COLOUR);

        //Delete created Application Tags
        deleteApplicationTags(new String[]{createdTag.getId()}, tenantDomain);
    }

    @Test(dataProvider = "applicationTagTestDataProvider", priority = 3)
    public void testGetApplicationTagById(String tenantDomain) throws Exception {

        String tagName = "testGetAppTagById_" + tenantDomain;
        String tagId = applicationTagManager.createApplicationTag(createAppTagObj(tagName, TEST_TAG_DEFAULT_COLOUR),
                tenantDomain).getId();
        ApplicationTagsListItem fetchedTag = applicationTagManager.getApplicationTagById(tagId, tenantDomain);
        Assert.assertNotNull(fetchedTag);
        Assert.assertEquals(fetchedTag.getId(), tagId);
        Assert.assertEquals(fetchedTag.getName(), tagName);
        Assert.assertEquals(fetchedTag.getColour(), TEST_TAG_DEFAULT_COLOUR);

        //Delete created Application Tags
        deleteApplicationTags(new String[]{tagId}, tenantDomain);
    }

    @Test(dataProvider = "applicationTagTestDataProvider")
    public void testUpdateApplicationTag(String tenantDomain) throws Exception {

        String tagId = applicationTagManager.createApplicationTag(createAppTagObj("testAppTag_" + tenantDomain,
                        TEST_TAG_DEFAULT_COLOUR), tenantDomain).getId();
        String updatedTagName = "testUpdatedAppTag_" + tenantDomain;
        ApplicationTag inputUpdateTag = createAppTagObj(updatedTagName, TEST_TAG_UPDATED_COLOUR);
        applicationTagManager.updateApplicationTag(inputUpdateTag, tagId, tenantDomain);

        ApplicationTagsListItem fetchedUpdatedTag = applicationTagManager.getApplicationTagById(tagId, tenantDomain);
        Assert.assertNotNull(fetchedUpdatedTag);
        Assert.assertEquals(fetchedUpdatedTag.getId(), tagId);
        Assert.assertEquals(fetchedUpdatedTag.getName(), updatedTagName);
        Assert.assertEquals(fetchedUpdatedTag.getColour(), TEST_TAG_UPDATED_COLOUR);

        //Delete created Application Tags
        deleteApplicationTags(new String[]{tagId}, tenantDomain);
    }

    @Test(dataProvider = "applicationTagTestDataProvider")
    public void testDeleteApplicationTag(String tenantDomain) throws Exception {

        String tagId = applicationTagManager.createApplicationTag(createAppTagObj(
                "testDeleteAppTag_" + tenantDomain, TEST_TAG_DEFAULT_COLOUR), tenantDomain).getId();
        applicationTagManager.deleteApplicationTagById(tagId, tenantDomain);
        ApplicationTagsListItem fetchedTag = applicationTagManager.getApplicationTagById(tagId, tenantDomain);
        Assert.assertNull(fetchedTag);
    }

    /**
     * Create Application Tag with the given values.
     *
     * @param name      Tag Name.
     * @param colour    Tag Colour.
     * @return ApplicationTagPOST.
     */
    private static ApplicationTag createAppTagObj(String name, String colour) {

        ApplicationTagBuilder appTagBuilder =
                new ApplicationTagBuilder()
                        .name(name)
                        .colour(colour);
        return appTagBuilder.build();
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

    private void deleteApplicationTags(String[] applicationTagIds, String tenantDomain) throws Exception {
        for (String tagId : applicationTagIds) {
            applicationTagManager.deleteApplicationTagById(tagId, tenantDomain);
        }
    }
}
