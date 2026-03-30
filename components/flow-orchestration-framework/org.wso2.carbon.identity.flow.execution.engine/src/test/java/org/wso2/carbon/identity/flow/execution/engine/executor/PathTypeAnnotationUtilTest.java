/*
 * Copyright (c) 2025, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.carbon.identity.flow.execution.engine.executor;

import org.testng.annotations.Test;
import org.wso2.carbon.identity.flow.execution.engine.inflow.extension.executor.PathTypeAnnotationUtil;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

/**
 * Unit tests for {@link PathTypeAnnotationUtil}.
 */
public class PathTypeAnnotationUtilTest {

    // ========================= stripAnnotation =========================

    @Test
    public void testStripAnnotationPrimaryType() {

        String[] result = PathTypeAnnotationUtil.stripAnnotation("/properties/riskFactor{String}");
        assertEquals(result[0], "/properties/riskFactor");
        assertEquals(result[1], "String");
    }

    @Test
    public void testStripAnnotationMultivaluedPrimary() {

        String[] result = PathTypeAnnotationUtil.stripAnnotation("/properties/riskFactors{[String]}");
        assertEquals(result[0], "/properties/riskFactors");
        assertEquals(result[1], "[String]");
    }

    @Test
    public void testStripAnnotationComplexObject() {

        String[] result = PathTypeAnnotationUtil.stripAnnotation(
                "/properties/risk{risk: Float, factor: String}");
        assertEquals(result[0], "/properties/risk");
        assertEquals(result[1], "risk: Float, factor: String");
    }

    @Test
    public void testStripAnnotationMultivaluedComplex() {

        String[] result = PathTypeAnnotationUtil.stripAnnotation(
                "/properties/risks{[risk: Float, factor: String]}");
        assertEquals(result[0], "/properties/risks");
        assertEquals(result[1], "[risk: Float, factor: String]");
    }

    @Test
    public void testStripAnnotationNoAnnotation() {

        String[] result = PathTypeAnnotationUtil.stripAnnotation("/properties/riskScore");
        assertEquals(result[0], "/properties/riskScore");
        assertNull(result[1]);
    }

    @Test
    public void testStripAnnotationNullInput() {

        String[] result = PathTypeAnnotationUtil.stripAnnotation(null);
        assertNull(result[0]);
        assertNull(result[1]);
    }

    @Test
    public void testStripAnnotationEmptyBraces() {

        String[] result = PathTypeAnnotationUtil.stripAnnotation("/properties/field{}");
        assertEquals(result[0], "/properties/field");
        assertEquals(result[1], "");
    }

    @Test
    public void testStripAnnotationIntegerType() {

        String[] result = PathTypeAnnotationUtil.stripAnnotation("/properties/count{Integer}");
        assertEquals(result[0], "/properties/count");
        assertEquals(result[1], "Integer");
    }

    // ========================= coerceValue =========================

    @Test
    public void testCoerceValueNoAnnotation() {

        Object result = PathTypeAnnotationUtil.coerceValue(
                "/properties/score", 42, Collections.emptyMap());
        assertEquals(result, "42");
    }

    @Test
    public void testCoerceValuePrimaryTypeAnnotation() {

        Map<String, String> annotations = new HashMap<>();
        annotations.put("/properties/score", "Integer");

        Object result = PathTypeAnnotationUtil.coerceValue("/properties/score", 95, annotations);
        assertEquals(result, "95");
    }

    @Test
    public void testCoerceValueMultivaluedPrimaryList() {

        Map<String, String> annotations = new HashMap<>();
        annotations.put("/properties/tags", "[String]");

        List<String> input = Arrays.asList("tag1", "tag2", "tag3");
        Object result = PathTypeAnnotationUtil.coerceValue("/properties/tags", input, annotations);

        assertTrue(result instanceof List);
        @SuppressWarnings("unchecked")
        List<String> list = (List<String>) result;
        assertEquals(list.size(), 3);
        assertEquals(list.get(0), "tag1");
    }

    @Test
    public void testCoerceValueMultivaluedPrimarySingleValue() {

        Map<String, String> annotations = new HashMap<>();
        annotations.put("/properties/tags", "[String]");

        Object result = PathTypeAnnotationUtil.coerceValue("/properties/tags", "singleTag", annotations);

        assertTrue(result instanceof List);
        @SuppressWarnings("unchecked")
        List<String> list = (List<String>) result;
        assertEquals(list.size(), 1);
        assertEquals(list.get(0), "singleTag");
    }

