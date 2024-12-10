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
import org.wso2.carbon.identity.claim.metadata.mgt.model.Claim;
import org.wso2.carbon.identity.claim.metadata.mgt.model.ClaimDialect;
import org.wso2.carbon.identity.claim.metadata.mgt.model.ExternalClaim;
import org.wso2.carbon.identity.claim.metadata.mgt.model.LocalClaim;

import java.util.List;
import java.util.Optional;

/**
 * Claim metadata reader.
 */
public interface ReadOnlyClaimMetadataManager {

    /**
     * Get all claim dialects.
     *
     * @param tenantId Tenant ID.
     * @return List of claim dialects.
     * @throws ClaimMetadataException if an error occurs during the operation.
     */
    List<ClaimDialect> getClaimDialects(int tenantId) throws ClaimMetadataException;

    /**
     * Get a claim dialect by URI.
     *
     * @param claimDialectURI Claim dialect URI.
     * @param tenantId        Tenant ID.
     * @return Claim dialect.
     * @throws ClaimMetadataException if an error occurs during the operation.
     */
    Optional<ClaimDialect> getClaimDialect(String claimDialectURI, int tenantId) throws ClaimMetadataException;

    /**
     * Get all local claims.
     *
     * @param tenantId Tenant ID.
     * @return List of local claims.
     * @throws ClaimMetadataException if an error occurs during the operation.
     */
    List<LocalClaim> getLocalClaims(int tenantId) throws ClaimMetadataException;

    /**
     * Get a local claim by URI.
     *
     * @param localClaimURI Local claim URI.
     * @param tenantId      Tenant ID.
     * @return Local claim.
     * @throws ClaimMetadataException if an error occurs during the operation.
     */
    Optional<LocalClaim> getLocalClaim(String localClaimURI ,int tenantId) throws ClaimMetadataException;

    /**
     * Get all external claims.
     *
     * @param externalClaimDialectURI External claim dialect URI.
     * @param tenantId                Tenant ID.
     * @return List of external claims.
     * @throws ClaimMetadataException if an error occurs during the operation.
     */
    List<ExternalClaim> getExternalClaims(String externalClaimDialectURI, int tenantId)
            throws ClaimMetadataException;

    /**
     * Get an external claim by URI.
     * @param externalClaimDialectURI   External claim dialect URI.
     * @param claimURI                  Claim URI.
     * @param tenantId                  Tenant ID.
     * @return External claim.
     * @throws ClaimMetadataException if an error occurs during the operation.
     */
    Optional<ExternalClaim> getExternalClaim(String externalClaimDialectURI, String claimURI, int tenantId)
            throws ClaimMetadataException;

    /**
     * Get all mapped external claims of a local claim.
     * @param localClaimURI Local claim URI.
     * @param tenantId      Tenant ID.
     * @return List of mapped external claims.
     * @throws ClaimMetadataException if an error occurs during the operation.
     */
    List<Claim> getMappedExternalClaims(String localClaimURI, int tenantId) throws ClaimMetadataException;

    /**
     * Check whether any external claim maps to a given local claim.
     * @param localClaimURI Local claim URI.
     * @param tenantId      Tenant ID.
     * @return True if the local claim is mapped.
     * @throws ClaimMetadataException if an error occurs during the operation.
     */
    boolean isMappedLocalClaim(String localClaimURI, int tenantId) throws ClaimMetadataException;

    /**
     * Check whether a local claim is mapped within a given dialect.
     * @param mappedLocalClaim          Mapped local claim.
     * @param externalClaimDialectURI   External claim dialect URI.
     * @param tenantId                  Tenant ID.
     * @return True if the local claim is mapped within the dialect.
     * @throws ClaimMetadataException if an error occurs during the operation.
     */
    boolean isLocalClaimMappedWithinDialect(String mappedLocalClaim, String externalClaimDialectURI, int tenantId)
            throws ClaimMetadataException;
}
