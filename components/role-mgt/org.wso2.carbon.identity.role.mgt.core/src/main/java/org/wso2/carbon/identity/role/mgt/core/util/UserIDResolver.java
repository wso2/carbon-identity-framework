/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.identity.role.mgt.core.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.identity.role.mgt.core.IdentityRoleManagementClientException;
import org.wso2.carbon.identity.role.mgt.core.IdentityRoleManagementException;
import org.wso2.carbon.identity.role.mgt.core.IdentityRoleManagementServerException;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.api.UserStoreManager;
import org.wso2.carbon.user.core.common.AbstractUserStoreManager;

import java.util.ArrayList;
import java.util.List;

import static org.wso2.carbon.identity.role.mgt.core.RoleConstants.Error.INVALID_REQUEST;
import static org.wso2.carbon.identity.role.mgt.core.RoleConstants.Error.OPERATION_NOT_SUPPORTED;
import static org.wso2.carbon.identity.role.mgt.core.RoleConstants.Error.UNEXPECTED_SERVER_ERROR;
import static org.wso2.carbon.identity.role.mgt.core.RoleConstants.RoleTableColumns.USER_NOT_FOUND_ERROR_MESSAGE;

/**
 * UserIDResolver Implementation of the {@link IDResolver} interface.
 */
public class UserIDResolver implements IDResolver {

    private Log log = LogFactory.getLog(UserIDResolver.class);

    @Override
    public String getNameByID(String id, String tenantDomain) throws IdentityRoleManagementException {

        String userName = resolveUserNameFromUserID(id);
        if (userName == null) {
            String errorMessage = "A user doesn't exist with id: " + id + " in the tenantDomain: " + tenantDomain;
            throw new IdentityRoleManagementClientException(INVALID_REQUEST.getCode(), errorMessage);
        }
        return userName;
    }

    /**
     * Retrieve the user names for the given ID list.
     *
     * @param idList       List of user IDs.
     * @param tenantDomain Tenant domain.
     * @return List of user names.
     * @throws IdentityRoleManagementException IdentityRoleManagementException.
     */
    public List<String> getNamesByIDs(List<String> idList, String tenantDomain) throws IdentityRoleManagementException {

        List<String> usersList = new ArrayList<>();
        for (String id : idList) {
            usersList.add(getNameByID(id, tenantDomain));
        }
        return usersList;
    }

    @Override
    public String getIDByName(String name, String tenantDomain) throws IdentityRoleManagementException {

        String id = resolveIDFromUserName(name);
        if (id == null) {
            String errorMessage = String.format(USER_NOT_FOUND_ERROR_MESSAGE, name, tenantDomain);
            throw new IdentityRoleManagementClientException(INVALID_REQUEST.getCode(), errorMessage);
        }
        return id;
    }

    /**
     * Retrieve the user IDs for the given names list.
     *
     * @param namesList    User names list.
     * @param tenantDomain Tenant domain.
     * @return List of user IDs.
     * @throws IdentityRoleManagementException IdentityRoleManagementException.
     */
    public List<String> getIDsByNames(List<String> namesList, String tenantDomain)
            throws IdentityRoleManagementException {

        List<String> usersIDList = new ArrayList<>();
        for (String name : namesList) {
            usersIDList.add(getIDByName(name, tenantDomain));
        }
        return usersIDList;
    }

    /**
     * Retrieves the unique user id of the given userID.
     *
     * @param userID userID.
     * @return unique user id of the user.
     * @throws IdentityRoleManagementException IdentityRoleManagementException.
     */
    private String resolveUserNameFromUserID(String userID) throws IdentityRoleManagementException {

        try {
            UserStoreManager userStoreManager = PrivilegedCarbonContext.getThreadLocalCarbonContext().getUserRealm()
                    .getUserStoreManager();
            try {
                if (userStoreManager instanceof AbstractUserStoreManager) {
                    return ((AbstractUserStoreManager) userStoreManager).getUserNameFromUserID(userID);
                }
                if (log.isDebugEnabled()) {
                    log.debug("Provided user store manager for the userID: " + userID + ", is not an instance of the "
                            + "AbstractUserStore manager");
                }
                throw new IdentityRoleManagementClientException(OPERATION_NOT_SUPPORTED.getCode(),
                        "Unable to get the username of the userID: " + userID + ".");
            } catch (UserStoreException e) {
                throw new IdentityRoleManagementServerException(UNEXPECTED_SERVER_ERROR.getCode(),
                        "Error occurred while resolving username for the userID: " + userID, e);
            }
        } catch (UserStoreException e) {
            throw new IdentityRoleManagementServerException(UNEXPECTED_SERVER_ERROR.getCode(),
                    "Error occurred while retrieving the userstore manager to resolve username for the userID: "
                            + userID, e);
        }
    }

    /**
     * Retrieves the unique user id of the given userName.
     *
     * @param userName userName.
     * @return unique user id of the user.
     * @throws IdentityRoleManagementException IdentityRoleManagementException.
     */
    private String resolveIDFromUserName(String userName) throws IdentityRoleManagementException {

        try {
            UserStoreManager userStoreManager = PrivilegedCarbonContext.getThreadLocalCarbonContext().getUserRealm()
                    .getUserStoreManager();
            try {
                if (userStoreManager instanceof AbstractUserStoreManager) {
                    return ((AbstractUserStoreManager) userStoreManager).getUserIDFromUserName(userName);
                }
                if (log.isDebugEnabled()) {
                    log.debug(
                            "Provided user store manager for the userName: " + userName + ", is not an instance of the "
                                    + "AbstractUserStore manager");
                }
                throw new IdentityRoleManagementClientException(OPERATION_NOT_SUPPORTED.getCode(),
                        "Unable to get the username of the userName: " + userName + ".");
            } catch (UserStoreException e) {
                throw new IdentityRoleManagementServerException(UNEXPECTED_SERVER_ERROR.getCode(),
                        "Error occurred while resolving username for the userName: " + userName, e);
            }
        } catch (UserStoreException e) {
            throw new IdentityRoleManagementServerException(UNEXPECTED_SERVER_ERROR.getCode(),
                    "Error occurred while retrieving the userstore manager to resolve username for the userName: "
                            + userName, e);
        }
    }
}
