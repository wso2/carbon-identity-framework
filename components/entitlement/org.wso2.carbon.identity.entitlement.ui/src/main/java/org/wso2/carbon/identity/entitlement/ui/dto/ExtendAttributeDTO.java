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
 * extended attribute value element
 */
public class ExtendAttributeDTO {

    private String id;

    private String selector;

    private String function;

    private String category;

    private String attributeValue;

    private String attributeId;

    private String dataType;

    private String issuer;

    private boolean notCompleted;

    public ExtendAttributeDTO() {
    }

    public ExtendAttributeDTO(ExtendAttributeDTO dto) {
        this.id = dto.getId();
        this.selector = dto.getSelector();
        this.function = dto.getFunction();
        this.category = dto.getCategory();
        this.attributeValue = dto.getAttributeValue();
        this.attributeId = dto.getAttributeId();
        this.dataType = dto.getDataType();
        this.issuer = dto.getIssuer();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSelector() {
        return selector;
    }

    public void setSelector(String selector) {
        this.selector = selector;
    }

    public String getDataType() {
        return dataType;
    }

    public void setDataType(String dataType) {
        this.dataType = dataType;
    }

    public String getAttributeValue() {
        return attributeValue;
    }

    public void setAttributeValue(String attributeValue) {
        this.attributeValue = attributeValue;
    }

    public String getAttributeId() {
        return attributeId;
    }

    public void setAttributeId(String attributeId) {
        this.attributeId = attributeId;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getFunction() {
        return function;
    }

    public void setFunction(String function) {
        this.function = function;
    }

    public String getIssuer() {
        return issuer;
    }

    public void setIssuer(String issuer) {
        this.issuer = issuer;
    }

    public boolean isNotCompleted() {
        return notCompleted;
    }

    public void setNotCompleted(boolean notCompleted) {
        this.notCompleted = notCompleted;
    }
}
