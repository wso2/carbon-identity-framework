/*
 * Copyright (c) 2025, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.carbon.identity.debug.framework.core;

/**
 * Constants for the Debug Framework.
 * Provides centralized definition of all string constants used throughout the framework
 * to ensure consistency and reduce duplication.
 */
public final class DebugFrameworkConstants {

    private DebugFrameworkConstants() {
    }

    // Debug Flow Context Constants.
    public static final String DEBUG_REQUEST_TYPE = "DFDP_DEBUG";

    // Debug Context Property Keys.
    public static final String DEBUG_TIMESTAMP = "DEBUG_TIMESTAMP";

    public static final String DEBUG_RESOURCE_ID = "DEBUG_RESOURCE_ID";
    public static final String DEBUG_RESOURCE_TYPE = "DEBUG_RESOURCE_TYPE";
    public static final String DEBUG_STATUS = "DEBUG_STATUS";

    // Debug Flow Identification Constants.
    public static final String DEBUG_IDENTIFIER_PARAM = "isDebugFlow";
    public static final String SESSION_DATA_KEY_PARAM = "sessionDataKey";

    // OAuth2 Callback Parameters.
    public static final String OAUTH2_CODE_PARAM = "code";
    public static final String OAUTH2_STATE_PARAM = "state";
    public static final String OAUTH2_ERROR_PARAM = "error";

    // Default Constants.
    public static final int CACHE_EXPIRY_MINUTES = 15;


    // Debug Result Context Properties.
    public static final String DEBUG_AUTH_ERROR = "DEBUG_AUTH_ERROR";
    public static final String DEBUG_AUTH_SUCCESS = "DEBUG_AUTH_SUCCESS";
    public static final String DEBUG_OAUTH_CODE = "DEBUG_OAUTH_CODE";
    public static final String DEBUG_OAUTH_STATE = "DEBUG_OAUTH_STATE";
    public static final String DEBUG_SESSION_DATA_KEY = "DEBUG_SESSION_DATA_KEY";
    public static final String DEBUG_CALLBACK_TIMESTAMP = "DEBUG_CALLBACK_TIMESTAMP";
    public static final String DEBUG_CALLBACK_PROCESSED = "DEBUG_CALLBACK_PROCESSED";

    // Common Parameter Values.
    public static final String DEBUG_PREFIX = "debug-";
    public static final String TRUE = "true";
    public static final String FALSE = "false";
}

