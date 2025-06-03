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

package org.wso2.carbon.identity.topic.management.internal.service.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;
import org.wso2.carbon.identity.topic.management.api.constant.ErrorMessage;
import org.wso2.carbon.identity.topic.management.api.exception.TopicManagementException;
import org.wso2.carbon.identity.topic.management.api.service.TopicManagementService;
import org.wso2.carbon.identity.topic.management.api.service.TopicManager;
import org.wso2.carbon.identity.topic.management.internal.component.TopicManagementComponentServiceHolder;
import org.wso2.carbon.identity.topic.management.internal.dao.TopicManagementDAO;
import org.wso2.carbon.identity.topic.management.internal.dao.impl.CacheBackedTopicManagementDAO;
import org.wso2.carbon.identity.topic.management.internal.dao.impl.TopicManagementDAOImpl;
import org.wso2.carbon.identity.topic.management.internal.util.TopicManagementExceptionHandler;

import java.util.List;

/**
 * Implementation of the TopicManagementService interface.
 * This class provides implementation for topic management operations.
 */
public class TopicManagementServiceImpl implements TopicManagementService {

    private static final Log LOG = LogFactory.getLog(TopicManagementServiceImpl.class);
    private static final TopicManagementServiceImpl topicManagementServiceImpl = new TopicManagementServiceImpl();
    private final TopicManagementDAO topicManagementDAO;
    //TODO: Get the topic manager name from a configuration
    private static final String ADAPTOR = "WEBSUBHUB";

    private TopicManagementServiceImpl() {

        topicManagementDAO = new CacheBackedTopicManagementDAO(new TopicManagementDAOImpl());
    }

    /**
     * Singleton instance of TopicManagementServiceImpl.
     *
     * @return Singleton instance.
     */
    public static TopicManagementServiceImpl getInstance() {

        return topicManagementServiceImpl;
    }

    /**
     * Constructs a topic from the channel URI.
     *
     * @param channelUri   The channel URI for the topic.
     * @param tenantDomain Tenant domain.
     * @return Constructed topic string.
     * @throws TopicManagementException If an error occurs during topic construction.
     */
    @Override
    public String constructTopic(String channelUri, String tenantDomain) throws TopicManagementException {

        if (channelUri == null || channelUri.trim().isEmpty()) {
            throw TopicManagementExceptionHandler.handleClientException(
                    ErrorMessage.ERROR_CODE_INVALID_CHANNEL_URI, "Channel URI cannot be null or empty");
        }

        List<TopicManager> managers =
                TopicManagementComponentServiceHolder.getInstance().getTopicManagers();
        if (managers.isEmpty()) {
            throw TopicManagementExceptionHandler.handleServerException(
                    ErrorMessage.ERROR_CODE_TOPIC_MANAGER_NOT_FOUND, tenantDomain);
        }

        for (TopicManager manager : managers) {
            if (ADAPTOR.equals(manager.getName())) {
                try {
                    return manager.constructTopic(channelUri, tenantDomain);
                } catch (TopicManagementException e) {
                    throw TopicManagementExceptionHandler.handleServerException(
                            ErrorMessage.ERROR_CODE_TOPIC_CONSTRUCT_ERROR, e, channelUri);
                }
            }
        }

        throw TopicManagementExceptionHandler.handleServerException(
                ErrorMessage.ERROR_CODE_TOPIC_MANAGER_NOT_FOUND, tenantDomain);
    }

