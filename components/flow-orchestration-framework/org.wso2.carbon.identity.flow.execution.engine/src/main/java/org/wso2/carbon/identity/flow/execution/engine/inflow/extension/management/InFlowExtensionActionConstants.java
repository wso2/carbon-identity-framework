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

package org.wso2.carbon.identity.flow.execution.engine.inflow.extension.management;

/**
 * Constants for In-Flow Extension action management.
 */
public class InFlowExtensionActionConstants {

    /**
     * Property key for the expose path prefixes stored in IDN_ACTION_PROPERTIES.
     * Stored as a BLOB (JSON-serialized list of strings).
     */
    public static final String ACCESS_CONFIG_EXPOSE = "ACCESS_CONFIG_EXPOSE";

    /**
     * Property key for the allowed operations stored in IDN_ACTION_PROPERTIES.
     * Stored as a BLOB (JSON-serialized list of operation descriptors).
     */
    public static final String ACCESS_CONFIG_MODIFY = "ACCESS_CONFIG_MODIFY";

    /**
     * Separator used between property key and flow type in override keys.
     * For example: "ACCESS_CONFIG_EXPOSE:REGISTRATION"
     */
    public static final String OVERRIDE_KEY_SEPARATOR = ":";

    /**
     * Prefix for flow-type-specific expose override properties.
     * Full key format: "ACCESS_CONFIG_EXPOSE:&lt;FLOW_TYPE&gt;"
     */
    public static final String ACCESS_CONFIG_EXPOSE_PREFIX = ACCESS_CONFIG_EXPOSE + OVERRIDE_KEY_SEPARATOR;

    /**
     * Prefix for flow-type-specific allowed operations override properties.
     * Full key format: "ACCESS_CONFIG_ALLOWED_OPERATIONS:&lt;FLOW_TYPE&gt;"
     */
    public static final String ACCESS_CONFIG_MODIFY_PREFIX =
            ACCESS_CONFIG_MODIFY + OVERRIDE_KEY_SEPARATOR;

    /**
     * Metadata key used in executor metadata (flow configuration) to pass a unified access config
     * object containing both {@code expose} and {@code allowedOperations}.
     * <p>
     * The value is a JSON object: {@code {"expose": [...], "allowedOperations": [...]}}
     * </p>
     * <p>
     * This key is extracted by the {@code AccessConfigFlowUpdateInterceptor} during flow updates,
     * stored as per-flow-type action properties, and <b>stripped</b> from executor metadata before
     * the flow graph is persisted (to avoid VARCHAR(255) column size limitations).
     * </p>
     */
    public static final String ACCESS_CONFIG_METADATA_KEY = "accessConfig";

    /**
     * Maximum number of expose path prefixes allowed per action.
     */
    public static final int MAX_EXPOSE_PATHS = 50;

    /**
     * Property key for the action's icon URL stored in IDN_ACTION_PROPERTIES.
     * Stored as a PRIMITIVE string.
     */
    public static final String ICON_URL = "ICON_URL";

    /**
     * Property key for the external service's certificate stored in IDN_ACTION_PROPERTIES.
     * During persistence the certificate object is replaced with its UUID (PRIMITIVE string);
     * during retrieval the UUID is resolved back to the full Certificate object.
     */
    public static final String CERTIFICATE = "CERTIFICATE";

    /**
     * Naming prefix for certificates stored via CertificateManagementService.
     * Full name format: {@code "ACTIONS:<action-uuid>"}.
     */
    public static final String CERTIFICATE_NAME_PREFIX = "ACTIONS:";

    private InFlowExtensionActionConstants() {
    }
}
