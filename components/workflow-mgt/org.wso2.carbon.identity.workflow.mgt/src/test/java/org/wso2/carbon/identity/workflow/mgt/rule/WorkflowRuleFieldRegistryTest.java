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

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertThrows;
import static org.testng.Assert.assertTrue;

/**
 * Unit tests for {@link WorkflowRuleFieldRegistry}.
 */
@WithCarbonHome
public class WorkflowRuleFieldRegistryTest {

    @BeforeClass
    public void initRuleMetadata() throws RuleMetadataConfigException {

        RuleMetadataConfigFactory.load();
    }

    private static final List<String> EXPECTED_KEYS = Arrays.asList(
            "user.domain",
            "user.groups",
            "user.roles",
            "initiator.domain",
            "initiator.groups",
            "initiator.roles",
            "role.id",
            "role.audience",
            "role.permissions",
            "role.hasAssignedUsers",
            "role.hasUnassignedUsers"
    );

    @Test
    public void testFieldsMapIsNotNull() {

        assertNotNull(WorkflowRuleFieldRegistry.FIELDS, "FIELDS map should not be null.");
    }

    @Test
    public void testFieldsMapContainsAllExpectedKeys() {

        for (String key : EXPECTED_KEYS) {
            assertTrue(WorkflowRuleFieldRegistry.FIELDS.containsKey(key),
                    "FIELDS map should contain key: " + key);
        }
    }

    @Test
    public void testFieldsMapHasExactlyElevenEntries() {

        assertEquals(WorkflowRuleFieldRegistry.FIELDS.size(), 11,
                "FIELDS map should have exactly 11 entries.");
    }

    @Test
    public void testFieldsMapIsUnmodifiable() {

        assertThrows(UnsupportedOperationException.class, () ->
                WorkflowRuleFieldRegistry.FIELDS.put("new.field", null));
    }

    @Test
    public void testFieldsMapPreservesInsertionOrder() {

        Iterator<String> keyIterator = WorkflowRuleFieldRegistry.FIELDS.keySet().iterator();
        for (String expectedKey : EXPECTED_KEYS) {
            assertTrue(keyIterator.hasNext(), "Iterator should have more elements.");
            assertEquals(keyIterator.next(), expectedKey, "Keys should be in insertion order.");
        }
    }

    @Test
    public void testEachFieldNameMatchesMapKey() {

        for (Map.Entry<String, FieldDefinition> entry : WorkflowRuleFieldRegistry.FIELDS.entrySet()) {
            assertEquals(entry.getValue().getField().getName(), entry.getKey(),
                    "FieldDefinition name should match its map key for: " + entry.getKey());
        }
    }

    @Test
    public void testEachFieldHasNonBlankDisplayName() {

        for (Map.Entry<String, FieldDefinition> entry : WorkflowRuleFieldRegistry.FIELDS.entrySet()) {
            assertFalse(entry.getValue().getField().getDisplayName().isEmpty(),
                    "Display name should not be blank for: " + entry.getKey());
        }
    }

    @Test
    public void testEachFieldHasAtLeastOneOperator() {

        for (Map.Entry<String, FieldDefinition> entry : WorkflowRuleFieldRegistry.FIELDS.entrySet()) {
            assertFalse(entry.getValue().getOperators().isEmpty(),
                    "Operators should not be empty for: " + entry.getKey());
        }
    }

    @Test
    public void testEachFieldHasNonNullValue() {

        for (Map.Entry<String, FieldDefinition> entry : WorkflowRuleFieldRegistry.FIELDS.entrySet()) {
            assertNotNull(entry.getValue().getValue(),
                    "Value should not be null for: " + entry.getKey());
        }
    }

    @Test
    public void testUserDomainField_hasEqualsAndNotEqualsOperators() {

        FieldDefinition field = WorkflowRuleFieldRegistry.FIELDS.get("user.domain");
        assertNotNull(field);
        assertEquals(field.getOperators().size(), 2);
        assertEquals(field.getOperators().get(0).getName(), "equals");
        assertEquals(field.getOperators().get(1).getName(), "notEquals");
    }

    @Test
    public void testUserDomainField_isReferenceType() {

        FieldDefinition field = WorkflowRuleFieldRegistry.FIELDS.get("user.domain");
        assertNotNull(field);
        assertTrue(field.getValue() instanceof OptionsReferenceValue,
                "user.domain value should be OptionsReferenceValue.");
        OptionsReferenceValue refValue = (OptionsReferenceValue) field.getValue();
        assertEquals(refValue.getValueType(), Value.ValueType.REFERENCE);
        assertEquals(refValue.getValueReferenceAttribute(), "name");
        assertEquals(refValue.getValueDisplayAttribute(), "name");
        assertFalse(refValue.getLinks().isEmpty(), "user.domain should have at least one link.");
    }

    @Test
    public void testUserGroupsField_hasContainsOperator() {

        FieldDefinition field = WorkflowRuleFieldRegistry.FIELDS.get("user.groups");
        assertNotNull(field);
        assertEquals(field.getOperators().size(), 1);
        assertEquals(field.getOperators().get(0).getName(), "contains");
    }

