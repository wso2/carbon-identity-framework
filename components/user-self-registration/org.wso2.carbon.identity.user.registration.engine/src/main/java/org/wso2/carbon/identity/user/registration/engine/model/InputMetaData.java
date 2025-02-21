/*
 * Copyright (c) 2025, WSO2 LLC. (https://www.wso2.com) All Rights Reserved.
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.identity.user.registration.engine.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Model class to represent the metadata of an input field.
 */
public class InputMetaData {

    private String id;
    private String name;
    private String availableValue;
    private String dataType;
    private boolean isMandatory;
    private boolean isReadOnly;
    private int order;
    private String validationRegex;
    private String i18nKey;

    public InputMetaData(String id, String name, String dataType, int order) {

        this.id = id;
        this.name = name;
        this.dataType = dataType;
        this.setOrder(order);
    }

    public String getId() {

        return id;
    }

    public void setId(String id) {

        this.id = id;
    }

    public String getName() {

        return name;
    }

    public void setName(String name) {

        this.name = name;
    }

    public String getAvailableValue() {

        return availableValue;
    }

    public void setAvailableValue(String availableValue) {

        this.availableValue = availableValue;
    }

    public String getDataType() {

        return dataType;
    }

    public void setDataType(String dataType) {

        this.dataType = dataType;
    }

    public boolean isMandatory() {

        return isMandatory;
    }

    public void setMandatory(boolean mandatory) {

        isMandatory = mandatory;
    }

    public boolean isReadOnly() {

        return isReadOnly;
    }

    public void setReadOnly(boolean readOnly) {

        isReadOnly = readOnly;
    }

    public int getOrder() {

        return order;
    }

    public void setOrder(int order) {

        this.order = order;
    }

    public String getValidationRegex() {

        return validationRegex;
    }

    public void setValidationRegex(String validationRegex) {

        this.validationRegex = validationRegex;
    }

    public String getI18nKey() {

        return i18nKey;
    }

    public void setI18nKey(String i18nKey) {

        this.i18nKey = i18nKey;
    }
}
