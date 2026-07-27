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

package org.wso2.carbon.identity.topic.management.internal.dao;

import org.wso2.carbon.identity.topic.management.api.exception.TopicManagementException;

/**
 * Topic Management DAO Interface.
 * This interface defines the data access operations for Topic Management.
 */
public interface TopicManagementDAO {

    /**
     * Add a new topic.
     *
     * @param topic               Topic to be added.
     * @param channelUri          The channel URI associated with the topic.
     * @param eventProfileVersion The version of the event profile.
     * @param tenantId            Tenant ID.
     * @throws TopicManagementException If an error occurs while adding the topic to the database.
     */
    void addTopic(String topic, String channelUri, String eventProfileVersion, int tenantId)
            throws TopicManagementException;

    /**
     * Delete a topic.
     *
     * @param topic    Topic to be deleted.
     * @param tenantId Tenant ID.
     * @throws TopicManagementException If an error occurs while deleting the topic from the database.
     */
    void deleteTopic(String topic, int tenantId) throws TopicManagementException;

    /**
     * Check if a topic exists.
     *
     * @param topic    Topic to check.
     * @param tenantId Tenant ID.
     * @return True if the topic exists, false otherwise.
     * @throws TopicManagementException If an error occurs while checking if the topic exists.
     */
    boolean isTopicExists(String topic, int tenantId) throws TopicManagementException;
}
