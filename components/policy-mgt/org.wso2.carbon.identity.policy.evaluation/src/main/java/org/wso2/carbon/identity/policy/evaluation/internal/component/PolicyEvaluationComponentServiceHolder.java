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

package org.wso2.carbon.identity.policy.evaluation.internal.component;

import org.wso2.carbon.identity.policy.evaluation.api.evaluator.PolicyResourceEvaluator;
import org.wso2.carbon.identity.policy.management.api.model.ResourceType;
import org.wso2.carbon.identity.policy.management.api.service.PolicyManagementService;
import org.wso2.carbon.identity.rule.evaluation.api.service.RuleEvaluationService;

import java.util.EnumMap;
import java.util.Map;

/**
 * Service holder for the policy evaluation component.
 * Provides access to OSGi services consumed by this bundle.
 */
public class PolicyEvaluationComponentServiceHolder {

    private static final PolicyEvaluationComponentServiceHolder INSTANCE = new PolicyEvaluationComponentServiceHolder();

    private PolicyManagementService policyManagementService;
    private RuleEvaluationService ruleEvaluationService;
    private final Map<ResourceType, PolicyResourceEvaluator> policyResourceEvaluators =
            new EnumMap<>(ResourceType.class);

    private PolicyEvaluationComponentServiceHolder() {

    }

    /**
     * Returns the singleton service holder instance.
     *
     * @return Service holder.
     */
    public static PolicyEvaluationComponentServiceHolder getInstance() {

        return INSTANCE;
    }

    /**
     * Get the PolicyManagementService.
     *
     * @return PolicyManagementService instance.
     */
    public PolicyManagementService getPolicyManagementService() {

        return policyManagementService;
    }

    /**
     * Set the PolicyManagementService.
     *
     * @param policyManagementService PolicyManagementService instance.
     */
    public void setPolicyManagementService(PolicyManagementService policyManagementService) {

        this.policyManagementService = policyManagementService;
    }

    /**
     * Get the RuleEvaluationService.
     *
     * @return RuleEvaluationService instance.
     */
    public RuleEvaluationService getRuleEvaluationService() {

        return ruleEvaluationService;
    }

    /**
     * Set the RuleEvaluationService.
     *
     * @param ruleEvaluationService RuleEvaluationService instance.
     */
    public void setRuleEvaluationService(RuleEvaluationService ruleEvaluationService) {

        this.ruleEvaluationService = ruleEvaluationService;
    }

    /**
     * Registers a policy resource evaluator, keyed by the resource type it supports.
     *
     * @param policyResourceEvaluator Evaluator to register.
     */
    public void addPolicyResourceEvaluator(PolicyResourceEvaluator policyResourceEvaluator) {

        policyResourceEvaluators.put(policyResourceEvaluator.getSupportedResourceType(), policyResourceEvaluator);
    }

    /**
     * Unregisters a policy resource evaluator.
     *
     * @param policyResourceEvaluator Evaluator to unregister.
     */
    public void removePolicyResourceEvaluator(PolicyResourceEvaluator policyResourceEvaluator) {

        policyResourceEvaluators.remove(policyResourceEvaluator.getSupportedResourceType());
    }

    /**
     * Returns the evaluator registered for the given resource type.
     *
     * @param resourceType Resource type.
     * @return Evaluator for the resource type, or {@code null} if none is registered.
     */
    public PolicyResourceEvaluator getPolicyResourceEvaluator(ResourceType resourceType) {

        return policyResourceEvaluators.get(resourceType);
    }
}
