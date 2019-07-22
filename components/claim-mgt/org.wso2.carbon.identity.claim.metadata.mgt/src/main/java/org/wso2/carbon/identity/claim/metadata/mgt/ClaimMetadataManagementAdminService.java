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
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.identity.claim.metadata.mgt.dto.ClaimDialectDTO;
import org.wso2.carbon.identity.claim.metadata.mgt.dto.ExternalClaimDTO;
import org.wso2.carbon.identity.claim.metadata.mgt.dto.LocalClaimDTO;
import org.wso2.carbon.identity.claim.metadata.mgt.exception.ClaimMetadataException;
import org.wso2.carbon.identity.claim.metadata.mgt.internal.IdentityClaimManagementServiceDataHolder;
import org.wso2.carbon.identity.claim.metadata.mgt.model.ClaimDialect;
import org.wso2.carbon.identity.claim.metadata.mgt.model.ExternalClaim;
import org.wso2.carbon.identity.claim.metadata.mgt.model.LocalClaim;
import org.wso2.carbon.identity.claim.metadata.mgt.util.ClaimMetadataUtils;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

/**
 * WS API for Claim Metadata Management.
 *
 * Handle management of Claim Dialects, Local Claims and External Claims entities.
 */
public class ClaimMetadataManagementAdminService {

    private static final Log log = LogFactory.getLog(ClaimMetadataManagementAdminService.class);

    // TODO : Return string array??
    @SuppressWarnings("unused")
    public ClaimDialectDTO[] getClaimDialects() throws ClaimMetadataException {

        try {
            String tenantDomain = CarbonContext.getThreadLocalCarbonContext().getTenantDomain();

            List<ClaimDialect> claimDialectList = IdentityClaimManagementServiceDataHolder.getInstance()
                    .getClaimManagementService().getClaimDialects(tenantDomain);

            ClaimDialect[] claimDialects = claimDialectList.toArray(new ClaimDialect[0]);

            ClaimDialectDTO[] claimDialectDTOS = ClaimMetadataUtils.convertClaimDialectsToClaimDialectDTOs(claimDialects);

            // Sort the claim dialects in the alphabetical order
            Arrays.sort(claimDialectDTOS, new Comparator<ClaimDialectDTO>() {
                @Override
                public int compare(ClaimDialectDTO o1, ClaimDialectDTO o2) {
                    return o1.getClaimDialectURI().toLowerCase().compareTo(
                            o2.getClaimDialectURI().toLowerCase());
                }
            });

            return claimDialectDTOS;
        } catch (Throwable e) {
            log.error(e.getMessage(), e);
            throw new ClaimMetadataException(e.getMessage(), e);
        }
    }

    @SuppressWarnings("unused")
    public void addClaimDialect(ClaimDialectDTO claimDialect) throws ClaimMetadataException {

        try {
            ClaimDialect dialect = ClaimMetadataUtils.convertClaimDialectDTOToClaimDialect(claimDialect);
            String tenantDomain = CarbonContext.getThreadLocalCarbonContext().getTenantDomain();

            IdentityClaimManagementServiceDataHolder.getInstance().getClaimManagementService()
                    .addClaimDialect(dialect, tenantDomain);
        } catch (Throwable e) {
            log.error(e.getMessage(), e);
            throw new ClaimMetadataException(e.getMessage(), e);
        }
    }

    @SuppressWarnings("unused")
    public void renameClaimDialect(ClaimDialectDTO oldClaimDialect, ClaimDialectDTO newClaimDialect) throws
            ClaimMetadataException {

        try {
            ClaimDialect oldDialect = ClaimMetadataUtils.convertClaimDialectDTOToClaimDialect(oldClaimDialect);
            ClaimDialect newDialect = ClaimMetadataUtils.convertClaimDialectDTOToClaimDialect(newClaimDialect);
            String tenantDomain = CarbonContext.getThreadLocalCarbonContext().getTenantDomain();

            IdentityClaimManagementServiceDataHolder.getInstance().getClaimManagementService()
                    .renameClaimDialect(oldDialect, newDialect, tenantDomain);
        } catch (Throwable e) {
            log.error(e.getMessage(), e);
            throw new ClaimMetadataException(e.getMessage(), e);
        }
    }

    @SuppressWarnings("unused")
    public void removeClaimDialect(ClaimDialectDTO claimDialect) throws
            ClaimMetadataException {

        try {
            ClaimDialect dialect = ClaimMetadataUtils.convertClaimDialectDTOToClaimDialect(claimDialect);
            String tenantDomain = CarbonContext.getThreadLocalCarbonContext().getTenantDomain();

            IdentityClaimManagementServiceDataHolder.getInstance().getClaimManagementService()
                    .removeClaimDialect(dialect, tenantDomain);
        } catch (Throwable e) {
            log.error(e.getMessage(), e);
            throw new ClaimMetadataException(e.getMessage(), e);
        }
    }


    @SuppressWarnings("unused")
    public LocalClaimDTO[] getLocalClaims() throws ClaimMetadataException {

        try {
            String tenantDomain = CarbonContext.getThreadLocalCarbonContext().getTenantDomain();

            List<LocalClaim> localClaimList = IdentityClaimManagementServiceDataHolder.getInstance()
                    .getClaimManagementService().getLocalClaims(tenantDomain);


            LocalClaim[] localClaims = localClaimList.toArray(new LocalClaim[0]);

            LocalClaimDTO[] localClaimDTOS = ClaimMetadataUtils.convertLocalClaimsToLocalClaimDTOs(localClaims);

            // Sort the local claims in the alphabetical order
            Arrays.sort(localClaimDTOS, new Comparator<LocalClaimDTO>() {
                @Override
                public int compare(LocalClaimDTO o1, LocalClaimDTO o2) {
                    return o1.getLocalClaimURI().toLowerCase().compareTo(
                            o2.getLocalClaimURI().toLowerCase());
                }
            });

            return localClaimDTOS;
        } catch (Throwable e) {
            log.error(e.getMessage(), e);
            throw new ClaimMetadataException(e.getMessage(), e);
        }
    }

