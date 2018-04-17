/*
 * Copyright (c) 2007 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 */

package org.wso2.carbon.user.mgt.bulkimport;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import org.apache.commons.logging.Log;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.user.core.UserStoreManager;
import org.wso2.carbon.user.mgt.UserMgtConstants;
import org.wso2.carbon.user.mgt.common.UserAdminException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Class to import multiple users to the Identity user store.
 * The users can be added in the format of CSV or Excel format.
 */
public abstract class UserBulkImport {

    static final Log auditLog = CarbonConstants.AUDIT_LOG;
    static final Map<String, String> errorUsersMap = new HashMap<>();
    static final List<String> duplicateUsers = new ArrayList<>();
    String userStoreDomain = "";
    int successCount = 0;
    int failCount = 0;
    int duplicateCount = 0;
    String tenantUser = PrivilegedCarbonContext.getThreadLocalCarbonContext().getUsername() + "@"
            + PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantDomain();

    /**
     * Method to add users to the given user store.
     *
     * @param userStore : The UserStore that the users should be imported to.
     * @throws UserAdminException :
     */
    public abstract void addUserList(UserStoreManager userStore) throws UserAdminException;

    /**
     * Build the summery log for the bulk user import operation.
     * The structure of the summery would be as follows.
     *
     * {
     *     operation : bulk_user_import
     *     performedBy : <Logged in User>
     *     userStore : <User Store Domain>
     *     successCount : x
     *     duplicateUsers : {count: x, users: [userName_1, userName_2, ..., userName_n]}
     *     failedUsers : {count: x, users: [{name: userName, cause: cause_for_the_failure}, ...]}
     * }
     */
    String buildBulkImportSummary() {

        JsonArray duplicateUsersJsonArray;
        JsonArray errorUsersJsonArray;
        JsonObject duplicateUsersJson;
        JsonObject errorUsersJson;
        JsonObject errorUserJson;
        JsonObject summaryJson = new JsonObject();

        summaryJson.addProperty(UserMgtConstants.OPERATION, UserMgtConstants.OPERATION_NAME);
        summaryJson.addProperty(UserMgtConstants.PERFORMED_BY,
                PrivilegedCarbonContext.getThreadLocalCarbonContext().getUsername());
        summaryJson.addProperty(UserMgtConstants.USER_STORE, userStoreDomain);
        summaryJson.addProperty(UserMgtConstants.SUCCESS_COUNT, successCount);

        if (duplicateCount > 0) {
            duplicateUsersJson = new JsonObject();
            duplicateUsersJsonArray = new JsonArray();
            duplicateUsersJson.addProperty(UserMgtConstants.COUNT, duplicateCount);

            for (String user : duplicateUsers) {
                JsonPrimitive userJson = new JsonPrimitive(user);
                duplicateUsersJsonArray.add(userJson);
            }

            duplicateUsersJson.add(UserMgtConstants.USERS, duplicateUsersJsonArray);
            summaryJson.add(UserMgtConstants.DUPLICATE_USERS, duplicateUsersJson);
        }

        if (failCount > 0) {
            errorUsersJson = new JsonObject();
            errorUsersJsonArray = new JsonArray();
            errorUsersJson.addProperty(UserMgtConstants.COUNT, failCount);
            for (Object o : errorUsersMap.entrySet()) {
                Map.Entry pair = (Map.Entry) o;
                errorUserJson = new JsonObject();
                errorUserJson.addProperty(UserMgtConstants.NAME, pair.getKey().toString());
                errorUserJson.addProperty(UserMgtConstants.CAUSE, pair.getValue().toString());
                errorUsersJsonArray.add(errorUserJson);
            }
            errorUsersJson.add(UserMgtConstants.USERS, errorUsersJsonArray);
            summaryJson.add(UserMgtConstants.FAILED_USERS, errorUsersJson);
        }

        return summaryJson.toString();
    }
}
