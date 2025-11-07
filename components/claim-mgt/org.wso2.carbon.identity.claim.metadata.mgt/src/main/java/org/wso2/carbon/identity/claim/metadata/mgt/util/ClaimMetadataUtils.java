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

package org.wso2.carbon.identity.claim.metadata.mgt.util;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.claim.metadata.mgt.dto.AttributeMappingDTO;
import org.wso2.carbon.identity.claim.metadata.mgt.dto.ClaimDialectDTO;
import org.wso2.carbon.identity.claim.metadata.mgt.dto.ClaimPropertyDTO;
import org.wso2.carbon.identity.claim.metadata.mgt.dto.ExternalClaimDTO;
import org.wso2.carbon.identity.claim.metadata.mgt.dto.LocalClaimDTO;
import org.wso2.carbon.identity.claim.metadata.mgt.internal.IdentityClaimManagementServiceDataHolder;
import org.wso2.carbon.identity.claim.metadata.mgt.model.AttributeMapping;
import org.wso2.carbon.identity.claim.metadata.mgt.model.ClaimDialect;
import org.wso2.carbon.identity.claim.metadata.mgt.model.ExternalClaim;
import org.wso2.carbon.identity.claim.metadata.mgt.model.LocalClaim;
import org.wso2.carbon.identity.core.util.IdentityUtil;
import org.wso2.carbon.user.api.UserRealm;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.core.UserCoreConstants;
import org.wso2.carbon.user.core.claim.Claim;
import org.wso2.carbon.user.core.claim.ClaimKey;
import org.wso2.carbon.user.core.claim.ClaimMapping;
import org.wso2.carbon.user.core.claim.inmemory.ClaimConfig;
import org.wso2.carbon.user.core.service.RealmService;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.wso2.carbon.identity.claim.metadata.mgt.util.ClaimConstants.COMMA_SEPARATOR;
import static org.wso2.carbon.identity.claim.metadata.mgt.util.ClaimConstants.LOCAL_CLAIM_DIALECT_URI;
import static org.wso2.carbon.identity.claim.metadata.mgt.util.ClaimConstants.UNIQUENESS_VALIDATION_SCOPE;

/**
 * Utility class containing various claim metadata implementation related functionality.
 */
public class ClaimMetadataUtils {

    private static final Log log = LogFactory.getLog(ClaimMetadataUtils.class);
    public static final String CORRELATION_ID_MDC = "Correlation-ID";
    private static final String IDENTITY_DATA_STORE_TYPE = "IdentityDataStore.DataStoreType";
    private static final String DEFAULT_USER_STORE_BASED_IDENTITY_DATA_STORE =
            "org.wso2.carbon.identity.governance.store.UserStoreBasedIdentityDataStore";

    private ClaimMetadataUtils() {
    }

    public static ClaimDialectDTO convertClaimDialectToClaimDialectDTO(ClaimDialect claimDialect) {

        ClaimDialectDTO claimDialectDTO = new ClaimDialectDTO();
        claimDialectDTO.setClaimDialectURI(claimDialect.getClaimDialectURI());
        return claimDialectDTO;
    }

    public static ClaimDialectDTO[] convertClaimDialectsToClaimDialectDTOs(ClaimDialect[] claimDialects) {

        ClaimDialectDTO[] claimDialectDTOs = new ClaimDialectDTO[claimDialects.length];

        for (int i = 0; i < claimDialects.length; i++) {
            claimDialectDTOs[i] = convertClaimDialectToClaimDialectDTO(claimDialects[i]);
        }

        return claimDialectDTOs;
    }

    public static ClaimDialect convertClaimDialectDTOToClaimDialect(ClaimDialectDTO claimDialectDTO) {

        ClaimDialect claimDialect = new ClaimDialect(claimDialectDTO.getClaimDialectURI());
        return claimDialect;
    }


