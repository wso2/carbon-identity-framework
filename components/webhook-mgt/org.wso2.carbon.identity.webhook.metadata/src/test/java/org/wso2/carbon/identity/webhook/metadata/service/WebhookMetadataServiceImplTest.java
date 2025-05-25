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

package org.wso2.carbon.identity.webhook.metadata.service;

import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.webhook.metadata.api.exception.WebhookMetadataClientException;
import org.wso2.carbon.identity.webhook.metadata.api.exception.WebhookMetadataServerException;
import org.wso2.carbon.identity.webhook.metadata.api.model.Event;
import org.wso2.carbon.identity.webhook.metadata.api.model.EventProfile;
import org.wso2.carbon.identity.webhook.metadata.internal.dao.impl.FileBasedWebhookMetadataDAOImpl;
import org.wso2.carbon.identity.webhook.metadata.internal.service.impl.WebhookMetadataServiceImpl;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.when;

/**
 * Unit tests for WebhookMetadataServiceImpl.
 */
public class WebhookMetadataServiceImplTest {

    private static final String TEST_PROFILE_NAME = "Test";
    private static final String TEST_SCHEMA_URI = "https://schemas.identity.wso2.org/events/test";

    @Mock
    private FileBasedWebhookMetadataDAOImpl mockDAO;

    private WebhookMetadataServiceImpl service;

    @BeforeMethod
    public void setUp() throws Exception {

        MockitoAnnotations.openMocks(this);

        service = WebhookMetadataServiceImpl.getInstance();

        Field daoField = WebhookMetadataServiceImpl.class.getDeclaredField("webhookMetadataDAO");
        daoField.setAccessible(true);
        daoField.set(service, mockDAO);
    }

    @AfterMethod
    public void tearDown() {
        // No cleanup needed
    }

    @Test
    public void testGetSupportedEventProfiles() throws Exception {

        List<String> profiles = Arrays.asList(TEST_PROFILE_NAME, "AnotherProfile");
        when(mockDAO.getSupportedEventProfiles()).thenReturn(profiles);

        List<String> result = service.getSupportedEventProfiles();

        Assert.assertNotNull(result);
        Assert.assertEquals(result.size(), 2);
        Assert.assertEquals(result.get(0), TEST_PROFILE_NAME);
    }

    @Test(expectedExceptions = WebhookMetadataServerException.class)
    public void testGetSupportedEventProfilesException() throws Exception {

        when(mockDAO.getSupportedEventProfiles()).thenThrow(new RuntimeException("Test exception"));

        service.getSupportedEventProfiles();
    }

    @Test
    public void testGetEventProfile() throws Exception {

        EventProfile mockProfile = new EventProfile();
        mockProfile.setProfile(TEST_PROFILE_NAME);
        when(mockDAO.getEventProfile(TEST_PROFILE_NAME)).thenReturn(mockProfile);

        EventProfile result = service.getEventProfile(TEST_PROFILE_NAME);

        Assert.assertNotNull(result);
        Assert.assertEquals(result.getProfile(), TEST_PROFILE_NAME);
    }

    @Test(expectedExceptions = WebhookMetadataClientException.class)
    public void testGetEventProfileNotFound() throws Exception {

        when(mockDAO.getEventProfile(TEST_PROFILE_NAME)).thenReturn(null);

        service.getEventProfile(TEST_PROFILE_NAME);
    }

    @Test(expectedExceptions = WebhookMetadataServerException.class)
    public void testGetEventProfileException() throws Exception {

        when(mockDAO.getEventProfile(TEST_PROFILE_NAME)).thenThrow(new RuntimeException("Test exception"));

        service.getEventProfile(TEST_PROFILE_NAME);
    }

    @Test
    public void testGetEventsBySchema() throws Exception {

        Event event1 = new Event("Event 1", "Description 1", "uri1");
        Event event2 = new Event("Event 2", "Description 2", "uri2");
        List<Event> events = Arrays.asList(event1, event2);
        when(mockDAO.getEventsBySchema(TEST_SCHEMA_URI)).thenReturn(events);

        List<Event> result = service.getEventsBySchema(TEST_SCHEMA_URI);

        Assert.assertNotNull(result);
        Assert.assertEquals(result.size(), 2);
        Assert.assertEquals(result.get(0).getName(), "Event 1");
    }

    @Test
    public void testGetEventsBySchemaEmpty() throws Exception {

        when(mockDAO.getEventsBySchema(TEST_SCHEMA_URI)).thenReturn(Collections.emptyList());

        List<Event> result = service.getEventsBySchema(TEST_SCHEMA_URI);

        Assert.assertNotNull(result);
        Assert.assertEquals(result.size(), 0);
    }

    @Test(expectedExceptions = WebhookMetadataServerException.class)
    public void testGetEventsBySchemaException() throws Exception {

        when(mockDAO.getEventsBySchema(TEST_SCHEMA_URI)).thenThrow(new RuntimeException("Test exception"));

        service.getEventsBySchema(TEST_SCHEMA_URI);
    }
}
