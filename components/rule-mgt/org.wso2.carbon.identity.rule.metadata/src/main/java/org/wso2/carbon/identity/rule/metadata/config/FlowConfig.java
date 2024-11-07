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

import com.fasterxml.jackson.databind.ObjectMapper;
import org.wso2.carbon.identity.rule.metadata.exception.RuleMetadataConfigException;
import org.wso2.carbon.identity.rule.metadata.exception.RuleMetadataException;
import org.wso2.carbon.identity.rule.metadata.model.FieldDefinition;
import org.wso2.carbon.identity.rule.metadata.model.FlowType;

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
            Map<String, List<String>> fieldsDefinitionMapPerFlow = mapper.readValue(file,
                    mapper.getTypeFactory().constructMapType(Map.class, String.class, List.class));

            Map<String, List<FieldDefinition>> fieldDefinitionsMap = new HashMap<>();
            for (Map.Entry<String, List<String>> entry : fieldsDefinitionMapPerFlow.entrySet()) {
                validateFlow(entry.getKey());
                List<FieldDefinition> fieldDefinitionList =
                        loadFieldDefinitions(entry.getValue(), fieldDefinitionConfig);
                fieldDefinitionsMap.put(entry.getKey(), fieldDefinitionList);
            }
            return new FlowConfig(fieldDefinitionsMap);
        } catch (Exception e) {
            throw new RuleMetadataConfigException("Error while loading flows from file: " + file.getAbsolutePath(), e);
        }
    }

    private static List<FieldDefinition> loadFieldDefinitions(List<String> fieldNames,
                                                              FieldDefinitionConfig fieldDefinitionConfig)
            throws RuleMetadataConfigException {

        List<FieldDefinition> fieldDefinitionList = new ArrayList<>();
        for (String fieldName : fieldNames) {
            if (!fieldDefinitionConfig.getFieldDefinitionMap().containsKey(fieldName)) {
                throw new RuleMetadataConfigException("Invalid field: " + fieldName);
            }
            fieldDefinitionList.add(fieldDefinitionConfig.getFieldDefinitionMap().get(fieldName));
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
}

