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

package org.wso2.carbon.identity.role.v2.mgt.core.model;

import java.util.Optional;

/**
 * Represents the permission.
 */
public class Permission {

    private String name;
    private String displayName;
    private Optional<String> apiId = Optional.empty();

    /**
     * Constructs a Permission with the specified name.
     *
     * @param name the name of the permission.
     */
    public Permission(String name) {

        this.name = name;
    }

    /**
     * Constructs a Permission with the specified name and display name.
     *
     * @param name        the name of the permission.
     * @param displayName the display name of the permission.
     */

    public Permission(String name, String displayName) {

        this.name = name;
        this.displayName = displayName;
    }

    /**
     * Constructs a Permission with the specified name, display name, and API ID.
     *
     * @param name        the name of the permission.
     * @param displayName the display name of the permission.
     * @param apiId       the API ID of the permission.
     */
    public Permission(String name, String displayName, String apiId) {

        this(name, displayName);
        this.apiId = Optional.ofNullable(apiId);
    }

    /**
     * Gets the name of the permission.
     *
     * @return the name of the permission.
     */
    public String getName() {

        return name;
    }

    /**
     * Sets the name of the permission.
     *
     * @param name the name to set for the permission.
     */
    public void setName(String name) {

        this.name = name;
    }

    /**
     * Gets the display name of the permission.
     *
     * @return the display name of the permission.
     */
    public String getDisplayName() {

        return displayName;
    }

    /**
     * Sets the display name for the permission.
     *
     * @param displayName the display name to set.
     */

    public void setDisplayName(String displayName) {

        this.displayName = displayName;
    }

    /**
     * Gets the optional API ID associated with the permission.
     *
     * @return an Optional containing the API ID if present, or an empty Optional otherwise.
     */
    public Optional<String> getApiId() {
        return apiId;
    }


    /**
     * Sets the API ID for the permission.
     *
     * @param apiId the API ID to set.
     */
    public void setApiId(String apiId) {
        this.apiId = Optional.ofNullable(apiId);
    }
}
