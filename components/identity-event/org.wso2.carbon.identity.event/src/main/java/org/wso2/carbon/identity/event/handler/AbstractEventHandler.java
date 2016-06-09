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
import org.wso2.carbon.identity.base.IdentityRuntimeException;
import org.wso2.carbon.identity.core.bean.context.MessageContext;
import org.wso2.carbon.identity.core.handler.AbstractIdentityMessageHandler;
import org.wso2.carbon.identity.core.handler.InitConfig;
import org.wso2.carbon.identity.event.EventMgtException;
import org.wso2.carbon.identity.event.EventMgtConfigBuilder;
import org.wso2.carbon.identity.event.bean.IdentityEventMessageContext;
import org.wso2.carbon.identity.event.bean.ModuleConfiguration;
import org.wso2.carbon.identity.event.bean.Subscription;
import org.wso2.carbon.identity.event.event.Event;
import org.wso2.carbon.identity.event.internal.EventMgtServiceDataHolder;

import java.util.List;
import java.util.Map;
import java.util.Set;

public abstract class AbstractEventHandler extends AbstractIdentityMessageHandler {

    private static final Log log = LogFactory.getLog(AbstractEventHandler.class);

    // the event types registered in this handler
    protected List<String> registeredEventList;
    private String moduleName;

    public boolean canHandle(MessageContext messageContext) throws IdentityRuntimeException {

        Event event = ((IdentityEventMessageContext)messageContext).getEvent();
        String eventName = event.getEventName();
        String moduleName = this.getName();
        EventMgtConfigBuilder notificationMgtConfigBuilder = null;
        try {
            notificationMgtConfigBuilder = EventMgtConfigBuilder.getInstance();
        } catch (EventMgtException e) {
            log.error("Error while retrieving event mgt config builder", e);
        }
        List<Subscription> subscriptionList = notificationMgtConfigBuilder.getModuleConfigurations(moduleName)
                .getSubscriptions();
        for (Subscription subscription : subscriptionList) {
            if (subscription.getSubscriptionName().equals(eventName)) {
                return true;
            }
        }
        return false;
    }

    public boolean isAssociationAsync(String eventName) throws EventMgtException {
        Map<String, ModuleConfiguration> moduleConfigurationList = EventMgtConfigBuilder.getInstance()
                .getModuleConfiguration();
        ModuleConfiguration moduleConfiguration = moduleConfigurationList.get(this.getName());
        List<Subscription> subscriptions = moduleConfiguration.getSubscriptions();
        for (Subscription sub : subscriptions) {
            if (sub.getSubscriptionName().equals(eventName)) {
                continue;
            }
            if (Boolean.parseBoolean(sub.getSubscriptionProperties().getProperty(this
                    .getName() + ".subscription." + eventName + "" +
                    ".operationAsync"))) {
                return true;
            } else {
                return false;
            }
        }
        return false;
    }

//    public Map<String, String> getTenantConfigurations (int tenantId) throws EventMgtException {
//        return EventMgtServiceDataHolder.getInstance().getEventMgtService().getConfiguration(tenantId);
//    }

    public abstract boolean handleEvent(Event event) throws EventMgtException;

    @Override
    public void init(InitConfig configuration) throws IdentityRuntimeException {
        ModuleConfiguration moduleConfig = (ModuleConfiguration)configuration;
        Set<String> modulePropertyNames = moduleConfig.getModuleProperties().stringPropertyNames();
        for (String modulePropertyName : modulePropertyNames) {
            properties.put(modulePropertyName, moduleConfig.getModuleProperties().getProperty(modulePropertyName));
        }
        List<Subscription> subscriptions = moduleConfig.getSubscriptions();
        for (Subscription subscription : subscriptions) {
            modulePropertyNames = subscription.getSubscriptionProperties().stringPropertyNames();
            for (String modulePropertyName : modulePropertyNames) {
                properties.put(modulePropertyName, subscription.getSubscriptionProperties().getProperty(modulePropertyName));
            }
        }
    }

}
