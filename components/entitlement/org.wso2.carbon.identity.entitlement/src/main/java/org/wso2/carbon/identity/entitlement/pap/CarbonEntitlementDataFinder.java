/*
*  Copyright (c) WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/

package org.wso2.carbon.identity.entitlement.pap;

import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.identity.entitlement.dto.EntitlementTreeNodeDTO;
import org.wso2.carbon.identity.entitlement.internal.EntitlementServiceComponent;
import org.wso2.carbon.registry.api.Resource;
import org.wso2.carbon.registry.core.Collection;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.user.api.UserStoreManager;
import org.wso2.carbon.user.core.common.AbstractUserStoreManager;

import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

/**
 * this is default implementation of the policy meta data finder module which finds the resource in the
 * under-line registry
 */
public class CarbonEntitlementDataFinder implements EntitlementDataFinderModule {

    private static final String MODULE_NAME = "Carbon Attribute Finder Module";

    private static final String SUBJECT_CATEGORY = "Subject";

    private static final String ACTION_CATEGORY = "Action";

    private static final String RESOURCE_CATEGORY = "Resource";

    private Registry registry;

    private String[] defaultActions = new String[]{"read", "write", "delete", "edit"};

    @Override
    public void init(Properties properties) throws Exception {

    }

    @Override
    public String getModuleName() {
        return MODULE_NAME;
    }

    @Override
    public Set<String> getRelatedApplications() {
        return null;
    }

    @Override
    public Set<String> getSupportedCategories() {
        Set<String> set = new HashSet<String>();
        set.add(SUBJECT_CATEGORY);
        set.add(ACTION_CATEGORY);
        set.add(RESOURCE_CATEGORY);
        return set;
    }

    @Override
    public EntitlementTreeNodeDTO getEntitlementData(String category, String regex,
                                                     int limit) throws Exception {

        registry = EntitlementServiceComponent.getRegistryService().getSystemRegistry(CarbonContext.
                getThreadLocalCarbonContext().getTenantId());
        if (RESOURCE_CATEGORY.equalsIgnoreCase(category)) {
            EntitlementTreeNodeDTO nodeDTO = new EntitlementTreeNodeDTO("/");
            getChildResources(nodeDTO, "_system");
            return nodeDTO;
        } else if (ACTION_CATEGORY.equalsIgnoreCase(category)) {
            EntitlementTreeNodeDTO nodeDTO = new EntitlementTreeNodeDTO("");
            for (String action : defaultActions) {
                EntitlementTreeNodeDTO childNode = new EntitlementTreeNodeDTO(action);
                nodeDTO.addChildNode(childNode);
            }
            return nodeDTO;
        } else if (SUBJECT_CATEGORY.equalsIgnoreCase(category)) {
            EntitlementTreeNodeDTO nodeDTO = new EntitlementTreeNodeDTO("");
            int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
            UserStoreManager userStoreManager = EntitlementServiceComponent.getRealmservice().
                    getTenantUserRealm(tenantId).getUserStoreManager();

            String[] roleNames = ((AbstractUserStoreManager) userStoreManager).
                    getRoleNames(regex, limit, false, true, true);

            for (String roleName : roleNames) {
                if (CarbonConstants.REGISTRY_ANONNYMOUS_ROLE_NAME.equals(roleName)) {
                    continue;
                }
                EntitlementTreeNodeDTO childNode = new EntitlementTreeNodeDTO(roleName);
                nodeDTO.addChildNode(childNode);
            }
            return nodeDTO;
        }

        return null;
    }

    @Override
    public EntitlementTreeNodeDTO getEntitlementDataByLevel(String category, int level) throws Exception {
        return null;
    }

    @Override
    public int getSupportedHierarchicalLevels() {
        return 0;
    }

    @Override
    public boolean isFullPathSupported() {
        return true;
    }

    @Override
    public boolean isHierarchicalTree() {
        return true;
    }

    @Override
    public boolean isAllApplicationRelated() {
        return true;
    }

    @Override
    public boolean isSearchSupported() {
        return true;
    }

    /**
     * This helps to find resources un a recursive manner
     *
     * @param node           attribute value node
     * @param parentResource parent resource Name
     * @return child resource set
     * @throws RegistryException throws
     */
    private EntitlementTreeNodeDTO getChildResources(EntitlementTreeNodeDTO node,
                                                     String parentResource) throws RegistryException {

        if (registry.resourceExists(parentResource)) {
            String[] resourcePath = parentResource.split("/");
            EntitlementTreeNodeDTO childNode =
                    new EntitlementTreeNodeDTO(resourcePath[resourcePath.length - 1]);
            node.addChildNode(childNode);
            Resource root = registry.get(parentResource);
            if (root instanceof Collection) {
                Collection collection = (Collection) root;
                String[] resources = collection.getChildren();
                for (String resource : resources) {
                    getChildResources(childNode, resource);
                }
            }
        }
        return node;
    }
}
