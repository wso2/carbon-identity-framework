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
import org.wso2.carbon.identity.rule.metadata.api.exception.RuleMetadataConfigException;
import org.wso2.carbon.identity.rule.metadata.api.model.Operator;

import java.io.File;
import java.util.Collections;
import java.util.Map;

/**
 * Class to load operators from a file.
 */
public class OperatorConfig {

    private final Map<String, Operator> operatorsMap;

    private OperatorConfig(Map<String, Operator> operatorsMap) {

        this.operatorsMap = operatorsMap;
    }

    public Map<String, Operator> getOperatorsMap() {

        return Collections.unmodifiableMap(operatorsMap);
    }

    public static OperatorConfig load(File file) throws RuleMetadataConfigException {

        try {
            ObjectMapper mapper = new ObjectMapper();
            Map<String, Operator> operators = mapper.readValue(
                    file,
                    mapper.getTypeFactory().constructMapType(Map.class, String.class, Operator.class)
                                                              );
            return new OperatorConfig(operators);
        } catch (Exception e) {
            throw new RuleMetadataConfigException("Error while loading operators from file: " + file.getAbsolutePath(),
                    e);
        }
    }
}
