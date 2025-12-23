/*
 * Copyright (c) 2016-2025, WSO2 LLC. (http://www.wso2.com).
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

import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.annotation.bundle.Capability;
import org.wso2.carbon.identity.claim.metadata.mgt.exception.ClaimMetadataClientException;
import org.wso2.carbon.identity.claim.metadata.mgt.exception.ClaimMetadataException;
import org.wso2.carbon.identity.claim.metadata.mgt.exception.ClaimMetadataServerException;
import org.wso2.carbon.identity.claim.metadata.mgt.internal.ReadOnlyClaimMetadataManager;
import org.wso2.carbon.identity.claim.metadata.mgt.internal.ReadWriteClaimMetadataManager;
import org.wso2.carbon.identity.claim.metadata.mgt.internal.IdentityClaimManagementServiceComponent;
import org.wso2.carbon.identity.claim.metadata.mgt.internal.IdentityClaimManagementServiceDataHolder;
import org.wso2.carbon.identity.claim.metadata.mgt.listener.ClaimMetadataMgtListener;
import org.wso2.carbon.identity.claim.metadata.mgt.model.AttributeMapping;
import org.wso2.carbon.identity.claim.metadata.mgt.model.Claim;
import org.wso2.carbon.identity.claim.metadata.mgt.model.ClaimDialect;
import org.wso2.carbon.identity.claim.metadata.mgt.model.ExternalClaim;
import org.wso2.carbon.identity.claim.metadata.mgt.model.LocalClaim;
import org.wso2.carbon.identity.claim.metadata.mgt.util.ClaimConstants;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;
import org.wso2.carbon.identity.core.util.IdentityUtil;
import org.wso2.carbon.user.api.UserRealm;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.core.UserCoreConstants;
import org.wso2.carbon.user.core.UserStoreManager;
import org.wso2.carbon.user.core.claim.inmemory.ClaimConfig;
import org.wso2.carbon.user.core.common.AbstractUserStoreManager;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static org.wso2.carbon.identity.claim.metadata.mgt.util.ClaimConstants.EXCLUDED_USER_STORES_PROPERTY;
import static org.wso2.carbon.identity.claim.metadata.mgt.util.ClaimConstants.ErrorMessage.ERROR_CODE_CANNOT_ADD_TO_EXTERNAL_DIALECT;
import static org.wso2.carbon.identity.claim.metadata.mgt.util.ClaimConstants.ErrorMessage.ERROR_CODE_CANNOT_EXCLUDE_USER_STORE;
import static org.wso2.carbon.identity.claim.metadata.mgt.util.ClaimConstants.ErrorMessage.ERROR_CODE_CLAIM_LENGTH_LIMIT;
import static org.wso2.carbon.identity.claim.metadata.mgt.util.ClaimConstants.ErrorMessage.ERROR_CODE_CLAIM_PROPERTY_CHAR_LIMIT_EXCEED;
import static org.wso2.carbon.identity.claim.metadata.mgt.util.ClaimConstants.ErrorMessage.ERROR_CODE_EMPTY_CLAIM_DIALECT;
import static org.wso2.carbon.identity.claim.metadata.mgt.util.ClaimConstants.ErrorMessage.ERROR_CODE_EMPTY_EXTERNAL_CLAIM_URI;
import static org.wso2.carbon.identity.claim.metadata.mgt.util.ClaimConstants.ErrorMessage.ERROR_CODE_EMPTY_EXTERNAL_DIALECT_URI;
import static org.wso2.carbon.identity.claim.metadata.mgt.util.ClaimConstants.ErrorMessage.ERROR_CODE_EMPTY_LOCAL_CLAIM_URI;
import static org.wso2.carbon.identity.claim.metadata.mgt.util.ClaimConstants.ErrorMessage.ERROR_CODE_EMPTY_MAPPED_ATTRIBUTES_IN_LOCAL_CLAIM;
import static org.wso2.carbon.identity.claim.metadata.mgt.util.ClaimConstants.ErrorMessage.ERROR_CODE_EMPTY_TENANT_DOMAIN;
import static org.wso2.carbon.identity.claim.metadata.mgt.util.ClaimConstants.ErrorMessage.ERROR_CODE_EXISTING_CLAIM_DIALECT;
import static org.wso2.carbon.identity.claim.metadata.mgt.util.ClaimConstants.ErrorMessage.ERROR_CODE_EXISTING_EXTERNAL_CLAIM_URI;
import static org.wso2.carbon.identity.claim.metadata.mgt.util.ClaimConstants.ErrorMessage.ERROR_CODE_EXISTING_LOCAL_CLAIM_MAPPING;
import static org.wso2.carbon.identity.claim.metadata.mgt.util.ClaimConstants.ErrorMessage.ERROR_CODE_EXISTING_LOCAL_CLAIM_URI;
import static org.wso2.carbon.identity.claim.metadata.mgt.util.ClaimConstants.ErrorMessage.ERROR_CODE_CLAIM_MUST_BE_MANAGED_IN_USER_STORE;
import static org.wso2.carbon.identity.claim.metadata.mgt.util.ClaimConstants.ErrorMessage.ERROR_CODE_INVALID_ATTRIBUTE_PROFILE;
import static org.wso2.carbon.identity.claim.metadata.mgt.util.ClaimConstants.ErrorMessage.ERROR_CODE_INVALID_EXTERNAL_CLAIM_DIALECT_URI;
import static org.wso2.carbon.identity.claim.metadata.mgt.util.ClaimConstants.ErrorMessage.ERROR_CODE_INVALID_EXTERNAL_CLAIM_URI;
import static org.wso2.carbon.identity.claim.metadata.mgt.util.ClaimConstants.ErrorMessage.ERROR_CODE_INVALID_EXTERNAL_CLAIM_DIALECT;
import static org.wso2.carbon.identity.claim.metadata.mgt.util.ClaimConstants.ErrorMessage.ERROR_CODE_INVALID_SHARED_PROFILE_VALUE_RESOLVING_METHOD;
import static org.wso2.carbon.identity.claim.metadata.mgt.util.ClaimConstants.ErrorMessage.ERROR_CODE_INVALID_TENANT_DOMAIN;
import static org.wso2.carbon.identity.claim.metadata.mgt.util.ClaimConstants.ErrorMessage.ERROR_CODE_LOCAL_CLAIM_HAS_MAPPED_EXTERNAL_CLAIM;
import static org.wso2.carbon.identity.claim.metadata.mgt.util.ClaimConstants.ErrorMessage.ERROR_CODE_MAPPED_TO_EMPTY_LOCAL_CLAIM_URI;
import static org.wso2.carbon.identity.claim.metadata.mgt.util.ClaimConstants.ErrorMessage.ERROR_CODE_MAPPED_TO_INVALID_LOCAL_CLAIM_URI;
import static org.wso2.carbon.identity.claim.metadata.mgt.util.ClaimConstants.ErrorMessage.ERROR_CODE_NON_EXISTING_EXTERNAL_CLAIM_URI;
import static org.wso2.carbon.identity.claim.metadata.mgt.util.ClaimConstants.ErrorMessage.ERROR_CODE_NON_EXISTING_LOCAL_CLAIM;
import static org.wso2.carbon.identity.claim.metadata.mgt.util.ClaimConstants.ErrorMessage.ERROR_CODE_NON_EXISTING_LOCAL_CLAIM_URI;
import static org.wso2.carbon.identity.claim.metadata.mgt.util.ClaimConstants.ErrorMessage.ERROR_CODE_NO_SHARED_PROFILE_VALUE_RESOLVING_METHOD_CHANGE_FOR_SYSTEM_CLAIM;
import static org.wso2.carbon.identity.claim.metadata.mgt.util.ClaimConstants.ErrorMessage.ERROR_CODE_SERVER_ERROR_GETTING_USER_STORE_MANAGER;
import static org.wso2.carbon.identity.claim.metadata.mgt.util.ClaimConstants.SUB_ATTRIBUTES_PROPERTY;
import static org.wso2.carbon.identity.claim.metadata.mgt.util.ClaimConstants.MANAGED_IN_USER_STORE_PROPERTY;
import static org.wso2.carbon.identity.claim.metadata.mgt.util.ClaimMetadataUtils.containsIgnoreCase;
import static org.wso2.carbon.identity.claim.metadata.mgt.util.ClaimMetadataUtils.getAllowedClaimProfiles;
import static org.wso2.carbon.identity.claim.metadata.mgt.util.ClaimMetadataUtils.getServerLevelClaimUniquenessScope;
import static org.wso2.carbon.identity.claim.metadata.mgt.util.ClaimMetadataUtils.isIdentityClaim;
import static org.wso2.carbon.identity.claim.metadata.mgt.util.ClaimMetadataUtils.isUserStoreBasedIdentityDataStore;

/**
 * Default implementation of {@link org.wso2.carbon.identity.claim.metadata.mgt.ClaimMetadataManagementService}
 * interface.
 */
@Capability(
        namespace = "osgi.service",
        attribute = {
                "objectClass=org.wso2.carbon.identity.claim.metadata.mgt.ClaimMetadataManagementService",
                "service.scope=singleton"
        }
)
public class ClaimMetadataManagementServiceImpl implements ClaimMetadataManagementService {

