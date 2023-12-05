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
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.user.mgt.workflow.stub.UserManagementWorkflowServiceStub;
import org.wso2.carbon.user.mgt.workflow.stub.UserManagementWorkflowServiceWorkflowExceptionException;

import java.rmi.RemoteException;

public class UserManagementWorkflowServiceClient {

    private UserManagementWorkflowServiceStub stub;
    private static final Log log = LogFactory.getLog(UserManagementWorkflowServiceClient.class);

    /**
     * @param cookie
     * @param backendServerURL
     * @param configCtx
     * @throws org.apache.axis2.AxisFault
     */
    public UserManagementWorkflowServiceClient(String cookie, String backendServerURL,
                                               ConfigurationContext configCtx) throws AxisFault {

        String serviceURL = backendServerURL + "UserManagementWorkflowService";
        stub = new UserManagementWorkflowServiceStub(configCtx, serviceURL);

        ServiceClient client = stub._getServiceClient();
        Options option = client.getOptions();
        option.setManageSession(true);
        option.setProperty(org.apache.axis2.transport.http.HTTPConstants.COOKIE_STRING, cookie);
    }

    /**
     * @param wfOperationType Operation Type of the Work-flow.
     * @param wfStatus        Current Status of the Work-flow.
     * @param entityType      Entity Type of the Work-flow.
     * @param entityIdFilter        Entity ID filter to search
     * @return
     * @throws java.rmi.RemoteException
     * @throws org.wso2.carbon.user.mgt.workflow.stub.UserManagementWorkflowServiceWorkflowExceptionException
     */

    public String[] listAllEntityNames(String wfOperationType, String wfStatus, String entityType, String
            entityIdFilter) throws RemoteException, UserManagementWorkflowServiceWorkflowExceptionException {

        String[] entityNames = stub.listAllEntityNames(wfOperationType, wfStatus, entityType, entityIdFilter);
        if (entityNames == null) {
            entityNames = new String[0];
        }
        return entityNames;
    }
}
