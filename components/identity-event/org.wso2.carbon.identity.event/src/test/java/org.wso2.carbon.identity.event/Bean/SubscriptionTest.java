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
package org.wso2.carbon.identity.event.Bean;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.event.bean.ModuleConfiguration;
import org.wso2.carbon.identity.event.bean.Subscription;
import java.util.Properties;

public class SubscriptionTest {

    Properties properties;
    String subscriptionName;
    Subscription subscription;

    @BeforeMethod
    public void setUp(){

        setPropertiesAndSubscription();
    }

    private void setPropertiesAndSubscription() {

        properties  = new Properties();
        subscriptionName = "subscriptionName";
        subscription = new Subscription(subscriptionName, properties);
    }

    @Test
    public void testGetSubscriptionName() {

        Assert.assertEquals(subscription.getSubscriptionName(),"subscriptionName");
    }

    @Test
    public void testGetSubscriptionProperties(){

        Assert.assertEquals(subscription.getSubscriptionProperties(), properties);
    }

    @Test
    public void testSetSubscriptionProperties(){

        Properties setProperties = new Properties();
        subscription.setSubscriptionProperties(setProperties);
        Assert.assertEquals(subscription.getSubscriptionProperties(), setProperties);
    }

    @Test
    public void testModuleConfiguration(){

        ModuleConfiguration moduleConfiguration = new ModuleConfiguration();
        Assert.assertEquals(moduleConfiguration.getModuleProperties(), properties);
    }
}
