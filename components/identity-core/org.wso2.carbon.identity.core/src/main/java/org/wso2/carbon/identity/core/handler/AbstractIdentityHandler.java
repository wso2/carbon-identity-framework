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

import org.wso2.carbon.identity.base.IdentityException;
import org.wso2.carbon.identity.base.IdentityRuntimeException;
import org.wso2.carbon.identity.core.bean.context.MessageContext;
import org.wso2.carbon.identity.core.model.IdentityEventListenerConfig;
import org.wso2.carbon.identity.core.util.IdentityCoreConstants;
import org.wso2.carbon.identity.core.util.IdentityUtil;

import java.util.Properties;

public abstract class AbstractIdentityHandler implements IdentityHandler {

    protected Properties properties = new Properties();

    public void init(Properties properties) throws IdentityRuntimeException {
        if(properties != null){
            this.properties = properties;
        }
    }

    public boolean isEnabled(MessageContext messageContext) throws IdentityException {

        IdentityEventListenerConfig identityEventListenerConfig = IdentityUtil.readEventListenerProperty
                (AbstractIdentityHandler.class.getName(), this.getClass().getName());

        if (identityEventListenerConfig == null) {
            return true;
        }

        return Boolean.parseBoolean(identityEventListenerConfig.getEnable());
    }

    public int getPriority(MessageContext messageContext) throws IdentityRuntimeException {

        IdentityEventListenerConfig identityEventListenerConfig = IdentityUtil.readEventListenerProperty
                (AbstractIdentityHandler.class.getName(), this.getClass().getName());
        if (identityEventListenerConfig == null) {
            return IdentityCoreConstants.EVENT_LISTENER_ORDER_ID;
        }
        return identityEventListenerConfig.getOrder();
    }

    public boolean canHandle(MessageContext messageContext) throws IdentityRuntimeException {
        return false;
    }

}
