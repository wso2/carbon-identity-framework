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

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.claim.metadata.mgt.exception.ClaimMetadataException;
import org.wso2.carbon.identity.claim.metadata.mgt.internal.IdentityClaimManagementServiceDataHolder;
import org.wso2.carbon.identity.claim.metadata.mgt.internal.ReadWriteClaimMetadataManager;
import org.wso2.carbon.identity.claim.metadata.mgt.model.ClaimDialect;
import org.wso2.carbon.identity.claim.metadata.mgt.model.ExternalClaim;
import org.wso2.carbon.identity.claim.metadata.mgt.model.LocalClaim;
import org.wso2.carbon.identity.claim.metadata.mgt.util.ClaimConstants;
import org.wso2.carbon.identity.claim.metadata.mgt.util.ClaimMetadataUtils;
import org.wso2.carbon.identity.core.util.IdentityUtil;
import org.wso2.carbon.user.api.Claim;
import org.wso2.carbon.user.api.ClaimMapping;
import org.wso2.carbon.user.api.UserRealm;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.core.UserCoreConstants;
import org.wso2.carbon.user.core.claim.inmemory.ClaimConfig;
import org.wso2.carbon.user.core.listener.ClaimManagerListener;

import java.util.ArrayList;
import java.util.List;

import static org.wso2.carbon.identity.base.IdentityConstants.ServerConfig.SKIP_CLAIM_METADATA_PERSISTENCE;

/**
 * Default implementation of {@link org.wso2.carbon.identity.claim.metadata.mgt.ClaimMetadataStore} interface.
 */
public class DefaultClaimMetadataStore implements ClaimMetadataStore {

    private static final Log log = LogFactory.getLog(DefaultClaimMetadataStore.class);

    private final UnifiedClaimMetadataManager unifiedClaimMetadataManager = new CacheBackedUnifiedClaimMetadataManager();

    private int tenantId;

    public static DefaultClaimMetadataStore getInstance(int tenantId) {
        ClaimConfig claimConfig = new ClaimConfig();
        return new DefaultClaimMetadataStore(claimConfig, tenantId);
    }

    public DefaultClaimMetadataStore(ClaimConfig claimConfig, int tenantId) {

        try {
            ReadWriteClaimMetadataManager dbBasedClaimMetadataManager = new DBBasedClaimMetadataManager();
            if (!skipClaimMetadataPersistence() && dbBasedClaimMetadataManager.getClaimDialects(tenantId).isEmpty()) {
                IdentityClaimManagementServiceDataHolder.getInstance().getClaimConfigInitDAO()
                        .initClaimConfig(claimConfig, tenantId);
            }
        } catch (ClaimMetadataException e) {
            log.error("Error while retrieving claim dialects", e);
        }

        this.tenantId = tenantId;
    }

    @Override
    public String[] getAllClaimUris() throws UserStoreException {

        String[] localClaims;

        for (ClaimManagerListener listener : IdentityClaimManagementServiceDataHolder.getClaimManagerListeners()) {
            if (!listener.getAllClaimUris()) {
                // TODO : Add listener handling impl
                return null;
            }
        }

        try {

            List<LocalClaim> localClaimList = this.unifiedClaimMetadataManager.getLocalClaims(tenantId);

            localClaims = new String[localClaimList.size()];

            int i = 0;
            for (LocalClaim localClaim : localClaimList) {
                localClaims[i] = localClaim.getClaimURI();
                i++;
            }
        } catch (ClaimMetadataException e) {
            throw new UserStoreException(e.getMessage(), e);
        }

        // Add listener??

        return localClaims;
    }

