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
 * Define the rules set configuration.
 */
public class RulesConfiguration {

    private String validatorName;
    private Map<String, String> properties;

    /**
     * Constructor without any attributes.
     */
    public RulesConfiguration() {}

    /**
     * Constructor with validator name and properties.
     *
     * @param validatorName     Validator name.
     * @param properties    Properties.
     */
    public RulesConfiguration(String validatorName, Map<String, String> properties) {

        this.validatorName = validatorName;
        this.properties = properties;
    }

    /**
     * Method to set validator name.
     *
     * @param validatorName Name of the validator.
     */
    public void setValidatorName(String validatorName) {

        this.validatorName = validatorName;
    }

    /**
     * Method to set the properties of the validator.
     *
     * @param properties    Properties.
     */
    public void setProperties(Map<String, String> properties) {

        this.properties = properties;
    }

    /**
     * Method to get validator name.
     *
     * @return  Name of the validator.
     */
    public String getValidatorName() {

        return validatorName;
    }

    /**
     * Method to get the properties.
     *
     * @return  Properties.
     */
    public Map<String, String> getProperties() {

        return properties;
    }
}
