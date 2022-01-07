/*
 * Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org).
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

package org.wso2.carbon.identity.claim.metadata.mgt.internal.impl;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.claim.metadata.mgt.dao.CacheBackedClaimDialectDAO;
import org.wso2.carbon.identity.claim.metadata.mgt.dao.ClaimConfigInitDAO;
import org.wso2.carbon.identity.claim.metadata.mgt.dao.ClaimDialectDAO;
import org.wso2.carbon.identity.claim.metadata.mgt.dao.ExternalClaimDAO;
import org.wso2.carbon.identity.claim.metadata.mgt.dao.LocalClaimDAO;
import org.wso2.carbon.identity.claim.metadata.mgt.exception.ClaimMetadataException;
import org.wso2.carbon.identity.claim.metadata.mgt.model.AttributeMapping;
import org.wso2.carbon.identity.claim.metadata.mgt.model.ClaimDialect;
import org.wso2.carbon.identity.claim.metadata.mgt.model.ExternalClaim;
import org.wso2.carbon.identity.claim.metadata.mgt.model.LocalClaim;
import org.wso2.carbon.identity.claim.metadata.mgt.util.ClaimConstants;
import org.wso2.carbon.identity.core.util.IdentityUtil;
import org.wso2.carbon.user.core.claim.ClaimKey;
import org.wso2.carbon.user.core.claim.ClaimMapping;
import org.wso2.carbon.user.core.claim.inmemory.ClaimConfig;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Default implementation to use the existing DAO methods to create the claim configuration of tenant.
 */
public class DefaultClaimConfigInitDAO implements ClaimConfigInitDAO {

    private static final Log log = LogFactory.getLog(DefaultClaimConfigInitDAO.class);
    private final ClaimDialectDAO claimDialectDAO = new CacheBackedClaimDialectDAO();

    @Override
    public void initClaimConfig(ClaimConfig claimConfig, int tenantId) {

        // Adding local claim dialect.
        try {
            claimDialectDAO.addClaimDialect(new ClaimDialect(ClaimConstants.LOCAL_CLAIM_DIALECT_URI), tenantId);
        } catch (ClaimMetadataException e) {
            log.error("Error while adding claim dialect " + ClaimConstants.LOCAL_CLAIM_DIALECT_URI, e);
        }

        if (claimConfig.getClaimMap() != null) {

            // Get the primary domain name.
            String primaryDomainName = IdentityUtil.getPrimaryDomainName();

            // Adding external dialects and claims.
            Set<String> claimDialectList = new HashSet<>();

            for (Map.Entry<ClaimKey, ClaimMapping> entry : claimConfig.getClaimMap()
                    .entrySet()) {

                ClaimKey claimKey = entry.getKey();
                ClaimMapping claimMapping = entry.getValue();
                String claimDialectURI = claimMapping.getClaim().getDialectURI();
                String claimURI = claimKey.getClaimUri();

                if (ClaimConstants.LOCAL_CLAIM_DIALECT_URI.equalsIgnoreCase(claimDialectURI)) {

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

                    LocalClaim localClaim = new LocalClaim(claimURI, mappedAttributes,
                            fillClaimProperties(claimConfig, claimKey));

                    try {
                        // As this is at the initial server startup or tenant creation time, no need go through the
                        // caching layer. Going through the caching layer add overhead for bulk claim add.
                        LocalClaimDAO localClaimDAO = new LocalClaimDAO();
                        localClaimDAO.addLocalClaim(localClaim, tenantId);
                    } catch (ClaimMetadataException e) {
                        log.error("Error while adding local claim " + claimURI, e);
                    }

                } else {
                    claimDialectList.add(claimDialectURI);
                }
            }

            // Add external claim dialects.
            for (String claimDialectURI : claimDialectList) {

                ClaimDialect claimDialect = new ClaimDialect(claimDialectURI);
                try {
                    claimDialectDAO.addClaimDialect(claimDialect, tenantId);
                } catch (ClaimMetadataException e) {
                    log.error("Error while adding claim dialect " + claimDialectURI, e);
                }
            }

            for (Map.Entry<ClaimKey, ClaimMapping> entry : claimConfig.getClaimMap()
                    .entrySet()) {

                ClaimKey claimKey = entry.getKey();
                String claimURI = claimKey.getClaimUri();

                String claimDialectURI = entry.getValue().getClaim().getDialectURI();

                if (!ClaimConstants.LOCAL_CLAIM_DIALECT_URI.equalsIgnoreCase(claimDialectURI)) {

                    String mappedLocalClaimURI = claimConfig.getPropertyHolderMap().get(claimKey).get(ClaimConstants
                            .MAPPED_LOCAL_CLAIM_PROPERTY);
                    ExternalClaim externalClaim = new ExternalClaim(claimDialectURI, claimURI, mappedLocalClaimURI,
                            fillClaimProperties(claimConfig, claimKey));

                    try {
                        // As this is at the initial server startup or tenant creation time, no need go through the
                        // caching layer. Going through the caching layer add overhead for bulk claim add.
                        ExternalClaimDAO externalClaimDAO = new ExternalClaimDAO();
                        externalClaimDAO.addExternalClaim(externalClaim, tenantId);
                    } catch (ClaimMetadataException e) {
                        log.error("Error while adding external claim " + claimURI + " to dialect " +
                                        claimDialectURI, e);
                    }
                }
            }
        }
    }

    private Map<String, String> fillClaimProperties(ClaimConfig claimConfig, ClaimKey claimKey) {

        Map<String, String> claimProperties = claimConfig.getPropertyHolderMap().get(claimKey);
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