    @Override
    public String getAttributeName(String domainName, String claimURI) throws UserStoreException {

        if (StringUtils.isBlank(domainName)) {
            throw new IllegalArgumentException("User store domain name parameter cannot be empty");
        }

        if (StringUtils.isBlank(claimURI)) {
            throw new IllegalArgumentException("Local claim URI parameter cannot be empty");
        }


        for (ClaimManagerListener listener : IdentityClaimManagementServiceDataHolder.getClaimManagerListeners()) {
            if (!listener.getAttributeName(domainName, claimURI)) {
                // TODO : Add listener handling impl
                return null;
            }
        }

        try {
            // Add listener

            List<LocalClaim> localClaimList = this.unifiedClaimMetadataManager.getLocalClaims(tenantId);

            // Add listener

            for (LocalClaim localClaim : localClaimList) {
                if (localClaim.getClaimURI().equalsIgnoreCase(claimURI)) {
                    return getMappedAttribute(domainName, localClaim, tenantId);
                }
            }


            // For backward compatibility
            List<ClaimDialect> claimDialects = unifiedClaimMetadataManager.getClaimDialects(tenantId);

            for (ClaimDialect claimDialect : claimDialects) {
                if (ClaimConstants.LOCAL_CLAIM_DIALECT_URI.equalsIgnoreCase(claimDialect.getClaimDialectURI())) {
                    continue;
                }

                List<ExternalClaim> externalClaims = unifiedClaimMetadataManager.getExternalClaims(claimDialect
                        .getClaimDialectURI(), tenantId);

                for (ExternalClaim externalClaim : externalClaims) {
                    if (externalClaim.getClaimURI().equalsIgnoreCase(claimURI)) {

                        for (LocalClaim localClaim : localClaimList) {
                            if (localClaim.getClaimURI().equalsIgnoreCase(externalClaim.getMappedLocalClaim())) {
                                if (log.isDebugEnabled()) {
                                    log.debug("Picking mapped attribute for external claim : " + externalClaim
                                            .getClaimURI() + " using mapped local claim : " + localClaim.getClaimURI());
                                }
                                return getMappedAttribute(domainName, localClaim, tenantId);
                            }
                        }

                    }
                }
            }

            if (log.isDebugEnabled()) {
                log.debug("Returning NULL for getAttributeName() for domain : " + domainName + ", claim URI : " +
                        claimURI);
            }

            return null;

        } catch (ClaimMetadataException e) {
            throw new UserStoreException(e.getMessage(), e);
        }
    }

    private String getMappedAttribute(String domainName, LocalClaim localClaim, int tenantId) throws
            UserStoreException {

        String mappedAttribute = localClaim.getMappedAttribute(domainName);

        if (StringUtils.isNotBlank(mappedAttribute)) {
            if (log.isDebugEnabled()) {
                log.debug("Assigned mapped attribute : " + mappedAttribute + " from user store domain : " + domainName +
                        " for claim : " + localClaim.getClaimURI() + " in tenant : " + tenantId);
            }

            return mappedAttribute;
        }

        mappedAttribute = localClaim.getClaimProperty(ClaimConstants.DEFAULT_ATTRIBUTE);

        if (StringUtils.isNotBlank(mappedAttribute)) {

            if (log.isDebugEnabled()) {
                log.debug("Assigned mapped attribute : " + mappedAttribute + " from " + ClaimConstants.DEFAULT_ATTRIBUTE +
                        " property for claim : " + localClaim.getClaimURI() + " in tenant : " + tenantId);
            }

            return mappedAttribute;
        }

        UserRealm realm = IdentityClaimManagementServiceDataHolder.getInstance().getRealmService()
                .getTenantUserRealm(tenantId);
        String primaryDomainName = realm.getRealmConfiguration().getUserStoreProperty
                (UserCoreConstants.RealmConfig.PROPERTY_DOMAIN_NAME);
        mappedAttribute = localClaim.getMappedAttribute(primaryDomainName);

        if (StringUtils.isNotBlank(mappedAttribute)) {
            if (log.isDebugEnabled()) {
                log.debug("Assigned mapped attribute : " + mappedAttribute + " from primary user store domain : " +
                        primaryDomainName + " for claim : " + localClaim.getClaimURI() + " in tenant : " + tenantId);
            }

            return mappedAttribute;
        } else {
            throw new IllegalStateException("Cannot find suitable mapped attribute for local claim " + localClaim
                    .getClaimURI());
        }
    }

    @Override
    @Deprecated
    public String getAttributeName(String claimURI) throws UserStoreException {

        UserRealm realm = IdentityClaimManagementServiceDataHolder.getInstance().getRealmService()
                .getTenantUserRealm(tenantId);
        String primaryDomainName = realm.getRealmConfiguration().getUserStoreProperty
                (UserCoreConstants.RealmConfig.PROPERTY_DOMAIN_NAME);
        return getAttributeName(primaryDomainName, claimURI);
    }

