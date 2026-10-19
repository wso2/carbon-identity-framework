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

import org.wso2.carbon.identity.rule.metadata.internal.config.RuleMetadataConfigFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Represents the definition of a field in a rule.
 */
public class FieldDefinition {

    private final Field field;
    private final List<Operator> operators;
    private final Value value;
    private final ValueFieldOptions valueFieldOptions;

    public FieldDefinition(Field field, List<Operator> operators, Value value) {

        this(field, operators, value, null);
    }

    /**
     * A field whose value may also be read from other fields.
     *
     * @param field             The field.
     * @param operators         The operators the field takes.
     * @param value             How a value given in the rule is entered.
     * @param valueFieldOptions The fields a value may be read from, or null.
     */
    public FieldDefinition(Field field, List<Operator> operators, Value value, ValueFieldOptions valueFieldOptions) {

        this.field = field;
        this.operators = withConfiguredValueInput(operators);
        this.value = value;
        this.valueFieldOptions = valueFieldOptions;
        validateValue();
    }

    public Field getField() {

        return field;
    }

    public List<Operator> getOperators() {

        return operators;
    }

    public Value getValue() {

        return value;
    }

    /**
     * The fields a value may be read from.
     *
     * @return The value field options, or null when a value can only be given in the rule.
     */
    public ValueFieldOptions getValueFieldOptions() {

        return valueFieldOptions;
    }

    /**
     * Validates the operators and takes the value input of each one from the operator configuration, so a field
     * cannot declare otherwise.
     *
     * @param operators Operators of the field.
     * @return The operators, each carrying its configured value input.
     */
    private List<Operator> withConfiguredValueInput(List<Operator> operators) {

        if (operators == null || operators.isEmpty()) {
            throw new IllegalArgumentException("Operators cannot be null or empty.");
        }

        List<Operator> configured = new ArrayList<>(operators.size());
        for (Operator operator : operators) {
            Operator definition = RuleMetadataConfigFactory.getOperatorConfig().getOperatorsMap()
                    .get(operator.getName());
            if (definition == null) {
                throw new IllegalArgumentException(
                        "Invalid operator: " + operator.getName() + " provided for field: " + field.getName());
            }
            configured.add(new Operator(operator.getName(), operator.getDisplayName(), definition.getValueInput()));
        }
        return Collections.unmodifiableList(configured);
    }

    /**
     * A field always needs a value definition. Besides describing the value a rule gives, it carries the field's
     * value type, which evaluation needs even for an operator that checks the field alone, such as isBlank.
     */
    private void validateValue() {

        if (value == null) {
            throw new IllegalArgumentException("Field " + field.getName() + " needs a value definition.");
        }
    }
}
