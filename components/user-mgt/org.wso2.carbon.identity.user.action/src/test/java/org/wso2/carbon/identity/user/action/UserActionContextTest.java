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

package org.wso2.carbon.identity.user.action;

import org.testng.Assert;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.action.execution.api.model.Organization;
import org.wso2.carbon.identity.user.action.api.model.UserActionContext;
import org.wso2.carbon.identity.user.action.api.model.UserActionRequestDTO;

/**
 * Test class for UserActionContext.
 */
public class UserActionContextTest {

    public static final String TEST_USER_ID = "testUser";
    public static final String PASSWORD = "testPassword";
    public static final String TEST_USER_STORE_DOMAIN = "testUserStoreDomain";
    public static final String TEST_CLAIM = "testClaim";
    public static final String TEST_CLAIM_VALUE = "testClaimValue";
    public static final String TEST_CLAIM_2 = "testClaim2";
    public static final String TEST_CLAIM_VALUE_1 = "testClaimValue1";
    public static final String TEST_CLAIM_VALUE_2 = "testClaimValue2";
    public static final String TEST_GROUP = "testGroup";
    public static final String TEST_ROLE = "testRole";
    public static final Organization ORGANIZATION = new Organization.Builder()
            .id("184a5362-c3ec-4508-a9fa-bc67087a8dae")
            .name("wso2")
            .orgHandle("wso2.com")
            .depth(1)
            .build();
    public static final String SHARED_USER_ID = "a84a536f-c3ec-4508-a9fa-ac67087a8da3";

    @Test
    public void testUserActionContext() {

        UserActionContext userActionContext = new UserActionContext.Builder()
                .userId(TEST_USER_ID)
                .password(PASSWORD.toCharArray())
                .userStoreDomain(TEST_USER_STORE_DOMAIN)
                .build();

        Assert.assertEquals(userActionContext.getUserId(), TEST_USER_ID);
        Assert.assertEquals(userActionContext.getPassword(), PASSWORD.toCharArray());
        Assert.assertEquals(userActionContext.getUserStoreDomain(), TEST_USER_STORE_DOMAIN);
    }

    @Test
    public void testUserActionContextWithoutPassword() {

        UserActionContext userActionContext = new UserActionContext.Builder()
                .userId(TEST_USER_ID)
                .userStoreDomain(TEST_USER_STORE_DOMAIN)
                .build();

        Assert.assertEquals(userActionContext.getUserId(), TEST_USER_ID);
        Assert.assertNull(userActionContext.getPassword());
        Assert.assertEquals(userActionContext.getUserStoreDomain(), TEST_USER_STORE_DOMAIN);
    }

