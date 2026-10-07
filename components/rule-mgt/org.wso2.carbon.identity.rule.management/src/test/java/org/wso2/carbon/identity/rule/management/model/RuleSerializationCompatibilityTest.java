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

package org.wso2.carbon.identity.rule.management.model;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.rule.management.api.model.ANDCombinedRule;
import org.wso2.carbon.identity.rule.management.api.model.Condition;
import org.wso2.carbon.identity.rule.management.api.model.Expression;
import org.wso2.carbon.identity.rule.management.api.model.FieldReference;
import org.wso2.carbon.identity.rule.management.api.model.ORCombinedRule;
import org.wso2.carbon.identity.rule.management.api.model.Value;

import java.util.Arrays;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertThrows;
import static org.testng.Assert.assertTrue;

/**
 * Tests that the stored JSON form of a rule stays readable as the model gains fields.
 * <p>
 * Rules are persisted as a JSON document, so every addition to these models is a compatibility
 * question in two directions. These tests pin literal JSON rather than round-tripping: a round trip
 * uses the same model on both sides and would pass no matter what the stored form looks like.
 */
public class RuleSerializationCompatibilityTest {

    /**
     * A rule exactly as written by a node that predates any of these additions. Nothing may change
     * this string -- its whole purpose is to be the shape already sitting in IDN_RULE.CONTENT.
     */
    private static final String STORED_RULE_JSON =
            "{\"condition\":\"OR\",\"rules\":[{\"condition\":\"AND\",\"expressions\":[" +
                    "{\"field\":\"application\",\"operator\":\"equals\"," +
                    "\"value\":{\"type\":\"REFERENCE\",\"value\":\"test-app-id\"}}]}]}";

    @Test
    public void testStoredRuleJsonStaysReadable() throws Exception {

        ORCombinedRule rule = new ObjectMapper().readValue(STORED_RULE_JSON, ORCombinedRule.class);

        assertNotNull(rule);
        assertEquals(rule.getCondition(), Condition.OR);
        assertEquals(rule.getRules().size(), 1);

        ANDCombinedRule andRule = rule.getRules().get(0);
        assertEquals(andRule.getCondition(), Condition.AND);
        assertEquals(andRule.getExpressions().size(), 1);

        Expression expression = andRule.getExpressions().get(0);
        assertEquals(expression.getField(), "application");
        assertEquals(expression.getOperator(), "equals");
        assertEquals(expression.getValue().getType(), Value.Type.REFERENCE);
        assertEquals(expression.getValue().getFieldValue(), "test-app-id");
    }

    /**
     * A rule written by a newer node carries properties this one has never heard of. It must read
     * what it understands and ignore the rest, rather than failing the whole document.
     */
    @Test
    public void testUnknownPropertyOnExpressionIsIgnored() throws Exception {

        String futureJson =
                "{\"condition\":\"OR\",\"rules\":[{\"condition\":\"AND\",\"expressions\":[" +
                        "{\"field\":\"user.claims\",\"fieldQualifier\":\"http://wso2.org/claims/country\"," +
                        "\"operator\":\"equals\"," +
                        "\"value\":{\"type\":\"STRING\",\"value\":\"LK\"}}]}]}";

        ORCombinedRule rule = new ObjectMapper().readValue(futureJson, ORCombinedRule.class);

        Expression expression = rule.getRules().get(0).getExpressions().get(0);
        assertEquals(expression.getField(), "user.claims");
        assertEquals(expression.getOperator(), "equals");
        assertEquals(expression.getValue().getFieldValue(), "LK");
    }

    @Test
    public void testUnknownPropertyOnRuleIsIgnored() throws Exception {

        String futureJson =
                "{\"condition\":\"OR\",\"unknownRuleProperty\":\"ignored\",\"rules\":[" +
                        "{\"condition\":\"AND\",\"unknownAndProperty\":1,\"expressions\":[" +
                        "{\"field\":\"application\",\"operator\":\"equals\"," +
                        "\"value\":{\"type\":\"STRING\",\"value\":\"x\",\"unknownValueProperty\":true}}]}]}";

        ORCombinedRule rule = new ObjectMapper().readValue(futureJson, ORCombinedRule.class);

        assertEquals(rule.getRules().get(0).getExpressions().get(0).getField(), "application");
    }

    /**
     * An unknown property and an unknown *enum constant* are different mechanisms, and only the
     * first is covered by the model annotations. A value type this node does not know must degrade
     * to null rather than throw -- which is why the read path configures the mapper as it does.
     */
    @Test
    public void testUnknownEnumConstantDegradesToNullWhenConfigured() throws Exception {

        String futureJson =
                "{\"condition\":\"OR\",\"rules\":[{\"condition\":\"AND\",\"expressions\":[" +
                        "{\"field\":\"application\",\"operator\":\"in\"," +
                        "\"value\":{\"type\":\"SOME_FUTURE_TYPE\",\"value\":\"x\"}}]}]}";

        ObjectMapper tolerantMapper = new ObjectMapper()
                .configure(DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_AS_NULL, true);

        ORCombinedRule rule = tolerantMapper.readValue(futureJson, ORCombinedRule.class);

        assertNull(rule.getRules().get(0).getExpressions().get(0).getValue().getType(),
                "An unrecognised value type should read as null so evaluation can fail closed.");
    }

