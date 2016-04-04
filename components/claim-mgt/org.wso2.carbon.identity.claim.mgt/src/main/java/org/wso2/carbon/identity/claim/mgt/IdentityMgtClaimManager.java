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
import org.wso2.carbon.identity.claim.mgt.dao.ClaimDAOImpl;
import org.wso2.carbon.identity.claim.mgt.model.ClaimMapping;
import org.wso2.carbon.identity.claim.mgt.model.ClaimToClaimMapping;
import org.wso2.carbon.identity.claim.mgt.model.Claim;
import org.wso2.carbon.user.core.UserCoreConstants;
import org.wso2.carbon.user.core.UserStoreException;
import org.wso2.carbon.user.core.claim.InMemoryClaimManager;
import org.wso2.carbon.user.core.util.DatabaseUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;


public class IdentityMgtClaimManager extends InMemoryClaimManager implements ClaimManager {

    private static ClaimInvalidationCache claimCache;
    private ClaimBuilder claimBuilder;
    private Map<String, ClaimMapping> claimMapping = null;
    private Map<String, ClaimMapping> localClaims = null;
    private Map<String, ClaimMapping> additionalClaims = null;
    private static ClaimToClaimMapping claimToClaimMapping;
    private static Log log = LogFactory.getLog(DatabaseUtil.class);
    private ClaimDAO claimDAO = new CacheBackedClaimDAO();
    private int tenantId;

    public IdentityMgtClaimManager(int tenantId) throws UserStoreException {
        this.tenantId = tenantId;
        this.claimDAO = new ClaimDAOImpl();
        this.claimCache = ClaimInvalidationCache.getInstance();

        claimMapping = new HashMap<>();
        localClaims = new HashMap<>();
        additionalClaims = new HashMap<>();

        int dialectCount = claimDAO.getDialectCount(tenantId);
        if (dialectCount > 0) {
            try {
                List<ClaimMapping> lst = claimDAO.loadClaimMappings(tenantId);
                for (ClaimMapping cm : lst) {
                    String uri = cm.getClaim().getClaimUri();
                    claimMapping.put(uri, cm);
                }
                if (claimMapping.size() > 0) {
                    doClaimCategorize();
                }
            } catch (UserStoreException e) {
                log.error("Error reading claims from database", e);
            }
        } else {
            try {
                this.claimBuilder = new ClaimBuilder(claimConfig.getPropertyHolder(), tenantId);
                claimMapping = claimBuilder.getClaimMapping();
                if (claimMapping.size() > 0) {
                    doClaimCategorize();
                }
                if (dialectCount <= 0 && localClaims.size() != 0) {
                    if (claimDAO.addClaimMappings(localClaims.values().toArray(new ClaimMapping[localClaims.size()]), tenantId)) {
                        claimDAO.addClaimMappings(additionalClaims.values().toArray(new ClaimMapping[additionalClaims
                                .size()]), tenantId);
                    }
                }
            } catch (Exception e) {
                 log.error("Error while populating the claim configurations", e);
            }

        }
    }

