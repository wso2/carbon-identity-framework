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

public class SystemDefaultClaimMetadataManager implements ClaimMetadataManager {

    private static final List<ClaimDialect> claimDialects;
    private static final Map<String, List<Claim>> claims;

    static {

        claimDialects = new ArrayList<>();
        claims = new HashMap<>();

        ClaimConfig claimConfig = IdentityClaimManagementServiceDataHolder.getInstance().getClaimConfig();
        if (claimConfig.getClaimMap() != null) {
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

        if (claimDialectURI == null || StringUtils.isBlank(claimDialectURI)) {
            return null;
        }

        return claimDialects.stream()
                .filter(claimDialect -> claimDialectURI.equals(claimDialect.getClaimDialectURI()))
                .findFirst()
                .orElse(null);
    }

    @Override
    public void addClaimDialect(ClaimDialect claimDialect, int tenantId) throws ClaimMetadataException {

        throw new UnsupportedOperationException("Unsupported operation for SystemDefaultClaimMetadataManager.");
    }

    @Override
    public void renameClaimDialect(ClaimDialect oldClaimDialect, ClaimDialect newClaimDialect, int tenantId)
            throws ClaimMetadataException {

        throw new UnsupportedOperationException("Unsupported operation for SystemDefaultClaimMetadataManager.");
    }

    @Override
    public void removeClaimDialect(ClaimDialect claimDialect, int tenantId) throws ClaimMetadataException {

        throw new UnsupportedOperationException("Unsupported operation for SystemDefaultClaimMetadataManager.");
    }

    @Override
    public List<LocalClaim> getLocalClaims(int tenantId) throws ClaimMetadataException {

        List<Claim> localClaims = claims.get(LOCAL_CLAIM_DIALECT_URI);

        if (localClaims == null) {
            throw new ClaimMetadataException("No local claims found.");
        }

        return localClaims.stream()
                .map(LocalClaim.class::cast)
                .collect(Collectors.toList());
    }

    @Override
    public LocalClaim getLocalClaim(String localClaimURI ,int tenantId) throws ClaimMetadataException {

        if (localClaimURI == null || StringUtils.isBlank(localClaimURI)) {
            return null;
        }

        return claims.getOrDefault(LOCAL_CLAIM_DIALECT_URI, Collections.emptyList()).stream()
                .filter(claim -> localClaimURI.equals(claim.getClaimURI()))
                .map(LocalClaim.class::cast)
                .findFirst()
                .orElse(null);
    }

    @Override
    public void addLocalClaim(LocalClaim localClaim, int tenantId) throws ClaimMetadataException {

        throw new UnsupportedOperationException("Unsupported operation for SystemDefaultClaimMetadataManager.");
    }

    @Override
    public void updateLocalClaim(LocalClaim localClaim, int tenantId) throws ClaimMetadataException {

        throw new UnsupportedOperationException("Unsupported operation for SystemDefaultClaimMetadataManager.");
    }

    @Override
    public void updateLocalClaimMappings(List<LocalClaim> localClaimList, int tenantId, String userStoreDomain)
            throws ClaimMetadataException {

        throw new UnsupportedOperationException("Unsupported operation for SystemDefaultClaimMetadataManager.");
    }

    @Override
    public void removeLocalClaim(String localClaimURI, int tenantId) throws ClaimMetadataException {

        throw new UnsupportedOperationException("Unsupported operation for SystemDefaultClaimMetadataManager.");
    }

    @Override
    public void removeClaimMappingAttributes(int tenantId, String userstoreDomain) throws ClaimMetadataException {

        throw new UnsupportedOperationException("Unsupported operation for SystemDefaultClaimMetadataManager.");
    }

    @Override
    public List<ExternalClaim> getExternalClaims(String externalClaimDialectURI, int tenantId)
            throws ClaimMetadataException {

        if (externalClaimDialectURI == null || StringUtils.isBlank(externalClaimDialectURI)) {
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

        if (externalClaimDialectURI == null || StringUtils.isBlank(externalClaimDialectURI) || claimURI == null ||
                StringUtils.isBlank(claimURI)) {
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
    public void addExternalClaim(ExternalClaim externalClaim, int tenantId) throws ClaimMetadataException {

        throw new UnsupportedOperationException("Unsupported operation for SystemDefaultClaimMetadataManager.");
    }

    @Override
    public void updateExternalClaim(ExternalClaim externalClaim, int tenantId) throws ClaimMetadataException {

        throw new UnsupportedOperationException("Unsupported operation for SystemDefaultClaimMetadataManager.");
    }

    @Override
    public void removeExternalClaim(String externalClaimDialectURI, String externalClaimURI, int tenantId)
            throws ClaimMetadataException {

        throw new UnsupportedOperationException("Unsupported operation for SystemDefaultClaimMetadataManager.");
    }

    @Override
    public List<Claim> getMappedExternalClaims(String localClaimURI, int tenantId) throws ClaimMetadataException {

        if (localClaimURI == null || StringUtils.isBlank(localClaimURI)) {
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
    public void removeAllClaimDialects(int tenantId) throws ClaimMetadataException {

        throw new UnsupportedOperationException("Unsupported operation for SystemDefaultClaimMetadataManager.");
    }

    @Override
    public boolean isMappedLocalClaim(String localClaimURI, int tenantId) throws ClaimMetadataException {

        if (localClaimURI == null || StringUtils.isBlank(localClaimURI)) {
            return false;
        }

        for (Map.Entry<String, List<Claim>> entry : claims.entrySet()) {
            if (LOCAL_CLAIM_DIALECT_URI.equals(entry.getKey())) {
                continue;
            }
            List<Claim> externalClaims = entry.getValue().stream()
                    .map(ExternalClaim.class::cast)
                    .filter(claim -> localClaimURI.equals(claim.getMappedLocalClaim()))
                    .collect(Collectors.toList());
            if (!externalClaims.isEmpty()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean isLocalClaimMappedWithinDialect(String mappedLocalClaim, String externalClaimDialectURI, int tenantId) throws ClaimMetadataException {

        if (externalClaimDialectURI == null || StringUtils.isBlank(externalClaimDialectURI)
                || !claims.containsKey(externalClaimDialectURI)) {
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
