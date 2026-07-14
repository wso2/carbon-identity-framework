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

package org.wso2.carbon.identity.policy.evaluation.api.model;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Engine-neutral context supplied by the caller for a policy evaluation.
 * Each {@code PolicyResourceEvaluator} adapts this context into whatever its backing engine requires
 * (e.g. the rule engine's flow context), so the policy API stays independent of any specific engine.
 * The flow type is carried as a plain string so that engines may add flow types without any change here.
 */
public class PolicyEvaluationContext {

    private final String flowType;
    private final Map<String, Object> contextData;

    private PolicyEvaluationContext(String flowType) {

        this.flowType = flowType;
        this.contextData = new HashMap<>();
    }

    /**
     * Creates a context for the given flow.
     *
     * @param flowType Identifier of the flow this evaluation runs in (e.g. {@code DEVICE_POLICY}).
     * @return A new context.
     */
    public static PolicyEvaluationContext create(String flowType) {

        return new PolicyEvaluationContext(flowType);
    }

    /**
     * Adds a context data entry.
     *
     * @param key   Data key.
     * @param value Data value.
     * @return This context, for chaining.
     */
    public PolicyEvaluationContext add(String key, Object value) {

        contextData.put(key, value);
        return this;
    }

    /**
     * Returns the flow this evaluation runs in.
     *
     * @return Flow type identifier.
     */
    public String getFlowType() {

        return flowType;
    }

    /**
     * Returns the context data supplied for evaluation.
     *
     * @return Unmodifiable context data.
     */
    public Map<String, Object> getContextData() {

        return Collections.unmodifiableMap(contextData);
    }
}
