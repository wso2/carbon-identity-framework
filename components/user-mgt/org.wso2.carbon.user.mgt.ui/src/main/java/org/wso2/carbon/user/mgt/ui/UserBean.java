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

package org.wso2.carbon.user.mgt.ui;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class UserBean {

    private String domain = "";
    private String username = "";
    private String password = "";
    private String[] userRoles = new String[0];
    private String email = "";

    public String getUsername() {

        if (!username.contains(UserAdminUIConstants.DOMAIN_SEPARATOR) && domain != null && domain.trim().length()
                > 0) {
            return domain + UserAdminUIConstants.DOMAIN_SEPARATOR + username;

        }
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String[] getUserRoles() {
        return Arrays.copyOf(userRoles, userRoles.length);
    }

    public void setUserRoles(String[] userRoles) {
        this.userRoles = Arrays.copyOf(userRoles, userRoles.length);
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        if (!UserAdminUIConstants.PRIMARY_DOMAIN_NAME_NOT_DEFINED.equalsIgnoreCase(domain)) {
            this.domain = domain;
        }
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setClaimMapping() {
        return;
    }

    public void cleanup() {
        username = null;
        password = null;
        userRoles = null;
        email = null;
        domain = null;
    }

    public void addUserRoles(Map<String, Boolean> checkedRolesMap) {

        if (checkedRolesMap == null) {
            return;
        }
        List<String> userRolesList = new ArrayList<String>();

        for (Map.Entry<String, Boolean> entry : checkedRolesMap.entrySet()) {
            if (entry.getValue()) {
                userRolesList.add(entry.getKey());
            }
        }
        for (String role : userRoles) {
            if (!userRolesList.contains(role)) {
                userRolesList.add(role);
            }
        }
        userRoles = userRolesList.toArray(new String[userRolesList.size()]);
    }
}
