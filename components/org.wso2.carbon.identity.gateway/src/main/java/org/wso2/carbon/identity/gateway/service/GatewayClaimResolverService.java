/*
 *
 *  * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *  *
 *  * WSO2 Inc. licenses this file to you under the Apache License,
 *  * Version 2.0 (the "License"); you may not use this file except
 *  * in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  *      http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing,
 *  * software distributed under the License is distributed on an
 *  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  * KIND, either express or implied.  See the License for the
 *  * specific language governing permissions and limitations
 *  * under the License.
 *
 */

package org.wso2.carbon.identity.gateway.service;

import org.slf4j.Logger;
import org.wso2.carbon.identity.claim.exception.ClaimResolvingServiceException;
import org.wso2.carbon.identity.claim.exception.ProfileMgtServiceException;
import org.wso2.carbon.identity.claim.mapping.profile.ClaimConfigEntry;
import org.wso2.carbon.identity.claim.mapping.profile.ProfileEntry;
import org.wso2.carbon.identity.claim.service.ClaimResolvingService;
import org.wso2.carbon.identity.claim.service.ProfileMgtService;
import org.wso2.carbon.identity.gateway.api.exception.GatewayServerException;
import org.wso2.carbon.identity.gateway.handler.GatewayHandlerManager;
import org.wso2.carbon.identity.gateway.internal.GatewayServiceHolder;
import org.wso2.carbon.identity.mgt.claim.Claim;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

/**
 * GatewayClaimResolverService is a service class that is wrapped the claim management service to get some aggregate
 * operation.
 */
public class GatewayClaimResolverService {
    private static Logger log = org.slf4j.LoggerFactory.getLogger(GatewayHandlerManager.class);
    private static GatewayClaimResolverService gatewayClaimResolverService = new GatewayClaimResolverService();

    private GatewayClaimResolverService() {

    }

    public static GatewayClaimResolverService getInstance() {
        return GatewayClaimResolverService.gatewayClaimResolverService;
    }


    public Set<Claim> transformToNativeDialect(Set<Claim> otherDialectClaims, String claimDialect, Optional<String>
            profile) {
        AtomicReference<Set<Claim>> transformedClaims = new AtomicReference<>(new HashSet<>());
        try {
            ClaimResolvingService claimResolvingService = GatewayServiceHolder.getInstance()
                    .getClaimResolvingService();
            Map<String, String> claimMapping = claimResolvingService.getClaimMapping(claimDialect);

            //#TODO: ClaimResolvingService should not return null for Map. It should be empty Map. After fixed that,
            // we can remove this null check.
            if (claimMapping == null) {
                throw new ClaimResolvingServiceException("Not implemented");
            }

            Set<Claim> transformedClaimsTmp = new HashSet<>();
            otherDialectClaims.stream().filter(claim -> claimMapping.containsKey(claim.getClaimUri()))
                    .forEach(claim -> {
                        Claim tmpClaim = new Claim("http://wso2.org/claims", claimMapping.get(claim.getClaimUri()),
                                claim.getValue());
                        transformedClaimsTmp.add(tmpClaim);
                    });

            if (profile.isPresent()) {
                ProfileMgtService profileMgtService = GatewayServiceHolder.getInstance().getProfileMgtService();
                ProfileEntry profileEntry = profileMgtService.getProfile(profile.get());
                if (profileEntry != null) {
                    List<ClaimConfigEntry> profileClaims = profileEntry.getClaims();
                    Map<String, ClaimConfigEntry> profileClaimMap = new HashMap<>();
                    profileClaims.forEach(profileClaim -> profileClaimMap.put(profileClaim.getClaimURI(),
                            profileClaim));

                    transformedClaimsTmp.stream().filter(claim -> profileClaimMap.containsKey(claim.getClaimUri()))
                            .forEach(transformedClaims.get()::add);
                } else {
                    transformedClaims.set(transformedClaimsTmp);
                }
            } else {
                transformedClaims.set(transformedClaimsTmp);
            }
        } catch (ClaimResolvingServiceException | ProfileMgtServiceException e) {
            String errorMessage = "Error occurred while calling transformToNativeDialect, " + e.getMessage();
            log.error(errorMessage, e);
        }
        return transformedClaims.get();
    }


    public Set<Claim> transformToOtherDialect(Set<Claim> nativeDialectClaims, String dialect, Optional<String>
            profile) {

        Set<Claim> transformedClaims = new HashSet<>();
        ClaimResolvingService claimResolvingService = GatewayServiceHolder.getInstance()
                .getClaimResolvingService();
        try {

            Map<String, Claim> claimMap = new HashMap<>();

            if (profile.isPresent()) {
                ProfileMgtService profileMgtService = GatewayServiceHolder.getInstance().getProfileMgtService();
                ProfileEntry profileEntry = profileMgtService.getProfile(profile.get());
                if (profileEntry == null) {
                    throw new GatewayServerException("Profile not found : " + profile.get());
                }
                List<ClaimConfigEntry> profileClaims = profileEntry.getClaims();
                Map<String, ClaimConfigEntry> profileClaimMap = new HashMap<>();
                profileClaims
                        .forEach(profileClaim -> profileClaimMap.put(profileClaim.getClaimURI(), profileClaim));

                nativeDialectClaims.stream().filter(claim -> profileClaimMap.containsKey(claim.getClaimUri()))
                        .forEach(claim -> claimMap.put(claim.getClaimUri(), claim));
            } else {
                nativeDialectClaims.forEach(claim -> claimMap.put(claim.getClaimUri(), claim));
            }

            Map<String, String> claimMapping = claimResolvingService.getClaimMapping(dialect);
            claimMapping.forEach((applicationClaimUri, nativeClaimUri) -> {
                if (claimMap.containsKey(nativeClaimUri)) {
                    Claim claim = new Claim(dialect, applicationClaimUri, claimMap.get(nativeClaimUri).getValue());
                    transformedClaims.add(claim);
                }
            });
        } catch (ClaimResolvingServiceException | ProfileMgtServiceException | GatewayServerException e) {
            String errorMessage = "Error occurred while calling transformToOtherDialect, " + e.getMessage();
            log.error(errorMessage, e);
        }

        return transformedClaims;
    }
}
