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

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.webhook.metadata.api.exception.WebhookMetadataException;
import org.wso2.carbon.identity.webhook.metadata.api.model.EventProfile;
import org.wso2.carbon.identity.webhook.metadata.api.model.ProfileType;
import org.wso2.carbon.identity.webhook.metadata.internal.dao.WebhookMetadataDAO;
import org.wso2.carbon.identity.webhook.metadata.internal.util.WebhookMetadataExceptionBuilder;
import org.wso2.carbon.utils.CarbonUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;

/**
 * Implementation of the WebhookMetadataDAO interface that reads webhook metadata from files.
 * Uses a singleton pattern and caches event profiles to avoid repeated file reads.
 */
public class FileBasedWebhookMetadataDAOImpl implements WebhookMetadataDAO {

    private static final Log LOG = LogFactory.getLog(FileBasedWebhookMetadataDAOImpl.class);
    private static final String METADATA_DIR = "repository/resources/identity/channel-profiles";
    private static final FileBasedWebhookMetadataDAOImpl INSTANCE = new FileBasedWebhookMetadataDAOImpl();

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Map<ProfileType, EventProfile> profileCache = new EnumMap<>(ProfileType.class);

    /**
     * Private constructor to enforce singleton pattern.
     */
    private FileBasedWebhookMetadataDAOImpl() {
        // Private constructor to enforce singleton pattern
    }

    /**
     * Get the singleton instance of FileBasedWebhookMetadataDAO.
     *
     * @return The singleton instance
     */
    public static FileBasedWebhookMetadataDAOImpl getInstance() {

        return INSTANCE;
    }

    /**
     * Get the event profile metadata for a specific profile type.
     * First checks the cache, if not found, reads from file and caches the result.
     *
     * @param profileType Type of event profile
     * @return EventProfile containing channel and event metadata
     * @throws WebhookMetadataException If an error occurs while retrieving the event profile
     */
    @Override
    public EventProfile getEventProfile(ProfileType profileType) throws WebhookMetadataException {
        // Check if the profile is already in the cache
        if (profileCache.containsKey(profileType)) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("EventProfile for " + profileType.getProfileName() + " found in cache");
            }
            return profileCache.get(profileType);
        }

        // Not in cache, load from file
        String fileName = profileType.getFileName();
        String filePath = Paths.get(CarbonUtils.getCarbonConfigDirPath(), METADATA_DIR, fileName).toString();

        try {
            File file = new File(filePath);
            if (!file.exists()) {
                // Try to load from resources
                file = new File(Objects.requireNonNull(getClass().getClassLoader().getResource(fileName)).getFile());
                if (!file.exists()) {
                    throw WebhookMetadataExceptionBuilder.buildServerException(
                            "Event profile file not found: " + filePath);
                }
            }

            EventProfile profile = objectMapper.readValue(file, EventProfile.class);

            // Cache the profile
            profileCache.put(profileType, profile);

            if (LOG.isDebugEnabled()) {
                LOG.debug("EventProfile for " + profileType.getProfileName() + " loaded from file and cached");
            }

            return profile;
        } catch (IOException e) {
            throw WebhookMetadataExceptionBuilder.buildServerException(
                    "Error reading event profile from file: " + filePath, e);
        }
    }

    /**
     * Clears the event profile cache.
     * This can be useful when profiles are updated and need to be reloaded.
     */
    public void clearCache() {

        profileCache.clear();
        LOG.debug("Event profile cache cleared");
    }
}
