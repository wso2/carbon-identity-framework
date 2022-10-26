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
 * Configuration object for rules validation.
 */
public class RulesValidator {

    private DefaultValidator lengthValidator;
    private DefaultValidator numeralsValidator;
    private DefaultValidator upperCaseValidator;
    private DefaultValidator lowerCaseValidator;
    private DefaultValidator specialCharacterValidator;
    private CharacterSequenceValidator repeatedCharacterValidator;
    private CharacterSequenceValidator uniqueCharacterValidator;

    /**
     * Method to get length validator.
     *
     * @return  length validator.
     */
    public DefaultValidator getLengthValidator() {

        return lengthValidator;
    }

    /**
     * Method to get numerals validator.
     *
     * @return  numerals validator.
     */
    public DefaultValidator getNumeralsValidator() {

        return numeralsValidator;
    }

    /**
     * Method to get lower-case validator.
     *
     * @return  lower-case validator.
     */
    public DefaultValidator getLowerCaseValidator() {

        return lowerCaseValidator;
    }

    /**
     * Method to get special character validator.
     *
     * @return  special character validator.
     */
    public DefaultValidator getSpecialCharacterValidator() {

        return specialCharacterValidator;
    }

    /**
     * Method to get upper-case validator.
     *
     * @return  upper-case validator.
     */
    public DefaultValidator getUpperCaseValidator() {

        return upperCaseValidator;
    }

    /**
     * Method to get repeated character validator.
     *
     * @return  repeated character validator.
     */
    public CharacterSequenceValidator getRepeatedCharacterValidator() {

        return repeatedCharacterValidator;
    }

    /**
     * Method to get unicode validator.
     *
     * @return unicode validator.
     */
    public CharacterSequenceValidator getUniqueCharacterValidator() {

        return uniqueCharacterValidator;
    }

    /**
     * Method to set length validator.
     *
     * @param lengthValidator   length validator.
     */
    public void setLengthValidator(DefaultValidator lengthValidator) {

        this.lengthValidator = lengthValidator;
    }

    /**
     * Method to set lower case validator.
     *
     * @param lowerCaseValidator    lower case validator.
     */
    public void setLowerCaseValidator(DefaultValidator lowerCaseValidator) {

        this.lowerCaseValidator = lowerCaseValidator;
    }

    /**
     * Method to set numerals validator.
     *
     * @param numeralsValidator numerals validator.
     */
    public void setNumeralsValidator(DefaultValidator numeralsValidator) {

        this.numeralsValidator = numeralsValidator;
    }

    /**
     * Method to set repeated character validator.
     *
     * @param repeatedCharacterValidator    repeated character validator.
     */
    public void setRepeatedCharacterValidator(CharacterSequenceValidator repeatedCharacterValidator) {

        this.repeatedCharacterValidator = repeatedCharacterValidator;
    }

    /**
     * Method to set special character validator.
     * @param specialCharacterValidator special character validator.
     */
    public void setSpecialCharacterValidator(DefaultValidator specialCharacterValidator) {

        this.specialCharacterValidator = specialCharacterValidator;
    }

    /**
     * Method to set unique character validator.
     *
     * @param uniqueCharacterValidator  unique character validator.
     */
    public void setUniqueCharacterValidator(CharacterSequenceValidator uniqueCharacterValidator) {

        this.uniqueCharacterValidator = uniqueCharacterValidator;
    }

    /**
     * Method to set upper case validator.
     *
     * @param upperCaseValidator    upper case validator.
     */
    public void setUpperCaseValidator(DefaultValidator upperCaseValidator) {

        this.upperCaseValidator = upperCaseValidator;
    }
}
