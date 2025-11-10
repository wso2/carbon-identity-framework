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

package org.wso2.carbon.identity.action.management.internal.component;

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
import org.wso2.carbon.identity.action.management.api.service.ActionConverter;
import org.wso2.carbon.identity.action.management.api.service.ActionDTOModelResolver;
import org.wso2.carbon.identity.action.management.api.service.ActionManagementService;
import org.wso2.carbon.identity.action.management.api.service.ActionValidator;
import org.wso2.carbon.identity.action.management.internal.dao.impl.ActionDTOModelResolverFactory;
import org.wso2.carbon.identity.action.management.internal.service.impl.ActionConverterFactory;
import org.wso2.carbon.identity.action.management.internal.service.impl.ActionValidatorFactory;
import org.wso2.carbon.identity.action.management.internal.service.impl.CacheBackedActionManagementService;
import org.wso2.carbon.identity.rule.management.api.service.RuleManagementService;
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
            service = ActionDTOModelResolver.class,
            cardinality = ReferenceCardinality.MULTIPLE,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetActionPropertyResolver"
    )
    protected void setActionPropertyResolver(ActionDTOModelResolver actionDTOModelResolver) {

        if (LOG.isDebugEnabled()) {
            LOG.debug("Registering ActionPropertyResolver: " + actionDTOModelResolver.getClass().getName() +
                    " in the ActionMgtServiceComponent.");
        }
        ActionDTOModelResolverFactory.registerActionDTOModelResolver(actionDTOModelResolver);
    }

    protected void unsetActionPropertyResolver(ActionDTOModelResolver actionDTOModelResolver) {

        if (LOG.isDebugEnabled()) {
            LOG.debug("Unregistering ActionPropertyResolver: " + actionDTOModelResolver.getClass().getName() +
                    " in the ActionMgtServiceComponent.");
        }
        ActionDTOModelResolverFactory.unregisterActionDTOModelResolver(actionDTOModelResolver);
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

    @Reference(
            name = "org.wso2.carbon.identity.rule.management.service.RuleManagementService",
            service = RuleManagementService.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetRuleManagementService"
    )
    private void setRuleManagementService(RuleManagementService ruleManagementService) {

        ActionMgtServiceComponentHolder.getInstance().setRuleManagementService(ruleManagementService);
        LOG.debug("RuleManagementService set in ActionMgtServiceComponentHolder bundle.");
    }

    private void unsetRuleManagementService(RuleManagementService ruleManagementService) {

        ActionMgtServiceComponentHolder.getInstance().setRuleManagementService(null);
        LOG.debug("RuleManagementService unset in ActionMgtServiceComponentHolder bundle.");
    }

    @Reference(
            name = "action.ActionValidator",
            service = ActionValidator.class,
            cardinality = ReferenceCardinality.MULTIPLE,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetActionValidator"
    )
    protected void setActionValidator(ActionValidator actionValidator) {

        LOG.debug("Registering ActionValidator: " + actionValidator.getClass().getName() +
                " in the ActionMgtServiceComponent.");
        ActionValidatorFactory.registerActionValidatorFactory(actionValidator);
    }

    protected void unsetActionValidator(ActionValidator actionValidator) {

        LOG.debug("Unregistering ActionValidator: " + actionValidator.getClass().getName() +
                " in the ActionMgtServiceComponent.");
        ActionValidatorFactory.unregisterActionValidatorFactory(actionValidator);
    }
}
