/*
 * Copyright (c) 2025, WSO2 LLC. (http://www.wso2.com).
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
import org.wso2.carbon.identity.claim.metadata.mgt.dao.CacheBackedClaimDialectDAO;
import org.wso2.carbon.identity.claim.metadata.mgt.dao.CacheBackedExternalClaimDAO;
import org.wso2.carbon.identity.claim.metadata.mgt.dao.CacheBackedLocalClaimDAO;
import org.wso2.carbon.identity.claim.metadata.mgt.dao.ExternalClaimDAO;
import org.wso2.carbon.identity.claim.metadata.mgt.dao.LocalClaimDAO;
import org.wso2.carbon.identity.claim.metadata.mgt.exception.ClaimMetadataException;
import org.wso2.carbon.identity.claim.metadata.mgt.exception.ClaimMetadataServerException;
import org.wso2.carbon.identity.claim.metadata.mgt.internal.ReadWriteClaimMetadataManager;
import org.wso2.carbon.identity.claim.metadata.mgt.model.Claim;
import org.wso2.carbon.identity.claim.metadata.mgt.model.ClaimDialect;
import org.wso2.carbon.identity.claim.metadata.mgt.model.ExternalClaim;
import org.wso2.carbon.identity.claim.metadata.mgt.model.LocalClaim;
import org.wso2.carbon.identity.claim.metadata.mgt.util.ClaimConstants;
import org.wso2.carbon.user.api.UserStoreException;

import java.util.List;
import java.util.Optional;

/**
 * Cache backed database-based claim metadata manager.
 */
public class CacheBackedDBBasedClaimMetadataManager implements ReadWriteClaimMetadataManager {

    private final LocalClaimDAO localClaimDAO = new LocalClaimDAO();
    private final ExternalClaimDAO externalClaimDAO = new ExternalClaimDAO();
    private final CacheBackedClaimDialectDAO cacheBackedClaimDialectDAO = new CacheBackedClaimDialectDAO();
    private final CacheBackedLocalClaimDAO cacheBackedLocalClaimDAO = new CacheBackedLocalClaimDAO(localClaimDAO);
    private final CacheBackedExternalClaimDAO cacheBackedExternalClaimDAO =
            new CacheBackedExternalClaimDAO(externalClaimDAO);

    @Override
    public List<ClaimDialect> getClaimDialects(int tenantId) throws ClaimMetadataException {

        return this.cacheBackedClaimDialectDAO.getClaimDialects(tenantId);
    }

    @Override
    public Optional<ClaimDialect> getClaimDialect(String claimDialectURI, int tenantId) throws ClaimMetadataException {

        if (StringUtils.isBlank(claimDialectURI)) {
            throw new ClaimMetadataException("Invalid claim dialect URI: " + claimDialectURI);
        }

        return this.cacheBackedClaimDialectDAO.getClaimDialects(tenantId).stream()
                .filter(claimDialect -> claimDialectURI.equals(claimDialect.getClaimDialectURI()))
                .findFirst();
    }

    @Override
    public void addClaimDialect(ClaimDialect claimDialect, int tenantId) throws ClaimMetadataException {

        this.cacheBackedClaimDialectDAO.addClaimDialect(claimDialect, tenantId);
    }

    @Override
    public void removeClaimDialect(ClaimDialect claimDialect, int tenantId) throws ClaimMetadataException {

        this.cacheBackedClaimDialectDAO.removeClaimDialect(claimDialect, tenantId);
    }

    @Override
    public List<LocalClaim> getLocalClaims(int tenantId) throws ClaimMetadataException {

        return this.cacheBackedLocalClaimDAO.getLocalClaims(tenantId);
    }

    @Override
    public Optional<LocalClaim> getLocalClaim(String localClaimURI , int tenantId) throws ClaimMetadataException {

        if (StringUtils.isBlank(localClaimURI)) {
            throw new ClaimMetadataException("Invalid local claim URI: " + localClaimURI);
        }
        return this.cacheBackedLocalClaimDAO.getLocalClaims(tenantId).stream()
                .filter(localClaim -> localClaimURI.equals(localClaim.getClaimURI()))
                .findFirst();
    }

    @Override
    public List<ExternalClaim> getExternalClaims(String externalClaimDialectURI, int tenantId)
            throws ClaimMetadataException {

        return this.cacheBackedExternalClaimDAO.getExternalClaims(externalClaimDialectURI, tenantId);
    }