    private static final Log log = LogFactory.getLog(ClaimMetadataManagementServiceImpl.class);

    private final ReadWriteClaimMetadataManager unifiedClaimMetadataManager =
            new CacheBackedUnifiedClaimMetadataManager();
    private final ReadOnlyClaimMetadataManager systemDefaultClaimMetadataManager =
            new SystemDefaultClaimMetadataManager();
    private static final int MAX_CLAIM_PROPERTY_LENGTH = 255;
    private static final int MAX_CLAIM_PROPERTY_LENGTH_LIMIT = 1024;
    private static final int MIN_CLAIM_PROPERTY_LENGTH_LIMIT = 0;
    private static final String STORE_IDENTITY_CLAIMS = "StoreIdentityClaims";

    @Override
    public List<ClaimDialect> getClaimDialects(String tenantDomain) throws ClaimMetadataException {

        // TODO : validate tenant domain?
        int tenantId = IdentityTenantUtil.getTenantId(tenantDomain);

        // Add listener

        List<ClaimDialect> claimDialects = this.unifiedClaimMetadataManager.getClaimDialects(tenantId);

        // Add listener

        return claimDialects;
    }

    @Override
    public void addClaimDialect(ClaimDialect claimDialect, String tenantDomain) throws ClaimMetadataException {

        if (claimDialect == null || StringUtils.isBlank(claimDialect.getClaimDialectURI())) {
            throw new ClaimMetadataClientException(ERROR_CODE_EMPTY_CLAIM_DIALECT);
        }
        if (StringUtils.isBlank(tenantDomain)) {
            throw new ClaimMetadataClientException(ERROR_CODE_EMPTY_TENANT_DOMAIN);
        }
        int tenantId = IdentityTenantUtil.getTenantId(tenantDomain);
        if (tenantId == MultitenantConstants.INVALID_TENANT_ID) {
            throw new ClaimMetadataClientException(ERROR_CODE_INVALID_TENANT_DOMAIN.getCode(),
                    String.format(ERROR_CODE_INVALID_TENANT_DOMAIN.getMessage(), tenantDomain));
        }

        List<ClaimDialect> claimDialects = this.unifiedClaimMetadataManager.getClaimDialects(tenantId);
        Set<String> claimDialectUris = claimDialects.stream().map(ClaimDialect::getClaimDialectURI).
                collect(Collectors.toSet());

        if (claimDialectUris.contains(claimDialect.getClaimDialectURI())) {
            throw new ClaimMetadataClientException(ERROR_CODE_EXISTING_CLAIM_DIALECT.getCode(),
                    String.format(ERROR_CODE_EXISTING_CLAIM_DIALECT.getMessage(), claimDialect.getClaimDialectURI()));
        }

        ClaimMetadataEventPublisherProxy.getInstance().publishPreAddClaimDialect(tenantId, claimDialect);

        this.unifiedClaimMetadataManager.addClaimDialect(claimDialect, tenantId);

        ClaimMetadataEventPublisherProxy.getInstance().publishPostAddClaimDialect(tenantId, claimDialect);

    }

    @Override
    public void renameClaimDialect(ClaimDialect oldClaimDialect, ClaimDialect newClaimDialect, String tenantDomain)
            throws ClaimMetadataException {

        if (oldClaimDialect == null || StringUtils.isBlank(oldClaimDialect.getClaimDialectURI())
                || newClaimDialect == null || StringUtils.isBlank(newClaimDialect.getClaimDialectURI())) {
            throw new ClaimMetadataClientException(ERROR_CODE_EMPTY_CLAIM_DIALECT);
        }

        // TODO : Validate oldClaimDialectURI is valid????

        // TODO : validate tenant domain?
        int tenantId = IdentityTenantUtil.getTenantId(tenantDomain);

        boolean isRenamedDialectAlreadyTaken = isExistingClaimDialect(newClaimDialect.getClaimDialectURI(), tenantId);
        if (isRenamedDialectAlreadyTaken) {
            throw new ClaimMetadataClientException(ERROR_CODE_EXISTING_CLAIM_DIALECT.getCode(),
                    String.format(ERROR_CODE_EXISTING_CLAIM_DIALECT.getMessage(), newClaimDialect.getClaimDialectURI()));
        }

        ClaimMetadataEventPublisherProxy.getInstance().publishPreUpdateClaimDialect(tenantId, oldClaimDialect, newClaimDialect);

        this.unifiedClaimMetadataManager.renameClaimDialect(oldClaimDialect, newClaimDialect, tenantId);

        ClaimMetadataEventPublisherProxy.getInstance().publishPostUpdateClaimDialect(tenantId, oldClaimDialect, newClaimDialect);

    }

    @Override
    public void removeClaimDialect(ClaimDialect claimDialect, String tenantDomain) throws ClaimMetadataException {

        if (claimDialect == null || StringUtils.isBlank(claimDialect.getClaimDialectURI())) {
            throw new ClaimMetadataClientException(ERROR_CODE_EMPTY_CLAIM_DIALECT.getCode(),
                    "Claim dialect URI cannot be empty");
        }

        // TODO : validate claim dialect already exists?

        // TODO : validate tenant domain?
        int tenantId = IdentityTenantUtil.getTenantId(tenantDomain);

        ClaimMetadataEventPublisherProxy.getInstance().publishPreDeleteClaimDialect(tenantId, claimDialect);

        this.unifiedClaimMetadataManager.removeClaimDialect(claimDialect, tenantId);
        ClaimMetadataEventPublisherProxy.getInstance().publishPostDeleteClaimDialect(tenantId, claimDialect);

    }

    /**
     * Retrieves the list of local claims supported by the specified profile for a given tenant.
     *
     * @param tenantDomain The tenant domain.
     * @param profileName  The profile name.
     * @return A list of local claims supported by the profile for the given tenant.
     * @throws ClaimMetadataException If an error occurs while retrieving claims.
     */
    @Override
    public List<LocalClaim> getSupportedLocalClaimsForProfile(String tenantDomain, String profileName)
            throws ClaimMetadataException {

        // Validate profile name.
        if (!getAllowedClaimProfiles().contains(profileName)) {
            throw new ClaimMetadataClientException(ERROR_CODE_INVALID_ATTRIBUTE_PROFILE);
        }

        String profileSupportedProperty =
                buildAttributeProfilePropertyKey(profileName, ClaimConstants.SUPPORTED_BY_DEFAULT_PROPERTY);
        return getLocalClaims(tenantDomain).stream().filter(localClaim ->
                isClaimSupportedByProfile(localClaim, profileName, profileSupportedProperty))
                .map(localClaim -> updateClaimPropertiesForProfile(localClaim, profileName))
                .collect(Collectors.toList());
    }

    /**
     * Determines if the given claim is supported by the specified profile.
     * If the profile-specific value exists, it takes precedence over the global value.
     *
     * @param claim                        The local claim to check.
     * @param profileName                  The profile name to evaluate against.
     * @param profileSupportedByDefaultProperty The profile-specific property key.
     * @return True if the claim is supported by the profile; False otherwise.
     */
    private boolean isClaimSupportedByProfile(LocalClaim claim, String profileName,
                                              String profileSupportedByDefaultProperty) {

        Map<String, String> claimProperties = claim.getClaimProperties();
        if (claimProperties.containsKey(profileSupportedByDefaultProperty)) {
            return Boolean.parseBoolean(claimProperties.get(profileSupportedByDefaultProperty));
        }
        return Boolean.parseBoolean(claimProperties.get(ClaimConstants.SUPPORTED_BY_DEFAULT_PROPERTY));
    }

    /**
     * Updates the global claim properties with profile-specific properties, if available.
     *
     * @param claim       The local claim to update.
     * @param profileName The profile name to apply the specific properties from.
     */
    private LocalClaim updateClaimPropertiesForProfile(LocalClaim claim, String profileName) {

        LocalClaim claimCopy = copyLocalClaim(claim);
        Map<String, String> claimProperties = claimCopy.getClaimProperties();
        for (String propertyKey: ClaimConstants.ALLOWED_PROFILE_PROPERTY_KEYS) {
            String profilePropertyKey = buildAttributeProfilePropertyKey(profileName, propertyKey);
            String profilePropertyValue = claimProperties.get(profilePropertyKey);

            if (StringUtils.isNotBlank(profilePropertyValue)) {
                claimProperties.put(propertyKey, profilePropertyValue);
            }
        }
        return claimCopy;
    }

    /**
     * Creates a deep copy of the given LocalClaim object.
     *
     * @param originalClaim The original LocalClaim to copy.
     * @return A deep copy of the original LocalClaim.
     */
    private LocalClaim copyLocalClaim(LocalClaim originalClaim) {

        Map<String, String> claimPropertiesCopy = originalClaim.getClaimProperties() != null
                ? new HashMap<>(originalClaim.getClaimProperties())
                : new HashMap<>();
        List<AttributeMapping> mappedAttributesCopy = originalClaim.getMappedAttributes() != null
                ? new ArrayList<>(originalClaim.getMappedAttributes())
                : new ArrayList<>();

        return new LocalClaim(originalClaim.getClaimURI(), mappedAttributesCopy, claimPropertiesCopy);
    }

