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

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.claim.metadata.mgt.dao.CacheBackedClaimDialectDAO;
import org.wso2.carbon.identity.claim.metadata.mgt.dao.CacheBackedExternalClaimDAO;
import org.wso2.carbon.identity.claim.metadata.mgt.dao.CacheBackedLocalClaimDAO;
import org.wso2.carbon.identity.claim.metadata.mgt.dao.ClaimDialectDAO;
import org.wso2.carbon.identity.claim.metadata.mgt.dao.ExternalClaimDAO;
import org.wso2.carbon.identity.claim.metadata.mgt.dao.LocalClaimDAO;
import org.wso2.carbon.identity.claim.metadata.mgt.exception.ClaimMetadataException;
import org.wso2.carbon.identity.claim.metadata.mgt.model.ClaimDialect;
import org.wso2.carbon.identity.claim.metadata.mgt.model.ExternalClaim;
import org.wso2.carbon.identity.claim.metadata.mgt.model.LocalClaim;
import org.wso2.carbon.identity.claim.metadata.mgt.util.ClaimConstants;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;

import java.util.List;

/**
 * Default implementation of {@link org.wso2.carbon.identity.claim.metadata.mgt.ClaimMetadataManagementService}
 * interface.
 */
public class ClaimMetadataManagementServiceImpl implements ClaimMetadataManagementService {

    private static final Log log = LogFactory.getLog(ClaimMetadataManagementServiceImpl.class);

    private ClaimDialectDAO claimDialectDAO = new CacheBackedClaimDialectDAO();
    private CacheBackedLocalClaimDAO localClaimDAO = new CacheBackedLocalClaimDAO(new LocalClaimDAO());
    private CacheBackedExternalClaimDAO externalClaimDAO = new CacheBackedExternalClaimDAO(new ExternalClaimDAO());


    @Override
    public List<ClaimDialect> getClaimDialects(String tenantDomain) throws ClaimMetadataException {

        // TODO : validate tenant domain?
        int tenantId = IdentityTenantUtil.getTenantId(tenantDomain);

        // Add listener

        List<ClaimDialect> claimDialects = this.claimDialectDAO.getClaimDialects(tenantId);

        // Add listener

        return claimDialects;
    }

    @Override
    public void addClaimDialect(ClaimDialect claimDialect, String tenantDomain) throws ClaimMetadataException {

        if (claimDialect == null || StringUtils.isBlank(claimDialect.getClaimDialectURI())) {
            throw new ClaimMetadataException("Claim dialect cannot be empty");
        }

        // TODO : validate claim dialect already exists?

        // TODO : validate tenant domain?
        int tenantId = IdentityTenantUtil.getTenantId(tenantDomain);

        // Add listener

        this.claimDialectDAO.addClaimDialect(claimDialect, tenantId);

        // Add listener

    }

    @Override
    public void renameClaimDialect(ClaimDialect oldClaimDialect, ClaimDialect newClaimDialect, String tenantDomain)
            throws ClaimMetadataException {

        if (oldClaimDialect == null || StringUtils.isBlank(oldClaimDialect.getClaimDialectURI())
                || newClaimDialect == null || StringUtils.isBlank(newClaimDialect.getClaimDialectURI())) {
            throw new ClaimMetadataException("Claim dialect cannot be empty");
        }

        // TODO : Validate oldClaimDialectURI is valid????

        // TODO : validate tenant domain?
        int tenantId = IdentityTenantUtil.getTenantId(tenantDomain);

        // Add listener

        this.claimDialectDAO.renameClaimDialect(oldClaimDialect, newClaimDialect, tenantId);

        // Add listener

    }

    @Override
    public void removeClaimDialect(ClaimDialect claimDialect, String tenantDomain) throws ClaimMetadataException {

        if (claimDialect == null || StringUtils.isBlank(claimDialect.getClaimDialectURI())) {
            throw new ClaimMetadataException("Claim dialect URI cannot be empty");
        }

        // TODO : validate claim dialect already exists?

        // TODO : validate tenant domain?
        int tenantId = IdentityTenantUtil.getTenantId(tenantDomain);

        // Add listener

        this.claimDialectDAO.removeClaimDialect(claimDialect, tenantId);

        // Add listener

    }

    @Override
    public List<LocalClaim> getLocalClaims(String tenantDomain) throws ClaimMetadataException {

        int tenantId = IdentityTenantUtil.getTenantId(tenantDomain);

        // Add listener

        List<LocalClaim> localClaims = this.localClaimDAO.getLocalClaims(tenantId);

        // Add listener


        return localClaims;
    }

    @Override
    public void addLocalClaim(LocalClaim localClaim, String tenantDomain) throws ClaimMetadataException {

        if (localClaim == null || StringUtils.isBlank(localClaim.getClaimURI())) {
            throw new ClaimMetadataException("Local claim URI cannot be empty");
        } else if (localClaim.getMappedAttributes().isEmpty()) {
            throw new ClaimMetadataException("Mapped attribute of the claim dialect URI : " + localClaim
                    .getClaimDialectURI() + " and Claim URI : " + localClaim.getClaimURI() + " cannot be empty");
        }

        // TODO : validate claim dialect already exists?

        // TODO : validate tenant domain?
        int tenantId = IdentityTenantUtil.getTenantId(tenantDomain);

        // Add listener

        this.localClaimDAO.addLocalClaim(localClaim, tenantId);

        // Add listener
    }

