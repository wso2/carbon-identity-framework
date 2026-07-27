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

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility for parsing path type annotations.
 */
public final class PathTypeAnnotationUtil {

    static final Pattern ANNOTATION_PATTERN = Pattern.compile("\\{([^}]*)}$");
    static final String LOCAL_CLAIM_DIALECT_PREFIX = "http://wso2.org/claims/";
    static final String IDENTITY_CLAIM_URI_PREFIX = "http://wso2.org/claims/identity/";
    static final int MAX_ATTRIBUTES_PER_OBJECT = 10;

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
}
