/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.user.mgt.ui;


import org.apache.axis2.AxisFault;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.user.mgt.UserManagementWorkflowStub;

import java.rmi.RemoteException;

public class UserManagementWorkflowServiceClient {

    private UserManagementWorkflowStub service;
    private static final Log log = LogFactory.getLog(UserManagementWorkflowServiceClient.class);

    /**
     * @param cookie
     * @param backendServerURL
     * @param configCtx
     * @throws org.apache.axis2.AxisFault
     */
    public UserManagementWorkflowServiceClient(String cookie, String backendServerURL,
                                               ConfigurationContext configCtx) throws AxisFault {

        try{
            service = (UserManagementWorkflowStub) PrivilegedCarbonContext.getThreadLocalCarbonContext().getOSGiService
                    (UserManagementWorkflowStub.class, null);
            if (service != null) {
                service.initiateUserManagementWorkflowServiceClient(cookie, backendServerURL, configCtx);
            }
        } catch (NullPointerException e) {
            log.debug("UserManagementWorkflowServiceClient is not available. Hence, not initializing the service client.");
        }
    }

    /**
     * @param wfOperationType Operation Type of the Work-flow.
     * @param wfStatus        Current Status of the Work-flow.
     * @param entityType      Entity Type of the Work-flow.
     * @param entityIdFilter        Entity ID filter to search
     * @return
     */
    public String[] listAllEntityNames(String wfOperationType, String wfStatus, String entityType,
                                       String entityIdFilter) throws RemoteException {

        String[] entityNames = (service != null) ? service.listAllEntityNames(wfOperationType, wfStatus, entityType, entityIdFilter)
                : new String[0];
        return (entityNames != null) ? entityNames : new String[0];
    }
}
