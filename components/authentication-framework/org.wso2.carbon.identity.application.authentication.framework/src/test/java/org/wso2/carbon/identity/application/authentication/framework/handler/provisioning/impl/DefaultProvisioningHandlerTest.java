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

package org.wso2.carbon.identity.application.authentication.framework.handler.provisioning.impl;

import org.mockito.Mockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.testng.PowerMockTestCase;
import org.testng.IObjectFactory;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.ObjectFactory;
import org.testng.annotations.Test;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.identity.application.authentication.framework.exception.FrameworkException;
import org.wso2.carbon.identity.application.authentication.framework.util.FrameworkUtils;
import org.wso2.carbon.identity.application.authentication.framwork.test.utils.CommonTestUtils;

import static org.powermock.api.mockito.PowerMockito.doNothing;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

@PrepareForTest({FrameworkUtils.class, PrivilegedCarbonContext.class})
//@PowerMockIgnore({"org.xml.*","org.w3c.*"})
public class DefaultProvisioningHandlerTest extends PowerMockTestCase {

    private DefaultProvisioningHandler provisioningHandler;

    @BeforeMethod
    public void setUp() throws Exception {
        provisioningHandler = new DefaultProvisioningHandler();
        mockStatic(PrivilegedCarbonContext.class);
        PrivilegedCarbonContext privilegedCarbonContext = Mockito.mock(PrivilegedCarbonContext.class);
        Mockito.when(PrivilegedCarbonContext.getThreadLocalCarbonContext()).thenReturn(privilegedCarbonContext);
        CommonTestUtils.initPrivilegedCarbonContext();
    }

    @AfterMethod
    public void tearDown() throws Exception {
        PrivilegedCarbonContext.endTenantFlow();
    }

    @Test
    public void testGetInstance() throws Exception {
        CommonTestUtils.testSingleton(
                DefaultProvisioningHandler.getInstance(),
                DefaultProvisioningHandler.getInstance()
        );
    }

    @Test
    public void testHandle() throws Exception {
    }

    @ObjectFactory
    public IObjectFactory getObjectFactory() {
        return new org.powermock.modules.testng.PowerMockObjectFactory();
    }

    @DataProvider(name = "associateUserEmptyInputProvider")
    public Object[][] getAssociatedUserEmptyInputs() {
        return new Object[][]{
                {"", null},
                {"", ""},
                {null, ""},
                {null, null},
        };
    }

    @Test(dataProvider = "associateUserEmptyInputProvider", expectedExceptions = FrameworkException.class)
    public void testAssociateUserEmptyInputs(String subject,
                                             String idp) throws Exception {

        mockStatic(FrameworkUtils.class);
        doNothing().when(FrameworkUtils.class, "startTenantFlow", "tenantDomain");
        provisioningHandler.associateUser("dummy_user_name", "DUMMY_DOMAIN", "dummy.com", subject, idp);
    }

    @Test
    public void testGeneratePassword() throws Exception {
        String randomPassword = provisioningHandler.generatePassword();
        assertNotNull(randomPassword);
        assertEquals(randomPassword.length(), 12);
    }
}