    @Test
    public void testUserGroupsField_isReferenceTypeWithTwoLinks() {

        FieldDefinition field = WorkflowRuleFieldRegistry.FIELDS.get("user.groups");
        assertNotNull(field);
        assertTrue(field.getValue() instanceof OptionsReferenceValue);
        OptionsReferenceValue refValue = (OptionsReferenceValue) field.getValue();
        assertEquals(refValue.getValueReferenceAttribute(), "id");
        assertEquals(refValue.getValueDisplayAttribute(), "displayName");
        assertEquals(refValue.getLinks().size(), 2, "user.groups should have values and filter links.");
    }

    @Test
    public void testUserRolesField_isReferenceTypeWithTwoLinks() {

        FieldDefinition field = WorkflowRuleFieldRegistry.FIELDS.get("user.roles");
        assertNotNull(field);
        assertTrue(field.getValue() instanceof OptionsReferenceValue);
        OptionsReferenceValue refValue = (OptionsReferenceValue) field.getValue();
        assertEquals(refValue.getLinks().size(), 2);
    }

    @Test
    public void testInitiatorFieldsMirrorUserFields() {

        // Initiator fields should mirror the user fields structure.
        FieldDefinition userDomain = WorkflowRuleFieldRegistry.FIELDS.get("user.domain");
        FieldDefinition initiatorDomain = WorkflowRuleFieldRegistry.FIELDS.get("initiator.domain");
        assertNotNull(initiatorDomain);
        assertEquals(initiatorDomain.getOperators().size(), userDomain.getOperators().size());
        assertEquals(initiatorDomain.getValue().getValueType(), userDomain.getValue().getValueType());

        FieldDefinition userGroups = WorkflowRuleFieldRegistry.FIELDS.get("user.groups");
        FieldDefinition initiatorGroups = WorkflowRuleFieldRegistry.FIELDS.get("initiator.groups");
        assertNotNull(initiatorGroups);
        assertEquals(initiatorGroups.getOperators().size(), userGroups.getOperators().size());
    }

    @Test
    public void testRolePermissionsField_isInputTypeString() {

        FieldDefinition field = WorkflowRuleFieldRegistry.FIELDS.get("role.permissions");
        assertNotNull(field);
        assertTrue(field.getValue() instanceof InputValue,
                "role.permissions value should be InputValue.");
        assertEquals(field.getValue().getValueType(), Value.ValueType.STRING);
        assertEquals(field.getValue().getInputType(), Value.InputType.INPUT);
        assertEquals(field.getOperators().size(), 1);
        assertEquals(field.getOperators().get(0).getName(), "contains");
    }

    @Test
    public void testRoleHasAssignedUsersField_hasFixedTrueFalseOptions() {

        FieldDefinition field = WorkflowRuleFieldRegistry.FIELDS.get("role.hasAssignedUsers");
        assertNotNull(field);
        assertTrue(field.getValue() instanceof OptionsInputValue,
                "role.hasAssignedUsers value should be OptionsInputValue.");
        OptionsInputValue inputValue = (OptionsInputValue) field.getValue();
        assertEquals(inputValue.getValueType(), Value.ValueType.STRING);
        assertEquals(inputValue.getValues().size(), 2);
        assertEquals(inputValue.getValues().get(0).getName(), "true");
        assertEquals(inputValue.getValues().get(1).getName(), "false");
        assertEquals(field.getOperators().size(), 1);
        assertEquals(field.getOperators().get(0).getName(), "equals");
    }

    @Test
    public void testRoleHasUnassignedUsersField_hasFixedTrueFalseOptions() {

        FieldDefinition field = WorkflowRuleFieldRegistry.FIELDS.get("role.hasUnassignedUsers");
        assertNotNull(field);
        assertTrue(field.getValue() instanceof OptionsInputValue);
        OptionsInputValue inputValue = (OptionsInputValue) field.getValue();
        assertEquals(inputValue.getValues().size(), 2);
        assertEquals(inputValue.getValues().get(0).getName(), "true");
        assertEquals(inputValue.getValues().get(1).getName(), "false");
    }

    @Test
    public void testRoleIdField_isReferenceTypeWithEqualsAndNotEquals() {

        FieldDefinition field = WorkflowRuleFieldRegistry.FIELDS.get("role.id");
        assertNotNull(field);
        assertEquals(field.getOperators().size(), 2);
        assertEquals(field.getOperators().get(0).getName(), "equals");
        assertEquals(field.getOperators().get(1).getName(), "notEquals");
        assertTrue(field.getValue() instanceof OptionsReferenceValue);
        OptionsReferenceValue refValue = (OptionsReferenceValue) field.getValue();
        assertEquals(refValue.getValueReferenceAttribute(), "id");
        assertEquals(refValue.getValueDisplayAttribute(), "displayName");
    }

    @Test
    public void testRoleAudienceField_isReferenceTypeWithApplicationLinks() {

        FieldDefinition field = WorkflowRuleFieldRegistry.FIELDS.get("role.audience");
        assertNotNull(field);
        assertTrue(field.getValue() instanceof OptionsReferenceValue);
        OptionsReferenceValue refValue = (OptionsReferenceValue) field.getValue();
        assertEquals(refValue.getValueReferenceAttribute(), "id");
        assertEquals(refValue.getValueDisplayAttribute(), "name");
        assertEquals(refValue.getLinks().size(), 2);
    }
}