    @Override
    public void updateLocalClaim(LocalClaim localClaim, String tenantDomain) throws ClaimMetadataException {

        if (localClaim == null || StringUtils.isBlank(localClaim.getClaimURI())) {
            throw new ClaimMetadataException("Local claim URI cannot be empty");
        }

        // TODO : validate claim URI already exists?

        // TODO : validate tenant domain?
        int tenantId = IdentityTenantUtil.getTenantId(tenantDomain);

        // Add listener

        this.localClaimDAO.updateLocalClaim(localClaim, tenantId);

        // Add listener
    }

    @Override
    public void removeLocalClaim(String localClaimURI, String tenantDomain) throws ClaimMetadataException {

        if (StringUtils.isBlank(localClaimURI)) {
            throw new ClaimMetadataException("Local claim URI cannot be empty");
        }

        // TODO : validate claim URI already exists?

        // TODO : validate tenant domain?
        int tenantId = IdentityTenantUtil.getTenantId(tenantDomain);

        boolean isMappedLocalClaim = this.externalClaimDAO.isMappedLocalClaim(localClaimURI, tenantId);

        if (isMappedLocalClaim) {
            throw new ClaimMetadataException("Cannot remove local claim " + localClaimURI + " while having " +
                    "associations with external claims.");
        }

        // Add listener

        this.localClaimDAO.removeLocalClaim(localClaimURI, tenantId);

        // Add listener
    }

    @Override
    public List<ExternalClaim> getExternalClaims(String externalClaimDialectURI, String tenantDomain) throws
            ClaimMetadataException {

        if (StringUtils.isBlank(externalClaimDialectURI)) {
            throw new ClaimMetadataException("External claim dialect URI cannot be empty");
        }

        if (ClaimConstants.LOCAL_CLAIM_DIALECT_URI.equalsIgnoreCase(externalClaimDialectURI)) {
            throw new ClaimMetadataException("Invalid external claim dialect " + ClaimConstants
                    .LOCAL_CLAIM_DIALECT_URI);
        }

        int tenantId = IdentityTenantUtil.getTenantId(tenantDomain);

        // Add listener

        List<ExternalClaim> externalClaims = this.externalClaimDAO.getExternalClaims(externalClaimDialectURI, tenantId);

        // Add listener

        return externalClaims;
    }

    @Override
    public void addExternalClaim(ExternalClaim externalClaim, String tenantDomain) throws ClaimMetadataException {

        if (externalClaim == null || StringUtils.isBlank(externalClaim.getClaimURI())) {
            throw new ClaimMetadataException("External claim URI cannot be empty");
        }

        if (StringUtils.isBlank(externalClaim.getClaimDialectURI())) {
            throw new ClaimMetadataException("External dialect URI cannot be empty");
        }

        if (StringUtils.isBlank(externalClaim.getMappedLocalClaim())) {
            throw new ClaimMetadataException("Mapped local claim URI cannot be empty");
        }

        if (ClaimConstants.LOCAL_CLAIM_DIALECT_URI.equalsIgnoreCase(externalClaim.getClaimDialectURI())) {
            throw new ClaimMetadataException("Invalid external claim dialect " + ClaimConstants
                    .LOCAL_CLAIM_DIALECT_URI);
        }

        // TODO : validate claim URI already exists?

        // TODO : validate tenant domain?
        int tenantId = IdentityTenantUtil.getTenantId(tenantDomain);

        // Add listener

        this.externalClaimDAO.addExternalClaim(externalClaim, tenantId);

        // Add listener
    }

    @Override
    public void updateExternalClaim(ExternalClaim externalClaim, String tenantDomain) throws
            ClaimMetadataException {

        if (externalClaim == null || StringUtils.isBlank(externalClaim.getClaimURI())) {
            throw new ClaimMetadataException("External claim URI cannot be empty");
        }

        if (StringUtils.isBlank(externalClaim.getClaimDialectURI())) {
            throw new ClaimMetadataException("External dialect URI cannot be empty");
        }

        if (StringUtils.isBlank(externalClaim.getMappedLocalClaim())) {
            throw new ClaimMetadataException("Mapped local claim URI cannot be empty");
        }

        if (ClaimConstants.LOCAL_CLAIM_DIALECT_URI.equalsIgnoreCase(externalClaim.getClaimDialectURI())) {
            throw new ClaimMetadataException("Invalid external claim dialect " + ClaimConstants
                    .LOCAL_CLAIM_DIALECT_URI);
        }

        // TODO : validate claim URI already exists?

        // TODO : validate tenant domain?
        int tenantId = IdentityTenantUtil.getTenantId(tenantDomain);

        // Add listener

        this.externalClaimDAO.updateExternalClaim(externalClaim, tenantId);

        // Add listener
    }

    @Override
    public void removeExternalClaim(String externalClaimDialectURI, String externalClaimURI, String tenantDomain)
            throws ClaimMetadataException {

        if (StringUtils.isBlank(externalClaimDialectURI)) {
            throw new ClaimMetadataException("External claim dialect URI cannot be empty");
        }

        if (StringUtils.isBlank(externalClaimURI)) {
            throw new ClaimMetadataException("External claim URI cannot be empty");
        }

        if (ClaimConstants.LOCAL_CLAIM_DIALECT_URI.equalsIgnoreCase(externalClaimDialectURI)) {
            throw new ClaimMetadataException("Invalid external claim dialect " + ClaimConstants
                    .LOCAL_CLAIM_DIALECT_URI);
        }


        // TODO : validate claim URI already exists?

        // TODO : validate tenant domain?
        int tenantId = IdentityTenantUtil.getTenantId(tenantDomain);

        // Add listener

        this.externalClaimDAO.removeExternalClaim(externalClaimDialectURI, externalClaimURI, tenantId);

        // Add listener
    }
}
