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

package org.wso2.carbon.identity.webhook.management.dao;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.common.testng.WithCarbonHome;
import org.wso2.carbon.identity.common.testng.WithH2Database;
import org.wso2.carbon.identity.webhook.management.api.exception.WebhookMgtException;
import org.wso2.carbon.identity.webhook.management.api.model.Webhook;
import org.wso2.carbon.identity.webhook.management.api.model.WebhookSearchResult;
import org.wso2.carbon.identity.webhook.management.internal.dao.impl.WebhookManagementDAOImpl;
import org.wso2.carbon.identity.webhook.management.util.TestUtil;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.wso2.carbon.identity.webhook.management.internal.constant.WebhookSQLConstants.STATUS_ACTIVE;
import static org.wso2.carbon.identity.webhook.management.internal.constant.WebhookSQLConstants.STATUS_INACTIVE;

/**
 * This class is a test suite for WebhookManagementDAOImpl class.
 * It contains unit tests to verify the functionality of the methods in the WebhookManagementDAOImpl class.
 */
@WithH2Database(files = {"dbscripts/h2.sql"})
@WithCarbonHome
public class WebhookManagementDAOImplTest {

    private static final int TENANT_ID = 1;
    private static final String ENDPOINT = "https://test-webhook.com/endpoint";
    private static final String DESCRIPTION = "Test webhook description";
    private static final String SECRET = "test-secret-key";
    private static final String VERSION = "1.0.0";
    private static final String EVENT_SCHEMA_NAME = "standard";
    private static final String EVENT_SCHEMA_URI = "https://example.com/schema";
    private static final String EVENT_SCHEMA_VERSION = "1.0";
    private static final List<String> EVENTS_SUBSCRIBED = Arrays.asList("user.created", "user.updated");

    private WebhookManagementDAOImpl daoImpl;
    private String webhookId;
    private Webhook createdWebhook;

    @BeforeClass
    public void setUpClass() {

        daoImpl = new WebhookManagementDAOImpl();
        webhookId = UUID.randomUUID().toString();
    }

    @Test(priority = 1)
    public void testCreateWebhook() throws WebhookMgtException {

        Webhook webhook = TestUtil.buildMockWebhook(
                webhookId,
                ENDPOINT,
                DESCRIPTION,
                SECRET,
                VERSION,
                EVENT_SCHEMA_NAME,
                EVENT_SCHEMA_URI,
                EVENT_SCHEMA_VERSION,
                STATUS_ACTIVE,
                EVENTS_SUBSCRIBED
        );

        try {
            daoImpl.createWebhook(webhook, TENANT_ID);
        } catch (Exception e) {
            Assert.fail("Failed to create webhook: " + e.getMessage());
        }

        Webhook retrievedWebhook = daoImpl.getWebhook(webhookId, TENANT_ID);
        Assert.assertNotNull(retrievedWebhook);
        Assert.assertEquals(retrievedWebhook.getId(), webhook.getId());
        Assert.assertEquals(retrievedWebhook.getEndpoint(), webhook.getEndpoint());
        Assert.assertEquals(retrievedWebhook.getDescription(), webhook.getDescription());
        Assert.assertEquals(retrievedWebhook.getSecret(), webhook.getSecret());
        Assert.assertEquals(retrievedWebhook.getVersion(), webhook.getVersion());
        Assert.assertEquals(retrievedWebhook.getEventSchemaName(), webhook.getEventSchemaName());
        Assert.assertEquals(retrievedWebhook.getEventSchemaUri(), webhook.getEventSchemaUri());
        Assert.assertEquals(retrievedWebhook.getEventSchemaVersion(), webhook.getEventSchemaVersion());
        Assert.assertEquals(retrievedWebhook.getStatus(), webhook.getStatus());
        Assert.assertNotNull(retrievedWebhook.getEventsSubscribed());
        Assert.assertEquals(retrievedWebhook.getEventsSubscribed().size(), webhook.getEventsSubscribed().size());
        Assert.assertTrue(retrievedWebhook.getEventsSubscribed().containsAll(webhook.getEventsSubscribed()));

        createdWebhook = retrievedWebhook;
    }

    @Test(priority = 2, dependsOnMethods = "testCreateWebhook")
    public void testGetWebhook() throws WebhookMgtException {

        Webhook webhook = daoImpl.getWebhook(webhookId, TENANT_ID);
        Assert.assertNotNull(webhook);
        Assert.assertEquals(webhook.getId(), webhookId);
    }

