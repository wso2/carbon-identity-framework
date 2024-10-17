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

import org.wso2.carbon.identity.claim.metadata.mgt.exception.ClaimMetadataClientException;
import org.wso2.carbon.identity.claim.metadata.mgt.exception.ClaimMetadataException;
import org.wso2.carbon.identity.claim.metadata.mgt.model.AttributeMapping;
import org.wso2.carbon.identity.claim.metadata.mgt.model.Claim;
import org.wso2.carbon.identity.claim.metadata.mgt.model.ClaimDialect;
import org.wso2.carbon.identity.claim.metadata.mgt.model.ExternalClaim;
import org.wso2.carbon.identity.claim.metadata.mgt.model.LocalClaim;
import org.wso2.carbon.identity.claim.metadata.mgt.util.ClaimConstants;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.wso2.carbon.identity.claim.metadata.mgt.util.ClaimConstants.ErrorMessage.ERROR_CODE_NON_EXISTING_LOCAL_CLAIM_URI;

public class UnifiedClaimMetadataManager implements ClaimMetadataManager {

    private final SystemDefaultClaimMetadataManager systemDefaultClaimMetadataManager =
            new SystemDefaultClaimMetadataManager();
    private final DBBasedClaimMetadataManager dbBasedClaimMetadataManager = new DBBasedClaimMetadataManager();

    @Override
    public List<ClaimDialect> getClaimDialects(int tenantId) throws ClaimMetadataException {

        List<ClaimDialect> claimDialectsInDB = this.dbBasedClaimMetadataManager.getClaimDialects(tenantId);
        List<ClaimDialect> claimDialectsInSystem = this.systemDefaultClaimMetadataManager.getClaimDialects(tenantId);
        Set<String> claimDialectURIsInDB = claimDialectsInDB.stream()
                .map(ClaimDialect::getClaimDialectURI)
                .collect(Collectors.toSet());

        List<ClaimDialect> finalClaimDialects = new ArrayList<>(claimDialectsInDB);
        claimDialectsInSystem.stream()
                .filter(claimDialect -> !claimDialectURIsInDB.contains(claimDialect.getClaimDialectURI()))
                .forEach(finalClaimDialects::add);
        return finalClaimDialects;
    }

    @Override
    public ClaimDialect getClaimDialect(String claimDialectURI, int tenantId) throws ClaimMetadataException {

        ClaimDialect claimDialectInDB = this.dbBasedClaimMetadataManager.getClaimDialect(claimDialectURI, tenantId);
        if (claimDialectInDB != null) {
            return claimDialectInDB;
        }
        return this.systemDefaultClaimMetadataManager.getClaimDialect(claimDialectURI, tenantId);
    }

    @Override
    public void addClaimDialect(ClaimDialect claimDialect, int tenantId) throws ClaimMetadataException {

        this.dbBasedClaimMetadataManager.addClaimDialect(claimDialect, tenantId);
    }

    @Override
    public void renameClaimDialect(ClaimDialect oldClaimDialect, ClaimDialect newClaimDialect, int tenantId)
            throws ClaimMetadataException {

        this.dbBasedClaimMetadataManager.renameClaimDialect(oldClaimDialect, newClaimDialect, tenantId);
    }

    @Override
    public void removeClaimDialect(ClaimDialect claimDialect, int tenantId) throws ClaimMetadataException {

        this.dbBasedClaimMetadataManager.removeClaimDialect(claimDialect, tenantId);
    }

    @Override
    public List<LocalClaim> getLocalClaims(int tenantId) throws ClaimMetadataException {

        List<LocalClaim> localClaimsInSystem = this.systemDefaultClaimMetadataManager.getLocalClaims(tenantId);
        List<LocalClaim> localClaimsInDB = this.dbBasedClaimMetadataManager.getLocalClaims(tenantId);

        Set<String> claimURIsInDB = localClaimsInDB.stream()
                .map(LocalClaim::getClaimURI)
                .collect(Collectors.toSet());

        List<LocalClaim> finalLocalClaims = new ArrayList<>(localClaimsInDB);
        localClaimsInSystem.stream()
                .filter(localClaim -> !claimURIsInDB.contains(localClaim.getClaimURI()))
                .forEach(finalLocalClaims::add);

        return finalLocalClaims;
    }

