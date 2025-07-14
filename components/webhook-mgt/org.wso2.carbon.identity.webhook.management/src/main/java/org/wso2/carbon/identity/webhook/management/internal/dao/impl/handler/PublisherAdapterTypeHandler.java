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

package org.wso2.carbon.identity.webhook.management.internal.dao.impl.handler;

import org.wso2.carbon.database.utils.jdbc.NamedJdbcTemplate;
import org.wso2.carbon.identity.core.util.IdentityDatabaseUtil;
import org.wso2.carbon.identity.subscription.management.api.model.Subscription;
import org.wso2.carbon.identity.webhook.management.api.exception.WebhookMgtException;
import org.wso2.carbon.identity.webhook.management.api.model.Webhook;
import org.wso2.carbon.identity.webhook.management.internal.constant.ErrorMessage;
import org.wso2.carbon.identity.webhook.management.internal.dao.WebhookManagementDAO;
import org.wso2.carbon.identity.webhook.management.internal.util.WebhookManagementExceptionHandler;

import java.util.List;

/**
 * Handler for managing webhooks specifically for the Publisher Adapter type.
 * This class extends the AdapterTypeHandler to provide implementations for
 * webhook management operations such as creating, retrieving, updating, and deleting webhooks.
 */
public class PublisherAdapterTypeHandler extends AdapterTypeHandler {

    private final WebhookManagementDAO dao;

    public PublisherAdapterTypeHandler(WebhookManagementDAO dao) {

        this.dao = dao;
    }

    @Override
    public void createWebhook(Webhook webhook, int tenantId) throws WebhookMgtException {

        NamedJdbcTemplate jdbcTemplate = new NamedJdbcTemplate(IdentityDatabaseUtil.getDataSource());
        runTransaction(jdbcTemplate,
                () -> dao.createWebhook(encryptAddingWebhookSecrets(webhook), tenantId),
                ErrorMessage.ERROR_CODE_WEBHOOK_ADD_ERROR);
    }

    @Override
    public List<Webhook> getWebhooks(int tenantId) throws WebhookMgtException {

        return dao.getWebhooks(tenantId);
    }

    @Override
    public Webhook getWebhook(String webhookId, int tenantId) throws WebhookMgtException {

        return dao.getWebhook(webhookId, tenantId);
    }

    @Override
    public List<Subscription> getWebhookEvents(String webhookId, int tenantId) throws WebhookMgtException {

        return dao.getWebhookEvents(webhookId, tenantId);
    }

    @Override
    public void deleteWebhook(String webhookId, int tenantId) throws WebhookMgtException {

        Webhook existingWebhook = dao.getWebhook(webhookId, tenantId);
        NamedJdbcTemplate jdbcTemplate = new NamedJdbcTemplate(IdentityDatabaseUtil.getDataSource());
        runTransaction(jdbcTemplate, () -> {
            dao.deleteWebhook(webhookId, tenantId);
            deleteWebhookSecrets(existingWebhook);
        }, ErrorMessage.ERROR_CODE_WEBHOOK_DELETE_ERROR);
    }

    @Override
    public boolean isWebhookEndpointExists(String endpoint, int tenantId) throws WebhookMgtException {

        return dao.isWebhookEndpointExists(endpoint, tenantId);
    }

    @Override
    public void activateWebhook(Webhook webhook, int tenantId) throws WebhookMgtException {

        activateWebhook(webhook.getId(), tenantId);
    }

    @Override
    public void activateWebhook(String webhookId, int tenantId) throws WebhookMgtException {

        dao.activateWebhook(webhookId, tenantId);
    }

    @Override
    public void deactivateWebhook(Webhook webhook, int tenantId) throws WebhookMgtException {

        deactivateWebhook(webhook.getId(), tenantId);
    }

    @Override
    public void deactivateWebhook(String webhookId, int tenantId) throws WebhookMgtException {

        dao.deactivateWebhook(webhookId, tenantId);
    }

    @Override
    public void retryWebhook(Webhook webhook, int tenantId) throws WebhookMgtException {

        throw WebhookManagementExceptionHandler.handleClientException(
                ErrorMessage.ERROR_OPERATION_NOT_SUPPORTED, webhook.getId());
    }

    @Override
    public int getWebhooksCount(int tenantId) throws WebhookMgtException {

        return dao.getWebhooksCount(tenantId);
    }

    @Override
    public void updateWebhook(Webhook webhook, int tenantId) throws WebhookMgtException {

        dao.updateWebhook(webhook, tenantId);
    }
}
