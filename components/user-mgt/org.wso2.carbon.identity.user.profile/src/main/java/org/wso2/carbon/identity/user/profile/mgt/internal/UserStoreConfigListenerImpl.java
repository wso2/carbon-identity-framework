/*
 * Copyright (c) 2015 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.identity.user.profile.mgt.internal;

import org.wso2.carbon.identity.user.profile.mgt.UserProfileException;
import org.wso2.carbon.identity.user.profile.mgt.dao.UserProfileMgtDAO;
import org.wso2.carbon.identity.user.store.configuration.listener.UserStoreConfigListener;
import org.wso2.carbon.user.api.UserStoreException;

public class UserStoreConfigListenerImpl implements UserStoreConfigListener {

    @Override
    public void onUserStoreNamePreUpdate(int tenantId, String currentUserStoreName,
                                         String newUserStoreName) throws UserStoreException {

        try {
            UserProfileMgtDAO.getInstance().updateDomainNameOfAssociations(tenantId, currentUserStoreName,
                                                                                   newUserStoreName);
        } catch (UserProfileException e) {
            throw new UserStoreException(String.format("Error occurred while updating user domain of associated " +
                                                       "ids with domain '%s'", currentUserStoreName), e);
        }
    }

    @Override
    public void onUserStoreNamePostUpdate(int i, String s, String s2) throws UserStoreException {

    }

    @Override
    public void onUserStorePreDelete(int tenantId, String userStoreName) throws UserStoreException {

        try {
            UserProfileMgtDAO.getInstance().deleteAssociationsFromDomain(tenantId, userStoreName);
        } catch (UserProfileException e) {
            throw new UserStoreException(String.format("Error occurred while deleting associated ids with " +
                                                       "domain '%s'", userStoreName), e);
        }
    }

    @Override
    public void onUserStorePostDelete(int i, String s) throws UserStoreException {

    }
}
