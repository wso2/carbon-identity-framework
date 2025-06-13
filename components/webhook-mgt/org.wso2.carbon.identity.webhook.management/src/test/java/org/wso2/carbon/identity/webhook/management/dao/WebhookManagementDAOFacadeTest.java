/*
 * Copyright (c) 2024, WSO2 LLC. (http://www.wso2.com).
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

import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.common.testng.WithCarbonHome;
import org.wso2.carbon.identity.common.testng.WithH2Database;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;
import org.wso2.carbon.identity.topic.management.api.exception.TopicManagementException;
import org.wso2.carbon.identity.topic.management.api.service.TopicManagementService;
import org.wso2.carbon.identity.webhook.management.api.exception.WebhookMgtException;
import org.wso2.carbon.identity.webhook.management.api.model.Webhook;
import org.wso2.carbon.identity.webhook.management.api.model.WebhookStatus;
import org.wso2.carbon.identity.webhook.management.internal.component.WebhookManagementComponentServiceHolder;
import org.wso2.carbon.identity.webhook.management.internal.dao.impl.WebhookManagementDAOFacade;
import org.wso2.carbon.identity.webhook.management.internal.dao.impl.WebhookManagementDAOImpl;
import org.wso2.carbon.identity.webhook.management.internal.service.impl.EventSubscriberService;

import java.sql.Timestamp;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

/**
 * This class is a test suite for the WebhookManagementDAOFacade class.
 * It contains unit tests to verify the functionality of the methods in the WebhookManagementDAOFacade class which is
 * responsible for handling external services.
 */
@WithH2Database(files = {"dbscripts/h2.sql"})
@WithCarbonHome
public class WebhookManagementDAOFacadeTest {

    public static final int TENANT_ID = 2;
    public static final int INVALID_TENANT_ID = -1;
    public static final String TENANT_DOMAIN = "carbon.super";
    private static final String WEBHOOK_ENDPOINT1 = "https://example.com/webhook1";
    private static final String WEBHOOK_ENDPOINT2 = "https://example.com/webhook2";
    private static final String WEBHOOK_NAME = "Test webhook";
    private static final String WEBHOOK_SECRET = "test-secret";
    private static final String WEBHOOK_EVENT_PROFILE_NAME = "user-events";
    private static final String WEBHOOK_EVENT_PROFILE_URI = "https://schemas.org/user-events";

    Webhook testWebhook;

    private MockedStatic<IdentityTenantUtil> identityTenantUtil;
    private WebhookManagementDAOFacade daoFacade;
    private EventSubscriberService eventSubscriberService;
    private TopicManagementService topicManagementService;

    @BeforeClass
    public void setUpClass() {

        daoFacade = new WebhookManagementDAOFacade(new WebhookManagementDAOImpl());
    }

    @BeforeMethod
    public void setUp() throws TopicManagementException {

        MockitoAnnotations.openMocks(this);

        eventSubscriberService = mock(EventSubscriberService.class);
        WebhookManagementComponentServiceHolder.getInstance().setEventSubscriberService(eventSubscriberService);

        topicManagementService = mock(TopicManagementService.class);
        WebhookManagementComponentServiceHolder.getInstance().setTopicManagementService(topicManagementService);

        // By default, all topics exist and registration is a no-op
        when(topicManagementService.isTopicExists(anyString(), anyString(), anyString())).thenReturn(true);

        identityTenantUtil = mockStatic(IdentityTenantUtil.class);
        identityTenantUtil.when(() -> IdentityTenantUtil.getTenantDomain(anyInt())).thenReturn(TENANT_DOMAIN);
    }

    @AfterMethod
    public void tearDown() {

        identityTenantUtil.close();
    }

    @Test(priority = 1)
    public void testAddWebhook() throws Exception {

        testWebhook = createTestWebhook();
        daoFacade.createWebhook(testWebhook, TENANT_ID);

        Webhook webhookRetrieved = daoFacade.getWebhook(testWebhook.getUuid(),
                TENANT_ID);

        verifyWebhook(webhookRetrieved, testWebhook);
    }

