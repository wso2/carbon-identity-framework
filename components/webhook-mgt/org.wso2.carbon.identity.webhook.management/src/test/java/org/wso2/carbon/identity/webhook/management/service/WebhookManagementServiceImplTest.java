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

package org.wso2.carbon.identity.webhook.management.service;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.common.testng.WithCarbonHome;
import org.wso2.carbon.identity.common.testng.WithH2Database;
import org.wso2.carbon.identity.common.testng.WithRealmService;
import org.wso2.carbon.identity.core.internal.IdentityCoreServiceDataHolder;
import org.wso2.carbon.identity.webhook.management.api.exception.WebhookMgtClientException;
import org.wso2.carbon.identity.webhook.management.api.exception.WebhookMgtException;
import org.wso2.carbon.identity.webhook.management.api.model.Webhook;
import org.wso2.carbon.identity.webhook.management.api.model.WebhookDTO;
import org.wso2.carbon.identity.webhook.management.api.model.WebhookSearchResultDTO;
import org.wso2.carbon.identity.webhook.management.api.service.WebhookManagementService;
import org.wso2.carbon.identity.webhook.management.internal.service.impl.WebhookManagementServiceImpl;
import org.wso2.carbon.identity.webhook.management.util.TestUtil;

import java.util.Arrays;
import java.util.UUID;

import static org.wso2.carbon.identity.webhook.management.util.TestUtil.TENANT_DOMAIN;
import static org.wso2.carbon.identity.webhook.management.util.TestUtil.TEST_DESCRIPTION;
import static org.wso2.carbon.identity.webhook.management.util.TestUtil.TEST_ENDPOINT;
import static org.wso2.carbon.identity.webhook.management.util.TestUtil.TEST_EVENT_SCHEMA_NAME;
import static org.wso2.carbon.identity.webhook.management.util.TestUtil.TEST_EVENT_SCHEMA_URI;
import static org.wso2.carbon.identity.webhook.management.util.TestUtil.TEST_EVENT_SCHEMA_VERSION;
import static org.wso2.carbon.identity.webhook.management.util.TestUtil.TEST_SECRET;
import static org.wso2.carbon.identity.webhook.management.util.TestUtil.TEST_STATUS;
import static org.wso2.carbon.identity.webhook.management.util.TestUtil.TEST_UPDATED_ENDPOINT;
import static org.wso2.carbon.identity.webhook.management.util.TestUtil.TEST_VERSION;
import static org.wso2.carbon.identity.webhook.management.util.TestUtil.TEST_WEBHOOK_ID;

@WithCarbonHome
@WithH2Database(files = {"dbscripts/h2.sql"})
@WithRealmService(injectToSingletons = {IdentityCoreServiceDataHolder.class})
public class WebhookManagementServiceImplTest {

    private WebhookManagementService webhookManagementService;
    private Webhook sampleWebhook;
    private String webhookId;

    @BeforeClass
    public void setUpClass() {

        webhookManagementService = new WebhookManagementServiceImpl();
    }

    @BeforeMethod
    public void setUp() {

        sampleWebhook = TestUtil.buildMockWebhook(
                TEST_WEBHOOK_ID,
                TEST_ENDPOINT,
                TEST_DESCRIPTION,
                TEST_SECRET,
                TEST_VERSION,
                TEST_EVENT_SCHEMA_NAME,
                TEST_EVENT_SCHEMA_URI,
                TEST_EVENT_SCHEMA_VERSION,
                TEST_STATUS,
                TestUtil.generateTestEvents());
    }

    @Test(priority = 1)
    public void testCreateWebhook() throws WebhookMgtException {

        WebhookDTO webhookDTO = webhookManagementService.createWebhook(sampleWebhook, TENANT_DOMAIN);

        Assert.assertNotNull(webhookDTO.getId());
        webhookId = webhookDTO.getId();
        Assert.assertEquals(webhookDTO.getEndpoint(), sampleWebhook.getEndpoint());
        Assert.assertEquals(webhookDTO.getDescription(), sampleWebhook.getDescription());
        Assert.assertEquals(webhookDTO.getEventSchemaName(), sampleWebhook.getEventSchemaName());
        Assert.assertEquals(webhookDTO.getEventSchemaVersion(), sampleWebhook.getEventSchemaVersion());
        Assert.assertEquals(webhookDTO.getEventSchemaUri(), sampleWebhook.getEventSchemaUri());
        Assert.assertEquals(webhookDTO.getVersion(), sampleWebhook.getVersion());
        Assert.assertEquals(webhookDTO.getStatus(), "ACTIVE");
        Assert.assertEquals(webhookDTO.getEventsSubscribed().size(), sampleWebhook.getEventsSubscribed().size());
        for (int i = 0; i < webhookDTO.getEventsSubscribed().size(); i++) {
            Assert.assertEquals(webhookDTO.getEventsSubscribed().get(i), sampleWebhook.getEventsSubscribed().get(i));
        }
    }

