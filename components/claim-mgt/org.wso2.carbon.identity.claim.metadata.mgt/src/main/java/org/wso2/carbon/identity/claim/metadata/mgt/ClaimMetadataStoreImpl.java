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
import org.wso2.carbon.user.api.Claim;
import org.wso2.carbon.user.api.ClaimMapping;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.core.claim.inmemory.ClaimConfig;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

/**
 * Default implementation of {@link org.wso2.carbon.identity.claim.metadata.mgt.ClaimMetadataStore} interface.
 */
public class ClaimMetadataStoreImpl implements ClaimMetadataStore {

    private static final Log log = LogFactory.getLog(ClaimMetadataStoreImpl.class);
    ClaimConfig claimConfig;

    ClaimMetadataStoreImpl(ClaimConfig claimConfig, int tenantId) {
        this.claimConfig = claimConfig;
    }

    @Override
    public String getAttributeName(String domainName, String claimURI) throws UserStoreException {

        // TODO : VERY IMPORTANT
        log.info(">>>>>>>>>>> getAttributeName(String, String) : ClaimURI : " + claimURI + ", DomainName : " +
                domainName);

        Map<String, org.wso2.carbon.user.core.claim.ClaimMapping> claimMappingMap = claimConfig.getClaims();

        if (claimMappingMap.containsKey(claimURI)) {
            org.wso2.carbon.user.core.claim.ClaimMapping claimMapping = claimMappingMap.get(claimURI);

            if (domainName != null && claimMapping.getMappedAttribute(domainName.toUpperCase()) != null) {
                return claimMapping.getMappedAttribute(domainName.toUpperCase());
            }

            return claimMapping.getMappedAttribute();
        }
        return null;
    }

    @Override
    public String getAttributeName(String claimURI) throws UserStoreException {

        // TODO : SHOULD Deprecate.
        log.info(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> getAttributeName(String) : " + claimURI);
        Thread.dumpStack();
        getAttributeName(null, claimURI);
        return null;
    }

    @Override
    public Claim getClaim(String claimURI) throws UserStoreException {

        // TODO : Less Important
        log.info(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> getClaim(String) : " + claimURI);
        return null;
    }

    @Override
    public ClaimMapping getClaimMapping(String claimURI) throws UserStoreException {

        // TODO : Can Deprecate.
        log.info("getClaimMapping(String) : " + claimURI);
        return null;
    }

    @Override
    public ClaimMapping[] getAllClaimMappings() throws UserStoreException {
        return new ClaimMapping[0];
    }

    @Override
    public String[] getAllClaimUris() throws UserStoreException {

        // TODO : VERY IMPORTANT
        log.info(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> getAllClaimUris()");
        Map<String, org.wso2.carbon.user.core.claim.ClaimMapping> claimMappingMap = claimConfig.getClaims();

        Set<String> claimURISet = claimMappingMap.keySet();
        return claimURISet.toArray(new String[claimURISet.size()]);
    }

    @Override
    public void addNewClaimMapping(ClaimMapping claimMapping) throws UserStoreException {
        throw new UnsupportedOperationException("ClaimMetadataStoreImpl does not supports management operations");
    }

    @Override
    public void deleteClaimMapping(ClaimMapping claimMapping) throws UserStoreException {
        throw new UnsupportedOperationException("ClaimMetadataStoreImpl does not supports management operations");
    }

    @Override
    public void updateClaimMapping(ClaimMapping claimMapping) throws UserStoreException {
        throw new UnsupportedOperationException("ClaimMetadataStoreImpl does not supports management operations");
    }

    @Override
    public ClaimMapping[] getAllSupportClaimMappingsByDefault() throws UserStoreException {
        // Used in profile metadata???
        log.info(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> getAllSupportClaimMappingsByDefault()");
        Map<String, org.wso2.carbon.user.core.claim.ClaimMapping> claimMappingMap = claimConfig.getClaims();

        Collection<org.wso2.carbon.user.core.claim.ClaimMapping> claimMappingSet = claimMappingMap.values();
        return claimMappingSet.toArray(new org.wso2.carbon.user.core.claim.ClaimMapping[claimMappingSet.size()]);
//        throw new UnsupportedOperationException("ClaimMetadataStoreImpl does not supports management operations");
    }

    @Override
    public ClaimMapping[] getAllRequiredClaimMappings() throws UserStoreException {
        throw new UnsupportedOperationException("ClaimMetadataStoreImpl does not supports management operations");
    }

    @Override
    public ClaimMapping[] getAllClaimMappings(String dialectUri) throws UserStoreException {
        throw new UnsupportedOperationException("ClaimMetadataStoreImpl does not supports management operations");
    }
}
