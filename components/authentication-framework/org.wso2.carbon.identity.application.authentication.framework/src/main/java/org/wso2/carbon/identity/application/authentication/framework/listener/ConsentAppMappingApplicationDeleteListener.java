/*
 * Copyright (c) 2026, WSO2 LLC. (http://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
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

package org.wso2.carbon.identity.application.authentication.framework.listener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.application.authentication.framework.exception.ConsentAppMappingException;
import org.wso2.carbon.identity.application.authentication.framework.internal.FrameworkServiceDataHolder;
import org.wso2.carbon.identity.application.common.IdentityApplicationManagementException;
import org.wso2.carbon.identity.application.common.model.ServiceProvider;
import org.wso2.carbon.identity.application.mgt.listener.AbstractApplicationMgtListener;

/**
 * Cleans up consent purpose-application mappings when an application is deleted.
 */
public class ConsentAppMappingApplicationDeleteListener extends AbstractApplicationMgtListener {

    private static final Log LOG = LogFactory.getLog(ConsentAppMappingApplicationDeleteListener.class);

    @Override
    public int getDefaultOrderId() {

        return 908;
    }

    @Override
    public boolean doPostDeleteApplication(ServiceProvider serviceProvider, String tenantDomain, String userName)
            throws IdentityApplicationManagementException {

        String applicationResourceId = serviceProvider.getApplicationResourceId();
        if (LOG.isDebugEnabled()) {
            LOG.debug("Removing consent purpose mappings for deleted application: " + applicationResourceId);
        }
        try {
            FrameworkServiceDataHolder.getInstance().getConsentAppMappingService()
                    .removeAllPurposeMappingsForApplication(applicationResourceId);
        } catch (ConsentAppMappingException e) {
            LOG.warn("Failed to remove purpose mappings for deleted application: " + applicationResourceId, e);
        }
        return true;
    }
}