    public static LocalClaimDTO convertLocalClaimToLocalClaimDTO(LocalClaim localClaim) {

        LocalClaimDTO localClaimDTO = new LocalClaimDTO();
        localClaimDTO.setLocalClaimURI(localClaim.getClaimURI());

        // Convert List<AttributeMapping> to AttributeMappingDTO[]
        List<AttributeMapping> attributeMappings = localClaim.getMappedAttributes();
        AttributeMappingDTO[] attributeMappingDTOs = new AttributeMappingDTO[attributeMappings.size()];

        int i = 0;
        for (AttributeMapping attributeMapping : attributeMappings) {
            AttributeMappingDTO attributeMappingDTO = new AttributeMappingDTO();
            attributeMappingDTO.setUserStoreDomain(attributeMapping.getUserStoreDomain());
            attributeMappingDTO.setAttributeName(attributeMapping.getAttributeName());
            attributeMappingDTOs[i] = attributeMappingDTO;
            i++;
        }

        localClaimDTO.setAttributeMappings(attributeMappingDTOs);

        // Convert Map<String, String> to ClaimPropertyDTO[]
        Map<String, String> claimProperties = localClaim.getClaimProperties();
        ClaimPropertyDTO[] claimPropertyDTOs = new ClaimPropertyDTO[claimProperties.size()];

        int j = 0;
        for (Map.Entry<String, String> claimPropertyEntry : claimProperties.entrySet()) {
            ClaimPropertyDTO claimProperty = new ClaimPropertyDTO();
            claimProperty.setPropertyName(claimPropertyEntry.getKey());
            claimProperty.setPropertyValue(claimPropertyEntry.getValue());
            claimPropertyDTOs[j] = claimProperty;
            j++;
        }

        localClaimDTO.setClaimProperties(claimPropertyDTOs);

        return localClaimDTO;
    }

    public static LocalClaimDTO[] convertLocalClaimsToLocalClaimDTOs(LocalClaim[] localClaims) {

        LocalClaimDTO[] localClaimDTOs = new LocalClaimDTO[localClaims.length];

        for (int i = 0; i < localClaims.length; i++) {
            localClaimDTOs[i] = convertLocalClaimToLocalClaimDTO(localClaims[i]);
        }

        return localClaimDTOs;
    }

    public static LocalClaim convertLocalClaimDTOToLocalClaim(LocalClaimDTO localClaimDTO) {

        // TODO : Validate if localClaimDTO null???
        LocalClaim localClaim = new LocalClaim(localClaimDTO.getLocalClaimURI());

        // Convert AttributeMappingDTO[] to List<AttributeMapping>
        if (localClaimDTO.getAttributeMappings() != null) {

            List<AttributeMapping> attributeMappings = new ArrayList<>();

            for (AttributeMappingDTO attributeMappingDTO : localClaimDTO.getAttributeMappings()) {
                attributeMappings.add(new AttributeMapping(attributeMappingDTO.getUserStoreDomain(),
                        attributeMappingDTO.getAttributeName()));
            }

            localClaim.setMappedAttributes(attributeMappings);
        }

        // Convert ClaimPropertyDTO[] to Map<String, String>
        if (localClaimDTO.getClaimProperties() != null) {

            Map<String, String> claimProperties = new HashMap<>();

            for (ClaimPropertyDTO claimPropertyDTO : localClaimDTO.getClaimProperties()) {
                claimProperties.put(claimPropertyDTO.getPropertyName(), claimPropertyDTO.getPropertyValue());
            }

            localClaim.setClaimProperties(claimProperties);
        }

        return localClaim;
    }


    public static ExternalClaimDTO convertExternalClaimToExternalClaimDTO(ExternalClaim externalClaim) {

        ExternalClaimDTO externalClaimDTO = new ExternalClaimDTO();
        externalClaimDTO.setExternalClaimDialectURI(externalClaim.getClaimDialectURI());
        externalClaimDTO.setExternalClaimURI(externalClaim.getClaimURI());
        externalClaimDTO.setMappedLocalClaimURI(externalClaim.getMappedLocalClaim());

        // Convert Map<String, String> to ClaimPropertyDTO[]
        Map<String, String> claimProperties = externalClaim.getClaimProperties();
        ClaimPropertyDTO[] claimPropertyDTOs = new ClaimPropertyDTO[claimProperties.size()];

        int j = 0;
        for (Map.Entry<String, String> claimPropertyEntry : claimProperties.entrySet()) {
            ClaimPropertyDTO claimProperty = new ClaimPropertyDTO();
            claimProperty.setPropertyName(claimPropertyEntry.getKey());
            claimProperty.setPropertyValue(claimPropertyEntry.getValue());
            claimPropertyDTOs[j] = claimProperty;
            j++;
        }
        externalClaimDTO.setClaimProperties(claimPropertyDTOs);
        return externalClaimDTO;
    }

