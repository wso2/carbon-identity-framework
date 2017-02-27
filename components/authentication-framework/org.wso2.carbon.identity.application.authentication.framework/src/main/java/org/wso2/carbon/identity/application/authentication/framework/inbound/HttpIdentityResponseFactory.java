/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.identity.application.authentication.framework.inbound;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.application.authentication.framework.exception.FrameworkException;
import org.wso2.carbon.identity.core.handler.AbstractIdentityHandler;
import org.wso2.carbon.identity.core.handler.InitConfig;
import org.wso2.carbon.identity.core.model.IdentityEventListenerConfig;
import org.wso2.carbon.identity.core.util.IdentityUtil;

import java.util.Map;
import java.util.Properties;

public abstract class HttpIdentityResponseFactory extends AbstractIdentityHandler {

    private static Log log = LogFactory.getLog(HttpIdentityResponseFactory.class);

    protected final Properties properties = new Properties();

    protected InitConfig initConfig;

    public void init(InitConfig initConfig) {

        this.initConfig = initConfig;

        IdentityEventListenerConfig identityEventListenerConfig = IdentityUtil.readEventListenerProperty
                (HttpIdentityResponseFactory.class.getName(), this.getClass().getName());

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


    public abstract boolean canHandle(IdentityResponse identityResponse);

    public boolean canHandle(FrameworkException exception) {
        return false;
    }

    public boolean canHandle(RuntimeException exception) {
        return false;
    }

    public abstract HttpIdentityResponse.HttpIdentityResponseBuilder create(IdentityResponse identityResponse);

    public abstract void create(
            HttpIdentityResponse.HttpIdentityResponseBuilder builder, IdentityResponse identityResponse);

    public HttpIdentityResponse.HttpIdentityResponseBuilder handleException(FrameworkException exception) {

        HttpIdentityResponse.HttpIdentityResponseBuilder builder = new HttpIdentityResponse.HttpIdentityResponseBuilder();
        builder.setStatusCode(500);
        return builder;
    }

    public HttpIdentityResponse.HttpIdentityResponseBuilder handleException(RuntimeException exception) {

        HttpIdentityResponse.HttpIdentityResponseBuilder builder = new HttpIdentityResponse.HttpIdentityResponseBuilder();
        builder.setStatusCode(500);
        return builder;
    }
}
