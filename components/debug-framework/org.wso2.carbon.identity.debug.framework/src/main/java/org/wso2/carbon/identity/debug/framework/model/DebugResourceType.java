/**
 * Copyright (c) 2026, WSO2 LLC. (https://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.identity.debug.framework.model;

import org.wso2.carbon.identity.debug.framework.extension.DebugResourceHandler;
import org.wso2.carbon.identity.debug.framework.registry.DebugHandlerRegistry;

/**
 * Enum for debug resource types.
 * Defines the different types of resources that can be debugged (IdP,
 * Application, Connector, etc.).
 */
public enum DebugResourceType {

    IDP,
    FRAUD_DETECTION,
    CUSTOM;

    /**
     * Converts a string to the corresponding DebugResourceType.
     * Matching is case-insensitive for explicit resource type values.
     *
     * @param resourceType The resource type identifier.
     * @return The corresponding DebugResourceType, or CUSTOM if not found.
     */
    public static DebugResourceType fromString(String resourceType) {

        if (resourceType == null) {
            return CUSTOM;
        }

        switch (resourceType.trim().toLowerCase(java.util.Locale.ENGLISH)) {
            case "idp":
                return IDP;

            case "fraud_detection":
                return FRAUD_DETECTION;

            default:
                return CUSTOM;
        }
    }

    /**
     * Gets the appropriate DebugResourceHandler for this resource type.
     * IDP handler is looked up from OSGi registry.
     * Other handlers are instantiated directly or looked up from registry.
     *
     * @return The appropriate DebugResourceHandler, or null if not found.
     */
    public DebugResourceHandler getHandler() {

        return DebugHandlerRegistry.getInstance().getHandler(name().toLowerCase(java.util.Locale.ENGLISH));
    }
}
