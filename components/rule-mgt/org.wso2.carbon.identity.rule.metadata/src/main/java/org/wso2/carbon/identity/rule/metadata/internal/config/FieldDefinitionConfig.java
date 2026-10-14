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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import org.wso2.carbon.identity.rule.metadata.api.exception.RuleMetadataConfigException;
import org.wso2.carbon.identity.rule.metadata.api.model.Field;
import org.wso2.carbon.identity.rule.metadata.api.model.FieldDefinition;
import org.wso2.carbon.identity.rule.metadata.api.model.Operator;
import org.wso2.carbon.identity.rule.metadata.api.model.Value;
import org.wso2.carbon.identity.rule.metadata.api.model.ValueFieldOptions;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
                Value valueMeta = mapper.convertValue(value.get("value"), Value.class);
                ValueFieldOptions valueFieldOptions =
                        mapper.convertValue(value.get("valueFieldOptions"), ValueFieldOptions.class);

                FieldDefinition fieldDefinition = new FieldDefinition(field, operators, valueMeta, valueFieldOptions);
                fieldDefinitionMap.put(key, fieldDefinition);
            }
            validateValueFieldOptions(fieldDefinitionMap.values());

            return new FieldDefinitionConfig(fieldDefinitionMap);
        } catch (Exception e) {
            throw new RuleMetadataConfigException(
                    "Error while loading field definitions from file: " + file.getAbsolutePath(), e);
        }
    }

    /**
     * A field can only be compared with fields defined alongside it, so each field named in valueFieldOptions must
     * be defined in the same file.
     */
    private static void validateValueFieldOptions(Collection<FieldDefinition> fieldDefinitions) {

        Set<String> fieldNames = new HashSet<>();
        fieldDefinitions.forEach(fieldDefinition -> fieldNames.add(fieldDefinition.getField().getName()));
        for (FieldDefinition fieldDefinition : fieldDefinitions) {
            if (fieldDefinition.getValueFieldOptions() == null) {
                continue;
            }
            for (String name : fieldDefinition.getValueFieldOptions().getNames()) {
                if (!fieldNames.contains(name)) {
                    throw new IllegalArgumentException("Field " + fieldDefinition.getField().getName()
                            + " can be compared with field " + name + ", which is not defined.");
                }
            }
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

}
