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
import org.wso2.carbon.identity.action.management.dao.impl.ActionPropertyResolverFactory;
import org.wso2.carbon.identity.action.management.service.ActionConverter;
import org.wso2.carbon.identity.action.management.service.ActionManagementService;
import org.wso2.carbon.identity.action.management.service.ActionPropertyResolver;
import org.wso2.carbon.identity.action.management.service.impl.ActionConverterFactory;
import org.wso2.carbon.identity.action.management.service.impl.CacheBackedActionManagementService;
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
            bundleCtx.registerService(ActionManagementService.class, CacheBackedActionManagementService.getInstance(),
                    null);
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
            LOG.debug("Action management bundle is deactivated");
        } catch (Throwable e) {
            LOG.error("Error while deactivating Action management component.", e);
        }
    }

    @Reference(
            name = "action.converter",
            service = ActionConverter.class,
            cardinality = ReferenceCardinality.MULTIPLE,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetActionConverter"
    )
    protected void setActionConverter(ActionConverter actionConverter) {

        LOG.debug("Registering ActionConverter: " + actionConverter.getClass().getName() +
                " in the ActionMgtServiceComponent.");
        ActionConverterFactory.registerActionConverter(actionConverter);
    }

    protected void unsetActionConverter(ActionConverter actionConverter) {

        if (LOG.isDebugEnabled()) {
            LOG.debug("Unregistering ActionConverter: " + actionConverter.getClass().getName() +
                    " in the ActionMgtServiceComponent.");
        }

        ActionConverterFactory.unregisterActionConverter(actionConverter);
    }

    @Reference(
            name = "action.property.resolver",
            service = ActionPropertyResolver.class,
            cardinality = ReferenceCardinality.MULTIPLE,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetActionPropertyResolver"
    )
    protected void setActionPropertyResolver(ActionPropertyResolver actionPropertyResolver) {

        if (LOG.isDebugEnabled()) {
            LOG.debug("Registering ActionPropertyResolver: " + actionPropertyResolver.getClass().getName() +
                    " in the ActionMgtServiceComponent.");
        }
        ActionPropertyResolverFactory.registerActionPropertyResolver(actionPropertyResolver);
    }

    protected void unsetActionPropertyResolver(ActionPropertyResolver actionPropertyResolver) {

        if (LOG.isDebugEnabled()) {
            LOG.debug("Unregistering ActionPropertyResolver: " + actionPropertyResolver.getClass().getName() +
                    " in the ActionMgtServiceComponent.");
        }
        ActionPropertyResolverFactory.unregisterActionPropertyResolver(actionPropertyResolver);
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
        LOG.debug("SecretManager set in ActionMgtServiceComponentHolder bundle.");
    }

    private void unsetSecretManager(SecretManager secretManager) {

        ActionMgtServiceComponentHolder.getInstance().setSecretManager(null);
        LOG.debug("SecretManager unset in ActionMgtServiceComponentHolder bundle.");
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
        LOG.debug("SecretResolveManager set in ActionMgtServiceComponentHolder bundle.");
    }

    private void unsetSecretResolveManager(SecretResolveManager secretResolveManager) {

        ActionMgtServiceComponentHolder.getInstance().setSecretResolveManager(null);
        LOG.debug("SecretResolveManager unset in ActionMgtServiceComponentHolder bundle.");
    }
}
