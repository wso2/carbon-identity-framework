/*
 * Copyright (c) 2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.identity.mgt.policy.password;

import org.testng.annotations.Test;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

public class DefaultPasswordWhitespacePolicyTest {

    DefaultPasswordWhitespacePolicy policy = new DefaultPasswordWhitespacePolicy();

    @Test
    public void testWhiteSpacesInPassword() {
        assertFalse(policy.enforce("passwor s"), "white spaces in password check failed");
        assertFalse(policy.enforce("pass wor s"), "white spaces in password check failed");
        assertFalse(policy.enforce("pas   swor s"), "white spaces in password check failed");
    }

    @Test
    public void testCorrectPassword() {
        assertTrue(policy.enforce("passwors"), "no white spaces in password check failed");
    }

    @Test
    public void testNullInput() {
        assertTrue(policy.enforce(null), "null input failed");
    }
}
