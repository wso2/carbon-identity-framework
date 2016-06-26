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
import org.wso2.carbon.identity.claim.metadata.mgt.dao.ClaimDialectDAO;
import org.wso2.carbon.identity.claim.metadata.mgt.dao.ExternalClaimDAO;
import org.wso2.carbon.identity.claim.metadata.mgt.dao.LocalClaimDAO;
import org.wso2.carbon.identity.claim.metadata.mgt.exception.ClaimMetadataException;
import org.wso2.carbon.identity.claim.metadata.mgt.internal.IdentityClaimManagementServiceDataHolder;
import org.wso2.carbon.identity.claim.metadata.mgt.model.AttributeMapping;
import org.wso2.carbon.identity.claim.metadata.mgt.model.ClaimDialect;
import org.wso2.carbon.identity.claim.metadata.mgt.model.ExternalClaim;
import org.wso2.carbon.identity.claim.metadata.mgt.model.LocalClaim;
import org.wso2.carbon.identity.claim.metadata.mgt.util.ClaimConstants;
import org.wso2.carbon.user.api.Claim;
import org.wso2.carbon.user.api.ClaimMapping;
import org.wso2.carbon.user.api.UserRealm;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.core.UserCoreConstants;
import org.wso2.carbon.user.core.claim.inmemory.ClaimConfig;
import org.wso2.carbon.user.core.listener.ClaimManagerListener;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Default implementation of {@link org.wso2.carbon.identity.claim.metadata.mgt.ClaimMetadataStore} interface.
 */
public class DefaultClaimMetadataStore implements ClaimMetadataStore {

    private static final Log log = LogFactory.getLog(DefaultClaimMetadataStore.class);

    private ClaimDialectDAO claimDialectDAO = new ClaimDialectDAO();
    private LocalClaimDAO localClaimDAO = new LocalClaimDAO();
    private ExternalClaimDAO externalClaimDAO = new ExternalClaimDAO();

    ClaimConfig claimConfig;
    int tenantId;

    DefaultClaimMetadataStore(ClaimConfig claimConfig, int tenantId) {

        try {
            if (claimDialectDAO.getClaimDialects(tenantId).size() == 0) {
                init(claimConfig, tenantId);
            }
        } catch (ClaimMetadataException e) {
            log.error("Error while retrieving claim dialects", e);
        }

        this.tenantId = tenantId;
    }