    @Test
    public void testCoerceValueComplexObjectAnnotation() {

        Map<String, String> annotations = new HashMap<>();
        annotations.put("/properties/risk", "risk: Float, factor: String");

        Map<String, Object> complexValue = new HashMap<>();
        complexValue.put("risk", 0.85);
        complexValue.put("factor", "ip_mismatch");

        Object result = PathTypeAnnotationUtil.coerceValue("/properties/risk", complexValue, annotations);
        // Complex annotation — passed through as-is.
        assertTrue(result instanceof Map);
        assertEquals(result, complexValue);
    }

    @Test
    public void testCoerceValueComplexArrayAnnotation() {

        Map<String, String> annotations = new HashMap<>();
        annotations.put("/properties/risks", "[risk: Float, factor: String]");

        List<Map<String, Object>> items = Arrays.asList(
                new HashMap<String, Object>() {{ put("risk", 0.5); put("factor", "a"); }},
                new HashMap<String, Object>() {{ put("risk", 0.8); put("factor", "b"); }}
        );

        Object result = PathTypeAnnotationUtil.coerceValue("/properties/risks", items, annotations);
        // Complex array annotation — passed through as-is.
        assertTrue(result instanceof List);
        assertEquals(result, items);
    }

    @Test
    public void testCoerceValueBooleanPrimaryType() {

        Map<String, String> annotations = new HashMap<>();
        annotations.put("/properties/active", "Boolean");

        Object result = PathTypeAnnotationUtil.coerceValue("/properties/active", true, annotations);
        assertEquals(result, "true");
    }

    @Test
    public void testCoerceValueStringValue() {

        Object result = PathTypeAnnotationUtil.coerceValue(
                "/properties/name", "test", Collections.emptyMap());
        assertEquals(result, "test");
    }

    // ========================= validateAnnotationLimits =========================

    @Test
    public void testValidateAnnotationLimitsNull() {

        assertTrue(PathTypeAnnotationUtil.validateAnnotationLimits(null));
    }

    @Test
    public void testValidateAnnotationLimitsEmpty() {

        assertTrue(PathTypeAnnotationUtil.validateAnnotationLimits(""));
    }

    @Test
    public void testValidateAnnotationLimitsPrimaryType() {

        assertTrue(PathTypeAnnotationUtil.validateAnnotationLimits("String"));
    }

    @Test
    public void testValidateAnnotationLimitsPrimaryArray() {

        assertTrue(PathTypeAnnotationUtil.validateAnnotationLimits("[String]"));
    }

    @Test
    public void testValidateAnnotationLimitsComplexWithinLimit() {

        assertTrue(PathTypeAnnotationUtil.validateAnnotationLimits("risk: Float, factor: String"));
    }

    @Test
    public void testValidateAnnotationLimitsComplexArrayWithinLimit() {

        assertTrue(PathTypeAnnotationUtil.validateAnnotationLimits("[risk: Float, factor: String]"));
    }

