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

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Transfer Object for Webhook API responses.
 * This class provides a formatted webhook representation for API responses
 * that doesn't include sensitive information like secrets.
 */
public class WebhookDTO {

    private String id;
    private String createdAt;
    private String updatedAt;
    private String endpoint;
    private String version;
    private String eventSchemaName;
    private String eventSchemaUri;
    private String eventSchemaVersion;
    private String description;
    private List<String> eventsSubscribed = new ArrayList<>();
    private String status;

    /**
     * Constructor to create WebhookDTO from a Webhook entity.
     *
     * @param webhook The webhook entity to convert.
     */
    public WebhookDTO(Webhook webhook) {

        this.id = webhook.getId();

        if (webhook.getCreatedAt() != null) {
            Instant instant = webhook.getCreatedAt().toInstant();
            ZonedDateTime zdt = instant.atZone(ZoneId.of("UTC"));
            this.createdAt = zdt.format(DateTimeFormatter.ISO_INSTANT);
        }

        if (webhook.getUpdatedAt() != null) {
            Instant instant = webhook.getUpdatedAt().toInstant();
            ZonedDateTime zdt = instant.atZone(ZoneId.of("UTC"));
            this.updatedAt = zdt.format(DateTimeFormatter.ISO_INSTANT);
        }

        this.endpoint = webhook.getEndpoint();
        this.description = webhook.getDescription();

        this.version = webhook.getVersion();
        this.eventSchemaName = webhook.getEventSchemaName();
        this.eventSchemaUri = webhook.getEventSchemaUri();
        this.eventSchemaVersion = webhook.getEventSchemaVersion();

        if (webhook.getEventsSubscribed() != null) {
            this.eventsSubscribed.addAll(webhook.getEventsSubscribed());
        }

        this.status = webhook.getStatus();
    }

    /**
     * Get webhook ID.
     *
     * @return Webhook ID.
     */
    public String getId() {

        return id;
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
     * Get webhook created timestamp.
     *
     * @return Webhook created timestamp in ISO-8601 format.
     */
    public String getCreatedAt() {

        return createdAt;
    }

    /**
     * Set webhook created timestamp.
     *
     * @param createdAt Webhook created timestamp in ISO-8601 format.
     */
    public void setCreatedAt(String createdAt) {

        this.createdAt = createdAt;
    }

    /**
     * Get webhook updated timestamp.
     *
     * @return Webhook updated timestamp in ISO-8601 format.
     */
    public String getUpdatedAt() {

        return updatedAt;
    }

    /**
     * Set webhook updated timestamp.
     *
     * @param updatedAt Webhook updated timestamp in ISO-8601 format.
     */
    public void setUpdatedAt(String updatedAt) {

        this.updatedAt = updatedAt;
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
     * Get webhook version.
     *
     * @return Webhook version.
     */
    public String getVersion() {

        return version;
    }

    /**
     * Set webhook version.
     *
     * @param version Webhook version.
     */
    public void setVersion(String version) {

        this.version = version;
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
     * Get webhook event schema version.
     *
     * @return Webhook event schema version.
     */
    public String getEventSchemaVersion() {

        return eventSchemaVersion;
    }

    /**
     * Set webhook event schema version.
     *
     * @param eventSchemaVersion Webhook event schema version.
     */
    public void setEventSchemaVersion(String eventSchemaVersion) {

        this.eventSchemaVersion = eventSchemaVersion;
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
     * Get list of subscribed event names.
     *
     * @return List of event names.
     */
    public List<String> getEventsSubscribed() {

        return eventsSubscribed;
    }

    /**
     * Set list of subscribed event names.
     *
     * @param eventsSubscribed List of event names.
     */
    public void setEventsSubscribed(List<String> eventsSubscribed) {

        this.eventsSubscribed = eventsSubscribed;
    }

    /**
     * Get webhook status.
     *
     * @return Webhook status.
     */
    public String getStatus() {

        return status;
    }

    /**
     * Set webhook status.
     *
     * @param status Webhook status.
     */
    public void setStatus(String status) {

        this.status = status;
    }
}
