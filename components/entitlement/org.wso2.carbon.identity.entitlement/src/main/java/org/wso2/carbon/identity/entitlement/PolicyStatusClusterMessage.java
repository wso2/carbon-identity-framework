
/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wso2.carbon.identity.entitlement;


import org.apache.axis2.clustering.ClusteringCommand;
import org.apache.axis2.clustering.ClusteringFault;
import org.apache.axis2.clustering.ClusteringMessage;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.entitlement.cache.IdentityCacheKey;
import org.wso2.carbon.identity.entitlement.cache.PolicyCache;
import org.wso2.carbon.identity.entitlement.cache.PolicyStatus;

/**
 * ClusterMessage to carry policy status changes (like UPDATE, CREATED) to other nodes.
 */
public class PolicyStatusClusterMessage extends ClusteringMessage {

    private static Log log = LogFactory.getLog(PolicyStatusClusterMessage.class);
    private static final long serialVersionUID = -5025603871368248102L;

    private IdentityCacheKey key;
    private PolicyStatus status;

    public PolicyStatusClusterMessage(IdentityCacheKey key, PolicyStatus status) {
        this.key = key;
        this.status = status;
    }

    public IdentityCacheKey getKey() {
        return key;
    }

    public PolicyStatus getStatus() {
        return status;
    }

    @Override
    public ClusteringCommand getResponse() {
        return null;
    }

    @Override
    public void execute(ConfigurationContext configurationContext) throws ClusteringFault {

        if (log.isDebugEnabled()) {
            log.debug("Received PolicyStatusClusterMessage.");
        }
        // we need to update our local policy status map based on the received cluster message from other node
        PolicyCache.updateLocalPolicyCacheMap(key, status);
        if (log.isDebugEnabled()) {
            log.debug("Updated Local Policy Status Map.");
        }
    }
}