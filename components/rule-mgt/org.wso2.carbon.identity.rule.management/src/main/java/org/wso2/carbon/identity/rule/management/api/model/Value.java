/*
 * Copyright (c) 2024, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.carbon.identity.rule.management.api.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Represents a value in Rule Management.
 * This class has a type and a value.
 */
public class Value {

    private final Type type;
    private final String fieldValue;

    @JsonCreator
    public Value(@JsonProperty("type") Type type, @JsonProperty("value") String fieldValue) {

        this.type = type;
        this.fieldValue = fieldValue;
    }

    public Type getType() {
        return type;
    }

    @JsonProperty("value")
    public String getFieldValue() {
        return fieldValue;
    }

    /**
     * Represents the type of the value.
     */
    public enum Type {
        STRING, NUMBER, BOOLEAN, DATE_TIME, REFERENCE, RAW
    }
}
