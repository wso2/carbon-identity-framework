/*
 *  Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.identity.core;

import org.apache.commons.lang.StringUtils;
import org.wso2.carbon.identity.core.model.IdentityEventListenerConfig;
import org.wso2.carbon.identity.core.util.IdentityCoreConstants;
import org.wso2.carbon.identity.core.util.IdentityUtil;
import org.wso2.carbon.stratos.common.beans.TenantInfoBean;
import org.wso2.carbon.stratos.common.exception.StratosException;
import org.wso2.carbon.stratos.common.listeners.TenantMgtListener;

public class AbstractIdentityTenantMgtListener implements TenantMgtListener {

    public boolean isEnable() {
        IdentityEventListenerConfig identityEventListenerConfig = IdentityUtil.readEventListenerProperty
                (TenantMgtListener.class.getName(), this.getClass().getName());

        if (identityEventListenerConfig == null) {
            return true;
        }

        if (StringUtils.isNotBlank(identityEventListenerConfig.getEnable())) {
            return Boolean.parseBoolean(identityEventListenerConfig.getEnable());
        } else {
            return true;
        }
    }

    public int getOrderId() {
        IdentityEventListenerConfig identityEventListenerConfig = IdentityUtil.readEventListenerProperty
                (TenantMgtListener.class.getName(), this.getClass().getName());
        if (identityEventListenerConfig == null) {
            return IdentityCoreConstants.EVENT_LISTENER_ORDER_ID;
        }
        return identityEventListenerConfig.getOrder();
    }

    @Override
    public void onTenantCreate(TenantInfoBean tenantInfoBean) throws StratosException {
        /* Method not implemented */
    }

    @Override
    public void onTenantUpdate(TenantInfoBean tenantInfoBean) throws StratosException {
        /* Method not implemented */
    }

    @Override
    public void onTenantDelete(int i) {
        /* Method not implemented */
    }

    @Override
    public void onTenantRename(int i, String s, String s2) throws StratosException {
        /* Method not implemented */
    }

    @Override
    public void onTenantInitialActivation(int i) throws StratosException {
        /* Method not implemented */
    }

    @Override
    public void onTenantActivation(int i) throws StratosException {
        /* Method not implemented */
    }

    @Override
    public void onTenantDeactivation(int i) throws StratosException {
        /* Method not implemented */
    }

    @Override
    public void onSubscriptionPlanChange(int i, String s, String s2) throws StratosException {
        /* Method not implemented */
    }

    @Override
    public int getListenerOrder() {
        return 0;
    }

    @Override
    public void onPreDelete(int i) throws StratosException {
        /* Method not implemented */
    }
}
