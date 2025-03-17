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

package org.wso2.carbon.identity.rule.management.internal.component;

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
import org.wso2.carbon.identity.rule.management.api.service.RuleManagementService;
import org.wso2.carbon.identity.rule.management.internal.service.impl.RuleManagementServiceImpl;
import org.wso2.carbon.identity.rule.metadata.api.service.RuleMetadataService;

/**
 * Rule Management Service Component.
 */
@Component(
        name = "rule.management.service.component",
        immediate = true
)
public class RuleManagementServiceComponent {

    private static final Log LOG = LogFactory.getLog(RuleManagementServiceComponent.class);

    @Activate
    protected void activate(ComponentContext context) {

        try {
            BundleContext bundleCtx = context.getBundleContext();

            bundleCtx.registerService(RuleManagementService.class.getName(),
                    RuleManagementServiceImpl.getInstance(), null);
            LOG.debug("Rule management bundle is activated.");
        } catch (Throwable e) {
            LOG.error("Error while initializing rule management service component.", e);
        }
    }

    @Deactivate
    protected void deactivate(ComponentContext context) {

        try {
            BundleContext bundleCtx = context.getBundleContext();
            bundleCtx.ungetService(bundleCtx.getServiceReference(RuleManagementService.class));
            LOG.debug("Rule management bundle is deactivated.");
        } catch (Throwable e) {
            LOG.error("Error while deactivating rule management service component.", e);
        }
    }

    @Reference(
            name = "rule.metadata.service.component",
            service = RuleMetadataService.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetRuleMetadataService"
    )
    protected void setRuleMetadataService(RuleMetadataService ruleMetadataService) {

        if (LOG.isDebugEnabled()) {
            LOG.debug("Registering a reference for RuleMetadataService in the rule management service component.");
        }

        RuleManagementComponentServiceHolder.getInstance().setRuleMetadataService(ruleMetadataService);
    }

    protected void unsetRuleMetadataService(RuleMetadataService ruleMetadataService) {

        if (LOG.isDebugEnabled()) {
            LOG.debug("Unregistering a reference for RuleMetadataService in the rule management service component.");
        }

        RuleManagementComponentServiceHolder.getInstance().setRuleMetadataService(null);
    }
}
