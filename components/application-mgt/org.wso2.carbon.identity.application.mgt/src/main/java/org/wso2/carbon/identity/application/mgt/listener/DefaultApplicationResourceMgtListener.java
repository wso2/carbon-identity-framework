/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wso2.carbon.identity.application.mgt.listener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.annotation.bundle.Capability;
import org.wso2.carbon.identity.application.common.IdentityApplicationManagementException;
import org.wso2.carbon.identity.application.common.model.ApplicationBasicInfo;
import org.wso2.carbon.identity.application.common.model.ServiceProvider;
import org.wso2.carbon.identity.application.mgt.ApplicationMgtSystemConfig;
import org.wso2.carbon.identity.application.mgt.dao.ApplicationDAO;
import org.wso2.carbon.identity.application.mgt.internal.ApplicationMgtListenerServiceComponent;

/**
 * Triggers {@link ApplicationMgtListener} listeners registered for application management operations.
 */
@Capability(
        namespace = "osgi.service",
        attribute = {
                "objectClass=org.wso2.carbon.identity.application.mgt.listener.ApplicationResourceManagementListener",
                "service.scope=singleton"
        }
)
public class DefaultApplicationResourceMgtListener implements ApplicationResourceManagementListener {

    private static final Log log = LogFactory.getLog(DefaultApplicationResourceMgtListener.class);

    @Override
    public int getDefaultOrderId() {

        return 10;
    }

    @Override
    public boolean doPreCreateApplication(ServiceProvider application,
                                          String tenantDomain,
                                          String userPerformingAction) throws IdentityApplicationManagementException {

        for (ApplicationMgtListener listener : ApplicationMgtListenerServiceComponent.getApplicationMgtListeners()) {
            if (listener.isEnable()
                    && !listener.doPreCreateApplication(application, tenantDomain, userPerformingAction)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean doPostCreateApplication(String resourceId,
                                           ServiceProvider application,
                                           String tenantDomain,
                                           String userPerformingAction) throws IdentityApplicationManagementException {

        int applicationId = getApplicationId(resourceId, tenantDomain);
        application.setApplicationID(applicationId);

        for (ApplicationMgtListener listener : ApplicationMgtListenerServiceComponent.getApplicationMgtListeners()) {
            if (listener.isEnable()
                    && !listener.doPostCreateApplication(application, tenantDomain, userPerformingAction)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean doPreUpdateApplicationByResourceId(ServiceProvider application,
                                                      String resourceId,
                                                      String tenantDomain,
                                                      String userPerformingAction)
            throws IdentityApplicationManagementException {

        int applicationId = getApplicationId(resourceId, tenantDomain);

        if (isApplicationExists(applicationId)) {
            application.setApplicationID(applicationId);
            for (ApplicationMgtListener listener :
                    ApplicationMgtListenerServiceComponent.getApplicationMgtListeners()) {
                if (listener.isEnable()
                        && !listener.doPreUpdateApplication(application, tenantDomain, userPerformingAction)) {
                    return false;
                }
            }
        }
        return true;
    }

    private boolean isApplicationExists(int applicationId) {

        return applicationId != -1;
    }

    @Override
    public boolean doPostUpdateApplicationByResourceId(ServiceProvider application,
                                                       String resourceId,
                                                       String tenantDomain,
                                                       String userPerformingAction)
            throws IdentityApplicationManagementException {

        int applicationId = getApplicationId(resourceId, tenantDomain);
        application.setApplicationID(applicationId);

        for (ApplicationMgtListener listener : ApplicationMgtListenerServiceComponent.getApplicationMgtListeners()) {
            if (listener.isEnable()
                    && !listener.doPostUpdateApplication(application, tenantDomain, userPerformingAction)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean doPreDeleteApplicationByResourceId(String resourceId,
                                                      String tenantDomain,
                                                      String userPerformingAction)
            throws IdentityApplicationManagementException {

        String applicationName = getApplicationName(resourceId, tenantDomain);
        if (isApplicationExists(applicationName)) {
            for (ApplicationMgtListener listener :
                    ApplicationMgtListenerServiceComponent.getApplicationMgtListeners()) {
                if (listener.isEnable()
                        && !listener.doPreDeleteApplication(applicationName, tenantDomain, userPerformingAction)) {
                    return false;
                }
            }
        } else {
            if (log.isDebugEnabled()) {
                log.debug("Application cannot be found for the resourceId: " + resourceId +
                        " in tenantDomain: " + tenantDomain + ". Therefore not triggering the " +
                        "doPreDeleteApplication() of ApplicationMgtListeners.");
            }
        }

        return true;
    }

    private boolean isApplicationExists(String applicationName) {

        return applicationName != null;
    }

    @Override
    public boolean doPostDeleteApplicationByResourceId(ServiceProvider deletedApplication,
                                                       String applicationResourceId,
                                                       String tenantDomain,
                                                       String userPerformingAction)
            throws IdentityApplicationManagementException {

        for (ApplicationMgtListener listener : ApplicationMgtListenerServiceComponent.getApplicationMgtListeners()) {
            if (listener.isEnable()
                    && !listener.doPostDeleteApplication(deletedApplication, tenantDomain, userPerformingAction)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean doPreGetApplicationByResourceId(String applicationResourceId, String tenantDomain)
            throws IdentityApplicationManagementException {

        String applicationName = getApplicationName(applicationResourceId, tenantDomain);
        if (isApplicationExists(applicationName)) {
            for (ApplicationMgtListener listener :
                    ApplicationMgtListenerServiceComponent.getApplicationMgtListeners()) {
                if (listener.isEnable()
                        && !listener.doPreGetServiceProvider(applicationName, tenantDomain)) {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public boolean doPostGetApplicationByResourceId(ServiceProvider serviceProvider, String resourceId,
                                                    String tenantDomain) throws IdentityApplicationManagementException {

        String applicationName = getApplicationName(resourceId, tenantDomain);
        for (ApplicationMgtListener listener : ApplicationMgtListenerServiceComponent.getApplicationMgtListeners()) {
            if (listener.isEnable()
                    && !listener.doPostGetServiceProvider(serviceProvider, applicationName, tenantDomain)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean doPreGetApplicationBasicInfoByResourceId(String resourceId, String tenantDomain)
            throws IdentityApplicationManagementException {

        return true;
    }

    @Override
    public boolean doPostGetApplicationBasicInfoByResourceId(ApplicationBasicInfo appInfo, String resourceId,
                                                             String tenantDomain)
            throws IdentityApplicationManagementException {

        return true;
    }

    private String getApplicationName(String resourceId,
                                      String tenantDomain) throws IdentityApplicationManagementException {

        ApplicationBasicInfo appInfo = getApplicationBasicInfoByResourceId(resourceId, tenantDomain);
        return appInfo != null ? appInfo.getApplicationName() : null;
    }

    private int getApplicationId(String applicationResourceId,
                                 String tenantDomain) throws IdentityApplicationManagementException {

        ApplicationBasicInfo appInfo = getApplicationBasicInfoByResourceId(applicationResourceId, tenantDomain);
        return appInfo != null ? appInfo.getApplicationId() : -1;
    }

    private ApplicationBasicInfo getApplicationBasicInfoByResourceId(String resourceId, String tenantDomain)
            throws IdentityApplicationManagementException {

        ApplicationDAO appDAO = ApplicationMgtSystemConfig.getInstance().getApplicationDAO();
        return appDAO.getApplicationBasicInfoByResourceId(resourceId, tenantDomain);
    }

}
