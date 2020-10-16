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
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 * NOTE: The code/logic in this class is copied from https://bitbucket.org/thetransactioncompany/cors-filter.
 * All credits goes to the original authors of the project https://bitbucket.org/thetransactioncompany/cors-filter.
 */

package org.wso2.carbon.identity.cors.mgt.core.model;

/**
 * An application which has an association with a particular CORS origin.
 */
public class CORSApplication {

    /**
     * ID of the application.
     */
    private String id;

    /**
     * Name of the application.
     */
    private String name;

    /**
     * Constructor for Application.
     *
     * @param id ID of the application.
     */
    public CORSApplication(String id) {

        this.id = id;
    }

    /**
     * Constructor for CORSApplication.
     *
     * @param id   ID of the application.
     * @param name Name of the application.
     */
    public CORSApplication(String id, String name) {

        this.id = id;
        this.name = name;
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
     * Get the {@code name}.
     *
     * @return Returns the {@code name}.
     */
    public String getName() {

        return name;
    }

    /**
     * Set the {@code name}.
     */
    public void setName(String name) {

        this.name = name;
    }
}
