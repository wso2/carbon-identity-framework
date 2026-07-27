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

package org.wso2.carbon.identity.rule.evaluation.internal.service.impl;

import org.wso2.carbon.identity.rule.evaluation.api.exception.RuleEvaluationException;
import org.wso2.carbon.identity.rule.evaluation.api.model.Field;
import org.wso2.carbon.identity.rule.evaluation.api.model.ValueType;
import org.wso2.carbon.identity.rule.management.api.model.Expression;
import org.wso2.carbon.identity.rule.management.api.model.Rule;
import org.wso2.carbon.identity.rule.metadata.api.model.FieldDefinition;
import org.wso2.carbon.identity.rule.metadata.api.model.OptionsReferenceValue;
import org.wso2.carbon.identity.rule.metadata.api.model.Value;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Extracts fields from a given rule.
 */
public class FieldExtractor {

    private final Map<String, FieldDefinition> expressionMetadataFieldsMap;

    public FieldExtractor(List<FieldDefinition> expressionMetadataFields) {

        this.expressionMetadataFieldsMap = expressionMetadataFields.stream()
                .collect(Collectors.toMap(fieldDefinition -> fieldDefinition.getField().getName(),
                        fieldDefinition -> fieldDefinition));
    }

    /**
     * This method extracts the unique fields used within expressions in the rule.
     * A given flow can contain only unique fields.
     *
     * @param rule Rule to extract fields from.
     * @return List of fields used in the rule.
     * @throws RuleEvaluationException If field definition not found for a field.
     */
    public List<Field> extractFields(Rule rule) throws RuleEvaluationException {

        List<Field> fieldList = new ArrayList<>();
        Set<String> extractedFieldName = new HashSet<>();

        for (Expression expression : rule.getExpressions()) {
            String fieldName = expression.getField();
            if (extractedFieldName.add(fieldName)) {
                FieldDefinition fieldDefinition = expressionMetadataFieldsMap.get(expression.getField());
                if (fieldDefinition == null) {
                    throw new RuleEvaluationException(
                            "Field definition not found for the field: " + expression.getField());
                }

                Field field = new Field(expression.getField(), resolveValueType(fieldDefinition.getValue()));
                fieldList.add(field);
            }
        }

        return fieldList;
    }

    private ValueType resolveValueType(Value fieldDefinitionValue) {

        Value.ValueType fieldDefinitionValueType = fieldDefinitionValue.getValueType();
        switch (fieldDefinitionValueType) {
            case STRING:
                return ValueType.STRING;
            case NUMBER:
                return ValueType.NUMBER;
            case BOOLEAN:
                return ValueType.BOOLEAN;
            case REFERENCE:
                OptionsReferenceValue referenceValue = (OptionsReferenceValue) fieldDefinitionValue;
                return ValueType.createReferenceType(referenceValue.getValueReferenceAttribute());
            default:
                throw new IllegalArgumentException("Unsupported value type: " + fieldDefinitionValueType);
        }
    }
}
