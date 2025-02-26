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
import org.wso2.carbon.identity.user.action.api.model.UserActionContext;

/**
 * Test class for UserActionContext.
 */
public class UserActionContextTest {

    public static final String TEST_USER_ID = "testUser";
    public static final String PASSWORD = "testPassword";
    public static final String TEST_USER_STORE_DOMAIN = "testUserStoreDomain";

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
}
