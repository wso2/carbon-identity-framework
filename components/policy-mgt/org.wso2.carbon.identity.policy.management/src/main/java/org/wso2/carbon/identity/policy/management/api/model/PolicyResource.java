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

package org.wso2.carbon.identity.policy.management.api.model;

/**
 * Association between a Policy and a resource (a Rule or an Action) for a specific target.
 * The target is a selector value (e.g. a platform or category); one target may have at most one
 * resource of each {@link ResourceType} per policy. Concrete subclasses own the type-specific
 * payload for their {@link ResourceType}, e.g. {@link RulePolicyResource}.
 */
public abstract class PolicyResource {

    private final String id;
    private final String target;
    private final String resourceId;

    protected PolicyResource(String id, String target, String resourceId) {

        this.id = id;
        this.target = target;
        this.resourceId = resourceId;
    }

    public String getId() {

        return id;
    }

    public String getTarget() {

        return target;
    }

    public String getResourceId() {

        return resourceId;
    }

    /**
     * Returns the resource type this policy resource represents.
     *
     * @return The resource type.
     */
    public abstract ResourceType getResourceType();
}
