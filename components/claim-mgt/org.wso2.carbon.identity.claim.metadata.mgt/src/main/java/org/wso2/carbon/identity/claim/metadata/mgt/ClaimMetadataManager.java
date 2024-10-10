package org.wso2.carbon.identity.claim.metadata.mgt;

import org.wso2.carbon.identity.claim.metadata.mgt.exception.ClaimMetadataException;
import org.wso2.carbon.identity.claim.metadata.mgt.model.Claim;
import org.wso2.carbon.identity.claim.metadata.mgt.model.ClaimDialect;
import org.wso2.carbon.identity.claim.metadata.mgt.model.ExternalClaim;
import org.wso2.carbon.identity.claim.metadata.mgt.model.LocalClaim;

import java.util.List;

/**
 * This interface defines the operations that can be performed on the claim metadata.
 */
public interface ClaimMetadataManager {

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
    ClaimDialect getClaimDialect(String claimDialectURI, int tenantId) throws ClaimMetadataException;

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
    LocalClaim getLocalClaim(String localClaimURI ,int tenantId) throws ClaimMetadataException;

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
    ExternalClaim getExternalClaim(String externalClaimDialectURI, String claimURI, int tenantId)
            throws ClaimMetadataException;

    /**
     * Add an external claim.
     * @param externalClaim     External claim.
     * @param tenantId          Tenant ID.
     * @throws ClaimMetadataException if an error occurs during the operation.
     */
    void addExternalClaim(ExternalClaim externalClaim, int tenantId)
            throws ClaimMetadataException;

    /**
     * Update an external claim.
     * @param externalClaim     External claim.
     * @param tenantId          Tenant ID.
     * @throws ClaimMetadataException if an error occurs during the operation.
     */
    void updateExternalClaim(ExternalClaim externalClaim, int tenantId)
            throws ClaimMetadataException;

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
     * Get all mapped external claims of a local claim.
     * @param localClaimURI Local claim URI.
     * @param tenantId      Tenant ID.
     * @return List of mapped external claims.
     * @throws ClaimMetadataException if an error occurs during the operation.
     */
    List<Claim> getMappedExternalClaims(String localClaimURI, int tenantId) throws ClaimMetadataException;

    /**
     * Remove all claim dialects.
     * @param tenantId  Tenant ID.
     * @throws ClaimMetadataException if an error occurs during the operation.
     */
    void removeAllClaimDialects(int tenantId) throws ClaimMetadataException;

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
