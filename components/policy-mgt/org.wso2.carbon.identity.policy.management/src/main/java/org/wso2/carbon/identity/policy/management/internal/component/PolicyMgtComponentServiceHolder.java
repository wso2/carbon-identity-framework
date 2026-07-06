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

package org.wso2.carbon.identity.policy.management.internal.component;

import org.wso2.carbon.identity.policy.management.api.manager.PolicyResourceManager;
import org.wso2.carbon.identity.policy.management.api.model.ResourceType;
import org.wso2.carbon.identity.policy.management.api.service.PolicyManagementService;
import org.wso2.carbon.identity.rule.management.api.service.RuleManagementService;

import java.util.EnumMap;
import java.util.Map;

/**
 * Service holder for the policy management component.
 * Provides access to OSGi services consumed by this bundle.
 */
public class PolicyMgtComponentServiceHolder {

    private static final PolicyMgtComponentServiceHolder INSTANCE = new PolicyMgtComponentServiceHolder();

    private RuleManagementService ruleManagementService;
    private PolicyManagementService policyManagementService;
    private final Map<ResourceType, PolicyResourceManager> resourceManagers = new EnumMap<>(ResourceType.class);

    private PolicyMgtComponentServiceHolder() {

    }

    /**
     * Returns the singleton service holder instance.
     *
     * @return Service holder.
     */
    public static PolicyMgtComponentServiceHolder getInstance() {

        return INSTANCE;
    }

    public RuleManagementService getRuleManagementService() {

        return ruleManagementService;
    }

    public void setRuleManagementService(RuleManagementService ruleManagementService) {

        this.ruleManagementService = ruleManagementService;
    }

    public PolicyManagementService getPolicyManagementService() {

        return policyManagementService;
    }

    public void setPolicyManagementService(PolicyManagementService policyManagementService) {

        this.policyManagementService = policyManagementService;
    }

    /**
     * Registers a policy resource manager, keyed by the resource type it supports.
     *
     * @param resourceManager Manager to register.
     */
    public void addResourceManager(PolicyResourceManager resourceManager) {

        resourceManagers.put(resourceManager.getSupportedResourceType(), resourceManager);
    }

    /**
     * Unregisters a policy resource manager.
     *
     * @param resourceManager Manager to unregister.
     */
    public void removeResourceManager(PolicyResourceManager resourceManager) {

        resourceManagers.remove(resourceManager.getSupportedResourceType());
    }

    /**
     * Returns the manager registered for the given resource type.
     *
     * @param resourceType Resource type.
     * @return Manager for the resource type, or {@code null} if none is registered.
     */
    public PolicyResourceManager getResourceManager(ResourceType resourceType) {

        return resourceManagers.get(resourceType);
    }
}
