/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.identity.workflow.mgt.bean;

/**
 * This class is used to represent entities associated with workflows, eg:- User entity, Role entity
 */
public class Entity {

    private String entityId;
    private String entityType;
    private int tenantId;

    public Entity(String entityId, String entityType, int tenantId) {
        this.entityId = entityId;
        this.entityType = entityType;
        this.tenantId = tenantId;
    }

    /**
     * Retrieve entity type
     *
     * @return
     */
    public String getEntityType() {
        return entityType;
    }

    /**
     * Set type of entity
     *
     * @param entityType value to set as entity type
     */
    public void setEntityType(String entityType) {
        this.entityType = entityType;
    }

    /**
     * Retrieve entity ID
     *
     * @return
     */
    public String getEntityId() {
        return entityId;
    }

    /**
     * Set entity ID
     *
     * @param entityId value to set as entity ID
     */
    public void setEntityId(String entityId) {
        this.entityId = entityId;
    }

    /**
     * Get ID of the tenant which entity belongs
     *
     * @return
     */
    public int getTenantId() {
        return tenantId;
    }

    /**
     * Set tenant ID for entity
     *
     * @param tenantId value to set as tenant ID
     */
    public void setTenantId(int tenantId) {
        this.tenantId = tenantId;
    }
}
