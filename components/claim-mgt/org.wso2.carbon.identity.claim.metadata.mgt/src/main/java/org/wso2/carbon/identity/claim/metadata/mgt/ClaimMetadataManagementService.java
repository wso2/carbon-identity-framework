/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.identity.claim.metadata.mgt;

import org.wso2.carbon.identity.claim.metadata.mgt.exception.ClaimMetadataException;
import org.wso2.carbon.identity.claim.metadata.mgt.model.ClaimDialect;
import org.wso2.carbon.identity.claim.metadata.mgt.model.ExternalClaim;
import org.wso2.carbon.identity.claim.metadata.mgt.model.LocalClaim;

import java.util.List;

/**
 * This interface used to expose claim metadata management functionalities as an OSGi Service.
 */
public interface ClaimMetadataManagementService {

    /**
     * Get claim dialects list for specified tenant
     *
     * @param tenantDomain
     * @return
     */
    List<ClaimDialect> getClaimDialects(String tenantDomain) throws ClaimMetadataException;

    /**
     * Add new claim dialect for specified tenant
     *
     * @param claimDialect
     * @param tenantDomain
     * @throws ClaimMetadataException
     */
    void addClaimDialect(ClaimDialect claimDialect, String tenantDomain) throws ClaimMetadataException;

    /**
     * Rename claim dialect uri while keeping associated claims unchanged
     *
     * @param oldClaimDialect
     * @param newClaimDialect
     * @param tenantDomain
     * @throws ClaimMetadataException
     */
    void renameClaimDialect(ClaimDialect oldClaimDialect, ClaimDialect newClaimDialect, String tenantDomain) throws
            ClaimMetadataException;

    /**
     * Remove specified claim dialect from specified tenant along with the associated claims
     *
     * @param claimDialect
     * @param tenantDomain
     * @throws ClaimMetadataException
     */
    void removeClaimDialect(ClaimDialect claimDialect, String tenantDomain) throws ClaimMetadataException;

    /**
     * Get local claim list for specified tenant
     *
     * @param tenantDomain
     * @return
     * @throws ClaimMetadataException
     */
    List<LocalClaim> getLocalClaims(String tenantDomain) throws ClaimMetadataException;

    /**
     * Add new local claim with attribute mappings and claim properties for specified tenant
     *
     * @param localClaim
     * @param tenantDomain
     * @throws ClaimMetadataException
     */
    void addLocalClaim(LocalClaim localClaim, String tenantDomain) throws ClaimMetadataException;

    /**
     * Update attribute mappings and claim properties for specified local claim
     *
     * @param localClaim
     * @param tenantDomain
     * @throws ClaimMetadataException
     */
    void updateLocalClaim(LocalClaim localClaim, String tenantDomain) throws ClaimMetadataException;

    /**
     * Remove specified local claim for specified tenant along with the associated attribute mapping and claim
     * properties
     *
     * @param localClaimURI
     * @param tenantDomain
     * @throws ClaimMetadataException
     */
    void removeLocalClaim(String localClaimURI, String tenantDomain) throws ClaimMetadataException;

    /**
     * Get external claim list for specified claim dialect
     *
     * @param externalClaimDialectURI
     * @param tenantDomain
     * @return
     * @throws ClaimMetadataException
     */
    List<ExternalClaim> getExternalClaims(String externalClaimDialectURI, String tenantDomain) throws
            ClaimMetadataException;

    /**
     * Add external claim to specified claim dialect with a mapping to local claim
     *
     * @param externalClaim
     * @param tenantDomain
     * @throws ClaimMetadataException
     */
    void addExternalClaim(ExternalClaim externalClaim, String tenantDomain) throws ClaimMetadataException;

    /**
     * Update mapping to local claim for the specified external claim in the specified claim dialect
     *
     * @param externalClaim
     * @param tenantDomain
     * @throws ClaimMetadataException
     */
    void updateExternalClaim(ExternalClaim externalClaim, String tenantDomain) throws ClaimMetadataException;

    /**
     * Remove specified external claim in specified claim dialect for specified tenant
     *
     * @param externalClaimURI
     * @param tenantDomain
     * @throws ClaimMetadataException
     */
    void removeExternalClaim(String externalClaimDialectURI, String externalClaimURI, String tenantDomain) throws
            ClaimMetadataException;

    /**
     * Remove attribute claim mappings related to a tenant domain.
     *
     * @param tenantId        Tenant Id
     * @param userstoreDomain Tenant domain
     * @throws ClaimMetadataException If an error occurred while removing local claim mappings
     */
    default void removeClaimMappingAttributes(int tenantId, String userstoreDomain) throws ClaimMetadataException {

    }
}
