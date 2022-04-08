/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.identity.mgt.listener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.identity.core.model.IdentityEventListenerConfig;
import org.wso2.carbon.identity.core.util.IdentityUtil;
import org.wso2.carbon.identity.mgt.IdentityMgtConfigException;
import org.wso2.carbon.identity.mgt.IdentityMgtEventListener;
import org.wso2.carbon.identity.mgt.config.Config;
import org.wso2.carbon.identity.mgt.config.ConfigBuilder;
import org.wso2.carbon.identity.mgt.config.EmailNotificationConfig;
import org.wso2.carbon.identity.mgt.config.StorageType;
import org.wso2.carbon.identity.mgt.util.UserIdentityManagementUtil;
import org.wso2.carbon.stratos.common.beans.TenantInfoBean;
import org.wso2.carbon.stratos.common.exception.StratosException;
import org.wso2.carbon.stratos.common.listeners.TenantMgtListener;
import org.wso2.carbon.user.core.listener.UserOperationEventListener;

/**
 * @deprecated use org.wso2.carbon.identity.recovery.listener.TenantManagementListener instead.
 */
@Deprecated
public class TenantManagementListener implements TenantMgtListener {
    private static final int EXEC_ORDER = 40;
    private static final Log log = LogFactory.getLog(TenantManagementListener.class);

    /**
     * Add the default challenge Question Set when a new tenant is registered.
     *
     * @param tenantInfo Information about the newly created tenant
     */
    @Override
    public void onTenantCreate(TenantInfoBean tenantInfo) throws StratosException {
        try {
            PrivilegedCarbonContext.getThreadLocalCarbonContext().startTenantFlow();
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(tenantInfo.getTenantDomain());
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantId(tenantInfo.getTenantId());
            UserIdentityManagementUtil.loadDefaultChallenges();

            IdentityEventListenerConfig identityEventListenerConfig = IdentityUtil.readEventListenerProperty
                    (UserOperationEventListener.class.getName(), IdentityMgtEventListener.class.getName());

            if (identityEventListenerConfig != null) {
                if (Boolean.parseBoolean(identityEventListenerConfig.getEnable())) {
                    Config emailConfigFile = ConfigBuilder.getInstance().loadEmailConfigFile();
                    EmailNotificationConfig emailNotificationConfig = new EmailNotificationConfig();
                    emailNotificationConfig.setProperties(emailConfigFile.getProperties());
                    ConfigBuilder.getInstance().saveConfiguration(StorageType.REGISTRY, tenantInfo.getTenantId(),
                            emailNotificationConfig);
                }
            }
        } catch (IdentityMgtConfigException e) {
            log.error("Error occurred while saving default email templates in registry for tenant: "
                      + tenantInfo.getTenantDomain());
        } finally {
            PrivilegedCarbonContext.getThreadLocalCarbonContext().endTenantFlow();
        }
    }

    @Override
    public void onTenantUpdate(TenantInfoBean tenantInfoBean) throws StratosException {

    }

    @Override
    public void onTenantDelete(int i) {

    }

    @Override
    public void onTenantRename(int i, String s, String s1) throws StratosException {

    }

    @Override
    public void onTenantInitialActivation(int i) throws StratosException {

    }

    @Override
    public void onTenantActivation(int i) throws StratosException {

    }

    @Override
    public void onTenantDeactivation(int i) throws StratosException {

    }

    @Override
    public void onSubscriptionPlanChange(int i, String s, String s1) throws StratosException {

    }

    @Override
    public int getListenerOrder() {
        return EXEC_ORDER;
    }

    @Override
    public void onPreDelete(int i) throws StratosException {

    }
}
