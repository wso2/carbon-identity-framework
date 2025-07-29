/*
 * Copyright (c) 2025, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.carbon.identity.workflow.mgt.bean;
/**
 * This class is used to represent entities associated with role workflows.
 */
public class RoleEntity extends Entity {

    private String audience;
    private String audienceId;

    public RoleEntity(String entityId, String entityType, int tenantId, String audience,
                      String audienceId) {

        super(entityId, entityType, tenantId);
        this.audience = audience;
        this.audienceId = audienceId;
    }

    /**
     * Get audience ID of the entity.
     *
     * @return Audience ID
     */
    public String getAudienceId() {

        return audienceId;
    }

    /**
     * Set audience ID for entity.
     *
     * @param audienceId value to set as audience ID
     */
    public void setAudienceId(String audienceId) {

        this.audienceId = audienceId;
    }

    /**
     * Get audience of the entity.
     *
     * @return Audience
     */
    public String getAudience() {

        return audience;
    }

    /**
     * Set audience for entity.
     *
     * @param audience value to set as audience
     */
    public void setAudience(String audience) {

        this.audience = audience;
    }
}
