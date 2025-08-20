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

import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.subscription.management.api.model.Subscription;
import org.wso2.carbon.identity.webhook.management.api.exception.WebhookMgtClientException;
import org.wso2.carbon.identity.webhook.management.api.exception.WebhookMgtException;
import org.wso2.carbon.identity.webhook.management.internal.component.WebhookManagementComponentServiceHolder;
import org.wso2.carbon.identity.webhook.management.internal.util.WebhookValidator;
import org.wso2.carbon.identity.webhook.metadata.api.model.Channel;
import org.wso2.carbon.identity.webhook.metadata.api.model.EventProfile;
import org.wso2.carbon.identity.webhook.metadata.api.service.WebhookMetadataService;

import java.util.Collections;
import java.util.List;

public class WebhookValidatorTest {

    private WebhookValidator validator;

    @BeforeClass
    public void setUp() {

        validator = new WebhookValidator();
    }

    @Test(expectedExceptions = WebhookMgtClientException.class)
    public void testValidateForBlankThrows() throws WebhookMgtClientException {

        validator.validateForBlank("field", "");
    }

    @Test
    public void testValidateForBlankPasses() throws WebhookMgtClientException {

        validator.validateForBlank("field", "not blank");
    }

    @DataProvider
    public Object[][] invalidNames() {

        return new Object[][] {
                {"@webhook"}, {"webhook#"}, {" webhook"}, {"webhook*"}
        };
    }

    @Test(dataProvider = "invalidNames", expectedExceptions = WebhookMgtClientException.class)
    public void testInvalidWebhookName(String name) throws WebhookMgtClientException {

        validator.validateWebhookName(name);
    }

    @DataProvider
    public Object[][] validNames() {

        return new Object[][] {
                {"webhook-1"}, {"webhook_2"}, {"Webhook Name"}, {"webhookName"}
        };
    }

    @Test(dataProvider = "validNames")
    public void testValidWebhookName(String name) throws WebhookMgtClientException {

        validator.validateWebhookName(name);
    }

    @DataProvider
    public Object[][] invalidUris() {

        return new Object[][] {
                {"ftp://example.com"}, {"http:/example.com"}, {"https://"}, {"https:// example.com"}
        };
    }

    @Test(dataProvider = "invalidUris", expectedExceptions = WebhookMgtClientException.class)
    public void testInvalidEndpointUri(String uri) throws WebhookMgtClientException {

        validator.validateEndpointUri(uri);
    }

    @DataProvider
    public Object[][] validUris() {

        return new Object[][] {
                {"https://example.com"}, {"http://example.com/path"}, {"https://example.com/path?query=1"}
        };
    }

    @Test(dataProvider = "validUris")
    public void testValidEndpointUri(String uri) throws WebhookMgtClientException {

        validator.validateEndpointUri(uri);
    }

    @DataProvider
    public Object[][] invalidSecrets() {

        return new Object[][] {
                {""}, {"\n"}, {String.join("", Collections.nCopies(101, "a"))}
        };
    }

    @Test(dataProvider = "invalidSecrets", expectedExceptions = WebhookMgtClientException.class)
    public void testInvalidSecret(String secret) throws WebhookMgtClientException {

        validator.validateWebhookSecret(secret);
    }

    @DataProvider
    public Object[][] validSecrets() {

        return new Object[][] {
                {"secret"}, {"123456"}, {"!@#$_-+="}, {String.join("", Collections.nCopies(100, "a"))}
        };
    }

    @Test(dataProvider = "validSecrets")
    public void testValidSecret(String secret) throws WebhookMgtClientException {

        validator.validateWebhookSecret(secret);
    }

    @Test(expectedExceptions = WebhookMgtClientException.class)
    public void testValidateChannelsSubscribedNullList() throws WebhookMgtException {

        validator.validateChannelsSubscribed("profile", null);
    }

    @Test(expectedExceptions = WebhookMgtClientException.class)
    public void testValidateChannelsSubscribedEmptyList() throws WebhookMgtException {

        validator.validateChannelsSubscribed("profile", Collections.emptyList());
    }

    @Test
    public void testValidateChannelsSubscribedValid() throws Exception {

        WebhookMetadataService metadataService = Mockito.mock(WebhookMetadataService.class);
        Channel channel = new Channel("channel1", "description1", "event1", Collections.emptyList());
        EventProfile profile = new EventProfile(
                "profile1", "uri1", Collections.singletonList(channel));
        List<EventProfile> profiles = Collections.singletonList(profile);

        Subscription subscription = Subscription.builder().channelUri("event1").build();

        try (MockedStatic<WebhookManagementComponentServiceHolder> mockedHolder = Mockito.mockStatic(
                WebhookManagementComponentServiceHolder.class)) {
            WebhookManagementComponentServiceHolder holder =
                    Mockito.mock(WebhookManagementComponentServiceHolder.class);
            Mockito.when(holder.getWebhookMetadataService()).thenReturn(metadataService);
            Mockito.when(metadataService.getSupportedEventProfiles()).thenReturn(profiles);
            mockedHolder.when(WebhookManagementComponentServiceHolder::getInstance).thenReturn(holder);

            validator.validateChannelsSubscribed("profile1", Collections.singletonList(subscription));
        }
    }

    @Test(expectedExceptions = WebhookMgtClientException.class)
    public void testValidateChannelsSubscribedInvalidProfile() throws Exception {

        WebhookMetadataService metadataService = Mockito.mock(WebhookMetadataService.class);
        EventProfile profile = new EventProfile("profile1", "uri1", Collections.emptyList());
        List<EventProfile> profiles = Collections.singletonList(profile);

        Subscription subscription = Subscription.builder().channelUri("event1").build();

        try (MockedStatic<WebhookManagementComponentServiceHolder> mockedHolder = Mockito.mockStatic(
                WebhookManagementComponentServiceHolder.class)) {
            WebhookManagementComponentServiceHolder holder =
                    Mockito.mock(WebhookManagementComponentServiceHolder.class);
            Mockito.when(holder.getWebhookMetadataService()).thenReturn(metadataService);
            Mockito.when(metadataService.getSupportedEventProfiles()).thenReturn(profiles);
            mockedHolder.when(WebhookManagementComponentServiceHolder::getInstance).thenReturn(holder);

            validator.validateChannelsSubscribed("profile2", Collections.singletonList(subscription));
        }
    }

    @Test(expectedExceptions = WebhookMgtClientException.class)
    public void testValidateChannelsSubscribedInvalidChannel() throws Exception {

        WebhookMetadataService metadataService = Mockito.mock(WebhookMetadataService.class);
        Channel channel = new Channel("channel1", "description1", "uri1", Collections.emptyList());
        EventProfile profile = new EventProfile("profile1", "uri1", Collections.singletonList(channel));
        List<EventProfile> profiles = Collections.singletonList(profile);

        Subscription subscription = Subscription.builder().channelUri("event2").build();

        try (MockedStatic<WebhookManagementComponentServiceHolder> mockedHolder = Mockito.mockStatic(
                WebhookManagementComponentServiceHolder.class)) {
            WebhookManagementComponentServiceHolder holder =
                    Mockito.mock(WebhookManagementComponentServiceHolder.class);
            Mockito.when(holder.getWebhookMetadataService()).thenReturn(metadataService);
            Mockito.when(metadataService.getSupportedEventProfiles()).thenReturn(profiles);
            mockedHolder.when(WebhookManagementComponentServiceHolder::getInstance).thenReturn(holder);

            validator.validateChannelsSubscribed("profile1", Collections.singletonList(subscription));
        }
    }
}
