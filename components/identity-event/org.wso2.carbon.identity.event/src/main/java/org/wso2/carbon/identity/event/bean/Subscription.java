/*
 * Copyright (c) 2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.identity.event.bean;

import java.util.Properties;

/**
 * Subscription bean is used to represent a subscription by a particular notification sending module. A set of
 * relevant subscriptions will be passed to a notification sending module
 */
@SuppressWarnings("unused")
public class Subscription {
    /**
     * Name of the subscription
     */
    protected String subscriptionName;
    /**
     * Subscription properties.
     */
    protected Properties subscriptionProperties;

    public Subscription(String subscriptionName, Properties subscriptionProperties) {
        this.subscriptionProperties = subscriptionProperties;
        this.subscriptionName = subscriptionName;
    }

    public String getSubscriptionName() {
        return subscriptionName;
    }

    public Properties getSubscriptionProperties() {
        return subscriptionProperties;
    }

    public void setSubscriptionProperties(Properties subscriptionProperties) {
        this.subscriptionProperties = subscriptionProperties;
    }
}
