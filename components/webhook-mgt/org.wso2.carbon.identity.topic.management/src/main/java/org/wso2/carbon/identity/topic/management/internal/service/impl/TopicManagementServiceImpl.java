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
import org.osgi.annotation.bundle.Capability;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;
import org.wso2.carbon.identity.topic.management.api.exception.TopicManagementException;
import org.wso2.carbon.identity.topic.management.api.exception.TopicManagementServerException;
import org.wso2.carbon.identity.topic.management.api.service.TopicManagementService;
import org.wso2.carbon.identity.topic.management.api.service.TopicManager;
import org.wso2.carbon.identity.topic.management.internal.component.TopicManagementComponentServiceHolder;
import org.wso2.carbon.identity.topic.management.internal.constant.ErrorMessage;
import org.wso2.carbon.identity.topic.management.internal.dao.TopicManagementDAO;
import org.wso2.carbon.identity.topic.management.internal.dao.impl.CacheBackedTopicManagementDAO;
import org.wso2.carbon.identity.topic.management.internal.dao.impl.TopicManagementDAOImpl;
import org.wso2.carbon.identity.topic.management.internal.util.TopicManagementExceptionHandler;

import java.util.List;

/**
 * Implementation of the TopicManagementService interface.
 * This class provides implementation for topic management operations.
 */
@Capability(
        namespace = "osgi.service",
        attribute = {
                "objectClass=org.wso2.carbon.identity.topic.management.api.service.TopicManagementService",
                "service.scope=singleton"
        }
)
public class TopicManagementServiceImpl implements TopicManagementService {

    private static final Log LOG = LogFactory.getLog(TopicManagementServiceImpl.class);
    private static final TopicManagementServiceImpl topicManagementServiceImpl = new TopicManagementServiceImpl();
    private final TopicManagementDAO topicManagementDAO;
    private final String webhookAdapter;

    private TopicManagementServiceImpl() {

        topicManagementDAO = new CacheBackedTopicManagementDAO(new TopicManagementDAOImpl());
        webhookAdapter = TopicManagementComponentServiceHolder.getInstance()
                .getWebhookAdapter().getName();
    }

    /**
     * Singleton instance of TopicManagementServiceImpl.
     *
     * @return Singleton instance.
     */
    public static TopicManagementServiceImpl getInstance() {

        return topicManagementServiceImpl;
    }

    @Override
    public void registerTopic(String channelUri, String eventProfileName, String eventProfileVersion,
                              String tenantDomain) throws TopicManagementException {

        TopicManager adapterManager = retrieveAdapterManager(webhookAdapter);
        String topic = adapterManager.constructTopic(channelUri, eventProfileName, eventProfileVersion, tenantDomain);
        if (isTopicExists(channelUri, eventProfileName, eventProfileVersion, tenantDomain)) {
            throw TopicManagementExceptionHandler.handleClientException(
                    ErrorMessage.ERROR_CODE_TOPIC_ALREADY_EXISTS, topic);
        }
        try {
            adapterManager.registerTopic(topic, tenantDomain);
        } catch (TopicManagementException e) {
            throw TopicManagementExceptionHandler.handleServerException(
                    ErrorMessage.ERROR_CODE_TOPIC_REGISTRATION_ERROR, e, topic);
        }
        int tenantId = IdentityTenantUtil.getTenantId(tenantDomain);
        try {
            topicManagementDAO.addTopic(topic, channelUri, eventProfileVersion, tenantId);
        } catch (TopicManagementException e) {
            adapterManager.deregisterTopic(topic, tenantDomain);
            throw TopicManagementExceptionHandler.handleServerException(
                    ErrorMessage.ERROR_CODE_TOPIC_PERSISTENCE_ERROR, e, topic);
        }
        LOG.debug("Topic registered successfully: " + topic + " for tenant: " + tenantDomain);
    }

    @Override
    public void deregisterTopic(String channelUri, String eventProfileName, String eventProfileVersion,
                                String tenantDomain) throws TopicManagementException {

        TopicManager adapterManager = retrieveAdapterManager(webhookAdapter);
        String topic = adapterManager.constructTopic(channelUri, eventProfileName, eventProfileVersion, tenantDomain);
        LOG.debug("Topic deregistration initiated: " + topic + " for tenant domain: " + tenantDomain);
        try {
            adapterManager.deregisterTopic(topic, tenantDomain);
        } catch (TopicManagementException e) {
            throw TopicManagementExceptionHandler.handleServerException(
                    ErrorMessage.ERROR_CODE_TOPIC_DEREGISTRATION_ERROR, e, topic);
        }
        int tenantId = IdentityTenantUtil.getTenantId(tenantDomain);
        try {
            topicManagementDAO.deleteTopic(topic, tenantId);
        } catch (TopicManagementException e) {
            adapterManager.registerTopic(topic, tenantDomain);
            throw TopicManagementExceptionHandler.handleServerException(
                    ErrorMessage.ERROR_CODE_TOPIC_DELETION_ERROR, e, topic);
        }
        LOG.debug("Topic deregistered successfully: " + topic + " for tenant: " + tenantDomain);
    }

    /**
     * Check if a topic exists in the system.
     *
     * @param channelUri          The channel URI associated with the topic.
     * @param eventProfileName    The name of the event profile.
     * @param eventProfileVersion The version of the event profile.
     * @param tenantDomain        Tenant domain.
     * @throws TopicManagementException If an error occurs while checking if the topic exists.
     */
    @Override
    public boolean isTopicExists(String channelUri, String eventProfileName, String eventProfileVersion,
                                 String tenantDomain) throws TopicManagementException {

        TopicManager adapterManager = retrieveAdapterManager(webhookAdapter);
        String topic = adapterManager.constructTopic(channelUri, eventProfileName, eventProfileVersion, tenantDomain);
        if (topic == null || topic.trim().isEmpty()) {
            throw TopicManagementExceptionHandler.handleClientException(
                    ErrorMessage.ERROR_CODE_INVALID_TOPIC);
        }

        int tenantId = IdentityTenantUtil.getTenantId(tenantDomain);
        boolean exists = topicManagementDAO.isTopicExists(topic, tenantId);

        LOG.debug("Checked existence of topic: " + topic + " for tenant domain: " + tenantDomain +
                ". Exists: " + exists);

        return exists;
    }

    private TopicManager retrieveAdapterManager(String adapter) throws TopicManagementServerException {

        List<TopicManager> managers =
                TopicManagementComponentServiceHolder.getInstance().getTopicManagers();

        for (TopicManager manager : managers) {
            if (adapter.equals(manager.getAssociatedAdapter())) {
                return manager;
            }
        }

        throw TopicManagementExceptionHandler.handleServerException(
                ErrorMessage.ERROR_CODE_TOPIC_MANAGER_NOT_FOUND);
    }
}
