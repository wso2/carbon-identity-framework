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

package org.wso2.carbon.identity.policy.management.type;

import org.testng.annotations.Test;
import org.wso2.carbon.identity.policy.management.api.model.PolicyResource.ResourceType;
import org.wso2.carbon.identity.policy.management.api.type.PolicyResourceTypeHandler;
import org.wso2.carbon.identity.policy.management.api.type.PolicyTypeDefinition;
import org.wso2.carbon.identity.policy.management.api.type.RuleResourceTypeHandler;
import org.wso2.carbon.identity.rule.management.api.model.FlowType;
import org.wso2.carbon.identity.rule.metadata.api.model.FieldDefinition;

import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.mock;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertSame;
import static org.testng.Assert.assertTrue;

/**
 * Tests for {@link PolicyTypeDefinition}.
 */
public class PolicyTypeDefinitionTest {

    private static final RuleResourceTypeHandler RULE_HANDLER = () -> FlowType.DEVICE_POLICY;

    @Test
    public void testSupportedResourceTypesComeFromHandlers() {

        PolicyTypeDefinition definition =
                new PolicyTypeDefinition("DEVICE", Collections.singleton("ios"), RULE_HANDLER) { };

        assertEquals(definition.getType(), "DEVICE");
        assertEquals(definition.getSupportedTargets(), Collections.singleton("ios"));
        assertEquals(definition.getSupportedResourceTypes(), Collections.singleton(ResourceType.RULE));
        assertSame(definition.getResourceTypeHandler(ResourceType.RULE), RULE_HANDLER);
    }

    @Test
    public void testTypeWithoutHandlersSupportsNoResourceTypes() {

        PolicyTypeDefinition definition = new PolicyTypeDefinition("OTHER", null) { };

        assertTrue(definition.getSupportedResourceTypes().isEmpty());
        assertTrue(definition.getSupportedTargets().isEmpty());
        assertNull(definition.getResourceTypeHandler(ResourceType.RULE));
    }

    @Test
    public void testRuleHandlerKeepsAllFieldsByDefault() {

        List<FieldDefinition> fieldDefinitions = Collections.singletonList(mock(FieldDefinition.class));

        assertSame(RULE_HANDLER.filterMetadata("ios", fieldDefinitions), fieldDefinitions);
        assertEquals(RULE_HANDLER.getResourceType(), ResourceType.RULE);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testBlankTypeRejected() {

        new PolicyTypeDefinition(" ", Collections.emptySet()) { };
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testDuplicateHandlersRejected() {

        new PolicyTypeDefinition("DEVICE", Collections.emptySet(), RULE_HANDLER, RULE_HANDLER) { };
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testRuleResourcesNeedARuleHandler() {

        PolicyResourceTypeHandler notARuleHandler = () -> ResourceType.RULE;
        new PolicyTypeDefinition("DEVICE", Collections.emptySet(), notARuleHandler) { };
    }
}
