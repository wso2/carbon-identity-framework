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

/**
 * Data Transfer Object for Webhook API responses without eventsSubscribed.
 */
public class WebhookSummaryDTO {

    private final String id;
    private final String createdAt;
    private final String updatedAt;
    private final String endpoint;
    private final String eventSchemaName;
    private final String eventSchemaUri;
    private final String description;
    private final WebhookStatus status;

    private WebhookSummaryDTO(Builder builder) {

        this.id = builder.id;
        this.createdAt = builder.createdAt;
        this.updatedAt = builder.updatedAt;
        this.endpoint = builder.endpoint;
        this.eventSchemaName = builder.eventSchemaName;
        this.eventSchemaUri = builder.eventSchemaUri;
        this.description = builder.description;
        this.status = builder.status;
    }

    public String getId() {

        return id;
    }

    public String getCreatedAt() {

        return createdAt;
    }

    public String getUpdatedAt() {

        return updatedAt;
    }

    public String getEndpoint() {

        return endpoint;
    }

    public String getEventSchemaName() {

        return eventSchemaName;
    }

    public String getEventSchemaUri() {

        return eventSchemaUri;
    }

    public String getDescription() {

        return description;
    }

    public WebhookStatus getStatus() {

        return status;
    }

    public String getStatusString() {

        return status != null ? status.name() : null;
    }

    /**
     * Builder class for WebhookSummaryDTO.
     */
    public static class Builder {

        private String id;
        private String createdAt;
        private String updatedAt;
        private String endpoint;
        private String eventSchemaName;
        private String eventSchemaUri;
        private String description;
        private WebhookStatus status;

        public Builder() {

        }

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
            return builder;
        }

        public Builder setId(String id) {

            this.id = id;
            return this;
        }

        public Builder setCreatedAt(String createdAt) {

            this.createdAt = createdAt;
            return this;
        }

        public Builder setUpdatedAt(String updatedAt) {

            this.updatedAt = updatedAt;
            return this;
        }

        public Builder setEndpoint(String endpoint) {

            this.endpoint = endpoint;
            return this;
        }

        public Builder setEventSchemaName(String eventSchemaName) {

            this.eventSchemaName = eventSchemaName;
            return this;
        }

        public Builder setEventSchemaUri(String eventSchemaUri) {

            this.eventSchemaUri = eventSchemaUri;
            return this;
        }

        public Builder setDescription(String description) {

            this.description = description;
            return this;
        }

        public Builder setStatus(WebhookStatus status) {

            this.status = status;
            return this;
        }

        public WebhookSummaryDTO build() {

            return new WebhookSummaryDTO(this);
        }
    }

    @Override
    public boolean equals(Object o) {

        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        WebhookSummaryDTO that = (WebhookSummaryDTO) o;
        return java.util.Objects.equals(id, that.id) &&
                java.util.Objects.equals(createdAt, that.createdAt) &&
                java.util.Objects.equals(updatedAt, that.updatedAt) &&
                java.util.Objects.equals(endpoint, that.endpoint) &&
                java.util.Objects.equals(eventSchemaName, that.eventSchemaName) &&
                java.util.Objects.equals(eventSchemaUri, that.eventSchemaUri) &&
                java.util.Objects.equals(description, that.description) &&
                status == that.status;
    }

    @Override
    public int hashCode() {

        return java.util.Objects.hash(id, createdAt, updatedAt, endpoint, eventSchemaName, eventSchemaUri, description,
                status);
    }
}
