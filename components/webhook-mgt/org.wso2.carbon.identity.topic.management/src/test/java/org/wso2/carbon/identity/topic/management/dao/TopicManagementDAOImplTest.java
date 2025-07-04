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

package org.wso2.carbon.identity.topic.management.dao;

import org.testng.annotations.Test;
import org.wso2.carbon.identity.common.testng.WithCarbonHome;
import org.wso2.carbon.identity.common.testng.WithH2Database;
import org.wso2.carbon.identity.topic.management.api.exception.TopicManagementException;
import org.wso2.carbon.identity.topic.management.internal.constant.ErrorMessage;
import org.wso2.carbon.identity.topic.management.internal.dao.impl.TopicManagementDAOImpl;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

@WithH2Database(files = {"dbscripts/h2.sql"})
@WithCarbonHome
public class TopicManagementDAOImplTest {

    private static final String TOPIC = "https://example.com/events/carbon.super";
    private static final String TOPIC2 = "https://example.com/events/carbon.super2";
    private static final String CHANNEL_URI = "example.com/events";
    private static final String EVENT_PROFILE_VERSION = "v1";
    private static final int TENANT_ID = 1;

    private final TopicManagementDAOImpl topicManagementDAOImpl = new TopicManagementDAOImpl();

    @Test
    public void testAddTopic() throws TopicManagementException {

        topicManagementDAOImpl.addTopic(TOPIC, CHANNEL_URI, EVENT_PROFILE_VERSION, TENANT_ID);
        assertTrue(topicManagementDAOImpl.isTopicExists(TOPIC, TENANT_ID));
    }

    @Test(dependsOnMethods = {"testAddTopic"})
    public void testAddTopicDifferentTenants() throws TopicManagementException {

        topicManagementDAOImpl.addTopic(TOPIC, CHANNEL_URI, EVENT_PROFILE_VERSION, TENANT_ID + 1);
        assertTrue(topicManagementDAOImpl.isTopicExists(TOPIC, TENANT_ID));
        assertTrue(topicManagementDAOImpl.isTopicExists(TOPIC, TENANT_ID + 1));
    }

    @Test(dependsOnMethods = {"testAddTopic"})
    public void testIsTopicExistsWhenItExists() throws TopicManagementException {

        assertTrue(topicManagementDAOImpl.isTopicExists(TOPIC, TENANT_ID));
    }

    @Test
    public void testIsTopicExistsWhenItDoesNotExist() throws TopicManagementException {

        assertFalse(topicManagementDAOImpl.isTopicExists("nonexistent-topic", TENANT_ID));
    }

    @Test(dependsOnMethods = {"testAddTopic"})
    public void testDeleteTopic() throws TopicManagementException {

        topicManagementDAOImpl.addTopic(TOPIC2, CHANNEL_URI, EVENT_PROFILE_VERSION, TENANT_ID);
        assertTrue(topicManagementDAOImpl.isTopicExists(TOPIC2, TENANT_ID));
        topicManagementDAOImpl.deleteTopic(TOPIC2, TENANT_ID);
        assertFalse(topicManagementDAOImpl.isTopicExists(TOPIC2, TENANT_ID));
    }

    @Test(dependsOnMethods = {"testAddTopic"})
    public void testDeleteNonExistentTopic() throws TopicManagementException {

        topicManagementDAOImpl.deleteTopic("nonexistent-topic", TENANT_ID);
        assertFalse(topicManagementDAOImpl.isTopicExists("nonexistent-topic", TENANT_ID));
    }

    @Test(dependsOnMethods = {"testAddTopic"})
    public void testAddDuplicateTopic() {

        try {
            topicManagementDAOImpl.addTopic(TOPIC, CHANNEL_URI, EVENT_PROFILE_VERSION, TENANT_ID);
            fail("Expected TopicManagementException to be thrown");
        } catch (TopicManagementException e) {
            assertEquals(ErrorMessage.ERROR_CODE_TOPIC_ADD_ERROR.getCode(), e.getErrorCode());

            assertTrue(e.getDescription().contains(TOPIC), "Error message should contain the topic name");
        }
    }
}
