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

import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.mgt.internal.IdentityMgtServiceDataHolder;
import org.wso2.carbon.user.api.RealmConfiguration;
import org.wso2.carbon.user.core.UserCoreConstants;
import org.wso2.carbon.user.core.common.AbstractUserStoreManager;
import org.wso2.carbon.user.core.common.AuthenticationResult;
import org.wso2.carbon.user.core.listener.UserOperationEventListener;

import java.util.Map;
import java.util.TreeMap;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertTrue;

public class IdentityUserNameResolverListenerTest {

    private static final String UNRESOLVABLE_USER_ID = "unresolvable-user-id";

    private AbstractUserStoreManager userStoreManager;
    private IdentityUserNameResolverListener listener;
    private UserOperationEventListener downstreamListener;

    @BeforeMethod
    public void setUp() throws Exception {

        RealmConfiguration realmConfiguration = mock(RealmConfiguration.class);
        when(realmConfiguration.getUserStoreProperty(UserCoreConstants.RealmConfig.PROPERTY_DOMAIN_NAME))
                .thenReturn(UserCoreConstants.PRIMARY_DEFAULT_DOMAIN_NAME);

        userStoreManager = mock(AbstractUserStoreManager.class);
        when(userStoreManager.getUserNameFromUserID(UNRESOLVABLE_USER_ID)).thenReturn(null);
        when(userStoreManager.getRealmConfiguration()).thenReturn(realmConfiguration);

        // The listener fans the resolved user name out to the registered listeners, so one has to be
        // registered for the tests to observe what they would be handed.
        downstreamListener = mock(UserOperationEventListener.class);
        when(downstreamListener.doPreAuthenticate(any(), any(), any())).thenReturn(true);
        when(downstreamListener.doPostAuthenticate(any(), anyBoolean(), any())).thenReturn(true);
        Map<Integer, UserOperationEventListener> listeners = new TreeMap<>();
        listeners.put(1, downstreamListener);
        IdentityMgtServiceDataHolder.getInstance().setUserOperationEventListeners(listeners);

        listener = spy(new IdentityUserNameResolverListener());
        doReturn(true).when(listener).isEnable();
    }

    @AfterMethod
    public void tearDown() {

        // The registry is static, so leaving the listener behind would leak into the other tests.
        IdentityMgtServiceDataHolder.getInstance().setUserOperationEventListeners(new TreeMap<>());
    }

    /**
     * A user ID that does not resolve to a user name is a supported outcome of
     * getUserNameFromUserID, and the listener is expected to carry on with the authentication rather
     * than fail it.
     */
    @Test
    public void testPreAuthenticateWithUnresolvableUserID() throws Exception {

        assertTrue(listener.doPreAuthenticateWithID(UNRESOLVABLE_USER_ID, "credential", userStoreManager),
                "Authentication should continue when the user ID cannot be resolved to a user name.");
        verify(downstreamListener, never()).doPreAuthenticate(any(), any(), any());
    }

    /**
     * A failed authentication resolves the user name from the user ID, so an unresolvable user ID
     * must not reach the post-authentication listeners as a null user name.
     */
    @Test
    public void testPostAuthenticateWithUnresolvableUserID() throws Exception {

        AuthenticationResult authenticationResult = mock(AuthenticationResult.class);
        when(authenticationResult.getAuthenticationStatus())
                .thenReturn(AuthenticationResult.AuthenticationStatus.FAIL);

        assertTrue(listener.doPostAuthenticateWithID(UNRESOLVABLE_USER_ID, authenticationResult, userStoreManager),
                "Post authentication should be skipped when the user ID cannot be resolved to a user name.");
        verify(downstreamListener, never()).doPostAuthenticate(any(), anyBoolean(), any());
    }
}
