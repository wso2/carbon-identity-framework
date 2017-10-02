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

import org.apache.neethi.Policy;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import javax.xml.namespace.QName;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

/**
 * Test class for IdentityBaseUtil's default sign policy tests
 */
public class SignInPolicyTest {

    private String existingPolicy;

    @BeforeClass
    public void setup() {
        existingPolicy = IdentityBaseUtil.policyString;
    }

    @Test
    public void testGetSignOnlyPolicy() throws Exception {
        Policy signOnlyPolicy = IdentityBaseUtil.getSignOnlyPolicy();
        assertNotNull(signOnlyPolicy);
        QName qName = new QName("http://docs.oasis-open" +
                ".org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd", "Id", "wsu");
        assertEquals(signOnlyPolicy.getAttributes().get(qName), "SigOnly", "Invalid default " +
                "policy");
    }

    @Test(expectedExceptions = IdentityException.class)
    public void testGetSignOnlyPolicyInvalidPolicyString() throws Exception {
        IdentityBaseUtil.policyString = "Some Invalid Text";
        Policy signOnlyPolicy = IdentityBaseUtil.getSignOnlyPolicy();
        assertNotNull(signOnlyPolicy);
        QName qName = new QName("http://docs.oasis-open" +
                ".org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd", "Id", "wsu");
        assertEquals(signOnlyPolicy.getAttributes().get(qName), "SigOnly", "Invalid default " +
                "policy");
    }

    @AfterClass
    public void tearDown() {
        IdentityBaseUtil.policyString = existingPolicy;
    }
}