    @Override
    public List<LocalClaim> getLocalClaims(String tenantDomain) throws ClaimMetadataException {

        int tenantId = IdentityTenantUtil.getTenantId(tenantDomain);

        // Add listener

        List<LocalClaim> localClaims = this.unifiedClaimMetadataManager.getLocalClaims(tenantId);

        // Add listener

        boolean isGroupRoleSeparationEnabled = IdentityUtil.isGroupsVsRolesSeparationImprovementsEnabled();
        boolean isShowRoleClaimOnGroupRoleSeparation = IdentityUtil.isShowLegacyRoleClaimOnGroupRoleSeparationEnabled();
        boolean userStoreBasedIdentityDataStore = isUserStoreBasedIdentityDataStore();
        List<LocalClaim> filteredLocalClaims = new ArrayList<>(localClaims.size());

        for (LocalClaim claim : localClaims) {
            if (isGroupRoleSeparationEnabled && !isShowRoleClaimOnGroupRoleSeparation &&
                    UserCoreConstants.ROLE_CLAIM.equals(claim.getClaimURI())) {
                continue;
            }
            // Add `UniquenessScope` property for claims that only have legacy `isUnique` property.
            // `UniquenessScope` is the current property used to configure claim-wise uniqueness validation scope,
            // while `isUnique` is maintained for backward compatibility.
            Map<String, String> properties = claim.getClaimProperties();
            if (properties.containsKey(ClaimConstants.IS_UNIQUE_CLAIM_PROPERTY) &&
                    !properties.containsKey(ClaimConstants.CLAIM_UNIQUENESS_SCOPE_PROPERTY)) {
                updateScopeFromIsUnique(properties,
                        properties.get(ClaimConstants.IS_UNIQUE_CLAIM_PROPERTY));
            }

            setManagedInUserStoreProperty(claim, userStoreBasedIdentityDataStore);
            filteredLocalClaims.add(claim);
        }
        return filteredLocalClaims;
    }

    @Override
    public Optional<LocalClaim> getLocalClaim(String localClaimURI, String tenantDomain) throws ClaimMetadataException {

        int tenantId = IdentityTenantUtil.getTenantId(tenantDomain);
        return this.unifiedClaimMetadataManager.getLocalClaim(localClaimURI, tenantId);
    }

    @Override
    public Optional<LocalClaim> getLocalClaim(String localClaimURI, String tenantDomain,
                                               boolean includeManagedInUserStoreInfo) throws ClaimMetadataException {

        Optional<LocalClaim> localClaim = getLocalClaim(localClaimURI, tenantDomain);
        if (!includeManagedInUserStoreInfo || !localClaim.isPresent()) {
            return localClaim;
        }

        LocalClaim claimCopy = copyLocalClaim(localClaim.get());
        setManagedInUserStorePropertyAndExcludedUserStoresProperty(claimCopy, tenantDomain);
        return Optional.of(claimCopy);
    }

    @Override
    public void addLocalClaim(LocalClaim localClaim, String tenantDomain) throws ClaimMetadataException {

        if (localClaim == null || StringUtils.isBlank(localClaim.getClaimURI())) {
            throw new ClaimMetadataClientException(ERROR_CODE_EMPTY_LOCAL_CLAIM_URI);
        } else if (localClaim.getMappedAttributes().isEmpty()) {
            throw new ClaimMetadataClientException(ERROR_CODE_EMPTY_MAPPED_ATTRIBUTES_IN_LOCAL_CLAIM.getCode(),
                    String.format(ERROR_CODE_EMPTY_MAPPED_ATTRIBUTES_IN_LOCAL_CLAIM.getMessage(), localClaim
                            .getClaimDialectURI(), localClaim.getClaimURI()));
        }
        validateClaimProperties(localClaim.getClaimProperties());

        // TODO : validate tenant domain?
        int tenantId = IdentityTenantUtil.getTenantId(tenantDomain);

        if (isExistingLocalClaim(localClaim.getClaimURI(), tenantId)) {
            throw new ClaimMetadataClientException(ERROR_CODE_EXISTING_LOCAL_CLAIM_URI.getCode(),
                    String.format(ERROR_CODE_EXISTING_LOCAL_CLAIM_URI.getMessage(), localClaim.getClaimURI()));
        }

        validateAndSyncUniquenessClaimProperties(localClaim.getClaimProperties(), null);
        validateAndSyncAttributeProfileProperties(localClaim.getClaimProperties());

        validateSharedProfileValueResolvingMethodValue(localClaim);
        validateAndSyncClaimStoreSettings(localClaim, tenantDomain);

        ClaimMetadataEventPublisherProxy.getInstance().publishPreAddLocalClaim(tenantId, localClaim);

        this.unifiedClaimMetadataManager.addLocalClaim(localClaim, tenantId);

        ClaimMetadataEventPublisherProxy.getInstance().publishPostAddLocalClaim(tenantId, localClaim);
    }

    @Override
    public void updateLocalClaim(LocalClaim localClaim, String tenantDomain) throws ClaimMetadataException {

        if (localClaim == null || StringUtils.isBlank(localClaim.getClaimURI())) {
            throw new ClaimMetadataClientException(ERROR_CODE_EMPTY_LOCAL_CLAIM_URI);
        } else if (localClaim.getMappedAttributes().isEmpty()) {
            throw new ClaimMetadataClientException(ERROR_CODE_EMPTY_MAPPED_ATTRIBUTES_IN_LOCAL_CLAIM.getCode(),
                    String.format(ERROR_CODE_EMPTY_MAPPED_ATTRIBUTES_IN_LOCAL_CLAIM.getMessage(), localClaim
                            .getClaimDialectURI(), localClaim.getClaimURI()));
        }
        validateClaimProperties(localClaim.getClaimProperties());

        // TODO : validate claim URI already exists?

        // TODO : validate tenant domain?
        int tenantId = IdentityTenantUtil.getTenantId(tenantDomain);

        Optional<LocalClaim> existingLocalClaim = getLocalClaim(localClaim.getClaimURI(), tenantDomain);
        if (!existingLocalClaim.isPresent()) {
            throw new ClaimMetadataClientException(ERROR_CODE_NON_EXISTING_LOCAL_CLAIM.getCode(),
                    String.format(ERROR_CODE_NON_EXISTING_LOCAL_CLAIM.getMessage(), localClaim.getClaimURI()));
        }

        validateAndSyncUniquenessClaimProperties(localClaim.getClaimProperties(),
                existingLocalClaim.get().getClaimProperties());
        validateAndSyncAttributeProfileProperties(localClaim.getClaimProperties());
        validateAndSyncClaimStoreSettings(localClaim, tenantDomain);

        validateSharedProfileValueResolvingMethodChange(localClaim, existingLocalClaim.get(), tenantId);

        ClaimMetadataEventPublisherProxy.getInstance().publishPreUpdateLocalClaim(tenantId, localClaim);

        this.unifiedClaimMetadataManager.updateLocalClaim(localClaim, tenantId);

        ClaimMetadataEventPublisherProxy.getInstance().publishPostUpdateLocalClaim(tenantId, localClaim);
    }

    @Override
    public void updateLocalClaimMappings(List<LocalClaim> localClaimList, String tenantDomain, String userStoreDomain)
            throws ClaimMetadataException {

        int tenantId = IdentityTenantUtil.getTenantId(tenantDomain);
        ClaimMetadataEventPublisherProxy claimMetadataEventPublisherProxy =
                ClaimMetadataEventPublisherProxy.getInstance();
        for (LocalClaim localClaim : localClaimList) {
            claimMetadataEventPublisherProxy.publishPreUpdateLocalClaim(tenantId, localClaim);
        }

        this.unifiedClaimMetadataManager.updateLocalClaimMappings(localClaimList, tenantId, userStoreDomain);

        for (LocalClaim localClaim : localClaimList) {
            claimMetadataEventPublisherProxy.publishPostUpdateLocalClaim(tenantId, localClaim);
        }
    }

    @Override
    public void removeLocalClaim(String localClaimURI, String tenantDomain) throws ClaimMetadataException {

        if (StringUtils.isBlank(localClaimURI)) {
            throw new ClaimMetadataClientException(ERROR_CODE_EMPTY_LOCAL_CLAIM_URI);
        }

        // TODO : validate claim URI already exists?

        // TODO : validate tenant domain?
        int tenantId = IdentityTenantUtil.getTenantId(tenantDomain);

        boolean isMappedLocalClaim = this.unifiedClaimMetadataManager.isMappedLocalClaim(localClaimURI, tenantId);
        if (isMappedLocalClaim) {
            throw new ClaimMetadataClientException(ERROR_CODE_LOCAL_CLAIM_HAS_MAPPED_EXTERNAL_CLAIM.getCode(),
                    String.format(ERROR_CODE_LOCAL_CLAIM_HAS_MAPPED_EXTERNAL_CLAIM.getMessage(), localClaimURI));
        }

        ClaimMetadataEventPublisherProxy.getInstance().publishPreDeleteLocalClaim(tenantId, localClaimURI);
        Collection<ClaimMetadataMgtListener> listeners =
                IdentityClaimManagementServiceComponent.getClaimMetadataMgtListeners();
        for (ClaimMetadataMgtListener listener : listeners) {
            if (listener.isEnable() && !listener.doPreDeleteClaim(localClaimURI, tenantDomain)) {
                return;
            }
        }

        this.unifiedClaimMetadataManager.removeLocalClaim(localClaimURI, tenantId);

        ClaimMetadataEventPublisherProxy.getInstance().publishPostDeleteLocalClaim(tenantId, localClaimURI);
        for (ClaimMetadataMgtListener listener : listeners) {
            if (listener.isEnable() && !listener.doPostDeleteClaim(localClaimURI, tenantDomain)) {
                return;
            }
        }
    }

