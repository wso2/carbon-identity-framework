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

package org.wso2.carbon.identity.rule.metadata.api.model;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.wso2.carbon.identity.rule.metadata.internal.deserializer.ValueTypeDeserializer;

/**
 * Represents a value meta of a field.
 */
public abstract class Value {

    private final InputType inputType;
    @JsonDeserialize(using = ValueTypeDeserializer.class)
    private final ValueType valueType;

    protected Value(InputType inputType, ValueType valueType) {

        this.inputType = inputType;
        this.valueType = valueType;
    }

    public InputType getInputType() {

        return inputType;
    }

    public ValueType getValueType() {

        return valueType;
    }

    /**
     * Represents the input type of the value.
     */
    public enum InputType {
        INPUT, OPTIONS,
    }

    /**
     * Represents the value type of the value.
     */
    public enum ValueType {
        STRING, NUMBER, BOOLEAN, DATE_TIME, REFERENCE
    }
}
