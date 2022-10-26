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
 * Configuration for validation param.
 */
public class ValidationParam {

    private String name;
    private String value;

    /**
     * The constructor with name and value.
     *
     * @param name  Param name.
     * @param value Param value.
     */
    public ValidationParam(String name, String value) {

        this.name = name;
        this.value = value;
    }

    /**
     * Method to get name.
     *
     * @return name.
     */
    public String getName() {

        return name;
    }

    /**
     * Method to get value.
     *
     * @return value.
     */
    public String getValue() {

        return value;
    }
}