    @Test(priority = 2)
    public void testUpdateWebhook() throws Exception {

        testWebhook = new Webhook.Builder()
                .uuid(testWebhook.getUuid())
                .endpoint(testWebhook.getEndpoint())
                .name("Updated name")
                .secret(testWebhook.getSecret())
                .tenantId(testWebhook.getTenantId())
                .eventProfileName(testWebhook.getEventProfileName())
                .eventProfileUri(testWebhook.getEventProfileUri())
                .status(testWebhook.getStatus())
                .createdAt(testWebhook.getCreatedAt())
                .updatedAt(testWebhook.getUpdatedAt())
                .eventsSubscribed(testWebhook.getEventsSubscribed())
                .build();

        daoFacade.updateWebhook(testWebhook, TENANT_ID);

        Webhook updatedWebhook = daoFacade.getWebhook(testWebhook.getUuid(), TENANT_ID);

        Assert.assertEquals(updatedWebhook.getName(), "Updated name");
    }

    @Test(priority = 3)
    public void testGetWebhook() throws Exception {

        Webhook webhookRetrieved = daoFacade.getWebhook(testWebhook.getUuid(),
                TENANT_ID);

        Assert.assertEquals(webhookRetrieved.getEventsSubscribed(), testWebhook.getEventsSubscribed());
    }

    @Test(priority = 4)
    public void testGetWebhookEvents() throws Exception {

        Webhook webhookRetrieved = daoFacade.getWebhook(testWebhook.getUuid(),
                TENANT_ID);

        verifyWebhook(webhookRetrieved, testWebhook);
    }

    @Test(priority = 5)
    public void testGetWebhookByIdWithInvalidId() throws Exception {

        String invalidWebhookId = "invalid-webhook-id";
        Webhook webhookRetrieved = daoFacade.getWebhook(invalidWebhookId,
                TENANT_ID);

        Assert.assertNull(webhookRetrieved);
    }

    @Test(priority = 6)
    public void testGetWebhookByIdWithInvalidTenant() throws Exception {

        String invalidTenantDomain = "invalid-tenant-domain";
        identityTenantUtil.when(() -> IdentityTenantUtil.getTenantDomain(anyInt())).thenReturn(invalidTenantDomain);

        String invalidWebhookId = "invalid-webhook-id";
        Webhook webhookRetrieved = daoFacade.getWebhook(invalidWebhookId,
                TENANT_ID);

        Assert.assertNull(webhookRetrieved);
    }

    @Test(priority = 7)
    public void testGetWebhooks() throws Exception {

        List<Webhook> webhooks = daoFacade.getWebhooks(TENANT_ID);

        Assert.assertNotNull(webhooks);
        Assert.assertEquals(webhooks.size(), 1);
        verifyWebhook(webhooks.get(0), testWebhook);
    }

    @Test(priority = 9)
    public void testGetWebhooksWithInvalidTenant() throws Exception {

        List<Webhook> webhooks = daoFacade.getWebhooks(INVALID_TENANT_ID);

        Assert.assertNotNull(webhooks);
        Assert.assertEquals(webhooks.size(), 0);
    }

    @Test(priority = 10)
    public void testGetWebhooksWithNoWebhooks() throws Exception {

        daoFacade.deleteWebhook(testWebhook.getUuid(), TENANT_ID);

        List<Webhook> webhooks = daoFacade.getWebhooks(TENANT_ID);

        Assert.assertNotNull(webhooks);
        Assert.assertEquals(webhooks.size(), 0);
    }

    @Test(priority = 14)
    public void testIsWebhookEndpointExists() throws Exception {

        testWebhook = createTestWebhook();
        // Create a new Webhook with the updated endpoint
        testWebhook = new Webhook.Builder()
                .uuid(testWebhook.getUuid())
                .endpoint(WEBHOOK_ENDPOINT2)
                .name(testWebhook.getName())
                .secret(testWebhook.getSecret())
                .tenantId(testWebhook.getTenantId())
                .eventProfileName(testWebhook.getEventProfileName())
                .eventProfileUri(testWebhook.getEventProfileUri())
                .status(testWebhook.getStatus())
                .createdAt(testWebhook.getCreatedAt())
                .updatedAt(testWebhook.getUpdatedAt())
                .eventsSubscribed(testWebhook.getEventsSubscribed())
                .build();

        daoFacade.createWebhook(testWebhook, TENANT_ID);
        boolean exists = daoFacade.isWebhookEndpointExists(WEBHOOK_ENDPOINT2, TENANT_ID);

        Assert.assertTrue(exists);
    }

    @Test(priority = 15)
    public void testIsWebhookEndpointExistsWithInvalidEndpoint() throws Exception {

        String invalidEndpoint = "invalid-endpoint";
        boolean exists = daoFacade.isWebhookEndpointExists(invalidEndpoint, TENANT_ID);

        Assert.assertFalse(exists);
    }