    @Override
    public LocalClaim getLocalClaim(String localClaimURI, int tenantId) throws ClaimMetadataException {

        LocalClaim localClaim = this.dbBasedClaimMetadataManager.getLocalClaim(localClaimURI, tenantId);
        if (localClaim == null) {
            localClaim = this.systemDefaultClaimMetadataManager.getLocalClaim(localClaimURI, tenantId);
        }
        return localClaim;
    }

    @Override
    public void addLocalClaim(LocalClaim localClaim, int tenantId) throws ClaimMetadataException {

        if (!isClaimDialectInDB(ClaimConstants.LOCAL_CLAIM_DIALECT_URI, tenantId)) {
            addSystemDefaultDialectToDB(ClaimConstants.LOCAL_CLAIM_DIALECT_URI, tenantId);
        }
        this.dbBasedClaimMetadataManager.addLocalClaim(localClaim, tenantId);
    }

    @Override
    public void updateLocalClaim(LocalClaim localClaim, int tenantId) throws ClaimMetadataException {

        if (isLocalClaimInDB(localClaim.getClaimURI(), tenantId)) {
            this.dbBasedClaimMetadataManager.updateLocalClaim(localClaim, tenantId);
        } else {
            this.addLocalClaim(localClaim, tenantId);
        }
    }

    @Override
    public void updateLocalClaimMappings(List<LocalClaim> localClaimList, int tenantId, String userStoreDomain)
            throws ClaimMetadataException {

        if (!localClaimList.isEmpty() && !isClaimDialectInDB(ClaimConstants.LOCAL_CLAIM_DIALECT_URI, tenantId)) {
            addSystemDefaultDialectToDB(ClaimConstants.LOCAL_CLAIM_DIALECT_URI, tenantId);
        }

        Map<String, LocalClaim> localClaimMap = this.getLocalClaims(tenantId).stream()
                .collect(Collectors.toMap(LocalClaim::getClaimURI, localClaim -> localClaim));
        for (LocalClaim localClaim : localClaimList) {
            if (localClaimMap.get(localClaim.getClaimURI()) == null) {
                throw new ClaimMetadataClientException(ERROR_CODE_NON_EXISTING_LOCAL_CLAIM_URI.getCode(),
                        String.format(ERROR_CODE_NON_EXISTING_LOCAL_CLAIM_URI.getMessage(), localClaim.getClaimURI()));
            }
            List<AttributeMapping> missingMappedAttributes = localClaimMap.get(localClaim.getClaimURI())
                    .getMappedAttributes().stream()
                    .filter(mappedAttribute -> !mappedAttribute.getUserStoreDomain().equals(userStoreDomain))
                    .collect(Collectors.toList());
            localClaim.getMappedAttributes().addAll(missingMappedAttributes);
            localClaim.setClaimProperties(localClaimMap.get(localClaim.getClaimURI()).getClaimProperties());
        }
        this.dbBasedClaimMetadataManager.updateLocalClaimMappings(localClaimList, tenantId, userStoreDomain);
    }

    @Override
    public void removeLocalClaim(String localClaimURI, int tenantId) throws ClaimMetadataException {

        this.dbBasedClaimMetadataManager.removeLocalClaim(localClaimURI, tenantId);
    }

