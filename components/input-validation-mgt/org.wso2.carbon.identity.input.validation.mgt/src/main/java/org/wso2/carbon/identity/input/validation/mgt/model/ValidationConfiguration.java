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
 * Input validation configurations.
 */
public class ValidationConfiguration {

    private String field;

    List<RulesConfiguration> rules;
    List<RulesConfiguration> regEx;

    /**
     * Method to set the field.
     *
     * @param field Name of the field.
     */
    public void setField(String field) {

        this.field = field;
    }

    /**
     * Method to set list of rules.
     *
     * @param rules List of rules.
     */
    public void setRules(List<RulesConfiguration> rules) {

        this.rules = rules;
    }

    /**
     * Method to set list of regex.
     *
     * @param regEx List of regex.
     */
    public void setRegEx(List<RulesConfiguration> regEx) {

        this.regEx = regEx;
    }

    /**
     * Method to get field.
     *
     * @return  Field name.
     */
    public String getField() {

        return field;
    }

    /**
     * Method to get rules.
     *
     * @return  List of rules.
     */
    public List<RulesConfiguration> getRules() {

        return rules;
    }

    /**
     * Method to get regex.
     *
     * @return  List of regex.
     */
    public List<RulesConfiguration> getRegEx() {

        return regEx;
    }
}
