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
import org.wso2.carbon.identity.webhook.metadata.api.exception.WebhookMetadataException;
import org.wso2.carbon.identity.webhook.metadata.api.exception.WebhookMetadataServerException;
import org.wso2.carbon.identity.webhook.metadata.api.model.Adapter;
import org.wso2.carbon.identity.webhook.metadata.api.model.AdapterType;
import org.wso2.carbon.identity.webhook.metadata.internal.dao.impl.FileBasedEventAdapterMetadataDAOImpl;

import java.io.File;
import java.io.FileWriter;
import java.lang.reflect.Field;
import java.net.URL;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;

/**
 * Unit tests for FileBasedEventAdapterMetadataDAOImpl.
 */
public class FileBasedEventAdapterMetadataDAOImplTest {

    private static final String CONFIG_FILE_NAME = "identity-outbound-adapter.properties";
    private FileBasedEventAdapterMetadataDAOImpl dao;
    private File tempDir;
    private File configFile;

    @BeforeMethod
    public void setUp() throws Exception {

        MockitoAnnotations.openMocks(this);

        tempDir = Files.createTempDirectory("adapter-metadata-test").toFile();

        URL root = this.getClass().getClassLoader().getResource(".");
        File file = new File(root.getPath());
        System.setProperty("carbon.home", file.getAbsolutePath());

        // Create a config file with two adapters, one enabled
        configFile = new File(tempDir, CONFIG_FILE_NAME);
        try (FileWriter writer = new FileWriter(configFile)) {
            writer.write("adapter.websubhub.enabled=true\n");
            writer.write("adapter.websubhub.baseUrl=http://localhost:9000/hub\n");
            writer.write("adapter.websubhub.type=PublisherSubscriber\n");
            writer.write("adapter.httppublisher.enabled=false\n");
            writer.write("adapter.httppublisher.type=Publisher\n");
        }

        // Point FileBasedEventAdapterMetadataDAOImpl.configPath to the temp config file
        Class<?> utilClass =
                Class.forName(
                        "org.wso2.carbon.identity.webhook.metadata.internal.dao.impl." +
                                "FileBasedEventAdapterMetadataDAOImpl");
        Field field = utilClass.getDeclaredField("configPath");
        field.setAccessible(true);
        field.set(null, tempDir.toPath().resolve(CONFIG_FILE_NAME));

        dao = FileBasedEventAdapterMetadataDAOImpl.getInstance();
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
    public void testGetAdapters() throws Exception {

        List<Adapter> adapters = dao.getAdapters();
        Assert.assertNotNull(adapters);
        Assert.assertEquals(adapters.size(), 2);
        Assert.assertTrue(adapters.stream().anyMatch(a -> "websubhub".equals(a.getName())));
        Assert.assertTrue(adapters.stream().anyMatch(a -> "httppublisher".equals(a.getName())));
    }

    @Test
    public void testEnabledAdapter() throws Exception {

        List<Adapter> adapters = dao.getAdapters();
        Adapter enabled = adapters.stream().filter(Adapter::isEnabled).findFirst().orElse(null);
        Assert.assertNotNull(enabled);
        Assert.assertEquals(enabled.getName(), "websubhub");
        Assert.assertEquals(enabled.getType(), AdapterType.PublisherSubscriber);
        Assert.assertEquals(enabled.getProperties().get("baseUrl"), "http://localhost:9000/hub");
    }

    @Test
    public void testDisabledAdapter() throws Exception {

        List<Adapter> adapters = dao.getAdapters();
        Adapter disabled = adapters.stream().filter(a -> "httppublisher".equals(a.getName())).findFirst().orElse(null);
        Assert.assertNotNull(disabled);
        Assert.assertFalse(disabled.isEnabled());
        Assert.assertEquals(disabled.getType(), AdapterType.Publisher);
    }

    @Test(expectedExceptions = WebhookMetadataException.class)
    public void testGetAdaptersNotInitialized() throws Exception {

        resetDAOState(dao);
        dao.getAdapters();
    }

    @Test(expectedExceptions = WebhookMetadataServerException.class)

    public void testInitWithMissingConfigFile() throws Exception {
        // Remove config file
        if (configFile.exists()) {
            configFile.delete();
        }
        // Point configPath to the missing file
        Class<?> utilClass =
                Class.forName(
                        "org.wso2.carbon.identity.webhook.metadata.internal.dao.impl." +
                                "FileBasedEventAdapterMetadataDAOImpl");
        Field field = utilClass.getDeclaredField("configPath");
        field.setAccessible(true);
        field.set(null, configFile.toPath());

        resetDAOState(dao);
        dao.init();
    }

    /**
     * Reset the DAO state using reflection.
     */
    private void resetDAOState(FileBasedEventAdapterMetadataDAOImpl dao) throws Exception {

        Field isInitializedField = FileBasedEventAdapterMetadataDAOImpl.class.getDeclaredField("isInitialized");
        isInitializedField.setAccessible(true);
        isInitializedField.set(dao, false);

        Field adapterCacheField = FileBasedEventAdapterMetadataDAOImpl.class.getDeclaredField("adapterCache");
        adapterCacheField.setAccessible(true);
        Map<String, Adapter> adapterCache = (Map<String, Adapter>) adapterCacheField.get(dao);
        adapterCache.clear();
    }
}
