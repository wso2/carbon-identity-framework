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

package org.wso2.carbon.identity.claim.metadata.mgt;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.claim.metadata.mgt.exception.ClaimMetadataClientException;
import org.wso2.carbon.identity.claim.metadata.mgt.exception.ClaimMetadataException;
import org.wso2.carbon.identity.claim.metadata.mgt.internal.IdentityClaimManagementServiceDataHolder;
import org.wso2.carbon.identity.claim.metadata.mgt.internal.ReadOnlyClaimMetadataManager;
import org.wso2.carbon.identity.claim.metadata.mgt.internal.ReadWriteClaimMetadataManager;
import org.wso2.carbon.identity.claim.metadata.mgt.model.AttributeMapping;
import org.wso2.carbon.identity.claim.metadata.mgt.model.Claim;
import org.wso2.carbon.identity.claim.metadata.mgt.model.ClaimDialect;
import org.wso2.carbon.identity.claim.metadata.mgt.model.ExternalClaim;
import org.wso2.carbon.identity.claim.metadata.mgt.model.LocalClaim;
import org.wso2.carbon.identity.claim.metadata.mgt.util.ClaimConstants;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;
import org.wso2.carbon.identity.core.util.IdentityUtil;
import org.wso2.carbon.identity.core.util.LambdaExceptionUtils;
import org.wso2.carbon.identity.organization.management.service.exception.OrganizationManagementException;
import org.wso2.carbon.identity.organization.management.service.util.OrganizationManagementUtil;
import org.wso2.carbon.identity.organization.management.service.util.Utils;
import org.wso2.carbon.identity.organization.resource.hierarchy.traverse.service.OrgResourceResolverService;
import org.wso2.carbon.identity.organization.resource.hierarchy.traverse.service.exception.OrgResourceHierarchyTraverseException;
import org.wso2.carbon.identity.organization.resource.hierarchy.traverse.service.strategy.FirstFoundAggregationStrategy;
import org.wso2.carbon.identity.organization.resource.hierarchy.traverse.service.strategy.MergeAllAggregationStrategy;
import org.wso2.carbon.user.api.UserStoreException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static org.wso2.carbon.identity.claim.metadata.mgt.util.ClaimConstants.ErrorMessage.ERROR_CODE_FAILED_TO_RESOLVE_ORGANIZATION_ID;
import static org.wso2.carbon.identity.claim.metadata.mgt.util.ClaimConstants.ErrorMessage.ERROR_CODE_FAILED_TO_RESOLVE_TENANT_ID_DURING_HIERARCHICAL_AGGREGATION;
import static org.wso2.carbon.identity.claim.metadata.mgt.util.ClaimConstants.ErrorMessage.ERROR_CODE_FAILURE_IN_CHECKING_IS_TENANT_AN_ORGANIZATION;
import static org.wso2.carbon.identity.claim.metadata.mgt.util.ClaimConstants.ErrorMessage.ERROR_CODE_FAILURE_IN_TRAVERSING_HIERARCHY;
import static org.wso2.carbon.identity.claim.metadata.mgt.util.ClaimConstants.ErrorMessage.ERROR_CODE_NON_EXISTING_LOCAL_CLAIM_URI;
import static org.wso2.carbon.identity.claim.metadata.mgt.util.ClaimConstants.ErrorMessage.ERROR_CODE_NO_DELETE_SYSTEM_CLAIM;
import static org.wso2.carbon.identity.claim.metadata.mgt.util.ClaimConstants.ErrorMessage.ERROR_CODE_NO_DELETE_SYSTEM_DIALECT;
import static org.wso2.carbon.identity.claim.metadata.mgt.util.ClaimConstants.ErrorMessage.ERROR_CODE_NO_RENAME_SYSTEM_DIALECT;

/**
 * Unified claim metadata manager.
 *
 * This class provides a unified view of claim metadata from the system default claim metadata manager and the
 * database-based claim metadata manager.
 */
public class UnifiedClaimMetadataManager implements ReadWriteClaimMetadataManager {

    private final ReadOnlyClaimMetadataManager systemDefaultClaimMetadataManager =
            new SystemDefaultClaimMetadataManager();
    private final ReadWriteClaimMetadataManager dbBasedClaimMetadataManager = new DBBasedClaimMetadataManager();
    private final ReadWriteClaimMetadataManager cacheBackedDBBasedClaimMetadataManager =
            new CacheBackedDBBasedClaimMetadataManager();
    private static final Log LOG = LogFactory.getLog(UnifiedClaimMetadataManager.class);

    /**
     * Get all claim dialects.
     *
     * @param tenantId Tenant ID.
     * @return List of claim dialects.
     * @throws ClaimMetadataException If an error occurs while retrieving claim dialects.
     */
    public List<ClaimDialect> getClaimDialects(int tenantId) throws ClaimMetadataException {

        List<ClaimDialect> claimDialectsInDB;
        String tenantDomain = IdentityTenantUtil.getTenantDomain(tenantId);
        if (resolveWithHierarchicalMode(tenantDomain, tenantId)) {
            if (isOrganization(tenantId)) {
                try {
                    String organizationId = getOrganizationId(tenantDomain, tenantId);
                    claimDialectsInDB = IdentityClaimManagementServiceDataHolder.getInstance()
                            .getOrgResourceResolverService().getResourcesFromOrgHierarchy(organizationId,
                                    LambdaExceptionUtils.rethrowFunction(this::retrieveClaimDialectsFromHierarchy),
                                    new MergeAllAggregationStrategy<>(this::mergeClaimDialectsInHierarchy)
                            );
                } catch (OrgResourceHierarchyTraverseException e) {
                    throw new ClaimMetadataException(ERROR_CODE_FAILURE_IN_TRAVERSING_HIERARCHY.getCode(),
                            String.format(ERROR_CODE_FAILURE_IN_TRAVERSING_HIERARCHY.getMessage(), tenantId,
                                    tenantDomain), e);
                }
            } else {
                claimDialectsInDB = this.cacheBackedDBBasedClaimMetadataManager.getClaimDialects(tenantId);
            }
        } else {
            claimDialectsInDB = this.dbBasedClaimMetadataManager.getClaimDialects(tenantId);
        }
        List<ClaimDialect> claimDialectsInSystem =
                this.systemDefaultClaimMetadataManager.getClaimDialects(tenantId);
        Set<String> claimDialectURIsInDB = claimDialectsInDB.stream()
                .map(ClaimDialect::getClaimDialectURI)
                .collect(Collectors.toSet());

        List<ClaimDialect> allClaimDialects = new ArrayList<>(claimDialectsInDB);
        claimDialectsInSystem.stream()
                .filter(claimDialect ->
                        !claimDialectURIsInDB.contains(claimDialect.getClaimDialectURI()))
                .forEach(allClaimDialects::add);
        return allClaimDialects;
    }

    /**
     * Retrieves claim dialects for an organization in the hierarchy during sub-organization claim dialect aggregation.
     *
     * @param orgId The organization id of the tenant for which the claim dialects need to be retrieved.
     * @return The claim dialects of the given tenant.
     * @throws ClaimMetadataException If an error occurs when getting the claim dialects.
     */
    private Optional<List<ClaimDialect>> retrieveClaimDialectsFromHierarchy(String orgId)
            throws ClaimMetadataException {

        int tenantId = getTenantId(orgId);
        List<ClaimDialect> claimDialects = this.cacheBackedDBBasedClaimMetadataManager.getClaimDialects(tenantId);
        return Optional.ofNullable(claimDialects);
    }

    /**
     * Merges claim dialects in the hierarchy and removes duplicates found at higher levels as priority is given
     * to the lower levels.
     *
     * @param aggregatedClaimDialects The claim dialects aggregated from the child organizations so far.
     * @param tenantClaimDialects     The claim dialects of the current tenant being considered.
     * @return The merged list of claim dialects up to the specific tenant being considered.
     */
    private List<ClaimDialect> mergeClaimDialectsInHierarchy(
            List<ClaimDialect> aggregatedClaimDialects, List<ClaimDialect> tenantClaimDialects) {

        Map<String, ClaimDialect> existingClaimDialects = aggregatedClaimDialects.stream()
                .collect(Collectors.toMap(ClaimDialect::getClaimDialectURI, Function.identity()));
        for (ClaimDialect tenantClaimDialect : tenantClaimDialects) {
            if (!existingClaimDialects.containsKey(tenantClaimDialect.getClaimDialectURI())) {
                aggregatedClaimDialects.add(tenantClaimDialect);
            }
        }
        return aggregatedClaimDialects;
    }

