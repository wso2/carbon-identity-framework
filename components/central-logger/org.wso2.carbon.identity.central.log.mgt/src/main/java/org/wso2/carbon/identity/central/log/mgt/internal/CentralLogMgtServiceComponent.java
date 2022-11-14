/*
 * Copyright (c) 2021, WSO2 Inc. (http://www.wso2.com).
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

package org.wso2.carbon.identity.central.log.mgt.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.wso2.carbon.identity.central.log.mgt.hanlder.CentralLogger;
import org.wso2.carbon.identity.central.log.mgt.utils.LoggerUtils;
import org.wso2.carbon.identity.event.handler.AbstractEventHandler;
import org.wso2.carbon.identity.event.services.IdentityEventService;

/**
 * OSGi declarative services component which handled activation and deactivation of central logger event handler.
 */
@Component(
    name = "central.log.management.service",
    immediate = true
)
public class CentralLogMgtServiceComponent {

    private static final Log log = LogFactory.getLog(CentralLogMgtServiceComponent.class);
    private ServiceRegistration serviceRegistration = null;

    @Activate
    protected void activate(ComponentContext context) {

        BundleContext bundleContext = context.getBundleContext();
        // Registering central logger event handler as an OSGIService.
        serviceRegistration =
                bundleContext.registerService(AbstractEventHandler.class.getName(), new CentralLogger(), null);
        if (log.isDebugEnabled()) {
            log.debug("Central logger event handler is activated.");
        }
        LoggerUtils.getLogMaskingConfigValue();
    }

    @Deactivate
    protected void deactivate(ComponentContext context) {

        if (log.isDebugEnabled()) {
            log.debug("Central logger event handler is deactivated.");
        }
        // Unregistering Central logger event handler.
        if (serviceRegistration != null) {
            serviceRegistration.unregister();
        }
    }

    @Reference(
            name = "identity.event.service",
            service = IdentityEventService.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetIdentityEventService"
    )
    protected void setIdentityEventService(IdentityEventService identityEventService) {

        CentralLogMgtServiceComponentHolder.getInstance().setIdentityEventService(identityEventService);
        if (log.isDebugEnabled()) {
            log.debug("IdentityEventService set in Central logger.");
        }
    }

    protected void unsetIdentityEventService(IdentityEventService identityEventService) {

        CentralLogMgtServiceComponentHolder.getInstance().setIdentityEventService(null);
    }
}
