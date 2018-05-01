/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
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

package org.wso2.carbon.identity.claim.metadata.mgt.dao;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.claim.metadata.mgt.cache.ClaimDialectCache;
import org.wso2.carbon.identity.claim.metadata.mgt.exception.ClaimMetadataException;
import org.wso2.carbon.identity.claim.metadata.mgt.model.ClaimDialect;

import java.util.List;

/**
 * Caches the claim dialect.
 * Uses direct DB call when cache is disabled.
 */
public class CacheBackedClaimDialectDAO extends ClaimDialectDAO {

    private static Log log = LogFactory.getLog(CacheBackedClaimDialectDAO.class);

    private ClaimDialectCache claimDialectCache = ClaimDialectCache.getInstance();

    public List<ClaimDialect> getClaimDialects(int tenantId) throws ClaimMetadataException {

        List<ClaimDialect> claimDialectList = claimDialectCache.getClaimDialects(tenantId);
        if (claimDialectList != null && !claimDialectList.isEmpty()) {
            if (log.isDebugEnabled()) {
                log.debug("Cache hit for claim dialect list for tenant: " + tenantId + ". Claim dialect list size: "
                        + claimDialectList.size());
            }
            return claimDialectList;
        }

        claimDialectList = super.getClaimDialects(tenantId);
        claimDialectCache.putClaimDialects(tenantId, claimDialectList);

        if (log.isDebugEnabled()) {
            log.debug("Cache miss for claim dialect list for tenant: " + tenantId + ". Updated cache with claim " +
                    "dialect list retrieved from database. Claim dialect list size: " + claimDialectList.size());
        }
        return claimDialectList;
    }

    @Override
    public void renameClaimDialect(ClaimDialect oldClaimDialect, ClaimDialect newClaimDialect, int tenantId)
            throws ClaimMetadataException {

        super.renameClaimDialect(oldClaimDialect, newClaimDialect, tenantId);
        claimDialectCache.clearClaimDialects(tenantId);
        if (log.isDebugEnabled()) {
            log.debug("Claim dialect: " + oldClaimDialect.getClaimDialectURI() + " is renamed to new claim dialect: "
                    + newClaimDialect.getClaimDialectURI() + " for tenant: " + tenantId + ". Invalidated " +
                    "ClaimDialectCache." );
        }
    }

    @Override
    public void removeClaimDialect(ClaimDialect claimDialect, int tenantId) throws ClaimMetadataException {

        super.removeClaimDialect(claimDialect, tenantId);
        claimDialectCache.clearClaimDialects(tenantId);
        if (log.isDebugEnabled()) {
            log.debug("Claim dialect: " + claimDialect.getClaimDialectURI() + " is removed for tenant: " + tenantId +
                    ". Invalidated ClaimDialectCache.");
        }
    }

    @Override
    public void addClaimDialect(ClaimDialect claimDialect, int tenantId) throws ClaimMetadataException {

        super.addClaimDialect(claimDialect, tenantId);
        claimDialectCache.clearClaimDialects(tenantId);
        if (log.isDebugEnabled()) {
            log.debug("Claim dialect: " + claimDialect.getClaimDialectURI() + " is added for tenant: " + tenantId +
                    ". Invalidated ClaimDialectCache.");
        }
    }

}
