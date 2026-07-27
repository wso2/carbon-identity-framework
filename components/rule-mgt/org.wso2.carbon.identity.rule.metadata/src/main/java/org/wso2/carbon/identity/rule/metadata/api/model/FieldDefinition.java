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

import java.util.List;

/**
 * Represents the definition of a field in a rule.
 */
public class FieldDefinition {

    private final Field field;
    private final List<Operator> operators;
    private final Value value;

    public FieldDefinition(Field field, List<Operator> operators, Value value) {

        validateOperators(operators);
        this.field = field;
        this.operators = operators;
        this.value = value;
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

    private void validateOperators(List<Operator> operators) {

        if (operators == null || operators.isEmpty()) {
            throw new IllegalArgumentException("Operators cannot be null or empty.");
        }

        for (Operator operator : operators) {
            if (!RuleMetadataConfigFactory.getOperatorConfig().getOperatorsMap().containsKey(operator.getName())) {
                throw new IllegalArgumentException(
                        "Invalid operator: " + operator.getName() + " provided for field: " + field.getName());
            }
        }
    }
}
