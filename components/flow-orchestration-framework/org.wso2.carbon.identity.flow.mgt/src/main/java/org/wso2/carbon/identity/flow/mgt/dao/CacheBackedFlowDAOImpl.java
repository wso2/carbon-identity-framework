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

package org.wso2.carbon.identity.flow.mgt.dao;

import org.wso2.carbon.identity.flow.mgt.cache.FlowMgtCache;
import org.wso2.carbon.identity.flow.mgt.cache.FlowMgtCacheKey;
import org.wso2.carbon.identity.flow.mgt.cache.GraphConfigCache;
import org.wso2.carbon.identity.flow.mgt.exception.FlowMgtFrameworkException;
import org.wso2.carbon.identity.flow.mgt.exception.FlowMgtServerException;
import org.wso2.carbon.identity.flow.mgt.model.FlowDTO;
import org.wso2.carbon.identity.flow.mgt.model.GraphConfig;
import org.wso2.carbon.identity.flow.mgt.utils.FlowMgtUtils;

import static org.wso2.carbon.identity.flow.mgt.Constants.DEFAULT_FLOW_NAME;

/**
 * Cache backed implementation of {@link FlowDAO}.
 * This class provides caching capabilities for flow management operations.
 */
public class CacheBackedFlowDAOImpl implements FlowDAO {

    private static final CacheBackedFlowDAOImpl INSTANCE = new CacheBackedFlowDAOImpl();
    private static final FlowDAOImpl FLOW_DAO = new FlowDAOImpl();

    private CacheBackedFlowDAOImpl() {

    }

    public static CacheBackedFlowDAOImpl getInstance() {

        return INSTANCE;
    }

    @Override
    public void updateFlow(String flowType, GraphConfig graphConfig, int tenantId, String flowName)
            throws FlowMgtFrameworkException {

        FlowMgtCacheKey flowMgtCacheKey = new FlowMgtCacheKey(flowType, flowName);
        FlowMgtCache.getInstance().clearCacheEntry(flowMgtCacheKey, tenantId);
        GraphConfigCache.getInstance().clearCacheEntry(flowMgtCacheKey, tenantId);
        FLOW_DAO.updateFlow(flowType, graphConfig, tenantId, flowName);
    }

    @Override
    public FlowDTO getFlow(String flowType, int tenantId) throws FlowMgtServerException {

        FlowMgtCacheKey flowMgtCacheKey = new FlowMgtCacheKey(flowType, DEFAULT_FLOW_NAME);
        FlowDTO cachedFlow = FlowMgtCache.getInstance().getValueFromCache(flowMgtCacheKey, tenantId);
        if (cachedFlow != null) {
            return cachedFlow;
        }
        FlowDTO flowDTO = FLOW_DAO.getFlow(flowType, tenantId);
        if (flowDTO != null) {
            FlowMgtCache.getInstance().addToCache(flowMgtCacheKey, flowDTO, tenantId);
        }
        return flowDTO;
    }

    @Override
    public void deleteFlow(String flowType, int tenantId) throws FlowMgtFrameworkException {

        FlowMgtCacheKey flowMgtCacheKey = new FlowMgtCacheKey(flowType, DEFAULT_FLOW_NAME);
        FlowMgtCache.getInstance().clearCacheEntry(flowMgtCacheKey, tenantId);
        GraphConfigCache.getInstance().clearCacheEntry(flowMgtCacheKey, tenantId);
        FLOW_DAO.deleteFlow(flowType, tenantId);
    }

    @Override
    public GraphConfig getGraphConfig(String flowType, int tenantId) throws FlowMgtFrameworkException {

        FlowMgtCacheKey flowMgtCacheKey = new FlowMgtCacheKey(flowType, DEFAULT_FLOW_NAME);
        GraphConfig cachedGraphConfig = GraphConfigCache.getInstance().getValueFromCache(flowMgtCacheKey, tenantId);
        if (cachedGraphConfig != null) {
            return cachedGraphConfig;
        }
        GraphConfig graphConfig = FLOW_DAO.getGraphConfig(flowType, tenantId);
        GraphConfigCache.getInstance().addToCache(flowMgtCacheKey, graphConfig, tenantId);
        return graphConfig;
    }
}
