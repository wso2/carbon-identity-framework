
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


package org.wso2.carbon.identity.application.common.model;

import java.io.Serializable;

public class ThreadLocalProvisioningServiceProvider implements Serializable {

    private static final long serialVersionUID = 8869773391988526466L;

    private String serviceProviderName;
    private String claimDialect;
    private boolean justInTimeProvisioning;
    private transient ProvisioningServiceProviderType serviceProviderType;
    private String tenantDomain;
    //isBulkUserAdd is true indicates bulk user add
    private boolean isBulkUserAdd;

    public boolean isBulkUserAdd() {
        return isBulkUserAdd;
    }

    public void setBulkUserAdd(boolean isBulkUserAdd) {
        this.isBulkUserAdd = isBulkUserAdd;
    }

    /**
     * @return
     */
    public String getServiceProviderName() {
        return serviceProviderName;
    }

    /**
     * @param serviceProviderName
     */
    public void setServiceProviderName(String serviceProviderName) {
        this.serviceProviderName = serviceProviderName;
    }

    /**
     * @return
     */
    public String getClaimDialect() {
        return claimDialect;
    }

    /**
     * @param claimDialect
     */
    public void setClaimDialect(String claimDialect) {
        this.claimDialect = claimDialect;
    }

    /**
     * @return
     */
    public boolean isJustInTimeProvisioning() {
        return justInTimeProvisioning;
    }

    /**
     * @param justInTimeProvisioning
     */
    public void setJustInTimeProvisioning(boolean justInTimeProvisioning) {
        this.justInTimeProvisioning = justInTimeProvisioning;
    }

    /**
     * @return
     */
    public ProvisioningServiceProviderType getServiceProviderType() {
        return serviceProviderType;
    }

    /**
     * @param serviceProviderType
     */
    public void setServiceProviderType(ProvisioningServiceProviderType serviceProviderType) {
        this.serviceProviderType = serviceProviderType;
    }

    /**
     * @return
     */
    public String getTenantDomain() {
        return tenantDomain;
    }

    /**
     * @param tenantDomain
     */
    public void setTenantDomain(String tenantDomain) {
        this.tenantDomain = tenantDomain;
    }

}