    @Test
    public void testValidateAnnotationLimitsExceedsMax() {

        // Build an annotation with 11 attributes (exceeds MAX_ATTRIBUTES_PER_OBJECT = 10).
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 11; i++) {
            if (i > 0) sb.append(", ");
            sb.append("attr").append(i).append(": String");
        }
        assertFalse(PathTypeAnnotationUtil.validateAnnotationLimits(sb.toString()));
    }

    @Test
    public void testValidateAnnotationLimitsExactlyAtMax() {

        // Build an annotation with exactly 10 attributes.
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 10; i++) {
            if (i > 0) sb.append(", ");
            sb.append("attr").append(i).append(": String");
        }
        assertTrue(PathTypeAnnotationUtil.validateAnnotationLimits(sb.toString()));
    }

    // ========================= validateValueAgainstAnnotation =========================

    @Test
    public void testValidateValueNoAnnotation() {

        assertTrue(PathTypeAnnotationUtil.validateValueAgainstAnnotation(
                "/properties/score", 42, Collections.emptyMap()));
    }

    @Test
    public void testValidateValuePrimaryAnnotation() {

        Map<String, String> annotations = new HashMap<>();
        annotations.put("/properties/score", "Integer");
        assertTrue(PathTypeAnnotationUtil.validateValueAgainstAnnotation(
                "/properties/score", 42, annotations));
    }

    @Test
    public void testValidateValueComplexObjectValid() {

        Map<String, String> annotations = new HashMap<>();
        annotations.put("/properties/risk", "risk: Float, factor: String");

        Map<String, Object> value = new HashMap<>();
        value.put("risk", 0.85);
        value.put("factor", "ip_mismatch");

        assertTrue(PathTypeAnnotationUtil.validateValueAgainstAnnotation(
                "/properties/risk", value, annotations));
    }

    @Test
    public void testValidateValueComplexObjectUnknownAttribute() {

        Map<String, String> annotations = new HashMap<>();
        annotations.put("/properties/risk", "risk: Float, factor: String");

        Map<String, Object> value = new HashMap<>();
        value.put("risk", 0.85);
        value.put("unknown", "bad");

        assertFalse(PathTypeAnnotationUtil.validateValueAgainstAnnotation(
                "/properties/risk", value, annotations));
    }

    @Test
    public void testValidateValueComplexObjectNotAMap() {

        Map<String, String> annotations = new HashMap<>();
        annotations.put("/properties/risk", "risk: Float, factor: String");

        assertFalse(PathTypeAnnotationUtil.validateValueAgainstAnnotation(
                "/properties/risk", "not a map", annotations));
    }

    @Test
    public void testValidateValueComplexObjectNestedMap() {

        Map<String, String> annotations = new HashMap<>();
        annotations.put("/properties/risk", "risk: Float, factor: String");

        Map<String, Object> nested = new HashMap<>();
        nested.put("deep", "value");

        Map<String, Object> value = new HashMap<>();
        value.put("risk", 0.85);
        value.put("factor", nested);

        assertFalse(PathTypeAnnotationUtil.validateValueAgainstAnnotation(
                "/properties/risk", value, annotations));
    }

    @Test
    public void testValidateValueComplexArrayValid() {

        Map<String, String> annotations = new HashMap<>();
        annotations.put("/properties/risks", "[risk: Float, factor: String]");

        List<Map<String, Object>> items = Arrays.asList(
                new HashMap<String, Object>() {{ put("risk", 0.5); put("factor", "a"); }},
                new HashMap<String, Object>() {{ put("risk", 0.8); put("factor", "b"); }}
        );

        assertTrue(PathTypeAnnotationUtil.validateValueAgainstAnnotation(
                "/properties/risks", items, annotations));
    }

    @Test
    public void testValidateValueComplexArrayExceedsItemLimit() {

        Map<String, String> annotations = new HashMap<>();
        annotations.put("/properties/risks", "[risk: Float]");

        List<Map<String, Object>> items = new java.util.ArrayList<>();
        for (int i = 0; i < 11; i++) {
            Map<String, Object> item = new HashMap<>();
            item.put("risk", (float) i);
            items.add(item);
        }

        assertFalse(PathTypeAnnotationUtil.validateValueAgainstAnnotation(
                "/properties/risks", items, annotations));
    }

    @Test
    public void testValidateValueComplexObjectWithArrayAttribute() {

        Map<String, String> annotations = new HashMap<>();
        annotations.put("/properties/data", "tags: String[], score: Float");

        Map<String, Object> value = new HashMap<>();
        value.put("tags", Arrays.asList("a", "b", "c"));
        value.put("score", 0.9);

        assertTrue(PathTypeAnnotationUtil.validateValueAgainstAnnotation(
                "/properties/data", value, annotations));
    }

    @Test
    public void testValidateValueComplexObjectArrayAttrExceedsLimit() {

        Map<String, String> annotations = new HashMap<>();
        annotations.put("/properties/data", "tags: String[]");

        List<String> bigList = new java.util.ArrayList<>();
        for (int i = 0; i < 11; i++) {
            bigList.add("item" + i);
        }

        Map<String, Object> value = new HashMap<>();
        value.put("tags", bigList);

        assertFalse(PathTypeAnnotationUtil.validateValueAgainstAnnotation(
                "/properties/data", value, annotations));
    }

    // ========================= enforceArrayItemLimit =========================

    @Test
    public void testEnforceArrayItemLimitNoAnnotation() {

        assertTrue(PathTypeAnnotationUtil.enforceArrayItemLimit(
                "/properties/score", Arrays.asList(1, 2, 3), Collections.emptyMap()));
    }

    @Test
    public void testEnforceArrayItemLimitWithinLimit() {

        Map<String, String> annotations = new HashMap<>();
        annotations.put("/properties/tags", "[String]");

        assertTrue(PathTypeAnnotationUtil.enforceArrayItemLimit(
                "/properties/tags", Arrays.asList("a", "b", "c"), annotations));
    }

    @Test
    public void testEnforceArrayItemLimitExceedsLimit() {

        Map<String, String> annotations = new HashMap<>();
        annotations.put("/properties/tags", "[String]");

        List<String> bigList = new java.util.ArrayList<>();
        for (int i = 0; i < 11; i++) {
            bigList.add("item" + i);
        }

        assertFalse(PathTypeAnnotationUtil.enforceArrayItemLimit(
                "/properties/tags", bigList, annotations));
    }

    @Test
    public void testEnforceArrayItemLimitNonArrayAnnotation() {

        Map<String, String> annotations = new HashMap<>();
        annotations.put("/properties/score", "Integer");

        assertTrue(PathTypeAnnotationUtil.enforceArrayItemLimit(
                "/properties/score", 42, annotations));
    }

    @Test
    public void testEnforceArrayItemLimitNonListValue() {

        Map<String, String> annotations = new HashMap<>();
        annotations.put("/properties/tags", "[String]");

        assertTrue(PathTypeAnnotationUtil.enforceArrayItemLimit(
                "/properties/tags", "singleValue", annotations));
    }

    // ========================= JSON string parsing =========================

    @Test
    public void testValidateValueComplexArrayFromJsonString()
            throws Exception {

        // This is the exact failing case: after JWE decryption the value is a JSON string.
        Map<String, String> annotations = new HashMap<>();
        annotations.put("/properties/riskFactors", "[factor: String, is-critical: Boolean]");

        String jsonString = "[{\"factor\":\"no_risk_factors_detected\",\"is-critical\":false}]";

        assertTrue(PathTypeAnnotationUtil.validateValueAgainstAnnotation(
                "/properties/riskFactors", jsonString, annotations));
    }

    @Test
    public void testValidateValueComplexArrayFromJsonStringMultipleItems() {

        Map<String, String> annotations = new HashMap<>();
        annotations.put("/properties/risks", "[risk: Float, factor: String]");

        String jsonString = "[{\"risk\":0.5,\"factor\":\"ip_mismatch\"}" +
                ",{\"risk\":0.8,\"factor\":\"high_risk_email\"}]";

        assertTrue(PathTypeAnnotationUtil.validateValueAgainstAnnotation(
                "/properties/risks", jsonString, annotations));
    }

    @Test
    public void testValidateValueComplexObjectFromJsonString() {

        Map<String, String> annotations = new HashMap<>();
        annotations.put("/properties/risk", "risk: Float, factor: String");

        String jsonString = "{\"risk\":0.85,\"factor\":\"ip_mismatch\"}";

        assertTrue(PathTypeAnnotationUtil.validateValueAgainstAnnotation(
                "/properties/risk", jsonString, annotations));
    }

    @Test
    public void testValidateValueComplexArrayFromJsonStringUnknownAttribute() {

        Map<String, String> annotations = new HashMap<>();
        annotations.put("/properties/risks", "[risk: Float, factor: String]");

        // JSON string with an attribute not in the schema.
        String jsonString = "[{\"risk\":0.5,\"unknown\":\"bad\"}]";

        assertFalse(PathTypeAnnotationUtil.validateValueAgainstAnnotation(
                "/properties/risks", jsonString, annotations));
    }

    @Test
    public void testCoerceValueComplexArrayFromJsonString() {

        Map<String, String> annotations = new HashMap<>();
        annotations.put("/properties/riskFactors", "[factor: String, is-critical: Boolean]");

        String jsonString = "[{\"factor\":\"no_risk_factors_detected\",\"is-critical\":false}]";

        Object result = PathTypeAnnotationUtil.coerceValue(
                "/properties/riskFactors", jsonString, annotations);

        // Should be parsed into a List, not returned as a plain string.
        assertTrue(result instanceof List);
        @SuppressWarnings("unchecked")
        List<Object> list = (List<Object>) result;
        assertEquals(list.size(), 1);
        assertTrue(list.get(0) instanceof Map);
    }

    @Test
    public void testCoerceValueMultivaluedPrimaryFromJsonString() {

        Map<String, String> annotations = new HashMap<>();
        annotations.put("/properties/tags", "[String]");

        String jsonString = "[\"tag1\",\"tag2\",\"tag3\"]";

        Object result = PathTypeAnnotationUtil.coerceValue(
                "/properties/tags", jsonString, annotations);

        assertTrue(result instanceof List);
        @SuppressWarnings("unchecked")
        List<String> list = (List<String>) result;
        assertEquals(list.size(), 3);
        assertEquals(list.get(0), "tag1");
    }

    @Test
    public void testCoerceValueComplexObjectFromJsonString() {

        Map<String, String> annotations = new HashMap<>();
        annotations.put("/properties/risk", "risk: Float, factor: String");

        String jsonString = "{\"risk\":0.85,\"factor\":\"ip_mismatch\"}";

        Object result = PathTypeAnnotationUtil.coerceValue(
                "/properties/risk", jsonString, annotations);

        // Should be parsed into a Map, not returned as a plain string.
        assertTrue(result instanceof Map);
        @SuppressWarnings("unchecked")
        Map<String, Object> map = (Map<String, Object>) result;
        assertEquals(map.get("factor"), "ip_mismatch");
    }

    @Test
    public void testCoerceValueNonJsonStringIsNotParsed() {

        Map<String, String> annotations = new HashMap<>();
        annotations.put("/properties/score", "Integer");

        // A plain string with no JSON structure should be coerced to String normally.
        Object result = PathTypeAnnotationUtil.coerceValue(
                "/properties/score", "42", annotations);
        assertEquals(result, "42");
    }
}
