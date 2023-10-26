/*
 * Copyright (c) 2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
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
 *
 */

package org.wso2.carbon.claim.mgt;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.CarbonException;
import org.wso2.carbon.claim.mgt.internal.ClaimManagementServiceComponent;
import org.wso2.carbon.core.util.AdminServicesUtil;
import org.wso2.carbon.core.util.AnonymousSessionUtil;
import org.wso2.carbon.identity.claim.metadata.mgt.ClaimMetadataManagementServiceImpl;
import org.wso2.carbon.identity.claim.metadata.mgt.exception.ClaimMetadataException;
import org.wso2.carbon.identity.claim.metadata.mgt.model.ExternalClaim;
import org.wso2.carbon.identity.claim.metadata.mgt.model.LocalClaim;
import org.wso2.carbon.user.api.Claim;
import org.wso2.carbon.user.api.ClaimMapping;
import org.wso2.carbon.user.core.UserCoreConstants;
import org.wso2.carbon.user.core.UserRealm;
import org.wso2.carbon.user.api.ClaimManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public enum ClaimManagerHandler {

    INSTANCE;

    private static final Log log = LogFactory.getLog(ClaimManagerHandler.class);
    // Maintains a single instance of UserStore.

    public static ClaimManagerHandler getInstance() {
        if (log.isDebugEnabled()) {
            log.debug("ClaimManagerHandler singleton instance created successfully");
        }
        return INSTANCE;
    }

    /**
     * Returns all supported claims.
     *
     * @return
     * @throws ClaimManagementException
     */
    public Claim[] getAllSupportedClaims() throws ClaimManagementException {
        ClaimManager claimManager;

        try {
            UserRealm realm = getRealm();
            claimManager = realm.getClaimManager();
            if (claimManager != null) {
                // There can be cases - we get a request for an external user
                // store - where we don'
                // have a claims administrator.
                ClaimMapping[] mappings = realm.getClaimManager()
                        .getAllSupportClaimMappingsByDefault();
                Claim[] claims = new Claim[mappings.length];

                for (int i = 0; i < mappings.length; i++) {
                    claims[i] = mappings[i].getClaim();
                }

                return claims;
            }
        } catch (org.wso2.carbon.user.api.UserStoreException e) {
            throw new ClaimManagementException("Error occurred while loading supported claims", e);
        }

        return new Claim[0];
    }

    /**
     * @return
     * @throws ClaimManagementException
     */
    public ClaimMapping[] getAllSupportedClaimMappings() throws ClaimManagementException {
        ClaimManager claimManager;

        try {
            UserRealm realm = getRealm();
            claimManager = realm.getClaimManager();
            if (claimManager == null) {
                // There can be cases - we get a request for an external user store - where we don'
                // have a claims administrator.
                return new ClaimMapping[0];
            }

            return claimManager.getAllSupportClaimMappingsByDefault();

        } catch (org.wso2.carbon.user.api.UserStoreException e) {
            throw new ClaimManagementException("Error occurred while loading supported claims", e);
        }
    }

    /**
     * @return
     * @throws ClaimManagementException
     */
    public ClaimMapping[] getAllClaimMappings() throws ClaimManagementException {
        ClaimManager claimManager;

        try {
            UserRealm realm = getRealm();
            claimManager = realm.getClaimManager();
            if (claimManager == null) {
                // There can be cases - we get a request for an external user store - where we don'
                // have a claims administrator.
                return new ClaimMapping[0];
            }

            return claimManager.getAllClaimMappings();

        } catch (org.wso2.carbon.user.api.UserStoreException e) {
            throw new ClaimManagementException("Error occurred while loading supported claims", e);
        }
    }

    public ClaimMapping[] getAllClaimMappings(String tenantDomain) throws ClaimManagementException {

        try {
            int tenantId =
                    ClaimManagementServiceComponent.getRealmService().getTenantManager().getTenantId(tenantDomain);
            ClaimManager claimManager =
                    ClaimManagementServiceComponent.getRealmService().getTenantUserRealm(tenantId).getClaimManager();
            if (claimManager == null) {
                // There can be cases - we get a request for an external user store - where we don'
                // have a claims administrator.
                return new ClaimMapping[0];
            }

            return claimManager.getAllClaimMappings();

        } catch (org.wso2.carbon.user.api.UserStoreException e) {
            throw new ClaimManagementException("Error occurred while loading claims mapping for tenant "
                                               + tenantDomain, e);
        }

    }

    public ClaimMapping[] getAllClaimMappings(String dialectURI, String tenantDomain)
            throws ClaimManagementException {

        try {
            int tenantId =
                    ClaimManagementServiceComponent.getRealmService().getTenantManager().getTenantId(tenantDomain);
            ClaimManager claimManager =
                    ClaimManagementServiceComponent.getRealmService().getTenantUserRealm(tenantId).getClaimManager();
            if (claimManager == null) {
                // There can be cases - we get a request for an external user store - where we don'
                // have a claims administrator.
                return new ClaimMapping[0];
            }

            return claimManager.getAllClaimMappings(dialectURI);

        } catch (org.wso2.carbon.user.api.UserStoreException e) {
            throw new ClaimManagementException("Error occurred while loading all claim mappings for tenant "
                                               + tenantDomain, e);
        }
    }

    public ClaimMapping getClaimMapping(String claimURI) throws ClaimManagementException {
        ClaimMapping claimMapping = null;
        ClaimManager claimManager;
        try {
            UserRealm realm = getRealm();
            claimManager = realm.getClaimManager();
            if (claimManager != null) {
                claimMapping = claimManager.getClaimMapping(claimURI);
            }
        } catch (org.wso2.carbon.user.api.UserStoreException e) {
            throw new ClaimManagementException("Error occurred while retrieving claim", e);
        }
        return claimMapping;
    }

    /**
     * @return
     * @throws ClaimManagementException
     */
    public ClaimMapping[] getAllSupportedClaimMappings(String dialectUri) throws ClaimManagementException {
        ClaimManager claimManager;
        try {
            UserRealm realm = getRealm();
            claimManager = realm.getClaimManager();
            if (claimManager == null) {
                // There can be cases - we get a request for an external user store - where we don'
                // have a claims administrator.
                return new ClaimMapping[0];
            }
            return claimManager.getAllClaimMappings(dialectUri);

        } catch (org.wso2.carbon.user.api.UserStoreException e) {
            throw new ClaimManagementException("Error occurred while loading supported claims", e);
        }
    }

    /**
     * Returns all supported claims for the given dialect.
     *
     * @return
     * @throws ClaimManagementException
     */
    public Claim[] getAllSupportedClaims(String dialectUri) throws ClaimManagementException {
        Claim[] claims = new Claim[0];
        List<Claim> reqClaims = null;
        ClaimManager claimManager = null;
        ClaimMapping[] mappings = null;

        try {
            UserRealm realm = getRealm();
            claimManager = realm.getClaimManager();
            if (claimManager == null) {
                // There can be cases - we get a request for an external user store - where we don'
                // have a claims administrator.
                return claims;
            }
            mappings = claimManager.getAllSupportClaimMappingsByDefault();
            reqClaims = new ArrayList<>();
            for (ClaimMapping mapping : mappings) {
                Claim claim = mapping.getClaim();
                if (dialectUri.equals(claim.getDialectURI())) {
                    reqClaims.add(claim);
                }
            }

            return reqClaims.toArray(new Claim[reqClaims.size()]);
        } catch (org.wso2.carbon.user.api.UserStoreException e) {
            throw new ClaimManagementException("Error occurred while loading supported claims from the dialect "
                                               + dialectUri, e);
        }
    }

    /**
     * @param mapping
     * @throws ClaimManagementException
     */
    public void updateClaimMapping(ClaimMapping mapping) throws ClaimManagementException {
        ClaimManager claimManager = null;
        try {
            UserRealm realm = getRealm();

            String primaryDomainName = realm.getRealmConfiguration().getUserStoreProperty(
                    UserCoreConstants.RealmConfig.PROPERTY_DOMAIN_NAME);

            if (primaryDomainName == null) {
                if (mapping.getMappedAttribute() == null) {
                    throw new ClaimManagementException("Attribute name cannot be null for the primary domain");
                }
            } else if (mapping.getMappedAttribute() == null) {
                String attr = mapping.getMappedAttribute(primaryDomainName);
                if (attr == null) {
                    throw new ClaimManagementException("Attribute name cannot be null for the primary domain");
                }
                mapping.setMappedAttribute(attr);
            }

            claimManager = realm.getClaimManager();
            if (claimManager != null) {
                // There can be cases - we get a request for an external user store - where we don'
                // have a claims administrator.
                claimManager.updateClaimMapping(mapping);
            }
        } catch (org.wso2.carbon.user.api.UserStoreException e) {
            throw new ClaimManagementException("Error occurred while updating claim mapping", e);

        }
    }

    /**
     * @param mapping
     * @throws ClaimManagementException
     */
    public void addNewClaimMapping(ClaimMapping mapping) throws ClaimManagementException {
        ClaimManager claimManager = null;
        try {
            UserRealm realm = getRealm();
            claimManager = realm.getClaimManager();
            if (claimManager != null) {
                // There can be cases - we get a request for an external user store - where we don'
                // have a claims administrator.
                claimManager.addNewClaimMapping(mapping);
            }
        } catch (org.wso2.carbon.user.api.UserStoreException e) {
            throw new ClaimManagementException("Error occurred while adding new claim mapping", e);

        }
    }

    /**
     * @param dialectUri
     * @param claimUri
     * @throws ClaimManagementException
     */
    public void removeClaimMapping(String dialectUri, String claimUri) throws ClaimManagementException {
        ClaimMapping mapping = null;
        Claim claim = null;
        ClaimManager claimManager = null;
        try {
            UserRealm realm = getRealm();
            claimManager = realm.getClaimManager();
            if (claimManager != null) {
                // There can be cases - we get a request for an external user store - where we don'
                // have a claims administrator.
                claim = new Claim();
                claim.setClaimUri(claimUri);
                claim.setDialectURI(dialectUri);
                mapping = new ClaimMapping(claim, null);
                claimManager.deleteClaimMapping(mapping);
            }
        } catch (org.wso2.carbon.user.api.UserStoreException e) {
            throw new ClaimManagementException("Error occurred while removing new claim mapping", e);
        }
    }

    /**
     * @param mappings
     */
    public void addNewClaimDialect(ClaimDialect mappings) throws ClaimManagementException {
        ClaimMapping[] mapping;
        ClaimManager claimManager;
        claimManager = null;
        try {
            UserRealm realm = getRealm();
            claimManager = realm.getClaimManager();
            if (claimManager != null) {
                mapping = mappings.getClaimMapping();
                for (ClaimMapping aMapping : mapping) {
                    claimManager.addNewClaimMapping(aMapping);
                }
            }
        } catch (org.wso2.carbon.user.api.UserStoreException e) {
            throw new ClaimManagementException("Error occurred while removing new claim mapping", e);
        }
    }

    /**
     * @param dialectUri
     * @throws ClaimManagementException
     */
    public void removeClaimDialect(String dialectUri) throws ClaimManagementException {
        ClaimMapping[] mapping;
        ClaimManager claimManager;
        try {
            UserRealm realm = getRealm();
            claimManager = realm.getClaimManager();
            if (claimManager != null) {
                mapping = claimManager.getAllClaimMappings(dialectUri);
                if (mapping != null) {
                    for (ClaimMapping aMapping : mapping) {
                        claimManager.deleteClaimMapping(aMapping);
                    }
                }
            }
        } catch (org.wso2.carbon.user.api.UserStoreException e) {
            throw new ClaimManagementException("Error occurred while removing new claim dialect", e);
        }
    }

    private UserRealm getRealm() throws ClaimManagementException {
        try {
            return AdminServicesUtil.getUserRealm();
        } catch (CarbonException e) {
            throw new ClaimManagementException("Error while trying get User Realm.", e);
        }
    }

    public Set<org.wso2.carbon.claim.mgt.ClaimMapping> getMappingsFromCarbonDialectToOther(
            String otherDialectURI, Set<String> carbonClaimURIs, String tenantDomain)
            throws ClaimManagementException {

        Set<org.wso2.carbon.claim.mgt.ClaimMapping> returnSet = new HashSet<>();

        if (UserCoreConstants.DEFAULT_CARBON_DIALECT.equals(otherDialectURI)) {
            for (String claimURI : carbonClaimURIs) {
                org.wso2.carbon.claim.mgt.ClaimMapping claimMapping = new org.wso2.carbon.claim.mgt.ClaimMapping(
                        otherDialectURI, claimURI, claimURI);

                returnSet.add(claimMapping);
            }
            return returnSet;
        }

        ClaimMapping[] claimMappingsInOtherDialect = getAllClaimMappings(otherDialectURI,
                                                                         tenantDomain);
        ClaimMapping[] allClaimMappingsInCarbonDialect = getAllClaimMappings(
                UserCoreConstants.DEFAULT_CARBON_DIALECT, tenantDomain);
        if (otherDialectURI == null) {
            String message = "Invalid argument: \'otherDialectURI\' is \'NULL\'";
            log.error(message);
            throw new ClaimManagementException(message);
        }
        if (carbonClaimURIs == null || carbonClaimURIs.isEmpty()) {
            String message = "Invalid argument: \'carbonClaimURIs\' is \'NULL\' or of zero length";
            log.error(message);
            throw new ClaimManagementException(message);
        }
        for (String requestedClaimURI : carbonClaimURIs) {
            if (allClaimMappingsInCarbonDialect != null
                && allClaimMappingsInCarbonDialect.length > 0) {
                for (ClaimMapping claimMapping : allClaimMappingsInCarbonDialect) {
                    if (requestedClaimURI.equals(claimMapping.getClaim().getClaimUri())) {
                        String mappedAttr = claimMapping.getMappedAttribute();
                        for (ClaimMapping carbonClaimMapping : claimMappingsInOtherDialect) {
                            if (mappedAttr.equals(carbonClaimMapping.getMappedAttribute())) {
                                returnSet.add(new org.wso2.carbon.claim.mgt.ClaimMapping(
                                        otherDialectURI, requestedClaimURI, carbonClaimMapping
                                        .getClaim().getClaimUri()));
                            }
                        }
                    }
                }
            }
        }
        return returnSet;
    }

    public Map<String, String> getMappingsMapFromCarbonDialectToOther(String otherDialectURI,
                                                                      Set<String> carbonClaimURIs, String
            tenantDomain) throws ClaimManagementException {

        Map<String, String> returnMap = new HashMap<>();
        Set<org.wso2.carbon.claim.mgt.ClaimMapping> mappings = getMappingsFromCarbonDialectToOther(
                otherDialectURI, carbonClaimURIs, tenantDomain);
        for (org.wso2.carbon.claim.mgt.ClaimMapping mapping : mappings) {
            returnMap.put(mapping.getCarbonClaimURI(), mapping.getNonCarbonClaimURI());
        }
        return returnMap;
    }

    /**
     * @param otherDialectURI
     * @param otherClaimURIs
     * @param tenantDomain
     * @return
     * @throws ClaimManagementException
     */
    public Set<org.wso2.carbon.claim.mgt.ClaimMapping> getMappingsFromOtherDialectToCarbon(
            String otherDialectURI, Set<String> otherClaimURIs, String tenantDomain)
            throws ClaimManagementException {

        Set<org.wso2.carbon.claim.mgt.ClaimMapping> returnSet = new HashSet<org.wso2.carbon.claim.mgt.ClaimMapping>();

        if (otherDialectURI == null) {
            String message = "Invalid argument: \'otherDialectURI\' is \'NULL\'";
            log.error(message);
            throw new ClaimManagementException(message);
        }


        try {
            ClaimMetadataManagementServiceImpl claimMetadataService = new ClaimMetadataManagementServiceImpl();

            if (otherDialectURI.equals(UserCoreConstants.DEFAULT_CARBON_DIALECT) ) {

                List<LocalClaim> localClaims = claimMetadataService.getLocalClaims(tenantDomain);

                if (otherClaimURIs == null|| otherClaimURIs.isEmpty()) {

                    for (LocalClaim localClaim : localClaims) {
                        org.wso2.carbon.claim.mgt.ClaimMapping claimMapping = new org.wso2.carbon.claim.mgt
                                .ClaimMapping(localClaim.getClaimDialectURI(), localClaim.getClaimURI(), localClaim
                                .getClaimURI());
                        returnSet.add(claimMapping);
                    }

                    return returnSet;

                } else {

                    for (LocalClaim localClaim : localClaims) {

                        if (otherClaimURIs.contains(localClaim.getClaimURI())) {

                            org.wso2.carbon.claim.mgt.ClaimMapping claimMapping = new org.wso2.carbon.claim.mgt.ClaimMapping(
                                    otherDialectURI, localClaim.getClaimURI(), localClaim.getClaimURI());
                            returnSet.add(claimMapping);

                        }
                    }

                    return returnSet;

                }

            } else {

                List<ExternalClaim> externalClaims = claimMetadataService.getExternalClaims(otherDialectURI,
                        tenantDomain);

                if (otherClaimURIs == null || otherClaimURIs.isEmpty()) {

                    for (ExternalClaim externalClaim : externalClaims) {

                        org.wso2.carbon.claim.mgt.ClaimMapping claimMapping = new org.wso2.carbon.claim.mgt.ClaimMapping
                                (externalClaim.getClaimDialectURI(), externalClaim.getClaimURI(), externalClaim
                                        .getMappedLocalClaim());
                        returnSet.add(claimMapping);
                    }

                } else {

                    for (ExternalClaim externalClaim : externalClaims) {

                        if (otherClaimURIs.contains(externalClaim.getClaimURI())) {

                            org.wso2.carbon.claim.mgt.ClaimMapping claimMapping = new org.wso2.carbon.claim.mgt.ClaimMapping
                                    (externalClaim.getClaimDialectURI(), externalClaim.getClaimURI(), externalClaim
                                            .getMappedLocalClaim());
                            returnSet.add(claimMapping);
                        }
                    }

                }

                return returnSet;
            }


        } catch (ClaimMetadataException e) {
            throw new ClaimManagementException(e.getMessage(), e);
        }
    }

    /**
     * @param otherDialectURI
     * @param otherClaimURIs
     * @param tenantDomain
     * @return
     * @throws Exception
     */
    public Map<String, String> getMappingsMapFromOtherDialectToCarbon(String otherDialectURI,
                                                                      Set<String> otherClaimURIs, String
            tenantDomain) throws ClaimManagementException {

        return getMappingsMapFromOtherDialectToCarbon(otherDialectURI, otherClaimURIs,
                                                      tenantDomain, false);
    }

    /**
     * @param otherDialectURI
     * @param otherClaimURIs
     * @param tenantDomain
     * @param useCarbonDialectAsKey
     * @return
     * @throws ClaimManagementException
     */
    public Map<String, String> getMappingsMapFromOtherDialectToCarbon(String otherDialectURI,
                                                                      Set<String> otherClaimURIs, String
            tenantDomain, boolean useCarbonDialectAsKey)
            throws ClaimManagementException {

        Map<String, String> returnMap = new HashMap<>();
        Set<org.wso2.carbon.claim.mgt.ClaimMapping> mappings = getMappingsFromOtherDialectToCarbon(
                otherDialectURI, otherClaimURIs, tenantDomain);
        for (org.wso2.carbon.claim.mgt.ClaimMapping mapping : mappings) {
            if (useCarbonDialectAsKey) {
                returnMap.put(mapping.getCarbonClaimURI(), mapping.getNonCarbonClaimURI());
            } else {
                returnMap.put(mapping.getNonCarbonClaimURI(), mapping.getCarbonClaimURI());
            }
        }
        return returnMap;
    }

    /**
     * @param tenantDomain
     * @return
     * @throws ClaimManagementException
     */
    public Set<String> getAllClaimDialects(String tenantDomain) throws ClaimManagementException {

        Set<String> dialects = new HashSet<>();

        List<ClaimMapping> claimMappings = new ArrayList<>(
                Arrays.asList(getAllClaimMappings(tenantDomain)));

        if (claimMappings.isEmpty()) {
            return new HashSet<>();
        }

        for (ClaimMapping claimMapping : claimMappings) {
            String dialectUri = claimMapping.getClaim().getDialectURI();
            dialects.add(dialectUri);
        }
        return dialects;
    }

    public boolean isKnownClaimDialect(String dialectURI, String tenantDomain) throws ClaimManagementException {
        if (StringUtils.isEmpty(dialectURI)) {
            String message = "Invalid argument : " + "\'dialectURI\' is \'NULL\' or empty";
            throw new IllegalArgumentException(message);
        }
        return getAllClaimDialects(tenantDomain).contains(dialectURI);
    }

    /*
     * validate input claims are belongs to wso2 default claim dialect. Check only the first attribute and assume other
     * are also same as first attribute
     */
    private Set<String> validateClaims(Set<String> attributeKeys) {
        if (attributeKeys != null && !attributeKeys.isEmpty()) {
            String claimURI = (String) new ArrayList(attributeKeys).get(0);
            if (!claimURI.startsWith(UserCoreConstants.DEFAULT_CARBON_DIALECT)) {
                return new HashSet<String>();
            }
        }
        return attributeKeys;
    }

}
