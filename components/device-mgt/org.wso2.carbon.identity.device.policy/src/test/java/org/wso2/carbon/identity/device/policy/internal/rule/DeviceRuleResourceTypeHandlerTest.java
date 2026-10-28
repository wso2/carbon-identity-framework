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

import org.testng.annotations.Test;
import org.wso2.carbon.identity.rule.management.api.model.FlowType;
import org.wso2.carbon.identity.rule.metadata.api.model.Field;
import org.wso2.carbon.identity.rule.metadata.api.model.FieldDefinition;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

/**
 * Tests for {@link DeviceRuleResourceTypeHandler}.
 */
public class DeviceRuleResourceTypeHandlerTest {

    private final DeviceRuleResourceTypeHandler handler = new DeviceRuleResourceTypeHandler();

    private final List<FieldDefinition> fieldDefinitions = Arrays.asList(
            fieldDefinition("platform"), fieldDefinition("androidOsVersion"), fieldDefinition("jailbreak"),
            fieldDefinition("lockScreen"));

    @Test
    public void testUsesDevicePolicyRuleFlow() {

        assertEquals(handler.getRuleFlowType(), FlowType.DEVICE_POLICY);
    }

    @Test
    public void testKeepsFieldsOfTheTargetPlatform() {

        assertEquals(names(handler.filterMetadata("android", fieldDefinitions)),
                Arrays.asList("platform", "androidOsVersion", "lockScreen"));
        assertEquals(names(handler.filterMetadata("ios", fieldDefinitions)),
                Arrays.asList("platform", "jailbreak", "lockScreen"));
    }

    @Test
    public void testUnknownPlatformHasNoFields() {

        assertTrue(handler.filterMetadata("linux", fieldDefinitions).isEmpty());
    }

    private static FieldDefinition fieldDefinition(String name) {

        FieldDefinition fieldDefinition = mock(FieldDefinition.class);
        when(fieldDefinition.getField()).thenReturn(new Field(name, name));
        return fieldDefinition;
    }

    private static List<String> names(List<FieldDefinition> fieldDefinitions) {

        return fieldDefinitions.stream()
                .map(fieldDefinition -> fieldDefinition.getField().getName())
                .collect(Collectors.toList());
    }
}
