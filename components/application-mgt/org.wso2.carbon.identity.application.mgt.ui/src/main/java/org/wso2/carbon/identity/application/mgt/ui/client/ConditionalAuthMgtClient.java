/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.wso2.carbon.identity.application.mgt.ui.client;

import org.apache.axis2.AxisFault;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.application.authentication.framework.stub.ConditionalAuthenticationMgtServiceStub;

import java.rmi.RemoteException;

public class ConditionalAuthMgtClient {

    private static final Log LOG = LogFactory.getLog(ApplicationManagementServiceClient.class);
    private ConditionalAuthenticationMgtServiceStub stub;

    public ConditionalAuthMgtClient(String cookie, String backendServerURL,
                                    ConfigurationContext configCtx) throws AxisFault {

        String serviceURL = backendServerURL + "ConditionalAuthenticationMgtService";
        stub = new ConditionalAuthenticationMgtServiceStub(configCtx, serviceURL);

        ServiceClient client = stub._getServiceClient();
        Options option = client.getOptions();
        option.setManageSession(true);
        option.setProperty(org.apache.axis2.transport.http.HTTPConstants.COOKIE_STRING, cookie);
    }

    public String[] listAvailableFunctions() {

        try {
            return stub.getAllAvailableFunctions();
        } catch (RemoteException e) {
            LOG.error("Error occured when listing conditional authentication functions.", e);
        }
        return ArrayUtils.EMPTY_STRING_ARRAY;
    }

}
