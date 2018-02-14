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

import java.util.HashMap;
import java.util.Map;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

public class DefaultPasswordPatternPolicyTest {

    DefaultPasswordPatternPolicy policy = new DefaultPasswordPatternPolicy();

    @Test
    public void testPassPatterns() {
        Map<String, String> params = new HashMap<String, String>();
        params.put("pattern", "^((?=.*\\d)(?=.*[a-z])(?=.*[A-Z])(?=.*[!@#$%&*])).{0,100}$");
        policy.init(params);

        assertTrue(policy.enforce("passwordW@1"), "pattern check failed " + policy.getErrorMessage());
        assertTrue(policy.enforce("passwordDW@1"), "pattern check failed " + policy.getErrorMessage());
        assertTrue(policy.enforce("PASSWORd#5"), "pattern check failed " + policy.getErrorMessage());

    }

    @Test
    public void testFailPatterns() {
        Map<String, String> params = new HashMap<String, String>();
        params.put("pattern", "^((?=.*\\d)(?=.*[a-z])(?=.*[A-Z])(?=.*[!@#$%&*])).{0,100}$");
        policy.init(params);

        assertFalse(policy.enforce("password@1"), "pattern fail check failed " + policy.getErrorMessage());
        assertFalse(policy.enforce("password"), "pattern fail check failed " + policy.getErrorMessage());
        assertFalse(policy.enforce("passwordW3"), "pattern fail check failed " + policy.getErrorMessage());
        assertFalse(policy.enforce("password#@1"), "pattern fail check failed " + policy.getErrorMessage());
        assertFalse(policy.enforce("PASSWORD@1"), "pattern fail check failed " + policy.getErrorMessage());
    }

    @Test
    public void testNullInput() {
        assertTrue(policy.enforce(null), "null input check failed");
    }
}
