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

import org.wso2.carbon.identity.rule.metadata.api.exception.RuleMetadataConfigException;
import org.wso2.carbon.utils.CarbonUtils;

import java.io.File;

/**
 * Factory class to load rule metadata configurations.
 * This class is used to load operator, field and flow configurations.
 */
public class RuleMetadataConfigFactory {

    private static final String OPERATORS_FILE_NAME = "operators.json";
    private static final String FIELDS_FILE_NAME = "fields.json";
    private static final String FLOWS_FILE_NAME = "flows.json";
    private static OperatorConfig operatorConfig;
    private static FieldDefinitionConfig fieldDefinitionConfig;
    private static FlowConfig flowConfig;

    private RuleMetadataConfigFactory() {

    }

    public static void load() throws RuleMetadataConfigException {

        String rulesMetadataDirectoryPath = getRulesMetadataDirectoryPath();

        operatorConfig = OperatorConfig.load(getFile(rulesMetadataDirectoryPath + OPERATORS_FILE_NAME));
        fieldDefinitionConfig =
                FieldDefinitionConfig.load(getFile(rulesMetadataDirectoryPath + FIELDS_FILE_NAME),
                        operatorConfig);
        flowConfig = FlowConfig.load(getFile(rulesMetadataDirectoryPath + FLOWS_FILE_NAME),
                fieldDefinitionConfig);
    }

    private static String getRulesMetadataDirectoryPath() {

        return CarbonUtils.getCarbonHome() + File.separator + "repository"
                + File.separator + "resources" + File.separator + "identity" + File.separator + "rulemeta" + File
                .separator;
    }

    public static OperatorConfig getOperatorConfig() {

        return operatorConfig;
    }

    public static FieldDefinitionConfig getFieldDefinitionConfig() {

        return fieldDefinitionConfig;
    }

    public static FlowConfig getFlowConfig() {

        return flowConfig;
    }

    private static File getFile(String fileName) throws RuleMetadataConfigException {

        File file = new File(fileName);
        if (!file.exists() || !file.isFile()) {
            throw new RuleMetadataConfigException("File not found at: " + file.getAbsolutePath());
        }

        return file;
    }
}
