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
import org.wso2.carbon.identity.event.IdentityEventException;
import org.wso2.carbon.identity.event.IdentityEventConfigBuilder;
import org.wso2.carbon.identity.event.bean.IdentityEventMessageContext;
import org.wso2.carbon.identity.event.bean.ModuleConfiguration;
import org.wso2.carbon.identity.event.bean.Subscription;
import org.wso2.carbon.identity.event.event.Event;

import java.util.List;
import java.util.Map;

public abstract class AbstractEventHandler extends AbstractIdentityMessageHandler {

    protected ModuleConfiguration configs;

    private static final Log log = LogFactory.getLog(AbstractEventHandler.class);

    public boolean canHandle(MessageContext messageContext) throws IdentityRuntimeException {

        Event event = ((IdentityEventMessageContext) messageContext).getEvent();
        String eventName = event.getEventName();
        String moduleName = this.getName();
        IdentityEventConfigBuilder notificationMgtConfigBuilder = null;
        try {
            notificationMgtConfigBuilder = IdentityEventConfigBuilder.getInstance();
        } catch (IdentityEventException e) {
            log.error("Error while retrieving event mgt config builder", e);
            return false;
        }
        List<Subscription> subscriptionList = null;
        ModuleConfiguration moduleConfiguration = null;
        if (notificationMgtConfigBuilder != null) {
            moduleConfiguration = notificationMgtConfigBuilder.getModuleConfigurations(moduleName);
        } else {
            return false;
        }
        if (moduleConfiguration != null) {
            subscriptionList = moduleConfiguration.getSubscriptions();
        } else {
            return false;
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

    public boolean isAssociationAsync(String eventName) throws IdentityEventException {
        Map<String, ModuleConfiguration> moduleConfigurationList = IdentityEventConfigBuilder.getInstance()
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

    public abstract void handleEvent(Event event) throws IdentityEventException;

    @Override
    public void init(InitConfig configuration) throws IdentityRuntimeException {
        if (configuration instanceof ModuleConfiguration) {
            this.configs = (ModuleConfiguration) configuration;
        } else {
            throw new IdentityRuntimeException("Initial configuration error");
        }
    }

}
