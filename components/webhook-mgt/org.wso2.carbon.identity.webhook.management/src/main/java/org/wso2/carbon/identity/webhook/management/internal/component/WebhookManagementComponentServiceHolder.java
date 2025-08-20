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

package org.wso2.carbon.identity.webhook.management.internal.component;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.secret.mgt.core.SecretManager;
import org.wso2.carbon.identity.secret.mgt.core.SecretResolveManager;
import org.wso2.carbon.identity.subscription.management.api.service.SubscriptionManagementService;
import org.wso2.carbon.identity.topic.management.api.service.TopicManagementService;
import org.wso2.carbon.identity.webhook.metadata.api.model.Adapter;
import org.wso2.carbon.identity.webhook.metadata.api.service.EventAdapterMetadataService;
import org.wso2.carbon.identity.webhook.metadata.api.service.WebhookMetadataService;

/**
 * Webhook Management Component Service Holder.
 * This class holds references to services required by the webhook management component.
 */
public class WebhookManagementComponentServiceHolder {

    private static final Log LOG = LogFactory.getLog(WebhookManagementComponentServiceHolder.class);
    private static final WebhookManagementComponentServiceHolder INSTANCE =
            new WebhookManagementComponentServiceHolder();
    private SecretManager secretManager;
    private SecretResolveManager secretResolveManager;
    private TopicManagementService topicManagementService;
    private SubscriptionManagementService subscriptionManagementService;
    private WebhookMetadataService webhookMetadataService;
    private EventAdapterMetadataService eventAdapterMetadataService;
    private Adapter webhookAdapter;

    private WebhookManagementComponentServiceHolder() {

    }

    public static WebhookManagementComponentServiceHolder getInstance() {

        return INSTANCE;
    }

    /**
     * Get the SecretManager.
     *
     * @return SecretManager instance.
     */
    public SecretManager getSecretManager() {

        return secretManager;
    }

    /**
     * Set the SecretManager.
     *
     * @param secretManager SecretManager instance.
     */
    public void setSecretManager(SecretManager secretManager) {

        this.secretManager = secretManager;
    }

    /**
     * Get the SecretResolveManager.
     *
     * @return SecretResolveManager instance.
     */
    public SecretResolveManager getSecretResolveManager() {

        return secretResolveManager;
    }

    /**
     * Set the SecretResolveManager.
     *
     * @param secretResolveManager SecretResolveManager instance.
     */
    public void setSecretResolveManager(SecretResolveManager secretResolveManager) {

        this.secretResolveManager = secretResolveManager;
    }

    /**
     * Get the TopicManagementService.
     *
     * @return TopicManagementService instance.
     */
    public TopicManagementService getTopicManagementService() {

        return topicManagementService;
    }

    /**
     * Set the TopicManagementService.
     *
     * @param topicManagementService TopicManagementService instance.
     */
    public void setTopicManagementService(TopicManagementService topicManagementService) {

        this.topicManagementService = topicManagementService;
    }

    /**
     * Get the SubscriptionManagementService.
     *
     * @return SubscriptionManagementService instance.
     */
    public SubscriptionManagementService getSubscriptionManagementService() {

        return subscriptionManagementService;
    }

    /**
     * Set the SubscriptionManagementService.
     *
     * @param subscriptionManagementService SubscriptionManagementService instance.
     */
    public void setSubscriptionManagementService(SubscriptionManagementService subscriptionManagementService) {

        this.subscriptionManagementService = subscriptionManagementService;
    }

    /**
     * Get the webhook metadata service.
     *
     * @return Webhook metadata service.
     */
    public WebhookMetadataService getWebhookMetadataService() {

        return webhookMetadataService;
    }

    /**
     * Set the webhook metadata service.
     *
     * @param webhookMetadataService Webhook metadata service.
     */
    public void setWebhookMetadataService(WebhookMetadataService webhookMetadataService) {

        this.webhookMetadataService = webhookMetadataService;
    }

    /**
     * Get the event adapter metadata service.
     *
     * @return EventAdapterMetadataService instance.
     */
    public EventAdapterMetadataService getEventAdapterMetadataService() {

        return eventAdapterMetadataService;
    }

    /**
     * Set the event adapter metadata service.
     *
     * @param eventAdapterMetadataService EventAdapterMetadataService instance.
     */
    public void setEventAdapterMetadataService(EventAdapterMetadataService eventAdapterMetadataService) {

        this.eventAdapterMetadataService = eventAdapterMetadataService;
    }

    /**
     * Get the webhook adapter.
     *
     * @return WebhookAdapter instance.
     */
    public Adapter getWebhookAdapter() {

        return webhookAdapter;
    }

    /**
     * Set the webhook adapter.
     *
     * @param webhookAdapter WebhookAdapter instance.
     */
    public void setWebhookAdapter(Adapter webhookAdapter) {

        this.webhookAdapter = webhookAdapter;
        LOG.debug("Webhook adapter set to type " + webhookAdapter.getType());
    }
}
