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

package org.wso2.carbon.identity.flow.inflow.extensions.executor;

/**
 * Diagnostic log constants for the In-Flow Extension layer.
 *
 * <p>These constants are intentionally separate from
 * {@link org.wso2.carbon.identity.action.execution.api.constant.ActionExecutionLogConstants}
 * because the events tracked here are flow-engine concerns (executor lifecycle, operation routing,
 * JWE decryption) rather than action HTTP-call concerns (which remain under "action-execution").</p>
 */
public class InFlowExtensionLogConstants {

    private InFlowExtensionLogConstants() {
    }

    public static final String COMPONENT_ID = "inflow-extension";

    /**
     * Action IDs for diagnostic log events emitted by the In-Flow Extension layer.
     */
    public static class ActionIDs {

        public static final String EXECUTE         = "execute-inflow-extension";
        public static final String PROCESS_RESPONSE = "process-inflow-extension-response";
    }
}
