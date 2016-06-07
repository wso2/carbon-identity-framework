/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
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
package org.wso2.carbon.identity.user.store.count.jdbc;

public class JDBCUserStoreMetricsConstants {

    public static final String COUNT_USERS_SQL = "SELECT COUNT(UM_USER_NAME) AS RESULT FROM UM_USER WHERE UM_USER_NAME LIKE ? " +
            "AND UM_TENANT_ID = ?";

    public static final String COUNT_ROLES_SQL = "SELECT COUNT(UM_ROLE_NAME) AS RESULT FROM UM_ROLE WHERE UM_ROLE_NAME LIKE ? AND " +
            "UM_TENANT_ID = ?";

    public static final String COUNT_CLAIM_SQL = "SELECT COUNT(UM_USER_ID) AS RESULT FROM UM_USER_ATTRIBUTE WHERE UM_ATTR_NAME = ? " +
            "AND UM_TENANT_ID = ? AND UM_ATTR_VALUE LIKE ? AND UM_PROFILE_ID = ?";

}
