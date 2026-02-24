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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
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

    private static final Log LOG = LogFactory.getLog(DebugResourceType.class);

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

        switch (resourceType.toLowerCase().trim()) {
            case "idp":
                return IDP;

            case "fraud_detection":
                return FRAUD_DETECTION;

            default:
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Unknown resource type: " + resourceType + ". Defaulting to CUSTOM.");
                }
                return CUSTOM;
        }
    }

    /**
     * Gets the appropriate DebugResourceHandler for this resource type.
     * IDP handler is looked up from OSGi registry (provided by debug.idp module).
     * Other handlers are instantiated directly or looked up from registry.
     *
     * @return The appropriate DebugResourceHandler, or null if not found.
     */
    public DebugResourceHandler getHandler() {

        switch (this) {
            case IDP:
                // IDP handler is provided and registered via OSGi.
                DebugResourceHandler idpHandler = DebugHandlerRegistry.getInstance().getHandler("idp");
                if (idpHandler == null && LOG.isDebugEnabled()) {
                    LOG.debug("IDP DebugResourceHandler not registered. " +
                            "Ensure org.wso2.carbon.identity.debug.idp bundle is deployed. " +
                            "Available handlers: " +
                            DebugHandlerRegistry.getInstance().getAllHandlers().keySet());
                }
                return idpHandler;

            case FRAUD_DETECTION:
                // Fraud detection handler is looked up from registry (provided by external module).
                DebugResourceHandler fraudHandler = DebugHandlerRegistry.getInstance()
                        .getHandler("fraud_detection");
                if (fraudHandler == null && LOG.isDebugEnabled()) {
                    LOG.debug("Fraud detection DebugResourceHandler not registered. " +
                            "Ensure the fraud detection debug bundle is deployed.");
                }
                return fraudHandler;

            case CUSTOM:
                // Custom handlers would be looked up from registry if needed.
                if (LOG.isDebugEnabled()) {
                    LOG.debug("CUSTOM resource type handler lookup would use registry");
                }
                return null;

            default:
                return null;
        }
    }
}
