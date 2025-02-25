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

import static org.wso2.carbon.identity.user.registration.mgt.Constants.DEFAULT_FLOW_NAME;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.user.registration.mgt.dao.RegistrationFlowDAO;
import org.wso2.carbon.identity.user.registration.mgt.dao.RegistrationFlowDAOImpl;
import org.wso2.carbon.identity.user.registration.mgt.exception.RegistrationFrameworkException;
import org.wso2.carbon.identity.user.registration.mgt.model.RegistrationFlowDTO;
import org.wso2.carbon.identity.user.registration.mgt.model.RegistrationGraphConfig;
import org.wso2.carbon.identity.user.registration.mgt.utils.GraphBuilder;

/**
 * This class is responsible for managing the registration flow.
 */
public class RegistrationFlowMgtService {

    private static final Log LOG = LogFactory.getLog(RegistrationFlowMgtService.class);
    private static final RegistrationFlowMgtService instance = new RegistrationFlowMgtService();
    private static final RegistrationFlowDAO registrationFlowDAO = new RegistrationFlowDAOImpl();

    private RegistrationFlowMgtService() {

    }

    public static RegistrationFlowMgtService getInstance() {

        return instance;
    }

    /**
     * Update the default registration flow of the given tenant.
     *
     * @param flowDTO  The registration flow.
     * @param tenantID The tenant ID.
     */
    public void updateDefaultRegistrationFlow(RegistrationFlowDTO flowDTO, int tenantID)
            throws RegistrationFrameworkException {

        RegistrationGraphConfig flowConfig = GraphBuilder.convert(flowDTO);
        registrationFlowDAO.updateDefaultRegistrationFlowByTenant(flowConfig, tenantID, DEFAULT_FLOW_NAME);
    }

    /**
     * Get the default registration flow of the given tenant.
     *
     * @param tenantID The tenant ID.
     * @return The registration flow.
     * @throws RegistrationFrameworkException If an error occurs while retrieving the default flow.
     */
    public RegistrationFlowDTO getRegistrationFlow(int tenantID) throws RegistrationFrameworkException {

        return registrationFlowDAO.getDefaultRegistrationFlowByTenant(tenantID);
    }

    /**
     * Get the registration flow by tenant ID.
     *
     * @param tenantID The tenant ID.
     */
    public RegistrationGraphConfig getRegistrationFlowConfig(int tenantID) {

        // TODO: Implement this method.
        return null;
    }
}
