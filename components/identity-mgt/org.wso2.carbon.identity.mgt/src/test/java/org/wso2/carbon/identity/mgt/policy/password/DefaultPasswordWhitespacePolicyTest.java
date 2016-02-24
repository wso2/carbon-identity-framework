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

public class DefaultPasswordWhitespacePolicyTest extends TestCase {

	DefaultPasswordWhitespacePolicy policy = new DefaultPasswordWhitespacePolicy();
	
	public void testWhitespacesinPassword() {
		
		assertFalse("white spaces in password check failed", policy.enforce("passwor s"));
		assertFalse("white spaces in password check failed", policy.enforce("pass wor s"));
		assertFalse("white spaces in password check failed", policy.enforce("pas   swor s"));
	}
	
	public void testCorrectPassword() {
		
		assertTrue("no white spaces in password check failed", policy.enforce("passwors"));
	}
	
	public void testNullInput(){
		
		assertTrue("null input failed", policy.enforce(null));
	}
}
