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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.CarbonException;
import org.wso2.carbon.core.util.AdminServicesUtil;
import org.wso2.carbon.identity.claim.metadata.mgt.ClaimMetadataManagementServiceImpl;
import org.wso2.carbon.identity.claim.metadata.mgt.exception.ClaimMetadataException;
import org.wso2.carbon.identity.claim.metadata.mgt.model.ExternalClaim;
import org.wso2.carbon.identity.claim.metadata.mgt.model.LocalClaim;
import org.wso2.carbon.user.core.UserCoreConstants;
import org.wso2.carbon.user.core.UserRealm;

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

    private UserRealm getRealm() throws ClaimManagementException {
        try {
            return AdminServicesUtil.getUserRealm();
        } catch (CarbonException e) {
            throw new ClaimManagementException("Error while trying get User Realm.", e);
        }
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
}
