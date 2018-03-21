/*
 * Copyright (c) 2013, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.identity.application.authentication.framework.config.builder;

import org.wso2.carbon.identity.application.authentication.framework.config.loader.UIBasedConfigurationLoader;
import org.wso2.carbon.identity.application.authentication.framework.config.model.SequenceConfig;
import org.wso2.carbon.identity.application.authentication.framework.exception.FrameworkException;
import org.wso2.carbon.identity.application.common.IdentityApplicationManagementException;
import org.wso2.carbon.identity.application.common.model.ServiceProvider;
import org.wso2.carbon.identity.application.mgt.ApplicationManagementService;

/**
 * @Depricated Please use {@link UIBasedConfigurationLoader}. It is wrong to have this class as singleton.
 * The *Builder suffix gives wrong meaning, This is not conforming to Builder pattern.
 */
@Deprecated
public class UIBasedConfigurationBuilder {

    private static volatile UIBasedConfigurationBuilder instance;
    private UIBasedConfigurationLoader uiBasedConfigurationLoader = new UIBasedConfigurationLoader();

    public static UIBasedConfigurationBuilder getInstance() {

        if (instance == null) {
            synchronized (UIBasedConfigurationBuilder.class) {
                if (instance == null) {
                    instance = new UIBasedConfigurationBuilder();
                }
            }
        }

        return instance;
    }

    public SequenceConfig getSequence(String reqType, String clientId, String tenantDomain)
            throws FrameworkException {

        ApplicationManagementService appInfo = ApplicationManagementService.getInstance();

        // special case for OpenID Connect, these clients are stored as OAuth2 clients
        if ("oidc".equals(reqType)) {
            reqType = "oauth2";
        }

        ServiceProvider serviceProvider;

        try {
            serviceProvider = appInfo.getServiceProviderByClientId(clientId, reqType, tenantDomain);
        } catch (IdentityApplicationManagementException e) {
            throw new FrameworkException(e.getMessage(), e);
        }
        return uiBasedConfigurationLoader.getSequence(serviceProvider, tenantDomain);
    }

}
