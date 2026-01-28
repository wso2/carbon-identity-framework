/*
 * Copyright (c) 2024, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.carbon.identity.rule.metadata.internal.component;

import org.wso2.carbon.identity.claim.metadata.mgt.ClaimMetadataManagementService;

/**
 * Data holder for Rule Metadata Service component.
 * This class holds references to OSGi services required by the rule metadata module.
 */
public class RuleMetadataServiceDataHolder {

    private static final RuleMetadataServiceDataHolder INSTANCE = new RuleMetadataServiceDataHolder();

    private ClaimMetadataManagementService claimMetadataManagementService;

    private RuleMetadataServiceDataHolder() {

    }

    /**
     * Get the singleton instance of RuleMetadataServiceDataHolder.
     *
     * @return RuleMetadataServiceDataHolder instance.
     */
    public static RuleMetadataServiceDataHolder getInstance() {

        return INSTANCE;
    }

    /**
     * Get the ClaimMetadataManagementService.
     *
     * @return ClaimMetadataManagementService instance.
     */
    public ClaimMetadataManagementService getClaimMetadataManagementService() {

        return claimMetadataManagementService;
    }

    /**
     * Set the ClaimMetadataManagementService.
     *
     * @param claimMetadataManagementService ClaimMetadataManagementService instance.
     */
    public void setClaimMetadataManagementService(ClaimMetadataManagementService claimMetadataManagementService) {

        this.claimMetadataManagementService = claimMetadataManagementService;
    }
}
