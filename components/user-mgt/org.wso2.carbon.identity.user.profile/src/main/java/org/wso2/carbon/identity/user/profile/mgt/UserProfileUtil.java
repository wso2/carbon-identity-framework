/*
 * Copyright (c) 2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.identity.user.profile.mgt;

import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.user.core.AuthorizationManager;
import org.wso2.carbon.user.core.UserRealm;
import org.wso2.carbon.user.core.UserStoreException;

public class UserProfileUtil {


    private UserProfileUtil(){

    }

    public static boolean isUserAuthorizedToConfigureProfile(UserRealm realm, String currentUserName, String targetUser)
            throws UserStoreException {
        boolean isAuthrized = false;
        if (currentUserName == null) {
            //do nothing
        } else if (currentUserName.equals(targetUser)) {
            isAuthrized = true;
        } else {
            AuthorizationManager authorizer = realm.getAuthorizationManager();
            isAuthrized = authorizer.isUserAuthorized(currentUserName,
                    CarbonConstants.UI_ADMIN_PERMISSION_COLLECTION + "/configure/security/usermgt/profiles",
                    "ui.execute");
        }
        return isAuthrized;
    }

}
