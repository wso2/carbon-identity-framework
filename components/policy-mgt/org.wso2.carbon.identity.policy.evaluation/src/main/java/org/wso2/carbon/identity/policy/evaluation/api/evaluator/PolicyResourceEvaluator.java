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

package org.wso2.carbon.identity.policy.evaluation.api.evaluator;

import org.wso2.carbon.identity.policy.evaluation.api.exception.PolicyEvaluationException;
import org.wso2.carbon.identity.policy.evaluation.api.model.PolicyEvaluationContext;
import org.wso2.carbon.identity.policy.evaluation.api.model.ResourceEvaluationResult;
import org.wso2.carbon.identity.policy.management.api.model.PolicyResource;
import org.wso2.carbon.identity.policy.management.api.model.ResourceType;

/**
 * SPI for evaluating a single {@link PolicyResource} of a specific {@link ResourceType}.
 * Implementations are registered as OSGi services and dispatched to by {@link ResourceType}.
 * Implementations adapt the engine-neutral {@link PolicyEvaluationContext} into whatever their
 * backing engine requires, so this contract stays independent of any specific engine.
 */
public interface PolicyResourceEvaluator {

    /**
     * Returns the resource type this evaluator handles.
     *
     * @return Supported resource type.
     */
    ResourceType getSupportedResourceType();

    /**
     * Evaluates the given resource against the provided evaluation context.
     *
     * @param resource     Policy resource to evaluate.
     * @param context      Engine-neutral context carrying the data for evaluation.
     * @param tenantDomain Tenant domain.
     * @return Evaluation result for the resource.
     * @throws PolicyEvaluationException If evaluation fails.
     */
    ResourceEvaluationResult evaluate(PolicyResource resource, PolicyEvaluationContext context,
                                       String tenantDomain) throws PolicyEvaluationException;
}