    /**
     * Get a claim dialect by URI.
     *
     * @param claimDialectURI Claim dialect URI.
     * @param tenantId        Tenant ID.
     * @return Claim dialect.
     * @throws ClaimMetadataException If an error occurs while retrieving claim dialect.
     */
    public Optional<ClaimDialect> getClaimDialect(String claimDialectURI, int tenantId) throws ClaimMetadataException {

        String tenantDomain = IdentityTenantUtil.getTenantDomain(tenantId);
        Optional<ClaimDialect> claimDialectInDB = Optional.empty();
        if (resolveWithHierarchicalMode(tenantDomain, tenantId)) {
            if (isOrganization(tenantId)) {
                try {
                    String organizationId = getOrganizationId(tenantDomain, tenantId);
                    ClaimDialect claimDialectInDBFromOrgHierarchy = IdentityClaimManagementServiceDataHolder
                            .getInstance().getOrgResourceResolverService().getResourcesFromOrgHierarchy(organizationId,
                                    LambdaExceptionUtils.rethrowFunction(orgId ->
                                            retrieveClaimDialectInHierarchy(claimDialectURI, orgId)),
                                    new FirstFoundAggregationStrategy<>()
                            );
                    if (claimDialectInDBFromOrgHierarchy != null) {
                        claimDialectInDB = Optional.of(claimDialectInDBFromOrgHierarchy);
                    }
                } catch (OrgResourceHierarchyTraverseException e) {
                    throw new ClaimMetadataException(ERROR_CODE_FAILURE_IN_TRAVERSING_HIERARCHY.getCode(),
                            String.format(ERROR_CODE_FAILURE_IN_TRAVERSING_HIERARCHY.getMessage(), tenantId,
                                    tenantDomain), e);
                }
            } else {
                claimDialectInDB = this.cacheBackedDBBasedClaimMetadataManager.getClaimDialect(claimDialectURI,
                        tenantId);
            }
        } else {
            claimDialectInDB = this.dbBasedClaimMetadataManager.getClaimDialect(claimDialectURI, tenantId);
        }
        if (claimDialectInDB.isPresent()) {
            return claimDialectInDB;
        }
        return this.systemDefaultClaimMetadataManager.getClaimDialect(claimDialectURI, tenantId);
    }

    /**
     * Retrieves a claim dialect for an organization in the hierarchy during sub-organization claim dialect aggregation.
     *
     * @param claimDialectURI The URI of the claim dialect to be retrieved.
     * @param orgId           The organization id of the tenant for which the claim dialects need to be retrieved.
     * @return The claim dialect matching the given URI for the given tenant, if available.
     * @throws ClaimMetadataException If an error occurs when getting the claim dialect.
     */
    private Optional<ClaimDialect> retrieveClaimDialectInHierarchy(String claimDialectURI, String orgId)
            throws ClaimMetadataException {

        int tenantId = getTenantId(orgId);
        return cacheBackedDBBasedClaimMetadataManager.getClaimDialect(claimDialectURI, tenantId);
    }

    /**
     * Add a claim dialect.
     *
     * @param claimDialect Claim dialect.
     * @param tenantId     Tenant ID.
     * @throws ClaimMetadataException If an error occurs while adding claim dialect.
     */
    public void addClaimDialect(ClaimDialect claimDialect, int tenantId) throws ClaimMetadataException {

        String tenantDomain = IdentityTenantUtil.getTenantDomain(tenantId);
        if (resolveWithHierarchicalMode(tenantDomain, tenantId)) {
            this.cacheBackedDBBasedClaimMetadataManager.addClaimDialect(claimDialect, tenantId);
        } else {
            this.dbBasedClaimMetadataManager.addClaimDialect(claimDialect, tenantId);
        }
    }

    /**
     * Rename a claim dialect.
     *
     * @param oldClaimDialect Old claim dialect.
     * @param newClaimDialect New claim dialect.
     * @param tenantId        Tenant ID.
     * @throws ClaimMetadataException If an error occurs while renaming claim dialect.
     */
    public void renameClaimDialect(ClaimDialect oldClaimDialect, ClaimDialect newClaimDialect, int tenantId)
            throws ClaimMetadataException {

        boolean isSystemDefaultClaimDialect = isSystemDefaultClaimDialect(oldClaimDialect.getClaimDialectURI(),
                tenantId);
        if (isSystemDefaultClaimDialect) {
            throw new ClaimMetadataClientException(ERROR_CODE_NO_RENAME_SYSTEM_DIALECT.getCode(),
                    String.format(ERROR_CODE_NO_RENAME_SYSTEM_DIALECT.getMessage(),
                            oldClaimDialect.getClaimDialectURI()));
        }
        String tenantDomain = IdentityTenantUtil.getTenantDomain(tenantId);
        if (resolveWithHierarchicalMode(tenantDomain, tenantId)) {
            this.cacheBackedDBBasedClaimMetadataManager.renameClaimDialect(oldClaimDialect, newClaimDialect, tenantId);
        } else {
            this.dbBasedClaimMetadataManager.renameClaimDialect(oldClaimDialect, newClaimDialect, tenantId);
        }
    }

    /**
     * Remove a claim dialect.
     *
     * @param claimDialect Claim dialect.
     * @param tenantId     Tenant ID.
     * @throws ClaimMetadataException If an error occurs while removing claim dialect.
     */
    public void removeClaimDialect(ClaimDialect claimDialect, int tenantId) throws ClaimMetadataException {

        boolean isSystemDefaultClaimDialect = isSystemDefaultClaimDialect(claimDialect.getClaimDialectURI(), tenantId);
        if (isSystemDefaultClaimDialect) {
            throw new ClaimMetadataClientException(ERROR_CODE_NO_DELETE_SYSTEM_DIALECT.getCode(),
                    String.format(ERROR_CODE_NO_DELETE_SYSTEM_DIALECT.getMessage(), claimDialect.getClaimDialectURI()));
        }

        String tenantDomain = IdentityTenantUtil.getTenantDomain(tenantId);
        if (resolveWithHierarchicalMode(tenantDomain, tenantId)) {
            this.cacheBackedDBBasedClaimMetadataManager.removeClaimDialect(claimDialect, tenantId);
        } else {
            this.dbBasedClaimMetadataManager.removeClaimDialect(claimDialect, tenantId);
        }
    }

    /**
     * Get all local claims.
     *
     * @param tenantId Tenant ID.
     * @return List of local claims.
     * @throws ClaimMetadataException If an error occurs while retrieving local claims.
     */
    public List<LocalClaim> getLocalClaims(int tenantId) throws ClaimMetadataException {

        List<LocalClaim> localClaimsInSystem = this.systemDefaultClaimMetadataManager.getLocalClaims(tenantId);
        List<LocalClaim> localClaimsInDB = getLocalClaimsInDB(tenantId);
        Map<String, LocalClaim> localClaimMap = localClaimsInDB.stream()
                .collect(Collectors.toMap(LocalClaim::getClaimURI, claim -> claim));

        localClaimsInSystem.forEach(systemClaim -> {
            markAsSystemClaim(systemClaim);
            localClaimMap.merge(systemClaim.getClaimURI(), systemClaim, (existingClaim, newClaim) -> {
                markAsSystemClaim(existingClaim);
                for (Map.Entry<String, String> entry : newClaim.getClaimProperties().entrySet()) {
                    if (!existingClaim.getClaimProperties().containsKey(entry.getKey())) {
                        existingClaim.setClaimProperty(entry.getKey(), entry.getValue());
                    }
                }
                resolvePrimaryUserStoreMappingFromSystemClaim(existingClaim, newClaim);
                return existingClaim;
            });
        });

        for (LocalClaim localClaim : localClaimMap.values()) {
            // If FlowInitiator claim property is present in localClaimsInDB, set it to system value.
            setFlowInitiatorClaimProperty(localClaim.getClaimURI(), tenantId, localClaim);
            // If SharedProfileValueResolvingMethod is missing in localClaimsInDB, set it to default value.
            setDefaultSharedProfileValueResolvingMethod(localClaim.getClaimURI(), tenantId, localClaim);
        }

        return new ArrayList<>(localClaimMap.values());
    }

