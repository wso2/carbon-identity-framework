/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.identity.application.mgt.listener;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.identity.application.common.IdentityApplicationManagementClientException;
import org.wso2.carbon.identity.application.common.IdentityApplicationManagementException;
import org.wso2.carbon.identity.application.common.model.InboundAuthenticationRequestConfig;
import org.wso2.carbon.identity.application.common.model.ServiceProvider;
import org.wso2.carbon.identity.application.common.util.IdentityApplicationConstants;
import org.wso2.carbon.identity.application.mgt.dao.ApplicationDAO;
import org.wso2.carbon.identity.application.mgt.dao.impl.ApplicationDAOImpl;

import java.util.Arrays;

import static org.wso2.carbon.identity.application.common.util.IdentityApplicationConstants.Error
        .INBOUND_KEY_ALREADY_EXISTS;

/**
 * Listener for validating inbound authentication keys of Passive-STS and WS-Trust auth types.
 */
public class InboundAuthKeyValidationListener extends AbstractApplicationMgtListener {

    private static Log log = LogFactory.getLog(InboundAuthKeyValidationListener.class);

    @Override
    public int getDefaultOrderId() {

        return 222;
    }

    public boolean doPreCreateApplication(ServiceProvider serviceProvider, String tenantDomain, String userName)
            throws IdentityApplicationManagementException {

        validateInboundAuthKey(serviceProvider, IdentityApplicationConstants.Authenticator.WSTrust.NAME);
        validateInboundAuthKey(serviceProvider, IdentityApplicationConstants.Authenticator.PassiveSTS.NAME);
        return true;
    }

    @Override
    public boolean doPreUpdateApplication(ServiceProvider serviceProvider,
                                          String tenantDomain,
                                          String userName) throws IdentityApplicationManagementException {

        validateInboundAuthKey(serviceProvider, IdentityApplicationConstants.Authenticator.WSTrust.NAME);
        validateInboundAuthKey(serviceProvider, IdentityApplicationConstants.Authenticator.PassiveSTS.NAME);
        return true;
    }

    @Override
    public void onPreCreateInbound(ServiceProvider serviceProvider, boolean isUpdate)
            throws IdentityApplicationManagementException {

        validateInboundAuthKey(serviceProvider, IdentityApplicationConstants.Authenticator.WSTrust.NAME);
        validateInboundAuthKey(serviceProvider, IdentityApplicationConstants.Authenticator.PassiveSTS.NAME);
    }

    /**
     * Validate whether the configured inbound authentication key is already being used by another application.
     *
     * @param serviceProvider New service provider information.
     * @param inboundAuthType Inbound authentication type.
     * @throws IdentityApplicationManagementException IdentityApplicationManagementException.
     */
    private void validateInboundAuthKey(ServiceProvider serviceProvider, String inboundAuthType) throws
            IdentityApplicationManagementException {

        InboundAuthenticationRequestConfig inboundConfig = getInboundConfig(serviceProvider, inboundAuthType);
        if (inboundConfig == null) {
            return;
        }

        String tenantDomain = CarbonContext.getThreadLocalCarbonContext().getTenantDomain();
        /* We need to directly retrieve the application from DB since {@link ServiceProviderByInboundAuthCache} cache
         * can have inconsistent applications stored against the <inbound-auth-key, inbound-auth-type, tenant-domain>
         * cache key which is not unique.
         */
        ApplicationDAO applicationDAO = new ApplicationDAOImpl();
        String existingAppName = applicationDAO.getServiceProviderNameByClientId
                (inboundConfig.getInboundAuthKey(), inboundConfig.getInboundAuthType(), CarbonContext
                        .getThreadLocalCarbonContext().getTenantDomain());

        if (StringUtils.isBlank(existingAppName)) {
            if (log.isDebugEnabled()) {
                log.debug("Cannot find application name for the inbound auth key: " + inboundConfig
                        .getInboundAuthKey() + " of inbound auth type: " + inboundConfig.getInboundAuthType());
            }
            return;
        }
        ServiceProvider existingApp = applicationDAO.getApplication(existingAppName, tenantDomain);

        if (existingApp != null && existingApp.getApplicationID() != serviceProvider.getApplicationID()) {
            String msg = "Inbound key: '" + inboundConfig.getInboundAuthKey() + "' is already configured for the" +
                    " application :'" + existingApp.getApplicationName() + "'";
            throw buildClientException(INBOUND_KEY_ALREADY_EXISTS, msg);
        }
    }

    private InboundAuthenticationRequestConfig getInboundConfig(ServiceProvider sp, String inboundAuthType) {

        if (sp != null && sp.getInboundAuthenticationConfig() != null) {
            if (ArrayUtils.isNotEmpty(sp.getInboundAuthenticationConfig().getInboundAuthenticationRequestConfigs())) {
                return Arrays.stream(sp.getInboundAuthenticationConfig().getInboundAuthenticationRequestConfigs())
                        .filter(inbound -> inboundAuthType.equals(inbound.getInboundAuthType()))
                        .findAny()
                        .orElse(null);
            }
        }
        return null;
    }

    private IdentityApplicationManagementClientException buildClientException(IdentityApplicationConstants.Error
                                                                                      errorMessage, String message) {

        return new IdentityApplicationManagementClientException(errorMessage.getCode(), message);
    }
}
