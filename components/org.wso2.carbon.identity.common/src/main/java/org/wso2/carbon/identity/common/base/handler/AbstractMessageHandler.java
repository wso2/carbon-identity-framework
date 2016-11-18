/*
 *  Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
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

package org.wso2.carbon.identity.common.base.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.identity.common.base.Constants;
import org.wso2.carbon.identity.common.base.message.MessageContext;
import org.wso2.carbon.identity.common.util.IdentityUtils;
import org.wso2.carbon.identity.common.internal.handler.HandlerConfig;
import org.wso2.carbon.identity.common.internal.handler.HandlerConfigKey;

import java.util.Map;
import java.util.Properties;

public abstract class AbstractMessageHandler implements MessageHandler {

    private static Logger logger = LoggerFactory.getLogger(AbstractMessageHandler.class);

    protected final Properties properties = new Properties();

    protected InitConfig initConfig;

    public void init(InitConfig initConfig) {

        this.initConfig = initConfig;

        HandlerConfig identityEventListenerConfig = IdentityUtils.getInstance().getHandlerConfig()
                .get(new HandlerConfigKey(AbstractMessageHandler.class.getName(), this.getClass().getName()));

        if (identityEventListenerConfig == null) {
            return;
        }

        if(identityEventListenerConfig.getProperties() != null) {
            for(Map.Entry<Object,Object> property:identityEventListenerConfig.getProperties().entrySet()) {
                String key = (String)property.getKey();
                String value = (String)property.getValue();
                if(!properties.containsKey(key)) {
                    properties.setProperty(key, value);
                } else {
                    logger.warn("Property key " + key + " already exists. Cannot add property!!");
                }
            }
        }
    }

    public boolean isEnabled(MessageContext messageContext) {

        HandlerConfig identityEventListenerConfig = IdentityUtils.getInstance().getHandlerConfig()
                .get(new HandlerConfigKey(AbstractMessageHandler.class.getName(), this.getClass().getName()));

        if (identityEventListenerConfig == null) {
            return true;
        }

        return Boolean.parseBoolean(identityEventListenerConfig.getEnable());
    }

    public int getPriority(MessageContext messageContext) {

        HandlerConfig identityEventListenerConfig = IdentityUtils.getInstance().getHandlerConfig()
                .get(new HandlerConfigKey(AbstractMessageHandler.class.getName(), this.getClass().getName()));
        if (identityEventListenerConfig == null) {
            return Constants.EVENT_LISTENER_ORDER_DEFAULT;
        }
        return identityEventListenerConfig.getOrder();
    }

    public boolean canHandle(MessageContext messageContext) {
        return false;
    }

}
