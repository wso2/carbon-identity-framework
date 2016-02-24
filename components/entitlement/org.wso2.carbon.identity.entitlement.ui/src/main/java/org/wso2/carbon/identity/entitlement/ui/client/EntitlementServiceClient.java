/*
*  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.wso2.carbon.identity.entitlement.ui.client;

import org.apache.axis2.AxisFault;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.entitlement.stub.EntitlementServiceStub;
import org.wso2.carbon.identity.entitlement.stub.dto.EntitledResultSetDTO;

public class EntitlementServiceClient {

    private static final Log log = LogFactory.getLog(EntitlementServiceClient.class);
    private EntitlementServiceStub stub;

    /**
     * Instantiates EntitlementServiceClient
     *
     * @param cookie           For session management
     * @param backendServerURL URL of the back end server where EntitlementService is running.
     * @param configCtx        ConfigurationContext
     * @throws org.apache.axis2.AxisFault
     */
    public EntitlementServiceClient(String cookie, String backendServerURL,
                                    ConfigurationContext configCtx) throws AxisFault {
        String serviceURL = backendServerURL + "EntitlementService";
        stub = new EntitlementServiceStub(configCtx, serviceURL);
        ServiceClient client = stub._getServiceClient();
        Options option = client.getOptions();
        option.setManageSession(true);
        option.setProperty(org.apache.axis2.transport.http.HTTPConstants.COOKIE_STRING, cookie);
    }

    /**
     * Evaluate XACML request with PDP
     *
     * @param request XACML request as String
     * @return XACML response as String
     * @throws AxisFault if fails
     */
    public String getDecision(String request) throws AxisFault {
        try {
            return stub.getDecision(request);
        } catch (Exception e) {
            handleException("Error occurred while policy evaluation", e);
        }
        return null;
    }

    /**
     * Gets user or role entitled resources
     *
     * @param subjectName       user or role name
     * @param resourceName      resource name
     * @param subjectId         attribute id of the subject, user or role
     * @param action            action name
     * @param enableChildSearch whether search is done for the child resources under the given resource name
     * @return entitled resources as String array
     * @throws org.apache.axis2.AxisFault throws
     */
    public EntitledResultSetDTO getEntitledAttributes(String subjectName, String resourceName,
                                                      String subjectId, String action, boolean enableChildSearch)
            throws AxisFault {
        try {
            return stub.getEntitledAttributes(subjectName, resourceName, subjectId, action,
                    enableChildSearch);
        } catch (Exception e) {
            handleException(e.getMessage(), e);
        }

        return null;
    }

    /**
     * Logs and wraps the given exception.
     *
     * @param msg Error message
     * @param e   Exception
     * @throws AxisFault
     */
    private void handleException(String msg, Exception e) throws AxisFault {
        log.error(msg, e);
        throw new AxisFault(msg, e);
    }
}