    public static ExternalClaimDTO[] convertExternalClaimsToExternalClaimDTOs(ExternalClaim[] externalClaims) {

        ExternalClaimDTO[] externalClaimDTOs = new ExternalClaimDTO[externalClaims.length];

        for (int i = 0; i < externalClaims.length; i++) {
            externalClaimDTOs[i] = convertExternalClaimToExternalClaimDTO(externalClaims[i]);
        }

        return externalClaimDTOs;
    }

    public static ExternalClaim convertExternalClaimDTOToExternalClaim(ExternalClaimDTO externalClaimDTO) {

        // TODO : Validate if externalClaimDTO null???
        ExternalClaim externalClaim = new ExternalClaim(externalClaimDTO.getExternalClaimDialectURI(), externalClaimDTO
                .getExternalClaimURI(), externalClaimDTO.getMappedLocalClaimURI());

        // Convert ClaimPropertyDTO[] to Map<String, String>
        if (externalClaimDTO.getClaimProperties() != null) {

            Map<String, String> claimProperties = new HashMap<>();

            for (ClaimPropertyDTO claimPropertyDTO : externalClaimDTO.getClaimProperties()) {
                claimProperties.put(claimPropertyDTO.getPropertyName(), claimPropertyDTO.getPropertyValue());
            }

            externalClaim.setClaimProperties(claimProperties);
        }
        return externalClaim;
    }

