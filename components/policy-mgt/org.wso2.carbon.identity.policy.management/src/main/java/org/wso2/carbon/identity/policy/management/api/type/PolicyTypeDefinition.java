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

import java.util.Set;

/**
 * Defines a policy type, such as device assurance policies.
 *
 * <p>A consumer that stores its policies through the policy management service registers an implementation of
 * this interface as an OSGi service. Policy management only accepts policies whose type is registered, and only
 * accepts resource targets that the type supports.</p>
 */
public interface PolicyTypeDefinition {

    /**
     * Returns the policy type. Types are uppercase constants, for example {@code DEVICE}.
     *
     * @return Policy type.
     */
    String getType();

    /**
     * Returns the resource targets supported by this policy type, in lowercase. For device policies, these are
     * the device platforms.
     *
     * @return Supported resource targets.
     */
    Set<String> getSupportedTargets();
}
