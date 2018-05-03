/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.user.mgt;

import org.junit.Test;
import org.wso2.carbon.user.core.UserStoreException;
import org.wso2.carbon.user.mgt.listeners.UserDeletionEventListener;

import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

/**
 * Unit tests related to the UserDeletionEventListener.
 */
public class UserDeletionEventListenerTest {

    private static final String DUMMY_USERNAME = "admin";

    @Test
    public void listenerDisabledTestCase() throws UserStoreException {

        UserDeletionEventListener userDeletionEventListener = spy(UserDeletionEventListener.class);
        when(userDeletionEventListener.isEnable()).thenReturn(false);

        userDeletionEventListener.doPostDeleteUser(DUMMY_USERNAME, null);
    }
}