    public static ClaimMapping convertLocalClaimToClaimMapping(LocalClaim localClaim, int tenantId) throws
            UserStoreException {

        ClaimMapping claimMapping = new ClaimMapping();

        Claim claim = new Claim();
        claim.setClaimUri(localClaim.getClaimURI());
        claim.setDialectURI(localClaim.getClaimDialectURI());

        Map<String, String> claimProperties = localClaim.getClaimProperties();

        if (claimProperties.containsKey(ClaimConstants.DISPLAY_NAME_PROPERTY)) {
            claim.setDisplayTag(claimProperties.get(ClaimConstants.DISPLAY_NAME_PROPERTY));
        }

        if (claimProperties.containsKey(ClaimConstants.DESCRIPTION_PROPERTY)) {
            claim.setDescription(claimProperties.get(ClaimConstants.DESCRIPTION_PROPERTY));
        }

        if (claimProperties.containsKey(ClaimConstants.REGULAR_EXPRESSION_PROPERTY)) {
            claim.setRegEx(claimProperties.get(ClaimConstants.REGULAR_EXPRESSION_PROPERTY));
        }

        if (claimProperties.containsKey(ClaimConstants.DISPLAY_ORDER_PROPERTY)) {
            claim.setDisplayOrder(Integer.parseInt(claimProperties.get(ClaimConstants.DISPLAY_ORDER_PROPERTY)));
        }

        if (claimProperties.containsKey(ClaimConstants.SUPPORTED_BY_DEFAULT_PROPERTY)) {
            if ("false".equalsIgnoreCase(claimProperties.get(ClaimConstants.SUPPORTED_BY_DEFAULT_PROPERTY))) {
                claim.setSupportedByDefault(Boolean.FALSE);

            } else {
                claim.setSupportedByDefault(Boolean.TRUE);
            }
        }

        if (claimProperties.containsKey(ClaimConstants.REQUIRED_PROPERTY)) {
            if ("false".equalsIgnoreCase(claimProperties.get(ClaimConstants.REQUIRED_PROPERTY))) {
                claim.setRequired(Boolean.FALSE);

            } else {
                claim.setRequired(Boolean.TRUE);
            }
        }

        if (claimProperties.containsKey(ClaimConstants.READ_ONLY_PROPERTY)) {
            if ("false".equalsIgnoreCase(claimProperties.get(ClaimConstants.READ_ONLY_PROPERTY))) {
                claim.setReadOnly(Boolean.FALSE);

            } else {
                claim.setReadOnly(Boolean.TRUE);
            }
        }

        if (claimProperties.containsKey(ClaimConstants.MULTI_VALUED_PROPERTY)) {
            claim.setMultiValued(Boolean.parseBoolean(claimProperties.get(ClaimConstants.MULTI_VALUED_PROPERTY)));
        }

        if (claimProperties.containsKey(ClaimConstants.MANAGED_IN_USER_STORE_PROPERTY)) {
            claim.setManagedInUserStore(
                    Boolean.parseBoolean(claimProperties.get(ClaimConstants.MANAGED_IN_USER_STORE_PROPERTY)));
        } else {
            claim.setManagedInUserStore(null);
        }

        if (claimProperties.containsKey(ClaimConstants.EXCLUDED_USER_STORES_PROPERTY)) {
            String excludedUserStoresStr = claimProperties.get(ClaimConstants.EXCLUDED_USER_STORES_PROPERTY);
            Set<String> excludedUserStores =
                    new HashSet<>(Arrays.asList(StringUtils.split(excludedUserStoresStr, COMMA_SEPARATOR)));
            claim.setExcludedUserStores(excludedUserStores);
        }

        claimMapping.setClaim(claim);

        List<AttributeMapping> mappedAttributes = localClaim.getMappedAttributes();
        for (AttributeMapping attributeMapping : mappedAttributes) {
            claimMapping.setMappedAttribute(attributeMapping.getUserStoreDomain(), attributeMapping.getAttributeName());
        }

        if (claimProperties.containsKey(ClaimConstants.DEFAULT_ATTRIBUTE)) {
            claimMapping.setMappedAttribute(claimProperties.get(ClaimConstants.DEFAULT_ATTRIBUTE));
        } else {
            RealmService realmService = IdentityClaimManagementServiceDataHolder.getInstance().getRealmService();

            if (realmService != null && realmService.getTenantUserRealm(tenantId) != null) {

                UserRealm realm = realmService.getTenantUserRealm(tenantId);
                String primaryDomainName = realm.getRealmConfiguration().getUserStoreProperty
                        (UserCoreConstants.RealmConfig.PROPERTY_DOMAIN_NAME);
                claimMapping.setMappedAttribute(localClaim.getMappedAttribute(primaryDomainName));
            } else {
                claimMapping.setMappedAttribute(localClaim.getMappedAttribute(UserCoreConstants.
                        PRIMARY_DEFAULT_DOMAIN_NAME));
            }
        }

        return claimMapping;
    }

    public static ClaimMapping convertExternalClaimToClaimMapping(ExternalClaim externalClaim, List<LocalClaim>
            localClaims, int tenantId) throws UserStoreException {

        ClaimMapping claimMapping = new ClaimMapping();

        if (localClaims != null) {
            for (LocalClaim localClaim : localClaims) {
                if (externalClaim.getMappedLocalClaim().equalsIgnoreCase(localClaim.getClaimURI())) {
                    claimMapping = convertLocalClaimToClaimMapping(localClaim, tenantId);
                    break;
                }
            }
        }

        if (claimMapping == null) {
            claimMapping = new ClaimMapping();
        }

        if (claimMapping.getClaim() == null) {
            Claim claim = new Claim();
            claimMapping.setClaim(claim);
        }

        claimMapping.getClaim().setDialectURI(externalClaim.getClaimDialectURI());
        claimMapping.getClaim().setClaimUri(externalClaim.getClaimURI());
        return claimMapping;
    }

