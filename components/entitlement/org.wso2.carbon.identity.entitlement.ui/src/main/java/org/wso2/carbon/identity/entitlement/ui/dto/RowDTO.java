/*
*  Copyright (c) WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/

package org.wso2.carbon.identity.entitlement.ui.dto;

/**
 *  @deprecated  As this moved to org.wso2.carbon.identity.entitlement.common
 */
@Deprecated
public class RowDTO {

    private String category;

    private String preFunction;

    private String function;

    private String attributeValue;

    private String attributeId;

    private String attributeDataType;

    private String combineFunction;

    private boolean notCompleted;

    public RowDTO() {
    }

    public RowDTO(RowDTO rowDTO) {
        this.category = rowDTO.getCategory();
        this.preFunction = rowDTO.getPreFunction();
        this.function = rowDTO.getFunction();
        this.attributeValue = rowDTO.getAttributeValue();
        this.attributeId = rowDTO.getAttributeId();
        this.combineFunction = rowDTO.getCombineFunction();
        this.attributeDataType = rowDTO.getAttributeDataType();
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getCombineFunction() {
        return combineFunction;
    }

    public void setCombineFunction(String combineFunction) {
        this.combineFunction = combineFunction;
    }

    public String getAttributeDataType() {
        return attributeDataType;
    }

    public void setAttributeDataType(String attributeDataType) {
        this.attributeDataType = attributeDataType;
    }

    public String getAttributeId() {
        return attributeId;
    }

    public void setAttributeId(String attributeId) {
        this.attributeId = attributeId;
    }

    public String getAttributeValue() {
        return attributeValue;
    }

    public void setAttributeValue(String attributeValue) {
        this.attributeValue = attributeValue;
    }

    public String getFunction() {
        return function;
    }

    public void setFunction(String function) {
        this.function = function;
    }

    public String getPreFunction() {
        return preFunction;
    }

    public void setPreFunction(String preFunction) {
        this.preFunction = preFunction;
    }

    public boolean isNotCompleted() {
        return notCompleted;
    }

    public void setNotCompleted(boolean notCompleted) {
        this.notCompleted = notCompleted;
    }
}
