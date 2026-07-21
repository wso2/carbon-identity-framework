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

package org.wso2.carbon.identity.configuration.mgt.core;

import org.wso2.carbon.identity.configuration.mgt.core.model.Resource;
import org.wso2.carbon.identity.configuration.mgt.core.model.ResourceIdentifier;

/**
 * Interface for resolving default configurations for a given resource type and name.
 */
public interface DefaultConfigResolver {

    /**
     * Get the resource identifier that this resolver can handle.
     * @return ResourceIdentifier of the default configuration.
     */
    ResourceIdentifier getResourceIdentifier();

    /**
     * Get the default configurations for the given resource type and name.
     *
     * @param resourceTypeName Name of the resource type.
     * @param resourceName     Name of the resource.
     * @return Resource containing the default configurations.
     */
    Resource getDefaultConfigs(String resourceTypeName, String resourceName);
}
