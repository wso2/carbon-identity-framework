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

package org.wso2.carbon.identity.policy.management.api.type;

import org.wso2.carbon.identity.policy.management.api.model.PolicyResource.ResourceType;

/**
 * Policy type specific behaviour for one resource type, such as rules.
 *
 * <p>Each resource type has its own handler interface that extends this one. A {@link PolicyTypeDefinition} holds
 * one handler for every resource type its policies support.</p>
 */
public interface PolicyResourceTypeHandler {

    /**
     * Returns the resource type this handler is for.
     *
     * @return Resource type.
     */
    ResourceType getResourceType();
}
