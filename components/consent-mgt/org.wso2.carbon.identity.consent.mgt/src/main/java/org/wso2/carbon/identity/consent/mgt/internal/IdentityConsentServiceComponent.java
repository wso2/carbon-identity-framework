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

package org.wso2.carbon.identity.consent.mgt.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.wso2.carbon.consent.mgt.core.ConsentManager;
import org.wso2.carbon.consent.mgt.core.PrivilegedConsentManager;
import org.wso2.carbon.identity.application.authentication.framework.handler.request.impl.consent.SSOConsentService;
import org.wso2.carbon.identity.application.mgt.listener.ApplicationMgtListener;
import org.wso2.carbon.identity.consent.mgt.handler.ConsentDeletionUserEventHandler;
import org.wso2.carbon.identity.consent.mgt.listener.ConsentDeletionAppMgtListener;
import org.wso2.carbon.identity.consent.mgt.listener.TenantConsentMgtListener;
import org.wso2.carbon.identity.consent.mgt.services.ConsentUtilityService;
import org.wso2.carbon.identity.core.util.IdentityCoreInitializedEvent;
import org.wso2.carbon.identity.event.handler.AbstractEventHandler;
import org.wso2.carbon.stratos.common.listeners.TenantMgtListener;

/**
 * OSGi declarative services component which handled registration and unregistration of IdentityConsentServiceComponent.
 */

@Component(
        name = "identity.consent.mgt.component",
        immediate = true
)
public class IdentityConsentServiceComponent {

    private static final Log log = LogFactory.getLog(IdentityConsentServiceComponent.class);

    @Activate
    protected void activate(ComponentContext ctxt) {

        try {
            ctxt.getBundleContext().registerService(AbstractEventHandler.class.getName(),
                    new ConsentDeletionUserEventHandler(), null);
            ctxt.getBundleContext().registerService(ApplicationMgtListener.class.getName(),
                    new ConsentDeletionAppMgtListener(), null);
            ctxt.getBundleContext().registerService(TenantMgtListener.class.getName(), new TenantConsentMgtListener()
                    , null);
            ctxt.getBundleContext().registerService(ConsentUtilityService.class.getName(), new ConsentUtilityService
                    (), null);
        } catch (Throwable throwable) {
            log.error("Error while activating Identity Consent Service Component.", throwable);
        }
    }

    @Reference(
            name = "consent.mgt.service",
            service = ConsentManager.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetConsentMgtService"
    )
    protected void setConsentMgtService(ConsentManager consentManager) {

        if (log.isDebugEnabled()) {
            log.debug("Consent Manger is set in the Identity Consent Service component bundle.");
        }
        IdentityConsentDataHolder.getInstance().setConsentManager(consentManager);
    }

    protected void unsetConsentMgtService(ConsentManager consentManager) {

        IdentityConsentDataHolder.getInstance().setConsentManager(null);
    }

    @Reference(
            name = "privileged.consent.manager",
            service = PrivilegedConsentManager.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetPrivilegedConsentManager"
    )
    protected void setPrivilegedConsentManager(PrivilegedConsentManager consentManager) {

        if (log.isDebugEnabled()) {
            log.debug("Privileged Consent Manger is set in the Identity Consent Service component bundle.");
        }
        IdentityConsentDataHolder.getInstance().setPrivilegedConsentManager(consentManager);
    }

    protected void unsetPrivilegedConsentManager(PrivilegedConsentManager consentManager) {

        IdentityConsentDataHolder.getInstance().setPrivilegedConsentManager(null);
    }

    @Reference(
            name = "identity.application.authentication.framework.component",
            service = SSOConsentService.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetSSOConsentService"
    )
    protected void setSSOConsentService(SSOConsentService ssoConsentService) {

        if (log.isDebugEnabled()) {
            log.debug("SSO Consent Service is set in the Identity Consent Service component bundle.");
        }
        IdentityConsentDataHolder.getInstance().setSSOConsentService(ssoConsentService);
    }

    protected void unsetSSOConsentService(SSOConsentService ssoConsentService) {

        IdentityConsentDataHolder.getInstance().setSSOConsentService(null);
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
}
