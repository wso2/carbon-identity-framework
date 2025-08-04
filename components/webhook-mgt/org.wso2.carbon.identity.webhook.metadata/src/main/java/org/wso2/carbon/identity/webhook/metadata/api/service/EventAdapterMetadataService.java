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

package org.wso2.carbon.identity.webhook.metadata.api.service;

import org.wso2.carbon.identity.webhook.metadata.api.exception.WebhookMetadataException;
import org.wso2.carbon.identity.webhook.metadata.api.model.Adapter;

import java.util.List;

/**
 * Service interface for managing event adapter metadata.
 * This service provides methods to retrieve information about available adapters,
 * the currently active adapter, and specific adapters by name.
 */
public interface EventAdapterMetadataService {

    /**
     * Get all available adapters.
     *
     * @return List of adapters
     * @throws WebhookMetadataException If an error occurs while retrieving adapters
     */
    List<Adapter> getAdapters() throws WebhookMetadataException;

    /**
     * Get the properties of the enabled adapter.
     *
     * @return Adapter object containing properties
     * @throws WebhookMetadataException If an error occurs while retrieving the adapter
     */
    Adapter getCurrentActiveAdapter() throws WebhookMetadataException;

    /**
     * Get an adapter by its name.
     *
     * @param name Name of the adapter
     * @return Adapter object if found
     * @throws WebhookMetadataException If an error occurs while retrieving the adapter or if not found
     */
    Adapter getAdapterByName(String name) throws WebhookMetadataException;
}