    @Override
    public List<ExternalClaim> getExternalClaims(String externalClaimDialectURI, int tenantId)
            throws ClaimMetadataException {

        List<ExternalClaim> externalClaimsInSystem = this.systemDefaultClaimMetadataManager.getExternalClaims(
                externalClaimDialectURI, tenantId);
        List<ExternalClaim> externalClaimsInDB = this.dbBasedClaimMetadataManager.getExternalClaims(
                externalClaimDialectURI, tenantId);

        Map<String, ExternalClaim> externalClaimsInDBMap = externalClaimsInDB.stream()
                .collect(Collectors.toMap(ExternalClaim::getClaimURI, claim -> claim));

        List<ExternalClaim> finalExternalClaims = new ArrayList<>();
        for (ExternalClaim externalClaimInSystem : externalClaimsInSystem) {
            ExternalClaim matchingClaimInDB = externalClaimsInDBMap.get(externalClaimInSystem.getClaimURI());
            if (matchingClaimInDB != null) {
                finalExternalClaims.add(matchingClaimInDB);
                externalClaimsInDBMap.remove(externalClaimInSystem.getClaimURI());
            } else {
                finalExternalClaims.add(externalClaimInSystem);
            }
        }
        finalExternalClaims.addAll(externalClaimsInDBMap.values());
        return finalExternalClaims;
    }

    @Override
    public ExternalClaim getExternalClaim(String externalClaimDialectURI, String claimURI, int tenantId)
            throws ClaimMetadataException {

        ExternalClaim externalClaim = this.dbBasedClaimMetadataManager.getExternalClaim(
                externalClaimDialectURI, claimURI, tenantId);
        if (externalClaim == null) {
            externalClaim = this.systemDefaultClaimMetadataManager.getExternalClaim(
                    externalClaimDialectURI, claimURI, tenantId);
        }
        return externalClaim;
    }

    @Override
    public void addExternalClaim(ExternalClaim externalClaim, int tenantId)
            throws ClaimMetadataException {

        if (!isClaimDialectInDB(externalClaim.getClaimDialectURI(), tenantId)) {
            addSystemDefaultDialectToDB(externalClaim.getClaimDialectURI(), tenantId);
        }
        if (!isLocalClaimInDB(externalClaim.getMappedLocalClaim(), tenantId)) {
            addSystemDefaultLocalClaimToDB(externalClaim.getMappedLocalClaim(), tenantId);
        }
        this.dbBasedClaimMetadataManager.addExternalClaim(externalClaim, tenantId);
    }

    @Override
    public void updateExternalClaim(ExternalClaim externalClaim, int tenantId)
            throws ClaimMetadataException {

        if (isExternalClaimInDB(externalClaim.getClaimURI(), externalClaim.getClaimDialectURI(), tenantId)) {
            this.dbBasedClaimMetadataManager.updateExternalClaim(externalClaim, tenantId);
        } else {
            this.addExternalClaim(externalClaim, tenantId);
        }
    }

    @Override
    public void removeExternalClaim(String externalClaimDialectURI, String externalClaimURI, int tenantId)
            throws ClaimMetadataException {

        this.dbBasedClaimMetadataManager.removeExternalClaim(externalClaimDialectURI, externalClaimURI, tenantId);
    }

