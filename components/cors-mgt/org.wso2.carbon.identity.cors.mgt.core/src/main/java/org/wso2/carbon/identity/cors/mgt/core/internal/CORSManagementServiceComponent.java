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
import org.osgi.framework.BundleContext;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.wso2.carbon.identity.configuration.mgt.core.ConfigurationManager;
import org.wso2.carbon.identity.cors.mgt.core.CORSManagementService;
import org.wso2.carbon.identity.cors.mgt.core.dao.CORSConfigurationDAO;
import org.wso2.carbon.identity.cors.mgt.core.dao.CORSOriginDAO;
import org.wso2.carbon.identity.cors.mgt.core.dao.impl.CORSConfigurationDAOImpl;
import org.wso2.carbon.identity.cors.mgt.core.dao.impl.CORSOriginDAOImpl;
import org.wso2.carbon.identity.cors.mgt.core.internal.impl.CORSManagementServiceImpl;
import org.wso2.carbon.identity.cors.mgt.core.model.CORSManagementServiceConfigurationHolder;

import java.util.Comparator;

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

        BundleContext bundleContext = context.getBundleContext();
        bundleContext.registerService(CORSOriginDAO.class,
                new CORSOriginDAOImpl(), null);
        bundleContext.registerService(CORSConfigurationDAO.class,
                new CORSConfigurationDAOImpl(), null);

        CORSManagementServiceConfigurationHolder corsManagementServiceConfigurationHolder =
                new CORSManagementServiceConfigurationHolder();
        corsManagementServiceConfigurationHolder
                .setCorsOriginDAOS(CORSManagementServiceHolder.getInstance().getCorsOriginDAOS());
        corsManagementServiceConfigurationHolder
                .setCorsConfigurationDAOS(CORSManagementServiceHolder.getInstance().getCorsConfigurationDAOS());

        bundleContext.registerService(CORSManagementService.class,
                new CORSManagementServiceImpl(corsManagementServiceConfigurationHolder), null);

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

    @Reference(
            name = "cors.origins.dao",
            service = CORSOriginDAO.class,
            cardinality = ReferenceCardinality.MULTIPLE,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetCORSOrigin"
    )
    protected void setCORSOrigin(CORSOriginDAO corsOriginDAO) {

        if (CORSManagementServiceHolder.getInstance().getCorsOriginDAOS() != null) {
            if (log.isDebugEnabled()) {
                log.debug("CORSOriginDAO is registered in CORSManagementService.");
            }

            CORSManagementServiceHolder.getInstance().getCorsOriginDAOS().add(corsOriginDAO);
            CORSManagementServiceHolder.getInstance().getCorsOriginDAOS()
                    .sort(Comparator.comparingInt(CORSOriginDAO::getPriority));
        }
    }

    protected void unsetCORSOrigin(CORSOriginDAO corsOriginDAO) {

        if (log.isDebugEnabled()) {
            log.debug("CORSOriginDAO is unregistered in CORSManagementService.");
        }
        CORSManagementServiceHolder.getInstance().getCorsOriginDAOS().remove(corsOriginDAO);
    }

    @Reference(
            name = "cors.configuration.dao",
            service = CORSConfigurationDAO.class,
            cardinality = ReferenceCardinality.MULTIPLE,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetCORSConfiguration"
    )
    protected void setCORSConfiguration(CORSConfigurationDAO corsConfigurationDAO) {

        if (CORSManagementServiceHolder.getInstance().getCorsConfigurationDAOS() != null) {
            if (log.isDebugEnabled()) {
                log.debug("CORSConfigurationDAO is registered in CORSManagementService.");
            }

            CORSManagementServiceHolder.getInstance().getCorsConfigurationDAOS().add(corsConfigurationDAO);
            CORSManagementServiceHolder.getInstance().getCorsConfigurationDAOS()
                    .sort(Comparator.comparingInt(CORSConfigurationDAO::getPriority));
        }
    }

    protected void unsetCORSConfiguration(CORSConfigurationDAO corsConfigurationDAO) {

        if (log.isDebugEnabled()) {
            log.debug("CORSConfigurationDAO is unregistered in CORSManagementService.");
        }
        CORSManagementServiceHolder.getInstance().getCorsConfigurationDAOS().remove(corsConfigurationDAO);
    }

    /**
     * Set the ConfigurationManager.
     *
     * @param configurationManager The {@code ConfigurationManager} instance.
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
     * @param configurationManager The {@code ConfigurationManager} instance.
     */
    protected void unsetConfigurationManager(ConfigurationManager configurationManager) {

        if (log.isDebugEnabled()) {
            log.debug("Unsetting the ConfigurationManager.");
        }
        CORSManagementServiceHolder.getInstance().setConfigurationManager(null);
    }
}
