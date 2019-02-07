/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.identity.application.mgt.listener;

import org.apache.commons.lang.StringUtils;
import org.wso2.carbon.identity.application.common.IdentityApplicationManagementException;
import org.wso2.carbon.identity.application.common.model.ServiceProvider;
import org.wso2.carbon.identity.application.mgt.dao.ApplicationDAO;
import org.wso2.carbon.identity.core.model.IdentityEventListenerConfig;
import org.wso2.carbon.identity.core.util.IdentityCoreConstants;
import org.wso2.carbon.identity.core.util.IdentityUtil;

public abstract class AbstractApplicationMgtListener implements ApplicationMgtListener {

    public boolean doPreCreateApplication(ServiceProvider serviceProvider, String tenantDomain, String userName) throws IdentityApplicationManagementException {
        return true;
    }

    public boolean doPostCreateApplication(ServiceProvider serviceProvider, String tenantDomain, String userName) throws IdentityApplicationManagementException {
        return true;
    }

    public boolean doPreUpdateApplication(ServiceProvider serviceProvider, String tenantDomain, String userName) throws IdentityApplicationManagementException {
        return true;
    }

    public boolean doPostUpdateApplication(ServiceProvider serviceProvider, String tenantDomain, String userName) throws IdentityApplicationManagementException {
        return true;
    }

    public boolean doPreDeleteApplication(String applicationName, String tenantDomain, String userName) throws IdentityApplicationManagementException {
        return true;
    }

    public boolean doPostDeleteApplication(String applicationName, String tenantDomain, String userName) throws IdentityApplicationManagementException {
        return true;
    }

    public boolean doPreGetServiceProvider(String applicationName, String tenantDomain)
            throws IdentityApplicationManagementException{
        return true;
    }

    public boolean doPostGetServiceProvider(ServiceProvider serviceProvider, String applicationName, String tenantDomain) throws IdentityApplicationManagementException{
        return true;
    }

    public boolean doPreGetServiceProviderByClientId(String clientId, String clientType,
                                                     String tenantDomain)throws IdentityApplicationManagementException{
        return true;
    }

    public boolean doPostGetServiceProviderByClientId(ServiceProvider serviceProvider, String clientId, String clientType,
                                                      String tenantDomain)throws IdentityApplicationManagementException{
        return true;
    }

    @Override
    public boolean doPreGetAllApplicationBasicInfo(String tenantDomain, String username) throws IdentityApplicationManagementException {
        return true;
    }

    @Override
    public boolean doPostGetAllApplicationBasicInfo(ApplicationDAO appDAO, String tenantDomain, String username) throws IdentityApplicationManagementException {
        return true;
    }

    @Override
    public boolean doPreGetApplicationExcludingFileBasedSPs(String applicationName, String tenantDomain) throws IdentityApplicationManagementException {
        return true;
    }

    @Override
    public boolean doPostGetApplicationExcludingFileBasedSPs(ServiceProvider serviceProvider, String applicationName, String tenantDomain) throws IdentityApplicationManagementException {
        return true;
    }

    @Override
    public boolean doPreGetApplicationBasicInfo(String tenantDomain, String username, String filter)
            throws IdentityApplicationManagementException {

        return true;
    }

    @Override
    public boolean doPostGetApplicationBasicInfo(ApplicationDAO appDAO, String tenantDomain, String username,
                                                 String filter) throws IdentityApplicationManagementException {

        return true;
    }

    @Override
    public boolean doPreGetServiceProviderNameByClientId(String clientId, String clientType, String tenantDomain) throws IdentityApplicationManagementException {
        return true;
    }

    @Override
    public boolean doPostGetServiceProviderNameByClientId(String name, String clientId, String clientType, String tenantDomain) throws IdentityApplicationManagementException {
        return true;
    }

    @Override
    public boolean doPreGetServiceProviderNameByClientIdExcludingFileBasedSPs(String name, String clientId, String type, String tenantDomain) throws IdentityApplicationManagementException {
        return true;
    }

    @Override
    public boolean doPostGetServiceProviderNameByClientIdExcludingFileBasedSPs(String name, String clientId, String type, String tenantDomain) {
        return true;
    }

    @Override
    public void doImportServiceProvider(ServiceProvider serviceProvider) throws IdentityApplicationManagementException {

        return;
    }

    @Override
    public void doExportServiceProvider(ServiceProvider serviceProvider, Boolean exportSecrets)
            throws IdentityApplicationManagementException {

        return;
    }

    @Override
    public void onPreCreateInbound(ServiceProvider serviceProvider, boolean isUpdate) throws
            IdentityApplicationManagementException {

        return;
    }

    @Override
    public boolean doPreCreateApplicationTemplate(ServiceProvider serviceProvider, String tenantDomain)
            throws IdentityApplicationManagementException {

        return true;
    }

    @Override
    public boolean doPreUpdateApplicationTemplate(ServiceProvider serviceProvider, String tenantDomain)
            throws IdentityApplicationManagementException {

        return true;
    }

    public boolean isEnable() {
        IdentityEventListenerConfig identityEventListenerConfig = IdentityUtil.readEventListenerProperty
                (ApplicationMgtListener.class.getName(), this.getClass().getName());
        if (identityEventListenerConfig == null) {
            return true;
        }
        if (StringUtils.isNotBlank(identityEventListenerConfig.getEnable())) {
            return Boolean.parseBoolean(identityEventListenerConfig.getEnable());
        } else {
            return true;
        }
    }

    public int getExecutionOrderId() {
        IdentityEventListenerConfig identityEventListenerConfig = IdentityUtil.readEventListenerProperty
                (ApplicationMgtListener.class.getName(), this.getClass().getName());
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
}
