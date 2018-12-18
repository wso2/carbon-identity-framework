/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.identity.configuration.mgt.core.model;

/**
 * A model class representing an attribute in a configuration
 * EX: Sender's email address in the email configuration
 */
public class Attribute {

    private String key;
    private String value;
    private String attributeId;

    /**
     * Initialize an Attribute object with a key,value pair.
     *
     * @param key   Name identifier for the Attribute object.
     * @param value Value for the Attribute object.
     */
    public Attribute(String key, String value) {

        this.key = key;
        this.value = value;
    }

    public Attribute(String key, String value, String attributeId) {

        this.key = key;
        this.value = value;
        this.attributeId = attributeId;
    }

    public Attribute() {

    }

    public String getAttributeId() {

        return attributeId;
    }

    public void setAttributeId(String attributeId) {

        this.attributeId = attributeId;
    }

    public String getKey() {

        return key;
    }

    public void setKey(String key) {

        this.key = key;
    }

    public String getValue() {

        return value;
    }

    public void setValue(String value) {

        this.value = value;
    }
}
