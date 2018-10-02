/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.identity.consent.mgt.listener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.consent.mgt.core.exception.ConsentManagementException;
import org.wso2.carbon.consent.mgt.core.model.PurposeCategory;
import org.wso2.carbon.identity.application.authentication.framework.util.FrameworkUtils;
import org.wso2.carbon.identity.consent.mgt.internal.IdentityConsentDataHolder;
import org.wso2.carbon.identity.core.AbstractIdentityTenantMgtListener;
import org.wso2.carbon.stratos.common.beans.TenantInfoBean;
import org.wso2.carbon.stratos.common.exception.StratosException;

/**
 * Tenant listener which adds default purpose category for a tenant
 */
public class TenantConsentMgtListener extends AbstractIdentityTenantMgtListener {

    private static final Log log = LogFactory.getLog(TenantConsentMgtListener.class);
    private static final String DEFAULT_PURPOSE_CATEGORY = "DEFAULT";

    @Override
    public void onTenantCreate(TenantInfoBean tenantInfoBean) throws StratosException {

        if (isSsoConsentManagementEnabled()) {
            addDefaultPurposeCategory(tenantInfoBean);
        }
    }

    protected void addDefaultPurposeCategory(TenantInfoBean tenantInfoBean) throws StratosException {

        FrameworkUtils.startTenantFlow(tenantInfoBean.getTenantDomain());
        try {
            PurposeCategory purposeCategory;
            PurposeCategory defaultPurposeCategory = new PurposeCategory(DEFAULT_PURPOSE_CATEGORY, "Core " +
                    "functionality");
            try {
                purposeCategory = IdentityConsentDataHolder.getInstance().getConsentManager().addPurposeCategory
                        (defaultPurposeCategory);
                if (log.isDebugEnabled()) {
                    log.debug(String.format("Added default purpose category for tenant: %s. Default purpose category " +
                            "id: %d", tenantInfoBean.getTenantDomain(), purposeCategory.getId()));
                }
            } catch (ConsentManagementException e) {
                throw new StratosException("Error while adding default purpose category for tenant:" + tenantInfoBean
                        .getTenantDomain(), e);
            }
        } finally {
            FrameworkUtils.endTenantFlow();
        }
    }

    protected boolean isSsoConsentManagementEnabled() {

        return IdentityConsentDataHolder.getInstance().getSSOConsentService().
                isSSOConsentManagementEnabled(null);
    }

}
