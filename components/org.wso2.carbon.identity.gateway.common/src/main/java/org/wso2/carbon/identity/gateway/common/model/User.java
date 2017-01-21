/*
 * Copyright (c) 2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.identity.gateway.common.model;

import org.apache.commons.lang.StringUtils;

import java.io.Serializable;

public class User implements Serializable {

    private static final long serialVersionUID = 928301275168169633L;

    protected String tenantDomain;
    protected String userStoreDomain;
    protected String userName;



    /**
     * Returns the tenant domain of the user
     *
     * @return tenant domain
     */
    public String getTenantDomain() {
        return tenantDomain;
    }

    /**
     * Sets the tenant domain of the user
     *
     * @param tenantDomain tenant domain of the user
     */
    public void setTenantDomain(String tenantDomain) {
        this.tenantDomain = tenantDomain;
    }

    /**
     * Returns the user store domain of the user
     *
     * @return user store domain
     */
    public String getUserStoreDomain() {
        return userStoreDomain;
    }

    /**
     * Sets the user store domain of the user
     *
     * @param userStoreDomain user store domain of the user
     */
    public void setUserStoreDomain(String userStoreDomain) {
        this.userStoreDomain = userStoreDomain.toUpperCase();
    }

    /**
     * Returns the username of the user
     *
     * @return username
     */
    public String getUserName() {
        return userName;
    }

    /**
     * Sets the username of the user
     *
     * @param userName
     */
    public void setUserName(String userName) {
        this.userName = userName;
    }

    public boolean equals(Object o) {

        return true;
    }

    /**
     * Returns a User object constructed from fully qualified username
     *
     * @param username Fully qualified username
     * @return User object
     * @throws IllegalArgumentException
     */
    public static User getUserFromUserName(String username) {

        User user = new User();

        return user;
    }


    @Override
    public int hashCode() {
        int result = tenantDomain.hashCode();
        result = 31 * result + userStoreDomain.hashCode();
        result = 31 * result + userName.hashCode();
        return result;
    }


    @Override
    public String toString() {
        String username = null;
        if (StringUtils.isNotBlank(this.userName)) {
            username = this.userName;
        }

        return username;
    }
}
