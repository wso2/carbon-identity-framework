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
import org.wso2.carbon.identity.webhook.metadata.api.exception.WebhookMetadataServerException;
import org.wso2.carbon.identity.webhook.metadata.api.model.Event;
import org.wso2.carbon.identity.webhook.metadata.api.model.EventProfile;
import org.wso2.carbon.identity.webhook.metadata.internal.dao.impl.FileBasedWebhookMetadataDAOImpl;
import org.wso2.carbon.identity.webhook.metadata.internal.service.impl.WebhookMetadataServiceImpl;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.when;

/**
 * Unit tests for WebhookMetadataServiceImpl.
 */
public class WebhookMetadataServiceImplTest {

    private static final String TEST_PROFILE_NAME = "Test";
    private static final String TEST_PROFILE_URI = "https://schemas.identity.wso2.org/events/test";
    private static final String TEST_URI = "https://schemas.identity.wso2.org";

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

        List<EventProfile> profiles = Arrays.asList(
                new EventProfile(TEST_PROFILE_NAME, "https://schemas.identity.wso2.org/events/test",
                        new ArrayList<>()),
                new EventProfile("AnotherProfile", "https://schemas.identity.wso2.org/events/another",
                        new ArrayList<>())
        );
        when(mockDAO.getSupportedEventProfiles()).thenReturn(profiles);

        List<EventProfile> result = service.getSupportedEventProfiles();

        Assert.assertNotNull(result);
        Assert.assertEquals(result.size(), 2);
        Assert.assertEquals(result.get(0).getProfile(), TEST_PROFILE_NAME);
    }

    @Test(expectedExceptions = WebhookMetadataServerException.class)
    public void testGetSupportedEventProfilesException() throws Exception {

        when(mockDAO.getSupportedEventProfiles()).thenThrow(new RuntimeException("Test exception"));

        service.getSupportedEventProfiles();
    }

    @Test
    public void testGetEventProfile() throws Exception {

        EventProfile mockProfile = new EventProfile(TEST_PROFILE_NAME, TEST_URI, new ArrayList<>());
        when(mockDAO.getEventProfile(TEST_PROFILE_NAME)).thenReturn(mockProfile);

        EventProfile result = service.getEventProfile(TEST_PROFILE_NAME);

        Assert.assertNotNull(result);
        Assert.assertEquals(result.getProfile(), TEST_PROFILE_NAME);
    }

    @Test
    public void testGetEventProfileNotFound() throws Exception {

        when(mockDAO.getEventProfile(TEST_PROFILE_NAME)).thenReturn(null);

        EventProfile result = service.getEventProfile(TEST_PROFILE_NAME);
        Assert.assertNull(result, "Expected null when event profile is not found");
    }

    @Test(expectedExceptions = WebhookMetadataServerException.class)
    public void testGetEventProfileException() throws Exception {

        when(mockDAO.getEventProfile(TEST_PROFILE_NAME)).thenThrow(new RuntimeException("Test exception"));

        service.getEventProfile(TEST_PROFILE_NAME);
    }

    @Test
    public void testGetEventsByProfileURI() throws Exception {

        Event event1 = new Event("Event 1", "Description 1", "uri1");
        Event event2 = new Event("Event 2", "Description 2", "uri2");
        List<Event> events = Arrays.asList(event1, event2);
        when(mockDAO.getEventsByProfile(TEST_PROFILE_URI)).thenReturn(events);

        List<Event> result = service.getEventsByProfileURI(TEST_PROFILE_URI);

        Assert.assertNotNull(result);
        Assert.assertEquals(result.size(), 2);
        Assert.assertEquals(result.get(0).getEventName(), "Event 1");
        Assert.assertEquals(result.get(0).getEventDescription(), "Description 1");
        Assert.assertEquals(result.get(0).getEventUri(), "uri1");
        Assert.assertEquals(result.get(1).getEventName(), "Event 2");
        Assert.assertEquals(result.get(1).getEventDescription(), "Description 2");
        Assert.assertEquals(result.get(1).getEventUri(), "uri2");
    }

    @Test
    public void testGetEventsByProfileURIEmpty() throws Exception {

        when(mockDAO.getEventsByProfile(TEST_PROFILE_URI)).thenReturn(Collections.emptyList());

        List<Event> result = service.getEventsByProfileURI(TEST_PROFILE_URI);

        Assert.assertNotNull(result);
        Assert.assertEquals(result.size(), 0);
    }

    @Test(expectedExceptions = WebhookMetadataServerException.class)
    public void testGetEventsByProfileURIException() throws Exception {

        when(mockDAO.getEventsByProfile(TEST_PROFILE_URI)).thenThrow(new RuntimeException("Test exception"));

        service.getEventsByProfileURI(TEST_PROFILE_URI);
    }
}
