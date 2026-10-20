/*
 * Copyright (c) 2026, WSO2 LLC. (http://www.wso2.com).
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

import org.mockito.Answers;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.webhook.management.api.exception.WebhookMgtException;
import org.wso2.carbon.identity.webhook.management.api.service.WebhookManagementService;

import static org.mockito.Mockito.mock;
import static org.testng.Assert.fail;

/**
 * Unit tests for the default methods of {@link WebhookManagementService}. An implementation that predates an
 * operation inherits a refusal rather than silently answering with nothing.
 */
public class WebhookManagementServiceDefaultMethodsTest {

    private WebhookManagementService service;

    @BeforeClass
    public void setUpClass() {

        // CALLS_REAL_METHODS runs the interface's own default bodies rather than stubbing them away.
        service = mock(WebhookManagementService.class, Answers.CALLS_REAL_METHODS);
    }

    @Test
    public void testPagedWebhookLookupsAreNotImplementedByDefault() {

        assertNotImplemented(() -> service.getWebhooks(0, 10, "test.com"));
        assertNotImplemented(() -> service.getWebhooksCount("test.com"));
    }

    @Test
    public void testChannelLookupsAreNotImplementedByDefault() {

        assertNotImplemented(() -> service.getWebhookChannels("webhookId", "test.com"));
        assertNotImplemented(() -> service.getWebhookChannel("webhookId", "channelId", "test.com"));
    }

    @Test
    public void testOrganizationSubscriptionLookupsAreNotImplementedByDefault() {

        assertNotImplemented(() -> service.getChannelOrganizationSubscriptions("webhookId", "channelId",
                "test.com", 0, 10));
        assertNotImplemented(() -> service.getSubscriptionAcceptedWebhooksForOrganization("profile", "v1",
                "channelUri", "test.com"));
    }

    private static void assertNotImplemented(ServiceCall call) {

        try {
            call.run();
            fail("The default implementation must refuse the operation.");
        } catch (UnsupportedOperationException e) {
            // org.apache.commons.lang.NotImplementedException extends UnsupportedOperationException, which is
            // matched here so the test does not depend on which commons-lang is on the classpath.
        } catch (WebhookMgtException e) {
            fail("The default implementation must refuse rather than fail: " + e.getMessage());
        }
    }

    @FunctionalInterface
    private interface ServiceCall {

        void run() throws WebhookMgtException;
    }
}
