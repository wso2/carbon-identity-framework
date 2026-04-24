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

import java.util.List;

/**
 * Utility class for hierarchical prefix-based path matching for In-Flow Extension access control.
 *
 * <p>Expose and modify path lists always contain <b>exact leaf paths</b> (no trailing {@code /}).
 * Two distinct matching operations are needed, served by two explicit methods:</p>
 * <ul>
 *   <li>{@link #anyExposedUnder(String, List)} — area-gate check: is <em>any</em> leaf path
 *       in the list under a given area prefix (e.g. {@code /user/claims/})?</li>
 *   <li>{@link #isExposedPath(String, List)} — exact check: is a specific leaf path
 *       (e.g. {@code /user/claims/http://wso2.org/claims/email}) present in the list?</li>
 * </ul>
 *
 * <p>Prefix hierarchy:</p>
 * <pre>
 * /user/                           - User context
 *   /user/claims/{claimURI}        - User claims
 *   /user/userId                   - User's unique identifier
 *   /user/userStoreDomain          - User store domain
 *   /user/credentials/{key}        - User credentials (no key validation required)
 *
 * /properties/{key}                - Flow properties (fully extensible)
 *
 * /flow/                           - Flow metadata (READ-ONLY)
 *   /flow/tenantDomain             - Tenant domain
 *   /flow/applicationId            - Application ID
 *   /flow/flowType                 - Flow type (REGISTRATION, etc.)
 *   /flow/callbackUrl              - Callback URL (expose-only)
 *   /flow/portalUrl                - Portal URL (expose-only)
 * </pre>
 */
public final class HierarchicalPrefixMatcher {

    // Context area prefix constants
    public static final String USER_PREFIX = "/user/";
    public static final String USER_CLAIMS_PREFIX = "/user/claims/";
    public static final String USER_CREDENTIALS_PREFIX = "/user/credentials/";
    public static final String USER_ID_PATH = "/user/userId";
    public static final String USER_STORE_DOMAIN_PATH = "/user/userStoreDomain";

    public static final String PROPERTIES_PREFIX = "/properties/";

    public static final String FLOW_PREFIX = "/flow/";
    public static final String FLOW_TENANT_PATH = "/flow/tenantDomain";
    public static final String FLOW_APP_ID_PATH = "/flow/applicationId";
    public static final String FLOW_TYPE_PATH = "/flow/flowType";
    public static final String FLOW_CALLBACK_URL_PATH = "/flow/callbackUrl";
    public static final String FLOW_PORTAL_URL_PATH = "/flow/portalUrl";

    private HierarchicalPrefixMatcher() {

    }

    /**
     * Check if a path is read-only (in /flow/ area).
     *
     * @param path The path to check
     * @return true if the path is in a read-only area
     */
    public static boolean isReadOnly(String path) {

        if (path == null) {
            return false;
        }
        return path.startsWith(FLOW_PREFIX);
    }

    /**
     * Check if any leaf path in the list falls under the given area prefix.
     *
     * <p>Used as an area-gate check before iterating over a data block — e.g., to decide
     * whether to include any claims, credentials, or properties in the outgoing request.
     * The {@code areaPrefix} always ends with {@code /} (e.g. {@code /user/claims/}).
     * The {@code leafPaths} list contains only exact leaf paths with no trailing {@code /}.</p>
     *
     * @param areaPrefix The area prefix to check (must end with {@code /}).
     * @param leafPaths  The list of exposed leaf paths.
     * @return {@code true} if at least one leaf path starts with the area prefix.
     */
    public static boolean anyExposedUnder(String areaPrefix, List<String> leafPaths) {

        if (areaPrefix == null || leafPaths == null || leafPaths.isEmpty()) {
            return false;
        }
        for (String path : leafPaths) {
            if (path != null && path.startsWith(areaPrefix)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Check if an exact leaf path is present in the expose list.
     *
     * <p>Used for leaf-level filtering — e.g., to decide whether a specific claim URI,
     * credential key, or scalar field should be included in the outgoing request.
     * The {@code leafPath} has no trailing {@code /}.
     * The {@code leafPaths} list contains only exact leaf paths.</p>
     *
     * @param leafPath  The exact path to look up.
     * @param leafPaths The list of exposed leaf paths.
     * @return {@code true} if the path is present in the list.
     */
    public static boolean isExposedPath(String leafPath, List<String> leafPaths) {

        if (leafPath == null || leafPaths == null || leafPaths.isEmpty()) {
            return false;
        }
        return leafPaths.contains(leafPath);
    }

}
