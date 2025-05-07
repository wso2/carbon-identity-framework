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

package org.wso2.carbon.identity.webhook.management.util;

import org.wso2.carbon.identity.webhook.management.api.model.Webhook;

import java.util.ArrayList;
import java.util.List;

/**
 * Utility class for testing webhook management features.
 */
public class TestUtil {

    public static final String TEST_WEBHOOK_ID = "test-webhook-id";
    public static final String TENANT_DOMAIN = "carbon.super";
    public static final String TEST_ENDPOINT = "https://test-webhook.com/endpoint";
    public static final String TEST_UPDATED_ENDPOINT = "https://updated-webhook.com/endpoint";
    public static final String TEST_DESCRIPTION = "Test webhook description";
    public static final String TEST_SECRET = "test-secret-key";
    public static final String TEST_VERSION = "1.0.0";
    public static final String TEST_EVENT_SCHEMA_NAME = "standard";
    public static final String TEST_EVENT_SCHEMA_URI = "https://example.com/schema";
    public static final String TEST_EVENT_SCHEMA_VERSION = "1.0";
    public static final String TEST_STATUS = "active";

    /**
     * Build a mock webhook for testing.
     *
     * @param id                 ID of the webhook.
     * @param endpoint           Webhook endpoint URL.
     * @param description        Webhook description.
     * @param secret             Webhook secret.
     * @param eventSchemaName    Event schema name.
     * @param eventSchemaUri     Event schema URI.
     * @param status             Webhook status.
     * @param eventsSubscribed   Array of events subscribed.
     * @return Mock webhook object.
     */
    public static Webhook buildMockWebhook(String id, String endpoint, String description, String secret,
                                           String eventSchemaName, String eventSchemaUri, String status,
                                           List<String> eventsSubscribed) {

        Webhook webhook = new Webhook();
        webhook.setId(id);
        webhook.setEndpoint(endpoint);
        webhook.setDescription(description);
        webhook.setSecret(secret);
        webhook.setEventSchemaName(eventSchemaName);
        webhook.setEventSchemaUri(eventSchemaUri);
        webhook.setStatus(status);
        webhook.setEventsSubscribed(eventsSubscribed);
        return webhook;
    }

    /**
     * Generate a list of standard test events.
     *
     * @return List of test events.
     */
    public static List<String> generateTestEvents() {

        List<String> events = new ArrayList<>();
        events.add("user.created");
        events.add("user.updated");
        return events;
    }

    /**
     * Generate a list of updated test events.
     *
     * @return List of updated test events.
     */
    public static List<String> generateUpdatedTestEvents() {

        List<String> events = new ArrayList<>();
        events.add("user.created");
        events.add("user.updated");
        events.add("user.deleted");
        return events;
    }
}
