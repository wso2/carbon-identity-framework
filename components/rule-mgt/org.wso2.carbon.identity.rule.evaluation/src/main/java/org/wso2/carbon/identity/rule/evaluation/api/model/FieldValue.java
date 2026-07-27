/*
 * Copyright (c) 2025, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.carbon.identity.rule.evaluation.api.model;

import java.util.List;

/**
 * Field value model.
 * This class represents a field value in the context that is utilized at rule evaluation.
 */
public class FieldValue {

    private final String name;
    private final ValueType valueType;
    private final Object value;

    public FieldValue(String name, String value, ValueType valueType) {

        if (!(valueType == ValueType.STRING || valueType instanceof ValueType.ReferenceValueType)) {
            throw new IllegalArgumentException("Value type should be STRING or REFERENCE to set a string value");
        }

        this.name = name;
        this.valueType = valueType;
        this.value = value;
    }

    public FieldValue(String name, Boolean value) {

        this.name = name;
        this.valueType = ValueType.BOOLEAN;
        this.value = value;
    }

    public FieldValue(String name, Number value) {

        this.name = name;
        this.valueType = ValueType.NUMBER;
        this.value = Double.valueOf(value.toString());
    }

    public FieldValue(String name, List<String> value) {

        this.name = name;
        this.valueType = ValueType.LIST;
        this.value = value;
    }

    public String getName() {

        return name;
    }

    public ValueType getValueType() {

        return valueType;
    }

    public Object getValue() {

        return value;
    }
}