    /**
     * Resolves the primary user store mapping; this checks if the existing claim has the primary user store mapping,
     * and if it is not found, the value is taken from the system claim instead.
     *
     * @param existingClaimFromDB The local claim found in the database.
     * @param systemClaim         The system claim from the claim-config.xml file.
     */
    private void resolvePrimaryUserStoreMappingFromSystemClaim(LocalClaim existingClaimFromDB, LocalClaim systemClaim) {

        String primaryUserStoreDomain = IdentityUtil.getPrimaryDomainName();
        boolean hasPrimaryMapping = existingClaimFromDB.getMappedAttributes().stream()
                .anyMatch(mapping -> mapping.getUserStoreDomain().equalsIgnoreCase(primaryUserStoreDomain));
        if (!hasPrimaryMapping) {
            systemClaim.getMappedAttributes().stream()
                    .filter(mapping -> mapping.getUserStoreDomain().equalsIgnoreCase(primaryUserStoreDomain))
                    .findFirst()
                    .ifPresent(mapping -> existingClaimFromDB.getMappedAttributes().add(
                            new AttributeMapping(mapping.getUserStoreDomain(), mapping.getAttributeName())
                    ));
        }
    }

    /**
     * Resolves the primary user store mapping; this checks if the existing claim has the primary user store mapping,
     * and if it is not found, the value is taken from the parent claim instead, provided the parent has a mapping.
     *
     * @param existingClaimFromDB The existing local claim being aggregated.
     * @param parentClaim         The local claim found in the database for the parent tenant.
     */
    private void resolvePrimaryUserStoreMappingFromDB(LocalClaim existingClaimFromDB, LocalClaim parentClaim) {

        /*
        As the primary user store mapping is inherited from the parent, there will be no primary user store mapping
        until the root organization is reached, and at this point, the value will be added to the claim.
        */
        String primaryUserStoreDomain = IdentityUtil.getPrimaryDomainName();
        for (AttributeMapping attributeMapping : parentClaim.getMappedAttributes()) {
            if (attributeMapping.getUserStoreDomain().equalsIgnoreCase(primaryUserStoreDomain)) {
                existingClaimFromDB.getMappedAttributes().add(new AttributeMapping(
                        attributeMapping.getUserStoreDomain(), attributeMapping.getAttributeName()));
                break;
            }
        }
    }

    /**
     * Resolves the claim properties from the parent organization.
     * <p>
     * The traversal is from the child organization upwards to the parents, therefore, if the property is present in
     * the existing claim, i.e., the claim aggregated from the child organization, it is the overridden value, which
     * will be prioritized.
     * <p>
     * If it is not present in the existing claim, it has not been overridden, therefore we get the parent's value.
     *
     * @param existingClaimFromDB The existing local claim being aggregated.
     * @param parentClaim         The local claim found in the database for the parent tenant.
     */
    private void resolveClaimPropertiesFromDB(LocalClaim existingClaimFromDB, LocalClaim parentClaim) {

        for (Map.Entry<String, String> entry : parentClaim.getClaimProperties().entrySet()) {
            if (ClaimConstants.EXCLUDED_USER_STORES_PROPERTY.equalsIgnoreCase(entry.getKey())) {
                continue;
            }
            if (existingClaimFromDB.getClaimProperty(entry.getKey()) == null) {
                existingClaimFromDB.setClaimProperty(entry.getKey(), entry.getValue());
            }
        }
    }

    /**
     * Gets the local claims in the DB for the given tenant. If hierarchical inheritance is enabled, gets the merged
     * local claims for the given tenant and its parent tenants in the DB.
     *
     * @param tenantId The id of the tenant for which the claims are to be retrieved.
     * @return The local claims in the DB.
     * @throws ClaimMetadataException If an error occurs when getting the local claims.
     */
    private List<LocalClaim> getLocalClaimsInDB(int tenantId) throws ClaimMetadataException {

        List<LocalClaim> localClaimsInDB;
        String tenantDomain = IdentityTenantUtil.getTenantDomain(tenantId);
        if (resolveWithHierarchicalMode(tenantDomain, tenantId)) {
            if (isOrganization(tenantId)) {
                try {
                    String organizationId = getOrganizationId(tenantDomain, tenantId);
                    localClaimsInDB = IdentityClaimManagementServiceDataHolder.getInstance()
                            .getOrgResourceResolverService().getResourcesFromOrgHierarchy(organizationId,
                                    LambdaExceptionUtils.rethrowFunction(orgId ->
                                            this.retrieveLocalClaimsFromHierarchy(orgId, tenantId)),
                                    new MergeAllAggregationStrategy<>(this::mergeLocalClaimsInHierarchy)
                            );
                } catch (OrgResourceHierarchyTraverseException e) {
                    throw new ClaimMetadataException(ERROR_CODE_FAILURE_IN_TRAVERSING_HIERARCHY.getCode(),
                            String.format(ERROR_CODE_FAILURE_IN_TRAVERSING_HIERARCHY.getMessage(), tenantId,
                                    tenantDomain), e);
                }
            } else {
                localClaimsInDB = cacheBackedDBBasedClaimMetadataManager.getLocalClaims(tenantId);
            }
        } else {
            localClaimsInDB = dbBasedClaimMetadataManager.getLocalClaims(tenantId);
        }
        return localClaimsInDB;
    }

    /**
     * Retrieves local claims for an organization in the hierarchy during sub-organization local claim aggregation.
     *
     * @param orgId The organization id of the tenant for which the local claims need to be retrieved.
     * @return The local claims of the given tenant.
     * @throws ClaimMetadataException If an error occurs when getting the local claims.
     */
    private Optional<List<LocalClaim>> retrieveLocalClaimsFromHierarchy(String orgId, int currentTenantId)
            throws ClaimMetadataException {

        int tenantId = getTenantId(orgId);
        List<LocalClaim> localClaims = cacheBackedDBBasedClaimMetadataManager.getLocalClaims(tenantId);
        /*
        * If the id of the tenant for which data is being retrieved matches the current tenant id, it means this is
        * the first retrieval of claims and deep clones of the claims are created before they are returned to construct
        * the aggregated claim list. This helps prevent mutation of the local claims stored in the cache.
        */
        if (tenantId == currentTenantId) {
            List<LocalClaim> copiedLocalClaims = new ArrayList<>(localClaims.size());
            for (LocalClaim localClaim: localClaims) {
                LocalClaim copiedLocalClaim = new LocalClaim(localClaim.getClaimURI(),
                        new ArrayList<>(localClaim.getMappedAttributes().size()),
                        new HashMap<>(localClaim.getClaimProperties().size()));
                for (AttributeMapping attributeMapping : localClaim.getMappedAttributes()) {
                    copiedLocalClaim.getMappedAttributes().add(new AttributeMapping(
                            attributeMapping.getUserStoreDomain(), attributeMapping.getAttributeName()));
                }
                for (Map.Entry<String, String> entry : localClaim.getClaimProperties().entrySet()) {
                    copiedLocalClaim.getClaimProperties().put(entry.getKey(), entry.getValue());
                }
                copiedLocalClaims.add(copiedLocalClaim);
            }
            return Optional.of(copiedLocalClaims);
        }
        return Optional.ofNullable(localClaims);
    }

    /**
     * Merges local claims in the hierarchy and removes duplicates found at higher levels as priority is given
     * to the lower levels.
     *
     * @param aggregatedLocalClaims The local claims aggregated from the child organizations so far.
     * @param tenantLocalClaims     The local claims of the current tenant being considered.
     * @return The merged list of local claims up to the specific tenant being considered.
     */
    private List<LocalClaim> mergeLocalClaimsInHierarchy(
            List<LocalClaim> aggregatedLocalClaims, List<LocalClaim> tenantLocalClaims) {

        Map<String, LocalClaim> existingLocalClaims = aggregatedLocalClaims.stream()
                .collect(Collectors.toMap(LocalClaim::getClaimURI, Function.identity()));

        for (LocalClaim tenantLocalClaim : tenantLocalClaims) {
            String claimURI = tenantLocalClaim.getClaimURI();
            if (existingLocalClaims.containsKey(claimURI)) {
                LocalClaim aggregatedLocalClaim = existingLocalClaims.get(claimURI);
                mergeExistingLocalClaimInHierarchy(aggregatedLocalClaim, tenantLocalClaim);
            } else {
                /*
                 * The attribute mappings are deep copied to avoid mutating the parent tenants' cached
                 * references during merging.
                 */
                LocalClaim copiedLocalClaim = new LocalClaim(tenantLocalClaim.getClaimURI(),
                        new ArrayList<>(tenantLocalClaim.getMappedAttributes().size()),
                        new HashMap<>(tenantLocalClaim.getClaimProperties().size()));
                resolvePrimaryUserStoreMappingFromDB(copiedLocalClaim, tenantLocalClaim);
                resolveClaimPropertiesFromDB(copiedLocalClaim, tenantLocalClaim);
                aggregatedLocalClaims.add(copiedLocalClaim);
            }
        }
        return aggregatedLocalClaims;
    }

