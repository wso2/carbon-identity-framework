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

package org.wso2.carbon.identity.webhook.metadata.internal.service.impl;

import org.wso2.carbon.identity.webhook.metadata.api.exception.WebhookMetadataException;
import org.wso2.carbon.identity.webhook.metadata.api.model.EventProfile;
import org.wso2.carbon.identity.webhook.metadata.api.model.ProfileType;
import org.wso2.carbon.identity.webhook.metadata.api.service.WebhookMetadataService;
import org.wso2.carbon.identity.webhook.metadata.internal.dao.WebhookMetadataDAO;

/**
 * Implementation of the WebhookMetadataService interface.
 * This class provides access to webhook event metadata.
 */
public class WebhookMetadataServiceImpl implements WebhookMetadataService {

    private final WebhookMetadataDAO webhookMetadataDAO;

    /**
     * Constructor with WebhookMetadataDAO dependency.
     *
     * @param webhookMetadataDAO The DAO to access webhook metadata
     */
    public WebhookMetadataServiceImpl(WebhookMetadataDAO webhookMetadataDAO) {

        this.webhookMetadataDAO = webhookMetadataDAO;
    }

    /**
     * Get the event profile metadata for a specific profile type.
     *
     * @param profileType Type of event profile
     * @return EventProfile containing channel and event metadata
     * @throws WebhookMetadataException If an error occurs while retrieving the event profile
     */
    @Override
    public EventProfile getEventProfile(ProfileType profileType) throws WebhookMetadataException {

        return webhookMetadataDAO.getEventProfile(profileType);
    }

    /**
     * Get all event profiles.
     *
     * @return Array of all available event profiles
     * @throws WebhookMetadataException If an error occurs while retrieving the event profiles
     */
    @Override
    public EventProfile[] getAllEventProfiles() throws WebhookMetadataException {
        // Get profiles for all types
        EventProfile[] profiles = new EventProfile[ProfileType.values().length];
        ProfileType[] types = ProfileType.values();

        for (int i = 0; i < types.length; i++) {
            profiles[i] = getEventProfile(types[i]);
        }

        return profiles;
    }
}