    @Override
    @Deprecated
    public Claim getClaim(String claimURI) throws UserStoreException {
        try {
            List<LocalClaim> localClaims = unifiedClaimMetadataManager.getLocalClaims(this.tenantId);

            for (LocalClaim localClaim : localClaims) {
                if (localClaim.getClaimURI().equalsIgnoreCase(claimURI)) {
                    ClaimMapping claimMapping = ClaimMetadataUtils.convertLocalClaimToClaimMapping(localClaim, this
                            .tenantId);
                    return claimMapping.getClaim();
                }
            }

            // For backward compatibility
            List<ClaimDialect> claimDialects = unifiedClaimMetadataManager.getClaimDialects(tenantId);

            for (ClaimDialect claimDialect : claimDialects) {
                if (ClaimConstants.LOCAL_CLAIM_DIALECT_URI.equalsIgnoreCase(claimDialect.getClaimDialectURI())) {
                    continue;
                }

                List<ExternalClaim> externalClaims = unifiedClaimMetadataManager.getExternalClaims(claimDialect
                        .getClaimDialectURI(), tenantId);

                for (ExternalClaim externalClaim : externalClaims) {
                    if (externalClaim.getClaimURI().equalsIgnoreCase(claimURI)) {

                        for (LocalClaim localClaim : localClaims) {
                            ClaimMapping claimMapping = ClaimMetadataUtils.convertLocalClaimToClaimMapping
                                    (localClaim, this.tenantId);
                            return claimMapping.getClaim();
                        }

                    }
                }
            }

            if (log.isDebugEnabled()) {
                log.debug("Returning NULL for getClaim() for claim URI : " + claimURI);
            }
            return null;
        } catch (ClaimMetadataException e) {
            throw new UserStoreException(e.getMessage(), e);
        }
    }

    @Override
    @Deprecated
    public ClaimMapping getClaimMapping(String claimURI) throws UserStoreException {
        try {
            List<LocalClaim> localClaims = unifiedClaimMetadataManager.getLocalClaims(this.tenantId);

            for (LocalClaim localClaim : localClaims) {
                if (localClaim.getClaimURI().equalsIgnoreCase(claimURI)) {
                    ClaimMapping claimMapping = ClaimMetadataUtils.convertLocalClaimToClaimMapping(localClaim, this
                            .tenantId);
                    return claimMapping;
                }
            }

            // For backward compatibility
            List<ClaimDialect> claimDialects = unifiedClaimMetadataManager.getClaimDialects(tenantId);

            for (ClaimDialect claimDialect : claimDialects) {
                if (ClaimConstants.LOCAL_CLAIM_DIALECT_URI.equalsIgnoreCase(claimDialect.getClaimDialectURI())) {
                    continue;
                }

                List<ExternalClaim> externalClaims = unifiedClaimMetadataManager.getExternalClaims(claimDialect
                        .getClaimDialectURI(), tenantId);

                for (ExternalClaim externalClaim : externalClaims) {
                    if (externalClaim.getClaimURI().equalsIgnoreCase(claimURI)) {

                        for (LocalClaim localClaim : localClaims) {
                            if (localClaim.getClaimURI().equalsIgnoreCase(externalClaim.getMappedLocalClaim())) {
                                ClaimMapping claimMapping = ClaimMetadataUtils.convertLocalClaimToClaimMapping
                                        (localClaim, this.tenantId);
                                return claimMapping;
                            }
                        }

                    }
                }
            }

            if (log.isDebugEnabled()) {
                log.debug("Returning NULL for getClaimMapping() for claim URI : " + claimURI);
            }
            return null;
        } catch (ClaimMetadataException e) {
            throw new UserStoreException(e.getMessage(), e);
        }
    }

    @Override
    @Deprecated
    public ClaimMapping[] getAllClaimMappings(String dialectUri) throws UserStoreException {

        if (ClaimConstants.LOCAL_CLAIM_DIALECT_URI.equalsIgnoreCase(dialectUri)) {
            try {
                List<LocalClaim> localClaims = unifiedClaimMetadataManager.getLocalClaims(this.tenantId);

                List<ClaimMapping> claimMappings = new ArrayList<>();

                for (LocalClaim localClaim : localClaims) {
                    if (isFilterableClaim(localClaim)) {
                        continue;
                    }

                    ClaimMapping claimMapping = ClaimMetadataUtils.convertLocalClaimToClaimMapping(localClaim, this
                            .tenantId);
                    claimMappings.add(claimMapping);
                }

                return claimMappings.toArray(new ClaimMapping[0]);
            } catch (ClaimMetadataException e) {
                throw new UserStoreException(e.getMessage(), e);
            }
        } else {
            try {
                List<ExternalClaim> externalClaims = unifiedClaimMetadataManager.getExternalClaims(dialectUri,
                        this.tenantId);
                List<LocalClaim> localClaims = unifiedClaimMetadataManager.getLocalClaims(this.tenantId);

                List<ClaimMapping> claimMappings = new ArrayList<>();

                for (ExternalClaim externalClaim : externalClaims) {
                    ClaimMapping claimMapping = ClaimMetadataUtils.convertExternalClaimToClaimMapping(externalClaim,
                            localClaims, this.tenantId);
                    claimMappings.add(claimMapping);
                }

                return claimMappings.toArray(new ClaimMapping[0]);
            } catch (ClaimMetadataException e) {
                throw new UserStoreException(e.getMessage(), e);
            }

        }
    }

