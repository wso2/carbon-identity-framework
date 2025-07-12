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
import org.wso2.carbon.identity.webhook.metadata.api.model.Adaptor;
import org.wso2.carbon.identity.webhook.metadata.api.model.WebhookAdaptorType;
import org.wso2.carbon.identity.webhook.metadata.internal.dao.EventAdopterMetadataDAO;
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

import static org.wso2.carbon.identity.webhook.metadata.internal.constant.ErrorMessage.ERROR_CODE_ADAPTORS_RETRIEVE_ERROR;
import static org.wso2.carbon.identity.webhook.metadata.internal.constant.ErrorMessage.ERROR_CODE_CONFIG_FILE_READ_ERROR;
import static org.wso2.carbon.identity.webhook.metadata.internal.constant.WebhookMetadataConstants.ADAPTOR_PREFIX;
import static org.wso2.carbon.identity.webhook.metadata.internal.constant.WebhookMetadataConstants.CONFIG_FILE_NAME;
import static org.wso2.carbon.identity.webhook.metadata.internal.constant.WebhookMetadataConstants.ENABLED_KEY;
import static org.wso2.carbon.identity.webhook.metadata.internal.constant.WebhookMetadataConstants.ENABLED_VALUE_TRUE;
import static org.wso2.carbon.identity.webhook.metadata.internal.constant.WebhookMetadataConstants.TYPE_KEY;

/**
 * File-based implementation of the EventAdopterMetadataDAO.
 * This DAO reads event adaptor metadata from a configuration file.
 */
public class FileBasedEventAdopterMetadataDAOImpl implements EventAdopterMetadataDAO {

    private static final Log log = LogFactory.getLog(FileBasedEventAdopterMetadataDAOImpl.class);
    private static final FileBasedEventAdopterMetadataDAOImpl INSTANCE = new FileBasedEventAdopterMetadataDAOImpl();

    // Cache of loaded adaptors
    private final Map<String, Adaptor> adopterCache = new HashMap<>();
    private boolean isInitialized = false;

    private FileBasedEventAdopterMetadataDAOImpl() {
        // Private constructor for singleton
    }

    public static FileBasedEventAdopterMetadataDAOImpl getInstance() {

        return INSTANCE;
    }

    /**
     * Initialize the DAO by loading all adaptors from the config file.
     */
    public synchronized void init() throws WebhookMetadataServerException {

        if (isInitialized) {
            return;
        }
        try {
            loadAdopters();
            isInitialized = true;
        } catch (WebhookMetadataException e) {
            throw WebhookMetadataExceptionHandler.handleServerException(
                    ERROR_CODE_CONFIG_FILE_READ_ERROR, e);
        }
    }

    /**
     * Load all adaptors from the config file into the cache.
     */
    private void loadAdopters() throws WebhookMetadataException {

        Path configPath = Paths.get(IdentityUtil.getIdentityConfigDirPath(), CONFIG_FILE_NAME);
        Properties properties = new Properties();
        try (InputStream in = Files.newInputStream(configPath)) {
            properties.load(in);
        } catch (Exception e) {
            throw WebhookMetadataExceptionHandler.handleServerException(
                    ERROR_CODE_CONFIG_FILE_READ_ERROR, e, configPath.toString());
        }

        Map<String, Map<String, String>> grouped = new HashMap<>();
        for (String key : properties.stringPropertyNames()) {
            if (key.startsWith(ADAPTOR_PREFIX)) {
                String[] parts = key.split("\\.");
                if (parts.length >= 3) {
                    String name = parts[1];
                    grouped.computeIfAbsent(name, k -> new HashMap<>())
                            .put(parts[2], properties.getProperty(key));
                }
            }
        }

        adopterCache.clear();
        for (Map.Entry<String, Map<String, String>> entry : grouped.entrySet()) {
            String name = entry.getKey();
            Map<String, String> props = entry.getValue();
            String type = props.getOrDefault(TYPE_KEY, "");
            boolean enabled = ENABLED_VALUE_TRUE.equalsIgnoreCase(props.getOrDefault(ENABLED_KEY, "false"));
            Adaptor adopter = new Adaptor.Builder()
                    .name(name)
                    .type(WebhookAdaptorType.valueOf(type))
                    .enabled(enabled)
                    .properties(props)
                    .build();
            adopterCache.put(name, adopter);
        }

        List<Adaptor> enabledAdopters = adopterCache.values().stream()
                .filter(Adaptor::isEnabled)
                .collect(Collectors.toList());
        if (enabledAdopters.size() > 1) {
            log.warn("Multiple enabled adopters found: " +
                    enabledAdopters.stream().map(Adaptor::getName).collect(Collectors.joining(", ")) +
                    ". Only the first will be used as active.");
        }
    }

    @Override
    public List<Adaptor> getAdopters() throws WebhookMetadataException {

        if (!isInitialized) {
            throw WebhookMetadataExceptionHandler.handleServerException(ERROR_CODE_ADAPTORS_RETRIEVE_ERROR);
        }
        return Collections.unmodifiableList(new ArrayList<>(adopterCache.values()));
    }
}
