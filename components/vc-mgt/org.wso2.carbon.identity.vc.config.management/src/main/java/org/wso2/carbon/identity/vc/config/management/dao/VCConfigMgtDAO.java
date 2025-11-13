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

package org.wso2.carbon.identity.vc.config.management.dao;

import org.wso2.carbon.identity.vc.config.management.exception.VCConfigMgtException;
import org.wso2.carbon.identity.vc.config.management.model.VCCredentialConfiguration;

import java.util.List;

/**
 * DAO for VC Config persistence.
 */
public interface VCConfigMgtDAO {

    /**
     * List all VC credential configurations for a tenant.
     *
     * @param tenantId Tenant ID.
     * @return List of configurations.
     * @throws VCConfigMgtException on retrieval errors.
     */
    List<VCCredentialConfiguration> list(int tenantId) throws VCConfigMgtException;

    /**
     * Get a configuration by ID.
     *
     * @param id Unique configuration id.
     * @param tenantId Tenant ID.
     * @return Configuration or null if not found.
     * @throws VCConfigMgtException on retrieval errors.
     */
    VCCredentialConfiguration get(String id, int tenantId) throws VCConfigMgtException;

    /**
     * Get a configuration by configuration id.
     *
     * @param configId Configuration id.
     * @param tenantId Tenant ID.
     * @return Configuration or null if not found.
     * @throws VCConfigMgtException on retrieval errors.
     */
    VCCredentialConfiguration getByConfigId(String configId, int tenantId) throws VCConfigMgtException;

    /**
     * Check existence by identifier.
     *
     * @param identifier Identifier.
     * @param tenantId   Tenant ID.
     * @return true if exists, false otherwise.
     * @throws VCConfigMgtException on retrieval errors.
     */
    boolean existsByIdentifier(String identifier, int tenantId) throws VCConfigMgtException;

    /**
     * Check existence by configuration ID.
     *
     * @param configurationId Configuration ID.
     * @param tenantId        Tenant ID.
     * @return true if exists, false otherwise.
     * @throws VCConfigMgtException on retrieval errors.
     */
    boolean existsByConfigurationId(String configurationId, int tenantId) throws VCConfigMgtException;

    /**
     * Add a new configuration.
     *
     * @param configuration Configuration payload.
     * @param tenantId      Tenant ID.
     * @return Added configuration.
     * @throws VCConfigMgtException on creation errors.
     */
    VCCredentialConfiguration add(VCCredentialConfiguration configuration, int tenantId)
            throws VCConfigMgtException;

    /**
     * Update an existing configuration by id.
     * @param id     Configuration id to update.
     * @param configuration Updated payload.
     * @param tenantId     Tenant ID.
     * @return Updated configuration.
     * @throws VCConfigMgtException on update errors.
     */
    VCCredentialConfiguration update(String id, VCCredentialConfiguration configuration, int tenantId)
            throws VCConfigMgtException;

    /**
     * Delete a configuration by id.
     * @param id     Configuration id.
     * @param tenantId     Tenant ID.
     * @throws VCConfigMgtException on deletion errors.
     */
    void delete(String id, int tenantId) throws VCConfigMgtException;
}
