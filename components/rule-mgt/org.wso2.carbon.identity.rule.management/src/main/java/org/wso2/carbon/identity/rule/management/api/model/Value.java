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
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.util.List;

/**
 * Represents a value in Rule Management.
 * This class has a type and a value.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class Value implements Serializable {

    private static final long serialVersionUID = 1L;

    private final Type type;
    private final String fieldValue;
    private final List<String> fieldValues;
    private final FieldReference fieldReference;

    public Value(Type type, String fieldValue) {

        this(type, fieldValue, null, null);
    }

    /**
     * A value of type LIST, for operators whose right-hand side is a set rather than a scalar.
     *
     * @param fieldValues The values.
     */
    public Value(List<String> fieldValues) {

        this(Type.LIST, null, fieldValues, null);
    }

    /**
     * A value of type FIELD, read from another field when the rule is evaluated.
     *
     * @param fieldReference The field to read.
     */
    public Value(FieldReference fieldReference) {

        this(Type.FIELD, null, null, fieldReference);
    }

    /**
     * Reads every stored shape: a single value, a LIST's values, or a FIELD's reference. A value stored before
     * LIST and FIELD existed carries neither and reads exactly as it did.
     * <p>
     * Only LIST and FIELD are checked for their shape. A scalar has always been accepted with a null value, so
     * requiring one now would make rules stored earlier unreadable. A null type is not rejected either: the
     * stored rules are read with unknown enum constants as null, so that a value type added by a newer node
     * reads here as null and fails closed on evaluation instead of making the whole rule unreadable.
     */
    @JsonCreator
    public Value(@JsonProperty("type") Type type, @JsonProperty("value") String fieldValue,
                 @JsonProperty("values") List<String> fieldValues,
                 @JsonProperty("field") FieldReference fieldReference) {

        if (type == Type.LIST) {
            if (fieldValues == null) {
                throw new IllegalArgumentException("Values must not be null for type LIST.");
            }
            if (fieldValue != null || fieldReference != null) {
                throw new IllegalArgumentException("A LIST value carries only values.");
            }
        } else if (type == Type.FIELD) {
            if (fieldReference == null) {
                throw new IllegalArgumentException("Field reference must not be null for type FIELD.");
            }
            if (fieldValue != null || fieldValues != null) {
                throw new IllegalArgumentException("A FIELD value carries only a field reference.");
            }
        } else if (type != null && (fieldValues != null || fieldReference != null)) {
            throw new IllegalArgumentException("A " + type + " value carries only a single value.");
        }

        this.type = type;
        this.fieldValue = fieldValue;
        this.fieldValues = fieldValues;
        this.fieldReference = fieldReference;
    }

    public Type getType() {
        return type;
    }

    @JsonProperty("value")
    public String getFieldValue() {
        return fieldValue;
    }

    /**
     * The values of a LIST valued expression. Null for every other type, where {@link #getFieldValue()}
     * carries the single value instead.
     *
     * @return The values, or null when this is not a LIST.
     */
    @JsonProperty("values")
    public List<String> getFieldValues() {
        return fieldValues;
    }

    /**
     * The field a FIELD valued expression is compared against. Null for every other type.
     *
     * @return The referenced field, or null when this is not a FIELD.
     */
    @JsonProperty("field")
    public FieldReference getFieldReference() {
        return fieldReference;
    }

    /**
     * Represents the type of the value.
     */
    public enum Type {
        STRING, NUMBER, BOOLEAN, DATE_TIME, REFERENCE, RAW, LIST, FIELD
    }
}
