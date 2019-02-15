/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.identity.event.handler;

import org.mockito.Matchers;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.base.IdentityRuntimeException;
import org.wso2.carbon.identity.core.handler.InitConfig;
import org.wso2.carbon.identity.event.IdentityEventConfigBuilder;
import org.wso2.carbon.identity.event.IdentityEventException;
import org.wso2.carbon.identity.event.bean.IdentityEventMessageContext;
import org.wso2.carbon.identity.event.bean.ModuleConfiguration;
import org.wso2.carbon.identity.event.bean.Subscription;
import org.wso2.carbon.identity.event.event.Event;
import org.wso2.carbon.identity.testutil.IdentityBaseTest;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public class AbstractEventHandlerTest extends IdentityBaseTest {

    List<Subscription> subscriptionList;
    ModuleConfiguration moduleConfiguration;
    String moduleName = TestEventHandler.class.getSimpleName();
    Map<String, ModuleConfiguration> moduleConfigurationMap;

    @BeforeMethod
    public void setUp() throws Exception {

        String home = IdentityEventConfigBuilder.class.getResource("/").getFile();
        String config = IdentityEventConfigBuilder.class.getResource("/").getFile();
        System.setProperty("carbon.home", home);
        System.setProperty("carbon.config.dir.path", config);

        subscriptionList = new ArrayList<>();
        moduleConfiguration = Mockito.mock(ModuleConfiguration.class);
        Mockito.doReturn(subscriptionList).when(moduleConfiguration).getSubscriptions();

        IdentityEventConfigBuilder identityEventConfigBuilder = Mockito.mock(IdentityEventConfigBuilder.class);
        Field field = IdentityEventConfigBuilder.class.getDeclaredField("notificationMgtConfigBuilder");
        field.setAccessible(true);
        field.set(null, identityEventConfigBuilder);
        Mockito.doReturn(moduleConfiguration).when(identityEventConfigBuilder).getModuleConfigurations(moduleName);

        moduleConfigurationMap = new HashMap<>();
        Mockito.doReturn(moduleConfigurationMap).when(identityEventConfigBuilder).getModuleConfiguration();
        Mockito.doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
                return moduleConfigurationMap.get(invocationOnMock.getArguments()[0]);
            }
        }).when(identityEventConfigBuilder).getModuleConfigurations(Matchers.anyString());
        ModuleConfiguration moduleConfiguration2 = new ModuleConfiguration(new Properties(), subscriptionList);
        moduleConfigurationMap.put("TestEventHandler", moduleConfiguration2);
    }

    @Test
    public void testCanHandle() throws IdentityEventException {

        Event event = new Event("eventName");
        IdentityEventMessageContext messageContext = new IdentityEventMessageContext(event);

        TestEventHandler testEventHandler = new TestEventHandler();
        testEventHandler.init(moduleConfiguration);
        boolean canHandle = testEventHandler.canHandle(messageContext);
        Assert.assertFalse(canHandle);

        subscriptionList.add(new Subscription("eventName", new Properties()));
        canHandle = testEventHandler.canHandle(messageContext);
        Assert.assertTrue(canHandle);
    }

    @Test
    public void testIsAssociationAsync() throws IdentityEventException {

        subscriptionList.add(new Subscription("eventName", new Properties()));
        TestEventHandler testEventHandler = new TestEventHandler();
        testEventHandler.init(moduleConfiguration);
        boolean isAssociationAsync = testEventHandler.isAssociationAsync("unknownEvent");

        Assert.assertFalse(isAssociationAsync);

        subscriptionList.add(new Subscription("testEvent", new Properties()));
        isAssociationAsync = testEventHandler.isAssociationAsync("testEvent");

        Assert.assertFalse(isAssociationAsync, "testEvent is an asynchronous event");

        Properties asyncProperties = new Properties();
        asyncProperties.setProperty("TestEventHandler.subscription.testAsyncEvent.operationAsync", "true");
        subscriptionList.add(new Subscription("testAsyncEvent", asyncProperties));

        isAssociationAsync = testEventHandler.isAssociationAsync("testAsyncEvent");
        Assert.assertTrue(isAssociationAsync, "testAsyncEvent is not an asynchronous event");
    }

    @Test
    public void testInit(){

        InitConfig configuration = new ModuleConfiguration();
        TestEventHandler testEventHandler = new TestEventHandler();
        testEventHandler.init(configuration);

        Assert.assertEquals(testEventHandler.configs,configuration);
    }

    @Test (expectedExceptions = {IdentityRuntimeException.class})
    public void testInitException() {

        InitConfig configuration = new InitConfig();
        TestEventHandler testEventHandler = new TestEventHandler();
        testEventHandler.init(configuration);

        Assert.assertEquals(testEventHandler.configs,configuration);
    }

    private class TestEventHandler extends AbstractEventHandler {

        @Override
        public void handleEvent(Event event) throws IdentityEventException {
            //do nothing
        }
    }
}