    @Override
    public List<ExternalClaim> getExternalClaims(String externalClaimDialectURI, String tenantDomain) throws
            ClaimMetadataException {

        if (StringUtils.isBlank(externalClaimDialectURI)) {
            throw new ClaimMetadataClientException(ERROR_CODE_EMPTY_EXTERNAL_CLAIM_URI);
        }

        if (ClaimConstants.LOCAL_CLAIM_DIALECT_URI.equalsIgnoreCase(externalClaimDialectURI)) {
            throw new ClaimMetadataClientException(ERROR_CODE_INVALID_EXTERNAL_CLAIM_DIALECT);
        }

        int tenantId = IdentityTenantUtil.getTenantId(tenantDomain);

        // Add listener

        List<ExternalClaim> externalClaims = this.unifiedClaimMetadataManager.getExternalClaims(
                externalClaimDialectURI, tenantId);

        // Add listener

        return externalClaims;
    }

    @Override
    public void addExternalClaim(ExternalClaim externalClaim, String tenantDomain) throws ClaimMetadataException {

        if (externalClaim == null || StringUtils.isBlank(externalClaim.getClaimURI())) {
            throw new ClaimMetadataClientException(ERROR_CODE_EMPTY_EXTERNAL_CLAIM_URI);
        }

        if (StringUtils.isBlank(externalClaim.getClaimDialectURI())) {
            throw new ClaimMetadataClientException(ERROR_CODE_EMPTY_EXTERNAL_DIALECT_URI);
        }

        if (StringUtils.isBlank(externalClaim.getMappedLocalClaim())) {
            throw new ClaimMetadataClientException(ERROR_CODE_MAPPED_TO_EMPTY_LOCAL_CLAIM_URI);
        }

        if (ClaimConstants.LOCAL_CLAIM_DIALECT_URI.equalsIgnoreCase(externalClaim.getClaimDialectURI())) {
            throw new ClaimMetadataClientException(ERROR_CODE_INVALID_EXTERNAL_CLAIM_DIALECT);
        }

        ClaimConfig claimConfig = IdentityClaimManagementServiceDataHolder.getInstance().getClaimConfig();
        String claimURIRegex = null;
        if (claimConfig != null) {
            claimURIRegex = claimConfig.getClaimUriRegex(externalClaim.getClaimDialectURI());
        }

        if (claimURIRegex != null && !externalClaim.getClaimURI().matches(claimURIRegex)) {
            throw new ClaimMetadataClientException(ERROR_CODE_INVALID_EXTERNAL_CLAIM_URI);
        }

        // TODO : validate tenant domain?
        int tenantId = IdentityTenantUtil.getTenantId(tenantDomain);

        if (!isExistingClaimDialect(externalClaim.getClaimDialectURI(), tenantId)) {
            throw new ClaimMetadataClientException(ERROR_CODE_INVALID_EXTERNAL_CLAIM_DIALECT_URI.getCode(),
                    String.format(ERROR_CODE_INVALID_EXTERNAL_CLAIM_DIALECT_URI.getMessage(),
                            externalClaim.getClaimDialectURI()));
        }

        if (isExistingExternalClaim(externalClaim.getClaimDialectURI(), externalClaim.getClaimURI(), tenantId)) {
            throw new ClaimMetadataClientException(ERROR_CODE_EXISTING_EXTERNAL_CLAIM_URI.getCode(),
                    String.format(ERROR_CODE_EXISTING_EXTERNAL_CLAIM_URI.getMessage(), externalClaim.getClaimURI(),
                            externalClaim.getClaimDialectURI()));
        }

        if (!isExistingLocalClaim(externalClaim.getMappedLocalClaim(), tenantId)) {
            throw new ClaimMetadataClientException(ERROR_CODE_MAPPED_TO_INVALID_LOCAL_CLAIM_URI.getCode(),
                    String.format(ERROR_CODE_MAPPED_TO_INVALID_LOCAL_CLAIM_URI.getMessage(),
                            externalClaim.getMappedLocalClaim(), ClaimConstants.LOCAL_CLAIM_DIALECT_URI));
        }

        boolean isLocalClaimAlreadyMapped =
                this.unifiedClaimMetadataManager.isLocalClaimMappedWithinDialect(externalClaim.getMappedLocalClaim(),
                        externalClaim.getClaimDialectURI(), tenantId);

        if (isLocalClaimAlreadyMapped) {
            throw new ClaimMetadataClientException((ERROR_CODE_EXISTING_LOCAL_CLAIM_MAPPING.getCode()),
                    String.format(ERROR_CODE_EXISTING_LOCAL_CLAIM_MAPPING.getMessage(),
                            externalClaim.getMappedLocalClaim(), externalClaim.getClaimDialectURI()));
        }

        try {
            ClaimMetadataEventPublisherProxy.getInstance().publishPreAddExternalClaim(tenantId, externalClaim);
            if (MapUtils.isNotEmpty(IdentityUtil.threadLocalProperties.get()) &&
                    Boolean.TRUE.equals(IdentityUtil.threadLocalProperties.get()
                            .get(ClaimConstants.EXTERNAL_CLAIM_ADDITION_NOT_ALLOWED_FOR_DIALECT))) {
                throw new ClaimMetadataClientException(ERROR_CODE_CANNOT_ADD_TO_EXTERNAL_DIALECT.getCode(),
                        String.format(ERROR_CODE_CANNOT_ADD_TO_EXTERNAL_DIALECT.getMessage(),
                                externalClaim.getClaimDialectURI()));
            }
        } finally {
            IdentityUtil.threadLocalProperties.get()
                    .remove(ClaimConstants.EXTERNAL_CLAIM_ADDITION_NOT_ALLOWED_FOR_DIALECT);
        }

        this.unifiedClaimMetadataManager.addExternalClaim(externalClaim, tenantId);

        ClaimMetadataEventPublisherProxy.getInstance().publishPostAddExternalClaim(tenantId, externalClaim);
    }

    @Override
    public void updateExternalClaim(ExternalClaim externalClaim, String tenantDomain) throws
            ClaimMetadataException {

        if (externalClaim == null || StringUtils.isBlank(externalClaim.getClaimURI())) {
            throw new ClaimMetadataClientException(ERROR_CODE_EMPTY_EXTERNAL_CLAIM_URI);
        }

        if (StringUtils.isBlank(externalClaim.getClaimDialectURI())) {
            throw new ClaimMetadataClientException(ERROR_CODE_EMPTY_EXTERNAL_DIALECT_URI);
        }

        if (StringUtils.isBlank(externalClaim.getMappedLocalClaim())) {
            throw new ClaimMetadataClientException(ERROR_CODE_MAPPED_TO_EMPTY_LOCAL_CLAIM_URI);
        }

        if (ClaimConstants.LOCAL_CLAIM_DIALECT_URI.equalsIgnoreCase(externalClaim.getClaimDialectURI())) {
            throw new ClaimMetadataClientException(ERROR_CODE_INVALID_EXTERNAL_CLAIM_DIALECT);
        }

        // TODO : validate claim URI already exists?

        // TODO : validate tenant domain?
        int tenantId = IdentityTenantUtil.getTenantId(tenantDomain);

        if (!isExistingClaimDialect(externalClaim.getClaimDialectURI(), tenantId)) {
            throw new ClaimMetadataClientException(ERROR_CODE_INVALID_EXTERNAL_CLAIM_DIALECT_URI.getCode(),
                    String.format(ERROR_CODE_INVALID_EXTERNAL_CLAIM_DIALECT_URI.getMessage(),
                            externalClaim.getClaimDialectURI()));
        }

        if (!isExistingExternalClaim(externalClaim.getClaimDialectURI(), externalClaim.getClaimURI(), tenantId)) {
            throw new ClaimMetadataClientException(ERROR_CODE_NON_EXISTING_EXTERNAL_CLAIM_URI.getCode(),
                    String.format(ERROR_CODE_NON_EXISTING_EXTERNAL_CLAIM_URI.getMessage(), externalClaim.getClaimURI(),
                            externalClaim.getClaimDialectURI()));
        }

        if (!isExistingLocalClaim(externalClaim.getMappedLocalClaim(), tenantId)) {
            throw new ClaimMetadataClientException(ERROR_CODE_NON_EXISTING_LOCAL_CLAIM.getCode(),
                    String.format(ERROR_CODE_NON_EXISTING_LOCAL_CLAIM.getMessage(), externalClaim.getMappedLocalClaim()));
        }

        boolean isLocalClaimAlreadyMapped = this.unifiedClaimMetadataManager.getMappedExternalClaims(
                externalClaim.getMappedLocalClaim(), tenantId).stream()
                .filter(claim -> claim.getClaimDialectURI().equals(externalClaim.getClaimDialectURI()))
                .anyMatch(claim -> !claim.getClaimURI().equals(externalClaim.getClaimURI()));

        if (isLocalClaimAlreadyMapped) {
            throw new ClaimMetadataClientException((ERROR_CODE_EXISTING_LOCAL_CLAIM_MAPPING.getCode()),
                    String.format(ERROR_CODE_EXISTING_LOCAL_CLAIM_MAPPING.getMessage(),
                            externalClaim.getMappedLocalClaim(), externalClaim.getClaimDialectURI()));
        }

        ClaimMetadataEventPublisherProxy.getInstance().publishPreUpdateExternalClaim(tenantId, externalClaim);

        this.unifiedClaimMetadataManager.updateExternalClaim(externalClaim, tenantId);

        ClaimMetadataEventPublisherProxy.getInstance().publishPostUpdateExternalClaim(tenantId, externalClaim);
    }

