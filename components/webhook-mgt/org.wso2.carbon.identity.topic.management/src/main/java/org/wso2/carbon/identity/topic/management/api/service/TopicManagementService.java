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

package org.wso2.carbon.identity.topic.management.api.service;

import org.wso2.carbon.identity.topic.management.api.exception.TopicManagementException;

/**
 * Service interface for topic management operations.
 */
public interface TopicManagementService {

    /**
     * Constructs a topic from the channel URI.
     *
     * @param channelUri   The channel URI for the topic.
     * @param tenantDomain Tenant domain.
     * @return Constructed topic string.
     * @throws TopicManagementException If an error occurs during topic construction.
     */
    public String constructTopic(String channelUri, String tenantDomain) throws TopicManagementException;

    /**
     * Registers a topic in the system.
     *
     * @param topic               The topic to register.
     * @param channelUri          The channel URI associated with the topic.
     * @param eventProfileVersion The version of the event profile.
     * @param tenantDomain        Tenant domain.
     * @throws TopicManagementException If an error occurs during topic registration.
     */
    public void registerTopic(String topic, String channelUri, String eventProfileVersion, String tenantDomain)
            throws TopicManagementException;

    /**
     * Deregisters a topic from the system and removes it from the database on success.
     *
     * @param topic        The topic to deregister.
     * @param tenantDomain Tenant domain.
     * @throws TopicManagementException If an error occurs during topic deregistration.
     */
    public void deregisterTopic(String topic, String tenantDomain) throws TopicManagementException;

    /**
     * Check if a topic exists in the system.
     *
     * @param topic        The topic to check.
     * @param tenantDomain Tenant domain.
     * @return True if the topic exists, false otherwise.
     * @throws TopicManagementException If an error occurs while checking if the topic exists.
     */
    public boolean isTopicExists(String topic, String tenantDomain) throws TopicManagementException;
}
