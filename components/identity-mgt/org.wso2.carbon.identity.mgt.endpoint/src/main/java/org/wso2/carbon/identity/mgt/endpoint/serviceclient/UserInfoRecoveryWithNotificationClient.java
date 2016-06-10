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
package org.wso2.carbon.identity.mgt.endpoint.serviceclient;

import org.apache.cxf.jaxrs.client.JAXRSClientFactory;
import org.apache.cxf.jaxrs.provider.json.JSONProvider;
import org.wso2.carbon.identity.mgt.endpoint.IdentityManagementEndpointConstants;
import org.wso2.carbon.identity.mgt.endpoint.serviceclient.beans.ResetPasswordRequest;
import org.wso2.carbon.identity.mgt.endpoint.serviceclient.beans.User;
import org.wso2.carbon.identity.mgt.endpoint.serviceclient.client.proxy.api.UserInfoRecoveryNotification;

import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UserInfoRecoveryWithNotificationClient {

    // Validate user and returns a new key through a notification
    public Response sendRecoveryNotification(String username, String tenantDomain, String userStoreDomain) {

        String url = IdentityManagementEndpointConstants.UserInfoRecovery.REST_URL;

        List providers = new ArrayList();

        JSONProvider jsonProvider = new JSONProvider();
        jsonProvider.setDropRootElement(true);
        jsonProvider.setIgnoreNamespaces(true);

        Map<String, String> map = new HashMap<>();
        jsonProvider.setNamespaceMap(map);
        providers.add(jsonProvider);

        UserInfoRecoveryNotification userInfoRecoveryNotification = JAXRSClientFactory.create(url, UserInfoRecoveryNotification.class, providers, false);

        User user = new User();
        user.setUserName(username);
        user.setTenantDomain(tenantDomain);
        user.setUserStoreDomain(userStoreDomain);

        Response response = userInfoRecoveryNotification.sendEmailNotification(user);

        return response;
    }

    // Updates the password in the system. Need to provide the key from notification, new password
    public Response resetPassword(String username, String tenantDomain, String userStoreDomain, String code, String password) {
        String url = IdentityManagementEndpointConstants.UserInfoRecovery.REST_URL;

        List providers = new ArrayList();

        JSONProvider jsonProvider = new JSONProvider();
        jsonProvider.setDropRootElement(true);
        jsonProvider.setIgnoreNamespaces(true);
        Map<String, String> map = new HashMap<>();
        jsonProvider.setNamespaceMap(map);
        providers.add(jsonProvider);

        UserInfoRecoveryNotification userInfoRecoveryNotification = JAXRSClientFactory.create(url, UserInfoRecoveryNotification.class, providers, false);

        User user = new User();
        user.setUserName(username);
        user.setTenantDomain(tenantDomain);
        user.setUserStoreDomain(userStoreDomain);

        ResetPasswordRequest resetPasswordRequest = new ResetPasswordRequest();
        resetPasswordRequest.setUser(user);
        resetPasswordRequest.setCode(code);
        resetPasswordRequest.setPassword(password);

        Response response = userInfoRecoveryNotification.resetPassword(resetPasswordRequest);

        return response;
    }


}
