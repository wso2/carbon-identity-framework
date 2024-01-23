/*
 * Copyright (c) 2024, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.carbon.role.mgt.ui;

public class UserStoreConfigBean {
    
    protected String realmType = null;
    
    protected String connectionURL = null;
   
    protected String connectionName = null;

    protected String connectionPassword = null;
    
    //Act dir
    protected String searchBase = null;

    //JDBC
    protected String driverName = null;
    
    protected String userListSQL = null;
    
    protected String roleListSQL = null;
    
    protected String userPasswordSQL = null;
    
    protected String userRoleSQL = null;
    
    protected String usersInRoleSQL = null;
    
    private String userFilterSQL = null;
    
    private String isUserExistingSQL = null;
        
    private String attrsSQL = null;
    
    private String attrsForProfileSQL = null;
    
    private String profileNamesSQL = null;

    //LDAP
    protected String userPattern = null;

    protected String userContextName = null;
    
    public String getRealmType() {
        return realmType;
    }

    public void setRealmType(String realmType) {
        this.realmType = realmType;
    }

    public String getConnectionURL() {
        return connectionURL;
    }

    public void setConnectionURL(String connectionURL) {
        this.connectionURL = connectionURL;
    }

    public String getConnectionName() {
        return connectionName;
    }

    public void setConnectionName(String connectionName) {
        this.connectionName = connectionName;
    }

    public String getConnectionPassword() {
        return connectionPassword;
    }

    public void setConnectionPassword(String connectionPassword) {
        this.connectionPassword = connectionPassword;
    }

    public String getDriverName() {
        return driverName;
    }

    public void setDriverName(String driverName) {
        this.driverName = driverName;
    }

    public String getUserListSQL() {
        return userListSQL;
    }

    public void setUserListSQL(String userListSQL) {
        this.userListSQL = userListSQL;
    }

    public String getRoleListSQL() {
        return roleListSQL;
    }

    public void setRoleListSQL(String roleListSQL) {
        this.roleListSQL = roleListSQL;
    }

    public String getUserPasswordSQL() {
        return userPasswordSQL;
    }

    public void setUserPasswordSQL(String userPasswordSQL) {
        this.userPasswordSQL = userPasswordSQL;
    }

    public String getUserRoleSQL() {
        return userRoleSQL;
    }

    public void setUserRoleSQL(String userRoleSQL) {
        this.userRoleSQL = userRoleSQL;
    }

    public String getSearchBase() {
        return searchBase;
    }

    public void setSearchBase(String searchBase) {
        this.searchBase = searchBase;
    }

    public String getUserPattern() {
        return userPattern;
    }

    public void setUserPattern(String userPattern) {
        this.userPattern = userPattern;
    }

    public String getUserContextName() {
        return userContextName;
    }

    public void setUserContextName(String userContextName) {
        this.userContextName = userContextName;
    }

    public String getUsersInRoleSQL() {
        return usersInRoleSQL;
    }

    public void setUsersInRoleSQL(String usersInRoleSQL) {
        this.usersInRoleSQL = usersInRoleSQL;
    }

    public String getUserFilterSQL() {
        return userFilterSQL;
    }

    public void setUserFilterSQL(String userFilterSQL) {
        this.userFilterSQL = userFilterSQL;
    }

    public String getIsUserExistingSQL() {
        return isUserExistingSQL;
    }

    public void setIsUserExistingSQL(String isUserExistingSQL) {
        this.isUserExistingSQL = isUserExistingSQL;
    }

    public String getAttrsSQL() {
        return attrsSQL;
    }

    public void setAttrsSQL(String attrsSQL) {
        this.attrsSQL = attrsSQL;
    }

    public String getAttrsForProfileSQL() {
        return attrsForProfileSQL;
    }

    public void setAttrsForProfileSQL(String attrsForProfileSQL) {
        this.attrsForProfileSQL = attrsForProfileSQL;
    }

    public String getProfileNamesSQL() {
        return profileNamesSQL;
    }

    public void setProfileNamesSQL(String profileNamesSQL) {
        this.profileNamesSQL = profileNamesSQL;
    }    
}
