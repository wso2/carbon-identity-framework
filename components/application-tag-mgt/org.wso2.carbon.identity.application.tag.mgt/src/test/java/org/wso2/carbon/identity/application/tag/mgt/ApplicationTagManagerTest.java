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

    private static final String TEST_TAG_1_NAME = "test_tag_1";
    private static final String TEST_TAG_1_COLOUR = "#677b66";
    private static final String TEST_TAG_2_NAME = "test_tag_2";
    private static final String TEST_TAG_2_COLOUR = "#589b66";
    private static final String TEST_TAG_UPDATED_COLOUR = "#927b66";
    private static final String TEST_TAG_DEFAULT_COLOUR = "#345f66";
    private static final String SUPER_TENANT_DOMAIN_NAME = "carbon.super";
    private ApplicationTagManager applicationTagManager;

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

        String tagId1 = applicationTagManager.createApplicationTag(createAppTagObj(TEST_TAG_1_NAME, TEST_TAG_1_COLOUR),
                tenantDomain);
        String tagId2 = applicationTagManager.createApplicationTag(createAppTagObj(TEST_TAG_2_NAME, TEST_TAG_2_COLOUR),
                tenantDomain);
        List<ApplicationTagsItem> fetchedTags = applicationTagManager.getAllApplicationTags(tenantDomain);

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

    @Test(dataProvider = "applicationTagTestDataProvider", priority = 2)
    public void testCreateApplicationTag(String tenantDomain) throws Exception {

        ApplicationTagMgtClientException exception = null;
        ApplicationTag inpTag = createAppTagObj("testCreateAppTag_" + tenantDomain, TEST_TAG_DEFAULT_COLOUR);
        String tagId = null;
        try {
            tagId = applicationTagManager.createApplicationTag(inpTag, tenantDomain);
        } catch (ApplicationTagMgtClientException e) {
            exception = e;
        }
        Assert.assertNull(exception);
        Assert.assertNotNull(applicationTagManager.getApplicationTagById(tagId, tenantDomain));
    }

    @Test(dataProvider = "applicationTagTestDataProvider", priority = 3)
    public void testGetApplicationTagById(String tenantDomain) throws Exception {

        String tagName = "testGetAppTagById_" + tenantDomain;
        String tagId = applicationTagManager.createApplicationTag(createAppTagObj(tagName, TEST_TAG_DEFAULT_COLOUR),
                tenantDomain);
        ApplicationTagsItem fetchedTag = applicationTagManager.getApplicationTagById(tagId, tenantDomain);
        Assert.assertNotNull(fetchedTag);
        Assert.assertEquals(fetchedTag.getId(), tagId);
        Assert.assertEquals(fetchedTag.getName(), tagName);
        Assert.assertEquals(fetchedTag.getColour(), TEST_TAG_DEFAULT_COLOUR);
    }

    @Test(dataProvider = "applicationTagTestDataProvider")
    public void testUpdateApplicationTag(String tenantDomain) throws Exception {

        String tagId = applicationTagManager.createApplicationTag(createAppTagObj("testAppTag_" + tenantDomain,
                        TEST_TAG_DEFAULT_COLOUR), tenantDomain);
        String updatedTagName = "testUpdatedAppTag_" + tenantDomain;
        ApplicationTag inputUpdateTag = createAppTagObj(updatedTagName, TEST_TAG_UPDATED_COLOUR);
        applicationTagManager.updateApplicationTag(inputUpdateTag, tagId, tenantDomain);

        ApplicationTagsItem fetchedUpdatedTag = applicationTagManager.getApplicationTagById(tagId, tenantDomain);
        Assert.assertNotNull(fetchedUpdatedTag);
        Assert.assertEquals(fetchedUpdatedTag.getId(), tagId);
        Assert.assertEquals(fetchedUpdatedTag.getName(), updatedTagName);
        Assert.assertEquals(fetchedUpdatedTag.getColour(), TEST_TAG_UPDATED_COLOUR);
    }

    @Test(dataProvider = "applicationTagTestDataProvider")
    public void testDeleteApplicationTag(String tenantDomain) throws Exception {

        String tagId = applicationTagManager.createApplicationTag(createAppTagObj(
                "testDeleteAppTag_" + tenantDomain, TEST_TAG_DEFAULT_COLOUR), tenantDomain);
        applicationTagManager.deleteApplicationTagById(tagId, tenantDomain);
        ApplicationTagsItem fetchedTag = applicationTagManager.getApplicationTagById(tagId, tenantDomain);
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
}