    @Test(priority = 2, expectedExceptions = WebhookMgtClientException.class,
            expectedExceptionsMessageRegExp = "Invalid request.")
    public void testCreateWebhookWithInvalidData() throws WebhookMgtException {

        Webhook invalidWebhook = TestUtil.buildMockWebhook(
                TEST_WEBHOOK_ID,
                "",  // Empty endpoint
                TEST_DESCRIPTION,
                TEST_SECRET,
                TEST_VERSION,
                TEST_EVENT_SCHEMA_NAME,
                TEST_EVENT_SCHEMA_URI,
                TEST_EVENT_SCHEMA_VERSION,
                TEST_STATUS,
                TestUtil.generateTestEvents());

        webhookManagementService.createWebhook(invalidWebhook, TENANT_DOMAIN);
    }

    @Test(priority = 3, expectedExceptions = WebhookMgtClientException.class,
            expectedExceptionsMessageRegExp = "Webhook endpoint already exists.")
    public void testCreateDuplicateWebhook() throws WebhookMgtException {
        // Try to create a webhook with the same endpoint
        webhookManagementService.createWebhook(sampleWebhook, TENANT_DOMAIN);
    }

    @Test(priority = 4)
    public void testGetWebhook() throws WebhookMgtException {

        WebhookDTO webhookDTO = webhookManagementService.getWebhook(webhookId, TENANT_DOMAIN);

        Assert.assertEquals(webhookDTO.getId(), webhookId);
        Assert.assertEquals(webhookDTO.getEndpoint(), sampleWebhook.getEndpoint());
        Assert.assertEquals(webhookDTO.getDescription(), sampleWebhook.getDescription());
        Assert.assertEquals(webhookDTO.getEventSchemaName(), sampleWebhook.getEventSchemaName());
        Assert.assertEquals(webhookDTO.getEventSchemaVersion(), sampleWebhook.getEventSchemaVersion());
        Assert.assertEquals(webhookDTO.getEventSchemaUri(), sampleWebhook.getEventSchemaUri());
        Assert.assertEquals(webhookDTO.getVersion(), sampleWebhook.getVersion());
    }

    @Test(priority = 5, expectedExceptions = WebhookMgtException.class,
            expectedExceptionsMessageRegExp = "Error retrieving webhook.")
    public void testGetNonExistingWebhook() throws WebhookMgtException {

        webhookManagementService.getWebhook(UUID.randomUUID().toString(), TENANT_DOMAIN);
    }

    @Test(priority = 6)
    public void testUpdateWebhook() throws WebhookMgtException {

        Webhook updatedWebhook = TestUtil.buildMockWebhook(
                TEST_WEBHOOK_ID,
                TEST_UPDATED_ENDPOINT,
                "Updated " + TEST_DESCRIPTION,
                TEST_SECRET,
                "2.0",
                TEST_EVENT_SCHEMA_NAME,
                TEST_EVENT_SCHEMA_URI,
                "2.0",
                TEST_STATUS,
                TestUtil.generateUpdatedTestEvents());

        // Set the ID of the webhook to be updated
        updatedWebhook.setId(webhookId);

        WebhookDTO updatedWebhookDTO = webhookManagementService.updateWebhook(webhookId, updatedWebhook, TENANT_DOMAIN);

        Assert.assertEquals(updatedWebhookDTO.getId(), webhookId);
        Assert.assertEquals(updatedWebhookDTO.getEndpoint(), updatedWebhook.getEndpoint());
        Assert.assertEquals(updatedWebhookDTO.getDescription(), updatedWebhook.getDescription());
        Assert.assertEquals(updatedWebhookDTO.getEventSchemaName(), updatedWebhook.getEventSchemaName());
        Assert.assertEquals(updatedWebhookDTO.getEventSchemaVersion(), updatedWebhook.getEventSchemaVersion());
        Assert.assertEquals(updatedWebhookDTO.getEventSchemaUri(), updatedWebhook.getEventSchemaUri());
        Assert.assertEquals(updatedWebhookDTO.getVersion(), updatedWebhook.getVersion());
        Assert.assertEquals(updatedWebhookDTO.getEventsSubscribed().size(),
                updatedWebhook.getEventsSubscribed().size());
    }

