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

import org.apache.commons.lang.StringUtils;
import org.wso2.carbon.identity.application.common.IdentityApplicationManagementException;
import org.wso2.carbon.identity.application.common.model.ApplicationBasicInfo;
import org.wso2.carbon.identity.application.common.model.ServiceProvider;
import org.wso2.carbon.identity.core.model.IdentityEventListenerConfig;
import org.wso2.carbon.identity.core.util.IdentityCoreConstants;
import org.wso2.carbon.identity.core.util.IdentityUtil;

/**
 * Allows executing additional actions during application management operations using the resourceId.
 */
public interface ApplicationResourceManagementListener {

    /**
     * Get the execution order identifier for this listener.
     *
     * @return The execution order identifier integer value.
     */
    default int getExecutionOrderId() {

        IdentityEventListenerConfig identityEventListenerConfig = IdentityUtil.readEventListenerProperty
                (ApplicationResourceManagementListener.class.getName(), this.getClass().getName());
        int orderId;
        if (identityEventListenerConfig == null) {
            orderId = IdentityCoreConstants.EVENT_LISTENER_ORDER_ID;
        } else {
            orderId = identityEventListenerConfig.getOrder();
        }

        if (orderId != IdentityCoreConstants.EVENT_LISTENER_ORDER_ID) {
            return orderId;
        }

        return getDefaultOrderId();
    }

    /**
     * Get the default order identifier for this listener.
     *
     * @return default order id
     */
    int getDefaultOrderId();

    /**
     * Check whether the listener is enabled or not
     *
     * @return true if enabled
     */
    default boolean isEnable() {

        IdentityEventListenerConfig identityEventListenerConfig = IdentityUtil.readEventListenerProperty
                (ApplicationResourceManagementListener.class.getName(), this.getClass().getName());
        if (identityEventListenerConfig == null) {
            return true;
        }
        if (StringUtils.isNotBlank(identityEventListenerConfig.getEnable())) {
            return Boolean.parseBoolean(identityEventListenerConfig.getEnable());
        } else {
            return true;
        }
    }

    /**
     * Define any additional actions before creating an application.
     *
     * @param serviceProvider      Created Service Provider
     * @param tenantDomain         Tenant domain of the user
     * @param userPerformingAction Tenant aware username of the user performing the action
     * @return Whether execution of this operation is allowed to continue.
     * @throws IdentityApplicationManagementException
     */
    boolean doPreCreateApplication(ServiceProvider serviceProvider,
                                   String tenantDomain,
                                   String userPerformingAction) throws IdentityApplicationManagementException;

    /**
     * Define any additional actions after creating an application.
     *
     * @param serviceProvider      Created Service Provider
     * @param tenantDomain         Tenant domain of the user
     * @param userPerformingAction Tenant aware username of the user performing the action
     * @return Whether execution of this operation is allowed to continue.
     * @throws IdentityApplicationManagementException
     */
    boolean doPostCreateApplication(ServiceProvider serviceProvider,
                                    String tenantDomain,
                                    String userPerformingAction) throws IdentityApplicationManagementException;

    /**
     * Define any additional actions before updating an application.
     *
     * @param serviceProvider      Created Service Provider
     * @param resourceId           Unique resource identifier of the application
     * @param tenantDomain         Tenant domain of the user
     * @param userPerformingAction Tenant aware username of the user performing the action
     * @return Whether execution of this operation is allowed to continue.
     * @throws IdentityApplicationManagementException
     */
    boolean doPreUpdateApplicationByResourceId(ServiceProvider serviceProvider,
                                               String resourceId,
                                               String tenantDomain,
                                               String userPerformingAction) throws IdentityApplicationManagementException;

    /**
     * Define any additional actions after updating an application.
     *
     * @param serviceProvider      Created Service Provider
     * @param resourceId           Unique resource identifier of the application
     * @param tenantDomain         Tenant domain of the user
     * @param userPerformingAction Tenant aware username of the user performing the action
     * @return Whether execution of this operation is allowed to continue.
     * @throws IdentityApplicationManagementException
     */
    boolean doPostUpdateApplicationByResourceId(ServiceProvider serviceProvider,
                                                String resourceId,
                                                String tenantDomain,
                                                String userPerformingAction) throws IdentityApplicationManagementException;

    /**
     * Define any additional actions before deleting an application.
     *
     * @param resourceId           Unique resource identifier of the application
     * @param tenantDomain         Tenant domain of the user
     * @param userPerformingAction Tenant aware username of the user performing the action
     * @return Whether execution of this operation is allowed to continue.
     * @throws IdentityApplicationManagementException
     */
    boolean doPreDeleteApplicationByResourceId(String resourceId,
                                               String tenantDomain,
                                               String userPerformingAction) throws IdentityApplicationManagementException;

    /**
     * Define any additional actions after deleting an application.
     *
     * @param deletedApplication   Deleted application
     * @param resourceId           Unique resource identifier of the application
     * @param tenantDomain         Tenant domain of the user
     * @param userPerformingAction Tenant aware username of the user performing the action
     * @return Whether execution of this operation is allowed to continue.
     * @throws IdentityApplicationManagementException
     */
    boolean doPostDeleteApplicationByResourceId(ServiceProvider deletedApplication,
                                                String resourceId,
                                                String tenantDomain,
                                                String userPerformingAction) throws IdentityApplicationManagementException;

    /**
     * Define any additional actions before retrieving an application by resourceId.
     *
     * @param resourceId           Unique resource identifier of the application
     * @param tenantDomain         Tenant domain of the user
     * @param userPerformingAction Tenant aware username of the user performing the action
     * @return Whether execution of this operation is allowed to continue.
     * @throws IdentityApplicationManagementException
     */
    boolean doPreGetApplicationByResourceId(String resourceId,
                                            String tenantDomain,
                                            String userPerformingAction) throws IdentityApplicationManagementException;

    /**
     * Define any additional actions before retrieving an application by resourceId.
     *
     * @param serviceProvider      Retrieved Service Provider
     * @param resourceId           Unique resource identifier of the application
     * @param tenantDomain         Tenant domain of the user
     * @param userPerformingAction Tenant aware username of the user performing the action
     * @return Whether execution of this operation is allowed to continue.
     * @throws IdentityApplicationManagementException
     */
    boolean doPostGetApplicationByResourceId(ServiceProvider serviceProvider,
                                             String resourceId,
                                             String tenantDomain,
                                             String userPerformingAction) throws IdentityApplicationManagementException;

    /**
     * Define any additional actions before retrieving basic information an application by resourceId.
     *
     * @param resourceId           Unique resource identifier of the application
     * @param tenantDomain         Tenant domain of the user
     * @param userPerformingAction Tenant aware username of the user performing the action
     * @return Whether execution of this operation is allowed to continue.
     * @throws IdentityApplicationManagementException
     */
    boolean doPreGetApplicationBasicInfoByResourceId(String resourceId,
                                                     String tenantDomain,
                                                     String userPerformingAction) throws IdentityApplicationManagementException;

    /**
     * Define any additional actions after retrieving basic information an application by resourceId.
     *
     * @param appInfo              Basic Information of the application
     * @param resourceId           Unique resource identifier of the application
     * @param tenantDomain         Tenant domain of the user
     * @param userPerformingAction Tenant aware username of the user performing the action
     * @return Whether execution of this operation is allowed to continue.
     * @throws IdentityApplicationManagementException
     */
    boolean doPostGetApplicationBasicInfoByResourceId(ApplicationBasicInfo appInfo,
                                                      String resourceId,
                                                      String tenantDomain,
                                                      String userPerformingAction) throws IdentityApplicationManagementException;

}
