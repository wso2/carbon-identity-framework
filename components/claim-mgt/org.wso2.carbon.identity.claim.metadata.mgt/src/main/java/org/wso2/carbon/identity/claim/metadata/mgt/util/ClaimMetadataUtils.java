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

package org.wso2.carbon.identity.claim.metadata.mgt.util;

import org.wso2.carbon.identity.claim.metadata.mgt.dto.AttributeMappingDTO;
import org.wso2.carbon.identity.claim.metadata.mgt.dto.ClaimDialectDTO;
import org.wso2.carbon.identity.claim.metadata.mgt.dto.ClaimPropertyDTO;
import org.wso2.carbon.identity.claim.metadata.mgt.dto.ExternalClaimDTO;
import org.wso2.carbon.identity.claim.metadata.mgt.dto.LocalClaimDTO;
import org.wso2.carbon.identity.claim.metadata.mgt.internal.IdentityClaimManagementServiceDataHolder;
import org.wso2.carbon.identity.claim.metadata.mgt.model.AttributeMapping;
import org.wso2.carbon.identity.claim.metadata.mgt.model.ClaimDialect;
import org.wso2.carbon.identity.claim.metadata.mgt.model.ExternalClaim;
import org.wso2.carbon.identity.claim.metadata.mgt.model.LocalClaim;
import org.wso2.carbon.user.api.UserRealm;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.core.UserCoreConstants;
import org.wso2.carbon.user.core.claim.Claim;
import org.wso2.carbon.user.core.claim.ClaimMapping;
import org.wso2.carbon.user.core.service.RealmService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Utility class containing various claim metadata implementation related functionality.
 */
public class ClaimMetadataUtils {

    private ClaimMetadataUtils() {
    }

    public static ClaimDialectDTO convertClaimDialectToClaimDialectDTO(ClaimDialect claimDialect) {

        ClaimDialectDTO claimDialectDTO = new ClaimDialectDTO();
        claimDialectDTO.setClaimDialectURI(claimDialect.getClaimDialectURI());
        return claimDialectDTO;
    }

    public static ClaimDialectDTO[] convertClaimDialectsToClaimDialectDTOs(ClaimDialect[] claimDialects) {

        ClaimDialectDTO[] claimDialectDTOs = new ClaimDialectDTO[claimDialects.length];

        for (int i = 0; i < claimDialects.length; i++) {
            claimDialectDTOs[i] = convertClaimDialectToClaimDialectDTO(claimDialects[i]);
        }

        return claimDialectDTOs;
    }

    public static ClaimDialect convertClaimDialectDTOToClaimDialect(ClaimDialectDTO claimDialectDTO) {

        ClaimDialect claimDialect = new ClaimDialect(claimDialectDTO.getClaimDialectURI());
        return claimDialect;
    }


    public static LocalClaimDTO convertLocalClaimToLocalClaimDTO(LocalClaim localClaim) {

        LocalClaimDTO localClaimDTO = new LocalClaimDTO();
        localClaimDTO.setLocalClaimURI(localClaim.getClaimURI());

        // Convert List<AttributeMapping> to AttributeMappingDTO[]
        List<AttributeMapping> attributeMappings = localClaim.getMappedAttributes();
        AttributeMappingDTO[] attributeMappingDTOs = new AttributeMappingDTO[attributeMappings.size()];

        int i = 0;
        for (AttributeMapping attributeMapping : attributeMappings) {
            AttributeMappingDTO attributeMappingDTO = new AttributeMappingDTO();
            attributeMappingDTO.setUserStoreDomain(attributeMapping.getUserStoreDomain());
            attributeMappingDTO.setAttributeName(attributeMapping.getAttributeName());
            attributeMappingDTOs[i] = attributeMappingDTO;
            i++;
        }

        localClaimDTO.setAttributeMappings(attributeMappingDTOs);

        // Convert Map<String, String> to ClaimPropertyDTO[]
        Map<String, String> claimProperties = localClaim.getClaimProperties();
        ClaimPropertyDTO[] claimPropertyDTOs = new ClaimPropertyDTO[claimProperties.size()];

        int j = 0;
        for (Map.Entry<String, String> claimPropertyEntry : claimProperties.entrySet()) {
            ClaimPropertyDTO claimProperty = new ClaimPropertyDTO();
            claimProperty.setPropertyName(claimPropertyEntry.getKey());
            claimProperty.setPropertyValue(claimPropertyEntry.getValue());
            claimPropertyDTOs[j] = claimProperty;
            j++;
        }

        localClaimDTO.setClaimProperties(claimPropertyDTOs);

        return localClaimDTO;
    }

