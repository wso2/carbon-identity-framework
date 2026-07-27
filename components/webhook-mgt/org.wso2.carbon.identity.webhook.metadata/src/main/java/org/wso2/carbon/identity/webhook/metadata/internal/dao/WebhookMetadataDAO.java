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
import org.wso2.carbon.identity.webhook.metadata.internal.model.WebhookMetadataProperty;

import java.util.Map;

/**
 * Data Access Object interface for webhook management.
 */
public interface WebhookMetadataDAO {

    /**
     * Get webhook metadata properties from the database.
     *
     * @param tenantId Tenant ID.
     * @return A map of webhook metadata properties.
     * @throws WebhookMetadataException If an error occurs while retrieving the webhook metadata properties.
     */
    Map<String, WebhookMetadataProperty> getWebhookMetadataProperties(int tenantId) throws WebhookMetadataException;

    /**
     * Add webhook metadata properties to the database.
     *
     * @param webhookMetadataProperties A map of webhook metadata properties to add.
     * @param tenantId                  Tenant ID.
     * @throws WebhookMetadataException If an error occurs while adding the webhook metadata properties.
     */
    void addWebhookMetadataProperties(Map<String, WebhookMetadataProperty> webhookMetadataProperties,
                                      int tenantId) throws WebhookMetadataException;

    /**
     * Update webhook metadata properties in the database.
     *
     * @param webhookMetadataProperties A map of webhook metadata properties to update.
     * @param tenantId                  Tenant ID.
     * @throws WebhookMetadataException If an error occurs while updating the webhook metadata properties.
     */
    void updateWebhookMetadataProperties(Map<String, WebhookMetadataProperty> webhookMetadataProperties,
                                         int tenantId) throws WebhookMetadataException;
}
