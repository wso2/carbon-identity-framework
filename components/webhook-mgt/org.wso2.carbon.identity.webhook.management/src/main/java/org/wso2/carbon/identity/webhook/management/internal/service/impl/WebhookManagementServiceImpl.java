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

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;
import org.wso2.carbon.identity.core.util.IdentityUtil;
import org.wso2.carbon.identity.subscription.management.api.model.Subscription;
import org.wso2.carbon.identity.webhook.management.api.exception.WebhookMgtException;
import org.wso2.carbon.identity.webhook.management.api.model.Webhook;
import org.wso2.carbon.identity.webhook.management.api.model.WebhookStatus;
import org.wso2.carbon.identity.webhook.management.api.service.WebhookManagementService;
import org.wso2.carbon.identity.webhook.management.internal.constant.ErrorMessage;
import org.wso2.carbon.identity.webhook.management.internal.constant.WebhookMgtConstants;
import org.wso2.carbon.identity.webhook.management.internal.dao.WebhookManagementDAO;
import org.wso2.carbon.identity.webhook.management.internal.dao.impl.CacheBackedWebhookManagementDAO;
import org.wso2.carbon.identity.webhook.management.internal.dao.impl.WebhookManagementDAOFacade;
import org.wso2.carbon.identity.webhook.management.internal.dao.impl.WebhookManagementDAOImpl;
import org.wso2.carbon.identity.webhook.management.internal.util.WebhookManagementAuditLogger;
import org.wso2.carbon.identity.webhook.management.internal.util.WebhookManagementExceptionHandler;
import org.wso2.carbon.identity.webhook.management.internal.util.WebhookValidator;

import java.util.List;
import java.util.UUID;

/**
 * Implementation of WebhookManagementService.
 * This class uses WebhookManagementFacade to handle webhook operations.
 */
public class WebhookManagementServiceImpl implements WebhookManagementService {

    private static final Log LOG = LogFactory.getLog(WebhookManagementServiceImpl.class);
    private static final WebhookManagementServiceImpl webhookManagementServiceImpl =
            new WebhookManagementServiceImpl();
    private final WebhookManagementDAO daoFACADE;
    private static final WebhookValidator WEBHOOK_VALIDATOR = new WebhookValidator();
    private static final WebhookManagementAuditLogger auditLogger = new WebhookManagementAuditLogger();

