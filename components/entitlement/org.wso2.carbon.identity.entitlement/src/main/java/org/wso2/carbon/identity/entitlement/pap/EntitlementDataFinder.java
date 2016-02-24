/*
*  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.identity.entitlement.dto.EntitlementFinderDataHolder;
import org.wso2.carbon.identity.entitlement.dto.EntitlementTreeNodeDTO;
import org.wso2.carbon.identity.entitlement.internal.EntitlementServiceComponent;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

/**
 * When creating XACML policies from WSO2 Identity server, We can define set of pre-defined attribute
 * values, attribute ids, function and so on.  These data can be retrieved from external sources such as
 * databases,  LDAPs,  or file systems. we can register, set of data retriever modules with this class.
 */
public class EntitlementDataFinder {

    private static Log log = LogFactory.getLog(EntitlementDataFinder.class);

    /**
     * List of entitlement data finder modules
     */
    Set<EntitlementDataFinderModule> dataFinderModules = new HashSet<EntitlementDataFinderModule>();

    /**
     * tenant id
     */
    int tenantId;

    public EntitlementDataFinder() {

        this.tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
        Map<EntitlementDataFinderModule, Properties> metaDataFinderConfigs = EntitlementServiceComponent.
                getEntitlementConfig().getPolicyEntitlementDataFinders();
        // only one module can be there.
        if (metaDataFinderConfigs != null && !metaDataFinderConfigs.isEmpty()) {
            dataFinderModules = metaDataFinderConfigs.keySet();
        }
    }

    public EntitlementFinderDataHolder[] getEntitlementDataModules() {

        List<EntitlementFinderDataHolder> dataHolders = new ArrayList<EntitlementFinderDataHolder>();

        for (EntitlementDataFinderModule module : dataFinderModules) {
            EntitlementFinderDataHolder holder = new EntitlementFinderDataHolder();

            String name = module.getModuleName();
            if (name == null || name.trim().length() == 0) {
                name = module.getClass().getName();
            }

            Set<String> applicationIds = module.getRelatedApplications();
            if (applicationIds == null) {
                applicationIds = new HashSet<String>();
            }

            Set<String> supportedCategories = module.getSupportedCategories();
            if (supportedCategories == null) {
                supportedCategories = new HashSet<String>();
            }

            holder.setName(name);
            holder.setApplicationIds(applicationIds.toArray(new String[applicationIds.size()]));
            holder.setFullPathSupported(module.isFullPathSupported());
            holder.setHierarchicalLevels(module.getSupportedHierarchicalLevels());
            holder.setHierarchicalTree(module.isHierarchicalTree());
            holder.setAllApplicationRelated(module.isAllApplicationRelated());
            holder.setSupportedCategory(supportedCategories.toArray(new String[supportedCategories.size()]));
            holder.setSearchSupported(module.isSearchSupported());
            dataHolders.add(holder);
        }

        return dataHolders.toArray(new EntitlementFinderDataHolder[dataHolders.size()]);
    }

    public EntitlementTreeNodeDTO getEntitlementData(String dataModule, String category,
                                                     String regex, int level, int limit) {

        for (EntitlementDataFinderModule module : dataFinderModules) {
            if (dataModule != null && dataModule.trim().equalsIgnoreCase(module.getModuleName())) {
                try {
                    if (level == 0) {
                        return module.getEntitlementData(category, regex, limit);
                    } else {
                        return module.getEntitlementDataByLevel(category, level);
                    }
                } catch (Exception e) {
                    log.error("Error while retrieving entitlement data by " + dataModule, e);
                }
            }
        }

        return new EntitlementTreeNodeDTO();
    }
}
