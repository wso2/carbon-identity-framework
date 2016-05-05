/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.identity.mgt.endpoint;

import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.transport.http.HttpTransportProperties;

/**
 * This class is is used to authenticate for admin services.
 */
public class ServiceAuthenticator {

    private static ServiceAuthenticator instance = new ServiceAuthenticator();

    private String serviceAccessUsername = null;
    private String serviceAccessPassword = null;

    private ServiceAuthenticator() {
    }

    public static ServiceAuthenticator getInstance() {
        return instance;
    }

    public void authenticate(ServiceClient client) {
        Options option = client.getOptions();
        HttpTransportProperties.Authenticator auth = new HttpTransportProperties.Authenticator();
        auth.setUsername(serviceAccessUsername);
        auth.setPassword(serviceAccessPassword);
        auth.setPreemptiveAuthentication(true);
        option.setProperty(org.apache.axis2.transport.http.HTTPConstants.AUTHENTICATE, auth);
        option.setManageSession(true);
    }

    public void setServiceAccessUsername(String serviceAccessUsername) {
        this.serviceAccessUsername = serviceAccessUsername;
    }

    public void setServiceAccessPassword(String serviceAccessPassword) {
        this.serviceAccessPassword = serviceAccessPassword;
    }
}
