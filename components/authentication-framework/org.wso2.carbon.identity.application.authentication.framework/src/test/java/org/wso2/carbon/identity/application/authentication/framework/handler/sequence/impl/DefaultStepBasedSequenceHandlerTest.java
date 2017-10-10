/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.identity.application.authentication.framework.handler.sequence.impl;

import org.powermock.core.classloader.annotations.PrepareForTest;
import org.testng.Assert;
import org.testng.IObjectFactory;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.ObjectFactory;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.application.authentication.framework.util.FrameworkUtils;

import static org.testng.Assert.*;

@PrepareForTest(FrameworkUtils.class)
public class DefaultStepBasedSequenceHandlerTest {

    @ObjectFactory
    public IObjectFactory getObjectFactory() {
        return new org.powermock.modules.testng.PowerMockObjectFactory();
    }

    @BeforeMethod
    public void setUp() throws Exception {
    }

    @AfterMethod
    public void tearDown() throws Exception {
    }

    @Test
    public void testGetInstance() throws Exception {

        DefaultStepBasedSequenceHandler sequenceHandler = DefaultStepBasedSequenceHandler.getInstance();
        Assert.assertNotNull(sequenceHandler);

        DefaultStepBasedSequenceHandler anotherSequenceHandler = DefaultStepBasedSequenceHandler.getInstance();
        Assert.assertNotNull(anotherSequenceHandler);
        Assert.assertEquals(sequenceHandler, anotherSequenceHandler);
    }

    @Test
    public void testHandle() throws Exception {
    }

    @Test
    public void testHandlePostAuthentication() throws Exception {
    }

    @Test
    public void testGetServiceProviderMappedUserRoles() throws Exception {
    }

    @Test
    public void testGetSpRoleClaimUri() throws Exception {
    }

    @Test
    public void testGetIdpRoleClaimUri() throws Exception {
    }

    @Test
    public void testGetIdentityProvideMappedUserRoles() throws Exception {
    }

    @Test
    public void testHandleClaimMappings() throws Exception {
    }

    @Test
    public void testHandleJitProvisioning() throws Exception {
    }

    @Test
    public void testResetAuthenticationContext() throws Exception {
    }

}