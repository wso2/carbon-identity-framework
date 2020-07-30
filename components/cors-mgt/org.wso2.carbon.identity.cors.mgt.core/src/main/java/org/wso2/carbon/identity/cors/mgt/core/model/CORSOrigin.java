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

package org.wso2.carbon.identity.cors.mgt.core.model;

import java.util.HashSet;
import java.util.Set;

/**
 * A class for a CORS origin.
 */
public class CORSOrigin {

    /**
     * ID of the origin.
     */
    String id;

    /**
     * The origin of the CORSOrigin instance.
     */
    private String origin;

    /**
     * App IDs associated with the {@code origin}.
     */
    private Set<String> appIds;

    /**
     * Default constructor.
     */
    public CORSOrigin() {

        this.appIds = new HashSet<>();
    }

    /**
     * Get the {@code id}.
     *
     * @return Returns the {@code id}.
     */
    public String getId() {

        return id;
    }

    /**
     * Set the {@code id}.
     */
    public void setId(String id) {

        this.id = id;
    }

    /**
     * Get the {@code origin}.
     *
     * @return Returns the {@code origin}.
     */
    public String getOrigin() {

        return origin;
    }

    /**
     * Set the {@code origin}.
     *
     * @param origin The value to be set as the {@code origin}.
     */
    public void setOrigin(String origin) {

        this.origin = origin;
    }

    /**
     * Get the {@code appIds}.
     *
     * @return Returns the {@code appIds}.
     */
    public Set<String> getAppIds() {

        return appIds;
    }

    /**
     * Set the {@code appIds}.
     *
     * @param appIds The values to be set as the {@code appIds}.
     */
    public void setAppIds(Set<String> appIds) {

        this.appIds = appIds;
    }
}
