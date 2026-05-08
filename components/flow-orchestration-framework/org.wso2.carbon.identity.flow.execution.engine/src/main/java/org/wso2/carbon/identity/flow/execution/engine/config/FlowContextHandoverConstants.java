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

package org.wso2.carbon.identity.flow.execution.engine.config;

/**
 * Constants used by the flow execution context handover filtering.
 */
public final class FlowContextHandoverConstants {

    private FlowContextHandoverConstants() {

    }

    // ---- identity.xml config keys (used by IdentityUtil.getPropertyAsList) ----
    public static final String HANDOVER_ROOT = "FlowExecutionContextHandover";
    public static final String INCLUDED_ATTRIBUTES_KEY =
            HANDOVER_ROOT + ".IncludedAttributes.IncludedAttribute";
    public static final String INCLUDED_USER_ATTRIBUTES_KEY =
            HANDOVER_ROOT + ".IncludedUserAttributes.IncludedUserAttribute";

    // ---- Special attribute names ----
    /** When present in {@code includedAttributes}, the entire {@code FlowUser} passes through
     *  and {@code includedUserAttributes} is ignored. */
    public static final String ATTR_FLOW_USER = "flowUser";

    /** Engine-internal flow id; always copied regardless of configuration. */
    public static final String ATTR_CONTEXT_IDENTIFIER = "contextIdentifier";

    /** User-credentials property name; the only Map field requiring per-entry char[] cloning. */
    public static final String ATTR_USER_CREDENTIALS = "userCredentials";
}
