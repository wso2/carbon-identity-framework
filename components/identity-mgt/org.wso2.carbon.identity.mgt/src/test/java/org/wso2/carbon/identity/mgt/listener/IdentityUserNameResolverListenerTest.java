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

package org.wso2.carbon.identity.mgt.listener;

import org.testng.annotations.Test;
import org.wso2.carbon.user.api.RealmConfiguration;
import org.wso2.carbon.user.core.UserCoreConstants;
import org.wso2.carbon.user.core.common.AbstractUserStoreManager;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertTrue;

public class IdentityUserNameResolverListenerTest {

    private static final String UNRESOLVABLE_USER_ID = "unresolvable-user-id";

    /**
     * A user ID that does not resolve to a user name is a supported outcome of
     * getUserNameFromUserID, and the listener is expected to carry on with the authentication rather
     * than fail it.
     */
    @Test
    public void testPreAuthenticateWithUnresolvableUserID() throws Exception {

        RealmConfiguration realmConfiguration = mock(RealmConfiguration.class);
        when(realmConfiguration.getUserStoreProperty(UserCoreConstants.RealmConfig.PROPERTY_DOMAIN_NAME))
                .thenReturn(UserCoreConstants.PRIMARY_DEFAULT_DOMAIN_NAME);

        AbstractUserStoreManager userStoreManager = mock(AbstractUserStoreManager.class);
        when(userStoreManager.getUserNameFromUserID(UNRESOLVABLE_USER_ID)).thenReturn(null);
        when(userStoreManager.getRealmConfiguration()).thenReturn(realmConfiguration);

        IdentityUserNameResolverListener listener = spy(new IdentityUserNameResolverListener());
        doReturn(true).when(listener).isEnable();

        assertTrue(listener.doPreAuthenticateWithID(UNRESOLVABLE_USER_ID, "credential", userStoreManager),
                "Authentication should continue when the user ID cannot be resolved to a user name.");
    }
}
