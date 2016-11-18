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

package org.wso2.carbon.identity.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.identity.common.base.exception.IdentityRuntimeException;
import org.wso2.carbon.identity.common.base.handler.AbstractMessageHandler;
import org.wso2.carbon.identity.common.base.handler.InitConfig;
import org.wso2.carbon.identity.common.base.message.MessageContext;
import org.wso2.carbon.identity.event.internal.ConfigParser;
import org.wso2.carbon.identity.event.model.Event;
import org.wso2.carbon.identity.event.model.ModuleConfig;
import org.wso2.carbon.identity.event.model.Subscription;

import java.util.List;
import java.util.Map;

public abstract class AbstractEventHandler extends AbstractMessageHandler {

    protected ModuleConfig moduleConfig;

    private static final Logger logger = LoggerFactory.getLogger(AbstractEventHandler.class);

    public boolean canHandle(MessageContext messageContext) throws IdentityRuntimeException {

        Event event = ((EventMessageContext)messageContext).getEvent();
        String eventName = event.getEventName();
        String moduleName = this.getName();
        ConfigParser notificationMgtConfigBuilder = null;
        try {
            notificationMgtConfigBuilder = ConfigParser.getInstance();
        } catch (EventException e) {
            logger.error("Error while retrieving event mgt config builder", e);
        }
        List<Subscription> subscriptionList = null;
        ModuleConfig moduleConfig = notificationMgtConfigBuilder.getModuleConfigurations(moduleName);
        if (moduleConfig != null) {
            subscriptionList = moduleConfig.getSubscriptions();
        }
        if (subscriptionList != null) {
            for (Subscription subscription : subscriptionList) {
                if (subscription.getSubscriptionName().equals(eventName)) {
                    return true;
                }
            }
        }

        return false;
    }

    public boolean isAssociationAsync(String eventName) throws EventException {
        Map<String, ModuleConfig> moduleConfigurationList = ConfigParser.getInstance()
                .getModuleConfiguration();
        ModuleConfig moduleConfig = moduleConfigurationList.get(this.getName());
        List<Subscription> subscriptions = moduleConfig.getSubscriptions();
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

    public abstract void handleEvent(Event event) throws EventException;

    @Override
    public void init(InitConfig configuration) throws IdentityRuntimeException {
        this.moduleConfig = (ModuleConfig)configuration;
    }

}