    public static LocalClaimDTO[] convertLocalClaimsToLocalClaimDTOs(LocalClaim[] localClaims) {

        LocalClaimDTO[] localClaimDTOs = new LocalClaimDTO[localClaims.length];

        for (int i = 0; i < localClaims.length; i++) {
            localClaimDTOs[i] = convertLocalClaimToLocalClaimDTO(localClaims[i]);
        }

        return localClaimDTOs;
    }

    public static LocalClaim convertLocalClaimDTOToLocalClaim(LocalClaimDTO localClaimDTO) {

        // TODO : Validate if localClaimDTO null???
        LocalClaim localClaim = new LocalClaim(localClaimDTO.getLocalClaimURI());

        // Convert AttributeMappingDTO[] to List<AttributeMapping>
        if (localClaimDTO.getAttributeMappings() != null) {

            List<AttributeMapping> attributeMappings = new ArrayList<>();

            for (AttributeMappingDTO attributeMappingDTO : localClaimDTO.getAttributeMappings()) {
                attributeMappings.add(new AttributeMapping(attributeMappingDTO.getUserStoreDomain(),
                        attributeMappingDTO.getAttributeName()));
            }

            localClaim.setMappedAttributes(attributeMappings);
        }

        // Convert ClaimPropertyDTO[] to Map<String, String>
        if (localClaimDTO.getClaimProperties() != null) {

            Map<String, String> claimProperties = new HashMap<>();

            for (ClaimPropertyDTO claimPropertyDTO : localClaimDTO.getClaimProperties()) {
                claimProperties.put(claimPropertyDTO.getPropertyName(), claimPropertyDTO.getPropertyValue());
            }

            localClaim.setClaimProperties(claimProperties);
        }

        return localClaim;
    }


    public static ExternalClaimDTO convertExternalClaimToExternalClaimDTO(ExternalClaim externalClaim) {

        ExternalClaimDTO externalClaimDTO = new ExternalClaimDTO();
        externalClaimDTO.setExternalClaimDialectURI(externalClaim.getClaimDialectURI());
        externalClaimDTO.setExternalClaimURI(externalClaim.getClaimURI());
        externalClaimDTO.setMappedLocalClaimURI(externalClaim.getMappedLocalClaim());

        // Convert Map<String, String> to ClaimPropertyDTO[]
        Map<String, String> claimProperties = externalClaim.getClaimProperties();
        ClaimPropertyDTO[] claimPropertyDTOs = new ClaimPropertyDTO[claimProperties.size()];

        int j = 0;
        for (Map.Entry<String, String> claimPropertyEntry : claimProperties.entrySet()) {
            ClaimPropertyDTO claimProperty = new ClaimPropertyDTO();
            claimProperty.setPropertyName(claimPropertyEntry.getKey());
            claimProperty.setPropertyValue(claimPropertyEntry.getValue());
            claimPropertyDTOs[j] = claimProperty;
            j++;
        }
        externalClaimDTO.setClaimProperties(claimPropertyDTOs);
        return externalClaimDTO;
    }

    public static ExternalClaimDTO[] convertExternalClaimsToExternalClaimDTOs(ExternalClaim[] externalClaims) {

        ExternalClaimDTO[] externalClaimDTOs = new ExternalClaimDTO[externalClaims.length];

        for (int i = 0; i < externalClaims.length; i++) {
            externalClaimDTOs[i] = convertExternalClaimToExternalClaimDTO(externalClaims[i]);
        }

        return externalClaimDTOs;
    }

    public static ExternalClaim convertExternalClaimDTOToExternalClaim(ExternalClaimDTO externalClaimDTO) {

        // TODO : Validate if externalClaimDTO null???
        ExternalClaim externalClaim = new ExternalClaim(externalClaimDTO.getExternalClaimDialectURI(), externalClaimDTO
                .getExternalClaimURI(), externalClaimDTO.getMappedLocalClaimURI());

        // Convert ClaimPropertyDTO[] to Map<String, String>
        if (externalClaimDTO.getClaimProperties() != null) {

            Map<String, String> claimProperties = new HashMap<>();

            for (ClaimPropertyDTO claimPropertyDTO : externalClaimDTO.getClaimProperties()) {
                claimProperties.put(claimPropertyDTO.getPropertyName(), claimPropertyDTO.getPropertyValue());
            }

            externalClaim.setClaimProperties(claimProperties);
        }
        return externalClaim;
    }