    /**
     * This method is used to build system default claims from claim config.
     *
     * @param claimConfig Claim Mapping
     * @return Claim Dialect
     */
    public static Map<String, List<org.wso2.carbon.identity.claim.metadata.mgt.model.Claim>> getClaimsMapFromClaimConfig
            (ClaimConfig claimConfig) {

        Map<String, List<org.wso2.carbon.identity.claim.metadata.mgt.model.Claim>> claims = new HashMap<>();
        if (claimConfig != null && MapUtils.isNotEmpty(claimConfig.getClaimMap())) {
            for (Map.Entry<ClaimKey, ClaimMapping> entry : claimConfig.getClaimMap().entrySet()) {
                ClaimKey claimKey = entry.getKey();
                ClaimMapping claimMapping = entry.getValue();
                String claimDialectURI = claimKey.getDialectUri();
                org.wso2.carbon.identity.claim.metadata.mgt.model.Claim claim;

                if (LOCAL_CLAIM_DIALECT_URI.equals(claimDialectURI)) {
                    claim = createLocalClaim(claimKey, claimMapping,
                            filterClaimProperties(claimConfig.getPropertyHolderMap().get(claimKey)));
                    claims.computeIfAbsent(claimDialectURI, k -> new ArrayList<>()).add(claim);
                } else {
                    /*
                     * If schemas.profile config is defined in the identity.xml, then attributes can be added to and
                     * removed from the default schemas as defined in schemas.xml file.
                     */
                    Map<String, String> removalsMap = DialectConfigParser.getInstance().getRemovalsFromDefaultDialects();
                    String removalDialect = removalsMap.get(claimKey.getClaimUri());
                    if (removalDialect == null || !removalDialect.equals(claimKey.getDialectUri())) {
                        claim = createExternalClaim(claimKey, claimConfig.getPropertyHolderMap().get(claimKey));
                        claims.computeIfAbsent(claimDialectURI, k -> new ArrayList<>()).add(claim);
                    }

                    Map<String, String> additionsMap = DialectConfigParser.getInstance().getAdditionsToDefaultDialects();
                    String newDialectUri = additionsMap.get(claimKey.getClaimUri());
                    if (newDialectUri != null) {
                        ClaimKey newClaimKey = new ClaimKey(claimKey.getClaimUri(), newDialectUri);
                        Map<String, String> newClaimProperties = new HashMap<>(
                                claimConfig.getPropertyHolderMap().computeIfAbsent(claimKey, k -> new HashMap<>())
                        );
                        newClaimProperties.put(ClaimConstants.DIALECT_PROPERTY, newDialectUri);
                        claim = createExternalClaim(newClaimKey, newClaimProperties);
                        claims.computeIfAbsent(newClaimKey.getDialectUri(), k -> new ArrayList<>()).add(claim);
                    }
                }
            }
        }
        return claims;
    }

    public static Map<String, String> filterClaimProperties(Map<String, String> claimProperties) {

        claimProperties.remove(ClaimConstants.DIALECT_PROPERTY);
        claimProperties.remove(ClaimConstants.CLAIM_URI_PROPERTY);
        claimProperties.remove(ClaimConstants.ATTRIBUTE_ID_PROPERTY);
        claimProperties.remove(ClaimConstants.IS_SYSTEM_CLAIM);

        claimProperties.putIfAbsent(ClaimConstants.DISPLAY_NAME_PROPERTY, "0");
        claimProperties.computeIfPresent(ClaimConstants.SUPPORTED_BY_DEFAULT_PROPERTY,
                (k, v) -> StringUtils.isBlank(v) ? "true" : v);
        claimProperties.computeIfPresent(ClaimConstants.READ_ONLY_PROPERTY,
                (k, v) -> StringUtils.isBlank(v) ? "true" : v);
        claimProperties.computeIfPresent(ClaimConstants.REQUIRED_PROPERTY,
                (k, v) -> StringUtils.isBlank(v) ? "true" : v);
        return claimProperties;
    }

    private static LocalClaim createLocalClaim(ClaimKey claimKey, ClaimMapping claimMapping,
                                               Map<String, String> claimProperties) {

        String primaryDomainName = IdentityUtil.getPrimaryDomainName();
        List<AttributeMapping> mappedAttributes = new ArrayList<>();
        if (StringUtils.isNotBlank(claimMapping.getMappedAttribute())) {
            mappedAttributes
                    .add(new AttributeMapping(primaryDomainName, claimMapping.getMappedAttribute()));
        }

        if (claimMapping.getMappedAttributes() != null) {
            for (Map.Entry<String, String> claimMappingEntry : claimMapping.getMappedAttributes()
                    .entrySet()) {
                mappedAttributes.add(new AttributeMapping(claimMappingEntry.getKey(),
                        claimMappingEntry.getValue()));
            }
        }
        return new LocalClaim(claimKey.getClaimUri(), mappedAttributes, claimProperties);
    }

