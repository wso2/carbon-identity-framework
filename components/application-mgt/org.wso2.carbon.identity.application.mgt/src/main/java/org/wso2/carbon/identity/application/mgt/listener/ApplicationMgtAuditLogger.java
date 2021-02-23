/*
 *  Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */

package org.wso2.carbon.identity.application.mgt.listener;

import org.apache.commons.logging.Log;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.identity.application.common.IdentityApplicationManagementException;
import org.wso2.carbon.identity.application.common.model.ServiceProvider;
import org.wso2.carbon.user.core.util.UserCoreUtil;

/**
 * Audit log implementation for Application (Service Provider) changes.
 */
public class ApplicationMgtAuditLogger extends AbstractApplicationMgtListener {

    private static final Log audit = CarbonConstants.AUDIT_LOG;
    private static final String AUDIT_MESSAGE = "Initiator : %s | Action : %s | Target : %s | Data : { %s } | " +
            "Result : %s ";
    private static final String SUCCESS = "Success";

    @Override
    public int getDefaultOrderId() {
        return 200;
    }

    @Override
    public boolean doPostCreateApplication(ServiceProvider serviceProvider, String tenantDomain, String userName)
            throws IdentityApplicationManagementException {

        int appId = getAppId(serviceProvider);
        String name = getApplicationName(serviceProvider);
        // Append tenant domain to username.
        String initiator = buildInitiatorUsername(tenantDomain, userName);

        audit.info(String.format(AUDIT_MESSAGE, initiator, "create", appId, name, SUCCESS));
        return true;
    }



    @Override
    public boolean doPostUpdateApplication(ServiceProvider serviceProvider, String tenantDomain, String userName)
            throws IdentityApplicationManagementException {

        String name = getApplicationName(serviceProvider);
        int appId = getAppId(serviceProvider);
        String initiator = buildInitiatorUsername(tenantDomain, userName);

        audit.info(String.format(AUDIT_MESSAGE, initiator, "update", appId, name, SUCCESS));
        return true;
    }

    @Override
    public boolean doPostDeleteApplication(ServiceProvider serviceProvider, String tenantDomain, String userName)
            throws IdentityApplicationManagementException {

        String applicationName = getApplicationName(serviceProvider);
        int appId = getAppId(serviceProvider);
        String initiator = buildInitiatorUsername(tenantDomain, userName);

        audit.info(String.format(AUDIT_MESSAGE, initiator, "delete", appId, applicationName, SUCCESS));
        return true;
    }

    private int getAppId(ServiceProvider serviceProvider) {

        if (serviceProvider != null) {
            return serviceProvider.getApplicationID();
        }
        return -1;
    }

    private String getApplicationName(ServiceProvider serviceProvider) {

        if (serviceProvider != null) {
            return serviceProvider.getApplicationName();
        }
        return "Undefined";
    }

    private String buildInitiatorUsername(String tenantDomain, String userName) {

        // Append tenant domain to username build the full qualified username of initiator.
        return UserCoreUtil.addTenantDomainToEntry(userName, tenantDomain);
    }
}
