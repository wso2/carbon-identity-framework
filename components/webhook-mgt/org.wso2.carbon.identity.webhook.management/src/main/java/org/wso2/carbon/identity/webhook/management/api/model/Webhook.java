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

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/**
 * Model class for Webhook subscription.
 */
public class Webhook {

    private String id;
    private String uuid;
    private String endpoint;
    private String description;
    private String secret;
    private int tenantId;
    private String eventSchemaName;
    private String eventSchemaUri;
    private WebhookStatus status; // Changed from String to WebhookStatus enum
    private Timestamp createdAt;
    private Timestamp updatedAt;
    private List<String> eventsSubscribed = new ArrayList<>();

    /**
     * Get webhook ID.
     *
     * @return Webhook ID.
     */
    public String getId() {

        return id;
    }

    /**
     * Get webhook UUID.
     *
     * @return Webhook UUID.
     */
    public String getUuid() {

        return uuid;
    }

    /**
     * Set webhook UUID.
     *
     * @param uuid Webhook UUID.
     */
    public void setUuid(String uuid) {

        this.uuid = uuid;
    }

    /**
     * Get tenant ID.
     *
     * @return Tenant ID.
     */
    public int getTenantId() {

        return tenantId;
    }

    /**
     * Set tenant ID.
     *
     * @param tenantId Tenant ID.
     */
    public void setTenantId(int tenantId) {

        this.tenantId = tenantId;
    }

    /**
     * Set webhook ID.
     *
     * @param id Webhook ID.
     */
    public void setId(String id) {

        this.id = id;
    }

    /**
     * Get webhook endpoint.
     *
     * @return Webhook endpoint.
     */
    public String getEndpoint() {

        return endpoint;
    }

    /**
     * Set webhook endpoint.
     *
     * @param endpoint Webhook endpoint.
     */
    public void setEndpoint(String endpoint) {

        this.endpoint = endpoint;
    }

    /**
     * Get webhook description.
     *
     * @return Webhook description.
     */
    public String getDescription() {

        return description;
    }

    /**
     * Set webhook description.
     *
     * @param description Webhook description.
     */
    public void setDescription(String description) {

        this.description = description;
    }

    /**
     * Get webhook secret.
     *
     * @return Webhook secret.
     */
    public String getSecret() {

        return secret;
    }

    /**
     * Set webhook secret.
     *
     * @param secret Webhook secret.
     */
    public void setSecret(String secret) {

        this.secret = secret;
    }

    /**
     * Get webhook event schema name.
     *
     * @return Webhook event schema name.
     */
    public String getEventSchemaName() {

        return eventSchemaName;
    }

    /**
     * Set webhook event schema name.
     *
     * @param eventSchemaName Webhook event schema name.
     */
    public void setEventSchemaName(String eventSchemaName) {

        this.eventSchemaName = eventSchemaName;
    }

    /**
     * Get webhook event schema URI.
     *
     * @return Webhook event schema URI.
     */
    public String getEventSchemaUri() {

        return eventSchemaUri;
    }

    /**
     * Set webhook event schema URI.
     *
     * @param eventSchemaUri Webhook event schema URI.
     */
    public void setEventSchemaUri(String eventSchemaUri) {

        this.eventSchemaUri = eventSchemaUri;
    }

    /**
     * Get webhook status.
     *
     * @return Webhook status.
     */
    public WebhookStatus getStatus() {

        return status;
    }

    /**
     * Set webhook status.
     *
     * @param status Webhook status.
     */
    public void setStatus(WebhookStatus status) {

        this.status = status;
    }

    /**
     * Set webhook status from string.
     *
     * @param statusString Status string.
     */
    public void setStatus(String statusString) {

        if (statusString != null) {
            try {
                this.status = WebhookStatus.valueOf(statusString);
            } catch (IllegalArgumentException e) {
                // Default to INACTIVE if invalid status
                this.status = WebhookStatus.INACTIVE;
            }
        }
    }

    /**
     * Get webhook status as string.
     *
     * @return Status string.
     */
    public String getStatusString() {

        return status != null ? status.name() : null;
    }

    /**
     * Get webhook created timestamp.
     *
     * @return Webhook created timestamp.
     */
    public Timestamp getCreatedAt() {

        return createdAt;
    }

    /**
     * Set webhook created timestamp.
     *
     * @param createdAt Webhook created timestamp.
     */
    public void setCreatedAt(Timestamp createdAt) {

        this.createdAt = createdAt;
    }

    /**
     * Get webhook updated timestamp.
     *
     * @return Webhook updated timestamp.
     */
    public Timestamp getUpdatedAt() {

        return updatedAt;
    }

    /**
     * Set webhook updated timestamp.
     *
     * @param updatedAt Webhook updated timestamp.
     */
    public void setUpdatedAt(Timestamp updatedAt) {

        this.updatedAt = updatedAt;
    }

    /**
     * Get list of events associated with the webhook.
     *
     * @return List of events.
     */
    public List<String> getEventsSubscribed() {

        return eventsSubscribed;
    }

    /**
     * Set list of events associated with the webhook.
     *
     * @param eventsSubscribed List of events.
     */
    public void setEventsSubscribed(List<String> eventsSubscribed) {

        this.eventsSubscribed = eventsSubscribed;
    }
}
