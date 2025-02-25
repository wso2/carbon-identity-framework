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

package org.wso2.carbon.identity.user.registration.engine;

import java.util.Map;
import java.util.UUID;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.user.registration.engine.internal.RegistrationFlowEngineDataHolder;
import org.wso2.carbon.identity.user.registration.engine.model.RegistrationContext;
import org.wso2.carbon.identity.user.registration.engine.model.RegistrationStep;
import org.wso2.carbon.identity.user.registration.engine.util.RegistrationFlowEngineUtils;
import org.wso2.carbon.identity.user.registration.mgt.exception.RegistrationFrameworkException;
import org.wso2.carbon.identity.user.registration.mgt.exception.RegistrationServerException;
import org.wso2.carbon.identity.user.registration.mgt.model.RegistrationFlowConfig;
import org.wso2.carbon.user.api.UserStoreException;
import static org.wso2.carbon.identity.user.registration.engine.util.Constants.ErrorMessages.ERROR_CODE_REG_FLOW_NOT_FOUND;
import static org.wso2.carbon.identity.user.registration.engine.util.Constants.ErrorMessages.ERROR_CODE_TENANT_RESOLVE_FAILURE;
import static org.wso2.carbon.identity.user.registration.engine.util.Constants.STATUS_COMPLETE;
import static org.wso2.carbon.identity.user.registration.engine.util.RegistrationFlowEngineUtils.handleServerException;

/**
 * Service class to handle the user registration flow.
 */
public class UserRegistrationFlowService {

    private static final Log LOG = LogFactory.getLog(UserRegistrationFlowService.class);
    private static final UserRegistrationFlowService instance = new UserRegistrationFlowService();
    private static final int superTenantDomain = -1234;

    private UserRegistrationFlowService() {

    }

    public static UserRegistrationFlowService getInstance() {

        return instance;
    }

    /**
     * Initiates the registration flow for the given tenant domain.
     *
     * @param tenantDomain Tenant domain.
     * @return RegistrationStep.
     * @throws RegistrationFrameworkException if something goes wrong while initiating the registration flow.
     */
    public RegistrationStep initiateRegistrationFlow(String tenantDomain) throws RegistrationFrameworkException {

        String flowId = UUID.randomUUID().toString();
        RegistrationContext context = new RegistrationContext();
        try {
            int tenantId = RegistrationFlowEngineDataHolder.getInstance().getRealmService().getTenantManager()
                    .getTenantId(tenantDomain);
            RegistrationFlowConfig flowConfig =
                    RegistrationFlowEngineDataHolder.getInstance().getRegistrationFlowMgtService()
                            .getRegistrationFlowConfig(tenantId);

            if (flowConfig == null) {
                throw handleServerException(ERROR_CODE_REG_FLOW_NOT_FOUND, tenantDomain);
            }
            context.setTenantDomain(tenantDomain);
            context.setRegGraph(flowConfig);
            context.setContextIdentifier(flowId);
            RegistrationStep step = RegistrationFlowEngine.getInstance().execute(context, flowConfig);
            RegistrationFlowEngineUtils.addRegContextToCache(context);
            return step;
        } catch (UserStoreException e) {
            throw handleServerException(ERROR_CODE_TENANT_RESOLVE_FAILURE, tenantDomain);
        }
    }

    /**
     * Continues the registration flow for the given flow ID with the provided user inputs.
     *
     * @param flowId Flow ID.
     * @param inputs User inputs.
     * @return ExecutionState.
     * @throws RegistrationFrameworkException if something goes wrong while continuing the registration flow.
     */
    public RegistrationStep continueFlow(String flowId, Map<String, String> inputs)
            throws RegistrationFrameworkException {

        RegistrationContext context = RegistrationFlowEngineUtils.retrieveRegContextFromCache(flowId);
        RegistrationFlowConfig flowConfig = context.getRegGraph();
        RegistrationStep step = RegistrationFlowEngine.getInstance().execute(context, flowConfig);
        if (STATUS_COMPLETE.equals(step.getFlowStatus())) {
            RegistrationFlowEngineUtils.removeRegContextFromCache(flowId);
        } else {
            RegistrationFlowEngineUtils.addRegContextToCache(context);
        }
        return step;
    }
}
