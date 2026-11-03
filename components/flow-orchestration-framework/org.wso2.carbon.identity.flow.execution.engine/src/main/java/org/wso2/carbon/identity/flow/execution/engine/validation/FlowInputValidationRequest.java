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

package org.wso2.carbon.identity.flow.execution.engine.validation;

import org.wso2.carbon.identity.flow.execution.engine.model.FlowExecutionContext;

import java.util.Collections;
import java.util.Map;

/**
 * Request passed to a {@link FlowInputValidator}.
 * <p>
 * It holds only the inputs submitted in the current request, which are the inputs of the current step. Inputs from
 * earlier steps are not included.
 */
public class FlowInputValidationRequest {

    private final FlowExecutionContext context;
    private final Map<String, String> inputs;

    public FlowInputValidationRequest(FlowExecutionContext context) {

        this.context = context;
        this.inputs = Collections.unmodifiableMap(context.getUserInputData());
    }

    /**
     * Get the inputs submitted to the current step, keyed by the input identifier.
     * Claim inputs are keyed by the claim URI.
     *
     * @return Read-only map of the submitted inputs.
     */
    public Map<String, String> getInputs() {

        return inputs;
    }

    /**
     * Get the tenant domain of the flow.
     *
     * @return Tenant domain.
     */
    public String getTenantDomain() {

        return context.getTenantDomain();
    }

    /**
     * Get the flow type.
     *
     * @return Flow type.
     */
    public String getFlowType() {

        return context.getFlowType();
    }

    /**
     * Get the flow execution context, for data that the request does not expose directly.
     * Validators must not modify the context.
     *
     * @return Flow execution context.
     */
    public FlowExecutionContext getContext() {

        return context;
    }
}
