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
 * Model class for paginated webhook search results.
 */
public class WebhookSearchResult {

    private List<Webhook> webhooks = new ArrayList<>();
    private int totalCount;
    
    /**
     * Default constructor.
     */
    public WebhookSearchResult() {
    }
    
    /**
     * Constructor with webhooks and total count.
     *
     * @param webhooks   List of webhooks.
     * @param totalCount Total count of webhooks.
     */
    public WebhookSearchResult(List<Webhook> webhooks, int totalCount) {
        this.webhooks = webhooks;
        this.totalCount = totalCount;
    }
    
    /**
     * Get list of webhooks.
     *
     * @return List of webhooks.
     */
    public List<Webhook> getWebhooks() {
        return webhooks;
    }
    
    /**
     * Set list of webhooks.
     *
     * @param webhooks List of webhooks.
     */
    public void setWebhooks(List<Webhook> webhooks) {
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
