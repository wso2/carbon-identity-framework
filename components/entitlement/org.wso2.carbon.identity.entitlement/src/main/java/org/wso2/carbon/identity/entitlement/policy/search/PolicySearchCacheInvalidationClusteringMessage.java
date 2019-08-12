/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
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
package org.wso2.carbon.identity.entitlement.policy.search;

import org.apache.axis2.clustering.ClusteringCommand;
import org.apache.axis2.clustering.ClusteringFault;
import org.apache.axis2.clustering.ClusteringMessage;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;
import org.wso2.carbon.identity.entitlement.pdp.EntitlementEngine;

/**
 * ClusterMessage to invalidate {@link org.wso2.carbon.identity.entitlement.cache.PolicySearchCache} in other nodes.
 */
public class PolicySearchCacheInvalidationClusteringMessage extends ClusteringMessage {

    private static Log log = LogFactory.getLog(PolicySearchCacheInvalidationClusteringMessage.class);
    private static final long serialVersionUID = -5025603871368248102L;

    private int tenantId;

    public PolicySearchCacheInvalidationClusteringMessage(int tenantId) {

        this.tenantId = tenantId;
    }

    @Override
    public ClusteringCommand getResponse() {

        return null;
    }

    @Override
    public void execute(ConfigurationContext configurationContext) throws ClusteringFault {

        if (log.isDebugEnabled()) {
            log.debug("Received PolicySearchCacheInvalidationClusteringMessage.");
        }
        // We need to clear our local policy search cache of the corresponding tenant based on the received cluster
        // message from other node.
        int tenantIdInThreadLocalContext = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        try{
            // Clear local cache for the tenant domain included with the cluster message.
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantId(tenantId, true);

            EntitlementEngine.getInstance().getPolicySearch().getPolicySearchCache().clearCache();
            if (log.isDebugEnabled()) {
                log.debug("Local policy search cache is cleared for the tenant: "
                        + IdentityTenantUtil.getTenantDomain(tenantId) + ".");
            }
        } finally {
            // Switch back to the original tenant domain used in this thread local context.
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantId(tenantIdInThreadLocalContext, true);
        }
    }
}
