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
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.user.core.UserStoreManager;
import org.wso2.carbon.user.mgt.common.UserAdminException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class UserBulkImport {

    Map<String, String> errorUsersMap = new HashMap<>();
    List<String> duplicateUsers = new ArrayList<>();
    String userStoreDomain = "";
    int totalCount = 0;
    int successCount = 0;
    int failCount = 0;
    int duplicateCount = 0;
    String tenantUser = PrivilegedCarbonContext.getThreadLocalCarbonContext().getUsername() + "@"
            + PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantDomain();

    public abstract void addUserList(UserStoreManager userStore) throws UserAdminException;

    /**
     * Build the summery log for the bulk user import operation.
     * The structure of the summery would be as follows.
     * <p>
     * SUMMERY
     * Bulk User Import Operation Performed by : <User Name>
     * User Store : <User Store Domain>
     * <p>
     * Duplicate User count :
     * Duplicate Users : n
     * [userName1, userName2, ...., userName(n)]
     * <p>
     * Failed User Count : n
     * Failed Users :
     * UserName : failed_UserName1
     * Cause : Cause of the failure
     */
    String buildBulkImportSummery() {

        JsonObject summeryJson = new JsonObject();
        JsonObject duplicateUsersJson = new JsonObject();
        JsonArray duplicateUsersJsonArray = new JsonArray();
        JsonObject errorUsersJson = new JsonObject();
        JsonObject errorUserJson;
        JsonArray errorUsersJsonArray = new JsonArray();

        summeryJson.addProperty("Bulk User Import Operation Performed by",
                PrivilegedCarbonContext.getThreadLocalCarbonContext().getUsername());
        summeryJson.addProperty("User Store", userStoreDomain);
        summeryJson.addProperty("Success Count", successCount);

        if (duplicateCount > 0) {
            duplicateUsersJson.addProperty("Duplicate User Count", duplicateCount);

            for (String user : duplicateUsers) {
                JsonObject userJson = new JsonObject();
                userJson.addProperty("Name", user);
                duplicateUsersJsonArray.add(userJson);
            }

            duplicateUsersJson.add("User Names", duplicateUsersJsonArray);
            summeryJson.add("Duplicate Users", duplicateUsersJson);
        }

        if (failCount > 0) {
            errorUsersJson.addProperty("Failed User Count", failCount);
            for (Object o : errorUsersMap.entrySet()) {
                Map.Entry pair = (Map.Entry) o;
                errorUserJson = new JsonObject();
                errorUserJson.addProperty("Name", pair.getKey().toString());
                errorUserJson.addProperty("Cause", pair.getValue().toString());
                errorUsersJsonArray.add(errorUserJson);
            }
            errorUsersJson.add("Failed Users List", errorUsersJsonArray);
            summeryJson.add("Failed Users", errorUsersJson);
        }

        return summeryJson.toString();
    }
}