    private void init(ClaimConfig claimConfig, int tenantId) {

        // Adding local claim dialect
        try {
            claimDialectDAO.addClaimDialect(new ClaimDialect(ClaimConstants.LOCAL_CLAIM_DIALECT_URI), tenantId);
        } catch (ClaimMetadataException e) {
            log.error("Error while adding claim dialect " + ClaimConstants.LOCAL_CLAIM_DIALECT_URI, e);
        }


        if (claimConfig.getClaims() != null) {

            // Adding local claims
            String primaryDomainName = null;
            UserRealm realm;
            try {
                realm = IdentityClaimManagementServiceDataHolder.getInstance().getRealmService()
                        .getTenantUserRealm(tenantId);
                primaryDomainName = realm.getRealmConfiguration().getUserStoreProperty
                        (UserCoreConstants.RealmConfig.PROPERTY_DOMAIN_NAME);
            } catch (UserStoreException e) {
                log.error("Error while retrieving primary userstore domain name", e);
            }

            for (Map.Entry<String, org.wso2.carbon.user.core.claim.ClaimMapping> entry : claimConfig.getClaims()
                    .entrySet()) {

                String claimURI = entry.getKey();
                org.wso2.carbon.user.core.claim.ClaimMapping claimMapping = entry.getValue();
                String claimDialectURI = claimMapping.getClaim().getDialectURI();

                if (ClaimConstants.LOCAL_CLAIM_DIALECT_URI.equalsIgnoreCase(claimDialectURI)) {

                    List<AttributeMapping> mappedAttributes = new ArrayList<>();
                    mappedAttributes.add(new AttributeMapping(primaryDomainName, claimMapping.getMappedAttribute()));

                    Map<String, String> claimProperties = claimConfig.getPropertyHolder().get(claimURI);

                    LocalClaim localClaim = new LocalClaim(claimURI, mappedAttributes, claimProperties);

                    try {
                        localClaimDAO.addLocalClaim(localClaim, tenantId);
                    } catch (ClaimMetadataException e) {
                        log.error("Error while adding local claim " + claimURI, e);
                    }

                }
            }

            // Adding external dialects and claims
            List<ClaimDialect> claimDialectList = new ArrayList<>();

            for (Map.Entry<String, org.wso2.carbon.user.core.claim.ClaimMapping> entry : claimConfig.getClaims()
                    .entrySet()) {

                String claimURI = entry.getKey();
                String claimDialectURI = entry.getValue().getClaim().getDialectURI();

                if (!ClaimConstants.LOCAL_CLAIM_DIALECT_URI.equalsIgnoreCase(claimDialectURI)) {

                    // Add external dialect
                    if (claimDialectList.indexOf(claimDialectURI) == -1) {
                        ClaimDialect claimDialect = new ClaimDialect(claimDialectURI);
                        try {
                            claimDialectDAO.addClaimDialect(claimDialect, tenantId);
                            claimDialectList.add(claimDialect);
                        } catch (ClaimMetadataException e) {
                            log.error("Error while adding claim dialect " + claimDialectURI, e);
                            continue;
                        }
                    }

                    // Add external claim
                    String mappedLocalClaimURI = claimConfig.getPropertyHolder().get(claimURI).get(ClaimConstants
                            .MAPPED_LOCAL_CLAIM_PROPERTY);
                    ExternalClaim externalClaim = new ExternalClaim(claimDialectURI, claimURI, mappedLocalClaimURI);

                    try {
                        externalClaimDAO.addExternalClaim(externalClaim, tenantId);
                    } catch (ClaimMetadataException e) {
                        log.error("Error while adding external claim " + claimURI + " to dialect " + claimDialectURI,
                                e);
                    }

                }
            }
        }

    }

    @Override
    public String[] getAllClaimUris() throws UserStoreException {

        String[] localClaims;

        for (ClaimManagerListener listener : IdentityClaimManagementServiceDataHolder.getClaimManagerListeners()) {
            if (!listener.getAllClaimUris()) {
                // TODO : WTH???
                return null;
            }
        }

        try {

            List<LocalClaim> localClaimList = this.localClaimDAO.getLocalClaims(tenantId);

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
                // TODO : WTH???
                return null;
            }
        }

