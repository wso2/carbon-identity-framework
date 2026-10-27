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

import org.apache.commons.lang.StringUtils;
import org.wso2.carbon.identity.policy.management.api.model.PolicyResource.ResourceType;

import java.util.Collections;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Defines a policy type, such as device assurance policies.
 *
 * <p>A consumer that stores its policies through the policy management service registers a subclass as an OSGi
 * service. Policy management only accepts policies whose type is registered, and only accepts resource targets and
 * resource types that the type supports.</p>
 *
 * <p>The type is given a {@link PolicyResourceTypeHandler} for every resource type its policies support, which
 * carries the behaviour specific to that resource type. The definition is immutable once created.</p>
 */
public abstract class PolicyTypeDefinition {

    private final String type;
    private final Set<String> supportedTargets;
    private final Map<ResourceType, PolicyResourceTypeHandler> resourceTypeHandlers;

    /**
     * Creates a policy type definition.
     *
     * @param type             Policy type, as an uppercase constant such as {@code DEVICE}.
     * @param supportedTargets Resource targets the type supports, in lowercase.
     * @param handlers         Handlers of the resource types the type supports, at most one per resource type.
     */
    protected PolicyTypeDefinition(String type, Set<String> supportedTargets, PolicyResourceTypeHandler... handlers) {

        if (StringUtils.isBlank(type)) {
            throw new IllegalArgumentException("Policy type cannot be blank.");
        }
        this.type = type;
        this.supportedTargets = supportedTargets != null
                ? Collections.unmodifiableSet(new HashSet<>(supportedTargets)) : Collections.emptySet();

        Map<ResourceType, PolicyResourceTypeHandler> handlerMap = new EnumMap<>(ResourceType.class);
        for (PolicyResourceTypeHandler handler : handlers) {
            if (handler == null || handler.getResourceType() == null) {
                throw new IllegalArgumentException("Resource type handler of policy type " + type + " is invalid.");
            }
            if (handler.getResourceType() == ResourceType.RULE && !(handler instanceof RuleResourceTypeHandler)) {
                throw new IllegalArgumentException("Rule resources of policy type " + type
                        + " must be handled by a " + RuleResourceTypeHandler.class.getSimpleName() + ".");
            }
            if (handlerMap.put(handler.getResourceType(), handler) != null) {
                throw new IllegalArgumentException("Policy type " + type + " has more than one handler for "
                        + "resource type " + handler.getResourceType() + ".");
            }
        }
        this.resourceTypeHandlers = Collections.unmodifiableMap(handlerMap);
    }

    /**
     * Returns the policy type. Types are uppercase constants, for example {@code DEVICE}.
     *
     * @return Policy type.
     */
    public final String getType() {

        return type;
    }

    /**
     * Returns the resource targets supported by this policy type, in lowercase. For device policies, these are
     * the device platforms.
     *
     * @return Supported resource targets.
     */
    public final Set<String> getSupportedTargets() {

        return supportedTargets;
    }

    /**
     * Returns the resource types a policy of this type can hold, which are the resource types it has a handler for.
     *
     * @return Supported resource types.
     */
    public final Set<ResourceType> getSupportedResourceTypes() {

        return resourceTypeHandlers.keySet();
    }

    /**
     * Returns the handler of a resource type. The handler of {@link ResourceType#RULE} resources is a
     * {@link RuleResourceTypeHandler}.
     *
     * @param resourceType Resource type.
     * @return Resource type handler, or {@code null} if the type does not support the resource type.
     */
    public final PolicyResourceTypeHandler getResourceTypeHandler(ResourceType resourceType) {

        return resourceTypeHandlers.get(resourceType);
    }
}
