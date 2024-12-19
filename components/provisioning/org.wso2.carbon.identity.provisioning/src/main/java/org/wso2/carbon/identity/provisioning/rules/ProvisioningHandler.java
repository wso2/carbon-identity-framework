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

package org.wso2.carbon.identity.provisioning.rules;

import org.wso2.carbon.identity.application.common.model.ServiceProvider;
import org.wso2.carbon.identity.provisioning.ProvisioningEntity;

/**
 * This interface is used to define the provisioning handler.
 */
public interface ProvisioningHandler {

    /**
     * This method is used to check whether the provisioning is allowed or not.
     *
     * @param tenantDomainName   Tenant domain name.
     * @param provisioningEntity Provisioning entity.
     * @param serviceProvider    Service provider.
     * @param idPName            Identity provider name.
     * @param connectorType      Connector type.
     * @return Whether the user provisioning is allowed or not.
     */
    boolean isAllowedToProvision(String tenantDomainName, ProvisioningEntity provisioningEntity,
                                 ServiceProvider serviceProvider,
                                 String idPName,
                                 String connectorType);
}
