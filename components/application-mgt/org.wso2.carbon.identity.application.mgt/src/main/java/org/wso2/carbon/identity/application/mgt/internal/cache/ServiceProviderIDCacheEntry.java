/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.wso2.carbon.identity.application.mgt.internal.cache;

import org.wso2.carbon.identity.application.common.cache.CacheEntry;

public class ServiceProviderIDCacheEntry extends CacheEntry {

    private static final long serialVersionUID = 5060231898427225662L;
    private String serviceProvider;
    private String tenantName;

    public ServiceProviderIDCacheEntry(String serviceProvider, String tenantName) {

        this.serviceProvider = serviceProvider;
        this.tenantName = tenantName;
    }

    public String getServiceProvider() {

        return serviceProvider;
    }

    public void setServiceProvider(String serviceProvider) {

        this.serviceProvider = serviceProvider;
    }

    public String getTenantName() {

        return tenantName;
    }

    public void setTenantName(String tenantName) {

        this.tenantName = tenantName;
    }
}