    @Test(priority = 3, dependsOnMethods = "testCreateWebhook")
    public void testUpdateWebhook() throws WebhookMgtException {

        String updatedEndpoint = "https://updated-webhook.com/endpoint";
        String updatedDescription = "Updated webhook description";
        String updatedSecret = "updated-secret-key";
        String updatedVersion = "2.0.0";
        String updatedEventSchemaName = "enhanced";
        String updatedEventSchemaUri = "https://example.com/updated-schema";
        String updatedEventSchemaVersion = "2.0";
        List<String> updatedEventsSubscribed = Arrays.asList("user.created", "user.updated", "user.deleted");

        Webhook updatedWebhook = TestUtil.buildMockWebhook(
                webhookId,
                updatedEndpoint,
                updatedDescription,
                updatedSecret,
                updatedVersion,
                updatedEventSchemaName,
                updatedEventSchemaUri,
                updatedEventSchemaVersion,
                STATUS_ACTIVE,
                updatedEventsSubscribed
        );

        try {
            daoImpl.updateWebhook(webhookId, updatedWebhook, TENANT_ID);
        } catch (Exception e) {
            Assert.fail("Failed to update webhook: " + e.getMessage());
        }

        Webhook retrievedWebhook = daoImpl.getWebhook(webhookId, TENANT_ID);
        Assert.assertNotNull(retrievedWebhook);
        Assert.assertEquals(retrievedWebhook.getId(), updatedWebhook.getId());
        Assert.assertEquals(retrievedWebhook.getEndpoint(), updatedWebhook.getEndpoint());
        Assert.assertEquals(retrievedWebhook.getDescription(), updatedWebhook.getDescription());
        Assert.assertEquals(retrievedWebhook.getSecret(), updatedWebhook.getSecret());
        Assert.assertEquals(retrievedWebhook.getVersion(), updatedWebhook.getVersion());
        Assert.assertEquals(retrievedWebhook.getEventSchemaName(), updatedWebhook.getEventSchemaName());
        Assert.assertEquals(retrievedWebhook.getEventSchemaUri(), updatedWebhook.getEventSchemaUri());
        Assert.assertEquals(retrievedWebhook.getEventSchemaVersion(), updatedWebhook.getEventSchemaVersion());
        Assert.assertEquals(retrievedWebhook.getStatus(), updatedWebhook.getStatus());
        Assert.assertNotNull(retrievedWebhook.getEventsSubscribed());
        Assert.assertEquals(retrievedWebhook.getEventsSubscribed().size(), updatedWebhook.getEventsSubscribed().size());
        Assert.assertTrue(retrievedWebhook.getEventsSubscribed().containsAll(updatedWebhook.getEventsSubscribed()));

        createdWebhook = retrievedWebhook;
    }

    @Test(priority = 4, dependsOnMethods = "testUpdateWebhook")
    public void testListWebhooks() throws WebhookMgtException {

        int limit = 10;
        int offset = 0;

        WebhookSearchResult searchResult = daoImpl.listWebhooks(STATUS_ACTIVE, limit, offset, TENANT_ID);
        Assert.assertNotNull(searchResult);
        Assert.assertEquals(searchResult.getTotalCount(), 1);
        Assert.assertEquals(searchResult.getWebhooks().size(), 1);
        Assert.assertEquals(searchResult.getWebhooks().get(0).getId(), webhookId);
    }

    @Test(priority = 5, dependsOnMethods = "testListWebhooks")
    public void testDeactivateWebhook() throws WebhookMgtException {

        try {
            daoImpl.deactivateWebhook(webhookId, TENANT_ID);
        } catch (Exception e) {
            Assert.fail("Failed to deactivate webhook: " + e.getMessage());
        }

        Webhook webhook = daoImpl.getWebhook(webhookId, TENANT_ID);
        Assert.assertNotNull(webhook);
        Assert.assertEquals(webhook.getStatus(), STATUS_INACTIVE);
    }

    @Test(priority = 6, dependsOnMethods = "testDeactivateWebhook")
    public void testActivateWebhook() throws WebhookMgtException {

        try {
            daoImpl.activateWebhook(webhookId, TENANT_ID);
        } catch (Exception e) {
            Assert.fail("Failed to activate webhook: " + e.getMessage());
        }

        Webhook webhook = daoImpl.getWebhook(webhookId, TENANT_ID);
        Assert.assertNotNull(webhook);
        Assert.assertEquals(webhook.getStatus(), STATUS_ACTIVE);
    }

    @Test(priority = 7, dependsOnMethods = "testActivateWebhook")
    public void testIsWebhookEndpointExists() throws WebhookMgtException {

        boolean exists = daoImpl.isWebhookEndpointExists(ENDPOINT, TENANT_ID);
        Assert.assertFalse(exists); // Should be false since we changed the endpoint

        boolean updatedExists = daoImpl.isWebhookEndpointExists(createdWebhook.getEndpoint(), TENANT_ID);
        Assert.assertTrue(updatedExists);
    }

    @Test(priority = 8, dependsOnMethods = "testIsWebhookEndpointExists")
    public void testListWebhookEndpoints() throws WebhookMgtException {

        List<String> endpoints = daoImpl.listWebhookEndpoints(TENANT_ID);
        Assert.assertNotNull(endpoints);
        Assert.assertFalse(endpoints.isEmpty());
        Assert.assertEquals(endpoints.get(0), createdWebhook.getEndpoint());
    }

    @Test(priority = 9, dependsOnMethods = "testListWebhookEndpoints")
    public void testDeleteWebhook() throws WebhookMgtException {

        try {
            daoImpl.deleteWebhook(webhookId, TENANT_ID);
        } catch (Exception e) {
            Assert.fail("Failed to delete webhook: " + e.getMessage());
        }

        Webhook webhook = daoImpl.getWebhook(webhookId, TENANT_ID);
        Assert.assertNull(webhook);
    }
}
