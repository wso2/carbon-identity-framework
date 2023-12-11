/*
 * Copyright (c) 2023, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.carbon.identity.user.onboard.core.service.model;

/**
 * This class holds the configuration of the invite-link.
 */
public class Configuration {

    private String username;
    private String userStore;
    private String tenantDomain;

    /**
     * Creates Configuration object.
     *
     * @param username      Username of the user to whom the invite-link should be generated.
     * @param userstore     User store where the user belongs to.
     * @param tenantDomain  Tenant name where the user belongs to.
     */
    public Configuration(String username, String userstore, String tenantDomain) {
        this.username = username;
        this.userStore = userstore;
        this.tenantDomain = tenantDomain;
    }

    /**
     * Method to get the username.
     *
     * @return username string.
     */
    public String getUsername() {
        return this.username;
    }

    /**
     * Get user store name of the user.
     *
     * @return user store domain name.
     */
    public String getUserStore() {
        return this.userStore;
    }

    /**
     * Get the name of the tenant.
     *
     * @return tenant domain name.
     */
    public String getTenantDomain() {
        return this.tenantDomain;
    }

    /**
     * Set the username to the configuration.
     *
     * @param username  the username of the user.
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * Set the user store domain to the configuration.
     *
     * @param userStore the user store domain name.
     */
    public void setUserStore(String userStore) {
        this.userStore = userStore;
    }

    /**
     * Set tenant domain to the configuration.
     *
     * @param tenantDomain tenant domain name.
     */
    public void setTenantDomain(String tenantDomain) {
        this.tenantDomain = tenantDomain;
    }
}
