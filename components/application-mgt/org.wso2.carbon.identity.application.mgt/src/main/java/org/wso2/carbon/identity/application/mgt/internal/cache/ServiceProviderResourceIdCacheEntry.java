/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.identity.application.mgt.internal.cache;

import org.wso2.carbon.identity.application.common.model.ServiceProvider;
import org.wso2.carbon.identity.core.cache.CacheEntry;

/**
 * Cache entry for Service provider as a resource, which is read as resource ID.
 */
public class ServiceProviderResourceIdCacheEntry extends CacheEntry {

    private static final long serialVersionUID = -3899492482306158765L;
    private ServiceProvider serviceProvider;

    public ServiceProviderResourceIdCacheEntry(ServiceProvider serviceProvider) {

        this.serviceProvider = serviceProvider;
    }

    public ServiceProvider getServiceProvider() {

        return serviceProvider;
    }

    public void setServiceProvider(ServiceProvider serviceProvider) {

        this.serviceProvider = serviceProvider;
    }
}
