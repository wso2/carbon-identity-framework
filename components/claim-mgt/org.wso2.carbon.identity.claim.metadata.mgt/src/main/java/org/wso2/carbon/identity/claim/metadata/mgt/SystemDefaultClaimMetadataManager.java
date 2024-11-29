/*
 * Copyright (c) 2024, WSO2 LLC. (http://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
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
 */

package org.wso2.carbon.identity.claim.metadata.mgt;

import org.apache.commons.lang.StringUtils;
import org.wso2.carbon.identity.claim.metadata.mgt.exception.ClaimMetadataException;
import org.wso2.carbon.identity.claim.metadata.mgt.internal.ReadOnlyClaimMetadataManager;
import org.wso2.carbon.identity.claim.metadata.mgt.internal.IdentityClaimManagementServiceDataHolder;
import org.wso2.carbon.identity.claim.metadata.mgt.model.Claim;
import org.wso2.carbon.identity.claim.metadata.mgt.model.ClaimDialect;
import org.wso2.carbon.identity.claim.metadata.mgt.model.ExternalClaim;
import org.wso2.carbon.identity.claim.metadata.mgt.model.LocalClaim;
import org.wso2.carbon.identity.claim.metadata.mgt.util.ClaimMetadataUtils;
import org.wso2.carbon.user.core.claim.inmemory.ClaimConfig;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.wso2.carbon.identity.claim.metadata.mgt.util.ClaimConstants.LOCAL_CLAIM_DIALECT_URI;

/**
 * System default claim metadata manager.
 */
public class SystemDefaultClaimMetadataManager implements ReadOnlyClaimMetadataManager {

    private static final List<ClaimDialect> claimDialects;
    private static final Map<String, List<Claim>> claims;

    static {

        ClaimConfig claimConfig = IdentityClaimManagementServiceDataHolder.getInstance().getClaimConfig();
        claims = ClaimMetadataUtils.getClaimsMapFromClaimConfig(claimConfig);
        claimDialects = claims.keySet().stream()
                .map(ClaimDialect::new)
                .collect(Collectors.toList());
    }

    @Override
    public List<ClaimDialect> getClaimDialects(int tenantId) throws ClaimMetadataException {

        return claimDialects;
    }

    @Override
    public Optional<ClaimDialect> getClaimDialect(String claimDialectURI, int tenantId) throws ClaimMetadataException {

        if (StringUtils.isBlank(claimDialectURI)) {
            throw new ClaimMetadataException("Invalid claim dialect URI: " + claimDialectURI);
        }

        return claimDialects.stream()
                .filter(claimDialect -> claimDialectURI.equals(claimDialect.getClaimDialectURI()))
                .findFirst();
    }

    @Override
    public List<LocalClaim> getLocalClaims(int tenantId) throws ClaimMetadataException {

        List<Claim> localClaims = claims.get(LOCAL_CLAIM_DIALECT_URI);

        if (localClaims == null) {
            return Collections.emptyList();
        }

        return localClaims.stream()
                .map(LocalClaim.class::cast)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<LocalClaim> getLocalClaim(String localClaimURI ,int tenantId) throws ClaimMetadataException {

        if (StringUtils.isBlank(localClaimURI)) {
            throw new ClaimMetadataException("Invalid local claim URI: " + localClaimURI);
        }

        return claims.getOrDefault(LOCAL_CLAIM_DIALECT_URI, Collections.emptyList()).stream()
                .filter(claim -> localClaimURI.equals(claim.getClaimURI()))
                .map(LocalClaim.class::cast)
                .findFirst();
    }

    @Override
    public List<ExternalClaim> getExternalClaims(String externalClaimDialectURI, int tenantId)
            throws ClaimMetadataException {

        if (StringUtils.isBlank(externalClaimDialectURI)) {
            throw new ClaimMetadataException("Invalid external claim dialect URI: " + externalClaimDialectURI);
        }

        if (externalClaimDialectURI.equals(LOCAL_CLAIM_DIALECT_URI)) {
            throw new ClaimMetadataException("Invalid external claim dialect URI: " + externalClaimDialectURI);
        }

        return claims.getOrDefault(externalClaimDialectURI, Collections.emptyList()).stream()
                .map(ExternalClaim.class::cast)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<ExternalClaim> getExternalClaim(String externalClaimDialectURI, String claimURI, int tenantId)
            throws ClaimMetadataException {

        if (StringUtils.isBlank(externalClaimDialectURI) || StringUtils.isBlank(claimURI)) {
            throw new ClaimMetadataException("Invalid external claim dialect URI or claim URI");
        }

        if (externalClaimDialectURI.equals(LOCAL_CLAIM_DIALECT_URI)) {
            throw new ClaimMetadataException("Invalid external claim dialect URI: " + externalClaimDialectURI);
        }

        return claims.getOrDefault(externalClaimDialectURI, Collections.emptyList()).stream()
                .filter(claim -> claimURI.equals(claim.getClaimURI()))
                .map(ExternalClaim.class::cast)
                .findFirst();
    }

    @Override
    public List<Claim> getMappedExternalClaims(String localClaimURI, int tenantId) throws ClaimMetadataException {

        if (StringUtils.isBlank(localClaimURI)) {
            throw new ClaimMetadataException("Invalid local claim URI: " + localClaimURI);
        }

        List<Claim> mappedExternalClaims = new ArrayList<>();
        for (Map.Entry<String, List<Claim>> entry : claims.entrySet()) {
            if (LOCAL_CLAIM_DIALECT_URI.equals(entry.getKey())) {
                continue;
            }
            List<Claim> externalClaims = entry.getValue().stream()
                    .map(ExternalClaim.class::cast)
                    .filter(claim -> localClaimURI.equals(claim.getMappedLocalClaim()))
                    .map(Claim.class::cast)
                    .collect(Collectors.toList());
            mappedExternalClaims.addAll(externalClaims);
        }
        return mappedExternalClaims;
    }

    @Override
    public boolean isMappedLocalClaim(String localClaimURI, int tenantId) throws ClaimMetadataException {

        if (StringUtils.isBlank(localClaimURI)) {
            throw new ClaimMetadataException("Invalid local claim URI: " + localClaimURI);
        }

        for (Map.Entry<String, List<Claim>> entry : claims.entrySet()) {
            if (LOCAL_CLAIM_DIALECT_URI.equals(entry.getKey())) {
                continue;
            }
            boolean isMapped = entry.getValue().stream()
                    .filter(claim -> claim instanceof ExternalClaim)
                    .map(ExternalClaim.class::cast)
                    .anyMatch(claim -> localClaimURI.equals(claim.getMappedLocalClaim()));

            if (isMapped) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean isLocalClaimMappedWithinDialect(String mappedLocalClaim, String externalClaimDialectURI, int tenantId) throws ClaimMetadataException {

        if (StringUtils.isBlank(externalClaimDialectURI) || StringUtils.isBlank(mappedLocalClaim)) {
            throw new ClaimMetadataException("Invalid external claim dialect URI or mapped local claim");
        }
        if (!claims.containsKey(externalClaimDialectURI)) {
            return false;
        }
        return claims.get(externalClaimDialectURI).stream()
                .filter(claim -> claim instanceof ExternalClaim)
                .map(ExternalClaim.class::cast)
                .anyMatch(claim -> mappedLocalClaim.equals(claim.getMappedLocalClaim()));
    }
}