    @Test
    public void testCreateUserActionContextWithUserActionRequestDTO() {

        UserActionRequestDTO userActionRequestDTO = new UserActionRequestDTO.Builder()
                .userId(TEST_USER_ID)
                .password(PASSWORD.toCharArray())
                .userStoreDomain(TEST_USER_STORE_DOMAIN)
                .addClaim(TEST_CLAIM, TEST_CLAIM_VALUE)
                .addClaim(TEST_CLAIM_2, new String[]{TEST_CLAIM_VALUE_1, TEST_CLAIM_VALUE_2})
                .addGroup(TEST_GROUP)
                .addRole(TEST_ROLE)
                .residentOrganization(ORGANIZATION)
                .sharedUserId(SHARED_USER_ID)
                .build();
        UserActionContext context = new UserActionContext(userActionRequestDTO);

        Assert.assertEquals(context.getUserActionRequestDTO().getUserId(), TEST_USER_ID);
        Assert.assertEquals(context.getUserActionRequestDTO().getPassword(), PASSWORD.toCharArray());
        Assert.assertEquals(context.getUserActionRequestDTO().getUserStoreDomain(), TEST_USER_STORE_DOMAIN);
        Assert.assertEquals(context.getUserActionRequestDTO().getClaims().get(TEST_CLAIM), TEST_CLAIM_VALUE);
        Assert.assertEquals(context.getUserActionRequestDTO().getClaims().get(TEST_CLAIM_2),
                new String[]{TEST_CLAIM_VALUE_1,
                        TEST_CLAIM_VALUE_2});
        Assert.assertTrue(context.getUserActionRequestDTO().getGroups().contains(TEST_GROUP));
        Assert.assertTrue(context.getUserActionRequestDTO().getRoles().contains(TEST_ROLE));

        Assert.assertEquals(context.getUserActionResponseDTO().getUserId(), TEST_USER_ID);
        Assert.assertEquals(context.getUserActionResponseDTO().getPassword(), PASSWORD.toCharArray());
        Assert.assertEquals(context.getUserActionResponseDTO().getUserStoreDomain(), TEST_USER_STORE_DOMAIN);
        Assert.assertEquals(context.getUserActionResponseDTO().getClaims().get(TEST_CLAIM), TEST_CLAIM_VALUE);
        Assert.assertEquals(context.getUserActionResponseDTO().getClaims().get(TEST_CLAIM_2),
                new String[]{TEST_CLAIM_VALUE_1,
                        TEST_CLAIM_VALUE_2});
        Assert.assertTrue(context.getUserActionResponseDTO().getGroups().contains(TEST_GROUP));
        Assert.assertTrue(context.getUserActionResponseDTO().getRoles().contains(TEST_ROLE));
        Assert.assertEquals(context.getUserActionResponseDTO().getResidentOrganization(), ORGANIZATION);
        Assert.assertEquals(context.getUserActionResponseDTO().getSharedUserId(), SHARED_USER_ID);
    }

    @Test
    public void testUpdateUserActionResponseDTOFromUserActionContext() {

        UserActionRequestDTO userActionRequestDTO = new UserActionRequestDTO.Builder()
                .userId(TEST_USER_ID)
                .password(PASSWORD.toCharArray())
                .userStoreDomain(TEST_USER_STORE_DOMAIN)
                .residentOrganization(ORGANIZATION)
                .sharedUserId(SHARED_USER_ID)
                .build();
        UserActionContext context = new UserActionContext(userActionRequestDTO);

        Assert.assertNotNull(context.getUserActionResponseDTO());
        Assert.assertEquals(context.getUserActionResponseDTO().getUserId(), TEST_USER_ID);
        Assert.assertEquals(context.getUserActionResponseDTO().getPassword(), PASSWORD.toCharArray());
        Assert.assertEquals(context.getUserActionResponseDTO().getUserStoreDomain(), TEST_USER_STORE_DOMAIN);
        Assert.assertEquals(context.getUserActionResponseDTO().getResidentOrganization(), ORGANIZATION);
        Assert.assertEquals(context.getUserActionResponseDTO().getSharedUserId(), SHARED_USER_ID);

        context.getUserActionResponseDTO().addRole(TEST_ROLE);
        context.getUserActionResponseDTO().addGroup(TEST_GROUP);
        context.getUserActionResponseDTO().addClaim(TEST_CLAIM, TEST_CLAIM_VALUE);
        context.getUserActionResponseDTO().addClaim(TEST_CLAIM_2, new String[]{TEST_CLAIM_VALUE_1, TEST_CLAIM_VALUE_2});

        Assert.assertTrue(context.getUserActionResponseDTO().getGroups().contains(TEST_GROUP));
        Assert.assertTrue(context.getUserActionResponseDTO().getRoles().contains(TEST_ROLE));
        Assert.assertEquals(context.getUserActionResponseDTO().getClaims().get(TEST_CLAIM), TEST_CLAIM_VALUE);
        Assert.assertEquals(context.getUserActionResponseDTO().getClaims().get(TEST_CLAIM_2),
                new String[]{TEST_CLAIM_VALUE_1,
                        TEST_CLAIM_VALUE_2});
    }
}
