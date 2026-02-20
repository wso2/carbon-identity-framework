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

package org.wso2.carbon.identity.action.execution.internal.executor;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Utility class for hierarchical prefix-based path matching and context area identification.
 * 
 * This class provides utilities for working with the unified prefix hierarchy used in
 * In-Flow Extensions for context access control.
 * 
 * Prefix Hierarchy Structure:
 * <pre>
 * /user/                           - User context
 *   /user/claims/{claimURI}        - User claims (keys are system-configured claim URIs)
 *   /user/userId                   - User's unique identifier
 *   /user/username                 - Resolved username
 *   /user/userStoreDomain          - User store domain
 *   /user/credentials/             - User credentials (system-configured types)
 *   /user/federatedAssociations/   - Federated IDP associations
 * 
 * /properties/{key}                - Flow properties (fully extensible)
 * 
 * /input/{key}                     - User input data (runtime extensible)
 * 
 * /flow/                           - Flow metadata (READ-ONLY)
 *   /flow/tenantDomain             - Tenant domain
 *   /flow/applicationId            - Application ID
 *   /flow/flowType                 - Flow type (REGISTRATION, etc.)
 *   /flow/contextIdentifier        - Flow context identifier
 * 
 * /graph/                          - Graph state (READ-ONLY)
 *   /graph/currentNode/            - Current node info
 *     /graph/currentNode/id        - Current node ID
 *     /graph/currentNode/type      - Current node type
 * </pre>
 */
public final class HierarchicalPrefixMatcher {

    // Context area prefix constants
    public static final String USER_PREFIX = "/user/";
    public static final String USER_CLAIMS_PREFIX = "/user/claims/";
    public static final String USER_CREDENTIALS_PREFIX = "/user/credentials/";
    public static final String USER_FEDERATED_PREFIX = "/user/federatedAssociations/";
    public static final String USER_ID_PATH = "/user/userId";
    public static final String USER_NAME_PATH = "/user/username";
    public static final String USER_STORE_DOMAIN_PATH = "/user/userStoreDomain";

    public static final String PROPERTIES_PREFIX = "/properties/";
    public static final String INPUT_PREFIX = "/input/";

    public static final String FLOW_PREFIX = "/flow/";
    public static final String FLOW_TENANT_PATH = "/flow/tenantDomain";
    public static final String FLOW_APP_ID_PATH = "/flow/applicationId";
    public static final String FLOW_TYPE_PATH = "/flow/flowType";

    public static final String GRAPH_PREFIX = "/graph/";
    public static final String GRAPH_CURRENT_NODE_PREFIX = "/graph/currentNode/";

    /**
     * Default expose configuration — all context areas are exposed.
     * Used when no explicit expose configuration is provided by the executor metadata.
     */
    public static final List<String> DEFAULT_EXPOSE = Collections.unmodifiableList(
            Arrays.asList(USER_PREFIX, PROPERTIES_PREFIX, INPUT_PREFIX, FLOW_PREFIX, GRAPH_PREFIX));

    /**
     * Context area enum for categorization.
     */
    public enum ContextArea {
        USER_CLAIMS(USER_CLAIMS_PREFIX, true),      // System-configured keys (claim URIs)
        USER_CREDENTIALS(USER_CREDENTIALS_PREFIX, true), // System-configured keys
        USER_FEDERATED(USER_FEDERATED_PREFIX, true),     // System-configured keys (IDP names)
        USER_SCALAR(USER_PREFIX, false),            // Scalar user fields (userId, username, etc.)
        PROPERTIES(PROPERTIES_PREFIX, false),       // Fully extensible
        INPUT(INPUT_PREFIX, false),                 // Runtime extensible
        FLOW(FLOW_PREFIX, false),                   // Read-only scalar values
        GRAPH(GRAPH_PREFIX, false);                 // Read-only graph state

        private final String prefix;
        private final boolean hasSystemConfiguredKeys;

        ContextArea(String prefix, boolean hasSystemConfiguredKeys) {

            this.prefix = prefix;
            this.hasSystemConfiguredKeys = hasSystemConfiguredKeys;
        }

        public String getPrefix() {

            return prefix;
        }

        /**
         * Whether this context area has system-configured keys that must be validated.
         * - USER_CLAIMS: Keys are claim URIs configured in claim dialect
         * - USER_CREDENTIALS: Keys are credential types (e.g., "password")
         * - USER_FEDERATED: Keys are IDP names
         */
        public boolean hasSystemConfiguredKeys() {

            return hasSystemConfiguredKeys;
        }
    }

    private HierarchicalPrefixMatcher() {

        // Utility class, no instantiation
    }

    /**
     * Identify the context area for a given path.
     *
     * @param path The full path (e.g., "/user/claims/http://wso2.org/claims/email")
     * @return The ContextArea or null if path doesn't match any known area
     */
    public static ContextArea identifyContextArea(String path) {

        if (path == null || path.isEmpty()) {
            return null;
        }

        // Check specific prefixes first (more specific before less specific)
        if (path.startsWith(USER_CLAIMS_PREFIX)) {
            return ContextArea.USER_CLAIMS;
        }
        if (path.startsWith(USER_CREDENTIALS_PREFIX)) {
            return ContextArea.USER_CREDENTIALS;
        }
        if (path.startsWith(USER_FEDERATED_PREFIX)) {
            return ContextArea.USER_FEDERATED;
        }
        if (path.startsWith(USER_PREFIX)) {
            return ContextArea.USER_SCALAR;
        }
        if (path.startsWith(PROPERTIES_PREFIX)) {
            return ContextArea.PROPERTIES;
        }
        if (path.startsWith(INPUT_PREFIX)) {
            return ContextArea.INPUT;
        }
        if (path.startsWith(FLOW_PREFIX)) {
            return ContextArea.FLOW;
        }
        if (path.startsWith(GRAPH_PREFIX)) {
            return ContextArea.GRAPH;
        }

        return null;
    }

