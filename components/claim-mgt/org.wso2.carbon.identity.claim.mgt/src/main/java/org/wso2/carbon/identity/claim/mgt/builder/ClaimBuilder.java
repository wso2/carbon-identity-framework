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

package org.wso2.carbon.identity.claim.mgt.builder;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleContext;
import org.wso2.carbon.identity.claim.mgt.dao.ClaimDAO;
import org.wso2.carbon.identity.claim.mgt.dao.ClaimDAOImpl;
import org.wso2.carbon.identity.claim.mgt.model.Claim;
import org.wso2.carbon.identity.claim.mgt.model.ClaimMapping;
import org.wso2.carbon.user.core.UserStoreException;

import java.util.*;

/**
 * builder that build the claim mappings from the
 * property holder contains in the claimConfig
 */

public class ClaimBuilder {

    private static Log log = LogFactory.getLog(ClaimBuilder.class);
    private static BundleContext bundleContext;
    private static Map<String, ClaimMapping> claimMapping = null;
    public static Set<String> dialects = new HashSet<>();
    int tenantId;

    public static final String LOCAL_NAME_DIALECT = "Dialect";
    public static final String LOCAL_NAME_CLAIM_URI = "ClaimURI";
    public static final String LOCAL_NAME_DISPLAY_NAME = "DisplayName";
    public static final String LOCAL_NAME_DESCRIPTION = "Description";
    public static final String LOCAL_NAME_REQUIRED = "Required";
    public static final String LOCAL_NAME_SUPPORTED_BY_DEFAULT = "SupportedByDefault";
    public static final String LOCAL_NAME_REG_EX = "RegEx";
    public static final String LOCAL_NAME_ATTR_ID = "AttributeID";
    public static final String LOCAL_NAME_DISPLAY_ORDER = "DisplayOrder";
    public static final String LOCAL_NAME_READ_ONLY = "ReadOnly";
    public static final String LOCAL_NAME_CHECKED_ATTR = "CheckedAttribute";
    public static final String LOCAL_DIALECT_URI = "http://wso2.org/claims";


    public ClaimBuilder(Map<String, Map<String, String>> propertyHolder, int tenantId) throws UserStoreException {
        this.tenantId = tenantId;
        claimMapping = new HashMap<>();
        populateProfileAndClaimMaps(propertyHolder, tenantId);
    }

    /**
     * this will prepare the claim mapping from the property holder
     * @param propertyHolder mappings of the claim and the claim properties
     * @param tenantId tenant identifier for each tenant
     * @return claim mappings
     */
    public void populateProfileAndClaimMaps(Map<String, Map<String, String>> propertyHolder,int tenantId) throws UserStoreException {

        Claim claim;
        ClaimMapping claimMapping;
        String attributeId = null;
        String claimUri = null;

        Map<String,String> customMetaData=null;

        Iterator<Map.Entry<String, Map<String, String>>> parent = propertyHolder.entrySet().iterator();
        while (parent.hasNext()) {
            Map.Entry<String, Map<String, String>> parentPair = parent.next();
            Iterator<Map.Entry<String, String>> child = (parentPair.getValue()).entrySet().iterator();
            customMetaData = new HashMap<>();
            claim = new Claim();
            while (child.hasNext()) {
                Map.Entry childPair = child.next();
                if (LOCAL_NAME_DIALECT.equals(childPair.getKey())) {
                    claim.setDialectURI(childPair.getValue().toString());
                }
                else if (LOCAL_NAME_CLAIM_URI.equals(childPair.getKey())) {
                    claimUri=childPair.getValue().toString();
                    if (claimUri.toLowerCase().contains(LOCAL_DIALECT_URI)) {
                        claim.setIsLocalClaim(true);
                    }
                    else{
                        claim.setIsLocalClaim(false);
                    }
                    claim.setClaimUri(childPair.getValue().toString());
                }
                else if (LOCAL_NAME_DISPLAY_NAME.equals(childPair.getKey())) {
                    claim.setDisplayTag(childPair.getValue().toString());
                }
                else if (LOCAL_NAME_DESCRIPTION.equals(childPair.getKey())) {
                    claim.setDescription(childPair.getValue().toString());
                }
                else if (LOCAL_NAME_REG_EX.equals(childPair.getKey())) {
                    claim.setRegEx(childPair.getValue().toString());
                }
                else if (LOCAL_NAME_REQUIRED.equals(childPair.getKey())) {
                    claim.setRequired(true);
                }
                else if (LOCAL_NAME_SUPPORTED_BY_DEFAULT.equals(childPair.getKey())) {
                    claim.setSupportedByDefault(true);
                }
                else if (LOCAL_NAME_DISPLAY_ORDER.equals(childPair.getKey())) {
                    claim.setDisplayOrder(Integer.parseInt(childPair.getValue().toString()));
                }
                else if (LOCAL_NAME_READ_ONLY.equals(childPair.getKey())) {
                    claim.setReadOnly(true);
                }
                else if (LOCAL_NAME_CHECKED_ATTR.equals(childPair.getKey())) {
                    claim.setCheckedAttribute(true);
                }
                else if (LOCAL_NAME_ATTR_ID.equals(childPair.getKey())) {
                    attributeId = childPair.getValue().toString();
                }
                else{
                    customMetaData.put(childPair.getKey().toString(),childPair.getValue().toString());
                    claim.setCustomMetaData(customMetaData);
                }
            }
            log.info(claim.getClaimUri() + " " + attributeId);
            claimMapping = new ClaimMapping(claim, attributeId);
            ClaimBuilder.claimMapping.put(claimUri, claimMapping);
        }
    }

    public static void setBundleContext(BundleContext bundleContext) {
        ClaimBuilder.bundleContext = bundleContext;
    }

    /**
     * This method will initiate claim loading from the database and finalize the total claims
     * @param realmName real name
     * @return the final claims from the build claim mappings
     */
    public Map<String, ClaimMapping> buildClaimMappingsFromDatabase(String realmName) {
        Map<String, ClaimMapping> claims = new HashMap<>();
        try {
            ClaimDAO claimDAO = new ClaimDAOImpl();
            List<ClaimMapping> lst = claimDAO.loadClaimMappings(tenantId);
            for (ClaimMapping cm : lst) {
                String uri = cm.getClaim().getClaimUri();
                claims.put(uri, cm);
            }
        } catch (UserStoreException e) {
            log.error(e.getMessage(), e);
        }
        return claims;
    }

    /**
     * This will method use to get the claim mappings
     * @return claim mappings
     */
    public static Map<String, ClaimMapping> getClaimMapping() {
        return claimMapping;
    }

    public static void setClaimMapping(Map<String, ClaimMapping> claimMapping) {
        ClaimBuilder.claimMapping = claimMapping;
    }


}