    /**
     * Registers a topic in the system.
     *
     * @param topic               The topic to register.
     * @param channelUri          The channel URI associated with the topic.
     * @param eventProfileVersion The version of the event profile.
     * @param tenantDomain        Tenant domain.
     * @throws TopicManagementException If an error occurs during topic registration.
     */
    @Override
    public void registerTopic(String topic, String channelUri, String eventProfileVersion, String tenantDomain)
            throws TopicManagementException {

        if (topic == null || topic.trim().isEmpty()) {
            throw TopicManagementExceptionHandler.handleClientException(
                    ErrorMessage.ERROR_CODE_INVALID_TOPIC);
        }

        if (isTopicExists(topic, tenantDomain)) {
            throw TopicManagementExceptionHandler.handleClientException(
                    ErrorMessage.ERROR_CODE_TOPIC_ALREADY_EXISTS, topic);
        }

        List<TopicManager> managers =
                TopicManagementComponentServiceHolder.getInstance().getTopicManagers();
        if (managers.isEmpty()) {
            throw TopicManagementExceptionHandler.handleServerException(
                    ErrorMessage.ERROR_CODE_TOPIC_MANAGER_NOT_FOUND, tenantDomain);
        }

        for (TopicManager manager : managers) {
            if (ADAPTOR.equals(manager.getName())) {
                try {
                    manager.registerTopic(topic, tenantDomain);
                    int tenantId = IdentityTenantUtil.getTenantId(tenantDomain);
                    topicManagementDAO.addTopic(topic, channelUri, eventProfileVersion, tenantId);

                    LOG.debug("Topic registered successfully: " + topic + " for tenant: " + tenantDomain);
                    return; // Exit after successful registration
                } catch (TopicManagementException e) {
                    throw TopicManagementExceptionHandler.handleServerException(
                            ErrorMessage.ERROR_CODE_TOPIC_REGISTRATION_ERROR, e, topic);
                }
            }
        }

        throw TopicManagementExceptionHandler.handleServerException(
                ErrorMessage.ERROR_CODE_TOPIC_MANAGER_NOT_FOUND, tenantDomain);
    }

    /**
     * Deregisters a topic from the system.
     *
     * @param topic        The topic to deregister.
     * @param tenantDomain Tenant domain.
     * @throws TopicManagementException If an error occurs during topic deregistration.
     */
    @Override
    public void deregisterTopic(String topic, String tenantDomain) throws TopicManagementException {

        if (topic == null || topic.trim().isEmpty()) {
            throw TopicManagementExceptionHandler.handleClientException(
                    ErrorMessage.ERROR_CODE_INVALID_TOPIC);
        }
        LOG.debug("Topic deregistration initiated: " + topic + " for tenant domain: " + tenantDomain);
        List<TopicManager> managers =
                TopicManagementComponentServiceHolder.getInstance().getTopicManagers();
        if (managers.isEmpty()) {
            throw TopicManagementExceptionHandler.handleServerException(
                    ErrorMessage.ERROR_CODE_TOPIC_MANAGER_NOT_FOUND, tenantDomain);
        }

        for (TopicManager manager : managers) {
            if (ADAPTOR.equals(manager.getName())) {
                try {
                    manager.deregisterTopic(topic, tenantDomain);
                    int tenantId = IdentityTenantUtil.getTenantId(tenantDomain);
                    topicManagementDAO.deleteTopic(topic, tenantId);

                    LOG.debug("Topic deregistered successfully: " + topic + " for tenant: " + tenantDomain);
                    return; // Exit after successful deregistration
                } catch (TopicManagementException e) {
                    throw TopicManagementExceptionHandler.handleServerException(
                            ErrorMessage.ERROR_CODE_TOPIC_DEREGISTRATION_ERROR, e, topic);
                }
            }
        }

        throw TopicManagementExceptionHandler.handleServerException(
                ErrorMessage.ERROR_CODE_TOPIC_MANAGER_NOT_FOUND, tenantDomain);
    }

    /**
     * Check if a topic exists in the system.
     *
     * @param topic        The topic to check.
     * @param tenantDomain Tenant domain.
     * @return True if the topic exists, false otherwise.
     * @throws TopicManagementException If an error occurs while checking if the topic exists.
     */
    @Override
    public boolean isTopicExists(String topic, String tenantDomain) throws TopicManagementException {

        if (topic == null || topic.trim().isEmpty()) {
            throw TopicManagementExceptionHandler.handleClientException(
                    ErrorMessage.ERROR_CODE_INVALID_TOPIC);
        }

        try {
            int tenantId = IdentityTenantUtil.getTenantId(tenantDomain);
            boolean exists = topicManagementDAO.isTopicExists(topic, tenantId);

            LOG.debug("Checked existence of topic: " + topic + " for tenant domain: " + tenantDomain +
                    ". Exists: " + exists);

            return exists;
        } catch (Exception e) {
            throw TopicManagementExceptionHandler.handleServerException(
                    ErrorMessage.ERROR_CODE_TOPIC_EXISTS_CHECK_ERROR, e, topic);
        }
    }
}
