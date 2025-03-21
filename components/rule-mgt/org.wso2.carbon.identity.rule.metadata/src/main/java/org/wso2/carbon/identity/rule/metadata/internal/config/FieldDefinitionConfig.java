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

package org.wso2.carbon.identity.rule.metadata.internal.config;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import org.wso2.carbon.identity.rule.metadata.api.exception.RuleMetadataConfigException;
import org.wso2.carbon.identity.rule.metadata.api.model.Field;
import org.wso2.carbon.identity.rule.metadata.api.model.FieldDefinition;
import org.wso2.carbon.identity.rule.metadata.api.model.InputValue;
import org.wso2.carbon.identity.rule.metadata.api.model.Operator;
import org.wso2.carbon.identity.rule.metadata.api.model.OptionsInputValue;
import org.wso2.carbon.identity.rule.metadata.api.model.OptionsReferenceValue;
import org.wso2.carbon.identity.rule.metadata.api.model.Value;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Class to load field definitions from a file.
 */
public class FieldDefinitionConfig {

    private final Map<String, FieldDefinition> fieldDefinitionMap;

    private FieldDefinitionConfig(Map<String, FieldDefinition> fieldDefinitionMap) {

        this.fieldDefinitionMap = fieldDefinitionMap;
    }

    public Map<String, FieldDefinition> getFieldDefinitionMap() {

        return Collections.unmodifiableMap(fieldDefinitionMap);
    }

    public static FieldDefinitionConfig load(File file, OperatorConfig operatorConfig)
            throws RuleMetadataConfigException {

        try {
            ObjectMapper mapper = new ObjectMapper();
            TypeFactory typeFactory = mapper.getTypeFactory();
            Map<String, Map<String, Object>> configMap =
                    mapper.readValue(file, typeFactory.constructMapType(
                            Map.class, String.class, Object.class));

            Map<String, FieldDefinition> fieldDefinitionMap = new HashMap<>();

            for (Map.Entry<String, Map<String, Object>> entry : configMap.entrySet()) {
                String key = entry.getKey();
                Map<String, Object> value = entry.getValue();

                Field field = mapper.convertValue(value.get("field"), Field.class);
                List<Operator> operators = loadOperators(value, operatorConfig, mapper);
                Value valueMeta = convertValueObject(value, mapper);

                FieldDefinition fieldDefinition = new FieldDefinition(field, operators, valueMeta);
                fieldDefinitionMap.put(key, fieldDefinition);
            }

            return new FieldDefinitionConfig(fieldDefinitionMap);
        } catch (Exception e) {
            throw new RuleMetadataConfigException(
                    "Error while loading field definitions from file: " + file.getAbsolutePath(), e);
        }
    }

    private static List<Operator> loadOperators(Map<String, Object> value, OperatorConfig operatorConfig,
                                                ObjectMapper mapper) {

        List<Operator> operators = new ArrayList<>();
        String[] operatorNames = mapper.convertValue(value.get("operators"), String[].class);
        for (String operatorName : operatorNames) {
            if (operatorConfig.getOperatorsMap().containsKey(operatorName)) {
                operators.add(operatorConfig.getOperatorsMap().get(operatorName));
            } else {
                throw new IllegalArgumentException("Invalid operator: " + operatorName + " provided");
            }
        }
        return operators;
    }

    private static Value convertValueObject(Map<String, Object> value, ObjectMapper mapper) {

        Map<String, Object> valueObject =
                mapper.convertValue(value.get("value"), new TypeReference<Map<String, Object>>() {
                });

        Value.InputType inputType = Value.InputType.valueOf(valueObject.get("inputType").toString().toUpperCase());
        Value.ValueType valueType = Value.ValueType.valueOf(valueObject.get("valueType").toString().toUpperCase());

        if (inputType == Value.InputType.OPTIONS && valueType == Value.ValueType.REFERENCE) {
            return mapper.convertValue(valueObject, OptionsReferenceValue.class);
        } else if (inputType == Value.InputType.OPTIONS) {
            return mapper.convertValue(valueObject, OptionsInputValue.class);
        } else if (inputType == Value.InputType.INPUT) {
            return mapper.convertValue(valueObject, InputValue.class);
        } else {
            throw new IllegalArgumentException("Unsupported inputType or valueType");
        }
    }
}