    /**
     * Merges a local claim in the hierarchy and removes duplicate claim properties found at higher levels as
     * priority is given to the lower levels.
     *
     * @param aggregatedLocalClaim The local claim aggregated from the child organizations so far.
     * @param tenantLocalClaim     The local claim of the current tenant being considered.
     * @return The merged local claim with the claim properties up to the specific tenant being considered.
     */
    private LocalClaim mergeExistingLocalClaimInHierarchy(
            LocalClaim aggregatedLocalClaim, LocalClaim tenantLocalClaim) {

        /*
        * At the point this method is called, the existing claim is expected to have been deeply copied already,
        * therefore, no deep copy is created during this merge.
        */
        resolveClaimPropertiesFromDB(aggregatedLocalClaim, tenantLocalClaim);
        resolvePrimaryUserStoreMappingFromDB(aggregatedLocalClaim, tenantLocalClaim);
        return aggregatedLocalClaim;
    }

    /**
     * Get a local claim by URI.
     *
     * @param localClaimURI Local claim URI.
     * @param tenantId      Tenant ID.
     * @return Local claim.
     * @throws ClaimMetadataException If an error occurs while retrieving local claim.
     */
    public Optional<LocalClaim> getLocalClaim(String localClaimURI, int tenantId) throws ClaimMetadataException {

        Optional<LocalClaim> localClaimInDB = retrieveLocalClaimInDBFromHierarchy(localClaimURI, tenantId);
        Optional<LocalClaim> localClaimInSystem = this.systemDefaultClaimMetadataManager.
                getLocalClaim(localClaimURI, tenantId);
        LocalClaim dbLocalClaim = localClaimInDB.orElse(null);
        LocalClaim systemDBClaim = localClaimInSystem.orElse(null);
        if (dbLocalClaim != null) {
            if (isSystemDefaultLocalClaim(localClaimURI, tenantId)) {
                markAsSystemClaim(dbLocalClaim);
            }
            // If SharedProfileValueResolvingMethod is missing in DB, set it to default value.
            setDefaultSharedProfileValueResolvingMethod(localClaimURI, tenantId, dbLocalClaim);
            if (systemDBClaim != null) {
                for (Map.Entry<String, String> entry : systemDBClaim.getClaimProperties().entrySet()) {
                    if (!dbLocalClaim.getClaimProperties().containsKey(entry.getKey())) {
                        dbLocalClaim.setClaimProperty(entry.getKey(), entry.getValue());
                    }
                }
                resolvePrimaryUserStoreMappingFromSystemClaim(dbLocalClaim, systemDBClaim);
            }
            return localClaimInDB;
        }
        if (systemDBClaim != null) {
            markAsSystemClaim(systemDBClaim);
            return localClaimInSystem;
        }
        return Optional.empty();
    }

    /**
     * Retrieves the local claim in the database based on whether hierarchical inheritance is enabled.
     *
     * @param localClaimURI The URI of the local claim to be retrieved.
     * @param tenantId      The organization id of the tenant for which the local claim needs to be retrieved.
     * @return The local claim in the database.
     * @throws ClaimMetadataException If an error occurs during retrieval.
     */
    private Optional<LocalClaim> retrieveLocalClaimInDBFromHierarchy(String localClaimURI, int tenantId)
            throws ClaimMetadataException {

        Optional<LocalClaim> localClaimInDB;
        String tenantDomain = IdentityTenantUtil.getTenantDomain(tenantId);
        if (resolveWithHierarchicalMode(tenantDomain, tenantId)) {
            if (isOrganization(tenantId)) {
                try {
                    String organizationId = getOrganizationId(tenantDomain, tenantId);
                    OrgResourceResolverService orgResourceManagementService =
                            IdentityClaimManagementServiceDataHolder.getInstance().getOrgResourceResolverService();
                    LocalClaim mergedLocalClaim = orgResourceManagementService.getResourcesFromOrgHierarchy(
                            organizationId,
                            LambdaExceptionUtils.rethrowFunction(orgId ->
                                    this.retrieveLocalClaimFromHierarchy(localClaimURI, orgId)),
                            new MergeAllAggregationStrategy<>(this::mergeLocalClaimInHierarchy)
                    );
                    localClaimInDB = Optional.of(mergedLocalClaim);
                } catch (OrgResourceHierarchyTraverseException e) {
                    throw new ClaimMetadataException(ERROR_CODE_FAILURE_IN_TRAVERSING_HIERARCHY.getCode(),
                            String.format(ERROR_CODE_FAILURE_IN_TRAVERSING_HIERARCHY.getMessage(), tenantId,
                                    tenantDomain), e);
                }
            } else {
                localClaimInDB = this.cacheBackedDBBasedClaimMetadataManager.getLocalClaim(localClaimURI, tenantId);
            }
        } else {
            localClaimInDB = this.dbBasedClaimMetadataManager.getLocalClaim(localClaimURI, tenantId);
        }
        return localClaimInDB;
    }

    /**
     * Retrieves a local claim for an organization in the hierarchy during sub-organization local claim aggregation.
     *
     * @param localClaimURI The URI of the local claim to be retrieved.
     * @param orgId         The organization id of the tenant for which the local claim needs to be retrieved.
     * @return The local claim of the given tenant.
     * @throws ClaimMetadataException If an error occurs when getting the local claim.
     */
    private Optional<LocalClaim> retrieveLocalClaimFromHierarchy(String localClaimURI, String orgId)
            throws ClaimMetadataException {

        int tenantId = getTenantId(orgId);
        return cacheBackedDBBasedClaimMetadataManager.getLocalClaim(localClaimURI, tenantId);
    }

    /**
     * Merges a local claim in the hierarchy and removes duplicate claim properties found at higher levels as
     * priority is given to the lower levels.
     *
     * @param aggregatedLocalClaim The local claim aggregated from the child organizations so far.
     * @param tenantLocalClaim     The local claim of the current tenant being considered.
     * @return The merged local claim with the claim properties up to the specific tenant being considered.
     */
    private LocalClaim mergeLocalClaimInHierarchy(
            LocalClaim aggregatedLocalClaim, LocalClaim tenantLocalClaim) {

        /*
         * The attribute mappings are deep copied to avoid mutating the parent tenants' cached
         * references during merging.
         */
        LocalClaim copiedLocalClaim = new LocalClaim(tenantLocalClaim.getClaimURI(),
                new ArrayList<>(tenantLocalClaim.getMappedAttributes().size()),
                new HashMap<>(tenantLocalClaim.getClaimProperties().size()));
        resolveClaimPropertiesFromDB(aggregatedLocalClaim, tenantLocalClaim);
        resolvePrimaryUserStoreMappingFromDB(aggregatedLocalClaim, tenantLocalClaim);
        return copiedLocalClaim;
    }

    private void setDefaultSharedProfileValueResolvingMethod(String localClaimURI, int tenantId,
                                                             LocalClaim localClaimInDB) throws ClaimMetadataException {

        String sharedProfileValueResolvingMethod =
                localClaimInDB.getClaimProperty(ClaimConstants.SHARED_PROFILE_VALUE_RESOLVING_METHOD);
        if (StringUtils.isNotBlank(sharedProfileValueResolvingMethod)) {
            return;
        }
        // If the claim is a system claim, get the default value set in the system default claim metadata.
        if (isSystemDefaultLocalClaim(localClaimURI, tenantId)) {
            Optional<LocalClaim> localClaimInSystem = this.systemDefaultClaimMetadataManager.getLocalClaim(
                    localClaimURI, tenantId);
            if (localClaimInSystem.isPresent()) {
                String systemDefaultSharedProfileValueResolvingMethod = localClaimInSystem.get()
                        .getClaimProperty(ClaimConstants.SHARED_PROFILE_VALUE_RESOLVING_METHOD);
                if (StringUtils.isNotBlank(systemDefaultSharedProfileValueResolvingMethod)) {
                    localClaimInDB.setClaimProperty(ClaimConstants.SHARED_PROFILE_VALUE_RESOLVING_METHOD,
                            systemDefaultSharedProfileValueResolvingMethod);
                } else {
                    if (LOG.isDebugEnabled()) {
                        LOG.debug(String.format("SharedProfileValueResolvingMethod is not defined for the system " +
                                "claim: %s", localClaimURI));
                    }
                }
            }
        } else {
            // For custom claims set the FromOrigin as the default value.
            localClaimInDB.setClaimProperty(ClaimConstants.SHARED_PROFILE_VALUE_RESOLVING_METHOD,
                    ClaimConstants.SharedProfileValueResolvingMethod.FROM_ORIGIN.getName());
        }
    }

