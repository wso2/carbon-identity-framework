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

import java.util.List;

/**
 *
 */
public class BasicRequestDTO {


    private List<RowDTO> rowDTOs;

    private String resources;

    private String subjects;

    private String actions;

    private String enviornement;

    private String userAttributeValue;

    private String userAttributeId;

    public String getResources() {
        return resources;
    }

    public void setResources(String resources) {
        this.resources = resources;
    }

    public String getSubjects() {
        return subjects;
    }

    public void setSubjects(String subjects) {
        this.subjects = subjects;
    }

    public String getActions() {
        return actions;
    }

    public void setActions(String actions) {
        this.actions = actions;
    }

    public String getUserAttributeValue() {
        return userAttributeValue;
    }

    public void setUserAttributeValue(String userAttributeValue) {
        this.userAttributeValue = userAttributeValue;
    }

    public String getUserAttributeId() {
        return userAttributeId;
    }

    public void setUserAttributeId(String userAttributeId) {
        this.userAttributeId = userAttributeId;
    }

    public String getEnviornement() {
        return enviornement;
    }

    public void setEnviornement(String enviornement) {
        this.enviornement = enviornement;
    }

    public List<RowDTO> getRowDTOs() {
        return rowDTOs;
    }

    public void setRowDTOs(List<RowDTO> rowDTOs) {
        this.rowDTOs = rowDTOs;
    }

    public void addRowDTOs(RowDTO rowDTO) {
        this.rowDTOs.add(rowDTO);
    }
}
