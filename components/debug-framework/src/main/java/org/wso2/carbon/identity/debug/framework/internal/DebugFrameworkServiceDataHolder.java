/*
 * Copyright (c) 2025, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.carbon.identity.debug.framework.internal;

import org.wso2.carbon.identity.claim.metadata.mgt.ClaimMetadataManagementService;

/**
 * Data holder for Debug Framework Service Component.
 * This holds references to OSGi services that are required by the debug framework.
 */
public class DebugFrameworkServiceDataHolder {

    private static final DebugFrameworkServiceDataHolder instance = new DebugFrameworkServiceDataHolder();
    
    private ClaimMetadataManagementService claimMetadataManagementService;

    /**
     * Private constructor to prevent instantiation.
     */
    private DebugFrameworkServiceDataHolder() {
    }

    /**
     * Returns the singleton instance of the data holder.
     *
     * @return the singleton instance
     */
    public static DebugFrameworkServiceDataHolder getInstance() {
        return instance;
    }

    /**
     * Gets the ClaimMetadataManagementService.
     *
     * @return the ClaimMetadataManagementService instance
     */
    public ClaimMetadataManagementService getClaimMetadataManagementService() {
        return claimMetadataManagementService;
    }

    /**
     * Sets the ClaimMetadataManagementService.
     *
     * @param claimMetadataManagementService the ClaimMetadataManagementService instance
     */
    public void setClaimMetadataManagementService(ClaimMetadataManagementService claimMetadataManagementService) {
        this.claimMetadataManagementService = claimMetadataManagementService;
    }
}
