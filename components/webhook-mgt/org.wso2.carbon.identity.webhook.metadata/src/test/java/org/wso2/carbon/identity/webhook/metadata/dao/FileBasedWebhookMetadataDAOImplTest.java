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

package org.wso2.carbon.identity.webhook.metadata.dao;

import org.mockito.MockedStatic;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.webhook.metadata.api.exception.WebhookMetadataClientException;
import org.wso2.carbon.identity.webhook.metadata.api.exception.WebhookMetadataException;
import org.wso2.carbon.identity.webhook.metadata.api.exception.WebhookMetadataServerException;
import org.wso2.carbon.identity.webhook.metadata.api.model.EventChannel;
import org.wso2.carbon.identity.webhook.metadata.api.model.EventProfile;
import org.wso2.carbon.identity.webhook.metadata.api.model.EventType;
import org.wso2.carbon.identity.webhook.metadata.api.model.ProfileType;
import org.wso2.carbon.identity.webhook.metadata.internal.dao.impl.FileBasedWebhookMetadataDAOImpl;
import org.wso2.carbon.utils.CarbonUtils;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static org.mockito.Mockito.mockStatic;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertSame;
import static org.testng.Assert.assertTrue;

/**
 * Unit tests for {@link FileBasedWebhookMetadataDAOImpl}.
 */
public class FileBasedWebhookMetadataDAOImplTest {

    private FileBasedWebhookMetadataDAOImpl webhookMetadataDAOImpl;
    private MockedStatic<CarbonUtils> carbonUtilsMockedStatic;
    private String testResourcesPath;

    @BeforeClass
    public void setUpClass() {

        webhookMetadataDAOImpl = FileBasedWebhookMetadataDAOImpl.getInstance();
        testResourcesPath = Paths.get(Objects.requireNonNull(getClass().getClassLoader().getResource("profiles"))
                .getPath()).toString();

        // Mock CarbonUtils.getCarbonConfigDirPath() to return our test resources path
        carbonUtilsMockedStatic = mockStatic(CarbonUtils.class);
        carbonUtilsMockedStatic.when(CarbonUtils::getCarbonConfigDirPath).thenReturn(testResourcesPath);
    }

    @AfterClass
    public void tearDownClass() {

        carbonUtilsMockedStatic.close();
    }

    @Test
    public void testGetEventProfile() throws WebhookMetadataException {
        // Test for WSO2_EVENT profile
        EventProfile wso2EventProfile = webhookMetadataDAOImpl.getEventProfile(ProfileType.WSO2_EVENT);
        assertNotNull(wso2EventProfile);
        assertNotNull(wso2EventProfile.getChannels());

        // Check if the channels map contains the expected test channel
        Map<String, EventChannel> channels = wso2EventProfile.getChannels();
        assertTrue(channels.containsKey("Test_Channel"));

        // Verify the channel properties
        EventChannel testChannel = channels.get("Test_Channel");
        assertEquals(testChannel.getName(), "Test Channel");
        assertEquals(testChannel.getDescription(), "This is a test channel");
        assertEquals(testChannel.getUri(), "https://schemas.identity.wso2.org/events/test");

        // Verify the events in the channel
        List<EventType> events = testChannel.getEvents();
        assertNotNull(events);
        assertEquals(events.size(), 2);

        // Check first event
        EventType event1 = events.get(0);
        assertEquals(event1.getName(), "Test Event 1");
        assertEquals(event1.getDescription(), "This is test event 1");
        assertEquals(event1.getUri(), "https://schemas.identity.wso2.org/events/test/event-type/test1");

        // Check second event
        EventType event2 = events.get(1);
        assertEquals(event2.getName(), "Test Event 2");
        assertEquals(event2.getDescription(), "This is test event 2");
        assertEquals(event2.getUri(), "https://schemas.identity.wso2.org/events/test/event-type/test2");
    }

    @Test
    public void testCaching() throws WebhookMetadataException {
        // First call should read from file
        EventProfile profile1 = webhookMetadataDAOImpl.getEventProfile(ProfileType.WSO2_EVENT);

        // Second call should read from cache - but we can't easily test this directly
        // Let's just verify that the two calls return the same object
        EventProfile profile2 = webhookMetadataDAOImpl.getEventProfile(ProfileType.WSO2_EVENT);

        assertNotNull(profile1);
        assertNotNull(profile2);
        assertSame(profile1, profile2, "Cached object should be the same instance");

        // Now clear the cache
        webhookMetadataDAOImpl.clearCache();

        // Next call should read from file again
        EventProfile profile3 = webhookMetadataDAOImpl.getEventProfile(ProfileType.WSO2_EVENT);

        assertNotNull(profile3);
        // profile3 should be a different object than profile1 after cache clear
        assertTrue(profile1 != profile3, "New object should be created after cache clear");
    }

