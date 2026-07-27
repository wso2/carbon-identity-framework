/*
 * Copyright (c) 2024, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.carbon.identity.trusted.app.mgt.internal;

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
import org.osgi.service.http.HttpService;
import org.wso2.carbon.identity.application.mgt.ApplicationManagementService;
import org.wso2.carbon.identity.trusted.app.mgt.services.TrustedAppMgtService;
import org.wso2.carbon.identity.trusted.app.mgt.services.TrustedAppMgtServiceImpl;

/**
 * Trusted App Management Service Component.
 */
@Component(
        name = "identity.trusted.app.mgt.component",
        immediate = true
)
public class TrustedAppMgtServiceComponent {

    private static final Log LOG = LogFactory.getLog(TrustedAppMgtServiceComponent.class);

    @Activate
    protected void activate(ComponentContext context) {

        try {
            TrustedAppMgtService trustedAppMgtService = new TrustedAppMgtServiceImpl();
            BundleContext bundleContext = context.getBundleContext();
            bundleContext.registerService(TrustedAppMgtService.class, trustedAppMgtService, null);
            TrustedAppMgtDataHolder.getInstance().setTrustedAppMgtService(trustedAppMgtService);

            if (LOG.isDebugEnabled()) {
                LOG.debug("TrustedAppMgtServiceComponent is activated.");
            }
        } catch (Throwable e) {
            LOG.error("Error while activating the TrustedAppMgtServiceComponent.", e);
        }
    }

    @Deactivate
    protected void deactivate(ComponentContext context) {

        if (LOG.isDebugEnabled()) {
            LOG.debug("TrustedAppMgtServiceComponent is deactivated.");
        }
    }

    @Reference(
            name = "osgi.http.service",
            service = HttpService.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetHttpService"
    )
    protected void setHttpService(HttpService httpService) {

        if (LOG.isDebugEnabled()) {
            LOG.debug("HTTP Service is set in Trusted App mgt bundle");
        }
        TrustedAppMgtDataHolder.getInstance().setHttpService(httpService);
    }

    protected void unsetHttpService(HttpService httpService) {

        if (LOG.isDebugEnabled()) {
            LOG.debug("HTTP Service is unset in the Trusted App mgt bundle");
        }
        TrustedAppMgtDataHolder.getInstance().setHttpService(null);
    }

    @Reference(
            name = "org.wso2.carbon.identity.application.mgt.ApplicationManagementService",
            service = org.wso2.carbon.identity.application.mgt.ApplicationManagementService.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetApplicationManagementService")
    protected void setApplicationManagementService(ApplicationManagementService applicationManagementService) {

        TrustedAppMgtDataHolder.getInstance().setApplicationManagementService(applicationManagementService);
        LOG.debug("ApplicationManagementService set in EntitlementServiceComponent bundle.");
    }

    protected void unsetApplicationManagementService(ApplicationManagementService applicationManagementService) {

        TrustedAppMgtDataHolder.getInstance().setApplicationManagementService(null);
        LOG.debug("ApplicationManagementService unset in EntitlementServiceComponent bundle.");
    }
}