    @Override
    public void removeExternalClaim(String externalClaimDialectURI, String externalClaimURI, String tenantDomain)
            throws ClaimMetadataException {

        if (StringUtils.isBlank(externalClaimDialectURI)) {
            throw new ClaimMetadataClientException(ERROR_CODE_EMPTY_EXTERNAL_DIALECT_URI.getCode(),
                    "External claim dialect URI cannot be empty");
        }

        if (StringUtils.isBlank(externalClaimURI)) {
            throw new ClaimMetadataClientException(ERROR_CODE_EMPTY_EXTERNAL_CLAIM_URI);
        }

        if (ClaimConstants.LOCAL_CLAIM_DIALECT_URI.equalsIgnoreCase(externalClaimDialectURI)) {
            throw new ClaimMetadataClientException(ERROR_CODE_INVALID_EXTERNAL_CLAIM_DIALECT);
        }

        // TODO : validate claim URI already exists?

        // TODO : validate tenant domain?
        int tenantId = IdentityTenantUtil.getTenantId(tenantDomain);

        ClaimMetadataEventPublisherProxy.getInstance().publishPreDeleteExternalClaim(tenantId,
                externalClaimDialectURI, externalClaimURI);

        this.unifiedClaimMetadataManager.removeExternalClaim(externalClaimDialectURI, externalClaimURI, tenantId);

        ClaimMetadataEventPublisherProxy.getInstance().publishPostDeleteExternalClaim(tenantId,
                externalClaimDialectURI, externalClaimURI);
    }

    @Override
    public void removeClaimMappingAttributes(int tenantId, String userstoreDomain) throws ClaimMetadataException {

        if (StringUtils.isEmpty(userstoreDomain)) {
            throw new ClaimMetadataClientException(ERROR_CODE_EMPTY_TENANT_DOMAIN.getCode(),
                    ERROR_CODE_EMPTY_TENANT_DOMAIN.getMessage());
        }
        this.unifiedClaimMetadataManager.removeClaimMappingAttributes(tenantId, userstoreDomain);
    }

    /**
     * Remove all claims of a given tenant.
     *
     * @param tenantId The id of the tenant.
     * @throws ClaimMetadataException throws when an error occurs in removing claims.
     */
    @Override
    public void removeAllClaims(int tenantId) throws ClaimMetadataException {

        this.unifiedClaimMetadataManager.removeAllClaimDialects(tenantId);
    }

    @Override
    public String getMaskingRegexForLocalClaim(String localClaimURI, String tenantDomain) throws
            ClaimMetadataException {

        List<LocalClaim> localClaims = getLocalClaims(tenantDomain);

        for (LocalClaim localClaim : localClaims) {
            if (localClaim.getClaimURI().equals(localClaimURI)) {
                return StringEscapeUtils.unescapeXml(localClaim.getClaimProperty(ClaimConstants
                        .MASKING_REGULAR_EXPRESSION_PROPERTY));
            }
        }
        return null;
    }

    @Override
    public void validateClaimAttributeMapping(List<LocalClaim> localClaimList, String tenantDomain)
            throws ClaimMetadataException {

        for (LocalClaim localClaim : localClaimList) {
            if (localClaim == null || StringUtils.isBlank(localClaim.getClaimURI())) {
                throw new ClaimMetadataClientException(ERROR_CODE_EMPTY_LOCAL_CLAIM_URI);
            } else if (localClaim.getMappedAttributes().isEmpty()) {
                throw new ClaimMetadataClientException(ERROR_CODE_EMPTY_MAPPED_ATTRIBUTES_IN_LOCAL_CLAIM.getCode(),
                        String.format(ERROR_CODE_EMPTY_MAPPED_ATTRIBUTES_IN_LOCAL_CLAIM.getMessage(), localClaim
                                .getClaimDialectURI(), localClaim.getClaimURI()));
            }
            if (!isExistingLocalClaim(localClaim.getClaimURI(), IdentityTenantUtil.getTenantId(tenantDomain))) {
                throw new ClaimMetadataClientException(ERROR_CODE_NON_EXISTING_LOCAL_CLAIM_URI.getCode(),
                        String.format(ERROR_CODE_NON_EXISTING_LOCAL_CLAIM_URI.getMessage(), localClaim.getClaimURI()));
            }
        }
    }

    /**
     * Check whether the properties are valid.
     *
     * @param claimProperties List of claim properties.
     * @throws ClaimMetadataClientException If any property is not valid.
     */
    private void validateClaimProperties(Map<String, String> claimProperties) throws ClaimMetadataClientException {

        if (MapUtils.isEmpty(claimProperties)) {
            return;
        }
        for (Map.Entry<String, String> property : claimProperties.entrySet()) {
            String value = property.getValue();
            // Validating the sub-attributes property separately as it can contain multiple sub-attributes.
            if (SUB_ATTRIBUTES_PROPERTY.equals(property.getKey())) {
                for (String subAttribute : StringUtils.split(value, ' ')) {
                    if (StringUtils.isNotBlank(subAttribute) && subAttribute.length() > MAX_CLAIM_PROPERTY_LENGTH) {
                        throw new ClaimMetadataClientException(ERROR_CODE_CLAIM_PROPERTY_CHAR_LIMIT_EXCEED.getCode(),
                                String.format(ERROR_CODE_CLAIM_PROPERTY_CHAR_LIMIT_EXCEED.getMessage(),
                                        property.getKey(), MAX_CLAIM_PROPERTY_LENGTH));
                    }
                }
            } else if (StringUtils.isNotBlank(value) && value.length() > MAX_CLAIM_PROPERTY_LENGTH) {
                throw new ClaimMetadataClientException(ERROR_CODE_CLAIM_PROPERTY_CHAR_LIMIT_EXCEED.getCode(),
                        String.format(ERROR_CODE_CLAIM_PROPERTY_CHAR_LIMIT_EXCEED.getMessage(), property.getKey(),
                                MAX_CLAIM_PROPERTY_LENGTH));
            }
            if (StringUtils.equalsIgnoreCase(ClaimConstants.MIN_LENGTH, property.getKey()) ||
                    StringUtils.equalsIgnoreCase(ClaimConstants.MAX_LENGTH, property.getKey())) {
                checkMinMaxLimit(property.getKey(), property.getValue());
            }
        }
    }

    private void checkMinMaxLimit(String property, String value) throws ClaimMetadataClientException {

        if (!NumberUtils.isNumber(value) || Integer.parseInt(value) < MIN_CLAIM_PROPERTY_LENGTH_LIMIT ||
                Integer.parseInt(value) > MAX_CLAIM_PROPERTY_LENGTH_LIMIT) {
            throw new ClaimMetadataClientException(ERROR_CODE_CLAIM_LENGTH_LIMIT.getCode(),
                    String.format(ERROR_CODE_CLAIM_LENGTH_LIMIT.getMessage(), property,
                            MIN_CLAIM_PROPERTY_LENGTH_LIMIT, MAX_CLAIM_PROPERTY_LENGTH_LIMIT));
        }
    }

    private boolean isExistingClaimDialect(String claimDialectURI, int tenantId) throws ClaimMetadataException {

        return this.unifiedClaimMetadataManager.getClaimDialects(tenantId).stream().anyMatch(
                claimDialect -> claimDialect.getClaimDialectURI().equalsIgnoreCase(claimDialectURI));
    }

    private boolean isExistingExternalClaim(String externalClaimDialectURI, String externalClaimURI, int tenantId)
            throws ClaimMetadataException {

        return this.unifiedClaimMetadataManager.getExternalClaims(externalClaimDialectURI, tenantId).stream().anyMatch(
                claim -> claim.getClaimURI().equalsIgnoreCase(externalClaimURI));
    }

    private boolean isExistingLocalClaim(String localClaimURI, int tenantId) throws ClaimMetadataException {

        return this.unifiedClaimMetadataManager.getLocalClaims(tenantId).stream().anyMatch(
                claim -> claim.getClaimURI().equalsIgnoreCase(localClaimURI));
    }

    @Override
    public List<Claim> getMappedExternalClaimsForLocalClaim(String localClaimURI, String tenantDomain) throws
            ClaimMetadataException {

        int tenantId = IdentityTenantUtil.getTenantId(tenantDomain);
        return this.unifiedClaimMetadataManager.getMappedExternalClaims(localClaimURI, tenantId);
    }