    /**
     * Extract the key from a path within a context area.
     * For map-based areas (claims, properties, input), this extracts the map key.
     *
     * @param path The full path
     * @param prefix The prefix to remove
     * @return The extracted key or null if invalid
     */
    public static String extractKey(String path, String prefix) {

        if (path == null || prefix == null || !path.startsWith(prefix)) {
            return null;
        }

        String key = path.substring(prefix.length());

        // Handle trailing slash
        if (key.endsWith("/")) {
            key = key.substring(0, key.length() - 1);
        }

        // Handle nested paths (e.g., /properties/nested/field -> return "nested")
        int slashIndex = key.indexOf('/');
        if (slashIndex > 0) {
            // For simple use, return only the first level key
            // The full remaining path is available via getSubPath()
            key = key.substring(0, slashIndex);
        }

        return key.isEmpty() ? null : key;
    }

    /**
     * Extract the full remaining path after the prefix.
     * Unlike extractKey, this returns the complete sub-path including nested paths.
     *
     * @param path The full path
     * @param prefix The prefix to remove
     * @return The full remaining path or null if invalid
     */
    public static String getSubPath(String path, String prefix) {

        if (path == null || prefix == null || !path.startsWith(prefix)) {
            return null;
        }

        String subPath = path.substring(prefix.length());

        // Handle trailing slash
        if (subPath.endsWith("/")) {
            subPath = subPath.substring(0, subPath.length() - 1);
        }

        return subPath.isEmpty() ? null : subPath;
    }

    /**
     * Build a full path from a prefix and key.
     *
     * @param prefix The context area prefix
     * @param key The key within that area
     * @return The full path
     */
    public static String buildPath(String prefix, String key) {

        if (prefix == null || key == null) {
            return null;
        }

        // Ensure prefix ends with /
        String normalizedPrefix = prefix.endsWith("/") ? prefix : prefix + "/";
        return normalizedPrefix + key;
    }

    /**
     * Check if a path is read-only (in /flow/ or /graph/ areas).
     *
     * @param path The path to check
     * @return true if the path is in a read-only area
     */
    public static boolean isReadOnly(String path) {

        if (path == null) {
            return false;
        }
        return path.startsWith(FLOW_PREFIX) || path.startsWith(GRAPH_PREFIX);
    }

    /**
     * Check if a path requires system-configured key validation.
     *
     * @param path The path to check
     * @return true if the path's keys must be validated against system configuration
     */
    public static boolean requiresSystemKeyValidation(String path) {

        ContextArea area = identifyContextArea(path);
        return area != null && area.hasSystemConfiguredKeys();
    }

    /**
     * Check if a path matches any of the given expose prefixes using bidirectional prefix matching.
     * A match occurs if the path starts with a prefix (prefix covers path) OR the prefix starts
     * with the path (path is a parent of prefix).
     *
     * <p>Examples:</p>
     * <ul>
     *   <li>{@code /user/claims/email} matches prefix {@code /user/} → prefix covers path</li>
     *   <li>{@code /user/} matches prefix {@code /user/claims/email} → path is parent of prefix</li>
     * </ul>
     *
     * @param path           The path to check.
     * @param exposePrefixes The list of expose prefixes.
     * @return {@code true} if the path matches any expose prefix.
     */
    public static boolean matchesAnyExpose(String path, List<String> exposePrefixes) {

        if (path == null || exposePrefixes == null || exposePrefixes.isEmpty()) {
            return false;
        }
        for (String prefix : exposePrefixes) {
            if (path.startsWith(prefix) || prefix.startsWith(path)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Map legacy path prefixes to unified hierarchy prefixes.
     * This provides backward compatibility with existing configurations.
     * 
     * Legacy mapping:
     * - /userInputs/ -> /input/
     * - /user/claims/ -> /user/claims/ (unchanged)
     * - /properties/ -> /properties/ (unchanged)
     */

    // TODO: This is a simple mapping for demonstration. In a real implementation,
    // this could be loaded from configuration OR use all context names unchanged.
    private static final Map<String, String> LEGACY_PREFIX_MAPPING = createLegacyMapping();

    private static Map<String, String> createLegacyMapping() {

        Map<String, String> mapping = new HashMap<>();
        mapping.put("/userInputs/", INPUT_PREFIX);
        // Add more legacy mappings as needed
        return mapping;
    }

    /**
     * Normalize a path by converting legacy prefixes to unified prefixes.
     *
     * @param path The path to normalize
     * @return The normalized path using unified prefixes
     */
    public static String normalizePath(String path) {

        if (path == null) {
            return null;
        }

        for (Map.Entry<String, String> entry : LEGACY_PREFIX_MAPPING.entrySet()) {
            if (path.startsWith(entry.getKey())) {
                return entry.getValue() + path.substring(entry.getKey().length());
            }
        }

        return path;
    }
}
