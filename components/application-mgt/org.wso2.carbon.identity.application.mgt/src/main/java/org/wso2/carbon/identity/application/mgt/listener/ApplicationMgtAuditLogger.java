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

public class ApplicationMgtAuditLogger extends AbstractApplicationMgtListener {


    private static final Log audit = CarbonConstants.AUDIT_LOG;
    private static String AUDIT_MESSAGE = "Initiator : %s | Action : %s | Target : %s | Data : { %s } | Result : %s ";
    private static String SUCCESS = "Success";


    @Override
    public int getDefaultOrderId() {
        return 200;
    }

    @Override
    public boolean doPostCreateApplication(ServiceProvider serviceProvider, String tenantDomain, String userName) throws IdentityApplicationManagementException {
        int appId = -1;
        String name = "Undefined";
        if (serviceProvider != null) {
            appId = serviceProvider.getApplicationID();
            name = serviceProvider.getApplicationName();
        }
        audit.info(String.format(AUDIT_MESSAGE, userName, "create", appId,
                name, SUCCESS));
        return true;
    }

    @Override
    public boolean doPostUpdateApplication(ServiceProvider serviceProvider, String tenantDomain, String userName) throws IdentityApplicationManagementException {
        int appId = -1;
        String name = "Undefined";
        if (serviceProvider != null) {
            appId = serviceProvider.getApplicationID();
            name = serviceProvider.getApplicationName();
        }
        audit.info(String.format(AUDIT_MESSAGE, userName, "update", appId,
                name, SUCCESS));
        return true;
    }

    @Override
    public boolean doPostDeleteApplication(String applicationName, String tenantDomain, String userName) throws IdentityApplicationManagementException {
        audit.info(String.format(AUDIT_MESSAGE, userName, "update", applicationName, null, SUCCESS));
        return true;
    }
}