    /**
     * Validate and sync the claim profile properties.
     *
     * @param claimProperties Map of claim properties to be updated.
     * @throws ClaimMetadataClientException If an invalid profile name is found.
     */
    private void validateAndSyncAttributeProfileProperties(Map<String, String> claimProperties)
            throws ClaimMetadataClientException {

        Set<String> allowedClaimProfiles = getAllowedClaimProfiles();

        // Validate profile names and throw an exception if an invalid profile name is found.
        for (Map.Entry<String, String> entry : claimProperties.entrySet()) {
            if (!entry.getKey().startsWith(ClaimConstants.PROFILES_CLAIM_PROPERTY_PREFIX)) {
                continue;
            }

           String[] profilePropertyKeyArray = entry.getKey().split("\\.");
           if (profilePropertyKeyArray.length < 2 || !allowedClaimProfiles.contains(profilePropertyKeyArray[1])) {
               throw new ClaimMetadataClientException(ERROR_CODE_INVALID_ATTRIBUTE_PROFILE);
           }
        }
        for (String propertyKey: ClaimConstants.ALLOWED_PROFILE_PROPERTY_KEYS) {
            syncAttributeProfileProperties(claimProperties, allowedClaimProfiles, propertyKey);
        }
    }

    /**
     * Remove the profile properties if the profile property value is the same as the global property value.
     * If all the profile properties have the same value, change the global property value to the profile value.
     *
     * @param claimProperties      Map of claim properties to be updated.
     * @param allowedClaimProfiles Set of allowed claim profiles.
     * @param propertyKey          Property key to be updated.
     */
    private void syncAttributeProfileProperties(Map<String, String> claimProperties, Set<String> allowedClaimProfiles,
                                                String propertyKey) {

        String globalValue = claimProperties.get(propertyKey);
        boolean isAllProfilesHaveSameValue = true;
        boolean isAtLeastOneProfileValueMatchingGlobal = false;
        String commonProfileValue = null;

        for (String profileName : allowedClaimProfiles) {
            String profilePropertyKey = buildAttributeProfilePropertyKey(profileName, propertyKey);
            String profilePropertyValue = claimProperties.get(profilePropertyKey);

            // Remove the profile property if it is the same as the global property.
            if (StringUtils.equals(globalValue, profilePropertyValue)) {
                claimProperties.remove(profilePropertyKey);
                isAtLeastOneProfileValueMatchingGlobal = true;
                continue;
            }

            // If we've already found at least one profile that matches the global, skip further consistency check.
            if (isAtLeastOneProfileValueMatchingGlobal) {
                continue;
            }

            if (StringUtils.isBlank(profilePropertyValue)) {
                isAllProfilesHaveSameValue = false;
                continue;
            }
            if (commonProfileValue == null) {
                commonProfileValue = profilePropertyValue;
            } else if (!StringUtils.equals(commonProfileValue, profilePropertyValue)) {
                isAllProfilesHaveSameValue = false;
            }
        }

        if (isAllProfilesHaveSameValue && !isAtLeastOneProfileValueMatchingGlobal) {
            // All the profiles have same value and the global value is different value. Hence, update the global value.
            claimProperties.put(propertyKey, commonProfileValue);

            //  Remove the profile-specific properties as the global value denotes all the profile properties.
            allowedClaimProfiles.forEach(profile -> {
                String key = ClaimConstants.PROFILES_CLAIM_PROPERTY_PREFIX + profile +
                        ClaimConstants.CLAIM_PROFILE_PROPERTY_DELIMITER + propertyKey;
                claimProperties.remove(key);
            });
        }
    }

    /**
     * Constructs the attribute profile property key for a given profile and property.
     *
     * @param profileName  Profile name (e.g., "console").
     * @param propertyKey  The property key associated with the profile (e.g., "Required").
     * @return A fully qualified profile property key (e.g., "Profiles.console.Required").
     */
    private String buildAttributeProfilePropertyKey(String profileName, String propertyKey) {

        return ClaimConstants.PROFILES_CLAIM_PROPERTY_PREFIX + profileName +
                ClaimConstants.CLAIM_PROFILE_PROPERTY_DELIMITER + propertyKey;
    }

    /**
     * Updates and synchronizes the claim uniqueness properties in the properties map.
     * Manages the relationship between the legacy 'isUnique' property and the newer 'UniquenessScope' property,
     * ensuring consistency between both properties based on their values.
     *
     * @param claimProperties         Map of claim properties to be updated.
     * @param existingClaimProperties Map of existing claim properties (null for new claims).
     */
    private void validateAndSyncUniquenessClaimProperties(Map<String, String> claimProperties,
                                                          Map<String, String> existingClaimProperties) {

        // Case 1: If the 'isUnique' property is not provided,
        // no property synchronization is needed.
        if (!claimProperties.containsKey(ClaimConstants.IS_UNIQUE_CLAIM_PROPERTY)) {
            return;
        }

        String newUniquenessScopeValue = claimProperties.get(ClaimConstants.CLAIM_UNIQUENESS_SCOPE_PROPERTY);
        String newIsUniqueValue = claimProperties.get(ClaimConstants.IS_UNIQUE_CLAIM_PROPERTY);

        // Case 2: If only the 'isUnique' property is provided,
        // set the 'UniquenessScope' property based on the 'isUnique' value.
        if (!claimProperties.containsKey(ClaimConstants.CLAIM_UNIQUENESS_SCOPE_PROPERTY)) {
            updateScopeFromIsUnique(claimProperties, newIsUniqueValue);
            return;
        }

        // Case 3: If both 'isUnique' and 'UniquenessScope' properties are provided.
        if (existingClaimProperties == null) {
            // If there are no existing claim properties (i.e., this is a new claim),
            // prioritize 'UniquenessScope' & update the 'isUnique' property accordingly.
            updateIsUniqueFromScope(claimProperties, newUniquenessScopeValue);
            return;
        }

        String existingScopeValue = existingClaimProperties.get(ClaimConstants.CLAIM_UNIQUENESS_SCOPE_PROPERTY);
        String existingIsUniqueValue = existingClaimProperties.get(ClaimConstants.IS_UNIQUE_CLAIM_PROPERTY);

        boolean isScopeChanged = !StringUtils.equals(newUniquenessScopeValue, existingScopeValue);
        boolean isUniqueChanged = !StringUtils.equalsIgnoreCase(newIsUniqueValue, existingIsUniqueValue);

        if (isScopeChanged) {
            // If 'UniquenessScope' has changed (regardless of 'isUnique' changes), prioritize 'UniquenessScope'.
            updateIsUniqueFromScope(claimProperties, newUniquenessScopeValue);
        } else if (isUniqueChanged) {
            // If only 'isUnique' has changed, update 'UniquenessScope' based on the new 'isUnique' value.
            updateScopeFromIsUnique(claimProperties, newIsUniqueValue);
        }
    }

    /**
     * Updates the uniqueness scope property based on the isUnique value.
     *
     * @param properties    Map of claim properties to update.
     * @param isUniqueValue String value of isUnique property ("true" or "false").
     */
    private void updateScopeFromIsUnique(Map<String, String> properties, String isUniqueValue) {

        boolean isUnique = Boolean.parseBoolean(isUniqueValue);
        properties.put(ClaimConstants.CLAIM_UNIQUENESS_SCOPE_PROPERTY,
                isUnique ? getServerLevelClaimUniquenessScope().toString() :
                        ClaimConstants.ClaimUniquenessScope.NONE.toString());
    }

    /**
     * Updates the isUnique property based on the uniqueness scope value.
     *
     * @param properties Map of claim properties to update.
     * @param scopeValue String value of the uniqueness scope.
     */
    private void updateIsUniqueFromScope(Map<String, String> properties, String scopeValue) {

        boolean isUnique = !ClaimConstants.ClaimUniquenessScope.NONE.toString().equals(scopeValue);
        properties.put(ClaimConstants.IS_UNIQUE_CLAIM_PROPERTY, String.valueOf(isUnique));
    }

    /**
     * Validates the shared profile value resolving method change for system claims.
     *
     * @param updatedLocalClaim  Updated local claim.
     * @param existingLocalClaim Existing local claim.
     * @param tenantId           Tenant ID.
     * @throws ClaimMetadataException If the shared profile value resolving method change is invalid for system claim
     *                                or updating value is unaccepted.
     */
    private void validateSharedProfileValueResolvingMethodChange(LocalClaim updatedLocalClaim,
                                                                 LocalClaim existingLocalClaim, int tenantId)
            throws ClaimMetadataException {

        validateSharedProfileValueResolvingMethodValue(updatedLocalClaim);
        /*
        If the existing local claim is non system claim, shared profile value resolving method can be changed
        if the updating value is valid.
         */
        if (!Boolean.parseBoolean(existingLocalClaim.getClaimProperty(ClaimConstants.IS_SYSTEM_CLAIM))) {
            return;
        }

        String updatedClaimProperty =
                updatedLocalClaim.getClaimProperty(ClaimConstants.SHARED_PROFILE_VALUE_RESOLVING_METHOD);
        String existingClaimProperty =
                existingLocalClaim.getClaimProperty(ClaimConstants.SHARED_PROFILE_VALUE_RESOLVING_METHOD);
        // If both values are blank or the same, no need to validate further.
        if (StringUtils.isBlank(updatedClaimProperty) && StringUtils.isBlank(existingClaimProperty)) {
            return;
        }
        if (StringUtils.equals(updatedClaimProperty, existingClaimProperty)) {
            return;
        }
        /*
        If updatedClaimProperty has a value, it should be equals to system default.
        Removing the updatedClaimProperty is allowed. Then, in the runtime, the system default value will be used.
         */
        if (StringUtils.isNotBlank(updatedClaimProperty)) {
            Optional<LocalClaim> systemDefaultClaim =
                    this.systemDefaultClaimMetadataManager.getLocalClaim(existingLocalClaim.getClaimURI(), tenantId);
            if (systemDefaultClaim.isPresent()) {
                String systemDefaultClaimPropertyValue =
                        systemDefaultClaim.get().getClaimProperty(ClaimConstants.SHARED_PROFILE_VALUE_RESOLVING_METHOD);
                if (StringUtils.equals(systemDefaultClaimPropertyValue, updatedClaimProperty)) {
                    return;
                }
                throw new ClaimMetadataClientException(
                        ERROR_CODE_NO_SHARED_PROFILE_VALUE_RESOLVING_METHOD_CHANGE_FOR_SYSTEM_CLAIM.getCode(),
                        String.format(
                                ERROR_CODE_NO_SHARED_PROFILE_VALUE_RESOLVING_METHOD_CHANGE_FOR_SYSTEM_CLAIM.getMessage(),
                                existingLocalClaim.getClaimURI()));
            }
        }
    }

