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

package org.wso2.carbon.identity.action.management.internal;

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
import org.wso2.carbon.identity.action.management.ActionManagementService;
import org.wso2.carbon.identity.action.management.ActionManagementServiceImpl;
import org.wso2.carbon.identity.action.management.listener.ActionManagementListener;
import org.wso2.carbon.identity.action.management.listener.ActionManagementV2AuditLogger;
import org.wso2.carbon.identity.event.services.IdentityEventService;
import org.wso2.carbon.identity.secret.mgt.core.SecretManager;
import org.wso2.carbon.identity.secret.mgt.core.SecretResolveManager;

/**
 * Service component for the Action management.
 */
@Component(
        name = "action.mgt.service.component",
        immediate = true
)
public class ActionMgtServiceComponent {

    private static final Log LOG = LogFactory.getLog(ActionMgtServiceComponent.class);

    @Activate
    protected void activate(ComponentContext context) {

        try {
            BundleContext bundleCtx = context.getBundleContext();
            bundleCtx.registerService(ActionManagementService.class, ActionManagementServiceImpl.getInstance(), null);
            bundleCtx.registerService(ActionManagementListener.class, new ActionManagementV2AuditLogger(), null);

            LOG.debug("Action management bundle is activated");
        } catch (Throwable e) {
            LOG.error("Error while initializing Action management component.", e);
        }
    }

    @Deactivate
    protected void deactivate(ComponentContext context) {

        try {
            BundleContext bundleCtx = context.getBundleContext();
            bundleCtx.ungetService(bundleCtx.getServiceReference(ActionManagementService.class));
            bundleCtx.ungetService(bundleCtx.getServiceReference(ActionManagementListener.class));
            LOG.debug("Action management bundle is deactivated");
        } catch (Throwable e) {
            LOG.error("Error while deactivating Action management component.", e);
        }
    }

    @Reference(
            name = "org.wso2.carbon.identity.secret.mgt.core.SecretManager",
            service = SecretManager.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetSecretManager"
    )
    private void setSecretManager(SecretManager secretManager) {

        ActionMgtServiceComponentHolder.getInstance().setSecretManager(secretManager);
    }

    private void unsetSecretManager(SecretManager secretManager) {

        ActionMgtServiceComponentHolder.getInstance().setSecretManager(null);
    }

    @Reference(
            name = "org.wso2.carbon.identity.secret.mgt.core.SecretResolveManager",
            service = SecretResolveManager.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetSecretResolveManager"
    )
    private void setSecretResolveManager(SecretResolveManager secretResolveManager) {

        ActionMgtServiceComponentHolder.getInstance().setSecretResolveManager(secretResolveManager);
    }

    private void unsetSecretResolveManager(SecretResolveManager secretResolveManager) {

        ActionMgtServiceComponentHolder.getInstance().setSecretResolveManager(null);
    }

    @Reference(
            name = "identity.event.service",
            service = IdentityEventService.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetIdentityEventService"
    )
    protected void setIdentityEventService(IdentityEventService identityEventService) {

        ActionMgtServiceComponentHolder.getInstance().setIdentityEventService(identityEventService);
        LOG.debug("IdentityEventService set in Action Management bundle");
    }

    protected void unsetIdentityEventService(IdentityEventService identityEventService) {

        ActionMgtServiceComponentHolder.getInstance().setIdentityEventService(null);
        LOG.debug("IdentityEventService set in Action Management bundle");
    }

    @Reference(
            name = "action.management.listener",
            service = ActionManagementListener.class,
            cardinality = ReferenceCardinality.MULTIPLE,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetActionManagementListener"
    )
    protected void setActionManagementListener(ActionManagementListener actionManagementListener) {

        ActionMgtServiceComponentHolder.getInstance().addActionManagementListener(actionManagementListener);
    }

    protected void unsetActionManagementListener(ActionManagementListener actionManagementListener) {

        ActionMgtServiceComponentHolder.getInstance().setActionManagementListenerList(null);
    }

    @Reference(
            name = "action.management.audit.v2.logger",
            service = ActionManagementV2AuditLogger.class,
            cardinality = ReferenceCardinality.MULTIPLE,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetActionManagementAuditV2Logger"
    )
    protected void setActionManagementAuditV2Logger(ActionManagementV2AuditLogger actionManagementV2AuditLogger) {

        ActionMgtServiceComponentHolder.getInstance().addActionManagementListener(actionManagementV2AuditLogger);
    }

    protected void unsetActionManagementAuditV2Logger(ActionManagementV2AuditLogger actionManagementListener) {

        ActionMgtServiceComponentHolder.getInstance().setActionManagementListenerList(null);
    }
}
