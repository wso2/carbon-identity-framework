/*
 *  Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.wso2.carbon.identity.entitlement.common.util;


import org.wso2.balana.utils.policy.dto.AttributeElementDTO;
import org.wso2.balana.utils.policy.dto.AttributesElementDTO;
import org.wso2.balana.utils.policy.dto.RequestElementDTO;
import org.wso2.carbon.identity.entitlement.common.EntitlementPolicyConstants;
import org.wso2.carbon.identity.entitlement.common.dto.RequestDTO;
import org.wso2.carbon.identity.entitlement.common.dto.RowDTO;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This is Util class which help to create a XACML policy
 */
public class PolicyCreatorUtil {
    /**
     * Creates XML request from  RequestDTO object
     *
     * @param requestDTO
     * @return
     */
    public static RequestElementDTO createRequestElementDTO(RequestDTO requestDTO) {

        RequestElementDTO requestElement = new RequestElementDTO();

        List<RowDTO> rowDTOs = requestDTO.getRowDTOs();
        if (rowDTOs == null || rowDTOs.size() < 1) {
            return requestElement;
        }

        Map<String, AttributesElementDTO> dtoMap = new HashMap<String, AttributesElementDTO>();
        List<AttributesElementDTO> dtoList = new ArrayList<AttributesElementDTO>();

        for (RowDTO rowDTO : rowDTOs) {
            String category = rowDTO.getCategory();
            String value = rowDTO.getAttributeValue();
            String attributeId = rowDTO.getAttributeId();
            if (category != null && category.trim().length() > 0 && value != null &&
                    value.trim().length() > 0 && attributeId != null && attributeId.trim().length() > 0) {

                if (requestDTO.isMultipleRequest()) {
                    String[] values = value.split(EntitlementPolicyConstants.ATTRIBUTE_SEPARATOR);
                    for (String attributeValue : values) {
                        AttributesElementDTO attributesElementDTO = new AttributesElementDTO();
                        attributesElementDTO.setCategory(category);

                        AttributeElementDTO attributeElementDTO = new AttributeElementDTO();
                        attributeElementDTO.addAttributeValue(attributeValue);
                        attributeElementDTO.setAttributeId(attributeId);
                        attributeElementDTO.setIncludeInResult(rowDTO.isNotCompleted());
                        attributesElementDTO.addAttributeElementDTO(attributeElementDTO);
                        if (rowDTO.getAttributeDataType() != null && rowDTO.
                                getAttributeDataType().trim().length() > 0) {
                            attributeElementDTO.setDataType(rowDTO.getAttributeDataType());
                        } else {
                            attributeElementDTO.setDataType(EntitlementPolicyConstants.STRING_DATA_TYPE);
                        }
                        dtoList.add(attributesElementDTO);
                    }

                } else {
                    AttributesElementDTO attributesElementDTO = dtoMap.get(category);
                    if (attributesElementDTO == null) {
                        attributesElementDTO = new AttributesElementDTO();
                        attributesElementDTO.setCategory(category);
                    }

                    String[] values = value.split(EntitlementPolicyConstants.ATTRIBUTE_SEPARATOR);
                    AttributeElementDTO attributeElementDTO = new AttributeElementDTO();
                    attributeElementDTO.setAttributeValues(Arrays.asList(values));
                    attributeElementDTO.setAttributeId(attributeId);
                    attributeElementDTO.setIncludeInResult(rowDTO.isNotCompleted());
                    attributesElementDTO.addAttributeElementDTO(attributeElementDTO);
                    if (rowDTO.getAttributeDataType() != null && rowDTO.
                            getAttributeDataType().trim().length() > 0) {
                        attributeElementDTO.setDataType(rowDTO.getAttributeDataType());
                    } else {
                        attributeElementDTO.setDataType(EntitlementPolicyConstants.STRING_DATA_TYPE);
                    }
                    dtoMap.put(category, attributesElementDTO);
                }
            }
        }

        requestElement.setMultipleRequest(requestDTO.isMultipleRequest());
        requestElement.setCombinedDecision(requestDTO.isCombinedDecision());
        requestElement.setReturnPolicyIdList(requestDTO.isReturnPolicyIdList());
        if (!requestDTO.isMultipleRequest()) {
            dtoList = new ArrayList<AttributesElementDTO>();
            for (Map.Entry<String, AttributesElementDTO> entry : dtoMap.entrySet()) {
                dtoList.add(entry.getValue());
            }
        }
        requestElement.setAttributesElementDTOs(dtoList);
        return requestElement;
    }
}