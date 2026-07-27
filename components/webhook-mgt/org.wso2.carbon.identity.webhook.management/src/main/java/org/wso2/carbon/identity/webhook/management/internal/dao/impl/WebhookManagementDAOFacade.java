/*
Copyright (c) 2025, WSO2 LLC. (http://www.wso2.com).

WSO2 LLC. licenses this file to you under the Apache License,
Version 2.0 (the "License"); you may not use this file except
in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing,
software distributed under the License is distributed on an
"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
KIND, either express or implied. See the License for the
specific language governing permissions and limitations
under the License. */

package org.wso2.carbon.identity.webhook.management.internal.dao.impl;

import org.wso2.carbon.identity.subscription.management.api.model.Subscription;
import org.wso2.carbon.identity.webhook.management.api.exception.WebhookMgtClientException;
import org.wso2.carbon.identity.webhook.management.api.exception.WebhookMgtException;
import org.wso2.carbon.identity.webhook.management.api.model.Webhook;
import org.wso2.carbon.identity.webhook.management.internal.component.WebhookManagementComponentServiceHolder;
import org.wso2.carbon.identity.webhook.management.internal.constant.ErrorMessage;
import org.wso2.carbon.identity.webhook.management.internal.dao.WebhookManagementDAO;
import org.wso2.carbon.identity.webhook.management.internal.dao.impl.handler.AdapterTypeHandler;
import org.wso2.carbon.identity.webhook.management.internal.dao.impl.handler.WebhookAdapterTypeHandlerFactory;
import org.wso2.carbon.identity.webhook.management.internal.util.WebhookManagementExceptionHandler;

import java.util.List;

/**
 * Facade for WebhookManagementDAO to handle webhook management operations.
 */
public class WebhookManagementDAOFacade implements WebhookManagementDAO {

    private final AdapterTypeHandler handler;

    public WebhookManagementDAOFacade(WebhookManagementDAO webhookManagementDAO) {

        this.handler = WebhookAdapterTypeHandlerFactory.getHandler(webhookManagementDAO);
    }

    private AdapterTypeHandler getHandler() throws WebhookMgtClientException {

        if (handler == null) {
            throw WebhookManagementExceptionHandler.handleClientException(
                    ErrorMessage.ERROR_OPERATION_NOT_SUPPORTED, String.valueOf(
                            WebhookManagementComponentServiceHolder.getInstance().getWebhookAdapter()));
        }
        return handler;
    }

    @Override
    public void createWebhook(Webhook webhook, int tenantId) throws WebhookMgtException {

        getHandler().createWebhook(webhook, tenantId);
    }

    @Override
    public List<Webhook> getWebhooks(int tenantId) throws WebhookMgtException {

        return getHandler().getWebhooks(tenantId);
    }

    @Override
    public Webhook getWebhook(String webhookId, int tenantId) throws WebhookMgtException {

        return getHandler().getWebhook(webhookId, tenantId);
    }

    @Override
    public List<Subscription> getWebhookEvents(String webhookId, int tenantId) throws WebhookMgtException {

        return getHandler().getWebhookEvents(webhookId, tenantId);
    }

    @Override
    public void deleteWebhook(String webhookId, int tenantId) throws WebhookMgtException {

        getHandler().deleteWebhook(webhookId, tenantId);
    }

    @Override
    public boolean isWebhookEndpointExists(String endpoint, int tenantId) throws WebhookMgtException {

        return getHandler().isWebhookEndpointExists(endpoint, tenantId);
    }

    @Override
    public void activateWebhook(Webhook webhook, int tenantId) throws WebhookMgtException {

        getHandler().activateWebhook(webhook, tenantId);
    }

    @Override
    public void deactivateWebhook(Webhook webhook, int tenantId) throws WebhookMgtException {

        getHandler().deactivateWebhook(webhook, tenantId);
    }

    @Override
    public void retryWebhook(Webhook webhook, int tenantId) throws WebhookMgtException {

        getHandler().retryWebhook(webhook, tenantId);
    }

    @Override
    public int getWebhooksCount(int tenantId) throws WebhookMgtException {

        return getHandler().getWebhooksCount(tenantId);
    }

    @Override
    public void updateWebhook(Webhook webhook, int tenantId) throws WebhookMgtException {

        getHandler().updateWebhook(webhook, tenantId);
    }

    @Override
    public List<Webhook> getActiveWebhooks(String eventProfileName, String eventProfileVersion, String channelUri,
                                           int tenantId) throws WebhookMgtException {

        return getHandler().getActiveWebhooks(eventProfileName, eventProfileVersion, channelUri, tenantId);
    }
}