    @Test(priority = 16)
    public void testIsWebhookEndpointExistsWithInvalidTenant() throws Exception {

        boolean exists = daoFacade.isWebhookEndpointExists(WEBHOOK_ENDPOINT1, INVALID_TENANT_ID);

        Assert.assertFalse(exists);
    }

    @Test(priority = 17)
    public void testActivateWebhook() throws Exception {

        // Create a webhook with INACTIVE status using the builder
        testWebhook = new Webhook.Builder()
                .uuid(UUID.randomUUID().toString())
                .endpoint(WEBHOOK_ENDPOINT1)
                .name(WEBHOOK_NAME)
                .secret(WEBHOOK_SECRET)
                .eventProfileName(WEBHOOK_EVENT_PROFILE_NAME)
                .eventProfileUri(WEBHOOK_EVENT_PROFILE_URI)
                .status(WebhookStatus.INACTIVE)
                .createdAt(new Timestamp(System.currentTimeMillis()))
                .updatedAt(new Timestamp(System.currentTimeMillis()))
                .tenantId(TENANT_ID)
                .build();

        daoFacade.createWebhook(testWebhook, TENANT_ID);
        daoFacade.activateWebhook(testWebhook.getUuid(), TENANT_ID);

        Webhook webhookRetrieved = daoFacade.getWebhook(testWebhook.getUuid(), TENANT_ID);

        Assert.assertEquals(webhookRetrieved.getStatus(), WebhookStatus.ACTIVE);
    }

    @Test(priority = 19)
    public void testDeactivateWebhook() throws Exception {

        daoFacade.deactivateWebhook(testWebhook.getUuid(), TENANT_ID);

        Webhook webhookRetrieved = daoFacade.getWebhook(testWebhook.getUuid(),
                TENANT_ID);

        Assert.assertEquals(webhookRetrieved.getStatus(), WebhookStatus.INACTIVE);
    }

    @Test(priority = 20, expectedExceptions = WebhookMgtException.class)
    public void testUpdateWebhookWithSubscriptionFailure() throws Exception {

        testWebhook = createTestWebhook();
        daoFacade.createWebhook(testWebhook, TENANT_ID);

        // Extract required fields from testWebhook for the subscribe method
        String webhookId = testWebhook.getUuid();
        String adaptor = "WebSubHubAdapter"; // Assuming WebSubHubAdapter is the adaptor used
        List<String> channels = testWebhook.getEventsSubscribed();
        String eventProfileVersion = "v1";
        String endpoint = testWebhook.getEndpoint();
        String secret = testWebhook.getSecret();
        String tenantDomain = IdentityTenantUtil.getTenantDomain(testWebhook.getTenantId());

        doThrow(new WebhookMgtException("Subscription failed")).when(eventSubscriberService)
                .subscribe(webhookId, adaptor, channels, eventProfileVersion, endpoint, secret, tenantDomain);

        daoFacade.updateWebhook(testWebhook, TENANT_ID);
    }

    private Webhook createTestWebhook() {

        List<String> eventsSubscribed = new java.util.ArrayList<>();
        eventsSubscribed.add("user.create");
        eventsSubscribed.add("user.update");
        return new Webhook.Builder()
                .uuid(UUID.randomUUID().toString())
                .endpoint(WEBHOOK_ENDPOINT1)
                .name(WEBHOOK_NAME)
                .secret(WEBHOOK_SECRET)
                .eventProfileName(WEBHOOK_EVENT_PROFILE_NAME)
                .eventProfileUri(WEBHOOK_EVENT_PROFILE_URI)
                .status(WebhookStatus.ACTIVE)
                .createdAt(new Timestamp(System.currentTimeMillis()))
                .updatedAt(new Timestamp(System.currentTimeMillis()))
                .eventsSubscribed(eventsSubscribed)
                .tenantId(TENANT_ID)
                .build();
    }

    private void verifyWebhook(Webhook actualWebhook, Webhook expectedWebhook) {

        Assert.assertEquals(actualWebhook.getUuid(), expectedWebhook.getUuid());
        Assert.assertEquals(actualWebhook.getEndpoint(), expectedWebhook.getEndpoint());
        Assert.assertEquals(actualWebhook.getName(), expectedWebhook.getName());
        Assert.assertEquals(actualWebhook.getEventProfileName(), expectedWebhook.getEventProfileName());
        Assert.assertEquals(actualWebhook.getEventProfileUri(), expectedWebhook.getEventProfileUri());
        Assert.assertEquals(actualWebhook.getStatus(), expectedWebhook.getStatus());
        Assert.assertEquals(actualWebhook.getTenantId(), expectedWebhook.getTenantId());
    }
}