    @SuppressWarnings("unused")
    public void addLocalClaim(LocalClaimDTO localClaim) throws ClaimMetadataException {

        try {
            LocalClaim claim = ClaimMetadataUtils.convertLocalClaimDTOToLocalClaim(localClaim);

            String tenantDomain = CarbonContext.getThreadLocalCarbonContext().getTenantDomain();

            IdentityClaimManagementServiceDataHolder.getInstance().getClaimManagementService()
                    .addLocalClaim(claim, tenantDomain);
        } catch (Throwable e) {
            log.error(e.getMessage(), e);
            throw new ClaimMetadataException(e.getMessage(), e);
        }
    }

    @SuppressWarnings("unused")
    public void updateLocalClaim(LocalClaimDTO localClaim) throws ClaimMetadataException {

        try {
            LocalClaim claim = ClaimMetadataUtils.convertLocalClaimDTOToLocalClaim(localClaim);

            String tenantDomain = CarbonContext.getThreadLocalCarbonContext().getTenantDomain();

            IdentityClaimManagementServiceDataHolder.getInstance().getClaimManagementService()
                    .updateLocalClaim(claim, tenantDomain);
        } catch (Throwable e) {
            log.error(e.getMessage(), e);
            throw new ClaimMetadataException(e.getMessage(), e);
        }
    }

    @SuppressWarnings("unused")
    public void removeLocalClaim(String localClaimURI) throws ClaimMetadataException {

        try {
            String tenantDomain = CarbonContext.getThreadLocalCarbonContext().getTenantDomain();

            IdentityClaimManagementServiceDataHolder.getInstance().getClaimManagementService()
                    .removeLocalClaim(localClaimURI, tenantDomain);
        } catch (Throwable e) {
            log.error(e.getMessage(), e);
            throw new ClaimMetadataException(e.getMessage(), e);
        }
    }


    @SuppressWarnings("unused")
    public ExternalClaimDTO[] getExternalClaims(String externalClaimDialectURI) throws ClaimMetadataException {

        try {
            String tenantDomain = CarbonContext.getThreadLocalCarbonContext().getTenantDomain();

            List<ExternalClaim> externalClaimList = IdentityClaimManagementServiceDataHolder.getInstance()
                    .getClaimManagementService().getExternalClaims(externalClaimDialectURI, tenantDomain);


            ExternalClaim[] externalClaims = externalClaimList.toArray(new ExternalClaim[0]);

            ExternalClaimDTO[] externalClaimDTOS = ClaimMetadataUtils.convertExternalClaimsToExternalClaimDTOs(externalClaims);

            // Sort the external claims in the alphabetical order
            Arrays.sort(externalClaimDTOS, new Comparator<ExternalClaimDTO>() {
                @Override
                public int compare(ExternalClaimDTO o1, ExternalClaimDTO o2) {
                    return o1.getExternalClaimURI().toLowerCase().compareTo(
                            o2.getExternalClaimURI().toLowerCase());
                }
            });

            return externalClaimDTOS;
        } catch (Throwable e) {
            log.error(e.getMessage(), e);
            throw new ClaimMetadataException(e.getMessage(), e);
        }
    }

    @SuppressWarnings("unused")
    public void addExternalClaim(ExternalClaimDTO externalClaim) throws ClaimMetadataException {

        try {
            ExternalClaim claim = ClaimMetadataUtils.convertExternalClaimDTOToExternalClaim(externalClaim);

            String tenantDomain = CarbonContext.getThreadLocalCarbonContext().getTenantDomain();

            IdentityClaimManagementServiceDataHolder.getInstance().getClaimManagementService()
                    .addExternalClaim(claim, tenantDomain);
        } catch (Throwable e) {
            log.error(e.getMessage(), e);
            throw new ClaimMetadataException(e.getMessage(), e);
        }
    }

    @SuppressWarnings("unused")
    public void updateExternalClaim(ExternalClaimDTO externalClaim) throws ClaimMetadataException {

        try {
            ExternalClaim claim = ClaimMetadataUtils.convertExternalClaimDTOToExternalClaim(externalClaim);

            String tenantDomain = CarbonContext.getThreadLocalCarbonContext().getTenantDomain();

            IdentityClaimManagementServiceDataHolder.getInstance().getClaimManagementService()
                    .updateExternalClaim(claim, tenantDomain);
        } catch (Throwable e) {
            log.error(e.getMessage(), e);
            throw new ClaimMetadataException(e.getMessage(), e);
        }
    }

    @SuppressWarnings("unused")
    public void removeExternalClaim(String externalClaimDialectURI, String externalClaimURI) throws
            ClaimMetadataException {

        try {
            String tenantDomain = CarbonContext.getThreadLocalCarbonContext().getTenantDomain();

            IdentityClaimManagementServiceDataHolder.getInstance().getClaimManagementService()
                    .removeExternalClaim(externalClaimDialectURI, externalClaimURI, tenantDomain);
        } catch (Throwable e) {
            log.error(e.getMessage(), e);
            throw new ClaimMetadataException(e.getMessage(), e);
        }
    }
}
