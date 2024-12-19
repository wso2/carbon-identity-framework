/*
 * Copyright (c) 2024, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.carbon.identity.claim.metadata.mgt.internal;

import org.wso2.carbon.identity.claim.metadata.mgt.exception.ClaimMetadataException;
import org.wso2.carbon.identity.claim.metadata.mgt.model.ClaimDialect;
import org.wso2.carbon.identity.claim.metadata.mgt.model.ExternalClaim;
import org.wso2.carbon.identity.claim.metadata.mgt.model.LocalClaim;

import java.util.List;

/**
 * Claim metadata writer.
 */
public interface ReadWriteClaimMetadataManager extends ReadOnlyClaimMetadataManager {

    /**
     * Add a claim dialect.
     *
     * @param claimDialect Claim dialect.
     * @param tenantId     Tenant ID.
     * @throws ClaimMetadataException if an error occurs during the operation.
     */
    void addClaimDialect(ClaimDialect claimDialect, int tenantId) throws ClaimMetadataException;

    /**
     * Rename a claim dialect.
     *
     * @param oldClaimDialect Old claim dialect.
     * @param newClaimDialect New claim dialect.
     * @param tenantId        Tenant ID.
     * @throws ClaimMetadataException if an error occurs during the operation.
     */
    void renameClaimDialect(ClaimDialect oldClaimDialect, ClaimDialect newClaimDialect, int tenantId)
            throws ClaimMetadataException;

    /**
     * Remove a claim dialect.
     * @param claimDialect  Claim dialect.
     * @param tenantId      Tenant ID.
     * @throws ClaimMetadataException if an error occurs during the operation.
     */
    void removeClaimDialect(ClaimDialect claimDialect, int tenantId) throws ClaimMetadataException;

    /**
     * Add a local claim.
     *
     * @param localClaim Local claim.
     * @param tenantId   Tenant ID.
     * @throws ClaimMetadataException if an error occurs during the operation.
     */
    void addLocalClaim(LocalClaim localClaim, int tenantId) throws ClaimMetadataException;

    /**
     * Update a local claim.
     *
     * @param localClaim Local claim.
     * @param tenantId   Tenant ID.
     * @throws ClaimMetadataException if an error occurs during the operation.
     */
    void updateLocalClaim(LocalClaim localClaim, int tenantId) throws ClaimMetadataException;

    /**
     * Update mapped user store attributes of a user store domain in bulk.
     *
     * @param localClaimList  List of local claims.
     * @param tenantId        Tenant ID.
     * @param userStoreDomain User Store Domain name.
     * @throws ClaimMetadataException if an error occurs during the operation.
     */
    void updateLocalClaimMappings(List<LocalClaim> localClaimList, int tenantId, String userStoreDomain)
            throws ClaimMetadataException;

    /**
     * Remove a local claim.
     * @param localClaimURI Local claim URI.
     * @param tenantId      Tenant ID.
     * @throws ClaimMetadataException if an error occurs during the operation.
     */
    void removeLocalClaim(String localClaimURI, int tenantId) throws ClaimMetadataException;

    /**
     * Remove mapped user store attributes of a user store domain.
     *
     * @param tenantId        Tenant ID.
     * @param userstoreDomain User Store Domain name.
     * @throws ClaimMetadataException if an error occurs during the operation.
     */
    void removeClaimMappingAttributes(int tenantId, String userstoreDomain) throws ClaimMetadataException;

    /**
     * Add an external claim.
     * @param externalClaim     External claim.
     * @param tenantId          Tenant ID.
     * @throws ClaimMetadataException if an error occurs during the operation.
     */
    void addExternalClaim(ExternalClaim externalClaim, int tenantId) throws ClaimMetadataException;

    /**
     * Update an external claim.
     * @param externalClaim     External claim.
     * @param tenantId          Tenant ID.
     * @throws ClaimMetadataException if an error occurs during the operation.
     */
    void updateExternalClaim(ExternalClaim externalClaim, int tenantId) throws ClaimMetadataException;

    /**
     * Remove an external claim.
     * @param externalClaimDialectURI   External claim dialect URI.
     * @param externalClaimURI          External claim URI.
     * @param tenantId                  Tenant ID.
     * @throws ClaimMetadataException if an error occurs during the operation.
     */
    void removeExternalClaim(String externalClaimDialectURI, String externalClaimURI, int tenantId)
            throws ClaimMetadataException;

    /**
     * Remove all claim dialects.
     * @param tenantId  Tenant ID.
     * @throws ClaimMetadataException if an error occurs during the operation.
     */
    void removeAllClaimDialects(int tenantId) throws ClaimMetadataException;
}
