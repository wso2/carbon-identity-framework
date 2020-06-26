/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.identity.cors.mgt.core.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.wso2.carbon.identity.configuration.mgt.core.ConfigurationManager;
import org.wso2.carbon.identity.cors.mgt.core.CORSManagementService;
import org.wso2.carbon.identity.cors.mgt.core.internal.impl.CORSManagementServiceImpl;

/**
 * Service component class for CORS-Service.
 */
@Component(
        name = "identity.cors.management.component",
        immediate = true
)
public class CORSManagementServiceComponent {

    private static final Log log = LogFactory.getLog(CORSManagementServiceComponent.class);

    /**
     * Activate the CORSManagementServiceComponent.
     *
     * @param context
     */
    @Activate
    protected void activate(ComponentContext context) {

        context.getBundleContext()
                .registerService(CORSManagementService.class, new CORSManagementServiceImpl(), null);

        if (log.isDebugEnabled()) {
            log.debug("CORSManagementServiceComponent is activated.");
        }
    }

    /**
     * Deactivate the CORSManagementServiceComponent.
     *
     * @param context
     */
    @Deactivate
    protected void deactivate(ComponentContext context) {

        if (log.isDebugEnabled()) {
            log.debug("CORSManagementServiceComponent is deactivated.");
        }
    }

    /**
     * Set the ConfigurationManager.
     *
     * @param configurationManager
     */
    @Reference(
            name = "resource.configuration.manager",
            service = ConfigurationManager.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetConfigurationManager"
    )
    protected void setConfigurationManager(ConfigurationManager configurationManager) {

        if (log.isDebugEnabled()) {
            log.debug("Setting the ConfigurationManager.");
        }
        CORSManagementServiceHolder.getInstance().setConfigurationManager(configurationManager);
    }

    /**
     * Unset the ConfigurationManager.
     *
     * @param configurationManager
     */
    protected void unsetConfigurationManager(ConfigurationManager configurationManager) {

        if (log.isDebugEnabled()) {
            log.debug("Unsetting the ConfigurationManager.");
        }
        CORSManagementServiceHolder.getInstance().setConfigurationManager(null);
    }
}
