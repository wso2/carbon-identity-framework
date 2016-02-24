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
package org.wso2.carbon.claim.mgt.dto;

public class ClaimMappingDTO {
    private ClaimDTO claim;

    private String mappedAttribute;

    private ClaimAttributeDTO[] mappedAttributes;

    public ClaimDTO getClaim() {
        return claim;
    }

    public void setClaim(ClaimDTO claim) {
        this.claim = claim;
    }

    public String getMappedAttribute() {
        return mappedAttribute;
    }

    public void setMappedAttribute(String mappedAttribute) {
        this.mappedAttribute = mappedAttribute;
    }

    public ClaimAttributeDTO[] getMappedAttributes() {
        if (mappedAttributes != null) {
            return mappedAttributes.clone();
        } else {
            return new ClaimAttributeDTO[0];
        }
    }

    public void setMappedAttributes(ClaimAttributeDTO[] mappedAttributes) {
        if (mappedAttributes != null) {
            this.mappedAttributes = mappedAttributes.clone();
        }
    }

}
