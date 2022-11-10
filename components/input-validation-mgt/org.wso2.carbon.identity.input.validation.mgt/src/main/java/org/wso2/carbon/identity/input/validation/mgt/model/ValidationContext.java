/*
 * Copyright (c) 2022, WSO2 LLC. (http://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.identity.input.validation.mgt.model;

import java.util.Map;

/**
 * Basic context of validation configurations.
 */
public class ValidationContext {

    private String field;
    private String tenantDomain;
    private Map<String, String> properties;
    private String value;

    /**
     * Constructor without attributes.
     */
    public ValidationContext() {}

    /**
     * Constructor with field, tenant name, properties and value.
     *
     * @param field         Name of field.
     * @param tenantDomain  Tenant domain.
     * @param properties    Properties.
     * @param value         Value.
     */
    public ValidationContext(String field, String tenantDomain, Map<String, String> properties, String value) {

        this.field = field;
        this.tenantDomain = tenantDomain;
        this.properties = properties;
        this.value = value;
    }

    /**
     * Method to get field name.
     *
     * @return  Field name.
     */
    public String getField() {

        return field;
    }

    /**
     * Method to get tenant name.
     *
     * @return  Tenant domain.
     */
    public String getTenantDomain() {

        return tenantDomain;
    }

    /**
     * Method to get properties.
     *
     * @return  Properties.
     */
    public Map<String, String> getProperties() {

        return properties;
    }

    /**
     *  Method to get value.
     *
     * @return  Value.
     */
    public String getValue() {

        return value;
    }

    /**
     * Method to set field name.
     *
     * @param field Field name.
     */
    public void setField(String field) {

        this.field = field;
    }

    /**
     *  Method to set tenant name.
     *
     * @param tenantDomain  Tenant domain.
     */
    public void setTenantDomain(String tenantDomain) {

        this.tenantDomain = tenantDomain;
    }

    /**
     *  Method to set properties.
     *
     * @param properties    Properties.
     */
    public void setProperties(Map<String, String> properties) {

        this.properties = properties;
    }

    /**
     *  Method to set value.
     *
     * @param value Value.
     */
    public void setValue(String value) {

        this.value = value;
    }
}
