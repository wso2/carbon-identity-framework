/*
 * Copyright (c) 2024, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.carbon.identity.action.management.util;

import org.wso2.carbon.identity.action.management.model.Action;
import org.wso2.carbon.identity.action.management.model.Authentication;
import org.wso2.carbon.identity.action.management.model.EndpointConfig;
import org.wso2.carbon.identity.action.management.model.PreUpdatePasswordAction;
import org.wso2.carbon.identity.action.management.model.PreUpdatePasswordAction.PasswordFormat;
import org.wso2.carbon.identity.certificate.management.model.Certificate;

import java.util.UUID;

/**
 * Utility class for Action Management Tests.
 */
public class TestUtil {

    public static final int TENANT_ID = 2;
    public static final String TENANT_DOMAIN = "wso2.com";

    public static final String PRE_ISSUE_ACCESS_TOKEN_TYPE = Action.ActionTypes.PRE_ISSUE_ACCESS_TOKEN.getActionType();
    public static final String PRE_UPDATE_PASSWORD_TYPE = Action.ActionTypes.PRE_UPDATE_PASSWORD.getActionType();

    public static final String PRE_ISSUE_ACCESS_TOKEN_ACTION_ID = String.valueOf(UUID.randomUUID());
    public static final String PRE_UPDATE_PASSWORD_ACTION_ID = String.valueOf(UUID.randomUUID());
    public static final String CERTIFICATE_ID = String.valueOf(UUID.randomUUID());
    public static final String CERTIFICATE_NAME = "ACTIONS:" + PRE_UPDATE_PASSWORD_ACTION_ID;
    public static final String CERTIFICATE = "sample-certificate";
    public static final String UPDATED_CERTIFICATE = "updated-sample-certificate";

    public static Action buildMockAction(String name,
                                         String description,
                                         String uri,
                                         Authentication authentication) {

        return new Action.ActionRequestBuilder()
                .name(name)
                .description(description)
                .endpoint(buildMockEndpointConfig(uri, authentication))
                .build();
    }

    public static PreUpdatePasswordAction buildMockPreUpdatePasswordAction(String name, String description, String uri,
                                                                           Authentication authentication,
                                                                           PasswordFormat passwordSharingFormat,
                                                                           String certificate) {

        return new PreUpdatePasswordAction.RequestBuilder()
                .name(name)
                .description(description)
                .endpoint(buildMockEndpointConfig(uri, authentication))
                .passwordSharingFormat(passwordSharingFormat)
                .certificate(new Certificate.Builder().certificateContent(certificate).build())
                .build();
    }

    public static Authentication buildMockBasicAuthentication(String username, String password) {

        return new Authentication.BasicAuthBuilder(username, password).build();
    }

    public static Authentication buildMockBearerAuthentication(String accessToken) {

        return new Authentication.BearerAuthBuilder(accessToken).build();
    }

    public static Authentication buildMockNoneAuthentication() {

        return new Authentication.NoneAuthBuilder().build();
    }

    private static EndpointConfig buildMockEndpointConfig(String uri, Authentication authentication) {

        if (uri == null && authentication == null) {
            return null;
        }

        return new EndpointConfig.EndpointConfigBuilder()
                .uri(uri)
                .authentication(authentication)
                .build();
    }
}
