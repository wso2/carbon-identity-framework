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
 * Object to store count of characters.
 */
public class CharacterCounter {

    private int numberOfDigits;
    private int numberOfUpperCase;
    private int numberOfLowerCase;
    private int numberOfSpecialChrs;

    /**
     * The constructor.
     *
     * @param numberOfDigits        number of digits.
     * @param numberOfUpperCase     number of upper-case characters.
     * @param numberOfLowerCase     number of lower-case characters.
     * @param numberOfSpecialChrs   number of special characters.
     */
    public CharacterCounter(int numberOfDigits, int numberOfUpperCase, int numberOfLowerCase, int numberOfSpecialChrs) {

        this.numberOfDigits = numberOfDigits;
        this.numberOfUpperCase = numberOfUpperCase;
        this.numberOfLowerCase = numberOfLowerCase;
        this.numberOfSpecialChrs = numberOfSpecialChrs;
    }

    /**
     * Method to get number of digits.
     *
     * @return numberOfDigits.
     */
    public int getNumberOfDigits() {

        return numberOfDigits;
    }

    /**
     * Method to get number of lower-case characters.
     *
     * @return numberOfLowerCase.
     */
    public int getNumberOfLowerCase() {

        return numberOfLowerCase;
    }

    /**
     * Method to get number of special characters.
     *
     * @return numberOfSpecialChrs.
     */
    public int getNumberOfSpecialChrs() {

        return numberOfSpecialChrs;
    }

    /**
     * Method to get number of upper-case characters.
     *
     * @return numberOfUpperCase.
     */
    public int getNumberOfUpperCase() {

        return numberOfUpperCase;
    }

    /**
     * Method to set number of digits.
     *
     * @param numberOfDigits    number of digits.
     */
    public void setNumberOfDigits(int numberOfDigits) {

        this.numberOfDigits = numberOfDigits;
    }

    /**
     * Method to set number of lower-case characters.
     *
     * @param numberOfLowerCase number of lower-case characters.
     */
    public void setNumberOfLowerCase(int numberOfLowerCase) {

        this.numberOfLowerCase = numberOfLowerCase;
    }

    /**
     * Method to set number of special characters.
     *
     * @param numberOfSpecialChrs   number of special characters.
     */
    public void setNumberOfSpecialChrs(int numberOfSpecialChrs) {

        this.numberOfSpecialChrs = numberOfSpecialChrs;
    }

    /**
     * Method to set number of upper-case characters.
     *
     * @param numberOfUpperCase number of upper-case characters.
     */
    public void setNumberOfUpperCase(int numberOfUpperCase) {

        this.numberOfUpperCase = numberOfUpperCase;
    }
}