    /**
     * Validates the shared profile value resolving method value.
     *
     * @param localClaim Local claim.
     * @throws ClaimMetadataException If the shared profile value resolving method value is invalid.
     */
    private void validateSharedProfileValueResolvingMethodValue(LocalClaim localClaim)
            throws ClaimMetadataClientException {

        String claimProperty =
                localClaim.getClaimProperty(ClaimConstants.SHARED_PROFILE_VALUE_RESOLVING_METHOD);

        if (StringUtils.isNotBlank(claimProperty)) {
            try {
                ClaimConstants.SharedProfileValueResolvingMethod.fromName(claimProperty);
            } catch (IllegalArgumentException e) {
                throw new ClaimMetadataClientException(
                        ERROR_CODE_INVALID_SHARED_PROFILE_VALUE_RESOLVING_METHOD.getCode(),
                        String.format(ERROR_CODE_INVALID_SHARED_PROFILE_VALUE_RESOLVING_METHOD.getMessage(),
                                claimProperty));
            }
        }
    }

    /**
     * Set the managed in user store property for the given local claim.
     *
     * @param localClaim                      Local claim.
     * @param userStoreBasedIdentityDataStore Whether the identity data store is user store based.
     */
    private void setManagedInUserStoreProperty(LocalClaim localClaim, boolean userStoreBasedIdentityDataStore) {

        if (localClaim.getClaimProperties().containsKey(ClaimConstants.MANAGED_IN_USER_STORE_PROPERTY)) {
            if (log.isDebugEnabled()) {
                log.debug("ManagedInUserStore property is already set for claim: " + localClaim.getClaimURI() +
                        ". Skipping setting default value.");
            }
            return;
        }

        if (userStoreBasedIdentityDataStore) {
            if (log.isDebugEnabled()) {
                log.debug(String.format("Identity data store is user store based. Setting ManagedInUserStore=true " +
                        "for claim: %s", localClaim.getClaimURI()));
            }
            localClaim.setClaimProperty(MANAGED_IN_USER_STORE_PROPERTY, Boolean.TRUE.toString());
        } else {
            String defaultManagedInUserStoreValue = getDefaultManagedInUserStoreValue(isIdentityClaim(localClaim));
            if (log.isDebugEnabled()) {
                log.debug(String.format("Setting default ManagedInUserStore=%s for claim: %s",
                        defaultManagedInUserStoreValue, localClaim.getClaimURI()));
            }
            localClaim.setClaimProperty(MANAGED_IN_USER_STORE_PROPERTY, defaultManagedInUserStoreValue);
        }
    }

    /**
     * Configure managed in user store related properties for the given local claim.
     *
     * @param localClaim   Local claim.
     * @param tenantDomain Tenant domain.
     * @throws ClaimMetadataException If an error occurs while getting UserStoreManager.
     */
    private void setManagedInUserStorePropertyAndExcludedUserStoresProperty(LocalClaim localClaim, String tenantDomain)
            throws ClaimMetadataException {

        if (log.isDebugEnabled()) {
            log.debug(String.format("Setting managed in user store property for claim: %s in tenant: %s",
                    localClaim.getClaimURI(), tenantDomain));
        }

        // If the identity data store is user store based, all the claims should be stored in user store.
        if (isUserStoreBasedIdentityDataStore()) {
            if (log.isDebugEnabled()) {
                log.debug(String.format("Identity data store is user store based. Setting ManagedInUserStore=true " +
                        "for claim: %s", localClaim.getClaimURI()));
            }
            localClaim.setClaimProperty(MANAGED_IN_USER_STORE_PROPERTY, Boolean.TRUE.toString());
            return;
        }

        String managedInUserStorePropertyValue = localClaim.getClaimProperty(MANAGED_IN_USER_STORE_PROPERTY);
        boolean isIdentityClaim = isIdentityClaim(localClaim);

        // Set default value for ManagedInUserStore property if not set.
        if (managedInUserStorePropertyValue == null) {
            managedInUserStorePropertyValue = getDefaultManagedInUserStoreValue(isIdentityClaim);
            localClaim.setClaimProperty(MANAGED_IN_USER_STORE_PROPERTY, managedInUserStorePropertyValue);

            if (log.isDebugEnabled()) {
                log.debug(String.format("Claim: %s does not have ManagedInUserStore property. " +
                                "Defaulting to %s.", localClaim.getClaimURI(), managedInUserStorePropertyValue));
            }
        }

        // If managed in user store is not enabled, no further processing is needed.
        boolean isManagedInUserStore = Boolean.parseBoolean(managedInUserStorePropertyValue);
        if (!isManagedInUserStore) {
            if (log.isDebugEnabled()) {
                log.debug(String.format("ManagedInUserStore is false for claim: %s. " +
                        "No excluded user stores processing needed.", localClaim.getClaimURI()));
            }
            return;
        }

        // Process excluded user stores for all claims where ManagedInUserStore is true.
        Set<String> excludedUserStores = getExcludedUserStoresFromClaim(localClaim);
        if (excludedUserStores.isEmpty()) {
            // No excluded user stores specified, set property to empty.
            localClaim.setClaimProperty(EXCLUDED_USER_STORES_PROPERTY, StringUtils.EMPTY);
            return;
        }

        // Resolve and validate excluded user stores.
        if (log.isDebugEnabled()) {
            log.debug(String.format("Processing excluded user stores for claim: %s", localClaim.getClaimURI()));
        }
        excludedUserStores = resolveExcludedUserStoresList(excludedUserStores, tenantDomain, false);

        if (!excludedUserStores.isEmpty()) {
            if (log.isDebugEnabled()) {
                log.debug(String.format("Setting excluded user stores for claim: %s. Excluded stores: %s",
                        localClaim.getClaimURI(), String.join(", ", excludedUserStores)));
            }
            localClaim.setClaimProperty(EXCLUDED_USER_STORES_PROPERTY,
                    String.join(ClaimConstants.COMMA_SEPARATOR, excludedUserStores));
        } else {
            localClaim.setClaimProperty(EXCLUDED_USER_STORES_PROPERTY, StringUtils.EMPTY);
        }
    }

