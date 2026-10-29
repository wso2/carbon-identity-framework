/*
 * Copyright (c) 2026, WSO2 LLC. (https://www.wso2.com) All Rights Reserved.
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.identity.flow.mgt.model;

/**
 * The type of value a header, query parameter or variable a flow defines holds.
 */
public enum FlowInputDataType {

    STRING,
    INTEGER,
    DECIMAL,
    BOOLEAN,
    STRING_ARRAY,
    NUMBER_ARRAY;

    /**
     * Whether the value is a list of entries.
     *
     * @return True for the array types.
     */
    public boolean isArray() {

        return this == STRING_ARRAY || this == NUMBER_ARRAY;
    }

    /**
     * The type of a value with the given name, or null when no type has that name.
     *
     * @param name Name of the type.
     * @return The type, or null.
     */
    public static FlowInputDataType fromName(String name) {

        try {
            return name == null ? null : valueOf(name);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
