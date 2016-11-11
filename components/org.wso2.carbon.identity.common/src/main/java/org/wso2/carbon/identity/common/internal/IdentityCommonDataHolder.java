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
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.identity.common.internal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.identity.common.internal.cache.CacheConfig;
import org.wso2.carbon.identity.common.internal.cache.CacheConfigKey;
import org.wso2.carbon.identity.common.internal.handler.HandlerConfig;
import org.wso2.carbon.identity.common.internal.handler.HandlerConfigKey;
import org.wso2.carbon.identity.common.internal.config.ConfigParser;
import org.wso2.carbon.identity.common.internal.cookie.CookieConfig;
import org.wso2.carbon.identity.common.internal.cookie.CookieConfigKey;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;

public class IdentityCommonDataHolder {

    private static Logger logger = LoggerFactory.getLogger(ConfigParser.class);

    private static IdentityCommonDataHolder instance;
    private Map<HandlerConfigKey, HandlerConfig> handlerConfig = new HashMap();
    private Map<CacheConfigKey, CacheConfig> cacheConfig = new HashMap();
    private Map<CookieConfigKey, CookieConfig> cookieConfig = new HashMap();

    private IdentityCommonDataHolder() {

    }

    public static IdentityCommonDataHolder getInstance() {
        if(instance == null) {
            synchronized (IdentityCommonDataHolder.class) {
                if (instance == null) {
                    instance = new IdentityCommonDataHolder();
                }
            }
        }
        return instance;
    }

    public void setHandlerConfig(Map<HandlerConfigKey, HandlerConfig> handlerConfig) {
        this.handlerConfig = handlerConfig;
    }

    public void setCacheConfig(Map<CacheConfigKey, CacheConfig> cacheConfig) {
        this.cacheConfig = cacheConfig;
    }

    public void setCookieConfig(Map<CookieConfigKey, CookieConfig> cookieConfig) {
        this.cookieConfig = cookieConfig;
    }

    public Map<HandlerConfigKey, HandlerConfig> getHandlerConfig() {
        return handlerConfig;
    }

    public Map<CacheConfigKey, CacheConfig> getCacheConfig() {
        return cacheConfig;
    }

    public Map<CookieConfigKey, CookieConfig> getCookieConfig() {
        return cookieConfig;
    }

}
