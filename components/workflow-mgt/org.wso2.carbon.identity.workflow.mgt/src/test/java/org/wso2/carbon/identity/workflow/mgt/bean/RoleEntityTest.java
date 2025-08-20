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

import org.testng.annotations.Test;

import static org.testng.AssertJUnit.assertEquals;

/**
 * Test class for RoleEntity.
 */
public class RoleEntityTest {

    private static final String ENTITY_ID = "role1";
    private static final String ENTITY_TYPE = "role";
    private static final int TENANT_ID = 1;
    private static final String AUDIENCE = "audience1";
    private static final String AUDIENCE_ID = "audienceId1";

    @Test
    public void testRoleEntity() {

        RoleEntity roleEntity = new RoleEntity(ENTITY_ID, ENTITY_TYPE, TENANT_ID, null, null);
        roleEntity.setAudience(AUDIENCE);
        roleEntity.setAudienceId(AUDIENCE_ID);
        assertEquals(ENTITY_ID, roleEntity.getEntityId());
        assertEquals(ENTITY_TYPE, roleEntity.getEntityType());
        assertEquals(TENANT_ID, roleEntity.getTenantId());
        assertEquals(AUDIENCE, roleEntity.getAudience());
        assertEquals(AUDIENCE_ID, roleEntity.getAudienceId());
    }
}
