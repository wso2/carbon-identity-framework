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
import org.wso2.carbon.identity.vc.config.management.model.VCOffer;

import java.util.List;

/**
 * DAO for VC Offer persistence.
 */
public interface VCOfferDAO {

    /**
     * List all VC offer configurations for a tenant.
     *
     * @param tenantId Tenant ID.
     * @return List of offer configurations.
     * @throws VCConfigMgtException on retrieval errors.
     */
    List<VCOffer> list(int tenantId) throws VCConfigMgtException;

    /**
     * Get an offer configuration by offer ID.
     *
     * @param offerId  Unique offer ID.
     * @param tenantId Tenant ID.
     * @return Offer configuration or null if not found.
     * @throws VCConfigMgtException on retrieval errors.
     */
    VCOffer get(String offerId, int tenantId) throws VCConfigMgtException;

    /**
     * Check existence by offer ID.
     *
     * @param offerId  Offer ID.
     * @param tenantId Tenant ID.
     * @return true if exists, false otherwise.
     * @throws VCConfigMgtException on retrieval errors.
     */
    boolean existsByOfferId(String offerId, int tenantId) throws VCConfigMgtException;

    /**
     * Add a new offer configuration.
     *
     * @param offer    Offer configuration payload.
     * @param tenantId Tenant ID.
     * @return Added offer configuration.
     * @throws VCConfigMgtException on creation errors.
     */
    VCOffer add(VCOffer offer, int tenantId) throws VCConfigMgtException;

    /**
     * Update an existing offer configuration by offer ID.
     *
     * @param offerId  Offer ID to update.
     * @param offer    Updated offer payload.
     * @param tenantId Tenant ID.
     * @return Updated offer configuration.
     * @throws VCConfigMgtException on update errors.
     */
    VCOffer update(String offerId, VCOffer offer, int tenantId) throws VCConfigMgtException;

    /**
     * Delete an offer configuration by offer ID.
     *
     * @param offerId  Offer ID.
     * @param tenantId Tenant ID.
     * @throws VCConfigMgtException on deletion errors.
     */
    void delete(String offerId, int tenantId) throws VCConfigMgtException;
}

