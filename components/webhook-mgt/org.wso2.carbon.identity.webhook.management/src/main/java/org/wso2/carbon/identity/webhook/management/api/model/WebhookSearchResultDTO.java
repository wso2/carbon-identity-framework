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

package org.wso2.carbon.identity.webhook.management.api.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Data Transfer Object for Webhook search results.
 * This class provides a secure representation of webhook search results
 * that doesn't include sensitive information like secrets.
 */
public class WebhookSearchResultDTO {

    private List<WebhookDTO> webhooks = new ArrayList<>();
    private int totalCount;

    /**
     * Default constructor.
     */
    public WebhookSearchResultDTO() {

    }

    /**
     * Constructor with WebhookSearchResult.
     * Automatically converts Webhook objects to WebhookDTO objects.
     *
     * @param result Original search result with Webhook objects.
     */
    public WebhookSearchResultDTO(WebhookSearchResult result) {

        if (result != null) {
            this.totalCount = result.getTotalCount();

            if (result.getWebhooks() != null) {
                for (Webhook webhook : result.getWebhooks()) {
                    this.webhooks.add(new WebhookDTO(webhook));
                }
            }
        }
    }

    /**
     * Constructor with webhooks and total count.
     *
     * @param webhooks   List of WebhookDTO objects.
     * @param totalCount Total count of WebhookDTO objects.
     */
    public WebhookSearchResultDTO(List<WebhookDTO> webhooks, int totalCount) {

        this.webhooks = webhooks;
        this.totalCount = totalCount;
    }

    /**
     * Get list of webhooks.
     *
     * @return List of WebhookDTO objects.
     */
    public List<WebhookDTO> getWebhooks() {

        return webhooks;
    }

    /**
     * Set list of webhooks.
     *
     * @param webhooks List of WebhookDTO objects.
     */
    public void setWebhooks(List<WebhookDTO> webhooks) {

        this.webhooks = webhooks;
    }

    /**
     * Get total count of webhooks.
     *
     * @return Total count of webhooks.
     */
    public int getTotalCount() {

        return totalCount;
    }

    /**
     * Set total count of webhooks.
     *
     * @param totalCount Total count of webhooks.
     */
    public void setTotalCount(int totalCount) {

        this.totalCount = totalCount;
    }
}
