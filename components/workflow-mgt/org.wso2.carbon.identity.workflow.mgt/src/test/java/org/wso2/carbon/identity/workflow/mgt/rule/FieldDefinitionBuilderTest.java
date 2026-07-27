/*
 * Copyright (c) 2026, WSO2 LLC. (https://www.wso2.com) All Rights Reserved.
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
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.identity.workflow.mgt.rule;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.common.testng.WithCarbonHome;
import org.wso2.carbon.identity.rule.metadata.api.exception.RuleMetadataConfigException;
import org.wso2.carbon.identity.rule.metadata.api.model.FieldDefinition;
import org.wso2.carbon.identity.rule.metadata.api.model.InputValue;
import org.wso2.carbon.identity.rule.metadata.api.model.OptionsInputValue;
import org.wso2.carbon.identity.rule.metadata.api.model.OptionsReferenceValue;
import org.wso2.carbon.identity.rule.metadata.api.model.Value;
import org.wso2.carbon.identity.rule.metadata.internal.config.RuleMetadataConfigFactory;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertThrows;
import static org.testng.Assert.assertTrue;

/**
 * Unit tests for {@link FieldDefinitionBuilder}.
 */
@WithCarbonHome
public class FieldDefinitionBuilderTest {

    @BeforeClass
    public void initRuleMetadata() throws RuleMetadataConfigException {

        RuleMetadataConfigFactory.load();
    }

    @Test
    public void testBuildInputField_string() {

        FieldDefinition fieldDef = new FieldDefinitionBuilder()
                .name("test.field")
                .displayName("Test Field")
                .operators("equals")
                .input()
                    .valueType(Value.ValueType.STRING)
                .build();

        assertNotNull(fieldDef);
        assertEquals(fieldDef.getField().getName(), "test.field");
        assertEquals(fieldDef.getField().getDisplayName(), "Test Field");
        assertEquals(fieldDef.getOperators().size(), 1);
        assertEquals(fieldDef.getOperators().get(0).getName(), "equals");
        assertTrue(fieldDef.getValue() instanceof InputValue);
        assertEquals(fieldDef.getValue().getValueType(), Value.ValueType.STRING);
        assertEquals(fieldDef.getValue().getInputType(), Value.InputType.INPUT);
    }

    @Test
    public void testBuildOptionsReferenceField() {

        FieldDefinition fieldDef = new FieldDefinitionBuilder()
                .name("ref.field")
                .displayName("Reference Field")
                .operators("equals", "notEquals")
                .options()
                    .valueType(Value.ValueType.REFERENCE)
                    .referenceAttr("id")
                    .displayAttr("displayName")
                    .addLink("/api/items?offset=0&limit=10", "GET", "values")
                    .addLink("/api/items?filter=name co &limit=10", "GET", "filter")
                .build();

        assertNotNull(fieldDef);
        assertTrue(fieldDef.getValue() instanceof OptionsReferenceValue);
        OptionsReferenceValue refValue = (OptionsReferenceValue) fieldDef.getValue();
        assertEquals(refValue.getValueType(), Value.ValueType.REFERENCE);
        assertEquals(refValue.getValueReferenceAttribute(), "id");
        assertEquals(refValue.getValueDisplayAttribute(), "displayName");
        assertEquals(refValue.getLinks().size(), 2);
        assertEquals(refValue.getLinks().get(0).getRel(), "values");
        assertEquals(refValue.getLinks().get(0).getMethod(), "GET");
        assertEquals(refValue.getLinks().get(1).getRel(), "filter");
    }

    @Test
    public void testBuildOptionsInputField_withFixedValues() {

        FieldDefinition fieldDef = new FieldDefinitionBuilder()
                .name("options.field")
                .displayName("Options Field")
                .operators("equals")
                .options()
                    .valueType(Value.ValueType.STRING)
                    .addFixedValue("option1", "Option One")
                    .addFixedValue("option2", "Option Two")
                    .addFixedValue("option3", "Option Three")
                .build();

        assertNotNull(fieldDef);
        assertTrue(fieldDef.getValue() instanceof OptionsInputValue);
        OptionsInputValue inputValue = (OptionsInputValue) fieldDef.getValue();
        assertEquals(inputValue.getValueType(), Value.ValueType.STRING);
        assertEquals(inputValue.getValues().size(), 3);
        assertEquals(inputValue.getValues().get(0).getName(), "option1");
        assertEquals(inputValue.getValues().get(0).getDisplayName(), "Option One");
        assertEquals(inputValue.getValues().get(1).getName(), "option2");
        assertEquals(inputValue.getValues().get(2).getName(), "option3");
    }