    private WebhookManagementServiceImpl() {

        daoFACADE = new WebhookManagementDAOFacade(new CacheBackedWebhookManagementDAO(new WebhookManagementDAOImpl()));
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
        validateMaxWebhooksCount(tenantDomain);
        doPreAddWebhookValidations(webhook);
        String generatedWebhookId = UUID.randomUUID().toString();

        WebhookStatus status = webhook.getStatus() != null ? webhook.getStatus() : WebhookStatus.INACTIVE;

        Webhook webhookToCreate = new Webhook.Builder()
                .uuid(generatedWebhookId)
                .endpoint(webhook.getEndpoint())
                .name(webhook.getName())
                .secret(webhook.getSecret())
                .eventProfileName(webhook.getEventProfileName())
                .eventProfileUri(webhook.getEventProfileUri())
                .eventProfileVersion(webhook.getEventProfileVersion())
                .status(status)
                .createdAt(webhook.getCreatedAt())
                .updatedAt(webhook.getUpdatedAt())
                .eventsSubscribed(webhook.getEventsSubscribed())
                .build();

        daoFACADE.createWebhook(webhookToCreate, tenantId);
        auditLogger.printAuditLog(WebhookManagementAuditLogger.Operation.ADD, webhookToCreate);
        return daoFACADE.getWebhook(webhookToCreate.getId(), tenantId);
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
    public List<Subscription> getWebhookEvents(String webhookId, String tenantDomain)
            throws WebhookMgtException {

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
    public Webhook updateWebhook(String webhookId, Webhook webhook, String tenantDomain)
            throws WebhookMgtException {

        int tenantId = IdentityTenantUtil.getTenantId(tenantDomain);
        if (!isWebhookExists(webhookId, tenantId)) {
            throw WebhookManagementExceptionHandler.handleClientException(
                    ErrorMessage.ERROR_CODE_WEBHOOK_NOT_FOUND, webhookId);
        }
        doPreUpdateWebhookValidations(webhook);
        daoFACADE.updateWebhook(webhook, tenantId);
        auditLogger.printAuditLog(WebhookManagementAuditLogger.Operation.UPDATE, webhook);
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
        auditLogger.printAuditLog(WebhookManagementAuditLogger.Operation.DELETE, webhookId);
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
    public Webhook activateWebhook(String webhookId, String tenantDomain) throws WebhookMgtException {

        if (LOG.isDebugEnabled()) {
            LOG.debug(String.format("Activating webhook with ID: %s for tenant: %s",
                    webhookId, tenantDomain));
        }
        int tenantId = IdentityTenantUtil.getTenantId(tenantDomain);
        Webhook existingWebhook = daoFACADE.getWebhook(webhookId, tenantId);
        if (existingWebhook == null) {
            throw WebhookManagementExceptionHandler.handleClientException(
                    ErrorMessage.ERROR_CODE_WEBHOOK_NOT_FOUND, webhookId);
        }
        daoFACADE.activateWebhook(existingWebhook, tenantId);
        auditLogger.printAuditLog(WebhookManagementAuditLogger.Operation.ACTIVATE, webhookId);
        return daoFACADE.getWebhook(webhookId, tenantId);
    }

    @Override
    public Webhook deactivateWebhook(String webhookId, String tenantDomain) throws WebhookMgtException {

        if (LOG.isDebugEnabled()) {
            LOG.debug(String.format("Deactivating webhook with ID: %s for tenant: %s",
                    webhookId, tenantDomain));
        }
        int tenantId = IdentityTenantUtil.getTenantId(tenantDomain);
        Webhook existingWebhook = daoFACADE.getWebhook(webhookId, tenantId);
        if (existingWebhook == null) {
            throw WebhookManagementExceptionHandler.handleClientException(
                    ErrorMessage.ERROR_CODE_WEBHOOK_NOT_FOUND, webhookId);
        }
        daoFACADE.deactivateWebhook(existingWebhook, tenantId);
        auditLogger.printAuditLog(WebhookManagementAuditLogger.Operation.DEACTIVATE, webhookId);
        return daoFACADE.getWebhook(webhookId, tenantId);
    }

    @Override
    public Webhook retryWebhook(String webhookId, String tenantDomain) throws WebhookMgtException {

        if (LOG.isDebugEnabled()) {
            LOG.debug(String.format("Retrying webhook with ID: %s for tenant: %s",
                    webhookId, tenantDomain));
        }
        int tenantId = IdentityTenantUtil.getTenantId(tenantDomain);
        Webhook existingWebhook = daoFACADE.getWebhook(webhookId, tenantId);
        if (existingWebhook == null) {
            throw WebhookManagementExceptionHandler.handleClientException(
                    ErrorMessage.ERROR_CODE_WEBHOOK_NOT_FOUND, webhookId);
        }
        daoFACADE.retryWebhook(existingWebhook, tenantId);
        return daoFACADE.getWebhook(webhookId, tenantId);
    }

    @Override
    public List<Webhook> getActiveWebhooks(String eventProfileName, String eventProfileVersion, String channelUri,
                                           String tenantDomain) throws WebhookMgtException {

        if (LOG.isDebugEnabled()) {
            LOG.debug(String.format("Retrieving active webhooks for channel URI: %s in tenant: %s",
                    channelUri, tenantDomain));
        }
        int tenantId = IdentityTenantUtil.getTenantId(tenantDomain);
        return daoFACADE.getActiveWebhooks(eventProfileName, eventProfileVersion, channelUri, tenantId);
    }

    private boolean isWebhookExists(String webhookId, int tenantId) throws WebhookMgtException {

        return daoFACADE.getWebhook(webhookId, tenantId) != null;
    }

    // Common validation for required fields except secret
    private void validateCommonWebhookFields(Webhook webhook) throws WebhookMgtException {

        WEBHOOK_VALIDATOR.validateForBlank(WebhookMgtConstants.WEBHOOK_NAME_FIELD, webhook.getName());
        WEBHOOK_VALIDATOR.validateForBlank(WebhookMgtConstants.ENDPOINT_URI_FIELD, webhook.getEndpoint());
        WEBHOOK_VALIDATOR.validateForBlank(WebhookMgtConstants.EVENT_PROFILE_NAME_FIELD, webhook.getEventProfileName());
        WEBHOOK_VALIDATOR.validateForBlank(WebhookMgtConstants.EVENT_PROFILE_URI_FIELD, webhook.getEventProfileUri());
        WEBHOOK_VALIDATOR.validateForBlank(WebhookMgtConstants.STATUS_FIELD, String.valueOf(webhook.getStatus()));
        WEBHOOK_VALIDATOR.validateWebhookName(webhook.getName());
        WEBHOOK_VALIDATOR.validateEndpointUri(webhook.getEndpoint());
        WEBHOOK_VALIDATOR.validateChannelsSubscribed(webhook.getEventProfileName(), webhook.getEventsSubscribed());
    }

    private void doPreAddWebhookValidations(Webhook webhook) throws WebhookMgtException {

        validateCommonWebhookFields(webhook);
        WEBHOOK_VALIDATOR.validateForBlank(WebhookMgtConstants.SECRET_FIELD, webhook.getSecret());
        WEBHOOK_VALIDATOR.validateWebhookSecret(webhook.getSecret());
    }

    private void doPreUpdateWebhookValidations(Webhook webhook) throws WebhookMgtException {

        validateCommonWebhookFields(webhook);
        // Secret is optional for update
        if (StringUtils.isNotBlank(webhook.getSecret())) {
            WEBHOOK_VALIDATOR.validateWebhookSecret(webhook.getSecret());
        }
    }

    private void validateMaxWebhooksCount(String tenantDomain) throws WebhookMgtException {

        LOG.debug("Retrieving webhook count for tenant: " + tenantDomain);
        int webhooksCount = daoFACADE.getWebhooksCount(IdentityTenantUtil.getTenantId(tenantDomain));
        int maxWebhooksCount = IdentityUtil.getMaximumWebhooksPerTenant();
        if (webhooksCount >= maxWebhooksCount) {
            throw WebhookManagementExceptionHandler.handleClientException(
                    ErrorMessage.ERROR_MAXIMUM_WEBHOOKS_PER_TENANT_REACHED, String.valueOf(maxWebhooksCount));
        }
    }
}
