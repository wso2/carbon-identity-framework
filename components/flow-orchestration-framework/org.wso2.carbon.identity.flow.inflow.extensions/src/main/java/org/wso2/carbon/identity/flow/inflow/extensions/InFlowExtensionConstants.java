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

package org.wso2.carbon.identity.flow.inflow.extensions;

/**
 * Constants for the In-Flow Extension executor pipeline.
 *
 * <p>Keys are shared across the executor, request builder, and response processor
 * via the {@link org.wso2.carbon.identity.action.execution.api.model.FlowContext}
 * handoff mechanism. Path prefixes drive operation routing in the response processor.</p>
 */
public class InFlowExtensionConstants {

    private InFlowExtensionConstants() {

    }

    // ---- FlowContext pipeline keys ----
    public static final String FLOW_EXECUTION_CONTEXT_KEY = "flowExecutionContext";
    public static final String PATH_TYPE_ANNOTATIONS_KEY  = "pathTypeAnnotations";
    public static final String MODIFY_PATHS_KEY           = "modifyPaths";
    public static final String PENDING_CLAIMS_KEY         = "pendingClaims";
    public static final String PENDING_CREDENTIALS_KEY    = "pendingCredentials";
    public static final String PENDING_PROPERTIES_KEY     = "pendingProperties";
    public static final String PENDING_REDIRECT_URL_KEY   = "pendingRedirectUrl";

    // ---- Response info keys (FAILED path) ----
    public static final String FAILURE_TYPE_KEY              = "failureType";
    public static final String IN_FLOW_EXTENSION_FAILURE_TYPE = "IN_FLOW_EXTENSION_FAILURE";
    public static final String FAILURE_MESSAGE_KEY           = "failureMessage";
    public static final String FAILURE_DESCRIPTION_KEY       = "failureDescription";

    // ---- Context path prefixes ----
    public static final String PROPERTIES_PATH_PREFIX      = "/properties/";
    public static final String USER_CLAIMS_PATH_PREFIX     = "/user/claims/";
    public static final String USER_CREDENTIALS_PATH_PREFIX = "/user/credentials/";

    // ---- Miscellaneous ----
    public static final String ACTION_ID_METADATA_KEY       = "actionId";
}
