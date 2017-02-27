/*
 *
 *  Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 *
 */

package org.wso2.carbon.identity.mgt.endpoint.client.implementation;

import org.wso2.carbon.identity.mgt.endpoint.client.ApiException;
import org.wso2.carbon.identity.mgt.endpoint.client.api.NotificationApi;
import org.wso2.carbon.identity.mgt.endpoint.client.model.RecoveryInitiatingRequest;
import org.wso2.carbon.identity.mgt.endpoint.client.model.User;

public class NotificationSendingImpl {
    public static void main(String[] args) {
        System.out.println("test");
        NotificationApi notificationApi = new NotificationApi();
        User user = new User();
        user.setUsername("admin");

        RecoveryInitiatingRequest recoveryInitiatingRequest = new RecoveryInitiatingRequest();
        recoveryInitiatingRequest.setUser(user);

        try {
            notificationApi.recoverPasswordPost(recoveryInitiatingRequest, null, null);
        } catch (ApiException e) {
            e.printStackTrace();
        }
    }
}
