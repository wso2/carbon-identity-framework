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

package org.wso2.carbon.identity.webhook.metadata.internal.dao.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.core.util.IdentityUtil;
import org.wso2.carbon.identity.webhook.metadata.api.exception.WebhookMetadataException;
import org.wso2.carbon.identity.webhook.metadata.api.exception.WebhookMetadataServerException;
import org.wso2.carbon.identity.webhook.metadata.api.model.Adapter;
import org.wso2.carbon.identity.webhook.metadata.api.model.AdapterType;
import org.wso2.carbon.identity.webhook.metadata.internal.constant.WebhookMetadataConstants;
import org.wso2.carbon.identity.webhook.metadata.internal.dao.EventAdapterMetadataDAO;
import org.wso2.carbon.identity.webhook.metadata.internal.util.WebhookMetadataExceptionHandler;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

import static org.wso2.carbon.identity.webhook.metadata.internal.constant.ErrorMessage.ERROR_CODE_ADAPTERS_RETRIEVE_ERROR;
import static org.wso2.carbon.identity.webhook.metadata.internal.constant.ErrorMessage.ERROR_CODE_CONFIG_FILE_READ_ERROR;

/**
 * File-based implementation of the EventAdapterMetadataDAO.
 * This DAO reads event adapter metadata from a configuration file.
 */
public class FileBasedEventAdapterMetadataDAOImpl implements EventAdapterMetadataDAO {

    private static final Log log = LogFactory.getLog(FileBasedEventAdapterMetadataDAOImpl.class);
    private static final FileBasedEventAdapterMetadataDAOImpl INSTANCE = new FileBasedEventAdapterMetadataDAOImpl();
    private static Path configPath;

    // Cache of loaded adapters
    private final Map<String, Adapter> adapterCache = new HashMap<>();
    private boolean isInitialized = false;

    private FileBasedEventAdapterMetadataDAOImpl() {
        // Private constructor for singleton
    }

    public static FileBasedEventAdapterMetadataDAOImpl getInstance() {

        return INSTANCE;
    }

    /**
     * Initialize the DAO by loading all adapters from the config file.
     */
    public synchronized void init() throws WebhookMetadataServerException {

        if (isInitialized) {
            return;
        }
        try {
            loadAdapters();
            isInitialized = true;
        } catch (WebhookMetadataException e) {
            throw WebhookMetadataExceptionHandler.handleServerException(
                    ERROR_CODE_CONFIG_FILE_READ_ERROR, e);
        }
    }

    /**
     * Load all adapters from the config file into the cache.
     */
    private void loadAdapters() throws WebhookMetadataException {

        if (configPath == null) {
            configPath = Paths.get(IdentityUtil.getIdentityConfigDirPath(),
                    WebhookMetadataConstants.AdapterConfig.CONFIG_FILE_NAME);
        }
        Properties properties = new Properties();
        try (InputStream in = Files.newInputStream(configPath)) {
            properties.load(in);
        } catch (Exception e) {
            throw WebhookMetadataExceptionHandler.handleServerException(
                    ERROR_CODE_CONFIG_FILE_READ_ERROR, e, configPath.toString());
        }

        Map<String, Map<String, String>> grouped = new HashMap<>();
        for (String key : properties.stringPropertyNames()) {
            if (key.startsWith(WebhookMetadataConstants.AdapterConfig.ADAPTER_PREFIX)) {
                String[] parts = key.split("\\.");
                if (parts.length >= 3) {
                    String name = parts[1];
                    grouped.computeIfAbsent(name, k -> new HashMap<>())
                            .put(parts[2], properties.getProperty(key));
                }
            }
        }

        adapterCache.clear();
        for (Map.Entry<String, Map<String, String>> entry : grouped.entrySet()) {
            String name = entry.getKey();
            Map<String, String> props = entry.getValue();
            String type = props.getOrDefault(WebhookMetadataConstants.AdapterConfig.TYPE_KEY, "");
            boolean enabled = WebhookMetadataConstants.AdapterConfig.ENABLED_VALUE_TRUE.equalsIgnoreCase(
                    props.getOrDefault(WebhookMetadataConstants.AdapterConfig.ENABLED_KEY, "false"));
            Adapter adapter = new Adapter.Builder()
                    .name(name)
                    .type(AdapterType.valueOf(type))
                    .enabled(enabled)
                    .properties(props)
                    .build();
            adapterCache.put(name, adapter);
        }

        List<Adapter> enabledAdapters = adapterCache.values().stream()
                .filter(Adapter::isEnabled)
                .collect(Collectors.toList());
        if (enabledAdapters.size() > 1) {
            log.warn("Multiple enabled adapters found: " +
                    enabledAdapters.stream().map(Adapter::getName).collect(Collectors.joining(", ")) +
                    ". Only the first will be used as active.");
        }
    }

    @Override
    public List<Adapter> getAdapters() throws WebhookMetadataException {

        if (!isInitialized) {
            throw WebhookMetadataExceptionHandler.handleServerException(ERROR_CODE_ADAPTERS_RETRIEVE_ERROR);
        }
        return Collections.unmodifiableList(new ArrayList<>(adapterCache.values()));
    }
}
