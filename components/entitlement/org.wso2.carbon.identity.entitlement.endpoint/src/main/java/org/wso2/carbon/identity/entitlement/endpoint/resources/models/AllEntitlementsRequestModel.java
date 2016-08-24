/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.identity.entitlement.endpoint.resources.models;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.wso2.carbon.identity.entitlement.dto.AttributeDTO;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(propOrder = {
        "identifier",
        "givenAttributes"
})
@JsonPropertyOrder({
        "identifier",
        "givenAttributes"
})
@XmlRootElement(name = "AllEntitlementsRequest")
/**
 * Model class representing AllEntitlements Request
 */
public class AllEntitlementsRequestModel {
    @XmlElement(required = false)
    private String identifier;
    @XmlElement(required = false)
    private AttributeDTO[] givenAttributes;

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public AttributeDTO[] getGivenAttributes() {
        return givenAttributes;
    }

    public void setGivenAttributes(AttributeDTO[] givenAttributes) {
        this.givenAttributes = givenAttributes;
    }
}

