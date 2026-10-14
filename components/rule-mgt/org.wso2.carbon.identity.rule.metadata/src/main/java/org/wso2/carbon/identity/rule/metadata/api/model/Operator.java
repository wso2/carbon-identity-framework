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

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import org.apache.commons.lang.StringUtils;

/**
 * Represents an operator in a rule.
 */
public class Operator {

    private final String name;
    private final String displayName;
    private final ValueInput valueInput;

    public Operator(String name, String displayName) {

        this(name, displayName, null);
    }

    /**
     * An operator, and the value a condition using it takes.
     *
     * @param name        Operator name.
     * @param displayName Display name.
     * @param valueInput  Value a condition using the operator takes; a single value when absent.
     */
    @JsonCreator
    public Operator(@JsonProperty("name") String name, @JsonProperty("displayName") String displayName,
                    @JsonProperty("valueInput") ValueInput valueInput) {

        validate(name, displayName);
        this.name = name;
        this.displayName = displayName;
        this.valueInput = valueInput == null ? ValueInput.SINGLE : valueInput;
    }

    public String getName() {

        return name;
    }

    public String getDisplayName() {

        return displayName;
    }

    /**
     * The value a condition using this operator takes.
     *
     * @return NONE for an operator that checks the field alone, LIST for one that takes a list of values,
     * otherwise SINGLE.
     */
    public ValueInput getValueInput() {

        return valueInput;
    }

    private void validate(String name, String displayName) {

        if (StringUtils.isBlank(name)) {
            throw new IllegalArgumentException("Operator 'name' cannot be null or empty.");
        }

        if (StringUtils.isBlank(displayName)) {
            throw new IllegalArgumentException("Operator 'displayName' cannot be null or empty.");
        }
    }

    /**
     * The value a condition using an operator takes, which decides the input shown for it.
     */
    public enum ValueInput {

        NONE("none"), SINGLE("single"), LIST("list");

        private final String value;

        ValueInput(String value) {

            this.value = value;
        }

        @JsonValue
        public String getValue() {

            return value;
        }

        /**
         * Value input of the given name, in any case.
         *
         * @param value Name of the value input.
         * @return The value input.
         */
        @JsonCreator
        public static ValueInput fromValue(String value) {

            for (ValueInput valueInput : values()) {
                if (valueInput.value.equalsIgnoreCase(value)) {
                    return valueInput;
                }
            }
            throw new IllegalArgumentException("Unsupported value input: " + value);
        }
    }
}
