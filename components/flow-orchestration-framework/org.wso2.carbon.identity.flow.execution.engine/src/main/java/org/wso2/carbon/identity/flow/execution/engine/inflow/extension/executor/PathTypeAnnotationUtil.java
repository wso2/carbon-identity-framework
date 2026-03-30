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

package org.wso2.carbon.identity.flow.execution.engine.inflow.extension.executor;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility class for path type annotation parsing, stripping, and value coercion.
 *
 * <p>Path type annotations use a trailing brace expression at the end of a modify path
 * to declare the expected data type for that path. The unified format uses curly braces:</p>
 * <ul>
 *   <li>{@code /properties/risk-factor{String}} — primary data type.</li>
 *   <li>{@code /properties/risk-factors{[String]}} — multivalued primary (array of type).</li>
 *   <li>{@code /properties/risk{risk: Float, factor: String}} — complex object with schema.</li>
 *   <li>{@code /properties/risk{[risk: Float, factor: String]}} — multivalued complex object array.</li>
 * </ul>
 *
 * <p>This class provides methods to strip annotations from paths and coerce incoming values
 * based on the stored annotations.</p>
 */
public final class PathTypeAnnotationUtil {

    /**
     * Regex pattern to match a trailing curly brace annotation at the end of a path.
     * Captures the content inside the braces (Group 1).
     * Examples: {@code {String}}, {@code {[String]}}, {@code {risk: Float, factor: String}}.
     */
    static final Pattern ANNOTATION_PATTERN = Pattern.compile("\\{([^}]*)}$");

    /** Claim URI prefix for the WSO2 local claim dialect. */
    static final String LOCAL_CLAIM_DIALECT_PREFIX = "http://wso2.org/claims/";

    /** Claim URI prefix for WSO2 identity claims (subset of local claims, not user-modifiable). */
    static final String IDENTITY_CLAIM_URI_PREFIX = "http://wso2.org/claims/identity/";

    /** Reusable ObjectMapper for parsing JSON-string values received for complex-typed paths. */
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private PathTypeAnnotationUtil() {

    }

    /**
     * Strip a trailing path type annotation from a raw path.
     *
     * @param rawPath The raw path potentially containing a trailing {@code {annotation}}.
     * @return A two-element array: {@code [cleanPath, annotation]}.
     *         If no annotation is found, annotation element is {@code null}.
     */
    public static String[] stripAnnotation(String rawPath) {

        if (rawPath == null) {
            return new String[]{null, null};
        }

        Matcher matcher = ANNOTATION_PATTERN.matcher(rawPath);
        if (matcher.find()) {
            String cleanPath = rawPath.substring(0, matcher.start());
            String annotation = matcher.group(1);
            return new String[]{cleanPath, annotation};
        }
        return new String[]{rawPath, null};
    }

    /**
     * Coerce a value based on path type annotations.
     *
     * <p>Annotation interpretation:</p>
     * <ul>
     *   <li>{@code null} (no annotation): value is coerced to String via {@code String.valueOf()}.</li>
     *   <li>Starts with {@code [} and contains {@code :} (e.g., {@code [risk: Float]}):
     *       complex object array — value is passed through as-is.</li>
     *   <li>Starts with {@code [} without {@code :} (e.g., {@code [String]}):
     *       multivalued primary type — value is expected to be a List; each element coerced to String.
     *       A single value is wrapped into a list.</li>
     *   <li>Contains {@code :} (e.g., {@code risk: Float, factor: String}):
     *       complex object — value is passed through as-is.</li>
     *   <li>Any other annotation (e.g., {@code String}, {@code Integer}):
     *       primary type — value is coerced to String via {@code String.valueOf()}.</li>
     * </ul>
     *
     * @param path                The operation path (used as lookup key in annotations map).
     * @param value               The raw value from the operation.
     * @param pathTypeAnnotations Map from clean path to annotation content (may be empty).
     * @return The coerced value.
     */
    @SuppressWarnings("unchecked")
    public static Object coerceValue(String path, Object value,
                                     Map<String, String> pathTypeAnnotations) {

        String annotation = pathTypeAnnotations.get(path);

        if (annotation == null) {
            // No annotation: coerce to String.
            return String.valueOf(value);
        }

        // Check for multivalued annotation: starts with [
        if (annotation.startsWith("[")) {
            String inner = annotation.substring(1, annotation.length() - 1);
            if (inner.contains(":")) {
                // Complex object array (e.g., [risk: Float, factor: String]):
                // parse JSON string if needed, then pass through.
                return tryParseJsonString(value);
            }
            // Multivalued primary type (e.g., [String], [Integer]): coerce to List<String>.
            // Parse JSON string first in case the value arrived as "[\"a\",\"b\"]".
            Object resolvedList = tryParseJsonString(value);
            if (resolvedList instanceof List) {
                List<Object> rawList = (List<Object>) resolvedList;
                List<String> stringList = new ArrayList<>();
                for (Object item : rawList) {
                    stringList.add(String.valueOf(item));
                }
                return stringList;
            }
            // Single value — wrap in a list.
            List<String> singleList = new ArrayList<>();
            singleList.add(String.valueOf(value));
            return singleList;
        }

        // Check for complex object annotation: contains ":"
        if (annotation.contains(":")) {
            // Complex object (e.g., risk: Float, factor: String):
            // parse JSON string if needed, then pass through.
            return tryParseJsonString(value);
        }

        // Primary type annotation (e.g., String, Integer, Boolean): coerce to String.
        return String.valueOf(value);
    }

