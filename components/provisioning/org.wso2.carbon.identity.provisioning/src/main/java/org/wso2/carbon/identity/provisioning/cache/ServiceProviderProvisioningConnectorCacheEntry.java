/*
 * Copyright (c) 2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.identity.provisioning.cache;

import org.wso2.carbon.identity.application.common.cache.CacheEntry;
import org.wso2.carbon.identity.provisioning.RuntimeProvisioningConfig;

import java.util.Map;

public class ServiceProviderProvisioningConnectorCacheEntry extends CacheEntry {

    private static final long serialVersionUID = -2523580603873437939L;

    private Map<String, RuntimeProvisioningConfig> connectors = null;

    public Map<String, RuntimeProvisioningConfig> getConnectors() {
        return connectors;
    }

    public void setConnectors(Map<String, RuntimeProvisioningConfig> connectors) {
        this.connectors = connectors;
    }

}
