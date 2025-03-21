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

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;

/**
 * Represents an expression in Rule Management.
 * This class has a field, an operator and a value.
 */
@JsonDeserialize(builder = Expression.Builder.class)
public class Expression {

    private final String field;
    private final String operator;
    private final Value value;

    private Expression(Builder builder) {

        this.field = builder.field;
        this.operator = builder.operator;
        this.value = resolveValue(builder);
    }

    public String getField() {

        return field;
    }

    public String getOperator() {

        return operator;
    }

    public Value getValue() {

        return value;
    }

    private Value resolveValue(Builder builder) {

        if (builder.rawValue != null) {
            return new Value(Value.Type.RAW, builder.rawValue);
        } else {
            return builder.value;
        }
    }

    /**
     * Builder for the Expression.
     */
    @JsonPOJOBuilder(withPrefix = "")
    public static class Builder {

        private String field;
        private String operator;
        private Value value;
        private String rawValue;

        public Builder field(String field) {

            this.field = field;
            return this;
        }

        public Builder operator(String operator) {

            this.operator = operator;
            return this;
        }

        @JsonProperty("value")
        public Builder value(Value value) {

            this.value = value;
            return this;
        }

        /**
         * '@JsonIgnore' annotation is used to ignore the invocation of this method when deserializing the object,
         * from JSON from the database.
         * {@link #value(Value)} method is expected to be invoked at deserialization.
         */
        @JsonIgnore
        public Builder value(String value) {

            this.rawValue = value;
            return this;
        }

        public Expression build() {

            if (field == null || field.isEmpty()) {
                throw new IllegalArgumentException("Field must be provided.");
            }

            if (operator == null || operator.isEmpty()) {
                throw new IllegalArgumentException("Operator must be provided.");
            }

            if (value == null && rawValue == null) {
                throw new IllegalArgumentException("Either primitive value or Value with type must be provided.");
            }

            return new Expression(this);
        }
    }
}
