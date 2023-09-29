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

package org.wso2.carbon.identity.role.v2.mgt.core;

/**
 * Represents the basic attributes of an entity.
 */
public class Entity {

    private String id;
    private String name;

    public Entity() {

    }

    public Entity(String id, String name) {

        this.id = id;
        this.name = name;
    }

    /**
     * Get the entity Id.
     *
     * @return entity Id.
     */
    public String getId() {

        return id;
    }

    /**
     * Set the entity Id.
     *
     * @param id entity Id.
     */
    public void setId(String id) {

        this.id = id;
    }

    /**
     * Get the entity name.
     *
     * @return entity name.
     */
    public String getName() {

        return name;
    }

    /**
     * Set the entity name.
     *
     * @param name entity name.
     */
    public void setName(String name) {

        this.name = name;

    }
}
