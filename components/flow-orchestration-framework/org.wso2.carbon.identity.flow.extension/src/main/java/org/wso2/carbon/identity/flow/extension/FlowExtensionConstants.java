/*
 * Copyright (c) 2026, WSO2 LLC. (https://www.wso2.com) All Rights Reserved.
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.identity.flow.extension;

import org.wso2.carbon.identity.flow.extension.executor.FlowExtensionResponseProcessor;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Constants for the In-Flow Extension executor pipeline.
 */
public class FlowExtensionConstants {

    private FlowExtensionConstants() {

    }

    public static final String FLOW_EXECUTION_CONTEXT_KEY = "flowExecutionContext";
    public static final String PATH_TYPE_ANNOTATIONS_KEY = "pathTypeAnnotations";
    public static final String MODIFY_PATHS_KEY = "modifyPaths";
    public static final String PENDING_CLAIMS_KEY = "pendingClaims";
    public static final String PENDING_CREDENTIALS_KEY = "pendingCredentials";
    public static final String PENDING_PROPERTIES_KEY = "pendingProperties";
    public static final String PENDING_REDIRECT_URL_KEY = "pendingRedirectUrl";

    public static final String FAILURE_TYPE_KEY = "failureType";
    public static final String FLOW_EXTENSION_FAILURE_TYPE = "FLOW_EXTENSION_FAILURE";
    public static final String FAILURE_MESSAGE_KEY = "failureMessage";
    public static final String FAILURE_DESCRIPTION_KEY = "failureDescription";

    public static final String ACTION_ID_METADATA_KEY = "actionId";

    /**
     * Keys under which {@link FlowExtensionResponseProcessor} populates
     * {@link org.wso2.carbon.identity.action.execution.api.model.SuccessStatus} response context.
     * See the class javadoc on {@code FlowExtensionResponseProcessor} for the failure-handling
     * policy these keys surface.
     */
    public static final class ResponseContext {

        public static final String FAILED_OPERATIONS_KEY = "failedOperations";
        public static final String TOTAL_OPERATIONS_KEY = "totalOperations";

        public static final String OP_PATH_KEY = "path";
        public static final String OP_TYPE_KEY = "op";
        public static final String OP_MESSAGE_KEY = "message";

        private ResponseContext() {

        }
    }

    /**
     * User-facing error message / description pairs returned via {@code ExecutorResponse}
     * when In-Flow Extension execution fails or is unavailable.
     */
    public static final class ErrorMessages {

        public static final String NOT_CONFIGURED_MESSAGE = "Extension is not configured.";
        public static final String NOT_CONFIGURED_DESCRIPTION =
                "The Flow Extension action is missing required configuration. " +
                        "Contact your administrator.";

        public static final String EXECUTION_FAILED_MESSAGE =
                "An error occurred while processing the extension. Please try again.";
        public static final String EXECUTION_FAILED_DESCRIPTION =
                "The external extension service could not complete the request. " +
                        "If the problem persists, contact your administrator.";

        public static final String INCOMPLETE_NO_REDIRECT_MESSAGE =
                "Extension returned INCOMPLETE without a redirect URL.";
        public static final String INCOMPLETE_NO_REDIRECT_DESCRIPTION =
                "The external extension returned an incomplete response. Please try again.";

        private ErrorMessages() {

        }
    }

    public static final class ActionManagement {

        public static final String ACCESS_CONFIG_EXPOSE = "ACCESS_CONFIG_EXPOSE";
        public static final String ACCESS_CONFIG_MODIFY = "ACCESS_CONFIG_MODIFY";
        public static final int MAX_EXPOSE_PATHS = 50;
        public static final String ICON_URL = "ICON_URL";
        public static final String CERTIFICATE = "CERTIFICATE";
        public static final String CERTIFICATE_NAME_PREFIX = "ACTIONS:";

        private ActionManagement() {

        }
    }

    public static final class Log {

        public static final String COMPONENT_ID = "inflow-extension";

        private Log() {

        }

        public static final class ActionIDs {