    @Override
    public Optional<ExternalClaim> getExternalClaim(String externalClaimDialectURI, String claimURI, int tenantId)
            throws ClaimMetadataException {

        if (StringUtils.isBlank(externalClaimDialectURI) || StringUtils.isBlank(claimURI)) {
            throw new ClaimMetadataException("Invalid external claim dialect URI or claim URI");
        }

        return this.cacheBackedExternalClaimDAO.getExternalClaims(externalClaimDialectURI, tenantId).stream()
                .filter(externalClaim -> claimURI.equals(externalClaim.getClaimURI()))
                .findFirst();
    }

    @Override
    public void addLocalClaim(LocalClaim localClaim, int tenantId) throws ClaimMetadataException {

        this.cacheBackedLocalClaimDAO.addLocalClaim(localClaim, tenantId);
    }

    @Override
    public void updateLocalClaim(LocalClaim localClaim, int tenantId) throws ClaimMetadataException {

        this.cacheBackedLocalClaimDAO.updateLocalClaim(localClaim, tenantId);
    }

    @Override
    public void updateLocalClaimMappings(List<LocalClaim> localClaims, int tenantId, String userStoreDomain)
            throws ClaimMetadataException {

        this.cacheBackedLocalClaimDAO.updateLocalClaimMappings(localClaims, tenantId, userStoreDomain);
    }

    @Override
    public void removeLocalClaim(String localClaimURI, int tenantId) throws ClaimMetadataException {

        this.cacheBackedLocalClaimDAO.removeLocalClaim(localClaimURI, tenantId);
    }

    @Override
    public void removeClaimMappingAttributes(int tenantId, String userstoreDomain) throws ClaimMetadataException {

        try {
            this.cacheBackedLocalClaimDAO.removeClaimMappingAttributes(tenantId, userstoreDomain);
        } catch (UserStoreException e) {
            String errorMessage = String.format(
                    ClaimConstants.ErrorMessage.ERROR_CODE_SERVER_ERROR_DELETING_CLAIM_MAPPINGS.getMessage(),
                    tenantId, userstoreDomain);
            throw new ClaimMetadataServerException(
                    ClaimConstants.ErrorMessage.ERROR_CODE_SERVER_ERROR_DELETING_CLAIM_MAPPINGS.getCode(),
                    errorMessage, e);
        }
    }

    @Override
    public void addExternalClaim(ExternalClaim externalClaim, int tenantId) throws ClaimMetadataException {

        this.cacheBackedExternalClaimDAO.addExternalClaim(externalClaim, tenantId);
    }

    @Override
    public void updateExternalClaim(ExternalClaim externalClaim, int tenantId) throws ClaimMetadataException {

        this.cacheBackedExternalClaimDAO.updateExternalClaim(externalClaim, tenantId);
    }

    @Override
    public void removeExternalClaim(String externalClaimDialectURI, String externalClaimURI, int tenantId)
            throws ClaimMetadataException {

        this.cacheBackedExternalClaimDAO.removeExternalClaim(externalClaimDialectURI, externalClaimURI, tenantId);
    }

    @Override
    public List<Claim> getMappedExternalClaims(String localClaimURI, int tenantId) throws ClaimMetadataException {

        return this.cacheBackedLocalClaimDAO.fetchMappedExternalClaims(localClaimURI, tenantId);
    }

    @Override
    public void renameClaimDialect(ClaimDialect oldClaimDialect, ClaimDialect newClaimDialect, int tenantId)
            throws ClaimMetadataException {

        this.cacheBackedClaimDialectDAO.renameClaimDialect(oldClaimDialect, newClaimDialect, tenantId);
    }

    @Override
    public void removeAllClaimDialects(int tenantId) throws ClaimMetadataException {

        // The relevant external claim deletions are handled by the DB through ON DELETE CASCADE.
        this.cacheBackedClaimDialectDAO.removeAllClaimDialects(tenantId);
    }

    @Override
    public boolean isMappedLocalClaim(String localClaimURI, int tenantId) throws ClaimMetadataException {

        return this.cacheBackedExternalClaimDAO.isMappedLocalClaim(localClaimURI, tenantId);
    }

    @Override
    public boolean isLocalClaimMappedWithinDialect(String mappedLocalClaim, String externalClaimDialectURI,
                                                   int tenantId) throws ClaimMetadataException {

        return this.cacheBackedExternalClaimDAO.isLocalClaimMappedWithinDialect(mappedLocalClaim,
                externalClaimDialectURI, tenantId);
    }
}
