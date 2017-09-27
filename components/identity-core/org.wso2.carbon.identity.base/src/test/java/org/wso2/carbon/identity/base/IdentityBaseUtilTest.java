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
import org.mockito.Mock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.testng.IObjectFactory;
import org.testng.annotations.ObjectFactory;
import org.testng.annotations.Test;
import org.wso2.carbon.base.ServerConfiguration;

import static org.mockito.Matchers.anyString;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.when;
import static org.testng.Assert.assertNotNull;

/**
 * Test class for IdentityBaseUtil test cases
 */
@PrepareForTest({ServerConfiguration.class})
public class IdentityBaseUtilTest {

    @Mock
    private ServerConfiguration mockServerConfig;

    @Test
    public void testGetDefaultRampartConfig() throws Exception {
        //mock ServerConfiguration
        mockStatic(ServerConfiguration.class);
        when(ServerConfiguration.getInstance()).thenReturn(mockServerConfig);
        when(mockServerConfig.getFirstProperty(anyString())).thenReturn("mockedValue");

        Policy policy = IdentityBaseUtil.getDefaultRampartConfig();
        assertNotNull(policy);
        assertNotNull(policy.getFirstPolicyComponent());
    }

    @ObjectFactory
    public IObjectFactory getObjectFactory() {
        return new org.powermock.modules.testng.PowerMockObjectFactory();
    }
}