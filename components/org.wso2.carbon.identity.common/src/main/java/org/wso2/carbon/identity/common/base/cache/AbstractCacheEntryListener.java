/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.identity.common.base.cache;

import org.apache.commons.lang3.StringUtils;
import org.wso2.carbon.identity.common.internal.handler.HandlerConfig;
import org.wso2.carbon.identity.common.internal.handler.HandlerConfigKey;
import org.wso2.carbon.identity.common.util.IdentityUtils;

import javax.cache.event.CacheEntryListener;

/**
 * Abstract Cache Entry Listener.
 * @param <K>
 * @param <V>
 */
public abstract class AbstractCacheEntryListener<K, V> implements CacheEntryListener<K, V> {

    /**
     * Return is listener enable.
     *
     * @return enable/disable
     */
    public boolean isEnable() {
        HandlerConfig handlerConfig = IdentityUtils.getInstance().getHandlerConfig()
                .get(new HandlerConfigKey(AbstractCacheEntryListener.class.getName(), this.getClass().getName()));

        if (handlerConfig == null) {
            return true;
        }

        if (StringUtils.isNotBlank(handlerConfig.getEnable())) {
            return Boolean.parseBoolean(handlerConfig.getEnable());
        } else {
            return true;
        }
    }
}
