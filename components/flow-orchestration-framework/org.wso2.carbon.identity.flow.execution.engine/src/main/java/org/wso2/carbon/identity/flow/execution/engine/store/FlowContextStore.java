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

package org.wso2.carbon.identity.flow.execution.engine.store;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.core.util.IdentityUtil;
import org.wso2.carbon.identity.flow.execution.engine.dao.FlowContextStoreDAO;
import org.wso2.carbon.identity.flow.execution.engine.dao.FlowContextStoreDAOImpl;
import org.wso2.carbon.identity.flow.execution.engine.exception.FlowEngineException;
import org.wso2.carbon.identity.flow.execution.engine.model.FlowExecutionContext;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Service to manage storing and fetching FlowExecutionContext objects.
 */
public class FlowContextStore {

    private static final Log LOG = LogFactory.getLog(FlowContextStore.class);
    private static final FlowContextStoreDAO flowContextStoreDAO = new FlowContextStoreDAOImpl();

    private static final String DEFAULT_TTL_CONFIG_KEY = "FlowExecution.DefaultFlowContextTTLSeconds";
    private static final long DEFAULT_TTL_FALLBACK_SECONDS = 900L;

    private static final Map<String, Long> FLOW_TYPE_TTL_MAP = new HashMap<>();

    private static final long DEFAULT_TTL_SECONDS = loadDefaultTTL();

    private static final FlowContextStore instance = new FlowContextStore();

    private FlowContextStore() {

    }

    /**
     * Get the singleton instance of FlowContextStore.
     *
     * @return FlowContextStore instance.
     */
    public static FlowContextStore getInstance() {

        return instance;
    }

    /**
     * Store a FlowExecutionContext with a TTL based on flow type.
     *
     * @param context FlowExecutionContext to store.
     * @throws FlowEngineException if an error occurs while storing the context.
     */
    public void storeContext(FlowExecutionContext context) throws FlowEngineException {

        String flowType = context.getFlowType();
        long ttl = resolveTTL(flowType);
        if (LOG.isDebugEnabled()) {
            LOG.debug("Storing context: " + context.getContextIdentifier() + " with TTL: " + ttl + "s");
        }
        flowContextStoreDAO.storeContext(context, ttl);
    }

    /**
     * Retrieve a FlowExecutionContext by its context ID.
     *
     * @param contextId ID of the FlowExecutionContext to retrieve.
     * @return Optional containing FlowExecutionContext if found.
     * @throws FlowEngineException if an error occurs while retrieving the context.
     */
    public Optional<FlowExecutionContext> getContext(String contextId) throws FlowEngineException {

        return Optional.ofNullable(flowContextStoreDAO.getContext(contextId));
    }

    /**
     * Delete a FlowExecutionContext by its context ID.
     *
     * @param contextId ID of the FlowExecutionContext to delete.
     * @throws FlowEngineException if an error occurs while deleting the context.
     */
    public void deleteContext(String contextId) throws FlowEngineException {

        if (LOG.isDebugEnabled()) {
            LOG.debug("Deleting context: " + contextId);
        }
        flowContextStoreDAO.deleteContext(contextId);
    }

    /**
     * Cleanup expired FlowExecutionContexts.
     *
     * @throws FlowEngineException if an error occurs during cleanup.
     */
    public void cleanupExpiredContexts() throws FlowEngineException {

        if (LOG.isDebugEnabled()) {
            LOG.debug("Running expired flow context cleanup.");
        }
        flowContextStoreDAO.cleanupExpiredContexts();
    }

    private long resolveTTL(String flowType) {

        return FLOW_TYPE_TTL_MAP.getOrDefault(flowType, DEFAULT_TTL_SECONDS);
    }

    private static long loadDefaultTTL() {

        String value = IdentityUtil.getProperty(DEFAULT_TTL_CONFIG_KEY);
        if (value != null) {
            try {
                return Long.parseLong(value);
            } catch (NumberFormatException e) {
                LOG.warn("Invalid TTL value for " + DEFAULT_TTL_CONFIG_KEY + ": " + value +
                        ". Falling back to default: " + DEFAULT_TTL_FALLBACK_SECONDS + " seconds.");
            }
        } else if (LOG.isDebugEnabled()) {
            LOG.debug("No config found for " + DEFAULT_TTL_CONFIG_KEY +
                    ". Using fallback: " + DEFAULT_TTL_FALLBACK_SECONDS + " seconds.");
        }
        return DEFAULT_TTL_FALLBACK_SECONDS;
    }
}
