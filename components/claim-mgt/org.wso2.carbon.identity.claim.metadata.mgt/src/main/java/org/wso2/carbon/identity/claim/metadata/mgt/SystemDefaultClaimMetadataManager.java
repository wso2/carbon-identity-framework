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
import org.wso2.carbon.identity.claim.metadata.mgt.internal.IdentityClaimManagementServiceDataHolder;
import org.wso2.carbon.identity.claim.metadata.mgt.model.AttributeMapping;
import org.wso2.carbon.identity.claim.metadata.mgt.model.Claim;
import org.wso2.carbon.identity.claim.metadata.mgt.model.ClaimDialect;
import org.wso2.carbon.identity.claim.metadata.mgt.model.ExternalClaim;
import org.wso2.carbon.identity.claim.metadata.mgt.model.LocalClaim;
import org.wso2.carbon.identity.claim.metadata.mgt.util.ClaimConstants;
import org.wso2.carbon.identity.core.util.IdentityUtil;
import org.wso2.carbon.user.core.claim.ClaimKey;
import org.wso2.carbon.user.core.claim.ClaimMapping;
import org.wso2.carbon.user.core.claim.inmemory.ClaimConfig;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.wso2.carbon.identity.claim.metadata.mgt.util.ClaimConstants.LOCAL_CLAIM_DIALECT_URI;

/**
 * System default claim metadata manager.
 */
public class SystemDefaultClaimMetadataManager implements ClaimMetadataReader {

    private static final List<ClaimDialect> claimDialects;
    private static final Map<String, List<Claim>> claims;

    static {

        claimDialects = new ArrayList<>();
        claims = new HashMap<>();

        ClaimConfig claimConfig = IdentityClaimManagementServiceDataHolder.getInstance().getClaimConfig();
        if (claimConfig != null && claimConfig.getClaimMap() != null) {
            String primaryDomainName = IdentityUtil.getPrimaryDomainName();

            for (Map.Entry<ClaimKey, ClaimMapping> entry : claimConfig.getClaimMap().entrySet()) {
                ClaimKey claimKey = entry.getKey();
                ClaimMapping claimMapping = entry.getValue();
                String claimDialectURI = claimKey.getDialectUri();
                String claimURI = claimKey.getClaimUri();
                Claim claim;

                boolean dialectExists = claimDialects.stream()
                        .anyMatch(claimDialect -> claimDialect.getClaimDialectURI().equals(claimDialectURI));
                if (!dialectExists) {
                    claimDialects.add(new ClaimDialect(claimDialectURI));
                }

                if (claimDialectURI.equals(LOCAL_CLAIM_DIALECT_URI)) {
                    List<AttributeMapping> mappedAttributes = new ArrayList<>();
                    if (StringUtils.isNotBlank(claimMapping.getMappedAttribute())) {
                        mappedAttributes
                                .add(new AttributeMapping(primaryDomainName, claimMapping.getMappedAttribute()));
                    }

                    if (claimMapping.getMappedAttributes() != null) {
                        for (Map.Entry<String, String> claimMappingEntry : claimMapping.getMappedAttributes()
                                .entrySet()) {
                            mappedAttributes.add(new AttributeMapping(claimMappingEntry.getKey(),
                                    claimMappingEntry.getValue()));
                        }
                    }

                    Map<String, String> claimProperties = filterClaimProperties(claimConfig.getPropertyHolderMap()
                            .get(claimKey));
                    claim = new LocalClaim(claimURI, mappedAttributes, claimProperties);
                } else {
                    String mappedLocalClaimURI = claimConfig.getPropertyHolderMap().get(claimKey).get(ClaimConstants
                            .MAPPED_LOCAL_CLAIM_PROPERTY);
                    Map<String, String> claimProperties = filterClaimProperties(claimConfig.getPropertyHolderMap()
                            .get(claimKey));
                    claim = new ExternalClaim(claimDialectURI, claimURI, mappedLocalClaimURI, claimProperties);
                }
                claims.computeIfAbsent(claimDialectURI, k -> new ArrayList<>());
                claims.get(claimDialectURI).add(claim);
            }
        }
    }

    @Override
    public List<ClaimDialect> getClaimDialects(int tenantId) throws ClaimMetadataException {

        return claimDialects;
    }

