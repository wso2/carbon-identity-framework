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

/**
 * Configuration object for password validator object.
 */
public class PasswordValidator {

    private RegExValidator regExValidator;
    private RulesValidator rulesValidator;

    /**
     * Method to set regEx validator.
     *
     * @param regExValidator    Regex validator.
     */
    public void setRegExValidator(RegExValidator regExValidator) {

        this.regExValidator = regExValidator;
    }

    /**
     * Method to set rules validator.
     *
     * @param rulesValidator    Rules validator.
     */
    public void setRulesValidator(RulesValidator rulesValidator) {
        this.rulesValidator = rulesValidator;
    }


    /**
     * Method to get rules validator.
     *
     * @return  Rules validator.
     */
    public RulesValidator getRulesValidator() {
        return rulesValidator;
    }

    /**
     * Method to get regex validator.
     *
     * @return Regex validator.
     */
    public RegExValidator getRegExValidator() {

        return regExValidator;
    }
}
