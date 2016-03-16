/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.identity.event.handler;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.event.EventMgtException;
import org.wso2.carbon.identity.event.EventMgtConfigBuilder;
import org.wso2.carbon.identity.event.bean.ModuleConfiguration;
import org.wso2.carbon.identity.event.bean.Subscription;
import org.wso2.carbon.identity.event.event.Event;

import java.util.List;
import java.util.Map;

public abstract class AbstractEventHandler implements EventHandler {

    private static final Log log = LogFactory.getLog(AbstractEventHandler.class);

    // the event types registered in this handler
    protected List<String> registeredEventList;
    private String moduleName;

    @Override
    public String getModuleName() {
        return this.getClass().getSimpleName();
    }

    @Override
    public boolean isRegistered(Event event) throws EventMgtException {

        String eventName = event.getEventName();
        String moduleName = this.getModuleName();
        EventMgtConfigBuilder notificationMgtConfigBuilder = EventMgtConfigBuilder.getInstance();
        List<Subscription> subscriptionList = notificationMgtConfigBuilder.getModuleConfigurations(moduleName)
                .getSubscriptions();
        for (Subscription subscription : subscriptionList) {
            if (subscription.getSubscriptionName().equals(eventName)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean isAssociationAsync(String eventName) throws EventMgtException {
        Map<String, ModuleConfiguration> moduleConfigurationList = EventMgtConfigBuilder.getInstance()
                .getModuleConfiguration();
        ModuleConfiguration moduleConfiguration = moduleConfigurationList.get(this.getModuleName());
        List<Subscription> subscriptions = moduleConfiguration.getSubscriptions();
        for (Subscription sub : subscriptions) {
            if (sub.getSubscriptionName().equals(eventName)) {
                continue;
            }
            if (Boolean.parseBoolean(sub.getSubscriptionProperties().getProperty(this
                    .getModuleName() + ".subscription." + eventName + "" +
                    ".operationAsync"))) {
                return true;
            } else {
                return false;
            }
        }
        return false;
    }

}