    @Override
    public ClaimDialect getClaimDialect(String claimDialectURI, int tenantId) throws ClaimMetadataException {

        if (StringUtils.isBlank(claimDialectURI)) {
            return null;
        }

        return claimDialects.stream()
                .filter(claimDialect -> claimDialectURI.equals(claimDialect.getClaimDialectURI()))
                .findFirst()
                .orElse(null);
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
    public LocalClaim getLocalClaim(String localClaimURI ,int tenantId) throws ClaimMetadataException {

        if (StringUtils.isBlank(localClaimURI)) {
            return null;
        }

        return claims.getOrDefault(LOCAL_CLAIM_DIALECT_URI, Collections.emptyList()).stream()
                .filter(claim -> localClaimURI.equals(claim.getClaimURI()))
                .map(LocalClaim.class::cast)
                .findFirst()
                .orElse(null);
    }

    @Override
    public List<ExternalClaim> getExternalClaims(String externalClaimDialectURI, int tenantId)
            throws ClaimMetadataException {

        if (StringUtils.isBlank(externalClaimDialectURI)) {
            return null;
        }

        if (externalClaimDialectURI.equals(LOCAL_CLAIM_DIALECT_URI)) {
            throw new ClaimMetadataException("Invalid external claim dialect URI: " + externalClaimDialectURI);
        }

        return claims.getOrDefault(externalClaimDialectURI, Collections.emptyList()).stream()
                .map(ExternalClaim.class::cast)
                .collect(Collectors.toList());
    }

    @Override
    public ExternalClaim getExternalClaim(String externalClaimDialectURI, String claimURI, int tenantId) throws ClaimMetadataException {

        if (StringUtils.isBlank(externalClaimDialectURI) || StringUtils.isBlank(claimURI)) {
            return null;
        }

        if (externalClaimDialectURI.equals(LOCAL_CLAIM_DIALECT_URI)) {
            throw new ClaimMetadataException("Invalid external claim dialect URI: " + externalClaimDialectURI);
        }

        return claims.getOrDefault(externalClaimDialectURI, Collections.emptyList()).stream()
                .filter(claim -> claimURI.equals(claim.getClaimURI()))
                .map(ExternalClaim.class::cast)
                .findFirst()
                .orElse(null);
    }

    @Override
    public List<Claim> getMappedExternalClaims(String localClaimURI, int tenantId) throws ClaimMetadataException {

        if (StringUtils.isBlank(localClaimURI)) {
            return null;
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
            return false;
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

        if (StringUtils.isBlank(externalClaimDialectURI) || !claims.containsKey(externalClaimDialectURI)) {
            return false;
        }
        if (StringUtils.isBlank(mappedLocalClaim)) {
            return false;
        }
        return claims.get(externalClaimDialectURI).stream()
                .filter(claim -> claim instanceof ExternalClaim)
                .map(ExternalClaim.class::cast)
                .anyMatch(claim -> mappedLocalClaim.equals(claim.getMappedLocalClaim()));
    }

    private static Map<String, String> filterClaimProperties(Map<String, String> claimProperties) {

        claimProperties.remove(ClaimConstants.DIALECT_PROPERTY);
        claimProperties.remove(ClaimConstants.CLAIM_URI_PROPERTY);
        claimProperties.remove(ClaimConstants.ATTRIBUTE_ID_PROPERTY);

        if (!claimProperties.containsKey(ClaimConstants.DISPLAY_NAME_PROPERTY)) {
            claimProperties.put(ClaimConstants.DISPLAY_NAME_PROPERTY, "0");
        }

        if (claimProperties.containsKey(ClaimConstants.SUPPORTED_BY_DEFAULT_PROPERTY)) {
            if (StringUtils.isBlank(claimProperties.get(ClaimConstants.SUPPORTED_BY_DEFAULT_PROPERTY))) {
                claimProperties.put(ClaimConstants.SUPPORTED_BY_DEFAULT_PROPERTY, "true");
            }
        }

        if (claimProperties.containsKey(ClaimConstants.READ_ONLY_PROPERTY)) {
            if (StringUtils.isBlank(claimProperties.get(ClaimConstants.READ_ONLY_PROPERTY))) {
                claimProperties.put(ClaimConstants.READ_ONLY_PROPERTY, "true");
            }
        }

        if (claimProperties.containsKey(ClaimConstants.REQUIRED_PROPERTY)) {
            if (StringUtils.isBlank(claimProperties.get(ClaimConstants.REQUIRED_PROPERTY))) {
                claimProperties.put(ClaimConstants.REQUIRED_PROPERTY, "true");
            }
        }
        return claimProperties;
    }
}
