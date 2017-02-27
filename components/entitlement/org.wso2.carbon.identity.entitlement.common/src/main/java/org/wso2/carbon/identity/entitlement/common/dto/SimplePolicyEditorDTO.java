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

package org.wso2.carbon.identity.entitlement.common.dto;

import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class SimplePolicyEditorDTO {

    private String policyId;

    private String appliedCategory;

    private String description;

    private String userAttributeValue;

    private String userAttributeId;

    private String resourceValue;

    private String actionValue;

    private String environmentValue;

    private String function;

    private String environmentId;

    private List<SimplePolicyEditorElementDTO> SimplePolicyEditorElementDTOs =
            new ArrayList<SimplePolicyEditorElementDTO>();

    public String getPolicyId() {
        return policyId;
    }

    public void setPolicyId(String policyId) {
        this.policyId = policyId;
    }

    public String getAppliedCategory() {
        return appliedCategory;
    }

    public void setAppliedCategory(String appliedCategory) {
        this.appliedCategory = appliedCategory;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<SimplePolicyEditorElementDTO> getSimplePolicyEditorElementDTOs() {
        return SimplePolicyEditorElementDTOs;
    }

    public void setSimplePolicyEditorElementDTOs(List<SimplePolicyEditorElementDTO>
                                                         simplePolicyEditorElementDTOs) {
        this.SimplePolicyEditorElementDTOs = simplePolicyEditorElementDTOs;
    }

    public void setBasicPolicyEditorElementDTO(SimplePolicyEditorElementDTO
                                                       SimplePolicyEditorElementDTO) {
        this.SimplePolicyEditorElementDTOs.add(SimplePolicyEditorElementDTO);
    }

    public String getUserAttributeValue() {
        return userAttributeValue;
    }

    public void setUserAttributeValue(String userAttributeValue) {
        this.userAttributeValue = userAttributeValue;
    }

    public String getEnvironmentValue() {
        return environmentValue;
    }

    public void setEnvironmentValue(String environmentValue) {
        this.environmentValue = environmentValue;
    }

    public String getFunction() {
        return function;
    }

    public void setFunction(String function) {
        this.function = function;
    }

    public String getActionValue() {
        return actionValue;
    }

    public void setActionValue(String actionValue) {
        this.actionValue = actionValue;
    }

    public String getResourceValue() {
        return resourceValue;
    }

    public void setResourceValue(String resourceValue) {
        this.resourceValue = resourceValue;
    }

    public String getUserAttributeId() {
        return userAttributeId;
    }

    public void setUserAttributeId(String userAttributeId) {
        this.userAttributeId = userAttributeId;
    }

    public String getEnvironmentId() {
        return environmentId;
    }

    public void setEnvironmentId(String environmentId) {
        this.environmentId = environmentId;
    }
}
