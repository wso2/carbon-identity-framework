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

package org.wso2.carbon.identity.webhook.metadata.internal.dao;

import org.wso2.carbon.identity.webhook.metadata.api.exception.WebhookMetadataException;
import org.wso2.carbon.identity.webhook.metadata.api.model.EventProfile;

import java.util.List;

/**
 * DAO interface for event profile metadata operations.
 */
public interface EventProfileMetadataDAO {

    /**
     * Get all supported event profiles.
     *
     * @return List of event profiles
     * @throws WebhookMetadataException If an error occurs while retrieving event profiles
     */
    List<EventProfile> getSupportedEventProfiles() throws WebhookMetadataException;

    /**
     * Get details of a specific event profile including its channels.
     *
     * @param profileName Name of the event profile
     * @return EventProfile object containing profile details and channels
     * @throws WebhookMetadataException If an error occurs while retrieving event profile details
     */
    EventProfile getEventProfile(String profileName) throws WebhookMetadataException;
}
