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
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.identity.user.registration.dto;

public class UserFieldDTO {

    private String fieldName;
    private String fieldValue;
    private String claimUri;
    private String defaultValue;
    private String[] inputValues;
    private int minLength;
    private int maxLength;
    private boolean required;
    private boolean supportedByDefault;
    private int displayOrder;
    private String regEx;

    public String getRegEx() {
        return regEx;
    }

    public void setRegEx(String regEx) {
        this.regEx = regEx;
    }

    public String getClaimUri() {
        return claimUri;
    }

    public void setClaimUri(String claimUri) {
        this.claimUri = claimUri;
    }

    public String getFieldValue() {
        return fieldValue;
    }

    public void setFieldValue(String fieldValue) {
        this.fieldValue = fieldValue;
    }

    public boolean isRequired() {
        return required;
    }

    public void setRequired(boolean required) {
        this.required = required;
    }

    public String getFieldName() {
        return fieldName;
    }

    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    public String[] getInputValues() {
        if(inputValues!=null){
            return inputValues.clone();
        }
        return new String[0];
    }

    public void setInputValues(String[] inputValues) {
        if(inputValues!=null){
            this.inputValues = inputValues.clone();
        }
    }

    public int getMinLength() {
        return minLength;
    }

    public void setMinLength(int minLength) {
        this.minLength = minLength;
    }

    public int getMaxLength() {
        return maxLength;
    }

    public void setMaxLength(int maxLength) {
        this.maxLength = maxLength;
    }

    public int getDisplayOrder() {
        return displayOrder;
    }

    public void setDisplayOrder(int displayOrder) {
        this.displayOrder = displayOrder;
    }

    public boolean getSupportedByDefault() {
        return supportedByDefault;
    }

    public void setSupportedByDefault(boolean isSupportedByDefault) {
        this.supportedByDefault = isSupportedByDefault;
    }
}
