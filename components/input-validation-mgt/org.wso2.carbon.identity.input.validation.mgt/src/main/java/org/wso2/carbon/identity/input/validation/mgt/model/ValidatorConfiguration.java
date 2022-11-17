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

import java.util.List;

/**
 * Configurations of a validator.
 */
public class ValidatorConfiguration {

    private String type;
    private String name;
    private List<Property> properties;

    /**
     * Method to set name.
     *
     * @param name  Name.
     */
    public void setName(String name) {

        this.name = name;
    }

    /**
     * Method to set type.
     *
     * @param type  Type.
     */
    public void setType(String type) {

        this.type = type;
    }

    /**
     * Method to set properties.
     * @param properties    Properties.
     */
    public void setProperties(List<Property> properties) {

        this.properties = properties;
    }

    /**
     * Method to get properties.
     *
     * @return  Properties.
     */
    public List<Property> getProperties() {

        return properties;
    }

    /**
     * Method to get name.
     *
     * @return  Name.
     */
    public String getName() {

        return name;
    }

    /**
     * Method to get type.
     *
     * @return  type.
     */
    public String getType() {

        return type;
    }
}
