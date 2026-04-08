/*
 * Copyright (c) 2026, WSO2 LLC. (https://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.identity.flow.mgt.utils;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.flow.mgt.Constants;
import org.wso2.carbon.identity.flow.mgt.exception.FlowMgtFrameworkException;
import org.wso2.carbon.identity.flow.mgt.exception.FlowMgtServerException;
import org.wso2.carbon.identity.flow.mgt.model.FlowDTO;
import org.wso2.carbon.identity.flow.mgt.model.GraphConfig;
import org.wso2.carbon.utils.CarbonUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import static org.wso2.carbon.identity.flow.mgt.Constants.ErrorMessages.ERROR_CODE_LOAD_SYSTEM_DEFAULT_FLOW;
import static org.wso2.carbon.identity.flow.mgt.utils.FlowMgtUtils.handleServerException;

/**
 * Provider for system default flows loaded from JSON configuration files located in the Carbon conf directory
 * under {@code <carbon-home>/repository/conf/flow-defaults/{flowType}.json}. System default flows serve as
 * fallbacks when no tenant-specific or org-hierarchy-inherited flow exists for a given flow type.
 */
public class SystemDefaultFlowProvider {

    private static final Log LOG = LogFactory.getLog(SystemDefaultFlowProvider.class);

    private static final SystemDefaultFlowProvider INSTANCE = new SystemDefaultFlowProvider();
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    private static final String FLOW_DEFAULTS_DIR = "flow-defaults";

    /**
     * Cache for loaded FlowDTOs per flow type.
     * Optional.empty() is stored when no default exists, preventing repeated file system lookups.
     */
    private final Map<String, Optional<FlowDTO>> flowDTOCache = new ConcurrentHashMap<>();

    private SystemDefaultFlowProvider() {

    }

    public static SystemDefaultFlowProvider getInstance() {

        return INSTANCE;
    }

    /**
     * Get the system default FlowDTO for the given flow type.
     * Returns {@code null} if no system default is configured for this flow type.
     * Each call returns a deep copy of the cached instance so callers cannot mutate shared state.
     *
     * @param flowType The flow type identifier (e.g. "INVITED_USER_REGISTRATION").
     * @return A deep copy of the cached FlowDTO, or {@code null} if not found.
     * @throws FlowMgtServerException If an error occurs while loading or copying the default flow.
     */
    public FlowDTO getSystemDefaultFlow(String flowType) throws FlowMgtServerException {

        if (LOG.isDebugEnabled()) {
            LOG.debug("Retrieving system default flow for flow type: " + flowType);
        }
        if (!flowDTOCache.containsKey(flowType)) {
            flowDTOCache.put(flowType, Optional.ofNullable(loadFlowFromResource(flowType)));
        }
        FlowDTO cached = flowDTOCache.get(flowType).orElse(null);
        if (cached == null) {
            return null;
        }
        try {
            return OBJECT_MAPPER.readValue(OBJECT_MAPPER.writeValueAsBytes(cached), FlowDTO.class);
        } catch (IOException e) {
            throw handleServerException(ERROR_CODE_LOAD_SYSTEM_DEFAULT_FLOW, e, flowType);
        }
    }

    /**
     * Get the system default GraphConfig for the given flow type.
     * Returns {@code null} if no system default is configured for this flow type.
     * The GraphConfig is built fresh on each call from a newly deserialized FlowDTO.
     *
     * @param flowType The flow type identifier (e.g. "INVITED_USER_REGISTRATION").
     * @return A new GraphConfig instance, or {@code null} if not found.
     * @throws FlowMgtServerException If an error occurs while loading or building the default flow graph.
     */
    public GraphConfig getSystemDefaultGraphConfig(String flowType) throws FlowMgtServerException {

        FlowDTO flowDTO = getSystemDefaultFlow(flowType);
        if (flowDTO == null) {
            return null;
        }
        try {
            return new GraphBuilder().withSteps(flowDTO.getSteps()).build();
        } catch (FlowMgtFrameworkException e) {
            throw handleServerException(ERROR_CODE_LOAD_SYSTEM_DEFAULT_FLOW, e, flowType);
        }
    }

    private FlowDTO loadFlowFromResource(String flowType) throws FlowMgtServerException {

        boolean isKnownFlowType = false;
        for (Constants.FlowTypes knownType : Constants.FlowTypes.values()) {
            if (knownType.getType().equals(flowType)) {
                isKnownFlowType = true;
                break;
            }
        }
        if (!isKnownFlowType) {
            LOG.warn("Rejected unknown flow type for system default flow lookup: " + flowType);
            return null;
        }
        File configFile = new File(CarbonUtils.getCarbonConfigDirPath(), FLOW_DEFAULTS_DIR + File.separator
                + flowType + ".json");
        if (!configFile.exists()) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("No system default flow configuration found for flow type: " + flowType
                        + " at path: " + configFile.getAbsolutePath());
            }
            return null;
        }
        try (InputStream inputStream = new FileInputStream(configFile)) {
            FlowDTO flowDTO = OBJECT_MAPPER.readValue(inputStream, FlowDTO.class);
            flowDTO.setFlowType(flowType);
            LOG.info("Successfully loaded system default flow for flow type: " + flowType);
            return flowDTO;
        } catch (IOException e) {
            throw handleServerException(ERROR_CODE_LOAD_SYSTEM_DEFAULT_FLOW, e, flowType);
        }
    }
}
