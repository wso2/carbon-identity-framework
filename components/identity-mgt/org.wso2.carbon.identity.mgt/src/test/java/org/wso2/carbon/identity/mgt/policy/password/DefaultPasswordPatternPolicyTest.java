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

import junit.framework.TestCase;

import java.util.HashMap;
import java.util.Map;

public class DefaultPasswordPatternPolicyTest extends TestCase {

    DefaultPasswordPatternPolicy policy = new DefaultPasswordPatternPolicy();

    public void testPassPatterns() {

        Map<String, String> params = new HashMap<String, String>();
        params.put("pattern", "^((?=.*\\d)(?=.*[a-z])(?=.*[A-Z])(?=.*[!@#$%&*])).{0,100}$");
        policy.init(params);

        assertTrue("pattern check failed " + policy.getErrorMessage(), policy.enforce("passwordW@1"));
        assertTrue("pattern check failed " + policy.getErrorMessage(), policy.enforce("passwordDW@1"));
        assertTrue("pattern check failed " + policy.getErrorMessage(), policy.enforce("PASSWORd#5"));

    }

    public void testFailPatterns() {

        Map<String, String> params = new HashMap<String, String>();
        params.put("pattern", "^((?=.*\\d)(?=.*[a-z])(?=.*[A-Z])(?=.*[!@#$%&*])).{0,100}$");
        policy.init(params);

        assertFalse("pattern fail check failed " + policy.getErrorMessage(), policy.enforce("password@1"));
        assertFalse("pattern fail check failed " + policy.getErrorMessage(), policy.enforce("password"));
        assertFalse("pattern fail check failed " + policy.getErrorMessage(), policy.enforce("passwordW3"));
        assertFalse("pattern fail check failed " + policy.getErrorMessage(), policy.enforce("password#@1"));
        assertFalse("pattern fail check failed " + policy.getErrorMessage(), policy.enforce("PASSWORD@1"));
    }

    public void testNullInput() {

        assertTrue("null input check failed", policy.enforce(null));
    }
}
