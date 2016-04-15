/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.identity.claim.mgt;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.claim.mgt.builder.ClaimBuilder;
import org.wso2.carbon.identity.claim.mgt.dao.CacheBackedClaimDAO;
import org.wso2.carbon.identity.claim.mgt.dao.ClaimDAO;
import org.wso2.carbon.identity.claim.mgt.model.ClaimMapping;
import org.wso2.carbon.identity.claim.mgt.model.Claim;
import org.wso2.carbon.user.core.UserCoreConstants;
import org.wso2.carbon.user.core.UserStoreException;
import org.wso2.carbon.user.core.claim.InMemoryClaimManager;
import org.wso2.carbon.user.core.util.DatabaseUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;


public class IdentityMgtClaimManager extends InMemoryClaimManager implements ClaimManager {

    private ClaimBuilder claimBuilder;
    private static Log log = LogFactory.getLog(DatabaseUtil.class);
    private ClaimDAO claimDAO = new CacheBackedClaimDAO();
    private int tenantId;

    public IdentityMgtClaimManager(int tenantId) throws UserStoreException {
        this.tenantId = tenantId;
        Map<String, ClaimMapping> claimMapping;
        Map<String, ClaimMapping> localClaims = new HashMap<>();
        Map<String, ClaimMapping> additionalClaims = new HashMap<>();

        int dialectCount = claimDAO.getDialectCount(tenantId);
        if (dialectCount <= 0) {
            try {
                this.claimBuilder = new ClaimBuilder(claimConfig.getPropertyHolder(), tenantId);
                claimMapping = claimBuilder.getClaimMapping();
                if (claimMapping.size() > 0) {
                    for (Map.Entry<String, ClaimMapping> entry : claimMapping.entrySet()) {
                        if (UserCoreConstants.DEFAULT_CARBON_DIALECT.equals(entry.getValue().getClaim().getDialectURI())) {
                            localClaims.put(entry.getKey(), entry.getValue());
                        } else {
                            additionalClaims.put(entry.getKey(), entry.getValue());
                        }
                    }
                }
                if (localClaims.size() > 0) {
                    claimDAO.addClaimMappings(localClaims.values().toArray(new ClaimMapping[localClaims.size()]),
                            tenantId);
                    claimDAO.addClaimMappings(additionalClaims.values().toArray(new ClaimMapping[additionalClaims.size()]), tenantId);
                }
            } catch (Exception e) {
                 log.error("Error while populating the claim configurations", e);
            }
        }
    }

    /**
     * Retrieves the attribute name of the claim URI.
     *
     * @param claimURI The claim URI
     * @return
     * @throws UserStoreException
     */
    @Override
    public String getAttributeName(String claimURI) throws UserStoreException {
        return claimDAO.getMappedAttribute(claimURI, tenantId);
    }

    /**
     * Get attribute name from domain name and claim uri.
     *
     * @param domainName domain name
     * @param claimURI   claim uri
     * @return attribute name specific to the domain
     * @throws UserStoreException
     */
    @Override
    public String getAttributeName(String domainName, String claimURI) throws UserStoreException {
        return claimDAO.getMappedAttribute(claimURI, tenantId, claimURI);
    }

    /**
     * The Claim object of the claim URI
     *
     * @param claimURI The claim URI
     * @return claim
     * @throws UserStoreException
     */
    @Override
    public Claim getClaim(String claimURI) throws UserStoreException {
        return claimDAO.getClaim(claimURI, tenantId);
    }

    /**
     * Get claim mapping.
     *
     * @param claimURI claim uri
     * @return claim mapping
     * @throws UserStoreException
     */
    @Override
    public ClaimMapping getClaimMapping(String claimURI) throws UserStoreException {
        return claimDAO.getClaimMapping(claimURI, tenantId);
    }

    /**
     * Give all the supported claim mappings by default in the system.
     *
     * @return supported claim mapping array.
     * @throws UserStoreException
     */
    @Override
    public ClaimMapping[] getAllSupportClaimMappingsByDefault() throws UserStoreException {
        //TODO: need to check if these claim mappings can be directly loaded from the database
        List<ClaimMapping> claimMappingList = claimDAO.loadClaimMappings(tenantId);
        List<ClaimMapping> supportedByDefaultClaimMappingList = new ArrayList<>();
        for (ClaimMapping claimMapping : claimMappingList) {
            if (claimMapping.getClaim().isSupportedByDefault()) {
                supportedByDefaultClaimMappingList.add(claimMapping);
            }
        }
        return supportedByDefaultClaimMappingList.toArray(new ClaimMapping[supportedByDefaultClaimMappingList.size()]);
    }

    /**
     * Give all the claim mappings from the database.
     *
     * @return an array of claim mappings
     * @throws UserStoreException
     */
    @Override
    public ClaimMapping[] getAllClaimMappings() throws UserStoreException {
        List<ClaimMapping> claimMappingList = claimDAO.loadClaimMappings(tenantId);
        return claimMappingList.toArray(new ClaimMapping[claimMappingList.size()]);
    }

