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
import org.wso2.carbon.identity.application.tag.common.model.ApplicationTagPOST;
import org.wso2.carbon.identity.application.tag.common.model.ApplicationTagsListItem;
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
    private static final String TEST_TAG_1_UPDATED_NAME = "test_tag_1_updated";
    private static final String TEST_TAG_1_UPDATED_COLOUR = "#927b66";
    private static final String SUPER_TENANT_DOMAIN_NAME = "carbon.super";
    private static final String SAMPLE_TENANT_DOMAIN = "wso2.com";
    private ApplicationTagManager applicationTagManager;
    private String tagId1;
    private String tagId2;

    @BeforeMethod
    public void setUp() {

        applicationTagManager = ApplicationTagManagerImpl.getInstance();
    }

    @AfterMethod
    public void tearDown() {

    }

    @DataProvider(name = "applicationTagTestDataProvider")
    public Object[][] applicationTagDataProvider() {

        return new Object[][]{{SUPER_TENANT_DOMAIN_NAME}, {SAMPLE_TENANT_DOMAIN}};
    }

    @Test(dataProvider = "applicationTagTestDataProvider")
    public void testCreateApplicationTag(String tenantDomain) throws Exception {

        ApplicationTagMgtClientException exception = null;
        ApplicationTagPOST inpTag = createApplicationTag(TEST_TAG_1_NAME, TEST_TAG_1_COLOUR);
        try {
            tagId1 = applicationTagManager.createApplicationTag(inpTag, tenantDomain);
        } catch (ApplicationTagMgtClientException e) {
            exception = e;
        }
        Assert.assertNull(exception);
        Assert.assertNotNull(applicationTagManager.getApplicationTagById(tagId1, tenantDomain));
    }

    @Test(dataProvider = "applicationTagTestDataProvider")
    public void testGetApplicationTagById(String tenantDomain) throws Exception {

        tagId2 = applicationTagManager.createApplicationTag(createApplicationTag(TEST_TAG_2_NAME, TEST_TAG_2_COLOUR),
                tenantDomain);
        ApplicationTagsListItem fetchedTag = applicationTagManager.getApplicationTagById(tagId2, tenantDomain);
        Assert.assertNotNull(fetchedTag);
        Assert.assertEquals(fetchedTag.getName(), TEST_TAG_2_NAME);
        Assert.assertEquals(fetchedTag.getColour(), TEST_TAG_2_COLOUR);
    }

    @Test(dataProvider = "applicationTagTestDataProvider")
    public void testGetAllApplicationTags(String tenantDomain) throws Exception {

        List<ApplicationTagsListItem> fetchedTags = applicationTagManager.getAllApplicationTags(tenantDomain);
        Assert.assertNotNull(fetchedTags);
        Assert.assertEquals(fetchedTags.size(), 2);
    }

    @Test(dataProvider = "applicationTagTestDataProvider")
    public void testUpdateApplicationTag(String tenantDomain) throws Exception {

        ApplicationTagPOST inputUpdateTag = createApplicationTag(TEST_TAG_1_UPDATED_NAME, TEST_TAG_1_UPDATED_COLOUR);
        applicationTagManager.updateApplicationTag(inputUpdateTag, tagId1, tenantDomain);

        ApplicationTagsListItem fetchedUpdatedTag = applicationTagManager.getApplicationTagById(tagId1, tenantDomain);
        Assert.assertNotNull(fetchedUpdatedTag);
        Assert.assertEquals(fetchedUpdatedTag.getName(), TEST_TAG_1_UPDATED_NAME);
        Assert.assertEquals(fetchedUpdatedTag.getColour(), TEST_TAG_1_UPDATED_COLOUR);
    }

    @Test(dataProvider = "applicationTagTestDataProvider")
    public void testDeleteApplicationTag(String tenantDomain) throws Exception {

        applicationTagManager.deleteApplicationTagById(tagId1, tenantDomain);
        applicationTagManager.deleteApplicationTagById(tagId2, tenantDomain);
        ApplicationTagsListItem fetchedTag = applicationTagManager.getApplicationTagById(tagId1, tenantDomain);

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
}
