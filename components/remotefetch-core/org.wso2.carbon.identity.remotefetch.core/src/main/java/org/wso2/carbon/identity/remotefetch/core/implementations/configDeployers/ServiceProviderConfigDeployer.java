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

import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.identity.application.common.IdentityApplicationManagementException;
import org.wso2.carbon.identity.application.common.model.ImportResponse;
import org.wso2.carbon.identity.application.common.model.ServiceProvider;
import org.wso2.carbon.identity.application.mgt.ApplicationManagementService;
import org.wso2.carbon.identity.application.mgt.ApplicationMgtUtil;
import org.wso2.carbon.identity.remotefetch.common.ConfigurationFileStream;
import org.wso2.carbon.identity.remotefetch.common.configdeployer.ConfigDeployer;
import org.wso2.carbon.identity.remotefetch.common.exceptions.RemoteFetchCoreException;
import org.wso2.carbon.identity.remotefetch.core.internal.RemoteFetchServiceComponentHolder;
import org.wso2.carbon.identity.application.common.model.SpFileStream;
import org.wso2.carbon.user.api.UserStoreException;

/**
 * Deploy new or update ServiceProviders using ApplicationManagementService.
 */
public class ServiceProviderConfigDeployer implements ConfigDeployer {

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
     * @param configurationFileStream
     * @throws RemoteFetchCoreException
     */
    @Override
    public void deploy(ConfigurationFileStream configurationFileStream) throws RemoteFetchCoreException {

        ServiceProvider previousServiceProvider = null;

        String tenantDomain = this.getTenantDomain();
        startTenantFlow(tenantDomain, this.userName);

        ServiceProvider serviceProvider = this.getServiceProviderFromStream(configurationFileStream);
        try {
            previousServiceProvider = this.applicationManagementService
                    .getApplicationExcludingFileBasedSPs(serviceProvider.getApplicationName(), tenantDomain);
        } catch (IdentityApplicationManagementException e) {
            throw new RemoteFetchCoreException("Unable to check if Application already exists", e);
        }

        try {
            ImportResponse importResponse = this.applicationManagementService.importSPApplication(serviceProvider,
                    tenantDomain, this.userName, previousServiceProvider != null);

            if (importResponse.getResponseCode() == ImportResponse.FAILED) {
                StringBuilder exceptionStringBuilder = new StringBuilder();

                exceptionStringBuilder.append("Unable to deploy Service Provider " +
                        serviceProvider.getApplicationName());
                for (String errorText : importResponse.getErrors()) {
                    exceptionStringBuilder.append(System.lineSeparator());
                    exceptionStringBuilder.append(errorText);
                }

                throw new RemoteFetchCoreException(exceptionStringBuilder.toString());
            }
        } catch (IdentityApplicationManagementException e) {
            throw new RemoteFetchCoreException("Unable to deploy Service Provider " +
                    serviceProvider.getApplicationName(), e);
        }
        endTenantFlow();

    }

    /**
     * resolve the unique identifier for the configuration
     *
     * @param configurationFileStream
     * @return
     * @throws RemoteFetchCoreException
     */
    @Override
    public String resolveConfigName(ConfigurationFileStream configurationFileStream) throws RemoteFetchCoreException {

        return this.getServiceProviderFromStream(configurationFileStream).getApplicationName();
    }

    private ServiceProvider getServiceProviderFromStream(ConfigurationFileStream configurationFileStream) throws RemoteFetchCoreException {

        SpFileStream spFileStream = new SpFileStream(configurationFileStream.getContentStream(),
                configurationFileStream.getPath().getName());

        String tenantDomain = this.getTenantDomain();

        try {
            return ApplicationMgtUtil.getApplicationFromSpFileStream(spFileStream, tenantDomain);
        } catch (IdentityApplicationManagementException e) {
            throw new RemoteFetchCoreException("Unable to get name for specified config file " +
                    configurationFileStream.getPath(), e);
        }
    }

    private String getTenantDomain() throws RemoteFetchCoreException {

        try {
            return RemoteFetchServiceComponentHolder.getInstance().getRealmService().getTenantManager()
                    .getDomain(this.tenantId);
        } catch (UserStoreException e) {
            throw new RemoteFetchCoreException("Unable to get tenant domain for tenant id " + tenantId, e);
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