    private static ExternalClaim createExternalClaim(ClaimKey claimKey, Map<String, String> claimProperties) {

        String mappedLocalClaimURI = claimProperties.get(ClaimConstants.MAPPED_LOCAL_CLAIM_PROPERTY);
        Map<String, String> filteredClaimProperties = filterClaimProperties(claimProperties);
        return new ExternalClaim(claimKey.getDialectUri(), claimKey.getClaimUri(),
                mappedLocalClaimURI, filteredClaimProperties);
    }

    /**
     * Retrieves the server-level uniqueness validation scope for claims based on configuration.
     *
     * @return Enum value of ClaimConstants.ClaimUniquenessScope indicating the server-level uniqueness scope.
     * Returns WITHIN_USERSTORE if the configuration is set to restrict uniqueness within the user store;
     * otherwise, returns ACROSS_USERSTORES.
     */
    public static ClaimConstants.ClaimUniquenessScope getServerLevelClaimUniquenessScope() {

        boolean isScopeWithinUserstore = Boolean.parseBoolean(IdentityUtil.getProperty(UNIQUENESS_VALIDATION_SCOPE));

        return isScopeWithinUserstore ? ClaimConstants.ClaimUniquenessScope.WITHIN_USERSTORE :
                ClaimConstants.ClaimUniquenessScope.ACROSS_USERSTORES;
    }

    /**
     * Retrieves the allowed claim profiles.
     *
     * @return Set of allowed claim profiles.
     */
    public static Set<String> getAllowedClaimProfiles() {

        Map<String, String> uniqueProfilesMap = new HashMap<>();
        Arrays.stream(ClaimConstants.DefaultAllowedClaimProfile.values())
                .map(ClaimConstants.DefaultAllowedClaimProfile::getProfileName)
                .forEach(profile -> uniqueProfilesMap.put(profile.toLowerCase(), profile));

        String serverWideClaimProfiles = IdentityUtil.getProperty(ClaimConstants.ALLOWED_ATTRIBUTE_PROFILE_CONFIG);
        if (StringUtils.isNotBlank(serverWideClaimProfiles)) {
            String[] profiles = serverWideClaimProfiles.split(",");
            Arrays.stream(profiles).map(String::trim).filter(StringUtils::isNotBlank)
                    .forEach(profile -> uniqueProfilesMap.putIfAbsent(profile.toLowerCase(), profile));
        }

        return Collections.unmodifiableSet(new HashSet<>(uniqueProfilesMap.values()));
    }

    /**
     * Check whether the configured identity data store is user store based. Then all identity claim data will be
     * stored in the user store.
     *
     * @return True if the configured identity data store is user store based. Else false.
     */
    public static boolean isUserStoreBasedIdentityDataStore() {

        log.debug("Checking whether the configured identity data store is user store based.");
        return StringUtils.equalsIgnoreCase(DEFAULT_USER_STORE_BASED_IDENTITY_DATA_STORE,
                IdentityUtil.getProperty(IDENTITY_DATA_STORE_TYPE));
    }

    /**
     * Check whether the given local claim is an identity claim.
     *
     * @param localClaim Local claim to be checked.
     * @return True if the given local claim is an identity claim. Else false.
     */
    public static boolean isIdentityClaim(LocalClaim localClaim) {

        if (log.isDebugEnabled()) {
            log.debug(String.format("Checking whether the claim: %s is an identity claim.", localClaim.getClaimURI()));
        }
        return StringUtils.startsWith(localClaim.getClaimURI(),
                UserCoreConstants.ClaimTypeURIs.IDENTITY_CLAIM_URI_PREFIX);
    }

    /**
     * Check if a set contains a string in a case-insensitive manner.
     *
     * @param set    Set of strings to search.
     * @param value  Value to find.
     * @return True if the set contains the value (case-insensitive), false otherwise.
     */
    public static boolean containsIgnoreCase(Set<String> set, String value) {

        return set.stream().anyMatch(item -> item.equalsIgnoreCase(value));
    }
}