    /**
     * Get all claim mappings specific to a dialect.
     *
     * @param dialectUri
     * @return array of claim mappings
     * @throws UserStoreException
     */
    @Override
    public ClaimMapping[] getAllClaimMappings(String dialectUri) throws UserStoreException {
        List<ClaimMapping> claimMappingList = claimDAO.loadClaimMappings(tenantId, dialectUri);
        return claimMappingList.toArray(new ClaimMapping[claimMappingList.size()]);
    }

    public Map<String, String> getMappingsMapFromOtherDialectToCarbon(String otherDialectURI, Set<String>
            otherClaimURIs, boolean useCarbonDialectAsKey) throws UserStoreException, ClaimManagementException {

        if (otherDialectURI == null) {
            String message = "Invalid argument: \'otherDialectURI\' is \'NULL\'";
            log.error(message);
            throw new ClaimManagementException(message);
        }

        Map<String, String> returnMap = new HashMap<>();
        if (UserCoreConstants.DEFAULT_CARBON_DIALECT.equals(otherDialectURI)) {
            for (String otherClaimURI : otherClaimURIs) {
                returnMap.put(otherClaimURI, otherClaimURI);
            }
        }
        ClaimMapping[] claimMappingsOfDialect = getAllClaimMappings(otherDialectURI);

        if (CollectionUtils.isEmpty(otherClaimURIs)) {
            for (ClaimMapping claimMapping : claimMappingsOfDialect) {
                if (useCarbonDialectAsKey) {
                    returnMap.put(claimMapping.getMappedAttribute(), claimMapping.getClaim().getClaimUri());
                } else {
                    returnMap.put(claimMapping.getClaim().getClaimUri(), claimMapping.getMappedAttribute());
                }
            }
        } else {
            Map<String, String> ClaimMappingsMap = new HashMap<>();
            for (ClaimMapping claimMapping : claimMappingsOfDialect) {
                ClaimMappingsMap.put(claimMapping.getClaim().getClaimUri(), claimMapping.getMappedAttribute());
            }

            for (String otherClaimURI : otherClaimURIs) {
                if (useCarbonDialectAsKey) {
                    returnMap.put(ClaimMappingsMap.get(otherClaimURI), otherClaimURI);
                } else {
                    returnMap.put(otherClaimURI, ClaimMappingsMap.get(otherClaimURI));
                }
            }
        }

        return returnMap;
    }

    /**
     * Give all the mandatory claims.
     *
     * @return an array of required claim mappings.
     * @throws UserStoreException
     */
    @Override
    public ClaimMapping[] getAllRequiredClaimMappings() throws UserStoreException {
        //TODO: need to check if these claim mappings can be directly loaded from the database
        List<ClaimMapping> claimMappingList = claimDAO.loadClaimMappings(tenantId);
        List<ClaimMapping> requiredClaimMappingList = new ArrayList<>();
        for (ClaimMapping claimMapping : claimMappingList) {
            if (claimMapping.getClaim().isRequired()) {
                requiredClaimMappingList.add(claimMapping);
            }
        }
        return requiredClaimMappingList.toArray(new ClaimMapping[requiredClaimMappingList.size()]);
    }

    /**
     * Get all the claim uri from the database.
     *
     * @return an array of claim uris.
     * @throws UserStoreException
     */
    @Override
    public String[] getAllClaimUris() throws UserStoreException {
        List<ClaimMapping> claimMappingList = claimDAO.loadClaimMappings(tenantId);
        String[] claimUris = new String[claimMappingList.size()];
        for (int i = 0; i < claimMappingList.size(); i++) {
            claimUris[i] = claimMappingList.get(i).getClaim().getClaimUri();
        }
        return claimUris;
    }

    @Override
    public void addNewClaimMapping(org.wso2.carbon.user.api.ClaimMapping claimMapping) throws UserStoreException {
        if (claimMapping != null) {
            ClaimMapping claimMappingFromDB = claimDAO.getClaimMapping(claimMapping.getClaim().getClaimUri(), tenantId);
            if (claimMappingFromDB == null) {
                claimDAO.addClaimMapping(claimMapping, tenantId);
            }
        }
    }

    @Override
    public void deleteClaimMapping(org.wso2.carbon.user.api.ClaimMapping claimMapping) throws UserStoreException {
        claimDAO.deleteClaimMapping(claimMapping, tenantId);
    }

    @Override
    public void updateClaimMapping(org.wso2.carbon.user.api.ClaimMapping claimMapping) throws UserStoreException {
        claimDAO.updateClaim(claimMapping, tenantId);
    }

    /**
     * Add new claim dialect.
     *
     * @param mappings new claim mapping, along with the new dialect information.
     * @throws Exception
     */
    public void addNewClaimDialect(ClaimDialect mappings) throws Exception {
        ClaimMapping[] mapping;
        mapping = mappings.getClaimMapping();
        for (ClaimMapping aMapping : mapping) {
            this.addNewClaimMapping(aMapping);
        }
    }

    /**
     * Deletes a dialect
     *
     * @param dialectUri uri of the dialect which need to be deleted
     * @throws Exception
     */
    public void removeClaimDialect(String dialectUri) throws Exception {
        ClaimMapping[] mapping;
        mapping = this.getAllClaimMappings(dialectUri);
        if (mapping != null) {
            for (ClaimMapping aMapping : mapping) {
                this.deleteClaimMapping(aMapping);
            }
        }

    }
}
