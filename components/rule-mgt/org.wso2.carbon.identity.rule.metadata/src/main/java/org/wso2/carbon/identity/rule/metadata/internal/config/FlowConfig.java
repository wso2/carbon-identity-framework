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
import org.wso2.carbon.identity.rule.metadata.api.exception.RuleMetadataConfigException;
import org.wso2.carbon.identity.rule.metadata.api.exception.RuleMetadataException;
import org.wso2.carbon.identity.rule.metadata.api.model.Field;
import org.wso2.carbon.identity.rule.metadata.api.model.FieldDefinition;
import org.wso2.carbon.identity.rule.metadata.api.model.FlowType;
import org.wso2.carbon.identity.rule.metadata.api.model.Link;
import org.wso2.carbon.identity.rule.metadata.api.model.OptionsInputValue;
import org.wso2.carbon.identity.rule.metadata.api.model.OptionsReferenceValue;
import org.wso2.carbon.identity.rule.metadata.api.model.OptionsValue;
import org.wso2.carbon.identity.rule.metadata.api.model.Value;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

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
            if (entry.containsKey("overrides")) {
                fieldDefinitionList.add(applyOverrides(mapper, entry, fieldDefinition));
            } else {
                validateModifiableAttributesOfField(fieldDefinition);
                fieldDefinitionList.add(fieldDefinition);
            }
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

    private static FieldDefinition resolveFieldDefinition(FieldDefinitionConfig fieldDefinitionConfig,
                                                          Map<String, Object> entry)
            throws RuleMetadataConfigException {

        String fieldName = Optional.ofNullable(entry.get("fieldName"))
                .map(Object::toString)
                .orElseThrow(() -> new RuleMetadataConfigException("'fieldName' is required for a field"));

        return Optional.ofNullable(fieldDefinitionConfig.getFieldDefinitionMap().get(fieldName))
                .orElseThrow(() -> new RuleMetadataConfigException("Invalid field: " + fieldName));
    }

    private static FieldDefinition applyOverrides(ObjectMapper mapper, Map<String, Object> entry,
                                                  FieldDefinition fieldDefinition) throws RuleMetadataConfigException {

        Map<String, Object> overrides = mapper.convertValue(entry.get("overrides"),
                new TypeReference<Map<String, Object>>() { });
        Field updatedField = getUpdatedField(mapper, overrides, fieldDefinition);
        Value updatedValue = getUpdatedValue(mapper, overrides, fieldDefinition);

        return new FieldDefinition(updatedField, fieldDefinition.getOperators(), updatedValue);
    }

    private static Field getUpdatedField(ObjectMapper mapper, Map<?, ?> overrides, FieldDefinition fieldDefinition) {

        if (!overrides.containsKey("field")) {
            return fieldDefinition.getField();
        }

        Map<String, Object> overriddenAttributes = mapper.convertValue(overrides.get("field"),
                new TypeReference<Map<String, Object>>() { });
        if (overriddenAttributes.containsKey("name")) {
            throw new IllegalArgumentException("Field 'name' cannot be overridden");
        }

        return new Field(fieldDefinition.getField().getName(),
                Objects.requireNonNull(overriddenAttributes.get("displayName")).toString());
    }

    private static Value getUpdatedValue(ObjectMapper mapper, Map<?, ?> overrides, FieldDefinition fieldDefinition)
            throws RuleMetadataConfigException {

        if (!overrides.containsKey("value")) {
            return fieldDefinition.getValue();
        }

        Map<String, Object> overriddenAttributes = mapper.convertValue(overrides.get("value"),
                new TypeReference<Map<String, Object>>() { });

        Value overriddenValue = null;
        Value initialValue = fieldDefinition.getValue();
        if (initialValue instanceof OptionsInputValue) {
            List<OptionsValue> overriddenValues = mapper.convertValue(overriddenAttributes.remove("values"),
                    new TypeReference<List<OptionsValue>>() { });
            if (overriddenValues.isEmpty()) {
                throw new RuleMetadataConfigException("'values' is required to override an options input value");
            }

            overriddenValue = new OptionsInputValue(initialValue.getValueType(), overriddenValues);
        } else if (initialValue instanceof OptionsReferenceValue) {
            OptionsReferenceValue initialOptionsReferenceValue = (OptionsReferenceValue) initialValue;
            List<Link> overriddenLinks = mapper.convertValue(overriddenAttributes.remove("links"),
                    new TypeReference<List<Link>>() { });
            if (overriddenLinks.isEmpty()) {
                throw new RuleMetadataConfigException("'links' is required to override for options reference value");
            }

            overriddenValue = new OptionsReferenceValue.Builder()
                    .valueReferenceAttribute(initialOptionsReferenceValue.getValueReferenceAttribute())
                    .valueDisplayAttribute(initialOptionsReferenceValue.getValueDisplayAttribute())
                    .valueType(initialOptionsReferenceValue.getValueType())
                    .links(overriddenLinks)
                    .build();
        } else {
            throw new RuleMetadataConfigException("Unsupported value type for overrides");
        }

        if (!overriddenAttributes.isEmpty()) {
            throw new RuleMetadataConfigException("Following attributes are not allowed to override: "
                    + overriddenAttributes.keySet());
        }

        return overriddenValue;
    }

    private static void validateModifiableAttributesOfField(FieldDefinition fieldDefinition) {

        if (fieldDefinition.getField().getDisplayName() == null) {
            throw new IllegalArgumentException("Field 'displayName' cannot be null or empty.");
        }

        Value value = fieldDefinition.getValue();
        if (value instanceof OptionsInputValue) {
            List<OptionsValue> values = ((OptionsInputValue) value).getValues();
            if (values == null || values.isEmpty()) {
                throw new IllegalArgumentException("'values' cannot be null or empty for an options input value");
            }
        } else if (value instanceof OptionsReferenceValue) {
            List<Link> links = ((OptionsReferenceValue) value).getLinks();
            if (links == null || links.isEmpty()) {
                throw new IllegalArgumentException("'links' cannot be null or empty for an options reference value");
            }
        }
    }
}
