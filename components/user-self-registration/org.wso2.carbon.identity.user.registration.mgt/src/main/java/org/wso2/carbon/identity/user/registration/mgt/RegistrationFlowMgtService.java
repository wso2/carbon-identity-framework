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

package org.wso2.carbon.identity.user.registration.mgt;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.wso2.carbon.identity.user.registration.mgt.dao.RegistrationFlowDAO;
import org.wso2.carbon.identity.user.registration.mgt.model.RegistrationFlowConfig;
import org.wso2.carbon.identity.user.registration.mgt.model.RegistrationFlowDTO;

/**
 * This class is responsible for managing the registration flow.
 */
public class RegistrationFlowMgtService {

    private static final Log LOG = LogFactory.getLog(RegistrationFlowMgtService.class);
    private static final RegistrationFlowMgtService instance = new RegistrationFlowMgtService();

    private RegistrationFlowMgtService() {

    }

    public static RegistrationFlowMgtService getInstance() {

        return instance;
    }

    /**
     * Store the registration flow.
     *
     * @param flowDTO  The registration flow.
     * @param tenantID The tenant ID.
     */
    public void addRegistrationFlow(RegistrationFlowDTO flowDTO, int tenantID) {

//        try {
//            RegistrationFlowConfig flowConfig = FlowConvertor.getSequence(flowDTO);
//            RegistrationFlowDAO.getInstance().addRegistrationObject(flowConfig, tenantID);
//        } catch (IOException e) {
//            LOG.error("Error while converting the flow to a DTO.", e);
//            throw new RuntimeException(e);
//        }
        LOG.info("Adding registration flow for tenant: " + tenantID);
    }

    public RegistrationFlowDTO getRegistrationFlow(int tenantID) {

        return new RegistrationFlowDTO();
    }

    /**
     * Get the registration flow by tenant ID.
     *
     * @param tenantID The tenant ID.
     */
    public RegistrationFlowConfig getRegistrationFlowConfig(int tenantID) {

        // Call the DAO to retrieve the flow.
        return null;
    }
}