    private void setFlowInitiatorClaimProperty(String localClaimURI, int tenantId,
                                               LocalClaim localClaimInDB) throws ClaimMetadataException {

        if (!isSystemDefaultLocalClaim(localClaimURI, tenantId)) {
            return;
        }

        // If the claim is a system claim, get the default value set in the system default claim metadata.
        Optional<LocalClaim> localClaimInSystem = this.systemDefaultClaimMetadataManager.getLocalClaim(
                localClaimURI, tenantId);
        if (localClaimInSystem.isPresent()) {
            String flowInitiatorProperty = localClaimInSystem.get().getClaimProperty(ClaimConstants.FLOW_INITIATOR);
            if (StringUtils.isNotBlank(flowInitiatorProperty)) {
                localClaimInDB.setClaimProperty(ClaimConstants.FLOW_INITIATOR, flowInitiatorProperty);
            }
        }
    }

    /**
     * Add a local claim.
     *
     * @param localClaim Local claim.
     * @param tenantId   Tenant ID.
     * @throws ClaimMetadataException If an error occurs while adding local claim.
     */
    public void addLocalClaim(LocalClaim localClaim, int tenantId) throws ClaimMetadataException {

        validateNonModifiableClaimProperties(localClaim);
        String tenantDomain = IdentityTenantUtil.getTenantDomain(tenantId);
        if (!isClaimDialectInDB(ClaimConstants.LOCAL_CLAIM_DIALECT_URI, tenantId, tenantDomain)) {
            addSystemDefaultDialectToDB(ClaimConstants.LOCAL_CLAIM_DIALECT_URI, tenantId);
        }
        if (resolveWithHierarchicalMode(tenantDomain, tenantId)) {
            if (isOrganization(tenantId)) {
                localClaim.getMappedAttributes().removeIf(attributeMapping ->
                        attributeMapping.getUserStoreDomain().equalsIgnoreCase(IdentityUtil.getPrimaryDomainName()));
            }
            this.cacheBackedDBBasedClaimMetadataManager.addLocalClaim(localClaim, tenantId);
        } else {
            this.dbBasedClaimMetadataManager.addLocalClaim(localClaim, tenantId);
        }
    }

    /**
     * Update a local claim.
     *
     * @param localClaim Local claim.
     * @param tenantId   Tenant ID.
     * @throws ClaimMetadataException If an error occurs while updating local claim.
     */
    public void updateLocalClaim(LocalClaim localClaim, int tenantId) throws ClaimMetadataException {

        validateNonModifiableClaimProperties(localClaim);
        String tenantDomain = IdentityTenantUtil.getTenantDomain(tenantId);
        if (isLocalClaimInDB(localClaim.getClaimURI(), tenantId, tenantDomain)) {
            if (resolveWithHierarchicalMode(tenantDomain, tenantId)) {
                if (isOrganization(tenantId)) {
                    localClaim.getMappedAttributes().removeIf(attributeMapping ->
                            attributeMapping.getUserStoreDomain()
                                    .equalsIgnoreCase(IdentityUtil.getPrimaryDomainName()));
                }
                this.cacheBackedDBBasedClaimMetadataManager.updateLocalClaim(localClaim, tenantId);
            } else {
                this.dbBasedClaimMetadataManager.updateLocalClaim(localClaim, tenantId);
            }
        } else {
            this.addLocalClaim(localClaim, tenantId);
        }
    }

    /**
     * Update local claim mappings.
     *
     * @param localClaimList  List of local claims.
     * @param tenantId        Tenant ID.
     * @param userStoreDomain User store domain.
     * @throws ClaimMetadataException If an error occurs while updating local claim mappings.
     */
    public void updateLocalClaimMappings(List<LocalClaim> localClaimList, int tenantId, String userStoreDomain)
            throws ClaimMetadataException {

        if (localClaimList == null) {
            return;
        }
        String tenantDomain = IdentityTenantUtil.getTenantDomain(tenantId);
        boolean isHierarchicalMode = resolveWithHierarchicalMode(tenantDomain, tenantId);
        String primaryUserStoreDomain = IdentityUtil.getPrimaryDomainName();
        if (!localClaimList.isEmpty() && !isClaimDialectInDB(ClaimConstants.LOCAL_CLAIM_DIALECT_URI, tenantId,
                tenantDomain)) {
            addSystemDefaultDialectToDB(ClaimConstants.LOCAL_CLAIM_DIALECT_URI, tenantId);
        }

        Map<String, LocalClaim> localClaimMap = this.getLocalClaims(tenantId).stream()
                .collect(Collectors.toMap(LocalClaim::getClaimURI, localClaim -> localClaim));
        for (LocalClaim localClaim : localClaimList) {
            if (localClaimMap.get(localClaim.getClaimURI()) == null) {
                throw new ClaimMetadataClientException(ERROR_CODE_NON_EXISTING_LOCAL_CLAIM_URI.getCode(),
                        String.format(ERROR_CODE_NON_EXISTING_LOCAL_CLAIM_URI.getMessage(), localClaim.getClaimURI()));
            }

            Predicate<AttributeMapping> filterCondition;
            if (isHierarchicalMode && isOrganization(tenantId)) {
                filterCondition = mappedAttribute -> !mappedAttribute.getUserStoreDomain().equals(userStoreDomain) &&
                                !mappedAttribute.getUserStoreDomain().equals(primaryUserStoreDomain);
            } else {
                filterCondition = mappedAttribute -> !mappedAttribute.getUserStoreDomain().equals(userStoreDomain);
                localClaim.setClaimProperties(localClaimMap.get(localClaim.getClaimURI()).getClaimProperties());
            }
            List<AttributeMapping> missingMappedAttributes = localClaimMap.get(localClaim.getClaimURI())
                    .getMappedAttributes().stream()
                    .filter(filterCondition)
                    .collect(Collectors.toList());
            localClaim.getMappedAttributes().addAll(missingMappedAttributes);
        }
        if (isHierarchicalMode) {
            this.cacheBackedDBBasedClaimMetadataManager
                    .updateLocalClaimMappings(localClaimList, tenantId, userStoreDomain);
        } else {
            this.dbBasedClaimMetadataManager.updateLocalClaimMappings(localClaimList, tenantId, userStoreDomain);
        }
    }

    /**
     * Remove a local claim.
     *
     * @param localClaimURI Local claim URI.
     * @param tenantId      Tenant ID.
     * @throws ClaimMetadataException If an error occurs while removing local claim.
     */
    public void removeLocalClaim(String localClaimURI, int tenantId) throws ClaimMetadataException {

        boolean isSystemDefaultClaim = isSystemDefaultLocalClaim(localClaimURI, tenantId);
        if (isSystemDefaultClaim) {
            throw new ClaimMetadataClientException(ERROR_CODE_NO_DELETE_SYSTEM_CLAIM.getCode(),
                    String.format(ERROR_CODE_NO_DELETE_SYSTEM_CLAIM.getMessage(), localClaimURI));
        }

        String tenantDomain = IdentityTenantUtil.getTenantDomain(tenantId);
        if (resolveWithHierarchicalMode(tenantDomain, tenantId)) {
            this.cacheBackedDBBasedClaimMetadataManager.removeLocalClaim(localClaimURI, tenantId);
        } else {
            this.dbBasedClaimMetadataManager.removeLocalClaim(localClaimURI, tenantId);
        }
    }

