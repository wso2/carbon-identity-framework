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

package org.wso2.carbon.identity.application.authentication.framework.inbound;

import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.application.authentication.framework.exception.FrameworkException;
import org.wso2.carbon.identity.application.authentication.framework.inbound.HttpIdentityResponse.HttpIdentityResponseBuilder;
import org.wso2.carbon.identity.core.handler.InitConfig;
import org.wso2.carbon.identity.core.util.IdentityUtil;

import java.util.Properties;

import static org.mockito.Mockito.spy;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;

@PrepareForTest(IdentityUtil.class)
@PowerMockIgnore("org.mockito.*")
public class HttpIdentityResponseFactoryTest {

    private HttpIdentityResponseFactory httpIdentityResponseFactory;

    @BeforeMethod
    public void setUp() throws Exception {
        initMocks(this);
        httpIdentityResponseFactory = spy(HttpIdentityResponseFactory.class);
    }

    @AfterMethod
    public void tearDown() throws Exception {
    }

    @Test
    public void testInit() throws Exception {
        // Mock returning a null after reading event listener configs
        Util.mockReturnNullEventListenerConfig();
        InitConfig initConfig = new InitConfig();
        httpIdentityResponseFactory.init(initConfig);
        assertEquals(httpIdentityResponseFactory.properties.size(), 0);
    }

    @DataProvider(name = "initConfigDataProvider")
    public Object[][] initConfigDataProvider() {
        return Util.getEventListenerPropertyData();
    }

    @Test(dataProvider = "initConfigDataProvider")
    public void testInitWithDuplicateEventListenerProperties(Properties eventListenerProperties,
                                                             Properties expectedListenerProperties) throws Exception {

        Util.mockReturnEventListenerConfigWithProperties(eventListenerProperties);
        httpIdentityResponseFactory.init(new InitConfig());
        Util.assertPropertiesEqual(httpIdentityResponseFactory.properties, expectedListenerProperties);
    }

    @Test
    public void testCanHandle() throws Exception {
        assertFalse(httpIdentityResponseFactory.canHandle(new RuntimeException("ERROR_MESSAGE")));
    }

    @Test
    public void testCanHandleFrameworkException() throws Exception {
        assertFalse(httpIdentityResponseFactory.canHandle(new FrameworkException("ERROR_MESSAGE")));
    }

    @Test
    public void testCanHandle2() throws Exception {
    }

    @Test
    public void testHandleException() throws Exception {
        HttpIdentityResponseBuilder responseBuilder =
                httpIdentityResponseFactory.handleException(new FrameworkException("ERROR_MESSAGE"));
        assertEquals(responseBuilder.statusCode, 500);
    }

    @Test
    public void testHandleException1() throws Exception {
        HttpIdentityResponseBuilder responseBuilder =
                httpIdentityResponseFactory.handleException(new RuntimeException());
        assertEquals(responseBuilder.statusCode, 500);
    }
}
