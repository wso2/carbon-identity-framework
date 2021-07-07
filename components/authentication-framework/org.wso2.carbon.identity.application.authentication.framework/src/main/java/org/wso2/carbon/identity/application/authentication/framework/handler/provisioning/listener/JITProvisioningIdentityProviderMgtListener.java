/*
 * Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.identity.application.authentication.framework.handler.provisioning.listener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.application.authentication.framework.handler.provisioning.ProvisionedUserDeleteThread;
import org.wso2.carbon.identity.application.authentication.framework.util.FrameworkConstants;
import org.wso2.carbon.identity.application.authentication.framework.util.FrameworkUtils;
import org.wso2.carbon.identity.application.common.model.IdentityProvider;
import org.wso2.carbon.identity.core.util.IdentityUtil;
import org.wso2.carbon.idp.mgt.IdentityProviderManagementException;
import org.wso2.carbon.idp.mgt.IdentityProviderManager;
import org.wso2.carbon.idp.mgt.listener.AbstractIdentityProviderMgtListener;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Listener to perform JIT provisioning related Identity Provider management operations.
 */
public class JITProvisioningIdentityProviderMgtListener extends AbstractIdentityProviderMgtListener {

    private static final Log log = LogFactory.getLog(JITProvisioningIdentityProviderMgtListener.class);
    private static ExecutorService threadPool = Executors.newFixedThreadPool(2);

    @Override
    public boolean doPostDeleteIdPByResourceId(String resourceId, IdentityProvider identityProvider,
                                               String tenantDomain) throws IdentityProviderManagementException {

        if (log.isDebugEnabled()) {
            log.debug("doPostDeleteIdPByResourceId executed for idp: " + resourceId + " of tenant domain: " +
                    tenantDomain);
        }

        if (!FrameworkUtils.isEnhancedFeature()) {
            // JIT provisioning enhanced feature is not enabled. Hence returning.
            return true;
        }

        ProvisionedUserDeleteThread provisionedUserDeleteThread =
                new ProvisionedUserDeleteThread(resourceId, tenantDomain);
        threadPool.submit(provisionedUserDeleteThread);
        return true;
    }

    @Override
    public boolean doPreDeleteIdP(String idPName, String tenantDomain) throws IdentityProviderManagementException {

        if (!FrameworkUtils.isEnhancedFeature()) {
            // JIT provisioning enhanced feature is not enabled. Hence returning.
            return true;
        }

        String idpId = IdentityProviderManager.getInstance().getIdPByName(idPName, tenantDomain,
                true).getResourceId();
        IdentityUtil.threadLocalProperties.get().put(FrameworkConstants.IDP_RESOURCE_ID, idpId);
        return true;
    }

    @Override
    public boolean doPostDeleteIdP(String idPName, String tenantDomain) throws IdentityProviderManagementException {

        if (log.isDebugEnabled()) {
            log.debug("doPostDeleteIdp executed for idp: " + idPName + " of tenant domain: " + tenantDomain);
        }

        if (!FrameworkUtils.isEnhancedFeature()) {
            // JIT provisioning enhanced feature is not enabled. Hence returning.
            return true;
        }

        try {
            String idpId = (String) IdentityUtil.threadLocalProperties.get().get(FrameworkConstants.IDP_RESOURCE_ID);
            ProvisionedUserDeleteThread provisionedUserDeleteThread =
                    new ProvisionedUserDeleteThread(idpId, tenantDomain);
            threadPool.submit(provisionedUserDeleteThread);
        } finally {
            IdentityUtil.threadLocalProperties.get().remove(FrameworkConstants.IDP_RESOURCE_ID);
        }
        return true;
    }

    @Override
    public int getDefaultOrderId() {

        return 36;
    }
}
