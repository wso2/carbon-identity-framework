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

package org.wso2.carbon.identity.application.authentication.framework.handler.provisioning;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.application.authentication.framework.internal.FrameworkServiceComponent;
import org.wso2.carbon.identity.application.authentication.framework.store.SessionCleanUpService;
import org.wso2.carbon.identity.application.authentication.framework.util.FrameworkConstants;
import org.wso2.carbon.identity.application.authentication.framework.util.FrameworkUtils;
import org.wso2.carbon.user.api.UserRealm;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.core.common.AbstractUserStoreManager;
import org.wso2.carbon.user.core.service.RealmService;

/**
 * Thread to perform JIT provisioned users deletion task based on provisioned IDP delete.
 */
public class ProvisionedUserDeleteThread implements Runnable {

    private static final Log log = LogFactory.getLog(SessionCleanUpService.class);
    private static final Log diagnosticLog = LogFactory.getLog("diagnostics");

    private final String resourceId;
    private final String tenantDomain;

    public ProvisionedUserDeleteThread(String resourceId, String tenantDomain) {

        this.resourceId = resourceId;
        this.tenantDomain = tenantDomain;
    }

    @Override
    public void run() {

        if (log.isDebugEnabled()) {
            log.debug("Start running the JIT provisioned user delete task.");
        }
        diagnosticLog.info("Start running the JIT provisioned user delete task.");
        provisionedUserDelete();
        if (log.isDebugEnabled()) {
            log.debug("Stop running the JIT provisioned user delete task.");
        }
        diagnosticLog.info("Stop running the JIT provisioned user delete task.");
    }

    private void provisionedUserDelete() {

        try {
            FrameworkUtils.startTenantFlow(tenantDomain);
            RealmService realmService = FrameworkServiceComponent.getRealmService();
            int tenantId = realmService.getTenantManager().getTenantId(tenantDomain);
            UserRealm userRealm = realmService.getTenantUserRealm(tenantId);
            String[] userList =
                    ((AbstractUserStoreManager) userRealm.getUserStoreManager()).
                            getUserList(FrameworkConstants.PROVISIONED_SOURCE_ID_CLAIM, resourceId, null);
            if (ArrayUtils.isNotEmpty(userList)) {
                for (String username : userList) {
                    ((AbstractUserStoreManager) userRealm.getUserStoreManager()).deleteUser(username);
                }
            }
        } catch (UserStoreException e) {
            log.error("Error occurred while deleting the provisioned users from IDP: " + resourceId, e);
        } finally {
            FrameworkUtils.endTenantFlow();
        }
    }
}
