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

import org.apache.commons.io.FileUtils;
import org.mockito.MockitoAnnotations;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.webhook.metadata.api.exception.WebhookMetadataServerException;
import org.wso2.carbon.identity.webhook.metadata.api.model.Event;
import org.wso2.carbon.identity.webhook.metadata.api.model.EventProfile;
import org.wso2.carbon.identity.webhook.metadata.internal.dao.impl.FileBasedEventProfileMetadataDAOImpl;

import java.io.File;
import java.lang.reflect.Field;
import java.net.URL;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;

/**
 * Unit tests for FileBasedEventProfileMetadataDAOImpl.
 */
public class FileBasedEventProfileMetadataDAOImplTest {

    private static final String TEST_PROFILE_NAME = "Test";
    private static final String TEST_PROFILE_URI = "https://schemas.identity.wso2.org/events/test";
    private FileBasedEventProfileMetadataDAOImpl dao;
    private File tempDir;

    @BeforeMethod
    public void setUp() throws Exception {

        MockitoAnnotations.openMocks(this);

        tempDir = Files.createTempDirectory("webhook-metadata-test").toFile();

        URL root = this.getClass().getClassLoader().getResource(".");
        File file = new File(root.getPath());
        System.setProperty("carbon.home", file.getAbsolutePath());

        dao = FileBasedEventProfileMetadataDAOImpl.getInstance();

        resetDAOState(dao);

        dao.init();
    }

    @AfterMethod
    public void tearDown() throws Exception {

        if (tempDir != null && tempDir.exists()) {
            FileUtils.deleteDirectory(tempDir);
        }
    }

    @Test
    public void testGetSupportedEventProfiles() throws Exception {

        List<EventProfile> profiles = dao.getSupportedEventProfiles();
        Assert.assertNotNull(profiles);
        Assert.assertEquals(profiles.size(), 1);
        Assert.assertEquals(profiles.get(0).getProfile(), TEST_PROFILE_NAME);
    }

    @Test
    public void testGetEventProfile() throws Exception {

        EventProfile profile = dao.getEventProfile(TEST_PROFILE_NAME);
        Assert.assertNotNull(profile);
        Assert.assertEquals(profile.getProfile(), TEST_PROFILE_NAME);
        Assert.assertNotNull(profile.getChannels());
        Assert.assertEquals(profile.getChannels().size(), 1);
        Assert.assertEquals(profile.getChannels().get(0).getName(), "Test Channel");
        Assert.assertEquals(profile.getChannels().get(0).getDescription(), "This is a test channel");
    }

    @Test
    public void testGetEventProfileNotFound() throws Exception {

        EventProfile profile = dao.getEventProfile("NonExistentProfile");
        Assert.assertNull(profile);
    }

    @Test(expectedExceptions = WebhookMetadataServerException.class)
    public void testGetEventProfilesDirectoryNotExist() throws Exception {

        File tempCarbonHome = Files.createTempDirectory("webhook-metadata-missing-dir").toFile();
        System.setProperty("carbon.home", tempCarbonHome.getAbsolutePath());

        // Reset the static cache in WebhookMetadataUtil
        Class<?> utilClass =
                Class.forName("org.wso2.carbon.identity.webhook.metadata.internal.util.WebhookMetadataUtil");
        Field field = utilClass.getDeclaredField("eventProfilesDirectory");
        field.setAccessible(true);
        field.set(null, null);

        try {
            utilClass.getMethod("getEventProfilesDirectory").invoke(null);
        } catch (java.lang.reflect.InvocationTargetException e) {
            // Rethrow the real cause so TestNG can catch it
            throw (Exception) e.getCause();
        }
    }

    @Test(expectedExceptions = org.wso2.carbon.identity.webhook.metadata.api.exception.WebhookMetadataException.class)
    public void testGetSupportedEventProfilesNotInitialized() throws Exception {

        resetDAOState(dao);
        dao.getSupportedEventProfiles();
    }

    @Test(expectedExceptions = org.wso2.carbon.identity.webhook.metadata.api.exception.WebhookMetadataException.class)
    public void testGetEventProfileNotInitialized() throws Exception {

        resetDAOState(dao);
        dao.getEventProfile(TEST_PROFILE_NAME);
    }

    @Test
    public void testProfileNameFallbackToFileName() throws Exception {
        // Create a JSON file with no "profile" field
        String json =
                "{ \"channels\": [{ \"name\": \"Channel1\", \"uri\": \"" + TEST_PROFILE_URI + "\", \"events\": [] }] }";
        File profileFile = new File(tempDir, "fallbackProfile.json");
        FileUtils.writeStringToFile(profileFile, json, "UTF-8");

        // Point the util to the tempDir
        Class<?> utilClass =
                Class.forName("org.wso2.carbon.identity.webhook.metadata.internal.util.WebhookMetadataUtil");
        Field field = utilClass.getDeclaredField("eventProfilesDirectory");
        field.setAccessible(true);
        field.set(null, tempDir.toPath());

        resetDAOState(dao);
        dao.init();

        List<EventProfile> profiles = dao.getSupportedEventProfiles();
        Assert.assertTrue(profiles.stream().anyMatch(p -> "fallbackProfile".equals(p.getProfile())));
        EventProfile profile = dao.getEventProfile("fallbackProfile");
        Assert.assertNotNull(profile);
        Assert.assertEquals(profile.getProfile(), "fallbackProfile");
    }

    @Test
    public void testChannelConstructorAndGetters() {

        String name = "Test Channel";
        String description = "Channel for testing";
        String uri = "https://test.uri";
        Event event = new Event(); // Use a real Event or a mock if needed
        List<Event> events = java.util.Collections.singletonList(event);

        org.wso2.carbon.identity.webhook.metadata.api.model.Channel channel =
                new org.wso2.carbon.identity.webhook.metadata.api.model.Channel(name, description, uri, events);

        Assert.assertEquals(channel.getName(), name);
        Assert.assertEquals(channel.getDescription(), description);
        Assert.assertEquals(channel.getUri(), uri);
        Assert.assertEquals(channel.getEvents(), events);
    }

    /**
     * Reset the DAO state using reflection.
     *
     * @param dao DAO instance to reset
     * @throws Exception If an error occurs
     */
    private void resetDAOState(FileBasedEventProfileMetadataDAOImpl dao) throws Exception {

        Field isInitializedField = FileBasedEventProfileMetadataDAOImpl.class.getDeclaredField("isInitialized");
        isInitializedField.setAccessible(true);
        isInitializedField.set(dao, false);

        Field profileCacheField = FileBasedEventProfileMetadataDAOImpl.class.getDeclaredField("profileCache");
        profileCacheField.setAccessible(true);
        Map<String, EventProfile> profileCache = (Map<String, EventProfile>) profileCacheField.get(dao);
        profileCache.clear();
    }
}