    /** Maximum number of attributes allowed in a complex object annotation. */
    static final int MAX_ATTRIBUTES_PER_OBJECT = 10;

    /** Maximum number of items allowed in an array (primary or complex object array). */
    static final int MAX_ARRAY_ITEMS = 10;

    /**
     * Validate that a complex object annotation does not exceed the maximum attribute count.
     * Should be called on the raw annotation content (inside braces) before stripping.
     *
     * @param annotation The annotation content (e.g., {@code "risk: Float, factor: String"}
     *                   or {@code "[risk: Float, factor: String]"}). May be {@code null}.
     * @return {@code true} if the annotation is valid (within limits or not a complex annotation).
     */
    public static boolean validateAnnotationLimits(String annotation) {

        if (annotation == null || annotation.isEmpty()) {
            return true;
        }

        String inner = annotation;
        // Unwrap array brackets if present.
        if (inner.startsWith("[") && inner.endsWith("]")) {
            inner = inner.substring(1, inner.length() - 1);
        }

        // Only validate complex annotations (those with attribute definitions containing ':').
        if (!inner.contains(":")) {
            return true;
        }

        return parseAnnotationAttributes(inner).size() <= MAX_ATTRIBUTES_PER_OBJECT;
    }

    /**
     * Validate a complex object value against its path type annotation schema.
     *
     * <p>Validates:</p>
     * <ul>
     *   <li>Value is a Map with attribute names matching the annotation schema.</li>
     *   <li>Only one nesting level: attributes must be primary types or primary arrays.</li>
     *   <li>Attribute count does not exceed {@link #MAX_ATTRIBUTES_PER_OBJECT}.</li>
     *   <li>Array attributes do not exceed {@link #MAX_ARRAY_ITEMS} items.</li>
     * </ul>
     *
     * <p>For non-complex annotations (primary types, primary arrays), this method returns
     * {@code true} without further validation since those are handled by coercion.</p>
     *
     * @param path                The operation path.
     * @param value               The value to validate.
     * @param pathTypeAnnotations Map from clean path to annotation content.
     * @return {@code true} if the value is valid against the annotation, {@code false} otherwise.
     */
    @SuppressWarnings("unchecked")
    public static boolean validateValueAgainstAnnotation(String path, Object value,
                                                         Map<String, String> pathTypeAnnotations) {

        String annotation = pathTypeAnnotations.get(path);
        if (annotation == null) {
            return true;
        }

        boolean isArray = annotation.startsWith("[");
        String inner = isArray ? annotation.substring(1, annotation.length() - 1) : annotation;

        // Only validate complex annotations (those with attribute definitions).
        if (!inner.contains(":")) {
            return true;
        }

        Map<String, String> schema = parseAnnotationAttributes(inner);

        // Parse JSON string if the value arrived as serialized JSON
        // (e.g., after JWE decryption or when the external service stringifies before encrypting).
        Object resolvedValue = tryParseJsonString(value);

        if (isArray) {
            // Complex object array: validate each item.
            if (!(resolvedValue instanceof List)) {
                return false;
            }
            List<Object> items = (List<Object>) resolvedValue;
            if (items.size() > MAX_ARRAY_ITEMS) {
                return false;
            }
            for (Object item : items) {
                if (!validateSingleComplexObject(item, schema)) {
                    return false;
                }
            }
            return true;
        }

        // Single complex object.
        return validateSingleComplexObject(resolvedValue, schema);
    }

