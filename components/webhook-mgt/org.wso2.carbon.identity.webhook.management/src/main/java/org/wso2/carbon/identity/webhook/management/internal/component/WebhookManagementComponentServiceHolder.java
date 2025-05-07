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
import org.wso2.carbon.identity.webhook.management.api.service.WebhookSubscriber;
import org.wso2.carbon.identity.webhook.management.api.service.WebhookSubscriberService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Webhook Management Component Service Holder.
 * This class holds references to services required by the webhook management component.
 */
public class WebhookManagementComponentServiceHolder {

    private static final Log LOG = LogFactory.getLog(WebhookManagementComponentServiceHolder.class);
    private static final WebhookManagementComponentServiceHolder INSTANCE =
            new WebhookManagementComponentServiceHolder();
    private List<WebhookSubscriber> webhookSubscribers = new ArrayList<>();
    private WebhookSubscriberService webhookSubscriberService;

    private WebhookManagementComponentServiceHolder() {

    }

    public static WebhookManagementComponentServiceHolder getInstance() {

        return INSTANCE;
    }

    /**
     * Get all registered webhook subscribers.
     *
     * @return List of WebhookSubscriber instances.
     */
    public List<WebhookSubscriber> getWebhookSubscribers() {

        return Collections.unmodifiableList(webhookSubscribers);
    }

    /**
     * Add a webhook subscriber.
     *
     * @param webhookSubscriber WebhookSubscriber instance to add.
     */
    public void addWebhookSubscriber(WebhookSubscriber webhookSubscriber) {

        if (webhookSubscriber != null) {
            LOG.info("Adding webhook subscriber: " + webhookSubscriber.getName());
            webhookSubscribers.add(webhookSubscriber);
        }
    }

    /**
     * Remove a webhook subscriber.
     *
     * @param webhookSubscriber WebhookSubscriber instance to remove.
     */
    public void removeWebhookSubscriber(WebhookSubscriber webhookSubscriber) {

        if (webhookSubscriber != null) {
            LOG.info("Removing webhook subscriber: " + webhookSubscriber.getName());
            webhookSubscribers.remove(webhookSubscriber);
        }
    }

    /**
     * Get the WebhookSubscriberService.
     *
     * @return WebhookSubscriberService instance.
     */
    public WebhookSubscriberService getWebhookSubscriberService() {

        return webhookSubscriberService;
    }

    /**
     * Set the WebhookSubscriberService.
     *
     * @param webhookSubscriberService WebhookSubscriberService instance.
     */
    public void setWebhookSubscriberService(WebhookSubscriberService webhookSubscriberService) {

        this.webhookSubscriberService = webhookSubscriberService;
    }
}
