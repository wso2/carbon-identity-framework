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

package org.wso2.carbon.identity.webhook.management.internal.service.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;
import org.wso2.carbon.identity.webhook.management.api.constant.ErrorMessage;
import org.wso2.carbon.identity.webhook.management.api.exception.WebhookMgtException;
import org.wso2.carbon.identity.webhook.management.api.model.Webhook;
import org.wso2.carbon.identity.webhook.management.api.model.WebhookDTO;
import org.wso2.carbon.identity.webhook.management.api.model.WebhookSearchResultDTO;
import org.wso2.carbon.identity.webhook.management.api.service.WebhookManagementService;
import org.wso2.carbon.identity.webhook.management.internal.dao.WebhookManagementDAO;
import org.wso2.carbon.identity.webhook.management.internal.dao.impl.WebhookManagementDAOImpl;
import org.wso2.carbon.identity.webhook.management.internal.dao.impl.WebhookManagementFacade;
import org.wso2.carbon.identity.webhook.management.internal.util.WebhookManagementExceptionHandler;

import java.util.UUID;

/**
 * Implementation of WebhookManagementService.
 * This class uses WebhookManagementFacade to handle webhook operations.
 */
public class WebhookManagementServiceImpl implements WebhookManagementService {

    private static final Log LOG = LogFactory.getLog(WebhookManagementServiceImpl.class);
    private static final WebhookManagementDAO DAO_FACADE =
            new WebhookManagementFacade(new WebhookManagementDAOImpl());

    @Override
    public WebhookDTO createWebhook(Webhook webhook, String tenantDomain) throws WebhookMgtException {

        if (LOG.isDebugEnabled()) {
            LOG.debug(String.format("Creating webhook with endpoint: %s for tenant: %s",
                    webhook.getEndpoint(), tenantDomain));
        }
        int tenantId = IdentityTenantUtil.getTenantId(tenantDomain);
        if (DAO_FACADE.isWebhookEndpointExists(webhook.getEndpoint(), tenantId)) {
            throw WebhookManagementExceptionHandler.handleClientException(
                    ErrorMessage.ERROR_CODE_WEBHOOK_ENDPOINT_ALREADY_EXISTS, webhook.getEndpoint());
        }
        String generatedWebhookId = UUID.randomUUID().toString();
        webhook.setId(generatedWebhookId);
        DAO_FACADE.createWebhook(webhook, tenantId);
        return new WebhookDTO(DAO_FACADE.getWebhook(webhook.getId(), tenantId));
    }

    @Override
    public WebhookDTO getWebhook(String webhookId, String tenantDomain) throws WebhookMgtException {

        if (LOG.isDebugEnabled()) {
            LOG.debug(String.format("Retrieving webhook with ID: %s for tenant: %s",
                    webhookId, tenantDomain));
        }
        int tenantId = IdentityTenantUtil.getTenantId(tenantDomain);
        return new WebhookDTO(DAO_FACADE.getWebhook(webhookId, tenantId));
    }

    @Override
    public WebhookDTO updateWebhook(String webhookId, Webhook webhook, String tenantDomain) throws WebhookMgtException {

        if (LOG.isDebugEnabled()) {
            LOG.debug(String.format("Updating webhook with ID: %s for tenant: %s",
                    webhookId, tenantDomain));
        }
        int tenantId = IdentityTenantUtil.getTenantId(tenantDomain);
        DAO_FACADE.updateWebhook(webhookId, webhook, tenantId);
        return new WebhookDTO(DAO_FACADE.getWebhook(webhookId, tenantId));
    }

    @Override
    public void deleteWebhook(String webhookId, String tenantDomain) throws WebhookMgtException {

        if (LOG.isDebugEnabled()) {
            LOG.debug(String.format("Deleting webhook with ID: %s for tenant: %s",
                    webhookId, tenantDomain));
        }
        int tenantId = IdentityTenantUtil.getTenantId(tenantDomain);
        DAO_FACADE.deleteWebhook(webhookId, tenantId);
    }

    @Override
    public WebhookSearchResultDTO listWebhooks(String status, int limit, int offset, String tenantDomain)
            throws WebhookMgtException {

        if (LOG.isDebugEnabled()) {
            LOG.debug(String.format("Listing webhooks with status: %s for tenant: %s",
                    status, tenantDomain));
        }
        int tenantId = IdentityTenantUtil.getTenantId(tenantDomain);
        return new WebhookSearchResultDTO(DAO_FACADE.listWebhooks(status, limit, offset, tenantId));
    }

    @Override
    public void activateWebhook(String webhookId, String tenantDomain) throws WebhookMgtException {

        if (LOG.isDebugEnabled()) {
            LOG.debug(String.format("Activating webhook with ID: %s for tenant: %s",
                    webhookId, tenantDomain));
        }
        int tenantId = IdentityTenantUtil.getTenantId(tenantDomain);
        DAO_FACADE.activateWebhook(webhookId, tenantId);
    }

    @Override
    public void deactivateWebhook(String webhookId, String tenantDomain) throws WebhookMgtException {

        if (LOG.isDebugEnabled()) {
            LOG.debug(String.format("Deactivating webhook with ID: %s for tenant: %s",
                    webhookId, tenantDomain));
        }
        int tenantId = IdentityTenantUtil.getTenantId(tenantDomain);
        DAO_FACADE.deactivateWebhook(webhookId, tenantId);
    }
}