    /**
     * Get all external claims.
     *
     * @param externalClaimDialectURI External claim dialect URI.
     * @param tenantId                Tenant ID.
     * @return List of external claims.
     * @throws ClaimMetadataException If an error occurs while retrieving external claims.
     */
    public List<ExternalClaim> getExternalClaims(String externalClaimDialectURI, int tenantId)
            throws ClaimMetadataException {

        List<ExternalClaim> externalClaimsInSystem = this.systemDefaultClaimMetadataManager.getExternalClaims(
                externalClaimDialectURI, tenantId);
        List<ExternalClaim> externalClaimsInDB = retrieveExternalClaimsInDBFromHierarchy(externalClaimDialectURI,
                tenantId);

            Map<String, ExternalClaim> externalClaimsInDBMap = new HashMap<>();
            Map<String, ExternalClaim> mappedLocalClaimInDBMap = new HashMap<>();
            externalClaimsInDB.forEach(claim -> {
                externalClaimsInDBMap.put(claim.getClaimURI(), claim);
                mappedLocalClaimInDBMap.put(claim.getMappedLocalClaim(), claim);
            });
            /*
             * If a system claim is also in the DB, then the claim retrieved from the DB gets the priority.
             * Also, if there is a system claim that is mapped to the same local claim as another external claim in the same
             * dialect, then we do not enforce the system claim on the tenant because that would violate the constraint of
             * having a unique claim mapping within the dialect. This is to preserve backward-compatibility.
             */
            List<ExternalClaim> allExternalClaims = new ArrayList<>();
            for (ExternalClaim externalClaimInSystem : externalClaimsInSystem) {
                ExternalClaim matchingClaimInDB = externalClaimsInDBMap.get(externalClaimInSystem.getClaimURI());
                if (matchingClaimInDB != null) {
                    markAsSystemClaim(matchingClaimInDB);
                    allExternalClaims.add(matchingClaimInDB);
                    externalClaimsInDBMap.remove(externalClaimInSystem.getClaimURI());
                } else if (!mappedLocalClaimInDBMap.containsKey(externalClaimInSystem.getMappedLocalClaim())) {
                    externalClaimInSystem.setClaimProperty(ClaimConstants.IS_SYSTEM_CLAIM, Boolean.TRUE.toString());
                    allExternalClaims.add(externalClaimInSystem);
                }
            }
            allExternalClaims.addAll(externalClaimsInDBMap.values());
            return allExternalClaims;

    }

    /**
     * Retrieves the external claims in the database based on whether hierarchical inheritance is enabled.
     *
     * @param externalClaimDialectURI The URI of the external claim dialect whose claims are to be retrieved.
     * @param tenantId                The id of the tenant for which the external claim needs to be retrieved.
     * @return The external claims of the given tenant and its parents in the DB.
     * @throws ClaimMetadataException
     */
    private List<ExternalClaim> retrieveExternalClaimsInDBFromHierarchy(String externalClaimDialectURI, int tenantId)
            throws ClaimMetadataException {

        String tenantDomain = IdentityTenantUtil.getTenantDomain(tenantId);
        List<ExternalClaim> externalClaimsInDB;
        if (resolveWithHierarchicalMode(tenantDomain, tenantId)) {
            if (isOrganization(tenantId)) {
                try {
                    String organizationId = getOrganizationId(tenantDomain, tenantId);
                    externalClaimsInDB = IdentityClaimManagementServiceDataHolder.getInstance()
                        .getOrgResourceResolverService().getResourcesFromOrgHierarchy(organizationId,
                                LambdaExceptionUtils.rethrowFunction(orgId ->
                                        retrieveExternalClaimsFromHierarchy(externalClaimDialectURI, orgId)),
                                new MergeAllAggregationStrategy<>(this::mergesExternalClaimsInHierarchy)
                        );
                } catch (OrgResourceHierarchyTraverseException e) {
                    throw new ClaimMetadataException(ERROR_CODE_FAILURE_IN_TRAVERSING_HIERARCHY.getCode(),
                            String.format(ERROR_CODE_FAILURE_IN_TRAVERSING_HIERARCHY.getMessage(), tenantId,
                                    tenantDomain), e);
                }
            } else {
                externalClaimsInDB = this.cacheBackedDBBasedClaimMetadataManager.getExternalClaims(
                        externalClaimDialectURI, tenantId);
            }
        } else {
            externalClaimsInDB = this.dbBasedClaimMetadataManager.getExternalClaims(
                    externalClaimDialectURI, tenantId);
        }
        return externalClaimsInDB;
    }

    /**
     * Retrieves external claims for an organization in the hierarchy during sub-organization external claim
     * aggregation.
     *
     * @param externalClaimDialectURI The URI of the external claim dialect whose claims are to be retrieved.
     * @param orgId                   The organization id of the tenant for which the external claim needs to be
     *                                retrieved.
     * @return The external claims of the given tenant.
     * @throws ClaimMetadataException If an error occurs when getting the external claims.
     */
    private Optional<List<ExternalClaim>> retrieveExternalClaimsFromHierarchy(String externalClaimDialectURI,
                                                                              String orgId)
            throws ClaimMetadataException {

        int tenantId = getTenantId(orgId);
        List<ExternalClaim> externalClaims = cacheBackedDBBasedClaimMetadataManager.getExternalClaims(
                externalClaimDialectURI, tenantId);
        return Optional.ofNullable(externalClaims);
    }

    /**
     * Merges external claims in the hierarchy and removes duplicates found at higher levels as priority is given to
     * the lower levels.
     *
     * @param aggregatedExternalClaims The external claims aggregated from the child organizations so far.
     * @param tenantExternalClaims     The external claims of the current tenant being considered.
     * @return The merged external claims of the specific tenant being considered.
     */
    private List<ExternalClaim> mergesExternalClaimsInHierarchy(
            List<ExternalClaim> aggregatedExternalClaims, List<ExternalClaim> tenantExternalClaims) {

        Map<String, ExternalClaim> existingExternalClaims = aggregatedExternalClaims.stream()
                .collect(Collectors.toMap(ExternalClaim::getClaimURI, Function.identity()));

        for (ExternalClaim tenantExternalClaim : tenantExternalClaims) {
            String claimURI = tenantExternalClaim.getClaimURI();

            if (!existingExternalClaims.containsKey(claimURI)) {
                aggregatedExternalClaims.add(tenantExternalClaim);
            }
            /*
            * External claim properties have been intentionally ignored during the merging to keep with the existing
            * behaviour in the in-memory claim implementation where any external claim found in the db ignores the
            * properties set in the claim-config.xml file.
            *
            * The API for adding custom external claims do not support properties, therefore, this is not a concern for
            * custom external claims not found in the claim-config.xml.
            */
        }
        return aggregatedExternalClaims;
    }

    /**
     * Get an external claim by URI.
     *
     * @param externalClaimDialectURI External claim dialect URI.
     * @param externalClaimURI        Claim URI.
     * @param tenantId                Tenant ID.
     * @return External claim.
     * @throws ClaimMetadataException If an error occurs while retrieving external claim.
     */
    public Optional<ExternalClaim> getExternalClaim(String externalClaimDialectURI, String externalClaimURI,
                                                    int tenantId) throws ClaimMetadataException {

        List<ExternalClaim> externalClaimsInDB = retrieveExternalClaimsInDBFromHierarchy(externalClaimDialectURI,
                tenantId);
        Optional<ExternalClaim> externalClaim = Optional.empty();
        Map<String, ExternalClaim> mappedLocalClaimInDBMap = new HashMap<>();

        for (ExternalClaim externalClaimInDB : externalClaimsInDB) {
            if (externalClaimInDB.getClaimURI().equals(externalClaimURI)) {
                externalClaim = Optional.of(externalClaimInDB);
            }
            mappedLocalClaimInDBMap.put(externalClaimInDB.getMappedLocalClaim(), externalClaimInDB);
        }
        if (externalClaim.isPresent()) {
            if (isSystemDefaultExternalClaim(externalClaimDialectURI, externalClaimURI, tenantId)) {
                markAsSystemClaim(externalClaim.get());
            }
            return externalClaim;
        }

        Optional<ExternalClaim> externalClaimInSystem = this.systemDefaultClaimMetadataManager.getExternalClaim(
                externalClaimDialectURI, externalClaimURI, tenantId);
        if (externalClaimInSystem.isPresent()
                && !mappedLocalClaimInDBMap.containsKey(externalClaimInSystem.get().getMappedLocalClaim())) {
            markAsSystemClaim(externalClaimInSystem.get());
            return externalClaimInSystem;
        }
        return Optional.empty();
    }

    /**
     * Add an external claim.
     *
     * @param externalClaim External claim.
     * @param tenantId      Tenant ID.
     * @throws ClaimMetadataException If an error occurs while adding external claim.
     */
    public void addExternalClaim(ExternalClaim externalClaim, int tenantId)
            throws ClaimMetadataException {

        externalClaim.getClaimProperties().remove(ClaimConstants.IS_SYSTEM_CLAIM);
        String tenantDomain = IdentityTenantUtil.getTenantDomain(tenantId);
        if (!isClaimDialectInDB(externalClaim.getClaimDialectURI(), tenantId, tenantDomain)) {
            addSystemDefaultDialectToDB(externalClaim.getClaimDialectURI(), tenantId);
        }
        if (!isLocalClaimInDB(externalClaim.getMappedLocalClaim(), tenantId, tenantDomain)) {
            addSystemDefaultLocalClaimToDB(externalClaim.getMappedLocalClaim(), tenantId);
        }
        if (resolveWithHierarchicalMode(tenantDomain, tenantId)) {
            this.cacheBackedDBBasedClaimMetadataManager.addExternalClaim(externalClaim, tenantId);
        } else {
            this.dbBasedClaimMetadataManager.addExternalClaim(externalClaim, tenantId);
        }
    }

