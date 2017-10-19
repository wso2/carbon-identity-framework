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
    private Map<String, String> claimProperties;

    public LocalClaim(String claimURI) {
        super(ClaimConstants.LOCAL_CLAIM_DIALECT_URI, claimURI);
        mappedAttributes = new ArrayList<>();
        claimProperties = new HashMap<>();
    }

    public LocalClaim(String claimURI, List<AttributeMapping> mappedAttributes, Map<String, String> claimProperties) {
        super(ClaimConstants.LOCAL_CLAIM_DIALECT_URI, claimURI);

        if (mappedAttributes == null) {
            mappedAttributes = new ArrayList<>();
        }

        if (claimProperties == null) {
            claimProperties = new HashMap<>();
        }

        this.mappedAttributes = mappedAttributes;
        this.claimProperties = claimProperties;
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

    public Map<String, String> getClaimProperties() {
        return claimProperties;
    }

    public String getClaimProperty(String propertyName) {
        if (this.getClaimProperties().containsKey(propertyName)) {
            this.getClaimProperties().get(propertyName);
        }
        return null;
    }

    public void setClaimProperties(Map<String, String> claimProperties) {
        if (claimProperties == null) {
            claimProperties = new HashMap<>();
        }
        this.claimProperties = claimProperties;
    }

    public void setClaimProperty(String propertyName, String propertyValue) {
        this.getClaimProperties().put(propertyName, propertyValue);
    }
}
