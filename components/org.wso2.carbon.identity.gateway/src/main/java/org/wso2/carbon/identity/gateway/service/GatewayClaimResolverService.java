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

import org.wso2.carbon.identity.claim.exception.ClaimResolvingServiceException;
import org.wso2.carbon.identity.claim.exception.ProfileMgtServiceException;
import org.wso2.carbon.identity.claim.mapping.profile.ClaimConfigEntry;
import org.wso2.carbon.identity.claim.mapping.profile.ProfileEntry;
import org.wso2.carbon.identity.claim.service.ClaimResolvingService;
import org.wso2.carbon.identity.claim.service.ProfileMgtService;
import org.wso2.carbon.identity.gateway.internal.FrameworkServiceDataHolder;
import org.wso2.carbon.identity.gateway.processor.handler.authentication.AuthenticationHandlerException;
import org.wso2.carbon.identity.mgt.claim.Claim;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class GatewayClaimResolverService {
    private static GatewayClaimResolverService gatewayClaimResolverService = new GatewayClaimResolverService();

    private GatewayClaimResolverService(){

    }
    public static GatewayClaimResolverService getInstance(){
        return GatewayClaimResolverService.gatewayClaimResolverService;
    }


    public Set<Claim> transformToNativeDialect(Set<Claim> otherDialectClaims, String claimDialect, Optional<String>
            profile){
        try {
            final Set<Claim> transformedClaims = new HashSet<>();

            ClaimResolvingService claimResolvingService = FrameworkServiceDataHolder.getInstance()
                    .getClaimResolvingService();
            Map<String, String> claimMapping = claimResolvingService.getClaimMapping(claimDialect);

            Set<Claim> transformedClaimsTmp = new HashSet<>();
            otherDialectClaims.stream().filter(claim -> claimMapping.containsKey(claim.getClaimUri()))
                    .forEach(claim -> {
                        Claim tmpClaim = new Claim("http://wso2.org/claims", claimMapping.get(claim.getClaimUri()), claim.getValue());
                        transformedClaimsTmp.add(tmpClaim);
                    });

            if(profile.isPresent()) {
                ProfileMgtService profileMgtService = FrameworkServiceDataHolder.getInstance().getProfileMgtService();
                ProfileEntry profileEntry = profileMgtService.getProfile(profile.get());
                List<ClaimConfigEntry> profileClaims = profileEntry.getClaims();
                Map<String, ClaimConfigEntry> profileClaimMap = new HashMap<>();
                profileClaims.forEach(profileClaim -> profileClaimMap.put(profileClaim.getClaimURI(), profileClaim));

                transformedClaimsTmp.stream().filter(claim -> profileClaimMap.containsKey(claim.getClaimUri()))
                        .forEach(transformedClaims::add);
                return transformedClaims ;
            }
            return transformedClaimsTmp;
        } catch (ClaimResolvingServiceException | ProfileMgtServiceException e) {

        }
        return null ;
    }


    public Set<Claim> transformToOtherDialect(Set<Claim> nativeDialectClaims, String dialect, Optional<String>
            profile) {

        Set<Claim> transformedClaims = new HashSet<>();
        ClaimResolvingService claimResolvingService = FrameworkServiceDataHolder.getInstance()
                .getClaimResolvingService();
        try {

            Map<String, Claim> claimMap = new HashMap<>();

            if(profile.isPresent()) {
                ProfileMgtService profileMgtService = FrameworkServiceDataHolder.getInstance().getProfileMgtService();
                ProfileEntry profileEntry = profileMgtService.getProfile(profile.get());

                List<ClaimConfigEntry> profileClaims = profileEntry.getClaims();
                Map<String, ClaimConfigEntry> profileClaimMap = new HashMap<>();
                profileClaims.forEach(profileClaim -> profileClaimMap.put(profileClaim.getClaimURI(), profileClaim));

                nativeDialectClaims.stream().filter(claim -> profileClaimMap.containsKey(claim.getClaimUri()))
                        .forEach(claim -> claimMap.put(claim.getClaimUri(), claim));
            }else {
                nativeDialectClaims.forEach(claim -> claimMap.put(claim.getClaimUri(), claim));
            }

            Map<String, String> claimMapping = claimResolvingService.getClaimMapping(dialect);
            claimMapping.forEach((applicationClaimUri,nativeClaimUri) -> {
                if(claimMap.containsKey(nativeClaimUri)){
                    Claim claim = new Claim(dialect, applicationClaimUri, claimMap.get(nativeClaimUri).getValue());
                    transformedClaims.add(claim);
                }
            });

        } catch (ClaimResolvingServiceException e) {
            e.printStackTrace();
        } catch (ProfileMgtServiceException e) {
            e.printStackTrace();
        }


        return transformedClaims;
    }
}