    @Test(priority = 7)
    public void testListWebhooks() throws WebhookMgtException {
        // Test listing all webhooks
        WebhookSearchResultDTO result = webhookManagementService.listWebhooks(null, 10, 0, TENANT_DOMAIN);
        Assert.assertEquals(result.getTotalCount(), 1);
        Assert.assertEquals(result.getWebhooks().size(), 1);

        // Test listing by status
        WebhookSearchResultDTO activeResult = webhookManagementService.listWebhooks("ACTIVE", 10, 0, TENANT_DOMAIN);
        Assert.assertEquals(activeResult.getTotalCount(), 1);

        WebhookSearchResultDTO inactiveResult = webhookManagementService.listWebhooks("DISABLED", 10, 0, TENANT_DOMAIN);
        Assert.assertEquals(inactiveResult.getTotalCount(), 0);
    }

    @Test(priority = 8)
    public void testDeactivateWebhook() throws WebhookMgtException {

        webhookManagementService.deactivateWebhook(webhookId, TENANT_DOMAIN);

        WebhookDTO webhookDTO = webhookManagementService.getWebhook(webhookId, TENANT_DOMAIN);
        Assert.assertEquals(webhookDTO.getStatus(), "DISABLED");

        // Verify in the search results
        WebhookSearchResultDTO activeResult = webhookManagementService.listWebhooks("ACTIVE", 10, 0, TENANT_DOMAIN);
        Assert.assertEquals(activeResult.getTotalCount(), 0);

        WebhookSearchResultDTO inactiveResult = webhookManagementService.listWebhooks("DISABLED", 10, 0, TENANT_DOMAIN);
        Assert.assertEquals(inactiveResult.getTotalCount(), 1);
    }

    @Test(priority = 9)
    public void testActivateWebhook() throws WebhookMgtException {

        webhookManagementService.activateWebhook(webhookId, TENANT_DOMAIN);

        WebhookDTO webhookDTO = webhookManagementService.getWebhook(webhookId, TENANT_DOMAIN);
        Assert.assertEquals(webhookDTO.getStatus(), "ACTIVE");

        // Verify in the search results
        WebhookSearchResultDTO activeResult = webhookManagementService.listWebhooks("ACTIVE", 10, 0, TENANT_DOMAIN);
        Assert.assertEquals(activeResult.getTotalCount(), 1);
    }

    @Test(priority = 10)
    public void testDeleteWebhook() throws WebhookMgtException {

        webhookManagementService.deleteWebhook(webhookId, TENANT_DOMAIN);

        // Verify webhook was deleted
        WebhookSearchResultDTO result = webhookManagementService.listWebhooks(null, 10, 0, TENANT_DOMAIN);
        Assert.assertEquals(result.getTotalCount(), 0);

        // Verify getting the deleted webhook throws exception
        try {
            webhookManagementService.getWebhook(webhookId, TENANT_DOMAIN);
            Assert.fail("Expected WebhookMgtException was not thrown");
        } catch (WebhookMgtException e) {
            Assert.assertTrue(e.getMessage().contains("Error retrieving webhook"));
        }
    }

    @Test(priority = 11)
    public void testCreateMultipleWebhooks() throws WebhookMgtException {
        // Create first webhook
        Webhook webhook1 = TestUtil.buildMockWebhook(
                "test-webhook-01",
                "https://example1.com/hooks",
                "Test Webhook 1",
                "secret1",
                "1.0",
                "standard",
                TEST_EVENT_SCHEMA_URI,
                "1.0",
                TEST_STATUS,
                Arrays.asList("event1", "event2"));
        WebhookDTO webhookDTO1 = webhookManagementService.createWebhook(webhook1, TENANT_DOMAIN);

        // Create second webhook
        Webhook webhook2 = TestUtil.buildMockWebhook(
                "test-webhook-02",
                "https://example2.com/hooks",
                "Test Webhook 2",
                "secret2",
                "1.0",
                "standard",
                TEST_EVENT_SCHEMA_URI,
                "1.0",
                TEST_STATUS,
                Arrays.asList("event3", "event4"));
        WebhookDTO webhookDTO2 = webhookManagementService.createWebhook(webhook2, TENANT_DOMAIN);

        // Verify both webhooks exist
        WebhookSearchResultDTO result = webhookManagementService.listWebhooks(null, 10, 0, TENANT_DOMAIN);
        Assert.assertEquals(result.getTotalCount(), 2);

        // Test pagination
        WebhookSearchResultDTO pageResult = webhookManagementService.listWebhooks(null, 1, 0, TENANT_DOMAIN);
        Assert.assertEquals(pageResult.getTotalCount(), 2);
        Assert.assertEquals(pageResult.getWebhooks().size(), 1);

        // Cleanup
        webhookManagementService.deleteWebhook(webhookDTO1.getId(), TENANT_DOMAIN);
        webhookManagementService.deleteWebhook(webhookDTO2.getId(), TENANT_DOMAIN);
    }
}
