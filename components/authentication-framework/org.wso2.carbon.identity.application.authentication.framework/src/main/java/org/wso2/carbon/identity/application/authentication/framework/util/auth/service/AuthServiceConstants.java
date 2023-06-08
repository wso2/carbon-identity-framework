/*
 * Copyright (c) 2023, WSO2 LLC. (https://www.wso2.com) All Rights Reserved.
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.identity.application.authentication.framework.util.auth.service;

/**
 * Constants class for Auth Service.
 */
public class AuthServiceConstants {

    /**
     * Enum for flow status.
     */
    public enum FlowStatus {

        SUCCESS_COMPLETED,
        FAIL_COMPLETED,
        FAIL_INCOMPLETE,
        INCOMPLETE
    }

    public static final String AUTH_SERVICE_AUTH_INITIATION_DATA = "authServiceAuthInitiationData";
    public static final String AUTHENTICATORS = "authenticators";
    public static final String FLOW_ID = "flowId";
    public static final String AUTHENTICATOR_SEPARATOR = ";";
    public static final String AUTHENTICATOR_IDP_SEPARATOR = ":";
    public static final String AUTH_FAILURE_PARAM = "authFailure";
    public static final String AUTH_FAILURE_MSG_PARAM = "authFailureMsg";
    public static final String ERROR_CODE_PARAM = "errorCode";
    public static final String ERROR_CODE_UNKNOWN_ERROR = "UNKNOWN_ERROR";
    public static final String ERROR_MSG_UNKNOWN_ERROR = "Unknown error occurred.";

}
