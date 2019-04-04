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
import org.wso2.carbon.identity.mgt.beans.User;
import org.wso2.carbon.identity.mgt.endpoint.IdentityManagementEndpointConstants;
import org.wso2.carbon.identity.mgt.endpoint.IdentityManagementEndpointUtil;
import org.wso2.carbon.identity.mgt.endpoint.IdentityManagementServiceUtil;
import org.wso2.carbon.identity.mgt.endpoint.serviceclient.beans.Claim;
import org.wso2.carbon.identity.mgt.endpoint.serviceclient.beans.ResetPasswordRequest;
import org.wso2.carbon.identity.mgt.endpoint.serviceclient.client.proxy.api.PasswordRecoveryNotification;
import org.wso2.carbon.identity.mgt.endpoint.serviceclient.client.proxy.api.UsernameRecoveryNotification;

import javax.ws.rs.core.Response;

/**
 * REST client for user information recovery with email notification
 */
public class UserInfoRecoveryWithNotificationClient {

    private static final String ENDPOINT_URL = IdentityManagementEndpointUtil.buildEndpointUrl(
                    IdentityManagementEndpointConstants.UserInfoRecovery.REST_API_URL_DOMAIN);

    // Validate user and returns a new key through a notification
    public Response sendPasswordRecoveryNotification(User user) {

        PasswordRecoveryNotification passwordRecoveryNotification = JAXRSClientFactory
                .create(ENDPOINT_URL, PasswordRecoveryNotification.class,
                        IdentityManagementServiceUtil.getInstance().getJSONProvider());
        Response response = passwordRecoveryNotification.sendPasswordRecoveryNotification(user);
        return response;
    }

    // Updates the password of the user
    public Response resetPassword(ResetPasswordRequest resetPasswordRequest) {

        PasswordRecoveryNotification passwordRecoveryNotification = JAXRSClientFactory
                .create(ENDPOINT_URL, PasswordRecoveryNotification.class,
                        IdentityManagementServiceUtil.getInstance().getJSONProvider());
        Response response = passwordRecoveryNotification.resetPassword(resetPasswordRequest);
        return response;
    }

    // Returns the supported claims
    public Response getAllLocalSupportedClaims() {
        UsernameRecoveryNotification usernameRecoveryNotification =
                JAXRSClientFactory.create(ENDPOINT_URL, UsernameRecoveryNotification.class,
                        IdentityManagementServiceUtil.getInstance().getJSONProvider());
        Response response = usernameRecoveryNotification.getAllLocalSupportedClaims();
        return response;
    }

    // Send a email notification for username recovery
    public Response sendUserNameRecoveryNotification(Claim[] userClaims, String tenantDomain) {

        UsernameRecoveryNotification usernameRecoveryNotification =
                JAXRSClientFactory.create(ENDPOINT_URL, UsernameRecoveryNotification.class,
                        IdentityManagementServiceUtil.getInstance().getJSONProvider());
        Response response = usernameRecoveryNotification.sendUsernameRecoveryNotification(userClaims, tenantDomain);
        return response;
    }


}