    @Test
    public void testGetCAEPEventProfile() throws WebhookMetadataException {
        // Test for CAEP_EVENT profile
        EventProfile caepEventProfile = webhookMetadataDAOImpl.getEventProfile(ProfileType.CAEP_EVENT);
        assertNotNull(caepEventProfile);
        assertNotNull(caepEventProfile.getChannels());

        // Check if the channels map contains the expected test channel
        Map<String, EventChannel> channels = caepEventProfile.getChannels();
        assertTrue(channels.containsKey("CAEP_Channel"));

        // Verify the channel properties
        EventChannel caepChannel = channels.get("CAEP_Channel");
        assertEquals(caepChannel.getName(), "CAEP Channel");
        assertTrue(caepChannel.getEvents().size() > 0, "CAEP channel should have events");
    }

    @Test
    public void testGetRISCEventProfile() throws WebhookMetadataException {
        // Test for RISC_EVENT profile
        EventProfile riscEventProfile = webhookMetadataDAOImpl.getEventProfile(ProfileType.RISC_EVENT);
        assertNotNull(riscEventProfile);
        assertNotNull(riscEventProfile.getChannels());

        // Check if the channels map contains the expected test channel
        Map<String, EventChannel> channels = riscEventProfile.getChannels();
        assertTrue(channels.containsKey("RISC_Channel"));

        // Verify the channel properties
        EventChannel riscChannel = channels.get("RISC_Channel");
        assertEquals(riscChannel.getName(), "RISC Channel");
        assertTrue(riscChannel.getEvents().size() > 0, "RISC channel should have events");
    }

    @Test(expectedExceptions = WebhookMetadataServerException.class)
    public void testFileNotFound() throws WebhookMetadataException {
        // Mock CarbonUtils to return a non-existent path
        carbonUtilsMockedStatic.when(CarbonUtils::getCarbonConfigDirPath).thenReturn("/non/existent/path");

        // Clear the cache to force a file read
        webhookMetadataDAOImpl.clearCache();

        // This should throw an exception since the file doesn't exist
        webhookMetadataDAOImpl.getEventProfile(ProfileType.WSO2_EVENT);
    }

    @Test(expectedExceptions = WebhookMetadataClientException.class)
    public void testInvalidJson() throws Exception {
        // Create a temporary file with invalid JSON
        String testDir = System.getProperty("java.io.tmpdir");
        Path tempFile = Paths.get(testDir, "invalid-wso2-event-profile.json");
        Files.write(tempFile, "{invalid json content".getBytes(StandardCharsets.UTF_8));

        try {
            // Mock CarbonUtils to return our temporary directory
            carbonUtilsMockedStatic.when(CarbonUtils::getCarbonConfigDirPath).thenReturn(testDir);

            // Clear the cache to force a file read
            webhookMetadataDAOImpl.clearCache();

            // This should throw an exception since the file has invalid JSON
            webhookMetadataDAOImpl.getEventProfile(ProfileType.WSO2_EVENT);
        } finally {
            // Cleanup temp file
            Files.deleteIfExists(tempFile);
        }
    }

    @Test
    public void testMultipleProfileTypes() throws WebhookMetadataException {
        // Test that we can load multiple different profile types
        EventProfile wso2Profile = webhookMetadataDAOImpl.getEventProfile(ProfileType.WSO2_EVENT);
        EventProfile caepProfile = webhookMetadataDAOImpl.getEventProfile(ProfileType.CAEP_EVENT);
        EventProfile riscProfile = webhookMetadataDAOImpl.getEventProfile(ProfileType.RISC_EVENT);

        assertNotNull(wso2Profile);
        assertNotNull(caepProfile);
        assertNotNull(riscProfile);

        // Verify they are different profiles
        assertTrue(wso2Profile != caepProfile);
        assertTrue(caepProfile != riscProfile);
        assertTrue(wso2Profile != riscProfile);

        // Check that each profile has the expected channel
        assertTrue(wso2Profile.getChannels().containsKey("Test_Channel"));
        assertTrue(caepProfile.getChannels().containsKey("CAEP_Channel"));
        assertTrue(riscProfile.getChannels().containsKey("RISC_Channel"));
    }
}
