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

package org.wso2.carbon.identity.user.pre.update.password.action.internal.constant;

/**
 * This class holds the constants used in Pre Update Password extension.
 */
public class PreUpdatePasswordActionConstants {

    public static final String PASSWORD_SHARING_FORMAT = "passwordSharingFormat";
    public static final String CERTIFICATE = "certificate";
    public static final String ATTRIBUTES = "attributes";

    public static final int MAX_ALLOWED_ATTRIBUTES = 10;

    public static final String ROLE_CLAIM_URI = "http://wso2.org/claims/roles";
    public static final String GROUP_CLAIM_URI = "http://wso2.org/claims/groups";

    // Event Context constants for pre update password action
    public static final String USER_ACTION_CONTEXT = "userActionContext";

    public static final String ACTION_VERSION_V1 = "v1";
    public static final String ACTION_VERSION_V2 = "v2";

    private PreUpdatePasswordActionConstants() {

    }
}
