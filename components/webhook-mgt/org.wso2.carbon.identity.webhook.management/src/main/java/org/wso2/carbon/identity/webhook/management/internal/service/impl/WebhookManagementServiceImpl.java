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
import org.wso2.carbon.identity.webhook.management.api.exception.WebhookMgtClientException;
import org.wso2.carbon.identity.webhook.management.api.exception.WebhookMgtException;
import org.wso2.carbon.identity.webhook.management.api.model.Webhook;
import org.wso2.carbon.identity.webhook.management.api.model.WebhookStatus;
import org.wso2.carbon.identity.webhook.management.api.service.WebhookManagementService;
import org.wso2.carbon.identity.webhook.management.internal.constant.WebhookMgtConstants;
import org.wso2.carbon.identity.webhook.management.internal.dao.WebhookManagementDAO;
import org.wso2.carbon.identity.webhook.management.internal.dao.impl.CacheBackedWebhookManagementDAO;
import org.wso2.carbon.identity.webhook.management.internal.dao.impl.WebhookManagementDAOFacade;
import org.wso2.carbon.identity.webhook.management.internal.dao.impl.WebhookManagementDAOImpl;
import org.wso2.carbon.identity.webhook.management.internal.util.WebhookManagementExceptionHandler;
import org.wso2.carbon.identity.webhook.management.internal.util.WebhookValidator;

import java.util.List;
import java.util.UUID;

/**
 * Implementation of WebhookManagementService.
 * This class uses WebhookManagementFacade to handle webhook operations.
 * TODO: Check the supported event and schema using Meta data service
 */
public class WebhookManagementServiceImpl implements WebhookManagementService {

    private static final Log LOG = LogFactory.getLog(WebhookManagementServiceImpl.class);
    private static final WebhookManagementServiceImpl webhookManagementServiceImpl = new WebhookManagementServiceImpl();
    private final WebhookManagementDAO daoFACADE;
    private static final WebhookValidator WEBHOOK_VALIDATOR = new WebhookValidator();

    private WebhookManagementServiceImpl() {

        daoFACADE =
                new WebhookManagementDAOFacade(new CacheBackedWebhookManagementDAO(new WebhookManagementDAOImpl()));
    }

    public static WebhookManagementServiceImpl getInstance() {

        return webhookManagementServiceImpl;
    }

    @Override
    public Webhook createWebhook(Webhook webhook, String tenantDomain) throws WebhookMgtException {

        if (LOG.isDebugEnabled()) {
            LOG.debug(String.format("Creating webhook with endpoint: %s for tenant: %s",
                    webhook.getEndpoint(), tenantDomain));
        }
        int tenantId = IdentityTenantUtil.getTenantId(tenantDomain);
        if (daoFACADE.isWebhookEndpointExists(webhook.getEndpoint(), tenantId)) {
            throw WebhookManagementExceptionHandler.handleClientException(
                    ErrorMessage.ERROR_CODE_WEBHOOK_ENDPOINT_ALREADY_EXISTS, webhook.getEndpoint());
        }
        doPreAddWebhookValidations(webhook);
        String generatedWebhookId = UUID.randomUUID().toString();

        WebhookStatus status = webhook.getStatus() != null ? webhook.getStatus() : WebhookStatus.INACTIVE;

        Webhook webhookToCreate = new Webhook.Builder()
                .uuid(generatedWebhookId)
                .endpoint(webhook.getEndpoint())
                .name(webhook.getName())
                .secret(webhook.getSecret())
                .tenantId(tenantId)
                .eventSchemaName(webhook.getEventSchemaName())
                .eventSchemaUri(webhook.getEventSchemaUri())
                .status(status)
                .createdAt(webhook.getCreatedAt())
                .updatedAt(webhook.getUpdatedAt())
                .eventsSubscribed(webhook.getEventsSubscribed())
                .build();

        daoFACADE.createWebhook(webhookToCreate, tenantId);
        return daoFACADE.getWebhook(webhookToCreate.getUuid(), tenantId);
    }

    @Override
    public Webhook getWebhook(String webhookId, String tenantDomain) throws WebhookMgtException {

        if (LOG.isDebugEnabled()) {
            LOG.debug(String.format("Retrieving webhook with ID: %s for tenant: %s",
                    webhookId, tenantDomain));
        }
        int tenantId = IdentityTenantUtil.getTenantId(tenantDomain);
        return daoFACADE.getWebhook(webhookId, tenantId);
    }

    @Override
    public List<String> getWebhookEvents(String webhookId, String tenantDomain) throws WebhookMgtException {

        if (LOG.isDebugEnabled()) {
            LOG.debug(String.format("Getting events for webhook with ID: %s for tenant: %s",
                    webhookId, tenantDomain));
        }

        int tenantId = IdentityTenantUtil.getTenantId(tenantDomain);
        if (!isWebhookExists(webhookId, tenantId)) {
            throw WebhookManagementExceptionHandler.handleClientException(
                    ErrorMessage.ERROR_CODE_WEBHOOK_NOT_FOUND, webhookId);
        }
        return daoFACADE.getWebhookEvents(webhookId, tenantId);
    }

