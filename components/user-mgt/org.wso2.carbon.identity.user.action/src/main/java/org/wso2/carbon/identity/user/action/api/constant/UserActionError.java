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

package org.wso2.carbon.identity.user.action.api.constant;

/**
 * Constants related to user action service.
 */
public class UserActionError {

    public static final String PRE_UPDATE_PASSWORD_ACTION_EXECUTION_FAILED = "USER-ACTION-PRE-UPDATE-PASSWORD-60001";
    public static final String PRE_UPDATE_PASSWORD_ACTION_EXECUTION_ERROR = "USER-ACTION-PRE-UPDATE-PASSWORD-65001";
    public static final String PRE_UPDATE_PASSWORD_ACTION_UNSUPPORTED_SECRET = "USER-ACTION-PRE-UPDATE-PASSWORD-65002";
    public static final String PRE_UPDATE_PASSWORD_ACTION_SERVER_ERROR = "USER-ACTION-PRE-UPDATE-PASSWORD-65003";

    public static final String PRE_UPDATE_PROFILE_ACTION_EXECUTION_FAILED = "USER-ACTION-PRE-UPDATE-PROFILE-60001";
    public static final String PRE_UPDATE_PROFILE_ACTION_EXECUTION_ERROR = "USER-ACTION-PRE-UPDATE-PROFILE-65001";
    public static final String PRE_UPDATE_PROFILE_ACTION_SERVER_ERROR = "USER-ACTION-PRE-UPDATE-PROFILE-65002";

    private UserActionError() {

    }
}