    /**
     * Enforce array item limits on a value. Applies to both primary arrays and complex object arrays.
     *
     * @param path                The operation path.
     * @param value               The value to check.
     * @param pathTypeAnnotations Map from clean path to annotation content.
     * @return {@code true} if the value is within array item limits, {@code false} otherwise.
     */
    @SuppressWarnings("unchecked")
    public static boolean enforceArrayItemLimit(String path, Object value,
                                                Map<String, String> pathTypeAnnotations) {

        String annotation = pathTypeAnnotations.get(path);
        if (annotation == null || !annotation.startsWith("[")) {
            return true;
        }

        if (!(value instanceof List)) {
            return true;
        }

        return ((List<Object>) value).size() <= MAX_ARRAY_ITEMS;
    }

    /**
     * Attempt to parse a value as JSON if it is a String that starts with {@code [} or {@code {}.
     * This handles values that arrive as serialized JSON strings — for example, after JWE
     * decryption, or when an external service serialises a complex object/array before encrypting it.
     *
     * <p>If the value is not a String, does not start with {@code [} or {@code {}, or cannot be
     * parsed as valid JSON, the original value is returned unchanged.</p>
     *
     * @param value The value to inspect.
     * @return The parsed JSON structure (List or Map), or the original value if not applicable.
     */
    private static Object tryParseJsonString(Object value) {

        if (!(value instanceof String)) {
            return value;
        }
        String str = ((String) value).trim();
        if (!str.startsWith("[") && !str.startsWith("{")) {
            return value;
        }
        try {
            return OBJECT_MAPPER.readValue(str, Object.class);
        } catch (IOException ignored) {
            return value;
        }
    }

    /**
     * Parse complex annotation attributes into a map of attribute name to type.
     * Handles array types indicated by trailing {@code []}.
     *
     * @param inner The inner annotation content (e.g., {@code "risk: Float, factor: String"}).
     * @return Map from attribute name to type string (e.g., {@code "Float"} or {@code "String[]"}).
     */
    private static Map<String, String> parseAnnotationAttributes(String inner) {

        Map<String, String> attributes = new HashMap<>();
        String[] parts = inner.split(",");
        for (String part : parts) {
            String trimmed = part.trim();
            if (trimmed.isEmpty()) {
                continue;
            }
            int colonIndex = trimmed.indexOf(':');
            if (colonIndex > 0) {
                String name = trimmed.substring(0, colonIndex).trim();
                String type = trimmed.substring(colonIndex + 1).trim();
                attributes.put(name, type);
            }
        }
        return attributes;
    }

    /**
     * Validate a single complex object value against a schema.
     * Ensures value is a Map, keys match schema names, attribute count within limits,
     * and nested values are only primary types or primary arrays (single nesting level).
     *
     * @param value  The value to validate (expected to be a Map).
     * @param schema The annotation schema (attribute name to type).
     * @return {@code true} if valid.
     */
    @SuppressWarnings("unchecked")
    private static boolean validateSingleComplexObject(Object value, Map<String, String> schema) {

        if (!(value instanceof Map)) {
            return false;
        }

        Map<String, Object> map = (Map<String, Object>) value;

        // Attribute count limit.
        if (map.size() > MAX_ATTRIBUTES_PER_OBJECT) {
            return false;
        }

        // All keys in the value must be defined in the schema.
        for (String key : map.keySet()) {
            if (!schema.containsKey(key)) {
                return false;
            }
        }

        // Validate single nesting level: each attribute value must be primary or primary array.
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            String type = schema.get(entry.getKey());
            Object attrValue = entry.getValue();

            if (type != null && type.endsWith("[]")) {
                // Array attribute: validate it's a list of primitives within item limit.
                if (!(attrValue instanceof List)) {
                    return false;
                }
                List<Object> list = (List<Object>) attrValue;
                if (list.size() > MAX_ARRAY_ITEMS) {
                    return false;
                }
                for (Object item : list) {
                    if (item instanceof Map || item instanceof List) {
                        return false;
                    }
                }
            } else {
                // Primary attribute: must not be a nested structure.
                if (attrValue instanceof Map || attrValue instanceof List) {
                    return false;
                }
            }
        }

        return true;
    }
}