    @Override
    public Webhook updateWebhook(String webhookId, Webhook webhook, String tenantDomain) throws WebhookMgtException {

        if (LOG.isDebugEnabled()) {
            LOG.debug(String.format("Updating webhook with ID: %s for tenant: %s",
                    webhookId, tenantDomain));
        }
        int tenantId = IdentityTenantUtil.getTenantId(tenantDomain);
        if (!isWebhookExists(webhookId, tenantId)) {
            throw WebhookManagementExceptionHandler.handleClientException(
                    ErrorMessage.ERROR_CODE_WEBHOOK_NOT_FOUND, webhookId);
        }
        Webhook existingWebhook = daoFACADE.getWebhook(webhookId, tenantId);
        if (!existingWebhook.getEndpoint().equals(webhook.getEndpoint()) &&
                daoFACADE.isWebhookEndpointExists(webhook.getEndpoint(), tenantId)) {
            throw WebhookManagementExceptionHandler.handleClientException(
                    ErrorMessage.ERROR_CODE_WEBHOOK_ENDPOINT_ALREADY_EXISTS, webhook.getEndpoint());
        }
        Webhook webhookToUpdate = new Webhook.Builder()
                .uuid(webhookId)
                .endpoint(webhook.getEndpoint())
                .name(webhook.getName())
                .secret(webhook.getSecret() != null ? webhook.getSecret() : existingWebhook.getSecret())
                .tenantId(tenantId)
                .eventSchemaName(webhook.getEventSchemaName())
                .eventSchemaUri(webhook.getEventSchemaUri())
                .status(webhook.getStatus())
                .createdAt(existingWebhook.getCreatedAt())
                .updatedAt(webhook.getUpdatedAt())
                .eventsSubscribed(webhook.getEventsSubscribed())
                .build();
        doPreAddWebhookValidations(webhookToUpdate);
        daoFACADE.updateWebhook(webhookToUpdate, tenantId);
        return daoFACADE.getWebhook(webhookId, tenantId);
    }

    @Override
    public void deleteWebhook(String webhookId, String tenantDomain) throws WebhookMgtException {

        if (LOG.isDebugEnabled()) {
            LOG.debug(String.format("Deleting webhook with ID: %s for tenant: %s",
                    webhookId, tenantDomain));
        }
        int tenantId = IdentityTenantUtil.getTenantId(tenantDomain);
        if (!isWebhookExists(webhookId, tenantId)) {
            throw WebhookManagementExceptionHandler.handleClientException(
                    ErrorMessage.ERROR_CODE_WEBHOOK_NOT_FOUND, webhookId);
        }
        daoFACADE.deleteWebhook(webhookId, tenantId);
    }

    @Override
    public List<Webhook> getWebhooks(String tenantDomain) throws WebhookMgtException {

        if (LOG.isDebugEnabled()) {
            LOG.debug(String.format("Getting all webhooks for tenant: %s", tenantDomain));
        }
        int tenantId = IdentityTenantUtil.getTenantId(tenantDomain);

        return daoFACADE.getWebhooks(tenantId);
    }

    @Override
    public void activateWebhook(String webhookId, String tenantDomain) throws WebhookMgtException {

        if (LOG.isDebugEnabled()) {
            LOG.debug(String.format("Activating webhook with ID: %s for tenant: %s",
                    webhookId, tenantDomain));
        }
        int tenantId = IdentityTenantUtil.getTenantId(tenantDomain);
        if (!isWebhookExists(webhookId, tenantId)) {
            throw WebhookManagementExceptionHandler.handleClientException(
                    ErrorMessage.ERROR_CODE_WEBHOOK_NOT_FOUND, webhookId);
        }
        daoFACADE.activateWebhook(webhookId, tenantId);
    }

    @Override
    public void deactivateWebhook(String webhookId, String tenantDomain) throws WebhookMgtException {

        if (LOG.isDebugEnabled()) {
            LOG.debug(String.format("Deactivating webhook with ID: %s for tenant: %s",
                    webhookId, tenantDomain));
        }
        int tenantId = IdentityTenantUtil.getTenantId(tenantDomain);
        if (!isWebhookExists(webhookId, tenantId)) {
            throw WebhookManagementExceptionHandler.handleClientException(
                    ErrorMessage.ERROR_CODE_WEBHOOK_NOT_FOUND, webhookId);
        }
        daoFACADE.deactivateWebhook(webhookId, tenantId);
    }

    private boolean isWebhookExists(String webhookId, int tenantId) throws WebhookMgtException {

        return daoFACADE.getWebhook(webhookId, tenantId) != null;
    }

    /**
     * Perform pre validations on webhook model when creating an webhook.
     *
     * @param webhook Webhook creation model.
     * @throws WebhookMgtClientException if webhook model is invalid.
     */
    private void doPreAddWebhookValidations(Webhook webhook) throws WebhookMgtException {

        WEBHOOK_VALIDATOR.validateForBlank(WebhookMgtConstants.WEBHOOK_NAME_FIELD, webhook.getName());
        WEBHOOK_VALIDATOR.validateForBlank(WebhookMgtConstants.ENDPOINT_URI_FIELD, webhook.getEndpoint());
        WEBHOOK_VALIDATOR.validateForBlank(WebhookMgtConstants.EVENT_PROFILE_NAME_FIELD, webhook.getEventSchemaName());
        WEBHOOK_VALIDATOR.validateForBlank(WebhookMgtConstants.EVENT_PROFILE_URI_FIELD, webhook.getEventSchemaUri());
        WEBHOOK_VALIDATOR.validateForBlank(WebhookMgtConstants.STATUS_FIELD, String.valueOf(webhook.getStatus()));
        WEBHOOK_VALIDATOR.validateForBlank(WebhookMgtConstants.SECRET_FIELD, webhook.getSecret());
        WEBHOOK_VALIDATOR.validateWebhookName(webhook.getName());
        WEBHOOK_VALIDATOR.validateEndpointUri(webhook.getEndpoint());
        WEBHOOK_VALIDATOR.validateWebhookSecret(webhook.getSecret());
        WEBHOOK_VALIDATOR.validateChannelsSubscribed(webhook.getEventsSubscribed());
    }
}