    /**
     * The companion to the test above: without that configuration the same document throws. This is
     * what makes a new enum constant a one-way door for any node already deployed.
     */
    @Test
    public void testUnknownEnumConstantThrowsWithoutConfiguration() {

        String futureJson =
                "{\"condition\":\"OR\",\"rules\":[{\"condition\":\"AND\",\"expressions\":[" +
                        "{\"field\":\"application\",\"operator\":\"in\"," +
                        "\"value\":{\"type\":\"SOME_FUTURE_TYPE\",\"value\":\"x\"}}]}]}";

        assertThrows(Exception.class, () -> new ObjectMapper().readValue(futureJson, ORCombinedRule.class));
    }

    /**
     * Absent must serialise as absent. With Jackson's default inclusion a null property is written
     * out explicitly, which would put an unknown property into every rule every other component
     * owns -- turning a narrow incompatibility into a total one.
     */
    @Test
    public void testNullPropertiesAreOmittedFromSerialisedForm() throws Exception {

        ORCombinedRule rule = new ORCombinedRule.Builder()
                .addRule(new ANDCombinedRule.Builder()
                        .addExpression(new Expression.Builder()
                                .field("application")
                                .operator("equals")
                                .value(new Value(Value.Type.STRING, null))
                                .build())
                        .build())
                .build();

        String json = new ObjectMapper().writeValueAsString(rule);

        assertFalse(json.contains("null"),
                "Serialised rule must not carry explicit nulls, but was: " + json);
    }
    /**
     * A value stored before FIELD existed carries no reference, and reads exactly as it did.
     */
    @Test
    public void testStoredValueReadsWithoutAFieldReference() throws Exception {

        ORCombinedRule rule = new ObjectMapper().readValue(STORED_RULE_JSON, ORCombinedRule.class);

        assertNull(rule.getRules().get(0).getExpressions().get(0).getValue().getFieldReference());
    }

    /**
     * A LIST value as stored for in and notIn keeps reading after FIELD was added.
     */
    @Test
    public void testStoredListValueStaysReadable() throws Exception {

        String storedJson =
                "{\"condition\":\"OR\",\"rules\":[{\"condition\":\"AND\",\"expressions\":[" +
                        "{\"field\":\"user.claims\",\"fieldQualifier\":\"http://wso2.org/claims/country\"," +
                        "\"operator\":\"in\",\"value\":{\"type\":\"LIST\",\"values\":[\"LK\",\"IN\"]}}]}]}";

        Value value = new ObjectMapper().readValue(storedJson, ORCombinedRule.class)
                .getRules().get(0).getExpressions().get(0).getValue();

        assertEquals(value.getType(), Value.Type.LIST);
        assertEquals(value.getFieldValues().size(), 2);
        assertNull(value.getFieldReference());
    }

    /**
     * The stored form of a FIELD value, pinned: the type, and the field it reads.
     */
    @Test
    public void testFieldValueStoredForm() throws Exception {

        String fieldJson =
                "{\"condition\":\"OR\",\"rules\":[{\"condition\":\"AND\",\"expressions\":[" +
                        "{\"field\":\"user.collectedClaims\",\"fieldQualifier\":\"http://wso2.org/claims/country\"," +
                        "\"operator\":\"notEquals\",\"value\":{\"type\":\"FIELD\",\"field\":" +
                        "{\"name\":\"user.claims\",\"qualifier\":\"http://wso2.org/claims/country\"}}}]}]}";

        ORCombinedRule rule = new ObjectMapper().readValue(fieldJson, ORCombinedRule.class);
        Value value = rule.getRules().get(0).getExpressions().get(0).getValue();

        assertEquals(value.getType(), Value.Type.FIELD);
        assertEquals(value.getFieldReference().getName(), "user.claims");
        assertEquals(value.getFieldReference().getQualifier(), "http://wso2.org/claims/country");
        assertNull(value.getFieldValue());
        assertEquals(new ObjectMapper().writeValueAsString(rule), fieldJson);
    }

    @Test
    public void testFieldValueBuiltInCodeOmitsTheOtherMembers() throws Exception {

        String json = new ObjectMapper().writeValueAsString(
                new Value(new FieldReference("user.claims", "http://wso2.org/claims/country")));

        assertTrue(json.contains("\"type\":\"FIELD\""), json);
        assertFalse(json.contains("\"value\""), json);
        assertFalse(json.contains("\"values\""), json);
    }

    /**
     * A LIST or FIELD value carries only what its type reads, and a scalar carries neither of their members.
     */
    @Test
    public void testValueShapeIsCheckedForListAndField() {

        FieldReference reference = new FieldReference("user.claims", "http://wso2.org/claims/country");

        assertThrows(IllegalArgumentException.class, () -> new Value(Value.Type.LIST, null, null, null));
        assertThrows(IllegalArgumentException.class,
                () -> new Value(Value.Type.LIST, "LK", Arrays.asList("LK"), null));
        assertThrows(IllegalArgumentException.class, () -> new Value(Value.Type.FIELD, null, null, null));
        assertThrows(IllegalArgumentException.class, () -> new Value(Value.Type.FIELD, "LK", null, reference));
        assertThrows(IllegalArgumentException.class,
                () -> new Value(Value.Type.STRING, "LK", Arrays.asList("LK"), null));
        assertThrows(IllegalArgumentException.class, () -> new Value(Value.Type.STRING, "LK", null, reference));
    }
}
