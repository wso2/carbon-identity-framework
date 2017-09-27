/*
 *  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.wso2.carbon.identity.base;

import org.apache.ws.security.WSPasswordCallback;
import org.testng.annotations.Test;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.UnsupportedCallbackException;

import static org.testng.Assert.assertEquals;

/**
 * Test class for InMemoryPasswordCallbackHandler tests
 */
public class InMemoryPasswordCallbackHandlerTest {

    @Test
    public void testHandle() throws Exception {
        InMemoryPasswordCallbackHandler.addUser("TestUser", "1234ABCD");
        WSPasswordCallback callback = new WSPasswordCallback("TestUser", "TestPW", "TestType", 0);
        InMemoryPasswordCallbackHandler tested = new InMemoryPasswordCallbackHandler();
        tested.handle(new Callback[]{callback});
        assertEquals(callback.getPassword(), "1234ABCD", "Invalid password from callback");
    }

    @Test(expectedExceptions = UnsupportedCallbackException.class)
    public void testHandleInvalidUser() throws Exception {
        Callback dummyCallback = new NameCallback("Dummy");
        WSPasswordCallback callback = new WSPasswordCallback("NonExisting", "TestPW", "TestType", 0);
        InMemoryPasswordCallbackHandler tested = new InMemoryPasswordCallbackHandler();
        tested.handle(new Callback[]{callback, dummyCallback});
    }

}