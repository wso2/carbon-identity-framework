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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.webhook.metadata.api.exception.WebhookMetadataException;
import org.wso2.carbon.identity.webhook.metadata.api.exception.WebhookMetadataServerException;
import org.wso2.carbon.identity.webhook.metadata.api.model.EventProfile;
import org.wso2.carbon.identity.webhook.metadata.internal.dao.EventProfileMetadataDAO;
import org.wso2.carbon.identity.webhook.metadata.internal.util.WebhookMetadataExceptionHandler;
import org.wso2.carbon.identity.webhook.metadata.internal.util.WebhookMetadataUtil;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.wso2.carbon.identity.webhook.metadata.internal.constant.ErrorMessage.ERROR_CODE_PROFILES_RETRIEVE_ERROR;
import static org.wso2.carbon.identity.webhook.metadata.internal.constant.ErrorMessage.ERROR_CODE_PROFILE_FILES_LOAD_ERROR;
import static org.wso2.carbon.identity.webhook.metadata.internal.constant.ErrorMessage.ERROR_CODE_PROFILE_RETRIEVE_ERROR;

/**
 * File-based implementation of the EventProfileMetadataDAO.
 * Loads event profiles from JSON files in the configured directory.
 */
public class FileBasedEventProfileMetadataDAOImpl implements EventProfileMetadataDAO {

    private static final Log log = LogFactory.getLog(FileBasedEventProfileMetadataDAOImpl.class);
    private static final FileBasedEventProfileMetadataDAOImpl INSTANCE = new FileBasedEventProfileMetadataDAOImpl();

    // Cache of loaded event profiles
    private final Map<String, EventProfile> profileCache = new HashMap<>();
    private boolean isInitialized = false;

    private FileBasedEventProfileMetadataDAOImpl() {
        // Private constructor to prevent instantiation
    }

    /**
     * Get the singleton instance of FileBasedEventProfileMetadataDAOImpl.
     *
     * @return Singleton instance
     */
    public static FileBasedEventProfileMetadataDAOImpl getInstance() {

        return INSTANCE;
    }

    /**
     * Initialize the DAO by loading all event profiles from the file system.
     * This is called during service activation.
     */
    public synchronized void init() throws WebhookMetadataServerException {

        if (isInitialized) {
            return;
        }

        try {
            loadEventProfiles();
            isInitialized = true;
        } catch (WebhookMetadataException e) {
            throw WebhookMetadataExceptionHandler.handleServerException(
                    ERROR_CODE_PROFILE_FILES_LOAD_ERROR, e);
        }
    }

    /**
     * Load all event profiles from the configured directory.
     */
    private void loadEventProfiles() throws WebhookMetadataException {

        try {
            Path eventProfilesPath = WebhookMetadataUtil.getEventProfilesDirectory();

            // Clear existing cache
            profileCache.clear();

            // Load all JSON files in the directory
            try (Stream<Path> paths = Files.walk(eventProfilesPath)) {
                List<Path> jsonFiles = paths
                        .filter(Files::isRegularFile)
                        .filter(path -> path.toString().endsWith(".json"))
                        .collect(Collectors.toList());

                for (Path jsonFile : jsonFiles) {
                    String json = FileUtils.readFileToString(jsonFile.toFile(), StandardCharsets.UTF_8);
                    Gson gson = new GsonBuilder().create();
                    EventProfile profile = gson.fromJson(json, EventProfile.class);

                    if (profile == null) {
                        log.warn("Failed to parse event profile from file: " + jsonFile);
                        continue;
                    }

                    // Prioritize using profile name from JSON content
                    // Only use filename as a fallback if profile name is not specified in the JSON
                    if (profile.getProfile() == null || profile.getProfile().isEmpty()) {
                        String fileName = FilenameUtils.getBaseName(jsonFile.getFileName().toString());
                        profile = new EventProfile(fileName, profile.getUri(), profile.getChannels());
                        log.debug("Profile name not found in JSON, using filename: " + fileName);
                    }

                    profileCache.put(profile.getProfile(), profile);
                    log.debug("Loaded event profile: " + profile.getProfile());
                }
            }
        } catch (IOException e) {
            throw WebhookMetadataExceptionHandler.handleServerException(
                    ERROR_CODE_PROFILE_FILES_LOAD_ERROR, e);
        }
    }

    @Override
    public List<EventProfile> getSupportedEventProfiles() throws WebhookMetadataException {

        if (!isInitialized) {
            throw WebhookMetadataExceptionHandler.handleClientException(
                    ERROR_CODE_PROFILES_RETRIEVE_ERROR);
        }
        return new ArrayList<>(profileCache.values());
    }

    @Override
    public synchronized EventProfile getEventProfile(String profileName) throws WebhookMetadataException {

        if (!isInitialized) {
            throw WebhookMetadataExceptionHandler.handleClientException(
                    ERROR_CODE_PROFILE_RETRIEVE_ERROR, profileName);
        }

        EventProfile profile = profileCache.get(profileName);
        if (profile == null) {
            log.debug("Event profile not found for name: " + profileName);
        }
        return profile;
    }
}
