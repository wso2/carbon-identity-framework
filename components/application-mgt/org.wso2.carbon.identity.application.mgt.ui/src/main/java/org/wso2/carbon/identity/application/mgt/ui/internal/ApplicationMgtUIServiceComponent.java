/*
 * Copyright (c) 2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.identity.application.mgt.ui.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.wso2.carbon.consent.mgt.core.ConsentManager;
import org.wso2.carbon.identity.core.util.IdentityCoreInitializedEvent;
import org.wso2.carbon.utils.ConfigurationContextService;

@Component(
        name = "org.wso2.carbon.identity.application.mgt.ui",
        immediate = true
)
public class ApplicationMgtUIServiceComponent {

    private static final Log log = LogFactory.getLog(ApplicationMgtUIServiceComponent.class);

    @Activate
    protected void activate(ComponentContext context) {
        if (log.isDebugEnabled()) {
            log.debug("Application Management UI bundle acticated!");
        }
    }

    @Deactivate
    protected void deactivate(ComponentContext context) {
        if (log.isDebugEnabled()) {
            log.debug("Application Management UI bundle is deactivated");
        }
    }

    @Reference(
            name = "config.context.service",
            service = ConfigurationContextService.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetConfigurationContextService"
    )
    protected void setConfigurationContextService(ConfigurationContextService configurationContextService) {
        ApplicationMgtServiceComponentHolder.getInstance().setConfigurationContextService(configurationContextService);
        if (log.isDebugEnabled()) {
            log.debug("ConfigurationContextService Instance was set.");
        }
    }

    protected void unsetConfigurationContextService(ConfigurationContextService configurationContextService) {
        ApplicationMgtServiceComponentHolder.getInstance().setConfigurationContextService(null);
        if (log.isDebugEnabled()) {
            log.debug("ConfigurationContextService Instance was unset.");
        }
    }

    @Reference(
            name = "identityCoreInitializedEventService",
            service = IdentityCoreInitializedEvent.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetIdentityCoreInitializedEventService"
    )
    protected void setIdentityCoreInitializedEventService(IdentityCoreInitializedEvent identityCoreInitializedEvent) {
                /* reference IdentityCoreInitializedEvent service to guarantee that this component will wait until identity core
         is started */
    }

    protected void unsetIdentityCoreInitializedEventService(IdentityCoreInitializedEvent identityCoreInitializedEvent) {
                 /* reference IdentityCoreInitializedEvent service to guarantee that this component will wait until identity core
         is started */
    }

    @Reference(
            name = "consent.mgt.service",
            service = ConsentManager.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetConsentMgtService"
    )
    protected void setConsentMgtService(ConsentManager consentManager) {

        ApplicationMgtServiceComponentHolder.getInstance().setConsentManager(consentManager);
    }

    protected void unsetConsentMgtService(ConsentManager consentManager) {

        ApplicationMgtServiceComponentHolder.getInstance().setConsentManager(null);
    }

}
