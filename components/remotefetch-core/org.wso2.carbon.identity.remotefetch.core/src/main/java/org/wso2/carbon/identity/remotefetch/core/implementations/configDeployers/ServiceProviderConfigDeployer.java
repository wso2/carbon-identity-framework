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

package org.wso2.carbon.identity.remotefetch.core.implementations.configDeployers;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.identity.application.common.IdentityApplicationManagementException;
import org.wso2.carbon.identity.application.common.model.ImportResponse;
import org.wso2.carbon.identity.application.common.model.ServiceProvider;
import org.wso2.carbon.identity.application.mgt.ApplicationManagementService;
import org.wso2.carbon.identity.application.mgt.ApplicationMgtUtil;
import org.wso2.carbon.identity.remotefetch.common.ConfigFileContent;
import org.wso2.carbon.identity.remotefetch.common.configdeployer.ConfigDeployer;
import org.wso2.carbon.identity.remotefetch.common.exceptions.RemoteFetchCoreException;
import org.wso2.carbon.identity.remotefetch.core.internal.RemoteFetchServiceComponentHolder;
import org.wso2.carbon.identity.application.common.model.SpFileContent;
import org.wso2.carbon.user.api.UserStoreException;

public class ServiceProviderConfigDeployer implements ConfigDeployer {

    private static final Log log = LogFactory.getLog(ServiceProviderConfigDeployer.class);

    private ApplicationManagementService applicationManagementService;
    private int tenantId;
    private String userName;

    public ServiceProviderConfigDeployer(int tenantId, String userName) {

        this.applicationManagementService = RemoteFetchServiceComponentHolder.getInstance()
                .getApplicationManagementService();
        this.tenantId = tenantId;
        this.userName = userName;
    }

    /**
     * Deploy the configuration
     *
     * @param configFileContent
     * @throws RemoteFetchCoreException
     */
    @Override
    public void deploy(ConfigFileContent configFileContent) throws RemoteFetchCoreException {

        String resolvedName = this.resolveConfigName(configFileContent);
        String tenantDomain = this.getTenantDomain();
        SpFileContent spFileContent = new SpFileContent();
        spFileContent.setContent(configFileContent.getContent());

        ServiceProvider serviceProvider;
        startTenantFlow(tenantDomain, this.userName);
        try {
            serviceProvider = this.applicationManagementService
                    .getApplicationExcludingFileBasedSPs(resolvedName, tenantDomain);
        } catch (IdentityApplicationManagementException e) {
            throw new RemoteFetchCoreException("Unable to check if Application already exists", e);
        }

        try {
            ImportResponse importResponse = this.applicationManagementService.importSPApplication(spFileContent,
                    tenantDomain, this.userName, serviceProvider != null);

            if (importResponse.getResponseCode() == ImportResponse.FAILED) {
                log.warn("Unable to deploy Service Provider " + resolvedName);
                for (String error : importResponse.getErrors()) {
                    log.warn("Reason " + error);
                }
                throw new RemoteFetchCoreException("Unable to deploy Service Provider");
            }
        } catch (IdentityApplicationManagementException e) {
            throw new RemoteFetchCoreException("Unable to deploy Service Provider", e);
        }
        endTenantFlow();

    }

    /**
     * resolve the unique identifier for the configuration
     *
     * @param configFileContent
     * @return
     * @throws RemoteFetchCoreException
     */
    @Override
    public String resolveConfigName(ConfigFileContent configFileContent) throws RemoteFetchCoreException {

        SpFileContent spFileContent = new SpFileContent();
        spFileContent.setContent(configFileContent.getContent());

        String tenantDomain = this.getTenantDomain();

        try {
            return ApplicationMgtUtil.getApplicationNameFromSpFileContent(spFileContent, tenantDomain);
        } catch (IdentityApplicationManagementException e) {
            throw new RemoteFetchCoreException("Unable to get name for specified config file", e);
        }

    }

    private String getTenantDomain() throws RemoteFetchCoreException {

        try {
            return RemoteFetchServiceComponentHolder.getInstance().getRealmService().getTenantManager()
                    .getDomain(this.tenantId);
        } catch (UserStoreException e) {
            throw new RemoteFetchCoreException("Unable to get tenant domain for specified tenant", e);
        }
    }

    private void startTenantFlow(String tenantDomain, String userName) {

        PrivilegedCarbonContext.startTenantFlow();
        PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(tenantDomain);
        PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantId(tenantId);
        PrivilegedCarbonContext.getThreadLocalCarbonContext().setUsername(userName);
    }

    private void endTenantFlow() {

        PrivilegedCarbonContext.endTenantFlow();
    }
}
