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

package org.wso2.carbon.identity.claim.metadata.mgt.model;

import org.wso2.carbon.identity.claim.metadata.mgt.util.ClaimConstants;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Represents the metadata of a local claim.
 */
public class LocalClaim extends Claim {
    private List<AttributeMapping> mappedAttributes;

    public LocalClaim(String claimURI) {
        super(ClaimConstants.LOCAL_CLAIM_DIALECT_URI, claimURI);
        mappedAttributes = new ArrayList<>();
    }

    public LocalClaim(String claimURI, List<AttributeMapping> mappedAttributes, Map<String, String> claimProperties) {
        super(ClaimConstants.LOCAL_CLAIM_DIALECT_URI, claimURI, claimProperties);

        if (mappedAttributes == null) {
            mappedAttributes = new ArrayList<>();
        }

        this.mappedAttributes = mappedAttributes;
    }

    public List<AttributeMapping> getMappedAttributes() {
        return mappedAttributes;
    }

    public String getMappedAttribute(String userStoreDomainName) {
        for (AttributeMapping mappedAttribute : this.getMappedAttributes()) {
            if (mappedAttribute.getUserStoreDomain().equals(userStoreDomainName.toUpperCase())) {
                return mappedAttribute.getAttributeName();
            }
        }
        return null;
    }

    public void setMappedAttributes(List<AttributeMapping> mappedAttributes) {
        if (mappedAttributes == null) {
            mappedAttributes = new ArrayList<>();
        }
        this.mappedAttributes = mappedAttributes;
    }

    public void setMappedAttribute(AttributeMapping mappedAttribute) {
        this.mappedAttributes.add(mappedAttribute);
    }
}
