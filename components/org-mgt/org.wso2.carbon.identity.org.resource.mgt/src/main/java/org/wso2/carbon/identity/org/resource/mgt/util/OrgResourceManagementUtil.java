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

package org.wso2.carbon.identity.org.resource.mgt.util;

import org.wso2.carbon.identity.org.resource.mgt.exceptions.OrgResourceManagementServerException;
import org.wso2.carbon.identity.org.resource.mgt.internal.OrgResourceManagementServiceDataHolder;
import org.wso2.carbon.identity.organization.management.service.OrganizationManager;
import org.wso2.carbon.identity.organization.management.service.exception.OrganizationManagementException;
import org.wso2.carbon.identity.organization.management.service.util.Utils;

/**
 * Utility class for organization resource management.
 */
public class OrgResourceManagementUtil {

    /**
     * Get the organization manager.
     *
     * @return Organization manager.
     */
    public static OrganizationManager getOrganizationManager() {

        return OrgResourceManagementServiceDataHolder.getInstance().getOrganizationManager();
    }

    /**
     * Check whether the minimum organization hierarchy depth is reached.
     *
     * @param orgId Organization ID.
     * @return True if the minimum hierarchy depth is reached.
     * @throws OrgResourceManagementServerException If an error occurs while checking the hierarchy depth.
     */
    public static boolean isMinOrgHierarchyDepthReached(String orgId) throws OrgResourceManagementServerException {

        int minHierarchyDepth = Utils.getSubOrgStartLevel() - 1;
        try {
            int depthInHierarchy = getOrganizationManager().getOrganizationDepthInHierarchy(orgId);
            return depthInHierarchy < minHierarchyDepth;
        } catch (OrganizationManagementException e) {
            throw new OrgResourceManagementServerException(
                    "Error occurred while getting the hierarchy depth of the organization: " + orgId, e);
        }
    }
}
