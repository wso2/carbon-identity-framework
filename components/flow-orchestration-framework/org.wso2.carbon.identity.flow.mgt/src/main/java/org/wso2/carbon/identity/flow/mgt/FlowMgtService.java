/*
 * Copyright (c) 2025, WSO2 LLC. (https://www.wso2.com) All Rights Reserved.
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.identity.flow.mgt;

import static org.wso2.carbon.identity.central.log.mgt.utils.LoggerUtils.triggerAuditLogEvent;
import static org.wso2.carbon.identity.flow.mgt.Constants.DEFAULT_FLOW_NAME;
import static org.wso2.carbon.identity.flow.mgt.utils.FlowMgtUtils.getInitiatorId;
import static org.wso2.carbon.identity.flow.mgt.utils.FlowMgtUtils.isEnableV2AuditLogs;
import org.wso2.carbon.identity.central.log.mgt.utils.LogConstants;
import org.wso2.carbon.identity.central.log.mgt.utils.LoggerUtils;
import org.wso2.carbon.identity.flow.mgt.dao.FlowDAO;
import org.wso2.carbon.identity.flow.mgt.dao.FlowDAOImpl;
import org.wso2.carbon.identity.flow.mgt.exception.FlowMgtFrameworkException;
import org.wso2.carbon.identity.flow.mgt.model.FlowDTO;
import org.wso2.carbon.identity.flow.mgt.model.GraphConfig;
import org.wso2.carbon.identity.flow.mgt.utils.GraphBuilder;
import org.wso2.carbon.utils.AuditLog;

/**
 * This class is responsible for managing the flow.
 */
public class FlowMgtService {

    private static final FlowMgtService instance = new FlowMgtService();
    private static final FlowDAO flowDAO = new FlowDAOImpl();

    private FlowMgtService() {

    }

    public static FlowMgtService getInstance() {

        return instance;
    }

    /**
     * Update a specific flow of the given tenant.
     *
     * @param flowDTO  The flow.
     * @param tenantID The tenant ID.
     */
    public void updateFlow(FlowDTO flowDTO, int tenantID)
            throws FlowMgtFrameworkException {

        GraphConfig flowConfig = new GraphBuilder().withSteps(flowDTO.getSteps()).build();
        flowDAO.updateFlow(flowDTO.getFlowType(), flowConfig, tenantID, DEFAULT_FLOW_NAME);
        AuditLog.AuditLogBuilder auditLogBuilder =
                new AuditLog.AuditLogBuilder(getInitiatorId(), LoggerUtils.getInitiatorType(getInitiatorId()),
                                             flowConfig.getId(),
                                             LoggerUtils.Target.Flow.name(),
                        String.format("%s%s", LogConstants.FlowManagement.UPDATE_FLOW, flowDTO.getFlowType()));
        triggerAuditLogEvent(auditLogBuilder);
    }

    /**
     * Get the specified flow of the given tenant.
     *
     * @param tenantID The tenant ID.
     * @return The flow.
     * @throws FlowMgtFrameworkException If an error occurs while retrieving the default flow.
     */
    public FlowDTO getFlow(String flowType, int tenantID) throws FlowMgtFrameworkException {

        return flowDAO.getFlow(flowType, tenantID);
    }

    /**
     * Get the graph config by tenant ID.
     *
     * @param tenantID The tenant ID.
     */
    public GraphConfig getGraphConfig(String flowType, int tenantID) throws FlowMgtFrameworkException {

        return flowDAO.getGraphConfig(flowType, tenantID);
    }
}
