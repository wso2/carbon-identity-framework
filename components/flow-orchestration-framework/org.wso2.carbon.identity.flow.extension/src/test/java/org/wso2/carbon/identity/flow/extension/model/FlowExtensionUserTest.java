/*
 * Copyright (c) 2026, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.carbon.identity.flow.extension.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.action.execution.api.model.User;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

/**
 * Tests for {@link FlowExtensionUser}, verifying the identity leaves the flow extension adds on
 * top of the shared {@link User} model are carried and serialized, while the shared model stays
 * free of them.
 */
public class FlowExtensionUserTest {

    private ObjectMapper mapper;

    @BeforeClass
    public void setUp() {

        // Mirror the request mapper configuration in ActionExecutorServiceImpl.
        mapper = new ObjectMapper();
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        mapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
    }

    @Test
    public void testGettersCarryIdentityLeaves() {

        FlowExtensionUser user = new FlowExtensionUser.Builder("uid-1")
                .username("alice")
                .userStoreDomain("PRIMARY")
                .build();

        assertEquals(user.getId(), "uid-1");
        assertEquals(user.getUsername(), "alice");
        assertEquals(user.getUserStoreDomain(), "PRIMARY");
    }

    @Test
    public void testIdentityLeavesSerializedPolymorphically() throws Exception {

        User user = new FlowExtensionUser.Builder("uid-1")
                .username("alice")
                .userStoreDomain("PRIMARY")
                .build();

        // Serialized via the static User type — Jackson uses the runtime type, so subclass
        // getters must still appear.
        String json = mapper.writeValueAsString(user);
        assertTrue(json.contains("\"username\":\"alice\""), json);
        assertTrue(json.contains("\"userStoreDomain\":\"PRIMARY\""), json);
        assertTrue(json.contains("\"id\":\"uid-1\""), json);
    }

    @Test
    public void testUnsetLeavesAreOmitted() throws Exception {

        User user = new FlowExtensionUser.Builder("uid-1").build();

        String json = mapper.writeValueAsString(user);
        assertFalse(json.contains("username"), json);
        assertFalse(json.contains("userStoreDomain"), json);
    }

    @Test
    public void testSharedUserModelHasNoIdentityLeaves() {

        // Guard against the regression of leaking these fields into the shared User API.
        for (java.lang.reflect.Method m : User.class.getMethods()) {
            assertFalse("getUsername".equals(m.getName()),
                    "Shared User model must not expose getUsername().");
            assertFalse("getUserStoreDomain".equals(m.getName()),
                    "Shared User model must not expose getUserStoreDomain().");
        }
    }
}
