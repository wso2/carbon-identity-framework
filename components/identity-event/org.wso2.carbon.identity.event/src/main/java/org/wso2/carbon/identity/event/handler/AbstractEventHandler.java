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
import org.wso2.carbon.identity.event.IdentityEventConfigBuilder;
import org.wso2.carbon.identity.event.IdentityEventException;
import org.wso2.carbon.identity.event.bean.IdentityEventMessageContext;
import org.wso2.carbon.identity.event.bean.ModuleConfiguration;
import org.wso2.carbon.identity.event.bean.Subscription;
import org.wso2.carbon.identity.event.event.Event;

import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

public abstract class AbstractEventHandler extends AbstractIdentityMessageHandler {

    protected ModuleConfiguration configs;

    private static final Log log = LogFactory.getLog(AbstractEventHandler.class);

    public boolean canHandle(MessageContext messageContext) throws IdentityRuntimeException {

        Event event = ((IdentityEventMessageContext) messageContext).getEvent();
        String eventName = event.getEventName();

        List<Subscription> subscriptionList = null;
        if (configs != null) {
            subscriptionList = configs.getSubscriptions();
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

    /**
     * Each event has its own subscriptions (configure in identity-event.properties) and it is possible to define
     * multiple properties for each subscription per event under the given module.
     * This method will allow to get the properties for event subscription under the current module.
     *
     * @param eventName Current Event Name
     * @return Property Map for the given event name under the current module.
     * @throws IdentityEventException
     */
    public Properties getSubscriptionProperties(String eventName) throws IdentityEventException {
        if (log.isDebugEnabled()) {
            log.debug("Get the subscription properties for event : " + eventName);
        }
        Properties subscriptionProperties = new Properties();

        List<Subscription> subscriptions = configs.getSubscriptions();
        for (Subscription sub : subscriptions) {
            if (sub.getSubscriptionName().equals(eventName)) {
                subscriptionProperties = sub.getSubscriptionProperties();
                break;
            }
        }
        if (log.isDebugEnabled() && subscriptionProperties != null) {
            log.debug("List of subscription properties for event : " + eventName);
            for (Object key : subscriptionProperties.keySet()) {
                log.debug("Key : " + key + " Value : " + subscriptionProperties.getProperty((String) key));
            }
        }
        return subscriptionProperties;
    }

    /**
     * Each event has its own subscriptions (configure in identity-event.properties) and it is possible to define
     * multiple properties for each subscription per event under the given module.
     * This method will allow to get the property value for event subscription and property name under the current module.
     *
     * @param propertyName Required property name to be read.
     * @param eventName    Current Event Name.
     * @return Return the String value of that property value.
     * @throws IdentityEventException
     */
    public String getSubscriptionProperty(String propertyName, String eventName) throws IdentityEventException {
        if (log.isDebugEnabled()) {
            log.debug("Get the subscription property value for property : " + propertyName
                    + " for event : " + eventName);
        }
        return getSubscriptionProperties(eventName).getProperty(this
                .getName() + ".subscription." + eventName + "." + propertyName);
    }



    /**
     * If the association which is given in the subscription property is true for 'operationAsync', then this method
     * will return true and the event will drop to queue to process based on the priority.
     * Otherwise, it will call in the same thread.
     *
     * @param eventName Current Event Name
     * @return
     * @throws IdentityEventException
     */
    public boolean isAssociationAsync(String eventName) throws IdentityEventException {
        if (log.isDebugEnabled()) {
            log.debug("Validate the association is sync or not.");
        }
        return Boolean.parseBoolean(getSubscriptionProperty("operationAsync", eventName));
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