    /**
     * Update an external claim.
     *
     * @param externalClaim External claim.
     * @param tenantId      Tenant ID.
     * @throws ClaimMetadataException If an error occurs while updating external claim.
     */
    public void updateExternalClaim(ExternalClaim externalClaim, int tenantId)
            throws ClaimMetadataException {

        externalClaim.getClaimProperties().remove(ClaimConstants.IS_SYSTEM_CLAIM);
        String tenantDomain = IdentityTenantUtil.getTenantDomain(tenantId);
        if (!isLocalClaimInDB(externalClaim.getMappedLocalClaim(), tenantId, tenantDomain)) {
            addSystemDefaultLocalClaimToDB(externalClaim.getMappedLocalClaim(), tenantId);
        }
        if (isExternalClaimInDB(externalClaim.getClaimURI(), externalClaim.getClaimDialectURI(), tenantId,
                tenantDomain)) {
            if (resolveWithHierarchicalMode(tenantDomain, tenantId)) {
                this.cacheBackedDBBasedClaimMetadataManager.updateExternalClaim(externalClaim, tenantId);
            } else {
                this.dbBasedClaimMetadataManager.updateExternalClaim(externalClaim, tenantId);
            }
        } else {
            this.addExternalClaim(externalClaim, tenantId);
        }
    }

    /**
     * Remove an external claim.
     *
     * @param externalClaimDialectURI External claim dialect URI.
     * @param externalClaimURI        External claim URI.
     * @param tenantId                Tenant ID.
     * @throws ClaimMetadataException If an error occurs while removing external claim.
     */
    public void removeExternalClaim(String externalClaimDialectURI, String externalClaimURI, int tenantId)
            throws ClaimMetadataException {

        boolean isSystemDefaultClaim = isSystemDefaultExternalClaim(externalClaimDialectURI, externalClaimURI, tenantId);
        if (isSystemDefaultClaim) {
            throw new ClaimMetadataClientException(ERROR_CODE_NO_DELETE_SYSTEM_CLAIM.getCode(),
                    String.format(ERROR_CODE_NO_DELETE_SYSTEM_CLAIM.getMessage(), externalClaimURI));
        }
        String tenantDomain = IdentityTenantUtil.getTenantDomain(tenantId);
        if (resolveWithHierarchicalMode(tenantDomain, tenantId)) {
            this.cacheBackedDBBasedClaimMetadataManager.removeExternalClaim(externalClaimDialectURI, externalClaimURI,
                    tenantId);
        } else {
            this.dbBasedClaimMetadataManager.removeExternalClaim(externalClaimDialectURI, externalClaimURI, tenantId);
        }
    }

