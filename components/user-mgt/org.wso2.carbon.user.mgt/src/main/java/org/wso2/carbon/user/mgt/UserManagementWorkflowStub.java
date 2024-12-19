/*
 * Copyright (c) 2023, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.carbon.user.mgt;

import org.apache.axis2.AxisFault;
import org.apache.axis2.context.ConfigurationContext;

import java.rmi.RemoteException;

/**
 * This interface is used to check Workflow related services.
 */
public interface UserManagementWorkflowStub {

    public void initiateUserManagementWorkflowServiceClient(String Cookie, String backendServerURL,
                                                            ConfigurationContext configCtx) throws AxisFault;

    public String[] listAllEntityNames(String wfOperationType,
                                       String wfStatus, String entityType, String entityIdFilter) throws RemoteException;
}