            public static final String EXECUTE = "execute-inflow-extension";
            public static final String PROCESS_RESPONSE = "process-inflow-extension-response";

            private ActionIDs() {

            }
        }
    }

    /**
     * Default handover policy: which {@code FlowExecutionContext} and {@code FlowUser} fields
     * are forwarded to the action framework. Serves as the documented defaults for the
     * toml-based dynamic config in {@code identity.xml.j2}.
     */
    public static final class HandoverPolicy {

        public static final String ATTR_FLOW_USER = "flowUser";
        public static final String ATTR_CONTEXT_IDENTIFIER = "contextIdentifier";
        public static final String ATTR_USER_CREDENTIALS = "userCredentials";
        public static final Set<String> INCLUDED_ATTRIBUTES = Collections.unmodifiableSet(
                new HashSet<>(Arrays.asList(
                        "contextIdentifier",
                        "tenantDomain",
                        "applicationId",
                        "flowType",
                        "callbackUrl",
                        "portalUrl",
                        "flowUser"
                )));

        public static final Set<String> INCLUDED_USER_ATTRIBUTES = Collections.unmodifiableSet(
                new HashSet<>(Arrays.asList(
                        "id",
                        "username",
                        "userStoreDomain",
                        "claims",
                        "userCredentials"
                )));

        private HandoverPolicy() {

        }
    }

    /**
     * Constants used when building the controlled In-Flow Extension context tree returned
     * by the metadata endpoint.
     */
    public static final class ContextTree {

        // Flow type identifiers — must match the values produced by FlowTypes.getType().
        public static final String FLOW_REGISTRATION = "REGISTRATION";
        public static final String FLOW_INVITED_USER_REGISTRATION = "INVITED_USER_REGISTRATION";
        public static final String FLOW_PASSWORD_RECOVERY = "PASSWORD_RECOVERY";

        // Node-type sentinels matching the tree component's NodeType enum on the Console side.
        public static final String NODE_OBJECT = "OBJECT";
        public static final String NODE_LEAF = "LEAF";
        public static final String NODE_MAP = "MAP";
        public static final String NODE_COMPLEX_MAP = "COMPLEX_MAP";

        public static final String OP_EXPOSE = "EXPOSE";
        public static final String OP_MODIFY = "MODIFY";
        public static final String DATA_TYPE_STRING = "String";

        private ContextTree() {

        }
    }

    /**
     * JSON-pointer-style path constants for the In-Flow Extension context tree.
     */
    public static final class FlowContextPaths {

        public static final String USER_PREFIX = "/user/";
        public static final String USER_ID_PATH = "/user/id";
        public static final String USER_STORE_DOMAIN_PATH = "/user/userStoreDomain";
        public static final String USER_CLAIMS_PATH_PREFIX = "/user/claims/";
        public static final String USER_CLAIMS_SELECTOR_PREFIX = "/user/claims[uri=";
        public static final String USER_CLAIMS_SELECTOR_SUFFIX = "]";
        public static final String USER_CREDENTIALS_PATH_PREFIX = "/user/credentials/";

        public static final String PROPERTIES_PATH_PREFIX = "/properties/";

        public static final String FLOW_PREFIX = "/flow/";
        public static final String FLOW_TYPE_PATH = "/flow/flowType";
        public static final String FLOW_PORTAL_URL_PATH = "/flow/portalUrl";

        public static final String TENANT_PREFIX = "/tenant/";
        public static final String TENANT_DOMAIN_PATH = "/tenant/domain";

        public static final String APPLICATION_PREFIX = "/application/";
        public static final String APPLICATION_ID_PATH = "/application/id";

        public static final String ORGANIZATION_PREFIX = "/organization/";
        public static final String ORGANIZATION_ID_PATH = "/organization/id";
        public static final String ORGANIZATION_NAME_PATH = "/organization/name";
        public static final String ORGANIZATION_HANDLE_PATH = "/organization/orgHandle";
        public static final String ORGANIZATION_DEPTH_PATH = "/organization/depth";

        private FlowContextPaths() {

        }
    }
}
