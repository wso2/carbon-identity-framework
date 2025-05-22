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
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.webhook.metadata.api.exception.WebhookMetadataException;
import org.wso2.carbon.identity.webhook.metadata.api.exception.WebhookMetadataClientException;
import org.wso2.carbon.identity.webhook.metadata.api.exception.WebhookMetadataServerException;
import org.wso2.carbon.identity.webhook.metadata.api.model.EventChannel;
import org.wso2.carbon.identity.webhook.metadata.api.model.EventProfile;
import org.wso2.carbon.identity.webhook.metadata.api.model.EventType;
import org.wso2.carbon.identity.webhook.metadata.api.model.ProfileType;
import org.wso2.carbon.identity.webhook.metadata.api.service.WebhookMetadataService;
import org.wso2.carbon.identity.webhook.metadata.internal.dao.WebhookMetadataDAO;
import org.wso2.carbon.identity.webhook.metadata.internal.service.impl.WebhookMetadataServiceImpl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

/**
 * Unit tests for {@link WebhookMetadataServiceImpl}.
 */
public class WebhookMetadataServiceTest {

    @Mock
    private WebhookMetadataDAO webhookMetadataDAO;

    private WebhookMetadataService webhookMetadataService;

    @BeforeClass
    public void setUpClass() {

        MockitoAnnotations.openMocks(this);
        webhookMetadataService = new WebhookMetadataServiceImpl(webhookMetadataDAO);
    }

    @Test
    public void testGetEventProfile() throws WebhookMetadataException {
        // Mock the DAO response
        EventProfile mockProfile = createMockEventProfile();
        when(webhookMetadataDAO.getEventProfile(ProfileType.WSO2_EVENT)).thenReturn(mockProfile);

        // Call the service method
        EventProfile result = webhookMetadataService.getEventProfile(ProfileType.WSO2_EVENT);

        // Assertions
        assertNotNull(result);
        assertNotNull(result.getChannels());
        assertEquals(result.getChannels().size(), 1);
        assertTrue(result.getChannels().containsKey("Test_Channel"));

        // Verify channel
        EventChannel channel = result.getChannels().get("Test_Channel");
        assertEquals(channel.getName(), "Test Channel");
        assertEquals(channel.getEvents().size(), 2);

        // Verify events
        EventType event = channel.getEvents().get(0);
        assertEquals(event.getName(), "Test Event 1");
        assertEquals(event.getUri(), "https://test/event1");
    }

    @Test
    public void testGetAllEventProfiles() throws WebhookMetadataException {
        // Mock the DAO responses for each profile type
        EventProfile mockWso2Profile = createMockEventProfile();
        EventProfile mockCaepProfile = createMockCaepProfile();
        EventProfile mockRiscProfile = createMockRiscProfile();

        when(webhookMetadataDAO.getEventProfile(ProfileType.WSO2_EVENT)).thenReturn(mockWso2Profile);
        when(webhookMetadataDAO.getEventProfile(ProfileType.CAEP_EVENT)).thenReturn(mockCaepProfile);
        when(webhookMetadataDAO.getEventProfile(ProfileType.RISC_EVENT)).thenReturn(mockRiscProfile);

        // Call the service method
        EventProfile[] profiles = webhookMetadataService.getAllEventProfiles();

        // Assertions
        assertNotNull(profiles);
        assertEquals(profiles.length, 3);

        // The order of profiles in the array should match the order of ProfileType enum values
        assertTrue(profiles[0].getChannels().containsKey("Test_Channel"));
        assertTrue(profiles[1].getChannels().containsKey("CAEP_Channel"));
        assertTrue(profiles[2].getChannels().containsKey("RISC_Channel"));
    }

    private EventProfile createMockEventProfile() {

        EventProfile profile = new EventProfile();
        Map<String, EventChannel> channels = new HashMap<>();
        EventChannel channel = new EventChannel();

        channel.setName("Test Channel");
        channel.setDescription("Test Channel Description");
        channel.setUri("https://test/channel");

        List<EventType> events = new ArrayList<>();
        EventType event1 = new EventType();
        event1.setName("Test Event 1");
        event1.setDescription("Test Event 1 Description");
        event1.setUri("https://test/event1");
        events.add(event1);

        EventType event2 = new EventType();
        event2.setName("Test Event 2");
        event2.setDescription("Test Event 2 Description");
        event2.setUri("https://test/event2");
        events.add(event2);

        channel.setEvents(events);
        channels.put("Test_Channel", channel);
        profile.setChannels(channels);

        return profile;
    }

    private EventProfile createMockCaepProfile() {

        EventProfile profile = new EventProfile();
        Map<String, EventChannel> channels = new HashMap<>();
        EventChannel channel = new EventChannel();

        channel.setName("CAEP Channel");
        channel.setUri("https://caep/channel");

        List<EventType> events = new ArrayList<>();
        EventType event = new EventType();
        event.setName("CAEP Event");
        event.setUri("https://caep/event");
        events.add(event);

        channel.setEvents(events);
        channels.put("CAEP_Channel", channel);
        profile.setChannels(channels);

        return profile;
    }

    private EventProfile createMockRiscProfile() {

        EventProfile profile = new EventProfile();
        Map<String, EventChannel> channels = new HashMap<>();
        EventChannel channel = new EventChannel();

        channel.setName("RISC Channel");
        channel.setUri("https://risc/channel");

        List<EventType> events = new ArrayList<>();
        EventType event = new EventType();
        event.setName("RISC Event");
        event.setUri("https://risc/event");
        events.add(event);

        channel.setEvents(events);
        channels.put("RISC_Channel", channel);
        profile.setChannels(channels);

        return profile;
    }

    private void assertTrue(boolean condition) {

        org.testng.Assert.assertTrue(condition);
    }

    @Test(expectedExceptions = WebhookMetadataException.class)
    public void testGetEventProfileWithException() throws WebhookMetadataException {
        // Mock the DAO to throw an exception
        when(webhookMetadataDAO.getEventProfile(ProfileType.WSO2_EVENT))
                .thenThrow(new WebhookMetadataServerException("Error loading profile"));

        // This should propagate the exception
        webhookMetadataService.getEventProfile(ProfileType.WSO2_EVENT);
    }

    @Test
    public void testGetEventProfileByName() throws WebhookMetadataException {
        // Mock the DAO response
        EventProfile mockProfile = createMockEventProfile();
        when(webhookMetadataDAO.getEventProfile(ProfileType.WSO2_EVENT)).thenReturn(mockProfile);

        // Call the service method
        EventProfile result = webhookMetadataService.getEventProfile(ProfileType.WSO2_EVENT);

        // Assertions
        assertNotNull(result);
        assertNotNull(result.getChannels());
        assertEquals(result.getChannels().size(), 1);
    }

    @Test(expectedExceptions = WebhookMetadataClientException.class)
    public void testGetEventProfileByInvalidName() throws WebhookMetadataException {
        // Call with an invalid profile type name
        webhookMetadataService.getEventProfile(ProfileType.valueOf("INVALID_PROFILE"));
    }
}
