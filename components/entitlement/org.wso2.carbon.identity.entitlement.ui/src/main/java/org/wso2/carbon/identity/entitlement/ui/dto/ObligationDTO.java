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
 * encapsulates obligation and advice expression data that requires for policy editor
 */
public class ObligationDTO {

    private String type;

    private String obligationId;

    private String effect;

    private String attributeValue;

    private String attributeValueDataType;

    private String resultAttributeId;

    private boolean notCompleted;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getResultAttributeId() {
        return resultAttributeId;
    }

    public void setResultAttributeId(String resultAttributeId) {
        this.resultAttributeId = resultAttributeId;
    }

    public String getAttributeValue() {
        return attributeValue;
    }

    public void setAttributeValue(String attributeValue) {
        this.attributeValue = attributeValue;
    }

    public String getAttributeValueDataType() {
        return attributeValueDataType;
    }

    public void setAttributeValueDataType(String attributeValueDataType) {
        this.attributeValueDataType = attributeValueDataType;
    }

    public String getEffect() {
        return effect;
    }

    public void setEffect(String effect) {
        this.effect = effect;
    }

    public String getObligationId() {
        return obligationId;
    }

    public void setObligationId(String obligationId) {
        this.obligationId = obligationId;
    }

    public boolean isNotCompleted() {
        return notCompleted;
    }

    public void setNotCompleted(boolean notCompleted) {
        this.notCompleted = notCompleted;
    }
}
