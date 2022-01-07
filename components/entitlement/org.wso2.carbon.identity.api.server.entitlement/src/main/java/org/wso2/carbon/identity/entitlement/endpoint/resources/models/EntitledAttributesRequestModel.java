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

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(propOrder = {
        "subjectName",
        "resourceName",
        "subjectId",
        "action",
        "enableChildSearch"
})
@JsonPropertyOrder({
        "subjectName",
        "resourceName",
        "subjectId",
        "action",
        "enableChildSearch"
})
@XmlRootElement(name = "EntitledAttributesRequest")
/**
 * Modeel class representing Entitled Attributes Request
 */
public class EntitledAttributesRequestModel {
    @XmlElement(required = false)
    private String subjectName;
    @XmlElement(required = false)
    private String resourceName;
    @XmlElement(required = false)
    private String subjectId;
    @XmlElement(required = false)
    private String action;
    @XmlElement(required = false)
    private boolean enableChildSearch;

    public String getSubjectName() {
        return subjectName;
    }

    public void setSubjectName(String subjectName) {
        this.subjectName = subjectName;
    }

    public String getResourceName() {
        return resourceName;
    }

    public void setResourceName(String resourceName) {
        this.resourceName = resourceName;
    }

    public String getSubjectId() {
        return subjectId;
    }

    public void setSubjectId(String subjectId) {
        this.subjectId = subjectId;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public boolean isEnableChildSearch() {
        return enableChildSearch;
    }

    public void setEnableChildSearch(boolean enableChildSearch) {
        this.enableChildSearch = enableChildSearch;
    }
}

