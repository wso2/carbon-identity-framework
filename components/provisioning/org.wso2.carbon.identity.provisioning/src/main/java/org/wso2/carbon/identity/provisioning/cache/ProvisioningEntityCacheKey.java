/*
 * Copyright (c) 2013, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import org.wso2.carbon.identity.application.common.cache.CacheKey;
import org.wso2.carbon.identity.provisioning.ProvisioningEntity;

public class ProvisioningEntityCacheKey extends CacheKey {

    private static final long serialVersionUID = -1414485745666304223L;

    private String identityProviderName;
    private String connectorType;
    private ProvisioningEntity provisioningEntity;

    /**
     * @param identityProviderName
     * @param connectorType
     * @param provisioningEntity
     */
    public ProvisioningEntityCacheKey(String identityProviderName, String connectorType,
                                      ProvisioningEntity provisioningEntity, String tenantDomain) {
        this.identityProviderName = identityProviderName;
        this.connectorType = connectorType;
        this.provisioningEntity = provisioningEntity;
        this.tenantDomain = tenantDomain.toLowerCase();
    }

    /**
     * @return
     */
    public String getIdentityProviderName() {
        return identityProviderName;
    }

    /**
     * @return
     */
    public String getConnectorType() {
        return connectorType;
    }

    /**
     * @return
     */
    public ProvisioningEntity getProvisioningEntity() {
        return provisioningEntity;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        ProvisioningEntityCacheKey that = (ProvisioningEntityCacheKey) o;

        if (!connectorType.equals(that.connectorType)) return false;
        if (!identityProviderName.equals(that.identityProviderName)) return false;
        if (!provisioningEntity.equals(that.provisioningEntity)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + identityProviderName.hashCode();
        result = 31 * result + connectorType.hashCode();
        result = 31 * result + provisioningEntity.hashCode();
        return result;
    }
}
