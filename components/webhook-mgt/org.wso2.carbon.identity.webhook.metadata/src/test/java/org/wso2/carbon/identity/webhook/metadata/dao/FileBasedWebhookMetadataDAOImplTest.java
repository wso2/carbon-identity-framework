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
import org.wso2.carbon.identity.webhook.metadata.api.model.Event;
import org.wso2.carbon.identity.webhook.metadata.api.model.EventProfile;
import org.wso2.carbon.identity.webhook.metadata.internal.dao.impl.FileBasedWebhookMetadataDAOImpl;

import java.io.File;
import java.lang.reflect.Field;
import java.net.URL;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;

/**
 * Unit tests for FileBasedWebhookMetadataDAOImpl.
 */
public class FileBasedWebhookMetadataDAOImplTest {

    private static final String TEST_PROFILE_NAME = "Test";
    private static final String TEST_PROFILE_URI = "https://schemas.identity.wso2.org/events/test";
    private FileBasedWebhookMetadataDAOImpl dao;
    private File tempDir;

    @BeforeMethod
    public void setUp() throws Exception {

        MockitoAnnotations.openMocks(this);

        tempDir = Files.createTempDirectory("webhook-metadata-test").toFile();

        URL root = this.getClass().getClassLoader().getResource(".");
        File file = new File(root.getPath());
        System.setProperty("carbon.home", file.getAbsolutePath());

        dao = FileBasedWebhookMetadataDAOImpl.getInstance();

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

        List<String> profiles = dao.getSupportedEventProfiles();
        Assert.assertNotNull(profiles);
        Assert.assertEquals(profiles.size(), 1);
        Assert.assertEquals(profiles.get(0), TEST_PROFILE_NAME);
    }

    @Test
    public void testGetEventProfile() throws Exception {

        EventProfile profile = dao.getEventProfile(TEST_PROFILE_NAME);
        Assert.assertNotNull(profile);
        Assert.assertEquals(profile.getProfile(), TEST_PROFILE_NAME);
        Assert.assertNotNull(profile.getChannels());
        Assert.assertEquals(profile.getChannels().size(), 1);
        Assert.assertEquals(profile.getChannels().get(0).getName(), "Test Channel");
    }

    @Test
    public void testGetEventProfileNotFound() throws Exception {

        EventProfile profile = dao.getEventProfile("NonExistentProfile");
        Assert.assertNull(profile);
    }

    @Test
    public void testGetEventsByProfile() throws Exception {

        List<Event> events = dao.getEventsByProfile(TEST_PROFILE_URI);
        Assert.assertNotNull(events);
        Assert.assertEquals(events.size(), 2);
        Assert.assertEquals(events.get(0).getEventName(), "Test Event 1");
        Assert.assertEquals(events.get(1).getEventName(), "Test Event 2");
    }

    @Test
    public void testGetEventsByProfileNotFound() throws Exception {

        List<Event> events = dao.getEventsByProfile("https://nonexistent.uri");
        Assert.assertNotNull(events);
        Assert.assertEquals(events.size(), 0);
    }

    /**
     * Reset the DAO state using reflection.
     *
     * @param dao DAO instance to reset
     * @throws Exception If an error occurs
     */
    private void resetDAOState(FileBasedWebhookMetadataDAOImpl dao) throws Exception {

        Field isInitializedField = FileBasedWebhookMetadataDAOImpl.class.getDeclaredField("isInitialized");
        isInitializedField.setAccessible(true);
        isInitializedField.set(dao, false);

        Field profileCacheField = FileBasedWebhookMetadataDAOImpl.class.getDeclaredField("profileCache");
        profileCacheField.setAccessible(true);
        Map<String, EventProfile> profileCache = (Map<String, EventProfile>) profileCacheField.get(dao);
        profileCache.clear();
    }
}
