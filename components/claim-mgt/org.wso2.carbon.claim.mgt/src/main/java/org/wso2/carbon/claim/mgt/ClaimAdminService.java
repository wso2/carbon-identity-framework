/*
 * Copyright (c) 2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.claim.mgt;

import org.wso2.carbon.claim.mgt.dto.ClaimAttributeDTO;
import org.wso2.carbon.claim.mgt.dto.ClaimDTO;
import org.wso2.carbon.claim.mgt.dto.ClaimDialectDTO;
import org.wso2.carbon.claim.mgt.dto.ClaimMappingDTO;
import org.wso2.carbon.user.api.Claim;
import org.wso2.carbon.user.api.ClaimMapping;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class ClaimAdminService {

    /**
     * @throws ClaimManagementException
     */
    public ClaimDialectDTO[] getClaimMappings() throws ClaimManagementException {

        ClaimMapping[] claimMappings = null;
        List<ClaimMapping> mappingList = null;
        Map<String, List<ClaimMapping>> claimMap = null;
        List<ClaimDialect> claims = null;
        ClaimDialect dto = null;

        claimMappings = ClaimManagerHandler.getInstance().getAllClaimMappings();

        if (claimMappings == null || claimMappings.length == 0) {
            return new ClaimDialectDTO[0];
        }

        claimMap = new HashMap<>();
        claims = new ArrayList<>();

        for (int i = 0; i < claimMappings.length; i++) {
            String dialectUri = claimMappings[i].getClaim().getDialectURI();

            if (!claimMap.containsKey(dialectUri)) {
                mappingList = new ArrayList<>();
                mappingList.add(claimMappings[i]);
                claimMap.put(dialectUri, mappingList);
            } else {
                mappingList = claimMap.get(dialectUri);
                mappingList.add(claimMappings[i]);
            }
        }

        for (Entry<String, List<ClaimMapping>> entry : claimMap.entrySet()) {
            mappingList = entry.getValue();
            dto = new ClaimDialect();
            ClaimMapping[] claimMappingArray = mappingList.toArray(new ClaimMapping[mappingList.size()]);
            // Sort the claims in the alphabetical order
            Arrays.sort(claimMappingArray, new Comparator<ClaimMapping>() {
                @Override
                public int compare(ClaimMapping o1, ClaimMapping o2) {
                    return o1.getClaim().getDescription().toLowerCase().compareTo(
                            o2.getClaim().getDescription().toLowerCase());
                }
            });
            dto.setClaimMapping(claimMappingArray);
            dto.setDialectUri(entry.getKey());
            claims.add(dto);
        }

        /*convert the array of ClaimDialects in complex structure to an array of ClaimDialectDTO in
         simple structure for the purpose of exposing as a data structure by the web service method.
         That will make it easy for POJO to generate client side code and wsdl with out complexity*/

        return convertClaimDialectArrayToClaimDialectDTOArray(
                claims.toArray(new ClaimDialect[claims.size()]));
    }

    /**
     * @param dialectUri
     * @return
     * @throws ClaimManagementException
     */
    public ClaimDialectDTO getClaimMappingByDialect(String dialectUri) throws ClaimManagementException {
        ClaimMapping[] claimMappings;
        ClaimDialect claimDialect;

        claimMappings = ClaimManagerHandler.getInstance().getAllSupportedClaimMappings(dialectUri);

        class ClaimComparator implements Comparator<ClaimMapping> {
            @Override
            public int compare(ClaimMapping o1, ClaimMapping o2) {
                return o1.getClaim().getDisplayTag().compareTo(o2.getClaim().getDisplayTag());
            }
        }
        // Sort the claims in the alphabetical order
        Arrays.sort(claimMappings, new Comparator<ClaimMapping>() {
            @Override
            public int compare(ClaimMapping o1, ClaimMapping o2) {
                return o1.getClaim().getDescription().toLowerCase().compareTo(
                        o2.getClaim().getDescription().toLowerCase());
            }
        });

        if (claimMappings.length == 0) {
            return null;
        }

        claimDialect = new ClaimDialect();
        claimDialect.setClaimMapping(claimMappings);
        claimDialect.setDialectUri(dialectUri);
        /*Convert the ClaimDialect type to ClaimDialectDTO type for simplicity of structure
        when exposing through the web service as a data structure.*/
        return convertClaimDialectToClaimDialectDTO(claimDialect);
    }

    /**
     * @param
     * @throws ClaimManagementException
     */
    public void upateClaimMapping(ClaimMappingDTO claimMappingDTO) throws ClaimManagementException {
        /*Convert the simple structure of ClaimMapping received, to the complex structure
        of ClaimMapping which is used in the back end. */
        ClaimMapping claimMapping = convertClaimMappingDTOToClaimMapping(claimMappingDTO);
        ClaimManagerHandler.getInstance().updateClaimMapping(claimMapping);
    }

    /**
     * @param
     * @throws ClaimManagementException
     */
    public void addNewClaimMapping(ClaimMappingDTO claimMappingDTO) throws ClaimManagementException {
        /*Convert the simple structure of ClaimMapping received, to the complex structure
        of ClaimMapping which is used in the back end. */
        ClaimMapping claimMapping = convertClaimMappingDTOToClaimMapping(claimMappingDTO);
        ClaimManagerHandler handler = ClaimManagerHandler.getInstance();
        ClaimMapping currentMapping = handler.getClaimMapping(
                claimMapping.getClaim().getClaimUri());
        if (currentMapping != null) {
            throw new ClaimManagementException(
                    "Duplicate claim exist in the system. Please pick a different Claim Uri");
        }
        handler.addNewClaimMapping(claimMapping);
    }

    /**
     * @param dialectUri
     * @param claimUri
     * @throws ClaimManagementException
     */
    public void removeClaimMapping(String dialectUri, String claimUri) throws ClaimManagementException {
        ClaimManagerHandler.getInstance().removeClaimMapping(dialectUri, claimUri);
    }

    /**
     * @param
     */
    public void addNewClaimDialect(ClaimDialectDTO claimDialectDTO) throws ClaimManagementException {
        /*Convert the simple structure of ClaimDialectDTO received, to the complex structure
        of ClaimDialect which is used in the back end. */
        ClaimDialect claimDialect = convertClaimDialectDTOToClaimDialect(claimDialectDTO);
        ClaimManagerHandler.getInstance().addNewClaimDialect(claimDialect);
    }

    /**
     * @param
     */
    public void removeClaimDialect(String dialectUri) throws ClaimManagementException {
        ClaimManagerHandler.getInstance().removeClaimDialect(dialectUri);
    }

    private ClaimDialectDTO[] convertClaimDialectArrayToClaimDialectDTOArray(
            ClaimDialect[] claimDialects) {
        List<ClaimDialectDTO> claimDialectDTOList = new ArrayList<>();
        for (ClaimDialect claimDialect : claimDialects) {
            ClaimDialectDTO claimDialectDTO = convertClaimDialectToClaimDialectDTO(claimDialect);
            claimDialectDTOList.add(claimDialectDTO);
        }
        return claimDialectDTOList.toArray(new ClaimDialectDTO[claimDialectDTOList.size()]);
    }

    private ClaimDialectDTO convertClaimDialectToClaimDialectDTO(ClaimDialect claimDialect) {
        ClaimDialectDTO claimDialectDTO = new ClaimDialectDTO();

        claimDialectDTO.setClaimMappings(convertClaimMappingArrayToClaimMappingDTOArray(
                claimDialect.getClaimMapping()));
        claimDialectDTO.setDialectURI(claimDialect.getDialectUri());
        claimDialectDTO.setUserStore(claimDialect.getUserStore());

        return claimDialectDTO;
    }

    private ClaimMappingDTO[] convertClaimMappingArrayToClaimMappingDTOArray(
            ClaimMapping[] claimMappings) {
        List<ClaimMappingDTO> claimMappingDTOList = new ArrayList<ClaimMappingDTO>();
        for (ClaimMapping claimMapping : claimMappings) {
            ClaimMappingDTO claimMappingDTO = convertClaimMappingToClaimMappingDTO(claimMapping);
            claimMappingDTOList.add(claimMappingDTO);
        }
        return claimMappingDTOList.toArray(new ClaimMappingDTO[claimMappingDTOList.size()]);
    }

    private ClaimMapping[] convertClaimMappingDTOArrayToClaimMappingArray(
            ClaimMappingDTO[] claimMappingDTOs) {
        List<ClaimMapping> claimMappings = new ArrayList<ClaimMapping>();
        for (ClaimMappingDTO claimMappingDTO : claimMappingDTOs) {
            ClaimMapping claimMapping = convertClaimMappingDTOToClaimMapping(claimMappingDTO);
            claimMappings.add(claimMapping);
        }
        return claimMappings.toArray(new ClaimMapping[claimMappings.size()]);

    }

    private ClaimMappingDTO convertClaimMappingToClaimMappingDTO(ClaimMapping claimMapping) {
        ClaimMappingDTO claimMappingDTO = new ClaimMappingDTO();
        claimMappingDTO.setClaim(convertClaimToClaimDTO(claimMapping.getClaim()));
        claimMappingDTO.setMappedAttribute(claimMapping.getMappedAttribute());

        Map<String, String> attributes = claimMapping.getMappedAttributes();

        if (attributes != null) {
            ClaimAttributeDTO[] attrDto = new ClaimAttributeDTO[attributes.size()];
            int i = 0;
            for (Map.Entry<String, String> entry : attributes.entrySet()) {
                ClaimAttributeDTO dto = new ClaimAttributeDTO();
                dto.setAttributeName(entry.getValue());
                dto.setDomainName(entry.getKey());
                attrDto[i++] = dto;
            }
            claimMappingDTO.setMappedAttributes(attrDto);
        }

        return claimMappingDTO;
    }

    private ClaimMapping convertClaimMappingDTOToClaimMapping(ClaimMappingDTO claimMappingDTO) {
        ClaimMapping claimMapping = new ClaimMapping(
                convertClaimDTOToClaim(claimMappingDTO.getClaim()),
                claimMappingDTO.getMappedAttribute());

        ClaimAttributeDTO[] attributes = claimMappingDTO.getMappedAttributes();

        if (attributes != null) {
            for (ClaimAttributeDTO attribute : attributes) {
                if (attribute.getDomainName() != null) {
                    claimMapping.setMappedAttribute(attribute.getDomainName(),
                                                    attribute.getAttributeName());
                }
            }
        }

        return claimMapping;
    }

    private ClaimDTO convertClaimToClaimDTO(Claim claim) {
        ClaimDTO claimDTO = new ClaimDTO();
        claimDTO.setClaimUri(claim.getClaimUri());
        claimDTO.setDescription(claim.getDescription());
        claimDTO.setDialectURI(claim.getDialectURI());
        claimDTO.setDisplayOrder(claim.getDisplayOrder());
        claimDTO.setDisplayTag(claim.getDisplayTag());
        claimDTO.setRegEx(claim.getRegEx());
        claimDTO.setRequired(claim.isRequired());
        claimDTO.setSupportedByDefault(claim.isSupportedByDefault());
        claimDTO.setValue(claim.getValue());
        claimDTO.setCheckedAttribute(claim.isCheckedAttribute());
        claimDTO.setReadOnly(claim.isReadOnly());
        return claimDTO;
    }

    private Claim convertClaimDTOToClaim(ClaimDTO claimDTO) {
        Claim claim = new Claim();
        claim.setSupportedByDefault(claimDTO.isSupportedByDefault());
        claim.setValue(claimDTO.getValue());
        claim.setClaimUri(claimDTO.getClaimUri());
        claim.setDescription(claimDTO.getDescription());
        claim.setDialectURI(claimDTO.getDialectURI());
        claim.setDisplayOrder(claimDTO.getDisplayOrder());
        claim.setDisplayTag(claimDTO.getDisplayTag());
        claim.setRegEx(claimDTO.getRegEx());
        claim.setRequired(claimDTO.isRequired());
        claim.setCheckedAttribute(claimDTO.isCheckedAttribute());
        claim.setReadOnly(claimDTO.isReadOnly());
        return claim;
    }

    private ClaimDialect convertClaimDialectDTOToClaimDialect(ClaimDialectDTO claimDialectDTO) {
        ClaimDialect claimDialect = new ClaimDialect();
        claimDialect.setClaimMapping(convertClaimMappingDTOArrayToClaimMappingArray(
                claimDialectDTO.getClaimMappings()));
        claimDialect.setDialectUri(claimDialectDTO.getDialectURI());
        claimDialect.setUserStore(claimDialectDTO.getUserStore());
        return claimDialect;
    }


}
