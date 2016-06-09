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

package org.wso2.carbon.identity.core.handler;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.core.model.IdentityEventListenerConfig;
import org.wso2.carbon.identity.core.util.IdentityCoreConstants;
import org.wso2.carbon.identity.core.util.IdentityUtil;

import java.util.Map;
import java.util.Properties;

public abstract class AbstractIdentityHandler implements IdentityHandler {

    private static Log log = LogFactory.getLog(AbstractIdentityHandler.class);

    protected final Properties properties = new Properties();

    protected InitConfig initConfig;

    @Override
    public void init(InitConfig initConfig) {

        this.initConfig = initConfig;

        IdentityEventListenerConfig identityEventListenerConfig = IdentityUtil.readEventListenerProperty
                (AbstractIdentityHandler.class.getName(), this.getClass().getName());

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
                    log.warn("Property key " + key + " already exists. Cannot add property!!");
                }
            }
        }
    }

    @Override
    public boolean isEnabled() {

        IdentityEventListenerConfig identityEventListenerConfig = IdentityUtil.readEventListenerProperty
                (AbstractIdentityHandler.class.getName(), this.getClass().getName());

        if (identityEventListenerConfig == null) {
            return true;
        }

        return Boolean.parseBoolean(identityEventListenerConfig.getEnable());
    }

    @Override
    public int getPriority() {

        IdentityEventListenerConfig identityEventListenerConfig = IdentityUtil.readEventListenerProperty
                (AbstractIdentityHandler.class.getName(), this.getClass().getName());
        if (identityEventListenerConfig == null) {
            return IdentityCoreConstants.EVENT_LISTENER_ORDER_ID;
        }
        return identityEventListenerConfig.getOrder();
    }

    public String getName() {
        return this.getClass().getSimpleName();
    }
}