    @Override
    @Deprecated
    public ClaimMapping[] getAllClaimMappings() throws UserStoreException {

        return getAllClaimMappings(ClaimConstants.LOCAL_CLAIM_DIALECT_URI);
    }

    @Override
    @Deprecated
    public void addNewClaimMapping(ClaimMapping claimMapping) throws UserStoreException {
        throw new UnsupportedOperationException("ClaimMetadataStore does not supports management operations");
    }

    @Override
    @Deprecated
    public void deleteClaimMapping(ClaimMapping claimMapping) throws UserStoreException {
        throw new UnsupportedOperationException("ClaimMetadataStore does not supports management operations");
    }

    @Override
    @Deprecated
    public void updateClaimMapping(ClaimMapping claimMapping) throws UserStoreException {
        throw new UnsupportedOperationException("ClaimMetadataStore does not supports management operations");
    }

    @Override
    @Deprecated
    public ClaimMapping[] getAllSupportClaimMappingsByDefault() throws UserStoreException {

        try {
            List<LocalClaim> localClaims = unifiedClaimMetadataManager.getLocalClaims(this.tenantId);

            List<ClaimMapping> claimMappings = new ArrayList<>();

            for (LocalClaim localClaim : localClaims) {
                if (isFilterableClaim(localClaim)) {
                    continue;
                }

                ClaimMapping claimMapping = ClaimMetadataUtils.convertLocalClaimToClaimMapping(localClaim, this
                        .tenantId);

                if (claimMapping.getClaim().isSupportedByDefault()) {
                    claimMappings.add(claimMapping);
                }
            }

            return claimMappings.toArray(new ClaimMapping[0]);
        } catch (ClaimMetadataException e) {
            throw new UserStoreException(e.getMessage(), e);
        }
    }

    @Override
    @Deprecated
    public ClaimMapping[] getAllRequiredClaimMappings() throws UserStoreException {

        try {
            List<LocalClaim> localClaims = unifiedClaimMetadataManager.getLocalClaims(this.tenantId);

            List<ClaimMapping> claimMappings = new ArrayList<>();

            for (LocalClaim localClaim : localClaims) {
                ClaimMapping claimMapping = ClaimMetadataUtils.convertLocalClaimToClaimMapping(localClaim, this
                        .tenantId);

                if (claimMapping.getClaim().isRequired()) {
                    claimMappings.add(claimMapping);
                }
            }

            return claimMappings.toArray(new ClaimMapping[0]);
        } catch (ClaimMetadataException e) {
            throw new UserStoreException(e.getMessage(), e);
        }
    }

    private boolean isFilterableClaim(LocalClaim localClaim) {

        // Filter the local claim `role` when groups vs roles separation is enabled. This claim is
        // considered as a legacy claim going forward, thus `roles` and `groups` claims should be used
        // instead.
        if (IdentityUtil.isGroupsVsRolesSeparationImprovementsEnabled() &&
                !IdentityUtil.isShowLegacyRoleClaimOnGroupRoleSeparationEnabled() && UserCoreConstants.ROLE_CLAIM.
                equals(localClaim.getClaimURI())) {
            if (log.isDebugEnabled()) {
                log.debug("Skipping the legacy role claim: " + localClaim.getClaimURI() + ", when getting " +
                        "local claims");
            }
            return true;
        }

        return false;
    }

    private boolean skipClaimMetadataPersistence() {

        return Boolean.parseBoolean(IdentityUtil.getProperty(SKIP_CLAIM_METADATA_PERSISTENCE));
    }
}
