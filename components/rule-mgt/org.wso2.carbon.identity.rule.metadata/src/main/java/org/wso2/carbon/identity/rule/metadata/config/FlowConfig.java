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

package org.wso2.carbon.identity.rule.metadata.config;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.wso2.carbon.identity.rule.metadata.exception.RuleMetadataConfigException;
import org.wso2.carbon.identity.rule.metadata.exception.RuleMetadataException;
import org.wso2.carbon.identity.rule.metadata.model.Field;
import org.wso2.carbon.identity.rule.metadata.model.FieldDefinition;
import org.wso2.carbon.identity.rule.metadata.model.FlowType;
import org.wso2.carbon.identity.rule.metadata.model.OptionsInputValue;
import org.wso2.carbon.identity.rule.metadata.model.OptionsReferenceValue;
import org.wso2.carbon.identity.rule.metadata.model.Value;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Class to load field definitions for a flow from a file.
 */
public class FlowConfig {

    private final Map<String, List<FieldDefinition>> fieldDefinitionsMap;

    private FlowConfig(Map<String, List<FieldDefinition>> fieldDefinitionsMap) {

        this.fieldDefinitionsMap = fieldDefinitionsMap;
    }

    public List<FieldDefinition> getFieldDefinitionsForFlow(FlowType flowType) {

        return fieldDefinitionsMap.get(flowType.getFlowAlias());
    }

    public static FlowConfig load(File file, FieldDefinitionConfig fieldDefinitionConfig)
            throws RuleMetadataConfigException {

        try {
            ObjectMapper mapper = new ObjectMapper();
            Map<String, List<Map<String, Object>>> fieldsDefinitionMapPerFlow = mapper.readValue(file,
                    mapper.getTypeFactory().constructMapType(Map.class, String.class, List.class));

            Map<String, List<FieldDefinition>> fieldDefinitionsMap = new HashMap<>();
            for (Map.Entry<String, List<Map<String, Object>>> entry : fieldsDefinitionMapPerFlow.entrySet()) {
                validateFlow(entry.getKey());
                List<FieldDefinition> fieldDefinitionList = loadFieldDefinitions(mapper, entry.getValue(),
                        fieldDefinitionConfig);
                fieldDefinitionsMap.put(entry.getKey(), fieldDefinitionList);
            }
            return new FlowConfig(fieldDefinitionsMap);
        } catch (Exception e) {
            throw new RuleMetadataConfigException("Error while loading flows from file: " + file.getAbsolutePath(), e);
        }
    }

    private static List<FieldDefinition> loadFieldDefinitions(ObjectMapper mapper,
                                                              List<Map<String, Object>> fieldDefinitions,
                                                              FieldDefinitionConfig fieldDefinitionConfig)
            throws RuleMetadataConfigException {

        List<FieldDefinition> fieldDefinitionList = new ArrayList<>();
        for (Map<String, Object> entry : fieldDefinitions) {
            FieldDefinition fieldDefinition = resolveFieldDefinition(fieldDefinitionConfig, entry);
            if (!entry.containsKey("overrides")) {
                fieldDefinitionList.add(fieldDefinition);
                continue;
            }

            Map<String, Object> overrides = mapper.convertValue(entry.get("overrides"),
                    new TypeReference<Map<String, Object>>() { });
            Field updatedField = getUpdatedField(mapper, overrides, fieldDefinition);
            Value updatedValue = getUpdatedValue(mapper, overrides, fieldDefinition);
            fieldDefinitionList.add(new FieldDefinition(updatedField, fieldDefinition.getOperators(),
                    updatedValue));
        }
        return fieldDefinitionList;
    }

    private static void validateFlow(String flowName) throws RuleMetadataConfigException {

        try {
            FlowType.valueOfFlowAlias(flowName);
        } catch (RuleMetadataException e) {
            throw new RuleMetadataConfigException("Invalid flow: " + flowName);
        }
    }

    /**
     * Resolve the field definition from the field definition config.
     *
     * @param fieldDefinitionConfig FieldDefinitionConfig
     * @param entry                 Entry
     * @return FieldDefinition
     * @throws RuleMetadataConfigException If an error occurs while resolving the field definition
     */
    private static FieldDefinition resolveFieldDefinition(FieldDefinitionConfig fieldDefinitionConfig,
                                                          Map<String, Object> entry)
            throws RuleMetadataConfigException {

        if (!entry.containsKey("fieldName")) {
            throw new RuleMetadataConfigException("'fieldName' is required for a field");
        }

        String fieldName = entry.get("fieldName").toString();
        if (!fieldDefinitionConfig.getFieldDefinitionMap().containsKey(fieldName)) {
            throw new RuleMetadataConfigException("Invalid field: " + fieldName);
        }

        return fieldDefinitionConfig.getFieldDefinitionMap().get(fieldName);
    }

    /**
     * Get the updated field if the field is overridden.
     *
     * @param mapper           ObjectMapper
     * @param overrides        Overrides
     * @param fieldDefinition  FieldDefinition
     * @return Updated field
     */
    private static Field getUpdatedField(ObjectMapper mapper, Map<?, ?> overrides, FieldDefinition fieldDefinition) {

        if (!overrides.containsKey("field")) {
            return fieldDefinition.getField();
        }

        return mapper.convertValue(overrides.get("field"), Field.class);
    }

    /**
     * Get the updated value if the value is overridden.
     *
     * @param mapper           ObjectMapper
     * @param overrides        Overrides
     * @param fieldDefinition  FieldDefinition
     * @return Updated value
     */
    private static Value getUpdatedValue(ObjectMapper mapper, Map<?, ?> overrides, FieldDefinition fieldDefinition) {

        if (!overrides.containsKey("value")) {
            return fieldDefinition.getValue();
        }

        Value initialValue = fieldDefinition.getValue();
        if (initialValue instanceof OptionsInputValue) {
            OptionsInputValue overriddenOptionsInputValue = mapper.convertValue(overrides.get("value"),
                    OptionsInputValue.class);
            return new OptionsInputValue(initialValue.getValueType(), overriddenOptionsInputValue.getValues());
        }

        if (initialValue instanceof OptionsReferenceValue) {
            OptionsReferenceValue initialOptionsReferenceValue = (OptionsReferenceValue) initialValue;
            OptionsReferenceValue overriddenOptionsReferenceValue = mapper.convertValue(overrides.get("value"),
                    OptionsReferenceValue.class);
            return new OptionsReferenceValue.Builder()
                    .valueReferenceAttribute(initialOptionsReferenceValue.getValueReferenceAttribute())
                    .valueDisplayAttribute(initialOptionsReferenceValue.getValueDisplayAttribute())
                    .valueType(initialOptionsReferenceValue.getValueType())
                    .links(overriddenOptionsReferenceValue.getLinks())
                    .build();
        }

        throw new IllegalArgumentException("Unsupported value type for overrides");
    }
}
