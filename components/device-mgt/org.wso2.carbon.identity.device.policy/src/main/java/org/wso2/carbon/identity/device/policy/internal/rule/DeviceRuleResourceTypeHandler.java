/*
 * Copyright (c) 2026, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.carbon.identity.device.policy.internal.rule;

import org.wso2.carbon.identity.device.policy.api.model.DeviceField;
import org.wso2.carbon.identity.device.policy.api.model.Platform;
import org.wso2.carbon.identity.policy.management.api.type.RuleResourceTypeHandler;
import org.wso2.carbon.identity.rule.management.api.model.FlowType;
import org.wso2.carbon.identity.rule.metadata.api.model.FieldDefinition;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Handles the rule resources of device policies. Device policy rules use the device policy rule flow, and each
 * rule can only use the device fields that apply to the platform it targets.
 */
public class DeviceRuleResourceTypeHandler implements RuleResourceTypeHandler {

    @Override
    public FlowType getRuleFlowType() {

        return FlowType.DEVICE_POLICY;
    }

    @Override
    public List<FieldDefinition> filterMetadata(String target, List<FieldDefinition> fieldDefinitions) {

        Platform platform = Platform.fromValue(target);
        if (platform == null) {
            return Collections.emptyList();
        }
        List<String> platformFields = DeviceField.getFieldNamesForPlatform(platform);
        return fieldDefinitions.stream()
                .filter(fieldDefinition -> platformFields.contains(fieldDefinition.getField().getName()))
                .collect(Collectors.toList());
    }
}
