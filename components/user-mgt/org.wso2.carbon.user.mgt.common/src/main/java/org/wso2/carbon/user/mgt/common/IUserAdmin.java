/*
 * Copyright (c) 2010 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.user.mgt.common;

import org.apache.axis2.AxisFault;

import javax.activation.DataHandler;


public interface IUserAdmin {
    /*
     * (non-Javadoc)
     * 
     * @see
     * org.wso2.carbon.user.mgt.UserAdmin#listInternalUsers(java.lang.String)
     */
    String[] listUsers(String filter, int limit) throws UserAdminException;

    /**
     * Return a FlaggedName object containing user info.
     * @param filer
     * @param limit
     * @return
     * @throws UserAdminException
     */
    FlaggedName[] listAllUsers(String filer, int limit) throws UserAdminException;

    FlaggedName[] listUserByClaim(ClaimValue claimValue, String filter, int maxLimit) throws UserAdminException;
    /*
     * (non-Javadoc)
     * 
     * @see org.wso2.carbon.user.mgt.UserAdmin#getInternalRoles()
     */
    FlaggedName[] getAllRolesNames(String filter, int limit) throws UserAdminException;

    UserStoreInfo getUserStoreInfo() throws UserAdminException;

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.wso2.carbon.user.mgt.UserAdmin#addUserToInternalStore(java.lang.String
     * , java.lang.String, java.lang.String[])
     */
    void addUser(String userName, String password, String[] roles,
            ClaimValue[] claims, String profileName) throws UserAdminException;

    /*
     * (non-Javadoc)
     * 
     * @see org.wso2.carbon.user.mgt.UserAdmin#changePassword(java.lang.String,
     * java.lang.String)
     */
    void changePassword(String userName, String newPassword) throws UserAdminException;

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.wso2.carbon.user.mgt.UserAdmin#deleteUserFromInternalStore(java.lang
     * .String)
     */
    void deleteUser(String userName) throws UserAdminException;

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.wso2.carbon.user.mgt.UserAdmin#addRoleToInternalStore(java.lang.String
     * , java.lang.String[], java.lang.String[])
     */
    void addRole(String roleName, String[] userList, String[] permissions)
            throws UserAdminException;

    void addInternalRole(String roleName, String[] userList, String[] permissions)
            throws UserAdminException;
    /*
     * (non-Javadoc)
     * 
     * @see
     * org.wso2.carbon.user.mgt.UserAdmin#deleteRoleFromInternalStore(java.lang
     * .String)
     */
    void deleteRole(String roleName) throws UserAdminException;

    /*
     * (non-Javadoc)
     * 
     * @see org.wso2.carbon.user.mgt.UserAdmin#getUsersInRole(java.lang.String)
     */
    FlaggedName[] getUsersOfRole(String roleName, String filter, int limit) throws UserAdminException;

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.wso2.carbon.user.mgt.UserAdmin#updateUsersOfRole(java.lang.String,
     * java.lang.String[], java.lang.String[])
     */
    void updateUsersOfRole(String roleName, FlaggedName[] userList)
            throws UserAdminException;

    /*
     * (non-Javadoc)
     *
     * @see org.wso2.carbon.user.mgt.UserAdmin#getUsersInRole(java.lang.String)
     */
    FlaggedName[] getRolesOfUser(String userName, String filter, int limit) throws UserAdminException;

    // FIXME: Fix the documentation of this interface including this.
    FlaggedName[] getRolesOfCurrentUser() throws UserAdminException;

    void updateRolesOfUser(String userName, String[] newUserList) throws UserAdminException;
    
    UIPermissionNode getAllUIPermissions() throws UserAdminException;

    UIPermissionNode getRolePermissions(String roleName) throws UserAdminException;

    void setRoleUIPermission(String roleName, String[] rawResources)
            throws UserAdminException;

    void bulkImportUsers(String fileName, DataHandler handler, String defaultPassword)
            throws UserAdminException;

    void changePasswordByUser(String oldPassword, String newPassword)
            throws UserAdminException, AxisFault;

    void updateRoleName(String roleName, String newRoleName) throws UserAdminException ;

    boolean hasMultipleUserStores() throws UserAdminException;

    public void addRemoveUsersOfRole(String roleName, String[] newUsers, String[] deletedUsers)  throws UserAdminException;

    public void addRemoveRolesOfUser(String userName, String[] newRoles, String[] deletedRoles)  throws UserAdminException;

}