    public static ClaimMapping convertLocalClaimToClaimMapping(LocalClaim localClaim, int tenantId) throws
            UserStoreException {

        ClaimMapping claimMapping = new ClaimMapping();

        Claim claim = new Claim();
        claim.setClaimUri(localClaim.getClaimURI());
        claim.setDialectURI(localClaim.getClaimDialectURI());

        Map<String, String> claimProperties = localClaim.getClaimProperties();

        if (claimProperties.containsKey(ClaimConstants.DISPLAY_NAME_PROPERTY)) {
            claim.setDisplayTag(claimProperties.get(ClaimConstants.DISPLAY_NAME_PROPERTY));
        }

        if (claimProperties.containsKey(ClaimConstants.DESCRIPTION_PROPERTY)) {
            claim.setDescription(claimProperties.get(ClaimConstants.DESCRIPTION_PROPERTY));
        }

        if (claimProperties.containsKey(ClaimConstants.REGULAR_EXPRESSION_PROPERTY)) {
            claim.setRegEx(claimProperties.get(ClaimConstants.REGULAR_EXPRESSION_PROPERTY));
        }

        if (claimProperties.containsKey(ClaimConstants.DISPLAY_ORDER_PROPERTY)) {
            claim.setDisplayOrder(Integer.parseInt(claimProperties.get(ClaimConstants.DISPLAY_ORDER_PROPERTY)));
        }

        if (claimProperties.containsKey(ClaimConstants.SUPPORTED_BY_DEFAULT_PROPERTY)) {
            if ("false".equalsIgnoreCase(claimProperties.get(ClaimConstants.SUPPORTED_BY_DEFAULT_PROPERTY))) {
                claim.setSupportedByDefault(Boolean.FALSE);

            } else {
                claim.setSupportedByDefault(Boolean.TRUE);
            }
        }

        if (claimProperties.containsKey(ClaimConstants.REQUIRED_PROPERTY)) {
            if ("false".equalsIgnoreCase(claimProperties.get(ClaimConstants.REQUIRED_PROPERTY))) {
                claim.setRequired(Boolean.FALSE);

            } else {
                claim.setRequired(Boolean.TRUE);
            }
        }

        if (claimProperties.containsKey(ClaimConstants.READ_ONLY_PROPERTY)) {
            if ("false".equalsIgnoreCase(claimProperties.get(ClaimConstants.READ_ONLY_PROPERTY))) {
                claim.setReadOnly(Boolean.FALSE);

            } else {
                claim.setReadOnly(Boolean.TRUE);
            }
        }

        claimMapping.setClaim(claim);

        List<AttributeMapping> mappedAttributes = localClaim.getMappedAttributes();
        for (AttributeMapping attributeMapping : mappedAttributes) {
            claimMapping.setMappedAttribute(attributeMapping.getUserStoreDomain(), attributeMapping.getAttributeName());
        }

        if (claimProperties.containsKey(ClaimConstants.DEFAULT_ATTRIBUTE)) {
            claimMapping.setMappedAttribute(claimProperties.get(ClaimConstants.DEFAULT_ATTRIBUTE));
        } else {
            RealmService realmService = IdentityClaimManagementServiceDataHolder.getInstance().getRealmService();

            if (realmService != null && realmService.getTenantUserRealm(tenantId) != null) {

                UserRealm realm = realmService.getTenantUserRealm(tenantId);
                String primaryDomainName = realm.getRealmConfiguration().getUserStoreProperty
                        (UserCoreConstants.RealmConfig.PROPERTY_DOMAIN_NAME);
                claimMapping.setMappedAttribute(localClaim.getMappedAttribute(primaryDomainName));
            } else {
                claimMapping.setMappedAttribute(localClaim.getMappedAttribute(UserCoreConstants.
                        PRIMARY_DEFAULT_DOMAIN_NAME));
            }
        }

        return claimMapping;
    }

    public static ClaimMapping convertExternalClaimToClaimMapping(ExternalClaim externalClaim, List<LocalClaim>
            localClaims, int tenantId) throws UserStoreException {

        ClaimMapping claimMapping = new ClaimMapping();

        if (localClaims != null) {
            for (LocalClaim localClaim : localClaims) {
                if (externalClaim.getMappedLocalClaim().equalsIgnoreCase(localClaim.getClaimURI())) {
                    claimMapping = convertLocalClaimToClaimMapping(localClaim, tenantId);
                    break;
                }
            }
        }

        if (claimMapping == null) {
            claimMapping = new ClaimMapping();
        }

        if (claimMapping.getClaim() == null) {
            Claim claim = new Claim();
            claimMapping.setClaim(claim);
        }

        claimMapping.getClaim().setDialectURI(externalClaim.getClaimDialectURI());
        claimMapping.getClaim().setClaimUri(externalClaim.getClaimURI());
        return claimMapping;
    }
}
