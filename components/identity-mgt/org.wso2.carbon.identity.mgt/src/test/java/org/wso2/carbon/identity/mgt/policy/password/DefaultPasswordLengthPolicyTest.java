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

public class DefaultPasswordLengthPolicyTest extends TestCase {

	DefaultPasswordLengthPolicy policy = new DefaultPasswordLengthPolicy();

	public void testMaxLength() {

		assertFalse("max length check failed", policy.enforce("password123"));
		assertTrue("max length check failed", policy.enforce("password"));
	}

	public void testMinLength() {

		assertFalse("min length check failed", policy.enforce("passw"));
		assertTrue("min length check failed", policy.enforce("password"));

	}

	public void testNullInput() {

		assertTrue("null input check failed", policy.enforce(null));
	}
}
