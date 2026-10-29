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

package org.wso2.carbon.identity.device.policy.internal.type;

import org.wso2.carbon.identity.device.policy.api.model.Platform;
import org.wso2.carbon.identity.device.policy.internal.rule.DeviceRuleResourceTypeHandler;
import org.wso2.carbon.identity.policy.management.api.type.PolicyTypeDefinition;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Registers device assurance policies with policy management. Each resource of a device policy is a rule that
 * targets a device platform.
 */
public class DevicePolicyTypeDefinition extends PolicyTypeDefinition {

    public static final String TYPE = "DEVICE";

    private static final Set<String> SUPPORTED_TARGETS =
            Arrays.stream(Platform.values()).map(Platform::getValue).collect(Collectors.toSet());

    public DevicePolicyTypeDefinition() {

        super(TYPE, SUPPORTED_TARGETS, new DeviceRuleResourceTypeHandler());
    }
}
