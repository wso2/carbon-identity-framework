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

package org.wso2.carbon.identity.provisioning;

import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.core.util.IdentityUtil;

import static org.testng.Assert.assertEquals;

/**
 * Unit tests for ProvisioningUtil.
 */
public class ProvisioningUtilTest {

    @DataProvider(name = "skipFullMemberListOnGroupUpdateConfigs")
    public Object[][] skipFullMemberListOnGroupUpdateConfigs() {

        return new Object[][]{
                {null, true},
                {"", true},
                {" ", true},
                {"true", true},
                {"false", false},
                {"invalid", false},
        };
    }

    @Test(dataProvider = "skipFullMemberListOnGroupUpdateConfigs")
    public void testIsSkipFullMemberListOnGroupUpdateEnabled(String configValue, boolean expected) {

        try (MockedStatic<IdentityUtil> identityUtilMockedStatic = Mockito.mockStatic(IdentityUtil.class)) {
            identityUtilMockedStatic.when(() -> IdentityUtil.getProperty(
                    IdentityProvisioningConstants.SKIP_FULL_MEMBER_LIST_ON_GROUP_UPDATE)).thenReturn(configValue);

            assertEquals(ProvisioningUtil.isSkipFullMemberListOnGroupUpdateEnabled(), expected);
        }
    }
}
