/*
 * Copyright (c) 2026, WSO2 LLC. (http://www.wso2.com).
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
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.identity.workflow.mgt.rule;

import org.wso2.carbon.identity.rule.metadata.api.model.Field;
import org.wso2.carbon.identity.rule.metadata.api.model.FieldDefinition;
import org.wso2.carbon.identity.rule.metadata.api.model.InputValue;
import org.wso2.carbon.identity.rule.metadata.api.model.Link;
import org.wso2.carbon.identity.rule.metadata.api.model.Operator;
import org.wso2.carbon.identity.rule.metadata.api.model.OptionsInputValue;
import org.wso2.carbon.identity.rule.metadata.api.model.OptionsReferenceValue;
import org.wso2.carbon.identity.rule.metadata.api.model.OptionsValue;
import org.wso2.carbon.identity.rule.metadata.api.model.Value;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Fluent builder for constructing {@link FieldDefinition} objects for approval workflow rule fields.
 *
 */
public class FieldDefinitionBuilder {

    private static final List<Operator> OPERATORS = Arrays.asList(
            new Operator("equals", "equals"),
            new Operator("notEquals", "not equals"),
            new Operator("contains", "contains")
    );

    private String name;
    private String displayName;
    private List<Operator> operators;

    /**
     * Set the field name (used as the rule expression attribute key).
     *
     * @param name Field name.
     * @return This builder.
     */
    public FieldDefinitionBuilder name(String name) {

        this.name = name;
        return this;
    }

    /**
     * Set the human-readable display name for the field.
     *
     * @param displayName Display name.
     * @return This builder.
     */
    public FieldDefinitionBuilder displayName(String displayName) {

        this.displayName = displayName;
        return this;
    }

    /**
     * Set the operators applicable for this field.
     *
     * @param operatorNames One or more operator names.
     * @return This builder.
     */
    public FieldDefinitionBuilder operators(String... operatorNames) {

        this.operators = Arrays.stream(operatorNames)
                .map(opName -> OPERATORS.stream()
                        .filter(op -> op.getName().equals(opName))
                        .findFirst()
                        .orElseThrow(() -> new IllegalArgumentException("Unknown operator: " + opName)))
                .collect(Collectors.toList());
        return this;
    }

    /**
     * Start building an options value (either reference-based or fixed-value list).
     *
     * @return An {@link OptionsValueBuilder} for further configuration.
     */
    public OptionsValueBuilder options() {

        return new OptionsValueBuilder(this);
    }

    /**
     * Start building a free-text input value.
     *
     * @return An {@link InputValueBuilder} for further configuration.
     */
    public InputValueBuilder input() {

        return new InputValueBuilder(this);
    }

    private FieldDefinition buildWithValue(Value value) {

        if (name == null || name.isEmpty()) {
            throw new IllegalStateException("Field name must be set.");
        }
        if (displayName == null || displayName.isEmpty()) {
            throw new IllegalStateException("Field displayName must be set.");
        }
        if (operators == null || operators.isEmpty()) {
            throw new IllegalStateException("At least one operator must be set.");
        }
        return new FieldDefinition(new Field(name, displayName), operators, value);
    }

    /**
     * Builder for options-type values (both reference and fixed-value list).
     */
    public static class OptionsValueBuilder {

        private final FieldDefinitionBuilder parent;
        private Value.ValueType valueType;
        private String referenceAttr;
        private String displayAttr;
        private final List<Link> links = new ArrayList<>();
        private final List<OptionsValue> fixedValues = new ArrayList<>();

        private OptionsValueBuilder(FieldDefinitionBuilder parent) {

            this.parent = parent;
        }

        /**
         * Set the value type.
         *
         * @param valueType Value type.
         * @return This builder.
         */
        public OptionsValueBuilder valueType(Value.ValueType valueType) {

            this.valueType = valueType;
            return this;
        }

        /**
         * Set the attribute on the referenced object used as the stored value.
         * Only applicable when valueType is {@link Value.ValueType#REFERENCE}.
         *
         * @param referenceAttr Attribute name (e.g., {"id"}).
         * @return This builder.
         */
        public OptionsValueBuilder referenceAttr(String referenceAttr) {

            this.referenceAttr = referenceAttr;
            return this;
        }

        /**
         * Set the attribute on the referenced object shown in the UI.
         * Only applicable when valueType is {@link Value.ValueType#REFERENCE}.
         *
         * @param displayAttr Attribute name (e.g., {"displayName"}).
         * @return This builder.
         */
        public OptionsValueBuilder displayAttr(String displayAttr) {

            this.displayAttr = displayAttr;
            return this;
        }

        /**
         * Add an API link used to fetch or filter selectable values.
         * Only applicable when valueType is {@link Value.ValueType#REFERENCE}.
         *
         * @param href   API path (e.g., {"/scim2/Groups?offset=0&limit=10"}).
         * @param method HTTP method (e.g., {"GET"}).
         * @param rel    Link relation: {"values"} for the list endpoint, {"filter"} for search.
         * @return This builder.
         */
        public OptionsValueBuilder addLink(String href, String method, String rel) {

            this.links.add(new Link(href, method, rel));
            return this;
        }

        /**
         * Add a fixed selectable value for the field.
         * Only applicable when valueType is not {@link Value.ValueType#REFERENCE}.
         *
         * @param name        Stored value (e.g., {"true"}).
         * @param displayName UI label (e.g., {"true"}).
         * @return This builder.
         */
        public OptionsValueBuilder addFixedValue(String name, String displayName) {

            this.fixedValues.add(new OptionsValue(name, displayName));
            return this;
        }

        /**
         * Build and return the completed FieldDefinition.
         *
         * @return Constructed field definition.
         */
        public FieldDefinition build() {

            if (valueType == null) {
                throw new IllegalStateException("valueType must be set on options value.");
            }
            Value value;
            if (valueType == Value.ValueType.REFERENCE) {
                value = new OptionsReferenceValue.Builder()
                        .valueType(valueType)
                        .valueReferenceAttribute(referenceAttr)
                        .valueDisplayAttribute(displayAttr)
                        .links(links)
                        .build();
            } else {
                value = new OptionsInputValue(valueType, fixedValues);
            }
            return parent.buildWithValue(value);
        }
    }

    /**
     * Builder for free-text input values.
     */
    public static class InputValueBuilder {

        private final FieldDefinitionBuilder parent;
        private Value.ValueType valueType;

        private InputValueBuilder(FieldDefinitionBuilder parent) {

            this.parent = parent;
        }

        /**
         * Set the value type (e.g., {@link Value.ValueType#STRING}).
         *
         * @param valueType Value type.
         * @return This builder.
         */
        public InputValueBuilder valueType(Value.ValueType valueType) {

            this.valueType = valueType;
            return this;
        }

        /**
         * Build and return the completed FieldDefinition.
         *
         * @return Constructed field definition.
         */
        public FieldDefinition build() {

            if (valueType == null) {
                throw new IllegalStateException("valueType must be set on input value.");
            }
            return parent.buildWithValue(new InputValue(valueType));
        }
    }
}
