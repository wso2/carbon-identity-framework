package org.wso2.carbon.identity.gateway.processor.util;


import org.wso2.carbon.identity.claim.exception.ClaimResolvingServiceException;
import org.wso2.carbon.identity.claim.exception.ProfileMgtServiceException;
import org.wso2.carbon.identity.claim.mapping.profile.ClaimConfigEntry;
import org.wso2.carbon.identity.claim.mapping.profile.ProfileEntry;
import org.wso2.carbon.identity.claim.service.ClaimResolvingService;
import org.wso2.carbon.identity.claim.service.ProfileMgtService;
import org.wso2.carbon.identity.gateway.internal.FrameworkServiceDataHolder;
import org.wso2.carbon.identity.mgt.claim.Claim;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class ClaimResolverUtility {

    public static Set<Claim> getRootClaims(Set<Claim> claims, String profile) throws ClaimResolvingServiceException,
                                                                                     ProfileMgtServiceException {

        Set<Claim> transformedClaims = new HashSet<>();

        String claimDialect = claims.stream().findFirst().get().getDialectUri();
        ClaimResolvingService claimResolvingService = FrameworkServiceDataHolder.getInstance()
                .getClaimResolvingService();
        Map<String, String> claimMapping = claimResolvingService.getClaimMapping(claimDialect);

        Set<Claim> transformedClaimsTmp = new HashSet<>();
        claims.stream().filter(claim -> claimMapping.containsKey(claim.getClaimUri()))
                .forEach(claim -> {
                    Claim tmpClaim = new Claim("rootdialect", claimMapping.get(claim.getClaimUri()), claim.getValue());
                    transformedClaimsTmp.add(tmpClaim);
                });

        ProfileMgtService profileMgtService = FrameworkServiceDataHolder.getInstance().getProfileMgtService();
        ProfileEntry profileEntry = profileMgtService.getProfile(profile);
        List<ClaimConfigEntry> profileClaims = profileEntry.getClaims();
        Map<String, ClaimConfigEntry> profileClaimMap = new HashMap<>();
        profileClaims.stream().forEach(profileClaim -> profileClaimMap.put(profileClaim.getClaimURI(), profileClaim));

        transformedClaimsTmp.stream().filter(claim -> profileClaimMap.containsKey(claim.getClaimUri()))
                .forEach(claim -> transformedClaims.add(claim));

        return transformedClaims ;
    }



    public static Set<Claim> getRootClaims(Set<Claim> claims) throws ClaimResolvingServiceException,
                                                                     ProfileMgtServiceException {
        Set<Claim> transformedClaims = new HashSet<>();
        String claimDialect = claims.stream().findFirst().get().getDialectUri();
        ClaimResolvingService claimResolvingService = FrameworkServiceDataHolder.getInstance()
                .getClaimResolvingService();
        Map<String, String> claimMapping = claimResolvingService.getClaimMapping(claimDialect);


        claims.stream().filter(claim -> claimMapping.containsKey(claim.getClaimUri()))
                .forEach(claim -> {
                    Claim tmpClaim = new Claim("rootdialect", claimMapping.get(claim.getClaimUri()), claim.getValue());
                    transformedClaims.add(tmpClaim);
                });

        return transformedClaims ;
    }
}
