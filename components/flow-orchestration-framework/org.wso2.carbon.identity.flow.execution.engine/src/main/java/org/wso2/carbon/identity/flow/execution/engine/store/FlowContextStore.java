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

import org.apache.axiom.om.OMElement;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.core.util.IdentityConfigParser;
import org.wso2.carbon.identity.core.util.IdentityUtil;
import org.wso2.carbon.identity.flow.execution.engine.Constants.FlowExecutionConfigs;
import org.wso2.carbon.identity.flow.execution.engine.dao.FlowContextStoreDAO;
import org.wso2.carbon.identity.flow.execution.engine.dao.FlowContextStoreDAOImpl;
import org.wso2.carbon.identity.flow.execution.engine.exception.FlowEngineException;
import org.wso2.carbon.identity.flow.execution.engine.model.FlowExecutionContext;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;

import javax.xml.namespace.QName;

/**
 * Service to manage storing and fetching FlowExecutionContext objects.
 */
public class FlowContextStore {

    private static final Log LOG = LogFactory.getLog(FlowContextStore.class);
    private static final FlowContextStoreDAO flowContextStoreDAO = new FlowContextStoreDAOImpl();
    private static final long DEFAULT_TTL_FALLBACK_MINUTES = 30L;

    private static final Map<String, Long> FLOW_TYPE_TTL_MAP = loadFlowTypeTTLMap();
    private static final long DEFAULT_TTL_MINUTES = loadDefaultTTL();
    private static final FlowContextStore INSTANCE = new FlowContextStore();

    private FlowContextStore() {

    }

    public static FlowContextStore getInstance() {

        return INSTANCE;
    }

    public void storeContext(FlowExecutionContext context) throws FlowEngineException {

        storeContext(context.getContextIdentifier(), context);
    }

    public void storeContext(String contextIdentifier, FlowExecutionContext context) throws FlowEngineException {

        String flowType = context.getFlowType();
        long ttlSecs = resolveTTL(flowType) * 60;

        if (LOG.isDebugEnabled()) {
            LOG.debug("Storing context: " + contextIdentifier + " with TTL: " + ttlSecs + " secs");
        }

        flowContextStoreDAO.storeContext(contextIdentifier, context, ttlSecs);
    }

    public Optional<FlowExecutionContext> getContext(String contextId) throws FlowEngineException {

        return Optional.ofNullable(flowContextStoreDAO.getContext(contextId));
    }

    public void deleteContext(String contextId) throws FlowEngineException {

        LOG.debug("Deleting context: " + contextId);
        flowContextStoreDAO.deleteContext(contextId);
    }

    private long resolveTTL(String flowType) {

        return FLOW_TYPE_TTL_MAP.getOrDefault(flowType, DEFAULT_TTL_MINUTES);
    }

    private static long loadDefaultTTL() {

        String value = IdentityUtil.getProperty(FlowExecutionConfigs.DEFAULT_TTL_PROPERTY);
        if (StringUtils.isNotBlank(value)) {
            try {
                return Long.parseLong(value);
            } catch (NumberFormatException e) {
                LOG.warn("Invalid TTL value for " + FlowExecutionConfigs.DEFAULT_TTL_PROPERTY +
                        ": " + value + ". Falling back to hard-coded default: " + DEFAULT_TTL_FALLBACK_MINUTES + " " +
                        "minutes.");
            }
        } else if (LOG.isDebugEnabled()) {
            LOG.debug("No config found for " + FlowExecutionConfigs.DEFAULT_TTL_PROPERTY +
                    ". Using fallback: " + DEFAULT_TTL_FALLBACK_MINUTES + " minutes.");
        }
        return DEFAULT_TTL_FALLBACK_MINUTES;
    }

    private static Map<String, Long> loadFlowTypeTTLMap() {

        Map<String, Long> flowTypeTTLMap = new HashMap<>();
        OMElement element = IdentityConfigParser.getInstance()
                .getConfigElement(FlowExecutionConfigs.FLOW_EXECUTION_PROPERTY);

        if (element == null) {
            return flowTypeTTLMap;
        }

        Iterator<OMElement> flowTypeTTLs = element.getChildrenWithLocalName(
                FlowExecutionConfigs.FLOW_TYPE_TTL_CONFIG_KEY_PREFIX);

        if (!flowTypeTTLs.hasNext()) {
            return flowTypeTTLMap;
        }

        OMElement flowTypeTTLsElement = flowTypeTTLs.next();
        Iterator<OMElement> flowTypeElements = flowTypeTTLsElement.getChildrenWithName(
                new QName(FlowExecutionConfigs.FLOW_TYPE_TTL_CONFIG_KEY));

        while (flowTypeElements.hasNext()) {
            OMElement flowTypeElement = flowTypeElements.next();
            String flowType = flowTypeElement.getAttributeValue(new QName(FlowExecutionConfigs.FLOW_TYPE_ATTRIBUTE));
            String ttlStr = flowTypeElement.getText();

            if (StringUtils.isNotBlank(flowType) && StringUtils.isNotBlank(ttlStr)) {
                try {
                    long ttl = Long.parseLong(ttlStr);
                    flowTypeTTLMap.put(flowType, ttl);
                } catch (NumberFormatException e) {
                    LOG.warn("Invalid TTL value for flow type: " + flowType + ". Using default TTL.");
                }
            }
        }

        return flowTypeTTLMap;
    }
}
