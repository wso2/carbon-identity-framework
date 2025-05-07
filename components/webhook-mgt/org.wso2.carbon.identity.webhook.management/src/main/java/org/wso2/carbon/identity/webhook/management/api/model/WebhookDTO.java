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

    private final String id;
    private final String createdAt;
    private final String updatedAt;
    private final String endpoint;
    private final String eventSchemaName;
    private final String eventSchemaUri;
    private final String description;
    private final List<String> eventsSubscribed;
    private final WebhookStatus status;

    /**
     * Private constructor used by the builder.
     *
     * @param builder Builder instance with configuration.
     */
    private WebhookDTO(Builder builder) {

        this.id = builder.id;
        this.createdAt = builder.createdAt;
        this.updatedAt = builder.updatedAt;
        this.endpoint = builder.endpoint;
        this.eventSchemaName = builder.eventSchemaName;
        this.eventSchemaUri = builder.eventSchemaUri;
        this.description = builder.description;
        this.eventsSubscribed = builder.eventsSubscribed;
        this.status = builder.status;
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
     * Get webhook created timestamp.
     *
     * @return Webhook created timestamp in ISO-8601 format.
     */
    public String getCreatedAt() {

        return createdAt;
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
     * Get webhook endpoint.
     *
     * @return Webhook endpoint.
     */
    public String getEndpoint() {

        return endpoint;
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
     * Get webhook event schema URI.
     *
     * @return Webhook event schema URI.
     */
    public String getEventSchemaUri() {

        return eventSchemaUri;
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
     * Get list of subscribed event names.
     *
     * @return List of event names.
     */
    public List<String> getEventsSubscribed() {

        return eventsSubscribed;
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
     * Get webhook status as string.
     *
     * @return Status string.
     */
    public String getStatusString() {

        return status != null ? status.name() : null;
    }

    /**
     * Builder class for WebhookDTO.
     */
    public static class Builder {

        private String id;
        private String createdAt;
        private String updatedAt;
        private String endpoint;
        private String eventSchemaName;
        private String eventSchemaUri;
        private String description;
        private List<String> eventsSubscribed = new ArrayList<>();
        private WebhookStatus status;

        /**
         * Default constructor.
         */
        public Builder() {

        }

        /**
         * Create a builder from an existing WebhookDTO.
         *
         * @param webhookDTO The webhook DTO to copy from.
         * @return Builder instance.
         */
        public static Builder fromWebhookDTO(WebhookDTO webhookDTO) {

            Builder builder = new Builder();

            if (webhookDTO == null) {
                return builder;
            }

            builder.id = webhookDTO.getId();
            builder.createdAt = webhookDTO.getCreatedAt();
            builder.updatedAt = webhookDTO.getUpdatedAt();
            builder.endpoint = webhookDTO.getEndpoint();
            builder.eventSchemaName = webhookDTO.getEventSchemaName();
            builder.eventSchemaUri = webhookDTO.getEventSchemaUri();
            builder.description = webhookDTO.getDescription();

            if (webhookDTO.getEventsSubscribed() != null) {
                builder.eventsSubscribed = new ArrayList<>(webhookDTO.getEventsSubscribed());
            }

            builder.status = webhookDTO.getStatus();
            return builder;
        }

        /**
         * Create a builder from an existing Webhook entity.
         *
         * @param webhook The webhook entity to convert.
         * @return Builder instance configured with webhook data.
         */
        public static Builder fromWebhook(Webhook webhook) {

            Builder builder = new Builder();

            if (webhook == null) {
                return builder;
            }

            builder.id = webhook.getId();

            if (webhook.getCreatedAt() != null) {
                Instant instant = webhook.getCreatedAt().toInstant();
                ZonedDateTime zdt = instant.atZone(ZoneId.of("UTC"));
                builder.createdAt = zdt.format(DateTimeFormatter.ISO_INSTANT);
            }

            if (webhook.getUpdatedAt() != null) {
                Instant instant = webhook.getUpdatedAt().toInstant();
                ZonedDateTime zdt = instant.atZone(ZoneId.of("UTC"));
                builder.updatedAt = zdt.format(DateTimeFormatter.ISO_INSTANT);
            }

            builder.endpoint = webhook.getEndpoint();
            builder.description = webhook.getDescription();
            builder.eventSchemaName = webhook.getEventSchemaName();
            builder.eventSchemaUri = webhook.getEventSchemaUri();
            builder.status = webhook.getStatus();

            if (webhook.getEventsSubscribed() != null) {
                builder.eventsSubscribed = new ArrayList<>(webhook.getEventsSubscribed());
            }

            return builder;
        }

        /**
         * Set webhook ID.
         *
         * @param id Webhook ID.
         * @return Builder instance.
         */
        public Builder setId(String id) {

            this.id = id;
            return this;
        }

        /**
         * Set webhook created timestamp.
         *
         * @param createdAt Webhook created timestamp in ISO-8601 format.
         * @return Builder instance.
         */
        public Builder setCreatedAt(String createdAt) {

            this.createdAt = createdAt;
            return this;
        }

        /**
         * Set webhook updated timestamp.
         *
         * @param updatedAt Webhook updated timestamp in ISO-8601 format.
         * @return Builder instance.
         */
        public Builder setUpdatedAt(String updatedAt) {

            this.updatedAt = updatedAt;
            return this;
        }

        /**
         * Set webhook endpoint.
         *
         * @param endpoint Webhook endpoint.
         * @return Builder instance.
         */
        public Builder setEndpoint(String endpoint) {

            this.endpoint = endpoint;
            return this;
        }

        /**
         * Set webhook event schema name.
         *
         * @param eventSchemaName Webhook event schema name.
         * @return Builder instance.
         */
        public Builder setEventSchemaName(String eventSchemaName) {

            this.eventSchemaName = eventSchemaName;
            return this;
        }

        /**
         * Set webhook event schema URI.
         *
         * @param eventSchemaUri Webhook event schema URI.
         * @return Builder instance.
         */
        public Builder setEventSchemaUri(String eventSchemaUri) {

            this.eventSchemaUri = eventSchemaUri;
            return this;
        }

        /**
         * Set webhook description.
         *
         * @param description Webhook description.
         * @return Builder instance.
         */
        public Builder setDescription(String description) {

            this.description = description;
            return this;
        }

        /**
         * Set list of subscribed event names.
         *
         * @param eventsSubscribed List of event names.
         * @return Builder instance.
         */
        public Builder setEventsSubscribed(List<String> eventsSubscribed) {

            if (eventsSubscribed != null) {
                this.eventsSubscribed = new ArrayList<>(eventsSubscribed);
            } else {
                this.eventsSubscribed = new ArrayList<>();
            }
            return this;
        }

        /**
         * Set webhook status.
         *
         * @param status Webhook status.
         * @return Builder instance.
         */
        public Builder setStatus(WebhookStatus status) {

            this.status = status;
            return this;
        }

        /**
         * Build the WebhookDTO object.
         *
         * @return WebhookDTO instance.
         */
        public WebhookDTO build() {

            return new WebhookDTO(this);
        }
    }
}