    @Override
    public boolean isMappedLocalClaim(String localClaimURI, int tenantId) throws ClaimMetadataException {

        List<ClaimDialect> claimDialects = this.getClaimDialects(tenantId);

        for (ClaimDialect claimDialect : claimDialects) {
            if (claimDialect.getClaimDialectURI().equals(ClaimConstants.LOCAL_CLAIM_DIALECT_URI)) {
                continue;
            }
            List<ExternalClaim> externalClaims = getExternalClaims(claimDialect.getClaimDialectURI(), tenantId);
            for (ExternalClaim externalClaim : externalClaims) {
                if (externalClaim.getMappedLocalClaim().equals(localClaimURI)) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public void removeClaimMappingAttributes(int tenantId, String userstoreDomain) throws ClaimMetadataException {

        this.dbBasedClaimMetadataManager.removeClaimMappingAttributes(tenantId, userstoreDomain);
    }

    @Override
    public void removeAllClaimDialects(int tenantId) throws ClaimMetadataException {

        this.dbBasedClaimMetadataManager.removeAllClaimDialects(tenantId);
    }

    @Override
    public List<Claim> getMappedExternalClaims(String localClaimURI, int tenantId) throws ClaimMetadataException {

        List<Claim> mappedExternalClaims = new ArrayList<>();
        List<ClaimDialect> claimDialects = getClaimDialects(tenantId);
        for (ClaimDialect claimDialect : claimDialects) {
            if (claimDialect.getClaimDialectURI().equals(ClaimConstants.LOCAL_CLAIM_DIALECT_URI)) {
                continue;
            }
            List<ExternalClaim> externalClaimsInDialect = getExternalClaims(claimDialect.getClaimDialectURI(),
                    tenantId);
            for (ExternalClaim externalClaim : externalClaimsInDialect) {
                if (externalClaim.getMappedLocalClaim().equals(localClaimURI)) {
                    mappedExternalClaims.add(externalClaim);
                }
            }
        }
        return mappedExternalClaims;
    }

    @Override
    public boolean isLocalClaimMappedWithinDialect(String mappedLocalClaim, String externalClaimDialectURI,
                                                   int tenantId) throws ClaimMetadataException {

        List<ExternalClaim> externalClaims = getExternalClaims(externalClaimDialectURI, tenantId);
        for (ExternalClaim externalClaim : externalClaims) {
            if (externalClaim.getMappedLocalClaim().equals(mappedLocalClaim)) {
                return true;
            }
        }
        return false;
    }

    public boolean isSystemDefaultClaimDialect(String claimDialectURI, int tenantId) throws ClaimMetadataException {

        ClaimDialect claimDialectInSystem = this.systemDefaultClaimMetadataManager.getClaimDialect(claimDialectURI,
                tenantId);
        return claimDialectInSystem != null;
    }

    public boolean isSystemDefaultLocalClaim(String localClaimURI, int tenantId) throws ClaimMetadataException {

        LocalClaim localClaimInSystem = this.systemDefaultClaimMetadataManager.getLocalClaims(tenantId).stream()
                .filter(localClaim -> localClaim.getClaimURI().equals(localClaimURI))
                .findFirst()
                .orElse(null);
        return localClaimInSystem != null;
    }

    public boolean isSystemDefaultExternalClaim(String externalClaimDialectURI, String externalClaimURI, int tenantId)
            throws ClaimMetadataException {

        ExternalClaim externalClaimInSystem = this.systemDefaultClaimMetadataManager.getExternalClaims(
                        externalClaimDialectURI, tenantId).stream()
                .filter(externalClaim -> externalClaim.getClaimURI().equals(externalClaimURI))
                .findFirst()
                .orElse(null);
        return externalClaimInSystem != null;
    }

    private boolean isClaimDialectInDB(String claimDialectURI, int tenantId) throws ClaimMetadataException {

        return this.dbBasedClaimMetadataManager.getClaimDialect(claimDialectURI, tenantId) != null;
    }

    private boolean isLocalClaimInDB(String localClaimURI, int tenantId) throws ClaimMetadataException {

        return this.dbBasedClaimMetadataManager.getLocalClaim(localClaimURI, tenantId) != null;
    }

    private boolean isExternalClaimInDB(String claimURI, String claimDialectURI, int tenantId)
            throws ClaimMetadataException {

        return this.dbBasedClaimMetadataManager.getExternalClaim(claimDialectURI, claimURI, tenantId) != null;
    }

    private void addSystemDefaultDialectToDB(String claimDialectURI, int tenantId) throws ClaimMetadataException {

        ClaimDialect claimDialectInSystem = this.systemDefaultClaimMetadataManager.getClaimDialect(claimDialectURI,
                tenantId);
        this.dbBasedClaimMetadataManager.addClaimDialect(claimDialectInSystem, tenantId);
    }

    private void addSystemDefaultLocalClaimToDB(String claimURI, int tenantId)
            throws ClaimMetadataException {

        boolean isClaimDialectInDB = isClaimDialectInDB(ClaimConstants.LOCAL_CLAIM_DIALECT_URI, tenantId);
        if (!isClaimDialectInDB) {
            addSystemDefaultDialectToDB(ClaimConstants.LOCAL_CLAIM_DIALECT_URI, tenantId);
        }
        LocalClaim claimInSystem = this.systemDefaultClaimMetadataManager.getLocalClaim(claimURI, tenantId);
        this.dbBasedClaimMetadataManager.addLocalClaim(claimInSystem, tenantId);
    }
}
