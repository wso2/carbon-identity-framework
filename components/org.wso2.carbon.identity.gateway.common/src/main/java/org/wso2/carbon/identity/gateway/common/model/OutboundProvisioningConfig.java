/*
 * Copyright (c) 2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.identity.gateway.common.model;

import java.io.Serializable;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class OutboundProvisioningConfig implements Serializable {

    private static final long serialVersionUID = 3669233357817378229L;

    private IdentityProvider[] provisioningIdentityProviders = new IdentityProvider[0];
    private String[] provisionByRoleList;



    /**
     * @return
     */
    public IdentityProvider[] getProvisioningIdentityProviders() {
        return provisioningIdentityProviders;
    }

    /**
     * @param provisioningIdentityProviders
     */
    public void setProvisioningIdentityProviders(IdentityProvider[] provisioningIdentityProviders) {
        if (provisioningIdentityProviders == null) {
            return;
        }
        Set<IdentityProvider> propertySet = new HashSet<IdentityProvider>(Arrays.asList(provisioningIdentityProviders));
        this.provisioningIdentityProviders = propertySet.toArray(new IdentityProvider[propertySet.size()]);
    }

    /**
     * @return
     */
    public String[] getProvisionByRoleList() {
        return provisionByRoleList;
    }

    /**
     * @param provisionByRoleList
     */
    public void setProvisionByRoleList(String[] provisionByRoleList) {
        this.provisionByRoleList = provisionByRoleList;
    }
}