    /**
     * Categorize the local claims and additional claims
     */
    private void doClaimCategorize() {
        for (Map.Entry<String, ClaimMapping> entry : claimMapping.entrySet()) {
            if (entry.getValue().getClaim().getIsLocalClaim()) {
                localClaims.put(entry.getKey(), entry.getValue());
            } else {
                additionalClaims.put(entry.getKey(), entry.getValue());
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
    public ClaimMapping getClaimMapping(String claimURI) throws UserStoreException {
        return claimDAO.getClaimMapping(claimURI, tenantId);
    }

    /**
     * Give all the supported claim mappings by default in the system.
     *
     * @return supported claim mapping array.
     * @throws UserStoreException
     */
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
    public ClaimMapping[] getAllClaimMappings(String dialectUri)
            throws UserStoreException {

        if (claimCache.isInvalid()) {
            this.claimMapping = getClaimMapFromDB();
        }
        List<ClaimMapping> claimList = null;
        claimList = new ArrayList<ClaimMapping>();
        Iterator<Map.Entry<String, ClaimMapping>> iterator = claimMapping.entrySet().iterator();

        for (; iterator.hasNext(); ) {
            ClaimMapping claimMapping = iterator.next().getValue();
            if (claimMapping.getClaim().getDialectURI().equals(dialectUri)) {
                claimList.add(claimMapping);
            }
        }
        return claimList.toArray(new ClaimMapping[claimList.size()]);
    }

    /**
     * Get all the claims with relations. This contains the additional claim and the
     * related mapped attribute(which is an local claim).
     *
     * @return an array of claimToClaimMappings
     * @throws UserStoreException
     */
    public ClaimToClaimMapping[] getAllClaimToClaimMappings() throws UserStoreException {
        if (claimCache.isInvalid()) {
            this.claimMapping = getClaimMapFromDB();
        }
        List<ClaimToClaimMapping> claimList = null;
        claimList = new ArrayList<ClaimToClaimMapping>();
        for (Map.Entry<String, ClaimMapping> localClaimEntry : localClaims.entrySet()) {
            for (Map.Entry<String, ClaimMapping> addtionalClaimEntry : localClaims.entrySet()) {
                if (localClaimEntry.getValue().equals(addtionalClaimEntry.getValue().getMappedAttribute())) {
                    claimToClaimMapping = new ClaimToClaimMapping(localClaimEntry.getValue().getClaim(),
                            addtionalClaimEntry.getValue().getClaim());
                    claimList.add(claimToClaimMapping);
                }
            }
        }
        return claimList.toArray(new ClaimToClaimMapping[claimList.size()]);
    }

    public Map<String, String> getMappingsMapFromOtherDialectToCarbon(String otherDialectURI, Set<String>
            otherClaimURIs, boolean useCarbonDialectAsKey) throws UserStoreException {

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
    public String[] getAllClaimUris() throws UserStoreException {
        List<ClaimMapping> claimMappingList = claimDAO.loadClaimMappings(tenantId);
        String[] claimUris = new String[claimMappingList.size()];
        for (int i = 0; i < claimMappingList.size(); i++) {
            claimUris[i] = claimMappingList.get(i).getClaim().getClaimUri();
        }
        return claimUris;
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
     * Adds a new claim mapping
     *
     * @param mapping The claim mapping to be added
     * @throws UserStoreException
     */
    public void addNewClaimMapping(ClaimMapping mapping) throws UserStoreException {
        if (mapping != null) {
            ClaimMapping claimMappingFromDB = claimDAO.getClaimMapping(mapping.getClaim().getClaimUri(), tenantId);
            if (claimMappingFromDB == null) {
                claimDAO.addClaimMapping(mapping, tenantId);
            }
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

    /**
     * Deletes a claim mapping
     *
     * @param mapping The claim mapping to be deleted
     * @throws UserStoreException
     */
    public void deleteClaimMapping(ClaimMapping mapping) throws UserStoreException {

        if (mapping != null && mapping.getClaim() != null) {

            if (claimCache.isInvalid()) {
                this.claimMapping = getClaimMapFromDB();
            }
            if (claimMapping.containsKey(mapping.getClaim().getClaimUri())) {
                claimMapping.remove(mapping.getClaim().getClaimUri());
                claimDAO.deleteClaimMapping(getClaimMapping(mapping), tenantId);
                this.claimCache.invalidateCache();
            }
        }
    }

    /**
     * Updates a claim mapping
     *
     * @param mapping The claim mapping to be updated
     * @throws UserStoreException
     */
    public void updateClaimMapping(ClaimMapping mapping) throws UserStoreException {

        if (mapping != null && mapping.getClaim() != null) {

            if (claimCache.isInvalid()) {
                this.claimMapping = getClaimMapFromDB();
            }
            if (claimMapping.containsKey(mapping.getClaim().getClaimUri())) {
                claimMapping.put(mapping.getClaim().getClaimUri(), getClaimMapping(mapping));
                claimDAO.updateClaim(getClaimMapping(mapping), tenantId);
                this.claimCache.invalidateCache();
            }
        }
    }

    /**
     * Gets the claim mapping.
     *
     * @param claimMapping The claim mapping
     * @return
     * @throws UserStoreException
     */
    private ClaimMapping getClaimMapping(ClaimMapping claimMapping) {
        ClaimMapping claimMap = null;
        if (claimMapping != null) {
            claimMap = new ClaimMapping(getClaim(claimMapping.getClaim()), claimMapping.getMappedAttribute());
            claimMap.setMappedAttributes(claimMapping.getMappedAttributes());
        } else {
            return new ClaimMapping();
        }
        return claimMap;
    }

    /**
     * The Claim object of the claim URI
     *
     * @param claim The claim
     * @return
     * @throws UserStoreException
     */
    private Claim getClaim(Claim claim) {

        Claim clm = new Claim();
        if (claim != null) {
            clm.setCheckedAttribute(claim.isCheckedAttribute());
            clm.setClaimUri(claim.getClaimUri());
            clm.setDescription(claim.getDescription());
            clm.setDialectURI(claim.getDialectURI());
            clm.setDisplayOrder(claim.getDisplayOrder());
            clm.setDisplayTag(claim.getDisplayTag());
            clm.setReadOnly(claim.isReadOnly());
            clm.setRegEx(claim.getRegEx());
            clm.setRequired(claim.isRequired());
            clm.setSupportedByDefault(claim.isSupportedByDefault());
            clm.setValue(claim.getValue());
        }
        return clm;
    }

    /**
     * Get all the claims from database.
     *
     * @return claim map
     * @throws UserStoreException
     */
    private Map<String, ClaimMapping> getClaimMapFromDB() throws UserStoreException {
        Map<String, ClaimMapping> claimMap = new ConcurrentHashMap<>();
        try {
            Map<String, ClaimMapping> dbClaimMap = this.claimBuilder.buildClaimMappingsFromDatabase(null);
            claimMap.putAll(dbClaimMap);
        } catch (Exception e) {
            throw new UserStoreException(e.getMessage(), e);
        }
        return claimMap;
    }
}
