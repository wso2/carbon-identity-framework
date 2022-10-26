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
 * Configuration object for Character sequence validator.
 */
public class CharacterSequenceValidator {

    private boolean enable;
    private boolean caseSensitive;

    /**
     * Method to get whether it is case-sensitive.
     *
     * @return boolean whether it is case-sensitive.
     */
    public boolean isCaseSensitive() {
        return caseSensitive;
    }

    /**
     * Method to get whether it is enabled.
     *
     * @return boolean whether it is enabled.
     */
    public boolean isEnable() {
        return enable;
    }

    /**
     * Method to set case-sensitive.
     *
     * @param caseSensitive Status for case-sensitive.
     */
    public void setCaseSensitive(boolean caseSensitive) {
        this.caseSensitive = caseSensitive;
    }

    /**
     * Method to set enable.
     *
     * @param enable    Status for enable.
     */
    public void setEnable(boolean enable) {
        this.enable = enable;
    }
}
