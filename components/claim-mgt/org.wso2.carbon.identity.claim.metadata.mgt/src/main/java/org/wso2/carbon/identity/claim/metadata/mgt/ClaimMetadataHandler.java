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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.claim.metadata.mgt.exception.ClaimMetadataException;
import org.wso2.carbon.identity.claim.metadata.mgt.model.ExternalClaim;
import org.wso2.carbon.identity.claim.metadata.mgt.model.LocalClaim;
import org.wso2.carbon.user.core.UserCoreConstants;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Handles the claim conversion to one claim dialect to the other
 */
public class ClaimMetadataHandler {

    private static final Log log = LogFactory.getLog(ClaimMetadataHandler.class);
    private static final ClaimMetadataHandler INSTANCE = new ClaimMetadataHandler();

    public static ClaimMetadataHandler getInstance() {
        return INSTANCE;
    }

    /**
     * @param otherDialectURI
     * @param otherClaimURIs
     * @param tenantDomain
     * @return
     * @throws ClaimMetadataException
     */
    public Set<ExternalClaim> getMappingsFromOtherDialectToCarbon(
            String otherDialectURI, Set<String> otherClaimURIs, String tenantDomain)
            throws ClaimMetadataException {

        Set<ExternalClaim> returnSet = new HashSet<ExternalClaim>();

        if (otherDialectURI == null) {
            String message = "Invalid argument: \'otherDialectURI\' is \'NULL\'";
            log.error(message);
            throw new ClaimMetadataException(message);
        }


        try {
            ClaimMetadataManagementServiceImpl claimMetadataService = new ClaimMetadataManagementServiceImpl();

            if (otherDialectURI.equals(UserCoreConstants.DEFAULT_CARBON_DIALECT) ) {

                List<LocalClaim> localClaims = claimMetadataService.getLocalClaims(tenantDomain);

                if (otherClaimURIs == null|| otherClaimURIs.isEmpty()) {

                    for (LocalClaim localClaim : localClaims) {
                        ExternalClaim claimMapping = new ExternalClaim(localClaim.getClaimDialectURI(), localClaim
                                .getClaimURI(), localClaim.getClaimURI());
                        returnSet.add(claimMapping);
                    }

                    return returnSet;

                } else {

                    for (LocalClaim localClaim : localClaims) {

                        if (otherClaimURIs.contains(localClaim.getClaimURI())) {

                            ExternalClaim claimMapping = new ExternalClaim(
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

                    returnSet = new HashSet<ExternalClaim>(externalClaims);

                } else {

                    for (ExternalClaim externalClaim : externalClaims) {

                        if (otherClaimURIs.contains(externalClaim.getClaimURI())) {
                            returnSet.add(externalClaim);
                        }
                    }

                }

                return returnSet;
            }


        } catch (ClaimMetadataException e) {
            throw new ClaimMetadataException(e.getMessage(), e);
        }
    }

    /**
     * @param otherDialectURI
     * @param otherClaimURIs
     * @param tenantDomain
     * @param useCarbonDialectAsKey
     * @return
     * @throws ClaimMetadataException
     */
    public Map<String, String> getMappingsMapFromOtherDialectToCarbon(String otherDialectURI, Set<String>
            otherClaimURIs, String tenantDomain, boolean useCarbonDialectAsKey) throws ClaimMetadataException {

        Map<String, String> returnMap = new HashMap<>();
        Set<ExternalClaim> mappings = getMappingsFromOtherDialectToCarbon(
                otherDialectURI, otherClaimURIs, tenantDomain);
        for (ExternalClaim externalClaim : mappings) {
            if (useCarbonDialectAsKey) {
                returnMap.put(externalClaim.getMappedLocalClaim(), externalClaim.getClaimURI());
            } else {
                returnMap.put(externalClaim.getClaimURI(), externalClaim.getMappedLocalClaim());
            }
        }
        return returnMap;
    }
}
