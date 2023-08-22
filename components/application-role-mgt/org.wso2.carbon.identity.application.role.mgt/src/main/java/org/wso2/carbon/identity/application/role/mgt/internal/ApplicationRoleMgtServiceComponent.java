/*
 * Copyright (c) 2023, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.carbon.identity.application.role.mgt.internal;

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
import org.wso2.carbon.identity.application.role.mgt.ApplicationRoleManager;
import org.wso2.carbon.identity.application.role.mgt.ApplicationRoleManagerImpl;
import org.wso2.carbon.idp.mgt.IdpManager;
import org.wso2.carbon.user.core.service.RealmService;

/**
 * OSGi declarative services component which handled activation and deactivation of Application Role Management.
 */
@Component(
        name = "identity.application.role.mgt.component",
        immediate = true
)
public class ApplicationRoleMgtServiceComponent {

    private static final Log LOG = LogFactory.getLog(ApplicationRoleMgtServiceComponent.class);

    @Activate
    protected void activate(ComponentContext ctxt) {

        try {
            BundleContext bundleCtx = ctxt.getBundleContext();
            bundleCtx.registerService(ApplicationRoleManager.class, ApplicationRoleManagerImpl.getInstance(), null);
        } catch (Throwable e) {
            LOG.error("Error while initializing application role management component.", e);
        }
    }

    @Deactivate
    protected void deactivate(ComponentContext ctxt) {

        try {
            BundleContext bundleCtx = ctxt.getBundleContext();
            bundleCtx.ungetService(bundleCtx.getServiceReference(ApplicationRoleManager.class));
            if (LOG.isDebugEnabled()) {
                LOG.debug("application role management bundle is deactivated");
            }
        } catch (Throwable e) {
            LOG.error("Error while deactivating application role management component.", e);
        }
    }

    @Reference(
            name = "realm.service",
            service = RealmService.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetRealmService")
    protected void setRealmService(RealmService realmService) {

        if (LOG.isDebugEnabled()) {
            LOG.debug("Setting the Realm Service");
        }
        ApplicationRoleMgtServiceComponentHolder.getInstance().setRealmService(realmService);
    }

    protected void unsetRealmService(RealmService realmService) {

        if (LOG.isDebugEnabled()) {
            LOG.debug("Unset the Realm Service.");
        }
        ApplicationRoleMgtServiceComponentHolder.getInstance().setRealmService(null);
    }

    @Reference(
            name = "idp.mgt.dscomponent",
            service = IdpManager.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetIdentityProviderManager"
    )
    protected void setIdentityProviderManager(IdpManager idpMgtService) {

        ApplicationRoleMgtServiceComponentHolder.getInstance().setIdentityProviderManager(idpMgtService);
    }

    protected void unsetIdentityProviderManager(IdpManager idpMgtService) {

        ApplicationRoleMgtServiceComponentHolder.getInstance().setIdentityProviderManager(null);
    }
}
