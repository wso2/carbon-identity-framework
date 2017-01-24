/*
 * Copyright (c) 2014 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.idp.mgt.util;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.application.common.model.IdentityProvider;
import org.wso2.carbon.identity.application.common.model.Property;
import org.wso2.carbon.identity.application.common.model.ProvisioningConnectorConfig;
import org.wso2.carbon.idp.mgt.internal.IdPManagementServiceComponent;
import org.wso2.carbon.user.api.TenantManager;
import org.wso2.carbon.user.api.UserStoreException;

public class IdPManagementUtil {

    private static final Log log = LogFactory.getLog(IdPManagementUtil.class);

    /**
     * Get the tenant id of the given tenant domain.
     *
     * @param tenantDomain Tenant Domain
     * @return Tenant Id of domain user belongs to.
     * @throws UserStoreException Error when getting tenant id from tenant domain
     */
    public static int getTenantIdOfDomain(String tenantDomain) throws UserStoreException {

        if (tenantDomain != null) {
            TenantManager tenantManager = IdPManagementServiceComponent.getRealmService()
                    .getTenantManager();
            int tenantId = tenantManager.getTenantId(tenantDomain);
            return tenantId;
        } else {
            log.debug("Invalid tenant domain: \'NULL\'");
            throw new IllegalArgumentException("Invalid tenant domain: \'NULL\'");
        }
    }

    /**
     * +     * Get the resident entity id configured in identity.xml.
     * +     *
     * +
     */
    public static String getResidentIdPEntityId() {
//        String localEntityId = IdentityUtil.getProperty("SSOService.EntityId");
//        if (localEntityId == null || localEntityId.trim().isEmpty()) {
//            localEntityId = "localhost";
//        }
        return null;
    }

    public static int getIdleSessionTimeOut() {

        return 10 * 60;
    }

    public static int getRememberMeTimeout() {

        return 10 * 60;
    }

    /**
     * Use this method to replace original passwords with random passwords before sending to UI front-end
     *
     * @param identityProvider
     * @return
     */
    public static void removeOriginalPasswords(IdentityProvider identityProvider) {

        if (identityProvider == null || identityProvider.getProvisioningConnectorConfigs() == null) {
            return;
        }

        for (ProvisioningConnectorConfig provisioningConnectorConfig : identityProvider
                .getProvisioningConnectorConfigs()) {
            Property[] properties = provisioningConnectorConfig.getProvisioningProperties();
            if (ArrayUtils.isEmpty(properties)) {
                continue;
            }
            //properties = RandomPasswordProcessor.getInstance().removeOriginalPasswords(properties);
            provisioningConnectorConfig.setProvisioningProperties(properties);
        }
    }

    /**
     * Use this method to replace random passwords with original passwords when original passwords are required
     *
     * @param identityProvider
     * @param withCacheClear
     */
    public static void removeRandomPasswords(IdentityProvider identityProvider, boolean withCacheClear) {

        if (identityProvider == null || identityProvider.getProvisioningConnectorConfigs() == null) {
            return;
        }
        for (ProvisioningConnectorConfig provisioningConnectorConfig : identityProvider
                .getProvisioningConnectorConfigs()) {
            Property[] properties = provisioningConnectorConfig.getProvisioningProperties();
            if (ArrayUtils.isEmpty(properties)) {
                continue;
            }
            // properties = RandomPasswordProcessor.getInstance().removeRandomPasswords(properties, withCacheClear);
            provisioningConnectorConfig.setProvisioningProperties(properties);
        }
    }
}
