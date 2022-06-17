/*
 * Copyright (c) 2022, WSO2 Inc. (http://www.wso2.com).
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

package org.wso2.carbon.identity.application.mgt.listener;

import org.wso2.carbon.identity.application.common.IdentityApplicationManagementException;
import org.wso2.carbon.identity.application.common.model.ServiceProvider;
import org.wso2.carbon.identity.application.mgt.ApplicationMgtSystemConfig;
import org.wso2.carbon.identity.application.mgt.dao.ApplicationDAO;

import java.util.Arrays;

import static org.wso2.carbon.identity.application.mgt.ApplicationConstants.IS_SHARED_APP;

/**
 * Application listener to restrict actions on shared applications.
 */
public class SharedApplicationMgtListener extends AbstractApplicationMgtListener {

    @Override
    public int getDefaultOrderId() {

        return 250;
    }

    @Override
    public boolean doPreUpdateApplication(ServiceProvider serviceProvider, String applicationName,
                                          String tenantDomain) throws IdentityApplicationManagementException {

        // If the application is a shared application, only certain updates to the application are allowed,
        // if any other data has been change, listener will reject the update request.
        ServiceProvider existingApplication = getApplication(serviceProvider.getApplicationID());
        if (existingApplication != null && Arrays.stream(existingApplication.getSpProperties())
                .anyMatch(p -> IS_SHARED_APP.equalsIgnoreCase(p.getName()) && Boolean.parseBoolean(p.getValue()))) {
            if (!validateUpdatedData(existingApplication, serviceProvider)) {
                return false;
            }
        }

        return super.doPostUpdateApplication(serviceProvider, applicationName, tenantDomain);
    }

    @Override
    public boolean doPreDeleteApplication(String applicationName, String tenantDomain, String userName)
            throws IdentityApplicationManagementException {

        // If the application is a shared application, application cannot be deleted
        // TODO: verify this.
        ServiceProvider existingApplication = getApplication(applicationName, tenantDomain);
        if (existingApplication != null && Arrays.stream(existingApplication.getSpProperties())
                .anyMatch(p -> IS_SHARED_APP.equalsIgnoreCase(p.getName()) && Boolean.parseBoolean(p.getValue()))) {
            return false;
        }

        return super.doPreDeleteApplication(applicationName, tenantDomain, userName);
    }

    private boolean validateUpdatedData(ServiceProvider existingApplication, ServiceProvider serviceProvider) {

        return existingApplication.getInboundAuthenticationConfig() ==
                serviceProvider.getInboundAuthenticationConfig() ||
                existingApplication.getPermissionAndRoleConfig() == serviceProvider.getPermissionAndRoleConfig() ||
                existingApplication.getSpProperties() == serviceProvider.getSpProperties();
    }

    private ServiceProvider getApplication(int applicationId) throws IdentityApplicationManagementException {

        ApplicationDAO appDAO = ApplicationMgtSystemConfig.getInstance().getApplicationDAO();
        return appDAO.getApplication(applicationId);
    }

    private ServiceProvider getApplication(String name, String tenantDomain)
            throws IdentityApplicationManagementException {

        ApplicationDAO appDAO = ApplicationMgtSystemConfig.getInstance().getApplicationDAO();
        return appDAO.getApplication(name, tenantDomain);
    }
}