    /**
     * Check whether any external claim maps to a given local claim.
     * @param localClaimURI Local claim URI.
     * @param tenantId      Tenant ID.
     * @return True if the local claim is mapped.
     * @throws ClaimMetadataException if an error occurs during the operation.
     */
    public boolean isMappedLocalClaim(String localClaimURI, int tenantId) throws ClaimMetadataException {

        List<ClaimDialect> claimDialects = this.getClaimDialects(tenantId);

        for (ClaimDialect claimDialect : claimDialects) {
            if (ClaimConstants.LOCAL_CLAIM_DIALECT_URI.equals(claimDialect.getClaimDialectURI())) {
                continue;
            }
            List<ExternalClaim> externalClaims = getExternalClaims(claimDialect.getClaimDialectURI(), tenantId);
            for (ExternalClaim externalClaim : externalClaims) {
                if (externalClaim.getMappedLocalClaim().equals(localClaimURI)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Remove mapped user store attributes of a user store domain.
     * @param tenantId        Tenant ID.
     * @param userstoreDomain User Store Domain name.
     * @throws ClaimMetadataException if an error occurs during the operation.
     */
    public void removeClaimMappingAttributes(int tenantId, String userstoreDomain) throws ClaimMetadataException {

        String tenantDomain = IdentityTenantUtil.getTenantDomain(tenantId);
        if (resolveWithHierarchicalMode(tenantDomain, tenantId)) {
            this.cacheBackedDBBasedClaimMetadataManager.removeClaimMappingAttributes(tenantId, userstoreDomain);
        } else {
            this.dbBasedClaimMetadataManager.removeClaimMappingAttributes(tenantId, userstoreDomain);
        }
    }

    /**
     * Remove all claim dialects.
     * @param tenantId  Tenant ID.
     * @throws ClaimMetadataException if an error occurs during the operation.
     */
    public void removeAllClaimDialects(int tenantId) throws ClaimMetadataException {

        this.cacheBackedDBBasedClaimMetadataManager.removeAllClaimDialects(tenantId);
    }

    /**
     * Get all external claims mapped to a local claim.
     * @param localClaimURI Local claim URI.
     * @param tenantId      Tenant ID.
     * @return List of mapped external claims.
     * @throws ClaimMetadataException if an error occurs during the operation.
     */
    public List<Claim> getMappedExternalClaims(String localClaimURI, int tenantId) throws ClaimMetadataException {

        List<Claim> mappedExternalClaims = new ArrayList<>();
        List<ClaimDialect> claimDialects = getClaimDialects(tenantId);
        for (ClaimDialect claimDialect : claimDialects) {
            if (ClaimConstants.LOCAL_CLAIM_DIALECT_URI.equals(claimDialect.getClaimDialectURI())) {
                continue;
            }
            List<ExternalClaim> externalClaimsInDialect = getExternalClaims(claimDialect.getClaimDialectURI(),
                    tenantId);
            for (ExternalClaim externalClaim : externalClaimsInDialect) {
                if (externalClaim.getMappedLocalClaim().equals(localClaimURI)) {
                    mappedExternalClaims.add(externalClaim);
                }
            }
        }
        return mappedExternalClaims;
    }

    /**
     * Check whether a local claim is mapped within a dialect.
     * @param mappedLocalClaim         Mapped local claim.
     * @param externalClaimDialectURI  External claim dialect URI.
     * @param tenantId                 Tenant ID.
     * @return True if the local claim is mapped.
     * @throws ClaimMetadataException if an error occurs during the operation.
     */
    public boolean isLocalClaimMappedWithinDialect(String mappedLocalClaim, String externalClaimDialectURI,
                                                   int tenantId) throws ClaimMetadataException {

        return getExternalClaims(externalClaimDialectURI, tenantId).stream()
                .anyMatch(externalClaim -> externalClaim.getMappedLocalClaim().equals(mappedLocalClaim));
    }

    /**
     * Check whether a claim dialect is a system default claim dialect.
     * @param claimDialectURI Claim dialect URI.
     * @param tenantId        Tenant ID.
     * @return True if the claim dialect is a system default claim dialect.
     * @throws ClaimMetadataException if an error occurs during the operation.
     */
    private boolean isSystemDefaultClaimDialect(String claimDialectURI, int tenantId) throws ClaimMetadataException {

        return this.systemDefaultClaimMetadataManager.getClaimDialect(claimDialectURI, tenantId).isPresent();
    }

    /**
     * Check whether a local claim is a system default local claim.
     * @param localClaimURI Local claim URI.
     * @param tenantId      Tenant ID.
     * @return True if the local claim is a system default local claim.
     * @throws ClaimMetadataException if an error occurs during the operation.
     */
    private boolean isSystemDefaultLocalClaim(String localClaimURI, int tenantId) throws ClaimMetadataException {

        return this.systemDefaultClaimMetadataManager.getLocalClaims(tenantId).stream()
                .anyMatch(localClaim -> localClaim.getClaimURI().equals(localClaimURI));
    }

    /**
     * Check whether an external claim is a system default external claim.
     * @param externalClaimDialectURI External claim dialect URI.
     * @param externalClaimURI        External claim URI.
     * @param tenantId                Tenant ID.
     * @return True if the external claim is a system default external claim.
     * @throws ClaimMetadataException if an error occurs during the operation.
     */
    private boolean isSystemDefaultExternalClaim(String externalClaimDialectURI, String externalClaimURI, int tenantId)
            throws ClaimMetadataException {

        return this.systemDefaultClaimMetadataManager.getExternalClaims(externalClaimDialectURI,tenantId).stream()
                .anyMatch(externalClaim -> externalClaim.getClaimURI().equals(externalClaimURI));
    }

    private boolean isClaimDialectInDB(String claimDialectURI, int tenantId, String tenantDomain) throws ClaimMetadataException {

        if (resolveWithHierarchicalMode(tenantDomain, tenantId)) {
            return this.cacheBackedDBBasedClaimMetadataManager.getClaimDialect(claimDialectURI, tenantId).isPresent();
        } else {
            return this.dbBasedClaimMetadataManager.getClaimDialect(claimDialectURI, tenantId).isPresent();
        }
    }

    private boolean isLocalClaimInDB(String localClaimURI, int tenantId, String tenantDomain) throws ClaimMetadataException {

        if (resolveWithHierarchicalMode(tenantDomain, tenantId)) {
            return this.cacheBackedDBBasedClaimMetadataManager.getLocalClaim(localClaimURI, tenantId).isPresent();
        } else {
            return this.dbBasedClaimMetadataManager.getLocalClaim(localClaimURI, tenantId).isPresent();
        }
    }

    private boolean isExternalClaimInDB(String claimURI, String claimDialectURI, int tenantId, String tenantDomain)
            throws ClaimMetadataException {

        if (resolveWithHierarchicalMode(tenantDomain, tenantId)) {
            return this.cacheBackedDBBasedClaimMetadataManager.getExternalClaim(claimDialectURI, claimURI, tenantId).isPresent();
        } else {
            return this.dbBasedClaimMetadataManager.getExternalClaim(claimDialectURI, claimURI, tenantId).isPresent();
        }
    }

    private void addSystemDefaultDialectToDB(String claimDialectURI, int tenantId) throws ClaimMetadataException {

        Optional<ClaimDialect> claimDialectInSystem = this.systemDefaultClaimMetadataManager
                .getClaimDialect(claimDialectURI, tenantId);
        if (claimDialectInSystem.isPresent()) {
            String tenantDomain = IdentityTenantUtil.getTenantDomain(tenantId);
            if (resolveWithHierarchicalMode(tenantDomain, tenantId)) {
                this.cacheBackedDBBasedClaimMetadataManager.addClaimDialect(claimDialectInSystem.get(), tenantId);
            } else {
                this.dbBasedClaimMetadataManager.addClaimDialect(claimDialectInSystem.get(), tenantId);
            }
        }
    }

    private void addSystemDefaultLocalClaimToDB(String claimURI, int tenantId)
            throws ClaimMetadataException {

        String tenantDomain = IdentityTenantUtil.getTenantDomain(tenantId);
        boolean isClaimDialectInDB = isClaimDialectInDB(ClaimConstants.LOCAL_CLAIM_DIALECT_URI, tenantId, tenantDomain);
        if (!isClaimDialectInDB) {
            addSystemDefaultDialectToDB(ClaimConstants.LOCAL_CLAIM_DIALECT_URI, tenantId);
        }
        Optional<LocalClaim> claimInSystem = this.systemDefaultClaimMetadataManager.getLocalClaim(claimURI, tenantId);
        if (claimInSystem.isPresent()) {
            if (resolveWithHierarchicalMode(tenantDomain, tenantId)) {
                this.cacheBackedDBBasedClaimMetadataManager.addLocalClaim(claimInSystem.get(), tenantId);
            } else {
                this.dbBasedClaimMetadataManager.addLocalClaim(claimInSystem.get(), tenantId);
            }
        }
    }

    private void validateNonModifiableClaimProperties(LocalClaim localClaim) throws ClaimMetadataClientException {

        //isSystemClaim is removed without throwing an exception to make it non-modifiable to preserve backward compatibility.
        localClaim.getClaimProperties().remove(ClaimConstants.IS_SYSTEM_CLAIM);
        if (localClaim.getClaimProperty(ClaimConstants.FLOW_INITIATOR) != null) {
            throw new ClaimMetadataClientException(
                    ClaimConstants.ErrorMessage.ERROR_CODE_CANNOT_MODIFY_FLOW_INITIATOR_CLAIM_PROPERTY.getCode(),
                    String.format(ClaimConstants.ErrorMessage.ERROR_CODE_CANNOT_MODIFY_FLOW_INITIATOR_CLAIM_PROPERTY
                                    .getMessage(), localClaim.getClaimURI()));
        }

    }

    private void markAsSystemClaim(Claim claim) {

        claim.setClaimProperty(ClaimConstants.IS_SYSTEM_CLAIM, Boolean.TRUE.toString());
    }

    /**
     * Gets the tenant id corresponding to a given organization id.
     *
     * @param orgId The organization id of the tenant to be retrieved.
     * @return The id of the tenant.
     * @throws ClaimMetadataException If an error occurs when getting the tenant id.
     */
    private int getTenantId(String orgId) throws ClaimMetadataException {

        try {
            String tenantDomain = IdentityClaimManagementServiceDataHolder.getInstance()
                    .getOrganizationManager().resolveTenantDomain(orgId);
            return IdentityClaimManagementServiceDataHolder.getInstance().getRealmService()
                    .getTenantManager().getTenantId(tenantDomain);
        } catch (OrganizationManagementException | UserStoreException e) {
            throw new ClaimMetadataException(ERROR_CODE_FAILED_TO_RESOLVE_TENANT_ID_DURING_HIERARCHICAL_AGGREGATION.getCode(), String.format(
                    ERROR_CODE_FAILED_TO_RESOLVE_TENANT_ID_DURING_HIERARCHICAL_AGGREGATION.getMessage(), orgId), e);
        }
    }

    /**
     * Checks whether a given tenant is an organization, i.e., whether it is a child of a root organization.
     *
     * @param tenantId The id of the tenant to be checked,
     * @return true if the tenant is an organization, false otherwise.
     * @throws ClaimMetadataException If an error occurs when checking whether the tenant is an organization.
     */
    private boolean isOrganization(int tenantId) throws ClaimMetadataException {

        try {
            return OrganizationManagementUtil.isOrganization(tenantId);
        } catch (OrganizationManagementException e) {
            throw new ClaimMetadataException(ERROR_CODE_FAILURE_IN_CHECKING_IS_TENANT_AN_ORGANIZATION.getCode(),
                    String.format(ERROR_CODE_FAILURE_IN_CHECKING_IS_TENANT_AN_ORGANIZATION.getMessage(), tenantId), e);
        }
    }

    /**
     * Gets the organization id corresponding to a given tenant id.
     *
     * @param tenantDomain The domain of the tenant for which the organization id needs to be retrieved.
     * @return The organization id of the given tenant.
     * @throws ClaimMetadataException If an error occurs when resolving the organization id.
     */
    private String getOrganizationId(String tenantDomain, int tenantId) throws ClaimMetadataException {

        try {
            return IdentityClaimManagementServiceDataHolder.getInstance()
                    .getOrganizationManager().resolveOrganizationId(tenantDomain);
        } catch (OrganizationManagementException e) {
            throw new ClaimMetadataException(ERROR_CODE_FAILED_TO_RESOLVE_ORGANIZATION_ID.getCode(), String.format(
                    ERROR_CODE_FAILED_TO_RESOLVE_ORGANIZATION_ID.getMessage(), tenantId, tenantDomain), e);
        }
    }

    /**
     * Checks whether to resolve the claims for the hierarchical inheritance model.
     *
     * @param tenantDomain The domain of the tenant.
     * @return true if hierarchical inheritance is enabled, false otherwise.
     */
    private boolean resolveWithHierarchicalMode(String tenantDomain, int tenantId) throws ClaimMetadataException {
        
        try {
            return Utils.isClaimAndOIDCScopeInheritanceEnabled(tenantDomain);
        } catch (OrganizationManagementException e) {
            if (isOrganization(tenantId)) {
                throw new ClaimMetadataException(ERROR_CODE_FAILED_TO_RESOLVE_ORGANIZATION_ID.getCode(),
                        String.format(ERROR_CODE_FAILED_TO_RESOLVE_ORGANIZATION_ID.getMessage(), tenantId,
                                tenantDomain));
            }
            /*
            * If it is not a child organization, i.e., if it is a root organization, hierarchical mode is essentially
            * just the additional DAO layer caching to help with future merging operations of child organizations.
            * Therefore, for instances such as listing the root organizations, where one root organization might
            * require resolving another root organization's tenant, we can simply proceed without the caching.
            */
            return false;
        }
    }
}
