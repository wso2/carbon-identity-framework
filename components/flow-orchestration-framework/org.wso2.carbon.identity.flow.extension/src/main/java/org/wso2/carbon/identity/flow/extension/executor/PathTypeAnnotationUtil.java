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

package org.wso2.carbon.identity.flow.extension.executor;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility for parsing and coercing path type annotations.
 */
public final class PathTypeAnnotationUtil {

    static final Pattern ANNOTATION_PATTERN = Pattern.compile("\\{([^}]*)}$");
    static final String LOCAL_CLAIM_DIALECT_PREFIX = "http://wso2.org/claims/";
    static final String IDENTITY_CLAIM_URI_PREFIX = "http://wso2.org/claims/identity/";
    static final int MAX_ATTRIBUTES_PER_OBJECT = 10;
    static final int MAX_ARRAY_ITEMS = 10;

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private PathTypeAnnotationUtil() {
    }

    /**
     * Strip a trailing annotation from a raw path.
     *
     * @return {@code [cleanPath, annotation]}; annotation is {@code null} if absent.
     */
    public static String[] stripAnnotation(String rawPath) {

        if (rawPath == null) {
            return new String[]{null, null};
        }
        Matcher matcher = ANNOTATION_PATTERN.matcher(rawPath);
        if (matcher.find()) {
            return new String[]{rawPath.substring(0, matcher.start()), matcher.group(1)};
        }
        return new String[]{rawPath, null};
    }

    /**
     * Coerce a value based on its path annotation: complex returned as-is,
     * primary arrays become {@code List<String>}, others become String.
     */
    @SuppressWarnings("unchecked")
    public static Object coerceValue(String path, Object value, Map<String, String> pathTypeAnnotations) {

        if (value == null) {
            return null;
        }
        String annotation = pathTypeAnnotations.get(path);
        if (annotation == null) {
            return String.valueOf(value);
        }
        if (annotation.startsWith("[")) {
            String inner = annotation.substring(1, annotation.length() - 1);
            if (inner.contains(":")) {
                return tryParseJsonString(value);
            }
            Object resolved = tryParseJsonString(value);
            if (resolved instanceof List) {
                List<String> stringList = new ArrayList<>();
                for (Object item : (List<Object>) resolved) {
                    stringList.add(item == null ? null : String.valueOf(item));
                }
                return stringList;
            }
            List<String> singleList = new ArrayList<>();
            singleList.add(String.valueOf(value));
            return singleList;
        }
        if (annotation.contains(":")) {
            return tryParseJsonString(value);
        }
        return String.valueOf(value);
    }

    /**
     * Validate a complex annotation does not exceed {@link #MAX_ATTRIBUTES_PER_OBJECT}.
     * Returns {@code true} for non-complex or empty annotations.
     */
    public static boolean validateAnnotationLimits(String annotation) {

        if (annotation == null || annotation.isEmpty()) {
            return true;
        }
        String inner = annotation;
        if (inner.startsWith("[") && inner.endsWith("]")) {
            inner = inner.substring(1, inner.length() - 1);
        }
        if (!inner.contains(":")) {
            return true;
        }
        return parseAnnotationAttributes(inner).size() <= MAX_ATTRIBUTES_PER_OBJECT;
    }

    /**
     * Validate a complex object/array value against its schema annotation.
     * Enforces {@link #MAX_ATTRIBUTES_PER_OBJECT} and {@link #MAX_ARRAY_ITEMS}.
     * Returns {@code true} for non-complex annotations.
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
        if (!inner.contains(":")) {
            return true;
        }
        Map<String, String> schema = parseAnnotationAttributes(inner);
        Object resolved = tryParseJsonString(value);
        if (!isArray) {
            return validateSingleComplexObject(resolved, schema);
        }
        if (!(resolved instanceof List)) {
            return false;
        }
        List<Object> items = (List<Object>) resolved;
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

    /**
     * Enforce {@link #MAX_ARRAY_ITEMS} on array-typed values.
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
     * Parse a String as JSON if it starts with {@code [} or {@code {}; otherwise return as-is.
     * Handles serialized JSON arriving from external services (e.g. after JWE decryption).
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
     * Parse complex annotation attributes into a name-to-type map.
     */
    private static Map<String, String> parseAnnotationAttributes(String inner) {

        Map<String, String> attributes = new HashMap<>();
        for (String part : inner.split(",")) {
            String trimmed = part.trim();
            if (trimmed.isEmpty()) {
                continue;
            }
            int colonIndex = trimmed.indexOf(':');
            if (colonIndex > 0) {
                attributes.put(trimmed.substring(0, colonIndex).trim(),
                        trimmed.substring(colonIndex + 1).trim());
            }
        }
        return attributes;
    }

    /**
     * Validate a single complex object: must be a Map with schema-only keys,
     * attribute count within limits, and only single-level nesting.
     */
    @SuppressWarnings("unchecked")
    private static boolean validateSingleComplexObject(Object value, Map<String, String> schema) {

        if (!isMapAndWithinAttributeLimit(value)) {
            return false;
        }
        Map<String, Object> map = (Map<String, Object>) value;
        if (!containsOnlySchemaKeys(map, schema)) {
            return false;
        }
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            if (!validateEntryValueAgainstType(entry, schema)) {
                return false;
            }
        }
        return true;
    }

    private static boolean isMapAndWithinAttributeLimit(Object value) {

        return value instanceof Map && ((Map<?, ?>) value).size() <= MAX_ATTRIBUTES_PER_OBJECT;
    }

    private static boolean containsOnlySchemaKeys(Map<String, Object> valueMap, Map<String, String> schema) {

        for (String key : valueMap.keySet()) {
            if (!schema.containsKey(key)) {
                return false;
            }
        }
        return true;
    }

    private static boolean validateEntryValueAgainstType(Map.Entry<String, Object> entry, Map<String, String> schema) {

        String type = schema.get(entry.getKey());
        Object attrValue = entry.getValue();
        if (type != null && type.endsWith("[]")) {
            return validatePrimaryArrayAttribute(attrValue);
        }
        return !isNestedStructure(attrValue);
    }

    @SuppressWarnings("unchecked")
    private static boolean validatePrimaryArrayAttribute(Object value) {

        if (!(value instanceof List)) {
            return false;
        }
        List<Object> list = (List<Object>) value;
        if (list.size() > MAX_ARRAY_ITEMS) {
            return false;
        }
        for (Object item : list) {
            if (isNestedStructure(item)) {
                return false;
            }
        }
        return true;
    }

    private static boolean isNestedStructure(Object value) {

        return value instanceof Map || value instanceof List;
    }
}
