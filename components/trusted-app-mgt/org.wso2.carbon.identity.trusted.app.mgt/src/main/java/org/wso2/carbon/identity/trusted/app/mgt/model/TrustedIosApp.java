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

package org.wso2.carbon.identity.trusted.app.mgt.model;

import java.util.Set;

/**
 * Model class for trusted iOS apps.
 */
public class TrustedIosApp {

    private String appId;
    private Set<String> permissions;

    /**
     * Get the app id of the ios app.
     *
     * @return The app id.
     */
    public String getAppId() {

        return appId;
    }

    /**
     * Set the app id of the ios app.
     *
     * @param appId The app id to set.
     */
    public void setAppId(String appId) {

        this.appId = appId;
    }

    /**
     * Get the permissions of the android app.
     *
     * @return The permissions.
     */
    public Set<String> getPermissions() {

        return permissions;
    }

    /**
     * Set the permissions of the android app.
     *
     * @param permissions The permissions to set.
     */
    public void setPermissions(Set<String> permissions) {

        this.permissions = permissions;
    }
}