        try {
            // Add listener

            List<LocalClaim> localClaimList = this.localClaimDAO.getLocalClaims(tenantId);

            // Add listener

            for (LocalClaim localClaim : localClaimList) {
                if (localClaim.getClaimURI().equalsIgnoreCase(claimURI)) {
                    String mappedAttribute = localClaim.getMappedAttribute(domainName);

                    if (StringUtils.isBlank(mappedAttribute)) {
                        mappedAttribute = localClaim.getClaimProperty(ClaimConstants.DEFAULT_ATTRIBUTE);
                    }

                    if (StringUtils.isBlank(mappedAttribute)) {
                        UserRealm realm = IdentityClaimManagementServiceDataHolder.getInstance().getRealmService()
                                .getTenantUserRealm(tenantId);
                        String primaryDomainName = realm.getRealmConfiguration().getUserStoreProperty
                                (UserCoreConstants.RealmConfig.PROPERTY_DOMAIN_NAME);
                        mappedAttribute = localClaim.getMappedAttribute(primaryDomainName);
                    }

                    if (StringUtils.isNotBlank(mappedAttribute)) {
                        throw new IllegalStateException("Cannot find suitable mapped attribute for local claim " +
                                claimURI);
                    }

                    return mappedAttribute;
                }
            }


            throw new IllegalStateException("Invalid local claim URI : " + claimURI);

        } catch (ClaimMetadataException e) {
            throw new UserStoreException(e.getMessage(), e);
        }
    }

    @Override
    @Deprecated
    public String getAttributeName(String claimURI) throws UserStoreException {

//        UserRealm realm = IdentityClaimManagementServiceDataHolder.getInstance().getRealmService()
//                .getTenantUserRealm(tenantId);
//        String primaryDomainName = realm.getRealmConfiguration().getUserStoreProperty
//                (UserCoreConstants.RealmConfig.PROPERTY_DOMAIN_NAME);
//        return getAttributeName(primaryDomainName, claimURI);

        Thread.dumpStack();
        throw new UnsupportedOperationException("Deprecated operation in ClaimMetadataStore");
    }

    @Override
    @Deprecated
    public Claim getClaim(String claimURI) throws UserStoreException {

        Thread.dumpStack();
//        throw new UnsupportedOperationException("Deprecated operation in ClaimMetadataStore");

        // TODO : Less Important -> Only one usage in AUSM getUserClaimValues() to get display name property
        // TODO : displayTag = claimManager.getClaim(entry.getKey()).getDisplayTag();
        return new Claim();
    }

    @Override
    @Deprecated
    public ClaimMapping getClaimMapping(String claimURI) throws UserStoreException {

        Thread.dumpStack();
        throw new UnsupportedOperationException("Deprecated operation in ClaimMetadataStore");
    }

    @Override
    @Deprecated
    public ClaimMapping[] getAllClaimMappings(String dialectUri) throws UserStoreException {
        Thread.dumpStack();
        throw new UnsupportedOperationException("Deprecated operation in ClaimMetadataStore");
    }

    @Override
    @Deprecated
    public ClaimMapping[] getAllClaimMappings() throws UserStoreException {

        Thread.dumpStack();
//        throw new UnsupportedOperationException("Deprecated operation in ClaimMetadataStore");

        // TODO : Update profile management feature and remove this
        return new ClaimMapping[0];
    }

    @Override
    @Deprecated
    public void addNewClaimMapping(ClaimMapping claimMapping) throws UserStoreException {
        Thread.dumpStack();
        throw new UnsupportedOperationException("ClaimMetadataStore does not supports management operations");
    }

    @Override
    @Deprecated
    public void deleteClaimMapping(ClaimMapping claimMapping) throws UserStoreException {
        Thread.dumpStack();
        throw new UnsupportedOperationException("ClaimMetadataStore does not supports management operations");
    }

    @Override
    @Deprecated
    public void updateClaimMapping(ClaimMapping claimMapping) throws UserStoreException {
        Thread.dumpStack();
        throw new UnsupportedOperationException("ClaimMetadataStore does not supports management operations");
    }

    @Override
    @Deprecated
    public ClaimMapping[] getAllSupportClaimMappingsByDefault() throws UserStoreException {

        Thread.dumpStack();
//        throw new UnsupportedOperationException("ClaimMetadataStore does not supports management operations");

        // TODO : Update profile management feature and remove this
        log.info(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> getAllSupportClaimMappingsByDefault()");
        Map<String, org.wso2.carbon.user.core.claim.ClaimMapping> claimMappingMap = claimConfig.getClaims();

        Collection<org.wso2.carbon.user.core.claim.ClaimMapping> claimMappingSet = claimMappingMap.values();
        return claimMappingSet.toArray(new org.wso2.carbon.user.core.claim.ClaimMapping[claimMappingSet.size()]);
    }

    @Override
    @Deprecated
    public ClaimMapping[] getAllRequiredClaimMappings() throws UserStoreException {
        Thread.dumpStack();
        throw new UnsupportedOperationException("ClaimMetadataStore does not supports management operations");
    }
}