    /**
     * Validate and synchronize the managed in user store related properties for the given local claim.
     * This method validates that mandatory user stores are not excluded and syncs the excluded list.
     *
     * @param localClaim   Local claim to validate.
     * @param tenantDomain Tenant domain.
     * @throws ClaimMetadataException If validation fails or an error occurs while getting UserStoreManager.
     */
    private void validateAndSyncClaimStoreSettings(LocalClaim localClaim, String tenantDomain)
            throws ClaimMetadataException {

        if (log.isDebugEnabled()) {
            log.debug(String.format("Validating and syncing claim store settings for claim: %s in tenant: %s",
                    localClaim.getClaimURI(), tenantDomain));
        }

        String managedInUserStorePropertyValue = localClaim.getClaimProperty(MANAGED_IN_USER_STORE_PROPERTY);
        boolean isUserStoreBasedIdentityDataStore = isUserStoreBasedIdentityDataStore();

        // Validate that managedInUserStore cannot be false when identity data store is user store based.
        if (isUserStoreBasedIdentityDataStore &&
                StringUtils.equalsIgnoreCase(managedInUserStorePropertyValue, Boolean.FALSE.toString())) {
            if (log.isDebugEnabled()) {
                log.debug(String.format("Cannot set ManagedInUserStore=false for claim: %s " +
                        "when identity data store is user store based.", localClaim.getClaimURI()));
            }
            throw new ClaimMetadataClientException(
                    ERROR_CODE_CLAIM_MUST_BE_MANAGED_IN_USER_STORE.getCode(),
                    String.format(ERROR_CODE_CLAIM_MUST_BE_MANAGED_IN_USER_STORE.getMessage(),
                            localClaim.getClaimURI()));
        }

        boolean isIdentityClaim = isIdentityClaim(localClaim);

        // Set default value for ManagedInUserStore property if not set.
        if (managedInUserStorePropertyValue == null) {
            managedInUserStorePropertyValue = isUserStoreBasedIdentityDataStore
                    ? Boolean.TRUE.toString()
                    : getDefaultManagedInUserStoreValue(isIdentityClaim);
            localClaim.setClaimProperty(MANAGED_IN_USER_STORE_PROPERTY, managedInUserStorePropertyValue);

            if (log.isDebugEnabled()) {
                log.debug(String.format("Claim: %s does not have ManagedInUserStore property. " +
                        "Defaulting to %s.", localClaim.getClaimURI(), managedInUserStorePropertyValue));
            }
        }

        boolean isManagedInUserStore = Boolean.parseBoolean(managedInUserStorePropertyValue);

        // If managed in user store is not enabled, remove excluded user stores property.
        if (!isManagedInUserStore) {
            if (log.isDebugEnabled()) {
                log.debug(String.format("ManagedInUserStore is false for claim: %s. " +
                        "Removing excluded user stores property.", localClaim.getClaimURI()));
            }
            localClaim.getClaimProperties().remove(EXCLUDED_USER_STORES_PROPERTY);
            return;
        }

        // Validate and resolve excluded user stores for all claims where ManagedInUserStore is true.
        Set<String> excludedUserStores = getExcludedUserStoresFromClaim(localClaim);
        if (excludedUserStores.isEmpty()) {
            // No excluded user stores specified, nothing to validate.
            if (log.isDebugEnabled()) {
                log.debug(String.format("No excluded user stores specified for claim: %s", localClaim.getClaimURI()));
            }
            return;
        }

        // Validate and resolve excluded user stores.
        if (log.isDebugEnabled()) {
            log.debug(String.format("Validating and resolving excluded user stores for claim: %s (validation mode)",
                    localClaim.getClaimURI()));
        }
        excludedUserStores = resolveExcludedUserStoresList(excludedUserStores, tenantDomain, true);

        if (!excludedUserStores.isEmpty()) {
            if (log.isDebugEnabled()) {
                log.debug(String.format("Setting validated excluded user stores for claim: %s. Excluded stores: %s",
                        localClaim.getClaimURI(), String.join(", ", excludedUserStores)));
            }
            localClaim.setClaimProperty(EXCLUDED_USER_STORES_PROPERTY,
                    String.join(ClaimConstants.COMMA_SEPARATOR, excludedUserStores));
        } else {
            if (log.isDebugEnabled()) {
                log.debug(String.format("No excluded user stores to set for claim: %s after validation",
                        localClaim.getClaimURI()));
            }
            // Remove the property if validation removed all excluded user stores.
            localClaim.getClaimProperties().remove(EXCLUDED_USER_STORES_PROPERTY);
        }
    }

    /**
     * Extract excluded user stores from the local claim property.
     *
     * @param localClaim Local claim containing excluded user stores property.
     * @return Set of excluded user store domain names.
     */
    private Set<String> getExcludedUserStoresFromClaim(LocalClaim localClaim) {

        Set<String> excludedUserStores = new LinkedHashSet<>();
        String excludedUserStoresPropertyValue = localClaim.getClaimProperty(EXCLUDED_USER_STORES_PROPERTY);

        if (StringUtils.isNotBlank(excludedUserStoresPropertyValue)) {
            excludedUserStores.addAll(
                    Arrays.stream(StringUtils.split(excludedUserStoresPropertyValue, ClaimConstants.COMMA_SEPARATOR))
                            .filter(StringUtils::isNotBlank)
                            .map(String::trim)
                            .collect(Collectors.toList()));
        }

        if (log.isDebugEnabled()) {
            log.debug(String.format("Extracted excluded user stores from claim: %s. Excluded stores: %s",
                    localClaim.getClaimURI(),
                    excludedUserStores.isEmpty() ? "None" : String.join(", ", excludedUserStores)));
        }

        return excludedUserStores;
    }

    /**
     * Resolve the excluded user stores list for any claim type.
     * This method validates the excluded user stores list based on the user store configuration.
     * User stores with StoreIdentityClaims=true should not be in the excluded list for any claim type
     * (both identity and non-identity claims).
     *
     * @param excludedUserStores Initial set of excluded user stores.
     * @param tenantDomain       Tenant domain.
     * @param validateMode       If true, throws an exception if a user store with StoreIdentityClaims=true is in the
     *                           excluded list. If false, silently removes such stores from the excluded list.
     * @return Updated set of excluded user stores.
     * @throws ClaimMetadataException If validation fails in validate mode or error getting UserStoreManager.
     */
    private Set<String> resolveExcludedUserStoresList(Set<String> excludedUserStores, String tenantDomain,
                                                        boolean validateMode) throws ClaimMetadataException {

        if (log.isDebugEnabled()) {
            log.debug(String.format("Resolving excluded user stores list. Tenant: %s, Validate mode: %s, " +
                            "Initial excluded stores: %s",
                    tenantDomain, validateMode,
                    excludedUserStores.isEmpty() ? "None" : String.join(", ", excludedUserStores)));
        }

        Set<String> processedExcludedStores = new LinkedHashSet<>(excludedUserStores);
        AbstractUserStoreManager userStoreManager = (AbstractUserStoreManager) getUserStoreManager(tenantDomain);

        int userStoreCount = 0;
        while (userStoreManager != null) {
            userStoreCount++;
            String domainName = userStoreManager.getRealmConfiguration()
                    .getUserStoreProperty(UserCoreConstants.RealmConfig.PROPERTY_DOMAIN_NAME);
            boolean isStoreIdentityClaimsEnabled = Boolean.parseBoolean(userStoreManager.getRealmConfiguration()
                    .getUserStoreProperty(STORE_IDENTITY_CLAIMS));

            if (log.isDebugEnabled()) {
                log.debug(String.format("Processing user store #%d: %s, StoreIdentityClaims: %s",
                        userStoreCount, domainName, isStoreIdentityClaimsEnabled));
            }

            if (isStoreIdentityClaimsEnabled) {
                // Stores configured to store identity claims should not be excluded for any claim type.
                boolean wasInExcludedList = containsIgnoreCase(processedExcludedStores, domainName);

                if (validateMode && wasInExcludedList) {
                    if (log.isDebugEnabled()) {
                        log.debug(String.format("Validation failed: User store '%s' is configured to store " +
                                "identity claims and cannot be excluded for any claim.", domainName));
                    }
                    throw new ClaimMetadataClientException(
                            ERROR_CODE_CANNOT_EXCLUDE_USER_STORE.getCode(),
                            String.format(
                                    ERROR_CODE_CANNOT_EXCLUDE_USER_STORE.getMessage(), domainName));
                }

                boolean removed = processedExcludedStores.removeIf(store -> store.equalsIgnoreCase(domainName));
                if (removed && log.isDebugEnabled()) {
                    log.debug(String.format("Removed user store '%s' from excluded list as it is configured to " +
                            "store identity claims (applies to all claims).", domainName));
                }
            }

            userStoreManager = (AbstractUserStoreManager) userStoreManager.getSecondaryUserStoreManager();
        }

        if (log.isDebugEnabled()) {
            log.debug(String.format("Resolved excluded user stores list. Processed %d user stores. " +
                            "Final excluded stores: %s",
                    userStoreCount,
                    processedExcludedStores.isEmpty() ? "None" : String.join(", ", processedExcludedStores)));
        }

        return processedExcludedStores;
    }

    /**
     * Get the default value for ManagedInUserStore property based on claim type.
     * Identity claims default to false, non-identity claims default to true.
     *
     * @param isIdentityClaim Whether the claim is an identity claim.
     * @return Default value for ManagedInUserStore property.
     */
    private String getDefaultManagedInUserStoreValue(boolean isIdentityClaim) {

        return isIdentityClaim ? Boolean.FALSE.toString() : Boolean.TRUE.toString();
    }

    /**
     * Get UserStoreManager for the given tenant domain.
     *
     * @param tenantDomain Tenant domain
     * @return UserStoreManager
     * @throws ClaimMetadataServerException If an error occurs while getting UserStoreManager
     */
    private UserStoreManager getUserStoreManager(String tenantDomain) throws ClaimMetadataServerException {

        UserStoreManager userStoreManager = null;
        RealmService realmService = IdentityClaimManagementServiceDataHolder.getInstance().getRealmService();
        int tenantId = IdentityTenantUtil.getTenantId(tenantDomain);
        try {

            UserRealm userRealm = realmService.getTenantUserRealm(tenantId);
            if (userRealm != null) {
                userStoreManager = (UserStoreManager) userRealm.getUserStoreManager();
                return userStoreManager;
            } else {
                log.error(String.format("User realm is null for tenant : %s", tenantDomain));
            }
        } catch (UserStoreException e) {
            if (log.isDebugEnabled()) {
                log.debug("Error while getting user store manager for tenant : " + tenantDomain, e);
            }
            throw new ClaimMetadataServerException(ERROR_CODE_SERVER_ERROR_GETTING_USER_STORE_MANAGER.getCode(),
                   String.format(ERROR_CODE_SERVER_ERROR_GETTING_USER_STORE_MANAGER.getMessage(), tenantDomain), e);
        }
        return userStoreManager;
    }
}
