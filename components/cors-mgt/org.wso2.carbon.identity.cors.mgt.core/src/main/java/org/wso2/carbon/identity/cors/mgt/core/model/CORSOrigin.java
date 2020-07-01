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

/**
 * CORSOrigin model class.
 */
public class CORSOrigin {

    /**
     * A {@code String} to hold the ID.
     */
    private String id;

    /**
     * A {@code String} to hold the URL.
     */
    private String url;

    /**
     * Default constructor.
     */
    public CORSOrigin() {

    }

    /**
     * Constructor with the {@code id} and {@code url} parameter.
     *
     * @param id  The value to be set as the {@code id}.
     * @param url The value to be set as the {@code url}.
     */
    public CORSOrigin(String id, String url) {

        this.id = id;
        this.url = url;
    }

    /**
     * Overrides {@code Object.hashCode}.
     *
     * @return The object hash code.
     */
    @Override
    public int hashCode() {

        return url.hashCode();
    }

    /**
     * Overrides {@code Object.equals()}.
     *
     * @param object The object to compare to.
     * @return {@code true} if the objects are both origins with the same value, else {@code false}.
     */
    @Override
    public boolean equals(Object object) {

        return object instanceof CORSOrigin && this.toString().equals(object.toString());
    }

    /**
     * Get the {@code id}.
     *
     * @return The {@code id}.
     */
    public String getId() {

        return id;
    }

    /**
     * Set the {@code id}.
     *
     * @param id The value to be set as the {@code id}.
     */
    public void setId(String id) {

        this.id = id;
    }

    /**
     * Get the {@code url}.
     *
     * @return The {@code url}.
     */
    public String getUrl() {

        return url;
    }

    /**
     * Set the {@code url}.
     *
     * @param url The value to be set as the {@code url}.
     */
    public void setUrl(String url) {

        this.url = url;
    }
}
