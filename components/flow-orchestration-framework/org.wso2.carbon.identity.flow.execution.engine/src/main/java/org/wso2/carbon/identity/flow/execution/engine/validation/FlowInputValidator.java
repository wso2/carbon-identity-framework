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

import org.wso2.carbon.identity.flow.execution.engine.exception.FlowEngineException;
import org.wso2.carbon.identity.flow.mgt.Constants.FlowTypes;

import java.util.Collections;
import java.util.Set;

/**
 * Extension point to validate the user inputs submitted to a flow step.
 * <p>
 * Implementations are registered as OSGi services. The flow engine calls them once per step submit, after the
 * built-in input validations pass and before the inputs are stored in the flow context. A validator that rejects
 * the inputs throws a {@link org.wso2.carbon.identity.flow.execution.engine.exception.FlowEngineClientException}.
 * The engine then shows the same step again with the error code and message of that exception.
 * <p>
 * The request contains raw input values, including passwords. Implementations must not log input values.
 */
public interface FlowInputValidator {

    /**
     * Get the unique name of the validator.
     *
     * @return Name of the validator.
     */
    String getName();

    /**
     * Get the order of the validator. Validators with a lower order run first.
     *
     * @return Order of the validator.
     */
    default int getOrder() {

        return 100;
    }

    /**
     * Get the flow types this validator applies to.
     *
     * @return Set of supported flow types. An empty set means all flow types.
     */
    default Set<FlowTypes> getSupportedFlowTypes() {

        return Collections.emptySet();
    }

    /**
     * Check whether the validator should run for the submitted step.
     * The engine calls this method on every step submit, so it must not do expensive work.
     *
     * @param request Flow input validation request.
     * @return true if the validator should validate the request.
     */
    boolean canHandle(FlowInputValidationRequest request);

    /**
     * Validate the inputs submitted to the step.
     *
     * @param request Flow input validation request.
     * @throws FlowEngineException FlowEngineClientException to reject the inputs with a user facing error, or
     *                             FlowEngineServerException if the validation could not be completed.
     */
    void validate(FlowInputValidationRequest request) throws FlowEngineException;
}