    @Test
    public void testMultipleOperators_allPresentInOrder() {

        FieldDefinition fieldDef = new FieldDefinitionBuilder()
                .name("multi.op.field")
                .displayName("Multi Operator Field")
                .operators("equals", "notEquals", "contains")
                .input()
                    .valueType(Value.ValueType.STRING)
                .build();

        assertEquals(fieldDef.getOperators().size(), 3);
        assertEquals(fieldDef.getOperators().get(0).getName(), "equals");
        assertEquals(fieldDef.getOperators().get(1).getName(), "notEquals");
        assertEquals(fieldDef.getOperators().get(2).getName(), "contains");
    }

    @Test
    public void testUnknownOperator_throwsIllegalArgumentException() {

        assertThrows(IllegalArgumentException.class, () ->
                new FieldDefinitionBuilder()
                        .name("field")
                        .displayName("Field")
                        .operators("unknownOperator")
                        .input()
                            .valueType(Value.ValueType.STRING)
                        .build());
    }

    @Test
    public void testMissingName_throwsIllegalStateException() {

        assertThrows(IllegalStateException.class, () ->
                new FieldDefinitionBuilder()
                        .displayName("Field")
                        .operators("equals")
                        .input()
                            .valueType(Value.ValueType.STRING)
                        .build());
    }

    @Test
    public void testMissingDisplayName_throwsIllegalStateException() {

        assertThrows(IllegalStateException.class, () ->
                new FieldDefinitionBuilder()
                        .name("field")
                        .operators("equals")
                        .input()
                            .valueType(Value.ValueType.STRING)
                        .build());
    }

    @Test
    public void testMissingOperators_throwsIllegalStateException() {

        assertThrows(IllegalStateException.class, () ->
                new FieldDefinitionBuilder()
                        .name("field")
                        .displayName("Field")
                        .input()
                            .valueType(Value.ValueType.STRING)
                        .build());
    }

    @Test
    public void testOptionsValueBuilder_missingValueType_throwsIllegalStateException() {

        assertThrows(IllegalStateException.class, () ->
                new FieldDefinitionBuilder()
                        .name("field")
                        .displayName("Field")
                        .operators("equals")
                        .options()
                        .build());
    }

    @Test
    public void testInputValueBuilder_missingValueType_throwsIllegalStateException() {

        assertThrows(IllegalStateException.class, () ->
                new FieldDefinitionBuilder()
                        .name("field")
                        .displayName("Field")
                        .operators("equals")
                        .input()
                        .build());
    }

    @Test
    public void testOptionsReferenceWithSingleLink() {

        FieldDefinition fieldDef = new FieldDefinitionBuilder()
                .name("single.link.field")
                .displayName("Single Link Field")
                .operators("equals")
                .options()
                    .valueType(Value.ValueType.REFERENCE)
                    .referenceAttr("name")
                    .displayAttr("name")
                    .addLink("/api/items", "GET", "values")
                .build();

        assertTrue(fieldDef.getValue() instanceof OptionsReferenceValue);
        OptionsReferenceValue refValue = (OptionsReferenceValue) fieldDef.getValue();
        assertEquals(refValue.getLinks().size(), 1);
        assertEquals(refValue.getLinks().get(0).getHref(), "/api/items");
        assertEquals(refValue.getLinks().get(0).getMethod(), "GET");
        assertEquals(refValue.getLinks().get(0).getRel(), "values");
    }

    @Test
    public void testContainsOperator_isValidOperator() {

        FieldDefinition fieldDef = new FieldDefinitionBuilder()
                .name("contains.field")
                .displayName("Contains Field")
                .operators("contains")
                .input()
                    .valueType(Value.ValueType.STRING)
                .build();

        assertEquals(fieldDef.getOperators().size(), 1);
        assertEquals(fieldDef.getOperators().get(0).getName(), "contains");
        assertEquals(fieldDef.getOperators().get(0).getDisplayName(), "contains");
    }
}
