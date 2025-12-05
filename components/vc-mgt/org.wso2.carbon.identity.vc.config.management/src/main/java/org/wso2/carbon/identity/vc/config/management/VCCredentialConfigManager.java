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

package org.wso2.carbon.identity.vc.config.management;

import org.wso2.carbon.identity.vc.config.management.exception.VCConfigMgtException;
import org.wso2.carbon.identity.vc.config.management.model.VCCredentialConfiguration;

import java.util.List;

/**
 * Manager interface for Verifiable Credential configurations.
 */
public interface VCCredentialConfigManager {

    /**
     * List all VC credential configurations for a tenant.
     *
     * @param tenantDomain Tenant domain.
     * @return List of configurations.
     * @throws VCConfigMgtException on retrieval errors.
     */
    List<VCCredentialConfiguration> list(String tenantDomain) throws VCConfigMgtException;

    /**
     * Get a configuration by ID.
     *
     * @param id     Unique configuration id.
     * @param tenantDomain Tenant domain.
     * @return Configuration or null if not found.
     * @throws VCConfigMgtException on retrieval errors.
     */
    VCCredentialConfiguration get(String id, String tenantDomain) throws VCConfigMgtException;

    /**
     * Get a configuration by identifier.
     *
     * @param identifier Identifier of the configuration.
     * @param tenantDomain Tenant domain.
     * @return Configuration or null if not found.
     * @throws VCConfigMgtException on retrieval errors.
     */
    VCCredentialConfiguration getByIdentifier(String identifier, String tenantDomain) throws VCConfigMgtException;

    /**
     * Get a configuration by offer ID.
     *
     * @param offerId Offer ID of the configuration.
     * @param tenantDomain Tenant domain.
     * @return Configuration or null if not found.
     * @throws VCConfigMgtException on retrieval errors.
     */
    VCCredentialConfiguration getByOfferId(String offerId, String tenantDomain) throws VCConfigMgtException;

    /**
     * Add a new configuration.
     *
     * @param configuration Configuration payload.
     * @param tenantDomain  Tenant domain.
     * @return Added configuration.
     * @throws VCConfigMgtException on creation errors.
     */
    VCCredentialConfiguration add(VCCredentialConfiguration configuration, String tenantDomain)
            throws VCConfigMgtException;

    /**
     * Update an existing configuration by id.
     *
     * @param id      Configuration id to update.
     * @param configuration Updated payload.
     * @param tenantDomain  Tenant domain.
     * @return Updated configuration.
     * @throws VCConfigMgtException on update errors.
     */
    VCCredentialConfiguration update(String id, VCCredentialConfiguration configuration, String tenantDomain)
            throws VCConfigMgtException;

    /**
     * Delete a configuration by id.
     *
     * @param id     Configuration id.
     * @param tenantDomain Tenant domain.
     * @throws VCConfigMgtException on deletion errors.
     */
    void delete(String id, String tenantDomain) throws VCConfigMgtException;

    /**
     * Generate or regenerate a credential offer for a configuration.
     * Creates a new random UUID for offerId.
     * If an offer already exists, it will be regenerated with a new UUID.
     *
     * @param configId The configuration ID.
     * @param tenantDomain Tenant domain.
     * @return Updated configuration with offerId.
     * @throws VCConfigMgtException if config not found.
     */
    VCCredentialConfiguration generateOffer(String configId, String tenantDomain) throws VCConfigMgtException;

    /**
     * Revoke/delete the credential offer for a configuration.
     * Sets offerId to null.
     * Returns 404 if no offer exists.
     *
     * @param configId The configuration ID.
     * @param tenantDomain Tenant domain.
     * @return Updated configuration with offerId = null.
     * @throws VCConfigMgtException if config or offer not found.
     */
    VCCredentialConfiguration revokeOffer(String configId, String tenantDomain) throws VCConfigMgtException;
}

