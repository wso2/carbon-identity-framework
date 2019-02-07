/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.wso2.carbon.directory.server.manager;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.application.common.IdentityApplicationManagementException;
import org.wso2.carbon.identity.application.common.model.InboundAuthenticationConfig;
import org.wso2.carbon.identity.application.common.model.InboundAuthenticationRequestConfig;
import org.wso2.carbon.identity.application.common.model.ServiceProvider;
import org.wso2.carbon.identity.application.mgt.ApplicationMgtSystemConfig;
import org.wso2.carbon.identity.application.mgt.dao.ApplicationDAO;
import org.wso2.carbon.identity.application.mgt.listener.AbstractApplicationMgtListener;

/**
 * The application listener responsible for handling "kerberos" related events in application management process.
 */
public class DirectoryServerApplicationMgtListener extends AbstractApplicationMgtListener {

    private static Log log = LogFactory.getLog(DirectoryServerApplicationMgtListener.class);

    private static final String KERBEROS = "kerberos";


    @Override
    public int getDefaultOrderId() {

        // Force this listener to be executed last as we are deleting the kerberos configurations in th pre-delete
        // method.
        return 999;
    }

    @Override
    public boolean doPreDeleteApplication(String applicationName, String tenantDomain, String userName)
            throws IdentityApplicationManagementException {

        ApplicationDAO appDAO = ApplicationMgtSystemConfig.getInstance().getApplicationDAO();
        ServiceProvider serviceProvider = appDAO.getApplication(applicationName, tenantDomain);
        if (serviceProvider != null &&
                serviceProvider.getInboundAuthenticationConfig() != null &&
                serviceProvider.getInboundAuthenticationConfig().getInboundAuthenticationRequestConfigs() != null) {
            InboundAuthenticationRequestConfig[] configs = serviceProvider.getInboundAuthenticationConfig()
                    .getInboundAuthenticationRequestConfigs();
            for (InboundAuthenticationRequestConfig config : configs) {
                if (KERBEROS.equalsIgnoreCase(config.getInboundAuthType()) && config.getInboundAuthKey() != null) {
                    DirectoryServerManager directoryServerManager = new DirectoryServerManager();
                    try {
                        directoryServerManager.removeServer(config.getInboundAuthKey());
                    } catch (DirectoryServerManagerException e) {
                        String error = "Error while removing a kerberos: " + config.getInboundAuthKey();
                        throw new IdentityApplicationManagementException(error, e);
                    }
                    break;
                }
            }
        }
        return true;
    }

    @Override
    public void doExportServiceProvider(ServiceProvider serviceProvider, Boolean exportSecrets)
            throws IdentityApplicationManagementException {

        InboundAuthenticationConfig inboundAuthenticationConfig = serviceProvider.getInboundAuthenticationConfig();
        if (inboundAuthenticationConfig != null &&
                inboundAuthenticationConfig.getInboundAuthenticationRequestConfigs() != null) {
            for (InboundAuthenticationRequestConfig authConfig
                    : inboundAuthenticationConfig.getInboundAuthenticationRequestConfigs()) {
                if (KERBEROS.equals(authConfig.getInboundAuthType())) {
                    inboundAuthenticationConfig.setInboundAuthenticationRequestConfigs(
                            (InboundAuthenticationRequestConfig[]) ArrayUtils.removeElement
                                    (inboundAuthenticationConfig.getInboundAuthenticationRequestConfigs(), authConfig));
                    return;
                }
            }
        }
    }
}
