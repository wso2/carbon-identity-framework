/*
 * Copyright (c) 2024, WSO2 LLC. (http://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
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
package org.wso2.carbon.identity.application.mgt.internal.cache;

import org.wso2.carbon.identity.application.common.model.TrustedApp;
import org.wso2.carbon.identity.core.cache.CacheEntry;

import java.util.List;

/**
 * Cache entry for trusted applications list of all tenants.
 */
public class TrustedAppPlatformTypeCacheEntry extends CacheEntry {

    private static final long serialVersionUID = -7123491976551481724L;
    private List<TrustedApp> trustedApps;

    public TrustedAppPlatformTypeCacheEntry(List<TrustedApp> trustedApps) {

        this.trustedApps = trustedApps;
    }

    public List<TrustedApp> getTrustedApps() {

        return trustedApps;
    }

    public void setTrustedApps(List<TrustedApp> trustedApps) {

        this.trustedApps = trustedApps;
    }
}
